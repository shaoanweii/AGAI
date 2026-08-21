<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import FtCard from '@/components/FtCard.vue'
import { computedCardHeight } from '@/utils'
import { useTable } from '@/hooks/table'
import { useAppStore } from '@/stores'
import { hasPermission } from '@/utils/permission'
import { downloadFromBlob, getFileNameWithExtension } from '@/utils/download'
import {
  downloadAgain,
  findVisibleUserList,
  FIND_DOWNLOAD_FILE_LIST_URL
} from '@/api/downloadManagement'
import type { ReportDownLoadFileVo } from '@/api/downloadManagement'
import useUserStore from '@/stores/modules/user'
import { downloadFile } from '@/api/common'

defineOptions({
  name: 'settings-download'
})

const userStore = useUserStore()

const DOWNLOAD_MANAGEMENT_BTN_MAP = {
  // 约定：若后端未配置该按钮权限，将默认“仅看自己”
  SELECT_ALL: 'dataCenter-dataQuery-selectAll',
  SELECT_ONESELF: 'dataCenter-dataQuery-selectOwn'
} as const

/**
 * @description: 获取是否全部可见（基于按钮权限）
 */
const getIsAllVisible = () => {
  if (hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ALL)) {
    return true
  }
  if (hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ONESELF)) {
    return false
  }
  return false
}

const isOkResponse = (res: any) => {
  return res?.success === true || res?.code === '200' || res?.code === 200
}

// 用户列表下拉（用于 el-select-v2）
const userOptions = ref<Array<{ userId: string; userName: string }>>([])
const getUserList = async () => {
  if (!hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ALL)) {
    userOptions.value = []
    return
  }

  try {
    const res = await findVisibleUserList(getIsAllVisible())
    if (!isOkResponse(res)) {
      userOptions.value = []
      return
    }

    const result = res?.result || []
    userOptions.value = result
      .map(user => {
        const userId = user?.id || user?.userId || user?.userid || user?.code || ''
        const userName = user?.userName || user?.username || user?.name || ''
        return { userId, userName }
      })
      .filter(item => item.userId && item.userName)

    const currentUserId = (userStore.userId || '').trim()
    if (userOptions.value?.length && currentUserId) {
      table.filter.userIds = [currentUserId]
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userOptions.value = []
  }
}

const dataCallBackByQueryApi = (res: any) => {
  const result = res?.result
  if (result?.records && Array.isArray(result.records)) {
    return {
      list: result.records,
      total: result.total || 0
    }
  }
  if (result?.list && Array.isArray(result.list)) {
    return {
      list: result.list,
      total: result.total || 0
    }
  }
  if (Array.isArray(result)) {
    return {
      list: result,
      total: result.length
    }
  }
  return {
    list: [] as ReportDownLoadFileVo[],
    total: 0
  }
}

const {
  table,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getFirstPageTableData
} = useTable(
  {
    url: FIND_DOWNLOAD_FILE_LIST_URL,
    method: 'POST',
    pageSize: 10
  },
  dataCallBackByQueryApi
)

/**
 * 搜索处理
 */
const handleSearch = async () => {
  table.filter.isAllVisible = getIsAllVisible()
  getFirstPageTableData()
}

const getDownloadUrl = (filePath: string) => {
  if (!filePath) return ''
  if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
    return filePath
  }
  // 统一走“当前站点”静态资源路径，避免被 /api 代理影响
  return `${window.location.origin}/${filePath.replace(/^\/+/, '')}`
}

// 页面级操作 loading：下载文件与重新下载共用，避免用户重复触发操作
const pageLoading = ref(false)

/**
 * 下载处理
 */
const handleDownload = async (row: ReportDownLoadFileVo & Record<string, any>) => {
  pageLoading.value = true
  try {
    const filePath = row.filePath || row.fileUrl || ''
    if (!filePath) {
      ElMessage.warning('文件地址不存在')
      return
    }

    const response = await downloadFile({ id: row.id })
    const filename = getFileNameWithExtension(row.fileName, filePath)

    downloadFromBlob((response as any).data, filename)
    ElMessage.success('文件下载成功')

    // const url = getDownloadUrl(filePath)
    // const response = await fetch(url)
    // if (!response.ok) {
    //   ElMessage.error(`文件下载失败: HTTP ${response.status}`)
    //   return
    // }
    // const blob = await response.blob()
    // const filename = getFileNameWithExtension(row.fileName, filePath)
    // downloadFromBlob(blob, filename)
    // ElMessage.success('文件下载成功')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '文件下载失败')
  } finally {
    pageLoading.value = false
  }
}

/**
 * 重新下载处理
 */
const handleRedownload = async (row: ReportDownLoadFileVo) => {
  pageLoading.value = true
  try {
    if (!row.id) {
      ElMessage.warning('任务ID不存在')
      return
    }

    const res = await downloadAgain({ id: row.id })
    if (isOkResponse(res)) {
      ElMessage.success(res?.message || '重新下载请求已提交')
      await getTableData(false)
    } else {
      ElMessage.error(res?.message || '重新下载失败')
    }
  } catch (error) {
    console.error('重新下载失败:', error)
    ElMessage.error('重新下载失败，请稍后重试')
  } finally {
    pageLoading.value = false
  }
}

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
const pollingTimer = ref<number | undefined>(undefined)
const POLLING_INTERVAL = 10000 // 10秒

const hasDownloadingItems = () => {
  const list = table.list as ReportDownLoadFileVo[]
  if (!Array.isArray(list) || list.length === 0) return false
  return list.some(item => !item.status || item.status === '')
}

const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = undefined
  }
}

const checkAndStartPolling = () => {
  stopPolling()

  if (!hasDownloadingItems()) return

  pollingTimer.value = window.setInterval(async () => {
    table.filter.isAllVisible = getIsAllVisible()
    await getTableData(false)
  }, POLLING_INTERVAL)
}

watch(
  () => table.list,
  () => {
    checkAndStartPolling()
  },
  { deep: true }
)

onMounted(async () => {
  table.filter = {
    userIds: [],
    fileName: '',
    isAllVisible: getIsAllVisible()
  }

  await getUserList()
  await getTableData()
  checkAndStartPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <div v-loading="pageLoading">
    <FtCard
      :style="computedCardHeight(0)"
      title="下载列表"
      model="titleOperation"
      clear-content-top-padding
    >
      <template #extra>
        <div class="flex gap-16">
          <el-select-v2
            v-if="hasPermission(DOWNLOAD_MANAGEMENT_BTN_MAP.SELECT_ALL)"
            v-model="table.filter.userIds"
            :options="userOptions"
            :props="{ value: 'userId', label: 'userName' }"
            placeholder="用户"
            filterable
            clearable
            multiple
            collapse-tags
            :max-collapse-tags="1"
            style="width: 300px"
            @change="handleSearch"
          />

          <el-input
            v-model="table.filter.fileName"
            placeholder="请输入文件名搜索"
            clearable
            style="width: 200px"
            :suffix-icon="Search"
            @change="handleSearch"
          />
        </div>
      </template>

      <el-table
        :data="table.list"
        v-loading="table.loading"
        style="width: 100%; height: 90%"
        :height="'90%'"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="fileName" label="下载内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="downloadTime" label="下载时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="220" show-overflow-tooltip />

        <el-table-column label="当前状态" width="120">
          <template #default="{ row }">
            <div class="flex-y-center">
              <div class="status-icon mr-8" :style="getStatusTagType(row.status)"></div>
              {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.status !== '1'"
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

      <el-pagination
        v-if="table.total >= useAppStore().showPaginationMinLength"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </FtCard>
  </div>
</template>

<style lang="scss" scoped>
.status-icon {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
</style>
