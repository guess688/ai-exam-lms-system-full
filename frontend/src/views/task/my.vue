<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="toolbar">
      <el-form-item label="任务状态">
        <el-select v-model="query.recordStatus" clearable placeholder="全部状态" @change="loadTasks">
          <el-option label="待完成" value="TODO" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型">
        <el-select v-model="query.taskType" clearable placeholder="全部类型" @change="loadTasks">
          <el-option v-for="item in taskTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-refresh" @click="loadTasks">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="tasks"
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
      <el-table-column label="关联内容" min-width="180">
        <template slot-scope="{ row }">{{ relatedText(row) }}</template>
      </el-table-column>
      <el-table-column label="进度" align="center" width="180">
        <template slot-scope="{ row }">
          <el-progress :percentage="formatRate(row.progressRate)" :stroke-width="10" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="{ row }">
          <el-tag :type="statusTagType(row.recordStatus)">
            {{ statusName(row.recordStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" align="center" width="170" />
      <el-table-column prop="finishTime" label="完成时间" align="center" width="170" />
      <el-table-column label="操作" align="center" fixed="right" width="140">
        <template slot-scope="{ row }">
          <el-button v-if="row.taskType === 'REVIEW' && row.recordStatus !== 'COMPLETED'" type="text" size="small" @click="confirmReview(row)">
            确认完成
          </el-button>
          <el-button v-else type="text" size="small" @click="goTask(row)">
            去完成
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && tasks.length === 0" class="empty-block">暂无学习任务</div>
  </div>
</template>

<script>
import { confirmReviewTask, fetchMyLearningTasks } from '@/api/learningTask'

export default {
  data() {
    return {
      loading: false,
      query: {
        recordStatus: 'TODO',
        taskType: ''
      },
      taskTypes: [
        { label: '考试任务', value: 'EXAM' },
        { label: '练习任务', value: 'PRACTICE' },
        { label: '网课任务', value: 'VIDEO' },
        { label: '错题订正任务', value: 'WRONG_QUESTION' },
        { label: '复习任务', value: 'REVIEW' }
      ],
      tasks: [],
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
  },
  methods: {
    async loadTasks() {
      this.loading = true
      try {
        const res = await fetchMyLearningTasks({
          recordStatus: this.query.recordStatus || null,
          taskType: this.query.taskType || null
        })
        this.tasks = res.data || []
      } finally {
        this.loading = false
      }
    },
    goTask(row) {
      if (row.taskType === 'EXAM' && row.relatedExamId) {
        localStorage.setItem('examInfo_examId', row.relatedExamId)
        this.$router.push({ name: 'prepare-exam' })
        return
      }
      if (row.taskType === 'PRACTICE' && row.relatedPaperId) {
        this.$router.push({ name: 'start-exercise', query: { repoId: row.relatedPaperId, repoTitle: row.relatedPaperTitle || row.title }})
        return
      }
      if (row.taskType === 'VIDEO' && row.courseId) {
        this.$router.push({ path: '/course-detail', query: { id: row.courseId, videoId: row.relatedVideoId }})
        return
      }
      if (row.taskType === 'WRONG_QUESTION') {
        this.$router.push({ name: 'wrong-book' })
        return
      }
      this.$message.info('该任务暂无可跳转资源')
    },
    confirmReview(row) {
      confirmReviewTask(row.id).then(() => {
        this.$message.success('复习任务已完成')
        this.loadTasks()
      })
    },
    relatedText(row) {
      return row.relatedExamTitle || row.relatedPaperTitle || row.relatedVideoTitle || row.knowledgePointName || row.chapterTitle || row.courseName || '-'
    },
    statusName(status) {
      if (status === 'COMPLETED') return '已完成'
      if (status === 'IN_PROGRESS') return '进行中'
      return '待完成'
    },
    statusTagType(status) {
      if (status === 'COMPLETED') return 'success'
      if (status === 'IN_PROGRESS') return 'warning'
      return 'info'
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
  padding: 42px 0;
  text-align: center;
}
</style>
