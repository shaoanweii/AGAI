<script setup lang="ts">
import { ref } from 'vue'

defineOptions({
  name: 'AFSelect'
})
const value = defineModel<any>()

const {
  options = [],
  multiSelect = false,
  fields = { label: 'itemText', value: 'itemValue' }
} = defineProps<{
  options: any[]
  multiSelect?: boolean
  fields?: {
    label: string
    value: string
  }
}>()

const activeChange = (_value: string) => {
  if (multiSelect) {
    // 初始化为空数组或获取当前值
    const currentValue = Array.isArray(value.value) ? value.value : []
    
    if (currentValue.includes(_value)) {
      // 移除已选中的项
      value.value = currentValue.filter((item: string) => item !== _value)
    } else {
      // 添加新选中的项
      value.value = [...currentValue, _value]
    }
  } else {
    value.value = _value
  }
}

// 判断选项是否选中
const isActive = (item: any) => {
  if (multiSelect) {
    return Array.isArray(value.value) && value.value.includes(item[fields.value])
  } else {
    return value.value === item[fields.value]
  }
}
</script>

<template>
  <div class="af-select">
    <template v-for="(item, index) in options" :key="index">
      <div
        class="afs-item"
        :class="{ 'afs-active': isActive(item) }"
        @click="activeChange(item[fields.value])"
      >
        {{ item[fields.label] }}
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.af-select {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  .afs-item {
    padding: 4px 14px;
    background: #f2f3f5;
    border-radius: 4px 4px 4px 4px;
    font-weight: 400;
    font-size: 12px;
    line-height: 18px;
    color: #1f2733;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    border: 1px solid transparent;
    &.afs-active {
      background: #e2f3fe;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #1677ff;
      font-weight: 600;
      color: #0062ff;
    }
  }
}
</style>
