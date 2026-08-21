<script setup lang="ts">
import { useTable } from '@/hooks/table'
import type { Options } from '@/hooks/table.d'
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import { debounce } from 'lodash-es'

const props = defineProps({
  record: {
    type: Object,
    default: () => {}
  }
})

const { record } = toRefs(props)
const subtitle = ref('')
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
// const formData = ref({
//   status: ''
// })

const option = {
  url: '/insights/regulation/findValidateRegulationResult',
  method: 'POST'
}
const { table, handleSizeChange, handleCurrentChange, getFirstPageTableData } = useTable(
  option as Options,
  res => {
    table.finishTime = res.result.finishTime
    return res.result.pageInfo
  }
)

const formChange = debounce(() => {
  getFirstPageTableData()
}, 300)

onMounted(() => {
  console.log('record.value', record.value)
  subtitle.value = `流程测试-${record.value?.processPhaseText}-${record.value?.regulationTypeText}-${record.value?.name}-`
  table.filter.clientId = record.value.clientId
  table.filter.rulesId = record.value.id
  table.filter.channelId = record.value.channel
  table.filter.dataType = '1'
  getFirstPageTableData()
})
</script>

<template>
  <div class="flex justify-between item-center">
    <div class="title" :data-testid="`prd-tr-title`">{{ `${subtitle}${table.finishTime}` }}</div>
    <div>
      <el-select
        v-model="table.filter.hitState"
        :data-testid="`prd-tr-s1`"
        placeholder="全部"
        clearable
        style="width: 136px"
        @change="formChange"
      >
        <el-option
          v-for="(item, index) in conditions.hitState"
          :key="index"
          :data-testid="`prd-tr-s1-op-${index}`"
          :label="item.value"
          :value="item.key"
        />
      </el-select>
      <el-select
        v-model="table.filter.dataCompare"
        :data-testid="`prd-tr-s2`"
        placeholder="全部"
        clearable
        style="width: 136px; margin-left: 24px"
        @change="formChange"
      >
        <el-option
          v-for="(item, index) in conditions.dataComparison"
          :key="index"
          :data-testid="`prd-tr-s2-op-${index}`"
          :label="item.value"
          :value="item.key"
        />
      </el-select>
    </div>
  </div>
  <!--查看校验结果-->
  <div v-loading="table.loading" style="width: 100%; height: 93%">
    <el-table
      :data-testid="`prd-tr-table`"
      class="mt-20"
      :data="table.list"
      style="width: 100%; height: 90%"
      :height="'90%'"
    >
      <el-table-column label="#" width="50">
        <template #default="{ $index }">
          <span :data-testid="`prd-tr-table-t0-${$index}`">{{ $index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalText" label="原文内容" show-overflow-tooltip width="320">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t1-${$index}`">{{ row.originalText }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalTextScene" label="声音片段" show-overflow-tooltip width="272">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t2-${$index}`">{{ row.originalTextScene }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="carSeriesName" label="车系" show-overflow-tooltip width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t3-${$index}`">{{ row.carSeriesName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="channelName" label="渠道" show-overflow-tooltip width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t4-${$index}`">{{ row.channelName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="hitStateStr" label="命中状态" show-overflow-tooltip width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t5-${$index}`">{{ row.hitStateStr }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="dataCompareStr" label="数据对比" show-overflow-tooltip width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t6-${$index}`">{{ row.dataCompareStr }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="originalProcessingResult"
        label="原处理结果"
        show-overflow-tooltip
        width="176"
      >
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t7-${$index}`">{{ row.originalProcessingResult }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="newProcessingResult"
        label="新处理结果"
        show-overflow-tooltip
        width="176"
      >
        <template #default="{ row, $index }">
          <span :data-testid="`prd-tr-table-t8-${$index}`">{{ row.newProcessingResult }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-if="table.total > 0"
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 15, 20, 25]"
      :total="table.total"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </div>
</template>

<style scoped lang="scss">
.title {
  font-weight: 600;
  font-size: 16px;
  color: #1d2129;
  line-height: 24px;
}
</style>
