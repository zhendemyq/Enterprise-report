<template>
  <div class="datasource-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">数据源管理</h1>
        <p class="page-desc">管理报表数据源，支持多种数据库类型</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建数据源
        </el-button>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="filter-card">
      <div class="filter-form">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索数据源名称、编码"
          prefix-icon="Search"
          clearable
          class="search-input"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />

        <el-select
          v-model="queryParams.datasourceType"
          placeholder="数据源类型"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="MySQL" :value="1" />
          <el-option label="PostgreSQL" :value="2" />
          <el-option label="Oracle" :value="3" />
          <el-option label="SQL Server" :value="4" />
          <el-option label="API接口" :value="5" />
        </el-select>

        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="正常" :value="1" />
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

    <!-- 数据源卡片网格 -->
    <div class="datasource-grid">
      <div
        v-for="ds in datasourceList"
        :key="ds.id"
        class="datasource-card"
      >
        <div class="card-header">
          <div class="ds-icon" :class="getTypeClass(ds.datasourceType)">
            <el-icon :size="24"><component :is="getTypeIcon(ds.datasourceType)" /></el-icon>
          </div>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, ds)">
            <el-button class="more-btn" text circle>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon><Edit /></el-icon>编辑
                </el-dropdown-item>
                <el-dropdown-item command="test">
                  <el-icon><Connection /></el-icon>测试连接
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon><Delete /></el-icon>删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div class="card-body">
          <h3 class="ds-name">{{ ds.datasourceName }}</h3>
          <p class="ds-code">{{ ds.datasourceCode }}</p>
          <p class="ds-desc">{{ ds.description || '暂无描述' }}</p>
        </div>
        
        <div class="card-info">
          <div class="info-item">
            <span class="info-label">类型</span>
            <span class="info-value">{{ getTypeName(ds.datasourceType) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">主机</span>
            <span class="info-value">{{ ds.host }}:{{ ds.port }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">数据库</span>
            <span class="info-value">{{ ds.databaseName }}</span>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="status-badge" :class="{ success: ds.testResult === 1, error: ds.testResult === 0 }">
            <el-icon v-if="ds.testResult === 1"><CircleCheckFilled /></el-icon>
            <el-icon v-else-if="ds.testResult === 0"><CircleCloseFilled /></el-icon>
            <el-icon v-else><QuestionFilled /></el-icon>
            <span>{{ getTestResultText(ds.testResult) }}</span>
          </div>
          <el-button size="small" @click="handleTest(ds)" :loading="ds.testing">
            测试连接
          </el-button>
        </div>
      </div>
      
      <!-- 添加卡片 -->
      <div class="datasource-card add-card" @click="handleCreate">
        <el-icon :size="32"><Plus /></el-icon>
        <span>添加数据源</span>
      </div>
    </div>
    
    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="top"
      >
        <el-form-item label="数据源名称" prop="datasourceName">
          <el-input v-model="formData.datasourceName" placeholder="请输入数据源名称" />
        </el-form-item>
        
        <el-form-item label="数据源编码" prop="datasourceCode">
          <el-input 
            v-model="formData.datasourceCode" 
            placeholder="请输入数据源编码"
            :disabled="!!formData.id"
          />
        </el-form-item>
        
        <el-form-item label="数据源类型" prop="datasourceType">
          <el-select v-model="formData.datasourceType" placeholder="选择数据源类型" style="width: 100%">
            <el-option label="MySQL" :value="1" />
            <el-option label="PostgreSQL" :value="2" />
            <el-option label="Oracle" :value="3" />
            <el-option label="SQL Server" :value="4" />
            <el-option label="API接口" :value="5" />
          </el-select>
        </el-form-item>
        
        <template v-if="formData.datasourceType !== 5">
          <div class="form-row">
            <el-form-item label="主机地址" prop="host" class="flex-1">
              <el-input v-model="formData.host" placeholder="如: localhost" />
            </el-form-item>
            <el-form-item label="端口" prop="port" style="width: 120px">
              <el-input-number v-model="formData.port" :min="1" :max="65535" />
            </el-form-item>
          </div>
          
          <el-form-item label="数据库名" prop="databaseName">
            <el-input v-model="formData.databaseName" placeholder="请输入数据库名" />
          </el-form-item>
          
          <div class="form-row">
            <el-form-item label="用户名" prop="username" class="flex-1">
              <el-input v-model="formData.username" placeholder="数据库用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password" class="flex-1">
              <el-input v-model="formData.password" type="password" placeholder="数据库密码" show-password />
            </el-form-item>
          </div>
        </template>
        
        <template v-else>
          <el-form-item label="API地址" prop="apiUrl">
            <el-input v-model="formData.apiUrl" placeholder="https://api.example.com/data" />
          </el-form-item>
        </template>
        
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
        <el-button @click="handleTestInDialog" :loading="testLoading">
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
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
import { Search, Refresh, Plus, More, Edit, Delete, Connection } from '@element-plus/icons-vue'
import {
  listDatasources,
  createDatasource,
  updateDatasource,
  deleteDatasource,
  testConnection
} from '@/api/datasource'

// 查询参数
const queryParams = reactive({
  keyword: '',
  datasourceType: null,
  status: null
})

// 数据
const datasourceList = ref([])
const allDatasources = ref([]) // 存储原始数据

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑数据源' : '新建数据源')
const submitLoading = ref(false)
const testLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  datasourceName: '',
  datasourceCode: '',
  datasourceType: 1,
  host: '',
  port: 3306,
  databaseName: '',
  username: '',
  password: '',
  apiUrl: '',
  description: ''
})

const formRules = {
  datasourceName: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' }
  ],
  datasourceCode: [
    { required: true, message: '请输入数据源编码', trigger: 'blur' }
  ],
  datasourceType: [
    { required: true, message: '请选择数据源类型', trigger: 'change' }
  ],
  host: [
    { required: true, message: '请输入主机地址', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口', trigger: 'blur' }
  ],
  databaseName: [
    { required: true, message: '请输入数据库名', trigger: 'blur' }
  ],
  apiUrl: [
    { required: true, message: '请输入API地址', trigger: 'blur' }
  ]
}

// 初始化
onMounted(() => {
  loadDatasources()
})

// 加载数据源
const loadDatasources = async () => {
  try {
    const res = await listDatasources()
    allDatasources.value = res.data || []
    handleSearch()
  } catch (error) {
    console.error('加载数据源失败:', error)
    allDatasources.value = []
    datasourceList.value = []
  }
}

// 搜索
const handleSearch = () => {
  let list = [...allDatasources.value]

  // 关键词过滤
  if (queryParams.keyword) {
    const keyword = queryParams.keyword.toLowerCase()
    list = list.filter(ds =>
      ds.datasourceName?.toLowerCase().includes(keyword) ||
      ds.datasourceCode?.toLowerCase().includes(keyword)
    )
  }

  // 类型过滤
  if (queryParams.datasourceType !== null && queryParams.datasourceType !== '') {
    list = list.filter(ds => ds.datasourceType === queryParams.datasourceType)
  }

  // 状态过滤
  if (queryParams.status !== null && queryParams.status !== '') {
    list = list.filter(ds => ds.status === queryParams.status)
  }

  datasourceList.value = list
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.datasourceType = null
  queryParams.status = null
  handleSearch()
}

// 新建
const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (ds) => {
  Object.assign(formData, ds)
  dialogVisible.value = true
}

// 测试连接
const handleTest = async (ds) => {
  ds.testing = true
  try {
    await testConnection(ds.id)
    ds.testResult = 1
    ElMessage.success('连接成功')
  } catch (error) {
    ds.testResult = 0
    ElMessage.error('连接失败')
  } finally {
    ds.testing = false
  }
}

// 弹窗内测试
const handleTestInDialog = async () => {
  testLoading.value = true
  try {
    if (!formData.id) {
      ElMessage.warning('?????????????')
      return
    }
    await testConnection(formData.id)
    ElMessage.success('????')
  } catch (error) {
    ElMessage.error('????')
  } finally {
    testLoading.value = false
  }
}

// 删除
const handleDelete = async (ds) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除数据源「${ds.datasourceName}」吗？`,
      '警告',
      { type: 'error', confirmButtonText: '删除' }
    )
    await deleteDatasource(ds.id)
    ElMessage.success('删除成功')
    loadDatasources()
  } catch (error) {
    // 取消
  }
}

// 处理命令
const handleCommand = (cmd, ds) => {
  switch (cmd) {
    case 'edit':
      handleEdit(ds)
      break
    case 'test':
      handleTest(ds)
      break
    case 'delete':
      handleDelete(ds)
      break
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateDatasource(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createDatasource(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadDatasources()
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
  Object.assign(formData, {
    id: null,
    datasourceName: '',
    datasourceCode: '',
    datasourceType: 1,
    host: '',
    port: 3306,
    databaseName: '',
    username: '',
    password: '',
    apiUrl: '',
    description: ''
  })
}

// 工具函数
const getTypeClass = (type) => {
  const map = { 1: 'mysql', 2: 'postgresql', 3: 'oracle', 4: 'sqlserver', 5: 'api' }
  return map[type] || 'mysql'
}

const getTypeIcon = (type) => {
  return type === 5 ? 'Link' : 'Coin'
}

const getTypeName = (type) => {
  const map = { 1: 'MySQL', 2: 'PostgreSQL', 3: 'Oracle', 4: 'SQL Server', 5: 'API接口' }
  return map[type] || '未知'
}

const getTestResultText = (result) => {
  if (result === 1) return '连接正常'
  if (result === 0) return '连接失败'
  return '未测试'
}
</script>

<style lang="scss" scoped>
.datasource-container {
  max-width: 1400px;
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

// 数据源卡片网格
.datasource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.datasource-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 24px;
  box-shadow: $shadow-sm;
  transition: all $transition-normal;
  
  &:hover {
    box-shadow: $shadow-md;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.ds-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.mysql {
    background: rgba(#00758F, 0.1);
    color: #00758F;
  }
  
  &.postgresql {
    background: rgba(#336791, 0.1);
    color: #336791;
  }
  
  &.oracle {
    background: rgba(#F80000, 0.1);
    color: #F80000;
  }
  
  &.sqlserver {
    background: rgba(#CC2927, 0.1);
    color: #CC2927;
  }
  
  &.api {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }
}

.more-btn {
  color: $text-secondary;
}

.card-body {
  margin-bottom: 16px;
}

.ds-name {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 4px;
}

.ds-code {
  font-size: 12px;
  color: $text-tertiary;
  font-family: $font-family-mono;
  margin-bottom: 12px;
}

.ds-desc {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.5;
}

.card-info {
  background: $gray-50;
  border-radius: $radius-md;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  
  &:not(:last-child) {
    border-bottom: 1px dashed $border-color;
    padding-bottom: 8px;
    margin-bottom: 8px;
  }
}

.info-label {
  font-size: 12px;
  color: $text-secondary;
}

.info-value {
  font-size: 13px;
  color: $text-primary;
  font-family: $font-family-mono;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: $radius-sm;
  background: $gray-100;
  color: $text-secondary;
  
  &.success {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &.error {
    background: rgba($danger-color, 0.1);
    color: $danger-color;
  }
}

// 添加卡片
.add-card {
  border: 2px dashed $border-color;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    border-color: $primary-color;
    background: rgba($primary-color, 0.02);
  }
  
  .el-icon {
    color: $text-tertiary;
    margin-bottom: 12px;
  }
  
  span {
    font-size: 14px;
    color: $text-secondary;
  }
}

.form-row {
  display: flex;
  gap: 16px;
  
  .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
