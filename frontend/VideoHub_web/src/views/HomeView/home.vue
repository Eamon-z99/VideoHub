<template>
  <div class="home">
    <header class="site-header">
      <div class="header-inner">
        <ul class="nav-left">
          <li class="nav-item" @click="goTo('/')">
            <img src="/assets/home.png" class="nav-icon" />
            <span>首页</span>
          </li>
          <li class="nav-item"><span>番剧</span></li>
          <li class="nav-item" @click="goTo('/live')"><span>直播</span></li>
          <li class="nav-item"><span>游戏中心</span></li>
          <li class="nav-item" @click="goTo('/mall')"><span>会员购</span></li>
          <li class="nav-item"><span>漫画</span></li>
          <li class="nav-item"><span>赛事</span></li>
          <li class="nav-item">
            <img src="/assets/download-client.png" class="nav-icon" />
            <span>下载客户端</span>
          </li>
        </ul>
        <div class="search">
          <input class="search-input" placeholder="搜索你感兴趣的内容" />
          <button class="search-btn">
            <!-- 🔍 -->
            <img src="/assets/search-button.png" class="search-btn-img"/>
          </button>
        </div>
        <div class="actions">
          <div class="avatar" @click="showLogin=true"/>
          <div class="action-col" @click="goTo('/vip')">
            <img src="/assets/vip.png" class="action-icon" /><span>大会员</span>
          </div>
          <div class="action-col" @click="goTo('/messages')">
            <img src="/assets/messages.png" class="action-icon" /><span>消息</span>
          </div>
          <div class="action-col" @click="goTo('/feed')">
            <img src="/assets/feed.png" class="action-icon" /><span>动态</span>
          </div>
          <div class="action-col" @click="goTo('/profile')">
            <img src="/assets/favorites.png" class="action-icon" /><span>收藏</span>
          </div>
          <div class="action-col" @click="goTo('/history')">
            <img src="/assets/history.png" class="action-icon" /><span>历史</span>
          </div>
          <div class="action-col" @click="navigateToCreatorCenter">
            <img src="/assets/creator-center.png" class="action-icon" /><span>创作中心</span>
          </div>
          <button class="primary" @click="goTo('/submitHome?view=submit')">投稿</button>
        </div>
      </div>
      <img class="header-bg" src="/assets/header.png" alt="banner" />
    </header>
    <section class="navigation-section">
      <div class="nav-left-section">
        <div class="nav-left-top">
          <div class="quick-icons">
            <div class="qi-item" @click="goTo('/feed')">
              <div class="qi-circle orange">
                <img src="/assets/feed.png" class="qi-icon" />
              </div>
              <div class="qi-text">动态</div>
            </div>
            <div class="qi-item">
              <div class="qi-circle pink">
                <img src="/assets/trending.png" class="qi-icon" />
              </div>
              <div class="qi-text">热门</div>
            </div>
          </div>

          <div class="grid-container">
            <a href="#" v-for="c in categories" :key="c" class="grid-item">{{ c }}</a>
          </div>
        </div>
      </div>
      
      <div class="nav-right-section">
        <div class="utility-links">
          <div class="utility-row">
            <div class="utility-item" @click="goTo('/column')">
              <img src="/assets/channel/column.png" class="utility-icon" />
              <span class="utility-text">专栏</span>
            </div>
            <div class="utility-item">
              <img src="/assets/channel/activity.png" class="utility-icon" />
              <span class="utility-text">活动</span>
            </div>
            <div class="utility-item" @click="goTo('/community')">
              <img src="/assets/channel/community.png" class="utility-icon" />
              <span class="utility-text">社区中心</span>
            </div>
          </div>
          <div class="utility-row">
            <div class="utility-item" @click="goTo('/live')">
              <img src="/assets/channel/live.png" class="utility-icon" />
              <span class="utility-text">直播</span>
            </div>
            <div class="utility-item" @click="goTo('/classroom')">
              <img src="/assets/channel/class.png" class="utility-icon" />
              <span class="utility-text">课堂</span>
            </div>
            <div class="utility-item" @click="goTo('/music-chart')">
              <img src="/assets/channel/new-songs-chart.png" class="utility-icon" />
              <span class="utility-text">新歌热榜</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="hero-grid">
      <div class="banner">
        <div class="slider" :style="{ transform: `translateX(-${slideIndex * 100}%)` }">
          <div v-for="(s, i) in slides" :key="i" class="slide">
            <img :src="s.src" :alt="s.alt" />
            <div class="slide-caption">{{ s.caption }}</div>
          </div>
        </div>
        <button class="arrow left" @click="prev">‹</button>
        <button class="arrow right" @click="next">›</button>
        <div class="indicators">
          <span v-for="(s,i) in slides" :key="`dot-${i}`" class="dot" :class="{ active: i===slideIndex }" @click="go(i)" />
        </div>
      </div>
      <aside class="recommend">
        <div class="top-video" v-for="(r, i) in recommends" :key="i">
          <div class="thumb-wrap"><img :src="r.cover" /></div>
          <div class="v-title" :title="r.title">{{ r.title }}</div>
          <div class="v-sub">推荐</div>
        </div>
      </aside>
    </section>

    <section class="section">
      <div class="video-grid">
        <div v-for="v in videos" :key="v.id" class="video" @click="playVideo(v)">
          <div class="thumb-wrap">
            <video v-if="v.isVideo" :src="v.playUrl as string" :poster="v.cover as string" class="video-preview" muted>
              <source :src="v.playUrl as string" type="video/mp4">
              您的浏览器不支持视频播放。
            </video>
            <img v-else :src="v.cover" />
            <span class="duration">{{ v.duration }}</span>
            <div v-if="v.isVideo" class="play-overlay">
              <div class="play-button">▶</div>
            </div>
          </div>
          <div class="v-title" :title="v.title">{{ v.title }}</div>
          <div class="v-sub">{{ v.playCount }} · {{ v.up }}</div>
        </div>
      </div>
    </section>
  </div>


  <!-- 登录组件 -->
  <!-- <Login v-if="showLogin" @close="showLogin=false" /> -->
  <Login v-model:show="showLogin" @close="showLogin=false" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Login from '@/components/Login.vue'
import { fetchVideos } from '@/api/video'

const router = useRouter()

let showLogin = ref(false)

const categories = [
  '番剧','国创','综艺','动画','鬼畜','舞蹈','娱乐','科技','美食','汽车','运动','VLOG','单机游戏','公益','电影','电视剧','纪录片','音乐','知识','资讯','生活','时尚'
]

const slides = ref([
  { src: '/images/banner-1.jpg', alt: '1', caption: '正在直播LPL：JDG vs LGD!' },
  { src: '/images/banner-2.jpg', alt: '2', caption: '蒙恬扁鹊 双人巅峰' },
  { src: '/images/banner-3.jpg', alt: '3', caption: '生命体征维持器' }
])
const slideIndex = ref(0)
let timer: any
const next = () => { slideIndex.value = (slideIndex.value + 1) % slides.value.length }
const prev = () => { slideIndex.value = (slideIndex.value - 1 + slides.value.length) % slides.value.length }
const go = (i: number) => { slideIndex.value = i }
onMounted(() => {
  timer = setInterval(next, 4000)
  fetchVideosData()
})
onUnmounted(() => { if (timer) clearInterval(timer) })

const recommends = ref([
  { cover: '/images/rec-1.jpg', title: '盾狗+蜂医 双人巅峰 04:05' },
  { cover: '/images/rec-2.jpg', title: '生命体征维持器 04:57' },
  { cover: '/images/rec-3.jpg', title: '为什么小妹很会赚钱 05:12' },
  { cover: '/images/rec-4.jpg', title: '开学第一天 01:52' }
])

const videos = ref<any[]>([])
const loadingVideos = ref(false)
const fallbackCover = '/images/banner-1.jpg'

const formatDuration = (seconds?: number) => {
  if (!seconds || seconds <= 0) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const fetchVideosData = async () => {
  loadingVideos.value = true
  try {
    const { data } = await fetchVideos()
    const list = Array.isArray(data) ? data : []
    videos.value = list.map((item: any) => {
      const durationText = formatDuration(item?.duration)
      return {
        ...item,
        cover: item?.coverUrl || fallbackCover,
        duration: durationText,
        playCount: typeof item?.viewCount === 'number' ? item.viewCount : '本地视频',
        up: item?.sourceFile || '本地文件',
        playUrl: item?.playUrl || '',
        id: item?.videoId || item?.id,
        isVideo: !!item?.playUrl
      }
    })
  } catch (e) {
    videos.value = []
  } finally {
    loadingVideos.value = false
  }
}

// 导航到创作中心
const goTo = (path: string) => { router.push(path) }

const navigateToCreatorCenter = () => {
  router.push('/submitHome?view=contentManagement')
}

// 播放视频
const playVideo = (video: any) => {
  if (video.isVideo) {
    // 跳转到视频播放页面
    router.push(`/video/${encodeURIComponent(video.id)}`)
  }
}
</script>

<style lang="scss" scoped>
.home {
  background: #fff;
  min-width: 1600px;
  max-width: 1800px;
  width: 100%;
  margin: 0 auto;
}

.site-header {
  position: relative;
  height: 156px;

  .header-bg {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 156px;
    object-fit: cover;
  }

  .header-inner {
    position: relative;
    z-index: 1;
    height: 64px;
    display: grid;
    grid-template-columns: auto 1fr auto;
    align-items: center;
    gap: 12px;
    padding: 8px 24px;
  }

  .nav-left {
    display: flex;
    gap: 20px;
    list-style: none;
    padding: 0;
    margin-left: 1vw;
    align-items: center;
  }

  .nav-item {
    display: flex;
    align-items: center;
    color: #fff;
    font-size: 14px;
    gap: 6px;
    cursor: pointer;

    span {
      transition: color .2s;
    }

    &:hover span {
      color: #00a1d6;
      animation: jump 0.3s ease;
    }
  }

  .nav-icon {
    width: 18px;
    height: 18px;
    filter: brightness(0) invert(1);
  }

  .search {
    display: grid;
    grid-template-columns: 1fr 40px;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    width: 500px;
    margin: 0 auto;

    .search-input {
      height: 36px;
      padding: 0 12px;
      border: 0;
      outline: none;
      font-size: 14px;
    }

    .search-btn {
      margin-left: 4px;
      margin-top: 4px;
      border: 0;
      background: transparent;
      cursor: pointer;
      font-size: 16px;
      padding: 8px;
      width: 25px;
      height: 25px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        background-color: #f5f5f5;
      }

      .search-btn-img {
        width: 20px;
        height: 20px;
        margin-top: 4px;
      }
    }
  }

  .actions {
    display: flex;
    gap: 20px;
    align-items: center;
    margin-right: 1vw;
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #d8d8d8;
    border: 2px solid rgba(255, 255, 255, .8);
  }

  .action-col {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #fff;
    gap: 4px;
    font-size: 12px;

    span {
      transition: color .2s;
    }

    &:hover span {
      color: #00a1d6;
      animation: jump 0.3s ease;
    }

    &:hover .action-icon {
      animation: jump 0.3s ease;
    }
  }

  .action-icon {
    width: 19px;
    height: 19px;
    /* 轻微发光，略细于上一版 */
    filter: brightness(0) invert(1) drop-shadow(0 0 0.3px rgba(255, 255, 255, 0.85));
  }

  .action {
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.4);
    color: #fff;
    padding: 6px 10px;
    border-radius: 6px;
    cursor: pointer;
  }

  .primary {
    background: #fb7299;
    border: none;
    color: #fff;
    padding: 6px 12px;
    border-radius: 6px;
    cursor: pointer;
  }
}

.navigation-section {
  max-width: 1350px;
  margin: -30px auto 0;
  padding: 0 20px;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-top: 20px;

  .nav-left-section {
    display: flex;
    flex-direction: column;
    gap: 10px;
    flex: 1;

    .nav-left-top {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .quick-icons {
      margin: 0;
      margin-left: 0px;
      margin-right: 20px;
      display: flex;
      gap: 20px;

      .qi-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;

        .qi-circle {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;

          &.orange {
            background: #ff9212;
          }

          &.pink {
            background: #f07775;
          }

          .qi-icon {
            width: 23px;
            height: 23px;
            filter: brightness(0) invert(1);
          }
        }

        .qi-text {
          font-size: 14px;
          color: #222;
        }
      }
    }

    .grid-container {
      display: grid;
      grid-template-columns: repeat(11, 1fr);
      grid-template-rows: repeat(2, 1fr);
      gap: 8px;
      width: 100%;

      .grid-item {
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #f0f0f0;
        border-radius: 8px;
        padding: 8px 12px;
        text-decoration: none;
        color: #61666d;
        font-size: 14px;
        transition: background-color 0.3s, color 0.3s;
        height: 15px;
        white-space: nowrap;

        &:hover {
          background-color: #e0e0e0;
          color: black;
        }
      }
    }
  }

  .nav-right-section {
    flex-shrink: 0;
    padding-left: 10px;
    border-left: 1.5px solid #eee;

    .utility-links {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .utility-row {
        display: flex;
        gap: 12px;

        .utility-item {
          display: flex;
          flex-direction: row;
          align-items: center;
          gap: 6px;
          cursor: pointer;
          padding: 4px 8px;
          border-radius: 6px;
          transition: background-color 0.2s;

          &:hover {
            background-color: #f5f5f5;
          }

          .utility-icon {
            width: 16px;
            height: 16px;
            filter: grayscale(100%) brightness(0.7);
          }

          .utility-text {
            font-size: 14px;
            color: #61666d;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

/* 顶部区域：5列网格，轮播占两列两行 */
.hero-grid {
  max-width: 1350px;
  margin: 20px auto 20px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  /* 顶部每个卡片的行高，轮播图将占两行 */
  grid-auto-rows: 220px;
  gap: 16px;
  
  /* 防止子项内容撑破导致列宽不一致 */
  > * { min-width: 0; }

  .banner {
    position: relative;
    background: linear-gradient(135deg, #2b2b3a, #5b6bd5);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, .08);
    grid-column: 1 / span 2;  /* 占两列 */
    grid-row: 1 / span 2;     /* 占两行 */
    /* 高度 = 两行高度 + 中间一条间距 */
    height: 390px;
    width: 100%;

    .slider {
      position: absolute;
      inset: 0;
      display: flex;
      transition: transform .45s ease;

      .slide {
        min-width: 100%;
        position: relative;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .slide-caption {
          position: absolute;
          left: 16px;
          bottom: 12px;
          background: rgba(0, 0, 0, .45);
          color: #fff;
          padding: 6px 10px;
          font-size: 12px;
          border-radius: 4px;
          backdrop-filter: blur(2px);
        }
      }
    }

    .arrow {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: 32px;
      height: 32px;
      border-radius: 50%;
      border: 0;
      background: rgba(0, 0, 0, .35);
      color: #fff;
      cursor: pointer;

      &.left {
        left: 8px;
      }

      &.right {
        right: 8px;
      }
    }

    .indicators {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 10px;
      display: flex;
      justify-content: center;
      gap: 6px;

      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: rgba(255, 255, 255, .5);
        cursor: pointer;

        &.active {
          background: #fff;
        }
      }
    }
  }

  .recommend {
    display: contents; /* 将推荐卡片直接放入网格，让其占据网格单元 */

    .top-video {
      display: grid;
      grid-template-rows: auto auto auto;
      gap: 6px;
      
      .thumb-wrap {
        position: relative;
        width: 100%;
        padding-bottom: 56%;
        border-radius: 8px;
        overflow: hidden;
        background: #f1f2f3;

        img {
          position: absolute;
          inset: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .v-title { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .v-sub { font-size: 12px; color: #8a8a8a; }
    }
  }
}

.section {
  max-width: 1350px;
  margin: 80px auto 40px;
  padding: 0 20px;

  .video-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;

    .video {
      display: grid;
      grid-template-rows: auto auto auto;
      gap: 6px;
      cursor: pointer;

      .thumb-wrap {
        position: relative;
        width: 100%;
        padding-bottom: 56%;
        border-radius: 8px;
        overflow: hidden;
        background: #f1f2f3;

        img, .video-preview {
          position: absolute;
          inset: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .video-preview {
          background: #000;
        }

        .duration {
          position: absolute;
          right: 6px;
          bottom: 6px;
          font-size: 12px;
          color: #fff;
          background: rgba(0, 0, 0, .55);
          padding: 2px 6px;
          border-radius: 4px;
          z-index: 2;
        }

        .play-overlay {
          position: absolute;
          inset: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(0, 0, 0, 0.3);
          opacity: 0;
          transition: opacity 0.3s ease;
          z-index: 1;

          .play-button {
            width: 50px;
            height: 50px;
            background: rgba(255, 255, 255, 0.9);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            color: #333;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
          }
        }

        &:hover .play-overlay {
          opacity: 1;
        }
      }

      .v-title {
        font-size: 13px;
        color: #222;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .v-sub {
        font-size: 12px;
        color: #8a8a8a;
      }
    }
  }
}

/* 固定阈值以下不再自适应，保持 1200px 布局 */

/* 定义跳动动画，保持与 HomeView.vue 一致 */
@keyframes jump {
  0% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
  100% { transform: translateY(0); }
}
</style>