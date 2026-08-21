<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted, toRaw, nextTick } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { singleEventStore, singleEventActions, singleRuleTypeValue } from './store'
import { cloneDeep, mergeWith } from 'lodash-es'
import { insertRule, updateRule, getRuleDetail } from '@/api/rules'
import { useRuleValidation } from '../hooks/useRuleValidation'
import DeptUserSelect from '../components/DeptUserSelect.vue'
import CcPersonnelSelect from '../components/CcPersonnelSelect.vue'
import ConditionConfig from './ConditionConfig.vue'
import { ConditionType } from '../components/constants'

defineOptions({ name: 'RuleForm' })

const emit = defineEmits<{
  success: []
}>()

// 预警时间默认值
const ALERT_TIME = { REALTIME_ZERO: '0' as const, DEFAULT_TIME: '08:00:00' as const }

interface Props {
  ruleData?: any
  visible?: boolean
  disabled?: boolean //详情场景
}
const props = withDefaults(defineProps<Props>(), {
  ruleData: null,
  visible: false,
  disabled: false
})
const hub = reactive({
  // 加载规则详情的loading
  loading: false
})

// 表单与校验
const formRef = ref<FormInstance>()

// 中文注释：表单初始数据使用 mock 值
let form = reactive({
  // 规则主键, 更新时必填
  ruleId: '',
  // 规则名称
  ruleName: '',
  // 数据来源
  dataSource: [],
  // 规则类型,数据字典-closedRuleType
  ruleType: '',
  // 分类ID
  categoryType: '',
  // 分类名称
  categoryTypeName: '',
  // 品牌编码
  brandCode: '',
  // 品牌名称
  brandName: '',
  // 事件等级,数据字典-closedRuleLevel
  eventLevel: '',
  // 处理优先级,数据字典-closedRulePriority
  processPriority: '',
  // 审核方式,数据字典-closedRuleAuditMethod
  auditMethod: '',
  // 审核人部门
  auditDepartment: null,
  // auditDepartment: {
  //   id: '', // 组织ID
  //   deptNo: '', // 部门编号
  //   name: '' // 部门名称
  // },
  // 审核人
  auditor: null,
  // auditor: {
  //   id: '', // 用户ID
  //   employeeId: '', // 工号
  //   name: '' // 用户姓名
  // },
  // 主责部门
  mainDepartment: null,
  // mainDepartment: {
  //   id: '', // 组织ID
  //   deptNo: '', // 部门编号
  //   name: '' // 部门名称
  // },
  // 主责人
  mainResponder: null,
  // mainResponder: {
  //   id: '', // 用户ID
  //   employeeId: '', // 工号
  //   name: '' // 用户姓名
  // },
  // 抄送人员
  /**
   * id: '', // 组织ID
    deptNo: '', // 部门编号
    deptName: '', // 部门名称
    isAll: undefined, // 是否部门全部人员
    userId: '', // 用户ID
    employeeId: '', // 工号
    userName: '' // 用户姓名
   */
  ccPersonnel: [] as any[],
  // 确认方式,数据字典-closedRuleConfirmMethod
  confirmMethod: '',
  //确认部门
  confirmDepartment: null,
  // confirmDepartment: {
  //   id: '', // 组织ID
  //   deptNo: '', // 部门编号
  //   name: '' // 部门名称
  // },
  // 确认人
  confirmer: null,
  // confirmer: {
  //   id: '', // 用户ID
  //   employeeId: '', // 工号
  //   name: '' // 用户姓名
  // },
  // 规则是否启用,数据字典-closedRuleEnabledStatus
  isEnabled: '',
  // 条件配置列表
  conditions: [],
  // 预警配置
  ruleAlert: null as any
  // ruleAlert: {
  //   alertType: '', // 预警周期：hourly=时，daily=日，weekly=周，monthly=月
  //   alertFrequency: '', // 预警频次
  //   alertTime: '', // 预警时间
  //   alertCron: '', // Cron表达式
  //   alertChannel: [] // 预警渠道（多选，存储渠道标识数组）,数据字典-closedRuleAlertChannel
  // }
})

const initialForm = cloneDeep(toRaw(form))

// 表单重置：恢复到初始值并清空校验状态
function resetForm() {
  //关闭弹框  重置表单
  formRef.value?.resetFields()
  //form 置空
  Object.keys(form).forEach(key => {
    form[key] = null
  })
  Object.assign(form, cloneDeep(initialForm))
}

// 是否为编辑态（兼容 id 与 ruleId）
const isEdit = computed(
  () => !!props.ruleData && (!!(props.ruleData as any).id || !!(props.ruleData as any).ruleId)
)
// 是否为单点规则
const isSingleRule = computed(() => form.ruleType === singleRuleTypeValue.value)

// 校验（抽离）
const { rules } = useRuleValidation(form, isSingleRule)

// 审核方式
const closedRuleAuditMethod = computed(() =>
  [...(singleEventStore.conditions.closedRuleAuditMethod || [])].reverse()
)
//确认方式
const closedRuleConfirmMethod = computed(() =>
  [...(singleEventStore.conditions.closedRuleConfirmMethod || [])].reverse()
)

// 打开弹框时，将编辑数据（若有）回填
watch(
  () => props.visible,
  v => {
    if (!v) {
      //关闭弹框  重置表单
      resetForm()
      return
    }
    nextTick(() => {
      if (props.ruleData) {
        if (isEdit.value) {
          const rid: any = (props.ruleData as any).ruleId
          if (rid) fetchRuleDetail(rid)
        } else {
          // 深拷贝 + 仅用有效值覆盖默认表单，null/undefined 不覆盖
          mergeKeepDefault(form, cloneDeep(props.ruleData))
        }
      }
    })
  },
  { immediate: true }
)

onMounted(() => {
  singleEventActions.updateDepartList()
})

// 详情：编辑时根据 ruleId 拉取后端数据并回填
const fetchRuleDetail = async (ruleId: string | number) => {
  try {
    hub.loading = true
    const response: any = await getRuleDetail(ruleId)
    if (response?.success && response?.result) {
      //结果中conditions里面的数据需要解析一下
      const result = response.result || {}
      result.conditions = (result.conditions || []).map((item: any) => {
        let value: any = item.value
        if (item.valueType === 'array') {
          // 数组类型按原有约定解析
          try {
            value = JSON.parse(item.value || '[]')
          } catch {
            value = []
          }
        } else if (item.valueType === 'object') {
          try {
            value = JSON.parse(item.value || '{}')
          } catch {
            value = {}
          }
        }
        return {
          ...item,
          value
        }
      })
      // 使用保留默认值的合并策略，避免后端返回 null/undefined 覆盖本地默认
      mergeKeepDefault(form, cloneDeep(result))
    } else {
      ElMessage.error(response?.message || '获取规则详情失败')
      // 兜底：回填传入的简要 ruleData
      if (props.ruleData) mergeKeepDefault(form, cloneDeep(props.ruleData))
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取规则详情失败')
    if (props.ruleData) mergeKeepDefault(form, cloneDeep(props.ruleData))
  } finally {
    hub.loading = false
  }
}

// 合并时保留默认值：仅当 source 值不是 null/undefined 时才覆盖；数组按整段替换
function mergeKeepDefault(target: any, source: any) {
  if (!source) return target
  return mergeWith(target, source, (objValue: any, srcValue: any) => {
    if (srcValue === null || srcValue === undefined) return objValue
    if (Array.isArray(srcValue)) return srcValue
    // 其它类型按 lodash 默认深合并
    return undefined
  })
}

// 切换类型时重置主题及分支表单
const onTypeChange = () => {
  // form.theme = themeMap[form.ruleType][0]
  // if (form.ruleType === singleRuleTypeValue) {
  //   if (form.conditions.length === 0) form.conditions.push(createCond())
  // } else {
  //   if (form.batchDimensions.length === 0) form.batchDimensions.push(createCond())
  //   if (form.batchMetrics.length === 0) form.batchMetrics.push(newMetric())
  // }
}

//切换品牌时，条件配置中的车系重置
const brandChange = () => {
  if (form.conditions?.length) {
    form.conditions.forEach((condition: any) => {
      // 如果条件类型是车系(carSeries)，则重置其值
      if (condition.conditionType === ConditionType.CAR_SERIES) {
        condition.value = []
      }
    })
  }
}

// 抄送人员变化时触发表单校验
const handleCcChange = () => {
  formRef.value?.validateField?.('ccPersonnel')
}

// 条件类型重复性检测
const dayOptions = Array.from({ length: 31 }, (_, i) => ({
  key: `${i + 1}`,
  value: `${i + 1}日`
}))

//预警信息配置
const ALERT_FIELD_CONFIG = {
  //时
  hourly: {
    alertFrequencys: [
      //预警频次
      { key: '2', value: '2' },
      { key: '4', value: '4' },
      { key: '8', value: '8' },
      { key: '16', value: '16' }
    ],
    alertTimeType: 0 //实时  其他类型是时间选择器
  },
  //日
  daily: {
    alertFrequencys: [
      //预警频次
      { key: '0', value: '每日' }
    ],
    alertTimeType: 1 //实时  其他类型是时间选择器
  },
  //周
  weekly: {
    alertFrequencys: [
      //预警频次
      { key: '1', value: '周一' },
      { key: '2', value: '周二' },
      { key: '3', value: '周三' },
      { key: '4', value: '周四' },
      { key: '5', value: '周五' },
      { key: '6', value: '周六' },
      { key: '7', value: '周日' }
    ],
    alertTimeType: 1 //实时  其他类型是时间选择器
  },
  //月
  monthly: {
    alertFrequencys: dayOptions,
    alertTimeType: 1 //实时  其他类型是时间选择器
  }
}

//预警推送分类
const alertTypeChange = (val: string) => {
  // 根据不同告警类型，重置频率与时间默认值，避免残留无效值
  const cfg: any = (ALERT_FIELD_CONFIG as any)[val]
  if (!cfg) return
  if (!form.ruleAlert) form.ruleAlert = {}
  form.ruleAlert.alertFrequency = cfg.alertFrequencys?.[0]?.key ?? ''
  form.ruleAlert.alertTime =
    cfg.alertTimeType === 0 ? ALERT_TIME.REALTIME_ZERO : ALERT_TIME.DEFAULT_TIME
}

// // 操作：增删行（批量-维度）
// const addDimension = () => form.batchDimensions.push(createCond())
// const removeDimension = (idx: number) => form.batchDimensions.splice(idx, 1)

// // 操作：增删行（批量-指标）
// const addMetric = () => form.batchMetrics.push(newMetric())
// const removeMetric = (idx: number) => form.batchMetrics.splice(idx, 1)

// 统一将 conditions 转为后端需要的结构
function serializeConditionsForApi(list: any[] = []) {
  return (list || []).map((condition: any) => {
    const next: any = { ...condition }
    if (Array.isArray(condition.value)) {
      next.valueType = 'array'
      next.value = JSON.stringify(condition.value)
    } else if (condition.value != null && typeof condition.value === 'object') {
      next.valueType = typeof condition.value
      next.value = JSON.stringify(condition.value)
    } else {
      next.valueType = 'string'
      next.value = condition.value
    }
    return next
  })
}
// 组装提交载荷（新实现）
const preparePayload = () => {
  const { ...rest } = form

  //由主责部门/主责人承担确认职责
  const payload: any = {
    ...rest,
    confirmDepartment: form.mainDepartment,
    confirmer: form.mainResponder
  }

  payload.conditions = serializeConditionsForApi(form.conditions || [])

  return payload
}

// 确定保存
const onConfirm = async ({ close }: { close: () => void }) => {
  // 表单校验
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 二次业务校验：条件类型不可重复
  // if (form.auditMethod === RuleAssignMethod.MANUAL && !(form.auditor && form.auditor.id)) {
  //   ElMessage.error('请选择审核人')
  //   return
  // }
  // if (form.confirmMethod === RuleAssignMethod.MANUAL && !(form.confirmer && form.confirmer.id)) {
  //   ElMessage.error('请选择确认人')
  //   return
  // }
  try {
    // 获取准备好的 payload
    const payload = preparePayload()

    let response: any = null
    if (isEdit.value) {
      response = await updateRule(payload)
    } else {
      response = await insertRule(payload)
    }
    if (response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
      emit('success')
      close()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}
defineExpose({ onConfirm })
</script>

<template>
  <el-form
    v-loading="hub.loading"
    ref="formRef"
    :model="form"
    :rules="rules"
    :disabled="disabled"
    label-width="104px"
  >
    <!-- 规则名称：类型 + 主题 + 输入框 -->
    <el-form-item label="规则名称" prop="ruleName">
      <div class="inline-row rule-name-class">
        <!-- <el-select v-model="form.ruleType" class="w-80" @change="onTypeChange">
          <el-option
            v-for="opt in singleEventStore.conditions.closedRuleType"
            :key="opt.key"
            :label="opt.value"
            :value="opt.key"
          />
        </el-select>
        <el-select v-model="form.categoryType" class="w-108">
          <el-option
            v-for="opt in [{ key: form.categoryType, value: form.categoryTypeName }]"
            :key="opt.key"
            :label="opt.value"
            :value="opt.key"
          />
        </el-select> -->
        <div class="flex-1">
          <el-input v-model.trim="form.ruleName" placeholder="请输入" />
        </div>
      </div>
    </el-form-item>

    <!-- 数据来源 -->
    <el-form-item label="数据来源" prop="dataSource">
      <el-cascader
        v-model="form.dataSource"
        :options="singleEventStore.channelTree"
        :max-collapse-tags="2"
        collapse-tags
        collapse-tags-tooltip
        :show-all-levels="false"
        show-checked-strategy="parent"
        filterable
        clearable
        :props="{
          label: 'name',
          value: 'code',
          children: 'child',
          multiple: true,
          emitPath: false,
          checkStrictly: false
        }"
        class="w-full"
      />
    </el-form-item>
    <el-form-item label="品牌" prop="brandCode">
      <el-select v-model="form.brandCode" class="w-full" @change="brandChange">
        <el-option
          v-for="opt in singleEventStore.conditions.selfBrand"
          :key="opt.key"
          :label="opt.value"
          :value="opt.key"
        />
      </el-select>
    </el-form-item>

    <!-- 单点：条件配置 -->
    <template v-if="form.ruleType === singleRuleTypeValue">
      <ConditionConfig v-model:conditions="form.conditions" :brandCode="form.brandCode" />
    </template>

    <!-- 批量：维度配置 + 指标配置 -->
    <!-- <template v-else>
        <el-form-item label="维度配置">
          <div class="block-panel">
            <div class="cond-row" v-for="(row, idx) in form.batchDimensions" :key="row.key">
              <el-select v-model="row.field" class="w-100" @change="() => resetCondRowByField(row)">
                <el-option v-for="f in condFieldOptions" :key="f" :label="f" :value="f" />
              </el-select>
              <el-select v-model="row.wildcard" class="w-100">
                <el-option
                  v-for="op in getWildcardOpsByField(row.field)"
                  :key="op"
                  :label="op"
                  :value="op"
                />
              </el-select>
              <el-select
                v-model="row.valueType"
                class="w-100"
                @change="() => onValueTypeChange(row)"
              >
                <el-option
                  v-for="t in getValueTypesByField(row.field)"
                  :key="t"
                  :label="t"
                  :value="t"
                />
              </el-select>
              <div class="flex-1">
                <template v-if="getInputComponent(row) === InputComponentEnum.SelectSingle">
                  <el-select v-model="row.value" filterable clearable>
                    <el-option
                      v-for="v in getSelectOptions(row, form.batchDimensions)"
                      :key="v.value"
                      :label="v.label"
                      :value="v.value"
                    />
                  </el-select>
                </template>
                <template v-else-if="getInputComponent(row) === InputComponentEnum.SelectMultiple">
                  <el-select v-model="row.value" multiple filterable clearable>
                    <el-option
                      v-for="v in getSelectOptions(row, form.batchDimensions)"
                      :key="v.value"
                      :label="v.label"
                      :value="v.value"
                    />
                  </el-select>
                </template>
                <template v-else-if="getInputComponent(row) === InputComponentEnum.Input">
                  <el-input
                    v-model.trim="row.value"
                    :maxlength="row.valueType === '值' ? 50 : undefined"
                    show-word-limit
                    placeholder="请输入"
                  />
                </template>
                <template v-else-if="getInputComponent(row) === InputComponentEnum.CascaderSingle">
                  <el-cascader
                    v-model="row.value"
                    :options="expCodeTree"
                    :props="{
                      checkStrictly: true,
                      emitPath: false,
                      multiple: false,
                      value: 'tagCode',
                      label: 'tagName',
                      children: 'child'
                    }"
                    clearable
                    class="w-full"
                  />
                </template>
                <template
                  v-else-if="getInputComponent(row) === InputComponentEnum.CascaderMultiple"
                >
                  <el-cascader
                    v-model="row.value"
                    :options="accountLexiconTree"
                    :props="{ multiple: true, checkStrictly: true }"
                    clearable
                    class="w-full"
                  />
                </template>
              </div>
              <el-button link @click="removeDimension(idx)"
                ><el-icon><CloseBold /></el-icon
              ></el-button>
            </div>
            <div class="w-136">
              <el-button type="primary" @click="addDimension">+ 添加维度配置</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="指标配置">
          <div class="block-panel">
            <div class="metric-row" v-for="(row, idx) in form.batchMetrics" :key="row.key">
              <el-select v-model="row.joiner" class="w-80">
                <el-option label="AND" value="AND" />
                <el-option label="OR" value="OR" />
              </el-select>
              <el-select v-model="row.metric" class="w-100">
                <el-option v-for="m in metricOptions" :key="m" :label="m" :value="m" />
              </el-select>
              <el-select v-model="row.leftValueType" class="w-80">
                <el-option v-for="t in valueTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
              <el-select v-model="row.op" class="w-64">
                <el-option v-for="op in numberOps" :key="op" :label="op" :value="op" />
              </el-select>
              <el-select v-model="row.rightValueType" class="w-80">
                <el-option v-for="t in valueTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
              <div class="flex-1">
                <el-input-number
                  v-model="row.value"
                  :min="0"
                  :controls="false"
                  style="width: 100%"
                />
              </div>
              <el-button link @click="removeMetric(idx)"
                ><el-icon><CloseBold /></el-icon
              ></el-button>
            </div>
            <div class="w-136">
              <el-button type="primary" @click="addMetric">+ 添加指标配置</el-button>
            </div>
          </div>
        </el-form-item>
      </template>
      -->

    <!-- 共同字段：敏感度、事件形态、事件等级、处理优先级 -->
    <template v-if="form.ruleType !== singleRuleTypeValue">
      <!-- <el-form-item label="敏感类型">
          <el-select v-model="form.sensitivity">
            <el-option v-for="opt in sensitivityOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item> -->
      <!-- <el-form-item label="事件形态">
          <el-select v-model="form.eventForm">
            <el-option v-for="opt in eventFormOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item> -->
    </template>
    <el-form-item label="事件等级" prop="eventLevel">
      <el-select v-model="form.eventLevel">
        <el-option
          v-for="opt in singleEventStore.conditions.closedRuleLevel"
          :key="opt.key"
          :label="opt.value"
          :value="opt.key"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="处理优先级" prop="processPriority">
      <el-select v-model="form.processPriority">
        <el-option
          v-for="opt in singleEventStore.conditions.closedRulePriority"
          :key="opt.key"
          :label="opt.value"
          :value="opt.key"
        />
      </el-select>
    </el-form-item>

    <!-- 审核人员 -->
    <el-form-item label="审核人员" prop="auditDepartment">
      <DeptUserSelect
        key="audit"
        v-model:deptModel="form.auditDepartment"
        v-model:userModel="form.auditor"
        :dept-options="singleEventStore.departOptions"
        value-key="id"
        label-key="name"
        dept-placeholder="部门"
        user-placeholder="选择审核人"
      />
    </el-form-item>
    <!-- 审核方式 -->
    <el-form-item label="审核方式" prop="auditMethod">
      <el-radio-group v-model="form.auditMethod" class="radio-line">
        <el-radio v-for="opt in closedRuleAuditMethod" :key="opt.key" :value="opt.key">{{
          opt.value
        }}</el-radio>
      </el-radio-group>
    </el-form-item>

    <!-- 负责人、抄送 -->
    <el-form-item label="主责部门" prop="mainDepartment">
      <DeptUserSelect
        key="main"
        v-model:deptModel="form.mainDepartment"
        v-model:userModel="form.mainResponder"
        :dept-options="singleEventStore.departOptions"
        value-key="id"
        label-key="name"
        dept-placeholder="部门"
        user-placeholder="人员"
      />
    </el-form-item>

    <!-- 确认方式 -->
    <template v-if="form.ruleType === singleRuleTypeValue">
      <el-form-item label="确认方式" prop="confirmMethod">
        <el-radio-group v-model="form.confirmMethod" class="radio-line">
          <el-radio v-for="opt in closedRuleConfirmMethod" :key="opt.key" :value="opt.key">{{
            opt.value
          }}</el-radio>
          <!-- <DeptUserSelect
              key="confirm"
              v-if="form.confirmMethod === 'manual'"
              v-model:deptModel="form.confirmDepartment"
              v-model:userModel="form.confirmer"
              :dept-options="singleEventStore.departOptions"
              value-key="id"
              label-key="name"
              dept-placeholder="部门"
              user-placeholder="选择确认人"
            /> -->
        </el-radio-group>
      </el-form-item>
    </template>

    <el-form-item label="抄送人员" prop="ccPersonnel">
      <CcPersonnelSelect v-model="form.ccPersonnel" @change="handleCcChange" />
    </el-form-item>

    <!-- 预警推送 -->
    <el-form-item prop="ruleAlert" v-if="false && form.ruleAlert" label="预警推送">
      <div class="inline-row">
        <el-select
          v-model="form.ruleAlert.alertType"
          @change="alertTypeChange"
          placeholder="分类"
          class="w-80"
        >
          <el-option label="时" value="hourly" />
          <el-option label="日" value="daily" />
          <el-option label="周" value="weekly" />
          <el-option label="月" value="monthly" />
        </el-select>
        <el-select v-model="form.ruleAlert.alertFrequency" placeholder="周期" class="w-80">
          <template v-if="ALERT_FIELD_CONFIG[form.ruleAlert.alertType]">
            <el-option
              v-for="option in ALERT_FIELD_CONFIG[form.ruleAlert.alertType].alertFrequencys"
              :key="option.key"
              :label="option.value"
              :value="option.key"
            />
          </template>
        </el-select>
        <el-select
          v-if="
            ALERT_FIELD_CONFIG[form.ruleAlert.alertType] &&
            ALERT_FIELD_CONFIG[form.ruleAlert.alertType].alertTimeType === 0
          "
          v-model="form.ruleAlert.alertTime"
          class="w-124"
        >
          <el-option label="实时" value="0" />
        </el-select>
        <el-time-picker
          v-else
          v-model="form.ruleAlert.alertTime"
          placeholder="时间"
          value-format="HH:mm:ss"
          style="width: 124px"
        />
        <el-select v-model="form.ruleAlert.alertChannel" multiple collapse-tags class="w-100">
          <el-option
            v-for="opt in singleEventStore.conditions.closedRuleAlertChannel"
            :key="opt.key"
            :label="opt.value"
            :value="opt.key"
          />
        </el-select>
      </div>
    </el-form-item>

    <!-- 批量独有：处理方式 -->
    <!-- <el-form-item prop="handleType" v-if="form.ruleType !== singleRuleTypeValue" label="处理方式">
        <el-radio-group v-model="form.handleType" class="radio-line">
          <el-radio value="KTM">KTM</el-radio>
          <el-radio value="VOC">VOC</el-radio>
          <el-radio value="接驳器">接驳器</el-radio>
        </el-radio-group>
      </el-form-item> -->

    <!-- 是否启用 -->
    <el-form-item label="是否启用" prop="isEnabled">
      <el-radio-group v-model="form.isEnabled">
        <el-radio
          v-for="item in singleEventStore.conditions.closedRuleEnabledStatus"
          :key="item.key"
          :value="item.key"
          >{{ item.value }}</el-radio
        >
      </el-radio-group>
    </el-form-item>
  </el-form>
</template>
<style lang="scss" scoped>
.inline-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.radio-line {
  display: flex;
  align-items: center;
  gap: 8px;
}
.flex-1 {
  flex: 1 1 auto;
}
.w-64 {
  width: 64px;
}
.w-80 {
  width: 80px;
}
.w-100 {
  width: 100px;
}
.w-108 {
  width: 108px;
}
.w-120 {
  width: 120px;
}
.w-124 {
  width: 124px;
}
.w-136 {
  width: 136px;
}
.w-140 {
  width: 140px;
}
.w-160 {
  width: 160px;
}
.w-180 {
  width: 180px;
}
.w-200 {
  width: 200px;
}
.w-420 {
  width: 420px;
}
.ml-12 {
  margin-left: 12px;
}

/* 中文注释：块状容器，用于模拟效果图中的灰底卡片 */
.block-panel {
  width: 100%;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cond-row,
.metric-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.el-form-item:last-child {
  margin-bottom: 0 !important;
}

.select-input-class {
  flex: 1 1 0;
}

.rule-name-class {
  gap: 0 !important;
  border-radius: 4px;
  border: 1px solid var(--el-border-color);
  :deep(.el-select) {
    border-right: 1px solid var(--el-border-color);
  }
  :deep(.el-select__wrapper) {
    box-shadow: none !important;
  }
  :deep(.el-input__wrapper) {
    box-shadow: none !important;
  }
}
:deep(.el-select__placeholder) {
  font-weight: 400 !important;
  font-size: 14px !important;
  color: #1d2129 !important;
}
:deep(.el-select__wrapper.is-disabled .el-select__placeholder) {
  color: var(--el-disabled-text-color) !important;
}
:deep(.el-select__placeholder.is-transparent) {
  color: #929aa6 !important;
}
:deep(.el-select__caret) {
  color: #4e5969 !important;
}
:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #1d2129 !important;
}
:deep(.el-radio.is-disabled .el-radio__label) {
  color: var(--el-disabled-text-color) !important;
}
:deep(.el-radio) {
  color: #1d2129 !important;
  margin-right: 16px !important;
}
:deep(.el-radio:last-child) {
  margin-right: 0 !important;
}
:deep(.el-input-number .el-input__inner) {
  text-align: left !important;
}
:deep(.el-input__inner) {
  color: #1d2129 !important;
}
</style>
