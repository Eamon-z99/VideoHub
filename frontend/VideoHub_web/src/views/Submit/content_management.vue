<template>
  <div class="cm-page">
    <div class="cm-top-tabs">
      <div class="tab is-active">视频管理</div>
      <!-- <div class="tab">图文管理</div>
      <div class="tab">互动视频管理</div>
      <div class="tab">音频管理</div>
      <div class="tab">贴纸管理</div>
      <div class="tab">视频素材管理</div> -->
      <div class="tab cm-spacer"></div>
      <div class="cm-search">
        <el-input v-model="keyword" placeholder="搜索稿件标题" clearable style="width: 260px" />
      </div>
    </div>

    <div class="cm-sub-tabs">
      <div class="sub-tab" :class="{ active: status === 'all' }" @click="status = 'all'">
        全部稿件 <span class="num">{{ stats.all }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'draft' }" @click="status = 'draft'">
        草稿 <span class="num">{{ stats.draft }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'reviewing' }" @click="status = 'reviewing'">
        进行中 <span class="num">{{ stats.reviewing }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'approved' }" @click="status = 'approved'">
        已通过 <span class="num">{{ stats.approved }}</span>
      </div>
      <div class="sub-tab" :class="{ active: status === 'rejected' }" @click="status = 'rejected'">
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
        <div v-if="displayList.length === 0" class="cm-empty">
          <el-empty description="一个稿件都没有，请换个筛选条件" />
        </div>

        <div v-else class="draft-list">
          <div class="draft-row" v-for="it in displayList" :key="it.submission_id">
            <div class="cover" :style="{ backgroundImage: it.cover_url ? `url(${normalizeCover(it.cover_url)})` : '' }"></div>
            <div class="meta">
              <div class="title">{{ it.title }}</div>
              <div class="desc">{{ it.description || '-' }}</div>
              <div class="time">更新：{{ formatDate(it.update_time) }}</div>
            </div>
            <div class="actions">
              <el-button size="small" @click="continueEdit(it)">继续编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeDraft(it)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteVideoDraft, listVideoDrafts } from '@/api/video'

const emit = defineEmits(['continue-edit'])

const keyword = ref('')
const status = ref('draft')
const order = ref('time')
const loading = ref(false)

const page = ref(1)
const pageSize = 20
const total = ref(0)
const list = ref([])

const stats = ref({
  all: 0,
  draft: 0,
  reviewing: 0,
  approved: 0,
  rejected: 0,
})

const totalPages = computed(() => Math.ceil((total.value || 0) / pageSize))

const displayList = computed(() => {
  // 目前先实现草稿箱（draft），其它状态后续可接 video_submissions/videos
  if (status.value !== 'draft') return []
  return list.value
})

const formatDate = (v) => {
  if (!v) return '-'
  try {
    return new Date(v).toLocaleString('zh-CN')
  } catch (e) {
    return String(v)
  }
}

const normalizeCover = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // 后端视频/封面文件通过 /local-videos/** 提供访问
  if (url.startsWith('/local-videos/')) return url
  if (url.startsWith('/')) {
    // 兼容旧数据：如果是 /uploads/... 这种路径，仍然走 /local-videos
    if (url.startsWith('/uploads/') || url.startsWith('/uploads\\')) {
      return '/local-videos' + url
    }
    return url
  }
  // 常见相对路径：uploads/submissions/covers/...
  if (url.startsWith('uploads/') || url.startsWith('uploads\\')) {
    return '/local-videos/' + url.replaceAll('\\', '/')
  }
  return '/local-videos/' + url.replaceAll('\\', '/')
}

const fetchDrafts = async () => {
  loading.value = true
  try {
    const { data } = await listVideoDrafts({ page: page.value, pageSize, keyword: keyword.value || undefined })
    if (data?.success) {
      const payload = data.data || {}
      list.value = payload.list || []
      total.value = payload.total || 0
      stats.value.draft = total.value
      stats.value.all = stats.value.draft
    } else {
      ElMessage.error(data?.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

watch([page, keyword, status], () => {
  page.value = Math.max(1, page.value)
  if (status.value === 'draft') fetchDrafts()
})

onMounted(() => {
  fetchDrafts()
})

const continueEdit = (it) => {
  // 通知父组件在当前页面切换到投稿编辑页
  emit('continue-edit', it.submission_id)
}

const removeDraft = async (it) => {
  try {
    await ElMessageBox.confirm('确认删除该草稿？', '删除草稿', { type: 'warning' })
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const { data } = await deleteVideoDraft(it.submission_id)
    if (data?.success) {
      ElMessage.success('已删除')
      fetchDrafts()
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
  border-bottom: none;
  height: 64px;
  padding: 0 35px;
  margin: 0;
}

.tab {
  font-size: 16px;
  color: #374151;
  padding: 0 18px;
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
  padding: 16px;
  min-height: 420px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
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
}
.cover {
  width: 120px;
  height: 68px;
  border-radius: 8px;
  background: #f3f4f6;
  background-size: cover;
  background-position: center;
  border: 1px solid #e5e7eb;
}
.meta {
  flex: 1;
  min-width: 0;
}
.title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.desc {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2; // 兼容性：提供标准属性给支持 line-clamp 的浏览器
  overflow: hidden;
}
.time {
  margin-top: 6px;
  color: #9ca3af;
  font-size: 12px;
}
.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cm-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: center;
}
</style>
