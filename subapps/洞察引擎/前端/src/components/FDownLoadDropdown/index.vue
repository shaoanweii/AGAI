<template>
  <div class="FDownLoadDropdown">
    <el-tooltip
      :visible="visible"
      placement="bottom-start"
      popper-class="down-wrapper"
      :show-arrow="false"
    >
      <img
        src="@/assets/icon/icon_down_2.png"
        style="width: 40px; height: 40px"
        class="point"
        alt=""
        @click="handleVisible"
      />
      <template #content>
        <div v-if="fileList.id" class="down-item">
          <div class="left">
            <img src="@/assets/icon/icon_excel.jpg" style="width: 30px; height: 30px" alt="" />
          </div>
          <div class="right">
            <div class="info-1">{{ fileList?.taskName }}</div>
            <div class="info-2">
              <template v-if="fileList.id">
                <!-- 0是失败  null是下载中, null && taskId 是成功  1是成功 -->
                <div v-if="fileList?.status?.toString() === '1'" class="success">
                  <span>导出完成</span>
                  <span class="local point" @click="handleDownload">下载到本地</span>
                </div>
                <div v-else-if="fileList?.status?.toString() === '0'" class="fail">
                  <span>导出失败，请重新尝试</span>
                </div>
                <div v-else class="loading">
                  <span>导出中，请稍后...</span>
                </div>
              </template>
            </div>
          </div>
        </div>
        <Empty v-else />
      </template>
    </el-tooltip>
  </div>
</template>

<script lang="ts" setup>
import { getFile } from '@/api/main'
import { ElMessage } from 'element-plus'
import to from 'await-to-js'
import eventBus from '@/utils/eventBus'
import axios from 'axios'
import { useAppStore } from '@/stores'

const visible = ref(false)
const timer = ref()
const appStore = useAppStore()
const handleVisible = () => {
  visible.value = true
}

const hanleWindowClick = (e: any) => {
  if (!e.target.closest('.down-wrapper') && !e.target.closest('.point')) {
    visible.value = false
  }
}

const updateFile = () => {
  if (timer.value) {
    return
  }
  // 有导出数据的时候轮询 5s一次  返回结果为成功或者失败的时候停止轮询
  timer.value = setInterval(() => {
    getFileApi()
  }, 5000)
}

const clearTimer = () => {
  clearInterval(timer.value)
  timer.value = undefined
}

const fileList = ref<any>()
const getFileApi = async () => {
  const [errs, data] = await to(getFile({}))

  if (errs) {
    ElMessage.error(errs.message)
    fileList.value = undefined
  }
  if (data) {
    fileList.value = data.result
    appStore.setFileData(data.result)
    // 没有导出数据的时候停止轮询 || 有导出结果的时候停止轮询
    // status 为 1和taskid为空的时候停止轮询
    // 有taskid status为其他时需要轮询
    if (data.result?.status?.toString() === '1' || !data.result?.taskId) {
      clearTimer()
    } else {
      updateFile()
    }
    // if (
    //   data.result?.status?.toString() === '1' ||
    //   data.result?.status?.toString() === '0' ||
    //   (!data.result?.status && data.result?.taskId)
    // ) {
    //   clearTimer()
    // } else if (data.result?.id) {
    //   updateFile()
    // }
  }
}

onBeforeMount(() => {
  getFileApi()
  window.addEventListener('click', hanleWindowClick)
})

eventBus.on('updateFile', () => {
  if (timer.value) {
    clearTimer()
  }
  updateFile()
})

onBeforeUnmount(() => {
  eventBus.off('updateFile')
  window.removeEventListener('click', hanleWindowClick)
})

const getFileUrl = () => {
  let _fileUrl = fileList.value.fileUrl

  if (_fileUrl.startsWith('http')) {
    _fileUrl = fileList.value.fileUrl
  } else {
    _fileUrl = `${window.location.origin}${fileList.value.fileUrl}`
  }

  return _fileUrl
}

const getFileName = () => {
  const fileKeyArr = fileList.value.fileKey?.split('/')
  return fileKeyArr[fileKeyArr?.length - 1] || fileList.value.fileKey
}

const downloadExcel = async () => {
  try {
    const response = await axios({
      method: 'get',
      url: getFileUrl(),
      responseType: 'blob'
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    // link.download = '6b8712f88edc33e25facb484fbc5891e.xlsx'
    link.download = getFileName()
    document.body.appendChild(link)
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载出错:', error)
  }
}

const handleDownload = () => {
  // window.open(fileList.value.fileUrl, '_blank')
  // window.open(fileList.value.fileUrl)
  downloadExcel()
  visible.value = false
}
</script>

<style lang="scss">
.FDownLoadDropdown {
}

.down-wrapper {
  background-color: transparent !important;
  background-color: #fff !important;
  width: 300px !important;
  color: #000 !important;
  padding: 0 !important;
  border-radius: 8px;
  max-height: 400px;
  overflow: auto;
  margin-right: 30px;
  /* box-shadow: 0.6px 0.6px 0.6px rgba(0, 0, 0, 0.034), 1.3px 1.4px 1.3px rgba(0, 0, 0, 0.048),
    2.5px 2.6px 2.5px rgba(0, 0, 0, 0.06), 4.5px 4.7px 4.5px rgba(0, 0, 0, 0.072),
    8.4px 8.8px 8.4px rgba(0, 0, 0, 0.086), 20px 21px 20px rgba(0, 0, 0, 0.12); */
  box-shadow: 0px 0 10px 0px rgba(0, 0, 0, 0.2);
  .down-item {
    display: flex;
    padding: 5px 10px;
    .left {
      width: 30px;
      flex: none;
      display: flex;
      align-items: center;
    }
    .right {
      flex: 1;
      margin-left: 10px;
      .info-1 {
        font-size: 16px;
        word-break: break-all;
      }
      .info-2 {
        font-size: 12px;
        color: #666;
      }

      .success {
        .local {
          color: #55aee5;
          text-decoration: underline;
          margin-left: 10px;
        }
      }
      .loading {
        color: #666;
      }
      .fail {
        color: #dd3e3e;
      }
    }
  }
}
</style>
