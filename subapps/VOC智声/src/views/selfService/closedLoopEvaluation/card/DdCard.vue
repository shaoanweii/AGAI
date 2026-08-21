<script setup lang="ts">
// @ts-nocheck
import { computed } from 'vue'
import { fmtNum, fmtFix } from '@/utils'
import { EventAnalyType } from '@/constants'

/**
 * 指标概览卡片 负面率，正面率，提及量，用户数
 */
defineOptions({
  name: 'DdCard'
})

// Props 定义
interface Props {
  cardListData?: any
  type?: string
}

const props = withDefaults(defineProps<Props>(), {
  cardListData: null
})

const colorList = [
  {
    iconColor: '#1677ff',
    iconBgColor: '#eaf3ff'
  },
  {
    iconColor: '#13c2c2', // 图标颜色
    iconBgColor: '#e9fbfb' // 图标背景颜色颜色
  },
  {
    iconColor: '#fa8c16', // 图标颜色
    iconBgColor: '#fff7e6' // 图标背景颜色颜色
  },
  {
    iconColor: '#12b76a', // 图标颜色
    iconBgColor: '#eafaf2' // 图标背景颜色颜色
  },
  {
    iconColor: '#722ed1', // 图标颜色
    iconBgColor: '#f3edff' // 图标背景颜色颜色
  },
  {
    iconColor: '#667085', // 图标颜色
    iconBgColor: '#f2f4f7' // 图标背景颜色颜色
  }
]

// 计算属性：格式化数据显示
const formattedData = computed(() => {
  const data: any = props.cardListData || {}
  let result: any = []
  let cardBackgroundColor = '#f8fbff' // 卡片每一项的背景颜色
  // 单点事件分析jian
  if (props.type === EventAnalyType.SingleEventAnaly) {
    result = [
      {
        iconColor: colorList[0].iconColor, // 图标颜色
        iconBgColor: colorList[0].iconBgColor, // 图标背景颜色颜色
        label: '预警事件总数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[1].iconColor, // 图标颜色
        iconBgColor: colorList[1].iconBgColor, // 图标背景颜色颜色
        label: '预警初审数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[2].iconColor, // 图标颜色
        iconBgColor: colorList[2].iconBgColor, // 图标背景颜色颜色
        label: '预警复审数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[3].iconColor, // 图标颜色
        iconBgColor: colorList[3].iconBgColor, // 图标背景颜色颜色
        label: '业务响应数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[4].iconColor, // 图标颜色
        iconBgColor: colorList[4].iconBgColor, // 图标背景颜色颜色
        label: '闭环处理数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[5].iconColor, // 图标颜色
        iconBgColor: colorList[5].iconBgColor, // 图标背景颜色颜色
        label: '事件关闭数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[0].iconColor, // 图标颜色
        iconBgColor: colorList[0].iconBgColor, // 图标背景颜色颜色
        label: '未下发数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[1].iconColor, // 图标颜色
        iconBgColor: colorList[1].iconBgColor, // 图标背景颜色颜色
        label: '实际下发数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[2].iconColor, // 图标颜色
        iconBgColor: colorList[2].iconBgColor, // 图标背景颜色颜色
        label: '已处理数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      }
    ]
  } else if (props.type === EventAnalyType.BatchEventAnaly) {
    result = [
      {
        iconColor: colorList[0].iconColor, // 图标颜色
        iconBgColor: colorList[0].iconBgColor, // 图标背景颜色颜色
        label: '预警事件总数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[3].iconColor, // 图标颜色
        iconBgColor: colorList[3].iconBgColor, // 图标背景颜色颜色
        label: '业务响应数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[4].iconColor, // 图标颜色
        iconBgColor: colorList[4].iconBgColor, // 图标背景颜色颜色
        label: '闭环处理数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[5].iconColor, // 图标颜色
        iconBgColor: colorList[5].iconBgColor, // 图标背景颜色颜色
        label: '事件关闭数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[0].iconColor, // 图标颜色
        iconBgColor: colorList[0].iconBgColor, // 图标背景颜色颜色
        label: '未下发数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[1].iconColor, // 图标颜色
        iconBgColor: colorList[1].iconBgColor, // 图标背景颜色颜色
        label: '实际下发数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[2].iconColor, // 图标颜色
        iconBgColor: colorList[2].iconBgColor, // 图标背景颜色颜色
        label: '已处理数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      }
    ]
  } else if (props.type === EventAnalyType.UserUseAnaly) {
    result = [
      {
        iconColor: colorList[0].iconColor, // 图标颜色
        iconBgColor: colorList[0].iconBgColor, // 图标背景颜色颜色
        label: '登录用户数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[1].iconColor, // 图标颜色
        iconBgColor: colorList[1].iconBgColor, // 图标背景颜色颜色
        label: '访问次数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[2].iconColor, // 图标颜色
        iconBgColor: colorList[2].iconBgColor, // 图标背景颜色颜色
        label: '平均登录次数',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      },
      {
        iconColor: colorList[3].iconColor, // 图标颜色
        iconBgColor: colorList[3].iconBgColor, // 图标背景颜色颜色
        label: '平均浏览时长',
        value: `${fmtNum(data.value)}`,
        rate: `${fmtFix(data.rate)}`, //百分比
        customClass: 'fml',
        rateColor: data.rateColor,
        cardBackgroundColor
      }
    ]
  }

  return result
})
</script>

<template>
  <div class="info-view">
    <template v-for="(item, index) in formattedData" :key="index">
      <div
        class="iv-item"
        :style="{ background: item.cardBackgroundColor }"
        :class="item.customClass"
      >
        <div class="logo" :style="{ background: item.iconBgColor }">
          <!-- <SvgIcon :name="item.icon" width="32px" height="32px" :color="item.iconColor" /> -->
          <el-icon :size="24" :color="item.iconColor"><Document /></el-icon>
        </div>
        <div class="content">
          <div class="label">{{ item.label }}</div>
          <div class="value mt-8" :style="{ color: item.rateColor }">
            <div>{{ item.value }}</div>
            <div class="c-tag">{{ item.rate }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.info-view {
  width: 100%;
  // height: 88px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-width: 0; // 确保grid容器可以正确收缩

  // 响应式设计
  @media (max-width: 1200px) {
    gap: 16px;
  }

  @media (max-width: 900px) {
    gap: 12px;
  }

  .iv-item {
    height: 88px;
    background: #f2f4f7;
    border-radius: 8px;
    padding: 16px;
    display: flex;
    align-items: center;
    min-width: 0; // 确保flex子元素可以正确收缩
    border: 1px solid #ebedf0;
    cursor: pointer;

    &.fml {
      background: rgba(255, 138, 139, 0.15);
    }

    &.zml {
      background: rgba(130, 227, 199, 0.15);
    }

    // 响应式设计
    @media (max-width: 1200px) {
      padding: 12px 16px;
    }

    @media (max-width: 900px) {
      padding: 8px 12px;
    }

    .logo {
      width: 56px;
      height: 56px;
      background: #ffffff;
      border-radius: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0; // 防止图标被压缩

      @media (max-width: 900px) {
        width: 48px;
        height: 48px;
      }
    }

    .content {
      margin-left: 16px;
      min-width: 0; // 确保内容可以正确收缩
      flex: 1;

      @media (max-width: 900px) {
        margin-left: 12px;
      }

      .label {
        font-size: 14px;
        color: #5f6a7a;
        line-height: 14px;

        @media (max-width: 900px) {
          font-size: 12px;
        }
      }

      .value {
        font-weight: 500;
        font-size: 20px;
        color: #1f2733;
        line-height: 28px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        @media (max-width: 1200px) {
          font-size: 18px;
        }

        @media (max-width: 900px) {
          font-size: 16px;
          line-height: 24px;
        }

        &.drange {
          color: #ff5959;
        }

        .c-tag {
          background: #fafafa;
          border-radius: 16px 16px 16px 16px;
          border: 1px solid #e9eaeb;
          padding: 2px 6px;
          min-width: 63px;
          text-align: center;
          font-weight: 500;
          font-size: 12px;
          color: #414651;
          line-height: 18px;
          // height: 22px;
        }
      }
    }
  }
}
</style>
