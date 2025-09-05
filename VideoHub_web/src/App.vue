<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="scroll-container">
    <RouterView />
  </div>
</template>

<style>
html, body, #app { height: 100%; }
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
}

/* 自定义容器滚动条（深色背景轨道） */
.scroll-container::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
.scroll-container::-webkit-scrollbar-track {
  background: #c0c0c0;
}
.scroll-container::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 8px;
  border: 2px solid #c0c0c0;
}
.scroll-container::-webkit-scrollbar-thumb:hover {
  background-color: #c0c0c0;
}

.scroll-container { scrollbar-width: thin; scrollbar-color: #c0c0c0 black }

</style>