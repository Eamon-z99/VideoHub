<template>
  <div class="data-center-page">
    <div class="top-tabs">
      <div class="tab active">数据中心</div>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div class="title">核心数据概览</div>
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
        <div
          v-for="m in coreMetrics"
          :key="m.key"
          class="metric-card"
          :class="{ active: m.key === selectedChartType }"
          @click="onSelectMetric(m.key)"
        >
          <div class="metric-title">{{ m.title }}</div>
          <div class="metric-value">
            {{ m.value }}
          </div>
        </div>
      </div>

      <!-- 播放趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <div class="left">
            <h3>{{ chartTitle }}</h3>
          </div>
        </div>
        <div class="trend-chart" ref="trendChartRef"></div>
      </div>
    </section>
  </div>
  
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { fetchCreatorOverview } from '@/api/creatorStats'

// 概览指标
const coreMetrics = ref([
  { key: 'plays', title: '播放量', value: '0', unit: '' },
  { key: 'visitors', title: '空间访客', value: '0', unit: '', desc: '访问你个人空间的独立用户', trend: 0 },
  { key: 'newFans', title: '净增粉丝', value: '0', unit: '', desc: '本周期新增关注人数', trend: 0 },
  { key: 'likes', title: '点赞', value: '0', unit: '', desc: '视频获得的总点赞数', trend: 0 },
  { key: 'favorites', title: '收藏', value: '0', unit: '', desc: '被加入收藏夹的次数', trend: 0 },
  { key: 'coins', title: '投币', value: '0', unit: '', desc: '用户为你视频投出的硬币数', trend: 0 },
  { key: 'comments', title: '评论', value: '0', unit: '', desc: '视频下产生的评论量', trend: 0 },
  { key: 'danmaku', title: '弹幕', value: '0', unit: '', desc: '视频播放过程中发送的弹幕', trend: 0 }
])

// 时间选择
const overviewRange = ref('7d')
const overviewRangeLabel = computed(() => ({ '7d': '7天', '30d': '30天', '90d': '90天' }[overviewRange.value]))

// ========= 折线图交互状态 =========
const selectedChartType = ref('plays') // plays | visitors | newFans | likes | favorites | comments | danmaku | coins | shares

const metricLabelMap = {
  plays: '播放量',
  visitors: '空间访客',
  newFans: '净增粉丝',
  likes: '点赞',
  favorites: '收藏',
  coins: '投币',
  comments: '评论',
  danmaku: '弹幕'
}

const chartTitle = computed(() => {
  const rangeLabel = overviewRangeLabel.value
  const metricLabel = metricLabelMap[selectedChartType.value] || selectedChartType.value
  return `近${rangeLabel}${metricLabel}`
})

const trendData = ref(null)
const trendChartRef = ref(null)

let trendChartInstance = null

const initTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
  trendChartInstance = echarts.init(trendChartRef.value)
}

const setTrendChartOption = () => {
  if (!trendChartInstance || !trendData.value) return
  const { labels = [] } = trendData.value
  const seriesKey = selectedChartType.value === 'plays' ? 'totalPlays' : selectedChartType.value
  const seriesLabel = metricLabelMap[selectedChartType.value] || seriesKey
  const seriesColor = '#ff85c0'
  const seriesData = trendData.value?.[seriesKey] || []

  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { show: false },
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [
      {
        name: seriesLabel,
        type: 'line',
        smooth: true,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3 },
        itemStyle: { color: seriesColor },
        data: seriesData
      }
    ]
  })
}

const renderChart = () => {
  setTrendChartOption()
}

const onSelectMetric = (metricKey) => {
  selectedChartType.value = metricKey
  renderChart()
}

// 稿件表现（示例静态数据，后续可改为接口返回）
const workFilter = ref({
  range: '30d',
  sortBy: 'plays'
})

const workList = ref([
  // 示例结构，真实场景可由后端统计接口填充
  // {
  //   id: 1,
  //   title: '这是一个示例稿件标题，实际数据来自后端接口',
  //   publishTime: '2025-01-01',
  //   plays: 1234,
  //   likes: 120,
  //   coins: 40,
  //   favorites: 80,
  //   finishRate: '56.2%'
  // }
])

const loading = ref(false)

const loadOverviewData = async () => {
  const userStore = useUserStore()
  // pinia store 里用户对象叫 `user`，不是 `userInfo`
  // 这里做兜底，避免字段名不一致导致 creatorId 传空
  const creatorId = userStore.user?.id ?? userStore.user?.userId
  if (!creatorId) return
  loading.value = true
  try {
    const { data } = await fetchCreatorOverview({
      creatorId,
      range: overviewRange.value
    })
    if (data?.success && data.data) {
      const core = data.data.coreMetrics || {}
      coreMetrics.value = coreMetrics.value.map(m => {
        const key = m.key
        const rawVal = core[key]
        const val = typeof rawVal === 'number' ? String(rawVal) : (rawVal ?? '0')
        return {
          ...m,
          value: val
        }
      })
      trendData.value = data.data.trend || null
      renderChart()
    }
  } catch (e) {
    // 已在 request 拦截器中统一提示
  } finally {
    loading.value = false
  }
}

watch(overviewRange, () => {
  loadOverviewData()
})

onMounted(() => {
  initTrendChart()
  loadOverviewData()
  window.addEventListener('resize', handleResize)
})

const handleResize = () => {
  if (trendChartInstance) {
    trendChartInstance.resize()
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }
})
</script>

<style lang="scss" scoped>
.data-center-page {
  padding: 0;
  --dc-tabs-content-left: 35px;
}

.top-tabs {
  display: flex;
  gap: 18px;
  border-bottom: 1.5px solid #eef2f7;
  padding: 0 35px;
  height: 64px;
  margin-bottom: 12px;
  align-items: center;
}

.tab {
  background: transparent;
  border: none;
  color: #374151;
  padding: 0;
  cursor: default;
  height: 64px;
  line-height: 64px;
  font-size: 16px;
  border-bottom: 3px solid transparent;
}

.tab.active {
  color: #00a1d6;
  font-weight: 700;
  border-bottom-color: #00a1d6;
}

.panel {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  padding-left: var(--dc-tabs-content-left);
  margin-bottom: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.metric-card {
  background: #fff;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s ease;
}

.metric-card.active {
  background: #ff85c0;
  border-color: #ff85c0;
}

.metric-title {
  color: #374151;
  font-size: 13px;
  font-weight: 600;
}

.metric-card.active .metric-title {
  color: #fff;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-top: 6px;
}

.metric-card.active .metric-value {
  color: #fff;
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
}

.trend-chart {
  height: 220px;
  width: 100%;
}


@media (max-width: 1200px) {
  .metric-grid { grid-template-columns: repeat(4, 1fr); }
}

@media (max-width: 768px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

