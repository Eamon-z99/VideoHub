<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchHomeHeroConfig, saveHomeHeroConfig } from '@/api/adminHomeHero'
import { fetchAdminVideos } from '@/api/adminVideos'
import { fetchAdminVideosByIds } from '@/api/adminVideosByIds'
import { coverStyle } from '@/utils/cover'

type VideoRow = {
  videoId?: string
  title?: string
  description?: string
  coverUrl?: string
  likeCount?: number
  uploaderName?: string
}

const loading = ref(false)
const saving = ref(false)

const MAX_HERO_SLOTS = 6

const videos = ref<VideoRow[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const keyword = ref('')

const selectedVideos = ref<VideoRow[]>([]) // 保持轮播顺序（slot 顺序）

const selectedSet = computed(() => {
  const set = new Set<string>()
  for (const v of selectedVideos.value) {
    const id = String(v.videoId || '').trim()
    if (id) set.add(id)
  }
  return set
})

function normalizeVideoRow(row: VideoRow): VideoRow {
  const id = String(row.videoId || '').trim()
  return {
    videoId: id,
    title: row.title,
    description: row.description,
    coverUrl: row.coverUrl,
    likeCount: row.likeCount,
    uploaderName: row.uploaderName,
  }
}

async function loadConfig() {
  try {
    const { data } = await fetchHomeHeroConfig()
    const ids = data?.data?.videoIds
    const list = Array.isArray(ids)
      ? ids.filter((x) => typeof x === 'string' && x.trim()).map((x) => x.trim()).slice(0, MAX_HERO_SLOTS)
      : []

    if (list.length === 0) {
      selectedVideos.value = []
      return
    }

    const resp = await fetchAdminVideosByIds(list)
    const loaded = resp?.data?.data?.list
    if (Array.isArray(loaded) && loaded.length > 0) {
      // 保持后端 FIELD(v.video_id, ...) 的顺序
      selectedVideos.value = loaded.map((x) => normalizeVideoRow(x as VideoRow))
    } else {
      selectedVideos.value = list.map((id) => ({ videoId: id }))
    }
  } catch {
    // ignore: 保底仍可手动选择
  }
}

async function loadVideos() {
  loading.value = true
  try {
    const { data } = await fetchAdminVideos({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
    })
    if (!data?.success) {
      ElMessage.error(data?.message || '视频列表加载失败')
      videos.value = []
      total.value = 0
      return
    }
    videos.value = (data.data?.list || []) as VideoRow[]
    total.value = Number(data.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function toggleSelect(row: VideoRow, checked: boolean) {
  const id = String(row.videoId || '').trim()
  if (!id) return

  const exists = selectedSet.value.has(id)
  if (checked) {
    if (!exists) {
      if (selectedVideos.value.length >= MAX_HERO_SLOTS) {
        ElMessage.warning(`最多只能选择 ${MAX_HERO_SLOTS} 个视频作为轮播位`)
        return
      }
      selectedVideos.value = [...selectedVideos.value, normalizeVideoRow(row)]
    }
  } else {
    if (exists) {
      selectedVideos.value = selectedVideos.value.filter((x) => String(x.videoId || '').trim() !== id)
    }
  }
}

function moveSelected(index: number, delta: number) {
  const nextIndex = index + delta
  if (nextIndex < 0 || nextIndex >= selectedVideos.value.length) return
  const arr = [...selectedVideos.value]
  // TS 开启了严格下标检查：arr[index] 可能被推断为 undefined
  const tmp = arr[index]!
  arr[index] = arr[nextIndex]!
  arr[nextIndex] = tmp
  selectedVideos.value = arr
}

async function onSave() {
  saving.value = true
  try {
    const ids = selectedVideos.value
      .map((v) => String(v.videoId || '').trim())
      .filter(Boolean)
      .slice(0, MAX_HERO_SLOTS)
    const resp = await saveHomeHeroConfig(ids)
    if (resp?.data?.success) {
      ElMessage.success('保存成功，首页轮播图将按顺序展示你选中的视频')
    } else {
      ElMessage.error(resp?.data?.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

function clearSelection() {
  selectedVideos.value = []
}

onMounted(() => {
  void (async () => {
    await loadConfig()
    await loadVideos()
  })()
})

async function onSearch() {
  page.value = 1
  await loadVideos()
}

async function onPageChange(p: number) {
  page.value = p
  await loadVideos()
}

async function onPageSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  await loadVideos()
}
</script>

<template>
  <div style="display: flex; flex-direction: column; gap: 14px">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; width: 100%">
          <span>已选轮播（顺序即展示顺序）</span>
          <el-button type="primary" :loading="saving" @click="onSave">
            保存配置
          </el-button>
        </div>
      </template>

      <div v-if="selectedVideos.length === 0" style="color: #9ca3af; font-size: 13px; line-height: 1.6">
        暂未选择。保存后首页将回退为随机展示。
      </div>

      <div v-else class="selected-list">
        <div v-for="(it, idx) in selectedVideos" :key="String(it.videoId || '') + '-' + idx" class="selected-row">
          <div class="selected-index">{{ idx + 1 }}</div>
          <div class="selected-cover" :style="coverStyle(it)" />

          <div class="selected-meta">
            <div class="selected-title-row">
              <span class="selected-title">{{ it.title || '未命名视频' }}</span>
              <span class="selected-uploader">{{ it.uploaderName || '-' }}</span>
            </div>
            <div class="selected-desc">{{ it.description || '' }}</div>
            <div class="selected-stat">
              点赞：{{ it.likeCount ?? 0 }}
            </div>
          </div>

          <div class="selected-actions">
            <el-button :disabled="idx === 0" size="small" @click="moveSelected(idx, -1)">上移</el-button>
            <el-button
              :disabled="idx === selectedVideos.length - 1"
              size="small"
              @click="moveSelected(idx, 1)"
            >
              下移
            </el-button>
            <el-button size="small" type="danger" @click="toggleSelect(it, false)">移除</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; width: 100%">
          <span>首页轮播图配置</span>
          <span style="color: #6b7280; font-size: 12px">勾选指定视频；不勾选则回退随机；最多展示前 6 个</span>
        </div>
      </template>

      <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 10px; flex-wrap: wrap">
        <el-input
          v-model="keyword"
          placeholder="搜索：标题/描述/作者名（可留空）"
          style="width: 360px"
          :disabled="loading || saving"
        />
        <el-button type="primary" :loading="loading" @click="onSearch">
          搜索
        </el-button>
        <el-button :disabled="loading || saving" @click="clearSelection">
          清空已选
        </el-button>
      </div>

      <el-table v-loading="loading" :data="videos" stripe empty-text="暂无视频" row-key="videoId">
        <el-table-column label="选中" width="60" align="center">
          <template #default="{ row }">
            <el-checkbox
              :model-value="selectedSet.has(row.videoId as string)"
              @change="(v: boolean) => toggleSelect(row, v)"
            />
          </template>
        </el-table-column>

        <el-table-column label="封面" width="180">
          <template #default="{ row }">
            <div class="cover" :style="coverStyle(row)" />
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />

        <el-table-column label="作者" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.uploaderName || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="点赞" width="100" align="right">
          <template #default="{ row }">
            <span>{{ row.likeCount ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="轮播顺序" width="120" align="right">
          <template #default="{ row }">
            <span>
              {{
                selectedVideos.findIndex((x) => x.videoId === row.videoId) + 1 || '-'
              }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 12px">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :page-sizes="[6, 12, 24]"
          :current-page="page"
          @current-change="onPageChange"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.cover {
  width: 154px;
  height: 87px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.selected-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selected-row {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  align-items: center;
  background: #fff;
}

.selected-index {
  width: 34px;
  color: #00a1d6;
  font-weight: 800;
  text-align: center;
  flex-shrink: 0;
}

.selected-cover {
  width: 154px;
  height: 87px;
  border-radius: 8px;
  background: #f3f4f6;
  background-size: cover;
  background-position: center;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.selected-meta {
  flex: 1;
  min-width: 0;
}

.selected-title-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.selected-title {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 520px;
}

.selected-uploader {
  color: #9ca3af;
  font-size: 12px;
}

.selected-desc {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

.selected-stat {
  margin-top: 6px;
  font-size: 12px;
  color: #9ca3af;
}

.selected-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex-shrink: 0;
}
</style>

