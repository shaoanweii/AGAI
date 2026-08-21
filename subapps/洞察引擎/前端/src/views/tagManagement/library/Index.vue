<template>
  <div>
    <FtCard title="标签库">
      <el-form layout="inline" :model="table.filter" class="clear-form-item-margin">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="标签类型">
              <el-select
                :data-testid="`library-10001`"
                v-model="table.filter.tagType"
                placeholder="全部"
                clearable
                @change="tagTypeListChange"
              >
                <el-option
                  v-for="(item, index) in conditions.labelType"
                  :key="index"
                  :data-testid="`library-10001-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="所属分类">
              <el-cascader
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
                :data-testid="`library-10002`"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="能源类型">
              <el-select
                :data-testid="`library-10003`"
                v-model="table.filter.energy"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.energy"
                  :key="index"
                  :data-testid="`library-10003-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="启用状态">
              <el-select
                :data-testid="`library-10004`"
                v-model="table.filter.tagStatus"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.enableType"
                  :key="index"
                  :data-testid="`library-10004-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="标签名称">
              <el-input
                :data-testid="`library-10005`"
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
                :data-testid="`library-10006`"
                color="#F2F3F5"
                style="margin-right: 8px"
                @click="handleReset"
                >重置
              </el-button>
              <el-button
                :data-testid="`library-10007`"
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
        <el-button
          v-if="userStore.isAdmin"
          v-auth="`tagManagement-application-systemCall`"
          :data-testid="`application-20001`"
          @click="handleSystemCall"
        >
          <i class="iconfont icon-mind-mapping mr-8"></i>
          标签映射
        </el-button>
        <el-button
          v-auth="`tagManagement-library-add`"
          :data-testid="`library-10008`"
          class="ml-16"
          type="primary"
          icon="plus"
          @click="handleAdd"
        >
          <template #icon>
            <icon-plus />
          </template>
          新增标签
        </el-button>
      </template>
      <el-table
        v-loading="table.loading"
        :data-testid="`library-list-20001`"
        :data="table.list"
        style="width: 100%"
        :max-height="'100%'"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="tagTypeText" width="261" label="标签类型">
          <template #default="{ row, $index }">
            <span :data-testid="`library-list-20001-t1-${$index}`">{{ row.tagTypeText }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagName" width="261" label="标签名称">
          <template #default="{ row, $index }">
            <span :data-testid="`library-list-20001-t2-${$index}`">{{ row.tagName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagCode" sortable="custom" width="261" label="所属分类">
          <template #default="{ row, $index }">
            <span :data-testid="`library-list-20001-t3-${$index}`">{{
              row.tagLibNameHierarchical || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagDescription" width="185" label="标签定义">
          <template #default="{ row, $index }">
            <span :data-testid="`library-list-20001-t4-${$index}`">{{
              row.tagDescription || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="energyTypeText" width="261" label="能源类型">
          <template #default="{ row, $index }">
            <span :data-testid="`library-list-20001-t5-${$index}`">{{
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
              <span :data-testid="`library-list-20001-t7-${$index}`">{{
                row.tagStatusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="84">
          <template #default="{ row, $index }">
            <el-link
              v-auth="`tagManagement-library-edit`"
              :data-testid="`library-list-20001-b1-${$index}`"
              :underline="false"
              type="primary"
              @click="handleEdit(row)"
              >编辑
            </el-link>
            <!-- <el-link :underline="false" type="primary" @click="handleDelete({userId: row.userId})">删除</el-link> -->
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

    <SystemCall
      v-model="systemCallVisible"
      :appClient="userStore.clientId"
      @refreshList="getFirstPageTableData"
    ></SystemCall>

    <Form @refreshList="getFirstPageTableData" />
  </div>
</template>

<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { Plus } from '@element-plus/icons-vue'
import Form from './components/Form.vue'
import { findTagLibCategoryTree } from '@/api/tag'
import useConditions from '@/hooks/useConditions'
import useComputedCascaderWidth from '@/hooks/useComputedCascaderWidth'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'
import useUserStore from '@/stores/modules/user'
import SystemCall from './components/SystemCall.vue'

const { conditions } = useConditions({ url: '/insights/insTagLib/conditions' })

const userStore = useUserStore()

const { refDom: tagParentIdsRef, formatLabelHandle } = useComputedCascaderWidth()
const {
  table,
  form,
  handleReset,
  getFirstPageTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleSortChange,
  sortOpts
} = useTable({
  url: '/insights/insTagLib/findTagLibList',
  method: 'POST'
})

const systemCallVisible = ref(false)

const handleSystemCall = () => {
  systemCallVisible.value = true
}

const categoryTree = ref<Record<string, any>[]>([])

const tagTypeListChange = async (val: any) => {
  table.filter.tagParentIds = []
  if (val && val?.length) {
    categoryTree.value = await findTagLibCategoryTree(table.filter.tagType).then(res => res.result)
  } else {
    categoryTree.value = []
  }
}

onMounted(async () => {
  getFirstPageTableData()
})

provide('form', form)
provide('conditions', conditions)
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
