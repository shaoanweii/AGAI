import request from './index'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from './constants'

export interface OpinionSynonymCreatePayload {
  entity?: string | null
  description?: string | null
  opinion?: string | null
  standard_opinion: string
  standard_opinion_id: string
  status_name: 'enabled' | 'disabled'
  opinion_type: 0 | 1
  // 操作人（可选）
  operator?: string | null
}

export interface OpinionSynonymUpdateByIdPayload {
  // 支持单个 ID 或 ID 数组，对应接口文档的 old_id: int | int[]
  old_id: number | number[]
  // 支持单条对象或对象数组，对应接口文档的 new: object | object[]
  new: OpinionSynonymCreatePayload | OpinionSynonymCreatePayload[]
}

export const createOpinionSynonym = (data: OpinionSynonymCreatePayload) => {
  return request<any>({
    url: '/ai/opinion-synonyms',
    method: 'POST',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}

export const updateOpinionSynonymById = (data: OpinionSynonymUpdateByIdPayload) => {
  return request<any>({
    url: '/ai/opinion-synonyms',
    method: 'PUT',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}
