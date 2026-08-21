<script setup lang="ts">
import { ref, onMounted } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { removeWatermark } from '@/utils'

const loading = ref(false)

// 清理旧版本缓存
const clearStorage = () => {
  const originVersion = localStorage.getItem('version')
  const nowVersion = import.meta.env.VITE_APP_VERSION || '1.0.0'
  if (originVersion !== nowVersion) {
    localStorage.clear()
    localStorage.setItem('version', nowVersion)
  }
}

// 初始化数据
const init = async () => {
  loading.value = true
  try {
    // 模拟初始化过程
    await initUserInfo()
    loading.value = false
  } catch (error) {
    console.error('初始化失败:', error)
    loading.value = false
  }
}

// 初始化用户信息
const initUserInfo = async () => {
  // 模拟用户信息
  // appStore.setUser({
  //   userName: '管理员',
  //   userAccount: 'admin'
  // })
}

onMounted(() => {
  removeWatermark()
  clearStorage()
  init()
})
</script>

<template>
  <div id="app" v-loading="loading" element-loading-text="获取网站基础信息中...">
    <el-config-provider :locale="zhCn">
      <!-- 主要内容 -->
      <router-view v-if="!loading" />
    </el-config-provider>
  </div>
</template>

<style></style>
