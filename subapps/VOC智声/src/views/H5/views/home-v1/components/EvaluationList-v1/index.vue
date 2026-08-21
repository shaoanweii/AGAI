<script setup lang="ts">
import { fmtNum, fmtFix } from '@/utils'
import HSortNum from '@h5/components/UI/HSortNum/index.vue'

defineOptions({
  name: 'EvaluationList-v1'
})

type EvaluationSortField = 'mentions' | 'mentionsMoM'
type EvaluationSortOrder = 'asc' | 'desc'

export interface EvaluationListProps {
  isShowMore: boolean //是否显示加载更多
  loading?: boolean
  /** 规点数据列表 */
  evaluationData?: Array<{
    id?: any
    /** 观点名 */
    opinion: string
    /** 提及量 */
    mentions: number
    /** 提及量环比，% 两位小数 */
    mentionsMoM: number
    /** 提及量同比，% 两位小数 */
    mentionsYoY: number
    /** 观点 */
    remark: string[]
  }>
  /** 选中项（v-model） */
  modelValue?: any
  /** 当前排序字段 */
  sortField?: EvaluationSortField
  /** 当前排序方向 */
  sortOrder?: EvaluationSortOrder
}

const props = withDefaults(defineProps<EvaluationListProps>(), {
  isShowMore: false,
  loading: false,
  evaluationData: () => [],
  modelValue: () => ({}),
  sortField: undefined,
  sortOrder: undefined
})

const emits = defineEmits<{
  (e: 'load-more'): void
  (e: 'item-click', item: any): void
  (e: 'update:modelValue', item: any): void
  (
    e: 'sort-change',
    payload: {
      sortField?: EvaluationSortField
      sortOrder?: EvaluationSortOrder
    }
  ): void
}>()

/**
 * 表头排序字段配置。
 * 说明：
 * - 与后端约定保持一致，点击后直接将字段名透传给父层请求参数。
 */
const SORT_FIELDS: Array<{
  field: EvaluationSortField
  label: string
  widthClass: 'mation-width' | 'mation-mom-width'
}> = [
  {
    field: 'mentions',
    label: '提及量',
    widthClass: 'mation-width'
  },
  {
    field: 'mentionsMoM',
    label: '环比',
    widthClass: 'mation-mom-width'
  }
]

// 判断是否为当前选中项（以 opinion 作为标识）
const isActive = (item: any) => {
  if (!item) return false
  return (
    props.modelValue?.opinion &&
    props.modelValue.opinion === item.opinion &&
    props.modelValue.id === item.id
  )
}

/**
 * 点击列表行时切换当前选中项。
 */
const handleClick = (item: any) => {
  let itemResult = isActive(item) ? {} : item
  emits('update:modelValue', itemResult)
  emits('item-click', itemResult)
}

/**
 * 计算指定列下一次点击后的排序方向。
 * 规则：
 * - 首次点击：降序
 * - 第二次点击：升序
 * - 第三次点击：恢复默认（不排序）
 */
const getNextSortOrder = (field: EvaluationSortField): EvaluationSortOrder | undefined => {
  if (props.sortField !== field) {
    return 'desc'
  }

  if (props.sortOrder === 'desc') {
    return 'asc'
  }

  if (props.sortOrder === 'asc') {
    return undefined
  }

  return 'desc'
}

/**
 * 点击表头时切换排序状态，并将结果交给父层统一取数。
 */
const handleSortClick = (field: EvaluationSortField) => {
  const nextSortOrder = getNextSortOrder(field)

  emits(
    'sort-change',
    nextSortOrder
      ? {
          sortField: field,
          sortOrder: nextSortOrder
        }
      : {}
  )
}

/**
 * 判断箭头是否应该高亮。
 */
const isArrowActive = (field: EvaluationSortField, direction: EvaluationSortOrder) => {
  return props.sortField === field && props.sortOrder === direction
}

const handleLoadMore = () => {
  emits('load-more')
}
</script>
<template>
  <!--    加载中-->
  <div v-if="(!evaluationData || evaluationData.length === 0) && loading" class="mt-12">
    <van-skeleton title :row="5" />
  </div>
  <div v-else-if="evaluationData.length > 0" class="component-layout mt-12">
    <div class="table-layout">
      <div class="table-header-layout">
        <div class="table-header-item rank-width">排名</div>
        <div class="table-header-item flex-1">TOP问题</div>
        <div
          v-for="sortItem in SORT_FIELDS"
          :key="sortItem.field"
          class="table-header-item text-center sort-header"
          :class="sortItem.widthClass"
          @click="handleSortClick(sortItem.field)"
        >
          <span>{{ sortItem.label }}</span>
          <span class="sort-icons">
            <svg class="sort-icon-svg" viewBox="0 0 8 12" aria-hidden="true" focusable="false">
              <path
                d="M4 0L8 5H0L4 0Z"
                class="sort-icon-path"
                :class="{ 'sort-icon-path--active': isArrowActive(sortItem.field, 'asc') }"
              />
              <path
                d="M4 12L0 7H8L4 12Z"
                class="sort-icon-path"
                :class="{ 'sort-icon-path--active': isArrowActive(sortItem.field, 'desc') }"
              />
            </svg>
          </span>
        </div>
      </div>
      <div class="table-column-layout">
        <div
          v-for="(item, index) in evaluationData"
          :key="index"
          class="table-row-item flex-y-center"
          :class="{ active: isActive(item) }"
          @click="handleClick(item)"
        >
          <div class="rank-width">
            <HSortNum :rank="index + 1"></HSortNum>
          </div>
          <div
            class="table-column-item opinion-class flex-1 fs-14 fw-400 flex-y-center single-line-ellipsis"
          >
            <div class="single-line-ellipsis">
              {{ item.opinion || '' }}
            </div>
          </div>
          <div class="table-column-item mation-width fs-14 fw-500 text-primary text-center">
            {{ fmtNum(item.mentions) }}
          </div>
          <div class="table-column-item mation-mom-width fs-14 fw-500 color-grey text-center">
            {{ fmtFix(item.mentionsMoM) }}
          </div>
        </div>
      </div>
    </div>
    <div v-if="loading" class="flex-center">
      <van-loading size="20px">加载中...</van-loading>
    </div>
    <div v-else-if="isShowMore" class="load-more flex-center pt-10 pb-8" @click="handleLoadMore">
      <div>点击加载更多<van-icon name="arrow-down" /></div>
    </div>
  </div>
  <van-empty v-else description="暂无数据" class="empty-container" />
</template>
<style lang="scss" scoped>
.component-layout {
  //background: linear-gradient( 180deg, #F5E6E6 0%, rgba(255,237,234,0) 100%);
  border-radius: 8px 8px 8px 8px;
  .title-layout {
    height: 40px;
    //background: rgba(255,138,139,0.5);
    border-radius: 4px 4px 4px 4px;
  }
  .title-class {
    font-weight: 600;
    font-size: 16px;
    color: #333333;
  }
  .table-layout {
    .table-header-layout {
      height: 40px;
      display: flex;
      align-items: center;
      font-weight: 400;
      font-size: 14px;
      color: #5f6a7a;
      border-bottom: 1px solid #dfe2e8;
    }
    .table-row-item {
      height: 42px;
    }
    .active {
      background: #eaf3ff;
      border-radius: 0px 0px 0px 0px;
      border-left: 2px solid #1677ff;
      .opinion-class {
        font-weight: 500 !important;
        color: #1677ff !important;
      }
    }
    .table-column-item {
      //display: flex;
      //align-items: center;
    }
  }
  .text-center {
    text-align: center;
  }
  .load-more {
    font-weight: 400;
    font-size: 12px;
    color: #929aa6;
  }
  .sort-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    white-space: nowrap;
  }
  .sort-icons {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 8px;
    height: 12px;
    flex-shrink: 0;
  }
  .sort-icon-svg {
    display: block;
    width: 8px;
    height: 12px;
  }
  .sort-icon-path {
    fill: #929aa6;
  }
  .sort-icon-path--active {
    fill: #1677ff;
  }
  .color-grey {
    color: #666;
  }
  .rank-width {
    margin-left: 4px;
    width: 34px;
  }
  .mation-width {
    width: 68px;
  }

  .mation-mom-width {
    width: 70px;
  }
  .arrow-width {
    width: 24px;
  }
}
.empty-container {
  //height: 100%;
}
</style>
