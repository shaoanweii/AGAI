<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
// import TopTable from '@/components/Business/Scene/Common/ViewpointEvaluation/TopTable.vue'
import TopTable from './TopTable.vue'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'

interface Props {
  goodOpinions?: any[]
  badOpinions?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  goodOpinions: () => [],
  badOpinions: () => []
})

const emit = defineEmits<{
  'opinion-top-sort': [{ sentiment: string; prop: string; order: string }]
  'opinion-row-click': [{ sentiment: string; data: any }]
}>()

const ddStore = useGeneralDrillDownStore()

// 报告解读数据由 ReportSummary 组件自动获取

// 处理查看更多
const handleViewMore = (sentiment: string) => {
  emit('opinion-row-click', {
    sentiment,
    data: {
      __viewMore: true,
      tableTitle: `${sentiment === 'positive' ? '好评' : '抱怨'}TOP`
    }
  })
}

// 处理行点击事件
const handleRowClick = (sentiment: string, data: any) => {
  emit('opinion-row-click', { sentiment, data })
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
</script>

<template>
  <div class="opinion-evaluation">
    <!-- 观点评价表格 -->
    <div class="opinion-tables">
      <div class="period-section">
        <div class="card-container">
          <TopTable
            mode="good"
            cusTitle="好评TOP10"
            :data="goodOpinions"
            @row-click="(data: any) => handleRowClick('positive', data)"
            @view-more="() => handleViewMore('positive')"
          ></TopTable>
        </div>
      </div>
      <div class="period-section">
        <div class="card-container">
          <TopTable
            mode="bad"
            cusTitle="抱怨TOP10"
            :data="badOpinions"
            @row-click="(data: any) => handleRowClick('negative', data)"
            @view-more="() => handleViewMore('negative')"
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
    grid-template-columns: repeat(2, 1fr);
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
        .top-table {
          height: unset !important;
        }
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
