<template>
  <el-dialog
    v-model="visible"
    title="语料映射详情"
    width="800px"
    style="padding: 0"
    header-class="user-dialog-form-header-class"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="review-setps">
        <div class="rs-item">
          <div class="rs-icon">
            <img src="@/assets/imgs/success.png" alt="" />
          </div>
          <div class="rs-info ml-8">
            <div>
              <span>{{ detailObj.submitUserName }}</span> <span class="ml-4 mr-4">|</span>
              <span>{{ detailObj.submitUserEmployeeID }}</span>
              <span class="ml-8">提交语料映射申请</span>
            </div>
            <div class="rsi-time mt-4">{{ detailObj.submitTime }}</div>
          </div>
        </div>

        <template v-if="detailObj.auditUserEmployeeID">
          <div class="rs-dividing-line"></div>

          <div class="rs-item">
            <div class="rs-icon">
              <img v-if="rowData.auditStatusCode === '1'" src="@/assets/imgs/success.png" alt="" />
              <img
                v-if="['2', '3'].includes(rowData.auditStatusCode)"
                src="@/assets/imgs/audiStatus-2.png"
                alt=""
              />
            </div>
            <div class="rs-info ml-8">
              <div>
                <span>{{ detailObj.auditUserName }}</span> <span class="ml-4 mr-4">|</span>
                <span>{{ detailObj.auditUserEmployeeID }}</span>
                <span class="ml-8">
                  <span v-if="rowData.auditStatusCode === '1'">审核通过</span>
                  <span v-if="rowData.auditStatusCode === '2'">审核失败</span>
                  <span v-if="rowData.auditStatusCode === '3'">审核撤销</span>
                </span>
              </div>
              <div class="rsi-time mt-4">{{ detailObj.auditTime }}</div>
            </div>
          </div>
        </template>
      </div>
      <div class="subtitle mt-24">语料数据信息</div>
      <el-table :data="table.list" size="large" style="width: 100%" class="mt-16">
        <el-table-column prop="id" show-overflow-tooltip label="ID" />
        <el-table-column prop="brandName" show-overflow-tooltip label="品牌" />
        <el-table-column prop="carSeriesName" show-overflow-tooltip label="车系" />
        <el-table-column prop="originalTextScene" show-overflow-tooltip label="声音片段" />
        <el-table-column prop="tagType" show-overflow-tooltip label="标签类型" />
        <el-table-column prop="category" show-overflow-tooltip label="所属分类" />
        <el-table-column prop="sentiment" show-overflow-tooltip label="情感" />
        <el-table-column prop="intention" show-overflow-tooltip label="意图" />
      </el-table>
      <el-pagination
        v-if="table.total > 0"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 15, 20, 25]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
      />

      <div class="subtitle mt-24">映射字段值</div>

      <el-table :data="tableData" size="large" style="width: 100%" class="mt-16">
        <el-table-column prop="field" label="字段" />
        <el-table-column prop="dataValueBefore" label="编辑前数据值" />
        <el-table-column prop="dataValueAfter" label="修改为数据值">
          <!-- <template #default="{ row }">
            <template v-if="row.field === '品牌'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="品牌"
                clearable
                filterable
                :options="brandOptions"
                @change="handleBrandChange"
              />
            </template>
            <template v-if="row.field === '车系'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="车系"
                clearable
                filterable
                :options="carSeriesOptions"
              />
            </template>
            <template v-if="row.field === '情感'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="情感"
                clearable
                :options="sentimentOptions"
              />
            </template>
            <template v-if="row.field === '意图'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="意图"
                clearable
                :options="intentionOptions"
              />
            </template>
            <template v-if="row.field === '观点'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="观点"
                clearable
                filterable
                :options="topicOptions"
                :props="{ label: 'tagName', value: 'tagCode' }"
              />
            </template>
          </template> -->
        </el-table-column>
      </el-table>
      <div class="subtitle mt-24">映射数据有效性</div>
      <div class="custome-switch-btn-wrap mt-16">
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

    <template #footer>
      <div class="dialog-footer flex-y-center">
        <div v-if="rowData.auditStatusCode === '0'" class="footer-btn-layout flex-y-center">
          <!-- 1通过， 2拒绝， 3撤销 -->
          <el-button class="flex-1" @click="handleSubmit(3)">撤销</el-button>
          <el-button class="flex-1" @click="handleSubmit(2)">拒绝</el-button>
          <el-button
            class="flex-1"
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit(1)"
          >
            通过
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { auditLabelCorrection, queryCorrectionInfo } from '@/api/review'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from '@/api/constants'
import { useTable } from '@/hooks/table'
import type { ConditionsDetailItem } from '@/types'
import { ref } from 'vue'
// import { queryRoleALlList, updateAccountInfo } from '@api/user'

defineOptions({
  name: 'CorpusMappingDetailDialog'
})

interface Props {
  // visible: boolean
  // topicOptions: any[]
  rowData: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

const visible = defineModel('visible', {
  default: false
})

const { rowData } = defineProps<Props>()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const tableData = ref([
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
    field: '情感',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '意图',
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

// 品牌选项
const brandOptions = computed(
  () => conditions.brandCar?.map(item => ({ label: item.value, value: item.key })) || []
)

// 车系选项 - 根据选中的品牌动态更新
const carSeriesOptions = ref<any[]>([])

// 情感选项
const sentimentOptions = computed(
  () => conditions.vocSentiment?.map(item => ({ label: item.value, value: item.key })) || []
)

// 意图选项
const intentionOptions = computed(
  () => conditions.vocIntention?.map(item => ({ label: item.value, value: item.key })) || []
)

// 处理品牌变化
const handleBrandChange = (brandValue: string) => {
  // 清空车系选择
  const carSeriesRow = tableData.value.find(row => row.field === '车系') as any
  if (carSeriesRow) {
    // carSeriesRow.beforeValue = ''
    carSeriesRow.dataValueBefore = ''
  }

  // 更新车系选项
  if (brandValue) {
    const selectedBrand = conditions.brandCar?.find(item => item.key === brandValue)
    carSeriesOptions.value =
      selectedBrand?.children?.map(child => ({
        label: child.value,
        value: child.key
      })) || []
  } else {
    carSeriesOptions.value = []
  }
}

type ErrorType = '1' | '2'

const errorType = ref<ErrorType>('2')

const switchErrorType = (type: ErrorType) => {
  errorType.value = type
}

const { table, form, handleSizeChange, handleCurrentChange, getFirstPageTableData } = useTable(
  {
    method: 'POST',
    url: '/insights/addLabel/queryDataInfo',
    headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
  },
  res => {
    return res.result
  }
)

const detailObj = ref<any>({})
// 获取详情信息
const getDetail = async () => {
  try {
    const res = (await queryCorrectionInfo({
      id: rowData.id
    })) as any
    detailObj.value = res.result || {}
    errorType.value = res.result.errorType
    // const _correctionInfoList = res.result.correctionInfoList
    tableData.value = res.result.correctionInfoList

    // if (_correctionInfoList?.length) {
    //   // 品牌
    //   tableData.value[0].afterValue = _correctionInfoList[0].brandName
    //   // 车系
    //   tableData.value[1].afterValue = _correctionInfoList[0].carSeriesName
    //   // 情感
    //   tableData.value[2].afterValue = _correctionInfoList[0].sentiment
    //   // 意图
    //   tableData.value[3].afterValue = _correctionInfoList[0].intention
    //   // 观点
    //   tableData.value[4].afterValue = _correctionInfoList[0].opinion
    //   // 场景
    //   // tableData.value[5].afterValue = rowData[0].scenario
    // }
    //  else if (rowData?.length > 1) {
    //   // 品牌
    //   tableData.value[0].afterValue = '*'
    //   // 车系
    //   tableData.value[1].afterValue = '*'
    //   // 情感
    //   tableData.value[2].afterValue = '*'
    //   // 意图
    //   tableData.value[3].afterValue = '*'
    //   // 观点
    //   tableData.value[4].afterValue = '*'
    //   // 场景
    //   tableData.value[5].afterValue = '*'
    // }
  } catch {
    detailObj.value = {}
  }
}

const handleOpen = () => {
  console.log('rowData', rowData)
  table.filter.id = rowData.id
  getFirstPageTableData()
  getDetail()
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async (type: number) => {
  // 1通过， 2拒绝， 3撤销
  try {
    const _params = {
      id: rowData.id,
      auditStatus: type
    }
    // 审核
    const res = (await auditLabelCorrection(_params)) as any
    if (res.success) {
      emit('success')
      handleClose()
    } else {
      ElMessage.warning(res.message)
    }
  } catch (error: any) {
    ElMessage.warning(error.message)
  }
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

  .review-setps {
    display: flex;
    align-items: center;

    .rs-dividing-line {
      flex: 1;
      height: 1px;
      background-color: #dfe2e8;
      margin: 0 24px;
    }
    .rs-item {
      display: flex;
      .rs-icon {
        width: 24px;
        height: 24px;
        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }
      .rs-info {
        font-weight: 400;
        font-size: 14px;
        color: #1f2733;
        line-height: 22px;
        .rsi-time {
          font-weight: 400;
          font-size: 12px;
          color: #5f6a7a;
          line-height: 20px;
        }
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
