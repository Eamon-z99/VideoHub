<template>
  <div class="recommendation-list">
    <div class="recommendation-header">
      <h2>为你推荐</h2>
      <div class="defense-badge" v-if="hasDefense">
        <span class="badge-icon">🛡️</span>
        <span>已启用防御保护</span>
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="videos.length === 0" class="empty">
      暂无推荐内容
    </div>
    
    <div v-else class="video-grid">
      <div 
        v-for="video in filteredVideos" 
        :key="video.videoId"
        class="video-card"
        :class="{ 'suspicious': isSuspiciousVideo(video) }"
      >
        <div class="video-thumbnail">
          <img :src="video.coverUrl" :alt="video.title" />
          <div v-if="isSuspiciousVideo(video)" class="suspicious-badge">
            可疑内容
          </div>
        </div>
        <div class="video-info">
          <h3 class="video-title">{{ video.title }}</h3>
          <div class="video-stats">
            <span>👁️ {{ formatCount(video.viewCount) }}</span>
            <span>👍 {{ formatCount(video.likeCount) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getRecommendations, detectUser } from '@/api/recommendation'
import { useUserStore } from '@/stores/user'

export default {
  name: 'RecommendationList',
  props: {
    algorithm: {
      type: String,
      default: 'user-based' // 'user-based' or 'item-based'
    },
    enableDefense: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      videos: [],
      loading: false,
      suspiciousUserIds: new Set(),
      hasDefense: false
    }
  },
  computed: {
    filteredVideos() {
      if (!this.enableDefense) {
        return this.videos
      }
      // 过滤掉可疑用户推荐的内容
      return this.videos.filter(video => !this.isSuspiciousVideo(video))
    }
  },
  mounted() {
    this.loadRecommendations()
    if (this.enableDefense) {
      this.loadSuspiciousUsers()
      this.hasDefense = true
    }
  },
  methods: {
    async loadRecommendations() {
      this.loading = true
      try {
        const userStore = useUserStore()
        const userId = userStore.userId || 1 // 默认用户ID
        
        let response
        if (this.algorithm === 'item-based') {
          response = await getRecommendations(userId, 20) // 使用item-based API
        } else {
          response = await getRecommendations(userId, 20)
        }
        
        if (response.data && response.data.list) {
          this.videos = response.data.list
        }
      } catch (error) {
        console.error('加载推荐失败:', error)
        this.videos = []
      } finally {
        this.loading = false
      }
    },
    async loadSuspiciousUsers() {
      try {
        // 这里可以调用防御API获取可疑用户列表
        // 简化实现：检查每个推荐视频的来源用户
        const userStore = useUserStore()
        if (userStore.userId) {
          const response = await detectUser(userStore.userId)
          // 根据实际情况处理响应
        }
      } catch (error) {
        console.error('加载可疑用户失败:', error)
      }
    },
    isSuspiciousVideo(video) {
      // 简化的可疑视频检测逻辑
      // 实际应该基于后端返回的标记
      return false
    },
    formatCount(count) {
      if (!count) return '0'
      if (count >= 10000) {
        return (count / 10000).toFixed(1) + '万'
      }
      return count.toString()
    }
  }
}
</script>

<style scoped>
.recommendation-list {
  padding: 20px;
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.recommendation-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.defense-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #e8f5e9;
  border-radius: 16px;
  font-size: 14px;
  color: #2e7d32;
}

.badge-icon {
  font-size: 16px;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.video-card.suspicious {
  border: 2px solid #ff6b6b;
  opacity: 0.7;
}

.video-thumbnail {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 */
  background: #f0f0f0;
  overflow: hidden;
}

.video-thumbnail img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.suspicious-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 107, 107, 0.9);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.video-info {
  padding: 12px;
}

.video-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>



