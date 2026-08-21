/**
 * 处理sso登录的逻辑
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
// 引入移动端容器 SDK；本地模式由中性适配器替代
import SDK from '@/integrations/mobile-container'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getTokenByUserAccount } from '@/api/common'
import { enCrypt } from '@/utils/encryption'
import {
  getCurrentEnvironment,
  isMobileEnvironment,
  isWeWorkEnvironment,
  isValidToken,
  setToken,
  removeToken
} from '@/utils'
import { showToast } from 'vant'
import { SSO_URL, TOKEN_KEY } from '@/constants'
import { ElMessage } from 'element-plus'
import { eacSso, qxAgentId, qxMode } from '@/constants/env'
import { systemAccessDuration } from '@/utils/systemAccessDuration'
import { usePermissionsStore } from './permissions'
// import { initWeWork } from '@h5/utils/weWork'

const H5_SSO_REDIRECT_STATE_KEY = 'h5_sso_redirect_state'
const H5_SSO_REDIRECT_STATE_MAX_AGE = 5 * 60 * 1000

type H5SsoRedirectQuery = Record<string, string | string[]>

interface H5SsoRedirectState {
  createdAt: number
  query: H5SsoRedirectQuery
}

/**
 * 判断是否为可恢复的 PDF 消息落地参数。
 * PDF 消息不携带普通 H5 路由使用的 path，需要通过 voctype 和 pdfUrl 单独识别。
 */
function isVocPdfRedirectQuery(query: Record<string, unknown>): boolean {
  return query.voctype === 'voc' && typeof query.pdfUrl === 'string' && Boolean(query.pdfUrl.trim())
}

/**
 * 判断 query 是否包含可跨 SSO 恢复的 H5 目标。
 */
function hasRestorableH5Target(query: Record<string, unknown>): boolean {
  const hasTargetPath = typeof query.path === 'string' && Boolean(query.path.trim())
  return hasTargetPath || isVocPdfRedirectQuery(query)
}

/**
 * 将路由 query 规范化为可跨 SSO 登录页保存的业务参数。
 * token 由回跳时重新签发，source 仅由前端当前环境决定，二者不参与恢复。
 */
function normalizeH5SsoRedirectQuery(query: Record<string, unknown>): H5SsoRedirectQuery {
  return Object.entries(query).reduce<H5SsoRedirectQuery>((result, [key, value]) => {
    if (key === 'token' || key === 'source' || value == null) return result

    if (Array.isArray(value)) {
      const values = value.filter((item): item is string => typeof item === 'string')
      if (values.length) result[key] = values
      return result
    }

    if (typeof value === 'string') result[key] = value
    return result
  }, {})
}

export const useH5ssoStore = defineStore('h5sso', () => {
  // 企微用户信息
  const qxUserInfo = ref<any>()
  // 重试计数器，防止无限刷新
  const retryCount = ref(0)
  const MAX_RETRY = 3
  const RESUME_SESSION_CHECK_INTERVAL = 1500
  const router = useRouter()
  const userStore = useUserStore()
  const _token = ref<string | null>(null)
  const lastResumeSessionCheckAt = ref(0)
  let resumeSessionCheckPromise: Promise<void> | null = null

  /**
   * @description: 设置token
   * @param {string} token
   * @return {*}
   */
  const setStoreToken = (token: string) => {
    if (!isValidToken(token)) {
      clearToken()
      return
    }

    _token.value = token
  }

  /**
   * @description: 清空token
   * @return {*}
   */
  const clearToken = () => {
    console.log('清空token')
    _token.value = null
    localStorage.removeItem(TOKEN_KEY)
    removeToken()
  }

  /**
   * 保存消息推送入口参数，以便第三方登录页未透传 query 时恢复 H5 目标页面。
   */
  const saveH5SsoRedirectState = (query: Record<string, unknown>) => {
    const normalizedQuery = normalizeH5SsoRedirectQuery(query)
    if (!hasRestorableH5Target(normalizedQuery)) return

    const state: H5SsoRedirectState = {
      createdAt: Date.now(),
      query: normalizedQuery
    }

    try {
      sessionStorage.setItem(H5_SSO_REDIRECT_STATE_KEY, JSON.stringify(state))
    } catch (error) {
      console.warn('保存 H5 SSO 回跳参数失败:', error)
    }
  }

  /**
   * 清除已经完成或失效的 H5 SSO 回跳参数。
   */
  const clearH5SsoRedirectState = () => {
    try {
      sessionStorage.removeItem(H5_SSO_REDIRECT_STATE_KEY)
    } catch (error) {
      console.warn('清除 H5 SSO 回跳参数失败:', error)
    }
  }

  /**
   * 消费第三方登录前保存的 H5 推送参数。
   * 读取后、超过五分钟或格式异常时都会清除，避免旧消息参数影响后续登录。
   */
  const consumeH5SsoRedirectState = (): H5SsoRedirectQuery | null => {
    try {
      const rawState = sessionStorage.getItem(H5_SSO_REDIRECT_STATE_KEY)
      if (!rawState) return null

      const state = JSON.parse(rawState) as H5SsoRedirectState
      const isExpired =
        !Number.isFinite(state.createdAt) ||
        Date.now() - state.createdAt > H5_SSO_REDIRECT_STATE_MAX_AGE
      const hasTarget = Boolean(state.query) && hasRestorableH5Target(state.query)

      if (isExpired || !hasTarget) return null

      return state.query
    } catch (error) {
      console.warn('读取 H5 SSO 回跳参数失败:', error)
      return null
    } finally {
      clearH5SsoRedirectState()
    }
  }

  // 跳转到PC首页
  const linkPCHome = () => {
    router.replace({
      path: userStore.homePath
    })
  }

  /**
   * 跳转到 VOC PDF 预览页面，并透传消息业务参数。
   * @param sourceQuery 指定的消息参数，缺省时使用当前路由 query
   */
  const linkVocViewPage = (
    sourceQuery: Record<string, unknown> = router.currentRoute.value?.query || {}
  ) => {
    const forwardedQuery: Record<string, string> = { redirect: 'true' }
    Object.keys(sourceQuery).forEach(key => {
      if (key === 'path' || key === 'token') return
      const value = sourceQuery[key]
      if (value) forwardedQuery[key] = String(value)
    })

    clearH5SsoRedirectState()
    router.replace({
      path: '/h5PdfView',
      query: forwardedQuery
    })
  }

  // 跳转到移动端首页
  const linkHome = () => {
    const routeQuery = (router.currentRoute.value?.query || {}) as Record<string, unknown>
    const savedQuery = consumeH5SsoRedirectState()
    // 推送参数缓存记录的是跳转 SSO 前的完整目标，优先用于恢复第三方登录后的路由。
    const currentQuery = savedQuery || routeQuery

    if (isVocPdfRedirectQuery(currentQuery)) {
      linkVocViewPage(currentQuery)
      return
    }

    const targetKey = typeof currentQuery.path === 'string' ? currentQuery.path : ''

    // 透传 query：仅保留基础类型，避免把 path/token 再带到目标页
    const forwardedQuery: Record<string, any> = { redirect: true }
    Object.keys(currentQuery).forEach(key => {
      if (key === 'path' || key === 'token') return
      const value = currentQuery[key] || ''
      if (value) forwardedQuery[key] = String(value)
    })

    if (!targetKey) {
      router.replace({ path: '/h5/home' })
      return
    }

    // 统一走 /h5 下的子路由
    router.replace({
      path: `/h5/${targetKey}`,
      query: forwardedQuery
    })
  }

  /**
   * @description: 初始化ichanganSDK
   *   wxwork: 1000302 // 传入企微id  生产
   *   wxwork: 1000023 // 传入企微id 测试
   * @return {*}
   */
  const iChangAnInitSDK = async () => {
    console.log('iChangAnInitSDK--start')

    try {
      const res = await SDK.init({
        clientId: {
          wxwork: qxAgentId
        },
        // hideMenu: false,
        // 生产 production | 测试 staging
        // mode: 'staging'
        mode: qxMode
      })
      console.log('iChangAnInitSDK--res', res)
      console.log('(window as any)?.wx--sdk', (window as any)?.wx)
      // initWeWork()
    } catch (error) {
      console.error('iChangAnInitSDK-->error', error)
      return Promise.reject(error)
    }
  }

  /**
   * @description: canswer在企微中登录
   * @return {*}
   */
  const qxInitByCanswer = async () => {
    try {
      console.log('qxInitByCanswer--start')
      // sdk初始化
      await iChangAnInitSDK()
      // sdk登录
      await SDK.login()
      // 获取用户信息
      const userInfo = await SDK.getUserInfo()
      qxUserInfo.value = userInfo
      console.log('userInfo--end', userInfo)
      // 根据用户信息换取token
      await getUserInfo()
      return userInfo
    } catch (error: any) {
      console.error('qxInitByCanswer-->error', error)
      showToast(error.message)
    }
  }

  const qxInit = async () => {
    try {
      console.log('qxInit--start')

      await iChangAnInitSDK()
      await qxLogin()
      await getUserInfo()

      linkHome()
    } catch (error: any) {
      console.error('qxInit-->error', error)
      showToast(error.message)
    }
  }

  const qxLogin = async () => {
    // const { user, token } = await SDK.login({ validate: false, watermark: true })
    // 需要先调用login方法，才能获取到用户信息
    try {
      await SDK.login()
    } catch (error) {
      console.log('qxLogin-->error', error)
      // sdk登录失败，使用sso登录
      // authLogin()
      return Promise.reject(error)
    }
    try {
      const userInfo = await SDK.getUserInfo()
      qxUserInfo.value = userInfo
    } catch (error) {
      console.log('getUserInfo-->error', error)
      return Promise.reject(error)
    }
  }

  /**
   * @description: 获取ichangan用户信息，并使用loginID换取VOC的token
   * @return {*}
   */
  const getUserInfo = async () => {
    console.log('qxUserInfo-->', qxUserInfo.value) // => UserInfo
    try {
      const resultToken = await getTokenByUserAccount(enCrypt(qxUserInfo.value.loginID))
      console.log('resultToken', resultToken)

      if (resultToken.success) {
        const saved = setToken(resultToken.result)
        if (!saved) {
          throw new Error('获取登录凭证失败')
        }
      } else {
        showToast(resultToken.message)
      }
    } catch (error: any) {
      showToast(error.message)
    }
  }

  /**
   * 在当前 H5 页面上下文中静默刷新企微 token。
   * - 优先复用已缓存的企微用户信息，避免额外跳转
   * - 若内存中的用户信息丢失，则补做 SDK 初始化与登录
   * - token 刷新成功后直接刷新当前 URL，尽量保留当前 H5 路由与 query
   */
  const refreshWeWorkTokenForCurrentPage = async () => {
    console.log('refreshWeWorkTokenForCurrentPage--start')

    if (!qxUserInfo.value?.loginID) {
      await iChangAnInitSDK()
      await qxLogin()
    }

    await getUserInfo()

    const currentToken = localStorage.getItem(TOKEN_KEY)
    if (!currentToken) {
      throw new Error('获取登录凭证失败')
    }

    retryCount.value = 0

    setTimeout(() => {
      location.reload()
    }, 100)
  }

  // ichangan登录
  const authLogin = async () => {
    console.log('authLogin--start')

    // 保留消息推送等入口的业务参数，供 SSO 回跳时恢复目标 H5 页面。
    // Vue Router 会自动解码回跳 URL 中标准 URL 编码的 query，无需重复解码。
    const currentQuery = router.currentRoute.value?.query || {}
    saveH5SsoRedirectState(currentQuery)
    const ssoParams = new URLSearchParams({
      source: isMobileEnvironment() ? 'app' : 'pc'
    })

    Object.entries(currentQuery).forEach(([key, value]) => {
      // token 由 SSO 回跳时重新签发，source 只能由当前运行环境决定。
      if (key === 'token' || key === 'source' || value == null) return

      if (Array.isArray(value)) {
        value.forEach(item => {
          if (item != null) ssoParams.append(key, item)
        })
        return
      }

      ssoParams.append(key, value)
    })

    const ssoUrl = `${SSO_URL}?${ssoParams.toString()}`
    console.log('location.origin', location.origin)
    console.log('SSO_URL', ssoUrl)
    location.href = ssoUrl
    // const result = await sso()
    // console.log('result', result)

    console.log('authLogin--end')
  }

  const PCInit = async () => {
    try {
      console.log('iChangeanInit--start--token', _token.value)
      if (_token.value) {
        setToken(_token.value as string)
        try {
          await userStore.getUserPermissions()
          setTimeout(() => {
            linkPCHome()
          }, 500)
        } catch (error: any) {
          console.error(error)
          // ElMessage.error(error.message)
        }
      } else {
        authLogin()
      }
    } catch (error: any) {
      console.error(error)
      // ElMessage.error(error.message)
    } finally {
      // loading.value = false
    }
  }

  const MobileInit = async () => {
    try {
      // iChangAnInitSDK()
      console.log('iChangeanInit--start--token', _token.value)

      if (_token.value) {
        setToken(_token.value as string)

        linkHome()
      } else {
        authLogin()
      }
    } catch (error: any) {
      console.error(error)
      showToast(error.message)
    }
  }

  const iChangeanInit = async () => {
    if (isMobileEnvironment()) {
      MobileInit()
    } else {
      PCInit()
    }
  }

  /**
   * @description: 处理H5Rct sso逻辑
   * @return {*}
   */
  const ssoByH5Rct = () => {
    // 根据环境选择初始化方式
    const currentEnv = getCurrentEnvironment()
    console.log('当前运行环境:', currentEnv)
    if (isWeWorkEnvironment()) {
      qxInit()
    } else {
      iChangeanInit()
    }
  }

  const qxVocInit = async () => {
    try {
      console.log('企微初始化--start')

      await iChangAnInitSDK()
      await qxLogin()
      await getUserInfo()

      linkVocViewPage()
    } catch (error: any) {
      console.error('企微初始化-->error', error)
      showToast(error.message)
    }
  }

  const iChangeanVocInit = async () => {
    if (isMobileEnvironment()) {
      MobileVocInit()
    } else {
      PCVocInit()
    }
  }

  const MobileVocInit = async () => {
    try {
      // iChangAnInitSDK()
      const cachedToken = localStorage.getItem(TOKEN_KEY)
      const availableToken = isValidToken(_token.value)
        ? _token.value
        : isValidToken(cachedToken)
          ? cachedToken
          : null
      console.log('移动端Init--start--hasToken', Boolean(availableToken))

      if (availableToken) {
        _token.value = availableToken
        setToken(availableToken)

        linkVocViewPage()
      } else {
        clearToken()
        authLogin()
      }
    } catch (error: any) {
      console.error(error)
      showToast(error.message)
    }
  }

  const PCVocInit = async () => {
    try {
      console.log('PC端Init--start--token', _token.value)
      if (_token.value) {
        setToken(_token.value as string)
        try {
          await userStore.getUserPermissions()
          setTimeout(() => {
            linkVocViewPage()
          }, 500)
        } catch (error: any) {
          console.error(error)
          // ElMessage.error(error.message)
        }
      } else {
        authLogin()
      }
    } catch (error: any) {
      console.error(error)
      // ElMessage.error(error.message)
    } finally {
      // loading.value = false
    }
  }
  /**
   * @description: 处理H5Rct VOC逻辑
   * @return {*}
   */
  const ssoByVocH5Rct = () => {
    // 根据环境选择初始化方式
    const currentEnv = getCurrentEnvironment()
    console.log('当前voc运行环境:', currentEnv)
    if (isWeWorkEnvironment()) {
      qxVocInit()
    } else {
      iChangeanVocInit()
    }
  }

  const PCScreenshotInit = async () => {
    try {
      console.log('截图访问PC端Init--start--token', _token.value)
      setToken(_token.value as string)
      try {
        await userStore.getUserPermissions()
      } catch (error: any) {
        console.error(error)
        // ElMessage.error(error.message)
      }
    } catch (error: any) {
      console.error(error)
      // ElMessage.error(error.message)
    } finally {
      // loading.value = false
    }
  }
  const ssoByScreenshotH5Rct = () => {
    // 截屏场景直接初始化
    PCScreenshotInit()
  }

  /**
   * @description: 企微环境的分享初始化方法
   * 完成登录和权限初始化，不进行权限校验和跳转（由调用方处理）
   * @param {string} targetPath - 目标页面路径（可选，已废弃，保留以兼容旧代码）
   * @return {*}
   */
  const qxInitForShare = async (targetPath?: string) => {
    try {
      console.log('qxInitForShare--start', { targetPath })

      // 1. SDK初始化
      await iChangAnInitSDK()

      // 2. SDK登录
      await qxLogin()

      // 3. 获取用户信息并换取token
      await getUserInfo()

      // 4. 初始化权限数据（强制刷新，确保数据最新）
      console.log('初始化权限数据...')
      const permissionsStore = usePermissionsStore()
      await permissionsStore.initUserPermissions(true) // 强制刷新
      console.log('权限数据初始化完成')

      // 注意：不在这里进行权限校验和跳转
      // 权限校验和跳转逻辑由调用方（redirect/index.vue）处理
      // 这样可以保持职责分离，sso.ts 只负责登录和权限初始化
    } catch (error: any) {
      console.error('qxInitForShare-->error', error)
      showToast(error.message || '登录失败')
      throw error // 抛出错误，让调用方处理
    }
  }

  /**
   * @description: 初始化SDK并登录（不包含token换取和跳转逻辑）
   * 用于token已存在时，仅初始化SDK以确保分享功能可用
   * @return {*}
   */
  const initSDKAndLogin = async () => {
    try {
      console.log('initSDKAndLogin--start')
      await iChangAnInitSDK()
      await qxLogin()
      console.log('initSDKAndLogin--end')
    } catch (error: any) {
      console.error('initSDKAndLogin-->error', error)
      // 不抛出错误，允许后续流程继续执行
    }
  }

  /**
   * @description: 处理企微环境分享场景的 SSO 流程
   * 只有企微环境才有分享功能，因此直接调用企微初始化方法
   * @param {string} targetPath - 目标页面路径（可选）
   * @return {*}
   */
  const ssoByH5RctForShare = async (targetPath?: string) => {
    // 只有企微环境才有分享，直接调用企微初始化方法
    await qxInitForShare(targetPath)
  }

  /**
   * @description: 处理请求拦截器中的sso逻辑
   * @return {*}
   */
  const ssoByInterceptors = async () => {
    // 防止无限重试
    if (retryCount.value >= MAX_RETRY) {
      showToast('登录失败，请手动刷新页面重试')
      return
    }

    retryCount.value++

    try {
      if (isWeWorkEnvironment()) {
        await refreshWeWorkTokenForCurrentPage()
      } else if (isMobileEnvironment()) {
        // 移动端环境: token过期时需要清除旧token并重新登录
        console.log('移动端token过期,重新登录')
        clearToken()
        authLogin()
      }
    } catch (error: any) {
      console.error('SSO处理失败:', error)
      showToast(error.message || 'SSO处理失败')
    }
  }

  /**
   * @description: h5页面401以后重新跳转到eac登录页
   * @return {*}
   */
  const ssoH5ByEAC = async () => {
    try {
      if (isWeWorkEnvironment()) {
        await refreshWeWorkTokenForCurrentPage()
      } else {
        // 移动端环境: 401未授权,清除旧token并重新登录
        console.log('移动端401未授权,重新登录')
        clearToken()
        authLogin()
      }
    } catch (error: any) {
      console.error('H5 页面 401 自动恢复失败:', error)
      showToast(error.message || '登录恢复失败')
    }
  }

  /**
   * 页面从外部返回或缓存恢复后，主动探测一次 H5 登录态。
   * - 直接复用 userPermissions 接口，以服务端结果作为 token 是否有效的最终依据
   * - 命中 100000/401 时，由现有请求拦截器自动触发 SSO 恢复
   * - 增加节流与并发复用，避免 pageshow / focus / visibilitychange 连续触发多次请求
   */
  const ensureH5SessionOnResume = async (): Promise<void> => {
    const currentRoute = router.currentRoute.value
    if (!currentRoute?.path?.startsWith('/h5')) return

    const now = Date.now()
    if (resumeSessionCheckPromise) {
      return resumeSessionCheckPromise
    }

    if (now - lastResumeSessionCheckAt.value < RESUME_SESSION_CHECK_INTERVAL) {
      return
    }

    lastResumeSessionCheckAt.value = now

    resumeSessionCheckPromise = (async () => {
      const permissionsStore = usePermissionsStore()

      try {
        await permissionsStore.initUserPermissions(true, {
          silentError: true,
          requestConfig: {
            silentError: true
          }
        })
      } catch {
        // 恢复探测属于基础设施请求。
        // 鉴权失效时会由 axios 拦截器接管 SSO；其他瞬时失败则静默处理，避免误提示用户。
      } finally {
        resumeSessionCheckPromise = null
      }
    })()

    return resumeSessionCheckPromise
  }

  /**
   * @description: 重新登录
   * @return {*}
   */
  const linkLogOut = () => {
    // 退出登录前尽力结束一次会话统计，避免 token 清理后无法鉴权上报
    systemAccessDuration.stop('logout')
    clearToken()
    // 跳转到EAC登出页面
    window.location.href = eacSso
  }

  /**
   * @description: open 打开链接
   * @param {string} url
   * @return {*}
   */
  const openBySdk = async (url: string) => {
    console.log('sdk调用open方法')

    try {
      await SDK.open({
        uri: url
        // params: { foo: 'bar' }
      })
    } catch (e) {
      console.error(e)
    }
  }

  return {
    ssoByScreenshotH5Rct,
    ssoByVocH5Rct,
    ssoByH5Rct,
    ssoByH5RctForShare,
    ssoByInterceptors,
    ensureH5SessionOnResume,
    PCInit,
    ssoH5ByEAC,
    linkLogOut,
    setStoreToken,
    clearToken,
    openBySdk,
    qxInitByCanswer,
    initSDKAndLogin
  }
})
