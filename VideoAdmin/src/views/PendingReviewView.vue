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
const playerVisible = ref(false)
const currentPlayUrl = ref('')
const currentTitle = ref('')

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

async function playVideo(row: Record<string, unknown>) {
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
    return
  }
  currentPlayUrl.value = url
  currentTitle.value = typeof row.title === 'string' ? row.title : '视频预览'
  playerVisible.value = true
}

function onPlayerClosed() {
  playerVisible.value = false
  currentPlayUrl.value = ''
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
  <el-card shadow="never">
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
          <el-button type="primary" plain size="small" @click="playVideo(row as Record<string, unknown>)">
            观看
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

  <el-dialog
    v-model="playerVisible"
    :title="`预览：${currentTitle}`"
    width="760px"
    destroy-on-close
    @closed="onPlayerClosed"
  >
    <video v-if="currentPlayUrl" class="player" :src="currentPlayUrl" controls autoplay />
    <el-empty v-else description="暂无视频地址" />
  </el-dialog>
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

.player {
  width: 100%;
  max-height: 70vh;
  background: #000;
  border-radius: 8px;
}
</style>
