<template>
  <div class="main-wrapper">
    <el-container>
      <el-aside :width="collapsed ? '64px' : '280px'">
        <Menu @onCollapse="(val: any) => onCollapse(val)" />
      </el-aside>
      <el-container>
        <el-header>
          <Header />
        </el-header>
        <el-main>
          <router-view :key="route.fullPath"></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script lang="ts" setup>
import Header from '@/layouts/Header.vue'
import Menu from '@/layouts/Menu.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const collapsed = ref(false)
const onCollapse = (val: boolean) => {
  collapsed.value = val
}
</script>
<style lang="scss" scoped>
.main-wrapper {
  height: 100vh;
  min-width: 0;
  overflow: hidden;
}
:deep(.el-container) {
  min-width: 0;
}
:deep(.el-header) {
  // background: #fff;
  border-bottom: 1px solid #d0d9e4;
  padding: 10px 16px;
  height: var(--header-height);
}
:deep(.el-aside) {
  // height: calc(100vh - var(--header-height));
  height: 100vh;
  background: #fff;
  transition: width 0.3s;
}
:deep(.el-main) {
  min-width: 0;
  height: calc(100vh - var(--header-height) - 24px);
  //padding: 24px 24px 48px;
  padding: 24px;
  box-sizing: border-box;
  overflow-y: auto;
}
</style>
