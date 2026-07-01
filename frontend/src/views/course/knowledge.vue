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
      <el-form-item label="章节">
        <el-select v-model="query.chapterId" filterable placeholder="请选择章节" @change="loadKnowledgePoints">
          <el-option
            v-for="item in chapterOptions"
            :key="item.id"
            :label="item.title"
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
        <el-button type="primary" @click="loadKnowledgePoints">查询</el-button>
        <el-button type="primary" :disabled="!query.chapterId" @click="openCreateDialog">新增知识点</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="knowledgePoints"
      border
      fit
      highlight-current-row
      :header-cell-style="tableHeaderStyle"
    >
      <el-table-column label="序号" align="center" width="80">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="name" label="知识点名称" min-width="180" />
      <el-table-column prop="chapterTitle" label="所属章节" min-width="160" />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" align="center" width="90" />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="150">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button
            type="text"
            size="small"
            style="color: #f56c6c"
            @click="disableKnowledgePoint(row)"
          >禁用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && knowledgePoints.length === 0" class="empty-block">
      暂无知识点数据
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="所属章节">
          <el-select v-model="form.chapterId" placeholder="请选择章节" style="width: 100%">
            <el-option
              v-for="item in chapterOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称">
          <el-input v-model="form.name" placeholder="请输入知识点名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入知识点描述" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitKnowledgePoint">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addKnowledgePoint,
  deleteKnowledgePoint,
  fetchCourseChapters,
  fetchCoursePage,
  fetchKnowledgePoints,
  updateKnowledgePoint
} from '@/api/course'

export default {
  data() {
    return {
      loading: false,
      query: {
        courseId: '',
        chapterId: '',
        status: ''
      },
      courseOptions: [],
      chapterOptions: [],
      knowledgePoints: [],
      dialogVisible: false,
      dialogTitle: '新增知识点',
      form: {
        id: null,
        chapterId: '',
        name: '',
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
        chapterId: this.query.chapterId,
        name: '',
        description: '',
        sortOrder: 0,
        status: 1
      }
    },
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000 })
      this.courseOptions = (res.data && res.data.records) || []
      const routeCourseId = this.$route.query.courseId ? Number(this.$route.query.courseId) : null
      const routeChapterId = this.$route.query.chapterId ? Number(this.$route.query.chapterId) : null
      if (routeCourseId) {
        this.query.courseId = routeCourseId
      } else if (!this.query.courseId && this.courseOptions.length > 0) {
        this.query.courseId = this.courseOptions[0].id
      }
      if (this.query.courseId) {
        await this.loadChapters(routeChapterId)
      }
    },
    async handleCourseChange() {
      this.query.chapterId = ''
      this.knowledgePoints = []
      await this.loadChapters()
    },
    async loadChapters(preferredChapterId) {
      const res = await fetchCourseChapters(this.query.courseId)
      this.chapterOptions = res.data || []
      if (preferredChapterId) {
        this.query.chapterId = preferredChapterId
      } else if (!this.query.chapterId && this.chapterOptions.length > 0) {
        this.query.chapterId = this.chapterOptions[0].id
      }
      if (this.query.chapterId) {
        this.loadKnowledgePoints()
      }
    },
    async loadKnowledgePoints() {
      if (!this.query.chapterId) {
        this.knowledgePoints = []
        return
      }
      this.loading = true
      try {
        const params = {
          status: this.query.status === '' ? null : this.query.status
        }
        const res = await fetchKnowledgePoints(this.query.chapterId, params)
        this.knowledgePoints = res.data || []
      } finally {
        this.loading = false
      }
    },
    openCreateDialog() {
      this.dialogTitle = '新增知识点'
      this.form = this.emptyForm()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.dialogTitle = '编辑知识点'
      this.form = {
        id: row.id,
        chapterId: row.chapterId,
        name: row.name,
        description: row.description,
        sortOrder: row.sortOrder,
        status: row.status
      }
      this.dialogVisible = true
    },
    submitKnowledgePoint() {
      if (!this.form.chapterId) {
        this.$message.warning('请选择章节')
        return
      }
      if (!this.form.name) {
        this.$message.warning('请输入知识点名称')
        return
      }
      const data = {
        chapterId: this.form.chapterId,
        name: this.form.name,
        description: this.form.description,
        sortOrder: this.form.sortOrder,
        status: this.form.status
      }
      const request = this.form.id ? updateKnowledgePoint(this.form.id, data) : addKnowledgePoint(data)
      request.then(() => {
        this.$message.success(this.form.id ? '编辑成功' : '新增成功')
        this.dialogVisible = false
        this.query.chapterId = this.form.chapterId
        this.loadKnowledgePoints()
      })
    },
    disableKnowledgePoint(row) {
      this.$confirm('确认禁用该知识点？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteKnowledgePoint(row.id).then(() => {
          this.$message.success('禁用成功')
          this.loadKnowledgePoints()
        })
      })
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
