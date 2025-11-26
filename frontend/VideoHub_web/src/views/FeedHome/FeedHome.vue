<template>
  <div class="feed-page">
    <div class="feed-container">
      <aside class="left-column">
        <!-- inlined: UserCard.vue -->
        <div class="user-card">
          <div class="avatar"></div>
          <div class="info">
            <div class="name">小明</div>
            <div class="stats">
              <div><b>32</b><span>关注</span></div>
              <div><b>0</b><span>粉丝</span></div>
              <div><b>0</b><span>动态</span></div>
            </div>
          </div>
        </div>
        <nav class="left-menu">
          <ul>
            <li>全部动态</li>
            <li>视频投稿</li>
            <li>追番追剧</li>
            <li>专栏</li>
          </ul>
        </nav>
      </aside>

      <main class="center-column">
        <!-- inlined: Composer.vue -->
        <div class="composer">
          <input class="input" placeholder="好的标题更容易获得支持，选题20字" />
          <div class="actions">
            <button class="btn">发布</button>
          </div>
        </div>

        <!-- inlined: StoriesStrip.vue -->
        <div class="stories">
          <div v-for="n in 10" :key="n" class="story">
            <div class="bubble"></div>
            <div class="label">好友{{ n }}</div>
          </div>
        </div>

        <!-- inlined: FeedList.vue -->
        <div class="feed-list">
          <article v-for="n in 6" :key="n" class="feed-card">
            <header class="meta">
              <div class="avatar"></div>
              <div class="who">
                <div class="name">马长林夕-梦</div>
                <div class="sub">44分钟前 · 投稿了视频</div>
              </div>
            </header>
            <div class="content">
              <div class="thumb"></div>
              <div class="title">小丸子说:总是喷痰咳气，幸福会跑掉。</div>
            </div>
            <footer class="actions">
              <span>赞 147</span>
              <span>评 0</span>
              <span>转 15</span>
            </footer>
          </article>
        </div>
      </main>

      <aside class="right-column">
        <!-- inlined: RightSidebar.vue -->
        <div class="right">
          <section class="card">
            <h4>社区中心</h4>
            <ul>
              <li>公告与规则</li>
              <li>活动广场</li>
              <li>意见反馈</li>
            </ul>
          </section>
          <section class="card">
            <h4>bilibili热搜</h4>
            <ol>
              <li v-for="n in 10" :key="n">热搜条目 {{ n }}</li>
            </ol>
          </section>
        </div>
      </aside>
    </div>
  </div>
  
</template>

<script setup>
// child components inlined; no imports needed
</script>

<style lang="scss" scoped>
// SCSS Variables
$primary-color: #00aeec;
$background-color: #e7f3f5;
$white: #fff;
$text-primary: #333;
$text-secondary: #666;
$text-muted: #888;
$border-radius: 8px;
$border-radius-sm: 6px;
$spacing-xs: 6px;
$spacing-sm: 8px;
$spacing-md: 12px;
$spacing-lg: 16px;
$spacing-xl: 40px;
$container-width: 1200px;
$left-column-width: 260px;
$right-column-width: 300px;

// SCSS Mixins
@mixin card-style {
  background: $white;
  border-radius: $border-radius;
  padding: $spacing-md;
}

@mixin flex-center {
  display: flex;
  align-items: center;
}

@mixin flex-column {
  display: flex;
  flex-direction: column;
}

@mixin hover-effect {
  transition: all 0.2s ease;
  
  &:hover {
    opacity: 0.9;
  }
}

@mixin avatar-style($size) {
  width: $size;
  height: $size;
  border-radius: 50%;
  background: #ddd;
}

// Main Layout
.feed-page {
  background: $background-color;
  min-height: 100vh;
}

.feed-container {
  width: $container-width;
  margin: 0 auto;
  display: grid;
  grid-template-columns: $left-column-width 1fr $right-column-width;
  gap: $spacing-lg;
  padding: $spacing-lg 0 $spacing-xl;
}

// Left Column
.left-column {
  @include flex-column;
  gap: $spacing-md;
}

.left-menu {
  @include card-style;
  padding: $spacing-md 0;
  
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
  }
  
  li {
    padding: 10px $spacing-lg;
    cursor: pointer;
    transition: background-color 0.2s ease;
    
    &:hover {
      background: #f5f7fa;
    }
  }
}

// Center Column
.center-column {
  @include flex-column;
  gap: $spacing-md;
}

// Right Column
.right-column {
  position: sticky;
  top: $spacing-lg;
  height: fit-content;
}

// User Card Component
.user-card {
  @include card-style;
  
  .avatar {
    @include avatar-style(56px);
  }
  
  .info {
    margin-top: $spacing-sm;
    
    .name {
      font-weight: 600;
    }
    
    .stats {
      display: flex;
      gap: $spacing-lg;
      margin-top: $spacing-sm;
      
      div {
        @include flex-column;
        align-items: center;
      }
      
      span {
        color: $text-muted;
        font-size: 12px;
      }
    }
  }
}

// Composer Component
.composer {
  @include card-style;
  
  .input {
    width: 100%;
    border: none;
    outline: none;
    background: #f7f8fa;
    padding: 10px $spacing-md;
    border-radius: $border-radius-sm;
  }
  
  .actions {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-sm;
  }
  
  .btn {
    background: $primary-color;
    color: $white;
    border: none;
    padding: $spacing-xs 14px;
    border-radius: $border-radius-sm;
    cursor: pointer;
    @include hover-effect;
  }
}

// Stories Component
.stories {
  display: flex;
  gap: $spacing-md;
  @include card-style;
  overflow-x: auto;
  
  .story {
    @include flex-column;
    align-items: center;
    gap: $spacing-xs;
    
    .bubble {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #e3e9ee;
    }
    
    .label {
      font-size: 12px;
      color: $text-secondary;
    }
  }
}

// Feed List Component
.feed-list {
  @include flex-column;
  gap: $spacing-md;
  
  .feed-card {
    @include card-style;
    
    .meta {
      @include flex-center;
      gap: 10px;
      
      .avatar {
        @include avatar-style(36px);
      }
      
      .who {
        .name {
          font-weight: 600;
        }
        
        .sub {
          color: $text-muted;
          font-size: 12px;
        }
      }
    }
    
    .content {
      display: flex;
      gap: $spacing-md;
      margin-top: 10px;
      
      .thumb {
        width: 180px;
        height: 100px;
        background: #d9dee3;
        border-radius: $border-radius-sm;
      }
      
      .title {
        font-size: 14px;
        line-height: 1.6;
      }
    }
    
    .actions {
      display: flex;
      gap: $spacing-lg;
      color: $text-secondary;
      font-size: 13px;
      margin-top: $spacing-sm;
    }
  }
}

// Right Sidebar Component
.right {
  @include flex-column;
  gap: $spacing-md;
  
  .card {
    @include card-style;
    
    h4 {
      margin: 0 0 $spacing-sm;
    }
    
    ul, ol {
      margin: 0;
      padding-left: 18px;
      color: $text-secondary;
    }
  }
}
</style>


