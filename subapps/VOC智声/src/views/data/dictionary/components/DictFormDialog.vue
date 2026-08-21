<script setup lang="ts">
/**
 * 数据字典类型表单弹窗组件
 * 支持新增和编辑字典类型
 */

defineOptions({
  name: 'DictFormDialog'
})

import { ref, reactive, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { dictApi } from '@/api/dictionary'
import type { DictListVo, DictTypeModel } from '@/api/dictionary/index.d'
import { DictType } from '@/api/dictionary/index.d'

// Props
interface Props {
  visible: boolean
  mode: 'add' | 'edit'
  editData?: DictListVo | null
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
const formData = reactive<DictTypeModel>({
  dictName: '',
  dictCode: '',
  type: DictType.STRING,
  description: ''
})

// 数据类型选项
const typeOptions = [
  { label: '字符串', value: DictType.STRING },
  { label: '数字', value: DictType.NUMBER },
  { label: '布尔值', value: DictType.BOOLEAN }
]

// 表单验证规则
const formRules: FormRules = {
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 50, message: '字典名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { min: 2, max: 50, message: '字典编码长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '字典编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择字典类型', trigger: 'change' }]
}

// 计算属性
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '新增字典' : '编辑字典'
})

// 重置表单
const resetForm = () => {
  // 清除所有字段，包括id
  delete (formData as any).id
  formData.dictName = ''
  formData.dictCode = ''
  formData.type = DictType.STRING
  formData.description = ''
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (data: DictListVo) => {
  formData.id = data.id
  formData.dictName = data.dictName
  formData.dictCode = data.dictCode
  formData.type = data.type as DictType
  formData.description = data.description || ''
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
      response = await dictApi.createDict(createData)
    } else {
      response = await dictApi.updateDict(formData)
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
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px" size="default">
      <el-form-item label="字典名称" prop="dictName">
        <el-input v-model="formData.dictName" placeholder="请输入字典名称" clearable />
      </el-form-item>

      <el-form-item label="字典编码" prop="dictCode">
        <el-input
          v-model="formData.dictCode"
          placeholder="请输入字典编码"
          clearable
          :disabled="mode === 'edit'"
        />
        <div class="form-tip">
          {{ mode === 'edit' ? '编辑时不可修改字典编码' : '只能包含字母、数字和下划线' }}
        </div>
      </el-form-item>

      <el-form-item label="字典类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择字典类型" style="width: 100%">
          <el-option
            v-for="option in typeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入字典描述"
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
