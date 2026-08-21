<script setup lang="ts">
import Leader from './leader/index.vue'
import { useQueryStore } from '@/store'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { onBeforeUnmount, onMounted, watch, ref } from 'vue'
import { getTimeDimensionByCode } from '@/utils/date'

defineOptions({
  name: 'leaderOverview'
})
const queryStore = useQueryStore()
const route = useRoute()

// 缓存当前页面的时间参数
const cacheTimeParams = () => {
  // 获取已有的缓存，保留其他字段
  const existingCache = queryStore.getUniversaFilterCacheSearchParams() || {}
  const timeParams: Record<string, any> = {
    ...existingCache,
    startDate: queryStore.currentQueryParams.startDate,
    endDate: queryStore.currentQueryParams.endDate,
    globalShortcutValue: queryStore.globalShortcutValue
  }

  // 根据 globalShortcutValue 设置 dateRange
  // 先清除旧的 dateRange，确保不会保留错误的值
  delete timeParams.dateRange

  if (queryStore.globalShortcutValue === '自定义') {
    // 如果是自定义时间，设置 dateRange 为 'custom'
    timeParams.dateRange = 'custom'
  } else if (queryStore.globalShortcutValue) {
    // 如果是快捷选项，根据 globalShortcutValue 找到对应的 code 并设置 dateRange
    const dimensionItem = getTimeDimensionByCode(queryStore.globalShortcutValue)
    if (dimensionItem && dimensionItem.code !== undefined) {
      timeParams.dateRange = dimensionItem.code.toString()
    }
  }

  queryStore.setUniversaFilterCacheSearchParams(timeParams)
}

// 恢复缓存的时间参数
const restoreCachedTimeParams = () => {
  const cachedParams = queryStore.getUniversaFilterCacheSearchParams()
  if (cachedParams && cachedParams.startDate && cachedParams.endDate) {
    queryStore.updateQueryParams({
      startDate: cachedParams.startDate,
      endDate: cachedParams.endDate
    })
    if (cachedParams.globalShortcutValue) {
      queryStore.globalShortcutValue = cachedParams.globalShortcutValue
    }
  } else {
    // 如果没有缓存，使用角色配置的默认值
    queryStore.setPageDefaultFilter(route.name as string)
  }
}

// 页面挂载时恢复缓存的时间参数
const isInitialized = ref(false)
onMounted(() => {
  restoreCachedTimeParams()

  // 延迟设置初始化标志，避免初始化时的 watch 触发
  setTimeout(() => {
    isInitialized.value = true
  }, 100)

  // 监听时间参数变化，实时更新缓存
  // 使用 watchEffect 监听 reactive 对象的变化
  watch(
    () => [
      queryStore.currentQueryParams.startDate,
      queryStore.currentQueryParams.endDate,
      queryStore.globalShortcutValue
    ],
    () => {
      // 只在初始化完成后才更新缓存，避免覆盖恢复的缓存
      if (isInitialized.value) {
        cacheTimeParams()
      }
    },
    { immediate: false }
  )
})

// 路由离开时缓存时间参数并清除数据
onBeforeRouteLeave(() => {
  cacheTimeParams()
  queryStore.updateQueryParams({
    intention: undefined,
    topic: undefined,
    brandCode: undefined,
    tempCode: undefined,
    tag2Code: undefined
  })
})
// 组件卸载时缓存时间参数并清除数据
onBeforeUnmount(() => {
  cacheTimeParams()
  queryStore.updateQueryParams({
    intention: undefined,
    topic: undefined,
    brandCode: undefined,
    tempCode: undefined,
    tag2Code: undefined
  })
})
</script>

<template>
  <Leader></Leader>
</template>
