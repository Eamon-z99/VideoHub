<template>
  <div class="video-card">
    <div class="thumb-wrap">
      <img
        :src="video.cover"
        loading="lazy"
        @error="onImgError && onImgError($event)"
      />
      <div class="bottom-gradient"></div>
      <div class="stats-overlay">
        <span class="play-count">
          <svg class="play-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
            <path d="M747.52 893.44H276.48a225.536 225.536 0 0 1-225.28-225.28v-312.32a225.536 225.536 0 0 1 225.28-225.28h471.04a225.536 225.536 0 0 1 225.28 225.28v312.32a225.536 225.536 0 0 1-225.28 225.28z m-471.04-680.96a143.36 143.36 0 0 0-143.36 143.36v312.32a143.36 143.36 0 0 0 143.36 143.36h471.04a143.36 143.36 0 0 0 143.36-143.36v-312.32a143.36 143.36 0 0 0-143.36-143.36z" fill="#FFFFFF"/>
            <path d="M449.4336 692.736a40.96 40.96 0 0 1-40.96-40.96l-1.536-279.5008a40.96 40.96 0 0 1 61.44-35.84l242.8416 138.24a40.96 40.96 0 0 1 0.3584 70.9632L470.0672 687.104a40.96 40.96 0 0 1-20.6336 5.632z m39.7824-249.7536l0.768 137.6256 118.784-69.4784z" fill="#FFFFFF"/>
          </svg>
          {{ formatPlayCount(video.playCount) }}
        </span>
        <span v-if="video.commentCount" class="comment-count">💬 {{ formatPlayCount(video.commentCount) }}</span>
      </div>
      <span class="duration">{{ video.duration }}</span>
      <div v-if="video.isVideo" class="play-overlay">
        <div class="play-button">▶</div>
      </div>
    </div>

    <div class="v-title" :title="video.title">
      {{ video.title }}
    </div>

    <div class="uploader-row">
      <svg class="uploader-icon" viewBox="0 0 1024 1024">
        <path
          d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z"
        />
        <path
          d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z"
        />
      </svg>
      <span class="uploader-name">
        {{ video.uploaderName || video.up }}
      </span>
      <span v-if="video.uploadDate" class="upload-date">
        · {{ video.uploadDate }}
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  video: {
    type: Object,
    required: true
  },
  onImgError: {
    type: Function,
    required: false
  }
})

const formatPlayCount = (count) => {
  if (!count || count === '本地视频') return '0'
  const num = typeof count === 'number' ? count : parseInt(count) || 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}
</script>

<style scoped>
.video-card {
  display: grid;
  grid-template-rows: auto auto auto;
  gap: 6px;
  width: 100%;
}

.thumb-wrap {
  position: relative;
  width: 100%;
  padding-bottom: 56%;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f2f3;
}

.thumb-wrap img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bottom-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.2) 50%, transparent 100%);
  z-index: 1;
  pointer-events: none;
}

.stats-overlay {
  position: absolute;
  left: 6px;
  bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 2;
}

.stats-overlay span {
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}

.play-count {
  align-items: flex-start;
}

.play-count .play-icon {
  margin-top: 1px;
}

.play-icon {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

.duration {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  padding: 2px 6px;
  border-radius: 4px;
  z-index: 2;
}

.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.play-button {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #333;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.thumb-wrap:hover .play-overlay {
  opacity: 1;
}

.v-title {
  font-size: 13px;
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.uploader-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #9499A0;
}

.uploader-icon {
  width: 17px;
  height: 17px;
  fill: currentColor;
  flex-shrink: 0;
}

.uploader-name,
.upload-date {
  white-space: nowrap;
  transform: translateY(-1px);
}
</style>