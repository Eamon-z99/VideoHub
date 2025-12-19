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
      <!-- 下拉菜单 -->
      <div class="user-dropdown">
        <!-- 用户信息头部 -->
        <div class="user-header">
          <div class="user-avatar">
            <img :src="userAvatar" :alt="username" />
          </div>
          <div class="user-name">{{ username }}</div>
          <div class="user-coins">
            <span>硬币: {{ coins }}</span>
            <span>B币: {{ bCoins }}</span>
          </div>
          
          <!-- 等级进度条 -->
          <div class="level-progress">
            <div class="level-info">
              <span class="level-label">LVS</span>
              <span class="level-value">LV{{ currentLevel }}</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <div class="level-text">
              当前成长{{ currentExp }},距离升级Lv.{{ nextLevel }} 还需要{{ needExp }}
            </div>
          </div>
          
          <!-- 统计数据 -->
          <div class="user-stats">
            <span>{{ following }} 关注</span>
            <span>{{ fans }} 粉丝</span>
            <span>{{ dynamics }} 动态</span>
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
          <div class="menu-item" @click="goToRecommend">
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
      <!-- 不可见的底部连接区域，防止鼠标移动到下方时消失 -->
      <div class="dropdown-bridge-bottom"></div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, VideoCamera, Star, Moon, RefreshLeft, ArrowRight } from '@element-plus/icons-vue'

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

// 用户信息
const user = computed(() => userStore.user || {})
const username = computed(() => user.value.username || user.value.loginAccount || '未登录')
const userAvatar = computed(() => user.value.avatar || 'https://placehold.co/80x80')

// 虚拟数据（后续可以从后端获取）
const coins = ref(1206)
const bCoins = ref(0)
const currentLevel = ref(5)
const nextLevel = ref(6)
const currentExp = ref(12690)
const needExp = ref(16110)
const following = ref(25)
const fans = ref(3)
const dynamics = ref(0)

const progressPercent = computed(() => {
  const total = currentExp.value + needExp.value
  return total > 0 ? (currentExp.value / total) * 100 : 0
})

const themeText = ref('浅色')

let leaveTimer = null
const dropdownStyle = ref({})

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
  
  // 查找所有 .user-area 元素，选择已登录的那个（包含用户名和下拉菜单的）
  const userAreas = document.querySelectorAll('.user-area')
  let userArea = null
  
  // 优先选择包含用户名的区域（已登录用户区域）
  for (const area of userAreas) {
    // 检查这个区域是否包含用户名（已登录用户区域）
    const userName = area.querySelector('.user-name')
    if (userName) {
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
  dropdownStyle.value = {
    top: `${rect.bottom + 8}px`,
    right: `${window.innerWidth - rect.right}px`
  }
}

// 监听 visible 变化，更新位置
watch(() => props.visible, async (val) => {
  if (val) {
    // 确保组件完全渲染后再更新位置
    await nextTick()
    updatePosition()
    // 监听窗口滚动和大小变化
    window.addEventListener('scroll', updatePosition, true)
    window.addEventListener('resize', updatePosition)
  } else {
    window.removeEventListener('scroll', updatePosition, true)
    window.removeEventListener('resize', updatePosition)
    // 关闭时清理定时器
    if (leaveTimer) {
      clearTimeout(leaveTimer)
      leaveTimer = null
    }
  }
})

// 组件挂载后，如果可见则更新位置
onMounted(() => {
  if (props.visible) {
    updatePosition()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', updatePosition, true)
  window.removeEventListener('resize', updatePosition)
  // 清理定时器
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
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

const goToProfile = () => {
  router.push('/profile')
  emit('close')
  emit('update:visible', false)
}

const goToSubmit = () => {
  router.push('/submitHome')
  emit('close')
  emit('update:visible', false)
}

const goToRecommend = () => {
  // 推荐服务页面（如果存在）
  emit('close')
  emit('update:visible', false)
}

const goToVip = () => {
  router.push('/vip')
  emit('close')
  emit('update:visible', false)
}

const toggleTheme = () => {
  themeText.value = themeText.value === '浅色' ? '深色' : '浅色'
  // TODO: 实现主题切换逻辑
  ElMessage.info('主题切换功能开发中')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  emit('close')
  emit('update:visible', false)
  router.push('/')
}
</script>

<style lang="scss" scoped>
.user-dropdown-wrapper {
  position: fixed; /* 使用 fixed 定位，确保不受父元素影响 */
  z-index: 99999; /* 设置最高层级，确保悬浮窗在所有元素之上 */
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

.user-dropdown {
  width: 320px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  /* 确保整个下拉菜单区域都能捕获鼠标事件 */
  pointer-events: auto;
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
    padding-bottom: 20px; /* 确保底部有足够的 padding */
    text-align: center;
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    /* 确保整个区域都能捕获鼠标事件 */
    min-height: 100%;
    
    .user-avatar {
      width: 80px;
      height: 80px;
      margin: 0 auto 12px;
      border-radius: 50%;
      overflow: hidden;
      border: 3px solid #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
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
        font-size: 12px;
        color: #61666d;
        margin-bottom: 6px;
        
        .level-label {
          font-weight: 500;
        }
        
        .level-value {
          color: #00a1d6;
          font-weight: 600;
        }
      }
      
      .progress-bar {
        width: 100%;
        height: 6px;
        background: #e3e5e7;
        border-radius: 3px;
        overflow: hidden;
        margin-bottom: 6px;
        
        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #00a1d6 0%, #00b5e5 100%);
          border-radius: 3px;
          transition: width 0.3s ease;
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
      justify-content: center;
      gap: 20px;
      font-size: 13px;
      color: #61666d;
      
      span {
        cursor: pointer;
        transition: color 0.2s;
        
        &:hover {
          color: #00a1d6;
        }
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
</style>

