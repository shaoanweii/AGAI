<script setup lang="ts">
// @ts-nocheck
import { computed } from 'vue'
import type { ProductBriefVo } from '@/api/productAnalysis/types'
import type { ProductSelfBriefVo } from '@/api/thisProductAnalysis/types.d'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import HoverPopover from './HoverPopover.vue'

/**
 * 指标概览卡片 负面率，正面率，提及量，用户数
 */
defineOptions({
  name: 'MetricSummaryCards'
})

// 通用简报数据类型 - 兼容产品分析和本品分析
type BriefData = ProductBriefVo | ProductSelfBriefVo

// Props 定义
interface Props {
  productBriefData?: BriefData | null
}

const props = withDefaults(defineProps<Props>(), {
  productBriefData: null
})

const emits = defineEmits(['cardChange'])

// 计算属性：格式化数据显示
const formattedData = computed(() => {
  const data: any = props.productBriefData || {}
  const result = [
    {
      icon: 'nsr-zm',
      label: '负面率',
      value: `${fmtPer(data.negativeRate)}`,
      rateMoM: `${fmtFix(data.negativeRateMoM)}`, //环比
      rateYoY: `${fmtFix(data.negativeRateYoY)}`, //同比
      customClass: 'fml',
      rateColor: data.rateColor,
      rateBackgroundColor: data.rateBackgroundColor
    },
    {
      icon: 'nsr-my',
      label: '正面率',
      value: `${fmtPer(data.positiveRate)}`,
      rateMoM: `${fmtFix(data.positiveRateMoM)}`,
      rateYoY: `${fmtFix(data.positiveRateYoY)}`, //同比
      customClass: 'zml'
    },
    {
      icon: 'voiceprint-fill',
      label: '提及量',
      value: fmtNum(data.mentions),
      rateMoM: `${fmtFix(data.mentionsMoM)}`,
      rateYoY: `${fmtFix(data.mentionsYoY)}` //同比
    },
    {
      icon: 'users02',
      iconColor: '#5F6A7A',
      label: '用户数',
      value: fmtNum(data.users),
      rateMoM: `${fmtFix(data.usersMoM)}`,
      rateYoY: `${fmtFix(data.usersYoY)}` //同比
    }
  ]
  return result
})

const cardChange = (cardType: string) => {
  emits('cardChange', { cardType, data: props.productBriefData })
}
</script>

<template>
  <div class="info-view">
    <template v-for="(item, index) in formattedData" :key="index">
      <HoverPopover
        placement="top"
        :show-after="200"
        :width="410"
        trigger="hover"
        :table-config="{
          data: [item],
          columns: [
            { title: '名称', dataIndex: 'label', width: 70 },
            { title: '数值', dataIndex: 'value', width: 80 },
            { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
            { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
          ]
        }"
      >
        <template #reference>
          <div class="iv-item" :style="{ background: item.rateBackgroundColor }" :class="item.customClass"
               @click="cardChange(item.label)">
            <div class="logo">
              <SvgIcon :name="item.icon" width="32px" height="32px" :color="item.iconColor" />
            </div>
            <div class="content">
              <div class="label">{{ item.label }}</div>
              <div class="value mt-8" :style="{ color: item.rateColor }">
                <div>{{ item.value }}</div>
                <div class="c-tag">{{ item.rateMoM }}</div>
              </div>
            </div>
          </div>
        </template>
      </HoverPopover>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.info-view {
  width: 100%;
  height: 88px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  min-width: 0;

  // HoverPopover 渲染的触发容器是 grid 直接子项，必须允许它突破内容最小宽度。
  > * {
    width: 100%;
    min-width: 0;
  }

  // 响应式设计
  @media (max-width: 1200px) {
    gap: 12px;
  }

  @media (max-width: 900px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    height: auto;
  }

  .iv-item {
    height: 88px;
    background: #f2f4f7;
    border-radius: 8px;
    padding: 16px;
    display: flex;
    align-items: center;
    width: 100%;
    min-width: 0;
    overflow: hidden;
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
