<template>
  <div class="history-page">
    <aside class="timeline">
      <div class="dot today"></div>
      <div class="label">今天</div>
      <div class="spacer"></div>
      <div class="dot yesterday"></div>
      <div class="label muted">昨天</div>
      <div class="spacer"></div>
      <div class="dot week"></div>
      <div class="label muted">近一周</div>
    </aside>

    <section class="content">
      <header class="toolbar">
        <h1>历史记录</h1>
        <div class="actions">
          <label class="switch">
            <input type="checkbox" v-model="recording" />
            <span>记录浏览历史</span>
          </label>
          <button class="btn" @click="clearAll">清空历史</button>
        </div>
      </header>

      <nav class="tabs">
        <button class="tab" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">综合</button>
        <button class="tab" :class="{ active: activeTab === 'video' }" @click="activeTab = 'video'">视频</button>
        <button class="tab" :class="{ active: activeTab === 'live' }" @click="activeTab = 'live'">直播</button>
        <div class="more">更多筛选 ▾</div>
        <div class="search">
          <input v-model="keyword" placeholder="搜索标题/UP主/时段" />
        </div>
      </nav>

      <div v-for="group in filteredGroups" :key="group.key" class="day-group">
        <div class="day-title">{{ group.title }}</div>
        <div class="grid">
          <div v-for="item in group.items" :key="item.id" class="card">
            <div class="thumb">
              <img :src="item.cover" :alt="item.title" />
              <span class="duration">{{ item.duration }}</span>
              <span v-if="item.badge" class="badge">{{ item.badge }}</span>
            </div>
            <div class="meta">
              <div class="title" :title="item.title">{{ item.title }}</div>
              <div class="sub">
                <span class="up">UP {{ item.up }}</span>
                <span class="dot">·</span>
                <span class="time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'History',
  data() {
    return {
      recording: true,
      activeTab: 'all',
      keyword: '',
      groups: [
        {
          key: 'today',
          title: '今天',
          items: generateMockItems(12)
        },
        {
          key: 'yesterday',
          title: '昨天',
          items: generateMockItems(8)
        },
        {
          key: 'week',
          title: '近一周',
          items: generateMockItems(10)
        }
      ]
    }
  },
  computed: {
    filteredGroups() {
      const kw = this.keyword.trim().toLowerCase()
      if (!kw) return this.groups
      return this.groups.map(g => ({
        ...g,
        items: g.items.filter(i =>
          i.title.toLowerCase().includes(kw) ||
          i.up.toLowerCase().includes(kw)
        )
      }))
    }
  },
  methods: {
    clearAll() {
      this.groups = this.groups.map(g => ({ ...g, items: [] }))
    }
  }
}

function generateMockItems(count) {
  const covers = [
    '/assets/home.png',
    '/assets/feed.png',
    '/assets/trending.png',
    '/assets/history.png',
    '/assets/messages.png',
    '/assets/search.png'
  ]
  const result = []
  for (let index = 0; index < count; index++) {
    const id = `${Date.now()}-${index}`
    const cover = covers[index % covers.length]
    result.push({
      id,
      title: `示例视频标题 ${index + 1}：这是一个很长很长的标题演示`,
      up: ['张三', '李四', '王五', '小明'][index % 4],
      time: `今天23:${(10 + index).toString().padStart(2, '0')}`,
      duration: `0${Math.floor(Math.random() * 5)}:${(10 + index).toString().slice(-2)}`,
      badge: index % 5 === 0 ? '直播回放' : '',
      cover
    })
  }
  return result
}
</script>

<style scoped>
.history-page {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 24px;
  padding: 16px 24px;
}

.timeline {
  position: sticky;
  top: 80px;
  height: fit-content;
  display: grid;
  grid-template-columns: 24px 1fr;
  row-gap: 12px;
  align-items: center;
}
.timeline .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ddd;
  justify-self: center;
}
.timeline .dot.today { background: #00a1d6; }
.timeline .label { font-size: 14px; color: #222; }
.timeline .label.muted { color: #888; }
.timeline .spacer {
  grid-column: 1 / -1;
  height: 24px;
  border-left: 2px solid #eee;
  margin-left: 11px;
}

.content {
  min-width: 0;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.toolbar h1 { font-size: 22px; margin: 0; }
.actions { display: flex; gap: 12px; align-items: center; }
.switch span { margin-left: 6px; color: #666; }
.btn { border: 1px solid #e3e3e3; background: #fff; padding: 6px 10px; border-radius: 6px; cursor: pointer; }

.tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0 16px;
}
.tab { background: transparent; border: none; padding: 6px 8px; cursor: pointer; color: #666; }
.tab.active { color: #00a1d6; font-weight: 600; }
.more { margin-left: auto; color: #666; }
.search input { border: 1px solid #e3e3e3; padding: 6px 10px; border-radius: 16px; width: 260px; }

.day-group { margin: 8px 0 24px; }
.day-title { font-size: 16px; color: #222; margin-bottom: 12px; }
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.card { background: #fff; border: 1px solid #f0f0f0; border-radius: 8px; overflow: hidden; cursor: pointer; }
.thumb { position: relative; aspect-ratio: 16 / 9; background: #f7f7f7; display: flex; align-items: center; justify-content: center; }
.thumb img { width: 100%; height: 100%; object-fit: cover; }
.duration { position: absolute; right: 8px; bottom: 8px; background: rgba(0,0,0,0.65); color: #fff; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.badge { position: absolute; left: 8px; top: 8px; background: #ff4d4f; color: #fff; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.meta { padding: 8px; }
.title { font-size: 14px; color: #222; line-height: 1.4; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.sub { color: #888; font-size: 12px; margin-top: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sub .dot { margin: 0 4px; }

@media (max-width: 1280px) {
  .grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 960px) {
  .history-page { grid-template-columns: 1fr; }
  .timeline { display: none; }
  .grid { grid-template-columns: repeat(2, 1fr); }
}
</style>


