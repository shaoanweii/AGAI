<template>
  <CompetitorAnalysis></CompetitorAnalysis>
</template>

<script setup lang="ts">
import CompetitorAnalysis from '@/components/Business/Scene/CompetitorAnalysis/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeUnmount } from 'vue'
defineOptions({
  name: 'CompetitorAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('CompetitorAnalysis')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.competitor-analysis {
  padding: 20px;
}
</style>
