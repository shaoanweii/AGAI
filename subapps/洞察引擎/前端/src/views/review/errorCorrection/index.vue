<script setup lang="ts">
import dayjs from 'dayjs'
import { h } from 'vue'
import { useTable } from '@/hooks/table'
import useConditions from '@/hooks/useConditions'
import { computedCardHeight, listHeight } from '@/utils'
import ErrorCorrectionDetailDialog from './components/ErrorCorrectionDetailDialog.vue'
import BatchReview from './components/BatchReview.vue'
import FDatePicker from '@/components/FDatePicker/index.vue'
import { queryCreateUserList } from '@/api/review'
import { findAllFinalTagLib } from '@/api/tag'
import { ElMessage, ElButton, ElCheckbox, TableV2FixedDir } from 'element-plus'
import type { Column } from 'element-plus'

defineOptions({
  name: 'errorCorrectionReview'
})
const { conditions } = useConditions({ url: '/insights/addLabel/conditions' })
provide('conditions', conditions)

const {
  table,
  form,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getTableData,
  getFirstPageTableData,
  handleEdit,
  handleDelete
} = useTable(
  {
    method: 'POST',
    url: '/insights/addLabel/queryLabelCorrectionList',
    deleteUrl: '/insights/addLabel/del'
  },
  res => {
    return res.result
  }
)

const batchVisible = ref(false)
const times = ref<any[]>([])
// const shortcutValue = ref('近7天')
const shortcutValue = ref('本年')
// 业务要求：纠错审核页的时间筛选不能选择今天之后的日期。
const maxSelectableDate = dayjs().format('YYYY-MM-DD')
const multipleSelection = ref<any[]>([])

/**
 * 清空当前列表勾选，避免查询条件变化后继续沿用旧结果集的批量操作对象。
 */
const clearListSelection = () => {
  multipleSelection.value = []
}

const cureateUserOptions = ref<any[]>([])
// 发起人
const getCureateUserOptions = async () => {
  try {
    const res = (await queryCreateUserList({})) as any
    cureateUserOptions.value = res.result
  } catch {
    cureateUserOptions.value = []
  }
}

const topicOptions = ref<any[]>([])
// 观点
const getLastTagOptions = async () => {
  try {
    const res = await findAllFinalTagLib({})
    topicOptions.value = res.result
  } catch {
    topicOptions.value = []
  }
}

const query = () => {
  // 查询会刷新列表口径，需先清空旧勾选，避免批量审核误用历史选择。
  clearListSelection()
  const [startTime, endTime] = times.value
  table.filter.startTime = startTime
  table.filter.endTime = endTime
  getFirstPageTableData()
}
const reset = () => {
  // 重置筛选后列表将恢复默认口径，因此同步清空勾选态。
  clearListSelection()
  handleReset(() => {
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime
  })
}

const init = () => {
  // getLastTagOptions()
  getCureateUserOptions()
}

init()

onMounted(() => {
  query()
})

const handleBatchReview = () => {
  if (!multipleSelection.value?.length) {
    ElMessage.error('请先选择数据')
    return
  }

  // 校验审核状态
  const hasNonPendingItems = multipleSelection.value.some(item => item.auditStatusCode !== '0')
  if (hasNonPendingItems) {
    ElMessage.error('仅可针对待审核内容进行批量操作，请重新选择')
    return
  }

  batchVisible.value = true
}

/**
 * @description: 查看详情（只读模式）
 * @param {*} row
 * @return {*}
 */
const handleView = (row: any) => {
  form.data = row
  form.operation = 'view'
  form.visible = true
}

const handleBatchReviewSuccess = () => {
  // 批量审核成功后列表数据口径已变化，需先清空勾选再回到第一页刷新。
  clearListSelection()
  getFirstPageTableData()
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status?: string) => {
  let color = ''
  switch (status) {
    // case '待审核':
    case '0':
      color = '#1677FF' // 启用
      break
    // case '已撤销':
    case '3':
      color = '#C9CDD4' // 停用
      break
    // case '已拒绝':
    case '2':
      color = '#FF5959' // 停用
      break
    // case '已通过':
    case '1':
      color = '#00B42A' // 停用
      break
    default:
      color = 'red'
      break
  }
  return { backgroundColor: color }
}

// 动态计算列宽
const getColumnWidth = (totalWidth: number) => {
  const fixedWidth = 55 + 120 + 180 + 180 + 120 + 100 // 选择+纠错数量+发起人+审核人+审核状态+操作
  const flexibleWidth = totalWidth - fixedWidth
  const flexibleColumns = 3 // 纠错信息+发起时间+审核时间
  const eachFlexWidth = Math.max(150, flexibleWidth / flexibleColumns)
  return eachFlexWidth
}

// 表格列配置
const getColumns = (tableWidth: number): Column[] => {
  const flexWidth = getColumnWidth(tableWidth)
  return [
    {
      key: 'selection',
      width: 55,
      fixed: true,
      cellRenderer: ({ rowData }) => {
        return h(ElCheckbox, {
          modelValue: multipleSelection.value.some(item => item.id === rowData.id),
          'onUpdate:modelValue': (val: any) => {
            if (val) {
              multipleSelection.value = [...multipleSelection.value, rowData]
            } else {
              multipleSelection.value = multipleSelection.value.filter(
                item => item.id !== rowData.id
              )
            }
          }
        })
      },
      headerCellRenderer: () => {
        const tableList = table.list || []
        const allSelected =
          tableList.length > 0 && multipleSelection.value.length === tableList.length
        const indeterminate =
          multipleSelection.value.length > 0 && multipleSelection.value.length < tableList.length
        return h(ElCheckbox, {
          modelValue: allSelected,
          indeterminate: indeterminate,
          'onUpdate:modelValue': (val: any) => {
            if (val) {
              multipleSelection.value = [...tableList]
            } else {
              multipleSelection.value = []
            }
          }
        })
      }
    },
    {
      key: 'correctionInfo',
      title: '纠错信息',
      dataKey: 'correctionInfo',
      width: flexWidth,
      cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
    },
    {
      key: 'correctionCount',
      title: '纠错数量',
      dataKey: 'correctionCount',
      width: 120,
      cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
    },
    {
      key: 'createUser',
      title: '发起人',
      dataKey: 'createUser',
      width: 180,
      cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
    },
    {
      key: 'createTime',
      title: '发起时间',
      dataKey: 'createTime',
      width: flexWidth,
      cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
    },
    {
      key: 'auditTime',
      title: '审核时间',
      dataKey: 'auditTime',
      width: flexWidth,
      cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
    },
    {
      key: 'auditUser',
      title: '审核人',
      dataKey: 'auditUser',
      width: 180,
      cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
    },
    {
      key: 'auditStatus',
      title: '审核状态',
      dataKey: 'auditStatus',
      width: 120,
      cellRenderer: ({ rowData }) => {
        return h('div', { class: 'flex-y-center' }, [
          h('div', {
            class: 'status-icon mr-8',
            style: getStatusTagType(rowData.auditStatusCode)
          }),
          h('span', rowData.auditStatus)
        ])
      }
    },
    {
      key: 'operation',
      title: '操作',
      width: 100,
      fixed: TableV2FixedDir.RIGHT,
      align: 'center',
      cellRenderer: ({ rowData }) => {
        return h(
          ElButton,
          {
            type: 'primary',
            link: true,
            onClick: () =>
              rowData.auditStatusCode === '0' ? handleEdit(rowData) : handleView(rowData)
          },
          () => (rowData.auditStatusCode === '0' ? '审核' : '查看')
        )
      }
    }
  ]
}
</script>

<template>
  <div>
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <div class="flex w-full">
        <div class="flex-1">
          <el-form layout="inline" :model="table.filter">
            <el-row class="w-full" :gutter="24">
              <el-col :span="8">
                <el-form-item label="时间范围">
                  <FDatePicker
                    v-model="times"
                    v-model:shortcutValue="shortcutValue"
                    type="daterange"
                    :clearable="false"
                    :max-selectable-date="maxSelectableDate"
                  ></FDatePicker>
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item label="发起人">
                  <el-select
                    v-model="table.filter.createUserName"
                    placeholder="全部"
                    multiple
                    :max-collapse-tags="1"
                    clearable
                  >
                    <el-option
                      v-for="(item, index) in cureateUserOptions"
                      :key="index"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="审核状态">
                  <el-select
                    v-model="table.filter.auditStatus"
                    placeholder="全部"
                    multiple
                    :max-collapse-tags="1"
                    clearable
                  >
                    <el-option
                      v-for="(item, index) in conditions.auditStatus"
                      :key="index"
                      :data-testid="`dataSource-original-10002-op-${index}`"
                      :label="item.value"
                      :value="item.key"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="w-220 border-left-e5e6eb">
          <div class="w-full h-full flex justify-center items-center">
            <div>
              <el-button type="primary" :data-testid="`dataSource-original-10005`" @click="query">
                <el-icon style="vertical-align: middle" class="mr-10">
                  <Search />
                </el-icon>
                查询
              </el-button>
            </div>
            <div class="ml-20">
              <el-button
                color="#F2F3F5"
                :data-testid="`dataSource-original-10004`"
                class=""
                @click="reset"
              >
                <el-icon style="vertical-align: middle" class="mr-10"><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </FtCard>

    <FtCard
      :style="computedCardHeight(175)"
      title="数据列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
    >
      <template #extra>
        <el-button :disabled="!multipleSelection?.length" type="primary" @click="handleBatchReview">
          批量审核
        </el-button>
      </template>
      <div class="table-container" :style="computedCardHeight(320)">
        <el-auto-resizer>
          <template #default="slotProps">
            <el-table-v2
              :data-testid="`dataSource-original-table`"
              v-loading="table.loading"
              :columns="getColumns(slotProps.width)"
              :data="table.list || []"
              :width="slotProps.width"
              :height="slotProps.height"
              :row-key="'id'"
              fixed
            />
          </template>
        </el-auto-resizer>

        <!-- 分页组件 -->
        <el-pagination
          v-if="table.total > 0"
          v-model:current-page="table.pageNum"
          v-model:page-size="table.pageSize"
          :page-sizes="[10, 15, 20, 25, 50, 100]"
          :total="table.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </div>
    </FtCard>

    <ErrorCorrectionDetailDialog
      v-model:visible="form.visible"
      :row-data="form.data"
      :topicOptions="topicOptions"
      :is-edit="form.operation === 'edit'"
      :is-read-only="form.operation === 'view'"
      @success="getTableData(false)"
    ></ErrorCorrectionDetailDialog>

    <BatchReview
      v-model:visible="batchVisible"
      :selected-data="multipleSelection"
      @success="handleBatchReviewSuccess"
    ></BatchReview>
  </div>
</template>

<style lang="scss" scoped>
.table-container {
  height: calc(100% - 48px); /* 减去分页组件的高度48px */
}

:deep(.cell-wrap-text) {
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  overflow-wrap: break-word;
  hyphens: auto;
}

.status-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
</style>
