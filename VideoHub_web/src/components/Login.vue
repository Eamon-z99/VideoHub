<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import axios from '@/utils/request';

const router = useRouter();
const userStore = useUserStore();

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:show'])

const form = ref({
  account: '',
  password: ''
});

const closeLogin = () => {
  emit('update:show', false)
  // 清空输入框
  form.value.account = ''
  form.value.password = ''
}

const login = async () => {
  if (!form.value.account || !form.value.password) {
    alert('请输入账号和密码')
    return
  }
  
  try {
    const response = await axios.post('/api/auth/login', form.value);
    
    // 存储token和用户信息
    userStore.setToken(response.data.token);
    userStore.setUser({
      id: response.data.userId,
      username: response.data.username,
      loginAccount: response.data.loginAccount
    });
    
    // 关闭登录框
    closeLogin();
    // 跳转到首页
    router.push('/');
  } catch (error) {
    console.error('登录失败:', error.response?.data?.message || error.message);
    alert(`登录失败: ${error.response?.data?.message || '请检查账号密码'}`);
  }
};
</script>

<template>
  <div class="login-container" v-if="show" @click.self="closeLogin">
    <div class="login-box">
      <!-- 左侧背景图 -->
      <div class="login-left">
        <img src="../assets/register-bg.png" alt="背景图片" class="background-image" />
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
          <button class="login-button" @click="login">登录</button>
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
  margin-bottom: 20px;
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
  top: -18px;
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

.login-button:hover {
  background: #00b5e5;
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