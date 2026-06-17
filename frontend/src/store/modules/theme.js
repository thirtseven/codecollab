const state = {
  isDarkMode: false
}

const mutations = {
  SET_DARK_MODE(state, value) {
    state.isDarkMode = value
    localStorage.setItem('darkMode', value)
    document.documentElement.setAttribute('data-theme', value ? 'dark' : 'light')
  },
  TOGGLE_DARK_MODE(state) {
    state.isDarkMode = !state.isDarkMode
    localStorage.setItem('darkMode', state.isDarkMode)
    document.documentElement.setAttribute('data-theme', state.isDarkMode ? 'dark' : 'light')
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
