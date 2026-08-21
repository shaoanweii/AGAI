<script setup lang="ts">
/**
 * 数据字典项表单弹窗组件
 * 支持新增和编辑字典项
 */

defineOptions({
  name: 'DictItemFormDialog'
})

import { ref, reactive, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { dictItemApi } from '@/api/dictionary'
import type { DictListVo, DictItemListVo, DictItemModel } from '@/api/dictionary/index.d'
import { DictStatus } from '@/api/dictionary/index.d'

// Props
interface Props {
  visible: boolean
  mode: 'add' | 'edit'
  dictInfo: DictListVo
  editData?: DictItemListVo | null
}

const props = withDefaults(defineProps<Props>(), {
  editData: null
})

// Emits
const emit = defineEmits<{
  'update:visible': [visible: boolean]
  confirm: []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 表单数据
const formData = reactive<DictItemModel>({
  dictId: '',
  itemText: '',
  itemKey: '',
  itemValue: '',
  description: '',
  sortOrder: 1,
  status: DictStatus.ENABLED
})

// 状态选项
const statusOptions = [
  { label: '启用', value: DictStatus.ENABLED },
  { label: '停用', value: DictStatus.DISABLED }
]

// 表单验证规则
const formRules: FormRules = {
  itemText: [
    { required: true, message: '请输入字典项名称', trigger: 'blur' },
    { min: 1, max: 50, message: '字典项名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  itemKey: [
    // { required: false, message: '请输入字典项键', trigger: 'blur' },
    { min: 1, max: 50, message: '字典项键长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  itemValue: [
    { required: true, message: '请输入字典项值', trigger: 'blur' },
    { min: 1, max: 100, message: '字典项值长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '排序值范围在 1 到 9999', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '新增字典项' : '编辑字典项'
})

// 重置表单
const resetForm = () => {
  // 清除所有字段，包括id
  delete (formData as any).id
  formData.dictId = props.dictInfo.id
  formData.itemText = ''
  formData.itemKey = ''
  formData.itemValue = ''
  formData.description = ''
  formData.sortOrder = 1
  formData.status = DictStatus.ENABLED
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (data: DictItemListVo) => {
  formData.id = data.id
  formData.dictId = data.dictId
  formData.itemText = data.itemText
  formData.itemKey = data.itemKey
  formData.itemValue = data.itemValue
  formData.description = data.description || ''
  formData.sortOrder = data.sortOrder
  formData.status = data.status as DictStatus
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    let response
    if (props.mode === 'add') {
      // 新增时不发送id字段
      const { id, ...createData } = formData as any
      response = await dictItemApi.createDictItem(createData)
    } else {
      response = await dictItemApi.updateDictItem(formData)
    }

    if (response.success) {
      ElMessage.success(props.mode === 'add' ? '新增成功' : '更新成功')
      emit('update:visible', false)
      emit('confirm')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消操作
const handleCancel = () => {
  emit('update:visible', false)
}

// 监听弹窗显示状态
watch(
  () => props.visible,
  visible => {
    if (visible) {
      nextTick(() => {
        if (props.mode === 'edit' && props.editData) {
          fillFormData(props.editData)
        } else {
          resetForm()
        }
      })
    }
  }
)
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="500px"
    destroy-on-close
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @close="handleCancel"
  >
    <div class="dict-context">
      <p class="context-title">所属字典：{{ dictInfo.dictName }}</p>
      <p class="context-code">编码：{{ dictInfo.dictCode }}</p>
    </div>

    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" size="default">
      <el-form-item label="名称" prop="itemText">
        <el-input v-model="formData.itemText" placeholder="请输入字典项名称" clearable />
      </el-form-item>

      <el-form-item label="数据键" prop="itemKey">
        <el-input v-model="formData.itemKey" placeholder="请输入字典项键" clearable />
      </el-form-item>

      <el-form-item label="数据值" prop="itemValue">
        <el-input v-model="formData.itemValue" placeholder="请输入字典项值" clearable />
        <div class="form-tip">实际存储和传输的值</div>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="排序值" prop="sortOrder">
            <el-input-number
              v-model="formData.sortOrder"
              :min="1"
              :max="9999"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
              <el-option
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入字典项描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.dict-context {
  margin-bottom: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 6px;
  border-left: 3px solid #409eff;

  .context-title {
    margin: 0 0 4px 0;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .context-code {
    margin: 0;
    font-size: 13px;
    color: #909399;
  }
}

.form-tip {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
  line-height: 1.2;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
