import type { ConditionsDetailItem } from '@/types'

/**
 * 字典接口的 key 可能存在命名差异，这里统一做容错映射，保证列表与弹框状态选项一致。
 */
export const resolveCarUsageScenarioStatusOptions = (
  source: Record<string, ConditionsDetailItem[]>
) => {
  const candidateKeys = ['stopOrEnable']
  for (const key of candidateKeys) {
    if (Array.isArray(source[key]) && source[key].length) {
      return source[key]
    }
  }
  return []
}
