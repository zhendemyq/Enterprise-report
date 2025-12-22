<template>
  <div class="role-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">角色管理</h1>
        <p class="page-desc">基于 RBAC 模型的用户-角色-权限访问控制</p>
      </div>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索角色名称或标识"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-select v-model="viewMode" class="view-mode-select">
          <el-option label="分层视图" value="hierarchy" />
          <el-option label="网格视图" value="grid" />
        </el-select>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建角色
        </el-button>
      </div>
    </div>

    <!-- RBAC 架构概览 -->
    <div class="rbac-overview">
      <div class="rbac-model">
        <div class="model-layer user-layer">
          <div class="layer-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="layer-info">
            <span class="layer-title">用户 (User)</span>
            <span class="layer-count">{{ totalUserCount }} 位</span>
          </div>
        </div>
        <div class="model-arrow">
          <el-icon><Right /></el-icon>
          <span>关联</span>
        </div>
        <div class="model-layer role-layer">
          <div class="layer-icon">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="layer-info">
            <span class="layer-title">角色 (Role)</span>
            <span class="layer-count">{{ roleList.length }} 个</span>
          </div>
        </div>
        <div class="model-arrow">
          <el-icon><Right /></el-icon>
          <span>拥有</span>
        </div>
        <div class="model-layer permission-layer">
          <div class="layer-icon">
            <el-icon><Key /></el-icon>
          </div>
          <div class="layer-info">
            <span class="layer-title">权限 (Permission)</span>
            <span class="layer-count">菜单 / API</span>
          </div>
        </div>
      </div>

      <!-- 角色统计 -->
      <div class="role-stats">
        <div class="stat-item">
          <span class="stat-value">{{ activeRoleCount }}</span>
          <span class="stat-label">启用</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ systemRoleCount }}</span>
          <span class="stat-label">系统角色</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ businessRoleCount }}</span>
          <span class="stat-label">业务角色</span>
        </div>
      </div>
    </div>

    <!-- 分层视图 -->
    <div v-if="viewMode === 'hierarchy'" class="hierarchy-view">
      <!-- 超级权限层 -->
      <div class="hierarchy-section super-admin">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="section-icon"><Crown /></el-icon>
            <span>超级权限</span>
            <el-tag size="small" type="danger" effect="dark">最高级别</el-tag>
          </div>
          <span class="section-desc">拥有系统所有权限，无需配置即可访问全部功能</span>
        </div>
        <div class="section-content">
          <div
            v-for="role in superAdminRoles"
            :key="role.id"
            class="role-card super"
            :class="{ disabled: role.status === 0 }"
          >
            <div class="card-header">
              <div class="role-avatar" :style="{ background: role.color || '#FF3B30' }">
                <el-icon><Medal /></el-icon>
              </div>
              <div class="role-info">
                <h4 class="role-name">{{ role.roleName }}</h4>
                <span class="role-code">{{ role.roleCode }}</span>
              </div>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, role)">
                <el-button text class="more-btn">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>编辑信息
                    </el-dropdown-item>
                    <el-dropdown-item command="users">
                      <el-icon><User /></el-icon>查看用户 ({{ role.userCount || 0 }})
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <p class="role-desc">{{ role.description || '拥有系统所有权限' }}</p>
            <div class="card-footer">
              <div class="user-badge" @click="handleViewUsers(role)">
                <el-icon><User /></el-icon>
                <span>{{ role.userCount || 0 }} 位用户</span>
              </div>
              <el-tag type="danger" size="small" effect="dark">
                <el-icon><StarFilled /></el-icon>
                全部权限
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 管理层级 - 垂直权限 -->
      <div class="hierarchy-section management">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="section-icon"><Operation /></el-icon>
            <span>管理层级</span>
            <el-tag size="small" type="warning">垂直权限</el-tag>
          </div>
          <span class="section-desc">按功能深度划分，从管理到操作再到只读的权限层级</span>
        </div>
        <div class="section-content vertical-hierarchy">
          <!-- 报表业务线 -->
          <div class="business-line">
            <div class="line-header">
              <el-icon><DataLine /></el-icon>
              <span>报表业务线</span>
            </div>
            <div class="line-roles">
              <div
                v-for="role in reportLineRoles"
                :key="role.id"
                class="role-card vertical"
                :class="{
                  disabled: role.status === 0,
                  'level-3': getPermissionLevel(role) === 3,
                  'level-2': getPermissionLevel(role) === 2,
                  'level-1': getPermissionLevel(role) === 1
                }"
              >
                <div class="level-indicator">
                  <div class="level-bars">
                    <div
                      v-for="i in 3"
                      :key="i"
                      class="bar"
                      :class="{ active: getPermissionLevel(role) >= i + 1 }"
                    />
                  </div>
                  <span class="level-text">{{ getPermissionLevelText(role) }}</span>
                </div>
                <div class="card-main">
                  <div class="role-avatar-small" :style="{ background: role.color }">
                    <el-icon v-if="getPermissionLevel(role) === 3"><Setting /></el-icon>
                    <el-icon v-else-if="getPermissionLevel(role) === 2"><Document /></el-icon>
                    <el-icon v-else><View /></el-icon>
                  </div>
                  <div class="role-info">
                    <h5 class="role-name">{{ role.roleName }}</h5>
                    <p class="role-desc-short">{{ getRoleBrief(role) }}</p>
                  </div>
                  <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, role)">
                    <el-button text size="small" class="more-btn">
                      <el-icon><More /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">
                          <el-icon><Edit /></el-icon>编辑信息
                        </el-dropdown-item>
                        <el-dropdown-item command="permission" :disabled="isReportManagerRole(role.roleCode)">
                          <el-icon><Key /></el-icon>权限配置
                        </el-dropdown-item>
                        <el-dropdown-item command="users">
                          <el-icon><User /></el-icon>查看用户 ({{ role.userCount || 0 }})
                        </el-dropdown-item>
                        <el-dropdown-item command="copy" divided>
                          <el-icon><CopyDocument /></el-icon>复制角色
                        </el-dropdown-item>
                        <el-dropdown-item v-if="!role.isSystem" command="delete" divided>
                          <el-icon><Delete /></el-icon>
                          <span class="text-danger">删除角色</span>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <div class="card-bottom">
                  <span class="user-count">{{ role.userCount || 0 }} 用户</span>
                  <el-switch
                    v-model="role.status"
                    :active-value="1"
                    :inactive-value="0"
                    :disabled="role.isSystem"
                    :loading="role.statusLoading"
                    size="small"
                    @change="handleStatusChange(role)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 职能层级 - 水平权限 -->
      <div class="hierarchy-section functional">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="section-icon"><OfficeBuilding /></el-icon>
            <span>职能层级</span>
            <el-tag size="small" type="success">水平权限</el-tag>
          </div>
          <span class="section-desc">按业务领域隔离，各司其职的部门级权限划分</span>
        </div>
        <div class="section-content functional-grid">
          <div
            v-for="role in departmentRoles"
            :key="role.id"
            class="role-card functional"
            :class="{ disabled: role.status === 0 }"
          >
            <div class="card-header">
              <div class="role-avatar" :style="{ background: role.color }">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="role-info">
                <h4 class="role-name">{{ role.roleName }}</h4>
                <span class="role-code">{{ role.roleCode }}</span>
              </div>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, role)">
                <el-button text class="more-btn">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>编辑信息
                    </el-dropdown-item>
                    <el-dropdown-item command="permission">
                      <el-icon><Key /></el-icon>权限配置
                    </el-dropdown-item>
                    <el-dropdown-item command="users">
                      <el-icon><User /></el-icon>查看用户 ({{ role.userCount || 0 }})
                    </el-dropdown-item>
                    <el-dropdown-item command="copy" divided>
                      <el-icon><CopyDocument /></el-icon>复制角色
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!role.isSystem" command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      <span class="text-danger">删除角色</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <p class="role-desc">{{ role.description }}</p>
            <div class="scope-tags">
              <el-tag size="small" type="info" effect="plain">
                {{ getDepartmentScope(role) }}
              </el-tag>
            </div>
            <div class="card-footer">
              <div class="user-badge" @click="handleViewUsers(role)">
                <el-icon><User /></el-icon>
                <span>{{ role.userCount || 0 }} 位用户</span>
              </div>
              <el-switch
                v-model="role.status"
                :active-value="1"
                :inactive-value="0"
                :disabled="role.isSystem"
                :loading="role.statusLoading"
                @change="handleStatusChange(role)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 其他角色 -->
      <div v-if="otherRoles.length > 0" class="hierarchy-section other">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="section-icon"><Files /></el-icon>
            <span>其他角色</span>
            <el-tag size="small">自定义</el-tag>
          </div>
          <span class="section-desc">用户自定义创建的业务角色</span>
        </div>
        <div class="section-content other-grid">
          <div
            v-for="role in otherRoles"
            :key="role.id"
            class="role-card other"
            :class="{ disabled: role.status === 0 }"
          >
            <div class="card-header">
              <div class="role-avatar" :style="{ background: role.color || '#007AFF' }">
                <el-icon><User /></el-icon>
              </div>
              <div class="role-info">
                <h4 class="role-name">{{ role.roleName }}</h4>
                <span class="role-code">{{ role.roleCode }}</span>
              </div>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, role)">
                <el-button text class="more-btn">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>编辑信息
                    </el-dropdown-item>
                    <el-dropdown-item command="permission">
                      <el-icon><Key /></el-icon>权限配置
                    </el-dropdown-item>
                    <el-dropdown-item command="users">
                      <el-icon><User /></el-icon>查看用户 ({{ role.userCount || 0 }})
                    </el-dropdown-item>
                    <el-dropdown-item command="copy" divided>
                      <el-icon><CopyDocument /></el-icon>复制角色
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      <span class="text-danger">删除角色</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <p class="role-desc">{{ role.description || '暂无描述' }}</p>
            <div class="card-footer">
              <div class="user-badge" @click="handleViewUsers(role)">
                <el-icon><User /></el-icon>
                <span>{{ role.userCount || 0 }} 位用户</span>
              </div>
              <el-switch
                v-model="role.status"
                :active-value="1"
                :inactive-value="0"
                :loading="role.statusLoading"
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
      </div>

      <!-- 空角色时的新建卡片 -->
      <div v-if="otherRoles.length === 0" class="add-role-section">
        <div class="add-card-large" @click="handleCreate">
          <el-icon class="add-icon"><Plus /></el-icon>
          <span class="add-text">新建自定义角色</span>
          <span class="add-hint">根据业务需求创建新的角色并配置权限</span>
        </div>
      </div>
    </div>

    <!-- 网格视图 -->
    <div v-else class="grid-view">
      <div class="role-grid">
        <div
          v-for="role in filteredRoleList"
          :key="role.id"
          class="role-card-grid"
          :class="{
            disabled: role.status === 0,
            'is-admin': isAdminRole(role.roleCode),
            'is-manager': isReportManagerRole(role.roleCode)
          }"
        >
          <div class="card-header">
            <div class="role-icon" :style="{ background: role.color || '#007AFF' }">
              <el-icon v-if="isAdminRole(role.roleCode)"><Medal /></el-icon>
              <el-icon v-else-if="isDepartmentRole(role.roleCode)"><OfficeBuilding /></el-icon>
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="card-badges">
              <el-tag v-if="isAdminRole(role.roleCode)" size="small" type="danger" effect="dark">
                超级权限
              </el-tag>
              <el-tag v-else-if="role.isSystem" size="small" type="warning">
                系统角色
              </el-tag>
              <el-tag v-if="role.status === 0" size="small" type="info">
                已禁用
              </el-tag>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, role)">
              <el-button text class="more-btn">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>编辑信息
                  </el-dropdown-item>
                  <el-dropdown-item command="permission" :disabled="isAdminRole(role.roleCode) || isReportManagerRole(role.roleCode)">
                    <el-icon><Key /></el-icon>权限配置
                  </el-dropdown-item>
                  <el-dropdown-item command="users">
                    <el-icon><User /></el-icon>查看用户 ({{ role.userCount || 0 }})
                  </el-dropdown-item>
                  <el-dropdown-item command="copy" divided>
                    <el-icon><CopyDocument /></el-icon>复制角色
                  </el-dropdown-item>
                  <el-dropdown-item v-if="!role.isSystem" command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    <span class="text-danger">删除角色</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="card-body">
            <h3 class="role-name">{{ role.roleName }}</h3>
            <p class="role-code">{{ role.roleCode }}</p>
            <p class="role-desc">{{ role.description || '暂无描述' }}</p>
            <div class="permission-level">
              <span class="level-label">权限层级:</span>
              <div class="level-bars">
                <div
                  v-for="level in 4"
                  :key="level"
                  class="level-bar"
                  :class="{ active: getPermissionLevel(role) >= level }"
                />
              </div>
              <span class="level-text">{{ getPermissionLevelText(role) }}</span>
            </div>
          </div>

          <div class="card-footer">
            <div class="user-count" @click="handleViewUsers(role)">
              <el-icon><User /></el-icon>
              <span>{{ role.userCount || 0 }} 位用户</span>
            </div>
            <el-switch
              v-model="role.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="role.isSystem"
              :loading="role.statusLoading"
              @change="handleStatusChange(role)"
            />
          </div>

          <div v-if="isAdminRole(role.roleCode)" class="admin-badge">
            <el-icon><StarFilled /></el-icon>
            拥有所有权限
          </div>
          <div v-else-if="isReportManagerRole(role.roleCode)" class="manager-badge">
            <el-icon><Key /></el-icon>
            拥有所有模板权限
          </div>
        </div>

        <div class="role-card-grid add-card" @click="handleCreate">
          <el-icon class="add-icon"><Plus /></el-icon>
          <span>新建角色</span>
        </div>
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
      width="550px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称，如：财务主管" />
        </el-form-item>

        <el-form-item label="角色标识" prop="roleCode">
          <el-input
            v-model="formData.roleCode"
            placeholder="如: FINANCE_MANAGER"
            :disabled="!!formData.id"
          >
            <template #prepend>ROLE_</template>
          </el-input>
          <div class="form-tip">
            角色标识用于后端权限判断 hasRole('ROLE_XXX')，创建后不可修改
          </div>
        </el-form-item>

        <el-form-item label="角色分类">
          <el-radio-group v-model="formData.category">
            <el-radio-button value="business">业务角色</el-radio-button>
            <el-radio-button value="department">部门角色</el-radio-button>
          </el-radio-group>
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

        <el-form-item label="角色描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="描述该角色的职责和权限范围"
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
      width="700px"
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

      <div class="perm-types-info">
        <div class="perm-type-item">
          <el-icon class="type-icon view"><View /></el-icon>
          <span>查看</span>
        </div>
        <div class="perm-type-item">
          <el-icon class="type-icon generate"><DocumentAdd /></el-icon>
          <span>生成</span>
        </div>
        <div class="perm-type-item">
          <el-icon class="type-icon download"><Download /></el-icon>
          <span>下载</span>
        </div>
        <div class="perm-type-item">
          <el-icon class="type-icon edit"><Edit /></el-icon>
          <span>编辑</span>
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
            {{ isAllExpanded ? '收起全部' : '展开全部' }}
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
      width="700px"
      class="user-dialog"
    >
      <div class="user-header">
        <div class="user-role-info">
          <div class="role-icon-small" :style="{ background: currentRole?.color }">
            <el-icon><User /></el-icon>
          </div>
          <div>
            <span class="role-name-text">{{ currentRole?.roleName }}</span>
            <el-tag size="small" type="info" style="margin-left: 8px">
              {{ roleUsers.length }} 位用户
            </el-tag>
          </div>
        </div>
        <el-input
          v-model="userSearchKeyword"
          placeholder="搜索用户"
          prefix-icon="Search"
          clearable
          style="width: 200px"
        />
      </div>

      <el-table :data="filteredUserList" max-height="400" v-if="roleUsers.length > 0">
        <el-table-column prop="avatar" label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">
              {{ row.nickname?.charAt(0) || row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="user-empty">
        <el-empty description="该角色下暂无用户" :image-size="80">
          <el-button type="primary" size="small" @click="goToUserManage">
            分配用户
          </el-button>
        </el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, More, Edit, Key, Delete, Plus, Folder, Document, InfoFilled,
  Check, Lock, UserFilled, Medal, OfficeBuilding, StarFilled, CopyDocument,
  View, DocumentAdd, Download, Right, Avatar, Operation, DataLine, Setting,
  Files, Crown
} from '@element-plus/icons-vue'
import {
  listRoles,
  createRole,
  updateRole,
  deleteRole,
  toggleRoleStatus,
  getRolePermissions,
  saveRolePermissions,
  getPermissionTree,
  getRoleUsers
} from '@/api/role'

const router = useRouter()

// 颜色选项
const colorOptions = [
  '#FF3B30', '#FF9500', '#FFCC00', '#34C759', '#00C7BE',
  '#007AFF', '#5856D6', '#AF52DE', '#FF2D55', '#A2845E'
]

// 搜索和视图模式
const searchKeyword = ref('')
const viewMode = ref('hierarchy')

// 数据
const roleList = ref([])

// 统计数据
const activeRoleCount = computed(() => roleList.value.filter(r => r.status === 1).length)
const systemRoleCount = computed(() => roleList.value.filter(r => r.isSystem).length)
const businessRoleCount = computed(() => roleList.value.filter(r => !r.isSystem).length)
const totalUserCount = computed(() => roleList.value.reduce((sum, r) => sum + (r.userCount || 0), 0))

// 角色分类判断
const isAdminRole = (roleCode) => {
  if (!roleCode) return false
  const code = roleCode.toUpperCase()
  return code === 'ROLE_ADMIN' || code === 'ADMIN'
}

const isReportManagerRole = (roleCode) => {
  if (!roleCode) return false
  const code = roleCode.toUpperCase()
  return code === 'ROLE_REPORT_MANAGER' || code === 'REPORT_MANAGER'
}

const isReportUserRole = (roleCode) => {
  if (!roleCode) return false
  const code = roleCode.toUpperCase()
  return code === 'ROLE_REPORT_USER' || code === 'REPORT_USER'
}

const isReportViewerRole = (roleCode) => {
  if (!roleCode) return false
  const code = roleCode.toUpperCase()
  return code === 'ROLE_REPORT_VIEWER' || code === 'REPORT_VIEWER'
}

const isDepartmentRole = (roleCode) => {
  if (!roleCode) return false
  const code = roleCode.toUpperCase()
  const deptRoles = [
    'ROLE_FINANCE_MANAGER', 'FINANCE_MANAGER',
    'ROLE_HR_MANAGER', 'HR_MANAGER',
    'ROLE_SALES_MANAGER', 'SALES_MANAGER',
    'ROLE_WAREHOUSE_MANAGER', 'WAREHOUSE_MANAGER'
  ]
  return deptRoles.includes(code)
}

const isReportLineRole = (roleCode) => {
  return isReportManagerRole(roleCode) || isReportUserRole(roleCode) || isReportViewerRole(roleCode)
}

// 获取权限层级
const getPermissionLevel = (role) => {
  if (isAdminRole(role.roleCode)) return 4
  if (isReportManagerRole(role.roleCode)) return 3
  if (isReportUserRole(role.roleCode)) return 2
  if (isDepartmentRole(role.roleCode)) return 2
  if (isReportViewerRole(role.roleCode)) return 1
  if (role.isSystem) return 3
  return 1
}

const getPermissionLevelText = (role) => {
  const level = getPermissionLevel(role)
  const texts = ['', '只读', '操作', '管理', '完全']
  return texts[level]
}

// 获取角色简述
const getRoleBrief = (role) => {
  if (isReportManagerRole(role.roleCode)) return '管理模板和数据源'
  if (isReportUserRole(role.roleCode)) return '查看和生成报表'
  if (isReportViewerRole(role.roleCode)) return '仅可查看报表'
  return role.description || '暂无描述'
}

// 获取部门权限范围
const getDepartmentScope = (role) => {
  const code = role.roleCode?.toUpperCase() || ''
  if (code.includes('FINANCE')) return '财务数据'
  if (code.includes('HR')) return '人事数据'
  if (code.includes('SALES')) return '销售数据'
  if (code.includes('WAREHOUSE')) return '仓库数据'
  return '部门数据'
}

// 分组角色列表
const superAdminRoles = computed(() => {
  return roleList.value.filter(r => isAdminRole(r.roleCode))
})

const reportLineRoles = computed(() => {
  return roleList.value.filter(r => isReportLineRole(r.roleCode))
    .sort((a, b) => getPermissionLevel(b) - getPermissionLevel(a))
})

const departmentRoles = computed(() => {
  return roleList.value.filter(r => isDepartmentRole(r.roleCode))
})

const otherRoles = computed(() => {
  return roleList.value.filter(r =>
    !isAdminRole(r.roleCode) &&
    !isReportLineRole(r.roleCode) &&
    !isDepartmentRole(r.roleCode)
  )
})

// 筛选后的角色列表
const filteredRoleList = computed(() => {
  if (!searchKeyword.value) return roleList.value
  const keyword = searchKeyword.value.toLowerCase()
  return roleList.value.filter(role =>
    role.roleName.toLowerCase().includes(keyword) ||
    role.roleCode.toLowerCase().includes(keyword) ||
    (role.description && role.description.toLowerCase().includes(keyword))
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
  category: 'business',
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
    { pattern: /^[A-Z][A-Z_]*$/, message: '格式: 大写字母和下划线', trigger: 'blur' }
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

const filterNode = (value, data) => {
  if (!value) return true
  return data.label.toLowerCase().includes(value.toLowerCase())
}

const selectedCount = computed(() => {
  if (!permTreeRef.value) return checkedPermissions.value.filter(id => id > 0).length
  const checked = permTreeRef.value.getCheckedKeys()
  return checked.filter(id => id > 0).length
})

// 用户列表弹窗
const userDialogVisible = ref(false)
const roleUsers = ref([])
const userSearchKeyword = ref('')

const filteredUserList = computed(() => {
  if (!userSearchKeyword.value) return roleUsers.value
  const keyword = userSearchKeyword.value.toLowerCase()
  return roleUsers.value.filter(user =>
    user.username?.toLowerCase().includes(keyword) ||
    user.nickname?.toLowerCase().includes(keyword) ||
    user.email?.toLowerCase().includes(keyword)
  )
})

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
    expandedKeys.value = getAllNodeIds(permissionTree.value)
  } catch (error) {
    console.error('加载权限树失败:', error)
    permissionTree.value = []
  }
}

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

// 处理下拉菜单命令
const handleCommand = (command, role) => {
  switch (command) {
    case 'edit':
      handleEdit(role)
      break
    case 'permission':
      handlePermission(role)
      break
    case 'users':
      handleViewUsers(role)
      break
    case 'copy':
      handleCopy(role)
      break
    case 'delete':
      handleDelete(role)
      break
  }
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
    roleCode: role.roleCode.replace('ROLE_', ''),
    category: isDepartmentRole(role.roleCode) ? 'department' : 'business',
    color: role.color || '#007AFF',
    sort: role.sort || 0,
    status: role.status,
    description: role.description || ''
  })
  dialogVisible.value = true
}

// 复制角色
const handleCopy = (role) => {
  Object.assign(formData, {
    id: null,
    roleName: role.roleName + ' (副本)',
    roleCode: '',
    category: isDepartmentRole(role.roleCode) ? 'department' : 'business',
    color: role.color || '#007AFF',
    sort: role.sort || 0,
    status: 1,
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
  if (isAdminRole(role.roleCode)) {
    ElMessage.info('系统管理员自动拥有所有权限，无需配置')
    return
  }
  if (isReportManagerRole(role.roleCode)) {
    ElMessage.info('报表管理员自动拥有所有模板权限，无需配置')
    return
  }

  currentRole.value = role
  permSearchKeyword.value = ''

  try {
    const res = await getRolePermissions(role.id)
    checkedPermissions.value = res.data || []
    permDialogVisible.value = true

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

const handleTreeCheck = () => {}

const handleSelectAll = () => {
  if (permTreeRef.value) {
    const allTemplateIds = getAllTemplateIds(permissionTree.value)
    permTreeRef.value.setCheckedKeys(allTemplateIds)
  }
}

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

const handleSelectNone = () => {
  if (permTreeRef.value) {
    permTreeRef.value.setCheckedKeys([])
  }
}

const handleExpandAll = () => {
  if (isAllExpanded.value) {
    expandedKeys.value = []
  } else {
    expandedKeys.value = getAllNodeIds(permissionTree.value)
  }
  isAllExpanded.value = !isAllExpanded.value
}

const handlePermSubmit = async () => {
  permLoading.value = true
  try {
    const checkedKeys = permTreeRef.value?.getCheckedKeys() || []
    const templateIds = checkedKeys.filter(id => id > 0)
    await saveRolePermissions(currentRole.value.id, templateIds)
    ElMessage.success('权限配置已保存')
    permDialogVisible.value = false
    loadRoles()
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    permLoading.value = false
  }
}

// 查看用户
const handleViewUsers = async (role) => {
  currentRole.value = role
  userSearchKeyword.value = ''

  try {
    const res = await getRoleUsers(role.id)
    roleUsers.value = res.data || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
    roleUsers.value = []
  }

  userDialogVisible.value = true
}

const goToUserManage = () => {
  userDialogVisible.value = false
  router.push('/system/user')
}

// 状态变更
const handleStatusChange = async (role) => {
  role.statusLoading = true
  try {
    await toggleRoleStatus(role.id, role.status)
    ElMessage.success(`角色「${role.roleName}」已${role.status ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('状态变更失败:', error)
    role.status = role.status ? 0 : 1
  } finally {
    role.statusLoading = false
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData = {
          ...formData,
          roleCode: formData.id ? formData.roleCode : 'ROLE_' + formData.roleCode
        }

        if (formData.id) {
          await updateRole(formData.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createRole(submitData)
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
    category: 'business',
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
  max-width: 1400px;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 220px;
}

.view-mode-select {
  width: 120px;
}

// RBAC 架构概览
.rbac-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, $bg-primary, rgba($primary-color, 0.02));
  border-radius: $radius-xl;
  box-shadow: $shadow-sm;
  margin-bottom: 24px;
  border: 1px solid rgba($primary-color, 0.1);
}

.rbac-model {
  display: flex;
  align-items: center;
  gap: 16px;
}

.model-layer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-radius: $radius-lg;
  background: $bg-primary;
  box-shadow: $shadow-sm;

  &.user-layer .layer-icon {
    background: linear-gradient(135deg, #5856D6, #AF52DE);
  }

  &.role-layer .layer-icon {
    background: linear-gradient(135deg, #007AFF, #00C7BE);
  }

  &.permission-layer .layer-icon {
    background: linear-gradient(135deg, #FF9500, #FFCC00);
  }
}

.layer-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.layer-info {
  display: flex;
  flex-direction: column;
}

.layer-title {
  font-size: 14px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.layer-count {
  font-size: 12px;
  color: $text-tertiary;
}

.model-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: $text-tertiary;

  .el-icon {
    font-size: 16px;
  }

  span {
    font-size: 11px;
  }
}

.role-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-item {
  text-align: center;

  .stat-value {
    display: block;
    font-size: 24px;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .stat-label {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: $border-color;
}

// 分层视图
.hierarchy-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hierarchy-section {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;

  &.super-admin {
    border-left: 4px solid $danger-color;
    background: linear-gradient(135deg, $bg-primary, rgba($danger-color, 0.02));
  }

  &.management {
    border-left: 4px solid $warning-color;
    background: linear-gradient(135deg, $bg-primary, rgba($warning-color, 0.02));
  }

  &.functional {
    border-left: 4px solid $success-color;
    background: linear-gradient(135deg, $bg-primary, rgba($success-color, 0.02));
  }

  &.other {
    border-left: 4px solid $primary-color;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid $border-color;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;

  .section-icon {
    font-size: 20px;
  }
}

.section-desc {
  font-size: 13px;
  color: $text-tertiary;
}

.section-content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;

  &.functional-grid,
  &.other-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

// 超级管理员卡片
.role-card.super {
  flex: 1;
  min-width: 300px;
  max-width: 400px;
  padding: 20px;
  background: $bg-primary;
  border-radius: $radius-lg;
  border: 2px solid rgba($danger-color, 0.2);

  .card-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 12px;
  }

  .role-avatar {
    width: 48px;
    height: 48px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
  }

  .role-info {
    flex: 1;

    .role-name {
      font-size: 18px;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      margin: 0;
    }

    .role-code {
      font-size: 12px;
      color: $text-tertiary;
      font-family: $font-family-mono;
    }
  }

  .role-desc {
    font-size: 13px;
    color: $text-secondary;
    margin-bottom: 16px;
    line-height: 1.5;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid $border-color;
  }

  .user-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: $text-secondary;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: $radius-sm;
    transition: all $transition-fast;

    &:hover {
      background: $gray-100;
      color: $primary-color;
    }
  }
}

// 垂直层级
.business-line {
  width: 100%;

  .line-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: $font-weight-medium;
    color: $text-secondary;
    margin-bottom: 12px;

    .el-icon {
      color: $warning-color;
    }
  }

  .line-roles {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }
}

.role-card.vertical {
  flex: 1;
  min-width: 240px;
  max-width: 300px;
  padding: 16px;
  background: $bg-primary;
  border-radius: $radius-lg;
  border: 1px solid $border-color;
  transition: all $transition-normal;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &.level-3 {
    border-color: rgba($warning-color, 0.3);
    .level-bars .bar.active { background: $warning-color; }
    .level-text { color: $warning-color; }
  }

  &.level-2 {
    border-color: rgba($success-color, 0.3);
    .level-bars .bar.active { background: $success-color; }
    .level-text { color: $success-color; }
  }

  &.level-1 {
    border-color: rgba($info-color, 0.3);
    .level-bars .bar.active { background: $info-color; }
    .level-text { color: $info-color; }
  }

  &.disabled {
    opacity: 0.6;
  }
}

.level-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  .level-bars {
    display: flex;
    gap: 3px;

    .bar {
      width: 20px;
      height: 4px;
      background: $gray-200;
      border-radius: 2px;

      &.active {
        background: $primary-color;
      }
    }
  }

  .level-text {
    font-size: 11px;
    font-weight: $font-weight-medium;
    color: $text-tertiary;
  }
}

.card-main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.role-avatar-small {
  width: 36px;
  height: 36px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
}

.role-card.vertical .role-info {
  flex: 1;
  min-width: 0;

  .role-name {
    font-size: 14px;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    margin: 0 0 4px;
  }

  .role-desc-short {
    font-size: 12px;
    color: $text-tertiary;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid $border-color;

  .user-count {
    font-size: 11px;
    color: $text-tertiary;
  }
}

// 职能层级卡片
.role-card.functional {
  padding: 16px;
  background: $bg-primary;
  border-radius: $radius-lg;
  border: 1px solid $border-color;
  transition: all $transition-normal;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &.disabled {
    opacity: 0.6;
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 10px;
  }

  .role-avatar {
    width: 40px;
    height: 40px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
  }

  .role-info {
    flex: 1;

    .role-name {
      font-size: 15px;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      margin: 0;
    }

    .role-code {
      font-size: 11px;
      color: $text-tertiary;
      font-family: $font-family-mono;
    }
  }

  .role-desc {
    font-size: 12px;
    color: $text-secondary;
    margin-bottom: 10px;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .scope-tags {
    margin-bottom: 12px;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid $border-color;
  }

  .user-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: $text-secondary;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: $radius-sm;

    &:hover {
      background: $gray-100;
      color: $primary-color;
    }
  }
}

// 其他角色卡片
.role-card.other {
  padding: 16px;
  background: $bg-primary;
  border-radius: $radius-lg;
  border: 1px solid $border-color;
  transition: all $transition-normal;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &.disabled {
    opacity: 0.6;
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 10px;
  }

  .role-avatar {
    width: 40px;
    height: 40px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
  }

  .role-info {
    flex: 1;

    .role-name {
      font-size: 15px;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      margin: 0;
    }

    .role-code {
      font-size: 11px;
      color: $text-tertiary;
      font-family: $font-family-mono;
    }
  }

  .role-desc {
    font-size: 12px;
    color: $text-secondary;
    margin-bottom: 12px;
    line-height: 1.5;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid $border-color;
  }

  .user-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: $text-secondary;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: $radius-sm;

    &:hover {
      background: $gray-100;
      color: $primary-color;
    }
  }
}

// 新建卡片
.role-card.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  border: 2px dashed $border-color;
  background: transparent;
  cursor: pointer;
  transition: all $transition-normal;

  .add-icon {
    font-size: 32px;
    color: $text-tertiary;
    margin-bottom: 8px;
  }

  span {
    font-size: 13px;
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

.add-role-section {
  margin-top: 24px;
}

.add-card-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  border: 2px dashed $border-color;
  border-radius: $radius-xl;
  background: $bg-primary;
  cursor: pointer;
  transition: all $transition-normal;

  .add-icon {
    font-size: 40px;
    color: $text-tertiary;
    margin-bottom: 12px;
  }

  .add-text {
    font-size: 16px;
    font-weight: $font-weight-medium;
    color: $text-secondary;
    margin-bottom: 8px;
  }

  .add-hint {
    font-size: 13px;
    color: $text-tertiary;
  }

  &:hover {
    border-color: $primary-color;
    background: rgba($primary-color, 0.02);

    .add-icon,
    .add-text {
      color: $primary-color;
    }
  }
}

.more-btn {
  color: $text-tertiary;

  &:hover {
    color: $text-primary;
  }
}

// 网格视图
.grid-view {
  .role-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }
}

.role-card-grid {
  background: $bg-primary;
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-sm;
  transition: all $transition-normal;
  position: relative;
  overflow: hidden;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  &.disabled {
    opacity: 0.6;
  }

  &.is-admin {
    border: 2px solid rgba($danger-color, 0.3);
    background: linear-gradient(135deg, $bg-primary, rgba($danger-color, 0.02));
  }

  &.is-manager {
    border: 2px solid rgba($primary-color, 0.3);
    background: linear-gradient(135deg, $bg-primary, rgba($primary-color, 0.02));
  }

  &.add-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 2px dashed $border-color;
    background: transparent;
    cursor: pointer;
    min-height: 260px;

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
    flex-wrap: wrap;
  }

  .role-icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
  }

  .card-body {
    margin-bottom: 16px;

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
      margin-bottom: 12px;
    }
  }

  .permission-level {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: $gray-50;
    border-radius: $radius-md;
  }

  .level-label {
    font-size: 12px;
    color: $text-tertiary;
  }

  .level-bars {
    display: flex;
    gap: 3px;
  }

  .level-bar {
    width: 16px;
    height: 6px;
    background: $gray-200;
    border-radius: 3px;

    &.active {
      background: linear-gradient(135deg, $primary-color, $success-color);
    }
  }

  .level-text {
    font-size: 12px;
    font-weight: $font-weight-medium;
    color: $primary-color;
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
    gap: 4px;
    font-size: 12px;
    color: $text-secondary;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: $radius-sm;
    transition: all $transition-fast;

    &:hover {
      background: $gray-100;
      color: $primary-color;
    }
  }
}

.admin-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px 12px;
  background: linear-gradient(135deg, $danger-color, #FF6B6B);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.manager-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px 12px;
  background: linear-gradient(135deg, $primary-color, #5B8DEF);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

// 表单样式
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
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: transform $transition-fast;
  border: 2px solid transparent;

  &:hover {
    transform: scale(1.1);
  }

  &.active {
    border-color: $text-primary;
    transform: scale(1.1);
  }
}

// 权限配置弹窗
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
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
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

.perm-types-info {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 12px;
  background: rgba($primary-color, 0.05);
  border-radius: $radius-md;
  margin-bottom: 16px;
}

.perm-type-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: $text-secondary;
}

.type-icon {
  font-size: 16px;

  &.view { color: $info-color; }
  &.generate { color: $success-color; }
  &.download { color: $warning-color; }
  &.edit { color: $primary-color; }
}

.perm-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.perm-search {
  width: 220px;
}

.perm-actions {
  display: flex;
  gap: 8px;
}

.perm-tree-wrapper {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: 12px;
  max-height: 300px;
  overflow-y: auto;
  background: $bg-primary;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  flex: 1;
}

.node-icon {
  font-size: 16px;

  &.category { color: $warning-color; }
  &.template { color: $primary-color; }
}

.node-label {
  flex: 1;
}

.perm-empty {
  padding: 40px 0;
}

.perm-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: rgba($info-color, 0.1);
  border-radius: $radius-md;
  font-size: 12px;
  color: $info-color;
  line-height: 1.5;
}

// 用户列表弹窗
.user-dialog {
  :deep(.el-dialog__body) {
    padding: 0 20px 20px;
  }
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: $gray-50;
  border-radius: $radius-lg;
  margin-bottom: 16px;
}

.user-role-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-empty {
  padding: 60px 0;
}

.empty-state {
  padding: 60px 0;
}

.text-danger {
  color: $danger-color;
}

// 响应式
@media (max-width: 1200px) {
  .rbac-overview {
    flex-direction: column;
    gap: 20px;
  }

  .rbac-model {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-input {
    flex: 1;
    min-width: 160px;
  }

  .role-stats {
    flex-wrap: wrap;
    justify-content: center;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
