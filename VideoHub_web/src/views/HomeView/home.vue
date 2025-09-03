<template>
  <div class="home">
    <header class="site-header">
      <div class="header-inner">
        <ul class="nav-left">
          <li class="nav-item">
            <img src="/assets/home.png" class="nav-icon" />
            <span>首页</span>
          </li>
          <li class="nav-item"><span>番剧</span></li>
          <li class="nav-item"><span>直播</span></li>
          <li class="nav-item"><span>游戏中心</span></li>
          <li class="nav-item"><span>会员购</span></li>
          <li class="nav-item"><span>漫画</span></li>
          <li class="nav-item"><span>赛事</span></li>
          <li class="nav-item">
            <img src="/assets/download-client.png" class="nav-icon" />
            <span>下载客户端</span>
          </li>
        </ul>
        <div class="search">
          <input class="search-input" placeholder="搜索你感兴趣的内容" />
          <button class="search-btn">🔍</button>
        </div>
        <nav class="actions">
          <div class="avatar" />
          <div class="action-col">
            <img src="/assets/vip.png" class="action-icon" /><span>大会员</span>
          </div>
          <div class="action-col">
            <img src="/assets/messages.png" class="action-icon" /><span>消息</span>
          </div>
          <div class="action-col">
            <img src="/assets/feed.png" class="action-icon" /><span>动态</span>
          </div>
          <div class="action-col">
            <img src="/assets/favorites.png" class="action-icon" /><span>收藏</span>
          </div>
          <div class="action-col">
            <img src="/assets/history.png" class="action-icon" /><span>历史</span>
          </div>
          <div class="action-col">
            <img src="/assets/creator-center.png" class="action-icon" /><span>创作中心</span>
          </div>
          <button class="primary">投稿</button>
        </nav>
      </div>
      <img class="header-bg" src="/assets/header.png" alt="banner" />
    </header>
    <section class="quick-icons">
      <div class="qi-item">
        <div class="qi-circle orange">⬤</div>
        <div class="qi-text">动态</div>
      </div>
      <div class="qi-item">
        <div class="qi-circle pink">⬤</div>
        <div class="qi-text">热门</div>
      </div>
    </section>

    <section class="chips">
      <button v-for="c in categories" :key="c" class="chip">{{ c }}</button>
    </section>

    <section class="main-block">
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
        <div class="rec-card" v-for="(r, i) in recommends" :key="i">
          <img :src="r.cover" />
          <div class="rec-title" :title="r.title">{{ r.title }}</div>
        </div>
      </aside>
    </section>

    <section class="section">
      <div class="section-title">正在热播</div>
      <div class="video-grid">
        <div v-for="v in videos" :key="v.id" class="video">
          <div class="thumb-wrap"><img :src="v.cover" /><span class="duration">{{ v.duration }}</span></div>
          <div class="v-title" :title="v.title">{{ v.title }}</div>
          <div class="v-sub">{{ v.playCount }} · {{ v.up }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const categories = [
  '动态','热门','番剧','国创','综艺','动画','鬼畜','舞蹈','娱乐','科技','美食','汽车','运动','VLOG','单机游戏','公益','电影','电视剧','纪录片','音乐','知识','资讯','生活','时尚'
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
onMounted(() => { timer = setInterval(next, 4000) })
onUnmounted(() => { if (timer) clearInterval(timer) })

const recommends = ref([
  { cover: '/images/rec-1.jpg', title: '盾狗+蜂医 双人巅峰 04:05' },
  { cover: '/images/rec-2.jpg', title: '生命体征维持器 04:57' },
  { cover: '/images/rec-3.jpg', title: '为什么小妹很会赚钱 05:12' },
  { cover: '/images/rec-4.jpg', title: '开学第一天 01:52' }
])

const videos = ref([
  { id: 1, title: '恶魔 or 天使？双胞姐妹二选一！', cover: '/images/demo-cover1.jpg', duration: '11:01', playCount: '138.6万', up: '何同学' },
  { id: 2, title: '开学第一天', cover: '/images/demo-cover2.jpg', duration: '01:52', playCount: '102万', up: '阿福' },
  { id: 3, title: '矿泉水大测评', cover: '/images/demo-cover3.jpg', duration: '09:20', playCount: '56.4万', up: '老番茄' },
  { id: 4, title: '头部创作者都说好', cover: '/images/demo-cover4.jpg', duration: '02:21', playCount: '广告', up: '推广' },
  { id: 5, title: '你的牛奶我的汽水', cover: '/images/demo-cover5.jpg', duration: '06:45', playCount: '8.7万', up: '某UP' },
  { id: 6, title: 'APP 粉丝做的', cover: '/images/demo-cover6.jpg', duration: '03:18', playCount: '20.2万', up: '某UP' }
])
</script>

<style lang="scss" scoped>
.home { background: #fff; min-width: 1200px; }

.site-header { position: relative; height: 156px; }
.header-bg { position: absolute; inset: 0; width: 100%; height: 156px; object-fit: cover; }
.header-inner { position: relative; z-index: 1; height: 64px; display: grid; grid-template-columns: auto 1fr auto; align-items: center; gap: 12px; padding: 8px 24px; }
.nav-left { display: flex; gap: 14px; list-style: none; padding: 0; margin: 0; align-items: center; }
.nav-item { display: flex; align-items: center; color: #fff; font-size: 14px; gap: 6px; cursor: pointer; }
.nav-item span { transition: color .2s; }
.nav-item:hover span { color: #00a1d6; }
.nav-icon { width: 18px; height: 18px; filter: brightness(0) invert(1); }
.search { display: grid; grid-template-columns: 1fr 40px; background: #fff; border-radius: 8px; overflow: hidden; }
.search-input { height: 36px; padding: 0 12px; border: 0; outline: none; font-size: 14px; }
.search-btn { border: 0; background: transparent; cursor: pointer; font-size: 16px; }
.actions { display: flex; gap: 10px; align-items: center; }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: #d8d8d8; border: 2px solid rgba(255,255,255,.8); }
.action-col { display: flex; flex-direction: column; align-items: center; color: #fff; gap: 4px; font-size: 12px; }
.action-icon { width: 18px; height: 18px; filter: brightness(0) invert(1); }
.action { background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.4); color: #fff; padding: 6px 10px; border-radius: 6px; cursor: pointer; }
.primary { background: #fb7299; border: none; color: #fff; padding: 6px 12px; border-radius: 6px; cursor: pointer; }

.chips { max-width: 1200px; margin: 10px auto 0; padding: 0 20px; display: flex; flex-wrap: wrap; gap: 8px; }
.chip { background: #f5f5f7; border: 1px solid #eee; border-radius: 14px; padding: 4px 10px; font-size: 12px; color: #61666d; cursor: pointer; }
.chip:hover { color: #00a1d6; background: #eef9ff; }

.quick-icons { max-width: 1200px; margin: -30px auto 6px; padding: 0 20px; display: flex; gap: 20px; }
.qi-item { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.qi-circle { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: transparent; position: relative; }
.qi-circle::after { content: ''; position: absolute; inset: 8px; background: rgba(255,255,255,.9); border-radius: 50%; filter: brightness(0) invert(1); }
.qi-circle.orange { background: #ff9212; }
.qi-circle.pink { background: #f07775; }
.qi-text { font-size: 12px; color: #222; }

.main-block { max-width: 1200px; margin: 10px auto 20px; padding: 0 20px; display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
.banner { position: relative; background: linear-gradient(135deg,#2b2b3a,#5b6bd5); border-radius: 8px; overflow: hidden; padding-bottom: 56.25%; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.slider { position: absolute; inset: 0; display: flex; transition: transform .45s ease; }
.slide { min-width: 100%; position: relative; }
.slide img { width: 100%; height: 100%; object-fit: cover; }
.slide-caption { position: absolute; left: 16px; bottom: 12px; background: rgba(0,0,0,.45); color: #fff; padding: 6px 10px; font-size: 12px; border-radius: 4px; backdrop-filter: blur(2px); }
.arrow { position: absolute; top: 50%; transform: translateY(-50%); width: 32px; height: 32px; border-radius: 50%; border: 0; background: rgba(0,0,0,.35); color: #fff; cursor: pointer; }
.arrow.left { left: 8px; }
.arrow.right { right: 8px; }
.indicators { position: absolute; left: 0; right: 0; bottom: 10px; display: flex; justify-content: center; gap: 6px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: rgba(255,255,255,.5); cursor: pointer; }
.dot.active { background: #fff; }

.recommend { display: grid; grid-template-columns: 1fr 1fr; grid-auto-rows: 96px; gap: 12px; align-content: start; }
.rec-card { display: grid; grid-template-rows: 1fr auto; background: #fff; border-radius: 8px; box-shadow: 0 1px 2px rgba(0,0,0,.05); overflow: hidden; }
.rec-card img { width: 100%; height: 100%; object-fit: cover; }
.rec-title { font-size: 12px; color: #333; padding: 6px 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.section { max-width: 1200px; margin: 0 auto 40px; padding: 0 20px; }
.section-title { font-size: 16px; font-weight: 600; margin: 8px 0 12px; }
.video-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.video { display: grid; grid-template-rows: auto auto auto; gap: 6px; }
.thumb-wrap { position: relative; width: 100%; padding-bottom: 56%; border-radius: 8px; overflow: hidden; background: #f1f2f3; }
.thumb-wrap img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.duration { position: absolute; right: 6px; bottom: 6px; font-size: 12px; color: #fff; background: rgba(0,0,0,.55); padding: 2px 6px; border-radius: 4px; }
.v-title { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.v-sub { font-size: 12px; color: #8a8a8a; }

/* 固定阈值以下不再自适应，保持 1200px 布局 */
</style>
