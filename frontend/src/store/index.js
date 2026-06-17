import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import user from './modules/user'
import theme from './modules/theme'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    user,
    theme
  },
  plugins: [createPersistedState({
    paths: ['user.token', 'user.user', 'theme.isDarkMode']
  })]
})
