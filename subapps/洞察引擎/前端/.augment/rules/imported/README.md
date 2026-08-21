---
type: 'always_apply'
---

# VOC 数智平台前端项目规则

> 洞察引擎前端代码的 Augment AI 规则，专注于数据治理、标签管理和项目管理功能开发

## 🚀 项目技术栈

- **Vue 3.4.15** + Composition API + TypeScript 5.3.0
- **Element Plus** + SCSS/Less 样式预处理
- **Vite 5.0.11** + **Pinia 2.1.7** + **Vue Router 4.2.5** (Hash 模式)
- **ECharts 5.5.0** + **Axios 1.6.7** + **Day.js 1.11.10**
- **Lodash-es 4.17.21** + **await-to-js 3.0.0** + **crypto-js 4.2.0**

## ⚠️ 核心约束

### 关键限制

- 🚫 **禁止自动提交代码** - 所有 Git 操作由用户手动控制
- 🚫 **禁止修改目录结构** - 不删除现有目录，不修改 vite.config.mts 别名
- 🚫 **禁止自动调整样式** - 尊重用户手动修改的样式代码
- 🚫 **禁止使用其他 UI 组件库** - 项目使用 Element Plus
- 🚫 **禁止修改 Vite 配置** - 特别是 alias 配置和代理设置

### 开发要求

- ✅ **使用 npm** 包管理器
- ✅ **始终使用中文** - 所有对话、注释、文档都必须使用中文
- ✅ **直接修改代码** 不只给建议
- ✅ **遵循 Vue 3 Composition API** + `<script setup>`
- ✅ **使用 TypeScript 严格模式** - 所有代码必须有明确的类型定义
- ✅ **遵循项目既定模式** - 使用现有的 hooks、工具函数和组件

## 📁 项目结构

```
src/
├── api/           # HTTP 请求封装
│   ├── index.ts   # Axios实例配置
│   ├── main.ts    # 主要接口(登录、权限等)
│   ├── project.ts # 项目管理接口
│   ├── dataCenter.ts # 数据中心接口
│   └── tag.ts     # 标签管理接口
├── assets/        # 静态资源
├── components/    # 公共组件
│   ├── global/    # 全局组件注册
│   ├── FCascader/ # 级联选择器
│   └── FSelect/   # 选择器组件
├── constant/      # 常量定义
├── directives/    # Vue指令
│   └── auth.ts    # 权限指令
├── hooks/         # 组合式函数
│   ├── table.ts   # 表格相关hooks
│   ├── useModal.ts # 弹窗hooks
│   ├── useTagVIewData.ts # 标签时间范围处理
│   └── useTabPermission.ts # 权限控制hooks
├── layouts/       # 布局组件
├── mock/          # 模拟数据
├── router/        # 路由配置 (Hash模式)
├── stores/        # Pinia 状态管理
│   └── modules/   # 状态模块
│       ├── app.ts # 应用状态
│       └── user.ts # 用户状态
├── style/         # SCSS 全局样式
│   ├── global.scss # 全局样式
│   ├── root.scss  # CSS变量定义
│   └── mainTable.scss # 表格样式
├── types/         # TypeScript 类型
├── utils/         # 工具函数
│   ├── index.ts   # 通用工具函数
│   └── permission.ts # 权限相关工具
├── views/         # 页面组件
│   ├── dataCenter/      # 数据治理
│   │   ├── dataProcessing/ # 规则处理
│   │   ├── insDataSource/  # 数据处理
│   │   └── dataFounding/   # 数据资产
│   ├── tagManagement/   # 标签管理
│   │   ├── application/ # 标签应用
│   │   ├── index/       # 标签主页
│   │   └── library/     # 标签库
│   ├── project/         # 项目管理
│   │   └── projectManagement/
│   └── settings/        # 系统设置
├── App.vue        # 根组件
├── main.ts        # 入口文件
└── permission.ts  # 权限控制
```

## 🔗 路径别名

```typescript
// 可用的导入别名
import from '@/'           // → src/
```

## 📋 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器 (http://localhost:5175)
npm run dev

# 代码检查
npm run lint

# TypeScript类型检查
npm run type-check

# 构建生产版本
npm run build

# 不同环境构建
npm run build:dev   # 开发环境
npm run build:test  # 测试环境
npm run build:rc    # RC环境
```

## 🛠️ 核心工具和模式

### 常用 Hooks

- `useTable` - 表格数据管理
- `useTagVIewData` - 标签时间范围处理
- `useTabPermission` - 权限控制
- `useModal` - 弹窗管理

### 权限系统

```vue
<template>
  <el-button v-auth="'project:edit'">编辑</el-button>
</template>
```

### API 请求模式

```typescript
import to from 'await-to-js'
const [err, response] = await to(apiFunction())
```

## 📖 详细规范

详细的开发规范、代码标准、Git 规范等请查看：**[完整开发指南](.augment/rules/imported/project-guide.md)**

---

**项目状态**: VOC 数智平台，包含数据治理、标签管理、项目管理等核心功能模块
**环境要求**: Node.js 20.11.0+ | npm 9.0.0+
**开发服务器**: http://localhost:5175
