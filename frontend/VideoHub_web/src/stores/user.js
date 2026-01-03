import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { logoutApi } from '@/api/auth';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'));
  
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