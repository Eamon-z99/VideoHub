<template>
  <div v-if="showWarning" class="defense-warning">
    <div class="warning-content">
      <div class="warning-icon">⚠️</div>
      <div class="warning-text">
        <h3>安全提示</h3>
        <p>检测到异常行为，部分推荐内容可能受到投毒攻击影响</p>
        <p class="warning-detail" v-if="suspiciousCount > 0">
          已检测到 {{ suspiciousCount }} 个可疑账户
        </p>
      </div>
      <button class="close-btn" @click="dismissWarning">×</button>
    </div>
  </div>
</template>

<script>
import { getSuspiciousUsers } from '@/api/recommendation'

export default {
  name: 'DefenseWarning',
  data() {
    return {
      showWarning: false,
      suspiciousCount: 0,
      checkInterval: null
    }
  },
  mounted() {
    this.checkSuspiciousUsers()
    // 每5分钟检查一次
    this.checkInterval = setInterval(() => {
      this.checkSuspiciousUsers()
    }, 5 * 60 * 1000)
  },
  beforeUnmount() {
    if (this.checkInterval) {
      clearInterval(this.checkInterval)
    }
  },
  methods: {
    async checkSuspiciousUsers() {
      try {
        const response = await getSuspiciousUsers()
        if (response.data && response.data.count > 0) {
          this.suspiciousCount = response.data.count
          this.showWarning = true
        } else {
          this.showWarning = false
        }
      } catch (error) {
        console.error('检查可疑用户失败:', error)
      }
    },
    dismissWarning() {
      this.showWarning = false
    }
  }
}
</script>

<style scoped>
.defense-warning {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  max-width: 400px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.warning-content {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.warning-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.warning-text {
  flex: 1;
}

.warning-text h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.warning-text p {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.95;
}

.warning-detail {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  flex-shrink: 0;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>



