<template>
  <!-- H5 底部导航：仅当路由允许且存在至少两个可切换入口时展示 -->
  <van-tabbar
    v-if="tabbarVisible"
    route
    fixed
    safe-area-inset-bottom
    style="position: fixed !important"
  >
    <van-tabbar-item
      v-for="item in visibleTabItems"
      :key="item.key"
      :replace="!!item.to"
      :to="item.to"
      @click="handleTabItemClick(item)"
    >
      <!-- 使用 SvgIcon 自定义图标，统一走配置化数据 -->
      <template #icon="{ active }">
        <SvgIcon
          :name="item.icon"
          width="22px"
          height="22px"
          :color="active ? '#1677FF' : '#5F6A7A'"
        />
      </template>
      {{ item.label }}
    </van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { useH5TabbarVisibility } from '@h5/hooks/useH5TabbarVisibility'
import { computed, onBeforeUnmount, watch } from 'vue'
import { usePermissionsStore, useTaskEventStore } from '@h5/store'
import { showToast } from 'vant'
import { isIndataUrl, setPendingIndataReturnFlag } from '@h5/utils/indataReturn'

type TabItemKey = 'home' | 'dataSquare' | 'task' | 'canswer'
type H5MenuPermissionKey = 'H5Home' | 'H5DataPlaza' | 'H5TaskEvent' | 'H5Canswer'

interface TabItemConfig {
  key: TabItemKey
  label: string
  to?: string
  icon: string
  permissionKey?: H5MenuPermissionKey
  alwaysVisible?: boolean
  requiresUserEvents?: boolean
  action?: () => void | Promise<void>
}

// 底栏至少要有两个入口才展示；只有首页时不展示，避免单入口导航占用页面空间
const MIN_TABBAR_ITEM_COUNT = 2

const permissionsStore = usePermissionsStore()
const taskEventStore = useTaskEventStore()

/**
 * 执行问数菜单的授权与外链跳转。
 * 与 CanswerLogin 悬浮入口保持同一套授权、返回标记和跳转逻辑。
 */
const handleCanswerTabClick = async () => {
  const res = (await permissionsStore.handleCanswerAuth()) as any

  if (res?.success) {
    if (res?.result) {
      const link = res.result

      if (isIndataUrl(link)) {
        setPendingIndataReturnFlag(link)
      }

      window.location.href = link
    } else {
      showToast('抱歉，您暂无此菜单访问权限，请联系系统管理员配置权限，感谢配合。')
    }
  } else {
    console.log('handleCanswerTabClick-->error--->', res)
  }
}

/**
 * 处理底栏点击事件；普通路由 Tab 交由 Vant route 模式处理。
 * @param item 当前点击的底部导航配置项
 */
const handleTabItemClick = (item: TabItemConfig) => {
  if (!item.action) return

  item.action()
}

// 底部导航配置，后续如需新增 Tab，优先维护权限和附加条件
const tabItems: TabItemConfig[] = [
  {
    key: 'home',
    label: '首页',
    to: '/h5/home',
    // 对应 src/assets/svg/h5/tabbar-home.svg，符号 ID 为 icon-h5-tabbar-home
    icon: 'h5-tabbar-home',
    permissionKey: 'H5Home',
    alwaysVisible: true
  },
  {
    key: 'dataSquare',
    label: '广场',
    to: '/h5/dataSquare',
    // 对应 src/assets/svg/h5/tabbar-square.svg，符号 ID 为 icon-h5-tabbar-square
    icon: 'h5-tabbar-square',
    permissionKey: 'H5DataPlaza'
  },
  {
    key: 'canswer',
    label: '问数',
    // 对应 src/assets/svg/h5/tabbar-square.svg，符号 ID 为 icon-h5-tabbar-square
    icon: 'h5-tabbar-square',
    permissionKey: 'H5Canswer',
    action: handleCanswerTabClick
  },
  {
    key: 'task',
    label: '任务',
    to: '/h5/task',
    // 对应 src/assets/svg/h5/tabbar-task.svg，符号 ID 为 icon-h5-tabbar-task
    icon: 'h5-tabbar-task',
    permissionKey: 'H5TaskEvent',
    requiresUserEvents: true
  }
]

/**
 * 判断单个 Tab 是否满足展示条件。
 * @param item 底部导航配置项
 * @returns 当前 Tab 是否可展示
 */
const isTabItemVisible = (item: TabItemConfig): boolean => {
  if (
    !item.alwaysVisible &&
    item.permissionKey &&
    !permissionsStore.hasMenuPermission(item.permissionKey)
  ) {
    return false
  }

  if (item.requiresUserEvents) {
    return taskEventStore.hasUserEvents
  }

  return true
}

/**
 * 根据配置、菜单权限和业务附加条件过滤 Tab。
 */
const visibleTabItems = computed(() => {
  return tabItems.filter(item => isTabItemVisible(item))
})

// 统一通过 Hook 管理路由级显隐逻辑和任务事件加载
const { visible } = useH5TabbarVisibility()

// 路由允许且存在足够的可切换入口时才展示底栏
const tabbarVisible = computed(
  () => visible.value && visibleTabItems.value.length >= MIN_TABBAR_ITEM_COUNT
)

watch(
  tabbarVisible,
  value => {
    const body = document.body
    if (!body) return

    if (value) {
      body.classList.add('h5-tabbar-visible')
    } else {
      body.classList.remove('h5-tabbar-visible')
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  // 组件卸载时兜底移除标记，避免切回 PC 页面后样式残留
  document.body.classList.remove('h5-tabbar-visible')
})
</script>

<style scoped lang="scss">
/* 暂时使用 Vant 默认样式，如需个性化再统一调整 */
</style>
