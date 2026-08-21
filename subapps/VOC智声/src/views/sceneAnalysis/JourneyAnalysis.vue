<template>
  <JourneyAnalysis></JourneyAnalysis>
</template>

<script setup lang="ts">
import JourneyAnalysis from '@/components/Business/Scene/JourneyAnalysis/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeMount, onBeforeUnmount } from 'vue'

defineOptions({
  name: 'JourneyAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('JourneyAnalysis')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.journey-analysis {
  padding: 20px;
}
</style>
