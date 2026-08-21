<template>
  <div>
    <FtCard
      :style="computedCardHeight(36)"
      title="关键词列表"
      model="titleOperation"
      clear-content-top-padding
    >
      <template #extra>
        <div class="flex">
          <el-form
            layout="inline"
            :model="table.filter"
            class="clear-form-item-margin"
            style="flex: 1"
          >
            <el-form-item label="关键词名称">
              <el-input
                v-model.trim="table.filter.riskKeywords"
                :data-testid="`discovery-tab3-10004`"
                placeholder="请输入"
                :maxlength="20"
                clearable
                style="width: 120px"
              />
              <div class="ml-10">
                <!-- <el-button :data-testid="`discovery-tab3-10005`" color="#F2F3F5" style="margin-right: 8px" @click="reset"
                >重置
              </el-button> -->
                <el-button :data-testid="`discovery-tab3-10006`" type="outline" @click="query"
                  >搜索</el-button
                >
              </div>
            </el-form-item>
          </el-form>
          <el-button
            v-auth="`tagManagement-keywords-add`"
            :data-testid="`discovery-tab3-10007`"
            class="ml-16"
            type="primary"
            :icon="Plus"
            @click="handleAdd"
          >
            新增关键词
          </el-button>
        </div>
      </template>
      <el-table
        :loading="table.loading"
        :data-testid="`discovery-tab3-20001`"
        :data="table.list"
        style="width: 100%"
        height="95%"
      >
        <el-table-column prop="riskKeywords" width="240" label="风险关键词">
          <template #default="{ row, $index }">
            <span :data-testid="`discovery-tab3-20001-t1-${$index}`">{{ row.riskKeywords }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="extendedWord" min-width="240" label="扩展词">
          <template #default="{ row, $index }">
            <span :data-testid="`discovery-tab3-20001-t2-${$index}`">{{ row.extendedWord }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagCategoryName" width="240" label="所属分类">
          <template #default="{ row, $index }">
            <span :data-testid="`discovery-tab3-20001-t3-${$index}`">{{
              row.tagCategoryName
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="seriousLevelText" width="120" label="严重性等级">
          <template #default="{ row, $index }">
            <span :data-testid="`discovery-tab3-20001-t4-${$index}`">{{
              row.seriousLevelText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enableStatus" width="120" label="启用状态">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <span
                class="status-circle"
                :class="[row.enableStatus === '1' ? 'success-bg' : 'forbidden-bg']"
              ></span>
              <span :data-testid="`discovery-tab3-20001-t7-${$index}`">{{
                row.enableStatusName || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row, $index }">
            <el-link
              v-auth="`tagManagement-keywords-edit`"
              :data-testid="`discovery-tab3-20001-b1-${$index}`"
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
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; display: flex; justify-content: flex-end"
      />
    </FtCard>
    <RiskWordsForm @refreshList="query"></RiskWordsForm>
  </div>
</template>

<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { Plus } from '@element-plus/icons-vue'
import RiskWordsForm from './RiskWordsForm.vue'
import useConditions from '@/hooks/useConditions'
import { debounce } from 'lodash-es'
import FtCard from '@/components/FtCard.vue'
import useUserStore from '@/stores/modules/user'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

const { conditions } = useConditions({ url: '/insights/keywords/conditions' })
provide('conditions', conditions)

const {
  table,
  form,
  // handleReset,
  getFirstPageTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleSortChange
} = useTable({
  url: '/insights/keywords/queryRisKeywordsList',
  method: 'POST',
  notResetKey: ['clientId']
})
const userStore = useUserStore()

watch(
  () => userStore.clientId,
  () => {
    query()
  },
  {
    deep: true
  }
)

// useEmitt({
//   name: emittName.clientChange,
//   callback: () => {
//     query()
//   },
// })

// const reset = debounce(() => {
//   handleReset()
// }, 300)

const query = debounce(() => {
  table.filter.clientId = userStore.clientId
  getFirstPageTableData()
}, 300)

onMounted(() => {
  query()
})

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
