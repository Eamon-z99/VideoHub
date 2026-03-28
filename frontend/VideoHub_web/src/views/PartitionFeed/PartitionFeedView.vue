<template>
  <div class="partition-feed">
    <TopHeader :transparent-at-top="false" />
    <div class="partition-feed__inner">
      <section class="partition-feed__main">
        <p v-if="currentLabel && isKnownLabel" class="partition-feed__summary">
          <span class="partition-feed__label">「{{ currentLabel }}」</span>
          <span class="partition-feed__meta">相关视频 · 共 {{ totalCount }} 条</span>
        </p>
        <div v-if="!isKnownLabel" class="partition-feed__state">
          该入口无效，请从首页右侧六个快捷入口进入或检查链接。
        </div>
        <div v-else-if="loading && videos.length === 0" class="partition-feed__state">加载中…</div>
        <div v-else-if="!loading && videos.length === 0" class="partition-feed__state">暂无包含该分区标签的视频</div>
        <div v-else-if="isKnownLabel" class="partition-feed__grid">
          <div
            v-for="v in videos"
            :key="v.id"
            class="partition-feed__cell"
            @click="playVideo(v)"
          >
            <VideoCard :video="v" :on-img-error="onImgError" lazy-cover />
          </div>
        </div>
        <div
          v-if="isKnownLabel && videos.length > 0"
          ref="loadMoreTrigger"
          class="partition-feed__loading-bar"
        >
          <span v-if="loadingMore">加载中...</span>
          <span v-else-if="finished">已加载全部</span>
          <span v-else style="visibility: hidden">加载更多</span>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import VideoCard from '@/components/VideoCard.vue'
import { fetchVideos } from '@/api/video'
import { VIDEO_UTILITY_PARTITIONS } from '@/constants/videoUtilityPartitions'

const route = useRoute()

const fallbackCover = '/images/banner-1.jpg'

const currentLabel = computed(() => {
  const raw = route.params.name
  const s = typeof raw === 'string' ? raw : Array.isArray(raw) ? raw[0] : ''
  try {
    return decodeURIComponent(s || '')
  } catch {
    return s || ''
  }
})

const isKnownLabel = computed(() => {
  const t = currentLabel.value?.trim()
  return !!t && VIDEO_UTILITY_PARTITIONS.includes(t)
})

const videos = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 40
const totalCount = ref(0)
const loadMoreTrigger = ref(null)
let observer = null

const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const normalizeVideoItem = (item) => {
  const rawCover = (item?.coverUrl || '').trim()
  const safeCover = rawCover || fallbackCover
  const durationText = formatDuration(item?.duration)
  return {
    ...item,
    cover: safeCover,
    duration: durationText,
    playCount: typeof item?.viewCount === 'number' ? item.viewCount : '本地视频',
    up: item?.uploaderName || item?.sourceFile || '本地',
    playUrl: item?.playUrl || '',
    id: item?.videoId || item?.id,
    isVideo: !!item?.playUrl
  }
}

const normalizeList = (data) => {
  const list = Array.isArray(data?.list) ? data.list : []
  return list.map((item) => normalizeVideoItem(item))
}

const fetchPage = async (reset = false) => {
  const label = currentLabel.value?.trim()
  if (!label || !VIDEO_UTILITY_PARTITIONS.includes(label)) {
    videos.value = []
    finished.value = true
    totalCount.value = 0
    return
  }
  if (loading.value || loadingMore.value) return

  if (reset) {
    page.value = 1
    finished.value = false
    videos.value = []
    totalCount.value = 0
  }

  const isFirst = page.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true

  try {
    const { data } = await fetchVideos(page.value, pageSize, null, false, null, null, label)
    const mapped = normalizeList(data)
    videos.value = [...videos.value, ...mapped]
    const total = typeof data?.total === 'number' ? data.total : undefined
    if (typeof total === 'number') totalCount.value = total
    if ((total !== undefined && videos.value.length >= total) || mapped.length < pageSize) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch {
    if (isFirst) videos.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function setupObserver() {
  if (observer) observer.disconnect()
  observer = null
  const el = loadMoreTrigger.value
  if (!el || finished.value) return
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0]?.isIntersecting && !loading.value && !loadingMore.value && !finished.value) {
        fetchPage(false)
      }
    },
    { root: null, rootMargin: '240px 0px', threshold: 0 }
  )
  observer.observe(el)
}

watch(
  currentLabel,
  () => {
    void fetchPage(true).then(() => nextTick(() => setupObserver()))
  },
  { immediate: true }
)

watch(
  () => finished.value,
  (done) => {
    if (done && observer) {
      observer.disconnect()
      observer = null
    } else if (!done) nextTick(() => setupObserver())
  }
)

onMounted(() => {
  nextTick(() => setupObserver())
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

const onImgError = (evt) => {
  const target = evt?.target
  if (!target || target.__fallbackApplied) return
  target.__fallbackApplied = true
  target.onerror = null
  target.src = fallbackCover
}

const openInNewTab = (path) => {
  const micro = window.__MICRO_APP_BASE_ROUTE__ || ''
  const base = micro
    ? String(micro).replace(/\/$/, '')
    : String(import.meta.env.BASE_URL || '/').replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${base}${normalizedPath}`
  window.open(url, '_blank', 'noopener,noreferrer')
}

const playVideo = (video) => {
  if (!video?.id) return
  openInNewTab(`/video/${encodeURIComponent(video.id)}`)
}
</script>

<style scoped lang="scss">
.partition-feed {
  min-height: 100vh;
  background: #fff;
  padding-top: 64px;
  box-sizing: border-box;
}

.partition-feed__inner {
  max-width: 1350px;
  margin: 0 auto;
  padding: 16px 20px 48px;
  box-sizing: border-box;
}

.partition-feed__summary {
  margin: 0 0 16px;
  font-size: 15px;
  color: #61666d;
}

.partition-feed__label {
  font-weight: 600;
  color: #18191c;
}

.partition-feed__meta {
  margin-left: 8px;
}

.partition-feed__state {
  padding: 48px 16px;
  text-align: center;
  color: #9499a0;
  font-size: 14px;
}

.partition-feed__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px 12px;
}

.partition-feed__cell {
  min-width: 0;
  cursor: pointer;
}

.partition-feed__loading-bar {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: #9499a0;
}
</style>
