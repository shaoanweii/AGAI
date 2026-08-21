<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { LabelTag } from '@/api/common/index.d'
import { findTagLabelByType, getTagLibClientTree } from '@/api/common'
import { ProductFilterTagName, ServiceFilterTagName, TagType } from '@/constants'
import { useRoute } from 'vue-router'
import { getAllByType } from '@/utils/tags'

defineOptions({
  name: 'ExperienceCodeSelector'
})

interface Props {
  modelValue?: string[] // 数组格式，例如: ['code1', 'code2', 'code3', 'code4']
  disabled?: boolean
  pageName?: string
}

interface Emits {
  (e: 'update:modelValue', value: string[]): void
  (
    e: 'change',
    data: {
      lastLevelCodes: string[]
      lastLevelIds: string[]
      names: string[]
      source?: 'user'
    }
  ): void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
})

const emit = defineEmits<Emits>()
const route = useRoute()

const tagOptions = ref<LabelTag[]>([])

// 使用独立的 ref 来存储每个级别的值
const firstCodeTag = ref<string[] | undefined>(undefined)
const secondCodeTag = ref<string[] | undefined>(undefined)
const threeCodeTag = ref<string[] | undefined>(undefined)
const fourCodeTag = ref<string[] | undefined>(undefined)

// 同步 props.modelValue 数组到本地 ref
// modelValue 格式: [level1Codes, level2Codes, level3Codes, level4Codes]
// 每个元素都是数组或字符串
watch(
  () => props.modelValue,
  newValue => {
    if (Array.isArray(newValue) && newValue.length > 0) {
      const [first, second, three, four] = newValue
      firstCodeTag.value = first ? (Array.isArray(first) ? first : [first]) : undefined
      secondCodeTag.value = second ? (Array.isArray(second) ? second : [second]) : undefined
      threeCodeTag.value = three ? (Array.isArray(three) ? three : [three]) : undefined
      fourCodeTag.value = four ? (Array.isArray(four) ? four : [four]) : undefined
    } else {
      firstCodeTag.value = undefined
      secondCodeTag.value = undefined
      threeCodeTag.value = undefined
      fourCodeTag.value = undefined
    }
  },
  { immediate: true, deep: true }
)

// 获取标签选项
const getTagOptions = async (tagType: TagType) => {
  try {
    const res = await findTagLabelByType({ tagLibType: tagType })
    return res.result
  } catch {
    return []
  }
}

// 获取一到四级标签
const getFourLevelOptions = async (tagType: TagType) => {
  try {
    const res = await getTagLibClientTree({ tagLibType: tagType })
    return res.result
  } catch {
    return []
  }
}

// 初始化标签数据
const init = async () => {
  const _PRName = props.pageName || (route.name as string)
  if (['journeyAnalysis'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.UserJourney)
    tagOptions.value = getAllByType(TagType.UserJourney, _PRName, _tagTree)
  } else if (['serviceAnalysis'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.Domain)
    tagOptions.value = _tagTree.filter((item: LabelTag) => item.tagName === ServiceFilterTagName)
  } else if (['productAnalysis'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.Domain)
    tagOptions.value = _tagTree.filter((item: LabelTag) => item.tagName === ProductFilterTagName)
  } else if (
    [
      'voiceManagement',
      'selfServiceOriginalSoundQuery',
      'rootCause',
      'ResultData',
      'OriginalData'
    ].includes(_PRName)
  ) {
    const _tagTree = await getFourLevelOptions(TagType.Domain)
    tagOptions.value = _tagTree
  } else if (['newCarLaunch'].includes(_PRName)) {
    const _tagTree = await getFourLevelOptions(TagType.Domain)
    tagOptions.value = _tagTree
  } else {
    // 默认情况，如果没有匹配的路由，使用空数组
    tagOptions.value = []
  }
}

// 根据选中的标签代码从指定选项列表中获取子选项
const getChildrenByCodes = (
  parentCodes: string[] | undefined,
  parentOptions: LabelTag[]
): LabelTag[] => {
  if (!parentCodes || parentCodes.length === 0 || parentOptions.length === 0) {
    return []
  }

  const childrenMap = new Map<string, LabelTag>()

  const findChildren = (options: LabelTag[], targetCodes: string[]) => {
    options.forEach(option => {
      if (targetCodes.includes(option.tagCode || '')) {
        if (option.child && option.child.length > 0) {
          option.child.forEach(child => {
            // 使用 tagCode 作为 key 去重
            if (child.tagCode) {
              childrenMap.set(child.tagCode, child)
            }
          })
        }
      }
      // 继续递归查找子节点
      if (option.child && option.child.length > 0) {
        findChildren(option.child, targetCodes)
      }
    })
  }

  findChildren(parentOptions, parentCodes)
  return Array.from(childrenMap.values())
}

// 根据tagCode在树中查找节点
const findNodeByCode = (options: LabelTag[], targetCode: string): LabelTag | null => {
  for (const option of options) {
    if (option.tagCode === targetCode) {
      return option
    }
    if (option.child && option.child.length > 0) {
      const found = findNodeByCode(option.child, targetCode)
      if (found) return found
    }
  }
  return null
}

// 获取末级的tagCode和id，以及每一级的中文名称
const getLastLevelInfo = () => {
  const lastLevelCodes: string[] = []
  const lastLevelIds: string[] = []
  const names: string[] = []

  // 确定末级是哪一级
  let lastLevelCodesArray: string[] | undefined = undefined

  if (fourCodeTag.value && fourCodeTag.value.length > 0) {
    lastLevelCodesArray = fourCodeTag.value
  } else if (threeCodeTag.value && threeCodeTag.value.length > 0) {
    lastLevelCodesArray = threeCodeTag.value
  } else if (secondCodeTag.value && secondCodeTag.value.length > 0) {
    lastLevelCodesArray = secondCodeTag.value
  } else if (firstCodeTag.value && firstCodeTag.value.length > 0) {
    lastLevelCodesArray = firstCodeTag.value
  }

  if (!lastLevelCodesArray || lastLevelCodesArray.length === 0) {
    return { lastLevelCodes: [], lastLevelIds: [], names: [] }
  }

  // 从完整的tagOptions树中查找每一级的节点，获取中文名称
  if (firstCodeTag.value && firstCodeTag.value.length > 0) {
    const firstNames = firstCodeTag.value
      .map(code => {
        const node = findNodeByCode(tagOptions.value, code)
        return node?.tagName || ''
      })
      .filter(Boolean)
    names.push(...firstNames)
  }

  if (secondCodeTag.value && secondCodeTag.value.length > 0) {
    const secondNames = secondCodeTag.value
      .map(code => {
        const node = findNodeByCode(tagOptions.value, code)
        return node?.tagName || ''
      })
      .filter(Boolean)
    names.push(...secondNames)
  }

  if (threeCodeTag.value && threeCodeTag.value.length > 0) {
    const threeNames = threeCodeTag.value
      .map(code => {
        const node = findNodeByCode(tagOptions.value, code)
        return node?.tagName || ''
      })
      .filter(Boolean)
    names.push(...threeNames)
  }

  if (fourCodeTag.value && fourCodeTag.value.length > 0) {
    const fourNames = fourCodeTag.value
      .map(code => {
        const node = findNodeByCode(tagOptions.value, code)
        return node?.tagName || ''
      })
      .filter(Boolean)
    names.push(...fourNames)
  }

  // 获取末级的tagCode和id
  lastLevelCodes.push(...lastLevelCodesArray)

  // 从完整的tagOptions树中查找末级节点，获取id
  lastLevelCodesArray.forEach(code => {
    const node = findNodeByCode(tagOptions.value, code)
    if (node?.id) {
      lastLevelIds.push(node.id)
    }
  })

  return { lastLevelCodes, lastLevelIds, names }
}

// 一级选项
const level1Options = computed(() => tagOptions.value)

// 二级选项：根据一级选中的值获取
const level2Options = computed(() => {
  const firstCodes = firstCodeTag.value
  if (!firstCodes) return []
  return getChildrenByCodes(firstCodes, level1Options.value)
})

// 三级选项：根据二级选中的值获取
const level3Options = computed(() => {
  const secondCodes = secondCodeTag.value
  if (!secondCodes) return []
  return getChildrenByCodes(secondCodes, level2Options.value)
})

// 四级选项：根据三级选中的值获取
const level4Options = computed(() => {
  const threeCodes = threeCodeTag.value
  if (!threeCodes) return []
  return getChildrenByCodes(threeCodes, level3Options.value)
})

// 判断是否显示下一级选择器
const showLevel2 = computed(() => {
  const firstCodes = firstCodeTag.value
  if (!firstCodes) return false
  const hasValue = firstCodes.length > 0
  return hasValue && level2Options.value.length > 0
})

const showLevel3 = computed(() => {
  const secondCodes = secondCodeTag.value
  if (!secondCodes) return false
  const hasValue = secondCodes.length > 0
  return hasValue && level3Options.value.length > 0
})

const showLevel4 = computed(() => {
  const threeCodes = threeCodeTag.value
  if (!threeCodes) return false
  const hasValue = threeCodes.length > 0
  return hasValue && level4Options.value.length > 0
})

// 更新值并触发事件
// 将四个级别的多选值组合成数组格式返回
function updateValue() {
  const result: (string | string[])[] = []
  if (firstCodeTag.value && firstCodeTag.value.length > 0) {
    result.push(firstCodeTag.value)
  }
  if (secondCodeTag.value && secondCodeTag.value.length > 0) {
    result.push(secondCodeTag.value)
  }
  if (threeCodeTag.value && threeCodeTag.value.length > 0) {
    result.push(threeCodeTag.value)
  }
  if (fourCodeTag.value && fourCodeTag.value.length > 0) {
    result.push(fourCodeTag.value)
  }
  emit('update:modelValue', result as string[])
}

// 处理选择变化
function handleChange(level: number) {
  userTriggered.value = true
  if (level === 1) {
    secondCodeTag.value = undefined
    threeCodeTag.value = undefined
    fourCodeTag.value = undefined
  } else if (level === 2) {
    threeCodeTag.value = undefined
    fourCodeTag.value = undefined
  } else if (level === 3) {
    fourCodeTag.value = undefined
  }
  updateValue()
  // 注意：不在这里触发change事件，只在watch中监听末级变化时触发
}

// 保存上一次的值，用于判断哪一级发生了变化
const prevFirstCodeTag = ref<string[] | undefined>(undefined)
const prevSecondCodeTag = ref<string[] | undefined>(undefined)
const prevThreeCodeTag = ref<string[] | undefined>(undefined)
const prevFourCodeTag = ref<string[] | undefined>(undefined)

// 标记是否由用户交互触发（用于避免初始化回显时清空标准观点）
const userTriggered = ref(false)

// 标记 tagOptions 是否已经加载完成
const tagOptionsLoaded = ref(false)

// 监听值变化，只在末级变化时触发change事件
watch(
  [firstCodeTag, secondCodeTag, threeCodeTag, fourCodeTag, tagOptions],
  () => {
    // 确保tagOptions已加载
    if (tagOptions.value.length === 0) {
      tagOptionsLoaded.value = false
      return
    }

    // 标记 tagOptions 已加载
    if (!tagOptionsLoaded.value) {
      tagOptionsLoaded.value = true
    }

    // 判断是否清空了所有值
    const allEmpty =
      !firstCodeTag.value && !secondCodeTag.value && !threeCodeTag.value && !fourCodeTag.value

    // 判断哪一级发生了变化
    const firstChanged =
      JSON.stringify(firstCodeTag.value) !== JSON.stringify(prevFirstCodeTag.value)
    const secondChanged =
      JSON.stringify(secondCodeTag.value) !== JSON.stringify(prevSecondCodeTag.value)
    const threeChanged =
      JSON.stringify(threeCodeTag.value) !== JSON.stringify(prevThreeCodeTag.value)
    const fourChanged = JSON.stringify(fourCodeTag.value) !== JSON.stringify(prevFourCodeTag.value)

    // 判断当前选中的最高级别
    const hasFourLevel = fourCodeTag.value && fourCodeTag.value.length > 0
    const hasThreeLevel = threeCodeTag.value && threeCodeTag.value.length > 0
    const hasTwoLevel = secondCodeTag.value && secondCodeTag.value.length > 0
    const hasOneLevel = firstCodeTag.value && firstCodeTag.value.length > 0

    // 判断是否是当前已选最深层级的变化
    // 业务要求：无论切换一级、二级、三级还是四级，都要按“当前最深已选层级”联动标准观点
    // 因此这里不再要求“必须是实际叶子节点”，只要当前最深已选层级变化，就触发 change
    // 其他触发条件：
    // 1. 清空所有值
    // 2. 清空任意层级的值（用于清空标准观点）
    // 3. tagOptions 刚加载完成且有值（用于初始化时触发 change 事件）
    let isLastLevelChange = false
    let isAnyLevelCleared = false
    let isInitialLoadWithValue = false

    // 判断是否是 tagOptions 刚加载完成且有默认值的情况
    // 这种情况发生在：tagOptions 从空变为有值，且此时已经有选中的值，但 prevFirstCodeTag 等还是 undefined
    if (
      tagOptionsLoaded.value &&
      !prevFirstCodeTag.value &&
      !prevSecondCodeTag.value &&
      !prevThreeCodeTag.value &&
      !prevFourCodeTag.value &&
      (hasOneLevel || hasTwoLevel || hasThreeLevel || hasFourLevel)
    ) {
      isInitialLoadWithValue = true
    }

    // 判断是否有任意层级被清空（从有值变为无值）
    if (
      (prevFirstCodeTag.value && prevFirstCodeTag.value.length > 0 && !hasOneLevel) ||
      (prevSecondCodeTag.value && prevSecondCodeTag.value.length > 0 && !hasTwoLevel) ||
      (prevThreeCodeTag.value && prevThreeCodeTag.value.length > 0 && !hasThreeLevel) ||
      (prevFourCodeTag.value && prevFourCodeTag.value.length > 0 && !hasFourLevel)
    ) {
      isAnyLevelCleared = true
    }

    if (fourChanged && hasFourLevel) {
      // 四级变化，四级始终是当前最深已选层级
      isLastLevelChange = true
    } else if (threeChanged && hasThreeLevel && !hasFourLevel) {
      // 三级变化，且当前没有选四级，说明三级是当前最深已选层级
      isLastLevelChange = true
    } else if (secondChanged && hasTwoLevel && !hasThreeLevel && !hasFourLevel) {
      // 二级变化，且当前没有选三级和四级，说明二级是当前最深已选层级
      isLastLevelChange = true
    } else if (firstChanged && hasOneLevel && !hasTwoLevel && !hasThreeLevel && !hasFourLevel) {
      // 一级变化，且当前没有选二级、三级、四级，说明一级是当前最深已选层级
      isLastLevelChange = true
    }

    // 在末级变化、清空所有值、清空任意层级或初始化加载完成且有值时触发change事件
    if (isLastLevelChange || allEmpty || isAnyLevelCleared || isInitialLoadWithValue) {
      const info = getLastLevelInfo()
      const source = userTriggered.value ? 'user' : undefined
      emit('change', source ? { ...info, source } : info)
    }

    // 更新上一次的值
    prevFirstCodeTag.value = firstCodeTag.value
    prevSecondCodeTag.value = secondCodeTag.value
    prevThreeCodeTag.value = threeCodeTag.value
    prevFourCodeTag.value = fourCodeTag.value

    if (userTriggered.value) {
      userTriggered.value = false
    }
  },
  { deep: true }
)

// 初始化
init()

// 获取完整的标签路径信息（包含每一级的 code 和 name）
const getTagPath = (): Array<{ code: string; name: string; level: number }> => {
  const tagPath: Array<{ code: string; name: string; level: number }> = []

  // 添加所有一级标签
  if (firstCodeTag.value && firstCodeTag.value.length > 0) {
    firstCodeTag.value.forEach(code => {
      const node = findNodeByCode(tagOptions.value, code)
      if (node) {
        tagPath.push({ code, name: node.tagName || '', level: 1 })
      }
    })
  }

  // 添加所有二级标签
  if (secondCodeTag.value && secondCodeTag.value.length > 0) {
    secondCodeTag.value.forEach(code => {
      const node = findNodeByCode(tagOptions.value, code)
      if (node) {
        tagPath.push({ code, name: node.tagName || '', level: 2 })
      }
    })
  }

  // 添加所有三级标签
  if (threeCodeTag.value && threeCodeTag.value.length > 0) {
    threeCodeTag.value.forEach(code => {
      const node = findNodeByCode(tagOptions.value, code)
      if (node) {
        tagPath.push({ code, name: node.tagName || '', level: 3 })
      }
    })
  }

  // 添加所有四级标签
  if (fourCodeTag.value && fourCodeTag.value.length > 0) {
    fourCodeTag.value.forEach(code => {
      const node = findNodeByCode(tagOptions.value, code)
      if (node) {
        tagPath.push({ code, name: node.tagName || '', level: 4 })
      }
    })
  }

  return tagPath
}

// 暴露方法给父组件
defineExpose({
  getLastLevelInfo,
  getTagPath,
  findNodeByCode,
  tagOptions
})
</script>

<template>
  <div class="experience-code-selector">
    <el-select
      v-model="firstCodeTag"
      placeholder="请选择"
      clearable
      filterable
      multiple
      :max-collapse-tags="1"
      :collapse-tags="true"
      :options="level1Options"
      :props="{ label: 'tagName', value: 'tagCode' }"
      :disabled="disabled"
      @change="handleChange(1)"
    />
    <el-select
      v-if="showLevel2"
      v-model="secondCodeTag"
      placeholder="请选择"
      clearable
      filterable
      multiple
      :max-collapse-tags="1"
      :collapse-tags="true"
      :options="level2Options"
      :props="{ label: 'tagName', value: 'tagCode' }"
      :disabled="disabled"
      @change="handleChange(2)"
    />
    <el-select
      v-if="showLevel3"
      v-model="threeCodeTag"
      placeholder="请选择"
      clearable
      filterable
      multiple
      :max-collapse-tags="1"
      :collapse-tags="true"
      :options="level3Options"
      :props="{ label: 'tagName', value: 'tagCode' }"
      :disabled="disabled"
      @change="handleChange(3)"
    />
    <el-select
      v-if="showLevel4"
      v-model="fourCodeTag"
      placeholder="请选择"
      clearable
      filterable
      multiple
      :max-collapse-tags="1"
      :collapse-tags="true"
      :options="level4Options"
      :props="{ label: 'tagName', value: 'tagCode' }"
      :disabled="disabled"
      @change="handleChange(4)"
    />
  </div>
</template>

<style lang="scss" scoped>
// .experience-code-selector {
//   width: 100%;
//   display: flex;
//   gap: 12px;

//   // .el-select {
//   //   flex: 1;
//   // }
// }
</style>
<style lang="scss">
.experience-code-selector {
  width: 100%;
  display: flex;
  gap: 12px;

  .el-select {
    flex: 1;
  }
}
</style>
