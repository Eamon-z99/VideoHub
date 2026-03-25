<template>
  <div class="comments">
    <div class="comment-header">
      <div class="left">
        <span class="title">评论 {{ visibleCommentTotalWithReplies }}</span>
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
import { getComments, addComment, likeComment, unlikeComment, getCommentReplies, getCommentCountWithReplies } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  videoId: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['comment-total-change'])

const userStore = useUserStore()

const commentText = ref('')
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(20)
const commentTotal = ref(0)
const commentTotalWithReplies = ref(0)
const loadingComments = ref(false)
const submittingComment = ref(false)
const activeCommentSort = ref('time')
const replyTarget = ref(null)
const replyText = ref('')
const replyCache = ref({})
const replyPlaceholderName = ref('')

// “评论数包含回复”展示口径：统计当前已加载的顶层评论 + 这些顶层评论下已加载的回复数
// 说明：top 分页仍然用 commentTotal（接口返回的顶层评论总数），这里用于顶部 meta/推荐同步显示。
const emitCommentTotalWithReplies = () => {
  emit('comment-total-change', commentTotalWithReplies.value)
}

const visibleCommentTotalWithReplies = computed(() => commentTotalWithReplies.value)

const loadCommentTotalWithReplies = async () => {
  const videoId = props.videoId
  if (!videoId) return

  try {
    const { data } = await getCommentCountWithReplies(videoId)
    const total = data?.total ?? 0
    commentTotalWithReplies.value = Number.isFinite(Number(total)) ? Number(total) : 0
    emitCommentTotalWithReplies()
  } catch (e) {
    // 如果接口失败，保持为 0（不影响顶层分页加载）
    console.warn('加载评论总数（含回复）失败:', e)
  }
}

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
      // 顶层评论数用于分页；“包含回复”的展示数由 replies 加载完成后再发出
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
      // 重新拉取“评论总数（含回复）”，避免口径与后端不一致
      await loadCommentTotalWithReplies()
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

// 提交回复（模板使用了 @click="submitReply(item)"）
const submitReply = async (parentComment) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!parentComment || !parentComment.id) return
  const videoId = props.videoId
  if (!videoId) return

  const content = replyText.value.trim()
  if (!content) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  submittingComment.value = true
  try {
    // 使用 addComment(videoId, content, parentId) 作为“回复”提交
    const { data } = await addComment(videoId, content, parentComment.id)
    if (data && data.success && data.data) {
      const r = data.data

      const target = comments.value.find(c => c.id === parentComment.id)
      if (target) {
        const replyItem = {
          id: r.id,
          name: r.username || '用户',
          time: r.createTime || '',
          text: r.content || content,
          likes: r.likeCount || 0,
          avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
          raw: r
        }
        if (!Array.isArray(target.replies)) target.replies = []
        target.replies.push(replyItem)
      }

      const old = replyCache.value[parentComment.id] || []
      replyCache.value[parentComment.id] = [...old, {
        id: r.id,
        name: r.username || '用户',
        time: r.createTime || '',
        text: r.content || content,
        likes: r.likeCount || 0,
        avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
        raw: r
      }]

      // 回复也计入“评论总数（含回复）”
      replyTarget.value = null
      replyText.value = ''
      replyPlaceholderName.value = ''
      ElMessage.success('回复成功')
      // 重新拉取“评论总数（含回复）”，避免口径与后端不一致
      await loadCommentTotalWithReplies()
    } else {
      ElMessage.error(data?.message || '回复失败，请稍后重试')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败，请稍后重试')
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
  async () => {
    if (props.videoId) {
      await loadCommentTotalWithReplies()
      await loadComments(true)
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.comments {
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
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
      }

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
</style>


