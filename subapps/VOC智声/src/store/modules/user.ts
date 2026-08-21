import { defineStore } from 'pinia'
import { postLogin, postLogout, userInfo, userPermissions } from '@/api/main'
import { enCrypt } from '@/utils/encryption'
// import router, { dynamicRoutes, notFoundRoute } from '@/router'
import router from '@/router'
import { cloneDeep } from 'lodash-es'
import { SSO_URL, TOKEN_KEY, USER_ID_KEY, USER_NAME_KEY } from '@/constants/index'
import type { LoginReq } from '@/api/main/type'
import { notFoundRoute } from '@/router/constantRoutes'
import { dynamicRoutes } from '@/router/dynamicRoutes'
import useQueryStore from './query'
import { sysAllDictItems } from '../../api/common/index.ts'
import { useAppStore } from './app.ts'
import { eacSso } from '@/constants/env.ts'
import { isDev, isLocalDemo } from '@/utils/env.ts'
import { systemAccessDuration } from '@/utils/systemAccessDuration'
import { FunctionPermission } from '@/constants/btnPermMap.ts'
import { createLocalDemoSession, LOCAL_DEMO_TOKEN } from '@/local-demo/session'

export const useUserStore = defineStore('user', {
  state: (): Record<any, any> => ({
    // clientId: '',
    clientId: '',
    clientIds: [],
    defaultClientId: '',
    isAdmin: null,
    buttonPerm: [],
    // 品牌车系
    brands: [],

    // 添加登出状态标识，防止重复调用
    isLoggingOut: false,
    menus: [],
    menusMap: new Map(),
    // 菜单 path -> 菜单 id，用于“菜单/页面访问记录”上报的 id 匹配（PC 统一从这里取值）
    menuPathIdMap: new Map(),
    // 主菜单
    menuList: [],
    // 首页
    homePath: '/',
    // 角色类型: 1 用户, 2 领导
    roleType: undefined,

    //数据字典
    allDictItems: {},
    // 移动端时间选项，用于角色管理中
    timeDimension: [],
    // 角色ID
    roleId: undefined,
    // 操作权限
    functionPermission: []
  }),
  getters: {
    getMenus(): Record<any, any>[] {
      return this.menus
    },
    getBrandService(): any[] {
      return this.brands
    },
    getRoleType(): string {
      return this.roleType
    },
    getMTimeOptions(): any[] {
      return this.timeDimension
    }
  },
  actions: {
    /**
     * 校验操作权限
     * @param permissionKey
     * @returns boolean
     */
    checkfunctionPermission(permissionKey: FunctionPermission) {
      if (isLocalDemo()) return permissionKey !== FunctionPermission.GO_TO_INSIGHTS
      return this.functionPermission?.includes(permissionKey)
    },

    // 设置移动端的时间下拉选项
    setMTimeOptions(options: any[]) {
      this.timeDimension = options
    },
    setCilenId(val: string) {
      this.clientId = val
    },
    /**
     * 登录
     * @param form
     */
    async login(form: LoginReq) {
      try {
        if (isLocalDemo()) {
          localStorage.setItem(TOKEN_KEY, LOCAL_DEMO_TOKEN)
          localStorage.setItem(USER_NAME_KEY, '演示管理员')
          localStorage.setItem(USER_ID_KEY, 'demo-admin')
          this.isLoggingOut = false
          await this.getUserPermissions()
          return Promise.resolve()
        }

        const params = Object.assign({}, form, {
          password: enCrypt(form.password),
          username: enCrypt(form.username)
        })
        const loginRes = await postLogin(params).then(res => res.result)
        localStorage.setItem(TOKEN_KEY, loginRes.access_token)
        localStorage.setItem(USER_NAME_KEY, loginRes.username)
        localStorage.setItem(USER_ID_KEY, loginRes.userid)
        // 登录成功后重置登出状态
        this.isLoggingOut = false
        await this.getUserPermissions()
        return Promise.resolve()
      } catch (e) {
        this.clearStorage()
        return Promise.reject(e)
      }
    },
    clearStorage() {
      localStorage.setItem(TOKEN_KEY, '')
      localStorage.setItem(USER_NAME_KEY, '')
      localStorage.setItem(USER_ID_KEY, '')
    },
    /**
     * 清理用户状态
     */
    clearUserState() {
      // 清理动态路由
      this.clearDynamicRoutes()
      // 清理状态
      this.menus = []
      this.menuList = []
      this.menusMap.clear()
      this.menuPathIdMap.clear()
      this.clientId = ''
      this.clientIds = []
      this.buttonPerm = []
      this.defaultClientId = ''
      this.isAdmin = null
      this.functionPermission = []
      this.homePath = '/'
    },
    /**
     * 清理动态路由
     */
    clearDynamicRoutes() {
      const remoteMenu = this.getRemoteMenuPermissionKey()
      remoteMenu.forEach(name => {
        if (name && !name.startsWith('H5') && router.hasRoute(name)) {
          router.removeRoute(name)
        }
      })
      if (router.hasRoute(notFoundRoute.name as string)) {
        router.removeRoute(notFoundRoute.name as string)
      }
    },
    linkLogin() {
      this.clearStorage()
      if (isDev()) {
        router.replace('/login')
      } else {
        window.location.href = SSO_URL
      }
    },
    /**
     * 主动退出登录后的跳转。
     * - 与无 token / 鉴权失效的 linkLogin 区分，主动退出需要先进入 EAC 登出页清理单点会话
     */
    linkLogout() {
      this.clearStorage()
      if (isDev()) {
        router.replace('/login')
      } else {
        window.location.href = eacSso
      }
    },
    /**
     * 登出
     * // tip: 无法手动跳转到login页面， 需要执行useUserStore().logout() 方法退出
     */
    async logout() {
      // 如果正在登出过程中，直接返回，避免重复调用
      if (this.isLoggingOut) {
        return
      }

      // 设置登出状态标识
      this.isLoggingOut = true

      try {
        // 退出登录前尽力结束一次会话统计，避免 token 清理后无法鉴权上报
        systemAccessDuration.stop('logout')
        await postLogout()
      } catch (err: any) {
        console.warn('登出接口调用失败:', err)
        // 即使接口失败也要继续执行清理操作
      } finally {
        // 清理用户状态
        this.clearUserState()
        this.linkLogout()
        // 注意：不在这里重置 isLoggingOut，因为页面即将跳转到登录页
        // 重置操作放在登录成功后进行
      }
    },
    /**
     * 获取用户信息
     */
    async getUserInfo() {
      await userInfo()
    },
    //获取数据字典
    async getSysAllDictItems() {
      try {
        const { result } = await sysAllDictItems()
        this.allDictItems = result?.sysAllDictItems || {}
      } catch (error) {
        console.error('获取数据字典失败:', error)
        throw error
      }
    },
    getDictItems(key: string): any[] {
      return this.allDictItems?.[key] || []
    },
    setRoleType(menus: any[]) {
      const result = menus?.find(el => el.roleType)
      this.roleType = result?.roleType
    },
    async getUserPermissions(): Promise<any> {
      try {
        const queryStore = useQueryStore()
        const appStore = useAppStore()
        const result = isLocalDemo()
          ? createLocalDemoSession()
          : await userPermissions().then(response => response.result)
        const {
          clientIds,
          menus = [],
          button = [],
          defaultClientId,
          isAdmin,
          brands,
          timeDimension,
          username,
          userId,
          functionPermission,
          roleId,
          name
        } = result || {}

        // 设置用户信息
        this.clientIds = clientIds?.details || []
        this.buttonPerm = button || []
        this.defaultClientId = defaultClientId
        this.isAdmin = isAdmin
        this.functionPermission = functionPermission || []
        this.roleId = roleId
        if (defaultClientId) this.setCilenId(defaultClientId)
        // 品牌车系
        this.brands = brands?.details
        appStore.setUser({
          userName: username,
          name: name,
          id: userId
        })

        // PC 端登录成功后立即开始统计访问时长（不依赖页面交互）
        systemAccessDuration.start({
          device: 'pc',
          userId: userId,
          username: username
        })
        // 设置角色类型
        this.setRoleType(menus)

        this.setMTimeOptions(timeDimension)

        // 获取默认选项
        queryStore.setDefaultJsonObjectMap(menus)

        // 处理菜单和路由
        const transformedMenus = this.transformMenus(menus)
        this.menus = transformedMenus || []

        // 重新构建 permissionKey -> menu 的映射，避免权限为空时残留旧数据
        this.menusMap = this.menuToMap(transformedMenus || [])
        // 基于后端 menus.path 构建 path -> id 的映射（与 menusMap 同一套数据源）
        this.menuPathIdMap = this.menuPathToMap(transformedMenus || [])
        if (this.menusMap?.size) {
          this.generateRoutes()
        } else {
          this.menuList = []
          this.menuPathIdMap.clear()
        }
        //获取数据字典
        if (isLocalDemo()) {
          this.allDictItems = result.allDictItems || {}
        } else {
          this.getSysAllDictItems()
        }

        return result
      } catch (error) {
        console.error('获取用户权限失败:', error)
        this.menus = []
        this.menusMap.clear()
        this.menuPathIdMap.clear()
        throw error
      }
    },
    /**
     * @description: 设置菜单列表，使用权限过滤后的路由
     * @return {*}
     */
    setMenuList(asyncRoutes: any[]) {
      this.menuList = asyncRoutes.filter(route => !route.meta?.hidden)

      // 递归处理菜单，将 permissionKey === 'linkUrl' 的菜单转换为路由格式
      const processLinkMenus = (menus: any[]): any[] => {
        const result: any[] = []
        menus.forEach((menu: any) => {
          if (menu.permissionKey === 'linkUrl') {
            const menuItem: any = {
              // 补齐菜单ID，供“操作记录”上报使用
              id: menu.id || '',
              path: menu.path || '#',
              name: menu.permissionKey,
              meta: {
                title: menu.name,
                icon: menu.icon,
                isExternal: true,
                externalUrl: menu.url
              },
              order: menu.sort || menu.order || 0
            }
            // 处理子菜单
            if (menu.children?.length) {
              menuItem.children = processLinkMenus(menu.children)
            }
            result.push(menuItem)
          }
        })
        return result
      }

      // 将 linkUrl 子菜单合并到已有路由的 children 中
      this.menus.forEach((menu: any) => {
        const existingRoute = this.menuList.find((r: any) => r.name === menu.permissionKey)
        if (existingRoute && menu.children?.length) {
          const linkChildren = processLinkMenus(menu.children)
          if (linkChildren.length > 0) {
            existingRoute.children = existingRoute.children || []
            existingRoute.children.push(...linkChildren)
            // 合并后按 order 排序
            existingRoute.children.sort((a: any, b: any) => (a.order || 0) - (b.order || 0))
          }
        }
      })

      const linkUrlMenus = processLinkMenus(this.menus)
      this.menuList.push(...linkUrlMenus)

      this.menuList.sort((a: any, b: any) => (a.order || 0) - (b.order || 0))

      if (this.menuList[0]?.redirect) {
        this.homePath = this.menuList[0].redirect
      } else if (this.menuList[0]?.children?.[0]?.path) {
        this.homePath = this.menuList[0].children[0].path
      }
    },
    /**
     * 根据后端返回的菜单, 过滤路由表并修改菜单名称和icon
     * 添加排序字段, 按照后端返回的顺序展示
     * @param remoteMenu
     */
    getAsyncRouter(remoteMenu: string[]) {
      const routers = cloneDeep(dynamicRoutes)

      const filteredRoutes = routers.filter((el: any) => {
        if (remoteMenu.includes(el.name)) {
          const curMenu = this.menusMap.get(el.name)
          if (curMenu) {
            // 补齐菜单ID，供“操作记录”上报使用
            el.id = curMenu.id || ''
            el.meta.title = curMenu.name
            el.meta.icon = curMenu.icon
            el.order = curMenu.sort || curMenu.order || 0
          }

          if (el.children && el.children.length > 0) {
            const filteredChildren = el.children.filter((j: any) => {
              if (remoteMenu.includes(j.name)) {
                const curJMenu = this.menusMap.get(j.name)
                if (curJMenu) {
                  // 补齐菜单ID，供“操作记录”上报使用
                  j.id = curJMenu.id || ''
                  j.meta.title = curJMenu.name
                  j.meta.icon = curJMenu.icon
                  j.order = curJMenu.sort || curJMenu.order || 0
                }
                return true
              }
              return false
            })

            if (filteredChildren.length === 0 && el.meta?.alwaysShow) {
              // 对于 alwaysShow 的菜单，保留原有的子路由
            } else if (el.name === 'sceneAnalysis') {
              // 对于场景分析，始终包含主页面路由（不需要单独权限）
              const mainRoute = el.children.find((child: any) => child.name === 'sceneAnalysisMain')
              if (mainRoute) {
                // 将主页面路由放在第一位
                const allChildren = [mainRoute, ...filteredChildren]
                el.children = allChildren.sort((a: any, b: any) => (a.order || 0) - (b.order || 0))
              } else {
                el.children = filteredChildren.sort(
                  (a: any, b: any) => (a.order || 0) - (b.order || 0)
                )
              }
            } else {
              el.children = filteredChildren.sort(
                (a: any, b: any) => (a.order || 0) - (b.order || 0)
              )
            }
          }
          return true
        }
        return false
      })

      return filteredRoutes.sort((a: any, b: any) => (a.order || 0) - (b.order || 0))
    },
    /**
     * 生成动态路由
     */
    generateRoutes() {
      if (!this.menusMap?.size) return

      const remoteMenu = this.getRemoteMenuPermissionKey()
      const asyncRouter = this.getAsyncRouter(remoteMenu)

      // 清理旧路由
      this.clearDynamicRoutes()
      if (router.hasRoute('Root')) router.removeRoute('Root')

      // 添加新路由
      asyncRouter.forEach(route => router.addRoute(route))

      // 确保404路由在最后，且不会影响H5路由
      if (router.hasRoute(notFoundRoute.name as string)) {
        router.removeRoute(notFoundRoute.name as string)
      }

      router.addRoute(notFoundRoute)

      this.setMenuList(asyncRouter)
    },
    /**
     * 根据路径获取菜单 ID（PC 端“访问记录”统一使用该方式匹配）
     */
    getMenuIdByPath(path: string) {
      const key = path
      if (!key) return ''
      return this.menuPathIdMap.get(key) || ''
    },
    /**
     * 判断是否为“首页路径”
     * - 用于避免首页上报与菜单点击上报重复
     */
    isHomePath(path: string) {
      const homeKey = this.homePath || '/'
      const key = path
      if (!homeKey || !key) return false
      // 兼容直接传入 / 的场景（常见于重定向入口）
      if (key === '/') return true
      return key === homeKey
    },
    /**
     * 将菜单树转成map结构
     */
    menuToMap(tree: any[]) {
      const map = new Map()
      if (!tree?.length) return map

      const traverse = (node: any) => {
        node.order = node.sort || 0
        map.set(node.permissionKey, node)
        node.children?.forEach(traverse)
      }

      tree.forEach(traverse)
      return map
    },
    /**
     * 将菜单树转成 path -> id 的 map（与 menuToMap 保持一致的构建方式）
     */
    menuPathToMap(tree: any[]) {
      const map = new Map<string, string>()
      if (!tree?.length) return map

      const traverse = (node: any) => {
        const key = node?.path || node?.htmlUri
        const id = node?.id === undefined || node?.id === null ? '' : String(node.id)

        // 同一路径重复时：保留首次出现的非空 id，避免上报抖动
        if (key && id && !map.has(key)) {
          map.set(key, id)
        }

        node.children?.forEach(traverse)
      }

      tree.forEach(traverse)
      return map
    },
    /**
     * 处理后端返回的菜单数据（递归处理所有层级）
     */
    transformMenus(menus: any[]) {
      const transform = (menu: any): any => ({
        ...menu,
        permissionKey: menu.permissionKey || menu.name,
        children: menu.children?.map(transform)
      })
      return menus?.map(transform)
    },
    /**
     * 获取后端返回菜单的PermissionKey集合，
     */
    getRemoteMenuPermissionKey() {
      return [...this.menusMap.keys()]
    }
  },
  persist: {
    key: 'userStore',
    pick: ['allDictItems']
  }
})

export default useUserStore
