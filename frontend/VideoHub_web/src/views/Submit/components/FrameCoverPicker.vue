<template>
  <el-dialog v-model="visible" title="从视频截取封面" width="980px" @closed="onClosed">
    <div class="frame-picker">
      <div class="frame-cropper" :style="frameCropperBackdropStyle" :class="{ 'is-switching': frameCropSwitching }">
        <VueCropper
          ref="frameCropperRef"
          class="frame-cropper-core"
          :img="frameCropImg"
          outputType="jpeg"
          :autoCrop="false"
          :centerBox="true"
          :fixed="false"
          :canMove="false"
          :canMoveBox="true"
          :canScale="true"
          :original="false"
          :info="true"
          @img-load="onFrameCropImgLoad"
          @realTime="onFrameCropRealTime"
        />
      </div>

      <div class="frame-controls">
        <div class="frame-preview">
          <video
            ref="frameVideoRef"
            class="frame-video"
            :src="videoObjectUrl"
            muted
            playsinline
            preload="auto"
          />
          <div class="frame-preview-hint">拖动下方时间条选帧（左侧会自动更新为当前帧，可继续裁剪）</div>
        </div>

        <div class="frame-time">
          <span>{{ formatTime(frameCurrentTime) }}</span>
          <span class="sep">/</span>
          <span>{{ formatTime(frameDuration) }}</span>
        </div>

        <el-slider
          v-model="frameCurrentTime"
          :min="0"
          :max="frameDuration"
          :step="0.04"
          :disabled="!frameDuration"
          :show-tooltip="false"
          @input="onFrameTimeInput"
        />

        <div class="frame-actions">
          <el-button @click="seekTo(frameCurrentTime - 1)" :disabled="!frameDuration">-1s</el-button>
          <el-button @click="seekTo(frameCurrentTime + 1)" :disabled="!frameDuration">+1s</el-button>
          <el-button type="primary" @click="captureFrameAsCover" :disabled="!frameDuration || !frameCropImg">
            设为封面
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  videoFile: { type: Object, default: null }, // File
})

const emit = defineEmits(['update:modelValue', 'selected'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const frameVideoRef = ref(null)
const frameDuration = ref(0)
const frameCurrentTime = ref(0)

const videoObjectUrl = ref('')
const rebuildObjectUrl = () => {
  if (videoObjectUrl.value) URL.revokeObjectURL(videoObjectUrl.value)
  videoObjectUrl.value = props.videoFile ? URL.createObjectURL(props.videoFile) : ''
}

watch(
  () => props.videoFile,
  () => {
    if (visible.value) {
      rebuildObjectUrl()
      initVideo()
    }
  }
)

watch(
  () => visible.value,
  async (v) => {
    if (!v) return
    if (!props.videoFile) {
      ElMessage.warning('缺少本地视频文件，无法截取封面（可点击重新上传）')
      visible.value = false
      return
    }
    rebuildObjectUrl()
    await nextTick()
    initVideo()
  }
)

const formatTime = (sec) => {
  const s = Math.max(0, Number(sec) || 0)
  const mm = Math.floor(s / 60)
  const ss = Math.floor(s % 60)
  return `${String(mm).padStart(2, '0')}:${String(ss).padStart(2, '0')}`
}

const frameCropperRef = ref(null)
const frameCropImg = ref('')
const frameCropBackdropImg = ref('')
const frameCropSwitching = ref(false)
const frameCropAxis = ref(null)
const frameCropStarted = ref(false)

const frameCropperBackdropStyle = computed(() => {
  const url = frameCropBackdropImg.value || frameCropImg.value
  if (!url) return { '--frame-cropper-backdrop-opacity': '0' }
  return {
    '--frame-cropper-backdrop': `url(${url})`,
    '--frame-cropper-backdrop-opacity': '1',
  }
})

const onFrameCropImgLoad = (status) => {
  if (status !== 'success') return
  frameCropBackdropImg.value = frameCropImg.value

  nextTick(() => {
    const cropper = frameCropperRef.value
    if (!cropper) return
    cropper.startCrop?.()
    if (frameCropAxis.value && cropper.setCropAxis) {
      cropper.setCropAxis(frameCropAxis.value)
    } else {
      cropper.goAutoCrop?.()
    }
    frameCropStarted.value = true
  })
}

const onFrameCropRealTime = () => {
  const cropper = frameCropperRef.value
  if (!cropper?.getCropAxis) return
  try {
    frameCropAxis.value = cropper.getCropAxis()
  } catch (e) {}
}

let frameCaptureTimer = null
let frameCaptureSeq = 0

const waitImageReady = (src) =>
  new Promise((resolve) => {
    if (!src) return resolve(false)
    const img = new Image()
    img.onload = () => resolve(true)
    img.onerror = () => resolve(false)
    img.src = src
  })

const captureCurrentFrameToCropper = async () => {
  const seq = ++frameCaptureSeq
  const videoEl = frameVideoRef.value
  if (!videoEl) return

  await new Promise((resolve) => {
    if (videoEl.readyState >= 2) return resolve()
    const handler = () => {
      videoEl.removeEventListener('loadeddata', handler)
      resolve()
    }
    videoEl.addEventListener('loadeddata', handler)
  })
  if (seq !== frameCaptureSeq) return

  const w = videoEl.videoWidth || 0
  const h = videoEl.videoHeight || 0
  if (!w || !h) return
  const canvas = document.createElement('canvas')
  canvas.width = w
  canvas.height = h
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.drawImage(videoEl, 0, 0, w, h)
  const nextUrl = canvas.toDataURL('image/jpeg', 0.92)

  if (frameCropImg.value) frameCropBackdropImg.value = frameCropImg.value
  {
    const cropper = frameCropperRef.value
    if (cropper?.getCropAxis) {
      try {
        frameCropAxis.value = cropper.getCropAxis()
      } catch (e) {}
    }
  }
  await waitImageReady(nextUrl)
  if (seq !== frameCaptureSeq) return
  frameCropImg.value = nextUrl
}

const scheduleCaptureCurrentFrame = () => {
  if (frameCaptureTimer) window.clearTimeout(frameCaptureTimer)
  frameCaptureTimer = window.setTimeout(() => {
    captureCurrentFrameToCropper()
  }, 80)
}

const initVideo = () => {
  const videoEl = frameVideoRef.value
  if (!videoEl) return

  frameDuration.value = 0
  frameCurrentTime.value = 0
  frameCropImg.value = ''
  frameCropBackdropImg.value = ''
  frameCropAxis.value = null
  frameCropStarted.value = false

  videoEl.onloadedmetadata = null
  videoEl.onseeked = null
  videoEl.onloadedmetadata = () => {
    const dur = Number(videoEl.duration)
    frameDuration.value = Number.isFinite(dur) && dur > 0 ? dur : 0
    frameCurrentTime.value = 0
    scheduleCaptureCurrentFrame()
  }
  videoEl.onseeked = () => {
    scheduleCaptureCurrentFrame()
  }
  try {
    videoEl.currentTime = 0
    videoEl.load?.()
  } catch (e) {}
}

const onFrameTimeInput = (val) => {
  seekTo(val)
}

const seekTo = (t) => {
  const videoEl = frameVideoRef.value
  if (!videoEl || !frameDuration.value) return
  const clamped = Math.min(frameDuration.value, Math.max(0, Number(t) || 0))
  frameCurrentTime.value = clamped
  videoEl.currentTime = clamped
  scheduleCaptureCurrentFrame()
}

const captureFrameAsCover = async () => {
  if (!frameCropImg.value) {
    await captureCurrentFrameToCropper()
  }
  const cropper = frameCropperRef.value
  if (!cropper?.getCropBlob) {
    ElMessage.error('裁剪组件未就绪，请刷新页面后重试')
    return
  }

  const blob = await new Promise((resolve) => {
    cropper.getCropBlob((b) => resolve(b || null))
  })
  if (!blob) {
    ElMessage.error('裁剪失败，请稍等片刻再试')
    return
  }

  const file = new File([blob], 'cover.jpg', { type: blob.type || 'image/jpeg' })
  emit('selected', { file, name: '从视频截取' })
  visible.value = false
}

const onClosed = () => {
  if (frameCaptureTimer) {
    window.clearTimeout(frameCaptureTimer)
    frameCaptureTimer = null
  }
  frameCropSwitching.value = false
  if (videoObjectUrl.value) {
    URL.revokeObjectURL(videoObjectUrl.value)
  }
  videoObjectUrl.value = ''
}

onBeforeUnmount(() => {
  if (videoObjectUrl.value) URL.revokeObjectURL(videoObjectUrl.value)
  if (frameCaptureTimer) window.clearTimeout(frameCaptureTimer)
})
</script>

<style scoped lang="scss">
.frame-picker {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 12px;
  min-height: 440px;
}

.frame-cropper {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  background: #0b1220;
  border: 1px solid #111827;
}

.frame-cropper::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: var(--frame-cropper-backdrop);
  background-size: cover;
  background-position: center;
  filter: blur(14px);
  opacity: var(--frame-cropper-backdrop-opacity, 0);
  transform: scale(1.06);
  transition: opacity 120ms ease;
}

.frame-cropper-core {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
}

.frame-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.frame-preview {
  background: #0b1220;
  border: 1px solid #111827;
  border-radius: 10px;
  padding: 10px;
}

.frame-video {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  object-fit: contain;
  background: #000;
}

.frame-preview-hint {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.4;
}

.frame-time {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  color: #334155;
  font-size: 12px;
}

.sep {
  color: #94a3b8;
}

.frame-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>

