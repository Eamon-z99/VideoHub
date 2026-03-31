<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  approveAvatarSubmission,
  fetchPendingAvatarSubmissions,
  rejectAvatarSubmission,
} from '@/api/adminAvatarSubmissions'

type Row = Record<string, any>

const list = ref<Row[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const loading = ref(false)

function formatDate(v: unknown): string {
  if (v == null || v === '') return '-'
  try {
    return new Date(String(v)).toLocaleString('zh-CN')
  } catch {
    return String(v)
  }
}

function pendingAvatar(row: Row): string {
  const v = row.avatar_url ?? row.avatarUrl ?? row.pendingAvatar
  return v ? String(v) : ''
}

function currentAvatar(row: Row): string {
  const v = row.current_avatar ?? row.currentAvatar
  return v ? String(v) : ''
}

function username(row: Row): string {
  const v = row.username ?? row.userName
  return v ? String(v) : '-'
}

function userId(row: Row): string {
  const v = row.user_id ?? row.userId
  return v != null ? String(v) : '-'
}

function submissionId(row: Row): string | null {
  const v = row.id
  if (v == null || v === '') return null
  return String(v)
}

async function load() {
  loading.value = true
  try {
    const { data } = await fetchPendingAvatarSubmissions({ page: page.value, pageSize: pageSize.value })
    if (!data?.success) {
      ElMessage.error(data?.message || '加载失败')
      list.value = []
      total.value = 0
      return
    }
    const d = data.data
    list.value = (d?.list || []) as Row[]
    total.value = Number(d?.total) || 0
  } finally {
    loading.value = false
  }
}

function onSizeChange() {
  page.value = 1
  void load()
}

async function onApprove(row: Row) {
  const id = submissionId(row)
  if (!id) return
  try {
    const { value } = await ElMessageBox.prompt('可选：填写审核备注', '通过头像审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '备注（可空）',
    })
    await approveAvatarSubmission(id, value ?? '')
    ElMessage.success('已通过')
    void load()
  } catch (e) {
    if (e !== 'cancel') {
      /* axios 已提示 */
    }
  }
}

async function onReject(row: Row) {
  const id = submissionId(row)
  if (!id) return
  try {
    const { value } = await ElMessageBox.prompt('请填写拒绝原因', '驳回头像', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '原因（必填）',
      inputValidator: (val) => {
        if (!val || !String(val).trim()) return '驳回原因不能为空'
        return true
      },
    })
    await rejectAvatarSubmission(id, value ?? '')
    ElMessage.success('已驳回')
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
      <span>头像审核</span>
      <el-button type="primary" link style="float: right" :loading="loading" @click="load">刷新</el-button>
    </template>

    <el-table v-loading="loading" :data="list" stripe empty-text="暂无待审核头像">
      <el-table-column label="用户" min-width="220">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-name">{{ username(row) }}</div>
            <div class="user-id">ID: {{ userId(row) }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="当前头像" width="120">
        <template #default="{ row }">
          <el-image
            v-if="currentAvatar(row)"
            :src="currentAvatar(row)"
            fit="cover"
            class="avatar-img"
            :preview-src-list="[currentAvatar(row)]"
          />
          <span v-else class="muted">无</span>
        </template>
      </el-table-column>

      <el-table-column label="待审头像" width="120">
        <template #default="{ row }">
          <el-image
            v-if="pendingAvatar(row)"
            :src="pendingAvatar(row)"
            fit="cover"
            class="avatar-img"
            :preview-src-list="[pendingAvatar(row)]"
          />
          <span v-else class="muted">无</span>
        </template>
      </el-table-column>

      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.create_time ?? row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="onApprove(row)">通过</el-button>
          <el-button type="danger" size="small" @click="onReject(row)">驳回</el-button>
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
</template>

<style scoped>
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.avatar-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  background: #f3f4f6;
}

.muted {
  color: #9ca3af;
  font-size: 12px;
}

.user-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  color: #111827;
}

.user-id {
  font-size: 12px;
  color: #6b7280;
}
</style>

