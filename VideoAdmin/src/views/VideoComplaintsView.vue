<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchPendingVideoComplaints, processVideoComplaint } from '@/api/adminComplaints'
import { fetchAdminVideosByIds } from '@/api/adminVideosByIds'
import { normalizeCover, normalizeMediaUrl, rawPlayUrl } from '@/utils/cover'

type ActionType = 'warn' | 'takedown' | 'dismiss'

type Complaint = Record<string, any>

const topLoading = ref(false)
const list = ref<Complaint[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const coverMap = ref<Record<string, string>>({})
const detailMode = ref(false)
const detailComplaint = ref<Complaint | null>(null)
const detailVideo = ref<Record<string, unknown> | null>(null)
const detailPlayUrl = ref('')

const actionLabel = (action: ActionType) => {
  if (action === 'warn') return '警示'
  if (action === 'takedown') return '下架'
  return '不予受理'
}

const categoryLabel = (category: string | undefined) => {
  const c = String(category || '')
  const map: Record<string, string> = {
    low_quality: '色情低俗',
    illegal_ads: '违法广告引流',
    politics_sensitive: '涉政敏感',
    attack_harassment: '引战、网暴、不友善',
    obscene_language: '传播放淫言',
    fraud: '涉嫌诈骗',
    uncomfortable: '引人不适',
    minors_bad_info: '涉未成年人不良信息',
    clickbait: '封面党、标题党',
    ip_violation: '侵犯个人知识产权',
    personal_right_violation: '侵犯个人人身权益',
    enterprise_right_violation: '侵犯企业权益',
    other: '其他',
  }
  return map[c] || c || '-'
}

function parseEvidenceUrls(evidenceUrls: unknown): string[] {
  if (!evidenceUrls) return []
  if (Array.isArray(evidenceUrls)) return evidenceUrls.filter(Boolean).map(String)
  if (typeof evidenceUrls === 'string') {
    const t = evidenceUrls.trim()
    if (!t) return []
    try {
      const out = JSON.parse(t)
      if (Array.isArray(out)) return out.filter(Boolean).map(String)
    } catch {
      // 非 JSON：兜底当单个 url
    }
    return [t]
  }
  return []
}

const fetchList = async () => {
  topLoading.value = true
  try {
    const { data } = await fetchPendingVideoComplaints({ page: page.value, pageSize: pageSize.value })
    if (!data?.success) {
      ElMessage.error(data?.message || '加载失败')
      list.value = []
      total.value = 0
      return
    }
    const d = data.data
    list.value = d?.list || []
    total.value = Number(d?.total) || 0
    await hydrateCoverMap()
  } finally {
    topLoading.value = false
  }
}

const hydrateCoverMap = async () => {
  const ids = list.value
    .map((row) => getVideoId(row))
    .filter((x): x is string => Boolean(x))

  if (ids.length === 0) {
    coverMap.value = {}
    return
  }

  try {
    const { data } = await fetchAdminVideosByIds(ids)
    const arr = data?.data?.list
    if (!Array.isArray(arr)) return
    const next: Record<string, string> = {}
    for (const it of arr) {
      const id = it?.videoId != null ? String(it.videoId) : ''
      const coverRaw = it?.coverUrl != null ? String(it.coverUrl) : ''
      if (id && coverRaw) next[id] = normalizeCover(coverRaw)
    }
    coverMap.value = next
  } catch {
    // 忽略：保底走原字段
  }
}

const processDialogVisible = ref(false)
const processSubmitting = ref(false)
const processAction = ref<ActionType>('warn')
const processHandlerNote = ref('')
const selectedComplaint = ref<Complaint | null>(null)

const openProcess = (row: Complaint, action: ActionType) => {
  selectedComplaint.value = row
  processAction.value = action
  processHandlerNote.value = ''
  processDialogVisible.value = true
}

const confirmProcess = async () => {
  if (!selectedComplaint.value?.id) return
  processSubmitting.value = true
  try {
    const action = processAction.value
    const handlerNote = processHandlerNote.value.trim() || null
    const resp = await processVideoComplaint(selectedComplaint.value.id, { action, handlerNote })
    if (resp?.data?.success) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      await fetchList()
    } else {
      ElMessage.error(resp?.data?.message || '处理失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '处理失败')
  } finally {
    processSubmitting.value = false
  }
}

onMounted(() => {
  void fetchList()
})

const canShowPager = computed(() => total.value > pageSize.value)

function getVideoId(row: Complaint): string | null {
  const v = row.videoId ?? row.video_id
  if (!v) return null
  return typeof v === 'string' ? v : String(v)
}

function openVideoDetail(row: Complaint) {
  detailComplaint.value = row
  detailVideo.value = null
  detailPlayUrl.value = normalizeMediaUrl(rawPlayUrl(row))
  detailMode.value = true
  const id = getVideoId(row)
  if (!id) return
  const fromMap = coverMap.value[id]
  if (fromMap) {
    detailVideo.value = { videoId: id, coverUrl: fromMap }
  }
  void loadVideoDetailById(id)
}

async function loadVideoDetailById(id: string) {
  try {
    const { data } = await fetchAdminVideosByIds([id])
    const arr = data?.data?.list
    if (Array.isArray(arr) && arr.length > 0) {
      detailVideo.value = (arr[0] || {}) as Record<string, unknown>
      if (!detailPlayUrl.value) {
        detailPlayUrl.value = normalizeMediaUrl(rawPlayUrl(detailVideo.value))
      }
    }
  } catch {
    // ignore
  }
}

function backToList() {
  detailMode.value = false
  detailComplaint.value = null
  detailVideo.value = null
  detailPlayUrl.value = ''
}

function getCoverUrl(row: Complaint): string | null {
  const id = getVideoId(row)
  if (id && coverMap.value[id]) return coverMap.value[id]

  const v =
    row.coverUrl ??
    row.cover_url ??
    row.videoCoverUrl ??
    row.video_cover_url ??
    row.video_cover ??
    row.videoCover ??
    row.video?.coverUrl ??
    row.video?.cover_url
  if (!v) return null
  return normalizeCover(String(v))
}
</script>

<template>
  <el-card v-if="!detailMode" shadow="never">
    <template #header>
      <div class="header-row">
        <span>待处理稿件举报</span>
        <el-button type="primary" link style="float: right" :loading="topLoading" @click="fetchList">
          刷新
        </el-button>
      </div>
    </template>

    <el-table v-loading="topLoading" :data="list" stripe empty-text="暂无待处理举报">
      <el-table-column label="封面" width="120">
        <template #default="{ row }">
          <el-image
            v-if="getCoverUrl(row)"
            :src="getCoverUrl(row)!"
            fit="cover"
            class="video-cover"
          />
          <span v-else style="color: #9ca3af; font-size: 12px">无封面</span>
        </template>
      </el-table-column>

      <el-table-column label="作品" min-width="200">
        <template #default="{ row }">
          <div class="cell-title">
            {{ row.videoTitle || row.video_title || '-' }}
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="reporterUsername" label="举报人" min-width="120" show-overflow-tooltip />

      <el-table-column label="类型" min-width="140">
        <template #default="{ row }">
          {{ categoryLabel(row.category) }}
        </template>
      </el-table-column>

      <el-table-column label="详情" min-width="220">
        <template #default="{ row }">
          <div class="detail-cell">{{ row.otherDetail || row.other_detail || '-' }}</div>
        </template>
      </el-table-column>

      <el-table-column label="图片证据" min-width="220">
        <template #default="{ row }">
          <div class="evidence-row">
            <el-image
              v-for="(u, idx) in parseEvidenceUrls(row.evidenceUrls || row.evidence_urls)"
              :key="u + idx"
              :src="u"
              fit="cover"
              class="evidence-thumb"
              :preview-src-list="parseEvidenceUrls(row.evidenceUrls || row.evidence_urls)"
            />
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <div class="action-row">
            <el-button
              size="small"
              type="primary"
              link
              @click="openVideoDetail(row)"
            >
              详情
            </el-button>
            <el-button size="small" type="primary" plain @click="openProcess(row, 'warn')">警示</el-button>
            <el-button size="small" type="danger" plain @click="openProcess(row, 'takedown')">下架</el-button>
            <el-button size="small" plain @click="openProcess(row, 'dismiss')">不予受理</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" v-if="canShowPager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>
  </el-card>

  <el-card v-else shadow="never">
    <template #header>
      <div class="detail-header-row">
        <span>举报稿件详情</span>
        <el-button type="primary" link @click="backToList">返回列表</el-button>
      </div>
    </template>

    <div v-if="detailComplaint" class="detail-layout">
      <div class="detail-player-wrap">
        <div class="section-title">视频预览</div>
        <div v-if="detailPlayUrl" class="player-shell">
          <video class="player-video" :src="detailPlayUrl" controls />
        </div>
        <el-empty v-else description="暂无视频地址" />
      </div>

      <el-form label-width="84px" class="detail-form">
        <el-form-item label="封面">
          <div class="cover-row">
            <div
              class="detail-cover"
              :style="{
                backgroundImage: getCoverUrl(detailComplaint)
                  ? `url('${getCoverUrl(detailComplaint)}')`
                  : 'none',
              }"
            />
          </div>
        </el-form-item>
        <el-form-item label="标题">
          <div class="readonly-value">{{ detailComplaint.videoTitle || detailComplaint.video_title || '-' }}</div>
        </el-form-item>
        <el-form-item label="类型">
          <div class="readonly-value">{{ categoryLabel(detailComplaint.category) }}</div>
        </el-form-item>
        <el-form-item label="举报人">
          <div class="readonly-value">{{ detailComplaint.reporterUsername || '-' }}</div>
        </el-form-item>
        <el-form-item label="举报详情">
          <div class="readonly-value multiline">{{ detailComplaint.otherDetail || detailComplaint.other_detail || '-' }}</div>
        </el-form-item>
      </el-form>
    </div>
    <el-empty v-else description="暂无详情数据" />
  </el-card>

  <el-dialog
    v-model="processDialogVisible"
    width="820px"
    title="处理举报"
    :show-close="false"
    destroy-on-close
    @closed="selectedComplaint = null"
  >
    <div class="process-meta" v-if="selectedComplaint">
      <div><b>作品：</b>{{ selectedComplaint.videoTitle || '-' }}</div>
      <div><b>举报人：</b>{{ selectedComplaint.reporterUsername || '-' }}</div>
      <div><b>类型：</b>{{ categoryLabel(selectedComplaint.category) }}</div>
      <div><b>详情：</b>{{ selectedComplaint.otherDetail || '-' }}</div>
    </div>

    <div class="process-evidence" v-if="parseEvidenceUrls(selectedComplaint?.evidenceUrls || []).length">
      <div class="evidence-title"><b>图片证据</b></div>
      <div class="evidence-row">
        <el-image
          v-for="(u, idx) in parseEvidenceUrls(selectedComplaint?.evidenceUrls || [])"
          :key="u + idx"
          :src="u"
          fit="cover"
          class="evidence-thumb"
          :preview-src-list="parseEvidenceUrls(selectedComplaint?.evidenceUrls || [])"
        />
      </div>
    </div>

    <div class="process-note">
      <div class="evidence-title"><b>备注（可选）</b></div>
      <el-input
        v-model="processHandlerNote"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="输入管理员处理备注"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="processSubmitting" @click="confirmProcess">
          确认{{ actionLabel(processAction) }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cell-title {
  font-weight: 600;
}

.video-cover {
  width: 104px;
  height: 58px;
  border-radius: 6px;
  background: #f3f4f6;
}

.detail-cell {
  color: #4b5563;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 72px;
  overflow: hidden;
  line-height: 1.5;
  font-size: 12px;
}

.evidence-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.evidence-thumb {
  width: 84px;
  height: 62px;
  border-radius: 8px;
  background: #f3f4f6;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.process-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  color: #374151;
}

.process-evidence,
.process-note {
  margin-top: 12px;
}

.evidence-title {
  font-size: 13px;
  margin-bottom: 8px;
  color: #111827;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-player-wrap {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section-title {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.player-shell {
  position: relative;
  width: 100%;
  max-width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
  overflow: hidden;
  border-radius: 8px;
}

.player-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.detail-form {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 14px 14px 4px;
  background: #fff;
}

.detail-cover {
  width: 154px;
  height: 87px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-color: #ebeef5;
  border: 1px solid #e5e7eb;
}

.readonly-value {
  min-height: 32px;
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
  color: #374151;
  padding: 6px 10px;
  display: flex;
  align-items: center;
  line-height: 1.5;
  box-sizing: border-box;
}

.readonly-value.multiline {
  white-space: pre-wrap;
  word-break: break-word;
  align-items: flex-start;
  min-height: 72px;
}
</style>

