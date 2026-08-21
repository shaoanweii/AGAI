<script setup lang="ts">
import { cloneDeep } from 'lodash-es'

// 类型定义
interface CascaderFieldNames {
  value?: string
  label?: string
  children?: string
  disabled?: string
  leaf?: string
}

interface Props {
  options: any[]
  multiple?: boolean
  clearable?: boolean
  fieldNames?: CascaderFieldNames
  placeholder?: string
  maxTagCount?: number
  subLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  fieldNames: () => ({ label: 'label', value: 'value' }),
  placeholder: '全选',
  multiple: false,
  clearable: false,
  maxTagCount: 1,
  subLength: 1
})

const { options, fieldNames, placeholder, multiple, clearable, maxTagCount, subLength } =
  toRefs(props)
const emits = defineEmits(['change'])
const modelValue = defineModel<any>()
const curValue = ref<any>()
const cacheValue = ref()
const changeSource = ref('')

const allList = ['all', '全选']

watch(
  () => modelValue.value,
  () => {
    if (!modelValue.value || modelValue.value?.length === 0) {
      curValue.value = []
      setCacheValue([])
    }
    // 外部传入的值, 需要初始化给 curValue
    if (changeSource.value !== 'selfChange') {
      initValue()
    }
    // 重置变更来源标识
    changeSource.value = ''
  },
  {
    deep: true
  }
)

// options 更新
watch(
  () => options.value,
  (nval: any) => {
    if (nval?.length > 0) {
      handleOptions()
    }
  },
  {
    deep: true
  }
)

const setValue = (val: any[] | any) => {
  curValue.value = val
  if (multiple.value) {
    modelValue.value = val?.filter((el: any) => !allList.includes(el))
  } else {
    modelValue.value = val
  }

  setCacheValue(val)
}

const setCacheValue = (val: any[]) => {
  cacheValue.value = cloneDeep(val)
}

const allValue = computed(() => {
  return options.value
    .filter(el => !allList.includes(el[fieldNames.value.value!]))
    .map(el => el[fieldNames.value.value!])
})

const handleChange = (val: any) => {
  emits('change', val)
  changeSource.value = 'selfChange'
  if (!multiple.value) {
    setValue(val)
    return
  }

  const hasAllInVal = val.includes('all') || val.includes('全选')
  const hasAllInCache = cacheValue.value?.includes('all') || cacheValue.value?.includes('全选')

  // 如果点击了全选选项
  if (hasAllInVal && !hasAllInCache) {
    // 选择全部实际选项
    curValue.value = ['all', ...allValue.value]
    modelValue.value = allValue.value
    setCacheValue(curValue.value)
    return
  }

  // 如果取消了全选选项
  if (!hasAllInVal && hasAllInCache) {
    // 清空所有选择
    setValue([])
    return
  }

  // 如果没有全选选项，但选择了所有实际选项
  if (!hasAllInVal && val.length === allValue.value.length) {
    // 自动添加全选选项
    curValue.value = ['all', ...allValue.value]
    modelValue.value = allValue.value
    setCacheValue(curValue.value)
    return
  }

  // 如果有全选选项，但取消了某个实际选项
  if (hasAllInVal && val.length < allValue.value.length + 1) {
    // 移除全选选项，只保留实际选择的选项
    const actualValues = val.filter((item: any) => !allList.includes(item))
    setValue(actualValues)
    return
  }

  // 普通选择
  setValue(val)
}

const formatLabel = (options: any) => {
  if (options.label?.length > subLength.value) {
    return `${options.label.substring(0, subLength.value)}...`
  } else {
    return options.label
  }
}

const handleOptions = () => {
  // if (!multiple.value) {
  //   return
  // }

  let allObj: any

  if (fieldNames.value) {
    allObj = { [fieldNames.value.value!]: 'all', [fieldNames.value.label!]: '全选' }
  } else {
    allObj = { value: 'all', label: '全选' }
  }

  if (options.value?.length > 0) {
    const firstLabel = options.value[0][fieldNames.value.label!]
    if (firstLabel === '全选') return
    options.value.unshift(allObj)
  }
}

/**
 * 初始化时设置缓存值
 */
const initValue = () => {
  if (multiple.value) {
    if (modelValue.value?.length === allValue.value?.length) {
      // 如果选择了所有实际选项，显示为全选状态
      curValue.value = ['all', ...allValue.value]
    } else {
      // 否则显示实际选择的选项
      curValue.value = modelValue.value || []
    }
    setCacheValue(curValue.value!)
  } else {
    curValue.value = modelValue.value
  }
}

onMounted(() => {
  if (multiple.value) {
    handleOptions()
    initValue()
  } else {
    handleOptions()
    initValue()
  }
})
</script>

<template>
  <div :class="['f-select']">
    <el-select
      v-model="curValue"
      :multiple="multiple"
      :clearable="clearable"
      :max-collapse-tags="maxTagCount"
      collapse-tags
      :placeholder="placeholder"
      :format-label="formatLabel"
      @change="handleChange"
    >
      <el-option
        v-for="(item, index) of options"
        :key="index"
        :value="item[fieldNames.value!]"
        :label="item[fieldNames.label!]"
      >
        {{ item[fieldNames.label!] }}
      </el-option>
    </el-select>
  </div>
</template>

<style lang="scss">
.f-select {
  width: 100%;
  .el-tag {
    background-color: #f2f3f5 !important;
  }
  .el-select-view-multiple.el-select-view-size-medium .el-select-view-inner {
    white-space: nowrap;
  }
}
</style>
