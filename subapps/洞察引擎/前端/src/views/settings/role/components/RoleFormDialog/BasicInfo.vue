<template>
  <el-form
    ref="formRef"
    :model="localFormData"
    :rules="formRules"
    label-width="auto"
    class="pl-16 pt-24 pr-16"
  >
    <el-row>
      <el-col :span="20">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="localFormData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
      </el-col>
      <el-col :span="20">
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="localFormData.remark"
            type="textarea"
            :rows="3"
            :maxlength="30"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-col>
      <el-col :span="20">
        <el-form-item label="是否启用" prop="status">
          <el-radio-group v-model="localFormData.enabled">
            <el-radio :value="RoleStatus.ENABLE">启用</el-radio>
            <el-radio :value="RoleStatus.DISABLE">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
// import { RoleStatus } from '@/types/system'

// 角色状态枚举
enum RoleStatus {
  ENABLE = 1, // 启用
  DISABLE = 0 // 禁用
}

interface InitialData {
  roleName?: string
  remark?: string
  enabled?: RoleStatus
}

const formRef = ref<FormInstance>()

// 内部表单数据
const localFormData = reactive({
  roleName: '',
  remark: '',
  enabled: RoleStatus.ENABLE
})

const formRules = computed<FormRules>(() => ({
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}))

// 初始化数据
const initData = (data: any) => {
  // console.log('data--basic', data)

  Object.assign(localFormData, {
    roleName: data.roleName || '',
    remark: data.remark || '',
    enabled: data.enabled
    // ?? RoleStatus.ENABLE
  })
}

// 验证表单并返回数据
const validateAndGetData = async () => {
  if (!formRef.value) return null
  try {
    await formRef.value.validate()
    return { ...localFormData }
  } catch {
    return null
  }
}

// 重置表单
const resetFields = () => {
  formRef.value?.resetFields()
}

defineExpose({
  initData,
  validateAndGetData,
  resetFields
})
</script>

<style lang="scss" scoped>
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  &:focus-within {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }
}

:deep(.el-textarea__inner) {
  &:focus {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }
}
</style>
