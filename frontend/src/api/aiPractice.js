import request from '@/utils/request'

export function generatePracticeRecommendation(data) {
  return request({
    url: 'ai/practice-recommendations/generate',
    method: 'post',
    data
  })
}

export function publishPracticeRecommendation(data) {
  return request({
    url: 'ai/practice-recommendations/publish',
    method: 'post',
    data
  })
}
