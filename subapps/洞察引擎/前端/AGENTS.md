# AGENTS.md

## 1. 目的

本文件提供给在本仓库执行任务的 agent，统一命令、规范与边界，减少误改。

## 2. 项目概览

- 技术栈：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus
- 包管理器：npm
- 构建配置：`vite.config.mts`
- 路由模式：Hash（`createWebHashHistory`）
- 类型检查：`vue-tsc`

## 3. 构建/Lint/检查命令（来源：package.json）

### 3.1 安装与开发

- `npm install`
- `npm run dev`

### 3.2 质量检查

- `npm run lint`（ESLint + 自动修复）
- `npm run type-check`

### 3.3 构建

- `npm run build`
- `npm run build-only`
- `npm run build:dev`
- `npm run build:test`
- `npm run build:rc`
- `npm run build:prod`
- `npm run postbuild`（执行 `node ./build/build.js`）

## 4. 测试现状与单测命令

当前仓库未配置测试脚本与测试配置：

- `package.json` 无 `test` / `test:unit`
- 未发现 `vitest.config.*` / `jest.config.*` / `cypress.config.*` / `playwright.config.*`
  结论：当前“运行单个测试文件/用例”的命令为 N/A。

## 5. 单测命令模板（仅后续引入 Vitest 时适用）

- 单文件：`npx vitest run path/to/file.test.ts`
- 单用例：`npx vitest run path/to/file.test.ts -t "用例名"`
- 监听模式：`npx vitest path/to/file.test.ts`
  说明：以上是模板，不代表当前仓库可直接执行。

## 6. 代码风格：Prettier（来源：prettier.config.js）

- `printWidth: 100`
- `tabWidth: 2`
- `useTabs: false`
- `semi: false`
- `singleQuote: true`
- `trailingComma: 'none'`
- `arrowParens: 'avoid'`
- `bracketSpacing: true`
- `endOfLine: 'auto'`

## 7. 代码风格：ESLint（来源：.eslintrc.js）

- 扩展：`plugin:vue/vue3-essential`、`plugin:@typescript-eslint/recommended`、`plugin:prettier/recommended`
- 解析器：`vue-eslint-parser` + `@typescript-eslint/parser`
- 关键规则：
  - `vue/multi-word-component-names`: 关闭
  - `@typescript-eslint/no-explicit-any`: 关闭
  - `@typescript-eslint/no-unused-vars`: warn
  - `vue/no-setup-props-destructure`: 关闭

## 8. TypeScript 约定（来源：tsconfig\*.json）

- 路径别名：`@/* -> src/*`
- `noImplicitAny: false`（允许隐式 any）
- 现状中 `type/interface` 与 `any` 并存；新增代码优先补齐显式类型

## 9. 导入规范

- 优先 `@/` 导入 `src` 内模块
- 相对路径用于同目录与近邻模块
- 仓库未启用 `import/order` 强制规则
- 建议顺序：第三方依赖 -> 内部模块 -> 样式/资源

## 10. 命名与结构规范

### 10.1 命名

- 组件文件：PascalCase（如 `StandardPointFormDialog.vue`）
- hooks/composables：`useXxx`（如 `useConditions.ts`）
- store 模块：`src/stores/modules/*`
- 类型名：PascalCase
- 常量：语义化命名，必要时全大写

### 10.2 目录分层

- `src/api`：请求封装
- `src/views`：页面业务
- `src/components`：通用组件
- `src/hooks`：复用逻辑
- `src/stores`：状态管理
- `src/router`：路由
- `src/utils`：工具函数
- `src/types`：类型定义
- `src/style`、`src/assets`：样式与资源

## 11. Vue 与状态管理规范

- 统一使用 `<script setup lang="ts">`
- 统一采用组合式 API（`ref/reactive/computed/onMounted`）
- 与 Element Plus 现有用法保持一致
- Pinia 的异步逻辑集中在 actions，避免在视图层散落状态流程

## 12. 错误处理规范

- 允许 `try/catch` 与 `await-to-js` 两种模式
- 用户可见错误统一通过 `ElMessage.error/warning/success`
- API 层应处理鉴权过期、通用错误提示与异常透传
- 避免静默吞错，至少返回错误或记录日志

## 13. 注释与文档规范

- 对话、注释、文档统一中文
- 新增或修改的代码必须包含注释（至少一处）
- 每个新增函数/方法需补基础注释，至少说明用途、关键参数、返回值或副作用，优先中文
- 复杂逻辑块需补“步骤型详细注释”，说明为什么这么做、分支意图、边界条件与异常/兜底策略
- 涉及数据转换、权限判断、图表拼装等业务逻辑时，注释密度需提高一个档位，重点解释业务意图与约束
- 对外导出的工具函数默认补充 JSDoc 风格注释，适用范围包含 `src/utils`、`src/hooks`、`src/stores`，以及 `src/api` 的接口封装函数
- 注释重点解释“为什么”，说明业务意图、关键判断或约束条件
- 涉及魔法值、分支条件、接口入参约定时，必须注明原因
- 禁止无信息注释（如“给变量赋值”），注释应解释设计意图与业务原因，不得仅复述代码字面含义

## 14. Agent 执行流程（建议）

每次改动后按顺序执行：

1. `npm run lint`
2. `npm run type-check`
3. 需要时执行 `npm run build`
   输出结果时给出改动文件与验证命令。

## 15. 禁止事项

- 未经用户明确要求，不执行 Git 提交/推送
- 不擅自修改 Vite alias 与核心构建配置
- 不删除或重排现有目录结构
- 不引入新的 UI 体系（当前标准为 Element Plus）
- 不批量覆盖用户手工样式改动

## 16. Cursor/Copilot 规则整合

仓库中未发现：

- `.cursor/rules/`
- `.cursorrules`
- `.github/copilot-instructions.md`

## 17. 其他规则整合（Augment）

发现并纳入：

- `.augment/rules/imported/README.md`
- `.augment/rules/imported/project-guide.md`
  提炼约束：
- 使用中文沟通、注释与文档
- 使用 Vue3 + TS + `<script setup>`
- 优先复用既有 hooks、组件与模式
- 避免擅改目录结构、alias 与既有样式约定

## 18. 关键信息来源

- `package.json`
- `README.md`
- `.eslintrc.js`
- `prettier.config.js`
- `tsconfig.json`
- `tsconfig.app.json`
- `tsconfig.node.json`
- `vite.config.mts`
- `.augment/rules/imported/README.md`
- `.augment/rules/imported/project-guide.md`
