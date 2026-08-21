<template>
  <HotDetailEvents></HotDetailEvents>
</template>

<script setup lang="ts">
import HotDetailEvents from '@/components/Business/Scene/HotDetailEvents/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeUnmount } from 'vue'

defineOptions({
  name: 'HotDetailEvents'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('HotDetailEvents')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.hot-events {
  padding: 20px;
}
</style>
