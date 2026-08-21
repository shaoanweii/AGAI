<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { singleEventStore, singleEventActions } from './store'
import { findConditionConfig, getTagLibStandardView } from '@/api/rules'
import ConditionValueInput from './ConditionValueInput.vue'

import { ConditionType, InputComponentEnum, InputOptionEnum } from '../components/constants'
import type { ConditionRow } from '../components/types'

defineOptions({ name: 'ConditionConfig' })

// 中文注释：条件列表由外部 v-model 双向绑定
const conditions = defineModel<ConditionRow[]>('conditions', { default: () => [] })
const props = defineProps<{ brandCode: string }>()

// 中文注释：组件内部状态（条件元数据、体验代码、标准观点等）
const hub = reactive({
  // 条件配置数组及映射表（用于根据字段 code 查找配置）
  conditionConfig: [] as any[],
  conditionConfigMap: {} as Record<string, any>,
  // 体验代码 / 标准观点相关数据
  tagLibOptions: [] as any[],
  tagStandardLoading: false as boolean,
  tagLibStandardOptionsAll: [] as any[],
  tagLibStandardOptions: [] as any[]
})

/**
 * 体验代码变更时，清空标准观点缓存与取值
 */
function onExperienceCodeChange() {
  hub.tagLibStandardOptionsAll = []
  hub.tagLibStandardOptions = []
  conditions.value.forEach((c: any) => {
    if (c.conditionType === ConditionType.STANDPOINT) c.value = ''
  })
}

/**
 * 标准观点远程搜索
 */
async function onStandpointRemoteMethod(query?: string) {
  // 根据当前体验代码条件，取其最后一层 id 作为查询父级
  const experienceCond = conditions.value.find(
    (tag: any) => tag.conditionType === ConditionType.EXPERIENCE_CODE
  ) as any

  let experienceCode = ''
  const rawValue = experienceCond?.value
  if (rawValue && typeof rawValue === 'object') {
    const keys = Object.keys(rawValue)
      .filter(k => !Number.isNaN(Number(k)))
      .sort((a, b) => Number(a) - Number(b))
    if (keys.length) {
      const lastKey = keys[keys.length - 1]
      experienceCode = String(rawValue[lastKey] ?? '')
    }
  } else if (rawValue != null && rawValue !== '') {
    // 兼容旧数据：旧版本体验代码为直接存储末级 id
    experienceCode = String(rawValue)
  }
  const experienceCodeId = singleEventStore.tagLibOptionsMap[experienceCode]?.id || ''

  const needFetch =
    !Array.isArray(hub.tagLibStandardOptionsAll) || hub.tagLibStandardOptionsAll.length === 0
  if (needFetch) {
    try {
      hub.tagStandardLoading = true
      const { result } = await getTagLibStandardView({
        tagType: 'CA',
        tagParentId: experienceCodeId || ''
      })
      hub.tagLibStandardOptionsAll = (result || []).map((item: any) => ({
        label: item.tagName,
        value: item.tagCode
      }))
    } catch (e) {
      hub.tagLibStandardOptionsAll = []
    } finally {
      hub.tagStandardLoading = false
    }
  }
  const q = String(query || '').toLowerCase()
  hub.tagLibStandardOptions = q
    ? hub.tagLibStandardOptionsAll.filter((tag: any) =>
        String(tag.label || '')
          .toLowerCase()
          .includes(q)
      )
    : hub.tagLibStandardOptionsAll.slice()
}

// 中文注释：根据条件类型配置输入组件与 options 等字段
const COND_FIELD_CONFIG = computed(() => ({
  [ConditionType.AD_TYPE]: {
    input: InputComponentEnum.SelectMultiple,
    // 中文注释：广告类型字典走闭环规则 conditions，结构与 vocSentiment 一致
    options: singleEventStore.conditions.batchAdType || [],
    props: {
      value: 'key',
      label: 'value'
    }
  },
  [ConditionType.CAR_SERIES]: {
    input: InputComponentEnum.SelectMultiple,
    options:
      singleEventStore.conditions.selfBrandCar?.find(tag => tag.key === props.brandCode)
        ?.children || [],
    props: {
      value: 'key',
      label: 'value'
    }
  },
  regulation_content_type: {
    input: InputComponentEnum.CascaderSingle,
    options: singleEventStore.conditions.contentType || [],
    props: {
      value: 'key',
      label: 'value',
      children: 'children'
    }
  },
  publish_user: {
    input: InputComponentEnum.CascaderMultiple,
    loading: singleEventStore.dataResource.loading,
    options: singleEventStore.dataResource.accountList
  },
  original_post_user: {
    input: InputComponentEnum.CascaderMultiple,
    loading: singleEventStore.dataResource.loading,
    options: singleEventStore.dataResource.accountList
  },
  [ConditionType.EXPERIENCE_CODE]: {
    input: InputComponentEnum.CascaderSingle,
    options: singleEventStore.tagLibOptions || [],
    change: onExperienceCodeChange
  },
  [ConditionType.STANDPOINT]: {
    input: InputComponentEnum.SelectSingle,
    remote: true,
    loading: hub.tagStandardLoading,
    remoteMethod: onStandpointRemoteMethod,
    options: hub.tagLibStandardOptions
  },
  emotion: {
    input: InputComponentEnum.SelectSingle,
    options: singleEventStore.conditions.vocSentiment || [],
    props: {
      value: 'key',
      label: 'value'
    }
  },
  intention: {
    input: InputComponentEnum.SelectSingle,
    options: singleEventStore.conditions.vocIntention || [],
    props: {
      value: 'key',
      label: 'value'
    }
  },
  title: {
    input: (row: any) =>
      row.option === InputOptionEnum.value
        ? InputComponentEnum.Input
        : InputComponentEnum.CascaderMultiple,
    maxlength: 50,
    prefix: '@',
    loading: singleEventStore.dataResource.loading,
    options: singleEventStore.dataResource.allList || []
  },
  content: {
    input: (row: any) =>
      row.option === InputOptionEnum.value
        ? InputComponentEnum.Input
        : InputComponentEnum.CascaderMultiple,
    maxlength: 50,
    prefix: '@',
    loading: singleEventStore.dataResource.loading,
    options: singleEventStore.dataResource.allList || []
  }
}))

// 中文注释：拉取条件配置元数据
const fetchConditionConfig = async () => {
  try {
    const resp = await findConditionConfig()
    if (resp.success) {
      hub.conditionConfig = resp.result || []
      hub.conditionConfigMap = {}
      hub.conditionConfig.forEach((cfg: any) => (hub.conditionConfigMap[cfg.code] = cfg))
    }
  } catch (e) {
    console.error(e)
  }
}

// 统一根据字段获取操作符与取值选项
const getWildcardOpsByField = (field: string) =>
  hub.conditionConfigMap[field]?.logicalOperator || []
const getOptionsByField = (field: string) => hub.conditionConfigMap[field]?.condition || []

// 根据当前行的配置决定渲染的输入组件类型
const getInputComponent = (row: any): any => {
  const cfg = COND_FIELD_CONFIG.value[row.conditionType]
  if (!cfg) return InputComponentEnum.Input as any
  return typeof cfg.input === 'function' ? (cfg.input as any)(row) : (cfg.input as any)
}

// 初始化条件行
const createCond = (): ConditionRow => ({
  conditionType: '', // 条件字段
  operator: '', // 操作符，数据字典-closedRuleConditionOperator
  option: '', // 取值方式，数据字典-closedRuleConditionOption
  valueType: '', // 值类型，数据字典-closedRuleConditionValueType
  value: '', // 条件值，具体结构由 valueType 决定
  sortOrder: conditions.value.length
})

// 字段切换时重置操作符 / 取值方式 / 值
const resetCondRowByField = (row: any) => {
  row.operator = getWildcardOpsByField(row.conditionType)?.[0]?.code || ''
  row.option = getOptionsByField(row.conditionType)?.[0]?.code || ''
  const input = getInputComponent(row)
  if (input === InputComponentEnum.SelectMultiple) row.value = []
  else if (input === InputComponentEnum.CascaderMultiple) row.value = null
  else if (input === InputComponentEnum.CascaderSingle) row.value = null
  else row.value = ''
}

// 取值方式切换时重置值
const onOptionChange = (row: any) => {
  const input = getInputComponent(row)
  if (input === InputComponentEnum.SelectMultiple) row.value = []
  else if (input === InputComponentEnum.CascaderMultiple) row.value = null
  else if (input === InputComponentEnum.CascaderSingle) row.value = null
  else row.value = ''
}

// 判断某个条件类型是否已被其他行使用，防止重复添加
const isConditionTypeDisabled = (code: string, currentIdx: number) =>
  conditions.value?.some((r: any, i: number) => i !== currentIdx && r?.conditionType === code)

// 控制“新增条件”按钮是否可用（所有条件都被使用时禁用）
const isAddCondDisabled = computed(() => {
  const all = hub.conditionConfig
  if (!Array.isArray(all) || all.length === 0) return false
  const used = new Set((conditions.value || []).map((r: any) => r?.conditionType).filter(Boolean))
  const remaining = all.filter((c: any) => !used.has(c.code))
  return remaining.length <= 0
})

// 新增 / 删除条件行
const addSingle = () => {
  if (!props.brandCode) {
    ElMessage.warning('请选择品牌')
    return
  }
  const all = hub.conditionConfig
  if (Array.isArray(all) && all.length > 0) {
    const used = new Set((conditions.value || []).map((r: any) => r?.conditionType).filter(Boolean))
    if (used.size >= all.length) return
  }
  conditions.value.push(createCond())
}
const removeSingle = (idx: number) => conditions.value.splice(idx, 1)

onMounted(() => {
  // 初始化条件配置和体验代码标签树
  Promise.all([
    fetchConditionConfig(),
    singleEventActions.updateTagLibClientTree(),
    onStandpointRemoteMethod()
  ])
})
</script>

<template>
  <el-form-item prop="conditions" label="条件配置">
    <div class="block-panel">
      <div class="cond-row" v-for="(row, idx) in conditions" :key="row.key ?? idx">
        <el-select
          v-model="row.conditionType"
          class="w-100"
          @change="() => resetCondRowByField(row)"
        >
          <el-option
            v-for="f in hub.conditionConfig"
            :key="f.code"
            :label="f.name"
            :value="f.code"
            :disabled="isConditionTypeDisabled(f.code, idx)"
          />
        </el-select>
        <el-select v-model="row.operator" class="w-100">
          <el-option
            v-for="op in getWildcardOpsByField(row.conditionType)"
            :key="op.code"
            :label="op.name"
            :value="op.code"
          />
        </el-select>
        <el-select v-model="row.option" class="w-100" @change="() => onOptionChange(row)">
          <el-option
            v-for="t in getOptionsByField(row.conditionType)"
            :key="t.code"
            :label="t.name"
            :value="t.code"
          />
        </el-select>
        <div class="select-input-class">
          <ConditionValueInput
            :row="row"
            :InputComponentEnum="InputComponentEnum"
            :condFieldConfig="COND_FIELD_CONFIG"
            :getInputComponent="getInputComponent"
          />
        </div>
        <el-button link @click="removeSingle(idx)">
          <el-icon><CloseBold /></el-icon>
        </el-button>
      </div>
      <div class="w-136">
        <el-button type="primary" @click="addSingle" :disabled="isAddCondDisabled">
          <template #icon><Plus /></template>
          添加条件配置
        </el-button>
      </div>
    </div>
  </el-form-item>
</template>

<style lang="scss" scoped>
.block-panel {
  width: 100%;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cond-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.select-input-class {
  flex: 1 1 0;
}
.w-100 {
  width: 100px;
}
.w-136 {
  width: 136px;
}
</style>
