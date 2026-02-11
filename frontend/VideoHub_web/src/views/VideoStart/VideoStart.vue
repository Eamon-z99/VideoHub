<template>
  <div class="video-page-wrapper">
    <div class="header-wrapper">
      <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    </div>
    <div class="video-page">
    <!-- 主视频区域 -->
    <section class="player-area">
      <!-- 标题与信息 / 互动条 -->
      <div class="video-meta">
        <h1 class="title">{{ title }}</h1>
        <div class="meta-row">
          <div class="meta-left">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              <span>{{ playCount }}</span>
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ commentTotal }}</span>
            </span>
            <span class="meta-item upload-time" v-if="uploadTime">
              {{ uploadTime }}
            </span>
          </div>
        </div>
      </div>

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

               <!-- 操作栏：点赞/收藏/分享（参考B站样式，位于播放器下方） -->
               <div class="action-bar">
                 <div class="action-left">
                   <button
                     class="action-item"
                     :class="{ 'is-active': isLiked }"
                     :disabled="likeLoading"
                     @click="toggleLike"
                   >
                     <span class="action-icon">
                       <svg width="28" height="28" viewBox="0 0 36 36" xmlns="http://www.w3.org/2000/svg">
                         <path
                           fill-rule="evenodd"
                           clip-rule="evenodd"
                           d="M9.77234 30.8573V11.7471H7.54573C5.50932 11.7471 3.85742 13.3931 3.85742 15.425V27.1794C3.85742 29.2112 5.50932 30.8573 7.54573 30.8573H9.77234ZM11.9902 30.8573V11.7054C14.9897 10.627 16.6942 7.8853 17.1055 3.33591C17.2666 1.55463 18.9633 0.814421 20.5803 1.59505C22.1847 2.36964 23.243 4.32583 23.243 6.93947C23.243 8.50265 23.0478 10.1054 22.6582 11.7471H29.7324C31.7739 11.7471 33.4289 13.402 33.4289 15.4435C33.4289 15.7416 33.3928 16.0386 33.3215 16.328L30.9883 25.7957C30.2558 28.7683 27.5894 30.8573 24.528 30.8573H11.9911H11.9902Z"
                           fill="currentColor"
                         />
                       </svg>
                     </span>
                     <span class="action-num">{{ formatCount(likeCount) }}</span>
                   </button>

                   <button
                     class="action-item"
                     :class="{ 'is-active': isFavorited }"
                     :disabled="favoriteLoading"
                     @click="toggleFavorite"
                   >
                     <span class="action-icon">
                       <svg width="28" height="28" viewBox="0 0 28 28" xmlns="http://www.w3.org/2000/svg">
                         <path
                           fill-rule="evenodd"
                           clip-rule="evenodd"
                           d="M19.8071 9.26152C18.7438 9.09915 17.7624 8.36846 17.3534 7.39421L15.4723 3.4972C14.8998 2.1982 13.1004 2.1982 12.4461 3.4972L10.6468 7.39421C10.1561 8.36846 9.25639 9.09915 8.19315 9.26152L3.94016 9.91102C2.63155 10.0734 2.05904 11.6972 3.04049 12.6714L6.23023 15.9189C6.96632 16.6496 7.29348 17.705 7.1299 18.7605L6.39381 23.307C6.14844 24.6872 7.62063 25.6614 8.84745 25.0119L12.4461 23.0634C13.4276 22.4951 14.6544 22.4951 15.6359 23.0634L19.2345 25.0119C20.4614 25.6614 21.8518 24.6872 21.6882 23.307L20.8703 18.7605C20.7051 17.705 21.0339 16.6496 21.77 15.9189L24.9597 12.6714C25.9412 11.6972 25.3687 10.0734 24.06 9.91102L19.8071 9.26152Z"
                           fill="currentColor"
                         />
                       </svg>
                     </span>
                     <span class="action-num">{{ formatCount(favoriteCount) }}</span>
                   </button>

                   <button class="action-item" @click="ElMessage.info('开发中')">
                     <span class="action-icon">
                       <svg width="28" height="28" viewBox="0 0 28 28" xmlns="http://www.w3.org/2000/svg">
                         <path
                           d="M12.6058 10.3326V5.44359C12.6058 4.64632 13.2718 4 14.0934 4C14.4423 4 14.78 4.11895 15.0476 4.33606L25.3847 12.7221C26.112 13.3121 26.2087 14.3626 25.6007 15.0684C25.5352 15.1443 25.463 15.2144 25.3847 15.2779L15.0476 23.6639C14.4173 24.1753 13.4791 24.094 12.9521 23.4823C12.7283 23.2226 12.6058 22.8949 12.6058 22.5564V18.053C7.59502 18.053 5.37116 19.9116 2.57197 23.5251C2.47607 23.6489 2.00031 23.7769 2.00031 23.2122C2.00031 16.2165 3.90102 10.3326 12.6058 10.3326Z"
                           fill="currentColor"
                         />
                       </svg>
                     </span>
                     <span class="action-num">{{ formatCount(0) }}</span>
                   </button>
                 </div>

                 <div class="action-right">
                   <button class="action-link" @click="ElMessage.info('开发中')">稿件举报</button>
                   <button class="action-link" @click="ElMessage.info('开发中')">记笔记</button>
                   <button class="action-more" @click="ElMessage.info('开发中')">⋯</button>
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
        <div class="comment-header">
          <div class="left">
            <span class="title">评论 {{ commentTotal }}</span>
          </div>
          <div class="sort-tabs">
            <span
              class="sort-item"
              :class="{ 'is-active': activeCommentSort === 'time' }"
              @click="setCommentSort('time')"
            >
              按时间
            </span>
            <span
              class="sort-item"
              :class="{ 'is-active': activeCommentSort === 'hot' }"
              @click="setCommentSort('hot')"
            >
              按热度
            </span>
          </div>
        </div>

        <div class="comment-editor">
          <div class="editor-avatar">
            <img
              v-if="userStore.user?.avatar"
              :src="normalizeAvatarUrl(userStore.user.avatar)"
              alt="avatar"
            />
            <div v-else class="avatar-placeholder"></div>
          </div>
          <div class="editor-main">
            <el-input
              v-model="commentText"
              :rows="3"
              type="textarea"
              placeholder="进来来和UP唠会嗑~"
              class="editor-input"
            />
            <div class="editor-actions">
              <span v-if="!userStore.isAuthenticated" class="login-hint">请先登录再参与评论</span>
              <el-button
                type="primary"
                size="small"
                :loading="submittingComment"
                @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>
        </div>

        <div class="comment-list">
          <div v-if="comments.length === 0 && !loadingComments" class="no-comment">
            还没有评论，来抢沙发吧~
          </div>
          <div v-else v-for="item in comments" :key="item.id" class="comment-item">
            <img :src="item.avatar || '/images/default-avatar.png'" class="avatar" />
            <div class="content">
              <div class="header">
                <span class="name">{{ item.name }}</span>
                <span class="time">{{ item.time }}</span>
              </div>
              <p class="text">{{ item.text }}</p>
              <div class="comment-footer">
                <div class="reply-summary" v-if="item.replies && item.replies.length">
                  共 {{ item.replies.length }} 条回复
                </div>
                <div class="comment-actions">
                  <el-button text size="small" @click="toggleCommentLike(item)">
                    {{ (item._liked ? '已赞 ' : '点赞 ') + (item.likes ?? 0) }}
                  </el-button>
                  <el-button
                    text
                    size="small"
                    @click="startReply(item)"
                  >
                    回复
                  </el-button>
                </div>
              </div>

              <!-- 回复列表 -->
              <div v-if="item.replies && item.replies.length" class="reply-list">
                       <div
                         v-for="reply in item.replies"
                         :key="reply.id"
                         class="reply-item"
                       >
                         <img :src="reply.avatar || '/images/default-avatar.png'" class="reply-avatar" />
                         <div class="reply-content">
                           <div class="reply-header">
                             <span class="name">{{ reply.name }}</span>
                             <span class="time">{{ reply.time }}</span>
                           </div>
                           <p class="text">{{ reply.text }}</p>
                           <div class="reply-actions">
                             <el-button
                               text
                               size="small"
                               @click="startReply(item, reply)"
                             >
                               回复
                             </el-button>
                           </div>
                         </div>
                       </div>
              </div>

              <!-- 回复输入框 -->
                       <div v-if="replyTarget && replyTarget.id === item.id" class="reply-editor">
                <el-input
                  v-model="replyText"
                  type="textarea"
                  :rows="2"
                           :placeholder="`回复 @${replyPlaceholderName || item.name}：`"
                />
                <div class="reply-actions">
                  <el-button size="small" @click="cancelReply">取消</el-button>
                  <el-button
                    type="primary"
                    size="small"
                    :loading="submittingComment"
                    @click="submitReply(item)"
                  >
                    发表回复
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="loadingComments" class="loading-more">加载中...</div>
          <div
            v-else-if="hasMoreComments"
            class="load-more"
            @click="loadMoreComments"
          >
            加载更多评论
          </div>
        </div>
      </div>
    </section>

    <!-- 右侧推荐区 -->
    <aside class="sidebar">
      <div class="uploader-card">
        <img v-if="uploader.avatar" class="u-avatar" :src="uploader.avatar" :alt="uploader.name || '作者'" />
        <div v-else class="u-avatar-placeholder"></div>
        <div class="u-info">
          <div class="u-name">{{ uploader.name || '用户上传' }}</div>
          <div class="u-stats">视频 {{ uploader.videoCount }} · 粉丝 {{ uploader.fans }}</div>
        </div>
        <el-button 
          type="primary" 
          size="small" 
          round
          :loading="followLoading"
          :disabled="!uploader.id || !userStore.isAuthenticated"
          @click="toggleFollow"
        >
          {{ isFollowing ? '已关注' : '+ 关注' }}
        </el-button>
      </div>

      <div class="recommend-title">相关推荐</div>
      <div class="recommend-list">
        <div class="rec-item" v-for="rec in recommends" :key="rec.id" @click="openRecommend(rec)">
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
      <div
        v-if="showExpandBtn"
        class="recommend-expand"
        @click="toggleRecommends"
      >
        {{ recommendsExpanded ? '关闭' : '展开' }}
      </div>
    </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View, ChatDotRound, Timer } from '@element-plus/icons-vue'
import { Pointer, Star, Share } from '@element-plus/icons-vue'
import { fetchVideoDetail, fetchVideosByUploader } from '@/api/video'
import { recordHistory } from '@/api/history'
import TopHeader from '@/components/TopHeader.vue'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { followUser, unfollowUser, checkFollow, getUserStats } from '@/api/follow'
import { likeVideo, unlikeVideo, checkLike, getLikeCount } from '@/api/like'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getComments, addComment, likeComment, unlikeComment, getCommentReplies } from '@/api/comment'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const MAX_RECOMMENDS = 40
const INITIAL_RECOMMENDS = 4

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
const likeCount = ref(0)
const favoriteCount = ref(0)
const tags = ref(['本地视频', '离线播放'])
const description = ref('播放来自 E:\\Videos 目录的本地视频。')
const uploadTime = ref('')
const videoSrc = ref('')
const posterUrl = ref('')
const loading = ref(false)
const fallbackCover = '/images/banner-1.jpg'

// 点赞相关
const isLiked = ref(false)
const likeLoading = ref(false)

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
    // 点赞 / 收藏数量：使用后端返回的 camelCase 字段
    likeCount.value = data.likeCount || 0
    favoriteCount.value = data.favoriteCount || 0
    uploadTime.value = formatUploadTime(data.createTime || data.uploadDate)
    videoSrc.value = data.playUrl || ''
    posterUrl.value = data.coverUrl || fallbackCover
    // 视频描述：现在完全使用数据库中的 description 字段
    description.value = data.description || ''
    tags.value = [
      data.sourceFile || '本地视频',
      data.storagePath || '',
    ].filter(Boolean)
    
    // 更新右侧边栏作者信息
    uploader.value = {
      id: data.uploaderId || null,
      name: data.uploaderName || '用户上传',
      avatar: data.uploaderAvatar ? normalizeAvatarUrl(data.uploaderAvatar) : '',
      videoCount: 0, // 后续可以从API获取
      fans: '0' // 后续可以从API获取
    }
    
    // 如果有关注者ID，获取统计信息
    if (uploader.value.id) {
      try {
        const statsResponse = await getUserStats(uploader.value.id)
        if (statsResponse.data && statsResponse.data.success) {
          const stats = statsResponse.data.stats
          uploader.value.videoCount = stats.videoCount || 0
          uploader.value.fans = formatCount(stats.followerCount || 0)
        }
      } catch (error) {
        console.warn('获取用户统计信息失败:', error)
      }
    }
    
    // 点赞数
    try {
      const likeResp = await getLikeCount(videoId)
      if (likeResp.data && likeResp.data.success) {
        likeCount.value = likeResp.data.likeCount || 0
      }
    } catch (error) {
      console.warn('获取点赞数失败:', error)
    }

    // 检查收藏 / 点赞 / 关注状态
    if (userStore.isAuthenticated) {
      await checkFavoriteStatus(videoId)
      await checkLikeStatus(videoId)
      if (uploader.value.id) {
        await checkFollowStatus(uploader.value.id)
      }
    }

    // 加载评论
    await loadComments(true)

    // 加载相关推荐（同UP其他视频）
    await loadRecommends()
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
        // 本地同步减一，避免与数据库显示不一致
        favoriteCount.value = Math.max(0, (favoriteCount.value || 0) - 1)
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error(data.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const { data } = await addFavorite(userId, videoId)
      if (data.success) {
        isFavorited.value = true
        // 只有真正新增收藏时才加一：后端当“已收藏过该视频”时 success=false
        favoriteCount.value = (favoriteCount.value || 0) + 1
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
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(20)
const commentTotal = ref(0)
const loadingComments = ref(false)
const submittingComment = ref(false)
const activeCommentSort = ref('time')
const replyTarget = ref(null) // 当前正在回复的评论
const replyText = ref('')
const replyCache = ref({}) // key: commentId, value: replies 数组
const replyPlaceholderName = ref('') // 占位提示中的 @名字

// 右侧边栏作者信息
const uploader = ref({
  id: null,
  name: '用户上传',
  videoCount: 0,
  fans: '0',
  avatar: ''
})

// 关注状态
const isFollowing = ref(false)
const followLoading = ref(false)

// 相关推荐
const allRecommends = ref([])
const recommends = ref([])
const recommendsExpanded = ref(false)

const showExpandBtn = computed(
  () => allRecommends.value.length > INITIAL_RECOMMENDS
)

// 规范化头像 URL
const normalizeAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return url
  }
  return '/' + url
}

// 格式化数字
const formatCount = (count) => {
  if (!count || count === 0) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

// 检查关注状态
const checkFollowStatus = async (followingId) => {
  if (!userStore.isAuthenticated || !followingId) return
  
  try {
    const { data } = await checkFollow(followingId)
    if (data && data.success) {
      isFollowing.value = data.isFollowing || false
    }
  } catch (error) {
    console.warn('检查关注状态失败:', error)
  }
}

// 检查点赞状态
const checkLikeStatus = async (videoId) => {
  if (!userStore.isAuthenticated || !videoId) return

  try {
    const { data } = await checkLike(videoId)
    if (data && data.success) {
      isLiked.value = data.isLiked || false
      if (typeof data.likeCount === 'number') {
        likeCount.value = data.likeCount
      }
    }
  } catch (error) {
    console.warn('检查点赞状态失败:', error)
  }
}

const formatUploadTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  if (Number.isNaN(d.getTime())) {
    return timeStr
  }
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${mi}:${s}`
}

// 切换点赞
const toggleLike = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  likeLoading.value = true
  try {
    if (isLiked.value) {
      const { data } = await unlikeVideo(videoId)
      if (data && data.success) {
        isLiked.value = false
        if (typeof data.likeCount === 'number') likeCount.value = data.likeCount
        ElMessage.success('已取消点赞')
      } else {
        ElMessage.warning(data?.message || '取消点赞失败')
      }
    } else {
      const { data } = await likeVideo(videoId)
      if (data && data.success) {
        isLiked.value = true
        if (typeof data.likeCount === 'number') likeCount.value = data.likeCount
        ElMessage.success('点赞成功')
      } else {
        if (typeof data.likeCount === 'number') likeCount.value = data.likeCount
        ElMessage.warning(data?.message || '已点赞过该视频')
      }
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    likeLoading.value = false
  }
}

// 切换关注状态
const toggleFollow = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!uploader.value.id) {
    ElMessage.warning('无法获取作者信息')
    return
  }
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      // 取消关注
      const { data } = await unfollowUser(uploader.value.id)
      if (data && data.success) {
        isFollowing.value = false
        ElMessage.success('已取消关注')
        // 更新粉丝数
        if (uploader.value.fans !== '0') {
          const currentFans = parseFloat(uploader.value.fans) || 0
          uploader.value.fans = formatCount(Math.max(0, currentFans - 1))
        }
      } else {
        ElMessage.error(data?.message || '取消关注失败')
      }
    } else {
      // 关注
      const { data } = await followUser(uploader.value.id)
      if (data && data.success) {
        isFollowing.value = true
        ElMessage.success('关注成功')
        // 更新粉丝数
        const currentFans = parseFloat(uploader.value.fans) || 0
        uploader.value.fans = formatCount(currentFans + 1)
      } else {
        ElMessage.warning(data?.message || '关注失败')
      }
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const refreshVisibleRecommends = () => {
  if (recommendsExpanded.value) {
    recommends.value = allRecommends.value
  } else {
    recommends.value = allRecommends.value.slice(0, INITIAL_RECOMMENDS)
  }
}

const loadRecommends = async () => {
  const uploaderId = uploader.value.id
  const currentVideoId = videoData.value.videoId || route.params.id
  if (!uploaderId) {
    allRecommends.value = []
    recommends.value = []
    return
  }
  try {
    const { data } = await fetchVideosByUploader(uploaderId, MAX_RECOMMENDS, currentVideoId)
    const list = Array.isArray(data?.list) ? data.list : []
    const mapped = list.map(item => ({
      id: item.videoId || item.id,
      videoId: item.videoId || item.id,
      title: item.title || '本地视频',
      duration: formatDuration(item.duration),
      plays: item.viewCount ? formatCount(item.viewCount) : '本地视频',
      cover: (item.coverUrl && item.coverUrl.trim()) ? item.coverUrl : fallbackCover
    }))
    allRecommends.value = mapped.slice(0, MAX_RECOMMENDS)
    recommendsExpanded.value = false
    refreshVisibleRecommends()
  } catch (error) {
    console.warn('加载相关推荐失败:', error)
    allRecommends.value = []
    recommends.value = []
  }
}

const openRecommend = (rec) => {
  if (!rec || !rec.videoId) return
  const id = rec.videoId
  router.push({ path: `/video/${encodeURIComponent(id)}` })
}

const toggleRecommends = () => {
  if (!showExpandBtn.value) return
  recommendsExpanded.value = !recommendsExpanded.value
  refreshVisibleRecommends()
}

// 加载评论列表
       const loadComments = async (reset = true) => {
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  if (reset) {
    commentPage.value = 1
  }

  loadingComments.value = true
  try {
    const { data } = await getComments(
      videoId,
      commentPage.value,
      commentPageSize.value,
      activeCommentSort.value
    )
    if (data && data.success) {
             const list = Array.isArray(data.list) ? data.list : []
      commentTotal.value = data.total ?? list.length
      const cache = replyCache.value || {}
      const mapped = list.map(item => ({
        id: item.id,
        name: item.username || '用户',
        time: item.createTime || '',
        text: item.content || '',
        likes: item.likeCount || 0,
        avatar: item.avatar ? normalizeAvatarUrl(item.avatar) : '',
        replies: cache[item.id] ? [...cache[item.id]] : [],
        raw: item
      }))

      // 如果是重置（切换排序或首次加载），直接用新列表替换；
      // 否则为“加载更多”时在末尾追加，避免页面跳动
             if (reset) {
               comments.value = mapped
             } else {
               comments.value = [...comments.value, ...mapped]
             }

             // 为当前页的每条评论加载 / 恢复回复列表
             mapped.forEach(c => {
               loadRepliesForComment(c.id)
             })
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

const hasMoreComments = computed(() => {
  return comments.value.length < commentTotal.value
})

const loadMoreComments = () => {
  if (loadingComments.value || !hasMoreComments.value) return
  commentPage.value += 1
  loadComments(false)
}

const setCommentSort = (type) => {
  if (activeCommentSort.value === type) return
  activeCommentSort.value = type
  // 重置并按新排序重新加载，只刷新评论区；
  // 不先清空列表，等待新数据回来后一次性替换，减少视觉跳动
  loadComments(true)
}

       // 为某条顶层评论加载 / 恢复回复列表
       const loadRepliesForComment = async (commentId) => {
         if (!commentId) return

         const cached = replyCache.value[commentId]
         const videoId = videoData.value.videoId || route.params.id
         if (!videoId) return

         // 如果已有缓存，直接挂回当前评论对象
         if (cached && Array.isArray(cached)) {
           const target = comments.value.find(c => c.id === commentId)
           if (target) {
             target.replies = [...cached]
           }
           return
         }

         try {
           const { data } = await getCommentReplies(videoId, commentId)
           if (data && data.success) {
             const list = Array.isArray(data.list) ? data.list : []
             const mapped = list.map(r => ({
               id: r.id,
               name: r.username || '用户',
               time: r.createTime || '',
               text: r.content || '',
               likes: r.likeCount || 0,
               avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
               raw: r
             }))
             const target = comments.value.find(c => c.id === commentId)
             if (target) {
               target.replies = mapped
             }
             replyCache.value[commentId] = mapped
           }
         } catch (e) {
           console.error('加载回复失败:', e)
         }
       }

       // 开始回复某条评论 / 某条子回复
       const startReply = async (comment, reply = null) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  replyTarget.value = comment
         replyPlaceholderName.value = reply && reply.name ? reply.name : (comment?.name || '')
  replyText.value = ''

         // 确保该评论的回复已加载
         if (comment && comment.id) {
           loadRepliesForComment(comment.id)
         }
}

const cancelReply = () => {
  replyTarget.value = null
  replyText.value = ''
         replyPlaceholderName.value = ''
}

// 发表评论
const submitComment = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  const content = commentText.value.trim()
  if (!content) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  submittingComment.value = true
  try {
    const { data } = await addComment(videoId, content)
    if (data && data.success && data.data) {
      const c = data.data
      comments.value.unshift({
        id: c.id,
        name: c.username || '我',
        time: c.createTime || '刚刚',
        text: c.content || content,
        likes: c.likeCount || 0,
        avatar: c.avatar ? normalizeAvatarUrl(c.avatar) : (userStore.user?.avatar ? normalizeAvatarUrl(userStore.user.avatar) : ''),
        replies: [],
        raw: c
      })
      commentTotal.value += 1
      commentText.value = ''
      ElMessage.success('评论成功')
    } else {
      ElMessage.error(data?.message || '评论失败，请稍后重试')
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('评论失败，请稍后重试')
  } finally {
    submittingComment.value = false
  }
}

// 点赞 / 取消点赞评论
const toggleCommentLike = async (comment) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!comment || !comment.id) return

  // 临时在本地记录是否已点赞
  const current = comments.value.find(c => c.id === comment.id)
  if (!current) return

  const hasLiked = current._liked === true
  try {
    if (hasLiked) {
      const { data } = await unlikeComment(comment.id)
      if (data && data.success) {
        current._liked = false
        if (typeof data.likeCount === 'number') {
          current.likes = data.likeCount
        } else {
          current.likes = Math.max(0, (current.likes || 0) - 1)
        }
      } else {
        ElMessage.warning(data?.message || '取消点赞失败')
      }
    } else {
      const { data } = await likeComment(comment.id)
      if (data && data.success) {
        current._liked = true
        if (typeof data.likeCount === 'number') {
          current.likes = data.likeCount
        } else {
          current.likes = (current.likes || 0) + 1
        }
      } else {
        if (typeof data?.likeCount === 'number') {
          current.likes = data.likeCount
        }
        ElMessage.warning(data?.message || '已点赞过该评论')
      }
    }
  } catch (error) {
    console.error('评论点赞失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 提交回复
const submitReply = async (parentComment) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  const content = replyText.value.trim()
  if (!content) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  submittingComment.value = true
  try {
    const { data } = await addComment(videoId, content, parentComment.id)
    if (data && data.success && data.data) {
      const r = data.data
      const replyItem = {
        id: r.id,
        name: r.username || '我',
        time: r.createTime || '刚刚',
        text: r.content || content,
        likes: r.likeCount || 0,
        avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : (userStore.user?.avatar ? normalizeAvatarUrl(userStore.user.avatar) : ''),
        raw: r
      }
      const target = comments.value.find(c => c.id === parentComment.id)
      if (target) {
        if (!Array.isArray(target.replies)) target.replies = []
        target.replies.push(replyItem)
      }
      // 更新缓存
      const old = replyCache.value[parentComment.id] || []
      replyCache.value[parentComment.id] = [...old, replyItem]
      replyText.value = ''
      replyTarget.value = null
      ElMessage.success('回复成功')
    } else {
      ElMessage.error(data?.message || '回复失败，请稍后重试')
    }
  } catch (e) {
    console.error('提交回复失败:', e)
    ElMessage.error('回复失败，请稍后重试')
  } finally {
    submittingComment.value = false
  }
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
.video-page-wrapper {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  min-height: 100vh;
  margin: 0 auto;
  background: #ffffff;
}

.header-wrapper {
  position: relative;
  height: 64px;
}

  .video-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  width: 86%;
  max-width: 1800px;
  margin: 0 auto;
  padding: 32px 16px 16px;

  .player-area {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .player {
      background: #000;
      // border-radius: 8px;
      overflow: hidden;
             position: relative;
             aspect-ratio: 16 / 9;

      .video {
        width: 100%;
               height: 100%;
        display: block;
        background: #000;
      }
    }

    .action-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 20px;
      padding: 10px 0 12px;
      border-bottom: 1px solid var(--line_regular, #E3E5E7);

      .action-left {
        display: flex;
        align-items: center;
        gap: 35px;
        min-width: 0;
      }

      .action-item {
        display: inline-flex;
        align-items: center;
        justify-content: flex-start;
        flex-direction: row;
        gap: 6px;
        min-width: 0;
        padding: 0;
        border-radius: 4px;
        border: none;
        background: transparent;
        cursor: pointer;
        user-select: none;
        color: #61666d;
        transition: color 0.15s ease;

        &:hover {
          color: #00a1d6;
        }

        &:disabled {
          cursor: not-allowed;
          opacity: 0.6;
        }

        &.is-active {
          color: #00a1d6;
        }

        .action-icon {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          font-size: 28px;
          flex-shrink: 0;
        }

        .action-num {
          font-size: 13px;
          line-height: 1;
          white-space: nowrap;
        }
      }

      .action-right {
        display: inline-flex;
        align-items: center;
        gap: 12px;
        flex-shrink: 0;
      }

      .action-link {
        background: transparent;
        border: none;
        cursor: pointer;
        color: #61666d;
        font-size: 13px;
        padding: 6px 8px;
        border-radius: 8px;
        transition: all 0.15s ease;

        &:hover {
          background: #f6f7f8;
          color: #00a1d6;
        }
      }

      .action-more {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        border: none;
        background: transparent;
        cursor: pointer;
        color: #61666d;
        font-size: 20px;
        line-height: 32px;
        text-align: center;
        transition: all 0.15s ease;

        &:hover {
          background: #f6f7f8;
          color: #00a1d6;
        }
      }
    }

    .video-meta {
      .title {
        margin: 0 0 4px;
        font-size: 20px;
        color: #18191c;
        font-weight: 500;
      }
      .meta-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;

        .meta-left {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
          font-size: 13px;
          color: #9499a0;

          .meta-item {
            display: inline-flex;
            align-items: center;
            gap: 4px;

            .el-icon {
              font-size: 14px;
            }
          }

          .upload-time {
            margin-left: 4px;
          }
        }

        .meta-right {
          display: inline-flex;
          align-items: center;
          gap: 8px;
        }
      }
    }

    .desc {
      background: #fff;
      border-radius: 8px;
      /* 让描述区左侧与视频左侧对齐：去掉左右内边距，仅保留上下间距 */
      padding: 12px 0;
      border-bottom: 1px solid var(--line_regular, #E3E5E7);
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
      padding: 16px 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .comment-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding-bottom: 12px;
        border-bottom: 1px solid #f1f2f3;

        .title {
          font-size: 16px;
          font-weight: 600;
          color: #18191c;
        }

        .sort-tabs {
          display: flex;
          align-items: center;
          gap: 16px;
          font-size: 13px;

          .sort-item {
            color: #61666d;
            cursor: pointer;
            position: relative;
            padding-bottom: 2px;

            &.is-active {
              color: #00a1d6;
              font-weight: 500;
            }
          }
        }
      }

      .comment-editor {
        display: flex;
        align-items: flex-start;
        margin-top: 8px;

        .editor-avatar {
          margin-right: 12px;

          img,
          .avatar-placeholder {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
            background: #e3e5e7;
          }
        }

        .editor-main {
          flex: 1 1 auto;

          .editor-input {
            width: 100%;

            :deep(.el-textarea__inner) {
              background-color: #f4f5f7;
              border-radius: 6px;
              border-color: transparent;
              padding: 8px 12px;

              &:focus {
                border-color: #00a1d6;
                background-color: #fff;
              }
            }
          }

          .editor-actions {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            margin-top: 8px;
            gap: 12px;

            .login-hint {
              font-size: 12px;
              color: #9499a0;
              margin-right: auto;
            }
          }
        }
      }

      .comment-list {
        margin-top: 8px;

        .no-comment {
          padding: 24px 0;
          text-align: center;
          color: #9499a0;
          font-size: 13px;
        }

        .comment-item {
          display: flex;
          gap: 12px;
          padding: 16px 0;
          border-bottom: 1px solid #f1f2f3;

          .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            flex: 0 0 40px;
            object-fit: cover;
            background: #e3e5e7;
          }

          .content {
            flex: 1 1 auto;

            .header {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 4px;

              .name {
                font-weight: 600;
                font-size: 13px;
                color: #18191c;
              }

              .time {
                color: #9499a0;
                font-size: 12px;
              }
            }

            .text {
              margin: 4px 0 6px;
              color: #18191c;
              font-size: 14px;
              line-height: 1.6;
            }

            .comment-footer {
              display: flex;
              align-items: center;
              justify-content: space-between;
              margin-top: 4px;

              .reply-summary {
                font-size: 12px;
                color: #9499a0;
              }

              .comment-actions {
                display: inline-flex;
                gap: 4px;

                :deep(.el-button) {
                  padding: 0 4px;
                  font-size: 12px;
                  color: #9499a0;

                  &:hover {
                    color: #00a1d6;
                  }
                }
              }
            }

            .reply-list {
              margin-top: 8px;
              padding-left: 48px;

              .reply-item {
                display: flex;
                gap: 8px;
                margin-bottom: 8px;

                .reply-avatar {
                  width: 24px;
                  height: 24px;
                  border-radius: 50%;
                }

                .reply-content {
                  .reply-header {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin-bottom: 2px;

                    .name {
                      font-size: 13px;
                      color: #18191c;
                    }

                    .time {
                      font-size: 12px;
                      color: #9499a0;
                    }
                  }

                  .text {
                    margin: 0;
                    font-size: 14px;
                    color: #222;
                  }
                }
              }
            }

            .reply-editor {
              margin-top: 8px;
              padding-left: 48px;

              .reply-actions {
                margin-top: 6px;
                display: flex;
                justify-content: flex-end;
                gap: 8px;
              }
            }
          }
        }

        .loading-more,
        .load-more {
          padding: 12px 0;
          text-align: center;
          font-size: 13px;
          color: #9499a0;
          cursor: default;
        }

        .load-more {
          cursor: pointer;

          &:hover {
            color: #00a1d6;
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
        object-fit: cover;
        position: relative;
        z-index: 99999;
      }
      
      .u-avatar-placeholder {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: #d8d8d8;
        flex-shrink: 0;
      }
      
      .u-info {
        min-width: 0;
        
        .u-name { 
          font-weight: 600; 
          color: #18191c;
          font-size: 14px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        .u-stats { 
          color: #61666d; 
          font-size: 12px; 
          margin-top: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
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
          .rec-title {
            font-size: 14px;
            color: #18191c;
            line-height: 1.4;
            display: -webkit-box;
            line-clamp: 2;
            -webkit-line-clamp: 2; /* WebKit 浏览器：最多两行 */
            -webkit-box-orient: vertical;
            overflow: hidden;
            word-break: break-all;
          }
          .rec-meta { color: #8a8a8a; display: flex; gap: 12px; margin-top: 6px; font-size: 12px; }
        }
      }
    }

    .recommend-expand {
      margin-top: 12px;
      padding: 8px 0;
      text-align: center;
      border-radius: 8px;
      border: 1px solid #e3e5e7;
      color: #18191c;
      background: #f7f7f8;
      cursor: pointer;
      font-size: 14px;
      user-select: none;

      &:hover {
        background: #f0f0f2;
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