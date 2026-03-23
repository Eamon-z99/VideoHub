import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchAdminMe } from '@/api/admin'

const STORAGE_KEY = 'videohub_admin_auth'

type Persisted = {
  token: string
  adminId: number | null
  username: string
  loginAccount: string
  isAdmin: boolean
}

function loadPersisted(): Persisted | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const p = JSON.parse(raw) as Record<string, unknown>
    const adminIdRaw = p.adminId ?? p.userId
    return {
      token: typeof p.token === 'string' ? p.token : '',
      adminId:
        adminIdRaw != null && !Number.isNaN(Number(adminIdRaw)) ? Number(adminIdRaw) : null,
      username: typeof p.username === 'string' ? p.username : '',
      loginAccount: typeof p.loginAccount === 'string' ? p.loginAccount : '',
      isAdmin: Boolean(p.isAdmin),
    }
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const persisted = loadPersisted()
  const token = ref(persisted?.token ?? '')
  const adminId = ref<number | null>(persisted?.adminId ?? null)
  const username = ref(persisted?.username ?? '')
  const loginAccount = ref(persisted?.loginAccount ?? '')
  const isAdmin = ref(Boolean(persisted?.isAdmin))

  const loggedIn = computed(() => Boolean(token.value))

  function persist() {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: token.value,
        adminId: adminId.value,
        username: username.value,
        loginAccount: loginAccount.value,
        isAdmin: isAdmin.value,
      }),
    )
  }

  function setSession(payload: {
    token: string
    adminId: number | null
    username: string
    loginAccount: string
    isAdmin: boolean
  }) {
    token.value = payload.token
    adminId.value = payload.adminId
    username.value = payload.username
    loginAccount.value = payload.loginAccount
    isAdmin.value = payload.isAdmin
    persist()
  }

  function clear() {
    token.value = ''
    adminId.value = null
    username.value = ''
    loginAccount.value = ''
    isAdmin.value = false
    localStorage.removeItem(STORAGE_KEY)
  }

  async function hydrateFromServer() {
    if (!token.value) return
    try {
      const { data } = await fetchAdminMe()
      if (data.success && data.isAdmin) {
        const aid = data.adminId
        adminId.value = aid != null && !Number.isNaN(Number(aid)) ? Number(aid) : null
        username.value = data.displayName || data.username || ''
        loginAccount.value = data.loginAccount ?? ''
        isAdmin.value = true
        persist()
      } else {
        clear()
        redirectToLogin()
      }
    } catch {
      clear()
      redirectToLogin()
    }
  }

  function redirectToLogin() {
    const path = window.location.pathname + window.location.search
    if (!path.startsWith('/login')) {
      window.location.replace(`/login?redirect=${encodeURIComponent(path)}`)
    }
  }

  return {
    token,
    adminId,
    username,
    loginAccount,
    isAdmin,
    loggedIn,
    setSession,
    clear,
    hydrateFromServer,
  }
})
