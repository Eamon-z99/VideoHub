<template>
  <div v-if="inCollection && collection" class="collection-panel">
    <div class="collection-header">
      <div class="row row-top">
        <div class="title-line">
          <span class="coll-title">{{ collection.name || '合集' }}</span>
          <span v-if="total > 0" class="coll-progress">({{ progressText }})</span>
        </div>
        <div class="autoplay-wrap">
          <span class="autoplay-label">自动连播</span>
          <el-switch v-model="autoPlay" size="small" />
        </div>
      </div>
      <div class="row row-meta">
        <span class="play-count">{{ playCountText }}</span>
        <el-tooltip
          placement="bottom-start"
          trigger="hover"
          :show-arrow="false"
          :offset="6"
          :enterable="true"
          popper-class="collection-intro-tooltip"
        >
          <template #content>
            <div class="intro-tooltip-content">{{ introTooltipText }}</div>
          </template>
          <button type="button" class="link-intro">
            <svg
              class="icon intro-icon"
              xmlns="http://www.w3.org/2000/svg"
              xmlns:xlink="http://www.w3.org/1999/xlink"
              viewBox="0 0 24 24"
              width="16"
              height="16"
              aria-hidden="true"
            >
              <path
                d="M11.8735 5.75C7.19104 5.75 4.06125 9.47906 2.74101 11.4633C2.52011 11.7953 2.52011 12.2047 2.74101 12.5367C4.06125 14.5209 7.19104 18.25 11.8735 18.25C16.524 18.25 19.7804 14.5765 21.1972 12.5699C21.4434 12.2213 21.4434 11.7787 21.1972 11.4301C19.7804 9.4235 16.524 5.75 11.8735 5.75zM1.07591 10.3554C2.44961 8.29081 6.10187 3.75 11.8735 3.75C17.5816 3.75 21.3619 8.19586 22.831 10.2765C23.5654 11.3167 23.5654 12.6833 22.831 13.7235C21.3619 15.8041 17.5816 20.25 11.8735 20.25C6.10187 20.25 2.44961 15.7092 1.07591 13.6446C0.40847 12.6415 0.40847 11.3585 1.07591 10.3554z"
                fill="currentColor"
              />
              <path
                d="M12 9.5C10.6193 9.5 9.5 10.6193 9.5 12C9.5 13.3807 10.6193 14.5 12 14.5C13.3807 14.5 14.5 13.3807 14.5 12C14.5 10.6193 13.3807 9.5 12 9.5zM7.5 12C7.5 9.51472 9.51472 7.5 12 7.5C14.4853 7.5 16.5 9.51472 16.5 12C16.5 14.4853 14.4853 16.5 12 16.5C9.51472 16.5 7.5 14.4853 7.5 12z"
                fill="currentColor"
              />
            </svg>
            简介
          </button>
        </el-tooltip>
        <div class="spacer" />
        <template v-if="ownCollection">
          <span class="sub-hint">自己的合集</span>
        </template>
        <template v-else-if="subscribed">
          <button type="button" class="btn-subscribe subscribed" :disabled="subLoading" @click="onUnsubscribe">
            已订阅
          </button>
        </template>
        <template v-else>
          <button
            type="button"
            class="btn-subscribe"
            :disabled="subLoading || !userStore.isAuthenticated"
            @click="onSubscribe"
          >
            订阅合集
          </button>
        </template>
      </div>
    </div>

    <div class="collection-list-wrap">
      <ul class="collection-list">
        <li
          v-for="(item, idx) in videos"
          :key="String(item.video_id)"
          class="collection-item"
          :class="{ active: isCurrent(item) }"
          @click="onSelect(item)"
        >
          <span
            v-if="isCurrent(item)"
            class="playing-icon"
            :class="{ 'is-playing': isPlaying }"
            aria-hidden="true"
          >
            <span class="bar b1" /><span class="bar b2" /><span class="bar b3" />
          </span>
          <span class="item-title">{{ item.title || '未命名' }}</span>
          <span class="item-dur">{{ formatDuration(item.duration) }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getVideoCollectionContextByVideo,
  subscribeVideoCollection,
  unsubscribeVideoCollection
} from '@/api/video'
import { useUserStore } from '@/stores/user'

const AUTOPLAY_KEY = 'videohub_collection_autoplay'

const props = defineProps({
  videoId: {
    type: String,
    default: ''
  },
  /** 与播放器同步：为 true 时当前集「波形」图标循环动画 */
  isPlaying: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['select-video'])

const userStore = useUserStore()

const inCollection = ref(false)
const collection = ref(null)
const videos = ref([])
const currentIndex = ref(-1)
const total = ref(0)
const totalPlayCount = ref(0)
const subscribed = ref(false)
const ownCollection = ref(false)
const subLoading = ref(false)

const autoPlay = ref(
  typeof localStorage !== 'undefined' && localStorage.getItem(AUTOPLAY_KEY) !== '0'
)

watch(autoPlay, (v) => {
  try {
    localStorage.setItem(AUTOPLAY_KEY, v ? '1' : '0')
  } catch (_) {}
})

const progressText = computed(() => {
  const t = total.value
  if (t <= 0) return ''
  const cur = currentIndex.value
  const n = cur >= 0 ? cur + 1 : '?'
  return `${n}/${t}`
})

const playCountText = computed(() => {
  const n = Number(totalPlayCount.value) || 0
  return `${formatPlayCount(n)}播放`
})

const introTooltipText = computed(() => {
  const d = collection.value?.description
  const t = d != null ? String(d).trim() : ''
  return t || '暂无简介'
})

function formatPlayCount (n) {
  if (n >= 10000) {
    const w = n / 10000
    const s = w >= 10 ? String(Math.round(w * 10) / 10) : w.toFixed(1).replace(/\.0$/, '')
    return `${s}万`
  }
  return String(n)
}

function formatDuration (sec) {
  const s = Number(sec) || 0
  const m = Math.floor(s / 60)
  const r = Math.floor(s % 60)
  return `${m}:${String(r).padStart(2, '0')}`
}

function isCurrent (item) {
  return String(item.video_id) === String(props.videoId)
}

async function load () {
  const id = (props.videoId || '').trim()
  if (!id) {
    inCollection.value = false
    return
  }
  try {
    const { data } = await getVideoCollectionContextByVideo(id)
    if (!data.success || !data.data) {
      inCollection.value = false
      return
    }
    const d = data.data
    inCollection.value = !!d.inCollection
    if (!d.inCollection) {
      collection.value = null
      videos.value = []
      return
    }
    collection.value = d.collection || null
    videos.value = Array.isArray(d.videos) ? d.videos : []
    currentIndex.value = typeof d.currentIndex === 'number' ? d.currentIndex : -1
    total.value = typeof d.total === 'number' ? d.total : videos.value.length
    totalPlayCount.value = d.totalPlayCount != null ? Number(d.totalPlayCount) : 0
    subscribed.value = !!d.subscribed
    ownCollection.value = !!d.ownCollection
  } catch (e) {
    inCollection.value = false
    console.error(e)
  }
}

function onSelect (item) {
  const vid = item?.video_id
  if (!vid || String(vid) === String(props.videoId)) return
  emit('select-video', String(vid))
}

async function onSubscribe () {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  const cid = collection.value?.id
  if (cid == null) return
  subLoading.value = true
  try {
    const { data } = await subscribeVideoCollection(cid)
    if (data.success) {
      subscribed.value = true
      ElMessage.success('订阅成功')
    } else {
      ElMessage.error(data.message || '订阅失败')
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    subLoading.value = false
  }
}

async function onUnsubscribe () {
  const cid = collection.value?.id
  if (cid == null) return
  try {
    await ElMessageBox.confirm('确定取消订阅该合集？', '提示', {
      type: 'warning',
      confirmButtonText: '取消订阅',
      cancelButtonText: '保留'
    })
  } catch {
    return
  }
  subLoading.value = true
  try {
    const { data } = await unsubscribeVideoCollection(cid)
    if (data.success) {
      subscribed.value = false
      ElMessage.success('已取消订阅')
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (e) {
    // —
  } finally {
    subLoading.value = false
  }
}

/**
 * 当前视频播放结束且开启自动连播时，返回下一集 video_id；否则 null。
 */
function getNextVideoIdAfter (currentVideoId) {
  if (!inCollection.value || !autoPlay.value) return null
  const list = videos.value
  const cur = String(currentVideoId || '')
  const i = list.findIndex((v) => String(v.video_id) === cur)
  if (i < 0 || i >= list.length - 1) return null
  return String(list[i + 1].video_id)
}

defineExpose({
  getNextVideoIdAfter,
  reload: load
})

watch(
  () => props.videoId,
  () => load(),
  { immediate: true }
)

watch(
  () => userStore.isAuthenticated,
  () => load()
)
</script>

<style scoped>
.collection-panel {
  background: #f1f2f3;
  border-radius: 8px;
  padding: 12px 12px 10px;
  box-sizing: border-box;
}

.collection-header {
  margin-bottom: 10px;
}

.row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.row-top {
  justify-content: space-between;
  margin-bottom: 8px;
}

.title-line {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.coll-title {
  font-size: 16px;
  font-weight: 700;
  color: #18191c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coll-progress {
  font-size: 14px;
  color: #9499a0;
  flex-shrink: 0;
}

.autoplay-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.autoplay-label {
  font-size: 14px;
  color: #61666d;
}

.row-meta {
  font-size: 14px;
  color: #9499a0;
}

.play-count {
  flex-shrink: 0;
}

.link-intro {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  padding: 0;
  font-size: 14px;
  color: #61666d;
  cursor: pointer;
}

.link-intro:hover {
  color: #409eff;
}

.intro-icon {
  flex-shrink: 0;
  display: block;
}

.spacer {
  flex: 1;
}

.sub-hint {
  font-size: 14px;
  color: #9499a0;
}

.btn-subscribe {
  border: 1px solid #409eff;
  color: #409eff;
  background: transparent;
  border-radius: 4px;
  padding: 2px 10px;
  font-size: 14px;
  cursor: pointer;
  line-height: 1.4;
}

.btn-subscribe:hover:not(:disabled) {
  background: rgba(64, 158, 255, 0.08);
}

.btn-subscribe:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-subscribe.subscribed {
  border-color: #c0c4cc;
  color: #606266;
}

.collection-list-wrap {
  max-height: 280px;
  overflow-y: auto;
  margin: 0 -4px;
  padding: 0 4px;
}

.collection-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.collection-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 15px;
  color: #18191c;
}

.collection-item:hover:not(.active) {
  background: rgba(0, 0, 0, 0.04);
}

.collection-item.active {
  background: #fff;
  color: #409eff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.playing-icon {
  display: inline-flex;
  align-items: flex-end;
  justify-content: center;
  gap: 3px;
  width: 14px;
  height: 16px;
  flex-shrink: 0;
}

.playing-icon .bar {
  width: 3px;
  height: 14px;
  border-radius: 999px;
  background: #409eff;
  transform-origin: center bottom;
  flex-shrink: 0;
}

/* 暂停：静态三柱（左短、中高、右中） */
.playing-icon:not(.is-playing) .b1 {
  transform: scaleY(0.42);
}
.playing-icon:not(.is-playing) .b2 {
  transform: scaleY(1);
}
.playing-icon:not(.is-playing) .b3 {
  transform: scaleY(0.62);
}

/* 播放中：独立循环起伏 */
.playing-icon.is-playing .b1 {
  animation: collection-eq 0.55s ease-in-out infinite;
  animation-delay: 0s;
}
.playing-icon.is-playing .b2 {
  animation: collection-eq 0.55s ease-in-out infinite;
  animation-delay: 0.14s;
}
.playing-icon.is-playing .b3 {
  animation: collection-eq 0.55s ease-in-out infinite;
  animation-delay: 0.28s;
}

@keyframes collection-eq {
  0%,
  100% {
    transform: scaleY(0.32);
  }
  50% {
    transform: scaleY(1);
  }
}

.item-title {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-dur {
  flex-shrink: 0;
  font-size: 14px;
  color: #9499a0;
}

.collection-item.active .item-dur {
  color: #79bbff;
}
</style>

<!-- popper 挂到 body，需非 scoped 覆盖样式 -->
<style>
.collection-intro-tooltip.el-popper {
  background: #e3e5e7 !important;
  border: none !important;
  border-radius: 8px !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08) !important;
  padding: 10px 12px !important;
  max-width: min(320px, 90vw) !important;
}

.collection-intro-tooltip .intro-tooltip-content {
  font-size: 14px;
  line-height: 1.55;
  color: #61666d;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
