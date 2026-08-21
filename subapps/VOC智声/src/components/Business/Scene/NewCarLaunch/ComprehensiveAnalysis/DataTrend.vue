<template>
  <div class="data-trend-chart">
    <FEcharts
      :options="chartOptions"
      :isEmpty="isEmpty"
      :emptyDescription="emptyDescription"
      :width="width"
      :height="height"
      @chartClick="handleChartClick"
      @dataZoom="onDataZoom"
      @chart-ready="handleChartReady($event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ShallowRef } from 'vue'
import FEcharts from '@/components/Charts/FEcharts/index.vue'
import { fmtPer, fmtNum } from '@/utils'
import type { EChartsOption, ECharts } from 'echarts'
// import { useQueryStore } from '@/store/modules/query'
import { calcDiffType, getDateDimension } from '@/utils/date'
import dayjs from 'dayjs'

interface TrendPoint {
  date?: string
  positiveMentions?: number // 正面提及量
  neutralMentions?: number // 中性提及量
  negativeMentions?: number // 负面提及量
  negativeRate?: number // 负面率
  positiveRate?: number // 正面率
  phase?: string
  [key: string]: any
}

interface TrendDataSource {
  preheat?: { trend?: TrendPoint[] }
  launch?: { trend?: TrendPoint[] }
  stable?: { trend?: TrendPoint[] }
}

const props = withDefaults(
  defineProps<{
    queryStore: any
    dataTrendChangeData?: TrendDataSource | null
    height?: string
    width?: string
    isShowLegend?: boolean
    emptyDescription?: string
    showPhaseArea?: boolean
  }>(),
  {
    dataTrendChangeData: null,
    height: '420px',
    width: '100%',
    isShowLegend: true,
    emptyDescription: '暂无数据',
    showPhaseArea: true
  }
)

// const queryStore = useQueryStore()

const graphicList = ref<any>([]) // 区域绘图列表

const newCarSeriesObj = ref<any>({}) // 区域绘图列表

// 监听选项变化
watch(
  () => props.dataTrendChangeData,
  newVal => {
    graphicList.value = newVal?.preheat?.trend || []
  },
  { deep: true }
)
watch(
  () => props.queryStore,
  newVal => {
    newCarSeriesObj.value = newVal?.currentQueryParams?.newCarSeriesObjList?.[0]
  },
  { deep: true }
)
// 图表实例引用
let chartInstance: ShallowRef<ECharts | null> = ref(null)

const emit = defineEmits<{
  (e: 'chart-click', params: any): void
}>()

// 处理图表就绪事件
const handleChartReady = (chart: ECharts) => {
  chartInstance.value = chart
}

const mergedTrend = computed<TrendPoint[]>(() => {
  const data = props.dataTrendChangeData

  if (!data) return []

  const addPhase = (list: any[] = [], phase: string) =>
    list
      .filter(item => item && item.date)
      .map(item => {
        return { ...item, phase }
      })

  const trendMap = new Map<string, any>()
  addPhase(data.preheat?.trend, '预热期').forEach(item => trendMap.set(item.date, item))
  // addPhase(data.launch?.trend, '上市期').forEach(item => trendMap.set(item.date, item))
  // addPhase(data.stable?.trend, '稳定期').forEach(item => trendMap.set(item.date, item))

  const calcData = (Array.from(trendMap.values()) as TrendPoint[]).sort((a, b) =>
    String(a.date).localeCompare(String(b.date))
  )
  return calcData
})

const isEmpty = computed(() => mergedTrend.value.length === 0)

const chartData = computed<{
  xAxisData: string[]
  positiveMentions: Array<TrendPoint & { name: string; value: number }>
  neutralMentions: Array<TrendPoint & { name: string; value: number }>
  negativeMentions: Array<TrendPoint & { name: string; value: number }>
  negativeRates: Array<TrendPoint & { name: string; value: number }>
  positiveRates: Array<TrendPoint & { name: string; value: number }>
  totalMentions: Array<TrendPoint & { name: string; value: number }>
}>(() => {
  const xAxisData = mergedTrend.value.map((item: TrendPoint) => item.date || '')
  const positiveMentions = mergedTrend.value.map((item: TrendPoint) => ({
    ...item,
    name: item.date || '',
    value: item.positiveMentions || 0
  }))
  const neutralMentions = mergedTrend.value.map((item: TrendPoint) => ({
    ...item,
    name: item.date || '',
    value: item.neutralMentions || 0
  }))
  const negativeMentions = mergedTrend.value.map((item: TrendPoint) => ({
    ...item,
    name: item.date || '',
    value: item.negativeMentions || 0
  }))
  const negativeRates = mergedTrend.value.map((item: TrendPoint) => ({
    ...item,
    name: item.date || '',
    value: item.negativeRate || 0
  }))

  const positiveRates = mergedTrend.value.map((item: TrendPoint) => ({
    ...item,
    name: item.date || '',
    value: item.positiveRate || 0
  }))

  const totalMentions = mergedTrend.value.map((item: TrendPoint) => ({
    ...item,
    name: item.date || '',
    value: item.totalMentions || 0
  }))
  return {
    xAxisData,
    positiveMentions,
    neutralMentions,
    negativeMentions,
    negativeRates,
    totalMentions,
    positiveRates
  }
})

const onDataZoom = (params: any) => {
  // params 里包含 start/end 或 startValue/endValue
  // 获取当前 dataZoom 范围
  const { start, end } = params.batch ? params.batch[0] : params
  // 计算索引
  const total = chartData.value.xAxisData.length
  const startIdx = Math.floor((start / 100) * total)
  const endIdx = Math.ceil((end / 100) * total) - 1
  // 截取当前可视范围的 x 轴数据
  const visibleXAxis = chartData.value.xAxisData.slice(startIdx, endIdx + 1)

  // 你可以在这里做进一步处理，比如 emit 或存到 ref
  // 在mergedTrend.value中找到所有的对应日期的数据
  const visData: TrendPoint[] = []
  mergedTrend.value.forEach((item: TrendPoint) => {
    if (visibleXAxis.includes(item.date || '')) {
      visData.push(item)
    }
  })

  if (visData?.length) {
    graphicList.value = visData
  }
}

// 返回该日期月的首日
const getMonthFirstDay = (dateStr: string) => {
  const [year, month] = dateStr.split('-')
  return `${year}-${month}-01`
}

// 传入一个数组 返回 phaseGraphics需要根据每个点的phase字段来判断是哪个时期 返回配置
const handTimeTrendGraphics = computed(() => {
  if (
    graphicList.value?.length === 0 ||
    !newCarSeriesObj.value?.preheatStartTime ||
    !newCarSeriesObj.value?.preheatEndTime ||
    !newCarSeriesObj.value?.launchStartTime ||
    !newCarSeriesObj.value?.launchEndTime ||
    !newCarSeriesObj.value?.stableStartTime ||
    !newCarSeriesObj.value?.stableEndTime ||
    !chartInstance.value
  ) {
    return []
  }
  console.log('当前车辆信息', newCarSeriesObj.value)
  // 预热期
  // 预热开始时间
  const preheatStartTime = newCarSeriesObj.value?.preheatStartTime || ''
  // 预热结束时间
  const preheatEndTime = newCarSeriesObj.value?.preheatEndTime || ''
  // 上市开始时间
  const launchStartTime = newCarSeriesObj.value?.launchStartTime || ''
  // 上市结束时间
  const launchEndTime = newCarSeriesObj.value?.launchEndTime || ''
  // 稳定开始时间
  const stableStartTime = newCarSeriesObj.value?.stableStartTime || ''
  // 稳定结束时间
  const stableEndTime = newCarSeriesObj.value?.stableEndTime || ''
  // 根据日期判定目前有几种状态 预热期 上市期 稳定期

  // 获取chartInstance图表的实时宽度高度
  const defalutLeftWidth = 60 // 默认的左右边距
  const widthDom = chartInstance.value?.getWidth() || 0

  const areaStyles: Record<
    string,
    {
      fill: string
      borderColor: string
      label: string
      textColor: string
    }
  > = {
    预热期: {
      fill: 'rgb(239, 246, 255)',
      borderColor: '#91D5FF',
      label: '预热期',
      textColor: '#1677FF'
    },
    上市期: {
      fill: 'rgb(234, 251, 254)',
      borderColor: '#B7EB8F',
      label: '上市期',
      textColor: '#14CA64'
    },
    稳定期: {
      fill: 'rgb(254, 243, 234)',
      borderColor: '#FFD591',
      label: '稳定期',
      textColor: '#FE793F'
    }
  }

  // 计算有几个时间段
  const phaseSet: any[] = []
  // 所有的时间节点 为了计算时间范围占比
  const timeList: any[] = []
  //  获取当前日期 返回YYYY-MM-DD格式的字符串
  // const nowDayStr = dayjs().format('YYYY-MM-DD')

  const suoyoushijian = graphicList.value.map((e: TrendPoint) => e.date) || []
  // 取最后一条数据的日期字段
  const lastDataDate = graphicList.value[graphicList.value.length - 1].date
  // 判断是月还是日期 后面需要判断截止时间
  const dt = getDateDimension(lastDataDate)

  const todoGraLogic = (text: string, startTime: string, endTime: string) => {
    if (dt === 'day') {
      // 说明后端返回的数据维度  是日期
      // 前提是开始时间在列表中 否则说明已经超过了整个时间跨度
      if (suoyoushijian.includes(startTime)) {
        // endTime 是不是跨度超过今天时间到未来了
        if (endTime > lastDataDate) {
          // 以最后一条数据为准
          timeList.push([startTime, lastDataDate])
        } else {
          //
          timeList.push([startTime, endTime])
        }
        phaseSet.push(text)
      } else {
        // 还需要处理缩放区域滚动 放大显示到只有一两个数据的情况下 该类型下的开始时间比显示的区域还小
        const isInQuer = graphicList.value.every((item: TrendPoint) => {
          const x = item.date || ''
          return startTime < x && x <= endTime
        })
        if (isInQuer) {
          const firstDataDate = graphicList.value[0].date
          timeList.push([firstDataDate, endTime])
          phaseSet.push(text)
        }
      }
    } else if (dt === 'month') {
      // 说明后端返回的数据维度  是月
      const preheatStartJieQu = startTime.substring(0, 7) //开始 取截至时间的年月
      const preheatEndJieQu = endTime.substring(0, 7) //结束 取截至时间的年月

      // 前提是开始时间在列表中 否则说明已经超过了整个时间跨度

      // 这个判断是下面区域从左往右拖动放大数据部分不显示问题
      const isInQuer = graphicList.value.every((item: TrendPoint) => {
        const x = item.date || ''
        return preheatStartJieQu <= x && x <= preheatEndJieQu
      })

      if (suoyoushijian.includes(preheatStartJieQu) || isInQuer) {
        const nowDayStr = dayjs().format('YYYY-MM-DD')
        if (!suoyoushijian.includes(preheatEndJieQu)) {
          // // 说明跨过了当前月来到了未来
          // 截至时间 用当前时间
          if (startTime < nowDayStr) {
            timeList.push([startTime, nowDayStr])
            phaseSet.push(text)
          }
        } else {
          // 判断到期时间是否是当前日期后面
          if (endTime > nowDayStr) {
            if (startTime < nowDayStr) {
              timeList.push([startTime, nowDayStr])
              phaseSet.push(text)
            }
          } else {
            timeList.push([startTime, endTime])
            phaseSet.push(text)
          }
        }
      }
    }
  }

  let judgeStart = ''
  let judgeEnd = ''
  const t = [
    preheatStartTime,
    preheatEndTime,
    launchStartTime,
    launchEndTime,
    stableStartTime,
    stableEndTime
  ].sort()
  // 真实的列表graphicList
  graphicList.value.forEach((item: TrendPoint) => {
    // 判断是不是应该包含预热期
    // 月的时候并且缩放区域放大到很大的时候需要特殊处理
    let yrq: boolean[] = []
    let ssq: boolean[] = []
    let wdq: boolean[] = []
    if (dt === 'month') {
      graphicList.value.forEach((item: TrendPoint) => {
        const x = item.date || ''

        const preheatStartJieQu = preheatStartTime.substring(0, 7) //开始 取截至时间的年月
        const preheatEndJieQu = preheatEndTime.substring(0, 7) //结束 取截至时间的年月
        yrq.push(preheatStartJieQu <= x && x <= preheatEndJieQu)

        const launchStartJieQu = launchStartTime.substring(0, 7) //开始 取截至时间的年月
        const launchEndJieQu = launchEndTime.substring(0, 7) //结束 取截至时间的年月
        ssq.push(launchStartJieQu <= x && x <= launchEndJieQu)

        const stableStartJieQu = stableStartTime.substring(0, 7) //开始 取截至时间的年月
        const stableEndJieQu = stableEndTime.substring(0, 7) //结束 取截至时间的年月
        wdq.push(stableStartJieQu <= x && x <= stableEndJieQu)
      })
    }

    if (
      (preheatStartTime.indexOf(item.date) !== -1 ||
        preheatEndTime.indexOf(item.date) !== -1 ||
        yrq.every(x => x === true)) &&
      phaseSet.indexOf('预热期') === -1
    ) {
      todoGraLogic('预热期', preheatStartTime, preheatEndTime)
    }
    // 判断是不是应该包含上市期
    if (
      (launchStartTime.indexOf(item.date) !== -1 ||
        launchEndTime.indexOf(item.date) !== -1 ||
        ssq.every(x => x === true)) &&
      phaseSet.indexOf('上市期') === -1
    ) {
      todoGraLogic('上市期', launchStartTime, launchEndTime)
    }
    // 判断是不是应该包含稳定期
    if (
      (stableStartTime.indexOf(item.date) !== -1 ||
        stableEndTime.indexOf(item.date) !== -1 ||
        wdq.every(x => x === true)) &&
      phaseSet.indexOf('稳定期') === -1
    ) {
      todoGraLogic('稳定期', stableStartTime, stableEndTime)
    }
  })
  // 计算最小的时间和最大的相隔多少天 后面按照这个天数计算百分比 一共有多少天
  // 计算真实的天数
  let totalDayNum: any = 0
  if (dt === 'day') {
    totalDayNum = calcDiffType(timeList?.[0]?.[0] || '', timeList?.[timeList.length - 1]?.[1] || '')
  } else if (dt === 'month') {
    // 月的情况下需要详细判断天数

    // 从小到大排序 并且得到最终的时间
    const curMouthDay = dayjs().format('YYYY-MM-DD') // 当前月

    const t1 = [
      [t[0], t[1]],
      [t[2], t[3]],
      [t[4], t[5]]
    ]
    // 每个月日期的处理 需要找到真实的开始结束日期
    judgeStart = t[0]

    /**
     * @param {string[][]} arr - 日期区间数组
     * @param {Date} [now] - 当前日期（可选，默认取今天）
     * @returns {string} - 目标月份的最后一天字符串
     */
    function getTargetMonthLastDay(arr: any, now = new Date()) {
      const currentYear = now.getFullYear()
      const currentMonth = now.getMonth() + 1 // 1-12

      for (const [start, end] of arr) {
        const endDate = new Date(end)
        const endYear = endDate.getFullYear()
        const endMonth = endDate.getMonth() + 1
        if (endYear < currentYear || (endYear === currentYear && endMonth < currentMonth)) {
          continue
        }
        if (endYear === currentYear && endMonth === currentMonth) {
          // 返回本月最后一天
          const lastDay = new Date(currentYear, currentMonth, 0).getDate()
          return `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
        }
        if (endYear > currentYear || (endYear === currentYear && endMonth > currentMonth)) {
          // 返回上一个区间的结束月的最后一天
          const prevMonth = endMonth - 1
          const prevYear = prevMonth === 0 ? endYear - 1 : endYear
          const realPrevMonth = prevMonth === 0 ? 12 : prevMonth
          const lastDay = new Date(prevYear, realPrevMonth, 0).getDate()
          return `${prevYear}-${String(realPrevMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
        }
      }
      // 如果都小于当前月，返回最后一个区间的结束月的最后一天
      const last = arr[arr.length - 1][1]
      const lastDate = new Date(last)
      const lastYear = lastDate.getFullYear()
      const lastMonth = lastDate.getMonth() + 1
      const lastDay = new Date(lastYear, lastMonth, 0).getDate()
      return `${lastYear}-${String(lastMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
    }

    judgeEnd = getTargetMonthLastDay(t1, new Date(curMouthDay))

    // 第一个月要从第一天计算开始才准确 最后一天要计算到该月的最后一天
    totalDayNum = calcDiffType(getMonthFirstDay(judgeStart), judgeEnd)
  }
  console.log('当前视图范围内所有的天数总和', totalDayNum, phaseSet, timeList, dt)

  if (typeof totalDayNum !== 'number') {
    // 说明日期错误
    return []
  }
  // calcDiffType计算出来的天数是差 实际应该+1
  totalDayNum = totalDayNum + 1

  // 这个是图表的实际绘画区域的宽度值
  const calcWidth = widthDom - 2 * defalutLeftWidth
  console.log('图表总宽度', calcWidth)

  // 第一条左侧多余的宽度天数
  let firstLeftdiff = 0
  // 最后一条右侧多余的天数
  let lastRightdiff = 0
  // 处理宽度逻辑
  const xr = phaseSet.flatMap((dateStr, sorar) => {
    const style = areaStyles[dateStr]
    // 全部统一处理
    // 当前时间段实际天数 calcDiffType计算出来的天数是差 实际应该+1
    const calcDateTodoStart = timeList[sorar][0]
    const calcDateTodoEnd = timeList[sorar][1]
    let everyDiff = calcDiffType(calcDateTodoStart, calcDateTodoEnd) || 0

    if (dt === 'month') {
      // 处理第一个月的数据
      if (sorar === 0) {
        firstLeftdiff = calcDiffType(getMonthFirstDay(judgeStart), calcDateTodoStart) || 0
        // 第一条额外需要加一个偏移宽度天数
        everyDiff = everyDiff + firstLeftdiff
      } else if (sorar === timeList.length - 1) {
        // 最后一条也需要添加一个额外的偏移宽度天数
        lastRightdiff = calcDiffType(calcDateTodoEnd, judgeEnd) || 0
        everyDiff = everyDiff + lastRightdiff
      }
    }
    const realEveryDiff = everyDiff + 1 // 实际天数

    // 计算evWidth宽度 每个柱子的宽度 第一个的宽度需要+1日期宽度
    let evWidth = (realEveryDiff / totalDayNum) * calcWidth // 换算成像素 // 换算成像素
    let marginLeft = 0
    if (sorar > 0) {
      // 距离左侧的距离减去一个日期柱子的宽度 calcDiffType计算出来的天数是差 实际应该+1
      const mlInnerDiff = calcDiffType(timeList?.[0]?.[0], calcDateTodoStart) || 0 // 开始时间距离第一个的宽度天数

      marginLeft = (mlInnerDiff / totalDayNum) * calcWidth
      // 月 后面的每一个都需要添加一个额外的左侧偏移天数
    }
    if (dt === 'month') {
      if (timeList.length === 1) {
        // 一个柱子的时候
        evWidth = calcWidth
        marginLeft = 0
      } else if (timeList.length === 2) {
        // 2个柱子的时候
        // 第一个柱子
        const xxx = timeList?.[sorar]
        const s = xxx?.[0]
        const e = xxx?.[1]
        const mlInnerDiff = calcDiffType(s, e) || 0
        if (sorar === 0) {
          evWidth = ((mlInnerDiff + firstLeftdiff) / totalDayNum) * calcWidth
          marginLeft = 0
        } else {
          // 第2个柱子
          evWidth = ((mlInnerDiff + lastRightdiff + 2) / totalDayNum) * calcWidth
          marginLeft = calcWidth - evWidth
        }
      } else {
        // 3个柱子的时候
        if (sorar === 0) {
          // 第1个柱子
          const xxxFi = timeList?.[0]
          const sFi = xxxFi?.[0]
          const eFi = xxxFi?.[1]
          const mlInnerDiffFi = calcDiffType(sFi, eFi) || 0
          evWidth = ((mlInnerDiffFi + firstLeftdiff) / totalDayNum) * calcWidth
          marginLeft = 0
        } else if (sorar === 1) {
          // 第2个柱子
          // 第1个柱子的信息的
          const xxxFi = timeList?.[0]
          const sFi = xxxFi?.[0]
          const eFi = xxxFi?.[1]
          // 第一个柱子宽度 天数
          const mlInnerDiffPre = calcDiffType(sFi, eFi) || 0
          // 第一个柱子宽度 像素
          const fw = ((mlInnerDiffPre + firstLeftdiff) / totalDayNum) * calcWidth

          // 第2个柱子宽度
          const xxxSe = timeList?.[1]
          const sSe = xxxSe?.[0]
          const eSe = xxxSe?.[1]
          // 第2个柱子宽度 天数
          const mlInnerDiffSe = calcDiffType(sSe, eSe) || 0
          // 第2个柱子宽度 像素
          const swww = (mlInnerDiffSe / totalDayNum) * calcWidth
          evWidth = swww
          marginLeft = fw
        } else if (sorar === 2) {
          // 第3个柱子

          // 第3个柱子宽度
          const xxxTh = timeList?.[2]
          const sTh = xxxTh?.[0]
          const eTh = xxxTh?.[1]
          // 第2个柱子宽度 天数
          const mlInnerDiffTh = calcDiffType(sTh, eTh) || 0
          // 第2个柱子宽度 像素
          evWidth = ((mlInnerDiffTh + lastRightdiff) / totalDayNum) * calcWidth

          // 第三个左侧距离天数
          const dysTh = calcDiffType(timeList?.[0]?.[0], sTh) || 0
          marginLeft = ((dysTh + firstLeftdiff - 2) / totalDayNum) * calcWidth
        }
      }
    }
    console.log(dateStr, '每个柱子宽度', evWidth + 'px', '距离左侧距离', marginLeft)

    const a = [
      {
        type: 'rect',
        left: marginLeft + defalutLeftWidth,
        top: 40,
        shape: {
          width: evWidth,
          height: 280
        },
        style: {
          fill: style.fill
        }
      },
      {
        type: 'text',
        left: marginLeft + evWidth / 2,
        top: 50,
        style: {
          text: style.label,
          fill: style.textColor,
          fontSize: 16,
          fontWeight: 600,
          textAlign: 'center'
        }
      }
    ]
    return a
  })
  // console.log('xr -----xr', xr)

  return xr
})

const chartOptions = computed(() => {
  const maxNegativeRate = Math.max(
    ...chartData.value.negativeRates.map((item: TrendPoint) => Number(item.value) || 0),
    100
  )
  const yRateMax = maxNegativeRate <= 100 ? 100 : Math.ceil(maxNegativeRate / 10) * 10
  const maxMentionVal = Math.max(
    ...(chartData.value.totalMentions?.map((item: any) => Number(item?.value) || 0) || []),
    0
  )
  const getSimpleMax = (val: number): number => {
    const safeVal = Number.isFinite(val) && val > 0 ? val : 1000
    const padded = safeVal * 1.1
    return Math.ceil(padded / 100) * 100
  }
  const mentionMax = getSimpleMax(maxMentionVal)

  return {
    grid: {
      left: 20,
      right: 20,
      top: 40,
      bottom: props.isShowLegend ? 80 : 60,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis' as const,
      axisPointer: {
        type: 'shadow' as const
      },
      confine: true,
      formatter: (params: any) => {
        if (!Array.isArray(params) || params.length === 0) return ''
        const data = params[0]?.data || params[params.length - 1]?.data
        if (!data) return ''
        return `
          <div style="padding: 10px; min-width: 220px; color: #333; background: #fff; border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.12);">
            <div style="font-size: 14px; font-weight: 600; margin-bottom: 8px;">${data.date || ''}</div>
            <div style="font-size: 12px; color: #4A4A4A; line-height: 1.75;">
              名称：${newCarSeriesObj.value?.name || '--'}<br />
              正面率：${fmtPer(data.positiveRate)}<br />
              负面率：${fmtPer(data.negativeRate)}<br />
              正面提及量：${fmtNum(data.positiveMentions)}<br />
              中性提及量：${fmtNum(data.neutralMentions)}<br />
              负面提及量：${fmtNum(data.negativeMentions)}
            </div>
          </div>
        `
      }
    },
    legend: {
      show: props.isShowLegend,
      bottom: 10,
      left: 'center',
      textStyle: { color: '#6E7B91', fontSize: 12 }
    },
    dataZoom: [
      {
        type: 'slider',
        bottom: 45,
        height: 24,
        handleIcon:
          'M8.2,13.4H3.8c-0.5,0-0.8-0.4-0.8-0.8V3.8c0-0.5,0.4-0.8,0.8-0.8h4.4c0.5,0,0.8,0.4,0.8,0.8v8.8C9,13,8.7,13.4,8.2,13.4z',
        handleSize: '120%',
        fillerColor: 'rgba(34, 112, 255, 0.18)',
        backgroundColor: '#F2F5FF',
        borderColor: '#D9E4FF'
      },
      {
        type: 'inside'
      }
    ],
    xAxis: {
      type: 'category' as const,
      data: chartData.value.xAxisData,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#D9D9D9' } },
      axisLabel: { color: '#6E7B91', fontSize: 12 }
    },
    yAxis: [
      {
        type: 'value' as const,
        name: '正负面占比',
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        },
        min: 0,
        max: yRateMax,
        alignTicks: true,
        splitNumber: 5,
        axisLabel: { formatter: '{value}%', color: '#6E7B91', fontSize: 12 },
        splitLine: { show: false }
      },
      {
        type: 'value' as const,
        name: '提及量',
        min: 0,
        max: mentionMax,
        position: 'right' as const,
        alignTicks: true,
        splitNumber: 5,
        axisLine: {
          lineStyle: {}
        },
        nameTextStyle: {
          padding: [0, 0, 0, 36]
        },
        axisLabel: {
          formatter: '{value}'
        },
        splitLine: { show: false }
      }
    ],
    graphic: handTimeTrendGraphics.value,
    series: [
      {
        name: '正面率',
        type: 'line' as const,
        yAxisIndex: 0,
        smooth: true,
        lineStyle: { width: 2, type: 'dashed' as const, color: '#82E3C7' },
        itemStyle: { color: '#82E3C7' },
        data: chartData.value.positiveRates
      },
      {
        name: '负面率',
        type: 'line' as const,
        yAxisIndex: 0,
        smooth: true,
        lineStyle: { width: 2, type: 'dashed' as const, color: '#FF8A8B' }, // dashed
        itemStyle: { color: '#FF8A8B' },
        data: chartData.value.negativeRates
      },
      {
        name: '正面提及量',
        type: 'bar' as const,
        yAxisIndex: 1,
        stack: 'mentions',
        barWidth: 24,
        barCategoryGap: '10%',
        barGap: 4,
        itemStyle: { color: '#82E3C7', borderColor: '#fff', borderWidth: 2 },
        data: chartData.value.positiveMentions
      },
      {
        name: '中性提及量',
        type: 'bar' as const,
        yAxisIndex: 1,
        stack: 'mentions',
        barWidth: 24,
        barGap: 4,
        barCategoryGap: '10%',
        itemStyle: { color: '#60B8EB', borderColor: '#fff', borderWidth: 2 },
        data: chartData.value.neutralMentions
      },
      {
        name: '负面提及量',
        type: 'bar' as const,
        yAxisIndex: 1,
        stack: 'mentions',
        barWidth: 24,
        barGap: 4,
        barCategoryGap: '10%',
        itemStyle: { color: '#FF8A8B', borderColor: '#fff', borderWidth: 2 },
        data: chartData.value.negativeMentions
      }
    ]
  }
})

const handleChartClick = (params: any) => {
  emit('chart-click', params)
}
</script>

<style scoped>
.data-trend-chart {
  width: 100%;
  height: 100%;
}
</style>
