<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>创作设置</h1>
      <p>个性化你的创作环境和偏好设置</p>
    </div>

    <!-- 设置导航 -->
    <div class="settings-nav">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="基本设置" name="basic">
          <div class="settings-content">
            <!-- 账号信息 -->
            <div class="settings-section">
              <h3>账号信息</h3>
              <div class="settings-form">
                <div class="form-item">
                  <label>创作者昵称</label>
                  <el-input v-model="accountInfo.nickname" placeholder="请输入昵称" />
                </div>
                <div class="form-item">
                  <label>个人简介</label>
                  <el-input 
                    v-model="accountInfo.bio" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="请输入个人简介" 
                  />
                </div>
                <div class="form-item">
                  <label>联系方式</label>
                  <el-input v-model="accountInfo.contact" placeholder="请输入联系方式" />
                </div>
              </div>
            </div>

            <!-- 创作偏好 -->
            <div class="settings-section">
              <h3>创作偏好</h3>
              <div class="settings-form">
                <div class="form-item">
                  <label>默认视频分类</label>
                  <el-select v-model="preferences.defaultCategory" placeholder="请选择分类">
                    <el-option label="科技" value="tech" />
                    <el-option label="教育" value="education" />
                    <el-option label="娱乐" value="entertainment" />
                    <el-option label="生活" value="lifestyle" />
                  </el-select>
                </div>
                <div class="form-item">
                  <label>视频质量</label>
                  <el-radio-group v-model="preferences.videoQuality">
                    <el-radio label="720p">720P</el-radio>
                    <el-radio label="1080p">1080P</el-radio>
                    <el-radio label="4k">4K</el-radio>
                  </el-radio-group>
                </div>
                <div class="form-item">
                  <label>自动发布</label>
                  <el-switch v-model="preferences.autoPublish" />
                  <span class="form-hint">开启后视频审核通过将自动发布</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="通知设置" name="notifications">
          <div class="settings-content">
            <div class="settings-section">
              <h3>通知偏好</h3>
              <div class="notification-list">
                <div v-for="notification in notifications" :key="notification.id" class="notification-item">
                  <div class="notification-info">
                    <h4>{{ notification.title }}</h4>
                    <p>{{ notification.description }}</p>
                  </div>
                  <div class="notification-controls">
                    <el-switch v-model="notification.enabled" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="隐私设置" name="privacy">
          <div class="settings-content">
            <div class="settings-section">
              <h3>隐私控制</h3>
              <div class="privacy-list">
                <div v-for="privacy in privacySettings" :key="privacy.id" class="privacy-item">
                  <div class="privacy-info">
                    <h4>{{ privacy.title }}</h4>
                    <p>{{ privacy.description }}</p>
                  </div>
                  <div class="privacy-controls">
                    <el-select v-model="privacy.value" size="small">
                      <el-option label="公开" value="public" />
                      <el-option label="仅粉丝" value="followers" />
                      <el-option label="私密" value="private" />
                    </el-select>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <div class="settings-content">
            <div class="settings-section">
              <h3>账号安全</h3>
              <div class="security-list">
                <div class="security-item">
                  <div class="security-info">
                    <h4>修改密码</h4>
                    <p>定期修改密码保护账号安全</p>
                  </div>
                  <el-button type="primary" plain @click="changePassword">修改密码</el-button>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <h4>两步验证</h4>
                    <p>开启两步验证增强账号安全性</p>
                  </div>
                  <el-switch v-model="security.twoFactor" />
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <h4>登录设备管理</h4>
                    <p>管理已登录的设备</p>
                  </div>
                  <el-button type="primary" plain @click="manageDevices">管理设备</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 保存按钮 -->
    <div class="settings-footer">
      <el-button type="primary" size="large" @click="saveSettings">保存设置</el-button>
      <el-button size="large" @click="resetSettings">重置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 当前标签页
const activeTab = ref('basic')

// 账号信息
const accountInfo = reactive({
  nickname: '创作者昵称',
  bio: '这是我的个人简介，介绍我的创作理念和风格。',
  contact: 'contact@example.com'
})

// 创作偏好
const preferences = reactive({
  defaultCategory: 'tech',
  videoQuality: '1080p',
  autoPublish: false
})

// 通知设置
const notifications = ref([
  {
    id: 1,
    title: '新评论通知',
    description: '当有人评论你的视频时通知你',
    enabled: true
  },
  {
    id: 2,
    title: '粉丝关注通知',
    description: '当有新粉丝关注你时通知你',
    enabled: true
  },
  {
    id: 3,
    title: '视频审核通知',
    description: '当视频审核完成时通知你',
    enabled: true
  },
  {
    id: 4,
    title: '系统消息通知',
    description: '接收系统重要消息和公告',
    enabled: false
  }
])

// 隐私设置
const privacySettings = ref([
  {
    id: 1,
    title: '个人资料可见性',
    description: '控制谁可以看到你的个人资料信息',
    value: 'public'
  },
  {
    id: 2,
    title: '视频可见性',
    description: '控制谁可以看到你发布的视频',
    value: 'public'
  },
  {
    id: 3,
    title: '关注列表可见性',
    description: '控制谁可以看到你的关注列表',
    value: 'followers'
  }
])

// 安全设置
const security = reactive({
  twoFactor: false
})

const handleTabClick = (tab) => {
  console.log('切换到标签页:', tab.props.name)
}

const changePassword = () => {
  console.log('修改密码')
}

const manageDevices = () => {
  console.log('管理设备')
}

const saveSettings = () => {
  console.log('保存设置')
  // 这里可以调用API保存设置
}

const resetSettings = () => {
  console.log('重置设置')
  // 这里可以重置所有设置到默认值
}
</script>

<style lang="scss" scoped>
.settings-page {
  padding: 0;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 28px;
    margin: 0 0 8px 0;
    color: #333;
  }
  
  p {
    font-size: 16px;
    color: #666;
    margin: 0;
  }
}

.settings-nav {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 24px;
}

.settings-content {
  padding: 24px;
}

.settings-section {
  margin-bottom: 32px;
  
  h3 {
    font-size: 18px;
    margin: 0 0 16px 0;
    color: #333;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  label {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }
  
  .form-hint {
    font-size: 12px;
    color: #999;
    margin-left: 8px;
  }
}

.notification-list, .privacy-list, .security-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-item, .privacy-item, .security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  
  .notification-info, .privacy-info, .security-info {
    flex: 1;
    
    h4 {
      font-size: 16px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0;
    }
  }
  
  .notification-controls, .privacy-controls {
    flex-shrink: 0;
  }
}

.settings-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

@media (max-width: 768px) {
  .notification-item, .privacy-item, .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .settings-footer {
    flex-direction: column;
  }
}
</style>
