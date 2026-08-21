<script setup lang="ts">
import { getUserChannelTree } from '@/api/common'
import { cloneDeep } from 'lodash-es'
import { computed, onMounted, ref, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'

defineOptions({
  name: 'DataSourceCascader'
})
const modelValue = defineModel<any>()

const {
  options = [],
  teleported = true,
  condition,
  childKey,
  waitForParent = false // 是否等待父组件加载数据
} = defineProps<{
  options?: any[]
  teleported?: boolean
  condition: any
  childKey: any
  waitForParent?: boolean // 如果为 true，表示父组件会传入 options，即使初始为空也要等待
}>()

const channelOptions = ref<any[]>([])
const isDataReady = ref(false)
const cachedValue = ref<any>(null)

const dataSourceOptions = computed(() => {
  // 有传入options就用options，否则使用全局的渠道数据
  if (options.length > 0) {
    return cloneDeep(options) || []
  } else {
    return channelOptions.value
  }
})

// 获取这个用户的渠道树
const getChannelTreeByUser = async () => {
  try {
    const res = await getUserChannelTree()
    return res.result
  } catch {
    return []
  }
}

const route = useRoute()

const init = async () => {
  // 如果父组件传入了 options 且已经有数据，直接使用
  if (options.length > 0) {
    isDataReady.value = true
    // 数据加载完成后,如果有缓存的值,重新设置
    if (cachedValue.value !== null) {
      await nextTick()
      modelValue.value = cloneDeep(cachedValue.value)
    }
    return
  }

  // 如果 waitForParent 为 true，说明父组件会传入 options，等待父组件加载数据
  // 否则自己加载数据
  if (waitForParent) {
    // 等待父组件加载数据（通过 watch 处理）
    return
  }

  // 父组件没有传入 options，自己获取数据
  // 角色管理页面不获取渠道树, 使用数据源中的渠道数据
  if (route.name !== 'RoleManagement') {
    channelOptions.value = await getChannelTreeByUser()
  }
  isDataReady.value = true

  // 数据加载完成后,如果有缓存的值,重新设置
  if (cachedValue.value !== null) {
    await nextTick()
    modelValue.value = cloneDeep(cachedValue.value)
  }
}

// 监听 options 变化，如果父组件传入了 options 且数据已加载完成
watch(
  () => options,
  newVal => {
    // 如果 waitForParent 为 true 且 options 从空变为有值，说明父组件数据加载完成
    if (waitForParent && newVal.length > 0 && !isDataReady.value) {
      isDataReady.value = true
      // 数据加载完成后,如果有缓存的值,重新设置
      if (cachedValue.value !== null) {
        nextTick(() => {
          modelValue.value = cloneDeep(cachedValue.value)
        })
      }
    }
  },
  { immediate: true, deep: true }
)

// 监听modelValue变化,如果数据未就绪则缓存
watch(
  () => modelValue.value,
  newVal => {
    if (!isDataReady.value && newVal) {
      cachedValue.value = cloneDeep(newVal)
    }
  },
  { immediate: true }
)

onMounted(() => {
  init()
})

// 暴露数据源选项给父组件使用
defineExpose({
  channelOptions,
  dataSourceOptions
})
</script>

<template>
  <el-cascader
    v-model="modelValue"
    :options="dataSourceOptions"
    collapse-tags
    :max-collapse-tags="1"
    :show-all-levels="false"
    filterable
    :key="childKey"
    :props="{
      value: 'code',
      label: 'name',
      children: 'child',
      multiple: condition.multiSelect ?? true,
      emitPath: false,
      checkStrictly: false
    }"
    :teleported="teleported"
  />
</template>

<style lang="scss" scoped></style>
