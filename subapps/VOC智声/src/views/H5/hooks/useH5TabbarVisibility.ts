import { computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionsStore, useTaskEventStore } from '@h5/store'

/**
 * H5 底部 Tabbar 显隐控制逻辑
 * - 统一根据当前路由 meta 计算是否需要展示 Tabbar
 * - 按任务页特殊条件触发关联事件加载
 *
 * 注意：
 * 1）基础逻辑依赖路由 meta.showTabBar 控制是否允许展示 Tabbar
 * 2）具体 Tab 项是否展示由 H5TabBar 组件按菜单权限过滤
 * 3）任务页依赖 H5 任务事件 Store 中的 hasUserEvents 标记，仅当当前登录人存在关联事件时才展示底栏
 */

export function useH5TabbarVisibility() {
  const route = useRoute()
  const permissionsStore = usePermissionsStore()
  const taskEventStore = useTaskEventStore()

  // 当前登录人是否存在关联事件（来自任务事件 Store）
  const hasUserEvents = computed(() => taskEventStore.hasUserEvents)

  // 基础显隐逻辑：
  // 仅当路由 meta 显式开启 showTabBar（meta.showTabBar === true）时才有可能展示
  const baseVisible = computed(() => {
    // 仅当路由 meta 明确标记需要展示时才显示，未配置时默认不展示
    const current = route.meta?.showTabBar
    if (typeof current === 'boolean') {
      return current
    }

    return false
  })

  // 路由级显隐逻辑：
  // - 路由允许展示 Tabbar
  // - 任务页额外要求当前登录人存在关联事件
  const routeVisible = computed(() => {
    if (!baseVisible.value) return false
    if (route.name === 'H5TaskEvent') {
      return hasUserEvents.value
    }
    return true
  })

  // 根据当前条件尝试拉取一次关联事件信息，由任务事件 Store 统一管理请求与缓存
  const ensureUserEventsLoaded = async () => {
    // 路由未开启 Tabbar 或无任务权限时，不需要拉取事件信息
    if (!baseVisible.value) return
    if (!permissionsStore.hasMenuPermission('H5TaskEvent')) return

    await taskEventStore.fetchUserEvents()
  }

  // 首次挂载时，如果路由允许展示 Tabbar，则尝试拉取一次事件信息
  onMounted(() => {
    ensureUserEventsLoaded()
  })

  // 路由 meta.showTabBar 或任务权限变化时，根据需要触发事件信息拉取
  watch(
    [baseVisible, () => permissionsStore.hasMenuPermission('H5TaskEvent')],
    () => {
      ensureUserEventsLoaded()
    },
    { immediate: true }
  )

  return {
    visible: routeVisible
  }
}
