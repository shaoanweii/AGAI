<script setup lang="ts">
import useMiddlewareStore from '@/stores/modules/middleware'
import { KeywordLibraryTab } from '@/constant'
import RuleLibrary from './ruleLibrary/index.vue'
import AccountLibrary from './accountLibrary/index.vue'

defineOptions({
  name: 'knowledgeCenterKeywordLibrary'
})

const middlewareStore = useMiddlewareStore()
</script>

<template>
  <div class="keyword-library-page flex-col h-full">
    <RuleLibrary v-if="middlewareStore.keywordLibraryType === KeywordLibraryTab.RULE" />
    <AccountLibrary v-else class="keyword-library-page__account h-full" />
  </div>
</template>

<style lang="scss" scoped>
.keyword-library-page {
}
.keyword-library-page__account {
  display: flex;
  flex: 1;
  min-height: 0;
}
</style>
