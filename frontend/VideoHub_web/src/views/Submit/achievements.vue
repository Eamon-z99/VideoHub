<template>
  <div class="achievements-page">
    <div class="page-header">
      <h1>任务成就</h1>
      <p>完成各种任务，解锁专属成就徽章</p>
    </div>

    <!-- 成就统计 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">🏆</div>
          <div class="stat-content">
            <div class="stat-value">{{ totalAchievements }}</div>
            <div class="stat-label">已获得成就</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⭐</div>
          <div class="stat-content">
            <div class="stat-value">{{ totalPoints }}</div>
            <div class="stat-label">成就积分</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🔥</div>
          <div class="stat-content">
            <div class="stat-value">{{ currentStreak }}</div>
            <div class="stat-label">连续完成天数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📈</div>
          <div class="stat-content">
            <div class="stat-value">{{ rank }}</div>
            <div class="stat-label">成就排名</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 每日任务 -->
    <div class="daily-tasks-section">
      <h2>每日任务</h2>
      <div class="tasks-list">
        <div v-for="task in dailyTasks" :key="task.id" class="task-item" :class="{ completed: task.completed }">
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
            <span class="reward-points">+{{ task.points }} 积分</span>
            <el-button v-if="!task.completed" type="primary" size="small" @click="completeTask(task)">
              完成
            </el-button>
            <el-tag v-else type="success">已完成</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 成就分类 -->
    <div class="achievements-section">
      <div class="section-header">
        <h2>成就徽章</h2>
        <div class="filter-tabs">
          <el-button 
            v-for="category in categories" 
            :key="category.key"
            :type="activeCategory === category.key ? 'primary' : ''"
            @click="activeCategory = category.key"
          >
            {{ category.label }}
          </el-button>
        </div>
      </div>
      
      <div class="achievements-grid">
        <div v-for="achievement in filteredAchievements" :key="achievement.id" class="achievement-card" :class="{ earned: achievement.earned }">
          <div class="achievement-icon">{{ achievement.icon }}</div>
          <div class="achievement-info">
            <h3 class="achievement-name">{{ achievement.name }}</h3>
            <p class="achievement-desc">{{ achievement.description }}</p>
            <div class="achievement-meta">
              <span class="achievement-points">{{ achievement.points }} 积分</span>
              <span class="achievement-rare" :class="achievement.rarity">{{ achievement.rarityText }}</span>
            </div>
            <div v-if="!achievement.earned" class="achievement-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (achievement.progress / achievement.requirement) * 100 + '%' }"></div>
              </div>
              <span class="progress-text">{{ achievement.progress }} / {{ achievement.requirement }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近获得 -->
    <div class="recent-section">
      <h2>最近获得</h2>
      <div class="recent-list">
        <div v-for="recent in recentAchievements" :key="recent.id" class="recent-item">
          <div class="recent-icon">{{ recent.icon }}</div>
          <div class="recent-info">
            <h4>{{ recent.name }}</h4>
            <p>{{ recent.description }}</p>
            <span class="recent-date">{{ recent.date }}</span>
          </div>
          <div class="recent-points">+{{ recent.points }} 积分</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Check } from '@element-plus/icons-vue'

// 统计数据
const totalAchievements = ref(12)
const totalPoints = ref(2450)
const currentStreak = ref(7)
const rank = ref('前 10%')

// 当前分类
const activeCategory = ref('all')

// 分类选项
const categories = ref([
  { key: 'all', label: '全部' },
  { key: 'content', label: '内容创作' },
  { key: 'social', label: '社交互动' },
  { key: 'growth', label: '成长进阶' },
  { key: 'special', label: '特殊成就' }
])

// 每日任务
const dailyTasks = ref([
  {
    id: 1,
    icon: '📹',
    title: '发布视频',
    description: '发布一个视频作品',
    progress: 0,
    requirement: 1,
    points: 50,
    completed: false
  },
  {
    id: 2,
    icon: '💬',
    title: '回复评论',
    description: '回复粉丝评论 5 次',
    progress: 3,
    requirement: 5,
    points: 30,
    completed: false
  },
  {
    id: 3,
    icon: '👥',
    title: '关注新用户',
    description: '关注 3 个新用户',
    progress: 1,
    requirement: 3,
    points: 20,
    completed: false
  },
  {
    id: 4,
    icon: '📚',
    title: '学习课程',
    description: '完成一节创作学院课程',
    progress: 0,
    requirement: 1,
    points: 40,
    completed: false
  }
])

// 成就数据
const achievements = ref([
  {
    id: 1,
    icon: '🏆',
    name: '新手上路',
    description: '发布第一个视频',
    points: 100,
    category: 'content',
    earned: true,
    rarity: 'common',
    rarityText: '普通'
  },
  {
    id: 2,
    icon: '👑',
    name: '人气王',
    description: '单个视频播放量达到 10万',
    points: 500,
    category: 'content',
    earned: false,
    progress: 25000,
    requirement: 100000,
    rarity: 'rare',
    rarityText: '稀有'
  },
  {
    id: 3,
    icon: '💎',
    name: '内容大师',
    description: '发布 50 个视频',
    points: 800,
    category: 'content',
    earned: false,
    progress: 12,
    requirement: 50,
    rarity: 'epic',
    rarityText: '史诗'
  },
  {
    id: 4,
    icon: '🌟',
    name: '粉丝收割机',
    description: '获得 1000 个粉丝',
    points: 1000,
    category: 'social',
    earned: false,
    progress: 150,
    requirement: 1000,
    rarity: 'legendary',
    rarityText: '传说'
  },
  {
    id: 5,
    icon: '🔥',
    name: '连续创作',
    description: '连续 30 天发布内容',
    points: 600,
    category: 'growth',
    earned: false,
    progress: 7,
    requirement: 30,
    rarity: 'rare',
    rarityText: '稀有'
  },
  {
    id: 6,
    icon: '🎨',
    name: '封面大师',
    description: '制作 100 个封面',
    points: 300,
    category: 'content',
    earned: true,
    rarity: 'common',
    rarityText: '普通'
  }
])

// 最近获得的成就
const recentAchievements = ref([
  {
    id: 1,
    icon: '🎨',
    name: '封面大师',
    description: '制作 100 个封面',
    points: 300,
    date: '2 天前'
  },
  {
    id: 2,
    icon: '📚',
    name: '学习达人',
    description: '完成 10 节课程',
    points: 200,
    date: '5 天前'
  },
  {
    id: 3,
    icon: '💬',
    name: '互动专家',
    description: '回复 100 条评论',
    points: 150,
    date: '1 周前'
  }
])

// 过滤后的成就
const filteredAchievements = computed(() => {
  if (activeCategory.value === 'all') {
    return achievements.value
  }
  return achievements.value.filter(achievement => achievement.category === activeCategory.value)
})

const completeTask = (task) => {
  task.completed = true
  totalPoints.value += task.points
  console.log(`完成任务: ${task.title}, 获得 ${task.points} 积分`)
}
</script>

<style lang="scss" scoped>
.achievements-page {
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

.stats-section {
  margin-bottom: 32px;
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

.daily-tasks-section, .achievements-section, .recent-section {
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
  
  .filter-tabs {
    display: flex;
    gap: 8px;
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
    
    .reward-points {
      font-size: 12px;
      color: #409eff;
      background: #ecf5ff;
      padding: 2px 8px;
      border-radius: 12px;
    }
  }
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.achievement-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 16px;
  opacity: 0.6;
  transition: all 0.3s ease;
  
  &.earned {
    opacity: 1;
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
  }
  
  .achievement-icon {
    font-size: 40px;
    flex-shrink: 0;
  }
  
  .achievement-info {
    flex: 1;
    
    .achievement-name {
      font-size: 18px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .achievement-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
    }
    
    .achievement-meta {
      display: flex;
      gap: 12px;
      margin-bottom: 12px;
      
      .achievement-points {
        font-size: 12px;
        color: #409eff;
        background: #ecf5ff;
        padding: 4px 8px;
        border-radius: 12px;
      }
      
      .achievement-rare {
        font-size: 12px;
        padding: 4px 8px;
        border-radius: 12px;
        
        &.common {
          background: #f0f0f0;
          color: #666;
        }
        
        &.rare {
          background: #e6f7ff;
          color: #1890ff;
        }
        
        &.epic {
          background: #f6ffed;
          color: #52c41a;
        }
        
        &.legendary {
          background: #fff7e6;
          color: #fa8c16;
        }
      }
    }
    
    .achievement-progress {
      .progress-bar {
        width: 100%;
        height: 6px;
        background: #f0f0f0;
        border-radius: 3px;
        overflow: hidden;
        margin-bottom: 4px;
        
        .progress-fill {
          height: 100%;
          background: #409eff;
          transition: width 0.3s ease;
        }
      }
      
      .progress-text {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  
  .recent-icon {
    font-size: 32px;
    flex-shrink: 0;
  }
  
  .recent-info {
    flex: 1;
    
    h4 {
      font-size: 16px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0 0 4px 0;
    }
    
    .recent-date {
      font-size: 12px;
      color: #999;
    }
  }
  
  .recent-points {
    font-size: 14px;
    color: #409eff;
    font-weight: 600;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .achievements-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
