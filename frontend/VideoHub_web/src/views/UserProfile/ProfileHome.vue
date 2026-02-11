<template>
  <div class="profile-home">
    <!-- 收藏夹内容 -->
    <section class="section favorites-section">
      <div class="section-header">
        <h2 class="section-title">收藏夹</h2>
      </div>
      <div v-if="loadingFolders" class="loading">加载中...</div>
      <div v-else-if="folders.length === 0" class="empty">暂无收藏夹</div>
      <div v-else class="folder-grid">
        <article
          v-for="folder in folders"
          :key="folder.id"
          class="folder-card"
          @click="openFolder(folder.id)"
        >
          <div class="folder-cover">
            <img 
              v-if="folder.coverUrl" 
              :src="normalizeImageUrl(folder.coverUrl)" 
              alt="" 
              @error="onImageError" 
            />
            <div 
              v-show="!folder.coverUrl" 
              class="folder-cover-placeholder"
            >
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 16L8.586 11.414C9.367 10.633 10.633 10.633 11.414 11.414L16 16M14 14L15.586 12.414C16.367 11.633 17.633 11.633 18.414 12.414L20 14M14 8H14.01M6 20H18C19.105 20 20 19.105 20 18V6C20 4.895 19.105 4 18 4H6C4.895 4 4 4.895 4 6V18C4 19.105 4.895 20 6 20Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
          <div class="folder-info">
            <div class="folder-name" :title="folder.name">{{ folder.name }}</div>
            <div class="folder-count">{{ folder.count }} 个视频</div>
          </div>
        </article>
      </div>
    </section>

    <!-- 最近点赞 -->
    <section class="section likes-section">
      <div class="section-header">
        <h2 class="section-title">最近点赞</h2>
      </div>
      <div v-if="loadingLikes" class="loading">加载中...</div>
      <div v-else-if="likedVideos.length === 0" class="empty">暂无点赞</div>
      <div v-else class="video-grid">
        <div
          v-for="v in likedVideos"
          :key="v.id"
          class="video-card-wrapper"
          @click="openVideo(v.videoId)"
        >
          <VideoCard
            :video="formatVideoForCard(v)"
            :on-img-error="onImageError"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoriteFolderList } from '@/api/favoriteFolder'
import { getUserLikedVideos } from '@/api/like'
import { useUserStore } from '@/stores/user'
import VideoCard from '@/components/VideoCard.vue'

const router = useRouter()
const userStore = useUserStore()

const folders = ref([])
const likedVideos = ref([])
const loadingFolders = ref(false)
const loadingLikes = ref(false)

onMounted(() => {
  loadFolders()
  loadLikes()
})

async function loadFolders() {
  if (!userStore.isAuthenticated) return
  loadingFolders.value = true
  try {
    const userId = userStore.user?.id || userStore.user?.userId
    if (!userId) {
      loadingFolders.value = false
      return
    }
    const { data } = await getFavoriteFolderList(userId)
    if (data && data.success && Array.isArray(data.list)) {
      folders.value = data.list.map(item => ({
        id: item.id,
        name: item.name || '未命名收藏夹',
        count: item.count ?? 0,
        coverUrl: item.coverUrl || '',
        description: item.description || '',
        isPublic: item.isPublic !== false
      }))
      
      // 如果收藏夹没有封面，尝试从第一个视频获取（异步加载，不阻塞UI）
      folders.value.forEach((folder, index) => {
        if ((!folder.coverUrl || folder.coverUrl === '') && folder.count > 0) {
          // 异步获取封面，不阻塞主流程
          import('@/api/favorite').then(({ getFavoriteListByFolder }) => {
            return getFavoriteListByFolder(userId, folder.id, 1, 1)
          }).then(({ data: videoData }) => {
            if (videoData && videoData.success && videoData.list && videoData.list.length > 0) {
              const firstVideo = videoData.list[0]
              if (firstVideo.coverUrl) {
                // 使用 Vue 3 的响应式更新方式
                const folderToUpdate = folders.value[index]
                if (folderToUpdate) {
                  folderToUpdate.coverUrl = normalizeImageUrl(firstVideo.coverUrl)
                }
              }
            }
          }).catch((e) => {
            // 忽略错误
            console.warn(`获取收藏夹 ${folder.id} 封面失败:`, e)
          })
        }
      })
    } else {
      folders.value = []
    }
  } catch (e) {
    console.error('加载收藏夹列表失败:', e)
    folders.value = []
  } finally {
    loadingFolders.value = false
  }
}

async function loadLikes() {
  if (!userStore.isAuthenticated) return
  loadingLikes.value = true
  try {
    const { data } = await getUserLikedVideos(1, 12)
    if (data && data.success && Array.isArray(data.list)) {
      likedVideos.value = data.list.map(item => ({
        id: item.id,
        videoId: item.videoId,
        title: item.title || '无标题',
        coverUrl: normalizeImageUrl(item.coverUrl),
        duration: item.duration || '00:00',
        viewCount: item.viewCount || 0,
        commentCount: item.commentCount || 0,
        createTime: item.createTime,
        uploaderName: item.uploaderName || '',
        uploaderAvatar: item.uploaderAvatar || '',
        uploaderId: item.uploaderId || null
      }))
    } else {
      likedVideos.value = []
    }
  } catch (e) {
    console.error('加载点赞列表失败:', e)
    likedVideos.value = []
  } finally {
    loadingLikes.value = false
  }
}

function normalizeImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return url
  }
  return '/' + url
}

function onImageError(e) {
  // 图片加载失败时，隐藏图片显示占位符
  const img = e.target
  if (img) {
    img.style.display = 'none'
    const placeholder = img.parentElement?.querySelector('.folder-cover-placeholder')
    if (placeholder) {
      placeholder.style.display = 'flex'
    }
  }
}

function openVideo(videoId) {
  if (!videoId) return
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const path = `/video/${encodeURIComponent(videoId)}`
  const url = `${normalizedBase}${path}`
  window.open(url, '_blank')
}

function openFolder(folderId) {
  if (!folderId) return
  // 跳转到收藏tab并展开对应收藏夹
  // 通过路由参数传递收藏夹ID
  router.push({
    path: '/user/profile',
    query: {
      tab: 'collections',
      folder: folderId
    }
  })
}

function formatVideoForCard(video) {
  // 格式化时间
  let uploadDate = ''
  if (video.createTime) {
    uploadDate = formatTime(video.createTime)
  }
  
  return {
    id: video.videoId || video.id,
    cover: normalizeImageUrl(video.coverUrl),
    title: video.title || '无标题',
    duration: video.duration || '00:00',
    playCount: video.viewCount || 0,
    commentCount: video.commentCount || 0,
    uploaderName: video.uploaderName || '未知UP主',
    up: video.uploaderName || '未知UP主',
    uploadDate: uploadDate,
    isVideo: true
  }
}

function formatCount(count) {
  if (!count || count === 0) return '0'
  const num = typeof count === 'number' ? count : parseInt(count) || 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    const now = new Date()
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    if (days === 0) return '今天'
    if (days === 1) return '昨天'
    if (days < 7) return `${days}天前`
    if (days < 30) return `${Math.floor(days / 7)}周前`
    if (days < 365) return `${Math.floor(days / 30)}个月前`
    return `${Math.floor(days / 365)}年前`
  } catch (e) {
    return ''
  }
}
</script>

<style scoped lang="scss">
.profile-home {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section {
  .section-header {
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #222;
    margin: 0;
  }

  .loading,
  .empty {
    text-align: center;
    padding: 40px 0;
    color: #999;
    font-size: 14px;
  }
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(246px, 1fr));
  gap: 14px;
}

.video-card-wrapper {
  cursor: pointer;
  width: 100%;
  
  :deep(.video-card) {
    width: 100%;
    
    .thumb-wrap {
      width: 100%;
      aspect-ratio: 246 / 137;
      padding-bottom: 0;
    }
  }
}

.folder-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  width: 100%;
}

.folder-card {
  cursor: pointer;
  transition: transform 0.2s;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;

  &:hover {
    transform: translateY(-2px);
  }

  .folder-cover {
    position: relative;
    width: 100%;
    padding-bottom: 55.7%; // 137/246 ≈ 0.557
    border-radius: 8px;
    overflow: hidden;
    background: #f1f2f3;

    img {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .folder-cover-placeholder {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ccc;
      background: #f5f7fa;
      z-index: 1;
    }
  }

  .folder-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .folder-name {
      font-size: 13px;
      color: #222;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .folder-count {
      font-size: 12px;
      color: #999;
    }
  }
}

.video-card {
  cursor: pointer;
  position: relative;

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

  .v-title-row {
    margin-top: 8px;
    display: flex;
    align-items: flex-start;
    gap: 8px;
    min-height: 36px;
  }

  .v-title {
    flex: 1;
    font-size: 13px;
    color: #222;
    line-height: 1.4;
    height: 36px;
    overflow: hidden;
    display: -webkit-box;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    transition: color 0.2s;

    &:hover {
      color: #00a1d6;
    }
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
</style>

