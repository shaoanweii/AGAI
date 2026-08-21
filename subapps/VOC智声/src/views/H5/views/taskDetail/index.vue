<script setup lang="ts">
import HCard from '@h5/components/UI/HCard/index.vue'
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import TaskCompletionRate from './TaskCompletionRate.vue'
import BrowsingHistory from './BrowsingHistory.vue'
import { ref, onMounted, reactive, computed } from 'vue'
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import { useRoute, useRouter } from 'vue-router'
import { taskCompletion, browseTrend, browseRecords, type H5VocBaseRequest } from '@h5/api/home'
import type { TaskCompletionVo, BrowseTrendVo, BrowseRecordVo } from '@h5/api/home/types'
import { fmtNum, formatAxisLabel } from '@/utils'
import dayjs from 'dayjs'
import VoiceDetailDialog from '@h5/components/VoiceDetailDialog/index.vue'
import { useH5MenuVisitRecord } from '@h5/hooks/useH5MenuVisitRecord'

defineOptions({
  name: 'taskDetail'
})

const router = useRouter()
const route = useRoute()

// H5-任务完成率：页面访问操作记录（非 keep-alive 页面首次进入走 onMounted）
useH5MenuVisitRecord()

// 基于路由query生成默认请求参数
const getDefaultRequestParams = (q: Record<string, any>): H5VocBaseRequest => {
  return {
    // ...route.query,
    // provinceCodeSet: JSON.parse((route.query.provinceCodeSet as string) || '[]'),
  }
}

// 接口数据
const taskCompletionData = ref<TaskCompletionVo>({})
const browseTrendData = ref<BrowseTrendVo[]>([])
const browseRecordsData = ref<BrowseRecordVo[]>([])
const requestParams = ref<H5VocBaseRequest>(getDefaultRequestParams(route.query))
const loading = reactive({
  taskCompletion: false,
  browseTrend: false,
  // 浏览记录首次加载/切换筛选时的整体loading
  browseRecords: false
})
const voiceDetailVisible = ref<boolean>(false)
const voiceDetailData = ref<any>({})
//当前点击的浏览柱状图
const selectedData = ref<any>({})


// 获取任务完成率数据
const fetchTaskCompletion = async () => {
  try {
    loading.taskCompletion = true
    const res = await taskCompletion({...requestParams.value})
    if (res.success) {
      taskCompletionData.value = res.result || {}
    } else {
      console.error('获取任务完成率数据失败:', res.message)
    }
  } catch (error) {
    console.error('获取任务完成率数据异常:', error)
  } finally {
    loading.taskCompletion = false
  }
}

// 获取浏览趋势数据
const fetchBrowseTrend = async () => {
  try {
    loading.browseTrend = true
    const res = await browseTrend({...requestParams.value})
    if (res.success) {
      browseTrendData.value = res.result || []
      // updateStackedBarChart()
    } else {
      console.error('获取浏览趋势数据失败:', res.message)
    }
  } catch (error) {
    console.error('获取浏览趋势数据异常:', error)
  } finally {
    loading.browseTrend = false
  }
}

// 分页状态与上拉加载
const listState = reactive({
  pageNum: 1,
  pageSize: 10, // 默认每次加载10条
  loading: false, // 绑定 van-list 的 loading
  finished: false // 是否已无更多数据
})

// 加载一页浏览记录（van-list @load）
const onLoad = async () => {
  if (listState.finished) return
  try {
    // 首次页使用整体 loading，分页时使用 van-list 内置 loading
    if (listState.pageNum === 1) {
      loading.browseRecords = true
    }
    const res = await browseRecords({
      ...requestParams.value,
      pageNum: listState.pageNum,
      pageSize: listState.pageSize
    }, { cancelPrevious: true })
    if (res.success) {
      const rows = res.result?.list || []
      if (listState.pageNum === 1) {
        // 首次页重置
        browseRecordsData.value = rows
      } else {
        // 追加
        browseRecordsData.value = [...browseRecordsData.value, ...rows]
      }
      // 根据返回数量判断是否还有下一页
      if (rows.length < listState.pageSize) {
        listState.finished = true
      } else {
        listState.pageNum += 1
      }
    } else {
      console.error('获取浏览记录数据失败:', res.message)
      // 避免反复触发加载
      listState.finished = true
    }
  } catch (error) {
    console.error('获取浏览记录数据异常:', error)
  } finally {
    listState.loading = false
    loading.browseRecords = false
  }
}

// 重置并加载（可带日期筛选）
const resetAndLoadBrowseRecords = (params: { startDate?: string; endDate?: string } = {}) => {
  listState.pageNum = 1
  listState.finished = false
  browseRecordsData.value = []
  if (params.startDate && params.endDate) {
    requestParams.value.startDate = params.startDate as any
    requestParams.value.endDate = params.endDate as any
  } else {
    // 清除日期筛选
    delete (requestParams.value as any).startDate
    delete (requestParams.value as any).endDate
  }
  // 触发首次加载
  listState.loading = true
  onLoad()
}

onMounted(() => {
  fetchTaskCompletion()
  fetchBrowseTrend()
  // 首次进入默认分页加载
  resetAndLoadBrowseRecords()
})

const stackedBarChart = computed(() => {
  const xAxisData = browseTrendData.value.map(item => (dayjs(item.dateTime || '').format('MM-DD')))
  const values = browseTrendData.value.map(item => item.totalCount || 0)
  const selectedIndex = browseTrendData.value.findIndex((d: any) => d?.dateTime && d.dateTime === selectedData.value?.dateTime)
  return {
    color: ['#60B8EB'],
    grid: {
      top: 30,
      left: 40,
      right: 40,
      bottom: 40
    },
    tooltip: {
      show: false,
      trigger: 'axis' as const,
      axisPointer: {
        type: 'shadow' as const
      }
    },
    legend: {
      show: false,
      data: ['抱怨', '咨询', '建议', '表扬'],
      icon: 'roundRect',
      itemWidth: 12,
      itemHeight: 12,
      bottom: -5,
      left: 'center',
      textStyle: {
        color: '#6E7B91'
      }
    },
    xAxis: {
      type: 'category' as const,
      data: xAxisData as string[],
      axisLine: {
        lineStyle: {
          color: '#F1F1F5'
        }
      },
      axisLabel: {
        interval: 0,
        hideOverlap: true,
        showMinLabel: true,
        showMaxLabel: true,
        rotate: xAxisData.length > 28 ? 60 : xAxisData.length > 5 ? 45 : 0,
        fontSize: xAxisData.length > 28 ? 10 : 12,
        color: '#5F6A7A',
        margin: 16
      } as any,
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value' as const,
      minInterval: 1,
      name: '',
      splitLine: {
        lineStyle: {
          color: '#F1F1F5'
        }
      },
      axisLabel: {
        formatter: function(value: number) {
          return formatAxisLabel(value)
        },
        color: '#92929D'
      } as any
    },
    series: [
      {
        name: '提及量',
        type: 'bar' as const,
        data: values,
        barMaxWidth: 16,
        // 根据选中状态动态设置柱体颜色（选中高亮，未选中默认）
        itemStyle: {
          color: (p: any) => (p.dataIndex === selectedIndex ? '#1677FF' : '#60B8EB')
        },
        label: {
          show: true,
          position: 'top' as const,
          color: '#666666',
          fontSize: 10,
          formatter: (p: any) => fmtNum(values[p.dataIndex] || 0)
        }
      }
      /*{
        name: '抱怨',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: []
      },
      {
        name: '咨询',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: []
      },
      {
        name: '建议',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: []
      },
      {
        name: '表扬',
        type: 'bar',
        stack: 'total',
        barWidth: 16,
        data: []
      }*/
    ]
  }
})

// 更新堆叠柱状图数据
const updateStackedBarChart = () => {
  const dateTimeData = browseTrendData.value.map(item => (dayjs(item.dateTime || '').format('MM-DD')))
  const complainData = browseTrendData.value.map(item => item.complainCount || 0)
  const consultData = browseTrendData.value.map(item => item.consultCount || 0)
  const suggestionData = browseTrendData.value.map(item => item.suggestionCount || 0)
  const praiseData = browseTrendData.value.map(item => item.praiseCount || 0)

  // stackedBarChart.value.xAxis.data = dateTimeData
  // stackedBarChart.value.xAxis.axisLabel.rotate = dateTimeData.length > 28 ? 60 : dateTimeData.length > 5? 45: 0
  // stackedBarChart.value.xAxis.axisLabel.fontSize = dateTimeData.length > 28 ? 10 : 12
  // stackedBarChart.value.series[0].data = complainData
  // stackedBarChart.value.series[1].data = consultData
  // stackedBarChart.value.series[2].data = suggestionData
  // stackedBarChart.value.series[3].data = praiseData
}

const handleBack = () => {
  router.back()
}

//TODO 点击抱怨/咨询/建议/表扬  跳转声音列表  需要联动筛选
const handleTypeClick = (type: string) => {
  // //本月
  // const startDate = dayjs().startOf('month').format('YYYY-MM-DD')
  // const endDate = dayjs().format('YYYY-MM-DD')
  // router.push({
  //   name: 'H5AllVoiceList',
  //   params: {
  //     //传递标记，接口调用的不一样
  //     tag: 'history'
  //   },
  //   query: {
  //     // 传递当前筛选参数
  //     // ...requestParams.value,
  //     startDate,
  //     endDate,
  //     // 传递当前点击的类型
  //     intention: type
  //   }
  // })
}

//点击浏览趋势图表
const handleChartClick = (params: any) => {
  const dataIndex = params.dataIndex
  if (dataIndex >= 0 && dataIndex < browseTrendData.value.length) {
    const clickedData = browseTrendData.value[dataIndex]
    const isSame = clickedData?.dateTime && selectedData.value?.dateTime && clickedData.dateTime === selectedData.value.dateTime
    if (isSame) {
      selectedData.value = {}
    } else {
      selectedData.value = clickedData
    }
    // 切换日筛选并重置分页
    resetAndLoadBrowseRecords(
      selectedData.value?.dateTime
        ? { startDate: selectedData.value.dateTime, endDate: selectedData.value.dateTime }
        : {}
    )
  }
}

/**
 * 浏览记录点击事件
 * */
const handleHistotyItem = (item: BrowseRecordVo) => {
  voiceDetailData.value = item
  voiceDetailVisible.value = true
}

</script>

<template>
  <HPage>
    <!-- 导航栏插槽 -->
    <template #nav-bar>
      <HNavBar left-text="返回" @click-left="handleBack" />
    </template>
    <div class="taskDetail">
      <HCard>
        <TaskCompletionRate v-loading="loading.taskCompletion" :data="taskCompletionData" @type-click="handleTypeClick"></TaskCompletionRate>
      </HCard>
      <HCard title="浏览趋势" height="289px" class="mt-12">
        <HEcharts v-loading="loading.browseTrend" :options="stackedBarChart" width="100%" height="100%" @chart-click="handleChartClick"></HEcharts>
      </HCard>

      <HCard title="浏览记录" height="auto" class="mt-12">
        <!-- 空数据态显示 -->
        <template v-if="!loading.browseRecords && listState.finished && browseRecordsData.length === 0">
          <van-empty description="暂无数据" />
        </template>
        <template v-else-if="loading.browseRecords">
          <van-skeleton class="mt-10" title :row="5" />
        </template>
        <!-- 上拉加载（每页10条） -->
        <van-list
          v-else
          v-model:loading="listState.loading"
          :finished="listState.finished"
          :immediate-check="false"
          @load="onLoad"
          v-loading="loading.browseRecords"
        >
          <BrowsingHistory :data="browseRecordsData" @item-click="handleHistotyItem" />
          <template #finished>
            <div v-if="browseRecordsData.length > 0" class="flex-center">
              <van-divider class="finish-text">已显示全部浏览记录</van-divider>
            </div>
          </template>
        </van-list>
      </HCard>
    </div>
    <VoiceDetailDialog
      v-if="voiceDetailVisible"
      v-model:show="voiceDetailVisible"
      :id="voiceDetailData.soundId"
      :originalId="voiceDetailData.originalId"
      @refresh-browse-ecord="fetchTaskCompletion"
    />
  </HPage>
</template>

<style lang="scss" scoped>
.taskDetail {
  //width: 100vw;
  //height: 100vh;
  background-color: #f5f7fa;
  padding: 12px;
  //overflow: auto;
}

.finish-text {
  width: 180px;
  margin: 0 !important;
  padding-bottom: 10px;
  text-align: center;
  font-weight: 400;
  font-size: 12px;
  color: #929aa6;
}
</style>
