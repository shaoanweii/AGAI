<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'
import searchPng from '@/assets/imgs/rules/search.png'
import { batchEventStore } from '../store'
import { copyBatchEventRule, createDefaultBatchRule, fetchBatchEventRulePage } from '../ruleApi'
import RuleFormDialog from './RuleFormDialog.vue'
import { useBatchEventContext } from '../useBatchEventContext'
import type { BatchRuleRecord, BatchRuleView } from '../types'

defineOptions({
  name: 'BatchEventRuleList'
})
const { currentCategory, notifyRuleChanged } = useBatchEventContext()

const hub = reactive({
  loading: false,
  keyword: '',
  pageNum: 1,
  pageSize: 10,
  total: 0,
  ruleList: [] as BatchRuleView[],
  visible: false,
  ruleData: null as Partial<BatchRuleRecord> | null
})
const fetchListRequestId = ref(0)
const ruleTypeLabelMap = computed(() => {
  // 规则类型展示统一取 closedRuleType 字典，避免列表直接显示后端 code
  return Object.fromEntries(
    (batchEventStore.batchConditions.closedRuleType || []).map((item: any) => [
      item.key,
      item.value
    ])
  ) as Record<string, string>
})
const emptyText = computed(() => {
  if (!currentCategory.value?.id) {
    return '请选择左侧分类后查看规则'
  }

  return hub.keyword.trim() ? '暂无匹配规则' : '当前分类暂无规则'
})

/**
 * 查询批量规则列表，并保持分页数据同步。
 * @returns Promise<void>
 */
const fetchList = async () => {
  if (!currentCategory.value?.id) {
    fetchListRequestId.value += 1
    hub.loading = false
    hub.ruleList = []
    hub.total = 0
    return
  }

  const currentRequestId = ++fetchListRequestId.value
  hub.loading = true

  try {
    const response = await fetchBatchEventRulePage({
      categoryId: currentCategory.value.id,
      keyword: hub.keyword,
      pageNum: hub.pageNum,
      pageSize: hub.pageSize
    })

    if (currentRequestId !== fetchListRequestId.value) {
      return
    }

    // 列表项中的 ruleType 统一映射为字典文案，保持与单点规则页面展示口径一致
    hub.ruleList = response.list.map(item => ({
      ...item,
      ruleType: ruleTypeLabelMap.value[item.ruleType] || item.ruleType
    }))
    hub.total = response.total
  } catch (error: any) {
    if (currentRequestId !== fetchListRequestId.value) {
      return
    }

    hub.ruleList = []
    hub.total = 0
    ElMessage.error(error?.message || '获取规则列表失败')
  } finally {
    if (currentRequestId === fetchListRequestId.value) {
      hub.loading = false
    }
  }
}

/**
 * 防抖处理规则名称搜索，避免连续输入时重复触发接口查询。
 */
const handleSearch = debounce(() => {
  hub.pageNum = 1
  fetchList()
}, 300)

/**
 * 输入框清空时立即刷新，避免继续等待防抖窗口导致空列表状态延迟。
 */
const handleSearchClear = () => {
  handleSearch.cancel()
  hub.pageNum = 1
  fetchList()
}

/**
 * 切换分页大小后重置到第一页，确保当前页码始终有效。
 * @param pageSize 当前分页大小
 */
const handleSizeChange = (pageSize: number) => {
  hub.pageSize = pageSize
  hub.pageNum = 1
  fetchList()
}

/**
 * 切换页码时重新拉取当前页数据。
 * @param pageNum 当前页码
 */
const handleCurrentChange = (pageNum: number) => {
  hub.pageNum = pageNum
  fetchList()
}

/**
 * 编辑时仅透传规则标识，详细数据在弹窗内通过接口加载。
 * @param row 当前规则行
 */
const handleEdit = (row: BatchRuleView) => {
  hub.ruleData = {
    ruleId: row.ruleId,
    categoryId: row.categoryId,
    categoryName: row.categoryName
  }
  hub.visible = true
}

/**
 * 复制规则后同步刷新列表和左侧分类统计。
 * @param row 当前规则行
 * @returns Promise<void>
 */
const handleCopy = async (row: BatchRuleView) => {
  try {
    await copyBatchEventRule(row.ruleId)
    ElMessage.success('复制成功')
    await fetchList()
    notifyRuleChanged()
  } catch (error: any) {
    ElMessage.error(error?.message || '复制失败')
  }
}

/**
 * 新建规则时自动带入当前分类，减少弹框内重复选择成本。
 */
const handleCreateRule = () => {
  if (!currentCategory.value) return

  hub.ruleData = createDefaultBatchRule(currentCategory.value.id, currentCategory.value.name)
  hub.visible = true
}

/**
 * 将状态值转换为页面展示需要的文案与颜色。
 * @param status 当前状态
 * @returns {{ text: string; dotColor: string }}
 */
const getStatusMeta = (status: BatchRuleView['status']) => {
  if (status === 'enabled') {
    return {
      text: '已启用',
      dotColor: '#00b42a'
    }
  }

  return {
    text: '已禁用',
    dotColor: '#c9cdd4'
  }
}

const handleDialogSuccess = async () => {
  hub.visible = false
  await fetchList()
  notifyRuleChanged()
}

onBeforeUnmount(() => {
  handleSearch.cancel()
})

watch(
  () => `${currentCategory.value?.id || ''}-${currentCategory.value?.name || ''}`,
  () => {
    hub.pageNum = 1
    fetchList()
  },
  { immediate: true }
)
</script>

<template>
  <div class="batch-rule-list h-full flex-col">
    <div class="flex-between items-center mb-24">
      <div class="header-title-class">规则列表</div>
      <div class="flex-y-center">
        <el-input
          v-model="hub.keyword"
          class="batch-rule-list__search"
          placeholder="请输入规则名称搜索"
          clearable
          @clear="handleSearchClear"
          @input="handleSearch"
        >
          <template #suffix>
            <el-image :src="searchPng" style="width: 20px; height: 20px" />
          </template>
        </el-input>
        <el-button
          type="primary"
          class="ml-16"
          :disabled="!currentCategory"
          @click="handleCreateRule"
        >
          <template #icon>
            <Plus />
          </template>
          新建规则
        </el-button>
      </div>
    </div>

    <div class="batch-rule-list__table-wrap" v-loading="hub.loading">
      <el-table :data="hub.ruleList" row-key="ruleId" height="100%">
        <el-table-column type="selection" width="56" />
        <el-table-column prop="ruleName" label="规则名称" min-width="250" show-overflow-tooltip />
        <!--        <el-table-column prop="ruleType" label="规则类型" min-width="120" />-->
        <el-table-column label="规则类型" min-width="120">
          <template #default>
            <span>批量</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="主题分类" width="140" />
        <el-table-column prop="creatorDisplayName" label="创建人" width="180" />
        <el-table-column label="当前状态" width="120">
          <template #default="{ row }">
            <div class="batch-rule-list__status">
              <span
                class="batch-rule-list__status-dot"
                :style="{ backgroundColor: getStatusMeta(row.status).dotColor }"
              />
              <span>{{ getStatusMeta(row.status).text }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="batch-rule-list__actions">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="handleCopy(row)">复制</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="emptyText" :image-size="100" />
        </template>
      </el-table>
    </div>

    <el-pagination
      v-if="hub.total > 0"
      v-model:current-page="hub.pageNum"
      v-model:page-size="hub.pageSize"
      :total="hub.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="->, total, prev, pager, next, sizes"
      class="pt-16"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <RuleFormDialog
      v-model:visible="hub.visible"
      :ruleData="hub.ruleData"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
.header-title-class {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  line-height: 32px;
}

.batch-rule-list__search {
  width: 240px;
}

.batch-rule-list {
  // 作为 flex 子项时必须允许收缩，否则表格最小宽度会反向撑开整行布局。
  min-width: 0;
}

.batch-rule-list__table-wrap {
  flex: 1;
  min-height: 0;
  min-width: 0;
  overflow: auto;
}

.batch-rule-list__status {
  display: flex;
  align-items: center;
  color: #1d2129;
}

.batch-rule-list__status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.batch-rule-list__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-table th.el-table__cell) {
  background: #f2f4f7;
  color: #1d2129;
  font-weight: 500;
  font-size: 14px;
}

:deep(.el-table .el-table__cell) {
  height: 56px;
}

:deep(.el-table) {
  min-width: 0;
}
</style>
