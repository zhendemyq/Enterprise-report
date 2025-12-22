<template>
  <div class="category-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">分类管理</h1>
        <p class="page-desc">管理报表分类，支持树形结构</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建分类
        </el-button>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="filter-card">
      <div class="filter-form">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索分类名称、编码"
          prefix-icon="Search"
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />

        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>

        <el-button @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 分类列表 -->
    <div class="category-card">
      <el-table
        v-loading="loading"
        :data="categoryList"
        row-key="id"
        :tree-props="{ children: 'children' }"
        style="width: 100%"
        default-expand-all
      >
        <el-table-column label="分类名称" min-width="200">
          <template #default="{ row }">
            <div class="category-name">
              <el-icon v-if="row.icon" class="category-icon">
                <component :is="row.icon" />
              </el-icon>
              <span>{{ row.categoryName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="分类编码" prop="categoryCode" width="150" />
        
        <el-table-column label="描述" prop="description" min-width="200">
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleAddChild(row)">
                添加子分类
              </el-button>
              <el-button link type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input 
            v-model="formData.categoryCode" 
            placeholder="请输入分类编码"
            :disabled="!!formData.id"
          />
        </el-form-item>
        
        <el-form-item label="父级分类" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="categoryTreeOptions"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="请选择父级分类（可为空）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="图标" prop="icon">
          <el-select v-model="formData.icon" placeholder="选择图标" clearable>
            <el-option
              v-for="icon in iconOptions"
              :key="icon"
              :label="icon"
              :value="icon"
            >
              <div class="icon-option">
                <el-icon><component :is="icon" /></el-icon>
                <span>{{ icon }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <div class="form-row">
          <el-form-item label="排序" prop="sort" class="flex-1">
            <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
          </el-form-item>
          
          <el-form-item label="状态" prop="status" class="flex-1">
            <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

// 查询参数
const queryParams = reactive({
  keyword: '',
  status: null
})

// 数据
const loading = ref(false)
const categoryList = ref([])
const allCategories = ref([]) // 存储原始数据

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑分类' : '新建分类')
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  categoryName: '',
  categoryCode: '',
  parentId: null,
  icon: '',
  sort: 0,
  status: 1,
  description: ''
})

const formRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ],
  categoryCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[a-z_][a-z0-9_]*$/, message: '编码只能包含小写字母、数字和下划线', trigger: 'blur' }
  ]
}

// 图标选项
const iconOptions = [
  'Money', 'TrendCharts', 'DataAnalysis', 'User', 'Folder', 
  'Document', 'Files', 'PieChart', 'Histogram', 'DataLine',
  'Grid', 'List', 'Setting', 'Tools', 'Operation'
]

// 分类树选项
const categoryTreeOptions = computed(() => {
  const buildTree = (items, parentId = 0) => {
    return items
      .filter(item => item.parentId === parentId && item.id !== formData.id)
      .map(item => ({
        ...item,
        children: buildTree(items, item.id)
      }))
      .filter(item => item.children.length > 0 || !items.some(i => i.parentId === item.id))
  }
  
  return [
    { id: 0, categoryName: '顶级分类', children: buildTree(flatCategoryList.value, 0) }
  ]
})

// 扁平分类列表
const flatCategoryList = computed(() => {
  const flatten = (list) => {
    return list.reduce((acc, item) => {
      acc.push(item)
      if (item.children?.length) {
        acc.push(...flatten(item.children))
      }
      return acc
    }, [])
  }
  return flatten(categoryList.value)
})

// 初始化
onMounted(() => {
  loadCategories()
})

// 加载分类
const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    allCategories.value = res.data || []
    handleSearch()
  } catch (error) {
    console.error('加载分类失败:', error)
    allCategories.value = []
    categoryList.value = []
  } finally {
    loading.value = false
  }
}

// 搜索 - 递归过滤树形数据
const filterTree = (tree, keyword, status) => {
  return tree.filter(node => {
    const matchKeyword = !keyword ||
      node.categoryName?.toLowerCase().includes(keyword.toLowerCase()) ||
      node.categoryCode?.toLowerCase().includes(keyword.toLowerCase())
    const matchStatus = status === null || status === '' || node.status === status

    if (node.children?.length) {
      node.children = filterTree(node.children, keyword, status)
      return matchKeyword && matchStatus || node.children.length > 0
    }
    return matchKeyword && matchStatus
  }).map(node => ({ ...node }))
}

const handleSearch = () => {
  if (!queryParams.keyword && (queryParams.status === null || queryParams.status === '')) {
    categoryList.value = JSON.parse(JSON.stringify(allCategories.value))
  } else {
    categoryList.value = filterTree(
      JSON.parse(JSON.stringify(allCategories.value)),
      queryParams.keyword,
      queryParams.status
    )
  }
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = null
  handleSearch()
}

// 新建
const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 添加子分类
const handleAddChild = (row) => {
  resetForm()
  formData.parentId = row.id
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    categoryName: row.categoryName,
    categoryCode: row.categoryCode,
    parentId: row.parentId,
    icon: row.icon,
    sort: row.sort,
    status: row.status,
    description: row.description
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  if (row.children?.length) {
    ElMessage.warning('请先删除子分类')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${row.categoryName}」吗？`,
      '警告',
      { type: 'error', confirmButtonText: '删除' }
    )
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 状态变更
const handleStatusChange = (row) => {
  ElMessage.success(`分类「${row.categoryName}」已${row.status ? '启用' : '禁用'}`)
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateCategory(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createCategory(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadCategories()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.categoryName = ''
  formData.categoryCode = ''
  formData.parentId = 0
  formData.icon = ''
  formData.sort = 0
  formData.status = 1
  formData.description = ''
}
</script>

<style lang="scss" scoped>
.category-container {
  max-width: 1200px;
  margin: 0 auto;
}

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

// 筛选区
.filter-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px 24px;
  margin-bottom: 24px;
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

.category-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  font-size: 18px;
  color: $primary-color;
}

.table-actions {
  display: flex;
  gap: 4px;
}

.form-row {
  display: flex;
  gap: 20px;
  
  .el-form-item {
    margin-bottom: 18px;
  }
}

.icon-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
