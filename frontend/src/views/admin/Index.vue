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
        </div>
        <el-table :data="users" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="username" label="用户名"></el-table-column>
          <el-table-column prop="email" label="邮箱"></el-table-column>
          <el-table-column prop="role" label="角色">
            <template slot-scope="scope">
              <el-tag :type="getRoleType(scope.row.role)">{{ getRoleName(scope.row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button size="mini" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="mini" type="danger" @click="deleteUser(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="userTotal > pageSize" :current-page="userPage" :page-size="pageSize" :total="userTotal" layout="prev, pager, next" @current-change="handleUserPageChange" style="margin-top: 20px;"></el-pagination>
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
  </div>
</template>

<script>
import { getUserPage, updateUserStatus, deleteUser, getDashboardStats, getSystemConfigs, updateSystemConfig } from '@/api/admin'

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
      userTotal: 0
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
    async fetchConfigs() {
      try {
        const res = await getSystemConfigs()
        this.configs = res.data || []
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
    async toggleStatus(user) {
      try {
        await updateUserStatus(user.id, user.status === 1 ? 0 : 1)
        this.$message.success('状态已更新')
        this.fetchUsers()
      } catch (error) {
        // Error handled by interceptor
      }
    },
    async deleteUser(user) {
      this.$confirm(`确定删除用户 ${user.username} 吗？`, '警告', { type: 'warning' }).then(async () => {
        await deleteUser(user.id)
        this.$message.success('用户已删除')
        this.fetchUsers()
      }).catch(() => {})
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
}
</style>
