<template>
  <div class="app-container">
    <el-form :inline="true" class="toolbar">
      <el-form-item label="真实姓名">
        <el-input v-model="realName" placeholder="真实姓名" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">查询</el-button>
        <el-button type="primary" @click="getExportScores">导出</el-button>
      </el-form-item>
    </el-form>

    <div class="ai-report-actions">
      <div>
        <div class="ai-report-title">AI 班级学情报告</div>
        <div class="ai-report-meta">
          基于成绩分布、章节知识点掌握、高频错题、难度表现、网课完成率和学习任务完成率生成
        </div>
      </div>
      <div class="ai-report-buttons">
        <el-button
          type="primary"
          icon="el-icon-cpu"
          :loading="aiReportLoading"
          @click="handleGenerateClassAiReport"
        >
          生成 AI 班级学情报告
        </el-button>
        <el-button icon="el-icon-document" @click="openClassAiReportDialog">
          查看报告
        </el-button>
        <el-button type="success" icon="el-icon-tickets" :loading="practiceRecommendLoading" @click="openPracticeRecommendDialog">
          生成推荐练习
        </el-button>
      </div>
    </div>

    <el-dialog
      title="AI 班级学情报告"
      :visible.sync="aiReportDialogVisible"
      width="780px"
      append-to-body
    >
      <div v-if="!aiReports.length" class="ai-report-empty">暂无 AI 班级学情报告</div>
      <el-collapse v-else v-model="activeReportId" accordion>
        <el-collapse-item
          v-for="report in aiReports"
          :key="report.id"
          :name="String(report.id)"
        >
          <template slot="title">
            <span class="ai-report-item-title">{{ report.title || 'AI 班级学情报告' }}</span>
            <span class="ai-report-item-time">{{ report.createTime }}</span>
          </template>
          <div class="ai-report-summary">{{ report.outputText || '报告已生成' }}</div>
          <pre class="ai-report-json">{{ stringifyReport(report) }}</pre>
          <div class="ai-report-dialog-actions">
            <el-button
              size="mini"
              type="primary"
              plain
              :loading="aiReportLoading"
              @click.stop="handleRegenerateClassAiReport(report.id)"
            >
              重新生成报告
            </el-button>
            <el-button size="mini" plain @click.stop="copyReportContent(report)">
              复制报告内容
            </el-button>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-dialog>

    <el-dialog
      title="AI 推荐练习任务"
      :visible.sync="practiceDialogVisible"
      width="960px"
      append-to-body
    >
      <el-form :model="practiceForm" label-width="110px" class="practice-form">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="推荐对象">
              <el-radio-group v-model="practiceForm.targetType">
                <el-radio-button label="CLASS">班级</el-radio-button>
                <el-radio-button label="STUDENT">学生</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="practiceForm.targetType === 'STUDENT'">
            <el-form-item label="学生">
              <el-select v-model="practiceForm.studentId" filterable placeholder="选择学生" style="width: 100%">
                <el-option
                  v-for="item in studentOptions"
                  :key="item.userId"
                  :label="item.realName || item.userName || item.userId"
                  :value="item.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推荐题量">
              <el-input-number v-model="practiceForm.questionCount" :min="1" :max="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="10">
            <el-form-item label="任务标题">
              <el-input v-model="practicePublishForm.title" placeholder="生成后可编辑" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="practicePublishForm.deadline"
                type="datetime"
                value-format="yyyy-MM-dd'T'HH:mm:ss"
                placeholder="选择截止时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" :loading="practiceRecommendLoading" @click="handleGeneratePracticeRecommendation">
              生成推荐
            </el-button>
          </el-col>
        </el-row>
        <el-form-item label="任务说明">
          <el-input v-model="practicePublishForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <div v-if="practiceRecommendation" class="practice-summary">
        <div><strong>推荐难度：</strong>{{ practiceRecommendation.recommendedDifficulty || '-' }}</div>
        <div><strong>推荐理由：</strong>{{ practiceRecommendation.reason || '-' }}</div>
      </div>

      <el-table
        ref="practiceQuestionTable"
        :data="recommendedQuestions"
        border
        max-height="360"
        @selection-change="handlePracticeSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="题目" min-width="260" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row.content }}</template>
        </el-table-column>
        <el-table-column label="知识点" min-width="130">
          <template slot-scope="{ row }">{{ row.knowledgePointName || '-' }}</template>
        </el-table-column>
        <el-table-column label="难度" align="center" width="90">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="difficultyTagType(row.difficulty)">{{ difficultyLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer">
        <el-button @click="practiceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="practicePublishLoading" @click="handlePublishPracticeTask">
          一键发布为学习任务
        </el-button>
      </div>
    </el-dialog>

    <el-table
      :data="data.records || []"
      border
      fit
      highlight-current-row
      :header-cell-style="{
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px'
      }"
    >
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column fixed label="序号" align="center" width="80">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="title" label="试卷名称" align="center" />
      <el-table-column prop="realName" label="真实姓名" align="center" />
      <el-table-column prop="userScore" label="用户得分" align="center" />
      <el-table-column prop="count" label="切屏次数" align="center" />
      <el-table-column prop="userTime" label="用户用时" align="center" />
      <el-table-column prop="limitTime" label="提交时间" align="center" />
      <el-table-column fixed="right" label="操作" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" style="font-size: 14px" @click="updateRow(row)">查看详情</el-button>
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

    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-title">班级成绩分布</div>
        <div v-if="!classScoreData.length" class="empty-chart">暂无数据</div>
        <div ref="classScoreChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-title">分数段统计</div>
        <div v-if="!scoreSegmentData.length" class="empty-chart">暂无数据</div>
        <div ref="scoreSegmentChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-title">章节掌握正确率</div>
        <div v-if="!chapterAnalysis.length" class="empty-chart">暂无数据</div>
        <div ref="chapterChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-title">知识点正确率</div>
        <div v-if="!knowledgeAnalysis.length" class="empty-chart">暂无数据</div>
        <div ref="knowledgeChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-title">高频错题排名</div>
        <div v-if="!wrongQuestionRanking.length" class="empty-chart">暂无数据</div>
        <div ref="wrongQuestionChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-title">题目难度与正确率</div>
        <div v-if="!difficultyAnalysis.length" class="empty-chart">暂无数据</div>
        <div ref="difficultyChart" class="chart-box" />
      </div>
    </div>

    <el-tabs v-model="analysisTab" class="analysis-tabs">
      <el-tab-pane label="章节掌握分析" name="chapter">
        <el-table :data="chapterAnalysis" border empty-text="暂无章节掌握数据">
          <el-table-column prop="name" label="章节" min-width="160" />
          <el-table-column prop="total" label="总题数" align="center" width="100" />
          <el-table-column prop="correct" label="答对题数" align="center" width="100" />
          <el-table-column prop="rate" label="正确率" align="center" width="110">
            <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
          </el-table-column>
          <el-table-column prop="level" label="掌握等级" align="center" width="120" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="知识点掌握分析" name="knowledge">
        <el-table :data="knowledgeAnalysis" border empty-text="暂无知识点掌握数据">
          <el-table-column prop="name" label="知识点" min-width="160" />
          <el-table-column prop="total" label="总题数" align="center" width="100" />
          <el-table-column prop="correct" label="答对题数" align="center" width="100" />
          <el-table-column prop="rate" label="正确率" align="center" width="110">
            <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
          </el-table-column>
          <el-table-column prop="level" label="掌握等级" align="center" width="120" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="薄弱章节排名" name="weakChapter">
        <el-table :data="weakChapterRanking" border empty-text="暂无薄弱章节数据">
          <el-table-column type="index" label="排名" align="center" width="80" />
          <el-table-column prop="name" label="章节" min-width="160" />
          <el-table-column prop="rate" label="正确率" align="center" width="110">
            <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
          </el-table-column>
          <el-table-column prop="level" label="掌握等级" align="center" width="120" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="薄弱知识点排名" name="weakKnowledge">
        <el-table :data="weakKnowledgeRanking" border empty-text="暂无薄弱知识点数据">
          <el-table-column type="index" label="排名" align="center" width="80" />
          <el-table-column prop="name" label="知识点" min-width="160" />
          <el-table-column prop="rate" label="正确率" align="center" width="110">
            <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
          </el-table-column>
          <el-table-column prop="level" label="掌握等级" align="center" width="120" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="高频错题排名" name="wrong">
        <el-table :data="wrongQuestionRanking" border empty-text="暂无高频错题数据">
          <el-table-column type="index" label="排名" align="center" width="80" />
          <el-table-column prop="title" label="题目" min-width="220" show-overflow-tooltip />
          <el-table-column prop="chapterName" label="章节" min-width="120" />
          <el-table-column prop="knowledgePointName" label="知识点" min-width="120" />
          <el-table-column prop="wrong" label="错误次数" align="center" width="100" />
          <el-table-column prop="rate" label="错误率" align="center" width="110">
            <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="难度表现分析" name="difficulty">
        <el-table :data="difficultyAnalysis" border empty-text="暂无难度表现数据">
          <el-table-column prop="name" label="难度" align="center" width="120" />
          <el-table-column prop="total" label="总题数" align="center" width="100" />
          <el-table-column prop="correct" label="答对题数" align="center" width="100" />
          <el-table-column prop="rate" label="正确率" align="center" width="110">
            <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
          </el-table-column>
          <el-table-column prop="level" label="掌握等级" align="center" width="120" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import echarts from 'echarts'
import {
  scorePaging,
  exportScores,
  getClassChapterAnalysis,
  getClassKnowledgeAnalysis,
  getWeakChapterRanking,
  getWeakKnowledgeRanking,
  getWrongQuestionRanking,
  getDifficultyAnalysis,
  getClassScoreChart,
  getScoreSegmentChart
} from '@/api/score'
import {
  fetchClassAiReportHistory,
  generateClassAiReport,
  regenerateAiReport
} from '@/api/ai'
import {
  generatePracticeRecommendation,
  publishPracticeRecommendation
} from '@/api/aiPractice'

export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      gradeId: '',
      examId: '',
      realName: '',
      examTitle: '',
      gradeName: '',
      data: {},
      formInline: {},
      analysisTab: 'chapter',
      classScoreData: [],
      scoreSegmentData: [],
      chapterAnalysis: [],
      knowledgeAnalysis: [],
      weakChapterRanking: [],
      weakKnowledgeRanking: [],
      wrongQuestionRanking: [],
      difficultyAnalysis: [],
      chartInstances: {},
      aiReportLoading: false,
      aiReportDialogVisible: false,
      aiReports: [],
      activeReportId: null,
      practiceDialogVisible: false,
      practiceRecommendLoading: false,
      practicePublishLoading: false,
      practiceRecommendation: null,
      selectedPracticeQuestions: [],
      practiceForm: {
        targetType: 'CLASS',
        studentId: '',
        questionCount: 12
      },
      practicePublishForm: {
        title: '',
        description: '',
        deadline: ''
      }
    }
  },
  computed: {
    studentOptions() {
      return (this.data.records || []).filter(item => item.userId)
    },
    recommendedQuestions() {
      return (this.practiceRecommendation && this.practiceRecommendation.recommendedQuestions) || []
    }
  },
  created() {
    this.examId = localStorage.getItem('examId')
    this.gradeId = localStorage.getItem('gradeId')
    this.examTitle = localStorage.getItem('examTitle')
    this.gradeName = localStorage.getItem('gradeName')
    this.getScorePage()
    this.loadAnalysis()
    this.loadClassAiReports(true)
  },
  mounted() {
    this.initCharts()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    localStorage.removeItem('examId')
    localStorage.removeItem('gradeId')
    window.removeEventListener('resize', this.resizeCharts)
    this.destroyCharts()
  },
  methods: {
    updateRow(row) {
      row.type = 1
      localStorage.setItem('record_exam_examId', row.examId)
      localStorage.setItem('record_exam_userId', row.userId)
      this.$router.push({ name: 'exam-record-detail', query: { data: row }})
    },
    async getScorePage() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        examId: this.examId,
        gradeId: this.gradeId,
        realName: this.realName
      }
      const res = await scorePaging(params)
      this.data = res.data || {}
    },
    async loadAnalysis() {
      if (!this.examId || !this.gradeId) {
        return
      }
      const params = {
        examId: this.examId,
        gradeId: this.gradeId
      }
      const [
        classScoreRes,
        scoreSegmentRes,
        chapterRes,
        knowledgeRes,
        weakChapterRes,
        weakKnowledgeRes,
        wrongRes,
        difficultyRes
      ] = await Promise.all([
        getClassScoreChart(params),
        getScoreSegmentChart(params),
        getClassChapterAnalysis(params),
        getClassKnowledgeAnalysis(params),
        getWeakChapterRanking(params),
        getWeakKnowledgeRanking(params),
        getWrongQuestionRanking(params),
        getDifficultyAnalysis(params)
      ])
      this.classScoreData = classScoreRes.data || []
      this.scoreSegmentData = scoreSegmentRes.data || []
      this.chapterAnalysis = chapterRes.data || []
      this.knowledgeAnalysis = knowledgeRes.data || []
      this.weakChapterRanking = weakChapterRes.data || []
      this.weakKnowledgeRanking = weakKnowledgeRes.data || []
      this.wrongQuestionRanking = wrongRes.data || []
      this.difficultyAnalysis = difficultyRes.data || []
      this.$nextTick(this.updateCharts)
    },
    buildClassAiReportParams() {
      return {
        examId: this.examId,
        gradeId: this.gradeId
      }
    },
    async loadClassAiReports(silent = false) {
      if (!this.examId || !this.gradeId) {
        return
      }
      try {
        const res = await fetchClassAiReportHistory(this.buildClassAiReportParams())
        this.aiReports = res.data || []
        if (this.aiReports.length && !this.activeReportId) {
          this.activeReportId = String(this.aiReports[0].id)
        }
      } catch (error) {
        if (!silent) {
          this.$message.error('AI 班级学情报告加载失败')
        }
      }
    },
    async handleGenerateClassAiReport() {
      if (!this.examId || !this.gradeId) {
        this.$message.warning('缺少考试或班级信息，无法生成报告')
        return
      }
      this.aiReportLoading = true
      try {
        const res = await generateClassAiReport(this.buildClassAiReportParams())
        this.$message.success('AI 班级学情报告已生成')
        await this.loadClassAiReports(true)
        if (res.data) {
          this.activeReportId = String(res.data.id)
        }
        this.aiReportDialogVisible = true
      } catch (error) {
        this.$message.error('AI 班级学情报告生成失败')
      } finally {
        this.aiReportLoading = false
      }
    },
    async handleRegenerateClassAiReport(reportId) {
      if (!reportId) {
        return
      }
      this.aiReportLoading = true
      try {
        const res = await regenerateAiReport(reportId)
        this.$message.success('AI 班级学情报告已重新生成')
        await this.loadClassAiReports(true)
        if (res.data) {
          this.activeReportId = String(res.data.id)
        }
      } catch (error) {
        this.$message.error('AI 班级学情报告重新生成失败')
      } finally {
        this.aiReportLoading = false
      }
    },
    async openClassAiReportDialog() {
      await this.loadClassAiReports(false)
      this.aiReportDialogVisible = true
    },
    stringifyReport(report) {
      const source = report.outputJson || report.outputText || ''
      if (!source) {
        return '暂无报告内容'
      }
      try {
        const parsed = typeof source === 'string' ? JSON.parse(source) : source
        return JSON.stringify(parsed, null, 2)
      } catch (error) {
        return String(source)
      }
    },
    copyReportContent(report) {
      const text = this.stringifyReport(report)
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('报告内容已复制')
        }).catch(() => {
          this.copyTextFallback(text)
        })
        return
      }
      this.copyTextFallback(text)
    },
    copyTextFallback(text) {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.setAttribute('readonly', 'readonly')
      textarea.style.position = 'fixed'
      textarea.style.left = '-9999px'
      document.body.appendChild(textarea)
      textarea.select()
      const copied = document.execCommand('copy')
      document.body.removeChild(textarea)
      if (copied) {
        this.$message.success('报告内容已复制')
      } else {
        this.$message.error('复制失败，请手动选择报告内容')
      }
    },
    openPracticeRecommendDialog() {
      this.practiceDialogVisible = true
      this.practiceRecommendation = null
      this.selectedPracticeQuestions = []
      this.practicePublishForm = {
        title: `${this.examTitle || '考试'} 推荐练习`,
        description: '',
        deadline: ''
      }
    },
    async handleGeneratePracticeRecommendation() {
      if (!this.examId || !this.gradeId) {
        this.$message.warning('缺少考试或班级信息，无法生成推荐练习')
        return
      }
      if (this.practiceForm.targetType === 'STUDENT' && !this.practiceForm.studentId) {
        this.$message.warning('请选择学生')
        return
      }
      this.practiceRecommendLoading = true
      try {
        const res = await generatePracticeRecommendation({
          examId: this.examId,
          targetType: this.practiceForm.targetType,
          gradeId: this.gradeId,
          studentId: this.practiceForm.targetType === 'STUDENT' ? this.practiceForm.studentId : null,
          questionCount: this.practiceForm.questionCount
        })
        this.practiceRecommendation = res.data
        this.practicePublishForm.title = res.data.suggestedTaskTitle || this.practicePublishForm.title
        this.practicePublishForm.description = res.data.suggestedTaskDescription || res.data.reason || ''
        this.practicePublishForm.deadline = this.formatDeadline(res.data.suggestedDeadline)
        this.$nextTick(() => {
          if (this.$refs.practiceQuestionTable) {
            this.recommendedQuestions.forEach(row => this.$refs.practiceQuestionTable.toggleRowSelection(row, true))
          }
        })
      } finally {
        this.practiceRecommendLoading = false
      }
    },
    handlePracticeSelectionChange(rows) {
      this.selectedPracticeQuestions = rows
    },
    async handlePublishPracticeTask() {
      if (!this.practiceRecommendation) {
        this.$message.warning('请先生成推荐练习')
        return
      }
      if (!this.selectedPracticeQuestions.length) {
        this.$message.warning('请至少选择一道推荐题目')
        return
      }
      if (!this.practicePublishForm.title) {
        this.$message.warning('请输入任务标题')
        return
      }
      const first = this.selectedPracticeQuestions[0] || {}
      this.practicePublishLoading = true
      try {
        await publishPracticeRecommendation({
          examId: this.examId,
          targetType: this.practiceRecommendation.targetType,
          gradeId: this.practiceRecommendation.gradeId,
          studentId: this.practiceRecommendation.studentId,
          courseId: first.courseId || null,
          chapterId: first.chapterId || null,
          knowledgePointId: first.knowledgePointId || null,
          title: this.practicePublishForm.title,
          description: this.practicePublishForm.description,
          deadline: this.practicePublishForm.deadline || null,
          questionIds: this.selectedPracticeQuestions.map(item => item.id)
        })
        this.$message.success('推荐练习任务已发布')
        this.practiceDialogVisible = false
      } finally {
        this.practicePublishLoading = false
      }
    },
    formatDeadline(value) {
      if (!value) {
        return ''
      }
      if (typeof value === 'string') {
        return value.length > 19 ? value.slice(0, 19) : value
      }
      return value
    },
    difficultyLabel(value) {
      if (value === 'EASY') return '简单'
      if (value === 'HARD') return '困难'
      return '中等'
    },
    difficultyTagType(value) {
      if (value === 'EASY') return 'success'
      if (value === 'HARD') return 'danger'
      return 'warning'
    },
    initCharts() {
      this.$nextTick(() => {
        [
          'classScoreChart',
          'scoreSegmentChart',
          'chapterChart',
          'knowledgeChart',
          'wrongQuestionChart',
          'difficultyChart'
        ].forEach((ref) => {
          if (this.$refs[ref] && !this.chartInstances[ref]) {
            this.chartInstances[ref] = echarts.init(this.$refs[ref])
          }
        })
        this.updateCharts()
      })
    },
    destroyCharts() {
      Object.keys(this.chartInstances).forEach((key) => {
        if (this.chartInstances[key]) {
          this.chartInstances[key].dispose()
        }
      })
      this.chartInstances = {}
    },
    resizeCharts() {
      Object.keys(this.chartInstances).forEach((key) => {
        if (this.chartInstances[key]) {
          this.chartInstances[key].resize()
        }
      })
    },
    updateCharts() {
      this.setBarChart('classScoreChart', this.classScoreData, 'value', '得分', '#409EFF')
      this.setBarChart('scoreSegmentChart', this.scoreSegmentData, 'value', '人数', '#67C23A')
      this.setBarChart('chapterChart', this.chapterAnalysis, 'rate', '正确率(%)', '#E6A23C')
      this.setBarChart('knowledgeChart', this.knowledgeAnalysis, 'rate', '正确率(%)', '#36A3F7')
      this.setBarChart('wrongQuestionChart', this.wrongQuestionRanking, 'wrong', '错误次数', '#F56C6C', 'title')
      this.setBarChart('difficultyChart', this.difficultyAnalysis, 'rate', '正确率(%)', '#8E71C7')
    },
    setBarChart(ref, rows, valueKey, yName, color, nameKey = 'name') {
      const chart = this.chartInstances[ref]
      if (!chart) {
        return
      }
      const names = (rows || []).map((item) => this.shortLabel(item[nameKey] || item.name || '未命名'))
      const values = (rows || []).map((item) => Number(item[valueKey] || 0))
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 48, right: 20, top: 24, bottom: names.length > 6 ? 72 : 42 },
        xAxis: {
          type: 'category',
          data: names,
          axisLabel: {
            interval: 0,
            rotate: names.length > 6 ? 30 : 0
          }
        },
        yAxis: { type: 'value', name: yName },
        series: [{
          type: 'bar',
          data: values,
          barMaxWidth: 42,
          itemStyle: { color }
        }]
      }, true)
    },
    shortLabel(value) {
      const text = String(value || '')
      return text.length > 12 ? `${text.slice(0, 12)}...` : text
    },
    formatRate(rate) {
      if (rate === null || rate === undefined) {
        return '0%'
      }
      return `${Number(rate).toFixed(2)}%`
    },
    getExportScores() {
      exportScores(this.examId, this.gradeId).then(res => {
        if (res) {
          const elink = document.createElement('a')
          let filename = 'downloaded-file.xlsx'
          if (this.gradeName) {
            filename = this.gradeName + '-' + this.examTitle + '.xlsx'
          }
          elink.download = filename
          elink.style.display = 'none'
          elink.href = URL.createObjectURL(res)
          document.body.appendChild(elink)
          elink.click()
          document.body.removeChild(elink)
        } else {
          this.$message.error('导出异常，请联系管理员')
        }
      })
    },
    onSubmit() {
      this.getScorePage()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getScorePage()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getScorePage()
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.ai-report-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.ai-report-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.ai-report-meta {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.ai-report-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.ai-report-empty {
  padding: 32px 0;
  text-align: center;
  color: #909399;
}

.ai-report-item-title {
  font-weight: 600;
  color: #303133;
}

.ai-report-item-time {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.ai-report-summary {
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.6;
}

.ai-report-json {
  max-height: 360px;
  overflow: auto;
  padding: 12px;
  border: 1px solid #ebeef5;
  background: #f8fafc;
  color: #303133;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-report-dialog-actions {
  margin-top: 12px;
  text-align: right;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.chart-card {
  position: relative;
  min-height: 300px;
  padding: 14px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
  height: 250px;
}

.empty-chart {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  text-align: center;
  color: #909399;
  z-index: 1;
}

.analysis-tabs {
  margin-top: 24px;
}

@media screen and (max-width: 900px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
