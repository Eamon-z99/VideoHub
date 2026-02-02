<template>
  <div class="feed-page">
    <div class="header-wrapper">
      <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    </div>
    <div ref="feedContainerRef" class="feed-container">
      <div class="feed-content">
      <aside class="left-column">
        <!-- inlined: UserCard.vue -->
        <div class="user-card">
          <div class="avatar" :style="{ backgroundImage: userAvatar ? `url(${userAvatar})` : 'none' }"></div>
          <div class="info">
            <div class="name">{{ userName || '未登录' }}</div>
            <div class="stats">
              <div><b>{{ userStats.follows || 0 }}</b><span>关注</span></div>
              <div><b>{{ userStats.fans || 0 }}</b><span>粉丝</span></div>
              <div><b>{{ userStats.posts || 0 }}</b><span>动态</span></div>
            </div>
          </div>
        </div>
        <nav class="left-menu">
          <ul>
            <li>全部动态</li>
            <li>视频投稿</li>
            <li>追番追剧</li>
            <li>专栏</li>
          </ul>
        </nav>
      </aside>

      <main class="center-column">
        <!-- inlined: Composer.vue -->
        <div class="composer">
          <input class="input" placeholder="好的标题更容易获得支持，选题20字" />
          <div class="actions">
            <button class="btn">发布</button>
          </div>
        </div>

        <!-- inlined: StoriesStrip.vue -->
        <div class="stories">
          <div v-for="n in 10" :key="n" class="story">
            <div class="bubble"></div>
            <div class="label">好友{{ n }}</div>
          </div>
        </div>

        <!-- inlined: FeedList.vue -->
        <div class="feed-list">
          <div v-if="loading && feedList.length === 0" class="loading">加载中...</div>
          <div v-else-if="feedList.length === 0" class="empty">暂无动态</div>
          <article v-for="item in feedList" :key="item.id" class="feed-card" @click="goToVideo(item.videoId)">
            <header class="meta">
              <div class="avatar" :style="{ backgroundImage: item.uploaderAvatar ? `url(${item.uploaderAvatar})` : 'none' }"></div>
              <div class="who">
                <div class="name">{{ item.uploaderName || '未知UP主' }}</div>
                <div class="sub">{{ formatTime(item.publishTime || item.createTime) }} · 投稿了视频</div>
              </div>
            </header>
            <div class="content">
              <div class="thumb" :style="{ backgroundImage: item.coverUrl ? `url(${item.coverUrl})` : 'none' }">
                <span v-if="item.duration" class="duration">{{ formatDuration(item.duration) }}</span>
              </div>
              <div class="title">{{ item.title || '无标题' }}</div>
            </div>
            <footer class="actions" @click.stop>
              <span>赞 {{ item.likeCount || 0 }}</span>
              <span>评 {{ item.commentCount || 0 }}</span>
              <span>转 {{ item.shareCount || 0 }}</span>
            </footer>
          </article>
          <!-- 加载更多触发器 -->
          <div 
            ref="loadMoreTrigger" 
            class="loading-bar"
          >
            <span v-if="loadingMore">加载中...</span>
            <span v-else-if="finished">已加载全部</span>
            <span v-else style="visibility: hidden;">加载更多</span>
          </div>
        </div>
      </main>

      <aside class="right-column">
        <!-- inlined: RightSidebar.vue -->
        <div class="right">
          <section class="card">
            <h4>社区中心</h4>
            <ul>
              <li>公告与规则</li>
              <li>活动广场</li>
              <li>意见反馈</li>
            </ul>
          </section>
          <section class="card">
            <h4>bilibili热搜</h4>
            <ol>
              <li v-for="n in 10" :key="n">热搜条目 {{ n }}</li>
            </ol>
          </section>
        </div>
      </aside>
      </div>
    </div>
  </div>
  
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import { useUserStore } from '@/stores/user'
import { fetchVideos } from '@/api/video'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userName = computed(() => {
  return userStore.user?.username || userStore.user?.nickname || '未登录'
})

const userAvatar = computed(() => {
  return userStore.user?.avatar || userStore.user?.avatarUrl || ''
})

const userStats = ref({
  follows: 0,
  fans: 0,
  posts: 0
})

// 动态列表
const feedList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '刚刚'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return ''
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) {
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m}:${s.toString().padStart(2, '0')}`
}

// 加载动态数据
const loadFeed = async (reset = false) => {
  if (loading.value || loadingMore.value) return

  if (reset) {
    page.value = 1
    finished.value = false
    feedList.value = []
  }

  const isFirstPage = page.value === 1
  if (isFirstPage) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const response = await fetchVideos(page.value, pageSize)
    const data = response.data || {}
    const list = Array.isArray(data.list) ? data.list : []
    
    // 转换数据格式
    const mappedList = list.map(item => ({
      id: item.id || item.videoId,
      videoId: item.videoId || item.id,
      title: item.title || '无标题',
      coverUrl: item.coverUrl || '/assets/home.png',
      duration: item.duration || 0,
      uploaderName: item.uploaderName || item.uploader || '未知UP主',
      uploaderAvatar: item.uploaderAvatar || '',
      publishTime: item.publishTime || item.createTime || new Date().toISOString(),
      likeCount: item.likeCount || item.likes || 0,
      commentCount: item.commentCount || item.comments || 0,
      shareCount: item.shareCount || item.shares || 0
    }))

    feedList.value = [...feedList.value, ...mappedList]

    // 判断是否已加载全部
    const total = typeof data.total === 'number' ? data.total : undefined
    if ((total && feedList.value.length >= total) || mappedList.length < pageSize) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (error) {
    console.error('加载动态失败:', error)
    if (isFirstPage) {
      feedList.value = []
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 跳转到视频播放页
const goToVideo = (videoId) => {
  if (videoId) {
    router.push(`/video/${encodeURIComponent(videoId)}`)
  }
}


// 使用 Intersection Observer 检测底部元素，实现无限滚动
const loadMoreTrigger = ref(null)
const feedContainerRef = ref(null)
let observer = null

const setupIntersectionObserver = () => {
  if (!loadMoreTrigger.value || !feedContainerRef.value) return
  
  if (observer) {
    observer.disconnect()
  }
  
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (entry.isIntersecting && !loading.value && !loadingMore.value && !finished.value) {
        loadFeed()
      }
    },
    {
      root: feedContainerRef.value, // 使用 .feed-container 作为滚动容器
      rootMargin: '100px',
      threshold: 0.1
    }
  )
  
  observer.observe(loadMoreTrigger.value)
}

// 监听动态列表变化，重新设置 observer
watch(
  () => feedList.value.length,
  () => {
    if (!finished.value) {
      nextTick(() => {
        setupIntersectionObserver()
      })
    }
  }
)

// 监听 finished 状态
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
  loadFeed(true)
  // 等待 DOM 渲染完成
  nextTick(() => {
    setupIntersectionObserver()
  })
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style lang="scss" scoped>
// SCSS Variables
$primary-color: #00aeec;
$background-color: #e7f3f5;
$white: #fff;
$text-primary: #333;
$text-secondary: #666;
$text-muted: #888;
$border-radius: 8px;
$border-radius-sm: 6px;
$spacing-xs: 6px;
$spacing-sm: 8px;
$spacing-md: 12px;
$spacing-lg: 16px;
$spacing-xl: 40px;
$container-width: 1200px;
$left-column-width: 260px;
$right-column-width: 300px;

// SCSS Mixins
@mixin card-style {
  background: $white;
  border-radius: $border-radius;
  padding: $spacing-md;
}

@mixin flex-center {
  display: flex;
  align-items: center;
}

@mixin flex-column {
  display: flex;
  flex-direction: column;
}

@mixin hover-effect {
  transition: all 0.2s ease;
  
  &:hover {
    opacity: 0.9;
  }
}

@mixin avatar-style($size) {
  width: $size;
  height: $size;
  border-radius: 50%;
  background: #ddd;
}

// Main Layout
.feed-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  height: 100vh;
  margin: 0 auto;
  background-image: url('/assets/history_bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  color: $text-primary; // 明确页面默认文字颜色，避免继承到过浅的颜色导致看不清
  display: flex;
  flex-direction: column;
  overflow: hidden; // 页面不允许滚动
}

.header-wrapper {
  flex-shrink: 0; // 顶部栏固定，不参与滚动
  position: relative;
  z-index: 1000;
  height: 64px; // 预留顶部栏高度，避免遮挡右侧滚动条顶部
}

.feed-container {
  flex: 1; // 占据剩余空间
  width: 100%; // 占据全屏宽度
  overflow-y: auto; // 模块 c 允许滚动
  overflow-x: hidden;
  // 使用浏览器原生滚动条（出现在最右侧）
  scrollbar-width: auto; // Firefox
}

.feed-content {
  width: $container-width;
  margin: 0 auto;
  display: grid;
  grid-template-columns: $left-column-width 1fr $right-column-width;
  gap: $spacing-lg;
  padding: $spacing-lg $spacing-lg $spacing-xl;
  box-sizing: border-box;
}

// Left Column
.left-column {
  @include flex-column;
  gap: $spacing-md;
  height: fit-content;
  align-self: start;
}

.left-menu {
  @include card-style;
  padding: $spacing-md 0;
  
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
  }
  
  li {
    padding: 10px $spacing-lg;
    cursor: pointer;
    color: $text-primary; // 提升对比度
    font-size: 14px;
    font-weight: 600;
    transition: background-color 0.2s ease;
    
    &:hover {
      background: #f5f7fa;
    }
  }
}

// Center Column
.center-column {
  @include flex-column;
  gap: $spacing-md;
  height: fit-content;
  align-self: start;
  min-width: 0; // 允许 grid 列收缩，防止溢出
  overflow: hidden; // 防止内容溢出
}

// Right Column
.right-column {
  @include flex-column;
  gap: $spacing-md;
  height: fit-content;
  align-self: start;
  
  .right {
    @include flex-column;
    gap: $spacing-md;
  }
  
  // 社区中心正常滚动
  .card:first-child {
    position: relative;
    flex-shrink: 0;
  }
  
  // 当滚动到热搜榜时，固定热搜榜
  .card:last-child {
    position: -webkit-sticky; // Safari 兼容
    position: sticky;
    top: $spacing-lg; // 固定在距离滚动容器顶部 16px 的位置
    align-self: flex-start;
    max-height: calc(100vh - 64px - $spacing-lg * 2);
    overflow-y: hidden;
    z-index: 10;
    flex-shrink: 0;
  }
}

// User Card Component
.user-card {
  @include card-style;
  
  .avatar {
    @include avatar-style(56px);
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }
  
  .info {
    margin-top: $spacing-sm;
    
    .name {
      font-weight: 600;
      color: $text-primary;
    }
    
    .stats {
      display: flex;
      gap: $spacing-lg;
      margin-top: $spacing-sm;
      
      div {
        @include flex-column;
        align-items: center;
      }
      
      span {
        color: $text-muted;
        font-size: 12px;
      }
    }
  }
}

// Composer Component
.composer {
  @include card-style;
  overflow: hidden; // 防止内容溢出
  box-sizing: border-box;
  
  .input {
    width: 100%;
    border: none;
    outline: none;
    background: #f7f8fa;
    padding: 10px $spacing-md;
    border-radius: $border-radius-sm;
    box-sizing: border-box; // 确保 padding 包含在宽度内
  }
  
  .actions {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-sm;
  }
  
  .btn {
    background: $primary-color;
    color: $white;
    border: none;
    padding: $spacing-xs 14px;
    border-radius: $border-radius-sm;
    cursor: pointer;
    @include hover-effect;
  }
}

// Stories Component
.stories {
  display: flex;
  gap: $spacing-md;
  @include card-style;
  overflow-x: auto;
  // 隐藏滚动条
  scrollbar-width: none; // Firefox
  -ms-overflow-style: none; // IE/Edge
  
  &::-webkit-scrollbar {
    display: none; // Chrome/Safari
  }
  
  .story {
    @include flex-column;
    align-items: center;
    gap: $spacing-xs;
    
    .bubble {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #e3e9ee;
    }
    
    .label {
      font-size: 12px;
      color: $text-secondary;
    }
  }
}

// Feed List Component
.feed-list {
  @include flex-column;
  gap: $spacing-md;
  
  .feed-card {
    @include card-style;
    
    .meta {
      @include flex-center;
      gap: 10px;
      
      .avatar {
        @include avatar-style(36px);
        background-size: cover;
        background-position: center;
        background-repeat: no-repeat;
      }
      
      .who {
        .name {
          font-weight: 600;
        }
        
        .sub {
          color: $text-muted;
          font-size: 12px;
        }
      }
    }
    
    .content {
      display: flex;
      gap: $spacing-md;
      margin-top: 10px;
      
      .thumb {
        width: 180px;
        height: 100px;
        background: #d9dee3;
        border-radius: $border-radius-sm;
        background-size: cover;
        background-position: center;
        background-repeat: no-repeat;
        position: relative;
        flex-shrink: 0;
        
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
      
      .title {
        font-size: 14px;
        line-height: 1.6;
      }
    }
    
    .actions {
      display: flex;
      gap: $spacing-lg;
      color: $text-secondary;
      font-size: 13px;
      margin-top: $spacing-sm;
      cursor: pointer;
      
      span {
        transition: color 0.2s;
        
        &:hover {
          color: $primary-color;
        }
      }
    }
  }
  
  .loading-bar {
    text-align: center;
    padding: 20px;
    color: $text-muted;
    font-size: 14px;
  }
  
  .loading, .empty {
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    font-size: 14px;
  }
}

// Right Sidebar Component
.right {
  @include flex-column;
  gap: $spacing-md;
  
  .card {
    @include card-style;
    
    h4 {
      margin: 0 0 $spacing-sm;
    }
    
    ul, ol {
      margin: 0;
      padding-left: 18px;
      color: $text-secondary;
    }
  }
}
</style>


