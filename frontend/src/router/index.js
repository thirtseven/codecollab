import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'teams',
        name: 'Teams',
        component: () => import('@/views/team/List.vue'),
        meta: { title: '团队管理' }
      },
      {
        path: 'teams/:id',
        name: 'TeamDetail',
        component: () => import('@/views/team/Detail.vue'),
        meta: { title: '团队详情' }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/List.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/Detail.vue'),
        meta: { title: '项目详情' }
      },
      {
        path: 'code/:projectId',
        name: 'CodeEditor',
        component: () => import('@/views/code/Editor.vue'),
        meta: { title: '代码编辑器' }
      },
      {
        path: 'ai',
        name: 'AiAssistant',
        component: () => import('@/views/ai/Chat.vue'),
        meta: { title: 'AI 助手' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/Index.vue'),
        meta: { title: '系统管理', requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/auth/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - CodeCollab`
  }
  if (to.meta.requiresAdmin) {
    const store = require('@/store').default
    if (store.state.user.user && store.state.user.user.role === 'ADMIN') {
      next()
    } else {
      next('/dashboard')
    }
  } else {
    next()
  }
})

export default router
