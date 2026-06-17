<template>
  <div class="page-container">
    <h2 class="page-title">个人中心</h2>
    <el-row :gutter="24">
      <el-col :span="8">
        <div class="card profile-card">
          <div class="profile-header">
            <el-avatar :size="80" :src="user.avatar">
              {{ user.nickname ? user.nickname[0] : 'U' }}
            </el-avatar>
            <h3>{{ user.nickname || user.username }}</h3>
            <el-tag :type="getRoleType(user.role)">{{ getRoleName(user.role) }}</el-tag>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <div class="stat-value">{{ user.createdAt?.split(' ')[0] }}</div>
              <div class="stat-label">加入时间</div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="16">
        <div class="card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="编辑资料" name="profile">
              <el-form :model="profileForm" label-width="100px">
                <el-form-item label="昵称">
                  <el-input v-model="profileForm.nickname"></el-input>
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone"></el-input>
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input v-model="profileForm.bio" type="textarea" :rows="3"></el-input>
                </el-form-item>
                <el-form-item label="GitHub">
                  <el-input v-model="profileForm.githubUrl" placeholder="https://github.com/username"></el-input>
                </el-form-item>
                <el-form-item label="个人网站">
                  <el-input v-model="profileForm.website" placeholder="https://example.com"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { updateProfile, changePassword, getUserInfo } from '@/api/auth'

export default {
  name: 'Profile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次密码不一致'))
      } else {
        callback()
      }
    }
    return {
      activeTab: 'profile',
      profileForm: { nickname: '', phone: '', bio: '', githubUrl: '', website: '' },
      passwordForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapState('user', ['user'])
  },
  created() {
    this.fetchUserInfo()
  },
  methods: {
    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        const user = res.data
        this.profileForm = {
          nickname: user.nickname || '',
          phone: user.phone || '',
          bio: user.bio || '',
          githubUrl: user.githubUrl || '',
          website: user.website || ''
        }
        this.$store.commit('user/SET_USER', user)
      } catch (error) {
        // Error handled by interceptor
      }
    },
    getRoleType(role) {
      const map = { ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'info' }
      return map[role] || 'info'
    },
    getRoleName(role) {
      const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
      return map[role] || '未知'
    },
    async updateProfile() {
      try {
        const res = await updateProfile(this.profileForm)
        this.$store.commit('user/SET_USER', res.data)
        this.$message.success('资料已更新')
      } catch (error) {
        // Error handled by interceptor
      }
    },
    changePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (!valid) return
        try {
          await changePassword(this.passwordForm.oldPassword, this.passwordForm.newPassword)
          this.$message.success('密码已修改')
          this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
        } catch (error) {
          // Error handled by interceptor
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page-title {
  margin-bottom: 24px;
  color: var(--text-color);
}

.profile-card {
  text-align: center;
}

.profile-header {
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;

  h3 {
    margin: 12px 0 8px;
    color: var(--text-color);
  }
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.stat-item {
  .stat-value {
    font-size: 16px;
    font-weight: 500;
    color: var(--text-color);
  }

  .stat-label {
    font-size: 12px;
    color: var(--text-color-secondary);
    margin-top: 4px;
  }
}
</style>
