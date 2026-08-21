<template>
  <div class="h5-layout">
    <!-- 根据路由 meta.keepAlive 对子路由进行缓存，以保留页面交互状态 -->
    <router-view v-slot="{ Component, route }">
      <keep-alive>
        <component v-if="route.meta?.keepAlive" :is="Component" :key="route.fullPath" />
      </keep-alive>
      <component v-if="!route.meta?.keepAlive" :is="Component" :key="route.fullPath" />
    </router-view>

    <!-- H5 底部导航 -->
    <H5TabBar />

    <!-- canswer登录 -->
    <!-- <CanswerLogin v-if="showCanswerLogin"></CanswerLogin> -->
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CanswerLogin from '@h5/components/CanswerLogin/index.vue'
import { usePermissionsStore } from '@h5/store'
import H5TabBar from './H5TabBar.vue'
import { useH5Layout } from '@h5/hooks/useH5Layout'

const permissionsStore = usePermissionsStore()

const showCanswerLogin = computed(() => {
  if (!permissionsStore.hasInited) return false

  const hasMenu = (menus: any[]): boolean => {
    if (!menus?.length) return false

    return menus.some((menu: any) => {
      if (menu?.name === 'linkUrl' && menu?.path === 'canswer') {
        return true
      }

      if (menu?.permissionKey?.toLowerCase?.().includes('canswer')) {
        return true
      }

      if (menu?.path?.toLowerCase?.().includes('canswer')) {
        return true
      }

      if (menu?.children?.length) {
        return hasMenu(menu.children)
      }

      return false
    })
  }

  return hasMenu(permissionsStore.menus || [])
})

// 统一挂载 H5 布局级逻辑：body 类名、viewport 管理、权限初始化等
useH5Layout()
</script>

<style lang="scss" scoped>
.h5-layout {
  display: flex;
  flex-direction: column;
  height: 100vh; /* 确保布局至少撑满整个视口高度 */
  background-color: white; /* H5 页面通常背景色较浅 */
}
</style>

<style>
/* 全局样式修改，确保 h5-active 生效 */
body.h5-active {
  min-width: unset !important; /* 覆盖全局的 min-width */
  overflow: hidden !important; /* 允许 H5 页面滚动 */
  -webkit-user-select: none; /* Chrome, Opera, Safari */
  -moz-user-select: none; /* Firefox */
  -ms-user-select: none; /* Internet Explorer/Edge */
  user-select: none; /* Standard */
}

/* H5 底部存在 Tabbar 时，为内部滚动容器预留底部空间，避免内容被遮挡 */
body.h5-tabbar-visible .f-page__content {
  /* 使用 CSS 变量统一管理 Tabbar 高度，便于后续视觉调整 */
  padding-bottom: calc(var(--h5-tabbar-height, 50px) + 6px);
}
</style>
