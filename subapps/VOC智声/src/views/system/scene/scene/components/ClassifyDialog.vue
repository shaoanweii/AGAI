<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑分类' : '新建分类'"
    width="600px"
    style="padding: 0"
    header-class="classify-dialog-form-header-class"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="ml-16 mr-80"
    >
      <el-form-item label="分类名称" prop="name">
        <!-- :disabled="isEdit" -->
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="关联角色" prop="roleIds">
        <el-select
          v-model="formData.roleIds"
          multiple
          collapse-tags
          :max-collapse-tags="3"
          collapse-tags-tooltip
          placeholder="关联角色"
        >
          <el-option
            v-for="item in roleOptions"
            :key="item.id"
            :label="item.roleName"
            :value="item.id"
            :disabled="item.enabled === RoleStatus.DISABLE"
          >
            <div class="flex items-center">
              <span>{{ item.roleName }}</span>
            </div>
          </el-option>
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
import { ref, reactive, watch, computed, nextTick, onMounted, inject } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { insertSpecialType, updateSpecialType } from '@api/system/scene'
import { RoleStatus } from '@/types/system.ts'
import { useUserStore } from '@/store'

interface Props {
  visible: boolean
  classifyData?: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  classifyData: null
})

const emit = defineEmits<Emits>()
const roleOptions = inject('roleOptions') as any[]

// 响应式数据
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const userStore = useUserStore()

enum UseStatus {
  ACTIVE = 1, // 正常
  DISABLED = 0 // 禁用
}

interface RoleOption {
  value: string
  label: string
}

const formData = reactive({
  id: undefined, //分类id
  name: '', //分类名称
  roleIds: [] as RoleOption[], //关联角色
  enabled: UseStatus.ACTIVE, //是否启用
  type: 1
})

// const roleOptions = ref<any>([])

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

// 判断是否为编辑
const isEdit = computed(() => props.classifyData?.id)

// 表单验证规则
const formRules = computed<FormRules>(() => ({
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
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

// onMounted(() => {
//   getRoleList()
// })

// 方法
const initFormData = () => {
  if (isEdit.value) {
    console.log('props.classifyData', props.classifyData)

    Object.assign(formData, {
      ...props.classifyData
      // name: props.classifyData.name,
      // roleIds: props.classifyData.roleIds,
      // id: props.classifyData.id,
      // enabled: props.classifyData.enabled
    })
  } else {
    Object.assign(formData, {})
    // 设置默认角色
    if (userStore.roleId) {
      formData.roleIds.push(userStore.roleId)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true

    //TODO 编辑场景
    const requestParams = {
      ...formData
    }
    let response: any = null
    if (isEdit.value) {
      response = await updateSpecialType(requestParams)
    } else {
      response = await insertSpecialType(requestParams)
    }
    if (response.success && response.result) {
      emit('success')
      dialogVisible.value = false
    } else {
      ElMessage.error(response.message || '新建/修改分类失败')
    }
  } catch (error) {
    console.log('提交表单失败:', error)
    // ElMessage.error(error || '新建/修改分类失败')
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  // 重置表单数据
  Object.assign(formData, {
    id: undefined,
    name: '',
    roleIds: [],
    enabled: UseStatus.ACTIVE,
    type: 1
  })
  emit('update:visible', false)
}
</script>
<style lang="scss">
.classify-dialog-form-header-class {
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
