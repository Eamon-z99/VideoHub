<template>
  <div class="messages-page">
    <div class="header-wrapper">
      <TopHeader :fixed-on-scroll="false" :transparent-at-top="false" />
    </div>
    <div class="container">
      <main class="main">
        <div class="topbar">
          <div class="search">
            <i class="icon search-icon" />
            <input v-model="keyword" placeholder="我的消息" />
          </div>
        </div>

        <section class="board">
          <!-- 左侧：关注好友 / 会话列表 -->
          <div class="left-list">
            <div class="left-list-header">
              <div class="title">最近消息</div>
            </div>
            <div
              v-for="contact in filteredContacts"
              :key="contact.userId"
              class="chat-item"
              :class="{ active: activeContact && activeContact.userId === contact.userId }"
              @click="selectContact(contact)"
            >
              <div
                class="avatar"
                :style="contact.avatar ? { backgroundImage: `url(${contact.avatar})`, backgroundSize: 'cover' } : {}"
              />
              <div class="info">
                <div class="title">
                  <span class="name">{{ contact.username }}</span>
                  <span v-if="contact.lastTime" class="time">
                    {{ contact.lastTime }}
                  </span>
                  <span v-if="contact.unreadCount > 0" class="unread">{{ contact.unreadCount }}</span>
                </div>
                <div class="snippet">
                  {{ contact.lastContent || '还没有开始聊天' }}
                </div>
              </div>
            </div>
            <div v-if="!loadingContacts && filteredContacts.length === 0" class="no-contacts">
              还没有关注的好友，去动态里关注一些吧～
            </div>
          </div>

          <!-- 右侧：当前会话 -->
          <div class="right-dialog">
            <div v-if="!activeContact" class="empty">
              <div class="illustration">
                <img src="/assets/messages.png" alt="empty" />
              </div>
              <div class="empty-tip">选中左侧的好友，开始私信聊天吧（・`ω´･）</div>
            </div>
            <div v-else class="room">
              <div class="room-header">
                <span class="room-name">{{ activeContact.username }}</span>
              </div>
              <div class="room-content">
                <template v-for="(msg, index) in messageList" :key="msg.id">
                  <template v-if="shouldShowTime(msg, index)">
                    <div class="msg-time-divider">
                      <span class="msg-time-text">{{ formatTime(msg.createTime) }}</span>
                    </div>
                  </template>
                  <div
                    class="msg-wrapper"
                    :class="msg.senderId === activeContact.userId ? 'left' : 'right'"
                  >
                    <div class="msg-avatar">
                      <img
                        v-if="getSenderAvatar(msg.senderId)"
                        :src="getSenderAvatar(msg.senderId)"
                        :alt="getSenderName(msg.senderId)"
                      />
                    </div>
                    <div class="msg-content-wrapper">
                      <div class="msg-bubble">
                        <div v-if="isImageMessage(msg.content)" class="msg-image">
                          <img :src="getImageUrl(msg.content)" alt="图片" @error="handleImageError" @click="previewImage(getImageUrl(msg.content))" />
                        </div>
                        <div v-else class="msg-text" v-html="formatMessageContent(msg.content)"></div>
                      </div>
                    </div>
                  </div>
                </template>
                <div v-if="loadingHistory" class="loading-tip">加载中...</div>
              </div>
              <div class="room-editor">
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/*"
                  @change="handleImageSelect"
                  style="display: none;"
                />
                <div class="editor-tools">
                  <button class="tool" type="button" title="图片" aria-label="图片" @click="triggerFileSelect">
                    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                      <path
                        fill="currentColor"
                        d="M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2Zm0 16H5V5h14v14Zm-3-2H8l2.5-3.2 1.8 2.2 1.2-1.5L16 17Zm-6.5-6A1.5 1.5 0 1 0 8 9.5 1.5 1.5 0 0 0 9.5 11Z"
                      />
                    </svg>
                  </button>
                  <button class="tool" type="button" title="表情" aria-label="表情" @click="toggleEmojiPanel">
                    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
                      <path
                        fill="currentColor"
                        d="M12 22a10 10 0 1 1 0-20 10 10 0 0 1 0 20Zm0-18a8 8 0 1 0 0 16 8 8 0 0 0 0-16Zm-3 6a1.25 1.25 0 1 1 0-2.5A1.25 1.25 0 0 1 9 10Zm6 0a1.25 1.25 0 1 1 0-2.5A1.25 1.25 0 0 1 15 10Zm-8 3.2a1 1 0 0 1 1.4.2 4.5 4.5 0 0 0 7.2 0 1 1 0 0 1 1.6 1.2 6.5 6.5 0 0 1-10.4 0 1 1 0 0 1 .2-1.4Z"
                      />
                    </svg>
                  </button>
                </div>
                <div class="editor-input-wrapper">
                  <textarea
                    v-model="draft"
                    placeholder="请输入消息内容"
                    @keydown.enter.exact.prevent="send"
                    @keydown.enter.shift.exact=""
                    maxlength="500"
                    rows="1"
                    ref="textareaRef"
                  ></textarea>
                  <div v-if="showEmojiPanel" class="emoji-panel">
                    <div class="emoji-list">
                      <span
                        v-for="emoji in emojiList"
                        :key="emoji"
                        class="emoji-item"
                        @click="insertEmoji(emoji)"
                      >{{ emoji }}</span>
                    </div>
                  </div>
                  <div class="editor-footer">
                    <div class="char-count">{{ draft.length }}/500</div>
                    <button class="send" type="button" @click="send">发送</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import { useUserStore } from '@/stores/user'
import { fetchContacts, fetchMessageHistory, sendPrivateMessage, fetchPeerInfo } from '@/api/messages'
import { getFollowingUsers } from '@/api/follow'
import { uploadImage } from '@/api/feed'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const tab = ref('mine')
const keyword = ref('')
const activeContactId = ref(null)
const draft = ref('')

const contactList = ref([])
const loadingContacts = ref(false)
const loadingHistory = ref(false)
const messageList = ref([])
const textareaRef = ref(null)
const fileInputRef = ref(null)
const showEmojiPanel = ref(false)
const uploadingImage = ref(false)

// 本地缓存：存储所有联系人的聊天记录 { peerId: [messages] }
const messageCache = ref(new Map())
// 记录每个联系人的最后加载时间，用于判断是否需要刷新
const lastLoadTime = ref(new Map())

// 常用表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
  '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
  '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔',
  '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
  '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮',
  '🤧', '🥵', '🥶', '😶‍🌫️', '😵', '😵‍💫', '🤯', '🤠', '🥳', '😎',
  '🤓', '🧐', '😕', '😟', '🙁', '😮', '😯', '😲', '😳', '🥺',
  '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣',
  '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈',
  '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾',
  '🤖', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾'
]

const filteredContacts = computed(() => {
  if (!keyword.value) return contactList.value
  const kw = keyword.value.trim()
  if (!kw) return contactList.value
  return contactList.value.filter(
    (c) =>
      (c.username && c.username.includes(kw)) ||
      (c.lastContent && c.lastContent.includes(kw))
  )
})

const activeContact = computed(() =>
  contactList.value.find((c) => c.userId === activeContactId.value) || null
)

const isAuthed = computed(() => !!userStore.token)

const normalizedAvatar = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
  if (avatar.startsWith('/')) return avatar
  return '/' + avatar
}

async function loadContacts() {
  if (!isAuthed.value) {
    contactList.value = []
    return
  }
  loadingContacts.value = true
  try {
    // 优先使用后端会话列表（含未读数、最后一条消息）
    const { data } = await fetchContacts()
    if (data && data.success && Array.isArray(data.data)) {
      contactList.value = data.data.map((item) => ({
        userId: item.userId,
        username: item.username,
        avatar: normalizedAvatar(item.avatar),
        lastContent: item.lastContent,
        lastTime: item.lastTime,
        unreadCount: item.unreadCount || 0,
      }))
      // 如果没有任何历史私信，再补充一次关注好友列表，至少让左侧有联系人
      if (contactList.value.length === 0) {
        await fallbackContactsFromFollowing()
      } else {
        // 预加载所有联系人的聊天记录
        preloadAllMessages()
      }
    } else {
      await fallbackContactsFromFollowing()
    }
  } catch (e) {
    console.error('加载会话列表失败:', e)
    await fallbackContactsFromFollowing()
  } finally {
    loadingContacts.value = false
  }
}

async function fallbackContactsFromFollowing() {
  try {
    const { data } = await getFollowingUsers()
    if (data && data.success && Array.isArray(data.users)) {
      contactList.value = data.users.map((u) => ({
        userId: u.id,
        username: u.username || u.nickname || '好友',
        avatar: normalizedAvatar(u.avatar),
        lastContent: '',
        lastTime: null,
        unreadCount: 0,
      }))
    } else {
      contactList.value = []
    }
  } catch (e) {
    console.error('加载关注好友失败:', e)
    contactList.value = []
  }
}

// 从缓存加载聊天记录
async function loadHistoryFromCache(peerId) {
  const cached = messageCache.value.get(peerId)
  if (cached && cached.length > 0) {
    messageList.value = [...cached]
    await nextTick()
    scrollToBottom()
    return true
  }
  return false
}

// 加载聊天记录（优先从缓存，必要时从接口）
async function loadHistory(peerId, forceRefresh = false) {
  if (!peerId || !isAuthed.value) return
  
  // 如果不是强制刷新，先尝试从缓存加载
  if (!forceRefresh && loadHistoryFromCache(peerId)) {
    // 将该联系人的未读数清零
    const target = contactList.value.find((c) => c.userId === peerId)
    if (target) target.unreadCount = 0
    return
  }
  
  loadingHistory.value = true
  try {
    const { data } = await fetchMessageHistory(peerId, 1, 100)
    if (data && data.success && Array.isArray(data.data)) {
      const messages = data.data.map((m) => ({
        id: m.id,
        senderId: m.senderId,
        receiverId: m.receiverId,
        content: m.content,
        createTime: m.createTime,
      }))
      
      // 更新缓存
      messageCache.value.set(peerId, messages)
      lastLoadTime.value.set(peerId, Date.now())
      
      messageList.value = messages
      // 将该联系人的未读数清零
      const target = contactList.value.find((c) => c.userId === peerId)
      if (target) target.unreadCount = 0
      await nextTick()
      scrollToBottom()
    } else {
      messageList.value = []
      messageCache.value.set(peerId, [])
    }
  } catch (e) {
    console.error('加载聊天记录失败:', e)
    messageList.value = []
  } finally {
    loadingHistory.value = false
  }
}

// 预加载所有联系人的聊天记录
async function preloadAllMessages() {
  if (!isAuthed.value || contactList.value.length === 0) return
  
  // 并发加载所有联系人的聊天记录（限制并发数）
  const peerIds = contactList.value.map(c => c.userId)
  const batchSize = 5 // 每批加载5个
  const batches = []
  
  for (let i = 0; i < peerIds.length; i += batchSize) {
    batches.push(peerIds.slice(i, i + batchSize))
  }
  
  for (const batch of batches) {
    await Promise.all(
      batch.map(async (peerId) => {
        // 如果缓存中有数据且不超过5分钟，跳过
        const lastTime = lastLoadTime.value.get(peerId)
        if (lastTime && Date.now() - lastTime < 5 * 60 * 1000) {
          return
        }
        
        try {
          const { data } = await fetchMessageHistory(peerId, 1, 100)
          if (data && data.success && Array.isArray(data.data)) {
            const messages = data.data.map((m) => ({
              id: m.id,
              senderId: m.senderId,
              receiverId: m.receiverId,
              content: m.content,
              createTime: m.createTime,
            }))
            messageCache.value.set(peerId, messages)
            lastLoadTime.value.set(peerId, Date.now())
          }
        } catch (e) {
          console.error(`预加载 ${peerId} 的聊天记录失败:`, e)
        }
      })
    )
  }
}

function selectContact(contact) {
  activeContactId.value = contact.userId
  loadHistory(contact.userId)
}

function adjustTextareaHeight() {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 200) + 'px'
  }
}

function triggerFileSelect() {
  fileInputRef.value?.click()
}

function toggleEmojiPanel() {
  showEmojiPanel.value = !showEmojiPanel.value
}

function closeEmojiPanel() {
  showEmojiPanel.value = false
}

function insertEmoji(emoji) {
  if (draft.value.length + emoji.length > 500) {
    ElMessage.warning('消息内容不能超过500字符')
    return
  }
  const textarea = textareaRef.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    draft.value = draft.value.substring(0, start) + emoji + draft.value.substring(end)
    nextTick(() => {
      textarea.focus()
      textarea.setSelectionRange(start + emoji.length, start + emoji.length)
      adjustTextareaHeight()
    })
  } else {
    draft.value += emoji
    adjustTextareaHeight()
  }
  showEmojiPanel.value = false
}

async function handleImageSelect(event) {
  const file = event.target.files?.[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    event.target.value = ''
    return
  }
  
  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    event.target.value = ''
    return
  }
  
  uploadingImage.value = true
  try {
    const response = await uploadImage(file)
    if (response.data && response.data.success) {
      const imageUrl = response.data.url
      // 将图片URL作为消息内容发送
      await sendImageMessage(imageUrl)
    } else {
      throw new Error(response.data?.message || '上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error(`图片上传失败: ${error.response?.data?.message || error.message}`)
  } finally {
    uploadingImage.value = false
    event.target.value = ''
  }
}

async function sendImageMessage(imageUrl) {
  if (!activeContactId.value) return
  if (!isAuthed.value) {
    ElMessage.warning('请先登录后再发送私信')
    return
  }
  try {
    // 使用特殊格式标记图片消息
    const imageContent = `[IMAGE:${imageUrl}]`
    const { data } = await sendPrivateMessage(activeContactId.value, imageContent)
    if (data && data.success && data.data) {
      const msg = data.data
      const newMessage = {
        id: msg.id,
        senderId: msg.senderId,
        receiverId: msg.receiverId,
        content: msg.content,
        createTime: msg.createTime,
      }
      messageList.value.push(newMessage)
      
      // 更新缓存
      const cached = messageCache.value.get(activeContactId.value) || []
      cached.push(newMessage)
      messageCache.value.set(activeContactId.value, cached)
      
      const target = contactList.value.find((c) => c.userId === activeContactId.value)
      if (target) {
        target.lastContent = '[图片]'
        target.lastTime = msg.createTime
      }
      await nextTick()
      scrollToBottom()
    }
  } catch (e) {
    console.error('发送图片失败:', e)
    ElMessage.error('发送图片失败')
  }
}

function isImageMessage(content) {
  return content && content.startsWith('[IMAGE:') && content.endsWith(']')
}

function getImageUrl(content) {
  if (isImageMessage(content)) {
    return content.slice(7, -1)
  }
  return ''
}

function handleImageError(event) {
  event.target.src = '/assets/placeholder-image.png'
  event.target.onerror = null
}

function previewImage(imageUrl) {
  // 简单的图片预览，可以后续扩展为模态框
  window.open(imageUrl, '_blank')
}

function formatMessageContent(content) {
  if (!content) return ''
  // 将表情符号和文本正常显示
  // 可以在这里添加更多格式化逻辑，比如链接识别等
  return content
    .replace(/\n/g, '<br>')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

async function send() {
  if (!activeContactId.value) return
  const text = draft.value.trim()
  if (!text) return
  if (!isAuthed.value) {
    // 简单提示，沿用登录逻辑
    alert('请先登录后再发送私信')
    return
  }
  try {
    const { data } = await sendPrivateMessage(activeContactId.value, text)
    if (data && data.success && data.data) {
      const msg = data.data
      const newMessage = {
        id: msg.id,
        senderId: msg.senderId,
        receiverId: msg.receiverId,
        content: msg.content,
        createTime: msg.createTime,
      }
      messageList.value.push(newMessage)
      
      // 更新缓存
      const cached = messageCache.value.get(activeContactId.value) || []
      cached.push(newMessage)
      messageCache.value.set(activeContactId.value, cached)
      
      draft.value = ''
      adjustTextareaHeight()
      showEmojiPanel.value = false
      const target = contactList.value.find((c) => c.userId === activeContactId.value)
      if (target) {
        target.lastContent = text.length > 20 ? text.substring(0, 20) + '...' : text
        target.lastTime = msg.createTime
      }
      await nextTick()
      scrollToBottom()
    }
  } catch (e) {
    console.error('发送消息失败:', e)
    ElMessage.error('发送消息失败')
  }
}

function getSenderAvatar(senderId) {
  const currentUserId = userStore.user?.id || userStore.user?.userId
  if (senderId === currentUserId) {
    return userStore.user?.avatar ? normalizedAvatar(userStore.user.avatar) : ''
  }
  const contact = contactList.value.find(c => c.userId === senderId)
  return contact ? contact.avatar : ''
}

function getSenderName(senderId) {
  const currentUserId = userStore.user?.id || userStore.user?.userId
  if (senderId === currentUserId) {
    return userStore.user?.username || '我'
  }
  const contact = contactList.value.find(c => c.userId === senderId)
  return contact ? contact.username : '未知用户'
}

function shouldShowTime(msg, index) {
  if (index === 0) return true
  const prevMsg = messageList.value[index - 1]
  if (!prevMsg || !prevMsg.createTime || !msg.createTime) return true
  
  const currentTime = new Date(msg.createTime).getTime()
  const prevTime = new Date(prevMsg.createTime).getTime()
  const diffMinutes = Math.abs(currentTime - prevTime) / (1000 * 60)
  
  // 如果时间差超过5分钟，显示时间
  return diffMinutes > 5
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  // 格式：2025年7月31日 20:00
  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}

function scrollToBottom() {
  const el = document.querySelector('.room-content')
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

function go(path) {
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${normalizedBase}${normalizedPath || '/'}`
  window.open(url, '_blank')
}

watch(draft, () => {
  adjustTextareaHeight()
})

// 点击外部关闭表情面板
function handleClickOutside(event) {
  const emojiPanel = document.querySelector('.emoji-panel')
  const editorTools = document.querySelector('.editor-tools')
  if (emojiPanel && !emojiPanel.contains(event.target) && 
      editorTools && !editorTools.contains(event.target)) {
    showEmojiPanel.value = false
  }
}

onMounted(() => {
  loadContacts().then(() => {
    // 支持从粉丝页等位置直达会话：/messages?peerId=xxx
    const peerIdParam = route.query.peerId
    if (peerIdParam && typeof peerIdParam === 'string') {
      const pid = Number(peerIdParam)
      if (Number.isFinite(pid) && pid > 0) {
        openConversation(pid)
      }
    }
  })
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function openConversation(peerId) {
  if (!isAuthed.value) return
  // 如果左侧已存在该联系人，直接选中
  const existing = contactList.value.find((c) => c.userId === peerId)
  if (existing) {
    selectContact(existing)
    return
  }
  // 不存在则通过后端拉取基础信息并临时插入联系人列表
  try {
    const { data } = await fetchPeerInfo(peerId)
    if (data?.success && data?.data) {
      const item = data.data
      const contact = {
        userId: item.userId || item.user_id || peerId,
        username: item.username || '用户',
        avatar: normalizedAvatar(item.avatar),
        lastContent: '',
        lastTime: null,
        unreadCount: 0,
      }
      contactList.value = [contact, ...contactList.value]
      selectContact(contact)
    }
  } catch (e) {
    // 静默失败：可能用户不存在
  }
}
</script>

<style scoped lang="scss">
.messages-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  height: 100vh;
  margin: 0 auto;
  background-image: url('/assets/history_bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.header-wrapper {
  position: relative;
  height: 64px;
}

.container {
  max-width: 1236px;
  margin: 0 auto;
  padding: 18px;
  display: flex;
  justify-content: center;
  height: calc(100vh - 64px);
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.5);
}

.sidebar {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  height: 100%;
  padding: 0;
  overflow: hidden;
  .s-title { padding: 12px 16px 10px; color: #61666d; font-weight: 600; }
  .menu { list-style: none; margin: 0; padding: 0; }
  .menu > li { position: relative; display: flex; align-items: center; gap: 8px; padding: 10px 16px; font-size: 14px; color: #222; cursor: default; }
  .menu > li:not(.sep):hover { background: #f7f7f7; }
  .menu .sep { height: 0; }
  .dot { width: 6px; height: 6px; background: #00a1d6; border-radius: 50%; display: inline-block; }
  .dot.hollow { background: transparent; border: 1px solid #c8c8c8; }
  .count { margin-left: auto; background: #ff4d4f; color: #fff; font-size: 12px; border-radius: 10px; padding: 0 6px; line-height: 18px; height: 18px; }
  .active { color: #00a1d6; background: #eaf6ff; }
  .disabled { color: #9ca3af; }
}

.main { 
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 1200px;
}

.topbar {
  display: flex; 
  align-items: center;
  width: 100%;
  // padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  box-shadow: 0 2px 4px #7992b98a;
}
.search { 
  display: grid; 
  grid-template-columns: 28px 1fr; 
  align-items: center; 
  border: 1px solid #e5e7eb; 
  background: #fff; 
  border-radius: 6px; 
  height: 36px; 
  overflow: hidden;
  width: 100%;
}
.search .icon { width: 16px; height: 16px; margin-left: 10px; background: url('/assets/search.png') center/contain no-repeat; filter: grayscale(100%) opacity(.6); }
.search input {
  border: 0;
  outline: none;
  height: 100%;
  padding: 0 10px;
  font-size: 14px;
  background: #ffffff;
  color: #222;
}
.search input::placeholder {
  color: #9ca3af;
}

.board { 
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px; 
  display: grid; 
  grid-template-columns: 340px 1fr; 
  height: 100%; 
  overflow: hidden;
  width: 100%;
  box-shadow: 0 2px 4px #7992b98a;
  flex: 1;
  min-height: 0;
}

.left-list { border-right: 1px solid rgba(0,0,0,.06); overflow: auto; background: #FFFFFF; }
.left-list-header {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0,0,0,.06);
  background: #FFFFFF;
  display: flex;
  align-items: center;
  height: 48px;
  box-sizing: border-box;
}
.left-list-header .title {
  font-size: 14px;
  font-weight: 600;
  color: #18191c;
}
.chat-item { display: grid; grid-template-columns: 44px 1fr; gap: 10px; padding: 10px 16px; cursor: pointer; background: #FFFFFF; }
.chat-item:hover { background: rgba(0,0,0,.02); }
.avatar { width: 44px; height: 44px; border-radius: 50%; background: #d8d8d8; }
.info { display: grid; gap: 6px; align-content: center; }
.title { display: flex; align-items: center; gap: 8px; }
.name { font-weight: 600; color: #222; }
.time {
  margin-left: auto;
  font-size: 12px;
  color: #9ca3af;
}
.unread { margin-left: auto; background: #ff4d4f; color: #fff; font-size: 12px; border-radius: 10px; padding: 0 6px; line-height: 18px; height: 18px; }
.snippet { color: #8a8a8a; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.right-dialog { 
  display: grid; 
  grid-template-rows: 1fr; 
  background: #F6F7F8;
  min-height: 0;
  overflow: hidden;
}
.empty { display: grid; place-items: center; color: #9ca3af; }
.illustration img { width: 120px; height: 120px; filter: grayscale(100%) opacity(.5); }
.empty-tip { margin-top: 8px; }

.room { 
  display: grid; 
  grid-template-rows: auto 1fr auto; 
  background: #f6f7f8;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
.room-header {
  padding: 12px 18px;
  border-bottom: 1px solid rgba(0,0,0,.06);
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  box-sizing: border-box;
}
.room-name {
  font-size: 14px;
  font-weight: 600;
  color: #18191c;
  text-align: center;
}
.room-content { 
  overflow-y: auto; 
  overflow-x: hidden;
  padding: 16px; 
  display: flex; 
  flex-direction: column; 
  gap: 16px; 
  background: transparent;
  min-height: 0;
  flex: 1;
}

/* 自定义滚动条样式 */
.room-content::-webkit-scrollbar {
  width: 6px;
}

.room-content::-webkit-scrollbar-track {
  background: transparent;
}

.room-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.room-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.msg-wrapper {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: 100%;
}

.msg-wrapper.left {
  align-items: flex-start;
}

.msg-wrapper.right {
  align-items: flex-start;
}

.msg-wrapper.right {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #d8d8d8;
  margin-top: 2px;
}

.msg-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.msg-content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.msg-wrapper.right .msg-content-wrapper {
  align-items: flex-end;
}

.msg-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #222;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  word-wrap: break-word;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.msg-wrapper.right .msg-bubble {
  background: #00a1d6;
  color: #ffffff;
  border: none;
}

.msg-time-divider {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 16px 0;
  width: 100%;
}

.msg-time-text {
  font-size: 12px;
  color: #9ca3af;
  padding: 4px 12px;
}
.room-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid rgba(0,0,0,.06);
  background: #f6f7f8;
}
.editor-tools {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
.editor-tools .tool {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid rgba(0,0,0,.08);
  background: #FFFFFF;
  color: #61666d;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: all 0.2s;
}
.editor-tools .tool:hover {
  background: #f5f5f5;
  border-color: rgba(0,0,0,.12);
}
.editor-tools .tool svg {
  display: block;
}
.editor-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  position: relative;
  background: #f6f7f8;
  border-radius: 8px;
  // padding: 8px;
}
.room-editor textarea {
  width: 100%;
  min-height: 80px;
  max-height: 200px;
  border: none;
  border-radius: 8px;
  // padding: 12px;
  font-size: 14px;
  background: #f6f7f8;
  color: #222;
  box-sizing: border-box;
  font-family: inherit;
  resize: none;
  overflow-y: auto;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}
.room-editor textarea::placeholder {
  color: #9ca3af;
}
.room-editor textarea:focus {
  outline: none;
}
/* 自定义滚动条样式 */
.room-editor textarea::-webkit-scrollbar {
  width: 6px;
}
.room-editor textarea::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.room-editor textarea::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
.room-editor textarea::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
.editor-footer {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}
.char-count {
  font-size: 12px;
  color: #9ca3af;
}
.room-editor .send {
  background: #00a1d6;
  color: #fff;
  border: 0;
  height: 36px;
  padding: 0 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}
.room-editor .send:hover {
  background: #0090c0;
}

.emoji-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 表情面板滚动条样式 */
.emoji-panel::-webkit-scrollbar {
  width: 8px;
}

.emoji-panel::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

.emoji-panel::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #c1c1c1 0%, #a8a8a8 100%);
  border-radius: 4px;
  border: 2px solid #f5f5f5;
}

.emoji-panel::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #a8a8a8 0%, #909090 100%);
}

.emoji-list {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 24px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
  user-select: none;
}

.emoji-item:hover {
  background: #f0f0f0;
}

.msg-image {
  max-width: 100%;
  border-radius: 4px;
  overflow: hidden;
}

.msg-image img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  display: block;
  cursor: pointer;
}

.msg-text {
  word-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
}
</style>


