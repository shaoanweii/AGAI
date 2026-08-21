<template>
  <div class="main-table">
    <el-tabs v-model="activeKey" class="main-tabs">
      <el-tab-pane v-if="getHasPermission('local')" name="local" label="本地上传">
        <DataSoutce />
      </el-tab-pane>
      <el-tab-pane v-if="getHasPermission('system')" name="system" label="系统集成">
        <DataSoutceBySys />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import DataSoutce from './components/dataSource/index.vue'
import DataSoutceBySys from './components/dataSourceBySys/index.vue'
import useConditions from '@/hooks/useConditions'
import { useSetTabDataId } from '@/hooks/useSetTabDataId'
import { useTabPermission } from '@/hooks/useTabPermission'

const { conditions } = useConditions({
  url: '/insights/insDataSource/conditions'
})

const { activeKey, getHasPermission } = useTabPermission('insDataSource')

useSetTabDataId('insDataSource-100')

provide('conditions', conditions)
</script>

<style lang="scss" scoped>
.status-wrapper {
  display: flex;
  align-items: center;

  .status-circle {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 8px;
  }

  .success-bg {
    background-color: var(--color-success);
  }

  .forbidden-bg {
    background-color: #c9cdd4;
  }
}
</style>
