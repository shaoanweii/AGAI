import request from './index'

// export interface ProjectList {
//   records: Project[]
//   total: number
//   size: number
//   current: number
// }

export interface WarningEntity {
  projectId: string
  clientId: string
  /**
   * 1 业务问题
   * 2 质量故障
   * 3 投诉用户
   */
  riskType: 1 | 2 | 3
  brandCode: string[]
  pageNum?: number
  pageSize?: number
  startTime?: string
  endTime?: string
  keywords?: string
  riskLeve?: string
  statisticType?: string
}

/**
 * 查看预警数据
 * @param data
 * @returns
 */
export const postProjectList = (data: WarningEntity) => {
  return request<any>({
    url: '/insights/insProjectInfo/findRiskWarningData',
    method: 'POST',
    data
  })
}

/**
 * 导出预警数据
 * @param data
 */
export const exportRiskWarningData = (data: WarningEntity) => {
  return request({
    method: 'POST',
    url: '/insights/insProjectInfo/exportRiskWarningData',
    data,
    responseType: 'blob'
  })
}
