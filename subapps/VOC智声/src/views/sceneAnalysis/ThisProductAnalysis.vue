<template>
  <ThisProductAnalysis></ThisProductAnalysis>
</template>

<script setup lang="ts">
import ThisProductAnalysis from '@/components/Business/Scene/ThisProductAnalysis/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeMount, onBeforeUnmount } from 'vue'

defineOptions({
  name: 'ThisProductAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('ThisProductAnalysis')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.this-product-analysis {
  padding: 20px;
}
</style>
