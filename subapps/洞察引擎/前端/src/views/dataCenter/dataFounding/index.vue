<template>
  <div class="main-table">
    <el-tabs v-model="activeKey" class="main-tabs">
      <el-tab-pane v-if="getHasPermission('3')" name="3" label="品牌车系库">
        <CarSeries v-if="activeKey === '3'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts" setup>
import CarSeries from './carSeries/index.vue'
import { useSetTabDataId } from '@/hooks/useSetTabDataId'
import { useTabPermission } from '@/hooks/useTabPermission'

useSetTabDataId(`founding-tabs-100`)

const { activeKey, getHasPermission } = useTabPermission('founding')

const dataSourceRef = ref()
const indexRef = ref()
const clientChange = () => {
  if (activeKey.value === '1') {
    dataSourceRef.value?.query()
  } else if (activeKey.value === '4') {
    indexRef.value?.query()
  }
}
</script>
