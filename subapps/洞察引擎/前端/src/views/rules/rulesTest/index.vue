<script setup lang="ts">
import RulesTestDialog from './components/RulesTestDialog.vue'
import ResultViewDialog from './components/ResultViewDialog.vue'
import { useTable } from '@/hooks/table'
import { queryCreateUserList, copyRuleTest, startRuleTest } from '@/api/rules'
import { computed, onMounted, ref } from 'vue'
import { computedCardHeight, listHeight } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rulesTestActions, rulesTestStore } from './store'
import { singleEventActions } from '../closedLoopRules/SingleEvent/store'
import FFilterLayout from '@/components/UI/FFilterLayout/index.vue'

defineOptions({
  name: 'rulesTest'
})

const {
  table,
  form,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getFirstPageTableData,
  handleAdd,
  handleEdit,
  handleDelete
} = useTable(
  {
    method: 'POST',
    url: '/insights/ruleTest/ruleTestList',
    deleteUrl: '/insights/ruleTest/delRuleTest'
  },
  res => {
    // 规则测试列表数据转换
    const result = res.result || {}
    const list = (result.list || []) as any[]
    return {
      list,
      total: result.total || 0
    }
  }
)

// 创建人下拉
const createUserOptions = ref<string[]>([])
const isFilterExpanded = ref(false)
const tableCardStyle = computed(() => computedCardHeight(isFilterExpanded.value ? 239 : 175))
const fetchCreateUserOptions = async () => {
  try {
    const res = (await queryCreateUserList({})) as any
    createUserOptions.value = res?.result || []
  } catch {
    createUserOptions.value = []
  }
}

const query = () => {
  getFirstPageTableData()
}

const reset = () => {
  handleReset()
}

const init = () => {
  // 如果后续有规则测试相关字典，这里可以通过 store 统一拉取
  rulesTestActions.updateDicts()
  fetchCreateUserOptions()
  singleEventActions.updateDicts()
  singleEventActions.updateChannelTree()
  singleEventActions.updateAllResourceTree()
}

init()

onMounted(() => {
  query()
})

/**
 * 删除单条记录
 */
const rewriteDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该项任务吗？删除后无法恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    handleDelete({ id: row.id })
  })
}

/**
 * 复制测试任务
 */
const handleCopy = async (row: any) => {
  try {
    const res: any = await copyRuleTest({ id: row.id })
    if (res.success) {
      ElMessage.success('复制成功')
      getFirstPageTableData()
    } else {
      ElMessage.error(res.message || '复制失败')
    }
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

/**
 * 开始测试任务
 */
const startLoading = ref<string>('')
const handleStart = async (row: any) => {
  try {
    startLoading.value = row.id
    const res: any = await startRuleTest({ id: row.id })
    if (res.success) {
      ElMessage.success('开始成功')
      getFirstPageTableData()
    } else {
      ElMessage.error(res.message || '开始失败')
    }
  } catch (error) {
    ElMessage.error('开始失败')
  } finally {
    startLoading.value = ''
  }
}

// 查看结果弹框显隐
const resultVisible = ref(false)
const currentTestId = ref('')

/**
 * 获取状态标签样式
 */
const getStatusTagType = (status?: string) => {
  let color = ''
  switch (status) {
    case '0':
      color = '#C9CDD4' // 待测试 / 进行中
      break
    case '5':
      color = '#1677FF' // 进行中
      break
    case '3':
      color = '#C9CDD4' // 已撤销 / 已关闭
      break
    case '2':
      color = '#FF5959' // 测试失败
      break
    case '1':
      color = '#00B42A' // 测试通过
      break
    default:
      color = 'red'
      break
  }
  return { backgroundColor: color }
}
</script>

<template>
  <div class="rules-test-page">
    <!-- 查询条件 -->
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout v-model="isFilterExpanded" @query="query" @reset="reset">
        <el-form :inline="true" :model="table.filter">
          <el-form-item label="测试信息" style="width: 287px">
            <el-input
              v-model.trim="table.filter.ruleTestInfo"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
          <el-form-item label="规则类型" style="width: 188px">
            <el-select
              v-model="table.filter.ruleType"
              placeholder="全部"
              collapse-tags
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in rulesTestStore.conditions.ruleType"
                :key="index"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建人" style="width: 188px">
            <el-select
              v-model="table.filter.createUserName"
              placeholder="全部"
              multiple
              collapse-tags
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in createUserOptions"
                :key="index"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="测试状态" style="width: 188px">
            <el-select
              v-model="table.filter.testStatus"
              placeholder="全部"
              collapse-tags
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in rulesTestStore.conditions.ruleTest"
                :key="index"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </FFilterLayout>
    </FtCard>

    <!-- 列表区域 -->
    <FtCard
      :style="tableCardStyle"
      title="任务列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
    >
      <template #extra>
        <el-button type="primary" @click="handleAdd">新建测试</el-button>
      </template>

      <div class="table" :style="computedCardHeight(320)">
        <el-table
          v-loading="table.loading"
          :data="table.list"
          style="width: 100%; height: 100%"
          :height="listHeight(230)"
          @sort-change="handleSortChange"
        >
          <el-table-column type="index" label="#" width="56" align="center" />
          <el-table-column prop="ruleTestInfo" label="测试信息" width="241">
            <template #default="{ row }">
              <div class="flex-y-center">
                <el-text truncated style="color: #1d2129">{{ row.ruleTestInfo }} </el-text>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="ruleTypeText" label="规则类型" width="120" />
          <el-table-column prop="ruleCount" label="规则数量" width="120" />
          <el-table-column prop="sampleCount" label="样本数量" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="205" />
          <el-table-column prop="createUser" label="创建人" width="120" />
          <el-table-column prop="finishTime" label="完成时间" width="205">
            <template #default="{ row }">
              <span>{{ row.finishTime || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="testStatusStr" label="当前状态" width="127">
            <template #default="{ row }">
              <div class="flex-y-center">
                <div class="status-icon mr-8" :style="getStatusTagType(row.testStatus)"></div>
                <span>{{ row.testStatusStr }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="220">
            <template #default="{ row }">
              <div class="rule-test-actions">
                <el-button
                  v-if="row.testStatus === '0'"
                  link
                  type="primary"
                  :loading="startLoading === row.id"
                  :disabled="startLoading === row.id"
                  @click="handleStart(row)"
                >
                  开始
                </el-button>
                <el-button
                  v-else-if="row.testStatus === '1'"
                  link
                  type="primary"
                  :disabled="startLoading === row.id"
                  @click="
                    () => {
                      currentTestId = row.id
                      resultVisible = true
                    }
                  "
                >
                  查看
                </el-button>
                <el-button
                  v-if="row.testStatus !== '1'"
                  link
                  type="primary"
                  :disabled="startLoading === row.id"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="primary"
                  :disabled="startLoading === row.id"
                  @click="rewriteDelete(row)"
                >
                  删除
                </el-button>
                <el-button
                  link
                  type="primary"
                  :disabled="startLoading === row.id"
                  @click="handleCopy(row)"
                >
                  复制
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <el-pagination
          v-if="table.total > 0"
          v-model:current-page="table.pageNum"
          v-model:page-size="table.pageSize"
          :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
          :total="table.total"
          layout="->,total, prev, pager, next, sizes"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pt-16"
        />
      </div>
    </FtCard>

    <RulesTestDialog
      v-model:visible="form.visible"
      :record="form.data"
      @success="getFirstPageTableData"
    />
    <ResultViewDialog v-model:visible="resultVisible" :test-id="currentTestId" />
  </div>
</template>

<style lang="scss" scoped>
.rules-test-page {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

:deep(.el-form-item__label) {
  color: #1f2733 !important;
}

.flex-y-center {
  display: flex;
  align-items: center;
}

.status-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.rule-test-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;

  :deep(.el-button + .el-button) {
    margin-left: 0;
  }
}

.table {
  min-width: 0;
  overflow: hidden;
}
</style>
