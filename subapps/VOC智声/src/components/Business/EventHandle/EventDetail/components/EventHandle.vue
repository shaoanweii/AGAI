<script setup lang="ts">
import { computed, ref, watch, watchEffect } from 'vue'
import RelatedTicket from './RelatedTicket.vue'
import useSingleEventStore from '@/store/modules/singleEvent'
import CcPersonnelSelect from '@/components/Business/EventHandle/components/CcPersonnelSelect.vue'
import { EventType, HandleModeEnum, taskStatusMap } from '../../ehConstants'
import type { SingleEventDetailVo, SingleEventPrivateMsgModel } from '@/api/singlePointEvent/types'
import { numberToChinese, openWindow } from '@/utils/index'
import { useClipboard } from '@/hooks/useClipboard'
import { ElMessage } from 'element-plus'
import StandardScript from '@/components/Business/EventHandle/EventDetail/components/StandardScript.vue'
import { ask_simple } from '@/api/common'
import { isEmpty } from 'lodash-es'

defineOptions({
  name: 'EventHandle'
})

const { eventType, eventInfo, originalSoundDetails, row } = defineProps<{
  eventType: EventType
  eventInfo: SingleEventDetailVo
  originalSoundDetails: any
  row: any
}>()

const singleEventStore = useSingleEventStore()

const ssVisible = ref(false)
const setSsVisible = (val: boolean) => {
  ssVisible.value = val
}

const initForm = () =>
  ({
    privateMsgCount: undefined,
    privateMsgProgressCode: undefined,
    reviewProgressCode: undefined,
    mainRespOrgId: undefined,
    mainRespUserId: undefined,
    // 处理人中间变量
    handlerUserId: undefined,
    ccUsers: undefined,
    privateMsgChannel: undefined,
    eventProcessStartTime: undefined,
    eventProcessEndTime: undefined,
    description: undefined,
    unprocessedReason: undefined,
    reviewDate: undefined,
    // 回评人员(中间变量)
    reviewUserId: undefined,
    reviewContent: undefined,
    isProcessed: undefined,
    processDescription: undefined,
    custName: undefined,
    custMobile: undefined,
    reviewModelContent: undefined
  }) as any

const formData = ref({
  remark: '',
  ...initForm()
})

/**
 * @description: 初始化私信详情
 * @return {*}
 */
const privateMsgDetailsItem = () => ({
  privateMsgTime: undefined,
  userId: undefined,
  userEmpNo: undefined,
  userName: undefined,
  orgId: undefined,
  orgNo: undefined,
  orgName: undefined
})

// 私信详情
const privateMsgDetails = ref<SingleEventPrivateMsgModel[]>(
  Array.from({ length: 3 }, privateMsgDetailsItem)
)

// 处理方式改变时的回调函数
const handleProcessModeChange = () => {
  formData.value.unprocessedReason = undefined
}

// 根据处理方式获取处理原因
const reasonForProcessingOptions = computed(() => {
  if (['1', '是'].includes(formData.value.isProcessed)) {
    return singleEventStore.task_event_approve_process_mode
  } else if (['0', '否'].includes(formData.value.isProcessed)) {
    return singleEventStore.task_event_close_reason
  } else {
    return []
  }
})

// 标准话术
const all_candidates = ref<any[]>([])

/**
 * @description: 获取	模型推荐话术及标准话术
 * @return {*}
 */
const getReviewContent = async () => {
  try {
    if (!eventInfo.mainPostDetails || !eventInfo.brandName) {
      return
    }
    const params = {
      // question: '大灯太暗了，晚上看不清路',
      // bot_name: '远途',
      question: eventInfo.mainPostDetails,
      bot_name: eventInfo.brandName,
      return_all_candidates: true
    }
    const res = await ask_simple(params)
    if (res?.success) {
      formData.value.reviewModelContent = res?.result?.answer
      all_candidates.value = res?.result?.all_candidates
    }
  } catch (error) {
    console.log('error', error)
  }
}

// 获取当前事件状态
const curEventTaskStatus = (type: EventType) => {
  // 查看状态
  if (type === EventType.VIEW && !eventInfo.permissions?.length) {
    return true
  }
  // permissions不为空，判断是否包含当前类型
  return eventInfo.permissions?.includes(type)
}

// 根据任务状态显示模型
const showModuleByTaskStatus = (status: string[]) => {
  // console.log('status', status)
  // console.log('eventInfo.taskStatus', eventInfo.taskStatus)
  return status.includes(eventInfo.taskStatus!)
}

// 处理人员下拉选项
const handleUserOptions = ref<any[]>([])
// 处理人员级联选项，仅用于“处理人员”相关字段
const handleUserCascaderOptions = ref<any[]>([])
// 处理人员完整用户信息，用于按 userId 还原提交对象
const handleUserLookupOptions = ref<any[]>([])

const handleUserCascaderProps = {
  label: 'label',
  value: 'value',
  children: 'children',
  emitPath: false,
  checkStrictly: true
} as const

/**
 * @description: 初始化处理人员相关数据源
 * @param {string | undefined} deptId 主责部门id
 * @return {Promise<void>}
 */
const initHandleUserSources = async (deptId?: string) => {
  if (!deptId) {
    handleUserOptions.value = []
    handleUserCascaderOptions.value = []
    handleUserLookupOptions.value = []
    singleEventStore.departUserOptions = []
    return
  }

  /**
   * 处理人员改为部门账号树后，需要同时保留两类数据：
   * 1. reviewUserId 仍沿用原来的平铺下拉数据；
   * 2. handlerUserId / 私信处理人员使用级联树展示，但提交时仍按 userId 回查完整人员模型。
   */
  const [reviewUsers, departAccountTree] = await Promise.all([
    singleEventStore.fetchAccountByDeptIds([deptId]),
    singleEventStore.fetchDepartAccountTreeByDeptIds([deptId])
  ])

  handleUserOptions.value = reviewUsers
  handleUserCascaderOptions.value =
    singleEventStore.buildDepartAccountCascaderOptions(departAccountTree)
  handleUserLookupOptions.value =
    singleEventStore.flattenDepartAccountUsers(departAccountTree) || []
  singleEventStore.departUserOptions = reviewUsers
}

const init = async () => {
  // console.log('eventInfo', eventInfo)

  if (showModuleByTaskStatus(taskStatusMap.handle)) {
    getReviewContent()
  }

  await initHandleUserSources(eventInfo.mainRespOrgId)
  formData.value.id = eventInfo.id
  formData.value.ccUsers = eventInfo.ccUsers
  formData.value.eventProcessStartTime = eventInfo.eventProcessStartTime
  formData.value.eventProcessEndTime = eventInfo.eventProcessEndTime
  formData.value.processDescription = eventInfo.processDescription
  formData.value.mainRespOrgId = eventInfo.mainRespOrgId
  formData.value.mainRespUserId = eventInfo.mainRespUserId
  // formData.value.mainRespUserId = eventInfo.handleUser?.userId
  formData.value.handlerUserId = eventInfo.handleUser?.userId
  formData.value.description = eventInfo.processDescription
  // formData.value.description = eventInfo.processDescription
  formData.value.isProcessed = eventInfo.isProcessed
  formData.value.unprocessedReason = eventInfo.unprocessedReason
  formData.value.reviewProgressCode = eventInfo.reviewProgressCode
  formData.value.reviewDate = eventInfo.reviewDate
  formData.value.reviewUserId = eventInfo.reviewHandler?.userId
  formData.value.reviewContent = eventInfo.reviewContent
  formData.value.privateMsgProgressCode = eventInfo.privateMsgProgressCode
  formData.value.privateMsgCount = eventInfo.privateMsgCount
  formData.value.privateMsgChannel = eventInfo.privateMsgChannel
  formData.value.custName = eventInfo.custName
  formData.value.custMobile = eventInfo.custMobile
  privateMsgDetails.value =
    eventInfo?.privateMsgDetails ?? Array.from({ length: 3 }, privateMsgDetailsItem)

  // 处理舆情台账回显
  if (eventInfo.showType === 2) {
    formData.value.reviewProgressCode = eventInfo.reviewProgressName
    formData.value.reviewUserId = eventInfo.reviewHandler?.userName
    formData.value.privateMsgProgressCode = eventInfo.privateMsgProgressName
    formData.value.privateMsgChannel = eventInfo.privateMsgChannelName

    if (eventInfo?.privateMsgDetails?.length) {
      privateMsgDetails.value = eventInfo?.privateMsgDetails?.map((item: any) => ({ ...item }))
    } else {
      privateMsgDetails.value = Array.from({ length: 3 }, privateMsgDetailsItem)
    }
  }
}

watchEffect(() => {
  // console.log('eventInfo--->watchEffect', eventInfo)
  if (!isEmpty(eventInfo)) {
    init()
  }
})

// watch(
//   () => eventInfo,
//   (nval: any) => {
//     console.log('nval', nval)

//     init()
//   },
//   {
//     deep: true
//   }
// )

// 处理私信详情中的处理人员
const getPrivateMsgDetails = () => {
  return privateMsgDetails.value.map(item => {
    if (!item.userId) return item
    const _itemUser = singleEventStore.getUserModelByUserId(
      item.userId,
      handleUserLookupOptions.value
    )
    return {
      privateMsgTime: item.privateMsgTime,
      userId: item.userId,
      userEmpNo: _itemUser.userEmpNo,
      userName: _itemUser.userName,
      orgId: _itemUser.orgId ?? row.mainRespOrgId,
      orgNo: _itemUser.orgNo ?? row.mainRespOrgNo,
      orgName: _itemUser.orgName ?? row.mainRespOrgName
    }
  })
}

const handleFormData = () => {
  return {
    ...formData.value,
    handler: singleEventStore.getUserModelByUserId(
      formData.value.handlerUserId,
      handleUserLookupOptions.value
    ),
    reviewHandler: singleEventStore.getUserModelByUserId(
      formData.value.reviewUserId,
      handleUserOptions.value
    ),
    privateMsgDetails: getPrivateMsgDetails()
  }
}

const { copy } = useClipboard()

// 复制并应用话术
const handleCopy = () => {
  copy(formData.value.reviewContent)
  // ElMessage.success('复制成功')
  formData.value.reviewContent = formData.value.reviewModelContent
}
// 前往主贴
const linkPost = () => {
  if (originalSoundDetails.mainPostUrl) {
    openWindow(originalSoundDetails.mainPostUrl!)
  }
}

// 复制并应用标准话术
const handleCopyAndApply = (row: any) => {
  // console.log('row', row)
  copy(row.afterValue)
  formData.value.reviewContent = row.answer
}

// 禁用
const isDisabled = computed(() => {
  // return curEventTaskStatus(EventType.VIEW)
  return !eventInfo.permissions?.length || eventType === EventType.VIEW
})

defineExpose({
  handleFormData
})

// init()
</script>

<template>
  <div class="event-handle">
    <el-form :model="formData" ref="formDataRef" @submit.prevent>
      <el-row :gutter="16">
        <!-- 审核 -->
        <!-- v-if="curEventTaskStatus(EventType.APPROVE)"> -->
        <template v-if="showModuleByTaskStatus(taskStatusMap.approve)">
          <!-- <el-col v-if="eventType === EventType.APPROVE" :span="8"> -->
          <el-col :span="8">
            <el-form-item label="主责人员" prop="">
              <div class="flex gap-16 w-full">
                <el-cascader
                  v-model="formData.mainRespOrgId"
                  placeholder=""
                  :max-collapse-tags="1"
                  collapse-tags
                  :show-all-levels="false"
                  show-checked-strategy="parent"
                  filterable
                  clearable
                  :disabled="isDisabled"
                  :options="singleEventStore.departTree"
                  :props="{
                    label: 'name',
                    value: 'id',
                    children: 'child',
                    emitPath: false,
                    checkStrictly: true
                  }"
                  class="flex-1"
                  @change="singleEventStore.departChange"
                />
                <!-- 主责人 -->
                <el-select-v2
                  v-model="formData.mainRespUserId"
                  placeholder=""
                  clearable
                  filterable
                  :disabled="isDisabled"
                  :options="singleEventStore.departUserOptions"
                  :props="{ label: 'userName', value: 'userId' }"
                  :fit-input-width="400"
                  class="flex-1"
                >
                  <template #default="{ item }">
                    <span>{{
                      `${item.userName}${item.employeeId ? `-${item.employeeId}` : ''}`
                    }}</span>
                  </template>
                </el-select-v2>
              </div>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="8" v-if="[EventType.CONFIRM, EventType.APPROVE].includes(eventType)"> -->
          <el-col :span="8">
            <el-form-item label="抄送人员" prop="">
              <CcPersonnelSelect
                v-model="formData.ccUsers"
                :disabled="isDisabled"
                :maxCCCount="500"
              ></CcPersonnelSelect>
            </el-form-item>
          </el-col>

          <!-- <el-col :span="8" v-if="[EventType.CONFIRM, EventType.APPROVE].includes(eventType)"> -->
          <el-col :span="8">
            <!-- <el-form-item label="处理进展" prop="">
            <FSelect :options="[]"></FSelect>
          </el-form-item> -->
            <el-form-item label="处理周期" prop="">
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
                clearable
                value-format="YYYY-MM-DD"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>

          <!-- <el-col v-if="[EventType.CONFIRM, EventType.APPROVE].includes(eventType)" :span="24"> -->
          <el-col :span="24">
            <el-form-item label="事件说明" prop="">
              <el-input
                v-model.trim="formData.description"
                clearable
                placeholder=""
                type="textarea"
                :rows="3"
                resize="none"
                show-word-limit
                :maxlength="500"
                :disabled="isDisabled"
              ></el-input>
            </el-form-item>
          </el-col>
        </template>

        <!-- 确认事件 -->
        <!-- <template v-if="curEventTaskStatus(EventType.CONFIRM)"> -->
        <template v-if="showModuleByTaskStatus(taskStatusMap.confirm)">
          <!-- eventType === EventType.CONFIRM -->
          <!-- <el-col v-if="[EventType.CONFIRM, EventType.HANDLE].includes(eventType)" :span="8"> -->
          <el-col :span="8">
            <!-- <el-form-item label="是否重复" prop="">
            <FSelect :options="[]"></FSelect>
          </el-form-item> -->
            <el-form-item label="处理人员" prop="">
              <el-cascader
                v-model="formData.handlerUserId"
                placeholder=""
                clearable
                filterable
                :show-all-levels="false"
                :options="handleUserCascaderOptions"
                :props="handleUserCascaderProps"
                class="flex-1"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>
          <!-- <el-col :span="8" v-if="[EventType.CONFIRM, EventType.APPROVE].includes(eventType)"> -->
          <el-col :span="8">
            <el-form-item label="抄送人员" prop="">
              <CcPersonnelSelect
                v-model="formData.ccUsers"
                :disabled="isDisabled"
                :maxCCCount="500"
              ></CcPersonnelSelect>
            </el-form-item>
          </el-col>
          <!-- <el-col :span="8" v-if="[EventType.CONFIRM, EventType.APPROVE].includes(eventType)"> -->
          <el-col :span="8">
            <!-- <el-form-item label="处理进展" prop="">
            <FSelect :options="[]"></FSelect>
          </el-form-item> -->
            <el-form-item label="处理周期" prop="">
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
                clearable
                value-format="YYYY-MM-DD"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>

          <!-- <el-col v-if="[EventType.CONFIRM, EventType.APPROVE].includes(eventType)" :span="24"> -->
          <el-col :span="24">
            <el-form-item label="事件说明" prop="">
              <el-input
                v-model.trim="formData.description"
                clearable
                placeholder=""
                type="textarea"
                :rows="3"
                resize="none"
                show-word-limit
                :maxlength="500"
                :disabled="isDisabled"
              ></el-input>
            </el-form-item>
          </el-col>
        </template>

        <!-- 处理事件 -->
        <!-- <template v-if="curEventTaskStatus(EventType.HANDLE) || curEventTaskStatus(EventType.VIEW)"> -->
        <template
          v-if="
            showModuleByTaskStatus(taskStatusMap.handle) ||
            showModuleByTaskStatus(taskStatusMap.close)
          "
        >
          <!-- <el-col v-if="eventType === EventType.HANDLE" :span="8"> -->
          <el-col :span="8">
            <el-form-item label="处理方式" prop="">
              <el-select
                v-model="formData.isProcessed"
                placeholder=""
                clearable
                filterable
                :options="singleEventStore.task_event_is_handled"
                :props="{ label: 'text', value: 'value' }"
                class="flex-1"
                :disabled="isDisabled"
                @change="handleProcessModeChange"
              />
            </el-form-item>
          </el-col>
          <!-- <el-col v-if="eventType === EventType.HANDLE" :span="8"> -->
          <el-col :span="8">
            <el-form-item label="处理原因" prop="">
              <el-select
                v-model="formData.unprocessedReason"
                placeholder=""
                clearable
                filterable
                :options="reasonForProcessingOptions"
                :props="{ label: 'text', value: 'value' }"
                class="flex-1"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>

          <!-- eventType === EventType.CONFIRM -->
          <!-- <el-col v-if="[EventType.CONFIRM, EventType.HANDLE].includes(eventType)" :span="8"> -->
          <el-col :span="8">
            <!-- <el-form-item label="是否重复" prop="">
            <FSelect :options="[]"></FSelect>
          </el-form-item> -->
            <el-form-item label="处理人员" prop="">
              <el-cascader
                v-model="formData.handlerUserId"
                placeholder=""
                clearable
                filterable
                :show-all-levels="false"
                :options="handleUserCascaderOptions"
                :props="handleUserCascaderProps"
                class="flex-1"
                :disabled="isDisabled"
              />
            </el-form-item>
          </el-col>

          <!-- <el-col v-if="[EventType.HANDLE].includes(eventType)" :span="24"> -->
          <el-col :span="24">
            <el-form-item label="添加描述" prop="">
              <el-input
                v-model.trim="formData.processDescription"
                clearable
                placeholder=""
                type="textarea"
                :rows="3"
                resize="none"
                show-word-limit
                :maxlength="500"
                :disabled="isDisabled"
              ></el-input>
              <!-- <div class="mt-16">
              <el-button type="primary">提交</el-button>
              <el-button type="primary" plain>转派事件</el-button>
            </div> -->
            </el-form-item>
          </el-col>

          <!-- 回评 -->
          <!-- [EventType.HANDLE].includes(eventType) -->
          <!-- &&
              (curEventTaskStatus(EventType.HANDLE) || curEventTaskStatus(EventType.VIEW)) -->
          <template
            v-if="
              formData.unprocessedReason &&
              [HandleModeEnum.OnlyReply, HandleModeEnum.ReplyAndPrivateMsg].includes(
                formData.unprocessedReason
              )
            "
          >
            <el-col :span="24">
              <div class="divid-line mb-16"></div>
            </el-col>
            <el-col :span="24">
              <div class="subtitle mb-16">回评内容</div>
            </el-col>
            <el-col :span="8">
              <el-form-item label="回评进度" prop="">
                <el-select
                  v-model="formData.reviewProgressCode"
                  placeholder=""
                  clearable
                  filterable
                  :options="singleEventStore.task_event_review_staus"
                  :props="{ label: 'text', value: 'value' }"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="回评时间" prop="">
                <el-date-picker
                  v-model="formData.reviewDate"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder=""
                  class="iw-full"
                  clearable
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="回评人员" prop="">
                <el-select-v2
                  v-model="formData.reviewUserId"
                  placeholder=""
                  clearable
                  filterable
                  :options="handleUserOptions"
                  :props="{ label: 'userName', value: 'userId' }"
                  class="flex-1"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="模型推荐话术" prop="" label-width="70px">
                <div class="w-full textarea-wrapper">
                  <textarea
                    v-model="formData.reviewModelContent"
                    :rows="4"
                    class="pb-32 w-full textarea-content"
                    disabled
                  ></textarea>
                  <div class="toolbar">
                    <div class="tool-item" @click="handleCopy">
                      <SvgIcon name="copy-03" width="16px" height="16px" color="#1677FF" />
                      <span>复制并应用</span>
                    </div>

                    <div class="tool-item" @click="linkPost">
                      <SvgIcon
                        name="arrow-narrow-up-right"
                        width="16px"
                        height="16px"
                        color="#1677FF"
                      />
                      <span>前往主贴</span>
                    </div>
                    <div class="tool-item" @click="setSsVisible(true)">
                      <SvgIcon name="search-eye-line" width="16px" height="16px" color="#1677FF" />
                      <span>查看标准话术</span>
                    </div>
                  </div>
                </div>
                <div class="mt-10 tip">模型推荐的话术仅作为回复参考，请人工审核后再进行回评。</div>
              </el-form-item>
              <el-form-item label="实际回评话术" prop="" label-width="70px">
                <div class="w-full textarea-wrapper">
                  <textarea
                    v-model="formData.reviewContent"
                    :rows="4"
                    class="pb-32 w-full textarea-content"
                    :disabled="isDisabled"
                  ></textarea>
                </div>
              </el-form-item>
            </el-col>
          </template>

          <!-- 私信 -->
          <!-- [EventType.HANDLE].includes(eventType) -->
          <!-- &&
              (curEventTaskStatus(EventType.HANDLE) || curEventTaskStatus(EventType.VIEW)) -->
          <template
            v-if="
              formData.unprocessedReason &&
              [HandleModeEnum.OnlyPrivateMsg, HandleModeEnum.ReplyAndPrivateMsg].includes(
                formData.unprocessedReason
              )
            "
          >
            <el-col :span="24">
              <div class="divid-line mb-16"></div>
            </el-col>
            <el-col :span="24">
              <div class="subtitle mb-16">私信用户</div>
            </el-col>
            <el-col :span="5">
              <el-form-item label="私信进度" prop="">
                <el-select
                  v-model="formData.privateMsgProgressCode"
                  placeholder=""
                  clearable
                  filterable
                  :options="singleEventStore.task_event_private_mst_staus"
                  :props="{ label: 'text', value: 'value' }"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="私信次数" prop="">
                <el-select
                  v-model="formData.privateMsgCount"
                  placeholder=""
                  clearable
                  filterable
                  :options="singleEventStore.task_event_private_mst_count"
                  :props="{ label: 'text', value: 'value' }"
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="私信渠道" prop="">
                <el-cascader
                  v-if="eventInfo.showType === 1"
                  v-model="formData.privateMsgChannel"
                  :options="singleEventStore.dataChannel"
                  :max-collapse-tags="1"
                  collapse-tags
                  :show-all-levels="false"
                  show-checked-strategy="parent"
                  filterable
                  clearable
                  placeholder=""
                  :props="{
                    label: 'name',
                    value: 'code',
                    children: 'child',
                    multiple: false,
                    emitPath: false,
                    checkStrictly: true
                  }"
                  class="w-full"
                  :disabled="isDisabled"
                />
                <el-select
                  v-if="eventInfo.showType === 2"
                  v-model="formData.privateMsgChannel"
                  clearable
                  placeholder=""
                  :disabled="isDisabled"
                >
                  <el-option value="1" label="1"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="客户姓名" prop="">
                <el-input
                  v-model.trim="formData.custName"
                  clearable
                  placeholder=""
                  :maxlength="10"
                  :disabled="isDisabled"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="手机号" prop="">
                <el-input
                  v-model.trim="formData.custMobile"
                  clearable
                  placeholder=""
                  :maxlength="20"
                  :disabled="isDisabled"
                ></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <div class="private-message-wrapper">
                <!-- privateMsgDetails -->
                <template v-for="(item, index) in privateMsgDetails" :key="index">
                  <el-col :span="24">
                    <div class="flex gap-24">
                      <el-form-item
                        :label="`第${numberToChinese(index + 1)}次私信时间`"
                        prop=""
                        class="flex-1"
                      >
                        <el-date-picker
                          v-model="item.privateMsgTime"
                          type="datetime"
                          placeholder=""
                          class="iw-full"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          clearable
                          :disabled="isDisabled"
                        />
                      </el-form-item>
                      <el-form-item
                        :label="`第${numberToChinese(index + 1)}次处理人员`"
                        prop=""
                        class="flex-1"
                      >
                        <el-input
                          v-if="eventInfo.showType === 2"
                          :model-value="item.userName"
                          class="flex-1"
                          :disabled="true"
                        />
                        <el-cascader
                          v-else
                          v-model="item.userId"
                          placeholder=""
                          clearable
                          filterable
                          :show-all-levels="false"
                          :options="handleUserCascaderOptions"
                          :props="handleUserCascaderProps"
                          class="flex-1"
                          :disabled="isDisabled"
                        />
                      </el-form-item>
                    </div>
                  </el-col>
                </template>
              </div>
            </el-col>
          </template>
          <!-- 关联工单 -->
          <!-- [EventType.HANDLE].includes(eventType) -->
          <!--  &&
              curEventTaskStatus(EventType.HANDLE) -->
          <template
            v-if="
              formData.unprocessedReason &&
              [HandleModeEnum.OnlyPrivateMsg, HandleModeEnum.ReplyAndPrivateMsg].includes(
                formData.unprocessedReason
              )
            "
          >
            <el-col :span="24">
              <div class="divid-line mx-16"></div>
            </el-col>
            <el-col :span="24">
              <div class="flex-between items-center mb-16">
                <div class="subtitle">关联工单</div>
                <div>
                  <!-- <el-button type="primary" plain>关联工单</el-button> -->
                  <!-- <el-button type="primary">
                <div class="flex items-center">
                  <SvgIcon name="plus" />
                  <span class="ml-8">创建工单</span>
                </div>
              </el-button> -->
                </div>
              </div>
            </el-col>
            <el-col :span="24">
              <RelatedTicket :eventType="eventType" :eventInfo="eventInfo"></RelatedTicket>
            </el-col>
          </template>
        </template>
      </el-row>
    </el-form>
    <!-- 查看标准话术 -->
    <StandardScript
      v-model="ssVisible"
      :tableData="all_candidates"
      @CopyAndApply="handleCopyAndApply"
    ></StandardScript>
  </div>
</template>

<style lang="scss" scoped>
.event-handle {
  .subtitle {
    font-weight: 400;
    font-size: 16px;
    color: rgba(0, 0, 0, 0.9);
    line-height: 24px;
  }

  .tip {
    font-weight: 400;
    font-size: 12px;
    color: #86909c;
    line-height: 20px;
  }

  .private-message-wrapper {
    width: 100%;
    background: #f5f7fa;
    border-radius: 4px 4px 4px 4px;
    padding: 16px;
  }

  .textarea-wrapper {
    position: relative;
    .textarea-content {
      resize: vertical;
      width: 100%;
      padding: 8px 16px 40px;
      border-radius: 4px;
      border: 1px solid #e4e7ed;
      min-height: 90px;
      height: 100px;
      display: block;
    }

    .toolbar {
      position: absolute;
      left: 16px;
      bottom: 8px;
      display: flex;
      gap: 10px;

      .tool-item {
        background: #eaf3ff;
        border-radius: 4px 4px 4px 4px;
        padding: 1px 8px;
        font-weight: 400;
        font-size: 14px;
        color: #1677ff;
        line-height: 22px;
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
      }
    }
  }
}
</style>
