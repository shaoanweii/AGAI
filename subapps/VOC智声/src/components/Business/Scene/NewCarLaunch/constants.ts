/**
 * @description: 数据类型 枚举 品牌 车系
 * @return {*}
 */
export enum QueryType {
  // 品牌
  Brand = 'brand',
  //车系
  Series = 'series'
}

// 变量数据
export const INTERVAL_TYPE_OPTIONS = [
  { key: 'complaint', value: '预热期' },
  { key: 'consultation', value: '上市期' },
  { key: 'suggestion', value: '稳定期' }
]
