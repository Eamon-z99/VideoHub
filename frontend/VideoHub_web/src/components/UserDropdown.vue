<template>
  <Teleport to="body">
    <div 
      v-if="visible" 
      ref="wrapperRef"
      class="user-dropdown-wrapper"
      :style="dropdownStyle"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
    >
      <!-- 不可见的连接区域，填充用户区域和下拉菜单之间的间隙 -->
      <div class="dropdown-bridge"></div>
      <!-- 阴影放在外层：带 mask 的内层会裁掉 box-shadow -->
      <div class="user-dropdown-shell">
      <div class="user-dropdown">
        <!-- 用户信息头部 -->
        <div class="user-header">
          <div class="user-name">{{ username }}</div>
          <div class="user-coins">
            <span>硬币: {{ coins }}</span>
          </div>
          
          <!-- 等级进度条 -->
          <div class="level-progress">
            <div class="level-info">
              <img
                class="level-icon"
                :src="currentLevelIconUrlWithCache"
                :alt="`LV${currentLevel}`"
              />
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <img
                class="level-icon"
                :src="nextLevelIconUrlWithCache"
                :alt="`LV${nextLevelForIcon}`"
              />
            </div>
            <div class="level-text">
              <template v-if="isMaxLevel">
                当前成长{{ currentExp }}，已达最高等级
              </template>
              <template v-else>
                当前成长{{ currentExp }}，距离升级Lv.{{ nextLevel }} 还需要{{ needExp }}
              </template>
            </div>
          </div>
          
          <!-- 统计数据 -->
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ following }}</div>
              <div class="stat-label">关注</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ fans }}</div>
              <div class="stat-label">粉丝</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ dynamics }}</div>
              <div class="stat-label">动态</div>
            </div>
          </div>
        </div>
        
        <!-- 会员推广 -->
        <div class="member-promotion">
          <div class="promotion-text">
            <div class="promotion-title">成为大会员</div>
            <div class="promotion-subtitle">热播内容看不停</div>
          </div>
          <button class="member-btn" @click="goToVip">会员中心</button>
        </div>
        
        <!-- 导航菜单 -->
        <div class="menu-list">
          <div class="menu-item" @click="goToProfile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item" @click="goToSubmit">
            <el-icon><VideoCamera /></el-icon>
            <span>投稿管理</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
          <div
            ref="recommendItemRef"
            class="menu-item recommend-item"
            @mouseenter="openRecommendPopover"
            @mouseleave="closeRecommendPopoverSoon"
            @click="toggleRecommendPopover"
          >
            <el-icon><Star /></el-icon>
            <span>推荐服务</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
          
          <div class="menu-divider"></div>
          
          <div class="menu-item" @click="toggleTheme">
            <el-icon><Moon /></el-icon>
            <span>主题: {{ themeText }}</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
          <div class="menu-item logout" @click="handleLogout">
            <el-icon><RefreshLeft /></el-icon>
            <span>退出登录</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      </div>
      <!-- 不可见的底部连接区域，防止鼠标移动到下方时消失 -->
      <div class="dropdown-bridge-bottom"></div>

      <!-- 推荐服务：右侧小弹窗（放到 .user-dropdown 外，避免被 overflow: hidden 裁剪） -->
      <div
        v-show="showRecommendPopover"
        class="recommend-popover"
        :style="recommendPopoverStyle"
        @mouseenter="openRecommendPopover"
        @mouseleave="closeRecommendPopoverSoon"
      >
        <div class="recommend-popover-item" @click.stop="goToDynamicCenter">
          <span class="recommend-text">动态中心</span>
        </div>
        <div class="recommend-popover-item" @click.stop="goToHistoryPage">
          <span class="recommend-text">历史页面</span>
        </div>
        <div class="recommend-popover-item" @click.stop="goToCreatorCenter">
          <span class="recommend-text">投稿中心</span>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, VideoCamera, Star, Moon, RefreshLeft, ArrowRight } from '@element-plus/icons-vue'
import { getUserStats } from '@/api/follow'
import { getCoinBalance } from '@/api/coin'
import { fetchFeeds } from '@/api/feed'
import { getUserLevelProgress } from '@/api/userLevel'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'update:visible', 'mouseenter'])

const router = useRouter()
const userStore = useUserStore()

const wrapperRef = ref(null)
const BASE_AVATAR_SIZE = 32
const EXPANDED_AVATAR_SIZE = 82
const DROPDOWN_BRIDGE_HEIGHT = 8

// 用户信息
const user = computed(() => userStore.user || {})
const username = computed(() => user.value.username || user.value.loginAccount || '未登录')

const coins = ref(0)
const currentLevel = ref(0)
const nextLevel = ref(1)
const currentExp = ref(0)
const needExp = ref(50)
const following = ref(0)
const fans = ref(0)
const dynamics = ref(0)

const progressPercent = computed(() => {
  // 已达到最高等级时：needExp=0，此时进度条应展示为满格
  if (needExp.value === 0) return 100
  const total = currentExp.value + needExp.value
  return total > 0 ? (currentExp.value / total) * 100 : 0
})

const isMaxLevel = computed(() => toNumber(nextLevel.value) === toNumber(currentLevel.value) || toNumber(needExp.value) === 0)

const clampLevel = (lv) => {
  const n = toNumber(lv)
  if (n < 0) return 0
  if (n > 6) return 6
  return n
}

// 右侧固定展示“下一个等级”的图标（未达到）
const nextLevelForIcon = computed(() => {
  const lv = clampLevel(currentLevel.value)
  if (lv >= 6) return 6
  return lv + 1
})

const currentLevelIconUrl = computed(() => `/assets/level_${clampLevel(currentLevel.value)}.svg`)
const nextLevelIconUrl = computed(() => `/assets/level_${nextLevelForIcon.value}.svg`)

// 带一个轻量 cache bust，避免浏览器缓存导致不刷新
const currentLevelIconUrlWithCache = computed(() => `${currentLevelIconUrl.value}?v=${clampLevel(currentLevel.value)}`)
const nextLevelIconUrlWithCache = computed(() => `${nextLevelIconUrl.value}?v=${nextLevelForIcon.value}`)

const themeText = ref('浅色')

let leaveTimer = null
const dropdownStyle = ref({})
let loadingStats = false

const showRecommendPopover = ref(false)
let recommendTimer = null
const recommendItemRef = ref(null)
const recommendPopoverStyle = ref({})

const openRecommendPopover = () => {
  if (recommendTimer) {
    clearTimeout(recommendTimer)
    recommendTimer = null
  }
  showRecommendPopover.value = true
  void updateRecommendPopoverPosition()
}

const closeRecommendPopoverSoon = () => {
  if (recommendTimer) clearTimeout(recommendTimer)
  recommendTimer = setTimeout(() => {
    showRecommendPopover.value = false
  }, 120)
}

const toggleRecommendPopover = () => {
  if (showRecommendPopover.value) {
    showRecommendPopover.value = false
    return
  }
  openRecommendPopover()
}

const updateRecommendPopoverPosition = async () => {
  await nextTick()
  const el = recommendItemRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const popoverEl = wrapperRef.value?.querySelector('.recommend-popover')
  const popoverWidth = popoverEl?.getBoundingClientRect?.().width || 140
  const desiredLeft = rect.right + 12
  const maxLeft = Math.max(8, window.innerWidth - popoverWidth - 8)
  const left = Math.min(desiredLeft, maxLeft)
  const top = rect.top + rect.height / 2
  recommendPopoverStyle.value = {
    position: 'fixed',
    left: `${left}px`,
    top: `${top}px`,
    transform: 'translateY(-50%)'
  }
}

const toNumber = (value) => {
  if (value === null || value === undefined || value === '') return 0
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

const loadRealtimeStats = async () => {
  const userId = user.value?.id || user.value?.userId
  if (!userId || loadingStats) return

  loadingStats = true
  try {
    const [coinResp, followResp, feedResp, levelResp] = await Promise.all([
      getCoinBalance(),
      getUserStats(userId),
      fetchFeeds(1, 1, userId, true, userId),
      getUserLevelProgress()
    ])

    const coinData = coinResp?.data || {}
    if (coinData.success) {
      coins.value = toNumber(coinData.coinBalance)
    }

    const followData = followResp?.data || {}
    if (followData.success) {
      const stats = followData.stats || {}
      following.value = toNumber(stats.followingCount)
      fans.value = toNumber(stats.followerCount)
    }

    const feedData = feedResp?.data || {}
    dynamics.value = toNumber(feedData.total)

    const levelData = levelResp?.data || {}
    if (levelResp?.success === false) {
      // 接口返回结构异常：不影响其他统计展示
    } else if (levelData.success) {
      currentLevel.value = toNumber(levelData.level)
      nextLevel.value = toNumber(levelData.nextLevel)
      currentExp.value = toNumber(levelData.currentExp)
      needExp.value = toNumber(levelData.needExp)
    } else if (levelResp?.data?.level !== undefined) {
      // 兼容：后端如未包裹 success
      currentLevel.value = toNumber(levelResp.data.level)
      nextLevel.value = toNumber(levelResp.data.nextLevel)
      currentExp.value = toNumber(levelResp.data.currentExp)
      needExp.value = toNumber(levelResp.data.needExp)
    }
  } catch (error) {
    // 保持静默，避免影响下拉显示
    console.warn('加载用户下拉统计失败:', error)
  } finally {
    loadingStats = false
  }
}

// 计算下拉菜单的位置
const updatePosition = async () => {
  // 等待多个 tick 确保组件完全渲染
  await nextTick()
  await nextTick()
  
  // 如果 wrapperRef 还没有准备好，再等待一下
  if (!wrapperRef.value) {
    await new Promise(resolve => setTimeout(resolve, 10))
    if (!wrapperRef.value) {
      // 如果还是没准备好，尝试再次等待
      await nextTick()
      if (!wrapperRef.value) return
    }
  }
  
  // 查找所有 .user-area 元素，选择已登录的那个（包含头像图片）
  const userAreas = document.querySelectorAll('.user-area')
  let userArea = null
  
  // 优先选择包含头像图片的区域（已登录用户区域）
  for (const area of userAreas) {
    const avatarImg = area.querySelector('.avatar img')
    if (avatarImg) {
      userArea = area
      break
    }
  }
  
  // 如果没找到，使用第一个
  if (!userArea && userAreas.length > 0) {
    userArea = userAreas[0]
  }
  
  if (!userArea) {
    console.warn('UserDropdown: 未找到 .user-area 元素')
    return
  }
  
  const rect = userArea.getBoundingClientRect()
  const avatarEl = userArea.querySelector('.avatar')
  const avatarImg = userArea.querySelector('.avatar img')
  const avatarElRect = avatarEl ? avatarEl.getBoundingClientRect() : null
  const avatarRect = avatarImg ? avatarImg.getBoundingClientRect() : null

  // 目标效果：头像“下半部分”压在弹窗上边中间。
  // 头像从右上角锚定向左下放大，基于 .avatar 的右边和上边计算放大后的中心更稳定，
  // 避免在 32->82 过渡过程中发生偏移。
  const anchorCenterX = avatarElRect
    ? avatarElRect.right - EXPANDED_AVATAR_SIZE / 2
    : (avatarRect ? avatarRect.left + EXPANDED_AVATAR_SIZE / 2 : rect.left + rect.width / 2)
  const anchorCenterY = avatarElRect
    ? avatarElRect.top + EXPANDED_AVATAR_SIZE / 2
    : (avatarRect ? avatarRect.top + EXPANDED_AVATAR_SIZE / 2 : rect.top + EXPANDED_AVATAR_SIZE / 2)

  const dropdownWidth = 320
  const desiredLeft = anchorCenterX - dropdownWidth / 2
  const minLeft = 8
  const maxLeft = Math.max(minLeft, window.innerWidth - dropdownWidth - 8)
  const left = Math.min(Math.max(desiredLeft, minLeft), maxLeft)
  dropdownStyle.value = {
    // wrapper 顶部还有 bridge 高度，因此减去 bridge，让卡片上边正好过头像中心线
    top: `${anchorCenterY - DROPDOWN_BRIDGE_HEIGHT}px`,
    left: `${left}px`
  }
}

// 监听 visible 变化，更新位置
watch(() => props.visible, async (val) => {
  if (val) {
    // 确保组件完全渲染后再更新位置
    await nextTick()
    updatePosition()
    loadRealtimeStats()
    // 监听窗口滚动和大小变化
    window.addEventListener('scroll', updatePosition, true)
    window.addEventListener('resize', updatePosition)
    window.addEventListener('scroll', updateRecommendPopoverPosition, true)
    window.addEventListener('resize', updateRecommendPopoverPosition)
  } else {
    window.removeEventListener('scroll', updatePosition, true)
    window.removeEventListener('resize', updatePosition)
    window.removeEventListener('scroll', updateRecommendPopoverPosition, true)
    window.removeEventListener('resize', updateRecommendPopoverPosition)
    // 关闭时清理定时器
    if (leaveTimer) {
      clearTimeout(leaveTimer)
      leaveTimer = null
    }
    if (recommendTimer) {
      clearTimeout(recommendTimer)
      recommendTimer = null
    }
    showRecommendPopover.value = false
  }
})

// 组件挂载后，如果可见则更新位置
onMounted(() => {
  if (props.visible) {
    updatePosition()
    loadRealtimeStats()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', updatePosition, true)
  window.removeEventListener('resize', updatePosition)
  window.removeEventListener('scroll', updateRecommendPopoverPosition, true)
  window.removeEventListener('resize', updateRecommendPopoverPosition)
  // 清理定时器
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
  if (recommendTimer) {
    clearTimeout(recommendTimer)
    recommendTimer = null
  }
})

const handleMouseEnter = () => {
  // 鼠标进入下拉菜单，取消关闭操作
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
  emit('update:visible', true)
  emit('mouseenter') // 通知父组件取消关闭操作
}

const handleMouseLeave = (e) => {
  // 清除之前的定时器
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
  
  if (!wrapperRef.value) {
    // 如果组件已经卸载，直接关闭
    emit('close')
    emit('update:visible', false)
    return
  }
  
  // 检查 relatedTarget 是否在 wrapper 内部
  const relatedTarget = e.relatedTarget
  
  // 如果 relatedTarget 存在且在 wrapper 内部，说明鼠标只是移动到了组件内的另一个元素
  if (relatedTarget && wrapperRef.value.contains(relatedTarget)) {
    // 鼠标移动到了下拉菜单内部的另一个元素，不关闭
    return
  }

  // 如果 relatedTarget 在头像所在的 .user-area 内部，认为是“从弹窗回到头像”，不关闭
  if (relatedTarget) {
    const userAreas = document.querySelectorAll('.top-header .user-area')
    for (const area of userAreas) {
      if (area.contains(relatedTarget)) {
        return
      }
    }
  }
  
  // 如果 relatedTarget 为 null 或不在 wrapper 内，说明鼠标真的离开了组件
  // 延迟关闭，给鼠标时间移动到组件内的其他元素（处理快速移动的情况）
  leaveTimer = setTimeout(() => {
    // 延迟后再次检查，确保鼠标真的离开了
    if (!wrapperRef.value) {
      emit('close')
      emit('update:visible', false)
      return
    }
    
    // 最终确认：如果延迟期间没有 mouseenter 事件，说明鼠标真的离开了
    emit('close')
    emit('update:visible', false)
  }, 100) // 减少延迟时间，让关闭更及时
}

const openInNewTab = (path) => {
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${normalizedBase}${normalizedPath || '/'}`
  window.open(url, '_blank')
}

const goToProfile = () => {
  openInNewTab('/profile')
  emit('close')
  emit('update:visible', false)
}

const goToSubmit = () => {
  openInNewTab('/submitHome')
  emit('close')
  emit('update:visible', false)
}

const goToRecommend = () => {
  // 推荐服务页面（如果存在）
  emit('close')
  emit('update:visible', false)
}

const goToDynamicCenter = () => {
  openInNewTab('/feed')
  showRecommendPopover.value = false
  emit('close')
  emit('update:visible', false)
}

const goToHistoryPage = () => {
  openInNewTab('/history')
  showRecommendPopover.value = false
  emit('close')
  emit('update:visible', false)
}

const goToCreatorCenter = () => {
  openInNewTab('/submitHome?view=contentManagement')
  showRecommendPopover.value = false
  emit('close')
  emit('update:visible', false)
}

const goToVip = () => {
  openInNewTab('/vip')
  emit('close')
  emit('update:visible', false)
}

const toggleTheme = () => {
  themeText.value = themeText.value === '浅色' ? '深色' : '浅色'
  // TODO: 实现主题切换逻辑
  ElMessage.info('主题切换功能开发中')
}

const handleLogout = async () => {
  try {
    // 调用退出登录
    await userStore.logout()
    ElMessage.success('已退出登录')
    // 关闭下拉菜单
    emit('close')
    emit('update:visible', false)
    // 跳转到首页
    router.push('/')
  } catch (error) {
    // 即使退出失败，也清除本地存储并跳转
    console.error('退出登录失败:', error)
    ElMessage.warning('退出登录时出现错误，但已清除本地登录信息')
    emit('close')
    emit('update:visible', false)
    router.push('/')
  }
}
</script>

<style lang="scss" scoped>
.user-dropdown-wrapper {
  position: fixed; /* 使用 fixed 定位，确保不受父元素影响 */
  z-index: 10; /* 由头像负责在更高层级上方展示 */
  /* 确保宽度足够覆盖用户区域 */
  min-width: 320px;
  /* 确保整个区域都能捕获鼠标事件 */
  pointer-events: auto;
  /* 确保整个容器区域都能响应鼠标事件 */
  display: flex;
  flex-direction: column;
}

.dropdown-bridge {
  height: 8px;
  width: 100%;
  /* 不可见但可以捕获鼠标事件 */
  background: transparent;
  /* 确保连接区域足够宽，能够覆盖用户区域 */
  min-width: 320px;
  /* 确保能够捕获鼠标事件 */
  pointer-events: auto;
  /* 防止收缩 */
  flex-shrink: 0;
}

.dropdown-bridge-bottom {
  height: 30px; /* 增加底部连接区域高度，防止鼠标移动到下方时消失 */
  width: 100%;
  /* 不可见但可以捕获鼠标事件 */
  background: transparent;
  /* 确保连接区域足够宽 */
  min-width: 320px;
  /* 确保能够捕获鼠标事件 */
  pointer-events: auto;
  /* 防止收缩 */
  flex-shrink: 0;
}

/* 无 mask，专门承载阴影，避免被内层 mask 吃掉 */
.user-dropdown-shell {
  width: 320px;
  flex-shrink: 0;
  border-radius: 8px;
  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.06),
    0 8px 24px rgba(0, 0, 0, 0.12),
    0 16px 48px rgba(0, 0, 0, 0.14);
  pointer-events: auto;
}

.user-dropdown {
  width: 320px;
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  /* 确保整个下拉菜单区域都能捕获鼠标事件 */
  pointer-events: auto;

  /* 顶部中间挖半圆缺口：让下方头像能透出来 */
  /* radius=40 => 80px 直径；圆心在元素顶部边界 => 正好是半圆缺口 */
  -webkit-mask-image: radial-gradient(circle 40px at 50% 0, transparent 38px, #000 40px);
  mask-image: radial-gradient(circle 40px at 50% 0, transparent 38px, #000 40px);
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.user-header {
    padding: 20px;
    padding-top: 50px;
    padding-bottom: 20px; /* 确保底部有足够的 padding */
    text-align: center;
    background: #ffffff;
    /* 确保整个区域都能捕获鼠标事件 */
    min-height: 100%;
    
    .user-name {
      font-size: 18px;
      font-weight: 600;
      color: #18191c;
      margin-bottom: 8px;
    }
    
    .user-coins {
      display: flex;
      justify-content: center;
      gap: 16px;
      font-size: 13px;
      color: #61666d;
      margin-bottom: 16px;
    }
    
    .level-progress {
      margin-bottom: 16px;
      
      .level-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: #61666d;
        margin-bottom: 6px;

        .level-icon {
          width: 60px;
          height: 32px;
          flex: 0 0 40px;
          display: inline-block;
          object-fit: contain;
        }

        .progress-bar {
          flex: 1;
          margin-left: 3px;
          margin-right: 6px;
          width: auto;
          height: 2px;
          background: #e3e5e7;
          border-radius: 1px;
          overflow: hidden;

          .progress-fill {
            height: 100%;
            background: #f3cb85;
            border-radius: 1px;
            transition: width 0.3s ease;
          }
        }
      }
      
      .level-text {
        font-size: 11px;
        color: #8a8a8a;
        line-height: 1.4;
      }
    }
    
    .user-stats {
      display: flex;
      justify-content: space-between;
      gap: 0;
      margin-top: 6px;
      padding: 0 6px;
      color: #61666d;

      .stat-item {
        flex: 1;
        text-align: center;
        cursor: pointer;
        padding: 6px 0;
        border-radius: 8px;
        transition: background 0.15s ease, color 0.2s ease;

        // &:hover {
        //   color: #00a1d6;
        //   background: rgba(245, 247, 250, 0.9);
        // }
      }

      .stat-value {
        font-size: 18px;
        line-height: 1.1;
        font-weight: 500;
        color: #18191c;
      }

      .stat-label {
        margin-top: 6px;
        font-size: 13px;
        color: #61666d;
      }
    }
  }

.member-promotion {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: #fff5f7;
    margin: 0 12px;
    border-radius: 6px;
    margin-bottom: 8px;
    
    .promotion-text {
      flex: 1;
      
      .promotion-title {
        font-size: 14px;
        font-weight: 600;
        color: #18191c;
        margin-bottom: 2px;
      }
      
      .promotion-subtitle {
        font-size: 12px;
        color: #61666d;
      }
    }
    
    .member-btn {
      padding: 6px 16px;
      background: #fb7299;
      color: #fff;
      border: none;
      border-radius: 4px;
      font-size: 13px;
      cursor: pointer;
      transition: background 0.2s;
      
      &:hover {
        background: #ff85b0;
      }
    }
  }

.menu-list {
    padding: 8px 0;
    
    .menu-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      cursor: pointer;
      transition: background 0.2s;
      position: relative;
      
      &:hover {
        background: #f5f7fa;
      }
      
      &.logout {
        color: #f56c6c;
        
        &:hover {
          background: #fef0f0;
        }
      }
      
      .el-icon {
        width: 18px;
        height: 18px;
        margin-right: 12px;
        color: #61666d;
      }
      
      span {
        flex: 1;
        font-size: 14px;
        color: #18191c;
      }
      
      .arrow {
        width: 14px;
        height: 14px;
        margin-right: 0;
        margin-left: auto;
        color: #c0c4cc;
      }
    }
    
    .menu-divider {
      height: 1px;
      background: #e3e5e7;
      margin: 8px 0;
    }
  }

.recommend-item {
  user-select: none;
}

.recommend-popover {
  width: max-content;
  min-width: 0;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
  border: 1px solid rgba(227, 229, 231, 0.9);
  z-index: 60;
}

.recommend-popover-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  color: #18191c;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s ease;
  white-space: nowrap;
}

.recommend-popover-item:hover {
  background: #f5f7fa;
}

.recommend-text {
  display: inline-block;
}
</style>

