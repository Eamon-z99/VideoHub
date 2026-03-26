<template>
  <div class="cm-page">
    <div class="cm-top-tabs">
      <div class="tab is-active">视频管理</div>
      <div class="tab cm-spacer"></div>
      <div class="cm-search">
        <el-input v-model="keyword" placeholder="搜索稿件标题" clearable style="width: 260px" />
      </div>
    </div>

    <div class="cm-sub-tabs">
      <div class="sub-tab" :class="{ active: status === 'all' }" @click="selectTab('all')">
        全部稿件 <span class="num">{{ stats.all }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'draft' }" @click="selectTab('draft')">
        草稿 <span class="num">{{ stats.draft }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'reviewing' }" @click="selectTab('reviewing')">
        进行中 <span class="num">{{ stats.reviewing }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'approved' }" @click="selectTab('approved')">
        已通过 <span class="num">{{ stats.approved }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'hidden' }" @click="selectTab('hidden')">
        已隐藏 <span class="num">{{ stats.hidden }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'takedown' }" @click="selectTab('takedown')">
        已下架 <span class="num">{{ stats.takedown }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'rejected' }" @click="selectTab('rejected')">
        未通过 <span class="num">{{ stats.rejected }}</span>
      </div>
      <div class="cm-sub-spacer"></div>
      <el-select v-model="order" size="small" style="width: 140px">
        <el-option label="按投稿时间排序" value="time" />
      </el-select>
    </div>

    <div class="cm-body">
      <div v-if="loading" class="cm-loading">
        <el-skeleton :rows="5" animated />
      </div>

      <template v-else>
        <div v-if="list.length === 0" class="cm-empty">
          <el-empty description="一个稿件都没有，请换个筛选条件" />
        </div>

        <div v-else class="draft-list">
          <div class="draft-row" v-for="it in list" :key="rowKey(it)">
            <div
              class="cover"
              :style="coverStyle(it)"
            ></div>
            <div class="meta">
              <div class="title-row">
                <span class="title">{{ it.title || '未命名稿件' }}</span>
                <el-tag :type="statusTagType(it)" size="small" class="status-tag">{{ statusLabel(it) }}</el-tag>
              </div>
              <div class="desc">{{ it.description || '-' }}</div>
              <div v-if="isTakedownRow(it)" class="takedown-reason">
                <span class="reason-title">下架原因：</span>
                <span class="reason-text">
                  {{ buildTakedownReason(it) }}
                </span>
              </div>
              <div class="time-row">
                <span class="time">更新：{{ formatDate(it.update_time) }}</span>
                <span v-if="isPublishedRow(it)" class="mini-stat">
                  {{ formatNum(it.view_count) }} 播放 · {{ formatNum(it.like_count) }} 点赞
                </span>
              </div>
            </div>
            <div class="actions">
              <el-button v-if="isPublishedRow(it)" size="small" type="primary" plain @click="openVideo(it.video_id)">
                查看视频
              </el-button>
              <el-button
                v-if="isHiddenRow(it)"
                size="small"
                type="primary"
                plain
                @click="unhidePublishedVideo(it)"
              >
                取消隐藏
              </el-button>
              <el-button
                v-else-if="isPublishedRow(it) && !isTakedownRow(it)"
                size="small"
                type="danger"
                plain
                @click="hidePublishedVideo(it)"
              >
                隐藏
              </el-button>
              <template v-if="isDraftRow(it)">
                <el-button size="small" @click="continueEdit(it)">继续编辑</el-button>
                <el-button size="small" type="danger" plain @click="removeDraft(it)">删除</el-button>
              </template>
            </div>
          </div>
        </div>

        <div class="cm-pagination" v-if="totalPages > 1">
          <el-pagination
            background
            layout="prev, pager, next"
            :page-size="pageSize"
            :total="total"
            v-model:current-page="page"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteVideoDraft,
  listCreatorWorks,
  hideCreatorWorkVideo,
  unhideCreatorWorkVideo
} from '@/api/video'

const emit = defineEmits(['continue-edit'])
const router = useRouter()

const keyword = ref('')
/** 默认「全部稿件」，展示已发布 + 草稿 + 审核流 */
const status = ref('all')
const order = ref('time')
const loading = ref(false)

const page = ref(1)
const pageSize = 20
const total = ref(0)
const list = ref([])

const getScrollEl = () => {
  // 该项目滚动使用 App 根级 .scroll-container
  return document.querySelector('.scroll-container') || null
}

const getScrollTop = () => {
  const sc = getScrollEl()
  if (sc) return sc.scrollTop || 0
  return window.scrollY || document.documentElement.scrollTop || document.body.scrollTop || 0
}

const restoreScrollTop = (y) => {
  requestAnimationFrame(() => {
    const sc = getScrollEl()
    if (sc) {
      sc.scrollTo({ top: y, behavior: 'auto' })
      return
    }
    window.scrollTo(0, y)
  })
}

const stats = ref({
  all: 0,
  draft: 0,
  reviewing: 0,
  approved: 0,
  rejected: 0,
  takedown: 0,
  hidden: 0
})

const totalPages = computed(() => Math.ceil((total.value || 0) / pageSize))

const rowKey = (it) => {
  const vid = it.video_id || it.videoId || ''
  const sid = it.submission_id || it.submissionId || ''
  const k = it.kind || it.KIND || ''
  const t = String(it.title || '')
  const u = String(it.update_time || it.sort_key || '')
  return `${k}|${vid}|${sid}|${t}|${u}`
}

const isPublishedRow = (it) => (it.kind || it.KIND) === 'published' && (it.video_id || it.videoId)
const isDraftRow = (it) => (it.kind || it.KIND) === 'draft'
const safeStr = (v) => (v === null || v === undefined ? '' : String(v))
const getTakedownTime = (it) => safeStr(it.takedown_time || it.takedownTime).trim()
const getTakedownNote = (it) => safeStr(it.takedown_note || it.takedownNote).trim()
const hasTakedownReason = (it) => Boolean(getTakedownTime(it) || getTakedownNote(it))

const isTakedownRow = (it) => {
  if (!isPublishedRow(it)) return false
  const st = String(it.video_status || it.videoStatus || '').toUpperCase()
  return (st === 'FAILED' || st === 'DOWN') && hasTakedownReason(it)
}

const isHiddenRow = (it) => {
  if (!isPublishedRow(it)) return false
  const st = String(it.video_status || it.videoStatus || '').toUpperCase()
  // 隐藏：DOWN 但没有“投诉/下架处理”的原因记录
  return st === 'DOWN' && !hasTakedownReason(it)
}

const statusLabel = (it) => {
  if (isHiddenRow(it)) return '已隐藏'
  if (isTakedownRow(it)) return '已下架'
  if (isPublishedRow(it)) return '已发布'
  if (isDraftRow(it)) return '草稿'
  const rs = String(it.review_status || it.reviewStatus || '').toUpperCase()
  if (rs === 'PENDING') return '审核中'
  if (rs === 'APPROVED') return '待发布'
  if (rs === 'REJECTED') return '未通过'
  return '投稿'
}

const statusTagType = (it) => {
  if (isHiddenRow(it)) return 'warning'
  if (isTakedownRow(it)) return 'danger'
  if (isPublishedRow(it)) return 'success'
  if (isDraftRow(it)) return 'info'
  const rs = String(it.review_status || it.reviewStatus || '').toUpperCase()
  if (rs === 'PENDING') return 'warning'
  if (rs === 'APPROVED') return 'primary'
  if (rs === 'REJECTED') return 'danger'
  return ''
}

const buildTakedownReason = (it) => {
  const note = String(it.takedown_note || it.takedownNote || '').trim()
  // 需求：下架原因取 handler_note（即 takedown_note）
  return note || '暂无（可能是手动标记/历史数据）'
}

const formatNum = (v) => {
  const n = Number(v)
  if (!Number.isFinite(n)) return '0'
  if (n >= 10000) return `${(n / 10000).toFixed(1).replace(/\.0$/, '')}万`
  return String(n)
}

const formatDate = (v) => {
  if (!v) return '-'
  try {
    return new Date(v).toLocaleString('zh-CN')
  } catch (e) {
    return String(v)
  }
}

const rawCover = (it) => (it && (it.cover_url ?? it.coverUrl)) || ''

const normalizeCover = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/local-videos/')) return url
  if (url.startsWith('/')) {
    if (url.startsWith('/uploads/') || url.startsWith('/uploads\\')) {
      return '/local-videos' + url
    }
    return url
  }
  if (url.startsWith('uploads/') || url.startsWith('uploads\\')) {
    return '/local-videos/' + url.replaceAll('\\', '/')
  }
  return '/local-videos/' + url.replaceAll('\\', '/')
}

/** 封面 URL 可能含括号、空格等，需加引号；后端已返回 /local-videos/... */
const coverStyle = (it) => {
  const u = normalizeCover(rawCover(it))
  if (!u) return {}
  return { backgroundImage: `url("${u.replace(/"/g, '\\"')}")` }
}

const fetchList = async () => {
  loading.value = true
  try {
    const { data } = await listCreatorWorks({
      page: page.value,
      pageSize,
      keyword: keyword.value || undefined,
      tab: status.value
    })
    if (data?.success) {
      const payload = data.data || {}
      list.value = payload.list || []
      total.value = payload.total || 0
      if (payload.stats && typeof payload.stats === 'object') {
        stats.value = {
          all: Number(payload.stats.all) || 0,
          draft: Number(payload.stats.draft) || 0,
          reviewing: Number(payload.stats.reviewing) || 0,
          approved: Number(payload.stats.approved) || 0,
          rejected: Number(payload.stats.rejected) || 0,
              takedown: Number(payload.stats.takedown) || 0,
              hidden: Number(payload.stats.hidden) || 0
        }
      }
    } else {
      ElMessage.error(data?.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const selectTab = (tab) => {
  if (status.value === tab) return
  status.value = tab
  page.value = 1
}

watch([page, keyword, status], () => {
  page.value = Math.max(1, page.value)
  fetchList()
})

onMounted(() => {
  fetchList()
})

const openVideo = (videoId) => {
  const id = videoId || ''
  if (!id) return
  router.push({ path: `/video/${encodeURIComponent(id)}` })
}

const hidePublishedVideo = async (it) => {
  const vid = it.video_id || it.videoId
  if (!vid) return
  try {
    await ElMessageBox.confirm('确认隐藏该已发布视频？隐藏后将不可在公开列表中看到。', '隐藏视频', {
      type: 'warning'
    })
  } catch (e) {
    return
  }

  const scrollY = getScrollTop()
  loading.value = true
  try {
    const { data } = await hideCreatorWorkVideo(vid)
    if (data?.success) {
      ElMessage.success('已隐藏')
      // 不刷新整页：本地更新列表/计数
      // approved tab 下隐藏后该条应从列表消失；all tab 则切换为“已隐藏”
      if (status.value === 'approved') {
        list.value = list.value.filter((x) => (x.video_id || x.videoId) !== vid)
      } else {
        const it2 = list.value.find((x) => (x.video_id || x.videoId) === vid)
        if (it2) {
          it2.video_status = 'DOWN'
          it2.videoStatus = 'DOWN'
          it2.takedown_category = null
          it2.takedownCategory = null
          it2.takedown_note = null
          it2.takedownNote = null
          it2.takedown_time = null
          it2.takedownTime = null
        }
      }

      stats.value.approved = Math.max(0, Number(stats.value.approved) - 1)
      stats.value.hidden = Number(stats.value.hidden) + 1
    } else {
      ElMessage.error(data?.message || '隐藏失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '隐藏失败')
  } finally {
    loading.value = false
    restoreScrollTop(scrollY)
  }
}

const unhidePublishedVideo = async (it) => {
  const vid = it.video_id || it.videoId
  if (!vid) return
  try {
    await ElMessageBox.confirm('确认取消隐藏？取消后将恢复可见。', '取消隐藏', {
      type: 'info'
    })
  } catch (e) {
    return
  }

  const scrollY = getScrollTop()
  loading.value = true
  try {
    const { data } = await unhideCreatorWorkVideo(vid)
    if (data?.success) {
      ElMessage.success('已取消隐藏')
      // 不刷新整页：本地更新列表/计数
      if (status.value === 'hidden') {
        list.value = list.value.filter((x) => (x.video_id || x.videoId) !== vid)
      } else {
        const it2 = list.value.find((x) => (x.video_id || x.videoId) === vid)
        if (it2) {
          it2.video_status = 'DONE'
          it2.videoStatus = 'DONE'
          it2.takedown_category = null
          it2.takedownCategory = null
          it2.takedown_note = null
          it2.takedownNote = null
          it2.takedown_time = null
          it2.takedownTime = null
        }
      }

      stats.value.hidden = Math.max(0, Number(stats.value.hidden) - 1)
      stats.value.approved = Number(stats.value.approved) + 1
    } else {
      ElMessage.error(data?.message || '取消隐藏失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '取消隐藏失败')
  } finally {
    loading.value = false
    restoreScrollTop(scrollY)
  }
}

const continueEdit = (it) => {
  const sid = it.submission_id || it.submissionId
  if (!sid) {
    ElMessage.info('该条目无法从草稿编辑页打开')
    return
  }
  emit('continue-edit', sid)
}

const removeDraft = async (it) => {
  const sid = it.submission_id || it.submissionId
  if (!sid) return
  try {
    await ElMessageBox.confirm('确认删除该草稿？', '删除草稿', { type: 'warning' })
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const { data } = await deleteVideoDraft(sid)
    if (data?.success) {
      ElMessage.success('已删除')
      await fetchList()
    } else {
      ElMessage.error(data?.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.cm-page {
  background: transparent;
  padding: 0;
}

.cm-top-tabs {
  display: flex;
  align-items: center;
  gap: 18px;
  height: 64px;
  padding: 0 35px;
  margin: 0;
  border-bottom: 1.5px solid #eef2f7;
}

.tab {
  font-size: 16px;
  color: #374151;
  height: 64px;
  line-height: 64px;
  cursor: pointer;
  border-bottom: 3px solid transparent;
}
.tab.is-active {
  color: #00a1d6;
  border-bottom-color: #00a1d6;
  font-weight: 700;
}
.cm-spacer {
  flex: 1;
}

.cm-sub-tabs {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 35px;
  flex-wrap: wrap;
}
.sub-tab {
  color: #374151;
  font-size: 13px;
  cursor: pointer;
  padding: 6px 0;
  border-bottom: 2px solid transparent;
}
.sub-tab.active {
  color: #00a1d6;
  border-bottom-color: #00a1d6;
  font-weight: 600;
}
.num {
  margin-left: 6px;
  color: #9ca3af;
  font-weight: 500;
}
.cm-sub-spacer {
  flex: 1;
}

.cm-body {
  margin-top: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 16px 35px;
  min-height: 420px;
}

.cm-empty {
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #6b7280;
}
.cm-empty :deep(.el-empty__image) {
  width: 220px;
}

.draft-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.draft-row {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  align-items: center;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}
.draft-row:hover {
  border-color: #dbeafe;
  box-shadow: 0 4px 14px rgba(0, 161, 214, 0.08);
}
.cover {
  width: 154px;
  height: 87px;
  border-radius: 8px;
  background: #f3f4f6;
  background-size: cover;
  background-position: center;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}
.meta {
  flex: 1;
  min-width: 0;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.status-tag {
  flex-shrink: 0;
}
.desc {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

.takedown-reason {
  margin-top: 6px;
  font-size: 12px;
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  padding: 8px 10px;
  line-height: 1.4;
}

.reason-title {
  font-weight: 700;
}
.time-row {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  font-size: 12px;
  color: #9ca3af;
}
.mini-stat {
  color: #6b7280;
}
.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.cm-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: center;
}
</style>
