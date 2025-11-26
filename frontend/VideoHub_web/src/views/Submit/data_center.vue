<template>
  <div class="data-center-page">
    <!-- 顶部标签导航 -->
    <nav class="tab-nav">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="tab-item"
        :class="{ active: activeTab===t.key }"
        @click="activeTab=t.key"
      >{{ t.label }}</button>
    </nav>

    <!-- 数据概览面板 -->
    <section class="panel">
      <div class="panel-header">
        <h2>核心数据概览</h2>
        <div class="header-actions">
          <el-select v-model="overviewRange" size="small" style="width: 96px">
            <el-option label="近7天" value="7d" />
            <el-option label="近30天" value="30d" />
            <el-option label="近90天" value="90d" />
          </el-select>
          <el-button size="small" class="export-btn" plain>导出数据</el-button>
        </div>
      </div>

      <!-- 指标网格 -->
      <div class="metric-grid">
        <div v-for="m in coreMetrics" :key="m.key" class="metric-card">
          <div class="metric-top">
            <span class="metric-name">{{ m.title }}</span>
          </div>
          <div class="metric-value">{{ m.value }}</div>
        </div>
      </div>

      <!-- 播放趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="left">
            <h3>近{{ overviewRangeLabel }}播放量</h3>
            <div class="legend">
              <span class="dot pink"></span><span>总播放量</span>
              <span class="dot blue"></span><span>粉丝播放量</span>
            </div>
          </div>
        </div>
        <div class="chart-placeholder"></div>
      </div>
    </section>

    <!-- 收益数据面板 -->
    <section class="panel">
      <div class="panel-header">
        <h2>收益数据</h2>
        <div class="header-actions">
          <el-select v-model="incomeRange" size="small" style="width: 96px">
            <el-option label="近7天" value="7d" />
            <el-option label="近30天" value="30d" />
            <el-option label="近90天" value="90d" />
          </el-select>
          <el-button size="small" class="export-btn" plain>导出数据</el-button>
        </div>
      </div>

      <div class="income-kpis">
        <div class="income-card">
          <div class="income-title">总收益（元）</div>
          <div class="income-value">{{ incomeTotal }}</div>
        </div>
        <div class="income-card">
          <div class="income-title">自定义充值（元）</div>
          <div class="income-value">{{ incomeCustom }}</div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <div class="left">
            <h3>近30天总收益（元）</h3>
          </div>
        </div>
        <div class="chart-placeholder"></div>
      </div>
    </section>
  </div>
  
</template>

<script setup>
import { computed, ref } from 'vue'

// 顶部标签
const tabs = [
  { key: 'overview', label: '数据概览' },
  { key: 'account', label: '账号诊断' },
  { key: 'submission', label: '稿件分析' },
  { key: 'fans', label: '粉丝分析' },
  { key: 'column', label: '专栏数据' }
]
const activeTab = ref('overview')

// 概览指标
const coreMetrics = ref([
  { key: 'plays', title: '播放数', value: '0' },
  { key: 'visitors', title: '空间访客', value: '0' },
  { key: 'newFans', title: '涨粉的粉丝', value: '0' },
  { key: 'likes', title: '点赞', value: '0' },
  { key: 'favorites', title: '收藏', value: '0' },
  { key: 'coins', title: '硬币', value: '0' },
  { key: 'comments', title: '评论', value: '0' },
  { key: 'danmaku', title: '弹幕', value: '0' },
  { key: 'shares', title: '分享', value: '0' }
])

// 时间选择
const overviewRange = ref('7d')
const incomeRange = ref('30d')
const overviewRangeLabel = computed(() => ({ '7d': '7天', '30d': '30天', '90d': '90天' }[overviewRange.value]))

// 收益
const incomeTotal = ref('0')
const incomeCustom = ref('0')
</script>

<style lang="scss" scoped>
.data-center-page {
  background: transparent;
}

.tab-nav {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.tab-item {
  background: transparent;
  border: none;
  color: #666;
  padding: 6px 2px;
  cursor: pointer;
  position: relative;
}

.tab-item.active {
  color: #00aeec;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -6px;
  height: 2px;
  background: #00aeec;
}

.panel {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  margin-bottom: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  h2 { margin: 0; font-size: 14px; color: #333; }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.export-btn {
  border-color: #e5e7eb;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.metric-card {
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  padding: 12px;
}

.metric-top {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 12px;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-top: 6px;
}

.chart-card {
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  padding: 12px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  h3 { margin: 0; font-size: 14px; color: #333; }
  .legend { display: inline-flex; align-items: center; gap: 8px; color: #666; font-size: 12px; margin-left: 8px; }
  .dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
  .dot.pink { background: #ff85c0; }
  .dot.blue { background: #69b1ff; }
}

.chart-placeholder {
  height: 220px;
  background: repeating-linear-gradient(45deg,#fafafa,#fafafa 8px,#f5f5f5 8px,#f5f5f5 16px);
  border-radius: 6px;
}

.income-kpis {
  display: grid;
  grid-template-columns: 220px 220px;
  gap: 12px;
  margin-bottom: 12px;
}

.income-card {
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  padding: 12px;
}

.income-title { color: #666; font-size: 12px; }
.income-value { color: #333; font-size: 20px; font-weight: 600; margin-top: 6px; }

@media (max-width: 1200px) {
  .metric-grid { grid-template-columns: repeat(4, 1fr); }
}

@media (max-width: 768px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
  .income-kpis { grid-template-columns: 1fr; }
}
</style>

