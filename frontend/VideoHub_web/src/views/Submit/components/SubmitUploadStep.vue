<template>
  <div class="submit-upload-step">
    <div class="upload-tips">
      <div class="tip-card">
        <div class="tip-icon" aria-hidden="true">
          <svg class="tip-svg" viewBox="0 0 250 129" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="linearGradient-1" x1="0.5" y1="0" x2="0.5" y2="1">
                <stop offset="0" stop-color="#c1dbfe" />
                <stop offset="1" stop-color="#ffffff" />
              </linearGradient>
              <linearGradient id="linearGradient-2" x1="0.5" y1="0" x2="0.8432293" y2="0.40023464">
                <stop offset="0" stop-color="#afc0ef" />
                <stop offset="1" stop-color="#6e94e7" />
              </linearGradient>
              <linearGradient id="linearGradient-3" x1="0.5" y1="0" x2="0.981224" y2="0.40023464">
                <stop offset="0" stop-color="#afc0ef" />
                <stop offset="1" stop-color="#6e94e7" />
              </linearGradient>
              <linearGradient id="linearGradient-4" x1="0.23125735" y1="0.13331176" x2="0.7208288" y2="0.8687874">
                <stop offset="0" stop-color="#b9d1f5" />
                <stop offset="1" stop-color="#89aff1" />
              </linearGradient>
              <linearGradient id="linearGradient-5" x1="0.5" y1="0" x2="0.3793453" y2="1">
                <stop offset="0" stop-color="#4b75cc" />
                <stop offset="1" stop-color="#8db1f9" />
              </linearGradient>
              <linearGradient id="linearGradient-6" x1="0.95891416" y1="0.5" x2="-0.18698098" y2="0.29170907">
                <stop offset="0" stop-color="#bbccf8" />
                <stop offset="1" stop-color="#7c9bdc" />
              </linearGradient>
              <linearGradient id="linearGradient-7" x1="0.5" y1="0" x2="0.5" y2="1">
                <stop offset="0" stop-color="#aaceff" />
                <stop offset="1" stop-color="#97b4f6" />
              </linearGradient>
            </defs>
            <g>
              <path d="M249.28 129C219.86 92.49 172.8 71 122.26 71 73.29 71 29.44 92.93 0 127.52Z" fill="url(#linearGradient-1)" />
              <path d="M73.34 33.52 117.6 23.32v65.07H73.34z" fill="url(#linearGradient-2)" />
              <path d="M117.6 23.32 169.53 36.26v52.13H117.13z" fill="url(#linearGradient-3)" />
              <path d="M73.34 33.52v54.87L124.71 110.82V49.22z" fill="url(#linearGradient-4)" />
              <path d="M124.6 49.22v61.6L169.53 87.62V36.26z" fill="url(#linearGradient-5)" />
              <path d="M124.6 49.22 140.77 79.48 184.35 58.02 169.53 36.26z" fill="url(#linearGradient-6)" />
              <path d="M73.34 33.52 60 55.86l13.34 5.49z" fill="url(#linearGradient-7)" />
              <path d="M81.6 52 116.21 62.91" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
              <path d="M81.97 64.59 100.65 70.48" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
              <path d="M81.97 76.59 97.05 81.34" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" />
              <path d="M141.34 11.4v11.81L167.36 0z" fill="#406ac6" />
              <path d="M167.36 0 145.52 14.11 157.15 21.93z" fill="#bcd0f1" />
              <path d="M133.6 4.42 141.34 11.61 167.36 0z" fill="#bcd0f1" />
              <path
                d="M138.61 15c-10.49 8.12-15.74 13.78-15.74 16.97 0 4.79 3.37 6.35 6.45 6.35s4.4-5.49-1.52-6.35c-3.95-.57-7.69 4.56-11.2 15.4"
                stroke="#b7dcfc"
                stroke-width="1"
                stroke-linecap="round"
                stroke-dasharray="2 2"
                fill="none"
              />
            </g>
          </svg>
        </div>
        <div class="tip-text">
          <div class="tip-title">视频大小</div>
          <div class="tip-desc">建议小于 1GB；时长建议小于 10 分钟</div>
        </div>
      </div>
      <div class="tip-card">
        <div class="tip-icon" aria-hidden="true">
          <svg class="tip-svg" viewBox="0 0 250 129" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="linearGradient-1" x1="0.5" y1="0" x2="0.5" y2="1">
                <stop offset="0" stop-color="#c1dbfe" />
                <stop offset="1" stop-color="#ffffff" />
              </linearGradient>
              <linearGradient id="linearGradient-2" x1="0.5" y1="0" x2="0.5" y2="0.9841902">
                <stop offset="0" stop-color="#a5c3f4" />
                <stop offset="1" stop-color="#789eea" />
              </linearGradient>
            </defs>
            <g>
              <path d="M249.28 129C219.86 92.49 172.8 71 122.26 71 73.29 71 29.44 92.93 0 127.52Z" fill="url(#linearGradient-1)" />
              <path
                d="M60.52 79.53h-4.67V63.52c0-1.28 1.04-2.31 2.32-2.31h.02c1.31 0 2.33 1.03 2.33 2.31v16.01z"
                fill="#bae637"
              />
              <path
                d="M62.84 83.45h-4.68v-8.36c0-1.24 1.01-2.25 2.27-2.25h.14c1.26 0 2.27 1.01 2.27 2.25v8.36z"
                fill="#389e0d"
              />
              <path
                d="M56.53 79.52h-4.72v-9.09c0-1.25 1.02-2.26 2.28-2.26h.16c1.26 0 2.28 1.01 2.28 2.26v9.09z"
                fill="#7cb305"
              />
              <path
                d="M63.12 92.8H50.85a.88.88 0 0 1-.88-.87V79.86c0-.48.39-.87.88-.87h12.27c.48 0 .88.39.88.87v12.07c0 .48-.4.87-.88.87"
                fill="#3b68b8"
              />
              <path
                d="M186.93 78.48h-4.05V64.97c0-1.07.9-1.95 2.02-1.95h.02c1.11 0 2.01.88 2.01 1.95v13.51z"
                fill="#bae637"
              />
              <path
                d="M188.95 81.79h-4.07v-7.06c0-1.04.89-1.89 1.97-1.89h.13c1.08 0 1.97.85 1.97 1.89v7.06z"
                fill="#389e0d"
              />
              <path
                d="M183.48 78.48h-4.09V70.8c0-1.05.88-1.9 1.97-1.9h.14c1.09 0 1.97.85 1.97 1.9v7.68z"
                fill="#7cb305"
              />
              <path
                d="M189.19 89.68h-10.64a.76.76 0 0 1-.76-.72v-10.2c0-.41.34-.73.76-.73h10.64c.42 0 .76.32.76.73v10.19c0 .41-.34.73-.76.73"
                fill="#3b68b8"
              />
              <path
                d="M86.13 100.95h-2.5v-8.68c0-.69.56-1.25 1.24-1.25h.02c.68 0 1.24.56 1.24 1.25v8.68z"
                fill="#bae637"
              />
              <path
                d="M87.37 103.08h-2.5v-4.54c0-.67.54-1.22 1.21-1.22h.07c.67 0 1.22.55 1.22 1.22v4.54z"
                fill="#389e0d"
              />
              <path
                d="M84 100.95h-2.52v-4.93c0-.68.55-1.23 1.22-1.23h.08c.68 0 1.22.55 1.22 1.23v4.93z"
                fill="#7cb305"
              />
              <path
                d="M87.52 108.15h-6.55a.47.47 0 0 1-.47-.47v-6.55c0-.26.21-.47.47-.47h6.55c.26 0 .46.21.46.47v6.55c0 .26-.2.47-.46.47"
                fill="#3b68b8"
              />
              <path
                d="M129.82 0 132.25 10.86a38.7 38.7 0 0 1 12.04 5.42l9.51-6.57 10.12 10.12-6.23 9.72a38.1 38.1 0 0 1 4.54 11.67l11.77 2.21v14.07l-11.5 2.57a48 48 0 0 1-5.08 12.18l6.7 10.46-10.11 10.11-10.43-7.19.32-.32a38.9 38.9 0 0 1-11.65 4.51L129.82 99h-14.07l-2.13-11.38a37.4 37.4 0 0 1-9.6-4.04l-10.02 6.91-10.11-10.12 6.49-10.13a37.9 37.9 0 0 1-4.88-11.67L74 55.82V41.75l11.77-2.21a39.7 39.7 0 0 1 4.13-9.99l-6.67-10.4 10.12-10.12 9.91 6.84a37.9 37.9 0 0 1 10.36-4.49L115.75 0h14.07zm-5.82 32c-9.94 0-18 8.06-18 18s8.06 18 18 18 18-8.06 18-18-8.06-18-18-18z"
                fill="url(#linearGradient-2)"
              />
            </g>
          </svg>
        </div>
        <div class="tip-text">
          <div class="tip-title">视频格式</div>
          <div class="tip-desc">推荐上传 MP4/MOV/MKV 格式</div>
        </div>
      </div>
      <div class="tip-card">
        <div class="tip-icon" aria-hidden="true">
          <svg class="tip-svg" viewBox="0 0 250 145" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="linearGradient-1" x1="0.5" y1="0" x2="0.5" y2="1">
                <stop offset="0" stop-color="#c1dbfe" />
                <stop offset="1" stop-color="#ffffff" />
              </linearGradient>
              <linearGradient id="linearGradient-2" x1="0.5366026" y1="0.69037145" x2="0.14728568" y2="0.3958288">
                <stop offset="0" stop-color="#adaeb6" />
                <stop offset="1" stop-color="#ffffff" stop-opacity="0" />
              </linearGradient>
              <linearGradient id="linearGradient-3" x1="0.5" y1="0" x2="0.32400462" y2="0.82585067">
                <stop offset="0" stop-color="#6d94e3" />
                <stop offset="1" stop-color="#658dde" />
              </linearGradient>
              <linearGradient id="linearGradient-4" x1="0.5" y1="0" x2="0.31971774" y2="0.82585067">
                <stop offset="0" stop-color="#b2c5f4" />
                <stop offset="1" stop-color="#9eb5eb" />
              </linearGradient>
              <linearGradient id="linearGradient-5" x1="0.5" y1="0" x2="0.39082652" y2="0.6836543">
                <stop offset="0" stop-color="#eaeef1" />
                <stop offset="1" stop-color="#feffff" />
              </linearGradient>
              <linearGradient id="linearGradient-8" x1="0.5" y1="0.36850756" x2="0.57458925" y2="0.6031876">
                <stop offset="0" stop-color="#4d7be1" />
                <stop offset="0.45177892" stop-color="#9bb9fc" />
                <stop offset="1" stop-color="#5a85e3" />
              </linearGradient>
            </defs>
            <g>
              <path d="M249.28 145C219.86 108.49 172.8 87 122.26 87 73.29 87 29.44 108.93 0 143.52Z" fill="url(#linearGradient-1)" />
              <path d="M93 115 158 107.43 99.63 46 49 71.32z" fill="url(#linearGradient-2)" fill-opacity="0.43" />
              <path
                d="M64.75 97.6h-2.99V87.48c0-.81.67-1.46 1.49-1.46h.01c.83 0 1.49.65 1.49 1.46V97.6z"
                fill="#bae637"
              />
              <path
                d="M66.24 100.08h-3V94.79c0-.78.65-1.42 1.45-1.42h.1c.8 0 1.45.64 1.45 1.42v5.29z"
                fill="#389e0d"
              />
              <path
                d="M62.2 97.59h-3.02V91.85c0-.79.65-1.43 1.46-1.43h.1c.81 0 1.46.64 1.46 1.43v5.74z"
                fill="#7cb305"
              />
              <path
                d="M66.42 105.99h-7.86a.56.56 0 0 1-.56-.55v-7.63c0-.31.25-.55.56-.55h7.86c.31 0 .56.24.56.55v7.63c0 .3-.25.55-.56.55"
                fill="#3b68b8"
              />
              <path
                d="M196.85 102.8h-4.36V89.01c0-1.1.97-1.99 2.17-1.99h.03c1.2 0 2.17.89 2.17 1.99v13.79z"
                fill="#bae637"
              />
              <path
                d="M199.02 106.17h-4.38v-7.2c0-1.07.95-1.93 2.12-1.93h.13c1.18 0 2.13.86 2.13 1.93v7.2z"
                fill="#389e0d"
              />
              <path
                d="M193.12 102.79h-4.4V94.96c0-1.07.95-1.94 2.13-1.94H191c1.18 0 2.12.87 2.12 1.94v7.83z"
                fill="#7cb305"
              />
              <path
                d="M199.28 114.23h-11.46c-.45 0-.82-.34-.82-.75v-10.4c0-.41.37-.75.82-.75h11.46c.45 0 .82.34.82.75v10.4c0 .41-.37.75-.82.75"
                fill="#3b68b8"
              />
              <path
                d="M74.92 126.17h-2.18v-7.13c0-.57.49-1.03 1.09-1.03h.01c.6 0 1.08.46 1.08 1.03v7.13z"
                fill="#bae637"
              />
              <path
                d="M76.01 127.92h-2.19v-3.73c0-.55.48-1.01 1.06-1.01h.07c.58 0 1.06.46 1.06 1.01v3.73z"
                fill="#389e0d"
              />
              <path
                d="M73.06 126.17h-2.2v-4.05c0-.56.48-1.01 1.06-1.01h.08c.59 0 1.06.45 1.06 1.01v4.05z"
                fill="#7cb305"
              />
              <path
                d="M76.14 132.08H70.41a.41.41 0 0 1-.41-.39v-5.37c0-.22.18-.39.41-.39h5.73c.22 0 .41.17.41.39v5.37c0 .22-.19.39-.41.39"
                fill="#3b68b8"
              />
              <path
                d="M96.07 9.21 151.07.26c6.55-1.06 12.71 3.38 13.77 9.92.11.64.16 1.28.16 1.93v86.84c0 5.88-4.27 10.9-10.07 11.84l-55 8.95c-6.55 1.06-12.71-3.38-13.77-9.92a11.9 11.9 0 0 1-.16-1.93V21.05c0-5.88 4.27-10.9 10.07-11.84z"
                fill="url(#linearGradient-3)"
              />
              <path
                d="M98.27 12.45 151.27 4.02c6-0.95 11.64 3.14 12.59 9.14.09.57.14 1.15.14 1.73V97c0 5.4-3.93 10.01-9.27 10.86l-53 8.43c-6 0.95-11.64-3.14-12.59-9.14-.09-.57-.14-1.15-.14-1.72V23.32c0-5.41 3.93-10.02 9.27-10.87z"
                fill="url(#linearGradient-4)"
              />
              <path
                d="M95 19.77v84.6c0 4.97 4.03 9 9 9 .5 0 .99-.04 1.49-.13L160 104.12 161.91 9 95 19.77z"
                fill="#eaf2ff"
              />
              <path
                d="M110 7.44 143.38 3.03c2.28-.27 3.61 1.2 3.98 4.41.38 3.2.59 6.01.64 8.42l-29.95 4.74a2.34 2.34 0 0 1-2.29-1.66 2.5 2.5 0 0 1-.02-.42c.24-4.37-.09-7.25-.98-8.64C113.7 8.25 112.11 7.44 110 7.44z"
                fill="#4069bd"
              />
              <path
                d="M109 7.56 142.84 2c2.58.24 4.07 2.1 4.44 5.56.3 2.74.51 5.29.64 7.65.05 1.02-.68 1.93-1.7 2.08L117.9 21.62a2.38 2.38 0 0 1-2.28-1.68 2.5 2.5 0 0 1-.02-.44c.33-4.7.04-7.8-.86-9.3C113.68 8.44 111.77 7.56 109 7.56z"
                fill="url(#linearGradient-8)"
              />
              <path
                d="M105.23 26.56l2.32-.4c3.26-.57 6.37 1.62 6.93 4.88.04.24.07.49.08.73l.17 3.46c.16 3.03-1.98 5.7-4.96 6.21l-2.32.4c-3.26.57-6.37-1.62-6.93-4.88a5 5 0 0 1-.08-.73l-.17-3.46c-.16-3.03 1.98-5.7 4.96-6.21z"
                fill="#afc2f3"
              />
              <path
                d="M107.23 50.56l2.32-.4c3.26-.57 6.37 1.62 6.93 4.88.04.24.07.49.08.73l.17 3.46c.16 3.03-1.98 5.7-4.96 6.21l-2.32.4c-3.26.57-6.37-1.62-6.93-4.88a5 5 0 0 1-.08-.73l-.17-3.46c-.16-3.03 1.98-5.7 4.96-6.21z"
                fill="#afc2f3"
              />
              <path
                d="M116.71 25.29 156 19.26c.89-.13 1.71.47 1.84 1.35.01.04.01.08.02.11.08.97-.61 1.84-1.57 1.99l-39.29 6.03a1.73 1.73 0 0 1-1.86-1.46 1.8 1.8 0 0 1-.02-.11c-.08-.97.61-1.84 1.57-1.99z"
                fill="#afc2f3"
              />
              <path
                d="M118.71 49.29 158 43.26c.89-.13 1.71.47 1.84 1.35.01.04.01.08.02.11.08.97-.61 1.84-1.57 1.99l-39.29 6.03a1.73 1.73 0 0 1-1.86-1.46 1.8 1.8 0 0 1-.02-.11c-.08-.97.61-1.84 1.57-1.99z"
                fill="#afc2f3"
              />
              <path
                d="M116.71 30.29 156 24.26c.89-.13 1.71.47 1.84 1.35.01.04.01.08.02.11.08.97-.61 1.84-1.57 1.99l-39.29 6.03a1.73 1.73 0 0 1-1.86-1.46 1.8 1.8 0 0 1-.02-.11c-.08-.97.61-1.84 1.57-1.99z"
                fill="#afc2f3"
              />
              <path
                d="M118.62 54.51 142.09 51.23c.85-.12 1.64.47 1.76 1.32 0 .03.01.05.01.08.08.91-.57 1.73-1.48 1.86l-23.47 3.28a1.73 1.73 0 0 1-1.76-1.32 1.6 1.6 0 0 1-.01-.08c-.08-.91.57-1.73 1.48-1.86z"
                fill="#afc2f3"
              />
              <path
                d="M117.62 37.52 127.09 36.22c.85-.11 1.64.48 1.76 1.34 0 .02.01.05.01.07.08.91-.57 1.73-1.48 1.86l-9.47 1.3a1.73 1.73 0 0 1-1.76-1.32 1.6 1.6 0 0 1-.01-.07c-.08-.91.57-1.73 1.48-1.86z"
                fill="#afc2f3"
              />
              <path
                d="M119.62 59.52 129.09 58.22c.85-.11 1.64.48 1.76 1.34 0 .02.01.05.01.07.08.91-.57 1.73-1.48 1.86l-9.47 1.3a1.73 1.73 0 0 1-1.76-1.32 1.6 1.6 0 0 1-.01-.07c-.08-.91.57-1.73 1.48-1.86z"
                fill="#afc2f3"
              />
            </g>
          </svg>
        </div>
        <div class="tip-text">
          <div class="tip-title">视频分辨率</div>
          <div class="tip-desc">推荐 1080P；画质越高观感越好</div>
        </div>
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
  gap: 55px;
  padding:10px 0;
}

.upload-tips {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.tip-card {
  background: #ffffff;
  //border: 1px solid #eef2f7;
  //border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.tip-icon {
  width: 75px;
  height: 75px;
  flex: 0 0 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.tip-svg {
  width: 75px;
  height: 75px;
  display: block;
}

.tip-text {
  min-width: 0;
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
  border: none;
  border-radius: 0;
  padding: 0 0 16px;
}

.upload-box__inner {
  height: 425px;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  color: #64748b;
  cursor: pointer;
  background: #fafafb;
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

.submit-upload-step :deep(.el-card__body) {
  border: none !important;
}
</style>

