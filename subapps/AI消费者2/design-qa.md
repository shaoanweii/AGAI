**Comparison target**

- Source visual truth: `/Users/Qiu./.codex/generated_images/019f5918-1932-7a40-ab11-e9eec6162fde/exec-3e00fb87-13f0-4d03-ac7a-1087d13644a5.png` (selected option 2).
- Implementation: `http://127.0.0.1:5178/`, 新建调研弹窗，desktop viewport inherited from the in-app browser.
- Intended state: 消费者模板与场景模板默认选中；右侧显示当前配置。

**Findings**

- No P0/P1/P2 issue found from the rendered DOM and interaction checks. The implementation presents a single long-form modal, a left configuration column, a right current-configuration panel, and fixed action buttons. The prior three-step workflow is absent.
- [P3] The implementation purposefully uses the system's existing template and scenario copy instead of mockup-only counts and dates. This keeps the page grounded in available application data.

**Required fidelity surfaces**

- Fonts and typography: uses the application's existing compact Chinese enterprise typography and heading hierarchy.
- Spacing and layout rhythm: the two-column grid, section spacing, tab strips, list rows, and footer follow the selected option's desktop density.
- Colors and visual tokens: uses existing `--accent`, `--line`, and muted text tokens; selected states use the existing blue treatment.
- Image quality and asset fidelity: the selected mockup has no required custom image assets; no decorative image replacement was needed.
- Copy and content: task name/description, consumer source, scenario source, current configuration, cancellation and create actions are rendered in Chinese using application data.

**Interaction checks**

- Opened “新建调研” on the local implementation.
- Verified the task name and description inputs, five consumer-template radios, and scenario-template radios are present.
- Verified “自定义目标群体” and “AI 自动圈选消费者群体” switch to their respective configuration states.
- Checked browser console errors: none.

**Open Questions**

- Browser screenshot capture timed out after the functional checks, so a same-frame visual bitmap comparison could not be retained. This is a verification artifact limitation, not a discovered UI failure.

**Implementation Checklist**

- [x] Replace the multi-step wizard with a single-page configuration layout.
- [x] Add task name and task description fields.
- [x] Add consumer template, custom target, and AI selection modes.
- [x] Add scenario template and custom-question modes.
- [x] Preserve existing template/scenario data and create-task behavior.

**Follow-up Polish**

- Re-run bitmap capture when the in-app browser screenshot channel is available, then perform a side-by-side visual comparison at the same viewport.

final result: blocked

---

## 校准优化（方案 2）实施验证

**Comparison target**

- Source visual truth: `/Users/Qiu./.codex/generated_images/019f5f25-7564-7282-8e50-81197facf6cb/exec-d8953e67-f384-4eaa-8067-acb4239be21f.png`（证据驱动校准方案）。
- Implementation: `http://127.0.0.1:5190/`，调研任务详情 → 校准优化页签。
- Intended state: 证据队列、校准指令、拟修改点、生成候选版本与版本差异预览。

**Findings**

- 未发现 P0/P1/P2 问题。校准页复用现有报告详情弹窗入口；相较设计图的全页任务工作台，弹窗承载是当前产品既有交互约束，不新增路由。
- 页面构建成功；`node --test test/list-utils.test.mjs` 8/8 通过；`git diff --check` 通过。
- Playwright 已验证：打开已完成任务报告 → 切换“校准优化” → 选择额外证据（引用数 2→3，指令自动追加）→ 生成待验证版本 → 保存草稿。
- 截图：`/Users/Qiu./Documents/Codex_projects/AI消费者2/.playwright-cli/page-2026-07-14T06-58-46-231Z.png`，1200 × 752。源图与实现并列对照：`/Users/Qiu./Documents/Codex_projects/AI消费者2/output/playwright/calibration-comparison.png`。
- 控制台仅有既存 `favicon.ico` 404；无应用运行时错误。

**Required fidelity surfaces**

- Fonts and typography: 复用报告详情的紧凑中文企业级层级；长文案在证据卡和指令框内可读且不溢出。
- Spacing and layout rhythm: 证据队列与指令区在桌面端形成稳定双栏，版本差异区位于下方，页面可滚动。
- Colors and visual tokens: 使用既有蓝色主操作、浅蓝选中态、绿色候选版本和红/绿预期影响语义色。
- Image quality and asset fidelity: 复用现有 logo 与 Lucide 标准图标；无新增装饰图片或占位资产。
- Copy and content: 汽车调研证据、校准指令、约束与 V1.2/V1.3 差异均在浏览器中可见。

**Implementation Checklist**

- [x] 新增报告详情的“校准优化”一级页签。
- [x] 实现证据选择、校准指令与约束编辑。
- [x] 实现候选版本生成、版本差异预览与草稿状态。
- [x] 完成同视口浏览器截图、点击和控制台验证。

**Follow-up Polish**

- [P3] 若后续将报告详情改为独立任务页，可扩展至设计图中的全宽四证据队列；当前弹窗高度下采用可滚动证据列表以保持既有产品导航模型。

final result: passed

---

## 分页器文案精简验收

- 已移除底部分页器中的“每页 10 条 · 当前第 2 / 5 页”说明，仅保留翻页按钮与页码。
- 浏览器实测分页器文本为“上一页12345下一页”，控制台无 warning/error；构建、8/8 单测与 `git diff --check` 均通过。

final result: passed

---

## 归因页密度与高度对齐验收

**Comparison target**

- Source visual truth: 用户标注的“模型归因分析”区域，要求与概览页同高，适当调整卡片高度和字体，并消除大面积留白。
- Implementation: `http://127.0.0.1:5180/`，消费调研 → 查看报告 → 归因路径。

**Findings**

- 归因仪表盘改为与报告内容区域等高的响应式网格，核心区、路径卡、证据区和行动区按可用高度分配，不再采用固定最小高度造成底部留白。
- 四张归因路径卡随所在区域拉伸；路径、证据、驱动与行动卡片的内边距和字号维持可读性，避免行动卡因填充高度出现大面积空白。
- 当前视口实测：归因页与概览页的面板高度均为 728px、仪表盘高度均为 708px，且归因页 `scrollHeight` 与 `clientHeight` 均为 728px，无额外溢出滚动。

**Verification**

- 浏览器实测：打开目标报告 → 归因路径 → 截图检查卡片比例与留白；切换概览后读取同一容器高度，再返回归因路径。
- 控制台：无 warning/error。
- `npm run build`：通过（仅有既有的大 bundle 警告）。
- `node --test test/list-utils.test.mjs`：8/8 通过。
- `git diff --check`：通过。

final result: passed

## 校准入口与归因路径修订验收

**Comparison target**

- Source visual truth: 本轮用户浏览器标注，要求将“校准优化”和“导出报告”改为蓝色背景边框；修复校准弹窗无法打开；重新设计难以理解的核心归因路径。
- Implementation: `http://127.0.0.1:5180/`，消费调研 → 查看报告 → 归因路径 / 校准优化。
- Evidence screenshots: `/Users/Qiu./Documents/Codex_projects/AI消费者2/.playwright-cli/page-2026-07-14T08-36-32-446Z.png`（归因路径底部行动区）与最新浏览器快照 `page-2026-07-14T08-37-01-900Z.yml`（校准弹窗）。

**Findings**

- 未发现 P0/P1/P2 问题。两个报告头部操作按钮均使用浅蓝背景、蓝色边框和明确的悬停/键盘焦点状态。
- 校准按钮以 `type="button"` 处理点击并阻止无关默认/冒泡行为；在重启到当前源码后，Playwright 真实点击可打开独立“校准优化”弹窗。
- 归因路径改为四段可解释链路：看到什么 → 为什么重要 → 会带来什么 → 需要同时处理；每段同时给出事实、判断与业务影响，并增加“如何使用这份归因”说明，避免把 β 系数直接当业务结论。
- 归因页的底部行动建议可滚动访问：实测滚动容器 `scrollHeight 670`、`clientHeight 582`，滚动到底后截图中三项行动建议和方法说明均完整可见。
- 浏览器控制台仅有 `favicon.ico` 404；无应用运行时错误。

**Verification**

- Playwright：打开报告 → 点击归因路径 → 验证解释链路与底部行动建议；点击校准优化 → 验证独立弹窗出现。
- `npm run build`：通过（仅有既有的大 bundle 警告）。
- `node --test test/list-utils.test.mjs`：8/8 通过。
- `git diff --check`：通过。

final result: passed

---

## 校准优化注释修订验收

**Comparison target**

- Source visual truth: 本轮用户在浏览器中的 6 条标注，覆盖入口位置、独立弹窗、真实反馈上传、当前任务消费者、影响范围说明，以及移除版本差异预览与“查看更多证据”。
- Implementation: `http://127.0.0.1:5180/`，消费调研 → 查看报告 → 校准优化。
- Evidence screenshot: `/Users/Qiu./Documents/Codex_projects/AI消费者2/.playwright-cli/page-2026-07-14T08-23-52-191Z.png`。

**Findings**

- 未发现 P0/P1/P2 问题。报告头部的“校准优化”位于“导出报告”左侧，并打开独立叠层弹窗；报告原有页签不再包含校准页签。
- 空数据时仅显示上传引导；Playwright 上传 `calibration-feedback.json` 后，页面读取并展示 2 条文件内真实反馈，含来源、问题与样本画像；无预置证据卡，也没有“查看更多证据”。
- “当前任务消费者”只读展示任务人群与样本量；“校准依据与影响范围”明确列出依据数据、会重新计算的报告洞察/归因/问答口径，以及不会改动的用户数据、标签体系、消费者模板和其他任务。
- 已移除“拟修改点”和“版本差异预览”。引用一条上传反馈、填写期望调整与约束后，“生成并验证校准版本”可用，点击后出现待验证成功状态。
- 控制台仅有 `favicon.ico` 404；无应用运行时错误。

**Verification**

- Playwright：打开已完成任务报告 → 点击校准优化 → 上传 JSON → 验证两条真实反馈 → 引用一条 → 填写期望调整与约束 → 生成待验证版本。
- `npm run build`：通过（仅有既有的大 bundle 警告）。
- `node --test test/list-utils.test.mjs`：8/8 通过。
- `git diff --check`：通过。

**Open scope**

- 当前版本在浏览器内读取 JSON / 简单 CSV 并保留本次会话状态；尚未接入后端真实数据源或版本持久化 API。

final result: passed

---

## 归因路径与校准表单收敛验收

**Comparison target**

- Source visual truth: 本轮用户在浏览器中的 9 条标注，覆盖归因模块命名、解释路径卡片、字体可读性、校准弹窗顶部组件、上传按钮对齐及约束条件必填状态。
- Implementation: `http://127.0.0.1:5180/`，消费调研 → 查看报告 → 归因路径 / 校准优化。
- Evidence screenshot: `/Users/Qiu./Documents/Codex_projects/AI消费者2/.playwright-cli/page-2026-07-14T09-00-25-104Z.png`（归因路径）；校准空状态已在浏览器中视觉核验。

**Findings**

- 归因页已更名为“模型归因分析”和“模型归因路径”；移除了阅读引导副标题及“如何使用这份归因”说明。
- 四张路径卡片扩展为等宽、更高的流程卡，分别增加观察、感知、趋势和风险图标；辅助正文最小字号已提高到 10px，并保留各层级的视觉区分。
- 校准弹窗已移除顶部“上传真实反馈数据”横条与其重复上传入口；空状态仅保留证据区内居中的“上传反馈数据”按钮。
- “约束条件”已标为“（可选）”，生成按钮的可用条件只依赖已引用证据和“期望调整”，不再要求填写约束条件。
- 当前浏览器控制台无 warning/error。

**Verification**

- 浏览器实测：打开报告 → 归因路径，确认新名称、四张图标卡和已移除文本均真实渲染；归因面板 `scrollHeight` 与 `clientHeight` 均为 728，当前视口内容完整可见。
- 浏览器实测：点击“校准优化”，确认独立弹窗正常打开、顶部重复上传组件不存在、上传按钮文字居中、约束条件显示“（可选）”。
- `npm run build`：通过（仅有既有的大 bundle 警告）。
- `node --test test/list-utils.test.mjs`：8/8 通过。
- `git diff --check`：通过。

final result: passed

---

## 归因路径卡与证据区压缩验收

**Findings**

- 四张归因路径卡压缩为 107px 高度，文本与图标保持垂直对齐，不再出现内容偏少但卡片过高的情况。
- 关键驱动区重排为“正向驱动 / 负向阻碍”左右两列，各保留三条指标；证据三角验证区增大单卡内边距和正文行高，四类证据可快速横向比对。
- 报告内容区无额外溢出滚动，整体高度仍与概览页对齐。

**Verification**

- 浏览器实测：打开目标报告 → 归因路径 → 检查路径卡、双列驱动与证据区的渲染结果。
- 实测数值：路径卡高度均为 107px；关键驱动区高度 170px；证据区高度 174px；报告面板高度 728px，溢出为 0。
- 控制台无 warning/error。
- `npm run build`、`node --test test/list-utils.test.mjs`（8/8）及 `git diff --check` 均通过。

final result: passed

---

## 调研结果页签命名验收

- 报告详情第二个页签已由“消费者画像”更名为“调研结果”，页签标识与既有内容逻辑保持不变。
- 浏览器实测：打开目标报告 → 点击“调研结果”，激活页签文本为“调研结果”，报告页签中不存在“消费者画像”。
- 控制台无 warning/error；`npm run build`、`node --test test/list-utils.test.mjs`（8/8）和 `git diff --check` 均通过。

final result: passed

---

## 调研数据统计与样本分页验收

- 年龄结构、城市级别分布及关键调研行为统计均改为从当前 50 条调研作答记录汇总；页面实测年龄为 20% / 40% / 20% / 20%，城市级别为 30% / 30% / 20% / 20%。
- 样本表头只保留“抽样用户与调研作答”；每页条数与当前页码已移动至下方分页器。
- 浏览器实测：从第 1 页切换至第 2 页，分页器更新为“当前第 2 / 5 页”，首条记录由 U-10431 更新为 U-10441。
- 控制台无 warning/error；`npm run build`、`node --test test/list-utils.test.mjs`（8/8）和 `git diff --check` 均通过。

final result: passed
