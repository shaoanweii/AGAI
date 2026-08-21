<script setup lang="ts">
import { ref, computed } from 'vue'
import { fmtNum, fmtPer, fmtFix, fmtHot } from '@/utils'
import { useQueryStore } from '@/store/modules/query'
import useGeneralDrillDownStore from '@store/modules/generalDrillDown.ts'
defineOptions({
  name: 'viewTable'
})

interface Props {
  dataTitle?: string /** 当前品牌名 */
  dataList?: any[] /** 集团简报数据 */
}

const props = withDefaults(defineProps<Props>(), {
  dataTitle: '',
  dataList: () => []
})
const queryStore = useQueryStore()
const generalDrillDown = useGeneralDrillDownStore()

const storePms = queryStore.currentQueryParams
// 新接口已自动排序
const sortedData = computed(() => {
  return props.dataList
  // 计算实际排序（跳过市场均值）
  // return getTop5(props.dataList)
})

// 新版 lastRanking = 升降的值
// const formatRankFlag = (ranking: number, lastRanking: number) => {
//   if (!lastRanking || !ranking) return ''
//   return Math.abs(lastRanking - ranking)
// }

// 格式化品牌车系显示
const formatBrandSeries = (name: string, series?: string) => {
  if (series) {
    return `${name} ${series}`
  }
  return name
}

const handleRowClick = (item: any) => {
  console.log('item', item)

  const filterTags = []
  if (item.brandCode) {
    filterTags.push({ text: item.name, value: { brandCode: item.brandCode } })
  } else {
    filterTags.push({ text: item.name, value: { automark: item.name } })
  }
  // if (item.tag1Code) {
  // }
  if (item.name !== '市场均值') {
    // 集团主品牌的字段和其他品牌的字段不一样
    generalDrillDown.openDD(
      {
        brandCode: item.brandCode || undefined,
        automark: item.brandCode ? undefined : item.name,
        brandDataType: 2,
        channelCatagory: '公域',
        // 清除品牌洞察干扰
        tempCode: undefined,
        tag2Code: undefined,
        intention: undefined,
        topic: undefined,
        startDate: queryStore?.currentQueryParams.startDate,
        endDate: queryStore?.currentQueryParams.endDate,
        channelIds: undefined,
        contentTypes: undefined,
        custProvinceCodeSet: undefined,
        gender: undefined,
        isBigV: undefined,
        isCarOwner: undefined,
        isMainPost: undefined,
        isWsaterArmy: undefined,
        carSeriesList: undefined,
        customerName: undefined,
        dataId: undefined,
        firstCodeTag: undefined,
        oneId: undefined,
        originalLink: undefined,
        secondCodeTag: undefined,
        titleOrOriginal: undefined,
        topicCodes: undefined
      },
      {
        // 弹框副标题显示：传入点击行的名称（仅用于展示）
        subTitle: item.name
      },
      filterTags,
      {
        // 领导页下钻不继承场景页的公共筛选，避免跨页面参数串值
        mergeCommonQueryParams: false
      }
    )
  }
}

// 格式化hover表格数据
const formatHoverTableData = (item: any) => {
  const rows = [
    {
      name: '提及量',
      value: fmtNum(item.mentionCount),
      mom: fmtFix(item.ringRatio),
      yoy: fmtFix(item.yearOnYearRatio)
    },
    {
      name: '负面率',
      value: fmtPer(item.negativeRate),
      mom: fmtFix(item.negativeRingRatio),
      yoy: fmtFix(item.negativeYearOnYearRatio)
    }
  ]
  return rows
}
</script>

<template>
  <!-- 表格 -->
  <div class="brand-data-table">
    <!-- 表头 -->
    <div class="table-header">
      <div class="header-cell competitor">
        <span style="display: inline-block; min-width: 36px">{{
          props.dataTitle.includes('集团') ? '竞企' : '竞品'
        }}</span>
        <!-- <span class="hack">--------------</span> -->
      </div>
      <div class="header-cell mentions">提及量</div>
      <div class="header-cell negative-rate">负面率</div>
    </div>

    <!-- 表格内容 -->
    <div class="table-body">
      <el-popover
        v-for="item in sortedData"
        :key="item.id"
        placement="top"
        popper-class="tool-pop"
        :show-after="200"
        :width="410"
        trigger="hover"
      >
        <template #reference>
          <!-- 行 row -->
          <div
            class="table-row"
            :class="{ 'market-average': !item.ranking, changan: item.name === props.dataTitle }"
            @click="handleRowClick(item)"
          >
            <!-- 第一列：竞品信息 -->
            <div class="cell competitor-cell">
              <div class="competitor-content">
                <!-- 市场均值特殊布局 -->
                <template v-if="!item.ranking">
                  <div class="market-average-layout">
                    <span class="market-indicator">-</span>
                    <div class="market-spacer mr-5"></div>
                    <span class="market-label">{{ item.name }}</span>
                  </div>
                </template>

                <!-- 普通品牌布局 -->
                <template v-else>
                  <!-- 排序号 -->
                  <div class="rank-number">
                    <SortNum :rank="item.ranking" />
                  </div>

                  <!-- 排名变化 -->
                  <div class="rank-change mr-5">
                    <span v-if="item.rankingFlag === 'rising'" class="change-indicator down">
                      <el-icon><CaretTop /></el-icon>{{ item.lastRanking }}
                    </span>
                    <span v-else-if="item.rankingFlag === 'falling'" class="change-indicator up">
                      <el-icon><CaretBottom /></el-icon>{{ item.lastRanking }}
                    </span>
                  </div>

                  <!-- 品牌名称 -->
                  <div class="brand-name">
                    <span :class="{ 'changan-text': item.name === props.dataTitle }">
                      {{ formatBrandSeries(item.name, item.series) }}
                    </span>
                  </div>
                </template>
              </div>
            </div>

            <!-- 第二列：提及量 -->
            <div class="cell mentions-cell">
              <span
                class="mentions-value"
                :class="{ 'changan-text': item.name === props.dataTitle }"
              >
                {{ fmtNum(item.mentionCount) }}</span
              >
            </div>

            <!-- 第三列：负面率 -->
            <div class="cell negative-rate-cell">
              <span class="negative-rate-value" v-html="fmtHot(item.negativeRate)"> </span>
            </div>
          </div>
        </template>
        <template #default>
          <div class="fs-14 fw-500 mb-12" style="color: #333">{{ item.name }}</div>
          <!-- hover表格 -->
          <el-table :data="formatHoverTableData(item)" class="pop-table">
            <el-table-column prop="name" label="名称" width="70" />
            <el-table-column prop="value" label="数值" />
            <el-table-column prop="mom" label="环比" class-name="c666" />
            <el-table-column prop="yoy" label="同比" class-name="c666" />
          </el-table>
        </template>
      </el-popover>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.pop-table {
  :deep(.cell) {
    color: #26292e;
  }
  :deep(td.c666 .cell) {
    color: #666;
  }
}

// zw
.table-header,
.table-row {
  grid-template-columns: 1fr 60px 60px !important;
}

.brand-name,
.market-label {
  min-width: 30px;
}
.hack {
  color: transparent;
}

// 源码
.brand-data-table {
  // 表头样式
  .table-header {
    display: grid;
    grid-template-columns: 1fr 70px 70px;
    border-bottom: 1px solid #dfe2e8;

    .header-cell {
      padding: 0;
      height: 39px;
      font-weight: 400;
      font-size: 14px;
      color: #5f6a7a;
      display: flex;
      align-items: center;
      justify-content: center;

      &.competitor {
        justify-content: flex-start;
        padding-left: 8px;
      }

      // 提及量列不需要特殊背景色

      // 负面率列默认居中对齐
    }
  }

  // 表格主体
  .table-body {
    height: 220px;
    .table-row {
      display: grid;
      grid-template-columns: 1fr 70px 70px;
      border-bottom: 1px solid #ebedf0;

      &:hover {
        font-weight: bold;
        // background-color:#f5f5f5 !important;
        cursor: pointer;
      }

      &:last-child:not(.market-average):not(.changan) {
        border-bottom: 1px solid transparent;
      }

      // 市场均值行样式
      &.market-average {
        background-color: #e5fafe !important;
      }

      // 集团主品牌行样式
      &.changan {
        background-color: #eaf3ff !important;
      }

      // 市场均值和集团主品牌行的提及量列继承行背景色，无需特殊设置
    }
  }
  .cell {
    padding: 0;
    height: 43px;
    display: flex;
    align-items: center;
    font-weight: 500;
    font-size: 14px;
    color: #333333;
    line-height: 24px;

    // 竞品列样式
    &.competitor-cell {
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
          min-width: 65px;

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

            line-height: 16px;
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
          width: 24px;
          display: flex;
          justify-content: center;
          align-items: center;

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
        }

        .brand-name {
          flex: 1;
          font-size: 14px;
          color: #26292e;

          line-height: 16px;

          .changan-text {
            color: #1677ff;
            font-weight: 500;
          }
        }
      }
    }

    // 提及量列样式
    &.mentions-cell {
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
    &.negative-rate-cell {
      justify-content: center;

      .negative-rate-value {
        font-size: 14px;
        color: #26292e;

        &.high-negative {
          color: #ff5959;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
