import { defineStore } from 'pinia'
import { postLogin, postLogout, userInfo, userPermissions } from '@/api/main'
import { findDepartAccountTree } from '@/api/accountInfo'
import { enCrypt } from '@/utils'
import router, { dynamicRoutes, notFoundRoute } from '@/router'
import { cloneDeep, debounce } from 'lodash-es'
import { eacSso, TOKEN_KEY, USER_ID_KEY, USER_NAME_KEY } from '@/constant'
import { isDev } from '@/utils/env'

let departAccountTreeInflight: Promise<any[]> | null = null

const normalizeLoginIdentity = (raw: any) => {
  const name = typeof raw?.name === 'string' ? raw.name.trim() : ''
  const employeeId = typeof raw?.employeeId === 'string' ? raw.employeeId.trim() : ''
  const legacyName = typeof raw?.username === 'string' ? raw.username.trim() : ''
  const legacyEmployeeId = typeof raw?.userid === 'string' ? raw.userid.trim() : ''

  return {
    name: name || legacyName,
    employeeId: employeeId || legacyEmployeeId
  }
}

const useUserStore = defineStore('user', {
  state: (): Record<any, any> => ({
    // clientId: '',
    clientId: '',
    clientIds: [],
    defaultClientId: '',
    isAdmin: null,
    menus: [],
    buttonPerm: [],
    menusMap: new Map(),
    homePath: '/',
    username: '',
    userId: '',
    hasLoadedPermissions: false, // 标记是否已加载过权限

    // 部门-账号树缓存（作为 findDepartTree / findDepartAccountTree 的统一数据源）
    departAccountTree: [] as any[],
    departAccountTreeLoading: false
  }),
  getters: {
    getMenus(): Record<any, any>[] {
      return this.menus
    }
  },
  actions: {
    menusMapClear() {
      this.menusMap.clear()
    },
    setCilenId(val: string) {
      this.clientId = val
    },
    /**
     * 登录
     * @param form
     */
    async login(form: Api.User.LoginReq) {
      try {
        const params = Object.assign({}, form, {
          password: enCrypt(form.password),
          username: enCrypt(form.username)
        })
        const loginRes = await postLogin(params).then(res => res.result)
        const identity = normalizeLoginIdentity(loginRes)
        localStorage.setItem(TOKEN_KEY, loginRes.access_token)
        localStorage.setItem(USER_NAME_KEY, identity.name)
        localStorage.setItem(USER_ID_KEY, identity.employeeId)
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
      this.hasLoadedPermissions = false // 重置权限加载标记
      this.menus = [] // 清空菜单
      this.buttonPerm = [] // 清空按钮权限
      this.menusMap.clear() // 清空菜单映射

      this.departAccountTree = []
      this.departAccountTreeLoading = false
      departAccountTreeInflight = null
    },
    linkLogin() {
      this.clearStorage()
      if (isDev()) {
        router.replace('/login')
      } else {
        // router.push('/redirect')
        window.location.href = eacSso
      }
    },
    /**
     * 登出
     * // tip: 无法手动跳转到login页面， 需要执行useUserStore().logout() 方法退出
     */
    logout() {
      const debounceFn = debounce(async () => {
        try {
          await postLogout()
        } catch (err: any) {
          return Promise.resolve()
        } finally {
          this.linkLogin()
        }
      }, 300)
      debounceFn()
    },
    /**
     * 获取用户信息
     */
    async getUserInfo() {
      await userInfo()
    },
    /**
     * 获取用户权限
     */
    async getUserPermissions() {
      const response = await userPermissions()
      const permissionsRes = response.result || {}

      const {
        clientIds,
        menus = [],
        button = [],
        defaultClientId,
        isAdmin,
        name,
        employeeId,
        username,
        userId
      } = permissionsRes

      this.clientIds = clientIds?.details
      this.menus = menus || []
      this.buttonPerm = button || []
      this.defaultClientId = defaultClientId
      this.isAdmin = isAdmin
      this.username = name || username
      this.userId = userId
      this.hasLoadedPermissions = true // 标记已加载过权限

      // 报表页跳过来时没有调用登录接口时兜底使用
      const storedUserName = localStorage.getItem(USER_NAME_KEY)
      const storedUserId = localStorage.getItem(USER_ID_KEY)
      if (!storedUserName && (name || username)) {
        localStorage.setItem(USER_NAME_KEY, name || username)
      }
      if (!storedUserId && (employeeId || userId)) {
        localStorage.setItem(USER_ID_KEY, employeeId || userId)
      }

      this.setCilenId(defaultClientId)

      if (menus?.length) {
        this.menusMap = this.menuToMap(menus)
        this.generateRoutes()
      }

      this.getDepartAccountTree({ force: true })
      return permissionsRes
    },

    /**
     * 获取部门-账号树（带缓存、去重并发）
     */
    async getDepartAccountTree(
      options: { force?: boolean; silent?: boolean } = {}
    ): Promise<any[]> {
      const force = !!options.force
      const silent = options.silent ?? true

      const hasCache = Array.isArray(this.departAccountTree) && this.departAccountTree.length > 0
      if (!force && hasCache) return this.departAccountTree

      if (departAccountTreeInflight) return departAccountTreeInflight

      this.departAccountTreeLoading = true
      departAccountTreeInflight = findDepartAccountTree()
        .then((res: any) => {
          const list = Array.isArray(res?.result) ? res.result : []
          this.departAccountTree = list
          return list
        })
        .catch((e: any) => {
          if (silent) return Array.isArray(this.departAccountTree) ? this.departAccountTree : []
          throw e
        })
        .finally(() => {
          this.departAccountTreeLoading = false
          departAccountTreeInflight = null
        })

      return departAccountTreeInflight
    },

    /**
     * 将菜单树转成map结构
     * @param tree
     */
    menuToMap(tree: any) {
      const map = new Map()
      if (tree?.length === 0) return map

      function traverse(node: any, order: number) {
        node.order = order + 1
        map.set(node.permissionKey, node) // 使用permissionKey作为键
        if (node.children) {
          node.children.forEach((child: any, index: number) => traverse(child, index)) // 递归遍历子节点
        }
      }

      tree.forEach((root: any, index: number) => traverse(root, index)) // 开始遍历根节点
      return map
    },
    /**
     * 获取后端返回菜单的PermissionKey集合，
     */
    getRemoteMenuPermissionKey() {
      return [...this.menusMap.keys()]
    },
    /**
     * 根据后端返回的菜单, 过滤路由表并修改菜单名称和icon
     * 添加排序字段, 按照后端返回的顺序展示
     * @param remoteMenu
     */
    getAsyncRouter(remoteMenu: string[]) {
      const routers = cloneDeep(dynamicRoutes)
      return routers
        .filter((el: any) => {
          if (remoteMenu.includes(el.name)) {
            const curMenu = this.menusMap.get(el.name)
            el.meta.title = curMenu.name
            el.meta.icon = curMenu.icon
            el.order = curMenu.order
            el.children = el.children
              .filter((j: any) => {
                if (remoteMenu.includes(j.name)) {
                  const curJMenu = this.menusMap.get(j.name)
                  j.meta.title = curJMenu.name
                  j.meta.icon = curJMenu.icon

                  j.order = curJMenu.order
                  return j
                }
              })
              .sort((a: any, b: any) => a.order - b.order)
            return el
          }
        })
        .sort((a: any, b: any) => a.order - b.order)
    },
    /**
     * 生成动态菜单
     */
    generateRoutes() {
      const remoteMenu = this.getRemoteMenuPermissionKey()
      const asyncRouter = this.getAsyncRouter(remoteMenu)
      asyncRouter.forEach(route => {
        router.addRoute(route) // 动态添加可访问路由表
      })
      router.addRoute(notFoundRoute)
    },
    /**
     * 根据客户id 获取客户code
     * @param clientId
     */
    getClientCodeByClientId(clientId: string) {
      return this.clientIds.find((el: any) => el.key === clientId)
    }
  }
})

export default useUserStore
