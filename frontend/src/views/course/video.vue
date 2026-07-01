<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="toolbar">
      <el-form-item label="课程">
        <el-select
          v-model="query.courseId"
          filterable
          clearable
          placeholder="请选择课程"
          style="width: 240px"
          @change="handleCourseChange"
        >
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.name"
            :value="course.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="章节">
        <el-select
          v-model="query.chapterId"
          clearable
          placeholder="全部章节"
          style="width: 220px"
          @change="loadAll"
        >
          <el-option
            v-for="chapter in chapters"
            :key="chapter.id"
            :label="chapter.title"
            :value="chapter.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="loadAll">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button type="primary" icon="el-icon-video-camera" @click="openCreateVideo">新增视频</el-button>
        <el-button type="success" icon="el-icon-document" @click="openCreateMaterial">新增资料</el-button>
      </el-form-item>
    </el-form>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="课程视频" name="videos">
        <el-table
          v-loading="loading"
          :data="videos"
          border
          fit
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column label="序号" align="center" width="70">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="title" label="视频标题" min-width="150" />
          <el-table-column prop="courseName" label="课程" min-width="140" />
          <el-table-column prop="chapterTitle" label="章节" min-width="140" />
          <el-table-column label="视频地址" min-width="220">
            <template slot-scope="{ row }">
              <a :href="row.videoUrl" target="_blank" rel="noopener noreferrer">{{ row.videoUrl }}</a>
            </template>
          </el-table-column>
          <el-table-column label="时长" align="center" width="100">
            <template slot-scope="{ row }">{{ formatSeconds(row.duration) }}</template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" align="center" width="80" />
          <el-table-column label="状态" align="center" width="90">
            <template slot-scope="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right" width="160">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="openEditVideo(row)">编辑</el-button>
              <el-button type="text" size="small" style="color: #f56c6c" @click="disableVideo(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && videos.length === 0" class="empty-block">暂无课程视频</div>
      </el-tab-pane>

      <el-tab-pane label="课程资料" name="materials">
        <el-table
          v-loading="loading"
          :data="materials"
          border
          fit
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column label="序号" align="center" width="70">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="title" label="资料标题" min-width="150" />
          <el-table-column prop="courseName" label="课程" min-width="140" />
          <el-table-column prop="chapterTitle" label="章节" min-width="140" />
          <el-table-column prop="fileType" label="类型" align="center" width="90" />
          <el-table-column label="资料地址" min-width="240">
            <template slot-scope="{ row }">
              <a :href="row.fileUrl" target="_blank" rel="noopener noreferrer">{{ row.fileUrl }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" align="center" width="80" />
          <el-table-column label="操作" align="center" fixed="right" width="160">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="openEditMaterial(row)">编辑</el-button>
              <el-button type="text" size="small" style="color: #f56c6c" @click="deleteMaterial(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && materials.length === 0" class="empty-block">暂无课程资料</div>
      </el-tab-pane>

      <el-tab-pane label="学习进度" name="progress">
        <el-table
          v-loading="loading"
          :data="progressList"
          border
          fit
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column label="序号" align="center" width="70">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="studentName" label="学生" align="center" width="120" />
          <el-table-column prop="courseName" label="课程" min-width="140" />
          <el-table-column prop="chapterTitle" label="章节" min-width="140" />
          <el-table-column prop="videoTitle" label="视频" min-width="150" />
          <el-table-column label="观看进度" align="center" width="180">
            <template slot-scope="{ row }">
              <el-progress :percentage="formatRate(row.progressRate)" :stroke-width="10" />
            </template>
          </el-table-column>
          <el-table-column label="观看时长" align="center" width="110">
            <template slot-scope="{ row }">{{ formatSeconds(row.watchedSeconds) }}</template>
          </el-table-column>
          <el-table-column label="是否完成" align="center" width="100">
            <template slot-scope="{ row }">
              <el-tag :type="row.completed === 1 ? 'success' : 'warning'">
                {{ row.completed === 1 ? '已完成' : '学习中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastWatchTime" label="最近观看" align="center" width="170" />
        </el-table>
        <div v-if="!loading && progressList.length === 0" class="empty-block">暂无学习进度</div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="videoDialogTitle" :visible.sync="videoDialogVisible" width="680px">
      <el-form :model="videoForm" label-width="100px">
        <el-form-item label="课程">
          <el-select v-model="videoForm.courseId" filterable placeholder="请选择课程" style="width: 100%" @change="loadFormChapters('video')">
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="videoForm.chapterId" placeholder="请选择章节" style="width: 100%">
            <el-option v-for="chapter in videoFormChapters" :key="chapter.id" :label="chapter.title" :value="chapter.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频标题">
          <el-input v-model="videoForm.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="视频地址">
          <el-input v-model="videoForm.videoUrl" placeholder="支持 mp4/webm/ogg 或可访问的视频 URL" />
        </el-form-item>
        <el-form-item label="上传视频">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeOnlineFileUpload"
            :on-success="handleVideoUploadSuccess"
            :on-error="handleUploadError"
            accept="video/*,.mp4,.webm,.ogg"
          >
            <el-button size="small" icon="el-icon-upload">上传视频文件</el-button>
            <div slot="tip" class="el-upload__tip">也可以不上传，直接填写外部视频地址；单个文件不超过 200MB。</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="videoForm.coverUrl" placeholder="可选，填写封面图片 URL" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="时长秒数">
              <el-input-number v-model="videoForm.duration" :min="0" :step="60" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="videoForm.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="videoForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="视频说明">
          <el-input v-model="videoForm.description" type="textarea" :rows="3" placeholder="可选，说明本节视频内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="videoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVideo">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="materialDialogTitle" :visible.sync="materialDialogVisible" width="620px">
      <el-form :model="materialForm" label-width="100px">
        <el-form-item label="课程">
          <el-select v-model="materialForm.courseId" filterable placeholder="请选择课程" style="width: 100%" @change="loadFormChapters('material')">
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节">
          <el-select v-model="materialForm.chapterId" placeholder="请选择章节" style="width: 100%">
            <el-option v-for="chapter in materialFormChapters" :key="chapter.id" :label="chapter.title" :value="chapter.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资料标题">
          <el-input v-model="materialForm.title" placeholder="请输入资料标题" />
        </el-form-item>
        <el-form-item label="资料地址">
          <el-input v-model="materialForm.fileUrl" placeholder="填写 PDF、文档或外部资料 URL" />
        </el-form-item>
        <el-form-item label="上传资料">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeOnlineFileUpload"
            :on-success="handleMaterialUploadSuccess"
            :on-error="handleUploadError"
          >
            <el-button size="small" icon="el-icon-upload">上传资料文件</el-button>
            <div slot="tip" class="el-upload__tip">支持 PDF、文档、图片等课程资料；单个文件不超过 200MB。</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="资料类型">
          <el-input v-model="materialForm.fileType" placeholder="可选，如 pdf/doc/link" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="materialForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMaterial">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addCourseMaterial,
  addCourseVideo,
  deleteCourseMaterial,
  deleteCourseVideo,
  fetchCourseChapters,
  fetchCourseMaterials,
  fetchCoursePage,
  fetchCourseVideoProgress,
  fetchCourseVideos,
  updateCourseMaterial,
  updateCourseVideo
} from '@/api/course'
import { getToken } from '@/utils/auth'

export default {
  data() {
    return {
      loading: false,
      activeTab: 'videos',
      query: {
        courseId: '',
        chapterId: ''
      },
      courses: [],
      chapters: [],
      videos: [],
      materials: [],
      progressList: [],
      videoDialogVisible: false,
      videoDialogTitle: '新增视频',
      videoFormChapters: [],
      videoForm: this.emptyVideoForm(),
      materialDialogVisible: false,
      materialDialogTitle: '新增资料',
      materialFormChapters: [],
      materialForm: this.emptyMaterialForm(),
      uploadUrl: process.env.VUE_APP_BASE_API + '/upload/file',
      uploadHeaders: { Authorization: getToken() },
      tableHeaderStyle: {
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px'
      }
    }
  },
  created() {
    this.loadCourses()
  },
  methods: {
    emptyVideoForm() {
      return {
        id: null,
        courseId: '',
        chapterId: '',
        title: '',
        description: '',
        videoUrl: '',
        duration: 0,
        coverUrl: '',
        sortOrder: 0,
        status: 1
      }
    },
    emptyMaterialForm() {
      return {
        id: null,
        courseId: '',
        chapterId: '',
        title: '',
        fileUrl: '',
        fileType: '',
        sortOrder: 0
      }
    },
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000 })
      this.courses = (res.data && res.data.records) || []
      const routeCourseId = this.$route.query.courseId ? Number(this.$route.query.courseId) : null
      if (routeCourseId) {
        this.query.courseId = routeCourseId
      } else if (!this.query.courseId && this.courses.length > 0) {
        this.query.courseId = this.courses[0].id
      }
      await this.handleCourseChange()
    },
    async handleCourseChange() {
      this.query.chapterId = ''
      await this.loadChapters()
      await this.loadAll()
    },
    async loadChapters() {
      if (!this.query.courseId) {
        this.chapters = []
        return
      }
      const res = await fetchCourseChapters(this.query.courseId)
      this.chapters = res.data || []
    },
    async loadAll() {
      if (!this.query.courseId) {
        this.videos = []
        this.materials = []
        this.progressList = []
        return
      }
      this.loading = true
      try {
        const params = {
          courseId: this.query.courseId,
          chapterId: this.query.chapterId || null
        }
        const videoRes = await fetchCourseVideos(params)
        const materialRes = await fetchCourseMaterials(params)
        const progressRes = await fetchCourseVideoProgress(params)
        this.videos = videoRes.data || []
        this.materials = materialRes.data || []
        this.progressList = progressRes.data || []
      } finally {
        this.loading = false
      }
    },
    resetQuery() {
      this.query.chapterId = ''
      this.loadAll()
    },
    async loadFormChapters(type) {
      const form = type === 'video' ? this.videoForm : this.materialForm
      if (!form.courseId) {
        if (type === 'video') {
          this.videoFormChapters = []
        } else {
          this.materialFormChapters = []
        }
        return
      }
      const res = await fetchCourseChapters(form.courseId)
      if (type === 'video') {
        this.videoFormChapters = res.data || []
        if (!this.videoFormChapters.some(item => item.id === form.chapterId)) {
          form.chapterId = ''
        }
      } else {
        this.materialFormChapters = res.data || []
        if (!this.materialFormChapters.some(item => item.id === form.chapterId)) {
          form.chapterId = ''
        }
      }
    },
    openCreateVideo() {
      this.videoDialogTitle = '新增视频'
      this.videoForm = this.emptyVideoForm()
      this.videoForm.courseId = this.query.courseId || ''
      this.videoForm.chapterId = this.query.chapterId || ''
      this.loadFormChapters('video')
      this.videoDialogVisible = true
    },
    openEditVideo(row) {
      this.videoDialogTitle = '编辑视频'
      this.videoForm = {
        id: row.id,
        courseId: row.courseId,
        chapterId: row.chapterId,
        title: row.title,
        description: row.description,
        videoUrl: row.videoUrl,
        duration: row.duration || 0,
        coverUrl: row.coverUrl,
        sortOrder: row.sortOrder || 0,
        status: row.status
      }
      this.loadFormChapters('video')
      this.videoDialogVisible = true
    },
    submitVideo() {
      if (!this.videoForm.courseId || !this.videoForm.chapterId || !this.videoForm.title || !this.videoForm.videoUrl) {
        this.$message.warning('请完整填写课程、章节、标题和视频地址')
        return
      }
      const request = this.videoForm.id
        ? updateCourseVideo(this.videoForm.id, this.videoForm)
        : addCourseVideo(this.videoForm)
      request.then(() => {
        this.$message.success(this.videoForm.id ? '视频已更新' : '视频已创建')
        this.videoDialogVisible = false
        this.loadAll()
      })
    },
    disableVideo(row) {
      this.$confirm('确认删除/禁用该课程视频？已有观看进度会保留。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCourseVideo(row.id).then(() => {
          this.$message.success('视频已禁用')
          this.loadAll()
        })
      })
    },
    openCreateMaterial() {
      this.materialDialogTitle = '新增资料'
      this.materialForm = this.emptyMaterialForm()
      this.materialForm.courseId = this.query.courseId || ''
      this.materialForm.chapterId = this.query.chapterId || ''
      this.loadFormChapters('material')
      this.materialDialogVisible = true
    },
    openEditMaterial(row) {
      this.materialDialogTitle = '编辑资料'
      this.materialForm = {
        id: row.id,
        courseId: row.courseId,
        chapterId: row.chapterId,
        title: row.title,
        fileUrl: row.fileUrl,
        fileType: row.fileType,
        sortOrder: row.sortOrder || 0
      }
      this.loadFormChapters('material')
      this.materialDialogVisible = true
    },
    submitMaterial() {
      if (!this.materialForm.courseId || !this.materialForm.chapterId || !this.materialForm.title || !this.materialForm.fileUrl) {
        this.$message.warning('请完整填写课程、章节、标题和资料地址')
        return
      }
      const request = this.materialForm.id
        ? updateCourseMaterial(this.materialForm.id, this.materialForm)
        : addCourseMaterial(this.materialForm)
      request.then(() => {
        this.$message.success(this.materialForm.id ? '资料已更新' : '资料已创建')
        this.materialDialogVisible = false
        this.loadAll()
      })
    },
    deleteMaterial(row) {
      this.$confirm('确认删除该课程资料？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCourseMaterial(row.id).then(() => {
          this.$message.success('资料已删除')
          this.loadAll()
        })
      })
    },
    beforeOnlineFileUpload(file) {
      const maxSize = 200
      const validSize = file.size / 1024 / 1024 <= maxSize
      if (!validSize) {
        this.$message.error(`单个课程文件不能超过 ${maxSize}MB`)
      }
      return validSize
    },
    handleVideoUploadSuccess(response) {
      if (response.code === 1) {
        this.videoForm.videoUrl = response.data
        this.$message.success('视频上传成功，地址已自动填入')
        return
      }
      this.$message.error(response.msg || '视频上传失败')
    },
    handleMaterialUploadSuccess(response, file) {
      if (response.code === 1) {
        this.materialForm.fileUrl = response.data
        if (!this.materialForm.fileType) {
          this.materialForm.fileType = this.inferFileType(file.name)
        }
        this.$message.success('资料上传成功，地址已自动填入')
        return
      }
      this.$message.error(response.msg || '资料上传失败')
    },
    handleUploadError() {
      this.$message.error('文件上传失败，请检查存储配置或稍后重试')
    },
    inferFileType(fileName) {
      const index = fileName ? fileName.lastIndexOf('.') : -1
      return index >= 0 ? fileName.slice(index + 1).toLowerCase() : 'file'
    },
    formatSeconds(seconds) {
      const total = Number(seconds || 0)
      const minutes = Math.floor(total / 60)
      const rest = total % 60
      return `${minutes}:${String(rest).padStart(2, '0')}`
    },
    formatRate(rate) {
      const value = Number(rate || 0)
      if (value < 0) return 0
      if (value > 100) return 100
      return Math.round(value)
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.empty-block {
  color: #909399;
  padding: 34px 0;
  text-align: center;
}

a {
  color: #409eff;
  word-break: break-all;
}
</style>
