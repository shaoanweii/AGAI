<template>
  <el-dialog
    v-model="visible"
    :title="props.mode === 'edit' ? '编辑车系' : '新增车系'"
    width="1120px"
    style="padding: 0"
    destroy-on-close
    header-class="user-dialog-form-header-class"
    @close="handleClose"
  >
    <div class="dialog-content">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px" @submit.prevent>
        <el-row>
          <el-col :span="20">
            <el-form-item label="车系名称" prop="name">
              <el-input
                v-model.trim="formData.name"
                clearable
                :maxlength="20"
                placeholder="请输入车系名称"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="英文名称" prop="nameEn">
              <el-input
                v-model.trim="formData.nameEn"
                clearable
                :maxlength="20"
                placeholder="请输入英文名称"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="品牌归属" prop="brandCode">
              <el-select-v2
                v-model="formData.brandCode"
                :options="brandOptions"
                :props="{ label: 'label', value: 'value' }"
                filterable
                clearable
                placeholder="请选择品牌归属"
              />
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="车系图片" prop="img">
              <ImageUploadField
                action="/api/insights/uploadCarSeries"
                :token="token"
                :img-url="imgUrl"
                :width="80"
                :height="60"
                :max-size-k-b="50"
                tip="仅支持上传png格式图片（推荐尺寸:80*60，大小不超过50KB）"
                @success="onSuccess"
              />
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="车系别名" prop="alias">
              <el-input
                v-model.trim="formData.alias"
                clearable
                type="textarea"
                placeholder="请输入车系别名，多个别名以逗号隔开"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="排除词" prop="exclusionWords">
              <el-input
                v-model.trim="formData.exclusionWords"
                clearable
                type="textarea"
                placeholder="请输入排除词，多个排除词以逗号隔开"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="是否核心" prop="isCore">
              <el-radio-group v-model="formData.isCore">
                <el-radio
                  v-for="item in props.isCoreOptions || []"
                  :key="item.key"
                  :value="item.key"
                >
                  {{ item.value }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="本竞品类型" prop="competitiveType">
              <el-radio-group v-model="formData.competitiveType">
                <el-radio
                  v-for="item in props.competitiveTypeOptions || []"
                  :key="item.key"
                  :value="item.key"
                >
                  {{ item.value }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col v-if="showCompetitiveRelation" :span="20">
            <el-form-item label="本竞品关系" prop="competitiveProduct">
              <el-select-v2
                v-model="selectedCompetitiveValues"
                :options="competitiveRelationOptions"
                :props="{ label: 'label', value: 'value' }"
                filterable
                clearable
                multiple
                collapse-tags
                placeholder="请选择本竞品关系"
              />
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="是否新车" prop="isNewCar">
              <el-radio-group v-model="formData.isNewCar">
                <el-radio
                  v-for="item in props.isNewCarOptions || []"
                  :key="item.key"
                  :value="item.key"
                >
                  {{ item.value }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="新车时间" prop="preheatStartTime">
              <div class="new-car-time-panel">
                <div class="new-car-time-panel__header">
                  <div
                    v-for="item in newCarStageConfigs"
                    :key="item.label"
                    class="new-car-time-panel__header-item"
                  >
                    {{ item.label }}
                  </div>
                </div>
                <div class="new-car-time-panel__body">
                  <div
                    v-for="item in newCarStageConfigs"
                    :key="item.startKey"
                    class="new-car-time-panel__body-item"
                  >
                    <el-date-picker
                      v-model="formData[item.startKey]"
                      type="date"
                      value-format="YYYY-MM-DD"
                      :disabled="normalizeValue(formData.isNewCar) !== '1'"
                      :placeholder="item.startPlaceholder"
                      @change="handleNewCarTimeChange"
                    />
                    <span class="new-car-time-panel__separator">-</span>
                    <el-date-picker
                      v-model="formData[item.endKey]"
                      type="date"
                      value-format="YYYY-MM-DD"
                      :disabled="normalizeValue(formData.isNewCar) !== '1'"
                      :placeholder="item.endPlaceholder"
                      @change="handleNewCarTimeChange"
                    />
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-form-item label="是否启用" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio
                  v-for="item in props.statusOptions || []"
                  :key="item.key"
                  :value="item.key"
                >
                  {{ item.value }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer flex-y-center">
        <div class="footer-btn-layout flex-y-center">
          <el-button class="flex-1" @click="handleClose">取消</el-button>
          <el-button class="flex-1" type="primary" :loading="confirmLoading" @click="handleConfirm"
            >确定</el-button
          >
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { TOKEN_KEY } from '@/constant'
import ImageUploadField from '../components/ImageUploadField.vue'
import {
  createSeries,
  findBrandByParam,
  findSeriesById,
  findSeriesByParam,
  updateSeries
} from '@/api/brandSeries'
import type { ConditionsDetailItem } from '@/types'

interface SelectOption {
  label: string
  value: string
  id?: string
  code?: string
  name?: string
}

interface Props {
  mode: 'add' | 'edit'
  rowData?: Record<string, any> | null
  isCoreOptions?: ConditionsDetailItem[]
  isNewCarOptions?: ConditionsDetailItem[]
  competitiveTypeOptions?: ConditionsDetailItem[]
  statusOptions?: ConditionsDetailItem[]
}

interface Emits {
  (e: 'success'): void
}

type NewCarStageFieldKey =
  | 'preheatStartTime'
  | 'preheatEndTime'
  | 'launchStartTime'
  | 'launchEndTime'
  | 'stableStartTime'
  | 'stableEndTime'

interface NewCarStageConfig {
  label: string
  startKey: NewCarStageFieldKey
  endKey: NewCarStageFieldKey
  startPlaceholder: string
  endPlaceholder: string
}

const props = withDefaults(defineProps<Props>(), {
  rowData: null
})

const emit = defineEmits<Emits>()

const visible = defineModel<boolean>('visible', {
  default: false
})

const newCarStageConfigs: NewCarStageConfig[] = [
  {
    label: '预热期',
    startKey: 'preheatStartTime',
    endKey: 'preheatEndTime',
    startPlaceholder: '预热开始时间',
    endPlaceholder: '预热结束时间'
  },
  {
    label: '上市期',
    startKey: 'launchStartTime',
    endKey: 'launchEndTime',
    startPlaceholder: '上市开始时间',
    endPlaceholder: '上市结束时间'
  },
  {
    label: '稳定期',
    startKey: 'stableStartTime',
    endKey: 'stableEndTime',
    startPlaceholder: '稳定开始时间',
    endPlaceholder: '稳定结束时间'
  }
]

const newCarStageFieldKeys: NewCarStageFieldKey[] = newCarStageConfigs.flatMap(item => [
  item.startKey,
  item.endKey
])

const formRef = ref<FormInstance>()
// 提交中态用于防止重复点击“确定”导致重复请求。
const confirmLoading = ref(false)
const defaultFormData = (): Api.BrandSeries.Series => ({
  name: '',
  nameEn: '',
  brandCode: '',
  img: '',
  alias: '',
  exclusionWords: '',
  isCore: undefined,
  competitiveType: undefined,
  competitiveProduct: [],
  isNewCar: '1',
  preheatStartTime: '',
  preheatEndTime: '',
  launchStartTime: '',
  launchEndTime: '',
  stableStartTime: '',
  stableEndTime: '',
  status: '1'
})
const formData = ref<Api.BrandSeries.Series>(defaultFormData())

const selectedCompetitiveValues = ref<string[]>([])
const competitiveRelationOptions = ref<SelectOption[]>([])
const brandOptions = ref<SelectOption[]>([])
const isInitializing = ref(false)

const normalizeValue = (value: unknown) =>
  value === undefined || value === null ? '' : String(value)

const toSafeObjectList = <T extends Record<string, any>>(list: unknown): T[] => {
  // 防御接口偶发返回 null/undefined，避免后续 map 时读取属性报错。
  if (!Array.isArray(list)) return []
  return list.filter((item): item is T => !!item && typeof item === 'object')
}

const getCompetitiveTypeKey = (label: string, fallback: string) => {
  const option = (props.competitiveTypeOptions || []).find(item => item.value === label)
  return normalizeValue(option?.key || fallback)
}

const ownTypeKey = computed(() => getCompetitiveTypeKey('本品', '1'))
const competitorTypeKey = computed(() => getCompetitiveTypeKey('竞品', '2'))

// 本竞品关系仅在“本品/竞品”时展示，空值和“非关注范围”都隐藏
const isRelationVisible = (type: unknown) => {
  const currentType = normalizeValue(type)
  return currentType === ownTypeKey.value || currentType === competitorTypeKey.value
}

const getOppositeCompetitiveType = (type: unknown) => {
  const currentType = normalizeValue(type)
  if (currentType === ownTypeKey.value) return competitorTypeKey.value
  if (currentType === competitorTypeKey.value) return ownTypeKey.value
  return ''
}

const showCompetitiveRelation = computed(() => isRelationVisible(formData.value.competitiveType))

const getBrandValue = (item: Api.BrandSeries.Brand) => {
  return item.code || ''
}

const getBrandLabel = (item: Api.BrandSeries.Brand) => {
  return item.name || item.code || ''
}

const brandOptionMap = computed(() => {
  const map = new Map<string, SelectOption>()
  brandOptions.value.forEach(item => {
    if (item.code) {
      map.set(item.code, item)
    }
  })
  return map
})

const brandIdOptionMap = computed(() => {
  const map = new Map<string, SelectOption>()
  brandOptions.value.forEach(item => {
    if (item.id) {
      map.set(item.id, item)
    }
  })
  return map
})

const relationOptionMap = computed(() => {
  const map = new Map<string, Api.BrandSeries.SimpleRef>()
  competitiveRelationOptions.value.forEach(item => {
    // 需要保留后端主键 id，避免新增接口仅提交 code/name 导致关联关系丢失
    map.set(item.value, {
      id: item.id || item.value,
      code: item.code || item.value,
      name: item.name || item.label
    })
  })
  return map
})

/**
 * 统一清空新车阶段时间，避免切换“否”后提交残留脏数据。
 */
const clearNewCarStageTimes = () => {
  newCarStageFieldKeys.forEach(key => {
    formData.value[key] = ''
  })
}

/**
 * 提取新车阶段时间配置与当前值，便于做顺序和重复校验。
 */
const getNewCarStageValues = () => {
  return newCarStageConfigs.map(item => ({
    ...item,
    start: normalizeValue(formData.value[item.startKey]),
    end: normalizeValue(formData.value[item.endKey])
  }))
}

/**
 * 新车时间需要满足分阶段必填、阶段内有序、阶段间不重叠且整体顺序递进。
 */
const validateNewCarStageTime = (_rule: any, _value: any, callback: (error?: Error) => void) => {
  if (normalizeValue(formData.value.isNewCar) !== '1') {
    callback()
    return
  }

  const stageValues = getNewCarStageValues()
  const missingStage = stageValues.find(item => !item.start || !item.end)
  if (missingStage) {
    callback(new Error(`请选择${missingStage.label}`))
    return
  }

  const invalidStage = stageValues.find(item => item.start >= item.end)
  if (invalidStage) {
    callback(new Error(`${invalidStage.label}开始时间必须早于结束时间`))
    return
  }

  const allStageDates = stageValues.flatMap(item => [item.start, item.end])
  if (new Set(allStageDates).size !== allStageDates.length) {
    callback(new Error('预热期、上市期、稳定期日期不允许重复'))
    return
  }

  const [preheatStage, launchStage, stableStage] = stageValues
  if (launchStage.start < preheatStage.end) {
    callback(new Error('上市开始时间不能早于预热结束时间'))
    return
  }
  if (stableStage.start < launchStage.end) {
    callback(new Error('稳定开始时间不能早于上市结束时间'))
    return
  }
  callback()
}

const validateCompetitiveRelation = (
  _rule: any,
  _value: any,
  callback: (error?: Error) => void
) => {
  const currentType = normalizeValue(formData.value.competitiveType)
  if (currentType !== competitorTypeKey.value) {
    callback()
    return
  }
  if (!selectedCompetitiveValues.value.length) {
    callback(new Error('竞品类型下本竞品关系必选'))
    return
  }
  callback()
}

const rules: FormRules = {
  name: [{ required: true, message: '请输入车系名称', trigger: 'blur' }],
  brandCode: [{ required: true, message: '请选择品牌归属', trigger: 'change' }],
  img: [{ required: true, message: '请上传车系图片', trigger: 'change' }],
  isCore: [{ required: true, message: '请选择是否核心', trigger: 'change' }],
  competitiveType: [{ required: true, message: '请选择本竞品类型', trigger: 'change' }],
  competitiveProduct: [{ validator: validateCompetitiveRelation, trigger: 'change' }],
  isNewCar: [{ required: true, message: '请选择是否新车', trigger: 'change' }],
  preheatStartTime: [{ validator: validateNewCarStageTime, trigger: 'change' }],
  status: [{ required: true, message: '请选择启用状态', trigger: 'change' }]
}
const imgUrl = ref('')
const token = 'Bearer ' + localStorage.getItem(TOKEN_KEY)

const mapSeriesData = (data?: Record<string, any> | null): Api.BrandSeries.Series => {
  const mergedData = {
    ...defaultFormData(),
    ...(data || {})
  } as Api.BrandSeries.Series
  // 详情接口返回数值时统一转字符串，保持与字典项 key 类型一致
  ;(mergedData as Record<string, any>).competitiveType = normalizeValue(mergedData.competitiveType)
  ;(mergedData as Record<string, any>).isCore = normalizeValue(mergedData.isCore)
  ;(mergedData as Record<string, any>).isNewCar = normalizeValue(mergedData.isNewCar)
  newCarStageFieldKeys.forEach(key => {
    mergedData[key] = normalizeValue(mergedData[key])
  })
  return mergedData
}

/**
 * 日期选择后主动触发表单校验，保证用户能即时感知阶段时间冲突。
 */
const handleNewCarTimeChange = () => {
  formRef.value?.validateField('preheatStartTime')
}

const syncBrandFields = () => {
  const brandCode = normalizeValue(formData.value.brandCode)
  const brandId = normalizeValue(formData.value.brandId)
  if (brandCode) {
    const matched = brandOptionMap.value.get(brandCode)
    if (matched?.id && matched.code) {
      formData.value.brandId = matched.id
      formData.value.brandCode = matched.code
    }
    return
  }
  if (brandId) {
    const matched = brandIdOptionMap.value.get(brandId)
    if (matched?.id && matched.code) {
      formData.value.brandId = matched.id
      formData.value.brandCode = matched.code
    }
  }
}

const handleClose = () => {
  visible.value = false
}

const loadBrandOptions = async () => {
  const res = await findBrandByParam({ status: '1' } as Api.BrandSeries.Brand)
  const list = toSafeObjectList<Api.BrandSeries.Brand>(res.result)
  brandOptions.value = list
    .map(item => ({
      label: getBrandLabel(item),
      value: getBrandValue(item),
      id: item.id,
      code: item.code,
      name: item.name
    }))
    .filter(item => item.id && item.code)
}

const loadCompetitiveRelationOptions = async () => {
  const currentType = normalizeValue(formData.value.competitiveType)
  if (!isRelationVisible(currentType)) {
    competitiveRelationOptions.value = []
    selectedCompetitiveValues.value = []
    return
  }

  const queryType = getOppositeCompetitiveType(currentType)
  const competitiveType = Number(queryType)
  if (!queryType || Number.isNaN(competitiveType)) {
    competitiveRelationOptions.value = []
    selectedCompetitiveValues.value = []
    return
  }

  const res = await findSeriesByParam({
    status: '1',
    competitiveType
  } as Api.BrandSeries.Series)

  const list = toSafeObjectList<Api.BrandSeries.Series>(res.result)
  const options = list
    .map(item => {
      // 统一以 id 作为内部值，确保编辑回显与提交映射一致
      const value = item.id || item.code || item.name || ''
      const label = item.name || item.code || ''
      return {
        label,
        value,
        id: item.id,
        code: item.code,
        name: item.name
      }
    })
    .filter(item => item.label && item.value)

  competitiveRelationOptions.value = options
  const validValues = new Set(options.map(item => item.value))
  selectedCompetitiveValues.value = selectedCompetitiveValues.value.filter(item =>
    validValues.has(item)
  )
}

const handleConfirm = async () => {
  if (!formRef.value) return
  if (confirmLoading.value) return

  const selectedBrand = brandOptionMap.value.get(normalizeValue(formData.value.brandCode))
  if (!selectedBrand?.id || !selectedBrand.code) {
    ElMessage.error('品牌归属不能为空')
    return
  }

  confirmLoading.value = true
  try {
    formData.value.brandId = selectedBrand.id
    formData.value.brandCode = selectedBrand.code
    formData.value.competitiveProduct = selectedCompetitiveValues.value.map(value => {
      return relationOptionMap.value.get(value) || { id: value, code: value, name: value }
    })
    await formRef.value.validate()
    const payload = { ...formData.value } as Api.BrandSeries.Series & Record<string, any>
    // 车系新车时间已切换为分阶段字段，提交时移除历史兼容字段，避免接口误判。
    delete payload.startTime
    delete payload.endTime
    // 字典 key 为字符串，提交前转成数值以对齐接口字段语义。
    payload.isNewCar = Number(payload.isNewCar)
    if (props.mode === 'edit') {
      await updateSeries(payload)
      ElMessage.success('编辑车系成功')
    } else {
      await createSeries(payload)
      ElMessage.success('新增车系成功')
    }
    emit('success')
    handleClose()
  } finally {
    confirmLoading.value = false
  }
}

const onSuccess = (res: any) => {
  const response = res
  if (response.code === '200') {
    formData.value.img = response.result.key
    imgUrl.value = response.result.url
    formRef.value?.validateField('img')
  }
}

watch(
  () => visible.value,
  async nv => {
    if (nv) {
      isInitializing.value = true
      try {
        await loadBrandOptions()
        if (props.mode === 'edit' && props.rowData?.id) {
          const res = await findSeriesById({ id: props.rowData.id })
          formData.value = mapSeriesData(res.result)
        } else {
          formData.value = mapSeriesData(props.rowData)
        }
        syncBrandFields()
        selectedCompetitiveValues.value = toSafeObjectList<Api.BrandSeries.SimpleRef>(
          formData.value.competitiveProduct
        )
          .map(item => item.id || item.code || item.name || '')
          .filter(Boolean) as string[]
        await loadCompetitiveRelationOptions()
        imgUrl.value = formData.value.img || ''
      } finally {
        isInitializing.value = false
      }
    } else {
      formRef.value?.resetFields()
      formData.value = defaultFormData()
      selectedCompetitiveValues.value = []
      competitiveRelationOptions.value = []
      brandOptions.value = []
      imgUrl.value = ''
      isInitializing.value = false
    }
  }
)

watch(
  () => formData.value.competitiveType,
  async () => {
    // 初始化回填阶段不清空已选项，避免编辑态本竞品关系无法回显
    if (isInitializing.value) return
    selectedCompetitiveValues.value = []
    formData.value.competitiveProduct = []
    await loadCompetitiveRelationOptions()
    formRef.value?.clearValidate('competitiveProduct')
    formRef.value?.validateField('competitiveProduct')
  },
  {
    deep: true
  }
)

watch(
  () => formData.value.isNewCar,
  () => {
    if (normalizeValue(formData.value.isNewCar) !== '1') {
      clearNewCarStageTimes()
    }
    formRef.value?.validateField('preheatStartTime')
  }
)
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

<style scoped lang="scss">
.dialog-content {
  padding: 0 24px;
}

.subtitle {
  font-size: 16px;
  font-weight: 600;
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

.new-car-time-panel {
  width: 100%;
  border: 1px solid #d7e2f0;
  border-radius: 4px;
  overflow-x: auto;
  overflow-y: hidden;
  background: #ffffff;
}

.new-car-time-panel__header,
.new-car-time-panel__body {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  min-width: 930px;
}

.new-car-time-panel__header {
  background: linear-gradient(180deg, #f4f8fe 0%, #eef5ff 100%);
  border-bottom: 1px solid #d7e2f0;
}

.new-car-time-panel__header-item,
.new-car-time-panel__body-item {
  min-width: 0;
}

.new-car-time-panel__header-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  font-weight: 600;
  color: #1f2733;
  border-right: 1px solid #d7e2f0;
}

.new-car-time-panel__body-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  border-right: 1px solid #d7e2f0;
}

.new-car-time-panel__header-item:last-child,
.new-car-time-panel__body-item:last-child {
  border-right: none;
}

.new-car-time-panel__separator {
  flex-shrink: 0;
  color: #86909c;
}

.new-car-time-panel :deep(.el-date-editor.el-input) {
  width: 100%;
  min-width: 132px;
}
</style>
