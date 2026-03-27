<template>
  <div 
    class="top-header" 
    :class="{ 
      scrolled: isScrolled || (!transparentAtTop && !fixedOnScroll),
      fixed: isFixed,
      'always-opaque': !transparentAtTop && !isScrolled,
      'avatar-overlay-active': showUserDropdown
    }"
    :style="headerStyle"
  >
    <div class="header-inner">
    <div class="header-left">
      <!-- 独立 Logo：仅下滑（scrolled）时显示 -->
      <div class="brand-logo" v-show="isScrolled" @click="goTo('/')" aria-label="返回首页" role="button">
        <span class="brand-logo-icon" aria-hidden="true" v-html="webLogoSvg"></span>
      </div>

      <ul class="nav-left">
        <li class="nav-item" @click="goTo('/')">
          <img src="/assets/home.png" class="nav-icon nav-home-icon" />
          <span>首页</span>
        </li>
      <li class="nav-item"><span>番剧</span></li>
      <li class="nav-item" @click="goTo('/live')"><span>直播</span></li>
      <li class="nav-item"><span>游戏中心</span></li>
      <li class="nav-item" @click="goTo('/mall')"><span>会员购</span></li>
      <li class="nav-item"><span>漫画</span></li>
      <li class="nav-item"><span>赛事</span></li>
      <li class="nav-item">
        <img src="/assets/download-client.png" class="nav-icon" />
        <span>下载客户端</span>
      </li>
      </ul>
    </div>
    <div class="search" :class="{ 'is-typing': !!searchText.trim(), 'is-open': showSuggest }">
      <div class="search-input-row">
        <input
          class="search-input"
          v-model="searchText"
          placeholder="搜索你感兴趣的内容"
          @keyup.enter="doSearch"
          @focus="onSearchFocus"
          @input="onSearchInput"
          @blur="hideSuggestSoon"
        />
        <button
          v-show="showSuggest || !!searchText.trim()"
          class="search-clear-btn"
          :class="{ disabled: !searchText.trim() }"
          type="button"
          aria-label="清空搜索"
          @mousedown.prevent
          @click="clearSearchText"
        >
          ×
        </button>
        <button class="search-btn" @click="doSearch">
          <img src="/assets/search-button.png" class="search-btn-img"/>
        </button>
      </div>

      <div class="search-expand-panel" :class="{ visible: showSuggest }">
        <SearchSuggestPopover
          :history="searchHistory"
          :hot-keywords="hotKeywords"
          @select="onSelectSuggestion"
          @clear-history="clearHistory"
          @remove-history="removeHistoryItem"
        />
      </div>
    </div>
    <div class="actions">
      <div 
        class="user-area"
        :class="{ 'is-dropdown-open': showUserDropdown }"
        @click="handleUserClick"
        @mouseenter="showUserDropdown = true"
        @mouseleave="handleUserAreaLeave"
        v-if="isAuthenticated"
      >
        <div class="avatar">
          <img v-if="userAvatar" :src="userAvatar" :alt="displayName" />
        </div>
        <UserDropdown 
          v-model:visible="showUserDropdown"
          @close="showUserDropdown = false"
          @mouseenter="handleDropdownEnter"
        />
      </div>
      <div 
        class="user-area" 
        @click="handleUserClick"
        v-else
      >
        <div class="avatar" />
      </div>
      <div class="action-col" @click="goTo('/vip')">
        <img src="/assets/vip.png" class="action-icon" /><span>大会员</span>
      </div>
      <div class="action-col" @click="goTo('/messages')">
        <img src="/assets/messages.png" class="action-icon" /><span>消息</span>
      </div>
      <div class="action-col" @click="goTo('/feed')">
        <img src="/assets/feed.png" class="action-icon" /><span>动态</span>
      </div>
      <div class="action-col" @click="goTo('/profile')">
        <img src="/assets/favorites.png" class="action-icon" /><span>收藏</span>
      </div>
      <div class="action-col" @click="goTo('/history')">
        <img src="/assets/history.png" class="action-icon" /><span>历史</span>
      </div>
      <div class="action-col" @click="navigateToCreatorCenter">
        <img src="/assets/creator-center.png" class="action-icon" /><span>创作中心</span>
      </div>
      <button class="primary" @click="goTo('/submitHome?view=submit')">投稿</button>
    </div>
    </div>
  </div>
  <Login v-model:show="showLogin" @close="showLogin=false" />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Login from '@/components/Login.vue'
import UserDropdown from '@/components/UserDropdown.vue'
import { useUserStore } from '@/stores/user'
import { fetchMyProfile } from '@/api/userProfile'
import SearchSuggestPopover from '@/components/SearchSuggestPopover.vue'
import { fetchHotKeywords, recordSearchKeyword } from '@/api/search'
import webLogoRaw from '../../public/assets/webLogo.svg?raw'

// Props
interface Props {
  // 是否需要在下滑时固定在顶部的悬浮开关（默认 true）
  fixedOnScroll?: boolean
  // 页面最顶部时背景色是否需要透明（默认 true）
  transparentAtTop?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  fixedOnScroll: true,
  transparentAtTop: true
})

const router = useRouter()
const userStore = useUserStore()

const webLogoSvg = computed(() =>
  webLogoRaw
    .replace(/fill="#000"\b/gi, 'fill="currentColor"')
    .replace(/fill="#000000"\b/gi, 'fill="currentColor"')
    .replace(/<svg\b/i, '<svg style="width:100%;height:100%;display:block;"')
)

let showLogin = ref(false)
let showUserDropdown = ref(false)
let dropdownTimer: any = null
let isScrolled = ref(false)
let isFixed = ref(false)
let scrollTop = ref(0)

const isAuthenticated = computed(() => userStore.isAuthenticated)

// 搜索关键字
const searchText = ref('')

const showSuggest = ref(false)
const searchHistory = ref<string[]>([])
const hotKeywords = ref<Array<{ keyword: string; isNew: boolean }>>([])
let hotKeywordsLoadedAt = 0
const HOT_KEYWORDS_CACHE_MS = 60 * 1000

const storageKey = computed(() => {
  const id = user.value?.id || user.value?.userId
  return `search_history_${id || 'guest'}`
})

function loadHistory() {
  try {
    const raw = localStorage.getItem(storageKey.value)
    if (!raw) {
      searchHistory.value = []
      return
    }
    const list = JSON.parse(raw)
    if (!Array.isArray(list)) {
      searchHistory.value = []
      return
    }
    searchHistory.value = list.filter(x => typeof x === 'string' && x.trim()).slice(0, 10)
  } catch {
    searchHistory.value = []
  }
}

function persistHistory(next: string[]) {
  try {
    localStorage.setItem(storageKey.value, JSON.stringify(next))
  } catch {
    // ignore
  }
}

function pushHistory(keyword: string) {
  const kw = keyword.trim()
  if (!kw) return
  const next = [kw, ...searchHistory.value.filter(x => x !== kw)].slice(0, 10)
  searchHistory.value = next
  persistHistory(next)
}

const clearHistory = () => {
  searchHistory.value = []
  persistHistory([])
}

const removeHistoryItem = (keyword: string) => {
  const next = searchHistory.value.filter(x => x !== keyword)
  searchHistory.value = next
  persistHistory(next)
}

async function loadHotKeywordsIfNeeded(force = false) {
  const now = Date.now()
  if (!force && hotKeywords.value.length > 0 && now - hotKeywordsLoadedAt < HOT_KEYWORDS_CACHE_MS) return

  try {
    const resp = await fetchHotKeywords({ limit: 10 })
    const list = resp?.data?.list || resp?.data?.data?.list || []
    hotKeywords.value = Array.isArray(list)
      ? list.map(it => ({ keyword: it.keyword, isNew: !!it.isNew }))
      : []
    hotKeywordsLoadedAt = now
  } catch {
    // 接口未就绪：允许前端先可用
    hotKeywords.value = []
  }
}

let blurTimer: any = null

const onSearchFocus = () => {
  showSuggest.value = true
  loadHistory()
  void loadHotKeywordsIfNeeded()
}

const onSearchInput = () => {
  // 输入变化也保持下拉展示
  showSuggest.value = true
  void loadHotKeywordsIfNeeded()
}

const doSearchWithKeyword = (kw: string) => {
  const keyword = (kw || '').trim()
  if (!keyword) return

  // 记录搜索历史（本地）
  pushHistory(keyword)

  // 记录到后端热搜（允许失败）
  void recordSearchKeyword({ keyword }).catch(() => {})

  const query = `?keyword=${encodeURIComponent(keyword)}`
  openInNewTab(`/search${query}`)
}

const onSelectSuggestion = (kw: string) => {
  showSuggest.value = false
  searchText.value = kw
  doSearchWithKeyword(kw)
}

const clearSearchText = () => {
  if (!searchText.value.trim()) return
  searchText.value = ''
}

function hideSuggestSoon() {
  if (blurTimer) clearTimeout(blurTimer)
  blurTimer = setTimeout(() => {
    showSuggest.value = false
  }, 140)
}

function onDocClick(e: MouseEvent) {
  const target = e.target as HTMLElement | null
  if (!target) return
  const wrap = document.querySelector('.top-header .search')
  if (!wrap) return
  if (wrap.contains(target)) return
  showSuggest.value = false
}

// 规范化头像 URL
const normalizeAvatarUrl = (url: string) => {
  if (!url) return ''
  // 如果已经是完整 URL（http/https），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径（以 / 开头），直接返回
  if (url.startsWith('/')) {
    return url
  }
  // 其他情况，当作相对路径处理
  return '/' + url
}

// 用户信息（与 UserDropdown.vue 保持一致）
const user = computed(() => userStore.user || {})
const displayName = computed(() => {
  return user.value.username || user.value.loginAccount || '未登录'
})

const userAvatar = computed(() => {
  const avatar = user.value.avatar || user.value.avatarUrl || ''
  if (!avatar) return ''
  return normalizeAvatarUrl(avatar)
})

// 加载用户资料
const loadUserProfile = async () => {
  try {
    const response = await fetchMyProfile()
    if (response.data) {
      const profileData = response.data.success ? response.data : response.data
      if (profileData.avatar || profileData.id) {
        const normalizedAvatar = profileData.avatar ? normalizeAvatarUrl(profileData.avatar) : ''
        const currentUser = user.value || {}
        userStore.setUser({
          ...currentUser,
          id: profileData.id || currentUser.id,
          userId: profileData.id || currentUser.userId || currentUser.id,
          username: profileData.username || currentUser.username,
          loginAccount: currentUser.loginAccount || profileData.account,
          avatar: normalizedAvatar,
          bio: profileData.bio || currentUser.bio
        })
      }
    }
  } catch (error) {
    // 静默失败，不影响页面正常使用
    console.warn('加载用户资料失败:', error)
  }
}

const handleScroll = () => {
  // 如果不需要固定悬浮，设置固定状态并返回
  if (!props.fixedOnScroll) {
    isFixed.value = false
    // 如果背景不透明，应该显示深色文字和图标（通过 scrolled 类）
    isScrolled.value = !props.transparentAtTop
    return
  }

  // 优先检查 .scroll-container，如果没有则检查 window
  const scrollContainer = document.querySelector('.scroll-container') as HTMLElement
  let currentScrollTop = 0
  
  if (scrollContainer) {
    currentScrollTop = scrollContainer.scrollTop
  } else {
    currentScrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0
  }
  
  scrollTop.value = currentScrollTop
  
  // 设置滚动阈值：80px
  // 0-80px：顶部栏保持在页面内容顶部（使用 absolute 定位）
  // 超过 80px：顶部栏固定在视口顶部（使用 fixed 定位），显示白色背景（悬浮状态）
  const scrollThreshold = 80
  
  if (currentScrollTop <= scrollThreshold) {
    // 0-80px：保持在页面内容顶部
    isFixed.value = false
    // 根据 transparentAtTop 决定是否显示白色背景
    isScrolled.value = !props.transparentAtTop
  } else {
    // 超过 80px：固定在视口顶部
    isFixed.value = true
    isScrolled.value = true
  }
}

const headerStyle = computed(() => {
  // 如果不需要固定悬浮，始终使用 absolute 定位
  if (!props.fixedOnScroll) {
    return {
      position: 'absolute' as const,
      top: '0',
      left: '0',
      right: '0'
    }
  }

  if (isFixed.value) {
    // 超过80px：固定在视口顶部
    return {
      position: 'fixed' as const,
      top: '0',
      left: '0',
      right: '0'
    }
  } else {
    // 0-80px：保持在页面内容顶部，不跟随滚动
    return {
      position: 'absolute' as const,
      top: '0',
      left: '0',
      right: '0'
    }
  }
})

onMounted(() => {
  // 如果不需要固定悬浮，根据 transparentAtTop 设置初始状态
  if (!props.fixedOnScroll) {
    isFixed.value = false
    isScrolled.value = !props.transparentAtTop
  } else {
    // 初始检查一次
    handleScroll()
    
    // 监听滚动事件
    const scrollContainer = document.querySelector('.scroll-container')
    if (scrollContainer) {
      scrollContainer.addEventListener('scroll', handleScroll, { passive: true })
    } else {
      // 如果没有 .scroll-container，则监听 window
      window.addEventListener('scroll', handleScroll, { passive: true })
    }
  }
  
  // 如果用户已登录但没有头像，自动加载用户资料
  if (userStore.isAuthenticated) {
    const currentUser = user.value || {}
    if (!currentUser.avatar && !currentUser.avatarUrl) {
      loadUserProfile()
    }
  }
})

onUnmounted(() => {
  // 如果需要固定悬浮，才移除滚动事件监听
  if (props.fixedOnScroll) {
    const scrollContainer = document.querySelector('.scroll-container')
    if (scrollContainer) {
      scrollContainer.removeEventListener('scroll', handleScroll)
    } else {
      window.removeEventListener('scroll', handleScroll)
    }
  }
  
  if (dropdownTimer) {
    clearTimeout(dropdownTimer)
  }
})

const handleUserClick = () => {
  if (!isAuthenticated.value) {
    showLogin.value = true
    return
  }
  if (!showUserDropdown.value) {
    showUserDropdown.value = true
  }
}

const handleUserAreaLeave = () => {
  dropdownTimer = setTimeout(() => {
    showUserDropdown.value = false
  }, 200)
}

const handleDropdownEnter = () => {
  if (dropdownTimer) {
    clearTimeout(dropdownTimer)
    dropdownTimer = null
  }
}

watch(showUserDropdown, (val) => {
  if (val && dropdownTimer) {
    clearTimeout(dropdownTimer)
    dropdownTimer = null
  }
})

// 监听用户状态变化，确保头像正确更新
watch(() => userStore.isAuthenticated, (isAuth) => {
  if (isAuth) {
    const currentUser = user.value || {}
    if (!currentUser.avatar && !currentUser.avatarUrl) {
      loadUserProfile()
    }
  }
})

// 监听用户信息变化，确保头像正确更新
watch(() => user.value, (newUser) => {
  if (newUser && userStore.isAuthenticated) {
    if (!newUser.avatar && !newUser.avatarUrl) {
      loadUserProfile()
    }
  }
}, { deep: true })

const openInNewTab = (path: string) => {
  const base = (window as any).__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${normalizedBase}${normalizedPath || '/'}`
  window.open(url, '_blank')
}

const goTo = (path: string) => { 
  openInNewTab(path)
}

const navigateToCreatorCenter = () => {
  openInNewTab('/submitHome?view=contentManagement')
}

const doSearch = () => {
  const kw = searchText.value.trim()
  if (!kw) return

  // 记录搜索历史（本地）
  pushHistory(kw)

  // 记录到后端热搜（允许失败）
  void recordSearchKeyword({ keyword: kw }).catch(() => {})

  // 在新标签页打开搜索结果，而不是当前页跳转
  const query = `?keyword=${encodeURIComponent(kw)}`
  openInNewTab(`/search${query}`)
}

watch(() => user.value, () => {
  // 用户信息变更时，刷新历史
  loadHistory()
})

onMounted(() => {
  document.addEventListener('click', onDocClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
  if (blurTimer) clearTimeout(blurTimer)
})
</script>

<style lang="scss" scoped>
.top-header {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  height: 64px;
  z-index: 1000;
  background-color: transparent;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
  margin: 0 auto;
  // position 和 top 通过内联样式动态设置
  // 0-80px：使用 absolute 定位，保持在页面内容顶部
  // 超过 80px：使用 fixed 定位，固定在视口顶部
  // 宽度和居中逻辑参照 home.vue 中的 .home 样式设置
  
  &.scrolled {
    background-color: #ffffff !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  &.always-opaque {
    background-color: #ffffff !important;
  }

  &.avatar-overlay-active {
    z-index: 200000 !important;
  }
}

// 确保背景色优先级足够高
.top-header.scrolled {
  background-color: #ffffff !important;
  background: #ffffff !important;
}

.top-header.always-opaque {
  background-color: #ffffff !important;
  background: #ffffff !important;
}

.header-inner {
  position: relative;
  z-index: 1;
  height: 100%;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  box-sizing: border-box;
  max-width: 2300px;
  min-width: 1600px;
  width: 100%;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.brand-logo {
  width: 90px;
  height: 45px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  // margin-right: 12px;
  margin-top: -6px;
  cursor: pointer;
  color: #00a1d6;
  transition: opacity 0.2s ease;

  &:active {
    opacity: 0.8;
  }
}

.brand-logo-icon {
  width: 100%;
  height: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  :deep(svg) {
    width: 100%;
    height: 100%;
    display: block;
  }

  :deep(path) {
    fill: currentColor !important;
  }
}

.nav-left {
  display: flex;
  gap: 20px;
  list-style: none;
  padding: 0;
  margin-left: 1vw;
  align-items: center;
}

.nav-item {
  display: flex;
  align-items: center;
  color: #fff;
  font-size: 14px;
  gap: 6px;
  cursor: pointer;

  span {
    transition: color .2s;
    white-space: nowrap;
  }

  &:hover span {
    color: #00a1d6;
    animation: jump 0.3s ease;
  }
}

.top-header.scrolled .nav-item {
  color: #333;
}

.nav-icon {
  width: 18px;
  height: 18px;
  // 透明背景时：将图标变成白色（如果原始是深色）或保持白色（如果原始是白色）
  filter: brightness(0) invert(1);
}

.top-header.scrolled .nav-icon {
  // 白色背景时：将图标变成深色（黑色）
  filter: brightness(0);
}

/* “首页”图标：仅最顶部显示；下滑时隐藏 */
.top-header.scrolled .nav-home-icon {
  display: none;
}

.search {
  display: flex;
  flex-direction: column;
  background: #F1F2F3;
  border: 0;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
  max-width: 560px;
  min-width: 260px;
  margin: 0 auto;
  margin-top: 12px;
  align-self: start;
  position: relative;
  box-sizing: border-box;
  padding: 4px;
  transition: border-radius 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}

.top-header.scrolled .search {
  /* 下滑悬浮状态：略缩短搜索框，给头像放大预留空间 */
  max-width: 520px;
}

.search-input-row {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 32px;
}

.search.is-typing,
.search:focus-within {
  background: #ffffff;
}

.search-input {
  height: 32px;
  padding: 0 38px 0 12px;
  border: 0;
  outline: none;
  font-size: 14px;
  background: #F1F2F3;
  color: #111827;
  border-radius: 8px;
  width: 93%;
  box-sizing: border-box;
  transition: background-color 0.2s ease;
}

.search-clear-btn {
  position: absolute;
  right: 32px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: #c7cbd1;
  color: #ffffff;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin-right: 13px;
}

.search-clear-btn:hover {
  background: #c7cbd1;
  color: #ffffff;
}

.search-clear-btn.disabled {
  opacity: 0.65;
  cursor: default;
}

/* 输入时背景色变深一点 */
.search.is-typing .search-input,
.search:focus-within .search-input {
  background: #e3e5e7;
}

.search.is-open {
  border-radius: 8px;
  box-shadow: 0 0 30px rgba(0, 0, 0, .1);
}

.search-expand-panel {
  max-height: 0;
  opacity: 0;
  transition: max-height 0.2s ease, opacity 0.2s ease;
}

.search-expand-panel.visible {
  max-height: 1000px;
  opacity: 1;
}

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  margin: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  padding: 8px;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background: transparent;
}

.search-btn-img {
  width: 20px;
  height: 20px;
  margin-top: 0;
  // 搜索按钮图标在搜索框内，搜索框本身是白色背景，所以图标应该是深色
  filter: brightness(0);
}

.actions {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-right: 1vw;
}

.user-area {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
  overflow: visible;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: transparent;
  flex-shrink: 0;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  /* 只移动头像视觉位置：不影响 header grid 布局，从而保证搜索框长度不被拉长/缩短 */
  transform: translateX(10px);
  /* 头像需要永远压在下拉弹窗之上，因此使用一个远高于下拉层的全局 z-index */
  z-index: 999990;
  
  img {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    object-fit: cover;
    position: absolute;
    top: 0;
    right: 0;
    z-index: 999991;
    transition: width 0.2s ease, height 0.2s ease, box-shadow 0.2s ease;
  }

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 38px;
    height: 38px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, .8);
    box-sizing: border-box;
    pointer-events: none;
    z-index: 999992;
    transition: width 0.2s ease, height 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  }
}

.top-header.scrolled .avatar {
  /* 下滑悬浮状态：头像整体再向右移动一些，避免放大后遮挡搜索框右侧 */
  transform: translateX(13x);
}

.user-area.is-dropdown-open .avatar,
.user-area:hover .avatar {
  z-index: 100100;
  /* 下拉/放大时：头像向左收一点，避免贴近右侧图标 */
  transform: translateX(4px);
}

.top-header.scrolled .user-area.is-dropdown-open .avatar,
.top-header.scrolled .user-area:hover .avatar {
  /* 下滑悬浮 + 下拉/放大时：同样向左收一点，别贴近右侧图标 */
  transform: translateX(12px);
}

.user-area.is-dropdown-open .avatar img,
.user-area:hover .avatar img {
  width: 82px;
  height: 82px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
}

.user-area.is-dropdown-open .avatar::after,
.user-area:hover .avatar::after {
  width: 82px;
  height: 82px;
}

.top-header.scrolled .avatar {
  &::after {
    border-color: #ddd;
  }
}

.user-name {
  font-size: 13px;
  color: #fff;
  white-space: nowrap;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top-header.scrolled .user-name {
  color: #333;
}

.action-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
  gap: 4px;
  font-size: 12px;
}

.action-col span {
  transition: color .2s;
}

.action-col:hover span {
  color: #00a1d6;
  animation: jump 0.3s ease;
}

.action-col:hover .action-icon {
  animation: jump 0.3s ease;
}

.top-header.scrolled .action-col {
  color: #333;
}

.action-icon {
  width: 19px;
  height: 19px;
  // 透明背景时：将图标变成白色
  filter: brightness(0) invert(1) drop-shadow(0 0 0.3px rgba(255, 255, 255, 0.85));
}

.top-header.scrolled .action-icon {
  // 白色背景时：将图标变成深色（黑色），移除白色阴影
  filter: brightness(0);
}

.primary {
  background: #fb7299;
  border: none;
  color: #fff;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}

@keyframes jump {
  0% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
  100% { transform: translateY(0); }
}
</style>