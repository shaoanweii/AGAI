<script setup lang="ts">
import TextNewWord from './textNewWord/index.vue'

defineOptions({
  name: 'DataCenterDiscovery'
})
</script>

<template>
  <div class="discovery-page flex-col h-full">
    <TextNewWord class="discovery-page__text h-full" />
  </div>
</template>

<style lang="scss" scoped>
.discovery-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.discovery-page__text {
  flex: 1;
  min-height: 0;
  width: 100%;
  display: block;
}
</style>
