<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑角色' : '新增角色'"
    width="800px"
    style="padding: 0; height: 96%; display: flex; flex-direction: column; border-radius: 12px"
    align-center
    header-class="role-dialog-form-header-class"
    body-class="role-dialog-form-body-class"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="pl-40 pr-40 pt-24 h-full flex-col">
      <!-- Tabs -->
      <div class="flex-y-center">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ 'tab-active': tab.key === currentTab }"
          @click="handleTabClick(tab)"
        >
          {{ tab.label }}
        </div>
      </div>
      <div v-if="dialogVisible" class="content-layout flex-1">
        <!-- 基本信息 -->
        <BasicInfo
          v-show="currentTab === RoleFormTabKey.BASIC_INFO"
          ref="basicInfoRef"
          :is-edit="isEdit"
        />

        <!-- 权限配置 -->
        <PermissionConfig
          v-show="currentTab === RoleFormTabKey.PERMISSION_CONFIG"
          ref="permissionConfigRef"
          :isEdit="isEdit"
          :menuPermissionList="menuPermissionList"
          :basicInfoRef="basicInfoRef"
        />

        <!-- 关联账号 -->
        <!-- <AccountRelation
          v-show="currentTab === RoleFormTabKey.ACCOUNT_RELATION"
          :roleData="roleData"
          ref="accountRelationRef"
        /> -->
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer flex-y-center">
        <div class="flex-y-center footer-btn-layout">
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
import { ref, reactive, computed, nextTick } from 'vue'
import BasicInfo from './BasicInfo.vue'
import PermissionConfig from './PermissionConfig.vue'
import AccountRelation from './AccountRelation.vue'
import { getListByRoleId, saveOrUpdateRole } from '@/api/role'

interface Props {
  visible: boolean
  roleData?: any
  isEdit?: boolean
  menuPermissionList: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  roleData: null,
  isEdit: false,
  menuPermissionList: () => ({})
})

const emit = defineEmits<Emits>()

// 组件引用
const basicInfoRef = ref()
const permissionConfigRef = ref()
// const accountRelationRef = ref()

// 响应式数据
const submitLoading = ref(false)

// 角色表单 Tab Key 枚举
enum RoleFormTabKey {
  BASIC_INFO = 'basicInfo',
  PERMISSION_CONFIG = 'permissionConfig',
  ACCOUNT_RELATION = 'accountRelation'
}

const tabs = reactive([
  {
    label: '基本信息',
    key: RoleFormTabKey.BASIC_INFO
  },
  {
    label: '权限配置',
    key: RoleFormTabKey.PERMISSION_CONFIG
  }
  // {
  //   label: '关联账号',
  //   key: RoleFormTabKey.ACCOUNT_RELATION
  // }
])

const currentTab = ref<RoleFormTabKey>(RoleFormTabKey.BASIC_INFO)

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

// 角色详情
const roleDetail = ref<any>({})
// 获取详情数据
const fetchDetailByRoleId = async () => {
  const res = await getListByRoleId({ roleId: props.roleData.roleId })
  if (res.success) {
    roleDetail.value = res.result
  }
}

// 方法
const initFormData = () => {
  // 初始化基本信息组件
  basicInfoRef.value?.initData(roleDetail.value)

  // 初始化权限配置组件
  permissionConfigRef.value?.initData(roleDetail.value)
}

// 提交数据
const handleSubmit = async () => {
  try {
    // 验证基本信息表单并获取数据
    const basicInfoData = await basicInfoRef.value?.validateAndGetData()
    console.log('basicInfoData', basicInfoData)

    if (!basicInfoData) {
      currentTab.value = RoleFormTabKey.BASIC_INFO
      return
    }

    // 验证权限配置表单并获取数据
    const permissionConfigData = await permissionConfigRef.value?.validateAndGetData()

    console.log('permissionConfigData', permissionConfigData)

    if (!permissionConfigData) {
      currentTab.value = RoleFormTabKey.PERMISSION_CONFIG
      return
    }

    submitLoading.value = true
    const params = {
      id: props?.roleData?.roleId || undefined,
      ...basicInfoData,
      ...permissionConfigData
      // userIdList: accountRelationRef.value.getLinkedUserIds()
    }
    const res = await saveOrUpdateRole(params)
    if (res.success) {
      emit('success')
      handleClose()
    }
  } catch (error) {
    console.error('提交表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 弹窗打开事件
const handleOpen = async () => {
  if (props.isEdit) {
    await fetchDetailByRoleId()
    initFormData()
  }
}
// 关闭弹窗事件
const handleClose = () => {
  // 重置所有子组件的表单
  basicInfoRef.value?.resetFields()
  permissionConfigRef.value?.resetFields()

  // 重置当前tab
  currentTab.value = RoleFormTabKey.BASIC_INFO

  nextTick(() => {
    // 重置数据
    basicInfoRef.value?.initData({})
    permissionConfigRef.value?.initData({})
  })

  emit('update:visible', false)
}

// 切换tab
const handleTabClick = (tab: { key: RoleFormTabKey }) => {
  currentTab.value = tab.key
}
</script>

<style lang="scss">
.role-dialog-form-header-class {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #ebf4fd 0%, #ffffff 100%);
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
  padding-bottom: 0;
}

.role-dialog-form-body-class {
  height: 100%;
}
</style>

<style lang="scss" scoped>
.content-layout {
  border: 1px solid #ebedf0;
}

.tab-item {
  cursor: pointer;
  width: 88px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f4f7;
  border-radius: 0;
  border-left: 1px solid;
  border-top: 1px solid;
  border-color: #ebedf0;
  font-weight: 600;
  font-size: 14px;
  color: #414651;
}

.tab-item:hover {
  background: #f2f4f7;
}

.tab-item:first-child {
  border-left: 1px solid #ebedf0;
}

.tab-item:last-child {
  border-right: 1px solid #ebedf0;
}

.tab-active {
  background: #1677ff !important;
  color: #ffffff !important;
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
</style>
