<template>
  <div class="pdf-view-container">
    <iframe v-if="pdfUrl" :src="pdfUrl" class="pdf-iframe" frameborder="0"></iframe>
    <div class="bottom-float-btn" v-if="showTops">
      <div class="top">
        <div class="text-tops">温馨提示</div>
        <van-icon name="cross" class="text-tops" @click="showTops = false" />
      </div>

      <div class="text-tops mt20">
        1.VOC 网页报告每小时动态更新，PDF 报告数据仅截止至任务推送时刻。
      </div>
      <div class="text-tops">
        2.完整报告可在 VOC智声 PC 端查看；移动端提供核心指标、趋势与结论摘要。
      </div>
      <div class="jump-btn mt20" @click="jumpToPC">PC端访问报告</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFileByFileName } from '@h5/api/report'
import { getReUrl } from '../../utils/initPcFilter'

// 1. 获取pdf在线地址（可通过路由参数或props传递，这里用路由query）
const route = useRoute()
const router = useRouter()
const pdfUrl = ref<string>('') // 获取pdf在线地址
const targetUrl = ref<string>((route.query.target as string) || '') // 获取跳转PC地址
const showTops = ref<boolean>(true) // 是否显示底部提示信息

onMounted(() => {
  console.log('当前参数', route.query, route)

  // 获取pdf在线地址
  const fileName = route.query.pdfUrl as string
  // pdfUrl.value = 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
  if (fileName) {
    // url编码
    const en = encodeURIComponent(fileName)
    getFileByFileName(en).then((res: any) => {
      let fileUrl = res?.result
      // 如果是/开头的需要拼接域名
      if (fileUrl.startsWith('files/')) {
        fileUrl = `${window.location.origin}/${fileUrl}`
      }

      console.log('获取的文件信息', res, '加载的地址', fileUrl)
      pdfUrl.value = fileUrl
    })
  }
})

// 2. 跳转PC端逻辑（可自定义跳转地址，这里假设为window.location.href替换为PC端域名）
const jumpToPC = () => {
  // 实际可根据业务调整
  if (targetUrl.value) {
    const x = getReUrl(targetUrl.value)
    window.location.href = x
  }
}
</script>

<style scoped>
.pdf-view-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  background: #f5f5f5;
  overflow: hidden;
}
.pdf-iframe {
  width: 100vw;
  height: 100vh;
  border: none;
  background: #fff;
  display: block;
}
.bottom-float-btn {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100vw;
  padding: 20Px;
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-tops {
  color: #000;
  font-weight: 700;
}
.mt20 {
  margin-top: 20Px;
}
.jump-btn {
  width: 100%;
  height: 80Px;
  background: #1677ff;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  border: none;
  /* border-radius: 22px; */
  /* font-size: 16px; */
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.12);
  cursor: pointer;
  transition: background 0.2s;
}

.jump-btn:active {
  background: #1456c2;
}
</style>
