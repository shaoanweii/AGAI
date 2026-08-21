<template>
  <div class="error-page">
    <div class="error-code">404</div>
    <div class="error-desc">抱歉，您访问的页面不存在</div>
    <el-button type="primary" @click="backHome">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const backHome = () => {
  // 如果有用户首页路径，优先跳转到首页
  const homePath = userStore.homePath || '/overview'
  router.replace(homePath)
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f5f5;
  .error-code {
    font-size: 160px;
    font-weight: 700;
    margin-bottom: 24px;
    line-height: 1;
    text-shadow: 4px 4px 10px rgba(0, 0, 0, 0.1);
  }

  .error-desc {
    font-size: 24px;
    margin-bottom: 48px;
  }
}
</style>
