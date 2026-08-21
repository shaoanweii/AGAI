import router from './router'
import { notFoundRoute } from './router/constantRoutes'
import { h5RouteGuards } from '@/views/H5/router/index'
import useUserStore from '@/store/modules/user'
import { TOKEN_KEY } from './constants'
import { isDev } from './utils/env'
import { useH5ssoStore } from './views/H5/store/sso'
import { PAGE_TITLE } from './views/H5/constants'
import { isLocalDemo } from './utils/env'
import { LOCAL_DEMO_TOKEN } from './local-demo/session'

// 不需要登录的页面（白名单）
const LOGIN_ROUTER = '/login'
const whiteList = ['/404', '/redirect', '/h5Rct']

/**
 * 重置路由
 */
const resetRouter = () => {
  const userStore = useUserStore()
  userStore.clearUserState()
}

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // H5路由特殊处理
  if (to.path.startsWith('/h5')) {
    if (isLocalDemo()) {
      const queryToken = typeof to.query.token === 'string' ? to.query.token : ''
      localStorage.setItem(TOKEN_KEY, queryToken || LOCAL_DEMO_TOKEN)
    }
    // document.title = 'VOC任务管理'
    document.title = PAGE_TITLE
    h5RouteGuards.beforeEnter(to, from, next)
    return
  }

  // 设置标题
  // document.title = 'VOC数智平台'
  document.title = 'VOC智声'

  const token = localStorage.getItem(TOKEN_KEY)

  // 判断是访问登陆页，有 Token 就在当前页面，没有 Token 重置路由到登陆页
  if (to.path.toLocaleLowerCase() === LOGIN_ROUTER) {
    if (token) {
      return next(from.fullPath)
    }
    resetRouter()
    return next()
  }

  // 判断访问页面是否在路由白名单地址(静态路由)中，如果存在直接放行
  if (whiteList.includes(to.path)) {
    return next()
  }

  // 判断是否有 Token，没有重定向到 login 页面
  if (!token) {
    // const ssoStore = useH5ssoStore()
    const userStore = useUserStore()
    if (isDev()) {
      return next({ path: LOGIN_ROUTER, replace: true })
    } else {
      // ssoStore.linkLogOut()
      userStore.linkLogin()
      return
    }
  }

  const userStore = useUserStore()
  // 判断是否需要初始化权限和路由
  if (!userStore.getMenus?.length) {
    userStore
      .getUserPermissions()
      .then(() => {
        // 权限获取成功后，重新导航到目标路由
        next({ ...to, replace: true })
      })
      .catch(() => userStore.linkLogin())
  } else {
    next()
  }
})
