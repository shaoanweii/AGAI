# 批量事件接口对齐说明

本文档记录 PC 批量事件接口与报表服务 Swagger 的当前对齐结果。Swagger 来源：
本说明已按 VOC智声本地兼容接口整理，读取日期：2026-06-09。

## PC 接口清单

PC 批量事件接口统一由 `src/api/batchEvent/index.ts` 维护，基础路径为
`/report/batch-event`。

已声明接口：

- 列表与总览：`batchEventList`、`dashboard/stat-cards`、`dashboard/event-list`
- 条件与统计：`conditions`、`brief`、`data-stat`、`trend-stat`、`car-series-stat`、`scene-stat`、`opinion-stat`、`province-stat`、`channel-stat`、`report-summary`
- 处理进度：`init`、`approve`、`batch-approve`、`confirm`、`batch-confirm`、`close`、`batch-close`、`reject`、`batch-reject`、`cc`、`reassign`、`handle-complete`
- 任务处理：`task-list`、`create-task`、`edit-task`、`update-task-progress`、`delete-task`、`reassign-task`
- Swagger 新增补齐：`create-event`、`export-event`、`executor-callback`、`init-batch-warning`、`getBatchEventListSounds`

移动端 `/mobileTerminal/batch-event/*` 接口仍由 H5 专属 API 维护，不混入 PC API。

## 关键入参约定

`/report/batch-event/conditions` 使用 `BatchEventConditionsQueryModel`：

- `ids?: string[]`：批量事件页、批量响应弹窗、批量事件详情按事件 ID 查询条件时使用。
- `soundIds?: string[]`：仅原声查询页面事件下发弹窗使用，批量事件页面不传该字段。

`/report/batch-event/confirm` 与 `/report/batch-event/batch-confirm`：

- 基础字段包含主责人、主责部门、处理方式、说明等。
- `handleMode` 使用 Swagger 约定值：`VOC` 表示 VOC 系统闭环，`ZJZ` 表示执剑者系统闭环。
- 协同字段已由旧 `coordinatingDepts` 调整为拆分字段：`coordinateSecondDeptId`、`coordinateSecondDeptName`、`coordinateThirdDeptId`、`coordinateThirdDeptName`、`coordinateUserId`、`coordinateUserEmpNo`、`coordinateUserName`，字段值均为去重后的 JSON 字符串数组。
- 详情弹窗“处理进度”确认处理按已选协同人员反推所属二级/三级部门并提交上述协同字段；列表页批量响应当前未提供协同选择控件，暂不向 `/batch-confirm` 提交协同字段。
- 选择 VOC 系统闭环时，不传执剑者专属字段。
- 选择执剑者系统闭环时，需传 `pageUrl`，值为当前浏览器页面 `window.location.href`。
- 执剑者系统闭环可同时传 `custType`、`usageScenario`、`topicText`，字段值为对应已选条件名称数组的 JSON 字符串。
- 移动端 `/mobileTerminal/batch-event/confirm` 与 PC 确认处理使用相同入参约定，H5 处理进度页同样提交拆分后的协同字段和名称数组字段。

## 使用边界

- PC 列表页面保留现有表单字段，但提交前会映射到 Swagger 字段，例如 `warningEventName` 映射为 `eventName`。
- 原声查询页事件下发弹窗只使用 `soundIds` 获取下发条件，不复用事件 ID 条件缓存。
- 新增接口目前只补齐 API 与类型声明；页面功能是否接入由对应业务入口决定。
