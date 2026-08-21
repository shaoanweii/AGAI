import request from './index'

/** 属性标签启用状态，后端当前约定：1=启用，0=禁用。 */
const ENABLE_STATUS = {
  ENABLED: '1',
  DISABLED: '0'
} as const

export const attributeLabelEnableStatus = ENABLE_STATUS

/** 新增属性标签。 */
export const createAttributeLabel = (data: Api.AttributeLabel.SavePayload) => {
  return request({
    method: 'POST',
    url: '/insights/attributeLabel/saveAttributeLabel',
    data
  })
}

/** 编辑属性标签。 */
export const updateAttributeLabel = (data: Api.AttributeLabel.SavePayload) => {
  return request({
    method: 'POST',
    url: '/insights/attributeLabel/updateAttributeLabel',
    data
  })
}

/** 批量修改属性标签启用状态。 */
export const batchChangeAttributeLabelStatus = (
  data: Api.AttributeLabel.BatchChangeStatusPayload
) => {
  return request({
    method: 'POST',
    url: '/insights/attributeLabel/batchChangeStatus',
    data
  })
}

/** 查询全部属性标签列表，用于观点表单多选。 */
export const findAllAttributeLabelList = (data: any) => {
  return request<Api.AttributeLabel.RecordItem[]>({
    method: 'POST',
    url: '/insights/attributeLabel/findAllAttributeLabelList',
    data
  })
}
