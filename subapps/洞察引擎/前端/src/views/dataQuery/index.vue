<script setup lang="ts">
import useMiddlewareStore from '@/stores/modules/middleware'
import { DataType } from '@/constant'
import RawData from './components/RawData/index.vue'
import ResultData from './components/ResultData/index.vue'
import useConditions from '@/hooks/useConditions'
import { getGlobalChannelTreeByClientId } from '@/api/main'

defineOptions({
  name: 'dataQuery'
})

const channelOptions = ref<any[]>([])

/**
 * 获取渠道级联树；接口异常结构不得继续传入 Element Plus，否则组件内部会在 map 时崩溃。
 */
const getChannelOptionsApi = async () => {
  try {
    const res = await getGlobalChannelTreeByClientId('', 'insCqCaDataSource')
    channelOptions.value = Array.isArray(res.result) ? res.result : []
  } catch {
    channelOptions.value = []
  }
}

const init = () => {
  getChannelOptionsApi()
}

init()

provide('channelOptions', channelOptions)

const middlewareStore = useMiddlewareStore()

const { conditions } = useConditions({ url: '/insights/insCqCaDataSource/conditions' })

provide('conditions', conditions)
</script>

<template>
  <div class="data-query">
    <RawData v-if="middlewareStore.dataType === DataType.RAW" />
    <RawData v-else-if="middlewareStore.dataType === DataType.CLEAN" mode="clean" />
    <ResultData v-else />
  </div>
</template>

<style lang="scss" scoped>
.data-query {
}
</style>
