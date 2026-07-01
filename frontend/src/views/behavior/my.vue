<template>
  <div class="app-container">
    <div class="page-head">
      <div>
        <h2>我的学习行为评价</h2>
        <p>结合近期考试、网课观看和学习任务完成情况生成</p>
      </div>
      <el-button type="primary" icon="el-icon-refresh" :loading="loading" @click="loadEvaluation">刷新评价</el-button>
    </div>

    <div v-if="!loading && !evaluation" class="empty-block">暂无学习行为评价数据</div>

    <template v-if="evaluation">
      <el-alert
        :title="evaluation.attentionLevel || '状态良好'"
        :description="evaluation.aiSummary || ''"
        :type="alertType(evaluation.attentionLevel)"
        :closable="false"
        show-icon
        class="summary-alert"
      />

      <el-row :gutter="12" class="metric-row">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric">
            <div class="metric-label">成绩趋势</div>
            <div class="metric-value">{{ evaluation.scoreTrendStatus }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric">
            <div class="metric-label">重复错题率</div>
            <div class="metric-value">{{ formatRateText(evaluation.wrongRepeatRate) }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric">
            <div class="metric-label">网课完成率</div>
            <el-progress :percentage="formatRate(evaluation.videoCompletionRate)" :stroke-width="12" />
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric">
            <div class="metric-label">任务完成率</div>
            <el-progress :percentage="formatRate(evaluation.taskCompletionRate)" :stroke-width="12" />
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="12" class="metric-row">
        <el-col :xs="24" :sm="12">
          <div class="metric">
            <div class="metric-label">章节掌握变化</div>
            <div class="metric-value small">{{ changeText(evaluation.chapterMasteryChange) }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12">
          <div class="metric">
            <div class="metric-label">知识点掌握变化</div>
            <div class="metric-value small">{{ changeText(evaluation.knowledgeMasteryChange) }}</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <div class="panel">
            <div class="section-title">我的学习建议</div>
            <ul class="suggestions">
              <li v-for="(item, index) in evaluation.suggestions || []" :key="index">{{ item }}</li>
            </ul>
            <div v-if="!(evaluation.suggestions || []).length" class="empty-inner">暂无建议</div>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="panel">
            <div class="section-title">行为信号</div>
            <div v-for="(signal, index) in evaluation.ruleSignals || []" :key="index" class="signal">
              <el-tag size="mini" :type="signalTagType(signal.level)">{{ signalName(signal.level) }}</el-tag>
              <span>{{ signal.message }}</span>
            </div>
            <div v-if="!(evaluation.ruleSignals || []).length" class="empty-inner">暂无行为信号</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <div class="panel">
            <div class="section-title">近次成绩趋势</div>
            <el-table :data="evaluation.scoreTrend || []" border size="small" empty-text="暂无成绩数据">
              <el-table-column prop="examName" label="考试" min-width="160" show-overflow-tooltip />
              <el-table-column label="得分" align="center" width="90">
                <template slot-scope="{ row }">{{ row.score }}/{{ row.grossScore }}</template>
              </el-table-column>
              <el-table-column label="得分率" align="center" width="90">
                <template slot-scope="{ row }">{{ formatRateText(row.rate) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="panel">
            <div class="section-title">章节掌握情况</div>
            <el-table :data="evaluation.latestChapterMastery || []" border size="small" empty-text="暂无章节数据">
              <el-table-column prop="name" label="章节" min-width="150" show-overflow-tooltip />
              <el-table-column label="正确率" align="center" width="90">
                <template slot-scope="{ row }">{{ formatRateText(row.rate) }}</template>
              </el-table-column>
              <el-table-column prop="level" label="状态" align="center" width="100" />
            </el-table>
          </div>
        </el-col>
      </el-row>

      <div class="panel">
        <div class="section-title">知识点掌握情况</div>
        <el-table :data="evaluation.latestKnowledgeMastery || []" border size="small" empty-text="暂无知识点数据">
          <el-table-column prop="name" label="知识点" min-width="180" show-overflow-tooltip />
          <el-table-column prop="total" label="总题数" align="center" width="90" />
          <el-table-column prop="correct" label="答对" align="center" width="90" />
          <el-table-column label="正确率" align="center" width="100">
            <template slot-scope="{ row }">{{ formatRateText(row.rate) }}</template>
          </el-table-column>
          <el-table-column prop="level" label="状态" align="center" width="120" />
        </el-table>
      </div>
    </template>
  </div>
</template>

<script>
import { fetchMyLearningBehavior } from '@/api/learningBehavior'

export default {
  data() {
    return {
      loading: false,
      evaluation: null
    }
  },
  created() {
    this.loadEvaluation()
  },
  methods: {
    async loadEvaluation() {
      this.loading = true
      try {
        const res = await fetchMyLearningBehavior()
        this.evaluation = res.data
      } finally {
        this.loading = false
      }
    },
    alertType(value) {
      if (value === '需要关注') return 'warning'
      if (value === '建议跟进') return 'info'
      return 'success'
    },
    signalTagType(value) {
      if (value === 'ATTENTION') return 'warning'
      if (value === 'IMPROVE') return 'info'
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
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-head h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.page-head p {
  margin: 6px 0 0;
  color: #909399;
}

.summary-alert {
  margin-bottom: 16px;
}

.metric-row {
  margin-bottom: 16px;
}

.metric,
.panel {
  padding: 14px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.metric {
  min-height: 84px;
}

.metric-label {
  margin-bottom: 8px;
  color: #909399;
  font-size: 12px;
}

.metric-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.metric-value.small {
  font-size: 15px;
}

.panel {
  margin-bottom: 16px;
}

.section-title {
  margin-bottom: 10px;
  font-weight: 600;
  color: #303133;
}

.suggestions {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  line-height: 1.8;
}

.signal {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.6;
}

.empty-block,
.empty-inner {
  color: #909399;
  text-align: center;
}

.empty-block {
  padding: 48px 0;
}

.empty-inner {
  padding: 20px 0;
}
</style>
