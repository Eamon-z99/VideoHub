<template>
<div class="video-card">
    <div class="cover-container">
    <img :src="video.cover" class="video-cover">
    <div class="duration">{{ video.duration }}</div>
    </div>
    <h3 class="title">{{ video.title }}</h3>
    <div class="stats">
    <span class="play-count">▶ {{ video.playCount }}</span>
    </div>
</div>
</template>

<script setup>
defineProps({
video: {
    type: Object,
    required: true
}
})
</script>

<style scoped>
.video-card {
background: #fff;
border-radius: 8px;
overflow: hidden;
transition: transform 0.2s;
}

.video-card:hover {
transform: translateY(-5px);
}

.cover-container {
position: relative;
aspect-ratio: 16/9;
}

.video-cover {
width: 100%;
height: 100%;
object-fit: cover;
border-radius: 8px;
}

.duration {
position: absolute;
bottom: 8px;
right: 8px;
background: rgba(0,0,0,0.7);
color: white;
padding: 3px 8px;
border-radius: 4px;
font-size: 12px;
}

.title {
  margin: 10px 0;
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  display: -moz-box; /* 兼容 Firefox */
  display: -ms-flexbox; /* 兼容 IE10 */
  -webkit-box-orient: vertical;
  -moz-box-orient: vertical; /* 兼容 Firefox */
  -ms-box-orient: vertical; /* 兼容 IE10 */
  -webkit-line-clamp: 2; /* 兼容 WebKit 浏览器（如 Chrome、Safari） */
  line-clamp: 2; /* 标准属性 */
  overflow: hidden;
}

.stats {
font-size: 12px;
color: #666;
}
</style>