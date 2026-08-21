<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'

defineOptions({
  name: 'TagSwitch'
})

const { options = [] } = defineProps<{
  options: any[]
}>()
const emits = defineEmits(['change'])

const modelValue = ref<any>()
const popoverVisible = ref(false)
const cascaderValue = ref<any>([])

// 根据选中的 code 查找对应的节点信息
const findNodeByCode = (nodes: any[], code: string): any => {
  for (const node of nodes) {
    if (node.tagCode === code) return node
    if (node.child) {
      const found = findNodeByCode(node.child, code)
      if (found) return found
    }
  }
  return null
}

// 递归查找标签路径
const findTagPath = (
  nodes: any[],
  targetCode: string,
  currentPath: Array<{ code: string; name: string }> = []
): Array<{ code: string; name: string }> | null => {
  for (const node of nodes) {
    const newPath = [...currentPath, { code: node.tagCode || '', name: node.tagName || '' }]

    if (node.tagCode === targetCode) {
      return newPath
    }

    if (node.child) {
      const found = findTagPath(node.child, targetCode, newPath)
      if (found) return found
    }
  }
  return null
}

// 处理级联选择器变化
function handleCascaderChange(value: any) {
  const selectedCode = Array.isArray(value) ? value[value.length - 1] : value
  const selectedNode = findNodeByCode(options, selectedCode)
  cascaderValue.value = value

  const tags = findTagPath(options, selectedCode) || []

  modelValue.value = {
    key: selectedNode?.tagName,
    value: selectedNode?.tagCode === 'all' ? '' : selectedNode?.tagCode,
    // value: selectedNode?.tagCode,
    path: value,
    tags
  }

  emits('change', modelValue.value)
  popoverVisible.value = false
}

/**
 * @description: 设置回显的值
 * @param {*} value
 * @return {*}
 */
const setCascaderValue = (value: any) => {
  const selectedKey = Array.isArray(value) ? value[value.length - 1] : value
  const selectedNode = findNodeByCode(options, selectedKey)
  cascaderValue.value = value

  const tags = findTagPath(options, selectedKey) || []

  modelValue.value = {
    key: selectedNode?.tagName,
    value: selectedNode?.tagCode === 'all' ? '' : selectedNode?.tagCode,
    // value: selectedNode?.tagCode,
    path: value,
    tags
  }

  return modelValue.value
}

// 避免2次点击，自动展开级联面板
const popoverRef = ref()
const onPopoverShow = () => {
  nextTick(() => {
    const inputEl = document.querySelector('.el-cascader .el-input__inner') as HTMLInputElement
    if (inputEl) {
      inputEl.click()
      // 重置value， 用于级联出正确的二级数据
      const _cacheValue = cascaderValue.value
      cascaderValue.value = []
      nextTick(() => {
        cascaderValue.value = _cacheValue
      })
      setTimeout(() => {
        inputEl.value = ''
      })
    }
  })
}

defineExpose({
  setCascaderValue
})
</script>

<template>
  <el-popover
    v-model:visible="popoverVisible"
    placement="bottom"
    trigger="click"
    :popper-style="{ padding: 0, width: 'auto', height: 'auto' }"
    ref="popoverRef"
    @show="onPopoverShow"
  >
    <template #reference>
      <el-button round type="info" color="#F2F4F7" class="ml-16">
        <span class="mr-4">切换</span>
        <SvgIcon name="switch-horizontal" width="16px" height="16px" color="#717B8A"></SvgIcon>
      </el-button>
    </template>
    <div>
      <el-cascader
        v-model="cascaderValue"
        style="width: fit-content; width: 362px"
        popper-class="w362"
        filterable
        ref="cascaderRef"
        :teleported="false"
        :props="{
          value: 'tagCode',
          label: 'tagName',
          children: 'child',
          checkStrictly: true,
          checkOnClickLeaf: true
        }"
        :options="options"
        @change="handleCascaderChange"
      />
    </div>
  </el-popover>
</template>

<style lang="scss">
// 全局
.w362 {
  min-width: 362px;
}
</style>
