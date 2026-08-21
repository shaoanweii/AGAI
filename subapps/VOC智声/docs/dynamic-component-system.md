# 动态组件系统使用指南

## 概述

本系统实现了一个全局动态组件渲染机制，可以根据不同的场景动态展示不同的弹窗组件。这种设计使得后期添加新的弹窗组件变得非常简单和灵活。

## 系统架构

### 1. Store 状态管理 (`src/store/modules/generalScenario.ts`)

```typescript
export const useGeneralScenarioStore = defineStore('generalScenario', () => {
  // 状态定义
  const visible = ref<boolean>(false)        // 弹窗显示状态
  const componentName = ref<string>('')      // 当前组件名称

  // 方法定义
  const handleOpen = (cName: string = 'GeneralScenario') => {
    visible.value = true
    componentName.value = cName
  }
  
  const handleClose = () => {
    visible.value = false
  }

  const switchComponent = (cName: string) => {
    componentName.value = cName
  }

  return {
    visible,
    componentName,
    handleOpen,
    handleClose,
    switchComponent
  }
})
```

### 2. 全局组件渲染 (`src/layout/index.vue`)

```vue
<template>
  <div class="layout">
    <!-- 其他布局内容 -->
    
    <!-- 动态组件渲染 -->
    <component 
      v-if="generalScenarioStore.visible && generalScenarioStore.componentName" 
      :is="componentMap[generalScenarioStore.componentName]"
    ></component>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
// 导入所有可能的弹窗组件
import GeneralScenario from '@/components/Business/GeneralScenario/index.vue'
import CompetitorAnalysis from '@/components/Business/CompetitorAnalysis/index.vue'

// 组件映射表类型定义
type ComponentMap = Record<string, Component>

// 组件映射表 - 根据组件名称动态渲染对应组件
const componentMap: ComponentMap = {
  GeneralScenario,
  CompetitorAnalysis,
  // 后续可以添加更多组件映射
  // 也可以使用懒加载方式
  // LazyComponent: () => import('@/components/Business/LazyComponent/index.vue'),
}
</script>
```

## 使用方法

### 1. 基本使用

```typescript
// 在任何组件中使用
import { useGeneralScenarioStore } from '@/store'

const generalScenarioStore = useGeneralScenarioStore()

// 打开综合分析组件
const openGeneralScenario = () => {
  generalScenarioStore.handleOpen('GeneralScenario')
}

// 打开竞品分析组件
const openCompetitorAnalysis = () => {
  generalScenarioStore.handleOpen('CompetitorAnalysis')
}

// 关闭弹窗
const closeModal = () => {
  generalScenarioStore.handleClose()
}

// 切换组件（在弹窗已打开的情况下）
const switchToOtherComponent = () => {
  generalScenarioStore.switchComponent('CompetitorAnalysis')
}
```

### 2. 在模板中使用

```vue
<template>
  <div>
    <el-button @click="openGeneralScenario">打开综合分析</el-button>
    <el-button @click="openCompetitorAnalysis">打开竞品分析</el-button>
    <el-button @click="closeModal">关闭弹窗</el-button>
    
    <!-- 显示当前状态 -->
    <p>当前组件: {{ generalScenarioStore.componentName || '无' }}</p>
    <p>弹窗状态: {{ generalScenarioStore.visible ? '显示' : '隐藏' }}</p>
  </div>
</template>
```

## 添加新组件

### 1. 创建新组件

```vue
<!-- src/components/Business/NewComponent/index.vue -->
<script setup lang="ts">
import { useGeneralScenarioStore } from '@/store'

defineOptions({
  name: 'NewComponent'
})

const generalScenarioStore = useGeneralScenarioStore()

const handleClosed = () => {
  generalScenarioStore.handleClose()
}
</script>

<template>
  <div v-if="generalScenarioStore.visible" class="new-component">
    <!-- 组件内容 -->
    <div class="header">
      <h2>新组件标题</h2>
      <button @click="handleClosed">关闭</button>
    </div>
    <!-- 其他内容 -->
  </div>
</template>
```

### 2. 注册到组件映射表

```typescript
// src/layout/index.vue
import NewComponent from '@/components/Business/NewComponent/index.vue'

const componentMap: ComponentMap = {
  GeneralScenario,
  CompetitorAnalysis,
  NewComponent,  // 添加新组件
}
```

### 3. 使用新组件

```typescript
// 在任何地方调用
generalScenarioStore.handleOpen('NewComponent')
```

## 最佳实践

### 1. 组件命名规范
- 使用 PascalCase 命名组件
- 组件名称应该具有描述性
- 保持命名一致性

### 2. 懒加载支持
对于大型组件，可以使用懒加载：

```typescript
const componentMap: ComponentMap = {
  GeneralScenario,
  CompetitorAnalysis,
  // 懒加载组件
  HeavyComponent: () => import('@/components/Business/HeavyComponent/index.vue'),
}
```

### 3. 类型安全
确保所有组件都有正确的 TypeScript 类型定义：

```typescript
// 可以定义组件名称的联合类型
type ComponentName = 'GeneralScenario' | 'CompetitorAnalysis' | 'NewComponent'

const handleOpen = (cName: ComponentName) => {
  generalScenarioStore.handleOpen(cName)
}
```

## 注意事项

1. **组件生命周期**：动态组件会在切换时重新创建，注意处理组件内部状态
2. **性能考虑**：对于复杂组件，建议使用懒加载
3. **错误处理**：确保组件映射表中的组件名称正确，避免运行时错误
4. **样式隔离**：每个组件应该有独立的样式作用域

## 示例项目

参考 `src/views/sceneAnalysis/index.vue` 中的完整示例，包含了动态组件的测试界面。
