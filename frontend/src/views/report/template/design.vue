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
          <div class="section-title">
            组件
            <el-tooltip content="双击或拖拽组件到设计器中" placement="right">
              <el-icon class="help-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="component-list">
            <div
              v-for="comp in components"
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="handleDragStart($event, comp)"
              @dblclick="handleComponentInsert(comp)"
              :title="`双击插入${comp.name}组件`"
            >
              <el-icon :size="20"><component :is="comp.icon" /></el-icon>
              <span>{{ comp.name }}</span>
            </div>
          </div>
          <div class="component-tip">
            <el-text type="info" size="small">提示：先选中单元格，再双击组件插入</el-text>
          </div>
        </div>

        <div class="panel-section">
          <div class="section-title">
            字段绑定
            <el-tooltip content="拖拽字段到设计器单元格中绑定数据" placement="right">
              <el-icon class="help-icon"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
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
                @dragstart="handleFieldDragStart($event, field)"
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
          <div class="spreadsheet-container" ref="spreadsheetRef" @dragover.prevent @drop="handleFieldDrop">
            <!-- Univer 电子表格设计器 -->
            <UniverSpreadsheet
              ref="univerRef"
              :workbook-data="workbookData"
              :fields="fieldList"
              @ready="handleUniverReady"
              @change="handleSpreadsheetChange"
              @cellSelect="handleCellSelect"
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
                <el-button size="small" type="primary" :loading="previewLoading" @click="generatePreview">
                  <el-icon><RefreshRight /></el-icon>
                  {{ previewData.length > 0 ? '刷新预览' : '生成预览' }}
                </el-button>
                <el-button size="small" :disabled="!previewData.length" @click="handleExportExcel">
                  <el-icon><Download /></el-icon>
                  导出 Excel
                </el-button>
                <el-button size="small" :disabled="!previewData.length || !templateId" :loading="pdfExporting" @click="handleExportPdf">
                  <el-icon><Document /></el-icon>
                  导出 PDF
                </el-button>
              </div>
            </div>

            <div class="preview-content">
              <!-- 加载中 -->
              <div v-if="previewLoading" class="preview-loading">
                <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
                <p>正在生成预览...</p>
              </div>
              <!-- 预览数据表格 -->
              <div v-else-if="previewData.length > 0" class="preview-table-wrapper">
                <el-table :data="previewData" border stripe max-height="calc(100vh - 300px)">
                  <el-table-column
                    v-for="(col, index) in previewColumns"
                    :key="index"
                    :prop="col"
                    :label="col"
                    min-width="120"
                  />
                </el-table>
                <p class="preview-info">共 {{ previewData.length }} 条数据</p>
              </div>
              <!-- 空状态 -->
              <div v-else class="preview-placeholder">
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
            <el-form-item label="当前单元格">
              <el-input v-model="currentSelectedCell" readonly size="small" placeholder="未选中" />
            </el-form-item>

            <el-form-item label="单元格样式">
              <div class="style-toolbar">
                <el-button-group>
                  <el-button size="small" :type="cellStyle.fontWeight === 'bold' ? 'primary' : ''" @click="toggleBold" title="加粗">
                    <strong>B</strong>
                  </el-button>
                  <el-button size="small" :type="cellStyle.fontStyle === 'italic' ? 'primary' : ''" @click="toggleItalic" title="斜体">
                    <em>I</em>
                  </el-button>
                  <el-button size="small" :type="cellStyle.textDecoration === 'underline' ? 'primary' : ''" @click="toggleUnderline" title="下划线">
                    <u>U</u>
                  </el-button>
                </el-button-group>
              </div>
            </el-form-item>

            <el-form-item label="对齐方式">
              <el-button-group>
                <el-button size="small" :type="cellStyle.textAlign === 'left' ? 'primary' : ''" @click="setTextAlign('left')" title="左对齐">
                  <el-icon><Back /></el-icon>
                </el-button>
                <el-button size="small" :type="cellStyle.textAlign === 'center' ? 'primary' : ''" @click="setTextAlign('center')" title="居中">
                  <el-icon><Position /></el-icon>
                </el-button>
                <el-button size="small" :type="cellStyle.textAlign === 'right' ? 'primary' : ''" @click="setTextAlign('right')" title="右对齐">
                  <el-icon><Right /></el-icon>
                </el-button>
              </el-button-group>
            </el-form-item>

            <el-form-item label="字体大小">
              <el-input-number v-model="cellStyle.fontSize" :min="8" :max="72" size="small" @change="applyCellStyle" />
            </el-form-item>

            <el-form-item label="背景色">
              <el-color-picker v-model="cellStyle.backgroundColor" size="small" @change="applyCellStyle" />
            </el-form-item>

            <el-form-item label="字体颜色">
              <el-color-picker v-model="cellStyle.color" size="small" @change="applyCellStyle" />
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
import { getTemplateDetail, createTemplate, updateTemplate, publishTemplate, saveTemplateDesign } from '@/api/template'
import { listDatasources, executeQuery, getTables, getTableColumns } from '@/api/datasource'
import { generateReport, previewReport, downloadReport } from '@/api/report'
import UniverSpreadsheet from '@/components/UniverSpreadsheet.vue'
import * as XLSX from 'xlsx-js-style'

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

// 当前选中单元格
const currentSelectedCell = ref('')

// 单元格样式
const cellStyle = reactive({
  fontWeight: 'normal',
  fontStyle: 'normal',
  textDecoration: 'none',
  textAlign: 'left',
  fontSize: 12,
  backgroundColor: '',
  color: '#000000'
})

// 预览相关
const previewLoading = ref(false)
const previewData = ref([])
const previewColumns = ref([])
const generatedReportId = ref(null)

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
        // 兼容处理：paramConfig 可能是字符串或对象
        if (typeof res.data.paramConfig === 'string') {
          try {
            paramList.value = JSON.parse(res.data.paramConfig)
          } catch (e) {
            console.error('解析paramConfig失败:', e)
            paramList.value = []
          }
        } else if (Array.isArray(res.data.paramConfig)) {
          paramList.value = res.data.paramConfig
        } else {
          paramList.value = []
        }
      }

      // 加载设计器数据（templateConfig）
      if (res.data.templateConfig) {
        const config = res.data.templateConfig
        // 尝试恢复设计器数据
        if (config.spreadsheetData && univerRef.value) {
          univerRef.value.importFromJson(config.spreadsheetData)
        } else if (config.cellData && univerRef.value) {
          // 兼容旧格式
          univerRef.value.importFromJson({ cellData: config.cellData })
        }
        // 保存到 workbookData 以便后续使用
        workbookData.value = config.spreadsheetData || config
      }

      if (datasourceId.value) {
        await handleDatasourceChange()
      }
    }
  } catch (error) {
    console.error(error)
  }
}

// 从SQL语句中提取表名
const extractTableFromSql = (sql) => {
  if (!sql) return null
  // 匹配 FROM 后面的表名，支持多种格式
  // SELECT * FROM table_name
  // SELECT * FROM table_name WHERE ...
  // SELECT * FROM schema.table_name
  const fromMatch = sql.match(/\bFROM\s+([`"]?[\w.]+[`"]?)/i)
  if (fromMatch) {
    // 去除可能的反引号或引号，并取最后一部分（处理 schema.table 格式）
    let tbl = fromMatch[1].replace(/[`"]/g, '')
    if (tbl.includes('.')) {
      tbl = tbl.split('.').pop()
    }
    return tbl
  }
  return null
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

    // 优先根据SQL语句自动选择表
    const extractedTable = extractTableFromSql(querySql.value)
    if (extractedTable && tableList.value.includes(extractedTable)) {
      tableName.value = extractedTable
    } else {
      tableName.value = tableList.value[0] || ''
    }

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
    // 自动更新SQL查询语句中的表名
    updateSqlTableName(value)
  } catch (error) {
    fieldList.value = []
    ElMessage.error('字段加载失败')
  } finally {
    fieldLoading.value = false
  }
}

// 更新SQL语句中的表名
const updateSqlTableName = (newTableName) => {
  if (!querySql.value.trim()) {
    // 如果SQL为空，生成默认的SELECT语句
    querySql.value = `SELECT *\nFROM ${newTableName}`
    return
  }

  // 替换SQL中的表名
  const currentTable = extractTableFromSql(querySql.value)
  if (currentTable && currentTable !== newTableName) {
    // 使用正则替换FROM后面的表名
    querySql.value = querySql.value.replace(
      /(\bFROM\s+)([`"]?[\w.]+[`"]?)/i,
      `$1${newTableName}`
    )
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

// 监听SQL变化，自动选择对应的表
watch(querySql, async (newSql) => {
  if (!newSql || !datasourceId.value || tableList.value.length === 0) return

  const extractedTable = extractTableFromSql(newSql)
  if (extractedTable && tableList.value.includes(extractedTable) && extractedTable !== tableName.value) {
    tableName.value = extractedTable
    await loadTableColumns(extractedTable)
  }
}, { immediate: false })

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

// ==================== 单元格样式操作 ====================

// 处理单元格选择事件（从设计器组件接收）
const handleCellSelect = (data) => {
  if (data && data.col && data.row) {
    currentSelectedCell.value = `${data.col}${data.row}`
    // 加载该单元格的样式
    loadCellStyle(currentSelectedCell.value)
  }
}

// 加载单元格样式
const loadCellStyle = (cellRef) => {
  if (!univerRef.value) return
  const data = univerRef.value.exportToJson()
  const styles = data.cellStyles || {}
  const style = styles[cellRef] || {}

  cellStyle.fontWeight = style.fontWeight || 'normal'
  cellStyle.fontStyle = style.fontStyle || 'normal'
  cellStyle.textDecoration = style.textDecoration || 'none'
  cellStyle.textAlign = style.textAlign || 'left'
  cellStyle.fontSize = style.fontSize || 12
  cellStyle.backgroundColor = style.backgroundColor || ''
  cellStyle.color = style.color || '#000000'
}

// 应用单元格样式
const applyCellStyle = () => {
  if (!currentSelectedCell.value || !univerRef.value) {
    ElMessage.warning('请先选择一个单元格')
    return
  }

  const data = univerRef.value.exportToJson()
  if (!data.cellStyles) data.cellStyles = {}

  data.cellStyles[currentSelectedCell.value] = {
    fontWeight: cellStyle.fontWeight,
    fontStyle: cellStyle.fontStyle,
    textDecoration: cellStyle.textDecoration,
    textAlign: cellStyle.textAlign,
    fontSize: cellStyle.fontSize,
    backgroundColor: cellStyle.backgroundColor,
    color: cellStyle.color
  }

  univerRef.value.importFromJson(data)
}

// 切换加粗
const toggleBold = () => {
  cellStyle.fontWeight = cellStyle.fontWeight === 'bold' ? 'normal' : 'bold'
  applyCellStyle()
}

// 切换斜体
const toggleItalic = () => {
  cellStyle.fontStyle = cellStyle.fontStyle === 'italic' ? 'normal' : 'italic'
  applyCellStyle()
}

// 切换下划线
const toggleUnderline = () => {
  cellStyle.textDecoration = cellStyle.textDecoration === 'underline' ? 'none' : 'underline'
  applyCellStyle()
}

// 设置文本对齐
const setTextAlign = (align) => {
  cellStyle.textAlign = align
  applyCellStyle()
}

// Univer设计器就绪事件
const handleUniverReady = ({ univer, workbook }) => {
  console.log('Univer设计器已就绪', univer, workbook)
  // 设计器准备好后，如果有保存的数据，恢复它
  if (workbookData.value && univerRef.value) {
    univerRef.value.importFromJson(workbookData.value)
  }
}

// 电子表格变化事件
const handleSpreadsheetChange = (data) => {
  console.log('电子表格数据变化', data)
}

// 拖拽开始
const handleDragStart = (event, comp) => {
  if (!event?.dataTransfer) return
  event.dataTransfer.setData('component-type', comp.type)
  event.dataTransfer.setData('component-name', comp.name)
}

const handleComponentInsert = (comp) => {
  if (!univerRef.value?.insertComponentPlaceholder) return
  const inserted = univerRef.value.insertComponentPlaceholder(comp.type, comp.name)
  if (!inserted) {
    ElMessage.warning('请先选择单元格')
  }
}

// 字段拖拽开始 - 插入到当前选中单元格
const handleFieldDragStart = (event, field) => {
  // 将字段名存储到拖拽数据中
  event.dataTransfer.setData('text/plain', `\${${field.name}}`)
  event.dataTransfer.setData('field-name', field.name)
}

// 处理字段放置
const handleFieldDrop = (event) => {
  event.preventDefault()
  const fieldName = event.dataTransfer.getData('field-name')
  const componentType = event.dataTransfer.getData('component-type')
  const componentName = event.dataTransfer.getData('component-name')
  if (fieldName && univerRef.value) {
    univerRef.value.insertFieldPlaceholder(fieldName)
    ElMessage.success(`已插入字段: ${fieldName}`)
    return
  }
  if (componentType && univerRef.value?.insertComponentPlaceholder) {
    const inserted = univerRef.value.insertComponentPlaceholder(componentType, componentName)
    if (inserted) {
      ElMessage.success(`已插入组件: ${componentName || componentType}`)
    } else {
      ElMessage.warning('请先选择单元格')
    }
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

// 从设计器中提取字段布局
const extractFieldLayout = () => {
  if (!univerRef.value) return null

  const spreadsheetData = univerRef.value.exportToJson()
  if (!spreadsheetData || !spreadsheetData.cellData) return null

  const cellData = spreadsheetData.cellData
  const fieldPattern = /\$\{(\w+)\}/

  // 解析单元格数据，提取字段映射
  // 格式: { col: 'A', row: 1, field: 'product_name', label: '产品名称' }
  const fieldMappings = []
  const headerRow = {} // 存储表头行的内容

  // 遍历所有单元格
  for (const [cellRef, value] of Object.entries(cellData)) {
    if (!value) continue

    const match = value.match(fieldPattern)
    if (match) {
      const col = cellRef.match(/^[A-Z]+/)[0]
      const row = parseInt(cellRef.match(/\d+$/)[0])
      const fieldName = match[1]

      // 查找该列的表头（假设表头在第1行）
      const headerCell = `${col}1`
      const headerValue = cellData[headerCell] || fieldName

      fieldMappings.push({
        col,
        row,
        field: fieldName,
        label: headerValue.startsWith('${') ? fieldName : headerValue
      })
    }
  }

  // 按列排序
  const colOrder = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  fieldMappings.sort((a, b) => colOrder.indexOf(a.col) - colOrder.indexOf(b.col))

  return fieldMappings.length > 0 ? fieldMappings : null
}

// 生成预览
const generatePreview = async () => {
  if (!datasourceId.value) {
    ElMessage.warning('请先选择数据源')
    activeTab.value = 'data'
    return
  }

  if (!querySql.value.trim()) {
    ElMessage.warning('请先配置SQL查询')
    activeTab.value = 'data'
    return
  }

  previewLoading.value = true
  previewData.value = []
  previewColumns.value = []

  try {
    // 直接执行SQL查询获取预览数据
    const res = await executeQuery(datasourceId.value, querySql.value)
    const data = res.data || []

    if (data.length > 0) {
      // 尝试从设计器中提取字段布局
      const fieldLayout = extractFieldLayout()

      if (fieldLayout && fieldLayout.length > 0) {
        // 使用设计器布局：只显示设计器中配置的字段
        const layoutFields = fieldLayout.map(f => f.field)
        const layoutLabels = fieldLayout.map(f => f.label)

        // 过滤数据，只保留设计器中配置的字段
        const filteredData = data.map(row => {
          const newRow = {}
          fieldLayout.forEach(f => {
            newRow[f.label] = row[f.field] !== undefined ? row[f.field] : ''
          })
          return newRow
        })

        previewColumns.value = layoutLabels
        previewData.value = filteredData
        ElMessage.success(`预览成功，共 ${data.length} 条数据（按设计器布局显示 ${layoutFields.length} 个字段）`)
      } else {
        // 没有设计器布局，显示所有字段
        previewColumns.value = Object.keys(data[0])
        previewData.value = data
        ElMessage.success(`预览成功，共 ${data.length} 条数据（显示全部字段，可在设计器中拖拽字段来自定义布局）`)
      }
    } else {
      ElMessage.info('查询结果为空')
    }
  } catch (error) {
    console.error('预览失败:', error)
    ElMessage.error('预览失败: ' + (error.message || '请检查SQL配置'))
  } finally {
    previewLoading.value = false
  }
}

// 导出Excel
const handleExportExcel = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  try {
    // 获取设计器中的样式数据
    let cellStyles = {}
    if (univerRef.value) {
      const designData = univerRef.value.exportToJson()
      cellStyles = designData.cellStyles || {}
    }

    // 使用xlsx库导出
    const worksheet = XLSX.utils.json_to_sheet(previewData.value)

    // 应用样式到工作表
    const range = XLSX.utils.decode_range(worksheet['!ref'] || 'A1')

    // 遍历所有单元格应用样式
    for (let row = range.s.r; row <= range.e.r; row++) {
      for (let col = range.s.c; col <= range.e.c; col++) {
        const colLetter = XLSX.utils.encode_col(col)
        const cellRef = `${colLetter}${row + 1}`
        const xlsxCellRef = XLSX.utils.encode_cell({ r: row, c: col })

        // 查找对应的样式
        const style = cellStyles[cellRef]
        if (style && worksheet[xlsxCellRef]) {
          // xlsx-js-style 格式的样式对象
          worksheet[xlsxCellRef].s = {
            font: {
              bold: style.fontWeight === 'bold',
              italic: style.fontStyle === 'italic',
              underline: style.textDecoration === 'underline',
              sz: style.fontSize || 12,
              color: style.color ? { rgb: style.color.replace('#', '') } : undefined
            },
            fill: style.backgroundColor ? {
              fgColor: { rgb: style.backgroundColor.replace('#', '') },
              patternType: 'solid'
            } : undefined,
            alignment: {
              horizontal: style.textAlign || 'left',
              vertical: 'center'
            }
          }
        }

        // 为表头行（第一行）添加默认样式
        if (row === 0 && worksheet[xlsxCellRef]) {
          worksheet[xlsxCellRef].s = worksheet[xlsxCellRef].s || {}
          worksheet[xlsxCellRef].s.font = {
            ...worksheet[xlsxCellRef].s?.font,
            bold: true
          }
          worksheet[xlsxCellRef].s.fill = {
            fgColor: { rgb: 'F5F7FA' },
            patternType: 'solid'
          }
          worksheet[xlsxCellRef].s.alignment = {
            horizontal: 'center',
            vertical: 'center'
          }
        }
      }
    }

    // 设置列宽
    const colWidths = []
    for (let col = range.s.c; col <= range.e.c; col++) {
      colWidths.push({ wch: 15 }) // 默认列宽15字符
    }
    worksheet['!cols'] = colWidths

    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '报表数据')
    XLSX.writeFile(workbook, `${templateName.value}_预览.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导出PDF
const pdfExporting = ref(false)

const handleExportPdf = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  // 检查模板是否已保存
  if (!templateId.value) {
    ElMessage.warning('请先保存模板后再导出PDF')
    return
  }

  pdfExporting.value = true
  try {
    // 调用后端生成PDF报表
    const res = await generateReport({
      templateId: templateId.value,
      reportName: `${templateName.value}_预览`,
      fileType: 'pdf',
      async: false
    })

    if (res.data && res.data.id) {
      // 生成成功，下载PDF文件
      const downloadRes = await downloadReport(res.data.id)

      // 创建下载链接 (downloadRes 是 axios response 对象，需要使用 .data)
      const blob = new Blob([downloadRes.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${templateName.value}_预览.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      ElMessage.success('PDF导出成功')
    } else {
      ElMessage.error('PDF生成失败')
    }
  } catch (error) {
    console.error('PDF导出失败:', error)
    ElMessage.error('PDF导出失败: ' + (error.message || '请检查后端服务'))
  } finally {
    pdfExporting.value = false
  }
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
  display: flex;
  align-items: center;
  gap: 6px;

  .help-icon {
    font-size: 14px;
    color: $text-tertiary;
    cursor: help;

    &:hover {
      color: $primary-color;
    }
  }
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
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  &:active {
    cursor: grabbing;
  }

  span {
    font-size: 12px;
    color: $text-secondary;
  }
}

.component-tip {
  margin-top: 8px;
  padding: 8px;
  background: $gray-50;
  border-radius: $radius-sm;
  text-align: center;
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

.preview-loading {
  text-align: center;

  .loading-icon {
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

.preview-table-wrapper {
  width: 100%;
  padding: 16px;
  overflow: auto;
}

.preview-info {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 12px;
  text-align: right;
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

















