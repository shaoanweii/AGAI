<script setup lang="ts">
import { onMounted, ref } from 'vue'
import DataSquareLeft from './component/DataSquareLeft.vue'
import DataSquareRight from './component/DataSquareRight.vue'
import type { CategoryFilterPayload } from './component/DataSquareLeft.vue'
import { dataSquareActions } from './store'

defineOptions({
  name: 'DataSquare'
})

interface CategoryRestorePayload {
  selectedParentId: string
  selectedCategoryId: string
}

const selectedCategoryId = ref<string | null>(null)
const categoryChangeVersion = ref(0)
const restoreSelection = ref<CategoryRestorePayload | null>(null)
const restoreSelectionVersion = ref(0)
const silentRefreshVersion = ref(0)

/**
 * 左侧分类变化后同步当前分类，并通知右侧刷新报告列表。
 */
const handleCategoryChange = (payload: CategoryFilterPayload) => {
  selectedCategoryId.value = payload.categoryId
  categoryChangeVersion.value += 1
}

/**
 * 右侧报告保存成功后刷新左侧分类树，并恢复到保存后的分类。
 */
const handleRestoreSelection = (payload: CategoryRestorePayload) => {
  restoreSelection.value = payload
  restoreSelectionVersion.value += 1
}

/**
 * 右侧报告变更成功后静默刷新左侧分类树，保持当前选中态不跳变。
 */
const handleReportChanged = () => {
  silentRefreshVersion.value += 1
}

onMounted(() => {
  void dataSquareActions.preloadReportDialogOptions()
})
</script>

<template>
  <div class="page-container flex-col h-full data-square">
    <el-card class="table-card data-square-card h-full flex-col" shadow="never">
      <div class="data-square-layout">
        <DataSquareLeft
          :restore-selection="restoreSelection"
          :restore-selection-version="restoreSelectionVersion"
          :silent-refresh-version="silentRefreshVersion"
          @change="handleCategoryChange"
        />
        <DataSquareRight
          :category-id="selectedCategoryId"
          :category-change-version="categoryChangeVersion"
          @restore-selection="handleRestoreSelection"
          @report-changed="handleReportChanged"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.data-square-card {
  flex: 1;
  display: flex;
  flex-direction: column;

  :deep(.el-card__body) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-table .el-table__cell) {
    height: 55px;
    padding: 0 !important;
  }

  :deep(.el-table--fit .el-table__inner-wrapper:before) {
    width: 0 !important;
  }

  :deep(.el-table__header .el-table__cell) {
    color: #1d2129;
    font-weight: 600;
  }

  :deep(.el-table__body-wrapper .el-table__cell) {
    color: #1d2129;
    font-weight: 400;
  }
}

.data-square-layout {
  flex: 1;
  min-height: 0;
  display: flex;
}

@media (max-width: 1200px) {
  .data-square-layout {
    overflow-x: auto;
  }
}
</style>
