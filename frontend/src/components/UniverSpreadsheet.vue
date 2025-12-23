<template>
  <div ref="containerRef" class="univer-spreadsheet-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>正在加载设计器...</p>
    </div>
    
    <!-- 备用表格设计器 -->
    <div v-if="useFallback" class="fallback-spreadsheet">
      <div class="fallback-toolbar">
        <span class="toolbar-title">报表设计器</span>
        <div class="toolbar-actions">
          <button class="toolbar-btn" @click="handleInsertRow" title="插入行">
            <span>+行</span>
          </button>
          <button class="toolbar-btn" @click="handleInsertCol" title="插入列">
            <span>+列</span>
          </button>
          <button class="toolbar-btn" @click="handleMergeCells" title="合并单元格">
            <span>合并</span>
          </button>
          <button class="toolbar-btn" @click="handleBold" title="加粗">
            <span style="font-weight:bold">B</span>
          </button>
        </div>
      </div>
      
      <div class="formula-bar">
        <span class="cell-ref">{{ selectedCell || 'A1' }}</span>
        <input 
          v-model="formulaInput" 
          class="formula-input" 
          placeholder="输入内容或公式..."
          @keydown.enter="applyFormula"
        />
      </div>
      
      <div class="fallback-grid" @drop="handleDrop" @dragover.prevent>
        <table ref="tableRef">
          <thead>
            <tr>
              <th class="row-header"></th>
              <th 
                v-for="col in fallbackColumns" 
                :key="col" 
                class="col-header"
                :class="{ 'selected': selectedCol === col }"
                @click="selectColumn(col)"
              >
                {{ col }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in fallbackRows" :key="row">
              <td 
                class="row-header"
                :class="{ 'selected': selectedRow === row }"
                @click="selectRow(row)"
              >
                {{ row }}
              </td>
              <td 
                v-for="col in fallbackColumns" 
                :key="`${col}${row}`"
                class="cell"
                :class="{ 
                  'selected': isSelected(col, row),
                  'has-content': getCellValue(col, row),
                  'is-formula': isFormula(col, row)
                }"
                :style="getCellStyle(col, row)"
                @click="selectCell(col, row)"
                @dblclick="editCell(col, row)"
              >
                <input
                  v-if="editingCell === `${col}${row}`"
                  ref="cellInputRef"
                  v-model="cellEditValue"
                  class="cell-input"
                  @blur="finishEdit"
                  @keydown.enter="finishEdit"
                  @keydown.tab.prevent="moveToNextCell"
                  @keydown.esc="cancelEdit"
                />
                <span v-else class="cell-content" :style="getCellContentStyle(col, row)">{{ getDisplayValue(col, row) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="fallback-statusbar">
        <span class="status-item" v-if="selectedCell">单元格: {{ selectedCell }}</span>
        <span class="status-item" v-if="selectedCellValue">值: {{ selectedCellValue }}</span>
        <span class="status-item">行: {{ fallbackRows }} | 列: {{ fallbackColumns.length }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'

const props = defineProps({
  // 初始化工作簿数据
  workbookData: {
    type: Object,
    default: null
  },
  // 是否只读模式
  readonly: {
    type: Boolean,
    default: false
  },
  // 模板字段列表（用于字段绑定提示）
  fields: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['ready', 'change', 'cellSelect'])

// DOM引用
const containerRef = ref(null)
const tableRef = ref(null)
const cellInputRef = ref(null)

// 状态
const loading = ref(true)
const useFallback = ref(true) // 默认使用备用方案
let univerInstance = null
let workbook = null

// 备用表格状态
const fallbackColumns = ref(['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'])
const fallbackRows = ref(30)
const fallbackCellData = reactive({})
const fallbackCellStyles = reactive({})

// 选择状态
const selectedCell = ref('')
const selectedRow = ref(null)
const selectedCol = ref(null)
const editingCell = ref('')
const cellEditValue = ref('')
const formulaInput = ref('')

// 计算选中单元格的值
const selectedCellValue = computed(() => {
  if (!selectedCell.value) return ''
  return fallbackCellData[selectedCell.value] || ''
})

// 初始化
onMounted(async () => {
  loading.value = true
  
  // 尝试加载Univer（由于版本兼容性，暂时使用备用方案）
  try {
    // await initUniver()
    // 注释掉Univer初始化，使用备用方案以确保稳定性
    initFallbackSpreadsheet()
  } catch (error) {
    console.warn('Univer加载失败，使用备用设计器:', error)
    initFallbackSpreadsheet()
  }
  
  loading.value = false
})

onUnmounted(() => {
  if (univerInstance) {
    try {
      univerInstance.dispose()
    } catch (e) {
      console.warn('Univer dispose error:', e)
    }
    univerInstance = null
    workbook = null
  }
})

// 初始化备用表格
const initFallbackSpreadsheet = () => {
  useFallback.value = true
  emit('ready', { fallback: true })
}

// ==================== 单元格操作 ====================

// 获取单元格值
const getCellValue = (col, row) => {
  return fallbackCellData[`${col}${row}`] || ''
}

// 获取显示值（处理公式）
const getDisplayValue = (col, row) => {
  const value = fallbackCellData[`${col}${row}`] || ''
  // 如果是模板变量，显示变量名
  if (value.startsWith('${') && value.endsWith('}')) {
    return value
  }
  return value
}

// 检查是否是公式
const isFormula = (col, row) => {
  const value = fallbackCellData[`${col}${row}`] || ''
  return value.startsWith('=') || (value.startsWith('${') && value.endsWith('}'))
}

// 获取单元格样式（背景色等）
const getCellStyle = (col, row) => {
  const style = fallbackCellStyles[`${col}${row}`] || {}
  const result = {}

  if (style.textAlign) result.textAlign = style.textAlign
  if (style.backgroundColor) result.backgroundColor = style.backgroundColor

  return result
}

// 获取单元格内容样式（字体相关）
const getCellContentStyle = (col, row) => {
  const style = fallbackCellStyles[`${col}${row}`] || {}
  const result = {}

  if (style.fontWeight) result.fontWeight = style.fontWeight
  if (style.fontStyle) result.fontStyle = style.fontStyle
  if (style.textDecoration) result.textDecoration = style.textDecoration
  if (style.fontSize) result.fontSize = `${style.fontSize}px`
  if (style.color) result.color = style.color

  return result
}

// 检查是否选中
const isSelected = (col, row) => {
  return selectedCell.value === `${col}${row}`
}

// 选择单元格
const selectCell = (col, row) => {
  selectedCell.value = `${col}${row}`
  selectedRow.value = row
  selectedCol.value = col
  formulaInput.value = fallbackCellData[`${col}${row}`] || ''
  emit('cellSelect', { col, row, value: fallbackCellData[`${col}${row}`] })
}

// 选择整行
const selectRow = (row) => {
  selectedRow.value = row
  selectedCol.value = null
  selectedCell.value = `A${row}`
}

// 选择整列
const selectColumn = (col) => {
  selectedCol.value = col
  selectedRow.value = null
  selectedCell.value = `${col}1`
}

// 编辑单元格
const editCell = (col, row) => {
  if (props.readonly) return
  editingCell.value = `${col}${row}`
  cellEditValue.value = fallbackCellData[`${col}${row}`] || ''
  nextTick(() => {
    if (cellInputRef.value) {
      const input = Array.isArray(cellInputRef.value) ? cellInputRef.value[0] : cellInputRef.value
      input?.focus()
      input?.select()
    }
  })
}

// 完成编辑
const finishEdit = () => {
  if (editingCell.value) {
    fallbackCellData[editingCell.value] = cellEditValue.value
    emit('change', { cell: editingCell.value, value: cellEditValue.value })
  }
  editingCell.value = ''
  cellEditValue.value = ''
}

// 取消编辑
const cancelEdit = () => {
  editingCell.value = ''
  cellEditValue.value = ''
}

// 移动到下一个单元格
const moveToNextCell = () => {
  finishEdit()
  if (selectedCell.value) {
    const col = selectedCell.value.charAt(0)
    const row = parseInt(selectedCell.value.slice(1))
    const colIndex = fallbackColumns.value.indexOf(col)
    if (colIndex < fallbackColumns.value.length - 1) {
      selectCell(fallbackColumns.value[colIndex + 1], row)
    } else {
      selectCell('A', row + 1)
    }
  }
}

// 应用公式栏输入
const applyFormula = () => {
  if (selectedCell.value) {
    fallbackCellData[selectedCell.value] = formulaInput.value
    emit('change', { cell: selectedCell.value, value: formulaInput.value })
  }
}

// ==================== 工具栏操作 ====================

// 插入行
const handleInsertRow = () => {
  fallbackRows.value++
}

// 插入列
const handleInsertCol = () => {
  const lastCol = fallbackColumns.value[fallbackColumns.value.length - 1]
  const nextCol = String.fromCharCode(lastCol.charCodeAt(0) + 1)
  if (nextCol <= 'Z') {
    fallbackColumns.value.push(nextCol)
  }
}

// 合并单元格（简化实现）
const handleMergeCells = () => {
  // 提示用户
  alert('合并单元格功能：选择多个单元格后点击合并（完整功能待实现）')
}

// 加粗
const handleBold = () => {
  if (selectedCell.value) {
    const currentStyle = fallbackCellStyles[selectedCell.value] || {}
    fallbackCellStyles[selectedCell.value] = {
      ...currentStyle,
      fontWeight: currentStyle.fontWeight === 'bold' ? 'normal' : 'bold'
    }
  }
}

// 处理拖放
const buildComponentPlaceholder = (componentType, componentName) => {
  const name = componentName ? componentName.trim() : ''
  const label = name ? `${componentType}:${name}` : componentType
  return `[[component:${label}]]`
}

const handleDrop = (event) => {
  event.preventDefault()
  const fieldName = event.dataTransfer.getData('field-name')
  if (fieldName && selectedCell.value) {
    fallbackCellData[selectedCell.value] = `\${${fieldName}}`
    emit('change', { cell: selectedCell.value, value: `\${${fieldName}}` })
    return
  }

  const componentType = event.dataTransfer.getData('component-type')
  const componentName = event.dataTransfer.getData('component-name')
  if (componentType && selectedCell.value) {
    const placeholder = buildComponentPlaceholder(componentType, componentName)
    fallbackCellData[selectedCell.value] = placeholder
    emit('change', { cell: selectedCell.value, value: placeholder })
  }
}

// ==================== 导出方法 ====================

// 插入字段占位符
const insertFieldPlaceholder = (fieldName) => {
  if (selectedCell.value) {
    fallbackCellData[selectedCell.value] = `\${${fieldName}}`
    formulaInput.value = `\${${fieldName}}`
    emit('change', { cell: selectedCell.value, value: `\${${fieldName}}` })
    return true
  }
  return false
}

const insertComponentPlaceholder = (componentType, componentName) => {
  if (!selectedCell.value) return false
  const placeholder = buildComponentPlaceholder(componentType, componentName)
  fallbackCellData[selectedCell.value] = placeholder
  formulaInput.value = placeholder
  emit('change', { cell: selectedCell.value, value: placeholder })
  return true
}

// 导出为JSON
const exportToJson = () => {
  return {
    columns: fallbackColumns.value,
    rows: fallbackRows.value,
    cellData: { ...fallbackCellData },
    cellStyles: { ...fallbackCellStyles },
    exportTime: new Date().toISOString()
  }
}

// 从JSON导入
const importFromJson = (jsonData) => {
  if (!jsonData) return false
  
  try {
    if (jsonData.columns) fallbackColumns.value = jsonData.columns
    if (jsonData.rows) fallbackRows.value = jsonData.rows
    if (jsonData.cellData) Object.assign(fallbackCellData, jsonData.cellData)
    if (jsonData.cellStyles) Object.assign(fallbackCellStyles, jsonData.cellStyles)
    return true
  } catch (error) {
    console.error('导入失败:', error)
    return false
  }
}

// 获取工作簿数据
const getWorkbookData = () => {
  return exportToJson()
}

// 设置单元格值
const setCellValue = (cellRef, value) => {
  fallbackCellData[cellRef] = value
}

// 暴露方法
defineExpose({
  getWorkbookData,
  setCellValue,
  insertFieldPlaceholder,
  insertComponentPlaceholder,
  exportToJson,
  importFromJson,
  getUniver: () => univerInstance,
  getWorkbook: () => workbook
})

// 监听工作簿数据变化
watch(() => props.workbookData, (newData) => {
  if (newData) {
    importFromJson(newData)
  }
}, { deep: true })
</script>

<style lang="scss" scoped>
.univer-spreadsheet-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: #fff;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f0f0f0;
    border-top-color: #409eff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  
  p {
    margin-top: 16px;
    color: #606266;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 备用表格样式
.fallback-spreadsheet {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.fallback-toolbar {
  height: 40px;
  background: linear-gradient(180deg, #f5f7fa, #eef1f6);
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  
  .toolbar-title {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }
  
  .toolbar-actions {
    display: flex;
    gap: 4px;
  }
  
  .toolbar-btn {
    padding: 4px 8px;
    font-size: 12px;
    border: 1px solid #dcdfe6;
    border-radius: 3px;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #ecf5ff;
      border-color: #409eff;
      color: #409eff;
    }
  }
}

.formula-bar {
  height: 28px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 8px;
  
  .cell-ref {
    width: 50px;
    font-size: 12px;
    font-weight: 500;
    color: #606266;
    text-align: center;
    border-right: 1px solid #e4e7ed;
    padding-right: 8px;
    margin-right: 8px;
  }
  
  .formula-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 12px;
    font-family: monospace;
    background: transparent;
  }
}

.fallback-grid {
  flex: 1;
  overflow: auto;
  
  table {
    width: max-content;
    min-width: 100%;
    border-collapse: collapse;
    font-size: 13px;
    table-layout: fixed;
  }
  
  th, td {
    border: 1px solid #e4e7ed;
    min-width: 100px;
    height: 24px;
    padding: 0;
    position: relative;
  }
  
  th {
    background: #f5f7fa;
    font-weight: 500;
    color: #606266;
    
    &.selected {
      background: #ecf5ff;
      color: #409eff;
    }
  }
  
  .row-header, .col-header {
    width: 40px;
    min-width: 40px;
    text-align: center;
    cursor: pointer;
    user-select: none;
    
    &.selected {
      background: #ecf5ff;
      color: #409eff;
    }
  }
  
  .cell {
    cursor: cell;
    background: #fff;

    &:hover {
      background: #f5f7fa;
    }

    &.selected {
      background: #ecf5ff;
      outline: 2px solid #409eff;
      outline-offset: -1px;
      z-index: 1;
    }

    &.is-formula {
      .cell-content {
        font-style: italic;
      }
    }
  }

  .cell-content {
    display: block;
    padding: 2px 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 20px;
    color: #303133;
  }
  
  .cell-input {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    border: 2px solid #409eff;
    outline: none;
    padding: 2px 6px;
    font-size: 13px;
    font-family: inherit;
    box-sizing: border-box;
    z-index: 2;
  }
}

.fallback-statusbar {
  height: 24px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 12px;
  font-size: 12px;
  color: #909399;
  
  .status-item {
    margin-right: 20px;
  }
}
</style>
