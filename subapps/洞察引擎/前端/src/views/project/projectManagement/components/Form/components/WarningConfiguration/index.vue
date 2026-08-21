<script setup lang="ts">
import type { ConditionsDetailItem } from '@/types'
import type { Brand, RiskEarlyWarning } from '@/types/project'
import type { Form } from '@/hooks/table.d'
import { useFormRules } from '@/hooks/useForm'

const { createRequiredRule } = useFormRules()
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const form = inject('form') as Form
// const type = inject('type') as Ref<number>

// const form = reactive<CarInfo[]>([
//   {
//     brandCode: '',
//     brandName: '',
//     carSeriesCode: '',
//     carSeriesName: '',
//     competitiveProduct: [{
//       competitiveBrandCode: '',
//       competitiveBrandName: '',
//       competitiveCarSeriesCode: '',
//       competitiveCarSeriesName: '',
//       core: '',
//     }]
//   }
// ])
let dataForm = defineModel<any>()

let activeBrandConfigurationIndex = ref(0)
// let activeWarningConfigurationIndex = ref(0)

let brandObj = ref<Partial<Brand>>({})
let warningObj = ref<Partial<RiskEarlyWarning>>({})
// let warningObj: Partial<RiskEarlyWarning> = {}
// if (brandObj.value.riskEarlyWarning?.length > 0) {
// }

// brandObj = ref<Brand>(dataForm.value.brand[activeBrandConfigurationIndex.value]) || {}
// console.log(brandObj, 'brandObj')
// warningObj = ref<RiskEarlyWarning>(
//   brandObj.value.riskEarlyWarning[activeWarningConfigurationIndex.value]
// )

// watch(
//   () => type,
//   nv => {
//     // console.log(nv, 'type')
//     if (nv.value === 5) {
//       initializeFormData()
//     }
//   },
//   { deep: true }
// )

// 先定义 handleWarningTypeChange 函数
const handleWarningTypeChange = (value: any) => {
  // 确保品牌对象和预警配置数据存在
  if (
    brandObj.value &&
    brandObj.value.riskEarlyWarning &&
    brandObj.value.riskEarlyWarning.length > 0
  ) {
    const foundWarning = brandObj.value.riskEarlyWarning.find(item => item.warningType === value)

    if (foundWarning) {
      warningObj.value = foundWarning
    } else {
      // 如果没找到对应的预警配置，使用第一个
      warningObj.value = brandObj.value.riskEarlyWarning[0]
    }
  }
}

const handleBrandChange = (value: any) => {
  activeBrandConfigurationIndex.value = value

  // 确保数据存在
  if (dataForm.value && dataForm.value.brand && dataForm.value.brand[value]) {
    brandObj.value = dataForm.value.brand[value]

    // 如果有标签且有预警配置数据，则设置预警类型
    if (brandObj.value.tags && brandObj.value.tags.length > 0) {
      handleWarningTypeChange(brandObj.value.tags[0])
    }
  }
}

const initializeFormData = () => {
  // 确保数据存在且有品牌信息
  if (dataForm.value && dataForm.value.brand && dataForm.value.brand.length > 0) {
    brandObj.value = dataForm.value.brand[activeBrandConfigurationIndex.value]

    // 如果是编辑模式且有预警配置数据
    if (
      form.operation == 'edit' &&
      brandObj.value.riskEarlyWarning &&
      brandObj.value.riskEarlyWarning.length > 0
    ) {
      warningObj.value =
        brandObj.value.riskEarlyWarning.find(
          item => item.warningType === (brandObj.value.tags && brandObj.value.tags[0])
        ) || brandObj.value.riskEarlyWarning[0]
    }

    handleBrandChange(0)
  }
}

// 监听数据变化
watch(
  () => dataForm.value,
  newVal => {
    if (newVal && newVal.brand && newVal.brand.length > 0) {
      initializeFormData()
    }
  },
  { deep: true, immediate: true }
)

onMounted(() => {
  initializeFormData()
})
const rules = {
  tags: [createRequiredRule('标签必填')],
  brand: [
    createRequiredRule('本品必填', {
      validator(value, callback) {
        // console.log(value, 'value')
        if (value.length === 0) {
          callback('本品必填')
        }
      }
    })
  ],
  riskSetting: [
    {
      required: true,
      validator: (rule: any, value: any, callback: (error?: string) => void) => {
        // console.log(value, 'value')
        if (!value || value.length === 0) {
          callback('必填项必填')
        } else {
          callback()
        }
      }
    }
  ]
}

const checkDisabled = (index: number) => {
  // console.log(warningObj.value.warningType)
  // PROD SERVICE  QY
  if (warningObj.value.warningType === 'PROD') {
    if (warningObj.value && warningObj.value.riskSetting) {
      if (
        (!warningObj.value.riskSetting[index].negative &&
          warningObj.value.riskSetting[index].negative !== 0) ||
        (!warningObj.value.riskSetting[index].complaint &&
          warningObj.value.riskSetting[index].complaint !== 0) ||
        (!warningObj.value.riskSetting[index].riskWords &&
          warningObj.value.riskSetting[index].riskWords !== 0)
      ) {
        warningObj.value.riskSetting[index].isApply = false
        return true
      }
    }
  } else if (warningObj.value.warningType === 'SERVICE') {
    if (warningObj.value && warningObj.value.riskSetting) {
      if (
        (!warningObj.value.riskSetting[index].negative &&
          warningObj.value.riskSetting[index].negative !== 0) ||
        (!warningObj.value.riskSetting[index].complaint &&
          warningObj.value.riskSetting[index].complaint !== 0) ||
        (!warningObj.value.riskSetting[index].riskWords &&
          warningObj.value.riskSetting[index].riskWords !== 0)
      ) {
        warningObj.value.riskSetting[index].isApply = false
        return true
      }
    }
  } else if (warningObj.value.warningType === 'QY') {
    if (warningObj.value && warningObj.value.riskSetting) {
      if (
        (!warningObj.value?.riskSetting?.[index].negative &&
          warningObj.value?.riskSetting?.[index].negative !== 0) ||
        (!warningObj.value.riskSetting[index].riskWords &&
          warningObj.value.riskSetting[index].riskWords !== 0)
      ) {
        warningObj.value.riskSetting[index].isApply = false
        return true
      }
    }
  } else if (warningObj.value.warningType === 'CM') {
    if (warningObj.value && warningObj.value.riskSetting) {
      if (
        (!warningObj.value.riskSetting[index].negative &&
          warningObj.value.riskSetting[index].negative !== 0) ||
        (!warningObj.value.riskSetting[index].complaint &&
          warningObj.value.riskSetting[index].complaint !== 0) ||
        (!warningObj.value.riskSetting[index].channelNum &&
          warningObj.value.riskSetting[index].channelNum !== 0)
      ) {
        warningObj.value.riskSetting[index].isApply = false
        return true
      }
    }
  }
}
const checkRiskDisabled = (index: number) => {
  // console.log(warningObj.value.warningType)
  if (warningObj.value.riskLevel && warningObj.value.riskLevel[index]) {
    if (!warningObj.value.riskLevel[index].endValue) {
      warningObj.value.riskLevel[index].isApply = false
      return true
    }
  }
}
</script>

<template>
  <el-form :model="dataForm" :rules="rules" class="w-full">
    <el-form-item prop="brand" label="关联品牌" class="w-full mb-20">
      <div class="radio-wrapper">
        <template v-for="(item, index) in dataForm.brand" :key="item.key">
          <div
            class="radio-item"
            :class="{
              'active-radio': index === activeBrandConfigurationIndex
            }"
            :data-testid="`projectManagement-warningType-1004-${index}`"
            @click="handleBrandChange(index)"
          >
            {{ item.brandName }}
          </div>
        </template>
      </div>
    </el-form-item>
    <el-form-item prop="tags" label="预警类型" validate-trigger="change" class="w-full mb-20">
      <div class="radio-wrapper" v-if="brandObj && conditions.earlyWarningType">
        <template v-for="(item, index) in conditions.earlyWarningType" :key="item.key">
          <div
            class="radio-item"
            :class="{
              'active-radio': warningObj && item.key === warningObj.warningType
            }"
            v-if="
              (brandObj && brandObj.tags && brandObj.tags.includes(item.key)) || item.key === 'CM'
            "
            :data-testid="`projectManagement-warningType-1004-${index}`"
            :value="item.key"
            @click="handleWarningTypeChange(item.key)"
          >
            {{ item.value }}
          </div>
        </template>
      </div>
    </el-form-item>
    <el-form-item prop="riskSetting" label="基础值设置" class="w-full mb-20">
      <!-- :key="activeWarningConfigurationIndex" -->
      <el-form
        v-if="dataForm.brand && warningObj && warningObj.riskSetting"
        :model="warningObj.riskSetting"
        style="margin-top: 5px"
        label-position="top"
        class="w-full"
      >
        <el-row class="w-full">
          <el-col :span="2">
            <el-form-item label="洞察周期">
              <el-space direction="vertical" fill>
                <div
                  v-for="(item, index) in conditions.insightCycle"
                  :key="index"
                  class="staticStr"
                >
                  {{ item.value }}
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="负面观点数">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.insightCycle" :key="index">
                  <span class="preifxSymobl"> ≥ </span>
                  <el-input-number
                    v-model="warningObj.riskSetting[index].negative"
                    :style="{ width: '119px' }"
                    @change="checkDisabled(index)"
                    :min="0"
                  >
                  </el-input-number>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4" v-if="warningObj.warningType !== 'QY'">
            <el-form-item label="投诉观点数">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.insightCycle" :key="index">
                  <span class="preifxSymobl"> ≥ </span>
                  <el-input-number
                    v-model="warningObj.riskSetting[index].complaint"
                    :style="{ width: '119px' }"
                    :min="0"
                  >
                  </el-input-number>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4" v-if="warningObj.warningType === 'CM'">
            <el-form-item label="发声渠道">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.insightCycle" :key="index">
                  <span class="preifxSymobl"> ≥ </span>
                  <el-input-number
                    v-model="warningObj.riskSetting[index].channelNum"
                    :style="{ width: '119px' }"
                    :min="0"
                  >
                  </el-input-number>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4" v-if="warningObj.warningType !== 'CM'">
            <el-form-item label="风险词观点数">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.insightCycle" :key="index">
                  <span class="preifxSymobl"> ≥ </span>
                  <el-input-number
                    v-model="warningObj.riskSetting[index].riskWords"
                    :style="{ width: '119px' }"
                    :min="0"
                  >
                  </el-input-number>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4" v-if="warningObj.warningType === 'CM'">
            <el-form-item label="体验指数">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.insightCycle" :key="index">
                  <span class="preifxSymobl"> ≤ </span>
                  <el-input-number
                    v-model="warningObj.riskSetting[index].affective"
                    :style="{ width: '119px' }"
                    :max="100"
                    :min="-100"
                  >
                  </el-input-number>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="是否应用">
              <el-space direction="vertical" fill>
                <el-checkbox
                  v-model="warningObj.riskSetting[index].isApply"
                  :disabled="checkDisabled(index)"
                  v-for="(item, index) of conditions.insightCycle"
                  :key="index"
                  class="line-base"
                />
              </el-space>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-form-item>
    <el-form-item :rules="[{ required: true }]" label="风险等级定义" class="w-full">
      <el-form
        v-if="warningObj && warningObj.riskLevel"
        :model="warningObj.riskLevel"
        label-position="top"
        style="margin-top: 5px"
        class="w-full"
      >
        <el-row class="w-full">
          <el-col :span="2">
            <el-form-item label="风险等级">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.riskLevel" :key="index" class="staticStr">
                  {{ item.value }}
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="9">
            <el-form-item label="G值范围">
              <el-space direction="vertical" fill>
                <div v-for="(item, index) of conditions.riskLevel" :key="index">
                  <el-input-number
                    v-model="warningObj.riskLevel[index].endValue"
                    :style="{ width: '119px' }"
                    :max="index === 0 ? 100 : warningObj.riskLevel[index - 1].startValue"
                    :min="
                      conditions.riskLevel.length === index + 1
                        ? 0
                        : warningObj.riskLevel[index].startValue
                    "
                  >
                  </el-input-number>
                  <span class="preifxSymobl"> ≥ </span>
                  <span style="display: inline-block; width: 32px; text-align: center">G</span>
                  <!-- <span class="preifxSymobl"> ≥ </span> -->
                  <span class="preifxSymobl"> > </span>
                  <el-input-number
                    v-model="warningObj.riskLevel[index].startValue"
                    :style="{ width: '119px' }"
                    :max="
                      conditions.riskLevel.length === index + 1
                        ? warningObj.riskLevel[index].endValue
                        : warningObj.riskLevel[index].endValue
                    "
                    :min="
                      conditions.riskLevel.length === index + 1
                        ? 0
                        : warningObj.riskLevel[index + 1].endValue
                    "
                  >
                  </el-input-number>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="1">
            <el-form-item label="颜色">
              <el-space direction="vertical" fill>
                <div class="line-base" v-for="(item, index) of conditions.riskLevel" :key="index">
                  <div
                    class="color-item"
                    :style="{
                        backgroundColor: conditions.color&&conditions.color.find(it => it.key === item.key)!.value
                      }"
                  ></div>
                </div>
              </el-space>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="是否应用">
              <el-space direction="vertical" fill>
                <!--  -->
                <template v-for="(item, index) of warningObj.riskLevel" :key="index">
                  <el-checkbox
                    v-model="item.isApply"
                    :disabled="checkRiskDisabled(index)"
                    v-if="index < conditions.riskLevel.length"
                    class="line-base"
                  />
                </template>
              </el-space>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-form-item>
  </el-form>
</template>

<style scoped lang="scss">
.color-item {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 2px solid #f2f3f5;
}
.line-base {
  height: 32px;
  line-height: 32px;
}
.staticStr {
  width: 56px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  background: #f2f3f5;
  border-radius: 2px 2px 2px 2px;
  // border: 1px solid rgba(0, 0, 0, 0.1);
}
.preifxSymobl {
  display: inline-block;
  width: 32px;
  height: 32px;
  text-align: center;
  line-height: 32px;
  background: #f2f3f5;
  border-radius: 2px 0px 0px 2px;
  // border: 1px solid #e5e6eb;
}
.radio-wrapper {
  display: flex;
}
.radio-item {
  width: 88px;
  height: 32px;
  text-align: center;
  line-height: 32px;
  cursor: pointer;
}
.active-radio {
  color: #165dff;
  background: #f2f3f5;
  border-radius: 100px 100px 100px 100px;
}
</style>
