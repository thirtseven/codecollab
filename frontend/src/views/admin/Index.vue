<template>
  <div class="page-container">
    <h2 class="page-title">系统管理</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="仪表盘" name="dashboard">
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in dashboardStats" :key="stat.label">
            <div class="stat-card" :style="{ background: stat.gradient }">
              <div class="stat-icon"><i :class="stat.icon"></i></div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="用户管理" name="users">
        <div class="table-header">
          <el-input v-model="userKeyword" placeholder="搜索用户..." prefix-icon="el-icon-search" clearable style="width: 250px;" @clear="fetchUsers" @keyup.enter.native="fetchUsers"></el-input>
          <el-select v-model="userRole" placeholder="筛选角色" clearable @change="fetchUsers">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="学生" value="STUDENT"></el-option>
          </el-select>
          <el-button type="primary" @click="showAddUserDialog = true">
            <i class="el-icon-plus"></i> 添加用户
          </el-button>
        </div>
        <el-table :data="users" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="username" label="用户名"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column prop="nickname" label="昵称"></el-table-column>
          <el-table-column prop="role" label="角色" width="150">
            <template slot-scope="scope">
              <el-select v-model="scope.row.role" size="small" @change="changeRole(scope.row)">
                <el-option label="管理员" value="ADMIN"></el-option>
                <el-option label="教师" value="TEACHER"></el-option>
                <el-option label="学生" value="STUDENT"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="180"></el-table-column>
          <el-table-column label="操作" width="280">
            <template slot-scope="scope">
              <el-button size="mini" @click="editUser(scope.row)">编辑</el-button>
              <el-button size="mini" @click="resetPassword(scope.row)">重置密码</el-button>
              <el-button size="mini" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="mini" type="danger" @click="deleteUser(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="userTotal > pageSize" :current-page="userPage" :page-size="pageSize" :total="userTotal" layout="prev, pager, next, total" @current-change="handleUserPageChange" style="margin-top: 20px;"></el-pagination>
      </el-tab-pane>
      <el-tab-pane label="系统配置" name="configs">
        <el-table :data="configs" style="width: 100%">
          <el-table-column prop="key" label="配置键"></el-table-column>
          <el-table-column prop="value" label="配置值">
            <template slot-scope="scope">
              <el-input v-model="scope.row.value" size="small"></el-input>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述"></el-table-column>
          <el-table-column label="操作" width="100">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="updateConfig(scope.row)">保存</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="添加用户" :visible.sync="showAddUserDialog" width="500px">
      <el-form :model="addUserForm" :rules="addUserRules" ref="addUserForm">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addUserForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addUserForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addUserForm.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="addUserForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addUserForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="学生" value="STUDENT"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddUserDialog = false">取消</el-button>
        <el-button type="primary" :loading="addingUser" @click="addUser">添加</el-button>
      </div>
    </el-dialog>

    <el-dialog title="编辑用户" :visible.sync="showEditDialog" width="500px">
      <el-form :model="editForm">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="邮箱"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="昵称"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="手机号"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="角色" style="width: 100%;">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="学生" value="STUDENT"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="重置密码" :visible.sync="showResetPwdDialog" width="400px">
      <el-form :model="resetPwdForm">
        <el-form-item label="新密码">
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showResetPwdDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认重置</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserPage, updateUserStatus, deleteUser, getDashboardStats, getSystemConfigs, updateSystemConfig, updateUserRole, resetUserPassword, updateUser, addUser } from '@/api/admin'

export default {
  name: 'AdminIndex',
  data() {
    return {
      activeTab: 'dashboard',
      users: [],
      configs: [],
      dashboardStats: [],
      userKeyword: '',
      userRole: '',
      userPage: 1,
      pageSize: 10,
      userTotal: 0,
      showAddUserDialog: false,
      showEditDialog: false,
      showResetPwdDialog: false,
      addingUser: false,
      addUserForm: { username: '', email: '', password: '', nickname: '', role: 'STUDENT' },
      addUserRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },
      editForm: { id: null, username: '', email: '', nickname: '', phone: '', role: '' },
      resetPwdForm: { userId: null, newPassword: '' }
    }
  },
  created() {
    this.fetchDashboard()
    this.fetchUsers()
    this.fetchConfigs()
  },
  methods: {
    async fetchDashboard() {
      try {
        const res = await getDashboardStats()
        const data = res.data
        this.dashboardStats = [
          { label: '总用户数', value: data.totalUsers, icon: 'el-icon-user', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
          { label: '总团队数', value: data.totalTeams, icon: 'el-icon-s-cooperation', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
          { label: '总项目数', value: data.totalProjects, icon: 'el-icon-folder-opened', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
          { label: '总提交数', value: data.totalSubmissions, icon: 'el-icon-upload2', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
        ]
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async fetchUsers() {
      try {
        const res = await getUserPage({ page: this.userPage, size: this.pageSize, keyword: this.userKeyword, role: this.userRole })
        this.users = res.data?.records || []
        this.userTotal = res.data?.total || 0
      } catch (error) {
        // Error handled by interceptor
      }
    },
    handleUserPageChange(page) {
      this.userPage = page
      this.fetchUsers()
    },
    getRoleType(role) {
      const map = { ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'info' }
      return map[role] || 'info'
    },
    getRoleName(role) {
      const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
      return map[role] || '未知'
    },
    async changeRole(user) {
      try {
        await updateUserRole(user.id, user.role)
        this.$message.success('角色已更新')
        this.fetchUsers()
      } catch (error) {
        this.fetchUsers()
      }
    },
    async toggleStatus(user) {
      try {
        await updateUserStatus(user.id, user.status === 1 ? 0 : 1)
        this.$message.success('状态已更新')
        this.fetchUsers()
      } catch (error) {
        // Error handled by interceptor
      }
    },
    editUser(user) {
      this.editForm = {
        id: user.id,
        username: user.username,
        email: user.email,
        nickname: user.nickname || '',
        phone: user.phone || '',
        role: user.role
      }
      this.showEditDialog = true
    },
    async saveUser() {
      try {
        await updateUser(this.editForm.id, this.editForm)
        this.$message.success('用户信息已更新')
        this.showEditDialog = false
        this.fetchUsers()
      } catch (error) {
        // Error handled by interceptor
      }
    },
    resetPassword(user) {
      this.resetPwdForm = { userId: user.id, newPassword: '' }
      this.showResetPwdDialog = true
    },
    async confirmResetPassword() {
      if (!this.resetPwdForm.newPassword || this.resetPwdForm.newPassword.length < 6) {
        this.$message.warning('密码至少6位')
        return
      }
      try {
        await resetUserPassword(this.resetPwdForm.userId, this.resetPwdForm.newPassword)
        this.$message.success('密码已重置')
        this.showResetPwdDialog = false
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async addUser() {
      this.$refs.addUserForm.validate(async valid => {
        if (!valid) return
        this.addingUser = true
        try {
          await addUser(this.addUserForm)
          this.$message.success('用户添加成功')
          this.showAddUserDialog = false
          this.addUserForm = { username: '', email: '', password: '', nickname: '', role: 'STUDENT' }
          this.fetchUsers()
        } catch (error) {
          // Error handled by interceptor
        } finally {
          this.addingUser = false
        }
      })
    },
    async deleteUser(user) {
      this.$confirm(`确定删除用户 ${user.username} 吗？`, '警告', { type: 'warning' }).then(async () => {
        await deleteUser(user.id)
        this.$message.success('用户已删除')
        this.fetchUsers()
      }).catch(() => {})
    },
    async fetchConfigs() {
      try {
        const res = await getSystemConfigs()
        this.configs = res.data || []
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async updateConfig(config) {
      try {
        await updateSystemConfig(config.key, config.value)
        this.$message.success('配置已更新')
      } catch (error) {
        // Error handled by interceptor
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.page-title {
  margin-bottom: 24px;
  color: var(--text-color);
}

.stat-card {
  padding: 24px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  }
}

.stat-icon {
  font-size: 36px;
  opacity: 0.8;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.table-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
</style>
