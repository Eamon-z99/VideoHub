<template>
  <div class="vip">
    <!-- 顶部粉色渐变横幅 -->
    <section class="vip-hero">
      <div class="container hero-inner">
        <div class="hero-copy">
          <h1 class="title">开通大会员</h1>
          <p class="subtitle">番剧国创优先看 · 专属身份标识 · 毫秒级蓝光</p>
          <div class="cta-row">
            <button class="btn primary">立即开通</button>
            <button class="btn ghost">了解权益</button>
          </div>
        </div>
        <div class="hero-mark">
          <div class="big-badge">大</div>
        </div>
      </div>
      <!-- 选项卡条 -->
      <div class="container tabs">
        <button class="tab active">全部权益</button>
        <button class="tab">番剧</button>
        <button class="tab">电影</button>
        <button class="tab">纪录片</button>
        <button class="tab">动画</button>
      </div>
    </section>

    <!-- 票券横条 -->
    <section class="coupon-row container">
      <div class="coupon" v-for="(c,i) in coupons" :key="i">
        <div class="price"><span class="currency">￥</span>{{ c.price }}</div>
        <div class="desc">{{ c.desc }}</div>
        <button class="btn claim">立即领取</button>
      </div>
    </section>

    <!-- 会员权益（横向胶囊卡） -->
    <section class="benefits container">
      <div class="benefit" v-for="(p,i) in privileges" :key="i">
        <div class="benefit-icon"><img :src="p.icon" alt="" /></div>
        <div class="benefit-text">
          <div class="b-title">{{ p.title }}</div>
          <div class="b-sub">{{ p.sub }}</div>
        </div>
      </div>
    </section>

    <section class="recs container">
      <h2 class="section-title">会员精选推荐</h2>
      <div class="rec-grid">
        <div class="rec" v-for="(r,i) in recs" :key="i">
          <div class="thumb"><img :src="r.cover" alt="" /></div>
          <div class="r-title" :title="r.title">{{ r.title }}</div>
          <div class="r-sub">{{ r.sub }}</div>
        </div>
      </div>
    </section>

    <!-- 扩充内容区：多组分区 -->
    <section class="vip-section container" v-for="(sec, si) in sections" :key="si">
      <div class="vip-section-head">
        <h2 class="section-title">{{ sec.title }}</h2>
        <a class="more" href="javascript:void(0)">更多 ›</a>
      </div>
      <div class="card-grid">
        <div class="card2" v-for="(item, ii) in sec.items" :key="ii">
          <div class="thumb"><img :src="item.cover" alt="" /></div>
          <div class="c-title" :title="item.title">{{ item.title }}</div>
          <div class="c-sub">{{ item.sub }}</div>
        </div>
      </div>
    </section>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'

const coupons = ref([
  { price: '10', desc: '大会员月卡立减' },
  { price: '5', desc: '大会员季卡立减' },
  { price: '10', desc: '大会员年卡立减' },
])

const privileges = ref([
  { icon: '/assets/trending.png', title: '番剧抢先', sub: '热门番剧优先看' },
  { icon: '/assets/feed.png', title: '超清画质', sub: '蓝光杜比视效' },
  { icon: '/assets/messages.png', title: '专属标识', sub: '尊享会员铭牌' },
  { icon: '/assets/favorites.png', title: '离线缓存', sub: '随时随地观看' },
  { icon: '/assets/history.png', title: '倍速播放', sub: '多档倍速自由选' },
  { icon: '/assets/creator-center.png', title: '专享活动', sub: '会员专属福利' },
])

const recs = ref([
  { cover: '/images/demo-cover1.jpg', title: '国创热播·仙剑奇侠传', sub: '超前点播' },
  { cover: '/images/demo-cover2.jpg', title: '番剧·巅峰对决', sub: '会员专享' },
  { cover: '/images/demo-cover3.jpg', title: '电影·顶级质感', sub: '杜比视界' },
  { cover: '/images/demo-cover4.jpg', title: '纪录片·自然奇境', sub: '超清蓝光' },
  { cover: '/images/demo-cover5.jpg', title: '综艺·欢乐满满', sub: '抢先看' },
  { cover: '/images/rec-4.jpg',       title: '动画·人气精选', sub: '火热更新' },
])

// 扩充的内容分区（与设计图一致的多组内容区）
const sections = ref([
  {
    title: '番剧精选',
    items: [
      { cover: '/images/demo-cover2.jpg', title: '热血对决', sub: '评分 9.2' },
      { cover: '/images/demo-cover3.jpg', title: '校园旋律', sub: '评分 8.8' },
      { cover: '/images/demo-cover4.jpg', title: '异世界冒险', sub: '会员抢先' },
      { cover: '/images/demo-cover5.jpg', title: '科幻群像', sub: '蓝光' },
      { cover: '/images/rec-1.jpg',       title: '恋爱喜剧', sub: '连载中' },
      { cover: '/images/rec-2.jpg',       title: '热血王道', sub: '更新至 12' },
    ]
  },
  {
    title: '电影精选',
    items: [
      { cover: '/images/demo-cover1.jpg', title: '奇幻史诗', sub: '4K 杜比' },
      { cover: '/images/demo-cover3.jpg', title: '悬疑反转', sub: '会员专享' },
      { cover: '/images/demo-cover4.jpg', title: '自然传奇', sub: '口碑佳作' },
      { cover: '/images/demo-cover5.jpg', title: '都市燃情', sub: '热门' },
      { cover: '/images/rec-3.jpg',       title: '黑色幽默', sub: '精选' },
      { cover: '/images/rec-4.jpg',       title: '合家欢', sub: '上新' },
    ]
  },
  {
    title: '纪录片·人文自然',
    items: [
      { cover: '/images/rec-1.jpg', title: '冰川纪事', sub: '自然' },
      { cover: '/images/rec-2.jpg', title: '海洋之心', sub: '探索' },
      { cover: '/images/rec-3.jpg', title: '城市记忆', sub: '人文' },
      { cover: '/images/demo-cover3.jpg', title: '星河纪元', sub: '科普' },
      { cover: '/images/demo-cover4.jpg', title: '荒野求生', sub: '自然' },
      { cover: '/images/demo-cover5.jpg', title: '地球奇旅', sub: '4K' },
    ]
  },
  {
    title: '人气动画',
    items: [
      { cover: '/images/demo-cover2.jpg', title: '热爱冒险的猫', sub: '人气' },
      { cover: '/images/demo-cover5.jpg', title: '陨落之翼', sub: '热播' },
      { cover: '/images/rec-4.jpg',       title: '奇妙乐园', sub: '热门' },
      { cover: '/images/demo-cover1.jpg', title: '星际航路', sub: '口碑' },
      { cover: '/images/demo-cover4.jpg', title: '风与海', sub: '治愈' },
      { cover: '/images/demo-cover3.jpg', title: '未来之城', sub: '上新' },
    ]
  }
])
</script>

<style scoped lang="scss">
.vip {
  background: #fff;
  min-width: 1200px;
}

.container {
  max-width: 1350px;
  margin: 0 auto;
  padding: 0 24px;
}

.vip-hero {
  background: linear-gradient(180deg, #f7b6c8 0%, #f6c7d3 50%, #fff 100%);
  padding-top: 28px;
  padding-bottom: 18px;

  .hero-inner { display: grid; grid-template-columns: 1fr 120px; align-items: center; }
  .hero-copy { padding: 20px 0 24px; }
  .title { font-size: 28px; color: #fff; font-weight: 700; text-shadow: 0 2px 8px rgba(0,0,0,.15); }
  .subtitle { margin-top: 6px; color: #fff; opacity: .95; }
  .cta-row { margin-top: 14px; display: flex; gap: 12px; }
  .btn { height: 32px; padding: 0 14px; border-radius: 8px; cursor: pointer; }
  .btn.primary { background: #fb7299; color: #fff; border: none; }
  .btn.ghost { background: transparent; color: #fff; border: 1px solid rgba(255,255,255,.9); }
  .big-badge { width: 72px; height: 72px; border-radius: 50%; background: rgba(255,255,255,.8); color: #fb7299; display:flex; align-items:center; justify-content:center; font-weight: 800; font-size: 28px; margin-left: auto; }

  .tabs { display: flex; gap: 10px; margin-top: 10px; }
  .tab { height: 28px; padding: 0 12px; background: rgba(255,255,255,.85); border: 1px solid rgba(255,255,255,.6); border-radius: 16px; color: #b54767; cursor: pointer; }
  .tab.active { background: #fff; color: #fb7299; border-color: #fff; box-shadow: 0 4px 12px rgba(251,114,153,.18); }
}

.coupon-row {
  margin: 6px auto 18px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .coupon {
    background: #ffffff;
    border-radius: 12px;
    border: 1px solid #f2e5ea;
    box-shadow: 0 6px 18px rgba(251,114,153,.12);
    padding: 14px 16px;
    display: grid;
    grid-template-columns: auto 1fr auto;
    align-items: center;
    gap: 10px;

    .price { font-size: 28px; color: #fb7299; font-weight: 700; }
    .currency { font-size: 14px; margin-right: 2px; }
    .desc { color: #7a7a7a; }
    .btn.claim { border: none; background: #fb7299; color: #fff; height: 32px; padding: 0 12px; border-radius: 8px; }
  }
}

/* 横向胶囊权益条 */
.benefits { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin: 8px auto 14px; }
.benefit { display:flex; align-items:center; gap:10px; background:#fff; border:1px solid #f0e6ea; border-radius:12px; padding:12px 14px; box-shadow: 0 4px 12px rgba(0,0,0,.04); }
.benefit-icon { width:36px; height:36px; border-radius:50%; background:#fff0f4; display:flex; align-items:center; justify-content:center; }
.benefit-icon img { width:20px; height:20px; }
.benefit .b-title { font-weight:600; color:#222; }
.benefit .b-sub { font-size:12px; color:#8a8a8a; }

.recs {
  margin: 8px auto 60px;
  .section-title { font-size: 18px; margin: 12px 0; color: #222; }
  .rec-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
  .rec { display: grid; gap: 6px; }
  .thumb { position: relative; padding-bottom: 56%; border-radius: 8px; overflow: hidden; background: #f1f2f3; }
  .thumb img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
  .r-title { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .r-sub { font-size: 12px; color: #8a8a8a; }
}

/* 统一的分区样式 */
.vip-section {
  margin: 8px auto 60px;
}
.vip-section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.vip-section .more {
  color: #909399;
  font-size: 13px;
  text-decoration: none;
}
.card-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.card2 { display: grid; gap: 6px; }
.card2 .thumb { position: relative; padding-bottom: 56%; border-radius: 8px; overflow: hidden; background: #f1f2f3; }
.card2 .thumb img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.card2 .c-title { font-size: 13px; color: #222; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card2 .c-sub { font-size: 12px; color: #8a8a8a; }
</style>



