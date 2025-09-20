<template>
  <div class="content-management-page">
    <header class="page-header">
      <h1>内容管理</h1>
      <div class="header-actions">
        <div class="search-box">
          <input type="text" placeholder="搜索视频标题" v-model="searchQuery" />
          <button class="search-btn">🔍</button>
        </div>
        <div class="filters">
          <select v-model="statusFilter" class="filter-select">
            <option value="">全部状态</option>
            <option value="published">已发布</option>
            <option value="draft">草稿</option>
            <option value="reviewing">审核中</option>
            <option value="rejected">已拒绝</option>
          </select>
          <select v-model="typeFilter" class="filter-select">
            <option value="">全部类型</option>
            <option value="video">视频</option>
            <option value="short">短视频</option>
            <option value="audio">音频</option>
          </select>
        </div>
      </div>
    </header>

    <!-- 统计卡片 -->
    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📹</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalVideos }}</div>
          <div class="stat-label">总视频数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👁️</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalViews }}</div>
          <div class="stat-label">总播放量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👍</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalLikes }}</div>
          <div class="stat-label">总点赞数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💬</div>
        <div class="stat-content">
          <div class="stat-value">{{ totalComments }}</div>
          <div class="stat-label">总评论数</div>
        </div>
      </div>
    </section>

    <!-- 内容列表 -->
    <section class="content-list">
      <div class="list-header">
        <div class="list-title">
          <h3>我的作品</h3>
          <span class="count">共 {{ filteredVideos.length }} 个</span>
        </div>
        <div class="list-actions">
          <button class="action-btn" @click="batchDelete" :disabled="selectedVideos.length === 0">
            批量删除
          </button>
          <button class="action-btn primary" @click="newVideo">
            新建视频
          </button>
        </div>
      </div>

      <div class="video-table">
        <div class="table-header">
          <div class="col-checkbox">
            <input 
              type="checkbox" 
              :checked="allSelected" 
              @change="toggleSelectAll"
            />
          </div>
          <div class="col-thumbnail">封面</div>
          <div class="col-title">标题</div>
          <div class="col-status">状态</div>
          <div class="col-stats">数据</div>
          <div class="col-date">发布时间</div>
          <div class="col-actions">操作</div>
        </div>

        <div class="table-body">
          <div 
            v-for="video in filteredVideos" 
            :key="video.id" 
            class="table-row"
          >
            <div class="col-checkbox">
              <input 
                type="checkbox" 
                :checked="selectedVideos.includes(video.id)"
                @change="toggleSelect(video.id)"
              />
            </div>
            <div class="col-thumbnail">
              <div class="video-thumbnail" :style="{ backgroundImage: `url(${video.thumbnail})` }">
                <div class="duration">{{ video.duration }}</div>
              </div>
            </div>
            <div class="col-title">
              <div class="video-title">{{ video.title }}</div>
              <div class="video-desc">{{ video.description }}</div>
            </div>
            <div class="col-status">
              <span class="status-badge" :class="video.status">
                {{ getStatusText(video.status) }}
              </span>
            </div>
            <div class="col-stats">
              <div class="stat-item">
                <span class="stat-label">播放:</span>
                <span class="stat-value">{{ formatNumber(video.views) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">点赞:</span>
                <span class="stat-value">{{ formatNumber(video.likes) }}</span>
              </div>
            </div>
            <div class="col-date">{{ formatDate(video.publishDate) }}</div>
            <div class="col-actions">
              <button class="action-btn small" @click="editVideo(video)">编辑</button>
              <button class="action-btn small" @click="viewVideo(video)">查看</button>
              <button class="action-btn small danger" @click="deleteVideo(video)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage }} 页，共 {{ totalPages }} 页
        </span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
          下一页
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const selectedVideos = ref([])
const currentPage = ref(1)
const pageSize = 10

// 模拟视频数据
const videos = ref([
  {
    id: 1,
    title: 'Vue 3 入门教程 - 从零开始学习前端框架',
    description: '详细介绍 Vue 3 的基础知识和核心概念',
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '15:30',
    status: 'published',
    views: 125000,
    likes: 3200,
    comments: 156,
    publishDate: '2024-01-15'
  },
  {
    id: 2,
    title: 'JavaScript 高级技巧分享',
    description: '分享一些实用的 JavaScript 编程技巧',
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '12:45',
    status: 'reviewing',
    views: 0,
    likes: 0,
    comments: 0,
    publishDate: '2024-01-20'
  },
  {
    id: 3,
    title: 'CSS 动画效果制作指南',
    description: '教你如何制作炫酷的 CSS 动画效果',
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '18:20',
    status: 'draft',
    views: 0,
    likes: 0,
    comments: 0,
    publishDate: ''
  },
  {
    id: 4,
    title: 'React vs Vue 框架对比分析',
    description: '深度对比两大前端框架的优缺点',
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '22:10',
    status: 'published',
    views: 89000,
    likes: 2100,
    comments: 89,
    publishDate: '2024-01-10'
  },
  {
    id: 5,
    title: 'TypeScript 类型系统详解',
    description: '深入理解 TypeScript 的类型系统',
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '25:35',
    status: 'rejected',
    views: 0,
    likes: 0,
    comments: 0,
    publishDate: ''
  }
])

// 计算属性
const filteredVideos = computed(() => {
  let result = videos.value

  // 搜索过滤
  if (searchQuery.value) {
    result = result.filter(video => 
      video.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(video => video.status === statusFilter.value)
  }

  // 类型过滤
  if (typeFilter.value) {
    // 这里可以根据实际需求添加类型字段
    result = result
  }

  return result
})

const allSelected = computed(() => {
  return filteredVideos.value.length > 0 && 
         filteredVideos.value.every(video => selectedVideos.value.includes(video.id))
})

const totalPages = computed(() => {
  return Math.ceil(filteredVideos.value.length / pageSize)
})

// 统计数据
const totalVideos = computed(() => videos.value.length)
const totalViews = computed(() => videos.value.reduce((sum, video) => sum + video.views, 0))
const totalLikes = computed(() => videos.value.reduce((sum, video) => sum + video.likes, 0))
const totalComments = computed(() => videos.value.reduce((sum, video) => sum + video.comments, 0))

// 方法
const getStatusText = (status) => {
  const statusMap = {
    published: '已发布',
    draft: '草稿',
    reviewing: '审核中',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const toggleSelect = (videoId) => {
  const index = selectedVideos.value.indexOf(videoId)
  if (index > -1) {
    selectedVideos.value.splice(index, 1)
  } else {
    selectedVideos.value.push(videoId)
  }
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedVideos.value = []
  } else {
    selectedVideos.value = filteredVideos.value.map(video => video.id)
  }
}

const editVideo = (video) => {
  console.log('编辑视频:', video.title)
}

const viewVideo = (video) => {
  console.log('查看视频:', video.title)
}

const deleteVideo = (video) => {
  if (confirm(`确定要删除视频"${video.title}"吗？`)) {
    const index = videos.value.findIndex(v => v.id === video.id)
    if (index > -1) {
      videos.value.splice(index, 1)
    }
  }
}

const batchDelete = () => {
  if (selectedVideos.value.length === 0) return
  
  if (confirm(`确定要删除选中的 ${selectedVideos.value.length} 个视频吗？`)) {
    videos.value = videos.value.filter(video => !selectedVideos.value.includes(video.id))
    selectedVideos.value = []
  }
}

const newVideo = () => {
  console.log('新建视频')
}
</script>

<style lang="scss" scoped>
.content-management-page {
  background: transparent;
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  h1 {
    margin: 0;
    font-size: 20px;
    color: #333;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
  
  input {
    border: none;
    outline: none;
    padding: 8px 12px;
    width: 200px;
  }
  
  .search-btn {
    background: #f5f7fa;
    border: none;
    padding: 8px 12px;
    cursor: pointer;
  }
}

.filters {
  display: flex;
  gap: 8px;
}

.filter-select {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px 12px;
  outline: none;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  
  .stat-icon {
    font-size: 24px;
  }
  
  .stat-content {
    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: #333;
    }
    
    .stat-label {
      font-size: 12px;
      color: #666;
    }
  }
}

.content-list {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  .list-title {
    display: flex;
    align-items: center;
    gap: 8px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      color: #333;
    }
    
    .count {
      color: #666;
      font-size: 12px;
    }
  }
}

.list-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  
  &.primary {
    background: #00aeec;
    color: #fff;
    border-color: #00aeec;
  }
  
  &.small {
    padding: 4px 8px;
    font-size: 12px;
  }
  
  &.danger {
    color: #ff4d4f;
    border-color: #ff4d4f;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.video-table {
  .table-header {
    display: grid;
    grid-template-columns: 40px 80px 1fr 100px 120px 120px 120px;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    font-weight: 600;
    color: #333;
  }
  
  .table-row {
    display: grid;
    grid-template-columns: 40px 80px 1fr 100px 120px 120px 120px;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;
    align-items: center;
    
    &:hover {
      background: #fafafa;
    }
  }
}

.col-checkbox {
  display: flex;
  justify-content: center;
}

.col-thumbnail {
  .video-thumbnail {
    width: 60px;
    height: 40px;
    background: #e0e0e0;
    border-radius: 4px;
    position: relative;
    background-size: cover;
    background-position: center;
    
    .duration {
      position: absolute;
      bottom: 2px;
      right: 2px;
      background: rgba(0,0,0,.7);
      color: #fff;
      font-size: 10px;
      padding: 1px 4px;
      border-radius: 2px;
    }
  }
}

.col-title {
  .video-title {
    font-weight: 500;
    color: #333;
    margin-bottom: 4px;
  }
  
  .video-desc {
    font-size: 12px;
    color: #666;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.status-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  
  &.published {
    background: #f6ffed;
    color: #52c41a;
  }
  
  &.draft {
    background: #fff7e6;
    color: #fa8c16;
  }
  
  &.reviewing {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  &.rejected {
    background: #fff1f0;
    color: #ff4d4f;
  }
}

.col-stats {
  .stat-item {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    margin-bottom: 2px;
    
    .stat-label {
      color: #666;
    }
    
    .stat-value {
      color: #333;
      font-weight: 500;
    }
  }
}

.col-actions {
  display: flex;
  gap: 4px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.page-btn {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.page-info {
  color: #666;
  font-size: 14px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .video-table {
    .table-header,
    .table-row {
      grid-template-columns: 40px 60px 1fr 80px 100px 100px;
    }
    
    .col-stats,
    .col-actions {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box input {
    width: 100%;
  }
  
  .video-table {
    .table-header,
    .table-row {
      grid-template-columns: 40px 1fr 80px;
    }
    
    .col-thumbnail,
    .col-stats,
    .col-date,
    .col-actions {
      display: none;
    }
  }
}
</style>
