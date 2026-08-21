<script setup lang="ts">
// @ts-nocheck
import { computed } from 'vue'
import type { ProductBriefVo } from '@/api/productAnalysis/types'
import type { ProductSelfBriefVo } from '@/api/thisProductAnalysis/types.d'
import { fmtNum, fmtPer, fmtFix } from '@/utils'
import HoverZhPopover from './HoverZhPopover.vue'

/**
 * 指标概览卡片 负面率，正面率，提及量，用户数
 */
defineOptions({
  name: 'ZhYsCards'
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
      icon: 'voiceprint-fill',
      label: '内容总数',
      value: fmtNum(data.totals),
      rateMoM: `${fmtPer(data.totalsMoM)}`, //环比
      rateYoY: `${fmtPer(data.totalsYoY)}` //同比
    },
    {
      icon: 'voiceprint-fill',
      label: '主帖数',
      value: fmtNum(data.posts),
      rateMoM: `${fmtPer(data.postMoM)}`,
      rateYoY: `${fmtPer(data.postYoY)}` //同比
    },
    {
      icon: 'voiceprint-fill',
      label: '评论数',
      value: fmtNum(data.comments),
      rateMoM: `${fmtPer(data.commentsMoM)}`,
      rateYoY: `${fmtPer(data.commentsYoY)}` //同比
    },
    {
      icon: 'users02',
      iconColor: '#5F6A7A',
      label: '主帖用户数',
      value: fmtNum(data.postUser),
      rateMoM: `${fmtPer(data.postUserMoM)}`,
      rateYoY: `${fmtPer(data.postUserYoY)}` //同比
    },
    {
      icon: 'users02',
      iconColor: '#5F6A7A',
      label: '评论用户数',
      value: fmtNum(data.commentUser),
      rateMoM: `${fmtPer(data.commentUserMoM)}`,
      rateYoY: `${fmtPer(data.commentUserYoY)}` //同比
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
      <HoverZhPopover
        placement="top"
        :show-after="200"
        :width="410"
        trigger="hover"
        :table-config="{
          data: [item],
          columns: [
            { title: '名称', dataIndex: 'label', width: 100 },
            { title: '数值', dataIndex: 'value', width: 80 },
            { title: '环比', dataIndex: 'rateMoM', className: 'c666' },
            { title: '同比', dataIndex: 'rateYoY', className: 'c666' }
          ]
        }"
      >
        <template #reference>
          <div
            class="iv-item"
            :style="{ background: item.rateBackgroundColor }"
            :class="item.customClass"
            @click="cardChange(item.label)"
          >
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
      </HoverZhPopover>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.info-view {
  width: 100%;
  height: 88px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
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
