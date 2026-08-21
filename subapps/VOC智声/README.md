# VOC

基于 Vue 3 + TypeScript + Element Plus 构建的现代化VOC数据分析系统。

## 🚀 技术栈

- **Vue 3.5.13** + TypeScript 5.8.3 + Vite 6.3.5
- **Element Plus 2.10.2** + Sass 1.89.0
- **ECharts 5.6.0** + Vue Router 4.5.1 + Pinia 3.0.2

## 📁 项目结构

```
.
├── README.md
├── count_lines.sh
├── eslint.config.js
├── index.html
├── package-lock.json
├── package.json
├── public
│   └── vite.svg
├── src
│   ├── App.vue
│   ├── api
│   │   ├── common
│   │   │   ├── index.d.ts
│   │   │   └── index.ts
│   │   ├── dictionary
│   │   │   ├── index.d.ts
│   │   │   └── index.ts
│   │   ├── drillDownDialog
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── groupAnalysis
│   │   │   ├── README.md
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── http
│   │   │   └── index.ts
│   │   ├── journeyAnalysis
│   │   │   ├── README.md
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── main
│   │   │   ├── index.ts
│   │   │   └── type.d.ts
│   │   ├── overview
│   │   │   ├── index.ts
│   │   │   ├── leader.ts
│   │   │   └── type.d.ts
│   │   ├── productAnalysis
│   │   │   ├── README.md
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── reportManagement
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── reportSummary
│   │   │   └── index.ts
│   │   ├── role
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── sceneAnalysis
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── serviceAnalysis
│   │   │   ├── README.md
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   ├── system
│   │   │   ├── configuration
│   │   │   └── scene
│   │   ├── thisProductAnalysis
│   │   │   ├── README.md
│   │   │   ├── index.ts
│   │   │   └── types.d.ts
│   │   └── user
│   │       ├── index.ts
│   │       └── types.d.ts
│   ├── assets
│   │   ├── h5
│   │   │   ├── arrow-down-s-line.png
│   │   │   ├── arrow-right-s-line.png
│   │   │   ├── arrow-right.png
│   │   │   ├── flag.png
│   │   │   ├── notice.png
│   │   │   ├── report
│   │   │   └── group-car.png
│   │   ├── images
│   │   │   ├── arrow-right-line.png
│   │   │   ├── arrow-right-s-line.png
│   │   │   ├── arrow-up-right.png
│   │   │   ├── avatar.png
│   │   │   ├── bq1.png
│   │   │   ├── cayl.png
│   │   │   ├── close.png
│   │   │   ├── crowd.png
│   │   │   ├── drilldown
│   │   │   ├── female.png
│   │   │   ├── group-car.png
│   │   │   ├── hot-s.png
│   │   │   ├── iqcc.png
│   │   │   ├── iqcc_bg.png
│   │   │   ├── login_bg.png
│   │   │   ├── logo.png
│   │   │   ├── mail.png
│   │   │   ├── male.png
│   │   │   ├── nodata.png
│   │   │   ├── phone-call.png
│   │   │   ├── phone.png
│   │   │   ├── question-mark.png
│   │   │   ├── search.png
│   │   │   ├── system
│   │   │   ├── time-fill.png
│   │   │   ├── top-gd1.png
│   │   │   ├── top-gd2.png
│   │   │   ├── top-gd3.png
│   │   │   ├── top-gd4.png
│   │   │   ├── voiceprint-fill.png
│   │   │   ├── yhlc.png
│   │   │   └── zhfx.png
│   │   └── svg
│   │       ├── ai-head.svg
│   │       ├── arrow-up-down-fill.svg
│   │       ├── bell.svg
│   │       ├── bgjd.svg
│   │       ├── calendar.svg
│   │       ├── car_group.svg
│   │       ├── chevron-down.svg
│   │       ├── close.svg
│   │       ├── crowd.svg
│   │       ├── doc_big.svg
│   │       ├── document.svg
│   │       ├── drilldown
│   │       ├── eye.svg
│   │       ├── filter-lines.svg
│   │       ├── folder-download.svg
│   │       ├── gdpj_title.svg
│   │       ├── h5
│   │       ├── heart-circle.svg
│   │       ├── hot_rank.svg
│   │       ├── ld.svg
│   │       ├── left_d.svg
│   │       ├── log-out-01.svg
│   │       ├── menu
│   │       ├── meteor-fill.svg
│   │       ├── notification-message.svg
│   │       ├── notification-text.svg
│   │       ├── nsr
│   │       ├── o_rank.svg
│   │       ├── question-mark.svg
│   │       ├── rd.svg
│   │       ├── recording-01.svg
│   │       ├── reverse-left.svg
│   │       ├── send-plane-line.svg
│   │       ├── switch-horizontal.svg
│   │       ├── thumb-down-fill.svg
│   │       ├── thumb-up-fill.svg
│   │       ├── thumbs-down.svg
│   │       ├── thumbs-up.svg
│   │       ├── union.svg
│   │       ├── users02.svg
│   │       ├── voiceprint-fill.svg
│   │       ├── yhlc
│   │       ├── yhlvbg.svg
│   │       ├── zxfx
│   │       └── zxfx_bg.svg
│   ├── components
│   │   ├── Business
│   │   │   ├── AdvancedFilter
│   │   │   ├── DrillDownDialog
│   │   │   ├── FilterValueInput
│   │   │   ├── Scene
│   │   │   ├── VoiceDetailsDialog
│   │   │   └── VoiceListPanel
│   │   ├── Charts
│   │   │   ├── BarAndPointChart
│   │   │   ├── BarOrLineChart
│   │   │   ├── FEcharts
│   │   │   ├── FLineChart
│   │   │   ├── FMapChart
│   │   │   ├── FPieChart
│   │   │   ├── FWordCloud
│   │   │   ├── SmallLineTrendChart
│   │   │   └── index.ts
│   │   ├── DataSourceAnalysis
│   │   │   ├── WordCloudChart.vue
│   │   │   ├── index.vue
│   │   │   └── types.d.ts
│   │   ├── ShowCompare
│   │   │   ├── index.vue
│   │   │   └── types.d.ts
│   │   ├── Tooltip
│   │   │   └── DataTooltip.vue
│   │   ├── UI
│   │   │   ├── ButtonGroup
│   │   │   ├── FAnalyseWrap
│   │   │   ├── FCard
│   │   │   ├── FDatePicker
│   │   │   ├── FDdbreadcrumb
│   │   │   ├── FEmpty
│   │   │   ├── FSelect
│   │   │   ├── FTable
│   │   │   ├── SortNum
│   │   │   ├── SvgIcon
│   │   │   └── SwitchButton
│   │   ├── VocTrendChart
│   │   │   ├── index.vue
│   │   │   └── types.d.ts
│   │   ├── global.ts
│   │   └── index.ts
│   ├── constants
│   │   ├── china.json
│   │   ├── env.ts
│   │   ├── index.ts
│   │   └── nsrDataURI.ts
│   ├── hooks
│   │   ├── useBrowseRecord.ts
│   │   ├── useLoading.ts
│   │   └── useQueryListener.ts
│   ├── layout
│   │   ├── components
│   │   │   ├── ExpandMenu.vue
│   │   │   ├── Header.vue
│   │   │   ├── Menu.vue
│   │   │   └── Sidebar.vue
│   │   └── index.vue
│   ├── main.ts
│   ├── mock
│   │   └── index.ts
│   ├── permission.ts
│   ├── router
│   │   ├── constantRoutes.ts
│   │   ├── dynamicRoutes.ts
│   │   └── index.ts
│   ├── store
│   │   ├── index.ts
│   │   └── modules
│   │       ├── app.ts
│   │       ├── generalDrillDown.ts
│   │       ├── generalScenario.ts
│   │       ├── query.ts
│   │       ├── sceneAnalysis.ts
│   │       └── user.ts
│   ├── styles
│   │   ├── _variables.scss
│   │   ├── base.scss
│   │   ├── element-plus-reset.scss
│   │   ├── generate.scss
│   │   └── utilities.scss
│   ├── types
│   │   ├── chart.d.ts
│   │   ├── common.d.ts
│   │   ├── global-components.d.ts
│   │   ├── ica-sdk.d.ts
│   │   ├── index.d.ts
│   │   └── system.ts
│   ├── utils
│   │   ├── chart.ts
│   │   ├── date.ts
│   │   ├── download.ts
│   │   ├── encryption.ts
│   │   ├── env.ts
│   │   ├── environment.ts
│   │   ├── index.ts
│   │   ├── rem.ts
│   │   └── tags.ts
│   ├── views
│   │   ├── H5
│   │   │   ├── api
│   │   │   ├── components
│   │   │   ├── constants
│   │   │   ├── hooks
│   │   │   ├── layout
│   │   │   ├── router
│   │   │   ├── store
│   │   │   ├── utils
│   │   │   └── views
│   │   ├── data
│   │   │   ├── UIShowcase
│   │   │   └── dictionary
│   │   ├── error
│   │   │   └── 404.vue
│   │   ├── login
│   │   │   └── index.vue
│   │   ├── overview
│   │   │   ├── index.vue
│   │   │   ├── leader
│   │   │   └── user
│   │   ├── redirect
│   │   │   └── index.vue
│   │   ├── sceneAnalysis
│   │   │   ├── CompetitorAnalysis.vue
│   │   │   ├── GroupAnalysis.vue
│   │   │   ├── JourneyAnalysis.vue
│   │   │   ├── ProductAnalysis.vue
│   │   │   ├── ServiceAnalysis.vue
│   │   │   ├── ThisProductAnalysis.vue
│   │   │   ├── components
│   │   │   └── index.vue
│   │   ├── sceneTest
│   │   │   └── index.vue
│   │   └── system
│   │       ├── configuration
│   │       ├── reportManagement
│   │       ├── role
│   │       ├── scene
│   │       ├── user
│   │       └── voice
│   └── vite-env.d.ts
├── tsconfig.app.json
├── tsconfig.json
├── tsconfig.node.json
├── tsconfig.tsbuildinfo
└── vite.config.ts

110 directories, 193 files
```

## 🛠️ 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

node版本: 22+
```

## 本地演示

- 启动 PC 与本地 API：`npm run dev`
- 构建离线版本：`npm run build:local`
- 执行去标识门禁：`npm run verify:neutral`

详细说明见 `docs/local-demo-runbook.md`。

开发环境 -> 开发分支直接发布
测试环境 -> 开发分支合并到 project_cqca_test_v2.0 发布
生产环境 -> 开发分支合并到 project_cqca_prod 再分别合并project_cqca_h5_prod/project_cqca_pc_prod
