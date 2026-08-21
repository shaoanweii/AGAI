// 去除无值的key
export const getRealAttr = (obj: any) => {
  const result = {} as any
  for (const key in obj) {
    if (obj[key] !== '' && obj[key] !== null && obj[key] !== undefined) {
      result[key] = obj[key]
    }
  }
  return result
}

const leaderGroupCode = 'groupCode'
const leaderGroupName = '智行汽车集团'

export interface BrandRankingQueryOptions {
  queryType?: string
  dataType?: string
  sortField?: string
  sortOrder?: string | null
}

/**
 * 构造领导总览品牌排行请求参数。
 * 未传排行选项时使用页面初始化默认值，供排行数据和对应报告总结复用。
 *
 * @param params 当前页面查询条件
 * @param options 当前排行维度、指标和排序条件
 * @returns 已移除空值的品牌排行请求参数
 */
export const getBrandRankingQueryParams = (
  params: VocQueryParams,
  options: BrandRankingQueryOptions = {}
): VocQueryParams => {
  const isGroupSelected = !params.brandCode || params.brandCode === leaderGroupCode

  return getRealAttr({
    brandCode: params.brandCode,
    startDate: params.startDate,
    endDate: params.endDate,
    queryType: options.queryType ?? (isGroupSelected ? 'seriesFactory' : 'brand'),
    dataType: options.dataType ?? 'mention',
    sortField: options.sortField,
    sortOrder: options.sortOrder
  })
}

/**
 * 构造领导总览产品场景分析请求参数。
 * 集团占位值转换为后端车企字段，具体品牌则转换为品牌编码，并移除前端临时字段。
 *
 * @param params 当前页面查询条件
 * @returns 已完成品牌转换并移除空值的产品场景分析请求参数
 */
export const getProductScenarioAnalysisQueryParams = (
  params: VocQueryParams
): VocQueryParams => {
  const brandParams =
    params.tempCode === leaderGroupCode
      ? {
          automark: leaderGroupName,
          brandCode: undefined,
          tempCode: undefined
        }
      : {
          brandCode: params.tempCode,
          automark: undefined,
          tempCode: undefined
        }

  return getRealAttr({
    ...params,
    ...brandParams
  })
}
