<template>
  <div class="home">
    <header class="site-header">
      <div class="header-inner">
        <ul class="nav-left">
          <li class="nav-item" @click="goTo('/')">
            <img src="/assets/home.png" class="nav-icon" />
            <span>首页</span>
          </li>
          <li class="nav-item"><span>番剧</span></li>
          <li class="nav-item" @click="goTo('/live')"><span>直播</span></li>
          <li class="nav-item"><span>游戏中心</span></li>
          <li class="nav-item" @click="goTo('/mall')"><span>会员购</span></li>
          <li class="nav-item"><span>漫画</span></li>
          <li class="nav-item"><span>赛事</span></li>
          <li class="nav-item">
            <img src="/assets/download-client.png" class="nav-icon" />
            <span>下载客户端</span>
          </li>
        </ul>
        <div class="search">
          <input class="search-input" placeholder="搜索你感兴趣的内容" />
          <button class="search-btn">
            <!-- 🔍 -->
            <img src="/assets/search-button.png" class="search-btn-img"/>
          </button>
        </div>
        <div class="actions">
          <div 
            class="user-area" 
            @click="handleUserClick"
            @mouseenter="showUserDropdown = true"
            @mouseleave="handleUserAreaLeave"
            v-if="isAuthenticated"
          >
            <div class="avatar" />
            <span class="user-name">{{ displayName }}</span>
            <UserDropdown 
              v-model:visible="showUserDropdown"
              @close="showUserDropdown = false"
              @mouseenter="handleDropdownEnter"
            />
          </div>
          <div 
            class="user-area" 
            @click="handleUserClick"
            v-else
          >
            <div class="avatar" />
          </div>
          <div class="action-col" @click="goTo('/vip')">
            <img src="/assets/vip.png" class="action-icon" /><span>大会员</span>
          </div>
          <div class="action-col" @click="goTo('/messages')">
            <img src="/assets/messages.png" class="action-icon" /><span>消息</span>
          </div>
          <div class="action-col" @click="goTo('/feed')">
            <img src="/assets/feed.png" class="action-icon" /><span>动态</span>
          </div>
          <div class="action-col" @click="goTo('/profile')">
            <img src="/assets/favorites.png" class="action-icon" /><span>收藏</span>
          </div>
          <div class="action-col" @click="goTo('/history')">
            <img src="/assets/history.png" class="action-icon" /><span>历史</span>
          </div>
          <div class="action-col" @click="navigateToCreatorCenter">
            <img src="/assets/creator-center.png" class="action-icon" /><span>创作中心</span>
          </div>
          <button class="primary" @click="goTo('/submitHome?view=submit')">投稿</button>
        </div>
      </div>
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
      <div class="video-virtual-wrapper">
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
              <div v-if="data && data[rowIndex * columnCount + columnIndex]" class="card" @click="playVideo(data[rowIndex * columnCount + columnIndex])">
                <div class="thumb-wrap">
                  <img
                    :src="data[rowIndex * columnCount + columnIndex].cover"
                    loading="lazy"
                    @error="onImgError"
                  />
                  <span class="duration">{{ data[rowIndex * columnCount + columnIndex].duration }}</span>
                  <div v-if="data[rowIndex * columnCount + columnIndex].isVideo" class="play-overlay">
                    <div class="play-button">▶</div>
                  </div>
                </div>
                <div class="v-title" :title="data[rowIndex * columnCount + columnIndex].title">{{ data[rowIndex * columnCount + columnIndex].title }}</div>
                <div class="v-sub">{{ data[rowIndex * columnCount + columnIndex].playCount }} · {{ data[rowIndex * columnCount + columnIndex].up }}</div>
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


  <!-- 登录组件 -->
  <!-- <Login v-if="showLogin" @close="showLogin=false" /> -->
  <Login v-model:show="showLogin" @close="showLogin=false" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
// @ts-ignore Element Plus 未在主导出暴露虚拟列表，子路径命名导出
import { FixedSizeGrid as ElVirtualGrid } from 'element-plus/es/components/virtual-list/index.mjs'
import { useRouter } from 'vue-router'
import Login from '@/components/Login.vue'
import UserDropdown from '@/components/UserDropdown.vue'
import { fetchVideos, fetchTopVideos } from '@/api/video'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

let showLogin = ref(false)
let showUserDropdown = ref(false)
let dropdownTimer: any = null

const isAuthenticated = computed(() => userStore.isAuthenticated)
const displayName = computed(() => {
  const user = (userStore as any).user || {}
  return user.username || user.loginAccount || '未登录'
})

const handleUserClick = () => {
  if (!isAuthenticated.value) {
    // 未登录：打开登录弹窗
    showLogin.value = true
    return
  }
  // 已登录：显示下拉菜单（如果未显示）
  if (!showUserDropdown.value) {
    showUserDropdown.value = true
  }
}

const handleUserAreaLeave = () => {
  // 延迟关闭，给鼠标移动到弹窗的时间
  dropdownTimer = setTimeout(() => {
    showUserDropdown.value = false
  }, 200)
}

const handleDropdownEnter = () => {
  // 鼠标进入下拉菜单，取消关闭操作
  if (dropdownTimer) {
    clearTimeout(dropdownTimer)
    dropdownTimer = null
  }
}

// 监听弹窗显示状态，清除定时器
watch(showUserDropdown, (val) => {
  if (val && dropdownTimer) {
    clearTimeout(dropdownTimer)
    dropdownTimer = null
  }
})

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
const columnCount = 5
const gridWidth = 1350
const columnWidth = Math.floor(gridWidth / columnCount)
const rowHeight = 230
// 计算已加载视频的行数（只显示已加载的数据）
const rowCount = computed(() => Math.ceil(videos.value.length / columnCount))
// 虚拟滚动的高度：基于已加载的行数，最小高度为1行
const gridHeight = computed(() => {
  const rows = rowCount.value
  return rows > 0 ? rows * rowHeight : rowHeight
})
const fallbackCover = '/images/banner-1.jpg'
const getPaddingStyle = (columnIndex: number) => {
  return {
    paddingLeft: columnIndex === 0 ? '0px' : undefined,
    paddingRight: columnIndex === columnCount - 1 ? '0px' : undefined
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
      })
    }
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

onMounted(() => {
  timer = setInterval(next, 4000)
  fetchVideosData()
  loadTopVideos()
  // 等待 DOM 渲染后设置 observer
  nextTick(() => {
    setupIntersectionObserver()
  })
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

const playTopVideo = (video: any) => {
  if (!video || !video.id) return
  router.push(`/video/${encodeURIComponent(video.id)}`)
}

// 导航到创作中心
const goTo = (path: string) => { router.push(path) }

const navigateToCreatorCenter = () => {
  router.push('/submitHome?view=contentManagement')
}

// 播放视频（列表区域）
const playVideo = (video: any) => {
  if (!video || !video.id) return
  router.push(`/video/${encodeURIComponent(video.id)}`)
}
</script>

<style lang="scss" scoped>
.home {
  background: #fff;
  min-width: 1600px;
  max-width: 1800px;
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

  .header-inner {
    position: relative;
    z-index: 1;
    height: 64px;
    display: grid;
    grid-template-columns: auto 1fr auto;
    align-items: center;
    gap: 12px;
    padding: 8px 24px;
  }

  .nav-left {
    display: flex;
    gap: 20px;
    list-style: none;
    padding: 0;
    margin-left: 1vw;
    align-items: center;
  }

  .nav-item {
    display: flex;
    align-items: center;
    color: #fff;
    font-size: 14px;
    gap: 6px;
    cursor: pointer;

    span {
      transition: color .2s;
    }

    &:hover span {
      color: #00a1d6;
      animation: jump 0.3s ease;
    }
  }

  .nav-icon {
    width: 18px;
    height: 18px;
    filter: brightness(0) invert(1);
  }

  .search {
    display: grid;
    grid-template-columns: 1fr 40px;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    width: 500px;
    margin: 0 auto;

    .search-input {
      height: 36px;
      padding: 0 12px;
      border: 0;
      outline: none;
      font-size: 14px;
    }

    .search-btn {
      margin-left: 4px;
      margin-top: 4px;
      border: 0;
      background: transparent;
      cursor: pointer;
      font-size: 16px;
      padding: 8px;
      width: 25px;
      height: 25px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background-color: #f5f5f5;
      }

      .search-btn-img {
        width: 20px;
        height: 20px;
        margin-top: 4px;
      }
    }
  }

  .actions {
    display: flex;
    gap: 20px;
    align-items: center;
    margin-right: 1vw;
  }

  .user-area {
    position: relative;
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 20px;
    transition: background 0.2s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
    
    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #d8d8d8;
      border: 2px solid rgba(255, 255, 255, .8);
      flex-shrink: 0;
    }
    
    .user-name {
      font-size: 13px;
      color: #fff;
      white-space: nowrap;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .action-col {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #fff;
    gap: 4px;
    font-size: 12px;

    span {
      transition: color .2s;
    }

    &:hover span {
      color: #00a1d6;
      animation: jump 0.3s ease;
    }

    &:hover .action-icon {
      animation: jump 0.3s ease;
    }
  }

  .action-icon {
    width: 19px;
    height: 19px;
    /* 轻微发光，略细于上一版 */
    filter: brightness(0) invert(1) drop-shadow(0 0 0.3px rgba(255, 255, 255, 0.85));
  }

  .action {
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.4);
    color: #fff;
    padding: 6px 10px;
    border-radius: 6px;
    cursor: pointer;
  }

  .primary {
    background: #fb7299;
    border: none;
    color: #fff;
    padding: 6px 12px;
    border-radius: 6px;
    cursor: pointer;
  }
}

.navigation-section {
  max-width: 1350px;
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
  margin: 20px auto 20px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  /* 顶部每个卡片的行高，轮播图将占两行 */
  grid-auto-rows: 220px;
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
    grid-row: 1 / span 2;     /* 占两行 */
    /* 高度 = 两行高度 + 中间一条间距 */
    height: 390px;
    width: 100%;

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
      grid-template-rows: auto auto auto;
      gap: 6px;
      cursor: pointer;
      
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
  margin: 80px auto 40px;
  padding: 0 20px;

  .video-virtual-wrapper {
    max-width: 1350px;
    margin: 0 auto;
    padding: 0 0;
    display: flex;
    justify-content: center;
    overflow: visible; /* 交给页面滚动 */
  }

  .video-virtual-grid {
    width: 100%;
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
</style>