<template>
  <div class="messages-page">
    <div class="container">
      <aside class="sidebar">
        <div class="s-title">消息中心</div>
        <ul class="menu">
          <li :class="{ active: tab==='mine' }" @click="tab='mine'">
            <i class="dot" />
            <span>我的消息</span>
            <span class="count">99+</span>
          </li>
          <li class="sep"> </li>
          <li class="disabled"><i class="dot hollow" /><span>回复我的</span></li>
          <li class="disabled"><i class="dot hollow" /><span>@ 我的</span></li>
          <li class="disabled"><i class="dot hollow" /><span>收到的赞</span></li>
          <li class="disabled"><i class="dot hollow" /><span>系统通知</span></li>
          <li class="disabled"><i class="dot hollow" /><span>消息设置</span></li>
        </ul>
      </aside>

      <main class="main">
        <div class="topbar">
          <div class="search">
            <i class="icon search-icon" />
            <input v-model="keyword" placeholder="我的消息" />
          </div>
          <button class="assistant">应援团助手</button>
        </div>

        <section class="board">
          <div class="left-list">
            <div
              v-for="(item, idx) in mockList"
              :key="idx"
              class="chat-item"
              @click="activeIndex=idx"
            >
              <div class="avatar" />
              <div class="info">
                <div class="title">
                  <span class="name">{{ item.name }}</span>
                  <span v-if="item.unread>0" class="unread">{{ item.unread }}</span>
                </div>
                <div class="snippet">{{ item.snippet }}</div>
              </div>
            </div>
          </div>

          <div class="right-dialog">
            <div class="empty" v-if="activeIndex===-1">
              <div class="illustration">
                <img src="/assets/messages.png" alt="empty" />
              </div>
              <div class="empty-tip">快找小伙伴聊天吧（・`ω´･）</div>
            </div>
            <div v-else class="room">
              <div class="room-header">{{ mockList[activeIndex].name }}</div>
              <div class="room-content">
                <div class="msg left">你好，这是一条示例消息。</div>
                <div class="msg right">你好！欢迎来到消息中心。</div>
              </div>
              <div class="room-editor">
                <input v-model="draft" placeholder="发个消息吧…" @keyup.enter="send" />
                <button class="send" @click="send">发送</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const tab = ref('mine')
const keyword = ref('')
const activeIndex = ref(-1)
const draft = ref('')

const baseList = [
  { name: '马俐霞家', snippet: '感谢你的关注~', unread: 2 },
  { name: '哔哩哔哩智能鸽机', snippet: 'Hi，B站的小伙伴！', unread: 2 },
  { name: '方山厨子', snippet: '感谢回来评论，谢啦~', unread: 1 },
  { name: '一君老狮', snippet: '欢迎关注我 ᕕ( ᐛ )ᕗ', unread: 1 },
  { name: '哔哩哔哩会员购', snippet: '商品发货通知', unread: 12 },
  { name: '支付小助手', snippet: '支付完成通知', unread: 0 },
]

const mockList = computed(() => {
  if (!keyword.value) return baseList
  return baseList.filter(i => i.name.includes(keyword.value) || i.snippet.includes(keyword.value))
})

function send() {
  if (!draft.value.trim()) return
  draft.value = ''
}
</script>

<style scoped lang="scss">
.messages-page {
  min-width: 1200px;
  background: #dff1f1; /* 贴近截图两侧的浅青色背景 */
}

.container {
  max-width: 1350px;
  margin: 0 auto;
  padding: 16px 20px;
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
}

.sidebar {
  background: #ffffff;
  border-radius: 8px;
  height: calc(100vh - 90px);
  padding: 10px 0;
  .s-title { padding: 0 16px 10px; color: #61666d; font-weight: 600; }
  .menu { list-style: none; margin: 0; padding: 6px 0; }
  .menu > li { position: relative; display: flex; align-items: center; gap: 8px; padding: 10px 16px; font-size: 14px; color: #222; cursor: default; }
  .menu > li:not(.sep):hover { background: #f7f7f7; }
  .menu .sep { height: 8px; }
  .dot { width: 6px; height: 6px; background: #00a1d6; border-radius: 50%; display: inline-block; }
  .dot.hollow { background: transparent; border: 1px solid #c8c8c8; }
  .count { margin-left: auto; background: #ff4d4f; color: #fff; font-size: 12px; border-radius: 10px; padding: 0 6px; line-height: 18px; height: 18px; }
  .active { color: #00a1d6; background: #eaf6ff; }
  .disabled { color: #9ca3af; }
}

.main { display: grid; grid-template-rows: auto 1fr; gap: 12px; }

.topbar {
  display: grid; grid-template-columns: 420px 1fr; align-items: center;
}
.search { display: grid; grid-template-columns: 28px 1fr; align-items: center; border: 1px solid #e5e7eb; background: #fff; border-radius: 6px; height: 36px; overflow: hidden; }
.search .icon { width: 16px; height: 16px; margin-left: 10px; background: url('/assets/search.png') center/contain no-repeat; filter: grayscale(100%) opacity(.6); }
.search input { border: 0; outline: none; height: 100%; padding: 0 10px; font-size: 14px; }
.assistant { justify-self: end; background: transparent; border: 0; color: #8a8a8a; cursor: pointer; }

.board { background: #fff; border-radius: 8px; display: grid; grid-template-columns: 340px 1fr; height: calc(100vh - 150px); overflow: hidden; }

.left-list { border-right: 1px solid #eee; overflow: auto; }
.chat-item { display: grid; grid-template-columns: 44px 1fr; gap: 10px; padding: 12px 14px; cursor: pointer; }
.chat-item:hover { background: #fafafa; }
.avatar { width: 44px; height: 44px; border-radius: 50%; background: #d8d8d8; }
.info { display: grid; gap: 6px; align-content: center; }
.title { display: flex; align-items: center; gap: 8px; }
.name { font-weight: 600; color: #222; }
.unread { margin-left: auto; background: #ff4d4f; color: #fff; font-size: 12px; border-radius: 10px; padding: 0 6px; line-height: 18px; height: 18px; }
.snippet { color: #8a8a8a; font-size: 12px; }

.right-dialog { display: grid; grid-template-rows: 1fr; }
.empty { display: grid; place-items: center; color: #9ca3af; }
.illustration img { width: 120px; height: 120px; filter: grayscale(100%) opacity(.5); }
.empty-tip { margin-top: 8px; }

.room { display: grid; grid-template-rows: auto 1fr auto; }
.room-header { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: 600; }
.room-content { overflow: auto; padding: 16px; display: grid; gap: 10px; align-content: start; background: #fafafa; }
.msg { max-width: 60%; padding: 8px 12px; border-radius: 8px; }
.msg.left { background: #fff; border: 1px solid #eee; }
.msg.right { background: #e6f3ff; margin-left: auto; }
.room-editor { display: grid; grid-template-columns: 1fr 90px; gap: 10px; padding: 12px; border-top: 1px solid #eee; }
.room-editor input { height: 36px; border: 1px solid #e5e7eb; border-radius: 6px; padding: 0 10px; }
.room-editor .send { background: #00a1d6; color: #fff; border: 0; border-radius: 6px; cursor: pointer; }
</style>


