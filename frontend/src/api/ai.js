import request from '@/utils/request'

function postAi(url, input) {
  return request({
    url,
    method: 'post',
    data: { input: input || {} }
  })
}

export function generateStudentReport(input) {
  return postAi('ai/student-report', input)
}

export function generateStudentAiReport(params) {
  return request({
    url: 'ai/reports/student',
    method: 'post',
    params
  })
}

export function fetchStudentAiReportHistory(params) {
  return request({
    url: 'ai/reports/student/history',
    method: 'get',
    params
  })
}

export function regenerateStudentAiReport(id) {
  return request({
    url: `ai/reports/${id}/regenerate`,
    method: 'post'
  })
}

export function generateClassAiReport(params) {
  return request({
    url: 'ai/reports/class',
    method: 'post',
    params
  })
}

export function fetchClassAiReportHistory(params) {
  return request({
    url: 'ai/reports/class/history',
    method: 'get',
    params
  })
}

export function regenerateAiReport(id) {
  return request({
    url: `ai/reports/${id}/regenerate`,
    method: 'post'
  })
}

export function generateClassReport(input) {
  return postAi('ai/class-report', input)
}

export function generateTeachingPlan(input) {
  return postAi('ai/teaching-plan', input)
}

export function generatePaperReview(input) {
  return postAi('ai/paper-review', input)
}

export function generateQuestions(input) {
  return postAi('ai/questions', input)
}

export function recommendPractice(input) {
  return postAi('ai/practice-recommend', input)
}

export function evaluateLearningBehavior(input) {
  return postAi('ai/learning-behavior', input)
}
