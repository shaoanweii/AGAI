/**
 * 企微相关功能封装
 * 统一管理企微工作台相关API调用
 */

import { isWeWorkEnvironment } from '@/utils'
import { useShareStore } from '@h5/store'

/**
 * @description: 批量显示功能按钮
 * @param {string} _menuList
 * @return {*}
 */
export const showMenuItems = (_menuList?: string[]) => {
  console.log('批量显示功能按钮:showMenuItems-->', _menuList)
  try {
    // 显示转发
    ; (window as any)?.wx?.showMenuItems({
      menuList: _menuList ? _menuList : ['menuItem:share:appMessage'] // 要显示的菜单项
    })
  } catch (error) {
    console.error('批量显示功能按钮:showMenuItems-->error', error)
  }
}

/**
 * @description: 显示右上角菜单
 * @return {*}
 */
export const showOptionMenu = () => {
  try {
    console.log('显示右上角菜单:showOptionMenu-->')
      ; (window as any)?.wx?.showOptionMenu()
  } catch (error) {
    console.error('显示右上角菜单:showOptionMenu-->catch->error', error)
  }
}

/**
 * @description: 批量隐藏功能按钮
 * @param {string} _menuList
 * @return {*}
 */
export const hideMenuItems = (_menuList?: string[]) => {
  console.log('批量隐藏功能按钮:hideMenuItems-->', _menuList)
  try {
    // 隐藏转发
    ; (window as any)?.wx?.hideMenuItems({
      menuList: _menuList ? _menuList : ['menuItem:share:wechat', 'menuItem:openWithSafari', 'menuItem:share:timeline']
    })
  } catch (error) {
    console.error('批量隐藏功能按钮:hideMenuItems-->error', error)
  }
}

/**
 * @description: 初始化企微工作台
 * @return {*}
 */
export const initWeWork = () => {
  console.log('(window as any)?.wx-->initWeWork', (window as any)?.wx)

  if (isWeWorkEnvironment()) {
    // showOptionMenu()
    hideMenuItems()
  }
}

/**
 * @description: 监听「转发」按钮点击
 * @param {string | (() => string)} title - 分享标题，可以是字符串或返回字符串的函数
 * @param {string | (() => string)} desc - 分享描述，可以是字符串或返回字符串的函数
 * @param {string | (() => string)} imgUrl - 分享图片URL，可以是字符串或返回字符串的函数
 * @return {*}
 */
export const onMenuShareAppMessage = (
  title: string | (() => string),
  desc: string | (() => string),
  imgUrl?: string | (() => string)
) => {
  // 获取实际的分享内容（函数则执行获取最新值）
  const getTitle = () => (typeof title === 'function' ? title() : title)
  const getDesc = () => (typeof desc === 'function' ? desc() : desc)
  const getImgUrl = () =>
    imgUrl ? (typeof imgUrl === 'function' ? imgUrl() : imgUrl) : `${location.origin}/report/share-round.png`

  // 打印分享信息（用于测试环境通过 vconsole 查看）
  const finalTitle = getTitle()
  const finalDesc = getDesc()
  const finalImgUrl = getImgUrl()
  console.log('=== 分享内容 ===')
  console.log('标题:', finalTitle)
  console.log('描述:', finalDesc)
  console.log('图片:', finalImgUrl)
  console.log('===============')

  try {
    ; (window as any)?.wx?.onMenuShareAppMessage({
      title: finalTitle,
      desc: finalDesc,
      imgUrl: finalImgUrl,
      success() {
        // 用户确认分享后回调
        console.log('onMenuShareAppMessage-->success', finalTitle, finalDesc)
      },
      cancel() {
        // 用户取消分享后回调
        console.log('onMenuShareAppMessage-->cancel', finalTitle, finalDesc)
      }
    })
  } catch (error) {
    console.error('监听「转发」:onMenuShareAppMessage-->error', error)
  }
}

/**
 * @description: 主动触发企微分享（用于测试按钮等场景）
 * @param {string} title - 分享标题
 * @param {string} desc - 分享描述
 * @return {*}
 */
export const invokeShareAppMessage = (title: string, desc: string) => {
  // 构建分享链接，统一跳转到 /h5Rct 中间页面
  // 通过 target 参数传递目标页面的完整路径（包括路径和查询参数）
  const buildShareLinkViaRedirect = () => {
    try {
      // 获取分享参数
      const shareStore = useShareStore()
      const shareParams = shareStore.getShareParams()

      const url = new URL(window.location.href)
      // 移除 code 参数，避免分享链接使用旧 code 去登录
      url.searchParams.delete('code')

      // 构建目标路径（path + hash + query）
      // 注意：项目使用 hash 路由模式，需要包含 hash 部分
      const hash = url.hash || ''

      // 如果 hash 中包含路径和查询参数，提取 hash 部分
      // 例如：http://example.com/#/h5/home?id=123 -> target 应该是 /h5/home?id=123
      // 注意：项目使用 hash 路由模式，路由信息都在 hash 部分
      let targetPath = ''
      if (hash) {
        // hash 格式：/#/h5/home?id=123，去掉开头的 #
        targetPath = hash.startsWith('#') ? hash.substring(1) : hash
      }

      // 如果 targetPath 为空，使用默认路径（hash 路由模式下，没有 hash 说明页面异常，使用默认路径）
      if (!targetPath || targetPath === '/') {
        targetPath = '/h5/home'
      }

      // 解析 targetPath，分离路径和查询参数
      let targetRoutePath = targetPath
      const targetQueryParams = new URLSearchParams()
      
      if (targetPath.includes('?')) {
        const [path, queryString] = targetPath.split('?')
        targetRoutePath = path
        // 解析现有查询参数
        const existingParams = new URLSearchParams(queryString)
        existingParams.forEach((value, key) => {
          targetQueryParams.set(key, value)
        })
      }

      // 如果有分享参数，添加到查询参数中
      if (shareParams) {
        if (shareParams.brandCode) {
          targetQueryParams.set('brandCode', shareParams.brandCode)
        }
        if (shareParams.dateUnit !== undefined) {
          targetQueryParams.set('dateUnit', String(shareParams.dateUnit))
        }
        if (shareParams.dateTime) {
          if (shareParams.dateTime.code !== undefined) {
            targetQueryParams.set('dateTimeCode', String(shareParams.dateTime.code))
          }
          if (shareParams.dateTime.startTime) {
            targetQueryParams.set('startTime', shareParams.dateTime.startTime)
          }
          if (shareParams.dateTime.endTime) {
            targetQueryParams.set('endTime', shareParams.dateTime.endTime)
          }
          if (shareParams.dateTime.name) {
            targetQueryParams.set('dateTimeName', shareParams.dateTime.name)
          }
        }
        if (shareParams.channelCatagory !== undefined) {
          targetQueryParams.set('channelCatagory', shareParams.channelCatagory)
        }
      }

      // 重新构建 targetPath，包含查询参数
      const queryString = targetQueryParams.toString()
      targetPath = queryString ? `${targetRoutePath}?${queryString}` : targetRoutePath

      // 构建中间页面链接（hash 路由模式下需要包含 #）
      // 注意：项目使用 hash 路由模式，URL 格式应该是 http://domain/#/h5Rct?target=xxx
      // 需要保留 pathname（如 /report）以确保分享链接路径正确
      // 移除 pathname 末尾的斜杠，避免产生双斜杠
      const pathname = window.location.pathname.replace(/\/$/, '')
      const baseUrl = window.location.origin + pathname
      const searchParams = new URLSearchParams()
      searchParams.set('target', encodeURIComponent(targetPath))

      // 返回完整 URL（hash 路由模式下，路径放在 hash 部分）
      return `${baseUrl}/#/h5Rct?${searchParams.toString()}`
    } catch (error) {
      console.error('构建分享链接失败:', error)
      // 如果 URL 解析失败，使用原始方式处理参数
      const originalUrl = window.location.href
      // 移除 code 参数，处理不同位置的情况
      const urlWithoutCode = originalUrl
        // 如果 code 是第一个参数且后面还有其他参数: ?code=xxx& 替换为 ?
        .replace(/\?code=[^&]*&/g, '?')
        // 如果 code 是第一个参数且是最后一个参数: ?code=xxx 替换为空（后面会处理）
        .replace(/\?code=[^&]*$/g, '')
        // 如果 code 在中间: &code=xxx& 替换为 &
        .replace(/&code=[^&]*&/g, '&')
        // 如果 code 在末尾: &code=xxx 替换为空
        .replace(/&code=[^&]*$/g, '')

      // 提取 hash 部分作为目标路径
      const hashMatch = originalUrl.match(/#(.+)$/)
      const targetPath = hashMatch ? hashMatch[1] : '/h5/home'

      // hash 路由模式下，中间页面路径应该放在 hash 部分
      // 需要保留 pathname（如 /report）以确保分享链接路径正确
      const urlObj = new URL(urlWithoutCode.split('#')[0]) // 移除原有 hash 并解析 URL
      // 移除 pathname 末尾的斜杠，避免产生双斜杠
      const pathname = urlObj.pathname.replace(/\/$/, '')
      const baseUrl = urlObj.origin + pathname
      const searchParams = new URLSearchParams()
      searchParams.set('target', encodeURIComponent(targetPath))

      return `${baseUrl}/#/h5Rct?${searchParams.toString()}`
    }
  }

  const shareLink = buildShareLinkViaRedirect()
  console.log('invokeShareAppMessage-->start', { title, desc, link: shareLink, imgUrl: `${location.origin}/report/share-round.png` })
  try {
    ; (window as any)?.wx?.invoke(
      'shareAppMessage',
      {
        title: title || '客户之声voc', // 分享标题
        desc: desc || '客户之声描述', // 分享描述
        link: shareLink, // 分享链接（统一跳转到 /h5Rct）
        imgUrl: `${location.origin}/report/share-round.png`, // 分享封面
        // enableIdTrans: 1 // 是否开启id转译，不填默认为0
        type: 1
      },
      function (res: any) {
        console.log('res-->shareAppMessage', res)

        if (res.err_msg == 'shareAppMessage:ok') {
          // 正确处理
          console.log('分享成功')
        } else {
          // 错误处理
          console.error('分享失败:', res.err_msg)
        }
      }
    )
  } catch (error) {
    console.error('invokeShareAppMessage-->error', error)
  }
}
