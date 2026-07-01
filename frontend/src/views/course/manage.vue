<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="demo-form-inline">
      <el-form-item label="课程名称">
        <el-input v-model="query.name" clearable placeholder="请输入课程名称" />
      </el-form-item>
      <el-form-item label="班级">
        <el-select v-model="query.gradeId" clearable placeholder="请选择班级">
          <el-option
            v-for="item in classOptions"
            :key="item.id"
            :label="item.gradeName"
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
        <el-button type="primary" @click="searchCourses">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="primary" @click="openCreateDialog">新增课程</el-button>
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
      <el-table-column label="序号" align="center" width="80">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="name" label="课程名称" min-width="160" />
      <el-table-column prop="gradeName" label="班级" align="center" width="130" />
      <el-table-column prop="teacherName" label="负责教师" align="center" width="120" />
      <el-table-column prop="chapterCount" label="章节数" align="center" width="90" />
      <el-table-column prop="knowledgePointCount" label="知识点数" align="center" width="100" />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" align="center" width="170" />
      <el-table-column label="操作" align="center" fixed="right" width="260">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="text" size="small" @click="goChapters(row)">章节</el-button>
          <el-button type="text" size="small" @click="goVideos(row)">网课</el-button>
          <el-button
            type="text"
            size="small"
            style="color: #f56c6c"
            @click="disableCourse(row)"
          >禁用</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="课程名称">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.gradeId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="item in classOptions"
              :key="item.id"
              :label="item.gradeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="form.coverUrl" placeholder="可填写课程封面图片 URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入课程描述" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCourse">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchClasses } from '@/api/class_'
import { addCourse, deleteCourse, fetchCoursePage, updateCourse } from '@/api/course'

export default {
  data() {
    return {
      loading: false,
      pageNum: 1,
      pageSize: 10,
      query: {
        name: '',
        gradeId: '',
        status: ''
      },
      data: {
        records: [],
        total: 0,
        size: 10,
        current: 1
      },
      classOptions: [],
      dialogVisible: false,
      dialogTitle: '新增课程',
      form: {
        id: null,
        name: '',
        description: '',
        coverUrl: '',
        gradeId: '',
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
    this.loadClasses()
    this.loadCourses()
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        name: '',
        description: '',
        coverUrl: '',
        gradeId: '',
        status: 1
      }
    },
    async loadClasses() {
      const res = await fetchClasses()
      this.classOptions = res.data || []
    },
    async loadCourses() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.query.name || null,
          gradeId: this.query.gradeId || null,
          status: this.query.status === '' ? null : this.query.status
        }
        const res = await fetchCoursePage(params)
        this.data = res.data
      } finally {
        this.loading = false
      }
    },
    searchCourses() {
      this.pageNum = 1
      this.loadCourses()
    },
    resetSearch() {
      this.query = {
        name: '',
        gradeId: '',
        status: ''
      }
      this.searchCourses()
    },
    openCreateDialog() {
      this.dialogTitle = '新增课程'
      this.form = this.emptyForm()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.dialogTitle = '编辑课程'
      this.form = {
        id: row.id,
        name: row.name,
        description: row.description,
        coverUrl: row.coverUrl,
        gradeId: row.gradeId,
        status: row.status
      }
      this.dialogVisible = true
    },
    submitCourse() {
      if (!this.form.name) {
        this.$message.warning('请输入课程名称')
        return
      }
      if (!this.form.gradeId) {
        this.$message.warning('请选择班级')
        return
      }
      const data = {
        name: this.form.name,
        description: this.form.description,
        coverUrl: this.form.coverUrl,
        gradeId: this.form.gradeId,
        status: this.form.status
      }
      const request = this.form.id ? updateCourse(this.form.id, data) : addCourse(data)
      request.then(() => {
        this.$message.success(this.form.id ? '编辑成功' : '新增成功')
        this.dialogVisible = false
        this.loadCourses()
      })
    },
    disableCourse(row) {
      this.$confirm('确认禁用该课程？课程下章节和知识点也会同步禁用。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCourse(row.id).then(() => {
          this.$message.success('禁用成功')
          this.loadCourses()
        })
      })
    },
    goChapters(row) {
      this.$router.push({ path: '/chapter-management', query: { courseId: row.id }})
    },
    goVideos(row) {
      this.$router.push({ path: '/course-video-management', query: { courseId: row.id }})
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadCourses()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadCourses()
    }
  }
}
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  text-align: center;
}
</style>
