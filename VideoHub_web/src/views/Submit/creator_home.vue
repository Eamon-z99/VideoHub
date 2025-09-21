<template>
  <div class="creator-home">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎来到创作中心</h1>
        <p>开始你的创作之旅，分享你的精彩内容</p>
        <div class="quick-actions">
          <el-button type="primary" size="large" @click="navigateToSubmit">
            <el-icon><Upload /></el-icon>
            立即投稿
          </el-button>
          <el-button size="large" @click="navigateToContentManagement">
            <el-icon><Folder /></el-icon>
            管理内容
          </el-button>
        </div>
      </div>
    </div>

    <!-- 数据概览 -->
    <div class="overview-section">
      <h2>数据概览</h2>
      <div class="stats-grid">
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
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <div class="stat-value">{{ totalFollowers }}</div>
            <div class="stat-label">粉丝数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近作品 -->
    <div class="recent-section">
      <div class="section-header">
        <h2>最近作品</h2>
        <el-button type="primary" plain @click="navigateToContentManagement">查看全部</el-button>
      </div>
      <div class="video-list">
        <div v-for="video in recentVideos" :key="video.id" class="video-item">
          <div class="video-thumbnail">
            <img :src="video.thumbnail" :alt="video.title" />
            <div class="duration">{{ video.duration }}</div>
          </div>
          <div class="video-info">
            <h3 class="video-title">{{ video.title }}</h3>
            <div class="video-stats">
              <span class="views">{{ video.views }} 播放</span>
              <span class="likes">{{ video.likes }} 点赞</span>
              <span class="date">{{ video.publishDate }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创作工具推荐 -->
    <div class="tools-section">
      <h2>创作工具</h2>
      <div class="tools-grid">
        <div class="tool-card">
          <div class="tool-icon">✂️</div>
          <h3>必剪</h3>
          <p>专业视频剪辑工具</p>
          <el-button type="primary" plain>立即使用</el-button>
        </div>
        <div class="tool-card">
          <div class="tool-icon">🎨</div>
          <h3>封面制作</h3>
          <p>快速制作精美封面</p>
          <el-button type="primary" plain>立即使用</el-button>
        </div>
        <div class="tool-card">
          <div class="tool-icon">📝</div>
          <h3>标题助手</h3>
          <p>智能生成吸引人标题</p>
          <el-button type="primary" plain>立即使用</el-button>
        </div>
      </div>
    </div>

    <!-- 创作学院推荐 -->
    <div class="academy-section">
      <div class="section-header">
        <h2>创作学院</h2>
        <el-button type="primary" plain @click="navigateToAcademy">查看更多</el-button>
      </div>
      <div class="course-list">
        <div v-for="course in courses" :key="course.id" class="course-item">
          <div class="course-thumbnail">
            <img :src="course.thumbnail" :alt="course.title" />
          </div>
          <div class="course-info">
            <h3 class="course-title">{{ course.title }}</h3>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-meta">
              <span class="duration">{{ course.duration }}</span>
              <span class="students">{{ course.students }} 人学习</span>
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
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '15:30',
    views: '125000',
    likes: '3200',
    publishDate: '2024-01-15'
  },
  {
    id: 2,
    title: 'JavaScript 高级技巧分享',
    thumbnail: '/placeholder-thumbnail.jpg',
    duration: '12:45',
    views: '89000',
    likes: '2100',
    publishDate: '2024-01-10'
  },
  {
    id: 3,
    title: 'CSS 动画效果制作指南',
    thumbnail: '/placeholder-thumbnail.jpg',
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
    thumbnail: '/placeholder-course.jpg',
    duration: '2小时30分',
    students: '1.2万'
  },
  {
    id: 2,
    title: '如何制作吸引人的封面',
    description: '学习封面设计原理，提升视频点击率',
    thumbnail: '/placeholder-course.jpg',
    duration: '1小时45分',
    students: '8.5千'
  },
  {
    id: 3,
    title: '内容创作策略',
    description: '了解用户喜好，制定有效的内容策略',
    thumbnail: '/placeholder-course.jpg',
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
  padding: 0;
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

.video-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-item {
  display: flex;
  gap: 12px;
  background: white;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  
  .video-thumbnail {
    width: 120px;
    height: 80px;
    border-radius: 6px;
    overflow: hidden;
    position: relative;
    background: #f0f0f0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .duration {
      position: absolute;
      bottom: 4px;
      right: 4px;
      background: rgba(0,0,0,0.7);
      color: white;
      font-size: 12px;
      padding: 2px 6px;
      border-radius: 4px;
    }
  }
  
  .video-info {
    flex: 1;
    
    .video-title {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .video-stats {
      display: flex;
      gap: 16px;
      font-size: 14px;
      color: #666;
    }
  }
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
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  
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

.course-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-item {
  display: flex;
  gap: 12px;
  background: white;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  
  .course-thumbnail {
    width: 120px;
    height: 80px;
    border-radius: 6px;
    overflow: hidden;
    background: #f0f0f0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .course-info {
    flex: 1;
    
    .course-title {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .course-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 8px 0;
    }
    
    .course-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #999;
    }
  }
}

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
}
</style>
