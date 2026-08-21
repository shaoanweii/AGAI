import router, { dynamicRoutes, notFoundRoute } from './router'
import useUserStore from '@/stores/modules/user'
import axios from 'axios'
import { eacSso, TOKEN_KEY, VERSION_KEY } from './constant'
import { isDev } from './utils/env'

// 白名单
const whiteList = ['/500', '/404', '/403', '/redirect']

const isRefresh = import.meta.env.VITE_REFRESH === 'true'

// AGAI 门户作为唯一登录入口：从门户进入时在 standalone 模式自动建立洞察引擎会话。
const isAgaiEntry = new URLSearchParams(window.location.search).get('agai') === '1'
let agaiLoginPromise: Promise<void> | null = null

const ensureAgaiSession = () => {
  if (!agaiLoginPromise) {
    agaiLoginPromise = useUserStore()
      .login({
        username: 'admin',
        password: 'Passw0rd@!',
        captcha: '2587',
        checkKey: 'agai-portal'
      })
      .then(() => undefined)
      .finally(() => {
        agaiLoginPromise = null
      })
  }
  return agaiLoginPromise
}

const bootstrapStaticAgaiSession = () => {
  localStorage.setItem(TOKEN_KEY, 'agai-portal-token')
  const userStore = useUserStore()
  const menus = dynamicRoutes.map((route: any) => ({
    permissionKey: route.name,
    name: route.meta?.title,
    icon: route.meta?.icon,
    children: (route.children || []).map((child: any) => ({
      permissionKey: child.name,
      name: child.meta?.title,
      icon: child.meta?.icon
    }))
  }))
  userStore.username = 'admin'
  userStore.userId = 'admin'
  userStore.menus = menus
  userStore.menusMap = userStore.menuToMap(menus)
  userStore.hasLoadedPermissions = true
}

router.beforeEach((to, from, next) => {
  // debugger
  // 静态同域子应用每次刷新都会重建 Pinia；即使 localStorage 已有门户令牌，也要同步恢复菜单，避免误请求独立权限接口。
  if (isAgaiEntry && import.meta.env.MODE === 'agai') {
    const userStore = useUserStore()
    if (!userStore.getMenus?.length) bootstrapStaticAgaiSession()
  }
  const token = localStorage.getItem(TOKEN_KEY)
  // 检查版本更新
  if (from.path !== '/' && isRefresh) {
    checkAppNewVersion()
  }

  // 判断是访问登陆页，有 Token 就在当前页面，没有 Token 重置路由到登陆页
  if (to.path.toLocaleLowerCase() === '/login') {
    if (token) return next(from.fullPath)
    // 没有token 就重置路由
    resetRouter()
    // 放行去登录页
    return next()
  }
  // 判断访问页面是否在路由白名单地址(静态路由)中，如果存在直接放行
  if (whiteList.includes(to.path)) return next()

  // 4.判断是否有 Token，没有重定向到 login 页面
  // if (!token) return next({ path: '/login', replace: true })
  if (!token) {
    if (isAgaiEntry && import.meta.env.MODE === 'agai') {
      // 同域静态子应用由 AGAI 登录门户授权，不再请求原登录页。
      bootstrapStaticAgaiSession()
      return next({ ...to, replace: true })
    }
    if (isAgaiEntry && import.meta.env.MODE === 'standalone') {
      ensureAgaiSession()
        .then(() => next({ ...to, replace: true }))
        .catch(() => {
          window.parent.location.href = '/workbench'
        })
      return
    }
    if (isDev()) {
      return next({ path: '/login', replace: true })
    } else {
      window.location.href = eacSso
      return
    }
  }

  const userStore = useUserStore()
  // 判断 store 里面的设置的菜单列表是否为空，为空就重新获取列表添加路由
  if (!userStore.getMenus?.length) {
    // 如果已经加载过权限但没有菜单，说明用户没有权限，跳转到redirect页面
    if (userStore.hasLoadedPermissions) {
      return next({ path: '/redirect', replace: true })
    }

    userStore
      .getUserPermissions()
      .then((res: any) => {
        // 如果没有菜单权限，跳转到redirect页面
        if (!res.menus || res.menus.length === 0) {
          next({ path: '/redirect', replace: true })
        } else {
          next({ ...to, replace: true })
        }
      })
      .catch(() => {
        userStore.linkLogin()
        next()
      })
  } else {
    next()
  }
})

router.onError((err: any) => {
  console.log('err', err)
})

/**
 * 重置路由
 */
const resetRouter = () => {
  const userStore = useUserStore()
  const rempteMenu = userStore.getRemoteMenuPermissionKey()
  const resultRouter = [...(rempteMenu || []), notFoundRoute.name]
  resultRouter?.forEach((name: string) => {
    if (name && router.hasRoute(name)) router.removeRoute(name)
  })
  userStore.menusMapClear()
}

// 检查服务端是否已经更新，如果更新刷新页面
async function checkAppNewVersion() {
  const url = `/ins/version.json?t=${Date.now()}`
  let res = null
  try {
    res = await axios.get(url)
  } catch (err) {
    console.error('checkAppNewVersion error: ', err)
  }
  if (!res) return
  const version = (res as any).data.version
  const localVersion = localStorage.getItem(VERSION_KEY)
  if (localVersion && localVersion !== version) {
    localStorage.setItem(VERSION_KEY, version)
    window.location.reload()
  }
  localStorage.setItem(VERSION_KEY, version)
}

// 监听页面打开显示
document.addEventListener('visibilitychange', function () {
  if (!document.hidden && isRefresh) {
    checkAppNewVersion()
  }
})
