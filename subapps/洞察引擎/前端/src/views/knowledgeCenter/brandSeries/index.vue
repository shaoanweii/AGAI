<script setup lang="ts">
import useMiddlewareStore from '@/stores/modules/middleware'
import Series from './Series/index.vue'
import Brand from './Brand/index.vue'
import Automaker from './Automaker/index.vue'
import { BrandSeriesTab } from '@/constant'

defineOptions({
  name: 'BrandSeries'
})

const middlewareStore = useMiddlewareStore()
</script>

<template>
  <div>
    <Series v-if="middlewareStore.brandSeriesTab === BrandSeriesTab.SERIES"></Series>
    <Brand v-if="middlewareStore.brandSeriesTab === BrandSeriesTab.BRAND"></Brand>
    <Automaker v-if="middlewareStore.brandSeriesTab === BrandSeriesTab.AUTOMAKER"></Automaker>
  </div>
</template>

<style lang="scss" scoped></style>
