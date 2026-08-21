<template>
  <el-drawer
    :size="1200"
    v-model="form.visible"
    @open="handleOpen"
    @close="handleClose"
    destroy-on-close
    :data-testid="`role-form-drawer`"
  >
    <template #header>
      <div>
        {{ titleStr }}
      </div>
    </template>
    <el-form ref="formRef" :model="formData" :rules="rules" :style="{ width: '800px' }">
      <el-form-item prop="roleName" label="角色名称" required>
        <!--<el-input v-model="formData.roleName" placeholder="请输入" :maxlength="20" clearable/>-->
        <FtInput
          v-model.allTrim="formData.roleName"
          :data-testid="`role-form-10001`"
          placeholder="请输入"
          :maxlength="20"
          clearable
        />
      </el-form-item>
      <el-form-item prop="permissionIdList" label="权限配置" required>
        <PermissionConfiguration
          v-model="formData.permissionIdList"
          :roleAuthTree="roleAuthTreeList!"
        />
      </el-form-item>
      <el-form-item prop="enabled" label="启用状态">
        <el-radio-group v-model="formData.enabled" :data-testid="`role-form-10003`">
          <el-radio
            v-for="(item, index) of conditions.stopOrEnable"
            :key="index"
            :data-testid="`role-form-10003-op-${index}`"
            :value="item.key"
            >{{ item.value }}
          </el-radio>
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
import PermissionConfiguration from '@/components/PermissionConfiguration/index.vue'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'
import { queryMenuPermissionList, queryRoleInfo, saveOrUpdateRole } from '@/api/role'
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import { useFormRules } from '@/hooks/useForm'
import { delayer } from '@/utils'
import useUserStore from '@/stores/modules/user'
import FtInput from '@/components/FtInput.vue'

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshList'])
const form = inject('form') as Form

const createDefaultModel = (): Api.Role.RoleParams => ({
  id: undefined,
  clientId: '',
  roleName: '',
  permissionIdList: [],
  enabled: ''
})

let formData = reactive<Api.Role.RoleParams>(createDefaultModel())
let titleStr = ref('')

const { createRequiredRule } = useFormRules()

const rules = {
  roleName: [createRequiredRule('角色名称必填')],
  permissionIdList: [
    createRequiredRule('权限配置必填', {
      trigger: 'change',
      validator: async (_rule: any, value: any, callback: (error?: string | Error) => void) => {
        await delayer()

        // 如果是编辑模式且权限树还没有加载完成，跳过验证
        if (
          form.operation === 'edit' &&
          (!roleAuthTreeList.value || roleAuthTreeList.value.length === 0)
        ) {
          callback()
          return
        }

        if (!value || value.length === 0) {
          callback(new Error('权限配置必填'))
          return
        }
        callback()
      }
    })
  ],
  enabled: [createRequiredRule('启用状态必填')]
}

const initializeFormData = () => {
  // formData = reactive();
  Object.assign(formData, { ...form.data })
  formData.clientId = useUserStore().clientId
  if (form.operation == 'add') {
    formData.enabled = '1'
    titleStr.value = '新增角色'
  } else if (form.operation == 'edit') {
    console.log('formData', formData)
    titleStr.value = '编辑角色'
  }
}
const roleAuthTreeList = ref<Api.Role.PermissionTree[]>([])
const handleOpen = async () => {
  initializeFormData()
  try {
    if (form.operation === 'add') {
      roleAuthTreeList.value = await queryMenuPermissionList({ clientId: formData.clientId }).then(
        res => res.result
      )
    } else if (form.operation == 'edit') {
      const response = await queryRoleInfo({
        id: formData.id,
        clientId: formData.clientId
      }).then(res => res.result)
      // formData.id = response.id;
      formData.enabled = response.enabled?.toString()
      formData.roleName = response.roleName
      roleAuthTreeList.value = response.roleAuthTreeList

      // 等待下一个tick，确保PermissionConfiguration组件已经处理完权限数据
      await nextTick()

      // 清除表单验证状态，避免编辑时显示必填提示
      if (formRef.value) {
        formRef.value.clearValidate('permissionIdList')
      }
    }
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}
const handleClose = () => {
  formData = reactive(createDefaultModel())
  roleAuthTreeList.value = []
  // 清除表单验证状态
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleCancel = () => {
  form.visible = false
}

const formRef = ref()
const handleOk = debounce(async () => {
  console.log('formData', formData)
  try {
    const valid = await formRef.value.validate()
    console.log('valid', valid)
    if (valid) {
      console.log('通过校验')
      saveOrUpdateRole(formData)
        .then(res => {
          if (res.code === '200') {
            form.visible = false
            emit('refreshList')
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch((err: any) => {
          ElMessage.error(err.message)
        })
    }
  } catch (error) {
    console.log('表单验证失败:', error)
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
  width: 688px;
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
