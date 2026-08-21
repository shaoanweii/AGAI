<template>
  <div class="subscribe-management">
    <el-tabs v-model="activeName" class="demo-tabs" @tab-change="handleTabClick">
      <el-tab-pane
        v-for="item in showTabList"
        :key="item.value"
        :label="item.label"
        :name="item.value"
      >
      </el-tab-pane>
    </el-tabs>
    <!-- 表格 -->
    <div class="list-view">
      <el-card class="table-card" shadow="never">
        <div class="flex-between items-center mb-24">
          <div class="text-h3" style="font-weight: 600">
            {{ tabList.find(item => item.value === activeName)?.label }}
          </div>
          <div class="flex gap-16">
            <el-select-v2
              v-if="
                [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
              "
              v-model="formData.statusList"
              :options="statusListOptions"
              :props="{ value: 'value', label: 'label' }"
              placeholder="全部状态"
              filterable
              clearable
              multiple
              collapse-tags
              :max-collapse-tags="1"
              style="width: 200px"
              @change="handleSearch"
            />

            <el-select-v2
              v-if="
                [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                  activeName
                )
              "
              v-model="formData.pushResultsList"
              :options="resultListOptions"
              :props="{ value: 'value', label: 'label' }"
              placeholder="全部结果"
              filterable
              clearable
              multiple
              collapse-tags
              :max-collapse-tags="1"
              style="width: 200px"
              @change="handleSearch"
            />

            <el-select-v2
              v-if="[SubscribeTabType.SubscribeAll].includes(activeName)"
              v-model="formData.creatorList"
              :options="userOptions"
              :props="{ value: 'userId', label: 'userName' }"
              placeholder="全部创建人"
              filterable
              clearable
              multiple
              collapse-tags
              :max-collapse-tags="1"
              style="width: 200px"
              @change="handleSearch"
            />

            <!-- 全部订阅+我创建的订阅 -->
            <el-input
              v-if="
                [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
              "
              v-model="formData.taskName"
              placeholder="搜索报告名称"
              clearable
              style="width: 172px"
              :suffix-icon="Search"
              @change="handleSearch"
            />

            <!-- 全部推送记录+推送给我的记录 -->
            <el-input
              v-if="
                [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                  activeName
                )
              "
              v-model="formData.reportName"
              placeholder="搜索报告名称"
              clearable
              style="width: 172px"
              :suffix-icon="Search"
              @change="handleSearch"
            />

            <el-button
              v-if="
                [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeRecord].includes(
                  activeName
                )
              "
              type="primary"
              @click="handleExport(activeName)"
              :disabled="exportTodo"
              >{{ exportTodo ? '导出中...' : '导出' }}</el-button
            >
          </div>
        </div>
        <el-table
          v-loading="loading"
          :data="dataList as any"
          max-height="calc(100vh- 56px - 56px - 54px)"
          class="flex-auto overflow-auto"
        >
          <el-table-column type="index" label="#" width="56" align="center" />
          <el-table-column prop="reportName" label="报告名称" width="260">
            <template #default="{ row }">
              <div class="">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="row.reportName"
                  placement="top"
                >
                  <span>{{ row.reportName }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sourceModule" label="来源模块" width="180" />
          <el-table-column prop="subscriptionPeriod" label="订阅周期" width="180" />
          <el-table-column prop="sendRuleDesc" label="发送规则" width="180" />
          <!-- 全部订阅+我创建的订阅 -->
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
            "
            prop="receiverNameOnly"
            label="接收人"
            width="180"
          >
            <template #default="{ row }">
              <div class="">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="row.receiverDesc"
                  placement="top"
                >
                  <span>{{ row.receiverNameOnly }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <!-- 全部推送记录+推送给我的记录 -->
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="receiverName"
            label="接收人"
            width="180"
          />

          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="receiverId"
            label="员工工号"
            width="180"
          />
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="deptLevel2"
            label="二级部门"
            width="180"
          />
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="deptLevel3"
            label="三级部门"
            width="180"
          />

          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
            "
            prop="receiveChannelDesc"
            label="接收渠道"
            width="180"
          />
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="receiveChannelDesc"
            label="接收渠道"
            width="180"
          >
            <template #default="{ row }">
              <div class="flex-y-center">
                <div class="status-icon mr-8"></div>
                {{ createChannelText(row) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
            "
            prop="pushCount"
            label="已推送次数"
            width="100"
          />
          <el-table-column prop="creatorName" label="创建人" width="120">
            <template #default="{ row }">
              <div class="">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="row.creatorWithDept"
                  placement="top"
                >
                  <span>{{ row.creatorName }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />

          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="pushTime"
            label="推送时间"
            width="180"
          />
          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeRecord, SubscribeTabType.SubscribeReceived].includes(
                activeName
              )
            "
            prop="pushResult"
            label="推送结果"
            width="180"
          >
            <template #default="{ row }">
              <div class="flex-y-center">
                <div
                  class="status-icon mr-8"
                  :style="{ backgroundColor: row.pushResult === 1 ? '#00B42A' : '#FF4D4F' }"
                ></div>
                {{ row.pushResult === 1 ? '成功' : '失败' }}
              </div>
            </template>
          </el-table-column>

          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
            "
            label="状态"
            width="100"
          >
            <template #default="{ row }">
              <div class="flex-y-center">
                <div class="status-icon mr-8" :style="getStatusTagType(row)"></div>
                {{ getStatusText(row.status) }}
              </div>
            </template>
          </el-table-column>

          <el-table-column
            v-if="
              [SubscribeTabType.SubscribeAll, SubscribeTabType.SubscribeMy].includes(activeName)
            "
            label="操作"
            width="180"
            fixed="right"
          >
            <template #default="{ row }">
              <!-- 0-未开始, 1-运行中 -->
              <el-button
                link
                type="primary"
                v-if="[0, 1].includes(row.status)"
                @click="changeStatus(row, 2)"
                :disabled="actionLoading"
                >停止</el-button
              >
              <!-- 2-已停止 -->
              <el-button
                link
                type="primary"
                v-if="[2].includes(row.status)"
                @click="changeStatus(row, 1)"
                :disabled="actionLoading"
                >恢复</el-button
              >
              <el-button
                link
                type="primary"
                @click="handleSubscribeReport(row)"
                :disabled="actionLoading"
              >
                编辑
              </el-button>
              <el-button
                link
                type="primary"
                @click="handleDeleteSubscribe(row)"
                :disabled="actionLoading"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="->,total, prev, pager, next, sizes"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
    <!-- 订阅弹窗 -->
    <SubscribeReport
      v-model="publicSubscribeVisible"
      :edit-item="editSubscribeItem"
      @updateCallback="updateCallback"
      @closeCallback="closeCallback"
    ></SubscribeReport>
  </div>
</template>

<script setup lang="ts">
import { h, ref, reactive, onMounted, watch, onUnmounted } from 'vue'

import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { hasPermission } from '@/utils/permission'

import { useUserStore } from '@/store'
import { SubscribeTabType } from '@/constants'
import { downloadFromBlob } from '@/utils/download'
import {
  exportSubscribeReport,
  exportPushRecord,
  findSubscribeTaskList,
  findPushTaskList,
  updateSubscribeTaskStatus,
  getSubscribeTaskUserList,
  deleteSubscribeTask
} from '@/api/subscribeReport'
import { userInfo } from '@/api/main'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import SubscribeReport from '@/components/Business/Scene/Common/SubscribeReport/index.vue'

defineOptions({
  name: 'SubscribeManagement'
})

const tabList = reactive([
  { label: '全部订阅', value: SubscribeTabType.SubscribeAll },
  { label: '全部推送记录', value: SubscribeTabType.SubscribeRecord },
  { label: '我创建的订阅', value: SubscribeTabType.SubscribeMy },
  { label: '推送给我的记录', value: SubscribeTabType.SubscribeReceived }
])

const userStore = useUserStore()
const activeName = ref() // 当前激活的标签页的 name
const showTabList: any = ref([]) // 当前列表的标签
const userDatainfo = ref<any>({})

const publicSubscribeVisible = ref<boolean>(false)
const editSubscribeItem = ref<any>(null)

const actionLoading = ref<boolean>(false) // 操作栏正在处理
const exportTodo = ref<boolean>(false) // 导出正在处理

// 默认值
const defalutValue = {
  creatorList: undefined, // 订阅的多人选择筛选
  creatorId: undefined,
  taskName: undefined, // 订阅的查询参数
  reportName: undefined, // 推送的的查询参数
  statusList: undefined,
  pushResultsList: undefined
}

// 表单查询参数
const formData: any = ref({ ...defalutValue })

const dataList = ref<any[]>([]) // 列表
const total = ref(0) // 总条数
const currentPage = ref(1) // 当前页
const pageSize = ref(10) // 每页条数
const loading = ref(false)

/**
 * @description: 订阅按钮事件
 * @return {*}
 */
const handleSubscribeReport = (item: any) => {
  editSubscribeItem.value = item
  publicSubscribeVisible.value = true
}

onMounted(() => {
  userInfo()
    .then((res: any) => {
      if (res.code === 200) {
        userDatainfo.value = res.result
      }
    })
    .finally(() => {
      // 处理初始化数据逻辑 根据权限来处理显示的tab又哪些

      // subscription_selectOneself    可见自己        subscription_selectAll   可见全部
      const hasAll = hasPermission('subscription_selectAll')
      if (hasAll) {
        showTabList.value = tabList
        activeName.value = tabList[0].value
      } else {
        const onldList = [tabList[2], tabList[3]]
        showTabList.value = onldList
        activeName.value = onldList[0].value
        // 处理查询参数
        formData.value = { ...defalutValue, creatorId: userDatainfo.value?.employeeId }
      }

      // 初始化数据
      getUserList()
      // 初始化数据
      fetchDataApi()
    })
})

const handleTabClick = (tab: any) => {
  activeName.value = tab
  let creatorId = undefined
  if (tab === SubscribeTabType.SubscribeMy) {
    // 我创建的订阅
    creatorId = userDatainfo.value?.employeeId
  }

  let receiverId = undefined
  if (tab === SubscribeTabType.SubscribeReceived) {
    // 推送给我的记录
    receiverId = userDatainfo.value?.employeeId
  }
  formData.value = { ...defalutValue, creatorId, receiverId } // 切换标签时重置查询条件
  dataList.value = [] // 切换标签时清空列表数据
  total.value = 0
  currentPage.value = 1 // 切换标签时重置页码
  pageSize.value = 10 // 切换标签时重置每页条数
  fetchDataApi() // 切换标签时重新获取数据
}

const statusListOptions = [
  {
    label: '未开始',
    value: 0,
    color: '#C9CDD4'
  },
  {
    label: '进行中',
    value: 1,
    color: '#00B42A'
  },
  {
    label: '已结束',
    value: 3,
    color: '#FF4D4F'
  },
  {
    label: '已停止',
    value: 2,
    color: '#FF4D4F'
  }
]

const resultListOptions = [
  {
    label: '成功',
    value: 1
  },
  {
    label: '失败',
    value: 0
  }
]

//获取用户列表下拉（用于 el-select-v2）
const userOptions = ref<Array<{ userId: string; userName: string }>>([])
const getUserList = async () => {
  try {
    const response = await getSubscribeTaskUserList()
    if (response.success) {
      let users: any[] = response.result || []

      // 转换为 el-select-v2 需要的格式
      userOptions.value = users
        .map(user => {
          const userId = user.creatorId
          const userName = user.creatorName || ''
          return { userId, userName }
        })
        .filter(item => item.userId && item.userName) // 过滤掉无效数据
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userOptions.value = []
  }
}

const fetchDataApi = async () => {
  loading.value = true
  // 合并查询参数：formData 和分页参数
  const params = {
    ...formData.value,
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  try {
    // 根据activeName的不同调用不同的接口

    let response: any
    if (
      activeName.value === SubscribeTabType.SubscribeAll ||
      activeName.value === SubscribeTabType.SubscribeMy
    ) {
      response = await findSubscribeTaskList(params)
    } else if (
      activeName.value === SubscribeTabType.SubscribeRecord ||
      activeName.value === SubscribeTabType.SubscribeReceived
    ) {
      response = await findPushTaskList(params)
    }
    if (response) {
      if (response.success) {
        // 处理 IPageReportDownLoadFileVo 数据结构
        const result = response.result
        if (result) {
          const list = result.list
          if (list && Array.isArray(list)) {
            dataList.value = list
            total.value = result.total || 0
          }
        }
      }
    }
  } catch (error) {
    console.error('获取列表失败:', error)
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 分页大小改变
 */
const handleSizeChange = async (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  await fetchDataApi()
}

/**
 * 当前页改变
 */
const handleCurrentChange = async (page: number) => {
  currentPage.value = page
  await fetchDataApi()
}

/**
 * 搜索处理
 */
const handleSearch = async () => {
  // 重置到第一页并刷新数据
  currentPage.value = 1
  fetchDataApi()
}

// 辅助方法
/**
 * 获取状态标签类型
 */
const getStatusTagType = (row?: any) => {
  let color = statusListOptions[0].color // 默认未开始的颜色
  const xxx = statusListOptions.find((item: any) => `${item.value}` === `${row?.status}`)
  if (xxx) {
    color = xxx.color
  }
  return { backgroundColor: color }
}

/**
 * 获取状态文本
 */
// 状态: 0-未开始, 1-进行中, 2-已停止
const getStatusText = (status?: any) => {
  const findItem = statusListOptions.find((item: any) => `${item.value}` === `${status}`)
  if (findItem) {
    return findItem.label
  }
  return ''
}

const handleExport = async (exportsName: string) => {
  const params = {
    ...formData.value,
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }
  let response = null
  try {
    let filename = `download_${Date.now()}.xlsx`
    if (exportsName === SubscribeTabType.SubscribeAll) {
      exportTodo.value = true
      response = await exportSubscribeReport(params)
      filename = '全部订阅.xlsx'
    } else if (exportsName === SubscribeTabType.SubscribeRecord) {
      exportTodo.value = true
      response = await exportPushRecord(params)
      filename = '全部推送记录.xlsx'
    }
    if (response) {
      downloadFromBlob(response.result, filename)
      ElMessage.success('文件下载成功')
    }
  } catch (error) {
    //
  } finally {
    exportTodo.value = false
  }
}

// 创建渠道
const createChannelText = (row: any) => {
  const receiveChannelDesc = row.receiveChannelDesc
  const text: string[] = []
  if (receiveChannelDesc) {
    // 将字符串转成json
    const channels = JSON.parse(receiveChannelDesc)
    channels.forEach((channel: any) => {
      if (`${channel}` === `1`) {
        text.push('站内通知')
      } else if (`${channel}` === '2') {
        text.push('邮件')
      }
    })
  }
  return text.join('、')
}

// 0-未开始, 1-运行中, 2-已停止
const changeStatus = (row: any, status: number) => {
  actionLoading.value = true
  updateSubscribeTaskStatus(row.id, status)
    .then(() => {
      fetchDataApi()
    })
    .finally(() => {
      actionLoading.value = false
    })
}

const updateCallback = (data: any) => {
  // 订阅新增或者编辑成功后的回调函数
  fetchDataApi() // 刷新列表数据
}
const closeCallback = (data: any) => {
  editSubscribeItem.value = null
}

const handleDeleteSubscribe = async (row: any) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h(
            'span',
            { class: 'ml-8' },
            `删除「${row.reportName}」后任务不可恢复，已推送记录将保留。`
          )
        ]),
      '删除订阅任务',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    actionLoading.value = true
    //
    deleteSubscribeTask(row.id)
      .then(() => {
        fetchDataApi() // 刷新列表数据
      })
      .finally(() => {
        actionLoading.value = false
      })
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

onUnmounted(() => {})
</script>

<style lang="scss" scoped>
.subscribe-management {
  height: 100%;
  display: flex;
  flex-direction: column;
  // .demo-tabs > .el-tabs__content {
  //   padding: 32px;
  //   color: #6b778c;
  //   font-size: 32px;
  //   font-weight: 600;
  // }
  .list-view {
    width: 100%;
    height: calc(100% - 54px);
    .table-card {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;

      :deep(.el-table .el-table__cell) {
        height: 55px;
        padding: 0 !important;
      }

      :deep(.el-table--fit .el-table__inner-wrapper:before) {
        width: 0 !important;
      }

      :deep(.el-table__header) {
        .el-table__cell {
          color: #1d2129;
          font-weight: 600;
        }
      }

      :deep(.el-card__body) {
        // flex: 1;
        width: 100%;
        height: calc(100vh - 186px); // 56px - 56px - 54px - 20px
        display: flex;
        flex-direction: column;
      }

      :deep(.el-table__body-wrapper) {
        .el-table__cell {
          color: #1d2129;
          font-weight: 400;
        }
      }

      .status-icon {
        width: 6px;
        height: 6px;
        border-radius: 50%;
      }

      .pagination-wrapper {
        margin-top: 16px;
      }
    }
  }
}
// 响应式设计
@media (max-width: 768px) {
  .user-management {
    .table-card {
      .pagination-wrapper {
        text-align: center;
      }
    }
  }
}
</style>
