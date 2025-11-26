<template>
  <div class="rights-page">
    <div class="page-header">
      <h1>创作权益</h1>
      <p>了解你的创作权益，享受更多创作福利</p>
    </div>

    <!-- 权益概览 -->
    <div class="rights-overview">
      <div class="overview-cards">
        <div class="overview-card">
          <div class="card-icon">👑</div>
          <div class="card-content">
            <h3>当前等级</h3>
            <div class="card-value">{{ currentLevel }}</div>
            <div class="card-label">{{ levelName }}</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">🎁</div>
          <div class="card-content">
            <h3>已解锁权益</h3>
            <div class="card-value">{{ unlockedRights }}</div>
            <div class="card-label">项权益</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">📈</div>
          <div class="card-content">
            <h3>升级进度</h3>
            <div class="card-value">{{ upgradeProgress }}%</div>
            <div class="card-label">距离下一级</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 当前权益 -->
    <div class="current-rights-section">
      <h2>当前权益</h2>
      <div class="rights-grid">
        <div v-for="right in currentRights" :key="right.id" class="right-card" :class="{ active: right.active }">
          <div class="right-icon">{{ right.icon }}</div>
          <div class="right-content">
            <h3 class="right-title">{{ right.title }}</h3>
            <p class="right-desc">{{ right.description }}</p>
            <div class="right-status">
              <el-tag :type="right.active ? 'success' : 'info'" size="small">
                {{ right.active ? '已激活' : '未激活' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 等级权益对比 -->
    <div class="level-comparison-section">
      <h2>等级权益对比</h2>
      <div class="comparison-table">
        <div class="table-header">
          <div class="col-feature">权益项目</div>
          <div class="col-level" v-for="level in levels" :key="level.id">
            <div class="level-name">{{ level.name }}</div>
            <div class="level-badge" :class="level.badge">{{ level.badgeText }}</div>
          </div>
        </div>
        <div class="table-body">
          <div v-for="feature in features" :key="feature.id" class="table-row">
            <div class="col-feature">
              <span class="feature-name">{{ feature.name }}</span>
              <span class="feature-desc">{{ feature.description }}</span>
            </div>
            <div v-for="level in levels" :key="level.id" class="col-level">
              <div class="feature-value" :class="getFeatureClass(feature, level)">
                {{ getFeatureValue(feature, level) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 升级任务 -->
    <div class="upgrade-tasks-section">
      <h2>升级任务</h2>
      <div class="tasks-list">
        <div v-for="task in upgradeTasks" :key="task.id" class="task-item" :class="{ completed: task.completed }">
          <div class="task-icon">
            <el-icon v-if="task.completed"><Check /></el-icon>
            <span v-else>{{ task.icon }}</span>
          </div>
          <div class="task-content">
            <h3 class="task-title">{{ task.title }}</h3>
            <p class="task-desc">{{ task.description }}</p>
            <div class="task-progress" v-if="!task.completed">
              <span>{{ task.progress }} / {{ task.requirement }}</span>
            </div>
          </div>
          <div class="task-reward">
            <span class="reward-text">升级后解锁</span>
            <el-button v-if="!task.completed" type="primary" size="small" @click="completeTask(task)">
              完成
            </el-button>
            <el-tag v-else type="success">已完成</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 特殊权益 -->
    <div class="special-rights-section">
      <h2>特殊权益</h2>
      <div class="special-grid">
        <div v-for="special in specialRights" :key="special.id" class="special-card">
          <div class="special-header">
            <div class="special-icon">{{ special.icon }}</div>
            <div class="special-badge" :class="special.badge">{{ special.badgeText }}</div>
          </div>
          <div class="special-content">
            <h3 class="special-title">{{ special.title }}</h3>
            <p class="special-desc">{{ special.description }}</p>
            <div class="special-benefits">
              <span v-for="benefit in special.benefits" :key="benefit" class="benefit-item">{{ benefit }}</span>
            </div>
          </div>
          <div class="special-action">
            <el-button type="primary" :disabled="!special.available">
              {{ special.available ? '立即申请' : '暂不可用' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Check } from '@element-plus/icons-vue'

// 概览数据
const currentLevel = ref(3)
const levelName = ref('活跃创作者')
const unlockedRights = ref(8)
const upgradeProgress = ref(65)

// 当前权益
const currentRights = ref([
  {
    id: 1,
    icon: '📹',
    title: '视频上传大小提升',
    description: '单个视频上传大小提升至 8GB',
    active: true
  },
  {
    id: 2,
    icon: '⚡',
    title: '优先审核通道',
    description: '视频审核时间缩短至 2 小时内',
    active: true
  },
  {
    id: 3,
    icon: '📊',
    title: '数据分析报告',
    description: '获得详细的数据分析报告',
    active: true
  },
  {
    id: 4,
    icon: '🎨',
    title: '专属创作工具',
    description: '使用高级创作工具和模板',
    active: false
  }
])

// 等级对比
const levels = ref([
  { id: 1, name: '新手', badge: 'basic', badgeText: '基础' },
  { id: 2, name: '活跃', badge: 'active', badgeText: '活跃' },
  { id: 3, name: '优质', badge: 'premium', badgeText: '优质' },
  { id: 4, name: '专业', badge: 'pro', badgeText: '专业' }
])

const features = ref([
  {
    id: 1,
    name: '视频上传大小',
    description: '单个视频最大上传大小',
    values: ['2GB', '4GB', '8GB', '16GB']
  },
  {
    id: 2,
    name: '审核时间',
    description: '视频审核等待时间',
    values: ['24小时', '12小时', '2小时', '1小时']
  },
  {
    id: 3,
    name: '数据分析',
    description: '数据报告详细程度',
    values: ['基础', '详细', '专业', '定制']
  },
  {
    id: 4,
    name: '创作工具',
    description: '可用创作工具数量',
    values: ['5个', '10个', '20个', '无限制']
  }
])

// 升级任务
const upgradeTasks = ref([
  {
    id: 1,
    icon: '📹',
    title: '发布 50 个视频',
    description: '累计发布 50 个视频作品',
    progress: 32,
    requirement: 50,
    completed: false
  },
  {
    id: 2,
    icon: '👥',
    title: '获得 1000 个粉丝',
    description: '累计获得 1000 个粉丝关注',
    progress: 650,
    requirement: 1000,
    completed: false
  },
  {
    id: 3,
    icon: '🔥',
    title: '视频播放量达到 10万',
    description: '单个视频播放量达到 10万次',
    progress: 75000,
    requirement: 100000,
    completed: false
  }
])

// 特殊权益
const specialRights = ref([
  {
    id: 1,
    icon: '🏆',
    title: '创作者认证',
    description: '获得官方认证标识，提升账号权威性',
    benefits: ['官方认证标识', '专属认证页面', '优先推荐机会'],
    badge: 'gold',
    badgeText: '黄金',
    available: true
  },
  {
    id: 2,
    icon: '💰',
    title: '创作激励计划',
    description: '参与创作激励，获得额外收益',
    benefits: ['创作激励金', '流量扶持', '专属活动'],
    badge: 'silver',
    badgeText: '白银',
    available: false
  },
  {
    id: 3,
    icon: '🎯',
    title: '精准推广服务',
    description: '获得专业的推广服务支持',
    benefits: ['专业推广指导', '定制推广方案', '效果数据分析'],
    badge: 'platinum',
    badgeText: '铂金',
    available: false
  }
])

const getFeatureClass = (feature, level) => {
  const currentLevelIndex = currentLevel.value - 1
  const levelIndex = level.id - 1
  
  if (levelIndex <= currentLevelIndex) {
    return 'available'
  } else if (levelIndex === currentLevelIndex + 1) {
    return 'next-level'
  } else {
    return 'locked'
  }
}

const getFeatureValue = (feature, level) => {
  return feature.values[level.id - 1]
}

const completeTask = (task) => {
  task.completed = true
  console.log('完成任务:', task.title)
}
</script>

<style lang="scss" scoped>
.rights-page {
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

.rights-overview {
  margin-bottom: 32px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.current-rights-section, .level-comparison-section, .upgrade-tasks-section, .special-rights-section {
  margin-bottom: 32px;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.rights-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.right-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 16px;
  transition: all 0.3s ease;
  
  &.active {
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
  }
  
  .right-icon {
    font-size: 24px;
    flex-shrink: 0;
  }
  
  .right-content {
    flex: 1;
    
    .right-title {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .right-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
    }
  }
}

.comparison-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.table-header {
  display: grid;
  grid-template-columns: 2fr repeat(4, 1fr);
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  font-weight: 600;
  color: #333;
  
  .col-feature {
    font-size: 16px;
  }
  
  .col-level {
    text-align: center;
    
    .level-name {
      font-size: 14px;
      margin-bottom: 4px;
    }
    
    .level-badge {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 12px;
      
      &.basic {
        background: #f0f0f0;
        color: #666;
      }
      
      &.active {
        background: #e6f7ff;
        color: #1890ff;
      }
      
      &.premium {
        background: #f6ffed;
        color: #52c41a;
      }
      
      &.pro {
        background: #fff7e6;
        color: #fa8c16;
      }
    }
  }
}

.table-body {
  .table-row {
    display: grid;
    grid-template-columns: 2fr repeat(4, 1fr);
    gap: 16px;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .col-feature {
      .feature-name {
        font-size: 14px;
        color: #333;
        display: block;
        margin-bottom: 4px;
      }
      
      .feature-desc {
        font-size: 12px;
        color: #666;
      }
    }
    
    .col-level {
      text-align: center;
      
      .feature-value {
        font-size: 14px;
        padding: 4px 8px;
        border-radius: 4px;
        
        &.available {
          background: #f6ffed;
          color: #52c41a;
        }
        
        &.next-level {
          background: #e6f7ff;
          color: #1890ff;
        }
        
        &.locked {
          background: #f5f5f5;
          color: #999;
        }
      }
    }
  }
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  
  &.completed {
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
  }
  
  .task-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
  }
  
  .task-content {
    flex: 1;
    
    .task-title {
      font-size: 16px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    .task-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 8px 0;
    }
    
    .task-progress {
      font-size: 12px;
      color: #999;
    }
  }
  
  .task-reward {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
    
    .reward-text {
      font-size: 12px;
      color: #409eff;
    }
  }
}

.special-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.special-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  
  .special-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .special-icon {
      font-size: 24px;
    }
    
    .special-badge {
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 12px;
      
      &.gold {
        background: #fff7e6;
        color: #fa8c16;
      }
      
      &.silver {
        background: #f0f0f0;
        color: #666;
      }
      
      &.platinum {
        background: #e6f7ff;
        color: #1890ff;
      }
    }
  }
  
  .special-content {
    margin-bottom: 16px;
    
    .special-title {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .special-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
    }
    
    .special-benefits {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .benefit-item {
        font-size: 12px;
        color: #666;
        position: relative;
        padding-left: 12px;
        
        &::before {
          content: '•';
          position: absolute;
          left: 0;
          color: #409eff;
        }
      }
    }
  }
  
  .special-action {
    .el-button {
      width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
  
  .rights-grid {
    grid-template-columns: 1fr;
  }
  
  .special-grid {
    grid-template-columns: 1fr;
  }
  
  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}
</style>
