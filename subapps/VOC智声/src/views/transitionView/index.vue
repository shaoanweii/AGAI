<template>
  <div class="transition-view">正在进入 VOC智声...</div>
</template>

<script setup lang="ts">
import useGeneralScenarioStore from '@/store/modules/generalScenario'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { onBeforeUnmount, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQueryDataByid } from '@/api/subscribeReport'
import { getQueryParams } from '@/utils'
import { isLocalDemo } from '@/utils/env'

defineOptions({
  name: 'TransitionView'
})

const generalScenarioStore = useGeneralScenarioStore()
const sceneAnalysisStore = useSceneAnalysisStore()
const route = useRoute()
const router = useRouter()

generalScenarioStore.handleOpen('TransitionView')

const initData = async () => {
  // 处理后端带参数直接访问逻辑 根据参数去判断是不是特定回显查询条件判断
  const target = String(route?.query?.target || '')
  if (!target) {
    await router.replace('/overview')
    return
  }
  const targetParams = getQueryParams(target)
  const reportId = targetParams?.sendReportId

  const taskId = targetParams?.sendTaskId

  let sendReportName = undefined
  if (reportId || taskId) {
    try {
      // 查询需要回显的条件
      const res = await getQueryDataByid({
        reportId,
        taskId
      })
      if (res.success) {
        const filterStr = res.result?.filter
        const item = {
          defaultCondition: filterStr,
          isDetail: true
        }
        // 调用点击报告公共的初始化参数方法
        await sceneAnalysisStore.setSceneOriginData({
          ...item,
          isDetail: true
        })
        const filterJson = JSON.parse(filterStr)
        if (filterJson?.sendReportName) {
          sendReportName = filterJson?.sendReportName
        }
      }
    } catch (error) {
      //
    } finally {
      // 执行真实跳转
    }
  }

  const url1 = target.split('?')?.[0]
  const url2 = url1.split('#')?.[1]
  // 热点事件页面需要额外带更多的参数过去 用于筛选器的回显
  let otherParams: any = {}
  if (target.includes('hotDetailEvents')) {
    otherParams.from = targetParams.from
    otherParams.isBack = targetParams.isBack
    if (reportId) {
      // 给一个全新的名称
      otherParams.reportHotId = reportId
    }
    if (taskId) {
      // 给一个全新的名称
      otherParams.taskHotId = taskId
    }
  }
  let query: any = {
    ...otherParams,
    centerJudge: 'true' // 中转跳转的页面参数 为了处理新车上市等页面不要重新初始化
  }
  if (sendReportName) {
    query.sendReportName = sendReportName
  }
  // 都需要跳转
  if (url2) {
    router.push({
      path: url2,
      query: query
    })
  } else {
    if (isLocalDemo()) {
      await router.replace(target.startsWith('/') ? target : '/overview')
    } else {
      window.location.href = target
    }
  }
}

// onBeforeUnmount(() => {
//   generalScenarioStore.handleClose()
//   sceneAnalysisStore.setSceneOriginData({ isDetail: false })
// })
onMounted(() => {
  initData()
})
</script>

<style lang="scss" scoped>
.transition-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f6a7a;
  background: #f5f7fa;
}
</style>
