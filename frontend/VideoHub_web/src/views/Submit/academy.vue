<template>
  <div class="academy-page">
    <div class="page-header">
      <h1>创作学院</h1>
      <p>学习专业创作技巧，提升内容质量，成为优秀的创作者</p>
    </div>

    <!-- 学习进度 -->
    <div class="progress-section">
      <div class="progress-card">
        <div class="progress-info">
          <h2>学习进度</h2>
          <div class="progress-stats">
            <div class="stat-item">
              <span class="stat-value">{{ completedCourses }}</span>
              <span class="stat-label">已完成课程</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ totalHours }}</span>
              <span class="stat-label">学习时长</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ currentLevel }}</span>
              <span class="stat-label">当前等级</span>
            </div>
          </div>
        </div>
        <div class="progress-visual">
          <div class="level-circle">
            <div class="level-number">{{ currentLevel }}</div>
            <div class="level-text">等级</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐课程 -->
    <div class="recommended-section">
      <h2>推荐课程</h2>
      <div class="courses-grid">
        <div v-for="course in recommendedCourses" :key="course.id" class="course-card">
          <div class="course-thumbnail">
            <img :src="course.thumbnail" :alt="course.title" />
            <div class="course-duration">{{ course.duration }}</div>
            <div class="course-level" :class="course.level">{{ course.levelText }}</div>
          </div>
          <div class="course-content">
            <h3 class="course-title">{{ course.title }}</h3>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-meta">
              <div class="course-stats">
                <span class="students">{{ course.students }} 人学习</span>
                <span class="rating">⭐ {{ course.rating }}</span>
              </div>
              <div class="course-price">
                <span v-if="course.price === 0" class="free">免费</span>
                <span v-else class="price">¥{{ course.price }}</span>
              </div>
            </div>
            <el-button type="primary" class="course-btn">开始学习</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程分类 -->
    <div class="categories-section">
      <h2>课程分类</h2>
      <div class="categories-grid">
        <div v-for="category in categories" :key="category.id" class="category-card">
          <div class="category-icon">{{ category.icon }}</div>
          <h3 class="category-name">{{ category.name }}</h3>
          <p class="category-desc">{{ category.description }}</p>
          <div class="category-stats">
            <span>{{ category.courseCount }} 门课程</span>
          </div>
          <el-button type="primary" plain class="category-btn">查看课程</el-button>
        </div>
      </div>
    </div>

    <!-- 我的学习 -->
    <div class="my-learning-section">
      <h2>我的学习</h2>
      <div class="learning-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="进行中" name="ongoing">
            <div class="learning-list">
              <div v-for="course in ongoingCourses" :key="course.id" class="learning-item">
                <div class="learning-thumbnail">
                  <img :src="course.thumbnail" :alt="course.title" />
                </div>
                <div class="learning-content">
                  <h3 class="learning-title">{{ course.title }}</h3>
                  <p class="learning-desc">{{ course.description }}</p>
                  <div class="learning-progress">
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
                    </div>
                    <span class="progress-text">{{ course.progress }}% 完成</span>
                  </div>
                </div>
                <div class="learning-actions">
                  <el-button type="primary" @click="continueLearning(course)">继续学习</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="已完成" name="completed">
            <div class="learning-list">
              <div v-for="course in completedCoursesList" :key="course.id" class="learning-item">
                <div class="learning-thumbnail">
                  <img :src="course.thumbnail" :alt="course.title" />
                  <div class="completed-badge">✓</div>
                </div>
                <div class="learning-content">
                  <h3 class="learning-title">{{ course.title }}</h3>
                  <p class="learning-desc">{{ course.description }}</p>
                  <div class="learning-meta">
                    <span class="completed-date">完成时间: {{ course.completedDate }}</span>
                    <span class="certificate">获得证书</span>
                  </div>
                </div>
                <div class="learning-actions">
                  <el-button @click="viewCertificate(course)">查看证书</el-button>
                  <el-button type="primary" plain @click="reviewCourse(course)">重新学习</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 学习成就 -->
    <div class="achievements-section">
      <h2>学习成就</h2>
      <div class="achievements-grid">
        <div v-for="achievement in learningAchievements" :key="achievement.id" class="achievement-card" :class="{ earned: achievement.earned }">
          <div class="achievement-icon">{{ achievement.icon }}</div>
          <div class="achievement-info">
            <h3 class="achievement-name">{{ achievement.name }}</h3>
            <p class="achievement-desc">{{ achievement.description }}</p>
            <div v-if="!achievement.earned" class="achievement-progress">
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

// 学习进度数据
const completedCourses = ref(8)
const totalHours = ref('24.5')
const currentLevel = ref(3)

// 推荐课程
const recommendedCourses = ref([
  {
    id: 1,
    title: '视频剪辑基础教程',
    description: '从零开始学习视频剪辑，掌握基本操作技巧',
    thumbnail: '/placeholder-course.jpg',
    duration: '2小时30分',
    students: '1.2万',
    rating: 4.8,
    price: 0,
    level: 'beginner',
    levelText: '初级'
  },
  {
    id: 2,
    title: '如何制作吸引人的封面',
    description: '学习封面设计原理，提升视频点击率',
    thumbnail: '/placeholder-course.jpg',
    duration: '1小时45分',
    students: '8.5千',
    rating: 4.6,
    price: 99,
    level: 'intermediate',
    levelText: '中级'
  },
  {
    id: 3,
    title: '内容创作策略',
    description: '了解用户喜好，制定有效的内容策略',
    thumbnail: '/placeholder-course.jpg',
    duration: '3小时15分',
    students: '2.1万',
    rating: 4.9,
    price: 199,
    level: 'advanced',
    levelText: '高级'
  }
])

// 课程分类
const categories = ref([
  {
    id: 1,
    icon: '🎬',
    name: '视频制作',
    description: '学习专业的视频制作技巧',
    courseCount: 15
  },
  {
    id: 2,
    icon: '🎨',
    name: '设计美学',
    description: '提升视觉设计能力',
    courseCount: 12
  },
  {
    id: 3,
    icon: '📝',
    name: '内容策划',
    description: '掌握内容创作策略',
    courseCount: 8
  },
  {
    id: 4,
    icon: '📊',
    name: '数据分析',
    description: '学会分析数据优化内容',
    courseCount: 6
  }
])

// 我的学习
const activeTab = ref('ongoing')

const ongoingCourses = ref([
  {
    id: 1,
    title: '高级视频剪辑技巧',
    description: '学习专业的视频剪辑技术，提升作品质量',
    thumbnail: '/placeholder-course.jpg',
    progress: 65
  },
  {
    id: 2,
    title: '封面设计进阶',
    description: '掌握高级封面设计技巧，提升视觉冲击力',
    thumbnail: '/placeholder-course.jpg',
    progress: 30
  }
])

const completedCoursesList = ref([
  {
    id: 1,
    title: '视频剪辑基础教程',
    description: '从零开始学习视频剪辑，掌握基本操作技巧',
    thumbnail: '/placeholder-course.jpg',
    completedDate: '2024-01-10'
  },
  {
    id: 2,
    title: '内容创作入门',
    description: '了解内容创作的基本概念和方法',
    thumbnail: '/placeholder-course.jpg',
    completedDate: '2024-01-05'
  }
])

// 学习成就
const learningAchievements = ref([
  {
    id: 1,
    icon: '🎓',
    name: '学习新手',
    description: '完成第一门课程',
    earned: true
  },
  {
    id: 2,
    icon: '📚',
    name: '知识探索者',
    description: '完成 10 门课程',
    earned: false,
    progress: 8,
    requirement: 10
  },
  {
    id: 3,
    icon: '⏰',
    name: '时间管理大师',
    description: '连续学习 7 天',
    earned: false,
    progress: 3,
    requirement: 7
  }
])

const continueLearning = (course) => {
  console.log('继续学习:', course.title)
}

const viewCertificate = (course) => {
  console.log('查看证书:', course.title)
}

const reviewCourse = (course) => {
  console.log('重新学习:', course.title)
}
</script>

<style lang="scss" scoped>
.academy-page {
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

.progress-section {
  margin-bottom: 32px;
}

.progress-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.progress-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  text-align: center;
  
  .stat-value {
    display: block;
    font-size: 24px;
    font-weight: 600;
    color: #409eff;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #666;
  }
}

.progress-visual {
  .level-circle {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409eff, #67c23a);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    
    .level-number {
      font-size: 24px;
      font-weight: 600;
    }
    
    .level-text {
      font-size: 12px;
    }
  }
}

.recommended-section, .categories-section, .my-learning-section, .achievements-section {
  margin-bottom: 32px;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.course-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.course-thumbnail {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .course-duration {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background: rgba(0,0,0,0.7);
    color: white;
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;
  }
  
  .course-level {
    position: absolute;
    top: 8px;
    left: 8px;
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;
    
    &.beginner {
      background: #e6f7ff;
      color: #1890ff;
    }
    
    &.intermediate {
      background: #f6ffed;
      color: #52c41a;
    }
    
    &.advanced {
      background: #fff7e6;
      color: #fa8c16;
    }
  }
}

.course-content {
  padding: 16px;
  
  .course-title {
    font-size: 16px;
    margin: 0 0 8px 0;
    color: #333;
  }
  
  .course-desc {
    font-size: 14px;
    color: #666;
    margin: 0 0 12px 0;
    line-height: 1.5;
  }
  
  .course-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .course-stats {
      display: flex;
      gap: 12px;
      font-size: 12px;
      color: #666;
    }
    
    .course-price {
      .free {
        color: #67c23a;
        font-weight: 600;
      }
      
      .price {
        color: #409eff;
        font-weight: 600;
      }
    }
  }
  
  .course-btn {
    width: 100%;
  }
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.category-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  
  .category-icon {
    font-size: 32px;
    margin-bottom: 12px;
  }
  
  .category-name {
    font-size: 16px;
    margin: 0 0 8px 0;
    color: #333;
  }
  
  .category-desc {
    font-size: 14px;
    color: #666;
    margin: 0 0 12px 0;
  }
  
  .category-stats {
    font-size: 12px;
    color: #999;
    margin-bottom: 16px;
  }
  
  .category-btn {
    width: 100%;
  }
}

.learning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.learning-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 16px;
  align-items: center;
  
  .learning-thumbnail {
    position: relative;
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
    
    .completed-badge {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #67c23a;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
    }
  }
  
  .learning-content {
    flex: 1;
    
    .learning-title {
      font-size: 16px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    .learning-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 8px 0;
    }
    
    .learning-progress {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .progress-bar {
        flex: 1;
        height: 6px;
        background: #f0f0f0;
        border-radius: 3px;
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          background: #409eff;
          transition: width 0.3s ease;
        }
      }
      
      .progress-text {
        font-size: 12px;
        color: #666;
      }
    }
    
    .learning-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #666;
      
      .certificate {
        color: #67c23a;
        font-weight: 600;
      }
    }
  }
  
  .learning-actions {
    display: flex;
    gap: 8px;
  }
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.achievement-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 12px;
  opacity: 0.6;
  
  &.earned {
    opacity: 1;
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
  }
  
  .achievement-icon {
    font-size: 24px;
    flex-shrink: 0;
  }
  
  .achievement-info {
    flex: 1;
    
    .achievement-name {
      font-size: 14px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    .achievement-desc {
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
  .progress-card {
    flex-direction: column;
    text-align: center;
  }
  
  .progress-stats {
    justify-content: center;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
  
  .categories-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .learning-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>
