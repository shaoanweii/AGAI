<template>
  <div class="error-page">
    <van-empty image="error" description="抱歉，您访问的页面不存在">
      <el-button type="primary" @click="backHome">返回首页</el-button>
    </van-empty>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const backHome = () => {
  router.push('/h5')
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
}
</style>
