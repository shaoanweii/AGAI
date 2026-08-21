import { getQueryDataByid } from '@h5/api/report'

// 重组url
export const getReUrl = (targetUrl: any) => {
  if (!targetUrl) {
    return ''
  }
  try {
    const url = new URL(targetUrl)
    const reUrl =
      url.origin + url.pathname + '#/transitionView?target=' + encodeURIComponent(targetUrl)
    return reUrl
  } catch (error) {
    //
  }
  return targetUrl
}
export const getQueryParams = (targetUrl: string): Record<string, string> => {
  if (!targetUrl) {
    return {}
  }

  const extractQueryFromHash = (hash: string): string => {
    const index = hash.indexOf('?')
    return index >= 0 ? hash.slice(index + 1) : ''
  }

  const normalizeQuery = (query: string): string => query.replace(/^[?#]/, '').replace(/&+/g, '&')

  let queryString = ''

  try {
    const url = new URL(targetUrl)
    if (url.hash) {
      queryString = extractQueryFromHash(url.hash)
    }
    if (!queryString && url.search) {
      queryString = url.search.slice(1)
    }
  } catch {
    const hashIndex = targetUrl.indexOf('#')
    if (hashIndex >= 0) {
      queryString = extractQueryFromHash(targetUrl.slice(hashIndex))
    } else {
      const queryIndex = targetUrl.indexOf('?')
      queryString = queryIndex >= 0 ? targetUrl.slice(queryIndex + 1) : ''
    }
  }

  queryString = normalizeQuery(queryString)

  if (!queryString) {
    return {}
  }

  const params = new URLSearchParams(queryString)
  const result: Record<string, string> = {}
  params.forEach((value, key) => {
    result[key] = value
  })
  return result
}

/**
 * @description: 移动端重定向的时候初始化PC端的本地数据
 * */
export const handRedictAction = async (options: any) => {
  // 获取目标地址参数，例如本地场景详情页携带的 pdfUrl、token 与 reportId。
  const { targetUrl, callback } = options || {}
  const params = getQueryParams(targetUrl)
  console.log('参数集合', params)

  const { sendReportId, sendTaskId } = params
  const reportId = sendReportId
  const taskId = sendTaskId
  const { useSceneAnalysisStore } = await import('@/store/modules/sceneAnalysis')
  const sceneAnalysisStore = useSceneAnalysisStore()
  try {
    // 查询需要回显的条件
    const res = await getQueryDataByid({
      reportId,
      taskId
    })
    if (res.success) {
      const filterStr = res.result?.filter
      const item = {
        defaultCondition: filterStr,
        isDetail: true
      }
      // 调用点击报告公共的初始化参数方法
      await sceneAnalysisStore.setSceneOriginData({
        ...item,
        isDetail: true
      })
    }
  } catch (error) {
    //
    console.log('获取查询条件异常', error)
  }
  callback?.()
}
