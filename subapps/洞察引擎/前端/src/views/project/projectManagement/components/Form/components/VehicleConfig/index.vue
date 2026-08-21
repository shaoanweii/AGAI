<script setup lang="ts">
import VehicleTrigger from './VehicleTrigger.vue'
import type { Form } from '@/hooks/table.d'
import { findBrandCarSeriesInfo } from '@/api/project'
import type { CarBrand, Brand, CarSeries } from '@/types/project'
import { Close, Plus, Edit, ArrowDown } from '@element-plus/icons-vue'

const form = inject('form') as Form

const type = inject('type') as Ref<number>

const coreOption = ref([
  {
    label: '是',
    value: '1'
  },
  {
    label: '否',
    value: '0'
  }
])

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
const brandForm = defineModel<any>()

let activeBrandConfigurationIndex = ref(0)

let brandObj = ref<Partial<Brand>>({})

const handleBrandChange = (value: any) => {
  // console.log(brandForm.value.brand, value, 'handleBrandChange')
  console.log(value, 'handleBrandChange')
  activeBrandConfigurationIndex.value = value
  brandObj.value = brandForm.value.brand[value]
  // console.log(brandObj, 'activeBrandConfigurationIndex')
}
// if (form.operation == 'add') {
//   brandForm.value = [
//     {
//       brandCode: '',
//       brandName: '',
//       carSeriesCode: '',
//       carSeriesName: '',
//       competitiveProduct: [{
//         competitiveBrandCode: '',
//         competitiveBrandName: '',
//         competitiveCarSeriesCode: '',
//         competitiveCarSeriesName: '',
//         core: '',
//       }]
//     }
//   ]
// }

const handleAdd = () => {
  brandObj.value.carSeries!.push({
    // brandCode: '',
    // brandName: '',
    carSeriesCode: '',
    carSeriesName: '',
    competitiveCarSeries: [
      {
        competitiveBrandCode: '',
        competitiveBrandName: '',
        competitiveCarSeriesCode: '',
        competitiveCarSeriesName: '',
        core: ''
      }
    ]
  })
}
const handleDelete = (index: number) => {
  brandObj.value.carSeries!.splice(index, 1)
}

const handleConfirm = (confirmData: CarSeries, index: number) => {
  // console.log(confirmData, 'confirmData')
  brandObj.value.carSeries![index]!.competitiveCarSeries = confirmData.competitiveCarSeries
  // console.log(brandForm.value, 'confirmData')
}

let brandCarSeriesOption = ref<CarBrand[]>([])

const initializeFormData = async () => {
  brandCarSeriesOption.value = (await findBrandCarSeriesInfo().then(
    res => res.result
  )) as CarBrand[]
  if (form.operation == 'add') {
    brandObj.value.carSeries = []
  }
  handleBrandChange(0)
}

onMounted(() => {
  initializeFormData()
})

watch(
  () => type,
  nv => {
    if (nv.value === 3) {
      initializeFormData()
    }
  },
  { deep: true }
)

const filteredCars = computed(() => {
  if (!brandForm.value) return
  let item = brandCarSeriesOption.value.find(item => item.code === brandObj.value.brandCode)
  // console.log(item, 'filteredCars')
  if (item) {
    return item.cars
  } else {
    return []
  }
})

const competitiveStr = (competitiveArr: any) => {
  // console.log(competitiveArr, 'count')
  if (!competitiveArr.competitiveCarSeries.length) return '竞品品牌丨车系'
  let count = competitiveArr.competitiveCarSeries.length
  let str = ''
  let coreSymbol = ''
  let needStrArr = []
  let plusNum = count > 2 ? `...(+${count - 2})` : ``

  if (count === 0) {
    return str
  } else {
    needStrArr = competitiveArr.competitiveCarSeries.slice(0, 2)
  }
  str = needStrArr
    .map((item: any) => {
      coreSymbol = item.core === '1' ? '*' : ''
      return `${item.competitiveBrandName || '竞品品牌'} | ${
        item.competitiveCarSeriesName || '车系'
      } ${coreSymbol}`
    })
    .join('、')
  // console.log(str, 'str')
  return str + plusNum
}
// let brandCarObj = reactive<any>({})
// const handleChangeBrand = () => {
//   console.log(clickItem, 'item')
//   brandCarObj = brandCarSeriesOption.value.find((item: any) => item.code === clickItem.brandCode)
//   brandForm.value[index].brandName = brandCarObj.name
//   console.log(brandForm, 'brandForm[index]')
// }
const handleChangeCar = (clickItem: any, index: number) => {
  // brandObj.value.carSeries[index].carSeriesName = brandCarObj.cars.find(
  //   (item: any) => item.code === clickItem.carSeriesCode
  // ).name
  if (filteredCars && filteredCars.value) {
    brandObj.value.carSeries![index].carSeriesName = filteredCars?.value.find(
      (item: any) => item.code === clickItem.carSeriesCode
    )!.name
  }
}
</script>

<template>
  <el-form :model="brandObj" auto-label-width>
    <el-form-item :rules="[{ required: true }]" label="关联品牌">
      <div class="radio-wrapper">
        <template v-for="(item, index) in brandForm.brand" :key="item.key">
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
    <el-form-item :rules="[{ required: true }]" label="车系配置">
      <el-form :model="brandObj" layout="vertical" style="margin-top: 5px; width: 100%">
        <el-row :gutter="24">
          <el-col :span="11">
            <el-form-item :content-flex="false" :merge-props="false" label-position="top">
              <template #label>
                <div class="flex" style="width: 100%">
                  <div style="width: 135px">本品车系</div>
                  <div style="width: 120px">核心</div>
                  <div style="width: 120px">停售</div>
                </div>
              </template>
              <el-form-item
                v-for="(item, index) of brandObj.carSeries"
                :prop="`brandCode[${index}]`"
                :key="index"
                class="w-full"
                :style="{ marginTop: index > 0 ? '10px' : '0' }"
              >
                <div class="w-full flex">
                  <el-select
                    v-model="item.carSeriesCode"
                    :data-testid="`vehicle-config-${index}`"
                    placeholder="选择车系"
                    allow-search
                    style="width: 135px"
                    @change="handleChangeCar(item, index)"
                  >
                    <el-option
                      v-for="(cars, index) in filteredCars"
                      :key="index"
                      :data-testid="`vehicle-config-10003-op-${index}`"
                      :label="cars.name"
                      :value="cars.code"
                    />
                  </el-select>
                  <!-- 核心 -->
                  <el-select
                    v-model="item.core"
                    :data-testid="`vehicle-core-${index}`"
                    placeholder="选择核心"
                    allow-search
                    style="width: 120px"
                  >
                    <el-option
                      v-for="(coreItem, index) in coreOption"
                      :key="index"
                      :data-testid="`vehicle-trigger-10133-op-${index}`"
                      :label="coreItem.label"
                      :value="coreItem.value"
                    />
                  </el-select>
                  <!-- 停售 -->
                  <el-select
                    v-model="item.haltSales"
                    :data-testid="`vehicle-haltSales-${index}`"
                    placeholder="选择停售"
                    allow-search
                    style="width: 120px"
                  >
                    <el-option
                      v-for="(coreItem, index) in coreOption"
                      :key="index"
                      :data-testid="`vehicle-trigger-10213-op-${index}`"
                      :label="coreItem.label"
                      :value="coreItem.value"
                    />
                  </el-select>
                  <el-button
                    @click="handleDelete(index)"
                    :style="{ marginLeft: '10px' }"
                    :data-testid="`vehicle-config-delete-${index}`"
                    :icon="Close"
                  ></el-button>
                </div>
              </el-form-item>
            </el-form-item>
          </el-col>
          <el-col :span="13">
            <el-form-item
              label="竞品车系"
              :content-flex="false"
              :merge-props="false"
              label-position="top"
              class="w-full"
            >
              <el-form-item
                v-for="(item, index) of brandObj.carSeries"
                :prop="`competitiveProduct[${index}].value`"
                :key="index"
                class="w-full"
                :style="{ marginTop: index > 0 ? '10px' : '0' }"
              >
                <div class="w-full flex">
                  <el-input
                    disabled
                    :placeholder="competitiveStr(item)"
                    :data-testid="`vehicle-config-competitive-${index}`"
                  >
                    <template #suffix>
                      <el-icon><ArrowDown /></el-icon>
                    </template>
                  </el-input>
                  <VehicleTrigger
                    :brandName="brandObj.brandName!"
                    :brandCode="brandObj.brandCode!"
                    :brandCarSeriesOption="brandCarSeriesOption"
                    :dataIndex="index"
                    :form="item"
                    @handleConfirm="handleConfirm"
                  >
                    <template #btn>
                      <el-button
                        :style="{ marginLeft: '10px' }"
                        :data-testid="`vehicle-config-10004-btn-${index}`"
                      >
                        <template #icon>
                          <Plus
                            v-if="
                              !item.competitiveCarSeries.length ||
                              !item.competitiveCarSeries[0].competitiveCarSeriesName
                            "
                          />
                          <Edit
                            v-if="
                              item.competitiveCarSeries.length &&
                              item.competitiveCarSeries[0].competitiveCarSeriesName
                            "
                          />
                        </template>
                      </el-button>
                    </template>
                  </VehicleTrigger>
                </div>
              </el-form-item>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-form-item>
  </el-form>
  <div>
    <el-button @click="handleAdd" type="text" data-testid="vehicle-config-add-1003"
      >添加车系</el-button
    >
  </div>
</template>

<style scoped lang="scss">
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
