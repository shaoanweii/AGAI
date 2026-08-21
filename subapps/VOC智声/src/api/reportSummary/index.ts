// ==================报告总结===============================

import { TOKEN_KEY } from '@/constants'

/**
 * 全局请求队列管理器
 * 用于控制报告总结接口的并发请求数量
 */
class RequestQueue {
  private queue: Array<{
    task: () => Promise<void>
    resolve: () => void
    reject: (error: any) => void
  }> = []
  private running = 0
  private readonly maxConcurrent = 3

  async add(task: () => Promise<void>): Promise<void> {
    return new Promise((resolve, reject) => {
      const wrappedTask = { task, resolve, reject }

      // 如果当前运行数小于最大并发数，直接执行
      if (this.running < this.maxConcurrent) {
        this.run(wrappedTask)
      } else {
        // 否则加入队列等待
        this.queue.push(wrappedTask)
      }
    })
  }

  private async run(wrappedTask: {
    task: () => Promise<void>
    resolve: () => void
    reject: (error: any) => void
  }): Promise<void> {
    this.running++

    try {
      await wrappedTask.task()
      wrappedTask.resolve()
    } catch (error) {
      wrappedTask.reject(error)
    } finally {
      this.running--
      this.next()
    }
  }

  private next(): void {
    if (this.queue.length > 0 && this.running < this.maxConcurrent) {
      const wrappedTask = this.queue.shift()
      if (wrappedTask) {
        this.run(wrappedTask)
      }
    }
  }
}

// 导出全局单例
export const globalRequestQueue = new RequestQueue()

/**
 * 流式fetch请求封装
 * @param url 请求URL
 * @param data 请求参数
 * @returns Promise<Response> 返回原生Response对象用于流式处理
 */
const streamFetch = async (url: string, data: any): Promise<Response> => {
  const token = localStorage.getItem(TOKEN_KEY)
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

  // 从data中提取signal，其余作为请求体
  const { signal, ...requestData } = data || {}

  const response = await fetch(`${baseURL}${url}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
      ...(token && { Authorization: `Bearer ${token}` })
    },
    body: JSON.stringify(requestData),
    signal // 支持AbortController
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  return response
}

// ============================集团分析================================
/**
 * @description: 集团分析第一个模块，获取报告总结（流式接收）
 * @param data 请求参数
 * @returns Promise<Response> 返回原生Response对象用于流式处理
 */
export const getProductReportResult = (data: any): Promise<Response> => {
  return streamFetch('/report/group-analysis/getProductReportResult', data)
}

/**
 * @description: 服务口碑分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getServiceReputationAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/group-analysis/getServiceReputationAnalysisResult', data)
}

/**
 * @description: 产品分析接口报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getProductTagAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/group-analysis/getProductTagAnalysisResult', data)
}

/**
 * @description: 观点评价报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getOpinionEvaluationResult = (data: any): Promise<Response> => {
  return streamFetch('/report/group-analysis/getOpinionEvaluationResult', data)
}

/**
 * @description: 新车上市 数据来源分析
 * @param {any} data
 * @return {*}
 */
export const getNewCarDataSourceResult = (data: any): Promise<Response> => {
  return streamFetch('/report/new-car-launch/data-source-report', data)
}

/**
 * @description: 新车上市 关注场景TOP
 * @param {any} data
 * @return {*}
 */
export const getNewCarScenceTopResult = (data: any): Promise<Response> => {
  return streamFetch('/report/new-car-launch/getFocusSceneResult', data)
}

/**
 * @description: 新车上市 整体印象
 * @param {any} data
 * @return {*}
 */
export const getNewCarYingXiangResult = (data: any): Promise<Response> => {
  return streamFetch('/report/new-car-launch/getUseOpinionComparisonTopResult', data)
}

/**
 * @description: 新车上市 观点评价报告结果
 * @param {any} data
 * @return {*}
 */
export const getNewCarOpinionEvaluationResult = (data: any): Promise<Response> => {
  return streamFetch('/report/new-car-launch/getOpinionEvaluationResult', data)
}

/**
 * @description: 新车上市 综合分析数据趋势变化
 * @param {any} data
 * @return {*}
 */
export const getNewCarDataTrendChangeResult = (data: any): Promise<Response> => {
  return streamFetch('/report/new-car-launch/getDataTrendChangeResult', data)
}

/**
 * @description: 数据来源报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getDataSourceAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/group-analysis/getDataSourceAnalysisResult', data)
}

// ============================本品分析================================

/**
 * @description: 用户旅程分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getUserJourneyAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-self-analysis/getUserJourneyAnalysisResult', data)
}

/**
 * @description: 服务分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getServiceTagAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-self-analysis/getServiceTagAnalysisResult', data)
}

/**
 * @description: 产品分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getThisProductTagAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-self-analysis/getProductTagAnalysisResult', data)
}

/**
 * @description: 数据趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getDataTrendChangeResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-self-analysis/getDataTrendChangeResult', data)
}

/**
 * @description: 渠道负面率趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getChannelNegativeTrendResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-self-analysis/getChannelNegativeTrendResult', data)
}

// ============================旅程分析================================

/**
 * @description: 意图观点TOP报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getUserIntentionOpinionTopResult = (data: any): Promise<Response> => {
  return streamFetch('/report/journey-analysis/getUserIntentionOpinionTopResult', data)
}

/**
 * @description: 旅程细化分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getJourneyDetailAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/journey-analysis/getJourneyDetailAnalysisResult', data)
}

/**
 * @description: 关注场景TOP报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getFocusSceneTopResult = (data: any): Promise<Response> => {
  return streamFetch('/report/journey-analysis/getFocusSceneTopResult', data)
}

/**
 * @description: 数据趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getJADataTrendChangeResult = (data: any): Promise<Response> => {
  return streamFetch('/report/journey-analysis/getDataTrendChangeResult', data)
}

/**
 * @description: 渠道负面率趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getJAChannelNegativeTrendResult = (data: any): Promise<Response> => {
  return streamFetch('/report/journey-analysis/getChannelNegativeTrendResult', data)
}

// ============================产品分析================================
/**
 * @description: 关注场景分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getFocusSceneAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-analysis/getFocusSceneAnalysisResult', data)
}

/**
 * @description: 数据趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getPADataTrendChangeResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-analysis/getDataTrendChangeResult', data)
}

/**
 * @description: 渠道负面率趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getPAChannelNegativeTrendResult = (data: any): Promise<Response> => {
  return streamFetch('/report/product-analysis/getChannelNegativeTrendResult', data)
}

// ============================服务分析================================
/**
 * @description: 省份排行报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getProvinceRankResult = (data: any): Promise<Response> => {
  return streamFetch('/report/service-analysis/getProvinceRankResult', data)
}

/**
 * @description: 关注场景分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getSAFocusSceneAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/service-analysis/getFocusSceneAnalysisResult', data)
}

/**
 * @description: 数据趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getSADataTrendChangeResult = (data: any): Promise<Response> => {
  return streamFetch('/report/service-analysis/getDataTrendChangeResult', data)
}

/**
 * @description: 渠道负面率趋势变化报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getSAChannelNegativeTrendResult = (data: any): Promise<Response> => {
  return streamFetch('/report/service-analysis/getChannelNegativeTrendResult', data)
}

// ============================领导版================================

/**
 * @description: 品牌洞察-产品场景分析报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getProductScenarioAnalysisResult = (data: any): Promise<Response> => {
  return streamFetch('/report/vocLeadership/getProductScenarioAnalysisResult', data)
}

/**
 * @description: 品牌洞察-品牌-车系排行报告结果输出
 * @param {any} data
 * @return {*}
 */
export const getBrandRankingResult = (data: any): Promise<Response> => {
  return streamFetch('/report/vocLeadership/getBrandRankingResult', data)
}

// ============================竞品对比================================

/**
 * @description: 用户观点对比报告结果输出
 * @param {any} data
 * @return {*}
 */
export const userTopicComparisonResult = (data: any): Promise<Response> => {
  return streamFetch('/report/competitor-compare/userTopicComparisonResult', data)
}

/**
 * @description: 服务对比报告结果输出
 * @param {any} data
 * @return {*}
 */
export const serviceComparisonResult = (data: any): Promise<Response> => {
  return streamFetch('/report/competitor-compare/serviceComparisonResult', data)
}

/**
 * @description: 场景对比报告结果输出
 * @param {any} data
 * @return {*}
 */
export const sceneComparisonResult = (data: any): Promise<Response> => {
  return streamFetch('/report/competitor-compare/sceneComparisonResult', data)
}

/**
 * @description: 产品对比报告结果输出
 * @param {any} data
 * @return {*}
 */
export const productComparisonResult = (data: any): Promise<Response> => {
  return streamFetch('/report/competitor-compare/productComparisonResult', data)
}

/**
 * @description: 数据源对比报告结果输出
 * @param {any} data
 * @return {*}
 */
export const dataSourceComparisonResult = (data: any): Promise<Response> => {
  return streamFetch('/report/competitor-compare/dataSourceComparisonResult', data)
}

/**
 * @description: 综合对比报告结果输出
 * @param {any} data
 * @return {*}
 */
export const comprehensiveComparisonResult = (data: any): Promise<Response> => {
  return streamFetch('/report/competitor-compare/comprehensiveComparisonResult', data)
}

/**
 * @description: 重点账号 综合分析
 * @param {any} data
 * @return {*}
 */
export const getMainAccZhfxResult = (data: any): Promise<Response> => {
  return streamFetch('/report/keyAccount/getDataTrendChangeResult', data)
}

/**
 * @description: 重点账号 关注场景
 * @param {any} data
 * @return {*}
 */
export const getMainAccGzcjResult = (data: any): Promise<Response> => {
  return streamFetch('/report/keyAccount/getFocusSceneTopResult', data)
}

/**
 * @description: 重点账号 场景分析
 * @param {any} data
 * @return {*}
 */
export const getMainAccCjfzResult = (data: any): Promise<Response> => {
  return streamFetch('/report/keyAccount/getFocusSceneAnalysisResult', data)
}

/**
 * @description: 结果数据 综合分析
 * @param {any} data
 * @return {*}
 */
export const getJgsjZhfxResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getResultDataTrendChangeOut', data)
}

/**
 * @description: 原始数据 综合分析
 * @param {any} data
 * @return {*}
 */
export const getYssjZhfxResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getOriginDataTrendChangeOut', data)
}

/**
 * @description: 结果数据 关注场景
 * @param {any} data
 * @return {*}
 */
export const getYssjGzcjResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getFocusSceneResult', data)
}

/**
 * @description: 结果数据 场景分析
 * @param {any} data
 * @return {*}
 */
export const getYssjCjfxResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getFocusSceneAnalysisResult', data)
}

/**
 * @description: 结果数据 观点评价
 * @param {any} data
 * @return {*}
 */
export const getYssjGdpjResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getOpinionEvaluationResult', data)
}

/**
 * @description: 结果数据 数据来源分析
 * @param {any} data
 * @return {*}
 */
export const getJgsjSjlyResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getChannelNegativeTrendResult', data)
}
/**
 * @description: 原始数据 数据来源分析
 * @param {any} data
 * @return {*}
 */
export const getYssjSjlyResult = (data: any): Promise<Response> => {
  return streamFetch('/report/hot-event/getOriginDataSourceAnalysisResult', data)
}
