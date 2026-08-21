<script setup lang="ts">
import BrandTrigger from './BrandTrigger.vue'
import type { Form } from '@/hooks/table.d'
import { findBrandCarSeriesInfo } from '@/api/project'
import type { CarInfo, CarBrand } from '@/types/project'
import { cloneDeep } from 'lodash-es'
import { Close, Plus, Edit } from '@element-plus/icons-vue'
const form = inject('form') as Form

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
// if (form.operation == 'add') {
//   brandForm.value = [
//     {
//       brandCode: '',
//       brandName: '',
//       carSeriesCode: '',
//       carSeriesName: '',
//       competitiveProduct: [],
//     },
//   ]
// }
const EMPTY_BRAND_OBJECT = {
  brandCode: '',
  brandName: '',
  // carSeriesCode: '',
  // carSeriesName: '',
  competitiveProduct: [
    {
      competitiveBrandCode: '',
      competitiveBrandName: '',
      // competitiveCarSeriesCode: '',
      // competitiveCarSeriesName: '',
      core: ''
    }
  ],
  carSeries: [],
  channel: [],
  region: [],
  tags: [],
  dataSource: []
}
const handleAdd = () => {
  brandForm.value.brand.push(cloneDeep(EMPTY_BRAND_OBJECT))
}
const handleDelete = (index: number) => {
  brandForm.value.brand.splice(index, 1)
}

const handleConfirm = (confirmData: CarInfo, index: number) => {
  // console.log(confirmData, 'confirmData')
  brandForm.value.brand[index].competitiveProduct = confirmData.competitiveProduct
  // console.log(brandForm.value, 'confirmData')
}

let brandCarSeriesOption = ref<CarBrand[]>([])

const initializeFormData = () => {
  if (form.operation == 'add') {
    brandForm.value.brand = []
  }
}
const getCarSeries = async () => {
  brandCarSeriesOption.value = (await findBrandCarSeriesInfo().then(
    res => res.result
  )) as CarBrand[]
}
getCarSeries()

onMounted(() => {
  initializeFormData()
})

const competitiveStr = (competitiveArr: any) => {
  if (!competitiveArr.competitiveProduct.length) return '竞品品牌'
  let count = competitiveArr.competitiveProduct.length
  let str = ''
  let coreSymbol = ''
  let needStrArr = []
  let plusNum = count > 2 ? `...(+${count - 2})` : ``

  if (count === 0) {
    return str
  } else {
    needStrArr = competitiveArr.competitiveProduct.slice(0, 2)
  }
  str = needStrArr
    .map((item: any) => {
      coreSymbol = item.core === '1' ? '*' : ''
      return `${item.competitiveBrandName || '竞品品牌'} ${coreSymbol}`
    })
    .join('、')
  return str + plusNum
}
let brandCarObj = reactive<any>({})
const handleChangeBrand = (clickItem: any, index: number) => {
  brandCarObj = brandCarSeriesOption.value.find((item: any) => item.code === clickItem.brandCode)
  brandForm.value.brand.splice(index, 1, cloneDeep(EMPTY_BRAND_OBJECT))
  brandForm.value.brand[index].brandName = brandCarObj.name
  brandForm.value.brand[index].brandCode = brandCarObj.code
}
</script>

<template>
  <el-form ref="brandConfigFormRef" :model="brandForm" layout="vertical">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-form-item
          label="本品品牌"
          prop="brand"
          :rules="[{ required: true }]"
          :content-flex="false"
          :merge-props="false"
        >
          <el-form-item
            v-for="(item, index) of brandForm.brand"
            :prop="`brandCode[${index}]`"
            :key="index"
            :hide-asterisk="true"
            class="w-full"
            :style="{ marginTop: Number(index) > 0 ? '10px' : '0' }"
          >
            <div class="flex w-full">
              <el-select
                v-model="item.brandCode"
                :data-testid="`brand-config-competitive-brand-${index}`"
                placeholder="选择品牌"
                allow-search
                @change="handleChangeBrand(item, Number(index))"
              >
                <el-option
                  v-for="(item, index) in brandCarSeriesOption"
                  :key="index"
                  :data-testid="`brand-config-competitive-brand-10002-op-${index}`"
                  :label="item.name"
                  :value="item.code"
                />
              </el-select>
              <el-button
                @click="handleDelete(Number(index))"
                :style="{ marginLeft: '10px' }"
                :icon="Close"
                :data-testid="`brand-config-competitive-brand-10003-delete-${index}`"
              ></el-button>
            </div>
          </el-form-item>
        </el-form-item>
      </el-col>
      <el-col :span="16">
        <el-form-item label="竞品品牌" :content-flex="false" :merge-props="false" class="w-full">
          <el-form-item
            v-for="(item, index) of brandForm.brand"
            :prop="`competitiveProduct[${index}].value`"
            :key="index"
            class="w-full"
            :style="{ marginTop: Number(index) > 0 ? '10px' : '0' }"
          >
            <div class="w-full flex">
              <el-input
                disabled
                :placeholder="competitiveStr(item)"
                :data-testid="`brand-config-competitive-brand-10004-${index}`"
              >
                <template #suffix>
                  <icon-down />
                </template>
              </el-input>
              <BrandTrigger
                :form="item"
                :brandCarSeriesOption="brandCarSeriesOption"
                :dataIndex="Number(index)"
                @handleConfirm="handleConfirm"
              >
                <template #btn>
                  <el-button
                    :style="{ marginLeft: '10px' }"
                    :data-testid="`brand-config-competitive-brand-10004-btn-${index}`"
                    :disabled="!item.brandCode"
                  >
                    <template #icon>
                      <Plus
                        v-if="
                          !item.competitiveProduct.length ||
                          !item.competitiveProduct[0].competitiveBrandCode
                        "
                      />
                      <Edit
                        v-if="
                          item.competitiveProduct.length &&
                          item.competitiveProduct[0].competitiveBrandCode
                        "
                      />
                    </template>
                  </el-button>
                </template>
              </BrandTrigger>
            </div>
          </el-form-item>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
  <div>
    <el-button
      @click="handleAdd"
      type="text"
      :data-testid="`brand-config-competitive-brand-add-1005`"
      >添加品牌</el-button
    >
  </div>
</template>

<style scoped lang="scss"></style>
