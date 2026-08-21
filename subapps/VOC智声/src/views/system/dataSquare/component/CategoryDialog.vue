<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElLoading, ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import uploadCloudIcon from '@/assets/images/upload-cloud.png'
import defaultIcon from '@/assets/images/system/dataSquare/default.png'
import defaultBg from '@/assets/images/system/dataSquare/default-bg.png'
import {
  insertDataPlazaCategory,
  updateDataPlazaCategory,
  uploadDataPlazaImage
} from '@/api/dataPlaza'
import type { DataPlazaCategoryItem } from '@/api/dataPlaza/types'
import { dataSquareStore } from '../store'

defineOptions({
  name: 'CategoryDialog'
})

interface CategoryDialogForm {
  id: string
  categoryName: string
  parentId: string
  brandCode: string
  listIcon: string
  detailImage: string
}

type CategoryDialogMode = 'create' | 'edit'
type UploadField = 'listIcon' | 'detailImage'

const CATEGORY_NAME_MAX_LENGTH = 20
const IMAGE_MAX_SIZE = 5 * 1024 * 1024

const visible = defineModel<boolean>('visible', { default: false })

const props = withDefaults(
  defineProps<{
    mode?: CategoryDialogMode
    currentParentId?: string
    editData?: DataPlazaCategoryItem | null
  }>(),
  {
    mode: 'create',
    currentParentId: '',
    editData: null
  }
)

const emit = defineEmits<{
  (e: 'success', payload: { selectedParentId: string; selectedCategoryId: string }): void
}>()

const formRef = ref()
const submitting = ref(false)
const listIconUploading = ref(false)
const detailImageUploading = ref(false)
const imagePreview = reactive<Record<UploadField, string>>({
  listIcon: '',
  detailImage: ''
})

const form = reactive<CategoryDialogForm>({
  id: '',
  categoryName: '',
  parentId: '',
  brandCode: '',
  listIcon: '',
  detailImage: ''
})

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    {
      max: CATEGORY_NAME_MAX_LENGTH,
      message: `分类名称最多${CATEGORY_NAME_MAX_LENGTH}个字`,
      trigger: 'blur'
    }
  ],
  parentId: [{ required: true, message: '请选择品牌归属', trigger: 'change' }]
}

const brandOptions = computed(() => {
  return (dataSquareStore.categoryTree || []).map(item => ({
    id: item.id,
    label: item.categoryName,
    brandCode: item.brandCode
  }))
})

const dialogTitle = computed(() => {
  return props.mode === 'edit' ? '编辑分类' : '新建分类'
})

const isUploading = computed(() => {
  return listIconUploading.value || detailImageUploading.value
})

/**
 * 根据一级分类 ID 同步品牌编码。
 * @param parentId 一级分类 ID
 */
const syncBrandCodeByParentId = (parentId: string) => {
  const currentBrand = brandOptions.value.find(item => item.id === parentId)
  form.brandCode = currentBrand?.brandCode || ''
}

/**
 * 重置弹框表单。
 */
const resetDialog = () => {
  form.id = props.editData?.id || ''
  form.categoryName = props.editData?.categoryName || ''
  form.parentId =
    props.editData?.parentId || props.currentParentId || brandOptions.value[0]?.id || ''
  syncBrandCodeByParentId(form.parentId)
  form.listIcon = props.editData?.listIcon || ''
  form.detailImage = props.editData?.detailImage || ''
  imagePreview.listIcon = props.editData?.listIconURL || ''
  imagePreview.detailImage = props.editData?.detailImageURL || ''
  formRef.value?.clearValidate?.()
}

/**
 * 校验上传文件格式与大小。
 * @param file 待上传文件
 * @returns 是否允许上传
 */
const validateUploadFile = (file: File, field: UploadField) => {
  const allowTypes = field === 'listIcon' ? ['image/png'] : ['image/png', 'image/jpeg', 'image/jpg']
  const isValidType = allowTypes.includes(file.type)
  if (!isValidType) {
    ElMessage.warning(
      field === 'listIcon' ? '仅支持上传 PNG 格式图片' : '仅支持上传 JPG、PNG 格式图片'
    )
    return false
  }

  if (file.size > IMAGE_MAX_SIZE) {
    ElMessage.warning('图片大小不能超过5MB')
    return false
  }

  return true
}

/**
 * 上传图片并回填字段 URL。
 * @param options 上传参数
 * @param field 目标字段
 */
const handleUpload = async (options: UploadRequestOptions, field: UploadField) => {
  const file = options.file as File
  if (!validateUploadFile(file, field)) return

  const loadingRef = field === 'listIcon' ? listIconUploading : detailImageUploading
  loadingRef.value = true
  const loadingInstance = ElLoading.service({ fullscreen: true })

  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await uploadDataPlazaImage(formData)
    form[field] = response.result?.key || ''
    imagePreview[field] = response.result?.urlSuffix || ''
    ElMessage.success('上传成功')
    options.onSuccess?.(response)
  } catch (error) {
    console.error('上传分类图片失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '上传失败')
    options.onError?.(error as any)
  } finally {
    loadingRef.value = false
    loadingInstance.close()
  }
}

/**
 * 提交新增或编辑分类。
 * @param close 关闭弹框方法
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (submitting.value || isUploading.value) return

  const valid = await formRef.value?.validate?.().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const payload = {
      categoryName: form.categoryName.trim(),
      brandCode: form.brandCode,
      parentId: form.parentId,
      listIcon: form.listIcon,
      detailImage: form.detailImage
    }

    const response =
      props.mode === 'edit' && form.id
        ? await updateDataPlazaCategory({
            id: form.id,
            ...payload
          })
        : await insertDataPlazaCategory(payload)

    if (!response.success) {
      ElMessage.error(response.message || '操作失败')
      return
    }

    ElMessage.success(response.message || '操作成功')
    emit('success', {
      selectedParentId: form.parentId,
      selectedCategoryId: props.mode === 'edit' ? form.id : ''
    })
    close()
  } catch (error) {
    console.error('操作分类失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 预览图统一取表单值，未上传时展示默认图。
 * @param field 字段名
 * @returns 预览地址
 */
const getPreviewSrc = (field: UploadField) => {
  if (field === 'listIcon') {
    return imagePreview.listIcon || defaultIcon
  }
  return imagePreview.detailImage || defaultBg
}

watch(
  visible,
  value => {
    if (value) {
      resetDialog()
    }
  },
  { flush: 'post' }
)

watch(
  () => form.parentId,
  value => {
    syncBrandCodeByParentId(value)
  }
)
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    width="640px"
    destroy-on-close
    :confirm="handleConfirm"
    @close="resetDialog"
  >
    <template #header>{{ dialogTitle }}</template>

    <div class="category-dialog">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="82px"
        class="category-dialog__form"
      >
        <el-form-item label="分类名称" prop="categoryName" required>
          <el-input
            v-model.trim="form.categoryName"
            :maxlength="CATEGORY_NAME_MAX_LENGTH"
            placeholder="请输入"
          />
        </el-form-item>

        <el-form-item label="品牌归属" prop="parentId" required>
          <el-select v-model="form.parentId" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="item in brandOptions"
              :key="item.id"
              :label="item.label"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="列表图标">
          <div class="category-dialog__upload-block">
            <el-upload
              :http-request="options => handleUpload(options, 'listIcon')"
              :show-file-list="false"
              accept=".png,image/png"
              class="category-dialog__upload"
            >
              <div class="upload-card upload-card--action">
                <img class="upload-card__icon" :src="uploadCloudIcon" alt="上传图片" />
                <div class="upload-card__text">上传图片</div>
              </div>
            </el-upload>

            <div class="upload-card upload-card--preview">
              <img
                class="upload-card__image upload-card__image--icon"
                :src="getPreviewSrc('listIcon')"
                alt="列表图标预览"
              />
              <div class="upload-card__text">默认图标</div>
            </div>
          </div>
          <div class="category-dialog__tip">
            默认图标尺寸20*20px,支持上传png格式，最大支持上传5MB
          </div>
        </el-form-item>

        <el-form-item label="详情配图">
          <div class="category-dialog__upload-block">
            <el-upload
              :http-request="options => handleUpload(options, 'detailImage')"
              :show-file-list="false"
              accept=".png,.jpg,.jpeg,image/png,image/jpeg"
              class="category-dialog__upload"
            >
              <div class="upload-card upload-card--action upload-card--detail">
                <img class="upload-card__icon" :src="uploadCloudIcon" alt="上传图片" />
                <div class="upload-card__text">上传图片</div>
              </div>
            </el-upload>

            <div class="upload-card upload-card--preview upload-card--detail">
              <img
                class="upload-card__image upload-card__image--detail"
                :src="getPreviewSrc('detailImage')"
                alt="详情配图预览"
              />
              <div class="upload-card__text upload-card__text--detail">默认配图</div>
            </div>
          </div>
          <div class="category-dialog__tip">
            默认配图尺寸750*200px,支持上传jpg、png格式，最大支持上传5MB
          </div>
        </el-form-item>
      </el-form>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.category-dialog {
  padding: 0 8px;
}

.category-dialog__form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-form-item__content) {
    min-width: 0;
    display: block;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    border-radius: 4px;
  }
}

.category-dialog__upload-block {
  display: flex;
  align-items: stretch;
  gap: 8px;
}

.category-dialog__upload {
  flex: 0 0 auto;

  :deep(.el-upload) {
    display: block;
  }
}

.upload-card {
  width: 120px;
  height: 80px;
  border: 1px solid #e5e6eb;
  background: #f2f4f7;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-card--preview {
  background-color: white !important;
}

.upload-card--action {
  cursor: pointer;
}

.upload-card--detail {
  width: 188px;
  height: 88px;
}

.upload-card__icon {
  width: 20px;
  height: 20px;
  display: block;
}

.upload-card__text {
  margin-top: 10px;
  font-size: 14px;
  color: #4e5969;
  line-height: 22px;
}

.upload-card__image {
  display: block;
  object-fit: cover;
}

.upload-card__image--icon {
  width: 20px;
  height: 20px;
  margin-top: 14px;
}

.upload-card__image--detail {
  width: 100%;
  height: 50px;
}

.upload-card__text--detail {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 0 !important;
}

.category-dialog__tip {
  margin-top: 8px;
  font-weight: 400;
  font-size: 12px;
  color: #86909c;
  line-height: 20px;
}
</style>
