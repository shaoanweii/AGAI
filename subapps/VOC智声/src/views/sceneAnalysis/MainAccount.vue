<template>
  <MainAccount></MainAccount>
</template>

<script setup lang="ts">
import MainAccount from '@/components/Business/Scene/MainAccount/index.vue'
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeMount, onBeforeUnmount } from 'vue'

defineOptions({
  name: 'MainAccount'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()

generalScenarioStore.handleOpen('MainAccount')

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
