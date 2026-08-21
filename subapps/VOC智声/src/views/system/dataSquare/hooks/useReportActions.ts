import { h } from 'vue'
import { ElMessage } from 'element-plus'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import {
  batchDeleteDataPlazaReport,
  batchPublishDataPlazaReport,
  batchUnpublishDataPlazaReport,
  copyDataPlazaReport,
  deleteDataPlazaReport,
  updateDataPlazaReportPin,
  updateDataPlazaReportPublishStatus
} from '@/api/dataPlaza'
import type { DataPlazaReportItem } from '@/api/dataPlaza/types'

interface OpenDataSquareActionConfirmOptions {
  title: string
  actionText: string
  targetText?: string
}

interface UseReportActionsOptions {
  refreshList: () => Promise<void>
  adjustPageAfterDelete?: (deletedCount: number) => void
}

interface OpenDataSquareBatchCountConfirmOptions {
  title: string
  label: string
  count: number
}

const confirmContainerStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  minHeight: '28px',
  color: '#4e5969',
  fontSize: '14px',
  lineHeight: '22px'
}

const confirmIconStyle = {
  flexShrink: 0 as const
}

const confirmTextStyle = {
  color: '#4e5969'
}

const batchCountLabelStyle = {
  fontSize: '14px',
  lineHeight: '22px',
  color: '#4e5969'
}

const batchCountValueStyle = {
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  minWidth: '40px',
  height: '28px',
  padding: '0 12px',
  marginLeft: '12px',
  borderRadius: '4px',
  background: '#eaf3ff',
  color: '#1677ff',
  fontSize: '14px',
  lineHeight: '22px'
}

/**
 * 构造数据广场操作确认弹窗内容，统一图标、布局与提醒文案。
 * @param actionText 操作文案
 * @param targetText 操作对象文案
 * @returns 弹窗内容节点
 */
const createDataSquareActionConfirmContent = (actionText: string, targetText: string) => {
  return h('div', { style: confirmContainerStyle }, [
    h(SvgIcon, {
      name: 'info-circle-filled',
      width: '20px',
      height: '20px',
      color: '#1677ff',
      style: confirmIconStyle
    }),
    h('span', { style: confirmTextStyle }, `是否确认${actionText}${targetText}?`)
  ])
}

/**
 * 打开数据广场统一操作确认弹窗。
 * @param options 弹窗配置
 */
export const openDataSquareActionConfirm = async (options: OpenDataSquareActionConfirmOptions) => {
  const { title, actionText, targetText = '当前报告' } = options

  await appDialogConfirm(createDataSquareActionConfirmContent(actionText, targetText), title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    dialogAttrs: {
      width: '480px',
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  })
}

/**
 * 打开数据广场批量操作计数确认弹窗。
 * @param options 弹窗配置
 */
export const openDataSquareBatchCountConfirm = async (
  options: OpenDataSquareBatchCountConfirmOptions
) => {
  const { title, label, count } = options

  await appDialogConfirm(
    h('div', { style: confirmContainerStyle }, [
      h(SvgIcon, {
        name: 'info-circle-filled',
        width: '20px',
        height: '20px',
        color: '#1677ff',
        style: confirmIconStyle
      }),
      h('span', { style: batchCountLabelStyle }, label),
      h('span', { style: batchCountValueStyle }, String(count))
    ]),
    title,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px',
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
    }
  )
}

/**
 * 数据广场报告操作集合，统一处理确认弹窗、接口调用与成功后的列表刷新。
 * @param options 刷新列表与删除后分页修正配置
 * @returns 报告操作方法
 */
export const useReportActions = (options: UseReportActionsOptions) => {
  const { refreshList, adjustPageAfterDelete } = options

  /**
   * 判断当前报告是否已上架。
   * @param row 报告行数据
   * @returns 是否已上架
   */
  const isPublishedReport = (row: DataPlazaReportItem) => row.publishStatus === 1

  /**
   * 获取当前报告操作列建议宽度，避免不同状态下按钮换行过早。
   * @param row 报告行数据
   * @returns 操作列宽度
   */
  const getActionColumnWidth = (row: DataPlazaReportItem) => {
    return isPublishedReport(row) ? 340 : 390
  }

  /**
   * 提取批量操作可用的报告 ID。
   * @param rows 选中报告列表
   * @returns 报告 ID 列表
   */
  const getBatchIds = (rows: DataPlazaReportItem[]) => {
    return rows.map(item => item.id).filter(Boolean)
  }

  /**
   * 处理报告置顶或取消置顶。
   * @param row 当前报告行
   */
  const handleTogglePin = async (row: DataPlazaReportItem) => {
    if (!row.id) {
      ElMessage.warning('缺少报告ID，无法执行置顶操作')
      return
    }

    const nextPinned: 0 | 1 = row.isPinned == 1 ? 0 : 1
    try {
      const response = await updateDataPlazaReportPin({
        id: row.id,
        isPinned: nextPinned
      })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return
      }

      ElMessage.success(response.message || '操作成功')
      await refreshList()
    } catch (error) {
      console.error('更新数据广场报告置顶状态失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
    }
  }

  /**
   * 处理报告发布或下架。
   * @param row 当前报告行
   */
  const handleTogglePublishStatus = async (row: DataPlazaReportItem) => {
    if (!row.id) {
      ElMessage.warning('缺少报告ID，无法执行发布状态操作')
      return
    }

    const nextPublishStatus: 0 | 1 = row.publishStatus === 1 ? 0 : 1
    const actionText = nextPublishStatus === 1 ? '发布' : '下架'

    try {
      await openDataSquareActionConfirm({
        title: `${actionText}报告`,
        actionText
      })
    } catch {
      return
    }

    try {
      const response = await updateDataPlazaReportPublishStatus({
        id: row.id,
        publishStatus: nextPublishStatus
      })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return
      }

      ElMessage.success(response.message || '操作成功')
      await refreshList()
    } catch (error) {
      console.error('更新数据广场报告发布状态失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
    }
  }

  /**
   * 复制报告。
   * @param row 当前报告行
   */
  const handleCopyReport = async (row: DataPlazaReportItem) => {
    if (!row.id) {
      ElMessage.warning('缺少报告ID，无法执行复制操作')
      return false
    }

    try {
      const response = await copyDataPlazaReport({ id: row.id })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return false
      }

      ElMessage.success(response.message || '操作成功')
      await refreshList()
      return true
    } catch (error) {
      console.error('复制数据广场报告失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
      return false
    }
  }

  /**
   * 删除报告，仅允许删除已下架状态。
   * @param row 当前报告行
   */
  const handleDeleteReport = async (row: DataPlazaReportItem) => {
    if (!row.id) {
      ElMessage.warning('缺少报告ID，无法执行删除操作')
      return
    }

    if (isPublishedReport(row)) {
      ElMessage.warning('已上架报告不允许删除')
      return
    }

    try {
      await openDataSquareActionConfirm({
        title: '删除报告',
        actionText: '删除'
      })
    } catch {
      return
    }

    try {
      const response = await deleteDataPlazaReport({ id: row.id })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return
      }

      ElMessage.success(response.message || '操作成功')
      adjustPageAfterDelete?.(1)
      await refreshList()
    } catch (error) {
      console.error('删除数据广场报告失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
    }
  }

  /**
   * 批量发布报告。
   * @param rows 当前选中报告
   */
  const handleBatchPublish = async (rows: DataPlazaReportItem[]) => {
    const ids = getBatchIds(rows)
    if (!ids.length) {
      ElMessage.warning('请选择报告')
      return false
    }

    try {
      await openDataSquareBatchCountConfirm({
        title: '批量发布',
        label: '发布条数',
        count: ids.length
      })
    } catch {
      return false
    }

    try {
      const response = await batchPublishDataPlazaReport({ ids })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return false
      }

      ElMessage.success(response.message || '操作成功')
      await refreshList()
      return true
    } catch (error) {
      console.error('批量发布数据广场报告失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
      return false
    }
  }

  /**
   * 批量下架报告。
   * @param rows 当前选中报告
   */
  const handleBatchUnpublish = async (rows: DataPlazaReportItem[]) => {
    const ids = getBatchIds(rows)
    if (!ids.length) {
      ElMessage.warning('请选择报告')
      return false
    }

    try {
      await openDataSquareBatchCountConfirm({
        title: '批量下架',
        label: '下架条数',
        count: ids.length
      })
    } catch {
      return false
    }

    try {
      const response = await batchUnpublishDataPlazaReport({ ids })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return false
      }

      ElMessage.success(response.message || '操作成功')
      await refreshList()
      return true
    } catch (error) {
      console.error('批量下架数据广场报告失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
      return false
    }
  }

  /**
   * 批量删除报告，仅支持全部为已下架状态。
   * @param rows 当前选中报告
   */
  const handleBatchDelete = async (rows: DataPlazaReportItem[]) => {
    const ids = getBatchIds(rows)
    if (!ids.length) {
      ElMessage.warning('请选择报告')
      return false
    }

    try {
      await openDataSquareBatchCountConfirm({
        title: '批量删除',
        label: '删除条数',
        count: ids.length
      })
    } catch {
      return false
    }

    try {
      const response = await batchDeleteDataPlazaReport({ ids })

      if (!response.success) {
        ElMessage.error(response.message || '操作失败')
        return false
      }

      ElMessage.success(response.message || '操作成功')
      adjustPageAfterDelete?.(ids.length)
      await refreshList()
      return true
    } catch (error) {
      console.error('批量删除数据广场报告失败:', error)
      // ElMessage.error(error instanceof Error ? error.message : '操作失败')
      return false
    }
  }

  return {
    isPublishedReport,
    getActionColumnWidth,
    handleTogglePin,
    handleTogglePublishStatus,
    handleCopyReport,
    handleDeleteReport,
    handleBatchPublish,
    handleBatchUnpublish,
    handleBatchDelete
  }
}
