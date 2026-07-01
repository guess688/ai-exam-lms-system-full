<template>
  <div class="app-container">
    <div class="legacy-header">
      <el-page-header content="课程详情" @back="$router.back()" />
      <el-button size="mini" type="primary" plain @click="goNewDetail">切换新版</el-button>
    </div>

    <div v-if="course" class="detail-header">
      <div v-if="course.coverUrl" class="detail-cover" :style="{ backgroundImage: 'url(' + course.coverUrl + ')' }" />
      <div v-else class="detail-cover detail-cover-placeholder">{{ course.name }}</div>
      <div class="detail-info">
        <h2>{{ course.name }}</h2>
        <p>{{ course.description || '暂无课程简介' }}</p>
        <div class="detail-meta">
          <el-tag type="success">{{ course.gradeName || '未分配班级' }}</el-tag>
          <span>{{ course.chapterCount || 0 }} 个章节</span>
          <span>{{ course.knowledgePointCount || 0 }} 个知识点</span>
        </div>
      </div>
    </div>

    <div v-if="currentVideo" class="learning-player">
      <div class="player-main">
        <video
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
      </div>
      <div class="player-side">
        <div class="video-title">{{ currentVideo.title }}</div>
        <div class="video-desc">{{ currentVideo.description || '暂无视频说明' }}</div>
        <div class="progress-label">
          <span>学习进度</span>
          <span>{{ formatRate(currentVideo.progressRate) }}%</span>
        </div>
        <el-progress :percentage="formatRate(currentVideo.progressRate)" :stroke-width="12" />
        <el-tag class="completion-tag" :type="currentVideo.completed === 1 ? 'success' : 'warning'">
          {{ currentVideo.completed === 1 ? '已完成' : '学习中' }}
        </el-tag>
        <a class="video-link" :href="currentVideo.videoUrl" target="_blank" rel="noopener noreferrer">新窗口打开视频</a>
      </div>
    </div>

    <el-collapse v-if="chapters.length > 0" v-model="activeChapters">
      <el-collapse-item
        v-for="chapter in chapters"
        :key="chapter.id"
        :name="String(chapter.id)"
      >
        <template slot="title">
          <span class="chapter-title">{{ chapter.title }}</span>
          <el-tag size="mini" type="info">{{ chapter.videoCount || 0 }} 个视频</el-tag>
          <el-tag size="mini" type="success">{{ chapter.knowledgePoints ? chapter.knowledgePoints.length : 0 }} 个知识点</el-tag>
        </template>

        <div class="chapter-desc">{{ chapter.description || '暂无章节说明' }}</div>
        <div class="chapter-summary">
          <span>练习题 {{ chapter.questionCount || 0 }} 道</span>
          <span>相关测验 {{ chapter.examCount || 0 }} 套</span>
          <span>视频完成 {{ chapter.completedVideoCount || 0 }}/{{ chapter.videoCount || 0 }}</span>
        </div>

        <div class="section-title">章节视频</div>
        <div v-if="chapter.videos && chapter.videos.length > 0" class="video-list">
          <button
            v-for="video in chapter.videos"
            :key="video.id"
            class="video-row"
            :class="{ active: currentVideo && currentVideo.id === video.id }"
            type="button"
            @click="selectVideo(video)"
          >
            <span class="video-row-title">{{ video.title }}</span>
            <span class="video-row-meta">{{ formatSeconds(video.duration) }}</span>
            <span class="video-row-progress">{{ formatRate(video.progressRate) }}%</span>
          </button>
        </div>
        <div v-else class="empty-inline">暂无章节视频</div>

        <div class="section-title">课程资料</div>
        <div v-if="chapter.materials && chapter.materials.length > 0" class="material-list">
          <a
            v-for="material in chapter.materials"
            :key="material.id"
            class="material-link"
            :href="material.fileUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            <i class="el-icon-document" />
            <span>{{ material.title }}</span>
            <em>{{ material.fileType || 'link' }}</em>
          </a>
        </div>
        <div v-else class="empty-inline">暂无课程资料</div>

        <div class="section-title">知识点</div>
        <div class="knowledge-list">
          <el-tag
            v-for="point in chapter.knowledgePoints"
            :key="point.id"
            class="knowledge-tag"
            type="success"
            effect="plain"
          >
            {{ point.name }}
          </el-tag>
          <span v-if="!chapter.knowledgePoints || chapter.knowledgePoints.length === 0" class="empty-inline">
            暂无知识点
          </span>
        </div>
      </el-collapse-item>
    </el-collapse>

    <div v-if="!loading && chapters.length === 0" class="empty-block">
      暂无课程章节
    </div>
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
      activeChapters: [],
      currentVideo: null,
      lastReportSecond: 0
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
        this.activeChapters = this.chapters.map(item => String(item.id))
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
            this.selectVideo(target)
            return
          }
        }
      }
      for (const chapter of this.chapters) {
        if (chapter.videos && chapter.videos.length > 0) {
          this.selectVideo(chapter.videos[0])
          return
        }
      }
      this.currentVideo = null
    },
    selectVideo(video) {
      this.reportCurrentProgress()
      this.currentVideo = video
      this.lastReportSecond = Number(video.watchedSeconds || 0)
      this.$nextTick(() => {
        const player = this.$refs.videoPlayer
        const watchedSeconds = Number(video.watchedSeconds || 0)
        const duration = Number(video.duration || 0)
        if (player && watchedSeconds > 0 && (!duration || watchedSeconds < duration)) {
          try {
            player.currentTime = watchedSeconds
          } catch (e) {
            // Some browsers reject seeking before metadata is ready; loadedmetadata handles the next chance.
          }
        }
      })
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
    isStudent() {
      return window.localStorage.getItem('roles') === 'student'
    },
    formatRate(rate) {
      const value = Number(rate || 0)
      if (value < 0) return 0
      if (value > 100) return 100
      return Math.round(value)
    },
    formatSeconds(seconds) {
      const total = Number(seconds || 0)
      const minutes = Math.floor(total / 60)
      const rest = total % 60
      return `${minutes}:${String(rest).padStart(2, '0')}`
    },
    goNewDetail() {
      this.$router.push({ path: '/course-detail', query: this.$route.query })
    }
  }
}
</script>

<style scoped>
.legacy-header {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.detail-header {
  align-items: stretch;
  display: flex;
  gap: 20px;
  margin: 24px 0;
}

.detail-cover {
  background-position: center;
  background-size: cover;
  border-radius: 6px;
  flex: 0 0 240px;
  min-height: 150px;
}

.detail-cover-placeholder {
  align-items: center;
  background: #f3f6fb;
  color: #606266;
  display: flex;
  font-size: 20px;
  justify-content: center;
  padding: 16px;
  text-align: center;
}

.detail-info {
  border-bottom: 1px solid #ebeef5;
  flex: 1;
  padding-bottom: 16px;
}

.detail-info h2 {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  margin: 0 0 12px;
}

.detail-info p {
  color: #606266;
  line-height: 24px;
  margin: 0 0 16px;
}

.detail-meta {
  align-items: center;
  color: #909399;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.learning-player {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  display: grid;
  gap: 18px;
  grid-template-columns: minmax(0, 1fr) 280px;
  margin: 18px 0 24px;
  padding: 16px;
}

.player-main {
  background: #111827;
  border-radius: 4px;
  overflow: hidden;
}

.video-player {
  aspect-ratio: 16 / 9;
  display: block;
  width: 100%;
}

.player-side {
  color: #606266;
  min-width: 0;
}

.video-title {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  line-height: 26px;
  margin-bottom: 8px;
}

.video-desc {
  line-height: 22px;
  margin-bottom: 18px;
}

.progress-label {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.completion-tag {
  margin-top: 14px;
}

.video-link {
  color: #409eff;
  display: block;
  margin-top: 14px;
  word-break: break-all;
}

.chapter-title {
  color: #303133;
  font-weight: 600;
  margin-right: 12px;
}

.chapter-desc {
  color: #606266;
  line-height: 24px;
  margin-bottom: 10px;
}

.chapter-summary {
  color: #909399;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 14px;
}

.section-title {
  color: #303133;
  font-weight: 600;
  margin: 14px 0 10px;
}

.video-list {
  display: grid;
  gap: 8px;
}

.video-row {
  align-items: center;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  color: #606266;
  cursor: pointer;
  display: grid;
  gap: 8px;
  grid-template-columns: minmax(0, 1fr) 80px 64px;
  padding: 10px 12px;
  text-align: left;
}

.video-row.active {
  background: #ecf5ff;
  border-color: #409eff;
  color: #303133;
}

.video-row-title {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-row-meta,
.video-row-progress {
  color: #909399;
  text-align: right;
}

.material-list {
  display: grid;
  gap: 8px;
}

.material-link {
  align-items: center;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  color: #409eff;
  display: grid;
  gap: 8px;
  grid-template-columns: 20px minmax(0, 1fr) 56px;
  padding: 10px 12px;
}

.material-link span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-link em {
  color: #909399;
  font-style: normal;
  text-align: right;
}

.knowledge-list {
  min-height: 32px;
}

.knowledge-tag {
  margin: 0 8px 8px 0;
}

.empty-block,
.empty-inline {
  color: #909399;
}

.empty-block {
  padding: 42px 0;
  text-align: center;
}

@media (max-width: 900px) {
  .learning-player {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .detail-header {
    display: block;
  }

  .detail-cover {
    margin-bottom: 16px;
  }

  .video-row {
    grid-template-columns: 1fr;
  }

  .video-row-meta,
  .video-row-progress {
    text-align: left;
  }
}
</style>
