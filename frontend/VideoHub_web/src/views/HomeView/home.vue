<template>
  <div class="home">
    <TopHeader />
    <header class="site-header">
      <img class="header-bg" src="/assets/header.png" alt="banner" />
    </header>
    <section class="navigation-section">
      <div class="nav-left-section">
        <div class="nav-left-top">
          <div class="quick-icons">
            <div class="qi-item" @click="goTo('/feed')">
              <div class="qi-circle orange">
                <img src="/assets/feed.png" class="qi-icon" />
              </div>
              <div class="qi-text">动态</div>
            </div>
            <div class="qi-item">
              <div class="qi-circle pink">
                <img src="/assets/trending.png" class="qi-icon" />
              </div>
              <div class="qi-text">热门</div>
            </div>
          </div>

          <div class="grid-container">
            <a href="#" v-for="c in categories" :key="c" class="grid-item">{{ c }}</a>
          </div>
        </div>
      </div>
      
      <div class="nav-right-section">
        <div class="utility-links">
          <div class="utility-row">
            <div class="utility-item" @click="goTo('/column')">
              <img src="/assets/channel/column.png" class="utility-icon" />
              <span class="utility-text">专栏</span>
            </div>
            <div class="utility-item">
              <img src="/assets/channel/activity.png" class="utility-icon" />
              <span class="utility-text">活动</span>
            </div>
            <div class="utility-item" @click="goTo('/community')">
              <img src="/assets/channel/community.png" class="utility-icon" />
              <span class="utility-text">社区中心</span>
            </div>
          </div>
          <div class="utility-row">
            <div class="utility-item" @click="goTo('/live')">
              <img src="/assets/channel/live.png" class="utility-icon" />
              <span class="utility-text">直播</span>
            </div>
            <div class="utility-item" @click="goTo('/classroom')">
              <img src="/assets/channel/class.png" class="utility-icon" />
              <span class="utility-text">课堂</span>
            </div>
            <div class="utility-item" @click="goTo('/music-chart')">
              <img src="/assets/channel/new-songs-chart.png" class="utility-icon" />
              <span class="utility-text">新歌热榜</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="hero-grid">
      <div class="banner">
        <div class="slider" :style="{ transform: `translateX(-${slideIndex * 100}%)` }">
          <div
            v-for="(s, i) in slides"
            :key="i"
            class="slide"
            @click="playTopVideo(s)"
          >
            <div class="thumb-wrap">
              <img :src="s.cover" :alt="s.title" @error="onImgError" />
              <span class="duration">{{ s.duration }}</span>
              <div class="play-overlay">
                <div class="play-button">▶</div>
              </div>
            </div>
            <div class="slide-caption">{{ s.title }}</div>
          </div>
        </div>
        <button class="arrow left" @click="prev">‹</button>
        <button class="arrow right" @click="next">›</button>
        <div class="indicators">
          <span v-for="(s,i) in slides" :key="`dot-${i}`" class="dot" :class="{ active: i===slideIndex }" @click="go(i)" />
        </div>
      </div>
      <aside class="recommend">
        <div
          class="top-video"
          v-for="(r, i) in recommends"
          :key="i"
          @click="playTopVideo(r)"
        >
          <div class="thumb-wrap">
            <img :src="r.cover" @error="onImgError" />
            <span class="duration">{{ r.duration }}</span>
            <div class="play-overlay">
              <div class="play-button">▶</div>
            </div>
          </div>
          <div class="v-title" :title="r.title">{{ r.title }}</div>
          <div class="v-sub">推荐</div>
        </div>
      </aside>
    </section>

    <section class="section">
      <div ref="videoWrapperRef" class="video-virtual-wrapper">
        <ElVirtualGrid
          class="video-virtual-grid"
          :data="videos"
          :total-column="columnCount"
          :total-row="rowCount"
          :column-count="columnCount"
          :column-width="columnWidth"
          :row-height="rowHeight"
          :height="gridHeight"
          :width="gridWidth"
        >
          <template #default="{ data, rowIndex, columnIndex, style }">
            <div :style="[style, getPaddingStyle(columnIndex)]" class="video">
              <div
                v-if="data && data[rowIndex * columnCount + columnIndex]"
                class="card"
                @click="playVideo(data[rowIndex * columnCount + columnIndex])"
              >
                <VideoCard
                  :video="data[rowIndex * columnCount + columnIndex]"
                  :on-img-error="onImgError"
                />
              </div>
            </div>
          </template>
        </ElVirtualGrid>
      </div>
      <div 
        ref="loadMoreTrigger" 
        class="loading-bar"
      >
        <span v-if="loadingMore">加载中...</span>
        <span v-else-if="finished">已加载全部</span>
        <span v-else-if="loadingVideos">加载中...</span>
        <span v-else style="visibility: hidden;">加载更多</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
// @ts-ignore Element Plus 未在主导出暴露虚拟列表，子路径命名导出
import { FixedSizeGrid as ElVirtualGrid } from 'element-plus/es/components/virtual-list/index.mjs'
import { useRouter } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import VideoCard from '@/components/VideoCard.vue'
import { fetchVideos, fetchTopVideos } from '@/api/video'

const router = useRouter()

const categories = [
  '番剧','国创','综艺','动画','鬼畜','舞蹈','娱乐','科技','美食','汽车','运动','VLOG','单机游戏','公益','电影','电视剧','纪录片','音乐','知识','资讯','生活','时尚'
]

const slides = ref<any[]>([])
const slideIndex = ref(0)
let timer: any
const next = () => { slideIndex.value = (slideIndex.value + 1) % slides.value.length }
const prev = () => { slideIndex.value = (slideIndex.value - 1 + slides.value.length) % slides.value.length }
const go = (i: number) => { slideIndex.value = i }

const recommends = ref<any[]>([])

const videos = ref<any[]>([])
const loadingVideos = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20
const totalCount = ref(0)
// 响应式窗口宽度
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1350)
// 视频容器ref，用于获取实际宽度
const videoWrapperRef = ref<HTMLElement | null>(null)
// 实际容器宽度
const actualContainerWidth = ref(1350)

// 响应式计算容器宽度：小屏幕1350px，大屏幕75%宽度
const containerWidth = computed(() => {
  const screenWidth = windowWidth.value
  if (screenWidth >= 1800) {
    // 大屏幕：75%宽度，但不超过2300px（与.home的max-width一致）
    return Math.min(Math.floor(screenWidth * 0.75), 2300)
  }
  return 1350
})

// 更新实际容器宽度
const updateActualWidth = () => {
  if (videoWrapperRef.value) {
    const rect = videoWrapperRef.value.getBoundingClientRect()
    actualContainerWidth.value = Math.floor(rect.width)
  }
}

// 网格可用宽度 = 实际容器宽度（已经减去了padding）
const gridWidth = computed(() => {
  // 使用实际测量的宽度，如果没有则使用计算值
  const width = actualContainerWidth.value > 0 ? actualContainerWidth.value : (containerWidth.value - 40)
  return width
})

// 响应式计算列数：小屏幕5列，大屏幕根据宽度动态计算
// 每列约270px（包含.video的左右padding各8px），但至少5列
const columnCount = computed(() => {
  const width = gridWidth.value
  // 每列约270px，但至少5列
  const cols = Math.max(5, Math.floor(width / 270))
  return cols
})

// 列宽 = 网格宽度 / 列数（每个.video有左右各8px padding，已包含在计算中）
const columnWidth = computed(() => {
  const width = gridWidth.value
  const cols = columnCount.value
  // 确保列宽是整数，并且能够整除
  return Math.floor(width / cols)
})
const rowHeight = 230
// 计算已加载视频的行数（只显示已加载的数据）
const rowCount = computed(() => Math.ceil(videos.value.length / columnCount.value))
// 虚拟滚动的高度：基于已加载的行数，最小高度为1行
const gridHeight = computed(() => {
  const rows = rowCount.value
  return rows > 0 ? rows * rowHeight : rowHeight
})
const fallbackCover = '/images/banner-1.jpg'
const getPaddingStyle = (columnIndex: number) => {
  return {
    paddingLeft: columnIndex === 0 ? '0px' : undefined,
    paddingRight: columnIndex === columnCount.value - 1 ? '0px' : undefined
  }
}

const onImgError = (evt: Event) => {
  const target = evt?.target as HTMLImageElement | null
  if (!target) return
  if ((target as any).__fallbackApplied) return
  ;(target as any).__fallbackApplied = true
  target.onerror = null
  target.src = fallbackCover
}

const formatDuration = (seconds?: number) => {
  if (!seconds || seconds <= 0) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const normalizeList = (data: any) => {
  const list = Array.isArray(data?.list) ? data.list : (Array.isArray(data) ? data : [])
  return list.map((item: any) => {
    const rawCover = (item?.coverUrl || '').trim()
    const safeCover = rawCover || fallbackCover
    const durationText = formatDuration(item?.duration)
    return {
      ...item,
      cover: safeCover,
      duration: durationText,
      playCount: typeof item?.viewCount === 'number' ? item.viewCount : '本地视频',
      up: item?.sourceFile || '本地文件',
      playUrl: item?.playUrl || '',
      id: item?.videoId || item?.id,
      isVideo: !!item?.playUrl
    }
  })
}

// 加载播放量最高的视频用于顶部轮播和右侧推荐
const loadTopVideos = async () => {
  try {
    const { data } = await fetchTopVideos(6)
    const list = Array.isArray(data?.list) ? data.list : []
    const mapped = list.map((item: any) => {
      const rawCover = (item?.coverUrl || '').trim()
      const safeCover = rawCover || fallbackCover
      const durationText = formatDuration(item?.duration)
      return {
        ...item,
        cover: safeCover,
        title: item?.title || '本地视频',
        duration: durationText,
        id: item?.videoId || item?.id
      }
    })
    slides.value = mapped
    recommends.value = mapped
  } catch (e) {
    // 失败时保持默认空状态，主列表仍可正常加载
  }
}

const fetchVideosData = async (reset = false) => {
  if (loadingVideos.value || loadingMore.value) return
  if (reset) {
    page.value = 1
    finished.value = false
    videos.value = []
    totalCount.value = 0
  }
  const isFirstPage = page.value === 1
  if (isFirstPage) loadingVideos.value = true
  else loadingMore.value = true
  try {
    const { data } = await fetchVideos(page.value, pageSize)
    const mapped = normalizeList(data)
    videos.value = [...videos.value, ...mapped]
    const total = typeof data?.total === 'number' ? data.total : undefined
    if (typeof total === 'number') {
      totalCount.value = total
    }
    if ((total && videos.value.length >= total) || mapped.length < pageSize) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (e) {
    if (isFirstPage) videos.value = []
  } finally {
    loadingVideos.value = false
    loadingMore.value = false
  }
}

// 使用 Intersection Observer 检测底部元素
const loadMoreTrigger = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

const setupIntersectionObserver = () => {
  if (!loadMoreTrigger.value) return
  
  // 清理旧的 observer
  if (observer) {
    observer.disconnect()
  }
  
  // 创建新的 observer
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      // 当底部元素进入视口时，加载更多
      if (entry.isIntersecting && !loadingVideos.value && !loadingMore.value && !finished.value) {
        fetchVideosData()
      }
    },
    {
      root: null, // 使用视口作为根
      rootMargin: '100px', // 提前100px触发
      threshold: 0.1
    }
  )
  
  observer.observe(loadMoreTrigger.value)
}

// 监听 videos 变化，重新设置 observer
watch(
  () => videos.value.length,
  () => {
    if (!finished.value) {
      nextTick(() => {
        setupIntersectionObserver()
        updateActualWidth()
      })
    }
  }
)

// 监听容器宽度变化，更新实际宽度
watch(
  () => containerWidth.value,
  () => {
    nextTick(() => {
      updateActualWidth()
    })
  }
)

// 监听 finished 状态，如果已完成则停止观察
watch(
  () => finished.value,
  (isFinished) => {
    if (isFinished && observer) {
      observer.disconnect()
      observer = null
    } else if (!isFinished) {
      nextTick(() => {
        setupIntersectionObserver()
      })
    }
  }
)

// 窗口大小变化处理函数
let handleResize: (() => void) | null = null

onMounted(() => {
  timer = setInterval(next, 4000)
  fetchVideosData()
  loadTopVideos()
  // 等待 DOM 渲染后设置 observer
  nextTick(() => {
    setupIntersectionObserver()
    // 更新实际容器宽度
    updateActualWidth()
  })
  // 监听窗口大小变化
  handleResize = () => {
    windowWidth.value = window.innerWidth
    // 延迟更新，等待DOM更新完成
    nextTick(() => {
      updateActualWidth()
    })
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (observer) {
    observer.disconnect()
    observer = null
  }
  if (handleResize) {
    window.removeEventListener('resize', handleResize)
    handleResize = null
  }
})

const openInNewTab = (path: string) => {
  const base = (window as any).__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${normalizedBase}${normalizedPath || '/'}`
  window.open(url, '_blank')
}

const playTopVideo = (video: any) => {
  if (!video || !video.id) return
  openInNewTab(`/video/${encodeURIComponent(video.id)}`)
}

// 导航到其他页面
const goTo = (path: string) => { 
  openInNewTab(path) 
}

const navigateToCreatorCenter = () => {
  openInNewTab('/submitHome?view=contentManagement')
}

// 播放视频（列表区域）
const playVideo = (video: any) => {
  if (!video || !video.id) return
  openInNewTab(`/video/${encodeURIComponent(video.id)}`)
}
</script>

<style lang="scss" scoped>
.home {
  position: relative;
  background: #fff;
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  margin: 0 auto;
}

.site-header {
  position: relative;
  height: 156px;

  .header-bg {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 156px;
    object-fit: cover;
  }
}

.navigation-section {
  max-width: 1350px;
  width: 100%;
  margin: -30px auto 0;
  padding: 0 20px;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-top: 20px;
  overflow-y: hidden;

  .nav-left-section {
    display: flex;
    flex-direction: column;
    gap: 10px;
    flex: 1;
    overflow-y: hidden;

    .nav-left-top {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .quick-icons {
      margin: 0;
      margin-left: 0px;
      margin-right: 20px;
      display: flex;
      gap: 20px;

      .qi-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;

        .qi-circle {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;

          &.orange {
            background: #ff9212;
          }

          &.pink {
            background: #f07775;
          }

          .qi-icon {
            width: 23px;
            height: 23px;
            filter: brightness(0) invert(1);
          }
        }

        .qi-text {
          font-size: 14px;
          color: #222;
        }
      }
    }

    .grid-container {
      display: grid;
      grid-template-columns: repeat(11, 1fr);
      grid-template-rows: repeat(2, 1fr);
      gap: 8px;
      width: 100%;

      .grid-item {
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #F6F7F8;
        border-radius: 8px;
        padding: 8px 12px;
        text-decoration: none;
        color: #61666D;
        font-size: 14px;
        letter-spacing: 2px;
        transition: color 0.3s;
        height: 15px;
        white-space: nowrap;
        &:hover {
            color: #000000;
        }
      }
    }
  }

  .nav-right-section {
    flex-shrink: 0;
    padding-left: 10px;
    border-left: 1.5px solid #eee;
    overflow-y: hidden;

    .utility-links {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .utility-row {
        display: flex;
        gap: 12px;

        .utility-item {
          display: flex;
          flex-direction: row;
          align-items: center;
          gap: 6px;
          cursor: pointer;
          padding: 4px 8px;
          border-radius: 6px;
          transition: background-color 0.2s;

          &:hover {
            background-color: #f5f5f5;
          }

          .utility-icon {
            width: 16px;
            height: 16px;
            filter: grayscale(100%) brightness(0.7);
          }

          .utility-text {
            font-size: 14px;
            color: #61666d;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

/* 顶部区域：5列网格，轮播占两列两行 */
.hero-grid {
  max-width: 1350px;
  width: 100%;
  margin: 20px auto 20px;
  padding: 0 20px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  /* 每个卡片的行高自适应内容 */
  grid-auto-rows: auto;
  gap: 16px;
  overflow-y: hidden;
  /* 防止子项内容撑破导致列宽不一致 */
  > * { min-width: 0; }

    .banner {
    position: relative;
    background: linear-gradient(135deg, #2b2b3a, #5b6bd5);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, .08);
    grid-column: 1 / span 2;  /* 占两列 */
    grid-row: 1 / span 3;
    width: 100%;
    height: 85%;

      .slider {
      position: absolute;
      inset: 0;
      display: flex;
      transition: transform .45s ease;

      .slide {
          min-width: 100%;
          position: relative;
          cursor: pointer;

          .thumb-wrap {
            position: relative;
            width: 100%;
            height: 100%;
            border-radius: 8px;
            overflow: hidden;
            background: #f1f2f3;

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
              font-size: 12px;
              color: #fff;
              background: rgba(0, 0, 0, .55);
              padding: 2px 6px;
              border-radius: 4px;
              z-index: 2;
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
            }

            &:hover .play-overlay {
              opacity: 1;
            }
          }

          .slide-caption {
            position: absolute;
            left: 16px;
            bottom: 12px;
            background: rgba(0, 0, 0, .45);
            color: #fff;
            padding: 6px 10px;
            font-size: 12px;
            border-radius: 4px;
            backdrop-filter: blur(2px);
          }
        }
    }

    .arrow {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: 32px;
      height: 32px;
      border-radius: 50%;
      border: 0;
      background: rgba(0, 0, 0, .35);
      color: #fff;
      cursor: pointer;

      &.left {
        left: 8px;
      }

      &.right {
        right: 8px;
      }
    }

    .indicators {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 10px;
      display: flex;
      justify-content: center;
      gap: 6px;

      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: rgba(255, 255, 255, .5);
        cursor: pointer;

        &.active {
          background: #fff;
        }
      }
    }
  }

  .recommend {
    display: contents; /* 将推荐卡片直接放入网格，让其占据网格单元 */

    .top-video {
      display: grid;
      grid-template-rows: minmax(0, 1fr) auto auto;
      gap: 6px;
      cursor: pointer;
      min-height: 0;
      
      .thumb-wrap {
        position: relative;
        width: 100%;
        padding-bottom: 56%;
        border-radius: 8px;
        overflow: hidden;
        background: #f1f2f3;

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
          font-size: 12px;
          color: #fff;
          background: rgba(0, 0, 0, .55);
          padding: 2px 6px;
          border-radius: 4px;
          z-index: 2;
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
        }

        &:hover .play-overlay {
          opacity: 1;
        }
      }

      .v-title { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .v-sub { font-size: 12px; color: #8a8a8a; }
    }
  }
}

.section {
  max-width: 1350px;
  width: 100%;
  margin: 80px auto 40px;
  padding: 0 20px;
  box-sizing: border-box;

  .video-virtual-wrapper {
    width: 100%;
    margin: 0;
    padding: 0;
    display: flex;
    justify-content: flex-start;
    overflow: visible; /* 交给页面滚动 */
    box-sizing: border-box;
  }

  .video-virtual-grid {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden !important; /* 禁止独立滚动，统一跟随页面 */
    overflow-y: hidden !important;
    overflow-x: hidden !important;
    /* 高度由组件动态计算，不强制设置 */
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
      display: none;
      width: 0;
      height: 0;
    }

    /* 组件内部类名被作用域隔离，使用 :deep 隐藏所有虚拟滚动条 */
    :deep(.el-virtual-scrollbar),
    :deep(.el-virtual-scrollbar__thumb),
    :deep(.el-virtual-scrollbar__bar) {
      display: none !important;
      width: 0 !important;
      height: 0 !important;
      opacity: 0 !important;
    }

    :deep(.el-vl__scrollbar) {
      display: none !important;
      width: 0 !important;
      height: 0 !important;
      opacity: 0 !important;
    }

    :deep(.el-vl__window),
    :deep(.el-vl__wrapper) {
      scrollbar-width: none;
      -ms-overflow-style: none;
      overflow: visible !important; /* 交给外层页面滚动 */
      height: auto !important;
      max-height: none !important;
      position: static !important; /* 避免内部定位影响整体高度 */
      overflow-y: visible !important;
      overflow-x: visible !important;
    }

    /* 部分版本还会在 wrap 上强制 overflow:auto，这里兜底 */
    :deep(.el-virtual-scrollbar__wrap),
    :deep(.el-virtual-scrollbar__wrap--horizontal) {
      overflow: visible !important;
      height: auto !important;
      max-height: none !important;
      overflow-y: visible !important;
      overflow-x: visible !important;
    }

    /* 兜底处理 el-virtual-list 自身可能的 overflow 设置 */
    :deep(.el-virtual-list),
    :deep(.el-virtual-list__window),
    :deep(.el-virtual-list__wrapper) {
      overflow: visible !important;
      overflow-y: visible !important;
      overflow-x: visible !important;
      height: auto !important;
      max-height: none !important;
      position: static !important;
    }

    /* 彻底禁用子容器滚动条 */
    :deep(.el-vl__window::-webkit-scrollbar),
    :deep(.el-vl__wrapper::-webkit-scrollbar) {
      display: none;
      width: 0;
      height: 0;
    }

    :deep(.el-vl__window::-webkit-scrollbar),
    :deep(.el-vl__wrapper::-webkit-scrollbar) {
      display: none;
      width: 0;
      height: 0;
    }
  }

  .video {
    padding: 8px;
    box-sizing: border-box;
    width: 100%;
    overflow: hidden;

    .card {
      display: grid;
      grid-template-rows: auto auto auto;
      gap: 6px;
      cursor: pointer;
    }

    .thumb-wrap {
      position: relative;
      width: 100%;
      padding-bottom: 56%;
      border-radius: 8px;
      overflow: hidden;
      background: #f1f2f3;

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
        font-size: 12px;
        color: #fff;
        background: rgba(0, 0, 0, .55);
        padding: 2px 6px;
        border-radius: 4px;
        z-index: 2;
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
      }

      &:hover .play-overlay {
        opacity: 1;
      }
    }

    .v-title {
      font-size: 13px;
      color: #222;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .v-sub {
      font-size: 12px;
      color: #8a8a8a;
    }
  }

  .loading-bar {
    text-align: center;
    color: #8a8a8a;
    padding: 12px 0;
  }

  .load-more-trigger {
    height: 1px;
    width: 100%;
    visibility: hidden;
    pointer-events: none;
  }
}

/* 固定阈值以下不再自适应，保持 1200px 布局 */

/* 定义跳动动画，保持与 HomeView.vue 一致 */
@keyframes jump {
  0% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
  100% { transform: translateY(0); }
}

/* 大屏幕响应式：当屏幕宽度超过1800px时，让内容区域占据75%宽度 */
@media (min-width: 1800px) {
  .navigation-section,
  .hero-grid,
  .section {
    max-width: 75%;
  }

  .section .video-virtual-wrapper {
    width: 100%;
  }
  
  .navigation-section,
  .hero-grid,
  .section {
    padding: 0 20px;
    box-sizing: border-box;
  }
}
</style>