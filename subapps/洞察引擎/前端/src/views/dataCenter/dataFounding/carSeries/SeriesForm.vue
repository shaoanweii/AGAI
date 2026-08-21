<template>
  <el-drawer v-model="form.visible" :size="1200" @open="handleOpen" destroy-on-close>
    <template #header>
      <h4 class="fw-600">
        {{ form.operation == 'add' ? '新增' : form.operation == 'edit' ? '编辑' : '查看' }}车系
      </h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form.data" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="brandId" label="归属品牌">
            <el-select
              v-model="form.data.brandId"
              placeholder="全部"
              :disabled="form.operation === 'edit'"
              :data-testid="`founding-carseries-series-40001`"
              clearable
              filterable
              popper-class="class-founding-carseries-series-40001"
            >
              <el-option
                v-for="(item, index) in conditions.brand"
                :label="item.value"
                :value="item.code"
                :key="item.key"
                :data-testid="`founding-carseries-series-40001-${index}`"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="name" label="车系名称">
            <el-input
              v-model.trim="form.data.name"
              placeholder="请输入"
              :maxlength="50"
              :data-testid="`founding-carseries-series-40002`"
              clearable
            />
          </el-form-item>
          <el-form-item label="英文名称">
            <el-input
              v-model.trim="form.data.nameEn"
              placeholder="请输入"
              :maxlength="50"
              :data-testid="`founding-carseries-series-40003`"
              clearable
            />
          </el-form-item>
          <el-form-item label="车系别名">
            <el-input
              type="textarea"
              v-model.trim="form.data.alias"
              placeholder="请输入车系别名，多个别名以逗号隔开"
              :data-testid="`founding-carseries-series-40004`"
              clearable
            />
          </el-form-item>
          <el-form-item label="排除词">
            <el-input
              type="textarea"
              v-model.trim="form.data.exclusionWords"
              placeholder="请输入排除词，多个排除词以逗号隔开"
              :data-testid="`founding-carseries-series-40005`"
              clearable
            />
          </el-form-item>
          <el-form-item label="展示图片">
            <el-upload
              action="/api/insights/uploadCarSeries"
              :headers="{ Authorization: token }"
              :on-before-upload="onBeforeUpload"
              :show-file-list="false"
              accept=".jpg, .png"
              :data-testid="`founding-carseries-series-40006`"
              @success="onSuccess"
            >
              <el-button type="primary" v-if="!imgUrl">
                <template #icon>
                  <Plus />
                </template>
                点击上传
              </el-button>
              <el-button type="primary" v-else>
                <template #icon>
                  <Plus />
                </template>
                重新上传
              </el-button>
            </el-upload>
            <div class="upload-tip">支持上传PNG、JPG格式的图片，尺寸小于500*500，大小不超过1M</div>
            <div
              v-if="imgUrl"
              style="border: 1px solid #eee; width: 80px; height: 80px; margin-top: 12px"
            >
              <img :src="imgUrl" width="100%" height="100%" style="object-fit: cover" />
            </div>
          </el-form-item>
          <!--<el-form-item-->
          <!--  prop="carLevel1"-->
          <!--  v-if="!carLevel2Selection.length"-->
          <!--  label="车辆级别"-->
          <!--&gt;-->
          <!--  <div class="flex" style="width: 100%">-->
          <!--    <el-select-->
          <!--      v-model="form.data.carLevel1"-->
          <!--      placeholder="全部"-->
          <!--      clearable-->
          <!--      @change="form.data.carLevel2 = ''"-->
          <!--    >-->
          <!--      <el-option-->
          <!--        v-for="item in carLevel1Selection"-->
          <!--        :label="item.value"-->
          <!--        :value="item.key"-->
          <!--      />-->
          <!--    </el-select>-->
          <!--  </div>-->
          <!--</el-form-item>-->
          <!--v-else-->
          <el-form-item prop="carLevel2" label="车辆级别">
            <div class="flex" style="width: 100%">
              <el-select
                v-model="form.data.carLevel1"
                placeholder="全部"
                clearable
                style="width: 50%"
                :data-testid="`founding-carseries-series-40007`"
                @change="form.data.carLevel2 = ''"
              >
                <el-option
                  v-for="(item, index) in carLevel1Selection"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                  :data-testid="`founding-carseries-series-40007-${index}`"
                />
              </el-select>
              <el-select
                v-model="form.data.carLevel2"
                placeholder="全部"
                clearable
                style="margin-left: 16px; width: 50%"
                :disabled="!carLevel2Selection.length"
                :data-testid="`founding-carseries-series-40008`"
              >
                <el-option
                  v-for="(item, index) in carLevel2Selection"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                  :data-testid="`founding-carseries-series-40008-${index}`"
                />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item prop="energyType1" v-if="!energyType2Selection.length" label="能源类型">
            <div class="flex" style="width: 100%">
              <el-select
                v-model="form.data.energyType1"
                placeholder="全部"
                clearable
                style="width: 50%"
                :data-testid="`founding-carseries-series-40009`"
                @change="form.data.energyType2 = ''"
              >
                <el-option
                  v-for="(item, index) in energyType1Selection"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                  :data-testid="`founding-carseries-series-40009-${index}`"
                />
              </el-select>
              <el-select
                v-model="form.data.energyType2"
                placeholder="全部"
                clearable
                :disabled="!energyType2Selection.length"
                style="margin-left: 16px; width: 50%"
                :data-testid="`founding-carseries-series-40010`"
              >
                <el-option
                  v-for="(item, index) in energyType2Selection"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                  :data-testid="`founding-carseries-series-40010-${index}`"
                />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item prop="energyType2" v-else label="能源类型">
            <div class="flex" style="width: 100%">
              <el-select
                v-model="form.data.energyType1"
                placeholder="全部"
                clearable
                style="width: 50%"
                @change="form.data.energyType2 = ''"
                :data-testid="`founding-carseries-series-40011`"
              >
                <el-option
                  v-for="(item, index) in energyType1Selection"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                  :data-testid="`founding-carseries-series-40011-${index}`"
                />
              </el-select>
              <el-select
                v-model="form.data.energyType2"
                placeholder="全部"
                clearable
                style="margin-left: 16px; width: 50%"
                :data-testid="`founding-carseries-series-40012`"
              >
                <el-option
                  v-for="(item, index) in energyType2Selection"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                  :data-testid="`founding-carseries-series-40012-${index}`"
                />
              </el-select>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style lang="scss" scoped>
.upload-tip {
  font-size: 12px;
  color: var(--color-medium);
  margin-top: 8px;
}
</style>
<script lang="ts" setup>
import { inject } from 'vue'
import type { Form } from '@/hooks/table.d'
import type { SeriesItem } from './type.d'
import { addCarSeriesInfo, updateCarSeriesInfo } from '@/api/dataCenter'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { ConditionsDetailItem } from '@/types'
import { TOKEN_KEY } from '@/constant'

const { filter } = defineProps<{
  filter: Record<any, any> | undefined
}>()
const emit = defineEmits(['refreshCarseriesList'])

const form = inject('form') as Form<SeriesItem>
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const formRef = ref()

const fileList = ref<FileList>()

const rules = {
  brandId: [{ required: true, message: '请输入品牌名称' }],
  name: [{ required: true, message: '请输入车系名称' }],
  carLevel1: [{ required: true, message: '请选择车辆级别' }],
  carLevel2: [{ required: true, message: '请选择车辆级别' }],
  energyType1: [{ required: true, message: '请选择能源类型' }],
  energyType2: [{ required: true, message: '请选择能源类型' }]
}

const carLevel1Selection = computed(() => conditions.carType || [])
const carLevel2Selection = computed(() => {
  let item: any = carLevel1Selection.value.find((it: any) => form.data.carLevel1 == it.key) || {}
  return item?.children || []
})

const energyType1Selection = computed(() => conditions.energy || [])
const energyType2Selection = computed(() => {
  let item: any =
    energyType1Selection.value.find((it: any) => form.data.energyType1 == it.key) || {}
  return item.children || []
})

const onBeforeUpload = (file: any) => {
  const maxSize = 1 * 1024 * 1024
  const maxWidth = 500
  const maxHeight = 500

  return getImgSize(file).then((res: any) => {
    console.log(file)
    const { width, height } = res
    if (width > maxWidth || height > maxHeight) {
      ElMessage.error('图片宽高不能超过500*500')
      return false
    } else if (file.size > maxSize) {
      ElMessage.error('图片大小不能超过1M')
      return false
    } else {
      console.log(fileList.value)

      return true
    }
  })
}

const getImgSize = (file: any) => {
  return new Promise(resolve => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => {
      const img = new Image()
      img.src = reader.result as string
      img.onload = () => {
        console.log(img.width, img.height)

        const width = img.width
        const height = img.height
        resolve({ width, height })
      }
    }
  })
}

// watchEffect(() => {
//   if (form.visible) {
//     getAllSelection();
//   }
// });

const handleCancel = () => {
  form.visible = false
}

const handleOk = async () => {
  try {
    await formRef.value.validate()
    // 验证通过，执行提交逻辑
    if (form.operation == 'add') {
      addCarSeriesInfo(form.data)
        .then(res => {
          console.log(res)
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshCarseriesList()
            form.visible = false
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch(err => {
          ElMessage.error(err.message || '操作失败')
        })
    } else if (form.operation == 'edit') {
      updateCarSeriesInfo(form.data)
        .then((res: any) => {
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshCarseriesList()
            form.visible = false
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch(err => {
          ElMessage.error(err.message || '操作失败')
        })
    }
  } catch (error) {
    // 验证失败，不执行提交逻辑
    console.log('表单验证失败:', error)
  }
}

const refreshCarseriesList = () => {
  emit('refreshCarseriesList')
}

const imgUrl = ref('')
const token = 'Bearer ' + localStorage.getItem(TOKEN_KEY)
const onSuccess = (res: any) => {
  // console.log(res);
  const response = res
  if (response.code === '200') {
    // const _key = response.result?.key?.split('/')
    // if (_key?.length) {
    //   form.data.img = _key[_key.length - 1]
    // } else {
    //   form.data.img = response.result.key
    // }

    form.data.img = response.result.key

    // imgUrl.value = import.meta.env.VITE_DEFAULT_IMAGE_PATH + response.result.key
    imgUrl.value = response.result.url
  }
}

const handleOpen = () => {
  if (form.operation === 'add') {
    form.data.brandId = filter?.brandId
  }
}

watch(
  () => form.visible,
  nv => {
    if (!nv) {
      imgUrl.value = ''
    } else {
      if (form.data.img) {
        // imgUrl.value = import.meta.env.VITE_DEFAULT_IMAGE_PATH + form.data.img
        imgUrl.value = form.data.img
      } else {
        imgUrl.value = ''
      }
    }
  }
)
</script>

<style lang="scss">
.body-wrapper {
  padding: 12px 30px;
  padding-right: 100px;
}
</style>
