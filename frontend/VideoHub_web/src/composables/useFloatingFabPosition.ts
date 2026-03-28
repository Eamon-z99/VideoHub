import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'

const FLOAT_GAP_PX = 8

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

/**
 * 与 Refresh「换一换」一致：悬浮按钮左缘 = anchor 右缘 + 间距，随滚动/缩放更新。
 */
export function useFloatingFabPosition(getAnchor: () => HTMLElement | null | undefined) {
  const fabLeftPx = ref<number | null>(null)

  let scrollRoots: EventTarget[] = []
  let io: IntersectionObserver | null = null
  let resizeObserver: ResizeObserver | null = null

  function updateFloatingFabPosition() {
    const el = getAnchor()
    if (!el) {
      fabLeftPx.value = null
      return
    }
    const r = el.getBoundingClientRect()
    fabLeftPx.value = Math.round(r.right + FLOAT_GAP_PX)
  }

  function onScrollOrResize() {
    updateFloatingFabPosition()
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

  function teardownResizeObserver() {
    if (resizeObserver) {
      resizeObserver.disconnect()
      resizeObserver = null
    }
  }

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
    teardownResizeObserver()
    if (!el) {
      fabLeftPx.value = null
      return
    }
    setupScrollListeners(el)
    setupIo(el)
    if (typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => {
        updateFloatingFabPosition()
      })
      resizeObserver.observe(el)
    }
    nextTick(() => {
      updateFloatingFabPosition()
    })
  }

  onMounted(() => {
    bindAnchor(getAnchor() ?? null)
    window.addEventListener('resize', onScrollOrResize)
  })

  onUnmounted(() => {
    teardownScrollListeners()
    teardownIo()
    teardownResizeObserver()
    window.removeEventListener('resize', onScrollOrResize)
  })

  watch(
    () => getAnchor(),
    (el) => {
      bindAnchor(el ?? null)
    }
  )

  return { fabLeftPx, updateFloatingFabPosition }
}
