<template>
  <el-dialog
    v-model="visible"
    :title="props.mode === 'edit' ? '编辑车企' : '新增车企'"
    width="800px"
    style="padding: 0"
    destroy-on-close
    header-class="user-dialog-form-header-class"
    @close="handleClose"
  >
    <div class="dialog-content">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px" @submit.prevent>
        <el-row>
          <el-col :span="20">
            <el-form-item label="车企名称" prop="name">
              <el-input
                v-model.trim="formData.name"
                clearable
                :maxlength="20"
                placeholder="请输入车企名称"
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
            <el-form-item label="车企图片" prop="img">
              <ImageUploadField
                action="/api/insights/uploadAutomark"
                :token="token"
                :img-url="imgUrl"
                :width="28"
                :height="28"
                :max-size-k-b="50"
                tip="仅支持上传png格式图片（推荐尺寸:28*28，大小不超过50KB）"
                @success="onSuccess"
              />
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
  createAutomaker,
  findAutomakerById,
  findAutomakerList,
  updateAutomaker
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
  competitiveTypeOptions?: ConditionsDetailItem[]
  statusOptions?: ConditionsDetailItem[]
}

interface Emits {
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  rowData: null
})

const emit = defineEmits<Emits>()

const visible = defineModel<boolean>('visible', {
  default: false
})

const formRef = ref<FormInstance>()
// 提交中态用于防止重复点击“确定”导致重复请求。
const confirmLoading = ref(false)
const defaultFormData = (): Api.BrandSeries.Automaker => ({
  name: '',
  nameEn: '',
  img: '',
  isCore: undefined,
  competitiveType: undefined,
  competitiveProduct: [],
  status: '1'
})
const formData = ref<Api.BrandSeries.Automaker>(defaultFormData())

const selectedCompetitiveValues = ref<string[]>([])
const competitiveRelationOptions = ref<SelectOption[]>([])
const isInitializing = ref(false)
const imgUrl = ref('')
const token = 'Bearer ' + localStorage.getItem(TOKEN_KEY)

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

const relationOptionMap = computed(() => {
  const map = new Map<string, Api.BrandSeries.SimpleRef>()
  competitiveRelationOptions.value.forEach(item => {
    // 统一按主键提交并保留可读字段，便于后端稳定识别关系
    map.set(item.value, {
      id: item.id || item.value,
      code: item.code || item.value,
      name: item.name || item.label
    })
  })
  return map
})

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
  name: [{ required: true, message: '请输入车企名称', trigger: 'blur' }],
  img: [{ required: true, message: '请上传车企图片', trigger: 'change' }],
  isCore: [{ required: true, message: '请选择是否核心', trigger: 'change' }],
  competitiveType: [{ required: true, message: '请选择本竞品类型', trigger: 'change' }],
  competitiveProduct: [{ validator: validateCompetitiveRelation, trigger: 'change' }],
  status: [{ required: true, message: '请选择启用状态', trigger: 'change' }]
}

const mapAutomakerData = (data?: Record<string, any> | null): Api.BrandSeries.Automaker => {
  const mergedData = {
    ...defaultFormData(),
    ...(data || {})
  } as Api.BrandSeries.Automaker
  ;(mergedData as Record<string, any>).competitiveType = normalizeValue(mergedData.competitiveType)
  ;(mergedData as Record<string, any>).isCore = normalizeValue(mergedData.isCore)
  ;(mergedData as Record<string, any>).status = normalizeValue(mergedData.status || '1')
  return mergedData
}

const handleClose = () => {
  visible.value = false
}

const loadEnabledAutomakerList = async (query: Partial<Api.BrandSeries.Automaker> = {}) => {
  const res = await findAutomakerList({
    status: '1',
    ...query
  } as Api.BrandSeries.Automaker)
  return toSafeObjectList<Api.BrandSeries.Automaker>(res.result)
}

const loadCompetitiveRelationOptions = async () => {
  const currentType = normalizeValue(formData.value.competitiveType)
  if (!isRelationVisible(currentType)) {
    competitiveRelationOptions.value = []
    selectedCompetitiveValues.value = []
    return
  }

  const queryType = getOppositeCompetitiveType(currentType)
  if (!queryType) {
    competitiveRelationOptions.value = []
    selectedCompetitiveValues.value = []
    return
  }

  const queryParams: Api.BrandSeries.Automaker = {
    status: '1'
  }
  const competitiveType = Number(queryType)
  if (!Number.isNaN(competitiveType)) {
    // 切换本竞品类型后实时查询，确保下拉候选与当前类型一致
    queryParams.competitiveType = competitiveType
  }

  const list = await loadEnabledAutomakerList(queryParams)
  const currentId = normalizeValue(formData.value.id)
  const options = list
    .map(item => {
      const value = item.id || item.name || ''
      const label = item.name || ''
      return {
        label,
        value,
        id: item.id,
        name: item.name,
        code: item.id || item.name
      }
    })
    .filter(item => !currentId || normalizeValue(item.id) !== currentId)
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

  confirmLoading.value = true
  try {
    formData.value.competitiveProduct = selectedCompetitiveValues.value.map(value => {
      return relationOptionMap.value.get(value) || { id: value, code: value, name: value }
    })
    await formRef.value.validate()
    const payload = { ...formData.value }
    if (props.mode === 'edit') {
      await updateAutomaker(payload)
      ElMessage.success('编辑车企成功')
    } else {
      await createAutomaker(payload)
      ElMessage.success('新增车企成功')
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
        if (props.mode === 'edit' && props.rowData?.id) {
          const res = await findAutomakerById({ id: props.rowData.id })
          formData.value = mapAutomakerData(res.result)
        } else {
          formData.value = mapAutomakerData(props.rowData)
        }
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
      imgUrl.value = ''
      isInitializing.value = false
    }
  }
)

watch(
  () => formData.value.competitiveType,
  async () => {
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
</style>
