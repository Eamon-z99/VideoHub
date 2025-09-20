<template>
  <div class="submit-layout">
    <!-- 左侧侧边栏（仅展示，不接入路由） -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="site-logo"></div>
        <span class="site-title">bilibili 创作中心</span>
      </div>
      <nav class="menu">
        <div class="menu-group">
          <a class="menu-item" :class="{ active: isActive('/submitHome') }" @click="router.push('/submitHome')"><span class="icon">⏫</span><span class="label">投稿</span></a>
          <a class="menu-item"><span class="icon">🏠</span><span class="label">首页</span></a>
        </div>
        <div class="menu-group">
          <a class="menu-item" :class="{ active: isActive('/contentManagement') }" @click="router.push('/contentManagement')"><span class="icon">📦</span><span class="label">内容管理</span></a>
          <a class="menu-item" :class="{ active: isActive('/dataCenter') }" @click="router.push('/dataCenter')"><span class="icon">📊</span><span class="label">数据中心</span></a>
          <a class="menu-item"><span class="icon">👥</span><span class="label">粉丝管理</span></a>
          <a class="menu-item"><span class="icon">💬</span><span class="label">互动管理</span></a>
          <a class="menu-item"><span class="icon">💰</span><span class="label">收益管理</span></a>
        </div>
        <div class="menu-group">
          <a class="menu-item"><span class="icon">📈</span><span class="label">创作成长</span></a>
          <a class="menu-item with-badge"><span class="icon">🏅</span><span class="label">任务成就</span><span class="badge new">NEW</span></a>
          <a class="menu-item with-badge"><span class="icon">📣</span><span class="label">必火推广</span><span class="badge new">NEW</span></a>
          <a class="menu-item"><span class="icon">🏫</span><span class="label">创作学院</span></a>
        </div>
        <div class="menu-group">
          <a class="menu-item"><span class="icon">🛡️</span><span class="label">创作权益</span></a>
          <a class="menu-item"><span class="icon">📜</span><span class="label">社区公约</span></a>
          <a class="menu-item"><span class="icon">⚙️</span><span class="label">创作设置</span></a>
        </div>
      </nav>
    </aside>

    <main class="submit-main">
      <div class="submit-page">
    <!-- 顶部标签导航 -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 上传区域 -->
    <div class="upload-card">
      <div
        class="dropzone"
        @dragover.prevent
        @dragenter.prevent
        @drop.prevent="onDrop"
      >
        <div class="drop-content">
          <div class="cloud-icon"></div>
          <p class="tip">拖拽到此处也可上传</p>
          <button class="primary-btn" @click="triggerFile">上传视频</button>
          <p class="sub-tip">当前审核队列 <span class="badge">快速</span></p>
        </div>
        <input ref="fileInput" type="file" class="hidden-input" @change="onSelect" />
      </div>
    </div>

    <!-- 推广模块 -->
    <div class="promo-list">
      <div class="promo-card">
        <div class="promo-icon quick"></div>
        <div class="promo-content">
          <h3>哔哩哔哩投稿快捷方式</h3>
          <p>保存bilibili投稿入口到桌面，界面更简单，投稿更快捷</p>
        </div>
        <button class="outline-btn">立即安装</button>
      </div>

      <div class="promo-card">
        <div class="promo-icon bijian"></div>
        <div class="promo-content">
          <h3>必剪桌面端</h3>
          <p>一键字幕，海量素材，全能剪辑，支持一键投稿</p>
        </div>
        <button class="outline-btn">立即下载</button>
      </div>
    </div>

    <!-- 底部说明 -->
    <div class="footer-note">
      <div class="links">
        <a href="javascript:void(0)">选择本地视频</a>
        <span class="dot">·</span>
        <a href="javascript:void(0)">哔哩哔哩内容规范</a>
        <span class="dot">·</span>
        <a href="javascript:void(0)">哔哩哔哩账号公约</a>
      </div>
      <div class="tools">
        <span>创作工具：</span>
        <a href="javascript:void(0)">小程序版</a>
        <span class="dot">·</span>
        <a href="javascript:void(0)">PC版</a>
      </div>
    </div>
      </div>
    </main>
  </div>
  
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isActive = (path) => route.path === path

const tabs = [
  { key: 'video', label: '视频投稿' },
  { key: 'short', label: '短剧投稿' },
  { key: 'column', label: '专栏投稿' },
  { key: 'interactive', label: '互动视频投稿' },
  { key: 'music', label: '音频投稿' },
  { key: 'sticker', label: '贴纸投稿' },
  { key: 'material', label: '视频素材投稿' }
]

const activeTab = ref('video')
const fileInput = ref(null)

const triggerFile = () => {
  fileInput.value && fileInput.value.click()
}

const onSelect = (e) => {
  const files = e.target.files
  if (files && files.length) {
    console.log('选择文件: ', files[0].name)
  }
}

const onDrop = (e) => {
  const files = e.dataTransfer.files
  if (files && files.length) {
    console.log('拖拽文件: ', files[0].name)
  }
}
</script>

<style lang="scss" scoped>
.submit-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px 8px;
  height: fit-content;
  position: sticky;
  top: 16px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 8px 12px 8px;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 8px;
  
  .site-logo {
    width: 24px;
    height: 24px;
    border-radius: 6px;
    background: linear-gradient(135deg, #67d1ff, #00aeec);
  }
  .site-title {
    color: #333;
    font-weight: 600;
  }
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.menu-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 4px 8px 4px;
  border-bottom: 1px solid #f7f7f7;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  color: #444;
  text-decoration: none;
  cursor: pointer;
  
  &:hover { background: #f5f7fa; }
  &.active { color: #00aeec; background: #e6f7ff; }
  
  .icon { width: 18px; text-align: center; opacity: .85; }
  .label { flex: 1; }
}

.menu-item.with-badge {
  position: relative;
  .badge.new {
    margin-left: auto;
    font-size: 10px;
    color: #ff4d4f;
    background: #fff1f0;
    border: 1px solid #ffd6d6;
    padding: 1px 6px;
    border-radius: 10px;
  }
}

.submit-main {
  min-width: 0;
}

.submit-page {
  background: #fff;
  padding: 16px 24px 32px;
}

.tabs {
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
  margin-bottom: 16px;
  
  .tab-btn {
    background: transparent;
    border: none;
    padding: 8px 12px;
    border-radius: 6px;
    color: #666;
    cursor: pointer;
    font-size: 14px;
    
    &.active {
      color: #00aeec;
      background: #e6f7ff;
    }
    
    &:hover {
      background: #f5f7fa;
    }
  }
}

.upload-card {
  background: #fff;
  border: 1px dashed #e5e7eb;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.dropzone {
  background: #fafafa;
  border: 2px dashed #e5e7eb;
  border-radius: 8px;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drop-content {
  text-align: center;
}

.cloud-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #9aa6ff, #a88bff);
  margin: 0 auto 12px;
}

.tip {
  color: #666;
  margin: 0 0 12px 0;
}

.primary-btn {
  background: #00aeec;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.sub-tip {
  color: #999;
  margin-top: 10px;
  .badge {
    display: inline-block;
    padding: 2px 6px;
    background: #e6f7ff;
    color: #00aeec;
    border-radius: 10px;
    font-size: 12px;
  }
}

.promo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.promo-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}

.promo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  &.quick { background: #e6f7ff; }
  &.bijian { background: #ffeaea; }
}

.promo-content {
  flex: 1;
  h3 { margin: 0 0 6px 0; font-size: 16px; color: #333; }
  p { margin: 0; color: #666; font-size: 13px; }
}

.outline-btn {
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  color: #333;
  padding: 8px 14px;
  cursor: pointer;
}

.footer-note {
  display: flex;
  justify-content: space-between;
  color: #888;
  font-size: 12px;
  margin-top: 12px;
  
  a { color: #666; text-decoration: none; }
  .dot { margin: 0 6px; color: #ccc; }
}

@media (max-width: 768px) {
  .submit-layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    position: static;
  }
  .footer-note {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

