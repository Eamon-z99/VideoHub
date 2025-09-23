<template>
  <div class="creator-home">
    <div class="banner">
      <img class="banner-img" src="/assets/creator-center.png" alt="creator banner" />
      <div class="banner-overlay">
        <h1>欢迎来到创作中心</h1>
        <p>开始你的创作之旅，分享你的精彩内容</p>
        <div class="quick-actions">
          <el-button type="primary" size="large" @click="navigateToSubmit">立即投稿</el-button>
          <el-button size="large" @click="navigateToContentManagement">管理内容</el-button>
        </div>
      </div>
    </div>

    <!-- 数据概览（图片卡片） -->
    <div class="overview-section">
      <h2>数据概览</h2>
      <div class="stats-grid metric-grid">
        <div class="metric-card">
          <div class="metric-title">总视频数</div>
          <div class="metric-value">{{ totalVideos }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-title">总播放量</div>
          <div class="metric-value">{{ totalViews }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-title">总点赞数</div>
          <div class="metric-value">{{ totalLikes }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-title">粉丝数</div>
          <div class="metric-value">{{ totalFollowers }}</div>
        </div>
      </div>
    </div>

    <!-- 最近作品（图片网格） -->
    <div class="recent-section">
      <div class="section-header">
        <h2>最近作品</h2>
        <el-button type="primary" plain @click="navigateToContentManagement">查看全部</el-button>
      </div>
      <div class="grid gallery">
        <div v-for="video in recentVideos" :key="video.id" class="tile" @click="navigateToContentManagement">
          <img :src="video.thumbnail" :alt="video.title" />
          <div class="tile-mask">
            <span class="tile-duration">{{ video.duration }}</span>
            <h3 class="tile-title">{{ video.title }}</h3>
            <div class="tile-meta">
              <span>{{ video.views }} 播放</span>
              <span>{{ video.likes }} 点赞</span>
              <span>{{ video.publishDate }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创作工具（纯文本卡片） -->
    <div class="tools-section">
      <h2>创作工具</h2>
      <div class="tools-grid">
        <div class="tool-card plain-tool">
          <h3>必剪</h3>
          <p>专业视频剪辑工具</p>
          <el-button type="primary" plain>立即使用</el-button>
        </div>
        <div class="tool-card plain-tool">
          <h3>封面制作</h3>
          <p>快速制作精美封面</p>
          <el-button type="primary" plain>立即使用</el-button>
        </div>
        <div class="tool-card plain-tool">
          <h3>标题助手</h3>
          <p>智能生成吸引人标题</p>
          <el-button type="primary" plain>立即使用</el-button>
        </div>
      </div>
    </div>

    <!-- 创作学院（图片网格） -->
    <div class="academy-section">
      <div class="section-header">
        <h2>创作学院</h2>
        <el-button type="primary" plain @click="navigateToAcademy">查看更多</el-button>
      </div>
      <div class="grid gallery courses">
        <div v-for="course in courses" :key="course.id" class="tile">
          <img :src="course.thumbnail" :alt="course.title" />
          <div class="tile-mask">
            <h3 class="tile-title">{{ course.title }}</h3>
            <div class="tile-meta">
              <span>{{ course.duration }}</span>
              <span>{{ course.students }} 人学习</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 统计数据
const totalVideos = ref('128')
const totalViews = ref('1.2万')
const totalLikes = ref('3.4万')
const totalFollowers = ref('1.5万')

// 最近作品数据
const recentVideos = ref([
  {
    id: 1,
    title: 'Vue 3 入门教程 - 从零开始学习前端框架',
    thumbnail: '/assets/channel/live.png',
    duration: '15:30',
    views: '125000',
    likes: '3200',
    publishDate: '2024-01-15'
  },
  {
    id: 2,
    title: 'JavaScript 高级技巧分享',
    thumbnail: '/assets/channel/activity.png',
    duration: '12:45',
    views: '89000',
    likes: '2100',
    publishDate: '2024-01-10'
  },
  {
    id: 3,
    title: 'CSS 动画效果制作指南',
    thumbnail: '/assets/channel/column.png',
    duration: '18:20',
    views: '67000',
    likes: '1800',
    publishDate: '2024-01-05'
  }
])

// 课程数据
const courses = ref([
  {
    id: 1,
    title: '视频剪辑基础教程',
    description: '从零开始学习视频剪辑，掌握基本操作技巧',
    thumbnail: '/assets/channel/class.png',
    duration: '2小时30分',
    students: '1.2万'
  },
  {
    id: 2,
    title: '如何制作吸引人的封面',
    description: '学习封面设计原理，提升视频点击率',
    thumbnail: '/assets/channel/community.png',
    duration: '1小时45分',
    students: '8.5千'
  },
  {
    id: 3,
    title: '内容创作策略',
    description: '了解用户喜好，制定有效的内容策略',
    thumbnail: '/assets/channel/new-songs-chart.png',
    duration: '3小时15分',
    students: '2.1万'
  }
])

// 导航函数
const navigateToSubmit = () => {
  // 这里可以触发父组件的视图切换
  console.log('导航到投稿页面')
}

const navigateToContentManagement = () => {
  console.log('导航到内容管理')
}

const navigateToAcademy = () => {
  console.log('导航到创作学院')
}
</script>

<style lang="scss" scoped>
.creator-home {
  padding: 16px;
  background: #f6f7fb;
}

.banner {
  position: relative;
  height: 220px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(0,0,0,0.55), rgba(0,0,0,0.15) 60%, rgba(0,0,0,0));
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 0 32px;
  gap: 12px;
}

.banner-overlay h1 {
  margin: 0;
  font-size: 28px;
}

.banner-overlay p {
  margin: 0;
  opacity: .9;
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px;
  border-radius: 12px;
  margin-bottom: 24px;
  text-align: center;
  
  h1 {
    font-size: 32px;
    margin: 0 0 12px 0;
  }
  
  p {
    font-size: 16px;
    margin: 0 0 24px 0;
    opacity: 0.9;
  }
  
  .quick-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
  }
}

.overview-section {
  margin-bottom: 32px;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

/* 通用图片卡片样式 */
.image-cards .image-card,
.image-card {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(0,0,0,0.08);
  background: #f5f5f5;
}

.image-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}

.image-card.small img {
  height: 120px;
}

.image-card-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 12px;
  gap: 6px;
  background: linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,0.65));
  color: #fff;
}

.image-card-title {
  font-size: 16px;
}

.image-card-value {
  font-size: 22px;
  font-weight: 700;
}

/* 纯文本指标卡片 */
.metric-grid {
  grid-template-columns: repeat(4, 1fr);
}

.metric-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  border: 1px solid #eef0f4;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  background-image: linear-gradient(180deg, rgba(0,0,0,0) 55%, rgba(0,0,0,0.2));
}

.metric-title {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 6px;
}

.metric-value {
  color: #111;
  font-size: 22px;
  font-weight: 700;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  
  .stat-icon {
    font-size: 32px;
  }
  
  .stat-content {
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #666;
    }
  }
}

.recent-section, .tools-section, .academy-section {
  margin-bottom: 32px;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 图片网格 */
.grid.gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.tile {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  border: 1px solid #eef0f4;
}

.tile img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}

.courses .tile img {
  height: 140px;
}

.tile-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 6px;
  padding: 12px;
  color: #fff;
  background: linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,0.7));
}

.tile-duration {
  align-self: flex-end;
  background: rgba(0,0,0,0.7);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.tile-title {
  margin: 0;
  font-size: 16px;
}

.tile-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  opacity: .9;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.tool-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #eef0f4;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  
  .tool-icon {
    font-size: 32px;
    margin-bottom: 12px;
  }
  
  h3 {
    font-size: 18px;
    margin: 0 0 8px 0;
    color: #333;
  }
  
  p {
    font-size: 14px;
    color: #666;
    margin: 0 0 16px 0;
  }
}

.plain-tool {
  text-align: left;
}

/* 课程卡片在上面的通用图片网格里显示，无需单独列表样式 */

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .tools-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    flex-direction: column;
  }

  .grid.gallery {
    grid-template-columns: 1fr 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
