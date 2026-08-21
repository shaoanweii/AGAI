<script setup lang="ts">
import useUserStore from '@/store/modules/user'
import { computed } from 'vue'

defineOptions({
  name: 'BrandCascader'
})
const modelValue = defineModel<any>()

const {
  options = [],
  teleported = true,
  condition = {},
  disabled = false
} = defineProps<{
  options?: any[]
  teleported?: boolean
  condition?: {
    multiSelect?: boolean
  }
  disabled?: boolean
}>()

const isMultiple = computed(() => condition.multiSelect === true)

const brandOptions = computed(() => {
  // 有传入options就用options，否则使用全局品牌数据. 只显示品牌,不显示车系
  const userStore = useUserStore()
  return options.length > 0
    ? options
    : userStore.getBrandService?.map(item => ({
        ...item,
        children: []
      }))
})
</script>

<template>
  <el-cascader
    v-model="modelValue"
    :options="brandOptions"
    :clearable="isMultiple"
    :collapse-tags="isMultiple"
    :max-collapse-tags="1"
    :show-all-levels="false"
    :filterable="isMultiple"
    :disabled="disabled"
    :props="{
      value: 'key',
      label: 'value',
      children: 'children',
      multiple: isMultiple,
      emitPath: !isMultiple,
      checkStrictly: true,
      checkOnClickLeaf: false
    }"
    :teleported="teleported"
  />
</template>

<style lang="scss" scoped></style>
