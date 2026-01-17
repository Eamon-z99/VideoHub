<template>
  <div class="history-page">
    <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    <div class="history-container">
      <div class="history-main">
        <!-- 二级导航栏 -->
        <nav class="secondary-nav">
          <div class="nav-left">
            <h1 class="page-title">历史记录</h1>
            <div class="tabs">
              <button 
                class="tab" 
                :class="{ active: activeTab === 'all' }" 
                @click="activeTab = 'all'"
              >
                综合
              </button>
              <button 
                class="tab" 
                :class="{ active: activeTab === 'video' }" 
                @click="activeTab = 'video'"
              >
                视频
              </button>
              <button 
                class="tab" 
                :class="{ active: activeTab === 'live' }" 
                @click="activeTab = 'live'"
              >
                直播
              </button>
              <button 
                class="tab" 
                :class="{ active: activeTab === 'column' }" 
                @click="activeTab = 'column'"
              >
                专栏
              </button>
              <button class="tab more-filter">
                更多筛选
              </button>
            </div>
          </div>
          <div class="nav-right">
            <input 
              v-model="keyword" 
              class="search-input" 
              placeholder="搜索标题/up主昵称" 
            />
            <button class="action-btn" @click="clearAll">清空历史</button>
            <button class="action-btn">批量管理</button>
          </div>
        </nav>

        <!-- 主内容区域 -->
        <div class="content-wrapper">
          <section class="content">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="filteredGroups.length === 0" class="empty">
              <p>暂无历史记录</p>
            </div>
            <div v-else>
              <div v-for="group in filteredGroups" :key="group.key" class="day-group">
                <div class="day-title">{{ group.title }}</div>
                <div class="grid">
                  <div 
                    v-for="item in group.items" 
                    :key="item.id" 
                    class="card"
                    @click="goToVideo(item.videoId)"
                  >
                    <div class="thumb">
                      <img :src="item.cover" :alt="item.title" />
                      <span v-if="item.duration" class="duration">{{ item.duration }}</span>
                      <div v-if="item.progressPercent > 0" class="progress-bar">
                        <div class="progress-fill" :style="{ width: item.progressPercent + '%' }"></div>
                      </div>
                    </div>
                    <div class="meta">
                      <div class="title" :title="item.title">{{ item.title }}</div>
                      <div class="sub">
                        <span class="time">{{ item.time }}</span>
                        <span v-if="item.isWatched" class="watched-badge">已看完</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>

      <aside class="sidebar">
        <button class="feedback-btn">新版反馈</button>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import { useUserStore } from '@/stores/user'
import { getHistoryList, clearAllHistory } from '@/api/history'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('all')
const keyword = ref('')
const historyList = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 按日期分组历史记录
const groups = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const weekAgo = new Date(today)
  weekAgo.setDate(weekAgo.getDate() - 7)

  const todayItems = []
  const yesterdayItems = []
  const weekItems = []
  const olderItems = []

  historyList.value.forEach(item => {
    const itemDate = new Date(item.lastWatchTime)
    if (itemDate >= today) {
      todayItems.push(item)
    } else if (itemDate >= yesterday) {
      yesterdayItems.push(item)
    } else if (itemDate >= weekAgo) {
      weekItems.push(item)
    } else {
      olderItems.push(item)
    }
  })

  const result = []
  if (todayItems.length > 0) {
    result.push({ key: 'today', title: '今天', items: todayItems })
  }
  if (yesterdayItems.length > 0) {
    result.push({ key: 'yesterday', title: '昨天', items: yesterdayItems })
  }
  if (weekItems.length > 0) {
    result.push({ key: 'week', title: '近一周', items: weekItems })
  }
  if (olderItems.length > 0) {
    result.push({ key: 'older', title: '更早', items: olderItems })
  }
  return result
})

const filteredGroups = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return groups.value
  return groups.value.map(g => ({
    ...g,
    items: g.items.filter(i =>
      i.title.toLowerCase().includes(kw)
    )
  })).filter(g => g.items.length > 0)
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
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

// 加载历史记录
const loadHistory = async () => {
  if (!userStore.isAuthenticated) {
    return
  }
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) {
    console.warn('用户未登录或用户ID不存在')
    return
  }

  loading.value = true
  try {
    const response = await getHistoryList(userId, page.value, pageSize.value)
    if (response.data.success) {
      historyList.value = response.data.list || []
      total.value = response.data.total || 0
      
      // 转换数据格式以适配前端显示
      historyList.value = historyList.value.map(item => ({
        id: item.id,
        videoId: item.videoId,
        title: item.title,
        cover: item.coverUrl || '/assets/home.png',
        duration: formatDuration(item.duration),
        time: formatTime(item.lastWatchTime),
        progressPercent: item.progressPercent || 0,
        isWatched: item.isWatched || false,
        watchCount: item.watchCount || 1
      }))
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 清空历史
const clearAll = async () => {
  if (!userStore.isAuthenticated) {
    return
  }
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) {
    return
  }

  if (!confirm('确定要清空所有历史记录吗？')) {
    return
  }

  try {
    const response = await clearAllHistory(userId)
    if (response.data.success) {
      historyList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('清空历史记录失败:', error)
    alert('清空失败，请稍后重试')
  }
}

// 跳转到视频播放页
const goToVideo = (videoId) => {
  router.push(`/video/${encodeURIComponent(videoId)}`)
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped lang="scss">
.history-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  margin: 0 auto;
  background: #fff;
  padding-top: 64px;

  .history-container {
    display: flex;
    gap: 0;
    padding: 20px 0;
    max-width: 1000px;
    min-width: 900px;
    width: 100%;
    margin: 0 auto;
    justify-content: center;
    align-items: flex-start;
  }

  .history-main {
    flex: 0 0 auto;
    width: 100%;
    max-width: 1000px;
    background: #fff;
    border-radius: 0;
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
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;

    &:hover {
      background: #f5f5f5;
    }

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
    width: 200px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid #e3e3e3;
    border-radius: 4px;
    font-size: 14px;
    outline: none;

    &:focus {
      border-color: #00a1d6;
    }
  }

  .action-btn {
    height: 32px;
    padding: 0 16px;
    border: 1px solid #e3e3e3;
    background: #fff;
    border-radius: 4px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #00a1d6;
      color: #00a1d6;
    }
  }

  .sidebar {
    display: none;
  }

  .feedback-btn {
    position: fixed;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    padding: 12px 16px;
    background: #fff;
    border: 1px solid #e3e3e3;
    border-radius: 8px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.2s;

    &:hover {
      border-color: #00a1d6;
      color: #00a1d6;
    }
  }

  .content-wrapper {
    min-width: 0;
  }

  .content {
    min-width: 0;
    max-width: calc(5 * 258px + 4 * 16px);
    margin: 0 auto;

    .day-group { 
      margin: 0 0 32px 0; 
    }
    
    .day-title { 
      font-size: 16px; 
      font-weight: 600;
      color: #222; 
      margin-bottom: 16px;
      padding-left: 0;
    }

    .grid {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      justify-content: flex-start;
      max-width: calc(5 * 258px + 4 * 16px); // 最多5个视频：5 * 258 + 4 * 16 = 1354px
      margin: 0 auto;

      .card {
        width: calc((100% - 16px * 3) / 4);
        max-width: 258px;
        min-width: calc((100% - 16px * 4) / 5);
        background: transparent;
        border: none;
        border-radius: 0;
        overflow: visible;
        cursor: pointer;
        transition: transform 0.2s;
        display: flex;
        flex-direction: column;

        &:hover {
          transform: translateY(-2px);
        }

        .thumb {
          position: relative;
          width: 100%;
          height: 145px;
          background: #f7f7f7;
          border-radius: 8px;
          overflow: hidden;
          margin-bottom: 8px;
          flex-shrink: 0;

          img { 
            width: 100%; 
            height: 100%; 
            object-fit: cover; 
          }
          
          .duration { 
            position: absolute; 
            right: 6px; 
            bottom: 6px; 
            background: rgba(0,0,0,0.65); 
            color: #fff; 
            padding: 2px 6px; 
            border-radius: 4px; 
            font-size: 12px; 
            z-index: 2;
          }
          
          .progress-bar {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: rgba(0,0,0,0.2);
            z-index: 1;
          }
          
          .progress-fill {
            height: 100%;
            background: #00a1d6;
            transition: width 0.3s;
          }
        }

        .meta {
          padding: 0;
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: flex-start;
          min-width: 0;
          
          .title { 
            font-size: 13px; 
            color: #222; 
            line-height: 1.4; 
            height: 36px; 
            overflow: hidden; 
            display: -webkit-box; 
            -webkit-line-clamp: 2; 
            -webkit-box-orient: vertical;
            margin-bottom: 4px;
          }
          
          .sub { 
            color: #999; 
            font-size: 12px; 
            overflow: hidden; 
            text-overflow: ellipsis; 
            white-space: nowrap; 
            display: flex;
            align-items: center;
            gap: 8px;
          }
          
          .sub .dot { margin: 0 4px; }
          
          .watched-badge {
            background: #f0f0f0;
            color: #666;
            padding: 2px 6px;
            border-radius: 2px;
            font-size: 11px;
          }
        }
      }
    }
  }
  
  .loading, .empty {
    text-align: center;
    padding: 60px 20px;
    color: #999;
    font-size: 14px;
  }
}

// @media (max-width: 1600px) {
//   .history-page {
//     min-width: 100%;
//     max-width: 100%;
//   }
// }

// @media (max-width: 1280px) {
//   .history-page {
//     .content {
//       .grid { grid-template-columns: repeat(4, 1fr); }
//     }
//   }
// }

// @media (max-width: 960px) {
//   .history-page {
//     .history-container {
//       flex-direction: column;
//     }

//     .sidebar {
//       width: 100%;
//     }

//     .content {
//       .grid { grid-template-columns: repeat(3, 1fr); }
//     }
//   }
// }
</style>


