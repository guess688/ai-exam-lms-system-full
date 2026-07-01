import request from '@/utils/request'

export function generateAiQuestions(data) {
  return request({
    url: 'ai/generated-questions/generate',
    method: 'post',
    data
  })
}

export function fetchAiGeneratedQuestionPage(params) {
  return request({
    url: 'ai/generated-questions/paging',
    method: 'get',
    params
  })
}

export function updateAiGeneratedQuestion(id, data) {
  return request({
    url: `ai/generated-questions/${id}`,
    method: 'put',
    data
  })
}

export function approveAiGeneratedQuestion(id, repoId) {
  return request({
    url: `ai/generated-questions/${id}/approve`,
    method: 'post',
    params: { repoId }
  })
}

export function rejectAiGeneratedQuestion(id) {
  return request({
    url: `ai/generated-questions/${id}`,
    method: 'delete'
  })
}
