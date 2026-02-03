<template>
  <div class="video-page">
    <!-- 主视频区域 -->
    <section class="player-area">
      <div class="player">
        <video
          ref="videoPlayer"
          class="video"
          :src="videoSrc"
          controls
          autoplay
          :poster="posterUrl"
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onVideoLoaded"
        />
      </div>

      <!-- 标题与互动条 -->
      <div class="video-meta">
        <h1 class="title">{{ title }}</h1>
        <div class="toolbar">
          <div class="stat">
            <el-icon><View /></el-icon>
            <span>{{ playCount }}</span>
          </div>
          <div class="stat">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ danmakuCount }}</span>
          </div>
          <div class="actions">
            <el-button size="small" round plain :icon="Pointer">点赞</el-button>
            <el-button 
              size="small" 
              round 
              :type="isFavorited ? 'primary' : 'default'"
              :icon="Star"
              @click="toggleFavorite"
              :loading="favoriteLoading"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button size="small" round plain :icon="Share">分享</el-button>
          </div>
        </div>
      </div>

      <!-- 简介与标签 -->
      <div class="desc">
        <div class="tags">
          <el-tag v-for="t in tags" :key="t" size="small" effect="light">{{ t }}</el-tag>
        </div>
        <p class="intro">{{ description }}</p>
      </div>

      <!-- 评论区 -->
      <div class="comments">
        <div class="comment-editor">
          <el-input
            v-model="commentText"
            :rows="3"
            type="textarea"
            placeholder="发条友善的评论吧"
          />
          <div class="editor-actions">
            <el-button type="primary" size="small" @click="submitComment">发表评论</el-button>
          </div>
        </div>

        <div class="comment-list">
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <img :src="item.avatar" class="avatar" />
            <div class="content">
              <div class="header">
                <span class="name">{{ item.name }}</span>
                <span class="time">{{ item.time }}</span>
              </div>
              <p class="text">{{ item.text }}</p>
              <div class="comment-actions">
                <el-button text size="small">点赞 {{ item.likes }}</el-button>
                <el-button text size="small">回复</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 右侧推荐区 -->
    <aside class="sidebar">
      <div class="uploader-card">
        <img class="u-avatar" :src="uploader.avatar" />
        <div class="u-info">
          <div class="u-name">{{ uploader.name }}</div>
          <div class="u-stats">视频 {{ uploader.videoCount }} · 粉丝 {{ uploader.fans }}</div>
        </div>
        <el-button type="primary" size="small" round>+ 关注</el-button>
      </div>

      <div class="recommend-title">相关推荐</div>
      <div class="recommend-list">
        <div class="rec-item" v-for="rec in recommends" :key="rec.id">
          <img :src="rec.cover" class="rec-cover" />
          <div class="rec-info">
            <div class="rec-title">{{ rec.title }}</div>
            <div class="rec-meta">
              <span><el-icon><View /></el-icon> {{ rec.plays }}</span>
              <span><el-icon><Timer /></el-icon> {{ rec.duration }}</span>
            </div>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { View, ChatDotRound, Timer } from '@element-plus/icons-vue'
import { Pointer, Star, Share } from '@element-plus/icons-vue'
import { fetchVideoDetail } from '@/api/video'
import { recordHistory } from '@/api/history'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const videoPlayer = ref(null)
const videoData = ref({
  id: '',
  title: '',
  playUrl: '',
  cover: '',
  duration: '',
  sizeText: '',
  video: true
})

const title = ref('本地视频')
const playCount = ref('本地文件')
const danmakuCount = ref('0')
const tags = ref(['本地视频', '离线播放'])
const description = ref('播放来自 E:\\Videos 目录的本地视频。')
const videoSrc = ref('')
const posterUrl = ref('')
const loading = ref(false)
const fallbackCover = '/images/banner-1.jpg'

// 收藏相关
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 播放历史记录相关
let recordTimer = null
let lastRecordTimestamp = 0 // 上次记录的时间戳（毫秒）
let lastRecordPlayTime = 0 // 上次记录的播放时间（秒）
const RECORD_INTERVAL = 10000 // 每10秒记录一次
const MIN_PLAY_TIME_DIFF = 5 // 播放时间变化超过5秒才记录

const loadVideo = async () => {
  const videoId = route.params.id
  if (typeof videoId !== 'string') return
  loading.value = true
  try {
    const { data } = await fetchVideoDetail(videoId)
    videoData.value = data
    title.value = data.title || data.videoId || '本地视频'
    playCount.value = data.viewCount || '本地文件'
    videoSrc.value = data.playUrl || ''
    posterUrl.value = data.coverUrl || fallbackCover
    description.value = data.description || `视频ID：${data.videoId || videoId}`
    tags.value = [
      data.sourceFile || '本地视频',
      data.storagePath || '',
    ].filter(Boolean)
    
    // 检查收藏状态
    if (userStore.isAuthenticated) {
      await checkFavoriteStatus(videoId)
    }
  } catch (e) {
    title.value = '未找到视频'
    videoSrc.value = ''
  } finally {
    loading.value = false
  }
}

// 检查收藏状态
const checkFavoriteStatus = async (videoId) => {
  if (!userStore.isAuthenticated) return
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) return
  
  try {
    const { data } = await checkFavorite(userId, videoId)
    if (data.success) {
      isFavorited.value = data.isFavorited || false
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId) {
    ElMessage.warning('用户信息获取失败')
    return
  }
  
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return
  
  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      // 取消收藏
      const { data } = await removeFavorite(userId, videoId)
      if (data.success) {
        isFavorited.value = false
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error(data.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const { data } = await addFavorite(userId, videoId)
      if (data.success) {
        isFavorited.value = true
        ElMessage.success('收藏成功')
      } else {
        ElMessage.warning(data.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    favoriteLoading.value = false
  }
}

onMounted(() => {
  loadVideo()
})

watch(() => route.params.id, () => {
  loadVideo()
})

watch(() => userStore.isAuthenticated, (isAuth) => {
  if (isAuth && videoData.value.videoId) {
    checkFavoriteStatus(videoData.value.videoId)
  } else {
    isFavorited.value = false
  }
})

const commentText = ref('')
const comments = ref([
  { id: 1, name: '玩家甲', time: '1小时前', text: '分析很细，受教了！', likes: 123, avatar: 'https://placehold.co/40x40' },
  { id: 2, name: '玩家乙', time: '2小时前', text: '实战里感觉有点吃队友配置。', likes: 56, avatar: 'https://placehold.co/40x40' },
])

const uploader = ref({
  name: '用户上传',
  videoCount: 1,
  fans: '1.2万',
  avatar: 'https://placehold.co/48x48'
})

const recommends = ref([
  { id: 1, title: '梦奇新皮肤技能演示', plays: '130.0万', duration: '04:12', cover: 'https://placehold.co/160x90' },
  { id: 2, title: '打不过你不会出肉吗？', plays: '76.5万', duration: '03:37', cover: 'https://placehold.co/160x90' },
  { id: 3, title: '王者里有梦奇这个英雄吗？', plays: '55.2万', duration: '05:07', cover: 'https://placehold.co/160x90' },
  { id: 4, title: '什么？梦奇要重做了？', plays: '28.3万', duration: '08:41', cover: 'https://placehold.co/160x90' },
])

const submitComment = () => {
  if (!commentText.value.trim()) return
  comments.value.unshift({
    id: Date.now(),
    name: '我',
    time: '刚刚',
    text: commentText.value,
    likes: 0,
    avatar: 'https://placehold.co/40x40'
  })
  commentText.value = ''
}

// 视频时间更新事件
const onTimeUpdate = () => {
  if (!videoPlayer.value || !userStore.isAuthenticated) return
  
  const currentTime = Math.floor(videoPlayer.value.currentTime)
  const now = Date.now()
  
  // 每10秒记录一次，或者播放时间变化超过5秒
  if (now - lastRecordTimestamp >= RECORD_INTERVAL || 
      Math.abs(currentTime - lastRecordPlayTime) >= MIN_PLAY_TIME_DIFF) {
    recordPlayHistory(currentTime)
    lastRecordTimestamp = now
    lastRecordPlayTime = currentTime
  }
}

// 视频加载完成事件
const onVideoLoaded = () => {
  if (!videoPlayer.value) return
  
  // 尝试自动播放（如果浏览器策略允许）
  videoPlayer.value.play().catch(err => {
    // 如果自动播放被阻止，静默失败（用户仍可手动播放）
    // 现代浏览器通常不允许自动播放带声音的视频，除非用户已与页面交互
    console.log('自动播放被阻止（可能需要用户交互）:', err.message)
  })
  
  // 视频加载完成后立即记录一次（如果已登录）
  if (userStore.isAuthenticated) {
    const currentTime = Math.floor(videoPlayer.value.currentTime || 0)
    recordPlayHistory(currentTime)
    lastRecordTimestamp = Date.now()
    lastRecordPlayTime = currentTime
  }
}

// 记录播放历史
const recordPlayHistory = async (playTime) => {
  if (!userStore.isAuthenticated) return
  
  const userId = userStore.user?.userId || userStore.user?.id
  if (!userId || !videoData.value.videoId) return

  // 优先从video元素获取实际时长，其次从videoData获取
  let duration = null
  if (videoPlayer.value && videoPlayer.value.duration) {
    duration = Math.floor(videoPlayer.value.duration)
  } else if (videoData.value.duration) {
    duration = typeof videoData.value.duration === 'number' 
      ? videoData.value.duration 
      : parseInt(videoData.value.duration)
  }

  try {
    await recordHistory(userId, videoData.value.videoId, playTime, duration)
  } catch (error) {
    console.error('记录播放历史失败:', error)
  }
}

// 清理定时器
onUnmounted(() => {
  if (recordTimer) {
    clearInterval(recordTimer)
    recordTimer = null
  }
})
</script>

<style lang="scss" scoped>
.video-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;

  .player-area {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .player {
      background: #000;
      border-radius: 8px;
      overflow: hidden;

      .video {
        width: 100%;
        height: 520px;
        display: block;
        background: #000;
      }
    }

    .video-meta {
      .title {
        margin: 0;
        font-size: 20px;
        color: #18191c;
        font-weight: 600;
      }
      .toolbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 8px;

        .stat {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          color: #6c6f73;
          margin-right: 12px;
        }

        .actions {
          display: inline-flex;
          gap: 8px;
        }
      }
    }

    .desc {
      background: #fff;
      border-radius: 8px;
      padding: 12px;
      .tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        margin-bottom: 8px;
      }
      .intro {
        margin: 0;
        color: #333;
        line-height: 1.6;
        font-size: 14px;
      }
    }

    .comments {
      background: #fff;
      border-radius: 8px;
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .comment-editor {
        .editor-actions {
          display: flex;
          justify-content: flex-end;
          margin-top: 8px;
        }
      }

      .comment-list {
        .comment-item {
          display: flex;
          gap: 8px;
          padding: 10px 0;
          border-bottom: 1px solid #f0f0f0;

          .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            flex: 0 0 40px;
            position: relative;
            z-index: 99999;
          }
          .content {
            flex: 1 1 auto;
            .header {
              display: flex;
              align-items: center;
              gap: 8px;
              .name { font-weight: 600; }
              .time { color: #8a8a8a; font-size: 12px; }
            }
            .text {
              margin: 6px 0;
              color: #222;
            }
            .comment-actions {
              display: inline-flex;
              gap: 8px;
            }
          }
        }
      }
    }
  }

  .sidebar {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .uploader-card {
      background: #fff;
      border-radius: 8px;
      padding: 12px;
      display: grid;
      grid-template-columns: 48px 1fr auto;
      gap: 10px;
      align-items: center;

      .u-avatar { 
        width: 48px; 
        height: 48px; 
        border-radius: 50%; 
        position: relative;
        z-index: 99999;
      }
      .u-info {
        .u-name { font-weight: 600; }
        .u-stats { color: #8a8a8a; font-size: 12px; margin-top: 2px; }
      }
    }

    .recommend-title { font-weight: 600; color: #18191c; }

    .recommend-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
      .rec-item {
        display: grid;
        grid-template-columns: 160px 1fr;
        gap: 10px;
        background: #fff;
        border-radius: 8px;
        padding: 8px;
        .rec-cover { width: 160px; height: 90px; border-radius: 6px; object-fit: cover; }
        .rec-info {
          .rec-title { font-size: 14px; color: #18191c; line-height: 1.4; }
          .rec-meta { color: #8a8a8a; display: flex; gap: 12px; margin-top: 6px; font-size: 12px; }
        }
      }
    }
  }
}

@media (max-width: 1100px) {
  .video-page {
    grid-template-columns: 1fr;
    .sidebar { order: 2; }
    .player-area { order: 1; }
  }
}
</style>