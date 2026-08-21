<script setup lang="ts">
import type { ConditionsDetailItem } from '@/types'
import { useFormRules } from '@/hooks/useForm'
import { findDataSourceInfo, findRegionInfo } from '@/api/project'
import useUserStore from '@/stores/modules/user'
import type { Brand, RiskEarlyWarning } from '@/types/project'
import { cloneDeep } from 'lodash-es'
import RegionCascader from './RegionCascader.vue'
import { riskLevelColorMap } from '@/constant'
import to from 'await-to-js'
import { ElMessage } from 'element-plus'

const type = inject('type') as Ref<number>

let userStore = useUserStore()
const { createRequiredRule } = useFormRules()
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

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
const dataForm = defineModel<any>()
// 应用标签
// let needInitTag = ['PROD', 'SERVICE', 'QY']
let needInitTag = conditions.earlyWarningType?.filter(el => el.key !== 'CM')?.map(el => el.key)

let activeBrandConfigurationIndex = ref(0)

let brandObj = ref<Partial<Brand>>({})

const regionList = ref()
/**
 * @description: 根据品牌获取区域
 * @param {*} brandName
 * @return {*}
 */
const getRegionList = async (brandName: string | undefined) => {
  if (!brandName) {
    regionList.value = []
    return
  }
  const [errs, data] = await to(findRegionInfo({ clientId: userStore.clientId, brandName }))
  if (errs) {
    ElMessage.error(errs.message)
  }
  if (data) {
    regionList.value = data.result
  }
}

const handleBrandChange = (value: any) => {
  // if (activeBrandConfigurationIndex.value === value) return
  activeBrandConfigurationIndex.value = value
  brandObj.value = dataForm.value.brand[value]
  // console.log(brandObj, 'activeBrandConfigurationIndex')
  getRegionList(brandObj.value.brandName)
}

let dataSourceOptions = ref<any[]>([])

const handleTagChange = (value: any) => {
  dataForm.value.tags = value
  checkWarning()
  // console.log(dataForm.value.tags, 'handleTagChange')
  if (value.length === 0) {
    dataForm.value.brand[activeBrandConfigurationIndex.value].riskEarlyWarning = []
  } else {
    let filterData = dataForm.value.brand[
      activeBrandConfigurationIndex.value
    ].riskEarlyWarning.filter((item: any) => {
      if (value.includes(item.warningType) || item.warningType === 'CM') return item
    })
    dataForm.value.brand[activeBrandConfigurationIndex.value].riskEarlyWarning = filterData
  }
}
const checkWarning = () => {
  // let default = [
  //   {
  //     periodType: 'd',
  //     negative: 0,
  //     complaint: 0,
  //     riskWords: 0,
  //     channelNum: 0,
  //     affective: 0,
  //     isApply: false
  //   },
  //   {
  //     periodType: 'w',
  //     negative: 0,
  //     complaint: 0,
  //     riskWords: 0,
  //     channelNum: 0,
  //     affective: 0,
  //     isApply: false
  //   },
  //   {
  //     periodType: 'm',
  //     negative: 0,
  //     complaint: 0,
  //     riskWords: 0,
  //     channelNum: 0,
  //     affective: 0,
  //     isApply: false
  //   },
  //   {
  //     periodType: 'q',
  //     negative: 0,
  //     complaint: 0,
  //     riskWords: 0,
  //     channelNum: 0,
  //     affective: 0,
  //     isApply: false
  //   },
  //   {
  //     periodType: 'y',
  //     negative: 0,
  //     complaint: 0,
  //     riskWords: 0,
  //     channelNum: 0,
  //     affective: 0,
  //     isApply: false
  //   }
  // ]

  let riskSetting = conditions.insightCycle?.map(item => {
    return {
      periodType: item.key,
      negative: 0,
      complaint: 0,
      riskWords: 0,
      channelNum: 0,
      affective: 0,
      isApply: false
    }
  })

  let riskLevel = conditions.riskLevel?.map(item => {
    return {
      level: item.key,
      startValue: 0,
      endValue: 0,
      color: riskLevelColorMap[item.key],
      isApply: false
    }
  })

  // [
  //   {
  //     level: 'S',
  //     startValue: 0,
  //     endValue: 0,
  //     color: '#F53F3F',
  //     isApply: false
  //   },
  //   {
  //     level: 'A',
  //     startValue: 0,
  //     endValue: 0,
  //     color: '#F77234',
  //     isApply: false
  //   },
  //   {
  //     level: 'B',
  //     startValue: 0,
  //     endValue: 0,
  //     color: '#F7BA1E',
  //     isApply: false
  //   },
  //   {
  //     level: 'C',
  //     startValue: 0,
  //     endValue: 0,
  //     color: '#007AFF',
  //     isApply: false
  //   },
  //   {
  //     level: 'D',
  //     startValue: 0,
  //     endValue: 0,
  //     color: '#23BCF8',
  //     isApply: false
  //   }
  // ]
  if (dataForm.value.brand.length > 0) {
    if (dataForm.value.brand[0].tags?.length > 0) {
      dataForm.value.brand.forEach((item: Brand) => {
        // console.log(item, 'item')
        let riskEarlyWarning: RiskEarlyWarning[] = []
        // item.tags.forEach((subItem: string) => {
        //   riskEarlyWarning.push({
        //     warningType: cloneDeep(subItem),
        //     riskLevel: cloneDeep(riskLevel),
        //     riskSetting: cloneDeep(riskSetting)
        //   })
        // })

        item.tags.map((tag: string) => {
          // console.log(needInitTag.includes(tag), tag, 'needInitTag.includes(tag)')
          if (needInitTag.includes(tag)) {
            if (
              !item.riskEarlyWarning ||
              item.riskEarlyWarning.every(subItem => {
                return subItem.warningType !== tag
              })
            ) {
              riskEarlyWarning.push({
                warningType: cloneDeep(tag),
                riskLevel: cloneDeep(riskLevel),
                riskSetting: cloneDeep(riskSetting)
              })
            }
          }
        })
        if (item.riskEarlyWarning?.length > 0) {
          item.riskEarlyWarning.push(...riskEarlyWarning)
        } else {
          item.riskEarlyWarning = riskEarlyWarning
        }
        if (
          item.riskEarlyWarning.every(subItem => {
            return subItem.warningType !== 'CM'
          })
        ) {
          item.riskEarlyWarning.push({
            warningType: 'CM',
            riskLevel: cloneDeep(riskLevel),
            riskSetting: cloneDeep(riskSetting)
          })
        }
        // console.log(riskEarlyWarning, 'item.riskEarlyWarning')
      })
      // console.log(dataForm.value.brand.riskEarlyWarning, 'dataForm.value.brand')
      // handleBrandChange(0)
    }
  }
}

const initializeFormData = async () => {
  dataSourceOptions.value = await findDataSourceInfo({ clientId: userStore.clientId }).then(
    (res: any) => res.result || []
  )
  handleBrandChange(0)
}

watch(
  () => type,
  nv => {
    // console.log(nv, 'type')
    if (nv.value === 4) {
      initializeFormData()
    }
  },
  { deep: true }
)
onMounted(() => {
  initializeFormData()
})

const rules = {
  // 移除 tags 验证规则，改为手动控制
}

// 为 brandObj 单独定义验证规则
const brandRules = {
  tags: [
    {
      required: true,
      validator: (rule: any, value: any, callback: (error?: string) => void) => {
        if (!value || value.length === 0) {
          callback('标签必填')
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}
</script>

<template>
  <el-form :model="dataForm" :rules="rules">
    <el-row>
      <el-col :span="24" class="mb-20">
        <el-form-item prop="brand" :rules="[{ required: true }]" label="关联品牌" class="w-full">
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
      </el-col>

      <el-col :span="24" class="mb-20">
        <!-- 使用独立的表单来验证 brandObj -->
        <el-form :model="brandObj" :rules="brandRules">
          <el-form-item prop="tags" label="应用标签" validate-trigger="change" class="w-full">
            <el-space>
              <el-checkbox-group
                v-model="brandObj.tags"
                @change="handleTagChange"
                :data-testid="`projectList-form-1004`"
              >
                <el-checkbox
                  :value="item.key"
                  :key="item.key"
                  v-for="(item, index) in conditions.labelType"
                  :data-testid="`projectList-form-1004-${index}`"
                  >{{ item.value }}
                </el-checkbox>
              </el-checkbox-group>
            </el-space>
          </el-form-item>
        </el-form>
      </el-col>

      <!-- <el-form-item :rules="[{ required: true }]" label="数据源">
        <FCascader
          data-testid="projectList-form-1011"
          v-model="brandObj.dataSource"
          :options="dataSourceOptions"
          :props="{ value: 'id', label: 'dataSourceName', children: 'child' }"
          clearable
          :subLength="-1"
          :max-collapse-tags="2"
          placeholder="请选择"
          multiple
        ></FCascader>
      </el-form-item> -->

      <el-col :span="24" class="mb-20">
        <el-form-item :rules="[{ required: true }]" label="数据渠道" class="w-full">
          <ChannelCascader
            v-model="brandObj.channel"
            :key="brandObj.brandCode"
            multiple
            placeholder="请选择"
            :width="'100%'"
            :subLength="-1"
            :max-collapse-tags="2"
            :fieldNames="{ value: 'code', label: 'name', children: 'child' }"
            :data-testid="`publicDB-20002`"
          ></ChannelCascader>
        </el-form-item>
      </el-col>

      <el-col :span="24">
        <el-form-item :rules="[{ required: true }]" label="区域选择" class="w-full">
          <!-- conditions.province -->
          <FCascader
            :data-testid="`publicDB-20003`"
            v-model="brandObj.region"
            :key="brandObj.brandCode"
            :options="regionList"
            :fieldNames="{ value: 'code', label: 'name', children: 'child' }"
            clearable
            :subLength="-1"
            :max-collapse-tags="2"
            placeholder="请选择"
            multiple
          ></FCascader>
          <!-- <RegionCascader
          v-model="brandObj.region"
          :key="brandObj.brandCode"
          :optionData="regionList"
          placeholder="请选择"
          multiple
          :width="'100%'"
          :data-testid="`publicDB-20003`"
        ></RegionCascader> -->
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<style scoped lang="scss"></style>
