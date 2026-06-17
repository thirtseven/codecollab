<template>
  <div id="app" :class="{ 'dark-mode': isDarkMode }">
    <router-view />
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'App',
  computed: {
    ...mapState('theme', ['isDarkMode'])
  },
  created() {
    const savedTheme = localStorage.getItem('darkMode')
    if (savedTheme === 'true') {
      this.$store.commit('theme/SET_DARK_MODE', true)
    }
  }
}
</script>

<style lang="scss">
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.dark-mode {
  background-color: #1a1a2e;
  color: #e0e0e0;
}
</style>
