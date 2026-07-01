import request from '@/utils/request'

export function fetchCoursePage(params) {
  return request({
    url: 'courses/paging',
    method: 'get',
    params
  })
}

export function fetchMyCourses(params) {
  return request({
    url: 'courses/my',
    method: 'get',
    params
  })
}

export function fetchCourseDetail(id) {
  return request({
    url: `courses/${id}`,
    method: 'get'
  })
}

export function addCourse(data) {
  return request({
    url: 'courses',
    method: 'post',
    data
  })
}

export function updateCourse(id, data) {
  return request({
    url: `courses/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourse(id) {
  return request({
    url: `courses/${id}`,
    method: 'delete'
  })
}

export function fetchCourseChapters(courseId, params) {
  return request({
    url: `course-chapters/course/${courseId}`,
    method: 'get',
    params
  })
}

export function addChapter(data) {
  return request({
    url: 'course-chapters',
    method: 'post',
    data
  })
}

export function updateChapter(id, data) {
  return request({
    url: `course-chapters/${id}`,
    method: 'put',
    data
  })
}

export function deleteChapter(id) {
  return request({
    url: `course-chapters/${id}`,
    method: 'delete'
  })
}

export function fetchKnowledgePoints(chapterId, params) {
  return request({
    url: `knowledge-points/chapter/${chapterId}`,
    method: 'get',
    params
  })
}

export function addKnowledgePoint(data) {
  return request({
    url: 'knowledge-points',
    method: 'post',
    data
  })
}

export function updateKnowledgePoint(id, data) {
  return request({
    url: `knowledge-points/${id}`,
    method: 'put',
    data
  })
}

export function deleteKnowledgePoint(id) {
  return request({
    url: `knowledge-points/${id}`,
    method: 'delete'
  })
}

export function fetchCourseLearning(courseId) {
  return request({
    url: `course-learning/course/${courseId}`,
    method: 'get'
  })
}

export function fetchCourseVideos(params) {
  return request({
    url: 'course-learning/videos',
    method: 'get',
    params
  })
}

export function addCourseVideo(data) {
  return request({
    url: 'course-learning/videos',
    method: 'post',
    data
  })
}

export function updateCourseVideo(id, data) {
  return request({
    url: `course-learning/videos/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourseVideo(id) {
  return request({
    url: `course-learning/videos/${id}`,
    method: 'delete'
  })
}

export function fetchCourseMaterials(params) {
  return request({
    url: 'course-learning/materials',
    method: 'get',
    params
  })
}

export function addCourseMaterial(data) {
  return request({
    url: 'course-learning/materials',
    method: 'post',
    data
  })
}

export function updateCourseMaterial(id, data) {
  return request({
    url: `course-learning/materials/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourseMaterial(id) {
  return request({
    url: `course-learning/materials/${id}`,
    method: 'delete'
  })
}

export function reportVideoProgress(data) {
  return request({
    url: 'course-learning/progress',
    method: 'post',
    data
  })
}

export function fetchMyVideoProgress(params) {
  return request({
    url: 'course-learning/progress/my',
    method: 'get',
    params
  })
}

export function fetchCourseVideoProgress(params) {
  return request({
    url: 'course-learning/progress/course',
    method: 'get',
    params
  })
}
