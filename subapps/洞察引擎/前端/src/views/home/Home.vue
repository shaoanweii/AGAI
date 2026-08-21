<script lang="ts" setup>
import type { Component } from 'vue'
import {
  ArrowRight,
  Box,
  CircleCheck,
  Coin,
  Collection,
  DataLine,
  DocumentAdd,
  Filter,
  Grid,
  Key,
  MagicStick,
  PriceTag,
  Warning
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import FtCard from '@/components/FtCard.vue'

interface OverviewMetric {
  label: string
  value: string
  compare: string
  trend: 'up' | 'down'
  icon: Component
  tone: 'primary' | 'success' | 'warning' | 'danger'
}

interface PipelineNode {
  name: string
  value: string
  successRate: string
  failCount: string
  icon: Component
}

interface KnowledgeAsset {
  name: string
  value: string
  coverage: string
  percent: number
  icon: Component
}

interface SourceRow {
  id: number
  source: string
  raw: string
  clean: string
  result: string
  errorRate: string
  warning?: boolean
}

const router = useRouter()

const overviewMetrics: OverviewMetric[] = [
  {
    label: '原声总量',
    value: '1,268.3w',
    compare: '+8.42%',
    trend: 'up',
    icon: DataLine,
    tone: 'primary'
  },
  {
    label: '清洗数据',
    value: '1,168.6w',
    compare: '+7.18%',
    trend: 'up',
    icon: Filter,
    tone: 'primary'
  },
  {
    label: '结果数据',
    value: '998.7w',
    compare: '+6.35%',
    trend: 'up',
    icon: Box,
    tone: 'primary'
  },
  {
    label: '今日新增',
    value: '85.4w',
    compare: '+12.53%',
    trend: 'up',
    icon: DocumentAdd,
    tone: 'warning'
  },
  {
    label: '清洗通过率',
    value: '92.36%',
    compare: '+1.82%',
    trend: 'up',
    icon: CircleCheck,
    tone: 'success'
  },
  {
    label: '异常数据',
    value: '32,846',
    compare: '-3.91%',
    trend: 'down',
    icon: Warning,
    tone: 'danger'
  }
]

const pipelineNodes: PipelineNode[] = [
  {
    name: '原始数据',
    value: '1,268.3w',
    successRate: '100.00%',
    failCount: '0',
    icon: Coin
  },
  {
    name: '清洗数据',
    value: '1,168.6w',
    successRate: '92.36%',
    failCount: '99,725',
    icon: Filter
  },
  {
    name: '模型解析',
    value: '1,056.2w',
    successRate: '90.38%',
    failCount: '112,347',
    icon: MagicStick
  },
  {
    name: '结果数据',
    value: '998.7w',
    successRate: '94.56%',
    failCount: '57,512',
    icon: Box
  },
  {
    name: '数据应用',
    value: '12.6w',
    successRate: '98.71%',
    failCount: '1,588',
    icon: Grid
  }
]

// 主控台只展示当前知识中心仍在使用的资产类型，避免出现已下线菜单对应的数据项。
const knowledgeAssets: KnowledgeAsset[] = [
  {
    name: '标准观点',
    value: '3,562',
    coverage: '81.74%',
    percent: 82,
    icon: Collection
  },
  {
    name: '用车场景',
    value: '1,876',
    coverage: '68.35%',
    percent: 68,
    icon: Grid
  },
  {
    name: '品牌车系',
    value: '645',
    coverage: '84.62%',
    percent: 85,
    icon: PriceTag
  },
  {
    name: '规则词库',
    value: '3,218',
    coverage: '72.48%',
    percent: 72,
    icon: Key
  },
  {
    name: '账号词库',
    value: '9,542',
    coverage: '88.21%',
    percent: 88,
    icon: Key
  }
]

const sourceRows: SourceRow[] = [
  {
    id: 1,
    source: '汽车之家',
    raw: '428.3w',
    clean: '392.6w',
    result: '344.8w',
    errorRate: '2.41%'
  },
  { id: 2, source: '懂车帝', raw: '312.5w', clean: '289.6w', result: '256.7w', errorRate: '2.15%' },
  {
    id: 3,
    source: '抖音',
    raw: '268.7w',
    clean: '241.0w',
    result: '208.9w',
    errorRate: '3.32%',
    warning: true
  },
  {
    id: 4,
    source: '客服热线',
    raw: '168.4w',
    clean: '156.2w',
    result: '143.6w',
    errorRate: '1.86%'
  },
  { id: 5, source: '问卷', raw: '90.4w', clean: '89.2w', result: '87.2w', errorRate: '0.93%' }
]

/** 跳转到数据查询页查看完整数据链路和数据源明细。 */
const goDataQuery = () => {
  void router.push('/dataCenter/dataQuery')
}

/** 跳转到标准观点页查看知识资产明细。 */
const goKnowledgeAssets = () => {
  void router.push('/knowledgeCenter/standardPoint')
}
</script>

<template>
  <div class="home-dashboard flex-col">
    <FtCard
      title="数据总览"
      model="titleOperation"
      clear-content-top-padding
      class="dashboard-section overview-section"
    >
      <div class="overview-grid">
        <article v-for="metric in overviewMetrics" :key="metric.label" class="metric-card">
          <div class="metric-card__main">
            <div :class="['metric-card__icon', `is-${metric.tone}`]">
              <el-icon><component :is="metric.icon" /></el-icon>
            </div>
            <div class="metric-card__content">
              <div class="metric-card__label">{{ metric.label }}</div>
              <div class="metric-card__value">{{ metric.value }}</div>
            </div>
          </div>
          <div class="metric-card__compare">
            <span>较昨日</span>
            <span :class="metric.trend === 'down' ? 'trend-success' : 'trend-danger'">
              {{ metric.compare }} {{ metric.trend === 'down' ? '↓' : '↑' }}
            </span>
          </div>
        </article>
      </div>
    </FtCard>

    <FtCard
      title="数据链路"
      model="titleOperation"
      clear-content-top-padding
      class="dashboard-section dashboard-spacing"
    >
      <template #extra>
        <el-button type="primary" link @click="goDataQuery">
          查看详情
          <el-icon class="ml-4"><ArrowRight /></el-icon>
        </el-button>
      </template>

      <div class="pipeline-flow">
        <template v-for="(node, index) in pipelineNodes" :key="node.name">
          <article class="pipeline-node">
            <div class="pipeline-node__head">
              <div class="pipeline-node__icon">
                <el-icon><component :is="node.icon" /></el-icon>
              </div>
              <div>
                <div class="pipeline-node__name">{{ node.name }}</div>
                <div class="pipeline-node__value">{{ node.value }}</div>
              </div>
            </div>
            <div class="pipeline-node__meta">
              <span>成功率</span>
              <strong>{{ node.successRate }}</strong>
            </div>
            <div class="pipeline-node__meta">
              <span>失败数</span>
              <strong :class="{ 'danger-text': node.failCount !== '0' }">{{
                node.failCount
              }}</strong>
            </div>
          </article>
          <div v-if="index < pipelineNodes.length - 1" class="pipeline-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </template>
      </div>
    </FtCard>

    <div class="dashboard-bottom dashboard-spacing">
      <FtCard
        title="知识资产"
        model="titleOperation"
        clear-content-top-padding
        class="dashboard-section knowledge-section"
      >
        <template #extra>
          <el-button type="primary" link @click="goKnowledgeAssets">
            查看详情
            <el-icon class="ml-4"><ArrowRight /></el-icon>
          </el-button>
        </template>

        <div class="knowledge-list">
          <div v-for="asset in knowledgeAssets" :key="asset.name" class="knowledge-row">
            <div class="knowledge-row__name">
              <span class="knowledge-row__icon"
                ><el-icon><component :is="asset.icon" /></el-icon
              ></span>
              <span>{{ asset.name }}</span>
            </div>
            <strong class="knowledge-row__value">{{ asset.value }}</strong>
            <el-progress
              :percentage="asset.percent"
              :show-text="false"
              :stroke-width="6"
              class="knowledge-row__progress"
            />
            <span class="knowledge-row__coverage">{{ asset.coverage }}</span>
          </div>
        </div>
      </FtCard>

      <FtCard
        title="数据源统计"
        model="titleOperation"
        clear-content-top-padding
        class="dashboard-section source-section"
      >
        <template #extra>
          <el-button type="primary" link @click="goDataQuery">
            查看详情
            <el-icon class="ml-4"><ArrowRight /></el-icon>
          </el-button>
        </template>

        <el-table :data="sourceRows" row-key="id" class="source-table" size="small">
          <el-table-column prop="source" label="数据源" min-width="72" />
          <el-table-column prop="raw" label="原声数据" min-width="62" />
          <el-table-column prop="clean" label="清洗数据" min-width="62" />
          <el-table-column prop="result" label="结果数据" min-width="62" />
          <el-table-column prop="errorRate" label="异常率" min-width="58">
            <template #default="{ row }">
              <span :class="row.warning ? 'danger-text' : 'success-text'">{{ row.errorRate }}</span>
            </template>
          </el-table-column>
        </el-table>
      </FtCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.home-dashboard {
  width: 100%;
  min-height: 100%;
  box-sizing: border-box;
}

.dashboard-section {
  box-sizing: border-box;
}

.dashboard-spacing {
  margin-top: 8px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  min-width: 0;
  min-height: 104px;
  padding: 12px;
  border: 1px solid #e5eaf2;
  border-radius: 8px;
  background: #fff;
  box-sizing: border-box;

  &__main {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    flex: 0 0 36px;
    border-radius: 8px;
    color: #1677ff;
    background: #edf5ff;
    font-size: 19px;

    &.is-success {
      color: #14a46f;
      background: #eaf8f2;
    }

    &.is-warning {
      color: #f08c1a;
      background: #fff5e8;
    }

    &.is-danger {
      color: #e5484d;
      background: #fff0f0;
    }
  }

  &__content {
    min-width: 0;
  }

  &__label {
    overflow: hidden;
    color: #4e5969;
    font-size: 12px;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__value {
    margin-top: 2px;
    color: #1d2129;
    font-size: 20px;
    font-weight: 600;
    line-height: 28px;
    white-space: nowrap;
  }

  &__compare {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
    color: #86909c;
    font-size: 12px;
    line-height: 20px;
  }
}

.trend-danger,
.danger-text {
  color: #e5484d !important;
}

.trend-success,
.success-text {
  color: #14a46f !important;
}

.pipeline-flow {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 18px minmax(0, 1fr) 18px minmax(0, 1fr) 18px minmax(0, 1fr) 18px minmax(
      0,
      1fr
    );
  align-items: center;
  gap: 6px;
}

.pipeline-node {
  min-width: 0;
  min-height: 128px;
  padding: 12px 10px;
  border: 1px solid #e5eaf2;
  border-radius: 8px;
  background: #fff;
  box-sizing: border-box;

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    flex: 0 0 32px;
    border-radius: 8px;
    color: #1677ff;
    background: #edf5ff;
    font-size: 18px;
  }

  &__name {
    overflow: hidden;
    color: #4e5969;
    font-size: 12px;
    line-height: 20px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__value {
    overflow: hidden;
    color: #1d2129;
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__meta {
    display: flex;
    justify-content: space-between;
    gap: 6px;
    margin-top: 6px;
    color: #86909c;
    font-size: 12px;
    line-height: 18px;

    strong {
      overflow: hidden;
      color: #1677ff;
      font-weight: 500;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.pipeline-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8abfff;
  font-size: 17px;
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: minmax(0, 0.4fr) minmax(0, 0.6fr);
  gap: 16px;
}

.knowledge-list {
  display: flex;
  flex-direction: column;
}

.knowledge-row {
  display: grid;
  grid-template-columns: minmax(80px, 1fr) 44px minmax(50px, 1fr) 38px;
  align-items: center;
  gap: 8px;
  min-height: 39px;
  border-bottom: 1px solid #f0f2f5;
  color: #4e5969;
  font-size: 13px;

  &:last-child {
    border-bottom: 0;
  }

  &__name {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
    white-space: nowrap;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    flex: 0 0 26px;
    border-radius: 6px;
    color: #1677ff;
    background: #edf5ff;
    font-size: 15px;
  }

  &__value {
    color: #1d2129;
    font-weight: 500;
    text-align: right;
  }

  &__coverage {
    color: #86909c;
    text-align: right;
  }
}

.source-table {
  width: 100%;

  :deep(.el-table__header-wrapper th) {
    height: 36px;
    background: #f5f7fa !important;
    color: #4e5969;
    font-size: 12px;
    font-weight: 600;
  }

  :deep(.el-table__row) {
    height: 35px;
  }

  :deep(.el-table__cell) {
    padding: 6px 0;
    border-bottom-color: #f0f2f5;
    color: #4e5969;
    font-size: 12px;
  }
}

@media (max-width: 800px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .pipeline-flow {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .pipeline-arrow {
    display: none;
  }

  .dashboard-bottom {
    grid-template-columns: 1fr;
  }
}
</style>
