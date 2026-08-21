<template>
  <NewCarLaunch></NewCarLaunch>
</template>

<script setup lang="ts">
import NewCarLaunch from '@/components/Business/Scene/NewCarLaunch/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeUnmount } from 'vue'

defineOptions({
  name: 'NewCarLaunch'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('NewCarLaunch')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.new-car-launch {
  padding: 20px;
}
</style>
