import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { listWatchLater, addWatchLater, removeWatchLater } from '@/api/watchLater'

export const useWatchLaterStore = defineStore('watchLater', () => {
  const items = ref([])
  const total = ref(0)
  const loading = ref(false)

  const videoIdSet = computed(() => new Set((items.value || []).map((i) => i.videoId)))

  async function load(userId) {
    if (!userId) {
      items.value = []
      total.value = 0
      return
    }
    loading.value = true
    try {
      const { data } = await listWatchLater(userId, 1, 100)
      if (data?.success) {
        items.value = Array.isArray(data.list) ? data.list : []
        total.value = typeof data.total === 'number' ? data.total : items.value.length
      }
    } catch {
      items.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  function isInList(videoId) {
    if (!videoId) return false
    return videoIdSet.value.has(String(videoId))
  }

  async function add(userId, videoId) {
    const { data } = await addWatchLater(userId, videoId)
    if (data?.success) {
      await load(userId)
    }
    return data
  }

  async function remove(userId, videoId) {
    const { data } = await removeWatchLater(userId, videoId)
    if (data?.success) {
      await load(userId)
    }
    return data
  }

  return {
    items,
    total,
    loading,
    load,
    add,
    remove,
    isInList
  }
})
