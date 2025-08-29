<template>
  <div class="feed-page">
    <div class="feed-container">
      <aside class="left-column">
        <!-- inlined: UserCard.vue -->
        <div class="user-card">
          <div class="avatar"></div>
          <div class="info">
            <div class="name">小明</div>
            <div class="stats">
              <div><b>32</b><span>关注</span></div>
              <div><b>0</b><span>粉丝</span></div>
              <div><b>0</b><span>动态</span></div>
            </div>
          </div>
        </div>
        <nav class="left-menu">
          <ul>
            <li>全部动态</li>
            <li>视频投稿</li>
            <li>追番追剧</li>
            <li>专栏</li>
          </ul>
        </nav>
      </aside>

      <main class="center-column">
        <!-- inlined: Composer.vue -->
        <div class="composer">
          <input class="input" placeholder="好的标题更容易获得支持，选题20字" />
          <div class="actions">
            <button class="btn">发布</button>
          </div>
        </div>

        <!-- inlined: StoriesStrip.vue -->
        <div class="stories">
          <div v-for="n in 10" :key="n" class="story">
            <div class="bubble"></div>
            <div class="label">好友{{ n }}</div>
          </div>
        </div>

        <!-- inlined: FeedList.vue -->
        <div class="feed-list">
          <article v-for="n in 6" :key="n" class="feed-card">
            <header class="meta">
              <div class="avatar"></div>
              <div class="who">
                <div class="name">马长林夕-梦</div>
                <div class="sub">44分钟前 · 投稿了视频</div>
              </div>
            </header>
            <div class="content">
              <div class="thumb"></div>
              <div class="title">小丸子说:总是喷痰咳气，幸福会跑掉。</div>
            </div>
            <footer class="actions">
              <span>赞 147</span>
              <span>评 0</span>
              <span>转 15</span>
            </footer>
          </article>
        </div>
      </main>

      <aside class="right-column">
        <!-- inlined: RightSidebar.vue -->
        <div class="right">
          <section class="card">
            <h4>社区中心</h4>
            <ul>
              <li>公告与规则</li>
              <li>活动广场</li>
              <li>意见反馈</li>
            </ul>
          </section>
          <section class="card">
            <h4>bilibili热搜</h4>
            <ol>
              <li v-for="n in 10" :key="n">热搜条目 {{ n }}</li>
            </ol>
          </section>
        </div>
      </aside>
    </div>
  </div>
  
</template>

<script setup>
// child components inlined; no imports needed
</script>

<style scoped>
.feed-page {
  background: #e7f3f5;
  min-height: 100vh;
}
.feed-container {
  width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 260px 1fr 300px;
  gap: 16px;
  padding: 16px 0 40px;
}
.left-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.left-menu {
  background: #fff;
  border-radius: 8px;
  padding: 12px 0;
}
.left-menu ul { list-style: none; margin: 0; padding: 0; }
.left-menu li {
  padding: 10px 16px;
  cursor: pointer;
}
.left-menu li:hover { background: #f5f7fa; }

.center-column { display: flex; flex-direction: column; gap: 12px; }
.right-column { position: sticky; top: 16px; height: fit-content; }

/* inlined styles from subcomponents */
.user-card { background: #fff; border-radius: 8px; padding: 16px; }
.user-card .avatar { width: 56px; height: 56px; border-radius: 50%; background: #ddd; }
.user-card .info { margin-top: 8px; }
.user-card .name { font-weight: 600; }
.user-card .stats { display: flex; gap: 16px; margin-top: 8px; }
.user-card .stats div { display: flex; flex-direction: column; align-items: center; }
.user-card .stats span { color: #888; font-size: 12px; }

.composer { background:#fff; border-radius:8px; padding:12px; }
.composer .input { width:100%; border:none; outline:none; background:#f7f8fa; padding:10px 12px; border-radius:6px; }
.composer .actions { display:flex; justify-content:flex-end; margin-top:8px; }
.composer .btn { background:#00aeec; color:#fff; border:none; padding:6px 14px; border-radius:6px; cursor:pointer; }
.composer .btn:hover { opacity:.9; }

.stories { display: flex; gap: 12px; background: #fff; border-radius: 8px; padding: 12px; overflow-x: auto; }
.story { display:flex; flex-direction:column; align-items:center; gap:6px; }
.bubble { width:48px; height:48px; border-radius:50%; background:#e3e9ee; }
.label { font-size:12px; color:#666; }

.feed-list { display:flex; flex-direction:column; gap:12px; }
.feed-card { background:#fff; border-radius:8px; padding:12px; }
.meta { display:flex; align-items:center; gap:10px; }
.avatar { width:36px; height:36px; border-radius:50%; background:#ddd; }
.who .name { font-weight:600; }
.who .sub { color:#888; font-size:12px; }
.content { display:flex; gap:12px; margin-top:10px; }
.thumb { width:180px; height:100px; background:#d9dee3; border-radius:6px; }
.title { font-size:14px; line-height:1.6; }
.actions { display:flex; gap:16px; color:#666; font-size:13px; margin-top:8px; }

.right { display:flex; flex-direction:column; gap:12px; }
.card { background:#fff; border-radius:8px; padding:12px; }
.card h4 { margin:0 0 8px; }
.card ul, .card ol { margin:0; padding-left:18px; color:#666; }
</style>


