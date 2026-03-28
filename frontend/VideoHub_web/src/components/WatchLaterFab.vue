<template>
  <div class="wl-fab-slot">
    <Teleport to="body">
      <button
        ref="triggerRef"
        type="button"
        class="wl-fab"
        :class="{ 'is-open': panelOpen }"
        aria-label="稍后再看"
        :style="fabStyle"
        @click.stop="togglePanel"
      >
        <span class="wl-fab__icon-wrap">
          <svg
            class="wl-fab__icon"
            viewBox="0 0 24 24"
            width="20"
            height="20"
            aria-hidden="true"
            fill="currentColor"
          >
            <path
              d="M12 3.99976C7.58172 3.99976 4 7.58148 4 11.9998C4 16.418 7.58172 19.9998 12 19.9998C14.209 19.9998 16.2072 19.1058 17.656 17.6575C18.0465 17.2671 18.6797 17.2672 19.0702 17.6577C19.4606 18.0483 19.4605 18.6815 19.0699 19.072C17.2615 20.8798 14.7606 21.9998 12 21.9998C6.47715 21.9998 2 17.5226 2 11.9998C2 6.47691 6.47715 1.99976 12 1.99976C17.5228 1.99976 22 6.47691 22 11.9998C22 12.3745 21.9793 12.7449 21.939 13.1096C21.8783 13.6586 21.3841 14.0544 20.8352 13.9937C20.2863 13.933 19.8904 13.4388 19.9511 12.8899C19.9834 12.598 20 12.301 20 11.9998C20 7.58148 16.4183 3.99976 12 3.99976z"
            />
            <path
              d="M18.2929 10.7926C18.6834 10.4021 19.3166 10.4021 19.7071 10.7926L21 12.0855L22.2929 10.7926C22.6834 10.4021 23.3166 10.4021 23.7071 10.7926C24.0976 11.1832 24.0976 11.8163 23.7071 12.2069L21.8839 14.0301C21.3957 14.5182 20.6043 14.5182 20.1161 14.0301L18.2929 12.2069C17.9024 11.8163 17.9024 11.1832 18.2929 10.7926z"
            />
            <path
              d="M15.1659 11.0861C15.8694 11.4922 15.8694 12.5077 15.1659 12.9138L11.207 15.1995C10.5035 15.6056 9.62413 15.0979 9.62413 14.2856L9.62413 9.71432C9.62413 8.90197 10.5035 8.39425 11.207 8.80042L15.1659 11.0861z"
            />
          </svg>
        </span>
        <span class="wl-fab__text-wrap">
          <span class="wl-fab__text">稍后再看</span>
        </span>
        <span
          v-if="badgeCount > 0"
          class="wl-fab__badge"
          :class="{ 'wl-fab__badge--wide': badgeCount > 9 }"
        >{{ badgeCount > 99 ? '99+' : badgeCount }}</span>
      </button>

      <div
        v-show="panelOpen"
        ref="panelRef"
        class="wl-panel"
        :style="panelBoxStyle"
        role="dialog"
        aria-label="稍后再看列表"
        @click.stop
      >
        <div
          class="wl-panel__dragbar"
          title="拖动移动"
          @pointerdown="onDragPointerDown"
        >
          <span class="wl-panel__drag-title">稍后再看</span>
          <span class="wl-panel__bar-count">{{ currentIndex + 1 }}/{{ displayList.length || 1 }}</span>
          <a
            v-if="previewItem?.videoId"
            class="wl-panel__detail-link"
            href="#"
            @click.prevent="openVideoPage(previewItem.videoId)"
          >
            详情页观看 &gt;
          </a>
          <button
            type="button"
            class="wl-panel__close"
            title="关闭"
            aria-label="关闭稍后再看"
            @click.stop="closePanel"
          >
            ×
          </button>
        </div>

        <div class="wl-panel__body">
          <div class="wl-panel__preview">
            <div class="wl-panel__video-wrap">
              <video
                v-if="previewItem && previewItem.videoUrl"
                ref="previewVideoRef"
                class="wl-panel__video"
                :src="previewItem.videoUrl"
                :poster="previewItem.coverUrl"
                controls
                playsinline
                preload="metadata"
                @loadedmetadata="onPreviewMeta"
              />
              <div v-else class="wl-panel__preview-placeholder">
                <span>{{ previewItem ? '暂无预览' : '暂无视频' }}</span>
              </div>
            </div>
            <div class="wl-panel__preview-title">{{ previewItem?.title || '—' }}</div>
          </div>

          <div class="wl-panel__list">
            <div v-if="!userId" class="wl-panel__empty">登录后可使用稍后再看</div>
            <div v-else-if="loading" class="wl-panel__empty">加载中…</div>
            <div v-else-if="!displayList.length" class="wl-panel__empty">暂无视频，去首页添加吧</div>
            <div
              v-for="(row, idx) in displayList"
              v-else
              :key="row.id || row.videoId"
              class="wl-row"
              :class="{ active: currentIndex === idx }"
              @click="selectRow(idx)"
            >
              <div class="wl-row__thumb">
                <img :src="row.coverUrl || '/images/banner-1.jpg'" alt="" />
                <span class="wl-row__dur">{{ row.duration || '--:--' }}</span>
              </div>
              <div class="wl-row__meta">
                <div class="wl-row__title">{{ row.title }}</div>
                <div class="wl-row__up">
                  <img
                    class="wl-row__up-icon"
                    :src="upPbIconUrl"
                    alt=""
                    width="20"
                    height="20"
                  />
                  {{ row.uploaderName || '—' }}
                </div>
              </div>
              <button
                type="button"
                class="wl-row__rm"
                title="移除"
                @click.stop="removeRow(row)"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <div
          class="wl-panel__resize"
          title="拖动调整大小"
          @pointerdown.stop="onResizePointerDown"
        />
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useFloatingFabPosition } from '@/composables/useFloatingFabPosition'
import { useUserStore } from '@/stores/user'
import { useWatchLaterStore } from '@/stores/watchLater'
import { removeWatchLaterById } from '@/api/watchLater'

const props = defineProps({
  anchor: { type: Object, default: null },
  bottomOffsetPx: { type: Number, default: 160 }
})

const userStore = useUserStore()
const wlStore = useWatchLaterStore()

const baseUrl = (import.meta.env.BASE_URL || '/').replace(/\/?$/, '/')
const upPbIconUrl = `${baseUrl}assets/up_pb.svg`

const { fabLeftPx } = useFloatingFabPosition(() => props.anchor ?? null)

const panelOpen = ref(false)
const triggerRef = ref(null)
const panelRef = ref(null)
const previewVideoRef = ref(null)
const currentIndex = ref(0)

const userId = computed(() => userStore.user?.userId || userStore.user?.id || null)

const displayList = computed(() => wlStore.items || [])
const loading = computed(() => wlStore.loading)
const badgeCount = computed(() => wlStore.total || 0)

const previewItem = computed(() => {
  const list = displayList.value
  if (!list.length) return null
  const i = Math.min(Math.max(0, currentIndex.value), list.length - 1)
  return list[i]
})

const MIN_W = 300
const MIN_H = 320
const DEFAULT_W = 380
const DEFAULT_H = 520

const panelLayout = reactive({
  left: 80,
  top: 80,
  width: DEFAULT_W,
  height: DEFAULT_H,
  initialized: false
})

let dragSession = null
let resizeSession = null

function clampPanel() {
  const pad = 8
  const maxW = window.innerWidth - pad * 2
  const maxH = window.innerHeight - pad * 2
  panelLayout.width = Math.min(Math.max(MIN_W, panelLayout.width), maxW)
  panelLayout.height = Math.min(Math.max(MIN_H, panelLayout.height), maxH)
  panelLayout.left = Math.min(Math.max(pad, panelLayout.left), window.innerWidth - panelLayout.width - pad)
  panelLayout.top = Math.min(Math.max(pad, panelLayout.top), window.innerHeight - panelLayout.height - pad)
}

function initPanelPosition() {
  const fabL = fabLeftPx.value ?? window.innerWidth - 56
  const safe = 16
  const fabBottomGap = safe + props.bottomOffsetPx
  const pw = Math.min(panelLayout.width, window.innerWidth - 16)
  const ph = Math.min(panelLayout.height, window.innerHeight - 16)
  panelLayout.width = pw
  panelLayout.height = ph
  const left = Math.max(8, fabL - 10 - pw)
  const top = Math.max(8, window.innerHeight - fabBottomGap - ph)
  panelLayout.left = left
  panelLayout.top = top
  clampPanel()
  panelLayout.initialized = true
}

const panelBoxStyle = computed(() => ({
  position: 'fixed',
  left: `${panelLayout.left}px`,
  top: `${panelLayout.top}px`,
  width: `${panelLayout.width}px`,
  height: `${panelLayout.height}px`,
  zIndex: 2100
}))

watch(
  () => [panelOpen.value, previewItem.value?.videoId],
  async () => {
    await nextTick()
    const v = previewVideoRef.value
    if (!v || !panelOpen.value) return
    try {
      v.muted = true
      await v.play?.().catch(() => {})
    } catch {
      /* 浏览器策略可能阻止自动播放，用户可通过控件播放 */
    }
  }
)

function onPreviewMeta() {
  const v = previewVideoRef.value
  if (!v || !panelOpen.value) return
  v.muted = true
  v.play?.().catch(() => {})
}

watch(userId, (id) => {
  if (id) wlStore.load(id)
})

watch(
  () => displayList.value.length,
  (n) => {
    if (currentIndex.value >= n) currentIndex.value = Math.max(0, n - 1)
  }
)

const fabStyle = computed(() => {
  const baseBottom = `max(16px, env(safe-area-inset-bottom, 0px))`
  const off = props.bottomOffsetPx
  if (fabLeftPx.value != null) {
    return {
      left: `${fabLeftPx.value}px`,
      right: 'auto',
      bottom: `calc(${baseBottom} + ${off}px)`
    }
  }
  return {
    right: 'max(16px, env(safe-area-inset-right, 0px))',
    bottom: `calc(${baseBottom} + ${off}px)`
  }
})

/**
 * 与首页 openInNewTab 一致：微前端下必须用 window.__MICRO_APP_BASE_ROUTE__，
 * 否则 window.open 会打开宿主根路径的 /video/...，易被重定向到主页。
 */
function resolvePublicAppBase() {
  const micro =
    typeof window !== 'undefined' ? window.__MICRO_APP_BASE_ROUTE__ || '' : ''
  if (micro) return String(micro).replace(/\/$/, '')
  return String(import.meta.env.BASE_URL || '/').replace(/\/$/, '')
}

function openVideoPage(videoId) {
  if (!videoId) return
  const base = resolvePublicAppBase()
  const path = `/video/${encodeURIComponent(videoId)}`
  const url = `${base}${path}`
  window.open(url, '_blank')
}

function selectRow(idx) {
  if (idx < 0 || idx >= displayList.value.length) return
  currentIndex.value = idx
}

function closePanel() {
  panelOpen.value = false
}

async function togglePanel() {
  if (!userId.value) {
    ElMessage.warning('请先登录后再使用稍后再看')
    return
  }
  panelOpen.value = !panelOpen.value
  if (panelOpen.value) {
    await wlStore.load(userId.value)
    currentIndex.value = 0
    await nextTick()
    if (!panelLayout.initialized) {
      initPanelPosition()
    } else {
      clampPanel()
    }
  }
}

async function removeRow(row) {
  if (!userId.value || !row?.id) return
  try {
    const { data } = await removeWatchLaterById(userId.value, row.id)
    if (data?.success) {
      await wlStore.load(userId.value)
      ElMessage.success('已移除')
    }
  } catch {
    ElMessage.error('移除失败')
  }
}

function onDragPointerDown(e) {
  if (e.button !== 0) return
  if (e.target.closest('a')) return
  if (e.target.closest('.wl-panel__close')) return
  e.preventDefault()
  dragSession = {
    startX: e.clientX,
    startY: e.clientY,
    origLeft: panelLayout.left,
    origTop: panelLayout.top
  }
  window.addEventListener('pointermove', onDragPointerMove)
  window.addEventListener('pointerup', onDragPointerUp, { once: true })
  window.addEventListener('pointercancel', onDragPointerUp, { once: true })
}

function onDragPointerMove(e) {
  if (!dragSession) return
  const dx = e.clientX - dragSession.startX
  const dy = e.clientY - dragSession.startY
  panelLayout.left = dragSession.origLeft + dx
  panelLayout.top = dragSession.origTop + dy
  clampPanel()
}

function onDragPointerUp() {
  dragSession = null
  window.removeEventListener('pointermove', onDragPointerMove)
}

function onResizePointerDown(e) {
  if (e.button !== 0) return
  e.preventDefault()
  e.stopPropagation()
  resizeSession = {
    startX: e.clientX,
    startY: e.clientY,
    origW: panelLayout.width,
    origH: panelLayout.height,
    origLeft: panelLayout.left,
    origTop: panelLayout.top
  }
  window.addEventListener('pointermove', onResizePointerMove)
  window.addEventListener('pointerup', onResizePointerUp, { once: true })
  window.addEventListener('pointercancel', onResizePointerUp, { once: true })
}

function onResizePointerMove(e) {
  if (!resizeSession) return
  const dx = e.clientX - resizeSession.startX
  const dy = e.clientY - resizeSession.startY
  let nw = resizeSession.origW + dx
  let nh = resizeSession.origH + dy
  nw = Math.min(Math.max(MIN_W, nw), window.innerWidth - 8 - panelLayout.left)
  nh = Math.min(Math.max(MIN_H, nh), window.innerHeight - 8 - panelLayout.top)
  panelLayout.width = nw
  panelLayout.height = nh
  clampPanel()
}

function onResizePointerUp() {
  resizeSession = null
  window.removeEventListener('pointermove', onResizePointerMove)
}

function onWinResize() {
  if (panelOpen.value) clampPanel()
}

function onDocClick(e) {
  if (!panelOpen.value) return
  const t = e.target
  if (triggerRef.value?.contains(t)) return
  if (panelRef.value?.contains(t)) return
  panelOpen.value = false
}

onMounted(() => {
  document.addEventListener('click', onDocClick, true)
  window.addEventListener('resize', onWinResize)
  if (userId.value) wlStore.load(userId.value)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick, true)
  window.removeEventListener('resize', onWinResize)
  window.removeEventListener('pointermove', onDragPointerMove)
  window.removeEventListener('pointermove', onResizePointerMove)
})
</script>

<style scoped lang="scss">
.wl-fab-slot {
  width: 0;
  height: 0;
  overflow: hidden;
  position: absolute;
  inset: auto auto 0 0;
  pointer-events: none;
}

/* 边长与其它 FAB（返回顶部 / 刷新）宽度 40px 一致；内边距对称使图标居中 */
.wl-fab {
  --wl-fab-icon: 20px;
  --wl-fab-side: 40px;
  --wl-fab-inset: calc((var(--wl-fab-side) - var(--wl-fab-icon)) / 2);

  pointer-events: auto;
  position: fixed;
  z-index: 122;
  overflow: visible;
  box-sizing: border-box;
  margin: 0;
  padding: var(--wl-fab-inset);
  width: var(--wl-fab-side);
  min-width: var(--wl-fab-side);
  height: var(--wl-fab-side);
  min-height: var(--wl-fab-side);
  display: flex;
  flex-direction: row;
  align-items: center;
  /* 图标在左固定，文字向右展开 */
  justify-content: flex-start;
  gap: 0;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: #fff;
  color: #18191c;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    transition:
    width 0.22s ease,
    min-width 0.22s ease,
    height 0.22s ease,
    min-height 0.22s ease,
    padding 0.22s ease,
    background 0.15s ease,
    border-color 0.15s ease,
    box-shadow 0.15s ease;

  &:hover {
    background: #fafafa;
    border-color: #d0d0d0;
  }

  &:hover,
  &:focus-visible,
  &.is-open {
    width: 108px;
    min-width: 108px;
    height: var(--wl-fab-side);
    min-height: var(--wl-fab-side);
    padding: var(--wl-fab-inset) 10px;
    gap: 6px;
  }

  &.is-open {
    border-color: #00a1d6;
    color: #00a1d6;
  }

  .wl-fab__text-wrap {
    max-width: 0;
    min-width: 0;
    opacity: 0;
    overflow: hidden;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    transition:
      max-width 0.22s ease,
      opacity 0.15s ease;
  }

  &:hover .wl-fab__text-wrap,
  &:focus-visible .wl-fab__text-wrap,
  &.is-open .wl-fab__text-wrap {
    max-width: 72px;
    opacity: 1;
  }

  .wl-fab__text {
    font-size: 12px;
    font-weight: 500;
    line-height: 1.2;
    letter-spacing: 0.02em;
    user-select: none;
    white-space: nowrap;
  }

  .wl-fab__icon-wrap {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .wl-fab__icon {
    display: block;
    flex-shrink: 0;
  }

  .wl-fab__badge {
    position: absolute;
    top: -6px;
    right: -6px;
    box-sizing: border-box;
    width: 18px;
    height: 18px;
    padding: 0;
    border-radius: 50%;
    background: #fb7299;
    color: #fff;
    font-size: 10px;
    line-height: 18px;
    text-align: center;
    box-shadow: 0 0 0 1px #fff;
    display: flex;
    align-items: center;
    justify-content: center;

    &.wl-fab__badge--wide {
      width: auto;
      min-width: 18px;
      height: 18px;
      padding: 0 5px;
      border-radius: 9px;
    }
  }
}
</style>

<style scoped lang="scss">
.wl-panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid #eee;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  user-select: none;
}

.wl-panel__dragbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: #f7f8fa;
  border-bottom: 1px solid #eee;
  cursor: grab;
  touch-action: none;

  &:active {
    cursor: grabbing;
  }
}

.wl-panel__drag-title {
  font-size: 14px;
  font-weight: 600;
  color: #222;
}

.wl-panel__bar-count {
  font-size: 12px;
  color: #9499a0;
  margin-right: auto;
}

.wl-panel__detail-link {
  font-size: 12px;
  color: #00a1d6;
  text-decoration: none;
  padding: 4px 10px;
  border: 1px solid #e3e5e7;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;

  &:hover {
    background: #f6f7f8;
  }
}

.wl-panel__close {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #9499a0;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  user-select: none;

  &:hover {
    background: rgba(0, 0, 0, 0.06);
    color: #222;
  }
}

.wl-panel__body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.wl-panel__preview {
  flex-shrink: 0;
  background: #000;
  border-bottom: 1px solid #eee;
}

.wl-panel__video-wrap {
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #111;
}

.wl-panel__video {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  vertical-align: top;
}

.wl-panel__preview-placeholder {
  aspect-ratio: 16 / 9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 13px;
  background: #222;
}

.wl-panel__preview-title {
  padding: 6px 10px 8px;
  font-size: 12px;
  font-weight: 600;
  color: #333;
  background: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.wl-panel__list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  user-select: text;
  scrollbar-width: thin;
  scrollbar-color: #c5c8ce transparent;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
    margin: 4px 0;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 100px;
  }

  &:hover {
    scrollbar-color: #a8adb8 transparent;
  }

  &:hover::-webkit-scrollbar-thumb {
    background: #c5c8ce;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8adb8;
  }
}

.wl-panel__empty {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  color: #9499a0;
}

.wl-panel__resize {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 18px;
  height: 18px;
  cursor: nwse-resize;
  background: transparent;
  z-index: 2;

  &::after {
    content: '';
    position: absolute;
    right: 4px;
    bottom: 4px;
    width: 10px;
    height: 10px;
    background: linear-gradient(
      135deg,
      transparent 45%,
      #c9ccd0 45%,
      #c9ccd0 48%,
      transparent 48%,
      transparent 52%,
      #c9ccd0 52%,
      #c9ccd0 55%,
      transparent 55%
    );
    opacity: 0.85;
    pointer-events: none;
  }
}

.wl-row {
  display: flex;
  gap: 10px;
  padding: 8px 10px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  position: relative;
  align-items: flex-start;

  &:hover,
  &.active {
    background: #f6f7f8;
  }
}

.wl-row__thumb {
  position: relative;
  width: 120px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #eee;

  img {
    width: 100%;
    aspect-ratio: 16 / 9;
    object-fit: cover;
    display: block;
  }
}

.wl-row__dur {
  position: absolute;
  right: 4px;
  bottom: 4px;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  padding: 1px 5px;
  border-radius: 3px;
}

.wl-row__meta {
  flex: 1;
  min-width: 0;
}

.wl-row__title {
  font-size: 13px;
  color: #008ac5;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.wl-row__up {
  margin-top: 6px;
  font-size: 12px;
  color: #9499a0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.wl-row__up-icon {
  flex-shrink: 0;
  display: block;
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.wl-row__rm {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 4px;
  cursor: pointer;
  line-height: 1;
  font-size: 16px;
  color: #666;

  &:hover {
    background: rgba(0, 0, 0, 0.1);
  }
}
</style>
