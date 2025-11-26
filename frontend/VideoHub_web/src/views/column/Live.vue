<template>
  <div class="live">
    <!-- 顶部：左侧大播放器 + 右侧直播推荐/主播榜单 -->
    <section class="hero container">
      <div class="player-wrap">
        <div class="player">
          <!-- 占位：可替换为真实播放器组件 -->
          <video class="video" controls :poster="hero.poster">
            <source :src="hero.src" type="video/mp4" />
          </video>
        </div>
      </div>
      <aside class="sidebar">
        <div class="side-card">
          <div class="side-title">热门直播</div>
          <div class="live-item" v-for="(r,i) in rightRecs" :key="i">
            <img class="cover" :src="r.cover" />
            <div class="meta">
              <div class="t" :title="r.title">{{ r.title }}</div>
              <div class="s">{{ r.viewers }} 人气</div>
            </div>
            <button class="go">进入</button>
          </div>
        </div>
        <div class="side-card">
          <div class="side-title">主播排行榜</div>
          <div class="rank-item" v-for="(u,i) in anchors" :key="i">
            <span class="idx" :class="{top:i<3}">{{ i+1 }}</span>
            <img class="avatar" :src="u.avatar" />
            <div class="u-meta">
              <div class="name">{{ u.name }}</div>
              <div class="desc">{{ u.desc }}</div>
            </div>
            <div class="heat">{{ u.heat }}</div>
          </div>
        </div>
      </aside>
    </section>

    <!-- 二层：我的关注 + 分区推荐胶囊（严格按图的两列板块） -->
    <section class="container row-boards">
      <div class="board follow">
        <div class="board-head">
          <span class="title">我的关注</span>
          <a href="javascript:void(0)" class="more">查看更多</a>
        </div>
        <div class="board-body empty">暂时还没有你关注的直播</div>
      </div>
      <div class="board classify">
        <div class="board-head">
          <span class="title">分区推荐</span>
        </div>
        <div class="capsules">
          <div class="cap" v-for="c in capsules" :key="c.text">
            <img :src="c.icon" /><span>{{ c.text }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 三层：左大图轮播 + 右侧热门分区/资讯中心/人气榜（3列布局） -->
    <section class="container grid-3">
      <div class="carousel">
        <div class="slides" :style="{ transform: `translateX(-${slideIndex * 100}%)` }">
          <div class="slide" v-for="(s,i) in banners" :key="i">
            <img :src="s.src" :alt="s.alt" />
          </div>
        </div>
        <div class="dots">
          <span v-for="(s,i) in banners" :key="`dot-${i}`" :class="['dot',{active:i===slideIndex}]" @click="go(i)" />
        </div>
      </div>
      <div class="hot-area">
        <div class="hot-title">热门分区</div>
        <div class="hot-cards">
          <div class="hot-card" v-for="(h,i) in hotAreas" :key="i">
            <img :src="h.cover" />
            <div class="h-title">{{ h.title }}</div>
          </div>
        </div>
      </div>
      <div class="news-and-rank">
        <div class="news">
          <div class="news-title">资讯中心</div>
          <ul class="news-list">
            <li v-for="(n,i) in news" :key="i">
              <span class="dot"></span>
              <span class="n-title" :title="n.title">{{ n.title }}</span>
              <span class="n-time">{{ n.time }}</span>
            </li>
          </ul>
        </div>
        <div class="rank">
          <div class="rank-title">人气榜</div>
          <ul class="rank-list">
            <li v-for="(r,i) in hotRank" :key="i">
              <span class="idx" :class="{top:i<3}">{{ i+1 }}</span>
              <span class="r-name">{{ r.name }}</span>
              <span class="r-heat">{{ r.heat }}</span>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- 推荐直播大区 -->
    <section class="section container">
      <div class="section-head">
        <h2>推荐直播</h2>
        <a class="more" href="javascript:void(0)">更多 ›</a>
      </div>
      <div class="card-grid">
        <div class="card" v-for="(c,i) in recLives" :key="i">
          <div class="thumb"><img :src="c.cover" /></div>
          <div class="title" :title="c.title">{{ c.title }}</div>
          <div class="sub">{{ c.tag }} · {{ c.viewers }} 人气</div>
        </div>
      </div>
    </section>

    <!-- 页面分区：分区推荐、热门专题、赛事直播等（示例三块） -->
    <section class="section container" v-for="(sec, si) in sections" :key="si">
      <div class="section-head">
        <h2>{{ sec.title }}</h2>
        <a class="more" href="javascript:void(0)">更多 ›</a>
      </div>
      <div class="card-grid">
        <div class="card" v-for="(c,i) in sec.items" :key="i">
          <div class="thumb"><img :src="c.cover" /></div>
          <div class="title" :title="c.title">{{ c.title }}</div>
          <div class="sub">{{ c.tag }} · {{ c.viewers }} 人气</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const hero = ref({
  src: '/videos/screen_recording.mp4',
  poster: '/images/demo-cover1.jpg'
})

// 二层：分区推荐胶囊
const capsules = ref([
  { icon: '/assets/channel/live.png', text: '网游' },
  { icon: '/assets/channel/live.png', text: '手游' },
  { icon: '/assets/channel/live.png', text: '单机游戏' },
  { icon: '/assets/channel/live.png', text: '娱乐' },
  { icon: '/assets/channel/live.png', text: '电台' },
  { icon: '/assets/channel/live.png', text: '虚拟主播' },
  { icon: '/assets/channel/live.png', text: '赛事' },
  { icon: '/assets/channel/live.png', text: '知识' },
])

const rightRecs = ref([
  { cover: '/images/rec-1.jpg', title: '星际服女王冲分', viewers: '5.6万' },
  { cover: '/images/rec-2.jpg', title: 'KPL 职业训练赛', viewers: '3.2万' },
  { cover: '/images/rec-3.jpg', title: 'CS:GO 高燃瞬间', viewers: '2.4万' },
  { cover: '/images/rec-4.jpg', title: 'MOBA 上分日常', viewers: '1.9万' },
])

const anchors = ref([
  { avatar: '/images/demo-cover2.jpg', name: '白银座的星', desc: '二次元·游戏', heat: '98.2万' },
  { avatar: '/images/demo-cover3.jpg', name: '峡谷药师', desc: 'MOBA·上单', heat: '87.5万' },
  { avatar: '/images/demo-cover4.jpg', name: '夜行者', desc: 'FPS·竞技', heat: '79.1万' },
  { avatar: '/images/demo-cover5.jpg', name: '老咸鱼', desc: '综艺·娱乐', heat: '65.8万' },
  { avatar: '/images/rec-3.jpg',       name: '森林法师', desc: '生存·沙盒', heat: '50.3万' },
])

const recLives = ref([
  { cover: '/images/rec-1.jpg', title: '峡谷之巅 冲刺王者', tag: 'MOBA', viewers: '8.7万' },
  { cover: '/images/rec-2.jpg', title: '职业选手训练赛', tag: '赛事', viewers: '6.2万' },
  { cover: '/images/rec-3.jpg', title: '荒野求生 极限挑战', tag: '生存', viewers: '4.1万' },
  { cover: '/images/rec-4.jpg', title: '二次元电台 唠嗑', tag: '电台', viewers: '3.6万' },
  { cover: '/images/demo-cover1.jpg', title: 'FPS 高能时刻', tag: '射击', viewers: '2.9万' },
  { cover: '/images/demo-cover2.jpg', title: '休闲联机 欢乐局', tag: '休闲', viewers: '2.5万' },
])

// 三层：左侧轮播、右侧热门分区、资讯、人气榜
const banners = ref([
  { src: '/images/banner-1.jpg', alt: 'banner1' },
  { src: '/images/banner-2.jpg', alt: 'banner2' },
  { src: '/images/banner-3.jpg', alt: 'banner3' }
])
const slideIndex = ref(0)
const go = (i: number) => { slideIndex.value = i }

const hotAreas = ref([
  { cover: '/images/rec-1.jpg', title: '英雄联盟' },
  { cover: '/images/rec-2.jpg', title: '王者荣耀' }
])

const news = ref([
  { title: 'KPL 年度总决赛开战在即，门票开售', time: '08-11' },
  { title: '平台主播公约升级公告', time: '08-10' },
  { title: '赛事周报：CS 邀请赛赛程公布', time: '08-09' },
  { title: '直播品类扩展：知识学习专区上线', time: '08-08' },
  { title: '严禁恶意引流与外挂行为的处罚规则', time: '08-07' }
])

const hotRank = ref([
  { name: '炎Yami', heat: '58.7万热度' },
  { name: '赛事官方号', heat: '43.2万热度' },
  { name: '小熊电台', heat: '29.1万热度' },
  { name: '外场摄影', heat: '21.4万热度' },
  { name: '陪玩小屋', heat: '18.2万热度' },
  { name: '音乐现场', heat: '16.9万热度' }
])

const sections = ref([
  {
    title: '分区推荐',
    items: [
      { cover: '/images/demo-cover3.jpg', title: '单排锐雯 操作拉满', tag: 'MOBA', viewers: '1.8万' },
      { cover: '/images/demo-cover4.jpg', title: '深海求生 海怪来袭', tag: '生存', viewers: '1.5万' },
      { cover: '/images/demo-cover5.jpg', title: '开黑欢乐时光', tag: '休闲', viewers: '1.2万' },
      { cover: '/images/rec-1.jpg',       title: '绝地枪王 操作秀', tag: '射击', viewers: '1.1万' },
      { cover: '/images/rec-2.jpg',       title: '解谜闯关 燃烧脑细胞', tag: '解谜', viewers: '0.9万' },
      { cover: '/images/rec-3.jpg',       title: '复古像素 冒险记', tag: '像素', viewers: '0.8万' },
    ]
  },
  {
    title: '热门专题',
    items: [
      { cover: '/images/demo-cover2.jpg', title: '开学季 学习直播', tag: '学习', viewers: '2.1万' },
      { cover: '/images/demo-cover1.jpg', title: '音乐房 轻松一下', tag: '音乐', viewers: '1.9万' },
      { cover: '/images/demo-cover4.jpg', title: '摄影外拍 旅途记', tag: '户外', viewers: '1.6万' },
      { cover: '/images/demo-cover5.jpg', title: '手工 DIY 创作', tag: '手工', viewers: '1.2万' },
      { cover: '/images/rec-4.jpg',       title: '读书分享会', tag: '阅读', viewers: '0.8万' },
      { cover: '/images/rec-3.jpg',       title: '美食探店', tag: '美食', viewers: '0.7万' },
    ]
  },
  {
    title: '赛事直播',
    items: [
      { cover: '/images/rec-1.jpg', title: 'KPL 夏季赛 半决赛', tag: '赛事', viewers: '12.4万' },
      { cover: '/images/rec-2.jpg', title: 'LPL 常规赛 巅峰对决', tag: '赛事', viewers: '10.5万' },
      { cover: '/images/rec-3.jpg', title: 'CS:GO 邀请赛', tag: '赛事', viewers: '7.7万' },
      { cover: '/images/rec-4.jpg', title: 'DOTA2 职业联赛', tag: '赛事', viewers: '6.8万' },
      { cover: '/images/demo-cover1.jpg', title: '守望先锋 联赛', tag: '赛事', viewers: '5.1万' },
      { cover: '/images/demo-cover2.jpg', title: '绝地求生 职业赛', tag: '赛事', viewers: '4.3万' },
    ]
  }
])
</script>

<style scoped lang="scss">
.container { max-width: 1350px; margin: 0 auto; padding: 0 16px; }
.live { background: #fff; }

.hero { display: grid; grid-template-columns: 1fr 360px; gap: 16px; margin-top: 12px; }
.player-wrap { background: #eaf4ee; border-radius: 8px; overflow: hidden; }
.player { position: relative; padding-bottom: 56.25%; background: #000; }
.player .video { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }

.sidebar { display: grid; gap: 12px; }
.side-card { background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 10px; }
.side-title { font-weight: 600; color: #222; margin-bottom: 8px; }
.live-item { display: grid; grid-template-columns: 84px 1fr auto; gap: 8px; align-items: center; padding: 6px 0; }
.live-item .cover { width: 84px; height: 48px; object-fit: cover; border-radius: 6px; }
.live-item .meta .t { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.live-item .meta .s { font-size: 12px; color: #8a8a8a; margin-top: 2px; }
.live-item .go { height: 28px; padding: 0 10px; background: #00aeec; color: #fff; border: none; border-radius: 6px; cursor: pointer; }

.rank-item { display: grid; grid-template-columns: 22px 32px 1fr auto; gap: 8px; align-items: center; padding: 6px 0; }
.rank-item .idx { font-size: 12px; color: #999; }
.rank-item .idx.top { color: #ff7b8c; font-weight: 700; }
.rank-item .avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.rank-item .u-meta .name { font-size: 13px; color: #222; }
.rank-item .u-meta .desc { font-size: 12px; color: #8a8a8a; }
.rank-item .heat { font-size: 12px; color: #fb7299; }

.row-boards { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 14px; }
.board { background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 12px; }
.board-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.board-head .title { font-weight: 600; color: #222; }
.board-head .more { color: #909399; font-size: 12px; text-decoration: none; }
.board .empty { color: #8a8a8a; background: #fafafa; border: 1px dashed #eee; border-radius: 6px; padding: 20px; text-align: center; }

.classify .capsules { display: grid; grid-template-columns: repeat(8, 1fr); gap: 10px; }
.cap { display:flex; align-items:center; gap:6px; padding:8px 10px; background:#f8f8f8; border-radius: 8px; cursor: pointer; }
.cap img { width:18px; height:18px; filter: grayscale(100%) brightness(.8); }
.cap span { font-size: 13px; color:#61666d; }

.grid-3 { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 16px; margin-top: 14px; }
.carousel { position: relative; height: 220px; border-radius: 8px; overflow: hidden; background:#000; }
.slides { position:absolute; inset:0; display:flex; transition: transform .45s ease; }
.slide { min-width: 100%; }
.slide img { width:100%; height:100%; object-fit: cover; }
.dots { position:absolute; left:0; right:0; bottom:10px; display:flex; justify-content:center; gap:6px; }
.dot { width:6px; height:6px; border-radius:50%; background: rgba(255,255,255,.45); }
.dot.active { background:#fff; }

.hot-area { background:#fff; border:1px solid #eee; border-radius:8px; padding:10px; }
.hot-title { font-weight:600; color:#222; margin-bottom:8px; }
.hot-cards { display:grid; grid-template-columns: 1fr; gap: 10px; }
.hot-card { display:grid; grid-template-rows:auto auto; gap:6px; }
.hot-card img { width:100%; height:96px; object-fit:cover; border-radius:6px; }
.hot-card .h-title { font-size:13px; color:#222; }

.news-and-rank { display:grid; gap:12px; }
.news, .rank { background:#fff; border:1px solid #eee; border-radius:8px; padding:10px; }
.news-title, .rank-title { font-weight:600; color:#222; margin-bottom:6px; }
.news-list { list-style:none; margin:0; padding:0; display:grid; gap:6px; }
.news-list li { display:grid; grid-template-columns: 8px 1fr auto; align-items:center; gap:6px; }
.news-list .dot { width:4px; height:4px; border-radius:50%; background:#bbb; }
.news-list .n-title { font-size:13px; color:#222; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.news-list .n-time { font-size:12px; color:#8a8a8a; }
.rank-list { list-style:none; margin:0; padding:0; display:grid; gap:6px; }
.rank-list li { display:grid; grid-template-columns: 18px 1fr auto; align-items:center; gap:8px; }
.rank-list .idx { font-size:12px; color:#999; }
.rank-list .idx.top { color:#ff7b8c; font-weight:700; }
.rank-list .r-name { font-size:13px; color:#222; }
.rank-list .r-heat { font-size:12px; color:#fb7299; }

.section { margin: 18px auto 28px; }
.section-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 10px; }
.section-head h2 { font-size: 18px; color: #222; }
.section-head .more { font-size: 13px; color: #909399; text-decoration: none; }
.card-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.card { display: grid; gap: 6px; }
.card .thumb { position: relative; padding-bottom: 56%; background: #f1f2f3; border-radius: 8px; overflow: hidden; }
.card .thumb img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.card .title { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card .sub { font-size: 12px; color: #8a8a8a; }

.promo-strip { margin: 0 auto 20px; }
.promo-strip img { width:100%; height: 96px; object-fit: cover; border-radius: 8px; }

.icons-area { display:grid; grid-template-columns: repeat(10, 1fr); gap: 12px; margin: 8px auto 18px; }
.icon-card { background:#fff; border:1px solid #eee; border-radius:10px; padding: 14px 10px; display:grid; justify-items:center; gap:6px; }
.icon-card img { width:32px; height:32px; object-fit: contain; }
.icon-card .ic-text { font-size:12px; color:#61666d; }
</style>


