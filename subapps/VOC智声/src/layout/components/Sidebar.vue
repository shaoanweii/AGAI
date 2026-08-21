<template>
  <!-- :class="{ 'sidebar--collapsed': isCollapse }" -->
  <div class="sidebar">
    <!-- Logo 区域 -->
    <div class="sidebar__logo">
      <img
        src="@/assets/images/brand/voc-voice-mark-v2.png"
        alt="VOC智声"
        class="sidebar__logo-img"
      />
    </div>
    <!--  :style="{ left: isCollapse ? '64px' : '220px' }" -->
    <div class="sidebar_collapse" @click="handleToggleCollapse">
      <img src="@/assets/images/arrow-right-s-line.png" alt="" :class="{ rotated: false }" />
    </div>

    <!-- 菜单区域 -->
    <div class="sidebar__menu">
      <SidebarMenu />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/store/modules/app'
import SidebarMenu from './Menu.vue'

/**
 * 默认状态下的菜单
 */
defineOptions({
  name: 'Sidebar'
})

const appStore = useAppStore()
const isCollapse = computed(() => appStore.isCollapse)

// 切换侧边栏折叠状态
const handleToggleCollapse = () => {
  appStore.toggleCollapse()
}
</script>

<style lang="scss" scoped>
.sidebar {
  // width: 240px;
  width: var(--menu-width-collapsed);
  height: 100vh;
  background: #f8f8fb;
  transition: width 0.3s ease;
  // overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;

  .sidebar_collapse {
    width: 40px;
    height: 40px;
    background: #f2f3f5;
    border-radius: 20px 20px 20px 20px;
    border: 1px solid #d9d9d9;
    position: absolute;
    top: 64px;
    // left: 64px;
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

  &--collapsed {
    width: var(--menu-width-collapsed);
  }

  &__logo {
    height: 84px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #d9d9d9;

    &-img {
      width: 48px;
      height: 48px;
      border-radius: 6px;
    }

    &-text {
      margin-left: 12px;
      color: #ffffff;
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.3s ease;
    }
  }

  &__menu {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;

    // 自定义滚动条
    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}
</style>
