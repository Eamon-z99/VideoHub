<template>
  <div class="mall">
    <div class="mall-header">
      <div class="logo">bilibili 会员购</div>
      <div class="search-wrap">
        <input class="search-input" placeholder="活动、聚会、场馆" />
      </div>
      <div class="user-mini" />
    </div>

    <div class="mall-tabs">
      <div class="tab" :class="{ active: activeTab==='exhibition' }" @click="activeTab='exhibition'">漫展演出</div>
      <div class="tab" :class="{ active: activeTab==='around' }" @click="activeTab='around'">B站周边</div>
    </div>

    <div class="filters">
      <div class="filter-row">
        <span class="label">城市：</span>
        <div class="options">
          <span v-for="c in cityFilters" :key="c" :class="['opt', {active: c===selected.city}]" @click="selected.city=c">{{ c }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="label">类型：</span>
        <div class="options">
          <span v-for="t in typeFilters" :key="t" :class="['opt', {active: t===selected.type}]" @click="selected.type=t">{{ t }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="label">筛选：</span>
        <div class="options">
          <span v-for="s in sortFilters" :key="s" :class="['opt', {active: s===selected.sort}]" @click="selected.sort=s">{{ s }}</span>
        </div>
      </div>
    </div>

    <div class="card-grid">
      <div v-for="(item, i) in items" :key="i" class="card">
        <div class="cover">
          <img :src="item.cover" alt="cover" />
        </div>
        <div class="info">
          <div class="title" :title="item.title">{{ item.title }}</div>
          <div class="meta">
            <span>📅 {{ item.date }}</span>
            <span>📍 {{ item.place }}</span>
          </div>
          <div class="price-row">
            <span class="price">¥ {{ item.price }}</span>
            <button class="btn">抢购</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const activeTab = ref('exhibition')
const cityFilters = ['全国','上海','北京','深圳','杭州','广州','成都','南京','天津','武汉','更多城市']
const typeFilters = ['全部类型','演出','展览','盛典','本地生活']
const sortFilters = ['全部时间','本周','本月']
const selected = reactive({ city: '全国', type: '全部类型', sort: '全部时间' })

const items = ref([
  { cover: '/images/mall-1.jpg', title: '北京·帝都惊悚ONLY同人 1.0', date: '2025-10-25', place: '北京 大红门会展中心', price: 78 },
  { cover: '/images/mall-2.jpg', title: '武汉·第四届超电动漫游戏嘉年华', date: '2025-11-23', place: '武汉客厅中国文化博览中心', price: 72 },
  { cover: '/images/mall-3.jpg', title: '南京·第四届超电动漫嘉年华', date: '2025-11-15 - 2025-11-16', place: 'KUMO联盟青春年少A馆', price: 70 },
  { cover: '/images/mall-4.jpg', title: '哈尔滨·第五人格only同人展5.0-周年庆典', date: '2025-11-02', place: '艾米达大悦城礼庄园', price: 64.9 }
])
</script>

<style scoped lang="scss">
.mall {
  min-width: 1200px;
  max-width: 1350px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.mall-header {
  display: grid;
  grid-template-columns: 200px 1fr 48px;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .logo { font-weight: 700; color: #fb7299; letter-spacing: .5px; }

  .search-wrap {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0,0,0,.04);
    padding: 8px 12px;
    .search-input { border: 0; outline: 0; width: 100%; font-size: 14px; }
  }

  .user-mini { width: 32px; height: 32px; border-radius: 50%; background: #e9e9e9; }
}

.mall-tabs {
  display: flex;
  gap: 24px;
  margin: 8px 0 12px;
  border-bottom: 2px solid #ffe5ee;

  .tab {
    padding: 10px 2px;
    color: #999;
    cursor: pointer;
    position: relative;
    &.active {
      color: #fb7299;
    }
    &.active::after {
      content: '';
      position: absolute;
      left: 0; right: 0; bottom: -2px; height: 2px;
      background: #fb7299;
    }
  }
}

.filters {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);

  .filter-row { display: flex; gap: 8px; padding: 6px 0; align-items: center; }
  .label { color: #999; width: 48px; flex-shrink: 0; }
  .options { display: flex; flex-wrap: wrap; gap: 10px; }
  .opt { color: #6b6b6b; padding: 4px 8px; border-radius: 4px; cursor: pointer; }
  .opt.active, .opt:hover { color: #fb7299; background: #fff1f5; }
}

.card-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,.05);
  overflow: hidden;
  display: grid;
  grid-template-columns: 140px 1fr;
  column-gap: 16px;
  padding: 14px;

  .cover {
    width: 140px; height: 180px; border-radius: 8px; overflow: hidden; background: #f5f5f5;
    img { width: 100%; height: 100%; object-fit: cover; }
  }

  .info {
    display: grid; grid-template-rows: auto auto 1fr; row-gap: 8px;
    .title { font-size: 16px; color: #222; font-weight: 600; line-height: 1.3; }
    .meta { font-size: 12px; color: #8a8a8a; display: flex; gap: 14px; }
    .price-row { margin-top: auto; display: flex; align-items: center; gap: 10px; }
    .price { color: #fb7299; font-weight: 700; }
    .btn { margin-left: auto; background: #ff6699; color: #fff; border: 0; padding: 6px 10px; border-radius: 6px; cursor: pointer; }
  }
}
</style>


