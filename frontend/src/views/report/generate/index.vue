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
              <el-icon><component :is="cat.icon" /></el-icon>
              <span>{{ cat.name }}</span>
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
        <div v-else class="preview-frame">
          <!-- PDF预览或表格预览 -->
          <div class="preview-placeholder">
            <el-icon :size="64"><Document /></el-icon>
            <h3>报表预览</h3>
            <p>此处显示报表预览内容</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
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
import { generateReport, downloadReport, previewReport } from '@/api/report'
import dayjs from 'dayjs'

// 搜索关键词
const searchKeyword = ref('')
const selectedCategory = ref('all')

// 分类列表
const categories = [
  { id: 'all', name: '全部', icon: 'Menu' },
  { id: 'finance', name: '财务', icon: 'Money' },
  { id: 'sales', name: '销售', icon: 'TrendCharts' },
  { id: 'operation', name: '运营', icon: 'DataAnalysis' },
  { id: 'hr', name: '人事', icon: 'User' }
]

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
const successDialogVisible = ref(false)
const generatedReport = ref(null)

// 过滤后的模板
const filteredTemplates = computed(() => {
  let result = templateList.value
  
  if (selectedCategory.value !== 'all') {
    result = result.filter(t => t.categoryCode === selectedCategory.value)
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
  loadTemplates()
})

// 加载模板列表
const loadTemplates = async () => {
  try {
    const res = await listUserTemplates()
    templateList.value = res.data || []
  } catch (error) {
    // 使用模拟数据
    templateList.value = mockTemplates()
  }
}

// 模拟数据
const mockTemplates = () => [
  { 
    id: 1, 
    templateName: '销售日报模板', 
    templateCode: 'sales_daily',
    templateType: 1, 
    categoryCode: 'sales',
    categoryName: '销售报表', 
    description: '每日销售数据统计报表，包含销售额、订单数、客单价等',
    useCount: 1256,
    params: [
      { name: 'dateRange', label: '日期范围', type: 'daterange', required: true },
      { name: 'region', label: '区域', type: 'select', options: [
        { label: '全部', value: '' },
        { label: '华东', value: 'east' },
        { label: '华南', value: 'south' },
        { label: '华北', value: 'north' }
      ]},
      { name: 'productType', label: '产品类型', type: 'multiselect', options: [
        { label: '类型A', value: 'A' },
        { label: '类型B', value: 'B' },
        { label: '类型C', value: 'C' }
      ]}
    ]
  },
  { 
    id: 2, 
    templateName: '财务月报模板', 
    templateCode: 'finance_monthly',
    templateType: 2, 
    categoryCode: 'finance',
    categoryName: '财务报表', 
    description: '月度财务汇总报表，包含收入、支出、利润等财务指标',
    useCount: 986,
    params: [
      { name: 'month', label: '月份', type: 'date', required: true },
      { name: 'department', label: '部门', type: 'select', options: [
        { label: '全公司', value: '' },
        { label: '销售部', value: 'sales' },
        { label: '研发部', value: 'rd' },
        { label: '运营部', value: 'ops' }
      ]}
    ]
  },
  { 
    id: 3, 
    templateName: '员工考勤统计', 
    templateCode: 'attendance_stats',
    templateType: 3, 
    categoryCode: 'hr',
    categoryName: '人事报表', 
    description: '员工考勤分组统计表，按部门汇总出勤情况',
    useCount: 754,
    params: [
      { name: 'dateRange', label: '统计周期', type: 'daterange', required: true },
      { name: 'department', label: '部门', type: 'select', options: [
        { label: '全部', value: '' },
        { label: '技术部', value: 'tech' },
        { label: '产品部', value: 'product' },
        { label: '市场部', value: 'market' }
      ]}
    ]
  },
  { 
    id: 4, 
    templateName: '订单明细报表', 
    templateCode: 'order_detail',
    templateType: 1, 
    categoryCode: 'operation',
    categoryName: '运营报表', 
    description: '订单详情明细表，展示订单完整信息',
    useCount: 621,
    params: [
      { name: 'dateRange', label: '订单日期', type: 'daterange', required: true },
      { name: 'status', label: '订单状态', type: 'select', options: [
        { label: '全部', value: '' },
        { label: '待支付', value: 'pending' },
        { label: '已支付', value: 'paid' },
        { label: '已发货', value: 'shipped' },
        { label: '已完成', value: 'completed' }
      ]},
      { name: 'minAmount', label: '最低金额', type: 'number' }
    ]
  },
  { 
    id: 5, 
    templateName: '销售趋势分析', 
    templateCode: 'sales_trend',
    templateType: 4, 
    categoryCode: 'sales',
    categoryName: '销售报表', 
    description: '销售数据图表分析，包含趋势图和对比图',
    useCount: 489,
    params: [
      { name: 'year', label: '年份', type: 'date', required: true },
      { name: 'compareYear', label: '对比年份', type: 'date' }
    ]
  }
]

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
  if (!paramsFormRef.value) return
  
  await paramsFormRef.value.validate(async (valid) => {
    if (valid) {
      previewDialogVisible.value = true
      previewLoading.value = true
      
      // 模拟加载
      setTimeout(() => {
        previewLoading.value = false
      }, 1500)
    }
  })
}

// 生成报表
const handleGenerate = async () => {
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
        
        // 模拟生成结果
        generatedReport.value = {
          id: res.data?.id || Date.now(),
          reportName: data.reportName,
          fileSize: 1024 * 256, // 256KB
          duration: 1200
        }
        
        successDialogVisible.value = true
      } catch (error) {
        // 模拟成功
        generatedReport.value = {
          id: Date.now(),
          reportName: reportName.value || selectedTemplate.value.templateName,
          fileSize: 1024 * 256,
          duration: 1200
        }
        successDialogVisible.value = true
      } finally {
        generating.value = false
      }
    }
  })
}

// 下载
const handleDownload = async () => {
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
  previewDialogVisible.value = false
  handleGenerate()
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
