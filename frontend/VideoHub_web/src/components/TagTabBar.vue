<template>
  <nav class="tag-tab-bar" aria-label="分区标签">
    <router-link
      v-for="t in VIDEO_NAV_TAGS"
      :key="t"
      :to="{ name: 'tag-feed', params: { tag: t } }"
      class="tag-pill"
      :class="{ 'is-active': t === activeTag }"
    >
      {{ t }}
    </router-link>
  </nav>
</template>

<script setup>
import { VIDEO_NAV_TAGS } from '@/constants/videoNavTags'

defineProps({
  /** 当前选中的标签文案，与路由 params.tag 一致 */
  activeTag: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
.tag-tab-bar {
  display: grid;
  grid-template-columns: repeat(11, 1fr);
  grid-template-rows: repeat(2, auto);
  gap: 8px;
  width: 100%;
  box-sizing: border-box;
}

.tag-pill {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 31px;
  padding: 8px 12px;
  background-color: #f6f7f8;
  border-radius: 8px;
  text-decoration: none;
  color: #61666d;
  font-size: 14px;
  letter-spacing: 2px;
  white-space: nowrap;
  transition:
    color 0.2s,
    background-color 0.2s,
    border-color 0.2s;
  border: 1px solid transparent;
  box-sizing: border-box;

  &:hover {
    color: #18191c;
    background-color: #eceff2;
  }

  &.is-active,
  &.router-link-active {
    color: #fff;
    background-color: #00a1d6;
    border-color: #00a1d6;
  }

  &.router-link-active:hover {
    color: #fff;
    background-color: #00b5e5;
  }
}
</style>
