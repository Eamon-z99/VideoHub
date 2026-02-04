<template>
  <div class="settings-page">
    <div class="container">
      <aside class="sidebar">
        <div class="s-title">消息中心</div>
        <ul class="menu">
          <li @click="go('/messages')"><i class="dot hollow" /><span>我的消息</span></li>
          <li class="sep" />
          <li class="disabled"><i class="dot hollow" /><span>回复我的</span></li>
          <li class="disabled"><i class="dot hollow" /><span>@ 我的</span></li>
          <li class="disabled"><i class="dot hollow" /><span>收到的赞</span></li>
          <li class="disabled"><i class="dot hollow" /><span>系统通知</span></li>
          <li class="active"><i class="dot" /><span>消息设置</span></li>
        </ul>
      </aside>

      <main class="main">
        <div class="topbar">
          <div class="title">消息设置</div>
        </div>

        <section class="cards">
          <div class="card">
            <div class="card-hd">消息提醒（关闭后，消息将不再进行提醒）</div>
            <div class="card-bd">
              <el-radio-group v-model="notify">
                <el-radio :label="true">开启</el-radio>
                <el-radio :label="false">关闭</el-radio>
              </el-radio-group>
            </div>
          </div>

          <div class="card">
            <div class="card-hd">私信智能拦截（开启后，将自动拦截骚扰或不良的会话）</div>
            <div class="card-bd">
              <el-radio-group v-model="smartBlock">
                <el-radio :label="true">开启</el-radio>
                <el-radio :label="false">关闭</el-radio>
              </el-radio-group>
            </div>
          </div>

          <div class="card">
            <div class="card-hd">消息屏蔽词（添加后，将不再接收包含屏蔽词的消息）</div>
            <div class="card-bd row">
              <el-button type="primary" link @click="addWord">+ 添加屏蔽词</el-button>
              <div class="words">
                <el-tag
                  v-for="(w,i) in words"
                  :key="i"
                  closable
                  @close="removeWord(i)"
                >{{ w }}</el-tag>
              </div>
            </div>
          </div>

          <div class="card">
            <div class="card-hd">回复我的消息提醒（接收谁的评论消息提醒）</div>
            <div class="card-bd">
              <el-radio-group v-model="replyNotify">
                <el-radio label="all">所有人</el-radio>
                <el-radio label="follow">关注的人</el-radio>
                <el-radio label="none">不接收任何消息提醒</el-radio>
              </el-radio-group>
            </div>
          </div>

          <div class="card">
            <div class="card-hd">@我的消息提醒（接收谁的 @ 消息提醒）</div>
            <div class="card-bd">
              <el-radio-group v-model="atNotify">
                <el-radio label="all">所有人</el-radio>
                <el-radio label="follow">关注的人</el-radio>
                <el-radio label="none">不接收任何消息提醒</el-radio>
              </el-radio-group>
            </div>
          </div>

          <div class="card">
            <div class="card-hd">收到的赞消息提醒</div>
            <div class="card-bd">
              <el-radio-group v-model="likeNotify">
                <el-radio :label="true">开启</el-radio>
                <el-radio :label="false">关闭</el-radio>
              </el-radio-group>
            </div>
          </div>

          <div class="card">
            <div class="card-hd">收到未关注人消息（开启后，未关注入消息将移到筛选夹）</div>
            <div class="card-bd">
              <el-radio-group v-model="strangerToFilter">
                <el-radio :label="true">开启</el-radio>
                <el-radio :label="false">关闭</el-radio>
              </el-radio-group>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const notify = ref(true)
const smartBlock = ref(true)
const words = ref(['广告', '辱骂'])
const replyNotify = ref('all')
const atNotify = ref('follow')
const likeNotify = ref(true)
const strangerToFilter = ref(false)

function go(path) {
  const base = window.__MICRO_APP_BASE_ROUTE__ || ''
  const normalizedBase = base.replace(/\/$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${normalizedBase}${normalizedPath || '/'}`
  window.open(url, '_blank')
}
function addWord() {
  const text = window.prompt('输入屏蔽词')
  if (text && !words.value.includes(text)) words.value.push(text)
}
function removeWord(i) { words.value.splice(i, 1) }
</script>

<style scoped lang="scss">
.settings-page { background: #dff1f1; min-width: 1200px; }
.container { max-width: 1350px; margin: 0 auto; padding: 16px 20px; display: grid; grid-template-columns: 220px 1fr; gap: 16px; }
.sidebar { background: #fff; border-radius: 8px; height: calc(100vh - 90px); padding: 10px 0; }
.s-title { padding: 0 16px 10px; color: #61666d; font-weight: 600; }
.menu { list-style: none; margin: 0; padding: 6px 0; }
.menu > li { display: flex; align-items: center; gap: 8px; padding: 10px 16px; font-size: 14px; color: #222; }
.menu > li:not(.sep):hover { background: #f7f7f7; cursor: pointer; }
.menu .sep { height: 8px; }
.dot { width: 6px; height: 6px; background: #00a1d6; border-radius: 50%; display: inline-block; }
.dot.hollow { background: transparent; border: 1px solid #c8c8c8; }
.active { color: #00a1d6; background: #eaf6ff; }
.disabled { color: #9ca3af; }

.main { display: grid; grid-template-rows: auto 1fr; gap: 12px; }
.topbar { display: flex; align-items: center; height: 36px; }
.title { font-weight: 600; color: #61666d; }

.cards { display: grid; gap: 12px; }
.card { background: #fff; border-radius: 8px; border: 1px solid #e6f0f0; box-shadow: 0 1px 0 #cfe8e8 inset; }
.card-hd { padding: 14px 16px; border-bottom: 1px solid #eef2f3; font-weight: 600; color: #4b5563; }
.card-bd { padding: 12px 16px; display: flex; align-items: center; gap: 16px; }
.card-bd.row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.words { display: flex; gap: 8px; flex-wrap: wrap; }
</style>


