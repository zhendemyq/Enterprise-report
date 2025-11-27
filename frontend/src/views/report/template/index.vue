<template>
  <div class="template-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">模板管理</h1>
        <p class="page-desc">管理报表模板，支持创建、编辑、发布和下线操作</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建模板
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-card">
      <div class="filter-form">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索模板名称、编码"
          prefix-icon="Search"
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        
        <el-select
          v-model="queryParams.categoryId"
          placeholder="选择分类"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.categoryName"
            :value="item.id"
          />
        </el-select>
        
        <el-select
          v-model="queryParams.templateType"
          placeholder="模板类型"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="明细表" :value="1" />
          <el-option label="汇总表" :value="2" />
          <el-option label="分组统计表" :value="3" />
          <el-option label="图表报表" :value="4" />
        </el-select>
        
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下线" :value="2" />
        </el-select>
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
      
      <!-- 视图切换 -->
      <div class="view-switch">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="card">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button label="table">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="template-grid">
      <div
        v-for="template in templateList"
        :key="template.id"
        class="template-card"
        @click="handleEdit(template)"
      >
        <div class="card-header">
          <div class="card-icon" :class="getTypeClass(template.templateType)">
            <el-icon><component :is="getTypeIcon(template.templateType)" /></el-icon>
          </div>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, template)">
            <el-button class="more-btn" text circle @click.stop>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon><Edit /></el-icon>编辑
                </el-dropdown-item>
                <el-dropdown-item command="design">
                  <el-icon><EditPen /></el-icon>设计
                </el-dropdown-item>
                <el-dropdown-item command="copy">
                  <el-icon><CopyDocument /></el-icon>复制
                </el-dropdown-item>
                <el-dropdown-item 
                  v-if="template.status !== 1" 
                  command="publish"
                >
                  <el-icon><Upload /></el-icon>发布
                </el-dropdown-item>
                <el-dropdown-item 
                  v-if="template.status === 1" 
                  command="offline"
                >
                  <el-icon><Download /></el-icon>下线
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon><Delete /></el-icon>删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div class="card-body">
          <h3 class="card-title">{{ template.templateName }}</h3>
          <p class="card-code">{{ template.templateCode }}</p>
          <p class="card-desc">{{ template.description || '暂无描述' }}</p>
        </div>
        
        <div class="card-footer">
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Folder /></el-icon>
              {{ template.categoryName || '未分类' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(template.updateTime) }}
            </span>
          </div>
          <el-tag 
            :type="getStatusType(template.status)"
            size="small"
            class="status-tag"
          >
            {{ getStatusText(template.status) }}
          </el-tag>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="templateList.length === 0" class="empty-state">
        <el-empty description="暂无模板数据">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建第一个模板
          </el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- 表格视图 -->
    <div v-else class="table-card">
      <el-table
        :data="templateList"
        style="width: 100%"
        row-key="id"
        @row-click="handleEdit"
      >
        <el-table-column label="模板信息" min-width="280">
          <template #default="{ row }">
            <div class="table-template-info">
              <div class="template-icon" :class="getTypeClass(row.templateType)">
                <el-icon><component :is="getTypeIcon(row.templateType)" /></el-icon>
              </div>
              <div class="template-text">
                <span class="template-name">{{ row.templateName }}</span>
                <span class="template-code">{{ row.templateCode }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="模板类型" width="120">
          <template #default="{ row }">
            {{ getTypeText(row.templateType) }}
          </template>
        </el-table-column>
        
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <span>{{ row.categoryName || '未分类' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="版本" width="80" align="center">
          <template #default="{ row }">
            <span class="version-badge">v{{ row.version }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button text type="primary" @click.stop="handleDesign(row)">
                设计
              </el-button>
              <el-button 
                v-if="row.status !== 1"
                text 
                type="success" 
                @click.stop="handlePublish(row)"
              >
                发布
              </el-button>
              <el-button 
                v-else
                text 
                type="warning" 
                @click.stop="handleOffline(row)"
              >
                下线
              </el-button>
              <el-button text type="danger" @click.stop="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
      class="template-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="模板名称" prop="templateName">
          <el-input 
            v-model="formData.templateName" 
            placeholder="请输入模板名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="模板编码" prop="templateCode">
          <el-input 
            v-model="formData.templateCode" 
            placeholder="请输入模板编码（唯一标识）"
            :disabled="!!formData.id"
            maxlength="50"
          />
        </el-form-item>
        
        <div class="form-row">
          <el-form-item label="模板类型" prop="templateType" class="flex-1">
            <el-select v-model="formData.templateType" placeholder="请选择模板类型">
              <el-option label="明细表" :value="1" />
              <el-option label="汇总表" :value="2" />
              <el-option label="分组统计表" :value="3" />
              <el-option label="图表报表" :value="4" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="所属分类" prop="categoryId" class="flex-1">
            <el-select v-model="formData.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.categoryName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>
        
        <el-form-item label="数据源" prop="datasourceId">
          <el-select v-model="formData.datasourceId" placeholder="请选择数据源" clearable>
            <el-option
              v-for="item in datasourceList"
              :key="item.id"
              :label="item.datasourceName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            placeholder="请输入模板描述"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 复制弹窗 -->
    <el-dialog
      v-model="copyDialogVisible"
      title="复制模板"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="copyFormRef"
        :model="copyFormData"
        :rules="copyFormRules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="新模板名称" prop="newName">
          <el-input 
            v-model="copyFormData.newName" 
            placeholder="请输入新模板名称"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="copyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="copyLoading" @click="handleCopySubmit">
            复制
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  pageTemplates, 
  createTemplate, 
  updateTemplate, 
  deleteTemplate,
  publishTemplate,
  offlineTemplate,
  copyTemplate
} from '@/api/template'
import { listDatasources } from '@/api/datasource'
import dayjs from 'dayjs'

const router = useRouter()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  categoryId: null,
  templateType: null,
  status: null
})

// 数据
const templateList = ref([])
const categoryList = ref([
  { id: 1, categoryName: '财务报表' },
  { id: 2, categoryName: '销售报表' },
  { id: 3, categoryName: '运营报表' },
  { id: 4, categoryName: '人事报表' }
])
const datasourceList = ref([])
const total = ref(0)
const viewMode = ref('card')

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑模板' : '新建模板')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  templateName: '',
  templateCode: '',
  templateType: 1,
  categoryId: null,
  datasourceId: null,
  description: ''
})

const formRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  templateCode: [
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '编码只能包含字母、数字和下划线，且以字母或下划线开头', trigger: 'blur' }
  ],
  templateType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ]
}

// 复制弹窗
const copyDialogVisible = ref(false)
const copyLoading = ref(false)
const copyFormRef = ref(null)
const copyFormData = reactive({
  id: null,
  newName: ''
})
const copyFormRules = {
  newName: [
    { required: true, message: '请输入新模板名称', trigger: 'blur' }
  ]
}

// 初始化
onMounted(() => {
  loadTemplates()
  loadDatasources()
})

// 加载模板列表
const loadTemplates = async () => {
  try {
    const res = await pageTemplates(queryParams)
    templateList.value = res.data?.records || mockTemplates()
    total.value = res.data?.total || templateList.value.length
  } catch (error) {
    // 使用模拟数据
    templateList.value = mockTemplates()
    total.value = templateList.value.length
  }
}

// 模拟数据
const mockTemplates = () => [
  { id: 1, templateName: '销售日报模板', templateCode: 'sales_daily', templateType: 1, categoryName: '销售报表', status: 1, version: 3, description: '每日销售数据统计报表', updateTime: '2024-10-28 14:30:00' },
  { id: 2, templateName: '财务月报模板', templateCode: 'finance_monthly', templateType: 2, categoryName: '财务报表', status: 1, version: 5, description: '月度财务汇总报表', updateTime: '2024-10-27 10:00:00' },
  { id: 3, templateName: '员工考勤统计', templateCode: 'attendance_stats', templateType: 3, categoryName: '人事报表', status: 0, version: 1, description: '员工考勤分组统计表', updateTime: '2024-10-26 16:45:00' },
  { id: 4, templateName: '订单明细报表', templateCode: 'order_detail', templateType: 1, categoryName: '运营报表', status: 1, version: 2, description: '订单详情明细表', updateTime: '2024-10-25 09:20:00' },
  { id: 5, templateName: '库存盘点报告', templateCode: 'inventory_check', templateType: 2, categoryName: '运营报表', status: 2, version: 4, description: '仓库库存盘点汇总', updateTime: '2024-10-24 11:30:00' },
  { id: 6, templateName: '销售趋势分析', templateCode: 'sales_trend', templateType: 4, categoryName: '销售报表', status: 1, version: 1, description: '销售数据图表分析', updateTime: '2024-10-23 15:00:00' }
]

// 加载数据源列表
const loadDatasources = async () => {
  try {
    const res = await listDatasources()
    datasourceList.value = res.data || []
  } catch (error) {
    datasourceList.value = [
      { id: 1, datasourceName: '本地MySQL' }
    ]
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  loadTemplates()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.categoryId = null
  queryParams.templateType = null
  queryParams.status = null
  handleSearch()
}

// 新建
const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (template) => {
  Object.assign(formData, template)
  dialogVisible.value = true
}

// 设计
const handleDesign = (template) => {
  router.push(`/report/template/design/${template.id}`)
}

// 发布
const handlePublish = async (template) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布模板「${template.templateName}」吗？`,
      '提示',
      { type: 'warning' }
    )
    await publishTemplate(template.id)
    ElMessage.success('发布成功')
    loadTemplates()
  } catch (error) {
    // 取消或失败
  }
}

// 下线
const handleOffline = async (template) => {
  try {
    await ElMessageBox.confirm(
      `确定要下线模板「${template.templateName}」吗？下线后业务用户将无法使用此模板生成报表。`,
      '提示',
      { type: 'warning' }
    )
    await offlineTemplate(template.id)
    ElMessage.success('下线成功')
    loadTemplates()
  } catch (error) {
    // 取消或失败
  }
}

// 复制
const handleCopy = (template) => {
  copyFormData.id = template.id
  copyFormData.newName = `${template.templateName}_copy`
  copyDialogVisible.value = true
}

// 复制提交
const handleCopySubmit = async () => {
  if (!copyFormRef.value) return
  
  await copyFormRef.value.validate(async (valid) => {
    if (valid) {
      copyLoading.value = true
      try {
        await copyTemplate(copyFormData.id, copyFormData.newName)
        ElMessage.success('复制成功')
        copyDialogVisible.value = false
        loadTemplates()
      } catch (error) {
        console.error(error)
      } finally {
        copyLoading.value = false
      }
    }
  })
}

// 删除
const handleDelete = async (template) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板「${template.templateName}」吗？此操作不可恢复。`,
      '警告',
      { type: 'error', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
    )
    await deleteTemplate(template.id)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    // 取消或失败
  }
}

// 处理下拉命令
const handleCommand = (command, template) => {
  switch (command) {
    case 'edit':
      handleEdit(template)
      break
    case 'design':
      handleDesign(template)
      break
    case 'copy':
      handleCopy(template)
      break
    case 'publish':
      handlePublish(template)
      break
    case 'offline':
      handleOffline(template)
      break
    case 'delete':
      handleDelete(template)
      break
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateTemplate(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createTemplate(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadTemplates()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.templateName = ''
  formData.templateCode = ''
  formData.templateType = 1
  formData.categoryId = null
  formData.datasourceId = null
  formData.description = ''
}

// 分页
const handleSizeChange = () => {
  queryParams.pageNum = 1
  loadTemplates()
}

const handleCurrentChange = () => {
  loadTemplates()
}

// 工具函数
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

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

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已下线' }
  return map[status] || '未知'
}
</script>

<style lang="scss" scoped>
.template-container {
  max-width: 1400px;
  margin: 0 auto;
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

// 筛选卡片
.filter-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: $shadow-sm;
}

.filter-form {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 280px;
}

.filter-select {
  width: 140px;
}

.view-switch {
  :deep(.el-radio-button__inner) {
    padding: 8px 12px;
    border-radius: $radius-md;
    border: none;
    background: transparent;
    
    &:hover {
      color: $primary-color;
    }
  }
  
  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
    box-shadow: none;
  }
}

// 模板卡片网格
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.template-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
  cursor: pointer;
  transition: all $transition-normal;
  display: flex;
  flex-direction: column;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  
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

.more-btn {
  color: $text-secondary;
  
  &:hover {
    color: $text-primary;
    background: $gray-50;
  }
}

.card-body {
  flex: 1;
  margin-bottom: 16px;
}

.card-title {
  font-size: 17px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 4px;
}

.card-code {
  font-size: 12px;
  color: $text-tertiary;
  font-family: $font-family-mono;
  margin-bottom: 12px;
}

.card-desc {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid $border-color;
}

.card-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-secondary;
  
  .el-icon {
    font-size: 14px;
  }
}

.status-tag {
  border-radius: $radius-sm;
}

// 表格视图
.table-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
  margin-bottom: 24px;
}

.table-template-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.template-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
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

.template-text {
  display: flex;
  flex-direction: column;
}

.template-name {
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.template-code {
  font-size: 12px;
  color: $text-tertiary;
  font-family: $font-family-mono;
}

.version-badge {
  font-size: 12px;
  font-weight: $font-weight-medium;
  color: $text-secondary;
  background: $gray-100;
  padding: 2px 8px;
  border-radius: $radius-sm;
}

.table-actions {
  display: flex;
  gap: 4px;
}

// 分页
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  background: $bg-primary;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
}

// 空状态
.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

// 弹窗
.template-dialog {
  :deep(.el-dialog__body) {
    padding-top: 20px;
  }
}

.form-row {
  display: flex;
  gap: 20px;
  
  .el-form-item {
    margin-bottom: 24px;
  }
  
  :deep(.el-select) {
    width: 100%;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
