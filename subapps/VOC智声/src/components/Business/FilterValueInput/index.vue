<script setup lang="ts">
import TagCascader from '@/components/Business/AdvancedFilter/TagCascader.vue'
import BrandCascader from '@/components/Business/AdvancedFilter/BrandCascader.vue'
import MTimeCascader from '@/components/Business/AdvancedFilter/MTimeCascader.vue'
import DataSourceCascader from '@/components/Business/AdvancedFilter/DataSourceCascader.vue'
import { FE_TIME_DIMENSION_OPTIONS } from '@/constants/index'

defineOptions({
  name: 'FilterValueInput'
})

interface Props {
  condition: any
  pageName?: string
  brandOptions?: any[]
  dataSourceOptions?: any[]
  teleported?: boolean
  childKey?: string
  disabled?: boolean
}

const value = defineModel<any>()
const selectedShortcut = defineModel<string>('shortcutValue')

const {
  condition,
  pageName,
  brandOptions,
  teleported = true,
  dataSourceOptions,
  childKey,
  disabled = false
} = defineProps<Props>()

// 获取条件配置
const getConditionConfig = (condition: any) => {
  return condition?.filterType ? condition : {}
}

/**
 * 判断当前筛选类型是否使用品牌级联控件。
 * @param filterType 后端筛选类型编码
 * @returns 是否为品牌筛选类型
 */
const isBrandFilterType = (filterType: unknown) => {
  return ['91', '911'].includes(String(filterType))
}
</script>

<template>
  <div class="filter-value-input">
    <!-- filterType为'1'时显示选择框 -->
    <FSelect
      v-if="getConditionConfig(condition)?.filterType === '1'"
      v-model="value"
      :options="getConditionConfig(condition)?.enumValue || []"
      :fields="{ label: 'value', value: 'key' }"
      :multiple="getConditionConfig(condition)?.multiSelect === true"
      placeholder="请选择"
      class="value-select"
      :disabled="disabled"
    />
    <!-- filterType为'2'时显示输入框 -->
    <el-input
      v-else-if="getConditionConfig(condition)?.filterType === '2'"
      v-model="value"
      placeholder="请输入"
      class="value-input"
      :disabled="disabled"
    />
    <!-- filterType为'93'时显示时间控件 -->
    <div v-else-if="getConditionConfig(condition)?.filterType === '93'">
      <!-- <FDatePicker
        v-model="value"
        v-model:shortcut-value="selectedShortcut"
        size="default"
        style="width: 100%"
        :teleported="teleported"
        :disabled="disabled"
      /> -->
      <el-cascader
        v-model="value"
        :options="FE_TIME_DIMENSION_OPTIONS"
        :props="{
          value: 'code',
          label: 'name',
          children: 'child',
          checkStrictly: false,
          checkOnClickLeaf: false
        }"
        :teleported="teleported"
        style="width: 100%"
      />
    </div>
    <!-- 92 标签 -->
    <TagCascader
      v-else-if="getConditionConfig(condition)?.filterType === '92'"
      v-model="value"
      style="width: 100%"
      :page-name="pageName"
      :teleported="teleported"
      :disabled="disabled"
    />
    <!-- 91 品牌单选 / 911 品牌多选 -->
    <BrandCascader
      v-else-if="isBrandFilterType(getConditionConfig(condition)?.filterType)"
      v-model="value"
      style="width: 100%"
      :condition="getConditionConfig(condition)"
      :options="brandOptions"
      :teleported="teleported"
      :disabled="disabled"
    />
    <!-- 94 移动端时间 -->
    <MTimeCascader
      v-else-if="getConditionConfig(condition)?.filterType === '94'"
      v-model="value"
      style="width: 100%"
      :teleported="teleported"
      :disabled="disabled"
    />
    <!-- 95 数据源 -->
    <DataSourceCascader
      v-else-if="getConditionConfig(condition)?.filterType === '95'"
      v-model="value"
      style="width: 100%"
      :condition="condition"
      :options="dataSourceOptions"
      :teleported="teleported"
      :instance-key="childKey"
      :key="childKey"
      :childKey="childKey"
      :wait-for-parent="dataSourceOptions !== undefined"
      :disabled="disabled"
    />
    <!-- 默认输入框 -->
    <el-input
      v-else
      v-model="value"
      placeholder="请选择范围"
      class="value-input"
      :disabled="disabled"
    />
  </div>
</template>

<style lang="scss" scoped>
.filter-value-input {
  width: 100%;
}
</style>
