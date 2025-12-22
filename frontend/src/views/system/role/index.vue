<template>
  <div class="role-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">角色管理</h1>
        <p class="page-desc">管理系统角色及权限配置</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索角色名称"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建角色
        </el-button>
      </div>
    </div>

    <!-- 角色列表 -->
    <div class="role-grid">
      <div
        v-for="role in filteredRoleList"
        :key="role.id"
        class="role-card"
        :class="{ disabled: role.status === 0 }"
      >
        <div class="card-header">
          <div class="role-icon" :style="{ background: role.color }">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-badges">
            <el-tag v-if="role.isSystem" size="small" type="warning">系统角色</el-tag>
            <el-tag v-if="role.status === 0" size="small" type="info">已禁用</el-tag>
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
                <el-dropdown-item @click="handleViewUsers(role)">
                  <el-icon><User /></el-icon>
                  查看用户
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
          <div class="footer-left">
            <div class="user-count">
              <el-icon><User /></el-icon>
              <span>{{ role.userCount || 0 }} 位用户</span>
            </div>
            <div class="perm-count" v-if="role.permissionCount">
              <el-icon><Key /></el-icon>
              <span>{{ role.permissionCount }} 项权限</span>
            </div>
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

    <!-- 空状态 -->
    <div v-if="filteredRoleList.length === 0 && searchKeyword" class="empty-state">
      <el-empty :description="`未找到包含 '${searchKeyword}' 的角色`">
        <el-button @click="searchKeyword = ''">清除搜索</el-button>
      </el-empty>
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
          <div class="form-tip">角色标识创建后不可修改，格式: ROLE_XXX</div>
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

        <div class="form-row">
          <el-form-item label="排序" class="flex-1">
            <el-input-number
              v-model="formData.sort"
              :min="0"
              :max="100"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="状态" class="flex-1">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="角色描述"
            maxlength="200"
            show-word-limit
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
      width="600px"
      class="perm-dialog"
      destroy-on-close
    >
      <div class="perm-header">
        <div class="perm-role-info">
          <div class="role-icon-small" :style="{ background: currentRole?.color }">
            <el-icon><User /></el-icon>
          </div>
          <div class="role-info-text">
            <span class="role-name-text">{{ currentRole?.roleName }}</span>
            <span class="role-code-text">{{ currentRole?.roleCode }}</span>
          </div>
        </div>
        <div class="perm-stats">
          <span class="stat-item">
            <el-icon><Document /></el-icon>
            已选 <strong>{{ selectedCount }}</strong> 个模板
          </span>
        </div>
      </div>

      <div class="perm-toolbar">
        <el-input
          v-model="permSearchKeyword"
          placeholder="搜索模板名称"
          prefix-icon="Search"
          clearable
          class="perm-search"
        />
        <div class="perm-actions">
          <el-button size="small" @click="handleSelectAll">全选</el-button>
          <el-button size="small" @click="handleSelectNone">清空</el-button>
          <el-button size="small" @click="handleExpandAll">
            {{ isAllExpanded ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>

      <div class="perm-tree-wrapper">
        <el-tree
          ref="permTreeRef"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :props="{ label: 'label', children: 'children' }"
          :default-expanded-keys="expandedKeys"
          :filter-node-method="filterNode"
          :check-strictly="false"
          highlight-current
          @check="handleTreeCheck"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <el-icon v-if="data.type === 'category'" class="node-icon category">
                <Folder />
              </el-icon>
              <el-icon v-else class="node-icon template">
                <Document />
              </el-icon>
              <span class="node-label">{{ node.label }}</span>
              <el-tag
                v-if="data.type === 'template'"
                size="small"
                type="info"
                class="node-tag"
              >
                模板
              </el-tag>
            </span>
          </template>
        </el-tree>

        <div v-if="permissionTree.length === 0" class="perm-empty">
          <el-empty description="暂无可分配的权限" :image-size="80" />
        </div>
      </div>

      <div class="perm-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>勾选模板后，该角色下的用户将拥有对应模板的查看、生成、下载、编辑权限</span>
      </div>

      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="handlePermSubmit">
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看用户弹窗 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="`${currentRole?.roleName} - 用户列表`"
      width="600px"
    >
      <el-table :data="roleUsers" max-height="400" v-if="roleUsers.length > 0">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="user-empty">
        <el-empty description="该角色下暂无用户" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, More, Edit, Key, Delete, Plus, Folder, Document, InfoFilled } from '@element-plus/icons-vue'
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

// 搜索
const searchKeyword = ref('')

// 数据
const roleList = ref([])

// 筛选后的角色列表
const filteredRoleList = computed(() => {
  if (!searchKeyword.value) return roleList.value
  const keyword = searchKeyword.value.toLowerCase()
  return roleList.value.filter(role =>
    role.roleName.toLowerCase().includes(keyword) ||
    role.roleCode.toLowerCase().includes(keyword)
  )
})

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
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度为 2-20 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '格式: ROLE_XXX (大写字母和下划线)', trigger: 'blur' }
  ]
}

// 权限配置
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permTreeRef = ref(null)
const currentRole = ref(null)
const checkedPermissions = ref([])
const permissionTree = ref([])
const permSearchKeyword = ref('')
const expandedKeys = ref([])
const isAllExpanded = ref(true)

// 过滤节点方法
const filterNode = (value, data) => {
  if (!value) return true
  return data.label.toLowerCase().includes(value.toLowerCase())
}

// 已选数量
const selectedCount = computed(() => {
  if (!permTreeRef.value) return checkedPermissions.value.filter(id => id > 0).length
  const checked = permTreeRef.value.getCheckedKeys()
  // 只计算模板节点（正数ID）
  return checked.filter(id => id > 0).length
})

// 用户列表弹窗
const userDialogVisible = ref(false)
const roleUsers = ref([])

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
    // 默认展开所有节点
    expandedKeys.value = getAllNodeIds(permissionTree.value)
  } catch (error) {
    console.error('加载权限树失败:', error)
    permissionTree.value = []
  }
}

// 获取所有节点ID
const getAllNodeIds = (nodes) => {
  let ids = []
  nodes.forEach(node => {
    ids.push(node.id)
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getAllNodeIds(node.children))
    }
  })
  return ids
}

// 新建
const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (role) => {
  Object.assign(formData, {
    id: role.id,
    roleName: role.roleName,
    roleCode: role.roleCode,
    color: role.color || '#007AFF',
    sort: role.sort || 0,
    status: role.status,
    description: role.description || ''
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (role) => {
  if (role.userCount > 0) {
    ElMessage.warning(`角色「${role.roleName}」下有 ${role.userCount} 位用户，请先移除用户后再删除`)
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除角色「${role.roleName}」吗？删除后无法恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
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
  permSearchKeyword.value = ''

  try {
    const res = await getRolePermissions(role.id)
    checkedPermissions.value = res.data || []
    permDialogVisible.value = true

    // 等待弹窗渲染后设置选中状态
    await nextTick()
    if (permTreeRef.value) {
      permTreeRef.value.setCheckedKeys(checkedPermissions.value)
    }
  } catch (error) {
    console.error('加载角色权限失败:', error)
    checkedPermissions.value = []
    permDialogVisible.value = true
  }
}

// 树节点选中事件
const handleTreeCheck = () => {
  // 触发重新计算已选数量
}

// 全选
const handleSelectAll = () => {
  if (permTreeRef.value) {
    const allTemplateIds = getAllTemplateIds(permissionTree.value)
    permTreeRef.value.setCheckedKeys(allTemplateIds)
  }
}

// 获取所有模板ID（正数）
const getAllTemplateIds = (nodes) => {
  let ids = []
  nodes.forEach(node => {
    if (node.id > 0) {
      ids.push(node.id)
    }
    if (node.children && node.children.length > 0) {
      ids = ids.concat(getAllTemplateIds(node.children))
    }
  })
  return ids
}

// 清空选择
const handleSelectNone = () => {
  if (permTreeRef.value) {
    permTreeRef.value.setCheckedKeys([])
  }
}

// 展开/收起
const handleExpandAll = () => {
  if (isAllExpanded.value) {
    expandedKeys.value = []
  } else {
    expandedKeys.value = getAllNodeIds(permissionTree.value)
  }
  isAllExpanded.value = !isAllExpanded.value
}

// 保存权限
const handlePermSubmit = async () => {
  permLoading.value = true
  try {
    const checkedKeys = permTreeRef.value?.getCheckedKeys() || []
    // 只保存模板ID（正数）
    const templateIds = checkedKeys.filter(id => id > 0)
    await saveRolePermissions(currentRole.value.id, templateIds)
    ElMessage.success('权限配置已保存')
    permDialogVisible.value = false
    loadRoles() // 刷新角色列表以更新权限数量
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    permLoading.value = false
  }
}

// 查看用户
const handleViewUsers = async (role) => {
  currentRole.value = role
  // 模拟用户数据，实际应该调用 API
  roleUsers.value = []
  userDialogVisible.value = true
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

// 监听权限搜索
watch(permSearchKeyword, (val) => {
  if (permTreeRef.value) {
    permTreeRef.value.filter(val)
  }
})
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

.header-left {
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
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 200px;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-badges {
  display: flex;
  gap: 6px;
  flex: 1;
  justify-content: flex-end;
  margin-right: 8px;
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

.footer-left {
  display: flex;
  gap: 16px;
}

.user-count,
.perm-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-secondary;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-tip {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 4px;
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

// 权限配置弹窗样式
.perm-dialog {
  :deep(.el-dialog__body) {
    padding: 0 20px 20px;
  }
}

.perm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, $gray-50, $gray-100);
  border-radius: $radius-lg;
  margin-bottom: 16px;
}

.perm-role-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-icon-small {
  width: 36px;
  height: 36px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
}

.role-info-text {
  display: flex;
  flex-direction: column;

  .role-name-text {
    font-size: 15px;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }

  .role-code-text {
    font-size: 12px;
    color: $text-tertiary;
    font-family: $font-family-mono;
  }
}

.perm-stats {
  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: $text-secondary;

    strong {
      color: $primary-color;
      font-size: 16px;
    }
  }
}

.perm-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.perm-search {
  width: 200px;
}

.perm-actions {
  display: flex;
  gap: 8px;
}

.perm-tree-wrapper {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: 12px;
  max-height: 350px;
  overflow-y: auto;
  background: $bg-primary;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.node-icon {
  font-size: 16px;

  &.category {
    color: $warning-color;
  }

  &.template {
    color: $primary-color;
  }
}

.node-label {
  flex: 1;
}

.node-tag {
  margin-left: 8px;
}

.perm-empty {
  padding: 40px 0;
}

.perm-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 12px;
  background: rgba($info-color, 0.1);
  border-radius: $radius-md;
  font-size: 12px;
  color: $info-color;
}

.user-empty {
  padding: 40px 0;
}

.empty-state {
  padding: 60px 0;
}

.text-danger {
  color: $danger-color;
}
</style>
