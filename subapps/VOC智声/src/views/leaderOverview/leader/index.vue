<script setup lang="ts">
import { computed, ref, type Ref, inject, watch } from 'vue'
import { useRoute } from 'vue-router'
import BrandView1 from './components/BrandView1/index.vue'
import BrandRank from './components/BrandRank/index.vue'
// 品牌懂擦
import BrandView2 from './components/BrandView2/index.vue'
import SceneAnalysis from './components/SceneAnalysis/index.vue'
import TopBank from './components/TopBank/index.vue'
import VoiceList from '@/components/Business/VoiceListPanel/index.vue'
import { useQueryStore } from '@/store/modules/query'
import { getBrandRankingResult, getProductScenarioAnalysisResult } from '@/api/reportSummary/index'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import PdfExportHeader from '@/components/Business/PdfExportHeader/index.vue'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { DrillTabKey } from '@components/Business/DrillDownDialog/constants.ts'
import { usePageCardDownload } from '@/hooks/usePageCardDownload'
import { useDownloadAction } from '@/hooks/useDownloadAction'
import { getCardStatExportRequest, leaderStatExportMap, exportVocSounds } from '@/api/downloadTask'
import { CARD_EXPORT_KEYS } from '@/constants/cardExportKeys'
import {
  getBrandRankingQueryParams,
  getProductScenarioAnalysisQueryParams
} from '@/views/leaderOverview/leader/common/fn.ts'

// 市场横评

// 组件name
defineOptions({
  name: 'LeaderOverview'
})

const ddStore = useGeneralDrillDownStore()

const route = useRoute()
const queryStore = useQueryStore()
const storePms = queryStore.currentQueryParams
const { downloading: voiceExportLoading, downloadByRequest: downloadVoiceData } =
  useDownloadAction()

usePageCardDownload({
  getParams: () => ({ ...storePms }),
  getStatRequest: payload => getCardStatExportRequest(leaderStatExportMap, payload)
})
const voiceListCommonProps = {
  showSortSelect: true,
  showHighQualityFilter: true,
  enableHighQualityActions: true,
  enableHighQualityInfo: true,
  enableErrorCorrection: true,
  showBatchAction: true,
  enableEventIssueAction: true
}

const channelCatagory = ref<any>('')
const switchOpts = ref([
  { value: '', label: '全域' },
  { value: '公域', label: '公域' },
  { value: '私域', label: '私域' }
])

const handleSwitch = () => {
  queryStore.updateQueryParams({
    channelCatagory: channelCatagory.value
  })
}

/**
 * 导出领导总览客户原声列表数据。
 * 参数由 VoiceListPanel 在点击时基于当前筛选条件组装，确保导出范围与列表一致。
 *
 * @param params 当前客户原声查询条件
 */
const handleVoiceExportData = async (params: VocQueryParams) => {
  await downloadVoiceData({
    request: exportVocSounds,
    params,
    exportMenu: '领导总览-客户原声',
    errorMessage: '导出数据失败，请稍后重试'
  })
}

// TopBank 组件引用，用于清空表格排序
const topBankRef = ref<InstanceType<typeof TopBank>>()

// 使用 inject 接收父组件传递的滚动位置
const scrollPosition = inject('scrollPosition') as Ref<number>

const setFixed = () => {
  const dom1 = document.getElementById('brandView1')
  const dom2 = document.getElementById('brandView2')
  const rect1 = dom1 && dom1.getBoundingClientRect()
  const rect2 = dom2 && dom2.getBoundingClientRect()

  if (dom1 && rect1) {
    dom1.classList.toggle('fixed', rect1.top <= -220) //< dom1被覆盖式距离顶部的位置
  }

  if (dom2 && rect2) {
    dom2.classList.toggle('fixed', rect2.top <= -100) //< dom2被覆盖式距离顶部的位置

    // 显示2 （dom2 居正位时）
    if (rect2.top < 300) {
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      dom1 && dom1.classList.remove('fixed')
    }
  }
}

const brandRankingReportParams = computed(() => getBrandRankingQueryParams(storePms))
const productScenarioAnalysisReportParams = computed(() =>
  getProductScenarioAnalysisQueryParams(storePms)
)
const exportTitle = computed(() => String(route.meta.title || '领导总览'))

/* 时间切换重置过滤条件 */
watch(
  () => ({
    startDate: storePms.startDate,
    endDate: storePms.endDate
  }),
  () => {
    // 重置时间时, 需重置渠道分类
    channelCatagory.value = ''
    queryStore.updateQueryParams({
      // 市场横评
      brandCode: 'groupCode',
      // 品牌洞察
      tempCode: 'groupCode', // 品牌洞察统一使用 tempCode 存当前选中项，请求时再转换为 automark / brandCode
      tag2Code: '',
      intention: '',
      topic: '',
      channelCatagory: ''
    })
    // 清空品牌洞察模块中表格的排序状态
    topBankRef.value?.clearAllSort()
  }
)

// 监听滚动位置的变化
watch(
  () => scrollPosition.value,
  () => {
    setFixed()
  }
)

// jacky本地测试，不影响线上
const isJacky = localStorage.getItem('jacky')

// 处理品牌排行表格点击事件
const handleBrandRankTableClick = (data: {
  row: any
  column?: any
  sceneData?: any
  queryType: string
}) => {
  console.log('品牌排行表格点击数据:', data)
  console.log('查询类型:', data.queryType)

  const { row, queryType, sceneData } = data
  const seriesFactoryName = row.seriesFactory || row.name
  const filterTags = []
  if (queryType === 'series') {
    filterTags.push({ text: row.name, value: { carSeriesCode: row.code } })
  } else if (queryType === 'seriesFactory') {
    filterTags.push({ text: seriesFactoryName, value: { seriesFactory: seriesFactoryName } })
  } else if (queryType === 'brand') {
    filterTags.push({ text: row.name, value: { brandCode: row.code } })
  }
  if (sceneData) {
    filterTags.push({ text: sceneData.sceneName, value: { tag4Code: sceneData.sceneCode } })
  }
  if (row.name !== '市场均值') {
    ddStore.openDD(
      {
        carSeriesCode: queryType === 'series' ? row.code : undefined,
        automark: queryType === 'seriesFactory' ? seriesFactoryName : undefined,
        brandCode: queryType === 'brand' ? row.code : undefined,
        tag4Code: sceneData ? sceneData.sceneCode : undefined,
        brandDataType: 2,
        channelCatagory: '公域',
        startDate: queryStore?.currentQueryParams.startDate,
        endDate: queryStore?.currentQueryParams.endDate,
        // 清除品牌洞察干扰
        tempCode: undefined,
        tag2Code: undefined,
        intention: undefined,
        topic: undefined,
        channelIds: undefined,
        contentTypes: undefined,
        custProvinceCodeSet: undefined,
        gender: undefined,
        isBigV: undefined,
        isCarOwner: undefined,
        isMainPost: undefined,
        isWsaterArmy: undefined,
        carSeriesList: undefined,
        customerName: undefined,
        dataId: undefined,
        firstCodeTag: undefined,
        oneId: undefined,
        originalLink: undefined,
        secondCodeTag: undefined,
        titleOrOriginal: undefined,
        topicCodes: undefined
      },
      {
        // 弹框副标题显示：传入点击行的名称（仅用于展示）
        subTitle: sceneData ? sceneData.sceneName : row.name,
        drillScene: sceneData ? DrillTabKey.SCENARIO : ''
      },
      filterTags,
      {
        // 领导页下钻不继承场景页的公共筛选，避免跨页面参数串值
        mergeCommonQueryParams: false
      }
    )
  }
}
</script>

<template>
  <!-- @scroll="handleScroll" -->
  <div class="leaderPage" data-page-export-root="leader">
    <PdfExportHeader
      :title="exportTitle"
      :start-date="storePms.startDate"
      :end-date="storePms.endDate"
    />

    <!-- 1 市场横评 -->
    <template v-if="!isJacky || true">
      <FCard
        title="市场横评"
        class="part1"
        :card-key="CARD_EXPORT_KEYS.leader.MarketComparison"
        downloadable
      >
        <template #leftExtra>
          <span class="text-xs lh-28">（该板块仅对公域渠道数据进行统计分析）</span>
        </template>

        <!-- 简报 -->
        <BrandView1 />

        <!-- 概要 -->
        <!-- <TheSummary type="1" /> -->
        <ReportSummary
          :api-function="getBrandRankingResult"
          :query-params="brandRankingReportParams"
          class="mt-24 mb-24"
        ></ReportSummary>

        <!-- 品牌排行 -->
        <BrandRank @table-click="handleBrandRankTableClick" />
      </FCard>
    </template>

    <!-- 2 品牌洞察 -->
    <template v-if="!isJacky || true">
      <FCard
        title="品牌洞察"
        class="mt-24"
        :card-key="CARD_EXPORT_KEYS.leader.BrandInsight"
        downloadable
      >
        <template #leftExtra>
          <span class="text-xs lh-28">（该板块对公域和私域全部渠道数据进行统计分析）</span>
        </template>
        <template #more>
          <SwitchButton
            v-model="channelCatagory"
            :options="switchOpts"
            @change="handleSwitch"
          ></SwitchButton>
        </template>

        <!-- 简报 -->
        <BrandView2 />

        <!-- 概要 -->
        <!-- <TheSummary type="2" /> -->
        <ReportSummary
          :api-function="getProductScenarioAnalysisResult"
          :query-params="productScenarioAnalysisReportParams"
          class="mt-24 mb-24"
        ></ReportSummary>

        <!-- 分析图表 2个 -->
        <SceneAnalysis />

        <!-- 观点top 4个 -->
        <TopBank ref="topBankRef" />
      </FCard>

      <!-- 客户原声 -->
      <VoiceList
        class="mt-24 voice-list-layout"
        v-bind="voiceListCommonProps"
        :show-export-action="true"
        :export-loading="voiceExportLoading"
        @export-data="handleVoiceExportData"
      />
    </template>
  </div>
</template>

<style lang="scss" scoped>
.voice-list-layout {
  height: 783px;
  display: flex;
  flex-direction: column;
}
// 公共
.leaderPage {
  font-family:
    PingFang SC,
    PingFang SC;

  :deep(.el-loading-mask) {
    z-index: 9;
  }
  :deep(.f-card) {
    background-color: #fff !important;
  }

  :deep(.bv-wrap) {
    &.fixed .brand-view {
      position: fixed;
      top: 84px;
      left: 284px;
      right: 31px;
      z-index: 10; // 上面设置loading是9
      // height: 100px;
      padding: 20px 24px;
      background-color: #fff;
      box-shadow: 0 4px 4px rgba(0, 0, 0, 0.05);

      .bv-item {
        // 简报1 不fixed时为8px
        padding-bottom: 16px;
      }

      .brand-data-table {
        display: none !important;
      }
    }
  }
  :deep(#brandView1) {
    &.fixed .brand-view {
      display: grid;
      grid-template-columns: repeat(6, 1fr);

      .bv-item {
        width: auto;
      }
    }
  }

  :deep(#brandView2) {
    &.fixed .brand-view {
      .bv-item {
        height: 60px;
      }
      .bv-cont {
        display: none !important;
      }
    }
  }

  // 市场横评
  :deep(.part1) {
    // 品牌车系排行
    .f-card {
      .text-h3 {
        font-size: 16px;
      }
      .fc-header,
      .fc-body {
        padding: 0 !important;
      }
    }
  }

  :deep(.text-h3) {
    font-weight: 600;
  }

  :deep(.text-tertiary) {
    color: #5f6a7a !important;
  }

  :deep(.el-select--large) .el-select__wrapper {
    margin-left: 5px;
    width: 122px !important;
    height: 32px;
    line-height: 32px;
    min-height: 32px !important;
  }

  // 全局
  :deep(.el-table.noBrd) {
    .el-table--border .el-table__inner-wrapper:after,
    .el-table--border:after,
    .el-table--border:before,
    .el-table__inner-wrapper:before {
      display: none;
    }
    th,
    td {
      text-align: center;
      border: none !important;
    }
  }

  :deep(.up .el-icon),
  :deep(.down .el-icon) {
    width: 10px;
    height: 10px;
    margin-top: 1px;
  }

  :deep(.hot),
  :deep(.high-negative) {
    color: #ff5959;
    font-weight: 500;
  }

  :deep(.hotBg) {
    background-color: #ffd1c9;
  }
}
</style>

<style lang="scss">
// hack
:deep(.el-empty) {
  padding: 0 !important;
}
</style>
