<template>
  <div class="search-page">
    <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    <div class="search-container">
      <div class="search-main">
        <!-- 二级导航 + 搜索栏 -->
        <nav class="secondary-nav">
          <div class="nav-left">
            <h1 class="page-title">搜索结果</h1>
            <div class="tabs">
              <button class="tab" :class="{ active: activeTab === 'all' }">综合</button>
              <button class="tab" disabled>视频</button>
              <button class="tab" disabled>番剧</button>
              <button class="tab" disabled>UP主</button>
              <button class="tab" disabled>专栏</button>
            </div>
          </div>
          <div class="nav-right">
            <input
              v-model="localKeyword"
              class="search-input"
              placeholder="搜索视频 / UP 主"
              @keyup.enter="triggerSearch"
            />
            <button class="search-btn" @click="triggerSearch">搜索</button>
          </div>
        </nav>

        <!-- 主内容网格 -->
        <section class="content">
          <div v-if="!keyword.trim()" class="empty">
            请输入关键词进行搜索
          </div>
          <div v-else-if="loading" class="loading">搜索中...</div>
          <div v-else-if="videos.length === 0" class="empty">
            没有找到与 “{{ keyword }}” 相关的视频
          </div>
          <div v-else>
            <div class="result-summary">
              共找到 <b>{{ total }}</b> 条与 “{{ keyword }}” 相关的视频
            </div>
            <div class="grid">
              <div
                v-for="item in videos"
                :key="item.id"
                class="card"
                @click="openVideo(item.id)"
              >
                <div class="thumb">
                  <img :src="item.cover" :alt="item.title" />
                  <span v-if="item.duration" class="duration">{{ item.duration }}</span>
                </div>
                <div class="meta">
                  <div class="title" :title="item.title">{{ item.title }}</div>
                  <div class="sub">
                    <span class="up">{{ item.up || '未知UP主' }}</span>
                    <span class="dot">·</span>
                    <span class="play-count">{{ item.playCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载更多触发器 -->
          <div
            ref="loadMoreTrigger"
            class="loading-bar"
            v-if="keyword.trim()"
          >
            <span v-if="loadingMore">加载中...</span>
            <span v-else-if="finished">已加载全部</span>
            <span v-else style="visibility: hidden;">加载更多</span>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import { searchVideos } from '@/api/video'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const localKeyword = ref('')
const activeTab = ref('all')

const videos = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)

const loadMoreTrigger = ref(null)
let observer = null
// 避免刚进入页面时触发一次 IntersectionObserver 导致立即请求第二页
const hasFirstIntersect = ref(false)

const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return ''
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) {
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m}:${s.toString().padStart(2, '0')}`
}

const normalizeList = (data) => {
  const list = Array.isArray(data?.list) ? data.list : (Array.isArray(data) ? data : [])
  return list.map((item) => {
    const rawCover = (item?.coverUrl || '').trim()
    const safeCover = rawCover || '/assets/home.png'
    return {
      ...item,
      cover: safeCover,
      duration: formatDuration(item?.duration),
      playCount: typeof item?.viewCount === 'number' ? item.viewCount : '本地视频',
      up: item?.uploaderName || '本地文件',
      id: item?.videoId || item?.id,
    }
  })
}

const loadResults = async (reset = false) => {
  if (loading.value || loadingMore.value) return

  const kw = keyword.value.trim()
  if (!kw) {
    videos.value = []
    total.value = 0
    finished.value = true
    return
  }

  if (reset) {
    page.value = 1
    finished.value = false
    videos.value = []
    total.value = 0
  }

  const isFirstPage = page.value === 1
  if (isFirstPage) loading.value = true
  else loadingMore.value = true

  try {
    const { data } = await searchVideos({
      page: page.value,
      pageSize,
      keyword: kw,
    })
    const mapped = normalizeList(data)
    videos.value = [...videos.value, ...mapped]
    const totalVal = typeof data?.total === 'number' ? data.total : undefined
    if (typeof totalVal === 'number') {
      total.value = totalVal
    }
    if ((total.value && videos.value.length >= total.value) || mapped.length < pageSize) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (e) {
    if (isFirstPage) {
      videos.value = []
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const setupIntersectionObserver = () => {
  if (!loadMoreTrigger.value || finished.value) return

  if (observer) {
    observer.disconnect()
  }

  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (!entry.isIntersecting) return

      // 第一次进入视口时只标记一下，不触发加载，防止进页面就请求两页
      if (!hasFirstIntersect.value) {
        hasFirstIntersect.value = true
        return
      }

      if (!loading.value && !loadingMore.value && !finished.value) {
        loadResults()
      }
    },
    {
      root: null,
      rootMargin: '100px',
      threshold: 0.1,
    },
  )

  observer.observe(loadMoreTrigger.value)
}

const triggerSearch = () => {
  const kw = localKeyword.value.trim()
  if (!kw) return
  router.push({ name: 'search', query: { keyword: kw } })
}

const openVideo = (videoId) => {
  if (!videoId) return
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const path = `/video/${encodeURIComponent(videoId)}`
  const url = `${normalizedBase}${path}`
  window.open(url, '_blank')
}

// 初始化关键字
onMounted(() => {
  const kwParam = route.query.keyword
  const kw = typeof kwParam === 'string' ? kwParam : ''
  keyword.value = kw
  localKeyword.value = kw
  hasFirstIntersect.value = false
  loadResults(true)
  nextTick(() => {
    setupIntersectionObserver()
  })
})

// 路由 keyword 变化时重新搜索
watch(
  () => route.query.keyword,
  (val) => {
    const kw = typeof val === 'string' ? val : ''
    if (kw === keyword.value) return
    keyword.value = kw
    localKeyword.value = kw
    hasFirstIntersect.value = false
    loadResults(true)
  },
)

// 列表变化时重新设置 observer
watch(
  () => videos.value.length,
  () => {
    nextTick(() => {
      setupIntersectionObserver()
    })
  },
)

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style scoped lang="scss">
.search-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  min-height: 100vh;
  margin: 0 auto;
  background: #fff;
  padding-top: 64px;

  .search-container {
    display: flex;
    padding: 20px 0;
    max-width: 75%;
    min-width: 1200px;
    width: 100%;
    margin: 0 auto;
    justify-content: center;
    align-items: flex-start;
  }

  .search-main {
    flex: 1;
    background: #fff;
    padding: 20px;
  }

  .secondary-nav {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 20px;
  }

  .nav-left {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #222;
    margin: 0;
  }

  .tabs {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .tab {
    padding: 6px 16px;
    border: none;
    background: transparent;
    color: #666;
    font-size: 14px;
    border-radius: 4px;
    cursor: default;

    &.active {
      color: #00a1d6;
      background: #e6f7ff;
    }
  }

  .nav-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .search-input {
    width: 320px;
    height: 36px;
    padding: 0 12px;
    border: 1px solid #e3e3e3;
    border-radius: 4px;
    font-size: 14px;

    &:focus {
      outline: none;
      border-color: #00a1d6;
    }
  }

  .search-btn {
    height: 36px;
    padding: 0 20px;
    border-radius: 4px;
    border: none;
    background: #00a1d6;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
  }

  .content {
    margin-top: 16px;
  }

  .result-summary {
    margin-bottom: 12px;
    font-size: 14px;
    color: #666;
  }

  .grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
  }

  .card {
    cursor: pointer;
    display: flex;
    flex-direction: column;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }
  }

  .thumb {
    position: relative;
    width: 100%;
    padding-bottom: 56.25%;
    border-radius: 8px;
    overflow: hidden;
    background: #f7f7f7;
    margin-bottom: 8px;

    img {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .duration {
      position: absolute;
      right: 6px;
      bottom: 6px;
      background: rgba(0, 0, 0, 0.65);
      color: #fff;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 12px;
    }
  }

  .meta {
    .title {
      font-size: 13px;
      color: #222;
      line-height: 1.4;
      max-height: 36px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 4px;
    }

    .sub {
      color: #999;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
    }

    .dot {
      margin: 0 2px;
    }
  }

  .loading,
  .empty {
    text-align: center;
    padding: 60px 20px;
    color: #999;
    font-size: 14px;
  }

  .loading-bar {
    text-align: center;
    padding: 20px;
    color: #999;
    font-size: 14px;
  }
}
</style>


