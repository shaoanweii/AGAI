<script setup lang="ts">
// 引入移动端容器 SDK；本地模式由中性适配器替代
import SDK, { HOST } from '@/integrations/mobile-container'
import { useRoute, useRouter } from 'vue-router'
import { useH5ssoStore } from '../../store/sso'
import { usePermissionsStore } from '../../store'
import { getReUrl } from '../../utils/initPcFilter'
// import { isWeWorkEnvironment } from '@/utils' // 已注释，环境检查逻辑已移除
import { onMounted, nextTick } from 'vue'
import { TOKEN_KEY } from '@/constants'
import { insUrl } from '@/constants/env'
import { enCrypt } from '@/utils/encryption'
import { getAuthDataUrl, insFreeRedictLogin, fetchH5UserPermissions } from '../../api/common'
import { isValidToken, setToken } from '@/utils'

defineOptions({
  name: 'Redirect'
})

const route = useRoute()
const router = useRouter()
const { token, target, pcTarget, voctype, pageUrlType } = route.query
const ssoStore = useH5ssoStore()
const permissionsStore = usePermissionsStore()

const DATA_SQUARE_REPORT_DETAIL_ROUTE_NAME = 'H5DataSquareReportDetail'
// 免菜单权限校验的分享落地页
const PERMISSION_FREE_ROUTE_NAMES = new Set<string>([DATA_SQUARE_REPORT_DETAIL_ROUTE_NAME])

console.log('SDK', SDK)
console.log('HOST', HOST)

/**
 * 检查权限
 * @param routeName 路由名称
 * @param menus 权限菜单列表
 * @returns 是否有权限访问
 */
function checkPermission(routeName: string | symbol | null | undefined, menus: any[]): boolean {
  if (!routeName) return false

  const routeNameStr = String(routeName)

  // 免菜单权限校验的分享落地页，只要求完成登录/token 初始化，数据权限由接口兜底。
  if (PERMISSION_FREE_ROUTE_NAMES.has(routeNameStr)) {
    return true
  }

  if (!menus || menus.length === 0) return false

  // 声音详情和任务详情页面的权限不会在menus中返回
  // 只需要判断对应的列表页权限即可：有列表页权限就有详情页权限
  if (routeNameStr === 'H5VoiceDetail') {
    // 声音详情页检查首页权限
    return menus.some((menu: any) => menu.permissionKey === 'H5Home')
  }

  if (routeNameStr === 'H5TaskEventDetail') {
    // 任务详情页检查任务列表页权限
    return menus.some((menu: any) => menu.permissionKey === 'H5TaskEvent')
  }

  // 其他路由：检查 menus 数组中是否存在匹配的 permissionKey
  return menus.some((menu: any) => menu.permissionKey === routeName)
}

/**
 * 从路径解析路由名称
 * @param path 路径
 * @returns 路由名称
 */
function getRouteNameFromPath(path: string): string | null {
  try {
    // 使用 router.resolve 解析路径
    const resolved = router.resolve(path)
    if (resolved && resolved.name) {
      return resolved.name as string
    }
    return null
  } catch (error) {
    console.error('解析路由名称失败:', error)
    return null
  }
}

/**
 * 解析查询参数
 * @param search 查询字符串
 * @returns 查询参数对象
 */
function parseQueryParams(search: string): Record<string, string> {
  const params: Record<string, string> = {}
  if (!search) return params

  // 移除开头的 ?
  const queryString = search.startsWith('?') ? search.substring(1) : search
  const pairs = queryString.split('&')

  pairs.forEach(pair => {
    const [key, value] = pair.split('=')
    if (key && value) {
      params[decodeURIComponent(key)] = decodeURIComponent(value)
    }
  })

  return params
}

/**
 * 从路由 query 中读取第一个字符串值。
 * @param value 原始 query 值
 * @returns 字符串值
 */
function getQueryString(value: unknown): string {
  if (Array.isArray(value)) {
    return typeof value[0] === 'string' ? value[0] : ''
  }

  return typeof value === 'string' ? value : ''
}

/**
 * 解码站内相对路由地址。
 * pcTarget 只允许站内 hash 路由路径，避免被构造成外部跳转。
 * @param rawTarget 原始 pcTarget
 * @returns 解析后的目标路径
 */
function normalizePcTarget(rawTarget: string): string {
  try {
    const decodedTarget = decodeURIComponent(rawTarget).trim()

    if (
      !decodedTarget ||
      decodedTarget.startsWith('//') ||
      /^https?:\/\//i.test(decodedTarget) ||
      !decodedTarget.startsWith('/')
    ) {
      return ''
    }

    return decodedTarget
  } catch (error) {
    console.warn('pcTarget 参数解码失败:', error)
    return ''
  }
}

/**
 * 获取 PC 通用跳转可复用的登录态。
 * URL token 优先；没有 URL token 时，复用当前域已登录写入的本地 token。
 * @returns 登录 token
 */
function getPcRedirectToken(): string {
  return getQueryString(token) || localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 处理通用 PC 目标页重定向。
 * 新分支不复用 H5 分享 target，避免普通浏览器触发企微 SDK 初始化。
 */
async function handlePcTargetRedirect() {
  const rawPcTarget = getQueryString(pcTarget)
  const pcTargetPath = normalizePcTarget(rawPcTarget)

  if (!pcTargetPath) {
    console.warn('pcTarget 参数无效，跳转到 PC 首页')
    await router.replace('/overview')
    return
  }

  const tokenValue = getPcRedirectToken()
  if (tokenValue) {
    ssoStore.setStoreToken(tokenValue)
    if (getQueryString(token)) {
      setToken(tokenValue)
    }
  }

  let targetRoutePath = pcTargetPath
  let targetQuery: Record<string, string> = {}

  if (pcTargetPath.includes('?')) {
    const queryIndex = pcTargetPath.indexOf('?')
    const path = pcTargetPath.slice(0, queryIndex)
    const queryString = pcTargetPath.slice(queryIndex + 1)
    targetRoutePath = path
    targetQuery = parseQueryParams(`?${queryString}`)
  }

  console.log('跳转到 PC 目标页面:', targetRoutePath, targetQuery)

  await router.replace({
    path: targetRoutePath,
    query: targetQuery
  })
}

/**
 * 处理分享场景的重定向
 */
async function handleShareRedirect() {
  // 环境检查：只有企微环境才有分享功能
  // if (!isWeWorkEnvironment()) {
  //   console.warn('非企微环境不应该有分享功能，跳转到首页')
  //   router.replace('/h5/home')
  //   return
  // }

  try {
    // 解析目标页面
    if (!target || typeof target !== 'string') {
      console.warn('缺少 target 参数，跳转到首页')
      router.replace('/h5/home')
      return
    }

    const targetPath = decodeURIComponent(target)
    console.log('分享目标路径（解码后）:', targetPath)
    console.log('原始target参数:', target)

    // 解析目标路径
    // 处理 hash 路由模式：targetPath 可能是 /h5/home?id=123 格式
    // 注意：router.replace 会自动处理 hash，所以直接使用 targetPath 即可
    let targetRoutePath = targetPath
    let targetQuery: Record<string, string> = {}

    // 如果包含查询参数，分离路径和查询参数
    if (targetPath.includes('?')) {
      const [path, queryString] = targetPath.split('?')
      targetRoutePath = path
      targetQuery = parseQueryParams(`?${queryString}`)
    }

    // 确保路径以 / 开头（hash 路由模式下，路径应该是 /h5/xxx 格式）
    if (!targetRoutePath.startsWith('/')) {
      targetRoutePath = '/' + targetRoutePath
    }

    // 统一走登录流程（无论token是否存在）
    console.log('分享链接点击，统一走登录流程确保状态最新')

    try {
      // 执行登录流程（SDK初始化、登录、获取token、初始化权限数据）
      await ssoStore.ssoByH5RctForShare()
    } catch (error: any) {
      console.error('登录流程失败:', error)
      // 登录失败，跳转到首页
      router.replace('/h5/home')
      return
    }

    // 登录完成后，进行权限校验
    const targetRouteName = getRouteNameFromPath(targetRoutePath)
    console.log('目标路由名称:', targetRouteName)

    if (targetRouteName) {
      const menus = permissionsStore.menus || []
      const hasPermission = checkPermission(targetRouteName, menus)

      if (!hasPermission) {
        console.warn('无权限访问目标页面，跳转到无权限页面')
        router.replace('/h5NotAuth')
        return
      }
    } else {
      console.warn('无法解析路由名称，跳过权限检查')
    }

    // 跳转到目标页面
    // 添加标记，表示是从中间页跳转过来的
    const finalQuery = {
      ...targetQuery,
      _fromRedirect: '1' // 标记从中间页跳转
    }
    console.log('跳转到目标页面:', targetRoutePath, finalQuery)

    // 先使用 window.history.replaceState() 将当前历史记录条目（中间页）替换为目标页面
    // 这样即使后续 router.replace() 创建了新条目，历史记录中也不会有中间页了
    const queryString = Object.keys(finalQuery)
      .map(
        key =>
          `${encodeURIComponent(key)}=${encodeURIComponent(finalQuery[key as keyof typeof finalQuery])}`
      )
      .join('&')
    const targetHash = `#${targetRoutePath}${queryString ? `?${queryString}` : ''}`

    try {
      // 获取当前历史记录的状态
      const currentState = window.history.state

      // 替换当前历史记录条目，将中间页的 URL 替换为目标页面的 URL
      // 在 Hash 路由模式下，虽然 replaceState 的第三个参数可能不会直接改变 hash，
      // 但我们可以通过这种方式标记历史记录，然后让 router.replace() 来完成实际的跳转
      window.history.replaceState(
        {
          ...currentState,
          // 添加标记，表示这个条目应该指向目标页面
          _targetPath: targetRoutePath,
          _targetQuery: finalQuery
        },
        '',
        targetHash
      )

      console.log('已替换历史记录中的中间页条目为目标页面')
    } catch (error) {
      console.warn('替换历史记录失败，继续执行路由跳转:', error)
    }

    // 执行路由跳转（使用 replace 确保不创建新的历史记录条目）
    await router.replace({
      path: targetRoutePath,
      query: finalQuery
    })

    // 等待路由跳转完成
    await nextTick()

    // 再次确保历史记录正确：如果检测到 referrer 是中间页，说明可能还有中间页的条目
    // 此时使用 replaceState 再次确保当前条目指向目标页面
    try {
      if (document.referrer && document.referrer.includes('/h5Rct')) {
        // 如果 referrer 是中间页，说明历史记录中可能还有中间页
        // 再次替换当前历史记录条目，确保不包含中间页
        window.history.replaceState(window.history.state, '', window.location.href)
        console.log('二次确认：已清除历史记录中的中间页条目')
      }
    } catch (error) {
      console.warn('二次清除历史记录失败，但不影响功能:', error)
    }
  } catch (error: any) {
    console.error('处理分享重定向失败:', error)
    // 发生错误时跳转到首页
    router.replace('/h5/home')
  }
}

const handPageByType = async () => {
  const map: any = {
    '1': '/scene/journeyAnalysis', // 旅程分析
    '2': ``, // 洞察引擎 另一个系统页面
    '3': '/overview', // VOC总览
    '4': '', // 智能问数 另一个系统页面
    '5': '', // 智能报告 另一个系统页面
    '6': '/scene/newCarLaunch', // 新车上市
    '7': '/leaderOverview' // 领导总览
  }
  const type: any = pageUrlType
  const urlJunmp = map[type]
  const config = {
    headers: {
      ...(isValidToken(token) ? { Authorization: `Bearer ${token}` } : {})
    }
  }
  if (isValidToken(token)) {
    ssoStore.setStoreToken(token)
    setToken(token)
  } else {
    ssoStore.clearToken()
    ssoStore.ssoByH5Rct()
    return
  }
  if (['2'].includes(type)) {
    // 说明需要另外开页面
    const perConfigRes = await fetchH5UserPermissions(config)

    const { userId, username } = perConfigRes.result || {}
    const res = await insFreeRedictLogin({
      username: enCrypt(username),
      userId: userId
    })
    if (res.success && res.result.access_token) {
      const token = res.result.access_token
      const url = `${insUrl}?token=${token}`
      window.location.href = url
    } else {
      console.log('洞察引擎登录失败', res)
    }
  } else if (['4', '5'].includes(type)) {
    // 说明需要另外开页面
    const perConfigRes = await fetchH5UserPermissions(config)

    const { username } = perConfigRes.result || {}
    const res = await getAuthDataUrl({ userCode: username })
    if (res.success && res.result) {
      window.location.href = res.result
    } else {
      console.log('智能问数系统登录失败', res)
    }
  } else {
    const currentQuery = (router.currentRoute.value?.query || {}) as Record<string, unknown>
    // 透传 query：仅保留基础类型，避免把 path/token 再带到目标页
    const forwardedQuery: Record<string, any> = { redirect: true }
    Object.keys(currentQuery).forEach(key => {
      if (key === 'path' || key === 'token') return
      const value = currentQuery[key] || ''
      if (value) forwardedQuery[key] = String(value)
    })
    router.replace({
      path: urlJunmp
      // query: forwardedQuery
    })
  }
}

onMounted(() => {
  if (!pcTarget && !target && !voctype && !pageUrlType) {
    const localToken = getPcRedirectToken() || 'voc-voice-local-demo-token'
    setToken(localToken)
    ssoStore.setStoreToken(localToken)
    void router.replace('/h5/home')
    return
  }

  // 分支判断：是否有 target 参数
  if (pcTarget) {
    void handlePcTargetRedirect()
  } else if (voctype === 'voc') {
    console.warn('重定向voctype', voctype)
    // 新增VOC场景
    // 推送访问的地址格式
    // /#/h5Rct?voctype=voc&token=xxx&target=/#/scene/mainAccount&pdfurl=xxx
    // 参数:voctype-区分类型 token-单点登录 target-跳转地址 tourl-点击按钮重定向地址
    // 访问地址类似 /page?voctype=voc&token=xxx&target=/#/scene/mainAccount

    if (token) {
      ssoStore.setStoreToken(token as string)
    }
    ssoStore.ssoByVocH5Rct()
  } else if (voctype === 'screenshot') {
    // 截屏场景
    // 新增截图访问的场景
    // /#/h5Rct?voctype=screenshot&token=xxx&target=/#/scene/mainAccount

    if (token) {
      ssoStore.setStoreToken(token as string)
    }
    // 初始化登录信息
    ssoStore.ssoByScreenshotH5Rct()
    const x = getReUrl(target)
    window.location.href = x
  } else if (pageUrlType) {
    handPageByType()
  } else {
    if (target) {
      // 新分支：处理分享场景
      handleShareRedirect()
    } else {
      // 原有逻辑：完全不变
      if (token) {
        ssoStore.setStoreToken(token as string)
      }
      ssoStore.ssoByH5Rct()
    }
  }
})
</script>

<template>
  <div class="Redirect">
    <van-loading color="#0094ff" :size="40" vertical>加载中...</van-loading>
  </div>
</template>

<style lang="scss" scoped>
.Redirect {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba($color: #000000, $alpha: 0.6);
}
</style>
