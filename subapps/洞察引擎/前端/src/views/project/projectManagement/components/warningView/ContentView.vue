<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { inject } from 'vue'
import { useExport } from '@/hooks/useExport'
import { debounce } from 'lodash-es'
import type { ConditionsDetailItem } from '@/types'
import { computedCardHeight } from '@/utils'
import useUserStore from '@/stores/modules/user'
import dayjs from 'dayjs'
import { exportRiskWarningData } from '@/api/warning'

interface Porps {
  record: any
  brandOptions: any[]
  riskType: string
  brand: string
}
const { record, brandOptions = [], riskType, brand } = defineProps<Porps>()

const userStore = useUserStore()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const {
  table,
  sortOpts,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getFirstPageTableData
} = useTable({
  method: 'POST',
  url: '/insights/insProjectInfo/findRiskWarningData'
})

// 获取上个月的第一天
const lastMonthFirstDay = dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD')

// 获取上个月的最后一天
const lastMonthLastDay = dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')

const defaultTime = [lastMonthFirstDay, lastMonthLastDay]
const times = ref(defaultTime)

watch(
  () => riskType,
  () => {
    filterInit()
    query()
  },
  {
    deep: true
  }
)

onMounted(() => {
  if (brandOptions?.length > 0) {
    table.filter.brand = brandOptions[0]?.brandName
  }
  query()
})

// 可选择的时间范围
// const disabledDate = (current: any) => {
//   // if (otherConditions.value?.startTime && otherConditions.value?.endTime) {
//   //   if (otherConditions.value?.startTime === otherConditions.value?.endTime) {
//   //     return dayjs(otherConditions.value?.startTime).format('YYYY-MM-DD') !== dayjs(current).format('YYYY-MM-DD')
//   //   } else {
//   //     return (
//   //       dayjs(current).isBefore(dayjs(otherConditions.value?.startTime)) ||
//   //       dayjs(current).isAfter(dayjs(otherConditions.value?.endTime))
//   //     )
//   //   }
//   // } else {
//   //   return false
//   // }
// }
const query = debounce(() => {
  table.filter.projectId = record.id
  table.filter.riskType = riskType
  table.filter.clientId = userStore.clientId
  const [startTime, endTime] = times.value
  table.filter.startTime = startTime
  table.filter.endTime = endTime
  // table.filter.brand = '上汽大众'
  getFirstPageTableData()
}, 300)

// watchEffect(() => {
//   table.filter.brand = brand
//   if (table.filter.brand) {
//     query()
//   }
// })
watch(
  () => brand,
  () => {
    table.filter.brand = brand
    query()
  },
  {
    deep: true
  }
)

const filterInit = () => {
  table.filter.projectId = record.id
  table.filter.clientId = userStore.clientId
  table.filter.riskType = riskType
  // table.filter.brand = '上汽大众'
  table.filter.statisticType = undefined
  table.filter.riskLevel = undefined
  table.filter.keywords = undefined
  // table.filter.brand = brandOptions[0]?.brandName
  table.filter.brand = brand
  times.value = defaultTime
  const [startTime, endTime] = times.value
  table.filter.startTime = startTime
  table.filter.endTime = endTime
}

const reset = () => {
  handleReset(filterInit)
}

const { exportFile, exporting } = useExport()
const handleExport = debounce(() => {
  exportFile(exportRiskWarningData, table.filter)
}, 300)
</script>

<template>
  <div class="flex">
    <el-form layout="inline" :model="table.filter">
      <el-row class="w-full" :gutter="24">
        <!-- <el-col :span="8">
          <el-form-item label="品牌">
            <el-select v-model="table.filter.brand" :data-testid="`wf-10001`" placeholder="全部">
              <el-option
                v-for="(item, index) in brandOptions"
                :key="index"
                :data-testid="`wf-10001-op-${index}`"
                :label="item.brandName"
                :value="item.brandName"
              />
            </el-select>
          </el-form-item>
        </el-col> -->
        <el-col :span="8">
          <el-form-item label="时间范围">
            <!-- :disabled-date="disabledDate" -->
            <el-date-picker v-model="times" type="daterange" :data-testid="`wf-10002`" />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="预警周期">
            <el-select
              v-model="table.filter.statisticType"
              :data-testid="`wf-10003`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.insightCycle"
                :key="index"
                :data-testid="`wf-10003-op-${index}`"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="风险等级">
            <el-select
              v-model="table.filter.riskLevel"
              :data-testid="`wf-10004`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.riskLevel"
                :key="index"
                :data-testid="`wf-10004-op-${index}`"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="关键词搜索">
            <el-input
              v-model.trim="table.filter.keywords"
              :data-testid="`wf-10005`"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="8" :offset="8">
          <div class="w-full flex justify-end">
            <el-button color="#F2F3F5" :data-testid="`wf-10006`" class="mr-8" @click="reset"
              >重置
            </el-button>
            <el-button type="primary" :data-testid="`wf-10007`" @click="query">查询 </el-button>
          </div>
        </el-col>
      </el-row>
    </el-form>
  </div>

  <el-divider />

  <div class="flex justify-between mb-12">
    <h3 class="table-left-title flex item-center">预警列表</h3>
    <el-button
      :disabled="!table.list?.length"
      :data-testid="`wtable-10001`"
      type="primary"
      class="ml-8"
      :loading="exporting"
      @click="handleExport"
    >
      <template #icon>
        <i class="iconfont icon-Export"></i>
      </template>
      导出数据
    </el-button>
  </div>
  <div class="table" :style="computedCardHeight(180)">
    <el-table
      :data-testid="`wtable-10002`"
      :loading="table.loading"
      :data="table.list"
      :pagination="{
        total: table.total,
        current: table.pageNum,
        pageSize: table.pageSize,
        showTotal: true,
        showPageSize: true,
        pageSizeOptions: [10, 15, 20, 25]
      }"
      :scroll="{
        x: '100%',
        y: '100%'
      }"
      @page-change="handleCurrentChange"
      @page-size-change="handleSizeChange"
      @sorter-change="handleSortChange"
    >
      <template #columns>
        <template v-if="['PROD', 'SERVICE'].includes(riskType)">
          <el-table-column data-index="brandCodeName" title="关联品牌" :size="150">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t1-${rowIndex}`">{{ record.brandCodeName }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="carSeriesName"
            title="涉及车系"
            ellipsis
            :tooltip="{
              contentClass: 'tooltipClass'
            }"
            :size="120"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t2-${rowIndex}`">{{ record.carSeriesName }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="riskName" title="风险问题" ellipsis tooltip :size="150">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t3-${rowIndex}`">{{ record.riskName }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="riskLevel" title="风险等级" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t4-${rowIndex}`">{{ record.riskLevel }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="focusName" title="聚焦问题" :size="180">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t5-${rowIndex}`">{{ record.focusName }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="opinionWords"
            title="观点热词"
            ellipsis
            :tooltip="{
              contentClass: 'tooltipClass'
            }"
            :size="240"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t6-${rowIndex}`">{{ record.opinionWords }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="negativeNum"
            title="负面观点数"
            :sortable="sortOpts"
            :size="130"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t7-${rowIndex}`">{{ record.negativeNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="complainNum"
            title="投诉观点数"
            :sortable="sortOpts"
            :size="130"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t8-${rowIndex}`">{{ record.complainNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="riskWordsNum"
            title="风险词观点数"
            :sortable="sortOpts"
            :size="140"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t9-${rowIndex}`">{{ record.riskWordsNum }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="userNum" title="发声用户数" :sortable="sortOpts" :size="130">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t10-${rowIndex}`">{{ record.userNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="channelNum"
            title="发声渠道"
            :sortable="sortOpts"
            :size="120"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t11-${rowIndex}`">{{ record.channelNum }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="createTime" title="预警时间" :size="180">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t12-${rowIndex}`">{{ record.createTime }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="statisticType" title="预警周期" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t13-${rowIndex}`">{{ record.statisticType }}</span>
            </template>
          </el-table-column>
        </template>
        <template v-if="riskType === 'QY'">
          <el-table-column data-index="brandCodeName" title="关联品牌" :size="150">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t1-${rowIndex}`">{{ record.brandCodeName }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="carSeriesName"
            title="涉及车系"
            ellipsis
            :tooltip="{
              contentClass: 'tooltipClass'
            }"
            :size="120"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t2-${rowIndex}`">{{ record.carSeriesName }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="riskName" title="风险问题" ellipsis tooltip :size="150">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t3-${rowIndex}`">{{ record.riskName }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="riskLevel" title="风险等级" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t4-${rowIndex}`">{{ record.riskLevel }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="focusName" title="聚焦问题" :size="180">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t5-${rowIndex}`">{{ record.focusName }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="opinionWords"
            title="观点热词"
            ellipsis
            :tooltip="{
              contentClass: 'tooltipClass'
            }"
            :size="240"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t6-${rowIndex}`">{{ record.opinionWords }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="negativeNum"
            title="负面观点数"
            :sortable="sortOpts"
            :size="130"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t7-${rowIndex}`">{{ record.negativeNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="riskWordsNum"
            title="风险词观点数"
            :sortable="sortOpts"
            :size="140"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t9-${rowIndex}`">{{ record.riskWordsNum }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="userNum" title="发声用户数" :sortable="sortOpts" :size="140">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t10-${rowIndex}`">{{ record.userNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="channelNum"
            title="发声渠道"
            :sortable="sortOpts"
            :size="120"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t11-${rowIndex}`">{{ record.channelNum }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="createTime" title="预警时间" :size="180">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t12-${rowIndex}`">{{ record.createTime }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="statisticType" title="预警周期" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t13-${rowIndex}`">{{ record.statisticType }}</span>
            </template>
          </el-table-column>
        </template>
        <template v-if="riskType === 'CM'">
          <el-table-column data-index="brandCodeName" title="关联品牌" :size="150">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t1-${rowIndex}`">{{ record.brandCodeName }}</span>
            </template>
          </el-table-column>
          <!-- 1 -->
          <el-table-column data-index="riskName" title="用户昵称" ellipsis tooltip :size="180">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-tt1001-${rowIndex}`">{{ record.riskName }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="riskLevel" title="风险等级" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t4-${rowIndex}`">{{ record.riskLevel }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="negativeNum"
            title="负面观点数"
            :sortable="sortOpts"
            :size="130"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t7-${rowIndex}`">{{ record.negativeNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="complainNum"
            title="投诉观点数"
            :sortable="sortOpts"
            :size="130"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-tt1002-${rowIndex}`">{{ record.complainNum }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="userNum" title="发声数" :sortable="sortOpts" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t10-${rowIndex}`">{{ record.userNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="channelNum"
            title="发声渠道"
            :sortable="sortOpts"
            :size="120"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t11-${rowIndex}`">{{ record.channelNum }}</span>
            </template>
          </el-table-column>
          <el-table-column
            data-index="emotionNum"
            title="净情感值"
            :sortable="sortOpts"
            :size="180"
          >
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-tt1003-${rowIndex}`">{{ record.emotionNum }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="createTime" title="预警时间" :size="180">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t12-${rowIndex}`">{{ record.createTime }}</span>
            </template>
          </el-table-column>
          <el-table-column data-index="statisticType" title="预警周期" :size="120">
            <template #cell="{ record, rowIndex }">
              <span :data-testid="`wtable-t13-${rowIndex}`">{{ record.statisticType }}</span>
            </template>
          </el-table-column>
        </template>
      </template>
    </el-table>
  </div>
</template>

<style scoped lang="scss"></style>
