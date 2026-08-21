/**
 * VoC 总览模块类型定义
 */

// 集团简报响应数据
export interface GroupBreifVo {
  /** 名称 */
  brandName?: string
  /** 品牌代码 */
  brandCode?: string
  /** 图片地址 */
  brandImage?: string
  topDataList?: any
}

// 品牌简报响应数据
export interface ProductExperienceIndexVo {
  /** 名称 */
  name?: string
  /** 品牌代码 */
  brandCode?: string
  /** 图片地址 */
  imgUrl?: string
  /** 负面率 */
  negativeRate?: number
  /** 增长量 */
  growth?: number
  /** 提及量 */
  mentionCount?: number
  /** 增长趋势 */
  growthTrend?: number[]
  // 文字颜色
  rateColor?: string
}

// 专项分析响应数据
export interface HomeReportTopVo {
  /** 报告名称 */
  reportName?: string
  /**
   * 报告地址
   * 报告的访问URL地址
   */
  reportUrl?: string
  /** ID */
  id?: string
  /** 状态 0-未查看 1-已查看 */
  status?: number
}

// 热点事件响应数据
export interface HotEventsVo {
  /** 名称 */
  name?: string
  /** 提及量 */
  mentionCount?: number
  /** 增长量 */
  growth?: number
  /** 标注 */
  label?: string
}

// 客户吐槽响应数据
export interface CustomerTeasingVo {
  /** 标题 */
  title?: string
  /** 提及量 */
  mentionCount?: number
  /** 原文id */
  dataId?: string
  /** 提及内容 */
  mentionContent?: string
  /** 提及时间 */
  mentionTime?: string
  /** 客户名称 */
  customerName?: string
}

// 客情直驱响应数据
export interface CustomerEmotionVo {
  /** 客户情感相关数据 */
  [key: string]: any
}

// 查询条件响应数据
export interface QueryConditionsVo {
  /** 查询条件相关数据 */
  [key: string]: any
}

// 标签类型响应数据
export interface TagTypeVo {
  /** 标签类型相关数据 */
  [key: string]: any
}

// 风险等级响应数据
export interface RiskLevelVo {
  /** 风险等级相关数据 */
  [key: string]: any
}

// 时间响应数据
export interface TimeVo {
  /** 时间相关数据 */
  [key: string]: any
}

export interface HomeMenuVo {
  /** 菜单名称 */
  name?: string
  /** HTML页面URI */
  htmlUri?: string
  /** 小图 */
  smallImage?: string
  /** 大图 */
  bigImage?: string
  /** 描述信息 */
  description?: string
}

export interface SpecialZoneTreeVo {
  /**
   * 唯一标识符
   */
  id?: string

  /**
   * 区域名称
   */
  name?: string

  /**
   * 父级区域的唯一标识符
   * 顶级区域可能没有父级ID
   */
  pid?: string

  /**
   * 区域类型
   * 1: 一级区域
   * 2: 二级区域
   */
  type?: 1 | 2

  /**
   * 区域图标
   * 通常为图标名称或图标路径
   */
  icon?: string

  /**
   * 排序号
   * 用于控制区域在列表中的显示顺序
   */
  sortNo?: number

  /**
   * 创建时间
   * 格式为 date-time (例如: 2023-09-22T10:30:00Z)
   */
  createTime?: string

  /**
   * 子节点列表
   * 包含当前区域的所有二级子区域
   */
  children?: SpecialZoneTreeVo[]
}

/**
 * 报告查询参数模型
 * 用于筛选、分页查询报告相关信息
 */
export interface PublishReportQueryParams {
  /**
   * 每页条数
   * 用于分页查询，指定每页显示的记录数量
   */
  pageSize?: number

  /**
   * 页码
   * 用于分页查询，指定当前查询的页码
   */
  pageNum?: number

  /**
   * 排序方式
   * 通常指定排序字段和排序方向，如"createTime desc"
   */
  order?: string

  /**
   * 主键ID
   * 报告的唯一标识符
   */
  id?: string

  /**
   * 报告名称
   * 用于按报告名称筛选
   */
  reportName?: string

  /**
   * 浏览量
   * 报告被查看的次数
   */
  viewCount?: number

  /**
   * 收藏数
   * 报告被收藏的次数
   */
  collectionCount?: number

  /**
   * 类型
   * 报告的分类类型标识
   */
  type?: number

  /**
   * 默认条件
   * 以JSON字符串形式存储的默认筛选条件
   */
  defaultCondition?: string

  /**
   * 时间条件
   * 以JSON字符串形式存储的时间范围筛选条件
   */
  dateCondition?: string

  /**
   * 品牌编码
   * 关联的品牌标识，用于按品牌筛选报告
   */
  brandCode?: string

  /**
   * 一级专区ID
   * 所属一级专区的唯一标识符
   */
  firstLevelZoneId?: string

  /**
   * 二级专区ID/专项分析类型ID
   * 所属二级专区或专项分析类型的唯一标识符
   */
  specialTypeId?: string

  /**
   * 状态
   * 报告的状态标识（如：草稿、发布、下架等）
   */
  status?: number

  /**
   * 创建时间
   * 报告的创建时间，格式为date-time（例如：2023-09-22T10:30:00Z）
   */
  createTime?: string

  /**
   * 更新时间
   * 报告的最后更新时间，格式为date-time
   */
  updateTime?: string

  /**
   * 创建人
   * 报告的创建者标识
   */
  createBy?: string

  /**
   * 更新人
   * 最后更新报告的用户标识
   */
  updateBy?: string

  /**
   * 描述
   * 报告的详细说明信息
   */
  description?: string

  /**
   * 报告地址
   * 报告的访问URL地址
   */
  reportUrl?: string

  [key: string]: any
}
