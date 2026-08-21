<script setup lang="ts">
import type { CarBrand, CarSeries } from '@/types/project'
import { Close } from '@element-plus/icons-vue'
// import { ElMessage } from 'element-plus'

const props = defineProps<{
  form: CarSeries
  brandCarSeriesOption: CarBrand[]
  dataIndex: number
  brandName: string
  brandCode: string
}>()

const form = toRefs(props.form)
let competitiveOptions = ref<CarBrand[]>([])
const getCompetitiveOptions = () => {
  let projectIndex = props.brandCarSeriesOption.findIndex(
    (item: CarBrand) => item.code === props.brandCode
  )!
  competitiveOptions.value = props.brandCarSeriesOption
    .slice(0, projectIndex)
    .concat(props.brandCarSeriesOption.slice(projectIndex + 1))
}

const handlePopShow = () => {
  getCompetitiveOptions()
}

const handleAdd = () => {
  form.competitiveCarSeries.value.push({
    competitiveBrandCode: '',
    competitiveBrandName: '',
    competitiveCarSeriesCode: '',
    competitiveCarSeriesName: '',
    core: ''
  })
}
const handleDelete = (index: number) => {
  if (form.competitiveCarSeries.value.length === 1) {
    form.competitiveCarSeries.value = [
      {
        competitiveBrandCode: '',
        competitiveBrandName: '',
        competitiveCarSeriesCode: '',
        competitiveCarSeriesName: '',
        core: ''
      }
    ]
  } else {
    form.competitiveCarSeries.value.splice(index, 1)
  }
}
const popVisible = ref(false)
const handlePopChange = (visible: boolean) => {
  popVisible.value = visible
}
const handleCancel = () => {
  handlePopChange(false)
}

const handleConfirm = () => {
  // console.log(props.form.competitiveCarSeries, form, 'triggle.form.value')
  // emit('handleConfirm', form, props.dataIndex)
  emit('handleConfirm', props.form, props.dataIndex)
  handlePopChange(false)
}

// const checkDuplicate = () => {
//   let arrCode = props.form.competitiveCarSeries.map((item: any) => item.competitiveCarSeriesCode)
//   return props.form.competitiveCarSeries.length === new Set(arrCode).size
// }

let brandCarObj = reactive<any>({})
const handleChangeBrand = (clickItem: any, index: number) => {
  clickItem.competitiveCarSeriesCode = ''
  // console.log(clickItem, 'item', competitiveOptions.value, clickItem.competitiveBrandCode);
  brandCarObj = competitiveOptions.value.find(
    (item: any) => item.code === clickItem.competitiveBrandCode
  )!
  // console.log(brandCarObj, 'brandCarObj.value');

  form.competitiveCarSeries.value[index].competitiveBrandName = brandCarObj.name
  // eslint-disable-next-line
  props.form.competitiveCarSeries[index].competitiveBrandName = brandCarObj.name
}
const filteredCars = (competitiveBrandCode: string) => {
  if (!competitiveBrandCode) return
  let item = competitiveOptions.value.find(item => item.code === competitiveBrandCode)
  if (item) {
    return item.cars
  } else {
    return []
  }
}
const handleChangeCar = (clickItem: any, index: number) => {
  let currentBrandCode = props.form.competitiveCarSeries[index].competitiveBrandCode
  brandCarObj = competitiveOptions.value.find(item => item.code === currentBrandCode)
  // console.log(brandCarObj, 'brandCarObj.value');

  form.competitiveCarSeries.value[index].competitiveCarSeriesName = brandCarObj.cars.find(
    (item: any) => item.code === clickItem.competitiveCarSeriesCode
  ).name
  // eslint-disable-next-line
  props.form.competitiveCarSeries[index].competitiveCarSeriesName = brandCarObj.cars.find(
    (item: any) => item.code === clickItem.competitiveCarSeriesCode
  ).name
}
const emit = defineEmits(['handlePopChange', 'handleConfirm'])
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
</script>
<template>
  <el-popover
    v-model:visible="popVisible"
    trigger="click"
    :teleported="false"
    placement="bottom-start"
    :width="590"
    @show="handlePopShow()"
  >
    <template #reference>
      <slot name="btn"></slot>
    </template>
    <template #default>
      <div class="trigger-form border">
        <div class="form-title">
          <div class="title-wrapper">
            <span class="title-text">编辑竞品车系</span>
            <span class="modal-series">{{ props.brandName }} | {{ props.form.carSeriesName }}</span>
          </div>
          <el-button :icon="Close" @click="handlePopChange(false)"> </el-button>
        </div>
        <div class="form-part">
          <el-form :model="props.form" :style="{ width: '100%' }" layout="vertical">
            <el-row :gutter="8" class="w-full">
              <el-col :span="6">
                <el-form-item
                  label="品牌"
                  :content-flex="false"
                  :merge-props="false"
                  class="w-full"
                >
                  <el-form-item
                    v-for="(item, index) of props.form.competitiveCarSeries"
                    :prop="`posts[${index}].value`"
                    :key="index"
                    class="w-full"
                    :style="{ marginTop: index > 0 ? '10px' : '0' }"
                  >
                    <el-select
                      v-model="item.competitiveBrandCode"
                      :data-testid="`vehicle-trigger-${index}`"
                      placeholder="选择品牌"
                      allow-search
                      @change="handleChangeBrand(item, index)"
                    >
                      <el-option
                        v-for="(item, index) in competitiveOptions"
                        :key="index"
                        :data-testid="`vehicle-trigger-10002-op-${index}`"
                        :label="item.name"
                        :value="item.code"
                      />
                    </el-select>
                  </el-form-item>
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item
                  label="车系"
                  :content-flex="false"
                  :merge-props="false"
                  class="w-full"
                >
                  <el-form-item
                    v-for="(item, index) of props.form.competitiveCarSeries"
                    :prop="`posts[${index}].value`"
                    :key="index"
                    class="w-full"
                    :style="{ marginTop: index > 0 ? '10px' : '0' }"
                  >
                    <el-select
                      v-model="item.competitiveCarSeriesCode"
                      :data-testid="`vehicle-trigger-${index}`"
                      placeholder="选择车系"
                      allow-search
                      @change="handleChangeCar(item, index)"
                    >
                      <el-option
                        v-for="(cars, index) in filteredCars(item.competitiveBrandCode)"
                        :key="index"
                        :data-testid="`vehicle-trigger-10003-op-${index}`"
                        :label="cars.name"
                        :value="cars.code"
                      />
                    </el-select>
                  </el-form-item>
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item
                  label="核心竞品"
                  :content-flex="false"
                  :merge-props="false"
                  class="w-full"
                >
                  <el-form-item
                    v-for="(item, index) of props.form.competitiveCarSeries"
                    :prop="`posts[${index}].value`"
                    :key="index"
                    class="w-full"
                    :style="{ marginTop: index > 0 ? '10px' : '0' }"
                  >
                    <div class="w-full flex">
                      <el-select
                        v-model="item.core"
                        :data-testid="`vehicle-trigger-${index}`"
                        placeholder="否"
                      >
                        <el-option
                          v-for="(coreItem, index) in coreOption"
                          :key="index"
                          :data-testid="`vehicle-trigger-10003-op-${index}`"
                          :label="coreItem.label"
                          :value="coreItem.value"
                        />
                      </el-select>
                      <el-button
                        @click="handleDelete(index)"
                        :style="{ marginLeft: '10px' }"
                        :icon="Close"
                      ></el-button>
                    </div>
                  </el-form-item>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <div>
            <el-button @click="handleAdd" type="text">添加车系</el-button>
          </div>
        </div>
        <div class="trigger-bottom">
          <div class="btn-wrapper">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleConfirm" style="margin-left: 8px"
              >确定</el-button
            >
          </div>
        </div>
      </div>
    </template>
  </el-popover>
</template>
<style lang="scss" scoped>
.trigger-form {
  background-color: #fff;
  box-shadow: 0px 2px 5px 0px rgba(0, 0, 0, 0.1);
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #e5e6eb;

  .form-title,
  .form-part {
    padding: 24px;
  }

  .form-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: linear-gradient(180deg, rgba(22, 93, 255, 0.08) 0%, #ffffff 100%), #ffffff;

    .title-wrapper {
      .title-text {
        font-weight: bold;
        font-size: 16px;
        color: #000000;
      }

      .modal-series {
        background: #f7f8fa;
        border-radius: 2px 2px 2px 2px;
        border: 1px solid #e5e6eb;
        margin-left: 8px;
        padding: 4px 8px;
        font-size: 14px;
        color: #4e5969;
      }
    }
  }
}

.trigger-bottom {
  padding: 12px 40px;
  background: #ffffff;
  box-shadow: 0px -3px 12px 0px rgba(0, 0, 0, 0.1);
  border-radius: 0px 0px 0px 0px;
  display: flex;
  justify-content: flex-end;
}
</style>
