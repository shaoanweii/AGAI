<script setup lang="ts">
const instance = getCurrentInstance()

const [model, modifiers] = defineModel<any, any>({
  set(value: string) {
    if (modifiers.allTrim) {
      // 清除前后以及中间的空格
      value = value.replace(/\s/g, '')
    }
    if (modifiers.disableChar) {
      // 不允许输入中文
      value = value.replace(/[\u4E00-\u9FA5]/g, '')
    }
    if (modifiers.onlyNum) {
      // 只允许输入数字
      value = value.replace(/[^\d]/g, '')
    }
    if (modifiers.integer) {
      // 只允许输入正整数且不能以0开头
      value = value.replace(/\D/g, '').replace(/^0{1,}/g, '')
    }
    return value
  }
})
</script>

<template>
  <el-input v-model="model" v-bind="$attrs">
    <template v-if="instance?.slots.append" #append>
      <slot name="append"></slot>
    </template>
    <template v-if="instance?.slots.prefix" #prefix>
      <slot name="prefix"></slot>
    </template>
    <template v-if="instance?.slots.prepend" #prepend>
      <slot name="prepend"></slot>
    </template>
    <template v-if="instance?.slots.suffix" #suffix>
      <slot name="suffix"></slot>
    </template>
  </el-input>
</template>

<style scoped lang="scss"></style>
