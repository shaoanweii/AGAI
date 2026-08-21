<script setup lang="ts">
import FtCascader from '@/components/FtCascader.vue'

const props = withDefaults(
  defineProps<{
    testid?: string
    width?: string
    placeholder?: string
    controller?: string
    maxTagCount?: number
    multiple?: boolean
    formatLabel?: (options: any) => string
    optionData: Record<any, any> | undefined
  }>(),
  {
    maxTagCount: 1,
    width: '350px',
    placeholder: '全部',
    multiple: false
  }
)
const { testid, maxTagCount, width, optionData } = toRefs(props)

const channel = defineModel<any>()
</script>

<template>
  <FtCascader
    :data-testid="testid"
    v-model="channel"
    :options="optionData"
    :props="{ value: 'code', label: 'name', children: 'child' }"
    clearable
    :max-collapse-tags="maxTagCount"
    :style="{ width: width }"
    :placeholder="placeholder"
    :multiple="multiple"
    :format-label="formatLabel"
  ></FtCascader>
</template>

<style scoped lang="scss"></style>
