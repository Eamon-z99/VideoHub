<template>
  <div class="stories">
    <div v-for="n in 10" :key="n" class="story">
      <div class="bubble"></div>
      <div class="label">好友{{ n }}</div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.stories {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  overflow-x: auto;
}
.story { display:flex; flex-direction:column; align-items:center; gap:6px; }
.bubble { width:48px; height:48px; border-radius:50%; background:#e3e9ee; }
.label { font-size:12px; color:#666; }
</style>


