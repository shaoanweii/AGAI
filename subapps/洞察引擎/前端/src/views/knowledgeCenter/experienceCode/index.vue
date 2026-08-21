<script setup lang="ts">
import { computed, provide, ref } from 'vue'
import useConditions from '@/hooks/useConditions'
import { CategoryList, ExperienceCodeList, type ExperienceFilterTarget } from './components'
import { fetchExperienceCategoryData } from './service'
import { experienceCodePageContextKey, type ExperienceCodePageCategoryData } from './context'
import { resolveExperienceCodeStatusOptions } from './statusOptions'
import { buildExperienceCodeTypeOptions } from './components/types'

defineOptions({
  name: 'KnowledgeCenterExperienceCode'
})

interface CategoryListRefreshOptions {
  refreshFull?: boolean
}

interface ExperienceCategoryListExposed {
  refreshList: (options?: CategoryListRefreshOptions) => Promise<void>
}

interface ExperienceCodeListExposed {
  refreshList: () => Promise<void>
}

const categoryListRef = ref<ExperienceCategoryListExposed | null>(null)
const experienceCodeListRef = ref<ExperienceCodeListExposed | null>(null)
const { conditions } = useConditions({ url: '/insights/insTagLibClient/conditions' })

const activeFilterTarget = ref<ExperienceFilterTarget | null>(null)
const INITIAL_CATEGORY_DATA: ExperienceCodePageCategoryData = {
  categories: [],
  typeSummaries: []
}

const categoryData = ref<ExperienceCodePageCategoryData>(INITIAL_CATEGORY_DATA)
const typeOptions = computed(() => {
  const categoryTypeOptions = categoryData.value.typeSummaries.map(item => ({
    label: item.label,
    value: item.typeCode
  }))

  return categoryTypeOptions.length
    ? categoryTypeOptions
    : buildExperienceCodeTypeOptions(conditions.tagLibeType || [])
})
const statusOptions = computed(() => resolveExperienceCodeStatusOptions(conditions))

/**
 * 页面层统一维护当前页分类元数据，供左右两侧与弹框共享最新结果。
 */
const refreshCategoryData = async (options: { force?: boolean } = {}) => {
  const nextCategoryData = await fetchExperienceCategoryData(options)
  categoryData.value = nextCategoryData
  return nextCategoryData
}

provide(experienceCodePageContextKey, {
  categoryData,
  refreshCategoryData,
  typeOptions,
  statusOptions
})

/**
 * 左侧分类切换后，驱动右侧列表同步过滤。
 */
const handleSelectionChange = (target: ExperienceFilterTarget | null) => {
  activeFilterTarget.value = target
}

/**
 * 左侧分类结构变更时，右侧需要刷新分类元数据与表格展示。
 */
const handleRefreshCodeList = () => {
  void experienceCodeListRef.value?.refreshList()
}

/**
 * 右侧新增、编辑或批量移动后，需要强制刷新左侧完整分类数据，确保数量统计与最新归属一致。
 */
const handleRefreshCategoryList = async () => {
  await categoryListRef.value?.refreshList({ refreshFull: true })
}
</script>

<template>
  <div class="page-container experience-code-page h-full">
    <div class="experience-code-page__layout h-full">
      <CategoryList
        ref="categoryListRef"
        @selection-change="handleSelectionChange"
        @refresh-code-list="handleRefreshCodeList"
      />
      <ExperienceCodeList
        ref="experienceCodeListRef"
        :active-target="activeFilterTarget"
        class="experience-code-page__main"
        @refresh-category-list="handleRefreshCategoryList"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.experience-code-page__layout {
  display: flex;
  gap: 16px;
}

.experience-code-page__main {
  flex: 1;
  min-width: 0;
}
</style>
