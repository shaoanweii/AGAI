<template>
  <GroupAnalysis></GroupAnalysis>
</template>

<script setup lang="ts">
import GroupAnalysis from '@/components/Business/Scene/GroupAnalysis/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeUnmount } from 'vue'

defineOptions({
  name: 'GroupAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('GroupAnalysis')

onBeforeUnmount(() => {
  generalScenarioStore.handleClose()
  sceneAnalysisStore.setSceneOriginData({ isDetail: false })
})
</script>

<style lang="scss" scoped>
.group-analysis {
  padding: 20px;
}
</style>
