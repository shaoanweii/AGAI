<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const goLogin = () => {
  // logout()
  router.back()
}
</script>

<template>
  <div class="full-content">
    <el-card class="f-card" :bordered="false">
      <el-result icon="warning" title="404" sub-title="抱歉，页面不见了～">
        <template #extra>
          <el-space>
            <!--<el-button>重试</el-button>-->
            <el-button type="primary" @click="goLogin">返回</el-button>
          </el-space>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.f-card {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  box-sizing: border-box;
  &::v-deep(.el-card-body) {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    box-sizing: border-box;
  }
}
</style>
