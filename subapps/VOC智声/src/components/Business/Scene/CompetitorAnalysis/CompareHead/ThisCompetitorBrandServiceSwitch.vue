<script setup lang="ts">
import { useUserStore } from '@/store'
import { findNodeByField } from '@/utils'
import { computed, ref, nextTick } from 'vue'
import type { brandCarSeriesItem } from '@/api/competitorAnalysis/types'

defineOptions({
  name: 'ThisCompetitorBrandServiceSwitch'
})

const { allBrandOrCarSeriesOptions = [], disabledCodes = [] } = defineProps<{
  allBrandOrCarSeriesOptions?: brandCarSeriesItem[]
  disabledCodes?: string[]
}>()

const emits = defineEmits(['change'])

const modelValue = ref<any>()
const popoverVisible = ref(false)
const cascaderValue = ref<any>([])

// 处理级联选择器变化
function handleCascaderChange(value: any) {
  const selectedKey = Array.isArray(value) ? value[value.length - 1] : value
  const selectedNode = findNodeByField(getOptions.value, selectedKey, 'code')
  cascaderValue.value = value
  modelValue.value = {
    key: selectedNode?.code,
    value: selectedNode?.name,
    path: value // 完整路径
  }

  emits('change', modelValue.value)
  popoverVisible.value = false // 关闭 popover
}

const getOptions = computed(() => {
  // 禁用已选中的选项
  if (disabledCodes.length === 0) return allBrandOrCarSeriesOptions

  const disableNodes = (nodes: any[]): any[] => {
    return nodes.map(node => {
      const newNode = { ...node }
      if (disabledCodes.includes(node.code)) {
        newNode.disabled = true
      }
      if (node.children && node.children.length > 0) {
        newNode.children = disableNodes(node.children)
      }
      return newNode
    })
  }

  return disableNodes(allBrandOrCarSeriesOptions)
})

/**
 * @description: 设置回显的值
 * @param {*} value
 * @return {*}
 */
const setCascaderValue = (value: any) => {
  const selectedKey = Array.isArray(value) ? value[value.length - 1] : value
  const selectedNode = findNodeByField(getOptions.value, selectedKey, 'code')
  cascaderValue.value = value
  modelValue.value = {
    key: selectedNode?.code,
    value: selectedNode?.name,
    path: value // 完整路径
  }

  return modelValue.value
}

// 避免2次点击，自动展开级联面板
const popoverRef = ref()
const selectV2Ref = ref()
const shouldKeepOpen = ref(false) // 标记是否应该保持面板打开

const onPopoverShow = () => {
  nextTick(() => {
    // 使用 popoverRef 精确定位当前组件的输入框，避免多个组件实例冲突
    // const inputEl = popoverRef.value?.popperRef?.contentRef?.querySelector(
    //   '.el-input__inner'
    // ) as HTMLInputElement
    // if (inputEl) {
    //   inputEl.click()
    //   // 重置value， 用于级联出正确的二级数据
    //   const _cacheValue = cascaderValue.value
    //   cascaderValue.value = []
    //   nextTick(() => {
    //     cascaderValue.value = _cacheValue
    //   })
    //   setTimeout(() => {
    //     inputEl.value = ''
    //   })
    // }

    // 使用 el-select-v2 的 ref 直接控制展开状态
    if (selectV2Ref.value) {
      // 重置value， 用于级联出正确的二级数据
      const _cacheValue = cascaderValue.value
      cascaderValue.value = []

      nextTick(() => {
        cascaderValue.value = _cacheValue

        // 延迟一帧后展开下拉面板，确保数据已更新
        nextTick(() => {
          // 通过设置 expanded 状态来打开下拉面板
          if (selectV2Ref.value) {
            shouldKeepOpen.value = true // 标记应该保持打开
            selectV2Ref.value.expanded = true
          }
        })
      })
    }
  })
}

// 监听下拉面板的显示/隐藏变化
const handleVisibleChange = (visible: boolean) => {
  // 如果标记为应该保持打开，且面板要关闭，则强制保持打开
  if (shouldKeepOpen.value && !visible) {
    nextTick(() => {
      if (selectV2Ref.value) {
        selectV2Ref.value.expanded = true
      }
    })
  }
}

// popover 隐藏时关闭下拉面板
const onPopoverHide = () => {
  shouldKeepOpen.value = false // 取消保持打开标记
  if (selectV2Ref.value) {
    selectV2Ref.value.expanded = false
  }
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
    @hide="onPopoverHide"
  >
    <template #reference>
      <span class="ch-switch">切换</span>
    </template>
    <div>
      <!-- 加teleported避免点击父节点时弹窗自动关闭了 -->
      <el-select-v2
        ref="selectV2Ref"
        v-model="cascaderValue"
        filterable
        :props="{
          value: 'code',
          label: 'name'
        }"
        :options="getOptions"
        style="width: fit-content; width: 200px"
        popper-class="w-200"
        :teleported="false"
        @change="handleCascaderChange"
        @visible-change="handleVisibleChange"
      >
        <!-- <template #default="{ item }">
          <span style="margin-right: 8px">{{ item.label }}</span>
          <span style="color: var(--el-text-color-secondary); font-size: 13px">
            {{ item.value }}
          </span>
        </template> -->
      </el-select-v2>

      <!-- <el-cascader
        ref="cascaderRef"
        v-model="cascaderValue"
        style="width: fit-content; width: 200px"
        filterable
        popper-class="w-200"
        :teleported="false"
        :props="{
          value: 'code',
          label: 'name',
          children: 'children',
          checkStrictly: true,
          checkOnClickLeaf: true
        }"
        :options="getOptions"
        @change="handleCascaderChange"
      /> -->
    </div>
  </el-popover>
</template>

<style lang="scss" scoped>
// 全局
.w362 {
  min-width: 362px;
}

.ch-switch {
  font-weight: 400;
  font-size: 16px;
  color: #1677ff;
  line-height: 20px;
  margin-left: 16px;
  cursor: pointer;
}
</style>
