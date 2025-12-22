<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="90%"
    top="3vh"
    class="pdf-preview-dialog"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- PDF 内容区域 -->
    <div ref="pdfContainer" class="pdf-content">
      <!-- 加载状态遮罩 -->
      <div v-if="loading" class="pdf-loading">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <p>正在加载 PDF...</p>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="pdf-error">
        <el-icon class="error-icon"><WarningFilled /></el-icon>
        <p class="error-message">{{ errorMessage }}</p>
        <el-button type="primary" @click="handleRetry">
          <el-icon><RefreshRight /></el-icon>
          重试
        </el-button>
      </div>

      <!-- PDF 渲染 - 使用 iframe 原生预览 -->
      <iframe
        v-show="!loading && !error && pdfSource"
        ref="pdfFrame"
        :src="pdfSource"
        class="pdf-iframe"
        @load="handleIframeLoad"
        @error="handleIframeError"
      />
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Loading, WarningFilled, RefreshRight } from '@element-plus/icons-vue'

const props = defineProps({
  url: {
    type: String,
    default: ''
  },
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'PDF 预览'
  }
})

const emit = defineEmits(['update:visible', 'error'])

// 状态
const loading = ref(false)
const error = ref(false)
const errorMessage = ref('')
const pdfFrame = ref(null)
const pdfContainer = ref(null)

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const pdfSource = computed(() => props.url)

// 监听 URL 变化，重新加载
watch(() => props.url, (newUrl) => {
  if (newUrl) {
    resetState()
    loading.value = true
  }
})

// 监听 visible 变化
watch(() => props.visible, (newVisible) => {
  if (newVisible && props.url) {
    resetState()
    loading.value = true
  }
})

// 重置状态
const resetState = () => {
  error.value = false
  errorMessage.value = ''
}

// iframe 加载完成
const handleIframeLoad = () => {
  loading.value = false
  error.value = false
}

// iframe 加载失败
const handleIframeError = (err) => {
  loading.value = false
  error.value = true
  errorMessage.value = '加载 PDF 失败，请检查文件是否有效'
  emit('error', err)
}

// 重试
const handleRetry = () => {
  error.value = false
  loading.value = true
  // 强制重新加载 iframe
  if (pdfFrame.value) {
    const currentSrc = pdfFrame.value.src
    pdfFrame.value.src = ''
    setTimeout(() => {
      pdfFrame.value.src = currentSrc
    }, 100)
  }
}

// 关闭
const handleClose = () => {
  dialogVisible.value = false
  resetState()
}
</script>


<style lang="scss" scoped>
.pdf-preview-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    display: flex;
    flex-direction: column;
    height: 80vh;
  }
}

.pdf-content {
  flex: 1;
  overflow: hidden;
  background: $gray-200;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.pdf-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.pdf-loading,
.pdf-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: $gray-200;
  z-index: 10;
}

.pdf-loading {
  .loading-icon {
    font-size: 48px;
    color: $primary-color;
    animation: rotate 1s linear infinite;
    margin-bottom: 16px;
  }

  p {
    color: $text-secondary;
    font-size: $font-size-md;
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.pdf-error {
  .error-icon {
    font-size: 64px;
    color: $warning-color;
    margin-bottom: 16px;
  }

  .error-message {
    color: $text-secondary;
    font-size: $font-size-md;
    margin-bottom: 24px;
    text-align: center;
    max-width: 400px;
  }
}
</style>
