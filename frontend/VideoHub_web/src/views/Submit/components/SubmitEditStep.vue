<template>
  <div class="submit-edit-step">
    <div class="edit-header">
      <div class="file-row">
        <div class="file-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path
              fill="currentColor"
              d="M8 5a3 3 0 0 1 3-3h2a3 3 0 0 1 3 3v14a3 3 0 0 1-3 3h-2a3 3 0 0 1-3-3V5z"
            />
          </svg>
        </div>
        <div class="file-meta">
          <div class="file-name">{{ videoName || '已上传视频' }}</div>
          <div class="file-status">已上传</div>
        </div>
        <div class="file-actions">
          <el-button link type="primary" @click="emit('back')">重新上传</el-button>
        </div>
      </div>
      <el-progress :percentage="100" :stroke-width="6" status="success" />
    </div>

    <el-form label-width="72px" class="edit-form">
      <el-form-item label="封面" required>
        <div class="cover-row">
          <input
            ref="coverFileInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="onCoverFileChange"
          />
          <div class="cover-left">
            <div class="cover-preview" @click="triggerSelectCover">
              <img v-if="coverPreview" :src="coverPreview" alt="封面预览" />
              <div v-else class="cover-empty">点击设置封面</div>
            </div>
            <div class="cover-actions">
              <el-button size="small" @click="triggerSelectCover">上传封面</el-button>
              <el-button
                size="small"
                :loading="loadingDraftVideo"
                :disabled="!videoFileForFrame || loadingDraftVideo"
                @click="framePickerVisible = true"
              >
                从视频截取
              </el-button>
              <span v-if="coverName" class="cover-name">已选择：{{ coverName }}</span>
            </div>
          </div>
        </div>
      </el-form-item>

      <FrameCoverPicker
        v-model="framePickerVisible"
        :video-file="videoFileForFrame"
        @selected="onFrameCoverSelected"
      />

      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="80" show-word-limit placeholder="请输入视频标题" />
      </el-form-item>

      <el-form-item label="类型">
        <el-radio-group v-model="form.copyright">
          <el-radio-button label="original">自制</el-radio-button>
          <el-radio-button label="repost">转载</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="分区">
        <el-select v-model="form.partition" placeholder="请选择分区" style="width: 260px">
          <el-option v-for="opt in partitionOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <div class="tag-editor">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="按回车键Enter创建标签"
            style="width: 100%"
          >
            <el-option v-for="opt in tagSuggestionOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
          <div class="tag-suggestions" v-if="tagSuggestionOptions.length">
            <span class="tag-suggestions__label">推荐标签：</span>
            <el-button
              v-for="t in tagSuggestionOptions"
              :key="t"
              size="small"
              :type="form.tags.includes(t) ? 'primary' : 'default'"
              @click="toggleTag(t)"
            >
              {{ t }}
            </el-button>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="简介">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="填写更完整的相关信息，让更多的人喜欢你的视频吧"
        />
      </el-form-item>

      <el-form-item label="定时发布">
        <div class="schedule-row">
          <el-switch v-model="form.scheduleEnabled" />
          <span class="schedule-hint">可选择距离当前最早5分钟/最晚15天的时间</span>
          <div v-if="form.scheduleEnabled" class="schedule-picker">
            <el-date-picker
              v-model="form.schedulePublishAt"
              type="datetime"
              placeholder="选择发布时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 260px"
            />
          </div>
        </div>
      </el-form-item>

      <el-form-item label="加入合集">
        <div class="collection-row">
          <el-switch v-model="form.collectionEnabled" />
          <span class="collection-hint">合集功能可用于收集整理系列投稿件</span>
          <div v-if="form.collectionEnabled" class="collection-editor">
            <el-input
              v-model="form.collectionName"
              placeholder="输入合集名称（示例：XX系列）"
              style="width: 260px"
              maxlength="40"
              show-word-limit
            />
          </div>
        </div>
      </el-form-item>

      <el-form-item label="二创设置">
        <el-checkbox v-model="form.allowSecondCreation">允许二创</el-checkbox>
      </el-form-item>

      <el-form-item label="商业推广">
        <el-checkbox v-model="form.commercialPromotion">增加商业推广信息</el-checkbox>
      </el-form-item>

      <el-form-item>
        <div class="form-actions">
          <el-button type="primary" plain :disabled="saving || submitting" @click="saveDraft">
            存草稿
          </el-button>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="saving || submitting"
            @click="submitNow"
          >
            立即投稿
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateVideoSubmission, uploadVideo } from '@/api/video'
import FrameCoverPicker from './FrameCoverPicker.vue'

const props = defineProps({
  submissionId: { type: String, default: '' },
  videoFile: { type: Object, default: null }, // File，可用于截取封面
  videoName: { type: String, default: '' },
  durationSeconds: { type: Number, default: 0 },
  initialDraft: { type: Object, default: null }, // 从草稿箱进入时的回填数据
})

const emit = defineEmits(['back', 'done', 'created'])

const partitionOptions = [
  { label: '游戏', value: 'game' },
  { label: '动画', value: 'anime' },
  { label: '生活', value: 'life' },
  { label: '科技', value: 'tech' },
  { label: '音乐', value: 'music' },
  { label: '知识', value: 'knowledge' },
]

const tagSuggestionOptions = ref(['短片', '视频剪辑', '剪辑', '原创', '搞笑'])

const form = ref({
  title: '',
  description: '',
  copyright: 'original',
  partition: 'game',
  tags: [],
  scheduleEnabled: false,
  schedulePublishAt: '',
  collectionEnabled: false,
  collectionName: '',
  allowSecondCreation: false,
  commercialPromotion: false,
})

watch(
  () => props.videoName,
  (name) => {
    if (!form.value.title && name) {
      const dot = name.lastIndexOf('.')
      form.value.title = dot > 0 ? name.slice(0, dot) : name
    }
  },
  { immediate: true }
)

watch(
  () => props.initialDraft,
  (d) => {
    if (!d) return
    // 回填草稿表单（不覆盖用户已经手动修改的字段：仅在当前为空时回填）
    if (!form.value.title && d.title) form.value.title = String(d.title)
    if (!form.value.description && d.description) form.value.description = String(d.description)
    if (d.copyright) form.value.copyright = String(d.copyright)
    if (d.partition) form.value.partition = String(d.partition)
    try {
      const tags = d.tags ? JSON.parse(String(d.tags)) : []
      if (Array.isArray(tags) && form.value.tags.length === 0) form.value.tags = tags
    } catch (e) {}
    form.value.scheduleEnabled = Number(d.schedule_enabled || 0) === 1
    if (d.schedule_publish_at) form.value.schedulePublishAt = String(d.schedule_publish_at)
    form.value.collectionEnabled = Number(d.collection_enabled || 0) === 1
    if (d.collection_name) form.value.collectionName = String(d.collection_name)
    form.value.allowSecondCreation = Number(d.allow_second_creation || 0) === 1
    form.value.commercialPromotion = Number(d.commercial_promotion || 0) === 1
    if (d.cover_url && !coverPreview.value) {
      // 草稿里若已有封面路径，先用于预览（不自动转 File）
      coverPreview.value = normalizeCoverUrl(String(d.cover_url))
      coverName.value = '已保存封面'
    }
  },
  { immediate: true }
)

const normalizeLocalVideoUrl = (path) => {
  if (!path) return ''
  const p = String(path).replaceAll('\\', '/')
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  if (p.startsWith('/local-videos/')) return p
  if (p.startsWith('/')) return '/local-videos' + p
  return '/local-videos/' + p
}

// 草稿箱进入时没有本地 File，需要从已保存的视频拉取 blob 供截帧使用
const videoFileForFrame = ref(props.videoFile || null)
const loadingDraftVideo = ref(false)

watch(
  () => props.videoFile,
  (f) => {
    if (f) videoFileForFrame.value = f
  },
  { immediate: true }
)

watch(
  () => props.initialDraft,
  async (d) => {
    if (!d) return
    if (videoFileForFrame.value) return
    const storagePath = d.storage_path || d.source_file
    if (!storagePath) return
    const url = normalizeLocalVideoUrl(storagePath)
    loadingDraftVideo.value = true
    try {
      const resp = await fetch(url)
      if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}`)
      }
      const blob = await resp.blob()
      const name = String(d.source_file || d.storage_path || 'draft-video').split('/').pop() || 'draft-video.mp4'
      const file = new File([blob], name, { type: blob.type || 'video/mp4' })
      videoFileForFrame.value = file
    } catch (e) {
      // 不阻断编辑，只是无法截帧
      ElMessage.warning('草稿视频文件读取失败，无法从视频截取封面（可重新上传视频）')
      videoFileForFrame.value = null
    } finally {
      loadingDraftVideo.value = false
    }
  },
  { immediate: true }
)

const normalizeCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/local-videos/')) return url
  if (url.startsWith('/')) {
    if (url.startsWith('/uploads/') || url.startsWith('/uploads\\')) {
      return '/local-videos' + url
    }
    return url
  }
  if (url.startsWith('uploads/') || url.startsWith('uploads\\')) {
    return '/local-videos/' + url.replaceAll('\\', '/')
  }
  return '/local-videos/' + url.replaceAll('\\', '/')
}

const toggleTag = (t) => {
  const idx = form.value.tags.indexOf(t)
  if (idx >= 0) form.value.tags.splice(idx, 1)
  else form.value.tags.push(t)
}

const coverFileInput = ref(null)
const coverFile = ref(null)
const coverName = ref('')
const coverPreview = ref('')

const hasCover = computed(() => !!coverPreview.value)

const triggerSelectCover = () => {
  coverFileInput.value?.click()
}

const onCoverFileChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type?.startsWith('image/')) {
    ElMessage.warning('封面必须是图片文件')
    return
  }
  setCover(file, file.name)
}

const setCover = (file, name) => {
  if (coverPreview.value) {
    URL.revokeObjectURL(coverPreview.value)
  }
  coverFile.value = file
  coverName.value = name || file.name || '封面'
  coverPreview.value = URL.createObjectURL(file)
}

const framePickerVisible = ref(false)
const onFrameCoverSelected = (payload) => {
  const file = payload?.file
  if (!file) return
  setCover(file, payload?.name || '从视频截取')
}

const saving = ref(false)
const submitting = ref(false)
const creating = ref(false)
const canSaveDraft = computed(() => !!props.submissionId)

const buildUpdateFormData = () => {
  const fd = new FormData()
  fd.append('title', (form.value.title || '').trim() || '用户投稿视频')
  fd.append('description', (form.value.description || '').trim())
  fd.append('copyright', form.value.copyright)
  fd.append('partition', form.value.partition || '')
  fd.append('tags', JSON.stringify(form.value.tags || []))
  fd.append('scheduleEnabled', form.value.scheduleEnabled ? '1' : '0')
  if (form.value.scheduleEnabled && form.value.schedulePublishAt) {
    fd.append('schedulePublishAt', form.value.schedulePublishAt)
  }
  fd.append('collectionEnabled', form.value.collectionEnabled ? '1' : '0')
  if (form.value.collectionEnabled && (form.value.collectionName || '').trim()) {
    fd.append('collectionName', form.value.collectionName.trim())
  }
  fd.append('allowSecondCreation', form.value.allowSecondCreation ? '1' : '0')
  fd.append('commercialPromotion', form.value.commercialPromotion ? '1' : '0')
  if (coverFile.value) {
    fd.append('cover', coverFile.value)
  }
  return fd
}

const buildCreateFormData = () => {
  const fd = buildUpdateFormData()
  if (!props.videoFile) {
    return fd
  }
  fd.append('file', props.videoFile)
  if (props.durationSeconds > 0) {
    fd.append('duration', String(props.durationSeconds))
  }
  return fd
}

const ensureSubmissionId = async (draftOnly) => {
  if (props.submissionId) return props.submissionId
  if (!props.videoFile) {
    ElMessage.warning('请先选择要上传的视频文件')
    return ''
  }
  if (creating.value) return ''
  creating.value = true
  try {
    const fd = buildCreateFormData()
    const { data } = await uploadVideo(fd, { draftOnly: draftOnly ? 1 : 0 })
    if (data?.success && data?.submissionId) {
      emit('created', data.submissionId)
      return data.submissionId
    }
    ElMessage.error(data?.message || '创建投稿失败，请稍后重试')
    return ''
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '创建投稿失败，请稍后重试')
    return ''
  } finally {
    creating.value = false
  }
}

const saveDraft = async () => {
  if (!hasCover.value) {
    ElMessage.warning('请先设置封面')
    return
  }
  const sid = canSaveDraft.value ? props.submissionId : await ensureSubmissionId(true)
  if (!sid) {
    return
  }
  const fd = buildUpdateFormData()
  saving.value = true
  try {
    const { data } = await updateVideoSubmission(sid, fd, { submitNow: 0 })
    if (data?.success) {
      ElMessage.success('已存入草稿箱（可在内容管理中继续编辑）')
      emit('done')
    } else {
      ElMessage.error(data?.message || '保存草稿失败，请稍后重试')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存草稿失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const submitNow = async () => {
  if (!hasCover.value) {
    ElMessage.warning('请先设置封面')
    return
  }
  const sid = canSaveDraft.value ? props.submissionId : await ensureSubmissionId(false)
  if (!sid) {
    return
  }
  const fd = buildUpdateFormData()
  submitting.value = true
  try {
    const { data } = await updateVideoSubmission(sid, fd, { submitNow: 1 })
    if (data?.success) {
      ElMessage.success('已提交审核，可在内容管理中查看进度')
      emit('done')
    } else {
      ElMessage.error(data?.message || '提交投稿失败，请稍后重试')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交投稿失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.submit-edit-step {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.edit-header {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 14px 14px 10px;
}

.file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.file-icon {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #00a1d6;
  background: #e6f6fb;
  border-radius: 8px;
}

.file-meta {
  flex: 1;
}

.file-name {
  font-weight: 600;
  color: #111827;
  font-size: 13px;
}

.file-status {
  color: #16a34a;
  font-size: 12px;
  margin-top: 2px;
}

.edit-form {
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 16px 14px;
}

.cover-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.cover-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cover-preview {
  width: 124px;
  height: 70px;
  border-radius: 10px;
  border: 1px dashed #cbd5e1;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-empty {
  color: #64748b;
  font-size: 12px;
}

.cover-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.cover-name {
  color: #6b7280;
  font-size: 12px;
}

.tag-editor {
  width: 100%;
}

.tag-suggestions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-suggestions__label {
  color: #6b7280;
  font-size: 12px;
}

.schedule-row,
.collection-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.schedule-hint,
.collection-hint {
  color: #6b7280;
  font-size: 12px;
  flex: 1;
  min-width: 0;
}

.schedule-picker,
.collection-editor {
  margin-top: 0;
  margin-left: 12px; // 右侧框与左侧文字之间留出间距
  flex-shrink: 0; // 避免输入框被挤压
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  width: 100%;
  flex-wrap: wrap;
}

.form-actions :deep(.el-button) {
  min-width: 120px;
}
</style>

