<script setup>
import { RouterView } from 'vue-router'
import { onMounted, onUnmounted } from 'vue'

// 监听系统主题变化，实时更新滚动条样式
const updateColorScheme = () => {
  const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  document.documentElement.style.colorScheme = isDark ? 'dark' : 'light'
  document.body.style.colorScheme = isDark ? 'dark' : 'light'
}

onMounted(() => {
  // 初始化
  updateColorScheme()
  
  // 监听主题变化
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  const handleChange = () => {
    updateColorScheme()
  }
  
  if (mediaQuery.addEventListener) {
    mediaQuery.addEventListener('change', handleChange)
  } else {
    // 兼容旧版浏览器
    mediaQuery.addListener(handleChange)
  }
  
  // 清理函数
  onUnmounted(() => {
    if (mediaQuery.removeEventListener) {
      mediaQuery.removeEventListener('change', handleChange)
    } else {
      mediaQuery.removeListener(handleChange)
    }
  })
})
</script>

<template>
  <div class="scroll-container">
    <RouterView />
  </div>
</template>

<style>
html, body, #app { height: 100%; }

/* 使用媒体查询让滚动条实时跟随系统主题 */
@media (prefers-color-scheme: light) {
  html {
    color-scheme: light;
  }
}

@media (prefers-color-scheme: dark) {
  html {
    color-scheme: dark;
  }
}

body { overflow: hidden; }

#app {
  width: 100%;
  margin: 0 auto;
  position: relative;
  background-color: #f5f5f5;
}

.scroll-container {
  position: fixed;
  inset: 0;
  overflow-y: auto;
  overflow-x: auto;
  overscroll-behavior: contain;
  background-color: #f5f5f5;
  /* 确保滚动条使用浏览器原生样式并跟随系统主题 */
  scrollbar-width: auto;
  scrollbar-color: auto;
}

</style>