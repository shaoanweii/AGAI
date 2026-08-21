<script setup lang="ts">
import { computed, ref } from 'vue'
import { debounce } from 'lodash-es'
import {
  BatchProcessTitleMapByType,
  HandleOperationTypeOptions,
  PassOperationTypeOptions,
  BatchProcessingTypeEnum,
  OperationTypeEnum
} from '../ehConstants'
import OptionToggleGroup from '@/components/Business/EventHandle/components/OptionToggleGroup.vue'
import useSingleEventStore from '@/store/modules/singleEvent'
import {
  assign,
  batchApprove,
  batchAssign,
  batchCloseApi,
  batchConfirm,
  batchConfirmReject
} from '@/api/singlePointEvent'
import { ElMessage } from 'element-plus'
import CcPersonnelSelect from '@/components/Business/EventHandle/components/CcPersonnelSelect.vue'
import { useLoading } from '@/hooks/useLoading'

// 批量处理弹窗
defineOptions({
  name: 'BatchProcessing'
})

const emits = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const visible = defineModel({ default: false })
const { type, selection } = defineProps<{
  type: BatchProcessingTypeEnum
  selection: any[]
}>()

const singleEventStore = useSingleEventStore()
const { showLoading, hideLoading } = useLoading()

const formData = ref<{
  description: string
  operationType: OperationTypeEnum
  closeReason: string | undefined
  rejectReason: string | undefined
  eventProcessStartTime: string | undefined
  eventProcessEndTime: string | undefined
  mainRespOrgId: string | undefined
  mainRespOrgNo: string | undefined
  mainRespOrgName: string | undefined
  mainRespUserId: string | undefined
  mainRespUserEmpNo: string | undefined
  mainRespUserName: string | undefined
  ccUsers: any[]
  handlers: any[]
  handlerUserIds: string[]
}>({
  description: '',
  operationType: OperationTypeEnum.PassResolve,
  closeReason: undefined,
  rejectReason: undefined,
  eventProcessStartTime: undefined,
  eventProcessEndTime: undefined,
  ccUsers: [],
  handlers: [],
  mainRespOrgId: undefined,
  mainRespOrgNo: undefined,
  mainRespOrgName: undefined,
  mainRespUserId: undefined,
  mainRespUserEmpNo: undefined,
  mainRespUserName: undefined,
  handlerUserIds: []
})

const ids = computed(() => {
  return selection?.map(item => item.id) || []
})

const ccPersonnel = ref()

// 批量审批
const batchPass = debounce(async () => {
  /**
   * 校验规则
   * 审核通过
   * 1.主责人员必填
   *
   * 关闭事件
   * 1.关闭原因必填
   * 2.关闭原因选中其他时，添加说明必填
   */
  if (formData.value.operationType === OperationTypeEnum.PassResolve) {
    // 审核通过
    if (!formData.value.mainRespUserId) {
      ElMessage.warning('请选择主责人员')
      return
    }
    showLoading()
    try {
      const params = {
        eventProcessStartTime: formData.value.eventProcessStartTime,
        eventProcessEndTime: formData.value.eventProcessEndTime,
        description: formData.value.description,
        ids: ids.value,
        ...singleEventStore.deptInfoByOrgId(formData.value.mainRespOrgId),
        ...singleEventStore.userInfoByUserId(formData.value.mainRespUserId),
        ccUsers: formData.value.ccUsers
      }
      const res = await batchApprove(params)
      if (res.success) {
        handleCancel()
        emits('confirm')
      }
    } catch (error: any) {
      // ElMessage.warning(error.message)
    } finally {
      hideLoading()
    }
  } else if (formData.value.operationType === OperationTypeEnum.CloseReject) {
    // 关闭事件
    if (!formData.value.closeReason) {
      ElMessage.warning('请选择关闭原因')
      return
    }
    showLoading()
    try {
      const params = {
        description: formData.value.description,
        closeReason: formData.value.closeReason,
        ids: ids.value
      }
      const res = await batchCloseApi(params)
      if (res.success) {
        handleCancel()
        emits('confirm')
      }
    } catch (error: any) {
      // ElMessage.warning(error.message)
    } finally {
      hideLoading()
    }
  }
}, 300)
// 批量确认
const batchHandle = debounce(async () => {
  /**
   * 校验规则
   * 驳回事件
   * 1.驳回原因必填
   * 2.驳回原因选中其他时，添加说明必填
   */
  if (formData.value.operationType === OperationTypeEnum.RejectEvent) {
    // 关闭事件
    if (!formData.value.rejectReason) {
      ElMessage.warning('请选择驳回原因')
      return
    }
    if (formData.value.rejectReason === 'other reason' && !formData.value.description) {
      ElMessage.warning('请填写添加说明')
      return
    }
    showLoading()
    try {
      const params = {
        description: formData.value.description,
        rejectReason: formData.value.rejectReason,
        ids: ids.value
      }
      const res = await batchConfirmReject(params)
      if (res.success) {
        handleCancel()
        emits('confirm')
      }
    } catch (error: any) {
      // ElMessage.warning(error.message)
    } finally {
      hideLoading()
    }
  } else if (formData.value.operationType === OperationTypeEnum.HandleResolve) {
    // 确认时处理人员改为必选，避免提交空 handlers
    if (!formData.value.handlerUserIds?.length) {
      ElMessage.warning('请选择处理人员')
      return
    }
    showLoading()
    try {
      // 将 handlerUserIds 转换为用户对象数组
      const handlers = formData.value.handlerUserIds?.map(userId => {
        return {
          ...singleEventStore.getUserModelByUserId(userId, handleUserOptions.value),
          orgId: selection?.[0]?.mainRespOrgId,
          orgNo: selection?.[0]?.mainRespOrgNo,
          orgName: selection?.[0]?.mainRespOrgName
        }
      })
      const params = {
        eventProcessStartTime: formData.value.eventProcessStartTime,
        eventProcessEndTime: formData.value.eventProcessEndTime,
        ids: ids.value,
        ccUsers: formData.value.ccUsers,
        handlers,
        description: formData.value.description
      }
      const res = await batchConfirm(params)
      if (res.success) {
        handleCancel()
        emits('confirm')
      }
    } catch (error: any) {
      // ElMessage.warning(error.message)
    } finally {
      hideLoading()
    }
  }
}, 300)
// 批量关闭
const batchClose = debounce(async () => {
  /**
   * 校验规则
   * 1.关闭原因必填
   */
  if (!formData.value.closeReason) {
    ElMessage.warning('请选择关闭原因')
    return
  }
  showLoading()
  try {
    const params = {
      description: formData.value.description,
      closeReason: formData.value.closeReason,
      ids: ids.value
    }
    const res = await batchCloseApi(params)
    if (res.success) {
      handleCancel()
      emits('confirm')
    }
  } catch (error: any) {
    ElMessage.warning(error.message)
  } finally {
    hideLoading()
  }
}, 300)
// 批量分派
const batchDispatch = debounce(async () => {
  /**
   * 校验规则
   * 1.处理人员必填
   */
  if (!formData.value.handlerUserIds?.length) {
    ElMessage.warning('请选择处理人员')
    return
  }
  showLoading()
  try {
    // 将 handlerUserIds 转换为用户对象数组
    const handlers = formData.value.handlerUserIds?.map(userId => {
      return {
        ...singleEventStore.getUserModelByUserId(userId, handleUserOptions.value),
        orgId: selection?.[0]?.mainRespOrgId,
        orgNo: selection?.[0]?.mainRespOrgNo,
        orgName: selection?.[0]?.mainRespOrgName
      }
    })
    const params = {
      eventProcessStartTime: formData.value.eventProcessStartTime,
      eventProcessEndTime: formData.value.eventProcessEndTime,
      description: formData.value.description,
      handlers,
      ids: ids.value
    }
    const res = await batchAssign(params)
    if (res.success) {
      handleCancel()
      emits('confirm')
    }
  } catch (error: any) {
    // ElMessage.warning(error.message)
  } finally {
    hideLoading()
  }
}, 300)

// 单条分派
const SingleDispatch = debounce(async () => {
  /**
   * 校验规则
   * 1.处理人员必填
   */
  if (!formData.value.mainRespUserId) {
    ElMessage.warning('请选择处理人员')
    return
  }
  showLoading()
  try {
    const _handlers = singleEventStore.getUserModelByUserId(
      formData.value.mainRespUserId,
      handleUserOptions.value
    ) as any
    const params = {
      eventProcessStartTime: formData.value.eventProcessStartTime,
      eventProcessEndTime: formData.value.eventProcessEndTime,
      description: formData.value.description,
      handler: {
        ..._handlers,
        ...(_handlers.orgId && _handlers.orgName
          ? {}
          : {
              orgId: selection?.[0]?.mainRespOrgId,
              orgNo: selection?.[0]?.mainRespOrgNo,
              orgName: selection?.[0]?.mainRespOrgName
            })
      },
      id: selection ? selection?.[0]?.id : undefined
    }
    const res = await assign(params)
    if (res.success) {
      handleCancel()
      emits('confirm')
    }
  } catch (error: any) {
    // ElMessage.warning(error.message)
  } finally {
    hideLoading()
  }
}, 300)

// 分发按钮事件
const handleConfirm = () => {
  console.log('ccPersonnel', ccPersonnel.value)

  if (type === BatchProcessingTypeEnum.Pass) {
    batchPass()
  } else if (type === BatchProcessingTypeEnum.Handle) {
    batchHandle()
  } else if (type === BatchProcessingTypeEnum.Close) {
    batchClose()
  } else if (type === BatchProcessingTypeEnum.Dispatch) {
    batchDispatch()
  } else if (type === BatchProcessingTypeEnum.SingleDispatch) {
    SingleDispatch()
  }
}

const clearFormData = () => {
  setTimeout(() => {
    formData.value.closeReason = undefined
    formData.value.description = ''
    formData.value.eventProcessStartTime = undefined
    formData.value.eventProcessEndTime = undefined
    formData.value.operationType = OptionToggleOptions.value[0]?.value
    formData.value.rejectReason = undefined
    formData.value.ccUsers = []
    formData.value.handlers = []
    formData.value.mainRespOrgId = undefined
    formData.value.mainRespOrgNo = undefined
    formData.value.mainRespOrgName = undefined
    formData.value.mainRespUserId = undefined
    formData.value.mainRespUserEmpNo = undefined
    formData.value.mainRespUserName = undefined
    formData.value.handlerUserIds = []
    handleUserOptions.value = []
    handleUserCascaderOptions.value = []
    handleUserMultiCascaderOptions.value = []
  }, 300)
}

const handleCancel = () => {
  visible.value = false

  emits('cancel')
}

const OptionToggleOptions = computed(() => {
  if (type === BatchProcessingTypeEnum.Pass) {
    return PassOperationTypeOptions
  } else if (type === BatchProcessingTypeEnum.Handle) {
    return HandleOperationTypeOptions
  } else {
    return []
  }
})

const closeOptions = computed(() => {
  if (type === BatchProcessingTypeEnum.Pass) {
    // return singleEventStore.task_event_approve_close_reason
    return singleEventStore.task_event_close_reason
  } else if (type === BatchProcessingTypeEnum.Close) {
    return singleEventStore.task_event_close_reason
  } else {
    return []
  }
})

const handleUserOptions = ref<any[]>([])
const handleUserCascaderOptions = ref<any[]>([])
const handleUserMultiCascaderOptions = ref<any[]>([])
const mainRespUserCascaderOptions = ref<any[]>([])
const mainRespUserOptions = ref<any[]>([])

const handleUserSingleCascaderProps = {
  label: 'label',
  value: 'value',
  children: 'children',
  emitPath: false,
  checkStrictly: true,
  expandTrigger: 'click'
} as const

const handleUserMultipleCascaderProps = {
  ...handleUserSingleCascaderProps,
  multiple: true,
  checkStrictly: false
} as const

const mainRespUserCascaderProps = {
  label: 'label',
  value: 'value',
  children: 'children',
  emitPath: false,
  checkStrictly: false,
  expandTrigger: 'click'
} as const

/**
 * 根据部门路径提取二级和三级部门名称，优先使用人员节点自带的部门字段兜底。
 * @param path 当前人员所在部门路径
 * @param account 人员信息
 * @returns 人员完整展示文案所需的部门名称
 */
const getMainRespDepartmentNames = (path: any[] = [], account?: any) => {
  const pathNames = path.map(item => item?.name || '').filter(Boolean)

  return {
    secondDeptName:
      account?.secondDeptName || pathNames[pathNames.length - 2] || pathNames[0] || '',
    thirdDeptName:
      account?.thirdDeptName || account?.deptName || pathNames[pathNames.length - 1] || ''
  }
}

/**
 * 格式化主责人员节点展示文案。
 * @param account 人员信息
 * @returns 员工-工号
 */
const formatMainRespUserLabel = (account: any) => {
  return `${account?.userName || ''}${account?.employeeId ? `-${account.employeeId}` : ''}`
}

/**
 * 将部门人员树转换为批量审核主责人员级联选项。
 * @param tree 原始部门人员树
 * @returns 二级部门 / 三级部门 / 员工级联选项
 */
const buildMainRespUserCascaderOptions = (tree: any[] = []) => {
  const userMap = new Map<string, any>()

  const buildAccountOptions = (accounts: any[] = [], path: any[] = []) => {
    return accounts
      .filter(account => account?.userId)
      .map(account => {
        const departmentNames = getMainRespDepartmentNames(path, account)
        const label = formatMainRespUserLabel(account)
        const fullLabel = [departmentNames.secondDeptName, departmentNames.thirdDeptName, label]
          .filter(Boolean)
          .join('#')

        userMap.set(account.userId, account)

        return {
          value: account.userId,
          label,
          userId: account.userId,
          userName: account.userName,
          employeeId: account.employeeId,
          deptId: account.deptId || path[path.length - 1]?.id,
          deptNo: account.thirdDeptCode || account.secondDeptCode || path[path.length - 1]?.code,
          deptName: account.deptName || path[path.length - 1]?.name,
          fullLabel
        }
      })
  }

  const options = (tree || [])
    .flatMap(root => root?.child || [])
    .filter(secondDept => secondDept?.id)
    .map(secondDept => {
      const thirdDeptOptions = (secondDept.child || [])
        .filter((thirdDept: any) => thirdDept?.id)
        .map((thirdDept: any) => {
          const accountOptions = buildAccountOptions(thirdDept.account || [], [
            secondDept,
            thirdDept
          ])

          if (!accountOptions.length) return null

          return {
            value: `dept:${thirdDept.id}`,
            label: thirdDept.name || '',
            children: accountOptions
          }
        })
        .filter(Boolean)

      const secondDeptAccountOptions = buildAccountOptions(secondDept.account || [], [secondDept])
      const children = [...thirdDeptOptions, ...secondDeptAccountOptions]

      if (!children.length) return null

      return {
        value: `dept:${secondDept.id}`,
        label: secondDept.name || '',
        children
      }
    })
    .filter(Boolean)

  mainRespUserOptions.value = Array.from(userMap.values())
  return options
}

/**
 * 按完整部门路径、人员名称和工号搜索主责人员。
 * @param node 级联搜索节点
 * @param keyword 搜索关键字
 * @returns 是否命中当前节点
 */
const filterMainRespUserNode = (node: any, keyword: string) => {
  const query = String(keyword || '')
    .trim()
    .toLowerCase()
  if (!query) return true

  const data = node?.data || {}
  const searchText = [
    node?.text,
    data.label,
    data.fullLabel,
    data.userName,
    data.employeeId,
    data.deptName
  ]
    .filter(Boolean)
    .join('#')
    .toLowerCase()

  return searchText.includes(query)
}

/**
 * 主责人员变更后同步主责部门字段，保持原接口入参结构不变。
 * @param value 级联组件当前值
 */
const handleMainRespUserChange = (value?: unknown) => {
  const userId = Array.isArray(value) ? String(value[value.length - 1] || '') : String(value || '')
  const userInfo = mainRespUserOptions.value.find(item => item.userId === userId)

  formData.value.mainRespUserEmpNo = userInfo?.employeeId
  formData.value.mainRespUserName = userInfo?.userName
  formData.value.mainRespOrgId = userInfo?.deptId
  formData.value.mainRespOrgNo = userInfo?.deptId
  formData.value.mainRespOrgName = userInfo?.deptName
}

// 初始化单个分派是处理人员的下拉选项
const initHandleUser = async () => {
  const _mainRespOrgId = selection?.[0]?.mainRespOrgId
  if (_mainRespOrgId) {
    /**
     * 处理人员改为级联后，界面展示使用树结构，
     * 但提交参数仍然要按 userId 还原为原来的 handler / handlers 对象结构。
     */
    const departAccountTree = await singleEventStore.fetchDepartAccountTreeByDeptIds([
      _mainRespOrgId
    ])
    handleUserCascaderOptions.value =
      singleEventStore.buildDepartAccountCascaderOptions(departAccountTree)
    handleUserMultiCascaderOptions.value =
      singleEventStore.buildDepartAccountMultiCascaderOptions(departAccountTree)
    handleUserOptions.value = singleEventStore.flattenDepartAccountUsers(departAccountTree)
  } else {
    handleUserOptions.value = []
    handleUserCascaderOptions.value = []
    handleUserMultiCascaderOptions.value = []
  }
}

// 初始化批量审核主责人员级联选项
const initMainRespUserOptions = async () => {
  const [departAccountTree] = await Promise.all([
    singleEventStore.getDepartAccountTree(),
    singleEventStore.getDepartTree()
  ])
  mainRespUserCascaderOptions.value = buildMainRespUserCascaderOptions(departAccountTree)
  singleEventStore.departUserOptions = mainRespUserOptions.value
}

const handleOpen = async () => {
  formData.value.operationType = OptionToggleOptions.value[0]?.value

  if (
    [
      BatchProcessingTypeEnum.SingleDispatch,
      BatchProcessingTypeEnum.Handle,
      BatchProcessingTypeEnum.Dispatch
    ].includes(type)
  ) {
    await initHandleUser()
  }

  if (type === BatchProcessingTypeEnum.Pass) {
    await initMainRespUserOptions()
  }
}

const handleClose = () => {
  clearFormData()
  emits('close')
}
</script>

<template>
  <FDialog
    v-model:visible="visible"
    width="480px"
    @open="handleOpen"
    :confirm="handleConfirm"
    @cancel="handleCancel"
    @close="handleClose"
  >
    <template #header>
      <span>{{ BatchProcessTitleMapByType[type] }}</span>
    </template>
    <div>
      <el-form :model="formData" ref="formDataRef" @submit.prevent>
        <el-form-item
          v-if="[BatchProcessingTypeEnum.Pass, BatchProcessingTypeEnum.Handle].includes(type)"
          label="操作类型"
          prop=""
        >
          <OptionToggleGroup v-model="formData.operationType" :options="OptionToggleOptions" />
        </el-form-item>
        <!-- 主责人员必选 -->
        <el-form-item
          v-if="
            [BatchProcessingTypeEnum.Pass].includes(type) &&
            [OperationTypeEnum.PassResolve].includes(formData.operationType)
          "
          label="主责人员"
          prop=""
        >
          <el-cascader
            v-model="formData.mainRespUserId"
            placeholder="请选择主责人员"
            clearable
            filterable
            separator="#"
            :show-all-levels="true"
            :options="mainRespUserCascaderOptions"
            :props="mainRespUserCascaderProps"
            :filter-method="filterMainRespUserNode"
            class="w-full"
            @change="handleMainRespUserChange"
          />
        </el-form-item>
        <!-- 处理人员单选 -->
        <el-form-item
          v-if="[BatchProcessingTypeEnum.SingleDispatch].includes(type)"
          label="处理人员"
          prop=""
        >
          <el-cascader
            v-model="formData.mainRespUserId"
            placeholder="请选择处理人员"
            clearable
            filterable
            :show-all-levels="false"
            :options="handleUserCascaderOptions"
            :props="handleUserSingleCascaderProps"
            class="flex-1"
          />
        </el-form-item>
        <!-- 处理人员多选 -->
        <el-form-item
          v-if="
            [BatchProcessingTypeEnum.Dispatch].includes(type) ||
            ([BatchProcessingTypeEnum.Handle].includes(type) &&
              [OperationTypeEnum.HandleResolve].includes(formData.operationType))
          "
          label="处理人员"
          prop=""
        >
          <el-cascader
            v-model="formData.handlerUserIds"
            placeholder="请选择处理人员"
            clearable
            filterable
            collapse-tags
            :show-all-levels="false"
            :options="handleUserMultiCascaderOptions"
            :props="handleUserMultipleCascaderProps"
            class="flex-1"
          />

          <!-- <CcPersonnelSelect
            v-model="formData.handlers"
            searchPlaceholder="请选择处理人员"
          ></CcPersonnelSelect> -->
        </el-form-item>
        <el-form-item
          v-if="
            ([BatchProcessingTypeEnum.Pass].includes(type) &&
              [OperationTypeEnum.PassResolve].includes(formData.operationType)) ||
            ([BatchProcessingTypeEnum.Handle].includes(type) &&
              [OperationTypeEnum.HandleResolve].includes(formData.operationType))
          "
          label="抄送人员"
          prop=""
        >
          <CcPersonnelSelect v-model="formData.ccUsers"></CcPersonnelSelect>
          <!-- <el-cascader
            placeholder="全部"
            :max-collapse-tags="1"
            collapse-tags
            :show-all-levels="false"
            show-checked-strategy="parent"
            filterable
            clearable
            :options="singleEventStore.departAccountTree"
            :props="{
              label: 'name',
              value: 'id',
              children: 'child',
              emitPath: false,
              checkStrictly: true
            }"
            class="flex-1"
            @change="departChange"
          /> -->
        </el-form-item>
        <!-- 关闭原因-必选项 -->
        <el-form-item
          v-if="
            ([BatchProcessingTypeEnum.Pass].includes(type) &&
              [OperationTypeEnum.CloseReject].includes(formData.operationType)) ||
            [BatchProcessingTypeEnum.Close].includes(type)
          "
          label="关闭原因"
          prop=""
        >
          <el-select
            v-model="formData.closeReason"
            placeholder="请选择关闭原因"
            clearable
            filterable
            :options="closeOptions"
            :props="{ label: 'text', value: 'value' }"
          />
        </el-form-item>
        <!-- 驳回原因-必选项 -->
        <el-form-item
          v-if="
            [BatchProcessingTypeEnum.Handle].includes(type) &&
            [OperationTypeEnum.RejectEvent].includes(formData.operationType)
          "
          label="驳回原因"
          prop=""
        >
          <el-select
            v-model="formData.rejectReason"
            placeholder="不限"
            clearable
            filterable
            :options="singleEventStore.task_event_reject_reason"
            :props="{ label: 'text', value: 'value' }"
          />
        </el-form-item>
        <el-form-item
          v-if="
            ([BatchProcessingTypeEnum.Pass].includes(type) &&
              [OperationTypeEnum.PassResolve].includes(formData.operationType)) ||
            ([BatchProcessingTypeEnum.Handle].includes(type) &&
              [OperationTypeEnum.HandleResolve].includes(formData.operationType)) ||
            [BatchProcessingTypeEnum.Dispatch, BatchProcessingTypeEnum.SingleDispatch].includes(
              type
            )
          "
          label="处理周期"
          prop=""
        >
          <el-date-picker
            :model-value="
              formData.eventProcessStartTime && formData.eventProcessEndTime
                ? [formData.eventProcessStartTime, formData.eventProcessEndTime]
                : undefined
            "
            @update:model-value="
              val => {
                formData.eventProcessStartTime = val?.[0]
                formData.eventProcessEndTime = val?.[1]
              }
            "
            type="daterange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <!-- 关闭原因, 驳回原因.选择其他时，添加说明必填 -->
        <el-form-item label="添加说明" prop="">
          <el-input
            v-model.trim="formData.description"
            clearable
            placeholder=""
            type="textarea"
            :rows="3"
            :maxlength="150"
            resize="none"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
    </div>
  </FDialog>
</template>

<style lang="scss" scoped></style>
