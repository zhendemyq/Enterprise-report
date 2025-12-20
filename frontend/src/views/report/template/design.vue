<template>
  <div class="design-container">
    <!-- 顶部工具栏 -->
    <div class="design-header">
      <div class="header-left">
        <el-button text @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="template-info">
          <el-input
            v-model="templateName"
            placeholder="请输入模板名称"
            class="name-input"
          />
          <el-tag v-if="templateId" type="info" size="small">编辑模式</el-tag>
          <el-tag v-else type="success" size="small">新建模式</el-tag>
        </div>
      </div>
      
      <div class="header-center">
        <el-radio-group v-model="activeTab" class="tab-group">
          <el-radio-button label="design">
            <el-icon><Edit /></el-icon>
            设计
          </el-radio-button>
          <el-radio-button label="data">
            <el-icon><Connection /></el-icon>
            数据
          </el-radio-button>
          <el-radio-button label="params">
            <el-icon><Filter /></el-icon>
            参数
          </el-radio-button>
          <el-radio-button label="preview">
            <el-icon><View /></el-icon>
            预览
          </el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="header-right">
        <el-button @click="handlePreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
        <el-button type="success" @click="handlePublish">
          <el-icon><Upload /></el-icon>
          发布
        </el-button>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="design-main">
      <!-- 左侧工具面板 -->
      <div class="left-panel">
        <div class="panel-section">
          <div class="section-title">组件</div>
          <div class="component-list">
            <div 
              v-for="comp in components" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="handleDragStart(comp)"
            >
              <el-icon :size="20"><component :is="comp.icon" /></el-icon>
              <span>{{ comp.name }}</span>
            </div>
          </div>
        </div>
        
        <div class="panel-section">
          <div class="section-title">字段绑定</div>
          <div class="field-list">
            <div v-if="!datasourceId" class="empty-tip">
              请先选择数据源
            </div>
            <template v-else>
              <el-select
                v-model="tableName"
                placeholder="选择表"
                size="small"
                class="table-select"
                :loading="fieldLoading"
                @change="handleTableChange"
              >
                <el-option
                  v-for="table in tableList"
                  :key="table"
                  :label="table"
                  :value="table"
                />
              </el-select>
              <div v-if="tableList.length === 0" class="empty-tip">
                暂无可用表
              </div>
              <div 
                v-for="field in fieldList" 
                :key="field.name"
                class="field-item"
                draggable="true"
                @dragstart="handleFieldDragStart(field)"
              >
                <el-icon><Document /></el-icon>
                <span class="field-name">{{ field.name }}</span>
                <span class="field-type">{{ field.type }}</span>
              </div>
            </template>
          </div>
        </div>
      </div>
      
      <!-- 中间设计区 -->
      <div class="center-panel">
        <!-- 设计面板 -->
        <div v-show="activeTab === 'design'" class="design-area">
          <div class="spreadsheet-container" ref="spreadsheetRef">
            <!-- Univer 电子表格设计器 -->
            <UniverSpreadsheet
              ref="univerRef"
              :workbook-data="workbookData"
              :fields="fieldList"
              @ready="handleUniverReady"
              @change="handleSpreadsheetChange"
            />
          </div>
          
          <!-- 设计器工具提示 -->
          <div class="design-tips" v-if="showDesignTips">
            <el-alert 
              title="设计器使用提示" 
              type="info" 
              closable
              @close="showDesignTips = false"
            >
              <template #default>
                <ul class="tips-list">
                  <li>从左侧拖拽字段到表格单元格进行数据绑定</li>
                  <li>使用 <code>${fieldName}</code> 格式插入动态字段</li>
                  <li>支持Excel公式，如 SUM、AVERAGE 等</li>
                  <li>可合并单元格、设置样式和格式化</li>
                </ul>
              </template>
            </el-alert>
          </div>
        </div>
        
        <!-- 数据配置面板 -->
        <div v-show="activeTab === 'data'" class="data-config">
          <div class="config-card">
            <h3 class="config-title">数据源配置</h3>
            
            <el-form label-position="top">
              <el-form-item label="选择数据源">
                <el-select 
                  v-model="datasourceId" 
                  placeholder="请选择数据源"
                  @change="handleDatasourceChange"
                >
                  <el-option
                    v-for="ds in datasourceList"
                    :key="ds.id"
                    :label="ds.datasourceName"
                    :value="ds.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="SQL查询">
                <div class="sql-editor">
                  <el-input
                    v-model="querySql"
                    type="textarea"
                    :rows="10"
                    placeholder="SELECT * FROM table_name WHERE ..."
                    class="sql-input"
                  />
                </div>
                <div class="sql-actions">
                  <el-button size="small" @click="handleFormatSql">
                    <el-icon><MagicStick /></el-icon>
                    格式化
                  </el-button>
                  <el-button size="small" type="primary" @click="handleExecuteSql">
                    <el-icon><CaretRight /></el-icon>
                    执行
                  </el-button>
                </div>
              </el-form-item>
              
              <el-form-item v-if="queryResult.length > 0" label="查询结果预览">
                <el-table :data="queryResult.slice(0, 5)" size="small" border>
                  <el-table-column
                    v-for="col in queryColumns"
                    :key="col"
                    :prop="col"
                    :label="col"
                    min-width="120"
                  />
                </el-table>
                <p class="result-tip">
                  共 {{ queryResult.length }} 条记录，显示前 5 条
                </p>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <!-- 参数配置面板 -->
        <div v-show="activeTab === 'params'" class="params-config">
          <div class="config-card">
            <div class="config-header">
              <h3 class="config-title">参数配置</h3>
              <el-button type="primary" size="small" @click="addParam">
                <el-icon><Plus /></el-icon>
                添加参数
              </el-button>
            </div>
            
            <div class="params-list">
              <div 
                v-for="(param, index) in paramList" 
                :key="index"
                class="param-item"
              >
                <div class="param-header">
                  <span class="param-name">{{ param.name || '新参数' }}</span>
                  <el-button text type="danger" @click="removeParam(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                
                <el-form label-position="top" class="param-form">
                  <div class="form-row">
                    <el-form-item label="参数名称" class="flex-1">
                      <el-input v-model="param.name" placeholder="如: startDate" />
                    </el-form-item>
                    <el-form-item label="显示名称" class="flex-1">
                      <el-input v-model="param.label" placeholder="如: 开始日期" />
                    </el-form-item>
                  </div>
                  
                  <div class="form-row">
                    <el-form-item label="控件类型" class="flex-1">
                      <el-select v-model="param.type">
                        <el-option label="文本框" value="input" />
                        <el-option label="数字框" value="number" />
                        <el-option label="日期选择" value="date" />
                        <el-option label="日期范围" value="daterange" />
                        <el-option label="下拉选择" value="select" />
                        <el-option label="多选" value="multiselect" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="默认值" class="flex-1">
                      <el-input v-model="param.defaultValue" placeholder="默认值" />
                    </el-form-item>
                  </div>
                  
                  <el-form-item label="SQL映射">
                    <el-input 
                      v-model="param.sqlMapping" 
                      placeholder="如: AND create_time >= #{startDate}"
                    />
                  </el-form-item>
                </el-form>
              </div>
              
              <div v-if="paramList.length === 0" class="empty-params">
                <el-empty description="暂无参数配置" :image-size="80">
                  <el-button type="primary" @click="addParam">添加参数</el-button>
                </el-empty>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 预览面板 -->
        <div v-show="activeTab === 'preview'" class="preview-panel">
          <div class="preview-card">
            <div class="preview-header">
              <h3 class="preview-title">报表预览</h3>
              <div class="preview-actions">
                <el-button size="small">
                  <el-icon><Download /></el-icon>
                  导出 Excel
                </el-button>
                <el-button size="small">
                  <el-icon><Document /></el-icon>
                  导出 PDF
                </el-button>
              </div>
            </div>
            
            <div class="preview-content">
              <div class="preview-placeholder">
                <el-icon :size="48"><Picture /></el-icon>
                <p>点击「生成预览」按钮查看报表效果</p>
                <el-button type="primary" @click="generatePreview">
                  生成预览
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧属性面板 -->
      <div class="right-panel">
        <div class="panel-section">
          <div class="section-title">属性设置</div>
          
          <el-form label-position="top" class="property-form">
            <el-form-item label="单元格样式">
              <div class="style-toolbar">
                <el-button-group>
                  <el-button size="small">
                    <el-icon><Operation /></el-icon>
                  </el-button>
                  <el-button size="small">
                    <el-icon><EditPen /></el-icon>
                  </el-button>
                  <el-button size="small">
                    <el-icon><Minus /></el-icon>
                  </el-button>
                </el-button-group>
              </div>
            </el-form-item>
            
            <el-form-item label="对齐方式">
              <el-button-group>
                <el-button size="small">
                  <el-icon><Back /></el-icon>
                </el-button>
                <el-button size="small">
                  <el-icon><Position /></el-icon>
                </el-button>
                <el-button size="small">
                  <el-icon><Right /></el-icon>
                </el-button>
              </el-button-group>
            </el-form-item>
            
            <el-form-item label="字体大小">
              <el-input-number v-model="fontSize" :min="8" :max="72" size="small" />
            </el-form-item>
            
            <el-form-item label="背景色">
              <el-color-picker v-model="bgColor" size="small" />
            </el-form-item>
            
            <el-form-item label="字体颜色">
              <el-color-picker v-model="fontColor" size="small" />
            </el-form-item>
          </el-form>
        </div>
        
        <div class="panel-section">
          <div class="section-title">模板设置</div>
          
          <el-form label-position="top" class="property-form">
            <el-form-item label="模板类型">
              <el-select v-model="templateType" placeholder="选择类型">
                <el-option label="明细表" :value="1" />
                <el-option label="汇总表" :value="2" />
                <el-option label="分组统计表" :value="3" />
                <el-option label="图表报表" :value="4" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="分页大小">
              <el-input-number v-model="pageSize" :min="100" :max="100000" :step="100" />
            </el-form-item>
            
            <el-form-item label="启用缓存">
              <el-switch v-model="enableCache" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTemplateDetail, createTemplate, updateTemplate, publishTemplate } from '@/api/template'
import { listDatasources, executeQuery, getTables, getTableColumns } from '@/api/datasource'
import { generateReport, previewReport, downloadReport } from '@/api/report'
import UniverSpreadsheet from '@/components/UniverSpreadsheet.vue'
import * as XLSX from 'xlsx'

const route = useRoute()
const router = useRouter()

// 模板ID
const templateId = ref(route.params.id || null)

// 基本信息
const templateName = ref('未命名报表模板')
const templateType = ref(1)
const activeTab = ref('design')
const saveLoading = ref(false)

// Univer设计器相关
const spreadsheetRef = ref(null)
const univerRef = ref(null)
const showDesignTips = ref(true)
const workbookData = ref(null)
const selectedCell = ref('')
const columns = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']

// 组件列表
const components = [
  { type: 'text', name: '文本', icon: 'Document' },
  { type: 'image', name: '图片', icon: 'Picture' },
  { type: 'table', name: '表格', icon: 'Grid' },
  { type: 'chart', name: '图表', icon: 'TrendCharts' },
  { type: 'formula', name: '公式', icon: 'Operation' }
]

// 数据源相关
const datasourceId = ref(null)
const datasourceList = ref([])
const querySql = ref('')
const queryResult = ref([])
const queryColumns = ref([])
const fieldList = ref([])
const tableList = ref([])
const tableName = ref('')
const fieldLoading = ref(false)

// 参数配置
const paramList = ref([])

// 样式相关
const fontSize = ref(12)
const bgColor = ref('')
const fontColor = ref('#000000')
const pageSize = ref(1000)
const enableCache = ref(true)

const cellData = reactive({})

// 初始化
onMounted(async () => {
  await loadDatasources()
  if (templateId.value) {
    await loadTemplateDetail()
  }
})

// 加载数据源
const loadDatasources = async () => {
  try {
    const res = await listDatasources()
    datasourceList.value = res.data || []
  } catch (error) {
    datasourceList.value = []
    ElMessage.error('???????')
  }
}

// 加载模板详情
const loadTemplateDetail = async () => {
  try {
    const res = await getTemplateDetail(templateId.value)
    if (res.data) {
      templateName.value = res.data.templateName
      templateType.value = res.data.templateType
      datasourceId.value = res.data.datasourceId
      querySql.value = res.data.querySql || ''
      if (res.data.paramConfig) {
        paramList.value = JSON.parse(res.data.paramConfig)
      }
      if (datasourceId.value) {
        await handleDatasourceChange()
      }
    }
  } catch (error) {
    console.error(error)
  }
}

// 处理数据源变更
const handleDatasourceChange = async () => {
  if (!datasourceId.value) {
    fieldList.value = []
    tableList.value = []
    tableName.value = ''
    return
  }

  fieldLoading.value = true
  try {
    const res = await getTables(datasourceId.value)
    tableList.value = res.data || []
    tableName.value = tableList.value[0] || ''
    if (tableName.value) {
      await loadTableColumns(tableName.value)
    } else {
      fieldList.value = []
    }
  } catch (error) {
    tableList.value = []
    tableName.value = ''
    fieldList.value = []
    ElMessage.error('字段加载失败')
  } finally {
    fieldLoading.value = false
  }
}

const handleTableChange = async (value) => {
  if (!value) {
    fieldList.value = []
    return
  }
  fieldLoading.value = true
  try {
    await loadTableColumns(value)
  } catch (error) {
    fieldList.value = []
    ElMessage.error('字段加载失败')
  } finally {
    fieldLoading.value = false
  }
}

const loadTableColumns = async (table) => {
  const res = await getTableColumns(datasourceId.value, table)
  const columns = res.data || []
  fieldList.value = columns.map((col) => ({
    name: col.COLUMN_NAME || col.column_name || col.columnName || col.name,
    type: col.DATA_TYPE || col.data_type || col.dataType || '',
    label: col.COLUMN_COMMENT || col.column_comment || col.columnComment || ''
  }))
}
// 格式化SQL
const handleFormatSql = () => {
  // 简单格式化
  querySql.value = querySql.value
    .replace(/\s+/g, ' ')
    .replace(/\s*,\s*/g, ',\n  ')
    .replace(/\s*FROM\s*/gi, '\nFROM ')
    .replace(/\s*WHERE\s*/gi, '\nWHERE ')
    .replace(/\s*AND\s*/gi, '\n  AND ')
    .replace(/\s*ORDER BY\s*/gi, '\nORDER BY ')
    .replace(/\s*GROUP BY\s*/gi, '\nGROUP BY ')
    .trim()
}

const inferFieldType = (value) => {
  if (value === null || value === undefined) return 'string'
  if (typeof value === 'number') return 'number'
  if (typeof value === 'boolean') return 'boolean'
  if (Object.prototype.toString.call(value) === '[object Date]') return 'date'
  if (typeof value === 'string' && /^\\d{4}-\\d{2}-\\d{2}/.test(value)) return 'date'
  return 'string'
}

// 执行SQL
const handleExecuteSql = async () => {
  if (!datasourceId.value) {
    ElMessage.warning('请先选择数据源')
    return
  }
  if (!querySql.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }
  
  try {
    const res = await executeQuery(datasourceId.value, querySql.value)
    queryResult.value = res.data || []
    if (queryResult.value.length > 0) {
      queryColumns.value = Object.keys(queryResult.value[0])
    }
    ElMessage.success('查询成功')
  } catch (error) {
    queryResult.value = []
    queryColumns.value = []
    ElMessage.error('????')
  }
}

// 添加参数
const addParam = () => {
  paramList.value.push({
    name: '',
    label: '',
    type: 'input',
    defaultValue: '',
    sqlMapping: ''
  })
}

// 删除参数
const removeParam = (index) => {
  paramList.value.splice(index, 1)
}

// 获取单元格内容
const getCellContent = (col, row) => {
  return cellData[`${col}${row}`] || ''
}

// 选中单元格
const selectCell = (cell) => {
  selectedCell.value = cell
}

// Univer设计器就绪事件
const handleUniverReady = ({ univer, workbook }) => {
  console.log('Univer设计器已就绪', univer, workbook)
}

// 电子表格变化事件
const handleSpreadsheetChange = (data) => {
  console.log('电子表格数据变化', data)
}

// 拖拽开始
const handleDragStart = (comp) => {
  // 处理组件拖拽
}

// 字段拖拽开始 - 插入到当前选中单元格
const handleFieldDragStart = (field) => {
  // 将字段名存储到拖拽数据中
  event.dataTransfer.setData('text/plain', `\${${field.name}}`)
  event.dataTransfer.setData('field-name', field.name)
}

// 处理字段放置
const handleFieldDrop = (event) => {
  event.preventDefault()
  const fieldName = event.dataTransfer.getData('field-name')
  if (fieldName && univerRef.value) {
    univerRef.value.insertFieldPlaceholder(fieldName)
    ElMessage.success(`已插入字段: ${fieldName}`)
  }
}

// 返回
const handleBack = () => {
  router.push('/report/template')
}

// 预览
const handlePreview = () => {
  activeTab.value = 'preview'
}

// 生成预览
const generatePreview = () => {
  ElMessage.info('正在生成预览...')
}

// 保存
const handleSave = async () => {
  saveLoading.value = true
  try {
    // 获取Univer设计器数据
    let spreadsheetData = null
    if (univerRef.value) {
      spreadsheetData = univerRef.value.exportToJson()
    }
    
    const designJson = {
      templateName: templateName.value,
      templateType: templateType.value,
      datasourceId: datasourceId.value,
      querySql: querySql.value,
      paramConfig: JSON.stringify(paramList.value),
      spreadsheetData: spreadsheetData, // Univer设计器数据
      cellData: cellData,
      styleConfig: {
        fontSize: fontSize.value,
        pageSize: pageSize.value,
        enableCache: enableCache.value
      }
    }
    
    if (templateId.value) {
      await saveTemplateDesign(templateId.value, designJson)
    }
    
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

// 发布
const handlePublish = async () => {
  try {
    await ElMessageBox.confirm('确定要发布此模板吗？发布后业务用户即可使用此模板生成报表。', '提示', {
      type: 'warning'
    })
    
    if (templateId.value) {
      await publishTemplate(templateId.value)
      ElMessage.success('发布成功')
    } else {
      ElMessage.warning('请先保存模板')
    }
  } catch (error) {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.design-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: $bg-secondary;
  margin: -24px;
}

// 顶部工具栏
.design-header {
  height: 60px;
  background: $bg-primary;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.template-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name-input {
  width: 240px;
  
  :deep(.el-input__wrapper) {
    border: none;
    box-shadow: none;
    background: transparent;
    
    &:hover,
    &.is-focus {
      box-shadow: 0 0 0 1px $border-color inset;
    }
  }
  
  :deep(.el-input__inner) {
    font-size: 16px;
    font-weight: $font-weight-semibold;
  }
}

.header-center {
  .tab-group {
    :deep(.el-radio-button__inner) {
      border-radius: $radius-md;
      border: none;
      padding: 8px 16px;
      background: transparent;
      
      .el-icon {
        margin-right: 6px;
      }
    }
    
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      background: rgba($primary-color, 0.1);
      color: $primary-color;
      box-shadow: none;
    }
  }
}

.header-right {
  display: flex;
  gap: 12px;
}

// 主内容区
.design-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// 左侧面板
.left-panel {
  width: 260px;
  background: $bg-primary;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-section {
  padding: 16px;
  border-bottom: 1px solid $border-color;
  
  &:last-child {
    flex: 1;
    overflow: auto;
  }
}

.section-title {
  font-size: 12px;
  font-weight: $font-weight-semibold;
  color: $text-secondary;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

.component-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.component-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border-radius: $radius-md;
  background: $gray-50;
  cursor: grab;
  transition: all $transition-fast;
  
  &:hover {
    background: $gray-100;
    transform: translateY(-2px);
  }
  
  span {
    font-size: 12px;
    color: $text-secondary;
  }
}

.field-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.table-select {
  margin-bottom: 8px;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: $radius-sm;
  cursor: grab;
  transition: background $transition-fast;
  
  &:hover {
    background: $gray-50;
  }
  
  .el-icon {
    color: $text-tertiary;
  }
}

.field-name {
  flex: 1;
  font-size: 13px;
  color: $text-primary;
}

.field-type {
  font-size: 11px;
  color: $text-tertiary;
  font-family: $font-family-mono;
}

.empty-tip {
  font-size: 13px;
  color: $text-secondary;
  text-align: center;
  padding: 20px;
}

// 中间设计区
.center-panel {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.design-area,
.data-config,
.params-config,
.preview-panel {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.design-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.design-tips {
  flex-shrink: 0;
  
  .tips-list {
    margin: 8px 0 0;
    padding-left: 20px;
    
    li {
      margin: 4px 0;
      font-size: 13px;
      color: #606266;
    }
    
    code {
      background: rgba(64, 158, 255, 0.1);
      color: #409eff;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
    }
  }
}

.spreadsheet-container {
  flex: 1;
  min-height: 400px;
  background: $bg-primary;
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
  overflow: hidden;
}

.spreadsheet-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.placeholder-content {
  padding: 40px;
  text-align: center;
  background: $gray-50;
  
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
    font-size: 14px;
  }
  
  .hint {
    font-size: 12px;
    color: $text-tertiary;
    margin-top: 4px;
  }
}


// 配置卡片
.config-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 24px;
  box-shadow: $shadow-sm;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.config-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 20px;
  
  .config-header & {
    margin-bottom: 0;
  }
}

.sql-editor {
  margin-bottom: 12px;
}

.sql-input {
  :deep(.el-textarea__inner) {
    font-family: $font-family-mono;
    font-size: 13px;
    line-height: 1.6;
    background: $gray-50;
    border-radius: $radius-md;
  }
}

.sql-actions {
  display: flex;
  gap: 8px;
}

.result-tip {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 8px;
}

// 参数配置
.params-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.param-item {
  background: $gray-50;
  border-radius: $radius-lg;
  padding: 16px;
}

.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.param-name {
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.param-form {
  .el-form-item {
    margin-bottom: 12px;
  }
}

.form-row {
  display: flex;
  gap: 16px;
}

.empty-params {
  padding: 40px;
}

// 预览面板
.preview-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-sm;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid $border-color;
}

.preview-title {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder {
  text-align: center;
  
  .el-icon {
    color: $text-tertiary;
    margin-bottom: 16px;
  }
  
  p {
    color: $text-secondary;
    margin-bottom: 16px;
  }
}

// 右侧属性面板
.right-panel {
  width: 280px;
  background: $bg-primary;
  border-left: 1px solid $border-color;
  overflow: auto;
}

.property-form {
  .el-form-item {
    margin-bottom: 16px;
  }
  
  .el-select {
    width: 100%;
  }
}

.style-toolbar {
  display: flex;
  gap: 8px;
}
</style>















