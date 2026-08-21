<script setup lang="ts">
import TextCorpus from './textCorpus/index.vue'

defineOptions({
  name: 'knowledgeCenterCorpusMapping'
})
</script>

<template>
  <div class="corpus-mapping-page">
    <TextCorpus class="corpus-mapping-page__content" />
  </div>
</template>

<style lang="scss" scoped>
.corpus-mapping-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.corpus-mapping-page__content {
  flex: 1;
  min-height: 0;
}
</style>
