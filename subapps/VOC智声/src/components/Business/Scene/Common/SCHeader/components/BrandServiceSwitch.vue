<script setup lang="ts">
import { useUserStore } from '@/store'
import { findNodeByField } from '@/utils'
import { computed, ref, nextTick } from 'vue'

defineOptions({
  name: 'BrandServiceSwitch'
})

const emits = defineEmits(['change'])

const userStore = useUserStore()

const modelValue = ref<any>()
const popoverVisible = ref(false)
const cascaderValue = ref<any>([])

// 处理级联选择器变化
function handleCascaderChange(value: any) {
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

const getOptions = computed(() => {
  // 只展示品牌
  return userStore.getBrandService?.map((item: any) => ({
    ...item,
    children: []
  }))
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
      <!-- 加teleported避免点击父节点时弹窗自动关闭了 -->
      <!--  style="width: fit-content; width: 362px" -->
      <!--   popper-class="w362" -->
      <el-cascader
        v-model="cascaderValue"
        filterable
        :teleported="false"
        style="width: fit-content; width: 200px"
        popper-class="w200"
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
.w200 {
  min-width: 200px;
}
</style>
