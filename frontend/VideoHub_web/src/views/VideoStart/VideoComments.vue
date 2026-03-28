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
            <span class="name-with-badge">
              <span class="name">{{ item.name }}</span>
              <span class="icon-badges">
                <img class="level-badge" :src="levelIconUrl(item.level)" :alt="`LV${item.level ?? 0}`" />
                <img v-if="item.isUploader" class="up-badge" src="/assets/up_pb.svg" alt="UP主" />
              </span>
            </span>
          </div>
          <p class="text">{{ item.text }}</p>
          <div class="comment-meta-row">
            <span class="meta-time">{{ item.time }}</span>
            <div class="meta-actions">
              <button
                type="button"
                class="action-with-count"
                :class="{ 'is-active': item._liked }"
                aria-label="点赞"
                @click="toggleCommentLike(item)"
              >
                <svg class="action-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
                  <path
                    :d="item._liked ? COMMENT_LIKE_PATH_FILLED : COMMENT_LIKE_PATH_OUTLINE"
                    fill="currentColor"
                  />
                </svg>
                <span class="action-num">{{ item.likes ?? 0 }}</span>
              </button>
              <button
                type="button"
                class="action-with-count"
                :class="{ 'is-active': item._disliked }"
                aria-label="点踩"
                @click="toggleCommentDislike(item)"
              >
                <svg class="action-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
                  <path
                    :d="item._disliked ? COMMENT_DISLIKE_PATH_FILLED : COMMENT_DISLIKE_PATH_OUTLINE"
                    fill="currentColor"
                  />
                </svg>
                <span class="action-num">{{ item.dislikes ?? 0 }}</span>
              </button>
              <button type="button" class="text-action-btn" @click="startReply(item)">回复</button>
            </div>
          </div>

          <!-- 回复列表 -->
          <div v-if="item.replies && item.replies.length" class="reply-list">
            <div
              v-for="reply in visibleReplies(item)"
              :key="reply.id"
              class="reply-item"
            >
              <img :src="reply.avatar || '/images/default-avatar.png'" class="reply-avatar" />
              <div class="reply-content">
                <div class="reply-header">
                  <span class="name-with-badge">
                    <span class="name">{{ reply.name }}</span>
                    <span class="icon-badges">
                      <img class="level-badge" :src="levelIconUrl(reply.level)" :alt="`LV${reply.level ?? 0}`" />
                      <img v-if="reply.isUploader" class="up-badge" src="/assets/up_pb.svg" alt="UP主" />
                    </span>
                  </span>
                </div>
                <p class="text">{{ reply.text }}</p>
                <div class="comment-meta-row reply-meta-row">
                  <span class="meta-time">{{ reply.time }}</span>
                  <div class="meta-actions">
                    <button
                      type="button"
                      class="action-with-count"
                      :class="{ 'is-active': reply._liked }"
                      aria-label="点赞"
                      @click="toggleCommentLike(reply)"
                    >
                      <svg class="action-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
                        <path
                          :d="reply._liked ? COMMENT_LIKE_PATH_FILLED : COMMENT_LIKE_PATH_OUTLINE"
                          fill="currentColor"
                        />
                      </svg>
                      <span class="action-num">{{ reply.likes ?? 0 }}</span>
                    </button>
                    <button
                      type="button"
                      class="action-with-count"
                      :class="{ 'is-active': reply._disliked }"
                      aria-label="点踩"
                      @click="toggleCommentDislike(reply)"
                    >
                      <svg class="action-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
                        <path
                          :d="reply._disliked ? COMMENT_DISLIKE_PATH_FILLED : COMMENT_DISLIKE_PATH_OUTLINE"
                          fill="currentColor"
                        />
                      </svg>
                      <span class="action-num">{{ reply.dislikes ?? 0 }}</span>
                    </button>
                    <button type="button" class="text-action-btn" @click="startReply(item, reply)">回复</button>
                  </div>
                </div>
              </div>
            </div>
            <div
              v-if="shouldShowReplyExpand(item)"
              class="reply-expand-row"
              role="button"
              tabindex="0"
              @click="expandReplies(item)"
              @keydown.enter.prevent="expandReplies(item)"
            >
              共 {{ item.replies.length }} 条回复，<span class="reply-expand-link">点击查看</span>
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
  },
  uploaderId: {
    type: [Number, String],
    default: null
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
/** 顶层评论 id -> 是否已展开全部回复（>2 条时默认折叠） */
const repliesExpanded = ref({})

const toNumber = (v) => {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const normalizeLevel = (lv) => {
  const n = toNumber(lv)
  if (n < 0) return 0
  if (n > 6) return 6
  return n
}

const isUploaderUser = (uid) => {
  if (props.uploaderId === null || props.uploaderId === undefined || uid === null || uid === undefined) return false
  return String(uid) === String(props.uploaderId)
}

const levelIconUrl = (lv) => `/assets/level_${normalizeLevel(lv)}.svg`

/** 评论点赞 SVG：空心 / 实心（已赞） */
const COMMENT_LIKE_PATH_OUTLINE =
  'M9.283433333333333 2.0303066666666663C9.095466666666667 2.0083933333333333 8.921333333333333 2.09014 8.828166666666666 2.1991199999999997C8.424633333333333 2.6711333333333336 8.332133333333333 3.3649466666666665 8.029333333333334 3.9012466666666663C7.630633333333333 4.607453333333333 7.258833333333333 5.034486666666666 6.800866666666666 5.436006666666666C6.42382 5.7665733333333336 6.042199999999999 5.987959999999999 5.666666666666666 6.09112L5.666666666666666 13.1497C6.19062 13.1611 6.751966666666666 13.168333333333333 7.333333333333333 13.168333333333333C8.831233333333333 13.168333333333333 10.1019 13.120766666666665 10.958166666666665 13.076699999999999C11.565133333333332 13.045433333333332 12.091966666666666 12.7451 12.366466666666668 12.256733333333333C12.7516 11.571599999999998 13.2264 10.5669 13.514166666666664 9.3835C13.7823 8.2808 13.904599999999999 7.374333333333333 13.959466666666666 6.734999999999999C13.984933333333332 6.438646666666667 13.750433333333334 6.166686666666667 13.386666666666665 6.166686666666667L10.065133333333332 6.166686666666667C9.898433333333333 6.166686666666667 9.742666666666667 6.08362 9.649833333333333 5.945166666666666C9.536066666666667 5.775493333333333 9.560033333333333 5.5828533333333334 9.6312 5.403346666666666C9.783966666666666 5.013846666666666 9.983933333333333 4.432846666666666 10.062766666666667 3.90454C10.1406 3.3830066666666667 10.121599999999999 2.9639466666666667 9.917133333333332 2.57626C9.697399999999998 2.1596933333333332 9.448266666666665 2.0495266666666665 9.283433333333333 2.0303066666666663zM10.773433333333333 5.166686666666666L13.386666666666665 5.166686666666666C14.269133333333333 5.166686666666666 15.036999999999999 5.875273333333333 14.9558 6.8206C14.897 7.505533333333333 14.767199999999999 8.462733333333333 14.485833333333334 9.6198C14.170333333333334 10.917200000000001 13.6532 12.008466666666665 13.238166666666666 12.746766666666666C12.7729 13.574433333333333 11.910266666666667 14.029 11.009566666666666 14.075366666666667C10.14 14.120166666666666 8.851766666666666 14.168333333333333 7.333333333333333 14.168333333333333C5.862206666666666 14.168333333333333 4.51776 14.1231 3.565173333333333 14.079633333333334C2.4932333333333334 14.030733333333332 1.5939999999999999 13.234466666666666 1.4786599999999999 12.143466666666665C1.4028 11.426066666666665 1.3333333333333333 10.4978 1.3333333333333333 9.501666666666665C1.3333333333333333 8.588966666666666 1.3916466666666667 7.761233333333333 1.4598999999999998 7.104466666666667C1.5791666666666666 5.95696 2.5641 5.166686666666666 3.671693333333333 5.166686666666666L5.166666666666666 5.166686666666666C5.3793066666666665 5.166686666666666 5.709213333333333 5.063186666666667 6.141613333333333 4.68408C6.516733333333333 4.355193333333333 6.816366666666667 4.015666666666666 7.158533333333333 3.409613333333333C7.5023 2.8007333333333335 7.6041 2.0920066666666663 8.068066666666667 1.54932C8.372133333333332 1.1936466666666665 8.8718 0.9755333333333334 9.399233333333333 1.03704C9.949866666666665 1.10124 10.457733333333334 1.4577866666666666 10.801633333333331 2.109713333333333C11.148866666666665 2.767993333333333 11.143799999999999 3.4356599999999995 11.051833333333335 4.0520933333333335C10.993899999999998 4.44022 10.875366666666666 4.852359999999999 10.773433333333333 5.166686666666666zM4.666666666666666 13.122166666666667L4.666666666666666 6.166686666666667L3.671693333333333 6.166686666666667C3.029613333333333 6.166686666666667 2.5161533333333335 6.615046666666666 2.4545466666666664 7.207833333333333C2.3890599999999997 7.837933333333333 2.333333333333333 8.630433333333333 2.333333333333333 9.501666666666665C2.333333333333333 10.453433333333333 2.399833333333333 11.345266666666667 2.473113333333333 12.038333333333334C2.533993333333333 12.614133333333331 3.0083466666666667 13.053199999999999 3.6107466666666665 13.0807C3.9228066666666668 13.094899999999999 4.278173333333333 13.109333333333334 4.666666666666666 13.122166666666667z'
const COMMENT_LIKE_PATH_FILLED =
  'M13.545733333333333 5.166653333333333L10.511766666666666 5.166653333333333C10.658033333333332 4.851813333333333 10.821733333333334 4.440706666666666 10.880833333333332 4.044453333333333C10.923233333333332 3.760413333333333 10.927266666666664 3.412493333333333 10.893133333333333 3.0813333333333333C10.859833333333334 2.7581999999999995 10.784866666666666 2.3969066666666663 10.6352 2.1132133333333334C10.318299999999999 1.5124266666666666 9.882166666666667 1.09052 9.357366666666666 0.9881599999999999C8.799166666666666 0.8792866666666665 8.318566666666666 1.1633 8.030966666666666 1.59852C7.7904333333333335 1.9625133333333333 7.6966 2.26618 7.611066666666667 2.5431266666666668L7.608366666666666 2.5519066666666665C7.526133333333332 2.817973333333333 7.4452333333333325 3.07934 7.237266666666667 3.4476933333333335C6.895133333333334 4.053713333333333 6.615993333333333 4.36802 6.240833333333333 4.69694C6.046326666666666 4.867473333333333 5.84366 4.974753333333333 5.666666666666666 5.03686L5.666666666666666 14.149866666666664C6.190953333333333 14.161133333333334 6.752166666666666 14.168266666666668 7.333333333333333 14.168266666666668C8.896066666666666 14.168266666666668 10.214966666666665 14.117266666666666 11.084633333333333 14.071433333333333C11.938133333333333 14.026433333333333 12.754100000000001 13.5962 13.1998 12.814466666666664C13.621066666666666 12.075666666666665 14.160633333333333 10.9572 14.485833333333334 9.619766666666667C14.7904 8.367233333333333 14.9174 7.348799999999999 14.968999999999998 6.656493333333334C15.032466666666666 5.8043733333333325 14.340166666666665 5.166653333333333 13.545733333333333 5.166653333333333zM4.666666666666666 14.122666666666667L4.666666666666666 5.166653333333333L3.5348733333333335 5.166653333333333C2.506193333333333 5.166653333333333 1.591813333333333 5.90056 1.4747533333333334 6.9655C1.4003066666666666 7.642799999999999 1.3333333333333333 8.523499999999999 1.3333333333333333 9.5016C1.3333333333333333 10.559333333333333 1.4116666666666666 11.540700000000001 1.4928399999999997 12.274533333333332C1.6048399999999998 13.287066666666664 2.43944 14.026599999999998 3.4350066666666668 14.073566666666666C3.78952 14.0903 4.205413333333333 14.107633333333332 4.666666666666666 14.122666666666667z'

/** 评论点踩 SVG：空心 / 实心（已踩） */
const COMMENT_DISLIKE_PATH_OUTLINE =
  'M9.283433333333333 13.9697C9.095466666666667 13.9916 8.921333333333333 13.909866666666666 8.828166666666666 13.8009C8.424633333333333 13.328866666666666 8.332133333333333 12.635066666666667 8.029333333333334 12.098766666666666C7.630633333333333 11.392566666666667 7.258833333333333 10.9655 6.800866666666666 10.564C6.42382 10.233433333333332 6.042199999999999 10.012033333333333 5.666666666666666 9.908866666666666L5.666666666666666 2.8503266666666662C6.19062 2.83892 6.751966666666666 2.8316799999999995 7.333333333333333 2.8316799999999995C8.831233333333333 2.8316799999999995 10.1019 2.87922 10.958166666666665 2.923313333333333C11.565133333333332 2.9545733333333333 12.091966666666666 3.254906666666667 12.366466666666668 3.74326C12.7516 4.428373333333333 13.2264 5.4331 13.514166666666664 6.616486666666667C13.7823 7.719199999999999 13.904599999999999 8.625666666666666 13.959466666666666 9.265C13.984933333333332 9.561366666666666 13.750433333333334 9.833333333333332 13.386666666666665 9.833333333333332L10.065133333333332 9.833333333333332C9.898433333333333 9.833333333333332 9.742666666666667 9.9164 9.649833333333333 10.054833333333333C9.536066666666667 10.224499999999999 9.560033333333333 10.417133333333332 9.6312 10.596633333333333C9.783966666666666 10.986166666666666 9.983933333333333 11.567166666666667 10.062766666666667 12.095466666666667C10.1406 12.616966666666666 10.121599999999999 13.036066666666665 9.917133333333332 13.423766666666666C9.697399999999998 13.8403 9.448266666666665 13.950466666666665 9.283433333333333 13.9697zM10.773433333333333 10.833333333333332L13.386666666666665 10.833333333333332C14.269133333333333 10.833333333333332 15.036999999999999 10.124733333333332 14.9558 9.1794C14.897 8.494466666666666 14.767199999999999 7.537266666666666 14.485833333333334 6.380213333333334C14.170333333333334 5.08278 13.6532 3.991546666666667 13.238166666666666 3.25324C12.7729 2.425586666666667 11.910266666666667 1.9710199999999998 11.009566666666666 1.9246400000000001C10.14 1.8798599999999999 8.851766666666666 1.8316866666666665 7.333333333333333 1.8316866666666665C5.862206666666666 1.8316866666666665 4.51776 1.8769066666666667 3.565173333333333 1.9203599999999998C2.4932333333333334 1.969253333333333 1.5939999999999999 2.765553333333333 1.4786599999999999 3.856533333333333C1.4028 4.573953333333333 1.3333333333333333 5.502233333333333 1.3333333333333333 6.498353333333332C1.3333333333333333 7.411033333333333 1.3916466666666667 8.238766666666667 1.4598999999999998 8.895533333333333C1.5791666666666666 10.043033333333334 2.5641 10.833333333333332 3.671693333333333 10.833333333333332L5.166666666666666 10.833333333333332C5.3793066666666665 10.833333333333332 5.709213333333333 10.936833333333333 6.141613333333333 11.3159C6.516733333333333 11.644799999999998 6.816366666666667 11.984333333333334 7.158533333333333 12.590399999999999C7.5023 13.199266666666666 7.6041 13.907999999999998 8.068066666666667 14.4507C8.372133333333332 14.806333333333331 8.8718 15.024466666666665 9.399233333333333 14.962966666666667C9.949866666666665 14.898766666666667 10.457733333333334 14.542233333333332 10.801633333333331 13.8903C11.148866666666665 13.232 11.143799999999999 12.564333333333332 11.051833333333335 11.947933333333333C10.993899999999998 11.5598 10.875366666666666 11.147633333333333 10.773433333333333 10.833333333333332zM4.666666666666666 2.8778466666666667L4.666666666666666 9.833333333333332L3.671693333333333 9.833333333333332C3.029613333333333 9.833333333333332 2.5161533333333335 9.384966666666667 2.4545466666666664 8.792166666666667C2.3890599999999997 8.162066666666666 2.333333333333333 7.369566666666666 2.333333333333333 6.498353333333332C2.333333333333333 5.546566666666667 2.399833333333333 4.654719999999999 2.473113333333333 3.96168C2.533993333333333 3.385866666666667 3.0083466666666667 2.946793333333333 3.6107466666666665 2.91932C3.9228066666666668 2.9050866666666666 4.278173333333333 2.8906666666666667 4.666666666666666 2.8778466666666667z'
const COMMENT_DISLIKE_PATH_FILLED =
  'M13.545733333333333 10.831566666666667L10.511766666666666 10.831566666666667C10.658033333333332 11.1464 10.821733333333334 11.5575 10.880833333333332 11.953766666666667C10.923233333333332 12.2378 10.927266666666664 12.5857 10.893133333333333 12.916866666666666C10.859833333333334 13.240033333333333 10.784866666666666 13.601299999999998 10.6352 13.885C10.318299999999999 14.4858 9.882166666666667 14.9077 9.357366666666666 15.010033333333332C8.799166666666666 15.118933333333333 8.318566666666666 14.8349 8.030966666666666 14.399666666666665C7.7904333333333335 14.0357 7.6966 13.732033333333334 7.611066666666667 13.455099999999998L7.608366666666666 13.4463C7.526133333333332 13.180233333333334 7.4452333333333325 12.918866666666666 7.237266666666667 12.550533333333334C6.895133333333334 11.9445 6.615993333333333 11.630199999999999 6.240833333333333 11.3013C6.046326666666666 11.130733333333334 5.84366 11.023466666666666 5.666666666666666 10.961333333333332L5.666666666666666 1.8483599999999998C6.190953333333333 1.8370733333333331 6.752166666666666 1.8299333333333332 7.333333333333333 1.8299333333333332C8.896066666666666 1.8299333333333332 10.214966666666665 1.8809666666666667 11.084633333333333 1.9268C11.938133333333333 1.9717866666666666 12.754100000000001 2.4020266666666665 13.1998 3.183733333333333C13.621066666666666 3.92254 14.160633333333333 5.041013333333333 14.485833333333334 6.3784600000000005C14.7904 7.631 14.9174 8.649366666666666 14.968999999999998 9.341733333333334C15.032466666666666 10.193833333333332 14.340166666666665 10.831566666666667 13.545733333333333 10.831566666666667zM4.666666666666666 1.8755266666666666L4.666666666666666 10.831566666666667L3.5348733333333335 10.831566666666667C2.506193333333333 10.831566666666667 1.591813333333333 10.097666666666665 1.4747533333333334 9.032699999999998C1.4003066666666666 8.3554 1.3333333333333333 7.4746999999999995 1.3333333333333333 6.496593333333333C1.3333333333333333 5.438859999999999 1.4116666666666666 4.457486666666666 1.4928399999999997 3.7236666666666665C1.6048399999999998 2.711153333333333 2.43944 1.9716066666666665 3.4350066666666668 1.9246466666666664C3.78952 1.9079266666666665 4.205413333333333 1.8906066666666668 4.666666666666666 1.8755266666666666z'

/** 后端若返回 dislikeCount 则展示；否则为 0（点踩仅前端累加时可递增） */
const initialDislikes = (row) => {
  const n = Number(row?.dislikeCount)
  return Number.isFinite(n) && n >= 0 ? n : 0
}

const isRepliesExpanded = (commentId) => !!repliesExpanded.value[commentId]

const visibleReplies = (item) => {
  const list = item?.replies || []
  if (list.length <= 2) return list
  if (isRepliesExpanded(item.id)) return list
  return list.slice(0, 2)
}

const shouldShowReplyExpand = (item) => {
  const n = item?.replies?.length || 0
  return n > 2 && !isRepliesExpanded(item.id)
}

const expandReplies = (item) => {
  if (!item?.id) return
  repliesExpanded.value = { ...repliesExpanded.value, [item.id]: true }
}

/** 顶层或某条回复（同一套点赞/点踩状态对象） */
const findCommentNode = (commentId) => {
  if (commentId === undefined || commentId === null) return null
  for (const c of comments.value) {
    if (c.id === commentId) return c
    const replies = c.replies
    if (Array.isArray(replies)) {
      const r = replies.find(x => x.id === commentId)
      if (r) return r
    }
  }
  return null
}

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
        level: normalizeLevel(item.level),
        userId: item.userId,
        isUploader: isUploaderUser(item.userId),
        time: item.createTime || '',
        text: item.content || '',
        likes: item.likeCount || 0,
        dislikes: initialDislikes(item),
        avatar: item.avatar ? normalizeAvatarUrl(item.avatar) : '',
        replies: cache[item.id] ? [...cache[item.id]] : [],
        _liked: false,
        _disliked: false,
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
        level: normalizeLevel(r.level),
        userId: r.userId,
        isUploader: isUploaderUser(r.userId),
        time: r.createTime || '',
        text: r.content || '',
        likes: r.likeCount || 0,
        dislikes: initialDislikes(r),
        avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
        _liked: false,
        _disliked: false,
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
        level: normalizeLevel(c.level),
        userId: c.userId,
        isUploader: isUploaderUser(c.userId),
        time: c.createTime || '刚刚',
        text: c.content || content,
        likes: c.likeCount || 0,
        dislikes: initialDislikes(c),
        avatar: c.avatar ? normalizeAvatarUrl(c.avatar) : (userStore.user?.avatar ? normalizeAvatarUrl(userStore.user.avatar) : ''),
        replies: [],
        _liked: false,
        _disliked: false,
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
          level: normalizeLevel(r.level),
          userId: r.userId,
          isUploader: isUploaderUser(r.userId),
          time: r.createTime || '',
          text: r.content || content,
          likes: r.likeCount || 0,
          dislikes: initialDislikes(r),
          avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
          _liked: false,
          _disliked: false,
          raw: r
        }
        if (!Array.isArray(target.replies)) target.replies = []
        target.replies.push(replyItem)
      }

      const old = replyCache.value[parentComment.id] || []
      replyCache.value[parentComment.id] = [...old, {
        id: r.id,
        name: r.username || '用户',
        level: normalizeLevel(r.level),
        userId: r.userId,
        isUploader: isUploaderUser(r.userId),
        time: r.createTime || '',
        text: r.content || content,
        likes: r.likeCount || 0,
        dislikes: initialDislikes(r),
        avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
        _liked: false,
        _disliked: false,
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

  const current = findCommentNode(comment.id)
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
      const wasDisliked = current._disliked === true
      const { data } = await likeComment(comment.id)
      if (data && data.success) {
        current._liked = true
        if (wasDisliked) {
          current._disliked = false
          current.dislikes = Math.max(0, (current.dislikes || 0) - 1)
        }
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

const toggleCommentDislike = async (comment) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!comment || !comment.id) return

  const current = findCommentNode(comment.id)
  if (!current) return

  if (current._disliked) {
    current._disliked = false
    current.dislikes = Math.max(0, (current.dislikes || 0) - 1)
    return
  }

  try {
    if (current._liked) {
      const { data } = await unlikeComment(comment.id)
      if (!data?.success) {
        ElMessage.warning(data?.message || '操作失败')
        return
      }
      current._liked = false
      if (typeof data.likeCount === 'number') {
        current.likes = data.likeCount
      } else {
        current.likes = Math.max(0, (current.likes || 0) - 1)
      }
    }
    current._disliked = true
    current.dislikes = (current.dislikes || 0) + 1
  } catch (error) {
    console.error('评论点踩处理失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

watch(
  () => props.videoId,
  async () => {
    repliesExpanded.value = {}
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

          .name-with-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;

            .name {
              font-size: 13px;
              font-weight: 400;
              color: #fb7299;
            }

            .icon-badges {
              display: inline-flex;
              align-items: center;
              gap: 3px;
            }

            .level-badge {
              width: 30px;
              height: 30px;
              object-fit: contain;
              display: inline-block;
            }

            .up-badge {
              width: 24px;
              height: 24px;
              object-fit: contain;
              display: inline-block;
            }
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

        .comment-meta-row {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 12px 16px;
          margin-top: 2px;

          .meta-time {
            flex: 0 0 auto;
            font-size: 12px;
            color: #9499a0;
          }

          .meta-actions {
            display: inline-flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px 16px;
          }

          .action-with-count {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 0;
            margin: 0;
            border: none;
            background: transparent;
            cursor: pointer;
            color: #9499a0;
            line-height: 1;

            .action-svg {
              flex: 0 0 auto;
              width: 16px;
              height: 16px;
              display: block;
            }

            .action-num {
              font-size: 12px;
              color: #61666d;
              font-variant-numeric: tabular-nums;
            }

            &:hover,
            &:hover .action-num {
              color: #00a1d6;
            }

            &.is-active,
            &.is-active .action-num {
              color: #00a1d6;
            }
          }

          .text-action-btn {
            padding: 0 2px;
            margin: 0;
            border: none;
            background: transparent;
            cursor: pointer;
            font-size: 12px;
            color: #9499a0;

            &:hover {
              color: #00a1d6;
            }
          }
        }

        .reply-meta-row {
          margin-top: 4px;
        }

        .reply-list {
          margin-top: 8px;
          padding-left: 48px;

          .reply-expand-row {
            font-size: 12px;
            color: #9499a0;
            margin-top: 4px;
            margin-bottom: 4px;
            cursor: pointer;
            user-select: none;

            .reply-expand-link {
              color: inherit;

              &:hover {
                color: #00a1d6;
              }
            }
          }

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
              flex: 1 1 auto;
              min-width: 0;

              .reply-header {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 2px;

                .name-with-badge {
                  display: inline-flex;
                  align-items: center;
                  gap: 6px;

                  .name {
                    font-size: 13px;
                    font-weight: 200;
                    color: #fb7299;
                  }

                  .icon-badges {
                    display: inline-flex;
                    align-items: center;
                    gap: 3px;
                  }

                  .level-badge {
                    width: 30px;
                    height: 30px;
                    object-fit: contain;
                    display: inline-block;
                  }

                  .up-badge {
                    width: 24px;
                    height: 24px;
                    object-fit: contain;
                    display: inline-block;
                  }
                }
              }

              .text {
                margin: 0 0 4px;
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


