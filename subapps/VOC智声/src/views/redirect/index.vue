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
import { SSO_URL, TOKEN_KEY } from '@/constants'
import { checkToken } from '@/api/common'
import useUserStore from '@/store/modules/user'
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { setToken } from '@/utils'
import { isLocalDemo } from '@/utils/env'
const route = useRoute()
const router = useRouter()
const { token } = route.query
const userStore = useUserStore()
const loading = ref(false)
const tipStr = ref('请返回工作台重新登录')
const isShowTip = ref(false)

const init = async () => {
  loading.value = true
  if (token) {
    localStorage.setItem(TOKEN_KEY, token as string)
    try {
      const data = await checkToken({ tokenKey: token as string })
      if ((data as any)?.result) {
        try {
          await userStore.getUserPermissions()
          setTimeout(() => {
            router.push(userStore.homePath)
            loading.value = false
          }, 500)
        } catch (error: any) {
          ElMessage.error(error.message)
          isShowTip.value = true
          loading.value = false
          if (error.message === '角色ID不允许为空') {
            tipStr.value = '尊敬的客户，您的账户未开通权限，请联系管理员进行配置。'
          } else {
            tipStr.value = error.message
          }
        }
      } else {
        isShowTip.value = true
        ElMessage.error('token无效')
        loading.value = false
      }
    } catch (error: any) {
      ElMessage.error(error.message)
      loading.value = false
      isShowTip.value = true
      tipStr.value = error.message
    }
  } else {
    isShowTip.value = true
  }
  setTimeout(() => {
    loading.value = false
  }, 1000)
}
// init()

const linkHome = () => {
  router.push({
    path: userStore.homePath
  })
}

// ichangan登录
const authLogin = async () => {
  console.log('authLogin--start')

  console.log('location.origin', location.origin)
  console.log('SSO_URL', SSO_URL)
  location.href = SSO_URL
  // const result = await sso()
  // console.log('result', result)

  console.log('authLogin--end')
}

const iChangeanInit = async () => {
  try {
    loading.value = true
    console.log('iChangeanInit--start--token', token)

    if (isLocalDemo()) {
      setToken('voc-voice-local-demo-token')
      await userStore.getUserPermissions()
      await router.replace(userStore.homePath)
      return
    }

    if (token) {
      setToken(token as string)

      try {
        await userStore.getUserPermissions()
        setTimeout(() => {
          linkHome()
          loading.value = false
        }, 500)
      } catch (error: any) {
        ElMessage.error(error.message)
        isShowTip.value = true
        loading.value = false
        if (error.message === '角色ID不允许为空') {
          tipStr.value = '尊敬的客户，您的账户未开通权限，请联系管理员进行配置。'
        } else {
          tipStr.value = error.message
        }
      }
    } else {
      authLogin()
    }
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

iChangeanInit()
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

  .font-20 {
    font-size: 20px;
  }
}
</style>
