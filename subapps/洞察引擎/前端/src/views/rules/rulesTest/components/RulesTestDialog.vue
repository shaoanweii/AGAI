<script setup lang="ts">
import { ref, reactive, computed, watch, toRaw } from 'vue'
import { ElMessage } from 'element-plus'
import { useExport } from '@/hooks/useExport'
import useUserStore from '@/stores/modules/user'
import { rulesTestStore } from '../store'
import RuleCategorySelector from './RuleCategorySelector.vue'
import {
  uploadFile,
  downloadTemplate,
  checkUploadRuleTest,
  addRuleTestList,
  getInfoRuleId
} from '@/api/rules'
import { cloneDeep } from 'lodash-es'

// 校验状态枚举
enum CheckStatus {
  UNCHECKED = 0, // 未校验
  CHECKING = 1, // 校验中
  SUCCESS = 2, // 成功
  FAILED = 3 // 失败
}

// 对外：支持 v-model:visible，以及传入编辑数据
const visible = defineModel<boolean>('visible', { default: false })
const props = defineProps<{ record?: any }>()
// 事件：保存成功通知父组件
const emit = defineEmits<{ (e: 'success'): void }>()

const isEdit = computed(() => !!props.record && !!props.record.id)

// 表单与本地状态
const formRef = ref()
const userStore = useUserStore()
const { exportFile } = useExport()

const form = reactive({
  id: '',
  ruleType: '', // 2=清洗规则, 1=闭环规则
  ruleId: [] as string[],
  fileName: '', // 上传完成后返回的 key
  fileBaseName: '', //文件原始名称
  batchId: '' // 文件上传完成后返回的 batchId
})

const formRules = {
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleId: [{ required: true, message: '请选择规则', trigger: 'change' }],
  fileName: [{ required: true, message: '请上传文件', trigger: 'change' }]
}

const hub = reactive({
  loading: false, //详情加载中
  uploadLading: false, // 上传中
  fileUrl: '', //文件地址 用于新增后编辑，用户下载查看
  resultMessages: [] as { type: 'success' | 'warning' | 'error'; text: string }[], // 校验结果
  checkStatus: CheckStatus.UNCHECKED as CheckStatus // 校验状态
})

const ruleCategorySelectorRef = ref()

watch(visible, val => {
  if (!val) {
    //关闭弹框  重置表单
    resetForm()
    return
  }
  if (isEdit.value) {
    fetchDetail()
  } else {
    Object.assign(form, {
      ruleType: rulesTestStore.conditions?.ruleType?.[0]?.key || ''
    })
  }
})

// 获取详情
const fetchDetail = async () => {
  try {
    hub.loading = true
    const res: any = await getInfoRuleId({ id: props.record.id })
    if (res.success && res.result) {
      const detail = res.result
      Object.assign(form, {
        id: detail.id,
        ruleType: detail.ruleType,
        ruleId: Array.isArray(detail.ruleTestList) ? detail.ruleTestList : [],
        fileBaseName: detail.fileBaseName,
        fileName: detail.fileName,
        batchId: detail.batchId
      })
      hub.fileUrl = detail.url
      setCheckStatus(CheckStatus.SUCCESS)
      hub.resultMessages = [{ type: 'success', text: '校验完成' }]
    } else {
      ElMessage.error(res?.message || '获取规则详情失败')
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取规则详情失败')
    console.error('获取详情失败', error)
  } finally {
    hub.loading = false
  }
}

const initialForm = cloneDeep(toRaw(form))
const initialHub = cloneDeep(toRaw(hub))

// 表单重置：恢复到初始值并清空校验状态
function resetForm() {
  //关闭弹框  重置表单
  formRef.value?.resetFields()
  Object.assign(form, cloneDeep(initialForm))
  Object.assign(hub, cloneDeep(initialHub))
}

// 上传
const doUpload = async (option: any) => {
  const { file, onSuccess, onError } = option
  const formData = new FormData()
  formData.append('file', file)
  hub.uploadLading = true
  try {
    const res = await uploadFile(formData)
    if (res.code === '200') {
      form.fileName = res.result?.key || ''
      form.fileBaseName = file?.name || ''
      hub.fileUrl = res.result?.url || ''
      onSuccess?.(res)
      // 选择了新文件后，重置校验状态
      setCheckStatus(CheckStatus.UNCHECKED)
      hub.resultMessages = []
    } else {
      onError?.(res)
    }
  } catch (err) {
    onError?.(err)
  } finally {
    hub.uploadLading = false
  }
}
const clearFile = () => {
  form.fileName = ''
  form.fileBaseName = ''
  hub.fileUrl = ''
  setCheckStatus(CheckStatus.UNCHECKED)
  hub.resultMessages = []
}
const downloadTpl = () => {
  exportFile(downloadTemplate, { clientId: userStore.clientId, fileName: '数据模板.xlsx' })
}

const downloadFile = () => {
  if (!hub.fileUrl) return
  const url = hub.fileUrl.startsWith('http')
    ? hub.fileUrl
    : `${window.location.origin}${hub.fileUrl.startsWith('/') ? '' : '/'}${hub.fileUrl}`
  window.open(url, '_blank')
}

const canStartCheck = computed(() => !!form.fileName)
const setCheckStatus = (s: CheckStatus) => (hub.checkStatus = s)

const startCheck = async () => {
  if (!canStartCheck.value) return
  setCheckStatus(CheckStatus.CHECKING)
  hub.resultMessages = []
  try {
    const payload: any = {
      fileName: form.fileName
    }
    const res: any = await checkUploadRuleTest(payload)
    form.batchId = ''
    if (res?.success) {
      setCheckStatus(CheckStatus.SUCCESS)
      const result = res?.result || {}
      form.batchId = result.batchId || ''
      const message = result.message || '校验完成'
      const succCount = Number(result.success || 0)
      if (succCount) {
        hub.resultMessages.push({ type: 'success', text: message })
      } else {
        hub.resultMessages.push({ type: 'warning', text: message })
      }
      // hub.resultMessages.push({ type: 'success', text: '校验完成。' })
    } else {
      setCheckStatus(CheckStatus.FAILED)
      hub.resultMessages.push({ type: 'error', text: res.message || '校验失败' })
    }
  } catch (e: any) {
    setCheckStatus(CheckStatus.FAILED)
    hub.resultMessages.push({ type: 'error', text: e.message || '校验失败' })
  }
}

// 确定按钮：通常在校验成功后才允许提交；如有后端保存接口，这里可以接入
const onConfirm = async ({ close }: { close: () => void }) => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (hub.checkStatus !== CheckStatus.SUCCESS) {
    ElMessage.warning('请先完成数据校验')
    return
  }
  try {
    let response: any = await addRuleTestList(form)
    if (response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
      // 成功后回到第一页重新拉取，确保数据最新
      close()
      emit('success')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 步骤条状态
const stepActive = computed(() => {
  if (!Array.isArray(form.ruleId) || form.ruleId.length === 0) return 1
  if (!form.fileName) return 2
  if (hub.checkStatus === CheckStatus.UNCHECKED || hub.checkStatus === CheckStatus.CHECKING)
    return 3
  return 4
})
const step4Status = computed(() => {
  if (hub.checkStatus === CheckStatus.FAILED) return 'error'
  if (hub.checkStatus === CheckStatus.SUCCESS) return 'success'
  return 'process'
})
</script>
<template>
  <!-- 基于 AppDialog 的统一弹框（带步骤条） -->
  <AppDialog
    v-model:visible="visible"
    :title="isEdit ? '编辑测试' : '新建测试'"
    width="800px"
    body-class="rule-test-dialog-body"
    style="display: flex; flex-direction: column; height: 814px"
    :confirm="onConfirm"
  >
    <div class="step-layout">
      <!-- 左侧步骤条 -->
      <div class="step-sidebar">
        <el-steps direction="vertical" :active="stepActive">
          <el-step style="flex-basis: 52px" />
          <el-step style="flex-basis: 290px" />
          <el-step :style="{ 'flex-basis': form.fileName ? '160px' : '100px' }" />
          <el-step :status="step4Status" />
        </el-steps>
      </div>

      <!-- 右侧表单内容 -->
      <div class="step-content">
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="auto">
          <!-- 1. 规则类型 -->
          <el-form-item label="规则类型" prop="ruleType" required>
            <el-radio-group v-model="form.ruleType" class="rules-radio-group">
              <el-radio-button
                v-for="item in rulesTestStore.conditions.ruleType"
                :key="item.key"
                :value="item.key"
                :label="item.value"
              />
            </el-radio-group>
          </el-form-item>

          <!-- 2. 选择规则（左右双列） -->
          <el-form-item label="选择规则" prop="ruleId" required>
            <RuleCategorySelector
              v-if="visible"
              ref="ruleCategorySelectorRef"
              v-model="form.ruleId"
            />
          </el-form-item>

          <!-- 3. 上传文件（复用数据中心上传能力） -->
          <el-form-item label="上传文件" prop="fileName" required>
            <div class="upload-wrapper">
              <el-upload
                accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,.csv"
                :show-file-list="false"
                :http-request="doUpload"
              >
                <template #trigger>
                  <el-button type="primary" :loading="hub.uploadLading">
                    <template #icon>
                      <el-icon>
                        <el-icon-plus />
                      </el-icon> </template
                    >{{ form.fileName ? '重新上传' : '点击上传' }}</el-button
                  >
                </template>
              </el-upload>
              <div class="upload-filename mt-24" v-if="form.fileName">
                <i class="iconfont icon-file-excel-2-line icno-excel" style="color: #5f6a7a"></i>
                <span @click="downloadFile" class="ml-8 single-line">{{ form.fileBaseName }}</span>
                <el-icon class="ml-8" @click="clearFile" style="cursor: pointer"
                  ><el-icon color="#5F6A7A"><Delete /></el-icon
                ></el-icon>
              </div>
              <div class="upload-tips mt-8">
                <div>文件支持excel格式的文件，文件数据限制20000条以内</div>
                <div>
                  请先下载模板，并按照格式进行调整后上传
                  <el-link type="primary" @click="downloadTpl" class="tpl-link"
                    >《数据模板》</el-link
                  >
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 4. 数据校验（按三态切换按钮与结果展示） -->
          <el-form-item label="数据校验" required>
            <div class="flex-y-center">
              <!-- 按钮区：开始校验 / 校验中... / 重新校验 -->
              <template v-if="hub.checkStatus === CheckStatus.UNCHECKED">
                <el-button type="primary" :disabled="!canStartCheck" @click="startCheck"
                  >开始校验</el-button
                >
              </template>
              <template v-else-if="hub.checkStatus === CheckStatus.CHECKING">
                <el-button class="checking-btn"
                  ><span style="color: #165dff">校验中...</span></el-button
                >
              </template>
              <template v-else-if="hub.checkStatus === CheckStatus.SUCCESS">
                <el-button type="primary" @click="startCheck">重新校验</el-button>
              </template>
              <template v-else-if="hub.checkStatus === CheckStatus.FAILED">
                <el-button type="primary" @click="startCheck">重新校验</el-button>
              </template>
            </div>
            <div class="check-wrapper">
              <!-- 结果区：根据不同结果渲染不同样式内容 -->
              <div class="result-list" v-if="hub.checkStatus === CheckStatus.SUCCESS">
                <template v-for="(item, idx) in hub.resultMessages" :key="idx">
                  <p v-if="item.type === 'error'" class="result-line error">
                    <i class="iconfont icon-shibai mr-8"></i>{{ item.text }}
                  </p>
                  <p v-else-if="item.type === 'warning'" class="result-line warn">
                    <i class="iconfont icon-tishi mr-8"></i>{{ item.text }}
                  </p>
                  <p v-else class="result-line success">
                    <i class="iconfont icon-wancheng mr-8"></i>{{ item.text }}
                  </p>
                </template>
              </div>
              <div class="result-list" v-else-if="hub.checkStatus === CheckStatus.FAILED">
                <p class="result-line error">
                  <i class="iconfont icon-shibai mr-8"></i>校验失败，请重新校验。
                </p>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </AppDialog>
</template>
<style lang="scss">
.rule-test-dialog-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
<style scoped lang="scss">
.step-layout {
  display: flex;
  gap: 10px;
}
.step-sidebar {
  width: 24px;
  padding-top: 2px;
}
.step-content {
  flex: 1;
}

.rules-radio-group {
  .el-radio-button + .el-radio-button {
    margin-left: 16px;
  }
  :deep(.el-radio-button__inner) {
    border-radius: 4px !important;
    border: 1px solid #dfe2e8 !important;
    font-weight: 500 !important;
    font-size: 14px !important;
  }
  :deep(.el-radio-button.is-active .el-radio-button__inner) {
    color: #1677ff !important;
    border: 1px solid #1677ff !important;
    background-color: transparent !important;
    box-shadow: none !important;
  }
}

.upload-wrapper {
  display: flex;
  flex-direction: column;
}
.upload-filename {
  display: flex;
  align-items: center;
  font-weight: 400;
  font-size: 14px;
  color: #1d2129;
  background: #f7f8fa;
  border-radius: 2px 2px 2px 2px;
  padding: 2px 14px;
}
.single-line {
  display: inline-block;
  width: 400px;
  white-space: nowrap; /* 禁止文字换行 */
  overflow: hidden; /* 隐藏超出容器的内容 */
  text-overflow: ellipsis; /* 超出部分显示省略号 */
  min-width: 0;
}
.upload-tips {
  font-weight: 400;
  font-size: 12px;
  color: #929aa6;
  line-height: 22px;
  .tpl-link {
    font-size: 12px !important;
  }
}
.file-link {
  color: #1677ff;
  text-decoration: none;
  cursor: pointer;
  &:hover {
    color: #4096ff;
  }
}

.checking-btn {
  background-color: #f2f3f5 !important;
}

.check-wrapper {
  width: 100%;
  box-sizing: border-box;
  display: flex;
}
.result-list {
}
.result-line {
  margin: 6px 0;
  display: flex;
  align-items: center;
}
.result-line.success {
  color: #00b42a;
}
.result-line.warn {
  color: #ff7d00;
}
.result-line.error {
  color: #f53f3f;
}
:deep(.el-step__line) {
  background-color: #ebedf0 !important;
  width: 1px !important;
}
:deep(.el-step__icon.is-text) {
  background: #c9ced6;
  border: 1px solid #e5e6eb;
  .el-step__icon-inner {
    font-weight: 400;
    font-size: 14px;
    color: #5f6a7a;
  }
}
:deep(.el-step__head.is-finish) {
  .el-step__icon.is-text {
    background: #1677ff !important;
    border: 1px solid #e5e6eb !important;
    .el-step__icon-inner {
      color: #fff;
    }
  }
}
</style>
