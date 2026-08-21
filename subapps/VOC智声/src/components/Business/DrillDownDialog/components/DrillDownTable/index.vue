<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import type { DrillDownTableProps, DrillDownTableEvents, TableSortable } from './types'
import BrandMentionTrend from '@views/overview/user/components/BrandView/BrandMentionTrend.vue'
import type { TableColumnCtx, TableInstance } from 'element-plus'

// 定义组件名称
defineOptions({ name: 'DrillDownTable' })

// 定义 Props
const props = withDefaults(defineProps<DrillDownTableProps>(), {
  data: () => [],
  columns: () => [],
  height: '400px',
  headerHeight: '56px',
  rowHeight: '56px',
  headerBackgroundColor: '#EAF3FF',
  rowBackgroundColor: '#F2F4F7',
  stripe: false,
  showSortIcon: true,
  border: false,
  loading: false,
  //更多加载中
  loadingMore: false,
  //更多加载完成
  loadingMoreDone: false,
  scrollable: false
})

// 定义事件
const emit = defineEmits<DrillDownTableEvents>()

// 计算表格数据
const tableData = computed(() => props.data)

// 获取表头,内容背景颜色
const getColumnsBackground = (backgroundColor: string | string[] | undefined) => {
  let headerBackgroundColor = props.headerBackgroundColor
  let rowBackgroundColor = props.rowBackgroundColor
  if (backgroundColor) {
    //header.backgroundColor如果是字符串
    if (typeof backgroundColor === 'string') {
      headerBackgroundColor = backgroundColor
      rowBackgroundColor = backgroundColor
      //字符串集合
    } else if (backgroundColor instanceof Array) {
      let [color1, color2] = backgroundColor
      headerBackgroundColor = color1 || headerBackgroundColor
      rowBackgroundColor = color2 || rowBackgroundColor
    }
  }
  return {
    headerBgColor: headerBackgroundColor,
    rowBgColor: rowBackgroundColor
  }
}

//获取内容高度
const getContentHeight = (rowHeight: string | number | undefined) => {
  //rowHeight number类型时需要增加处理
  rowHeight = typeof rowHeight === 'number' ? `${rowHeight}px` : rowHeight
  //props.rowHeight number类型时需要增加处理
  const propsRowHeight =
    typeof props.rowHeight === 'number' ? `${props.rowHeight}px` : props.rowHeight
  return rowHeight || propsRowHeight
}

// 根据背景颜色生成对应的CSS类名
const getBackgroundClass = (backgroundColor?: string): string => {
  if (!backgroundColor || typeof backgroundColor !== 'string') return ''

  try {
    // 移除 # 号并转换为小写，确保是有效的颜色值
    const colorCode = backgroundColor.replace('#', '').toLowerCase()
    // 验证是否为有效的十六进制颜色
    if (!/^[0-9a-f]{3,6}$/i.test(colorCode)) {
      console.warn('无效的颜色值:', backgroundColor)
      return ''
    }
    return `bg-${colorCode}`
  } catch (error) {
    console.warn('生成背景色类名时出错:', error)
    return ''
  }
}

// 缓存计算结果，避免重复计算
const styleCache = ref<Map<string, Record<string, string>>>(new Map())

// 生成样式缓存键
const generateStyleCacheKey = (columns: typeof props.columns): string => {
  return JSON.stringify(
    columns?.map(col => ({
      key: col.key,
      backgroundColor: col.backgroundColor,
      columnPadding: col.columnPadding,
      rowHeight: col.rowHeight
    })) || []
  )
}

// 优化的动态样式计算
const dynamicStyles = computed(() => {
  try {
    if (!props.columns?.length) return {}

    const cacheKey = generateStyleCacheKey(props.columns)

    // 检查缓存
    if (styleCache.value.has(cacheKey)) {
      return styleCache.value.get(cacheKey)!
    }

    const styles: Record<string, string> = {}

    // 添加基础表头样式（只添加一次）
    styles[`.drill-down-table .el-table__header .group-header .cell`] =
      `height: ${props.headerHeight}; line-height: ${props.headerHeight}; margin-bottom: 4px`
    styles[`.drill-down-table .el-table__header .columns-header .cell`] =
      `height: calc((${props.headerHeight} - 8px) / 2); line-height: calc((${props.headerHeight} - 8px) / 2)`

    // 遍历列配置生成样式
    props.columns.forEach((header, index) => {
      try {
        // 背景颜色样式
        const { headerBgColor, rowBgColor } = getColumnsBackground(header.backgroundColor)

        if (headerBgColor) {
          const headerClassName = getBackgroundClass(headerBgColor)
          if (headerClassName) {
            styles[`.drill-down-table .el-table__header .${headerClassName} .cell`] =
              `background-color: ${headerBgColor} !important;`
          }
        }

        if (rowBgColor) {
          const itemClassName = getBackgroundClass(rowBgColor)
          if (itemClassName) {
            styles[`.drill-down-table .el-table__body .${itemClassName} .cell`] =
              `background-color: ${rowBgColor} !important;`
          }
        }

        // 行高样式
        const rowHeight = getContentHeight(header.rowHeight)
        if (rowHeight) {
          const rowHeightClassName = getHeightClass(rowHeight)
          if (rowHeightClassName) {
            styles[`.drill-down-table .el-table__body .${rowHeightClassName} .cell`] =
              `height: ${rowHeight}; line-height: ${rowHeight}`
          }
        }

        // 内间距样式
        const paddingClass = getPaddingClass(header.columnPadding)
        if (paddingClass && header.columnPadding) {
          const paddingValue = Array.isArray(header.columnPadding)
            ? header.columnPadding.join(' ')
            : header.columnPadding
          styles[`.drill-down-table .${paddingClass}`] = `padding: ${paddingValue} !important;`
        }
      } catch (error) {
        console.warn(`生成第${index}列样式时出错:`, error)
      }
    })

    // 缓存结果（限制缓存大小）
    if (styleCache.value.size > 10) {
      const firstKey = styleCache.value.keys().next().value
      if (firstKey !== undefined) {
        styleCache.value.delete(firstKey)
      }
    }
    styleCache.value.set(cacheKey, styles)

    return styles
  } catch (error) {
    console.error('动态样式计算出错:', error)
    return {}
  }
})

//根据padding生成对应的CSS类名
const getPaddingClass = (padding: string | string[] | undefined): string => {
  if (!padding) return ''
  // 处理字符串类型
  if (typeof padding === 'string') {
    // 移除空格并替换特殊字符，生成有效的CSS类名
    return `padding-${padding.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9-]/g, '')}`
  }
  // 处理字符串数组类型
  if (Array.isArray(padding)) {
    // 将数组元素用下划线连接，并替换特殊字符
    return `padding-${padding.join('_').replace(/[^a-zA-Z0-9_]/g, '')}`
  }
  return ''
}

//根据height生成对应的CSS类名
const getHeightClass = (height: string | number | undefined): string => {
  if (!height) return ''
  try {
    // 清理高度值中的特殊字符，确保生成有效的CSS类名
    const cleanHeight = String(height).replace(/[^a-zA-Z0-9-]/g, '')
    return cleanHeight ? `height-${cleanHeight}` : ''
  } catch (error) {
    console.warn('生成高度类名时出错:', error)
    return ''
  }
}

// 事件处理函数
const handleSortChange = ({ column, prop, order }: any): void => {
  emit('sortChange', prop, order)
}

// 将字符串数值进行规范化，便于数值排序
const toNumeric = (val: unknown): number => {
  // 已是数值直接返回
  if (typeof val === 'number') return val
  if (val === null || val === undefined) return NaN
  // 处理常见的字符串数值：如 "-50"、"20"、"1,234"、"12.3%"
  const str = String(val).trim().replace(/,/g, '').replace(/%/g, '')
  const num = parseFloat(str)
  return Number.isNaN(num) ? NaN : num
}

// 生成指定字段的排序方法（Element Plus 本地排序）
// 说明：仅负责比较两行记录在该字段上的大小关系
const makeSorterFor = (key: string) => {
  return (a: Record<string, unknown>, b: Record<string, unknown>) => {
    const av = toNumeric(a?.[key])
    const bv = toNumeric(b?.[key])

    // 两者均不可解析为数值时，按字符串比较，保证排序稳定
    if (Number.isNaN(av) && Number.isNaN(bv)) {
      const as = String(a?.[key] ?? '')
      const bs = String(b?.[key] ?? '')
      return as.localeCompare(bs, 'zh')
    }
    // 单侧不可解析时，将不可解析项放在后面（升序场景下更符合直觉）
    if (Number.isNaN(av)) return 1
    if (Number.isNaN(bv)) return -1

    // 数值直接比较，负数与小数均可正确排序
    return av - bv
  }
}

/**
 * 仅在声明为本地排序时挂载排序函数。
 * 当列使用 custom 排序时，交由父组件发起后端请求，避免本地数据先被重排。
 */
const getSortMethod = (sortable: TableSortable | undefined, key: string) => {
  if (!sortable || sortable === 'custom') return undefined
  return makeSorterFor(key)
}

const handleRowClick = (row: any, column: any, event: MouseEvent): void => {
  try {
    const index = props.data.findIndex(item => item === row)
    // 处理findIndex可能返回-1的情况
    if (index !== -1) {
      emit('rowClick', row, index)
    } else {
      console.warn('未找到对应的行数据')
      emit('rowClick', row, -1)
    }
  } catch (error) {
    console.error('行点击事件处理出错:', error)
  }
}

//点击单元格
const handleCellClick = (
  row: any,
  column: TableColumnCtx<any>,
  cell: HTMLTableCellElement,
  event: Event
): void => {
  emit('cellClick', row, column, cell, event)
}

const handleTrendClick = (row: any): void => {
  try {
    // 检查趋势数据是否存在
    if (!row?.mentionsTrendData) {
      console.warn('趋势数据不存在:', row)
    }
    emit('trendClick', row)
  } catch (error) {
    console.error('趋势图点击事件处理出错:', error)
  }
}

// 表格滚动监听（用于滚动分页触底触发）
const rootEl = ref<HTMLElement | null>(null)
const tableRef = ref<TableInstance | null>(null)

const handleScroll = (event: any) => {
  const root = rootEl.value as HTMLElement | null
  if (!root) return
  // el-table 的可滚动容器选择器
  const wrapper = root.querySelector('.el-table__body-wrapper') as HTMLElement | null
  const table = root.querySelector('.el-table__body-wrapper .el-table__body') as HTMLElement | null
  if (!wrapper || !table) return
  const nearBottom = event.scrollTop + wrapper.clientHeight >= table.scrollHeight - 50 // 底部阈值 50px
  if (nearBottom) emit('reachBottom')
}

// 公开的实例方法
const refresh = (): void => {}
const clearSort = (): void => {}
// 将表格滚动位置重置到顶部，分页切换等场景可调用
const scrollToTop = (): void => {
  nextTick(() => {
    // 使用 Element Plus 提供的表格实例方法重置表体滚动条位置
    if (tableRef.value) {
      tableRef.value.setScrollTop(0)
    }
  })
}
const getTableData = () => props.data

// 暴露给父组件使用
defineExpose({
  refresh,
  clearSort,
  scrollToTop,
  getTableData
})

// console.log('tableData@@',tableData.value)
// console.log('columns@@',props.columns)
</script>

<template>
  <div class="drill-down-table" :class="{ 'flex-auto overflow-auto': scrollable }" ref="rootEl">
    <!-- 动态样式注入 -->
    <component :is="'style'" v-if="Object.keys(dynamicStyles).length > 0">
      {{
        Object.entries(dynamicStyles)
          .map(([selector, style]) => `${selector} { ${style} }`)
          .join('\n')
      }}
    </component>
    <!-- 表格内容 -->
    <div class="table-container" :style="{ height }">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        :stripe="stripe"
        :height="height"
        :span-method="spanMethod"
        :border="border"
        :show-border="border"
        class="custom-table"
        @scroll="handleScroll"
        @sort-change="handleSortChange"
        @row-click="handleRowClick"
        @cell-click="handleCellClick"
      >
        <!-- 动态生成多级表头 -->
        <template v-for="(headerGroup, groupIndex) in columns" :key="`groupIndex${groupIndex}`">
          <!-- 提及列组 -->
          <el-table-column
            :prop="headerGroup.key"
            :label="headerGroup.label"
            :width="headerGroup.width"
            :min-width="headerGroup.minWidth"
            :sortable="headerGroup.sortable"
            :sort-method="getSortMethod(headerGroup.sortable, headerGroup.key)"
            :class-name="`${getHeightClass(getContentHeight(headerGroup.rowHeight))} columns-cell ${headerGroup?.columns?.length ? 'columns-header' : 'group-header'} ${getPaddingClass(headerGroup.columnPadding)} ${getBackgroundClass(getColumnsBackground(headerGroup.backgroundColor).headerBgColor)} ${getBackgroundClass(getColumnsBackground(headerGroup.backgroundColor).rowBgColor)} ${getPaddingClass(headerGroup.columnPadding)}`"
          >
            <template #header>
              <span>
                {{ headerGroup.label }}
              </span>
            </template>
            <template #default="{ row: groupRow }">
              <template v-if="headerGroup?.columns?.length">
                <!-- 提及量 -->
                <el-table-column
                  v-for="(column, index) in headerGroup.columns"
                  :prop="column.key"
                  :label="column.label"
                  :width="column.width"
                  :min-width="column.minWidth"
                  :sortable="column.sortable"
                  :sort-method="getSortMethod(column.sortable, column.key)"
                  :key="index"
                  :class-name="`${getHeightClass(getContentHeight(headerGroup.rowHeight))} columns-cell columns-header ${getBackgroundClass(getColumnsBackground(headerGroup.backgroundColor).headerBgColor)} ${getBackgroundClass(getColumnsBackground(headerGroup.backgroundColor).rowBgColor)} ${getPaddingClass(headerGroup.columnPadding)}`"
                >
                  <template #header>
                    <span>
                      {{ column.label }}
                    </span>
                  </template>
                  <template #default="{ row }">
                    <el-tooltip
                      effect="dark"
                      :disabled="column.tooltip?.show !== true"
                      :content="String(row[column.key] ?? '')"
                      placement="top"
                      popper-class="text-tooltip-light"
                    >
                      <template v-if="column.key === 'Operations'">
                        <slot name="operations" v-bind="{ row, index }"></slot>
                      </template>
                      <template v-else-if="column.render === 'trend'">
                        <div class="trend-chart-layout">
                          <div class="trend-chart" @click.stop="handleTrendClick(row)">
                            <BrandMentionTrend
                              :style="`height: calc((${getContentHeight(props.rowHeight)} - 16px) / 2)`"
                              :trend-data="row[column.key] || []"
                              :smooth="true"
                              :show-symbol="false"
                            />
                          </div>
                        </div>
                      </template>
                      <template v-else-if="typeof column.render === 'function'">
                        <div
                          v-if="column.render"
                          v-html="column.render(row, column)"
                          class="column-cell-class"
                        ></div>
                      </template>
                      <template v-else-if="typeof column.render === 'string'">
                        <slot :name="column.render" v-bind="{ row, index }">
                          <div class="column-cell-class">{{ row[column.key] }}</div>
                        </slot>
                      </template>
                      <template v-else>
                        <div class="column-cell-class">{{ row[column.key] }}</div>
                      </template>
                    </el-tooltip>
                  </template>
                </el-table-column>
              </template>
              <template v-else>
                <el-tooltip
                  effect="dark"
                  :disabled="headerGroup.tooltip?.show !== true"
                  :content="String(groupRow[headerGroup.key] ?? '')"
                  placement="top"
                  popper-class="text-tooltip-light"
                >
                  <template v-if="headerGroup.key === 'Operations'">
                    <slot name="operations" v-bind="{ row: groupRow, index: groupIndex }"></slot>
                  </template>
                  <template v-else-if="headerGroup.render === 'trend'">
                    <div class="trend-chart-layout">
                      <div class="trend-chart" @click.stop="handleTrendClick(groupRow)">
                        <BrandMentionTrend
                          :style="`height: calc((${getContentHeight(props.rowHeight)} - 16px) / 2)`"
                          :trend-data="groupRow[headerGroup.key] || []"
                          :smooth="true"
                          :show-symbol="false"
                        />
                      </div>
                    </div>
                  </template>
                  <template v-else-if="typeof headerGroup.render === 'function'">
                    <div
                      v-if="headerGroup.render"
                      v-html="headerGroup.render(groupRow, headerGroup)"
                      class="column-cell-class"
                    ></div>
                  </template>
                  <template v-else-if="typeof headerGroup.render === 'string'">
                    <slot :name="headerGroup.render" v-bind="{ row: groupRow, index: groupIndex }">
                      <div class="column-cell-class">{{ groupRow[headerGroup.key] }}</div>
                    </slot>
                  </template>
                  <template v-else>
                    <div class="column-cell-class">{{ groupRow[headerGroup.key] }}</div>
                  </template>
                </el-tooltip>
              </template>
            </template>
          </el-table-column>
        </template>
        <template #append>
          <p v-if="loadingMore" class="text-center p-6">Loading...</p>
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">
.drill-down-table {
  width: 100%;

  .table-container {
    width: 100%;
    //overflow: auto;

    :deep(.el-table) {
      border: none;

      .cell {
        padding-left: 0 !important;
        padding-right: 0 !important;
        border-radius: 8px;
        font-size: 16px;
        color: #333;
        font-weight: 500;
      }

      .column-cell-class {
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
        width: 100%;
      }
    }

    .custom-table {
      width: 100%;
      border: none;

      :deep(.el-table__header) {
        th {
          background-color: $neutral-white !important;
        }

        .el-table__cell {
          //background-color: $neutral-white;
          border: none;
          text-align: center;
          vertical-align: middle;
        }
      }

      :deep(.el-table__body) {
        .el-table__cell {
          border: none;
          text-align: center;
          vertical-align: middle;

          .cell {
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }

      // 设置列间距
      :deep(.el-table__cell) {
        padding: 0 4px;
      }

      // 设置行间距
      :deep(.el-table__row) {
        td {
          padding-top: 4px;
          padding-bottom: 4px;
        }
      }
    }

    .trend-chart-layout {
      padding: 5px 0 !important;
    }

    // 移除表格边框伪元素的背景色
    :deep(.el-table--border .el-table__inner-wrapper:after),
    :deep(.el-table--border:after),
    :deep(.el-table--border:before),
    :deep(.el-table__inner-wrapper:before),
    :deep(.el-table__border-left-patch) {
      width: 0 !important;
      height: 0 !important;
    }

    .group-header {
      height: 100%;
      text-align: center;
      font-weight: $font-weight-semibold;
      font-size: $font-size-body;
      color: $text-primary;
      border-radius: $border-radius-m;
    }

    .trend-chart {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 46px; // 适配 BrandMentionTrend 组件的高度
      padding: 8px 0;
    }
  }
}
</style>
