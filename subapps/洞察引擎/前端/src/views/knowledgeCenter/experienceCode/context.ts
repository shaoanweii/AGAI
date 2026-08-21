import type { ComputedRef, InjectionKey, Ref } from 'vue'
import type {
  ExperienceCategoryItem,
  ExperienceCategoryTypeSummary,
  ExperienceCodeTypeOption,
  OptionItem,
  StatusValue
} from './components/types'

/**
 * 体验代码页面共享的分类元数据，供左右两侧列表复用同一份分类结果。
 */
export interface ExperienceCodePageCategoryData {
  categories: ExperienceCategoryItem[]
  typeSummaries: ExperienceCategoryTypeSummary[]
}

/**
 * 页面级共享上下文，集中维护分类缓存与刷新方法，避免左右两侧各自维护同类请求。
 */
export interface ExperienceCodePageContext {
  categoryData: Ref<ExperienceCodePageCategoryData>
  refreshCategoryData: (options?: { force?: boolean }) => Promise<ExperienceCodePageCategoryData>
  typeOptions: ComputedRef<ExperienceCodeTypeOption[]>
  statusOptions: ComputedRef<OptionItem<StatusValue>[]>
}

export const experienceCodePageContextKey: InjectionKey<ExperienceCodePageContext> = Symbol(
  'experienceCodePageContext'
)
