<template>
  <div class="fans-page">
    <div class="top-tabs">
      <div class="tab active">粉丝列表</div>
      <div class="tab">粉丝勋章</div>
      <div class="tab">骑士团</div>
    </div>

    <div class="panel">
      <div class="panel-header">
        <div class="title">我的粉丝数 {{ total }}</div>
        <div class="actions">
          <el-select v-model="filter" size="small" style="width: 120px">
            <el-option label="全部粉丝" value="all" />
          </el-select>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="4" animated />
      </div>

      <div v-else class="list">
        <div v-for="u in list" :key="u.id" class="row">
          <div class="avatar" :style="u.avatar ? { backgroundImage: `url(${u.avatar})` } : {}" />
          <div class="info">
            <div class="name">{{ u.username || u.account || ('用户' + u.id) }}</div>
          </div>

          <div class="right">
            <el-button
              size="small"
              class="follow-btn"
              :type="u.iFollow ? 'default' : 'primary'"
              @click="toggleFollow(u)"
            >
              {{ u.iFollow ? '已关注' : '关注' }}
            </el-button>

            <el-dropdown trigger="click" @command="(cmd) => onMoreCommand(cmd, u)">
              <el-button size="small" class="more-btn" circle>
                <span class="more-dot">···</span>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="message">私信</el-dropdown-item>
                  <el-dropdown-item command="remove" divided>移除粉丝</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div v-if="list.length === 0" class="empty">
          <el-empty description="暂无粉丝" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { followUser, getFansUsers, removeFan, unfollowUser } from '@/api/follow'

const loading = ref(false)
const filter = ref('all')
const list = ref([])

const normalizeAvatar = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
  if (avatar.startsWith('/')) return avatar
  return '/' + avatar
}

const total = computed(() => list.value.length)

async function loadFans() {
  loading.value = true
  try {
    const { data } = await getFansUsers()
    if (data?.success && Array.isArray(data.users)) {
      list.value = data.users.map((u) => ({
        id: u.id,
        username: u.username,
        account: u.account,
        avatar: normalizeAvatar(u.avatar),
        iFollow: Number(u.iFollow || u.i_follow || 0) === 1,
      }))
    } else {
      list.value = []
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '加载粉丝失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function toggleFollow(u) {
  try {
    if (u.iFollow) {
      const { data } = await unfollowUser(u.id)
      if (data?.success) {
        u.iFollow = false
        ElMessage.success('已取消关注')
      } else {
        ElMessage.warning(data?.message || '操作失败')
      }
    } else {
      const { data } = await followUser(u.id)
      if (data?.success) {
        u.iFollow = true
        ElMessage.success('关注成功')
      } else {
        ElMessage.warning(data?.message || '操作失败')
      }
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

function openInNewTab(path) {
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${normalizedBase}${normalizedPath}`
  window.open(url, '_blank')
}

function onMoreCommand(cmd, u) {
  if (cmd === 'message') {
    openInNewTab(`/messages?peerId=${encodeURIComponent(u.id)}`)
    return
  }
  if (cmd === 'remove') {
    removeOne(u)
  }
}

async function removeOne(u) {
  try {
    await ElMessageBox.confirm('确定要移除该粉丝吗？（将取消对方对你的关注）', '移除粉丝', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const { data } = await removeFan(u.id)
    if (data?.success) {
      ElMessage.success('已移除')
      list.value = list.value.filter((x) => x.id !== u.id)
    } else {
      ElMessage.warning(data?.message || '移除失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '移除失败')
  }
}

onMounted(() => {
  loadFans()
})
</script>

<style scoped lang="scss">
.fans-page {
  padding: 0;
}

.top-tabs {
  display: flex;
  gap: 18px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 10px;
  margin-bottom: 12px;
}

.tab {
  font-size: 14px;
  color: #374151;
  padding: 8px 0;
  cursor: default;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #00a1d6;
  border-bottom-color: #00a1d6;
  font-weight: 600;
}

.panel {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  min-height: 360px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 6px;
  border-bottom: 1px solid #f3f4f6;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #e5e7eb;
  background-size: cover;
  background-position: center;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.follow-btn {
  min-width: 72px;
}

.more-btn {
  width: 34px;
  height: 34px;
  padding: 0;
}

.more-dot {
  display: inline-block;
  font-weight: 700;
  color: #6b7280;
  transform: translateY(-1px);
}

.empty {
  padding: 28px 0;
}
</style>


