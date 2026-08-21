<template>
  <div class="main-table">
    <FtCard hide-title title="数据处理">
      <el-form layout="inline" :model="table.filter" class="clear-form-item-margin">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="数据源名称">
              <el-input
                v-model.trim="table.filter.dataSourceName"
                :data-testid="`founding-dataSource-10001`"
                placeholder="请输入"
                :maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6" :offset="12">
            <div class="w-full flex justify-end">
              <el-button
                color="#F2F3F5"
                :data-testid="`founding-dataSource-10003`"
                style="margin-right: 8px"
                @click="reset"
                >重置</el-button
              >
              <el-button type="primary" :data-testid="`founding-dataSource-10004`" @click="query"
                >查询</el-button
              >
            </div>
          </el-col>
        </el-row>
      </el-form>
    </FtCard>

    <div class="flex mt-24">
      <div v-loading="tableLoading" style="width: 100%; display: flex">
        <div
          class="table-wrapper cm-card"
          style="margin-right: 24px; width: 380px"
          :style="computedCardHeight(162)"
        >
          <div class="table-header">
            <h3>数据源列表</h3>
          </div>

          <div :style="computedCardHeight(266)">
            <el-table
              :data="table.list"
              style="width: 100%; height: 100%"
              :max-height="'100%'"
              :row-class-name="
                ({ row }: any) => {
                  return curDataSource?.id == row.id ? 'row-active' : ''
                }
              "
              class="simple-table"
            >
              <el-table-column label="数据源名称" show-overflow-tooltip>
                <template #header>
                  <span style="font-weight: bold">数据源名称</span>
                </template>
                <template #default="{ row, $index }">
                  <div class="item">
                    <div
                      class="item-left"
                      :data-testid="`founding-index-20003-${$index}`"
                      @click="handleDataSourceChange(row)"
                    >
                      <i
                        v-if="row.dataSourceAccessWay === 'push'"
                        class="iconfont icon-shucang"
                      ></i>
                      <i v-if="row.dataSourceAccessWay === 'api'" class="iconfont icon-api"></i>
                      <i
                        v-if="row.dataSourceAccessWay === 'upload'"
                        class="iconfont icon-bendi"
                      ></i>
                      <span style="margin-left: 8px">{{ row.dataSourceName }}</span>
                    </div>
                    <div class="item-right">
                      <!-- 系统集成模块不显示编辑和删除按钮 -->
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页组件 -->
            <el-pagination
              v-if="table.total >= useAppStore().showPaginationMinLength"
              v-model:current-page="table.pageNum"
              v-model:page-size="table.pageSize"
              :page-sizes="[10, 15, 20, 25]"
              :total="table.total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              class="pagination"
            />
          </div>
        </div>
        <Table ref="tableRef" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Table from './Table.vue'
import { useTable } from '@/hooks/table'
import FtCard from '@/components/FtCard.vue'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight } from '@/utils'
import { debounce } from 'lodash-es'
import { useAppStore } from '@/stores'

const userStore = useUserStore()

const {
  table,
  form,
  // handleReset,
  handleSizeChange,
  handleCurrentChange,
  getFirstPageTableData
} = useTable({
  method: 'POST',
  url: '/insights/insDataSource/findDataSource',
  notResetKey: ['clientId', 'dataSourceAccessWay']
})

const query = async () => {
  table.filter.dataSourceAccessWay = 'api'
  table.filter.clientId = userStore.clientId
  await getFirstPageTableData()
  if (table.list?.length) {
    handleDataSourceChange(table.list?.[0])
  } else {
    tableRef.value?.clearTable()
  }
}

const reset = () => {
  delete table.filter.dataSourceName
  // 系统集成模块始终保持 api 类型过滤
  query()
}

onMounted(() => {
  // 重置客户为默认客户
  userStore.setCilenId(userStore.defaultClientId)
  query()
})

provide('form', form)

const curDataSource = ref()

const tableLoading = ref(false)

const handleDataSourceChange = debounce((item: any) => {
  curDataSource.value = item
  nextTick(() => {
    getDataSourceList()
  })
}, 300)

const tableRef = ref()
const getDataSourceList = async () => {
  tableLoading.value = true
  await tableRef.value?.refreshTable(curDataSource.value, table.list)
  tableLoading.value = false
}

defineExpose({ query })
</script>

<style scoped lang="scss">
.simple-table {
  :deep(.el-table__row.row-active) {
    background-color: #f0f9ff;

    &:hover {
      background-color: #e0f2fe !important;
    }
  }

  .item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    .item-left {
      display: flex;
      align-items: center;
      cursor: pointer;
      flex: 1;

      &:hover {
        color: #409eff;
      }
    }

    .item-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        cursor: pointer;
        color: #606266;

        &:hover {
          color: #409eff;
        }
      }
    }
  }
}

.point {
  cursor: pointer;
}

.ml-16 {
  margin-left: 16px;
}
</style>
