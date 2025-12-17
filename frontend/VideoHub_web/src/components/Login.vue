<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { loginApi } from '@/api/auth';
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

const closeLogin = () => {
  emit('update:show', false)
  // 清空输入框
  form.value.account = ''
  form.value.password = ''
  loading.value = false
  
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
      // 存储token和用户信息
      userStore.setToken(response.data.token);
      userStore.setUser({
        id: response.data.userId,
        username: response.data.username,
        loginAccount: response.data.loginAccount
      });
      
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
</script>

<template>
  <div class="login-container" v-if="shouldShow" @click.self="closeLogin">
    <div class="login-box">
      <!-- 左侧背景图 -->
      <div class="login-left">
        <img src="/assets/register-bg.png" alt="背景图片" class="background-image" />
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <!-- 关闭按钮 -->
        <div class="close-button" @click="closeLogin">×</div>
        
        <!-- 登录标题 -->
        <h2 class="login-title">密码登录</h2>

        <!-- 登录表单 -->
        <div class="login-form">
          <div class="input-group">
            <input 
              type="text" 
              v-model="form.account" 
              placeholder="请输入账号" 
              class="login-input" 
            />
          </div>
          <div class="input-group">
            <input 
              type="password" 
              v-model="form.password" 
              placeholder="请输入密码" 
              class="login-input" 
            />
            <a href="#" class="forget-pwd">忘记密码？</a>
          </div>
          <button 
            class="login-button" 
            @click="login"
            :disabled="loading"
          >
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <div class="register-link">
            <button class="register-button">注册</button>
          </div>
        </div>

        <!-- 用户协议 -->
        <div class="agreement">
          登录或完成注册即代表你同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.login-box {
  position: relative;
  width: 750px;
  height: 400px;
  background: white;
  border-radius: 8px;
  display: flex;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login-left {
  width: 400px;
  height: 100%;
  overflow: hidden;
  border-radius: 8px 0 0 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: white;
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

.background-image {
  width: 80%;
  height: 80%;
  object-fit: contain;
  position: relative;
  left: 0%;
}

.login-right {
  position: relative;
  width: 350px;
  padding: 20px;
  box-sizing: border-box;
  background: white;
  border-radius: 0 8px 8px 0;
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
  width: 100%;
  padding: 0 20px;
  box-sizing: border-box;
}

.input-group {
  position: relative;
  margin-bottom: 27px;
}

.login-input {
  width: 100%;
  height: 35px;
  border: 1px solid #e3e5e7;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 13px;
  box-sizing: border-box;
}

.login-input:focus {
  border-color: #00a1d6;
  outline: none;
}

.forget-pwd {
  position: absolute;
  right: 0;
  top: -23px;
  font-size: 12px;
  color: #00a1d6;
  text-decoration: none;
}

.login-button {
  width: 100%;
  height: 35px;
  background: #00a1d6;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  margin-top: 15px;
}

.login-button:hover:not(:disabled) {
  background: #00b5e5;
}

.login-button:disabled {
  background: #99a2aa;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 12px;
}

.register-button {
  background: none;
  border: none;
  color: #00a1d6;
  cursor: pointer;
  font-size: 13px;
}

.agreement {
  position: absolute;
  bottom: 20px;
  left: 0;
  width: 100%;
  font-size: 12px;
  color: #99a2aa;
  text-align: center;
  padding: 0 20px;
  box-sizing: border-box;
}

.agreement a {
  color: #00a1d6;
  text-decoration: none;
}
</style>