<script setup lang="ts">
/**
 * 带有icon的排序数字，根据排序不同，展示不同icon
 */
defineOptions({
  name: 'HSortNum'
})

const { rank = 0, flag = 4 } = defineProps<{
  rank: number
  flag?: number
}>()
</script>

<template>
  <div class="SortNum ps-relative flex-center" :class="{ hot: rank < flag }">
    <div class="position-center">{{ rank }}</div>
    <SvgIcon v-if="rank < flag" name="hot_rank" width="20px" height="20px"></SvgIcon>
    <SvgIcon v-else name="o_rank" width="20px" height="20px"></SvgIcon>
  </div>
</template>

<style lang="scss" scoped>
.SortNum {
  width: 20px;
  height: 20px;
  font-weight: bold;
  font-size: 14px;
  color: #5c7092;
  &.hot {
    color: #e5484d;
  }
}
</style>
