<template>
  <!-- 图2：左栏导航（蓝底白字选中）+ 右栏标题 / 筛选 / 搜索 / 卡片栅格 -->
  <aside class="rel-sidebar-col">
    <div class="rel-side-section">
      <div class="rel-side-head">{{ isOwnProfile ? '我的关注' : 'TA的关注' }}</div>
      <button
        v-if="isOwnProfile"
        type="button"
        class="rel-new-group"
        @click="onNewGroup"
      >
        <span class="rel-new-group-plus">＋</span>
        新建分组
      </button>
      <div
        class="rel-nav-scroll"
        :class="{ 'rel-nav-scroll--overflow': sidebarFollowNeedsScroll }"
      >
      <ul class="rel-nav-list">
        <li
          class="rel-nav-item vui_sidebar-item"
          :class="{ 'vui_sidebar-item--active': isFollowingMode && activeGroup === 'all' }"
          @click="selectFollowingNav('all')"
        >
          <span class="rel-nav-icon" aria-hidden="true">
            <svg class="rel-nav-icon-svg" viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg">
              <path d="M17.2036 6.10902C17.3632 5.72677 17.8024 5.54623 18.1846 5.70577C20.35055 6.60978 21.87505 8.74844 21.87505 11.24495C21.87505 13.51545 20.6136 15.49025 18.75685 16.50865C18.3937 16.70785 17.9378 16.57495 17.7386 16.2118C17.5394 15.8486 17.6723 15.3927 18.0355 15.1935C19.4314 14.4278 20.37505 12.94605 20.37505 11.24495C20.37505 9.37462 19.23385 7.76911 17.6069 7.09004C17.2246 6.93049 17.0441 6.49127 17.2036 6.10902z" fill="currentColor" />
              <path d="M17.7553 15.4977C17.9578 15.1364 18.4149 15.00765 18.7762 15.21015C20.86785 16.3824 21.5243 18.1724 21.6619 18.62735C21.78185 19.0238 21.5577 19.44245 21.1612 19.5624C20.76475 19.68235 20.3461 19.4582 20.2262 19.06175C20.14655 18.7986 19.662 17.42605 18.04285 16.51865C17.68155 16.31615 17.5528 15.85905 17.7553 15.4977z" fill="currentColor" />
              <path d="M9.125 4C6.0184 4 3.5 6.5184 3.5 9.625C3.5 11.7516 4.67988 13.6038 6.42431 14.5606C6.78748 14.7598 6.9204 15.2157 6.7212 15.5789C6.522 15.94205 6.06611 16.075 5.70294 15.8758C3.49771 14.6662 2 12.321 2 9.625C2 5.68997 5.18997 2.5 9.125 2.5C13.06005 2.5 16.25 5.68997 16.25 9.625C16.25 12.321 14.7523 14.6662 12.54705 15.8758C12.1839 16.075 11.728 15.94205 11.5288 15.57885C11.3296 15.2157 11.4625 14.7598 11.8257 14.5606C13.5701 13.6038 14.75 11.7516 14.75 9.625C14.75 6.5184 12.2316 4 9.125 4z" fill="currentColor" />
              <path d="M6.70177 14.868C6.90428 15.2293 6.77552 15.6864 6.41418 15.8889C4.39752 17.0191 3.79067 18.7305 3.68779 19.07055C3.56784 19.467 3.14921 19.6912 2.75274 19.5712C2.35628 19.4513 2.13211 19.03265 2.25206 18.6362C2.41297 18.10435 3.19161 15.97545 5.68084 14.58035C6.04218 14.37785 6.49926 14.50665 6.70177 14.868zM11.5484 14.868C11.75095 14.50665 12.208 14.37785 12.56935 14.58035C15.0586 15.97545 15.8372 18.10435 15.9981 18.6362C16.1181 19.03265 15.89395 19.4513 15.4975 19.5712C15.10095 19.6912 14.6824 19.467 14.5624 19.07055C14.4595 18.7305 13.8527 17.0191 11.836 15.8889C11.47465 15.6864 11.3459 15.2293 11.5484 14.868z" fill="currentColor" />
            </svg>
          </span>
          <span class="rel-nav-label">全部关注</span>
          <span class="rel-nav-count">{{ totalFollowingCount }}</span>
        </li>
        <li
          v-for="g in sidebarCustomGroups"
          :key="g.id"
          class="rel-nav-item rel-nav-item--action vui_sidebar-item"
          :class="{ 'vui_sidebar-item--active': isFollowingMode && isSidebarGroupActive(g.id) }"
          @click="selectFollowingNav(g.id)"
        >
          <span class="rel-nav-icon" aria-hidden="true">
            <svg class="rel-nav-icon-svg" viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 3.5C8.54822 3.5 5.75 6.29822 5.75 9.75C5.75 12.0005 6.93897 13.97395 8.72662 15.07535C9.07927 15.2927 9.18901 15.7547 8.97172 16.10735C8.75445 16.46 8.29242 16.56975 7.93977 16.35245C5.72747 14.98935 4.25 12.54255 4.25 9.75C4.25 5.46979 7.71979 2 12 2C16.2802 2 19.75 5.46979 19.75 9.75C19.75 11.61445 19.07265 13.3226 17.95715 14.6334C17.7066 14.96045 17.23835 15.02245 16.9113 14.7719C16.5843 14.5214 16.5223 14.0532 16.77285 13.72615C17.6917 12.6635 18.25 11.27295 18.25 9.75C18.25 6.29822 15.4518 3.5 12 3.5z" fill="currentColor" />
              <path d="M8.96424 15.368C9.1752 15.7245 9.05725 16.18445 8.70078 16.39545C6.50693 17.6938 5.8355 19.5794 5.7177 19.9677C5.59745 20.3641 5.17865 20.58795 4.78228 20.46765C4.3859 20.34745 4.16205 19.92865 4.2823 19.53225C4.45731 18.9554 5.29392 16.66865 7.93682 15.10455C8.29329 14.89355 8.75327 15.01155 8.96424 15.368z" fill="currentColor" />
              <path d="M9.40854 11.03805C9.7903 10.87745 10.23 11.05675 10.39065 11.4385C10.65855 12.06405 11.2792 12.49995 12 12.49995C12.72075 12.49995 13.3414 12.06405 13.6094 11.4385C13.77 11.05675 14.2097 10.87745 14.59145 11.03805C14.97325 11.1987 15.1525 11.6384 14.99185 12.02015C14.49815 13.1825 13.3454 13.99995 12 13.99995C10.6546 13.99995 9.50183 13.1825 9.00813 12.02015C8.8475 11.6384 9.02677 11.1987 9.40854 11.03805z" fill="currentColor" />
              <path d="M13 17.5C13 17.0858 13.3358 16.75 13.75 16.75L21.25 16.75C21.6642 16.75 22 17.0858 22 17.5C22 17.9142 21.6642 18.25 21.25 18.25L13.75 18.25C13.3358 18.25 13 17.9142 13 17.5z" fill="currentColor" />
              <path d="M13 21.5C13 21.0858 13.3358 20.75 13.75 20.75L17.75 20.75C18.1642 20.75 18.5 21.0858 18.5 21.5C18.5 21.9142 18.1642 22.25 17.75 22.25L13.75 22.25C13.3358 22.25 13 21.9142 13 21.5z" fill="currentColor" />
            </svg>
          </span>
          <span class="rel-nav-label">{{ g.name }}</span>
          <span class="rel-nav-count">{{ g.count }}</span>
          <button
            v-if="isOwnProfile"
            type="button"
            class="rel-nav-delete"
            title="删除分组"
            @click.stop="onDeleteGroup(g)"
          >
            ×
          </button>
        </li>
      </ul>
      </div>
    </div>
    <div class="rel-side-section">
      <div class="rel-side-head">{{ isOwnProfile ? '我的粉丝' : 'TA的粉丝' }}</div>
      <ul class="rel-nav-list">
        <li
          class="rel-nav-item vui_sidebar-item"
          :class="{ 'vui_sidebar-item--active': isFansMode }"
          @click="selectFansNav"
        >
          <span class="rel-nav-icon" aria-hidden="true">
            <svg
              class="rel-nav-icon-svg"
              width="18"
              height="18"
              viewBox="0 0 1024 1024"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fill="currentColor"
                d="M959.488 573.44c-56.32-53.248-144.384-55.808-206.848-10.24-7.168-5.12-14.848-10.24-23.04-13.824-1.536-2.048-3.072-3.584-5.12-5.632-28.16-24.064-58.368-44.544-89.6-61.44-5.632-3.072-11.776-5.12-17.408-6.656 50.176-53.76 77.824-113.664 80.896-175.104 3.072-62.464-20.48-123.392-67.584-175.104-42.496-47.104-105.472-76.288-172.544-79.872-67.584-3.584-133.632 18.944-181.248 61.952C220.16 158.72 192.512 223.744 196.608 295.424c3.584 58.88 28.672 119.808 73.728 178.688C121.856 547.84 36.864 670.72 17.92 840.192c-4.608 42.496 3.072 76.288 24.064 99.328 19.968 22.528 50.688 34.304 90.624 34.304h85.504c79.36 0 160.256 0.512 241.152 0.512 90.624 0 181.76-0.512 271.872-1.024 2.048 0 4.096 0 5.632-0.512 5.12 0.512 9.728 1.024 14.848 1.024 27.136 0 52.224-10.24 71.168-29.184l139.776-139.776c30.72-30.72 47.616-72.704 47.104-116.224 0-44.032-18.432-86.016-50.176-115.2zM218.624 912.384H133.12c-22.016 0-36.864-4.608-45.056-13.312-10.752-12.288-10.752-34.816-8.704-51.712 17.92-161.28 100.864-270.336 252.416-332.8 9.216-3.584 15.872-11.776 18.432-21.504 2.048-9.728-0.512-19.968-7.168-27.136-103.936-115.2-113.152-231.936-24.064-312.32 35.328-32.256 84.992-49.152 136.704-46.08 51.2 2.56 98.304 24.064 130.048 59.392 36.352 39.936 54.272 84.992 51.712 131.072-2.56 54.272-32.256 109.056-86.528 157.696-8.192 7.68-11.776 18.944-9.216 30.208 8.704 35.328 37.888 43.008 53.76 47.616 4.096 1.024 8.704 2.56 10.752 3.584l3.072 1.536c-23.04 6.656-44.544 18.432-62.976 35.84-31.744 29.696-50.176 71.68-51.2 114.688-0.512 43.52 16.384 85.504 47.104 116.224l107.52 107.52c-144.384 0-289.792 0-431.104-0.512zm701.44-151.04L780.288 901.12c-7.168 7.168-17.408 11.264-27.648 11.264-10.752 0-20.48-4.096-27.648-11.264l-139.776-139.776c-18.944-18.944-29.184-45.056-29.184-71.68 0.512-26.624 11.776-52.736 31.744-71.168 18.432-17.408 41.984-25.6 66.048-25.6 27.136 0 54.784 10.752 75.776 31.744l1.024 1.024c11.776 11.776 31.232 11.776 43.52 0l1.024-1.024c39.424-39.936 102.912-42.496 141.824-6.144l0.512 0.512c19.456 17.92 30.72 43.52 31.232 70.656 1.024 26.624-9.728 52.736-28.672 71.68z"
              />
            </svg>
          </span>
          <span class="rel-nav-label">全部粉丝</span>
          <span class="rel-nav-count">{{ fansTotalCount }}</span>
        </li>
      </ul>
    </div>
  </aside>

  <section class="rel-main-col">
    <div class="rel-main">
        <header class="rel-main-head">
          <h1 class="rel-title">
            {{ mainAreaTitle }}
          </h1>
          <div class="rel-head-actions">
            <button
              v-if="isOwnProfile && !isFansMode && followingUsers.length && !batchMode"
              type="button"
              class="batch-btn"
              @click="toggleBatchMode"
            >
              批量操作
            </button>
          </div>
        </header>

        <div
          v-if="batchMode && isOwnProfile && !isFansMode"
          class="rel-batch-bar"
        >
          <label class="rel-batch-check">
            <input
              type="checkbox"
              class="rel-checkbox-input"
              :checked="batchAllSelected"
              @change="toggleSelectAllBatch"
            />
            全选
          </label>
          <span class="rel-batch-meta">已选 {{ selectedUserIds.length }} 人</span>
          <div class="rel-batch-actions">
            <el-dropdown
              trigger="click"
              @command="(cmd) => batchMoveToGroup(cmd === 'null' ? null : Number(cmd))"
            >
              <button type="button" class="batch-btn batch-btn--primary">
                移入分组
              </button>
              <template #dropdown>
                <el-dropdown-menu class="rel-batch-move-menu">
                  <el-dropdown-item
                    v-for="g in followGroups"
                    :key="g.id"
                    :command="String(g.id)"
                  >
                    {{ g.name }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    :divided="followGroups.length > 0"
                    command="null"
                  >
                    未分组（移出分组）
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <button
              type="button"
              class="batch-btn batch-btn--danger"
              @click="batchUnfollowSelected"
            >
              取消关注
            </button>
            <button type="button" class="batch-btn" @click="toggleBatchMode">
              完成
            </button>
          </div>
        </div>

        <div class="rel-toolbar" :class="{ 'rel-toolbar--dim': batchMode }">
          <div class="rel-sort-tabs">
            <button
              type="button"
              class="rel-sort-pill"
              :class="{ active: sortMode === 'recent' }"
              @click="sortMode = 'recent'"
            >
              {{ isFansMode ? '最近粉丝' : '最近关注' }}
            </button>
            <button
              type="button"
              class="rel-sort-pill"
              :class="{ active: sortMode === 'active' }"
              @click="sortMode = 'active'"
            >
              最常访问
            </button>
          </div>
          <div class="page-search page-search--h34 page-search--w320 rel-search">
            <div class="search-input-row">
              <input
                v-model.trim="keyword"
                class="search-input"
                type="search"
                placeholder="输入关键词"
                autocomplete="off"
              />
              <button type="button" class="search-btn" aria-label="搜索" @click.prevent>
                <img class="search-btn-img" src="/assets/search-button.png" alt="" />
              </button>
            </div>
          </div>
        </div>

        <div v-if="loading" class="state">加载中...</div>
        <div v-else-if="displayUsers.length === 0" class="state muted">暂无数据</div>
        <div v-else class="user-grid">
          <article
            v-for="u in displayUsers"
            :key="u.id"
            class="user-card"
            :class="{ 'user-card--batch': batchMode && isOwnProfile && !isFansMode }"
          >
            <label
              v-if="batchMode && isOwnProfile && !isFansMode"
              class="user-card__batch-check"
              @click.stop
            >
              <input
                type="checkbox"
                class="rel-checkbox-input"
                :checked="isUserBatchSelected(u.id)"
                @change="toggleUserBatchSelect(u.id)"
              />
            </label>
            <div class="card-body">
              <div class="card-inner">
                <a
                  v-if="!isBatchFollowingBlockNav"
                  class="avatar-wrap avatar-wrap--link"
                  :href="profileAbsoluteUrl(u.id)"
                  rel="noopener noreferrer"
                  @click="openProfileInNewTab($event, u.id)"
                >
                  <img
                    class="avatar"
                    :src="normalizeAvatar(u.avatar)"
                    :alt="u.username"
                    @error="onImgErr"
                  />
                </a>
                <div v-else class="avatar-wrap">
                  <img
                    class="avatar"
                    :src="normalizeAvatar(u.avatar)"
                    :alt="u.username"
                    @error="onImgErr"
                  />
                </div>
                <div class="text-col">
                  <a
                    v-if="!isBatchFollowingBlockNav"
                    class="uname uname--link"
                    :href="profileAbsoluteUrl(u.id)"
                    rel="noopener noreferrer"
                    @click="openProfileInNewTab($event, u.id)"
                  >
                    {{ u.username || '用户' }}
                  </a>
                  <div v-else class="uname">{{ u.username || '用户' }}</div>
                  <p class="ubio">{{ bioText(u) }}</p>
                </div>
              </div>
              <div
                v-if="!batchMode || !isOwnProfile || isFansMode"
                class="card-actions"
                @click.stop
              >
              <template v-if="isFansMode">
                <el-dropdown v-if="isOwnProfile" trigger="click" @command="(cmd) => onFanCommand(cmd, u)">
                  <button type="button" class="follow-btn">
                    <span class="menu-dots">≡</span>
                    {{ u.iFollow ? '已关注' : '关注' }}
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="!u.iFollow" command="follow">关注</el-dropdown-item>
                      <el-dropdown-item v-else command="unfollow">取消关注</el-dropdown-item>
                      <el-dropdown-item command="remove" divided>移除粉丝</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <template v-else>
                  <button
                    type="button"
                    class="follow-btn"
                    :disabled="followLoadingId === u.id || !userStore.isAuthenticated"
                    @click="toggleFollowUser(u)"
                  >
                    <span class="menu-dots">≡</span>
                    {{ u.iFollow ? '已关注' : '关注' }}
                  </button>
                </template>
              </template>
              <template v-else>
                <el-popover
                  v-if="isOwnProfile"
                  placement="bottom-end"
                  :width="260"
                  trigger="click"
                  popper-class="rel-follow-popper"
                  @hide="onFollowPopHide"
                >
                  <template #reference>
                    <button type="button" class="follow-btn">
                      <span class="menu-dots">≡</span>
                      已关注
                    </button>
                  </template>
                  <div class="rel-follow-menu">
                    <button
                      type="button"
                      class="rel-follow-menu__unfollow"
                      @click="onMenuUnfollow(u)"
                    >
                      取消关注
                    </button>
                    <div v-if="followGroups.length" class="rel-follow-menu__move">
                      <button
                        type="button"
                        class="rel-follow-menu__move-trigger"
                        @click="moveExpandUserId = moveExpandUserId === u.id ? null : u.id"
                      >
                        <span>移入</span>
                        <span class="rel-follow-menu__chev">{{ moveExpandUserId === u.id ? '▾' : '▸' }}</span>
                      </button>
                      <div
                        v-show="moveExpandUserId === u.id"
                        class="rel-follow-menu__move-scroll"
                        :class="{ 'rel-follow-menu__move-scroll--overflow': followGroups.length > 10 }"
                      >
                        <button
                          v-for="g in followGroups"
                          :key="g.id"
                          type="button"
                          class="rel-follow-menu__move-item"
                          @click="onMenuSetGroup(u, g.id)"
                        >
                          {{ g.name }}
                        </button>
                      </div>
                    </div>
                    <div class="rel-follow-menu__divider" />
                    <button
                      type="button"
                      class="rel-follow-menu__footer"
                      @click="onMenuSetGroup(u, null)"
                    >
                      未分组（移出分组）
                    </button>
                  </div>
                </el-popover>
                <button
                  v-else
                  type="button"
                  class="follow-btn"
                  :disabled="followLoadingId === u.id || !userStore.isAuthenticated"
                  @click="toggleFollowUser(u)"
                >
                  <span class="menu-dots">≡</span>
                  {{ u.iFollow ? '已关注' : '关注' }}
                </button>
              </template>
              </div>
            </div>
          </article>
        </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getProfileFollowingUsers,
  getProfileFansUsers,
  followUser,
  unfollowUser,
  removeFan,
  listFollowGroups,
  createFollowGroup,
  deleteFollowGroup,
  setFollowingGroup as apiSetFollowingGroup
} from '@/api/follow'

const props = defineProps({
  /** 被查看的个人主页用户 id（与路由 /profile/:id 一致） */
  profileUserId: { type: [Number, String], default: null }
})

/** following | fans，与 UserProfile.relationsView 同步 */
const mode = defineModel('mode', { type: String, required: true })

const router = useRouter()
const userStore = useUserStore()

/** 当前站点下的个人主页绝对地址（含路由 base，供新窗口打开） */
function profileAbsoluteUrl (userId) {
  const r = router.resolve({ name: 'profile', params: { id: String(userId) } })
  const path = r.href ?? r.fullPath
  if (!path) {
    return `${window.location.origin}/profile/${encodeURIComponent(String(userId))}`
  }
  if (/^https?:\/\//i.test(path)) {
    return path
  }
  const normalized = path.startsWith('/') ? path : `/${path}`
  try {
    return new URL(normalized, window.location.origin).href
  } catch {
    return `${window.location.origin}${normalized}`
  }
}

/** 显式新标签打开，避免 SPA 内仍被当成同页路由切换 */
function openProfileInNewTab (e, userId) {
  if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return
  if (e.button !== 0) return
  e.preventDefault()
  e.stopPropagation()
  const url = profileAbsoluteUrl(userId)
  window.open(url, '_blank', 'noopener,noreferrer')
}

const resolvedProfileId = computed(() => {
  const p = props.profileUserId
  if (p == null || p === '') return null
  const n = Number(p)
  return Number.isFinite(n) ? n : null
})

const isFansMode = computed(() => mode.value === 'fans')
const isFollowingMode = computed(() => mode.value === 'following')

const myUserId = computed(() => {
  const u = userStore.user
  if (!u) return null
  const id = u.userId ?? u.id
  return id != null ? Number(id) : null
})

const isOwnProfile = computed(() => {
  if (resolvedProfileId.value == null || myUserId.value == null) return false
  return resolvedProfileId.value === myUserId.value
})

const followingUsers = ref([])
const fansUsers = ref([])
const followGroups = ref([])
const loading = ref(false)
const keyword = ref('')
const sortMode = ref('recent')
/** 'all' | 'ungrouped' | 分组数字 id */
const activeGroup = ref('all')
const followLoadingId = ref(null)

/** 批量：仅「我的关注」列表 */
const batchMode = ref(false)
const selectedUserIds = ref([])
const moveExpandUserId = ref(null)

/** 批量选关注时头像/昵称不跳转，避免误触主页 */
const isBatchFollowingBlockNav = computed(
  () => batchMode.value && isOwnProfile.value && !isFansMode.value
)

function rowGroupId (row) {
  const g = row.groupId ?? row.group_id
  return g != null && g !== '' ? Number(g) : null
}

function isSidebarGroupActive (gid) {
  const a = activeGroup.value
  if (typeof a !== 'number' && typeof gid !== 'number') {
    return a === gid
  }
  return Number(a) === Number(gid)
}

const fansTotalCount = computed(() => fansUsers.value.length)

const totalFollowingCount = computed(() => followingUsers.value.length)

/** 侧栏自定义分组：本人用接口数据并带上实时人数；访客从列表推导 */
const sidebarCustomGroups = computed(() => {
  if (isOwnProfile.value) {
    return followGroups.value.map((g) => ({
      id: g.id,
      name: g.name,
      count: followingUsers.value.filter((row) => rowGroupId(row) === Number(g.id)).length
    }))
  }
  const m = new Map()
  for (const row of followingUsers.value) {
    const gid = rowGroupId(row)
    const gn = row.groupName ?? row.group_name
    if (gid == null) continue
    if (!m.has(gid)) {
      m.set(gid, { id: gid, name: gn || '分组', count: 0 })
    }
    m.get(gid).count++
  }
  return [...m.values()].sort((a, b) => a.id - b.id)
})

/** 侧栏：全部关注 1 行 + 自定义分组；超过 10 行出现滚动 */
const sidebarFollowRowCount = computed(() => 1 + sidebarCustomGroups.value.length)
const sidebarFollowNeedsScroll = computed(() => sidebarFollowRowCount.value > 10)

const mainAreaTitle = computed(() => {
  if (isFansMode.value) {
    return isOwnProfile.value ? '全部粉丝' : 'TA的粉丝'
  }
  if (activeGroup.value === 'all' || activeGroup.value === 'ungrouped') {
    return isOwnProfile.value ? '全部关注' : 'TA的关注'
  }
  const g = sidebarCustomGroups.value.find(
    (x) => Number(x.id) === Number(activeGroup.value)
  )
  return g ? g.name : '全部关注'
})

const listSource = computed(() => (isFansMode.value ? fansUsers.value : followingUsers.value))

function selectFollowingNav (val) {
  mode.value = 'following'
  activeGroup.value = val
}

function selectFansNav () {
  mode.value = 'fans'
}

function normalizeAvatar (url) {
  if (!url) return '/public/favicon.ico'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

function onImgErr (e) {
  e.target.src = '/public/favicon.ico'
}

function bioText (u) {
  const b = (u.bio && String(u.bio).trim()) || ''
  return b || '暂无简介'
}

const filteredUsers = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  let list = listSource.value.map((row) => ({
    id: row.id,
    username: row.username,
    avatar: row.avatar,
    account: row.account,
    bio: row.bio,
    followerCount: row.followerCount ?? row.follower_count,
    iFollow: Number(row.iFollow ?? row.i_follow ?? 0) === 1,
    groupId: rowGroupId(row),
    groupName: row.groupName ?? row.group_name ?? null
  }))
  if (kw) {
    list = list.filter(
      (u) =>
        (u.username && u.username.toLowerCase().includes(kw)) ||
        (u.account && String(u.account).toLowerCase().includes(kw)) ||
        (u.bio && String(u.bio).toLowerCase().includes(kw))
    )
  }
  return list
})

const displayUsers = computed(() => {
  if (isFansMode.value) return filteredUsers.value
  const list = filteredUsers.value
  const ag = activeGroup.value
  if (ag === 'all' || ag === 'ungrouped') return list
  return list.filter((u) => Number(u.groupId) === Number(ag))
})

const batchAllSelected = computed(() => {
  const list = displayUsers.value
  if (!list.length) return false
  const ids = new Set(list.map((u) => u.id))
  return [...ids].every((id) => selectedUserIds.value.includes(id))
})

function toggleBatchMode () {
  if (!isOwnProfile.value || isFansMode.value) return
  batchMode.value = !batchMode.value
  if (!batchMode.value) selectedUserIds.value = []
}

function toggleUserBatchSelect (id) {
  const i = selectedUserIds.value.indexOf(id)
  if (i === -1) selectedUserIds.value = [...selectedUserIds.value, id]
  else selectedUserIds.value = selectedUserIds.value.filter((x) => x !== id)
}

function toggleSelectAllBatch () {
  const list = displayUsers.value
  if (batchAllSelected.value) {
    selectedUserIds.value = []
  } else {
    selectedUserIds.value = list.map((u) => u.id)
  }
}

function isUserBatchSelected (id) {
  return selectedUserIds.value.includes(id)
}

async function applySetFollowingGroup (userId, gid, { silent = false } = {}) {
  try {
    const { data } = await apiSetFollowingGroup(userId, gid)
    if (data?.success) {
      const raw = followingUsers.value.find((row) => row.id === userId)
      if (raw) {
        if (gid == null) {
          raw.groupId = null
          raw.group_id = null
          raw.groupName = null
          raw.group_name = null
        } else {
          raw.groupId = gid
          raw.group_id = gid
          const g = followGroups.value.find((x) => Number(x.id) === gid)
          raw.groupName = g?.name ?? null
          raw.group_name = raw.groupName
        }
      }
      await loadGroups()
      if (!silent) ElMessage.success('已更新分组')
      return true
    }
    ElMessage.warning(data?.message || '操作失败')
    return false
  } catch (_) {
    ElMessage.error('设置分组失败')
    return false
  }
}

async function batchUnfollowSelected () {
  const ids = [...selectedUserIds.value]
  if (!ids.length) {
    ElMessage.info('请先选择用户')
    return
  }
  try {
    await ElMessageBox.confirm(`确定对 ${ids.length} 人取消关注？`, '批量取消关注', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  let ok = 0
  for (const id of ids) {
    try {
      const { data } = await unfollowUser(id)
      if (data?.success) ok++
    } catch (_) {}
  }
  ElMessage.success(`已取消关注 ${ok} 人`)
  selectedUserIds.value = []
  await load()
}

async function batchMoveToGroup (gid) {
  const ids = [...selectedUserIds.value]
  if (!ids.length) {
    ElMessage.info('请先选择用户')
    return
  }
  let ok = 0
  for (const id of ids) {
    try {
      const { data } = await apiSetFollowingGroup(id, gid)
      if (data?.success) ok++
    } catch (_) {}
  }
  if (ok) ElMessage.success(`已更新 ${ok} 人的分组`)
  selectedUserIds.value = []
  await load()
}

async function loadGroups () {
  if (!isOwnProfile.value || !userStore.isAuthenticated) {
    followGroups.value = []
    return
  }
  try {
    const { data } = await listFollowGroups()
    if (data?.success && Array.isArray(data.groups)) {
      followGroups.value = data.groups
    } else {
      followGroups.value = []
    }
  } catch {
    followGroups.value = []
  }
}

async function load () {
  const uid = resolvedProfileId.value
  if (!uid) return
  loading.value = true
  try {
    const sort = sortMode.value === 'active' ? 'active' : 'recent'
    const [followRes, fansRes] = await Promise.all([
      getProfileFollowingUsers(uid, { sort }),
      getProfileFansUsers(uid, { sort })
    ])
    const fd = followRes?.data
    const fansd = fansRes?.data
    if (fd?.success && Array.isArray(fd.users)) {
      followingUsers.value = fd.users
    } else {
      followingUsers.value = []
    }
    if (fansd?.success && Array.isArray(fansd.users)) {
      fansUsers.value = fansd.users
    } else {
      fansUsers.value = []
    }
    await loadGroups()
  } catch (e) {
    console.error(e)
    followingUsers.value = []
    fansUsers.value = []
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function onFollowPopHide () {
  moveExpandUserId.value = null
}

async function onNewGroup () {
  if (!isOwnProfile.value) return
  if (isFansMode.value) {
    mode.value = 'following'
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入分组名称', '新建分组', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：游戏区',
      inputValidator: (v) => {
        const s = (v || '').trim()
        if (!s) return '名称不能为空'
        if (s.length > 100) return '名称过长'
        return true
      }
    })
    const name = (value || '').trim()
    const { data } = await createFollowGroup(name)
    if (data?.success) {
      ElMessage.success('分组已创建')
      await loadGroups()
    } else {
      ElMessage.warning(data?.message || '创建失败')
    }
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.warning(e?.response?.data?.message || '创建失败')
  }
}

async function onDeleteGroup (g) {
  if (!g?.id || !isOwnProfile.value) return
  try {
    await ElMessageBox.confirm(`确定删除分组「${g.name}」？组内关注将变为未分组。`, '删除分组', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    const { data } = await deleteFollowGroup(g.id)
    if (data?.success) {
      ElMessage.success('已删除')
      if (activeGroup.value === g.id) activeGroup.value = 'all'
      await load()
    } else {
      ElMessage.warning('删除失败')
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

async function toggleFollowUser (u) {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  const id = u.id
  followLoadingId.value = id
  try {
    if (u.iFollow) {
      const { data } = await unfollowUser(id)
      if (data?.success) {
        u.iFollow = false
        ElMessage.success('已取消关注')
      } else {
        ElMessage.warning(data?.message || '操作失败')
      }
    } else {
      const { data } = await followUser(id)
      if (data?.success) {
        u.iFollow = true
        ElMessage.success('关注成功')
      } else {
        ElMessage.warning(data?.message || '关注失败')
      }
    }
  } catch (_) {
    // 拦截器提示
  } finally {
    followLoadingId.value = null
  }
}

async function onFollowingCommand (cmd, u) {
  if (cmd === 'unfollow') {
    try {
      const { data } = await unfollowUser(u.id)
      if (data?.success) {
        ElMessage.success('已取消关注')
        followingUsers.value = followingUsers.value.filter((row) => row.id !== u.id)
      } else {
        ElMessage.warning(data?.message || '操作失败')
      }
    } catch (_) {}
  }
}

async function onMenuUnfollow (u) {
  moveExpandUserId.value = null
  await onFollowingCommand('unfollow', u)
}

async function onMenuSetGroup (u, gid) {
  moveExpandUserId.value = null
  await applySetFollowingGroup(u.id, gid)
}

async function onFanCommand (cmd, u) {
  if (cmd === 'follow') {
    await toggleFollowUser(u)
    return
  }
  if (cmd === 'unfollow') {
    await toggleFollowUser(u)
    return
  }
  if (cmd === 'remove') {
    if (!isOwnProfile.value) return
    try {
      await ElMessageBox.confirm('确定移除该粉丝？对方将不再关注你。', '移除粉丝', {
        type: 'warning',
        confirmButtonText: '移除',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    try {
      const { data } = await removeFan(u.id)
      if (data?.success) {
        ElMessage.success('已移除')
        fansUsers.value = fansUsers.value.filter((row) => row.id !== u.id)
      } else {
        ElMessage.error('移除失败')
      }
    } catch (_) {}
  }
}

watch(
  () => isFansMode.value,
  (fans) => {
    if (fans) {
      batchMode.value = false
      selectedUserIds.value = []
    }
  }
)

watch(
  () => resolvedProfileId.value,
  () => {
    batchMode.value = false
    selectedUserIds.value = []
    if (activeGroup.value === 'ungrouped') activeGroup.value = 'all'
    load()
  },
  { immediate: true }
)

watch(sortMode, () => {
  if (resolvedProfileId.value != null) load()
})
</script>

<style scoped lang="scss">
/* —— 图2 左栏：蓝底白字选中、灰边「新建分组」、展开分组 —— */
$rel-blue: #00aeec;
$rel-text: #222;
$rel-muted: #9499a0;

.rel-sidebar-col {
  width: 100%;
  min-width: 0;
  align-self: start;
  background: #fff;
  border-radius: 8px;
  padding: 12px 10px 16px;
  box-sizing: border-box;
}

.rel-side-section {
  margin-bottom: 22px;

  &:last-child {
    margin-bottom: 0;
  }
}

.rel-side-head {
  font-size: 14px;
  font-weight: 600;
  color: $rel-text;
  margin-bottom: 10px;
}

.rel-new-group {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 10px;
  margin-bottom: 12px;
  border: 1px solid #e3e5e7;
  border-radius: 8px;
  background: #fff;
  color: $rel-muted;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;

  &:hover {
    border-color: $rel-blue;
    color: $rel-blue;
  }
}

.rel-new-group-plus {
  font-weight: 700;
  font-size: 15px;
  line-height: 1;
}

.rel-nav-scroll {
  &--overflow {
    max-height: calc(44px * 10 + 4px * 9);
    overflow-y: auto;
    padding-right: 4px;
    margin-right: -2px;
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.22) transparent;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.16);
      border-radius: 6px;

      &:hover {
        background: rgba(0, 0, 0, 0.24);
      }
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }
}

.rel-nav-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.rel-nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 10px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  color: $rel-text;
  font-size: 14px;
  transition: background 0.15s, color 0.15s;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover:not(.vui_sidebar-item--active) {
    background: #f6f7f8;
  }

  /* 与「收藏」侧栏 .folder.active 一致 */
  &.vui_sidebar-item--active {
    background: #e8f7ff;
    color: #00a1d6;

    .rel-nav-icon {
      color: #00a1d6;
      opacity: 1;
    }

    .rel-nav-count {
      color: #00a1d6;
    }
  }
}

.rel-nav-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9499a0;

  svg {
    display: block;
  }
}

.rel-nav-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rel-nav-count {
  flex-shrink: 0;
  font-size: 12px;
  color: $rel-muted;
}

.rel-nav-item--action {
  padding-right: 28px;
}

.rel-nav-delete {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #999;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;

  .rel-nav-item:hover & {
    color: #e53935;
  }

  .rel-nav-item.vui_sidebar-item--active & {
    color: #999;

    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: #e53935;
    }
  }
}

/* —— 图2 右栏 —— */
.rel-main-col {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px 22px;
  min-width: 0;
  min-height: 520px;
  box-sizing: border-box;
}

.rel-main {
  min-width: 0;
}

.rel-main-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
}

.rel-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #18191c;
  line-height: 1.35;
  flex: 1;
  min-width: 0;
}

.rel-head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.batch-btn {
  border: 1px solid #e3e5e7;
  background: #fff;
  color: #61666d;
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;

  &:hover {
    border-color: $rel-blue;
    color: $rel-blue;
  }

  &--primary {
    border-color: rgba($rel-blue, 0.45);
    color: $rel-blue;
    font-weight: 500;

    &:hover {
      background: rgba($rel-blue, 0.06);
    }
  }

  &--danger {
    border-color: #f5c6c6;
    color: #e23c3c;

    &:hover {
      background: #fff5f5;
      border-color: #e23c3c;
    }
  }
}

.rel-batch-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
  margin: -8px 0 20px;
  padding: 12px 14px;
  background: #f7f9fa;
  border: 1px solid #e8eaed;
  border-radius: 10px;
}

.rel-checkbox-input {
  appearance: none;
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  margin: 0;
  flex-shrink: 0;
  border: 1px solid #c9ccd0;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  position: relative;
  transition:
    border-color 0.15s,
    background 0.15s,
    box-shadow 0.15s;
  vertical-align: middle;

  &:hover {
    border-color: #00a1d6;
  }

  &:checked {
    background: #e8f7ff;
    border-color: #00a1d6;

    &::after {
      content: '';
      position: absolute;
      left: 4px;
      top: 1px;
      width: 4px;
      height: 8px;
      border: solid #00a1d6;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg);
    }
  }

  &:focus-visible {
    outline: 2px solid rgba(0, 161, 214, 0.35);
    outline-offset: 1px;
  }
}

.rel-batch-check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #222;
  cursor: pointer;
  user-select: none;
}

.rel-batch-meta {
  font-size: 13px;
  color: $rel-muted;
}

.rel-batch-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.rel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 24px;

  &--dim {
    opacity: 0.55;
    pointer-events: none;
  }
}

.rel-sort-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rel-sort-pill {
  border: none;
  border-radius: 16px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  background: transparent;
  color: $rel-muted;
  transition: background 0.15s, color 0.15s;

  &.active {
    background: $rel-blue;
    color: #fff;
    font-weight: 500;
  }

  &:hover:not(.active) {
    color: #61666d;
  }
}

.rel-search {
  flex: 1;
  min-width: 200px;
  max-width: 320px;
}

.state {
  text-align: center;
  padding: 48px 16px;
  color: #222;
}

.state.muted {
  color: #999;
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;

  @media (max-width: 960px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.user-card {
  position: relative;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 14px 12px;
  padding-right: 100px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: box-shadow 0.15s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  &--batch {
    display: grid;
    grid-template-columns: 22px 1fr;
    gap: 8px 12px;
    align-items: start;
    padding-right: 12px;
  }
}

.user-card__batch-check {
  grid-column: 1;
  grid-row: 1;
  padding-top: 6px;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
}

.user-card--batch .card-body {
  grid-column: 2;
  grid-row: 1;
}

.card-body {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.card-inner {
  display: flex;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar-wrap--link {
  display: block;
  text-decoration: none;
  color: inherit;
  border-radius: 50%;
  outline: none;

  &:focus-visible {
    box-shadow: 0 0 0 2px rgba(0, 161, 214, 0.45);
  }
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  background: #f2f3f5;
}

.text-col {
  flex: 1;
  min-width: 0;
}

.uname {
  font-size: 15px;
  font-weight: 600;
  color: #222;
  margin-bottom: 6px;
}

.uname--link {
  text-decoration: none;
  color: #222;
  display: inline-block;
  max-width: 100%;
  outline: none;

  &:visited {
    color: #222;
  }

  &:hover {
    color: #00a1d6;
  }

  &:focus-visible {
    border-radius: 4px;
    box-shadow: 0 0 0 2px rgba(0, 161, 214, 0.35);
  }
}

.ubio {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  position: absolute;
  right: 12px;
  bottom: 14px;
  flex-shrink: 0;
  padding-left: 0;
}

.follow-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #e3e5e7;
  background: #f6f7f8;
  color: #61666d;
  font-size: 12px;
  cursor: pointer;

  &:hover:not(:disabled) {
    border-color: #00a1d6;
    color: #00a1d6;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.menu-dots {
  font-size: 14px;
  line-height: 1;
  opacity: 0.75;
}

/* 单用户操作菜单（Popover 内容随 teleport，此处保证样式命中） */
.rel-follow-menu {
  padding: 4px 0;
  min-width: 220px;
}

.rel-follow-menu__unfollow {
  width: calc(100% - 8px);
  margin: 0 4px 4px;
  border: none;
  background: #e8f4fc;
  color: #00a1d6;
  font-size: 14px;
  font-weight: 500;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  border-radius: 8px;

  &:hover {
    background: #dceef9;
  }
}

.rel-follow-menu__move {
  margin: 2px 0 4px;
}

.rel-follow-menu__move-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: none;
  background: #fff;
  color: #222;
  font-size: 14px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 8px;
  text-align: left;

  &:hover {
    background: #f6f7f8;
  }
}

.rel-follow-menu__chev {
  color: #999;
  font-size: 12px;
}

.rel-follow-menu__move-scroll {
  padding: 0 4px 6px;

  &--overflow {
    max-height: calc(36px * 10 + 8px);
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.22) transparent;

    &::-webkit-scrollbar {
      width: 5px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.16);
      border-radius: 4px;
    }
  }
}

.rel-follow-menu__move-item {
  display: block;
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  padding: 8px 10px;
  font-size: 13px;
  color: #222;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    background: #f6f7f8;
  }
}

.rel-follow-menu__divider {
  height: 1px;
  background: #eee;
  margin: 6px 8px;
}

.rel-follow-menu__footer {
  width: calc(100% - 8px);
  margin: 0 4px;
  border: none;
  background: #fff;
  color: #61666d;
  font-size: 13px;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  border-radius: 8px;

  &:hover {
    background: #f6f7f8;
  }
}
</style>

<style lang="scss">
/* Popover 挂载到 body */
.rel-follow-popper.el-popover.el-popper {
  padding: 6px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
</style>
