<template>
  <div class="composer">
    <input class="input" placeholder="好的标题更容易获得支持，选题20字" />
    <div class="actions">
      <button class="btn">发布</button>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.composer { background:#fff; border-radius:8px; padding:12px; }
.input { width:100%; border:none; outline:none; background:#f7f8fa; padding:10px 12px; border-radius:6px; }
.actions { display:flex; justify-content:flex-end; margin-top:8px; }
.btn { background:#00aeec; color:#fff; border:none; padding:6px 14px; border-radius:6px; cursor:pointer; }
.btn:hover { opacity:.9; }
</style>


