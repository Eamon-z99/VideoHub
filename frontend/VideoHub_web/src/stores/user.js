import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { logoutApi } from '@/api/auth';

// 安全读取本地存储，避免被污染或结构不完整时导致异常
function loadInitialState () {
  let initialToken = localStorage.getItem('token') || '';
  let initialUser = {};

  try {
    const raw = localStorage.getItem('user');
    if (raw) {
      const parsed = JSON.parse(raw);
      if (parsed && typeof parsed === 'object') {
        initialUser = parsed;
      }
    }
  } catch (e) {
    // 本地 user 字段被写成了非 JSON（例如 "1"），忽略并清理
    console.warn('解析本地 user 失败，已忽略并清理:', e);
    localStorage.removeItem('user');
    initialUser = {};
  }

  // 如果没有 token，则不信任任何本地 user 信息，统一视为未登录
  if (!initialToken) {
    initialUser = {};
  }

  return { initialToken, initialUser };
}

export const useUserStore = defineStore('user', () => {
  const { initialToken, initialUser } = loadInitialState();

  const token = ref(initialToken);
  const user = ref(initialUser);
  
  const setToken = (newToken) => {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  };
  
  const setUser = (userData) => {
    user.value = userData;
    localStorage.setItem('user', JSON.stringify(userData));
  };
  
  const clear = () => {
    token.value = '';
    user.value = {};
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };
  
  const isAuthenticated = computed(() => !!token.value);
  
  /**
   * 退出登录
   * 调用后端API退出登录，然后清除本地存储的token和用户信息
   */
  const logout = async () => {
    try {
      // 调用后端退出登录接口
      await logoutApi();
    } catch (error) {
      // 即使API调用失败，也清除本地存储
      // 因为JWT是无状态的，客户端清除token即可完成退出
      console.warn('退出登录API调用失败，但继续清除本地存储:', error);
    } finally {
      // 无论API调用成功与否，都清除本地存储
      clear();
    }
  };
  
  return { token, user, setToken, setUser, clear, isAuthenticated, logout };
});