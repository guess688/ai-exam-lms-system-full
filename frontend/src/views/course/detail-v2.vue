<template>
  <div v-loading="loading" class="course-v2-page">
    <aside class="learn-sidebar">
      <div class="sidebar-head">
        <span>知识点</span>
        <button type="button" class="icon-button" title="折叠目录" @click="catalogCollapsed = !catalogCollapsed">
          <i :class="catalogCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'" />
        </button>
      </div>

      <section class="progress-panel">
        <div class="score-ring">
          <svg viewBox="0 0 120 120" aria-hidden="true">
            <circle cx="60" cy="60" r="48" class="ring-bg" />
            <circle cx="60" cy="60" r="48" class="ring-value" :stroke-dasharray="ringDashArray" transform="rotate(-90 60 60)" />
          </svg>
          <div class="ring-copy">
            <span>当前进度</span>
            <strong>{{ overallProgressText }}</strong>
            <em>满分100</em>
          </div>
        </div>
        <div class="progress-copy">
          <div class="mini-label">
            <span>学习进度</span>
            <button type="button" @click="goOldDetail">详情 &gt;</button>
          </div>
          <el-progress :percentage="overallProgress" :stroke-width="10" :show-text="false" />
          <p>{{ progressSummary }}</p>
        </div>
      </section>

      <div class="catalog-filter">
        <button :class="{ active: catalogFilter === 'all' }" type="button" @click="catalogFilter = 'all'">全部</button>
        <button :class="{ active: catalogFilter === 'video' }" type="button" @click="catalogFilter = 'video'">视频</button>
        <button :class="{ active: catalogFilter === 'material' }" type="button" @click="catalogFilter = 'material'">资料</button>
      </div>

      <div class="catalog-list" :class="{ collapsed: catalogCollapsed }">
        <div v-for="chapter in chapters" :key="chapter.id" class="catalog-group">
          <button class="catalog-chapter" type="button" @click="toggleChapter(chapter.id)">
            <span>{{ chapter.title }}</span>
            <i :class="isChapterOpen(chapter.id) ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
          </button>
          <div v-show="isChapterOpen(chapter.id)" class="catalog-items">
            <button
              v-for="video in filteredVideos(chapter)"
              :key="'video-' + video.id"
              class="catalog-row"
              :class="{ active: currentVideo && currentVideo.id === video.id }"
              type="button"
              @click="selectVideo(video, chapter)"
            >
              <span class="row-type">视频</span>
              <span class="row-title">{{ video.title }}</span>
              <i v-if="video.completed === 1" class="el-icon-success done-icon" />
            </button>
            <a
              v-for="material in filteredMaterials(chapter)"
              :key="'material-' + material.id"
              class="catalog-row material"
              :href="material.fileUrl"
              target="_blank"
              rel="noopener noreferrer"
            >
              <span class="row-type task">资料</span>
              <span class="row-title">{{ material.title }}</span>
              <i class="el-icon-link" />
            </a>
            <div v-if="isCatalogChapterEmpty(chapter)" class="catalog-empty">暂无内容</div>
          </div>
        </div>
      </div>
    </aside>

    <main class="learn-main">
      <header class="lesson-toolbar">
        <div class="lesson-title">
          <span class="lesson-chip">视频</span>
          <h1>{{ currentVideo ? currentVideo.title : courseName }}</h1>
        </div>
        <div class="lesson-actions">
          <button type="button" class="nav-arrow" title="上一节" :disabled="!hasPreviousVideo" @click="selectNeighborVideo(-1)">
            <i class="el-icon-arrow-left" />
          </button>
          <button type="button" class="nav-arrow" title="下一节" :disabled="!hasNextVideo" @click="selectNeighborVideo(1)">
            <i class="el-icon-arrow-right" />
          </button>
          <el-button size="mini" plain @click="goOldDetail">旧版</el-button>
        </div>
      </header>

      <div v-if="noticeVisible" class="notice-bar">
        <i class="el-icon-info" />
        <span>此视频建议在截止时间前完成学习，系统会根据观看进度记录网课成绩。</span>
        <button type="button" title="关闭提示" @click="noticeVisible = false">
          <i class="el-icon-close" />
        </button>
      </div>

      <section class="video-stage">
        <video
          v-if="currentVideo"
          ref="videoPlayer"
          class="video-player"
          :src="currentVideo.videoUrl"
          :poster="currentVideo.coverUrl"
          controls
          preload="metadata"
          @loadedmetadata="handleLoadedMetadata"
          @timeupdate="handleTimeUpdate"
          @pause="reportCurrentProgress"
          @ended="handleVideoEnded"
        >
          当前浏览器不支持视频播放。
        </video>
        <div v-else class="video-empty">
          <i class="el-icon-video-camera" />
          <span>当前课程暂无可播放视频</span>
        </div>
      </section>

      <section class="study-tabs">
        <div class="tab-head">
          <button :class="{ active: activeTab === 'guide' }" type="button" @click="activeTab = 'guide'">知识导引</button>
          <button :class="{ active: activeTab === 'draft' }" type="button" @click="activeTab = 'draft'">讲稿</button>
          <button :class="{ active: activeTab === 'discussion' }" type="button" @click="activeTab = 'discussion'">讨论 (0)</button>
          <div class="guide-search">
            <i class="el-icon-search" />
            <input v-model="guideKeyword" type="text" placeholder="搜索知识导引" />
          </div>
        </div>

        <div v-if="activeTab === 'guide'" class="timeline">
          <article v-for="item in guideItems" :key="item.key" class="timeline-row">
            <div class="timeline-time">{{ item.time }}</div>
            <div class="timeline-dot" />
            <div class="timeline-card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.text }}</p>
            </div>
          </article>
          <div v-if="guideItems.length === 0" class="soft-empty">暂无匹配的知识导引</div>
        </div>

        <div v-if="activeTab === 'draft'" class="draft-panel">
          <h3>{{ currentChapter ? currentChapter.title : courseName }}</h3>
          <p>{{ currentChapter && currentChapter.description ? currentChapter.description : '本节讲稿将围绕课程章节目标、知识点和视频内容展开，建议结合左侧目录按顺序完成学习。' }}</p>
          <div class="knowledge-pills">
            <span v-for="point in currentKnowledgePoints" :key="point.id">{{ point.name }}</span>
            <em v-if="currentKnowledgePoints.length === 0">暂无知识点</em>
          </div>
        </div>

        <div v-if="activeTab === 'discussion'" class="draft-panel">
          <h3>课堂讨论</h3>
          <p>当前章节暂未开放讨论。你可以先记录问题，在右侧 AI 学伴中进行知识点答疑。</p>
        </div>
      </section>
    </main>

    <aside class="ai-panel">
      <header class="ai-head">
        <div class="ai-avatar">AI</div>
        <strong>24H智能学伴</strong>
        <span>历史会话</span>
        <i class="el-icon-close" />
      </header>

      <div class="chat-stream">
        <section class="ai-card">
          <div class="ai-label">
            <span>AI导练</span>
            <em>智能作业</em>
          </div>
          <div class="question-card">
            <p>基于所学知识点，以下是为你准备的一道测试题：</p>
            <strong>【多选题】</strong>
            <p>{{ aiQuestionStem }}</p>
            <button
              v-for="option in aiOptions"
              :key="option.key"
              type="button"
              :class="{ selected: selectedAiOption === option.key }"
              @click="selectedAiOption = option.key"
            >
              <span>{{ option.key }}</span>
              {{ option.text }}
            </button>
          </div>
          <small>习题内容由 AI 生成，仅供参考</small>
        </section>
      </div>

      <div class="ai-actions">
        <button type="button">知识点答疑</button>
        <button type="button">更多AI应用 <i class="el-icon-arrow-right" /></button>
      </div>

      <div class="ai-input">
        <textarea v-model="aiInput" rows="3" placeholder="学习有困惑？问问你的 AI 学伴吧~" />
        <button type="button" @click="askAi">
          <i class="el-icon-s-promotion" />
        </button>
      </div>
      <p class="ai-footnote">以上内容均由AI生成，仅供参考和借鉴。</p>
    </aside>
  </div>
</template>

<script>
import { fetchCourseDetail, fetchCourseLearning, reportVideoProgress } from '@/api/course'

export default {
  data() {
    return {
      loading: false,
      course: null,
      chapters: [],
      openChapterIds: [],
      currentVideo: null,
      currentChapter: null,
      lastReportSecond: 0,
      activeTab: 'guide',
      guideKeyword: '',
      catalogFilter: 'all',
      catalogCollapsed: false,
      noticeVisible: true,
      selectedAiOption: '',
      aiInput: ''
    }
  },
  computed: {
    courseName() {
      return this.course ? this.course.name : '课程学习'
    },
    allVideos() {
      const videos = []
      this.chapters.forEach(chapter => {
        ;(chapter.videos || []).forEach(video => {
          videos.push({ video, chapter })
        })
      })
      return videos
    },
    currentVideoIndex() {
      if (!this.currentVideo) return -1
      return this.allVideos.findIndex(item => item.video.id === this.currentVideo.id)
    },
    hasPreviousVideo() {
      return this.currentVideoIndex > 0
    },
    hasNextVideo() {
      return this.currentVideoIndex >= 0 && this.currentVideoIndex < this.allVideos.length - 1
    },
    overallProgress() {
      const videos = this.allVideos.map(item => item.video)
      if (!videos.length) return 0
      const total = videos.reduce((sum, video) => sum + Number(video.progressRate || 0), 0)
      return Math.min(100, Math.round(total / videos.length))
    },
    overallProgressText() {
      return this.overallProgress.toFixed(2)
    },
    progressSummary() {
      const videos = this.allVideos.map(item => item.video)
      const completed = videos.filter(video => video.completed === 1).length
      return `已完成 ${completed}/${videos.length || 0} 个视频，继续保持学习节奏。`
    },
    ringDashArray() {
      const radius = 48
      const circumference = 2 * Math.PI * radius
      const value = (this.overallProgress / 100) * circumference
      return `${value} ${circumference - value}`
    },
    currentKnowledgePoints() {
      return (this.currentChapter && this.currentChapter.knowledgePoints) || []
    },
    guideItems() {
      const base = this.currentKnowledgePoints.length
        ? this.currentKnowledgePoints.map((point, index) => ({
          key: `kp-${point.id}`,
          time: this.guideTime(index),
          title: point.name,
          text: point.description || `${point.name} 是本节课的重要知识点，建议结合视频讲解、课后练习和错题订正进行巩固。`
        }))
        : [{
          key: 'intro',
          time: '00:00',
          title: this.currentVideo ? this.currentVideo.title : this.courseName,
          text: this.currentVideo && this.currentVideo.description
            ? this.currentVideo.description
            : '本节内容暂无详细知识点说明，建议先完整观看视频，再结合章节资料进行复习。'
        }]
      const keyword = this.guideKeyword.trim()
      if (!keyword) return base
      return base.filter(item => item.title.indexOf(keyword) > -1 || item.text.indexOf(keyword) > -1)
    },
    aiQuestionStem() {
      const name = this.currentKnowledgePoints[0] ? this.currentKnowledgePoints[0].name : (this.currentChapter ? this.currentChapter.title : '本节课')
      return `学习 ${name} 时，哪些做法有助于真正掌握本节内容？`
    },
    aiOptions() {
      const name = this.currentKnowledgePoints[0] ? this.currentKnowledgePoints[0].name : '核心知识点'
      return [
        { key: 'A', text: `先完整观看视频，再整理 ${name} 的关键概念` },
        { key: 'B', text: '只看一遍视频，不做任何记录或练习' },
        { key: 'C', text: '结合章节资料和错题进行二次复盘' },
        { key: 'D', text: '跳过基础题，直接做高难度题' }
      ]
    }
  },
  created() {
    this.loadDetail()
  },
  beforeDestroy() {
    this.reportCurrentProgress()
  },
  methods: {
    async loadDetail() {
      const courseId = this.$route.query.id
      if (!courseId) {
        this.$message.warning('缺少课程 ID')
        return
      }
      this.loading = true
      try {
        const courseRes = await fetchCourseDetail(courseId)
        const learningRes = await fetchCourseLearning(courseId)
        this.course = courseRes.data
        this.chapters = learningRes.data || []
        this.openChapterIds = this.chapters.map(item => item.id)
        this.selectFirstVideo()
      } finally {
        this.loading = false
      }
    },
    selectFirstVideo() {
      const targetVideoId = this.$route.query.videoId ? Number(this.$route.query.videoId) : null
      for (const chapter of this.chapters) {
        if (chapter.videos && chapter.videos.length > 0) {
          const target = targetVideoId ? chapter.videos.find(video => video.id === targetVideoId) : null
          if (target) {
            this.selectVideo(target, chapter)
            return
          }
        }
      }
      const first = this.allVideos[0]
      if (first) {
        this.selectVideo(first.video, first.chapter)
      }
    },
    selectVideo(video, chapter) {
      this.reportCurrentProgress()
      this.currentVideo = video
      this.currentChapter = chapter || this.findChapterByVideo(video.id)
      this.lastReportSecond = Number(video.watchedSeconds || 0)
      if (this.currentChapter && !this.isChapterOpen(this.currentChapter.id)) {
        this.openChapterIds.push(this.currentChapter.id)
      }
      this.$nextTick(() => {
        const player = this.$refs.videoPlayer
        const watchedSeconds = Number(video.watchedSeconds || 0)
        const duration = Number(video.duration || 0)
        if (player && watchedSeconds > 0 && (!duration || watchedSeconds < duration)) {
          try {
            player.currentTime = watchedSeconds
          } catch (e) {
            // Some browsers reject seeking before metadata is ready.
          }
        }
      })
    },
    selectNeighborVideo(offset) {
      const next = this.allVideos[this.currentVideoIndex + offset]
      if (next) {
        this.selectVideo(next.video, next.chapter)
      }
    },
    findChapterByVideo(videoId) {
      return this.chapters.find(chapter => (chapter.videos || []).some(video => video.id === videoId)) || null
    },
    toggleChapter(id) {
      if (this.isChapterOpen(id)) {
        this.openChapterIds = this.openChapterIds.filter(item => item !== id)
      } else {
        this.openChapterIds.push(id)
      }
    },
    isChapterOpen(id) {
      return this.openChapterIds.indexOf(id) > -1
    },
    filteredVideos(chapter) {
      if (this.catalogFilter === 'material') return []
      return chapter.videos || []
    },
    filteredMaterials(chapter) {
      if (this.catalogFilter === 'video') return []
      return chapter.materials || []
    },
    isCatalogChapterEmpty(chapter) {
      return this.filteredVideos(chapter).length === 0 && this.filteredMaterials(chapter).length === 0
    },
    guideTime(index) {
      const minutes = index === 0 ? 0 : index * 3 + 4
      return `${String(minutes).padStart(2, '0')}:${index === 0 ? '00' : '15'}`
    },
    handleLoadedMetadata(event) {
      if (!this.currentVideo) return
      const duration = Math.floor(event.target.duration || 0)
      if (duration > 0 && !this.currentVideo.duration) {
        this.$set(this.currentVideo, 'duration', duration)
      }
      const watchedSeconds = Number(this.currentVideo.watchedSeconds || 0)
      if (watchedSeconds > 0 && watchedSeconds < duration) {
        event.target.currentTime = watchedSeconds
      }
    },
    handleTimeUpdate(event) {
      if (!this.isStudent() || !this.currentVideo) return
      const current = Math.floor(event.target.currentTime || 0)
      if (current - this.lastReportSecond >= 10) {
        this.lastReportSecond = current
        this.reportCurrentProgress()
      }
    },
    handleVideoEnded() {
      if (!this.currentVideo) return
      this.$set(this.currentVideo, 'progressRate', 100)
      this.$set(this.currentVideo, 'completed', 1)
      this.reportCurrentProgress()
    },
    async reportCurrentProgress() {
      if (!this.isStudent() || !this.currentVideo || !this.$refs.videoPlayer) return
      const player = this.$refs.videoPlayer
      const watchedSeconds = Math.floor(player.currentTime || this.currentVideo.watchedSeconds || 0)
      const duration = Math.floor(player.duration || this.currentVideo.duration || 0)
      if (watchedSeconds <= 0 && duration <= 0) return
      try {
        const res = await reportVideoProgress({
          videoId: this.currentVideo.id,
          watchedSeconds,
          duration
        })
        this.applyProgress(res.data)
      } catch (e) {
        // Request interceptor has already shown the error message.
      }
    },
    applyProgress(progress) {
      if (!progress) return
      this.chapters.forEach(chapter => {
        ;(chapter.videos || []).forEach(video => {
          if (video.id === progress.videoId) {
            this.$set(video, 'watchedSeconds', progress.watchedSeconds)
            this.$set(video, 'progressRate', progress.progressRate)
            this.$set(video, 'completed', progress.completed)
            this.$set(video, 'lastWatchTime', progress.lastWatchTime)
          }
        })
        const videos = chapter.videos || []
        const completedCount = videos.filter(video => video.completed === 1).length
        this.$set(chapter, 'completedVideoCount', completedCount)
        this.$set(chapter, 'progressRate', videos.length ? Math.round((completedCount * 10000) / videos.length) / 100 : 0)
      })
    },
    askAi() {
      if (!this.aiInput.trim()) {
        this.$message.info('请输入你的问题')
        return
      }
      this.$message.success('AI 学伴已收到问题，后续可接入真实问答服务')
      this.aiInput = ''
    },
    goOldDetail() {
      this.$router.push({ path: '/old-course-detail', query: this.$route.query })
    },
    isStudent() {
      return window.localStorage.getItem('roles') === 'student'
    }
  }
}
</script>

<style scoped>
.course-v2-page {
  background: linear-gradient(120deg, #5b86f7 0%, #edf4ff 58%, #f5f9ff 100%);
  color: #193764;
  display: grid;
  gap: 32px;
  grid-template-columns: 332px minmax(520px, 1fr) 390px;
  min-height: calc(100vh - 84px);
  overflow: hidden;
  padding: 26px 34px 0;
}

.sidebar-head,
.lesson-toolbar,
.ai-head,
.tab-head,
.catalog-row,
.catalog-chapter,
.ai-actions {
  align-items: center;
  display: flex;
}

.sidebar-head {
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  justify-content: space-between;
  margin-bottom: 12px;
}

.icon-button,
.nav-arrow,
.notice-bar button,
.catalog-filter button,
.catalog-chapter,
.catalog-row,
.tab-head button,
.ai-actions button,
.ai-input button {
  border: 0;
  cursor: pointer;
  font-family: inherit;
}

.icon-button {
  background: transparent;
  color: #eaf2ff;
  font-size: 18px;
  height: 28px;
  width: 28px;
}

.progress-panel {
  align-items: center;
  background: rgba(255, 255, 255, 0.52);
  border: 1px solid rgba(255, 255, 255, 0.52);
  border-radius: 6px;
  box-shadow: 0 18px 42px rgba(47, 96, 184, 0.18);
  display: grid;
  gap: 18px;
  grid-template-columns: 96px 1fr;
  margin-bottom: 14px;
  padding: 16px;
}

.score-ring {
  height: 96px;
  position: relative;
  width: 96px;
}

.score-ring svg {
  height: 96px;
  width: 96px;
}

.ring-bg,
.ring-value {
  fill: none;
  stroke-width: 11;
}

.ring-bg {
  stroke: rgba(101, 145, 240, 0.22);
}

.ring-value {
  stroke: #5f8df6;
  stroke-linecap: round;
}

.ring-copy {
  align-items: center;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  left: 0;
  position: absolute;
  right: 0;
  top: 0;
}

.ring-copy span,
.ring-copy em {
  color: #6b83b4;
  font-size: 11px;
  font-style: normal;
  line-height: 14px;
}

.ring-copy strong {
  color: #315bc3;
  font-size: 24px;
  font-weight: 800;
  line-height: 30px;
}

.mini-label {
  align-items: center;
  color: #617aa9;
  display: flex;
  font-size: 13px;
  justify-content: space-between;
  margin-bottom: 12px;
}

.mini-label button {
  background: transparent;
  border: 0;
  color: #5c7fc7;
  cursor: pointer;
}

.progress-copy p {
  color: #6e83ad;
  font-size: 12px;
  line-height: 20px;
  margin: 10px 0 0;
}

.catalog-filter {
  display: flex;
  gap: 8px;
  margin: 10px 0 12px;
}

.catalog-filter button {
  background: transparent;
  color: #315184;
  font-size: 13px;
  padding: 6px 8px;
}

.catalog-filter button.active {
  color: #163e95;
  font-weight: 700;
}

.catalog-list {
  max-height: calc(100vh - 260px);
  overflow-y: auto;
  padding-right: 8px;
}

.catalog-list.collapsed .catalog-items {
  display: none;
}

.catalog-group {
  margin-bottom: 5px;
}

.catalog-chapter {
  background: transparent;
  color: #284a80;
  font-size: 14px;
  justify-content: space-between;
  line-height: 22px;
  padding: 10px 8px;
  text-align: left;
  width: 100%;
}

.catalog-items {
  display: grid;
  gap: 6px;
  padding: 0 2px 4px 12px;
}

.catalog-row {
  background: transparent;
  border-radius: 6px;
  color: #2c4b7d;
  display: grid;
  gap: 8px;
  grid-template-columns: 34px minmax(0, 1fr) 18px;
  min-height: 38px;
  padding: 8px 10px;
  text-align: left;
  text-decoration: none;
}

.catalog-row.active {
  background: #74a2ff;
  color: #fff;
}

.catalog-row.material {
  color: #315184;
}

.row-type {
  align-items: center;
  border: 1px solid currentColor;
  border-radius: 4px;
  display: inline-flex;
  font-size: 12px;
  height: 18px;
  justify-content: center;
}

.row-type.task {
  color: #5cb45d;
}

.row-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.done-icon {
  color: #28c36a;
}

.catalog-empty {
  color: #8aa0c8;
  font-size: 13px;
  padding: 8px 10px;
}

.learn-main {
  min-width: 0;
  padding-bottom: 40px;
}

.lesson-toolbar {
  color: #fff;
  height: 40px;
  justify-content: space-between;
}

.lesson-title {
  align-items: center;
  display: flex;
  min-width: 0;
}

.lesson-chip {
  background: rgba(255, 255, 255, 0.18);
  border-radius: 4px;
  font-size: 13px;
  margin-right: 10px;
  padding: 5px 8px;
}

.lesson-title h1 {
  font-size: 16px;
  font-weight: 700;
  line-height: 24px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lesson-actions {
  align-items: center;
  display: flex;
  gap: 10px;
}

.nav-arrow {
  background: transparent;
  color: #fff;
  font-size: 26px;
  height: 34px;
  width: 34px;
}

.nav-arrow:disabled {
  cursor: not-allowed;
  opacity: 0.35;
}

.notice-bar {
  align-items: center;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid #2f74ef;
  border-radius: 4px;
  color: #243b63;
  display: flex;
  font-size: 13px;
  gap: 8px;
  line-height: 20px;
  margin: 0 0 22px;
  min-height: 36px;
  padding: 8px 10px;
}

.notice-bar i {
  color: #2f74ef;
}

.notice-bar span {
  flex: 1;
}

.notice-bar button {
  background: transparent;
  color: #8da1c3;
  height: 24px;
  width: 24px;
}

.video-stage {
  background: #071022;
  border-radius: 4px;
  box-shadow: 0 24px 60px rgba(23, 54, 120, 0.28);
  min-height: 360px;
  overflow: hidden;
}

.video-player {
  aspect-ratio: 16 / 9;
  display: block;
  width: 100%;
}

.video-empty {
  align-items: center;
  color: #b8c7e8;
  display: flex;
  flex-direction: column;
  font-size: 15px;
  gap: 12px;
  height: 420px;
  justify-content: center;
}

.video-empty i {
  font-size: 42px;
}

.study-tabs {
  background: rgba(255, 255, 255, 0.72);
  border-radius: 0 0 4px 4px;
  box-shadow: 0 20px 42px rgba(74, 116, 184, 0.12);
  min-height: 310px;
}

.tab-head {
  border-bottom: 1px solid rgba(111, 150, 218, 0.22);
  gap: 24px;
  min-height: 46px;
  padding: 0 8px;
}

.tab-head button {
  background: transparent;
  color: #26436e;
  font-size: 15px;
  height: 46px;
  padding: 0 4px;
  position: relative;
}

.tab-head button.active {
  color: #1b5fe9;
  font-weight: 700;
}

.tab-head button.active::after {
  background: #1b72ff;
  border-radius: 2px;
  bottom: 0;
  content: "";
  height: 3px;
  left: 8px;
  position: absolute;
  right: 8px;
}

.guide-search {
  align-items: center;
  background: #fff;
  border: 1px solid #d7e2f3;
  border-radius: 6px;
  display: flex;
  margin-left: auto;
  padding: 0 10px;
  width: 230px;
}

.guide-search i {
  color: #a4b3cc;
  margin-right: 6px;
}

.guide-search input {
  border: 0;
  color: #315184;
  font-size: 13px;
  height: 30px;
  outline: none;
  width: 100%;
}

.timeline {
  padding: 16px 18px 28px;
}

.timeline-row {
  display: grid;
  gap: 16px;
  grid-template-columns: 48px 12px minmax(0, 1fr);
  margin-bottom: 18px;
}

.timeline-time {
  color: #7b8dad;
  font-size: 13px;
  line-height: 22px;
  text-align: right;
}

.timeline-dot {
  background: #2f7bff;
  border-radius: 50%;
  height: 6px;
  margin-top: 8px;
  position: relative;
  width: 6px;
}

.timeline-dot::after {
  background: rgba(47, 123, 255, 0.18);
  content: "";
  height: 84px;
  left: 2px;
  position: absolute;
  top: 12px;
  width: 1px;
}

.timeline-card,
.draft-panel {
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(216, 226, 244, 0.9);
  border-radius: 6px;
  padding: 14px 18px;
}

.timeline-card h3,
.draft-panel h3 {
  color: #17315b;
  font-size: 16px;
  font-weight: 700;
  line-height: 24px;
  margin: 0 0 8px;
}

.timeline-card p,
.draft-panel p {
  color: #27456f;
  font-size: 14px;
  line-height: 25px;
  margin: 0;
}

.draft-panel {
  margin: 18px;
}

.knowledge-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.knowledge-pills span {
  background: #eaf2ff;
  border: 1px solid #bdd4ff;
  border-radius: 4px;
  color: #1f5ec9;
  font-size: 13px;
  padding: 5px 8px;
}

.knowledge-pills em,
.soft-empty {
  color: #8ba1c0;
  font-style: normal;
}

.soft-empty {
  padding: 26px;
  text-align: center;
}

.ai-panel {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.62);
  box-shadow: 0 18px 50px rgba(54, 105, 182, 0.16);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 90px);
  min-width: 0;
  padding: 20px 20px 12px;
}

.ai-head {
  color: #17315b;
  gap: 10px;
  margin-bottom: 14px;
}

.ai-avatar {
  align-items: center;
  background: linear-gradient(135deg, #55d0ff, #705fff);
  border-radius: 50%;
  color: #fff;
  display: flex;
  font-size: 12px;
  font-weight: 800;
  height: 26px;
  justify-content: center;
  width: 26px;
}

.ai-head strong {
  flex: 1;
  font-size: 15px;
}

.ai-head span {
  color: #5f77a5;
  font-size: 12px;
}

.chat-stream {
  flex: 1;
  overflow-y: auto;
}

.ai-card {
  border-left: 3px solid #82b7ff;
  padding-left: 12px;
}

.ai-label {
  align-items: center;
  color: #5e78a6;
  display: flex;
  font-size: 13px;
  gap: 6px;
  margin: 4px 0 8px;
}

.ai-label em {
  background: #8dc7ff;
  border-radius: 4px;
  color: #fff;
  font-size: 11px;
  font-style: normal;
  padding: 2px 6px;
}

.question-card {
  background: #fff;
  border: 2px solid #8bc4ff;
  border-radius: 8px;
  padding: 16px;
}

.question-card p {
  color: #213c66;
  font-size: 13px;
  line-height: 22px;
  margin: 0 0 12px;
}

.question-card strong {
  color: #071d40;
  display: block;
  font-size: 14px;
  margin-bottom: 10px;
}

.question-card button {
  align-items: center;
  background: #f3f7fd;
  border: 1px solid transparent;
  border-radius: 6px;
  color: #18345f;
  cursor: pointer;
  display: grid;
  font-family: inherit;
  font-size: 13px;
  gap: 10px;
  grid-template-columns: 30px 1fr;
  line-height: 21px;
  margin-top: 12px;
  padding: 10px;
  text-align: left;
  width: 100%;
}

.question-card button.selected {
  background: #eaf2ff;
  border-color: #5b94ff;
}

.question-card button span {
  align-items: center;
  background: #e2ecfb;
  border-radius: 5px;
  color: #5a78aa;
  display: inline-flex;
  font-weight: 700;
  height: 26px;
  justify-content: center;
  width: 26px;
}

.ai-card small {
  color: #90a1bf;
  display: block;
  font-size: 12px;
  margin: 10px 0 20px 20px;
}

.ai-actions {
  gap: 8px;
  margin: 10px 0 12px;
}

.ai-actions button {
  background: #e7f0ff;
  border-radius: 6px;
  color: #3f65a7;
  font-size: 13px;
  padding: 8px 12px;
}

.ai-input {
  align-items: flex-end;
  background: #fff;
  border: 1px solid #a8c8ff;
  border-radius: 8px;
  display: flex;
  gap: 8px;
  padding: 10px;
}

.ai-input textarea {
  border: 0;
  color: #26436e;
  flex: 1;
  font-family: inherit;
  font-size: 14px;
  line-height: 22px;
  outline: none;
  resize: none;
}

.ai-input button {
  align-items: center;
  background: #d1d9e8;
  border-radius: 50%;
  color: #fff;
  display: flex;
  height: 30px;
  justify-content: center;
  width: 30px;
}

.ai-footnote {
  color: #8a9cbb;
  font-size: 11px;
  line-height: 18px;
  margin: 8px 0 0;
  text-align: center;
}

@media (max-width: 1400px) {
  .course-v2-page {
    gap: 22px;
    grid-template-columns: 300px minmax(480px, 1fr) 340px;
    padding-left: 22px;
    padding-right: 22px;
  }
}

@media (max-width: 1180px) {
  .course-v2-page {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .ai-panel {
    grid-column: 1 / -1;
    max-height: none;
  }
}

@media (max-width: 860px) {
  .course-v2-page {
    display: block;
    min-height: auto;
    overflow: visible;
    padding: 16px;
  }

  .learn-sidebar,
  .learn-main,
  .ai-panel {
    margin-bottom: 18px;
  }

  .catalog-list {
    max-height: none;
  }

  .lesson-toolbar {
    color: #17315b;
  }

  .nav-arrow {
    color: #315184;
  }

  .guide-search {
    width: 100%;
  }

  .tab-head {
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 10px;
    padding: 8px;
  }
}
</style>
