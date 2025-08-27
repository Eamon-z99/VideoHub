<template>
  <div class="right">
    <section class="card">
      <h4>社区中心</h4>
      <ul>
        <li>公告与规则</li>
        <li>活动广场</li>
        <li>意见反馈</li>
      </ul>
    </section>
    <section class="card">
      <h4>bilibili热搜</h4>
      <ol>
        <li v-for="n in 10" :key="n">热搜条目 {{ n }}</li>
      </ol>
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped>
.right { display:flex; flex-direction:column; gap:12px; }
.card { background:#fff; border-radius:8px; padding:12px; }
.card h4 { margin:0 0 8px; }
.card ul, .card ol { margin:0; padding-left:18px; color:#666; }
</style>


