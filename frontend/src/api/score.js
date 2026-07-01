
import request from '@/utils/request'

export function scorePaging(params) {
  return request({
    url: 'score/paging',
    method: 'get',
    params
  })
}

export function getExamScore(params) {
  return request({
    url: 'score/getExamScore',
    method: 'get',
    params
  })
}

export function exportScores(examId, gradeId) {
  return request({
    url: `score/export/${examId}/${gradeId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function getStudentChapterAnalysis(params) {
  return request({
    url: 'score/analysis/student/chapter',
    method: 'get',
    params
  })
}

export function getStudentKnowledgeAnalysis(params) {
  return request({
    url: 'score/analysis/student/knowledge',
    method: 'get',
    params
  })
}

export function getClassChapterAnalysis(params) {
  return request({
    url: 'score/analysis/class/chapter',
    method: 'get',
    params
  })
}

export function getClassKnowledgeAnalysis(params) {
  return request({
    url: 'score/analysis/class/knowledge',
    method: 'get',
    params
  })
}

export function getWeakChapterRanking(params) {
  return request({
    url: 'score/analysis/weak-chapters',
    method: 'get',
    params
  })
}

export function getWeakKnowledgeRanking(params) {
  return request({
    url: 'score/analysis/weak-knowledge',
    method: 'get',
    params
  })
}

export function getWrongQuestionRanking(params) {
  return request({
    url: 'score/analysis/wrong-questions',
    method: 'get',
    params
  })
}

export function getDifficultyAnalysis(params) {
  return request({
    url: 'score/analysis/difficulty',
    method: 'get',
    params
  })
}

export function getClassScoreChart(params) {
  return request({
    url: 'score/analysis/class-scores',
    method: 'get',
    params
  })
}

export function getScoreSegmentChart(params) {
  return request({
    url: 'score/analysis/score-segments',
    method: 'get',
    params
  })
}

export function getStudentScoreTrend(params) {
  return request({
    url: 'score/analysis/student-score-trend',
    method: 'get',
    params
  })
}

