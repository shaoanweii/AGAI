<script setup lang="ts">
import HEcharts from '@h5/components/UI/HEcharts/index.vue'
import { ref, computed, watch } from 'vue'
import type { TaskCompletionVo } from '@h5/api/home/types'

defineOptions({
  name: 'TaskCompletionRate'
})

interface Props {
  data?: TaskCompletionVo
}

const props = defineProps<Props>()

// 对外事件：点击类型项
const emit = defineEmits<{
  // 点击类型项时抛出：'抱怨' | '咨询' | '建议' | '表扬'
  (e: 'type-click', type: string): void
}>()

// 统一处理点击事件
const handleTypeClick = (type: string) => {
  try {
    if (!type) return
    emit('type-click', type)
  } catch (err) {
    console.warn('任务完成率类型点击事件触发失败:', err)
  }
}

// 计算完成率百分比
const completionRatePercent = computed(() => {
  return (props.data?.completionRate || 0) / 100
})

const gaugeChart = ref<any>({
  grid: {
    left: 0,
    top: 0,
    right: 0,
    bottom: 0
  },
  graphic: [
    {
      type: 'text',
      left: 'center',
      top: '35%',
      style: {
        text: '本月任务完成率',
        fontSize: 12,
        fontWeight: 500,
        fill: 'rgba(31, 39, 51, 0.65)',
        textAlign: 'center'
      }
    },
    {
      type: 'text',
      left: 'center',
      top: '49%',
      style: {
        text: `${props.data?.completionRate || 0}%`,
        fontSize: 24,
        fontWeight: 500,
        fill: '#1677ff',
        textAlign: 'center'
      }
    },
    {
      type: 'text',
      left: 'center',
      top: '65%',
      style: {
        text: `累计浏览时长 ${props.data?.totalBrowsingDuration || '0秒'}`,
        fontSize: 12,
        fontWeight: 400,
        fill: '#1f2733',
        textAlign: 'center',
        backgroundColor: '#F5F7FA',
        borderColor: '#DFE2E8',
        borderWidth: 1,
        borderRadius: 20,
        padding: [4, 12, 3, 12]
      }
    }
  ],
  series: [
    // 外圈进度条
    {
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      radius: '120%',
      center: ['50%', '63%'],
      axisLine: {
        show: true,
        roundCap: true,
        lineStyle: {
          width: 15,
          color: [
            [1, '#DFE2E8'],
          ]
        }
      },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      pointer: { show: false },
      detail: { show: false },
      title: { show: false },
      data: [{ value: 0 }]
    },
    // 内圈刻度
    {
      type: 'gauge',
      startAngle: 200,
      endAngle: -20,
      radius: '105%',
      center: ['50%', '63%'],
      axisLine: {
        show: false,
        lineStyle: {
          width: 1,
          color: [[1, '#ddd']]
        }
      },
      axisTick: {
        show: true,
        splitNumber: 5,
        length: 10,
        lineStyle: {
          color: '#999',
          width: 1
        }
      },
      splitLine: {
        show: false
      },
      axisLabel: {
        show: false
      },
      pointer: { show: false },
      detail: { show: false },
      title: { show: false },
      data: [{ value: 0 }]
    }
  ]
})

// 监听数据变化更新图表
watch(() => props.data?.completionRate, (newRate: number | undefined) => {
  if (newRate !== undefined) {
    const rate = newRate / 100
    gaugeChart.value.series[0].axisLine.lineStyle.color = [
      [rate, '#2f89fc'],
      [1, rate >= 1? 'transparent' : '#DFE2E8']
    ]

    gaugeChart.value.series[0].data = [{ value: newRate }]

    // 更新graphic中的完成率文字
    gaugeChart.value.graphic[1].style.text = `${newRate}%`
  }
}, { immediate: true })

// 监听浏览时长变化更新图表
watch(() => props.data?.totalBrowsingDuration, (newDuration: string | undefined) => {
  if (newDuration !== undefined) {
    gaugeChart.value.graphic[2].style.text = `累计浏览时长 ${newDuration}`
  }
}, { immediate: true })
</script>

<template>
  <div class="TaskCompletionRateView">
    <div style="height: 200px;">
      <HEcharts :options="gaugeChart" width="100%" height="100%"></HEcharts>
    </div>
    <!--    <div class="task-status">
          <div class="ts-item">
            <div class="tsi-info">总浏览量</div>
            <div class="tsi-num">{{ props.data?.browseData?.totalCount || 0 }}</div>
          </div>

          <div class="ts-line"></div>

          <div class="ts-item" @click="handleTypeClick('抱怨')">
            <div class="tsi-info">抱怨</div>
            <div class="tsi-num">{{ props.data?.browseData?.complainCount || 0 }}</div>
          </div>
          <div class="ts-item" @click="handleTypeClick('咨询')">
            <div class="tsi-info">咨询</div>
            <div class="tsi-num">{{ props.data?.browseData?.consultCount || 0 }}</div>
          </div>
          <div class="ts-item" @click="handleTypeClick('建议')">
            <div class="tsi-info">建议</div>
            <div class="tsi-num">{{ props.data?.browseData?.suggestionCount || 0 }}</div>
          </div>
          <div class="ts-item" @click="handleTypeClick('表扬')">
            <div class="tsi-info">表扬</div>
            <div class="tsi-num">{{ props.data?.browseData?.praiseCount || 0 }}</div>
          </div>
        </div>-->
  </div>
</template>

<style lang="scss" scoped>
.TaskCompletionRateView {
  width: 100%;
  height: 100%;

  .task-status {
    display: flex;
    justify-content: center;
    gap: 11px;

    .ts-line {
      width: 1px;
      height: 36px;
      background-color: #dfe2e8;
    }
    .ts-item {
      min-width: 48px;
      text-align: center;
      cursor: pointer;

      &:nth-child(3) {
        .tsi-num {
          color: #fe7840;
        }
      }
      &:nth-child(4) {
        .tsi-num {
          color: #0aadff;
        }
      }
      &:nth-child(5) {
        .tsi-num {
          color: #28c7c7;
        }
      }
      &:nth-child(6) {
        .tsi-num {
          color: #14ca64;
        }
      }
      .tsi-info {
        font-weight: 500;
        font-size: 10px;
        color: #1f2733;
      }

      .tsi-num {
        font-weight: 500;
        font-size: 14px;
        color: #1f2733;
        line-height: 14px;
        margin-top: 8px;
      }
    }
  }
}
</style>
