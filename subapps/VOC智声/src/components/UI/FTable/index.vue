<!--
  FTable 组件 - 基于 Element Plus Table 的封装组件

  功能特性：
  - 支持自定义列配置和渲染
  - 支持列级别的自定义样式配置
  - 支持排序、固定列、溢出提示等功能
  - 基于 Element Plus 官方 API 实现样式自定义

  @author VOC Team
  @version 1.0.0
-->
<script setup lang="tsx">
import { computed, onMounted, ref } from 'vue'
import type { TableColumn } from './types'
import { fmtNum, fmtPer, fmtFix, fmtHot } from '@/utils'

/**
 * 组件 Props 接口定义
 */
interface Props {
  /** 列配置数组 */
  columns?: TableColumn[]
  /** 表格数据 */
  data?: any[]
  /** 加载状态 */
  loading?: boolean
  /** 表格高度 */
  height?: string | number
  /** 分页配置 */
  pagination?: boolean | object
  /** 边框配置 */
  bordered?: boolean | object
  /** 表格尺寸 */
  size?: '' | 'large' | 'small' | 'default'
  /** 是否有悬浮弹出 */
  hasHoverPop?: boolean
  /** 悬浮表格显示行数 */
  hoverTableRows?: 1 | 2
}

/**
 * Props 默认值配置
 */
const props = withDefaults(defineProps<Props>(), {
  columns: () => [],
  data: () => [],
  fmtHoverData: () => [],
  loading: false,
  height: 330,
  pagination: false,
  bordered: false,
  size: 'large',
  hasHoverPop: false,
  hoverTableRows: 2
})

/**
 * 组件事件定义
 */
const emit = defineEmits<{
  /** 排序变化事件 */
  'sort-change': [{ column: any; prop: string; order: string | null }]
  /** 行点击事件 */
  'row-click': [row: any]
}>()

/**
 * 组件选项配置
 */
defineOptions({
  name: 'FTable'
})

// Element Plus Table 引用
const elTableRef = ref<any>(null)

// 暴露方法给父组件
defineExpose({
  clearSort: () => elTableRef.value?.clearSort()
})

/**
 * 计算表格高度
 * 支持数字和字符串类型的高度设置
 */
const tableHeight = computed(() => {
  if (typeof props.height === 'number') {
    return props.height
  }
  return props.height
})

/**
 * 处理列配置
 * 对传入的列配置进行预处理，确保兼容性
 */
const processedColumns = computed(() => {
  return props.columns.map(column => {
    // 如果有 render 函数，直接使用原配置
    if (column.render) {
      return column
    }

    // 如果 title 是函数，处理函数类型的标题
    if (typeof column.title === 'function') {
      return {
        ...column,
        title: column.title
      }
    }

    // 返回原始列配置
    return column
  })
})

/**
 * 自定义样式配置处理函数
 * 基于 Element Plus 官方 API 实现列级别的样式自定义
 */

/**
 * 获取表头单元格自定义样式
 * @param {Object} params - Element Plus 传递的参数对象
 * @param {any} params.row - 行数据
 * @param {any} params.column - 列配置
 * @param {number} params.rowIndex - 行索引
 * @param {number} params.columnIndex - 列索引
 * @returns {Object} 样式对象
 */
const getHeaderCellStyle = ({ row, column, rowIndex, columnIndex }: any) => {
  const tableColumn = props.columns[columnIndex]
  if (tableColumn?.headerCellStyle) {
    // 如果是函数，调用函数获取动态样式
    if (typeof tableColumn.headerCellStyle === 'function') {
      return tableColumn.headerCellStyle({ row, column, rowIndex, columnIndex })
    }
    // 如果是对象，直接返回静态样式
    return tableColumn.headerCellStyle
  }
  return {}
}

/**
 * 获取表头单元格自定义类名
 * @param {Object} params - Element Plus 传递的参数对象
 * @returns {string} 类名字符串
 */
const getHeaderCellClassName = ({ row, column, rowIndex, columnIndex }: any) => {
  const tableColumn = props.columns[columnIndex]
  if (tableColumn?.headerCellClassName) {
    // 如果是函数，调用函数获取动态类名
    if (typeof tableColumn.headerCellClassName === 'function') {
      return tableColumn.headerCellClassName({ row, column, rowIndex, columnIndex })
    }
    // 如果是字符串，直接返回静态类名
    return tableColumn.headerCellClassName
  }
  return ''
}

/**
 * 获取数据单元格自定义样式
 * @param params - Element Plus 传递的参数对象
 * @returns 样式对象
 */
const getCellStyle = ({ row, column, rowIndex, columnIndex }: any) => {
  const tableColumn = props.columns[columnIndex]
  if (tableColumn?.cellStyle) {
    // 如果是函数，调用函数获取动态样式
    if (typeof tableColumn.cellStyle === 'function') {
      return tableColumn.cellStyle({ row, column, rowIndex, columnIndex })
    }
    // 如果是对象，直接返回静态样式
    return tableColumn.cellStyle
  }
  return {}
}

/**
 * 获取数据单元格自定义类名
 * @param params - Element Plus 传递的参数对象
 * @returns 类名字符串
 */
const getCellClassName = ({ row, column, rowIndex, columnIndex }: any) => {
  const tableColumn = props.columns[columnIndex]
  if (tableColumn?.cellClassName) {
    // 如果是函数，调用函数获取动态类名
    if (typeof tableColumn.cellClassName === 'function') {
      return tableColumn.cellClassName({ row, column, rowIndex, columnIndex })
    }
    // 如果是字符串，直接返回静态类名
    return tableColumn.cellClassName
  }
  return ''
}

/**
 * 处理排序变化事件
 * 当用户点击列头进行排序时触发
 * @param params - 排序参数对象
 * @param params.column - 列配置对象
 * @param params.prop - 排序字段名
 * @param params.order - 排序方向 ('ascending' | 'descending' | null)
 */
const handleSortChange = ({
  column,
  prop,
  order
}: {
  column: any
  prop: string
  order: string | null
}) => {
  // 向父组件发送排序变化事件
  emit('sort-change', { column, prop, order })
}

/**
 * 处理行点击事件
 * 当用户点击表格行时触发
 * @param row - 点击的行数据
 * @param column - 点击的列配置对象
 * @param event - 点击事件对象
 */
const handleRowClick = (row: any) => {
  emit('row-click', row)
}

// 格式化hover表格数据
const formatHoverTableData = (row: any) => {
  const data = [
    {
      name: '提及量',
      value: fmtNum(row.mentions),
      mom: fmtFix(row.mentionsMoM),
      yoy: fmtFix(row.mentionsYoY)
    },
    {
      name: '负面率',
      value: fmtPer(row.negativeRate),
      mom: fmtFix(row.negativeRateMoM),
      yoy: fmtFix(row.negativeRateYoY)
    }
  ]

  return props.hoverTableRows === 1 ? [data[0]] : data
}

// onMounted(() => {
//   console.log('processedColumns@@@',processedColumns)
// })
</script>

<!--
  模板部分
  使用 Element Plus 的 el-table 组件作为基础
-->
<template>
  <!--
    主表格组件
    配置了自定义样式处理函数和基础属性
  -->
  <el-table
    ref="elTableRef"
    :data="props.data"
    :height="props.height ? tableHeight : undefined"
    :border="false"
    v-loading="props.loading"
    :size="props.size"
    class="f-table"
    :header-cell-style="getHeaderCellStyle"
    :header-cell-class-name="getHeaderCellClassName"
    :cell-style="getCellStyle"
    :cell-class-name="getCellClassName"
    @sort-change="handleSortChange"
    @row-click="handleRowClick"
  >
    <!--
      动态生成表格列
      遍历处理后的列配置，为每列创建 el-table-column
    -->
    <el-table-column
      v-for="(column, index) in processedColumns"
      :key="index"
      :prop="column.dataIndex"
      :width="column.width"
      :min-width="column.minWidth"
      :align="column.align"
      :fixed="column.fixed"
      :sortable="column.sortable"
      :show-overflow-tooltip="column.showOverflowTooltip"
    >
      <!-- 自定义表头：函数类型标题 -->
      <template #header v-if="typeof column.title === 'function'">
        <component :is="column.title" />
      </template>

      <!-- 默认表头：字符串类型标题 -->
      <template #header v-else-if="typeof column.title === 'string'">
        {{ column.title }}
      </template>

      <!-- 自定义单元格内容：使用 render 函数渲染 -->
      <template #default="scope" v-if="column.render">
        <!-- 有悬浮弹窗且是第一列时显示 popover -->
        <template v-if="index === 0 && hasHoverPop">
          <el-popover
            placement="top"
            popper-class="tool-pop"
            :show-after="200"
            :width="410"
            trigger="hover"
          >
            <template #reference>
              <div style="width: 100%">
                <component
                  :is="column.render"
                  :record="scope.row"
                  :rowIndex="scope.$index"
                  :column="column"
                />
              </div>
            </template>
            <template #default>
              <div class="fs-14 fw-500 mb-12" style="color: #333">
                {{ column.dataIndex ? scope.row[column.dataIndex] : '-' }}
              </div>
              <el-table :data="formatHoverTableData(scope.row)" class="pop-table">
                <el-table-column prop="name" label="名称" width="70" />
                <el-table-column prop="value" label="数值" />
                <el-table-column prop="mom" label="环比" class-name="c666" />
                <el-table-column prop="yoy" label="同比" class-name="c666" />
              </el-table>
            </template>
          </el-popover>
        </template>
        <!-- 其他情况直接渲染组件 -->
        <template v-else>
          <component
            :is="column.render"
            :record="scope.row"
            :rowIndex="scope.$index"
            :column="column"
          />
        </template>
      </template>
    </el-table-column>
  </el-table>
</template>

<!--
  样式定义
  使用 SCSS 和深度选择器来自定义 Element Plus Table 的样式
-->
<style lang="scss" scoped>
.f-table {
  /* 使用深度选择器穿透组件样式作用域 */
  :deep(.el-table) {
    /* 表头样式 */
    thead {
      .el-table__header-wrapper th {
        /* 注意：不设置 background-color，允许自定义样式生效 */
        color: #26292e;
        font-weight: 500;
        font-size: 14px;
        border-bottom: 1px solid #f0f0f0;
      }
    }

    /* 表体样式 */
    tbody {
      .el-table__body-wrapper td {
        border-bottom: 1px solid #f6f6f6;
        color: #26292e;
        font-size: 14px;
      }

      /* 行悬停效果 */
      .el-table__row:hover td {
        background-color: #f5f7fa;
      }
    }
  }

  /* 加载遮罩样式 */
  :deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.9);
  }
}
</style>

<style lang="scss">
// 全局
.tool-pop {
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
