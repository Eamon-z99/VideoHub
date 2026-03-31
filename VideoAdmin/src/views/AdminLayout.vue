<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import webLogoRaw from '@/assets/webLogo.svg?raw'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const webLogoSvg = computed(() =>
  webLogoRaw
    .replace(/fill="#000"\b/gi, 'fill="currentColor"')
    .replace(/fill="#000000"\b/gi, 'fill="currentColor"')
    .replace(/<svg\b/i, '<svg style="width:100%;height:100%;display:block;"')
)

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
        <span class="site-logo" aria-hidden="true" v-html="webLogoSvg"></span>
        <span class="site-title">管理中心</span>
        <!-- <span class="page-title">· {{ pageTitle }}</span> -->
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
          <el-sub-menu index="review">
            <template #title>
              <div class="parent-menu-item-inner">
                <span class="menu-icon" aria-hidden="true">
                  <svg viewBox="0 0 1024 1024" width="20" height="20">
                    <path d="M800 672.96a96 96 0 1 1 0 192 96 96 0 0 1 0-192z m0 64a32 32 0 1 0 0 64 32 32 0 0 0 0-64z" fill="currentColor"></path>
                    <path d="M751.616 536.128a96 96 0 0 1 96.768 0l128.128 74.624a96 96 0 0 1 47.616 82.944v150.464a96 96 0 0 1-47.616 82.944l-128.128 74.688a96 96 0 0 1-96.768-0.064l-127.68-74.624a96 96 0 0 1-47.552-82.88v-150.592a96 96 0 0 1 47.552-82.88z m64.512 55.232a32 32 0 0 0-32.256 0l-127.68 74.688a32 32 0 0 0-15.808 27.584v150.592a32 32 0 0 0 15.808 27.584l127.68 74.624a32 32 0 0 0 32.256 0l128.128-74.624a32 32 0 0 0 15.872-27.648v-150.4a32 32 0 0 0-15.872-27.712z" fill="currentColor"></path>
                    <path d="M800 63.488a96 96 0 0 1 95.552 86.72l0.384 9.728v287.616h-64V158.72l-0.448-4.992A32 32 0 0 0 805.76 128l-5.76-0.512-575.232 0.064a32 32 0 0 0-31.488 26.24l-0.512 5.76v702.848a32 32 0 0 0 26.112 31.488l5.76 0.512L512.064 896l-0.256 64-287.552-1.6a96 96 0 0 1-95.168-86.784l-0.384-9.216V159.552A96 96 0 0 1 215.488 64l9.28-0.448L800 63.488z" fill="currentColor"></path>
                    <path d="M448 640v64H256.128v-64zM640 448v64H257.28V448zM766.912 256.96v64H256.128v-64z" fill="currentColor"></path>
                  </svg>
                </span>
                <span class="menu-label">视频审核</span>
              </div>
            </template>

            <el-menu-item index="/pending">
              <div class="child-menu-item-inner">
                <span class="menu-label">待审核</span>
              </div>
            </el-menu-item>

            <el-menu-item index="/publish">
              <div class="child-menu-item-inner">
                <span class="menu-label">待发布</span>
              </div>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/avatar-review">
            <div class="parent-menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path
                    d="M512 64c247.4 0 448 200.6 448 448S759.4 960 512 960 64 759.4 64 512 264.6 64 512 64zm0 64C300 128 128 300 128 512s172 384 384 384 384-172 384-384S724 128 512 128z"
                    fill="currentColor"
                  />
                  <path
                    d="M512 288c79.5 0 144 64.5 144 144s-64.5 144-144 144-144-64.5-144-144 64.5-144 144-144zm0 64a80 80 0 1 0 0 160 80 80 0 0 0 0-160z"
                    fill="currentColor"
                  />
                  <path
                    d="M512 608c124.6 0 235 63.6 300.1 160.3a32 32 0 0 1-53.1 35.4C705.6 723.4 614 672 512 672s-193.6 51.4-247 131.7a32 32 0 0 1-53.1-35.4C277 671.6 387.4 608 512 608z"
                    fill="currentColor"
                  />
                </svg>
              </span>
              <span class="menu-label">头像审核</span>
            </div>
          </el-menu-item>

          <el-menu-item index="/complaints">
            <div class="parent-menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M818.7 223.9c12.9 0 23.8 10.9 23.8 23.8v400.9c0 12.9-10.9 23.8-23.8 23.8H209.3c-12.9 0-23.8-10.9-23.8-23.8V247.7c0-12.9 10.9-23.8 23.8-23.8h609.4m0-65.5H209.3c-49.1 0-89.3 40.2-89.3 89.3v400.9c0 49.1 40.2 89.3 89.3 89.3h609.3c49.1 0 89.3-40.2 89.3-89.3V247.7c0-49.1-40.1-89.3-89.2-89.3z" fill="currentColor"></path>
                  <path d="M309.8 544.2c-9.3 0-18.6-4-25.1-11.7-11.6-13.9-9.8-34.5 4-46.1L435 363.6c13.1-10.9 32.3-10.1 44.3 2.1l99.4 100.6 117.5-105.8c13.4-12.1 34.1-11 46.2 2.4 12.1 13.4 11 34.1-2.4 46.2L599.4 535.8c-13 11.7-32.9 11.1-45.2-1.3L454 433.2 330.8 536.6c-6.1 5.1-13.6 7.6-21 7.6zM807.7 887.4H220.3c-18.1 0-32.7-14.7-32.7-32.7s14.7-32.7 32.7-32.7h587.4c18.1 0 32.7 14.7 32.7 32.7s-14.6 32.7-32.7 32.7z" fill="currentColor"></path>
                </svg>
              </span>
              <span class="menu-label">视频举报处理</span>
            </div>
          </el-menu-item>

          <el-menu-item index="/home-hero">
            <div class="parent-menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M985.6 486.4l-448-448c-12.8-6.4-38.4-6.4-51.2 0l-448 448c-6.4 12.8-12.8 44.8 0 51.2 12.8 6.4 38.4 6.4 51.2 0L512 108.8l422.4 428.8c6.4 6.4 19.2 6.4 25.6 6.4 6.4 0 19.2 0 25.6-12.8 12.8-12.8 12.8-32 0-44.8zM832 620.8c-19.2 0-32 12.8-32 32V896h-576v-236.8c0-19.2-12.8-32-32-32s-32 12.8-32 25.6v275.2c0 19.2 12.8 32 32 32h640c19.2 0 32-12.8 32-25.6v-275.2c0-19.2-12.8-38.4-32-38.4z" fill="currentColor"></path>
                  <path d="M704 550.4c0-19.2-6.4-32-25.6-38.4-19.2-6.4-38.4 6.4-38.4 25.6-12.8 57.6-64 102.4-128 102.4s-115.2-38.4-128-102.4c-6.4-19.2-19.2-25.6-38.4-25.6-12.8 0-25.6 19.2-25.6 32C339.2 640 422.4 704 512 704s172.8-64 192-153.6z" fill="currentColor"></path>
                </svg>
              </span>
              <span class="menu-label">首页轮播配置</span>
            </div>
          </el-menu-item>

          <el-menu-item index="/hot-search">
            <div class="parent-menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path
                    d="M512 64c247.4 0 448 200.6 448 448S759.4 960 512 960 64 759.4 64 512 264.6 64 512 64zm0 64C300 128 128 300 128 512s172 384 384 384 384-172 384-384S724 128 512 128z"
                    fill="currentColor"
                  />
                  <path
                    d="M512 256c141.4 0 256 114.6 256 256S653.4 768 512 768 256 653.4 256 512 370.6 256 512 256zm0 64c-106 0-192 86-192 192s86 192 192 192 192-86 192-192-86-192-192-192z"
                    fill="currentColor"
                  />
                  <path
                    d="M720 320l48 48-208 208-112-112 48-48 64 64z"
                    fill="currentColor"
                  />
                </svg>
              </span>
              <span class="menu-label">热搜管理</span>
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
  align-items: center;
  gap: 8px;
}

.site-logo {
  width: 90px;
  height: 45px;
  display: inline-flex;
  flex: 0 0 auto;
  color: currentColor;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.site-logo :deep(svg) {
  width: 100%;
  height: 100%;
  display: block;
}

.site-logo :deep(path) {
  fill: currentColor !important;
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

/* 父 / 子菜单文字左侧对齐：去掉 Element Plus 默认缩进 */
::deep(.sidebar-menu .el-sub-menu__title),
::deep(.sidebar-menu .el-menu-item) {
  padding-left: 0 !important;
}

.parent-menu-item-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 0 20px;
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

.child-menu-item-inner {
  display: flex;
  align-items: center;
  width: 100%;
  padding-left: 50px;
  margin-left: -5px;
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
