<script setup lang="ts">
import { ref, watch } from 'vue'
import RuleSectionBox from './RuleSectionBox.vue'
import { getExecuteRuleTestInfo } from '@/api/rules'

// Define ResultRow type
interface ResultRow {
  id: string
  name: string
  hitRule: string
  ruleId: string
}

const props = defineProps<{ row: ResultRow | null }>()

const loading = ref(false)

const fetchDetailData = async (ruleId: string) => {
  loading.value = true
  try {
    const res = await getExecuteRuleTestInfo({ ruleId })
    // TODO: 处理返回的数据
    console.log(res)
  } catch (error) {
    console.error('获取规则测试详情失败:', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.row?.ruleId,
  async newId => {
    if (newId) {
      await fetchDetailData(newId)
    }
  },
  { immediate: true }
)

// 根据点击行构造 mock 详情数据（仅演示）
const buildDetailLines = (r: ResultRow | null) => {
  if (!r)
    return [
      '规则名称：—',
      '数据来源：—',
      '数据来源项：—',
      '意图：—',
      '账号等于 选项 —',
      'AND 提及量 ≥ 数值 —',
      'AND 提及量环比 ≥ —',
      '数据类型：—',
      '数据来源：—',
      '数据来源：—'
    ]
  const base = r.hitRule?.replace(/\s/g, '')
  return [
    `规则名称：${base}`,
    '数据来源：外部',
    '数据来源项：品牌车系 等于 选项 领航品牌',
    `意图 等于 选项 ${r.name}`,
    `账号 等于 选项 ${r.name}账号`,
    'AND 提及量 ≥ 数值 1000',
    'AND 提及量 ≥ 环比百分比 40%',
    '数据类型：事实型',
    '数据来源：S',
    '数据来源：PO'
  ]
}

const buildResultLines = (r: ResultRow | null) => {
  if (!r) return ['—']
  return [
    '品牌车系 等于 选项 领航品牌',
    `意图 等于 选项 ${r.name}`,
    `账号 等于 选项 ${r.name}账号`,
    'AND 提及量 ≥ 数值 2000',
    'AND 提及量环比 ≥ 百分比 50%'
  ]
}
</script>

<template>
  <div v-loading="loading" class="h-full overflow-auto rule-detail">
    <div class="title">规则明细</div>
    <RuleSectionBox :lines="buildDetailLines(row)" class="mb16" />

    <div class="title">数据结果</div>
    <RuleSectionBox :lines="buildResultLines(row)" />
  </div>
</template>

<style scoped lang="scss">
.rule-detail {
  .title {
    font-weight: 500;
    font-size: 16px;
    color: #26292e;
    line-height: 24px;
    padding-bottom: 24px;
  }
}
.mb16 {
  margin-bottom: 16px;
}
</style>
