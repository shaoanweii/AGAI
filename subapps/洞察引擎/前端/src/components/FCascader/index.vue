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

interface TagProps {
  size?: string
  type?: string
  [key: string]: any
}

interface CascaderOption {
  value: string | number
  label: string
  children?: CascaderOption[]
  disabled?: boolean
  leaf?: boolean
  [key: string]: any
}

interface Props {
  options: any[]
  multiple?: boolean
  clearable?: boolean
  fieldNames: CascaderFieldNames
  placeholder?: string
  separator?: string
  maxTagCount?: number
  subLength?: number
  checkStrictly?: boolean
  pathMode?: boolean
  parentKey?: string
  tagProps?: TagProps
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  fieldNames: () => ({ label: 'label', value: 'value', children: 'children' }),
  placeholder: '全部',
  multiple: true,
  clearable: true,
  maxTagCount: 1,
  subLength: 1,
  separator: '#',
  checkStrictly: false,
  pathMode: false,
  parentKey: 'tagCode',
  tagProps: undefined
})

const {
  options,
  fieldNames,
  placeholder,
  multiple,
  clearable,
  maxTagCount,
  separator,
  subLength,
  checkStrictly,
  pathMode,
  parentKey,
  tagProps
} = toRefs(props)

const modelValue = defineModel<string[]>()
const emits = defineEmits([
  'change',
  'popupVisibleChange',
  'clear',
  'afterChange',
  'indeterminateCheckedStatus'
])

const curValue = ref<string[]>()
const cacheValue = ref<string[]>([])
const changeSource = ref('')
const cascadeRef = useTemplateRef('cascadeRef')

// 计算 Element Plus 级联选择器的 props 配置
const cascaderProps = computed(() => {
  const props = {
    ...fieldNames.value,
    // Element Plus 级联选择器的多选配置
    multiple: multiple.value,
    // 启用严格模式，允许选择任意级别的节点，支持全选功能
    checkStrictly: checkStrictly.value,
    // 多选时发出完整路径
    emitPath: pathMode.value
  }
  // console.log('cascaderProps:', props)
  return props
})

watch(
  () => modelValue.value,
  () => {
    if (!modelValue.value || modelValue.value?.length === 0) {
      curValue.value = []
    }
    // 外部传入的值, 需要初始化给 curValue
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
  () => options.value,
  (nval: any) => {
    // console.log('options 更新:', nval)
    if (nval?.length > 0) {
      handleOptions()
      initValue()
      // console.log('处理后的 options:', options.value)
      // console.log('当前 curValue:', curValue.value)
    }
  },
  {
    deep: true
  }
)

const formatLabel = (options: CascaderOption[]) => {
  const str = options.map((option: any) => option[fieldNames.value.label!])?.join(separator.value)
  // const offsetWidth = cascadeRef.value?.offsetWidth || 100
  // const fontSize = 12
  // const width1 = 16 + 24 + 6 + 12
  // const strWidth = fontSize * str.length
  // let newWidth = offsetWidth - width1
  // if (options?.length === 1) {
  //   newWidth = offsetWidth
  // } else if (options?.length < 10) {
  //   newWidth = offsetWidth - 45
  // } else if (options?.length < 100) {
  //   newWidth = offsetWidth - 55
  // } else if (options?.length < 1000) {
  //   newWidth = offsetWidth - 61
  // }
  // let len = Math.floor(newWidth / fontSize) - 4
  // len = len < 0 ? 0 : len
  // return `${str.substring(0, subLength.value)}...`
  if (subLength.value === -1) {
    return str
  }
  if (str?.length > subLength.value) {
    return `${str.substring(0, subLength.value)}...`
  } else {
    return str
  }
  // return str?.length >= len ? `${str.substring(0, len)}...` : str
}

const setValue = (val: any[]) => {
  curValue.value = val
  if (pathMode.value) {
    modelValue.value = multiple.value ? val?.filter(el => el[0] !== 'all') : val
  } else {
    modelValue.value = multiple.value ? val?.filter(el => el !== 'all') : val
  }
  emits('afterChange', modelValue.value)
  emits('indeterminateCheckedStatus', indeterminateCheckedStatus(val))
  setCacheValue(val)
}

const setCacheValue = (val: any[]) => {
  cacheValue.value = cloneDeep(val)
}

/**
 * @description: 默认change模式
 * @param {*} val
 * @return {*}
 */
const defaultChange = (val: string[]) => {
  // console.log('defaultChange 被调用，val:', val)
  emits('change', val)
  changeSource.value = 'selfChange'
  if (!multiple.value) {
    setValue(val)
    return
  }

  // 获取所有叶子节点的值（不包含全选）
  const allLeafValues = getAllLeafValues(options.value)
  const hasAllInVal = val.includes('all')
  const hadAllInCache = cacheValue.value?.includes('all')

  // console.log('全选逻辑调试:', {
  //   val,
  //   hasAllInVal,
  //   hadAllInCache,
  //   allLeafValues,
  //   cacheValue: cacheValue.value
  // })

  // 处理全选逻辑
  if (hasAllInVal && !hadAllInCache) {
    // console.log('新选择全选，设置所有叶子节点')
    // 全选时，设置所有叶子节点的值
    const fullSelection = ['all', ...allLeafValues]
    setValue(fullSelection)
    return
  }

  // 处理取消全选的情况
  if (!hasAllInVal && hadAllInCache) {
    // console.log('取消全选，清空所有选择')
    // 如果之前包含全选，现在不包含，说明用户取消了全选
    setValue([])
    return
  }

  // 处理选择所有叶子节点时自动选择全选
  if (!hasAllInVal && !hadAllInCache) {
    const valWithoutAll = val.filter(v => v !== 'all')
    if (
      valWithoutAll.length === allLeafValues.length &&
      allLeafValues.every(leaf => valWithoutAll.includes(leaf))
    ) {
      // console.log('选择了所有叶子节点，自动添加全选')
      const allValues = ['all', ...allLeafValues]
      setValue(allValues)
      return
    }
  }

  // 处理从全选状态取消部分选项的情况
  if (hasAllInVal && hadAllInCache) {
    const valWithoutAll = val.filter(v => v !== 'all')
    if (valWithoutAll.length < allLeafValues.length) {
      // console.log('从全选状态取消部分选项，移除全选标识')
      setValue(valWithoutAll)
      return
    }
  }

  setValue(val)
}

/**
 * @description: pathMode change模式
 * @param {*} val
 * @return {*}
 */
const pathModeChange = (val: any[]) => {
  emits('change', val)
  changeSource.value = 'selfChange'
  if (!multiple.value) {
    setValue(val)
    return
  }

  if (cacheValue.value?.length > val.length && val.length === 1 && val[0]?.[0] === 'all') {
    setValue([])
    return
  }

  if ((val.length === 1 && val[0]?.[0] === 'all') || val[val.length - 1]?.[0] === 'all') {
    setValue(allValue.value)
    return
  }
  // 新增
  if (val.length >= cacheValue.value?.length) {
    if (!val.toString()?.includes('all') && val.length === allValue.value.length - 1) {
      setValue(allValue.value)
      return
    }
  } else {
    if (!val.toString()?.includes('all') && cacheValue.value?.[0]?.[0] === 'all') {
      setValue([])
      return
    }
    if (val.toString()?.includes('all')) {
      setValue(val?.filter(el => el?.[0] !== 'all'))
      return
    }
  }
  setValue(val)
}

const handleChange = (val: string[]) => {
  if (pathMode.value) {
    pathModeChange(val)
  } else {
    defaultChange(val)
  }
}

/**
 * @description: 递归获取最后一级值
 * @param {*} arr
 * @return {*}
 */
const getLastLevelValues = (arr: any[]) => {
  if (!arr || arr?.length === 0) return arr
  let values: any[] = []
  if (!fieldNames.value.value) {
    for (let item of arr) {
      // 跳过全选选项
      if (item.value === 'all' || item.label === '全选') continue

      if (item.children) {
        values = values.concat(getLastLevelValues(item.children))
      } else {
        values.push(item.value)
      }
    }
  } else {
    for (let item of arr) {
      // 跳过全选选项
      if (item[fieldNames.value.value!] === 'all' || item[fieldNames.value.label!] === '全选')
        continue

      if (item[fieldNames.value['children']!]) {
        values = values.concat(getLastLevelValues(item[fieldNames.value['children']!]))
      } else {
        values.push(item[fieldNames.value['value']])
      }
    }
  }

  return values
}

/**
 * @description: checkStrictly模式 获取所有的value
 * @param {*} tree
 * @return {*}
 */
const getAllValueInArray = (tree: any) => {
  let vales: any = []

  function traverse(node: any) {
    if (node) {
      vales.push(node[fieldNames.value.value!])
      if (node[fieldNames.value.children!] && node[fieldNames.value.children!].length > 0) {
        node[fieldNames.value.children!].forEach(traverse)
      }
    }
  }

  tree.forEach(traverse)

  return vales
}

/**
 * 获取所有叶子节点的值（用于全选功能）
 * @param {*} tree
 * @return {*}
 */
const getAllLeafValues = (tree: any) => {
  let leafValues: any[] = []

  function traverse(node: any) {
    if (node) {
      // 跳过全选节点
      if (node[fieldNames.value.value!] === 'all' || node[fieldNames.value.label!] === '全选') {
        return
      }

      // 如果没有子节点，则是叶子节点
      if (!node[fieldNames.value.children!] || node[fieldNames.value.children!].length === 0) {
        if (node[fieldNames.value.value!]) {
          leafValues.push(node[fieldNames.value.value!])
        }
      } else {
        // 如果有子节点，继续遍历
        node[fieldNames.value.children!].forEach(traverse)
      }
    }
  }

  tree.forEach(traverse)

  return leafValues
}

/**
 * @description: pathMode 模式 获取所有的value存在二维数组中， 用于pathMode模式
 * @param {*} arr
 * @return {*}
 */
const getLastLevelValuesIn2DArray = (arr: any[]) => {
  if (!arr || arr?.length === 0) return arr
  let result: any[] = []
  function recursiveTraversal(node: any, path: any[]) {
    path.push(node[fieldNames.value.value!])
    if (node[fieldNames.value.children!] && node[fieldNames.value.children!].length > 0) {
      for (const child of node[fieldNames.value.children!]) {
        recursiveTraversal(child, [...path])
      }
    } else {
      result.push(path)
    }
  }
  for (const rootNode of arr) {
    recursiveTraversal(rootNode, [])
  }
  return result
}

/**
 * @description: checkStrictly && pathMode 模式下。 获取每一项放到数组中， [父级， 子级]
 * @param {*} arr
 * @return {*}
 */
const getAllValueIn2DArray = (arr: any[]) => {
  if (!arr || arr?.length === 0) return arr
  let result: any[] = []
  function recursiveTraversal(node: any, path = []) {
    const currentPath: any = [...path, node[fieldNames.value.value!]]
    result.push(currentPath)
    if (node[fieldNames.value.children!] && node[fieldNames.value.children!].length > 0) {
      for (const child of node[fieldNames.value.children!]) {
        recursiveTraversal(child, currentPath)
      }
    }
  }
  for (const rootNode of arr) {
    recursiveTraversal(rootNode, [])
  }
  return result
}

const getAllLevelValues = (arr: any[]) => {
  if (pathMode.value && checkStrictly.value) {
    return getAllValueIn2DArray(arr)
  }
  if (pathMode.value) {
    return getLastLevelValuesIn2DArray(arr)
  }
  if (checkStrictly.value) {
    return getAllValueInArray(arr)
  }
}

const allValue = computed(() => {
  if (checkStrictly.value || pathMode.value) {
    return getAllLevelValues(options.value)
  } else {
    return getLastLevelValues(options.value)
  }
})

const handlePopupVisibleChange = (val: boolean) => {
  if (!val) {
    emits('popupVisibleChange', modelValue.value)
  }
}

const handleClear = () => {
  emits('clear')
}

/**
 * @description: 初始化时设置缓存值
 * @return {*}
 */
const initValue = () => {
  if (!modelValue.value || modelValue.value.length === 0) {
    curValue.value = []
    setCacheValue([])
    return
  }

  // 获取所有叶子节点的值
  const allLeafValues = getAllLeafValues(options.value)

  // 检查是否选择了所有叶子节点
  const modelWithoutAll = modelValue.value.filter(v => v !== 'all')
  const hasAllLeafNodes =
    allLeafValues.length > 0 &&
    allLeafValues.every(leaf => modelWithoutAll.includes(leaf)) &&
    modelWithoutAll.length === allLeafValues.length

  if (hasAllLeafNodes && !modelValue.value.includes('all')) {
    // 如果选择了所有叶子节点但没有全选标识，添加全选标识
    curValue.value = ['all', ...allLeafValues]
  } else {
    curValue.value = modelValue.value
  }

  setCacheValue(curValue.value!)
}

const addTagPropsToTree = (tree: any) => {
  function traverse(node: any) {
    if (node) {
      node.tagProps = tagProps.value

      if (node[fieldNames.value.children!] && node[fieldNames.value.children!].length > 0) {
        node[fieldNames.value.children!].forEach(traverse)
      }
    }
  }

  tree.forEach(traverse)

  return tree
}

/**
 * @description: 基础多选模式下的全选半选状态
 * @return {*}
 */
const indeterminateCheckedStatus = (value: any, allInValue = true) => {
  let checked = false
  let indeterminate = false
  let allValueLength = allValue.value?.length
  if (allInValue) {
    allValueLength = allValue.value?.length
  } else {
    allValueLength = allValue.value?.length - 1
  }
  if (allValue.value?.length === 0) {
    checked = false
    indeterminate = false
    return { checked, indeterminate }
  }
  if (value?.length === allValueLength) {
    checked = true
    indeterminate = false
  } else if (value?.length === 0) {
    checked = false
    indeterminate = false
  } else {
    checked = false
    indeterminate = true
  }

  return { checked, indeterminate }
}

/**
 * @description: 处理选项(添加全选选项)
 * @return {*}
 */
const handleOptions = () => {
  if (!multiple.value) return

  const allObj: Record<string, any> = fieldNames.value
    ? { [fieldNames.value.value!]: 'all', [fieldNames.value.label!]: '全选' }
    : { value: 'all', label: '全选' }

  if (options.value?.length > 0) {
    const firstLabel = options.value[0][fieldNames.value.label!]
    if (firstLabel === '全选') return
    options.value.unshift(allObj)
  }
  if (tagProps.value) {
    addTagPropsToTree(options.value)
  }
}

onMounted(() => {
  if (multiple.value) {
    handleOptions()
    initValue()
  }
})
onBeforeUnmount(() => {
  changeSource.value = ''
})

defineExpose({ handleChange, indeterminateCheckedStatus })
</script>

<template>
  <div class="ft-cascader" ref="cascadeRef">
    <!--  :allow-search="false" -->
    <el-cascader
      v-model="curValue"
      :options="options"
      :clearable="clearable"
      :props="cascaderProps"
      :max-collapse-tags="maxTagCount"
      collapse-tags
      :show-all-levels="!pathMode"
      :placeholder="placeholder"
      @change="(val: any) => handleChange(val)"
      @visible-change="handlePopupVisibleChange"
      @clear="handleClear"
      :style="{ width: '100%' }"
    >
    </el-cascader>
  </div>
</template>

<style lang="scss">
.ft-cascader {
  width: 100%;
  /* display: inline-block; */
  .el-tag {
    /* background-color: #f2f3f5 !important; */
  }
  .el-select-view-multiple {
    /* border: none; */
  }
  .el-select-view-multiple.el-select-view-size-medium .el-select-view-inner {
    /* white-space: nowrap; */
  }
}
</style>
