<template>
  <div ref="commentsRootRef" class="comments">
    <div class="comment-header">
      <h2 class="comment-title">评论</h2>
      <span class="title-count">{{ visibleCommentTotalWithReplies }}</span>
      <div class="sort-tabs">
        <span
          class="sort-item"
          :class="{ 'is-active': activeCommentSort === 'hot' }"
          @click="setCommentSort('hot')"
        >
          最热
        </span>
        <span class="sort-divider" aria-hidden="true">|</span>
        <span
          class="sort-item"
          :class="{ 'is-active': activeCommentSort === 'time' }"
          @click="setCommentSort('time')"
        >
          最新
        </span>
      </div>
    </div>

    <div ref="topEditorRef" class="comment-editor">
      <div class="editor-avatar">
        <template v-if="userStore.user?.avatar && !editorAvatarFailed">
          <img
            :src="normalizeAvatarUrl(userStore.user.avatar)"
            alt=""
            @error="editorAvatarFailed = true"
          />
        </template>
        <div v-else class="avatar-placeholder" aria-hidden="true" />
      </div>
      <div
        class="editor-main"
        ref="topEditorMainRef"
        @focusin="onTopEditorMainFocusIn"
        @focusout="onTopEditorMainFocusOut"
      >
        <div
          class="editor-composer"
          :class="{
            'is-expanded': commentMainComposerExpanded,
            'is-expanded-empty': commentMainComposerTextEmpty,
            'has-previews': commentDraftImages.length > 0
          }"
        >
          <el-input
            ref="topCommentInputRef"
            v-model="commentText"
            type="textarea"
            :autosize="shouldMainCommentTextareaAutosize ? commentMainTextareaAutosize : false"
            :input-style="mainCommentTextareaInputStyle"
            :placeholder="COMMENT_PLACEHOLDER"
            class="editor-input editor-input-fixed-h editor-input-in-composer"
          />
          <div v-if="commentDraftImages.length" class="editor-image-previews">
            <div
              v-for="img in commentDraftImages"
              :key="img.id"
              class="editor-image-chip"
            >
              <img :src="normalizeCommentImgUrl(img.url)" alt="" />
              <button
                type="button"
                class="editor-image-remove"
                aria-label="移除图片"
                @click="removeCommentDraftImage(img.id)"
              >
                ×
              </button>
            </div>
          </div>
        </div>
        <div
          v-show="commentMainComposerExpanded"
          class="editor-toolbar-row"
          @pointerdown.capture="onMainCommentToolbarPointerDown"
        >
          <div class="toolbar-tools">
            <el-popover
              v-model:visible="emojiPopTopVisible"
              placement="bottom-start"
              :width="288"
              trigger="click"
              popper-class="comment-emoji-popover"
              @show="onEmojiPopoverShow"
            >
              <template #reference>
                <button type="button" class="tool-icon-btn" title="表情" aria-label="表情">
                  <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                    <path d="M6.14978 9.5497C5.911519999999999 9.618533333333332 5.77148 9.876833333333334 5.8369800000000005 10.126666666666665C6.010859999999999 10.792133333333332 6.458333333333333 11.221066666666667 6.9891000000000005 11.3022C7.358466666666666 11.371699999999999 7.7259 11.253699999999998 8 10.9912C8.2289 11.203199999999999 8.517266666666666 11.320733333333333 8.822433333333333 11.322099999999999C9.428333333333333 11.324366666666666 9.968266666666667 10.8752 10.163466666666666 10.126666666666665C10.228933333333334 9.876833333333334 10.088899999999999 9.6185 9.850633333333333 9.549733333333332C9.612366666666667 9.480899999999998 9.3661 9.627666666666666 9.300633333333334 9.8775C9.194333333333333 10.287166666666666 8.959666666666667 10.39 8.8136 10.385066666666667C8.6771 10.381133333333333 8.507366666666666 10.289066666666667 8.426033333333333 10.021666666666667C8.366433333333333 9.828 8.1944 9.696733333333333 8.0002 9.696766666666665C7.805999999999999 9.696766666666665 7.634 9.828 7.574399999999999 10.0217C7.479933333333333 10.332566666666665 7.2652 10.411999999999999 7.106766666666666 10.376533333333333C6.978133333333333 10.348466666666667 6.791413333333333 10.229966666666666 6.699806666666666 9.877466666666667C6.640386666666666 9.650866666666666 6.432313333333333 9.5091 6.2164399999999995 9.536166666666666L6.14978 9.5497z" fill="currentColor" />
                    <path d="M4.17582 6.281926666666667C4.34018 6.060033333333333 4.653313333333333 6.013393333333333 4.875206666666666 6.177766666666667L6.575206666666666 7.437033333333333C6.709806666666666 7.536733333333332 6.7855 7.697166666666666 6.776933333333333 7.8644333333333325C6.768366666666666 8.031699999999999 6.676566666666666 8.183566666666666 6.5325 8.268966666666666L4.8325 9.276333333333334C4.5949333333333335 9.417133333333332 4.288226666666667 9.3387 4.147446666666666 9.101099999999999C4.0066733333333335 8.863533333333333 4.085133333333333 8.556833333333334 4.3226933333333335 8.416066666666666L5.37502 7.792466666666667L4.27998 6.981299999999999C4.058086666666666 6.81696 4.011446666666666 6.503826666666666 4.17582 6.281926666666667z" fill="currentColor" />
                    <path d="M11.8223 6.281926666666667C11.657933333333332 6.060033333333333 11.3448 6.013393333333333 11.122899999999998 6.177766666666667L9.422899999999998 7.437033333333333C9.288333333333332 7.536733333333332 9.212599999999998 7.697166666666666 9.221166666666665 7.8644333333333325C9.229766666666666 8.031699999999999 9.321533333333333 8.183566666666666 9.465633333333333 8.268966666666666L11.165633333333332 9.276333333333334C11.403166666666666 9.417133333333332 11.7099 9.3387 11.850666666666665 9.101099999999999C11.991433333333333 8.863533333333333 11.912966666666666 8.556833333333334 11.675433333333332 8.416066666666666L10.623099999999999 7.792466666666667L11.718133333333334 6.981299999999999C11.940033333333332 6.81696 11.986666666666666 6.503826666666666 11.8223 6.281926666666667z" fill="currentColor" />
                    <path d="M8.000233333333332 2.333333333333333C4.870613333333333 2.333333333333333 2.33356 4.870386666666667 2.33356 8C2.33356 11.129633333333333 4.870613333333333 13.666666666666666 8.000233333333332 13.666666666666666C11.129833333333332 13.666666666666666 13.6669 11.129633333333333 13.6669 8C13.6669 4.870386666666667 11.129833333333332 2.333333333333333 8.000233333333332 2.333333333333333zM1.3335533333333331 8C1.3335533333333331 4.318099999999999 4.318326666666667 1.3333333333333333 8.000233333333332 1.3333333333333333C11.6821 1.3333333333333333 14.6669 4.318099999999999 14.6669 8C14.6669 11.681866666666666 11.6821 14.666666666666666 8.000233333333332 14.666666666666666C4.318326666666667 14.666666666666666 1.3335533333333331 11.681866666666666 1.3335533333333331 8z" fill="currentColor" />
                  </svg>
                </button>
              </template>
              <div class="emoji-picker-grid">
                <button
                  v-for="(em, ei) in COMMENT_EMOJI_LIST"
                  :key="`${em}-${ei}`"
                  type="button"
                  class="emoji-picker-cell"
                  @click="pickEmoji('top', em)"
                >
                  {{ em }}
                </button>
              </div>
            </el-popover>
            <el-popover
              v-model:visible="mentionPopTopVisible"
              placement="bottom-start"
              :width="280"
              trigger="click"
              popper-class="comment-mention-popover"
              @show="onMentionPopoverShow"
            >
              <template #reference>
                <button type="button" class="tool-icon-btn" title="@好友" aria-label="插入艾特">
                  <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                    <path d="M7.571333333333333 5.642906666666666C6.5793133333333325 5.642906666666666 5.642753333333333 6.618966666666666 5.642753333333333 8.000066666666665C5.642753333333333 9.381133333333333 6.5793133333333325 10.357199999999999 7.571333333333333 10.357199999999999C8.563333333333333 10.357199999999999 9.499866666666666 9.381133333333333 9.499866666666666 8.000066666666665C9.499866666666666 6.618966666666666 8.563333333333333 5.642906666666666 7.571333333333333 5.642906666666666zM4.642753333333333 8.000066666666665C4.642753333333333 6.225226666666666 5.8808066666666665 4.642906666666667 7.571333333333333 4.642906666666667C9.261833333333332 4.642906666666667 10.499866666666666 6.225226666666666 10.499866666666666 8.000066666666665C10.499866666666666 9.774866666666666 9.261833333333332 11.357199999999999 7.571333333333333 11.357199999999999C5.8808066666666665 11.357199999999999 4.642753333333333 9.774866666666666 4.642753333333333 8.000066666666665z" fill="currentColor" />
                    <path d="M10.142933333333332 4.928546666666667C10.4191 4.928546666666667 10.642933333333332 5.152406666666666 10.642933333333332 5.428546666666667L10.642933333333332 9.2252C10.642933333333332 9.942633333333333 11.248999999999999 10.398666666666667 11.991566666666666 10.313733333333332C12.674966666666666 10.235533333333333 13.5227 9.639933333333332 13.621933333333333 8.034233333333333C13.639 7.7585999999999995 13.876199999999999 7.548966666666666 14.151833333333332 7.566033333333333C14.427433333333333 7.5830666666666655 14.637066666666666 7.8203 14.620033333333334 8.095933333333333C14.496433333333332 10.095066666666666 13.355166666666666 11.164233333333332 12.105233333333333 11.307233333333333C10.914533333333333 11.443466666666666 9.642933333333332 10.677233333333334 9.642933333333332 9.2252L9.642933333333332 5.428546666666667C9.642933333333332 5.152406666666666 9.866833333333332 4.928546666666667 10.142933333333332 4.928546666666667z" fill="currentColor" />
                    <path d="M8 2.375C4.8934 2.375 2.375 4.8934 2.375 8C2.375 11.1066 4.8934 13.625 8 13.625C9.132133333333332 13.625 10.184733333333334 13.291066666666666 11.066533333333332 12.716433333333331C11.2979 12.5657 11.607666666666667 12.631033333333331 11.758433333333333 12.862400000000001C11.909166666666666 13.093733333333333 11.843833333333333 13.403533333333332 11.612466666666666 13.5543C10.5732 14.231499999999999 9.3318 14.625 8 14.625C4.3411133333333325 14.625 1.375 11.6589 1.375 8C1.375 4.3411133333333325 4.3411133333333325 1.375 8 1.375C11.6589 1.375 14.625 4.3411133333333325 14.625 8C14.625 8.276133333333332 14.401133333333334 8.5 14.125 8.5C13.848866666666666 8.5 13.625 8.276133333333332 13.625 8C13.625 4.8934 11.1066 2.375 8 2.375z" fill="currentColor" />
                  </svg>
                </button>
              </template>
              <div class="mention-popover-body">
                <div class="mention-popover-title">我的关注</div>
                <div v-if="!userStore.isAuthenticated" class="mention-popover-hint">请先登录后再@好友</div>
                <div v-else-if="followingLoading" class="mention-popover-hint">加载中…</div>
                <div v-else-if="followingUsers.length === 0" class="mention-popover-hint">暂无关注</div>
                <div v-else class="mention-user-list">
                  <button
                    v-for="u in followingUsers"
                    :key="u.id"
                    type="button"
                    class="mention-user-row"
                    @click="pickMention('top', u)"
                  >
                    <img
                      class="mention-user-avatar"
                      :src="normalizeAvatarUrl(u.avatar) || '/images/default-avatar.png'"
                      alt=""
                    />
                    <div class="mention-user-meta">
                      <span class="mention-user-name">{{ followingDisplayName(u) }}</span>
                      <span v-if="u.followerCount != null && u.followerCount !== ''" class="mention-user-fans">{{ formatFollowerCount(u.followerCount) }}</span>
                    </div>
                  </button>
                </div>
              </div>
            </el-popover>
            <button
              type="button"
              class="tool-icon-btn"
              title="上传图片"
              aria-label="上传图片"
              :disabled="uploadingCommentImage"
              @click="openCommentImagePicker('top')"
            >
              <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                <path d="M5.666666666666666 5.648313333333332C5.2883466666666665 5.648313333333332 4.9816666666666665 5.9549933333333325 4.9816666666666665 6.333313333333333C4.9816666666666665 6.71164 5.2883466666666665 7.0183 5.666666666666666 7.0183C6.04498 7.0183 6.351666666666667 6.71164 6.351666666666667 6.333313333333333C6.351666666666667 5.9549933333333325 6.04498 5.648313333333332 5.666666666666666 5.648313333333332zM4.018333333333333 6.333313333333333C4.018333333333333 5.42296 4.75632 4.684973333333333 5.666666666666666 4.684973333333333C6.577013333333333 4.684973333333333 7.3149999999999995 5.42296 7.3149999999999995 6.333313333333333C7.3149999999999995 7.2436333333333325 6.577013333333333 7.981633333333333 5.666666666666666 7.981633333333333C4.75632 7.981633333333333 4.018333333333333 7.2436333333333325 4.018333333333333 6.333313333333333z" fill="currentColor" />
                <path d="M8 3.3317066666666664C6.321186666666667 3.3317066666666664 4.855333333333333 3.4171066666666663 3.820593333333333 3.5010199999999996C3.1014733333333333 3.5593333333333335 2.5440733333333334 4.108686666666666 2.48 4.821366666666666C2.4040466666666664 5.666206666666666 2.333333333333333 6.780333333333333 2.333333333333333 7.9984C2.333333333333333 9.216433333333333 2.4040466666666664 10.330533333333332 2.48 11.1754C2.5440733333333334 11.888066666666667 3.1014733333333333 12.437433333333333 3.820593333333333 12.495733333333334C4.855333333333333 12.579666666666665 6.321186666666667 12.665066666666664 8 12.665066666666664C9.678999999999998 12.665066666666664 11.144933333333334 12.5796 12.179733333333333 12.4957C12.898733333333332 12.437399999999998 13.456 11.8882 13.520066666666667 11.175699999999999C13.595999999999998 10.331233333333333 13.666666666666666 9.2173 13.666666666666666 7.9984C13.666666666666666 6.779433333333333 13.595999999999998 5.665519999999999 13.520066666666667 4.82104C13.456 4.10854 12.898733333333332 3.559353333333333 12.179733333333333 3.5010399999999997C11.144933333333334 3.4171266666666664 9.678999999999998 3.3317066666666664 8 3.3317066666666664zM3.7397666666666667 2.504293333333333C4.794879999999999 2.418733333333333 6.288386666666666 2.3317066666666664 8 2.3317066666666664C9.7118 2.3317066666666664 11.2054 2.4187466666666664 12.260533333333331 2.504313333333333C13.458733333333331 2.601493333333333 14.407866666666665 3.5282 14.516066666666667 4.731493333333333C14.593933333333332 5.597606666666666 14.666666666666666 6.742366666666666 14.666666666666666 7.9984C14.666666666666666 9.254366666666666 14.593933333333332 10.399133333333332 14.516066666666667 11.265266666666665C14.407866666666665 12.468533333333333 13.458733333333331 13.395266666666666 12.260533333333331 13.492433333333333C11.2054 13.578 9.7118 13.665066666666664 8 13.665066666666664C6.288386666666666 13.665066666666664 4.794879999999999 13.578 3.7397666666666667 13.492466666666667C2.541373333333333 13.395266666666666 1.5922066666666668 12.468333333333334 1.4840200000000001 11.264933333333333C1.4061199999999998 10.398466666666666 1.3333333333333333 9.253533333333333 1.3333333333333333 7.9984C1.3333333333333333 6.7432 1.4061199999999998 5.598253333333333 1.4840200000000001 4.731826666666667C1.5922066666666668 3.5284199999999997 2.541373333333333 2.6014733333333333 3.7397666666666667 2.504293333333333z" fill="currentColor" />
                <path d="M10.574933333333332 9.035466666666666C10.3016 8.762066666666666 9.858366666666665 8.762066666666666 9.584966666666666 9.035433333333334L8.230066666666666 10.390366666666665C7.660799999999999 10.959633333333333 6.737666666666667 10.959433333333333 6.16866 10.3898C5.943406666666666 10.164333333333332 5.5779733333333334 10.164233333333332 5.35258 10.3896L4.353533333333333 11.3885C4.15826 11.583733333333331 3.841673333333333 11.583733333333331 3.6464266666666667 11.388466666666666C3.451173333333333 11.1932 3.451193333333333 10.8766 3.646466666666666 10.681333333333331L4.645526666666666 9.682433333333332C5.261586666666666 9.066433333333332 6.260426666666666 9.066733333333332 6.876133333333333 9.683066666666665C7.0547 9.861799999999999 7.344333333333333 9.861899999999999 7.522966666666666 9.683266666666666L8.877833333333333 8.328333333333333C9.541766666666666 7.6644000000000005 10.618233333333333 7.664433333333333 11.282133333333334 8.328433333333333L12.3536 9.400066666666666C12.548833333333334 9.595333333333333 12.5488 9.9119 12.353499999999999 10.107166666666666C12.158233333333332 10.302399999999999 11.841666666666665 10.302399999999999 11.6464 10.107099999999999L10.574933333333332 9.035466666666666z" fill="currentColor" />
              </svg>
            </button>
          </div>
          <div class="toolbar-submit">
            <span v-if="!userStore.isAuthenticated" class="login-hint">请先登录再参与评论</span>
            <el-button
              type="primary"
              size="small"
              class="comment-toolbar-send-btn"
              :loading="submittingComment"
              @click="submitComment"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <input
      ref="commentImageInputRef"
      type="file"
      accept="image/jpeg,image/png,image/gif,image/webp"
      class="comment-image-input-hidden"
      @change="onCommentImageFileChange"
    />

    <div class="comment-list">
      <div v-if="comments.length === 0 && !loadingComments" class="no-comment">
        还没有评论，来抢沙发吧~
      </div>
      <div v-else v-for="item in comments" :key="item.id" class="comment-item">
        <img
          :src="item.avatar || '/images/default-avatar.png'"
          class="avatar"
          alt=""
          @error="onCommentListAvatarError"
        />
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
          <div class="comment-rich-text top-comment-text">
            <template v-for="(seg, si) in parseCommentSegments(item.text)" :key="`c-${item.id}-${si}`">
              <span v-if="seg.type === 'text'" class="comment-text-chunk">{{ seg.value }}</span>
              <router-link
                v-else-if="seg.type === 'mention'"
                class="comment-mention-link"
                :to="{ path: `/profile/${encodeURIComponent(String(seg.userId))}` }"
              >@{{ seg.name }}</router-link>
              <img
                v-else-if="seg.type === 'img' && isAllowedCommentImageUrl(seg.url)"
                :src="normalizeCommentImgUrl(seg.url)"
                :alt="seg.alt || '评论配图'"
                class="comment-inline-img"
              />
              <span v-else-if="seg.type === 'img'" class="comment-text-chunk comment-img-fallback">[图片]</span>
            </template>
          </div>
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
              <img
                :src="reply.avatar || '/images/default-avatar.png'"
                class="reply-avatar"
                alt=""
                @error="onCommentListAvatarError"
              />
              <div class="reply-content">
                <!-- 回复：昵称+等级+UP 与正文同一行连续排列（仅回复区；父评论仍为上下结构） -->
                <div class="reply-line">
                  <span class="name-with-badge">
                    <span class="name">{{ reply.name }}</span>
                    <span class="icon-badges">
                      <img class="level-badge" :src="levelIconUrl(reply.level)" :alt="`LV${reply.level ?? 0}`" />
                      <img v-if="reply.isUploader" class="up-badge" src="/assets/up_pb.svg" alt="UP主" />
                    </span>
                  </span>
                  <span v-if="reply.replyToUserId" class="reply-to-inline">
                    回复
                    <router-link
                      class="reply-to-user-link"
                      :to="{ path: `/profile/${encodeURIComponent(String(reply.replyToUserId))}` }"
                    >@{{ reply.replyToName || '用户' }}</router-link>：
                  </span>
                  <span class="comment-rich-text reply-inline-rich">
                    <template v-for="(seg, si) in parseCommentSegments(reply.text)" :key="`r-${reply.id}-${si}`">
                      <span v-if="seg.type === 'text'" class="comment-text-chunk">{{ seg.value }}</span>
                      <router-link
                        v-else-if="seg.type === 'mention'"
                        class="comment-mention-link"
                        :to="{ path: `/profile/${encodeURIComponent(String(seg.userId))}` }"
                      >@{{ seg.name }}</router-link>
                      <img
                        v-else-if="seg.type === 'img' && isAllowedCommentImageUrl(seg.url)"
                        :src="normalizeCommentImgUrl(seg.url)"
                        :alt="seg.alt || '评论配图'"
                        class="comment-inline-img"
                      />
                      <span v-else-if="seg.type === 'img'" class="comment-text-chunk comment-img-fallback">[图片]</span>
                    </template>
                  </span>
                </div>
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
            <div
              v-if="shouldShowReplyPager(item)"
              class="reply-pager-row"
            >
              <span class="reply-pager-text">共 {{ replyTotalPages(item) }} 页</span>
              <button
                v-if="replyCurrentPage(item) > 1"
                type="button"
                class="reply-pager-link"
                @click="goReplyPrevPage(item)"
              >
                上一页
              </button>
              <button
                v-for="p in replyPageRange(item)"
                :key="p"
                type="button"
                class="reply-pager-page"
                :class="{ 'is-active': p === replyCurrentPage(item) }"
                @click="setReplyPage(item, p)"
              >
                {{ p }}
              </button>
              <button
                v-if="replyCurrentPage(item) < replyTotalPages(item)"
                type="button"
                class="reply-pager-link"
                @click="goReplyNextPage(item)"
              >
                下一页
              </button>
              <button type="button" class="reply-pager-link" @click="collapseReplies(item)">收起</button>
            </div>
            <div
              v-else-if="shouldShowReplyCollapseOnly(item)"
              class="reply-pager-row reply-pager-row--collapse-only"
            >
              <button type="button" class="reply-pager-link" @click="collapseReplies(item)">收起</button>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div
            v-if="replyTarget && replyTarget.id === item.id"
            ref="replyEditorWrapperRef"
            class="reply-editor"
            @focusin="onReplyEditorFocusIn"
            @focusout="onReplyEditorFocusOut"
          >
            <div
              class="editor-composer reply-editor-composer"
              :class="{
                'is-expanded': replyComposerExpanded,
                'is-expanded-empty': replyComposerTextEmpty,
                'has-previews': replyDraftImages.length > 0
              }"
            >
              <el-input
                ref="replyCommentInputRef"
                v-model="replyText"
                type="textarea"
                :autosize="shouldReplyTextareaAutosize ? replyTextareaAutosize : false"
                :input-style="replyTextareaInputStyle"
                :placeholder="`回复 @${replyPlaceholderName || item.name}：`"
                class="editor-input-in-composer reply-composer-input"
              />
              <div v-if="replyDraftImages.length" class="editor-image-previews">
                <div
                  v-for="img in replyDraftImages"
                  :key="img.id"
                  class="editor-image-chip"
                >
                  <img :src="normalizeCommentImgUrl(img.url)" alt="" />
                  <button
                    type="button"
                    class="editor-image-remove"
                    aria-label="移除图片"
                    @click="removeReplyDraftImage(img.id)"
                  >
                    ×
                  </button>
                </div>
              </div>
            </div>
            <div
              v-show="replyComposerExpanded"
              class="reply-editor-toolbar"
              @pointerdown.capture="onReplyEditorToolbarPointerDown"
            >
              <el-popover
                v-model:visible="emojiPopReplyVisible"
                placement="bottom-start"
                :width="288"
                trigger="click"
                popper-class="comment-emoji-popover"
                @show="onEmojiPopoverShow"
              >
                <template #reference>
                  <button type="button" class="tool-icon-btn" title="表情" aria-label="表情">
                    <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                      <path d="M6.14978 9.5497C5.911519999999999 9.618533333333332 5.77148 9.876833333333334 5.8369800000000005 10.126666666666665C6.010859999999999 10.792133333333332 6.458333333333333 11.221066666666667 6.9891000000000005 11.3022C7.358466666666666 11.371699999999999 7.7259 11.253699999999998 8 10.9912C8.2289 11.203199999999999 8.517266666666666 11.320733333333333 8.822433333333333 11.322099999999999C9.428333333333333 11.324366666666666 9.968266666666667 10.8752 10.163466666666666 10.126666666666665C10.228933333333334 9.876833333333334 10.088899999999999 9.6185 9.850633333333333 9.549733333333332C9.612366666666667 9.480899999999998 9.3661 9.627666666666666 9.300633333333334 9.8775C9.194333333333333 10.287166666666666 8.959666666666667 10.39 8.8136 10.385066666666667C8.6771 10.381133333333333 8.507366666666666 10.289066666666667 8.426033333333333 10.021666666666667C8.366433333333333 9.828 8.1944 9.696733333333333 8.0002 9.696766666666665C7.805999999999999 9.696766666666665 7.634 9.828 7.574399999999999 10.0217C7.479933333333333 10.332566666666665 7.2652 10.411999999999999 7.106766666666666 10.376533333333333C6.978133333333333 10.348466666666667 6.791413333333333 10.229966666666666 6.699806666666666 9.877466666666667C6.640386666666666 9.650866666666666 6.432313333333333 9.5091 6.2164399999999995 9.536166666666666L6.14978 9.5497z" fill="currentColor" />
                      <path d="M4.17582 6.281926666666667C4.34018 6.060033333333333 4.653313333333333 6.013393333333333 4.875206666666666 6.177766666666667L6.575206666666666 7.437033333333333C6.709806666666666 7.536733333333332 6.7855 7.697166666666666 6.776933333333333 7.8644333333333325C6.768366666666666 8.031699999999999 6.676566666666666 8.183566666666666 6.5325 8.268966666666666L4.8325 9.276333333333334C4.5949333333333335 9.417133333333332 4.288226666666667 9.3387 4.147446666666666 9.101099999999999C4.0066733333333335 8.863533333333333 4.085133333333333 8.556833333333334 4.3226933333333335 8.416066666666666L5.37502 7.792466666666667L4.27998 6.981299999999999C4.058086666666666 6.81696 4.011446666666666 6.503826666666666 4.17582 6.281926666666667z" fill="currentColor" />
                      <path d="M11.8223 6.281926666666667C11.657933333333332 6.060033333333333 11.3448 6.013393333333333 11.122899999999998 6.177766666666667L9.422899999999998 7.437033333333333C9.288333333333332 7.536733333333332 9.212599999999998 7.697166666666666 9.221166666666665 7.8644333333333325C9.229766666666666 8.031699999999999 9.321533333333333 8.183566666666666 9.465633333333333 8.268966666666666L11.165633333333332 9.276333333333334C11.403166666666666 9.417133333333332 11.7099 9.3387 11.850666666666665 9.101099999999999C11.991433333333333 8.863533333333333 11.912966666666666 8.556833333333334 11.675433333333332 8.416066666666666L10.623099999999999 7.792466666666667L11.718133333333334 6.981299999999999C11.940033333333332 6.81696 11.986666666666666 6.503826666666666 11.8223 6.281926666666667z" fill="currentColor" />
                      <path d="M8.000233333333332 2.333333333333333C4.870613333333333 2.333333333333333 2.33356 4.870386666666667 2.33356 8C2.33356 11.129633333333333 4.870613333333333 13.666666666666666 8.000233333333332 13.666666666666666C11.129833333333332 13.666666666666666 13.6669 11.129633333333333 13.6669 8C13.6669 4.870386666666667 11.129833333333332 2.333333333333333 8.000233333333332 2.333333333333333zM1.3335533333333331 8C1.3335533333333331 4.318099999999999 4.318326666666667 1.3333333333333333 8.000233333333332 1.3333333333333333C11.6821 1.3333333333333333 14.6669 4.318099999999999 14.6669 8C14.6669 11.681866666666666 11.6821 14.666666666666666 8.000233333333332 14.666666666666666C4.318326666666667 14.666666666666666 1.3335533333333331 11.681866666666666 1.3335533333333331 8z" fill="currentColor" />
                    </svg>
                  </button>
                </template>
                <div class="emoji-picker-grid">
                  <button
                    v-for="(em, ei) in COMMENT_EMOJI_LIST"
                    :key="`r-${em}-${ei}`"
                    type="button"
                    class="emoji-picker-cell"
                    @click="pickEmoji('reply', em)"
                  >
                    {{ em }}
                  </button>
                </div>
              </el-popover>
              <el-popover
                v-model:visible="mentionPopReplyVisible"
                placement="bottom-start"
                :width="280"
                trigger="click"
                popper-class="comment-mention-popover"
                @show="onMentionPopoverShow"
              >
                <template #reference>
                  <button type="button" class="tool-icon-btn" title="@好友" aria-label="插入艾特">
                    <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                      <path d="M7.571333333333333 5.642906666666666C6.5793133333333325 5.642906666666666 5.642753333333333 6.618966666666666 5.642753333333333 8.000066666666665C5.642753333333333 9.381133333333333 6.5793133333333325 10.357199999999999 7.571333333333333 10.357199999999999C8.563333333333333 10.357199999999999 9.499866666666666 9.381133333333333 9.499866666666666 8.000066666666665C9.499866666666666 6.618966666666666 8.563333333333333 5.642906666666666 7.571333333333333 5.642906666666666zM4.642753333333333 8.000066666666665C4.642753333333333 6.225226666666666 5.8808066666666665 4.642906666666667 7.571333333333333 4.642906666666667C9.261833333333332 4.642906666666667 10.499866666666666 6.225226666666666 10.499866666666666 8.000066666666665C10.499866666666666 9.774866666666666 9.261833333333332 11.357199999999999 7.571333333333333 11.357199999999999C5.8808066666666665 11.357199999999999 4.642753333333333 9.774866666666666 4.642753333333333 8.000066666666665z" fill="currentColor" />
                      <path d="M10.142933333333332 4.928546666666667C10.4191 4.928546666666667 10.642933333333332 5.152406666666666 10.642933333333332 5.428546666666667L10.642933333333332 9.2252C10.642933333333332 9.942633333333333 11.248999999999999 10.398666666666667 11.991566666666666 10.313733333333332C12.674966666666666 10.235533333333333 13.5227 9.639933333333332 13.621933333333333 8.034233333333333C13.639 7.7585999999999995 13.876199999999999 7.548966666666666 14.151833333333332 7.566033333333333C14.427433333333333 7.5830666666666655 14.637066666666666 7.8203 14.620033333333334 8.095933333333333C14.496433333333332 10.095066666666666 13.355166666666666 11.164233333333332 12.105233333333333 11.307233333333333C10.914533333333333 11.443466666666666 9.642933333333332 10.677233333333334 9.642933333333332 9.2252L9.642933333333332 5.428546666666667C9.642933333333332 5.152406666666666 9.866833333333332 4.928546666666667 10.142933333333332 4.928546666666667z" fill="currentColor" />
                      <path d="M8 2.375C4.8934 2.375 2.375 4.8934 2.375 8C2.375 11.1066 4.8934 13.625 8 13.625C9.132133333333332 13.625 10.184733333333334 13.291066666666666 11.066533333333332 12.716433333333331C11.2979 12.5657 11.607666666666667 12.631033333333331 11.758433333333333 12.862400000000001C11.909166666666666 13.093733333333333 11.843833333333333 13.403533333333332 11.612466666666666 13.5543C10.5732 14.231499999999999 9.3318 14.625 8 14.625C4.3411133333333325 14.625 1.375 11.6589 1.375 8C1.375 4.3411133333333325 4.3411133333333325 1.375 8 1.375C11.6589 1.375 14.625 4.3411133333333325 14.625 8C14.625 8.276133333333332 14.401133333333334 8.5 14.125 8.5C13.848866666666666 8.5 13.625 8.276133333333332 13.625 8C13.625 4.8934 11.1066 2.375 8 2.375z" fill="currentColor" />
                    </svg>
                  </button>
                </template>
                <div class="mention-popover-body">
                  <div class="mention-popover-title">我的关注</div>
                  <div v-if="!userStore.isAuthenticated" class="mention-popover-hint">请先登录后再@好友</div>
                  <div v-else-if="followingLoading" class="mention-popover-hint">加载中…</div>
                  <div v-else-if="followingUsers.length === 0" class="mention-popover-hint">暂无关注</div>
                  <div v-else class="mention-user-list">
                    <button
                      v-for="u in followingUsers"
                      :key="u.id"
                      type="button"
                      class="mention-user-row"
                      @click="pickMention('reply', u)"
                    >
                      <img
                        class="mention-user-avatar"
                        :src="normalizeAvatarUrl(u.avatar) || '/images/default-avatar.png'"
                        alt=""
                      />
                      <div class="mention-user-meta">
                        <span class="mention-user-name">{{ followingDisplayName(u) }}</span>
                        <span v-if="u.followerCount != null && u.followerCount !== ''" class="mention-user-fans">{{ formatFollowerCount(u.followerCount) }}</span>
                      </div>
                    </button>
                  </div>
                </div>
              </el-popover>
              <button
                type="button"
                class="tool-icon-btn"
                title="上传图片"
                aria-label="上传图片"
                :disabled="uploadingCommentImage"
                @click="openCommentImagePicker('reply')"
              >
                <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                  <path d="M5.666666666666666 5.648313333333332C5.2883466666666665 5.648313333333332 4.9816666666666665 5.9549933333333325 4.9816666666666665 6.333313333333333C4.9816666666666665 6.71164 5.2883466666666665 7.0183 5.666666666666666 7.0183C6.04498 7.0183 6.351666666666667 6.71164 6.351666666666667 6.333313333333333C6.351666666666667 5.9549933333333325 6.04498 5.648313333333332 5.666666666666666 5.648313333333332zM4.018333333333333 6.333313333333333C4.018333333333333 5.42296 4.75632 4.684973333333333 5.666666666666666 4.684973333333333C6.577013333333333 4.684973333333333 7.3149999999999995 5.42296 7.3149999999999995 6.333313333333333C7.3149999999999995 7.2436333333333325 6.577013333333333 7.981633333333333 5.666666666666666 7.981633333333333C4.75632 7.981633333333333 4.018333333333333 7.2436333333333325 4.018333333333333 6.333313333333333z" fill="currentColor" />
                  <path d="M8 3.3317066666666664C6.321186666666667 3.3317066666666664 4.855333333333333 3.4171066666666663 3.820593333333333 3.5010199999999996C3.1014733333333333 3.5593333333333335 2.5440733333333334 4.108686666666666 2.48 4.821366666666666C2.4040466666666664 5.666206666666666 2.333333333333333 6.780333333333333 2.333333333333333 7.9984C2.333333333333333 9.216433333333333 2.4040466666666664 10.330533333333332 2.48 11.1754C2.5440733333333334 11.888066666666667 3.1014733333333333 12.437433333333333 3.820593333333333 12.495733333333334C4.855333333333333 12.579666666666665 6.321186666666667 12.665066666666664 8 12.665066666666664C9.678999999999998 12.665066666666664 11.144933333333334 12.5796 12.179733333333333 12.4957C12.898733333333332 12.437399999999998 13.456 11.8882 13.520066666666667 11.175699999999999C13.595999999999998 10.331233333333333 13.666666666666666 9.2173 13.666666666666666 7.9984C13.666666666666666 6.779433333333333 13.595999999999998 5.665519999999999 13.520066666666667 4.82104C13.456 4.10854 12.898733333333332 3.559353333333333 12.179733333333333 3.5010399999999997C11.144933333333334 3.4171266666666664 9.678999999999998 3.3317066666666664 8 3.3317066666666664zM3.7397666666666667 2.504293333333333C4.794879999999999 2.418733333333333 6.288386666666666 2.3317066666666664 8 2.3317066666666664C9.7118 2.3317066666666664 11.2054 2.4187466666666664 12.260533333333331 2.504313333333333C13.458733333333331 2.601493333333333 14.407866666666665 3.5282 14.516066666666667 4.731493333333333C14.593933333333332 5.597606666666666 14.666666666666666 6.742366666666666 14.666666666666666 7.9984C14.666666666666666 9.254366666666666 14.593933333333332 10.399133333333332 14.516066666666667 11.265266666666665C14.407866666666665 12.468533333333333 13.458733333333331 13.395266666666666 12.260533333333331 13.492433333333333C11.2054 13.578 9.7118 13.665066666666664 8 13.665066666666664C6.288386666666666 13.665066666666664 4.794879999999999 13.578 3.7397666666666667 13.492466666666667C2.541373333333333 13.395266666666666 1.5922066666666668 12.468333333333334 1.4840200000000001 11.264933333333333C1.4061199999999998 10.398466666666666 1.3333333333333333 9.253533333333333 1.3333333333333333 7.9984C1.3333333333333333 6.7432 1.4061199999999998 5.598253333333333 1.4840200000000001 4.731826666666667C1.5922066666666668 3.5284199999999997 2.541373333333333 2.6014733333333333 3.7397666666666667 2.504293333333333z" fill="currentColor" />
                  <path d="M10.574933333333332 9.035466666666666C10.3016 8.762066666666666 9.858366666666665 8.762066666666666 9.584966666666666 9.035433333333334L8.230066666666666 10.390366666666665C7.660799999999999 10.959633333333333 6.737666666666667 10.959433333333333 6.16866 10.3898C5.943406666666666 10.164333333333332 5.5779733333333334 10.164233333333332 5.35258 10.3896L4.353533333333333 11.3885C4.15826 11.583733333333331 3.841673333333333 11.583733333333331 3.6464266666666667 11.388466666666666C3.451173333333333 11.1932 3.451193333333333 10.8766 3.646466666666666 10.681333333333331L4.645526666666666 9.682433333333332C5.261586666666666 9.066433333333332 6.260426666666666 9.066733333333332 6.876133333333333 9.683066666666665C7.0547 9.861799999999999 7.344333333333333 9.861899999999999 7.522966666666666 9.683266666666666L8.877833333333333 8.328333333333333C9.541766666666666 7.6644000000000005 10.618233333333333 7.664433333333333 11.282133333333334 8.328433333333333L12.3536 9.400066666666666C12.548833333333334 9.595333333333333 12.5488 9.9119 12.353499999999999 10.107166666666666C12.158233333333332 10.302399999999999 11.841666666666665 10.302399999999999 11.6464 10.107099999999999L10.574933333333332 9.035466666666666z" fill="currentColor" />
                </svg>
              </button>
            </div>
            <div
              v-show="replyComposerExpanded"
              class="reply-actions"
              @pointerdown.capture="onReplyEditorToolbarPointerDown"
            >
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

    <div
      v-show="stickyEditorVisible && !replyTarget"
      class="comment-sticky-spacer"
      :style="{ height: `${commentStickySpacerPx}px` }"
      aria-hidden="true"
    />

    <teleport to="body">
      <div
        ref="commentEditorStickyRef"
        v-show="stickyEditorVisible && !replyTarget"
        class="comment-editor-sticky"
        :style="stickyBarStyle"
      >
        <div class="comment-editor-sticky-inner">
          <div class="editor-avatar">
            <template v-if="userStore.user?.avatar && !editorAvatarFailed">
              <img
                :src="normalizeAvatarUrl(userStore.user.avatar)"
                alt=""
                @error="editorAvatarFailed = true"
              />
            </template>
            <div v-else class="avatar-placeholder" aria-hidden="true" />
          </div>
          <div
            class="editor-main"
            ref="stickyEditorMainRef"
            @focusin="onStickyEditorMainFocusIn"
            @focusout="onStickyEditorMainFocusOut"
          >
            <div
              class="editor-composer"
              :class="{
                'is-expanded': commentMainComposerExpanded,
                'is-expanded-empty': commentMainComposerTextEmpty,
                'has-previews': commentDraftImages.length > 0
              }"
            >
              <el-input
                ref="stickyCommentInputRef"
                v-model="commentText"
                type="textarea"
                :autosize="shouldMainCommentTextareaAutosize ? commentMainTextareaAutosize : false"
                :input-style="mainCommentTextareaInputStyle"
                :placeholder="COMMENT_PLACEHOLDER"
                class="editor-input editor-input-fixed-h editor-input-in-composer"
              />
              <div v-if="commentDraftImages.length" class="editor-image-previews">
                <div
                  v-for="img in commentDraftImages"
                  :key="`s-${img.id}`"
                  class="editor-image-chip"
                >
                  <img :src="normalizeCommentImgUrl(img.url)" alt="" />
                  <button
                    type="button"
                    class="editor-image-remove"
                    aria-label="移除图片"
                    @click="removeCommentDraftImage(img.id)"
                  >
                    ×
                  </button>
                </div>
              </div>
            </div>
            <div
              v-show="commentMainComposerExpanded"
              class="editor-toolbar-row"
              @pointerdown.capture="onMainCommentToolbarPointerDown"
            >
              <div class="toolbar-tools">
                <el-popover
                  v-model:visible="emojiPopStickyVisible"
                  placement="top-start"
                  :width="288"
                  trigger="click"
                  popper-class="comment-emoji-popover"
                  @show="onEmojiPopoverShow"
                >
                  <template #reference>
                    <button type="button" class="tool-icon-btn" title="表情" aria-label="表情">
                      <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                        <path d="M6.14978 9.5497C5.911519999999999 9.618533333333332 5.77148 9.876833333333334 5.8369800000000005 10.126666666666665C6.010859999999999 10.792133333333332 6.458333333333333 11.221066666666667 6.9891000000000005 11.3022C7.358466666666666 11.371699999999999 7.7259 11.253699999999998 8 10.9912C8.2289 11.203199999999999 8.517266666666666 11.320733333333333 8.822433333333333 11.322099999999999C9.428333333333333 11.324366666666666 9.968266666666667 10.8752 10.163466666666666 10.126666666666665C10.228933333333334 9.876833333333334 10.088899999999999 9.6185 9.850633333333333 9.549733333333332C9.612366666666667 9.480899999999998 9.3661 9.627666666666666 9.300633333333334 9.8775C9.194333333333333 10.287166666666666 8.959666666666667 10.39 8.8136 10.385066666666667C8.6771 10.381133333333333 8.507366666666666 10.289066666666667 8.426033333333333 10.021666666666667C8.366433333333333 9.828 8.1944 9.696733333333333 8.0002 9.696766666666665C7.805999999999999 9.696766666666665 7.634 9.828 7.574399999999999 10.0217C7.479933333333333 10.332566666666665 7.2652 10.411999999999999 7.106766666666666 10.376533333333333C6.978133333333333 10.348466666666667 6.791413333333333 10.229966666666666 6.699806666666666 9.877466666666667C6.640386666666666 9.650866666666666 6.432313333333333 9.5091 6.2164399999999995 9.536166666666666L6.14978 9.5497z" fill="currentColor" />
                        <path d="M4.17582 6.281926666666667C4.34018 6.060033333333333 4.653313333333333 6.013393333333333 4.875206666666666 6.177766666666667L6.575206666666666 7.437033333333333C6.709806666666666 7.536733333333332 6.7855 7.697166666666666 6.776933333333333 7.8644333333333325C6.768366666666666 8.031699999999999 6.676566666666666 8.183566666666666 6.5325 8.268966666666666L4.8325 9.276333333333334C4.5949333333333335 9.417133333333332 4.288226666666667 9.3387 4.147446666666666 9.101099999999999C4.0066733333333335 8.863533333333333 4.085133333333333 8.556833333333334 4.3226933333333335 8.416066666666666L5.37502 7.792466666666667L4.27998 6.981299999999999C4.058086666666666 6.81696 4.011446666666666 6.503826666666666 4.17582 6.281926666666667z" fill="currentColor" />
                        <path d="M11.8223 6.281926666666667C11.657933333333332 6.060033333333333 11.3448 6.013393333333333 11.122899999999998 6.177766666666667L9.422899999999998 7.437033333333333C9.288333333333332 7.536733333333332 9.212599999999998 7.697166666666666 9.221166666666665 7.8644333333333325C9.229766666666666 8.031699999999999 9.321533333333333 8.183566666666666 9.465633333333333 8.268966666666666L11.165633333333332 9.276333333333334C11.403166666666666 9.417133333333332 11.7099 9.3387 11.850666666666665 9.101099999999999C11.991433333333333 8.863533333333333 11.912966666666666 8.556833333333334 11.675433333333332 8.416066666666666L10.623099999999999 7.792466666666667L11.718133333333334 6.981299999999999C11.940033333333332 6.81696 11.986666666666666 6.503826666666666 11.8223 6.281926666666667z" fill="currentColor" />
                        <path d="M8.000233333333332 2.333333333333333C4.870613333333333 2.333333333333333 2.33356 4.870386666666667 2.33356 8C2.33356 11.129633333333333 4.870613333333333 13.666666666666666 8.000233333333332 13.666666666666666C11.129833333333332 13.666666666666666 13.6669 11.129633333333333 13.6669 8C13.6669 4.870386666666667 11.129833333333332 2.333333333333333 8.000233333333332 2.333333333333333zM1.3335533333333331 8C1.3335533333333331 4.318099999999999 4.318326666666667 1.3333333333333333 8.000233333333332 1.3333333333333333C11.6821 1.3333333333333333 14.6669 4.318099999999999 14.6669 8C14.6669 11.681866666666666 11.6821 14.666666666666666 8.000233333333332 14.666666666666666C4.318326666666667 14.666666666666666 1.3335533333333331 11.681866666666666 1.3335533333333331 8z" fill="currentColor" />
                      </svg>
                    </button>
                  </template>
                  <div class="emoji-picker-grid">
                    <button
                      v-for="(em, ei) in COMMENT_EMOJI_LIST"
                      :key="`s-${em}-${ei}`"
                      type="button"
                      class="emoji-picker-cell"
                      @click="pickEmoji('sticky', em)"
                    >
                      {{ em }}
                    </button>
                  </div>
                </el-popover>
                <el-popover
                  v-model:visible="mentionPopStickyVisible"
                  placement="top-start"
                  :width="280"
                  trigger="click"
                  popper-class="comment-mention-popover"
                  @show="onMentionPopoverShow"
                >
                  <template #reference>
                    <button type="button" class="tool-icon-btn" title="@好友" aria-label="插入艾特">
                      <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                        <path d="M7.571333333333333 5.642906666666666C6.5793133333333325 5.642906666666666 5.642753333333333 6.618966666666666 5.642753333333333 8.000066666666665C5.642753333333333 9.381133333333333 6.5793133333333325 10.357199999999999 7.571333333333333 10.357199999999999C8.563333333333333 10.357199999999999 9.499866666666666 9.381133333333333 9.499866666666666 8.000066666666665C9.499866666666666 6.618966666666666 8.563333333333333 5.642906666666666 7.571333333333333 5.642906666666666zM4.642753333333333 8.000066666666665C4.642753333333333 6.225226666666666 5.8808066666666665 4.642906666666667 7.571333333333333 4.642906666666667C9.261833333333332 4.642906666666667 10.499866666666666 6.225226666666666 10.499866666666666 8.000066666666665C10.499866666666666 9.774866666666666 9.261833333333332 11.357199999999999 7.571333333333333 11.357199999999999C5.8808066666666665 11.357199999999999 4.642753333333333 9.774866666666666 4.642753333333333 8.000066666666665z" fill="currentColor" />
                        <path d="M10.142933333333332 4.928546666666667C10.4191 4.928546666666667 10.642933333333332 5.152406666666666 10.642933333333332 5.428546666666667L10.642933333333332 9.2252C10.642933333333332 9.942633333333333 11.248999999999999 10.398666666666667 11.991566666666666 10.313733333333332C12.674966666666666 10.235533333333333 13.5227 9.639933333333332 13.621933333333333 8.034233333333333C13.639 7.7585999999999995 13.876199999999999 7.548966666666666 14.151833333333332 7.566033333333333C14.427433333333333 7.5830666666666655 14.637066666666666 7.8203 14.620033333333334 8.095933333333333C14.496433333333332 10.095066666666666 13.355166666666666 11.164233333333332 12.105233333333333 11.307233333333333C10.914533333333333 11.443466666666666 9.642933333333332 10.677233333333334 9.642933333333332 9.2252L9.642933333333332 5.428546666666667C9.642933333333332 5.152406666666666 9.866833333333332 4.928546666666667 10.142933333333332 4.928546666666667z" fill="currentColor" />
                        <path d="M8 2.375C4.8934 2.375 2.375 4.8934 2.375 8C2.375 11.1066 4.8934 13.625 8 13.625C9.132133333333332 13.625 10.184733333333334 13.291066666666666 11.066533333333332 12.716433333333331C11.2979 12.5657 11.607666666666667 12.631033333333331 11.758433333333333 12.862400000000001C11.909166666666666 13.093733333333333 11.843833333333333 13.403533333333332 11.612466666666666 13.5543C10.5732 14.231499999999999 9.3318 14.625 8 14.625C4.3411133333333325 14.625 1.375 11.6589 1.375 8C1.375 4.3411133333333325 4.3411133333333325 1.375 8 1.375C11.6589 1.375 14.625 4.3411133333333325 14.625 8C14.625 8.276133333333332 14.401133333333334 8.5 14.125 8.5C13.848866666666666 8.5 13.625 8.276133333333332 13.625 8C13.625 4.8934 11.1066 2.375 8 2.375z" fill="currentColor" />
                      </svg>
                    </button>
                  </template>
                  <div class="mention-popover-body">
                    <div class="mention-popover-title">我的关注</div>
                    <div v-if="!userStore.isAuthenticated" class="mention-popover-hint">请先登录后再@好友</div>
                    <div v-else-if="followingLoading" class="mention-popover-hint">加载中…</div>
                    <div v-else-if="followingUsers.length === 0" class="mention-popover-hint">暂无关注</div>
                    <div v-else class="mention-user-list">
                      <button
                        v-for="u in followingUsers"
                        :key="u.id"
                        type="button"
                        class="mention-user-row"
                        @click="pickMention('sticky', u)"
                      >
                        <img
                          class="mention-user-avatar"
                          :src="normalizeAvatarUrl(u.avatar) || '/images/default-avatar.png'"
                          alt=""
                        />
                        <div class="mention-user-meta">
                          <span class="mention-user-name">{{ followingDisplayName(u) }}</span>
                          <span v-if="u.followerCount != null && u.followerCount !== ''" class="mention-user-fans">{{ formatFollowerCount(u.followerCount) }}</span>
                        </div>
                      </button>
                    </div>
                  </div>
                </el-popover>
                <button
                  type="button"
                  class="tool-icon-btn"
                  title="上传图片"
                  aria-label="上传图片"
                  :disabled="uploadingCommentImage"
                  @click="openCommentImagePicker('sticky')"
                >
                  <svg class="tool-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
                    <path d="M5.666666666666666 5.648313333333332C5.2883466666666665 5.648313333333332 4.9816666666666665 5.9549933333333325 4.9816666666666665 6.333313333333333C4.9816666666666665 6.71164 5.2883466666666665 7.0183 5.666666666666666 7.0183C6.04498 7.0183 6.351666666666667 6.71164 6.351666666666667 6.333313333333333C6.351666666666667 5.9549933333333325 6.04498 5.648313333333332 5.666666666666666 5.648313333333332zM4.018333333333333 6.333313333333333C4.018333333333333 5.42296 4.75632 4.684973333333333 5.666666666666666 4.684973333333333C6.577013333333333 4.684973333333333 7.3149999999999995 5.42296 7.3149999999999995 6.333313333333333C7.3149999999999995 7.2436333333333325 6.577013333333333 7.981633333333333 5.666666666666666 7.981633333333333C4.75632 7.981633333333333 4.018333333333333 7.2436333333333325 4.018333333333333 6.333313333333333z" fill="currentColor" />
                    <path d="M8 3.3317066666666664C6.321186666666667 3.3317066666666664 4.855333333333333 3.4171066666666663 3.820593333333333 3.5010199999999996C3.1014733333333333 3.5593333333333335 2.5440733333333334 4.108686666666666 2.48 4.821366666666666C2.4040466666666664 5.666206666666666 2.333333333333333 6.780333333333333 2.333333333333333 7.9984C2.333333333333333 9.216433333333333 2.4040466666666664 10.330533333333332 2.48 11.1754C2.5440733333333334 11.888066666666667 3.1014733333333333 12.437433333333333 3.820593333333333 12.495733333333334C4.855333333333333 12.579666666666665 6.321186666666667 12.665066666666664 8 12.665066666666664C9.678999999999998 12.665066666666664 11.144933333333334 12.5796 12.179733333333333 12.4957C12.898733333333332 12.437399999999998 13.456 11.8882 13.520066666666667 11.175699999999999C13.595999999999998 10.331233333333333 13.666666666666666 9.2173 13.666666666666666 7.9984C13.666666666666666 6.779433333333333 13.595999999999998 5.665519999999999 13.520066666666667 4.82104C13.456 4.10854 12.898733333333332 3.559353333333333 12.179733333333333 3.5010399999999997C11.144933333333334 3.4171266666666664 9.678999999999998 3.3317066666666664 8 3.3317066666666664zM3.7397666666666667 2.504293333333333C4.794879999999999 2.418733333333333 6.288386666666666 2.3317066666666664 8 2.3317066666666664C9.7118 2.3317066666666664 11.2054 2.4187466666666664 12.260533333333331 2.504313333333333C13.458733333333331 2.601493333333333 14.407866666666665 3.5282 14.516066666666667 4.731493333333333C14.593933333333332 5.597606666666666 14.666666666666666 6.742366666666666 14.666666666666666 7.9984C14.666666666666666 9.254366666666666 14.593933333333332 10.399133333333332 14.516066666666667 11.265266666666665C14.407866666666665 12.468533333333333 13.458733333333331 13.395266666666666 12.260533333333331 13.492433333333333C11.2054 13.578 9.7118 13.665066666666664 8 13.665066666666664C6.288386666666666 13.665066666666664 4.794879999999999 13.578 3.7397666666666667 13.492466666666667C2.541373333333333 13.395266666666666 1.5922066666666668 12.468333333333334 1.4840200000000001 11.264933333333333C1.4061199999999998 10.398466666666666 1.3333333333333333 9.253533333333333 1.3333333333333333 7.9984C1.3333333333333333 6.7432 1.4061199999999998 5.598253333333333 1.4840200000000001 4.731826666666667C1.5922066666666668 3.5284199999999997 2.541373333333333 2.6014733333333333 3.7397666666666667 2.504293333333333z" fill="currentColor" />
                    <path d="M10.574933333333332 9.035466666666666C10.3016 8.762066666666666 9.858366666666665 8.762066666666666 9.584966666666666 9.035433333333334L8.230066666666666 10.390366666666665C7.660799999999999 10.959633333333333 6.737666666666667 10.959433333333333 6.16866 10.3898C5.943406666666666 10.164333333333332 5.5779733333333334 10.164233333333332 5.35258 10.3896L4.353533333333333 11.3885C4.15826 11.583733333333331 3.841673333333333 11.583733333333331 3.6464266666666667 11.388466666666666C3.451173333333333 11.1932 3.451193333333333 10.8766 3.646466666666666 10.681333333333331L4.645526666666666 9.682433333333332C5.261586666666666 9.066433333333332 6.260426666666666 9.066733333333332 6.876133333333333 9.683066666666665C7.0547 9.861799999999999 7.344333333333333 9.861899999999999 7.522966666666666 9.683266666666666L8.877833333333333 8.328333333333333C9.541766666666666 7.6644000000000005 10.618233333333333 7.664433333333333 11.282133333333334 8.328433333333333L12.3536 9.400066666666666C12.548833333333334 9.595333333333333 12.5488 9.9119 12.353499999999999 10.107166666666666C12.158233333333332 10.302399999999999 11.841666666666665 10.302399999999999 11.6464 10.107099999999999L10.574933333333332 9.035466666666666z" fill="currentColor" />
                  </svg>
                </button>
              </div>
              <div class="toolbar-submit">
                <span v-if="!userStore.isAuthenticated" class="login-hint">请先登录再参与评论</span>
                <el-button
                  type="primary"
                  size="small"
                  class="comment-toolbar-send-btn"
                  :loading="submittingComment"
                  @click="submitComment"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getComments, addComment, likeComment, unlikeComment, getCommentReplies, getCommentCountWithReplies, uploadCommentImage } from '@/api/comment'
import { getFollowingUsers } from '@/api/follow'
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

/** 主评输入区头像：URL 存在但加载失败时显示灰色圆形占位 */
const editorAvatarFailed = ref(false)
watch(
  () => userStore.user?.avatar,
  () => {
    editorAvatarFailed.value = false
  }
)

const commentText = ref('')
/** 主评论草稿中的配图（上传成功后的 URL），提交时合并进 content，不在输入框里显示 Markdown */
const commentDraftImages = ref([])
/** 回复草稿配图 */
const replyDraftImages = ref([])
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
/** 展开后回复分页：每页条数 */
const REPLIES_PREVIEW = 2
const REPLIES_PAGE_SIZE = 10
/** 顶层评论 id -> 当前回复页（从 1 起） */
const replyPageByRoot = ref({})
/** 正在回复的「被引用回复」评论 id（对回复再回复时非空） */
const replyToCommentRef = ref(null)

const COMMENT_PLACEHOLDER = '下面我简单喵两句'

/** 评论区可用表情（Unicode，无需额外素材授权） */
const COMMENT_EMOJI_LIST = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊',
  '😍', '🥰', '😘', '😗', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭',
  '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
  '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤧', '😵', '🤯', '🥳',
  '🥺', '😭', '😡', '🤬', '👍', '👎', '👏', '🙏', '💪', '🔥', '✨', '❤️'
]

const commentsRootRef = ref(null)
const topEditorRef = ref(null)
const topCommentInputRef = ref(null)
const stickyCommentInputRef = ref(null)
const commentEditorStickyRef = ref(null)
const replyCommentInputRef = ref(null)
const commentImageInputRef = ref(null)
const commentImageTarget = ref('top')

const emojiPopTopVisible = ref(false)
const emojiPopStickyVisible = ref(false)
const emojiPopReplyVisible = ref(false)
const mentionPopTopVisible = ref(false)
const mentionPopStickyVisible = ref(false)
const mentionPopReplyVisible = ref(false)
const followingUsers = ref([])
const followingLoading = ref(false)
const uploadingCommentImage = ref(false)

const topEditorMainRef = ref(null)
const stickyEditorMainRef = ref(null)
const replyEditorWrapperRef = ref(null)

/** 顶部主评 editor-main 内是否有焦点（含工具条按钮） */
const commentTopMainFocusedInside = ref(false)
/** 底部固定栏 editor-main 内是否有焦点 */
const commentStickyMainFocusedInside = ref(false)
/** 回复框内是否有焦点 */
const replyEditorFocusedInside = ref(false)

/** 主评输入：有内容/草稿图/表情面板/焦点在输入区时展开为白底+约 6 行；否则灰底+单行高度 */
const commentMainComposerExpanded = computed(() => {
  const t = (commentText.value || '').trim()
  return (
    t.length > 0 ||
    commentDraftImages.value.length > 0 ||
    emojiPopTopVisible.value ||
    emojiPopStickyVisible.value ||
    mentionPopTopVisible.value ||
    mentionPopStickyVisible.value ||
    commentTopMainFocusedInside.value ||
    commentStickyMainFocusedInside.value
  )
})

/** 已展开但尚未输入任何字符：文本框保持与灰条相同的 50px 高（配图区另算） */
const commentMainComposerTextEmpty = computed(
  () => commentMainComposerExpanded.value && (commentText.value || '').length === 0
)

/** 有字符后才启用 Element Plus autosize，避免占位符参与撑高 */
const shouldMainCommentTextareaAutosize = computed(
  () => commentMainComposerExpanded.value && (commentText.value || '').length > 0
)

const commentMainTextareaAutosize = Object.freeze({ minRows: 1, maxRows: 6 })

const replyComposerTextEmpty = computed(
  () => replyComposerExpanded.value && (replyText.value || '').length === 0
)

const shouldReplyTextareaAutosize = computed(
  () => replyComposerExpanded.value && (replyText.value || '').length > 0
)

const replyTextareaAutosize = Object.freeze({ minRows: 1, maxRows: 6 })

const replyComposerExpanded = computed(() => {
  const t = (replyText.value || '').trim()
  return (
    t.length > 0 ||
    replyDraftImages.value.length > 0 ||
    emojiPopReplyVisible.value ||
    mentionPopReplyVisible.value ||
    replyEditorFocusedInside.value
  )
})

/** 与 EP textareaCalcStyle 合并，内联 maxHeight 确保封顶 */
const COMMENT_TEXTAREA_MAX = '115px'
const COMMENT_MAIN_IDLE_MAX = '50px'
const COMMENT_REPLY_IDLE_MAX = '48px'

const mainCommentTextareaInputStyle = computed(() => {
  if (!commentMainComposerExpanded.value) return {}
  if (commentMainComposerTextEmpty.value) return { maxHeight: COMMENT_MAIN_IDLE_MAX }
  return { maxHeight: COMMENT_TEXTAREA_MAX }
})

const replyTextareaInputStyle = computed(() => {
  if (!replyComposerExpanded.value) return {}
  if (replyComposerTextEmpty.value) return { maxHeight: COMMENT_REPLY_IDLE_MAX }
  return { maxHeight: COMMENT_TEXTAREA_MAX }
})

/** 点击工具行空白处时焦点会落到 body，focusout 误判为「离开输入区」；pointer 阶段先打标再判断 */
const mainCommentToolbarPointerGuard = ref(false)
const replyEditorToolbarPointerGuard = ref(false)

const onMainCommentToolbarPointerDown = () => {
  mainCommentToolbarPointerGuard.value = true
}

const onReplyEditorToolbarPointerDown = () => {
  replyEditorToolbarPointerGuard.value = true
}

const onTopEditorMainFocusIn = () => {
  commentTopMainFocusedInside.value = true
}
const onTopEditorMainFocusOut = (e) => {
  const related = e.relatedTarget
  if (related && topEditorMainRef.value?.contains(related)) {
    mainCommentToolbarPointerGuard.value = false
    return
  }
  nextTick(() => {
    if (mainCommentToolbarPointerGuard.value) {
      mainCommentToolbarPointerGuard.value = false
      commentTopMainFocusedInside.value = true
      return
    }
    if (!topEditorMainRef.value?.contains(document.activeElement)) {
      commentTopMainFocusedInside.value = false
    }
  })
}

const onStickyEditorMainFocusIn = () => {
  commentStickyMainFocusedInside.value = true
}
const onStickyEditorMainFocusOut = (e) => {
  const related = e.relatedTarget
  if (related && stickyEditorMainRef.value?.contains(related)) {
    mainCommentToolbarPointerGuard.value = false
    return
  }
  nextTick(() => {
    if (mainCommentToolbarPointerGuard.value) {
      mainCommentToolbarPointerGuard.value = false
      commentStickyMainFocusedInside.value = true
      return
    }
    if (!stickyEditorMainRef.value?.contains(document.activeElement)) {
      commentStickyMainFocusedInside.value = false
    }
  })
}

const onReplyEditorFocusIn = () => {
  replyEditorFocusedInside.value = true
}
const onReplyEditorFocusOut = (e) => {
  const related = e.relatedTarget
  if (related && replyEditorWrapperRef.value?.contains(related)) {
    replyEditorToolbarPointerGuard.value = false
    return
  }
  nextTick(() => {
    if (replyEditorToolbarPointerGuard.value) {
      replyEditorToolbarPointerGuard.value = false
      replyEditorFocusedInside.value = true
      return
    }
    if (!replyEditorWrapperRef.value?.contains(document.activeElement)) {
      replyEditorFocusedInside.value = false
    }
  })
}

/** 底栏占位：测量 teleport 条实际高度（随输入增高而变） */
const commentStickySpacerPx = ref(108)
let stickySpacerResizeObserver = null

const syncMainCommentTextareaSize = () => {
  if (typeof document === 'undefined') return
  nextTick(() => {
    nextTick(() => {
      topCommentInputRef.value?.resizeTextarea?.()
      stickyCommentInputRef.value?.resizeTextarea?.()
      replyCommentInputRef.value?.resizeTextarea?.()
      updateStickySpacerFromStickyBar()
    })
  })
}

const updateStickySpacerFromStickyBar = () => {
  const el = commentEditorStickyRef.value
  if (!el || !stickyEditorVisible.value || replyTarget.value) return
  const h = el.getBoundingClientRect().height
  if (h > 0) commentStickySpacerPx.value = Math.ceil(h)
}

const bindStickySpacerResizeObserver = () => {
  stickySpacerResizeObserver?.disconnect()
  stickySpacerResizeObserver = null
  const el = commentEditorStickyRef.value
  if (!el || typeof ResizeObserver === 'undefined') return
  stickySpacerResizeObserver = new ResizeObserver(() => updateStickySpacerFromStickyBar())
  stickySpacerResizeObserver.observe(el)
}
/** 底栏与左侧评论区同宽、同水平位置，不盖住右侧推荐栏 */
const stickyBarStyle = ref({})
const stickyEditorVisible = ref(false)
/** 避免首屏顶部输入框在视口外时误显示底栏 */
const commentEditorEverVisible = ref(false)
let topEditorObserver = null
let stickyBarResizeObserver = null
let stickyLayoutRaf = 0
let stickyBarScrollHandler = null

const measureStickyBarLayout = () => {
  const root = commentsRootRef.value
  if (!root) return
  const r = root.getBoundingClientRect()
  const vw = typeof window !== 'undefined' ? window.innerWidth : r.width
  const left = Math.max(0, Math.min(r.left, vw - 200))
  const width = Math.max(200, Math.min(r.width, vw - left))
  stickyBarStyle.value = {
    left: `${left}px`,
    width: `${width}px`,
    right: 'auto',
    maxWidth: 'none'
  }
}

const scheduleStickyBarLayout = () => {
  if (stickyLayoutRaf) return
  stickyLayoutRaf = requestAnimationFrame(() => {
    stickyLayoutRaf = 0
    measureStickyBarLayout()
  })
}

watch(stickyEditorVisible, (visible) => {
  if (visible) {
    nextTick(() => {
      scheduleStickyBarLayout()
      bindStickySpacerResizeObserver()
      syncMainCommentTextareaSize()
    })
  } else {
    stickySpacerResizeObserver?.disconnect()
    stickySpacerResizeObserver = null
  }
})

watch(commentMainComposerExpanded, () => syncMainCommentTextareaSize())

watch(commentText, () => syncMainCommentTextareaSize())

watch(
  () => commentDraftImages.value.length,
  () => syncMainCommentTextareaSize()
)

watch(replyComposerExpanded, () => syncMainCommentTextareaSize())

watch(replyText, () => syncMainCommentTextareaSize())

watch(
  () => replyDraftImages.value.length,
  () => syncMainCommentTextareaSize()
)

watch(replyTarget, () => {
  nextTick(() => {
    bindStickySpacerResizeObserver()
    syncMainCommentTextareaSize()
    updateStickySpacerFromStickyBar()
  })
})

onMounted(() => {
  stickyBarScrollHandler = () => scheduleStickyBarLayout()
  window.addEventListener('scroll', stickyBarScrollHandler, { passive: true, capture: true })
  window.addEventListener('resize', stickyBarScrollHandler)

  nextTick(() => {
    if (commentsRootRef.value && typeof ResizeObserver !== 'undefined') {
      stickyBarResizeObserver = new ResizeObserver(() => scheduleStickyBarLayout())
      stickyBarResizeObserver.observe(commentsRootRef.value)
    }

    const el = topEditorRef.value
    if (!el || typeof IntersectionObserver === 'undefined') return
    topEditorObserver = new IntersectionObserver(
      ([entry]) => {
        if (entry?.isIntersecting) {
          commentEditorEverVisible.value = true
        }
        stickyEditorVisible.value =
          commentEditorEverVisible.value && !entry.isIntersecting
        scheduleStickyBarLayout()
      },
      { threshold: 0, rootMargin: '0px' }
    )
    topEditorObserver.observe(el)
    scheduleStickyBarLayout()
    bindStickySpacerResizeObserver()
    syncMainCommentTextareaSize()
  })
})

onUnmounted(() => {
  if (stickyBarScrollHandler) {
    window.removeEventListener('scroll', stickyBarScrollHandler, { capture: true })
    window.removeEventListener('resize', stickyBarScrollHandler)
    stickyBarScrollHandler = null
  }
  topEditorObserver?.disconnect()
  topEditorObserver = null
  stickyBarResizeObserver?.disconnect()
  stickyBarResizeObserver = null
  stickySpacerResizeObserver?.disconnect()
  stickySpacerResizeObserver = null
  if (stickyLayoutRaf) {
    cancelAnimationFrame(stickyLayoutRaf)
    stickyLayoutRaf = 0
  }
})

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

const mapReplyRow = (r, textOverride) => ({
  id: r.id,
  name: r.username || '用户',
  level: normalizeLevel(r.level),
  userId: r.userId,
  isUploader: isUploaderUser(r.userId),
  time: r.createTime || '',
  text: textOverride != null ? textOverride : (r.content || ''),
  likes: r.likeCount || 0,
  dislikes: initialDislikes(r),
  avatar: r.avatar ? normalizeAvatarUrl(r.avatar) : '',
  replyToUserId: r.replyToUserId != null ? r.replyToUserId : null,
  replyToName: r.replyToUsername || null,
  _liked: false,
  _disliked: false,
  raw: r
})

const replyTotalPages = (item) => {
  const n = item?.replies?.length || 0
  if (n <= 0) return 1
  return Math.max(1, Math.ceil(n / REPLIES_PAGE_SIZE))
}

const replyCurrentPage = (item) => {
  if (!item?.id) return 1
  const total = replyTotalPages(item)
  const raw = replyPageByRoot.value[item.id] || 1
  return Math.min(Math.max(1, raw), total)
}

const replyPageRange = (item) => {
  const t = replyTotalPages(item)
  return Array.from({ length: t }, (_, i) => i + 1)
}

const visibleReplies = (item) => {
  const list = item?.replies || []
  const n = list.length
  if (n <= REPLIES_PREVIEW) return list
  if (!isRepliesExpanded(item.id)) return list.slice(0, REPLIES_PREVIEW)
  const page = replyCurrentPage(item)
  const start = (page - 1) * REPLIES_PAGE_SIZE
  return list.slice(start, start + REPLIES_PAGE_SIZE)
}

const shouldShowReplyExpand = (item) => {
  const n = item?.replies?.length || 0
  return n > REPLIES_PREVIEW && !isRepliesExpanded(item.id)
}

const shouldShowReplyPager = (item) => {
  const n = item?.replies?.length || 0
  return isRepliesExpanded(item.id) && n > REPLIES_PAGE_SIZE
}

const shouldShowReplyCollapseOnly = (item) => {
  const n = item?.replies?.length || 0
  return isRepliesExpanded(item.id) && n > REPLIES_PREVIEW && n <= REPLIES_PAGE_SIZE
}

const expandReplies = (item) => {
  if (!item?.id) return
  repliesExpanded.value = { ...repliesExpanded.value, [item.id]: true }
  replyPageByRoot.value = { ...replyPageByRoot.value, [item.id]: 1 }
}

const collapseReplies = (item) => {
  if (!item?.id) return
  repliesExpanded.value = { ...repliesExpanded.value, [item.id]: false }
  replyPageByRoot.value = { ...replyPageByRoot.value, [item.id]: 1 }
}

const setReplyPage = (item, p) => {
  if (!item?.id || p < 1) return
  replyPageByRoot.value = { ...replyPageByRoot.value, [item.id]: p }
}

const goReplyPrevPage = (item) => {
  const cur = replyCurrentPage(item)
  if (cur > 1) setReplyPage(item, cur - 1)
}

const goReplyNextPage = (item) => {
  const cur = replyCurrentPage(item)
  const total = replyTotalPages(item)
  if (cur < total) setReplyPage(item, cur + 1)
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

/** 评论列表 / 回复行头像加载失败时回退默认图（避免破损图标与异常宽高） */
const onCommentListAvatarError = (e) => {
  const el = e?.target
  if (!el || el.tagName !== 'IMG') return
  const fallback = '/images/default-avatar.png'
  try {
    const abs = new URL(fallback, window.location.origin).href
    if (el.src === abs || el.src.endsWith('/images/default-avatar.png')) {
      el.onerror = null
      return
    }
  } catch {
    /* ignore */
  }
  el.onerror = null
  el.src = fallback
}

const IMG_INLINE_RE = /!\[([^\]]*)\]\(([^)]+)\)/g
/** 存储格式 @[userId:展示名]，展示名内不允许 ]（插入时剔除） */
const MENTION_INLINE_RE = /@\[(\d+):([^\]]+)\]/g

const splitTextWithMentions = (chunk) => {
  const text = chunk == null ? '' : String(chunk)
  if (!text) return [{ type: 'text', value: '' }]
  const re = new RegExp(MENTION_INLINE_RE.source, 'g')
  const segs = []
  let last = 0
  let m
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) {
      segs.push({ type: 'text', value: text.slice(last, m.index) })
    }
    segs.push({ type: 'mention', userId: m[1], name: m[2] })
    last = re.lastIndex
  }
  if (last < text.length) {
    segs.push({ type: 'text', value: text.slice(last) })
  }
  return segs.length ? segs : [{ type: 'text', value: text }]
}

const parseCommentSegments = (raw) => {
  const text = raw == null ? '' : String(raw)
  if (!text) return [{ type: 'text', value: '' }]
  const re = new RegExp(IMG_INLINE_RE.source, 'g')
  const segs = []
  let last = 0
  let m
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) {
      segs.push(...splitTextWithMentions(text.slice(last, m.index)))
    }
    segs.push({ type: 'img', url: (m[2] || '').trim(), alt: m[1] || '' })
    last = re.lastIndex
  }
  if (last < text.length) {
    segs.push(...splitTextWithMentions(text.slice(last)))
  }
  return segs.length ? segs : [{ type: 'text', value: '' }]
}

const isAllowedCommentImageUrl = (url) => {
  const u = (url || '').trim()
  if (!u) return false
  const lower = u.toLowerCase()
  // 本地或同源代理：/feed-images/...
  if (lower.startsWith('/feed-images/')) return true
  if (lower.startsWith('http://') || lower.startsWith('https://')) {
    try {
      const pathname = new URL(u).pathname.toLowerCase()
      // CDN 常见：桶路径 /FeedImages/feed-images/...，pathname 不以 /feed-images/ 开头
      return pathname.includes('/feed-images/')
    } catch {
      return false
    }
  }
  return false
}

const normalizeCommentImgUrl = (url) => normalizeAvatarUrl(url)

const newDraftImageId = () =>
  typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `img-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`

/** 正文 + 草稿图合并为后端存储用的 content（Markdown 图片语法） */
const composeContentWithDraftImages = (rawText, draftList) => {
  const text = (rawText == null ? '' : String(rawText)).trim()
  const imgMd = (draftList || [])
    .map((i) => (i && i.url ? String(i.url).trim() : ''))
    .filter((u) => u && isAllowedCommentImageUrl(u))
    .map((u) => `![](${u})`)
    .join('\n')
  if (!text && !imgMd) return ''
  return [text, imgMd].filter(Boolean).join('\n')
}

const removeCommentDraftImage = (id) => {
  commentDraftImages.value = commentDraftImages.value.filter((x) => x.id !== id)
}

const removeReplyDraftImage = (id) => {
  replyDraftImages.value = replyDraftImages.value.filter((x) => x.id !== id)
}

const getTextareaFromInputRef = (inputRef) => {
  const root = inputRef?.value?.$el
  if (!root) return null
  return root.querySelector('textarea')
}

const insertAtCursor = (textModel, inputRef, chunk) => {
  const ta = getTextareaFromInputRef(inputRef)
  const cur = textModel.value
  if (!ta) {
    textModel.value = cur + chunk
    return
  }
  const start = typeof ta.selectionStart === 'number' ? ta.selectionStart : cur.length
  const end = typeof ta.selectionEnd === 'number' ? ta.selectionEnd : cur.length
  textModel.value = cur.slice(0, start) + chunk + cur.slice(end)
  nextTick(() => {
    ta.focus()
    const pos = start + chunk.length
    ta.setSelectionRange(pos, pos)
    syncMainCommentTextareaSize()
  })
}

const pickEmoji = (where, em) => {
  if (where === 'reply') {
    insertAtCursor(replyText, replyCommentInputRef, em)
    closeAllEmojiPops()
  } else {
    insertMainCommentAtCursorByFocus(em, where === 'sticky' ? 'sticky' : 'top')
    closeAllEmojiPops()
  }
  closeAllMentionPops()
}

const followingDisplayName = (u) => {
  if (!u) return '用户'
  return u.username || u.account || `用户${u.id}`
}

const formatFollowerCount = (n) => {
  const x = Number(n)
  if (!Number.isFinite(x) || x < 0) return ''
  if (x >= 10000) return `${(x / 10000).toFixed(1)}万粉丝`
  return `${x}粉丝`
}

const buildMentionToken = (id, displayName) => {
  const safe = String(displayName || '用户')
    .replace(/\]/g, '')
    .replace(/:/g, '：') || '用户'
  return `@[${id}:${safe}]`
}

const loadFollowingUsers = async () => {
  if (!userStore.isAuthenticated) {
    followingUsers.value = []
    return
  }
  followingLoading.value = true
  try {
    const { data } = await getFollowingUsers()
    if (data?.success && Array.isArray(data.users)) {
      followingUsers.value = data.users
    } else {
      followingUsers.value = []
    }
  } catch (e) {
    console.warn('加载关注列表失败', e)
    followingUsers.value = []
  } finally {
    followingLoading.value = false
  }
}

const closeAllEmojiPops = () => {
  emojiPopTopVisible.value = false
  emojiPopStickyVisible.value = false
  emojiPopReplyVisible.value = false
}

const closeAllMentionPops = () => {
  mentionPopTopVisible.value = false
  mentionPopStickyVisible.value = false
  mentionPopReplyVisible.value = false
}

/** 主评有两套输入框（顶栏 + 底部固定条）：按焦点决定插入目标，避免艾特后表情插错位置 */
const insertMainCommentAtCursorByFocus = (token, toolbarHint) => {
  const stickyTa = getTextareaFromInputRef(stickyCommentInputRef)
  const topTa = getTextareaFromInputRef(topCommentInputRef)
  const active = document.activeElement
  if (stickyTa && active === stickyTa) {
    insertAtCursor(commentText, stickyCommentInputRef, token)
  } else if (topTa && active === topTa) {
    insertAtCursor(commentText, topCommentInputRef, token)
  } else if (toolbarHint === 'sticky') {
    insertAtCursor(commentText, stickyCommentInputRef, token)
  } else {
    insertAtCursor(commentText, topCommentInputRef, token)
  }
}

const onEmojiPopoverShow = () => {
  closeAllMentionPops()
}

const onMentionPopoverShow = () => {
  closeAllEmojiPops()
  loadFollowingUsers()
}

const pickMention = (where, u) => {
  if (!u?.id) return
  const token = buildMentionToken(u.id, followingDisplayName(u))
  if (where === 'reply') {
    insertAtCursor(replyText, replyCommentInputRef, token)
    closeAllMentionPops()
  } else {
    insertMainCommentAtCursorByFocus(token, where === 'sticky' ? 'sticky' : 'top')
    closeAllMentionPops()
  }
  closeAllEmojiPops()
}

const openCommentImagePicker = (target) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  commentImageTarget.value = target
  commentImageInputRef.value?.click()
}

const onCommentImageFileChange = async (e) => {
  const input = e.target
  const file = input.files?.[0]
  if (input) input.value = ''
  if (!file) return
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  uploadingCommentImage.value = true
  try {
    const { data } = await uploadCommentImage(file)
    if (data?.success && data.url) {
      const url = String(data.url).trim()
      const row = { id: newDraftImageId(), url }
      const t = commentImageTarget.value
      if (t === 'reply') {
        replyDraftImages.value = [...replyDraftImages.value, row]
      } else {
        commentDraftImages.value = [...commentDraftImages.value, row]
      }
    } else {
      ElMessage.error(data?.message || '图片上传失败')
    }
  } catch (err) {
    console.error(err)
  } finally {
    uploadingCommentImage.value = false
  }
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
      const mapped = list.map(r => mapReplyRow(r))
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
  replyToCommentRef.value = reply && reply.id != null ? { id: reply.id } : null
  replyPlaceholderName.value = reply && reply.name ? reply.name : (comment?.name || '')
  replyText.value = ''
  replyDraftImages.value = []

  if (comment && comment.id) {
    loadRepliesForComment(comment.id)
  }
}

const cancelReply = () => {
  replyTarget.value = null
  replyToCommentRef.value = null
  replyText.value = ''
  replyDraftImages.value = []
  replyPlaceholderName.value = ''
}

const submitComment = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  const content = composeContentWithDraftImages(commentText.value, commentDraftImages.value)
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
      commentDraftImages.value = []
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

  const content = composeContentWithDraftImages(replyText.value, replyDraftImages.value)
  if (!content) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  submittingComment.value = true
  try {
    // 使用 addComment(videoId, content, parentId) 作为“回复”提交
    const replyToId = replyToCommentRef.value?.id ?? null
    const { data } = await addComment(videoId, content, parentComment.id, replyToId)
    if (data && data.success && data.data) {
      const r = data.data

      const target = comments.value.find(c => c.id === parentComment.id)
      if (target) {
        const replyItem = mapReplyRow(r, r.content || content)
        if (!Array.isArray(target.replies)) target.replies = []
        target.replies.push(replyItem)
      }

      const old = replyCache.value[parentComment.id] || []
      replyCache.value[parentComment.id] = [...old, mapReplyRow(r, r.content || content)]

      // 回复也计入“评论总数（含回复）”
      replyTarget.value = null
      replyToCommentRef.value = null
      replyText.value = ''
      replyDraftImages.value = []
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
    commentEditorEverVisible.value = false
    stickyEditorVisible.value = false
    commentDraftImages.value = []
    replyDraftImages.value = []
    commentTopMainFocusedInside.value = false
    commentStickyMainFocusedInside.value = false
    replyEditorFocusedInside.value = false
    if (props.videoId) {
      await loadCommentTotalWithReplies()
      await loadComments(true)
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
/* 评论输入框滚动条：细轨道 + 圆角滑块（参考常见站样式，WebKit + Firefox） */
/*
 * 行与行之间「额外」空隙 5px：在 Element Plus textarea 默认 line-height:1.5 之上再加 5px。
 * 若写成 calc(1em + 5px) 会比 1.5 更紧（14px 字约 19px < 21px），视觉上会像没调。
 */
$comment-line-gap-extra: 5px;
$comment-line-height-with-gap: calc(1.5em + #{$comment-line-gap-extra});
/* 主评 / 回复输入：无字时与灰条同高；有字后 autosize 增高，仅框体最高 115px（不含下方配图预览区） */
$comment-main-composer-idle-textarea-px: 50px;
$comment-reply-composer-idle-textarea-px: 48px;
$comment-textarea-max-height: 115px;

@mixin comment-textarea-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #7a8088 #e8eaed;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #e8eaed;
    border-radius: 3px;
    margin: 2px 0;
  }

  &::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #9aa0a8 0%, #7d838c 100%);
    border-radius: 100px;
    border: 1px solid #dde0e5;
    min-height: 28px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #5f636b;
    border-color: #cfd3d9;
  }

  &::-webkit-scrollbar-thumb:active {
    background: #4a4e55;
  }

  &::-webkit-scrollbar-corner {
    background: transparent;
  }

  /* 隐藏默认上下箭头按钮，保持细条观感（与图2 细滚动条一致） */
  &::-webkit-scrollbar-button {
    display: none;
    width: 0;
    height: 0;
  }
}

.comments {
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;

  /* 主评顶栏 + 回复区：工具栏图标与表情格（表情弹层挂 body 时见全局 .comment-emoji-popover） */
  .tool-icon-btn {
    box-sizing: border-box;
    width: 32px;
    height: 26px;
    padding: 0;
    margin: 0;
    border: 1px solid #e3e5e7;
    border-radius: 6px;
    background: #fff;
    color: #61666d;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    transition: border-color 0.15s, color 0.15s;

    &:hover:not(:disabled) {
      border-color: #00a1d6;
      color: #00a1d6;
    }

    &:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    &--sm {
      width: 32px;
      height: 26px;
    }
  }

  .tool-icon-svg {
    width: 16px;
    height: 16px;
    display: block;
    flex-shrink: 0;
  }

  .emoji-picker-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 2px;
    padding: 4px;
    overflow: hidden;
    max-height: none;
  }

  .emoji-picker-cell {
    border: none;
    background: transparent;
    font-size: 18px;
    line-height: 1.15;
    padding: 3px;
    margin: 0;
    cursor: pointer;
    border-radius: 4px;

    &:hover {
      background: #f1f2f3;
    }
  }

  .comment-toolbar-send-btn {
    width: 70px !important;
    min-width: 70px !important;
    height: 32px !important;
    min-height: 32px !important;
    padding: 0 8px !important;
    box-sizing: border-box;
    font-size: 15px;
    line-height: 1;
    background-color: #00aeec;
  }

  .comment-header {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 0;
    padding-bottom: 12px;
    // border-bottom: 1px solid #f1f2f3;

    .comment-title {
      margin: 0;
      padding: 0;
      font-size: 20px;
      font-weight: 600;
      line-height: 1.2;
      color: #18191c;
      font-family:
        'PingFang SC',
        'HarmonyOS Sans',
        'Microsoft YaHei',
        system-ui,
        -apple-system,
        sans-serif;
    }

    .title-count {
      margin: 0 30px 0 6px;
      font-size: 13px;
      font-weight: 400;
      line-height: 1.2;
      color: #9499a0;
      font-family:
        'PingFang SC',
        'HarmonyOS Sans',
        'Microsoft YaHei',
        system-ui,
        -apple-system,
        sans-serif;
    }

    .sort-tabs {
      display: inline-flex;
      align-items: baseline;
      gap: 10px;
      font-size: 13px;
      line-height: 1.2;

      .sort-divider {
        color: #e3e5e7;
        user-select: none;
        font-weight: 300;
        line-height: 1.2;
      }

      .sort-item {
        color: #9499a0;
        cursor: pointer;
        line-height: 1.2;

        &.is-active {
          color: #18191c;
          font-weight: 600;
        }

        &:hover {
          color: #61666d;
        }
      }
    }
  }

  .comment-editor {
    display: flex;
    align-items: flex-start;
    margin-top: 10px;
    padding: 6px 0 10px;

    .editor-avatar {
      flex-shrink: 0;
      margin-right: 12px;

      img,
      .avatar-placeholder {
        display: block;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        object-fit: cover;
        background: #e3e5e7;
        overflow: hidden;
        box-sizing: border-box;
      }
    }

    .editor-main {
      flex: 1 1 auto;
      min-width: 0;

      .editor-input {
        width: 100%;
      }

      .editor-composer {
        border-radius: 8px;
        border: 1px solid #e3e5e7;
        box-sizing: border-box;
        transition:
          border-color 0.15s ease,
          background 0.15s ease;

        &:not(.is-expanded) {
          background: #f1f2f3;

          &:focus-within {
            border-color: #00a1d6;
          }
        }

        &.is-expanded {
          background: #fff;

          &:focus-within {
            border-color: #00a1d6;
          }
        }

        &.has-previews :deep(.editor-input-fixed-h .el-textarea__inner) {
          border-radius: 8px 8px 0 0;
        }

        &:not(.has-previews) :deep(.editor-input-fixed-h .el-textarea__inner) {
          border-radius: 8px;
        }
      }

      .editor-input-in-composer {
        width: 100%;

        :deep(.el-textarea__inner) {
          border: none !important;
          box-shadow: none !important;
          padding: 8px 12px 8px 14px;
          font-size: 14px;
          color: #18191c;
          line-height: $comment-line-height-with-gap !important;
          resize: none;

          &::placeholder {
            color: #9499a0;
            font-size: 13px;
          }

          &:focus {
            outline: none;
          }
        }
      }

      .editor-composer:not(.is-expanded) {
        .editor-input-in-composer :deep(.el-textarea__inner) {
          background: #f1f2f3 !important;
        }

        .editor-input-fixed-h.editor-input-in-composer {
          :deep(.el-textarea) {
            min-height: 50px;
          }

          :deep(.el-textarea__inner) {
            min-height: 50px !important;
            max-height: 50px !important;
            height: auto !important;
            overflow-y: auto;
            box-sizing: border-box;
            @include comment-textarea-scrollbar;
          }
        }
      }

      .editor-composer.is-expanded {
        .editor-input-in-composer :deep(.el-textarea__inner) {
          background: #fff !important;
        }

        &.is-expanded-empty .editor-input-fixed-h.editor-input-in-composer {
          :deep(.el-textarea) {
            min-height: $comment-main-composer-idle-textarea-px;
          }

          :deep(.el-textarea__inner) {
            min-height: $comment-main-composer-idle-textarea-px !important;
            max-height: $comment-main-composer-idle-textarea-px !important;
            height: auto !important;
            overflow-y: auto;
            box-sizing: border-box;
            @include comment-textarea-scrollbar;
          }
        }

        &:not(.is-expanded-empty) .editor-input-fixed-h.editor-input-in-composer {
          :deep(.el-textarea) {
            min-height: 0;
          }

          /*
           * 不要用 height:auto !important：会压过 Element Plus 写在 textarea 上的内联 height，
           * autosize 失效，高度会卡在默认/行数估算值（如 ~68px），max-height 也看起来像没生效。
           * 只保留 max-height 封顶，高度交给组件内联样式。
           */
          :deep(.el-textarea__inner) {
            min-height: 0 !important;
            max-height: $comment-textarea-max-height !important;
            overflow-y: auto !important;
            box-sizing: border-box;
            @include comment-textarea-scrollbar;
          }
        }
      }

      .editor-image-previews {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        padding: 0 10px 10px;
        box-sizing: border-box;
      }

      .editor-image-chip {
        position: relative;
        width: 80px;
        height: 80px;
        flex-shrink: 0;
        border-radius: 6px;
        overflow: hidden;
        background: #e3e5e7;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          display: block;
        }
      }

      .editor-image-chip--sm {
        width: 64px;
        height: 64px;
      }

      .editor-image-remove {
        position: absolute;
        top: 2px;
        right: 2px;
        width: 20px;
        height: 20px;
        padding: 0;
        margin: 0;
        border: none;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.55);
        color: #fff;
        font-size: 14px;
        line-height: 1;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;

        &:hover {
          background: rgba(0, 0, 0, 0.75);
        }
      }

      .editor-toolbar-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px 12px;
        margin-top: 8px;

        .toolbar-tools {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-wrap: wrap;
        }

        .toolbar-submit {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-left: auto;

          .login-hint {
            font-size: 12px;
            color: #9499a0;
          }
        }
      }
    }
  }

  .comment-image-input-hidden {
    position: absolute;
    width: 0;
    height: 0;
    opacity: 0;
    pointer-events: none;
    overflow: hidden;
  }

  .comment-sticky-spacer {
    height: 108px;
    flex-shrink: 0;
  }

  .comment-list {
    margin-top: 8px;

    /* 已发出评论正文：行间额外 5px（与输入框同一套算法） */
    .comment-rich-text {
      line-height: $comment-line-height-with-gap;
    }

    .reply-line {
      line-height: $comment-line-height-with-gap;
    }

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
        display: block;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        flex: 0 0 40px;
        object-fit: cover;
        background: #e3e5e7;
        overflow: hidden;
        box-sizing: border-box;
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
            flex-wrap: nowrap;
            gap: 6px;
            min-height: 0;

            .name {
              font-size: 13px;
              font-weight: 500;
              color: #61666d;
              line-height: 1;
              display: inline-flex;
              align-items: center;
            }

            .icon-badges {
              display: inline-flex;
              align-items: center;
              flex-wrap: nowrap;
              gap: 0px;
            }

            .level-badge {
              width: 30px;
              height: 30px;
              object-fit: contain;
              display: block;
              flex-shrink: 0;
            }

            .up-badge {
              width: 24px;
              height: 24px;
              object-fit: contain;
              display: block;
              flex-shrink: 0;
            }
          }

          .time {
            color: #9499a0;
            font-size: 12px;
          }
        }

        /* 仅顶层评论正文；回复正文在 .reply-line .reply-inline-rich */
        > .comment-rich-text.top-comment-text {
          margin: 4px 0 6px;
          color: #18191c;
          font-size: 15px;
          font-weight: 500;
          font-family:
            'PingFang SC',
            'HarmonyOS Sans SC',
            'HarmonyOS Sans',
            'Microsoft YaHei',
            sans-serif;
        }

        .comment-rich-text {
          word-break: break-word;
          overflow-wrap: anywhere;

          .comment-text-chunk {
            white-space: pre-wrap;
          }

          .comment-mention-link {
            color: #00aeec;
            text-decoration: none;
            margin-inline-end: 4px;

            &:hover {
              color: #00a1d6;
            }
          }

          .comment-inline-img {
            display: block;
            max-width: 200px;
            max-height: 200px;
            width: auto;
            height: auto;
            object-fit: contain;
            border-radius: 6px;
            margin: 6px 0 2px;
            background: #f1f2f3;
          }

          .comment-img-fallback {
            color: #9499a0;
            font-size: 13px;
            font-weight: 400;
          }
        }

        .comment-meta-row {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 12px 16px;
          margin-top: 2px;

          .meta-time {
            flex: 0 0 auto;
            font-size: 13px;
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

        /* 与父评论正文列左缘对齐（与 .content 内昵称/正文同起点，不拉到父头像下） */
        .reply-list {
          margin-top: 8px;
          padding-left: 0;
          box-sizing: border-box;

          .reply-expand-row {
            font-size: 13px;
            color: #9499a0;
            margin-top: 4px;
            margin-bottom: 4px;
            padding-left: 0;
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
            align-items: flex-start;
            gap: 8px;
            margin-bottom: 8px;

            .reply-avatar {
              display: block;
              width: 24px;
              height: 24px;
              border-radius: 50%;
              flex-shrink: 0;
              margin-top: 2px;
              object-fit: cover;
              background: #e3e5e7;
              overflow: hidden;
              box-sizing: border-box;
            }

            .reply-content {
              flex: 1 1 auto;
              min-width: 0;

              .reply-line {
                margin: 0 0 6px;
                word-break: break-word;
                overflow-wrap: anywhere;

                .name-with-badge {
                  display: inline-flex;
                  align-items: center;
                  flex-wrap: nowrap;
                  gap: 6px;
                  vertical-align: baseline;
                  margin-right: 6px;

                  .name {
                    font-size: 13px;
                    font-weight: 500;
                    color: #61666d;
                    line-height: 1;
                    display: inline-flex;
                    align-items: center;
                  }

                  .icon-badges {
                    display: inline-flex;
                    align-items: center;
                    flex-wrap: nowrap;
                    gap: 0px;
                  }

                  .level-badge {
                    width: 30px;
                    height: 30px;
                    object-fit: contain;
                    display: block;
                    flex-shrink: 0;
                  }

                  .up-badge {
                    width: 24px;
                    height: 24px;
                    object-fit: contain;
                    display: block;
                    flex-shrink: 0;
                  }
                }

                /* 与 .text 一致，仅 @昵称 使用 .reply-to-user-link 蓝色 */
                .reply-to-inline {
                  display: inline;
                  margin: 0;
                  margin-left: -6px;
                  font-size: 15px;
                  font-weight: 500;
                  color: #18191c;
                  line-height: inherit;
                  font-family:
                    'PingFang SC',
                    'HarmonyOS Sans SC',
                    'HarmonyOS Sans',
                    'Microsoft YaHei',
                    sans-serif;
                }

                .reply-to-user-link {
                  color: #00aeec;
                  text-decoration: none;
                  margin: 0 1px;

                  &:hover {
                    color: #00a1d6;
                  }
                }

                .reply-inline-rich {
                  display: inline;
                  margin: 0;
                  font-size: 15px;
                  font-weight: 500;
                  color: #18191c;
                  line-height: inherit;
                  font-family:
                    'PingFang SC',
                    'HarmonyOS Sans SC',
                    'HarmonyOS Sans',
                    'Microsoft YaHei',
                    sans-serif;
                }
              }
            }
          }

          .reply-pager-row {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 12px 18px;
            margin-top: 6px;
            margin-bottom: 4px;
            font-size: 13px;
            color: #18191c;

            &--collapse-only {
              gap: 0;
            }

            .reply-pager-text {
              color: #18191c;
            }

            .reply-pager-link {
              padding: 0;
              margin: 0;
              border: none;
              background: transparent;
              cursor: pointer;
              font-size: 13px;
              color: #18191c;
              font-family: inherit;

              &:hover {
                color: #00aeec;
              }
            }

            .reply-pager-page {
              padding: 0;
              margin: 0;
              min-width: 1em;
              border: none;
              background: transparent;
              cursor: pointer;
              font-size: 13px;
              color: #18191c;
              font-family: inherit;

              &:hover {
                color: #00aeec;
              }

              &.is-active {
                color: #00aeec;
              }
            }
          }
        }

        .reply-editor {
          margin-top: 8px;
          padding-left: 0;
          box-sizing: border-box;

          .editor-composer {
            border-radius: 8px;
            border: 1px solid #e3e5e7;
            box-sizing: border-box;
            transition:
              border-color 0.15s ease,
              background 0.15s ease;

            &:not(.is-expanded) {
              background: #f1f2f3;

              &:focus-within {
                border-color: #00a1d6;
              }
            }

            &.is-expanded {
              background: #fff;

              &:focus-within {
                border-color: #00a1d6;
              }
            }

            &.has-previews :deep(.reply-composer-input .el-textarea__inner) {
              border-radius: 8px 8px 0 0;
            }

            &:not(.has-previews) :deep(.reply-composer-input .el-textarea__inner) {
              border-radius: 8px;
            }
          }

          .reply-composer-input {
            width: 100%;

            :deep(.el-textarea__inner) {
              border: none !important;
              box-shadow: none !important;
              padding: 8px 10px 8px 12px;
              font-size: 14px;
              color: #18191c;
              line-height: $comment-line-height-with-gap !important;
              overflow-y: auto;
              @include comment-textarea-scrollbar;

              &::placeholder {
                color: #9499a0;
                font-size: 13px;
              }

              &:focus {
                outline: none;
              }
            }
          }

          .editor-composer:not(.is-expanded) .reply-composer-input :deep(.el-textarea__inner) {
            background: #f1f2f3 !important;
            min-height: $comment-reply-composer-idle-textarea-px !important;
            max-height: $comment-reply-composer-idle-textarea-px !important;
            resize: none !important;
          }

          .editor-composer.is-expanded {
            &.is-expanded-empty .reply-composer-input {
              :deep(.el-textarea) {
                min-height: $comment-reply-composer-idle-textarea-px;
              }

              :deep(.el-textarea__inner) {
                background: #fff !important;
                min-height: $comment-reply-composer-idle-textarea-px !important;
                max-height: $comment-reply-composer-idle-textarea-px !important;
                height: auto !important;
                overflow-y: auto !important;
                resize: none !important;
                box-sizing: border-box;
                @include comment-textarea-scrollbar;
              }
            }

            &:not(.is-expanded-empty) .reply-composer-input {
              :deep(.el-textarea) {
                min-height: 0;
              }

              :deep(.el-textarea__inner) {
                background: #fff !important;
                min-height: 0 !important;
                max-height: $comment-textarea-max-height !important;
                overflow-y: auto !important;
                resize: none !important;
                box-sizing: border-box;
                @include comment-textarea-scrollbar;
              }
            }
          }

          .editor-image-previews {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            padding: 0 8px 8px;
            box-sizing: border-box;
          }

          .editor-image-chip {
            position: relative;
            width: 64px;
            height: 64px;
            flex-shrink: 0;
            border-radius: 6px;
            overflow: hidden;
            background: #e3e5e7;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
            }
          }

          .editor-image-remove {
            position: absolute;
            top: 2px;
            right: 2px;
            width: 18px;
            height: 18px;
            padding: 0;
            margin: 0;
            border: none;
            border-radius: 50%;
            background: rgba(0, 0, 0, 0.55);
            color: #fff;
            font-size: 13px;
            line-height: 1;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;

            &:hover {
              background: rgba(0, 0, 0, 0.75);
            }
          }

          .reply-editor-toolbar {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-top: 8px;
            flex-wrap: wrap;
          }

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

/* 底部固定输入条（teleport 到 body，与 .comments 同级以便 scoped 生效） */
.comment-editor-sticky {
  position: fixed;
  bottom: 0;
  z-index: 200;
  box-sizing: border-box;
  background: #fff;
  /* 与 .comment-header / 列表分隔同款 #f1f2f3 细线，无阴影 */
  border-top: 1px solid #f1f2f3;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom, 0px));
  /* left / width 由内联 style 绑定评论区 .comments 的 getBoundingClientRect，避免盖住右侧栏 */

  .comment-editor-sticky-inner {
    display: flex;
    align-items: flex-start;
    width: 100%;
    max-width: 100%;
    margin: 0;
    gap: 12px;
  }

  .editor-avatar {
    flex-shrink: 0;

    img,
    .avatar-placeholder {
      display: block;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
      background: #e3e5e7;
      overflow: hidden;
      box-sizing: border-box;
    }
  }

  .editor-main {
    flex: 1 1 auto;
    min-width: 0;

    .editor-composer {
      border-radius: 8px;
      border: 1px solid #e3e5e7;
      box-sizing: border-box;
      transition:
        border-color 0.15s ease,
        background 0.15s ease;

      &:not(.is-expanded) {
        background: #f1f2f3;

        &:focus-within {
          border-color: #00a1d6;
        }
      }

      &.is-expanded {
        background: #fff;

        &:focus-within {
          border-color: #00a1d6;
        }
      }

      &.has-previews :deep(.editor-input-fixed-h .el-textarea__inner) {
        border-radius: 8px 8px 0 0;
      }

      &:not(.has-previews) :deep(.editor-input-fixed-h .el-textarea__inner) {
        border-radius: 8px;
      }
    }

    .editor-input-in-composer {
      width: 100%;

      :deep(.el-textarea__inner) {
        border: none !important;
        box-shadow: none !important;
        padding: 8px 12px 8px 14px;
        font-size: 14px;
        color: #18191c;
        line-height: $comment-line-height-with-gap !important;
        resize: none;

        &::placeholder {
          color: #9499a0;
          font-size: 13px;
        }

        &:focus {
          outline: none;
        }
      }
    }

    .editor-composer:not(.is-expanded) {
      .editor-input-in-composer :deep(.el-textarea__inner) {
        background: #f1f2f3 !important;
      }

      .editor-input-fixed-h.editor-input-in-composer {
        :deep(.el-textarea) {
          min-height: 50px;
        }

        :deep(.el-textarea__inner) {
          min-height: 50px !important;
          max-height: 50px !important;
          height: auto !important;
          overflow-y: auto;
          box-sizing: border-box;
          @include comment-textarea-scrollbar;
        }
      }
    }

    .editor-composer.is-expanded {
      .editor-input-in-composer :deep(.el-textarea__inner) {
        background: #fff !important;
      }

      &.is-expanded-empty .editor-input-fixed-h.editor-input-in-composer {
        :deep(.el-textarea) {
          min-height: $comment-main-composer-idle-textarea-px;
        }

        :deep(.el-textarea__inner) {
          min-height: $comment-main-composer-idle-textarea-px !important;
          max-height: $comment-main-composer-idle-textarea-px !important;
          height: auto !important;
          overflow-y: auto;
          box-sizing: border-box;
          @include comment-textarea-scrollbar;
        }
      }

      &:not(.is-expanded-empty) .editor-input-fixed-h.editor-input-in-composer {
        :deep(.el-textarea) {
          min-height: 0;
        }

        /* 同顶部主评：勿用 height:auto !important，以免盖住 EP 内联 height */
        :deep(.el-textarea__inner) {
          min-height: 0 !important;
          max-height: $comment-textarea-max-height !important;
          overflow-y: auto !important;
          box-sizing: border-box;
          @include comment-textarea-scrollbar;
        }
      }
    }

    .editor-image-previews {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding: 0 10px 10px;
      box-sizing: border-box;
    }

    .editor-image-chip {
      position: relative;
      width: 80px;
      height: 80px;
      flex-shrink: 0;
      border-radius: 6px;
      overflow: hidden;
      background: #e3e5e7;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }

    .editor-image-remove {
      position: absolute;
      top: 2px;
      right: 2px;
      width: 20px;
      height: 20px;
      padding: 0;
      margin: 0;
      border: none;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.55);
      color: #fff;
      font-size: 14px;
      line-height: 1;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background: rgba(0, 0, 0, 0.75);
      }
    }

    .editor-toolbar-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 10px 12px;
      margin-top: 8px;

      .toolbar-tools {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }

      .toolbar-submit {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-left: auto;

        .login-hint {
          font-size: 12px;
          color: #9499a0;
        }
      }
    }

    .tool-icon-btn {
      box-sizing: border-box;
      width: 32px;
      height: 26px;
      padding: 0;
      margin: 0;
      border: 1px solid #e3e5e7;
      border-radius: 6px;
      background: #fff;
      color: #61666d;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
      transition: border-color 0.15s, color 0.15s;

      &:hover:not(:disabled) {
        border-color: #00a1d6;
        color: #00a1d6;
      }

      &:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }
    }

    .tool-icon-svg {
      width: 16px;
      height: 16px;
      display: block;
      flex-shrink: 0;
    }

    .comment-toolbar-send-btn {
      width: 70px !important;
      min-width: 70px !important;
      height: 32px !important;
      min-height: 32px !important;
      padding: 0 8px !important;
      box-sizing: border-box;
      font-size: 15px;
      line-height: 1;
      background-color: #00aeec;
    }
  }
}
</style>

<style lang="scss">
/* el-popover 挂到 body，需非 scoped */
.comment-emoji-popover.el-popper {
  background: #ffffff !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
  padding: 8px !important;
  border-radius: 8px !important;
  overflow: hidden !important;
}

.comment-emoji-popover .el-popper__arrow::before {
  background: #ffffff !important;
  border: 1px solid #ebeef5 !important;
}

.comment-emoji-popover .emoji-picker-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 2px;
  padding: 4px;
  overflow: hidden !important;
  max-height: none !important;
}

.comment-emoji-popover .emoji-picker-cell {
  border: none;
  background: transparent;
  font-size: 18px;
  line-height: 1.15;
  padding: 3px;
  margin: 0;
  cursor: pointer;
  border-radius: 4px;

  &:hover {
    background: #f1f2f3;
  }
}

.comment-mention-popover.el-popper {
  background: #ffffff !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
  padding: 0 !important;
  border-radius: 8px !important;
  overflow: hidden !important;
}

.comment-mention-popover .el-popper__arrow::before {
  background: #ffffff !important;
  border: 1px solid #ebeef5 !important;
}

.comment-mention-popover .mention-popover-body {
  width: 100%;
  box-sizing: border-box;
}

.comment-mention-popover .mention-popover-title {
  padding: 10px 12px 6px;
  font-size: 13px;
  font-weight: 600;
  color: #18191c;
}

.comment-mention-popover .mention-popover-hint {
  padding: 12px;
  font-size: 13px;
  color: #9499a0;
}

/* 约两行关注项；行高按头像 36px + 上下 padding 8px，并留少量余量兼容昵称下的粉丝数文案 */
.comment-mention-popover .mention-user-list {
  box-sizing: border-box;
  max-height: calc(5 * (36px + 8px + 8px) + 8px);
  overflow-y: auto;
  overflow-x: hidden;
  /* 与 .mention-popover-title 左右 12px 对齐；stable 为滚动条预留占位，避免仅右侧显得更窄 */
  padding: 0 3px 6px 6px;
  margin-left: 4px;
  scrollbar-gutter: stable;
  scrollbar-width: thin;
  /* Firefox：仅拇指可见，轨道透明 */
  scrollbar-color: rgba(24, 25, 28, 0.28) transparent;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-button {
    display: none;
    width: 0;
    height: 0;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(24, 25, 28, 0.22);
    border-radius: 3px;
    border: 2px solid transparent;
    background-clip: padding-box;

    &:hover {
      background: rgba(24, 25, 28, 0.34);
      background-clip: padding-box;
    }
  }
}

.comment-mention-popover .mention-user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 8px;
  margin: 0;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  text-align: left;
  box-sizing: border-box;

  &:hover {
    background: #f1f2f3;
  }
}

.comment-mention-popover .mention-user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  background: #e3e5e7;
}

.comment-mention-popover .mention-user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.comment-mention-popover .mention-user-name {
  font-size: 14px;
  font-weight: 500;
  color: #18191c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-mention-popover .mention-user-fans {
  font-size: 12px;
  color: #9499a0;
}
</style>


