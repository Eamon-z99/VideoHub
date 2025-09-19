<template>
  <div class="data-center-page">
    <header class="page-header">
      <h1>数据中心</h1>
      <div class="filters">
        <button class="filter-btn" :class="{active: range==='7d'}" @click="range='7d'">近7天</button>
        <button class="filter-btn" :class="{active: range==='30d'}" @click="range='30d'">近30天</button>
        <button class="filter-btn" :class="{active: range==='90d'}" @click="range='90d'">近90天</button>
      </div>
    </header>

    <!-- 指标卡片 -->
    <section class="kpi-grid">
      <div class="kpi-card" v-for="k in kpis" :key="k.key">
        <div class="kpi-header">
          <span class="kpi-title">{{ k.title }}</span>
          <span class="kpi-trend" :class="{ up: k.trend>0, down: k.trend<0 }">
            {{ k.trend>0 ? '+'+k.trend+'%' : k.trend+'%' }}
          </span>
        </div>
        <div class="kpi-value">{{ k.value }}</div>
        <div class="mini-chart"></div>
      </div>
    </section>

    <!-- 图表占位 -->
    <section class="charts">
      <div class="chart-card">
        <div class="chart-header">
          <h3>播放趋势</h3>
          <div class="legend">
            <span class="dot blue"></span><span>播放量</span>
          </div>
        </div>
        <div class="chart-placeholder"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>互动趋势</h3>
          <div class="legend">
            <span class="dot green"></span><span>点赞</span>
            <span class="dot orange"></span><span>评论</span>
          </div>
        </div>
        <div class="chart-placeholder"></div>
      </div>
    </section>

    <!-- 明细列表占位 -->
    <section class="table-card">
      <div class="table-header">
        <h3>作品表现（近{{ rangeLabel }}）</h3>
        <button class="outline-btn">导出数据</button>
      </div>
      <div class="table-placeholder">
        将在此展示作品明细列表（标题、播放、点赞、评论、投币、收藏等）
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const range = ref('7d')
const rangeLabel = computed(() => ({'7d':'7天','30d':'30天','90d':'90天'})[range.value])

const kpis = ref([
  { key: 'views', title: '总播放量', value: '128.4万', trend: 6.3 },
  { key: 'likes', title: '总点赞', value: '4.2万', trend: 3.1 },
  { key: 'comments', title: '总评论', value: '1.8万', trend: -1.4 },
  { key: 'followers', title: '新增粉丝', value: '1.2万', trend: 8.7 }
])
</script>

<style lang="scss" scoped>
.data-center-page {
  background: #f5f7fa;
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  h1 { margin: 0; font-size: 20px; color: #333; }
}

.filters {
  display: flex;
  gap: 8px;
  .filter-btn {
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    padding: 6px 12px;
    color: #333;
    cursor: pointer;
    &.active { background: #e6f7ff; color: #00aeec; border-color: #91d5ff; }
  }
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.kpi-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  .kpi-title { color: #666; font-size: 12px; }
  .kpi-trend { font-size: 12px; }
  .kpi-trend.up { color: #3bbd5a; }
  .kpi-trend.down { color: #ff4d4f; }
}

.kpi-value { font-size: 20px; font-weight: 600; color: #333; margin-bottom: 8px; }
.mini-chart { height: 36px; background: linear-gradient(180deg,#f8fbff,#eef5ff); border-radius: 6px; }

.charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  h3 { margin: 0; font-size: 14px; color: #333; }
  .legend { display: flex; align-items: center; gap: 8px; color: #666; font-size: 12px; }
  .dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
  .dot.blue { background: #69b1ff; }
  .dot.green { background: #95de64; }
  .dot.orange { background: #ffd666; }
}

.chart-placeholder { height: 220px; background: repeating-linear-gradient(45deg,#fafafa,#fafafa 8px,#f5f5f5 8px,#f5f5f5 16px); border-radius: 6px; }

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  h3 { margin: 0; font-size: 14px; color: #333; }
}

.outline-btn {
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  color: #333;
  padding: 6px 12px;
  cursor: pointer;
}

.table-placeholder { color: #666; font-size: 13px; padding: 16px; background: #fafafa; border-radius: 6px; }

@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .charts { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .kpi-grid { grid-template-columns: 1fr; }
}
</style>

