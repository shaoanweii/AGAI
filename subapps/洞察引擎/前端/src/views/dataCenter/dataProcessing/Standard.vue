<template>
  <div>
    <FtCard hide-title>
      <el-form inline :model="table.filter">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="规则名称" class="w-full">
              <el-input
                :data-testid="`processing-standard-1001`"
                v-model.trim="table.filter.name"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="规则类型" class="w-full">
              <el-select
                :data-testid="`processing-standard-1002`"
                v-model="table.filter.regulationTypes"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationPreType"
                  :key="index"
                  :data-testid="`processing-standard-1002-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="内容格式" class="w-full">
              <el-select
                :data-testid="`processing-standard-1003`"
                v-model="table.filter.contentTypes"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationContentType"
                  :key="index"
                  :data-testid="`processing-standard-1003-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="启用状态" class="w-full">
              <el-select
                :data-testid="`processing-standard-1004`"
                v-model="table.filter.statusList"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationStatusType"
                  :key="index"
                  :data-testid="`processing-standard-1004-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" :offset="18">
            <div class="w-full flex justify-end">
              <el-button
                :data-testid="`processing-standard-1005`"
                color="#F2F3F5"
                style="margin-right: 8px"
                @click="handleReset(() => (table.filter.regulationClassify = 'general'))"
                >重置
              </el-button>
              <el-button
                :data-testid="`processing-standard-1006`"
                type="primary"
                @click="handleQuery"
              >
                查询
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </FtCard>
    <FtCard
      :style="computedCardHeight(178)"
      title="规则列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
    >
      <el-table
        v-loading="table.loading"
        :data="table.list"
        style="width: 100%; height: 90%"
        :height="'90%'"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="规则名称" show-overflow-tooltip width="300">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t0-${$index}`">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="规则描述" show-overflow-tooltip width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t1-${$index}`">{{
              row.description
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="regulationTypeText" label="规则类型" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t2-${$index}`">{{
              row.regulationTypeText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contentTypeText" label="内容格式" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t3-${$index}`">{{
              row.contentTypeText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="processPhaseText" label="处理阶段" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t4-${$index}`">{{
              row.processPhaseText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="regulationWeight" label="权重" sortable="custom" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t5-${$index}`">{{
              row.regulationWeight
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" sortable="custom" width="190">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-standard-2001-t6-${$index}`">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="启用状态" width="170">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <el-badge v-if="row.status === 'Enabled'" status="success" />
              <el-badge v-else status="normal" />
              <span :data-testid="`processing-standard-2001-t7-${$index}`" class="ml-8">{{
                row.statusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row, $index }">
            <el-link
              v-auth="`dataCenter-standard-disable`"
              :data-testid="`processing-standard-2001-t8-${$index}`"
              v-if="row.status === 'Enabled'"
              :underline="false"
              type="primary"
              @click="handleDisabled(row)"
              >禁用
            </el-link>
            <el-link
              v-auth="`dataCenter-standard-disable`"
              :data-testid="`processing-standard-2001-t9-${$index}`"
              v-else
              :underline="false"
              type="primary"
              @click="handleEnable(row)"
              >启用
            </el-link>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div
        v-if="table.total >= useAppStore().showPaginationMinLength"
        class="pagination-wrapper"
        style="margin-top: 16px; display: flex; justify-content: flex-end"
      >
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
    </FtCard>
  </div>
</template>

<script lang="ts" setup>
import { useTable } from '@/hooks/table'
import type { Options } from '@/hooks/table.d'

import { ElMessageBox } from 'element-plus'
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import { disabledOrEnableRegulationInfo } from '@/api/dataProcessing'
import { debounce } from 'lodash-es'
import FtCard from '@/components/FtCard.vue'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

// 类型定义
interface TableData {
  [key: string]: any
}

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const option = {
  url: '/insights/regulation/findRegulationInfoList',
  method: 'POST'
}
const {
  table,
  // form,
  // getAllSelection,
  // getSlection,
  handleReset,
  // getTableData,
  handleSizeChange,
  handleCurrentChange,
  // handleAdd,
  // handleEdit,
  // handleDelete,
  handleSortChange,
  getFirstPageTableData
} = useTable(option as Options)

onMounted(() => {
  table.filter.regulationClassify = 'general'
  handleQuery()
})

const handleQuery = () => {
  getFirstPageTableData()
}

const setStatus = (id: string, status: 'Disabled' | 'Enabled') => {
  if (!id) return
  disabledOrEnableRegulationInfo({ id, status }).then(res => {
    if (res.code === '200') {
      handleQuery()
    }
  })
}

// 禁用
const handleDisabled = (record: TableData) => {
  ElMessageBox.confirm('请确定是否已与相关负责人确认禁用当前规则', '操作提示', {
    confirmButtonText: '确定禁用',
    cancelButtonText: '取消禁用',
    type: 'warning',
    center: true
  })
    .then(() => {
      debounce(() => {
        setStatus(record.id, 'Disabled')
      }, 300)()
    })
    .catch(() => {
      console.log('取消禁用')
    })
}
// 启用
const handleEnable = (record: TableData) => {
  ElMessageBox.confirm(
    '请确定是否已完成规则校验和测试，并与相关负责人确认启用当前数据',
    '操作提示',
    {
      confirmButtonText: '确定启用',
      cancelButtonText: '取消启用',
      type: 'warning',
      center: true
    }
  )
    .then(() => {
      debounce(() => {
        setStatus(record.id, 'Enabled')
      }, 300)()
    })
    .catch(() => {
      console.log('取消启用')
    })
}
</script>

<style lang="scss">
.status-wrapper {
  display: flex;
  align-items: center;

  .status-circle {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 8px;
  }

  .success-bg {
    background-color: var(--color-success);
  }

  .forbidden-bg {
    background-color: #c9cdd4;
  }
}
</style>
