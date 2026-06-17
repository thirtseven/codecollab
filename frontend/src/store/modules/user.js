const state = {
  token: null,
  refreshToken: null,
  user: null
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
  },
  SET_REFRESH_TOKEN(state, refreshToken) {
    state.refreshToken = refreshToken
  },
  SET_USER(state, user) {
    state.user = user
  },
  CLEAR_USER(state) {
    state.token = null
    state.refreshToken = null
    state.user = null
  }
}

const actions = {
  login({ commit }, data) {
    commit('SET_TOKEN', data.token)
    commit('SET_REFRESH_TOKEN', data.refreshToken)
    commit('SET_USER', data.user)
  },
  logout({ commit }) {
    commit('CLEAR_USER')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
