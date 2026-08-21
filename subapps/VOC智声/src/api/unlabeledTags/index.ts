import request from '@/api/http'
import type { TaskDistributionModel } from './types'

/**
 * 未达标数据-列表：单点事件任务下发。
 *
 * @param data 事件下发参数
 * @returns 后端下发结果
 */
export const taskDistribution = (data: TaskDistributionModel): Promise<BaseResponse<boolean>> => {
  return request({
    url: '/report/tags/taskDistribution',
    method: 'post',
    data
  })
}
