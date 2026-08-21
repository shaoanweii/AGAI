import type { InjectionKey, Ref } from 'vue'
import type { ConditionsDetailItem } from '@/types'

/**
 * 当前页面共享的用车场景字典上下文，供列表与后续弹框复用。
 */
export interface CarUsageScenarioPageContext {
  conditionLoading: Ref<boolean>
  conditionMap: Ref<Record<string, ConditionsDetailItem[]>>
  refreshConditions: () => Promise<void>
}

export const carUsageScenarioPageContextKey: InjectionKey<CarUsageScenarioPageContext> = Symbol(
  'carUsageScenarioPageContext'
)
