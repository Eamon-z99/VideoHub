<template>
  <div class="classroom-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <input 
          type="text" 
          class="search-input" 
          placeholder="搜索课程"
          v-model="searchQuery"
        />
        <button class="search-btn">
          <img src="/assets/search.png" alt="搜索" class="search-icon" />
        </button>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="nav-container">
        <div 
          v-for="category in categories" 
          :key="category.id"
          class="nav-item"
          :class="{ active: activeCategory === category.id }"
          @click="setActiveCategory(category.id)"
        >
          {{ category.name }}
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧课程列表 -->
      <div class="courses-section">
        <div class="section-header">
          <h2 class="section-title">推荐课程</h2>
          <div class="filter-options">
            <select v-model="sortBy" class="sort-select">
              <option value="latest">最新</option>
              <option value="popular">最热</option>
              <option value="rating">评分</option>
            </select>
          </div>
        </div>
        
        <div class="courses-grid">
          <div 
            v-for="course in filteredCourses" 
            :key="course.id" 
            class="course-card"
            @click="viewCourse(course)"
          >
            <div class="course-thumbnail">
              <img :src="course.thumbnail" :alt="course.title" />
              <div class="course-duration">{{ course.duration }}</div>
              <div class="course-level">{{ course.level }}</div>
            </div>
            <div class="course-info">
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-description">{{ course.description }}</p>
              <div class="course-meta">
                <div class="instructor-info">
                  <div class="instructor-avatar" :style="{ backgroundColor: course.instructor.avatarColor }">
                    {{ course.instructor.avatarText }}
                  </div>
                  <span class="instructor-name">{{ course.instructor.name }}</span>
                </div>
                <div class="course-stats">
                  <span class="stat-item">
                    <span class="icon">👥</span>
                    {{ course.students }}
                  </span>
                  <span class="stat-item">
                    <span class="icon">⭐</span>
                    {{ course.rating }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧推荐侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-section">
          <h3 class="sidebar-title">热门课程</h3>
          <div class="sidebar-courses">
            <div 
              v-for="(course, index) in sidebarCourses" 
              :key="index" 
              class="sidebar-course"
              @click="viewCourse(course)"
            >
              <div class="sidebar-thumbnail">
                <img :src="course.thumbnail" :alt="course.title" />
              </div>
              <div class="sidebar-content">
                <h4 class="sidebar-course-title">{{ course.title }}</h4>
                <p class="sidebar-instructor">{{ course.instructor.name }}</p>
                <div class="sidebar-stats">
                  <span>{{ course.students }}人学习</span>
                  <span>{{ course.rating }}分</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">学习路径</h3>
          <div class="learning-paths">
            <div 
              v-for="path in learningPaths" 
              :key="path.id" 
              class="path-item"
            >
              <div class="path-icon">{{ path.icon }}</div>
              <div class="path-content">
                <h4 class="path-title">{{ path.title }}</h4>
                <p class="path-description">{{ path.description }}</p>
                <div class="path-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: path.progress + '%' }"></div>
                  </div>
                  <span class="progress-text">{{ path.progress }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 搜索查询
const searchQuery = ref('')
const sortBy = ref('latest')
const activeCategory = ref('all')

// 分类数据
const categories = ref([
  { id: 'all', name: '全部' },
  { id: 'programming', name: '编程开发' },
  { id: 'design', name: '设计创意' },
  { id: 'business', name: '商业管理' },
  { id: 'language', name: '语言学习' },
  { id: 'science', name: '科学教育' },
  { id: 'art', name: '艺术文化' },
  { id: 'life', name: '生活技能' }
])

// 课程数据
const courses = ref([
  {
    id: 1,
    title: "Vue.js 3.0 从入门到精通",
    description: "全面学习Vue.js 3.0的核心概念和高级特性，包括Composition API、响应式系统等",
    thumbnail: "/images/course1.jpg",
    duration: "12小时",
    level: "中级",
    instructor: {
      name: "张老师",
      avatarText: "张",
      avatarColor: "#3B82F6"
    },
    students: "2.3万",
    rating: "4.8",
    category: "programming"
  },
  {
    id: 2,
    title: "UI/UX设计实战课程",
    description: "从零开始学习现代UI/UX设计，掌握Figma、Sketch等设计工具",
    thumbnail: "/images/course2.jpg",
    duration: "8小时",
    level: "初级",
    instructor: {
      name: "李设计师",
      avatarText: "李",
      avatarColor: "#8B5CF6"
    },
    students: "1.8万",
    rating: "4.9",
    category: "design"
  },
  {
    id: 3,
    title: "Python数据分析与可视化",
    description: "使用Python进行数据分析，掌握pandas、matplotlib等库的使用",
    thumbnail: "/images/course3.jpg",
    duration: "15小时",
    level: "中级",
    instructor: {
      name: "王博士",
      avatarText: "王",
      avatarColor: "#10B981"
    },
    students: "3.1万",
    rating: "4.7",
    category: "programming"
  },
  {
    id: 4,
    title: "英语口语提升训练营",
    description: "通过情景对话和发音练习，快速提升英语口语水平",
    thumbnail: "/images/course4.jpg",
    duration: "20小时",
    level: "初级",
    instructor: {
      name: "Sarah老师",
      avatarText: "S",
      avatarColor: "#F59E0B"
    },
    students: "4.2万",
    rating: "4.9",
    category: "language"
  },
  {
    id: 5,
    title: "创业思维与商业模式设计",
    description: "学习创业思维，掌握商业模式设计的方法和工具",
    thumbnail: "/images/course5.jpg",
    duration: "10小时",
    level: "高级",
    instructor: {
      name: "陈教授",
      avatarText: "陈",
      avatarColor: "#EF4444"
    },
    students: "1.5万",
    rating: "4.6",
    category: "business"
  },
  {
    id: 6,
    title: "摄影构图与后期处理",
    description: "学习摄影构图技巧和Lightroom后期处理，提升摄影水平",
    thumbnail: "/images/course6.jpg",
    duration: "6小时",
    level: "初级",
    instructor: {
      name: "摄影师小刘",
      avatarText: "刘",
      avatarColor: "#06B6D4"
    },
    students: "2.8万",
    rating: "4.8",
    category: "art"
  }
])

// 侧边栏课程数据
const sidebarCourses = ref([
  {
    id: 7,
    title: "React Native移动开发",
    thumbnail: "/images/sidebar1.jpg",
    instructor: { name: "赵工程师" },
    students: "1.2万",
    rating: "4.7"
  },
  {
    id: 8,
    title: "产品经理必修课",
    thumbnail: "/images/sidebar2.jpg",
    instructor: { name: "产品经理小王" },
    students: "2.1万",
    rating: "4.8"
  },
  {
    id: 9,
    title: "机器学习入门",
    thumbnail: "/images/sidebar3.jpg",
    instructor: { name: "AI专家" },
    students: "3.5万",
    rating: "4.9"
  }
])

// 学习路径数据
const learningPaths = ref([
  {
    id: 1,
    icon: "💻",
    title: "前端开发路径",
    description: "从HTML到React的完整学习路径",
    progress: 65
  },
  {
    id: 2,
    icon: "🎨",
    title: "UI设计路径",
    description: "从基础设计到高级UI的成长之路",
    progress: 30
  },
  {
    id: 3,
    icon: "📊",
    title: "数据分析路径",
    description: "Python数据分析师养成计划",
    progress: 80
  }
])

// 计算属性：过滤后的课程
const filteredCourses = computed(() => {
  let filtered = courses.value

  // 按分类过滤
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(course => course.category === activeCategory.value)
  }

  // 按搜索查询过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(course => 
      course.title.toLowerCase().includes(query) ||
      course.description.toLowerCase().includes(query) ||
      course.instructor.name.toLowerCase().includes(query)
    )
  }

  // 排序
  switch (sortBy.value) {
    case 'popular':
      return filtered.sort((a, b) => parseFloat(b.students) - parseFloat(a.students))
    case 'rating':
      return filtered.sort((a, b) => parseFloat(b.rating) - parseFloat(a.rating))
    default:
      return filtered
  }
})

// 方法
const setActiveCategory = (categoryId) => {
  activeCategory.value = categoryId
}

const viewCourse = (course) => {
  console.log('查看课程:', course.title)
  // 这里可以添加跳转到课程详情页的逻辑
}
</script>

<style lang="scss" scoped>
// 变量定义
$primary-color: #00aeec;
$background-color: #f5f7fa;
$white: #fff;
$text-primary: #333;
$text-secondary: #666;
$text-muted: #888;
$border-color: #e5e7eb;
$border-radius: 8px;
$spacing-sm: 8px;
$spacing-md: 12px;
$spacing-lg: 16px;
$spacing-xl: 24px;

.classroom-page {
  background-color: $background-color;
  min-height: 100vh;
  padding: $spacing-lg;
}

// 搜索头部
.search-header {
  background: $white;
  padding: $spacing-lg;
  border-radius: $border-radius;
  margin-bottom: $spacing-lg;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  .search-container {
    display: flex;
    align-items: center;
    max-width: 400px;
    margin-left: auto;
    background: #f7f8fa;
    border-radius: 6px;
    border: 1px solid $border-color;

    .search-input {
      flex: 1;
      padding: 10px $spacing-md;
      border: none;
      outline: none;
      background: transparent;
      font-size: 14px;

      &::placeholder {
        color: $text-muted;
      }
    }

    .search-btn {
      padding: 8px;
      border: none;
      background: transparent;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;

      .search-icon {
        width: 16px;
        height: 16px;
        opacity: 0.6;
      }

      &:hover .search-icon {
        opacity: 1;
      }
    }
  }
}

// 分类导航
.category-nav {
  background: $white;
  padding: $spacing-md $spacing-lg;
  border-radius: $border-radius;
  margin-bottom: $spacing-lg;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  .nav-container {
    display: flex;
    gap: $spacing-md;
    overflow-x: auto;
    padding-bottom: 4px;

    .nav-item {
      padding: 8px 16px;
      border-radius: 20px;
      background: #f0f0f0;
      color: $text-secondary;
      cursor: pointer;
      transition: all 0.2s ease;
      white-space: nowrap;
      font-size: 14px;

      &:hover {
        background: #e0e0e0;
        color: $text-primary;
      }

      &.active {
        background: $primary-color;
        color: $white;
      }
    }
  }
}

// 主要内容区域
.main-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: $spacing-xl;
  max-width: 1200px;
  margin: 0 auto;
}

// 课程区域
.courses-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;

    .section-title {
      font-size: 20px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
    }

    .filter-options {
      .sort-select {
        padding: 6px 12px;
        border: 1px solid $border-color;
        border-radius: 4px;
        background: $white;
        font-size: 14px;
        cursor: pointer;
      }
    }
  }

  .courses-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: $spacing-lg;
  }

  .course-card {
    background: $white;
    border-radius: $border-radius;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;
    cursor: pointer;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }

    .course-thumbnail {
      position: relative;
      width: 100%;
      height: 180px;
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
        background: rgba(0, 0, 0, 0.7);
        color: $white;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
      }

      .course-level {
        position: absolute;
        top: 8px;
        left: 8px;
        background: $primary-color;
        color: $white;
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 12px;
      }
    }

    .course-info {
      padding: $spacing-lg;

      .course-title {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin: 0 0 $spacing-sm 0;
        line-height: 1.4;
      }

      .course-description {
        color: $text-secondary;
        font-size: 14px;
        line-height: 1.5;
        margin: 0 0 $spacing-md 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .course-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .instructor-info {
          display: flex;
          align-items: center;
          gap: $spacing-sm;

          .instructor-avatar {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: $white;
            font-size: 12px;
            font-weight: 600;
          }

          .instructor-name {
            font-size: 13px;
            color: $text-primary;
            font-weight: 500;
          }
        }

        .course-stats {
          display: flex;
          gap: $spacing-md;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: $text-muted;

            .icon {
              font-size: 12px;
            }
          }
        }
      }
    }
  }
}

// 侧边栏
.sidebar {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;

  .sidebar-section {
    background: $white;
    border-radius: $border-radius;
    padding: $spacing-lg;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    .sidebar-title {
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
      margin: 0 0 $spacing-md 0;
    }
  }

  .sidebar-courses {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;

    .sidebar-course {
      display: flex;
      gap: $spacing-md;
      cursor: pointer;
      transition: background-color 0.2s ease;
      padding: $spacing-sm;
      border-radius: 4px;

      &:hover {
        background: #f8f9fa;
      }

      .sidebar-thumbnail {
        width: 60px;
        height: 40px;
        border-radius: 4px;
        overflow: hidden;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .sidebar-content {
        flex: 1;

        .sidebar-course-title {
          font-size: 13px;
          font-weight: 500;
          color: $text-primary;
          margin: 0 0 4px 0;
          line-height: 1.3;
        }

        .sidebar-instructor {
          font-size: 12px;
          color: $text-muted;
          margin: 0 0 4px 0;
        }

        .sidebar-stats {
          display: flex;
          gap: $spacing-sm;
          font-size: 11px;
          color: $text-muted;
        }
      }
    }
  }

  .learning-paths {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;

    .path-item {
      display: flex;
      gap: $spacing-md;
      padding: $spacing-sm;
      border-radius: 4px;
      transition: background-color 0.2s ease;

      &:hover {
        background: #f8f9fa;
      }

      .path-icon {
        font-size: 20px;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f0f0;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .path-content {
        flex: 1;

        .path-title {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
          margin: 0 0 4px 0;
        }

        .path-description {
          font-size: 12px;
          color: $text-muted;
          margin: 0 0 $spacing-sm 0;
        }

        .path-progress {
          display: flex;
          align-items: center;
          gap: $spacing-sm;

          .progress-bar {
            flex: 1;
            height: 4px;
            background: #e5e7eb;
            border-radius: 2px;
            overflow: hidden;

            .progress-fill {
              height: 100%;
              background: $primary-color;
              transition: width 0.3s ease;
            }
          }

          .progress-text {
            font-size: 11px;
            color: $text-muted;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: $spacing-lg;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }

  .search-header .search-container {
    max-width: 100%;
  }

  .category-nav .nav-container {
    gap: $spacing-sm;
  }

  .nav-item {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>
