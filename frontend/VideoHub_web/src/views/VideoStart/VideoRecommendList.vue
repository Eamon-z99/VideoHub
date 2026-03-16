<template>
  <div class="recommend-wrapper">
    <div class="recommend-title">相关推荐</div>
    <div class="recommend-list">
      <div class="rec-item" v-for="rec in recommends" :key="rec.id" @click="openRecommend(rec)">
        <img :src="rec.cover" class="rec-cover" />
        <div class="rec-info">
          <div class="rec-title">{{ rec.title }}</div>
          <div class="rec-meta">
            <span><el-icon><View /></el-icon> {{ rec.plays }}</span>
            <span><el-icon><Timer /></el-icon> {{ rec.duration }}</span>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="showExpandBtn"
      class="recommend-expand"
      @click="toggleRecommends"
    >
      {{ recommendsExpanded ? '关闭' : '展开' }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { View, Timer } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { fetchVideosByUploader } from '@/api/video'

const props = defineProps({
  uploaderId: {
    type: [String, Number],
    default: null
  },
  currentVideoId: {
    type: String,
    default: ''
  },
  fallbackCover: {
    type: String,
    default: '/images/banner-1.jpg'
  }
})

const router = useRouter()

const MAX_RECOMMENDS = 40
const INITIAL_RECOMMENDS = 4

const allRecommends = ref([])
const recommends = ref([])
const recommendsExpanded = ref(false)

const showExpandBtn = computed(() => allRecommends.value.length > INITIAL_RECOMMENDS)

const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const formatCount = (count) => {
  if (!count || count === 0) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const refreshVisibleRecommends = () => {
  if (recommendsExpanded.value) {
    recommends.value = allRecommends.value
  } else {
    recommends.value = allRecommends.value.slice(0, INITIAL_RECOMMENDS)
  }
}

const loadRecommends = async () => {
  const uploaderId = props.uploaderId
  const currentVideoId = props.currentVideoId
  if (!uploaderId) {
    allRecommends.value = []
    recommends.value = []
    return
  }
  try {
    const { data } = await fetchVideosByUploader(uploaderId, MAX_RECOMMENDS, currentVideoId)
    const list = Array.isArray(data?.list) ? data.list : []
    const mapped = list.map(item => ({
      id: item.videoId || item.id,
      videoId: item.videoId || item.id,
      title: item.title || '本地视频',
      duration: formatDuration(item.duration),
      plays: item.viewCount ? formatCount(item.viewCount) : '本地视频',
      cover: (item.coverUrl && item.coverUrl.trim()) ? item.coverUrl : props.fallbackCover
    }))
    allRecommends.value = mapped.slice(0, MAX_RECOMMENDS)
    recommendsExpanded.value = false
    refreshVisibleRecommends()
  } catch (error) {
    console.warn('加载相关推荐失败:', error)
    allRecommends.value = []
    recommends.value = []
  }
}

const openRecommend = (rec) => {
  if (!rec || !rec.videoId) return
  const id = rec.videoId
  router.push({ path: `/video/${encodeURIComponent(id)}` })
}

const toggleRecommends = () => {
  if (!showExpandBtn.value) return
  recommendsExpanded.value = !recommendsExpanded.value
  refreshVisibleRecommends()
}

watch(
  () => [props.uploaderId, props.currentVideoId],
  () => {
    loadRecommends()
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.recommend-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>


