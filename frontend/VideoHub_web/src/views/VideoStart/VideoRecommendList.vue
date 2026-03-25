<template>
  <div class="recommend-wrapper">
    <div class="recommend-title">相关推荐</div>
    <div class="recommend-list">
      <div class="rec-item" v-for="rec in recommends" :key="rec.id" @click="openRecommend(rec)">
        <div
          class="rec-cover-wrap"
          @mouseenter="hoverVideoId = rec.videoId"
          @mouseleave="hoverVideoId = null"
        >
          <video
            v-if="hoverVideoId === rec.videoId"
            :src="rec.playUrl"
            class="rec-cover"
            :poster="rec.cover"
            muted
            autoplay
            loop
            playsinline
            preload="metadata"
            disablePictureInPicture
            disableRemotePlayback
            controlsList="nodownload noplaybackrate nofullscreen noremoteplayback"
            @contextmenu.prevent
            @dblclick.prevent
          />
          <img v-else :src="rec.cover" class="rec-cover" />
          <div class="rec-time" v-if="hoverVideoId !== rec.videoId">{{ rec.duration }}</div>
          <div v-if="hoverVideoId === rec.videoId" class="rec-play-overlay" aria-hidden="true">
            <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
              <path d="M7.4 5.2v7.6l6.4-3.8L7.4 5.2z" fill="currentColor" />
            </svg>
          </div>
        </div>
        <div class="rec-info">
          <div class="rec-title">{{ rec.title }}</div>
          <div class="rec-author" @click.stop="openUserProfile(rec)">
            <svg style="width:18px;height:18px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="20" height="20">
              <path d="M4.612500000000001 6.186037499999999C4.92315 6.186037499999999 5.175000000000001 6.437872500000001 5.175000000000001 6.748537499999999L5.175000000000001 9.580575C5.175000000000001 10.191075000000001 5.66991 10.686 6.280425000000001 10.686C6.8909325 10.686 7.38585 10.191075000000001 7.38585 9.580575L7.38585 6.748537499999999C7.38585 6.437872500000001 7.637700000000001 6.186037499999999 7.94835 6.186037499999999C8.259 6.186037499999999 8.51085 6.437872500000001 8.51085 6.748537499999999L8.51085 9.580575C8.51085 10.8124125 7.512262499999999 11.811 6.280425000000001 11.811C5.048595000000001 11.811 4.050000000000001 10.8124125 4.050000000000001 9.580575L4.050000000000001 6.748537499999999C4.050000000000001 6.437872500000001 4.3018350000000005 6.186037499999999 4.612500000000001 6.186037499999999z" fill="currentColor"></path>
              <path d="M9.48915 6.748537499999999C9.48915 6.437872500000001 9.7409625 6.186037499999999 10.05165 6.186037499999999L11.79375 6.186037499999999C12.984637500000002 6.186037499999999 13.950000000000001 7.151415 13.950000000000001 8.34225C13.950000000000001 9.5331375 12.984637500000002 10.4985 11.79375 10.4985L10.61415 10.4985L10.61415 11.2485C10.61415 11.55915 10.3623 11.811 10.05165 11.811C9.7409625 11.811 9.48915 11.55915 9.48915 11.2485L9.48915 6.748537499999999zM10.61415 9.3735L11.79375 9.3735C12.3633 9.3735 12.825000000000001 8.9118 12.825000000000001 8.34225C12.825000000000001 7.7727375 12.3633 7.31103 11.79375 7.31103L10.61415 7.31103L10.61415 9.3735z" fill="currentColor"></path>
              <path d="M9 3.7485375000000003C7.111335 3.7485375000000003 5.46225 3.84462 4.2981675 3.939015C3.4891575 4.0046175 2.8620825 4.6226400000000005 2.79 5.424405C2.7045525 6.37485 2.625 7.6282499999999995 2.625 8.9985C2.625 10.368825000000001 2.7045525 11.622225 2.79 12.5726625C2.8620825 13.374412500000002 3.4891575 13.992450000000002 4.2981675 14.058074999999999C5.46225 14.152425000000001 7.111335 14.2485 9 14.2485C10.888874999999999 14.2485 12.538050000000002 14.152425000000001 13.702200000000001 14.058037500000001C14.511074999999998 13.992412500000002 15.138000000000002 13.3746 15.210075 12.573037500000002C15.295499999999999 11.622975 15.375 10.3698375 15.375 8.9985C15.375 7.627237500000001 15.295499999999999 6.3740775 15.210075 5.4240375C15.138000000000002 4.622475 14.511074999999998 4.00464 13.702200000000001 3.9390374999999995C12.538050000000002 3.844635 10.888874999999999 3.7485375000000003 9 3.7485375000000003zM4.2072375 2.8176975C5.39424 2.7214425 7.074434999999999 2.6235375000000003 9 2.6235375000000003C10.925775 2.6235375000000003 12.606075 2.7214575 13.793099999999999 2.81772C15.141074999999999 2.92704 16.208849999999998 3.9695849999999995 16.330575 5.323297500000001C16.418174999999998 6.297675 16.5 7.585537500000001 16.5 8.9985C16.5 10.4115375 16.418174999999998 11.6994 16.330575 12.6738C16.208849999999998 14.027474999999999 15.141074999999999 15.0700125 13.793099999999999 15.1793625C12.606075 15.275625 10.925775 15.3735 9 15.3735C7.074434999999999 15.3735 5.39424 15.275625 4.2072375 15.179400000000001C2.859045 15.070049999999998 1.7912325 14.027212500000001 1.6695225000000002 12.673425C1.5818849999999998 11.69865 1.5 10.4106 1.5 8.9985C1.5 7.586475 1.5818849999999998 6.2984025 1.6695225000000002 5.3236725C1.7912325 3.96984 2.859045 2.9270175000000003 4.2072375 2.8176975z" fill="currentColor"></path>
            </svg>
            <span class="rec-author-name">{{ rec.author }}</span>
          </div>
          <div class="rec-meta">
            <span class="rec-meta-item">
              <svg class="play" style="width:18px;height:18px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="20" height="20">
                <path d="M9 3.7485375000000003C7.111335 3.7485375000000003 5.46225 3.84462 4.2981675 3.939015C3.4891575 4.0046175 2.8620825 4.6226400000000005 2.79 5.424405C2.7045525 6.37485 2.625 7.6282499999999995 2.625 8.9985C2.625 10.368825000000001 2.7045525 11.622225 2.79 12.5726625C2.8620825 13.374412500000002 3.4891575 13.992450000000002 4.2981675 14.058074999999999C5.46225 14.152425000000001 7.111335 14.2485 9 14.2485C10.888874999999999 14.2485 12.538050000000002 14.152425000000001 13.702200000000001 14.058037500000001C14.511074999999998 13.9924125 15.138000000000002 13.3746 15.210075 12.573037500000002C15.295499999999999 11.622975 15.375 10.3698375 15.375 8.9985C15.375 7.627237500000001 15.295499999999999 6.3740775 15.210075 5.4240375C15.138000000000002 4.622475 14.511074999999998 4.00464 13.702200000000001 3.9390374999999995C12.538050000000002 3.844635 10.888874999999999 3.7485375000000003 9 3.7485375000000003zM4.2072375 2.8176975C5.39424 2.7214425 7.074434999999999 2.6235375000000003 9 2.6235375000000003C10.925775 2.6235375000000003 12.606075 2.7214575 13.793099999999999 2.81772C15.141074999999999 2.92704 16.208849999999998 3.9695849999999995 16.330575 5.323297500000001C16.418174999999998 6.297675 16.5 7.585537500000001 16.5 8.9985C16.5 10.4115375 16.418174999999998 11.6994 16.330575 12.6738C16.208849999999998 14.027474999999999 15.141074999999999 15.0700125 13.793099999999999 15.1793625C12.606075 15.275625 10.925775 15.3735 9 15.3735C7.074434999999999 15.3735 5.39424 15.275625 4.2072375 15.179400000000001C2.859045 15.070049999999998 1.7912325 14.027212500000001 1.6695225000000002 12.673425C1.5818849999999998 11.69865 1.5 10.4106 1.5 8.9985C1.5 7.586475 1.5818849999999998 6.2984025 1.6695225000000002 5.3236725C1.7912325 3.96984 2.859045 2.9270175000000003 4.2072375 2.8176975z" fill="currentColor"></path>
                <path d="M11.035350000000001 8.2265625C11.6307375 8.570325 11.6307375 9.42975 11.0353125 9.773475L8.652149999999999 11.149425C8.0567625 11.49315 7.3125 11.063475 7.3125075 10.37595L7.3125075 7.624124999999999C7.3125075 6.936607500000001 8.0567625 6.5069025 8.652149999999999 6.850664999999999L11.035350000000001 8.2265625z" fill="currentColor"></path>
              </svg>
              {{ rec.plays }}
            </span>
            <span class="rec-meta-item">
              <svg class="dm" style="width:18px;height:18px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="20" height="20">
                <path d="M9 3.7485375000000003C7.111335 3.7485375000000003 5.46225 3.84462 4.2981675 3.939015C3.4891575 4.0046175 2.8620825 4.6226400000000005 2.79 5.424405C2.7045525 6.37485 2.625 7.6282499999999995 2.625 8.9985C2.625 10.368825000000001 2.7045525 11.622225 2.79 12.5726625C2.8620825 13.374412500000002 3.4891575 13.992450000000002 4.2981675 14.058074999999999C5.46225 14.152425000000001 7.111335 14.2485 9 14.2485C10.888874999999999 14.2485 12.538050000000002 14.152425000000001 13.702200000000001 14.058037500000001C14.511074999999998 13.992412500000002 15.138000000000002 13.3746 15.210075 12.573037500000002C15.295499999999999 11.622975 15.375 10.3698375 15.375 8.9985C15.375 7.627237500000001 15.295499999999999 6.3740775 15.210075 5.4240375C15.138000000000002 4.622475 14.511074999999998 4.00464 13.702200000000001 3.9390374999999995C12.538050000000002 3.844635 10.888874999999999 3.7485375000000003 9 3.7485375000000003zM4.2072375 2.8176975C5.39424 2.7214425 7.074434999999999 2.6235375000000003 9 2.6235375000000003C10.925775 2.6235375000000003 12.606075 2.7214575 13.793099999999999 2.81772C15.141074999999999 2.92704 16.208849999999998 3.9695849999999995 16.330575 5.323297500000001C16.418174999999998 6.297675 16.5 7.585537500000001 16.5 8.9985C16.5 10.4115375 16.418174999999998 11.6994 16.330575 12.6738C16.208849999999998 14.027474999999999 15.141074999999999 15.0700125 13.793099999999999 15.1793625C12.606075 15.275625 10.925775 15.3735 9 15.3735C7.074434999999999 15.3735 5.39424 15.275625 4.2072375 15.179400000000001C2.859045 15.070049999999998 1.7912325 14.027212500000001 1.6695225000000002 12.673425C1.5818849999999998 11.69865 1.5 10.4106 1.5 8.9985C1.5 7.586475 1.5818849999999998 6.2984025 1.6695225000000002 5.3236725C1.7912325 3.96984 2.859045 2.9270175000000003 4.2072375 2.8176975z" fill="currentColor"></path>
                <path d="M11.90625 8.0625L7.40625 8.0625C7.0955924999999995 8.0625 6.84375 7.810649999999999 6.84375 7.5C6.84375 7.1893424999999995 7.0955924999999995 6.9375 7.40625 6.9375L11.90625 6.9375C12.2169 6.9375 12.46875 7.189342499999999 12.46875 7.5C12.46875 7.810649999999999 12.2169 8.0625 11.90625 8.0625z" fill="currentColor"></path>
                <path d="M13.03125 11.0625L8.53125 11.0625C8.220600000000001 11.0625 7.96875 10.810649999999999 7.96875 10.5C7.96875 10.189350000000001 8.220600000000001 9.9375 8.53125 9.9375L13.03125 9.9375C13.3419 9.9375 13.59375 10.189350000000001 13.59375 10.5C13.59375 10.810649999999999 13.3419 11.0625 13.03125 11.0625z" fill="currentColor"></path>
                <path d="M5.90625 7.5C5.90625 7.810649999999999 5.654407500000001 8.0625 5.34375 8.0625L4.96875 8.0625C4.6580925 8.0625 4.40625 7.810649999999999 4.40625 7.5C4.40625 7.1893424999999995 4.6580925 6.9375 4.96875 6.9375L5.34375 6.9375C5.6544075 6.9375 5.90625 7.1893424999999995 5.90625 7.5z" fill="currentColor"></path>
                <path d="M7.03125 10.5C7.03125 10.810649999999999 6.7794075000000005 11.0625 6.46875 11.0625L6.09375 11.0625C5.7830925 11.0625 5.53125 10.810649999999999 5.53125 10.5C5.53125 10.189350000000001 5.7830925 9.9375 6.09375 9.9375L6.46875 9.9375C6.7794075 9.9375 7.03125 10.189350000000001 7.03125 10.5z" fill="currentColor"></path>
              </svg>
              {{ rec.comments }}
            </span>
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
const INITIAL_RECOMMENDS = 20

const allRecommends = ref([])
const recommends = ref([])
const recommendsExpanded = ref(false)
const hoverVideoId = ref(null)

const showExpandBtn = computed(() => allRecommends.value.length > INITIAL_RECOMMENDS)

const formatCount = (count) => {
  if (!count || count === 0) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const formatDuration = (seconds) => {
  if (seconds === null || seconds === undefined) return '--:--'
  const n = typeof seconds === 'number' ? seconds : parseInt(seconds, 10)
  if (!n || n <= 0) return '--:--'
  const m = Math.floor(n / 60)
  const s = n % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
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
      author: item.uploaderName || '未知UP主',
      authorId: item.uploaderId || item.uploader_id || null,
      plays: item.viewCount ? formatCount(item.viewCount) : '0',
      // 后端已统一返回 commentCount（包含回复）
      comments: formatCount(item.commentCount ?? item.comments ?? 0),
      duration: formatDuration(item.duration),
      playUrl: item.playUrl || '',
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

const openUserProfile = (rec) => {
  const authorId = rec?.authorId
  if (!authorId) return
  router.push({ path: `/profile/${encodeURIComponent(String(authorId))}` })
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

.rec-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  height: 80px; // 与左侧封面保持一致
  justify-content: center; // 让三行内容在 80px 内居中
}

.rec-cover-wrap {
  position: relative;
  width: 140px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
}

.rec-cover {
  transition: none;
  position: relative;
  z-index: 1;
}

.rec-time {
  position: absolute;
  z-index: 2;
  right: 6px;
  bottom: 6px;
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.55);
  pointer-events: none;
}

.rec-play-overlay {
  position: absolute;
  z-index: 3;
  top: 6px;
  right: 6px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  color: #ffffff;
  pointer-events: none;
}

.rec-title:hover {
  color: #00aeec !important;
  cursor: pointer;
}

.rec-title {
  font-size: 15px;
  color: #303133;
}

.recommend-title {
  font-size: 15px;
}

.rec-author {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #909399;
  margin-top: 3px; // 在 rec-info gap 基础上，让“标题”和“作者行”之间额外加大 3px
  cursor: pointer;
}

.rec-author:hover {
  color: #00aeec;
}

.rec-author:hover svg {
  color: #00aeec;
}
.rec-author-name {
  line-height: 1;
}

.rec-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: #909399;
}

.rec-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
</style>


