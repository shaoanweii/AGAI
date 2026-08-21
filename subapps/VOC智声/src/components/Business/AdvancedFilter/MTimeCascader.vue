<script setup lang="ts">
import useUserStore from '@/store/modules/user'
import { computed } from 'vue'

defineOptions({
  name: 'MTimeCascader'
})
const modelValue = defineModel<any>()

const { teleported = true } = defineProps<{
  teleported?: boolean
}>()

const options = computed(() => {
  return useUserStore().getMTimeOptions
})
</script>

<template>
  <el-cascader
    v-model="modelValue"
    :options="options"
    :props="{
      value: 'code',
      label: 'name',
      children: 'child',
      checkStrictly: false,
      checkOnClickLeaf: false
    }"
    :teleported="teleported"
  />
</template>

<style lang="scss" scoped></style>
