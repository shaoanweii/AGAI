<script setup lang="ts">
import { onMounted, provide, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { Conditions, ConditionsDetailItem } from '@/types'
import { getCarSceneConditions } from '@/api/carUsageScenarios'
import { SceneList } from './components'
import { carUsageScenarioPageContextKey } from './context'

defineOptions({
  name: 'KnowledgeCenterCarUsageScenarios'
})

const conditionLoading = ref(false)
const conditionMap = ref<Record<string, ConditionsDetailItem[]>>({})

/**
 * 页面级统一拉取用车场景字典，后续弹框和列表共用一份数据，避免重复请求。
 */
const refreshConditions = async () => {
  conditionLoading.value = true
  try {
    const response = await getCarSceneConditions()
    const nextConditions: Record<string, ConditionsDetailItem[]> = {}

    ;(response.result || []).forEach((item: Conditions) => {
      nextConditions[item.key] = item.details || []
    })

    conditionMap.value = nextConditions
  } catch (error: any) {
    ElMessage.error(error?.message || '获取用车场景字典失败，请稍后重试')
  } finally {
    conditionLoading.value = false
  }
}

provide(carUsageScenarioPageContextKey, {
  conditionLoading,
  conditionMap,
  refreshConditions
})

onMounted(() => {
  void refreshConditions()
})
</script>

<template>
  <div class="car-usage-scenarios-page h-full">
    <!-- 用车场景不再设置分类层级，页面直接展示全量场景列表。 -->
    <SceneList />
  </div>
</template>

<style scoped lang="scss">
.car-usage-scenarios-page {
  height: 100%;
}
</style>
