<template>
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
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getComments, addComment, likeComment, unlikeComment, getCommentReplies } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  videoId: {
    type: String,
    required: true
  }
})

const userStore = useUserStore()

const commentText = ref('')
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(20)
const commentTotal = ref(0)
const loadingComments = ref(false)
const submittingComment = ref(false)
const activeCommentSort = ref('time')
const replyTarget = ref(null)
const replyText = ref('')
const replyCache = ref({})
const replyPlaceholderName = ref('')

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

const loadComments = async (reset = true) => {
  const videoId = props.videoId
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

      if (reset) {
        comments.value = mapped
      } else {
        comments.value = [...comments.value, ...mapped]
      }

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

const hasMoreComments = computed(() => comments.value.length < commentTotal.value)

const loadMoreComments = () => {
  if (loadingComments.value || !hasMoreComments.value) return
  commentPage.value += 1
  loadComments(false)
}

const setCommentSort = (type) => {
  if (activeCommentSort.value === type) return
  activeCommentSort.value = type
  loadComments(true)
}

const loadRepliesForComment = async (commentId) => {
  if (!commentId) return

  const cached = replyCache.value[commentId]
  const videoId = props.videoId
  if (!videoId) return

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

const startReply = async (comment, reply = null) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  replyTarget.value = comment
  replyPlaceholderName.value = reply && reply.name ? reply.name : (comment?.name || '')
  replyText.value = ''

  if (comment && comment.id) {
    loadRepliesForComment(comment.id)
  }
}

const cancelReply = () => {
  replyTarget.value = null
  replyText.value = ''
  replyPlaceholderName.value = ''
}

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

  const videoId = props.videoId
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

const toggleCommentLike = async (comment) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!comment || !comment.id) return

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

watch(
  () => props.videoId,
  () => {
    if (props.videoId) {
      loadComments(true)
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">

</style>


