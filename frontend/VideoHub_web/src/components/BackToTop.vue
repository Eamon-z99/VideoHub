<template>
  <div class="back-to-top-slot">
    <Teleport to="body">
      <button
        v-show="visible"
        type="button"
        class="back-to-top-fab"
        aria-label="回到顶部"
        :style="fixedInlineStyle"
        @click="scrollToTop"
      >
        <svg
          class="back-to-top-fab__arrow"
          width="12"
          height="10"
          viewBox="0 0 12 10"
          aria-hidden="true"
        >
          <polygon points="6,0 12,10 0,10" fill="currentColor" />
        </svg>
        <span class="back-to-top-fab__label">顶部</span>
      </button>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useFloatingFabPosition } from '@/composables/useFloatingFabPosition'

const props = defineProps<{
  /** 与 Refresh 一致：贴在主内容区右缘外侧 */
  anchor?: HTMLElement | null | undefined
  /** 相对 safe-area 底边的额外上移量（px）；首页与「换一换」叠放时一般为 0（自身贴底） */
  bottomOffsetPx?: number
  /** 滚动超过该距离后显示按钮 */
  showAfterScrollPx?: number
}>()

const bottomOffsetPx = computed(() =>
  typeof props.bottomOffsetPx === 'number' ? props.bottomOffsetPx : 0
)
const showAfterScrollPx = computed(() =>
  typeof props.showAfterScrollPx === 'number' ? props.showAfterScrollPx : 280
)

const { fabLeftPx } = useFloatingFabPosition(() => props.anchor ?? null)

const visible = ref(false)

function getScrollTop(): number {
  const sc = document.querySelector('.scroll-container') as HTMLElement | null
  if (sc) return sc.scrollTop
  return window.scrollY || document.documentElement.scrollTop || 0
}

function updateVisible() {
  visible.value = getScrollTop() >= showAfterScrollPx.value
}

function scrollToTop() {
  const sc = document.querySelector('.scroll-container') as HTMLElement | null
  if (sc) {
    sc.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const fixedInlineStyle = computed(() => {
  const baseBottom = `max(16px, env(safe-area-inset-bottom, 0px))`
  if (fabLeftPx.value != null) {
    return {
      left: `${fabLeftPx.value}px`,
      right: 'auto',
      bottom: `calc(${baseBottom} + ${bottomOffsetPx.value}px)`
    }
  }
  return {
    right: 'max(16px, env(safe-area-inset-right, 0px))',
    bottom: `calc(${baseBottom} + ${bottomOffsetPx.value}px)`
  }
})

let scrollRoots: EventTarget[] = []

function collectScrollRoots(): EventTarget[] {
  const set = new Set<EventTarget>()
  set.add(window)
  const sc = document.querySelector('.scroll-container')
  if (sc) set.add(sc)
  return [...set]
}

onMounted(() => {
  updateVisible()
  scrollRoots = collectScrollRoots()
  for (const t of scrollRoots) {
    t.addEventListener('scroll', updateVisible, { passive: true })
  }
})

onUnmounted(() => {
  for (const t of scrollRoots) {
    t.removeEventListener('scroll', updateVisible)
  }
  scrollRoots = []
})
</script>

<style scoped lang="scss">
.back-to-top-slot {
  width: 0;
  height: 0;
  overflow: hidden;
  position: absolute;
  inset: auto auto 0 0;
  pointer-events: none;
}
</style>

<style scoped lang="scss">
.back-to-top-fab {
  pointer-events: auto;
  position: fixed;
  top: auto;
  z-index: 119;
  box-sizing: border-box;
  margin: 0;
  padding: 10px 6px;
  width: 40px;
  min-width: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: #fff;
  color: #18191c;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    opacity 0.2s ease;

  &:hover {
    background: #fafafa;
    border-color: #d0d0d0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &:active {
    background: #f3f4f6;
  }

  .back-to-top-fab__arrow {
    display: block;
    flex-shrink: 0;
    margin-bottom: 6px;
    color: #18191c;
  }

  .back-to-top-fab__label {
    font-size: 12px;
    font-weight: 500;
    line-height: 1.25;
    user-select: none;
  }
}
</style>
