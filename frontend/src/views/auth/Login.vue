<template>
  <div class="login-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="login-card">
      <div class="login-header">
        <h1><i class="el-icon-s-platform"></i> CodeCollab</h1>
        <p>{{ AUTH.LOGIN_SUBTITLE }}</p>
      </div>
      <el-form ref="loginForm" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" :placeholder="AUTH.USERNAME" prefix-icon="el-icon-user" size="large"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" :placeholder="AUTH.PASSWORD" prefix-icon="el-icon-lock" size="large" show-password @keyup.enter.native="handleLogin"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" size="large" class="login-btn">
            {{ loading ? COMMON.LOADING : AUTH.LOGIN }}
          </el-button>
        </el-form-item>
        <div class="login-footer">
          <span>{{ AUTH.NO_ACCOUNT }} </span>
          <router-link to="/register">{{ AUTH.REGISTER }}</router-link>
        </div>
      </el-form>
      <div class="theme-switch">
        <el-tooltip :content="isDarkMode ? '切换亮色模式' : '切换暗色模式'" placement="bottom">
          <el-button circle @click="toggleDarkMode" size="small">
            <i :class="isDarkMode ? 'el-icon-sunny' : 'el-icon-moon'"></i>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { login } from '@/api/auth'
import { AUTH, COMMON, VALIDATION } from '@/utils/constants'

export default {
  name: 'Login',
  data() {
    return {
      AUTH,
      COMMON,
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: VALIDATION.USERNAME_REQUIRED, trigger: 'blur' }],
        password: [{ required: true, message: VALIDATION.PASSWORD_REQUIRED, trigger: 'blur' }]
      },
      loading: false
    }
  },
  computed: {
    ...mapState('theme', ['isDarkMode'])
  },
  methods: {
    ...mapMutations('theme', ['TOGGLE_DARK_MODE']),
    toggleDarkMode() {
      this.TOGGLE_DARK_MODE()
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.form)
          this.$store.dispatch('user/login', res.data)
          this.$message.success(AUTH.LOGIN_SUCCESS)
          this.$router.push('/dashboard')
        } catch (error) {
          // Error handled by interceptor
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: background 0.3s ease;

  &.dark-mode {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  }
}

.login-card {
  width: 420px;
  padding: 40px;
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: scaleIn 0.3s ease;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  h1 {
    color: var(--primary-color);
    font-size: 28px;
    margin-bottom: 8px;
  }

  p {
    color: var(--text-color-secondary);
    font-size: 14px;
  }
}

.login-form {
  .el-input {
    margin-bottom: 4px;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #409eff 0%, #6366f1 100%);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  color: var(--text-color-secondary);

  a {
    color: var(--primary-color);
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

.theme-switch {
  position: absolute;
  top: 16px;
  right: 16px;
}
</style>
