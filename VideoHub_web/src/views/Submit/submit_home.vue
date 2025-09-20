<template>
  <div class="submit-layout">
    <!-- 左侧侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="sidebar-header">
        <span class="site-title">bilibili 创作中心</span>
      </div>
      
      <el-menu
        :default-active="currentView"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item-group title="主要功能">
          <el-menu-item index="submit">
            <span>投稿</span>
          </el-menu-item>
          <el-menu-item index="home">
            <span>首页</span>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="内容管理">
          <el-menu-item index="contentManagement">
            <span>内容管理</span>
          </el-menu-item>
          <el-menu-item index="dataCenter">
            <span>数据中心</span>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="创作成长">
          <el-menu-item index="growth">
            <span>创作成长</span>
          </el-menu-item>
          <el-menu-item index="achievement">
            <span>任务成就</span>
            <el-tag size="small" type="danger" class="new-tag">NEW</el-tag>
          </el-menu-item>
          <el-menu-item index="promotion">
            <span>必火推广</span>
            <el-tag size="small" type="danger" class="new-tag">NEW</el-tag>
          </el-menu-item>
          <el-menu-item index="academy">
            <span>创作学院</span>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="设置与规范">
          <el-menu-item index="rights">
            <span>创作权益</span>
          </el-menu-item>
          <el-menu-item index="convention">
            <span>社区公约</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <span>创作设置</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区域 -->
    <el-main class="content-main">
      <!-- 投稿页面内容 -->
      <div v-if="currentView === 'submit'" class="content-page">
        <!-- 顶部标签导航 -->
        <el-tabs v-model="activeTab" class="submit-tabs">
          <el-tab-pane
            v-for="tab in tabs"
            :key="tab.key"
            :label="tab.label"
            :name="tab.key"
          />
        </el-tabs>

        <!-- 上传区域 -->
        <el-card class="upload-card" shadow="never">
          <div class="upload-area">
            <p>拖拽到此处也可上传</p>
            <el-button type="primary" @click="handleFileChange">上传视频</el-button>
            <p>当前审核队列 <el-tag size="small" type="primary">快速</el-tag></p>
          </div>
        </el-card>

        <!-- 推广模块 -->
        <div class="promo-list">
          <el-card class="promo-card" shadow="hover">
            <div class="promo-content">
              <div class="promo-text">
                <h3>哔哩哔哩投稿快捷方式</h3>
                <p>保存bilibili投稿入口到桌面，界面更简单，投稿更快捷</p>
              </div>
              <el-button type="primary" plain>立即安装</el-button>
            </div>
          </el-card>

          <el-card class="promo-card" shadow="hover">
            <div class="promo-content">
              <div class="promo-text">
                <h3>必剪桌面端</h3>
                <p>一键字幕，海量素材，全能剪辑，支持一键投稿</p>
              </div>
              <el-button type="primary" plain>立即下载</el-button>
            </div>
          </el-card>
        </div>

        <!-- 底部说明 -->
        <div class="footer-note">
          <div class="links">
            <el-link type="primary" href="javascript:void(0)">选择本地视频</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" href="javascript:void(0)">哔哩哔哩内容规范</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" href="javascript:void(0)">哔哩哔哩账号公约</el-link>
          </div>
          <div class="tools">
            <span>创作工具：</span>
            <el-link type="primary" href="javascript:void(0)">小程序版</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" href="javascript:void(0)">PC版</el-link>
          </div>
        </div>
      </div>

      <!-- 内容管理页面内容 -->
      <div v-else-if="currentView === 'contentManagement'" class="content-page">
        <ContentManagement />
      </div>

      <!-- 数据中心页面内容 -->
      <div v-else-if="currentView === 'dataCenter'" class="content-page">
        <DataCenter />
      </div>

      <!-- 其他页面内容 -->
      <div v-else class="content-page">
        <h2>{{ getPageTitle(currentView) }}</h2>
        <p>这里将显示 {{ getPageTitle(currentView) }} 相关功能</p>
      </div>
    </el-main>
  </div>
  
</template>

<script setup>
import { ref, computed } from 'vue'
import ContentManagement from './content_management.vue'
import DataCenter from './data_center.vue'

// 当前视图状态
const currentView = ref('submit')

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

const handleMenuSelect = (index) => {
  currentView.value = index
  console.log('切换到:', index)
}

const handleFileChange = () => {
  console.log('点击上传按钮')
}

// 获取页面标题
const getPageTitle = (view) => {
  const titleMap = {
    'submit': '投稿',
    'home': '首页',
    'contentManagement': '内容管理',
    'dataCenter': '数据中心',
    'growth': '创作成长',
    'achievement': '任务成就',
    'promotion': '必火推广',
    'academy': '创作学院',
    'rights': '创作权益',
    'convention': '社区公约',
    'settings': '创作设置'
  }
  return titleMap[view] || view
}
</script>

<style lang="scss" scoped>
.submit-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  
  .site-logo {
    background: linear-gradient(135deg, #67d1ff, #00aeec);
  }
  
  .site-title {
    color: #303133;
    font-weight: 600;
    font-size: 16px;
  }
}

.sidebar-menu {
  border-right: none;
  
  :deep(.el-menu-item-group__title) {
    color: #909399;
    font-size: 12px;
    padding: 0 20px;
    margin-top: 16px;
  }
  
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    
    &.is-active {
      background-color: #ecf5ff;
      color: #409eff;
    }
  }
}

.new-tag {
  margin-left: auto;
}

.content-main {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.content-page {
  max-width: 1200px;
  margin: 0 auto;
}

.submit-tabs {
  margin-bottom: 16px;
  
  :deep(.el-tabs__header) {
    margin: 0 0 16px 0;
  }
}

.upload-card {
  margin-bottom: 16px;
}

.upload-area {
  text-align: center;
  padding: 40px;
  
  p {
    margin: 16px 0;
    color: #606266;
  }
}

.promo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.promo-card {
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.promo-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.promo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  
  &.quick {
    background: #e6f7ff;
    color: #409eff;
  }
  
  &.bijian {
    background: #ffeaea;
    color: #f56c6c;
  }
}

.promo-text {
  flex: 1;
  
  h3 {
    margin: 0 0 6px 0;
    font-size: 16px;
    color: #303133;
    font-weight: 500;
  }
  
  p {
    margin: 0;
    color: #606266;
    font-size: 13px;
    line-height: 1.4;
  }
}

.footer-note {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  
  .links,
  .tools {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  :deep(.el-divider--vertical) {
    margin: 0 8px;
  }
}

@media (max-width: 768px) {
  .submit-layout {
    flex-direction: column;
  }
  
  .sidebar {
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .footer-note {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>

