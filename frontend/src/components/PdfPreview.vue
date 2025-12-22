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
    <!-- 工具栏 -->
    <div class="pdf-toolbar">
      <!-- 翻页控制 -->
      <div class="toolbar-group">
        <el-button
          :disabled="currentPage <= 1 || loading"
          @click="prevPage"
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="page-info">
          <el-input-number
            v-model="currentPage"
            :min="1"
            :max="totalPages"
            :disabled="loading || totalPages === 0"
            controls-position="right"
            size="small"
            @change="handlePageChange"
          />
          <span class="page-separator">/</span>
          <span class="total-pages">{{ totalPages }}</span>
        </div>
        <el-button
          :disabled="currentPage >= totalPages || loading"
          @click="nextPage"
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 缩放控制 -->
      <div class="toolbar-group">
        <el-button
          :disabled="loading || scale <= 0.5"
          @click="zoomOut"
        >
          <el-icon><ZoomOut /></el-icon>
        </el-button>
        <span class="zoom-info">{{ Math.round(scale * 100) }}%</span>
        <el-button
          :disabled="loading || scale >= 3"
          @click="zoomIn"
        >
          <el-icon><ZoomIn /></el-icon>
        </el-button>
        <el-button
          :disabled="loading"
          @click="fitToWidth"
        >
          适应宽度
        </el-button>
      </div>
    </div>

    <!-- PDF 内容区域 -->
    <div ref="pdfContainer" class="pdf-content">
      <!-- 加载状态遮罩 -->
      <div v-if="loading && !error" class="pdf-loading">
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

      <!-- PDF 渲染 - 始终存在以便触发加载事件 -->
      <div v-show="!error && pdfSource" class="pdf-wrapper">
        <VuePdfEmbed
          ref="pdfRef"
          :source="pdfSource"
          :page="currentPage"
          :scale="scale"
          @loaded="handleLoaded"
          @loading-failed="handleLoadingFailed"
          @rendered="handleRendered"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { ArrowLeft, ArrowRight, ZoomIn, ZoomOut, Loading, WarningFilled, RefreshRight } from '@element-plus/icons-vue'

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
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1)
const pdfRef = ref(null)
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
  currentPage.value = 1
  totalPages.value = 0
  scale.value = 1
  error.value = false
  errorMessage.value = ''
}

// PDF 加载完成
const handleLoaded = (pdf) => {
  loading.value = false
  totalPages.value = pdf.numPages
  error.value = false
}

// PDF 加载失败
const handleLoadingFailed = (err) => {
  loading.value = false
  error.value = true
  errorMessage.value = err?.message || '加载 PDF 失败，请检查文件是否有效'
  emit('error', err)
}

// PDF 渲染完成
const handleRendered = () => {
  // 渲染完成后可以进行额外操作
}

// 翻页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const handlePageChange = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 缩放
const zoomIn = () => {
  if (scale.value < 3) {
    scale.value = Math.min(3, scale.value + 0.25)
  }
}

const zoomOut = () => {
  if (scale.value > 0.5) {
    scale.value = Math.max(0.5, scale.value - 0.25)
  }
}

const fitToWidth = async () => {
  await nextTick()
  if (pdfContainer.value) {
    // 获取容器宽度，计算适应宽度的缩放比例
    const containerWidth = pdfContainer.value.clientWidth - 48 // 减去 padding
    // 假设 PDF 默认宽度约为 612pt (8.5 inch * 72 dpi)
    const defaultPdfWidth = 612
    const newScale = containerWidth / defaultPdfWidth
    scale.value = Math.max(0.5, Math.min(3, newScale))
  }
}

// 重试
const handleRetry = () => {
  error.value = false
  loading.value = true
  // 通过重新设置 source 触发重新加载
  const currentUrl = props.url
  if (pdfRef.value) {
    // 强制重新加载
    nextTick(() => {
      // vue-pdf-embed 会根据 source 变化自动重新加载
    })
  }
}

// 关闭
const handleClose = () => {
  dialogVisible.value = false
  // 释放资源
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

.pdf-toolbar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  padding: 12px 24px;
  background: $gray-50;
  border-bottom: 1px solid $border-color;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 8px;
  
  :deep(.el-input-number) {
    width: 80px;
  }
}

.page-separator {
  color: $text-secondary;
}

.total-pages {
  color: $text-primary;
  font-weight: $font-weight-medium;
  min-width: 30px;
}

.zoom-info {
  min-width: 50px;
  text-align: center;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.pdf-content {
  flex: 1;
  overflow: auto;
  background: $gray-200;
  display: flex;
  justify-content: center;
  padding: 24px;
  position: relative;
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

.pdf-wrapper {
  background: $bg-primary;
  box-shadow: $shadow-lg;
  border-radius: $radius-sm;
  
  :deep(canvas) {
    display: block;
    max-width: 100%;
  }
}
</style>
