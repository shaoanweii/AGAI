/**
 * 条件限制项
 */
export interface ConditionItem {
  /**
   * 字段
   */
  fieldName: string
  /**
   * 变量值
   */
  variableValue: string
  /**
   * 逻辑运算符
   */
  logicalOperator: string
  /**
   * 条件类型
   */
  conditionType: string
  /**
   * 条件详情
   */
  conditionDetail: string
  /**
   * 序号
   */
  serialNumber: string
}

/**
 * 定义一个类型接口，用于描述所需的校验请求参数。
 */
export interface ValidationRequest {
  /**
   * 客户端ID。
   */
  clientId: string

  /**
   * 本次执行校验规则的ID集合。
   */
  validRuleIds?: string[]

  /**
   * 启用的规则的系统集合。
   */
  enabledRuleIds?: string[]

  /**
   * 数据处理链路标识。
   */
  wordId?: string

  /**
   * 开始时间。
   */
  startTime?: string

  /**
   * 结束时间。
   */
  endTime?: string

  /**
   * 筛选条件。
   */
  attrs?: RegulationDetailsVo[]

  /**
   * 本次条件范围内数据量。
   */
  dataCount?: number

  /**
   * 校验状态。
   * -1 表示未校验
   * 0 表示校验中
   * 1 表示校验成功
   * 2 表示校验失败
   * 默认为 -1
   */
  validateStatus?: string
}

/**
 * 定义 RegulationDetailsVo 类型接口，用于描述筛选条件的具体细节。
 */
export interface RegulationDetailsVo {
  /**
   * ID。
   */
  id?: string

  /**
   * 规则ID。
   */
  regulationId?: string

  /**
   * 字段名。
   */
  fieldName: string

  /**
   * 字段名文本。
   */
  fieldNameText?: string

  /**
   * 变量值。
   */
  variableValue: string

  /**
   * 逻辑运算符。
   */
  logicalOperator: string

  /**
   * 条件类型。
   */
  conditionType: string

  /**
   * 条件详细信息。
   */
  conditionDetail: string

  /**
   * 条件详细信息文本。
   */
  conditionDetailText?: string

  /**
   * 详细类型。
   */
  detailType?: string

  /**
   * 序列号。
   */
  serialNumber: string
}
