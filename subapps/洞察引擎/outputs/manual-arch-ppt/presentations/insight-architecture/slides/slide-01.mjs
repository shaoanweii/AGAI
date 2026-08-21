const C = {
  blue: "#0d6fe8",
  blue2: "#075bd3",
  navy: "#082b6f",
  text: "#083a86",
  border: "#b8d4fb",
  panel: "#f7fbff",
  pale: "#eef6ff",
  teal: "#009d95",
  purple: "#5c55d6",
};

function rect(ctx, slide, x, y, w, h, fill = "#ffffff", line = C.border, lw = 1.2) {
  return ctx.addShape(slide, {
    x, y, width: w, height: h,
    fill,
    line: ctx.line(line, lw),
  });
}

function text(ctx, slide, t, x, y, w, h, opts = {}) {
  return ctx.addText(slide, {
    x, y, width: w, height: h, text: t,
    fontSize: opts.size ?? 18,
    bold: opts.bold ?? false,
    color: opts.color ?? C.text,
    align: opts.align ?? "center",
    valign: opts.valign ?? "middle",
    typeface: "Hiragino Sans GB",
    fill: opts.fill ?? "#00000000",
    line: ctx.line(opts.line ?? "#00000000", opts.lineWidth ?? 0),
    insets: opts.insets ?? { left: 4, right: 4, top: 2, bottom: 2 },
  });
}

function chip(ctx, slide, t, x, y, w, h, opts = {}) {
  rect(ctx, slide, x, y, w, h, opts.fill ?? "#f8fbff", opts.line ?? "#d5e6fb", 1);
  text(ctx, slide, t, x, y, w, h, { size: opts.size ?? 13, bold: true, color: C.text });
}

async function layerCard(ctx, slide, x, y, h, icon, title, sub) {
  rect(ctx, slide, x, y, 145, h, "#ffffff", "#9bc5ff", 1.5);
  await ctx.addLucideIcon(slide, { icon, x: x + 50, y: y + 20, width: 45, height: 45, color: C.blue2, strokeWidth: 2.2 });
  text(ctx, slide, title, x + 8, y + h / 2 - 8, 129, 34, { size: 23, bold: true });
  text(ctx, slide, sub, x + 20, y + h / 2 + 30, 105, 24, { size: 14, bold: true });
}

async function topPanel(ctx, slide, x, y, w, title, icon, items) {
  rect(ctx, slide, x, y, w, 102, "#ffffff", "#b6d5fb", 1.2);
  rect(ctx, slide, x, y, w, 36, C.blue, C.blue, 0);
  await ctx.addLucideIcon(slide, { icon, x: x + w / 2 - 70, y: y + 8, width: 23, height: 23, color: "#ffffff", strokeWidth: 2.2 });
  text(ctx, slide, title, x, y + 4, w, 30, { size: 22, bold: true, color: "#ffffff" });
  const gap = 7;
  const cw = (w - 24 - gap * (items.length - 1)) / items.length;
  items.forEach((it, i) => chip(ctx, slide, it, x + 12 + i * (cw + gap), y + 56, cw, 28, { size: 12 }));
}

async function moduleCard(ctx, slide, x, y, w, title, icon, items) {
  rect(ctx, slide, x, y, w, 170, "#ffffff", "#b6d5fb", 1.2);
  rect(ctx, slide, x, y, w, 42, C.blue, C.blue, 0);
  await ctx.addLucideIcon(slide, { icon, x: x + 13, y: y + 8, width: 26, height: 26, color: "#ffffff", strokeWidth: 2.4 });
  text(ctx, slide, title, x + 35, y + 4, w - 40, 34, { size: 19, bold: true, color: "#ffffff" });
  items.forEach((it, i) => chip(ctx, slide, it, x + 18, y + 55 + i * 27, w - 36, 22, { size: 12.5 }));
}

async function knowledgeCard(ctx, slide, x, y, w, title, icon, color, items) {
  rect(ctx, slide, x, y, w, 104, "#ffffff", "#b6d5fb", 1.2);
  rect(ctx, slide, x, y, w, 38, color, color, 0);
  await ctx.addLucideIcon(slide, { icon, x: x + w / 2 - 88, y: y + 8, width: 23, height: 23, color: "#ffffff", strokeWidth: 2.2 });
  text(ctx, slide, title, x, y + 3, w, 32, { size: 22, bold: true, color: "#ffffff" });
  const cols = Math.min(4, items.length);
  const cw = (w - 28 - 8 * (cols - 1)) / cols;
  items.forEach((it, i) => {
    const row = Math.floor(i / cols);
    const col = i % cols;
    chip(ctx, slide, it, x + 14 + col * (cw + 8), y + 54 + row * 27, cw, 22, { size: 12 });
  });
}

function arrowText(ctx, slide, x, y, w, h, label, dir = "right", size = 30) {
  const glyph = dir === "left" ? "←" : dir === "up" ? "↑" : "→";
  text(ctx, slide, glyph, x, y, w, h, { size, bold: true, color: C.blue2 });
  if (label) text(ctx, slide, label, x - 35, y + h - 2, w + 70, 20, { size: 13, bold: true });
}

async function rightScenario(ctx, slide, x, y, title, icon, items) {
  rect(ctx, slide, x, y, 160, 120, "#ffffff", "#c4dbfa", 1.2);
  await ctx.addLucideIcon(slide, { icon, x: x + 18, y: y + 20, width: 30, height: 30, color: C.blue2, strokeWidth: 2.1 });
  text(ctx, slide, title, x + 52, y + 18, 95, 28, { size: 20, bold: true });
  items.forEach((it, i) => chip(ctx, slide, it, x + 34, y + 55 + i * 23, 98, 19, { size: 11.5 }));
}

export async function slide01(presentation, ctx) {
  const slide = presentation.slides.add();
  rect(ctx, slide, 0, 0, 1600, 900, "#f8fbff", "#00000000", 0);

  text(ctx, slide, "洞察引擎产品化架构图", 0, 16, 1600, 54, { size: 42, bold: true, color: C.navy });
  text(ctx, slide, "系统运营  ·  数据接入  ·  流程编排  ·  任务执行  ·  智能处理  ·  资产沉淀  ·  业务应用闭环", 0, 74, 1600, 26, { size: 18, bold: true });

  await layerCard(ctx, slide, 16, 116, 172, "Monitor", "系统运营层", "运营管控");
  await layerCard(ctx, slide, 16, 312, 240, "Settings", "处理生产层", "生产执行");
  await layerCard(ctx, slide, 16, 578, 150, "BookOpenCheck", "知识规则层", "知识规则");
  await layerCard(ctx, slide, 16, 758, 124, "Server", "平台支撑层", "基础底座");

  const mainX = 190, mainW = 1160;
  rect(ctx, slide, mainX, 112, mainW, 175, "#f6fbff", "#b6d5fb", 1.2);
  await topPanel(ctx, slide, 205, 130, 330, "主控台", "Monitor", ["全局概览", "任务态势", "质量看板", "告警概览"]);
  await topPanel(ctx, slide, 555, 130, 390, "监控中心", "Activity", ["数据质量\n监控", "处理链路\n监控", "标签质量\n监控", "模型质量\n监控", "后处理质量\n监控"]);
  await topPanel(ctx, slide, 965, 130, 350, "审核反馈", "UserCheck", ["纠错审核", "AI低置信审核", "新词处理", "未归一观点"]);
  rect(ctx, slide, 495, 255, 470, 30, "#eef6ff", "#c8dcf6", 1);
  text(ctx, slide, "可视化运营   ·   质量监控   ·   业务需求审核", 495, 256, 470, 26, { size: 17, bold: true });

  rect(ctx, slide, mainX, 315, mainW, 250, "#f6fbff", "#b6d5fb", 1.2);
  const mods = [
    ["数据接入", "Database", ["数据源管理", "字段映射", "接入任务", "接入记录"]],
    ["流程配置", "Workflow", ["节点编排", "前置规则", "模型节点", "后置规则"]],
    ["执行任务", "PlayCircle", ["任务管理", "调度执行", "处理记录", "异常重试"]],
    ["数据清洗", "Brush", ["去重", "格式标准化", "字段校验", "噪声过滤"]],
    ["AI识别", "BrainCircuit", ["文本识别", "标签识别", "观点抽取", "情感识别"]],
    ["数据后处理", "SlidersHorizontal", ["结果合并", "置信度校准", "规则修正", "格式转换"]],
    ["数据资产", "DatabaseZap", ["原始留存", "结果数据", "异常数据", "反馈样本", "下载文件"]],
  ];
  const mx = [205, 355, 505, 655, 805, 955, 1105];
  for (let i = 0; i < mods.length; i++) {
    await moduleCard(ctx, slide, mx[i], 330, 130, mods[i][0], mods[i][1], mods[i][2]);
    if (i < mods.length - 1) arrowText(ctx, slide, mx[i] + 125, 392, 35, 30, "", "right", 32);
  }
  rect(ctx, slide, 205, 515, 1030, 32, "#eef6ff", "#c8dcf6", 1);
  const lanes = ["接入暂存", "流程编排", "任务调度", "标准化处理", "智能识别", "后处理修正", "资产沉淀"];
  lanes.forEach((l, i) => {
    text(ctx, slide, l, 215 + i * 145, 520, 105, 22, { size: 15, bold: true });
    if (i < lanes.length - 1) text(ctx, slide, "→", 320 + i * 145, 518, 35, 24, { size: 24, bold: true, color: C.blue2 });
  });

  rect(ctx, slide, mainX, 600, mainW, 160, "#f6fbff", "#b6d5fb", 1.2);
  await knowledgeCard(ctx, slide, 205, 625, 360, "知识中心", "BookOpen", C.teal, ["标签体系", "标准观点", "关键词库", "语料映射", "业务场景", "用车场景", "品牌车系"]);
  await knowledgeCard(ctx, slide, 620, 625, 330, "规则中心", "ShieldCheck", C.purple, ["数据规则", "业务规则", "闭环规则", "监控规则"]);
  await knowledgeCard(ctx, slide, 1010, 625, 330, "规则测试", "FlaskConical", C.blue, ["样本测试", "命中验证", "影响评估", "发布校验"]);
  arrowText(ctx, slide, 360, 548, 42, 34, "AI能力支撑", "up", 32);
  arrowText(ctx, slide, 720, 548, 42, 34, "数据处理支撑", "up", 32);
  arrowText(ctx, slide, 955, 680, 46, 24, "可行性验证", "left", 26);

  rect(ctx, slide, mainX, 780, 1160, 100, "#f6fbff", "#b6d5fb", 1.2);
  const platform1 = ["客户管理", "项目管理", "模型配置", "下载管理", "账号管理", "角色管理", "权限管理", "操作审计"];
  const platformIcons = ["Users", "Folder", "Box", "Download", "UserRound", "UsersRound", "Shield", "ClipboardList"];
  for (let i = 0; i < platform1.length; i++) {
    const x = 230 + i * 135;
    arrowText(ctx, slide, x + 30, 748, 24, 22, "平台支撑", "up", 24);
    await ctx.addLucideIcon(slide, { icon: platformIcons[i], x, y: 805, width: 27, height: 27, color: C.blue2, strokeWidth: 2.1 });
    text(ctx, slide, platform1[i], x + 30, 804, 90, 28, { size: 16, bold: true });
  }
  const chips = ["多租户", "项目隔离", "权限控制", "模型治理", "审计追踪", "数据血缘"];
  chips.forEach((c, i) => chip(ctx, slide, c, 220 + i * 170, 846, 135, 25, { size: 13 }));

  rect(ctx, slide, 1380, 116, 200, 766, "#f6fbff", "#b6d5fb", 1.2);
  rect(ctx, slide, 1380, 116, 200, 46, C.blue, C.blue, 0);
  text(ctx, slide, "业务运营层", 1380, 121, 200, 35, { size: 24, bold: true, color: "#ffffff" });
  await rightScenario(ctx, slide, 1400, 180, "场景看板", "BarChart3", ["经营洞察", "质量趋势", "舆情专题"]);
  await rightScenario(ctx, slide, 1400, 350, "智能问数", "MessageSquareText", ["自然语言查询", "指标问答", "归因解释"]);
  await rightScenario(ctx, slide, 1400, 520, "智能报告", "FileText", ["自动摘要", "专题分析", "周期报告"]);
  await rightScenario(ctx, slide, 1400, 690, "工单闭环", "ClipboardCheck", ["问题流转", "责任跟踪", "处置复盘"]);
  text(ctx, slide, "←", 1290, 165, 80, 35, { size: 38, bold: true, color: C.blue2 });
  text(ctx, slide, "业务反哺", 1288, 198, 90, 45, { size: 16, bold: true });
  text(ctx, slide, "→", 1295, 420, 70, 42, { size: 44, bold: true, color: C.blue2 });
  text(ctx, slide, "资产服务", 1280, 384, 95, 28, { size: 16, bold: true });

  return slide;
}
