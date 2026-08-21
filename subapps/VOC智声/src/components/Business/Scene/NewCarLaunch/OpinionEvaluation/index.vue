<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import ReportSummary from '@/components/Business/Scene/Common/ReportSummary.vue'
import TopTable from '@/components/Business/Scene/Common/ViewpointEvaluation/TopTable.vue'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { getNewCarOpinionEvaluationResult } from '@/api/reportSummary/index'

interface Props {
  queryParams: any
  opinionEvaluationData?: {
    preheat: {
      goodOpinions: any[]
      badOpinions: any[]
    }
    launch: {
      goodOpinions: any[]
      badOpinions: any[]
    }
    stable: {
      goodOpinions: any[]
      badOpinions: any[]
    }
  }
}

const props = withDefaults(defineProps<Props>(), {
  opinionEvaluationData: () => ({
    preheat: { goodOpinions: [], badOpinions: [] },
    launch: { goodOpinions: [], badOpinions: [] },
    stable: { goodOpinions: [], badOpinions: [] }
  })
})

const emit = defineEmits<{
  'opinion-top-sort': [{ phase: string; sentiment: string; prop: string; order: string }]
  'opinion-row-click': [{ phase: string; sentiment: string; data: any }]
}>()

const ddStore = useGeneralDrillDownStore()

// 数据状态
const reportSummary = ref<any>(null)

// 计算属性：处理观点评价数据
const opinionData = computed(() => ({
  preheat: {
    positive: props.opinionEvaluationData?.preheat?.goodOpinions || [],
    negative: props.opinionEvaluationData?.preheat?.badOpinions || []
  },
  launch: {
    positive: props.opinionEvaluationData?.launch?.goodOpinions || [],
    negative: props.opinionEvaluationData?.launch?.badOpinions || []
  },
  stable: {
    positive: props.opinionEvaluationData?.stable?.goodOpinions || [],
    negative: props.opinionEvaluationData?.stable?.badOpinions || []
  }
}))

// 报告解读数据由 ReportSummary 组件自动获取

// 处理查看更多
const handleViewMore = (phase: string, sentiment: string) => {
  emit('opinion-row-click', {
    phase,
    sentiment,
    data: {
      __viewMore: true,
      tableTitle: `${phase}${sentiment === 'positive' ? '好评' : '抱怨'}TOP`
    }
  })
}

// 处理行点击事件
const handleRowClick = (phase: string, sentiment: string, data: any) => {
  emit('opinion-row-click', { phase, sentiment, data })
}

// 暴露方法
const refreshData = async () => {
  // 数据由父组件传递，不需要在这里获取
}

defineExpose({
  refreshData
})

// 生命周期
onMounted(() => {
  // 数据由父组件传递，不需要在这里获取
})

// 监听查询参数变化
watch(
  () => props.queryParams,
  () => {
    // 数据由父组件传递，不需要在这里获取
  },
  { deep: true }
)
</script>

<template>
  <div class="opinion-evaluation">
    <!-- 报告解读 -->
    <div class="report-summary-wrapper">
      <ReportSummary
        :api-function="getNewCarOpinionEvaluationResult"
        :query-params="props.queryParams"
      ></ReportSummary>
    </div>

    <!-- 观点评价表格 -->
    <div class="opinion-tables">
      <!-- 预热期 -->
      <div class="period-section">
        <div class="period-title">
          <h3>预热期</h3>
        </div>
        <div class="card-container">
          <!-- 好评TOP -->
          <TopTable
            mode="good"
            :data="opinionData.preheat.positive"
            @row-click="(data: any) => handleRowClick('预热期', 'positive', data)"
            @view-more="() => handleViewMore('预热期', 'positive')"
          ></TopTable>
          
          <!-- 抱怨TOP -->
          <TopTable
            mode="bad"
            class="mt-24"
            :data="opinionData.preheat.negative"
            @row-click="(data: any) => handleRowClick('预热期', 'negative', data)"
            @view-more="() => handleViewMore('预热期', 'negative')"
          ></TopTable>
        </div>
      </div>

      <!-- 上市期 -->
      <div class="period-section">
        <div class="period-title">
          <h3>上市期</h3>
        </div>
        <div class="card-container">
          <!-- 好评TOP -->
          <TopTable
            mode="good"
            :data="opinionData.launch.positive"
            @row-click="(data: any) => handleRowClick('上市期', 'positive', data)"
            @view-more="() => handleViewMore('上市期', 'positive')"
          ></TopTable>
          
          <!-- 抱怨TOP -->
          <TopTable
            mode="bad"
            class="mt-24"
            :data="opinionData.launch.negative"
            @row-click="(data: any) => handleRowClick('上市期', 'negative', data)"
            @view-more="() => handleViewMore('上市期', 'negative')"
          ></TopTable>
        </div>
      </div>

      <!-- 稳定期 -->
      <div class="period-section">
        <div class="period-title">
          <h3>稳定期</h3>
        </div>
        <div class="card-container">
          <!-- 好评TOP -->
          <TopTable
            mode="good"
            :data="opinionData.stable.positive"
            @row-click="(data: any) => handleRowClick('稳定期', 'positive', data)"
            @view-more="() => handleViewMore('稳定期', 'positive')"
          ></TopTable>
          
          <!-- 抱怨TOP -->
          <TopTable
            mode="bad"
            class="mt-24"
            :data="opinionData.stable.negative"
            @row-click="(data: any) => handleRowClick('稳定期', 'negative', data)"
            @view-more="() => handleViewMore('稳定期', 'negative')"
          ></TopTable>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.opinion-evaluation {
  width: 100%;

  .report-summary-wrapper {
    margin-bottom: 24px;
  }

  .opinion-tables {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;

    .period-section {
      border-radius: 8px;
      border: 1px solid #e8e8e8;
      overflow: hidden;

      .period-title {
        padding: 14px 24px;
        margin: 0;
        text-align: center;
        position: relative;
      }

      &:nth-child(1) .period-title {
        background-color: #eaf3ff;
      }

      &:nth-child(2) .period-title {
        background-color: #e6fffb;
      }

      &:nth-child(3) .period-title {
        background-color: #fff7e6;
      }

      .period-title h3 {
        font-size: 16px;
        font-weight: 600;
        margin: 0;
      }

      &:nth-child(1) .period-title h3 {
        color: #1677ff;
      }

      &:nth-child(2) .period-title h3 {
        color: #08979c;
      }

      &:nth-child(3) .period-title h3 {
        color: #d48806;
      }

      .card-container {
        padding: 16px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .opinion-evaluation {
    .opinion-tables {
      grid-template-columns: 1fr;
    }
  }
}
</style>
