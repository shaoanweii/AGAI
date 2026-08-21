<template>
  <div class="menu-wrapper">
    <div class="menu-header flex item-center">
      <img class="product-logo" :src="voiceLogo" alt="声音洞察引擎" />
      <div class="brand-copy">
        <h1>声音洞察引擎</h1>
        <span>AGAI Voice Insight</span>
      </div>
    </div>
    <el-menu
      :default-active="active"
      :default-openeds="openKeys"
      class="menu sidebar-menu"
      :collapse="collapsed"
      mode="vertical"
      router
      @select="handleClick"
    >
      <template v-for="item in menuList" :key="item.path">
        <el-menu-item v-if="item.meta?.alwaysShow" :index="item.redirect || item.path">
          <template #title>
            <SvgIcon
              :name="item.meta?.icon"
              width="24px"
              height="24px"
              class="mr-8"
              :color="active === (item.redirect || item.path) ? '#1677FF' : '#929AA6'"
            />
            <span class="menu-title">{{ item?.meta?.title }}</span>
          </template>
        </el-menu-item>
        <el-sub-menu v-else-if="item.children && item.children.length" :index="item.path">
          <!--  <img src="@/assets/svg/menus/instance-fill.svg" alt="" /> -->
          <template #title>
            <!-- :color="active === item.path ? '#1677FF' : ' #929AA6'" -->
            <SvgIcon
              :name="item.meta?.icon"
              width="24px"
              height="24px"
              class="mr-8"
              :color="active.startsWith(item.path as string) ? '#1677FF' : '#929AA6'"
            />
            <span>{{ item.meta?.title }}</span>
          </template>
          <el-menu-item v-for="it in item.children" :key="it.path" :index="it.path">
            <template #title>
              <!-- <SvgIcon name="menus-instance-fill" color="#929AA6" /> -->
              <span class="ml-12"> {{ it.meta?.title }}</span>
            </template>
          </el-menu-item>
        </el-sub-menu>
        <template v-else>
          <el-menu-item :index="item.path">
            <template #title>
              <SvgIcon
                :name="item.meta?.icon"
                width="24px"
                height="24px"
                class="mr-8"
                :color="active === item.path ? '#1677FF' : '#929AA6'"
              />
              <span class="menu-title">{{ item.meta?.title }}</span>
            </template>
          </el-menu-item>
        </template>
      </template>
    </el-menu>
  </div>
</template>

<script lang="ts" setup>
import { useRoute, useRouter } from 'vue-router'
import useUserStore from '@/stores/modules/user'

const voiceLogo = '/workspace-assets/logos/voice-insight-logo.png'

interface MenuItem {
  icon: string
  name: string
  path?: string
  redirect?: string
  meta?: Record<string, any>
  children?: MenuItem[]
}

// const emit = defineEmits(['onCollapse'])

// 添加 collapsed 属性
const collapsed = ref(false)
const active = ref('/home')
const openKeys = ref<string[]>([])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const knowledgeCenterMenuOrder = new Map(
  [
    '/knowledgeCenter/experienceCode',
    '/knowledgeCenter/standardPoint',
    '/knowledgeCenter/corpusMapping',
    '/knowledgeCenter/userJourney',
    '/knowledgeCenter/carUsageScenarios',
    '/knowledgeCenter/brandSeries',
    '/knowledgeCenter/keywordLibrary'
  ].map((path, index) => [path, index])
)

// 监听折叠状态变化 (暂时未使用)
// const onCollapse = (val: boolean) => {
//   collapsed.value = val
//   emit('onCollapse', val)
// }

onMounted(() => {
  setCurRoute(route.path)
})

const menuList = computed((): MenuItem[] => {
  // 获取用户权限菜单的 permissionKey 列表
  const remoteMenuKeys = userStore.getRemoteMenuPermissionKey()

  // 获取所有路由，包括动态添加的路由
  const allRoutes = router.getRoutes()

  const filtered = allRoutes.filter(route => {
    // 过滤条件：不是隐藏的，且有子路由或者设置了 alwaysShow
    const isVisible = !route.meta?.hidden && (route.children?.length > 0 || route.meta?.alwaysShow)
    // 排除登录页和错误页
    const isNotSpecialPage = !['login', '403', '404', '500'].includes(route.name as string)
    // 检查是否在用户权限菜单中（首页路径'/'始终显示，因为它在constantRoutes中）
    const hasPermission = route.path === '/' || remoteMenuKeys.includes(route.name as string)

    return isVisible && isNotSpecialPage && hasPermission
  }) as any
  console.log('filtered', filtered)

  return filtered.map(route => {
    if (route.path !== '/knowledgeCenter') return route

    return {
      ...route,
      children: [...(route.children || [])].sort((a, b) => {
        const orderA = knowledgeCenterMenuOrder.get(a.path) ?? Number.MAX_SAFE_INTEGER
        const orderB = knowledgeCenterMenuOrder.get(b.path) ?? Number.MAX_SAFE_INTEGER
        return orderA - orderB
      })
    }
  })
})

const setCurRoute = (path: string) => {
  let subMenuKey = path.startsWith('/') && path.substring(1).split('/')[0]
  active.value = path
  openKeys.value = (subMenuKey && [`/${subMenuKey}`]) || []
}

watch(
  () => route.path,
  (val: string) => {
    setCurRoute(val)
  }
)

const handleClick = (key: string) => {
  if (key && key !== route.path) {
    router.push(key)
  }
}
</script>

<style lang="scss" scoped>
.menu-wrapper {
  position: relative;
  height: 100%;

  .menu-header {
    height: var(--header-height);
    background: #fff;
    border-bottom: 1px solid #d0d9e4;
    box-sizing: border-box;

    .product-logo {
      width: 56px;
      height: 56px;
      object-fit: contain;
      margin-left: 16px;
    }

    .brand-copy {
      display: flex;
      flex-direction: column;
      justify-content: center;
      width: 90px;
      margin-left: 10px;
    }

    h1 {
      margin: 0;
      font-weight: 700;
      font-size: 19px;
      color: #102a56;
      line-height: 22px;
      white-space: nowrap;
    }

    span {
      margin-top: 3px;
      font-size: 11px;
      font-weight: 600;
      color: #5f7fac;
      line-height: 13px;
      letter-spacing: 2px;
      white-space: nowrap;
    }
  }

  .menu {
    border-right: none !important;
  }

  .el-menu {
    height: calc(100% - var(--header-height));
    width: 280px;
    box-sizing: border-box;

    &.el-menu-collapsed {
      width: 48px;
    }
  }
}

.sidebar-menu {
  border: none;
  background: transparent;
  margin: 0 auto;
  padding: 9px;

  // 菜单项文字样式
  :deep(.el-menu-item) {
    background: rgba(255, 255, 255, 0.5);
    border-radius: 9px;
    padding: 0;
    font-weight: 600;
    font-size: 16px;
    color: #5f6a7a;

    .menu-title {
      color: #5f6a7a;
      font-weight: 600;
    }

    &:hover {
      background-color: rgba(234, 243, 255, 0.5);

      .menu-title {
        color: #5f6a7a;
      }
    }

    &.is-active {
      background: #eaf3ff;
      border-radius: 9px;
      color: #1677ff;
      font-weight: 600;

      .menu-title {
        color: #1677ff;
        font-weight: 600;
      }
    }

    &.el-menu-item:not(:first-child) {
      // margin-top: 16px;
      margin-top: 8px;
    }
  }

  // 子菜单标题样式
  :deep(.el-sub-menu__title) {
    font-weight: 600;
    font-size: 16px;
    color: #5f6a7a;

    &:hover {
      border-radius: 9px;
      background-color: rgba(234, 243, 255, 0.5);
      color: #5f6a7a;
    }
  }

  // 子菜单容器样式
  :deep(.el-sub-menu) {
    background: rgba(255, 255, 255, 0.5);
    border-radius: 9px;

    &:hover {
      border-radius: 9px;
    }

    &.el-sub-menu:not(:first-child) {
      // margin-top: 16px;
      margin-top: 8px;
    }

    &.is-active {
      .el-sub-menu__title {
        color: #1677ff;
        font-weight: 600;
      }
    }

    // 子菜单项样式
    .el-menu {
      .el-menu-item {
        font-weight: 500;
        font-size: 16px;
        color: #5f6a7a;

        &:hover {
          background-color: rgba(234, 243, 255, 0.5);
          border-radius: 0;
          color: #5f6a7a;
        }

        &.is-active {
          background: #eaf3ff;
          border-radius: 0;
          color: #1677ff;
          font-weight: 600;
        }
      }
    }
  }

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
        // margin-top: 16px;
        margin-top: 8px;
      }

      &:hover {
        background-color: rgba(234, 243, 255, 0.5);
      }

      &.is-active {
        background: #eaf3ff;
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
        // margin-top: 16px;
        margin-top: 8px;
      }

      &:hover {
        background-color: rgba(234, 243, 255, 0.5);
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
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}
</style>
