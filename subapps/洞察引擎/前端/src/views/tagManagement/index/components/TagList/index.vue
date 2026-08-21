<template>
  <div class="table-wrapper cm-card" style="flex: 1; width: 0">
    <div class="table-header">
      <h3 style="line-height: 32px">标签列表</h3>
      <div class="flex">
        <el-button
          v-auth="`tagManagement-application-add`"
          type="primary"
          :icon="Plus"
          @click="handleAdd"
        >
          新增标签
        </el-button>
      </div>
    </div>
    <div class="table" :style="computedCardHeight(275)">
      <el-table
        class="detail-table"
        :data="table.list"
        style="width: 100%; height: 100%"
        :height="'100%'"
        :max-height="'100%'"
      >
        <el-table-column v-if="hasPermission('tagManagement-application-batch')" width="40">
          <template #header>
            <el-checkbox
              v-model="checkedAll"
              :indeterminate="indeterminateAll"
              @change="(val: any) => changeAllChecked(val)"
            ></el-checkbox>
          </template>
          <template #default="{ row }">
            <el-checkbox
              v-model="row.checked"
              @change="(val: any) => changeChecked(val, row)"
            ></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="标签名称" width="180" show-overflow-tooltip>
          <template #default="{ row, $index }">
            <span :data-testid="`corpus-table-10001-t0-${$index}`">{{ row.tagName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="所属分类">
          <template #default="{ row, $index }">
            <span :data-testid="`corpus-table-10001-t1-${$index}`">{{
              row.tagLibNameHierarchical || '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tagStatusText" width="170" label="创建时间">
          <template #default="{ row }">
            <span>{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <!-- :sortable="sortOpts" -->
        <el-table-column prop="tagStatusText" width="90" label="启用状态">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <el-badge v-if="row.tagStatus === '1'" status="success" />
              <el-badge v-else status="info" />
              <span class="ml-8" :data-testid="`corpus-table-10001-t3-${$index}`">{{
                row.tagStatusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row, $index }">
            <el-button
              v-auth="`tagManagement-application-edit`"
              :data-testid="`corpus-table-10001-b1-${$index}`"
              type="primary"
              link
              @click="handleEdit(row)"
              >编辑
            </el-button>
            <el-button
              v-auth="`tagManagement-application-del`"
              :data-testid="`corpus-table-10001-b2-${$index}`"
              :disabled="row.tagStatus === '1'"
              type="danger"
              link
              @click="handleDel(row)"
              >删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-between">
        <!-- 批量操作按钮 -->
        <div v-if="hasPermission('tagManagement-application-batch')" style="margin-top: 16px">
          <el-dropdown
            placement="top-start"
            :disabled="selectedKeys?.length === 0"
            @command="dropSelect"
            @visible-change="dropPopupVisibleChange"
          >
            <el-button
              v-auth="`tagManagement-application-batch`"
              :disabled="selectedKeys?.length === 0"
              type="primary"
              >批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon
            ></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1">移动</el-dropdown-item>
                <el-dropdown-item command="2">导出</el-dropdown-item>
                <el-dropdown-item command="3">启用</el-dropdown-item>
                <el-dropdown-item command="4">禁用</el-dropdown-item>
                <el-dropdown-item v-if="!deleteOptionFlag" command="5">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

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
        />
      </div>
    </div>
  </div>
  <Form :filter="filter" :curCategorizeByParent="curCategorize" @refreshList="refreshTable"></Form>
  <BatchMove
    v-model="batchMovelVisible"
    :allFinalTagLib="allFinalTagLib"
    :curCategorize="curCategorize"
    :filter="filter"
    :selectedKeys="selectedKeys"
    @refreshList="refreshTable"
  ></BatchMove>

  <TipModal v-model="modelVisible" :modelType="modelType" @ok="tipModalOk" />
</template>
<script lang="ts" setup>
import Form from './Form.vue'
import { useTable } from '@/hooks/table'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'
import TipModal from './TipModal.vue'
import { debounce } from 'lodash-es'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useExport } from '@/hooks/useExport'
import useUserStore from '@/stores/modules/user'
import {
  batchDeleteTagLibClient,
  batchDownloadTagLibClient,
  batchUpdateStatusTagLibClient,
  deleteTagLibClient,
  findAllFinalTagLibClientVoList,
  findTagLibClientList
} from '@/api/tag'
import BatchMove from './batchMove.vue'
import { useModal } from '@/hooks/useModal'
import to from 'await-to-js'
import { hasPermission } from '@/utils/permission'
import { Plus } from '@element-plus/icons-vue'

const { filter, setLoading } = defineProps<{
  filter: any
  setLoading: (val: boolean) => void
}>()

const emits = defineEmits(['refreshLeft'])
const { table, form, getTableData, handleSizeChange, handleCurrentChange, handleEdit, handleAdd } =
  useTable(
    {
      method: 'POST',
      url: '/insights/insTagLibClient/findTagLibClientList'
    },
    (res: any) => {
      if (indeterminateAll.value && selectedKeys.value?.length) {
        res.result.list.forEach((item: any) => {
          if (selectedKeys.value.includes(item.id)) {
            item.checked = true
          } else {
            item.checked = false
          }
        })
      }
      if (res.result?.list?.length && checkedAll.value) {
        res.result.list.forEach((item: any) => {
          item.checked = true
        })
      }

      return res.result
    }
  )

const { visible: batchMovelVisible, showVisible } = useModal()

const userStore = useUserStore()
const selectedKeys = ref<any[]>([])
const curCategorize = ref()
provide('form', form)

const clearCheckedStatus = () => {
  selectedKeys.value = []
  checkedAll.value = false
  indeterminateAll.value = false
}

const refreshTable = async (val?: any) => {
  if (val) {
    curCategorize.value = val
  }
  setLoading(true)
  clearCheckedStatus()
  table.filter = filter
  table.filter.tagStatusList = filter.tagStatusList?.length ? filter.tagStatusList : undefined
  table.filter.tagParentId = curCategorize.value?.id
  table.filter.appClient = userStore.clientId
  table.filter.tagName = filter.tagName ? filter.tagName : undefined

  table.pageNum = 1
  await getFinalTagLib()
  await getTableData()
  setLoading(false)
}
// -------------全选反选逻辑 start ----------
const indeterminateAll = ref(false)
const checkedAll = ref(false)
// 全选
const changeAllChecked = (val: boolean) => {
  if (val) {
    indeterminateAll.value = false
    selectedKeys.value = allFinalTagLib.value?.map((el: any) => el.id)
  } else {
    selectedKeys.value = []
  }

  table.list.forEach(item => {
    item.checked = val
  })
  if (selectedKeys.value?.length === 0) {
    indeterminateAll.value = false
  }
}
// 单选
const changeChecked = (val: boolean, record: any) => {
  const checkedResult = table.list?.filter(el => el.checked)
  if (checkedResult?.length > 0) {
    indeterminateAll.value = true
    checkedAll.value = false
  } else if (checkedResult?.length === 0) {
    indeterminateAll.value = false
  }

  if (val) {
    selectedKeys.value.push(record.id)
  } else {
    selectedKeys.value = selectedKeys.value.filter(el => el !== record.id)
  }
}

/**
 * @description: 清除table选中状态
 * @return {*}
 */
const clearTableChencked = () => {
  selectedKeys.value = []
  checkedAll.value = false
  table.list.forEach((item: any) => {
    item.checked = false
  })
}

// -------------全选反选逻辑 end----------
const allFinalTagLib = ref<Record<any, any>>([])

// 获取当前分类下的所有末级标签
const getFinalTagLib = async () => {
  const [errs, data] = await to(
    findAllFinalTagLibClientVoList({
      appClient: userStore.clientId,
      tagParentId: curCategorize.value?.id,
      tagType: filter.tagType,
      tagName: filter.tagName || undefined,
      tagStatusList: filter.tagStatusList
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
  } else {
    allFinalTagLib.value = data.result
  }
}

const deleteOptionFlag = ref(false)

/**
 * @description: 批量操作前置判断是否显示删除选项
 * @param {*} val
 * @return {*}
 */
const dropPopupVisibleChange = (val: boolean) => {
  if (val) {
    // const result = table.list .filter((el: any) => selectedKeys.value.includes(el.id))
    const result = allFinalTagLib.value
      ?.filter((el: any) => selectedKeys.value.includes(el.id))
      ?.some((el: any) => el.tagStatus?.toString() === '1')
    deleteOptionFlag.value = result

    console.log('result', result)
  }
}

const modelVisible = ref(false)
const modelType = ref()

// 批量移动
const batchMove = async () => {
  showVisible()
}

// 批量更新状态
const batchUpdateStatus = async (tagStatus: string) => {
  const [errs, data] = await to(
    batchUpdateStatusTagLibClient({
      appClient: userStore.clientId,
      ids: selectedKeys.value,
      tagStatus
    })
  )

  if (errs) {
    ElMessage.error(errs.message)
  }
  if (data) {
    refreshByAll()
    clearCheckedStatus()
  }
}
// 启用
const batchEnable = async () => {
  batchUpdateStatus('1')
}
// 禁用
const batchDisable = () => {
  batchUpdateStatus('0')
}

const dropSelect = debounce(async (val: any) => {
  modelType.value = Number(val)
  if (Number(val) === 1) {
    // 移动
    batchMove()
  } else if (Number(val) === 2) {
    // 导出
    // batchDownload()
    exportData()
  } else {
    // 启用 禁用 删除
    modelVisible.value = true
  }
}, 300)

const tipModalOk = async (type: number) => {
  if (type === 3) {
    batchEnable()
  } else if (type === 4) {
    batchDisable()
  } else if (type === 5) {
    batchDel()
  }
  selectedKeys.value = []
}

// 批量删除
const batchDel = async () => {
  setLoading(true)
  const [errs, data] = await to(
    batchDeleteTagLibClient({
      appClient: userStore.clientId,
      ids: selectedKeys.value
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
  }
  if (data) {
    setLoading(false)
    refreshByAll()
  }
}

const { exportFile, exporting } = useExport()
// 导出数据
const exportData = debounce(async () => {
  setLoading(true)
  const [errs] = await to(
    exportFile(batchDownloadTagLibClient, {
      // ...filter,
      appClient: userStore.clientId,
      ids: selectedKeys.value,
      tagType: curCategorize.value?.tagType || filter.tagType
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
    setLoading(false)
    return
  }

  clearTableChencked()
  setLoading(false)
}, 300)

// 单条删除
const handleDel = debounce(async (record: any) => {
  if (record.id) {
    try {
      setLoading(true)
      await deleteTagLibClient({ id: record.id, appClient: userStore.clientId })
      refreshByAll()
    } catch (error) {
      ElMessage.error('删除失败')
    } finally {
      setLoading(false)
    }
  }
}, 300)

const refreshByAll = () => {
  refreshTable()
  emits('refreshLeft')
}

defineExpose({
  refreshTable
})
</script>

<style lang="scss" scoped>
.detail-table {
  &:deep(.el-table-pagination) {
    justify-content: space-between;
  }
}
.doption-item {
  width: 88px;
  display: flex;
  justify-content: center;
}
</style>
<style>
.el-dropdown-option-active,
.el-dropdown-option:not(.el-dropdown-option-disabled):hover {
  background: #e8f3ff;
  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
}
</style>
