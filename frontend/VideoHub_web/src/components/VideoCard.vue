<template>
  <div class="video-card">
    <div
      ref="thumbOuterRef"
      class="thumb-outer"
      @mouseenter="onThumbEnter"
      @mouseleave="onThumbLeave"
    >
      <div class="thumb-wrap">
      <video
        v-if="hoverPreview && thumbHover && displayPlayUrl"
        ref="thumbVideoRef"
        :src="displayPlayUrl"
        class="thumb-video"
        :poster="video.cover"
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
        @timeupdate="onThumbVideoTimeUpdate"
        @loadedmetadata="onThumbVideoLoadedMeta"
      />
      <img
        v-else
        :src="coverSrc"
        :loading="lazyCover ? 'eager' : 'lazy'"
        decoding="async"
        @error="onImgError && onImgError($event)"
      />
      <div v-if="!isPreviewPlaying" class="bottom-gradient"></div>
      <div v-if="!isPreviewPlaying" class="stats-overlay">
        <span class="play-count">
          <svg class="play-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
            <path d="M747.52 893.44H276.48a225.536 225.536 0 0 1-225.28-225.28v-312.32a225.536 225.536 0 0 1 225.28-225.28h471.04a225.536 225.536 0 0 1 225.28 225.28v312.32a225.536 225.536 0 0 1-225.28 225.28z m-471.04-680.96a143.36 143.36 0 0 0-143.36 143.36v312.32a143.36 143.36 0 0 0 143.36 143.36h471.04a143.36 143.36 0 0 0 143.36-143.36v-312.32a143.36 143.36 0 0 0-143.36-143.36z" fill="#FFFFFF"/>
            <path d="M449.4336 692.736a40.96 40.96 0 0 1-40.96-40.96l-1.536-279.5008a40.96 40.96 0 0 1 61.44-35.84l242.8416 138.24a40.96 40.96 0 0 1 0.3584 70.9632L470.0672 687.104a40.96 40.96 0 0 1-20.6336 5.632z m39.7824-249.7536l0.768 137.6256 118.784-69.4784z" fill="#FFFFFF"/>
          </svg>
          {{ formatPlayCount(video.playCount) }}
        </span>
        <span v-if="video.commentCount != null && video.commentCount !== ''" class="comment-count">
          <svg class="comment-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect
              x="4"
              y="5"
              width="16"
              height="14"
              rx="2"
              stroke="currentColor"
              stroke-width="1.6"
            />
            <path
              d="M7.5 9.5h9M7.5 12h6M7.5 14.5h9"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
          {{ formatPlayCount(video.commentCount) }}
        </span>
      </div>
      <span class="duration">{{ durationOverlayText }}</span>
      <div v-if="!isPreviewPlaying && video.isVideo" class="play-overlay">
        <div class="play-button">▶</div>
      </div>
    </div>

      <button
        v-show="thumbHover"
        type="button"
        class="wl-add"
        :class="{ 'is-added': inWatchLater }"
        title="稍后再看"
        @click.stop="onWatchLaterClick"
      >
        <span class="wl-add__icon" aria-hidden="true">
          <svg
            v-if="inWatchLater"
            class="wl-add__svg"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            width="18"
            height="18"
            fill="currentColor"
          >
            <path
              d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"
            />
          </svg>
          <svg
            v-else
            class="wl-add__svg"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            width="18"
            height="18"
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
        <span class="wl-add__hint">{{ inWatchLater ? '已添加稍后再看' : '添加至稍后再看' }}</span>
      </button>
    </div>

    <div class="v-title" :title="video.title">
      {{ video.title }}
    </div>

    <div class="uploader-row">
      <span v-if="showLikeBadge" class="like-badge">{{ likeBadgeText }}</span>
      <svg v-else class="uploader-icon" viewBox="0 0 1024 1024">
        <path
          d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z"
        />
        <path
          d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z"
        />
      </svg>
      <span class="uploader-name">
        {{ video.uploaderName || video.up }}
      </span>
      <span v-if="video.uploadDate" class="upload-date">
        · {{ video.uploadDate }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchVideoDetail } from '@/api/video'
import { useUserStore } from '@/stores/user'
import { useWatchLaterStore } from '@/stores/watchLater'

/** 仅占位的透明 1×1，真实封面由 IntersectionObserver / 悬停再加载 */
const COVER_PLACEHOLDER =
  'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7'

const props = defineProps({
  video: {
    type: Object,
    required: true
  },
  onImgError: {
    type: Function,
    required: false
  },
  /** 鼠标悬停封面时尝试播放预览（与播放页右侧推荐一致） */
  hoverPreview: {
    type: Boolean,
    default: true
  },
  /**
   * 为 true 时：进入视口（或悬停）再请求真实封面；适合首页虚拟网格，避免屏外卡片抢带宽。
   */
  lazyCover: {
    type: Boolean,
    default: false
  }
})

const userStore = useUserStore()
const wlStore = useWatchLaterStore()

const thumbHover = ref(false)
const playUrlResolved = ref('')
const thumbVideoRef = ref(null)
const thumbOuterRef = ref(null)
const thumbCurrentSec = ref(0)
const thumbDurationSec = ref(0)
/** 懒加载封面：已进入视口或用户悬停则为 true */
const coverRevealed = ref(!props.lazyCover)

let coverIo = null

const videoId = computed(() => props.video?.id || props.video?.videoId || null)

const coverSrc = computed(() => {
  const real = (props.video?.cover || '').trim()
  if (!props.lazyCover) return real
  if (coverRevealed.value || thumbHover.value) return real || COVER_PLACEHOLDER
  return COVER_PLACEHOLDER
})

function disconnectCoverIo() {
  if (coverIo) {
    coverIo.disconnect()
    coverIo = null
  }
}

function setupCoverIo() {
  if (!props.lazyCover || coverRevealed.value) return
  disconnectCoverIo()
  nextTick(() => {
    const el = thumbOuterRef.value
    if (!el || coverRevealed.value) return
    coverIo = new IntersectionObserver(
      (entries) => {
        const hit = entries.some((e) => e.isIntersecting)
        if (hit) {
          coverRevealed.value = true
          disconnectCoverIo()
        }
      },
      { root: null, rootMargin: '160px 0px', threshold: 0.01 }
    )
    coverIo.observe(el)
  })
}

const displayPlayUrl = computed(() => {
  if (props.video?.playUrl) return props.video.playUrl
  return playUrlResolved.value || ''
})

const inWatchLater = computed(() => wlStore.isInList(videoId.value))

/** 封面正在预览播放：隐藏渐变、数据角标、稍后再看等叠层；右下角保留无底色进度时间 */
const isPreviewPlaying = computed(() => {
  return props.hoverPreview && thumbHover.value && !!displayPlayUrl.value
})

function formatTimeMmSs(sec) {
  const n = Number(sec)
  if (!Number.isFinite(n) || n < 0) return '00:00'
  const m = Math.floor(n / 60)
  const s = Math.floor(n % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function parseDurationLabelToSec(label) {
  if (label == null || label === '') return 0
  if (typeof label === 'number' && Number.isFinite(label)) return label
  const s = String(label).trim()
  const parts = s.split(':').map((x) => parseInt(x, 10))
  if (parts.some((x) => !Number.isFinite(x))) return 0
  if (parts.length === 2) return parts[0] * 60 + parts[1]
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2]
  return 0
}

const durationOverlayText = computed(() => {
  if (isPreviewPlaying.value) {
    const total =
      thumbDurationSec.value > 0 ? thumbDurationSec.value : parseDurationLabelToSec(props.video?.duration)
    return `${formatTimeMmSs(thumbCurrentSec.value)}/${formatTimeMmSs(total)}`
  }
  return props.video?.duration ?? ''
})

function onThumbVideoTimeUpdate() {
  const v = thumbVideoRef.value
  if (!v) return
  thumbCurrentSec.value = v.currentTime || 0
}

function onThumbVideoLoadedMeta() {
  const v = thumbVideoRef.value
  if (!v || !Number.isFinite(v.duration) || v.duration <= 0) return
  thumbDurationSec.value = v.duration
}

watch(
  () => props.video?.playUrl,
  (u) => {
    if (u) playUrlResolved.value = u
  },
  { immediate: true }
)

watch(videoId, () => {
  if (props.video?.playUrl) {
    playUrlResolved.value = props.video.playUrl
  } else {
    playUrlResolved.value = ''
  }
})

watch(
  () => [props.lazyCover, videoId.value],
  () => {
    if (!props.lazyCover) {
      coverRevealed.value = true
      disconnectCoverIo()
      return
    }
    coverRevealed.value = false
    disconnectCoverIo()
    setupCoverIo()
  }
)

onMounted(() => {
  if (!props.lazyCover) {
    coverRevealed.value = true
    return
  }
  setupCoverIo()
})

onUnmounted(() => {
  disconnectCoverIo()
})

async function ensurePlayUrl() {
  if (!props.hoverPreview) return
  if (displayPlayUrl.value) return
  const id = videoId.value
  if (!id) return
  try {
    const { data } = await fetchVideoDetail(id)
    const url = data?.playUrl || ''
    if (url) playUrlResolved.value = url
  } catch {
    /* 忽略 */
  }
}

function onThumbEnter() {
  thumbHover.value = true
  if (props.lazyCover) {
    coverRevealed.value = true
    disconnectCoverIo()
  }
  if (props.hoverPreview) {
    void ensurePlayUrl()
  }
}

function onThumbLeave() {
  thumbHover.value = false
  thumbCurrentSec.value = 0
  thumbDurationSec.value = 0
}

async function onWatchLaterClick() {
  const uid = userStore.user?.userId || userStore.user?.id
  if (!uid) {
    ElMessage.warning('请先登录后再使用稍后再看')
    return
  }
  const vid = videoId.value
  if (!vid) return
  if (wlStore.isInList(vid)) {
    const data = await wlStore.remove(uid, vid)
    if (data?.success !== false) ElMessage.success('已取消稍后再看')
  } else {
    const data = await wlStore.add(uid, vid)
    if (data?.success) {
      ElMessage.success('已加入稍后再看')
    } else if (data?.message) {
      ElMessage.info(data.message)
    }
  }
}

const formatPlayCount = (count) => {
  if (!count || count === '本地视频') return '0'
  const num = typeof count === 'number' ? count : parseInt(count) || 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const parseLikeCount = (raw) => {
  if (raw === null || raw === undefined || raw === '') return 0
  if (typeof raw === 'number') return Number.isFinite(raw) ? Math.floor(raw) : 0
  // 兼容后端可能返回 likeCount / like_count / likes
  const n = parseInt(String(raw), 10)
  return Number.isFinite(n) ? n : 0
}

const likeCount = computed(() => {
  const v = props.video?.likeCount ?? props.video?.like_count ?? props.video?.likes
  return parseLikeCount(v)
})

const showLikeBadge = computed(() => likeCount.value > 6000)

const likeBadgeText = computed(() => {
  const n = likeCount.value
  // 规则：单位为千/万；向下取整数
  if (n >= 10000) {
    const w = Math.floor(n / 10000)
    return `${w}万点赞`
  }
  const k = Math.floor(n / 1000)
  return `${k}千点赞`
})
</script>

<style scoped>
.video-card {
  display: grid;
  grid-template-rows: auto auto auto;
  gap: 6px;
  width: 100%;
}

.thumb-outer {
  position: relative;
  width: 100%;
  padding-bottom: 56%;
}

.thumb-wrap {
  position: absolute;
  inset: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f2f3;
}

.thumb-wrap img,
.thumb-wrap .thumb-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-video {
  z-index: 0;
}

.bottom-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.2) 50%, transparent 100%);
  z-index: 1;
  pointer-events: none;
}

.stats-overlay {
  position: absolute;
  left: 6px;
  bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 2;
}

.stats-overlay span {
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}

.play-count {
  align-items: flex-start;
}

.play-count .play-icon {
  margin-top: 1px;
}

.play-icon {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

.comment-count {
  align-items: flex-start;
}

.comment-count .comment-icon {
  margin-top: 1px;
}

.comment-icon {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
  color: #fff;
}

.duration {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: 12px;
  color: #fff;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.55), 0 0 1px rgba(0, 0, 0, 0.45);
  z-index: 3;
  pointer-events: none;
}

.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.play-button {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #333;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.thumb-wrap:hover .play-overlay {
  opacity: 1;
}

/* 纯色底：半透明会与预览画面叠色，悬停播预览时像「变色」 */
.wl-add {
  --wl-add-bg: #2f2f2f;

  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 20;
  display: inline-flex;
  align-items: center;
  gap: 0;
  max-width: 30px;
  height: 28px;
  padding: 0 6px;
  border: none;
  border-radius: 4px;
  appearance: none;
  -webkit-appearance: none;
  background-color: var(--wl-add-bg);
  color: #fff;
  cursor: pointer;
  overflow: hidden;
  white-space: nowrap;
  transition: max-width 0.22s ease, gap 0.2s ease;
  -webkit-tap-highlight-color: transparent;
  isolation: isolate;
}

/* 与默认相同，压住系统/浏览器对 button 的悬停、激活态改色 */
.wl-add:hover,
.wl-add:focus-visible,
.wl-add:focus,
.wl-add:active,
.thumb-outer:hover .wl-add,
.thumb-outer:hover .wl-add:hover,
.thumb-outer:hover .wl-add:focus-visible,
.thumb-outer:hover .wl-add:focus,
.thumb-outer:hover .wl-add:active {
  background-color: var(--wl-add-bg);
}

.wl-add:hover {
  max-width: 220px;
  gap: 8px;
}

.wl-add__icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
}

.wl-add__svg {
  display: block;
}

/* 仅鼠标在本按钮上时展开文案；悬停在封面其它区域不显示 */
.wl-add__hint {
  font-size: 12px;
  line-height: 1;
  padding-right: 4px;
  max-width: 0;
  opacity: 0;
  overflow: hidden;
  transition: max-width 0.2s ease, opacity 0.15s ease;
}

.wl-add:hover .wl-add__hint {
  max-width: 140px;
  opacity: 1;
}

.v-title {
  font-size: 13px;
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.uploader-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #9499a0;
}

.uploader-icon {
  width: 17px;
  height: 17px;
  fill: currentColor;
  flex-shrink: 0;
}

.uploader-name,
.upload-date {
  white-space: nowrap;
  transform: translateY(-1px);
}

.like-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 18px;
  padding: 0px 6px;
  border-radius: 5px;
  background-color: #FFEDE2;
  color: #FF661A;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
  transform: translateY(-1px);
}
</style>
