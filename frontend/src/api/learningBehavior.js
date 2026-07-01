import request from '@/utils/request'

export function fetchMyLearningBehavior() {
  return request({
    url: 'ai/learning-behavior/my',
    method: 'get'
  })
}

export function fetchStudentLearningBehaviors(params) {
  return request({
    url: 'ai/learning-behavior/students',
    method: 'get',
    params
  })
}

export function fetchStudentLearningBehavior(studentId) {
  return request({
    url: `ai/learning-behavior/students/${studentId}`,
    method: 'get'
  })
}
