<template>
  <div class="app-container">
    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="Question">
        <el-input v-model="searchName" clearable placeholder="Question content" />
      </el-form-item>
      <el-form-item label="Repo">
        <repo-select v-model="selectedRepoSingleSearch" />
      </el-form-item>
      <el-form-item label="Type">
        <el-select v-model="selValue" clearable placeholder="All types" style="width: 170px">
          <el-option v-for="item in questionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Course">
        <el-select
          v-model="searchCourseId"
          clearable
          filterable
          placeholder="All courses"
          style="width: 180px"
          @change="handleCourseFilterChange"
        >
          <el-option v-for="item in courseOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="Chapter">
        <el-select
          v-model="searchChapterId"
          clearable
          filterable
          :disabled="!searchCourseId"
          placeholder="All chapters"
          style="width: 180px"
          @change="handleChapterFilterChange"
        >
          <el-option v-for="item in chapterOptions" :key="item.id" :label="item.title" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="Knowledge">
        <el-select
          v-model="searchKnowledgePointId"
          clearable
          filterable
          :disabled="!searchChapterId"
          placeholder="All points"
          style="width: 180px"
        >
          <el-option v-for="item in knowledgePointOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="Difficulty">
        <el-select v-model="searchDifficulty" clearable placeholder="All" style="width: 140px">
          <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchQu">Search</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="screenInfo()">Add</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" plain @click="$router.push({ name: 'ai-generated-questions' })">AI Generate</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fileDialogVisible = true">Import</el-button>
      </el-form-item>
    </el-form>

    <el-dialog
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
      title="Upload file"
      :visible.sync="fileDialogVisible"
    >
      <div style="margin-bottom: 10px">Select repo:</div>
      <repo-select v-model="selectedRepoSingle" style="margin-bottom: 10px" />
      <el-upload
        class="upload-demo"
        drag
        action="xxxxxx"
        multiple
        :limit="1"
        accept=".xlsx, .xls"
        :auto-upload="false"
        :on-remove="handleRemove"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">Drop file here, or <em>click to upload</em></div>
        <div slot="tip" class="el-upload__tip">Only xls/xlsx files are allowed.</div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="fileDialogVisible = false">Cancel</el-button>
        <el-button type="success" plain @click="startDownload">Download template</el-button>
        <el-button type="primary" @click="importQu">OK</el-button>
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
      <el-table-column label="No." align="center" width="70">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="content" label="Question" align="center" min-width="220" />
      <el-table-column label="Type" align="center" width="150">
        <template slot-scope="scope">{{ questionTypeLabel(scope.row.quType) }}</template>
      </el-table-column>
      <el-table-column prop="repoTitle" label="Repo" align="center" min-width="130" />
      <el-table-column prop="courseName" label="Course" align="center" min-width="130">
        <template slot-scope="scope">{{ scope.row.courseName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="chapterTitle" label="Chapter" align="center" min-width="130">
        <template slot-scope="scope">{{ scope.row.chapterTitle || '-' }}</template>
      </el-table-column>
      <el-table-column prop="knowledgePointName" label="Knowledge" align="center" min-width="130">
        <template slot-scope="scope">{{ scope.row.knowledgePointName || '-' }}</template>
      </el-table-column>
      <el-table-column label="Difficulty" align="center" width="110">
        <template slot-scope="scope">
          <el-tag size="mini" :type="difficultyTagType(scope.row.difficulty)">
            {{ difficultyLabel(scope.row.difficulty) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Created" align="center" width="160" />
      <el-table-column align="center" label="Actions" width="130">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" style="font-size: 14px" @click="updateRow(row)">Edit</el-button>
          <el-button type="text" size="small" style="color: red; font-size: 14px" @click="delQu(row)">Delete</el-button>
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
  </div>
</template>

<script>
import { importQue, quDel, quPaging } from '@/api/question'
import { fetchCourseChapters, fetchCoursePage, fetchKnowledgePoints } from '@/api/course'
import RepoSelect from '@/components/RepoSelect'

export default {
  components: { RepoSelect },
  data() {
    return {
      questionTypeOptions: [
        { value: 1, label: 'Single choice' },
        { value: 2, label: 'Multiple choice' },
        { value: 3, label: 'True or false' },
        { value: 4, label: 'Indefinite choice' }
      ],
      difficultyOptions: [
        { value: 'EASY', label: 'Easy' },
        { value: 'MEDIUM', label: 'Medium' },
        { value: 'HARD', label: 'Hard' }
      ],
      courseOptions: [],
      chapterOptions: [],
      knowledgePointOptions: [],
      fileList: [],
      selValue: '',
      searchName: '',
      searchCourseId: '',
      searchChapterId: '',
      searchKnowledgePointId: '',
      searchDifficulty: '',
      pageNum: 1,
      pageSize: 10,
      data: {},
      fileDialogVisible: false,
      selectedRepoSingle: '',
      selectedRepoSingleSearch: '',
      formInline: {},
      hasFiles: null
    }
  },
  created() {
    this.loadCourses()
    this.getQuPage()
  },
  methods: {
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000, status: 1 })
      this.courseOptions = (res.data && res.data.records) || []
    },
    async handleCourseFilterChange(courseId) {
      this.searchChapterId = ''
      this.searchKnowledgePointId = ''
      this.chapterOptions = []
      this.knowledgePointOptions = []
      if (courseId) {
        const res = await fetchCourseChapters(courseId, { status: 1 })
        this.chapterOptions = res.data || []
      }
    },
    async handleChapterFilterChange(chapterId) {
      this.searchKnowledgePointId = ''
      this.knowledgePointOptions = []
      if (chapterId) {
        const res = await fetchKnowledgePoints(chapterId, { status: 1 })
        this.knowledgePointOptions = res.data || []
      }
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
    updateRow(row) {
      localStorage.setItem('quId', row.id)
      this.$router.push({ name: 'questions-add' })
    },
    importQu() {
      if (this.fileList && this.fileList.length > 0 && this.selectedRepoSingle !== '') {
        const formData = new FormData()
        formData.append('file', this.fileList[0].raw)
        importQue(this.selectedRepoSingle, formData)
          .then((response) => {
            if (response.code) {
              this.$message.success('Import success')
              this.getQuPage(this.pageNum, this.pageSize)
              this.fileDialogVisible = false
              this.selectedRepoSingle = ''
              this.fileList = []
            }
          })
          .catch(() => {
            this.fileList = []
          })
      } else {
        this.$message.warning('Please select repo and file')
      }
    },
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    handleRemove(file, fileList) {
      if (fileList.length === 0) {
        this.hasFiles = false
      }
    },
    async getQuPage(pageNum = this.pageNum, pageSize = this.pageSize) {
      const params = {
        pageNum,
        pageSize,
        content: this.searchName || null,
        repoId: this.selectedRepoSingleSearch || null,
        type: this.selValue || null,
        courseId: this.searchCourseId || null,
        chapterId: this.searchChapterId || null,
        knowledgePointId: this.searchKnowledgePointId || null,
        difficulty: this.searchDifficulty || null
      }
      const res = await quPaging(params)
      this.data = res.data
    },
    delQu(row) {
      this.$confirm('This question will be deleted. Continue?', 'Warning', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning',
        center: true
      }).then(() => {
        quDel(row.id).then((res) => {
          if (res.code) {
            this.getQuPage(this.pageNum, this.pageSize)
            this.$message({ type: 'success', message: 'Delete success' })
          }
        })
      }).catch(() => {
        this.$message({ type: 'info', message: 'Canceled' })
      })
    },
    searchQu() {
      this.pageNum = 1
      this.getQuPage(1, this.pageSize)
    },
    screenInfo() {
      this.$router.push({ name: 'questions-add' })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getQuPage(this.pageNum, val)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getQuPage(val, this.pageSize)
    },
    startDownload() {
      const a = document.createElement('a')
      a.href = './template/ImportQuestionTemplate.xlsx'
      a.download = 'ImportQuestionTemplate.xlsx'
      a.style.display = 'none'
      document.body.appendChild(a)
      a.click()
      a.remove()
    }
  }
}
</script>

<style></style>
