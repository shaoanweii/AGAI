<template>
  <div class="echarts-container" :style="{ height, width }">
    <!-- 有数据时显示图表 -->
    <div ref="chartRef" class="chart-content" :style="{ height: '100%', width: '100%' }"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, markRaw } from 'vue'
import * as echarts from 'echarts'
import type { PropType, ShallowRef } from 'vue'
import type { EChartsOption, ECharts } from 'echarts'

defineOptions({
  name: 'HEcharts'
})

const props = defineProps({
  // 图表选项
  options: {
    type: Object as PropType<EChartsOption>,
    required: true
  },
  // 图表高度
  height: {
    type: String,
    default: '400px'
  },
  // 图表宽度
  width: {
    type: String,
    default: '100%'
  },
  // 图表主题
  theme: {
    type: String,
    default: ''
  },
  // 自动调整大小
  autoResize: {
    type: Boolean,
    default: true
  },
  // 自动更新图表
  autoUpdate: {
    type: Boolean,
    default: true
  },
  // 是否展示X轴tooltip
  isShowXAxisTooltip: {
    type: Boolean,
    default: false
  },
  // 事件配置对象
  events: {
    type: Object as PropType<Record<string, (params: any) => void>>,
    default: () => ({})
  },
  // 是否显示为空状态
  isEmpty: {
    type: Boolean,
    default: false
  },
  // 空状态描述文本
  emptyDescription: {
    type: String,
    default: '暂无数据'
  }
})

// 定义事件
const emit = defineEmits([
  'chartReady',
  'chartClick',
  'chartDblclick',
  'chartMouseover',
  'chartMouseout'
])

// 图表容器引用
const chartRef = ref<HTMLElement | null>(null)
// 图表实例引用
let chartInstance: ShallowRef<ECharts | null> = ref(null)
// ResizeObserver 实例
let resizeObserver: ResizeObserver | null = null

/**
 * @description: xAxis文本悬浮显示
 * @param {*} chart
 * @return {*}
 */
const extension = (chart: any) => {
  // 注意这里，是以X轴显示内容过长为例，如果是y轴的话，需要把params.componentType == 'xAxis'改为yAxis
  // 判断是否创建过div框,如果创建过就不再创建了
  // 该div用来盛放文本显示内容的，方便对其悬浮位置进行处理
  var elementDiv: any = document.getElementById('extension')
  if (!elementDiv) {
    var div = document.createElement('div')
    div.setAttribute('id', 'extension')
    div.style.display = 'block'
    document.querySelector('html')?.appendChild(div)
  }
  chart?.on('mouseover', function (params: any) {
    if (params.componentType == 'xAxis') {
      if (params.value?.length <= 5) {
        return
      }
      var elementDiv: any = document.querySelector('#extension')
      //设置悬浮文本的位置以及样式
      var elementStyle =
        'position: absolute;z-index: 99999;color: #fff;font-size: 12px;padding: 5px 8px;display: inline;border-radius: 4px;background-color: #303133;box-shadow: rgba(0, 0, 0, 0.3) 2px 2px 8px'
      elementDiv.style.cssText = elementStyle
      elementDiv.innerHTML = params.value
      document.querySelector('html')!.onmousemove = function (event) {
        var elementDiv: any = document.querySelector('#extension')
        var xx = event.pageX - 10
        var yy = event.pageY + 15
        elementDiv.style.top = yy + 'px'
        elementDiv.style.left = xx + 'px'
      }
    }
  })
  chart?.on('mouseout', function (params: any) {
    //注意这里，我是以X轴显示内容过长为例，如果是y轴的话，需要改为yAxis
    if (params.componentType == 'xAxis') {
      var elementDiv: any = document.querySelector('#extension')

      elementDiv.style.cssText = 'display:none'
    }
  })
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value || !props.options || props.isEmpty) return

  // 销毁之前的实例
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  // 创建新的图表实例
  chartInstance.value = markRaw(echarts.init(chartRef.value, props.theme))

  // 设置图表选项
  chartInstance.value.setOption(props.options, { notMerge: true })

  if (props.isShowXAxisTooltip) {
    extension(chartInstance.value)
  }

  // 绑定事件
  chartInstance.value.on('click', params => {
    emit('chartClick', params)
  })

  chartInstance.value.on('dblclick', params => {
    emit('chartDblclick', params)
  })

  chartInstance.value.on('mouseover', params => {
    emit('chartMouseover', params)
  })

  chartInstance.value.on('mouseout', params => {
    emit('chartMouseout', params)
  })

  // 绑定自定义事件
  if (props.events) {
    Object.entries(props.events).forEach(([eventName, callback]) => {
      if (typeof callback === 'function') {
        chartInstance.value?.on(eventName, callback)
      }
    })
  }

  // 发出图表就绪事件
  emit('chartReady', chartInstance.value)
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value || !props.options || props.isEmpty) return

  try {
    // 先清除之前的tooltip，避免DOM引用错误
    if (chartInstance.value) {
      chartInstance.value.dispatchAction({
        type: 'hideTip'
      })
    }

    // 使用 merge: false 完全替换配置，避免引用问题
    chartInstance.value.setOption(props.options, {
      notMerge: true,
      replaceMerge: ['series', 'tooltip']
    })
  } catch (error) {
    console.warn('图表更新时发生错误，尝试重新初始化:', error)
    // 如果更新失败，重新初始化图表
    nextTick(() => {
      initChart()
    })
  }
}

// 窗口大小变化处理
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

// 设置 ResizeObserver
const setupResizeObserver = () => {
  if (!chartRef.value || !window.ResizeObserver) return

  cleanupResizeObserver()

  resizeObserver = new ResizeObserver(() => {
    handleResize()
  })

  resizeObserver.observe(chartRef.value)
}

// 清理 ResizeObserver
const cleanupResizeObserver = () => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
}

// 监听选项变化
watch(
  () => props.options,
  () => {
    if (props.autoUpdate) {
      nextTick(() => {
        updateChart()
      })
    }
  },
  { deep: true }
)

// 监听主题变化
watch(
  () => props.theme,
  () => {
    nextTick(() => {
      initChart()
    })
  }
)

// 监听宽度和高度变化
watch(
  () => [props.width, props.height],
  () => {
    nextTick(() => {
      handleResize()
    })
  }
)

// 监听自动调整选项变化
watch(
  () => props.autoResize,
  newVal => {
    if (newVal) {
      window.addEventListener('resize', handleResize)
      setupResizeObserver()
    } else {
      window.removeEventListener('resize', handleResize)
      cleanupResizeObserver()
    }
  }
)

// 监听isEmpty变化
watch(
  () => props.isEmpty,
  newVal => {
    if (newVal) {
      // 数据为空时，销毁图表实例
      if (chartInstance.value) {
        chartInstance.value.dispose()
        chartInstance.value = null
      }
    } else {
      // 数据不为空时，重新初始化图表
      nextTick(() => {
        initChart()
      })
    }
  }
)

// 组件挂载
onMounted(() => {
  nextTick(() => {
    initChart()

    if (props.autoResize) {
      window.addEventListener('resize', handleResize)
      setupResizeObserver()
    }
  })
})

// 组件卸载
onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }

  if (props.autoResize) {
    window.removeEventListener('resize', handleResize)
  }

  cleanupResizeObserver()
})

// 暴露方法
defineExpose({
  getChartInstance: () => chartInstance.value,
  resize: handleResize,
  update: updateChart
})
</script>

<style lang="scss" scoped>
.echarts-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  // 确保空状态在容器中居中显示
  .el-empty {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  // 图表内容容器
  .chart-content {
    position: relative;
  }
}
</style>
