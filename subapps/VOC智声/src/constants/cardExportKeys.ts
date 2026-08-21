/**
 * 分析页卡片导出 key。
 *
 * 维护约定：
 * - key 用于匹配统计导出接口，不跟随页面标题文案变化。
 * - 页面新增可下载 FCard 时，优先在对应模块下补 key。
 * - 只有卡片业务含义变化时才调整 key，单纯改标题不改 key。
 */
export const CARD_EXPORT_KEYS = {
  /** 集团分析 */
  group: {
    Composite: 'group.composite',
    Service: 'group.service',
    Product: 'group.product',
    Opinion: 'group.opinion',
    DataSource: 'group.dataSource'
  },

  /** 竞品对比 */
  competitor: {
    Composite: 'competitor.composite',
    Service: 'competitor.service',
    Product: 'competitor.product',
    Scene: 'competitor.scene',
    Opinion: 'competitor.opinion',
    DataSource: 'competitor.dataSource'
  },

  /** 本品分析 */
  productSelf: {
    Composite: 'productSelf.composite',
    UserJourney: 'productSelf.userJourney',
    Service: 'productSelf.service',
    Product: 'productSelf.product',
    DataSource: 'productSelf.dataSource'
  },

  /** 旅程分析 */
  journey: {
    Composite: 'journey.composite',
    Crowd: 'journey.crowd',
    Detail: 'journey.detail',
    Opinion: 'journey.opinion',
    DataSource: 'journey.dataSource'
  },

  /** 产品分析 */
  product: {
    Composite: 'product.composite',
    FocusScene: 'product.focusScene',
    DataSource: 'product.dataSource'
  },

  /** 服务分析 */
  service: {
    Composite: 'service.composite',
    FocusScene: 'service.focusScene',
    Region: 'service.region',
    DataSource: 'service.dataSource'
  },

  /** 领导总览 */
  leader: {
    MarketComparison: 'leader.marketComparison',
    BrandInsight: 'leader.brandInsight'
  }
} as const
