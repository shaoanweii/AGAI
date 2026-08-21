<script setup lang="ts">
import { useH5LayoutSimple } from '@h5/hooks/useH5LayoutSimple'

defineOptions({
  name: 'NotAuth'
})

// 统一挂载 H5 布局级基础逻辑：rem 适配、viewport 管理、body 类名等
useH5LayoutSimple()
</script>

<template>
  <div class="not-auth-page">
    <img src="@/assets/images/h5-notAuth.svg" width="240" height="240" alt="" />
    <div class="info1">暂无权限访问</div>
    <div class="info2">
      当前会话暂无 VOC智声 访问权限，请返回扫码入口重新进入，或联系
      <span class="info3">演示管理员</span>。
    </div>
  </div>
</template>

<style lang="scss" scoped>
.not-auth-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  // justify-content: center;
  background-color: #fff;
  flex-direction: column;
  padding-top: 40%;
  .info1 {
    font-weight: 500;
    font-size: 16px;
    color: #26292e;
    line-height: 24px;
    margin-top: 20px;
  }
  .info2 {
    font-weight: 400;
    font-size: 14px;
    color: #1f2733;
    line-height: 24px;
    margin-top: 12px;
    width: 230px;
    text-align: center;
  }
  .info3 {
    color: #1677ff;
  }
}
</style>
