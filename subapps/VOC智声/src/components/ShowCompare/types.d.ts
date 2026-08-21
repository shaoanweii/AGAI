/**
 * ShowCompare 组件类型定义
 */

/** 支持的比较键类型 */
export type CompareKeyType =
  | 'momTotalMentionValueRate' // 提及数环比
  | 'momExperienceValueRate' // 体验值环比
  | 'momNegativeMentionRate' // 负面提及率环比

/** 组件 Props 类型 */
export interface ShowCompareProps {
  /** 比较键名 */
  compareKey: CompareKeyType
  /** 比较数值，不用传百分比 */
  compareValue: number | string
  /** 自定义类名 */
  customClass?: string
}
