<script setup lang="ts">
import type { CarInfo, CarBrand } from '@/types/project'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

const props = defineProps<{
  form: CarInfo
  brandCarSeriesOption: CarBrand[]
  dataIndex: number
}>()

const form = toRefs(props.form)
let competitiveOptions = ref<CarBrand[]>([])
const getCompetitiveOptions = () => {
  let projectIndex = props.brandCarSeriesOption.findIndex(
    (item: CarBrand) => item.code === props.form.brandCode
  )!
  competitiveOptions.value = props.brandCarSeriesOption
    .slice(0, projectIndex)
    .concat(props.brandCarSeriesOption.slice(projectIndex + 1))
}

const handlePopShow = () => {
  getCompetitiveOptions()
}

const handleAdd = () => {
  form.competitiveProduct.value.push({
    competitiveBrandCode: '',
    competitiveBrandName: '',
    core: ''
  })
}
const handleDelete = (index: number) => {
  if (form.competitiveProduct.value.length === 1) {
    form.competitiveProduct.value = [
      { competitiveBrandCode: '', competitiveBrandName: '', core: '' }
    ]
  } else {
    form.competitiveProduct.value.splice(index, 1)
  }
}
const popVisible = ref(false)
const handlePopChange = (visible: boolean) => {
  if (!checkDuplicate()) {
    return ElMessage.warning('品牌名称重复，请修改后重新提交')
  }
  popVisible.value = visible
}
const handleCancel = () => {
  handlePopChange(false)
}

const handleConfirm = () => {
  // console.log(props.form,form, 'triggle.form.value');
  if (!checkDuplicate()) {
    return ElMessage.warning('品牌名称重复，请修改后重新提交')
  } else {
    emit('handleConfirm', props.form, props.dataIndex)
    handlePopChange(false)
  }
}

const checkDuplicate = () => {
  let arrCode = props.form.competitiveProduct.map((item: any) => item.competitiveBrandCode)
  return props.form.competitiveProduct.length === new Set(arrCode).size
}

let brandCarObj = reactive<any>({})
const handleChangeBrand = (clickItem: any, index: number) => {
  // console.log(clickItem, 'item', competitiveOptions.value, clickItem.competitiveBrandCode);
  brandCarObj = competitiveOptions.value.find(
    (item: any) => item.code === clickItem.competitiveBrandCode
  )!
  // console.log(brandCarObj, 'brandCarObj.value');

  form.competitiveProduct.value[index].competitiveBrandName = brandCarObj.name
  // eslint-disable-next-line
  props.form.competitiveProduct[index].competitiveBrandName = brandCarObj.name
  // console.log(form, 'form[index]');
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
    :width="490"
    @show="handlePopShow()"
  >
    <template #reference>
      <slot name="btn"></slot>
    </template>
    <template #default>
      <div class="trigger-form border">
        <div class="form-title">
          <div class="title-wrapper">
            <span class="title-text">编辑竞品品牌</span>
            <span class="modal-series">{{ props.form.brandName }}</span>
          </div>
          <el-button :icon="Close" @click="handlePopChange(false)"> </el-button>
        </div>
        <div class="form-part">
          <el-form :model="props.form" :style="{ width: '436px' }" layout="vertical">
            <el-row :gutter="8">
              <el-col :span="13">
                <el-form-item
                  label="品牌"
                  :content-flex="false"
                  :merge-props="false"
                  class="w-full"
                >
                  <el-form-item
                    v-for="(item, index) of props.form.competitiveProduct"
                    :prop="`posts[${index}].value`"
                    :key="index"
                    class="w-full"
                    :style="{ marginTop: index > 0 ? '10px' : '0' }"
                  >
                    <el-select
                      v-model="item.competitiveBrandCode"
                      :data-testid="`brand-trigger-${index}`"
                      placeholder="选择品牌"
                      allow-search
                      @change="handleChangeBrand(item, index)"
                    >
                      <el-option
                        v-for="(item, index) in competitiveOptions"
                        :key="index"
                        :data-testid="`brand-trigger-10002-op-${index}`"
                        :label="item.name"
                        :value="item.code"
                      />
                    </el-select>
                  </el-form-item>
                </el-form-item>
              </el-col>
              <el-col :span="11">
                <el-form-item
                  label="核心竞品"
                  :content-flex="false"
                  :merge-props="false"
                  class="w-full"
                >
                  <el-form-item
                    v-for="(item, index) of props.form.competitiveProduct"
                    :prop="`posts[${index}].value`"
                    :key="index"
                    class="w-full"
                    :style="{ marginTop: index > 0 ? '10px' : '0' }"
                  >
                    <div class="w-full flex">
                      <el-select
                        v-model="item.core"
                        :data-testid="`brand-trigger-${index}`"
                        placeholder="否"
                      >
                        <el-option
                          v-for="(coreItem, index) in coreOption"
                          :key="index"
                          :data-testid="`brand-trigger-10003-op-${index}`"
                          :label="coreItem.label"
                          :value="coreItem.value"
                        />
                      </el-select>
                      <el-button
                        @click="handleDelete(index)"
                        :style="{ marginLeft: '10px' }"
                        :icon="Close"
                        :data-testid="`brand-trigger-delete-${index}`"
                      ></el-button>
                    </div>
                  </el-form-item>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <div>
            <el-button @click="handleAdd" type="text" data-testid="brand-trigger-add-1004"
              >添加品牌</el-button
            >
          </div>
        </div>
        <div class="trigger-bottom">
          <div class="btn-wrapper">
            <el-button @click="handleCancel">取消</el-button>
            <el-button
              type="primary"
              @click="handleConfirm"
              style="margin-left: 8px"
              data-testid="brand-trigger-confirm-1006"
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
  width: 490px;

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
