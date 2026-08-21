<script setup lang="ts">
import { getFiles, saUpload } from '@/api/reportManagement'
import type { FileItem } from '@/api/reportManagement/types'
import { ref, computed, onMounted } from 'vue'
import { ElLoading, ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'

defineOptions({
  name: 'reportManagement'
})

const loading = ref(false)
const currentPeriod = ref('')
const fileList = ref<FileItem[]>([])

// 自动填充至5条数据
const reportList = computed(() => {
  const list = [...fileList.value]
  while (list.length < 5) {
    list.push({ fileName: '', uploadTime: '' })
  }
  return list
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFiles()
    // 模拟API调用
    // const data: ReportData = {
    //   currentPeriod: '当前周期为2025年第10周呈报件附件内容',
    //   fileList: [
    //     {
    //       fileName: 'report.pdf',
    //       uploadTime: '2024-10-13 16:30:00',
    //       size: 1024000
    //     }
    //   ]
    // }
    currentPeriod.value = res.result.currentPeriod
    fileList.value = res.result.fileList
  } finally {
    loading.value = false
  }
}

// 自定义上传
const handleUpload = async (options: UploadRequestOptions) => {
  const loadingInstance = ElLoading.service({ fullscreen: true })
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const res = await saUpload(formData)
    if (res.success) {
      ElMessage.success('上传成功')
      await fetchData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    // ElMessage.error('上传失败')
  } finally {
    loadingInstance.close()
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="report-management">
    <div class="rm-title">{{ currentPeriod }}</div>
    <el-card shadow="never" class="card-wrap">
      <el-table v-loading="loading" :data="reportList">
        <el-table-column prop="fileName" label="附件名称" />
        <el-table-column prop="uploadTime" label="附件上传时间" />
        <el-table-column label="附件操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-upload
              :http-request="handleUpload"
              :show-file-list="false"
              accept="application/pdf"
            >
              <el-button type="primary">上传</el-button>
            </el-upload>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip">
        已上传的呈报件内容，如果需要覆盖，则再上传一个同名的呈报件附件，附件只能上传PDF文件。
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.report-management {
  height: 100%;

  .rm-title {
    font-size: 24px;
    text-align: center;
    margin-bottom: 16px;
  }

  .tip {
    margin-top: 16px;
    color: #929aa6;
  }

  .card-wrap {
    height: calc(100% - 40px);
  }
}
</style>
