<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="toolbar">
      <el-form-item label="任务标题">
        <el-input v-model="query.title" clearable placeholder="请输入任务标题" />
      </el-form-item>
      <el-form-item label="任务类型">
        <el-select v-model="query.taskType" clearable placeholder="全部类型">
          <el-option v-for="item in taskTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="searchTasks">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button type="primary" icon="el-icon-plus" @click="openCreateDialog">发布任务</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="data.records"
      border
      fit
      highlight-current-row
      :header-cell-style="tableHeaderStyle"
    >
      <el-table-column label="序号" align="center" width="70">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="title" label="任务标题" min-width="170" />
      <el-table-column prop="taskTypeName" label="类型" align="center" width="120" />
      <el-table-column label="目标" min-width="140">
        <template slot-scope="{ row }">
          {{ row.targetTypeName }}：{{ row.targetClassName || row.targetStudentName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="关联资源" min-width="180">
        <template slot-scope="{ row }">{{ relatedText(row) }}</template>
      </el-table-column>
      <el-table-column label="完成率" align="center" width="170">
        <template slot-scope="{ row }">
          <el-progress :percentage="formatRate(row.completionRate)" :stroke-width="10" />
        </template>
      </el-table-column>
      <el-table-column label="完成人数" align="center" width="100">
        <template slot-scope="{ row }">{{ row.completedCount || 0 }}/{{ row.totalCount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" align="center" width="170" />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="180">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openRecordDialog(row)">完成情况</el-button>
          <el-button type="text" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="text" size="small" style="color: #f56c6c" @click="disableTask(row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        :current-page="data.current"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="data.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="data.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="760px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="任务标题">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选，说明任务要求" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="任务类型">
              <el-select v-model="form.taskType" placeholder="请选择任务类型" style="width: 100%">
                <el-option v-for="item in taskTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="form.deadline"
                type="datetime"
                value-format="yyyy-MM-dd'T'HH:mm:ss"
                placeholder="可选"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="目标类型">
              <el-radio-group v-model="form.targetType" @change="handleTargetTypeChange">
                <el-radio-button label="CLASS">班级</el-radio-button>
                <el-radio-button label="STUDENT">学生</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标班级" v-if="form.targetType === 'CLASS'">
              <el-select v-model="form.targetClassId" filterable placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in classes" :key="item.id" :label="item.gradeName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标学生" v-else>
              <el-select v-model="form.targetStudentId" filterable remote clearable placeholder="请选择学生" style="width: 100%" :remote-method="loadStudents">
                <el-option v-for="item in students" :key="item.id" :label="item.realName || item.userName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">教学范围</el-divider>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="课程">
              <el-select v-model="form.courseId" filterable clearable placeholder="可选" style="width: 100%" @change="handleCourseChange">
                <el-option v-for="item in courses" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="章节">
              <el-select v-model="form.chapterId" clearable placeholder="可选" style="width: 100%" @change="handleChapterChange">
                <el-option v-for="item in chapters" :key="item.id" :label="item.title" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="知识点">
              <el-select v-model="form.knowledgePointId" clearable placeholder="可选" style="width: 100%">
                <el-option v-for="item in knowledgePoints" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">关联资源</el-divider>
        <el-form-item v-if="form.taskType === 'EXAM' || form.taskType === 'WRONG_QUESTION'" label="关联考试">
          <el-select v-model="form.relatedExamId" filterable clearable placeholder="请选择考试" style="width: 100%">
            <el-option v-for="item in exams" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.taskType === 'PRACTICE'" label="关联题库">
          <el-select v-model="form.relatedPaperId" filterable placeholder="请选择练习题库" style="width: 100%">
            <el-option v-for="item in repos" :key="item.id" :label="item.title || item.repoTitle" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.taskType === 'VIDEO'" label="关联视频">
          <el-select v-model="form.relatedVideoId" filterable placeholder="请选择课程视频" style="width: 100%">
            <el-option v-for="item in videos" :key="item.id" :label="videoOptionLabel(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTask">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="任务完成情况" :visible.sync="recordDialogVisible" width="780px">
      <el-table :data="records" border fit :header-cell-style="tableHeaderStyle">
        <el-table-column prop="studentName" label="学生" min-width="130" />
        <el-table-column prop="statusName" label="状态" align="center" width="100" />
        <el-table-column label="进度" align="center" width="180">
          <template slot-scope="{ row }">
            <el-progress :percentage="formatRate(row.progressRate)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" align="center" width="170" />
        <el-table-column prop="updateTime" label="更新时间" align="center" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { fetchClasses } from '@/api/class_'
import { fetchCourseChapters, fetchCoursePage, fetchCourseVideos, fetchKnowledgePoints } from '@/api/course'
import { examPaging } from '@/api/exam'
import { fetchLearningTaskPage, createLearningTask, updateLearningTask, disableLearningTask, fetchLearningTaskRecords } from '@/api/learningTask'
import { repoPaging } from '@/api/repo'
import { userPaging } from '@/api/user'

export default {
  data() {
    return {
      loading: false,
      pageNum: 1,
      pageSize: 10,
      query: {
        title: '',
        taskType: '',
        status: ''
      },
      data: {
        records: [],
        total: 0,
        size: 10,
        current: 1
      },
      taskTypes: [
        { label: '考试任务', value: 'EXAM' },
        { label: '练习任务', value: 'PRACTICE' },
        { label: '网课任务', value: 'VIDEO' },
        { label: '错题订正任务', value: 'WRONG_QUESTION' },
        { label: '复习任务', value: 'REVIEW' }
      ],
      classes: [],
      students: [],
      courses: [],
      chapters: [],
      knowledgePoints: [],
      exams: [],
      repos: [],
      videos: [],
      dialogVisible: false,
      dialogTitle: '发布任务',
      form: this.emptyForm(),
      recordDialogVisible: false,
      records: [],
      tableHeaderStyle: {
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px'
      }
    }
  },
  created() {
    this.loadTasks()
    this.loadOptions()
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        title: '',
        description: '',
        taskType: 'EXAM',
        courseId: '',
        chapterId: '',
        knowledgePointId: '',
        targetType: 'CLASS',
        targetClassId: '',
        targetStudentId: '',
        relatedExamId: '',
        relatedPaperId: '',
        relatedVideoId: '',
        deadline: '',
        status: 1
      }
    },
    async loadOptions() {
      const [classRes, courseRes, examRes, repoRes] = await Promise.all([
        fetchClasses(),
        fetchCoursePage({ pageNum: 1, pageSize: 1000 }),
        examPaging({ pageNum: 1, pageSize: 1000 }),
        repoPaging({ pageNum: 1, pageSize: 1000 })
      ])
      this.classes = classRes.data || []
      this.courses = (courseRes.data && courseRes.data.records) || []
      this.exams = (examRes.data && examRes.data.records) || []
      this.repos = (repoRes.data && repoRes.data.records) || []
      this.loadStudents()
    },
    async loadStudents(query = '') {
      const res = await userPaging({ pageNum: 1, pageSize: 1000, realName: query || null, roleId: 1 })
      this.students = (res.data && res.data.records) || []
    },
    async loadTasks() {
      this.loading = true
      try {
        const res = await fetchLearningTaskPage({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          title: this.query.title || null,
          taskType: this.query.taskType || null,
          status: this.query.status === '' ? null : this.query.status
        })
        this.data = res.data
      } finally {
        this.loading = false
      }
    },
    searchTasks() {
      this.pageNum = 1
      this.loadTasks()
    },
    resetQuery() {
      this.query = { title: '', taskType: '', status: '' }
      this.searchTasks()
    },
    openCreateDialog() {
      this.dialogTitle = '发布任务'
      this.form = this.emptyForm()
      this.chapters = []
      this.knowledgePoints = []
      this.videos = []
      this.dialogVisible = true
    },
    async openEditDialog(row) {
      this.dialogTitle = '编辑任务'
      this.form = {
        id: row.id,
        title: row.title,
        description: row.description,
        taskType: row.taskType,
        courseId: row.courseId || '',
        chapterId: row.chapterId || '',
        knowledgePointId: row.knowledgePointId || '',
        targetType: row.targetType,
        targetClassId: row.targetClassId || '',
        targetStudentId: row.targetStudentId || '',
        relatedExamId: row.relatedExamId || '',
        relatedPaperId: row.relatedPaperId || '',
        relatedVideoId: row.relatedVideoId || '',
        deadline: row.deadline || '',
        status: row.status
      }
      await this.handleCourseChange(false)
      if (this.form.chapterId) {
        await this.handleChapterChange(false)
      }
      this.dialogVisible = true
    },
    handleTargetTypeChange() {
      this.form.targetClassId = ''
      this.form.targetStudentId = ''
    },
    async handleCourseChange(reset = true) {
      if (reset) {
        this.form.chapterId = ''
        this.form.knowledgePointId = ''
        this.form.relatedVideoId = ''
      }
      if (!this.form.courseId) {
        this.chapters = []
        this.videos = []
        return
      }
      const [chapterRes, videoRes] = await Promise.all([
        fetchCourseChapters(this.form.courseId),
        fetchCourseVideos({ courseId: this.form.courseId })
      ])
      this.chapters = chapterRes.data || []
      this.videos = videoRes.data || []
    },
    async handleChapterChange(reset = true) {
      if (reset) {
        this.form.knowledgePointId = ''
      }
      if (!this.form.chapterId) {
        this.knowledgePoints = []
        return
      }
      const res = await fetchKnowledgePoints(this.form.chapterId)
      this.knowledgePoints = res.data || []
    },
    submitTask() {
      if (!this.form.title || !this.form.taskType || !this.form.targetType) {
        this.$message.warning('请填写任务标题、类型和目标')
        return
      }
      if (this.form.targetType === 'CLASS' && !this.form.targetClassId) {
        this.$message.warning('请选择目标班级')
        return
      }
      if (this.form.targetType === 'STUDENT' && !this.form.targetStudentId) {
        this.$message.warning('请选择目标学生')
        return
      }
      const data = this.cleanPayload(this.form)
      const request = this.form.id ? updateLearningTask(this.form.id, data) : createLearningTask(data)
      request.then(() => {
        this.$message.success(this.form.id ? '任务已更新' : '任务已发布')
        this.dialogVisible = false
        this.loadTasks()
      })
    },
    cleanPayload(form) {
      const data = { ...form }
      delete data.id
      Object.keys(data).forEach(key => {
        if (data[key] === '') {
          data[key] = null
        }
      })
      return data
    },
    disableTask(row) {
      this.$confirm('确认禁用该学习任务？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        disableLearningTask(row.id).then(() => {
          this.$message.success('任务已禁用')
          this.loadTasks()
        })
      })
    },
    async openRecordDialog(row) {
      const res = await fetchLearningTaskRecords(row.id)
      this.records = res.data || []
      this.recordDialogVisible = true
    },
    relatedText(row) {
      return row.relatedExamTitle || row.relatedPaperTitle || row.relatedVideoTitle || row.knowledgePointName || row.chapterTitle || row.courseName || '-'
    },
    videoOptionLabel(video) {
      return `${video.chapterTitle || '未分章节'} / ${video.title}`
    },
    formatRate(rate) {
      const value = Number(rate || 0)
      if (value < 0) return 0
      if (value > 100) return 100
      return Math.round(value)
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadTasks()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadTasks()
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style>
