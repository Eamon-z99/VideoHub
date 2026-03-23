<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchApprovedUnpublished,
  publishSubmission,
  publishDue,
  fetchSubmissionPlayUrl,
  fetchPublishedCheck,
} from '@/api/adminSubmission'
import { coverStyle, normalizeMediaUrl, rawPlayUrl } from '@/utils/cover'

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const dueLoading = ref(false)
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

function isScheduled(row: Record<string, unknown>): boolean {
  const en = row.schedule_enabled ?? row.scheduleEnabled
  if (en === true || en === 1 || en === '1') return true
  const s = String(en ?? '').toLowerCase()
  return s === 'true' || s === 'yes' || s === 'on'
}

function formatSchedule(row: Record<string, unknown>): string {
  const at = row.schedule_publish_at ?? row.schedulePublishAt
  if (at == null || at === '') return '-'
  return formatDate(at)
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
    const { data } = await fetchApprovedUnpublished({
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

async function onPublish(row: Record<string, unknown>, force: boolean) {
  const id = getSubmissionId(row)
  if (!id) return
  const tip = force
    ? '未到定时时间也会立即发布，确定？'
    : '将写入正式视频库；若未到定时发布时间可能失败，可用「强制发布」。确定发布？'
  try {
    await ElMessageBox.confirm(tip, '确认发布', { type: 'warning' })
    const { data } = await publishSubmission(id, force)
    if (data.success) {
      try {
        const checkResp = await fetchPublishedCheck(id)
        if (checkResp?.data?.success) {
          const d = checkResp.data.data || {}
          const publishedVideoId = d.publishedVideoId || ''
          const videoFound = Boolean(d.videoFound)
          const fileExists = Boolean(d.fileExists)
          if (!videoFound) {
            ElMessage.error(
              `已发布（返回 videoId），但 videos 表未找到对应记录：publishedVideoId=${publishedVideoId || '空'}`
            )
          } else if (!fileExists) {
            ElMessage.warning(
              `已发布：videos 表有记录，但视频文件未在磁盘找到（可能是路径/文件迁移失败）：publishedVideoId=${publishedVideoId || '空'}`
            )
          } else {
            ElMessage.success(
              data.videoId ? `已发布，videoId: ${data.videoId}` : '已发布（videos + 文件均正常）'
            )
          }
        } else {
          ElMessage.success(data.videoId ? `已发布，videoId: ${data.videoId}` : '已发布')
        }
      } catch {
        ElMessage.success(data.videoId ? `已发布，videoId: ${data.videoId}` : '已发布')
      }
      void load()
    }
  } catch (e) {
    if (e !== 'cancel') {
      /* axios 已提示 */
    }
  }
}

async function onPublishDue() {
  dueLoading.value = true
  try {
    const { data } = await publishDue(50)
    if (data.success) {
      const d = data.data
      if (d && typeof d === 'object') {
        ElMessage.success(`扫描完成：待处理 ${d.processed ?? 0} 条，已发布 ${d.published ?? 0} 条`)
      } else {
        ElMessage.success('批量发布任务已执行')
      }
      void load()
    }
  } finally {
    dueLoading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span>已通过、待发布</span>
      <div class="header-actions">
        <el-button type="warning" plain size="small" :loading="dueLoading" @click="onPublishDue">
          批量发布到点任务
        </el-button>
        <el-button type="primary" link :loading="loading" @click="load">刷新</el-button>
      </div>
    </template>
    <p class="tip">定时发布的稿件会在到达时间后由后端或此处「批量发布到点任务」写入正式视频库。</p>
    <el-table v-loading="loading" :data="list" stripe empty-text="暂无待发布">
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <div class="cover" :style="coverStyle(row as Record<string, unknown>)" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="partition" label="分区" width="100" />
      <el-table-column label="定时" width="120">
        <template #default="{ row }">
          <el-tag v-if="isScheduled(row as Record<string, unknown>)" type="warning" size="small">
            定时
          </el-tag>
          <span v-else>立即</span>
        </template>
      </el-table-column>
      <el-table-column label="定时发布时间" width="170">
        <template #default="{ row }">
          {{ formatSchedule(row as Record<string, unknown>) }}
        </template>
      </el-table-column>
      <el-table-column label="投稿人" width="120">
        <template #default="{ row }">
          {{ uploaderName(row as Record<string, unknown>) }}
        </template>
      </el-table-column>
      <el-table-column label="通过/创建时间" width="170">
        <template #default="{ row }">
          {{ formatDate((row as Record<string, unknown>).create_time) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="290" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" plain size="small" @click="playVideo(row as Record<string, unknown>)">
            观看
          </el-button>
          <el-button type="primary" size="small" @click="onPublish(row as Record<string, unknown>, false)">
            发布
          </el-button>
          <el-button type="danger" plain size="small" @click="onPublish(row as Record<string, unknown>, true)">
            强制发布
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
.header-actions {
  float: right;
  display: flex;
  align-items: center;
  gap: 8px;
}
.tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
}
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
