<template>
  <div class="app-container">
    <el-card class="generate-panel">
      <div slot="header" class="panel-header">
        <span>AI 自动出题</span>
        <el-button type="primary" :loading="generating" @click="handleGenerate">生成题目</el-button>
      </div>
      <el-form ref="generateForm" :model="generateForm" :rules="generateRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="generateForm.courseId" filterable clearable placeholder="选择课程" @change="handleGenerateCourseChange">
                <el-option v-for="item in courseOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="章节" prop="chapterId">
              <el-select v-model="generateForm.chapterId" filterable clearable :disabled="!generateForm.courseId" placeholder="选择章节" @change="handleGenerateChapterChange">
                <el-option v-for="item in generateChapterOptions" :key="item.id" :label="item.title" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="知识点" prop="knowledgePointId">
              <el-select v-model="generateForm.knowledgePointId" filterable clearable :disabled="!generateForm.chapterId" placeholder="选择知识点">
                <el-option v-for="item in generateKnowledgeOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="题型" prop="questionType">
              <el-select v-model="generateForm.questionType" placeholder="选择题型">
                <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="generateForm.difficulty" placeholder="选择难度">
                <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="题目数量" prop="count">
              <el-input-number v-model="generateForm.count" :min="1" :max="20" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="选项数量">
              <el-input-number v-model="generateForm.optionCount" :min="2" :max="8" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="额外要求">
          <el-input v-model="generateForm.extraRequirement" type="textarea" :rows="3" placeholder="例如：贴近课堂例题，避免纯记忆题" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="review-panel">
      <div slot="header" class="panel-header">
        <span>AI 待审核题目</span>
        <div class="review-tools">
          <repo-select v-model="selectedRepoId" style="width: 220px" />
          <el-select v-model="statusFilter" style="width: 150px" @change="fetchReviewPage">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已入库" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="全部" value="" />
          </el-select>
          <el-button @click="fetchReviewPage">刷新</el-button>
        </div>
      </div>

      <el-table :data="reviewPage.records || []" border>
        <el-table-column type="index" label="序号" align="center" width="70" />
        <el-table-column label="题干" min-width="240" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ parsedContent(row).content || '-' }}</template>
        </el-table-column>
        <el-table-column label="题型" align="center" width="120">
          <template slot-scope="{ row }">{{ questionTypeLabel(row.questionType) }}</template>
        </el-table-column>
        <el-table-column label="难度" align="center" width="100">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="difficultyTagType(row.difficulty)">{{ difficultyLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="{ row }">
            <el-tag size="mini" :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" align="center" width="160" />
        <el-table-column label="操作" align="center" width="210">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" :disabled="row.status !== 'PENDING'" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="text" size="small" :disabled="row.status !== 'PENDING'" @click="handleApprove(row)">确认入库</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" :disabled="row.status !== 'PENDING'" @click="handleReject(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 40]"
          :total="reviewPage.total || 0"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog title="编辑 AI 题目" :visible.sync="editDialogVisible" width="900px" append-to-body>
      <el-form :model="editForm" label-width="110px">
        <el-form-item label="题干">
          <el-input v-model="editForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="题型">
              <el-select v-model="editForm.questionType" style="width: 100%" @change="handleEditTypeChange">
                <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度">
              <el-select v-model="editForm.difficulty" style="width: 100%">
                <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="选项">
          <el-button type="primary" size="mini" plain @click="addEditOption">添加选项</el-button>
          <el-table :data="editForm.options" border class="option-table">
            <el-table-column label="答案" width="90" align="center">
              <template slot-scope="{ row }">
                <el-checkbox v-model="row.isRight" />
              </template>
            </el-table-column>
            <el-table-column label="标签" width="90" align="center">
              <template slot-scope="{ row }">
                <el-input v-model="row.label" />
              </template>
            </el-table-column>
            <el-table-column label="选项内容">
              <template slot-scope="{ row }">
                <el-input v-model="row.content" type="textarea" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button type="text" style="color: #f56c6c" @click="removeEditOption(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="答案解析">
          <el-input v-model="editForm.analysis" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import RepoSelect from '@/components/RepoSelect'
import { fetchCourseChapters, fetchCoursePage, fetchKnowledgePoints } from '@/api/course'
import {
  approveAiGeneratedQuestion,
  fetchAiGeneratedQuestionPage,
  generateAiQuestions,
  rejectAiGeneratedQuestion,
  updateAiGeneratedQuestion
} from '@/api/aiQuestion'

export default {
  name: 'AiQuestionReview',
  components: { RepoSelect },
  data() {
    return {
      generating: false,
      saving: false,
      editDialogVisible: false,
      selectedRepoId: '',
      statusFilter: 'PENDING',
      pageNum: 1,
      pageSize: 10,
      reviewPage: {},
      courseOptions: [],
      generateChapterOptions: [],
      generateKnowledgeOptions: [],
      generateForm: {
        courseId: '',
        chapterId: '',
        knowledgePointId: '',
        questionType: 'SINGLE',
        difficulty: 'MEDIUM',
        count: 3,
        optionCount: 4,
        extraRequirement: ''
      },
      editForm: {
        id: null,
        courseId: '',
        chapterId: '',
        knowledgePointId: '',
        questionType: 'SINGLE',
        difficulty: 'MEDIUM',
        content: '',
        analysis: '',
        options: []
      },
      generateRules: {
        courseId: [{ required: true, message: '请选择课程' }],
        chapterId: [{ required: true, message: '请选择章节' }],
        knowledgePointId: [{ required: true, message: '请选择知识点' }],
        questionType: [{ required: true, message: '请选择题型' }],
        difficulty: [{ required: true, message: '请选择难度' }],
        count: [{ required: true, message: '请输入题目数量' }]
      },
      questionTypeOptions: [
        { value: 'SINGLE', label: '单选题' },
        { value: 'MULTIPLE', label: '多选题' },
        { value: 'INDEFINITE', label: '不定项选择题' },
        { value: 'JUDGE', label: '判断题' }
      ],
      difficultyOptions: [
        { value: 'EASY', label: '简单' },
        { value: 'MEDIUM', label: '中等' },
        { value: 'HARD', label: '困难' }
      ]
    }
  },
  created() {
    this.loadCourses()
    this.fetchReviewPage()
  },
  methods: {
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000, status: 1 })
      this.courseOptions = (res.data && res.data.records) || []
    },
    async handleGenerateCourseChange(courseId) {
      this.generateForm.chapterId = ''
      this.generateForm.knowledgePointId = ''
      this.generateChapterOptions = []
      this.generateKnowledgeOptions = []
      if (courseId) {
        const res = await fetchCourseChapters(courseId, { status: 1 })
        this.generateChapterOptions = res.data || []
      }
    },
    async handleGenerateChapterChange(chapterId) {
      this.generateForm.knowledgePointId = ''
      this.generateKnowledgeOptions = []
      if (chapterId) {
        const res = await fetchKnowledgePoints(chapterId, { status: 1 })
        this.generateKnowledgeOptions = res.data || []
      }
    },
    handleGenerate() {
      this.$refs.generateForm.validate(async valid => {
        if (!valid) {
          return
        }
        this.generating = true
        try {
          await generateAiQuestions(this.generateForm)
          this.$message.success('AI 题目已生成，请在待审核列表中确认入库')
          this.statusFilter = 'PENDING'
          this.pageNum = 1
          await this.fetchReviewPage()
        } finally {
          this.generating = false
        }
      })
    },
    async fetchReviewPage() {
      const res = await fetchAiGeneratedQuestionPage({
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        status: this.statusFilter || null
      })
      this.reviewPage = res.data || {}
    },
    parsedContent(row) {
      try {
        return JSON.parse(row.contentJson || '{}')
      } catch (error) {
        return {}
      }
    },
    openEditDialog(row) {
      const parsed = this.parsedContent(row)
      this.editForm = {
        id: row.id,
        courseId: row.courseId,
        chapterId: row.chapterId,
        knowledgePointId: row.knowledgePointId,
        questionType: row.questionType,
        difficulty: row.difficulty,
        content: parsed.content || '',
        analysis: parsed.analysis || '',
        options: (parsed.options || []).map((item, index) => ({
          label: item.label || this.optionLabel(index),
          content: item.content || '',
          isRight: item.isRight === true || item.isRight === 1
        }))
      }
      this.editDialogVisible = true
    },
    handleEditTypeChange(type) {
      if (type === 'JUDGE') {
        this.editForm.options = [
          { label: 'A', content: '正确', isRight: true },
          { label: 'B', content: '错误', isRight: false }
        ]
      }
    },
    addEditOption() {
      this.editForm.options.push({
        label: this.optionLabel(this.editForm.options.length),
        content: '',
        isRight: false
      })
    },
    removeEditOption(index) {
      this.editForm.options.splice(index, 1)
    },
    validateEditForm() {
      if (!this.editForm.content) {
        this.$message.warning('题干不能为空')
        return false
      }
      if (!this.editForm.options.length || this.editForm.options.some(item => !item.content)) {
        this.$message.warning('选项不能为空')
        return false
      }
      const rightCount = this.editForm.options.filter(item => item.isRight).length
      if (this.editForm.questionType === 'SINGLE' && rightCount !== 1) {
        this.$message.warning('单选题必须且只能有一个正确答案')
        return false
      }
      if (this.editForm.questionType === 'JUDGE' && rightCount !== 1) {
        this.$message.warning('判断题必须且只能有一个正确答案')
        return false
      }
      if ((this.editForm.questionType === 'MULTIPLE' || this.editForm.questionType === 'INDEFINITE') && rightCount < 1) {
        this.$message.warning('多选/不定项题至少需要一个正确答案')
        return false
      }
      return true
    },
    async saveEdit() {
      if (!this.validateEditForm()) {
        return
      }
      this.saving = true
      try {
        await updateAiGeneratedQuestion(this.editForm.id, this.editForm)
        this.$message.success('保存成功')
        this.editDialogVisible = false
        await this.fetchReviewPage()
      } finally {
        this.saving = false
      }
    },
    handleApprove(row) {
      if (!this.selectedRepoId) {
        this.$message.warning('请先选择审核入库的题库')
        return
      }
      this.$confirm('确认将该 AI 题目加入正式题库？', '确认入库', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await approveAiGeneratedQuestion(row.id, this.selectedRepoId)
        this.$message.success('题目已加入正式题库')
        await this.fetchReviewPage()
      })
    },
    handleReject(row) {
      this.$confirm('该 AI 题目将标记为已删除/拒绝，是否继续？', '删除 AI 题目', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await rejectAiGeneratedQuestion(row.id)
        this.$message.success('已删除')
        await this.fetchReviewPage()
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.fetchReviewPage()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.fetchReviewPage()
    },
    questionTypeLabel(value) {
      const item = this.questionTypeOptions.find(option => option.value === value)
      return item ? item.label : '-'
    },
    difficultyLabel(value) {
      const item = this.difficultyOptions.find(option => option.value === value)
      return item ? item.label : '-'
    },
    difficultyTagType(value) {
      if (value === 'EASY') return 'success'
      if (value === 'HARD') return 'danger'
      return 'warning'
    },
    statusLabel(value) {
      if (value === 'APPROVED') return '已入库'
      if (value === 'REJECTED') return '已删除'
      return '待审核'
    },
    statusTagType(value) {
      if (value === 'APPROVED') return 'success'
      if (value === 'REJECTED') return 'info'
      return 'warning'
    },
    optionLabel(index) {
      return String.fromCharCode(65 + index)
    }
  }
}
</script>

<style scoped>
.generate-panel,
.review-panel {
  margin-bottom: 16px;
}

.panel-header,
.review-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.review-tools {
  justify-content: flex-end;
}

.el-select {
  width: 100%;
}

.pagination-container {
  margin-top: 16px;
  text-align: right;
}

.option-table {
  margin-top: 10px;
}
</style>
