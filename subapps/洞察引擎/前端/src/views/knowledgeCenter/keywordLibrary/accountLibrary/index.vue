<script setup lang="ts">
import { ref } from 'vue'
import CategoryList from './components/CategoryList.vue'
import RuleList from './components/RuleList.vue'

defineOptions({
  name: 'AccountKeywordLibrary'
})

const keywordListRef = ref()

const handleCategoryChange = (item: any) => {
  keywordListRef.value?.leftChange(item)
}
</script>

<template>
  <div class="account-library flex h-full gap-16">
    <div class="account-library__card account-library__category pr-24">
      <CategoryList @category-item-click="handleCategoryChange" />
    </div>
    <div class="account-library__card account-library__list flex-1">
      <RuleList ref="keywordListRef" class="h-full" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
$card-bg: #ffffff;
$card-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);

.account-library {
  display: flex;
  gap: 16px;
}

.account-library__card {
  padding: 16px 24px;
  background: $card-bg;
  box-shadow: $card-shadow;
  border-radius: 8px;
}

.account-library__category {
  width: 352px;
}
</style>
