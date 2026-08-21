<script setup lang="ts">
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { findCarSceneCategoryTree } from '@/api/carUsageScenarios'

interface CarSceneQueryFilters {
  usageScenarioFirstList: string[]
  usageScenarioSecondList: string[]
}

interface CarSceneCacheNode {
  value: string
  label: string
  queryValue: string
  children: CarSceneCacheNode[]
}

interface CarSceneCascaderOption {
  value: string
  label: string
  leaf: boolean
  children?: CarSceneCascaderOption[]
}

interface Props {
  placeholder?: string
  popperClass?: string
  disabled?: boolean
  maxCollapseTags?: number
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '不限',
  popperClass: 'car-scene-cascader-popper',
  disabled: false,
  maxCollapseTags: 1
})

const selectedValue = defineModel<string[][]>({
  default: () => []
})

const emit = defineEmits<{
  (e: 'filter-change', value: CarSceneQueryFilters): void
}>()

const loading = ref(false)
const cascaderOptions = ref<CarSceneCascaderOption[]>([])
const cascaderTreeCache = ref<CarSceneCacheNode[]>([])
const nodeMetaMap = ref<Record<string, CarSceneCacheNode>>({})
const allNodesLoaded = ref(false)

/**
 * @description: 统一清洗接口文本字段，避免空值和空格干扰展示与筛选。
 * @param {unknown} value 待清洗字段
 * @return {string} 清洗后的文本
 */
const normalizeText = (value: unknown) => {
  return String(value ?? '').trim()
}

/**
 * @description: 返回空筛选对象，保证父组件在重置场景下拿到稳定字段结构。
 * @return {CarSceneQueryFilters} 空筛选对象
 */
const createEmptyFilters = (): CarSceneQueryFilters => {
  return {
    usageScenarioFirstList: [],
    usageScenarioSecondList: []
  }
}

/**
 * @description: 计算级联节点展示名称，优先使用更贴近业务语义的场景名称。
 * @param {Api.CarUsageScenarios.CategoryNode} node 接口原始节点
 * @return {string} 展示名称
 */
const resolveNodeLabel = (node: Api.CarUsageScenarios.CategoryNode) => {
  return (
    normalizeText(node.sceneName) ||
    normalizeText(node.categoryName) ||
    normalizeText(node.typeName)
  )
}

/**
 * @description: 规范化级联多选回传值，确保始终使用二维路径数组。
 * @param {unknown} value 级联组件回传值
 * @return {string[][]} 标准化后的路径数组
 */
const normalizeSelection = (value: unknown): string[][] => {
  if (!Array.isArray(value)) return []

  return value
    .map(item => {
      if (!Array.isArray(item)) return []
      return item.map(pathValue => String(pathValue)).filter(Boolean)
    })
    .filter(path => path.length)
}

/**
 * @description: 将缓存节点转换为 el-cascader 可消费的数据结构。
 * @param {CarSceneCacheNode} node 缓存节点
 * @param {boolean} includeChildren 是否递归挂载子节点
 * @return {CarSceneCascaderOption} 级联节点
 */
const createCascaderOption = (
  node: CarSceneCacheNode,
  includeChildren = false
): CarSceneCascaderOption => {
  const children = includeChildren
    ? node.children.map(child => createCascaderOption(child, true))
    : undefined

  return {
    value: node.value,
    label: node.label,
    leaf: !node.children.length,
    children
  }
}

/**
 * @description: 将接口树转换为前端缓存树，级联面板展示结构完全跟随后端 children 关系。
 * @param {Api.CarUsageScenarios.CategoryNode[]} nodes 接口树节点
 * @param {string} parentKey 父级唯一 key
 * @param {number} currentDepth 当前递归深度，仅用于兜底生成唯一值
 * @return {CarSceneCacheNode[]} 级联缓存树
 */
const buildCacheTree = (
  nodes: Api.CarUsageScenarios.CategoryNode[] = [],
  parentKey = '',
  currentDepth = 1
): CarSceneCacheNode[] => {
  return (Array.isArray(nodes) ? nodes : [])
    .flatMap((node, index) => {
      const nodeId = normalizeText(node.id) || normalizeText(node.categoryId)
      const label = resolveNodeLabel(node)
      const uniquePart = nodeId || label || `${currentDepth}-${index}`
      const value = parentKey ? `${parentKey}/${uniquePart}` : uniquePart
      const nextNodes = Array.isArray(node.children) ? node.children : []

      if (!label) return []

      const cacheNode: CarSceneCacheNode = {
        value,
        label,
        // 查询接口当前按中文场景名称匹配，这里显式回传展示名称而不是分类 id。
        queryValue: label,
        children: buildCacheTree(nextNodes, value, currentDepth + 1)
      }

      nodeMetaMap.value[value] = cacheNode
      return [cacheNode]
    })
    .filter(Boolean) as CarSceneCacheNode[]
}

/**
 * @description: 基于级联面板层级拆分查询字段，一级面板传一级列表，二级面板传二级列表。
 * @param {string[][]} paths 当前级联选中路径
 * @return {CarSceneQueryFilters} 查询字段
 */
const buildQueryFilters = (paths = selectedValue.value): CarSceneQueryFilters => {
  const usageScenarioFirstSet = new Set<string>()
  const usageScenarioSecondSet = new Set<string>()

  normalizeSelection(paths).forEach(path => {
    const lastValue = path[path.length - 1]
    const targetNode = nodeMetaMap.value[lastValue]
    if (!targetNode?.queryValue) return

    /**
     * @description: 不依赖接口 level 字段，只看用户在级联面板选中的层级位置。
     */
    if (path.length === 1) {
      usageScenarioFirstSet.add(targetNode.queryValue)
      return
    }

    if (path.length >= 2) {
      usageScenarioSecondSet.add(targetNode.queryValue)
    }
  })

  return {
    usageScenarioFirstList: Array.from(usageScenarioFirstSet),
    usageScenarioSecondList: Array.from(usageScenarioSecondSet)
  }
}

/**
 * @description: 向父组件同步场景筛选字段，查询和导出统一复用这一份结果。
 * @return {*}
 */
const emitCurrentFilters = () => {
  emit('filter-change', buildQueryFilters())
}

/**
 * @description: 懒加载读取缓存树，避免级联组件初次渲染时一次性挂载全部节点。
 * @param {any} node 当前展开节点
 * @param {(data: CarSceneCascaderOption[]) => void} resolve 回调函数
 * @return {*}
 */
const lazyLoadCarSceneOptions = (node: any, resolve: (data: CarSceneCascaderOption[]) => void) => {
  if (node.level === 0) {
    resolve(cascaderOptions.value)
    return
  }

  const parentValue = normalizeText(node?.data?.value)
  const parentNode = nodeMetaMap.value[parentValue]
  resolve((parentNode?.children || []).map(child => createCascaderOption(child)))
}

/**
 * @description: 搜索前补齐所有节点，兼顾“懒加载首屏”与“全量搜索”两种交互诉求。
 * @param {string} keyword 搜索关键字
 * @return {boolean | Promise<boolean>} 是否允许执行筛选
 */
const beforeFilter = (keyword: string) => {
  if (!normalizeText(keyword)) return true
  if (!allNodesLoaded.value) {
    cascaderOptions.value = cascaderTreeCache.value.map(node => createCascaderOption(node, true))
    allNodesLoaded.value = true
  }
  return true
}

/**
 * @description: 自定义搜索逻辑，仅按当前节点名称匹配，避免路径文案影响用户输入。
 * @param {any} node 级联节点实例
 * @param {string} keyword 搜索关键字
 * @return {boolean} 是否命中
 */
const filterMethod = (node: any, keyword: string) => {
  const normalizedKeyword = normalizeText(keyword).toLowerCase()
  const currentLabel = normalizeText(node?.text || node?.label).toLowerCase()
  return currentLabel.includes(normalizedKeyword)
}

/**
 * @description: 搜索结果中展示“父级 / 子级”路径，解决同名场景节点难以区分的问题。
 * @param {any} node cascader 搜索结果节点
 * @return {string} 面向搜索结果的路径文本
 */
const formatSuggestionPath = (node: any) => {
  if (!node) return ''
  return normalizeText(node?.calcText?.(true, ' / ') || node?.text || node?.label)
}

/**
 * @description: 拉取用车场景树并转换为级联缓存结构，首屏仅挂载可见首列节点。
 * @return {*}
 */
const refreshOptions = async () => {
  loading.value = true
  try {
    const res = await findCarSceneCategoryTree({})
    nodeMetaMap.value = {}
    cascaderTreeCache.value = buildCacheTree(res.result || [])
    cascaderOptions.value = cascaderTreeCache.value.map(node => createCascaderOption(node))
    allNodesLoaded.value = false
    emitCurrentFilters()
  } catch (error: any) {
    cascaderTreeCache.value = []
    cascaderOptions.value = []
    nodeMetaMap.value = {}
    selectedValue.value = []
    allNodesLoaded.value = false
    emit('filter-change', createEmptyFilters())
    ElMessage.error(error?.message || '获取用车场景筛选条件失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * @description: 清空当前选择，供父组件重置时复用同一套逻辑。
 * @return {*}
 */
const clearSelection = () => {
  selectedValue.value = []
  emit('filter-change', createEmptyFilters())
}

/**
 * @description: 级联值变化时统一标准化为路径数组，避免清空和手动赋值产生脏数据。
 * @param {unknown} value 组件回传值
 * @return {*}
 */
const handleChange = (value: unknown) => {
  selectedValue.value = normalizeSelection(value)
}

const cascaderProps = computed(() => {
  return {
    multiple: true,
    checkStrictly: true,
    emitPath: true,
    lazy: true,
    lazyLoad: lazyLoadCarSceneOptions,
    value: 'value',
    label: 'label',
    children: 'children',
    leaf: 'leaf'
  }
})

watch(
  () => selectedValue.value,
  value => {
    const normalizedValue = normalizeSelection(value)
    if (JSON.stringify(normalizedValue) !== JSON.stringify(value || [])) {
      selectedValue.value = normalizedValue
      return
    }
    emitCurrentFilters()
  },
  {
    deep: true,
    immediate: true
  }
)

onMounted(() => {
  refreshOptions()
})

defineExpose({
  refreshOptions,
  buildQueryFilters,
  clearSelection
})
</script>

<template>
  <div class="car-scene-filter">
    <el-cascader
      :model-value="selectedValue"
      :options="cascaderOptions"
      :props="cascaderProps"
      :placeholder="loading ? '加载中...' : props.placeholder"
      :disabled="props.disabled || loading"
      :show-all-levels="false"
      :collapse-tags="true"
      :collapse-tags-tooltip="true"
      :max-collapse-tags="props.maxCollapseTags"
      :popper-class="props.popperClass"
      clearable
      filterable
      class="w-full"
      @change="handleChange"
      @clear="clearSelection"
      :before-filter="beforeFilter"
      :filter-method="filterMethod"
    >
      <template #suggestion-item="{ item }">
        <span class="car-scene-suggestion__text" :title="formatSuggestionPath(item)">
          {{ formatSuggestionPath(item) }}
        </span>
        <el-icon v-if="item.checked" class="car-scene-suggestion__icon">
          <Check />
        </el-icon>
      </template>
    </el-cascader>
  </div>
</template>

<style scoped lang="scss">
.car-scene-filter {
  width: 100%;
}

.car-scene-suggestion__text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.car-scene-suggestion__icon {
  flex-shrink: 0;
  margin-left: 8px;
}
</style>
