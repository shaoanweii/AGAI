/**
 * 数据字典相关API接口
 */

import http from '@/api/http'
import type {
  DictTypeModel,
  DictItemModel,
  DictQueryParams,
  DictItemQueryParams,
  DictPageResult,
  DictItemPageResult,
  DictItemListResult,
  DictDetailResult,
  DictItemDetailResult
} from './index.d'

// 数据字典类型管理接口
export const dictApi = {
  // 分页查询数据字典列表
  getDictList: (params: DictQueryParams): Promise<DictPageResult> => {
    return http.post('/report/insDict/dict-list', params)
  },

  // 新增数据字典
  createDict: (data: DictTypeModel): Promise<BaseResponse<number>> => {
    return http.post('/report/insDict/insert', data)
  },

  // 更新数据字典
  updateDict: (data: DictTypeModel): Promise<BaseResponse<number>> => {
    return http.post('/report/insDict/update', data)
  },

  // 根据ID查询数据字典详情
  getDictDetail: (id: string): Promise<DictDetailResult> => {
    return http.get(`/report/insDict/dict-detail/${id}`)
  },

  // 删除数据字典
  deleteDict: (id: string): Promise<BaseResponse<number>> => {
    return http.delete(`/report/insDict/delete/${id}`)
  },

  // 批量删除数据字典
  batchDeleteDict: (ids: string[]): Promise<BaseResponse<number>> => {
    return http.post('/report/insDict/batch-delete', ids)
  }
}

// 数据字典项管理接口
export const dictItemApi = {
  // 分页查询数据字典项列表
  getDictItemList: (params: DictItemQueryParams): Promise<DictItemPageResult> => {
    return http.post('/report/insDictItem/dict-item-list', params)
  },

  // 根据字典ID查询所有字典项
  getDictItemsByDictId: (dictId: string): Promise<DictItemListResult> => {
    return http.get(`/report/insDictItem/dict-items-by-dict/${dictId}`)
  },

  // 新增数据字典项
  createDictItem: (data: DictItemModel): Promise<BaseResponse<number>> => {
    return http.post('/report/insDictItem/insert', data)
  },

  // 更新数据字典项
  updateDictItem: (data: DictItemModel): Promise<BaseResponse<number>> => {
    return http.post('/report/insDictItem/update', data)
  },

  // 根据ID查询数据字典项详情
  getDictItemDetail: (id: string): Promise<DictItemDetailResult> => {
    return http.get(`/report/insDictItem/dict-item-detail/${id}`)
  },

  // 删除数据字典项
  deleteDictItem: (id: string): Promise<BaseResponse<number>> => {
    return http.delete(`/report/insDictItem/delete/${id}`)
  }
}
