<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { createVideoComplaint } from '@/api/videoComplaint'
import { uploadImage } from '@/api/feed'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  videoId: { type: String, required: true }
})

const emit = defineEmits(['update:modelValue', 'submitted'])

const userStore = useUserStore()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const reportCategory = ref('')
const reportDetail = ref('')
const evidenceUrls = ref([]) // string[]

const submitting = ref(false)
const uploadedUids = new Set()

const onClose = () => {
  visible.value = false
}

const resetForm = () => {
  reportCategory.value = ''
  reportDetail.value = ''
  evidenceUrls.value = []
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) resetForm()
  }
)

const legalItems = [
  { label: '侵犯个人知识产权（盗播、抄袭）', value: 'ip_violation' },
  { label: '侵犯个人 人身权益（名誉、肖像、隐私）', value: 'personal_right_violation' },
  { label: '侵犯企业权益', value: 'enterprise_right_violation' }
]

const ruleItems = [
  { label: '色情低俗', value: 'low_quality' },
  { label: '违规广告引流', value: 'illegal_ads' },
  { label: '涉政敏感', value: 'politics_sensitive' },
  { label: '引战、网暴、不友善', value: 'attack_harassment' },
  { label: '传播放淫言', value: 'obscene_language' },
  { label: '涉嫌诈骗', value: 'fraud' },
  { label: '引人不适', value: 'uncomfortable' },
  { label: '涉未成年人不良信息', value: 'minors_bad_info' },
  { label: '封面党、标题党', value: 'clickbait' }
]

// 直接复用 Element Plus 的上传容器，但我们自己上传到 /api/images/upload
const selectedEvidenceCount = computed(() => evidenceUrls.value.length)
const evidenceLimit = 5

const beforeEvidenceUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.warning('只能上传图片')
    return false
  }
  if (selectedEvidenceCount.value >= evidenceLimit) {
    ElMessage.warning(`最多上传 ${evidenceLimit} 张图片`)
    return false
  }
  return true
}

const onEvidenceChange = async (file, fileList) => {
  // fileList 包含未上传和已上传项；这里我们只关心新增的 raw file
  // Element Plus 触发时机不完全可靠，所以以 file.status 为准
  if (!file || file.status === 'fail') return
  if (!file.raw) return
  if (uploadedUids.has(file.uid)) return

  try {
    if (selectedEvidenceCount.value >= evidenceLimit) return
    uploadedUids.add(file.uid)

    const resp = await uploadImage(file.raw)
    const url = resp?.data?.url
    if (!url) throw new Error('上传失败')

    evidenceUrls.value = [...evidenceUrls.value, url]
  } catch (e) {
    // 上传失败：允许用户重试同一文件
    uploadedUids.delete(file.uid)
    ElMessage.error('图片上传失败，请稍后重试')
  }
}

const submit = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  if (!reportCategory.value) {
    ElMessage.warning('请选择举报类型')
    return
  }
  if (!reportDetail.value.trim()) {
    ElMessage.warning('请填写详细描述')
    return
  }
  if (reportDetail.value.length > 400) {
    ElMessage.warning('详细描述最多 400 字')
    return
  }

  try {
    submitting.value = true
    const resp = await createVideoComplaint(
      props.videoId,
      reportCategory.value,
      reportDetail.value.trim(),
      evidenceUrls.value
    )
    if (resp?.data?.success) {
      ElMessage.success('举报提交成功')
      emit('submitted')
      visible.value = false
    } else {
      ElMessage.error(resp?.data?.message || '举报提交失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '举报提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    width="760px"
    :show-close="false"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <template #header>
      <div class="modal-header">
        <div class="modal-title">举报</div>
        <el-button type="info" circle @click="onClose" class="modal-close-el-btn">×</el-button>
      </div>
    </template>

    <div class="complaint-body">
      <div class="complaint-block">
        <div class="block-title">违反社区规则</div>
        <el-radio-group v-model="reportCategory" class="radio-grid">
          <el-radio
            v-for="it in ruleItems"
            :key="it.value"
            :label="it.value"
          >
            {{ it.label }}
          </el-radio>
        </el-radio-group>
      </div>

      <div class="complaint-block">
        <div class="block-title">详细描述</div>
        <el-input
          v-model="reportDetail"
          type="textarea"
          :rows="4"
          maxlength="400"
          show-word-limit
          placeholder="请填写违规内容的具体片段（X分X秒）和详细描述"
        />
      </div>

      <div class="complaint-block">
        <div class="block-title">
          上传图片材料（选填）
          <span class="count">({{ selectedEvidenceCount }}/{{ evidenceLimit }})</span>
        </div>

        <el-upload
          class="evidence-upload"
          action=""
          :auto-upload="false"
          list-type="picture-card"
          :limit="evidenceLimit"
          :before-upload="beforeEvidenceUpload"
          :on-change="onEvidenceChange"
          :show-file-list="false"
          accept="image/*"
        >
          <el-icon class="upload-plus">
            <Plus />
          </el-icon>
          <template #tip>
            <div class="upload-tip">最多上传 {{ evidenceLimit }} 张，支持 JPG/PNG 等图片。</div>
          </template>
        </el-upload>

        <div class="evidence-preview" v-if="evidenceUrls.length > 0">
          <div class="thumb" v-for="(u, idx) in evidenceUrls" :key="u + idx">
            <el-image :src="u" fit="cover" class="thumb-img" />
            <el-button
              type="danger"
              circle
              size="small"
              class="thumb-remove-el-btn"
              @click="evidenceUrls.splice(idx, 1)"
            >
              ×
            </el-button>
          </div>
        </div>
      </div>

      <div class="complaint-hint">
        通过对违规内容的具体指引与证据提交，更快获得处理反馈。
      </div>
    </div>

    <template #footer>
      <div class="modal-footer">
        <el-button @click="onClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </div>
    </template>
  </el-dialog>
  <!-- 注：图片上传预览仅展示已上传的 url -->
  <!-- 真实文件上传通过 /api/images/upload 完成 -->
</template>

<style scoped>
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  font-weight: 700;
  font-size: 18px;
}

.modal-close-btn {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.modal-close-el-btn {
  width: 30px;
  height: 30px;
  border-radius: 999px;
}

.complaint-body {
  padding: 4px 2px 0;
}

.complaint-block {
  margin-bottom: 18px;
}

.block-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.radio-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 20px;
}

.count {
  font-weight: 500;
  color: #909399;
  margin-left: 6px;
  font-size: 12px;
}

.evidence-upload {
  width: 160px;
}

.upload-btn {
  width: 160px;
}

.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.evidence-preview {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.thumb {
  position: relative;
  width: 96px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.thumb-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  border: none;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  cursor: pointer;
}

.thumb-remove-el-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 999px;
}

.complaint-hint {
  padding: 12px 14px;
  background: #f6f7fb;
  border-radius: 8px;
  color: #606266;
  font-size: 13px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
}
</style>

