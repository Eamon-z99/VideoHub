<template>
  <div class="user-profile-page">
    <!-- 顶部栏：不随滚动悬浮出现 + 顶部透明 -->
    <TopHeader :fixed-on-scroll="false" :transparent-at-top="true" />

    <!-- 顶部封面 + 个人信息 -->
    <header class="profile-header">
      <div class="profile-header-inner">
        <img class="cover" :src="coverImage" alt="cover" @error="onImageError" @load="onImageLoad" />
        <div class="header-inner user-profile-container">
          <div class="user-row">
            <img class="avatar" :src="avatar" alt="avatar" />
            <div class="user-meta">
              <div class="name-row">
                <div class="name">{{ nickname }}</div>
                <span class="badge level">LV5</span>
                <span class="badge vip">大会员</span>
              </div>
              <div class="sub-row">编辑个性签名</div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体：左侧收藏夹列表 + 右侧内容 -->
    <main class="content-wrap">
      <!-- Tab -->
      <nav class="profile-tabs user-profile-container">
        <div class="tabs-left">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="tab"
            :class="{ active: t.key === activeTab }"
            @click="onTabChange(t.key)"
          >
            <span class="tab-icon" :class="t.key" />
            <span class="tab-text">{{ t.label }}</span>
            <span v-if="t.count != null" class="tab-count">{{ t.count }}</span>
          </button>
        </div>
        <div class="user-stats">
          <div class="stat">
            <div class="num">{{ stats.following }}</div>
            <div class="label">关注数</div>
          </div>
          <div class="stat">
            <div class="num">{{ stats.followers }}</div>
            <div class="label">粉丝数</div>
          </div>
          <div class="stat">
            <div class="num">{{ stats.likes }}</div>
            <div class="label">获赞数</div>
          </div>
          <div class="stat">
            <div class="num">{{ stats.views }}</div>
            <div class="label">播放数</div>
          </div>
        </div>
      </nav>
      <div class="content-inner user-profile-container">
        <aside class="left-panel" v-if="activeTab === 'collections'">
          <div class="panel-section">
            <div class="panel-title">我创建的收藏夹</div>
            <button class="new-folder">
              <span class="plus">＋</span>
              新建收藏夹
            </button>
            <ul class="folder-list">
              <li
                v-for="f in folders"
                :key="f.id"
                class="folder"
                :class="{ active: f.id === activeFolderId }"
                @click="activeFolderId = f.id"
              >
                <span class="folder-icon" />
                <span class="folder-name">{{ f.name }}</span>
                <span class="folder-count">{{ f.count }}</span>
              </li>
            </ul>
          </div>
          <div class="panel-section">
            <div class="panel-title">我追的合集/收藏夹</div>
            <ul class="folder-list">
              <li
                v-for="f in followedFolders"
                :key="f.id"
                class="folder"
              >
                <span class="folder-icon" />
                <span class="folder-name">{{ f.name }}</span>
                <span class="folder-count">{{ f.count }}</span>
              </li>
            </ul>
          </div>
        </aside>

        <section class="right-panel">
          <div v-if="activeTab === 'collections'" class="fav-header">
            <div class="fav-left">
              <div class="fav-title">{{ activeFolder?.name || '默认收藏夹' }}</div>
              <div class="fav-sub">公开 · 视频数：{{ activeFolder?.count ?? 0 }}</div>
            </div>
            <button class="play-all">播放全部</button>
            <div class="fav-tools">
              <button class="tool-btn">批量操作</button>
            </div>
          </div>

          <div v-if="activeTab === 'collections'" class="toolbar">
            <div class="chips">
              <button class="chip active">最近收藏</button>
              <button class="chip">最多播放</button>
              <button class="chip">最近投稿</button>
            </div>
            <div class="searchbar">
              <input class="input" placeholder="请输入关键词" />
              <button class="search-btn" aria-label="search">🔍</button>
            </div>
          </div>

          <div v-if="activeTab === 'collections'" class="video-grid">
            <article v-for="v in videos" :key="v.id" class="video-card">
              <div class="thumb">
                <img v-if="v.cover" :src="v.cover" alt="" />
                <div v-else class="thumb-ph" />
                <span class="duration">{{ v.duration }}</span>
              </div>
              <div class="v-title" :title="v.title">{{ v.title }}</div>
              <div class="v-meta">
                <span>▶ {{ v.play }}</span>
                <span>💬 {{ v.danmaku }}</span>
                <span class="time">{{ v.time }}</span>
              </div>
            </article>
          </div>

          <div v-else-if="activeTab === 'home'" class="empty">主页内容</div>
          <div v-else-if="activeTab === 'dynamics'" class="empty">动态内容</div>
          <div v-else-if="activeTab === 'submit'" class="empty">投稿内容</div>
          <div v-else class="empty">暂无内容</div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import TopHeader from '@/components/TopHeader.vue'

export default {
  name: 'UserProfile',
  components: { TopHeader },
  data () {
    return {
      coverImage: '/assets/topheader/favorite.png',
      activeTab: 'home',
      tabs: [
        { key: 'home', label: '主页' },
        { key: 'dynamics', label: '动态' },
        { key: 'submit', label: '投稿' },
        { key: 'collections', label: '收藏', count: 16 }
      ],
      nickname: '皇升级',
      avatar: '/public/favicon.ico',
      stats: { following: 24, followers: 3, likes: 0, views: 0 },
      activeFolderId: 'default',
      folders: [
        { id: 'default', name: '默认收藏夹', count: 101 },
        { id: 'game', name: '游戏', count: 1 },
        { id: 'web', name: '网文', count: 1 },
        { id: 'fe', name: '前端', count: 1 },
        { id: 'grad', name: '考研', count: 2 },
        { id: 'uniapp', name: 'uniapp', count: 6 },
        { id: 'startup', name: '开图', count: 4 },
        { id: 'target', name: '目标检测', count: 2 }
      ],
      followedFolders: [
        { id: 'fisco', name: 'FISCO BCOS 学习', count: 8 }
      ],
      videos: Array.from({ length: 10 }).map((_, i) => ({
        id: `v-${i + 1}`,
        cover: '',
        title: `示例视频标题 ${i + 1}`,
        duration: '02:29:02',
        play: i % 2 === 0 ? '16.8万' : '9176',
        danmaku: i % 3 === 0 ? '30' : '2',
        time: '昨天'
      }))
    }
  },
  computed: {
    activeFolder () {
      return this.folders.find(f => f.id === this.activeFolderId)
    }
  },
  methods: {
    onTabChange (key) {
      this.activeTab = key
    },
    onImageError (e) {
      console.error('图片加载失败:', e.target.src)
      console.error('尝试的路径:', this.coverImage)
    },
    onImageLoad () {
      console.log('图片加载成功:', this.coverImage)
    }
  }
}
</script>

<style lang="scss" scoped>
.user-profile-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  margin: 0 auto;
  background: #FFFFFF;
  min-height: 100vh;
  // 本页 TopHeader 需要覆盖在封面图上，因此不预留顶部空间
  padding-top: 0;
}

// 统一内容区域宽度，避免 header / main 各自写死不同的 width 导致不一致
.user-profile-container {
  width: 100%;
  max-width: 75%;
  min-width: 1200px;
  margin: 0 auto;
  padding: 0 8px;
}

.profile-header {
  position: relative;
  height: 230px;
  overflow: hidden;

  .profile-header-inner {
    position: relative;
    height: 90%;
  }

  // 顶部大图
  .cover {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    filter: brightness(0.8);
  }

  // 用户信息这一横栏（含头像）和下方 Tab，整体锚定在图片容器底部
  .header-inner {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
  }

  .user-row {
    position: relative;
    margin-top: 0;
    display: grid;
    grid-template-columns: 75px 1fr;
    gap: 16px;
    align-items: end;
    padding-bottom: 14px;
  }

  .avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    border: 3px solid #fff;
    object-fit: cover;
    background: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
  }

  .user-meta {
    padding-bottom: 6px;

    .name-row {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .name {
      font-size: 22px;
      font-weight: 700;
      color: #fff;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
    }

    .sub-row {
      margin-top: 6px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.85);
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
    }

    .badge {
      font-size: 12px;
      line-height: 18px;
      padding: 0 8px;
      border-radius: 10px;
      background: rgba(0, 0, 0, 0.25);
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(2px);
    }

    .badge.vip {
      background: rgba(255, 255, 255, 0.18);
    }
  }
}

.profile-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
  background: #fff;
  border-radius: 8px 8px 0 0;

  .tabs-left {
    display: flex;
    gap: 18px;
  }

  .user-stats {
    display: grid;
    grid-auto-flow: column;
    gap: 22px;
    color: #222;
    font-size: 14px;

    .stat {
      text-align: center;
      min-width: 60px;
    }

    .num {
      font-weight: 700;
      font-size: 16px;
      color: #222;
    }

    .label {
      margin-top: 2px;
      font-size: 12px;
      color: #999;
    }
  }

  .tab {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    border: 0;
    background: transparent;
    cursor: pointer;
    color: #61666d;
    font-size: 14px;
    position: relative;

    .tab-icon {
      width: 16px;
      height: 16px;
      border-radius: 4px;
      background: #e8f7ff;
    }

    .tab-count {
      font-size: 12px;
      color: #00a1d6;
      font-weight: 600;
    }

    &.active {
      color: #00a1d6;
      font-weight: 600;
    }

    &.active::after {
      content: '';
      position: absolute;
      left: 8px;
      right: 8px;
      bottom: -10px;
      height: 2px;
      background: #00a1d6;
      border-radius: 2px;
    }
  }
}

.content-wrap {
  padding: 16px 0 36px;
}

.content-inner {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
  padding: 0; // padding 由 .user-profile-container 统一控制
}

.left-panel {
  background: #fff;
  border-radius: 8px;
  padding: 14px 12px;
  height: fit-content;
}

.panel-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.panel-title {
  font-size: 14px;
  color: #222;
  font-weight: 600;
  margin-bottom: 10px;
}

.new-folder {
  width: 100%;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #222;
  margin-bottom: 10px;

  .plus {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    background: #e8f7ff;
    color: #00a1d6;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
  }
}

.folder-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.folder {
  display: grid;
  grid-template-columns: 18px 1fr auto;
  gap: 8px;
  align-items: center;
  padding: 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  color: #222;

  &:hover {
    background: #f6f7f8;
  }

  &.active {
    background: #e8f7ff;
    color: #00a1d6;
  }

  .folder-icon {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    background: #f1f2f3;
  }

  .folder-count {
    font-size: 12px;
    color: #999;
  }

  &.active .folder-count {
    color: #00a1d6;
  }
}

.right-panel {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px 18px;
  min-height: 520px;
}

.fav-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.fav-left {
  flex: 1;
}

.fav-title {
  font-size: 18px;
  font-weight: 700;
  color: #222;
}

.fav-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.play-all {
  border: 0;
  background: #00a1d6;
  color: #fff;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
}

.tool-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 9px 12px;
  cursor: pointer;
}

.toolbar {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chips {
  display: flex;
  gap: 10px;
}

.chip {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  color: #61666d;
  font-size: 13px;

  &.active {
    background: #00a1d6;
    border-color: #00a1d6;
    color: #fff;
  }
}

.searchbar {
  display: grid;
  grid-template-columns: 1fr 38px;
  gap: 10px;
  align-items: center;
}

.input {
  height: 34px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 10px;
  outline: none;
  width: 100%;
}

.search-btn {
  height: 34px;
  width: 38px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
}

.video-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.video-card {
  cursor: pointer;

  .thumb {
    position: relative;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
    background: #f1f2f3;
    padding-bottom: 56%;

    img,
    .thumb-ph {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .duration {
      position: absolute;
      right: 6px;
      bottom: 6px;
      font-size: 12px;
      color: #fff;
      background: rgba(0, 0, 0, 0.55);
      padding: 2px 6px;
      border-radius: 4px;
    }
  }

  .v-title {
    margin-top: 8px;
    font-size: 13px;
    color: #222;
    line-height: 1.4;
    height: 36px;
    overflow: hidden;
    display: -webkit-box;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .v-meta {
    margin-top: 6px;
    font-size: 12px;
    color: #999;
    display: flex;
    gap: 8px;

    .time {
      margin-left: auto;
    }
  }
}

.empty {
  text-align: center;
  color: #999;
  padding: 60px 0;
}

@media (max-width: 960px) {
  .profile-header .header-inner,
  .content-inner {
    width: auto;
    padding: 0 12px;
  }

  .content-inner {
    grid-template-columns: 1fr;
  }

  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
