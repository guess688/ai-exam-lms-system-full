import request from '@/utils/request'

export function fetchLearningTaskPage(params) {
  return request({
    url: 'learning-tasks/paging',
    method: 'get',
    params
  })
}

export function createLearningTask(data) {
  return request({
    url: 'learning-tasks',
    method: 'post',
    data
  })
}

export function updateLearningTask(id, data) {
  return request({
    url: `learning-tasks/${id}`,
    method: 'put',
    data
  })
}

export function disableLearningTask(id) {
  return request({
    url: `learning-tasks/${id}`,
    method: 'delete'
  })
}

export function fetchLearningTaskRecords(taskId) {
  return request({
    url: `learning-tasks/${taskId}/records`,
    method: 'get'
  })
}

export function fetchMyLearningTasks(params) {
  return request({
    url: 'learning-tasks/my',
    method: 'get',
    params
  })
}

export function confirmReviewTask(taskId) {
  return request({
    url: `learning-tasks/${taskId}/confirm-review`,
    method: 'post'
  })
}
