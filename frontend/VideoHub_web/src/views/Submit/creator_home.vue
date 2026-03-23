<template>
  <div class="creator-home">
    <section class="hero">
      <div class="hero-media">
        <img class="hero-img" src="/assets/creator-center.png" alt="" />
        <div class="hero-scrim" />
        <div class="hero-pattern" aria-hidden="true" />
      </div>
      <div class="hero-content">
        <p class="hero-kicker">CREATOR STUDIO</p>
        <h1 class="hero-title">欢迎来到创作中心</h1>
        <p class="hero-sub">开始你的创作之旅，分享你的精彩内容</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="ch-btn" @click="goSubmit">立即投稿</el-button>
          <el-button size="large" class="ch-btn" @click="goContentManagement">管理内容</el-button>
          <el-button size="large" class="ch-btn" @click="goDataCenter">数据中心</el-button>
          <el-button size="large" class="ch-btn" @click="goFans">粉丝管理</el-button>
        </div>
      </div>
    </section>

    <div v-if="!creatorId" class="login-hint">
      <el-alert type="warning" show-icon :closable="false" title="请先登录后查看创作数据" />
    </div>

    <template v-else>
      <section class="panel overview-panel">
        <div class="panel-head">
          <h2 class="panel-title">数据概览</h2>
          <span class="panel-hint">点击卡片快速进入对应模块</span>
        </div>
        <div v-if="overviewLoading" class="panel-skeleton">
          <el-skeleton :rows="1" animated />
        </div>
        <div v-else class="metric-strip">
          <button
            type="button"
            class="metric-tile metric-tile--videos"
            @click="goDataCenter"
          >
            <span class="metric-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.6">
                <rect x="3" y="5" width="14" height="12" rx="2" />
                <path d="M17 9l4-2v10l-4-2z" />
              </svg>
            </span>
            <span class="metric-label">总视频数</span>
            <span class="metric-num">{{ displayTotalVideos }}</span>
          </button>
          <button
            type="button"
            class="metric-tile metric-tile--plays"
            @click="goDataCenter"
          >
            <span class="metric-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.6">
                <path d="M8 5v14l11-7z" />
              </svg>
            </span>
            <span class="metric-label">总播放量</span>
            <span class="metric-num">{{ displayTotalViews }}</span>
          </button>
          <button
            type="button"
            class="metric-tile metric-tile--likes"
            @click="goDataCenter"
          >
            <span class="metric-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.6">
                <path d="M12 21s-7-4.35-7-10a4 4 0 0 1 7-2 4 4 0 0 1 7 2c0 5.65-7 10-7 10z" />
              </svg>
            </span>
            <span class="metric-label">总点赞数</span>
            <span class="metric-num">{{ displayTotalLikes }}</span>
          </button>
          <button
            type="button"
            class="metric-tile metric-tile--fans"
            @click="goFans"
          >
            <span class="metric-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.6">
                <circle cx="9" cy="7" r="3" />
                <circle cx="17" cy="9" r="2.5" />
                <path d="M3 20c0-3.5 4-5.5 6-5.5s6 2 6 5.5" />
                <path d="M14 20c0-2 2.5-3.5 4.5-3.5s3.5 1.2 3.5 3.5" />
              </svg>
            </span>
            <span class="metric-label">粉丝数</span>
            <span class="metric-num">{{ displayTotalFollowers }}</span>
          </button>
        </div>
      </section>

      <section class="panel recent-panel">
        <div class="panel-head">
          <h2 class="panel-title">最近作品</h2>
          <el-button type="primary" plain class="ch-btn" @click="goContentManagement">查看全部</el-button>
        </div>
        <div v-if="recentLoading" class="panel-skeleton">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="recentVideos.length === 0" class="recent-empty">
          <el-empty description="暂无已发布视频，去投稿试试吧" />
        </div>
        <div v-else class="video-grid">
          <article
            v-for="video in recentVideos"
            :key="video.videoId"
            class="video-card"
            @click="openVideo(video.videoId)"
          >
            <div class="video-thumb">
              <img :src="video.thumbnail" :alt="video.title" loading="lazy" />
              <span class="video-duration">{{ video.duration }}</span>
              <div class="video-shade" />
            </div>
            <div class="video-body">
              <h3 class="video-title">{{ video.title }}</h3>
              <p class="video-stats">
                <span>{{ video.views }} 播放</span>
                <span class="dot">·</span>
                <span>{{ video.likes }} 点赞</span>
                <span class="dot">·</span>
                <span>{{ video.publishDate }}</span>
              </p>
            </div>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { fetchCreatorOverview } from '@/api/creatorStats'
import { fetchVideosByUploader } from '@/api/video'

const emit = defineEmits(['switch-view'])
const router = useRouter()
const userStore = useUserStore()

const creatorId = computed(() => userStore.user?.userId ?? userStore.user?.id ?? null)

const overviewLoading = ref(false)
const recentLoading = ref(false)

const coreMetrics = ref({
  videoCount: 0,
  plays: 0,
  likes: 0,
  newFans: 0
})

const recentVideos = ref([])

const formatCompact = (n) => {
  const num = Number(n) || 0
  if (num >= 100000000) return `${(num / 100000000).toFixed(1).replace(/\.0$/, '')}亿`
  if (num >= 10000) return `${(num / 10000).toFixed(1).replace(/\.0$/, '')}万`
  return String(num)
}

const displayTotalVideos = computed(() => formatCompact(coreMetrics.value.videoCount))
const displayTotalViews = computed(() => formatCompact(coreMetrics.value.plays))
const displayTotalLikes = computed(() => formatCompact(coreMetrics.value.likes))
const displayTotalFollowers = computed(() => formatCompact(coreMetrics.value.newFans))

const normalizeCover = (url) => {
  if (!url) return '/assets/channel/live.png'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/local-videos/')) return url
  if (url.startsWith('/')) {
    if (url.startsWith('/uploads/') || url.startsWith('/uploads\\')) {
      return '/local-videos' + url
    }
    return url
  }
  if (url.startsWith('uploads/') || url.startsWith('uploads\\')) {
    return '/local-videos/' + url.replaceAll('\\', '/')
  }
  return '/local-videos/' + url.replaceAll('\\', '/')
}

const formatDuration = (seconds) => {
  const s = Number(seconds)
  if (!Number.isFinite(s) || s <= 0) return '--:--'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${String(sec).padStart(2, '0')}`
}

const formatPublishDate = (createTime) => {
  if (!createTime) return ''
  const str = String(createTime)
  return str.length >= 10 ? str.slice(0, 10) : str
}

const loadOverview = async () => {
  const id = creatorId.value
  if (!id) return
  overviewLoading.value = true
  try {
    const { data } = await fetchCreatorOverview({
      creatorId: id,
      range: 'all'
    })
    if (data?.success && data.data?.coreMetrics) {
      const c = data.data.coreMetrics
      coreMetrics.value = {
        videoCount: Number(c.videoCount) || 0,
        plays: Number(c.plays) || 0,
        likes: Number(c.likes) || 0,
        newFans: Number(c.newFans) || 0
      }
    }
  } catch {
    // 请求层已提示
  } finally {
    overviewLoading.value = false
  }
}

const loadRecentVideos = async () => {
  const id = creatorId.value
  if (!id) return
  recentLoading.value = true
  try {
    const { data } = await fetchVideosByUploader(id, 12, null)
    const list = data?.list || data || []
    recentVideos.value = (Array.isArray(list) ? list : []).map((v) => ({
      videoId: v.videoId,
      title: v.title || '未命名稿件',
      thumbnail: normalizeCover(v.coverUrl),
      duration: formatDuration(v.duration),
      views: formatCompact(v.viewCount ?? 0),
      likes: formatCompact(v.likeCount ?? 0),
      publishDate: formatPublishDate(v.createTime)
    }))
  } catch {
    recentVideos.value = []
  } finally {
    recentLoading.value = false
  }
}

const goSubmit = () => emit('switch-view', 'submit')
const goContentManagement = () => emit('switch-view', 'contentManagement')
const goDataCenter = () => emit('switch-view', 'dataCenter')
const goFans = () => emit('switch-view', 'fans')

const openVideo = (videoId) => {
  if (!videoId) return
  router.push({ path: `/video/${encodeURIComponent(videoId)}` })
}

const refreshData = () => {
  if (!creatorId.value) return
  loadOverview()
  loadRecentVideos()
}

onMounted(refreshData)

watch(creatorId, (id) => {
  if (id) refreshData()
  else {
    recentVideos.value = []
    coreMetrics.value = { videoCount: 0, plays: 0, likes: 0, newFans: 0 }
  }
})
</script>

<style lang="scss" scoped>
/* 与内容管理、数据中心等一致：纯白内容区 */
.creator-home {
  position: relative;
  padding: 20px 20px 28px;
  min-height: 100%;
  overflow: hidden;
  background: #fff;
}

.hero {
  position: relative;
  z-index: 1;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 22px;
  border: 1px solid #eef2f7;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.hero-media {
  position: relative;
  height: 232px;
}

.hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transform: scale(1.02);
}

.hero-scrim {
  position: absolute;
  inset: 0;
  /* 改为纯色背景：去掉渐变，避免图片透出 */
  background: #ffffff;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  opacity: 0;
  background-image: none;
}

.hero-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 36px;
  color: #0f172a;
  gap: 8px;
}

.hero-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.22em;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.6);
}

.hero-title {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.02em;
  text-shadow: 0 2px 18px rgba(15, 23, 42, 0.08);
}

.hero-sub {
  margin: 0;
  font-size: 15px;
  color: rgba(15, 23, 42, 0.78);
  max-width: 420px;
  line-height: 1.5;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

/* 五个操作按钮：圆角 5px；悬停/点击背景保持与常态一致，仅字色为品牌蓝（主色按钮保持白字） */
.ch-btn {
  border-radius: 5px !important;
}

/* 横幅：立即投稿（实心主色）— 背景/边框不变，字保持白色（蓝底上若改蓝字会看不清） */
.hero-actions :deep(.ch-btn.el-button--primary:not(.is-plain)) {
  background-color: #00a1d6 !important;
  border-color: #00a1d6 !important;
  color: #fff !important;
}
.hero-actions :deep(.ch-btn.el-button--primary:not(.is-plain):hover),
.hero-actions :deep(.ch-btn.el-button--primary:not(.is-plain):focus),
.hero-actions :deep(.ch-btn.el-button--primary:not(.is-plain):active) {
  background-color: #00a1d6 !important;
  border-color: #00a1d6 !important;
  color: #fff !important;
}

/* 横幅：白底默认按钮 — 背景不变，悬停/点击仅字色为 #00a1d6 */
.hero-actions :deep(.ch-btn.el-button--default) {
  background-color: #fff !important;
  border-color: #e5e7eb !important;
  color: #374151 !important;
}
.hero-actions :deep(.ch-btn.el-button--default:hover),
.hero-actions :deep(.ch-btn.el-button--default:focus),
.hero-actions :deep(.ch-btn.el-button--default:active) {
  background-color: #fff !important;
  border-color: #e5e7eb !important;
  color: #00a1d6 !important;
}

/* 最近作品：查看全部（plain 主色）— 背景不变，字色保持/强调为品牌蓝 */
.panel-head :deep(.ch-btn.el-button--primary.is-plain) {
  background-color: #fff !important;
  border-color: #00a1d6 !important;
  color: #00a1d6 !important;
}
.panel-head :deep(.ch-btn.el-button--primary.is-plain:hover),
.panel-head :deep(.ch-btn.el-button--primary.is-plain:focus),
.panel-head :deep(.ch-btn.el-button--primary.is-plain:active) {
  background-color: #fff !important;
  border-color: #00a1d6 !important;
  color: #00a1d6 !important;
}

.login-hint {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}

.panel {
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: 10px;
  padding: 20px 22px 22px;
  margin-bottom: 18px;
  border: 1px solid #eef2f7;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.panel-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  padding-left: 12px;
  border-left: 4px solid #00a1d6;
}

.panel-hint {
  font-size: 12px;
  color: #94a3b8;
}

.panel-skeleton {
  padding: 8px 0;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.metric-tile {
  text-align: left;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #eef0f4;
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font: inherit;
  color: inherit;
}
.metric-tile:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
  border-color: #e5e7eb;
}
.metric-tile:active {
  transform: none;
}
.metric-tile:focus-visible {
  outline: 2px solid #00a1d6;
  outline-offset: 2px;
}

.metric-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}
.metric-tile--videos .metric-icon {
  background: linear-gradient(135deg, #e0f2fe, #bae6fd);
  color: #0369a1;
}
.metric-tile--plays .metric-icon {
  background: linear-gradient(135deg, #fce7f3, #fbcfe8);
  color: #db2777;
}
.metric-tile--likes .metric-icon {
  background: linear-gradient(135deg, #ffedd5, #fed7aa);
  color: #c2410c;
}
.metric-tile--fans .metric-icon {
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  color: #6d28d9;
}

.metric-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.metric-num {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.recent-empty {
  padding: 32px 0;
  border-radius: 12px;
  background: #fafafa;
}

/* 最近作品：3 行 × 4 列（共 12 条） */
.video-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.video-card {
  border-radius: 5px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #eef0f4;
  cursor: pointer;
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}
.video-card:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
  border-color: #e5e7eb;
}
.video-card:active {
  transform: none;
}

.video-thumb {
  position: relative;
  aspect-ratio: 16 / 9;
  background: #f1f5f9;
}
.video-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.video-duration {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 7px;
  border-radius: 6px;
  background: rgba(15, 23, 42, 0.78);
  color: #fff;
}
.video-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 55%, rgba(15, 23, 42, 0.35) 100%);
  pointer-events: none;
}

.video-body {
  padding: 6px 10px 8px;
}
.video-title {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.video-stats {
  margin: 4px 0 0;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.3;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}
.video-stats .dot {
  opacity: 0.5;
}

@media (max-width: 900px) {
  .metric-strip {
    grid-template-columns: repeat(2, 1fr);
  }
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .creator-home {
    padding: 12px;
  }
  .hero-media {
    height: 200px;
  }
  .hero-content {
    padding: 0 20px;
  }
  .hero-title {
    font-size: 24px;
  }
  .hero-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .metric-strip {
    grid-template-columns: 1fr 1fr;
  }
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
