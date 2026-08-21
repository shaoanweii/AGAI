import { onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCustomReportDetail } from '@/api/sceneAnalysis'
import { insertReportViewLog } from '@/api/reportViewLog'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'

/**
 * 归一化路由 query 参数，兼容数组和空值。
 * @param value 路由 query 参数
 * @returns 单个有效字符串
 */
const normalizeQueryValue = (value: unknown): string => {
  if (Array.isArray(value)) {
    return normalizeQueryValue(value[0])
  }

  return typeof value === 'string' ? value.trim() : ''
}

/**
 * 在报告业务页面挂载前恢复外部直链携带的报告上下文。
 * @returns 页面挂载状态及重试方法
 */
export const useSceneReportDetailBootstrap = () => {
  const route = useRoute()
  const sceneAnalysisStore = useSceneAnalysisStore()
  const ready = ref(true)
  const loading = ref(false)
  const errorMessage = ref('')
  let requestSerial = 0

  /** 根据当前路由加载报告详情，并忽略路由切换后的过期响应。 */
  const initialize = async () => {
    const currentSerial = ++requestSerial
    const supportsReportDetail = route.meta.sceneReportDetail === true
    const reportId = normalizeQueryValue(route.query.reportJudgeId)

    if (!supportsReportDetail || !reportId) {
      loading.value = false
      errorMessage.value = ''
      ready.value = true
      return
    }

    if (
      sceneAnalysisStore.sceneOriginData.isDetail &&
      sceneAnalysisStore.sceneOriginData.reportId === reportId &&
      sceneAnalysisStore.sceneOriginData.hasValidCondition
    ) {
      loading.value = false
      errorMessage.value = ''
      ready.value = true
      return
    }

    ready.value = false
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await getCustomReportDetail({ id: reportId })
      if (currentSerial !== requestSerial) return

      if (!response.success || !response.result) {
        throw new Error(response.message || '报告不存在或当前账号无权查看')
      }

      const routeReportName = normalizeQueryValue(route.query.reportName)
      await sceneAnalysisStore.initializeSceneReportDetail({
        ...response.result,
        id: reportId,
        reportName: response.result.reportName || routeReportName,
        isDetail: true
      })
      if (currentSerial !== requestSerial) return

      ready.value = true
      void insertReportViewLog({ reportId }).catch(error => {
        console.error('新增报告查看记录失败:', error)
      })
    } catch (error) {
      if (currentSerial !== requestSerial) return

      console.error('初始化报告直链失败:', error)
      errorMessage.value = error instanceof Error ? error.message : '报告加载失败，请稍后重试'
    } finally {
      if (currentSerial === requestSerial) {
        loading.value = false
      }
    }
  }

  watch(
    () => [route.fullPath, route.meta.sceneReportDetail],
    () => {
      void initialize()
    },
    { immediate: true }
  )

  onBeforeUnmount(() => {
    requestSerial += 1
  })

  return {
    reportDetailReady: ready,
    reportDetailLoading: loading,
    reportDetailError: errorMessage,
    retryReportDetail: initialize
  }
}
