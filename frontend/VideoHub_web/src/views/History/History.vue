<template>
  <div class="history-page">
    <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    <div class="history-container">
      <div class="history-main">
        <!-- 批量管理头部 -->
        <div v-if="isBatchMode" class="batch-header">
          <div class="batch-left">
            <label class="select-all-label">
              <input 
                type="checkbox" 
                :checked="isAllSelected" 
                @change="toggleSelectAll"
              />
              <span>全选</span>
            </label>
            <span class="selected-count">已经选择 {{ selectedIds.length }} 条历史记录</span>
          </div>
          <div class="batch-right">
            <button class="batch-btn delete-btn" @click="batchDelete" :disabled="selectedIds.length === 0">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </button>
            <button class="batch-btn exit-btn" @click="exitBatchMode">退出管理</button>
          </div>
        </div>

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
              <button 
                class="tab more-filter" 
                :class="{ active: showMoreFilters }"
                @click="showMoreFilters = !showMoreFilters"
              >
                更多筛选
                <el-icon class="filter-icon" :class="{ rotate: showMoreFilters }">
                  <ArrowUp />
                </el-icon>
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
            <button class="action-btn" @click="enterBatchMode">批量管理</button>
          </div>
        </nav>

        <!-- 更多筛选区域 -->
        <div v-if="showMoreFilters" class="more-filters">
          <div class="filter-row">
            <span class="filter-label">时长：</span>
            <div class="filter-buttons">
              <button 
                v-for="duration in durationFilters" 
                :key="duration.value"
                class="filter-btn"
                :class="{ active: selectedDuration === duration.value }"
                @click="selectedDuration = duration.value"
              >
                {{ duration.label }}
              </button>
            </div>
          </div>
          <div class="filter-row">
            <span class="filter-label">时间：</span>
            <div class="filter-buttons">
              <button 
                v-for="time in timeFilters" 
                :key="time.value"
                class="filter-btn"
                :class="{ active: selectedTime === time.value }"
                @click="selectedTime = time.value"
              >
                {{ time.label }}
              </button>
              <div class="date-range">
                <input 
                  type="date" 
                  v-model="startDate" 
                  class="date-input"
                  placeholder="开始日期"
                />
                <span class="date-separator">至</span>
                <input 
                  type="date" 
                  v-model="endDate" 
                  class="date-input"
                  placeholder="结束日期"
                />
              </div>
            </div>
          </div>
        </div>

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
                    :class="{ 'batch-mode': isBatchMode }"
                    @click="!isBatchMode && goToVideo(item.videoId)"
                  >
                    <!-- 批量模式下的选择框 -->
                    <div v-if="isBatchMode" class="card-checkbox" @click.stop>
                      <input 
                        type="checkbox" 
                        :checked="selectedIds.includes(item.id)"
                        @change="toggleSelect(item.id)"
                      />
                    </div>
                    <div class="thumb" @click.stop="!isBatchMode && goToVideo(item.videoId)">
                      <img :src="item.cover" :alt="item.title" />
                      <span v-if="item.duration" class="duration">{{ item.duration }}</span>
                      <div v-if="item.progressPercent > 0 && item.progressPercent < 90" class="progress-bar">
                        <div class="progress-fill" :style="{ width: item.progressPercent + '%' }"></div>
                      </div>
                    </div>
                    <div class="meta">
                      <div class="title" :title="item.title">{{ item.title }}</div>
                      <div class="sub">
                        <span class="up">{{ item.up || '未知UP主' }}</span>
                        <span class="dot">·</span>
                        <span class="time">{{ item.time }}</span>
                      </div>
                    </div>
                    <!-- 删除图标 -->
                    <div class="delete-icon" @click.stop="deleteItem(item.id)">
                      <el-icon><Delete /></el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- 加载更多触发器 -->
            <div 
              ref="loadMoreTrigger" 
              class="loading-bar"
            >
              <span v-if="loadingMore">加载中...</span>
              <span v-else-if="finished">已加载全部</span>
              <span v-else style="visibility: hidden;">加载更多</span>
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
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, ArrowUp } from '@element-plus/icons-vue'
import TopHeader from '@/components/TopHeader.vue'
import { useUserStore } from '@/stores/user'
import { getHistoryList, clearAllHistory, deleteHistory, batchDeleteHistory } from '@/api/history'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const activeTab = ref('all')
const keyword = ref('')
const historyList = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 更多筛选相关
const showMoreFilters = ref(false)
const selectedDuration = ref('all') // all, <10, 10-30, 30-60, >60
const selectedTime = ref('all') // all, today, yesterday, week, custom
const startDate = ref('')
const endDate = ref('')

// 批量管理相关
const isBatchMode = ref(false)
const selectedIds = ref([])

const durationFilters = [
  { label: '全部时长', value: 'all' },
  { label: '10分钟以下', value: '<10' },
  { label: '10-30分钟', value: '10-30' },
  { label: '30-60分钟', value: '30-60' },
  { label: '60分钟以上', value: '>60' }
]

const timeFilters = [
  { label: '全部时间', value: 'all' },
  { label: '今天', value: 'today' },
  { label: '昨天', value: 'yesterday' },
  { label: '近一周', value: 'week' }
]

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
  let filtered = groups.value

  // 关键词筛选
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    filtered = filtered.map(g => ({
      ...g,
      items: g.items.filter(i =>
        i.title.toLowerCase().includes(kw) ||
        (i.up && i.up.toLowerCase().includes(kw))
      )
    })).filter(g => g.items.length > 0)
  }

  // 时长筛选
  if (selectedDuration.value !== 'all') {
    filtered = filtered.map(g => ({
      ...g,
      items: g.items.filter(item => {
        const duration = item.rawDuration || 0 // 原始秒数
        switch (selectedDuration.value) {
          case '<10':
            return duration < 600
          case '10-30':
            return duration >= 600 && duration <= 1800
          case '30-60':
            return duration > 1800 && duration <= 3600
          case '>60':
            return duration > 3600
          default:
            return true
        }
      })
    })).filter(g => g.items.length > 0)
  }

  // 时间筛选
  if (selectedTime.value !== 'all' || startDate.value || endDate.value) {
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const weekAgo = new Date(today)
    weekAgo.setDate(weekAgo.getDate() - 7)

    filtered = filtered.map(g => ({
      ...g,
      items: g.items.filter(item => {
        const itemDate = new Date(item.lastWatchTime)
        
        // 自定义日期范围
        if (startDate.value || endDate.value) {
          const start = startDate.value ? new Date(startDate.value) : null
          const end = endDate.value ? new Date(endDate.value) : null
          if (start) start.setHours(0, 0, 0, 0)
          if (end) end.setHours(23, 59, 59, 999)
          
          if (start && itemDate < start) return false
          if (end && itemDate > end) return false
          return true
        }

        // 预设时间筛选
        switch (selectedTime.value) {
          case 'today':
            return itemDate >= today
          case 'yesterday':
            return itemDate >= yesterday && itemDate < today
          case 'week':
            return itemDate >= weekAgo
          default:
            return true
        }
      })
    })).filter(g => g.items.length > 0)
  }

  return filtered
})

// 全选状态
const isAllSelected = computed(() => {
  if (filteredGroups.value.length === 0) return false
  const allItems = filteredGroups.value.flatMap(g => g.items)
  return allItems.length > 0 && allItems.every(item => selectedIds.value.includes(item.id))
})

// 切换全选
const toggleSelectAll = (e) => {
  const allItems = filteredGroups.value.flatMap(g => g.items)
  if (e.target.checked) {
    selectedIds.value = [...new Set([...selectedIds.value, ...allItems.map(item => item.id)])]
  } else {
    const idsToRemove = new Set(allItems.map(item => item.id))
    selectedIds.value = selectedIds.value.filter(id => !idsToRemove.has(id))
  }
}

// 切换单个选择
const toggleSelect = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

// 进入批量管理模式
const enterBatchMode = () => {
  isBatchMode.value = true
  selectedIds.value = []
}

// 退出批量管理模式
const exitBatchMode = () => {
  isBatchMode.value = false
  selectedIds.value = []
}

// 批量删除
const batchDelete = async () => {
  if (selectedIds.value.length === 0) return
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) return

  if (!confirm(`确定要删除选中的 ${selectedIds.value.length} 条历史记录吗？`)) {
    return
  }

  try {
    const response = await batchDeleteHistory(userId, selectedIds.value)
    if (response.data.success) {
      selectedIds.value = []
      await loadHistory(true) // 重置加载
    }
  } catch (error) {
    console.error('批量删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

// 删除单条记录
const deleteItem = async (id) => {
  if (!confirm('确定要删除这条历史记录吗？')) {
    return
  }

  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) return

  try {
    const response = await deleteHistory(id, userId)
    if (response.data.success) {
      // 从选中列表中移除
      const index = selectedIds.value.indexOf(id)
      if (index > -1) {
        selectedIds.value.splice(index, 1)
      }
      // 从列表中移除该项
      const itemIndex = historyList.value.findIndex(item => item.id === id)
      if (itemIndex > -1) {
        historyList.value.splice(itemIndex, 1)
      }
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

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
const loadHistory = async (reset = false) => {
  if (!userStore.isAuthenticated) {
    return
  }
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) {
    console.warn('用户未登录或用户ID不存在')
    return
  }

  // 如果正在加载，则跳过
  if (loading.value || loadingMore.value) {
    return
  }

  // 重置时清空数据
  if (reset) {
    page.value = 1
    finished.value = false
    historyList.value = []
    total.value = 0
  }

  const isFirstPage = page.value === 1
  if (isFirstPage) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const response = await getHistoryList(userId, page.value, pageSize.value)
    if (response.data.success) {
      const newList = response.data.list || []
      total.value = response.data.total || 0
      
      // 转换数据格式以适配前端显示
      const mappedList = newList.map(item => ({
        id: item.id,
        videoId: item.videoId,
        title: item.title,
        cover: item.coverUrl || '/assets/home.png',
        duration: formatDuration(item.duration),
        rawDuration: item.duration, // 保留原始时长（秒）用于筛选
        time: formatTime(item.lastWatchTime),
        lastWatchTime: item.lastWatchTime, // 保留原始时间用于筛选
        progressPercent: item.progressPercent || 0,
        isWatched: item.isWatched || false,
        watchCount: item.watchCount || 1,
        up: item.uploaderName || '未知UP主'
      }))

      // 追加数据而不是替换
      historyList.value = [...historyList.value, ...mappedList]

      // 判断是否已加载全部
      if ((total.value && historyList.value.length >= total.value) || mappedList.length < pageSize.value) {
        finished.value = true
      } else {
        page.value += 1
      }
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    if (isFirstPage) {
      historyList.value = []
    }
  } finally {
    loading.value = false
    loadingMore.value = false
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
      finished.value = true
      page.value = 1
    }
  } catch (error) {
    console.error('清空历史记录失败:', error)
    alert('清空失败，请稍后重试')
  }
}

// 在新标签页打开视频播放页
const goToVideo = (videoId) => {
  if (!videoId) return
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const path = `/video/${encodeURIComponent(videoId)}`
  const url = `${normalizedBase}${path}`
  window.open(url, '_blank')
}

// 使用 Intersection Observer 检测底部元素，实现无限滚动
const loadMoreTrigger = ref(null)
let observer = null

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
      if (entry.isIntersecting && !loading.value && !loadingMore.value && !finished.value) {
        loadHistory()
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

// 监听历史记录列表变化，重新设置 observer
watch(
  () => historyList.value.length,
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

// 监听筛选条件变化，重新加载数据（重置）
watch([activeTab, selectedDuration, selectedTime, startDate, endDate], () => {
  loadHistory(true) // 重置加载
}, { deep: true })

onMounted(() => {
  loadHistory(true) // 初始加载
  // 等待 DOM 渲染后设置 observer
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

<style scoped lang="scss">
.history-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  min-height: 100%;
  margin: 0 auto;
  background: #fff;
  padding-top: 64px;

  .history-container {
    display: flex;
    gap: 0;
    padding: 20px 0;
    max-width: 75%;
    min-width: 1200px;
    width: 100%;
    margin: 0 auto;
    justify-content: center;
    align-items: flex-start;
  }

  .history-main {
    flex: 0 0 auto;
    width: 100%;
    /* max-width: 1000px; */
    background: #fff;
    border-radius: 0;
    padding: 20px;
  }

  .batch-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 20px;

    .batch-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .select-all-label {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        font-size: 14px;
        color: #222;
      }

      .selected-count {
        font-size: 14px;
        color: #666;
      }
    }

    .batch-right {
      display: flex;
      align-items: center;
      gap: 12px;

      .batch-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 16px;
        border: 1px solid #e3e3e3;
        background: #fff;
        border-radius: 4px;
        font-size: 14px;
        color: #666;
        cursor: pointer;
        transition: all 0.2s;

        &:hover:not(:disabled) {
          border-color: #00a1d6;
          color: #00a1d6;
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        &.delete-btn {
          color: #ff4d4f;
          
          &:hover:not(:disabled) {
            border-color: #ff4d4f;
            color: #ff4d4f;
          }
        }
      }
    }
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
    display: flex;
    align-items: center;
    gap: 4px;

    &:hover {
      background: #f5f5f5;
    }

    &.active {
      color: #00a1d6;
      background: #e6f7ff;
    }

    &.more-filter {
      .filter-icon {
        transition: transform 0.3s;
        
        &.rotate {
          transform: rotate(180deg);
        }
      }
    }
  }

  .more-filters {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 20px;

    .filter-row {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-label {
        font-size: 14px;
        color: #666;
        min-width: 50px;
      }

      .filter-buttons {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }

      .filter-btn {
        padding: 4px 16px;
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

        &.active {
          background: #e6f7ff;
          border-color: #00a1d6;
          color: #00a1d6;
        }
      }

      .date-range {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-left: 8px;

        .date-input {
          padding: 4px 12px;
          border: 1px solid #e3e3e3;
          border-radius: 4px;
          font-size: 14px;
          outline: none;

          &:focus {
            border-color: #00a1d6;
          }
        }

        .date-separator {
          font-size: 14px;
          color: #666;
        }
      }
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
    width: 100%;
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
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 16px;
      width: 100%;
      margin: 0 auto;

      .card {
        width: 100%;
        background: transparent;
        border: none;
        border-radius: 0;
        overflow: visible;
        cursor: pointer;
        transition: transform 0.2s;
        display: flex;
        flex-direction: column;
        position: relative;

        &:hover {
          transform: translateY(-2px);
          
          .delete-icon {
            opacity: 1;
          }
        }

        &.batch-mode {
          cursor: default;
        }

        .card-checkbox {
          position: absolute;
          // top: 8px;
          // left: 8px;
          z-index: 10;
          // background: rgba(255, 255, 255, 0.9);
          border-radius: 4px;
          padding: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 20px;
          height: 20px;

          input[type="checkbox"] {
            cursor: pointer;
          }
        }

        .delete-icon {
          position: absolute;
          top: 8px;
          right: 8px;
          z-index: 10;
          background: rgba(0, 0, 0, 0.6);
          color: #fff;
          border-radius: 4px;
          padding: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 24px;
          height: 24px;
          cursor: pointer;
          opacity: 0;
          transition: opacity 0.2s;

          &:hover {
            background: rgba(0, 0, 0, 0.8);
          }
        }

        .thumb {
          position: relative;
          width: 100%;
          height: 0;
          padding-bottom: 56.25%;
          background: #f7f7f7;
          border-radius: 8px;
          overflow: hidden;
          margin-bottom: 8px;
          flex-shrink: 0;

          img { 
            position: absolute;
            top: 0;
            left: 0;
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

  .loading-bar {
    text-align: center;
    padding: 20px;
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


