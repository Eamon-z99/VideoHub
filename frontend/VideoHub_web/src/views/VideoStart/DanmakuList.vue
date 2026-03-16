<template>
  <div class="danmaku-list-section-inner">
    <div class="danmaku-list-header" @click="toggleDanmakuList">
      <span class="header-title">弹幕列表</span>
      <el-icon class="header-arrow" :class="{ 'is-expanded': expanded }">
        <ArrowUp />
      </el-icon>
    </div>
    <div v-if="expanded" class="danmaku-list-content">
      <div class="danmaku-table">
        <div class="danmaku-table-header">
          <div class="col-time">时间</div>
          <div class="col-content">
            弹幕内容<span v-if="danmakuDateLabel">（{{ danmakuDateLabel }}）</span>
          </div>
          <div class="col-send-time">发送时间</div>
        </div>
        <div class="danmaku-table-body">
          <div v-if="loadingDanmakuList" class="danmaku-loading">加载中...</div>
          <div v-else-if="danmakuList.length === 0" class="danmaku-empty">
            {{ danmakuDateLabel ? '该日暂无弹幕' : '暂无弹幕' }}
          </div>
          <div
            v-else
            v-for="(item, index) in danmakuList"
            :key="index"
            class="danmaku-table-row"
          >
            <div class="col-time">{{ formatVideoTime(item.time) }}</div>
            <div class="col-content">{{ item.content }}</div>
            <div class="col-send-time">{{ formatSendTime(item.sendTime) }}</div>
          </div>
        </div>
      </div>
      <div class="danmaku-history-picker">
        <div class="danmaku-history-btn">
          <span class="history-text">查询历史弹幕</span>
          <el-date-picker
            v-model="danmakuHistoryDate"
            type="date"
            size="small"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="查询历史弹幕"
            :disabled-date="disableDanmakuDate"
            @change="handleDanmakuHistoryChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'
import { getDanmakuList, getDanmakuByDate } from '@/api/danmaku'

const props = defineProps({
  videoId: {
    type: String,
    required: true
  },
  danmakuMinDate: {
    type: Date,
    default: null
  },
  expanded: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:expanded'])

const internalExpanded = ref(props.expanded)
watch(
  () => props.expanded,
  (val) => {
    internalExpanded.value = val
  }
)

watch(internalExpanded, (val) => {
  emit('update:expanded', val)
  if (val && danmakuList.value.length === 0) {
    loadDanmakuList()
  }
})

const danmakuList = ref([])
const danmakuListTotal = ref(0)
const loadingDanmakuList = ref(false)
const danmakuHistoryDate = ref(null) // Date 对象
const danmakuDateLabel = ref('') // 用于显示在“弹幕内容 (3月14日)”中

const toggleDanmakuList = () => {
  internalExpanded.value = !internalExpanded.value
}

const loadDanmakuList = async () => {
  if (!props.videoId) return
  loadingDanmakuList.value = true
  try {
    const { data } = await getDanmakuList(props.videoId)
    if (data && data.success) {
      const list = Array.isArray(data.list) ? data.list : []
      danmakuListTotal.value = data.total || 0
      danmakuList.value = list
      danmakuDateLabel.value = ''
    }
  } catch (error) {
    console.error('加载弹幕列表失败:', error)
  } finally {
    loadingDanmakuList.value = false
  }
}

const formatDateLabel = (isoDateStr) => {
  if (!isoDateStr) return ''
  const d = new Date(isoDateStr)
  if (Number.isNaN(d.getTime())) return ''
  const m = d.getMonth() + 1
  const day = d.getDate()
  return `${m}月${day}日`
}

const loadDanmakuByDate = async (dateObj) => {
  if (!props.videoId || !dateObj) return

  const year = dateObj.getFullYear()
  const month = String(dateObj.getMonth() + 1).padStart(2, '0')
  const day = String(dateObj.getDate()).padStart(2, '0')
  const dateStr = `${year}-${month}-${day}`

  loadingDanmakuList.value = true
  try {
    const { data } = await getDanmakuByDate(props.videoId, dateStr)
    if (data && data.success) {
      const list = Array.isArray(data.list) ? data.list : []
      danmakuListTotal.value = data.total || list.length
      danmakuList.value = list
      danmakuDateLabel.value = formatDateLabel(dateStr)
    }
  } catch (error) {
    console.error('按日期加载弹幕失败:', error)
  } finally {
    loadingDanmakuList.value = false
  }
}

const handleDanmakuHistoryChange = (val) => {
  if (!val) return
  const dateObj = typeof val === 'string' ? new Date(val) : val
  loadDanmakuByDate(dateObj)
}

const disableDanmakuDate = (time) => {
  const d = new Date(time)
  d.setHours(0, 0, 0, 0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  if (props.danmakuMinDate) {
    const min = new Date(props.danmakuMinDate)
    min.setHours(0, 0, 0, 0)
    if (d < min) return true
  }

  return d > today
}

const formatVideoTime = (seconds) => {
  if (seconds == null || seconds < 0) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const formatSendTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  if (isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped lang="scss">
.danmaku-list-section-inner {
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.danmaku-history-picker {
  padding: 10px 0 0;
  display: flex;
  justify-content: flex-end;
}

.danmaku-history-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  line-height: 1;
}

.history-text {
  color: #000;
  font-size: 14px;
}

/* 让日期选择器可点击但完全“隐形”，只显示文字 */
.danmaku-history-btn :deep(.el-date-editor) {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
}

.danmaku-history-btn :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
}

.danmaku-history-btn :deep(.el-input__inner) {
  cursor: pointer;
}
</style>


