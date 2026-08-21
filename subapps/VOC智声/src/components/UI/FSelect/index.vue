<script setup lang="ts">
defineOptions({
  name: 'FSelect',
  inheritAttrs: false
})
const { options = [], fields = { label: 'itemText', value: 'itemValue' } } = defineProps<{
  options: any[] | undefined | null
  fields?: {
    label: string
    value: string
  }
}>()
const value = defineModel<any>()
</script>

<template>
  <el-select
    v-model="value"
    clearable
    filterable
    :teleported="false"
    data-page-export-expand
    v-bind="$attrs"
  >
    <el-option
      v-for="(item, index) of options"
      :key="index"
      :value="item[fields.value]"
      :label="item[fields.label]"
    ></el-option>
  </el-select>
</template>

<style lang="scss" scoped></style>
