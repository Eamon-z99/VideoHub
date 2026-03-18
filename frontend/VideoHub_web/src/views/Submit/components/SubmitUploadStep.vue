<template>
  <div class="submit-upload-step">
    <div class="upload-tips">
      <div class="tip-card">
        <div class="tip-title">视频大小</div>
        <div class="tip-desc">建议小于 1GB；时长建议小于 10 分钟</div>
      </div>
      <div class="tip-card">
        <div class="tip-title">视频格式</div>
        <div class="tip-desc">推荐上传 MP4/MOV/MKV 格式</div>
      </div>
      <div class="tip-card">
        <div class="tip-title">视频分辨率</div>
        <div class="tip-desc">推荐 1080P；画质越高观感越好</div>
      </div>
    </div>

    <div class="upload-box">
      <input
        ref="videoFileInput"
        type="file"
        accept="video/*"
        style="display: none"
        @change="onVideoFileChange"
      />

      <div class="upload-box__inner" @click="triggerSelectVideo">
        <div class="upload-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="48" height="48">
            <path
              fill="currentColor"
              d="M19 15v4H5v-4H3v4a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-4z"
            />
            <path
              fill="currentColor"
              d="M11 15V7.83L8.41 10.4 7 9l5-5 5 5-1.41 1.41L13 7.83V15z"
            />
          </svg>
        </div>
        <div class="upload-hint">点击上传你的视频文件或拖拽到此区域</div>
        <el-button type="primary" @click.stop="triggerSelectVideo">
          {{ videoFile ? '重新选择视频' : '选择视频' }}
        </el-button>
        <div v-if="selectedVideoName" class="file-meta">
          <div class="file-name">{{ selectedVideoName }}</div>
          <div class="file-size">{{ selectedVideoSize }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['uploaded'])

const videoFileInput = ref(null)
const videoFile = ref(null)
const selectedVideoName = ref('')
const selectedVideoSize = ref('')
const durationSeconds = ref(0)

const formatSize = (size) => {
  if (!size && size !== 0) return ''
  if (size >= 1024 * 1024 * 1024) return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  if (size >= 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  if (size >= 1024) return (size / 1024).toFixed(2) + ' KB'
  return size + ' B'
}

const triggerSelectVideo = () => {
  videoFileInput.value?.click()
}

const onVideoFileChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type?.startsWith('video/')) {
    ElMessage.warning('请选择视频文件')
    return
  }
  videoFile.value = file
  selectedVideoName.value = file.name
  selectedVideoSize.value = formatSize(file.size)
  durationSeconds.value = 0

  // 读取时长（秒）
  try {
    const url = URL.createObjectURL(file)
    const videoEl = document.createElement('video')
    videoEl.preload = 'metadata'
    videoEl.src = url
    videoEl.onloadedmetadata = () => {
      videoEl.pause()
      const dur = videoEl.duration
      durationSeconds.value = typeof dur === 'number' && dur > 0 ? Math.floor(dur) : 0
      URL.revokeObjectURL(url)
    }
    videoEl.onerror = () => {
      durationSeconds.value = 0
      URL.revokeObjectURL(url)
    }
  } catch (e) {
    durationSeconds.value = 0
  }

  // 只选择文件并进入下一步（填写信息），不自动上传
  emit('uploaded', {
    submissionId: '',
    videoFile: videoFile.value,
    videoName: selectedVideoName.value,
    durationSeconds: durationSeconds.value,
  })
}
</script>

<style scoped lang="scss">
.submit-upload-step {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.upload-tips {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.tip-card {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 12px 14px;
}

.tip-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
}

.tip-desc {
  color: #6b7280;
  font-size: 12px;
  line-height: 1.4;
}

.upload-box {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 16px;
}

.upload-box__inner {
  height: 240px;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  color: #64748b;
  cursor: pointer;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.upload-icon {
  color: #94a3b8;
}

.upload-hint {
  font-size: 13px;
}

.file-meta {
  margin-top: 4px;
  text-align: center;
}

.file-name {
  color: #111827;
  font-weight: 600;
  font-size: 13px;
}

.file-size {
  color: #6b7280;
  font-size: 12px;
}

.upload-actions {
  display: flex;
  justify-content: center;
  margin-top: 14px;
}
</style>

