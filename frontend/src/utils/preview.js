export function isPreviewMode() {
  return typeof window !== 'undefined' && window.location.hostname.endsWith('github.io')
}

export function getPreviewRole(username = '') {
  const normalized = String(username).trim().toLowerCase()
  if (normalized.includes('student')) {
    return { key: 'student', roleId: 1 }
  }
  if (normalized.includes('teacher')) {
    return { key: 'teacher', roleId: 2 }
  }
  return { key: 'admin', roleId: 3 }
}

function encodeBase64Url(value) {
  return btoa(unescape(encodeURIComponent(value)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
}

export function createPreviewUser(username = 'admin') {
  const role = getPreviewRole(username)
  return {
    id: role.roleId,
    username: username || role.key,
    realName: `Preview ${role.key}`,
    roleId: role.roleId,
    gradeId: 1,
    avatar: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MCIgaGVpZ2h0PSI4MCIgdmlld0JveD0iMCAwIDgwIDgwIj48cmVjdCB3aWR0aD0iODAiIGhlaWdodD0iODAiIHJ4PSIxNiIgZmlsbD0iIzQwOWVmZiIvPjx0ZXh0IHg9IjQwIiB5PSI0OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1zaXplPSIzMiIgZm9udC1mYW1pbHk9IkFyaWFsIiBmaWxsPSJ3aGl0ZSI+QTwvdGV4dD48L3N2Zz4='
  }
}

export function createPreviewToken(username = 'admin') {
  const header = { alg: 'none', typ: 'JWT' }
  const payload = {
    sub: 'github-pages-preview',
    exp: Math.floor(Date.now() / 1000) + 7 * 24 * 60 * 60,
    userInfo: JSON.stringify(createPreviewUser(username))
  }
  return `${encodeBase64Url(JSON.stringify(header))}.${encodeBase64Url(JSON.stringify(payload))}.`
}
