<script setup lang="ts">
import DataResourceCascaderLinkage from '../components/DataResourceCascaderLinkage.vue'
import CascaderSingleInput from '../components/CascaderSingleInput.vue'

// 中文注释：组件用于按行渲染“取值输入”区域（select-input-class 内部内容）
// 只解构实际使用到的 props，避免未使用变量告警
const { row, InputComponentEnum, condFieldConfig, getInputComponent } = defineProps<{
  row: any
  // 输入组件类型枚举
  InputComponentEnum: Record<string, string>
  // 各条件字段配置（如 options/remote 等）
  condFieldConfig: Record<string, any>
  // 根据当前行判断使用的输入组件
  getInputComponent: (row: any) => string
}>()
</script>

<template>
  <!-- eslint-disable vue/no-mutating-props -->
  <template v-if="getInputComponent(row) === InputComponentEnum.SelectSingle">
    <el-select-v2
      v-model="row.value"
      :loading="!!condFieldConfig[row.conditionType].loading"
      :options="condFieldConfig[row.conditionType].options"
      :remote="condFieldConfig[row.conditionType].remote"
      :props="condFieldConfig[row.conditionType].props"
      :remote-method="condFieldConfig[row.conditionType].remoteMethod"
      @change="condFieldConfig[row.conditionType].change"
      filterable
      clearable
      :height="320"
    />
  </template>

  <template v-else-if="getInputComponent(row) === InputComponentEnum.SelectMultiple">
    <el-select-v2
      v-model="row.value"
      multiple
      collapse-tags
      collapse-tags-tooltip
      filterable
      clearable
      :props="condFieldConfig[row.conditionType].props"
      :options="condFieldConfig[row.conditionType].options"
      :height="320"
    />
  </template>

  <template v-else-if="getInputComponent(row) === InputComponentEnum.Input">
    <el-input
      v-model.trim="row.value"
      :maxlength="condFieldConfig[row.conditionType]?.maxlength"
      show-word-limit
      placeholder="请输入"
    />
  </template>

  <template v-else-if="getInputComponent(row) === InputComponentEnum.CascaderSingle">
    <CascaderSingleInput
      v-model="row.value"
      :options="condFieldConfig[row.conditionType].options"
      :props="condFieldConfig[row.conditionType].props"
      @change="condFieldConfig[row.conditionType].change"
    />
  </template>

  <template v-else-if="getInputComponent(row) === InputComponentEnum.CascaderMultiple">
    <!-- 说明：Popover + 两级级联（左一级单选、右二级多选），输出统一对象结构并支持回显 -->
    <DataResourceCascaderLinkage
      v-model="row.value"
      :loading="!!condFieldConfig[row.conditionType].loading"
      :prefix="condFieldConfig[row.conditionType].prefix"
      :options="condFieldConfig[row.conditionType].options"
    />
  </template>
</template>
