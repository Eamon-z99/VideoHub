<template>
  <div class="feed-list">
    <article v-for="n in 6" :key="n" class="feed-card">
      <header class="meta">
        <div class="avatar"></div>
        <div class="who">
          <div class="name">马长林夕-梦</div>
          <div class="sub">44分钟前 · 投稿了视频</div>
        </div>
      </header>
      <div class="content">
        <div class="thumb"></div>
        <div class="title">小丸子说:总是喷痰咳气，幸福会跑掉。</div>
      </div>
      <footer class="actions">
        <span>赞 147</span>
        <span>评 0</span>
        <span>转 15</span>
      </footer>
    </article>
  </div>
  
</template>

<script setup>
</script>

<style scoped>
.feed-list { display:flex; flex-direction:column; gap:12px; }
.feed-card { background:#fff; border-radius:8px; padding:12px; }
.meta { display:flex; align-items:center; gap:10px; }
.avatar { width:36px; height:36px; border-radius:50%; background:#ddd; }
.who .name { font-weight:600; }
.who .sub { color:#888; font-size:12px; }
.content { display:flex; gap:12px; margin-top:10px; }
.thumb { width:180px; height:100px; background:#d9dee3; border-radius:6px; }
.title { font-size:14px; line-height:1.6; }
.actions { display:flex; gap:16px; color:#666; font-size:13px; margin-top:8px; }
</style>


