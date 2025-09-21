<template>
  <div class="promotion-page">
    <div class="page-header">
      <h1>必火推广</h1>
      <p>让你的优质内容获得更多曝光，提升播放量和粉丝增长</p>
    </div>

    <!-- 推广概览 -->
    <div class="overview-section">
      <div class="overview-cards">
        <div class="overview-card">
          <div class="card-icon">📈</div>
          <div class="card-content">
            <h3>推广中</h3>
            <div class="card-value">{{ activePromotions }}</div>
            <div class="card-label">个视频</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">👁️</div>
          <div class="card-content">
            <h3>总曝光</h3>
            <div class="card-value">{{ totalExposure }}</div>
            <div class="card-label">次展示</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">💰</div>
          <div class="card-content">
            <h3>总花费</h3>
            <div class="card-value">{{ totalSpent }}</div>
            <div class="card-label">元</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">📊</div>
          <div class="card-content">
            <h3>ROI</h3>
            <div class="card-value">{{ roi }}</div>
            <div class="card-label">投资回报率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推广工具 -->
    <div class="tools-section">
      <h2>推广工具</h2>
      <div class="tools-grid">
        <div class="tool-card">
          <div class="tool-header">
            <div class="tool-icon">🔥</div>
            <h3>热门推荐</h3>
            <el-tag type="danger" size="small">热门</el-tag>
          </div>
          <p class="tool-desc">将视频推送到热门推荐位，获得大量曝光</p>
          <div class="tool-features">
            <span class="feature">• 首页推荐位展示</span>
            <span class="feature">• 精准用户匹配</span>
            <span class="feature">• 实时数据监控</span>
          </div>
          <div class="tool-pricing">
            <span class="price">¥0.5</span>
            <span class="unit">/ 千次曝光</span>
          </div>
          <el-button type="primary" size="large" class="tool-btn">立即推广</el-button>
        </div>

        <div class="tool-card">
          <div class="tool-header">
            <div class="tool-icon">🎯</div>
            <h3>精准投放</h3>
            <el-tag type="success" size="small">精准</el-tag>
          </div>
          <p class="tool-desc">基于用户兴趣和行为的精准投放</p>
          <div class="tool-features">
            <span class="feature">• 兴趣标签匹配</span>
            <span class="feature">• 年龄性别筛选</span>
            <span class="feature">• 地域定向投放</span>
          </div>
          <div class="tool-pricing">
            <span class="price">¥0.8</span>
            <span class="unit">/ 千次曝光</span>
          </div>
          <el-button type="primary" size="large" class="tool-btn">立即推广</el-button>
        </div>

        <div class="tool-card">
          <div class="tool-header">
            <div class="tool-icon">⚡</div>
            <h3>快速推广</h3>
            <el-tag type="warning" size="small">快速</el-tag>
          </div>
          <p class="tool-desc">快速提升视频初始播放量</p>
          <div class="tool-features">
            <span class="feature">• 24小时内生效</span>
            <span class="feature">• 自然流量增长</span>
            <span class="feature">• 算法友好</span>
          </div>
          <div class="tool-pricing">
            <span class="price">¥0.3</span>
            <span class="unit">/ 千次曝光</span>
          </div>
          <el-button type="primary" size="large" class="tool-btn">立即推广</el-button>
        </div>
      </div>
    </div>

    <!-- 推广历史 -->
    <div class="history-section">
      <div class="section-header">
        <h2>推广历史</h2>
        <el-button type="primary" plain>查看全部</el-button>
      </div>
      <div class="history-table">
        <div class="table-header">
          <div class="col-video">视频</div>
          <div class="col-tool">推广工具</div>
          <div class="col-budget">预算</div>
          <div class="col-exposure">曝光量</div>
          <div class="col-clicks">点击量</div>
          <div class="col-status">状态</div>
          <div class="col-actions">操作</div>
        </div>
        <div class="table-body">
          <div v-for="item in promotionHistory" :key="item.id" class="table-row">
            <div class="col-video">
              <div class="video-info">
                <img :src="item.thumbnail" :alt="item.title" class="video-thumbnail" />
                <div class="video-details">
                  <h4 class="video-title">{{ item.title }}</h4>
                  <span class="video-date">{{ item.date }}</span>
                </div>
              </div>
            </div>
            <div class="col-tool">
              <el-tag :type="item.toolType" size="small">{{ item.tool }}</el-tag>
            </div>
            <div class="col-budget">¥{{ item.budget }}</div>
            <div class="col-exposure">{{ item.exposure }}</div>
            <div class="col-clicks">{{ item.clicks }}</div>
            <div class="col-status">
              <el-tag :type="item.statusType" size="small">{{ item.status }}</el-tag>
            </div>
            <div class="col-actions">
              <el-button size="small" @click="viewDetails(item)">查看</el-button>
              <el-button v-if="item.status === '进行中'" size="small" type="danger" @click="stopPromotion(item)">停止</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推广建议 -->
    <div class="suggestions-section">
      <h2>推广建议</h2>
      <div class="suggestions-grid">
        <div class="suggestion-card">
          <div class="suggestion-icon">💡</div>
          <div class="suggestion-content">
            <h3>最佳推广时间</h3>
            <p>根据你的粉丝活跃时间，建议在 <strong>19:00-21:00</strong> 进行推广</p>
          </div>
        </div>
        <div class="suggestion-card">
          <div class="suggestion-icon">🎯</div>
          <div class="suggestion-content">
            <h3>目标用户画像</h3>
            <p>你的内容主要吸引 <strong>18-25岁</strong> 的 <strong>学生群体</strong>，建议定向投放</p>
          </div>
        </div>
        <div class="suggestion-card">
          <div class="suggestion-icon">📊</div>
          <div class="suggestion-content">
            <h3>预算建议</h3>
            <p>基于历史数据，建议每日推广预算为 <strong>¥50-100</strong></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 概览数据
const activePromotions = ref(3)
const totalExposure = ref('125.6万')
const totalSpent = ref('2,450')
const roi = ref('3.2x')

// 推广历史数据
const promotionHistory = ref([
  {
    id: 1,
    title: 'Vue 3 入门教程 - 从零开始学习前端框架',
    thumbnail: '/placeholder-thumbnail.jpg',
    date: '2024-01-15',
    tool: '热门推荐',
    toolType: 'danger',
    budget: 500,
    exposure: '45.2万',
    clicks: '8,500',
    status: '已完成',
    statusType: 'success'
  },
  {
    id: 2,
    title: 'JavaScript 高级技巧分享',
    thumbnail: '/placeholder-thumbnail.jpg',
    date: '2024-01-12',
    tool: '精准投放',
    toolType: 'success',
    budget: 300,
    exposure: '32.1万',
    clicks: '6,200',
    status: '进行中',
    statusType: 'warning'
  },
  {
    id: 3,
    title: 'CSS 动画效果制作指南',
    thumbnail: '/placeholder-thumbnail.jpg',
    date: '2024-01-10',
    tool: '快速推广',
    toolType: 'warning',
    budget: 200,
    exposure: '28.3万',
    clicks: '4,800',
    status: '已完成',
    statusType: 'success'
  }
])

const viewDetails = (item) => {
  console.log('查看推广详情:', item.title)
}

const stopPromotion = (item) => {
  console.log('停止推广:', item.title)
}
</script>

<style lang="scss" scoped>
.promotion-page {
  padding: 0;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 28px;
    margin: 0 0 8px 0;
    color: #333;
  }
  
  p {
    font-size: 16px;
    color: #666;
    margin: 0;
  }
}

.overview-section {
  margin-bottom: 32px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.overview-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  
  .card-icon {
    font-size: 32px;
  }
  
  .card-content {
    h3 {
      font-size: 14px;
      color: #666;
      margin: 0 0 4px 0;
    }
    
    .card-value {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 2px;
    }
    
    .card-label {
      font-size: 12px;
      color: #999;
    }
  }
}

.tools-section, .history-section, .suggestions-section {
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

.tools-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.tool-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
  
  .tool-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 12px;
    
    .tool-icon {
      font-size: 24px;
    }
    
    h3 {
      font-size: 18px;
      margin: 0;
      color: #333;
    }
  }
  
  .tool-desc {
    font-size: 14px;
    color: #666;
    margin: 0 0 16px 0;
  }
  
  .tool-features {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 16px;
    
    .feature {
      font-size: 12px;
      color: #666;
    }
  }
  
  .tool-pricing {
    margin-bottom: 16px;
    
    .price {
      font-size: 24px;
      font-weight: 600;
      color: #409eff;
    }
    
    .unit {
      font-size: 14px;
      color: #666;
    }
  }
  
  .tool-btn {
    width: 100%;
  }
}

.history-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  font-weight: 600;
  color: #333;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
  
  &:hover {
    background: #fafafa;
  }
}

.video-info {
  display: flex;
  gap: 12px;
  align-items: center;
  
  .video-thumbnail {
    width: 60px;
    height: 40px;
    border-radius: 4px;
    object-fit: cover;
    background: #f0f0f0;
  }
  
  .video-details {
    .video-title {
      font-size: 14px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    .video-date {
      font-size: 12px;
      color: #666;
    }
  }
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.suggestion-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  gap: 16px;
  
  .suggestion-icon {
    font-size: 24px;
    flex-shrink: 0;
  }
  
  .suggestion-content {
    h3 {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0;
      line-height: 1.5;
    }
  }
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .tools-grid {
    grid-template-columns: 1fr;
  }
  
  .suggestions-grid {
    grid-template-columns: 1fr;
  }
  
  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .col-video,
  .col-tool,
  .col-budget,
  .col-exposure,
  .col-clicks,
  .col-status,
  .col-actions {
    display: none;
  }
}
</style>
