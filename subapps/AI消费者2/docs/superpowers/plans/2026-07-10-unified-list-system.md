# 统一列表系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将全系统数据管理页面统一为透明标题栏、标题栏内筛选和操作、内部滚动列表及共享标签数据模型。

**Architecture:** 在 `src/main.jsx` 中抽取共享的标题栏、分页和标签分组展示逻辑，保留现有单文件 React 结构。`taxonomyRows` 作为标签体系唯一来源，用户数据通过标签名称回查一级分类和三级内容；`src/styles.css` 新增统一工作台与宽体编辑器样式。

**Tech Stack:** React 19、Vite、Lucide、原生 CSS。

## Global Constraints

- 所有菜单标题仅显示菜单名称，不显示解释文案。
- 筛选控件在标题栏左侧，操作按钮在标题栏右侧。
- 用户和标签列表默认每页 20 条，数据区内部滚动。
- 用户标签必须引用 `taxonomyRows` 中定义的一级、二级、三级结构。
- 启用标签不可删除。
- 不新增生产依赖。

---

### Task 1: 建立共享标题栏、分页和标签分组数据辅助函数

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`

**Interfaces:**
- Produces: `PageHeader({ title, filters, actions })`、`Pagination({ total, page, pageSize, onPageChange })`、`getTaxonomySelections(tags, rows)`。
- Consumes: `taxonomyRows`、现有 `Search`、`Filter`、`Plus` 图标。

- [ ] **Step 1: 写入共享数据映射的纯函数测试用例说明**

```js
const grouped = getTaxonomySelections(['25-35岁'], taxonomyRows);
expect(grouped['个人属性']).toEqual(['年龄：25-35岁']);
```

- [ ] **Step 2: 验证当前实现不具备一级分类字段映射**

Run: `rg -n "getTaxonomySelections|PageHeader|Pagination" src/main.jsx`

Expected: no matching helper definitions.

- [ ] **Step 3: 实现共享函数和透明标题栏样式**

```jsx
function PageHeader({ title, filters, actions }) {
  return <div className="page-header"><h2>{title}</h2><div className="header-tools">{filters}{actions}</div></div>;
}

function getTaxonomySelections(tags, rows) {
  return rows.reduce((groups, row) => {
    const selected = row.values.filter((value) => tags.includes(value));
    if (selected.length) groups[row.category] = [...(groups[row.category] || []), `${row.name}：${selected.join('、')}`];
    return groups;
  }, {});
}
```

- [ ] **Step 4: 运行构建**

Run: `npm run build`

Expected: exit code 0.

### Task 2: 重构标签体系列表、编辑弹窗与删除约束

**Files:**
- Modify: `src/main.jsx:968-1064`
- Modify: `src/main.jsx`（新增 `TagEditorModal`）
- Modify: `src/styles.css:1-37`

**Interfaces:**
- Consumes: `PageHeader`、`Pagination`、`taxonomyRows`。
- Produces: 状态可编辑的标签行、20 条分页、内部滚动表格、完整编辑弹窗。

- [ ] **Step 1: 定义交互验证用例**

```js
// 点击“编辑”后应显示标签名称、标签定义、同义词、标签内容、标签状态。
// 标签状态为“启用”时，删除按钮必须具备 disabled 属性。
```

- [ ] **Step 2: 替换标签页标题栏和分页切片**

```js
const pageSize = 20;
const pageRows = filteredRows.slice((page - 1) * pageSize, page * pageSize);
```

- [ ] **Step 3: 实现标签编辑器和禁用删除**

```jsx
<button className="text-button danger" disabled={item.status === '启用'} title={item.status === '启用' ? '请先停用标签' : '删除标签'}>删除</button>
```

- [ ] **Step 4: 构建并通过浏览器验证编辑、筛选、分页与禁用删除**

Run: `npm run build`

Expected: exit code 0; 浏览器中启用行删除不可点击。

### Task 3: 重构用户数据字段、宽体编辑弹窗和 20 条分页

**Files:**
- Modify: `src/main.jsx:1066-1169`
- Modify: `src/main.jsx:2080-2150`
- Modify: `src/styles.css:2125-2182`

**Interfaces:**
- Consumes: `getTaxonomySelections`、`taxonomyRows`、`PageHeader`、`Pagination`。
- Produces: 按一级分类列展示用户标签、按一级分类选择三级内容的宽体弹窗。

- [ ] **Step 1: 定义用户保存与分组显示验收用例**

```js
// 编辑 U-10001，勾选“个人属性/年龄/25-35岁”，保存后“个人属性”列展示“年龄：25-35岁”。
// 点击“确定”后更新行数据并关闭弹窗。
```

- [ ] **Step 2: 更新用户列表字段和分页**

```jsx
{taxonomyCategories.map((category) => <span key={category}>{groupedTags[category]?.join('；') || '-'}</span>)}
```

- [ ] **Step 3: 更新 `UserBuilder` 标签选择器与主按钮名称**

```jsx
{taxonomyRows.map((row) => <section className="taxonomy-selector" key={row.id}>...</section>)}
<button className="primary" onClick={save}>确定</button>
```

- [ ] **Step 4: 构建并验证新增、编辑、筛选和分页**

Run: `npm run build`

Expected: exit code 0; 弹窗为宽体、无“用户数据”辅助标题。

### Task 4: 将消费者模板、调研场景和任务列表对齐工作台规范

**Files:**
- Modify: `src/main.jsx:881-966`
- Modify: `src/main.jsx:1171-1337`
- Modify: `src/styles.css:379-420`
- Modify: `src/styles.css:2184-2239`

**Interfaces:**
- Consumes: `PageHeader`、既有模板/场景/任务数据。
- Produces: 标题栏内筛选与操作、任务内部滚动、15 个模板、场景列表行。

- [ ] **Step 1: 添加模板基准数据至 15 条**

```js
const consumerTemplateSeed = [...existingTemplates, ...additionalTemplates].slice(0, 15);
```

- [ ] **Step 2: 替换独立筛选栏为标题栏工具组**

```jsx
<PageHeader title="调研场景" filters={<ScenarioFilters />} actions={<button className="primary">新建场景模板</button>} />
```

- [ ] **Step 3: 将调研场景改为表头加数据行结构，任务表加入滚动数据区**

```css
.list-body-scroll { max-height: calc(100vh - 300px); overflow: auto; }
.list-table-head { position: sticky; top: 0; z-index: 1; }
```

- [ ] **Step 4: 构建并在浏览器验证模板数量、场景行和任务内部滚动**

Run: `npm run build`

Expected: exit code 0; 消费者模板显示 15 条。

### Task 5: 全局文案清理、视觉一致性和交互回归

**Files:**
- Modify: `src/main.jsx`
- Modify: `src/styles.css`

**Interfaces:**
- Consumes: 所有前序任务的共享组件和样式。
- Produces: 无标题说明文案的统一页面框架。

- [ ] **Step 1: 搜索并移除页面标题下的说明段落**

Run: `rg -n "<PageIntro|description=" src/main.jsx`

Expected: 每个调用迁移到 `PageHeader`，不再渲染说明文案。

- [ ] **Step 2: 统一透明标题栏、状态与标签颜色**

```css
.page-header { background: transparent; border: 0; padding: 0 0 14px; }
.page-header h2 { margin: 0; }
```

- [ ] **Step 3: 运行生产构建**

Run: `npm run build`

Expected: exit code 0.

- [ ] **Step 4: 浏览器回归验证**

Run: 使用 Browser 插件依次验证用户数据、标签体系、消费者库、调研场景、任务列表。

Expected: 页面非空、控制台无相关错误、筛选/分页/编辑/禁用删除均可用。
