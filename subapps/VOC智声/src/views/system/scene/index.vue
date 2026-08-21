<script setup lang="ts">
import scene from './scene/index.vue'
// import report from '../report/index.vue'
// import useMiddlewareStore from '@/store/modules/middleware'
// import { SceneManagementTab } from '@/constants'

defineOptions({
  name: 'sceneManagement'
})

// const middlewareStore = useMiddlewareStore()
</script>

<template>
  <!-- 场景管理 -->
  <scene></scene>
  <!-- 报告管理 -->
  <!-- <report v-if="middlewareStore.sceneManagementType === SceneManagementTab.REPORT"></report> -->
</template>

<style lang="scss" scoped></style>
