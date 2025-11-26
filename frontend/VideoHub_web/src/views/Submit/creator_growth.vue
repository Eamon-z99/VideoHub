<template>
  <div class="creator-growth">
    <div class="growth-header">
      <h1>创作成长</h1>
      <p>提升你的创作技能，解锁更多创作权益</p>
    </div>

    <!-- 成长进度 -->
    <div class="progress-section">
      <h2>当前等级</h2>
      <div class="level-card">
        <div class="level-info">
          <div class="level-badge">
            <span class="level-number">{{ currentLevel }}</span>
            <span class="level-name">{{ levelName }}</span>
          </div>
          <div class="level-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
            </div>
            <div class="progress-text">
              <span>{{ currentExp }} / {{ nextLevelExp }} 经验值</span>
              <span>{{ nextLevelExp - currentExp }} 经验值升级</span>
            </div>
          </div>
        </div>
        <div class="level-benefits">
          <h3>当前权益</h3>
          <ul>
            <li v-for="benefit in currentBenefits" :key="benefit">{{ benefit }}</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 成长任务 -->
    <div class="tasks-section">
      <h2>成长任务</h2>
      <div class="tasks-grid">
        <div v-for="task in tasks" :key="task.id" class="task-card" :class="{ completed: task.completed }">
          <div class="task-icon">
            <el-icon v-if="task.completed"><Check /></el-icon>
            <span v-else>{{ task.icon }}</span>
          </div>
          <div class="task-content">
            <h3 class="task-title">{{ task.title }}</h3>
            <p class="task-desc">{{ task.description }}</p>
            <div class="task-reward">
              <span class="reward-exp">+{{ task.exp }} 经验</span>
              <span v-if="task.bonus" class="reward-bonus">{{ task.bonus }}</span>
            </div>
          </div>
          <div class="task-action">
            <el-button v-if="!task.completed" type="primary" size="small" @click="completeTask(task)">
              完成
            </el-button>
            <el-tag v-else type="success">已完成</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 技能树 -->
    <div class="skills-section">
      <h2>技能树</h2>
      <div class="skills-tree">
        <div v-for="skill in skills" :key="skill.id" class="skill-node" :class="{ unlocked: skill.unlocked }">
          <div class="skill-icon">{{ skill.icon }}</div>
          <div class="skill-info">
            <h4>{{ skill.name }}</h4>
            <p>{{ skill.description }}</p>
            <div class="skill-level">
              <span>等级: {{ skill.level }}/{{ skill.maxLevel }}</span>
              <div class="skill-progress">
                <div class="skill-progress-bar" :style="{ width: (skill.level / skill.maxLevel) * 100 + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 成就系统 -->
    <div class="achievements-section">
      <h2>成就徽章</h2>
      <div class="achievements-grid">
        <div v-for="achievement in achievements" :key="achievement.id" class="achievement-card" :class="{ earned: achievement.earned }">
          <div class="achievement-icon">{{ achievement.icon }}</div>
          <div class="achievement-info">
            <h4>{{ achievement.name }}</h4>
            <p>{{ achievement.description }}</p>
            <div class="achievement-progress" v-if="!achievement.earned">
              <span>{{ achievement.progress }} / {{ achievement.requirement }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Check } from '@element-plus/icons-vue'

// 当前等级信息
const currentLevel = ref(5)
const levelName = ref('活跃创作者')
const currentExp = ref(1250)
const nextLevelExp = ref(2000)
const progressPercentage = ref((currentExp.value / nextLevelExp.value) * 100)

const currentBenefits = ref([
  '视频上传大小提升至 8GB',
  '优先审核通道',
  '专属创作工具',
  '数据分析报告'
])

// 成长任务
const tasks = ref([
  {
    id: 1,
    icon: '📹',
    title: '发布第一个视频',
    description: '上传并发布你的第一个视频作品',
    exp: 100,
    completed: true
  },
  {
    id: 2,
    icon: '👥',
    title: '获得 100 个粉丝',
    description: '通过优质内容吸引更多关注者',
    exp: 200,
    bonus: '粉丝徽章',
    completed: true
  },
  {
    id: 3,
    icon: '🔥',
    title: '视频播放量达到 1万',
    description: '创作受欢迎的内容，提升播放量',
    exp: 300,
    completed: false
  },
  {
    id: 4,
    icon: '💬',
    title: '与粉丝互动 50 次',
    description: '回复评论，与粉丝建立良好关系',
    exp: 150,
    completed: false
  },
  {
    id: 5,
    icon: '📚',
    title: '完成创作学院课程',
    description: '学习创作技巧，提升内容质量',
    exp: 250,
    bonus: '学习徽章',
    completed: false
  }
])

// 技能树
const skills = ref([
  {
    id: 1,
    icon: '🎬',
    name: '视频剪辑',
    description: '提升视频剪辑技能，制作更专业的内容',
    level: 3,
    maxLevel: 5,
    unlocked: true
  },
  {
    id: 2,
    icon: '🎨',
    name: '封面设计',
    description: '学习封面设计技巧，提升视频点击率',
    level: 2,
    maxLevel: 5,
    unlocked: true
  },
  {
    id: 3,
    icon: '📝',
    name: '内容策划',
    description: '掌握内容策划方法，制定有效的内容策略',
    level: 1,
    maxLevel: 5,
    unlocked: true
  },
  {
    id: 4,
    icon: '📊',
    name: '数据分析',
    description: '学会分析数据，优化内容表现',
    level: 0,
    maxLevel: 3,
    unlocked: false
  }
])

// 成就系统
const achievements = ref([
  {
    id: 1,
    icon: '🏆',
    name: '新手上路',
    description: '发布第一个视频',
    earned: true
  },
  {
    id: 2,
    icon: '👑',
    name: '人气王',
    description: '单个视频播放量达到 10万',
    earned: false,
    progress: 25000,
    requirement: 100000
  },
  {
    id: 3,
    icon: '💎',
    name: '内容大师',
    description: '发布 50 个视频',
    earned: false,
    progress: 12,
    requirement: 50
  },
  {
    id: 4,
    icon: '🌟',
    name: '粉丝收割机',
    description: '获得 1000 个粉丝',
    earned: false,
    progress: 150,
    requirement: 1000
  }
])

const completeTask = (task) => {
  task.completed = true
  currentExp.value += task.exp
  progressPercentage.value = (currentExp.value / nextLevelExp.value) * 100
  
  if (currentExp.value >= nextLevelExp.value) {
    currentLevel.value++
    nextLevelExp.value += 1000
    progressPercentage.value = 0
  }
}
</script>

<style lang="scss" scoped>
.creator-growth {
  padding: 0;
}

.growth-header {
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

.progress-section, .tasks-section, .skills-section, .achievements-section {
  margin-bottom: 32px;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.level-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  gap: 32px;
}

.level-info {
  flex: 1;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  
  .level-number {
    font-size: 32px;
    font-weight: bold;
    color: #409eff;
  }
  
  .level-name {
    font-size: 18px;
    color: #333;
  }
}

.level-progress {
  .progress-bar {
    width: 100%;
    height: 8px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 8px;
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #409eff, #67c23a);
      transition: width 0.3s ease;
    }
  }
  
  .progress-text {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: #666;
  }
}

.level-benefits {
  h3 {
    font-size: 16px;
    margin: 0 0 12px 0;
    color: #333;
  }
  
  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      padding: 4px 0;
      color: #666;
      position: relative;
      padding-left: 16px;
      
      &::before {
        content: '✓';
        position: absolute;
        left: 0;
        color: #67c23a;
        font-weight: bold;
      }
    }
  }
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.task-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 12px;
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
    
    .task-reward {
      display: flex;
      gap: 8px;
      
      .reward-exp {
        font-size: 12px;
        color: #409eff;
        background: #ecf5ff;
        padding: 2px 6px;
        border-radius: 4px;
      }
      
      .reward-bonus {
        font-size: 12px;
        color: #67c23a;
        background: #f0f9ff;
        padding: 2px 6px;
        border-radius: 4px;
      }
    }
  }
}

.skills-tree {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.skill-node {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 12px;
  opacity: 0.6;
  
  &.unlocked {
    opacity: 1;
  }
  
  .skill-icon {
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
  
  .skill-info {
    flex: 1;
    
    h4 {
      font-size: 16px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0 0 8px 0;
    }
    
    .skill-level {
      font-size: 12px;
      color: #666;
      margin-bottom: 4px;
    }
    
    .skill-progress {
      width: 100%;
      height: 4px;
      background: #f0f0f0;
      border-radius: 2px;
      overflow: hidden;
      
      .skill-progress-bar {
        height: 100%;
        background: #409eff;
        transition: width 0.3s ease;
      }
    }
  }
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.achievement-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  text-align: center;
  opacity: 0.6;
  
  &.earned {
    opacity: 1;
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
  }
  
  .achievement-icon {
    font-size: 32px;
    margin-bottom: 8px;
  }
  
  .achievement-info {
    h4 {
      font-size: 14px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    p {
      font-size: 12px;
      color: #666;
      margin: 0 0 8px 0;
    }
    
    .achievement-progress {
      font-size: 12px;
      color: #999;
    }
  }
}

@media (max-width: 768px) {
  .level-card {
    flex-direction: column;
  }
  
  .tasks-grid,
  .skills-tree,
  .achievements-grid {
    grid-template-columns: 1fr;
  }
}
</style>
