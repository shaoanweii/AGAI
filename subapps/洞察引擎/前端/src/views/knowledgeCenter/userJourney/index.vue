<script setup lang="ts">
import ExperienceCodePage from '../experienceCode/index.vue'

defineOptions({
  name: 'KnowledgeCenterUserJourney'
})
</script>

<template>
  <!-- 复用稳定的双栏管理组件；数据源由当前用户旅程路由独立解析。 -->
  <ExperienceCodePage />
</template>
