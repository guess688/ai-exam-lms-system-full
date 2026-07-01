<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="demo-form-inline">
      <el-form-item label="课程名称">
        <el-input v-model="query.name" clearable placeholder="请输入课程名称" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadCourses">查询</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="20">
      <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="course-card" shadow="hover">
          <div v-if="course.coverUrl" class="cover" :style="{ backgroundImage: 'url(' + course.coverUrl + ')' }" />
          <div v-else class="cover cover-placeholder">
            <span>{{ course.name }}</span>
          </div>
          <div class="course-title">{{ course.name }}</div>
          <div class="course-meta">{{ course.gradeName || '未分配班级' }}</div>
          <div class="course-desc">{{ course.description || '暂无课程简介' }}</div>
          <div class="course-footer">
            <span>{{ course.chapterCount || 0 }} 章 / {{ course.knowledgePointCount || 0 }} 个知识点</span>
            <el-button type="text" @click="goDetail(course)">查看详情</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!loading && courses.length === 0" class="empty-block">
      暂无可见课程
    </div>
  </div>
</template>

<script>
import { fetchMyCourses } from '@/api/course'

export default {
  data() {
    return {
      loading: false,
      query: {
        name: ''
      },
      courses: []
    }
  },
  created() {
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      this.loading = true
      try {
        const res = await fetchMyCourses({ name: this.query.name || null })
        this.courses = res.data || []
      } finally {
        this.loading = false
      }
    },
    goDetail(course) {
      this.$router.push({ path: '/course-detail', query: { id: course.id }})
    }
  }
}
</script>

<style scoped>
.course-card {
  border-radius: 6px;
  margin-bottom: 20px;
}

.cover {
  background-position: center;
  background-size: cover;
  border-radius: 4px;
  height: 128px;
  margin-bottom: 14px;
}

.cover-placeholder {
  align-items: center;
  background: #f3f6fb;
  color: #606266;
  display: flex;
  font-size: 18px;
  justify-content: center;
  overflow: hidden;
  padding: 12px;
  text-align: center;
}

.course-title {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.course-meta {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.course-desc {
  color: #606266;
  height: 44px;
  line-height: 22px;
  margin-top: 10px;
  overflow: hidden;
}

.course-footer {
  align-items: center;
  border-top: 1px solid #ebeef5;
  color: #909399;
  display: flex;
  font-size: 13px;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 10px;
}

.empty-block {
  color: #909399;
  padding: 42px 0;
  text-align: center;
}
</style>
