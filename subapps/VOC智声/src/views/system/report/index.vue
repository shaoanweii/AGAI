<template>
  <div class="role-management">
    <!-- 报告列表 -->
    <el-card class="table-card" shadow="never">
      <div class="flex-between items-center mb-24">
        <div class="text-h3" style="font-weight: 600">报告列表</div>
        <div class="flex gap-16">
          <el-select
            v-model="formData.firstLevelZoneIds"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
            :options="zoneOptions"
            :props="{ label: 'name', value: 'id' }"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            @change="handleZoneChange"
            @blur="handleSearch"
          />

          <el-select
            v-model="formData.specialTypeIds"
            placeholder="请选择专区"
            clearable
            style="width: 150px"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            :options="specialZoneOptions"
            :props="{ label: 'name', value: 'id' }"
            @blur="handleSearch"
          >
          </el-select>
          <el-select
            v-model="formData.statuses"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            :options="statusOptions"
            :props="{ label: 'text', value: 'value' }"
            @blur="handleSearch"
          >
          </el-select>
          <el-input
            v-model="formData.reportName"
            placeholder="请输入关键词搜索"
            clearable
            :maxlength="20"
            style="width: 200px"
            @keyup.enter="handleSearch"
            @blur="handleSearch"
          />

          <el-dropdown v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU" trigger="click">
            <el-button type="primary" :disabled="selection?.length === 0">
              批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="canShowReview"
                  @click="handleBatchOperation(ReportBatchType.Review)"
                  >批量审核</el-dropdown-item
                >
                <el-dropdown-item
                  v-if="canShowRelease"
                  @click="handleBatchOperation(ReportBatchType.Release)"
                  >批量发布</el-dropdown-item
                >
                <el-dropdown-item
                  v-if="canShowDelisted"
                  @click="handleBatchOperation(ReportBatchType.Delisted)"
                  >批量下架</el-dropdown-item
                >
                <el-dropdown-item
                  v-if="canShowMove"
                  @click="handleBatchOperation(ReportBatchType.Move)"
                  >批量移动</el-dropdown-item
                >
                <!-- 批量删除 -->
                <el-dropdown-item v-if="canShowBatchDelete" @click="handleBatchDelete"
                  >批量删除</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <el-table
        ref="tableRef"
        v-loading="tableState.loading.value"
        :data="tableState.dataList.value"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
        @selection-change="(val: any) => tableMethods.handleSelectionChange(val)"
      >
        <el-table-column type="selection" width="56" align="center" />

        <el-table-column label="报告名称" prop="reportName" width="200"> </el-table-column>

        <el-table-column
          label="分类"
          prop="class1Name"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column
          label="专区"
          prop="class2Name"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column
          label="发布人"
          prop="createBy"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column
          label="发布时间"
          prop="createTime"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column
          label="审核人"
          prop="auditBy"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column
          label="审核时间"
          prop="auditTime"
          align="left"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column label="状态" width="200" prop="status">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagType(row.status)"></div>
              <span>{{ statusNameByCode(row.status) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 待审核：审核、查看 -->
            <template v-if="row.status === 0">
              <el-button
                v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU"
                link
                type="primary"
                @click="handleReview(row)"
                >审核</el-button
              >
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
            </template>
            <!-- 已发布：置顶/取消置顶、下架、查看 -->
            <template v-else-if="row.status === 1">
              <el-button
                v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU"
                link
                type="primary"
                @click="handleTopToggle(row)"
                >{{ row?.pinToTop == 1 ? '取消置顶' : '置顶' }}</el-button
              >
              <el-button
                v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU"
                link
                type="primary"
                @click="handleDelisted(row)"
                >下架</el-button
              >
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
            </template>
            <!-- 已下架：上架、查看、删除 -->
            <template v-else-if="row.status === 2">
              <el-button
                v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU"
                link
                type="primary"
                @click="handleRelease(row)"
                >上架</el-button
              >
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button
                v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU"
                link
                type="primary"
                @click="handleDelete(row)"
                >删除</el-button
              >
            </template>
            <!-- 未通过：查看、删除 -->
            <template v-else-if="row.status === 3">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button
                v-auth="SYSTEM_REPORT_BTN_MAP.RMRSU"
                link
                type="primary"
                @click="handleDelete(row)"
                >删除</el-button
              >
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="tableState.currentPage.value"
          v-model:page-size="tableState.pageSize.value"
          :total="tableState.total.value"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
        />
      </div>
    </el-card>

    <BatchDialog
      v-model:visible="batchDialogVisible"
      :type="batchType"
      :selection="selection"
      @confirm="tableMethods.getList"
    ></BatchDialog>

    <TipDialog
      v-model:visible="tipDialogVisible"
      :type="tipType"
      :data="viewEntity"
      @confirm="tableMethods.getList"
    ></TipDialog>

    <ReviewDialog
      v-model:visible="reviewDialogVisible"
      :data="reviewData"
      @confirm="tableMethods.getList"
    ></ReviewDialog>
  </div>
</template>

<script setup lang="ts">
import { h, ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useTable } from '@/hooks/useTable'
import { useUserStore } from '@/store'
import { appDialogConfirm } from '@/components'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import {
  batchDeleteCustomReport,
  deleteCustomReport,
  reportList,
  topReport
} from '@/api/reportManagement'
import { getSpecialZoneOptions } from '@/api/overview'
import type { ReportListParams } from '@/api/reportManagement/types'
import { ReportBatchType, TipType } from './constants'
import { useToggle } from '@/hooks/useToggle'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import BatchDialog from './components/BatchDialog.vue'
import TipDialog from './components/TipDialog.vue'
import ReviewDialog from './components/ReviewDialog.vue'
import { SYSTEM_REPORT_BTN_MAP } from '@/constants/btnPermMap'
import { hasPermission } from '@/utils/permission'
import { insertReportViewLog } from '@/api/reportViewLog/index'

defineOptions({
  name: 'report'
})

const tableRef = ref<any>()

// 搜索表单
const searchForm = reactive({
  searchKeyword: '',
  enabled: undefined as string | undefined
})

const router = useRouter()
const userStore = useUserStore()
const sceneAnalysisStore = useSceneAnalysisStore()
const [batchDialogVisible, handleBatchDialogVisible] = useToggle()

const tipType = ref<TipType>(TipType.Release)
const [tipDialogVisible, handleTipDialogVisible] = useToggle()

const reviewDialogVisible = ref(false)
const reviewData = ref<any>(null)

const statusOptions = computed(() => {
  return userStore.getDictItems('report_release_staus')
})

// 将状态转为中文
const statusNameByCode = (code: string) => {
  const item = statusOptions.value.find((item: any) => item.value === code?.toString())
  return item ? item.text : ''
}

// 分类
const zoneOptions = ref<any[]>()
// 专区
const specialZoneOptions = computed(() => {
  if (!formData.value.firstLevelZoneIds?.length || !zoneOptions.value?.length) return []
  return zoneOptions.value
    .filter(zone => formData.value.firstLevelZoneIds?.includes(zone.id))
    .flatMap(zone => zone.children || [])
})

/**
 * @description: 获取专区下拉树
 * @return {*}
 */
const getZoneOptions = async () => {
  try {
    const res = await getSpecialZoneOptions({})
    if (res.success) {
      zoneOptions.value = res.result
    } else {
      zoneOptions.value = []
    }
  } catch (error: any) {
    ElMessage.error(error.message)
  }
}

// 使用 useTable hook
const { formData, tableMethods, tableState, viewEntity, selection } = useTable<ReportListParams>({
  immediate: true,
  initialFormData: {
    firstLevelZoneIds: undefined,
    specialTypeIds: undefined,
    statuses: undefined,
    reportName: undefined
  },
  fetchDataApi: async () => {
    const response = await reportList({
      pageSize: tableState.pageSize.value,
      pageNum: tableState.currentPage.value,
      ...formData.value,
      isAuditBy: hasPermission(SYSTEM_REPORT_BTN_MAP.RMRSU)
    })

    if (response.success) {
      return {
        list: response.result.list || [],
        total: response.result.total || 0
      }
    } else {
      ElMessage.error(response.message || '获取角色列表失败')
      return { list: [], total: 0 }
    }
  }
})

const batchType = ref<ReportBatchType>(ReportBatchType.Review)

// 批量操作按钮显示控制
const canShowReview = computed(() => {
  if (selection.value.length === 0) return true
  return selection.value.every((item: any) => item.status === 0)
})

const canShowRelease = computed(() => {
  if (selection.value.length === 0) return false
  return selection.value.every((item: any) => item.status === 2)
})

const canShowDelisted = computed(() => {
  if (selection.value.length === 0) return false
  return selection.value.every((item: any) => item.status === 1)
})

const canShowMove = computed(() => {
  if (selection.value.length === 0) return false
  return selection.value.every((item: any) => item.status !== 0)
})

const canShowBatchDelete = computed(() => {
  if (selection.value.length === 0) return false
  return selection.value.every((item: any) => [2, 3].includes(item.status))
})

const adjustPageAfterDelete = (deletedCount: number) => {
  const currentPage = tableState.currentPage.value
  const dataLen = tableState.dataList.value?.length || 0

  // 删除后当前页会变成空页时，自动回退一页，避免出现空表格
  if (currentPage > 1 && deletedCount >= dataLen) {
    tableState.currentPage.value = currentPage - 1
  }
}

const clearSelection = () => {
  tableRef.value?.clearSelection?.()
}

const createConfirmContent = (text: string) =>
  h('div', { class: 'flex items-center' }, [
    h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
    h('span', { class: 'ml-8' }, text)
  ])

// 单个操作方法
const handleReview = (row: any) => {
  reviewData.value = row
  reviewDialogVisible.value = true
}

const handleTopToggle = async (row: any) => {
  if (!row?.id) {
    ElMessage.warning('缺少报告ID，无法执行置顶操作')
    return
  }

  // pinToTop 入参：1 置顶，0 取消置顶
  const nextPinToTop: 0 | 1 = row?.pinToTop == 1 ? 0 : 1

  // 取消置顶会影响排序：使用 appDialogConfirm 二次确认，降低误触成本
  if (nextPinToTop === 0) {
    const name = row?.reportName
    const text = name
      ? `“${name}”取消置顶后将影响列表排序，是否继续？`
      : '取消置顶后将影响列表排序，是否继续？'

    try {
      await appDialogConfirm(createConfirmContent(text), '确认取消置顶', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        dialogAttrs: {
          width: '480px',
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
      })
    } catch {
      // 用户取消/关闭：不继续执行
      return
    }
  }

  const successText = nextPinToTop === 1 ? '置顶成功' : '取消置顶成功'
  const errorText = nextPinToTop === 1 ? '置顶失败' : '取消置顶失败'

  try {
    const res = await topReport({ id: row.id, pinToTop: nextPinToTop })
    if (res.success) {
      ElMessage.success(successText)
      tableMethods.getList()
    } else {
      ElMessage.error(res.message || errorText)
    }
  } catch (error: any) {
    ElMessage.error(error?.message || errorText)
  }
}

const handleRelease = (row: any) => {
  tipType.value = TipType.Release
  viewEntity.record = row
  handleTipDialogVisible(true)
}

const handleDelisted = (row: any) => {
  tipType.value = TipType.Delisted
  viewEntity.record = row
  handleTipDialogVisible(true)
}

const handleView = async (row: any) => {
  const reportName = typeof row?.reportName === 'string' ? row.reportName.trim() : undefined

  // 总是设置场景源数据（从 defaultCondition 或 dateCondition 中解析）
  await sceneAnalysisStore.setSceneOriginData({
    ...row,
    isDetail: true
  })

  if (row.reportUrl) {
    const reportId = row?.id || ''
    // 记录报告查看行为：不阻塞跳转，避免影响用户打开速度
    if (reportId) {
      void insertReportViewLog({ reportId }).catch(error => {
        console.error('新增报告查看记录失败:', error)
      })
    }
    router.push({
      path: row.reportUrl,
      query: {
        reportJudgeId: reportId,
        isBack: '1',
        from: '/system/report',
        reportName
      }
    })
  } else {
    ElMessage.warning('未找到对应页面')
  }
}

const handleDelete = async (row: any) => {
  if (!row?.id) {
    ElMessage.warning('缺少报告ID，无法执行删除操作')
    return
  }

  const name = row?.reportName
  const text = name ? `确定删除“${name}”吗？删除后不可恢复。` : '确定删除该报告吗？删除后不可恢复。'

  try {
    await appDialogConfirm(createConfirmContent(text), '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px',
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    })
  } catch {
    return
  }

  try {
    const res = await deleteCustomReport({ id: row.id })
    if (res.success) {
      ElMessage.success('删除成功')
      clearSelection()
      adjustPageAfterDelete(1)
      tableMethods.getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '删除失败')
  }
}

const handleBatchDelete = async () => {
  if (selection.value.length === 0) {
    ElMessage.warning('请选择报告')
    return
  }

  if (!selection.value.every((item: any) => [2, 3].includes(item.status))) {
    ElMessage.warning('请选择全部为已下架或未通过状态的报告')
    return
  }

  const ids = selection.value.map((item: any) => item.id).filter(Boolean)
  if (ids.length === 0) {
    ElMessage.warning('未获取到可删除的报告ID')
    return
  }

  const text = `确定删除选中的 ${ids.length} 条报告吗？删除后不可恢复。`

  try {
    await appDialogConfirm(createConfirmContent(text), '确认批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px',
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    })
  } catch {
    return
  }

  try {
    const res = await batchDeleteCustomReport({ ids })
    if (res.success) {
      ElMessage.success('删除成功')
      clearSelection()
      adjustPageAfterDelete(ids.length)
      tableMethods.getList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '删除失败')
  }
}

const handleBatchOperation = (_type: ReportBatchType) => {
  if (selection.value.length === 0) {
    ElMessage.warning('请选择报告')
    return
  }

  // 状态校验
  const statuses = selection.value.map((item: any) => item.status)

  switch (_type) {
    case ReportBatchType.Review:
      if (!statuses.every((status: number) => status === 0)) {
        ElMessage.warning('请选择全部为待审核状态的报告')
        return
      }
      break
    case ReportBatchType.Release:
      if (!statuses.every((status: number) => status === 2)) {
        ElMessage.warning('请选择全部为已下架状态的报告')
        return
      }
      break
    case ReportBatchType.Delisted:
      if (!statuses.every((status: number) => status === 1)) {
        ElMessage.warning('请选择全部为已发布状态的报告')
        return
      }
      break
    case ReportBatchType.Move:
      if (statuses.some((status: number) => status === 0)) {
        ElMessage.warning('请选择非待审核状态的报告')
        return
      }
      break
  }

  batchType.value = _type
  handleBatchDialogVisible(true)
}

/**
 * 获取状态标签类型
 */
const getStatusTagType = (status?: number) => {
  let color = ''
  //   [
  //     {
  //         "value": "0",
  //         "text": "待审核"
  //     },
  //     {
  //         "value": "1",
  //         "text": "已发布"
  //     },
  //     {
  //         "value": "2",
  //         "text": "已下架"
  //     },
  //     {
  //         "value": "3",
  //         "text": "未通过"
  //     }
  // ]
  switch (status) {
    case 0:
      color = '#FAB007' // 待审核
      break
    case 1:
      color = '#1677FF' // 已发布
      break
    case 2:
      color = '#C9CDD4' // 已下架
      break
    case 3:
      color = '#FF5959' // 未通过
      break
    default:
      color = 'red'
      break
  }
  return { backgroundColor: color }
}

/**
 * 处理分类变化
 */
const handleZoneChange = () => {
  formData.value.specialTypeIds = undefined
}

/**
 * 搜索角色
 */
const handleSearch = () => {
  tableState.currentPage.value = 1
  tableMethods.getList()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.searchKeyword = ''
  searchForm.enabled = undefined
  tableState.currentPage.value = 1
  tableMethods.getList()
}

const init = () => {
  getZoneOptions()
}

init()
</script>

<style lang="scss" scoped>
.role-management {
  height: 100%;
  display: flex;
  flex-direction: column;

  .table-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-table .el-table__cell) {
      height: 55px;
      padding: 0 !important;
    }

    :deep(.el-table--fit .el-table__inner-wrapper:before) {
      width: 0 !important;
    }

    :deep(.el-table__header) {
      .el-table__cell {
        color: #1d2129;
        font-weight: 600;
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
      }
    }

    .report-name {
      max-width: 160px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .role-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .role-icon {
        flex-shrink: 0;
      }

      .role-details {
        .role-name {
          font-weight: 600;
          color: #303133;
        }

        .role-code {
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
          font-family: monospace;
        }
      }
    }

    .status-icon {
      width: 6px;
      height: 6px;
      border-radius: 50%;
    }

    .pagination-wrapper {
      margin-top: 16px;
    }
  }
}
</style>
