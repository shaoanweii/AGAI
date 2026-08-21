<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductScenarioAnalysisQueryParams } from '@/views/leaderOverview/leader/common/fn.ts'
import { useQueryStore } from '@/store/modules/query'
import { fields, chartOpts } from './chartOpts.ts'
import { getProductAnalysis, getServiceAnalysis } from '@/api/overview/leader'
import { debounce } from 'lodash-es'
// import { FEcharts } from '@/components/global'

defineOptions({
  name: 'SceneAnalysis'
})

// 基础设置
const loading = ref<any>({
  prod: true,
  server: true
})

const dataOpts = ref<any>({
  prod: { ...chartOpts },
  server: { ...chartOpts }
})

const queryStore = useQueryStore()
const storePms = queryStore.currentQueryParams

// 数据名去重
const clearRepeat = (list: any[]): any => {
  // 为重复name加序号
  const copy: {
    [key: string]: number
  } = {}

  list.forEach(item => {
    if (!copy[item.tagName]) copy[item.tagName] = 1
    else {
      copy[item.tagName]++
      item.tagName = item.tagName + '(' + copy[item.tagName] + ')'
    }
  })
  return list
}

// 调接口
const fetchDataBy = async (type: string) => {
  let isProd = type === 'prod'
  const errMsg = `获取${isProd ? '产品' : '服务'}场景分析数据失败`
  try {
    // 重置
    loading.value[type] = true

    const queryParams = getProductScenarioAnalysisQueryParams(storePms)

    const response = isProd
      ? await getProductAnalysis(queryParams)
      : await getServiceAnalysis(queryParams)

    if (response.success && response.result) {
      dataOpts.value[type].dataset = {
        dimensions: ['tagName', ...fields],
        source: clearRepeat(response.result)
      }
    } else {
      ElMessage.error(response.message || errMsg)
    }
  } catch (error) {
    console.error(`${errMsg}:, ${error}`)
    ElMessage.error(`${errMsg}，请稍后重试`)
  } finally {
    loading.value[type] = false
  }
}

// 事件
const chartClick = (params: any) => {
  queryStore.updateQueryParams({
    tag2Code: params.data && params.data.tagCode,
    intention: '',
    topic: '',
    tagType: 'DOM'
  })
  // console.log('params', params.data && params.data.tagCode)
}

const fetchData = () => {
  fetchDataBy('prod')
  fetchDataBy('server')
}

const fetchDataDelay = debounce(fetchData, 300)

onMounted(() => {
  fetchDataDelay()
})

watch(
  () => ({
    startDate: storePms.startDate,
    endDate: storePms.endDate
  }),
  () => {
    fetchDataDelay()
  }
)

watch(
  () => [storePms.tempCode, storePms.channelCatagory],
  () => {
    fetchDataDelay()
  }
)
</script>

<template>
  <div class="chartWrap">
    <div class="cc-chart" v-loading="loading['prod']">
      <div class="text-h3 mt-20 ml-20 mr-8">产品场景分析</div>
      <FEcharts
        :options="dataOpts['prod']"
        @chart-click="chartClick($event)"
        width="100%"
        :height="'346px'"
      ></FEcharts>
    </div>
    <div class="cc-chart" v-loading="loading['server']">
      <div class="text-h3 mt-20 ml-20 mr-8">服务场景分析</div>
      <FEcharts
        :options="dataOpts['server']"
        @chart-click="chartClick($event)"
        width="100%"
        :height="'346px'"
      ></FEcharts>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chartWrap {
  display: flex;
  overflow: hidden;

  .cc-chart {
    // height: 553px;
    // box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    // border-radius: 12px;
    // border: 1px solid #ebedf0;
    // margin-top: 24px;

    height: 404px;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    border-radius: 12px;
    border: 1px solid #ebedf0;
    &:first-child {
      width: calc(53.8% - 12px);
      margin-right: 24px;
    }
    &:last-child {
      width: calc(46.2% - 12px);
    }
  }

  :deep(.text-h3) {
    line-height: 32px !important;
    margin-top: 24px !important;
    font-size: 16px !important;
  }
}

.test {
  color: #7298d0;
  color: #82e3c7;
  color: #60b8eb;
  color: #ff8a8b;
  color: #9a60b4;
  color: #ea7ccc;
}
</style>
