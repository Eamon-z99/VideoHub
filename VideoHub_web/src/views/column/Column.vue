<template>
  <div class="column-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-container">
        <input 
          type="text" 
          class="search-input" 
          placeholder="搜索专栏文章"
        />
        <button class="search-btn">
          <img src="/assets/search.png" alt="搜索" class="search-icon" />
        </button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧推荐文章列表 -->
      <div class="articles-section">
        <h2 class="section-title">推荐文章</h2>
        <div class="articles-list">
          <article 
            v-for="article in articles" 
            :key="article.id" 
            class="article-card"
          >
            <div class="article-header">
              <h3 class="article-title">{{ article.title }}</h3>
              <span class="article-source">{{ article.source }}</span>
            </div>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <div class="author-info">
                <div class="author-avatar" :style="{ backgroundColor: article.author.avatarColor }">
                  {{ article.author.avatarText }}
                </div>
                <span class="author-name">{{ article.author.name }}</span>
                <span class="article-category">{{ article.category }}</span>
              </div>
              <div class="article-stats">
                <span class="stat-item">
                  <span class="icon-placeholder"></span>
                  {{ article.views }}
                </span>
                <span class="stat-item">
                  <span class="icon-placeholder"></span>
                  {{ article.likes }}
                </span>
                <span class="stat-item">
                  <span class="icon-placeholder"></span>
                  {{ article.comments }}
                </span>
              </div>
            </div>
          </article>
        </div>
      </div>

      <!-- 右侧文章预览侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-articles">
          <div 
            v-for="(preview, index) in sidebarPreviews" 
            :key="index" 
            class="sidebar-item"
          >
            <div class="preview-thumbnail"></div>
            <div class="preview-content">
              <p class="preview-text">{{ preview.text }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 推荐文章数据
const articles = ref([
  {
    id: 1,
    title: "枷锁chapter1",
    source: "来自文集",
    excerpt: "破镜重圆 追夫he勿上升本人!! 枷锁 chapter1 巴黎的瞬逝,迎来了洛杉矶的黎明。"莎头,...",
    author: {
      name: "Waekk",
      avatarText: "W",
      avatarColor: "#8B5CF6"
    },
    category: "日常",
    views: "6.4万",
    likes: "432",
    comments: "10"
  },
  {
    id: 2,
    title: "这一次,一定要赢。",
    source: "全部笔记",
    excerpt: "你本该赢,而不是一定要赢如果你输了,那就证明国服要开启武士大决斗时代了",
    author: {
      name: "图吧王老五",
      avatarText: "图",
      avatarColor: "#F59E0B"
    },
    category: "",
    views: "729",
    likes: "2896",
    comments: "0"
  },
  {
    id: 3,
    title: "莎头/无尽夏(一)(二)",
    source: "来自文集",
    excerpt: "(一)孙颖莎站在门口,目光注视着屋内的陈设,久久没有离开视线。过了好久才把大门掩上,来到院...",
    author: {
      name: "椰子味豆沙",
      avatarText: "椰",
      avatarColor: "#F59E0B"
    },
    category: "原创连载",
    views: "5.0万",
    likes: "405",
    comments: "11"
  },
  {
    id: 4,
    title: "Chapter1·缘分",
    source: "来自文集",
    excerpt: "少男少女的恋爱心事 | 伪破镜重圆 欢迎评论???这是我继续创作的动力?执子之笔已完结 BGM.稳稳...",
    author: {
      name: "执子之笔",
      avatarText: "执",
      avatarColor: "#EF4444"
    },
    category: "日常",
    views: "1.8万",
    likes: "179",
    comments: "21"
  },
  {
    id: 5,
    title: "小愿,小愿",
    source: "来自文集",
    excerpt: "【!!!注意:都是假的!OOC!不要上升本人!不要带入!以及,我创作的纸片人在我这里存在于...",
    author: {
      name: "喵的小树林",
      avatarText: "喵",
      avatarColor: "#F59E0B"
    },
    category: "日常",
    views: "2.4万",
    likes: "250",
    comments: "8"
  },
  {
    id: 6,
    title: "缘分1",
    source: "来自文集",
    excerpt: "Au向,小众护肤品公司销售总监X新晋影帝久别重逢论有一个影帝同学的好处就是,我可以硬拉他...",
    author: {
      name: "滚蛋吃屁",
      avatarText: "滚",
      avatarColor: "#F59E0B"
    },
    category: "日常",
    views: "1.9万",
    likes: "256",
    comments: "16"
  }
])

// 侧边栏预览数据
const sidebarPreviews = ref([
  { text: "破镜重圆 追夫he 勿上升本人!! 枷锁 chapter1 巴黎的瞬逝,迎来了洛杉..." },
  { text: "神这一块/ 19 2008 回复" },
  { text: "(一)孙颖莎站在门口,目光注视着屋内的陈设,久久没有离开视线。过了好久才把大..." },
  { text: "少男少女的恋爱心事 | 伪破镜重圆 欢迎评论???这是我继续创作的动力 执子之笔...." },
  { text: "【!!!注意:都是假的!OOC!不要上升本人!不要帶入!以及 我创作的纸片人..." },
  { text: "Au向,小众护肤品公司销售总监X新晋影帝久别重逢 论有一个影帝同学的好处就是..." }
])
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

.column-page {
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

// 主要内容区域
.main-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: $spacing-xl;
  max-width: 1200px;
  margin: 0 auto;
}

// 文章区域
.articles-section {
  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: $text-primary;
    margin: 0 0 $spacing-lg 0;
  }

  .articles-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;
  }

  .article-card {
    background: $white;
    border-radius: $border-radius;
    padding: $spacing-lg;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .article-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: $spacing-md;

      .article-title {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin: 0;
        flex: 1;
      }

      .article-source {
        font-size: 12px;
        color: $text-muted;
        margin-left: $spacing-md;
      }
    }

    .article-excerpt {
      color: $text-secondary;
      font-size: 14px;
      line-height: 1.6;
      margin: 0 0 $spacing-md 0;
    }

    .article-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .author-info {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        .author-avatar {
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

        .author-name {
          font-size: 13px;
          color: $text-primary;
          font-weight: 500;
        }

        .article-category {
          font-size: 12px;
          color: $text-muted;
          background: #f0f0f0;
          padding: 2px 6px;
          border-radius: 4px;
        }
      }

      .article-stats {
        display: flex;
        gap: $spacing-lg;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: $text-muted;

          .icon-placeholder {
            width: 12px;
            height: 12px;
            background: #d9dee3;
            border-radius: 2px;
          }
        }
      }
    }
  }
}

// 侧边栏
.sidebar {
  .sidebar-articles {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  .sidebar-item {
    display: flex;
    gap: $spacing-md;
    background: $white;
    border-radius: $border-radius;
    padding: $spacing-md;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .preview-thumbnail {
      width: 60px;
      height: 40px;
      border-radius: 4px;
      background: #d9dee3;
      flex-shrink: 0;
    }

    .preview-content {
      flex: 1;

      .preview-text {
        font-size: 12px;
        color: $text-secondary;
        line-height: 1.4;
        margin: 0;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
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

  .search-header .search-container {
    max-width: 100%;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-sm;
  }
}
</style>
