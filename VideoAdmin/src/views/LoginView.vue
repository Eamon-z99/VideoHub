<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { adminLoginApi } from '@/api/adminAuth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  account: '',
  password: '',
})

const rules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value?.validate().catch(() => null)
  if (!form.account.trim() || !form.password) return
  loading.value = true
  try {
    const { data } = await adminLoginApi(form.account.trim(), form.password)
    if (!data.success) {
      ElMessage.error(data.message || '登录失败')
      return
    }
    if (!data.token) {
      ElMessage.error('登录响应缺少 token')
      return
    }
    auth.setSession({
      token: data.token,
      adminId: data.adminId ?? null,
      username: data.displayName ?? data.account ?? '',
      loginAccount: data.account ?? '',
      isAdmin: true,
    })
    const redirect = route.query.redirect
    router.replace(typeof redirect === 'string' && redirect ? redirect : '/')
  } catch {
    /* axios 拦截器已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="card" shadow="hover">
      <h2>VideoHub 管理端</h2>
      <p class="hint">请使用后台管理员账号登录（admins 表，与用户账号无关）</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" placeholder="手机号 / 邮箱 / 用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" class="btn" :loading="loading" native-type="submit" @click="onSubmit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}
.card {
  width: 400px;
  max-width: 92vw;
}
.card h2 {
  margin: 0 0 8px;
  text-align: center;
}
.hint {
  margin: 0 0 20px;
  font-size: 13px;
  color: #909399;
  text-align: center;
}
.btn {
  width: 100%;
  margin-top: 8px;
}
</style>
