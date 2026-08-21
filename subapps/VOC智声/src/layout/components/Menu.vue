<template>
  <!-- :collapse="isCollapse" -->
  <el-menu
    :default-active="activeMenu"
    :collapse="true"
    :unique-opened="true"
    class="sidebar-menu"
    @select="handleMenuSelect"
  >
    <template v-for="item in menuItems" :key="item.path">
      <template v-if="!item.meta?.hidden">
        <!-- 单级菜单（alwaysShow为true或在Sidebar模式下的场景分析） -->
        <el-menu-item
          v-if="item.meta?.alwaysShow || (isCollapse && item.name === 'sceneAnalysis')"
          :index="item.redirect || item.path"
        >
          <el-tooltip
            v-if="item.meta?.icon"
            effect="dark"
            :content="item.meta?.title"
            placement="right"
            popper-class="text-tooltip-light"
          >
            <span class="flex-center">
              <SvgIcon
                :name="item.meta.icon"
                width="24px"
                height="24px"
                :color="activeMenu === (item.redirect || item.path) ? '#1677FF' : '#929AA6'"
              />
            </span>
          </el-tooltip>
          <!-- <el-icon v-if="item.meta?.icon" :size="24" class="menu-icon">
            <component :is="item.meta.icon" />
          </el-icon> -->
          <span>{{ item.meta?.title }}</span>
        </el-menu-item>

        <!-- 有子菜单的情况（但不在Sidebar模式下的场景分析） -->
        <el-sub-menu
          v-else-if="
            item.children &&
            item.children.length > 0 &&
            !(isCollapse && item.name === 'sceneAnalysis')
          "
          :index="item.path"
        >
          <template #title>
            <SvgIcon
              v-if="item.meta?.icon"
              :name="item.meta.icon"
              width="24px"
              height="24px"
              :color="activeMenu.startsWith(item.path) ? '#1677FF' : '#929AA6'"
            />
            <!-- <el-icon v-if="item.meta?.icon" :size="24" class="menu-icon">
              <component :is="item.meta.icon" />
            </el-icon> -->
            <span>{{ item.meta?.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="child.path"
            v-show="!child.meta?.hidden"
          >
            <SvgIcon
              v-if="item.meta?.icon"
              :name="item.meta.icon"
              width="24px"
              height="24px"
              :color="activeMenu === child.path ? '#1677FF' : '#929AA6'"
              class="mr-8"
            />
            <!-- <el-icon v-if="child.meta?.icon" :size="24" class="menu-icon">
              <component :is="child.meta.icon" />
            </el-icon> -->
            <span>{{ child.meta?.title }}</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 无子菜单的情况 -->
        <el-menu-item v-else :index="item.path">
          <el-tooltip
            v-if="item.meta?.icon"
            effect="dark"
            :content="item.meta?.title"
            placement="right"
            popper-class="text-tooltip-light"
          >
            <span class="flex-center">
              <SvgIcon
                :name="item.meta.icon"
                width="24px"
                height="24px"
                :color="activeMenu === item.path ? '#1677FF' : '#929AA6'"
              />
            </span>
          </el-tooltip>
          <!-- <el-icon v-if="item.meta?.icon" :size="24" class="menu-icon">
            <component :is="item.meta.icon" />
          </el-icon> -->
          <span>{{ item.meta?.title }}</span>
        </el-menu-item>
      </template>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'

defineOptions({
  name: 'SidebarMenu'
})

const route = useRoute()
const routerInstance = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const menuOrder = [
  'vocView',
  'leaderOverview',
  'sceneAnalysis',
  'selfServiceAnalysis',
  'CustomerDirectEngage',
  'system'
]

// 折叠菜单与展开菜单共用固定信息架构顺序。
const menuItems = computed(() =>
  [...(userStore.menuList || [])].sort((first: any, second: any) => {
    const firstIndex = menuOrder.indexOf(String(first.name))
    const secondIndex = menuOrder.indexOf(String(second.name))
    return (
      (firstIndex < 0 ? menuOrder.length : firstIndex) -
      (secondIndex < 0 ? menuOrder.length : secondIndex)
    )
  })
)

const isCollapse = computed(() => appStore.isCollapse)
const activeMenu = computed(() => route.path)

const handleMenuSelect = (index: string) => {
  // 检查是否为外链菜单
  const menuItem = findMenuByPath(index, menuItems.value)
  if (menuItem?.meta?.isExternal && menuItem.meta.externalUrl) {
    window.open(menuItem.meta.externalUrl, '_blank')
    return
  }

  // 对于自助分析菜单，跳转到根因分析
  if (index === '/selfService' || menuItem?.name === 'selfServiceAnalysis') {
    const selfServiceMenu = menuItems.value.find((item: any) => item.name === 'selfServiceAnalysis')
    if (selfServiceMenu?.children) {
      const rootCauseMenu = selfServiceMenu.children.find(
        (child: any) => child.path === '/rootCause' || child.name === 'rootCause'
      )
      if (rootCauseMenu) {
        routerInstance.push(rootCauseMenu.path)
        return
      }
    }
    // 如果没有找到根因分析，则不跳转（避免404）
    return
  }

  routerInstance.push(index)
}

// 递归查找菜单项
const findMenuByPath = (path: string, menus: any[]): any => {
  for (const menu of menus) {
    if (menu.path === path) return menu
    if (menu.children) {
      const found = findMenuByPath(path, menu.children)
      if (found) return found
    }
  }
  return null
}
</script>

<style lang="scss" scoped>
.sidebar-menu {
  border: none;
  background: transparent;
  margin: 0 auto;
  padding: 9px;
  // collapse状态
  &.el-menu--collapse {
    .el-menu-item {
      width: 48px;
      height: 48px;
      background: rgba(255, 255, 255, 0.5);
      border-radius: 9px;
      padding: 0 10px;
      text-align: center;
      display: flex;
      justify-content: center;
      align-items: center;

      &.el-menu-item:not(:first-child) {
        margin-top: 16px;
      }
      &:hover {
        background-color: #ecf5ff;
      }
    }

    .el-sub-menu {
      width: 48px;
      height: 48px;
      background: rgba(255, 255, 255, 0.5);
      border-radius: 9px 9px 9px 9px;
      padding: 0 10px;
      text-align: center;
      display: flex;
      justify-content: center;
      align-items: center;

      &.el-sub-menu:not(:first-child) {
        margin-top: 16px;
      }
      &:hover {
      }
      &.is-active {
        background: #eaf3ff;
        border-radius: 9px;
      }
    }

    :deep(.el-sub-menu__title) {
      padding: 0 12px;
      height: 48px;
      border-radius: 9px;
    }
  }
  // 默认状态
  :deep(.el-menu-item) {
    background: rgba(255, 255, 255, 0.5);
    border-radius: 9px;
    padding: 0;

    &:hover {
      background-color: #ecf5ff;
    }

    &.is-active {
      background: #eaf3ff;
      border-radius: 9px;
    }
    &.el-menu-item:not(:first-child) {
      margin-top: 16px;
    }
  }
  // 默认状态
  :deep(.el-sub-menu__title) {
    &:hover {
      border-radius: 9px;
    }
  }
  // 默认状态
  :deep(.el-sub-menu) {
    background: rgba(255, 255, 255, 0.5);
    border-radius: 9px;
    &:hover {
      border-radius: 9px;
    }
    &.el-sub-menu:not(:first-child) {
      margin-top: 16px;
    }
    .el-menu {
      .el-menu-item {
        &.is-active {
          border-radius: 0;
        }
        &:hover {
          border-radius: 0;
        }
      }
    }
  }
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}
</style>
