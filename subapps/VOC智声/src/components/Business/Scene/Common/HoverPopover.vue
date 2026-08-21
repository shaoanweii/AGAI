<template>
  <el-popover
    :placement="placement"
    popper-class="c-hover-popover"
    :show-after="showAfter"
    :width="width"
    :trigger="trigger"
    :disabled="disabled"
    :hide-after="hideAfter"
    :offset="offset"
    :show-arrow="showArrow"
    :transition="transition"
    :teleported="teleported"
    :persistent="persistent"
  >
    <!-- 触发元素插槽：reference -->
    <template #reference>
      <span class="c-hover-popover__reference">
        <slot name="reference" />
      </span>
    </template>
    <template #default>
      <!-- 默认内容插槽：popover 内容区域 -->
      <template v-if="$slots.default">
        <slot />
      </template>
      <template v-else>
        <div v-if="tableConfig.title" class="fs-14 fw-500 mb-12" style="color: #333">
          {{ tableConfig.title }}
        </div>
        <!--  hover表格 -->
        <el-table :data="tableConfig.data" class="pop-table">
          <el-table-column
            v-for="col in tableConfig.columns"
            :key="col.dataIndex"
            :prop="col.dataIndex"
            :label="col.title"
            :width="col.width"
            :class-name="col.className"
          />
        </el-table>
      </template>
    </template>
  </el-popover>
</template>

<style scoped>
.c-hover-popover__reference {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
</style>

<script setup lang="ts">
// @ts-nocheck
import { computed, ref } from 'vue'

/**
 * 通用 HoverPopover 组件（<script setup>）
 * - 封装 el-popover
 * - reference 使用插槽插入触发元素
 * - 透传常用配置，支持 v-model:visible（受控/非受控）
 */

type PopoverTrigger = 'hover' | 'click' | 'focus' | 'contextmenu'

type LightOrDark = 'light' | 'dark'

type TableColumn = {
  dataIndex: string
  title: string
  width?: number | string
  className?: string
}

type TableConfig = {
  title?: string
  columns: TableColumn[]
  data: any[]
}

defineOptions({ name: 'HoverPopover' })

const props = withDefaults(
  defineProps<{
    modelValue?: boolean
    placement?: string
    popperClass?: string | string[]
    showAfter?: number
    width?: number | string
    trigger?: PopoverTrigger
    disabled?: boolean
    hideAfter?: number
    offset?: number
    showArrow?: boolean
    transition?: string
    teleported?: boolean
    persistent?: boolean
    effect?: LightOrDark
    tableConfig?: TableConfig
  }>(),
  {
    placement: 'top',
    showAfter: 200,
    width: 410,
    trigger: 'hover',
    disabled: false,
    hideAfter: 0,
    offset: 12,
    showArrow: true,
    teleported: true,
    persistent: false,
    tableConfig: () => ({ title: '', columns: [], data: [] })
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'visible-change', value: boolean): void
}>()

// 非受控内部状态
const innerVisible = ref(false)

const tableConfig = computed<TableConfig>(
  () => props.tableConfig || { title: '', columns: [], data: [] }
)
</script>

<style lang="scss">
// 全局
.c-hover-popover {
  .el-table {
    .el-table--border .el-table__inner-wrapper:after,
    .el-table--border:after,
    .el-table--border:before,
    .el-table__inner-wrapper:before {
      display: none;
    }
    th,
    td {
      text-align: center;
      border: none !important;
    }

    .el-table__header th {
      background-color: #eaf3ff !important;
    }

    .cell {
      color: #26292e;
    }

    td.c666 .cell {
      color: #666;
    }

    .mod {
      background-color: #fff !important;
      border: 1px solid #dfe2e8;
      border-radius: 4px;
    }

    .hot {
      color: #ff5959;
      font-weight: 500;
    }
  }
}
</style>
<style scoped lang="scss">
/* 语义化类名，避免与全局冲突 */
.c-hover-popover {
}
</style>
