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
            <el-option label="全部" value="all" />
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
          <span v-if="m.key === selectedChartType" class="metric-bg-icon" aria-hidden="true">
            <svg
              t="1774099618580"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="17363"
              width="72"
              height="72"
            >
              <path
                d="M360.896 183.968L269.984 95.872s-14.208-17.472 9.824-37.248c24.16-19.648 25.376-10.912 33.504-5.472s135.2 130.816 135.2 130.816h-87.616z m301.952 3.264l90.912-88.096s14.208-17.472-9.824-37.248c-24.032-19.648-25.376-10.912-33.504-5.472s-135.2 130.816-135.2 130.816h87.616z m341.152 163.104c-3.264-137.984-123.168-164.192-123.168-164.192s-614.336-4.96-742.496 0C10.176 222.304 20 350.336 20 350.336s1.696 274.272-0.128 413.12c13.824 138.848 120.864 160.928 120.864 160.928s42.72 0.864 73.92 0.864c3.264 8.992 5.696 52.544 54.24 52.544 48.416 0 54.24-52.544 54.24-52.544s354.88-1.696 384.352-1.696c1.696 14.816 8.992 54.976 57.536 54.24 48.416-0.864 51.712-57.536 51.712-57.536s16.384-1.696 65.664 0c114.944-21.376 121.6-156.064 121.6-156.064s-1.568-275.872 0-413.856z m-98.912 439.232c0 21.728-17.248 39.456-38.464 39.456H167.2c-21.248 0-38.464-17.6-38.464-39.456V326.336c0-21.728 17.248-39.456 38.464-39.456h699.424c21.248 0 38.464 17.6 38.464 39.456v463.232zM202.4 457.152l205.344-39.456 15.52 77.184-203.648 39.456z m638.976 0l-205.344-39.456-15.648 77.184 203.776 39.456z m-418.08 191.392s45.152 81.312 95.264-26.336c48.416 105.088 101.824 27.904 101.824 27.904l30.336 19.776s-56.672 91.136-131.424 22.208c-63.232 68.928-129.728-21.952-129.728-21.952l33.728-21.6z"
                fill="currentColor"
                p-id="17364"
              ></path>
            </svg>
          </span>
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
const overviewRangeLabel = computed(() => ({ '7d': '7天', '30d': '30天', '90d': '90天', all: '全部' }[overviewRange.value]))

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
  return overviewRange.value === 'all'
    ? `${rangeLabel}${metricLabel}`
    : `近${rangeLabel}${metricLabel}`
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
  const { dayKeys = [], labels = [] } = trendData.value
  const xAxisLabels = labels.length ? labels : dayKeys.map(d => d.slice(5))
  const seriesKey = selectedChartType.value === 'plays' ? 'totalPlays' : selectedChartType.value
  const seriesLabel = metricLabelMap[selectedChartType.value] || seriesKey
  const seriesColor = '#ff6699'
  const seriesData = trendData.value?.[seriesKey] || []
  const numericSeriesData = seriesData.map(v => Number(v) || 0)
  const maxVal = numericSeriesData.length ? Math.max(...numericSeriesData) : 0
  const nonZeroVals = numericSeriesData.filter(v => v > 0)
  const minNonZero = nonZeroVals.length ? Math.min(...nonZeroVals) : 0
  const useLogAxis = nonZeroVals.length >= 2 && minNonZero > 0 && (maxVal / minNonZero >= 50)
  const chartSeriesData = useLogAxis ? numericSeriesData.map(v => (v > 0 ? v : 0.1)) : numericSeriesData

  trendChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const first = params?.[0]
        if (!first) return ''
        const idx = first.dataIndex
        const day = dayKeys[idx] || first.axisValue
        return `${day}<br/>${seriesLabel}：${numericSeriesData[idx] ?? 0}`
      }
    },
    legend: { show: false },
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: xAxisLabels },
    yAxis: {
      type: useLogAxis ? 'log' : 'value',
      logBase: 10,
      minorSplitLine: { show: useLogAxis },
      min: useLogAxis ? 0.1 : null,
      axisLabel: {
        formatter: (val) => (useLogAxis && val < 1 ? '0' : val)
      }
    },
    series: [
      {
        name: seriesLabel,
        type: 'line',
        smooth: true,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3.5, color: seriesColor },
        itemStyle: { color: seriesColor },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 102, 153, 0.35)' },
            { offset: 1, color: 'rgba(255, 102, 153, 0.02)' }
          ])
        },
        data: chartSeriesData
      }
    ]
  }, { notMerge: true })
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
  const creatorId = userStore.user?.userId ?? userStore.user?.id
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
  background: #fafafa;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.metric-card.active {
  background: #ff6699;
  border-color: #ff6699;
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

.metric-bg-icon {
  position: absolute;
  right: 10px;
  top: 6px;
  color: rgba(255, 255, 255, 0.25);
  line-height: 0;
  pointer-events: none;
}

.metric-bg-icon svg {
  display: block;
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

