<template>
  <div class="register-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="register-card">
      <div class="register-header">
        <h1><i class="el-icon-s-platform"></i> CodeCollab</h1>
        <p>{{ AUTH.REGISTER_SUBTITLE }}</p>
      </div>
      <el-form ref="registerForm" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" :placeholder="AUTH.USERNAME" prefix-icon="el-icon-user" size="large"></el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" :placeholder="AUTH.EMAIL" prefix-icon="el-icon-message" size="large"></el-input>
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input">
            <el-input v-model="form.code" :placeholder="AUTH.VERIFICATION_CODE" prefix-icon="el-icon-key" size="large"></el-input>
            <el-button :disabled="countdown > 0" @click="sendCode" size="large" type="primary" plain>
              {{ countdown > 0 ? `${countdown}s` : AUTH.SEND_CODE }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" :placeholder="AUTH.PASSWORD" prefix-icon="el-icon-lock" size="large" show-password></el-input>
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" :placeholder="AUTH.NICKNAME + '（可选）'" prefix-icon="el-icon-user-solid" size="large"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" size="large" class="register-btn">
            {{ loading ? COMMON.LOADING : AUTH.REGISTER }}
          </el-button>
        </el-form-item>
        <div class="register-footer">
          <span>{{ AUTH.HAS_ACCOUNT }} </span>
          <router-link to="/login">{{ AUTH.LOGIN }}</router-link>
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
import { register, sendCode } from '@/api/auth'
import { AUTH, COMMON, VALIDATION } from '@/utils/constants'

export default {
  name: 'Register',
  data() {
    return {
      AUTH,
      COMMON,
      form: { username: '', email: '', code: '', password: '', nickname: '' },
      rules: {
        username: [
          { required: true, message: VALIDATION.USERNAME_REQUIRED, trigger: 'blur' },
          { min: 3, max: 20, message: VALIDATION.USERNAME_LENGTH, trigger: 'blur' }
        ],
        email: [
          { required: true, message: VALIDATION.EMAIL_REQUIRED, trigger: 'blur' },
          { type: 'email', message: VALIDATION.EMAIL_INVALID, trigger: 'blur' }
        ],
        code: [{ required: true, message: VALIDATION.CODE_REQUIRED, trigger: 'blur' }],
        password: [
          { required: true, message: VALIDATION.PASSWORD_REQUIRED, trigger: 'blur' },
          { min: 6, max: 20, message: VALIDATION.PASSWORD_LENGTH, trigger: 'blur' }
        ]
      },
      loading: false,
      countdown: 0
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
    async sendCode() {
      if (!this.form.email) {
        this.$message.warning(VALIDATION.EMAIL_REQUIRED)
        return
      }
      try {
        await sendCode(this.form.email, 'REGISTER')
        this.$message.success('验证码已发送')
        this.countdown = 60
        const timer = setInterval(() => {
          this.countdown--
          if (this.countdown <= 0) clearInterval(timer)
        }, 1000)
      } catch (error) {
        // Error handled by interceptor
      }
    },
    handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          await register(this.form)
          this.$message.success(AUTH.REGISTER_SUCCESS)
          this.$router.push('/login')
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
.register-container {
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

.register-card {
  width: 420px;
  padding: 40px;
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: scaleIn 0.3s ease;
}

.register-header {
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

.code-input {
  display: flex;
  gap: 10px;

  .el-input {
    flex: 1;
  }
}

.register-btn {
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

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: var(--text-color-secondary);

  a {
    color: var(--primary-color);
    font-weight: 500;
  }
}

.theme-switch {
  position: absolute;
  top: 16px;
  right: 16px;
}
</style>
