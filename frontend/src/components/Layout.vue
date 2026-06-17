<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo" @click="$router.push('/dashboard')">
        <i class="el-icon-s-platform"></i>
        <span v-show="!isCollapse">CodeCollab</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :background-color="isDarkMode ? '#0f3460' : '#304156'"
        :text-color="isDarkMode ? '#e0e0e0' : '#bfcbd9'"
        :active-text-color="'#409eff'"
        router
        unique-opened
      >
        <el-menu-item index="/dashboard">
          <i class="el-icon-s-home"></i>
          <span>{{ NAV.DASHBOARD }}</span>
        </el-menu-item>
        <el-menu-item index="/teams">
          <i class="el-icon-s-cooperation"></i>
          <span>{{ NAV.TEAMS }}</span>
        </el-menu-item>
        <el-menu-item index="/projects">
          <i class="el-icon-folder-opened"></i>
          <span>{{ NAV.PROJECTS }}</span>
        </el-menu-item>
        <el-menu-item index="/ai">
          <i class="el-icon-chat-dot-round"></i>
          <span>{{ NAV.AI_ASSISTANT }}</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin">
          <i class="el-icon-s-tools"></i>
          <span>{{ NAV.ADMIN }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <i class="el-icon-s-fold collapse-btn" @click="isCollapse = !isCollapse"></i>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip :content="isDarkMode ? '切换亮色模式' : '切换暗色模式'" placement="bottom">
            <el-button circle @click="toggleDarkMode" class="theme-toggle">
              <i :class="isDarkMode ? 'el-icon-sunny' : 'el-icon-moon'"></i>
            </el-button>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="(user && user.avatar) || defaultAvatar">
                {{ user && user.nickname ? user.nickname[0] : 'U' }}
              </el-avatar>
              <span class="username">{{ user ? (user.nickname || user.username) : '' }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i> {{ NAV.PROFILE }}
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <i class="el-icon-switch-button"></i> {{ NAV.LOGOUT }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <transition name="slide-fade" mode="out-in">
          <router-view />
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { NAV } from '@/utils/constants'
import { getUserInfo } from '@/api/auth'

export default {
  name: 'Layout',
  data() {
    return {
      NAV,
      isCollapse: false,
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iIzQwOWVmZiIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1zaXplPSI0MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0id2hpdGUiPlU8L3RleHQ+PC9zdmc+'
    }
  },
  created() {
    if (!this.user) {
      this.fetchUserInfo()
    }
  },
  computed: {
    ...mapState('user', ['user']),
    ...mapState('theme', ['isDarkMode']),
    isAdmin() {
      return this.user && this.user.role === 'ADMIN'
    }
  },
  methods: {
    ...mapMutations('theme', ['TOGGLE_DARK_MODE']),
    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        this.$store.commit('user/SET_USER', res.data)
      } catch (error) {
        // Token invalid, redirect to login
        this.$store.dispatch('user/logout')
        this.$router.push('/login')
      }
    },
    toggleDarkMode() {
      this.TOGGLE_DARK_MODE()
      this.$message({
        message: this.isDarkMode ? '已切换为暗色模式' : '已切换为亮色模式',
        type: 'success',
        duration: 1500
      })
    },
    handleCommand(command) {
      if (command === 'profile') {
        this.$router.push('/profile')
      } else if (command === 'logout') {
        this.$store.dispatch('user/logout')
        this.$router.push('/login')
        this.$message.success('已退出登录')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: var(--sidebar-bg);
  transition: width 0.3s ease;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  i {
    font-size: 24px;
    margin-right: 8px;
  }
}

.header {
  background: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  transition: background-color 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-color);
  transition: color 0.3s ease;

  &:hover {
    color: var(--primary-color);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-toggle {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-color);
  transition: all 0.3s ease;

  &:hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s ease;

  &:hover {
    background: var(--bg-color);
  }
}

.username {
  color: var(--text-color);
  font-size: 14px;
}

.main-content {
  background: var(--bg-color);
  padding: 20px;
  overflow-y: auto;
  transition: background-color 0.3s ease;
}

.el-menu {
  border-right: none;
}
</style>
