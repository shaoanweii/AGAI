<script setup lang="ts">
import { computed } from 'vue'
import type { CustomerDirectEngageFilterFormData } from '../types'
import { useBatchEventOptions } from '../hooks/useBatchEventOptions'
import SameLevelExperienceCodeCascader from '../../shared/components/SameLevelExperienceCodeCascader.vue'

defineOptions({
  name: 'CustomerDirectEngageFilterForm'
})

interface Props {
  carSeriesOptions: any[]
  carSeriesOptionProps: Record<string, string>
  experienceCodeOptions: any[]
  topicOptions: any[]
}

interface Emits {
  (e: 'query'): void
  (e: 'reset'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = defineModel<CustomerDirectEngageFilterFormData>('formData', { required: true })
const times = defineModel<string[]>('times', { required: true })
const shortcutValue = defineModel<string>('shortcutValue', { required: true })
const isExpanded = defineModel<boolean>('isExpanded', { default: false })

const {
  brandOptions,
  closed_rule_priority,
  voc_intention,
  batch_event_status,
  batchEvent_reject_reason_type,
  batchEvent_close_reason_type,
  batchEvent_warning_rate,
  batchEvent_is_rejected,
  batchEvent_event_validity,
  filterDepartmentCascaderOptions,
  filterHandlerCascaderOptions,
  batchRuleCategoryTree,
  ensureFilterDepartmentCascaderOptions,
  ensureFilterHandlerCascaderOptions,
  loadBatchRuleCategoryTree,
  brandOptionProps
} = useBatchEventOptions()

const singleEventDictOptionProps = { label: 'text', value: 'value' }

/**
 * 是否驳回筛选在页面上单选，提交时沿用后端 isRejectedList 数组入参。
 */
const isRejectedValue = computed({
  get: () => formData.value.isRejectedList[0],
  set: value => {
    formData.value.isRejectedList = value ? [value] : []
  }
})

const departmentCascaderProps = {
  label: 'name',
  value: 'id',
  children: 'child',
  multiple: true,
  emitPath: false,
  checkStrictly: true
} as const

const handlerCascaderProps = {
  label: 'name',
  value: 'id',
  children: 'child',
  disabled: 'disabled',
  multiple: true,
  emitPath: false,
  checkStrictly: true
} as const

const subjectCategoryCascaderProps = {
  label: 'name',
  value: 'id',
  children: 'children',
  multiple: true,
  emitPath: false,
  checkStrictly: false
} as const

/**
 * 部门级联展开时兜底确认数据源已加载。
 * @param visible 下拉是否展开
 */
const handleDepartmentCascaderVisibleChange = (visible: boolean) => {
  if (visible) {
    void ensureFilterDepartmentCascaderOptions()
  }
}

/**
 * 处理人员级联展开时兜底确认数据源已加载。
 * @param visible 下拉是否展开
 */
const handleHandlerCascaderVisibleChange = (visible: boolean) => {
  if (visible) {
    void ensureFilterHandlerCascaderOptions()
  }
}

/**
 * 主题分类展开时兜底确认树形分类已加载。
 * @param visible 下拉是否展开
 */
const handleSubjectCategoryVisibleChange = (visible: boolean) => {
  if (visible) {
    void loadBatchRuleCategoryTree()
  }
}

const handleQuery = () => {
  emit('query')
}

const handleReset = () => {
  emit('reset')
}
</script>

<template>
  <FFilterLayout v-model="isExpanded" @query="handleQuery" @reset="handleReset">
    <el-form layout="inline" :model="formData" label-position="right">
      <el-row class="w-full" :gutter="24">
        <el-col :span="9">
          <el-form-item label="预警时间">
            <FDatePicker
              v-model="times"
              v-model:shortcutValue="shortcutValue"
              type="daterange"
              :clearable="false"
              class="iround-4"
              size="default"
            />
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="预警频率">
            <el-select
              v-model="formData.warningPeriods"
              placeholder="全部"
              clearable
              filterable
              multiple
              :max-collapse-tags="1"
              collapse-tags
              :options="batchEvent_warning_rate"
              :props="{ label: 'text', value: 'value' }"
            />
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="事件编号">
            <el-input
              v-model.trim="formData.warningEventNo"
              clearable
              placeholder="请输入"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="事件名称">
            <el-input
              v-model.trim="formData.eventName"
              clearable
              placeholder="请输入"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="主题分类">
            <el-cascader
              v-model="formData.subjectCategoryIds"
              placeholder="不限"
              clearable
              filterable
              :options="batchRuleCategoryTree"
              :props="subjectCategoryCascaderProps"
              :max-collapse-tags="1"
              collapse-tags
              :show-all-levels="false"
              show-checked-strategy="child"
              class="w-full"
              @visible-change="handleSubjectCategoryVisibleChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="处理优先级">
            <el-select
              v-model="formData.eventPriorities"
              placeholder="不限"
              clearable
              filterable
              :options="closed_rule_priority"
              :props="singleEventDictOptionProps"
              multiple
              :max-collapse-tags="1"
              collapse-tags
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="品牌">
            <el-select
              v-model="formData.brandCodes"
              placeholder="全部"
              clearable
              filterable
              multiple
              :max-collapse-tags="1"
              collapse-tags
              :options="brandOptions"
              :props="brandOptionProps"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="车系">
            <el-select
              v-model="formData.carSeriesCodes"
              placeholder="全部"
              clearable
              filterable
              multiple
              :max-collapse-tags="1"
              collapse-tags
              :options="props.carSeriesOptions"
              :props="props.carSeriesOptionProps"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="意图">
            <el-select
              v-model="formData.intentionList"
              placeholder="全部"
              clearable
              filterable
              multiple
              :max-collapse-tags="1"
              collapse-tags
              :options="voc_intention"
              :props="{ label: 'text', value: 'value' }"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="体验代码">
            <SameLevelExperienceCodeCascader
              v-model:first-code-tag="formData.firstCodeTag"
              v-model:second-code-tag="formData.secondCodeTag"
              v-model:three-code-tag="formData.threeCodeTag"
              v-model:four-code-tag="formData.fourCodeTag"
              :options="props.experienceCodeOptions"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="标准观点">
            <el-select-v2
              v-model="formData.topicList"
              placeholder="请选择"
              clearable
              filterable
              multiple
              :max-collapse-tags="1"
              collapse-tags
              :options="props.topicOptions"
              :props="{ label: 'tagName', value: 'tagName' }"
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="业务责任人">
            <el-input
              v-model.trim="formData.reviewUserName"
              placeholder="全部"
              clearable
              maxlength="20"
            />
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="主责部门">
            <el-cascader
              v-model="formData.mainRespOrgId"
              placeholder="不限"
              clearable
              filterable
              :options="filterDepartmentCascaderOptions"
              :props="departmentCascaderProps"
              :max-collapse-tags="1"
              collapse-tags
              :show-all-levels="false"
              show-checked-strategy="parent"
              class="w-full"
              @visible-change="handleDepartmentCascaderVisibleChange"
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="协同部门">
            <el-cascader
              v-model="formData.collaborateOrgIds"
              placeholder="不限"
              clearable
              filterable
              :options="filterDepartmentCascaderOptions"
              :props="departmentCascaderProps"
              :max-collapse-tags="1"
              collapse-tags
              :show-all-levels="false"
              show-checked-strategy="parent"
              class="w-full"
              @visible-change="handleDepartmentCascaderVisibleChange"
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="处理人员">
            <el-cascader
              v-model="formData.handlerUserIds"
              placeholder="不限"
              clearable
              filterable
              :options="filterHandlerCascaderOptions"
              :props="handlerCascaderProps"
              :max-collapse-tags="1"
              collapse-tags
              :show-all-levels="false"
              class="w-full"
              @visible-change="handleHandlerCascaderVisibleChange"
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="事件状态">
            <el-select
              v-model="formData.taskStatuses"
              placeholder="不限"
              clearable
              filterable
              :options="batch_event_status"
              :props="singleEventDictOptionProps"
              multiple
              :max-collapse-tags="1"
              collapse-tags
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="是否驳回">
            <el-select
              v-model="isRejectedValue"
              placeholder="不限"
              clearable
              filterable
              :options="batchEvent_is_rejected"
              :props="{ label: 'text', value: 'value' }"
            />
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="驳回原因">
            <el-select
              v-model="formData.rejectReasonList"
              placeholder="不限"
              clearable
              filterable
              :options="batchEvent_reject_reason_type"
              :props="{ label: 'text', value: 'value' }"
              multiple
              :max-collapse-tags="1"
              collapse-tags
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="事件属性">
            <el-select
              v-model="formData.eventAttributeList"
              placeholder="不限"
              clearable
              filterable
              :options="batchEvent_close_reason_type"
              :props="{ label: 'text', value: 'value' }"
              multiple
              :max-collapse-tags="1"
              collapse-tags
            />
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-form-item label="事件有效性">
            <el-select
              v-model="formData.eventValidityList"
              placeholder="不限"
              clearable
              filterable
              :options="batchEvent_event_validity"
              :props="singleEventDictOptionProps"
              multiple
              :max-collapse-tags="1"
              collapse-tags
            />
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="创建人员">
            <el-input
              v-model.trim="formData.createUser"
              placeholder="全部"
              clearable
              maxlength="20"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </FFilterLayout>
</template>
