<template>
  <div class="refresh-fab-slot" :class="{ 'refresh-fab-slot--floating-only': floatingOnly }">
    <!-- 吸附在侧栏轨道内；固定到视口时挂到 body，并用与 hero-grid 右缘对齐的 right -->
    <Teleport to="body" :disabled="useDockSlot">
      <button
        type="button"
        class="refresh-fab"
        :class="{
          'is-fixed': !dockInHero || floatingOnly,
          'is-docked': dockInHero && !floatingOnly,
          'is-busy': busy,
          'is-floating-only': floatingOnly
        }"
        aria-label="换一换"
        :disabled="busy"
        :style="fixedInlineStyle"
        @click="$emit('swap')"
      >
        <svg
          class="refresh-fab__icon"
          xmlns="http://www.w3.org/2000/svg"
          width="18"
          height="18"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
        >
          <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
          <path d="M21 3v5h-5" />
          <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16" />
          <path d="M3 21v-5h5" />
        </svg>
        <span class="refresh-fab__chars">
          <span>换</span>
          <span>一</span>
          <span>换</span>
        </span>
      </button>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useFloatingFabPosition } from '@/composables/useFloatingFabPosition'

const props = defineProps<{
  /** 作为参照的区块（如 hero-grid / 视频区）；非 floatingOnly 时用于吸顶对齐；floatingOnly 时用于贴在参照区右缘外侧 */
  anchor?: HTMLElement | null | undefined
  /** 刷新进行中，禁用按钮并显示忙碌态 */
  busy?: boolean
  /** 视口右下角固定悬浮，滚动不消失，不占用侧栏槽位 */
  floatingOnly?: boolean
  /**
   * floatingOnly 时：在 safe-area 底边之上再增加的像素，用于叠在下方另一枚 FAB（如「顶部」）之上
   */
  floatingBottomOffsetPx?: number
}>()

defineEmits<{
  swap: []
}>()

/**
 * true：按钮在侧栏轨道内（与主内容区并排）
 * false：hero 顶部滚出视口 → 固定到视口下沿，right 与 hero-grid 右缘对齐
 */
const dockInHero = ref(true)

/** Teleport 到 body：侧栏槽位模式且当前吸在 hero 内时关闭，避免重复节点 */
const useDockSlot = computed(() => dockInHero.value && !props.floatingOnly)

/** 与 anchor 右缘对齐：距视口右侧的像素（用于 position: fixed，非 floatingOnly） */
const fixedRightPx = ref(24)

const { fabLeftPx } = useFloatingFabPosition(() =>
  props.floatingOnly ? (props.anchor ?? null) : null
)

function shouldDockInHero(el: HTMLElement): boolean {
  const r = el.getBoundingClientRect()
  const h = window.innerHeight
  return r.top >= 0 && r.bottom > 0 && r.top < h
}

function updateDockMode() {
  const el = props.anchor
  if (!el) {
    dockInHero.value = true
    return
  }
  dockInHero.value = shouldDockInHero(el)
}

function updateFixedRightFromAnchor() {
  const el = props.anchor
  if (!el) {
    fixedRightPx.value = 24
    return
  }
  fixedRightPx.value = Math.max(8, window.innerWidth - el.getBoundingClientRect().right)
}

const floatingBottomStyle = computed(() => {
  const base = 'max(16px, env(safe-area-inset-bottom, 0px))'
  const extra =
    typeof props.floatingBottomOffsetPx === 'number' ? props.floatingBottomOffsetPx : 0
  return extra > 0 ? `calc(${base} + ${extra}px)` : base
})

const fixedInlineStyle = computed(() => {
  if (props.floatingOnly) {
    const bottom = floatingBottomStyle.value
    if (fabLeftPx.value != null) {
      return {
        left: `${fabLeftPx.value}px`,
        right: 'auto',
        bottom
      }
    }
    return {
      right: 'max(16px, env(safe-area-inset-right, 0px))',
      bottom
    }
  }
  if (dockInHero.value) return undefined
  return {
    right: `${fixedRightPx.value}px`,
    bottom: '24px'
  }
})

/** App.vue 中实际滚动发生在 .scroll-container 上 */
function collectScrollRoots(anchor: HTMLElement | null): EventTarget[] {
  const set = new Set<EventTarget>()
  set.add(window)
  if (typeof document !== 'undefined') {
    const sc = document.querySelector('.scroll-container')
    if (sc) set.add(sc)
    let p: HTMLElement | null = anchor
    while (p) {
      const st = getComputedStyle(p)
      if (/(auto|scroll|overlay)/.test(st.overflowY) || /(auto|scroll|overlay)/.test(st.overflowX)) {
        set.add(p)
      }
      p = p.parentElement
    }
  }
  return [...set]
}

let scrollRoots: EventTarget[] = []

function onScrollOrResize() {
  if (props.floatingOnly) return
  updateDockMode()
  updateFixedRightFromAnchor()
}

function teardownScrollListeners() {
  for (const t of scrollRoots) {
    t.removeEventListener('scroll', onScrollOrResize)
  }
  scrollRoots = []
}

function teardownIo() {
  if (io) {
    io.disconnect()
    io = null
  }
}

let io: IntersectionObserver | null = null

function setupScrollListeners(anchor: HTMLElement | null) {
  teardownScrollListeners()
  scrollRoots = collectScrollRoots(anchor)
  for (const t of scrollRoots) {
    t.addEventListener('scroll', onScrollOrResize, { passive: true })
  }
}

function setupIo(anchor: HTMLElement) {
  teardownIo()
  io = new IntersectionObserver(
    () => {
      onScrollOrResize()
    },
    {
      root: null,
      threshold: Array.from({ length: 21 }, (_, i) => i / 20)
    }
  )
  io.observe(anchor)
}

function bindAnchor(el: HTMLElement | null) {
  teardownScrollListeners()
  teardownIo()
  if (props.floatingOnly) {
    return
  }
  if (!el) {
    dockInHero.value = true
    fixedRightPx.value = 24
    return
  }
  setupScrollListeners(el)
  setupIo(el)
  nextTick(() => {
    onScrollOrResize()
  })
}

onMounted(() => {
  bindAnchor(props.anchor ?? null)
  window.addEventListener('resize', onScrollOrResize)
})

onUnmounted(() => {
  teardownScrollListeners()
  teardownIo()
  window.removeEventListener('resize', onScrollOrResize)
})

watch(
  () => [props.anchor, props.floatingOnly] as const,
  ([el]) => {
    bindAnchor(el ?? null)
  }
)
</script>

<style scoped lang="scss">
.refresh-fab-slot {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  pointer-events: none;

  &.refresh-fab-slot--floating-only {
    width: 0;
    height: 0;
    overflow: hidden;
    position: absolute;
    inset: auto auto 0 0;
  }
}

.refresh-fab {
  pointer-events: auto;
  position: relative;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 6px;
  min-width: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: #fff;
  color: #18191c;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    box-shadow 0.15s ease;

  &:hover:not(:disabled) {
    background: #fafafa;
    border-color: #d0d0d0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &:active:not(:disabled) {
    background: #f3f4f6;
  }

  &:disabled {
    opacity: 0.65;
    cursor: wait;
  }

  &.is-fixed {
    position: fixed;
    top: auto;
    left: auto;
    z-index: 100;
  }

  &.is-docked {
    top: auto;
    right: auto;
    bottom: auto;
  }

  &.is-busy .refresh-fab__icon {
    animation: refresh-spin 0.8s linear infinite;
  }

  .refresh-fab__icon {
    display: block;
    flex-shrink: 0;
    margin-bottom: 6px;
  }

  .refresh-fab__chars {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 12px;
    font-weight: 500;
    line-height: 1.25;
    user-select: none;

    span {
      display: block;
    }
  }
}

@keyframes refresh-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

<style lang="scss">
.refresh-fab.is-fixed {
  z-index: 9999 !important;
}

/* 首页悬浮：低于全局弹窗，避免盖住对话框；与「顶部」同宽 */
.refresh-fab.is-fixed.is-floating-only {
  z-index: 120 !important;
  box-sizing: border-box;
  width: 40px;
  min-width: 40px;
}
</style>
