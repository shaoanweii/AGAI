<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissionsStore } from '../../store/permissions'
import { useH5ssoStore } from '../../store/sso'
import { showToast } from 'vant'
import { isLocalDemo } from '@/utils/env'

defineOptions({
  name: 'linkCanswer'
})

const userPermStore = usePermissionsStore()
const ssoStore = useH5ssoStore()
const router = useRouter()

const linkSrc = ref()
const loading = ref(true)

const init = async () => {
  if (isLocalDemo()) {
    await router.replace({
      path: '/h5/analysisAndVoice',
      query: { brandCode: 'voc-brand-zhixing', source: 'local-demo' }
    })
    return
  }

  try {
    const userInfo = await ssoStore.qxInitByCanswer()
    if (!userInfo?.loginID) {
      loading.value = false
      return
    }
    const res = (await userPermStore.handleCanswerAuth(userInfo?.loginID)) as any
    console.log('Canswer->getAuthDataUrl--->res', res)
    console.log('Canswer->res.result--url', res?.result)
    if (res?.success) {
      linkSrc.value = res?.result
    } else {
      showToast(res?.message)
    }
  } catch (error) {
    console.log('error', error)
  } finally {
    loading.value = false
  }
}
init()
</script>

<template>
  <div class="link-canswer">
    <div v-if="loading" class="lc-loading">
      <van-loading color="#0094ff" :size="40" vertical>加载中...</van-loading>
    </div>
    <iframe v-if="linkSrc" :src="linkSrc" frameborder="0" class="ov-iframe"></iframe>
    <div v-else class="empty">
      <van-empty
        image="error"
        description="抱歉，您暂无此菜单访问权限，请联系系统管理员配置权限，感谢配合。"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.link-canswer {
  width: 100vw;
  height: 100vh;
  .empty {
    width: 100vw;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
  }
  .ov-iframe {
    width: 100%;
    height: 100%;
  }

  .lc-loading {
    width: 100vw;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba($color: #000000, $alpha: 0.6);
  }
}
</style>
