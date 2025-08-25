<template>
  <nav class="profile-tabs">
    <button v-for="t in tabs" :key="t.key" class="tab" :class="{active: t.key===active}" @click="$emit('change', t.key)">
      {{ t.label }}
    </button>
  </nav>
</template>

<script>
export default {
  name: 'ProfileTabs',
  props: { active: { type: String, default: 'collections' } },
  data () {
    return {
      tabs: [
        { key: 'collections', label: '收藏夹' },
        { key: 'dynamics', label: '动态' },
        { key: 'albums', label: '合集' }
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  .tab {
    padding: 8px 14px;
    border-radius: 18px;
    border: 1px solid #eee;
    background: #fff;
    cursor: pointer;
    &.active { background: #222; color: #fff; border-color: #222; }
  }
}
</style>


