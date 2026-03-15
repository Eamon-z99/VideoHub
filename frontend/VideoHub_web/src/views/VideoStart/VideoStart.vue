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

      <div class="player" ref="playerRef">
        <video
          ref="videoPlayer"
          class="video"
          :src="videoSrc"
          controls
          autoplay
          :poster="posterUrl"
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onVideoLoaded"
          @play="onVideoPlay"
          @pause="onVideoPause"
        />

        <!-- 弹幕展示层 -->
        <div class="danmaku-layer" v-if="danmakuEnabled">
          <div
            v-for="item in danmakuItems"
            :key="item.id"
            class="danmaku-item"
            :style="{
              top: (item.track * 26 + 20) + 'px',
              animationDuration: item.duration + 's',
              color: item.color || '#ffffff',
              animationPlayState: isVideoPlaying ? 'running' : 'paused'
            }"
          >
            {{ item.content }}
          </div>
        </div>
      </div>

      <!-- 弹幕输入栏 -->
      <div class="danmaku-bar" ref="danmakuBarRef">
        <div class="danmaku-info">
          <span class="danmaku-stats">
            <span class="watching-count">
              <span class="count-number">{{ watchingCount }}</span>
              人正在看
            </span>
            <span class="danmaku-count">
              已装填
              <span class="count-number">{{ loadedDanmakuCount }}</span>
              条弹幕
            </span>
          </span>
          <div class="danmaku-controls">
            <button 
              class="danmaku-toggle-btn" 
              :class="{ 'is-active': danmakuEnabled }"
              @click="toggleDanmaku"
              title="弹幕开关"
            >
              <!-- 关闭：带 X 角标（SVG 内部实现，不叠加 span） -->
              <svg v-if="!danmakuEnabled" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path fill-rule="evenodd" d="M11.989 4.828c-.47 0-.975.004-1.515.012l-1.71-2.566a1.008 1.008 0 0 0-1.678 1.118l.999 1.5c-.681.018-1.403.04-2.164.068a4.013 4.013 0 0 0-3.83 3.44c-.165 1.15-.245 2.545-.245 4.185 0 1.965.115 3.67.35 5.116a4.012 4.012 0 0 0 3.763 3.363l.906.046c1.205.063 1.808.095 3.607.095a.988.988 0 0 0 0-1.975c-1.758 0-2.339-.03-3.501-.092l-.915-.047a2.037 2.037 0 0 1-1.91-1.708c-.216-1.324-.325-2.924-.325-4.798 0-1.563.076-2.864.225-3.904.14-.977.96-1.713 1.945-1.747 2.444-.087 4.465-.13 6.063-.131 1.598 0 3.62.044 6.064.13.96.034 1.71.81 1.855 1.814.075.524.113 1.962.141 3.065v.002c.01.342.017.65.025.88a.987.987 0 1 0 1.974-.068c-.008-.226-.016-.523-.025-.856v-.027c-.03-1.118-.073-2.663-.16-3.276-.273-1.906-1.783-3.438-3.74-3.507-.9-.032-1.743-.058-2.531-.078l1.05-1.46a1.008 1.008 0 0 0-1.638-1.177l-1.862 2.59c-.38-.004-.744-.007-1.088-.007h-.13Zm.521 4.775h-1.32v4.631h2.222v.847h-2.618v1.078h2.618l.003.678c.36.026.714.163 1.01.407h.11v-1.085h2.694v-1.078h-2.695v-.847H16.8v-4.63h-1.276a8.59 8.59 0 0 0 .748-1.42L15.183 7.8a14.232 14.232 0 0 1-.814 1.804h-1.518l.693-.308a8.862 8.862 0 0 0-.814-1.408l-1.045.352c.297.396.572.847.825 1.364Zm-4.18 3.564.154-1.485h1.98V8.294h-3.2v.98H9.33v1.43H7.472l-.308 3.453h2.277c0 1.166-.044 1.925-.12 2.277-.078.352-.386.528-.936.528-.308 0-.616-.022-.902-.055l.297 1.067.062.005c.285.02.551.04.818.04 1.001-.067 1.562-.419 1.694-1.057.11-.638.176-1.903.176-3.795h-2.2Zm7.458.11v-.858h-1.254v.858h1.254Zm-2.376-.858v.858h-1.199v-.858h1.2Zm-1.199-.946h1.2v-.902h-1.2v.902Zm2.321 0v-.902h1.254v.902h-1.254Z" clip-rule="evenodd"></path>
                <!-- 角标：加大并右下对齐，更醒目 -->
                <circle cx="19" cy="19" r="4.25" fill="currentColor"></circle>
                <path d="M17.6 17.6L20.4 20.4M20.4 17.6L17.6 20.4" stroke="#fff" stroke-width="2" stroke-linecap="round"></path>
              </svg>
              <!-- 开启：带 ✓ 角标（SVG 内部实现，不叠加 span） -->
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path fill-rule="evenodd" d="M11.989 4.828c-.47 0-.975.004-1.515.012l-1.71-2.566a1.008 1.008 0 0 0-1.678 1.118l.999 1.5c-.681.018-1.403.04-2.164.068a4.013 4.013 0 0 0-3.83 3.44c-.165 1.15-.245 2.545-.245 4.185 0 1.965.115 3.67.35 5.116a4.012 4.012 0 0 0 3.763 3.363l.906.046c1.205.063 1.808.095 3.607.095a.988.988 0 0 0 0-1.975c-1.758 0-2.339-.03-3.501-.092l-.915-.047a2.037 2.037 0 0 1-1.91-1.708c-.216-1.324-.325-2.924-.325-4.798 0-1.563.076-2.864.225-3.904.14-.977.96-1.713 1.945-1.747 2.444-.087 4.465-.13 6.063-.131 1.598 0 3.62.044 6.064.13.96.034 1.71.81 1.855 1.814.075.524.113 1.962.141 3.065v.002c.01.342.017.65.025.88a.987.987 0 1 0 1.974-.068c-.008-.226-.016-.523-.025-.856v-.027c-.03-1.118-.073-2.663-.16-3.276-.273-1.906-1.783-3.438-3.74-3.507-.9-.032-1.743-.058-2.531-.078l1.05-1.46a1.008 1.008 0 0 0-1.638-1.177l-1.862 2.59c-.38-.004-.744-.007-1.088-.007h-.13Zm.521 4.775h-1.32v4.631h2.222v.847h-2.618v1.078h2.618l.003.678c.36.026.714.163 1.01.407h.11v-1.085h2.694v-1.078h-2.695v-.847H16.8v-4.63h-1.276a8.59 8.59 0 0 0 .748-1.42L15.183 7.8a14.232 14.232 0 0 1-.814 1.804h-1.518l.693-.308a8.862 8.862 0 0 0-.814-1.408l-1.045.352c.297.396.572.847.825 1.364Zm-4.18 3.564.154-1.485h1.98V8.294h-3.2v.98H9.33v1.43H7.472l-.308 3.453h2.277c0 1.166-.044 1.925-.12 2.277-.078.352-.386.528-.936.528-.308 0-.616-.022-.902-.055l.297 1.067.062.005c.285.02.551.04.818.04 1.001-.067 1.562-.419 1.694-1.057.11-.638.176-1.903.176-3.795h-2.2Zm7.458.11v-.858h-1.254v.858h1.254Zm-2.376-.858v.858h-1.199v-.858h1.2Zm-1.199-.946h1.2v-.902h-1.2v.902Zm2.321 0v-.902h1.254v.902h-1.254Z" clip-rule="evenodd"></path>
                <!-- 角标：加大并右下对齐，更醒目 -->
                <circle cx="19" cy="19" r="4.25" fill="currentColor"></circle>
                <path d="M16.9 19.0L18.4 20.5L21.1 17.8" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
              </svg>
            </button>
            <button 
              class="danmaku-settings-btn" 
              :disabled="!danmakuEnabled"
              :class="{ 'is-disabled': !danmakuEnabled }"
              @click="danmakuEnabled && (showDanmakuSettings = true)"
              title="弹幕设置"
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path fill-rule="evenodd" d="m15.645 4.881 1.06-1.473a.998.998 0 1 0-1.622-1.166L13.22 4.835a110.67 110.67 0 0 0-1.1-.007h-.131c-.47 0-.975.004-1.515.012L8.783 2.3A.998.998 0 0 0 7.12 3.408l.988 1.484c-.688.019-1.418.042-2.188.069a4.013 4.013 0 0 0-3.83 3.44c-.165 1.15-.245 2.545-.245 4.185 0 1.965.115 3.67.35 5.116a4.012 4.012 0 0 0 3.763 3.363c1.903.094 3.317.141 5.513.141a.988.988 0 0 0 0-1.975 97.58 97.58 0 0 1-5.416-.139 2.037 2.037 0 0 1-1.91-1.708c-.216-1.324-.325-2.924-.325-4.798 0-1.563.076-2.864.225-3.904.14-.977.96-1.713 1.945-1.747 2.444-.087 4.465-.13 6.063-.131 1.598 0 3.62.044 6.064.13.96.034 1.71.81 1.855 1.814.075.524.113 1.962.141 3.065v.002c.005.183.01.07.014-.038.004-.096.008-.189.011-.081a.987.987 0 1 0 1.974-.069c-.004-.105-.007-.009-.011.09-.002.056-.004.112-.007.135l-.002.01a.574.574 0 0 1-.005-.091v-.027c-.03-1.118-.073-2.663-.16-3.276-.273-1.906-1.783-3.438-3.74-3.507-.905-.032-1.752-.058-2.543-.079Zm-3.113 4.703h-1.307v4.643h2.2v.04l.651-1.234c.113-.215.281-.389.482-.509v-.11h.235c.137-.049.283-.074.433-.074h1.553V9.584h-1.264a8.5 8.5 0 0 0 .741-1.405l-1.078-.381c-.24.631-.501 1.23-.806 1.786h-1.503l.686-.305c-.228-.501-.5-.959-.806-1.394l-1.034.348c.294.392.566.839.817 1.35Zm-1.7 5.502h2.16l-.564 1.068h-1.595v-1.068Zm-2.498-1.863.152-1.561h1.96V8.289H7.277v.969h2.048v1.435h-1.84l-.306 3.51h2.254c0 1.155-.043 1.906-.12 2.255-.076.348-.38.523-.925.523-.305 0-.61-.022-.893-.055l.294 1.056.061.005c.282.02.546.039.81.039.991-.065 1.547-.414 1.677-1.046.11-.631.175-1.883.175-3.757H8.334Zm5.09-.8v.85h-1.188v-.85h1.187Zm-1.188-.955h1.187v-.893h-1.187v.893Zm2.322.007v-.893h1.241v.893h-1.241Zm.528 2.757a1.26 1.26 0 0 1 1.087-.627l4.003-.009a1.26 1.26 0 0 1 1.094.63l1.721 2.982c.226.39.225.872-.001 1.263l-1.743 3a1.26 1.26 0 0 1-1.086.628l-4.003.009a1.26 1.26 0 0 1-1.094-.63l-1.722-2.982a1.26 1.26 0 0 1 .002-1.263l1.742-3Zm1.967.858a1.26 1.26 0 0 0-1.08.614l-.903 1.513a1.26 1.26 0 0 0-.002 1.289l.885 1.492c.227.384.64.62 1.086.618l2.192-.005a1.26 1.26 0 0 0 1.08-.615l.904-1.518a1.26 1.26 0 0 0 .001-1.288l-.884-1.489a1.26 1.26 0 0 0-1.086-.616l-2.193.005Zm2.517 2.76a1.4 1.4 0 1 1-2.8 0 1.4 1.4 0 0 1 2.8 0Z" clip-rule="evenodd"></path>
              </svg>
            </button>
          </div>
        </div>
        <div class="danmaku-input-wrapper">
          <input
            v-model="danmakuInput"
            class="danmaku-input"
            type="text"
            :disabled="!danmakuEnabled"
            :placeholder="danmakuEnabled ? '发个友善的弹幕见证当下' : '已关闭弹幕'"
            @keyup.enter="handleSendDanmaku"
          />
          <button class="danmaku-etiquette-btn" @click="ElMessage.info('请遵守弹幕礼仪，文明发言')">
            弹幕礼仪 >
          </button>
          <button class="danmaku-send-btn" :disabled="!danmakuEnabled" @click="handleSendDanmaku">
            发送
          </button>
        </div>
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

      <!-- 弹幕列表 -->
      <div
        class="danmaku-list-section"
        ref="danmakuListSectionRef"
        :style="danmakuListStyle"
      >
        <div class="danmaku-list-header" @click="toggleDanmakuList">
          <span class="header-title">弹幕列表</span>
          <el-icon class="header-arrow" :class="{ 'is-expanded': danmakuListExpanded }">
            <ArrowUp />
          </el-icon>
        </div>
        <div v-if="danmakuListExpanded" class="danmaku-list-content">
          <div class="danmaku-table">
            <div class="danmaku-table-header">
              <div class="col-time">时间</div>
              <div class="col-content">
                弹幕内容<span v-if="danmakuDateLabel">（{{ danmakuDateLabel }}）</span>
              </div>
              <div class="col-send-time">发送时间</div>
            </div>
            <div class="danmaku-table-body">
              <div v-if="loadingDanmakuList" class="danmaku-loading">加载中...</div>
              <div v-else-if="danmakuList.length === 0" class="danmaku-empty">
                {{ danmakuDateLabel ? '该日暂无弹幕' : '暂无弹幕' }}
              </div>
              <div
                v-else
                v-for="(item, index) in danmakuList"
                :key="index"
                class="danmaku-table-row"
              >
                <div class="col-time">{{ formatVideoTime(item.time) }}</div>
                <div class="col-content">{{ item.content }}</div>
                <div class="col-send-time">{{ formatSendTime(item.sendTime) }}</div>
              </div>
            </div>
          </div>
          <div class="danmaku-history-picker">
            <div class="danmaku-history-btn">
              <span class="history-text">查询历史弹幕</span>
              <el-date-picker
                v-model="danmakuHistoryDate"
                type="date"
                size="small"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="查询历史弹幕"
                :disabled-date="disableDanmakuDate"
                @change="handleDanmakuHistoryChange"
              />
            </div>
          </div>
        </div>
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
import { ref, onMounted, watch, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View, ChatDotRound, Timer, ArrowUp } from '@element-plus/icons-vue'
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
import { fetchDanmaku, sendDanmaku, getDanmakuCount, getDanmakuList, getDanmakuByDate } from '@/api/danmaku'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const MAX_RECOMMENDS = 40
const INITIAL_RECOMMENDS = 4

const videoPlayer = ref(null)
const playerRef = ref(null)
const danmakuBarRef = ref(null)
const danmakuListSectionRef = ref(null)
const danmakuListHeight = ref(0)
const danmakuListOffset = ref(0)
const danmakuListBaseOffset = ref(null) // 记录第一次计算出的基准偏移，避免因 transform 导致反复漂移
const danmakuListStyle = computed(() => {
  const style = {}
  // 顶部始终与播放器顶部对齐
  if (danmakuListOffset.value) {
    style.transform = `translateY(${danmakuListOffset.value}px)`
  }
  // 只在展开时撑满“播放器顶部 -> 弹幕发送栏底部”的高度
  if (danmakuListExpanded.value && danmakuListHeight.value) {
    style.height = `${danmakuListHeight.value}px`
  }
  return style
})
const isVideoPlaying = ref(true)
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

// 弹幕相关
const danmakuInput = ref('')
const danmakuItems = ref([]) // 当前在屏幕上飞行的弹幕
const DANMAKU_TRACK_COUNT = 8
const DANMAKU_ANIM_DURATION = 8 // 秒
let danmakuIdCounter = 0
let lastDanmakuFetchTimestamp = 0
const DANMAKU_FETCH_INTERVAL = 5000 // 每5秒拉取一次
const DANMAKU_LOOKAHEAD_SEC = 20 // 每次拉取未来 20 秒的弹幕，降低"看不到"的概率
const loadedDanmakuKeys = new Set() // 用于"本次播放过程"去重
const pendingDanmakuQueue = ref([]) // 已拉取但未到展示时间的弹幕（按 time 升序）
let lastVideoTimeSec = 0
const danmakuEnabled = ref(true) // 弹幕开关状态
const watchingCount = ref(1) // 观看人数（暂时固定，后续可从后端获取）
const loadedDanmakuCount = ref(0) // 已装填弹幕数
const showDanmakuSettings = ref(false) // 弹幕设置弹窗

const resetDanmakuRuntime = () => {
  danmakuItems.value = []
  pendingDanmakuQueue.value = []
  loadedDanmakuKeys.clear()
  lastDanmakuFetchTimestamp = 0
  lastVideoTimeSec = 0
}

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
    // 解析视频发布日（用于限制历史弹幕可选日期）
    if (uploadTime.value) {
      const datePart = uploadTime.value.split(' ')[0] // yyyy-MM-dd
      const d = new Date(datePart)
      if (!Number.isNaN(d.getTime())) {
        danmakuMinDate.value = d
      }
    }
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

    // 加载弹幕总数
    try {
      const countResp = await getDanmakuCount(videoId)
      if (countResp.data && countResp.data.success) {
        loadedDanmakuCount.value = countResp.data.count || 0
      }
    } catch (error) {
      console.warn('获取弹幕总数失败:', error)
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

    // 切视频时重置弹幕运行态，避免“上一条视频的去重集合/队列”影响当前视频
    resetDanmakuRuntime()
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
  // 初次挂载后，同步一次高度
  nextTick(() => {
    updateDanmakuListHeight()
  })
  // 监听窗口大小变化，保持高度同步
  window.addEventListener('resize', updateDanmakuListHeight)
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

// 同步右侧弹幕列表高度，使其与左侧视频区域对齐：
// 顶部对齐播放器顶部，底部对齐弹幕发送栏底部
const updateDanmakuListHeight = () => {
  if (!playerRef.value || !danmakuBarRef.value || !danmakuListSectionRef.value) return

  const playerRect = playerRef.value.getBoundingClientRect()
  const barRect = danmakuBarRef.value.getBoundingClientRect()

  // 目标高度：从播放器顶部到弹幕输入栏底部
  const totalHeight = (barRect.bottom - playerRect.top)
  if (totalHeight > 0) {
    danmakuListHeight.value = totalHeight
  }

  // 纵向偏移：让弹幕列表顶部与播放器顶部对齐（只在首次计算时记录基准）
  if (danmakuListBaseOffset.value === null) {
    const listRect = danmakuListSectionRef.value.getBoundingClientRect()
    danmakuListBaseOffset.value = playerRect.top - listRect.top
  }
  danmakuListOffset.value = danmakuListBaseOffset.value || 0
}

// 弹幕列表相关（一次性获取全部弹幕 / 按日期查询）
const danmakuListExpanded = ref(false)
const danmakuList = ref([])
const danmakuListTotal = ref(0)
const loadingDanmakuList = ref(false)
const danmakuHistoryDate = ref(null) // Date 对象
const danmakuDateLabel = ref('')    // 用于显示在“弹幕内容 (3月14日)”中
const danmakuMinDate = ref(null)    // 视频发布日起

// 切换弹幕列表展开/收起
const toggleDanmakuList = () => {
  danmakuListExpanded.value = !danmakuListExpanded.value
  if (danmakuListExpanded.value && danmakuList.value.length === 0) {
    loadDanmakuList()
  }
}

// 加载弹幕列表：一次性获取全部弹幕
const loadDanmakuList = async () => {
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  loadingDanmakuList.value = true
  try {
    const { data } = await getDanmakuList(videoId)
    if (data && data.success) {
      const list = Array.isArray(data.list) ? data.list : []
      danmakuListTotal.value = data.total || 0
      danmakuList.value = list
      danmakuDateLabel.value = '' // 默认“当前全部”不显示日期
    }
  } catch (error) {
    console.error('加载弹幕列表失败:', error)
  } finally {
    loadingDanmakuList.value = false
  }
}

// 加载更多弹幕（全量模式下不分页，此处保留空实现以兼容模板）
const loadMoreDanmaku = () => {}

// 是否有更多弹幕（全量模式下固定为 false）
const hasMoreDanmaku = computed(() => false)

// 将 YYYY-MM-DD 转成 “M月D日” 文本
const formatDateLabel = (isoDateStr) => {
  if (!isoDateStr) return ''
  const d = new Date(isoDateStr)
  if (Number.isNaN(d.getTime())) return ''
  const m = d.getMonth() + 1
  const day = d.getDate()
  return `${m}月${day}日`
}

// 查询指定日期的历史弹幕
const loadDanmakuByDate = async (dateObj) => {
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId || !dateObj) return

  const year = dateObj.getFullYear()
  const month = String(dateObj.getMonth() + 1).padStart(2, '0')
  const day = String(dateObj.getDate()).padStart(2, '0')
  const dateStr = `${year}-${month}-${day}` // 传给后端的 YYYY-MM-DD

  loadingDanmakuList.value = true
  try {
    const { data } = await getDanmakuByDate(videoId, dateStr)
    if (data && data.success) {
      const list = Array.isArray(data.list) ? data.list : []
      danmakuListTotal.value = data.total || list.length
      danmakuList.value = list
      danmakuDateLabel.value = formatDateLabel(dateStr)
    }
  } catch (error) {
    console.error('按日期加载弹幕失败:', error)
  } finally {
    loadingDanmakuList.value = false
  }
}

// 日期选择变化回调
const handleDanmakuHistoryChange = (val) => {
  if (!val) return
  // el-date-picker 在设置了 value-format 后会传字符串，这里统一转成 Date 对象
  const dateObj = typeof val === 'string' ? new Date(val) : val
  loadDanmakuByDate(dateObj)
}

// 限制可选日期：从视频发布日期到今天
const disableDanmakuDate = (time) => {
  const d = new Date(time)
  d.setHours(0, 0, 0, 0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  if (danmakuMinDate.value) {
    const min = new Date(danmakuMinDate.value)
    min.setHours(0, 0, 0, 0)
    if (d < min) return true
  }

  return d > today
}

// 格式化视频内时间
const formatVideoTime = (seconds) => {
  if (seconds == null || seconds < 0) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 格式化发送时间（显示到“年-月-日 时:分”）
const formatSendTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  if (isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
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

// 将一条弹幕加入本地展示层，并在动画结束后移除
const addDanmakuToLayer = (content, time, color = '#ffffff', mode = 'scroll') => {
  const id = danmakuIdCounter++
  const track = id % DANMAKU_TRACK_COUNT
  const item = {
    id,
    content,
    time,
    track,
    duration: DANMAKU_ANIM_DURATION,
    color,
    mode
  }
  danmakuItems.value.push(item)

  // 动画结束后移除，避免数组无限增长
  setTimeout(() => {
    const index = danmakuItems.value.findIndex(d => d.id === id)
    if (index !== -1) {
      danmakuItems.value.splice(index, 1)
    }
  }, DANMAKU_ANIM_DURATION * 1000 + 500)
}

// 将后端返回的弹幕放入“待展示队列”，到点再显示
const enqueueDanmakuList = (list) => {
  if (!Array.isArray(list) || list.length === 0) return

  const queue = pendingDanmakuQueue.value
  list.forEach(item => {
    const t = typeof item.time === 'number' ? item.time : (parseFloat(item.time) || 0)
    const content = (item.content || '').toString()
    if (!content.trim()) return

    // 更稳妥的去重 key（避免同一秒多条内容一样时互相覆盖）
    const key = `${t}-${content}-${item.userId ?? ''}-${item.color ?? ''}-${item.mode ?? ''}`
    if (loadedDanmakuKeys.has(key)) return
    loadedDanmakuKeys.add(key)

    queue.push({
      time: t,
      content,
      color: item.color || '#ffffff',
      mode: item.mode || 'scroll',
      userId: item.userId
    })
  })

  // 按时间排序，方便 flush
  queue.sort((a, b) => (a.time || 0) - (b.time || 0))
}

// 将“已到时间”的弹幕从队列吐到屏幕上
const flushDueDanmaku = (currentTimeSec) => {
  const queue = pendingDanmakuQueue.value
  if (!Array.isArray(queue) || queue.length === 0) return

  // 提前 250ms 展示，体感更接近“同步”
  const dueTime = currentTimeSec + 0.25
  while (queue.length > 0) {
    const next = queue[0]
    if (!next || typeof next.time !== 'number') {
      queue.shift()
      continue
    }
    if (next.time > dueTime) break
    queue.shift()
    addDanmakuToLayer(next.content, next.time, next.color, next.mode)
  }
}

// 从后端按时间段拉取弹幕
const fetchDanmakuSegment = async (from, to) => {
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  try {
    const { data } = await fetchDanmaku(videoId, from, to)
    if (!data || !data.success || !Array.isArray(data.list)) return

    // 注意：不要立刻 add 到 layer，否则会出现“时机不对/看起来没显示”的情况
    enqueueDanmakuList(data.list)
  } catch (e) {
    console.warn('拉取弹幕失败:', e)
  }
}

// 发送弹幕
const handleSendDanmaku = async () => {
  const text = danmakuInput.value.trim()
  if (!text) return

  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录再发送弹幕')
    return
  }

  const videoId = videoData.value.videoId || route.params.id
  if (!videoId || !videoPlayer.value) return

  const currentTime = videoPlayer.value.currentTime || 0

  try {
    await sendDanmaku(videoId, {
      time: currentTime,
      content: text,
      color: '#ffffff',
      mode: 'scroll'
    })
    // 本地立即显示一条（更有反馈感）
    addDanmakuToLayer(text, currentTime, '#ffffff', 'scroll')
    danmakuInput.value = ''
    // 更新弹幕总数
    loadedDanmakuCount.value += 1
  } catch (e) {
    console.error('发送弹幕失败:', e)
    ElMessage.error('发送弹幕失败，请稍后重试')
  }
}

// 切换弹幕开关
const toggleDanmaku = () => {
  danmakuEnabled.value = !danmakuEnabled.value
  if (!danmakuEnabled.value) {
    // 关闭弹幕时清空屏幕上的弹幕
    danmakuItems.value = []
  }
}

// 视频时间更新事件
const onTimeUpdate = () => {
  if (!videoPlayer.value) return

  const currentTime = videoPlayer.value.currentTime || 0
  const now = Date.now()

  // seek 检测：拖动进度条/跳播时，重置队列与去重集合，保证弹幕能在新时间轴重新出现
  if (Math.abs(currentTime - lastVideoTimeSec) > 2.5) {
    // 只清理运行态，不影响后端数据
    danmakuItems.value = []
    pendingDanmakuQueue.value = []
    loadedDanmakuKeys.clear()
    lastDanmakuFetchTimestamp = 0
  }
  lastVideoTimeSec = currentTime

  // 所有人都可以拉取弹幕（仅在弹幕开启时）
  if (danmakuEnabled.value && now - lastDanmakuFetchTimestamp >= DANMAKU_FETCH_INTERVAL) {
    const from = Math.max(0, currentTime - 1)
    const to = currentTime + DANMAKU_LOOKAHEAD_SEC
    fetchDanmakuSegment(from, to)
    lastDanmakuFetchTimestamp = now
  }

  // 将到点的弹幕吐到屏幕上（仅在弹幕开启时）
  if (danmakuEnabled.value) {
    flushDueDanmaku(currentTime)
  }

  // 未登录则不记录播放历史
  if (!userStore.isAuthenticated) return

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

  // 预拉一段弹幕（首屏更稳定）
  const t = videoPlayer.value.currentTime || 0
  fetchDanmakuSegment(Math.max(0, t - 1), t + DANMAKU_LOOKAHEAD_SEC)
  // 视频尺寸稳定后，同步一次右侧弹幕列表高度
  nextTick(() => {
    updateDanmakuListHeight()
  })
}

// 视频播放 / 暂停事件，用于控制弹幕动画暂停/继续
const onVideoPlay = () => {
  isVideoPlaying.value = true
}

const onVideoPause = () => {
  isVideoPlaying.value = false
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

// 清理定时器和事件监听
onUnmounted(() => {
  if (recordTimer) {
    clearInterval(recordTimer)
    recordTimer = null
  }
  window.removeEventListener('resize', updateDanmakuListHeight)
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

      .danmaku-layer {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        z-index: 2;
        pointer-events: none;
        overflow: hidden;
      }

      .danmaku-item {
        position: absolute;
        left: 100%;
        white-space: nowrap;
        color: #ffffff;
        font-size: 16px;
        text-shadow: 0 0 2px #000, 0 0 4px #000;
        animation-name: danmaku-move;
        animation-timing-function: linear;
        animation-fill-mode: forwards;
      }

      @keyframes danmaku-move {
        from {
          transform: translateX(0);
        }
        to {
          /* 确保能横穿整个播放器（% 是相对弹幕自身宽度，容易不够） */
          transform: translateX(-120vw);
        }
      }
    }

    .danmaku-bar {
      background: #fff;
      border-radius: 0;
      padding: 0 16px;
      margin-top: 0;
      height: 46px;
      display: flex;
      align-items: center;
      gap: 16px;
      margin-top: -12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      // border: 1px solid #e3e5e7;

      .danmaku-info {
        display: flex;
        align-items: center;
        justify-content: flex-start;
        gap: 12px;
        white-space: nowrap;

        .danmaku-stats {
          display: flex;
          align-items: center;
          gap: 16px;
          font-size: 13px;
          color: #61666d;

          .watching-count,
          .danmaku-count {
            color: #61666d;
            font-weight: 500;
          }

          .count-number {
            font-weight: 600;
          }
        }

        .danmaku-controls {
          display: flex;
          align-items: center;
          gap: 8px;

          .danmaku-toggle-btn,
          .danmaku-settings-btn {
            width: 32px;
            height: 32px;
            border-radius: 6px;
            border: none;
            background: transparent;
            color: #61666d;
            font-weight: 500;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            transition: all 0.2s;

            // 保持图标风格，与 B 站一致：去掉 hover 背景，只改变前景色
            &:hover {
              background: transparent;
              color: #00aeec;
            }

            &.is-active {
              color: #00aeec;

              .checkmark {
                position: absolute;
                bottom: 2px;
                right: 2px;
                width: 12px;
                height: 12px;
                background: #00a1d6;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #fff;
                font-size: 8px;
                font-weight: bold;
              }
            }

            svg {
              width: 20px;
              height: 20px;
              fill: currentColor;
            }

            svg path {
              fill: currentColor;
            }
          }

          .danmaku-settings-btn.is-disabled {
            color: #c9ccd0;
            cursor: not-allowed;
            pointer-events: none;
          }

          .danmaku-settings-btn.is-disabled:hover {
            background: transparent;
            color: #c9ccd0;
          }
        }
      }

      .danmaku-input-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;

        .danmaku-input {
          flex: 1;
          height: 32px;
          padding: 0 12px;
          border-radius: 4px;
          border: 1px solid #e3e5e7;
          background: #f6f7f8;
          outline: none;
          font-size: 14px;
          color: #18191c;
          transition: all 0.2s;

          &::placeholder {
            color: #9499a0;
          }

          &:focus {
            background: #fff;
            border-color: #00a1d6;
            box-shadow: 0 0 0 2px rgba(0, 161, 214, 0.1);
          }

          &:disabled {
            cursor: not-allowed;
            color: #9499a0;
          }
        }

        .danmaku-etiquette-btn {
          height: 32px;
          padding: 0 12px;
          border-radius: 4px;
          border: 1px solid #e3e5e7;
          background: #fff;
          color: #61666d;
          cursor: pointer;
          font-size: 13px;
          white-space: nowrap;
          transition: all 0.2s;

          &:hover {
            border-color: #00a1d6;
            color: #00a1d6;
          }
        }

        .danmaku-send-btn {
          flex-shrink: 0;
          height: 32px;
          padding: 0 20px;
          border-radius: 4px;
          border: none;
          background: #00a1d6;
          color: #fff;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
          transition: all 0.2s;

          &:hover {
            background: #00b5e6;
          }

          &:active {
            background: #008fb8;
          }

          &:disabled {
            opacity: 0.45;
            cursor: not-allowed;
          }
        }
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
      margin-bottom: 7px;
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
      /* 与上方 .desc 左侧对齐：去掉左右内边距，仅保留上下内边距 */
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
      margin-top: -12px;
      margin-bottom: 7px;
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

    // 弹幕列表区域
    .danmaku-list-section {
      background: #fff;
      // border-radius: 8px;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      .danmaku-list-header {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 16px;
        // min-height: 44px;
        cursor: pointer;
        user-select: none;
        border-radius: 8px;
        // 固定背景色，不随 hover 变化
        background: #F1F2F3;

        .header-title {
          flex: 1;
          // font-weight: 600;
          font-size: 15px;
          color: #18191c;
        }

        .header-arrow {
          font-size: 14px;
          color: #9499a0;
          transition: transform 0.3s;

          &.is-expanded {
            transform: rotate(180deg);
          }
        }
      }

      .danmaku-list-content {
        border-top: 1px solid #f1f2f3;
        display: flex;
        flex-direction: column;
        flex: 1;
        min-height: 0;
      }

      .danmaku-table {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-height: 0;

        .danmaku-table-header {
          display: grid;
          grid-template-columns: 60px 1fr 100px;
          gap: 8px;
          padding: 8px 16px;
          // background: #f7f7f8;
          // border-bottom: 1px solid #e3e5e7;
          font-size: 12px;
          color: #61666d; // 三个标题统一颜色
          font-weight: 500;

          .col-time {
            text-align: left;
          }

          .col-content {
            text-align: left;
          }

          .col-send-time {
            text-align: right;
          }
        }

        .danmaku-table-body {
          flex: 1;
          min-height: 0;
          overflow-y: auto;

          // 美化滚动条
          scrollbar-width: thin;              // Firefox
          scrollbar-color: #c0c4cc transparent;

          &::-webkit-scrollbar {
            width: 6px;
          }

          &::-webkit-scrollbar-track {
            background: transparent;
          }

          &::-webkit-scrollbar-thumb {
            background-color: #c0c4cc;
            border-radius: 3px;
          }

          .danmaku-loading,
          .danmaku-empty {
            padding: 24px;
            text-align: center;
            color: #9499a0;
            font-size: 13px;
          }


          .danmaku-table-row {
            display: grid;
            grid-template-columns: 60px 1fr 100px;
            gap: 8px;
            padding: 4px 16px;
            min-height: 24px;
            align-items: center;
            font-size: 12px;
            transition: background-color 0.2s;

            &:hover {
              background: #f7f7f8;
            }

            .col-time {
              color: #61666d; // 时间列颜色
              font-weight: 500;
            }

            .col-content {
              color: #18191c; // 弹幕内容颜色
              word-break: break-all;
              line-height: 1.4;
            }

            .col-send-time {
              color: #61666d; // 发送时间列颜色
              text-align: right;
            }
          }
        }
      }

      .danmaku-load-more {
        padding: 12px;
        text-align: center;
        border-top: 1px solid #f1f2f3;
        color: #00a1d6;
        font-size: 13px;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background: #f7f7f8;
        }
      }

      .danmaku-history-picker {
        padding: 2px 16px 2px;
        border-top: 1px solid #f1f2f3;
        text-align: center;
        background: #F1F2F3;
        border-radius: 8px;

        .danmaku-history-btn {
          position: relative;
          width: 100%;
          height: 32px;
        }

        .history-text {
          position: absolute;
          left: 50%;
          top: 50%;
          transform: translate(-50%, -50%);
          font-size: 13px;
          color: #18191c;  // 固定为纯黑文字
          pointer-events: none;
          z-index: 2;
        }

        :deep(.el-date-editor) {
          width: 100%;
          height: 100%;
          position: absolute;
          inset: 0;
          z-index: 1;
        }

        :deep(.el-input__wrapper) {
          border-radius: 8px;
          border: none;
          background: #f1f2f3;
          box-shadow: none;
          padding-inline: 0;
          justify-content: center;
        }

        :deep(.el-input__inner) {
          text-align: center;
          font-size: 13px;
          color: transparent;       // 隐藏真实文本
          caret-color: transparent; // 隐藏光标
        }

        // 隐藏前后缀图标，让整体看起来像一条按钮
        :deep(.el-input__prefix),
        :deep(.el-input__suffix) {
          display: none;
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