<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { cloneDeep } from 'lodash-es'
import TopTable from './TopTable.vue'
import type { IntentionOpinionTopVo } from '@/api/productAnalysis/types'
import type { ServiceIntentionOpinionTopVo } from '@/api/serviceAnalysis/types'
import { ElMessage } from 'element-plus'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
import { INTERVAL_TYPE_OPTIONS } from '../constants'

defineOptions({
  name: 'DataSourceAnalysis'
})

// 接收从父组件传递的数据
interface Props {
  intentionOpinionTopData?: {
    complaint: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
    consultation: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
    suggestion: IntentionOpinionTopVo[] | ServiceIntentionOpinionTopVo[]
  }

  tagPath?: Array<{ code: string; name: string; level?: number }> // 标签路径信息
}

const {
  intentionOpinionTopData = {
    complaint: [],
    consultation: [],
    suggestion: []
  },
  tagPath = undefined
} = defineProps<Props>()

// 注意：searchParams 目前未直接使用，但保留作为 prop 用于将来扩展

// 定义emits
const emit = defineEmits<{
  'intention-top-sort': [{ intention: string; prop: string; order: string }]
  'data-type-change': [dataType: string, data: any]
  'chart-click': [data: any]
  'table-row-click': [{ intention: string; data: any }]
}>()

const vvList = INTERVAL_TYPE_OPTIONS.map(item => item.value)

// Store和下钻相关状态
const curTag = ref<any>()

// 内部标签路径状态
const localTagPath = ref<Array<{ code: string; name: string; level?: number }>>([])

// 监听 tagPath prop 的变化，同步到 localTagPath
watch(
  () => tagPath,
  newTagPath => {
    if (newTagPath && Array.isArray(newTagPath) && newTagPath.length > 0) {
      localTagPath.value = cloneDeep(newTagPath)
      // 默认不触发 chart-click，只有用户点击标题或图表时才触发
      curTag.value = undefined
    } else {
      localTagPath.value = []
      curTag.value = undefined
    }
  },
  { immediate: true, deep: true }
)

// 处理TopTable的排序事件
const handleTopTableSort = ({
  intention,
  prop,
  order
}: {
  intention: string
  prop: string
  order: string
}) => {
  emit('intention-top-sort', { intention, prop, order })
}

// 查看更多（观点TOP）
const handleTableViewMore = (intention: string) => {
  emit('table-row-click', {
    intention,
    data: {
      __viewMore: true,
      tableTitle: `${intention}观点TOP`
    }
  })
}
// 处理表格行点击事件
const handleTableRowClick = (data: any, intention: string) => {
  emit('table-row-click', { intention, data })
}

// 表格引用
const complaintTableRef = ref<any>(null)
const consultationTableRef = ref<any>(null)
const suggestionTableRef = ref<any>(null)
const praiseTableRef = ref<any>(null)

// 清空所有表格的排序状态
const clearAllSort = () => {
  complaintTableRef.value?.clearSort()
  consultationTableRef.value?.clearSort()
  suggestionTableRef.value?.clearSort()
  praiseTableRef.value?.clearSort()
}

// 暴露方法给父组件
defineExpose({
  clearAllSort
})
</script>

<template>
  <div class="data-source-analysis">
    <div class="mt-24 top-analysis-container">
      <FCard
        :title="vvList[0]"
        titleSize="small"
        :height="'560px'"
        :isShowMore="true"
        class="f-card-border"
        headerClass="fc-reset-header1"
        leftExtraClass="fc-reset-left1"
        @handleMore="() => handleTableViewMore(vvList[0])"
      >
        <template #more>
          <ViewMore textColor="#1677ff" />
        </template>
        <TopTable
          ref="complaintTableRef"
          :data="intentionOpinionTopData.complaint"
          :intention="vvList[0]"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, vvList[0])"
        ></TopTable>
      </FCard>
      <FCard
        :title="vvList[1]"
        titleSize="small"
        :height="'560px'"
        :isShowMore="true"
        class="f-card-border"
        headerClass="fc-reset-header2"
        leftExtraClass="fc-reset-left2"
        @handleMore="() => handleTableViewMore(vvList[1])"
      >
        <template #more>
          <ViewMore textColor="#08979c" />
        </template>
        <TopTable
          ref="consultationTableRef"
          :data="intentionOpinionTopData.consultation"
          :intention="vvList[1]"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, vvList[1])"
        ></TopTable>
      </FCard>
      <FCard
        :title="vvList[2]"
        titleSize="small"
        :height="'560px'"
        :isShowMore="true"
        class="f-card-border"
        headerClass="fc-reset-header3"
        leftExtraClass="fc-reset-left3"
        @handleMore="() => handleTableViewMore(vvList[2])"
      >
        <template #more>
          <ViewMore textColor="#d48806" />
        </template>
        <TopTable
          ref="suggestionTableRef"
          :data="intentionOpinionTopData.suggestion"
          :intention="vvList[2]"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, vvList[2])"
        ></TopTable>
      </FCard>
    </div>
  </div>
</template>

<style lang="scss">
// 强制重写 样式
.fc-reset-left1 {
  color: #1677ff !important;
}
.fc-reset-left2 {
  color: #08979c !important;
}
.fc-reset-left3 {
  color: #d48806 !important;
}
.fc-reset-header1 {
  padding: 10px 24px !important;
  background: #eaf3ff;
}

.fc-reset-header2 {
  padding: 10px 24px !important;
  background: #e6fffb;
}
.fc-reset-header3 {
  padding: 10px 24px !important;
  background: #fff7e6;
}

.data-source-analysis {
  .top-analysis-container {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}
</style>
