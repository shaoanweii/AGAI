<script setup lang="ts">
import { useUserStore } from '@/store'
import { findNodeByField } from '@/utils'
import { isEqual } from 'lodash-es'
import { computed, ref, nextTick } from 'vue'
import { BrandServiceCategoryOptions } from '@/components/Business/Scene/CompetitorAnalysis/constants'

defineOptions({
  name: 'BrandServiceCategorySwitch'
})

const emits = defineEmits(['change'])

const userStore = useUserStore()

const modelValue = ref<any>()
const popoverVisible = ref(false)
const cascaderValue = ref<any>([])

// 处理级联选择器变化
function handleCascaderChange(value: any) {
  // 如果点击已选中的选项（value为空或相同），阻止取消选中，保持原状态
  if (!value || (Array.isArray(value) && value.length === 0)) {
    popoverVisible.value = false
    return
  }

  const selectedKey = Array.isArray(value) ? value[value.length - 1] : value
  const selectedNode = findNodeByField(getOptions.value, selectedKey)
  cascaderValue.value = value
  modelValue.value = {
    key: selectedNode?.key,
    value: selectedNode?.value,
    path: value // 完整路径
  }

  emits('change', modelValue.value)
  popoverVisible.value = false // 关闭 popover
}

const getOptions = computed<any[]>(() => {
  return BrandServiceCategoryOptions
})

/**
 * @description: 设置回显的值
 * @param {*} value
 * @return {*}
 */
const setCascaderValue = (value: any) => {
  const selectedKey = Array.isArray(value) ? value[value.length - 1] : value
  const selectedNode = findNodeByField(getOptions.value, selectedKey)
  cascaderValue.value = value
  modelValue.value = {
    key: selectedNode?.key,
    value: selectedNode?.value,
    path: value // 完整路径
  }

  return modelValue.value
}

// 避免2次点击，自动展开级联面板
const popoverRef = ref()
const onPopoverShow = () => {
  nextTick(() => {
    // console.log('modelValue.value', modelValue.value)

    // 回显已选择的值
    cascaderValue.value = modelValue.value?.path
  })
}

defineExpose({
  setCascaderValue
})
</script>

<template>
  <!-- @show="onPopoverShow" -->
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
      <!-- 加teleported避免点击父节点时弹窗自动关闭了 -->
      <el-cascader-panel
        v-model="cascaderValue"
        filterable
        :teleported="false"
        :props="{
          value: 'key',
          label: 'value',
          children: 'children',
          checkStrictly: true,
          checkOnClickLeaf: true
        }"
        :options="getOptions"
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
