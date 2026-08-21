# AGENTS.md

本文件面向在本仓库工作的 agentic coding agents。默认使用中文沟通，遵循当前仓库的
Vue 3 + TypeScript + Vite 工具链与既有业务边界。

## 沟通与协作

- 默认使用中文交流，除非用户明确要求其他语言。
- 先读代码、配置和现有实现，再给出方案或修改；不要基于旧文档直接假设。
- 需求不清且会影响实现结果时再提问；可通过仓库事实确认的问题不要打断用户。
- 无法确定应该使用后端哪个字段，且该选择会影响实现结果时，必须向用户确认。
- 不要擅自提交 Git，不要自动创建 commit、tag 或推送远端。
- 工作区可能存在用户改动；不要回滚、覆盖或格式化无关文件。
- 删除、迁移、批量格式化等高影响操作只做用户明确要求的范围。

## 项目概览

- 项目类型：VOC 客情直驱/客户数据分析前端，PC 与 H5 共仓维护。
- 核心框架：Vue 3.5、TypeScript 5.8、Vite 6。
- PC 组件库：Element Plus。
- H5 组件库：Vant，移动端代码集中在 `src/views/H5`。
- 状态管理：Pinia + `pinia-plugin-persistedstate`。
- 路由：Vue Router 4，使用 Hash history。
- 图表：ECharts + `echarts-wordcloud`。
- 请求：Axios，统一封装在 `src/api/http/index.ts`；H5 有独立封装。
- 认证/授权：使用 `@ichangan/ica-sdk` 与项目内 token、权限路由逻辑。

## 常用命令

使用 npm，仓库存在 `package-lock.json`，不要改用 yarn 或 pnpm。

- 安装依赖：`npm install`
- 本地开发：`npm run dev`，Vite 默认端口 `5173`
- 生产构建：`npm run build`，包含 `vue-tsc -b`
- 环境构建：`npm run build:dev` / `npm run build:test` / `npm run build:prod`
- RC 构建：`npm run build:rc`
- 预览构建产物：`npm run preview`
- Lint：`npm run lint`
- Lint 修复：`npm run lint:fix`
- 格式化源码：`npm run format`

### 单文件检查

- 单文件 ESLint：`npx eslint "src/path/to/file.vue"`
- 单文件 Prettier：`npx prettier --write "src/path/to/file.vue"`
- 类型检查：当前无单独脚本，使用 `npm run build` 触发 `vue-tsc -b`

### 测试

- `package.json` 当前没有 `test` 脚本。
- 未发现 `vitest.config.*`、`jest.config.*` 或 `playwright.config.*`。
- 若后续引入测试框架，应同步更新本文件中的测试命令与约定。

## 项目结构

- `src/api`：PC 侧 API，按业务模块组织，类型多为同目录 `types.d.ts`。
- `src/api/http/index.ts`：PC 请求封装、响应包装、鉴权失败处理和错误提示。
- `src/components/Business`：业务组件，如高级筛选、通用筛选、钻取弹窗、事件处理、声音详情与列表。
- `src/components/Charts`：图表组件，包含 ECharts 封装、折线、饼图、地图、词云等。
- `src/components/UI`：基础 UI 组件，如表格、日期选择、空状态、面包屑、卡片、切换控件。
- `src/views`：PC 页面，包含总览、领导总览、场景分析、自助分析、客情直驱、系统管理等。
- `src/views/H5`：H5 独立区域，包含 H5 的 api、components、hooks、layout、router、store、utils、views。
- `src/store/modules`：PC 侧 Pinia store，入口为 `src/store/index.ts`。
- `src/router`：PC 静态路由、动态路由和路由实例。
- `src/permission.ts`：全局路由守卫，区分 PC 与 H5 路由处理。
- `src/hooks`：通用组合式函数。
- `src/directives`：全局指令，当前包含权限相关指令。
- `src/utils`：通用工具函数，包含日期、下载、图表、权限、环境、声音高亮等。
- `src/constants`：常量、权限映射、环境常量、车型/数据源选项等。
- `src/types`：全局类型、图表类型、系统类型、SDK 类型声明。
- `src/styles`：全局 SCSS、Element Plus 覆盖、工具样式和变量。
- `docs`：项目补充文档，如动态组件系统、原声高亮逻辑。

## 架构约定

- PC 与 H5 共用仓库但保持隔离：PC 默认使用 Element Plus，H5 默认使用 Vant。
- H5 代码只放在 `src/views/H5` 及 H5 专属资源目录中，不从 PC 页面反向复用 H5 业务实现。
- PC 动态菜单路由在 `src/router/dynamicRoutes.ts`，后端菜单 `permissionKey` 需与路由 `name` 对齐。
- H5 路由从 `src/views/H5/router/index.ts` 导出，并合入静态路由。
- 全局前置守卫在 `src/permission.ts`：`/h5` 前缀走 H5 守卫，PC 侧根据 token 和权限菜单处理。
- 全局弹窗/动态组件机制参考 `docs/dynamic-component-system.md`，组件映射注册在 `src/layout/index.vue`。
- `src/components/index.ts` 负责公共组件导出；`src/components/global.ts` 会自动注册
  `src/components/UI/*/index.vue`，新增全局 UI 组件时需确认命名、注册范围和类型声明影响。
- H5 侧有独立的基础组件、业务组件、API、store、hooks 和 utils，PC 与 H5 复用时优先保持目录与
  依赖边界隔离。
- SVG 图标通过 `build/svg-icons-plugin.ts` 注册，资源位于 `src/assets/svg`，symbol 格式为 `icon-[dir]-[name]`。
- 构建分包在 `vite.config.ts` 的 `manualChunks` 中维护，避免随意按目录拆分应用代码。

## API 与数据处理

- API 返回优先使用 `BaseResponse<T>`，请求方法统一走 `request.get/post/put/delete`。
- PC 请求基础路径来自 `VITE_API_BASE_URL`，缺省为 `/api`。
- 后端成功码约定为 `data.code === '200'`，封装层会转换为 `BaseResponse` 结构。
- `401`、`100000`、`100046`、`100047` 等鉴权异常会触发登录/SSO 相关处理。
- Blob、文件下载和流式响应必须按现有封装逻辑处理，不要套普通 JSON 解析。
- 取消请求不展示错误提示；普通错误提示统一走 Element Plus `ElMessage`。
- 尽量不要擅自重命名、归一化或改写后端字段语义；缺字段映射依据时向用户确认。
- 可复用的数据格式化、展示转换、字段映射应放到 `src/utils` 或对应业务公共 `utils/`。

## Vue 与 TypeScript 规范

- Vue 组件统一使用 `<script setup lang="ts">`。
- Props 和 Emits 使用 `defineProps<T>()` / `defineEmits<T>()` 的显式类型写法。
- 组件复杂类型优先放同目录 `types.d.ts` 或 `types.ts`；跨模块类型放 `src/types`。
- TypeScript 严格模式开启，避免隐式 `any`，即使 ESLint 未禁止显式 `any` 也应谨慎使用。
- 复杂页面逻辑优先拆分为 `hooks/`、`utils/` 或局部子组件。
- 使用 Pinia store 时保持模块边界清晰，持久化状态需明确业务必要性。
- Vue Router 新增动态路由时，同时确认菜单权限、页面标题、图标和 fallback 行为。

## 代码体量与拆分

- 新增 `.vue`、`.ts`、`.tsx`、`.scss` 文件默认控制在 500 行以内；接近 500 行时优先拆分后再继续堆叠逻辑。
- 既有超过 500 行的文件不要求一次性清理历史代码，但本次需求新增的复杂逻辑应拆到局部子组件、
  `hooks/`、`utils/`、`constants.ts`、`types.ts` 或同业务目录下的辅助模块。
- 触碰既有大文件时只在本次需求相关范围内渐进拆分，不为了满足行数约束扩大无关 diff 或重写业务逻辑。
- 页面组件优先保持“视图编排”职责，数据请求、字段转换、筛选条件拼装、图表 option、复杂事件处理等逻辑
  应按业务边界抽离。
- 大型 `.d.ts` 或类型声明应按业务域拆分，避免把接口、表单、弹窗、图表等无关类型继续堆到同一个文件。
- 抽象应贴近已有目录与命名习惯；只有能减少真实重复或降低复杂度时才新增公共抽象。

## 命名与组织

- 组件文件夹使用 PascalCase，主入口一般为 `index.vue`。
- 基础/业务组件导出入口优先使用对应目录的 `index.ts`。
- Hooks 使用 `useXxx` 命名。
- Store 模块按业务命名，放在 `src/store/modules` 或 H5 专属 store 目录。
- API 模块按业务域命名，接口实现与类型文件保持同目录。
- 页面目录以业务模块聚合，避免把 PC 与 H5 页面混放。
- 导入优先使用别名，如 `@/utils/date`、`@components/...`、`@h5/...`。

## 样式与资源

- 样式使用 SCSS，组件样式通常使用 `<style scoped>`。
- 全局变量在 `src/styles/_variables.scss`，由 Vite SCSS `additionalData` 注入。
- Element Plus 覆盖样式集中在 `src/styles/element-plus-reset.scss`。
- 新增样式前先检查 `src/styles/_variables.scss`、`src/styles/base.scss`、`src/styles/utilities.scss`
  和现有 CSS 变量，优先复用设计 token、工具类和已有覆盖规则。
- 页面、卡片、表格、筛选、弹窗、空状态、图表等 UI 需求优先检查 `src/components/UI`、
  `src/components/Business`、`src/components/Charts`；H5 需求优先检查 `src/views/H5/components`。
- 不重复创建通用 flex、ellipsis、card border、颜色、字号、间距等样式；确需新增公共样式时保持命名隔离，
  并说明影响范围。
- H5 px-to-rem 仅对 `src/views/H5` 下文件生效，配置在 `vite.config.ts`。
- `postcss-pxtorem` 会排除 `node_modules`、非 H5 目录、`.el-`、`.van-` 和 `html`。
- 不要随意新增全局样式；确需新增时说明影响范围并保持命名隔离。

## 格式化与 Lint

- Prettier 配置见 `.prettierrc`：行宽 100、2 空格、单引号、无分号、无尾随逗号。
- `.editorconfig` 要求 UTF-8、LF、去除行尾空格、文件末尾换行。
- ESLint 配置为 `eslint.config.js`，使用 ESLint 9 flat config。
- `@typescript-eslint/no-explicit-any` 关闭，但不要滥用 `any`。
- `@typescript-eslint/no-unused-vars` 与 `vue/no-unused-vars` 为 warn。
- `vue/multi-word-component-names` 关闭，允许单词组件名。
- `.vue`、`.ts`、`.tsx` 中 `no-undef` 关闭。
- 自动导入全局变量来自 `.eslintrc-auto-import.json`，该文件可能不存在。
- 格式化只处理本次实际修改文件或最小必要范围，避免引入无关 diff。

## 注释要求

- 新增函数/方法应补充基础注释，说明用途、关键参数、返回值或副作用。
- 对外导出的工具函数默认使用 JSDoc 风格注释。
- 复杂逻辑需要说明分支意图、边界条件和异常兜底。
- 数据转换、权限判断、图表拼装、H5/PC 分支等业务逻辑应适当提高注释密度。
- 避免无信息注释，注释应解释意图和约束，不要复述代码。

## 环境与构建配置

- 环境文件：`.env`、`.env.development`、`.env.test`、`.env.production`。
- 常用变量：`VITE_APP_TITLE`、`VITE_API_BASE_URL`、`VITE_DEBUG`、`VITE_APP_VERSION`。
- Vite `base` 当前为 `./`。
- 开发服务器监听 `0.0.0.0:5173`。
- 开发代理包含 `/api` 和 `^/api/review`，目标地址以 `vite.config.ts` 为准。
- `VITE_DEBUG=true` 时开启 sourcemap。
- 非生产环境会动态加载 `vconsole`。

## 文档维护

- 本文件是当前仓库面向 coding agents 的唯一根级协作说明。
- `README.md` 仅作参考，若 README 与实际配置冲突，以 `package.json`、`vite.config.ts`、源码结构和
  当前 `AGENTS.md` 为准。
- 修改脚本、目录结构、测试框架、构建配置、权限/路由机制后，应同步更新本文件。
- 修改组件注册、公共样式入口、复用规则或文件拆分约定后，应同步更新本文件。
- 不要重新引入 Claude 专属说明文件或本地 agent 配置目录，除非用户明确要求。
