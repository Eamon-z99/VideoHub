<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchPendingSubmissions,
  approveSubmission,
  rejectSubmission,
  fetchSubmissionPlayUrl,
} from '@/api/adminSubmission'
import { coverStyle, normalizeMediaUrl, rawPlayUrl } from '@/utils/cover'

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const detailMode = ref(false)
const currentPlayUrl = ref('')
const currentTitle = ref('')
const currentRow = ref<Record<string, unknown> | null>(null)

function formatDate(v: unknown): string {
  if (v == null || v === '') return '-'
  try {
    return new Date(String(v)).toLocaleString('zh-CN')
  } catch {
    return String(v)
  }
}

function formatDuration(sec: unknown): string {
  if (sec == null || sec === '') return '-'
  const n = Number(sec)
  if (!Number.isFinite(n) || n <= 0) return '-'
  const m = Math.floor(n / 60)
  const s = n % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

function getSubmissionId(row: Record<string, unknown>): string | undefined {
  const v = row.submission_id ?? row.submissionId
  if (typeof v === 'string') return v
  if (v != null) return String(v)
  return undefined
}

function uploaderName(row: Record<string, unknown>): string {
  const v = row.uploader_name ?? row.uploaderName
  return typeof v === 'string' ? v : '-'
}

function descriptionText(row: Record<string, unknown>): string {
  const v = row.description ?? row.desc
  return typeof v === 'string' && v.trim() ? v.trim() : '-'
}

function copyrightText(row: Record<string, unknown>): string {
  const v = row.copyright
  if (!v) return '-'
  const s = String(v)
  if (s === 'original') return '原创'
  if (s === 'reprint') return '转载'
  return s
}

function tagsText(row: Record<string, unknown>): string {
  const raw = row.tags
  if (!raw) return '-'
  if (Array.isArray(raw)) return raw.join('、')
  const s = String(raw)
  try {
    const arr = JSON.parse(s)
    if (Array.isArray(arr)) return arr.join('、')
  } catch {
    // ignore
  }
  return s
}

function boolFlag(row: Record<string, unknown>, ...keys: string[]): string {
  for (const k of keys) {
    const v = (row as any)[k]
    if (typeof v === 'boolean') return v ? '是' : '否'
    if (v != null && v !== '') {
      const n = Number(v)
      if (Number.isFinite(n)) return n === 1 ? '是' : '否'
    }
  }
  return '否'
}

async function openDetail(row: Record<string, unknown>) {
  const raw = rawPlayUrl(row)
  let url = normalizeMediaUrl(raw)
  if (!url) {
    const sid = getSubmissionId(row)
    if (sid) {
      try {
        const { data } = await fetchSubmissionPlayUrl(sid)
        if (data.success) {
          const d = data.data || {}
          url = normalizeMediaUrl((d.playUrl as string) || (d.sourceFile as string) || (d.storagePath as string) || '')
        }
      } catch {
        // 拦截器会提示
      }
    }
  }
  if (!url) {
    ElMessage.warning('该投稿暂无可播放视频地址')
  } else {
    currentPlayUrl.value = url
  }
  currentTitle.value = typeof row.title === 'string' ? row.title : '视频预览'
  currentRow.value = row
  detailMode.value = true
}

function backToList() {
  detailMode.value = false
  currentPlayUrl.value = ''
  currentRow.value = null
}

async function load() {
  loading.value = true
  try {
    const { data } = await fetchPendingSubmissions({
      page: page.value,
      pageSize: pageSize.value,
    })
    if (!data.success) {
      ElMessage.error(data.message || '加载失败')
      return
    }
    const d = data.data
    list.value = d?.list ?? []
    total.value = d?.total ?? 0
  } finally {
    loading.value = false
  }
}

function onSizeChange() {
  page.value = 1
  void load()
}

async function onApprove(row: Record<string, unknown>) {
  const id = getSubmissionId(row)
  if (!id) return
  try {
    const { value } = await ElMessageBox.prompt('可选：填写审核备注', '通过审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '备注（可空）',
    })
    await approveSubmission(id, value ?? '')
    ElMessage.success('已通过')
    void load()
  } catch (e) {
    if (e !== 'cancel') {
      /* axios 已提示 */
    }
  }
}

async function onReject(row: Record<string, unknown>) {
  const id = getSubmissionId(row)
  if (!id) return
  try {
    const { value } = await ElMessageBox.prompt('可选：填写拒绝原因', '拒绝投稿', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '原因（可空）',
    })
    await rejectSubmission(id, value ?? '')
    ElMessage.success('已拒绝')
    void load()
  } catch (e) {
    if (e !== 'cancel') {
      /* axios 已提示 */
    }
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <el-card v-if="!detailMode" shadow="never">
    <template #header>
      <span>待审核投稿</span>
      <el-button type="primary" link style="float: right" :loading="loading" @click="load">刷新</el-button>
    </template>
    <el-table v-loading="loading" :data="list" stripe empty-text="暂无待审核">
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <div class="cover" :style="coverStyle(row as Record<string, unknown>)" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="partition" label="分区" width="100" />
      <el-table-column label="时长" width="90">
        <template #default="{ row }">
          {{ formatDuration((row as Record<string, unknown>).duration) }}
        </template>
      </el-table-column>
      <el-table-column label="投稿人" width="120">
        <template #default="{ row }">
          {{ uploaderName(row as Record<string, unknown>) }}
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">
          {{ formatDate((row as Record<string, unknown>).create_time) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row as Record<string, unknown>)">
            详情
          </el-button>
          <el-button type="success" size="small" @click="onApprove(row as Record<string, unknown>)">
            通过
          </el-button>
          <el-button type="danger" size="small" @click="onReject(row as Record<string, unknown>)">
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </div>
  </el-card>

  <el-card v-else shadow="never">
    <template #header>
      <div class="detail-header-row">
        <span>投稿详情</span>
        <el-button type="primary" link @click="backToList">返回列表</el-button>
      </div>
    </template>
    <div v-if="currentRow" class="detail-layout">
      <div class="edit-header">
        <div class="file-row">
          <div class="file-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path
                fill="currentColor"
                d="M8 5a3 3 0 0 1 3-3h2a3 3 0 0 1 3 3v14a3 3 0 0 1-3 3h-2a3 3 0 0 1-3-3V5z"
              />
            </svg>
          </div>
          <div class="file-meta">
            <div class="file-name">{{ (currentRow.title as string) || '未命名视频' }}</div>
            <div class="file-status">待审核</div>
          </div>
        </div>
        <el-progress :percentage="100" :stroke-width="6" status="success" />
      </div>

      <div class="detail-player-wrap">
        <div class="section-title">视频预览</div>
        <div v-if="currentPlayUrl" class="player-shell">
          <video class="player-video" :src="currentPlayUrl" controls />
        </div>
        <el-empty v-else description="暂无视频地址" />
      </div>

      <el-form label-width="84px" class="detail-form">
        <el-form-item label="封面">
          <div class="cover-row">
            <div class="detail-cover" :style="coverStyle(currentRow as Record<string, unknown>)" />
          </div>
        </el-form-item>
        <el-form-item label="标题">
          <div class="readonly-value">{{ (currentRow.title as string) || '-' }}</div>
        </el-form-item>
        <el-form-item label="类型">
          <div class="readonly-value">{{ copyrightText(currentRow) }}</div>
        </el-form-item>
        <el-form-item label="分区">
          <div class="readonly-value">{{ (currentRow.partition as string) || '-' }}</div>
        </el-form-item>
        <el-form-item label="标签">
          <div class="readonly-value">{{ tagsText(currentRow) }}</div>
        </el-form-item>
        <el-form-item label="简介">
          <div class="readonly-value multiline">{{ descriptionText(currentRow) }}</div>
        </el-form-item>
        <el-form-item label="定时发布">
          <div class="readonly-value">
            {{
              boolFlag(currentRow, 'schedule_enabled', 'scheduleEnabled') === '是'
                ? formatDate((currentRow as any).schedule_publish_at || (currentRow as any).schedulePublishAt)
                : '否'
            }}
          </div>
        </el-form-item>
        <el-form-item label="加入合集">
          <div class="readonly-value">
            {{
              boolFlag(currentRow, 'collection_enabled', 'collectionEnabled') === '是'
                ? ((currentRow.collection_name as string) || (currentRow.collectionName as string) || '已加入合集')
                : '否'
            }}
          </div>
        </el-form-item>
        <el-form-item label="二创设置">
          <div class="readonly-value">{{ boolFlag(currentRow, 'allow_second_creation', 'allowSecondCreation') }}</div>
        </el-form-item>
        <el-form-item label="商业推广">
          <div class="readonly-value">{{ boolFlag(currentRow, 'commercial_promotion', 'commercialPromotion') }}</div>
        </el-form-item>
        <el-form-item label="投稿人">
          <div class="readonly-value">{{ uploaderName(currentRow) }}</div>
        </el-form-item>
        <el-form-item label="时长">
          <div class="readonly-value">{{ formatDuration(currentRow.duration) }}</div>
        </el-form-item>
        <el-form-item label="提交时间">
          <div class="readonly-value">{{ formatDate(currentRow.create_time) }}</div>
        </el-form-item>
      </el-form>
    </div>
    <el-empty v-else description="暂无投稿数据" />
  </el-card>
</template>

<style scoped>
.cover {
  width: 72px;
  height: 40px;
  border-radius: 4px;
  background-size: cover;
  background-position: center;
  background-color: #ebeef5;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.edit-header {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
}

.file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.file-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #eef6ff;
  color: #00a1d6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.file-meta {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-status {
  font-size: 12px;
  color: #00a1d6;
}

.detail-cover {
  width: 154px;
  height: 87px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-color: #ebeef5;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.detail-form {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 14px 14px 4px;
  background: #fff;
}

.section-title {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
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

.detail-player-wrap {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
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
</style>
