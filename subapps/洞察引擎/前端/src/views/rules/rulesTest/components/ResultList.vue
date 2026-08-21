<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import searchPng from '@/assets/imgs/rules/search.png'
import { getRuleInfo } from '@/api/rules'

const props = defineProps<{
  testId?: string
}>()

// 行数据类型（与弹框父组件保持一致）
export interface ResultRow {
  id: string
  dataId: string
  channelName: string
  brandName: string
  carSeriesName: string
  contentType: string
  hasMainPost: string
  title: string
  content: string
  sentiment: string
  intention: string
  domTagFirst: string
  domTagSecond: string
  domTagThree: string
  domTagFour: string
  topicText: string
  publishUserNickname: string
  publishUserId: string
  mainPostUserId: string
  mainPostUserName: string
}

const emit = defineEmits<{ (e: 'view', row: ResultRow): void }>()

// 右上角检索条件（仅在列表态展示）
const filters = reactive({
  ruleName: ''
})

const table = reactive({
  loading: false,
  list: [] as ResultRow[],
  total: 0,
  pageNum: 1,
  pageSize: 20
})

const columns = [
  { label: '原始单号', prop: 'dataId', width: 120, align: 'center', showOverflowTooltip: true },
  { label: '渠道名称', prop: 'channelName', width: 120, showOverflowTooltip: true },
  { label: '品牌名称', prop: 'brandName', width: 120, showOverflowTooltip: true },
  { label: '车系名称', prop: 'carSeriesName', width: 120, showOverflowTooltip: true },
  { label: '内容类型', prop: 'contentType', width: 100, showOverflowTooltip: true },
  // { label: '是否是主贴', prop: 'hasMainPost', width: 100, align: 'center' },
  { label: '标题', prop: 'title', width: 150, showOverflowTooltip: true },
  { label: '内容', prop: 'content', width: 200, showOverflowTooltip: true },
  { label: '情感', prop: 'sentiment', width: 80, align: 'center', showOverflowTooltip: true },
  { label: '意图', prop: 'intention', width: 80, align: 'center', showOverflowTooltip: true },
  { label: '全领域标签一级', prop: 'domTagFirst', width: 160, showOverflowTooltip: true },
  { label: '全领域标签二级', prop: 'domTagSecond', width: 160, showOverflowTooltip: true },
  { label: '全领域标签三级', prop: 'domTagThree', width: 160, showOverflowTooltip: true },
  { label: '全领域标签四级', prop: 'domTagFour', width: 160, showOverflowTooltip: true },
  { label: '观点', prop: 'topicText', width: 150, showOverflowTooltip: true },
  { label: '发布用户昵称', prop: 'publishUserNickname', width: 120, showOverflowTooltip: true },
  { label: '发布用户ID', prop: 'publishUserId', width: 120, showOverflowTooltip: true },
  { label: '主贴用户ID', prop: 'mainPostUserId', width: 120, showOverflowTooltip: true },
  { label: '主贴用户名称', prop: 'mainPostUserName', width: 120, showOverflowTooltip: true },
  { label: '命中规则', prop: 'ruleName', width: 180, showOverflowTooltip: true }
]

const getTableData = async () => {
  if (!props.testId) return
  table.loading = true
  table.list = []
  try {
    const res: any = await getRuleInfo({
      id: props.testId,
      pageNum: table.pageNum,
      pageSize: table.pageSize,
      ruleName: filters.ruleName
    })
    table.list = res.result?.list || []
    table.total = res.result?.total || 0
  } finally {
    table.loading = false
  }
}

watch(
  () => props.testId,
  () => {
    table.pageNum = 1
    getTableData()
  },
  { immediate: true }
)

// 搜索事件（防抖）
const handleSearch = debounce(() => {
  table.pageNum = 1
  getTableData()
}, 500)

// 分页事件
const handleSizeChange = (size: number) => {
  table.pageSize = size
  table.pageNum = 1
  getTableData()
}
const handleCurrentChange = (page: number) => {
  table.pageNum = page
  getTableData()
}

// 查看规则
const viewRule = (row: ResultRow) => emit('view', row)
</script>

<template>
  <div class="h-full flex flex-col">
    <div class="dialog-toolbar">
      <div class="left">事件列表</div>
      <div class="right">
        <el-input
          v-model.trim="filters.ruleName"
          placeholder="请输入规则名称关键词搜索"
          style="width: 228px"
          @change="handleSearch"
        >
          <template #suffix>
            <el-image :src="searchPng" style="width: 20px; height: 20px" />
          </template>
        </el-input>
      </div>
    </div>
    <!-- 列表与分页 -->
    <el-table
      v-loading="table.loading"
      :data="table.list"
      row-class-name="rules-test-row-class"
      header-row-class-name="rules-test-row-class"
      style="width: 100%"
      class="flex-auto overflow-auto"
    >
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :align="col.align"
        :show-overflow-tooltip="col.showOverflowTooltip"
      >
        <template #default="{ row }">
          {{ row[col.prop] || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="108" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="!row.ruleId || row.ruleId === '-'"
            @click="viewRule(row)"
            >查看规则</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="table.total > 0"
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="table.total"
      layout="->,total, prev, pager, next, sizes"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pt-16"
    />
  </div>
</template>
<style lang="scss">
.rules-test-row-class {
  height: 52px !important;
}
</style>
<style scoped lang="scss">
.dialog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.dialog-toolbar .left {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}
.dialog-toolbar .right {
  display: flex;
  gap: 16px;
}
:deep(.el-table .el-table__cell) {
  padding: 20px 0 !important;
}
</style>
