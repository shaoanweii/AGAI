<script setup lang="ts">
import { ref, computed } from 'vue'
import { Image as VanImage } from 'vant'
import HEcharts from '@/views/H5/components/UI/HEcharts/index.vue'
import type { EChartsOption } from 'echarts'
import type { NegativeRateData } from '@h5/views/home/components/NegativeRateCard/types'
import arrowRightPng from '@/assets/h5/arrow-right.png'
import { usePermissionsStore } from '@/store'

defineOptions({
  name: 'TotalVoice'
})

// 组件属性
const props = withDefaults(defineProps<{ dataBrief?: NegativeRateData }>(), {
  dataBrief: () => ({
    name: '',
    negativeRate: 0,
    negativeRateMom: 0,
    mentionCount: 0,
    mentionCountMom: 0,
    achieveRate: 0,
    achieveRateTalk: ''
  })
})

const permissionsStore = usePermissionsStore()

// 调试日志
console.log('TotalVoice props.dataBrief:', props.dataBrief)

// ECharts配置
const chartOptions = computed<EChartsOption>(() => ({
  series: [
    {
      type: 'pie',
      radius: ['60%', '80%'],
      center: ['50%', '50%'],
      startAngle: 90,
      data: [
        {
          value: props.dataBrief.achieveRate,
          itemStyle: {
            borderRadius: '50%',
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 1,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: '#11EDFE'
                },
                {
                  offset: 1,
                  color: '#3872F7'
                }
              ]
            }
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          }
        },
        {
          value: 100 - props.dataBrief.achieveRate,
          itemStyle: {
            color: '#f0f0f0'
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          }
        }
      ],
      emphasis: {
        disabled: true
      }
    }
  ],
  graphic: {
    elements: [
      {
        type: 'text',
        left: 'center',
        top: 'center',
        style: {
          text: `${Number(props.dataBrief.achieveRate || 0)}%`,
          textAlign: 'center',
          textVerticalAlign: 'middle',
          fontSize: 12,
          fontWeight: '400',
          fill: '#333333'
        }
      }
    ]
  }
}))
</script>

<template>
  <!-- 先判断高管权限, 然后再判断是否有数据  && dataBrief.achieveRate && dataBrief.achieveRateTalk -->
  <div v-if="permissionsStore.executivePermission" class="total-voice-container flex-y-center">
    <!-- 左侧圆形进度条 -->
    <div class="progress-section">
      <HEcharts :options="chartOptions" height="50px" width="50px" />
    </div>

    <!-- 右侧文字说明 -->
    <div class="text-section">
      <div class="progress-text">
        <!--        本月任务已达成<span class="highlight">{{ progress }}%</span>，您需在接下来的{{ remainingDays }}天内继续浏览<span class="highlight">{{ remainingTasks }}条</span>{{ taskType }}-->
        <div v-html="dataBrief.achieveRateTalk || ''"></div>
        <!-- {{ dataBrief.achieveRateTalk || '' }} -->
      </div>
    </div>
    <div class="arrow-section">
      <van-image width="12" height="12" :src="arrowRightPng" fit="cover" />
    </div>
  </div>
</template>
<style scoped lang="scss">
.total-voice-container {
  height: 61px;
  background: #ffffff;
  border-radius: 8px 8px 8px 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-left: 10px;
  padding-right: 10px;
}

.progress-section {
  flex-shrink: 0;
}

.text-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.arrow-section {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e2f3fe;
}

.progress-text,
.task-text {
  line-height: 20px;
  font-weight: 400;
  font-size: 12px;
  color: #5f6a7a;
}

.progress-text {
  ::v-deep(.highlight) {
    padding-left: 2px;
    padding-right: 2px;
    font-weight: 400;
    color: #1677ff;
    font-size: 12px;
  }
}
</style>
<style lang="scss"></style>
