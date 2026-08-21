<template>
  <div class="main-table">
    <FtCard title="标签应用">
      <el-form inline :model="table.filter" class="clear-form-item-margin">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="标签类型">
              <el-select
                :data-testid="`application-10002`"
                v-model="table.filter.tagType"
                placeholder="全部"
                clearable
                @change="tagTypeChange"
              >
                <el-option
                  v-for="(item, index) in conditions.labelType"
                  :key="index"
                  :data-testid="`application-10002-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="所属分类">
              <el-cascader
                :data-testid="`application-10003`"
                v-model="table.filter.tagParentIds"
                :options="categoryTree"
                ref="tagParentIdsRef"
                multiple
                :max-collapse-tags="1"
                :props="{ value: 'id', label: 'tagName', children: 'child' }"
                :format-label="(options: any) => {
                   return formatLabelHandle(table.filter.tagParentIds, options, 'tagName')
                }"
                clearable
                placeholder="全部"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="能源类型">
              <el-select
                :data-testid="`application-10004`"
                v-model="table.filter.energy"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.energy"
                  :key="index"
                  :data-testid="`application-10004-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="启用状态">
              <el-select
                :data-testid="`application-10005`"
                v-model="table.filter.tagStatus"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.enableType"
                  :key="index"
                  :data-testid="`application-10005-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="标签名称">
              <el-input
                :data-testid="`application-10006`"
                v-model.trim="table.filter.tagName"
                placeholder="请输入"
                :maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" :offset="6">
            <div class="w-full flex justify-end">
              <el-button
                :data-testid="`application-10007`"
                color="#F2F3F5"
                style="margin-right: 8px"
                @click="handleReset"
                >重置
              </el-button>
              <el-button
                :data-testid="`application-10008`"
                type="primary"
                @click="getFirstPageTableData"
                >查询
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </FtCard>

    <FtCard
      :style="computedCardHeight(177)"
      title="标签列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24"
    >
      <template #extra>
        <div class="flex item-center">
          <el-button class="ml-16">
            <i class="iconfont icon-import mr-8"></i>
            导出
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="table.loading"
        :data-testid="`application-30001`"
        :data="table.list"
        style="width: 100%"
        :max-height="'100%'"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="tagTypeText" width="275" label="标签类型">
          <template #default="{ row, $index }">
            <span :data-testid="`application-30001-t1-${$index}`">{{ row.tagTypeText }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagName" width="275" label="标签名称">
          <template #default="{ row, $index }">
            <span :data-testid="`application-30001-t2-${$index}`">{{ row.tagName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagCode" sortable="custom" label="所属分类" width="261">
          <template #default="{ row, $index }">
            <span :data-testid="`application-30001-t3-${$index}`">{{
              row.tagLibNameHierarchical || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagDescription" width="275" label="标签定义">
          <template #default="{ row, $index }">
            <span :data-testid="`application-30001-t4-${$index}`">{{
              row.tagDescription || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="energyTypeText" width="275" label="能源类型">
          <template #default="{ row, $index }">
            <span :data-testid="`application-30001-t5-${$index}`">{{
              row.energyTypeText?.join('、') || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagStatus" width="100" label="启用状态">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <span
                class="status-circle"
                :class="[row.tagStatus === '1' ? 'success-bg' : 'forbidden-bg']"
              ></span>
              <span :data-testid="`application-30001-t6-${$index}`">{{
                row.tagStatusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row, $index }">
            <el-link
              v-auth="`tagManagement-application-edit`"
              :data-testid="`application-30001-b1-${$index}`"
              :underline="false"
              type="primary"
              @click="handleEdit(row)"
              >编辑
            </el-link>
            <!--<el-link-->
            <!--  :underline="false"-->
            <!--  type="primary"-->
            <!--  @click="handleDelete({id: row.id, appClient: table.filter.appClient })"-->
            <!--&gt;删除-->
            <!--</el-link>-->
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
        style="margin-top: 16px; justify-content: flex-end"
        small
      />
    </FtCard>

    <Form @refreshList="getFirstPageTableData" />
  </div>
</template>

<script setup lang="ts">
import { useTable } from '@/hooks/table'
import Form from './components/Form.vue'
import { findTagLibClientCategoryTree } from '@/api/tag'
import useConditions from '@/hooks/useConditions'
import useUserStore from '@/stores/modules/user'
import useComputedCascaderWidth from '@/hooks/useComputedCascaderWidth'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

const { refDom: tagParentIdsRef, formatLabelHandle } = useComputedCascaderWidth()

const userStore = useUserStore()

const {
  table,
  form,
  handleReset,
  getFirstPageTableData,
  handleSizeChange,
  handleCurrentChange,
  // handleAdd,
  handleEdit,
  handleSortChange
} = useTable({
  url: '/insights/insTagLibClient/findTagLibClientList',
  deleteUrl: '/insights/insTagLibClient/deleteTagLibClient',
  method: 'POST',
  notResetKey: ['appClient']
})
const { conditions } = useConditions({ url: '/insights/insTagLibClient/conditions' })

const categoryTree = ref<Record<string, any>[]>([])

const tagTypeChange = async (val: any) => {
  table.filter.tagParentIds = []
  if (val && val?.length) {
    // categoryTree.value = await findTagLibCategoryTree(table.filter.tagType).then(res => res.result);
    categoryTree.value = await findTagLibClientCategoryTree(
      table.filter.appClient,
      table.filter.tagType
    ).then(res => res.result)
  } else {
    categoryTree.value = []
  }
}

onMounted(async () => {
  table.filter.appClient = userStore.clientId
  getFirstPageTableData()
})

// const handleAddTag = () => {
//   handleAdd({ appClient: table.filter.appClient })
// }

provide('conditions', conditions)
provide('form', form)
</script>

<style lang="scss" scoped>
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
