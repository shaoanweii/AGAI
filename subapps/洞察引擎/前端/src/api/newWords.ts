import request from './index'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from './constants'

export interface NewWordsListParams {
  start_date: string
  end_date: string
  page: number
  page_size: number
  batch_id?: string
  entity?: string
  description?: string
  full_opinion?: string
  recommend_topic_text?: string
  status?: number
  opinion_type?: 0 | 1
  operator?: string
}

export interface RecommendTopicItem {
  topic_id: string
  topic_text: string
  similarity: number
  rank: number
  frequency: number
}

export interface NewWordItem {
  id: number | string
  batch_id: string
  opinion_type: 0 | 1
  entity: string | null
  description: string | null
  full_opinion: string
  data_id_list: string[]
  frequency: number
  status: number
  selected_topic: string | null
  selected_category: string | null
  recommend_topic_list: RecommendTopicItem[]
  modification_history: any[]
  created_time: string
  processed_time: string | null
  last_modified_time: string | null
  recommended_topic: string | null
}

export interface NewWordsListResult {
  total: number
  page: number
  page_size: number
  items: NewWordItem[]
}

export const listNewWords = (data: NewWordsListParams) => {
  return request<NewWordsListResult>({
    url: '/new-words/search',
    method: 'POST',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}

export interface UpdateNewWordPayload {
  id: number | string
  status: 0 | 1 | 2
  selected_topic_id?: string
  selected_topic_text?: string
  remark?: string
  username: string
  user_id: string
}

export const updateNewWord = (data: UpdateNewWordPayload) => {
  return request<NewWordItem>({
    url: '/new-words/update',
    method: 'POST',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}

export interface BatchUpdateNewWordsPayload {
  ids: Array<number | string>
  operation: 'enable' | 'disable'
  user_id?: string
  username?: string
  remark?: string
}

export interface BatchUpdateNewWordsResult {
  success_count: number
  failed_count: number
  failed_ids: Array<number | string>
}

export const batchUpdateNewWords = (data: BatchUpdateNewWordsPayload) => {
  return request<BatchUpdateNewWordsResult>({
    url: '/new-words/batch-update',
    method: 'POST',
    data,
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  })
}
