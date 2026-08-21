<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="700px"
    style="padding: 0"
    header-class="user-dialog-form-header-class"
    @close="handleClose"
  >
    <div v-if="isEdit" class="mb-40 ml-36 mr-80">
      <div class="flex-y-center">
        <el-image :src="avatarPng" style="width: 48px; height: 48px" />
        <div class="ml-16">
          <div class="flex-y-center">
            <div class="fs-16 fw-600 text-primary">{{ formData.userName }}</div>
            <div class="fs-16 fw-500 text-primary ml-8">{{ formData.accountName }}</div>
            <div class="ml-8 fs-14 fw-400 color-grey">{{ formData.deptName }}</div>
            <!-- <el-divider direction="vertical" v-if="formData.deptId" />
            <div class="ml-8 fs-14 fw-400 color-grey" v-if="formData.deptId">
              {{ formData?.deptId }}
            </div> -->
          </div>
          <div class="flex-y-center mt-10 fs-14 fw-400 color-grey">
            <template v-if="formData.contact">
              <el-image :src="phoneCallPng" style="width: 16px; height: 16px" />
              <span class="ml-8">{{ formData.contact }}</span>
            </template>
            <!--            <template v-if="formData.phone">
              <el-image class="ml-16" :src="phonePng" style="width: 16px;height: 16px"/>
              <span class="ml-8">{{formData.phone}}</span>
            </template>-->
            <template v-if="formData.email">
              <el-image class="ml-16" :src="mailPng" style="width: 16px; height: 16px" />
              <span class="ml-8">{{ formData.email }}</span>
            </template>
          </div>
        </div>
      </div>
    </div>
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="ml-36 mr-80"
    >
      <el-form-item label="员工账号" prop="accountName">
        <el-input v-model="formData.accountName" placeholder="请输入员工账号" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="角色类型" prop="roleId">
        <!-- multiple -->
        <el-select v-model="formData.roleId" placeholder="请选择角色类型" style="width: 100%">
          <el-option
            v-for="item in roleOptions"
            :label="item.roleName"
            :value="item.id"
            :key="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用" prop="status" v-if="isEdit">
        <el-radio-group v-model="formData.status">
          <!-- <el-radio value="1">启用</el-radio>
          <el-radio value="0">禁用</el-radio> -->
          <el-radio
            v-for="item in conditions.stopOrEnable"
            :key="item.key || ''"
            :value="item.key || ''"
            >{{ item.value }}</el-radio
          >

          <!-- <el-option
              v-for="item in conditions.completionRate"
              :key="item.key || ''"
              :label="item.value || ''"
              :value="item.key || ''"
            /> -->
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
import { ref, reactive, watch, computed, nextTick, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import avatarPng from '@/assets/imgs/avatar.png'
import phoneCallPng from '@/assets/imgs/phone-call.png'
import mailPng from '@/assets/imgs/mail.png'
import { queryRoleALlList, updateAccountInfo } from '@/api/settings'
import type { ConditionsDetailItem } from '@/types'
// import { queryRoleALlList, updateAccountInfo } from '@api/user'

interface Props {
  visible: boolean
  userData?: any | null
  isEdit?: boolean
  roles: any[]
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  userData: null,
  isEdit: false,
  roles: () => []
})

const emit = defineEmits<Emits>()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

// 响应式数据
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const formData = reactive<any>({})

const roleOptions = ref<Record<string, any>[]>()

const getRoleALlList = async () => {
  try {
    roleOptions.value = await queryRoleALlList({
      // clientId: userStore.clientId,
      // checkAdmin: formData.accountType
    }).then(res => res.result)
  } catch (e) {
    roleOptions.value = []
  }
}

// const roles = ref<RoleOption[]>([])

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

// 表单验证规则
const formRules = computed<FormRules>(() => ({
  roleId: [{ required: true, message: '请选择至少一个角色类型', trigger: 'change' }],
  accountName: [{ required: true, message: '请输入员工账号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
}))

onMounted(() => {
  // getRoleList()
})

// 方法
const initFormData = () => {
  if (props.isEdit && props.userData) {
    getRoleALlList()
    Object.assign(formData, {
      ...props.userData
    })
  } else {
    Object.assign(formData, {})
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true
    console.log('formData', formData)

    if (props.isEdit && props.userData && props.userData.userId) {
      // 编辑用户
      const updateData: any = {
        accountName: formData.accountName,
        userId: formData.userId,
        roleId: formData.roleId,
        status: formData.status,
        clientId: '764547797eb2e192763f5334028d49c9'
      }
      const response = (await updateAccountInfo(updateData)) as any
      if (response.success) {
        ElMessage.success('更新成功')
        handleClose()
        emit('success')
      } else {
        ElMessage.error(response.message || '更新失败')
      }
    } else {
      // // 新增用户
      // const createData: CreateUserRequest = {
      //   username: formData.username,
      //   nickname: formData.nickname,
      //   email: formData.email,
      //   phone: formData.phone || undefined,
      //   password: formData.password,
      //   roleIds: formData.roleIds,
      //   remark: formData.remark || undefined
      // }
      // await systemStore.createUser(createData)
    }

    emit('success')
  } catch (error) {
    console.error('提交表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:visible', false)
}

//获取角色类型列表数据
// const getRoleList = async () => {
//   const response = await queryRoleALlList({})
//   if (response.success) {
//     roles.value = response.result || []
//   }
// }

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
</script>

<style lang="scss">
.user-dialog-form-header-class {
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
