<template>
  <div v-loading="loading" class="w-full h-full">
    <div class="redirect-wrapper">
      <FEmpty v-if="isShowTip" :tipStr="tipStr">
        <div class="font-20 mt-20">{{ tipStr }}</div>
      </FEmpty>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { TOKEN_KEY } from '@/constant'
import useUserStore from '@/stores/modules/user'
// import to from 'await-to-js'
import { useRoute, useRouter } from 'vue-router'
import FEmpty from '@/components/FEmpty/index.vue'

const route = useRoute()
const router = useRouter()
const { token } = route.query
const userStore = useUserStore()
const loading = ref(false)
const tipStr = ref('请返回VOC客情直驱平台重新登录')
const isShowTip = ref(false)

const init = async () => {
  try {
    loading.value = true
    if (token) {
      localStorage.setItem(TOKEN_KEY, token as string)
      try {
        await userStore.getUserPermissions()
        // 检查是否有菜单权限
        if (!userStore.menus || userStore.menus.length === 0) {
          // 没有菜单权限，清除用户状态
          isShowTip.value = true
          tipStr.value = '当前用户没有角色权限，请联系管理员配置菜单权限。'
          // 清除用户状态
          userStore.clearStorage()
          return
        }
        setTimeout(() => {
          router.push(userStore.homePath)
        }, 500)
      } catch (error: any) {
        // ElMessage.error(error?.message || error?.msg || '登录失败，请重试')
        isShowTip.value = true
        if (error?.message === '角色ID不允许为空') {
          tipStr.value = '尊敬的客户，您的账户未开通权限，请联系管理员进行配置。'
          // 清除用户状态
          userStore.clearStorage()
        } else {
          tipStr.value = error?.message || error?.msg || '登录失败，请重试'
        }
      }
    } else {
      // 没有token，检查是否是因为没有菜单权限被重定向过来
      if (userStore.hasLoadedPermissions && (!userStore.menus || userStore.menus.length === 0)) {
        isShowTip.value = true
        tipStr.value = '当前用户没有角色权限，请联系管理员配置菜单权限。'
        // 清除用户状态
        userStore.clearStorage()
      } else {
        isShowTip.value = true
      }
    }
  } catch (error) {
    console.error('初始化失败:', error)
    isShowTip.value = true
    tipStr.value = '初始化失败，请重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  init()
})

// const goHome = () => {
//   window.location.href = 'https://www.baidu.com'
// }
</script>

<style lang="scss" scoped>
.redirect-wrapper {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}
</style>
