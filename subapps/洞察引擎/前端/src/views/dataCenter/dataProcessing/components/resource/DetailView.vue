<template>
  <el-drawer v-model="form.visible" :size="1200" @open="init" destroy-on-close>
    <template #header>
      <h4 class="fw-600">资源组详情</h4>
    </template>
    <template #default>
      <div class="drawer-body">
        <div class="header">
          <el-form layout="inline" :model="form.data">
            <el-form-item label="资源组名称:">
              <span :data-testid="`index-view-10001`">{{ form?.data?.name }}</span>
            </el-form-item>
            <!--<el-form-item label="资源组类型:">-->
            <!--  <span>{{ form?.data?.typeText }}</span>-->
            <!--</el-form-item>-->
            <!--<el-form-item label="资源组归属:">-->
            <!--  <span>{{ form?.data?.customerText }}</span>-->
            <!--</el-form-item>-->
            <el-form-item label="数据详情:" style="flex: 2">
              <span
                class="detail-item"
                v-for="(item, index) in conditions.repositoryStatus"
                :data-testid="`index-view-10002-op-${index}`"
                :key="item.key"
              >
                <span class="icon-circle" :class="item.key"></span>
                <span class="detail-text" :data-testid="`index-view-10002-value-${index}`">{{
                  item.value
                }}</span>
                <span class="detail-number" :data-testid="`index-view-10002-count-${index}`">{{
                  getStatusCount(item.key)
                }}</span>
              </span>
            </el-form-item>
          </el-form>
        </div>
        <div class="table">
          <div class="table-title flex justify-between item-center">
            <h3>应用规则</h3>
          </div>
          <div v-loading="table.loading" style="width: 100%">
            <el-table
              :data-testid="`index-view-table`"
              :data="table.list"
              :pagination="{
                total: table.total,
                current: table.pageNum,
                pageSize: table.pageSize,
                showTotal: true,
                showPageSize: true,
                pageSizeOptions: [10, 15, 20, 25]
              }"
              @page-change="handleCurrentChange"
              @page-size-change="handleSizeChange"
            >
              <template #columns>
                <el-table-column data-index="name" title="规则名称">
                  <template #cell="{ record, rowIndex }">
                    <span :data-testid="`index-view-t1-${rowIndex}`">{{ record.name }}</span>
                  </template>
                </el-table-column>
                <el-table-column data-index="regulationTypeText" title="规则类型" :size="200">
                  <template #cell="{ record, rowIndex }">
                    <span :data-testid="`index-view-t2-${rowIndex}`">{{
                      record.regulationTypeText
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column title="启用状态" :size="110">
                  <template #cell="{ record, rowIndex }">
                    <span
                      class="status-circle"
                      :data-testid="`index-view-t3-${rowIndex}`"
                      :class="record.status"
                    ></span>
                    {{ record.statusText || '-' }}
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </div>
        </div>
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script lang="ts" setup>
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import type { statusItem } from './type.d'
import { useTable } from '@/hooks/table'
import { findResourceCount } from '@/api/dataCenter'

const form = inject('detailObj') as Record<string, any>
const conditions = inject('insDataResourceConditions') as Record<string, ConditionsDetailItem[]>

const { table, getTableData, handleSizeChange, handleCurrentChange } = useTable({
  method: 'POST',
  url: '/insights/insDataResource/findResource'
})

let resourceCount = ref<statusItem[]>([])

const init = () => {
  getResourceCount()
  console.log('form', form)
  table.filter.id = form.data.id
  table.filter.customer = form.data.customer
  getTableData()
}

const getStatusCount = (key: string) => {
  const findItem = resourceCount.value.find(subItem => subItem.status === key)
  return findItem ? findItem.statusCount : '-'
}
const getResourceCount = () => {
  // let params = `/${form.data.id}`
  let params = {
    id: form.data.id,
    customer: form.data.customer
  }
  findResourceCount(params).then((res: any) => {
    if (res.code == 200) {
      resourceCount.value = res.result
    }
  })
}

const handleCancel = () => {
  form.visible = false
}

const handleOk = () => {
  form.visible = false
}
</script>

<style lang="scss" scoped>
.drawer-body {
  padding: 12px 24px;

  .el-table-th-title {
    font-weight: 600;
  }

  .el-table-pagination {
    margin-top: 28px;
  }
}

.header {
  background-color: #f7f8fa;
  padding: 12px 16px 4px;
  margin-bottom: 28px;

  .el-form {
    .el-form-item {
      flex: 1;
    }
  }
}

.table-title {
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: bold;
  }
}

i.point {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 3px;
  margin-right: 8px;
  background-color: var(--color-primary);

  &.success {
    background-color: var(--color-success);
  }

  &.error {
    background-color: var(--color-error);
  }
}

::v-deep(.el-form-item-label) {
  color: var(--color-light);
}

.detail-item {
  display: flex;
  align-items: center;
  margin-right: 16px;
  color: var(--color-high);

  .icon-circle {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 6px;
  }

  .detail-text {
    margin: 0 8px;
  }

  .enabled {
    background-color: var(--color-enabled);
  }

  .disabled {
    background-color: var(--color-disabled);
  }

  .not-enabled {
    background-color: var(--color-not-enabled);
  }

  .Enabled {
    background-color: var(--color-success);
  }

  .NotEnabled {
    background-color: var(--color-not-enabled);
  }

  .Disabled {
    background-color: var(--color-disabled);
  }
}

.status-circle {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 8px;
}

.Enabled {
  background-color: var(--color-success);
}

.NotEnabled {
  background-color: var(--color-not-enabled);
}

.Disabled {
  background-color: var(--color-disabled);
}

.enabled-bg {
  background-color: var(--color-enabled);
}

.disabled {
  background-color: var(--color-disabled);
}

.not-enabled {
  background-color: var(--color-not-enabled);
}
</style>
