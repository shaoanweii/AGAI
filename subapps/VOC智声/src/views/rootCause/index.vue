<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useQueryStore } from '@/store/modules/query'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import useGeneralDrillDownStore, { ContentType } from '@/store/modules/generalDrillDown'
import DrillDownPage from '@components/Business/DrillDownPage/index.vue'
import UniversaFilter from '@/components/Business/UniversaFilter/index.vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'

defineOptions({
  name: 'RootCause'
})

type VoiceManagementParams = {
  brandCarCodes: any[]
  tagCodes: any[]
  viewpoint?: string
  highQuality?: unknown
}

const normalizeVoiceManagementParams = (input: unknown): VoiceManagementParams => {
  const obj = input && typeof input === 'object' ? (input as any) : {}
  return {
    brandCarCodes: Array.isArray(obj.brandCarCodes) ? obj.brandCarCodes : [],
    tagCodes: Array.isArray(obj.tagCodes) ? obj.tagCodes : [],
    viewpoint: typeof obj.viewpoint === 'string' ? obj.viewpoint : undefined,
    highQuality:
      obj.highQuality === undefined || obj.highQuality === null || obj.highQuality === ''
        ? undefined
        : obj.highQuality
  }
}

const queryStore = useQueryStore()
const sceneAnalysisStore = useSceneAnalysisStore()
const ddStore = useGeneralDrillDownStore()
const route = useRoute()

const isDrillDownReady = ref(false)
const shouldClearReportDetailContext = ref(false)

const isReportDetailPage = computed(
  () =>
    sceneAnalysisStore.sceneOriginData.isDetail &&
    (route.query.isBack === '1' || Boolean(route.query.reportJudgeId))
)

/**
 * 重置客户原声高质量筛选，避免 rootCause 页签中的私有条件带入其他页面。
 */
const resetHighQualityFilter = () => {
  queryStore.voiceManagementParams.highQuality = undefined
}

const buildRootCauseInitParams = () => {
  const voice = normalizeVoiceManagementParams(queryStore.voiceManagementParams)
  const [brandCode, carSeriesCode] = voice.brandCarCodes
  const [tag1Code, tag2Code] = voice.tagCodes

  return {
    brandCode: brandCode || undefined,
    carSeriesCode: carSeriesCode || undefined,
    tag1Code: tag1Code || undefined,
    tag2Code: tag2Code || undefined,
    topic: voice.viewpoint || undefined,
    highQuality: voice.highQuality
  }
}

onMounted(() => {
  ddStore.openDD(buildRootCauseInitParams())
  isDrillDownReady.value = true
})

const handleSearch = (_formData: any) => {
  console.log('handleSearch', _formData)
  ddStore.openDD(_formData)
  isDrillDownReady.value = true
}

onBeforeRouteLeave(() => {
  resetHighQualityFilter()
  shouldClearReportDetailContext.value = isReportDetailPage.value
})

onUnmounted(() => {
  resetHighQualityFilter()
  if (shouldClearReportDetailContext.value) {
    // 报告条件使用独立上下文，离开时无需清理普通页面原有缓存。
    void sceneAnalysisStore.setSceneOriginData({ isDetail: false })
  }
  ddStore.componentName = ContentType.MAIN
  ddStore.headerMode = 'back'
})
</script>

<template>
  <div class="root-cause-analysis">
    <UniversaFilter :routeName="`${route.name as string}`" @search="handleSearch"></UniversaFilter>
    <div class="content-wrapper">
      <DrillDownPage v-if="isDrillDownReady" @tab-change="resetHighQualityFilter" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.root-cause-analysis {
  // width: 100%;
  // height: 100%;
  // margin-top: -24px;
  // margin-bottom: 24px;

  width: calc(100% - 5px);
  height: calc(100% + 24px);
  margin-top: -24px;
  box-sizing: border-box;
}

.content-wrapper {
  width: 100%;
  height: calc(100% - 24px);
  padding: 24px 0;
}

.root-cause-analysis__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.root-cause-analysis__placeholder {
  color: #717680;
  font-size: 14px;
  line-height: 22px;
}
</style>
