<template>
  <div class="app-container">
    <el-form
      ref="postForm"
      :model="postForm"
      :rules="rules"
      label-position="left"
      label-width="150px"
    >
      <el-card>
        <el-form-item label="Question type" prop="quType">
          <el-select
            v-model="postForm.quType"
            :disabled="quTypeDisabled"
            class="filter-item"
            style="width: 400px"
            @change="handleTypeChange"
          >
            <el-option v-for="item in quTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="Repo" prop="repoId">
          <repo-select v-model="postForm.repoId" :multi="false" style="width: 400px" />
        </el-form-item>

        <el-form-item label="Course" prop="courseId">
          <el-select
            v-model="postForm.courseId"
            clearable
            filterable
            placeholder="Select course"
            style="width: 400px"
            @change="handleCourseChange"
          >
            <el-option v-for="item in courseOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="Chapter" prop="chapterId">
          <el-select
            v-model="postForm.chapterId"
            clearable
            filterable
            :disabled="!postForm.courseId"
            placeholder="Select chapter"
            style="width: 400px"
            @change="handleChapterChange"
          >
            <el-option v-for="item in chapterOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="Knowledge point" prop="knowledgePointId">
          <el-select
            v-model="postForm.knowledgePointId"
            clearable
            filterable
            :disabled="!postForm.chapterId"
            placeholder="Select knowledge point"
            style="width: 400px"
          >
            <el-option v-for="item in knowledgePointOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="Difficulty" prop="difficulty">
          <el-select v-model="postForm.difficulty" placeholder="Select difficulty" style="width: 400px">
            <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="Question content" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="4"
            resize="vertical"
            style="width: 1200px"
          />
        </el-form-item>

        <el-form-item label="Question image" style="margin-left: 7px">
          <file-upload v-model="postForm.image" accept=".jpg,.jepg,.png" />
        </el-form-item>

        <el-form-item label="Analysis" style="margin-left: 7px">
          <el-input
            v-model="postForm.analysis"
            type="textarea"
            :rows="8"
            resize="vertical"
            style="width: 1200px"
          />
        </el-form-item>
      </el-card>

      <div v-if="postForm.quType" class="filter-container" style="margin-top: 25px">
        <el-button
          class="filter-item"
          type="primary"
          icon="el-icon-plus"
          size="small"
          plain
          @click="handleAdd"
        >
          Add
        </el-button>

        <el-table :data="activeOptions" :border="true" style="width: 90%">
          <el-table-column label="Right" width="120" align="center">
            <template v-slot="scope">
              <el-checkbox v-model="scope.row.isRight">Answer</el-checkbox>
            </template>
          </el-table-column>

          <el-table-column v-if="itemImage" label="Image" width="120px" align="center">
            <template v-slot="scope">
              <file-upload v-model="scope.row.image" accept=".jpg,.jepg,.png" />
            </template>
          </el-table-column>

          <el-table-column label="Option content">
            <template v-slot="scope">
              <el-input v-model="scope.row.content" type="textarea" />
            </template>
          </el-table-column>

          <el-table-column label="Actions" align="center" width="100px">
            <template v-slot="scope">
              <el-button type="danger" icon="el-icon-delete" circle @click="removeItem(scope.$index)" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-table
        v-if="false"
        :data="postForm.options"
        :border="true"
        style="width: 90%; margin-top: 30px"
      >
        <el-table-column label="Answer content">
          <template v-slot="scope">
            <el-input v-model="scope.row.content" type="textarea" />
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px">
        <el-button type="primary" @click="submitForm">Save</el-button>
        <el-button type="info" @click="onCancel">Back</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { quAdd, quDetail, quUpdate } from '@/api/question'
import { fetchCourseChapters, fetchCoursePage, fetchKnowledgePoints } from '@/api/course'
import RepoSelect from '@/components/RepoSelect'
import FileUpload from '@/components/FileUpload'

export default {
  name: 'QuDetail',
  components: { FileUpload, RepoSelect },
  data() {
    return {
      quId: '',
      quTypeDisabled: false,
      itemImage: true,
      courseOptions: [],
      chapterOptions: [],
      knowledgePointOptions: [],
      difficultyOptions: [
        { value: 'EASY', label: 'Easy' },
        { value: 'MEDIUM', label: 'Medium' },
        { value: 'HARD', label: 'Hard' }
      ],
      quTypes: [
        { value: 1, label: 'Single choice' },
        { value: 2, label: 'Multiple choice' },
        { value: 3, label: 'True or false' },
        { value: 4, label: 'Indefinite choice' }
      ],
      postForm: {
        repoId: '',
        courseId: '',
        chapterId: '',
        knowledgePointId: '',
        difficulty: 'MEDIUM',
        options: []
      },
      rules: {
        content: [{ required: true, message: 'Question content is required' }],
        quType: [{ required: true, message: 'Question type is required' }],
        repoId: [{ required: true, message: 'Repo is required' }],
        courseId: [{ required: true, message: 'Course is required' }],
        chapterId: [{ required: true, message: 'Chapter is required' }],
        knowledgePointId: [{ required: true, message: 'Knowledge point is required' }],
        difficulty: [{ required: true, message: 'Difficulty is required' }]
      }
    }
  },
  computed: {
    activeOptions() {
      return (this.postForm.options || []).filter(option => !option.isDeleted)
    }
  },
  created() {
    this.loadCourses()
    this.quId = localStorage.getItem('quId')
    if (this.quId) {
      this.getQuDetail()
    }
  },
  beforeDestroy() {
    localStorage.removeItem('quId')
    this.postForm = {}
  },
  methods: {
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000, status: 1 })
      this.courseOptions = (res.data && res.data.records) || []
    },
    async loadChapters(courseId) {
      if (!courseId) {
        this.chapterOptions = []
        return
      }
      const res = await fetchCourseChapters(courseId, { status: 1 })
      this.chapterOptions = res.data || []
    },
    async loadKnowledgePoints(chapterId) {
      if (!chapterId) {
        this.knowledgePointOptions = []
        return
      }
      const res = await fetchKnowledgePoints(chapterId, { status: 1 })
      this.knowledgePointOptions = res.data || []
    },
    async handleCourseChange(courseId) {
      this.postForm.chapterId = ''
      this.postForm.knowledgePointId = ''
      this.knowledgePointOptions = []
      await this.loadChapters(courseId)
    },
    async handleChapterChange(chapterId) {
      this.postForm.knowledgePointId = ''
      await this.loadKnowledgePoints(chapterId)
    },
    async getQuDetail() {
      const res = await quDetail(this.quId)
      if (res.code) {
        const detail = Object.assign({
          repoId: '',
          courseId: '',
          chapterId: '',
          knowledgePointId: '',
          difficulty: 'MEDIUM',
          options: []
        }, res.data)
        detail.options = (detail.options || []).map(item => {
          item.isRight = item.isRight === 1 || item.isRight === true
          return item
        })
        this.postForm = detail
        await this.loadChapters(this.postForm.courseId)
        await this.loadKnowledgePoints(this.postForm.chapterId)
      }
    },
    handleTypeChange(v) {
      this.postForm.options = []
      if (v === 3) {
        this.postForm.options.push({ isRight: true, content: 'True' })
        this.postForm.options.push({ isRight: false, content: 'False' })
      }
      if (v === 1 || v === 2 || v === 4) {
        this.postForm.options.push({ isRight: false, content: '' })
        this.postForm.options.push({ isRight: false, content: '' })
        this.postForm.options.push({ isRight: false, content: '' })
        this.postForm.options.push({ isRight: false, content: '' })
      }
    },
    handleAdd() {
      this.postForm.options.push({ isRight: false, content: '' })
    },
    removeItem(index) {
      const option = this.activeOptions[index]
      if (!option) {
        return
      }
      if (option.id) {
        option.isDeleted = 1
      } else {
        const rawIndex = this.postForm.options.indexOf(option)
        if (rawIndex >= 0) {
          this.postForm.options.splice(rawIndex, 1)
        }
      }
    },
    validateOptions() {
      const rightCount = this.activeOptions.filter(item => item.isRight).length
      if (this.activeOptions.length < 2) {
        this.$message({ message: 'Objective question needs at least two options', type: 'warning' })
        return false
      }
      if (this.activeOptions.some(item => !item.content)) {
        this.$message({ message: 'Option or answer content is required', type: 'warning' })
        return false
      }
      if (this.postForm.quType === 1 && rightCount !== 1) {
        this.$message({ message: 'Single choice question must have exactly one right answer', type: 'warning' })
        return false
      }
      if (this.postForm.quType === 2 && rightCount < 1) {
        this.$message({ message: 'Multiple choice question needs at least one right answer', type: 'warning' })
        return false
      }
      if (this.postForm.quType === 3 && rightCount !== 1) {
        this.$message({ message: 'True or false question must have exactly one right answer', type: 'warning' })
        return false
      }
      if (this.postForm.quType === 4 && rightCount < 1) {
        this.$message({ message: 'Indefinite choice question needs at least one right answer', type: 'warning' })
        return false
      }
      return true
    },
    submitForm() {
      if (!this.validateOptions()) {
        return
      }
      this.$refs.postForm.validate((valid) => {
        if (!valid) {
          return
        }
        const payload = Object.assign({}, this.postForm, {
          options: (this.postForm.options || []).map(option => Object.assign({}, option, {
            isRight: option.isRight ? 1 : 0
          }))
        })
        if (this.quId) {
          quUpdate(this.quId, payload).then(res => {
            if (res.code) {
              this.$notify({ title: 'Success', message: `${res.msg}`, type: 'success', duration: 2000 })
              this.$router.push({ name: 'questions-management' })
            }
          })
        } else {
          quAdd(payload).then((response) => {
            if (response.code) {
              this.$notify({ title: 'Success', message: 'Question saved', type: 'success', duration: 2000 })
              this.$router.push({ name: 'questions-management' })
            }
          })
        }
      })
    },
    onCancel() {
      this.$router.push({ name: 'questions-management' })
    }
  }
}
</script>

<style scoped>
.el-button--primary.is-plain {
  color: #409eff;
  background: #ecf5ff;
  border-color: #b3d8ff;
  margin-bottom: 25px;
}

.el-form-item {
  margin-bottom: 22px;
}

.el-textarea__inner {
  min-height: 120px;
  font-size: 14px;
  line-height: 1.5;
}

.el-form-item__label {
  font-weight: 500;
}
</style>
