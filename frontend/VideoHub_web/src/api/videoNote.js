import request from '@/utils/request'

// 用户记笔记
export const createVideoNote = (videoId, payload = {}) => {
  return request.post('/api/video-notes', {
    videoId,
    title: payload.title,
    content: payload.content,
    visibility: payload.visibility
  })
}

// 获取当前用户在某个作品下的历史笔记
export const fetchVideoNotesHistory = (videoId) => {
  return request.get('/api/video-notes/history', {
    params: { videoId }
  })
}

// 获取当前用户的全部笔记（跨视频）
export const fetchAllVideoNotesHistory = () => {
  return request.get('/api/video-notes/history/all')
}

// 获取单条笔记详情
export const fetchVideoNoteById = (noteId) => {
  return request.get(`/api/video-notes/${encodeURIComponent(noteId)}`)
}

// 更新笔记内容（仅作者可用）
export const updateVideoNote = (noteId, payload = {}) => {
  return request.put(`/api/video-notes/${encodeURIComponent(noteId)}`, {
    content: payload.content
  })
}

// 删除笔记（仅作者可用）
export const deleteVideoNote = (noteId) => {
  return request.delete(`/api/video-notes/${encodeURIComponent(noteId)}`)
}

