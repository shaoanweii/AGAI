<template>
  <el-drawer
    :size="1200"
    v-model="form.visible"
    @open="handleOpen"
    @close="handleClose"
    destroy-on-close
    :data-testid="`account-form-drawer`"
  >
    <template #header>
      <div>
        {{ titleStr }}
      </div>
    </template>
    <el-form ref="formRef" :model="formData" :rules="rules" :style="{ width: '800px' }">
      <el-form-item prop="roleId" label="角色类型" required>
        <el-select
          v-model="formData.roleId"
          :data-testid="`account-form-10001`"
          placeholder="请选择"
          :disabled="disabled"
          clearable
        >
          <el-option
            v-for="(item, index) in roleOptions"
            :data-testid="`account-form-10001-op-${index}`"
            :label="item.roleName"
            :value="item.id"
            :key="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item prop="accountName" label="账号名称" required>
        <FtInput
          v-model.allTrim="formData.accountName"
          :data-testid="`account-form-10002`"
          placeholder="请输入"
          clearable
          :disabled="form.operation !== 'add'"
        >
          <template #prepend>
            <span :data-testid="`account-form-10002-1`">{{ `${prependCustomer}_` }}</span>
          </template>
        </FtInput>
        <!--<el-input v-model="formData.accountName" placeholder="请输入" :disabled="disabled">-->
        <!--  <template #prepend>-->
        <!--    <span>{{ `${prependCustomer}_` }}</span>-->
        <!--  </template>-->
        <!--</el-input>-->
      </el-form-item>
      <template v-if="form.operation === 'add'">
        <el-form-item prop="accountPwd" label="账号密码">
          <!--<el-input v-model="formData.accountPwd" placeholder="请输入"/>-->
          <FtInput
            v-model.allTrim="formData.accountPwd"
            :data-testid="`account-form-10003`"
            placeholder="请输入"
            clearable
          />
        </el-form-item>
      </template>
      <template v-if="form.operation === 'edit'">
        <el-form-item v-if="isEditPwd" prop="accountPwd" label="账号密码">
          <!--<el-input v-model="formData.accountPwd" placeholder="请输入"/>-->
          <div class="flex w-full">
            <div class="flex-1">
              <FtInput
                v-model.allTrim="formData.accountPwd"
                :data-testid="`account-form-10004`"
                placeholder="请输入"
                clearable
              />
            </div>

            <el-button class="ml-16" @click="handleCancelPwd" :data-testid="`account-form-10004-1`">
              <span style="width: 56px; text-align: center">取消</span>
            </el-button>
          </div>
        </el-form-item>
        <el-form-item v-else label="账号密码">
          <div class="flex">
            <div class="pwd-default" :data-testid="`account-form-10004-2`">
              <div v-for="item of 8" :key="item" class="pwdd-circle"></div>
            </div>
            <el-button
              :data-testid="`account-form-10004-3`"
              type="primary"
              class="ml-16"
              @click="handleEditPwd"
              >修改密码
            </el-button>
          </div>
        </el-form-item>
      </template>

      <el-form-item prop="userName" label="用户名">
        <el-input
          v-model.trim="formData.userName"
          :data-testid="`account-form-10005`"
          clearable
          placeholder="请输入"
        />
      </el-form-item>
      <el-form-item prop="contact" label="联系电话">
        <el-input
          v-model.trim="formData.contact"
          :data-testid="`account-form-10006`"
          clearable
          placeholder="请输入"
        />
      </el-form-item>
      <el-form-item prop="status" label="启用状态" required>
        <el-radio-group
          v-model="formData.status"
          :disabled="disabled"
          :data-testid="`account-form-10007`"
        >
          <el-radio
            v-for="(item, index) of conditions.stopOrEnable"
            :key="index"
            :data-testid="`account-form-10007-op-${index}`"
            :value="item.key"
            >{{ item.value }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="expiryDateBoolean" label="有效期" required>
        <el-radio-group
          v-model="formData.expiryDateBoolean"
          :data-testid="`account-form-10008`"
          :disabled="disabled"
          @change="expiryDateChange"
        >
          <el-radio :value="1" :data-testid="`account-form-10008-op-1`">
            截止日期
            <el-date-picker
              v-model="formData.expiryDate"
              :disabled="formData.expiryDateBoolean !== 1 || disabled"
              :data-testid="`account-form-10008-1`"
            />
          </el-radio>
          <el-radio :value="0" :data-testid="`account-form-10008-op-2`">长期</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script setup lang="ts">
import type { Form } from '@/hooks/table.d'
import { ElMessage } from 'element-plus'
import { saveAccountInfo, updateAccountInfo, queryRoleALlList } from '@/api/settings'
import { debounce } from 'lodash-es'
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import { useFormRules } from '@/hooks/useForm'
import useUserStore from '@/stores/modules/user'
import { resetObjectValues } from '@/utils'
import FtInput from '@/components/FtInput.vue'

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshList'])
const form = inject('form') as Form
let formData = reactive<Record<any, any>>({})
let titleStr = ref('')
let prependCustomer = ref('')
let userStore = useUserStore()

const disabled = computed(() => {
  // formData.accountType true 管理员
  return form.operation === 'edit' && formData.accountType
})

const isEditPwd = ref(false)

const handleCancelPwd = () => {
  isEditPwd.value = false
  formRef.value?.clearValidate('accountPwd')
}
const handleEditPwd = () => {
  isEditPwd.value = true
}

const { createStrLengthRule, createRequiredRule } = useFormRules()

const rules = {
  roleId: [createRequiredRule('角色类型必填')],
  accountName: [createStrLengthRule('账号名称', 3, 16)],
  accountPwd: [createStrLengthRule('账号密码', 8, 16)],
  // userName: [createStrLengthRule('用户名', 2, 16)],
  userName: [
    {
      required: false,
      validator: (rule: any, value: any, callback: (error?: string | Error) => void) => {
        if (value && value.trim()) {
          const strValue = value.toString()
          if (strValue.length < 2 || strValue.length > 16) {
            callback(new Error('用户名字符限制2-16个字符'))
            return
          }
        }
        callback()
      }
    }
  ],
  contact: [
    {
      required: false,
      validator: (rule: any, value: any, callback: (error?: string | Error) => void) => {
        if (value && value.trim()) {
          const strValue = value.toString()
          if (strValue.length < 5 || strValue.length > 16) {
            callback(new Error('联系电话字符限制5-16个字符'))
            return
          }
        }
        callback()
      }
    }
  ],
  status: [createRequiredRule('启用状态必填')],
  expiryDateBoolean: [
    createRequiredRule('', {
      validator: (rule: any, value: any, callback: (error?: string | Error) => void) => {
        if (formData.expiryDateBoolean === 1 && !formData.expiryDate) {
          callback(new Error('有效期必填'))
        } else {
          callback() // 验证通过时必须调用 callback()
        }
      }
    })
  ]
}

watch(
  () => form.visible,
  (nv, ov) => {
    if (nv && !ov) {
      initializeFormData()
    }
  }
)
const roleOptions = ref<Record<string, any>[]>()

const getRoleALlList = async () => {
  try {
    roleOptions.value = await queryRoleALlList({
      clientId: userStore.clientId,
      checkAdmin: formData.accountType
    }).then(res => res.result)
  } catch (e) {
    roleOptions.value = []
  }
}

const getPrependCustomer = async () => {
  // prependCustomer.value = await queryCodeById({clientId: userStore.clientId}).then(res => res.result)
  prependCustomer.value = userStore.getClientCodeByClientId(userStore.clientId)?.code || ''
}
const handleOpen = async () => {
  await getRoleALlList()
}

const initializeFormData = () => {
  Object.assign(formData, form.data)
  formData.status = formData.status ? formData.status : '1'
  formData.expiryDate = formData.expiryDate ? formData.expiryDate : null
  formData.expiryDateBoolean = formData.expiryDate ? 1 : 0
  if (form.operation == 'add') {
    titleStr.value = '新增账号'
    getPrependCustomer()
  } else if (form.operation == 'edit') {
    titleStr.value = '编辑账号'
    const str = form.data?.accountName.split('_')
    prependCustomer.value = str?.[0]
    formData.accountName = str?.[1]
  }
}

const expiryDateChange = (value: string | number | boolean) => {
  if (value === 0) {
    formData.expiryDate = null
  }
}

const handleClose = () => {
  resetObjectValues(formData)
  isEditPwd.value = false
}

const handleCancel = () => {
  form.visible = false
}

const formRef = ref()
const handleOk = debounce(async () => {
  if (!formRef.value) {
    ElMessage.error('表单引用错误，请重试')
    return
  }

  try {
    const valid = await formRef.value.validate()

    if (valid) {
      const params = {
        ...formData,
        accountName: `${prependCustomer.value}_${formData.accountName}`,
        clientId: userStore.clientId
      }

      const api = form.operation === 'add' ? saveAccountInfo : updateAccountInfo

      try {
        const res = await api(params)

        if (res.code === '200') {
          form.visible = false
          emit('refreshList')
          ElMessage.success('操作成功')
        } else {
          ElMessage.error(res.message)
        }
      } catch (apiError) {
        ElMessage.error('网络请求失败，请稍后重试')
      }
    }
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入内容')
  }
}, 300)
</script>

<style lang="scss" scoped>
.self-radio {
  padding: 4px 16px;
  border-radius: 32px;
}

.active-self-radio {
  background-color: #f2f3f5;
  color: #165dff;
}

.per-content {
  margin-top: 10px;
}

.pwd-default {
  // width: 688px;
  width: 605px;
  height: 32px;
  background: #f2f3f5;
  border-radius: 2px 2px 2px 2px;
  padding: 0 12px;
  display: flex;
  align-items: center;

  .pwdd-circle {
    width: 6px;
    height: 6px;
    background: #4e5969;
    border-radius: 50%;

    & + .pwdd-circle {
      margin-left: 12px;
    }
  }
}
</style>
<style lang="scss">
.per-content {
  margin-top: 5px;
}
</style>
