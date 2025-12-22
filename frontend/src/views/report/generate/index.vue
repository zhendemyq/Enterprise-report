<template>
  <div class="generate-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">报表生成</h1>
        <p class="page-desc">选择模板并配置参数，快速生成所需报表</p>
      </div>
    </div>
    
    <!-- 主内容 -->
    <div class="generate-content">
      <!-- 左侧模板选择 -->
      <div class="template-section">
        <div class="section-card">
          <div class="card-header">
            <h3 class="card-title">选择模板</h3>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索模板..."
              prefix-icon="Search"
              clearable
              class="search-input"
            />
          </div>
          
          <!-- 分类标签 -->
          <div class="category-tabs">
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="category-tab"
              :class="{ active: selectedCategory === cat.id }"
              @click="selectedCategory = cat.id"
            >
              <el-icon><component :is="cat.icon || 'Folder'" /></el-icon>
              <span>{{ cat.categoryName }}</span>
            </div>
          </div>
          
          <!-- 模板列表 -->
          <div class="template-list">
            <div
              v-for="template in filteredTemplates"
              :key="template.id"
              class="template-item"
              :class="{ selected: selectedTemplate?.id === template.id }"
              @click="selectTemplate(template)"
            >
              <div class="template-icon" :class="getTypeClass(template.templateType)">
                <el-icon><component :is="getTypeIcon(template.templateType)" /></el-icon>
              </div>
              <div class="template-info">
                <span class="template-name">{{ template.templateName }}</span>
                <span class="template-desc">{{ template.description || '暂无描述' }}</span>
              </div>
              <el-icon v-if="selectedTemplate?.id === template.id" class="check-icon">
                <CircleCheckFilled />
              </el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧参数配置 -->
      <div class="config-section">
        <div v-if="!selectedTemplate" class="empty-config">
          <div class="empty-content">
            <el-icon :size="64"><DocumentAdd /></el-icon>
            <h3>请选择报表模板</h3>
            <p>从左侧列表选择一个模板开始生成报表</p>
          </div>
        </div>
        
        <template v-else>
          <!-- 模板信息 -->
          <div class="section-card template-detail">
            <div class="detail-header">
              <div class="detail-icon" :class="getTypeClass(selectedTemplate.templateType)">
                <el-icon :size="28"><component :is="getTypeIcon(selectedTemplate.templateType)" /></el-icon>
              </div>
              <div class="detail-info">
                <h2 class="detail-name">{{ selectedTemplate.templateName }}</h2>
                <p class="detail-desc">{{ selectedTemplate.description }}</p>
              </div>
            </div>
            <div class="detail-meta">
              <span class="meta-item">
                <el-icon><Folder /></el-icon>
                {{ selectedTemplate.categoryName || '未分类' }}
              </span>
              <span class="meta-item">
                <el-icon><Document /></el-icon>
                {{ getTypeText(selectedTemplate.templateType) }}
              </span>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                使用 {{ selectedTemplate.useCount || 0 }} 次
              </span>
            </div>
          </div>
          
          <!-- 参数配置 -->
          <div class="section-card params-config">
            <div class="card-header">
              <h3 class="card-title">参数配置</h3>
              <el-button text type="primary" @click="resetParams">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </div>
            
            <el-form 
              ref="paramsFormRef"
              :model="generateParams" 
              label-position="top"
              class="params-form"
            >
              <div class="params-grid">
                <el-form-item 
                  v-for="param in selectedTemplate.params"
                  :key="param.name"
                  :label="param.label"
                  :prop="param.name"
                  :rules="param.required ? [{ required: true, message: `请填写${param.label}` }] : []"
                >
                  <!-- 文本输入 -->
                  <el-input
                    v-if="param.type === 'input'"
                    v-model="generateParams[param.name]"
                    :placeholder="param.placeholder || `请输入${param.label}`"
                  />
                  
                  <!-- 数字输入 -->
                  <el-input-number
                    v-else-if="param.type === 'number'"
                    v-model="generateParams[param.name]"
                    :placeholder="param.placeholder"
                    style="width: 100%"
                  />
                  
                  <!-- 日期选择 -->
                  <el-date-picker
                    v-else-if="param.type === 'date'"
                    v-model="generateParams[param.name]"
                    type="date"
                    :placeholder="param.placeholder || '选择日期'"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                  
                  <!-- 日期范围 -->
                  <el-date-picker
                    v-else-if="param.type === 'daterange'"
                    v-model="generateParams[param.name]"
                    type="daterange"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                  
                  <!-- 下拉选择 -->
                  <el-select
                    v-else-if="param.type === 'select'"
                    v-model="generateParams[param.name]"
                    :placeholder="param.placeholder || `选择${param.label}`"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="opt in param.options"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-select>
                  
                  <!-- 多选 -->
                  <el-select
                    v-else-if="param.type === 'multiselect'"
                    v-model="generateParams[param.name]"
                    multiple
                    :placeholder="param.placeholder || `选择${param.label}`"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="opt in param.options"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </el-form>
          </div>
          
          <!-- 生成选项 -->
          <div class="section-card generate-options">
            <div class="card-header">
              <h3 class="card-title">生成选项</h3>
            </div>
            
            <div class="options-grid">
              <div class="option-item">
                <span class="option-label">报表名称</span>
                <el-input
                  v-model="reportName"
                  placeholder="请输入报表名称（可选）"
                />
              </div>
              
              <div class="option-item">
                <span class="option-label">导出格式</span>
                <div class="format-options">
                  <div
                    v-for="format in formatOptions"
                    :key="format.value"
                    class="format-item"
                    :class="{ selected: selectedFormat === format.value }"
                    @click="selectedFormat = format.value"
                  >
                    <el-icon :size="24"><component :is="format.icon" /></el-icon>
                    <span>{{ format.label }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button size="large" @click="handlePreview">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button type="primary" size="large" :loading="generating" @click="handleGenerate">
              <el-icon><Printer /></el-icon>
              {{ generating ? '生成中...' : '生成报表' }}
            </el-button>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="报表预览"
      width="90%"
      top="5vh"
      class="preview-dialog"
    >
      <div class="preview-content">
        <div v-if="previewLoading" class="preview-loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <p>正在加载预览...</p>
        </div>
        <!-- PDF预览 -->
        <div v-else-if="previewType === 'pdf'" class="preview-frame">
          <iframe v-if="previewUrl" :src="previewUrl" width="100%" height="100%" frameborder="0"></iframe>
        </div>
        <!-- Excel/CSV表格预览 -->
        <div v-else-if="previewData.length > 0" class="preview-table-wrapper">
          <el-table :data="previewData" border stripe max-height="65vh">
            <el-table-column
              v-for="(header, index) in previewHeaders"
              :key="index"
              :prop="String(index)"
              :label="String(header ?? '')"
              min-width="120"
            >
              <template #default="{ row }">{{ row[index] }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else class="preview-placeholder">
          <el-icon :size="64"><Document /></el-icon>
          <h3>暂无数据</h3>
        </div>
      </div>
      <template #footer>
        <el-button @click="closePreview">关闭</el-button>
        <el-button type="primary" @click="handleDownloadFromPreview">
          <el-icon><Download /></el-icon>
          下载
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 生成成功弹窗 -->
    <el-dialog
      v-model="successDialogVisible"
      title="生成成功"
      width="480px"
      center
      class="success-dialog"
    >
      <div class="success-content">
        <div class="success-icon">
          <el-icon :size="64"><CircleCheckFilled /></el-icon>
        </div>
        <h3 class="success-title">报表生成成功！</h3>
        <p class="success-info">{{ generatedReport?.reportName }}</p>
        <div class="success-meta">
          <span>文件大小：{{ formatFileSize(generatedReport?.fileSize) }}</span>
          <span>耗时：{{ generatedReport?.duration }}ms</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="successDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload">
          <el-icon><Download /></el-icon>
          立即下载
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listUserTemplates } from '@/api/template'
import { getCategoryTree } from '@/api/category'
import { generateReport, downloadReport, previewReport } from '@/api/report'
import dayjs from 'dayjs'
import * as XLSX from 'xlsx'

// 搜索关键词
const searchKeyword = ref('')
const selectedCategory = ref('all')

// 分类列表 - 动态加载
const categories = ref([
  { id: 'all', categoryName: '全部', icon: 'Menu' }
])

// 模板列表
const templateList = ref([])
const selectedTemplate = ref(null)

// 参数
const paramsFormRef = ref(null)
const generateParams = reactive({})

// 生成选项
const reportName = ref('')
const selectedFormat = ref('xlsx')
const formatOptions = [
  { value: 'xlsx', label: 'Excel', icon: 'Document' },
  { value: 'pdf', label: 'PDF', icon: 'Document' },
  { value: 'csv', label: 'CSV', icon: 'Document' }
]

// 状态
const generating = ref(false)
const previewDialogVisible = ref(false)
const previewLoading = ref(false)
const previewUrl = ref('')
const previewData = ref([])  // Excel/CSV 表格数据
const previewHeaders = ref([])  // 表头
const previewType = ref('pdf')  // 预览类型
const successDialogVisible = ref(false)
const generatedReport = ref(null)

// 过滤后的模板
const filteredTemplates = computed(() => {
  let result = templateList.value

  if (selectedCategory.value !== 'all') {
    result = result.filter(t => t.categoryId == selectedCategory.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      t.templateName.toLowerCase().includes(keyword) ||
      t.description?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 初始化
onMounted(() => {
  loadCategories()
  loadTemplates()
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    const list = res.data || []
    // 扁平化树形结构
    const flatList = []
    const flatten = (items) => {
      items.forEach(item => {
        flatList.push(item)
        if (item.children?.length) {
          flatten(item.children)
        }
      })
    }
    flatten(list)
    categories.value = [
      { id: 'all', categoryName: '全部', icon: 'Menu' },
      ...flatList
    ]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载模板列表
const loadTemplates = async () => {
  try {
    const res = await listUserTemplates()
    templateList.value = res.data || []
  } catch (error) {
    console.error('加载模板列表失败:', error)
    templateList.value = []
  }
}

// 选择模板
const selectTemplate = (template) => {
  selectedTemplate.value = template
  resetParams()
  
  // 设置默认报表名称
  reportName.value = `${template.templateName}_${dayjs().format('YYYYMMDD')}`
}

// 重置参数
const resetParams = () => {
  if (!selectedTemplate.value?.params) return
  
  // 清空所有参数
  Object.keys(generateParams).forEach(key => {
    delete generateParams[key]
  })
  
  // 设置默认值
  selectedTemplate.value.params.forEach(param => {
    if (param.defaultValue !== undefined) {
      generateParams[param.name] = param.defaultValue
    }
  })
}

// 预览
const handlePreview = async () => {
  // 权限检查
  if (!selectedTemplate.value?.canView) {
    ElMessage.warning('您没有该模板的预览权限')
    return
  }
  if (!selectedTemplate.value?.canGenerate) {
    ElMessage.warning('您没有该模板的生成权限，无法预览')
    return
  }
  if (!selectedTemplate.value?.canDownload) {
    ElMessage.warning('您没有该模板的下载权限，无法预览')
    return
  }

  if (!paramsFormRef.value) return

  await paramsFormRef.value.validate(async (valid) => {
    if (valid) {
      previewDialogVisible.value = true
      previewLoading.value = true
      previewUrl.value = ''
      previewData.value = []
      previewHeaders.value = []
      previewType.value = selectedFormat.value

      try {
        const data = {
          templateId: selectedTemplate.value.id,
          reportName: reportName.value || selectedTemplate.value.templateName,
          fileType: selectedFormat.value,
          params: generateParams
        }
        const res = await generateReport(data)
        if (res.data?.id) {
          generatedReport.value = res.data
          const previewRes = await previewReport(res.data.id)

          if (selectedFormat.value === 'pdf') {
            const blob = new Blob([previewRes.data], { type: 'application/pdf' })
            previewUrl.value = URL.createObjectURL(blob)
          } else if (selectedFormat.value === 'xlsx') {
            try {
              // 确保数据是ArrayBuffer格式
              let arrayBuffer
              if (previewRes.data instanceof ArrayBuffer) {
                arrayBuffer = previewRes.data
              } else if (previewRes.data instanceof Blob) {
                arrayBuffer = await previewRes.data.arrayBuffer()
              } else if (typeof previewRes.data.arrayBuffer === 'function') {
                arrayBuffer = await previewRes.data.arrayBuffer()
              } else {
                // 如果是其他格式，尝试转换
                const blob = new Blob([previewRes.data])
                arrayBuffer = await blob.arrayBuffer()
              }

              const workbook = XLSX.read(new Uint8Array(arrayBuffer), { type: 'array' })
              const sheetName = workbook.SheetNames[0]
              const worksheet = workbook.Sheets[sheetName]
              const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
              if (jsonData.length > 0) {
                previewHeaders.value = jsonData[0].map(h => String(h ?? ''))
                previewData.value = jsonData.slice(1)
              }
            } catch (xlsxError) {
              console.error('Excel解析错误:', xlsxError)
              ElMessage.warning('Excel文件解析失败，请尝试下载后查看')
            }
          } else if (selectedFormat.value === 'csv') {
            let text
            if (typeof previewRes.data === 'string') {
              text = previewRes.data
            } else if (previewRes.data instanceof Blob) {
              text = await previewRes.data.text()
            } else if (typeof previewRes.data.text === 'function') {
              text = await previewRes.data.text()
            } else {
              const blob = new Blob([previewRes.data])
              text = await blob.text()
            }
            const lines = text.split('\n').filter(line => line.trim())
            if (lines.length > 0) {
              previewHeaders.value = lines[0].split(',').map(h => String(h ?? '').trim())
              previewData.value = lines.slice(1).map(line => line.split(',').map(c => c.trim()))
            }
          }
        }
      } catch (error) {
        console.error('预览失败:', error)
        ElMessage.error('预览失败，请重试')
      } finally {
        previewLoading.value = false
      }
    }
  })
}

// 生成报表
const handleGenerate = async () => {
  // 权限检查
  if (!selectedTemplate.value?.canGenerate) {
    ElMessage.warning('您没有该模板的生成权限')
    return
  }
  if (!selectedTemplate.value?.canDownload) {
    ElMessage.warning('您没有该模板的下载权限，无法生成报表')
    return
  }

  if (!paramsFormRef.value) return

  await paramsFormRef.value.validate(async (valid) => {
    if (valid) {
      generating.value = true
      
      try {
        const data = {
          templateId: selectedTemplate.value.id,
          reportName: reportName.value || selectedTemplate.value.templateName,
          fileType: selectedFormat.value,
          params: generateParams
        }

        const res = await generateReport(data)

        if (res.data) {
          generatedReport.value = {
            id: res.data.id,
            reportName: res.data.reportName || data.reportName,
            fileSize: res.data.fileSize || 0,
            duration: res.data.duration || 0
          }
          successDialogVisible.value = true

          // 刷新模板列表以更新使用次数
          await loadTemplates()
        }
      } catch (error) {
        console.error('生成报表失败:', error)
        ElMessage.error('报表生成失败，请重试')
      } finally {
        generating.value = false
      }
    }
  })
}

// 下载
const handleDownload = async () => {
  // 权限检查
  if (!selectedTemplate.value?.canDownload) {
    ElMessage.warning('您没有该模板的下载权限')
    return
  }

  if (!generatedReport.value) return

  try {
    const res = await downloadReport(generatedReport.value.id)

    // 创建下载链接
    const blob = new Blob([res.data], { type: res.headers['content-type'] })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${generatedReport.value.reportName}.${selectedFormat.value}`
    a.click()
    window.URL.revokeObjectURL(url)

    successDialogVisible.value = false
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.info('下载功能需要后端支持')
  }
}

// 从预览下载
const handleDownloadFromPreview = () => {
  // 权限检查
  if (!selectedTemplate.value?.canDownload) {
    ElMessage.warning('您没有该模板的下载权限')
    return
  }

  previewDialogVisible.value = false
  handleDownload()
}

// 关闭预览
const closePreview = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  previewDialogVisible.value = false
}

// 工具函数
const getTypeClass = (type) => {
  const map = { 1: 'type-detail', 2: 'type-summary', 3: 'type-group', 4: 'type-chart' }
  return map[type] || 'type-detail'
}

const getTypeIcon = (type) => {
  const map = { 1: 'Document', 2: 'DataAnalysis', 3: 'Grid', 4: 'TrendCharts' }
  return map[type] || 'Document'
}

const getTypeText = (type) => {
  const map = { 1: '明细表', 2: '汇总表', 3: '分组统计表', 4: '图表报表' }
  return map[type] || '未知'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style lang="scss" scoped>
.generate-container {
  max-width: 1400px;
  margin: 0 auto;
}

// 页面头部
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: $text-secondary;
}

// 主内容
.generate-content {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
}

// 通用卡片样式
.section-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 24px;
  box-shadow: $shadow-sm;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// 模板选择区
.template-section {
  .section-card {
    height: calc(100vh - 200px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}

.search-input {
  width: 180px;
}

.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid $border-color;
  overflow-x: auto;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: $radius-full;
  font-size: 13px;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  white-space: nowrap;
  
  &:hover {
    background: $gray-50;
    color: $text-primary;
  }
  
  &.active {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }
  
  .el-icon {
    font-size: 16px;
  }
}

.template-list {
  flex: 1;
  overflow-y: auto;
  margin: 0 -8px;
  padding: 0 8px;
}

.template-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-fast;
  margin-bottom: 8px;
  border: 2px solid transparent;
  
  &:hover {
    background: $gray-50;
  }
  
  &.selected {
    background: rgba($primary-color, 0.04);
    border-color: $primary-color;
  }
}

.template-icon {
  width: 44px;
  height: 44px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  
  &.type-detail {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }
  
  &.type-summary {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &.type-group {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }
  
  &.type-chart {
    background: rgba($info-color, 0.1);
    color: $info-color;
  }
}

.template-info {
  flex: 1;
  min-width: 0;
}

.template-name {
  display: block;
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  margin-bottom: 4px;
}

.template-desc {
  display: block;
  font-size: 12px;
  color: $text-secondary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.check-icon {
  color: $primary-color;
  font-size: 20px;
}

// 配置区
.config-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.empty-config {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-primary;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
}

.empty-content {
  text-align: center;
  
  .el-icon {
    color: $text-tertiary;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 20px;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: $text-secondary;
  }
}

// 模板详情
.template-detail {
  .detail-header {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
  }
  
  .detail-icon {
    width: 64px;
    height: 64px;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    &.type-detail {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
    }
    
    &.type-summary {
      background: rgba($success-color, 0.1);
      color: $success-color;
    }
    
    &.type-group {
      background: rgba($warning-color, 0.1);
      color: $warning-color;
    }
    
    &.type-chart {
      background: rgba($info-color, 0.1);
      color: $info-color;
    }
  }
  
  .detail-info {
    flex: 1;
  }
  
  .detail-name {
    font-size: 22px;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  .detail-desc {
    font-size: 14px;
    color: $text-secondary;
    line-height: 1.5;
  }
  
  .detail-meta {
    display: flex;
    gap: 24px;
    padding-top: 16px;
    border-top: 1px solid $border-color;
  }
  
  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: $text-secondary;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

// 参数配置
.params-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0 20px;
}

// 生成选项
.options-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.option-item {
  .option-label {
    display: block;
    font-size: 14px;
    font-weight: $font-weight-medium;
    color: $text-primary;
    margin-bottom: 10px;
  }
}

.format-options {
  display: flex;
  gap: 12px;
}

.format-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid $border-color;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    border-color: $gray-300;
  }
  
  &.selected {
    border-color: $primary-color;
    background: rgba($primary-color, 0.04);
    
    .el-icon {
      color: $primary-color;
    }
  }
  
  .el-icon {
    color: $text-secondary;
  }
  
  span {
    font-size: 13px;
    color: $text-primary;
    font-weight: $font-weight-medium;
  }
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 16px;
  
  .el-button {
    flex: 1;
    height: 52px;
    font-size: 16px;
  }
}

// 预览弹窗
.preview-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: 70vh;
  }
}

.preview-content {
  height: 100%;
}

.preview-loading {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .loading-icon {
    font-size: 48px;
    color: $primary-color;
    animation: rotate 1s linear infinite;
    margin-bottom: 16px;
  }
  
  p {
    color: $text-secondary;
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.preview-frame {
  height: 100%;
}

.preview-table-wrapper {
  height: 100%;
  padding: 16px;
  overflow: auto;
}

.preview-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .el-icon {
    color: $text-tertiary;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 20px;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  p {
    color: $text-secondary;
  }
}

// 成功弹窗
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
  
  .el-icon {
    color: $success-color;
  }
}

.success-title {
  font-size: 22px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 8px;
}

.success-info {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: 16px;
}

.success-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 13px;
  color: $text-tertiary;
}

// 响应式
@media (max-width: 1024px) {
  .generate-content {
    grid-template-columns: 1fr;
  }
  
  .template-section .section-card {
    height: auto;
    max-height: 400px;
  }
}
</style>
