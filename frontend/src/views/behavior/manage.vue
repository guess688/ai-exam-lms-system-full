<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="toolbar">
      <el-form-item label="班级">
        <el-select v-model="query.gradeId" clearable filterable placeholder="全部班级">
          <el-option v-for="item in classes" :key="item.id" :label="item.gradeName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="学生">
        <el-input v-model="query.realName" clearable placeholder="输入学生姓名" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      :title="`当前页需要重点关注学生 ${focusCount} 人`"
      type="warning"
      :closable="false"
      show-icon
      class="focus-alert"
    />

    <el-table
      v-loading="loading"
      :data="data.records || []"
      border
      fit
      highlight-current-row
      :header-cell-style="tableHeaderStyle"
    >
      <el-table-column label="序号" align="center" width="70">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="studentName" label="学生" min-width="120" />
      <el-table-column prop="gradeName" label="班级" min-width="120" />
      <el-table-column label="关注状态" align="center" width="120">
        <template slot-scope="{ row }">
          <el-tag :type="attentionTagType(row.attentionLevel)">{{ row.attentionLevel || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="recentExamCount" label="近考次数" align="center" width="90" />
      <el-table-column prop="scoreTrendStatus" label="成绩趋势" align="center" width="110" />
      <el-table-column label="重复错题率" align="center" width="130">
        <template slot-scope="{ row }">{{ formatRateText(row.wrongRepeatRate) }}</template>
      </el-table-column>
      <el-table-column label="网课完成率" align="center" width="150">
        <template slot-scope="{ row }">
          <el-progress :percentage="formatRate(row.videoCompletionRate)" :stroke-width="10" />
        </template>
      </el-table-column>
      <el-table-column label="任务完成率" align="center" width="150">
        <template slot-scope="{ row }">
          <el-progress :percentage="formatRate(row.taskCompletionRate)" :stroke-width="10" />
        </template>
      </el-table-column>
      <el-table-column label="按时完成率" align="center" width="130">
        <template slot-scope="{ row }">{{ formatRateText(row.onTimeCompletionRate) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="110">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openDetail(row)">行为分析</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        :current-page="data.current"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="data.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="data.total || 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog title="学生学习行为分析" :visible.sync="detailVisible" width="920px" append-to-body>
      <div v-if="detail" class="detail-wrap">
        <div class="detail-header">
          <div>
            <div class="detail-title">{{ detail.studentName }}</div>
            <div class="detail-meta">{{ detail.gradeName || '未分配班级' }} · {{ detail.generatedAt || '' }}</div>
          </div>
          <el-tag :type="attentionTagType(detail.attentionLevel)" size="medium">{{ detail.attentionLevel }}</el-tag>
        </div>

        <el-row :gutter="12" class="metric-row">
          <el-col :span="6">
            <div class="metric">
              <div class="metric-label">成绩趋势</div>
              <div class="metric-value">{{ detail.scoreTrendStatus }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric">
              <div class="metric-label">重复错题率</div>
              <div class="metric-value">{{ formatRateText(detail.wrongRepeatRate) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric">
              <div class="metric-label">网课完成率</div>
              <div class="metric-value">{{ formatRateText(detail.videoCompletionRate) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric">
              <div class="metric-label">任务完成率</div>
              <div class="metric-value">{{ formatRateText(detail.taskCompletionRate) }}</div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="12" class="metric-row">
          <el-col :span="12">
            <div class="metric">
              <div class="metric-label">章节掌握变化</div>
              <div class="metric-value small">{{ changeText(detail.chapterMasteryChange) }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="metric">
              <div class="metric-label">知识点掌握变化</div>
              <div class="metric-value small">{{ changeText(detail.knowledgeMasteryChange) }}</div>
            </div>
          </el-col>
        </el-row>

        <div class="section-title">AI 总结</div>
        <div class="summary">{{ detail.aiSummary || '暂无总结' }}</div>

        <div class="section-title">规则信号</div>
        <el-table :data="detail.ruleSignals || []" border size="small">
          <el-table-column label="类型" prop="type" width="130" />
          <el-table-column label="等级" width="110" align="center">
            <template slot-scope="{ row }">
              <el-tag size="mini" :type="signalTagType(row.level)">{{ signalName(row.level) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="观察" prop="message" min-width="260" />
          <el-table-column label="建议" prop="suggestion" min-width="260" />
        </el-table>

        <div class="section-title">学习建议</div>
        <ul class="suggestions">
          <li v-for="(item, index) in detail.suggestions || []" :key="index">{{ item }}</li>
        </ul>

        <el-row :gutter="12">
          <el-col :span="12">
            <div class="section-title">近次成绩</div>
            <el-table :data="detail.scoreTrend || []" border size="small" empty-text="暂无成绩数据">
              <el-table-column prop="examName" label="考试" min-width="160" show-overflow-tooltip />
              <el-table-column label="得分" align="center" width="90">
                <template slot-scope="{ row }">{{ row.score }}/{{ row.grossScore }}</template>
              </el-table-column>
              <el-table-column label="得分率" align="center" width="90">
                <template slot-scope="{ row }">{{ formatRateText(row.rate) }}</template>
              </el-table-column>
            </el-table>
          </el-col>
          <el-col :span="12">
            <div class="section-title">章节掌握</div>
            <el-table :data="detail.latestChapterMastery || []" border size="small" empty-text="暂无章节数据">
              <el-table-column prop="name" label="章节" min-width="150" show-overflow-tooltip />
              <el-table-column label="正确率" align="center" width="90">
                <template slot-scope="{ row }">{{ formatRateText(row.rate) }}</template>
              </el-table-column>
              <el-table-column prop="level" label="掌握状态" align="center" width="100" />
            </el-table>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchClasses } from '@/api/class_'
import {
  fetchStudentLearningBehavior,
  fetchStudentLearningBehaviors
} from '@/api/learningBehavior'

export default {
  data() {
    return {
      loading: false,
      detailLoading: false,
      detailVisible: false,
      detail: null,
      classes: [],
      query: {
        gradeId: '',
        realName: ''
      },
      pageNum: 1,
      pageSize: 10,
      data: {
        records: [],
        total: 0,
        current: 1,
        size: 10
      },
      tableHeaderStyle: {
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px'
      }
    }
  },
  computed: {
    focusCount() {
      return (this.data.records || []).filter(item => item.needAttention).length
    }
  },
  created() {
    this.loadClasses()
    this.loadList()
  },
  methods: {
    async loadClasses() {
      const res = await fetchClasses()
      this.classes = res.data || []
    },
    async loadList() {
      this.loading = true
      try {
        const res = await fetchStudentLearningBehaviors({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          gradeId: this.query.gradeId || null,
          realName: this.query.realName || null
        })
        this.data = res.data || this.data
      } finally {
        this.loading = false
      }
    },
    search() {
      this.pageNum = 1
      this.loadList()
    },
    resetQuery() {
      this.query = { gradeId: '', realName: '' }
      this.search()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadList()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadList()
    },
    async openDetail(row) {
      this.detailVisible = true
      this.detail = null
      this.detailLoading = true
      try {
        const res = await fetchStudentLearningBehavior(row.studentId)
        this.detail = res.data
      } finally {
        this.detailLoading = false
      }
    },
    attentionTagType(value) {
      if (value === '需要关注') return 'danger'
      if (value === '建议跟进') return 'warning'
      return 'success'
    },
    signalTagType(value) {
      if (value === 'ATTENTION') return 'danger'
      if (value === 'IMPROVE') return 'warning'
      if (value === 'GOOD') return 'success'
      return 'info'
    },
    signalName(value) {
      if (value === 'ATTENTION') return '需要关注'
      if (value === 'IMPROVE') return '建议改进'
      if (value === 'GOOD') return '积极信号'
      return '信息'
    },
    formatRate(value) {
      const rate = Number(value || 0)
      if (rate < 0) return 0
      if (rate > 100) return 100
      return Math.round(rate)
    },
    formatRateText(value) {
      return `${this.formatRate(value)}%`
    },
    changeText(change) {
      if (!change) return '暂无对比'
      const diff = change.changeRate === null || change.changeRate === undefined ? '' : ` (${change.changeRate > 0 ? '+' : ''}${change.changeRate}%)`
      return `${change.status || '暂无对比'}${diff}`
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.focus-alert {
  margin-bottom: 12px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.detail-meta {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.metric-row {
  margin-bottom: 14px;
}

.metric {
  padding: 12px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.metric-label {
  color: #909399;
  font-size: 12px;
}

.metric-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.metric-value.small {
  font-size: 15px;
}

.section-title {
  margin: 16px 0 8px;
  font-weight: 600;
  color: #303133;
}

.summary {
  padding: 12px;
  line-height: 1.7;
  color: #606266;
  background: #f8fafc;
  border: 1px solid #ebeef5;
}

.suggestions {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  line-height: 1.8;
}
</style>
