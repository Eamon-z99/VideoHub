<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import newBadgePng from '@/assets/new.png'
import hotBadgePng from '@/assets/hot.png'
import {
  fetchHotSlots,
  fetchKeywordEvents,
  fetchKeywordStats,
  fetchUserEvents,
  saveHotSlots,
} from '@/api/adminSearchManage'

type AnyRow = Record<string, any>

const activeTab = ref<'stats' | 'events' | 'slots'>('stats')

// 关键词统计
const kwLoading = ref(false)
const kwList = ref<AnyRow[]>([])
const kwTotal = ref(0)
const kwPage = ref(1)
const kwPageSize = ref(20)
const kwQuery = ref('')

// 流水查看（按关键词/用户）
const evMode = ref<'keyword' | 'user'>('keyword')
const evLoading = ref(false)
const evList = ref<AnyRow[]>([])
const evTotal = ref(0)
const evPage = ref(1)
const evPageSize = ref(20)
const evKeyword = ref('')
const evUserId = ref('')

// 热搜槽位
const slotsLoading = ref(false)
const slots = ref<Array<{ slot: number; keyword: string; badge: '' | 'NEW' | 'HOT' }>>(
  Array.from({ length: 10 }, (_, i) => ({ slot: i + 1, keyword: '', badge: '' })),
)
const slotsSaving = ref(false)

const selectedKeywordToSlot = computed(() => {
  const m = new Map<string, number>() // keyword -> slot(1-10)
  for (const s of slots.value) {
    const k = s.keyword.trim()
    if (!k) continue
    m.set(k, s.slot)
  }
  return m
})

function slotIndexOfKeyword(keyword: string): number {
  const slot = selectedKeywordToSlot.value.get(keyword.trim())
  return slot ? slot : 0
}

function firstEmptySlotIndex(): number {
  return slots.value.findIndex((s) => !s.keyword.trim())
}

async function persistSlots(silent = true) {
  if (slotKeywordDup.value.length) {
    if (!silent) ElMessage.error(`热搜词不能重复：${slotKeywordDup.value.join('、')}`)
    return
  }
  slotsSaving.value = true
  try {
    const payload = slots.value.map((s) => ({
      slot: s.slot,
      keyword: s.keyword.trim(),
      badge: s.badge,
    }))
    const { data } = await saveHotSlots(payload)
    if (!data?.success) {
      if (!silent) ElMessage.error(data?.message || '保存失败')
      return
    }
    if (!silent) ElMessage.success('已保存')
    await loadSlots()
  } finally {
    slotsSaving.value = false
  }
}

async function toggleKeywordFromStats(row: AnyRow, checked: boolean) {
  const keyword = row?.keyword ? String(row.keyword).trim() : ''
  if (!keyword) return
  const existsSlot = selectedKeywordToSlot.value.get(keyword)
  if (checked) {
    if (existsSlot) return
    const emptyIdx = firstEmptySlotIndex()
    if (emptyIdx < 0) {
      ElMessage.warning('热搜最多只能配置 10 条，请先移除再添加')
      return
    }
    const next = [...slots.value]
    const cur = next[emptyIdx]
    if (!cur) return
    next[emptyIdx] = { slot: cur.slot, keyword, badge: '' }
    slots.value = next
    await persistSlots(true)
    return
  }
  // unchecked -> remove
  if (!existsSlot) return
  const idx = existsSlot - 1
  const next = [...slots.value]
  if (next[idx]) next[idx] = { ...next[idx], keyword: '', badge: '' }
  slots.value = next
  await persistSlots(true)
}

function moveSlot(index: number, delta: number) {
  const nextIndex = index + delta
  if (nextIndex < 0 || nextIndex >= slots.value.length) return
  const arr = [...slots.value]
  const tmp = arr[index]!
  arr[index] = arr[nextIndex]!
  arr[nextIndex] = tmp
  // 槽位编号固定为 1-10（仅交换内容）
  arr[index] = { ...arr[index], slot: index + 1 }
  arr[nextIndex] = { ...arr[nextIndex], slot: nextIndex + 1 }
  slots.value = arr
}

function clearSlot(index: number) {
  const arr = [...slots.value]
  if (!arr[index]) return
  arr[index] = { ...arr[index], keyword: '', badge: '' }
  slots.value = arr
}

function formatDate(v: unknown): string {
  if (v == null || v === '') return '-'
  try {
    return new Date(String(v)).toLocaleString('zh-CN')
  } catch {
    return String(v)
  }
}

async function loadKeywordStats() {
  kwLoading.value = true
  try {
    const { data } = await fetchKeywordStats({
      page: kwPage.value,
      pageSize: kwPageSize.value,
      keyword: kwQuery.value.trim() || undefined,
    })
    if (!data?.success) {
      ElMessage.error(data?.message || '加载失败')
      kwList.value = []
      kwTotal.value = 0
      return
    }
    const d = data.data
    kwList.value = (d?.list || []) as AnyRow[]
    kwTotal.value = Number(d?.total) || 0
  } finally {
    kwLoading.value = false
  }
}

async function loadEvents() {
  evLoading.value = true
  try {
    if (evMode.value === 'keyword') {
      const keyword = evKeyword.value.trim()
      if (!keyword) {
        evList.value = []
        evTotal.value = 0
        return
      }
      const { data } = await fetchKeywordEvents({ keyword, page: evPage.value, pageSize: evPageSize.value })
      if (!data?.success) {
        ElMessage.error(data?.message || '加载失败')
        return
      }
      const d = data.data
      evList.value = (d?.list || []) as AnyRow[]
      evTotal.value = Number(d?.total) || 0
      return
    }
    const uid = evUserId.value.trim()
    if (!uid) {
      evList.value = []
      evTotal.value = 0
      return
    }
    const { data } = await fetchUserEvents({ userId: uid, page: evPage.value, pageSize: evPageSize.value })
    if (!data?.success) {
      ElMessage.error(data?.message || '加载失败')
      return
    }
    const d = data.data
    evList.value = (d?.list || []) as AnyRow[]
    evTotal.value = Number(d?.total) || 0
  } finally {
    evLoading.value = false
  }
}

function resetEventsPager() {
  evPage.value = 1
}

async function loadSlots() {
  slotsLoading.value = true
  try {
    const { data } = await fetchHotSlots()
    if (!data?.success) {
      ElMessage.error(data?.message || '加载失败')
      return
    }
    const list = data?.data?.list || []
    const next = Array.from({ length: 10 }, (_, i) => ({ slot: i + 1, keyword: '', badge: '' as const }))
    for (const it of list) {
      const slot = Number(it?.slot)
      if (!Number.isFinite(slot) || slot < 1 || slot > 10) continue
      const idx = slot - 1
      const target = next[idx]
      if (!target) continue
      target.keyword = it?.keyword ? String(it.keyword) : ''
      const b = it?.badge ? String(it.badge).toUpperCase() : ''
      target.badge = (b === 'NEW' || b === 'HOT') ? (b as any) : ''
    }
    slots.value = next
  } finally {
    slotsLoading.value = false
  }
}

const slotKeywordDup = computed(() => {
  const m = new Map<string, number>()
  for (const s of slots.value) {
    const k = s.keyword.trim()
    if (!k) continue
    m.set(k, (m.get(k) || 0) + 1)
  }
  return Array.from(m.entries()).filter(([, n]) => n > 1).map(([k]) => k)
})

async function onSaveSlots() {
  await persistSlots(false)
}

onMounted(async () => {
  await loadKeywordStats()
  await loadSlots()
})
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <span>热搜管理</span>
        <div class="header-actions">
          <el-button type="primary" link :loading="kwLoading" @click="loadKeywordStats">刷新统计</el-button>
          <el-button type="primary" link :loading="slotsLoading" @click="loadSlots">刷新槽位</el-button>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="关键词统计" name="stats">
        <div class="toolbar">
          <el-input
            v-model="kwQuery"
            placeholder="按关键词模糊搜索"
            clearable
            style="max-width: 360px"
            @keyup.enter="kwPage = 1; loadKeywordStats()"
            @clear="kwPage = 1; loadKeywordStats()"
          />
          <el-button type="primary" :loading="kwLoading" @click="kwPage = 1; loadKeywordStats()">
            查询
          </el-button>
        </div>

        <el-table v-loading="kwLoading" :data="kwList" stripe empty-text="暂无搜索数据">
          <el-table-column label="选中" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox
                :model-value="Boolean(selectedKeywordToSlot.get(String(row.keyword || '').trim()))"
                @change="(v: boolean) => toggleKeywordFromStats(row, v)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="keyword" label="关键词" min-width="200" show-overflow-tooltip />
          <el-table-column label="热搜位" width="90" align="center">
            <template #default="{ row }">
              {{ slotIndexOfKeyword(String(row.keyword || '').trim()) || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="累计次数" width="120">
            <template #default="{ row }">{{ row.total_count ?? row.totalCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="近24h" width="100">
            <template #default="{ row }">{{ row.count_24h ?? row.count24h ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="首见时间" width="170">
            <template #default="{ row }">{{ formatDate(row.first_seen_time) }}</template>
          </el-table-column>
          <el-table-column label="末见时间" width="170">
            <template #default="{ row }">{{ formatDate(row.last_seen_time) }}</template>
          </el-table-column>
          <el-table-column label="60分钟热度" width="120">
            <template #default="{ row }">{{ row.score_60m ?? row.score60m ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                @click="
                  activeTab = 'events';
                  evMode = 'keyword';
                  evKeyword = row.keyword;
                  resetEventsPager();
                  loadEvents();
                "
              >
                查看流水
              </el-button>
              <el-button
                v-if="!selectedKeywordToSlot.get(String(row.keyword || '').trim())"
                type="success"
                size="small"
                @click="toggleKeywordFromStats(row, true)"
              >
                加入热搜
              </el-button>
              <el-button
                v-else
                type="danger"
                size="small"
                @click="toggleKeywordFromStats(row, false)"
              >
                移除热搜
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            v-model:current-page="kwPage"
            v-model:page-size="kwPageSize"
            :total="kwTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="loadKeywordStats"
            @size-change="kwPage = 1; loadKeywordStats()"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="搜索流水" name="events">
        <div class="toolbar">
          <el-radio-group v-model="evMode" @change="evList = []; evTotal = 0; resetEventsPager()">
            <el-radio-button label="keyword">按关键词</el-radio-button>
            <el-radio-button label="user">按用户ID</el-radio-button>
          </el-radio-group>

          <el-input
            v-if="evMode === 'keyword'"
            v-model="evKeyword"
            placeholder="输入 keyword 精确查询"
            clearable
            style="max-width: 360px"
            @keyup.enter="resetEventsPager(); loadEvents()"
          />
          <el-input
            v-else
            v-model="evUserId"
            placeholder="输入 userId 查询该用户搜索历史"
            clearable
            style="max-width: 360px"
            @keyup.enter="resetEventsPager(); loadEvents()"
          />
          <el-button type="primary" :loading="evLoading" @click="resetEventsPager(); loadEvents()">查询</el-button>
        </div>

        <el-table v-loading="evLoading" :data="evList" stripe empty-text="暂无流水">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="keyword" label="关键词" min-width="180" show-overflow-tooltip />
          <el-table-column prop="weight" label="次数(权重)" width="110" />
          <el-table-column label="用户" min-width="160">
            <template #default="{ row }">
              <div class="user-inline">
                <span class="muted">ID:</span> {{ row.user_id ?? '-' }}
                <span class="muted" style="margin-left: 8px">昵称:</span> {{ row.username ?? '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="180">
            <template #default="{ row }">{{ formatDate(row.create_time) }}</template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            v-model:current-page="evPage"
            v-model:page-size="evPageSize"
            :total="evTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="loadEvents"
            @size-change="evPage = 1; loadEvents()"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="热搜10条配置" name="slots">
        <div class="slot-tip">
          - **规则**：仅展示前 10 槽位中已填写的关键词；每条角标只允许 **NEW/HOT/空** 之一（互斥）。
        </div>

        <el-table v-loading="slotsLoading" :data="slots" stripe>
          <el-table-column prop="slot" label="槽位" width="80" />
          <el-table-column label="关键词" min-width="260">
            <template #default="{ row }">
              <el-input v-model="row.keyword" placeholder="输入热搜词" clearable />
            </template>
          </el-table-column>
          <el-table-column label="角标" width="220">
            <template #default="{ row }">
              <el-radio-group v-model="row.badge">
                <el-radio-button label="">无</el-radio-button>
                <el-radio-button label="NEW">
                  <span class="badge-option">
                    <img class="badge-img" :src="newBadgePng" alt="新" />
                  </span>
                </el-radio-button>
                <el-radio-button label="HOT">
                  <span class="badge-option">
                    <img class="badge-img" :src="hotBadgePng" alt="热" />
                  </span>
                </el-radio-button>
              </el-radio-group>
            </template>
          </el-table-column>
          <el-table-column label="移动" width="220" fixed="right">
            <template #default="{ row, $index }">
              <el-button size="small" :disabled="$index === 0" @click="moveSlot($index, -1)">上移</el-button>
              <el-button
                size="small"
                :disabled="$index === slots.length - 1"
                @click="moveSlot($index, 1)"
              >
                下移
              </el-button>
              <el-button size="small" type="danger" @click="clearSlot($index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="slot-footer">
          <div v-if="slotKeywordDup.length" class="dup-warn">
            关键词重复：{{ slotKeywordDup.join('、') }}
          </div>
          <div class="slot-actions">
            <el-button @click="loadSlots" :loading="slotsLoading">重载</el-button>
            <el-button type="primary" :loading="slotsSaving" @click="onSaveSlots">保存</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.muted {
  color: #6b7280;
  font-size: 12px;
}
.user-inline {
  color: #111827;
  font-size: 13px;
}
.slot-tip {
  color: #6b7280;
  font-size: 12px;
  margin-bottom: 10px;
}
.slot-footer {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.dup-warn {
  color: #ef4444;
  font-size: 12px;
}
.slot-actions {
  display: flex;
  gap: 10px;
}

.badge-option {
  display: inline-flex;
  align-items: center;
  gap: 0;
}

.badge-img {
  width: 14px;
  height: 14px;
  display: inline-block;
  object-fit: contain;
}

/* 角标单选：选中背景改为浅蓝（避免默认深蓝过重） */
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #e6f4ff !important;
  border-color: #91caff !important;
  color: #1677ff !important;
  box-shadow: none !important;
}

:deep(.el-radio-button__inner:hover) {
  color: #1677ff !important;
}

:deep(.el-radio-button__inner) {
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}
</style>

