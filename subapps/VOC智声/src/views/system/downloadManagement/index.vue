<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import {
  findReportDownLoadFileList,
  downloadAgain,
  findVisibleUserList
} from '@/api/downloadManagement'
import type { ReportDownLoadFileVo } from '@/api/downloadManagement/index.d'
import { findAccountInfoList } from '@/api/user'
import type { AccountInfo } from '@/api/user/types'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useTable } from '@/hooks/useTable'
import request from '@/api/http'
import { downloadFromBlob, getFileNameWithExtension } from '@/utils/download'
import { hasPermission } from '@/utils/permission'
import { DOWNLOAD_MANAGEMENT_BTN_MAP } from '@/constants/btnPermMap'
import { downloadFile } from '@/api/common'

defineOptions({
  name: 'downloadManagement'
})

/**
 * @description: 获取是否全部可见
 * @return {boolean}
 */
const getIsAllVisible = () => {
  if (hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ALL)) {
    return true
  } else if (hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ONESELF)) {
    return false
  } else {
    return false
  }
}

//获取用户列表下拉（用于 el-select-v2）
const userOptions = ref<Array<{ userId: string; userName: string }>>([])
const getUserList = async () => {
  try {
    const response = await findVisibleUserList(getIsAllVisible())
    if (response.success) {
      const result = response.result
      let users: AccountInfo[] = []
      if (result) {
        if (result.list && Array.isArray(result.list)) {
          users = result.list
        } else if (Array.isArray(result)) {
          users = result
        } else if (result.records && Array.isArray(result.records)) {
          users = result.records
        }
      }
      // 转换为 el-select-v2 需要的格式
      userOptions.value = users
        .map(user => {
          const userId = (user as any).id || user.userId || ''
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

// 使用 useTable hook，直接在返回值中解构 tableState
const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable({
  immediate: false, // 手动控制初始化时机
  initialFormData: {
    userIds: [] as string[],
    fileName: ''
  },
  fetchDataApi: async () => {
    // 合并查询参数：formData 和分页参数
    const params = {
      ...formData.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      isAllVisible: getIsAllVisible()
    }

    const response = await findReportDownLoadFileList(params)
    if (response.success) {
      // 处理 IPageReportDownLoadFileVo 数据结构
      const result = response.result
      if (result) {
        // IPageReportDownLoadFileVo 包含 records 和 total 字段
        if (result.records && Array.isArray(result.records)) {
          return {
            list: result.records,
            total: result.total || 0
          }
        }
        // 默认处理
        else {
          return {
            list: [],
            total: 0
          }
        }
      } else {
        return {
          list: [],
          total: 0
        }
      }
    } else {
      ElMessage.error(response.message || '获取下载列表失败')
      return {
        list: [],
        total: 0
      }
    }
  }
})

/**
 * 搜索处理
 */
const handleSearch = async () => {
  // 重置到第一页并刷新数据
  currentPage.value = 1
  await tableMethods.handleQuery()
}

/**
 * 下载处理
 */
const handleDownload = async (row: ReportDownLoadFileVo) => {
  try {
    if (!row.filePath) {
      ElMessage.warning('文件路径不存在')
      return
    }

    const response = await downloadFile({ id: row.id })
    const filename = getFileNameWithExtension(row.fileName, row.filePath)
    downloadFromBlob(response.result, filename)
    ElMessage.success('文件下载成功')
    return

    // 判断 filePath 是否为完整 URL
    // const isFullUrl = row.filePath.startsWith('http://') || row.filePath.startsWith('https://')

    // if (isFullUrl) {
    //   // 如果是完整 URL（包括带签名的URL），直接使用 GET 请求下载
    //   const response = await fetch(row.filePath)
    //   if (!response.ok) {
    //     ElMessage.error(`文件下载失败: HTTP ${response.status}`)
    //     return
    //   }
    //   const blob = await response.blob()
    //   const filename = getFileNameWithExtension(row.fileName, row.filePath)
    //   downloadFromBlob(blob, filename)
    //   ElMessage.success('文件下载成功')
    // } else {
    //   // 如果是相对路径，直接构建完整URL，避免走 /api 代理
    //   // 例如：files/账号列表-20260121115457.xlsx -> http://localhost:5173/files/账号列表-20260121115457.xlsx
    //   const fullUrl = `${window.location.origin}/${row.filePath}`
    //   const response = await fetch(fullUrl)
    //   if (!response.ok) {
    //     ElMessage.error(`文件下载失败: HTTP ${response.status}`)
    //     return
    //   }
    //   const blob = await response.blob()
    //   const filename = getFileNameWithExtension(row.fileName, row.filePath)
    //   downloadFromBlob(blob, filename)
    //   ElMessage.success('文件下载成功')
    // }
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '文件下载失败')
  }
}

/**
 * 重新下载处理
 */
const handleRedownload = async (row: ReportDownLoadFileVo) => {
  try {
    if (!row.id) {
      ElMessage.warning('文件ID不存在')
      return
    }

    // 调用 downloadAgain 接口重新下载
    const response = await downloadAgain({ id: row.id })

    if (response.success) {
      // 如果返回的是 Blob，直接下载
      if (response.result instanceof Blob) {
        const filename = getFileNameWithExtension(row.fileName, row.filePath)
        downloadFromBlob(response.result, filename)
        ElMessage.success('文件重新下载成功')
        // 刷新列表
        await tableMethods.refresh()
      } else {
        // 如果返回的不是 Blob，可能是成功消息，刷新列表
        ElMessage.success(response.message || '重新下载请求已提交')
        await tableMethods.refresh()
      }
    } else {
      ElMessage.error(response.message || '重新下载失败')
    }
  } catch (error) {
    console.error('重新下载文件失败:', error)
    ElMessage.error('重新下载失败，请稍后重试')
  }
}

// 辅助方法
/**
 * 获取状态标签类型
 */
const getStatusTagType = (status?: string) => {
  let color = ''
  // 空：正在下载，0：下载失败,1:下载成功
  switch (status) {
    case '1':
      color = '#00B42A' // 下载成功
      break
    case '0':
      color = '#C9CDD4' // 下载失败
      break
    default:
      color = '#1677FF' // 正在下载
      break
  }
  return { backgroundColor: color }
}

/**
 * 获取状态文本
 */
const getStatusText = (status?: string) => {
  switch (status) {
    case '1':
      return '下载成功'
    case '0':
      return '下载失败'
    default:
      return '正在下载'
  }
}

// 轮询管理
const pollingTimer = ref<number | null>(null)
const POLLING_INTERVAL = 10000 // 10秒

/**
 * 检查是否有正在下载的数据
 */
const hasDownloadingItems = (): boolean => {
  if (!dataList.value || !Array.isArray(dataList.value)) {
    return false
  }
  // status 为空或 undefined 表示正在下载
  return dataList.value.some(item => !item.status || item.status === '')
}

/**
 * 停止轮询
 */
const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

/**
 * 检查并启动/停止轮询
 */
const checkAndStartPolling = () => {
  // 先清除现有轮询
  stopPolling()

  // 检查是否有正在下载的数据
  if (hasDownloadingItems()) {
    // 启动轮询，每10秒调用一次查询接口
    pollingTimer.value = window.setInterval(async () => {
      await tableMethods.handleQuery()
    }, POLLING_INTERVAL)
  }
}

// 监听数据列表变化，自动管理轮询
watch(
  () => dataList.value,
  () => {
    checkAndStartPolling()
  },
  { deep: true }
)

// 生命周期
onMounted(async () => {
  // 初始化数据
  await tableMethods.getList()
  getUserList()
  // 初始检查是否需要启动轮询
  checkAndStartPolling()
})

onUnmounted(() => {
  // 组件卸载时清除轮询
  stopPolling()
})
</script>

<template>
  <div class="user-management">
    <!-- 用户表格 -->
    <el-card class="table-card" shadow="never">
      <div class="flex-between items-center mb-24">
        <div class="text-h3" style="font-weight: 600">下载列表</div>
        <div class="flex gap-16">
          <el-select-v2
            v-if="hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ALL)"
            v-model="formData.userIds"
            :options="userOptions"
            :props="{ value: 'userId', label: 'userName' }"
            placeholder="用户"
            filterable
            clearable
            multiple
            collapse-tags
            :max-collapse-tags="1"
            style="width: 200px"
            @blur="handleSearch"
          />

          <el-input
            v-model="formData.fileName"
            placeholder="请输入文件名搜索"
            clearable
            style="width: 172px"
            :suffix-icon="Search"
            @change="handleSearch"
          />
        </div>
      </div>
      <el-table
        v-loading="loading"
        :data="dataList as any"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="fileName" label="下载内容" />
        <el-table-column prop="downloadTime" label="下载时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="260" />

        <el-table-column label="当前状态" width="100">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagType(row.status)"></div>
              {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="!row.status || row.status !== '1'"
              @click="handleDownload(row)"
            >
              下载到本地
            </el-button>
            <el-button v-if="row.status === '0'" link type="primary" @click="handleRedownload(row)">
              重新下载
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
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.user-management {
  height: 100%;
  display: flex;
  flex-direction: column;

  .table-card {
    flex: 1;
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
      flex: 1;
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
