<template>
  <div class="feed-page">
    <div class="feed-container">
      <aside class="left-column">
        <UserCard />
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
        <Composer />
        <StoriesStrip />
        <FeedList />
      </main>

      <aside class="right-column">
        <RightSidebar />
      </aside>
    </div>
  </div>
  
</template>

<script setup>
import UserCard from './components/UserCard.vue'
import Composer from './components/Composer.vue'
import StoriesStrip from './components/StoriesStrip.vue'
import FeedList from './components/FeedList.vue'
import RightSidebar from './components/RightSidebar.vue'
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
</style>


