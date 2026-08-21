<template>
  <el-drawer v-model="visible" :size="1200" @open="handleOpen" @close="handleClose">
    <template #header>
      <h4 class="fw-600">导入数据</h4>
    </template>
    <template #default>
      <div v-loading="loading" style="width: 100%; height: 100%">
        <div class="body-wrapper">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form">
            <el-form-item prop="dataSourceId" label="数据源">
              <el-select
                v-model="form.dataSourceType"
                :data-testid="`dataSource-import-10001`"
                disabled
                style="width: 112px; margin-right: 10px"
              >
                <el-option
                  v-for="(item, index) in conditions.dataType"
                  :key="index"
                  :data-testid="`dataSource-import-10001-op-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
              <el-select
                v-model="form.dataSourceId"
                :data-testid="`dataSource-import-10002`"
                placeholder="请选择"
                :max-collapse-tags="2"
                clearable
              >
                <el-option
                  v-for="(item, index) in dataSourceList"
                  :key="index"
                  :data-testid="`dataSource-import-10002-op-${index}`"
                  :label="item.dataSourceName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item prop="dataName" label="数据名称">
              <el-input
                v-model.trim="form.dataName"
                :data-testid="`dataSource-import-10003`"
                clearable
                :maxlength="50"
                placeholder="请输入"
              />
            </el-form-item>
            <el-form-item prop="fileName" label="上传文件">
              <div class="flex">
                <div class="upload">
                  <!--失败class添加 fail-->
                  <i class="iconfont icon-file-excel-2-line icno-excel mb-8"></i>
                  <p style="width: 80%; word-break: break-all; text-align: center" class="mb-8">
                    {{ form.fileName }}
                  </p>
                  <el-upload
                    accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,.csv"
                    :show-file-list="false"
                    :custom-request="customRequest"
                  >
                    <template #upload-button>
                      <el-button type="primary" :data-testid="`dataSource-import-10004`">
                        {{ form.fileName ? '重新上传' : '上传文件' }}
                      </el-button>
                    </template>
                  </el-upload>
                </div>

                <div class="upload-tip">
                  <p class="mb-12 flex item-center">
                    <i class="circle mr-4"></i>
                    本地文件支持excel和csv对应格式的文件；
                  </p>
                  <p class="mb-12 flex item-center">
                    <i class="circle mr-4"></i>
                    文件数据限制20000条以内；
                  </p>
                  <p class="mb-12 flex item-center">
                    <i class="circle mr-4"></i>
                    请先下载模板，并按照格式进行调整后上传；
                  </p>
                  <!--<p class="file-info mb-12">《文本格式模板》.csv</p>-->
                  <p
                    class="file-info point"
                    :data-testid="`dataSource-import-10005`"
                    @click="downloadTem"
                  >
                    《文本格式模板》.xlsx
                  </p>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="batchId" label="数据校验">
              <div class="check-wrapper">
                <el-button
                  v-if="checkStatus === 0"
                  :data-testid="`dataSource-import-10006`"
                  :disabled="!form.fileName"
                  size="mini"
                  type="primary"
                  @click="startCheck"
                  >开始校验
                </el-button>
                <el-button
                  v-if="checkStatus === 1"
                  size="mini"
                  :data-testid="`dataSource-import-10007`"
                >
                  <span style="color: #165dff">校验中...</span>
                </el-button>
                <template v-if="checkStatus === 2">
                  <!--<div class="flex item-center">-->
                  <!--  <i class="iconfont icon-wancheng mr-8" style="color: #00B42A; font-size: 20px"></i>-->
                  <!--  <p>{{ checkResult?.message }}</p>-->
                  <!--</div>-->
                  <div
                    v-if="checkResult?.total !== 0 && checkResult?.success !== 0"
                    class="flex item-center"
                  >
                    <i
                      class="iconfont icon-wancheng mr-8"
                      style="color: #00b42a; font-size: 20px"
                    ></i>
                    <!--校验完成，共2000条，有效数据1929条，系统仅导入有效数据。-->
                    <p>{{ checkResult?.message }}</p>
                  </div>

                  <div v-else-if="checkResult.success === 0" class="flex item-center">
                    <i class="iconfont icon-tishi mr-8" style="color: #ff7d00; font-size: 20px"></i>
                    <!--校验完成，共2000条，有效数据0条，请重新上传。-->
                    <p style="color: #ff7d00">{{ checkResult?.message }}</p>
                  </div>
                  <div v-else class="flex item-center">
                    <i class="iconfont icon-tishi mr-8" style="color: #ff7d00; font-size: 20px"></i>
                    <!--<p style="color: #FF7D00">校验完成，上传文件数据与模板结构不符，请重新上传。</p>-->
                    <p style="color: #ff7d00">{{ checkResult?.message }}</p>
                  </div>
                </template>

                <div v-if="checkStatus === 3" class="flex item-center">
                  <el-button
                    size="mini"
                    type="primary"
                    :data-testid="`dataSource-import-10008`"
                    @click="startCheck"
                  >
                    重新校验
                  </el-button>
                  <i
                    class="iconfont icon-shibai mr-8 ml-16"
                    style="color: #f53f3f; font-size: 20px"
                  ></i>
                  <p style="color: #f53f3f">校验失败，请重新校验。</p>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :disabled="OkBtnDisabled" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script lang="ts" setup>
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import {
  checkUploadDataSource,
  downloadDataSource,
  getDataSourceList,
  saveUploadDataSource,
  uploadDataSource
} from '@/api/dataCenter'
import { useExport } from '@/hooks/useExport'
import { ElMessage } from 'element-plus'
import { resetObjectValues } from '@/utils'
import useUserStore from '@/stores/modules/user'

const visible = defineModel({ default: false })
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const emit = defineEmits(['refreshList'])
const { exportFile } = useExport()
const props = withDefaults(
  defineProps<{
    curDataSource: any
  }>(),
  {}
)
const { curDataSource } = toRefs(props)

const userStore = useUserStore()

const rules = {
  dataSourceId: [{ required: true, message: '请选择数据源' }],
  dataName: [{ required: true, message: '请输入数据名称' }]
}

const form = reactive({
  dataSourceType: '',
  dataSourceId: '',
  dataName: '',
  fileName: '',
  batchId: '',
  clientId: ''
})
const loading = ref(false)
/**
 * 下载模板
 */
const downloadTem = async () => {
  exportFile(downloadDataSource, {
    clientId: userStore.clientId,
    fileName: '文本格式模板.xlsx'
  })
}

/**
 * 上传文件
 * @param option
 */
const customRequest = (option: any) => {
  const { fileItem, onSuccess, onError } = option
  loading.value = true
  const formData = new FormData()
  formData.append('file', fileItem.file)
  uploadDataSource(formData)
    .then(res => {
      if (res.code === '200') {
        form.fileName = res.result?.key
        onSuccess(res)
      }
    })
    .catch(err => {
      onError(err)
    })
    .finally(() => {
      loading.value = false
      checkResult.value = null
      setCheckStatus(0)
    })
  return {
    abort() {}
  }
}

/**
 * 0 未校验
 * 1 校验中
 * 2 校验成功
 * 3 校验失败
 */
const checkStatus = ref(0)
const checkResult = ref()
const setCheckStatus = (status: number) => {
  checkStatus.value = status
}
/**
 * 开始校验
 */
const startCheck = async () => {
  setCheckStatus(1)
  try {
    const response = await checkUploadDataSource({
      clientId: userStore.clientId,
      fileName: form.fileName
    })
    if (response.code === '200') {
      setCheckStatus(2)
      form.batchId = response.result.batchId
      checkResult.value = response.result
    } else {
      setCheckStatus(3)
      checkResult.value = response.result
    }
  } catch (e) {
    setCheckStatus(3)
    checkResult.value = null
  }
}

const dataSourceList = ref<any[]>([])

const getDataSource = async () => {
  try {
    dataSourceList.value = await getDataSourceList({
      clientId: userStore.clientId!,
      dataSourceType: curDataSource.value.dataSourceType
    }).then(res => res.result)
  } catch (e) {
    dataSourceList.value = []
  }
}

const OkBtnDisabled = computed(() => {
  return !(form.batchId && checkResult.value?.success > 0)
})

const handleOpen = () => {
  form.clientId = userStore.clientId!
  form.dataSourceType = curDataSource.value.dataSourceType
  form.dataSourceId = curDataSource.value.id
  getDataSource()
}
const handleCancel = () => {
  handleClose()
}
// 关闭
const handleClose = () => {
  visible.value = false
  loading.value = false
  setCheckStatus(0)
  checkResult.value = null
  resetObjectValues(form)
}
const formRef = ref()
const handleOk = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      saveUploadDataSource(form)
        .then(res => {
          if (res.code === '200') {
            emit('refreshList')
            handleCancel()
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch((err: any) => {
          ElMessage.error(err.message)
        })
    }
  } catch (error) {
    console.log('表单验证失败:', error)
  }
}
</script>

<style lang="scss" scoped>
.body-wrapper {
  padding: 12px 100px 12px 30px;
  //padding-right: 100px;
  .upload {
    width: 200px;
    height: 156px;
    background: #f2f3f5;
    border-radius: 2px 2px 2px 2px;
    border: 1px dashed #e5e6eb;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .icno-excel {
      color: #165dff;
      font-size: 28px;

      &.fail {
        color: #f53f3f;
      }
    }
  }

  .upload-tip {
    padding: 11px 0 0 24px;
    font-size: 12px;
    color: #6e7b91;
    line-height: 14px;
    box-sizing: border-box;

    .circle {
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background-color: #6e7b91;
    }

    .file-info {
      font-size: 14px;
      color: #165dff;
      line-height: 22px;
    }
  }

  .check-wrapper {
    width: 100%;
    border: 1px solid #e5e6eb;
    height: 60px;
    padding: 0 16px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
  }
}
</style>
