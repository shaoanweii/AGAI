// H5 布局级通用逻辑
// 复用 useH5LayoutSimple 的基础适配逻辑，在此基础上添加权限初始化和访问时长统计
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionsStore } from '@h5/store'
import { systemAccessDuration } from '@/utils/systemAccessDuration'
import { useH5LayoutSimple } from './useH5LayoutSimple'

/**
 * H5 布局级通用逻辑（完整版）
 * - 复用 useH5LayoutSimple 的基础适配逻辑（rem 适配、viewport 设置、body 类名）
 * - 负责初始化 H5 权限数据，避免各页面重复拉取
 * - 负责访问时长统计
 */
export function useH5Layout() {
  const route = useRoute()
  let accessDurationStarted = false

  // 先调用基础适配 Hook，处理 rem 适配、viewport 设置和 body 类名
  useH5LayoutSimple()

  /**
   * 判断当前是否为 PC 管理端 iframe 预览。
   * 预览页只需要 H5 基础适配，不参与真实 H5 访问统计和非必要权限初始化。
   */
  const isDataSquarePreview = () => {
    return route.name === 'H5DataSquareReportDetail' && route.query.preview === '1'
  }

  onMounted(async () => {
    if (typeof document === 'undefined') return
    if (isDataSquarePreview()) return

    // 初始化 H5 权限数据（品牌、时间维度、场景等），避免各页面重复拉取
    const permissionsStore = usePermissionsStore()
    try {
      await permissionsStore.initUserPermissions(true)

      // 进入 H5 系统即开始统计访问时长（不依赖页面交互）
      systemAccessDuration.start({
        device: 'h5',
        userId: permissionsStore.userId,
        username: permissionsStore.username
      })
      accessDurationStarted = true
    } catch (error) {
      // 仅做错误日志输出，具体兜底由各页面根据 Store 状态自行处理
      console.error('初始化 H5 权限信息失败:', error)
    }
  })

  onBeforeUnmount(() => {
    if (typeof document === 'undefined') return

    // 移除 h5-tabbar-visible 类名（这是 useH5Layout 特有的职责）
    // h5-active 类名由 useH5LayoutSimple 负责清理
    document.body.classList.remove('h5-tabbar-visible')

    if (!accessDurationStarted) return

    // 离开 H5 系统时结束统计（尽力上报 end）
    systemAccessDuration.stop('leave-h5')
  })
}
