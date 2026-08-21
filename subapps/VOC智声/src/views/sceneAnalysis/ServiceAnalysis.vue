<template>
  <ServiceAnalysis></ServiceAnalysis>
</template>

<script setup lang="ts">
import ServiceAnalysis from '@/components/Business/Scene/ServiceAnalysis/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeMount, onBeforeUnmount } from 'vue'

defineOptions({
  name: 'ServiceAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('ServiceAnalysis')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.service-analysis {
  padding: 20px;
}
</style>
