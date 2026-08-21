<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HNavBar from '@h5/components/UI/HNavBar'
import { isLocalDemo } from '@/utils/env'

defineOptions({
  name: 'originalView'
})

const route = useRoute()
const router = useRouter()
const linkSrc = ref()
const localArticle = computed(() => ({
  title: String(route.query.title || '车机升级后的稳定性体验记录'),
  source: String(route.query.source || '车主社区'),
  time: String(route.query.time || '2026-08-06 10:20:00'),
  content: String(
    route.query.content ||
      '完成车机系统升级后，导航、音乐和倒车影像之间快速切换时偶发卡顿。售后服务专员已完成远程诊断并建立专项跟进，后续将同步版本验证、推送节奏与客户回访结果。'
  )
}))
const init = () => {
  const link = route.query.link as string
  linkSrc.value = link ? decodeURIComponent(link) : ''
}

init()

const handleBack = () => {
  router.back()
}
</script>

<template>
  <div class="originalView">
    <HNavBar left-text="返回" @click-left="handleBack" />
    <article v-if="isLocalDemo() || !linkSrc" class="ov-article">
      <h1>{{ localArticle.title }}</h1>
      <div class="ov-meta">{{ localArticle.source }} · {{ localArticle.time }}</div>
      <p>{{ localArticle.content }}</p>
      <section>
        <h2>处理进展</h2>
        <p>问题已进入智能座舱稳定性专项，当前完成日志采集、场景复现和责任分派。</p>
      </section>
    </article>
    <iframe v-else :src="linkSrc" frameborder="0" class="ov-iframe"></iframe>
  </div>
</template>

<style lang="scss" scoped>
.originalView {
  width: 100vw;
  height: 100vh;
  .ov-iframe {
    width: 100vw;
    height: calc(100vh - 46px);
  }

  .ov-article {
    box-sizing: border-box;
    min-height: calc(100vh - 46px);
    padding: 24px 20px;
    color: #1f2733;
    background: #fff;

    h1 {
      margin: 0;
      font-size: 22px;
      line-height: 1.45;
    }

    h2 {
      margin: 28px 0 10px;
      font-size: 17px;
    }

    p {
      margin: 18px 0 0;
      color: #475467;
      font-size: 16px;
      line-height: 1.85;
    }

    .ov-meta {
      margin-top: 12px;
      color: #92929d;
      font-size: 13px;
    }
  }
}
</style>
