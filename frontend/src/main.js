import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './assets/styles/global.scss'
import './assets/styles/transitions.scss'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

Vue.use(ElementUI, { size: 'medium' })
Vue.config.productionTip = false

router.beforeEach((to, from, next) => {
  NProgress.start()
  const token = store.state.user.token
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
