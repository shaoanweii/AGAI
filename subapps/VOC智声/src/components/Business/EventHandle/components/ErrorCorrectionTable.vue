<script setup lang="ts">
import { ref } from 'vue'

// 纠错表格
defineOptions({
  name: 'ErrorCorrectionTable'
})

const tableData = ref([
  {
    field: '品牌',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '车系',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '情感',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '意图',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '观点',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '场景',
    afterValue: '',
    beforeValue: ''
  }
])
</script>

<template>
  <el-table :data="tableData" :border="false" size="large" :style="{ width: '100%' }">
    <el-table-column prop="field" label="字段" width="80" />
    <el-table-column prop="afterValue" label="编辑前数据值" />
    <el-table-column prop="beforeValue" label="修改为数据值">
      <!-- <template #default="{ row }">
        <template v-if="row.field === '品牌'">
          <el-select-v2
            v-model="row.beforeValue"
            placeholder="品牌"
            clearable
            filterable
            :options="brandOptions"
            @change="handleBrandChange"
          />
        </template>
        <template v-if="row.field === '车系'">
          <el-select-v2
            v-model="row.beforeValue"
            placeholder="车系"
            clearable
            filterable
            :options="carSeriesOptions"
          />
        </template>
        <template v-if="row.field === '情感'">
          <el-select-v2
            v-model="row.beforeValue"
            placeholder="情感"
            clearable
            :options="sentimentOptions"
          />
        </template>
        <template v-if="row.field === '意图'">
          <el-select-v2
            v-model="row.beforeValue"
            placeholder="意图"
            clearable
            :options="intentionOptions"
          />
        </template>
        <template v-if="row.field === '观点'">
          <el-select-v2
            v-model="row.beforeValue"
            placeholder="观点"
            clearable
            filterable
            :options="topicOptions"
            :props="{ label: 'tagName', value: 'tagCode' }"
          />
        </template>
      </template> -->
    </el-table-column>
  </el-table>
</template>

<style lang="scss" scoped></style>
