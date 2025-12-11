<template>
  <div class="video-page">
    <!-- 主视频区域 -->
    <section class="player-area">
      <div class="player">
        <video
          class="video"
          :src="videoSrc"
          controls
          :poster="posterUrl"
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
            <el-button size="small" round plain :icon="Star">收藏</el-button>
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { View, ChatDotRound, Timer } from '@element-plus/icons-vue'
import { Pointer, Star, Share } from '@element-plus/icons-vue'
import { fetchVideoDetail } from '@/api/video'

const route = useRoute()

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
  } catch (e) {
    title.value = '未找到视频'
    videoSrc.value = ''
  } finally {
    loading.value = false
  }
}

onMounted(loadVideo)
watch(() => route.params.id, () => loadVideo())

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

      .u-avatar { width: 48px; height: 48px; border-radius: 50%; }
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