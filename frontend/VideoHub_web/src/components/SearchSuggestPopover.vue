<template>
  <div
    class="search-suggest-popover"
    role="dialog"
    aria-modal="false"
    @mousedown.stop
  >
    <div class="section" v-if="history && history.length > 0">
      <div class="section-head">
        <div class="section-title">搜索历史</div>
        <button class="clear-btn" type="button" @click="$emit('clear-history')">
          清空
        </button>
      </div>

      <div class="history-list">
        <div
          v-for="kw in displayHistory"
          :key="kw"
          class="history-item"
          @click="$emit('select', kw)"
        >
          <span class="history-text">{{ kw }}</span>
          <button
            class="history-remove"
            type="button"
            aria-label="删除"
            @click.stop="$emit('remove-history', kw)"
          >
            ×
          </button>
        </div>
        <div v-if="hasMoreHistory" class="history-item history-item-ellipsis" aria-hidden="true">
          <span class="history-text">...</span>
        </div>
      </div>

    </div>

    <div class="section hot-section">
      <div class="section-head">
        <div class="section-title">热搜</div>
      </div>

      <div class="hot-grid" v-if="hotKeywords && hotKeywords.length > 0">
        <div
          v-for="(item, idx) in hotKeywords"
          :key="item.keyword"
          class="hot-item"
          @click="$emit('select', item.keyword)"
        >
          <span class="hot-rank" :class="{ muted: idx >= 3 }">{{ idx + 1 }}</span>
          <span class="hot-keyword">{{ item.keyword }}</span>
          <img
            v-if="item.isNew"
            class="hot-badge-new"
            src="/assets/new.png"
            alt="新"
          />
          <img
            v-else-if="item.isHot"
            class="hot-badge-hot"
            src="/assets/hot.png"
            alt="热"
          />
        </div>
      </div>

      <div class="empty" v-else>
        热搜加载中...
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface HotKeywordItem {
  keyword: string
  isNew: boolean
  isHot?: boolean
}

const props = defineProps<{
  history: string[]
  hotKeywords: HotKeywordItem[]
}>()

defineEmits<{
  (e: 'select', keyword: string): void
  (e: 'clear-history'): void
  (e: 'remove-history', keyword: string): void
}>()

const MAX_HISTORY_DISPLAY = 10
const displayHistory = computed(() => (props.history || []).slice(0, MAX_HISTORY_DISPLAY))
const hasMoreHistory = computed(() => (props.history || []).length > MAX_HISTORY_DISPLAY)
</script>

<style scoped lang="scss">
.search-suggest-popover {
  width: 100%;
  box-sizing: border-box;
  background: #ffffff;
  border-radius: 0 0 12px 12px;
  // box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
  // border: 1px solid rgba(2, 132, 199, 0.18);
  border-top: none;
  padding: 12px 10px 10px;
  max-height: none;
  overflow: visible;
}

.section {
  margin-bottom: 10px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.section-title {
  font-weight: 800;
  font-size: 14px;
  color: #6b7280;
}

.clear-btn {
  border: none;
  background: transparent;
  color: #9ca3af;
  font-size: 13px;
  cursor: pointer;
}

.clear-btn:hover {
  color: #00a1d6;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0px 12px;
  overflow: visible;
}

.history-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 6px 10px;
  background: #f3f4f6;
  border-radius: 4px;
  cursor: pointer;
  height: 30px;
  box-sizing: border-box;
  z-index: 1;
}

.history-item-ellipsis {
  cursor: default;
}

.history-text {
  color: #18191C;
  font-size: 12px;
  line-height: 1.2;
  text-align: center;
  display: block;
}

/* 历史部分：hover 只变字颜色，不变背景色 */
.history-item:hover {
  background: #f3f4f6;
}

.history-item:hover .history-text {
  color: #00a1d6;
}

.history-remove {
  position: absolute;
  right: -4px;
  top: -4px;
  border: none;
  background: #c7cbd1;
  color: #ffffff;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  z-index: 20;
}

.history-remove:hover {
  background: #c7cbd1;
  color: #ffffff;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 17px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 6px 6px 6px 2px;
  height: 38px;
  box-sizing: border-box;
  border-radius: 8px;
  cursor: pointer;
  transition: color 0.15s ease;
  background: transparent;
  position: relative;
  z-index: 0;
}

.hot-item::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background: transparent;
  z-index: -1;
}

/* 左列 hover：贴到最左侧边缘 */
.hot-item:nth-child(odd)::before {
  left: -10px;
  right: 0;
}

/* 右列 hover：贴到最右侧边缘 */
.hot-item:nth-child(even)::before {
  left: 0;
  right: -10px;
}

.hot-item:hover::before {
  background: #e6e7e8;
}

.hot-rank {
  font-weight: 400;
  color: #18191C;
  font-size: 14px;
  line-height: 17px;
  height: 17px;
  width: 18px;
  text-align: right;
  margin-right: 6px;
}

.hot-rank.muted {
  color: #9499A0;
}

.hot-keyword {
  color: #18191C;
  font-size: 14px;
  font-weight: 400;
  flex: 0 1 auto;
  max-width: calc(100% - 50px);
  display: block;
  line-height: 17px;
  height: 17px;
  margin-right: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0;
}

.hot-badge-new,
.hot-badge-hot {
  width: 14px;
  height: 14px;
  margin-left: 2px;
  object-fit: contain;
  flex-shrink: 0;
}

.empty {
  padding: 16px 0;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}
</style>

