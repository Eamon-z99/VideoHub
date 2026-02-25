<template>
  <div class="submit-layout">
    <!-- 顶部标题栏（横跨整页） -->
    <div class="sidebar-header">
      <span class="site-title">创作中心</span>
    </div>

    <div class="submit-main">
      <!-- 左侧侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <el-menu
        :default-active="currentView"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item-group title="主要功能">
          <!-- 投稿：使用纯样式图标按钮，不再依赖图片资源 -->
          <el-menu-item index="submit" class="submit-menu-item">
            <div class="submit-entry">
              <span class="submit-icon">⭱</span>
              <span class="submit-text">投稿</span>
            </div>
          </el-menu-item>
          <el-menu-item index="home">
            <span>首页</span>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="内容管理">
          <el-menu-item index="contentManagement" disabled class="todo-item">
            <span>内容管理</span>
          </el-menu-item>
          <el-menu-item index="dataCenter" disabled class="todo-item">
            <span>数据中心</span>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="创作成长">
          <el-menu-item index="growth" disabled class="todo-item">
            <span>创作成长</span>
          </el-menu-item>
          <el-menu-item index="achievement" disabled class="todo-item">
            <span>任务成就</span>
            <el-tag size="small" type="danger" class="new-tag">NEW</el-tag>
          </el-menu-item>
          <el-menu-item index="promotion" disabled class="todo-item">
            <span>必火推广</span>
            <el-tag size="small" type="danger" class="new-tag">NEW</el-tag>
          </el-menu-item>
          <el-menu-item index="academy" disabled class="todo-item">
            <span>创作学院</span>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="设置与规范">
          <el-menu-item index="rights" disabled class="todo-item">
            <span>创作权益</span>
          </el-menu-item>
          <el-menu-item index="convention" disabled class="todo-item">
            <span>社区公约</span>
          </el-menu-item>
          <el-menu-item index="settings" disabled class="todo-item">
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
            <input
              ref="videoFileInput"
              type="file"
              accept="video/*"
              style="display: none"
              @change="onVideoFileChange"
            />
            <input
              ref="coverFileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="onCoverFileChange"
            />
            <p class="upload-title">选择要投稿的视频文件</p>
            <el-button type="primary" @click="triggerSelectVideo" :loading="uploading">
              {{ uploading ? '上传中...' : '选择视频并上传' }}
            </el-button>
            <p v-if="selectedVideoName" class="file-hint">
              当前文件：{{ selectedVideoName }}（{{ selectedVideoSize }}）
            </p>
            <div class="cover-row">
              <div class="cover-info">
                <span class="cover-label">封面（可选）：</span>
                <el-button size="small" @click="triggerSelectCover" :disabled="uploading">
                  选择封面图片
                </el-button>
                <span v-if="coverName" class="cover-text">已选择：{{ coverName }}</span>
              </div>
              <div v-if="coverPreview" class="cover-preview">
                <img :src="coverPreview" alt="封面预览" />
              </div>
            </div>
            <el-form label-width="72px" class="upload-form">
              <el-form-item label="标题">
                <el-input
                  v-model="videoTitle"
                  maxlength="80"
                  show-word-limit
                  placeholder="请输入视频标题"
                />
              </el-form-item>
              <el-form-item label="简介">
                <el-input
                  v-model="videoDescription"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  placeholder="简单介绍一下你的视频内容"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  :disabled="!videoFile || uploading"
                  :loading="uploading"
                  @click="submitVideo"
                >
                  {{ uploading ? '正在投稿...' : '立即投稿' }}
                </el-button>
              </el-form-item>
            </el-form>
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

      <!-- 首页内容 -->
      <div v-else-if="currentView === 'home'" class="content-page">
        <CreatorHome />
      </div>

      <!-- 创作成长内容 -->
      <div v-else-if="currentView === 'growth'" class="content-page">
        <CreatorGrowth />
      </div>

      <!-- 任务成就内容 -->
      <div v-else-if="currentView === 'achievement'" class="content-page">
        <Achievements />
      </div>

      <!-- 必火推广内容 -->
      <div v-else-if="currentView === 'promotion'" class="content-page">
        <Promotion />
      </div>

      <!-- 创作学院内容 -->
      <div v-else-if="currentView === 'academy'" class="content-page">
        <Academy />
      </div>

      <!-- 创作权益内容 -->
      <div v-else-if="currentView === 'rights'" class="content-page">
        <Rights />
      </div>

      <!-- 社区公约内容 -->
      <div v-else-if="currentView === 'convention'" class="content-page">
        <Convention />
      </div>

      <!-- 创作设置内容 -->
      <div v-else-if="currentView === 'settings'" class="content-page">
        <Settings />
      </div>

      <!-- 其他页面内容 -->
      <div v-else class="content-page">
        <h2>{{ getPageTitle(currentView) }}</h2>
        <p>这里将显示 {{ getPageTitle(currentView) }} 相关功能</p>
      </div>
      </el-main>
    </div>
  </div>
  
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ContentManagement from './content_management.vue'
import DataCenter from './data_center.vue'
import CreatorHome from './creator_home.vue'
import CreatorGrowth from './creator_growth.vue'
import Achievements from './achievements.vue'
import Promotion from './promotion.vue'
import Academy from './academy.vue'
import Rights from './rights.vue'
import Convention from './convention.vue'
import Settings from './settings.vue'
import { uploadVideo } from '@/api/video'
import { ElMessage } from 'element-plus'

const route = useRoute()

// 当前视图状态
const currentView = ref('submit')

// 根据 URL 参数设置初始视图
onMounted(() => {
  const viewParam = route.query.view
  if (viewParam && typeof viewParam === 'string') {
    currentView.value = viewParam
  }
})

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

// 视频投稿表单状态
const videoFileInput = ref(null)
const coverFileInput = ref(null)
const videoFile = ref(null)
const selectedVideoName = ref('')
const selectedVideoSize = ref('')
const coverFile = ref(null)
const coverName = ref('')
const coverPreview = ref('')
const videoDurationSeconds = ref(0)
const videoTitle = ref('')
const videoDescription = ref('')
const uploading = ref(false)

const triggerSelectVideo = () => {
  videoFileInput.value?.click()
}

const triggerSelectCover = () => {
  coverFileInput.value?.click()
}

const formatSize = (size) => {
  if (!size && size !== 0) return ''
  if (size >= 1024 * 1024 * 1024) {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
  if (size >= 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
  if (size >= 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  }
  return size + ' B'
}

const onVideoFileChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  // 简单类型校验
  if (!file.type.startsWith('video/')) {
    ElMessage.warning('请选择视频文件')
    return
  }
  videoFile.value = file
  selectedVideoName.value = file.name
  selectedVideoSize.value = formatSize(file.size)
  if (!videoTitle.value) {
    const dot = file.name.lastIndexOf('.')
    videoTitle.value = dot > 0 ? file.name.slice(0, dot) : file.name
  }

  // 获取视频时长（秒）：在浏览器端用隐藏 video 元素读取 metadata
  try {
    const url = URL.createObjectURL(file)
    const videoEl = document.createElement('video')
    videoEl.preload = 'metadata'
    videoEl.src = url
    videoEl.onloadedmetadata = () => {
      // 部分浏览器需要先暂停
      videoEl.pause()
      const dur = videoEl.duration
      if (typeof dur === 'number' && !Number.isNaN(dur) && dur > 0) {
        // 使用向下取整，避免 11.9 这类被四舍五入成 12 秒导致数据库多 1 秒
        videoDurationSeconds.value = Math.floor(dur)
      } else {
        videoDurationSeconds.value = 0
      }
      URL.revokeObjectURL(url)
    }
    videoEl.onerror = () => {
      videoDurationSeconds.value = 0
      URL.revokeObjectURL(url)
    }
  } catch (e) {
    videoDurationSeconds.value = 0
  }
}

const onCoverFileChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('封面必须是图片文件')
    return
  }
  coverFile.value = file
  coverName.value = file.name
  // 预览
  coverPreview.value = URL.createObjectURL(file)
}

const submitVideo = async () => {
  if (!videoFile.value) {
    ElMessage.warning('请先选择要投稿的视频文件')
    return
  }
  if (!videoTitle.value.trim()) {
    ElMessage.warning('请填写视频标题')
    return
  }
  const formData = new FormData()
  formData.append('file', videoFile.value)
  formData.append('title', videoTitle.value.trim())
  formData.append('description', videoDescription.value.trim())
  if (coverFile.value) {
    formData.append('cover', coverFile.value)
  }
  if (videoDurationSeconds.value && videoDurationSeconds.value > 0) {
    formData.append('duration', String(videoDurationSeconds.value))
  }

  uploading.value = true
  try {
    const { data } = await uploadVideo(formData)
    if (data && data.success) {
      ElMessage.success('投稿成功，稍后即可在首页看到新视频')
      // 重置表单
      videoFile.value = null
      selectedVideoName.value = ''
      selectedVideoSize.value = ''
      videoTitle.value = ''
      videoDescription.value = ''
      if (videoFileInput.value) {
        videoFileInput.value.value = ''
      }
      if (coverFileInput.value) {
        coverFileInput.value.value = ''
      }
      if (coverPreview.value) {
        URL.revokeObjectURL(coverPreview.value)
      }
      coverFile.value = null
      coverName.value = ''
      coverPreview.value = ''
    } else {
      ElMessage.error(data?.message || '投稿失败，请稍后重试')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '投稿失败，请稍后重试')
  } finally {
    uploading.value = false
  }
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
  flex-direction: column;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.submit-main {
  display: flex;
  flex: 1;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
  
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
    // 不显示分组标题（主要功能 / 内容管理 / 创作成长 / 设置与规范）
    display: none;
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

/* 左侧“投稿”按钮的图标样式，参考你提供的蓝色按钮效果 */
:deep(.submit-menu-item) {
  /* 清理默认高亮背景，交给内部按钮控制 */
  &.is-active {
    background-color: transparent;
  }

  .submit-entry {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 40px;
    margin: 4px 0;
    border-radius: 4px;
    background-color: #00a7e1;
    color: #fff;
    font-size: 15px;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }

  .submit-icon {
    width: 22px;
    height: 22px;
    border-radius: 4px;
    border: 2px solid #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    line-height: 1;
  }

  .submit-text {
    letter-spacing: 2px;
  }

  &:hover .submit-entry {
    background-color: #0092c6;
  }
}

.new-tag {
  margin-left: auto;
}

/* 其他未开发菜单项：浅灰色、不可点击 */
:deep(.el-menu-item.todo-item.is-disabled) {
  color: #9ca3af !important; // 稍深一点的灰色，保证可读性
  cursor: default;

  &:hover {
    background-color: transparent !important;
  }

  .el-tag {
    opacity: 0.5;
  }
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
  padding: 24px 40px 32px;
  
  .upload-title {
    margin: 8px 0 16px;
    color: #606266;
    font-size: 14px;
  }

  .file-hint {
    margin-top: 8px;
    color: #909399;
    font-size: 13px;
  }

  .cover-row {
    margin-top: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .cover-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #606266;
  }

  .cover-label {
    font-weight: 500;
  }

  .cover-text {
    color: #909399;
  }

  .cover-preview {
    width: 120px;
    height: 68px;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .upload-form {
    margin-top: 24px;
    text-align: left;
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

