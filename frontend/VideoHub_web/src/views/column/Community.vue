<template>
  <div class="community">
    <!-- 平台规则 -->
    <section class="container card rules">
      <div class="rules-left">
        <div class="promo placeholder">平台规则</div>
        <div class="quick">
          <div class="q-item"><span class="dot"/>社区公告</div>
          <div class="q-item"><span class="dot"/>直播规则</div>
          <div class="q-item"><span class="dot"/>小黑屋处理案例</div>
        </div>
      </div>
      <div class="rules-right">
        <div class="list">
          <div class="li" v-for="(it,i) in ruleNews" :key="i">
            <span class="tag" :class="it.tagClass">{{ it.tag }}</span>
            <span class="title" :title="it.title">{{ it.title }}</span>
          </div>
        </div>
        <a class="more" href="javascript:void(0)">更多公告 ›</a>
      </div>
    </section>

    <!-- 规则解读：大视频 + 缩略条 -->
    <section class="container explain">
      <div class="player-wrap">
        <div class="player">
          <video class="video" controls>
            <source :src="expSrc" type="video/mp4" />
          </video>
        </div>
      </div>
      <div class="thumbs">
        <div class="t placeholder" v-for="(t,i) in thumbs" :key="i"></div>
      </div>
    </section>

    <!-- 社区参与：两列上方卡 + 两组列表 -->
    <section class="container participate">
      <div class="top-cards">
        <div class="pcard">
          <div class="icon-placeholder">🚫</div>
          <div class="pc-title">小黑屋</div>
        </div>
        <div class="pcard">
          <div class="icon-placeholder">🛡️</div>
          <div class="pc-title">风纪委员</div>
        </div>
      </div>
      <div class="lists">
        <div class="list-card">
          <div class="lc-title">社区话题</div>
          <ul>
            <li v-for="(s,i) in communityTopics" :key="i">
              <span class="dot"/>{{ s }}
              <span class="arrow">›</span>
            </li>
          </ul>
        </div>
        <div class="list-card">
          <div class="lc-title">产品话题</div>
          <ul>
            <li v-for="(s,i) in productTopics" :key="i">
              <span class="dot"/>{{ s }}
              <span class="arrow">›</span>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- 治理视频：大视频 + 缩略条 -->
    <section class="container govern">
      <div class="player-wrap">
        <div class="player">
          <video class="video" controls>
            <source :src="govSrc" type="video/mp4" />
          </video>
        </div>
      </div>
      <div class="thumbs">
        <div class="t placeholder" v-for="(t,i) in govThumbs" :key="i"></div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const ruleNews = ref([
  { tag: '公告', tagClass: 'info', title: '小黑屋的变更小课堂（2.1）' },
  { tag: '提醒', tagClass: 'warn', title: '屏蔽词管理说明（第十五期）' },
  { tag: '通知', tagClass: 'note', title: '直播治违明细（第四六期）' },
  { tag: '公告', tagClass: 'info', title: '2024年屏蔽词增补（2月）' },
])

const expSrc = ''
const thumbs = ref([1,2,3,4,5])

const communityTopics = ref([
  'B站稿件、评论及动态审核问题反馈','B站新风纪委员会反馈收集'
])
const productTopics = ref([
  '使用站内创作时遇到的问题','在B站看视频时出现的播放问题','使用站内PC端遇到的功能BUG','使用站内APP遇到的功能BUG','希望B站能增加或修改的功能建议','新手上路遇到不会使用的求助'
])

const govSrc = ''
const govThumbs = ref([1,2,3,4,5])
</script>

<style scoped lang="scss">
.community { background:#f7f8fa; }
.container { max-width: 980px; margin: 0 auto; padding: 0 12px; }
.card { background:#fff; border:1px solid #eee; border-radius:10px; box-shadow:0 4px 14px rgba(0,0,0,.04); }

/* 平台规则 */
.rules { display:grid; grid-template-columns: 1fr 1fr; gap: 16px; padding: 14px; margin: 16px auto; }
.rules-left .promo { width:100%; height:120px; object-fit:cover; border-radius:8px; }
.quick { display:flex; gap:12px; margin-top:10px; }
.q-item { background:#f5f6f7; border:1px solid #eee; border-radius:8px; padding:8px 10px; font-size:13px; color:#61666d; display:flex; align-items:center; gap:6px; }
.q-item .dot { width:6px; height:6px; border-radius:50%; background:#00a1d6; display:inline-block; }
.rules-right { display:flex; flex-direction:column; gap:10px; }
.list { display:grid; gap:8px; }
.li { display:grid; grid-template-columns:auto 1fr; gap:10px; align-items:center; }
.tag { font-size:12px; padding:2px 6px; border-radius:4px; color:#fff; }
.tag.info{ background:#00a1d6; }
.tag.warn{ background:#ff9212; }
.tag.note{ background:#6c8; color:#fff; }
.title{ font-size:13px; color:#222; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.more{ color:#909399; font-size:12px; text-decoration:none; }

/* 视频块（规则解读/治理视频） */
.explain, .govern { background:#fff; border:1px solid #eee; border-radius:10px; padding: 14px; margin: 12px auto; }
.player-wrap{ background:#000; border-radius:8px; overflow:hidden; }
.player{ position:relative; padding-bottom:56.25%; }
.video{ position:absolute; inset:0; width:100%; height:100%; object-fit:cover; }
.thumbs{ display:flex; gap:10px; margin-top:8px; }
.thumbs .t{ width:120px; height:68px; border-radius:6px; overflow:hidden; background:#f1f2f3; }
.thumbs .t img{ width:100%; height:100%; object-fit:cover; }

/* 社区参与 */
.participate{ background:#fff; border:1px solid #eee; border-radius:10px; padding: 14px; margin: 12px auto; }
.top-cards{ display:grid; grid-template-columns: 1fr 1fr; gap:12px; }
.pcard{ background:#f7f8fa; border:1px solid #eee; border-radius:10px; padding:16px; display:flex; align-items:center; gap:10px; }
.pcard img{ width:28px; height:28px; filter: grayscale(100%) brightness(.9); }
.pc-title{ font-weight:600; color:#222; }
.lists{ display:grid; grid-template-columns: 1fr 1fr; gap:12px; margin-top:12px; }
.list-card{ background:#fff; border:1px dashed #e8e8e8; border-radius:8px; padding:10px; }
.lc-title{ font-weight:600; margin-bottom:8px; color:#222; }
.list-card ul{ list-style:none; margin:0; padding:0; display:grid; gap:8px; }
.list-card li{ display:grid; grid-template-columns: 8px 1fr auto; align-items:center; gap:8px; font-size:13px; color:#61666d; }
.list-card .dot{ width:6px; height:6px; border-radius:50%; background:#bbb; }
.list-card .arrow{ color:#c0c4cc; }
</style>


