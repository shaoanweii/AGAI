<script setup lang="ts">
import { ref } from 'vue'
import CategoryList from './components/CategoryList.vue'
import RuleList from './components/RuleList.vue'

defineOptions({
  name: 'KeywordRuleLibrary'
})

const keywordListRef = ref()

const handleCategoryChange = (item: any) => {
  keywordListRef.value?.leftChange(item)
}
</script>

<template>
  <div class="keyword-rule-library flex h-full gap-16">
    <div class="keyword-rule-library__card keyword-rule-library__category pr-24">
      <CategoryList @category-item-click="handleCategoryChange" />
    </div>
    <div class="keyword-rule-library__card keyword-rule-library__list flex-1">
      <RuleList ref="keywordListRef" class="h-full" />
    </div>
  </div>
</template>

<style scoped lang="scss">
$card-bg: #ffffff;
$card-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);

.keyword-rule-library {
  display: flex;
  gap: 16px;
}

.keyword-rule-library__card {
  padding: 16px 24px;
  background: $card-bg;
  box-shadow: $card-shadow;
  border-radius: 8px;
}

.keyword-rule-library__category {
  width: 352px;
}
</style>
