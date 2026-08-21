<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script lang="ts" setup>
import { zhCn } from 'element-plus/es/locale/index.mjs'
</script>

<style lang="scss">
#app {
  height: 100%;
  font-family: 'PingFangSC-Regular', 'Microsoft YaHei', sans-serif, Avenir, Helvetica, Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */
  font-size: 14px;
  color: var(--color-medium);
}
</style>
