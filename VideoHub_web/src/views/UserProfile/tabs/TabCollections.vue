<template>
  <div class="tab-collections">
    <section class="section">
      <div class="section-head">
        <h2>收藏夹 · 13</h2>
        <button class="more">查看更多</button>
      </div>
      <div class="grid">
        <div class="item" v-for="i in 8" :key="i">
          <div class="thumb"></div>
          <div class="title">默认收藏夹</div>
          <div class="meta">公开 · 24个视频</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default { name: 'TabCollections' }
</script>

<style lang="scss" scoped>
.tab-collections {
  .section {
    .section-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      h2 { font-size: 16px; margin: 0; }
      .more { background: none; border: none; color: #409eff; cursor: pointer; }
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      @media (max-width: 960px) {
        grid-template-columns: repeat(2, 1fr);
      }
      .item {
        background: #fafafa;
        border-radius: 8px;
        padding: 8px;
        .thumb { height: 120px; background: #eaeaea; border-radius: 6px; }
        .title { margin-top: 6px; font-weight: 600; }
        .meta { color: #888; font-size: 12px; }
      }
    }
  }
}
</style>


