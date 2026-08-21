/**
 * H5 权限管理 Store
 * 管理 H5 页面的权限状态和权限检查逻辑
 */

import { defineStore } from 'pinia'
import { useH5AppStore } from './h5App'
import { getAuthDataUrl } from '../api/common'
import { userPermissions as fetchH5UserPermissions } from '../api/home'
import type { HttpRequestConfig } from '../api/http'
import type { BrandItem } from '@views/H5/api/brand/types.d'
import { showToast } from 'vant'
import { FunctionPermission } from '@/constants/btnPermMap'

/** 条件详情 */
export interface ConditionDetailsVo {
  key: string
  value: string
  code: string
  img: string
  startThresholdValue: string
  sort: number
  endThresholdValue: string
  children?: ConditionDetailsVo[]
}

/** 条件对象 */
export interface ConditionVo {
  key: string
  details: ConditionDetailsVo[]
}

/** 时间维度 */
export interface TimeDimensionVo {
  /** 时间维度名称 */
  name: string
  /** 时间维度编码 */
  code: number
  /** 子时间维度 */
  child?: TimeDimensionVo[]
}

/** H5 权限菜单项 */
export interface H5PermissionMenuItem {
  permissionKey?: string
  children?: H5PermissionMenuItem[]
  [key: string]: any
}

interface PermissionsState {
  // 用户信息字段
  advanced: object[] | null
  brands: ConditionVo | null
  appTagsMobile: ConditionDetailsVo[]
  timeDimension: TimeDimensionVo[]
  functionPermission: string[]
  executivePermission: boolean
  // 是否已完成权限初始化（用于区分“未加载”与“加载后但无数据”）
  hasInited?: boolean
  menus?: any[]
  username?: string
  userId?: string
  deptName?: string
  deptId?: string
  deptCode?: string
  finalDeptCode?: string
  finalDeptId?: string
  finalDeptName?: string
  // 开启任务的权限
  openTaskPermission?: boolean
  // 开启看数广场权限
  hasDataSquarePermission?: boolean
}

/**
 * 检查菜单树中是否存在指定权限。
 * @param menus 权限菜单树
 * @param permissionKey 菜单权限标识
 * @returns 是否拥有该菜单权限
 */
const containsMenuPermission = (menus: H5PermissionMenuItem[], permissionKey: string): boolean => {
  if (!permissionKey) return false

  return menus.some(menu => {
    if (menu.permissionKey === permissionKey) return true
    if (menu.children?.length) return containsMenuPermission(menu.children, permissionKey)
    return false
  })
}

/**
 * H5 userPermissions 接口返回中与权限 Store 相关的字段
 */
interface H5UserPermissionsPayload {
  advanced?: object[] | null
  brands?: ConditionVo | null
  appTagsMobile?: ConditionDetailsVo[]
  timeDimension?: TimeDimensionVo[]
  executivePermission?: boolean
  functionPermission?: string[]
  menus?: any[]
  username?: string
  userId?: string
  deptName?: string
  deptId?: string
  deptCode?: string
  finalDeptCode?: string
  finalDeptId?: string
  finalDeptName?: string
  openTaskPermission?: boolean
  hasDataSquarePermission?: boolean
}

interface InitPermissionsOptions {
  silentError?: boolean
  requestConfig?: HttpRequestConfig
}

// H5 权限初始化请求单例
// - 避免多个页面并发调用时产生重复请求
// - 仅作为运行时变量存在，不写入 Pinia state
let initPromise: Promise<void> | null = null

export const usePermissionsStore = defineStore('h5-permissions', {
  state: (): PermissionsState => ({
    // 用户信息字段
    advanced: null,
    brands: null,
    appTagsMobile: [],
    timeDimension: [],
    functionPermission: [],
    // 高管任务权限
    executivePermission: false,
    // 权限初始化标记
    hasInited: false,
    // 工号
    username: undefined,
    userId: undefined,
    deptName: undefined,
    deptId: undefined,
    deptCode: undefined,
    finalDeptCode: undefined,
    finalDeptId: undefined,
    finalDeptName: undefined,
    // 开启任务的权限
    openTaskPermission: false,
    // 开启看数广场权限
    hasDataSquarePermission: false
  }),

  getters: {
    // 高级筛选配置，未初始化时返回空数组，避免组件直接使用时报错
    getAdvanced(): any[] {
      return Array.isArray(this.advanced) ? this.advanced : []
    },
    // 品牌列表（仅保留 H5 首页所需字段）
    getBrandListForHome(): BrandItem[] {
      const details = this.brands?.details || []
      return details.map(item => ({
        key: item.key,
        value: item.value,
        img: item.img
      }))
    },
    // 时间维度列表，这里保持为 any[]，方便在各页面中按自身类型约束使用
    getTimeDimensionList(): any[] {
      return this.timeDimension || []
    },
    // H5 场景选项（服务/产品等），转换为前端 HSwitchButton 可直接使用的结构
    getSceneOptions(): { label: string; value: string }[] {
      return (this.appTagsMobile || []).map(item => ({
        label: item.value || '',
        value: item.key || ''
      }))
    },
    /**
     * 判断当前用户是否拥有指定菜单权限。
     * @returns 权限判断函数
     */
    hasMenuPermission(): (permissionKey: string) => boolean {
      return (permissionKey: string) => {
        return containsMenuPermission((this.menus || []) as H5PermissionMenuItem[], permissionKey)
      }
    }
  },

  actions: {
    /**
     * 初始化并拉取 H5 用户权限信息
     * - 调用 /report/userPermissions 接口
     * - 将结果写入当前 Store
     * - 支持多页面并发调用时复用同一个请求，避免重复发起
     * @param force 是否强制刷新，true 时无视本地已缓存结果
     * @returns 初始化完成后 resolve；失败会抛出异常（内部已 Toast 提示）
     */
    async initUserPermissions(force = false, options: InitPermissionsOptions = {}): Promise<void> {
      // 已初始化且不强制刷新时，直接复用本地状态，避免重复请求
      if (
        !force &&
        (this.brands ||
          (this.timeDimension && this.timeDimension.length > 0) ||
          this.appTagsMobile.length > 0)
      )
        return

      // 存在进行中的初始化请求且不强制刷新时，复用同一个 Promise
      if (initPromise && !force) {
        return initPromise
      }

      // 发起新的初始化请求
      initPromise = (async () => {
        try {
          const res = await fetchH5UserPermissions(options.requestConfig)

          // 兜底：接口 code 可能为 200，但 success=false / result 为空时，不会走 axios 拦截器的 reject
          // 这种“静默失败”场景需要显式提示，否则页面只会出现空数据，问题难定位
          if (!res?.success || !res?.result) {
            const message = res?.message || '获取权限信息失败，请稍后重试'
            if (!options.silentError) {
              showToast(message)
            }
            throw new Error(message)
          }

          this.setPermissionsInfo(res.result as H5UserPermissionsPayload)
          return
        } finally {
          initPromise = null
        }
      })()

      return initPromise
    },

    /**
     * 设置用户信息
     */
    setPermissionsInfo(info: H5UserPermissionsPayload) {
      const h5AppStore = useH5AppStore()

      // 按字段白名单方式更新，避免接口多余字段污染 Store 结构
      this.advanced = info.advanced ?? []
      this.brands = info.brands ?? null
      this.appTagsMobile = info.appTagsMobile ?? []
      this.timeDimension = info.timeDimension ?? []
      this.functionPermission = info.functionPermission ?? []
      // this.executivePermission = !!info.executivePermission
      // 高管任务的权限从functionPermission的数组中去获取
      this.executivePermission = this.checkfunctionPermission(FunctionPermission.EXECUTIVE_TASK)
      this.menus = info.menus ?? []
      this.username = info.username
      this.userId = info.userId
      this.deptName = info.deptName
      this.deptId = info.deptId
      this.deptCode = info.deptCode
      this.finalDeptCode = info.finalDeptCode
      this.finalDeptId = info.finalDeptId
      this.finalDeptName = info.finalDeptName
      this.hasInited = true
      this.openTaskPermission = this.hasMenuPermission('H5TaskEvent')
      this.hasDataSquarePermission = this.hasMenuPermission('H5DataPlaza')

      // 根据菜单与时间维度配置，生成默认时间/品牌等信息
      h5AppStore.setDefaultJsonObject(this.menus || [], this.timeDimension || [])
    },
    /**
     * 校验 H5 操作权限，仅使用 H5 权限接口返回的 functionPermission。
     * @param permissionKey 操作权限 key
     * @returns 当前 H5 用户是否拥有该操作权限
     */
    checkfunctionPermission(permissionKey: FunctionPermission) {
      return this.functionPermission.includes(permissionKey)
    },
    // 处理 Canswer认证以后跳转
    async handleCanswerAuth(userNo?: string) {
      let _userCode = this.username
      // const _userCode = '6323004'
      if (userNo) {
        _userCode = userNo
      }
      try {
        const res = await getAuthDataUrl({ userCode: _userCode, source: 'h5' })
        return res
      } catch (error) {
        console.log('getAuthDataUrl-->error', error)
        return {}
      }
    }
  }
})
