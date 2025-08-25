<template>
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
</template>

<script>
export default {
  name: 'ProfileHeader',
  data () {
    return {
      nickname: '皇升级',
      avatar: '/public/favicon.ico',
      stats: { following: 32, followers: 0, likes: 0, views: 0 }
    }
  }
}
</script>

<style lang="scss" scoped>
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
</style>


