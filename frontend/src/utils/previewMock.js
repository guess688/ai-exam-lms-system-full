import { createPreviewToken, createPreviewUser } from './preview'

function page(records = []) {
  return {
    records,
    total: records.length,
    size: records.length || 10,
    current: 1,
    pages: 1
  }
}

function dailyData() {
  const today = new Date()
  return Array.from({ length: 15 }, (_, index) => {
    const date = new Date(today.getTime() - (14 - index) * 24 * 60 * 60 * 1000)
    return {
      loginDate: date.toISOString().slice(0, 10),
      totalSeconds: (index % 5) * 900
    }
  })
}

function resolvePreviewData(config) {
  const url = String(config.url || '').replace(/^\/+/, '')
  const method = String(config.method || 'get').toLowerCase()
  let body = config.data || {}
  if (typeof config.data === 'string') {
    try {
      body = JSON.parse(config.data || '{}')
    } catch (error) {
      body = {}
    }
  }

  if (url === 'auths/login') {
    return createPreviewToken(body.username || 'admin')
  }
  if (url === 'user/info') {
    return createPreviewUser()
  }
  if (url === 'stat/allCounts') {
    return { classCount: 6, questionCount: 128, examCount: 18 }
  }
  if (url === 'stat/student') {
    return [
      { gradeName: 'Class 1', totalStudent: 32 },
      { gradeName: 'Class 2', totalStudent: 28 },
      { gradeName: 'Class 3', totalStudent: 35 }
    ]
  }
  if (url === 'stat/exam') {
    return [
      { gradeName: 'Class 1', total: 5 },
      { gradeName: 'Class 2', total: 4 },
      { gradeName: 'Class 3', total: 6 }
    ]
  }
  if (url === 'stat/daily') {
    return dailyData()
  }
  if (url === 'notices/new') {
    return page([
      {
        title: 'GitHub Pages preview',
        content: 'This page is running with static preview data. Connect a public backend API for live data.',
        realName: 'Preview Admin',
        createTime: '2026-06-29 10:00:00'
      }
    ])
  }
  if (url === 'courses/my' || url === 'courses/paging' || url.endsWith('/paging')) {
    return page([])
  }
  if (url.includes('course-chapters') || url.includes('knowledge-points')) {
    return []
  }
  if (method === 'get') {
    return url.includes('paging') ? page([]) : null
  }
  return true
}

export function createPreviewAxiosResponse(config) {
  return Promise.resolve({
    data: {
      code: 1,
      msg: 'preview',
      data: resolvePreviewData(config)
    },
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
    request: {}
  })
}
