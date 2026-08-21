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
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
// const formData = ref({
//   status: ''
// })
const subtitle = ref('')

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
  subtitle.value = `规则校验-${record.value?.processPhaseText}-${record.value?.regulationTypeText}-${record.value?.name}-`

  table.filter.clientId = record.value.clientId
  table.filter.rulesId = record.value.id
  table.filter.channelId = record.value.channel
  table.filter.dataType = '0'
  getFirstPageTableData()
})
</script>

<template>
  <div class="flex justify-between item-center">
    <div class="title" :data-testid="`prd-cr-title`">{{ `${subtitle}${table.finishTime}` }}</div>
    <div>
      <el-select
        v-model="table.filter.hitState"
        :data-testid="`prd-cr-selected`"
        placeholder="全部"
        clearable
        style="width: 136px"
        @change="formChange"
      >
        <el-option
          v-for="(item, index) in conditions.hitState"
          :key="index"
          :data-testid="`prd-cr-selected-op-${index}`"
          :label="item.value"
          :value="item.key"
        />
      </el-select>
    </div>
  </div>
  <!--查看校验结果-->
  <div v-loading="table.loading" style="width: 100%; height: 93%">
    <el-table
      :data-testid="`prd-cr-table`"
      class="mt-20"
      :data="table.list"
      style="width: 100%; height: 90%"
      :height="'90%'"
    >
      <el-table-column label="#" width="50">
        <template #default="{ $index }">
          <span :data-testid="`prd-cr-table-t0-${$index}`">{{ $index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalText" label="原文内容" show-overflow-tooltip width="350">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-cr-table-t1-${$index}`">{{ row.originalText }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalTextScene" label="声音片段" show-overflow-tooltip width="272">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-cr-table-t2-${$index}`">{{ row.originalTextScene }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="carSeriesName" label="车系" show-overflow-tooltip width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-cr-table-t3-${$index}`">{{ row.carSeriesName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="channelName" label="渠道" show-overflow-tooltip width="96">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-cr-table-t4-${$index}`">{{ row.channelName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="hitStateStr" label="命中状态" show-overflow-tooltip width="96">
        <template #default="{ row, $index }">
          <span :data-testid="`prd-cr-table-t5-${$index}`">{{ row.hitStateStr }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="newProcessingResult"
        label="返回结果"
        show-overflow-tooltip
        width="176"
      >
        <template #default="{ row, $index }">
          <span :data-testid="`prd-cr-table-t6-${$index}`">{{ row.newProcessingResult }}</span>
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
