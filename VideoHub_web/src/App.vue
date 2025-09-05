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
  scrollbar-width: thin; /* 滚动条宽度 */
  scrollbar-color: #c0c0c0 black; /* 滚动条样式 */
}

</style>