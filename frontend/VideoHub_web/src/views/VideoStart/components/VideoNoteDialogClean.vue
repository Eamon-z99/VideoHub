<script setup>
import { ElMessage } from 'element-plus'
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { createVideoNote, fetchAllVideoNotesHistory, updateVideoNote } from '@/api/videoNote'
import { fetchVideoDetail } from '@/api/video'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  videoId: { type: String, required: true },
})

const emit = defineEmits(['update:modelValue', 'submitted'])

const userStore = useUserStore()
const router = useRouter()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const quillContent = ref('')
const submitting = ref(false)
const formError = ref('')

// editor | edit | history
const historyMode = ref('editor')
const historyLoading = ref(false)
const historyList = ref([])
const selectedHistoryNote = ref(null)

const noteTextareaRef = ref(null)
const dialogInlineStyle = ref({})

const NOTE_DIALOG_WIDTH = 360
const NOTE_DIALOG_GAP = 14

// 封面悬浮播放：鼠标移入封面时拉取 playUrl，并在封面位置覆盖 <video> 预览
const hoverNoteId = ref(null)
const hoverPlayUrlById = ref({})
const hoverPlayUrlLoading = ref({})
// v-for 下用函数 ref 收集每个 note 的 video 元素，保证同一 videoId 的多条笔记不会同时播放
const hoverPreviewVideoElByNoteId = ref({})

const ensureHoverPlayUrl = async (videoId) => {
  if (!videoId) return ''
  if (hoverPlayUrlById.value[videoId]) return hoverPlayUrlById.value[videoId]
  if (hoverPlayUrlLoading.value[videoId]) return ''

  hoverPlayUrlLoading.value = { ...hoverPlayUrlLoading.value, [videoId]: true }
  try {
    const { data } = await fetchVideoDetail(videoId)
    const playUrl = data?.playUrl || ''
    hoverPlayUrlById.value = { ...hoverPlayUrlById.value, [videoId]: playUrl }
    return playUrl
  } catch {
    return ''
  } finally {
    hoverPlayUrlLoading.value = { ...hoverPlayUrlLoading.value, [videoId]: false }
  }
}

const setHoverPreviewVideoEl = (noteId, el) => {
  if (!noteId) return
  const next = { ...hoverPreviewVideoElByNoteId.value }
  if (el) next[noteId] = el
  else delete next[noteId]
  hoverPreviewVideoElByNoteId.value = next
}

const pausePreviewByNoteId = (noteId) => {
  if (!noteId) return
  const v = hoverPreviewVideoElByNoteId.value?.[noteId]
  if (!v) return
  try {
    v.pause?.()
    // 让下一次 hover 从头更自然（避免停在中间帧）
    v.currentTime = 0
  } catch {}
}

const onCoverMouseEnter = async (noteId, videoId) => {
  // 切换 hover 时，先停掉上一个（避免同 videoId 多条笔记同时播放）
  if (hoverNoteId.value && hoverNoteId.value !== noteId) {
    pausePreviewByNoteId(hoverNoteId.value)
  }
  hoverNoteId.value = noteId
  void ensureHoverPlayUrl(videoId)

  // 让新的 video DOM 渲染出来后再尝试播放
  void nextTick().then(() => {
    const v = hoverPreviewVideoElByNoteId.value?.[noteId]
    if (!v) return
    try {
      v.muted = true
      v.play?.().catch(() => {})
    } catch {}
  })
}

const onCoverMouseLeave = (noteId) => {
  pausePreviewByNoteId(noteId)
  if (hoverNoteId.value === noteId) hoverNoteId.value = null
}

function stripHtml(html) {
  if (html == null) return ''
  return String(html).replace(/<(.|\\n)*?>/g, '')
}

function getNoteSnippet(html) {
  const plain = stripHtml(html).trim()
  if (!plain) return '（无内容）'
  if (plain.length <= 90) return plain
  return `${plain.slice(0, 90)}...`
}

function reset() {
  quillContent.value = ''
  formError.value = ''
  historyMode.value = 'editor'
  historyLoading.value = false
  historyList.value = []
  selectedHistoryNote.value = null
}

function updateDialogPosition() {
  const playerEl = document.querySelector('.player-shell .player')
  const videoPageEl = document.querySelector('.video-page')
  if (!playerEl || !videoPageEl) return

  const playerRect = playerEl.getBoundingClientRect()
  const videoPageRect = videoPageEl.getBoundingClientRect()

  const desiredHeight = playerRect.height

  let left = playerRect.right + NOTE_DIALOG_GAP
  const maxLeft = videoPageRect.right - NOTE_DIALOG_WIDTH - 8
  left = Math.min(left, maxLeft)
  left = Math.max(left, videoPageRect.left + 8)

  const maxHeight = window.innerHeight - 16
  let height = Math.min(desiredHeight || 520, maxHeight)
  height = Math.max(280, height)

  let top = playerRect.top + 6
  const maxTop = window.innerHeight - height - 8
  top = Math.min(top, maxTop)
  top = Math.max(top, 8)

  dialogInlineStyle.value = {
    position: 'fixed',
    left: `${left}px`,
    top: `${top}px`,
    width: `${NOTE_DIALOG_WIDTH}px`,
    height: `${height}px`,
    margin: '0',
    transform: 'none',
    zIndex: 3000,
  }
}

async function fetchHistory() {
  if (!userStore.isAuthenticated) return
  historyLoading.value = true
  try {
    const resp = await fetchAllVideoNotesHistory()
    if (resp?.data?.success) {
      historyList.value = resp.data.data?.list || []
    } else {
      historyList.value = []
    }
  } catch (e) {
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

function openHistory() {
  historyMode.value = 'history'
  selectedHistoryNote.value = null
  void fetchHistory()
}

function openEditor() {
  historyMode.value = 'editor'
  selectedHistoryNote.value = null
  formError.value = ''
  quillContent.value = ''
  void nextTick().then(() => noteTextareaRef.value?.focus?.())
}

function openEdit(note) {
  selectedHistoryNote.value = note
  historyMode.value = 'edit'
  formError.value = ''
  quillContent.value = stripHtml(note?.content || '')
  void nextTick().then(() => noteTextareaRef.value?.focus?.())
}

function onClose() {
  visible.value = false
}

function clear() {
  quillContent.value = ''
  formError.value = ''
  void nextTick().then(() => noteTextareaRef.value?.focus?.())
}

function applyAroundSelection(left, right) {
  const textarea = noteTextareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart ?? 0
  const end = textarea.selectionEnd ?? 0
  const val = quillContent.value || ''
  const selected = val.slice(start, end)

  if (start === end) {
    const insert = `${left}${right}`
    quillContent.value = val.slice(0, start) + insert + val.slice(end)
    requestAnimationFrame(() => {
      const pos = start + left.length
      textarea.setSelectionRange(pos, pos)
      textarea.focus()
    })
    return
  }

  quillContent.value = val.slice(0, start) + left + selected + right + val.slice(end)
  requestAnimationFrame(() => {
    const nextStart = start + left.length
    const nextEnd = nextStart + selected.length
    textarea.setSelectionRange(nextStart, nextEnd)
    textarea.focus()
  })
}

function applyPrefix(prefix) {
  const textarea = noteTextareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart ?? 0
  const end = textarea.selectionEnd ?? 0
  const val = quillContent.value || ''
  const selected = val.slice(start, end)

  const lines = (selected || '').split('\n')
  const prefixed = lines.map((l) => (l.trim() ? `${prefix}${l}` : l)).join('\n')
  quillContent.value = val.slice(0, start) + prefixed + val.slice(end)
  requestAnimationFrame(() => textarea.focus())
}

function applyLink() {
  const textarea = noteTextareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart ?? 0
  const end = textarea.selectionEnd ?? 0
  const val = quillContent.value || ''
  const selected = val.slice(start, end) || ''

  const insert = `[${selected}](https://)`
  quillContent.value = val.slice(0, start) + insert + val.slice(end)
  requestAnimationFrame(() => textarea.focus())
}

function toolbarBold() {
  applyAroundSelection('**', '**')
}
function toolbarItalic() {
  applyAroundSelection('*', '*')
}
function toolbarStrike() {
  applyAroundSelection('~~', '~~')
}
function toolbarQuote() {
  applyPrefix('> ')
}
function toolbarCode() {
  applyAroundSelection('`', '`')
}
function toolbarLink() {
  applyLink()
}

function goToVideo(videoId) {
  if (!videoId) return
  router.push({ name: 'video', params: { id: videoId } })
}

async function submit() {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  const content = quillContent.value
  if (!content || content.replace(/<(.|\\n)*?>/g, '').trim().length === 0) {
    ElMessage.warning('请输入笔记内容')
    return
  }

  try {
    submitting.value = true
    formError.value = ''

    if (historyMode.value === 'edit' && selectedHistoryNote.value?.id) {
      const resp = await updateVideoNote(selectedHistoryNote.value.id, { content })
      if (resp?.data?.success) {
        ElMessage.success('笔记保存成功')
        emit('submitted')
        selectedHistoryNote.value = null
        await fetchHistory()
        historyMode.value = 'history'
      } else {
        formError.value = resp?.data?.message || '笔记保存失败'
        ElMessage.error(formError.value)
      }
      return
    }

    const resp = await createVideoNote(props.videoId, { title: '笔记', content, visibility: 1 })
    if (resp?.data?.success) {
      ElMessage.success('笔记提交成功')
      emit('submitted')
      await fetchHistory()
      selectedHistoryNote.value = null
      historyMode.value = 'history'
    } else {
      formError.value = resp?.data?.message || '笔记提交失败'
      ElMessage.error(formError.value)
    }
  } catch (e) {
    formError.value = e?.response?.data?.message || e?.message || '笔记提交失败'
    ElMessage.error(formError.value)
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      reset()
      void fetchHistory()
      void nextTick().then(updateDialogPosition)
      window.addEventListener('resize', updateDialogPosition)
      window.addEventListener('scroll', updateDialogPosition, { passive: true })
    } else {
      reset()
      window.removeEventListener('resize', updateDialogPosition)
      window.removeEventListener('scroll', updateDialogPosition)
    }
  },
)

onUnmounted(() => {
  window.removeEventListener('resize', updateDialogPosition)
  window.removeEventListener('scroll', updateDialogPosition)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="video-note-floating" :style="dialogInlineStyle" role="dialog" aria-modal="false">
      <!-- 编辑/编辑已有笔记 -->
      <div v-if="historyMode === 'editor' || historyMode === 'edit'" class="note-modal-header">
        <button class="note-icon-btn" type="button" aria-label="返回" @click="openHistory">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>

        <div class="note-public-title">笔记</div>

        <div class="note-header-right">
          <button class="note-icon-btn" type="button" aria-label="清空" @click="clear">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 6h18" />
              <path d="M8 6V4h8v2" />
              <path d="M6 6l1 16h10l1-16" />
              <path d="M10 11v6" />
              <path d="M14 11v6" />
            </svg>
          </button>

          <button class="note-icon-btn note-close-btn" type="button" aria-label="关闭" @click="onClose">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18" />
              <path d="M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 历史页 -->
      <div v-else class="note-modal-header note-history-header">
        <div class="note-history-title">笔记</div>
        <button class="note-icon-btn note-close-btn" type="button" aria-label="关闭" @click="onClose">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18" />
            <path d="M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="note-modal-body">
        <!-- 编辑页 -->
        <div v-if="historyMode === 'editor' || historyMode === 'edit'" class="note-editor-mode">
          <div class="note-editor-body">
            <div class="note-toolbar">
              <button class="note-tool-btn" type="button" @click="toolbarBold">B</button>
              <button class="note-tool-btn" type="button" @click="toolbarItalic">A</button>
              <button class="note-tool-btn" type="button" @click="toolbarStrike">—</button>
              <button class="note-tool-btn" type="button" @click="toolbarQuote">☷</button>
              <button class="note-tool-btn" type="button" @click="toolbarCode">▢</button>
              <button class="note-tool-btn" type="button" @click="toolbarLink">⇩</button>
            </div>

            <textarea
              ref="noteTextareaRef"
              v-model="quillContent"
              class="note-textarea"
              placeholder="输入笔记内容..."
              spellcheck="false"
            ></textarea>

            <div v-if="formError" class="note-error">{{ formError }}</div>
          </div>
        </div>

        <!-- 历史页 -->
        <div v-else class="note-history-mode">
          <button class="note-new-btn" type="button" @click="openEditor" :disabled="historyLoading">
            <span class="note-new-icon">✎</span>
            记笔记
          </button>

          <div class="note-divider" />

          <div class="history-empty" v-if="!historyLoading && historyList.length === 0">
            还没有人发布笔记呢，快去发布一篇吧~
          </div>

          <div v-else-if="historyLoading" class="history-loading">加载中...</div>

          <div v-else class="history-list">
            <div
              v-for="n in historyList"
              :key="n.id"
              class="history-item"
              :class="{ active: selectedHistoryNote?.id === n.id }"
              @click="openEdit(n)"
            >
              <div class="history-video-row">
                <div
                  v-if="n.videoCoverUrl"
                  class="history-cover-wrap"
                  @click.stop="goToVideo(n.videoId)"
                  @mouseenter="onCoverMouseEnter(n.id, n.videoId)"
                  @mouseleave="onCoverMouseLeave(n.id)"
                >
                  <img v-if="hoverNoteId !== n.id" :src="n.videoCoverUrl" class="history-video-cover" alt="" />
                  <video
                    v-if="hoverNoteId === n.id && hoverPlayUrlById[n.videoId]"
                    class="history-video-cover history-video-cover-video"
                    :src="hoverPlayUrlById[n.videoId]"
                    :poster="n.videoCoverUrl"
                    :ref="(el) => setHoverPreviewVideoEl(n.id, el)"
                    muted
                    autoplay
                    loop
                    playsinline
                    preload="metadata"
                    disablePictureInPicture
                    disableRemotePlayback
                    controlsList="nodownload noplaybackrate nofullscreen noremoteplayback"
                    @contextmenu.prevent
                    @dblclick.prevent
                  />
                  <div class="history-play-overlay" aria-hidden="true">
                    <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg" fill="currentColor">
                      <path d="M7 4.5C7 3.74212 7.83504 3.29474 8.47022 3.70269L14.0108 7.20269C14.6032 7.58433 14.6032 8.41567 14.0108 8.79731L8.47022 12.2973C7.83504 12.7053 7 12.2579 7 11.5V4.5Z" />
                    </svg>
                  </div>
                </div>

                <div class="history-video-meta">
                  <div class="history-video-title" @click.stop="goToVideo(n.videoId)">
                    {{ n.videoTitle || '-' }}
                  </div>
                  <div class="history-note-snippet">{{ getNoteSnippet(n.content) }}</div>
                </div>
              </div>

              <div class="history-time">{{ n.createdTime || '' }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="historyMode === 'editor' || historyMode === 'edit'" class="note-modal-footer">
        <button class="note-submit-btn" type="button" :disabled="submitting" @click="submit">
          {{ historyMode === 'edit' ? '保存' : '提交' }}
        </button>
      </div>
    </div>
  </Teleport>
</template>

<style>
.video-note-floating {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.16);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  resize: both;
  pointer-events: auto;
  min-width: 320px;
  min-height: 280px;
  box-sizing: border-box;
}

.note-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #eef2f7;
}

.note-history-header {
  padding-right: 6px;
}

.note-history-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.note-public-title {
  flex: 1;
  text-align: center;
  font-weight: 800;
  color: #0f172a;
  font-size: 18px;
  user-select: none;
}

.note-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.note-icon-btn {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  background: #ffffff;
  color: #0284c7;
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(2, 6, 23, 0.08);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.note-icon-btn:hover {
  background: #eff6ff;
  border-color: #0284c7;
}

.note-close-btn {
  background: #ffffff;
  border-color: #dbeafe;
  color: #0284c7;
  box-shadow: 0 8px 22px rgba(2, 6, 23, 0.08);
}

.note-modal-body {
  flex: 1;
  overflow: hidden;
  padding: 12px 12px 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.note-editor-mode,
.note-history-mode {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.note-editor-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.note-toolbar {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 8px 10px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  background: #f8fafc;
  margin-bottom: 10px;
  flex: 0 0 auto;
}

.note-tool-btn {
  border: none;
  background: transparent;
  padding: 0;
  margin: 0;
  cursor: pointer;
  font-weight: 700;
  color: #6b7280;
  user-select: none;
  font-size: 14px;
  line-height: 1;
}

.note-tool-btn:hover {
  color: #00a1d6;
}

.note-textarea {
  flex: 1 1 auto;
  min-height: 0;
  width: 100%;
  resize: none;
  overflow: auto;
  padding: 12px;
  border: 1px solid #e5e7eb !important;
  border-radius: 8px;
  background: #ffffff;
  color: #111827;
  caret-color: #111827;
  box-sizing: border-box;
  outline: none;
}

.note-textarea::placeholder {
  color: #9ca3af;
}

.note-textarea:focus,
.note-textarea:focus-visible {
  outline: none;
  border-color: #e5e7eb !important;
  box-shadow: none !important;
}

.note-textarea:hover {
  border-color: #e5e7eb !important;
}

.note-error {
  margin-top: 10px;
  color: #dc2626;
  font-size: 13px;
}

.note-modal-footer {
  padding: 12px 12px 10px; /* 关键：输入区底部 -> 按钮顶部 12px */
  display: flex;
  justify-content: flex-end;
}

.note-submit-btn {
  height: 42px;
  padding: 0 18px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 700;
}

.note-submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.note-new-btn {
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 10px;
  border: 1px solid #00a1d6;
  background: #00a1d6;
  color: #ffffff;
  cursor: pointer;
}

.note-new-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.note-new-icon {
  font-size: 16px;
  line-height: 1;
}

.note-divider {
  height: 1px;
  background: #eef2f7;
  margin: 10px 0;
}

.history-empty,
.history-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 14px;
}

.history-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding-right: 6px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.55) rgba(0, 0, 0, 0);
}

.history-list::-webkit-scrollbar {
  width: 10px;
}

.history-list::-webkit-scrollbar-track {
  background: transparent;
}

.history-list::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.38);
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.85);
}

.history-list::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.6);
}

.history-item {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  background: #fff;
  padding: 12px 14px;
  cursor: pointer;
  margin-bottom: 10px;
}

.history-item.active {
  border-color: #00a1d6;
}

.history-video-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.history-cover-wrap {
  position: relative;
  width: 112px;
  height: 64px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  flex: 0 0 auto;
  background: #f3f4f6;
}

.history-video-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.history-video-cover-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.history-play-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 34px;
  height: 34px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.history-video-meta {
  flex: 1;
  min-width: 0;
}

.history-video-title {
  font-weight: 700;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.history-video-title:hover {
  color: #00aeec !important;
}

.history-note-snippet {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.history-time {
  margin-top: 6px;
  color: #9ca3af;
  font-size: 12px;
}
</style>

