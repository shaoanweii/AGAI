<template>
  <el-dialog
    v-model="visible"
    title="数据纠错"
    width="95%"
    style="padding: 0; border-radius: 8px"
    destroy-on-close
    header-class="user-dialog-form-header-class"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="subtitle">数据信息</div>
      <div class="info-text mt-16">共{{ rowData?.length }}条数据</div>

      <div class="flex-between mt-24 items-center">
        <div class="subtitle">修正字段值</div>
        <div class="flex items-center">
          <div class="subtitle">数据有效性</div>
          <div class="custome-switch-btn-wrap ml-10">
            <div
              class="csb-item"
              :class="{ 'csb-tap': errorType === '2' }"
              @click="switchErrorType('2')"
            >
              有效数据
            </div>
            <div
              class="csb-item"
              :class="{ 'csb-tap': errorType === '1' }"
              @click="switchErrorType('1')"
            >
              无效数据
            </div>
          </div>
        </div>
      </div>

      <el-table :data="tableData" size="large" style="width: 100%" class="mt-16">
        <el-table-column prop="field" label="字段" />
        <el-table-column prop="afterValue" label="编辑前数据值" />
        <el-table-column prop="beforeValue" label="修改为数据值">
          <template #default="{ row }">
            <template v-if="row.field === '品牌'">
              <el-select-v2
                v-model="row.beforeValue"
                placeholder="品牌"
                clearable
                filterable
                :options="brandOptions"
                @change="handleBrandChange"
              />
            </template>
            <template v-if="row.field === '车系'">
              <el-select-v2
                v-model="row.beforeValue"
                placeholder="车系"
                clearable
                filterable
                :options="carSeriesOptions"
              />
            </template>
            <template v-if="row.field === '观点'">
              <el-select-v2
                v-model="row.beforeValue"
                placeholder="观点"
                clearable
                filterable
                :options="topicOptions"
                :props="{ label: 'tagName', value: 'tagCode' }"
                :popper-class="'selectV2PopClass'"
              />
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <div class="dialog-footer flex-y-center">
        <div class="footer-btn-layout flex-y-center">
          <el-button class="flex-1" @click="handleClose">取消</el-button>
          <el-button class="flex-1" type="primary" :loading="submitLoading" @click="handleConfirm">
            确定
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'
import { dataQueryInsertLabelCorrection } from '@/api/dataCenter'

interface Props {
  rowData: any[]
  topicOptions: TopicOption[]
  filter: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

interface TopicOption {
  tagCode: string
  tagName: string
  emotion?: string
  intention?: string
}

interface CorrectionTableRow {
  field: string
  afterValue: string
  beforeValue: string
}

const emit = defineEmits<Emits>()

const { rowData, topicOptions = [], filter } = defineProps<Props>()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

// 品牌选项
const brandOptions = computed(
  () => conditions.brandCar?.map(item => ({ label: item.value, value: item.key })) || []
)

// 车系下拉中的“-”仅用于界面展示，提交给后端时需显式传字符串 'null'。
const EMPTY_CAR_SERIES_OPTION = {
  label: '-',
  value: 'null'
}

// 车系选项 - 根据选中的品牌动态更新
const carSeriesOptions = ref<any[]>([])

// 处理品牌变化
const handleBrandChange = (brandValue: string) => {
  // 清空车系选择
  const carSeriesRow = tableData.value.find(row => row.field === '车系')
  if (carSeriesRow) {
    carSeriesRow.beforeValue = ''
  }

  // 更新车系选项
  if (brandValue) {
    const selectedBrand = conditions.brandCar?.find(item => item.key === brandValue)
    const brandCarSeriesOptions =
      selectedBrand?.children?.map(child => ({
        label: child.value,
        value: child.key
      })) || []
    // 品牌选中后，车系下拉顶部插入一个空车系占位，满足人工纠错的特殊传参约定。
    carSeriesOptions.value = [EMPTY_CAR_SERIES_OPTION, ...brandCarSeriesOptions]
  } else {
    carSeriesOptions.value = []
  }
}

const visible = defineModel('visible', {
  default: false
})

type ErrorType = '1' | '2'

const errorType = ref<ErrorType>('2')

const switchErrorType = (type: ErrorType) => {
  errorType.value = type
}

// 修正字段值表格仅保留当前允许人工修正的品牌、车系和观点三项
const tableData = ref<CorrectionTableRow[]>([
  {
    field: '品牌',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '车系',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '观点',
    afterValue: '',
    beforeValue: ''
  }
  // {
  //   field: '场景',
  //   afterValue: '',
  //   beforeValue: ''
  // }
])

// 响应式数据
const submitLoading = ref(false)

/**
 * @description: 根据当前选中的观点编码回查完整观点对象，纠错提交时复用其中的情感和意图字段。
 * @return {TopicOption | undefined}
 */
const getSelectedTopicOption = () => {
  const selectedTopicCode = tableData.value[2]?.beforeValue
  if (!selectedTopicCode) {
    return undefined
  }

  // 中文注释：观点下拉选项除展示名称外，还承载纠错接口所需的 emotion / intention 关联字段。
  return topicOptions.find(item => String(item.tagCode ?? '') === String(selectedTopicCode))
}

const handleOpen = () => {
  console.log('rowData', rowData)
  if (rowData?.length === 1) {
    // 品牌
    tableData.value[0].afterValue = rowData[0].brandName
    // 车系
    tableData.value[1].afterValue = rowData[0].carSeriesName
    // 观点
    tableData.value[2].afterValue = rowData[0].topicText
    // 场景
    // tableData.value[3].afterValue = rowData[0].scenario
  } else if (rowData?.length > 1) {
    // 品牌
    tableData.value[0].afterValue = '*'
    // 车系
    tableData.value[1].afterValue = '*'
    // 观点
    tableData.value[2].afterValue = '*'
    // 场景
    // tableData.value[3].afterValue = '*'
  }
}

const handleConfirm = async () => {
  // 数据有效性为无效数据时，不需要校验必填字段
  // if (errorType.value === '2') {
  //   // 校验必填字段（除了车系）
  //   const requiredFields = [
  //     { field: '品牌', value: tableData.value[0].beforeValue },
  //     { field: '观点', value: tableData.value[2].beforeValue }
  //   ]

  //   for (const item of requiredFields) {
  //     if (!item.value) {
  //       ElMessage.warning(`请选择${item.field}`)
  //       return
  //     }
  //   }
  // }

  const loading = ElLoading.service({
    lock: true,
    text: '数据提交中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    const carSeriesValue = tableData.value[1].beforeValue
    const selectedTopicOption = getSelectedTopicOption()
    const _params = {
      newId: rowData.map(el => el.id),
      errorType: errorType.value,
      // 品牌code
      brandCode: tableData.value[0].beforeValue,
      // 车系code
      carSeriesCode: carSeriesValue,
      // 观点
      topicCode: tableData.value[2].beforeValue,

      brandName:
        brandOptions.value.find(item => item.value === tableData.value[0].beforeValue)?.label || '',
      // 选中“-”时，展示名称与接口约定值不同，这里强制传后端要求的字符串 'null'。
      carSeriesName:
        carSeriesValue === 'null'
          ? 'null'
          : carSeriesOptions.value.find(item => item.value === carSeriesValue)?.label || '',
      topicName: selectedTopicOption?.tagName || '',
      // 中文注释：观点被人工改成标准观点后，情感和意图必须与该观点的元数据保持一致，避免后端纠错记录缺字段。
      sentiment: selectedTopicOption?.emotion || '',
      intention: selectedTopicOption?.intention || '',
      startTime: filter?.startTime,
      endTime: filter?.endTime
      // // 用车场景一级
      // usageScenarioFirst: tableData.value[3].beforeValue,
      // // 用车场景二级
      // usageScenarioSecond: tableData.value[3].beforeValue
    }
    const res = (await dataQueryInsertLabelCorrection(_params)) as any
    if (res.success) {
      ElMessage.success('操作成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.warning(res.message)
    }
  } catch (error: any) {
    ElMessage.warning(error.message)
  } finally {
    loading.close()
  }
}

const handleClose = () => {
  visible.value = false
  errorType.value = '2'
  carSeriesOptions.value = []

  tableData.value.forEach(row => {
    row.beforeValue = ''
  })
}
</script>

<style lang="scss">
.user-dialog-form-header-class {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #ebf4fd 0%, #ffffff 100%);
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
}
</style>
<style lang="scss" scoped>
.dialog-content {
  padding: 0 24px;

  .custome-switch-btn-wrap {
    display: flex;
    align-items: center;
    gap: 8px;
    .csb-item {
      padding: 6px 14px;
      font-weight: 500;
      font-size: 14px;
      color: #535862;
      line-height: 20px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #dfe2e8;
      cursor: pointer;
      &.csb-tap {
        border: 1px solid #1677ff;
        color: #1677ff;
      }
    }
  }

  :deep(.el-table thead th.el-table__cell) {
    font-weight: 600;
    font-size: 14px;
    color: #1d2129;
    line-height: 22px;
  }
}
.subtitle {
  font-weight: 600;
  font-size: 16px;
  color: #1d2129;
  line-height: 24px;
}
.info-text {
  font-weight: 400;
  font-size: 14px;
  color: #1d2129;
  line-height: 22px;
}
.dialog-footer {
  height: 80px;
  border-top: 1px solid #ebedf0;
}

.footer-btn-layout {
  gap: 8px;
  width: 100%;
  padding: 0 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  &:focus-within {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }
}

:deep(.el-textarea__inner) {
  &:focus {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }
}
</style>
