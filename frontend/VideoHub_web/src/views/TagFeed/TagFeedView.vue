<template>
  <div class="tag-feed">
    <TopHeader :transparent-at-top="false" />
    <div class="tag-feed__inner">
      <section class="tag-feed__main">
        <p v-if="currentTag && isKnownTag" class="tag-feed__summary">
          <span class="tag-feed__label">「{{ currentTag }}」</span>
          <span class="tag-feed__meta">相关视频 · 共 {{ totalCount }} 条</span>
        </p>
        <div v-if="!isKnownTag" class="tag-feed__state">该地址不是有效的分区标签，请从首页分区入口进入或检查链接。</div>
        <div v-else-if="loading && videos.length === 0" class="tag-feed__state">加载中…</div>
        <div v-else-if="!loading && videos.length === 0" class="tag-feed__state">暂无带该标签的视频</div>
        <div v-else-if="isKnownTag" class="tag-feed__grid">
          <div
            v-for="v in videos"
            :key="v.id"
            class="tag-feed__cell"
            @click="playVideo(v)"
          >
            <VideoCard :video="v" :on-img-error="onImgError" lazy-cover />
          </div>
        </div>
        <div
          v-if="isKnownTag && videos.length > 0"
          ref="loadMoreTrigger"
          class="tag-feed__loading-bar"
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
import { VIDEO_NAV_TAGS } from '@/constants/videoNavTags'

const route = useRoute()

const fallbackCover = '/images/banner-1.jpg'

const currentTag = computed(() => {
  const raw = route.params.tag
  const s = typeof raw === 'string' ? raw : Array.isArray(raw) ? raw[0] : ''
  try {
    return decodeURIComponent(s || '')
  } catch {
    return s || ''
  }
})

const isKnownTag = computed(() => {
  const t = currentTag.value?.trim()
  return !!t && VIDEO_NAV_TAGS.includes(t)
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
  const tag = currentTag.value?.trim()
  if (!tag || !VIDEO_NAV_TAGS.includes(tag)) {
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
    const { data } = await fetchVideos(page.value, pageSize, null, false, null, tag)
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
  currentTag,
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
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = String(base).replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  window.open(`${normalizedBase}${normalizedPath || '/'}`, '_blank')
}

const playVideo = (video) => {
  if (!video?.id) return
  openInNewTab(`/video/${encodeURIComponent(video.id)}`)
}
</script>

<style scoped lang="scss">
.tag-feed {
  min-height: 100vh;
  background: #fff;
  padding-top: 64px;
  box-sizing: border-box;
}

.tag-feed__inner {
  max-width: 1350px;
  margin: 0 auto;
  padding: 16px 20px 48px;
  box-sizing: border-box;
}

.tag-feed__summary {
  margin: 0 0 16px;
  font-size: 15px;
  color: #61666d;
}

.tag-feed__label {
  font-weight: 600;
  color: #18191c;
}

.tag-feed__meta {
  margin-left: 8px;
}

.tag-feed__state {
  padding: 48px 16px;
  text-align: center;
  color: #9499a0;
  font-size: 14px;
}

.tag-feed__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px 12px;
}

.tag-feed__cell {
  min-width: 0;
  cursor: pointer;
}

.tag-feed__loading-bar {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: #9499a0;
}
</style>
