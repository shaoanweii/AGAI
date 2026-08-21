<script setup lang="ts">
import type { GenderDistributionVo, UserTypeDistributionVo } from '@/api/journeyAnalysis/types'
import { CHART_THEME_COLORS } from '@/constants'
import type { EChartsOption } from 'echarts'
import { ref, onMounted, computed } from 'vue'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'

defineOptions({
  name: 'Middle'
})

const { genderData, userTypeData: userTypeDataProp } = defineProps<{
  genderData: GenderDistributionVo[]
  userTypeData: UserTypeDistributionVo[]
}>()

interface UserTypeChartItem extends UserTypeDistributionVo {
  color: string
  percentValue: number
  rawData: UserTypeDistributionVo
}

/**
 * 将接口占比字段统一转换为数值，避免空值或字符串影响柱图计算。
 * @param value 原始占比值
 * @returns 可用于图表计算的数值
 */
const normalizeToNumber = (value: number | string | null | undefined) => {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : 0
}

// 处理性别数据
const handleGenderData = computed<any>(() => {
  const result = genderData.reduce(
    (pre, cur) => {
      if (cur.gender?.startsWith('男')) {
        pre.male = cur
      } else if (cur.gender?.startsWith('女')) {
        pre.female = cur
      }
      return pre
    },
    { male: {}, female: {} }
  )
  return {
    male: result.male,
    female: result.female
  }
})

/**
 * 按后端返回顺序构建客户类型图表数据，避免前端重排影响业务展示顺序。
 */
const userTypeChartData = computed<UserTypeChartItem[]>(() => {
  return userTypeDataProp.map((item, index) => ({
    ...item,
    color: CHART_THEME_COLORS[index % CHART_THEME_COLORS.length],
    percentValue: normalizeToNumber(item.percent),
    rawData: item
  }))
})

const userTypeAxisLabelRich = computed<Record<string, Record<string, string | number>>>(() => {
  return userTypeChartData.value.reduce<Record<string, Record<string, string | number>>>(
    (result, item, index) => {
      result[`label${index}`] = {
        color: item.color,
        fontSize: 14,
        fontWeight: 500,
        lineHeight: 20,
        align: 'center',
        width: 76
      }
      return result
    },
    {}
  )
})

/**
 * 统一构建用户类型图表 tooltip，保持与原 HoverPopover 一致的信息口径。
 * @param item 用户类型原始数据
 * @returns tooltip HTML 字符串
 */
const buildUserTypeTooltip = (item: UserTypeDistributionVo) => {
  return `
    <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
      <div class="mb-12 fs-14 fw-500" style="color: #333">
        ${item.userType || ''}
      </div>
      <table style="width: 100%; border-collapse: collapse; margin: 0;">
        <thead>
          <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
            <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">占比</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">环比</th>
            <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">同比</th>
          </tr>
        </thead>
        <tbody>
          <tr style="background: white;">
            <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: none;">用户数</td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">
              ${fmtNum(item.value)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: none;">
              ${fmtPer(item.percent)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: none;">
              ${fmtFix(item.valueMoM)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: none;">
              ${fmtFix(item.valueYoY)}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `
}

// 动画控制
const animationStarted = ref(false)
const USER_TYPE_BAR_WIDTH = 20
const USER_TYPE_CHART_TOP_PADDING_RATIO = 0.1

/**
 * 根据当前最大占比动态计算纵轴上限，让柱体尽量撑满绘图区，同时为顶部标签预留空隙。
 */
const userTypeChartYAxisMax = computed(() => {
  const maxPercentValue = userTypeChartData.value.reduce((currentMax, item) => {
    return Math.max(currentMax, item.percentValue)
  }, 0)

  if (maxPercentValue <= 0) {
    return 1
  }

  return Number((maxPercentValue * (1 + USER_TYPE_CHART_TOP_PADDING_RATIO)).toFixed(2))
})

const userTypeChartOptions = computed<EChartsOption>(() => {
  if (!userTypeChartData.value.length) {
    return {}
  }

  return {
    animationDuration: 800,
    animationEasing: 'cubicOut',
    grid: {
      left: 0,
      right: 0,
      top: 36,
      bottom: 32,
      containLabel: true
    },
    tooltip: {
      trigger: 'item',
      confine: true,
      formatter: (params: any) => {
        const target = params?.data?.rawData as UserTypeDistributionVo | undefined
        return target ? buildUserTypeTooltip(target) : ''
      }
    },
    xAxis: {
      type: 'category',
      data: userTypeChartData.value.map(item => item.userType),
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#D9DEE7',
          width: 1
        }
      },
      axisLabel: {
        interval: 0,
        margin: 14,
        formatter: (value: string, index: number) => {
          return `{label${index}|${value}}`
        },
        rich: userTypeAxisLabelRich.value
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: userTypeChartYAxisMax.value,
      show: false
    },
    series: [
      {
        name: '占比',
        type: 'bar',
        barWidth: USER_TYPE_BAR_WIDTH,
        data: userTypeChartData.value.map(item => ({
          value: item.percentValue,
          rawData: item.rawData
        })),
        itemStyle: {
          color: (params: any) => userTypeChartData.value[params.dataIndex]?.color || '#1677FF'
        }
      },
      {
        name: '标签层',
        type: 'bar',
        silent: true,
        barGap: '-100%',
        barWidth: USER_TYPE_BAR_WIDTH,
        data: userTypeChartData.value.map(item => item.percentValue),
        itemStyle: {
          color: 'transparent'
        },
        tooltip: {
          show: false
        },
        label: {
          show: true,
          position: 'top',
          distance: 10,
          color: '#26292E',
          fontSize: 14,
          fontWeight: 500,
          formatter: (params: any) => {
            return fmtNum(userTypeChartData.value[params.dataIndex]?.value ?? 0)
          }
        }
      }
    ]
  }
})

// 事件定义
const emit = defineEmits<{
  (e: 'gender-click', data: GenderDistributionVo): void
  (e: 'user-type-click', data: UserTypeDistributionVo): void
}>()

// 处理性别点击事件
const handleGenderClick = (gender: 'male' | 'female') => {
  const genderData = gender === 'male' ? handleGenderData.value.male : handleGenderData.value.female
  emit('gender-click', genderData)
}

// 处理用户类型点击事件
const handleUserTypeClick = (item: UserTypeDistributionVo) => {
  emit('user-type-click', item)
}

/**
 * 处理用户类型柱状图点击事件，统一将不同系列点击映射回原始业务数据。
 * @param params ECharts 点击事件参数
 */
const handleUserTypeChartClick = (params: any) => {
  const dataIndex = Number(params?.dataIndex)
  if (!Number.isInteger(dataIndex) || dataIndex < 0) {
    return
  }

  const target = userTypeChartData.value[dataIndex]?.rawData
  if (target) {
    handleUserTypeClick(target)
  }
}

onMounted(() => {
  // 延迟启动动画
  setTimeout(() => {
    animationStarted.value = true
  }, 300)
})
</script>

<template>
  <div class="population-analysis">
    <div class="center-bg pt-11">
      <!-- <SvgIcon name="crowd" width="100%" height="431px"></SvgIcon> -->
      <img src="@/assets/images/crowd.png" class="h-431" alt="" />
    </div>
    <!-- 性别分析 -->
    <div class="gender-section px-50">
      <div class="gender-stats">
        <HoverPopover
          placement="top"
          :show-after="200"
          :width="500"
          trigger="hover"
          :table-config="{
            title: handleGenderData.male.gender,
            data: [
              {
                ...handleGenderData.male,
                name: '用户数',
                value: fmtNum(handleGenderData.male.value),
                percent: fmtPer(normalizeToNumber(handleGenderData.male.percent)),
                valueMoM: fmtFix(handleGenderData.male.valueMoM),
                valueYoY: fmtFix(handleGenderData.male.valueYoY)
              }
            ],
            columns: [
              { title: '名称', dataIndex: 'name', width: 70 },
              { title: '数值', dataIndex: 'value', width: 90 },
              { title: '占比', dataIndex: 'percent', width: 90 },
              { title: '环比', dataIndex: 'valueMoM', className: 'c666' },
              { title: '同比', dataIndex: 'valueYoY', className: 'c666' }
            ]
          }"
        >
          <template #reference>
            <div
              class="gender-item male"
              @click="handleGenderClick('male')"
              style="cursor: pointer"
            >
              <SvgIcon name="union" width="27px" height="33px" color="#60B8EB"></SvgIcon>
              <div class="gender-info">
                <div class="percentage">
                  {{ fmtPer(normalizeToNumber(handleGenderData.male.percent)) }}
                </div>
                <div class="label">{{ handleGenderData.male.gender }}</div>
              </div>
            </div>
          </template>
        </HoverPopover>

        <HoverPopover
          placement="top"
          :show-after="200"
          :width="500"
          trigger="hover"
          :table-config="{
            title: handleGenderData.female.gender,
            data: [
              {
                ...handleGenderData.female,
                name: '用户数',
                value: fmtNum(handleGenderData.female.value),
                percent: fmtPer(normalizeToNumber(handleGenderData.female.percent)),
                valueMoM: fmtFix(handleGenderData.female.valueMoM),
                valueYoY: fmtFix(handleGenderData.female.valueYoY)
              }
            ],
            columns: [
              { title: '名称', dataIndex: 'name', width: 70 },
              { title: '数值', dataIndex: 'value', width: 90 },
              { title: '占比', dataIndex: 'percent', width: 90 },
              { title: '环比', dataIndex: 'valueMoM', className: 'c666' },
              { title: '同比', dataIndex: 'valueYoY', className: 'c666' }
            ]
          }"
        >
          <template #reference>
            <div
              class="gender-item female"
              @click="handleGenderClick('female')"
              style="cursor: pointer"
            >
              <div class="gender-info">
                <div class="percentage">
                  {{ fmtPer(normalizeToNumber(handleGenderData.female.percent)) }}
                </div>
                <div class="label">{{ handleGenderData.female.gender }}</div>
              </div>
              <SvgIcon name="union" width="27px" height="33px" color="#FF8A8B"></SvgIcon>
            </div>
          </template>
        </HoverPopover>
      </div>

      <!-- 性别比例条 -->
      <div class="gender-bar">
        <div
          class="male-bar"
          :style="{
            width: animationStarted ? `${normalizeToNumber(handleGenderData.male.percent)}%` : '0%'
          }"
          @click="handleGenderClick('male')"
          style="cursor: pointer"
        ></div>
        <div
          class="female-bar"
          :style="{
            width: animationStarted
              ? `${normalizeToNumber(handleGenderData.female.percent)}%`
              : '0%'
          }"
          @click="handleGenderClick('female')"
          style="cursor: pointer"
        ></div>
      </div>
    </div>

    <!-- 用户类型分析 -->
    <div class="user-type-section px-24">
      <div v-if="userTypeChartData.length" class="user-type-chart-panel">
        <FEcharts
          class="user-type-chart"
          :options="userTypeChartOptions"
          :width="'100%'"
          :height="'100%'"
          @chart-click="handleUserTypeChartClick"
        />
      </div>
      <el-empty v-else description="暂无数据" style="padding: 0" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.population-analysis {
  .center-bg {
    width: 100%;
    height: 431px;
    display: flex;
    justify-content: center;
    img {
      width: 90%;
      object-fit: contain;
    }
  }
}

.gender-section {
  margin-bottom: 24px;

  .gender-stats {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .gender-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      font-size: 14px;
      line-height: 24px;
      &.male {
        .percentage {
          color: #60b8eb;
        }
      }

      &.female {
        text-align: right;
        .percentage {
          color: #ff8a8b;
        }
      }

      .gender-info {
        .percentage {
          font-size: 18px;
          font-weight: 600;
        }

        .label {
          font-weight: 500;
          font-size: 14px;
          color: #5f6a7a;
          line-height: 24px;
        }
      }
    }
  }

  .gender-bar {
    height: 32px;
    overflow: hidden;
    display: flex;
    background: #f5f5f5;
    gap: 4px;

    .male-bar {
      background: linear-gradient(90deg, #5cb3ff 0%, #4da6ff 100%);
      transition: width 1.5s ease-out;
    }

    .female-bar {
      background: linear-gradient(90deg, #ff9999 0%, #ff7b7b 100%);
      transition: width 1.5s ease-out;
    }
  }
}

.user-type-section {
  .user-type-chart-panel {
    height: 180px;
  }

  .user-type-chart {
    width: 100%;
    height: 100%;

    :deep(canvas) {
      cursor: pointer;
    }
  }
}

@media (max-width: 768px) {
  .population-analysis {
    padding: 16px;

    .user-type-section {
      .user-type-chart-panel {
        height: 160px;
      }
    }
  }
}
</style>
