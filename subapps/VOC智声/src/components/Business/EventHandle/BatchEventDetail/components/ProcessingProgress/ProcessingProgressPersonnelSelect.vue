<script setup lang="ts">
import { computed, nextTick, ref, shallowRef, watch } from 'vue'
import type { InsReportSysDepartVo } from '@/api/common/index.d'
import {
  buildProcessingProgressPersonnelTree,
  getAccountFullLabelByUserId,
  type ProcessingProgressPersonnelTreeNode
} from './personnelTree'

defineOptions({
  name: 'ProcessingProgressPersonnelSelect'
})

const props = withDefaults(
  defineProps<{
    departAccountTree?: InsReportSysDepartVo[]
    placeholder?: string
    disabled?: boolean
    fallbackLabel?: string
    multiple?: boolean
  }>(),
  {
    departAccountTree: () => [],
    placeholder: '请选择处理人员',
    disabled: false,
    fallbackLabel: '',
    multiple: false
  }
)

const model = defineModel<string | string[]>({ default: '' })

const treeRef = ref()
const panelVisible = ref(false)
const query = ref('')
const treeBuilt = ref(false)
const pickedUserId = ref('')
const pickedUserLabel = ref('')
const pickedUserLabelMap = shallowRef<Map<string, string>>(new Map())
const treeData = shallowRef<ProcessingProgressPersonnelTreeNode[]>([])
const defaultExpandedKeys = shallowRef<string[]>([])

const treeProps = {
  value: 'key',
  label: 'label',
  children: 'children',
  disabled: 'disabled'
} as const

const rawDepartAccountTree = computed(() => {
  return Array.isArray(props.departAccountTree) ? props.departAccountTree : []
})

const getPersonnelTreeNode = (data: unknown): ProcessingProgressPersonnelTreeNode | null => {
  if (!data || typeof data !== 'object') {
    return null
  }

  const node = data as Partial<ProcessingProgressPersonnelTreeNode>
  if (
    typeof node.key !== 'string' ||
    typeof node.label !== 'string' ||
    (node.type !== 'dept' && node.type !== 'user')
  ) {
    return null
  }

  return node as ProcessingProgressPersonnelTreeNode
}

const selectedUserIds = computed(() => {
  return Array.isArray(model.value)
    ? model.value.filter(Boolean)
    : model.value
      ? [model.value]
      : []
})

const getDisplayLabelByUserId = (userId: string) => {
  return (
    pickedUserLabelMap.value.get(userId) ||
    (pickedUserId.value === userId ? pickedUserLabel.value : '') ||
    getAccountFullLabelByUserId(rawDepartAccountTree.value, userId) ||
    userId
  )
}

const displayText = computed(() => {
  if (props.multiple) {
    return selectedUserIds.value.map(getDisplayLabelByUserId).join('、')
  }

  const currentUserId = selectedUserIds.value[0]
  if (!currentUserId) return ''

  return getDisplayLabelByUserId(currentUserId) || props.fallbackLabel
})

/**
 * 展开弹层时才构建 TreeV2 数据，避免处理进度默认渲染时遍历全量人员树。
 */
const buildTreeOnDemand = async () => {
  if (!treeBuilt.value) {
    const builtTree = buildProcessingProgressPersonnelTree(rawDepartAccountTree.value)
    treeData.value = builtTree.treeData
    defaultExpandedKeys.value = builtTree.defaultExpandedKeys
    treeBuilt.value = true
  }

  await nextTick()

  const currentKeys = selectedUserIds.value.map(userId => `user:${userId}`)
  if (props.multiple) {
    treeRef.value?.setCheckedKeys?.(currentKeys)
    return
  }

  if (currentKeys[0]) {
    treeRef.value?.setCurrentKey?.(currentKeys[0])
  }
}

const resetBuiltTree = () => {
  treeBuilt.value = false
  treeData.value = []
  defaultExpandedKeys.value = []
}

const filterNode = (keyword: string, data: unknown): boolean => {
  const node = getPersonnelTreeNode(data)
  if (!node) return false

  const q = String(keyword || '')
    .trim()
    .toLowerCase()
  if (!q) return true

  const label = String(node.label || '').toLowerCase()
  const fullLabel = String(node.fullLabel || '').toLowerCase()
  const employeeId = String(node.employeeId || '').toLowerCase()
  if (label.includes(q) || fullLabel.includes(q) || employeeId.includes(q)) return true

  const children = Array.isArray(node.children) ? node.children : []
  return children.some(child => filterNode(q, child))
}

const handleNodeClick = (data: unknown) => {
  const node = getPersonnelTreeNode(data)
  if (!node || node.type !== 'user' || !node.userId) return
  if (props.multiple) return

  pickedUserId.value = node.userId
  pickedUserLabel.value = node.fullLabel || node.label
  model.value = node.userId
  panelVisible.value = false
}

/**
 * 多选模式下只保留人员节点，并将 TreeV2 的 user:key 转回接口需要的 userId。
 */
const handleTreeCheck = () => {
  if (!props.multiple) return

  const checkedNodes = (treeRef.value?.getCheckedNodes?.(true) ||
    []) as ProcessingProgressPersonnelTreeNode[]
  const checkedUsers = checkedNodes.filter(node => node.type === 'user' && node.userId)
  const nextLabelMap = new Map(pickedUserLabelMap.value)

  checkedUsers.forEach(node => {
    if (node.userId) {
      nextLabelMap.set(node.userId, node.fullLabel || node.label)
    }
  })

  pickedUserLabelMap.value = nextLabelMap
  model.value = checkedUsers.map(node => node.userId!) as string[]
}

const clearValue = () => {
  pickedUserId.value = ''
  pickedUserLabel.value = ''
  pickedUserLabelMap.value = new Map()
  model.value = props.multiple ? [] : ''
  treeRef.value?.setCurrentKey?.()
  treeRef.value?.setCheckedKeys?.([])
}

watch(
  () => panelVisible.value,
  visible => {
    if (visible) {
      void buildTreeOnDemand()
      return
    }

    query.value = ''
  }
)

watch(
  () => query.value,
  value => {
    treeRef.value?.filter?.(value)
  }
)

watch(
  () => rawDepartAccountTree.value,
  () => {
    resetBuiltTree()
    if (panelVisible.value) {
      void buildTreeOnDemand()
    }
  }
)

watch(
  () => props.multiple,
  () => {
    clearValue()
  }
)
</script>

<template>
  <el-popover
    v-model:visible="panelVisible"
    placement="bottom-start"
    trigger="click"
    :width="420"
    teleported
    :disabled="props.disabled"
  >
    <template #reference>
      <el-input
        :model-value="displayText"
        :placeholder="props.placeholder"
        clearable
        readonly
        class="processing-progress-personnel-select"
        :disabled="props.disabled"
        @clear="clearValue"
      />
    </template>

    <div class="processing-progress-personnel-select__panel">
      <el-input
        v-model.trim="query"
        placeholder="搜索部门/人员/工号"
        clearable
        class="processing-progress-personnel-select__search"
      />
      <el-tree-v2
        v-if="rawDepartAccountTree.length"
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        :height="280"
        :filter-method="filterNode"
        :default-expanded-keys="defaultExpandedKeys"
        highlight-current
        :show-checkbox="props.multiple"
        :check-strictly="props.multiple"
        :check-on-click-leaf="props.multiple"
        @node-click="handleNodeClick"
        @check="handleTreeCheck"
      />
      <el-empty v-else description="暂无可选人员" :image-size="72" />
    </div>
  </el-popover>
</template>

<style scoped lang="scss">
.processing-progress-personnel-select {
  width: 100%;
}

.processing-progress-personnel-select :deep(.el-input__wrapper) {
  min-height: 36px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dfe4ea inset;
}

.processing-progress-personnel-select__panel {
  width: 100%;
}

.processing-progress-personnel-select__search {
  margin-bottom: 8px;
}
</style>
