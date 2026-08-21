<script setup lang="ts">
import { computed, ref } from 'vue'
import { useUserStore } from '@/store'
import PublishReport from '@/components/Business/Scene/Common/PublishReport/index.vue'
import SubscribeReport from '@/components/Business/Scene/Common/SubscribeReport/index.vue'
import { useRoute, useRouter } from 'vue-router'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'
import { BrandServiceCategoryOptions } from '@/components/Business/Scene/CompetitorAnalysis/constants'
import useMiddlewareStore from '@/store/modules/middleware'
import { FunctionPermission } from '@/constants/btnPermMap'
import { ORIGINAL_DATA_TYPE_OPTIONS, OriginalDataType } from '@/constants'
import { usePdfExport } from '@/hooks/usePdfExport'

defineOptions({
  name: 'SCHeader'
})

const route = useRoute()

// tydmArrObj是为了 回显体验代码的  它的值是整个联动组件改变的事件data
const { title = '主标题', tydmArrObj } = defineProps<{
  title?: string
  tydmArrObj?: any
}>()
const userStore = useUserStore()
const sceneAnalysisStore = useSceneAnalysisStore()
const router = useRouter()
const middlewareStore = useMiddlewareStore()

/**
 * @description: 归一化路由 query 中的标题参数，兼容数组与空值场景
 * @param {unknown} queryValue
 * @return {string}
 */
const normalizeQueryReportName = (queryValue: unknown): string => {
  if (Array.isArray(queryValue)) {
    return normalizeQueryReportName(queryValue[0])
  }

  if (typeof queryValue !== 'string') {
    return ''
  }

  return queryValue.trim()
}

/**
 * @description: 是否显示返回按钮
 * @param {*} computed
 * @return {*}
 */
const isHideBackBtn = computed(() => {
  return route.query.isBack === '1'
})

/**
 * @description: 是否显示发布订阅
 * @param {*} computed
 * @return {*}
 */
const isShowPubAndSub = computed(() => {
  // 热点事件不需要这两个按钮
  if (['hotEvents'].includes(route.name as string)) {
    return false
  }
  // 热点事件详情页面 并且原始数据  不需要这两个按钮
  if (
    ['hotDetailEvents'].includes(route.name as string) &&
    middlewareStore.originalDataType === OriginalDataType.OriginalData
  ) {
    return false
  }
  return true
})

/**
 * @description: 详情态标题优先使用报告名称，普通进入时继续展示默认分析标题
 * @param {*} computed
 * @return {*}
 */
const displayTitle = computed(() => {
  const sendReportName = normalizeQueryReportName(route.query.sendReportName)
  if (sendReportName) {
    return sendReportName
  }
  if (!isHideBackBtn.value && !sceneAnalysisStore.sceneOriginData.isDetail) {
    return title
  }

  const queryReportName = normalizeQueryReportName(route.query.reportName)
  const storeReportName = sceneAnalysisStore.sceneOriginData.reportName

  return storeReportName || queryReportName || title
})
/**
 * @description: 返回事件
 * @return {*}
 */
const handleClose = () => {
  // 如果查询参数中有 from 参数，返回到指定页面，否则返回到默认的场景分析页面
  const fromPath = route.query.from as string
  if (fromPath) {
    router.push({
      path: fromPath
    })
  } else {
    router.push({
      path: '/scene/analysis'
    })
  }
}

const publicReportVisible = ref<boolean>(false)

const publicSubscribeVisible = ref<boolean>(false)
/**
 * @description: 发布按钮事件
 * @return {*}
 */
const handlePublicReport = () => {
  publicReportVisible.value = true
}

const { exporting, canExportCurrentPage, handleExportPdf } = usePdfExport({
  trigger: 'sceneHeader',
  getTitle: () => displayTitle.value
})
/**
 * @description: 订阅按钮事件
 * @return {*}
 */
const handleSubscribeReport = () => {
  publicSubscribeVisible.value = true
}
</script>

<template>
  <div class="sc-header">
    <div v-if="isHideBackBtn" class="go-back mr-16 flex-center" @click="handleClose">
      <SvgIcon name="reverse-left" width="24px" height="24px" color="#5F6A7A"></SvgIcon>
    </div>
    <div class="title-wrapper">
      <span class="title">{{ displayTitle }}</span>
    </div>
    <!-- 竞品对比 品牌/车系分类切换 -->
    <div v-if="['competitorAnalysis'].includes(route.name as string)">
      <SwitchButton
        v-model="middlewareStore.brandServiceCategoryType"
        :options="BrandServiceCategoryOptions"
        class="ml-16"
      ></SwitchButton>
    </div>

    <!-- 事件详情页面的按钮 -->
    <div v-if="['hotDetailEvents'].includes(route.name as string)">
      <SwitchButton
        v-model="middlewareStore.originalDataType"
        :options="ORIGINAL_DATA_TYPE_OPTIONS.filter((e, i) => i < 2)"
        class="ml-16"
      ></SwitchButton>
    </div>

    <div class="btn-group" data-page-export-exclude v-if="isShowPubAndSub">
      <el-button
        v-if="canExportCurrentPage"
        type="primary"
        plain
        size="large"
        class="iround-8"
        :loading="exporting"
        @click="handleExportPdf"
      >
        <span>导出为PDF</span>
      </el-button>

      <el-button type="primary" size="large" class="iround-8" @click="handleSubscribeReport">
        <SvgIcon name="bell" width="20px" height="20px" color="#FFFFFF"></SvgIcon>
        <span class="ml-8">订阅</span>
      </el-button>

      <template v-if="userStore.checkfunctionPermission(FunctionPermission.SCENARIO_PUBLISH)">
        <el-button
          v-if="sceneAnalysisStore.detailFlag"
          type="primary"
          size="large"
          class="iround-8"
          @click="handlePublicReport"
        >
          <SvgIcon name="send-plane-line" width="20px" height="20px" color="#FFFFFF"></SvgIcon>
          <span class="ml-8">发布</span>
        </el-button>
      </template>
    </div>
    <!-- 发布弹窗 -->
    <PublishReport v-model="publicReportVisible" :tydmArrObj="tydmArrObj"></PublishReport>
    <!-- 订阅弹窗 -->
    <SubscribeReport v-model="publicSubscribeVisible" :tydmArrObj="tydmArrObj"></SubscribeReport>
  </div>
</template>

<style lang="scss" scoped>
.sc-header {
  width: 100%;
  // height: 72px;
  height: 84px;
  display: flex;
  align-items: center;
  padding: 0 40px;

  .go-back {
    width: 40px;
    height: 40px;
    background: #f2f4f7;
    border-radius: 8px;
    cursor: pointer;
  }

  .title-wrapper {
    font-weight: 600;
    font-size: 24px;
    line-height: 32px;
    .title {
      color: #1f2733;
    }
  }

  .btn-group {
    margin-left: auto;
  }
}
</style>
