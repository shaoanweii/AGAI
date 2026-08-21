<script setup lang="ts">
import { h, ref, onMounted, computed, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { useGeneralScenarioStore } from '@/store'
import { useQueryStore } from '@/store/modules/query'
import SCHeader from '@/components/Business/Scene/Common/SCHeader/index.vue'
import { Search } from '@element-plus/icons-vue'

import useMiddlewareStore from '@/store/modules/middleware'

import { useQueryListener } from '@/hooks/useQueryListener'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { DrillTabKey } from '@/components/Business/DrillDownDialog/constants.ts'

// import { getSeriesCondition } from '@/api/newCarLaunch'
import { getDateRange } from '@/utils/date'
import { formatDate } from '@/utils'
import { useRoute, useRouter } from 'vue-router'

import { getSubscribeTaskUserList } from '@/api/subscribeReport'
import { getHotListData, getHotUserCreaterData, deleteHotData } from '@/api/hotAphttp'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import HotDialogEvents from '@/components/Business/Scene/Common/HotDialogEvents/index.vue'

defineOptions({
  name: 'HotEvents'
})

const generalScenarioStore = useGeneralScenarioStore()
const route = useRoute()

const middlewareStore = useMiddlewareStore()

// 初始化 ddStore
const ddStore = useGeneralDrillDownStore()

const queryStore = useQueryStore()

const router = useRouter()

// 默认值
const defalutValue = {
  status: undefined, // 状态筛选
  createBy: undefined, // 全部创建人选择筛选
  eventsName: undefined // 事件名称搜索
}

// 1-未开始，2-进行中，3-已结束
const statusListOptions = [
  {
    label: '未开始',
    value: 1,
    color: '#C9CDD4'
  },
  {
    label: '进行中',
    value: 2,
    color: '#00B42A'
  },
  {
    label: '已结束',
    value: 3,
    color: '#FF4D4F'
  }
]

// 表单查询参数
const formData: any = ref({ ...defalutValue })

const dataList = ref<any[]>([]) // 列表
const total = ref(0) // 总条数
const currentPage = ref(1) // 当前页
const pageSize = ref(10) // 每页条数
const loading = ref(false)

//获取用户列表下拉（用于 el-select-v2）
const userOptions = ref<Array<{ userId: string; userName: string }>>([])

const eventsDialogVisible = ref<boolean>(false)
const editHotEventsItem = ref<any>(null)

// ==================== 数据状态 ====================

// ==================== 接口调用方法 ===================

/**
 * 搜索处理
 */
const handleSearch = async () => {
  // 重置到第一页并刷新数据
  currentPage.value = 1
  fetchDataApi()
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
    let response = await getHotListData(params)
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
  } finally {
    loading.value = false
  }
}

const getUserList = async () => {
  try {
    const response = await getHotUserCreaterData()
    if (response.success) {
      let users: any[] = response.result || []

      // 转换为 el-select-v2 需要的格式
      userOptions.value = users
        .map(user => {
          const userId = user.employeeId
          const userName = user.userName || ''
          return { userId, userName }
        })
        .filter(item => item.userId && item.userName) // 过滤掉无效数据
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userOptions.value = []
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

const clickCreatBtn = () => {
  eventsDialogVisible.value = true
}

const updateCallback = (data: any) => {
  // 新增或者编辑成功后的回调函数
  fetchDataApi() // 刷新列表数据
}

const closeCallback = () => {
  editHotEventsItem.value = null
  eventsDialogVisible.value = false
}

const clickView = (item: any) => {
  // 跳转页面到详情页面
  router.push({
    path: '/hotView/hotDetailEvents',

    query: {
      // ...item,
      id: item.id,
      from: '/scene/hotEvents',
      isBack: '1'
    }
  })
}

const editData = (item: any) => {
  editHotEventsItem.value = item
  eventsDialogVisible.value = true
}

const deleteByid = async (item: any) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, `删除「${item.eventName}」`)
        ]),
      '删除任务',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }

  deleteHotData(item.id).then((res: any) => {
    if (res.success) {
      ElMessage.success('删除成功')
      fetchDataApi()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  })
}

const coverText = (str: any, fillStr = '') => {
  if (str) {
    try {
      const xx = JSON.parse(str)
      const text = xx.join('、')
      return text
    } catch (error) {
      return str
    }
  }
  return fillStr
}

const longStr = 10
const hanhLongText = (str: any, long: number = longStr) => {
  if (!str) {
    return ''
  }
  // 截取字符串
  return str?.length > long ? str?.substring?.(0, long) + '...' : str
}

// ==================== 生命周期 ====================

onBeforeMount(() => {})

// 组件挂载时加载数据
onMounted(() => {
  getUserList()
  fetchDataApi()
})
</script>

<template>
  <FAnalyseWrap v-model="generalScenarioStore.visible">
    <template #header>
      <SCHeader title="热点事件" subtitle="智行汽车集团"></SCHeader>
    </template>
    <!-- 表格 -->
    <div class="list-view">
      <el-card class="table-card" shadow="never">
        <div class="flex-between items-center mb-24">
          <div class="text-h3" style="font-weight: 600">事件列表</div>
          <div class="flex gap-16">
            <el-select-v2
              v-model="formData.status"
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
              v-model="formData.createBy"
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

            <el-input
              v-model="formData.eventsName"
              placeholder="请输入事件名称或关键词搜索"
              clearable
              style="width: 240px"
              :suffix-icon="Search"
              @change="handleSearch"
            />

            <el-button type="primary" @click="clickCreatBtn">创建事件</el-button>
          </div>
        </div>
        <el-table
          v-loading="loading"
          :data="dataList"
          max-height="calc(100vh- 56px - 56px - 24px)"
          class="flex-auto overflow-auto"
          :empty-text="'暂无列表数据'"
        >
          <el-table-column type="index" label="#" width="56" align="center" />
          <el-table-column prop="eventName" label="事件名称" width="260">
            <template #default="{ row }">
              <div class="flex-y-center">
                <el-tooltip class="box-item" effect="dark" :content="row.eventName" placement="top">
                  <span>{{ hanhLongText(row.eventName) }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="keywords" label="关键词" width="180">
            <template #default="{ row }">
              <div class="flex-y-center">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="coverText(row.keywords)"
                  placement="top"
                >
                  <span>{{ hanhLongText(coverText(row.keywords)) }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="brandList" label="品牌" width="180">
            <template #default="{ row }">
              <div class="flex-y-center">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="coverText(row.brandList, '不限')"
                  placement="top"
                >
                  <span>{{ hanhLongText(coverText(row.brandList, '不限')) }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="seriesList" label="车系" width="180">
            <template #default="{ row }">
              <div class="flex-y-center">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="coverText(row.seriesList, '不限')"
                  placement="top"
                >
                  <span>{{ hanhLongText(coverText(row.seriesList, '不限')) }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="180">
            <template #default="{ row }">
              <div class="flex-y-center">
                {{ statusListOptions.find(e => e.value === row?.status)?.label }}
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="createBy" label="创建人" width="140" />
          <el-table-column prop="createTime" label="创建时间" width="180" />

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="clickView(row)"> 查看 </el-button>
              <el-button link type="primary" @click="editData(row)"> 编辑 </el-button>
              <el-button link type="primary" @click="deleteByid(row)"> 删除 </el-button>
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
    <HotDialogEvents
      v-model="eventsDialogVisible"
      :edit-item="editHotEventsItem"
      @updateCallback="updateCallback"
      @closeCallback="closeCallback"
    ></HotDialogEvents>
  </FAnalyseWrap>
</template>

<style lang="scss" scoped>
.list-view {
  width: 100%;
  height: calc(100% - 24px);
  margin-top: 24px;
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
      height: calc(100vh - 156px); // 56px - 56px - 54px - 20px
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
</style>
