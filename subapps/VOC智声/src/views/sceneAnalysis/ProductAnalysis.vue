<template>
  <ProductAnalysis></ProductAnalysis>
</template>

<script setup lang="ts">
import ProductAnalysis from '@/components/Business/Scene/ProductAnalysis/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeMount, onBeforeUnmount } from 'vue'

defineOptions({
  name: 'ProductAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('ProductAnalysis')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.product-analysis {
  padding: 20px;
}
</style>
