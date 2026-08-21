<script setup lang="ts">
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { recordMenuVisit } from '@/utils/operationLog'

/**
 * 展开状态的菜单
 */
defineOptions({
  name: 'ExpandMenu'
})

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const menuOrder = [
  'vocView',
  'leaderOverview',
  'sceneAnalysis',
  'selfServiceAnalysis',
  'CustomerDirectEngage',
  'system'
]

// 本地菜单始终按产品信息架构展示，避免接口缓存中的旧 sort 覆盖目标顺序。
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
const activeMenu = computed(() => route.path)

/**
 * 记录菜单访问（不影响主流程）
 * - visitUrl：拼出目标路由对应的 hash 地址
 * - menuName：优先取点击菜单的 meta.title（与菜单展示一致），兜底取目标路由 meta.title
 */
const reportMenuVisit = (targetPath: string, menuItem: any) => {
  try {
    // 首页访问记录由 Layout 内的监听统一上报，避免与菜单点击重复调用
    if (userStore.isHomePath(targetPath)) {
      return
    }

    const resolved = router.resolve(targetPath)
    const frontRouting = resolved?.fullPath || targetPath || ''
    const lastMatched = resolved?.matched?.[resolved.matched.length - 1]
    const menuName = menuItem?.meta?.title || (lastMatched?.meta as any)?.title || ''
    // PC 端菜单ID统一从 userStore 的 path->id 映射中取，避免依赖路由 name/手动补齐
    const menuId = userStore.getMenuIdByPath(resolved?.path || frontRouting || targetPath) || ''

    const visitUrl = `${window.location.origin}${window.location.pathname}${window.location.search}#${frontRouting}`

    recordMenuVisit({
      visitUrl,
      frontRouting,
      menuName,
      menuId
    }).catch(() => void 0)
  } catch (error) {
    console.warn('菜单访问记录失败:', error)
  }
}

// 菜单点击处理
const handleMenuClick = (menuItem: any) => {
  // 一级菜单外链无需处理
  if (menuItem.name === 'linkUrl') {
    return
  }
  if (menuItem.meta?.isExternal && menuItem.meta.externalUrl) {
    window.open(menuItem.meta.externalUrl, '_blank')
    return
  }

  // 对于 alwaysShow 的菜单，直接跳转到 redirect 或第一个子菜单
  if (menuItem.meta?.alwaysShow) {
    const targetPath = menuItem.redirect || menuItem.children?.[0]?.path || menuItem.path
    reportMenuVisit(targetPath, menuItem)
    router.push(targetPath)
  } else if (menuItem.children && menuItem.children.length > 0) {
    // 对于场景分析，直接跳转到主页面
    if (menuItem.name === 'sceneAnalysis') {
      reportMenuVisit('/scene/analysis', menuItem)
      router.push('/scene/analysis')
    } else if (menuItem.name === 'selfServiceAnalysis') {
      // 对于自助分析，跳转到根因分析（第一个非外链子菜单）
      const rootCauseMenu = menuItem.children.find(
        (child: any) => child.path === '/rootCause' || child.name === 'rootCause'
      )
      if (rootCauseMenu) {
        reportMenuVisit(rootCauseMenu.path, menuItem)
        router.push(rootCauseMenu.path)
      }
      // 如果没有找到根因分析，则不跳转（避免404）
    } else {
      // 其他有子菜单的情况，跳转到第一个子菜单
      reportMenuVisit(menuItem.children[0].path, menuItem)
      router.push(menuItem.children[0].path)
    }
  } else {
    reportMenuVisit(menuItem.path, menuItem)
    router.push(menuItem.path)
  }
}

// 子菜单点击处理
const handleSubMenuClick = (subMenuItem: any) => {
  if (subMenuItem.meta?.isExternal && subMenuItem.meta.externalUrl) {
    window.open(subMenuItem.meta.externalUrl, '_blank')
    return
  }
  // 跳转canswer
  if (subMenuItem.path === 'canswer' && subMenuItem.name === 'linkUrl') {
    appStore.handleCanswerAuth()
    return
  }
  reportMenuVisit(subMenuItem.path, subMenuItem)
  router.push(subMenuItem.path)
}

// 精确匹配一级菜单激活状态（不做父子联动）
const isMenuActive = (menuItem: any) => {
  // 对于场景分析，当访问主页面时高亮
  if (menuItem.name === 'sceneAnalysis' && activeMenu.value === '/scene/analysis') {
    return true
  }
  // 对于自助分析，不进行高亮（避免在访问子菜单时高亮父菜单）
  if (menuItem.name === 'selfServiceAnalysis') {
    return false
  }
  // 其他情况精确匹配路径
  return menuItem.redirect === activeMenu.value || menuItem.path === activeMenu.value
}

// 精确匹配子菜单激活状态
const isSubMenuActive = (subMenuItem: any) => {
  return subMenuItem.path === activeMenu.value
}
</script>

<template>
  <div class="expand-menu">
    <!-- logo -->
    <div class="expand-menu__logo">
      <img
        src="@/assets/images/brand/voc-voice-mark-v2.png"
        alt="VOC智声"
        class="expand-menu__logo-img"
      />
      <div>VOC智声</div>
    </div>
    <!-- 切换箭头 -->
    <!-- <div class="sidebar_collapse" @click="handleToggleCollapse">
      <img src="@/assets/images/arrow-right-s-line.png" alt="" :class="{ rotated: true }" />
    </div> -->

    <div class="menu-wrap">
      <template v-for="menuItem in menuItems" :key="menuItem.path">
        <div
          class="menu-item"
          :class="{ 'menu-active': isMenuActive(menuItem) }"
          @click="handleMenuClick(menuItem)"
        >
          <div class="mi-icon">
            <SvgIcon
              v-if="menuItem.meta?.icon"
              :name="menuItem.meta.icon"
              width="24px"
              height="24px"
              :color="isMenuActive(menuItem) ? '#1677FF' : '#929AA6'"
            />
            <!-- <el-icon v-if="menuItem.meta?.icon" :size="24">
              <component :is="menuItem.meta.icon" />
            </el-icon> -->
          </div>
          <div>{{ menuItem.meta?.title }}</div>
        </div>

        <!-- 子菜单（只在非 alwaysShow 的情况下显示，但场景分析例外） -->
        <template
          v-if="
            menuItem.children &&
            menuItem.children.length > 0 &&
            (!menuItem.meta?.alwaysShow || menuItem.name === 'sceneAnalysis')
          "
        >
          <div
            v-for="subItem in menuItem.children"
            :key="subItem.path"
            class="sub-menu-item"
            :class="{ 'sub-menu-active': isSubMenuActive(subItem) }"
            @click="handleSubMenuClick(subItem)"
            v-show="!(menuItem.name === 'sceneAnalysis' && subItem.name === 'sceneAnalysisMain')"
          >
            {{ subItem.meta?.title }}
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.expand-menu {
  width: var(--menu-width);
  min-width: var(--menu-width);
  max-width: var(--menu-width);
  background-color: #fff;
  z-index: 110;
  flex-shrink: 0;
  &__logo {
    height: 84px;
    display: flex;
    align-items: center;
    // justify-content: center;
    border-bottom: 1px solid #d9d9d9;
    border-right: 1px solid #d9d9d9;
    font-weight: 600;
    font-size: 20px;
    color: #0b457f;
    line-height: 22px;
    padding-left: 16px;

    &-img {
      width: 48px;
      height: 48px;
      border-radius: 6px;
      margin-right: 10px;
    }
  }

  .sidebar_collapse {
    width: 40px;
    height: 40px;
    background: #f2f3f5;
    border-radius: 20px 20px 20px 20px;
    border: 1px solid #d9d9d9;
    position: absolute;
    top: 64px;
    right: -20px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: width 0.3s ease;
    z-index: 100;

    img {
      width: 20px;
      height: 20px;
      transition: transform 0.3s ease;

      &.rotated {
        transform: rotate(180deg);
      }
    }
  }

  .menu-wrap {
    width: 100%;
    height: calc(100vh - 84px);
    padding: 24px 16px;
    border-right: 1px solid #d9d9d9;
    overflow: auto;

    .menu-item {
      display: flex;
      align-items: center;
      width: 100%;
      height: 48px;
      border-radius: 8px 8px 8px 8px;
      font-weight: 600;
      font-size: 16px;
      color: #5f6a7a;
      line-height: 24px;
      padding: 0 12px;
      cursor: pointer;

      &:not(:first-child) {
        margin-top: 16px;
      }

      .mi-icon {
        width: 24px;
        height: 24px;
        margin-right: 8px;
      }
    }

    .sub-menu-item {
      display: flex;
      align-items: center;
      width: 100%;
      height: 40px;
      padding-left: 44px;
      font-weight: 500;
      font-size: 16px;
      color: #5f6a7a;
      line-height: 24px;
      cursor: pointer;

      &:hover {
        background: rgba(234, 243, 255, 0.5);
        border-radius: 8px;
      }
    }

    .sub-menu-active {
      background: #eaf3ff;
      font-weight: 600;
      color: #1677ff;
      border-radius: 8px;
    }

    .menu-active {
      background: #eaf3ff;
      font-weight: 600;
      color: #1677ff;
    }
  }
}
</style>
