<template>
  <div class="main-table">
    <FtCard hide-title>
      <el-form inline :model="table.filter" class="clear-form-item-margin">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="数据详情" class="w-full">
              <el-input
                :data-testid="`founding-index-10001`"
                v-model.trim="table.filter.nameDescFilter"
                placeholder="请输入"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="启用状态" class="w-full">
              <el-select
                :data-testid="`founding-index-10002`"
                v-model="table.filter.statusFilters"
                multiple
                placeholder="全部"
                :max-collapse-tags="1"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.repositoryStatus"
                  :key="index"
                  :data-testid="`founding-index-10002-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" :offset="6">
            <div class="w-full flex justify-end">
              <el-button
                :data-testid="`founding-index-10003`"
                color="#F2F3F5"
                style="margin-right: 8px"
                @click="handleDetailReset"
                >重置
              </el-button>
              <el-button :data-testid="`founding-index-10004`" type="primary" @click="getDetailData"
                >查询
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </FtCard>

    <div class="mt-24">
      <div v-loading="tableLoading" style="width: 100%; display: flex">
        <div
          class="table-wrapper cm-card"
          style="margin-right: 24px; width: 380px"
          :style="computedCardHeight(182)"
        >
          <div class="table-header">
            <h3>资源组列表</h3>
            <el-button
              v-auth="`dataCenter-resources-add`"
              :data-testid="`founding-index-20001`"
              type="primary"
              @click="handleAdd"
            >
              <template #icon>
                <el-icon><Plus /></el-icon>
              </template>
              新增资源组
            </el-button>
          </div>
          <div :style="computedCardHeight(230)">
            <el-table
              :data="table.list"
              style="width: 100%; height: 100%"
              :max-height="'100%'"
              :row-class-name="
                ({ row }: any) => {
                  return groupId == row.id ? 'row-active' : ''
                }
              "
              class="simple-table"
            >
              <el-table-column label="资源组名称" show-overflow-tooltip>
                <template #header>
                  <span class="list-title" style="font-weight: bold">资源组名称</span>
                </template>
                <template #default="{ row, $index }">
                  <div class="item">
                    <div
                      class="item-left"
                      :data-testid="`founding-index-20003-${$index}`"
                      @click="handleGroupChange(row.id)"
                    >
                      <span
                        :data-testid="`founding-index-20004-${$index}`"
                        style="word-break: break-all"
                      >
                        {{ row.name }}
                      </span>
                    </div>
                    <div class="item-right">
                      <el-icon
                        v-auth="`dataCenter-resources-edit`"
                        :data-testid="`founding-index-20005-${$index}`"
                        @click.stop="handleDetailView(row)"
                        class="point"
                      >
                        <View />
                      </el-icon>
                      <el-icon
                        v-auth="`dataCenter-resources-edit`"
                        class="point ml-16"
                        :data-testid="`founding-index-20006-${$index}`"
                        @click.stop="handleEdit(row)"
                      >
                        <Edit />
                      </el-icon>
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
        <Table
          ref="tableRef"
          :filter="{ ...table.filter, resourceId: groupId == '' ? undefined : groupId }"
        />
      </div>
    </div>
    <GroupForm @refreshList="query" />
    <DetailView />
  </div>
</template>

<script lang="ts" setup>
import GroupForm from './GroupForm.vue'
import DetailView from './DetailView.vue'
import Table from './Table.vue'
import { useTable } from '@/hooks/table'
import type { ListItem, DetailObj } from './type.d'
import { Plus, View, Edit } from '@element-plus/icons-vue'
import useConditions from '@/hooks/useConditions'
import FtCard from '@/components/FtCard.vue'
import { computedCardHeight } from '@/utils'
import useUserStore from '@/stores/modules/user'
import { debounce } from 'lodash-es'
import { useAppStore } from '@/stores'

const { conditions } = useConditions({ url: '/insights/insDataResource/conditions' })
provide('insDataResourceConditions', conditions)
const {
  table,
  form,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  getFirstPageTableData
} = useTable({
  method: 'POST',
  url: '/insights/insDataResource/list',
  notResetKey: ['customer']
})
// 当前选中资源组
const groupId = ref('')
const tableRef = ref()
let userStore = useUserStore()

onMounted(() => {
  // 重置客户为默认客户
  userStore.setCilenId(userStore.defaultClientId)
  query()
})

const handleDetailReset = () => {
  delete table.filter.statusFilters
  delete table.filter.nameDescFilter
  tableRef.value?.handleReset()
}

const tableLoading = ref(false)
const getDetailData = async () => {
  tableLoading.value = true
  await tableRef.value?.getFirstPageTableData()
  tableLoading.value = false
}

const handleGroupChange = debounce((code: string) => {
  groupId.value = code
  nextTick(() => {
    getDetailData()
  })
}, 300)

const query = async () => {
  table.filter.customer = userStore.clientId
  await getFirstPageTableData()
  groupId.value = table.list?.length && table.list[0].id

  handleGroupChange(groupId.value)
}

let detailObj = reactive<DetailObj>({
  visible: false,
  data: {}
})
const handleDetailView = (item: ListItem) => {
  detailObj.data = JSON.parse(JSON.stringify(item))
  detailObj.visible = true
  form.operation = 'view'
}

provide('detailObj', detailObj)
provide('form', form)

defineExpose({ query })
</script>

<style lang="scss" scoped>
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

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.point {
  cursor: pointer;
}
</style>
