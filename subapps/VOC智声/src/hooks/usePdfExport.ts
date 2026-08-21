import { ElMessage } from 'element-plus'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useQueryStore } from '@/store/modules/query'
import { useLoading } from '@/hooks/useLoading'
import { getPdfExportErrorMessage } from '@/utils/pdfExport/error'
import { exportPagePdf, isPdfExportRouteName, shouldShowPdfExportButton } from '@/utils/pdfExport'

export interface UsePdfExportOptions {
  trigger: 'layoutHeader' | 'sceneHeader'
  getTitle: () => string
}

/**
 * 获取导出文件名使用的时间范围。
 * 场景分析页的筛选组件会把真实查询条件写入 commonQueryParams，
 * 领导页仍以顶部日期选择器维护的 currentQueryParams 为准。
 */
const resolveExportDateRange = (
  trigger: UsePdfExportOptions['trigger'],
  queryStore: ReturnType<typeof useQueryStore>
) => {
  if (trigger === 'sceneHeader') {
    return {
      startDate: queryStore.commonQueryParams.startDate || queryStore.currentQueryParams.startDate,
      endDate: queryStore.commonQueryParams.endDate || queryStore.currentQueryParams.endDate
    }
  }

  return {
    startDate: queryStore.currentQueryParams.startDate || queryStore.commonQueryParams.startDate,
    endDate: queryStore.currentQueryParams.endDate || queryStore.commonQueryParams.endDate
  }
}

/**
 * 统一封装页面 PDF 导出状态与触发逻辑。
 * - 避免多个 Header 各自维护 loading 与导出参数
 * - 后续若调整导出链路，只需要改这一处
 */
export const usePdfExport = (options: UsePdfExportOptions) => {
  const route = useRoute()
  const queryStore = useQueryStore()
  const { showLoading, hideLoading } = useLoading()
  const exporting = ref(false)

  const canExportCurrentPage = computed(() =>
    shouldShowPdfExportButton(route.name, options.trigger)
  )

  /**
   * 触发当前页面 PDF 导出，并在入口处统一拦截重复点击和不支持导出的路由。
   */
  const handleExportPdf = async () => {
    if (exporting.value || !canExportCurrentPage.value || !isPdfExportRouteName(route.name)) {
      return
    }

    exporting.value = true

    try {
      showLoading({
        text: 'PDF导出中，请稍候...',
        background: 'rgba(31, 39, 51, 0.45)',
        fullscreen: true,
        lock: true,
        customClass: 'loading-white-text'
      })

      const { startDate, endDate } = resolveExportDateRange(options.trigger, queryStore)

      await exportPagePdf({
        routeName: route.name,
        title: options.getTitle(),
        startDate,
        endDate
      })
    } catch (error) {
      console.error('导出PDF失败:', error)
      ElMessage.error(getPdfExportErrorMessage(error))
    } finally {
      hideLoading()
      exporting.value = false
    }
  }

  return {
    exporting,
    canExportCurrentPage,
    handleExportPdf
  }
}
