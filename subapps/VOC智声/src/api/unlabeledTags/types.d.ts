/**
 * 未达标数据事件下发参数。
 * 字段来源于 Swagger: /report/tags/taskDistribution。
 */
export interface TaskDistributionModel {
  /** 原始数据 id 集合 */
  dataId?: string[]
  /** voc_sentiment_annotations_results_v 视图主键 id 集合 */
  id?: string[]
  /** 事件名称 */
  riskName?: string
  /** 事件优先级 */
  eventPriority?: string
  /** 事件等级 */
  eventLevel?: string
  /** 品牌名称 */
  brandName?: string
  /** 品牌编码 */
  brandCode?: string
  /** 主责组织 ID */
  mainRespOrgId?: string
  /** 主责组织编号 */
  mainRespOrgNo?: string
  /** 主责组织名称 */
  mainRespOrgName?: string
  /** 主责人 ID */
  mainRespUserId?: string
  /** 主责人工号 */
  mainRespUserEmpNo?: string
  /** 主责人名称 */
  mainRespUserName?: string
  /** 意图 */
  intention?: string
  /** 全领域 1 级 Code */
  domTagFirstCode?: string
  /** 全领域 1 级名称 */
  domTagFirst?: string
  /** 全领域 2 级 Code */
  domTagSecondCode?: string
  /** 全领域 2 级名称 */
  domTagSecond?: string
  /** 全领域 3 级 Code */
  domTagThreeCode?: string
  /** 全领域 3 级名称 */
  domTagThree?: string
  /** 全领域 4 级 Code */
  domTagFourCode?: string
  /** 全领域 4 级名称 */
  domTagFour?: string
  /** 观点名称 */
  topic?: string
  /** 观点编码 */
  topicCode?: string
  /** 备注 */
  remark?: string
}
