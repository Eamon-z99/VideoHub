<template>
  <div class="user-card">
    <div class="avatar"></div>
    <div class="info">
      <div class="name">小明</div>
      <div class="stats">
        <div><b>32</b><span>关注</span></div>
        <div><b>0</b><span>粉丝</span></div>
        <div><b>0</b><span>动态</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.user-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #ddd;
}
.info { margin-top: 8px; }
.name { font-weight: 600; }
.stats { display: flex; gap: 16px; margin-top: 8px; }
.stats div { display: flex; flex-direction: column; align-items: center; }
.stats span { color: #888; font-size: 12px; }
</style>


