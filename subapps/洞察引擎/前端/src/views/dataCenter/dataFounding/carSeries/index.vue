<template>
  <div class="main-table">
    <FtCard hide-title>
      <el-form inline :model="table.filter">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="归属品牌" class="w-full">
              <el-select v-if="!(conditions.brand?.length > 0)" placeholder="全部" :options="[]" />
              <el-select
                v-if="conditions.brand?.length > 0"
                :data-testid="`founding-carseries-10001`"
                v-model="table.filter.brandFilters"
                placeholder="全部"
                clearable
                multiple
                collapse-tags
                :max-collapse-tags="1"
                filterable
                popper-class="carSeries-brand-select"
              >
                <el-option
                  v-for="(item, index) in conditions.brand"
                  :label="item.value"
                  :value="item.code"
                  :key="item.key"
                  :data-testid="`founding-carseries-10001-${index}`"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="车辆级别" class="w-full">
              <el-cascader
                :data-testid="`founding-carseries-10002`"
                v-model="table.filter.carTypeFilter"
                :max-collapse-tags="1"
                collapse-tags
                :options="conditions.carType"
                clearable
                :props="carTypeCascaderProps"
                placeholder="请选择"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="能源类型" class="w-full">
              <el-cascader
                :data-testid="`founding-carseries-10003`"
                v-model="table.filter.energyTypeFilter"
                :max-collapse-tags="1"
                collapse-tags
                :options="conditions.energy"
                clearable
                :props="energyTypeCascaderProps"
                placeholder="请选择"
                class="w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="车系名称" class="w-full">
              <el-input
                :data-testid="`founding-carseries-10004`"
                v-model.trim="table.filter.nameFilter"
                placeholder="请输入"
                :maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6" :offset="18">
            <div class="w-full flex justify-end">
              <el-button
                :data-testid="`founding-carseries-10005`"
                color="#F2F3F5"
                style="margin-right: 8px"
                @click="handleResetTableData"
                >重置
              </el-button>
              <el-button
                :data-testid="`founding-carseries-10006`"
                type="primary"
                @click="getFirstPageBrandCarseriesTableData"
                >查询
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </FtCard>

    <div class="flex mt-24">
      <div
        class="table-wrapper cm-card"
        style="margin-right: 24px; width: 380px"
        :style="computedCardHeight(226)"
      >
        <div class="table-header">
          <h3>品牌列表</h3>
          <el-button
            v-auth="`dataCenter-carSeries-add`"
            :data-testid="`founding-carseries-10007`"
            type="primary"
            @click="handleAdd"
          >
            <template #icon>
              <Plus />
            </template>
            新增品牌
          </el-button>
        </div>
        <div :style="computedCardHeight(274)">
          <el-table
            :data="table.list"
            v-loading="table.loading"
            style="width: 100%; height: 100%"
            :row-class-name="
              ({ row }: any) => {
                return brandCode == row.id ? 'row-active' : ''
              }
            "
            class="simple-table"
          >
            <el-table-column label="品牌列表" show-overflow-tooltip>
              <template #header>
                <span
                  class="list-title point"
                  style="font-weight: bold"
                  :class="{ active: brandCode == '' }"
                  @click="handleBrandChange('')"
                  >全部品牌</span
                >
              </template>
              <template #default="{ row, $index }">
                <div class="item">
                  <div
                    class="item-left"
                    :data-testid="`founding-carseries-20001-${$index}-s1`"
                    @click="handleBrandChange(row.id)"
                  >
                    <el-avatar
                      :size="22"
                      style="background: #fff; border: var(--border); margin-right: 8px"
                    >
                      <!-- <img :src="defaultImagePath + row.img" v-if="row.img" /> -->
                      <img :src="row.img" v-if="row.img" />
                      <i
                        v-else
                        class="iconfont icon-logo1"
                        style="font-size: 8px; color: #000; margin-left: 1px"
                      />
                    </el-avatar>
                    <span style="word-break: break-all">{{ row.name }}</span>
                  </div>
                  <div class="item-right">
                    <el-icon
                      v-auth="`dataCenter-carSeries-edit`"
                      :data-testid="`founding-carseries-20001-${$index}-s2`"
                      class="point"
                      @click.stop="handleEdit(row)"
                    >
                      <Edit />
                    </el-icon>
                    <el-popconfirm title="确认删除?" @confirm="handleDeleteBrand({ id: row.id })">
                      <template #reference>
                        <el-icon
                          v-auth="`dataCenter-carSeries-delete`"
                          :data-testid="`founding-carseries-20001-${$index}-s3`"
                          class="ml-16 point"
                        >
                          <Delete />
                        </el-icon>
                      </template>
                    </el-popconfirm>
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
        :filter="{
          ...table.filter,
          brandId: brandCode == '' ? undefined : brandCode
        }"
      />
    </div>
    <BrandForm @refreshList="getNewBrandInfo" />
  </div>
</template>

<script lang="ts" setup>
import BrandForm from './BrandForm.vue'
import Table from './Table.vue'
import { useTable } from '@/hooks/table'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import FtCard from '@/components/FtCard.vue'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'
import useConditions from '@/hooks/useConditions'

// const defaultImagePath = import.meta.env.VITE_DEFAULT_IMAGE_PATH

// Cascader props 配置
const carTypeCascaderProps = {
  value: 'key',
  label: 'value',
  multiple: true,
  expandTrigger: 'hover'
}

const energyTypeCascaderProps = {
  value: 'key',
  label: 'value',
  multiple: true,
  expandTrigger: 'hover'
}

const { conditions } = useConditions({ url: '/insights/brandInfo/conditions' })
provide('conditions', conditions)
const {
  table,
  form,
  // handleReset,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleDelete,
  getFirstPageTableData
} = useTable({
  method: 'POST',
  url: '/insights/brandInfo/queryBySelect',
  deleteUrl: '/insights/brandInfo/deleteBrandInfo'
})
// 当前选中品牌
const brandCode = ref()
const tableRef = ref()
onMounted(() => {
  getNewTableData()
})

provide('form', form)

const handleBrandChange = (code: string) => {
  brandCode.value = code
  nextTick(() => {
    getSeriesData()
  })
}

const getSeriesData = () => {
  tableRef.value?.getTableData()
}

const getFirstPageBrandCarseriesTableData = () => {
  table.pageNum = 1
  getNewTableData()
}

const handleResetTableData = async () => {
  table.filter = {}
  table.pageNum = 1
  getNewTableData()
}
const getNewTableData = async () => {
  const res: any = await getTableData()
  if (
    table.filter.carTypeFilter?.length ||
    table.filter.energyTypeFilter?.length ||
    table.filter.nameFilter?.length
  ) {
    brandCode.value = ''
  } else {
    brandCode.value = res?.list.length && res.list[0].id
  }

  handleBrandChange(brandCode.value)
}
const getNewBrandInfo = () => {
  getFirstPageTableData()
}
const handleDeleteBrand = ({ id }: { id: number }) => {
  handleDelete({ id })
}
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

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.point {
  cursor: pointer;
}

.list-title {
  &.active {
    color: #409eff;
  }
}
</style>
