<template>
  <div 
    class="top-header" 
    :class="{ 
      scrolled: isScrolled || (!transparentAtTop && !fixedOnScroll),
      fixed: isFixed,
      'always-opaque': !transparentAtTop && !isScrolled
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
    <div class="search">
      <input
        class="search-input"
        v-model="searchText"
        placeholder="搜索你感兴趣的内容"
        @keyup.enter="doSearch"
      />
      <button class="search-btn" @click="doSearch">
        <!-- 🔍 -->
        <img src="/assets/search-button.png" class="search-btn-img"/>
      </button>
    </div>
    <div class="actions">
      <div 
        class="user-area" 
        @click="handleUserClick"
        @mouseenter="showUserDropdown = true"
        @mouseleave="handleUserAreaLeave"
        v-if="isAuthenticated"
      >
        <div class="avatar">
          <img v-if="userAvatar" :src="userAvatar" :alt="displayName" />
        </div>
        <span class="user-name">{{ displayName }}</span>
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
  // 在新标签页打开搜索结果，而不是当前页跳转
  const query = `?keyword=${encodeURIComponent(kw)}`
  openInNewTab(`/search${query}`)
}
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
  display: grid;
  grid-template-columns: 1fr 40px;
  background: #F1F2F3;
  border-radius: 8px;
  overflow: hidden;
  width: clamp(260px, 28vw, 420px);
  margin: 0 auto;
}

.search-input {
  height: 36px;
  padding: 0 12px;
  border: 0;
  outline: none;
  font-size: 14px;
  background: #F1F2F3;
}

.search-btn {
  margin-left: 4px;
  margin-top: 4px;
  border: 0;
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  padding: 8px;
  width: 25px;
  height: 25px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background-color: #f5f5f5;
}

.search-btn-img {
  width: 20px;
  height: 20px;
  margin-top: 4px;
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
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #d8d8d8;
  border: 2px solid rgba(255, 255, 255, .8);
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 99999;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    position: relative;
    z-index: 99999;
  }
}

.top-header.scrolled .avatar {
  border-color: #ddd;
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