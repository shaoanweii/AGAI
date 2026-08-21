<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑专区' : '新建专区'"
    width="600px"
    style="padding: 0"
    header-class="zone-dialog-form-header-class"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="ml-16 mr-80"
    >
      <el-form-item label="专区名称" prop="name">
        <!-- :disabled="isEdit" -->
        <el-input v-model="formData.name" placeholder="请输入专区名称" />
      </el-form-item>
      <el-form-item label="关联角色" prop="roleIds">
        <el-select
          v-model="formData.roleIds"
          value-key="value"
          multiple
          collapse-tags
          :max-collapse-tags="3"
          collapse-tags-tooltip
          placeholder="关联角色"
        >
          <el-option
            v-for="item in getRoleOptions"
            :key="item.id"
            :label="item.roleName"
            :value="item.id"
          >
            <div class="flex items-center">
              <span>{{ item.roleName }}</span>
            </div>
          </el-option>
          <!-- <template #tag>
            <el-tag
              v-for="tag in formData.roleIds"
              :key="tag.value"
              type="info"
              closable
              color="#F2F4F7"
              @close="removeRole(tag)"
            >
              <span class="text-primary">{{ tag.label }}</span>
            </el-tag>
          </template> -->
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用" prop="enabled">
        <el-radio-group v-model="formData.enabled">
          <el-radio :label="UseStatus.ACTIVE">启用</el-radio>
          <el-radio :label="UseStatus.DISABLED">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer flex-y-center">
        <div class="footer-btn-layout flex-y-center">
          <el-button class="flex-1" @click="handleClose">取消</el-button>
          <el-button class="flex-1" type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, nextTick, inject, type Ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { insertSpecialType, updateSpecialType } from '@/api/system/scene'

interface Props {
  visible: boolean
  zoneData?: any
  curLeftItem: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  zoneData: null,
  curLeftItem: null
})

const emit = defineEmits<Emits>()
const roleOptions = inject('roleOptions') as Ref<any[]>

// 响应式数据
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

enum UseStatus {
  ACTIVE = 1, // 正常
  DISABLED = 0 // 禁用
}

interface RoleOption {
  value: string
  label: string
}

const formData = reactive({
  name: '', //专区名称
  roleIds: [] as RoleOption[], //关联角色
  enabled: UseStatus.ACTIVE, //是否启用
  type: 2
})

const getRoleOptions = computed(() => {
  console.log('roleOptions', roleOptions)

  return roleOptions.value.filter((item: any) => {
    return props.curLeftItem.roleIds.includes(item.id)
  })
})

// const roleOptions = [
//   {
//     value: '1',
//     label: '角色1'
//   },
//   {
//     value: '2',
//     label: '角色2'
//   },
//   {
//     value: '3',
//     label: '角色3'
//   }
// ]

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

// 判断是否为编辑
const isEdit = computed(() => props.zoneData)

// 表单验证规则
const formRules = computed<FormRules>(() => ({
  name: [
    { required: true, message: '请输入专区名称', trigger: 'blur' },
    { min: 2, max: 20, message: '显示名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleIds: [{ required: true, message: '请关联角色', trigger: 'change' }],
  enabled: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}))

// 监听对话框显示状态
watch(
  () => props.visible,
  newVal => {
    if (newVal) {
      nextTick(() => {
        initFormData()
      })
    }
  },
  { immediate: true }
)

// 方法
const initFormData = () => {
  if (isEdit.value) {
    Object.assign(formData, {
      name: props.zoneData.name,
      roleIds: props.zoneData.roleIds,
      enabled: props.zoneData.enabled
    })
  } else {
    Object.assign(formData, {
      name: '',
      roleIds: [],
      enabled: UseStatus.ACTIVE
    })
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true

    // TODO: 实现专区的创建/更新逻辑
    console.log('提交专区数据:', formData)
    const params = {
      id: isEdit.value ? props.zoneData.id : undefined,
      pid: props.curLeftItem.id,
      ...formData
    }

    let response: any = null
    if (isEdit.value) {
      response = await updateSpecialType(params)
    } else {
      response = await insertSpecialType(params)
    }
    if (response.success && response.result) {
      emit('success')
      dialogVisible.value = false
    } else {
      ElMessage.error(response.message || '新建/修改分类失败')
    }
  } catch (error) {
    console.error('提交表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  // 重置表单数据
  Object.assign(formData, {
    name: '',
    roleIds: [],
    enabled: UseStatus.ACTIVE,
    type: 2
  })
  emit('update:visible', false)
}
</script>
<style lang="scss">
.zone-dialog-form-header-class {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #ebf4fd 0%, #ffffff 100%);
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
}
</style>
<style lang="scss" scoped>
.color-grey {
  color: #535862;
}
.role-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.dialog-footer {
  height: 80px;
  border-top: 1px solid #ebedf0;
}

.footer-btn-layout {
  gap: 8px;
  width: 100%;
  padding: 0 40px;
}

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
