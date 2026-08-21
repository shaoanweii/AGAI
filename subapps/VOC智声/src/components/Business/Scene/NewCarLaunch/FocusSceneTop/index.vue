<script setup lang="ts">
import { ref, computed } from 'vue'
import TopTable from './TopTable.vue'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import { getNewCarScenceTopResult } from '@/api/reportSummary/index'

interface Props {
  focusSceneTopData?: {
    preheat: any[]
    launch: any[]
    stable: any[]
  }
  queryParams?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  focusSceneTopData: () => ({
    preheat: [],
    launch: [],
    stable: []
  })
})

const emit = defineEmits<{
  'focus-scene-top-sort': [{ intention: string; prop: string; order: string }]
  'table-row-click': [{ intention: string; data: any }]
}>()

const focusSceneData = computed(() => ({
  preheat: props.focusSceneTopData?.preheat || [],
  launch: props.focusSceneTopData?.launch || [],
  stable: props.focusSceneTopData?.stable || []
}))

const handleTopTableSort = ({
  intention,
  prop,
  order
}: {
  intention: string
  prop: string
  order: string
}) => {
  emit('focus-scene-top-sort', { intention, prop, order })
}

const handleTableViewMore = (intention: string) => {
  emit('table-row-click', {
    intention,
    data: {
      __viewMore: true,
      tableTitle: `${intention}关注场景TOP`
    }
  })
}

const handleTableRowClick = (data: any, intention: string) => {
  emit('table-row-click', { intention, data })
}

const preheatTableRef = ref<any>(null)
const launchTableRef = ref<any>(null)
const stableTableRef = ref<any>(null)

const clearAllSort = () => {
  preheatTableRef.value?.clearSort()
  launchTableRef.value?.clearSort()
  stableTableRef.value?.clearSort()
}

defineExpose({
  clearAllSort
})
</script>

<template>
  <div class="focus-scene-top">
    <!-- 报告解读 -->
    <div class="report-summary-wrapper">
      <ReportSummary
        :api-function="getNewCarScenceTopResult"
        :query-params="props.queryParams"
      ></ReportSummary>
    </div>

    <!-- 三个时期的数据卡片 -->
    <div class="periods-container">
      <!-- 预热期 -->
      <FCard
        :title="'预热期'"
        titleSize="small"
        :height="'560px'"
        :isShowMore="true"
        class="f-card-border"
        headerClass="fc-reset-header1"
        leftExtraClass="fc-reset-left1"
        @handleMore="() => handleTableViewMore('预热期')"
      >
        <template #more>
          <ViewMore textColor="#1677ff" />
        </template>
        <TopTable
          ref="preheatTableRef"
          :data="focusSceneData.preheat"
          intention="预热期"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '预热期')"
        ></TopTable>
      </FCard>

      <!-- 上市期 -->
      <FCard
        :title="'上市期'"
        titleSize="small"
        :height="'560px'"
        :isShowMore="true"
        class="f-card-border"
        headerClass="fc-reset-header2"
        leftExtraClass="fc-reset-left2"
        @handleMore="() => handleTableViewMore('上市期')"
      >
        <template #more>
          <ViewMore textColor="#08979c" />
        </template>
        <TopTable
          ref="launchTableRef"
          :data="focusSceneData.launch"
          intention="上市期"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '上市期')"
        ></TopTable>
      </FCard>

      <!-- 稳定期 -->
      <FCard
        :title="'稳定期'"
        titleSize="small"
        :height="'560px'"
        :isShowMore="true"
        class="f-card-border"
        headerClass="fc-reset-header3"
        leftExtraClass="fc-reset-left3"
        @handleMore="() => handleTableViewMore('稳定期')"
      >
        <template #more>
          <ViewMore textColor="#d48806" />
        </template>
        <TopTable
          ref="stableTableRef"
          :data="focusSceneData.stable"
          intention="稳定期"
          @sort-change="handleTopTableSort"
          @row-click="(data: any) => handleTableRowClick(data, '稳定期')"
        ></TopTable>
      </FCard>
    </div>
  </div>
</template>

<style lang="scss">
// 强制重写 样式（与数据来源分析模块保持一致）
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

.focus-scene-top {
  .report-summary-wrapper {
    margin-top: 0;
    margin-bottom: 24px;
    padding-top: 0;
  }

  .report-summary-wrapper :deep(.report-summary) {
    padding: 16px;
    margin-top: 0;
  }

  .periods-container {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-top: 0;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .focus-scene-top {
    .periods-container {
      grid-template-columns: 1fr;
    }
  }
}
</style>
