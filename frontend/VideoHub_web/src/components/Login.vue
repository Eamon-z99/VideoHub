<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { loginApi } from '@/api/auth';
import { fetchMyProfile } from '@/api/userProfile';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

// 如果是路由模式（/login），自动显示登录框
const isRouteMode = computed(() => route.path === '/login')
const shouldShow = computed(() => props.show || isRouteMode.value)

const emit = defineEmits(['update:show'])

const form = ref({
  account: '',
  password: ''
});

const loading = ref(false);
const passwordVisible = ref(false)
const isPasswordInputActive = ref(false)
const registerMode = ref(false)

// 规范化头像 URL
const normalizeAvatarUrl = (url) => {
  if (!url) return ''
  // data URL（默认灰头像）直接返回
  if (url.startsWith('data:')) return url
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

const closeLogin = () => {
  emit('update:show', false)
  // 清空输入框
  form.value.account = ''
  form.value.password = ''
  loading.value = false
  registerMode.value = false
  
  // 如果是路由模式且用户手动关闭，跳转到首页或redirect页面
  if (isRouteMode.value && !userStore.isAuthenticated) {
    const redirect = route.query.redirect
    const targetPath = (redirect && typeof redirect === 'string' && redirect !== '/login') 
      ? redirect 
      : '/'
    router.replace(targetPath)
  }
}

const handleKeyPress = (event) => {
  if (event.key === 'Enter' && shouldShow.value) {
    login();
  }
}

onMounted(() => {
  window.addEventListener('keypress', handleKeyPress);
})

onUnmounted(() => {
  window.removeEventListener('keypress', handleKeyPress);
})

const login = async () => {
  // 验证输入
  if (!form.value.account || !form.value.account.trim()) {
    ElMessage.warning('请输入账号');
    return;
  }
  if (!form.value.password || !form.value.password.trim()) {
    ElMessage.warning('请输入密码');
    return;
  }
  
  loading.value = true;
  
  try {
    const response = await loginApi(form.value.account, form.value.password);
    
    console.log('登录响应:', response.data);
    
    // 检查响应数据
    if (response.data && response.data.success && response.data.token) {
      // 存储token和用户基本信息
      userStore.setToken(response.data.token);
      userStore.setUser({
        id: response.data.userId,
        userId: response.data.userId,
        username: response.data.username,
        loginAccount: response.data.loginAccount
      });
      
      // 登录成功后立即获取完整用户资料（包括头像、签名等）
      try {
        const profileResponse = await fetchMyProfile();
        if (profileResponse.data && profileResponse.data.success) {
          const profileData = profileResponse.data;
          const normalizedAvatar = normalizeAvatarUrl(profileData.avatar);
          // 更新用户资料到 store
          userStore.setUser({
            id: profileData.id || response.data.userId,
            userId: profileData.id || response.data.userId,
            username: profileData.username || response.data.username,
            loginAccount: response.data.loginAccount || profileData.account,
            avatar: normalizedAvatar,
            bio: profileData.bio || ''
          });
        }
      } catch (profileError) {
        // 如果获取资料失败，不影响登录流程，只记录错误
        console.warn('获取用户资料失败，但不影响登录:', profileError);
      }
      
      ElMessage.success('登录成功');
      
      // 关闭登录框
      closeLogin();
      
      // 等待token更新和DOM更新后再跳转
      await nextTick();
      // 再等待一小段时间确保token已写入localStorage
      await new Promise(resolve => setTimeout(resolve, 100));
      
      // 跳转到redirect参数指定的页面，或首页
      let redirect = route.query.redirect;
      let targetPath = '/';
      
      // 处理redirect参数
      if (redirect && typeof redirect === 'string') {
        try {
          // 解码redirect参数
          const decodedRedirect = decodeURIComponent(redirect);
          // 确保redirect不是登录页，且不包含/login
          if (decodedRedirect !== '/login' && !decodedRedirect.includes('/login')) {
            targetPath = decodedRedirect;
          }
        } catch (e) {
          console.error('解码redirect参数失败:', e);
        }
      }
      
      console.log('准备跳转到:', targetPath);
      console.log('当前token:', userStore.token);
      console.log('当前路由:', route.path);
      
      // 如果当前在登录页面，使用window.location强制跳转，避免路由守卫拦截
      if (route.path === '/login') {
        // 使用window.location.href强制跳转，避免路由守卫循环
        window.location.href = targetPath;
      } else {
        // 不在登录页面，正常跳转
        router.replace(targetPath).then(() => {
          console.log('跳转成功');
        }).catch(err => {
          console.error('跳转失败:', err);
          // 如果跳转失败，使用window.location强制跳转
          window.location.href = targetPath;
        });
      }
    } else {
      throw new Error(response.data.message || '登录失败');
    }
  } catch (error) {
    console.error('登录失败:', error);
    const errorMessage = error.response?.data?.message || error.message || '登录失败，请检查账号密码';
    ElMessage.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

const onAuthSubmit = async () => {
  if (loading.value) return
  await login()
}

const onRegisterClick = () => {
  registerMode.value = true
  ElMessage({
    type: 'info',
    message: '未注册过的账号，我们将自动帮你注册账号。',
    customClass: 'register-hint-message',
    duration: 2200
  })
}
</script>

<template>
  <teleport to="body">
    <div class="login-container" v-if="shouldShow" @click.self="closeLogin">
      <div class="login-box">
      <!-- 底角装饰：密码输入中使用 dark，否则 bright -->
      <img
        class="corner corner-left"
        :src="isPasswordInputActive ? '/loginleftdark.png' : '/loginleftbright.png'"
        alt=""
        aria-hidden="true"
      />
      <img
        class="corner corner-right"
        :src="isPasswordInputActive ? '/loginrightdark.png' : '/loginrightbright.png'"
        alt=""
        aria-hidden="true"
      />
      <div class="login-main">
        <!-- 左侧背景图 -->
        <div class="login-left">
          <div class="scan-card">
            <div class="scan-title">扫码二维码登录</div>
            <div class="scan-box">
              <img src="/scan.png" alt="扫码登录二维码" class="scan-image" />
            </div>
            <div class="scan-desc">
              <div class="scan-line">
                请使用 <span class="scan-highlight">VideoHub客户端</span>
              </div>
              <div class="scan-line">扫码登录或扫码下载安装 APP</div>
            </div>
          </div>
        </div>

        <!-- 右侧登录表单 -->
        <div class="login-right">
          <!-- 关闭按钮 -->
          <div class="close-button" @click="closeLogin">×</div>

          <!-- 登录标题 -->
          <h2 class="login-title">密码登录</h2>

          <!-- 登录表单 -->
          <div class="login-form">
            <div class="inputs-card">
              <div class="input-row">
                <div class="row-label">账号</div>
                <input
                  type="text"
                  v-model="form.account"
                  placeholder="请输入账号"
                  class="login-input"
                />
              </div>
              <div class="divider"></div>
              <div class="input-row">
                <div class="row-label">密码</div>
                <input
                  :type="passwordVisible ? 'text' : 'password'"
                  v-model="form.password"
                  placeholder="请输入密码"
                  class="login-input"
                  @focus="isPasswordInputActive = true"
                  @blur="isPasswordInputActive = false"
                />
                <button
                  class="pwd-toggle"
                  type="button"
                  :aria-label="passwordVisible ? '隐藏密码' : '显示密码'"
                  @click="passwordVisible = !passwordVisible"
                >
                <svg
                  v-if="passwordVisible"
                  width="20"
                  height="20"
                  viewBox="0 0 20 20"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    fill-rule="evenodd"
                    clip-rule="evenodd"
                    d="M2.11069 9.43732C3.21647 7.77542 5.87904 4.58331 9.89458 4.58331C13.8801 4.58331 16.6483 7.72502 17.8345 9.4049C18.0905 9.76747 18.0905 10.2325 17.8345 10.5951C16.6483 12.2749 13.8801 15.4166 9.89458 15.4166C5.87904 15.4166 3.21647 12.2245 2.11069 10.5626C1.88009 10.2161 1.88009 9.7839 2.11069 9.43732ZM9.89458 3.33331C5.19832 3.33331 2.20919 7.03277 1.07001 8.74489C0.560324 9.51091 0.560323 10.4891 1.07001 11.2551C2.20919 12.9672 5.19832 16.6666 9.89458 16.6666C14.5412 16.6666 17.6368 13.0422 18.8556 11.3161C19.4168 10.5213 19.4168 9.4787 18.8556 8.68391C17.6368 6.95774 14.5412 3.33331 9.89458 3.33331ZM7.29165 9.99998C7.29165 8.50421 8.50421 7.29165 9.99998 7.29165C11.4958 7.29165 12.7083 8.50421 12.7083 9.99998C12.7083 11.4958 11.4958 12.7083 9.99998 12.7083C8.50421 12.7083 7.29165 11.4958 7.29165 9.99998ZM9.99998 6.04165C7.81385 6.04165 6.04165 7.81385 6.04165 9.99998C6.04165 12.1861 7.81385 13.9583 9.99998 13.9583C12.1861 13.9583 13.9583 12.1861 13.9583 9.99998C13.9583 7.81385 12.1861 6.04165 9.99998 6.04165Z"
                    fill="#9499A0"
                  />
                </svg>
                <svg
                  v-else
                  width="20"
                  height="20"
                  viewBox="0 0 20 20"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    fill-rule="evenodd"
                    clip-rule="evenodd"
                    d="M17.5753 6.85456C17.7122 6.71896 17.8939 6.63806 18.0866 6.63806C18.7321 6.63806 19.0436 7.42626 18.5748 7.87006C18.1144 8.30554 17.457 8.69885 16.6478 9.03168L18.1457 10.5296C18.2101 10.5941 18.2613 10.6706 18.2962 10.7548C18.331 10.839 18.349 10.9293 18.349 11.0204C18.349 11.1116 18.331 11.2019 18.2962 11.2861C18.2613 11.3703 18.2101 11.4468 18.1457 11.5113C18.0812 11.5757 18.0047 11.6269 17.9205 11.6618C17.8363 11.6967 17.746 11.7146 17.6548 11.7146C17.5637 11.7146 17.4734 11.6967 17.3892 11.6618C17.305 11.6269 17.2284 11.5757 17.164 11.5113L15.3409 9.68819C15.2898 9.63708 15.247 9.57838 15.2141 9.51428C14.4874 9.71293 13.6876 9.87122 12.8344 9.98119C12.8363 9.99011 12.8381 9.99908 12.8397 10.0081L13.2874 12.5472C13.315 12.7266 13.2713 12.9098 13.1656 13.0573C13.0598 13.2049 12.9005 13.3052 12.7217 13.3367C12.5429 13.3683 12.3589 13.3285 12.2091 13.2259C12.0592 13.1234 11.9555 12.9663 11.9202 12.7882L11.4725 10.2491C11.4645 10.2039 11.4611 10.1581 11.4621 10.1125C10.9858 10.1428 10.4976 10.1586 10.0002 10.1586C9.57059 10.1586 9.14778 10.1468 8.73362 10.1241C8.73477 10.1656 8.7322 10.2074 8.72578 10.249L8.27808 12.7881C8.24612 12.9694 8.14345 13.1306 7.99265 13.2362C7.84186 13.3418 7.65528 13.3831 7.47398 13.3512C7.29268 13.3192 7.1315 13.2166 7.0259 13.0658C6.9203 12.915 6.87892 12.7284 6.91088 12.5471L7.35858 10.008C7.35877 10.007 7.35896 10.0061 7.35915 10.0052C6.50085 9.90284 5.6941 9.75191 4.95838 9.56025C4.93012 9.60634 4.89634 9.64933 4.85748 9.68819L3.03438 11.5113C2.96992 11.5757 2.8934 11.6269 2.80918 11.6618C2.72496 11.6967 2.63469 11.7146 2.54353 11.7146C2.45237 11.7146 2.36211 11.6967 2.27789 11.6618C2.19367 11.6269 2.11714 11.5757 2.05268 11.5113C1.98822 11.4468 1.93709 11.3703 1.90221 11.2861C1.86732 11.2019 1.84937 11.1116 1.84937 11.0204C1.84937 10.9293 1.86732 10.839 1.90221 10.7548C1.93709 10.6706 1.98822 10.5941 2.05268 10.5296L3.49373 9.08855C2.6197 8.744 1.91247 8.33062 1.42559 7.87006C0.956591 7.42636 1.26799 6.63816 1.91359 6.63816C2.10629 6.63816 2.28789 6.71896 2.42489 6.85456C2.70009 7.12696 3.19529 7.45886 3.98459 7.77796C5.54429 8.40856 7.73699 8.77016 10.0001 8.77016C12.2632 8.77016 14.4558 8.40856 16.0156 7.77796C16.8049 7.45886 17.3001 7.12696 17.5753 6.85456Z"
                    fill="#9499A0"
                  />
                </svg>
                </button>
                <a href="#" class="forget-pwd">忘记密码？</a>
              </div>
            </div>

            <div class="btn-row">
              <template v-if="!registerMode">
                <button
                  class="register-btn"
                  type="button"
                  @click="onRegisterClick"
                  :disabled="loading"
                >
                  注册
                </button>
                <button
                  class="login-btn"
                  type="button"
                  @click="login"
                  :disabled="loading"
                >
                  {{ loading ? '登录中...' : '登录' }}
                </button>
              </template>
              <button
                v-else
                class="auth-btn"
                type="button"
                @click="onAuthSubmit"
                :disabled="loading"
              >
                {{ loading ? '登录中...' : '登录/注册' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户协议：独立底部区，居中显示 -->
      <div class="login-bottom">
        <div class="agreement">
          <div class="agreement-topline">未注册过的账号，我们将自动帮你注册账号</div>
          登录或完成注册即代表你同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a>
        </div>
      </div>
      </div>
    </div>
  </teleport>
</template>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.35);
  display: flex;
  justify-content: center;
  align-items: center;
  /* 覆盖 TopHeader/悬浮组件（部分会挂到 body 并带 !important z-index） */
  z-index: 2147483647;
}

.login-box {
  position: relative;
  /* 左侧 400 + 右侧表单（400）+ padding/留白 */
  width: 820px;
  height: 430px;
  background: white;
  border-radius: 8px;
  overflow: hidden; /* 恢复右下角圆角裁切 */
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-main {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  padding: 25px;
}

.login-bottom {
  flex: 0 0 auto;
  padding: 14px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.corner {
  position: absolute;
  bottom: 0;
  width: 130px;
  height: auto;
  pointer-events: none;
  user-select: none;
  /* 角标需要显示在白色背景上层；pointer-events 不影响输入框点击 */
  z-index: 5;
}

.corner-left {
  left: 0;
}

.corner-right {
  right: 0;
}

.login-left {
  width: 350px;
  height: 100%;
  border-radius: 8px 0 0 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: white;
  z-index: 1;
}

.login-left::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  height: 100%;
  width: 1px;
  background-color: #e3e5e7;
}

.scan-card {
  width: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.scan-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.scan-box {
  width: 160px;
  height: 160px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 6px;
  box-sizing: border-box;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.scan-desc {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  line-height: 1.4;
}

.scan-line + .scan-line {
  margin-top: 2px;
}

.scan-highlight {
  color: #00a1d6;
}

.login-right {
  position: relative;
  flex: 1 1 auto;
  min-width: 0;
  padding: 28px 30px;
  box-sizing: border-box;
  background: white;
  border-radius: 0 8px 8px 0;
  z-index: 1;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.login-title {
  text-align: center;
  font-size: 16px;
  color: #333;
  margin-bottom: 20px;
}

.login-form {
  width: 400px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.forget-pwd {
  flex: 0 0 auto;
  margin-left: 12px;
  font-size: 14px;
  color: #00a1d6;
  text-decoration: none;
  white-space: nowrap;
}

.pwd-toggle {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  padding: 0;
  cursor: pointer;
  border-radius: 6px;
  margin-right: -15px;
}

/* 输入框卡片：宽400，高两行各44，中间分割线 */
.inputs-card {
  width: 400px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
}

.input-row {
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  box-sizing: border-box;
  gap: 12px;
}

.row-label {
  flex: 0 0 auto;
  width: 44px;
  color: #212121;
  font-size: 15px;
  font-weight: 400;
  line-height: 44px;
  text-align: left;
}

.divider {
  height: 1px;
  background: #eef2f7;
}

.login-input {
  flex: 1 1 auto;
  height: 44px;
  border: 0;
  outline: none;
  /* 输入文字样式：对齐左侧「账号/密码」标签 */
  font-size: 15px;
  font-weight: 400;
  background: #fff;
  padding: 0;
  line-height: 44px;
  color: #212121;
  min-width: 0;
  margin: 0;
}

.login-input::placeholder {
  color: #9ca3af;
}

/* 避免浏览器自动填充导致背景变黄 */
.login-input:-webkit-autofill,
.login-input:-webkit-autofill:hover,
.login-input:-webkit-autofill:focus {
  -webkit-text-fill-color: #212121;
  transition: background-color 9999s ease-in-out 0s;
  box-shadow: 0 0 0px 1000px #fff inset;
}

.btn-row {
  margin-top: 18px;
  width: 400px;
  display: flex;
  gap: 18px;
}

.register-btn,
.login-btn {
  flex: 1 1 0;
  height: 44px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.register-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #111827;
}

.register-btn:hover:not(:disabled) {
  background: #f5f7fa;
  border-color: #d8dde5;
  box-shadow: 0 4px 12px rgba(17, 24, 39, 0.08);
}

.login-btn {
  border: 1px solid transparent;
  background: #00aeec;
  color: #fff;
}

.login-btn:hover:not(:disabled) {
  background: #33c0f0;
}

.login-btn:active:not(:disabled) {
  background: #1bb8ee;
}

.register-btn:disabled,
.login-btn:disabled {
  background: #99a2aa;
  border-color: #99a2aa;
  color: #fff;
  cursor: not-allowed;
}

.auth-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  border: 1px solid transparent;
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  background: #00aeec;
  color: #fff;
  transition: background-color 0.2s ease, border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.auth-btn:hover:not(:disabled) {
  background: #33c0f0;
}

.auth-btn:active:not(:disabled) {
  background: #1bb8ee;
}

.auth-btn:disabled {
  background: #99a2aa;
  cursor: not-allowed;
}

.agreement {
  width: 100%;
  font-size: 12px;
  color: #99a2aa;
  text-align: center;
  padding: 0 20px;
  box-sizing: border-box;
}

.agreement-topline {
  margin-bottom: 6px;
  font-size: 12px;
  color: #99a2aa;
}

.agreement a {
  color: #00a1d6;
  text-decoration: none;
}
</style>

<style>
.register-hint-message {
  background: #fff !important;
  border: 1px solid #dcdfe6 !important;
  box-shadow: none !important;
  color: #303133 !important;
  filter: none !important;
  z-index: 2147483647 !important;
}

.register-hint-message .el-message__content {
  color: #303133 !important;
}

.register-hint-message .el-message__icon {
  color: #909399 !important;
}

/* 防止该提示条被 login 遮罩暗化 */
.register-hint-message.el-message__wrapper,
.register-hint-message.el-message {
  z-index: 2147483647 !important;
  background: #fff !important;
  box-shadow: none !important;
  filter: none !important;
}
</style>