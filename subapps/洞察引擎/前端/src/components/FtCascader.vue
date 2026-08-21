<script setup lang="ts">
import { cloneDeep } from 'lodash-es'

// const instance = getCurrentInstance();

const attrs = useAttrs()
const slots = useSlots()

const value = defineModel<string[]>()
// const modelValue = defineModel<string[]>();

const _value = ref<string[]>()
const cacheValue = ref<string[]>()
const changeSource = ref('')

watch(
  () => value.value,
  () => {
    if (!value.value || value.value?.length === 0) {
      _value.value = []
    }
    // 外部传入的值, 需要初始化给 _value
    if (changeSource.value !== 'selfChange') {
      initValue()
    }
  },
  {
    deep: true
  }
)

// options 更新
watch(
  () => attrs?.options,
  (nval: any) => {
    if (nval?.length > 0) {
      handleOptions()
    }
  },
  {
    deep: true
  }
)

const setValue = (val: any[]) => {
  _value.value = val
  value.value = isMultiple.value ? val?.filter(el => el !== 'all') : val
  setCacheValue(val)
}

const setCacheValue = (val: any[]) => {
  cacheValue.value = cloneDeep(val)
}

const handleChange = (val: string[]) => {
  changeSource.value = 'selfChange'
  if (!isMultiple.value) {
    setValue(val)
    return
  }

  if ((val.length === 1 && val[0] === 'all') || val[val.length - 1] === 'all') {
    setValue(allValue.value)
    return
  }

  // 新增
  // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
  if (val.length >= cacheValue.value?.length!) {
    if (!val.includes('all') && val.length === allValue.value.length - 1) {
      setValue(allValue.value)
      return
    }
  } else {
    if (!val.includes('all') && cacheValue.value?.[0] === 'all') {
      setValue([])
      return
    }
    if (val.includes('all')) {
      setValue(val?.filter(el => el !== 'all'))
      return
    }
  }
  setValue(val)
}

const handleOptions = () => {
  const options = (attrs?.options || []) as any[]
  let allObj
  if (fieldNames.value) {
    allObj = { [fieldNames.value?.value]: 'all', [fieldNames.value?.label]: '全选' }
  } else {
    allObj = { value: 'all', label: '全选' }
  }

  if (options?.length > 0) {
    const firstLabel = options[0][fieldNames.value?.label]
    if (firstLabel === '全选') return
    options.unshift(allObj)
  }
}

const fieldNames = computed(() => {
  return attrs['field-names'] as any
})

const isMultiple = computed(() => {
  return Object.keys(attrs!).includes('multiple')
})

const getLastLevelValues = (arr: any[]) => {
  if (!arr || arr?.length === 0) return arr
  let values: any[] = []
  if (!fieldNames.value) {
    for (let item of arr) {
      if (item.children) {
        values = values.concat(getLastLevelValues(item.children))
      } else {
        values.push(item.value)
      }
    }
  } else {
    for (let item of arr) {
      if (item[fieldNames.value['children']]) {
        values = values.concat(getLastLevelValues(item[fieldNames.value['children']]))
      } else {
        values.push(item[fieldNames.value['value']])
      }
    }
  }

  return values
}

const allValue = computed(() => {
  return getLastLevelValues(attrs?.options as any)
})

// 计算级联选择器的 props 配置
const cascaderProps = computed(() => {
  const baseProps = {
    // 从 field-names 属性获取字段配置
    ...(fieldNames.value || {}),
    // 如果是多选模式，启用多选
    multiple: isMultiple.value,
    // 启用严格模式，允许选择任意级别的节点
    checkStrictly: isMultiple.value,
    // 多选时发出完整路径
    emitPath: true
  }

  return baseProps
})

// 计算级联选择器的其他属性（排除 props 相关的）
const cascaderAttrs = computed(() => {
  const { 'field-names': fieldNamesAttr, multiple, props, ...otherAttrs } = attrs
  return {
    ...otherAttrs,
    // 多选模式
    multiple: isMultiple.value,
    // 多选时的标签折叠
    'collapse-tags': isMultiple.value,
    // 多选时显示标签数量限制
    'max-collapse-tags': 1,
    // 多选时显示标签提示
    'collapse-tags-tooltip': isMultiple.value,
    // 显示所有级别
    'show-all-levels': false
  }
})

const initValue = () => {
  if (value.value?.length === allValue.value?.length - 1) {
    _value.value = allValue.value
  } else {
    _value.value = value.value
  }
  setCacheValue(_value.value!)
}

onMounted(() => {
  if (isMultiple.value) {
    handleOptions()
    initValue()
  }
})
</script>

<template>
  <el-cascader
    v-model="_value"
    v-bind="cascaderAttrs"
    :props="cascaderProps"
    @change="(val: any) => handleChange(val)"
  >
    <template v-if="slots.label" #label>
      <slot name="label"></slot>
    </template>
    <template v-if="slots.prefix" #prefix>
      <slot name="prefix"></slot>
    </template>
    <template v-if="slots['arrow-icon']" #arrow-icon>
      <slot name="arrow-icon"></slot>
    </template>
    <template v-if="slots['loading-icon']" #loading-icon>
      <slot name="loading-icon"></slot>
    </template>
    <template v-if="slots['search-icon']" #search-icon>
      <slot name="search-icon"></slot>
    </template>
    <template v-if="slots.empty" #empty>
      <slot name="empty"></slot>
    </template>
    <template v-if="slots.option" #option>
      <slot name="option"></slot>
    </template>
  </el-cascader>
</template>

<style scoped lang="scss"></style>
