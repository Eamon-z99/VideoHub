<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="main-app">
    <!-- 主应用导航栏（可选） -->
    <!-- <header class="main-header">
      <div class="header-content">
        <h1>VideoHub 微前端</h1>
        <nav class="main-nav">
          <router-link to="/videohub">视频中心</router-link>
        </nav>
      </div>
    </header> -->
    
    <!-- 主应用路由视图和子应用容器 -->
    <main class="main-content">
      <RouterView />
      <!-- 子应用挂载容器 - qiankun 会将子应用挂载到这里 -->
      <div id="subapp-container" class="subapp-container"></div>
    </main>
  </div>
</template>

<style scoped>
.main-app {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  z-index: 1000;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.main-header h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.main-nav {
  display: flex;
  gap: 20px;
}

.main-nav a {
  text-decoration: none;
  color: #333;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.main-nav a:hover,
.main-nav a.router-link-active {
  background-color: #f0f0f0;
  color: #409eff;
}

.main-content {
  flex: 1;
  overflow: auto;
}

.subapp-container {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

#main-app {
  height: 100%;
}
</style>

