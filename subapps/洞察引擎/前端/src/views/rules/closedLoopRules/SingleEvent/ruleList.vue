<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { Column } from 'element-plus/es/components/table-v2'
import { ElMessage, ElMessageBox } from 'element-plus'
import searchPng from '@/assets/imgs/rules/search.png'
import RuleFormDialog from './RuleFormDialog.vue'
import { getCopyRule, queryRulePage, batchOperateClosedRule } from '@/api/rules'
import { debounce } from 'lodash-es'
import {
  singleEventStore,
  getNameByDictValue,
  singleRuleTypeValue,
  enabledStatusValue
} from './store'
import { RuleAssignMethod } from '../components/index'

const emits = defineEmits(['refresh'])

const hub = reactive({
  loading: false,
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    keyword: '' //关键字
  },
  total: 0,
  ruleList: [] as Array<any>,
  visible: false,
  ruleData: null as any, //当前编辑的规则数据
  enabledDict: [] as Array<any>, //启用字典
  isUserClickedSelectAll: false, //跟踪用户是否点击了全选按钮
  selectedRows: [] as Array<any> //选中的行数据
})

// 列定义（业务字段配置）
const columns = [
  {
    prop: 'selection',
    width: 56,
    align: 'center'
  },
  {
    label: '规则名称',
    prop: 'ruleName',
    width: 230
  },
  {
    label: '规则类型',
    prop: 'ruleType',
    width: 191
  },
  {
    label: '主题分类',
    prop: 'categoryTypeName',
    width: 191
  },
  {
    label: '创建人',
    prop: 'creator',
    width: 191
  },
  {
    label: '当前状态',
    prop: 'isEnabled',
    width: 111
  },
  {
    label: '操作',
    prop: 'actions',
    width: 191
  }
]

type RuleRow = any
type RuleTableColumn = Column<RuleRow> & {
  baseWidth?: number
  minWidth?: number
  flexWeight?: number
}

// TableV2 列配置（包含选择列与操作列）
const tableColumns = computed<RuleTableColumn[]>(() => {
  const dataColumns: RuleTableColumn[] = (columns as readonly any[]).map(col => {
    const isRuleNameColumn = col.prop === 'ruleName'
    const isActionColumn = col.prop === 'actions'

    return {
      key: col.prop,
      dataKey: col.prop,
      title: col.label,
      align: col.align,
      width: col.width,
      fixed: isActionColumn ? 'right' : undefined,
      baseWidth: col.width,
      minWidth: col.width,
      // 规则名称通常是最长文本，只让它承接剩余空间，避免其他列被无意义拉伸
      flexWeight: isRuleNameColumn ? 1 : 0
    } as RuleTableColumn
  })

  return [...dataColumns]
})

/**
 * 根据表格容器宽度重算列宽。
 * 仅“规则名称”列吸收剩余空间，操作列固定在右侧，确保核心操作始终可见。
 */
const resolveTableColumns = (containerWidth: number): Column<RuleRow>[] => {
  const sourceColumns = tableColumns.value || []
  if (!sourceColumns.length) return []

  const totalBaseWidth = sourceColumns.reduce((sum, column) => {
    return sum + Number(column.baseWidth ?? column.width ?? 0)
  }, 0)
  const extraWidth = Math.max(0, Number(containerWidth || 0) - totalBaseWidth)
  const totalFlexWeight = sourceColumns.reduce((sum, column) => {
    return sum + Number(column.flexWeight ?? 0)
  }, 0)

  return sourceColumns.map(column => {
    const { baseWidth, minWidth, flexWeight, ...rest } = column
    const base = Number(baseWidth ?? column.width ?? 0)

    let nextWidth = base
    if (extraWidth > 0 && totalFlexWeight > 0 && Number(flexWeight) > 0) {
      nextWidth = base + (extraWidth * Number(flexWeight)) / totalFlexWeight
    }

    return {
      ...rest,
      width: Math.max(Number(minWidth ?? 0), Math.floor(nextWidth))
    } as Column<RuleRow>
  })
}

const keywordSearch = debounce(() => {
  // 搜索后统一清空勾选，避免把旧查询结果中的选中项带入当前列表做批量操作
  clearSelection()
  fetchList()
}, 300)

const curLeftItem = ref()
/**
 * @prop resetScroll 切换分页时，将滚动条滚动到顶部
 */
const fetchList = async (resetScroll = true) => {
  try {
    hub.loading = true
    if (resetScroll) {
      hub.ruleList = []
    }
    const params: any = {
      pageNum: hub.queryParams.pageNum,
      pageSize: hub.queryParams.pageSize,
      categoryType: curLeftItem.value.id,
      ruleName: hub.queryParams.keyword
    }

    const response = await queryRulePage(params)
    if (response.success && response.result) {
      // 将接口返回的数据转换为组件需要的格式
      hub.ruleList =
        response.result.list?.map((item: any) => ({
          ...item,
          isActive: false,
          isDisable: false
        })) || []

      // 设置总数
      hub.total = response.result.total
    } else {
      ElMessage.error(response.message || '获取分类列表失败')
      hub.ruleList = []
    }
  } catch (error: any) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败，请稍后重试')
    hub.ruleList = []
  } finally {
    hub.loading = false
  }
}

const selectedIds = computed(() =>
  (hub.selectedRows || []).map((r: any) => r?.ruleId).filter(Boolean)
)

// 是否全选
const isAllSelected = computed(() => {
  return hub.ruleList.length > 0 && hub.selectedRows.length === hub.ruleList.length
})

// 是否处于半选状态
const isIndeterminate = computed(() => {
  return hub.selectedRows.length > 0 && hub.selectedRows.length < hub.ruleList.length
})

// 判断行是否选中
const isRowSelected = (row: RuleRow) => {
  return selectedIds.value.includes(row.ruleId)
}

// 清空勾选状态
const clearSelection = () => {
  hub.selectedRows = []
}

// 行级勾选
const handleRowSelectChange = (checked: boolean, row: RuleRow) => {
  const exists = (hub.selectedRows || []).some((item: any) => item.ruleId === row.ruleId)

  if (checked && !exists) {
    hub.selectedRows.push(row)
  } else if (!checked && exists) {
    hub.selectedRows = (hub.selectedRows || []).filter((item: any) => item.ruleId !== row.ruleId)
  }
}

// 头部全选
const handleSelectAllChange = (checked: boolean) => {
  if (checked) {
    hub.selectedRows = [...hub.ruleList]
  } else {
    clearSelection()
  }
}

// 批量操作：根据下拉命令(规则状态)对选中规则执行批量启用/停用
const handleClick = async (command: string) => {
  try {
    if (!command) return
    const ids = selectedIds.value
    const payload = {
      ids,
      isEnabled: command
    }
    const resp = await batchOperateClosedRule(payload)
    if (resp?.success) {
      ElMessage.success('批量操作成功')
      // 批量操作成功后重置勾选状态，确保后续操作只基于最新列表重新选择
      clearSelection()
      fetchList(false)
    } else {
      ElMessage.error(resp?.message || '批量操作失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '批量操作异常，请稍后重试')
  }
}
const handleEdit = (row: any) => {
  if (row) {
    //编辑
    hub.ruleData = row
  } else {
    //新增
    hub.ruleData = {
      ruleType: curLeftItem.value.ruleType || singleRuleTypeValue.value, //默认单点
      categoryType: curLeftItem.value.id,
      categoryTypeName: curLeftItem.value.name,
      isEnabled: enabledStatusValue.value,
      confirmMethod: RuleAssignMethod.MANUAL,
      auditMethod: RuleAssignMethod.MANUAL
    }
  }
  hub.visible = true
}

//赋值
const handleCopy = async (row: any) => {
  try {
    const response = await getCopyRule(row.ruleId)
    if (response.success) {
      ElMessage.success('复制规则成功')
      fetchList()
    } else {
      ElMessage.error(response.message || '复制规则失败')
    }
  } catch (e) {
    console.error('复制规则失败:', e)
  }
}

//刷新列表
const refreshList = () => {
  hub.queryParams.pageNum = 1
  // 刷新列表时清空勾选，保证分页、筛选与勾选状态一致
  clearSelection()
  fetchList()
}

// 分页
const handleSizeChange = (val: number) => {
  hub.queryParams.pageSize = val
  clearSelection()
  fetchList()
}

// 分页
const handleCurrentChange = (val: number) => {
  hub.queryParams.pageNum = val
  clearSelection()
  fetchList()
}

// 删除分类
const handleDelete = async (item: any) => {
  //调用接口
  // try {
  //   await ElMessageBox.confirm(`确定要删除分类 "${item.name}" 吗？`, '删除确认', {
  //     confirmButtonText: '确定',
  //     cancelButtonText: '取消',
  //     type: 'warning'
  //   })
  //   const response = await deleteSpecialType({ id: item.id })
  //   if (response.success) {
  //     ElMessage.success('删除分类成功')
  //     fetchList()
  //   } else {
  //     ElMessage.error(response.message || '删除分类失败')
  //   }
  // } catch (e) {
  //   console.error('删除分类失败:', e)
  // }
}

const getStatusTagType = (status?: string) => {
  let color = '#C9CDD4'
  switch (status) {
    case 'enabled':
      color = '#00B42A'
      break
  }
  return { backgroundColor: color }
}

const leftChange = (leftItem: any) => {
  curLeftItem.value = leftItem
  clearSelection()
  fetchList()
}

//弹框成功事件
const onDialogSuccess = () => {
  //编辑场景 不需要重置滚动条
  if (hub.ruleData.ruleId) {
    fetchList(false)
  } else {
    refreshList()
    emits('refresh')
  }
}

defineExpose({
  leftChange
})
</script>
<template>
  <div class="pl-24 h-full flex-col">
    <div class="flex-between items-center mb-24">
      <div class="header-title-class">规则列表</div>
      <div>
        <el-input
          v-model="hub.queryParams.keyword"
          style="width: 186px"
          placeholder="请输入规则名称搜索"
          @change="keywordSearch"
        >
          <template #suffix>
            <el-image :src="searchPng" style="width: 20px; height: 20px" />
          </template>
        </el-input>
        <el-dropdown trigger="click" placement="bottom-end" @command="handleClick">
          <el-button class="ml-16" text bg :disabled="!selectedIds.length">批量操作</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="opt in singleEventStore.conditions.closedRuleEnabledStatus"
                :key="opt.key"
                :command="opt.key"
                >{{ opt.value }}</el-dropdown-item
              >
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button
          :disabled="!curLeftItem"
          style="margin-left: 16px"
          type="primary"
          @click="handleEdit(null)"
        >
          <template #icon>
            <el-icon>
              <el-icon-plus />
            </el-icon>
          </template>
          新建规则</el-button
        >
      </div>
    </div>
    <div class="flex-auto overflow-hidden" v-loading="hub.loading">
      <el-auto-resizer>
        <template #default="{ height, width }">
          <el-table-v2
            :columns="resolveTableColumns(width)"
            :data="hub.ruleList"
            :width="width"
            :height="height"
            :header-height="55"
            :row-height="55"
            row-key="ruleId"
            fixed
          >
            <template #header-cell="{ column }">
              <template v-if="column.key === 'selection'">
                <el-checkbox
                  :indeterminate="isIndeterminate"
                  :model-value="isAllSelected"
                  @change="handleSelectAllChange"
                />
              </template>
              <template v-else>
                {{ column.title }}
              </template>
            </template>

            <template #cell="{ column, rowData }">
              <template v-if="column.key === 'selection'">
                <el-checkbox
                  :model-value="isRowSelected(rowData)"
                  @change="(val: boolean) => handleRowSelectChange(val, rowData)"
                />
              </template>
              <template v-else-if="column.key === 'isEnabled'">
                <div class="flex-y-center">
                  <div class="status-icon mr-8" :style="getStatusTagType(rowData.isEnabled)"></div>
                  <span>{{
                    getNameByDictValue(
                      singleEventStore.conditions.closedRuleEnabledStatus,
                      rowData.isEnabled
                    )
                  }}</span>
                </div>
              </template>
              <template v-else-if="column.key === 'ruleType'">
                {{
                  getNameByDictValue(singleEventStore.conditions.closedRuleType, rowData.ruleType)
                }}
              </template>
              <template v-else-if="column.key === 'creator'">
                {{ rowData.creator?.name || '' }}-{{ rowData.creator?.employeeId || '' }}
              </template>
              <template v-else-if="column.key === 'actions'">
                <div class="flex-y-center">
                  <el-button link type="primary" @click="handleEdit(rowData)"> 编辑 </el-button>
                  <!--            <el-button link type="primary" @click="() => {}"> 测试 </el-button>-->
                  <el-button link type="primary" @click="() => handleCopy(rowData)">
                    复制
                  </el-button>
                </div>
              </template>
              <template v-else>
                {{ rowData[column.dataKey] }}
              </template>
            </template>
          </el-table-v2>
        </template>
      </el-auto-resizer>
    </div>
    <el-pagination
      v-if="hub.total > 0"
      v-model:current-page="hub.queryParams.pageNum"
      v-model:page-size="hub.queryParams.pageSize"
      :total="hub.total"
      :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
      layout="->,total, prev, pager, next, sizes"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pt-16"
    />
    <RuleFormDialog
      v-model:visible="hub.visible"
      :ruleData="hub.ruleData"
      @success="onDialogSuccess"
    />
  </div>
</template>
<style lang="scss" scoped>
.header-title-class {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  line-height: 32px;
}
:deep(.el-table .el-table__cell) {
  height: 55px;
  padding: 0 !important;
}

:deep(.el-table__header) {
  .el-table__cell {
    color: #1d2129;
    font-weight: 500;
    font-size: 14px;
  }
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-table__body-wrapper) {
  .el-table__cell {
    color: #1d2129;
    font-weight: 400;
    font-size: 14px;
  }
}

:deep(.el-table--fit .el-table__inner-wrapper:before) {
  width: 0 !important;
}

// TableV2 样式适配
:deep(.el-table-v2__row-cell) {
  font-weight: 400;
  font-size: 14px;
  color: #1d2129;
}

:deep(.el-table-v2__header-cell) {
  font-weight: 500;
  font-size: 14px;
  color: #1d2129;
  background-color: #f2f4f7 !important;
}

:deep(.el-button.is-text.is-disabled) {
  background-color: #f2f3f5 !important;
}
</style>
