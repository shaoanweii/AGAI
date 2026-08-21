<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { fmtFix, fmtHot, fmtNum } from '@/utils'
import LineTrend from '@/views/leaderOverview/leader/common/LineTrend.vue'
import ToolPop from '@/views/leaderOverview/leader/common/ToolPop.vue'
const fallbackBrandMark = '/demo-assets/brands/zhixing.png'

interface Props {
  dataType: string
  queryType: string
  rankingData: any[]
  loading: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataType: 'negativeRate',
  queryType: 'brand',
  loading: true,
  rankingData: () => []
})

// 新版 lastRanking = 升降的值
// const formatRankFlag = (ranking: number, lastRanking: number) => {
//   if (!lastRanking || !ranking) return ''
//   return Math.abs(lastRanking - ranking)
// }

/**
 * 组件事件定义
 */
const emit = defineEmits<{
  sort: [prop: string, order: string | null]
  tableClick: [data: { row: any; column?: any; sceneData?: any; queryType: string }]
}>()

/* 处理排序变化事件
 * 当用户点击列头进行排序时触发
 * @param params - 排序参数对象
 * @param params.prop - 排序字段名
 * @param params.order - 排序方向 ('ascending' | 'descending' | null)
 */
const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (order) emit('sort', prop, order === 'ascending' ? 'asc' : 'desc')
  else emit('sort', '', null)
}

// 下钻 - 新版
const handleCellClick = (row: any, column: any) => {
  // 发送点击数据到父组件
  emit('tableClick', { row, column, queryType: props.queryType })
}

// 场景下钻
const handleClickScene = (row: any, curItem: any) => {
  // 发送场景点击数据到父组件
  emit('tableClick', { row, sceneData: curItem, queryType: props.queryType })
}

const isSmall = ref(false)

const tableRef = ref<any>(null)

const clearSort = () => {
  tableRef.value?.clearSort()
}

defineExpose({
  clearSort
})

/**
 * 根据当前维度生成第一列标题。
 * 集团模式下使用车企，其余延续品牌/车系的既有展示。
 */
const nameColumnLabel = computed(() => {
  if (props.queryType === 'seriesFactory') {
    return '车企'
  }
  if (props.queryType === 'series') {
    return '车系'
  }
  return '品牌'
})

/**
 * 统一处理第一列名称兜底，兼容 seriesFactory 接口可能返回的车企字段。
 */
const getNameColumnValue = (row: any) => {
  return row.name || row.seriesFactory || row.brandName || '--'
}

const updatePageWidth = () => {
  const width = window.innerWidth
  isSmall.value = width < 1600
  // console.log("页面宽度: " + width);
  // console.log("是否小屏幕: " + isSmall.value);
}

// 页面加载时执行
onMounted(() => {
  updatePageWidth()
  window.addEventListener('resize', updatePageWidth)
})

// 页面大小改变时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', updatePageWidth)
})
</script>

<template>
  <!-- height="366"  -->
  <el-table
    ref="tableRef"
    v-loading="props.loading"
    :data="props.rankingData"
    :style="{ width: '100%' }"
    height="630"
    class="rankTable noBrd"
    @cell-click="handleCellClick"
    @sort-change="handleSortChange"
  >
    <el-table-column prop="ranking" label="本期排行" :width="isSmall ? 100 : 120">
      <template #default="scope">
        <div class="flex" :class="'type' + scope.row.type">
          <div class="rank-number">
            <SortNum :rank="scope.row.ranking" />
          </div>
          <div class="rank-change mr-5">
            <!-- 旧版up是绿色，新版反转改为红色 -->
            <span v-if="scope.row.rankingFlag === 'rising'" class="change-indicator down">
              <el-icon><CaretTop /></el-icon>{{ scope.row.lastRanking }}
            </span>
            <span v-if="scope.row.rankingFlag === 'falling'" class="change-indicator up">
              <el-icon><CaretBottom /></el-icon>{{ scope.row.lastRanking }}
            </span>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column
      v-if="['brand', 'series', 'seriesFactory'].includes(queryType)"
      prop="name"
      :label="nameColumnLabel"
      :width="isSmall ? 100 : 120"
    >
      <template #default="scope">
        <div class="cell2 imgCell" :class="'type' + scope.row.type">
          <div><img :src="scope.row.image || fallbackBrandMark" class="imgLogo" :alt="getNameColumnValue(scope.row)" /></div>
          <div>{{ getNameColumnValue(scope.row) }}</div>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="type" label="类型" :width="isSmall ? 100 : 120">
      <template #default="scope">
        <div :class="'type' + scope.row.type">
          {{ scope.row.type === '1' ? '本品' : scope.row.type === '2' ? '竞品' : '-' }}
        </div>
      </template>
    </el-table-column>

    <!-- 排序完全以接口返回顺序为准，这里使用 custom 禁止表格本地重排。 -->
    <el-table-column
      prop="mentionCount"
      label="提及量"
      :width="isSmall ? 100 : 120"
      sortable="custom"
    >
      <template #default="scope">
        <ToolPop
          :queryType="queryType"
          :data-type="dataType"
          tool-name="mentionCount"
          :row-data="scope.row"
        >
          <template #popBtn>
            <div class="cell2" :class="'type' + scope.row.type">
              <div class="mb-10">{{ fmtNum(scope.row.mentionCount) }}</div>
              <div class="gray">{{ fmtFix(scope.row.mentionRingRatio) }}</div>
            </div>
          </template>
        </ToolPop>
      </template>
    </el-table-column>
    <el-table-column prop="mentionRatio" label="占比" :width="isSmall ? 100 : 120">
      <template #default="scope">
        <div v-if="scope.row.type === ''" class="type">-</div>
        <ToolPop
          v-else
          :query-Type="queryType"
          :data-type="dataType"
          tool-name="mentionRatio"
          :row-data="scope.row"
        >
          <template #popBtn>
            <div :class="'type' + scope.row.type" v-html="fmtHot(scope.row.mentionRatio)"></div>
          </template>
        </ToolPop>
      </template>
    </el-table-column>
    <el-table-column
      prop="negativeRate"
      label="负面率"
      :width="isSmall ? 100 : 120"
      sortable="custom"
    >
      <template #default="scope">
        <ToolPop
          :queryType="queryType"
          :data-type="dataType"
          tool-name="negativeRate"
          :row-data="scope.row"
        >
          <template #popBtn>
            <div
              class="cell2"
              :class="[scope.row.negativeRate > 80 && 'hotBg', 'type' + scope.row.type]"
            >
              <div class="mb-10" v-html="fmtHot(scope.row.negativeRate)"></div>
              <div class="gray">{{ fmtFix(scope.row.negativeRingRatio) }}</div>
            </div>
          </template>
        </ToolPop>
      </template>
    </el-table-column>
    <el-table-column prop="trend" label="负面率趋势" :width="isSmall ? 150 : 200">
      <template #default="scope">
        <div style="padding: 10px" :class="'type' + scope.row.type">
          <LineTrend :trend-data="scope.row.trend" width="100%" :height="'60px'"></LineTrend>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="focusSceneTop3" label="关注场景TOP">
      <template #default="scope">
        <div
          v-if="scope.row.focusSceneTop3 && scope.row.focusSceneTop3.length > 0"
          class="flex pl-8 pr-8 modWrap"
          :class="'type' + scope.row.type"
        >
          <template v-for="item in scope.row.focusSceneTop3" :key="item.sceneName">
            <div class="modItem">
              <ToolPop
                :queryType="queryType"
                :data-type="dataType"
                :scene-data="item"
                tool-name="focusSceneTop3"
                :row-data="scope.row"
              >
                <template #popBtn>
                  <div class="mod" @click.stop="handleClickScene(scope.row, item)">
                    <span class="txt">{{ item.sceneName }}</span>
                    <span class="val" v-html="fmtHot(item.value, dataType)"></span>
                  </div>
                </template>
              </ToolPop>
            </div>
          </template>
        </div>
        <div v-else>-</div>
      </template>
    </el-table-column>
  </el-table>
</template>

<style lang="scss" scoped>
.el-table {
  border: none !important;

  :deep(tr:hover th .cell) {
    background-color: #eaf3ff !important;
  }

  :deep(th),
  :deep(td) {
    text-align: center;
    line-height: 60px;
    padding: 5px !important;
    border: none !important;
    background-color: transparent !important;
  }

  :deep(.cell) {
    // width:100% !important;
    height: 80px;
    line-height: 80px;
    padding: 0 0px !important;
    margin: 0 !important;
    border-radius: 8px !important;
    text-align: center;
    font-weight: 500;
    font-size: 16px;
    color: #333333;
    background-color: #eaf3ff;
    cursor: pointer;
  }

  // 市场均值
  .type,
  .type- {
    cursor: default;
    background: #e5fafe;
  }

  .type1 {
    background: #eaf3ff;
  }

  .type2 {
    background: #f2f4f7;
  }

  // :deep(tr):hover .cell{
  //   background: #F2F4F7;
  // }

  .flex {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .modWrap {
    width: 100%;
    cursor: default;
    gap: 8px;
  }

  .modItem {
    flex: 1;
    min-width: 0;
  }

  .mod {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    min-width: 0;
    width: 100%;
    height: 40px;
    padding: 0 8px;
    box-sizing: border-box;
    border-radius: 4px;
    font-size: 14px;
    text-align: center;
    cursor: pointer;
    background-color: #fff !important;
    border: 1px solid #dfe2e8;

    .txt {
      flex: 0 1 auto;
      min-width: 0;
      max-width: calc(100% - 58px);
      text-align: center;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .val {
      flex: 0 0 auto;
      min-width: 50px;
      text-align: center;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .cell2 {
    width: 100%;
    height: 100%;
    line-height: 20px;
    padding-top: 15px;
    text-align: center;

    &.imgCell {
      padding-top: 10px;
    }

    // 市场均值
    &.type {
      padding-top: 0 !important;
      line-height: 80px;
      .imgLogo {
        display: none;
      }
    }

    .gray {
      font-size: 14px;
      font-weight: 400px;
    }
  }

  :deep(.el-icon) {
    margin-right: 2px;
  }

  // 竞品列样式
  .competitor-cell {
    .competitor-content {
      display: flex;
      align-items: center;
      gap: 0;
      width: 100%;

      // 市场均值特殊布局
      .market-average-layout {
        display: flex;
        align-items: center;
        width: 100%;

        .market-indicator {
          font-size: 14px;
          color: #5f6a7a;
          font-weight: bold;
          width: 20px;
          text-align: center;
          flex-shrink: 0;
        }

        .market-spacer {
          width: 24px;
          flex-shrink: 0;
        }

        .market-label {
          font-size: 14px;
          color: #26292e;
          flex: 1;
        }
      }

      .rank-number {
        flex-shrink: 0;
        width: 20px;
        display: flex;
        justify-content: center;
        align-items: center;

        .market-indicator {
          font-size: 14px;
          color: #5f6a7a;
          font-weight: bold;
        }
      }

      .brand-name {
        flex: 1;
        font-size: 14px;
        color: #26292e;

        .changan-text {
          color: #1677ff;
          font-weight: 500;
        }
      }
    }
  }

  // 提及量列样式
  .mentions-cell {
    justify-content: center;

    .mentions-value {
      font-size: 14px;
      color: #26292e;

      &.changan-text {
        color: #1677ff;
        font-weight: 500;
      }
    }
  }

  // 负面率列样式
  .negative-rate-cell {
    justify-content: center;

    .negative-rate-value {
      font-size: 14px;
      color: #26292e;
    }
  }
}

@media screen and (max-width: 1400px) {
  .el-table {
    .modWrap {
      padding: 0 2px !important;
      gap: 4px;
    }
    .mod {
      padding: 0 4px !important;
      gap: 4px;
      .txt {
        max-width: calc(100% - 46px);
      }
      .val {
        min-width: 42px;
      }
    }
  }
}

.rank-number {
  flex-shrink: 0;
  width: 20px;
  display: flex;
  justify-content: center;
  align-items: center;

  .market-indicator {
    font-size: 14px;
    color: #5f6a7a;
    font-weight: bold;
  }
}

.rank-change {
  flex-shrink: 0;
  width: 40px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.change-indicator {
  font-size: 12px;
  font-weight: 500;

  &.up {
    color: #52c41a;
  }

  &.down {
    color: #ff4d4f;
  }
}

.imgLogo {
  width: 28px;
  height: 28px;
  display: block;
  margin: 0 auto 8px auto;
  object-fit: contain;
  border-radius: 6px;
}

.gray {
  color: #666;
}

:deep(.isTool) {
  cursor: pointer;
}
</style>
