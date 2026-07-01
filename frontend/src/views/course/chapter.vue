<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="demo-form-inline">
      <el-form-item label="课程">
        <el-select v-model="query.courseId" filterable placeholder="请选择课程" @change="handleCourseChange">
          <el-option
            v-for="item in courseOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="请选择状态">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadChapters">查询</el-button>
        <el-button type="primary" :disabled="!query.courseId" @click="openCreateDialog">新增章节</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="chapters"
      border
      fit
      highlight-current-row
      :header-cell-style="tableHeaderStyle"
    >
      <el-table-column label="序号" align="center" width="80">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="title" label="章节标题" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" align="center" width="90" />
      <el-table-column prop="knowledgePointCount" label="知识点数" align="center" width="100" />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="230">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="text" size="small" @click="goKnowledge(row)">知识点</el-button>
          <el-button
            type="text"
            size="small"
            style="color: #f56c6c"
            @click="disableChapter(row)"
          >禁用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && chapters.length === 0" class="empty-block">
      暂无章节数据
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="item in courseOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="章节标题">
          <el-input v-model="form.title" placeholder="请输入章节标题" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入章节描述" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChapter">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addChapter, deleteChapter, fetchCourseChapters, fetchCoursePage, updateChapter } from '@/api/course'

export default {
  data() {
    return {
      loading: false,
      query: {
        courseId: '',
        status: ''
      },
      courseOptions: [],
      chapters: [],
      dialogVisible: false,
      dialogTitle: '新增章节',
      form: {
        id: null,
        courseId: '',
        title: '',
        description: '',
        sortOrder: 0,
        status: 1
      },
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
    emptyForm() {
      return {
        id: null,
        courseId: this.query.courseId,
        title: '',
        description: '',
        sortOrder: 0,
        status: 1
      }
    },
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000 })
      this.courseOptions = (res.data && res.data.records) || []
      const routeCourseId = this.$route.query.courseId ? Number(this.$route.query.courseId) : null
      if (routeCourseId) {
        this.query.courseId = routeCourseId
      } else if (!this.query.courseId && this.courseOptions.length > 0) {
        this.query.courseId = this.courseOptions[0].id
      }
      if (this.query.courseId) {
        this.loadChapters()
      }
    },
    handleCourseChange() {
      this.loadChapters()
    },
    async loadChapters() {
      if (!this.query.courseId) {
        this.chapters = []
        return
      }
      this.loading = true
      try {
        const params = {
          status: this.query.status === '' ? null : this.query.status
        }
        const res = await fetchCourseChapters(this.query.courseId, params)
        this.chapters = res.data || []
      } finally {
        this.loading = false
      }
    },
    openCreateDialog() {
      this.dialogTitle = '新增章节'
      this.form = this.emptyForm()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.dialogTitle = '编辑章节'
      this.form = {
        id: row.id,
        courseId: row.courseId,
        title: row.title,
        description: row.description,
        sortOrder: row.sortOrder,
        status: row.status
      }
      this.dialogVisible = true
    },
    submitChapter() {
      if (!this.form.courseId) {
        this.$message.warning('请选择课程')
        return
      }
      if (!this.form.title) {
        this.$message.warning('请输入章节标题')
        return
      }
      const data = {
        courseId: this.form.courseId,
        title: this.form.title,
        description: this.form.description,
        sortOrder: this.form.sortOrder,
        status: this.form.status
      }
      const request = this.form.id ? updateChapter(this.form.id, data) : addChapter(data)
      request.then(() => {
        this.$message.success(this.form.id ? '编辑成功' : '新增成功')
        this.dialogVisible = false
        this.query.courseId = this.form.courseId
        this.loadChapters()
      })
    },
    disableChapter(row) {
      this.$confirm('确认禁用该章节？章节下知识点也会同步禁用。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteChapter(row.id).then(() => {
          this.$message.success('禁用成功')
          this.loadChapters()
        })
      })
    },
    goKnowledge(row) {
      this.$router.push({ path: '/knowledge-management', query: { courseId: row.courseId, chapterId: row.id }})
    }
  }
}
</script>

<style scoped>
.empty-block {
  color: #909399;
  padding: 32px 0;
  text-align: center;
}
</style>
