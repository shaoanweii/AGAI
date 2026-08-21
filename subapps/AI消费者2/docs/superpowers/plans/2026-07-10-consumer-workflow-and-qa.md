# 消费者工作流与问答体验 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让标签、消费者模板、调研创建和模拟问答形成一致且可操作的产品链路。

**Architecture:** 继续使用当前 React 单文件页面组合与本地 state；不新增服务端或依赖。消费者模板的表单和详情各自成为可复用的 modal，调研创建共享模板数据，问答状态在组件内维护多轮消息和侧栏尺寸。

**Tech Stack:** React 19、Vite、lucide-react、CSS。

## Global Constraints

- 不新增生产依赖，不修改发布配置。
- 保持现有中文产品术语和静态演示数据。
- 所有新增交互均应在本地状态中真实更新。

---

### Task 1: 标签与用户编辑器

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`
- Test: `test/list-utils.test.mjs`

- [ ] 删除标签内容编辑器里的三级标签新增输入和添加动作，只编辑既有内容项。
- [ ] 将用户编辑器改成一级分类分页；当前分类内的全部二级标签按纵向分组展示并可勾选；移除上一步、下一步和二级 tab 切换。
- [ ] 运行 `node --test test/list-utils.test.mjs` 与 `npm run build`。

### Task 2: 消费者模板资产

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`

- [ ] 模板卡片最多显示三个标签，剩余标签以 `+N` 表示，并补齐“查看”“编辑”入口。
- [ ] 查看页展示完整画像、标签层级、来源、样本量、命中用户和适配场景。
- [ ] 新建与编辑复用同一完整表单，显示名称、来源、按标签层级勾选项、样本数量、命中用户与说明。
- [ ] 运行 `npm run build`，在浏览器中验证卡片、查看、编辑和保存。

### Task 3: 三步调研创建

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`

- [ ] 将步骤改为“选择消费者（多选）→ 选择调研场景（单选）→ 调研任务设置”。
- [ ] 任务设置页提供名称、输入补充、样本量与置信度；提交后直接创建调研任务。
- [ ] 运行 `npm run build`，在浏览器完成一次三步创建并检查任务列表新增记录。

### Task 4: 多轮 AI 消费者问答

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`

- [ ] 结果页使用会话式消息流，展示消费者头像、分析步骤、最终结论，并允许继续追问。
- [ ] 默认收起归因侧栏；顶部透明工具栏提供展开/收起按钮；侧栏支持拖拽宽度。
- [ ] 将归因展示扩展为驱动、阻力、证据、冲突与建议五个层级。
- [ ] 运行 `npm run build`，在浏览器验证流式状态、多轮追问、侧栏开关和宽度拖拽。

### Task 5: 回归检查

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`

- [ ] 在桌面与移动视口检查标签、消费者、创建调研和问答四个核心路径。
- [ ] 检查浏览器控制台错误、白屏、模态层溢出与窄屏布局。
- [ ] 运行 `node --test test/list-utils.test.mjs` 和 `npm run build`。
