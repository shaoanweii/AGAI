<template>
  <el-drawer v-model="form.visible" :size="1200" destroy-on-close>
    <template #header>
      <h4 class="fw-600">
        {{ form.operation == 'add' ? '新增' : form.operation == 'edit' ? '编辑' : '查看' }}品牌
      </h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form.data" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="name" label="品牌名称">
            <el-input
              v-model.trim="form.data.name"
              :data-testid="`founding-carseries-brand-30001`"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
          <el-form-item label="英文名称">
            <el-input
              v-model.trim="form.data.nameEn"
              :data-testid="`founding-carseries-brand-30002`"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
          <el-form-item label="品牌别名">
            <el-input
              type="textarea"
              v-model.trim="form.data.alias"
              :data-testid="`founding-carseries-brand-30003`"
              placeholder="请输入车系别名，多个别名以逗号隔开"
              clearable
            />
          </el-form-item>
          <el-form-item label="排除词">
            <el-input
              type="textarea"
              v-model.trim="form.data.exclusionWords"
              :data-testid="`founding-carseries-brand-30004`"
              placeholder="请输入排除词，多个排除词以逗号隔开"
              clearable
            />
          </el-form-item>
          <el-form-item label="展示图片">
            <el-upload
              action="/api/insights/uploadBrand"
              :headers="{ Authorization: token }"
              :on-before-upload="onBeforeUpload"
              :show-file-list="false"
              accept=".jpg, .png"
              :data-testid="`founding-carseries-brand-30005`"
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
import type { ListItem } from './type.d'
import { addBrandInfo, updateBrandInfo } from '@/api/dataCenter'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { TOKEN_KEY } from '@/constant'

const emit = defineEmits(['refreshList'])

const token = 'Bearer ' + localStorage.getItem(TOKEN_KEY)

const formRef = ref()
const imgUrl = ref('')

const form = inject('form') as Form<ListItem>
console.log(form)

const rules = {
  name: [{ required: true, message: '请输入品牌名称' }]
}

const handleCancel = () => {
  form.visible = false
}

const handleOk = async () => {
  try {
    await formRef.value.validate()
    // 验证通过，执行提交逻辑
    if (form.operation == 'add') {
      addBrandInfo(form.data)
        .then(res => {
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshList()
            form.visible = false
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch(err => {
          ElMessage.error(err.message || '操作失败')
        })
    } else if (form.operation == 'edit') {
      updateBrandInfo(form.data)
        .then((res: any) => {
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshList()
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

const onBeforeUpload = (file: any) => {
  const maxSize = 1 * 1024 * 1024
  const maxWidth = 500
  const maxHeight = 500

  return getImgSize(file).then((res: any) => {
    const { width, height } = res
    if (width > maxWidth || height > maxHeight) {
      ElMessage.error('图片宽高不能超过500*500')
      return false
    } else if (file.size > maxSize) {
      ElMessage.error('图片大小不能超过1M')
      return false
    } else {
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

const onSuccess = (res: any) => {
  console.log(res)
  const response = res
  if (response.code === '200') {
    form.data.img = response.result.key
    // imgUrl.value = 'http://172.16.80.16:30125/voc-cloud/' + response.result.key
    // imgUrl.value = response.result.url.split('?')[0]
    // imgUrl.value = import.meta.env.VITE_DEFAULT_IMAGE_PATH + response.result.key
    imgUrl.value = response.result.url
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

const refreshList = () => {
  emit('refreshList')
}
</script>

<style lang="scss">
.body-wrapper {
  padding: 12px 30px;
  padding-right: 100px;
}
</style>
