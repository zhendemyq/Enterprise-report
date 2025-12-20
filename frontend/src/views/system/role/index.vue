<template>
  <div class="role-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">角色管理</h1>
        <p class="page-desc">管理系统角色及权限配置</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建角色
        </el-button>
      </div>
    </div>
    
    <!-- 角色列表 -->
    <div class="role-grid">
      <div
        v-for="role in roleList"
        :key="role.id"
        class="role-card"
        :class="{ disabled: role.status === 0 }"
      >
        <div class="card-header">
          <div class="role-icon" :style="{ background: role.color }">
            <el-icon><User /></el-icon>
          </div>
          <el-dropdown trigger="click">
            <el-button text class="more-btn">
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleEdit(role)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-dropdown-item>
                <el-dropdown-item @click="handlePermission(role)">
                  <el-icon><Key /></el-icon>
                  权限配置
                </el-dropdown-item>
                <el-dropdown-item 
                  v-if="!role.isSystem"
                  divided 
                  @click="handleDelete(role)"
                >
                  <el-icon><Delete /></el-icon>
                  <span class="text-danger">删除</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div class="card-body">
          <h3 class="role-name">{{ role.roleName }}</h3>
          <p class="role-code">{{ role.roleCode }}</p>
          <p class="role-desc">{{ role.description || '暂无描述' }}</p>
        </div>
        
        <div class="card-footer">
          <div class="user-count">
            <el-icon><User /></el-icon>
            <span>{{ role.userCount }} 位用户</span>
          </div>
          <el-switch
            v-model="role.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="role.isSystem"
            @change="handleStatusChange(role)"
          />
        </div>
      </div>
      
      <!-- 新建角色卡片 -->
      <div class="role-card add-card" @click="handleCreate">
        <el-icon class="add-icon"><Plus /></el-icon>
        <span>新建角色</span>
      </div>
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
        label-width="80px"
        label-position="top"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        
        <el-form-item label="角色标识" prop="roleCode">
          <el-input 
            v-model="formData.roleCode" 
            placeholder="如: ROLE_ADMIN"
            :disabled="!!formData.id"
          />
        </el-form-item>
        
        <el-form-item label="角色颜色">
          <div class="color-picker">
            <div
              v-for="color in colorOptions"
              :key="color"
              class="color-item"
              :class="{ active: formData.color === color }"
              :style="{ background: color }"
              @click="formData.color = color"
            />
          </div>
        </el-form-item>
        
        <el-form-item label="排序">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="100"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="角色描述"
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
    
    <!-- 权限配置弹窗 -->
    <el-dialog
      v-model="permDialogVisible"
      title="权限配置"
      width="500px"
    >
      <div class="perm-header">
        <span class="role-label">角色：</span>
        <span class="role-value">{{ currentRole?.roleName }}</span>
      </div>
      
      <div class="perm-tree">
        <el-tree
          ref="permTreeRef"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :props="{ label: 'label', children: 'children' }"
          :default-checked-keys="checkedPermissions"
        />
      </div>
      
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="handlePermSubmit">
          保存配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, More, Edit, Key, Delete, Plus } from '@element-plus/icons-vue'
import { 
  listRoles, 
  createRole, 
  updateRole, 
  deleteRole, 
  toggleRoleStatus,
  getRolePermissions,
  saveRolePermissions,
  getPermissionTree 
} from '@/api/role'

// 颜色选项
const colorOptions = [
  '#007AFF', '#34C759', '#FF9500', '#FF3B30', '#5856D6',
  '#AF52DE', '#FF2D55', '#A2845E', '#00C7BE', '#32ADE6'
]

// 数据
const roleList = ref([])

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑角色' : '新建角色')
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  color: '#007AFF',
  sort: 0,
  status: 1,
  description: ''
})

const formRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '格式: ROLE_XXX', trigger: 'blur' }
  ]
}

// 权限配置
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permTreeRef = ref(null)
const currentRole = ref(null)
const checkedPermissions = ref([])
const permissionTree = ref([])

// 初始化
onMounted(() => {
  loadRoles()
  loadPermissionTree()
})

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await listRoles()
    roleList.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    roleList.value = []
  }
}

// 加载权限树
const loadPermissionTree = async () => {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res.data || []
  } catch (error) {
    console.error('加载权限树失败:', error)
    permissionTree.value = []
  }
}

// 新建
const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (role) => {
  Object.assign(formData, role)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (role) => {
  if (role.userCount > 0) {
    ElMessage.warning(`角色「${role.roleName}」下有 ${role.userCount} 位用户，请先移除用户`)
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除角色「${role.roleName}」吗？`,
      '警告',
      { type: 'error', confirmButtonText: '删除' }
    )
    await deleteRole(role.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 权限配置
const handlePermission = async (role) => {
  currentRole.value = role
  try {
    const res = await getRolePermissions(role.id)
    checkedPermissions.value = res.data || []
  } catch (error) {
    console.error('加载角色权限失败:', error)
    checkedPermissions.value = []
  }
  permDialogVisible.value = true
}

const handlePermSubmit = async () => {
  permLoading.value = true
  try {
    const checkedKeys = permTreeRef.value?.getCheckedKeys() || []
    await saveRolePermissions(currentRole.value.id, checkedKeys)
    ElMessage.success('权限配置已保存')
    permDialogVisible.value = false
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    permLoading.value = false
  }
}

// 状态变更
const handleStatusChange = async (role) => {
  try {
    await toggleRoleStatus(role.id, role.status)
    ElMessage.success(`角色「${role.roleName}」已${role.status ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('状态变更失败:', error)
    // 回滚状态
    role.status = role.status ? 0 : 1
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
          await updateRole(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createRole(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadRoles()
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
  Object.assign(formData, {
    id: null,
    roleName: '',
    roleCode: '',
    color: '#007AFF',
    sort: 0,
    status: 1,
    description: ''
  })
}
</script>

<style lang="scss" scoped>
.role-container {
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

.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.role-card {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
  transition: all $transition-normal;
  
  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
  
  &.disabled {
    opacity: 0.6;
  }
  
  &.add-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 2px dashed $border-color;
    background: transparent;
    cursor: pointer;
    min-height: 220px;
    
    .add-icon {
      font-size: 36px;
      color: $text-tertiary;
      margin-bottom: 12px;
    }
    
    span {
      color: $text-secondary;
    }
    
    &:hover {
      border-color: $primary-color;
      background: rgba($primary-color, 0.02);
      
      .add-icon,
      span {
        color: $primary-color;
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.role-icon {
  width: 44px;
  height: 44px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.more-btn {
  color: $text-tertiary;
  
  &:hover {
    color: $text-primary;
  }
}

.card-body {
  margin-bottom: 16px;
}

.role-name {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 4px;
}

.role-code {
  font-family: $font-family-mono;
  font-size: 12px;
  color: $text-tertiary;
  margin-bottom: 8px;
}

.role-desc {
  font-size: 13px;
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

.user-count {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: $text-secondary;
}

.color-picker {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.color-item {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  transition: transform $transition-fast;
  
  &:hover {
    transform: scale(1.1);
  }
  
  &.active {
    box-shadow: 0 0 0 3px rgba($primary-color, 0.3);
    transform: scale(1.1);
  }
}

.perm-header {
  margin-bottom: 16px;
  padding: 12px 16px;
  background: $gray-100;
  border-radius: $radius-md;
}

.role-label {
  color: $text-secondary;
}

.role-value {
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.perm-tree {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: 12px;
}

.text-danger {
  color: $danger-color;
}
</style>
