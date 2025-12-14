<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Login from '@/components/Login.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const show = ref(true)

onMounted(() => {
  // 如果从其它页面跳转而来，保持弹窗显示
  show.value = true
})

const handleClosed = () => {
  // 只有在用户手动关闭（非登录成功）时才跳转
  // 登录成功时，Login组件会自己处理跳转
  if (!userStore.isAuthenticated) {
    const redirect = route.query.redirect
    if (redirect && typeof redirect === 'string') {
      router.replace(redirect)
    } else {
      router.replace('/')
    }
  }
}
</script>

<template>
  <div>
    <Login v-model:show="show" @update:show="val => { if (!val) handleClosed() }" />
  </div>
</template>

<style scoped>
/* 页面由登录弹窗主导，无额外样式 */
</style>

