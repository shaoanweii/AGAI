<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { useExport } from '@/hooks/useExport'
import { debounce } from 'lodash-es'
import type { ConditionsDetailItem } from '@/types'
import { computedCardHeight } from '@/utils'
import useUserStore from '@/stores/modules/user'
import dayjs from 'dayjs'
import { exportLabelCorrectionList, auditLabelCorrection } from '@/api/project'
import { ElMessage } from 'element-plus'

interface Porps {
  record: any
  // brandOptions: any[]
  channelOptions: Record<any, any>[] | undefined
  brand: string
  carSeriesOptions: any[]
  tagLibCategoryVosOptions: any[]
  conditions: Record<string, ConditionsDetailItem[]>
  // conditions: Conditions[]
}

const props = defineProps<Porps>()
const { channelOptions, brand, carSeriesOptions, tagLibCategoryVosOptions, conditions } =
  toRefs(props)

const userStore = useUserStore()

const {
  table,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getFirstPageTableData
} = useTable({
  method: 'POST',
  url: '/insights/label/queryLabelCorrectionList'
})

// 获取上个月的第一天
const lastMonthFirstDay = dayjs().startOf('month').format('YYYY-MM-DD')

// 获取上个月的最后一天
const lastMonthLastDay = dayjs().format('YYYY-MM-DD')

const defaultTime = [lastMonthFirstDay, lastMonthLastDay]
const times = ref(defaultTime)

onMounted(() => {
  // if (brandOptions?.length > 0) {
  //   table.filter.brand = brandOptions[0]?.brandName
  // }
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
  table.filter.brandCode = [brand.value]
  table.filter.clientId = userStore.clientId
  const [startTime, endTime] = times.value
  table.filter.startTime = startTime
  table.filter.endTime = endTime
  getFirstPageTableData()
}, 300)

// watchEffect(() => {
//   table.filter.brand = brand
//   if (table.filter.brand) {
//     query()
//   }
// })

const handleOkCheck = (record: any) => {
  changeAudit(record, '1')
}

const changeAudit = (record: any, auditStatus: string) => {
  auditLabelCorrection({
    newId: record.newId,
    auditStatus: auditStatus,
    clientId: userStore.clientId
  })
    .then(res => {
      if (res.code === '200') {
        ElMessage.success(res.message)
      } else {
        ElMessage.error(res.message)
      }
    })
    .finally(() => {
      query()
    })
}

const handleRejectCheck = (record: any) => {
  changeAudit(record, '2')
}

watch(
  () => brand,
  () => {
    table.filter.brandCode = [brand.value]
    query()
  },
  {
    deep: true
  }
)

const reset = () => {
  handleReset(() => {
    table.filter.brandCode = [brand.value]
    table.filter.clientId = userStore.clientId
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime
  })
  getFirstPageTableData()
}
const { exportFile, exporting } = useExport()
const handleExport = debounce(() => {
  exportFile(exportLabelCorrectionList, table.filter)
}, 300)
</script>

<template>
  <div class="flex">
    <el-form inline :model="table.filter">
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
          <el-form-item label="时间范围" class="w-full">
            <!-- :disabled-date="disabledDate" -->
            <el-date-picker
              v-model="times"
              type="daterange"
              :data-testid="`wf-10002`"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="数据渠道" class="w-full">
            <FCascader
              v-model="table.filter.channelIdList"
              :options="channelOptions"
              width="100%"
              multiple
              clearable
              :subLength="10"
              :placeholder="'全部'"
              :fieldNames="{ value: 'code', label: 'name', children: 'child' }"
              :data-testid="`detail-original-channel-20001`"
            ></FCascader>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="品牌车系" class="w-full">
            <FSelect
              v-model="table.filter.carSeries"
              multiple
              clearable
              :subLength="7"
              :options="carSeriesOptions"
              :fieldNames="{ value: 'value', label: 'name' }"
              placeholder="全部车系"
            ></FSelect>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="分类" class="w-full">
            <!-- <el-select
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
            </el-select> -->
            <FCascader
              v-model="table.filter.tagList"
              :options="tagLibCategoryVosOptions"
              :fieldNames="{ value: 'tagName', label: 'tagName', children: 'child' }"
              clearable
              :subLength="4"
              :max-collapse-tags="1"
              placeholder="全部"
              multiple
              :data-testid="`dataSource-result-10004`"
            ></FCascader>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="情感" class="w-full">
            <el-select
              v-model="table.filter.sentiment"
              :data-testid="`wf-10004`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.emotion"
                :key="index"
                :data-testid="`wf-10004-op-${index}`"
                :label="item.value"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="意图" class="w-full">
            <el-select
              v-model="table.filter.intention"
              placeholder="意图"
              :data-testid="`detail-result-10008`"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.intention"
                :key="index"
                :data-testid="`detail-result-10008-op-${index}`"
                :label="item.value"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="审核状态" class="w-full">
            <el-select
              v-model="table.filter.auditStatus"
              :data-testid="`wf-10005`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.auditStatus"
                :key="index"
                :data-testid="`wf-10005-op-${index}`"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
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
    <h3 class="table-left-title flex item-center">数据列表</h3>
    <el-button
      :disabled="!table.list?.length"
      :data-testid="`ctable-10001`"
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
  <div class="table" :style="computedCardHeight(380)">
    <el-table
      :data-testid="`ctable-10002`"
      v-loading="table.loading"
      :data="table.list"
      height="100%"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="operateTime" label="纠错时间" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t1-${$index}`">{{ row.operateTime }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operateUser" label="纠错人" width="150" show-overflow-tooltip>
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t3-${$index}`">{{ row.operateUser }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="声音ID" width="320">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t4-${$index}`">{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发声时间" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t5-${$index}`">{{ row.publishTime }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="发声人" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t6-${$index}`">{{ row.userName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="channelFirst" label="一级渠道" width="130">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t8-${$index}`">{{ row.channelFirst }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="channelSecond" label="二级渠道" width="140">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t9-${$index}`">{{ row.channelSecond }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="channelThree" label="三级渠道" width="130">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t10-${$index}`">{{ row.channelThree }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="areaName" label="区域" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t11-${$index}`">{{ row.areaName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="cityName" label="省市" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t12-${$index}`">{{ row.cityName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="carSeriesName" label="车系" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.carSeriesName }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="originalTextScene"
        label="声音片段内容"
        width="200"
        show-overflow-tooltip
      >
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.originalTextScene }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sentiment" label="情感" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.sentiment }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="intentionType" label="意图" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.intentionType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="topic" label="观点" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.topic }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="textLabel" label="分类明细" width="220" show-overflow-tooltip>
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.textLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="errorTypeText" label="错误类型" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.errorTypeText }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="correctionInfo" label="纠错明细" width="520" show-overflow-tooltip>
        <template #default="{ row, $index }">
          <pre :data-testid="`ctable-t13-${$index}`">{{ row.correctionInfo }}</pre>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatusText" label="审核状态" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`ctable-t13-${$index}`">{{ row.auditStatusText }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="160">
        <template #default="{ row, $index }">
          <el-popconfirm
            :data-testid="`ctable-t14-${$index}`"
            title="确认通过该内容？操作生效后内容将进行修改。"
            @confirm="handleOkCheck(row)"
          >
            <template #reference>
              <el-button v-if="row.auditStatus === '0'" type="text">确认</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm
            :data-testid="`ctable-t15-${$index}`"
            title="确定拒绝该内容？该操作将关闭本次申请纠错。"
            @confirm="handleRejectCheck(row)"
          >
            <template #reference>
              <el-button v-if="row.auditStatus === '0'" type="text">拒绝</el-button>
            </template>
          </el-popconfirm>
          <span v-if="row.auditStatus !== '0'">--</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="flex justify-end mt-16">
      <el-pagination
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 15, 20, 25]"
        :total="table.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
