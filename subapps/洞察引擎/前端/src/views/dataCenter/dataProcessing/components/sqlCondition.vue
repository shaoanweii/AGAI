<script setup lang="ts">
import { inject } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import type { ConditionItem } from '@/types/rule.types'
import { debounce } from 'lodash-es'
import { findResourceGroupByAppClient } from '@/api/dataCenter'
import FtInput from '@/components/FtInput.vue'

const props = withDefaults(
  defineProps<{
    // 1 条件 2 动作
    type: 1 | 2
    // 内容格式
    contentType: string
    // 处理阶段
    processPhase: string
    // 规则类型
    regulationType: string
    clientId: string
  }>(),
  {
    type: 1
  }
)

const { type, contentType, processPhase, regulationType, clientId } = toRefs(props)

const labelTextPreMap = {
  1: '条件',
  2: '动作'
}
// label 前缀
const labelTextPre = computed(() => {
  return labelTextPreMap[type.value]
})

const checkField = ref('')

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const sqlList = defineModel<Record<string, any>[]>({ required: true })
const optionsList = ref<Record<string, ConditionsDetailItem[]>[]>([])

const fieldPreMap = {
  1: 'condition',
  2: 'action'
}
// 生成form-item的field 属性
const generateField = (index: number) => {
  return `${fieldPreMap[type.value]}-item-${index}`
}

const setSqlListItemDefault = (key: string) => {
  if (sqlList.value && Array.isArray(sqlList.value)) {
    sqlList.value.map(el => {
      el[key] = ''
    })
  }
}

// 校验数组是否有重复项
const hasDuplicate = (arr: any) => {
  if (!arr || !Array.isArray(arr)) {
    return false
  }
  arr = arr.map((el: any) => ({
    fieldName: el.fieldName,
    variableValue: el.variableValue,
    logicalOperator: el.logicalOperator,
    conditionType: el.conditionType,
    conditionDetail: el.conditionDetail
  }))
  const seen = new Set()
  for (const [index, obj] of arr.entries()) {
    const strObj = JSON.stringify(obj)
    if (seen.has(strObj)) {
      // 记录已存在的Field
      checkField.value = generateField(index)
      return true
    }
    seen.add(strObj)
  }
  return false
}
// 动态生成校验规则
const sqlRule = computed(() => {
  return sqlList.value?.reduce((result, cur, curIndex) => {
    return {
      ...result,
      [`${generateField(curIndex)}`]: [
        {
          required: true,
          validator: (_rule: any, _value: string, callback: any) => {
            let ccur = {}
            if (['empty', 'notEmpty'].includes(cur.logicalOperator)) {
              ccur = {
                fieldName: cur.fieldName,
                variableValue: cur.variableValue,
                logicalOperator: cur.logicalOperator
                // conditionType: cur.conditionType,
                // conditionDetail: cur.conditionDetail
              }
            } else {
              ccur = {
                fieldName: cur.fieldName,
                variableValue: cur.variableValue,
                logicalOperator: cur.logicalOperator,
                conditionType: cur.conditionType,
                conditionDetail: cur.conditionDetail
              }
            }

            const isEmpty = Object.values(ccur).every(el => !!el)
            if (!isEmpty) {
              callback(new Error('配置项必填'))
              return
            }
            const result = hasDuplicate(sqlList.value)
            if (result) {
              callback(new Error('配置条件不允许重复，请删除或修改后重新提交'))
              return
            }
            callback()
          }
        }
      ]
    }
  }, {})
})
const sqlRef = ref()

const firstOptionMap: Record<string, any> = {
  // 前置
  pre: {
    // 文本
    text: conditions.vocTextType,
    // 工单
    order: conditions.vocOrderType
  },
  // 后置
  post: {
    text: conditions.postFields,
    order: conditions.postFields
  }
}

watch(
  () => processPhase.value,
  (nval, oval) => {
    if (!oval) {
      return
    }
    setSqlListItemDefault('fieldName')
  }
)
watch(
  () => contentType.value,
  (nval, oval) => {
    if (!oval) {
      return
    }
    if (processPhase.value === 'pre') {
      setSqlListItemDefault('fieldName')
    }
  }
)
watch(
  () => sqlList.value,
  nval => {
    // type.value === 1 &&
    if (optionsList.value.length === 0 && nval) {
      // 回显 过滤下拉数据
      nval.forEach((item, index) => {
        optionsList.value.push(generateOptionItem())
        handleThirdChange(item.logicalOperator, item, index)
        handleFourthChange(item.conditionType, item, index, true)
        handleVariableValueChange(item.variableValue, item, index, true)
      })
    }
  }
)
watch(
  () => regulationType.value,
  nval => {
    if (type.value === 2 && nval) {
      optionsList.value = optionsList.value.map(el => {
        return {
          ...el,
          third: thirdOption.value as ConditionsDetailItem[],
          fourth: fourthOption.value as ConditionsDetailItem[]
        }
      })
    }
  }
)
// 第一个下拉框
const variableOptions = computed(() => {
  // setSqlListItemDefault('fieldName')
  return firstOptionMap[processPhase.value][contentType.value] || []
})

const variableValueOptions = computed(() => {
  if (type.value === 1) {
    return conditions.variableValue
  } else if (type.value === 2) {
    return conditions.variableValue.filter(el => el.key == 'value')
  }
})

const thirdOptionByAction = computed(() => {
  let list: ConditionsDetailItem[] = []
  if (processPhase.value === 'pre') {
    list = conditions.regulationPreType
  } else if (processPhase.value === 'post') {
    list = conditions.regulationPostType
  }
  return list?.filter((el: ConditionsDetailItem) => el.key === regulationType.value)
})
// 第三个下拉框
const thirdOption = computed(() => {
  if (type.value === 1) {
    // return conditions.ruleLogicalOperator
    return getThirdOptionsByVariableValue('value')
  } else if (type.value === 2) {
    /**
     * 清洗的时候: 动作 逻辑运算符[==、!= 、包含 、不包含] R04
     * 脱敏的时候: 动作 逻辑运算符[包含] R02
     * 修改的时候:动作 逻辑运算符[==] R01
     * 补充的时候:动作 逻辑运算符[==] R03
     * "key": "R01","value": "修改"/
     * "key": "R02","value": "脱敏"/
     * "key": "R03","value": "补充"/
     * "key": "R04","value": "清洗"/
     * "key": "R05","value": "过滤"
     */
    if (['R01', 'R03'].includes(regulationType.value)) {
      return conditions.ruleLogicalOperator.filter(el => el.key === 'equals')
    }
    if (regulationType.value === 'R02') {
      return conditions.ruleLogicalOperator.filter(el => el.key === 'contain')
    }
    if (regulationType.value === 'R04') {
      return conditions.ruleLogicalOperator.filter(el => !['empty', 'notEmpty'].includes(el.key))
    }
    return thirdOptionByAction.value
  }
})

// 第四个下拉框
const fourthOption = computed(() => {
  if (type.value === 1) {
    return conditions.ruleConditionType
  } else if (type.value === 2) {
    // return conditions.ruleConditionType;
    if (['R01', 'R03'].includes(regulationType.value)) {
      return conditions.ruleConditionType.filter(el => el.key === 'value')
    } else {
      return conditions.ruleConditionType
    }
  }
})

const firstDisabled = (option: any, optionIndex: number, item: any, index: number) => {
  if (type.value === 1) {
    return false
  } else if (type.value === 2) {
    return sqlList.value.some((listItem, listIndex) => {
      return listIndex !== index && listItem.fieldName === option.key
    })
  }
}

/**
 * 根据维度选择筛选出逻辑运算符的选项
 * @param variableValue
 */
const getThirdOptionsByVariableValue = (variableValue: string) => {
  const options = [
    'greaterThen',
    'lessThen',
    'greaterThenOrEqual',
    'lessThenOrEqual',
    'equals',
    'notEquals'
  ]
  const options1 = ['greaterThen', 'lessThen', 'greaterThenOrEqual', 'lessThenOrEqual']
  if (variableValue === 'textLength') {
    return conditions.ruleLogicalOperator.filter((el: ConditionsDetailItem) =>
      options.includes(el.key)
    )
  } else if (variableValue === 'value') {
    return conditions.ruleLogicalOperator.filter(
      (el: ConditionsDetailItem) => !options1.includes(el.key)
    )
  }
}

/**
 * 根据维度选择筛选出条件类型的选项
 * @param variableValue
 */
const getFourthOptionsByVariableValue = (variableValue: string) => {
  if (variableValue === 'textLength') {
    return conditions.ruleConditionType.filter((el: ConditionsDetailItem) => el.key === 'value')
  } else if (variableValue === 'value') {
    return conditions.ruleConditionType
  }
}

const handleVariableValueChange = (
  val: string,
  item: Record<string, any>,
  index: number,
  isWatch = false
) => {
  if (type.value === 1) {
    optionsList.value[index].third = getThirdOptionsByVariableValue(val)!
    optionsList.value[index].fourth = getFourthOptionsByVariableValue(val)!
    if (!isWatch) {
      sqlList.value[index].logicalOperator = ''
      sqlList.value[index].conditionType = ''
      sqlList.value[index].conditionDetail = ''
    }
  }
}

const handleThirdChange = (val: string, item: Record<string, any>, index: number) => {
  // == !=   -> 四没有正则表达式
  if (type.value === 1 && sqlList.value[index].variableValue === 'value') {
    if (['equals', 'notEquals'].includes(val)) {
      optionsList.value[index].fourth = conditions.ruleConditionType.filter(
        (el: ConditionsDetailItem) => el.key !== 'regex'
      )
      sqlList.value[index].conditionType = item.conditionType === 'regex' ? '' : item.conditionType
    } else if (['empty', 'notEmpty'].includes(val)) {
      sqlList.value[index].conditionType = ''
      sqlList.value[index].conditionDetail = ''
    } else {
      optionsList.value[index].fourth = conditions.ruleConditionType
    }
  }
}
const handleFourthChange = (
  val: string,
  item: Record<string, any>,
  index: number,
  isWatch = false
) => {
  // 正则表达式  -》 三隐藏 == !=
  if (type.value === 1 && sqlList.value[index].variableValue === 'value') {
    if (['regex'].includes(val)) {
      optionsList.value[index].third = getThirdOptionsByVariableValue('value')!.filter(
        (el: ConditionsDetailItem) => !['equals', 'notEquals'].includes(el.key)
      )
      sqlList.value[index].logicalOperator = ['equals', 'notEquals'].includes(item.logicalOperator)
        ? ''
        : item.logicalOperator
    } else {
      optionsList.value[index].third = getThirdOptionsByVariableValue('value')!
    }
  }
  if (!isWatch && val) {
    sqlList.value[index].conditionDetail = ''
  }
}

/**
 * 生成条件项
 * @param serialNumber
 */
const generateConditionItem = (serialNumber: string): ConditionItem => {
  return {
    fieldName: '',
    variableValue: '',
    logicalOperator: '',
    conditionType: '',
    conditionDetail: '',
    serialNumber
  }
}

/**
 * 生成第三第四列下拉数据
 */
const generateOptionItem = (): Record<string, ConditionsDetailItem[]> => {
  return {
    third: thirdOption.value as ConditionsDetailItem[],
    fourth: fourthOption.value as ConditionsDetailItem[]
  }
}

/**
 * 添加条件
 */
const handleClickAdd = debounce((isClearList = false) => {
  if (!sqlList.value || !Array.isArray(sqlList.value) || !sqlList.value.length) {
    sqlList.value = []
  }
  if (isClearList) {
    sqlList.value.length = 0
  }
  sqlList.value.push(generateConditionItem((sqlList.value?.length || 0) + 1 + ''))
  optionsList.value.push(generateOptionItem())
}, 300)

/**
 * 删除条件
 * @param item
 * @param index
 */
const handleClickDel = debounce((item: ConditionItem, index: number) => {
  if (sqlList.value.length === 1) {
    handleClickAdd(true)
    return
  }
  sqlList.value.splice(index, 1)
  optionsList.value.splice(index, 1)
}, 300)

/**
 * 校验数组的每一项是否为空
 * @param arr
 */
const checkArrayForEmptyFields = (arr: any[]) => {
  if (!arr || !Array.isArray(arr)) {
    return { isValid: true, index: -1, field: null }
  }
  arr = arr.map((el: any) => ({
    fieldName: el.fieldName,
    variableValue: el.variableValue,
    logicalOperator: el.logicalOperator,
    conditionType: el.conditionType,
    conditionDetail: el.conditionDetail
  }))
  for (let i = 0; i < arr.length; i++) {
    let item = {}
    if (['empty', 'notEmpty'].includes(arr[i].logicalOperator)) {
      item = {
        fieldName: arr[i].fieldName,
        variableValue: arr[i].variableValue,
        logicalOperator: arr[i].logicalOperator
        // conditionType: arr[i].conditionType,
        // conditionDetail: arr[i].conditionDetail
      }
    } else {
      item = {
        fieldName: arr[i].fieldName,
        variableValue: arr[i].variableValue,
        logicalOperator: arr[i].logicalOperator,
        conditionType: arr[i].conditionType,
        conditionDetail: arr[i].conditionDetail
      }
    }

    const emptyField = Object.entries(item).find(
      ([, value]) => value === null || value === undefined || value === ''
    )

    if (emptyField) {
      return { isValid: false, index: i, field: emptyField[0] }
    }
  }

  return { isValid: true, index: -1, field: null }
}
/**
 * 校验数据-  非空与重复项校验
 */
const checkData = async () => {
  const isEmpty = checkArrayForEmptyFields(sqlList.value)
  // 非空检验
  if (!isEmpty.isValid) {
    await sqlRef.value.validateField(generateField(isEmpty.index))
    return false
  }
  const isRepeat = hasDuplicate(sqlList.value)
  // 重复项校验
  if (isRepeat) {
    await sqlRef.value.validateField(checkField.value)
    return false
  }
  return true
}

/**
 * 提供一个设置默认值的方法
 */
const setDefaultValue = (serialNumber = '1') => {
  optionsList.value.push(generateOptionItem())
  return [generateConditionItem(serialNumber)]
}

const resourceGroupOptions = ref<Record<string, any>[]>([])

const getResourceGroupOptions = (clientId: string) => {
  findResourceGroupByAppClient({ customer: clientId })
    .then(res => {
      if (res.code === '200') {
        resourceGroupOptions.value = res.result as any
      } else {
        resourceGroupOptions.value = []
      }
    })
    .catch(() => {
      resourceGroupOptions.value = []
    })
}

watch(
  clientId,
  (nval: any) => {
    if (nval) {
      getResourceGroupOptions(nval)
    }
  },
  {
    deep: true,
    immediate: true
  }
)

defineExpose({ setDefaultValue, checkData })
</script>

<template>
  <div class="sql-condition">
    <el-form :model="sqlList" :rules="sqlRule" auto-label-width ref="sqlRef">
      <div v-for="(item, index) of sqlList || []" :key="index">
        <!--feedback-->
        <!--:validate-status="'error'"-->
        <!--help="配置条件不允许重复，请删除或修改后重新提交"-->
        <el-form-item
          label-width="0px"
          :style="{ width: '100%' }"
          :prop="`${generateField(index)}`"
        >
          <div style="width: 45px">{{ labelTextPre }} {{ index + 1 }}</div>
          <!--字段-->
          <el-select
            v-model="item.fieldName"
            placeholder="字段"
            clearable
            style="width: 140px"
            class="bg-white ml-16"
            :data-testid="`sql-${type}-9001-${index}`"
          >
            <el-option
              v-for="(item1, index1) in variableOptions"
              :key="index1"
              :data-testid="`sql-${type}-9001-${index}-${index1}`"
              :label="item1.value"
              :disabled="firstDisabled(item1, Number(index1), item, Number(index))"
              :value="item1.key"
            />
          </el-select>
          <!--变量值-->
          <el-select
            v-model="item.variableValue"
            placeholder="变量值"
            clearable
            style="width: 110px"
            class="bg-white ml-16"
            :data-testid="`sql-${type}-9002-${index}`"
            @change="(val) => handleVariableValueChange(val as string, item, index)"
          >
            <el-option
              v-for="(item2, item2Index) in variableValueOptions"
              :key="item2Index"
              :data-testid="`sql-${type}-9002-op-${index}-${item2Index}`"
              :label="item2.value"
              :value="item2.key"
            />
          </el-select>
          <!--逻辑运算符-->
          <el-select
            v-model="item.logicalOperator"
            placeholder="逻辑运算符"
            clearable
            style="width: 100px"
            class="bg-white ml-16"
            @change="(val) => handleThirdChange(val as string, item, index)"
            :data-testid="`sql-${type}-9003-${index}`"
          >
            <el-option
              v-for="(item3, item3Index) in optionsList[index]?.third"
              :key="item3Index"
              :data-testid="`sql-${type}-9003-${index}-${item3Index}`"
              :label="item3.value"
              :value="item3.key"
            />
          </el-select>
          <!--条件类型-->
          <div style="width: 120px" class="ml-16">
            <template v-if="!['empty', 'notEmpty'].includes(item.logicalOperator)">
              <el-select
                v-model="item.conditionType"
                placeholder="条件类型"
                clearable
                style="width: 120px"
                class="bg-white"
                :data-testid="`sql-${type}-9004-${index}`"
                @change="(val) => handleFourthChange(val as string, item, index)"
              >
                <el-option
                  v-for="(item4, item4Index) in optionsList[index]?.fourth"
                  :key="item4Index"
                  :data-testid="`sql-${type}-9004-${index}-${item4Index}`"
                  :label="item4.value"
                  :value="item4.key"
                />
              </el-select>
            </template>
          </div>

          <!--条件详情-->
          <!--维度：文本长度-->
          <div v-if="item.variableValue === 'textLength'" style="width: 160px" class="ml-16">
            <FtInput
              v-model.trim.integer="item.conditionDetail"
              placeholder="条件详情"
              clearable
              style="width: 160px"
              class="bg-white"
              :data-testid="`sql-${type}-9005-${index}`"
            />
          </div>
          <!--维度：变量值  ="item.variableValue === 'value'"-->
          <div v-else style="width: 160px" class="ml-16">
            <template v-if="!['empty', 'notEmpty'].includes(item.logicalOperator)">
              <!--资源组对应的选项-->
              <el-select
                v-if="item.conditionType === 'resourceGroup'"
                v-model="item.conditionDetail"
                placeholder="条件详情"
                clearable
                style="width: 160px"
                class="bg-white"
                :data-testid="`sql-${type}-9005-${index}`"
              >
                <!--<el-option v-for="item5 in conditions.dataResources" :label="item5.value" :value="item5.key"/>-->
                <el-option
                  v-for="(item5, item5Index) in resourceGroupOptions"
                  :key="item5Index"
                  :data-testid="`sql-${type}-9005-${index}-${item5Index}`"
                  :label="item5.name"
                  :value="item5.id"
                />
              </el-select>
              <el-input
                v-else
                v-model.trim="item.conditionDetail"
                placeholder="条件详情"
                clearable
                style="width: 160px"
                class="bg-white"
                :data-testid="`sql-${type}-9005-${index}`"
              ></el-input>
            </template>
          </div>

          <el-link
            :underline="false"
            type="primary"
            class="ml-16"
            :data-testid="`sql-${type}-9006-${index}`"
            @click="handleClickDel(item as ConditionItem, index)"
            >删除
          </el-link>
        </el-form-item>
      </div>
      <span>
        <el-link
          :underline="false"
          type="primary"
          style="margin-left: -4px; margin-top: 4px"
          :data-testid="`sql-${type}-9007`"
          @click="() => handleClickAdd()"
          >新增{{ labelTextPre }}
        </el-link>
        <!--<el-link :underline="false" type="primary" style="margin-left: -4px; margin-top: 4px;"-->
        <!--        @click="() => checkData()">校验-->
        <!--</el-link>-->
      </span>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.sql-condition {
  background: #f7f8fa;
  padding: 24px 16px 16px 16px;

  &::v-deep(.el-form-item-message) {
    padding-left: 60px;
  }

  &::v-deep(.el-input-wrapper) {
    background-color: #fff;
  }

  &::v-deep(.el-select-view-single) {
    background-color: #fff;
  }

  &::v-deep(
      .el-form-item-status-error .el-select-view:not(.el-select-view-disabled),
      .el-form-item-status-error .el-input-tag:not(.el-input-tag-disabled)
    ) {
    background-color: rgb(255, 236, 232) !important;
  }

  &::v-deep(
      .el-form-item-status-error .el-input-wrapper:not(.el-input-disabled),
      .el-form-item-status-error .el-textarea-wrapper:not(.el-textarea-disabled)
    ) {
    background-color: rgb(255, 236, 232) !important;
  }
}
</style>
