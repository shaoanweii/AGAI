<template>
  <div class="table-wrapper cm-card" style="flex: 1; width: 0">
    <div class="table-header">
      <h3>数据列表</h3>
      <div>
        <el-button
          :icon="Plus"
          :data-testid="`dataSource-table-10001`"
          :disabled="!table.list?.length"
          @click="() => handleClickAllData()"
        >
          查看全部数据
        </el-button>
        <el-button
          v-auth="`dataCenter-dataSource-import`"
          :data-testid="`dataSource-table-10002`"
          type="primary"
          class="ml-8"
          @click="() => importShowVisible()"
        >
          <template #icon>
            <i class="iconfont icon-import"></i>
          </template>
          导入数据
        </el-button>
      </div>
    </div>
    <div class="table" :style="computedCardHeight(266)">
      <el-table
        :data-testid="`dataSource-table-10003`"
        :data="table.list"
        style="width: 100%; height: 100%"
        :height="computedCardHeight(266)"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="dataName" label="数据名称" show-overflow-tooltip width="284">
          <template #default="{ row, $index }">
            <span :data-testid="`dataSource-table-10003-t1-${$index}`">{{ row.dataName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="importResult" label="数据信息" show-overflow-tooltip min-width="284">
          <template #default="{ row, $index }">
            <span :data-testid="`dataSource-table-10003-t2-${$index}`">{{ row.importResult }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="导入时间" sortable="custom" width="180">
          <template #default="{ row, $index }">
            <span :data-testid="`dataSource-table-10003-t3-${$index}`">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="channelId" label="处理状态" width="120">
          <template #default="{ row, $index }">
            <div class="flex item-center" :data-testid="`dataSource-table-10003-t4-${$index}`">
              <el-badge
                v-if="row.status === HANDLE_STATUS.untreated"
                :data-testid="`dataSource-table-10003-t4-1-${$index}`"
                :value="''"
                :color="'#4E5969'"
              />
              <el-badge
                v-if="row.status === HANDLE_STATUS.process"
                :data-testid="`dataSource-table-10003-t4-2-${$index}`"
                :value="''"
                :color="'#165DFF'"
              />
              <el-badge
                v-if="row.status === HANDLE_STATUS.done"
                :data-testid="`dataSource-table-10003-t4-3-${$index}`"
                :value="''"
                :color="'#00B42A'"
              />
              <span class="ml-8" :data-testid="`dataSource-table-10003-t4-5-${$index}`">{{
                row.statusText || '-'
              }}</span>
              <i
                v-if="row.status === HANDLE_STATUS.done && row.fail"
                :data-testid="`dataSource-table-10003-t4-6-${$index}`"
                class="iconfont icon-tips_exclamation-circle ml-8"
              ></i>
            </div>
          </template>
        </el-table-column>

        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row, $index }">
            <el-dropdown
              placement="bottom-start"
              @command="(val: any) => handleSelectDownload(val, row, $index)"
            >
              <el-button
                :underline="false"
                :data-testid="`dataSource-table-10003-tb1-${$index}`"
                :loading="row.loading"
                :disabled="row.status === HANDLE_STATUS.process"
                type="primary"
                link
                >导出
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <!--
                      invalid ：是否有无效数据
                      fail：是否有失败数据
                      row.status === '0' 未处理
                      row.status === '2' 已完成

                          不同状态显示弹窗可选项不同：
                        - 未处理（无无效数据）：仅显示导出全部数据
                        - 未处理（有无效数据）：仅显示导出全部数据和无效数据
                        - 已完成（无无效数据、无失败数据）：仅显示导出全部数据
                        - 已完成（有无效数据、无失败数据）：仅显示导出全部数据和无效数据
                        - 已完成（无无效数据、有失败数据）：仅显示导出全部数据和失败数据
                        - 已完成（有无效数据、有失败数据）：显示导出全部数据、无效数据和失败数据

                        导出全部数据:  未处理（无无效数据（有无效数据） 已完成 无失败数据  有失败数据
                        导出无效数据: 未处理 已完成（有无效数据）
                        导出失败数据: 已完成、有失败数据

                    -->
                  <el-dropdown-item
                    :command="DOWNLOAD_TYPE.all"
                    :data-testid="`dataSource-table-10003-tb1-op1-${$index}`"
                  >
                    导出全部数据
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="DOWNLOAD_TYPE.invalid"
                    :data-testid="`dataSource-table-10003-tb1-op2-${$index}`"
                    :disabled="!row.invalid"
                    >导出无效数据
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="DOWNLOAD_TYPE.fail"
                    :data-testid="`dataSource-table-10003-tb1-op3-${$index}`"
                    :disabled="!(row.status === HANDLE_STATUS.done && row.fail)"
                    >导出失败数据
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-button
              v-auth="`dataCenter-dataSource-edit`"
              :data-testid="`dataSource-table-10003-tb2-${$index}`"
              :disabled="!row.processible"
              :underline="false"
              type="primary"
              link
              @click="handleStartProcessing(row)"
              >处理
            </el-button>
            <el-button
              v-auth="`dataCenter-dataSource-edit`"
              :data-testid="`dataSource-table-10003-tb3-${$index}`"
              :disabled="!(row.status === HANDLE_STATUS.done)"
              :underline="false"
              type="primary"
              link
              @click="handleDetail(row)"
              >查看
            </el-button>
            <el-button
              v-auth="`dataCenter-dataSource-delete`"
              :data-testid="`dataSource-table-10003-tb4-${$index}`"
              :disabled="!(row.status === HANDLE_STATUS.untreated)"
              :underline="false"
              type="primary"
              link
              @click="handleDel(row)"
              >删除
            </el-button>
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
    <!--导入数据-->
    <ImportView
      v-model="importVisible"
      :curDataSource="curDataSource"
      @refreshList="getFirstPageTableData"
    ></ImportView>
    <!--查看全部数据-->
    <DetailView
      v-model="detailVisible"
      :curDataSource="curDataSource"
      :curDataSourceDetail="curDataSourceDetail"
    ></DetailView>
  </div>
</template>
<script lang="ts" setup>
import { Plus } from '@element-plus/icons-vue'
import { useTable } from '@/hooks/table'
import ImportView from './ImportView.vue'
import DetailView from './DetailView.vue'
import { useModal } from '@/hooks/useModal'
import { deleteDataSourceDetail, startProcessing, exportRawDataByStatus } from '@/api/dataCenter'
import type { DataSourceDetail } from '@/types/dataCenter.types'
import { debounce } from 'lodash-es'
import { computedCardHeight, listHeight } from '@/utils'
import { DOWNLOAD_TYPE, HANDLE_STATUS } from '@/constant'
import { useExport } from '@/hooks/useExport'
import useUserStore from '@/stores/modules/user'
import { useAppStore } from '@/stores'

const { visible: importVisible, showVisible: importShowVisible } = useModal()
const { visible: detailVisible, showVisible: detailShowVisible } = useModal()
const userStore = useUserStore()

const {
  table,
  form,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  // handleView,
  handleSortChange,
  getFirstPageTableData,
  sortOpts
} = useTable({
  method: 'POST',
  url: '/insights/insDataSource/findDataSourceDetail'
})

provide('form', form)

const curDataSourceDetail = ref<DataSourceDetail>()
const handleDetail = (record: DataSourceDetail) => {
  curDataSourceDetail.value = record
  detailShowVisible()
}

const handleClickAllData = () => {
  curDataSourceDetail.value = undefined
  detailShowVisible()
}
// 处理
const handleStartProcessing = debounce(async (record: any) => {
  try {
    const response = await startProcessing({
      batchId: record.batchId,
      clientId: userStore.clientId!,
      dataSourceId: curDataSource.value.id
    })
    if (response.code === '200') {
      getFirstPageTableData()
    }
  } catch (e) {}
}, 300)

const handleDel = async (record: any) => {
  console.log('record', record)
  try {
    await deleteDataSourceDetail({
      batchId: record.batchId,
      clientId: userStore.clientId
    })
    getFirstPageTableData()
  } catch (e) {}
}

const dataSourceList = ref<any[]>([])
const curDataSource = ref<any>()
const refreshTable = async (cur: any, list: any[]) => {
  dataSourceList.value = list
  curDataSource.value = cur
  table.filter.dataSourceId = cur?.id
  table.filter.clientId = userStore.clientId
  await getFirstPageTableData()
}

const clearTable = () => {
  // 数据源没有数据 清空数据详情， 清空数据源id
  table.filter.dataSourceId = ''
  table.filter.clientId = userStore.clientId
  table.list = []
}

const timer = ref()

onMounted(() => {
  timer.value = setInterval(() => {
    table.filter.clientId = userStore.clientId
    if (table.filter.dataSourceId && table.filter.clientId) {
      getTableData()
    }
  }, 30000)
})

onBeforeUnmount(() => {
  timer.value && clearInterval(timer.value)
})

const { exportFile } = useExport()

const handleSelectDownload = debounce(async (val: DownLoadType, record: any, rowIndex: number) => {
  let params: Api.InsDataSource.ExportRawParams = {
    batchId: record.batchId,
    clientId: userStore.clientId!,
    dataSourceId: curDataSource.value.id,
    dataValidity: undefined,
    status: undefined
  }
  try {
    table.list[rowIndex].loading = true

    params.dataValidity = val === DOWNLOAD_TYPE.invalid ? '0' : undefined
    params.status = val === DOWNLOAD_TYPE.fail ? '-1' : undefined

    await exportFile(exportRawDataByStatus, params)
  } finally {
    table.list[rowIndex].loading = false
  }
}, 300)

defineExpose({
  refreshTable,
  clearTable
})
</script>
