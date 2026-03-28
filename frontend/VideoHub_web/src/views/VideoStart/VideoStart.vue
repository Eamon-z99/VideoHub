<template>
  <div class="video-page-wrapper">
    <div class="header-wrapper">
      <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    </div>
    <div class="video-page" :class="{ 'is-widescreen': isWidescreen, 'is-web-fullscreen': isWebFullscreen }">
    <!-- 播放器主区 -->
    <section class="player-main">
      <!-- 标题与作者 -->
      <div class="video-meta">
        <div class="title-row">
          <h1 class="title">{{ title }}</h1>
          <div v-if="isWidescreen" class="uploader-inline">
            <img v-if="uploader.avatar" class="u-avatar" :src="uploader.avatar" :alt="uploader.name || '作者'" />
            <div v-else class="u-avatar-placeholder"></div>
            <div class="u-info">
              <div class="u-name">{{ uploader.name || '用户上传' }}</div>
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
        </div>
        <div class="meta-row">
          <div class="meta-left">
            <span class="meta-item">
              <svg class="view-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" width="20" height="20">
                <path d="M10 4.040041666666666C7.897383333333334 4.040041666666666 6.061606666666667 4.147 4.765636666666667 4.252088333333334C3.806826666666667 4.32984 3.061106666666667 5.0637316666666665 2.9755000000000003 6.015921666666667C2.8803183333333333 7.074671666666667 2.791666666666667 8.471183333333332 2.791666666666667 9.998333333333333C2.791666666666667 11.525566666666668 2.8803183333333333 12.922083333333333 2.9755000000000003 13.9808C3.061106666666667 14.932983333333334 3.806826666666667 15.666916666666667 4.765636666666667 15.744683333333336C6.061611666666668 15.849716666666666 7.897383333333334 15.956666666666667 10 15.956666666666667C12.10285 15.956666666666667 13.93871666666667 15.849716666666666 15.234766666666667 15.74461666666667C16.193416666666668 15.66685 16.939000000000004 14.933216666666667 17.024583333333336 13.981216666666668C17.11975 12.922916666666667 17.208333333333332 11.526666666666666 17.208333333333332 9.998333333333333C17.208333333333332 8.470083333333333 17.11975 7.073818333333334 17.024583333333336 6.015513333333334C16.939000000000004 5.063538333333333 16.193416666666668 4.329865000000001 15.234766666666667 4.252118333333334C13.93871666666667 4.147016666666667 12.10285 4.040041666666666 10 4.040041666666666zM4.684808333333334 3.255365C6.001155 3.14862 7.864583333333334 3.0400416666666668 10 3.0400416666666668C12.13565 3.0400416666666668 13.999199999999998 3.148636666666667 15.315566666666667 3.2553900000000002C16.753416666666666 3.3720016666666672 17.890833333333333 4.483195 18.020583333333335 5.925965000000001C18.11766666666667 7.005906666666667 18.208333333333336 8.433 18.208333333333336 9.998333333333333C18.208333333333336 11.56375 18.11766666666667 12.990833333333335 18.020583333333335 14.0708C17.890833333333333 15.513533333333331 16.753416666666666 16.624733333333335 15.315566666666667 16.74138333333333C13.999199999999998 16.848116666666666 12.13565 16.95666666666667 10 16.95666666666667C7.864583333333334 16.95666666666667 6.001155 16.848116666666666 4.684808333333334 16.7414C3.2467266666666665 16.624750000000002 2.1092383333333338 15.513266666666667 1.9795200000000002 14.070383333333334C1.8823900000000002 12.990000000000002 1.7916666666666667 11.562683333333334 1.7916666666666667 9.998333333333333C1.7916666666666667 8.434066666666666 1.8823900000000002 7.00672 1.9795200000000002 5.926381666666667C2.1092383333333338 4.483463333333334 3.2467266666666665 3.371976666666667 4.684808333333334 3.255365z" fill="currentColor"></path>
                <path d="M12.23275 9.1962C12.851516666666667 9.553483333333332 12.851516666666667 10.44665 12.232683333333332 10.803866666666666L9.57975 12.335600000000001C8.960983333333335 12.692816666666667 8.1875 12.246250000000002 8.187503333333334 11.531733333333333L8.187503333333334 8.4684C8.187503333333334 7.753871666666667 8.960983333333335 7.307296666666667 9.57975 7.66456L12.23275 9.1962z" fill="currentColor"></path>
              </svg>
              <span>{{ displayPlayCount }}</span>
            </span>
            <span class="meta-item">
              <svg class="dm-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" width="20" height="20">
                <path d="M10 4.040041666666666C7.897383333333334 4.040041666666666 6.061606666666667 4.147 4.765636666666667 4.252088333333334C3.806826666666667 4.32984 3.061106666666667 5.0637316666666665 2.9755000000000003 6.015921666666667C2.8803183333333333 7.074671666666667 2.791666666666667 8.471183333333332 2.791666666666667 9.998333333333333C2.791666666666667 11.525566666666668 2.8803183333333333 12.922083333333333 2.9755000000000003 13.9808C3.061106666666667 14.932983333333334 3.806826666666667 15.666916666666667 4.765636666666667 15.744683333333336C6.061611666666668 15.849716666666666 7.897383333333334 15.956666666666667 10 15.956666666666667C12.10285 15.956666666666667 13.93871666666667 15.849716666666666 15.234766666666667 15.74461666666667C16.193416666666668 15.66685 16.939000000000004 14.933216666666667 17.024583333333336 13.981216666666668C17.11975 12.922916666666667 17.208333333333332 11.526666666666666 17.208333333333332 9.998333333333333C17.208333333333332 8.470083333333333 17.11975 7.073818333333334 17.024583333333336 6.015513333333334C16.939000000000004 5.063538333333333 16.193416666666668 4.329865000000001 15.234766666666667 4.252118333333334C13.93871666666667 4.147016666666667 12.10285 4.040041666666666 10 4.040041666666666zM4.684808333333334 3.255365C6.001155 3.14862 7.864583333333334 3.0400416666666668 10 3.0400416666666668C12.13565 3.0400416666666668 13.999199999999998 3.148636666666667 15.315566666666667 3.2553900000000002C16.753416666666666 3.3720016666666672 17.890833333333333 4.483195 18.020583333333335 5.925965000000001C18.11766666666667 7.005906666666667 18.208333333333336 8.433 18.208333333333336 9.998333333333333C18.208333333333336 11.56375 18.11766666666667 12.990833333333335 18.020583333333335 14.0708C17.890833333333333 15.513533333333331 16.753416666666666 16.624733333333335 15.315566666666667 16.74138333333333C13.999199999999998 16.848116666666666 12.13565 16.95666666666667 10 16.95666666666667C7.864583333333334 16.95666666666667 6.001155 16.848116666666666 4.684808333333334 16.7414C3.2467266666666665 16.624750000000002 2.1092383333333338 15.513266666666667 1.9795200000000002 14.070383333333334C1.8823900000000002 12.990000000000002 1.7916666666666667 11.562683333333334 1.7916666666666667 9.998333333333333C1.7916666666666667 8.434066666666666 1.8823900000000002 7.00672 1.9795200000000002 5.926381666666667C2.1092383333333338 4.483463333333334 3.2467266666666665 3.371976666666667 4.684808333333334 3.255365z" fill="currentColor"></path>
                <path d="M13.291666666666666 8.833333333333334L8.166666666666668 8.833333333333334C7.890526666666666 8.833333333333334 7.666666666666666 8.609449999999999 7.666666666666666 8.333333333333334C7.666666666666666 8.057193333333334 7.890526666666666 7.833333333333334 8.166666666666668 7.833333333333334L13.291666666666666 7.833333333333334C13.567783333333335 7.833333333333334 13.791666666666668 8.057193333333334 13.791666666666668 8.333333333333334C13.791666666666668 8.609449999999999 13.567783333333335 8.833333333333334 13.291666666666666 8.833333333333334z" fill="currentColor"></path>
                <path d="M14.541666666666666 12.166666666666666L9.416666666666668 12.166666666666666C9.140550000000001 12.166666666666666 8.916666666666666 11.942783333333333 8.916666666666666 11.666666666666668C8.916666666666666 11.390550000000001 9.140550000000001 11.166666666666668 9.416666666666668 11.166666666666668L14.541666666666666 11.166666666666668C14.817783333333335 11.166666666666668 15.041666666666668 11.390550000000001 15.041666666666668 11.666666666666668C15.041666666666668 11.942783333333333 14.817783333333335 12.166666666666666 14.541666666666666 12.166666666666666z" fill="currentColor"></path>
                <path d="M6.5 8.333333333333334C6.5 8.609449999999999 6.27614 8.833333333333334 6 8.833333333333334L5.458333333333333 8.833333333333334C5.182193333333334 8.833333333333334 4.958333333333334 8.609449999999999 4.958333333333334 8.333333333333334C4.958333333333334 8.057193333333334 5.182193333333334 7.833333333333334 5.458333333333333 7.833333333333334L6 7.833333333333334C6.27614 7.833333333333334 6.5 8.057193333333334 6.5 8.333333333333334z" fill="currentColor"></path>
                <path d="M7.750000000000001 11.666666666666668C7.750000000000001 11.942783333333333 7.526140000000001 12.166666666666666 7.25 12.166666666666666L6.708333333333334 12.166666666666666C6.432193333333334 12.166666666666666 6.208333333333334 11.942783333333333 6.208333333333334 11.666666666666668C6.208333333333334 11.390550000000001 6.432193333333334 11.166666666666668 6.708333333333334 11.166666666666668L7.25 11.166666666666668C7.526140000000001 11.166666666666668 7.750000000000001 11.390550000000001 7.750000000000001 11.666666666666668z" fill="currentColor"></path>
              </svg>
              <span>{{ displayCommentTotal }}</span>
            </span>
            <span class="meta-item upload-time" v-if="uploadTime">
              {{ uploadTime }}
            </span>
            <span class="meta-item copyright">
              <svg class="copyright-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="16" height="16">
                <path d="M3.6893999999999996 5.1036666666666655C2.3498133333333335 7.12952 2.5688666666666666 9.875600000000002 4.346579999999999 11.653333333333332C6.1242600000000005 13.431066666666666 8.8704 13.650133333333333 10.896266666666664 12.310533333333334L3.6893999999999996 5.1036666666666655zM5.103626666666667 3.689440000000001L12.310599999999997 10.896333333333331C13.650066666666667 8.870466666666665 13.43113333333333 6.124313333333333 11.653400000000001 4.346606666666665C9.875666666666667 2.5689266666666666 7.129559999999999 2.349853333333333 5.103626666666667 3.689440000000001zM2.9323533333333334 13.067533333333337C0.13359999999999994 10.268866666666664 0.13359999999999994 5.731153333333332 2.9323533333333334 2.9324C5.731113333333333 0.1336399999999999 10.268799999999999 0.1336399999999999 13.0676 2.9324C15.866399999999995 5.731153333333332 15.866399999999995 10.268866666666664 13.0676 13.067533333333337C10.268799999999999 15.866333333333332 5.731113333333333 15.866333333333332 2.9323533333333334 13.067533333333337z" fill="currentColor"></path>
              </svg>
              <span>未经作者授权，禁止转载</span>
            </span>
          </div>
        </div>
      </div>

      <div class="player-shell">
        <div
          class="player"
          :class="{ 'is-web-fullscreen': isWebFullscreen, 'is-mini-player': isMiniPlayer, 'is-paused': !isVideoPlaying }"
          :style="miniPlayerStyle"
          ref="playerRef"
          @mouseenter="onPlayerEnter"
          @mouseleave="onPlayerLeave"
        >
          <video
            ref="videoPlayer"
            class="video"
            :src="videoSrc"
            :controls="false"
            autoplay
            :poster="posterUrl"
            disableRemotePlayback
            controlsList="nodownload noplaybackrate nofullscreen noremoteplayback"
            @contextmenu.prevent
            @dblclick.prevent
            @timeupdate="onTimeUpdate"
            @loadedmetadata="onVideoLoaded"
            @play="onVideoPlay"
            @pause="onVideoPause"
          />

          <div class="player-corner-time" aria-hidden="true">
            {{ currentTimeText }}/{{ durationText }}
          </div>

          <button
            v-if="isMiniPlayer"
            class="mini-close-btn"
            type="button"
            title="退出画中画"
            @click="togglePiP"
          >
            ×
          </button>
          <div
            v-if="isMiniPlayer"
            class="mini-drag-handle"
            title="拖动悬浮窗"
            @mousedown.prevent="onMiniDragStart"
          />

        <div class="player-controls" @click.stop>
          <div
            class="progress-bar-wrap"
            ref="progressBarRef"
            @mousemove="onPlayerMouseMove"
            @mouseleave="onPlayerMouseLeave"
            @click="onProgressBarClick"
            @mousedown="onProgressBarMouseDown"
          >
            <div class="progress-bar-bg"></div>
            <div class="progress-bar-fill" :style="{ width: miniProgressPercent + '%' }"></div>
            <div class="progress-bar-thumb" :style="{ left: miniProgressPercent + '%' }"></div>
            <div
              v-show="previewVisible"
              class="hover-preview-box"
              :style="{ left: previewLeftPx + 'px' }"
            >
              <video
                ref="previewVideoPlayer"
                class="hover-preview-video"
                :src="videoSrc"
                muted
                playsinline
                preload="metadata"
              />
              <div class="hover-preview-time">{{ formatVideoTime(previewTimeSec) }}</div>
            </div>
          </div>

          <div class="controls-bottom-row">
            <button class="control-play-btn" @click="togglePlayPause" title="播放/暂停">
              <svg v-if="isVideoPlaying" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M6 5h4v14H6zM14 5h4v14h-4z"></path>
              </svg>
              <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                <path d="M8 5v14l11-7z"></path>
              </svg>
            </button>
            <span class="control-time">{{ currentTimeText }} / {{ durationText }}</span>
            <div class="controls-spacer"></div>
            <!-- 右下角控件（参考图） -->
            <div class="ctrl-group" ref="ctrlGroupRef">
              <!-- 清晰度 -->
              <div class="ctrl-icon-wrap">
                <button class="ctrl-text-btn" type="button" @click="togglePanel('quality')">
                  {{ qualityLabel }}
                </button>
                <div v-show="openPanel === 'quality'" class="ctrl-panel ctrl-panel-quality">
                  <button
                    v-for="q in qualityOptions"
                    :key="q.key"
                    class="ctrl-panel-item"
                    :class="{ 'is-active': q.key === selectedQualityKey }"
                    type="button"
                    @click="selectQuality(q.key)"
                  >
                    <span class="label">{{ q.label }}</span>
                    <span v-if="q.vip" class="vip-tag">大会员</span>
                  </button>
                </div>
              </div>

              <!-- 倍速 -->
              <div class="ctrl-icon-wrap">
                <button class="ctrl-text-btn" type="button" @click="togglePanel('speed')">
                  倍速
                </button>
                <div v-show="openPanel === 'speed'" class="ctrl-panel ctrl-panel-speed">
                  <button
                    v-for="s in speedOptions"
                    :key="s"
                    class="ctrl-panel-item"
                    :class="{ 'is-active': playbackRate === s }"
                    type="button"
                    @click="setPlaybackRate(s)"
                  >
                    {{ s.toFixed(2).replace(/\.00$/, '.0') }}x
                  </button>
                </div>
              </div>

              <!-- 音量 -->
              <div class="ctrl-icon-wrap">
                <button class="ctrl-icon-btn" type="button" title="音量" @click="togglePanel('volume')">
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M3 10v4h4l5 4V6L7 10H3zm13.5 2c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89 1 5 3.77 5 6.71s-2.11 5.71-5 6.71v2.06c4.01-1.1 7-4.78 7-8.77s-2.99-7.67-7-8.77z"></path>
                  </svg>
                </button>
                <div v-show="openPanel === 'volume'" class="ctrl-panel ctrl-panel-volume" @mousedown.stop>
                  <div class="volume-value">{{ Math.round(volume * 100) }}</div>
                  <input
                    class="volume-slider"
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    :value="Math.round(volume * 100)"
                    @input="onVolumeInput"
                  />
                </div>
              </div>

              <!-- 画中画 -->
              <button class="ctrl-icon-btn has-tooltip" type="button" :data-tip="pipTipText" @click="togglePiP">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M19 7H5v10h14V7zm-1.5 1.5v7h-11v-7h11zM14 10h3v3h-3v-3z"></path>
                </svg>
              </button>

              <!-- 宽屏 -->
              <button class="ctrl-icon-btn has-tooltip" type="button" :data-tip="widescreenTipText" @click="toggleWidescreen">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M3 7h18v10H3V7zm2 2v6h14V9H5z"></path>
                </svg>
              </button>

              <!-- 网页全屏 -->
              <button class="ctrl-icon-btn has-tooltip" type="button" :data-tip="webFullscreenTipText" @click="toggleWebFullscreen">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M4 9V4h5v2H6v3H4zm10-5h5v5h-2V6h-3V4zM4 15h2v3h3v2H4v-5zm13 3v-3h2v5h-5v-2h3z"></path>
                </svg>
              </button>

              <!-- 进入全屏 -->
              <button class="ctrl-icon-btn has-tooltip" type="button" :data-tip="fullscreenTipText" @click="enterFullscreen">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M4 9V4h5v2H6v3H4zm10-5h5v5h-2V6h-3V4zM4 15h2v3h3v2H4v-5zm13 3v-3h2v5h-5v-2h3z"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>

          <!-- 弹幕展示层 -->
          <div class="danmaku-layer" v-if="danmakuEnabled && !isMiniPlayer">
          <div
            v-for="item in danmakuItems"
            :key="item.id"
            class="danmaku-item"
            @animationend="onDanmakuAnimationEnd(item.id)"
            :style="{
              top: getDanmakuTopPx(item.track) + 'px',
              animationDuration: item.duration + 's',
              color: item.color || '#ffffff',
              opacity: danmakuOpacity,
              fontSize: (item.fontSizePx || danmakuFontSizePx) + 'px',
              border: item.isSelf ? '1.5px solid currentColor' : 'none',
              borderRadius: '0',
              '--danmaku-travel-distance': (item.travelDistancePx || 0) + 'px',
              animationPlayState: isVideoPlaying ? 'running' : 'paused'
            }"
          >
            {{ item.content }}
          </div>
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
            <div
              class="danmaku-settings-wrap"
              @mouseenter="onDanmakuSettingsEnter"
              @mouseleave="onDanmakuSettingsLeave"
            >
              <button
                class="danmaku-settings-btn"
                :disabled="!danmakuEnabled"
                :class="{ 'is-disabled': !danmakuEnabled }"
                title="弹幕设置"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <path fill-rule="evenodd" d="m15.645 4.881 1.06-1.473a.998.998 0 1 0-1.622-1.166L13.22 4.835a110.67 110.67 0 0 0-1.1-.007h-.131c-.47 0-.975.004-1.515.012L8.783 2.3A.998.998 0 0 0 7.12 3.408l.988 1.484c-.688.019-1.418.042-2.188.069a4.013 4.013 0 0 0-3.83 3.44c-.165 1.15-.245 2.545-.245 4.185 0 1.965.115 3.67.35 5.116a4.012 4.012 0 0 0 3.763 3.363c1.903.094 3.317.141 5.513.141a.988.988 0 0 0 0-1.975 97.58 97.58 0 0 1-5.416-.139 2.037 2.037 0 0 1-1.91-1.708c-.216-1.324-.325-2.924-.325-4.798 0-1.563.076-2.864.225-3.904.14-.977.96-1.713 1.945-1.747 2.444-.087 4.465-.13 6.063-.131 1.598 0 3.62.044 6.064.13.96.034 1.71.81 1.855 1.814.075.524.113 1.962.141 3.065v.002c.005.183.01.07.014-.038.004-.096.008-.189.011-.081a.987.987 0 1 0 1.974-.069c-.004-.105-.007-.009-.011.09-.002.056-.004.112-.007.135l-.002.01a.574.574 0 0 1-.005-.091v-.027c-.03-1.118-.073-2.663-.16-3.276-.273-1.906-1.783-3.438-3.74-3.507-.905-.032-1.752-.058-2.543-.079Zm-3.113 4.703h-1.307v4.643h2.2v.04l.651-1.234c.113-.215.281-.389.482-.509v-.11h.235c.137-.049.283-.074.433-.074h1.553V9.584h-1.264a8.5 8.5 0 0 0 .741-1.405l-1.078-.381c-.24.631-.501 1.23-.806 1.786h-1.503l.686-.305c-.228-.501-.5-.959-.806-1.394l-1.034.348c.294.392.566.839.817 1.35Zm-1.7 5.502h2.16l-.564 1.068h-1.595v-1.068Zm-2.498-1.863.152-1.561h1.96V8.289H7.277v.969h2.048v1.435h-1.84l-.306 3.51h2.254c0 1.155-.043 1.906-.12 2.255-.076.348-.38.523-.925.523-.305 0-.61-.022-.893-.055l.294 1.056.061.005c.282.02.546.039.81.039.991-.065 1.547-.414 1.677-1.046.11-.631.175-1.883.175-3.757H8.334Zm5.09-.8v.85h-1.188v-.85h1.187Zm-1.188-.955h1.187v-.893h-1.187v.893Zm2.322.007v-.893h1.241v.893h-1.241Zm.528 2.757a1.26 1.26 0 0 1 1.087-.627l4.003-.009a1.26 1.26 0 0 1 1.094.63l1.721 2.982c.226.39.225.872-.001 1.263l-1.743 3a1.26 1.26 0 0 1-1.086.628l-4.003.009a1.26 1.26 0 0 1-1.094-.63l-1.722-2.982a1.26 1.26 0 0 1 .002-1.263l1.742-3Zm1.967.858a1.26 1.26 0 0 0-1.08.614l-.903 1.513a1.26 1.26 0 0 0-.002 1.289l.885 1.492c.227.384.64.62 1.086.618l2.192-.005a1.26 1.26 0 0 0 1.08-.615l.904-1.518a1.26 1.26 0 0 0 .001-1.288l-.884-1.489a1.26 1.26 0 0 0-1.086-.616l-2.193.005Zm2.517 2.76a1.4 1.4 0 1 1-2.8 0 1.4 1.4 0 0 1 2.8 0Z" clip-rule="evenodd"></path>
                </svg>
              </button>
              <div
                v-if="isDanmakuSettingsOpen && danmakuEnabled"
                class="danmaku-settings-panel"
              >
                <div class="setting-row">
                  <span class="setting-label">显示区域</span>
                  <div class="setting-slider-wrap">
                    <input
                      class="setting-slider slider-with-marks"
                      type="range"
                      min="0"
                      max="4"
                      step="1"
                      :value="danmakuDisplayAreaSlotIndex"
                      :style="getSliderTrackStyle(displayAreaTrackPercent)"
                      @input="onDisplayAreaInput"
                    />
                    <div class="slider-marks">
                      <span
                        v-for="(_, idx) in 5"
                        :key="`display-mark-${idx}`"
                        class="slider-mark"
                      />
                    </div>
                  </div>
                  <span class="setting-value">{{ danmakuDisplayAreaPercent }}%</span>
                </div>

                <div class="setting-row">
                  <span class="setting-label">不透明度</span>
                  <div class="setting-slider-wrap">
                    <input
                      v-model.number="danmakuOpacityPercent"
                      class="setting-slider"
                      type="range"
                      min="0"
                      max="100"
                      step="1"
                      :style="getSliderTrackStyle(danmakuOpacityPercent)"
                    />
                  </div>
                  <span class="setting-value">{{ danmakuOpacityPercent }}%</span>
                </div>

                <div class="setting-row">
                  <span class="setting-label">弹幕字号</span>
                  <div class="setting-slider-wrap">
                    <input
                      v-model.number="danmakuFontSizePercent"
                      class="setting-slider"
                      type="range"
                      min="50"
                      max="150"
                      step="1"
                      :style="getSliderTrackStyle(fontSizeTrackPercent)"
                    />
                  </div>
                  <span class="setting-value">{{ danmakuFontSizePercent }}%</span>
                </div>

                <div class="setting-row">
                  <span class="setting-label">弹幕速度</span>
                  <div class="setting-slider-wrap">
                    <input
                      v-model.number="danmakuSpeedSlotIndex"
                      class="setting-slider slider-with-marks"
                      type="range"
                      min="0"
                      max="4"
                      step="1"
                      :style="getSliderTrackStyle(speedTrackPercent)"
                    />
                    <div class="slider-marks">
                      <span
                        v-for="(_, idx) in 5"
                        :key="`speed-mark-${idx}`"
                        class="slider-mark"
                      />
                    </div>
                  </div>
                  <span class="setting-value">{{ danmakuSpeedLabel }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="danmaku-input-wrapper">
          <div class="danmaku-input-box">
            <div class="sender-settings-wrap" ref="senderSettingsWrapRef">
              <button
                class="sender-settings-btn"
                :disabled="!danmakuEnabled"
                :class="{ 'is-disabled': !danmakuEnabled, 'is-active': isSenderSettingsOpen }"
                title="发送弹幕设置"
                @click="toggleSenderSettingsPanel"
              >
                <svg xmlns="http://www.w3.org/2000/svg" xml:space="preserve" viewBox="0 0 22 22">
                  <path d="M17 16H5c-.55 0-1 .45-1 1s.45 1 1 1h12c.55 0 1-.45 1-1s-.45-1-1-1zM6.96 15c.39 0 .74-.24.89-.6l.65-1.6h5l.66 1.6c.15.36.5.6.89.6.69 0 1.15-.71.88-1.34l-3.88-8.97C11.87 4.27 11.46 4 11 4s-.87.27-1.05.69l-3.88 8.97c-.27.63.2 1.34.89 1.34zM11 5.98 12.87 11H9.13L11 5.98z"></path>
                </svg>
              </button>
              <div v-if="isSenderSettingsOpen && danmakuEnabled" class="sender-settings-panel">
                <div class="sender-setting-row">
                  <span class="sender-setting-label">字号</span>
                  <div class="sender-size-options">
                    <button
                      v-for="opt in senderDanmakuSizeOptions"
                      :key="opt.key"
                      class="sender-size-btn"
                      :class="{ 'is-active': senderDanmakuSizeKey === opt.key }"
                      @click="senderDanmakuSizeKey = opt.key"
                    >
                      {{ opt.label }}
                    </button>
                  </div>
                </div>
                <div class="sender-setting-row">
                  <span class="sender-setting-label">颜色</span>
                  <div class="sender-color-wrap">
                    <input
                      v-model="senderDanmakuColor"
                      class="sender-color-input"
                      type="text"
                      maxlength="7"
                      @blur="normalizeSenderDanmakuColor"
                    />
                    <span class="sender-color-preview" :style="{ backgroundColor: senderDanmakuColor }"></span>
                  </div>
                  <div class="sender-color-grid">
                    <button
                      v-for="color in senderDanmakuColorOptions"
                      :key="color"
                      class="sender-color-btn"
                      :style="{ backgroundColor: color }"
                      :class="{ 'is-active': senderDanmakuColor.toLowerCase() === color.toLowerCase() }"
                      @click="senderDanmakuColor = color"
                    />
                  </div>
                </div>
              </div>
            </div>
            <input
              v-model="danmakuInput"
              class="danmaku-input"
              type="text"
              :disabled="!danmakuEnabled"
              :placeholder="danmakuEnabled ? '发个友善的弹幕见证当下' : '已关闭弹幕'"
              @keyup.enter="handleSendDanmaku"
            />
            <span class="danmaku-etiquette-text" @click="ElMessage.info('请遵守弹幕礼仪，文明发言')">
              弹幕礼仪 &gt;
            </span>
          </div>
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

                  <!-- 位置调整：投币与收藏互换；投币使用新 SVG 图标 -->
                  <button
                    class="action-item"
                    :class="{ 'is-active': isCoined }"
                    :disabled="coinLoading"
                    @click="toggleCoin"
                  >
                    <span class="action-icon">
                      <svg
                        width="28"
                        height="28"
                        viewBox="0 0 28 28"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          fill-rule="evenodd"
                          clip-rule="evenodd"
                          d="M14.045 25.5454C7.69377 25.5454 2.54504 20.3967 2.54504 14.0454C2.54504 7.69413 7.69377 2.54541 14.045 2.54541C20.3963 2.54541 25.545 7.69413 25.545 14.0454C25.545 17.0954 24.3334 20.0205 22.1768 22.1771C20.0201 24.3338 17.095 25.5454 14.045 25.5454ZM9.66202 6.81624H18.2761C18.825 6.81624 19.27 7.22183 19.27 7.72216C19.27 8.22248 18.825 8.62807 18.2761 8.62807H14.95V10.2903C17.989 10.4444 20.3766 12.9487 20.3855 15.9916V17.1995C20.3854 17.6997 19.9799 18.1052 19.4796 18.1052C18.9793 18.1052 18.5738 17.6997 18.5737 17.1995V15.9916C18.5667 13.9478 16.9882 12.2535 14.95 12.1022V20.5574C14.95 21.0577 14.5444 21.4633 14.0441 21.4633C13.5437 21.4633 13.1382 21.0577 13.1382 20.5574V12.1022C11.1 12.2535 9.52148 13.9478 9.51448 15.9916V17.1995C9.5144 17.6997 9.10883 18.1052 8.60856 18.1052C8.1083 18.1052 7.70273 17.6997 7.70265 17.1995V15.9916C7.71158 12.9487 10.0992 10.4444 13.1382 10.2903V8.62807H9.66202C9.11309 8.62807 8.66809 8.22248 8.66809 7.72216C8.66809 7.22183 9.11309 6.81624 9.66202 6.81624Z"
                          fill="currentColor"
                        />
                      </svg>
                    </span>
                    <span class="action-num">{{ formatCount(coinCount) }}</span>
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
                  <button class="action-link" @click="openReportDialog">
                    <svg
                      class="video-complaint-icon video-toolbar-item-icon"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="currentColor"
                    >
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M9.40194 3.75C10.5566 1.74999 13.4434 1.75001 14.5981 3.75L21.7428 16.125C22.8975 18.125 21.4541 20.625 19.1447 20.625H4.8553C2.5459 20.625 1.10253 18.125 2.25723 16.125L9.40194 3.75ZM12.866 4.75C12.4811 4.08333 11.5189 4.08333 11.134 4.75L3.98928 17.125C3.60438 17.7917 4.08551 18.625 4.8553 18.625H19.1447C19.9145 18.625 20.3957 17.7917 20.0108 17.125L12.866 4.75Z"
                      ></path>
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M12 8C12.4142 8 12.75 8.33579 12.75 8.75V13.75C12.75 14.1642 12.4142 14.5 12 14.5C11.5858 14.5 11.25 14.1642 11.25 13.75V8.75C11.25 8.33579 11.5858 8 12 8Z"
                      ></path>
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M12 15.5C12.4142 15.5 12.75 15.8358 12.75 16.25V16.75C12.75 17.1642 12.4142 17.5 12 17.5C11.5858 17.5 11.25 17.1642 11.25 16.75V16.25C11.25 15.8358 11.5858 15.5 12 15.5Z"
                      ></path>
                    </svg>
                    <span class="action-link-text">稿件举报</span>
                  </button>
                  <button class="action-link" @click="openNoteDialog">
                    <svg
                      class="video-note-icon video-toolbar-item-icon"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="currentColor"
                    >
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M6.75 10C6.75 9.58579 7.08579 9.25 7.5 9.25H16.5C16.9142 9.25 17.25 9.58579 17.25 10C17.25 10.4142 16.9142 10.75 16.5 10.75H7.5C7.08579 10.75 6.75 10.4142 6.75 10Z"
                      ></path>
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M6.75 14C6.75 13.5858 7.08579 13.25 7.5 13.25H13C13.4142 13.25 13.75 13.5858 13.75 14C13.75 14.4142 13.4142 14.75 13 14.75H7.5C7.08579 14.75 6.75 14.4142 6.75 14Z"
                      ></path>
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M12 5.25C9.48998 5.25 7.29811 5.3777 5.75109 5.50315C4.79223 5.58091 4.05407 6.31053 3.96899 7.25687C3.85555 8.51874 3.75 10.1822 3.75 12C3.75 13.8178 3.85555 15.4813 3.96899 16.7431C4.05408 17.6895 4.79214 18.4191 5.75095 18.4968C7.17292 18.6122 9.14013 18.7294 11.3987 18.7476C11.951 18.752 12.3951 19.2033 12.3906 19.7556C12.3862 20.3079 11.9349 20.752 11.3826 20.7475C9.06584 20.7289 7.04905 20.6087 5.58929 20.4903C3.67182 20.3348 2.15034 18.8499 1.97703 16.9222C1.8597 15.6172 1.75 13.892 1.75 12C1.75 10.108 1.8597 8.38283 1.97703 7.07779C2.15034 5.15 3.67203 3.66518 5.58944 3.50969C7.17721 3.38094 9.42438 3.25 12 3.25C14.5759 3.25 16.8232 3.38096 18.411 3.50973C20.3281 3.6652 21.8497 5.14946 22.0231 7.07716C22.1127 8.07392 22.1977 9.31512 22.233 10.6888C22.2471 11.2409 21.811 11.6999 21.2589 11.7141C20.7068 11.7282 20.2478 11.2921 20.2336 10.74C20.1997 9.41683 20.1177 8.21901 20.0311 7.25626C19.946 6.31026 19.2081 5.58094 18.2494 5.50319C16.7023 5.37772 14.5103 5.25 12 5.25Z"
                      ></path>
                      <path
                        fill-rule="evenodd"
                        clip-rule="evenodd"
                        d="M18.2557 13.3102C19.0368 12.5292 20.3031 12.5292 21.0841 13.3102L22.4983 14.7244C23.2794 15.5055 23.2794 16.7718 22.4983 17.5528L18.5486 21.5026C18.1735 21.8777 17.6648 22.0884 17.1344 22.0884L15.0702 22.0884C14.3246 22.0884 13.7202 21.484 13.7202 20.7384V18.6742C13.7202 18.1437 13.9309 17.635 14.306 17.26L18.2557 13.3102ZM21.0841 16.1386L19.6699 14.7244L15.7202 18.6742L15.7202 20.0884L17.1344 20.0884L21.0841 16.1386Z"
                      ></path>
                    </svg>
                    <span class="action-link-text">记笔记</span>
                  </button>
                   <button class="action-more" @click="ElMessage.info('开发中')">⋯</button>
                 </div>
               </div>

      <!-- 简介在上、标签在下 -->
      <div v-if="!isWidescreen" class="desc">
        <p class="intro">{{ description }}</p>
        <div v-if="tags.length" class="tags">
          <router-link
            v-for="t in tags"
            :key="t"
            class="tag-link"
            :to="{ name: 'search', query: { keyword: t } }"
          >{{ t }}</router-link>
        </div>
      </div>

      <!-- 评论区组件 -->
      <VideoComments
        v-if="!isWidescreen"
        :video-id="videoData.videoId || route.params.id"
        :uploader-id="uploader.id"
        @comment-total-change="onCommentTotalChange"
      />
    </section>

    <!-- 左下内容区（仅宽屏显示） -->
    <section v-if="isWidescreen" class="content-main">
      <!-- 简介在上、标签在下 -->
      <div class="desc">
        <p class="intro">{{ description }}</p>
        <div v-if="tags.length" class="tags">
          <router-link
            v-for="t in tags"
            :key="t"
            class="tag-link"
            :to="{ name: 'search', query: { keyword: t } }"
          >{{ t }}</router-link>
        </div>
      </div>

      <!-- 评论区组件 -->
      <VideoComments
        :video-id="videoData.videoId || route.params.id"
        :uploader-id="uploader.id"
        @comment-total-change="onCommentTotalChange"
      />
    </section>

    <!-- 右侧推荐区 -->
    <aside class="sidebar">
      <div v-if="!isWidescreen" class="uploader-card">
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

      <!-- 弹幕列表组件 -->
      <div
        class="danmaku-list-section"
        ref="danmakuListSectionRef"
        :class="{ 'is-expanded': danmakuListExpanded }"
        :style="danmakuListStyle"
      >
        <DanmakuList
          :video-id="videoData.videoId || route.params.id"
          :danmaku-min-date="danmakuMinDate"
          v-model:expanded="danmakuListExpanded"
        />
      </div>

      <VideoRecommendList
        :uploader-id="uploader.id"
        :current-video-id="videoData.videoId || route.params.id"
        :fallback-cover="fallbackCover"
      />
    </aside>

    <VideoComplaintDialog v-model="reportDialogVisible" :video-id="currentVideoId" />
    <VideoNoteDialog v-model="noteDialogVisible" :video-id="currentVideoId" />

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View, ChatDotRound, Timer } from '@element-plus/icons-vue'
import { Pointer, Star, Share } from '@element-plus/icons-vue'
import { fetchVideoDetail } from '@/api/video'
import { recordHistory } from '@/api/history'
import TopHeader from '@/components/TopHeader.vue'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { followUser, unfollowUser, checkFollow, getUserStats } from '@/api/follow'
import { likeVideo, unlikeVideo, checkLike, getLikeCount } from '@/api/like'
import { coinVideo, uncoinVideo, checkCoin, getCoinCount } from '@/api/coin'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { fetchDanmaku, sendDanmaku, getDanmakuCount } from '@/api/danmaku'
import { heartbeatWatch } from '@/api/watch'
import DanmakuList from './DanmakuList.vue'
import VideoRecommendList from './VideoRecommendList.vue'
import VideoComments from './VideoComments.vue'
import VideoComplaintDialog from './components/VideoComplaintDialog.vue'
import VideoNoteDialog from './components/VideoNoteDialogClean.vue'

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
  // 展开时：限制最大高度（弹幕多就滚动），弹幕少则不强行占满，避免把推荐区顶出大片空白
  if (danmakuListExpanded.value && danmakuListHeight.value) {
    style['--danmaku-max-height'] = `${danmakuListHeight.value}px`
  }
  return style
})
const isVideoPlaying = ref(true)
const progressBarRef = ref(null)
const currentVideoTimeSec = ref(0)

// 右下角控件（清晰度/倍速/字幕/音量/设置）
const ctrlGroupRef = ref(null)
const openPanel = ref(null) // 'quality' | 'speed' | 'volume'
const speedOptions = [2.0, 1.5, 1.25, 1.0, 0.75, 0.5]
const playbackRate = ref(1.0)
const volume = ref(1.0)
const isWebFullscreen = ref(false)
const isWidescreen = ref(false)
const isMiniPlayer = ref(false)
const isPictureInPicture = ref(false)
const isFullscreenActive = ref(false)
const miniPosX = ref(null)
const miniPosY = ref(null)
const miniDragging = ref(false)
let miniDragOffsetX = 0
let miniDragOffsetY = 0
let miniDragMoveHandler = null
let miniDragUpHandler = null
const MINI_PLAYER_W = 360
const MINI_PLAYER_H = 202

const qualityOptions = [
  { key: '4k', label: '4K 超高清', vip: true },
  { key: '1080p_high_rate', label: '1080P 高码率', vip: true },
  { key: '1080p', label: '1080P 高清', vip: false },
  { key: '720p', label: '720P 准高清', vip: false },
  { key: '480p', label: '480P 标清', vip: false },
  { key: '360p', label: '360P 流畅', vip: false },
]
const selectedQualityKey = ref('auto')
const qualityLabel = computed(() => {
  if (selectedQualityKey.value === 'auto') return '自动'
  const hit = qualityOptions.find(q => q.key === selectedQualityKey.value)
  return hit ? hit.label.replace(/\s.*$/, '') : '自动'
})
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
const coinCount = ref(0)

// 稿件举报 / 记笔记弹窗开关（表单逻辑在各自组件内）
const reportDialogVisible = ref(false)
const noteDialogVisible = ref(false)

const currentVideoId = computed(() => videoData.value.videoId || route.params.id)

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

// 投币相关
const isCoined = ref(false)
const coinLoading = ref(false)

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
const DANMAKU_BASE_TOP_PX = 20
const DANMAKU_BASE_STEP_PX = 26
const DANMAKU_ANIM_DURATION_FALLBACK_SEC = 8 // 兜底：避免异常时动画参数缺失
const DANMAKU_TRACK_GAP_PX = 12 // 同轨相邻弹幕最小间距
let danmakuIdCounter = 0
let danmakuTrackCursor = 0
const danmakuTrackStateMap = new Map()
let lastDanmakuFetchTimestamp = 0
const DANMAKU_FETCH_INTERVAL = 5000 // 每5秒拉取一次
const DANMAKU_LOOKAHEAD_SEC = 20 // 每次拉取未来 20 秒的弹幕，降低"看不到"的概率
const loadedDanmakuKeys = new Set() // 用于"本次播放过程"去重
const pendingDanmakuQueue = ref([]) // 已拉取但未到展示时间的弹幕（按 time 升序）
let lastVideoTimeSec = 0

const miniProgressPercent = ref(0)
const previewVideoPlayer = ref(null)
const previewVisible = ref(false)
const previewTimeSec = ref(0)
const previewLeftPx = ref(0)
const isPlayerHovered = ref(false)
const isPlayerActive = computed(() => isPlayerHovered.value || openPanel.value !== null)
let previewSeekTimer = null
let previewSeekSeq = 0
const PREVIEW_WIDTH_PX = 180
const danmakuEnabled = ref(true) // 弹幕开关状态
const watchingCount = ref(1)
const loadedDanmakuCount = ref(0) // 已装填弹幕数

// 显示区域四档：25/50/75/100（直接驱动百分比）
const danmakuDisplayAreaPercent = ref(75)

const danmakuOpacityPercent = ref(80) // 不透明度
const danmakuFontSizePercent = ref(100) // 弹幕字号（百分比）

const danmakuSpeedSlots = [
  { label: '极慢', duration: 14 },
  { label: '较慢', duration: 11 },
  { label: '适中', duration: 8 },
  { label: '较快', duration: 6 },
  { label: '极快', duration: 4 },
] // 弹幕速度五档
const danmakuSpeedSlotIndex = ref(2) // 默认适中

const danmakuOpacity = computed(() => {
  const v = Number(danmakuOpacityPercent.value)
  return Math.max(0, Math.min(100, v)) / 100
})

// 当前 CSS 默认字号约 16px，以 100% = 16px 为基准
const danmakuFontSizePx = computed(() => {
  const base = 16
  const v = Number(danmakuFontSizePercent.value)
  const px = base * (v / 100)
  return Math.max(8, Math.min(28, px))
})

const danmakuSpeedLabel = computed(() => {
  return danmakuSpeedSlots[danmakuSpeedSlotIndex.value]?.label || '适中'
})

const danmakuAnimationDurationSec = computed(() => {
  return danmakuSpeedSlots[danmakuSpeedSlotIndex.value]?.duration || 8
})

const visibleDanmakuTrackCount = computed(() => {
  const displayPercent = Math.max(0, Math.min(100, Number(danmakuDisplayAreaPercent.value) || 0))
  // 显示区域控制“可用轨道数”，而不是压缩每条弹幕的高度/字号
  const trackCount = Math.ceil((displayPercent / 100) * DANMAKU_TRACK_COUNT)
  return Math.max(1, Math.min(DANMAKU_TRACK_COUNT, trackCount))
})

const isDanmakuSettingsOpen = ref(false)
let danmakuSettingsCloseTimer = null
const danmakuDisplayAreaSlots = [10, 25, 50, 75, 100]
const danmakuDisplayAreaSlotIndex = computed(() => {
  const current = Number(danmakuDisplayAreaPercent.value)
  const index = danmakuDisplayAreaSlots.findIndex(v => v === current)
  return index === -1 ? 3 : index
})

const displayAreaTrackPercent = computed(() => {
  return (danmakuDisplayAreaSlotIndex.value / 4) * 100
})

const speedTrackPercent = computed(() => {
  return (Number(danmakuSpeedSlotIndex.value) / 4) * 100
})

const fontSizeTrackPercent = computed(() => {
  const value = Number(danmakuFontSizePercent.value)
  return Math.max(0, Math.min(100, value - 50))
})

const getSliderTrackStyle = (activePercent) => {
  const p = Math.max(0, Math.min(100, Number(activePercent) || 0))
  return {
    background: `linear-gradient(to right, #18b8ff 0%, #18b8ff ${p}%, rgba(255,255,255,0.28) ${p}%, rgba(255,255,255,0.28) 100%)`
  }
}

const onDisplayAreaInput = (event) => {
  const index = Math.max(0, Math.min(4, Number(event?.target?.value || 0)))
  danmakuDisplayAreaPercent.value = danmakuDisplayAreaSlots[index]
}

const onDanmakuSettingsEnter = () => {
  if (!danmakuEnabled.value) return
  if (danmakuSettingsCloseTimer) {
    window.clearTimeout(danmakuSettingsCloseTimer)
    danmakuSettingsCloseTimer = null
  }
  isDanmakuSettingsOpen.value = true
}

const onDanmakuSettingsLeave = () => {
  if (danmakuSettingsCloseTimer) {
    window.clearTimeout(danmakuSettingsCloseTimer)
    danmakuSettingsCloseTimer = null
  }
  danmakuSettingsCloseTimer = window.setTimeout(() => {
    isDanmakuSettingsOpen.value = false
    danmakuSettingsCloseTimer = null
  }, 80)
}

const senderSettingsWrapRef = ref(null)
const isSenderSettingsOpen = ref(false)
const senderDanmakuColor = ref('#FE0302')
const senderDanmakuColorOptions = [
  '#FE0302', '#FF7A00', '#FFB300', '#FFD200', '#F7F71C', '#8DEB00', '#00C800',
  '#00A2A5', '#3E67C4', '#75BDF0', '#E3008C', '#1A1A1A', '#9B9B9B', '#FFFFFF'
]
const senderDanmakuSizeOptions = [
  { key: 'small', label: '小', px: 14 },
  { key: 'normal', label: '标准', px: 16 }
]
const senderDanmakuSizeKey = ref('small')

const senderDanmakuFontSizePx = computed(() => {
  const option = senderDanmakuSizeOptions.find(opt => opt.key === senderDanmakuSizeKey.value)
  return option?.px || 14
})

const normalizeSenderDanmakuColor = () => {
  const raw = (senderDanmakuColor.value || '').trim()
  const color = raw.startsWith('#') ? raw : `#${raw}`
  if (/^#[0-9a-fA-F]{6}$/.test(color)) {
    senderDanmakuColor.value = color.toUpperCase()
  } else {
    senderDanmakuColor.value = '#FE0302'
  }
}

const toggleSenderSettingsPanel = () => {
  if (!danmakuEnabled.value) return
  isSenderSettingsOpen.value = !isSenderSettingsOpen.value
}

const onGlobalClickCloseSenderSettings = (event) => {
  const target = event?.target
  if (!target) return
  if (senderSettingsWrapRef.value && senderSettingsWrapRef.value.contains(target)) return
  isSenderSettingsOpen.value = false
}

// “在看人数”心跳（轻量实现：TTL 计数）
const WATCH_CLIENT_ID_KEY = 'videohub_watch_client_id'
const watchClientId = (() => {
  try {
    const existing = localStorage.getItem(WATCH_CLIENT_ID_KEY)
    if (existing) return existing
    const id = (crypto?.randomUUID ? crypto.randomUUID() : `c_${Date.now()}_${Math.random().toString(16).slice(2)}`)
    localStorage.setItem(WATCH_CLIENT_ID_KEY, id)
    return id
  } catch (e) {
    return `c_${Date.now()}_${Math.random().toString(16).slice(2)}`
  }
})()
let watchHeartbeatTimer = null

const sendWatchHeartbeat = async () => {
  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return
  try {
    const { data } = await heartbeatWatch(String(videoId), watchClientId)
    if (data && data.success && typeof data.count === 'number') {
      watchingCount.value = data.count
    }
  } catch (e) {
    // 静默失败：不影响播放体验
  }
}

const resetDanmakuRuntime = () => {
  danmakuItems.value = []
  pendingDanmakuQueue.value = []
  loadedDanmakuKeys.clear()
  lastDanmakuFetchTimestamp = 0
  lastVideoTimeSec = 0
  danmakuTrackCursor = 0
  danmakuTrackStateMap.clear()
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

    // 标签：优先使用后端返回的 videos.tags（JSON 数组字符串）
    const parseTags = (v) => {
      if (!v) return []
      if (Array.isArray(v)) return v.map(String).filter(Boolean)
      if (typeof v === 'string') {
        const s = v.trim()
        if (!s) return []
        try {
          const arr = JSON.parse(s)
          if (Array.isArray(arr)) return arr.map(String).filter(Boolean)
        } catch {}
        // 兜底：如果不是 JSON 数组，就当作单个标签文本
        return [s]
      }
      return []
    }

    const parsedTags = parseTags(data.tags)
    tags.value = parsedTags.length
      ? parsedTags
      : [data.sourceFile || '本地视频', data.storagePath || ''].filter(Boolean)
    
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

    // 投币数
    try {
      const coinResp = await getCoinCount(videoId)
      if (coinResp.data && coinResp.data.success) {
        coinCount.value = coinResp.data.coinCount || 0
      }
    } catch (error) {
      console.warn('获取投币数失败:', error)
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

    // 检查收藏 / 点赞 / 投币 / 关注状态
    if (userStore.isAuthenticated) {
      await checkFavoriteStatus(videoId)
      await checkLikeStatus(videoId)
      await checkCoinStatus(videoId)
      if (uploader.value.id) {
        await checkFollowStatus(uploader.value.id)
      }
    }

    // 顶部 meta 的评论数由 `VideoComments.vue` 通过事件同步，
    // 这里不再额外加载，避免“顶层/包含回复口径不同”导致不一致

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

  // 在看人数：立即心跳一次，并周期上报
  sendWatchHeartbeat()
  watchHeartbeatTimer = setInterval(sendWatchHeartbeat, 10_000)
  window.addEventListener('mousedown', onGlobalClickCloseSenderSettings)
  window.addEventListener('mousedown', onGlobalClickCloseCtrlPanels)
  const v = videoPlayer.value
  if (v) {
    v.addEventListener('enterpictureinpicture', onEnterPictureInPicture)
    v.addEventListener('leavepictureinpicture', onLeavePictureInPicture)
  }
  window.addEventListener('fullscreenchange', onFullscreenChange)
})

watch(() => route.params.id, () => {
  loadVideo()
})

watch(() => userStore.isAuthenticated, (isAuth) => {
  if (isAuth && videoData.value.videoId) {
    checkFavoriteStatus(videoData.value.videoId)
    checkCoinStatus(videoData.value.videoId)
  } else {
    isFavorited.value = false
    isCoined.value = false
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

// 用于同步顶部 meta 区“评论数量”，确保与右侧评论区一致
const onCommentTotalChange = (val) => {
  const n = Number(val)
  commentTotal.value = Number.isFinite(n) && n >= 0 ? n : 0
}

// ========== 稿件举报 / 记笔记 ==========
const openReportDialog = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  noteDialogVisible.value = false
  reportDialogVisible.value = true
}

const openNoteDialog = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  noteDialogVisible.value = true
}

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
    // 不四舍五入：向下截断到 1 位小数，避免 9.9万 被显示为 10万
    const w = count / 10000
    const floored = Math.floor(w * 10) / 10
    const text = (floored % 1 === 0) ? String(Math.trunc(floored)) : floored.toFixed(1)
    return text + '万'
  }
  return count.toString()
}

// 顶部 meta 区的数字格式：>9999 显示 x.x万；非数字内容（如“本地文件”）原样返回
const formatMetaNumber = (val) => {
  if (val == null) return ''
  if (typeof val === 'number' && Number.isFinite(val)) return formatCount(val)
  if (typeof val === 'string') {
    const n = Number(val)
    if (Number.isFinite(n) && val.trim() !== '') return formatCount(n)
    return val
  }
  return String(val)
}

const displayPlayCount = computed(() => formatMetaNumber(playCount.value))
const displayCommentTotal = computed(() => formatMetaNumber(commentTotal.value))

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

// 检查投币状态
const checkCoinStatus = async (videoId) => {
  if (!userStore.isAuthenticated || !videoId) return
  try {
    const { data } = await checkCoin(videoId)
    if (data && data.success) {
      isCoined.value = data.isCoined || false
      if (typeof data.coinCount === 'number') {
        coinCount.value = data.coinCount
      }
    }
  } catch (error) {
    console.warn('检查投币状态失败:', error)
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

// 切换投币
const toggleCoin = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  const videoId = videoData.value.videoId || route.params.id
  if (!videoId) return

  coinLoading.value = true
  try {
    if (isCoined.value) {
      const { data } = await uncoinVideo(videoId)
      if (data && data.success) {
        isCoined.value = false
        if (typeof data.coinCount === 'number') coinCount.value = data.coinCount
        ElMessage.success('已取消投币')
      } else {
        ElMessage.warning(data?.message || '取消投币失败')
      }
    } else {
      const { data } = await coinVideo(videoId)
      if (data && data.success) {
        isCoined.value = true
        if (typeof data.coinCount === 'number') coinCount.value = data.coinCount
        ElMessage.success('投币成功')
      } else {
        if (typeof data?.coinCount === 'number') coinCount.value = data.coinCount
        ElMessage.warning(data?.message || '已投币过该视频')
      }
    }
  } catch (error) {
    console.error('投币操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    coinLoading.value = false
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

// 弹幕列表展开状态、最小日期（发布日）
const danmakuListExpanded = ref(false)
const danmakuMinDate = ref(null)    // 视频发布日起

// 格式化视频内时间
const formatVideoTime = (seconds) => {
  if (seconds == null || seconds < 0) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const parseDurationToSeconds = (value) => {
  if (value == null) return 0
  if (typeof value === 'number') {
    return Number.isFinite(value) && value > 0 ? value : 0
  }
  const text = String(value).trim()
  if (!text) return 0
  if (/^\d+(\.\d+)?$/.test(text)) {
    const n = Number(text)
    return Number.isFinite(n) && n > 0 ? n : 0
  }
  const parts = text.split(':').map(part => Number(part))
  if (parts.some(n => !Number.isFinite(n) || n < 0)) return 0
  if (parts.length === 2) {
    return parts[0] * 60 + parts[1]
  }
  if (parts.length === 3) {
    return parts[0] * 3600 + parts[1] * 60 + parts[2]
  }
  return 0
}

const getVideoDurationSec = () => {
  const playerDuration = Number(videoPlayer.value?.duration)
  if (Number.isFinite(playerDuration) && playerDuration > 0) {
    return playerDuration
  }
  return parseDurationToSeconds(videoData.value?.duration)
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
const getPlayerWidthPx = () => {
  const width = Number(playerRef.value?.clientWidth || videoPlayer.value?.clientWidth || 0)
  return Math.max(1, width)
}

const measureDanmakuTextWidthPx = (content, fontSizePx = null) => {
  const text = (content || '').toString()
  if (!text) return 0
  const canvas = measureDanmakuTextWidthPx._canvas || (measureDanmakuTextWidthPx._canvas = document.createElement('canvas'))
  const ctx = canvas.getContext('2d')
  if (!ctx) return text.length * 16
  const size = Number(fontSizePx ?? danmakuFontSizePx.value) || 16
  ctx.font = `600 ${size}px sans-serif`
  return Math.ceil(ctx.measureText(text).width)
}

const canLaunchOnTrack = (track, nowSec, textWidthPx, durationSec, travelDistancePx) => {
  const state = danmakuTrackStateMap.get(track)
  if (!state) return true
  const elapsed = Math.max(0, nowSec - state.launchAtSec)
  const prevSpeedPxPerSec = state.travelDistancePx / Math.max(0.001, state.durationSec)
  const movedPx = prevSpeedPxPerSec * elapsed
  return movedPx >= (state.textWidthPx + DANMAKU_TRACK_GAP_PX)
}

const findAvailableDanmakuTrack = (nowSec, textWidthPx, durationSec, travelDistancePx) => {
  const trackCount = visibleDanmakuTrackCount.value
  for (let i = 0; i < trackCount; i += 1) {
    const candidate = (danmakuTrackCursor + i) % trackCount
    if (canLaunchOnTrack(candidate, nowSec, textWidthPx, durationSec, travelDistancePx)) {
      return candidate
    }
  }
  return -1
}

const addDanmakuToLayer = (
  content,
  time,
  color = '#ffffff',
  mode = 'scroll',
  forcedTrack = null,
  textWidthPx = null,
  travelDistancePx = null,
  fontSizePx = null,
  isSelf = false
) => {
  const id = danmakuIdCounter++
  const trackCount = visibleDanmakuTrackCount.value
  const track = Number.isInteger(forcedTrack) ? forcedTrack : (danmakuTrackCursor % trackCount)
  danmakuTrackCursor = (track + 1) % Math.max(1, trackCount)
  const durationSec = Number(danmakuAnimationDurationSec.value) || DANMAKU_ANIM_DURATION_FALLBACK_SEC
  const actualFontSizePx = Number(fontSizePx ?? danmakuFontSizePx.value) || 16
  const widthPx = Math.max(1, Number(textWidthPx ?? measureDanmakuTextWidthPx(content, actualFontSizePx)) || 1)
  const distancePx = Math.max(1, Number(travelDistancePx ?? (getPlayerWidthPx() + widthPx + 48)) || 1)
  const item = {
    id,
    content,
    time,
    track,
    duration: durationSec,
    color,
    mode,
    travelDistancePx: distancePx,
    fontSizePx: actualFontSizePx,
    isSelf: Boolean(isSelf)
  }
  danmakuItems.value.push(item)
  danmakuTrackStateMap.set(track, {
    launchAtSec: Date.now() / 1000,
    textWidthPx: widthPx,
    durationSec,
    travelDistancePx: distancePx
  })

}

const onDanmakuAnimationEnd = (id) => {
  const index = danmakuItems.value.findIndex(d => d.id === id)
  if (index !== -1) {
    danmakuItems.value.splice(index, 1)
  }
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
    const contentWidthPx = measureDanmakuTextWidthPx(next.content)
    const durationSec = Number(danmakuAnimationDurationSec.value) || DANMAKU_ANIM_DURATION_FALLBACK_SEC
    const travelDistancePx = getPlayerWidthPx() + contentWidthPx + 48
    const nowSec = Date.now() / 1000
    const track = findAvailableDanmakuTrack(nowSec, contentWidthPx, durationSec, travelDistancePx)
    // 所有轨道都暂不可发射时，保留队列头，等待更紧凑但不重叠的同轨续发
    if (track === -1) break
    queue.shift()
    const currentUserId = userStore.user?.userId || userStore.user?.id
    const isSelfDanmaku = currentUserId != null && next.userId != null && String(currentUserId) === String(next.userId)
    addDanmakuToLayer(next.content, next.time, next.color, next.mode, track, contentWidthPx, travelDistancePx, null, isSelfDanmaku)
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
    normalizeSenderDanmakuColor()
    const sendColor = senderDanmakuColor.value || '#FE0302'
    const sendFontSizePx = senderDanmakuFontSizePx.value
    await sendDanmaku(videoId, {
      time: currentTime,
      content: text,
      color: sendColor,
      mode: 'scroll'
    })
    // 本地立即显示一条（更有反馈感）
    addDanmakuToLayer(text, currentTime, sendColor, 'scroll', null, null, null, sendFontSizePx, true)
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
    isDanmakuSettingsOpen.value = false
    isSenderSettingsOpen.value = false
    // 关闭弹幕时清空屏幕上的弹幕
    danmakuItems.value = []
    danmakuTrackStateMap.clear()
    danmakuTrackCursor = 0
  }
}

// 弹幕速度调整时：更新运行中的弹幕动画时长
watch(danmakuSpeedSlotIndex, () => {
  const newDurationSec = Number(danmakuAnimationDurationSec.value) || DANMAKU_ANIM_DURATION_FALLBACK_SEC
  danmakuItems.value.forEach(item => {
    if (!item) return
    item.duration = newDurationSec
  })
})

const getDanmakuTopPx = (track) => {
  const safeTrack = Math.max(0, Math.floor(Number(track) || 0))
  return DANMAKU_BASE_TOP_PX + safeTrack * DANMAKU_BASE_STEP_PX
}

const clampValue = (value, min, max) => Math.max(min, Math.min(max, value))

const updatePreviewFrame = (timeSec) => {
  const pv = previewVideoPlayer.value
  if (!pv) return

  const token = ++previewSeekSeq
  if (previewSeekTimer) window.clearTimeout(previewSeekTimer)
  previewSeekTimer = window.setTimeout(() => {
    if (token !== previewSeekSeq || !previewVideoPlayer.value) return
    try {
      const v = previewVideoPlayer.value
      v.pause()
      const duration = Number(videoPlayer.value?.duration || 0) || 0
      const target = clampValue(timeSec, 0, Math.max(0, duration - 0.01))
      v.currentTime = target
    } catch {
      // 预览失败不影响主播放
    }
  }, 80)
}

const onPlayerMouseMove = (event) => {
  if (!progressBarRef.value || !videoPlayer.value) return
  const duration = getVideoDurationSec()
  if (!duration) return

  const rect = progressBarRef.value.getBoundingClientRect()
  if (!rect.width) return

  const x = clampValue(event.clientX - rect.left, 0, rect.width)
  const ratio = x / rect.width
  const t = duration * ratio

  previewTimeSec.value = t
  previewLeftPx.value = clampValue(x - PREVIEW_WIDTH_PX / 2, 6, rect.width - PREVIEW_WIDTH_PX - 6)
  previewVisible.value = true
  updatePreviewFrame(t)
}

const onPlayerMouseLeave = () => {
  previewVisible.value = false
}

const onPlayerEnter = () => {
  isPlayerHovered.value = true
}

const onPlayerLeave = () => {
  isPlayerHovered.value = false
  previewVisible.value = false
}

const togglePanel = (key) => {
  openPanel.value = (openPanel.value === key) ? null : key
}

const selectQuality = (key) => {
  selectedQualityKey.value = key
  openPanel.value = null
}

const setPlaybackRate = (rate) => {
  const v = videoPlayer.value
  if (!v) return
  playbackRate.value = rate
  v.playbackRate = rate
  openPanel.value = null
}

const onVolumeInput = (event) => {
  const v = videoPlayer.value
  if (!v) return
  const val = Number(event?.target?.value || 0)
  const vol = Math.max(0, Math.min(100, val)) / 100
  volume.value = vol
  v.volume = vol
  v.muted = vol === 0
}

const togglePiP = async () => {
  // 浏览器原生画中画（可跨应用悬浮置顶）
  const v = videoPlayer.value
  if (!v) return
  try {
    // @ts-ignore
    if (document.pictureInPictureElement) {
      // @ts-ignore
      await document.exitPictureInPicture()
      isPictureInPicture.value = false
    } else {
      // @ts-ignore
      await v.requestPictureInPicture()
      isPictureInPicture.value = true
    }
  } catch {
    // ignore: 不支持/无权限/未满足用户手势等
  } finally {
    openPanel.value = null
  }
}

const miniPlayerStyle = computed(() => {
  if (!isMiniPlayer.value) return {}
  const x = Number(miniPosX.value)
  const y = Number(miniPosY.value)
  if (!Number.isFinite(x) || !Number.isFinite(y)) return {}
  return {
    left: `${x}px`,
    top: `${y}px`,
    right: 'auto',
    bottom: 'auto'
  }
})

const clampToViewport = (x, y) => {
  const vw = window.innerWidth || 0
  const vh = window.innerHeight || 0
  const maxX = Math.max(0, vw - MINI_PLAYER_W)
  const maxY = Math.max(0, vh - MINI_PLAYER_H)
  return {
    x: Math.max(0, Math.min(maxX, x)),
    y: Math.max(0, Math.min(maxY, y)),
  }
}

const onMiniDragStart = (event) => {
  if (!isMiniPlayer.value) return
  const rect = playerRef.value?.getBoundingClientRect()
  if (!rect) return
  miniDragging.value = true
  const startX = event.clientX
  const startY = event.clientY
  miniDragOffsetX = startX - rect.left
  miniDragOffsetY = startY - rect.top

  miniDragMoveHandler = (e) => {
    if (!miniDragging.value) return
    const nextX = e.clientX - miniDragOffsetX
    const nextY = e.clientY - miniDragOffsetY
    const clamped = clampToViewport(nextX, nextY)
    miniPosX.value = clamped.x
    miniPosY.value = clamped.y
  }
  miniDragUpHandler = () => {
    miniDragging.value = false
    if (miniDragMoveHandler) {
      window.removeEventListener('mousemove', miniDragMoveHandler)
      miniDragMoveHandler = null
    }
    if (miniDragUpHandler) {
      window.removeEventListener('mouseup', miniDragUpHandler)
      miniDragUpHandler = null
    }
  }

  window.addEventListener('mousemove', miniDragMoveHandler)
  window.addEventListener('mouseup', miniDragUpHandler)
}

const toggleWidescreen = () => {
  const next = !isWidescreen.value
  // 三种显示状态互斥：宽屏 / 网页全屏 / 全屏
  isWebFullscreen.value = false
  if (document.fullscreenElement) {
    document.exitFullscreen().catch(() => {})
  }
  isFullscreenActive.value = false
  isWidescreen.value = next
  openPanel.value = null
}

const toggleWebFullscreen = () => {
  const next = !isWebFullscreen.value
  // 三种显示状态互斥：宽屏 / 网页全屏 / 全屏
  isWidescreen.value = false
  if (document.fullscreenElement) {
    document.exitFullscreen().catch(() => {})
  }
  isFullscreenActive.value = false
  isWebFullscreen.value = next
  openPanel.value = null
}

const enterFullscreen = async () => {
  const el = playerRef.value
  if (!el) return
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
      isFullscreenActive.value = false
    } else {
      // 三种显示状态互斥：进入全屏前先退出宽屏/网页全屏
      isWidescreen.value = false
      isWebFullscreen.value = false
      await el.requestFullscreen()
      isFullscreenActive.value = true
    }
  } catch {
    // ignore
  } finally {
    openPanel.value = null
  }
}

const pipTipText = computed(() => `${isPictureInPicture.value ? '退出' : ''}开启画中画`)
const widescreenTipText = computed(() => `${isWidescreen.value ? '退出' : ''}宽屏模式`)
const webFullscreenTipText = computed(() => `${isWebFullscreen.value ? '退出' : ''}网页全屏`)
const fullscreenTipText = computed(() => `${isFullscreenActive.value ? '退出' : ''}进入全屏（f）`)

const onGlobalClickCloseCtrlPanels = (event) => {
  const target = event?.target
  if (!target) return
  if (ctrlGroupRef.value && ctrlGroupRef.value.contains(target)) return
  openPanel.value = null
}

const onEnterPictureInPicture = () => {
  isPictureInPicture.value = true
}

const onLeavePictureInPicture = () => {
  isPictureInPicture.value = false
}

const onFullscreenChange = () => {
  isFullscreenActive.value = Boolean(document.fullscreenElement)
}

const currentTimeText = computed(() => formatVideoTime(currentVideoTimeSec.value))
const durationText = computed(() => {
  return formatVideoTime(getVideoDurationSec())
})

const togglePlayPause = () => {
  if (!videoPlayer.value) return
  if (videoPlayer.value.paused) {
    videoPlayer.value.play().catch(() => {})
  } else {
    videoPlayer.value.pause()
  }
}

const seekByClientX = (clientX) => {
  if (!videoPlayer.value || !progressBarRef.value) return
  const duration = getVideoDurationSec()
  if (!duration) return
  const rect = progressBarRef.value.getBoundingClientRect()
  if (!rect.width) return
  const ratio = clampValue((clientX - rect.left) / rect.width, 0, 1)
  const t = duration * ratio
  videoPlayer.value.currentTime = t
  currentVideoTimeSec.value = t
  miniProgressPercent.value = Math.max(0, Math.min(100, ratio * 100))
}

const onProgressBarClick = (event) => {
  seekByClientX(event.clientX)
}

let progressDragMoveHandler = null
let progressDragUpHandler = null
const onProgressBarMouseDown = (event) => {
  event.preventDefault()
  seekByClientX(event.clientX)
  progressDragMoveHandler = (e) => seekByClientX(e.clientX)
  progressDragUpHandler = () => {
    if (progressDragMoveHandler) {
      window.removeEventListener('mousemove', progressDragMoveHandler)
      progressDragMoveHandler = null
    }
    if (progressDragUpHandler) {
      window.removeEventListener('mouseup', progressDragUpHandler)
      progressDragUpHandler = null
    }
  }
  window.addEventListener('mousemove', progressDragMoveHandler)
  window.addEventListener('mouseup', progressDragUpHandler)
}

// 视频时间更新事件
const onTimeUpdate = () => {
  if (!videoPlayer.value) return

  const currentTime = videoPlayer.value.currentTime || 0
  currentVideoTimeSec.value = currentTime
  const duration = videoPlayer.value.duration || 0
  if (duration > 0) {
    miniProgressPercent.value = Math.max(0, Math.min(100, (currentTime / duration) * 100))
  }
  const now = Date.now()

  // seek 检测：拖动进度条/跳播时，重置队列与去重集合，保证弹幕能在新时间轴重新出现
  if (Math.abs(currentTime - lastVideoTimeSec) > 2.5) {
    // 只清理运行态，不影响后端数据
    danmakuItems.value = []
    pendingDanmakuQueue.value = []
    loadedDanmakuKeys.clear()
    lastDanmakuFetchTimestamp = 0
    danmakuTrackCursor = 0
    danmakuTrackStateMap.clear()
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

  const duration = videoPlayer.value.duration || 0
  const currentTime = videoPlayer.value.currentTime || 0
  currentVideoTimeSec.value = currentTime
  playbackRate.value = Number(videoPlayer.value.playbackRate || 1) || 1
  volume.value = Number(videoPlayer.value.volume ?? 1) || 1
  if (duration > 0) {
    miniProgressPercent.value = Math.max(0, Math.min(100, (currentTime / duration) * 100))
  }
  
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
  if (watchHeartbeatTimer) {
    clearInterval(watchHeartbeatTimer)
    watchHeartbeatTimer = null
  }
  window.removeEventListener('resize', updateDanmakuListHeight)
  if (danmakuSettingsCloseTimer) {
    window.clearTimeout(danmakuSettingsCloseTimer)
    danmakuSettingsCloseTimer = null
  }
  if (previewSeekTimer) {
    window.clearTimeout(previewSeekTimer)
    previewSeekTimer = null
  }
  if (progressDragMoveHandler) {
    window.removeEventListener('mousemove', progressDragMoveHandler)
    progressDragMoveHandler = null
  }
  if (progressDragUpHandler) {
    window.removeEventListener('mouseup', progressDragUpHandler)
    progressDragUpHandler = null
  }
  if (miniDragMoveHandler) {
    window.removeEventListener('mousemove', miniDragMoveHandler)
    miniDragMoveHandler = null
  }
  if (miniDragUpHandler) {
    window.removeEventListener('mouseup', miniDragUpHandler)
    miniDragUpHandler = null
  }
  window.removeEventListener('mousedown', onGlobalClickCloseSenderSettings)
  window.removeEventListener('mousedown', onGlobalClickCloseCtrlPanels)
  const v = videoPlayer.value
  if (v) {
    v.removeEventListener('enterpictureinpicture', onEnterPictureInPicture)
    v.removeEventListener('leavepictureinpicture', onLeavePictureInPicture)
  }
  window.removeEventListener('fullscreenchange', onFullscreenChange)
})
</script>

<style lang="scss">
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
  box-sizing: border-box;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  grid-template-rows: auto auto;
  gap: 16px;
  width: 86%;
  max-width: 1800px;
  margin: 0 auto;
  padding: 32px 16px 16px;

  .player-main {
    grid-column: 1;
    grid-row: 1;
  }

  .content-main {
    grid-column: 1;
    grid-row: 2;
  }

  .sidebar {
    grid-column: 2;
    grid-row: 1 / span 2;
  }

  &.is-widescreen {
    width: 80%;
    max-width: 80%;
    grid-template-columns: minmax(0, 1fr) 360px;
    grid-template-rows: auto auto;

    .player-main {
      grid-column: 1 / -1;
      grid-row: 1;
    }

    .content-main {
      grid-column: 1;
      grid-row: 2;
    }

    .sidebar {
      grid-column: 2;
      grid-row: 2;
    }
  }

  &.is-web-fullscreen {
    width: 100%;
    max-width: none;
    padding: 0;
  }

  .player-main {
    display: flex;
    flex-direction: column;
    // gap: 12px;

    .player-shell {
      position: relative;
      width: 100%;
      max-width: 100%;
      aspect-ratio: 16 / 9;
      background: #000;
      overflow: hidden;
    }

    .player-shell > .player {
      position: absolute;
      inset: 0;
    }

    .player {
      background: #000;
      // border-radius: 8px;
      overflow: hidden;
             position: relative;
             aspect-ratio: auto;

      .player-corner-time {
        position: absolute;
        right: 12px;
        bottom: 12px;
        z-index: 8;
        font-size: 13px;
        line-height: 1.2;
        font-variant-numeric: tabular-nums;
        color: #fff;
        pointer-events: none;
        text-shadow:
          0 1px 2px rgba(0, 0, 0, 0.75),
          0 0 1px rgba(0, 0, 0, 0.85);
        transition: bottom 0.18s ease;
      }

      &:hover:not(.is-mini-player) .player-corner-time {
        bottom: 54px;
      }

      &.is-mini-player .player-corner-time {
        right: 8px;
        bottom: 8px;
        font-size: 11px;
      }

      &.is-web-fullscreen {
        position: fixed;
        inset: 0;
        width: 100vw;
        height: 100vh;
        aspect-ratio: auto;
        z-index: 9999;
        background: #000;
      }

      .player-controls {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 10;
        padding: 8px 14px 10px;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0));
      }

      // 鼠标移出时：隐藏底部控件区，但保留蓝色进度条
      &:not(:hover) .controls-bottom-row {
        display: none;
      }

      &:not(:hover) .player-controls {
        padding: 0;
        background: transparent;
      }

      &:not(:hover) .progress-bar-wrap {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 2px;
      }

      &:not(:hover) .progress-bar-thumb,
      &:not(:hover) .hover-preview-box {
        display: none;
      }

      .player-controls.is-idle {
        padding: 8px 14px 10px;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0));
      }

      .player-controls.is-idle .controls-bottom-row {
        display: none;
      }

      .player-controls.is-idle .progress-bar-wrap {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 2px;
      }

      .player-controls.is-idle .progress-bar-bg {
        background: rgba(255, 255, 255, 0.32);
      }

      .player-controls.is-idle .progress-bar-fill {
        background: #409eff;
      }

      .player-controls.is-idle .progress-bar-thumb,
      .player-controls.is-idle .hover-preview-box {
        display: none;
      }

      .progress-bar-wrap {
        position: relative;
        height: 3px;
        cursor: pointer;
      }

      .progress-bar-bg {
        position: absolute;
        inset: 0;
        border-radius: 0;
        background: rgba(255, 255, 255, 0.24);
      }

      .progress-bar-fill {
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        border-radius: 0;
        background: #00aeec;
      }

      .progress-bar-thumb {
        position: absolute;
        top: 50%;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #fff;
        border: 1px solid rgba(0, 174, 236, 0.86);
        transform: translate(-50%, -50%);
      }

      .controls-bottom-row {
        margin-top: 8px;
        display: flex;
        align-items: center;
        gap: 10px;
        color: #fff;
        font-size: 14px;
        line-height: 1;
      }

      .control-play-btn {
        border: none;
        background: transparent;
        color: #fff;
        cursor: pointer;
        padding: 0;
        width: 22px;
        height: 22px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }

      .control-play-btn svg {
        width: 18px;
        height: 18px;
        fill: currentColor;
        display: block;
      }

      .control-time {
        font-size: 14px;
        color: #fff;
      }

      .controls-spacer {
        flex: 1;
      }

      .control-item {
        color: #fff;
        opacity: 0.9;
        font-size: 14px;
      }

      .ctrl-group {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        position: relative;
      }

      .ctrl-text-btn {
        width: 32px;
        height: 22px;
        border: 1px solid transparent;
        background: transparent;
        color: rgba(255, 255, 255, 0.92);
        font-size: 14px;
        cursor: pointer;
        padding: 0;
        line-height: 22px;
        text-align: center;
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }

      .ctrl-icon-wrap {
        position: relative;
        display: inline-flex;
        align-items: center;
      }

      .ctrl-icon-btn {
        width: 32px;
        height: 22px;
        border: none;
        background: transparent;
        color: rgba(255, 255, 255, 0.92);
        cursor: pointer;
        padding: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }

      .ctrl-icon-btn svg {
        width: 18px;
        height: 18px;
        fill: currentColor;
        display: block;
      }

      .ctrl-icon-btn.has-tooltip {
        position: relative;
      }

      .ctrl-icon-btn.has-tooltip:hover::after {
        content: attr(data-tip);
        position: absolute;
        left: 50%;
        bottom: calc(100% + 8px);
        transform: translateX(-50%);
        white-space: nowrap;
        padding: 6px 10px;
        font-size: 13px;
        color: #fff;
        background: rgba(0, 0, 0, 0.86);
        border-radius: 0;
        line-height: 1;
        pointer-events: none;
        z-index: 40;
      }

      .ctrl-icon-btn.has-tooltip:hover::before {
        content: '';
        position: absolute;
        left: 50%;
        bottom: calc(100% + 2px);
        transform: translateX(-50%);
        width: 0;
        height: 0;
        border-left: 5px solid transparent;
        border-right: 5px solid transparent;
        border-top: 6px solid rgba(0, 0, 0, 0.86);
        pointer-events: none;
        z-index: 40;
      }

      .ctrl-panel {
        position: absolute;
        bottom: calc(100% + 10px);
        left: 50%;
        transform: translateX(-50%);
        background: rgba(0, 0, 0, 0.78);
        border: 1px solid rgba(255, 255, 255, 0.08);
        color: #fff;
        padding: 10px 0;
        min-width: 160px;
        z-index: 30;
      }

      .ctrl-panel-item {
        width: 100%;
        padding: 10px 14px;
        border: none;
        background: transparent;
        color: rgba(255, 255, 255, 0.92);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 14px;
      }

      .ctrl-panel-item:hover {
        background: rgba(255, 255, 255, 0.08);
      }

      .ctrl-panel-item.is-active {
        color: #00aeec;
      }

      .vip-tag {
        background: #ff5c88;
        color: #fff;
        border-radius: 999px;
        padding: 2px 8px;
        font-size: 12px;
      }

      .ctrl-panel-speed {
        min-width: 110px;
      }

      .ctrl-panel-volume {
        min-width: 30px;
        width: 30px;
        height: 122px;
        padding: 6px 0 4px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: flex-start;
      }

      .volume-value {
        text-align: center;
        font-size: 11px;
        line-height: 1;
        margin: 0 0 4px;
        font-weight: 500;
      }

      .volume-slider {
        -webkit-appearance: none;
        appearance: none;
        width: 92px;
        height: 2px;
        transform: rotate(-90deg);
        transform-origin: 50% 50%;
        margin: 50px 0 0;
        cursor: pointer;
        background: transparent;
      }

      .volume-slider:focus,
      .volume-slider:active {
        outline: none;
        box-shadow: none;
      }

      .volume-slider::-webkit-slider-runnable-track {
        height: 2px;
        background: #ffffff;
        border: none;
      }

      .volume-slider::-webkit-slider-thumb {
        -webkit-appearance: none;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: #ffffff;
        border: none;
        margin-top: -5px;
      }

      .volume-slider:active::-webkit-slider-thumb {
        background: #ffffff;
      }

      .volume-slider::-moz-range-track {
        height: 2px;
        background: #ffffff;
        border: none;
      }

      .volume-slider::-moz-range-progress {
        height: 2px;
        background: #ffffff;
        border: none;
      }

      .volume-slider::-moz-range-thumb {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: #ffffff;
        border: none;
      }

      .video {
        width: 100%;
        height: 100%;
        display: block;
        background: #000;
        accent-color: #00aeec;
      }

      &.is-mini-player {
        position: fixed !important;
        right: auto;
        bottom: auto;
        width: 360px;
        height: 202px;
        inset: auto;
        z-index: 10000;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.45);
        border: 1px solid rgba(255, 255, 255, 0.12);
      }

      .mini-close-btn {
        position: absolute;
        right: 8px;
        top: 8px;
        width: 26px;
        height: 26px;
        border: none;
        border-radius: 0;
        background: rgba(0, 0, 0, 0.55);
        color: #fff;
        cursor: pointer;
        z-index: 10001;
        line-height: 26px;
        text-align: center;
        font-size: 18px;
      }

      .mini-close-btn:hover {
        background: rgba(0, 0, 0, 0.75);
      }

      .mini-drag-handle {
        position: absolute;
        left: 0;
        top: 0;
        right: 0;
        height: 34px;
        cursor: move;
        z-index: 10000;
        background: linear-gradient(to bottom, rgba(0, 0, 0, 0.35), rgba(0, 0, 0, 0));
      }

      .video::-webkit-media-controls-timeline {
        accent-color: #00aeec;
      }

      .video::-webkit-media-controls-current-time-display,
      .video::-webkit-media-controls-time-remaining-display {
        color: #fff;
      }

      .hover-preview-box {
        position: absolute;
        bottom: calc(100% + 10px);
        width: 180px;
        height: 102px;
        border-radius: 2px;
        border: 1px solid rgba(255, 255, 255, 0.22);
        background: #000;
        overflow: hidden;
        pointer-events: none;
        z-index: 9;
      }

      .hover-preview-video {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .hover-preview-time {
        position: absolute;
        left: 50%;
        bottom: 8px;
        transform: translateX(-50%);
        min-width: 50px;
        text-align: center;
        padding: 2px 8px;
        border-radius: 2px;
        font-size: 14px;
        line-height: 1.2;
        color: #fff;
        background: rgba(0, 0, 0, 0.62);
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
        box-sizing: border-box;
        padding: 0 4px;
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
          transform: translateX(calc(-1 * var(--danmaku-travel-distance, 1200px)));
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
      // margin-top: -12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      // border: 1px solid #e3e5e7;

      .danmaku-info {
        display: flex;
        align-items: center;
        justify-content: flex-start;
        gap: 12px;
        white-space: nowrap;
        flex: 0 1 auto;
        min-width: 0;

        .danmaku-stats {
          display: flex;
          align-items: center;
          gap: 16px;
          font-size: 13px;
          color: #61666d;
          min-width: 0;
          overflow: hidden;

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
          position: relative;

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

          .danmaku-settings-wrap {
            position: relative;
            display: inline-flex;
            align-items: center;
            justify-content: center;
          }

          .danmaku-settings-panel {
            position: absolute;
            bottom: calc(100% + 10px);
            left: 50%;
            transform: translateX(-50%);
            width: 300px;
            padding: 14px 14px 10px;
            background: rgba(0, 0, 0, 0.78);
            border-radius: 0;
            z-index: 20;
            color: #fff;
          }

          .setting-row {
            display: grid;
            grid-template-columns: 68px 1fr auto;
            align-items: center;
            column-gap: 12px;
            margin-bottom: 12px;
          }

          .setting-label {
            font-size: 14px;
            color: #fff;
            white-space: nowrap;
          }

          .setting-value {
            min-width: 46px;
            text-align: right;
            color: #fff;
            font-size: 14px;
          }

          .setting-slider-wrap {
            position: relative;
            height: 16px;
            display: flex;
            align-items: center;
          }

          .setting-slider {
            width: 100%;
            -webkit-appearance: none;
            appearance: none;
            height: 4px;
            border-radius: 999px;
            outline: none;
            margin: 0;
          }

          .setting-slider::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            width: 14px;
            height: 14px;
            border-radius: 50%;
            border: none;
            background: #fff;
            cursor: pointer;
          }

          .setting-slider::-moz-range-thumb {
            width: 14px;
            height: 14px;
            border-radius: 50%;
            border: none;
            background: #fff;
            cursor: pointer;
          }

          .setting-slider::-moz-range-track {
            height: 4px;
            border: none;
            border-radius: 999px;
            background: transparent;
          }

          .slider-with-marks {
            position: relative;
            z-index: 2;
          }

          .slider-marks {
            position: absolute;
            left: 2px;
            right: 2px;
            top: 50%;
            transform: translateY(-50%);
            pointer-events: none;
            display: flex;
            justify-content: space-between;
            z-index: 3;
          }

          .slider-mark {
            width: 5px;
            height: 5px;
            border-radius: 50%;
            background: #fff;
            opacity: 0.92;
          }
        }
      }

      .danmaku-input-wrapper {
        display: flex;
        align-items: center;
        gap: 0;
        flex: 1;
        min-width: 0;
        max-width: 100%;

        .sender-settings-wrap {
          position: absolute;
          // left: 8px;
          top: 50%;
          transform: translateY(-50%);
          z-index: 2;
        }

        .sender-settings-btn {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          border: none;
          background: transparent;
          color: #61666d;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            color: #00a1d6;
          }

          &.is-active {
            color: #00a1d6;
          }

          &.is-disabled {
            color: #c9ccd0;
            background: transparent;
            cursor: not-allowed;
          }

          svg {
            width: 18px;
            height: 18px;
            fill: currentColor;
          }
        }

        .sender-settings-panel {
          position: absolute;
          left: 0;
          bottom: calc(100% + 6px);
          width: 300px;
          padding: 12px;
          border-radius: 8px;
          background: rgba(0, 0, 0, 0.82);
          z-index: 21;
          color: #fff;
        }

        .sender-setting-row + .sender-setting-row {
          margin-top: 12px;
        }

        .sender-setting-label {
          display: block;
          margin-bottom: 8px;
          font-size: 14px;
          font-weight: 600;
        }

        .sender-size-options {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 8px;
        }

        .sender-size-btn {
          height: 32px;
          border: none;
          border-radius: 4px;
          color: #fff;
          background: rgba(255, 255, 255, 0.18);
          cursor: pointer;
          font-size: 15px;
          font-weight: 600;

          &.is-active {
            background: #18b8ff;
          }
        }

        .sender-color-wrap {
          display: grid;
          grid-template-columns: 1fr 58px;
          gap: 8px;
          align-items: center;
          margin-bottom: 8px;
        }

        .sender-color-input {
          height: 30px;
          border-radius: 4px;
          border: 1px solid rgba(255, 255, 255, 0.2);
          background: rgba(0, 0, 0, 0.25);
          color: #fff;
          padding: 0 10px;
          font-size: 14px;
          outline: none;
        }

        .sender-color-preview {
          height: 30px;
          border-radius: 4px;
          border: 1px solid rgba(255, 255, 255, 0.22);
        }

        .sender-color-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 8px;
        }

        .sender-color-btn {
          width: 100%;
          aspect-ratio: 1 / 1;
          border-radius: 4px;
          border: 1px solid rgba(255, 255, 255, 0.2);
          cursor: pointer;
          padding: 0;

          &.is-active {
            box-shadow: 0 0 0 2px #18b8ff inset;
          }
        }

        .danmaku-input-box {
          position: relative;
          flex: 1;
          min-width: 0;
        }

        .danmaku-input {
          width: 95%;
          height: 32px;
          padding: 0 0px 0 30px;
          border-radius: 8px 0 0 8px;
          border: 1px solid #e3e5e7;
          background: #f6f7f8;
          outline: none;
          font-size: 14px;
          color: #18191c;

          &::placeholder {
            color: #9499a0;
          }

          &:focus {
            background: #f6f7f8;
            border-color: #e3e5e7;
            box-shadow: none;
          }

          &:disabled {
            cursor: not-allowed;
            color: #9499a0;
          }
        }

        .danmaku-etiquette-text {
          position: absolute;
          right: 12px;
          top: 50%;
          transform: translateY(-50%);
          color: #61666d;
          cursor: pointer;
          font-size: 13px;
          white-space: nowrap;
          user-select: none;

          &:hover {
            color: #00a1d6;
          }
        }

        .danmaku-send-btn {
          flex-shrink: 0;
          height: 32px;
          padding: 0 20px;
          margin-left: -1px;
          border-radius: 0 8px 8px 0;
          border: none;
          background: #00a1d6;
          color: #fff;
          display: inline-flex;
          align-items: center;
          justify-content: center;
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
            background: #00a1d6;
            color: #fff;
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
        display: inline-flex;
        align-items: center;
        gap: 6px;
        user-select: none;

        &:hover {
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
          color: #00a1d6;
        }
      }
    }

    .video-meta {
      margin-bottom: 18px;
      .title-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 6px;
      }
      .title {
        margin: 0;
        font-size: 20px;
        color: #18191c;
        font-weight: 500;
      }
      .uploader-inline {
        display: inline-grid;
        grid-template-columns: 36px minmax(0, 1fr) auto;
        gap: 10px;
        align-items: center;
        min-width: 0;
        max-width: 420px;

        .u-avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          object-fit: cover;
        }

        .u-avatar-placeholder {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: #d8d8d8;
        }

        .u-info {
          min-width: 0;
        }

        .u-name {
          font-weight: 600;
          color: #18191c;
          font-size: 14px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
      .meta-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        flex-wrap: nowrap;

        .meta-left {
          display: flex;
          flex-wrap: nowrap;
          gap: 12px;
          font-size: 13px;
          color: #9499a0;
          white-space: nowrap;

          .meta-item {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            line-height: 1;

            .view-icon,
            .dm-icon {
              width: 20px;
              height: 20px;
              flex: 0 0 20px;
              display: block;
            }

            .copyright-icon {
              width: 16px;
              height: 16px;
              flex: 0 0 16px;
              display: block;
              color: #fd676f;
            }

            span {
              line-height: 1;
              display: inline-block;
            }
          }

          .upload-time {
            margin-left: 4px;
          }

          .copyright {
            margin-left: 10px;
            color: #9499a0;
            font-size: 12px;
            user-select: none;
            white-space: nowrap;
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
      .intro {
        margin: 0;
        color: #18191c;
        line-height: 1.6;
        font-size: 15px;
        font-family:
          'PingFang SC',
          'HarmonyOS Sans SC',
          'HarmonyOS Sans',
          'Microsoft YaHei',
          sans-serif;
      }

      .tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 16px;
      }

      .tag-link {
        box-sizing: border-box;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        height: 28px;
        padding: 0 12px;
        border-radius: 15px;
        background: #f1f2f3;
        color: #61666d;
        font-size: 13px;
        font-weight: 400;
        line-height: 1;
        font-family:
          'PingFang SC',
          'HarmonyOS Sans SC',
          'HarmonyOS Sans',
          'Microsoft YaHei',
          sans-serif;
        text-decoration: none;
        white-space: nowrap;

        &:visited {
          color: #61666d;
        }

        // &:hover {
        //   background: #e3e5e7;
        //   color: #61666d;
        // }
      }
    }

  }

  .content-main {
    min-width: 0;

    .desc {
      background: #fff;
      border-radius: 8px;
      padding: 12px 0;
      border-bottom: 1px solid var(--line_regular, #E3E5E7);

      .intro {
        margin: 0;
        color: #18191c;
        line-height: 1.6;
        font-size: 15px;
        font-family:
          'PingFang SC',
          'HarmonyOS Sans SC',
          'HarmonyOS Sans',
          'Microsoft YaHei',
          sans-serif;
      }

      .tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-top: 16px;
      }

      .tag-link {
        box-sizing: border-box;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        height: 28px;
        padding: 0 12px;
        border-radius: 15px;
        background: #f1f2f3;
        color: #61666d;
        font-size: 13px;
        font-weight: 400;
        line-height: 1;
        font-family:
          'PingFang SC',
          'HarmonyOS Sans SC',
          'HarmonyOS Sans',
          'Microsoft YaHei',
          sans-serif;
        text-decoration: none;
        white-space: nowrap;

        &:visited {
          color: #61666d;
        }

        &:hover {
          background: #e3e5e7;
          color: #61666d;
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
      }

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

    // 弹幕列表区域
    .danmaku-list-section {
      background: #fff;
      // border-radius: 8px;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      &.is-expanded {
        /* 只限制最大高度，不强行撑满 */
        max-height: var(--danmaku-max-height);
      }

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
        /* 内容自适应高度，避免在容器有 max-height 时被 flex:1 撑出空白 */
        flex: 0 0 auto;
      }

      .danmaku-table {
        display: flex;
        flex-direction: column;
        flex: 0 0 auto;

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
          overflow-y: auto;
          /* 当弹幕很多时在这里滚动；弹幕很少时高度随内容，推荐区不会被顶下去 */
          max-height: calc(var(--danmaku-max-height, 0px) - 120px);

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
        grid-template-columns: 140px 1fr;
        gap: 6px;
        align-items: center;
        background: #fff;
        border-radius: 8px;
        padding: 6px 6px 6px 0;
        .rec-cover { width: 140px; height: 80px; border-radius: 6px; object-fit: cover; }
        .rec-info {
          .rec-title {
            font-size: 15px;
            color: #18191c;
            line-height: 1.3;
            display: -webkit-box;
            line-clamp: 2;
            -webkit-line-clamp: 2; /* WebKit 浏览器：最多两行 */
            -webkit-box-orient: vertical;
            overflow: hidden;
            word-break: break-all;
            flex-shrink: 0; // 防止被 rec-info 固定高度压缩后裁掉底部
          }
          .rec-meta { color: #8a8a8a; display: flex; gap: 12px; margin-top: 0; font-size: 13px; flex-shrink: 0; }
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
    grid-template-rows: auto auto auto;
    .player-main {
      grid-column: 1;
      grid-row: 1;
    }
    .content-main {
      grid-column: 1;
      grid-row: 2;
    }
    .sidebar {
      grid-column: 1;
      grid-row: 3;
    }
  }
}
</style>