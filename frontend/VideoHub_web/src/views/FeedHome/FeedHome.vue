<template>
  <div class="feed-page">
    <div class="header-wrapper">
      <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    </div>
    <div ref="feedContainerRef" class="feed-container">
      <div class="feed-content">
      <aside class="left-column">
        <!-- inlined: UserCard.vue -->
        <div class="user-card">
          <div class="avatar">
            <img :src="resolveAvatar(userAvatar)" :alt="userName" @error="onAvatarImgError" />
          </div>
          <div class="info">
            <div class="name">{{ userName || '未登录' }}</div>
            <div class="stats">
              <div><b>{{ userStats.follows || 0 }}</b><span>关注</span></div>
              <div><b>{{ userStats.fans || 0 }}</b><span>粉丝</span></div>
              <div><b>{{ userStats.posts || 0 }}</b><span>动态</span></div>
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
        <div 
          class="composer"
          @mouseenter="onComposerMouseEnter"
          @mouseleave="onComposerMouseLeave"
        >
          <!-- hidden file input (shared by add-tile + bottom bar) -->
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            multiple
            @change="handleImageSelect"
            :disabled="publishing"
            style="display: none;"
          />

          <div class="composer-hint">
            <input
              v-model="feedTitle"
              class="title-input"
              type="text"
              :maxlength="20"
              placeholder="好的标题更容易获得支持，选填20字"
              :disabled="publishing"
            />
          </div>

          <div class="composer-body">
            <textarea 
              v-model="feedContent" 
              class="input" 
              placeholder="有什么想和大家分享的？" 
              rows="3"
              :disabled="publishing"
            ></textarea>
          </div>

          <!-- 图片区域（点击底部图片按钮后出现） -->
          <div v-if="showImagePanel || selectedImages.length > 0" class="image-preview-container">
            <button
              v-if="selectedImages.length < 9"
              class="image-add-tile"
              type="button"
              :disabled="publishing"
              @click="triggerFileSelect"
              title="添加图片"
            >
              <span class="plus">+</span>
            </button>
            <div
              v-for="(img, idx) in selectedImages"
              :key="idx"
              class="image-preview-item"
              :class="{ uploading: img.uploading }"
            >
              <img :src="img.url || img" :alt="`图片${idx + 1}`" />
              <div v-if="img.uploading" class="uploading-mask">上传中...</div>
              <button class="remove-image" @click="removeImage(idx)" :disabled="publishing">×</button>
            </div>
          </div>

          <!-- 底部操作栏 -->
          <div class="composer-bottom">
            <div class="bottom-left">
              <button
                class="icon-btn"
                type="button"
                :disabled="publishing"
                @click="showEmojiPicker = !showEmojiPicker"
                title="表情"
              >
                😊
              </button>
              <button
                class="icon-btn"
                type="button"
                :disabled="publishing || selectedImages.length >= 9"
                @click="openImagePanel"
                title="图片"
              >
                🖼️
              </button>
              <button class="icon-btn" type="button" disabled title="@（暂未实现）">@</button>
              <button class="icon-btn" type="button" disabled title="直播（暂未实现）">📡</button>
            </div>

            <div class="bottom-right">
              <span class="count">{{ feedContent.trim().length }}</span>
              <span class="divider">|</span>
              <button class="icon-btn" type="button" disabled title="设置（暂未实现）">⚙</button>
              <button 
                class="publish-btn" 
                :disabled="publishing || (!feedContent.trim() && selectedImages.length === 0)"
                @click="publishFeed"
              >
                {{ publishing ? '发布中...' : '发布' }}
              </button>
            </div>
          </div>
          
          <!-- 表情包选择器（悬浮窗） -->
          <div 
            v-if="showEmojiPicker" 
            class="emoji-picker"
          >
            <div class="emoji-grid">
              <span 
                v-for="emoji in commonEmojis" 
                :key="emoji"
                class="emoji-item"
                @click="insertEmoji(emoji)"
              >
                {{ emoji }}
              </span>
            </div>
          </div>
        </div>

        <!-- inlined: StoriesStrip.vue -->
        <div class="stories">
          <div
            class="story"
            :class="{ active: selectedFollowingId === null }"
            @click="selectAllDynamics"
          >
            <div class="bubble all">
              <!-- 全部动态图标 -->
              <svg class="all-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path d="M541.87 55.47l-0.06 296.95c44.59-51.23 110.27-83.62 183.53-83.62 134.32 0 243.2 108.88 243.2 243.2v29.87l-296.95-0.06c51.23 44.59 83.62 110.27 83.62 183.53 0 134.32-108.88 243.2-243.2 243.2h-29.87l0.02-296.91c-44.59 51.21-110.26 83.58-183.49 83.58-134.32 0-243.2-108.88-243.2-243.2v-29.87l296.95 0.06c-51.23-44.59-83.62-110.27-83.62-183.53 0-134.32 108.88-243.2 243.2-243.2h29.87z m0 488.83v362.03l5-0.83c81.83-15.74 144.32-85.85 148.39-171.27l0.21-8.89c0-89.41-63.95-163.87-148.6-180.16l-5-0.88z m-62.17-2.43H117.67l0.83 5c15.74 81.83 85.85 144.32 171.27 148.39l8.89 0.21c89.41 0 163.87-63.95 180.16-148.6l0.88-5z m245.63-213.34c-89.41 0-163.87 63.95-180.16 148.6l-0.88 5h362.03l-0.83-5c-15.74-81.83-85.85-144.32-171.27-148.39l-8.89-0.21z m-243.2-210.9l-5 0.88c-84.65 16.28-148.6 90.75-148.6 180.16l0.21 8.89c4.07 85.42 66.56 155.53 148.39 171.27l5 0.83V117.63z" />
              </svg>
            </div>
            <div class="label">全部动态</div>
          </div>

          <div v-if="followingUsers.length === 0" class="empty-friends">暂无关注的好友</div>
          <div
            v-for="user in followingUsers"
            :key="user.id"
            class="story"
            :class="{ active: selectedFollowingId === user.id }"
            @click="selectFollowingUser(user.id)"
          >
            <div class="bubble">
              <img :src="resolveAvatar(user.avatar)" :alt="user.username" @error="onAvatarImgError" />
            </div>
            <div class="label">{{ user.username || '用户' }}</div>
          </div>
        </div>

        <!-- inlined: FeedList.vue -->
        <div class="feed-list">
          <div v-if="loading && feedList.length === 0" class="loading">加载中...</div>
          <div v-else-if="feedList.length === 0" class="empty">暂无动态</div>
          <article 
            v-for="item in feedList" 
            :key="`${item.type}-${item.id}`" 
            :id="item.type === 'feed' ? 'feed-card-' + item.id : undefined"
            class="feed-card"
            :class="{ 'feed-text-card': item.type === 'feed' }"
            @click="item.type === 'video' ? openVideoInNewTab(item.videoId) : null"
          >
            <header class="meta">
              <div class="avatar">
                <img :src="resolveAvatar(item.uploaderAvatar)" :alt="item.uploaderName" @error="onAvatarImgError" />
              </div>
              <div class="who">
                <div class="name">{{ item.uploaderName || (item.type === 'video' ? '未知UP主' : '未知用户') }}</div>
                <div class="sub">{{ formatTime(item.publishTime) }} · {{ item.type === 'video' ? '投稿了视频' : '发布了动态' }}</div>
              </div>
            </header>
            <div class="content">
              <!-- 视频内容 -->
              <template v-if="item.type === 'video'">
              <div class="thumb" :style="{ backgroundImage: item.coverUrl ? `url(${item.coverUrl})` : 'none' }">
                <span v-if="item.duration" class="duration">{{ formatDuration(item.duration) }}</span>
              </div>
              <div class="title">{{ item.title || '无标题' }}</div>
              </template>
              <!-- 动态内容 -->
              <template v-else>
                <div v-if="item.title" class="feed-title-text">{{ item.title }}</div>
                <div class="feed-content-text">{{ item.content }}</div>
                <div v-if="item.images && item.images.length > 0" class="feed-images">
                  <img 
                    v-for="(img, idx) in item.images" 
                    :key="idx" 
                    :src="normalizeAvatarUrl(img)" 
                    :alt="`图片${idx + 1}`"
                    class="feed-image"
                  />
                </div>
              </template>
            </div>
          </article>
          <!-- 加载更多触发器 -->
          <div 
            ref="loadMoreTrigger" 
            class="loading-bar"
          >
            <span v-if="loadingMore">加载中...</span>
            <span v-else-if="finished">已加载全部</span>
            <span v-else style="visibility: hidden;">加载更多</span>
          </div>
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
  </div>
  
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import { useUserStore } from '@/stores/user'
import { fetchVideos } from '@/api/video'
import { fetchFeeds, fetchFeedById, createFeed, uploadImage } from '@/api/feed'
import { fetchMyProfile } from '@/api/userProfile'
import { getFollowingUsers } from '@/api/follow'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const fileInputRef = ref(null)
const showImagePanel = ref(false)
const triggerFileSelect = () => {
  if (publishing.value) return
  if (selectedImages.value.length >= 9) {
    ElMessage.warning('最多只能上传9张图片')
    return
  }
  fileInputRef.value?.click?.()
}
const openImagePanel = () => {
  if (publishing.value) return
  showImagePanel.value = true
  triggerFileSelect()
}

// 规范化头像 URL
const normalizeAvatarUrl = (url) => {
  if (!url) return ''
  // data URL（默认灰头像）直接返回
  if (url.startsWith('data:')) return url
  // 如果已经是完整 URL（http/https），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径（以 / 开头），直接返回
  if (url.startsWith('/')) {
    return url
  }
  // 其他情况，当作相对路径处理
  return '/' + url
}

const DEFAULT_GREY_AVATAR = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 120 120">
    <circle cx="60" cy="60" r="58" fill="#d1d5db"/>
    <circle cx="60" cy="48" r="18" fill="#b6c0ca"/>
    <path d="M22 120c6-30 25-46 38-46s32 16 38 46" fill="#b6c0ca"/>
  </svg>`
)}`

const resolveAvatar = (url) => {
  if (!url) return DEFAULT_GREY_AVATAR
  const out = normalizeAvatarUrl(url)
  return out || DEFAULT_GREY_AVATAR
}

const onAvatarImgError = (e) => {
  if (!e || !e.target) return
  if (e.target.src === DEFAULT_GREY_AVATAR) return
  e.target.src = DEFAULT_GREY_AVATAR
}

// 用户信息（与 UserDropdown.vue 保持一致）
const user = computed(() => userStore.user || {})
const userName = computed(() => {
  return user.value.username || user.value.nickname || '未登录'
})

const userAvatar = computed(() => {
  const avatar = user.value.avatar || user.value.avatarUrl || ''
  return resolveAvatar(avatar)
})

const userStats = ref({
  follows: 0,
  fans: 0,
  posts: 0
})

// 关注的好友列表
const followingUsers = ref([])
// 当前选中的关注用户（null 代表“全部动态”）
const selectedFollowingId = ref(null)

// 动态列表
const feedList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20

// 发布动态相关
const feedTitle = ref('')
const feedContent = ref('')
const publishing = ref(false)
const selectedImages = ref([]) // 选中的图片列表 [{ url: '...', uploading: false }]
const showEmojiPicker = ref(false)
let emojiCloseTimer = null

// 常用表情包
const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '😣', '😖',
  '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '🤯',
  '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗', '🤔',
  '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯', '😦',
  '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴',
  '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈', '👿',
  '👹', '👺', '🤡', '💩', '👻', '💀', '☠️', '👽', '👾', '🤖',
  '🎃', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾'
]

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '刚刚'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return ''
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) {
    return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m}:${s.toString().padStart(2, '0')}`
}

// 加载动态数据（同时获取关注用户的视频和动态，按时间排序）
const loadFeed = async (reset = false, options = { keepListOnReset: false }) => {
  if (loading.value || loadingMore.value) return

  if (reset) {
    page.value = 1
    finished.value = false
    if (!options.keepListOnReset) {
    feedList.value = []
    }
  }

  const isFirstPage = page.value === 1
  if (isFirstPage) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const currentUserId = userStore.user?.userId || userStore.user?.id
    if (!currentUserId) {
      feedList.value = []
      return
    }

    // 并行获取视频和动态
    const [videosResponse, feedsResponse] = await Promise.all([
      fetchVideos(page.value, pageSize, currentUserId, true, selectedFollowingId.value),
      fetchFeeds(page.value, pageSize, currentUserId, true, selectedFollowingId.value)
    ])

    const videosData = videosResponse.data || {}
    const feedsData = feedsResponse.data || {}
    const videosList = Array.isArray(videosData.list) ? videosData.list : []
    const feedsList = Array.isArray(feedsData.list) ? feedsData.list : []
    
    // 转换视频数据格式
    const mappedVideos = videosList.map(item => {
      // 处理时间：优先使用 createTime，如果没有则使用当前时间
      let publishTime = new Date().toISOString()
      if (item.createTime) {
        // 如果是字符串，直接使用；如果是时间戳，转换
        if (typeof item.createTime === 'string') {
          publishTime = item.createTime
        } else if (item.createTime instanceof Date) {
          publishTime = item.createTime.toISOString()
        } else if (typeof item.createTime === 'number') {
          publishTime = new Date(item.createTime).toISOString()
        }
      } else if (item.publishTime) {
        publishTime = item.publishTime
      }
      
      return {
        type: 'video',
      id: item.id || item.videoId,
      videoId: item.videoId || item.id,
      title: item.title || '无标题',
      coverUrl: item.coverUrl || '/assets/home.png',
      duration: item.duration || 0,
      uploaderName: item.uploaderName || item.uploader || '未知UP主',
      uploaderAvatar: item.uploaderAvatar ? normalizeAvatarUrl(item.uploaderAvatar) : '',
        uploaderId: item.uploaderId || null,
        publishTime: publishTime,
      likeCount: item.likeCount || item.likes || 0,
      commentCount: item.commentCount || item.comments || 0,
      shareCount: item.shareCount || item.shares || 0
      }
    })

    // 转换动态数据格式
    const mappedFeeds = feedsList.map(item => ({
      type: 'feed',
      id: item.id,
      feedId: item.id,
      title: item.title || '',
      content: item.content || '',
      images: item.images || [],
      uploaderName: item.uploaderName || '未知用户',
      uploaderAvatar: item.uploaderAvatar ? normalizeAvatarUrl(item.uploaderAvatar) : '',
      uploaderId: item.uploaderId || null,
      publishTime: item.createTime || new Date().toISOString(),
      likeCount: item.likeCount || 0,
      commentCount: item.commentCount || 0,
      shareCount: item.shareCount || 0
    }))

    // 合并并按时间排序（最新的在前）
    const mergedList = [...mappedVideos, ...mappedFeeds].sort((a, b) => {
      const timeA = new Date(a.publishTime).getTime()
      const timeB = new Date(b.publishTime).getTime()
      return timeB - timeA
    })

    if (reset) {
      feedList.value = mergedList
    } else {
      feedList.value = [...feedList.value, ...mergedList]
    }

    // 判断是否已加载全部（如果视频和动态都少于pageSize，说明已加载完）
    const videosTotal = typeof videosData.total === 'number' ? videosData.total : 0
    const feedsTotal = typeof feedsData.total === 'number' ? feedsData.total : 0
    const hasMoreVideos = mappedVideos.length === pageSize
    const hasMoreFeeds = mappedFeeds.length === pageSize
    
    if (!hasMoreVideos && !hasMoreFeeds) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (error) {
    console.error('加载动态失败:', error)
    if (isFirstPage) {
      feedList.value = []
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

/** 将接口返回的单条动态转为列表项（与 loadFeed 中 mappedFeeds 一致） */
const mapApiFeedToListItem = (item) => ({
  type: 'feed',
  id: item.id,
  feedId: item.id,
  title: item.title || '',
  content: item.content || '',
  images: Array.isArray(item.images) ? item.images : [],
  uploaderName: item.uploaderName || '未知用户',
  uploaderAvatar: item.uploaderAvatar ? normalizeAvatarUrl(item.uploaderAvatar) : '',
  uploaderId: item.uploaderId || null,
  publishTime: item.createTime || new Date().toISOString(),
  likeCount: item.likeCount || 0,
  commentCount: item.commentCount || 0,
  shareCount: item.shareCount || 0
})

/** 路由带 /feed/:feedId 时插入列表并滚动到对应卡片 */
const highlightFeedFromRoute = async () => {
  const fid = route.params.feedId
  if (fid === undefined || fid === null || String(fid).trim() === '') return
  try {
    const res = await fetchFeedById(fid)
    const data = res?.data
    const raw = data?.data ?? data
    if (!raw || raw.id == null) return
    const mapped = mapApiFeedToListItem(raw)
    const exists = feedList.value.some(
      (x) => x.type === 'feed' && String(x.id) === String(raw.id)
    )
    if (!exists) {
      feedList.value = [mapped, ...feedList.value]
    }
    await nextTick()
    requestAnimationFrame(() => {
      const el = document.getElementById(`feed-card-${raw.id}`)
      el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    })
  } catch (e) {
    console.warn('定位动态失败', e)
  }
}

// 在新标签页打开视频播放页
const openVideoInNewTab = (videoId) => {
  if (!videoId) return
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const path = `/video/${encodeURIComponent(videoId)}`
  const url = `${normalizedBase}${path}`
  window.open(url, '_blank')
}

// 处理图片选择
const handleImageSelect = async (event) => {
  const files = Array.from(event.target.files || [])
  if (files.length === 0) return
  
  // 限制最多9张图片
  const remainingSlots = 9 - selectedImages.value.length
  if (remainingSlots <= 0) {
    ElMessage.warning('最多只能上传9张图片')
    return
  }
  
  const filesToUpload = files.slice(0, remainingSlots)
  
  for (const file of filesToUpload) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`${file.name} 不是图片文件`)
      continue
    }
    
    // 验证文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning(`${file.name} 大小超过5MB`)
      continue
    }
    
    // 创建预览
    const previewUrl = URL.createObjectURL(file)
    const imageItem = {
      file: file,
      url: previewUrl,
      uploading: true,
      uploadedUrl: null
    }
    selectedImages.value.push(imageItem)
    
    // 上传图片
    try {
      const response = await uploadImage(file)
      if (response.data && response.data.success) {
        imageItem.uploadedUrl = response.data.url
        imageItem.uploading = false
        // 注意：不要在这里 revokeObjectURL(previewUrl)。
        // Vue 可能仍在使用 blob: 预览地址渲染 <img>，过早 revoke 会导致预览“裂图/变灰”。
        // 预览继续使用 imageItem.url（blob:）；真正保存/展示使用 uploadedUrl。
      } else {
        throw new Error(response.data?.message || '上传失败')
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      ElMessage.error(`${file.name} 上传失败: ${error.response?.data?.message || error.message}`)
      // 移除失败的图片
      const index = selectedImages.value.indexOf(imageItem)
      if (index > -1) {
        URL.revokeObjectURL(previewUrl)
        selectedImages.value.splice(index, 1)
      }
    }
  }
  
  // 清空文件选择
  event.target.value = ''
}

// 移除图片
const removeImage = (index) => {
  const imageItem = selectedImages.value[index]
  if (imageItem && imageItem.url && imageItem.url.startsWith('blob:')) {
    URL.revokeObjectURL(imageItem.url)
  }
  selectedImages.value.splice(index, 1)
}

// 插入表情包
const insertEmoji = (emoji) => {
  const textarea = document.querySelector('.composer .input')
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = feedContent.value
    feedContent.value = text.substring(0, start) + emoji + text.substring(end)
    // 设置光标位置
    nextTick(() => {
      textarea.focus()
      textarea.setSelectionRange(start + emoji.length, start + emoji.length)
    })
  } else {
    feedContent.value += emoji
  }
  showEmojiPicker.value = false
}

// 发布动态
const publishFeed = async () => {
  const title = feedTitle.value.trim()
  const content = feedContent.value.trim()
  const imageUrls = selectedImages.value
    .map(img => img.uploadedUrl || img.url)
    .filter(url => url && !url.startsWith('blob:')) // 只使用已上传的图片
  
  if (!content && imageUrls.length === 0) {
    ElMessage.warning('请输入动态内容或上传图片')
    return
  }

  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 检查是否有图片还在上传中
  const uploadingImages = selectedImages.value.filter(img => img.uploading)
  if (uploadingImages.length > 0) {
    ElMessage.warning('请等待图片上传完成')
    return
  }

  publishing.value = true
  try {
    const response = await createFeed(title, content, imageUrls)
    if (response.data && response.data.success) {
      ElMessage.success('发布成功')
      feedTitle.value = ''
      feedContent.value = ''
      selectedImages.value = []
      showEmojiPicker.value = false
      
      // 获取当前用户信息
      const currentUser = user.value || {}
      const currentUserId = userStore.user?.userId || userStore.user?.id
      
      // 直接将新发布的动态添加到列表顶部
      const newFeed = {
        type: 'feed',
        id: response.data.data?.id,
        feedId: response.data.data?.id,
        content: content,
        images: response.data.data?.images || imageUrls,
        uploaderName: currentUser.username || currentUser.loginAccount || '我',
        uploaderAvatar: userAvatar.value || '',
        uploaderId: currentUserId,
        publishTime: new Date().toISOString(),
        likeCount: 0,
        commentCount: 0,
        shareCount: 0
      }
      
      // 添加到列表顶部
      feedList.value = [newFeed, ...feedList.value]
      
      // 延迟刷新以确保数据同步（可选）
      setTimeout(() => {
        loadFeed(true)
      }, 500)
    } else {
      ElMessage.error(response.data?.message || '发布失败')
    }
  } catch (error) {
    console.error('发布动态失败:', error)
    ElMessage.error(error.response?.data?.message || '发布失败，请稍后重试')
  } finally {
    publishing.value = false
  }
}

const selectAllDynamics = () => {
  selectedFollowingId.value = null
}

const selectFollowingUser = (followingId) => {
  selectedFollowingId.value = followingId
}

// 加载关注的好友列表
const loadFollowingUsers = async () => {
  if (!userStore.isAuthenticated) {
    followingUsers.value = []
    return
  }
  
  try {
    const { data } = await getFollowingUsers()
    if (data && data.success && Array.isArray(data.users)) {
      followingUsers.value = data.users
    } else {
      followingUsers.value = []
    }
  } catch (error) {
    console.error('加载关注好友列表失败:', error)
    followingUsers.value = []
  }
}

// 处理发布框鼠标进入/离开，用于控制表情悬浮窗关闭时机
const onComposerMouseEnter = () => {
  if (emojiCloseTimer) {
    clearTimeout(emojiCloseTimer)
    emojiCloseTimer = null
  }
}

const onComposerMouseLeave = () => {
  if (!showEmojiPicker.value) return
  if (emojiCloseTimer) {
    clearTimeout(emojiCloseTimer)
  }
  // 稍微延迟关闭，避免快速移动时误触
  emojiCloseTimer = setTimeout(() => {
    showEmojiPicker.value = false
    emojiCloseTimer = null
  }, 150)
}

watch(() => selectedFollowingId.value, () => {
  loadFeed(true, { keepListOnReset: true })
})


// 使用 Intersection Observer 检测底部元素，实现无限滚动
const loadMoreTrigger = ref(null)
const feedContainerRef = ref(null)
let observer = null

const setupIntersectionObserver = () => {
  if (!loadMoreTrigger.value || !feedContainerRef.value) return
  
  if (observer) {
    observer.disconnect()
  }
  
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (entry.isIntersecting && !loading.value && !loadingMore.value && !finished.value) {
        loadFeed()
      }
    },
    {
      root: feedContainerRef.value, // 使用 .feed-container 作为滚动容器
      rootMargin: '100px',
      threshold: 0.1
    }
  )
  
  observer.observe(loadMoreTrigger.value)
}

// 监听动态列表变化，重新设置 observer
watch(
  () => feedList.value.length,
  () => {
    if (!finished.value) {
      nextTick(() => {
        setupIntersectionObserver()
      })
    }
  }
)

// 监听 finished 状态
watch(
  () => finished.value,
  (isFinished) => {
    if (isFinished && observer) {
      observer.disconnect()
      observer = null
    } else if (!isFinished) {
      nextTick(() => {
        setupIntersectionObserver()
      })
    }
  }
)

// 加载用户资料
const loadUserProfile = async () => {
  try {
    const response = await fetchMyProfile()
    if (response.data && response.data.success) {
      const profileData = response.data
      const normalizedAvatar = normalizeAvatarUrl(profileData.avatar)
      // 更新用户资料到 store
      const currentUser = user.value || {}
      userStore.setUser({
        ...currentUser,
        id: profileData.id || currentUser.id,
        userId: profileData.id || currentUser.userId || currentUser.id,
        username: profileData.username || currentUser.username,
        loginAccount: currentUser.loginAccount || profileData.account,
        avatar: normalizedAvatar,
        bio: profileData.bio || currentUser.bio
      })
    }
  } catch (error) {
    // 静默失败，不影响页面正常使用
    console.warn('加载用户资料失败:', error)
  }
}

onMounted(async () => {
  await loadFeed(true)
  loadFollowingUsers()
  await highlightFeedFromRoute()
  nextTick(() => {
    setupIntersectionObserver()
  })

  // 如果用户已登录但没有头像，自动加载用户资料
  if (userStore.isAuthenticated) {
    const currentUser = user.value || {}
    if (!currentUser.avatar && !currentUser.avatarUrl) {
      loadUserProfile()
    }
  }
})

watch(() => route.params.feedId, () => {
  highlightFeedFromRoute()
})

// 监听用户状态变化，确保头像正确更新
watch(() => userStore.isAuthenticated, (isAuth) => {
  if (isAuth) {
    const currentUser = user.value || {}
    if (!currentUser.avatar && !currentUser.avatarUrl) {
      loadUserProfile()
    }
  }
})

// 监听用户信息变化，确保头像正确更新
watch(() => user.value, (newUser) => {
  if (newUser && userStore.isAuthenticated) {
    if (!newUser.avatar && !newUser.avatarUrl) {
      loadUserProfile()
    }
  }
}, { deep: true })

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style lang="scss" scoped>
@use "sass:color";
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
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  height: 100vh;
  margin: 0 auto;
  background-image: url('/assets/history_bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  color: $text-primary; // 明确页面默认文字颜色，避免继承到过浅的颜色导致看不清
  display: flex;
  flex-direction: column;
  overflow: hidden; // 页面不允许滚动
}

.header-wrapper {
  flex-shrink: 0; // 顶部栏固定，不参与滚动
  position: relative;
  z-index: 1000;
  height: 64px; // 预留顶部栏高度，避免遮挡右侧滚动条顶部
}

.feed-container {
  flex: 1; // 占据剩余空间
  width: 100%; // 占据全屏宽度
  overflow-y: auto; // 模块 c 允许滚动
  overflow-x: hidden;
  // 使用浏览器原生滚动条（出现在最右侧）
  scrollbar-width: auto; // Firefox
}

.feed-content {
  width: $container-width;
  margin: 0 auto;
  display: grid;
  grid-template-columns: $left-column-width 1fr $right-column-width;
  gap: $spacing-lg;
  padding: $spacing-lg $spacing-lg $spacing-xl;
  box-sizing: border-box;
}

// Left Column
.left-column {
  @include flex-column;
  gap: $spacing-md;
  height: fit-content;
  align-self: start;
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
    color: $text-primary; // 提升对比度
    font-size: 14px;
    font-weight: 600;
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
  height: fit-content;
  align-self: start;
  min-width: 0; // 允许 grid 列收缩，防止溢出
  overflow: hidden; // 防止内容溢出
}

// Right Column
.right-column {
  @include flex-column;
  gap: $spacing-md;
  height: fit-content;
  align-self: start;
  
  .right {
    @include flex-column;
    gap: $spacing-md;
  }
  
  // 社区中心正常滚动
  .card:first-child {
    position: relative;
    flex-shrink: 0;
  }
  
  // 当滚动到热搜榜时，固定热搜榜
  .card:last-child {
    position: -webkit-sticky; // Safari 兼容
    position: sticky;
    top: $spacing-lg; // 固定在距离滚动容器顶部 16px 的位置
    align-self: flex-start;
    max-height: calc(100vh - 64px - $spacing-lg * 2);
    overflow-y: hidden;
    z-index: 10;
    flex-shrink: 0;
  }
}

// User Card Component
.user-card {
  @include card-style;
  
  .avatar {
    @include avatar-style(56px);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 1;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      position: relative;
      z-index: 1;
    }
  }
  
  .info {
    margin-top: $spacing-sm;
    
    .name {
      font-weight: 600;
      color: $text-primary;
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
  // 允许内部悬浮窗（表情面板）溢出显示
  overflow: visible;
  box-sizing: border-box;
  padding: 0; // 使用内部块的 padding，更接近图2布局
  position: relative;
  z-index: 20;

  .composer-hint {
    padding: 8px $spacing-md 0;

    .title-input {
      width: 100%;
      border: none;
      outline: none;
      background: transparent;
      padding: 0;
      font-size: 16px;
      line-height: 1.4;
      color: #18191c;

      &::placeholder {
        color: #777;
      }

      &:disabled {
        color: #999;
        cursor: not-allowed;
      }
    }
  }

  .composer-body {
    padding: 10px $spacing-md 0;
  }

  .input {
    width: 100%;
    border: none;
    outline: none;
    background: transparent;
    padding: 0;
    box-sizing: border-box;
    font-family: inherit;
    font-size: 16px;
    line-height: 1.65;
    resize: none; // 不允许上下拉伸
    min-height: 140px;
    color: #18191c;
    font-weight: 400;

    &::placeholder {
      color: #9aa0a6;
      opacity: 1;
    }

    &:disabled {
      color: #999;
      cursor: not-allowed;
    }

    // 自定义滚动条样式（长文时更好看）
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.12);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: rgba(0, 0, 0, 0.18);
    }
  }
  
  // 图片预览容器
  .image-preview-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
    gap: $spacing-xs;
    margin-top: 10px;
    padding: 0 $spacing-md 4px;

    .image-add-tile {
      width: 96px;
      height: 96px;
      border: 2px dashed #d5dbe3;
      border-radius: 10px;
      background: #fafbfc;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: border-color 0.2s, background 0.2s, color 0.2s;

      .plus {
        font-size: 30px;
        color: #c3c8d0;
        line-height: 1;
      }

      &:hover:not(:disabled) {
        border-color: rgba(0, 174, 236, 0.65);
        background: rgba(0, 174, 236, 0.04);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .image-preview-item {
      position: relative;
      width: 100%;
      padding-top: 100%; // 1:1 比例
      background: #f0f0f0;
      border-radius: $border-radius-sm;
      overflow: hidden;

      &.uploading {
        opacity: 0.9;
      }
      
      img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .uploading-mask {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.35);
        color: #fff;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      .remove-image {
        position: absolute;
        top: 4px;
        right: 4px;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.6);
        color: #fff;
        border: none;
        cursor: pointer;
        font-size: 16px;
        line-height: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.2s;
        
        &:hover:not(:disabled) {
          background: rgba(0, 0, 0, 0.8);
        }
        
        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
    }
  }
  
  // 底部栏
  .composer-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding: 10px $spacing-md 6px;
    border-top: 1px solid #eef0f2;

    .bottom-left {
      display: flex;
      gap: 12px;
    }

    .bottom-right {
      display: flex;
      align-items: center;
      gap: 10px;

      .count {
        color: #8a8a8a;
        font-size: 12px;
        min-width: 18px;
        text-align: right;
      }

      .divider {
        color: #d2d6db;
        font-size: 12px;
      }
    }

    .icon-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 6px 8px;
      border: none;
      background: transparent;
      color: $text-secondary;
      cursor: pointer;
      font-size: 16px;
      border-radius: $border-radius-sm;
      transition: background 0.2s, color 0.2s, opacity 0.2s;

      &:hover:not(:disabled) {
        background: #f0f0f0;
        color: $text-primary;
      }

      &:disabled {
        opacity: 0.45;
        cursor: not-allowed;
      }
    }

    .publish-btn {
      height: 34px;
      padding: 0 18px;
      border-radius: 8px;
      border: none;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      background: rgba(0, 174, 236, 0.25);
      color: #fff;
      transition: background 0.2s, opacity 0.2s;

      &:not(:disabled) {
        background: $primary-color;
      }

      &:hover:not(:disabled) {
        background: color.adjust($primary-color, $lightness: -5%);
      }

      &:disabled {
        cursor: not-allowed;
        opacity: 0.7;
      }
    }
  }

  // 表情包选择器
  .emoji-picker {
    position: absolute;
    left: $spacing-md;
    top: 100%; // 在发布框底部下方展开
    margin-top: 8px;
    padding: $spacing-md;
    background: #fff;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 8px 24px rgba(15, 23, 42, 0.18);
    z-index: 30;

    .emoji-grid {
      display: grid;
      grid-template-columns: repeat(10, 1fr);
      gap: 4px;
      max-height: 200px;
      overflow-y: auto;
      // 自定义滚动条样式，让表情面板滚动条更好看
      scrollbar-width: thin; // Firefox
      scrollbar-color: rgba(0, 0, 0, 0.16) transparent;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: transparent;
      }

      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.16);
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb:hover {
        background: rgba(0, 0, 0, 0.22);
      }

      .emoji-item {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 8px;
        font-size: 20px;
        cursor: pointer;
        border-radius: $border-radius-sm;
        transition: background 0.2s;
        
        &:hover {
          background: #e5e7eb;
        }
      }
    }
  }
}

// 动态标题展示
.feed-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #18191c;
  margin-bottom: 6px;
}

// Stories Component
.stories {
  display: flex;
  gap: $spacing-md;
  @include card-style;
  overflow-x: auto;
  // 隐藏滚动条
  scrollbar-width: none; // Firefox
  -ms-overflow-style: none; // IE/Edge
  
  &::-webkit-scrollbar {
    display: none; // Chrome/Safari
  }
  
  .story {
    @include flex-column;
    align-items: center;
    gap: $spacing-xs;
    cursor: pointer;
    flex-shrink: 0;
    transition: color 0.2s ease, border-color 0.2s ease;
    
    .bubble {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #e3e9ee;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #fff;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      position: relative;
      z-index: 1;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        z-index: 1;
      }

      &.all {
        background: rgba(0, 174, 236, 0.06);
        border-color: rgba(0, 174, 236, 0.3);
      }

      .all-icon {
        width: 26px;
        height: 26px;
        fill: #00AEEC;
      }
    }
    
    .label {
      font-size: 12px;
      color: $text-secondary;
      max-width: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      text-align: center;
    }
  }

  .story.active {
    .bubble {
      border-color: #00AEEC;
      box-shadow: 0 0 0 1px rgba(0, 174, 236, 0.12);
    }

    .label {
      color: #00AEEC;
    }
  }
  
  .empty-friends {
    padding: $spacing-md;
    text-align: center;
    color: $text-muted;
    font-size: 14px;
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
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        z-index: 1;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          position: relative;
          z-index: 1;
        }
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
        background-size: cover;
        background-position: center;
        background-repeat: no-repeat;
        position: relative;
        flex-shrink: 0;
        
        .duration {
          position: absolute;
          right: 6px;
          bottom: 6px;
          background: rgba(0, 0, 0, 0.65);
          color: #fff;
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 12px;
        }
      }
      
      .title {
        font-size: 14px;
        line-height: 1.6;
      }
      
      // 文字动态样式
      .feed-content-text {
        font-size: 14px;
        line-height: 1.8;
        color: $text-primary;
        white-space: pre-wrap;
        word-break: break-word;
        margin-bottom: $spacing-md;
      }
      
      .feed-images {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: $spacing-sm;
        margin-top: $spacing-md;
        
        .feed-image {
          width: 100%;
          height: auto;
          border-radius: $border-radius-sm;
          object-fit: cover;
          cursor: pointer;
          transition: transform 0.2s;
          
          &:hover {
            transform: scale(1.02);
          }
        }
      }
    }
    
    // 文字动态卡片特殊样式
    &.feed-text-card {
      .content {
        flex-direction: column;
      }
    }
    
    .actions {
      display: flex;
      gap: $spacing-lg;
      color: $text-secondary;
      font-size: 13px;
      margin-top: $spacing-sm;
      cursor: pointer;
      
      span {
        transition: color 0.2s;
        
        &:hover {
          color: $primary-color;
        }
      }
    }
  }
  
  .loading-bar {
    text-align: center;
    padding: 20px;
    color: $text-muted;
    font-size: 14px;
  }
  
  .loading, .empty {
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    font-size: 14px;
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


