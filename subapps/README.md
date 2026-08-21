# AGAI 标品子应用

本目录保存 AGAI 标品门户关联子应用的可调整源码，后续统一从当前项目维护。

| 目录 | 应用 | 导入来源 | 导入基线 |
| --- | --- | --- | --- |
| `AI消费者2` | 用户洞察引擎 / 消费者智调 | `/Users/Qiu./Documents/Codex_projects/AI消费者2` | `afdb3de495659a06ff6f791a859870820a6e3fd2` |
| `洞察引擎` | 声音洞察引擎（前端、后端与本地运行配套） | `/Users/Qiu./Documents/Codex_projects/洞察引擎` | 2026-08-10 当前文件快照；源目录无 Git 仓库 |
| `VOC智声` | VOC 智声应用（PC/H5 与本地演示服务） | `/Users/Qiu./Documents/Codex_projects/VOC智声` | `da27fc1482ee6b5776b3a78d503fa20d491190a7` |

归集内容包含源码、配置、锁文件、项目规则、脚本和项目文档。洞察引擎源目录没有 Git 元数据，复制时排除了 `node_modules`、`.npm-cache`、`.gradle`、前端 `dist`、Playwright 临时目录及后端各模块的 `build` 编译产物。

门户根目录执行 `npm run build:subapps` 时，会依次构建三个子应用到 `public/apps/consumer`、`public/apps/insight` 和 `public/apps/voc`。首次在新目录构建前，需要分别按各子应用锁文件安装依赖。
