<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { List, Upload } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => (route.path === '/' ? '/pending' : route.path))
const pageTitle = computed(() => (typeof route.meta.title === 'string' ? route.meta.title : '管理'))

onMounted(() => {
  void auth.hydrateFromServer()
})

function logout() {
  auth.clear()
  void router.replace('/login')
}
</script>

<template>
  <div class="submit-layout">
    <!-- 顶部标题栏（参考创作中心：横跨整页） -->
    <div class="sidebar-header">
      <div class="header-left">
        <span class="site-title">管理中心</span>
        <span class="page-title">· {{ pageTitle }}</span>
      </div>

      <div class="header-right">
        <span class="user">{{ auth.username || auth.loginAccount }}</span>
        <el-button type="danger" link @click="logout">退出</el-button>
      </div>
    </div>

    <div class="submit-main">
      <!-- 左侧侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <el-menu
          :default-active="active"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/pending">
            <div class="menu-item-inner">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-label">待审核</span>
            </div>
          </el-menu-item>

          <el-menu-item index="/publish">
            <div class="menu-item-inner">
              <el-icon class="menu-icon"><Upload /></el-icon>
              <span class="menu-label">待发布</span>
            </div>
          </el-menu-item>

          <el-menu-item index="/complaints">
            <div class="menu-item-inner">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-label">举报处理</span>
            </div>
          </el-menu-item>

          <el-menu-item index="/home-hero">
            <div class="menu-item-inner">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-label">首页轮播配置</span>
            </div>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域（参考创作中心：居中 max-width） -->
      <el-main class="content-main">
        <div class="content-page">
          <RouterView />
        </div>
      </el-main>
    </div>
  </div>
</template>

<style scoped>
.submit-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  background: #f5f7fa;
  box-sizing: border-box;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
  color: #00a1d6;
  width: 100%;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.site-title {
  font-weight: 700;
  font-size: 20px;
}

.page-title {
  color: #6b7280;
  font-size: 14px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user {
  color: #606266;
  font-size: 14px;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.submit-main {
  display: flex;
  flex: 1;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  width: 220px;
  flex: 0 0 220px;
}

.sidebar-menu {
  border-right: none;
}

:deep(.sidebar-menu .el-menu-item) {
  height: 46px;
  line-height: 46px;
  color: #4b5563;
}

:deep(.sidebar-menu .el-menu-item.is-active) {
  background-color: transparent !important;
  color: #00a1d6 !important;
}

:deep(.sidebar-menu .el-menu-item:hover) {
  background-color: #f6f6f8 !important;
}

.menu-item-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 80%;
  margin: 0 auto;
}

.menu-icon {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: inherit;
}

.menu-label {
  font-size: 14px;
}

.content-main.el-main {
  flex: 1;
  min-width: 0;
  width: 100%;
  padding: 16px 16px 24px;
  overflow: visible !important;
  background: #fafafa;
}

.content-page {
  width: 100%;
  max-width: none;
  margin: 0;
  background: #ffffff;
  min-height: 0;
}

@media (max-width: 768px) {
  .submit-main {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
}
</style>
