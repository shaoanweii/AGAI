<template>
  <div class="main-table">
    <FtCard hide-title>
      <el-form layout="inline" :model="table.filter">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="规则名称">
              <el-input
                :data-testid="`processing-custom-1001`"
                v-model.trim="table.filter.name"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="规则类型">
              <el-select
                :data-testid="`processing-custom-1002`"
                v-model="table.filter.regulationTypes"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationPostType"
                  :key="index"
                  :data-testid="`processing-custom-1002-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="处理阶段">
              <el-select
                :data-testid="`processing-custom-1003`"
                v-model="table.filter.processPhases"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationStage"
                  :key="index"
                  :data-testid="`processing-custom-1003-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="数据渠道">
              <ChannelCascader
                ref="channelRef"
                v-model="table.filter.channel"
                controller="regulation"
                :testid="`processing-custom-1004`"
                :format-label="(options: any) => {
                   return formatLabelHandle(table.filter.channel, options, 'name', '/')
                }"
                width="100%"
                multiple
              ></ChannelCascader>
              <!--<el-cascader :data-testid="`processing-custom-1004`" v-model="table.filter.channel" :options="channelOptions"-->
              <!--            :props="{value: 'key', label: 'value'}" clearable-->
              <!--            :max-collapse-tags="1" style="width:320px" placeholder="全部" multiple/>-->
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="内容格式">
              <el-select
                :data-testid="`processing-custom-1005`"
                v-model="table.filter.contentTypes"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationContentType"
                  :key="index"
                  :data-testid="`processing-custom-1005-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="启用状态">
              <el-select
                :data-testid="`processing-custom-1006`"
                v-model="table.filter.statusList"
                multiple
                :max-collapse-tags="1"
                placeholder="全部"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.regulationStatusType"
                  :key="index"
                  :data-testid="`processing-custom-1006-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" :offset="6">
            <div class="w-full flex justify-end">
              <el-button
                :data-testid="`processing-custom-1007`"
                color="#F2F3F5"
                style="margin-right: 8px"
                @click="reset"
                >重置
              </el-button>
              <el-button :data-testid="`processing-custom-1008`" type="primary" @click="handleQuery"
                >查询
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
      <template #extra>
        <el-button
          v-auth="`dataCenter-customized-add`"
          :data-testid="`processing-2001`"
          type="primary"
          @click="handleAdd"
        >
          <template #icon>
            <el-icon><Plus /></el-icon>
          </template>
          新增规则
        </el-button>
      </template>
      <el-table
        v-loading="table.loading"
        :data="table.list"
        style="width: 100%; height: 90%"
        :height="'90%'"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="规则名称" show-overflow-tooltip width="300">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t0-${$index}`">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="规则描述" show-overflow-tooltip width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t1-${$index}`">{{ row.description }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="regulationTypeText" label="规则类型" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t2-${$index}`">{{
              row.regulationTypeText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contentTypeText" label="内容格式" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t3-${$index}`">{{
              row.contentTypeText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="channelText" label="数据渠道" show-overflow-tooltip width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t4-${$index}`">{{
              (Array.isArray(row.channelText) && row.channelText?.join('、')) ||
              row.channelText?.toString()
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="processPhaseText" label="处理阶段" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t5-${$index}`">{{
              row.processPhaseText
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="regulationWeight" label="权重" sortable="custom" width="170">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t6-${$index}`">{{
              row.regulationWeight
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" sortable="custom" width="190">
          <template #default="{ row, $index }">
            <span :data-testid="`processing-custom-2001-t7-${$index}`">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="singleValidateStatus" label="校验状态" width="170">
          <template #default="{ row, $index }">
            <!--完全验证状态 -1 未测试 0 测试中 1测试成功 2 测试失败 默认 -1-->
            <div class="status-wrapper">
              <el-badge v-if="row.singleValidateStatus === '-1'" status="info" />
              <el-badge v-else-if="row.singleValidateStatus === '0'" status="warning" />
              <el-badge v-else-if="row.singleValidateStatus === '1'" status="success" />
              <el-badge v-else-if="row.singleValidateStatus === '2'" status="danger" />
              <span :data-testid="`processing-custom-2001-t8-${$index}`" class="ml-8">{{
                row.singleValidateStatusText || '-'
              }}</span>
              <i
                v-if="row.singleValidateStatus === '1'"
                class="iconfont icon-file-search-line point"
                style="font-size: 14px; color: #165dff; margin-left: 10px"
                :data-testid="`processing-2009-${$index}`"
                @click="handleResult(row, 1)"
              ></i>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fullyValidateStatus" label="测试状态" width="170">
          <template #default="{ row, $index }">
            <!--完全验证状态 -1 未测试 0 测试中 1测试成功 2 测试失败 默认 -1-->
            <div class="status-wrapper">
              <el-badge v-if="row.fullyValidateStatus === '-1'" status="info" />
              <el-badge v-else-if="row.fullyValidateStatus === '0'" status="warning" />
              <el-badge v-else-if="row.fullyValidateStatus === '1'" status="success" />
              <el-badge v-else-if="row.fullyValidateStatus === '2'" status="danger" />
              <span :data-testid="`processing-custom-2001-t9-${$index}`" class="ml-8">{{
                row.fullyValidateStatusText || '-'
              }}</span>
              <i
                v-if="row.fullyValidateStatus === '1'"
                class="iconfont icon-file-search-line point"
                style="font-size: 14px; color: #165dff; margin-left: 10px"
                :data-testid="`processing-2010-${$index}`"
                @click="handleResult(row, 2)"
              ></i>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="启用状态" width="170">
          <template #default="{ row, $index }">
            <div class="status-wrapper">
              <el-badge v-if="row.status === 'Enabled'" status="success" />
              <el-badge v-else status="info" />
              <span :data-testid="`processing-custom-2001-t10-${$index}`" class="ml-8">{{
                row.statusText || '-'
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="290">
          <template #default="{ row, $index }">
            <!--
                启用状态 status
                  Disabled 已禁用
                  Enabled 已启用
                  NotEnabled 未启用

                测试状态 fullyValidateStatus
                  测试失败,2
                  测试成功,1
                  测试中,0
                  未测试,-1

                校验状态 singleValidateStatus
                校验失败,2
                校验成功,1
                校验中,0
                未校验,-1
              -->
            <!-- v-if="['Enabled', 'Disabled'].includes(record.status)" -->

            <el-button
              v-auth="`dataCenter-customized-edit`"
              v-if="['Enabled'].includes(row.status)"
              :underline="false"
              type="primary"
              link
              :data-testid="`processing-2002-${$index}`"
              @click="handleDetail(row)"
              >查看
            </el-button>

            <el-button
              v-auth="`dataCenter-customized-edit`"
              v-if="['Disabled', 'NotEnabled'].includes(row.status)"
              :underline="false"
              type="primary"
              link
              :data-testid="`processing-2003-${$index}`"
              @click="handleEdit(row)"
              >编辑
            </el-button>

            <el-button
              v-auth="`dataCenter-customized-enable`"
              v-if="['Disabled', 'NotEnabled'].includes(row.status)"
              :underline="false"
              type="primary"
              link
              :data-testid="`processing-2004-${$index}`"
              @click="handleEnable(row)"
              >启用
            </el-button>

            <el-button
              v-auth="`dataCenter-customized-enable`"
              v-if="['Enabled'].includes(row.status)"
              :underline="false"
              type="primary"
              link
              :data-testid="`processing-2005-${$index}`"
              @click="handleDisabled(row)"
              >禁用
            </el-button>

            <el-button
              v-auth="`dataCenter-customized-check`"
              :underline="false"
              type="primary"
              link
              :data-testid="`processing-2006-${$index}`"
              @click="handleCheckAndTestVisible(row, 1)"
              >校验
            </el-button>

            <el-button
              v-auth="`dataCenter-customized-test`"
              :underline="false"
              type="primary"
              link
              :data-testid="`processing-2007-${$index}`"
              @click="handleCheckAndTestVisible(row, 2)"
              >测试
            </el-button>

            <el-button
              v-auth="`dataCenter-customized-copy`"
              :underline="false"
              type="primary"
              link
              @click="handleCopy(row)"
              :data-testid="`processing-2008-${$index}`"
              >复制
            </el-button>

            <el-button
              v-if="['Disabled', 'NotEnabled'].includes(row.status)"
              v-auth="`dataCenter-customized-delete`"
              :underline="false"
              type="danger"
              link
              @click="handleDel(row)"
              :data-testid="`processing-btn-del-2009-${$index}`"
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
        style="margin-top: 16px; justify-content: flex-end"
      />
    </FtCard>

    <!--新增/编辑-->
    <CustomForm
      :filterClient="filterClient"
      :clientId="userStore.clientId"
      @refreshList="handleQuery"
    />
    <!--结果-->
    <DrawerContainer
      v-model="resultObj.visible"
      :data-testid="`processing-result-drawer`"
      title-str="查看结果"
    >
      <!--校验结果 resultObj.record -->
      <!--流程测试—“处理阶段”processPhaseText-“规则类型”regulationTypeText-“规则名称”name-结果生成时间-->
      <CheckResult v-if="resultObj.type === 1" :record="resultObj.record"></CheckResult>
      <!--测试结果-->
      <TestResult v-if="resultObj.type === 2" :record="resultObj.record"></TestResult>
    </DrawerContainer>

    <!--详情-->
    <RuleDetail
      v-model="detailObj.visible"
      :id="detailObj.id"
      :clientId="detailObj.clientId"
    ></RuleDetail>

    <!--规则校验、流程测试 :channelList="channelOptions"-->
    <CheckAndTest
      v-model="checkAndTestObj.visible"
      :type="checkAndTestObj.type"
      :id="checkAndTestObj.id"
      :clientId="checkAndTestObj.clientId"
      @refreshList="handleQuery"
    ></CheckAndTest>
  </div>
</template>

<script lang="ts" setup>
import { useTable } from '@/hooks/table'
import type { Options } from '@/hooks/table.d'
import CustomForm from './components/CustomForm.vue'

import { ElMessage, ElMessageBox } from 'element-plus'
import DrawerContainer from '@/views/dataCenter/dataProcessing/components/DrawerContainer.vue'
import TestResult from '@/views/dataCenter/dataProcessing/components/TestResult.vue'
import CheckResult from '@/views/dataCenter/dataProcessing/components/CheckResult.vue'
import RuleDetail from '@/views/dataCenter/dataProcessing/components/RuleDetail.vue'
import CheckAndTest from '@/views/dataCenter/dataProcessing/components/CheckAndTest.vue'
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import {
  copyRegulationInfo,
  deleteRegulationInfo,
  disabledOrEnableRegulationInfo
} from '@/api/dataProcessing'
import { debounce } from 'lodash-es'
import ChannelCascader from '@/components/ChannelCascader.vue'
import FtCard from '@/components/FtCard.vue'
import { Plus } from '@element-plus/icons-vue'
import useUserStore from '@/stores/modules/user'
import useComputedCascaderWidth from '@/hooks/useComputedCascaderWidth'
import { computedCardHeight } from '@/utils'
import { useAppStore } from '@/stores'

// 类型定义
interface TableData {
  [key: string]: any
}

interface TableSortable {
  sorter?: boolean
  sortDirections?: string[]
}

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const { refDom: channelRef, formatLabelHandle } = useComputedCascaderWidth()
const resultVisible = ref(false)
const detailObj = reactive({
  visible: false,
  id: '',
  clientId: ''
})
const resultObj = reactive<{
  visible: boolean
  type: 1 | 2
  id: string
  record: Record<string, any>
}>({
  visible: false,
  type: 1,
  id: '',
  record: {}
})
const checkAndTestObj = reactive<{
  visible: boolean
  type: 1 | 2
  id: string
  clientId: string
}>({
  visible: false,
  type: 1,
  id: '',
  clientId: ''
})

const userStore = useUserStore()

const option = {
  url: '/insights/regulation/findRegulationInfoList',
  method: 'POST',
  notResetKey: ['clientId']
}
const {
  table,
  form,
  getTableData,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  handleSortChange,
  getFirstPageTableData
} = useTable(option as Options)

// watch(() => userStore.clientId, (val: string) => {
//   table.filter.clientId = val;
// }, {
//   immediate: true,
//   deep: true
// });

/**
 * 客户列表
 * 过滤去掉系统
 */
const filterClient = computed(() => {
  return conditions.client?.filter(item => item.key !== '0')
  // return conditions.client;
})

const timer = ref()
onMounted(async () => {
  table.filter.regulationClassify = 'custom'
  handleQuery()
  timer.value = setInterval(() => {
    table.filter.clientId = userStore.clientId
    getTableData()
  }, 10000)
})

onBeforeUnmount(() => {
  timer.value && clearInterval(timer.value)
})

/**
 * 查询
 */
const handleQuery = () => {
  table.filter.clientId = userStore.clientId
  getFirstPageTableData()
}

const reset = () => {
  handleReset(() => {
    table.filter.regulationClassify = 'custom'
  })
}

/**
 * 查看校验与测试结果
 * @param record
 * @param type
 */
const handleResult = (record: any, type: 1 | 2) => {
  resultVisible.value = true
  resultObj.visible = true
  resultObj.type = type
  resultObj.id = record.id
  resultObj.record = record
}

/**
 * 查看详情
 * @param record
 */
const handleDetail = async (record: any) => {
  detailObj.visible = true
  detailObj.id = record.id
  detailObj.clientId = record.clientId
}

/**
 * 校验与测试
 * @param record
 * @param type
 */
const handleCheckAndTestVisible = (record: any, type: 1 | 2) => {
  checkAndTestObj.visible = true
  checkAndTestObj.id = record.id
  checkAndTestObj.type = type
  checkAndTestObj.clientId = record.clientId
}

/**
 * 复制
 * @param record
 */
const handleCopy = debounce((record: any) => {
  if (record.id) {
    copyRegulationInfo({ id: record.id, clientId: userStore.clientId })
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('复制成功')
          handleQuery()
        }
      })
      .catch((err: any) => {
        ElMessage.error(err?.message)
      })
  }
}, 300)

/**
 * 删除
 * @param record
 */
const handleDel = debounce((record: any) => {
  if (record.id) {
    deleteRegulationInfo({ id: record.id, clientId: userStore.clientId })
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('删除成功')
          handleQuery()
        }
      })
      .catch((err: any) => {
        ElMessage.error(err?.message)
      })
  }
}, 300)

/**
 * 禁用 启用
 * @param id
 * @param status
 */
const setStatus = (id: string, status: 'Disabled' | 'Enabled') => {
  if (!id) return
  disabledOrEnableRegulationInfo({
    id,
    status,
    clientId: userStore.clientId
  }).then(res => {
    if (res.code === '200') {
      handleQuery()
    }
  })
}

const sortOpts: TableSortable = {
  sortDirections: ['ascend', 'descend'],
  sorter: true
}

// 禁用
const handleDisabled = (record: TableData) => {
  console.log('record', record)
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
        console.log('启用')
        setStatus(record.id, 'Enabled')
      }, 300)()
    })
    .catch(() => {
      console.log('取消启用')
    })
}

watch(
  () => userStore.clientId,
  () => {
    table.filter.channel = undefined
    handleQuery()
  }
)

provide('form', form)

// defineExpose({handleQuery});
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
