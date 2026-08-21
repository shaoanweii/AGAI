<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import HSortNum from '@h5/components/UI/HSortNum/index.vue'
import { fmtFix, fmtNum } from '@/utils'
import type { SeriesRankItemVo } from '@h5/api/rootCauseAnalysis/types'
import defaultCarImage from '@/assets/images/group-car.png'

defineOptions({
  name: 'CarSeriesRankCard'
})

type SeriesSortField = 'mentions' | 'mentionsMoM'
type SeriesSortOrder = 'asc' | 'desc'

const props = defineProps<{
  data: SeriesRankItemVo[]
  activeCode?: string
  loading?: boolean
  hasMore?: boolean
  sortField?: SeriesSortField
  sortOrder?: SeriesSortOrder
}>()

const emit = defineEmits<{
  rowClick: [item: SeriesRankItemVo]
  loadMore: []
  'sort-change': [
    payload: {
      sortField?: SeriesSortField
      sortOrder?: SeriesSortOrder
    }
  ]
}>()

const bodyRef = ref<HTMLElement | null>(null)

/**
 * 车系排行支持的接口排序字段。
 */
const SORT_FIELDS: Array<{
  field: SeriesSortField
  label: string
}> = [
  {
    field: 'mentions',
    label: '提及量'
  },
  {
    field: 'mentionsMoM',
    label: '环比'
  }
]

/**
 * 点击车系行后交给页面处理联动与取消联动。
 * @param item 当前车系
 */
const handleRowClick = (item: SeriesRankItemVo) => {
  emit('rowClick', item)
}

/**
 * 计算指定列下一次点击后的排序方向。
 * 规则：首次降序、再次升序、第三次恢复默认排序。
 * @param field 排序字段
 * @returns 下一次排序方向
 */
const getNextSortOrder = (field: SeriesSortField): SeriesSortOrder | undefined => {
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
 * 点击表头时切换排序状态。
 * @param field 排序字段
 */
const handleSortClick = (field: SeriesSortField) => {
  const nextSortOrder = getNextSortOrder(field)

  emit(
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
 * 判断排序箭头是否高亮。
 * @param field 排序字段
 * @param direction 排序方向
 * @returns 是否高亮
 */
const isArrowActive = (field: SeriesSortField, direction: SeriesSortOrder) => {
  return props.sortField === field && props.sortOrder === direction
}

// 追加车系后定位到新增批次首条，让用户明确看到新加载内容。
watch(
  () => props.data.length,
  (newLength, oldLength) => {
    if (!oldLength || newLength <= oldLength) return

    nextTick(() => {
      const bodyEl = bodyRef.value
      const firstNewRow = bodyEl?.querySelector<HTMLElement>(`[data-row-index="${oldLength}"]`)
      if (!bodyEl || !firstNewRow) return

      const bodyRect = bodyEl.getBoundingClientRect()
      const rowRect = firstNewRow.getBoundingClientRect()

      bodyEl.scrollTo({
        top: bodyEl.scrollTop + rowRect.top - bodyRect.top,
        behavior: 'smooth'
      })
    })
  }
)
</script>

<template>
  <HCard title="车系排行TOP">
    <div class="car-rank">
      <div v-if="!loading && data.length > 0" class="car-rank__header">
        <div>排名</div>
        <div>车系</div>
        <div
          v-for="sortItem in SORT_FIELDS"
          :key="sortItem.field"
          class="car-rank__sort-header"
          @click="handleSortClick(sortItem.field)"
        >
          <span>{{ sortItem.label }}</span>
          <span class="car-rank__sort-icons">
            <svg
              class="car-rank__sort-icon-svg"
              viewBox="0 0 8 12"
              aria-hidden="true"
              focusable="false"
            >
              <path
                d="M4 0L8 5H0L4 0Z"
                class="car-rank__sort-icon-path"
                :class="{
                  'car-rank__sort-icon-path--active': isArrowActive(sortItem.field, 'asc')
                }"
              />
              <path
                d="M4 12L0 7H8L4 12Z"
                class="car-rank__sort-icon-path"
                :class="{
                  'car-rank__sort-icon-path--active': isArrowActive(sortItem.field, 'desc')
                }"
              />
            </svg>
          </span>
        </div>
      </div>
      <van-skeleton v-if="loading && data.length === 0" title :row="6" />
      <div v-else ref="bodyRef" class="car-rank__body">
        <button
          v-for="(item, index) in props.data"
          :key="item.code || index"
          type="button"
          class="car-rank__row"
          :class="{ 'is-active': activeCode === item.code }"
          :data-row-index="index"
          @click="handleRowClick(item)"
        >
          <span class="car-rank__rank">
            <HSortNum :rank="index + 1" />
          </span>
          <span class="car-rank__series">
            <img :src="item.imageUrl || defaultCarImage" alt="" class="car-rank__image" />
            <span class="car-rank__name">{{ item.name || '-' }}</span>
          </span>
          <span class="car-rank__number">{{ fmtNum(item.mentions || 0) }}</span>
          <span class="car-rank__rate">{{ fmtFix(item.mentionsMoM) }}</span>
        </button>
        <van-empty v-if="!loading && data.length === 0" image-size="64" description="暂无数据" />
        <button v-if="hasMore" type="button" class="car-rank__more" @click="emit('loadMore')">
          点击查看更多
        </button>
      </div>
    </div>
  </HCard>
</template>

<style scoped lang="scss">
.car-rank {
  margin-top: 10px;

  &__header,
  &__row {
    display: grid;
    grid-template-columns: 42px minmax(0, 1fr) 66px 66px;
    align-items: center;
  }

  &__header {
    height: 40px;
    border-bottom: 1px solid #dfe2e8;
    font-weight: 400;
    font-size: 14px;
    color: #5f6a7a;
  }

  &__body {
    max-height: 440px;
    overflow-y: auto;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  &__row {
    width: 100%;
    height: 40px;
    padding: 0;
    border: 0;
    background: #ffffff;
    text-align: left;
    color: #5f6a7a;

    &.is-active {
      background: #eaf3ff;
      box-shadow: inset 3px 0 0 #1677ff;
    }
  }

  &__rank {
    display: flex;
    justify-content: center;
  }

  &__series {
    display: flex;
    align-items: center;
    min-width: 0;
    gap: 8px;
  }

  &__image {
    width: 24px;
    height: 18px;
    object-fit: contain;
    flex-shrink: 0;
  }

  &__name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-weight: 400;
    font-size: 14px;
    color: #333333;
    line-height: 24px;
  }

  &__row.is-active &__name {
    font-weight: 500;
    color: #1677ff;
  }

  &__number {
    font-weight: 500;
    font-size: 14px;
    color: #1f2733;
    line-height: 20px;
    text-align: right;
    font-style: normal;
  }

  &__rate {
    font-weight: 500;
    font-size: 14px;
    color: #666666;
    line-height: 24px;
    text-align: right;
    font-style: normal;
  }

  &__sort-header {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 4px;
    cursor: pointer;
    white-space: nowrap;
  }

  &__sort-icons {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 8px;
    height: 12px;
    flex-shrink: 0;
  }

  &__sort-icon-svg {
    display: block;
    width: 8px;
    height: 12px;
  }

  &__sort-icon-path {
    fill: #929aa6;
  }

  &__sort-icon-path--active {
    fill: #1677ff;
  }

  &__more {
    width: 100%;
    height: 34px;
    border: 0;
    background: #ffffff;
    color: #929aa6;
    font-size: 12px;
  }
}
</style>
