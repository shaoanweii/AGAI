import type { DataPlazaConditionGroup, DataPlazaConditionOption } from '@/api/dataPlaza/types'

export const USAGE_SCENARIO_PROP = 'usageScenarioCodes'

/**
 * 从数据广场筛选条件中提取用车场景树。
 * @param result 数据广场 conditions 接口返回结果
 * @returns 用车场景一、二级选项
 */
export function extractUsageScenarioOptions(result: unknown): DataPlazaConditionOption[] {
  if (Array.isArray(result)) {
    const conditionGroups = result as DataPlazaConditionGroup[]
    const carSceneGroup = conditionGroups.find(item => item?.key === 'carScene')

    if (Array.isArray(carSceneGroup?.details)) {
      return carSceneGroup.details
    }

    // 兼容接口直接返回 carScene 明细数组的情况。
    return result.filter((item: any) => item?.value || item?.children) as DataPlazaConditionOption[]
  }

  const resultMap = result as Record<string, any>
  return Array.isArray(resultMap?.carScene) ? resultMap.carScene : []
}
