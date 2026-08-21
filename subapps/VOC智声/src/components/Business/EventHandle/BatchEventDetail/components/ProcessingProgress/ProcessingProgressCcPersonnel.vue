<script setup lang="ts">
import FCollapseSection from '@/components/UI/FCollapseSection/index.vue'
import type { BatchEventProcessingCcPersonnelItem } from '../../types'

defineOptions({
  name: 'ProcessingProgressCcPersonnel'
})

defineProps<{
  items: BatchEventProcessingCcPersonnelItem[]
}>()

defineSlots<{
  titleExtra?: () => any
}>()
</script>

<template>
  <FCollapseSection title="抄送人员" class="processing-progress-cc-personnel">
    <template v-if="$slots.titleExtra" #titleExtra>
      <slot name="titleExtra"></slot>
    </template>

    <el-table
      :data="items"
      row-key="employeeId"
      table-layout="fixed"
      empty-text="暂无抄送人员"
      class="processing-progress-cc-personnel__table"
    >
      <el-table-column prop="secondaryDepartment" label="二级部门" min-width="220" />
      <el-table-column prop="tertiaryDepartment" label="三级部门" min-width="220" />
      <el-table-column prop="userName" label="姓名" min-width="120" />
      <el-table-column prop="employeeId" label="工号" min-width="120" />
    </el-table>
  </FCollapseSection>
</template>

<style scoped lang="scss">
.processing-progress-cc-personnel__table {
  width: 100%;
}

.processing-progress-cc-personnel__table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.processing-progress-cc-personnel__table :deep(.el-table__header-wrapper th.el-table__cell) {
  height: 64px;
  padding: 0 20px;
  background: #f5f7fa;
  font-weight: 500;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
}

.processing-progress-cc-personnel__table :deep(.el-table__body-wrapper td.el-table__cell) {
  height: 64px;
  padding: 0 20px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
}
</style>
