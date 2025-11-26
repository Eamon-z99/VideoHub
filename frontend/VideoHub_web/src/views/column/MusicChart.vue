<template>
  <div class="music-chart-page">
    <!-- 顶部横幅 -->
    <div class="banner-section">
      <div class="banner-content">
        <div class="artist-info">
          <h1 class="artist-name">ELLA</h1>
          <p class="artist-subtitle">陈嘉桦 巡回演唱会</p>
        </div>
        <div class="banner-image">
          <div class="placeholder-image"></div>
        </div>
      </div>
    </div>

    <!-- 新歌速递 -->
    <div class="section">
      <div class="section-header">
        <span class="section-icon">🎵</span>
        <h2 class="section-title">新歌速递</h2>
        <button class="section-more" @click="handleMore('new')">更多</button>
      </div>
      <div class="new-songs-content">
        <div class="featured-card">
          <div class="card-bg"></div>
          <h3>钢铁洪流进行曲</h3>
          <p>庆祝中华人民共和国成立周年</p>
        </div>
        <div class="songs-list">
          <div class="song-card" v-for="song in newSongs" :key="song.id">
            <div class="song-thumbnail">
              <div class="play-overlay" title="播放"></div>
            </div>
            <div class="song-info">
              <h4>{{ song.title }}</h4>
              <p>{{ song.artist }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 热歌精选 -->
    <div class="section">
      <div class="section-header">
        <span class="section-icon">🔥</span>
        <h2 class="section-title">热歌精选</h2>
        <button class="section-more" @click="handleMore('hot')">更多</button>
      </div>
      <div class="hot-songs-content">
        <div class="chart-list">
          <h3>bilibili音乐榜</h3>
          <div class="chart-item" v-for="(song, index) in hotSongs" :key="song.id">
            <span class="rank">{{ index + 1 }}</span>
            <span class="title">{{ song.title }}</span>
            <span class="plays">{{ song.plays }}</span>
          </div>
        </div>
        <div class="hot-songs-grid">
          <div class="song-card" v-for="song in hotSongsGrid" :key="song.id">
            <div class="song-thumbnail">
              <div class="play-overlay" title="播放"></div>
            </div>
            <div class="song-info">
              <h4>{{ song.title }}</h4>
              <p>{{ song.artist }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 音乐人 -->
    <div class="section">
      <div class="section-header">
        <span class="section-icon">👤</span>
        <h2 class="section-title">音乐人</h2>
        <button class="section-more" @click="handleMore('musicians')">更多</button>
      </div>
      <div class="musicians-list">
        <div class="musician-card" v-for="musician in musicians" :key="musician.id">
          <div class="musician-avatar"></div>
          <div class="musician-info">
            <h4>{{ musician.name }}</h4>
            <p>{{ musician.followers }}粉丝</p>
            <button class="follow-btn" :class="{ followed: musician.followed }" @click="toggleFollow(musician)">{{ musician.followed ? '已关注' : '关注' }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 原创音乐 -->
    <div class="section">
      <div class="section-header">
        <span class="section-icon">🎵</span>
        <h2 class="section-title">原创音乐</h2>
        <button class="section-more" @click="handleMore('original')">更多</button>
      </div>
      <div class="original-music-list">
        <div class="music-card" v-for="music in originalMusic" :key="music.id">
          <div class="music-thumbnail">
            <div class="play-overlay" title="播放"></div>
          </div>
          <div class="music-info">
            <h4>{{ music.title }}</h4>
            <p>{{ music.description }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 新歌数据
const newSongs = ref([
  { id: 1, title: '日食', artist: 'bilibili音乐人' },
  { id: 2, title: '安娜', artist: 'bilibili音乐人' },
  { id: 3, title: 'おまじない', artist: 'bilibili音乐人' },
  { id: 4, title: '来信', artist: 'bilibili音乐人' },
  { id: 5, title: '人生に期待をしてはいけない', artist: 'bilibili音乐人' }
])

// 热歌数据
const hotSongs = ref([
  { id: 1, title: 'Rich Man', plays: '217.9万' },
  { id: 2, title: '伯虎说', plays: '155.8万' },
  { id: 3, title: 'Once Upon a Time', plays: '126.3万' },
  { id: 4, title: 'モニタリング', plays: '124.2万' },
  { id: 5, title: '请你检网', plays: '113.8万' }
])

const hotSongsGrid = ref([
  { id: 1, title: 'Call of Silence', artist: 'bilibili音乐人' },
  { id: 2, title: '在你的身边', artist: 'bilibili音乐人' },
  { id: 3, title: 'Void', artist: 'bilibili音乐人' },
  { id: 4, title: '椿木', artist: 'bilibili音乐人' },
  { id: 5, title: 'Nevada', artist: 'bilibili音乐人' },
  { id: 6, title: 'unravel', artist: 'bilibili音乐人' }
])

// 音乐人数据
const musicians = ref([
  { id: 1, name: 'Marshmello', followers: '140万', followed: false },
  { id: 2, name: 'NMIXX', followers: '53.7万', followed: false },
  { id: 3, name: 'RIIZE', followers: '52.7万', followed: false }
])

// 原创音乐数据
const originalMusic = ref([
  { id: 1, title: '波山歌 (phonk)完整版来了', description: '这次我火力全开出来了!' },
  { id: 2, title: '《青衣》', description: '原创音乐作品' },
  { id: 3, title: '为什么我认为它不是好歌', description: '音乐评论' },
  { id: 4, title: '梦里变大侠写了一首歌', description: '原创音乐作品' }
])

// 交互：更多
const handleMore = (section) => {
  console.log('更多 ->', section)
}

// 交互：关注
const toggleFollow = (musician) => {
  musician.followed = !musician.followed
}
</script>

<style lang="scss" scoped>
.music-chart-page {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20px;
}

// 横幅区域
.banner-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
  
  .banner-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .artist-info {
      .artist-name {
        font-size: 48px;
        font-weight: bold;
        margin: 0 0 10px 0;
      }
      
      .artist-subtitle {
        font-size: 18px;
        margin: 0;
        opacity: 0.9;
      }
    }
    
    .banner-image {
      .placeholder-image {
        width: 200px;
        height: 150px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 8px;
      }
    }
  }
}

// 通用板块样式
.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    gap: 12px;
    
    .section-more {
      margin-left: auto;
      background: transparent;
      border: 1px solid #e5e7eb;
      color: #333;
      padding: 6px 12px;
      border-radius: 16px;
      cursor: pointer;
      font-size: 12px;
      transition: all .2s ease;
      
      &:hover {
        background: #f5f7fa;
      }
    }
    
    .section-icon {
      font-size: 24px;
      margin-right: 12px;
    }
    
    .section-title {
      font-size: 20px;
      font-weight: 600;
      margin: 0;
      color: #333;
    }
  }
}

// 新歌速递
.new-songs-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
  
  .featured-card {
    background: linear-gradient(45deg, #ff6b6b, #ee5a24);
    border-radius: 12px;
    padding: 24px;
    color: white;
    position: relative;
    overflow: hidden;
    
    .card-bg {
      position: absolute;
      top: -20px;
      right: -20px;
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
    }
    
    h3 {
      font-size: 18px;
      margin: 0 0 8px 0;
    }
    
    p {
      font-size: 14px;
      margin: 0;
      opacity: 0.9;
    }
  }
  
  .songs-list {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding-bottom: 6px;
    
    .song-card {
      min-width: 120px;
      text-align: center;
      
      &:hover .play-overlay {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
      }
      
      .song-thumbnail {
        width: 100%;
        height: 80px;
        background: #e0e0e0;
        border-radius: 8px;
        margin-bottom: 8px;
        position: relative;
        overflow: hidden;
        
        .play-overlay {
          position: absolute;
          left: 50%;
          top: 50%;
          transform: translate(-50%, -50%) scale(.9);
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: rgba(0, 0, 0, .6);
          box-shadow: 0 2px 6px rgba(0,0,0,.2);
          opacity: 0;
          transition: all .2s ease;
        }
      }
      
      .song-info {
        h4 {
          font-size: 12px;
          margin: 0 0 4px 0;
          color: #333;
        }
        
        p {
          font-size: 10px;
          margin: 0;
          color: #666;
        }
      }
    }
  }
}

// 热歌精选
.hot-songs-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
  
  .chart-list {
    h3 {
      font-size: 16px;
      margin: 0 0 16px 0;
      color: #333;
    }
    
    .chart-item {
      display: flex;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
      
      .rank {
        width: 24px;
        font-weight: bold;
        color: #ff6b6b;
      }
      
      .title {
        flex: 1;
        font-size: 14px;
        color: #333;
      }
      
      .plays {
        font-size: 12px;
        color: #666;
      }
    }
  }
  
  .hot-songs-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    
    .song-card {
      &:hover .play-overlay {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
      }
      .song-thumbnail {
        width: 100%;
        height: 80px;
        background: #e0e0e0;
        border-radius: 8px;
        margin-bottom: 8px;
        position: relative;
        overflow: hidden;
        
        .play-overlay {
          position: absolute;
          left: 50%;
          top: 50%;
          transform: translate(-50%, -50%) scale(.9);
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: rgba(0, 0, 0, .6);
          box-shadow: 0 2px 6px rgba(0,0,0,.2);
          opacity: 0;
          transition: all .2s ease;
        }
      }
      
      .song-info {
        h4 {
          font-size: 12px;
          margin: 0 0 4px 0;
          color: #333;
        }
        
        p {
          font-size: 10px;
          margin: 0;
          color: #666;
        }
      }
    }
  }
}

// 音乐人
.musicians-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  
  .musician-card {
    min-width: 150px;
    text-align: center;
    
    .musician-avatar {
      width: 60px;
      height: 60px;
      background: #e0e0e0;
      border-radius: 50%;
      margin: 0 auto 12px;
    }
    
    .musician-info {
      h4 {
        font-size: 14px;
        margin: 0 0 4px 0;
        color: #333;
      }
      
      p {
        font-size: 12px;
        margin: 0 0 8px 0;
        color: #666;
      }
      
      .follow-btn {
        background: #00aeec;
        color: white;
        border: none;
        padding: 6px 16px;
        border-radius: 16px;
        font-size: 12px;
        cursor: pointer;
        
        &:hover {
          background: #0099d4;
        }
      }
    }
  }
}

// 原创音乐
.original-music-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  
  .music-card {
    min-width: 200px;
    
    &:hover .play-overlay {
      opacity: 1;
      transform: translate(-50%, -50%) scale(1);
    }
    
    .music-thumbnail {
      width: 100%;
      height: 100px;
      background: #e0e0e0;
      border-radius: 8px;
      margin-bottom: 12px;
      position: relative;
      overflow: hidden;
      
      .play-overlay {
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%) scale(.9);
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: rgba(0, 0, 0, .6);
        box-shadow: 0 2px 6px rgba(0,0,0,.2);
        opacity: 0;
        transition: all .2s ease;
      }
    }
    
    .music-info {
      h4 {
        font-size: 14px;
        margin: 0 0 4px 0;
        color: #333;
      }
      
      p {
        font-size: 12px;
        margin: 0;
        color: #666;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .new-songs-content,
  .hot-songs-content {
    grid-template-columns: 1fr;
  }
  
  .hot-songs-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .banner-section .banner-content {
    flex-direction: column;
    text-align: center;
    
    .artist-name {
      font-size: 32px;
    }
  }
}
</style>
