<template>
  <div class="user-profile">
    <!-- inlined: ProfileHeader.vue -->
    <header class="profile-header">
      <div class="cover"></div>
      <div class="header-content">
        <img class="avatar" :src="avatar" alt="avatar" />
        <div class="meta">
          <div class="row">
            <h1 class="name">{{ nickname }}</h1>
            <span class="level">LV5</span>
            <button class="btn follow">关注</button>
            <button class="btn message">发消息</button>
          </div>
          <div class="stats">
            <span>关注 {{ stats.following }}</span>
            <span>粉丝 {{ stats.followers }}</span>
            <span>获赞 {{ stats.likes }}</span>
            <span>播放 {{ stats.views }}</span>
          </div>
        </div>
      </div>
    </header>
    <div class="profile-body">
      <div class="profile-main">
        <!-- inlined: ProfileTabs.vue -->
        <nav class="profile-tabs">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="tab"
            :class="{active: t.key===activeTab}"
            @click="onTabChange(t.key)"
          >
            {{ t.label }}
          </button>
        </nav>

        <!-- inlined: tabs -->
        <div v-if="activeTab==='collections'" class="tab-collections">
          <section class="section">
            <div class="section-head">
              <h2>收藏夹 · 13</h2>
              <button class="more">查看更多</button>
            </div>
            <div class="grid">
              <div class="item" v-for="i in 8" :key="i">
                <div class="thumb"></div>
                <div class="title">默认收藏夹</div>
                <div class="meta">公开 · 24个视频</div>
              </div>
            </div>
          </section>
        </div>
        <div v-else-if="activeTab==='dynamics'" class="tab-dynamics">
          <div class="empty">暂无动态</div>
        </div>
        <div v-else-if="activeTab==='albums'" class="tab-albums">
          <div class="empty">暂无合集</div>
        </div>
      </div>
      <aside class="profile-aside">
        <!-- inlined: ProfileSidebar.vue -->
        <div class="profile-sidebar">
          <section class="card">
            <h3>公告</h3>
            <p>编辑我的公告</p>
          </section>
          <section class="card">
            <h3>个人资料</h3>
            <ul>
              <li>UID 473967601</li>
              <li>生日 08-20</li>
            </ul>
          </section>
        </div>
      </aside>
    </div>
  </div>
  
</template>

<script>
export default {
  name: 'UserProfile',
  data () {
    return {
      activeTab: 'collections',
      tabs: [
        { key: 'collections', label: '收藏夹' },
        { key: 'dynamics', label: '动态' },
        { key: 'albums', label: '合集' }
      ],
      nickname: '皇升级',
      avatar: '/public/favicon.ico',
      stats: { following: 32, followers: 0, likes: 0, views: 0 }
    }
  },
  methods: {
    onTabChange (key) {
      this.activeTab = key
    }
  }
}
</script>

<style lang="scss" scoped>
.user-profile {
  display: flex;
  flex-direction: column;
  .profile-body {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 16px;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 16px 0 32px;

    .profile-main {
      background: #fff;
      border-radius: 8px;
      padding: 16px;
    }

    .profile-aside {
      position: sticky;
      top: 72px;
      height: fit-content;
    }
  }
}

/* inlined styles from ProfileHeader.vue */
.profile-header {
  .cover {
    height: 180px;
    background: linear-gradient(90deg, #f3f5ff, #ffeef5);
    border-radius: 8px;
  }
  .header-content {
    position: relative;
    max-width: 1200px;
    margin: -40px auto 0;
    display: flex;
    align-items: center;
    padding: 0 8px 8px;
    .avatar {
      width: 88px;
      height: 88px;
      border-radius: 50%;
      border: 3px solid #fff;
      object-fit: cover;
      background: #fff;
    }
    .meta {
      flex: 1;
      margin-left: 16px;
      .row {
        display: flex;
        align-items: center;
        gap: 8px;
        .name { font-size: 20px; font-weight: 600; }
        .level { color: #ff7a45; font-weight: 600; }
        .btn { padding: 6px 12px; border-radius: 16px; border: 1px solid #e5e5e5; background: #fff; cursor: pointer; }
        .follow { background: #ff6699; color: #fff; border: none; }
      }
      .stats { margin-top: 6px; color: #666; display: flex; gap: 12px; font-size: 13px; }
    }
  }
}

/* inlined styles from ProfileTabs.vue */
.profile-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  .tab {
    padding: 8px 14px;
    border-radius: 18px;
    border: 1px solid #eee;
    background: #fff;
    cursor: pointer;
    &.active { background: #222; color: #fff; border-color: #222; }
  }
}

/* inlined styles from ProfileSidebar.vue */
.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  .card {
    background: #fff;
    border-radius: 8px;
    padding: 12px;
    h3 { margin: 0 0 8px; font-size: 15px; }
    ul { margin: 0; padding-left: 16px; color: #666; }
  }
}

/* inlined styles from tabs */
.tab-collections {
  .section {
    .section-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      h2 { font-size: 16px; margin: 0; }
      .more { background: none; border: none; color: #409eff; cursor: pointer; }
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      @media (max-width: 960px) {
        grid-template-columns: repeat(2, 1fr);
      }
      .item {
        background: #fafafa;
        border-radius: 8px;
        padding: 8px;
        .thumb { height: 120px; background: #eaeaea; border-radius: 6px; }
        .title { margin-top: 6px; font-weight: 600; }
        .meta { color: #888; font-size: 12px; }
      }
    }
  }
}
.tab-dynamics { .empty { text-align: center; color: #999; padding: 40px 0; } }
.tab-albums { .empty { text-align: center; color: #999; padding: 40px 0; } }

@media (max-width: 960px) {
  .user-profile {
    .profile-body {
      grid-template-columns: 1fr;
    }
  }
}
</style>


