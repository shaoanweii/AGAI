import request from './index'

/** 启用状态值，后端约定：1=启用，0=禁用 */
const ENABLE_STATUS = {
  ENABLED: '1',
  DISABLED: '0'
} as const

type EnableStatus = (typeof ENABLE_STATUS)[keyof typeof ENABLE_STATUS]

/** 统一分页结果：兼容 list / records 两种结构 */
const toPageResult = <T>(result: any): Api.BrandSeries.PageResult<T> => {
  const list = result?.list || result?.records || []
  const total = Number(result?.total || 0)
  return { list, total }
}

export const brandSeriesEnableStatus = ENABLE_STATUS

/** 车企模块过滤条件 */
export const getAutomakerConditions = () => {
  return request<Api.BrandSeries.ConditionGroup[]>({
    method: 'GET',
    url: '/insights/automark/conditions'
  })
}

/** 品牌模块过滤条件 */
export const getBrandConditions = () => {
  return request<Api.BrandSeries.ConditionGroup[]>({
    method: 'GET',
    url: '/insights/brandInfo/conditions'
  })
}

/** 车系模块过滤条件 */
export const getSeriesConditions = () => {
  return request<Api.BrandSeries.ConditionGroup[]>({
    method: 'GET',
    url: '/insights/carSeriesInfo/conditions'
  })
}

/** 车企分页查询 */
export const queryAutomakerPage = (data: Api.BrandSeries.Automaker) => {
  return request<Api.BrandSeries.PageResult<Api.BrandSeries.Automaker>>({
    method: 'POST',
    url: '/insights/automark/findAutomarkList',
    data
  }).then(res => ({ ...res, result: toPageResult<Api.BrandSeries.Automaker>(res.result) }))
}

/** 按 id 查询车企详情 */
export const findAutomakerById = (data: Pick<Api.BrandSeries.Automaker, 'id'>) => {
  return request<Api.BrandSeries.Automaker>({
    method: 'POST',
    url: '/insights/automark/findAutomarkInfo',
    data
  })
}

/** 查询车企列表（非分页） */
export const findAutomakerList = (data: Api.BrandSeries.Automaker) => {
  return request<Api.BrandSeries.Automaker[]>({
    method: 'POST',
    url: '/insights/automark/findAutomarkInfoList',
    data
  })
}

/** 新增车企 */
export const createAutomaker = (data: Api.BrandSeries.Automaker) => {
  return request({
    method: 'POST',
    url: '/insights/automark/saveAutomark',
    data
  })
}

/** 编辑车企 */
export const updateAutomaker = (data: Api.BrandSeries.Automaker) => {
  return request({
    method: 'POST',
    url: '/insights/automark/updateAutomark',
    data
  })
}

/** 车企批量启用/禁用 */
export const batchUpdateAutomakerStatus = (ids: string[], status: EnableStatus) => {
  return request({
    method: 'POST',
    url: '/insights/automark/batchChangeStatus',
    data: { ids, status }
  })
}

/** 车企渠道树 */
export const getAutomakerChannelTree = (params: Api.BrandSeries.TreeQuery) => {
  return request<any>({
    method: 'GET',
    url: '/insights/automark/getChannelTree',
    params
  })
}

/** 车企标签分类树 */
export const getAutomakerTagLibCategoryTree = (params: Api.BrandSeries.TreeQuery) => {
  return request<any>({
    method: 'GET',
    url: '/insights/automark/findTagLibCategoryTree',
    params
  })
}

/** 品牌分页查询 */
export const queryBrandPage = (data: Api.BrandSeries.Brand) => {
  return request<Api.BrandSeries.PageResult<Api.BrandSeries.Brand>>({
    method: 'POST',
    url: '/insights/brandInfo/queryBySelect',
    data
  }).then(res => ({ ...res, result: toPageResult<Api.BrandSeries.Brand>(res.result) }))
}

/** 新增品牌 */
export const createBrand = (data: Api.BrandSeries.Brand) => {
  return request({
    method: 'POST',
    url: '/insights/brandInfo/addBrandInfo',
    data
  })
}

/** 按 id 查询品牌详情 */
export const findBrandById = (data: Pick<Api.BrandSeries.Brand, 'id'>) => {
  return request<Api.BrandSeries.Brand>({
    method: 'POST',
    url: '/insights/brandInfo/findInsBrandInfo',
    data
  })
}

/** 按条件查询品牌 */
export const findBrandByParam = (data: Api.BrandSeries.Brand) => {
  return request<Api.BrandSeries.Brand[]>({
    method: 'POST',
    url: '/insights/brandInfo/findByParam',
    data
  })
}

/** 编辑品牌 */
export const updateBrand = (data: Api.BrandSeries.Brand) => {
  return request({
    method: 'POST',
    url: '/insights/brandInfo/updateBrandInfo',
    data
  })
}

/** 删除品牌 */
export const deleteBrand = (data: Pick<Api.BrandSeries.Brand, 'id'>) => {
  return request({
    method: 'POST',
    url: '/insights/brandInfo/deleteBrandInfo',
    data
  })
}

/** 品牌批量启用/禁用 */
export const batchUpdateBrandStatus = (ids: string[], status: EnableStatus) => {
  return request({
    method: 'POST',
    url: '/insights/brandInfo/batchChangeStatus',
    data: { ids, status }
  })
}

/** 品牌渠道树 */
export const getBrandChannelTree = (params: Api.BrandSeries.TreeQuery) => {
  return request<any>({
    method: 'GET',
    url: '/insights/brandInfo/getChannelTree',
    params
  })
}

/** 品牌标签分类树 */
export const getBrandTagLibCategoryTree = (params: Api.BrandSeries.TreeQuery) => {
  return request<any>({
    method: 'GET',
    url: '/insights/brandInfo/findTagLibCategoryTree',
    params
  })
}

/** 查询全部品牌 */
export const findAllBrands = () => {
  return request<Api.BrandSeries.Brand[]>({
    method: 'GET',
    url: '/insights/brandInfo/findAll'
  })
}

/** 查询全部品牌与车系 */
export const findAllBrandAndSeries = () => {
  return request<Api.BrandSeries.BrandAndSeries[]>({
    method: 'GET',
    url: '/insights/brandInfo/findAllBrandAndCarSeries'
  })
}

/** 车系分页查询 */
export const querySeriesPage = (data: Api.BrandSeries.Series) => {
  return request<Api.BrandSeries.PageResult<Api.BrandSeries.Series>>({
    method: 'POST',
    url: '/insights/carSeriesInfo/queryBySelect',
    data
  }).then(res => ({ ...res, result: toPageResult<Api.BrandSeries.Series>(res.result) }))
}

/** 新增车系 */
export const createSeries = (data: Api.BrandSeries.Series) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/addCarSeriesInfo',
    data
  })
}

/** 按 id 查询车系详情 */
export const findSeriesById = (data: Pick<Api.BrandSeries.Series, 'id'>) => {
  return request<Api.BrandSeries.Series>({
    method: 'POST',
    url: '/insights/carSeriesInfo/findCarSeriesInfo',
    data
  })
}

/** 按 ids 查询车系 */
export const findSeriesByIds = (data: Pick<Api.BrandSeries.Series, 'ids'>) => {
  return request<Api.BrandSeries.Series[]>({
    method: 'POST',
    url: '/insights/carSeriesInfo/findCarSeriesByIds',
    data
  })
}

/** 按条件查询车系 */
export const findSeriesByParam = (data: Api.BrandSeries.Series) => {
  return request<Api.BrandSeries.Series[]>({
    method: 'POST',
    url: '/insights/carSeriesInfo/findByParam',
    data
  })
}

/** 编辑车系 */
export const updateSeries = (data: Api.BrandSeries.Series) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/updateCarSeriesInfo',
    data
  })
}

/** 删除车系 */
export const deleteSeries = (data: Pick<Api.BrandSeries.Series, 'id'>) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/deleteCarSeriesInfo',
    data
  })
}

/** 车系批量启用/禁用 */
export const batchUpdateSeriesStatus = (ids: string[], status: EnableStatus) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/batchChangeStatus',
    data: { ids, status }
  })
}

/** 车系 Excel 导入 */
export const uploadSeriesExcel = (data: object) => {
  return request({
    method: 'POST',
    url: '/insights/carSeriesInfo/uploadExcel',
    data
  })
}

/** 车系渠道树 */
export const getSeriesChannelTree = (params: Api.BrandSeries.TreeQuery) => {
  return request<any>({
    method: 'GET',
    url: '/insights/carSeriesInfo/getChannelTree',
    params
  })
}

/** 车系标签分类树 */
export const getSeriesTagLibCategoryTree = (params: Api.BrandSeries.TreeQuery) => {
  return request<any>({
    method: 'GET',
    url: '/insights/carSeriesInfo/findTagLibCategoryTree',
    params
  })
}
