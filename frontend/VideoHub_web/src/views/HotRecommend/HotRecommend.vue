<template>
  <div class="hot-page">
    <TopHeader :transparent-at-top="false" />

    <main class="hot-main">
      <section class="hot-header">
        <h1 class="hot-title">为你推荐</h1>
        <p class="hot-subtitle">基于你的观看与互动行为生成的个性化推荐</p>
      </section>

      <section class="hot-content">
        <div class="hot-toolbar">
          <div class="algo-switch">
            <button
              class="algo-btn"
              :class="{ 'is-active': activeAlgo === 'user' }"
              :disabled="loading"
              @click="switchAlgo('user')"
            >
              按相似用户
            </button>
            <button
              class="algo-btn"
              :class="{ 'is-active': activeAlgo === 'item' }"
              :disabled="loading"
              @click="switchAlgo('item')"
            >
              按相似视频
            </button>
            <button
              class="algo-btn"
              :class="{ 'is-active': activeAlgo === 'mf' }"
              :disabled="loading"
              @click="switchAlgo('mf')"
            >
              潜在因子(MF)
            </button>
          </div>
          <button
            class="refresh-btn"
            :disabled="loading"
            @click="refresh"
          >
            {{ loading ? '刷新中...' : '换一换推荐' }}
          </button>
          <span v-if="error" class="error-text">{{ error }}</span>
        </div>

        <div v-if="loading && videos.length === 0" class="hot-loading">
          正在为你生成推荐...
        </div>

        <div v-else-if="!loading && videos.length === 0" class="hot-empty">
          暂时没有推荐结果，去多看看视频、点点赞，再回来试试吧～
        </div>

        <div v-else class="hot-grid">
          <div
            v-for="item in videos"
            :key="item.id"
            class="hot-card"
            @click="openVideo(item)"
          >
            <VideoCard :video="item" :on-img-error="onImgError" />
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import VideoCard from '@/components/VideoCard.vue'
import { useUserStore } from '@/stores/user'
import { getRecommendations, getItemBasedRecommendations, getMfRecommendations } from '@/api/recommendation'

const router = useRouter()
const userStore = useUserStore()

const videos = ref([])
const loading = ref(false)
const error = ref('')
const activeAlgo = ref('user') // 'user' | 'item' | 'mf'

const userId = computed(() => {
  const raw = userStore.user?.id || userStore.user?.userId
  return raw ? Number(raw) : null
})

const normalizeVideo = (v) => {
  if (!v) return {}
  return {
    id: v.id ?? v.videoId ?? v.vid,
    videoId: v.videoId ?? v.id ?? v.vid,
    title: v.title || '未命名视频',
    cover: v.cover || v.coverUrl || '/images/default-cover.png',
    duration: v.duration || '00:00',
    playCount: v.playCount ?? v.views ?? 0,
    commentCount: v.commentCount ?? v.comments ?? 0,
    uploaderName: v.uploaderName || v.up || v.author || '佚名',
    uploadDate: v.uploadDate || v.pubTime || '',
    isVideo: true
  }
}

const fetchData = async () => {
  error.value = ''
  if (!userId.value) {
    videos.value = []
    error.value = '请先登录以获取个性化推荐'
    return
  }
  loading.value = true
  try {
    let api
    if (activeAlgo.value === 'item') {
      api = getItemBasedRecommendations
    } else if (activeAlgo.value === 'mf') {
      api = getMfRecommendations
    } else {
      api = getRecommendations
    }

    const { data } = await api(userId.value, 30)
    const list = (data && (data.list || data.data || data)) || []
    videos.value = Array.isArray(list) ? list.map(normalizeVideo) : []
  } catch (e) {
    console.error('加载推荐失败:', e)
    error.value = '加载推荐失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  if (!loading.value) {
    fetchData()
  }
}

const switchAlgo = (algo) => {
  if (activeAlgo.value === algo) return
  activeAlgo.value = algo
  // 切换算法时重新加载推荐
  fetchData()
}

const openVideo = (item) => {
  const id = item.videoId || item.id
  if (!id) return
  router.push({ name: 'video', params: { id } })
}

const onImgError = (e) => {
  if (e && e.target) {
    e.target.src = '/images/default-cover.png'
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.hot-page {
  min-width: 1300px;
  background: #ffffff;
  min-height: 100vh;
  padding-top: 64px;
}

.hot-main {
  width: 86%;
  max-width: 1800px;
  margin: 16px auto 40px;
}

.hot-header {
  margin-bottom: 16px;
}

.hot-title {
  font-size: 22px;
  font-weight: 700;
  color: #18191c;
  margin: 0;
}

.hot-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9499a0;
}

.hot-content {
  margin-top: 12px;
}

.hot-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.algo-switch {
  display: inline-flex;
  padding: 2px;
  border-radius: 999px;
  background: #f1f2f3;
}

.algo-btn {
  border: none;
  background: transparent;
  padding: 4px 10px;
  font-size: 12px;
  color: #61666d;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.algo-btn.is-active {
  background: #00a1d6;
  color: #fff;
  font-weight: 500;
}

.algo-btn:disabled {
  opacity: 0.7;
  cursor: default;
}

.refresh-btn {
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid #00a1d6;
  background: #e5f6ff;
  color: #00a1d6;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.refresh-btn:not(:disabled):hover {
  background: #00a1d6;
  color: #fff;
}

.error-text {
  font-size: 13px;
  color: #f56c6c;
}

.hot-loading,
.hot-empty {
  padding: 40px 0;
  text-align: center;
  color: #9499a0;
  font-size: 14px;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px 20px;
}

.hot-card {
  cursor: pointer;
}

@media (max-width: 1500px) {
  .hot-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .hot-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>


