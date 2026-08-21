---
type: 'always_apply'
---

# VOC 数智平台前端开发指南

> 本文档包含 VOC 数智平台项目的所有开发规范、代码标准、Git 规范。

## 📋 目录

- [技术栈详情](mdc:#技术栈详情)
- [开发约束](mdc:#开发约束)
- [代码规范](mdc:#代码规范)
- [目录结构](mdc:#目录结构)
- [样式规范](mdc:#样式规范)
- [VOC 平台特有规范](mdc:#voc平台特有规范)
- [常用工具和 Hooks](mdc:#常用工具和hooks)
- [权限系统](mdc:#权限系统)
- [Git 规范](mdc:#git-规范)

---

## 技术栈详情

### 核心技术

- **前端框架**: Vue 3.4.15 (Composition API)
- **开发语言**: TypeScript 5.3.0
- **构建工具**: Vite 5.0.11
- **UI 组件库**: Element Plus
- **状态管理**: Pinia 2.1.7
- **路由管理**: Vue Router 4.2.5 (Hash 模式)

### 开发工具

- **样式预处理**: Sass 1.71.0 + Less 4.2.0
- **代码规范**: ESLint 7.32.0 + Prettier 2.8.8
- **类型检查**: vue-tsc 1.8.27
- **包管理器**: npm
- **自动导入**: unplugin-auto-import + unplugin-vue-components

### 功能库

- **HTTP 客户端**: Axios 1.6.7
- **图表库**: ECharts 5.5.0
- **工具库**: Lodash-es 4.17.21 + await-to-js 3.0.0
- **日期处理**: Day.js 1.11.10
- **加密库**: crypto-js 4.2.0
- **事件总线**: mitt 3.0.1

---

## 开发约束

### ⚠️ 严格限制

```typescript
// 禁止操作
❌ 自动执行 Git 命令 (git add, git commit, git push)
❌ 删除或修改现有目录结构
❌ 修改 vite.config.mts 中的 alias 配置
❌ 自动调整用户手动修改的样式代码
❌ 使用其他UI组件库 (项目使用 Element Plus)
```

### ✅ 必须遵循

```typescript
// 开发要求
✅ 始终使用中文 - 所有对话、注释、文档都必须使用中文
✅ 直接修改代码，不只给建议
✅ 使用 Vue 3 Composition API + <script setup>
✅ 充分利用 TypeScript 类型检查
✅ 优先使用项目既定组件和工具
✅ 遵循 Element Plus 组件使用规范
```

---

## 代码规范

### Vue 3 组件规范

```vue
<script setup lang="ts">
// 组件名称定义
defineOptions({
  name: 'DataCard'
})

// Props 接口定义
interface Props {
  data: DataInfo
  showHeader?: boolean
}

// Props 和默认值
const { data, showHeader = true } = defineProps<Props>()

// Emits 定义
const emit = defineEmits<{
  edit: [id: number]
  delete: [id: number]
}>()

// 响应式数据
const isLoading = ref(false)
const formData = reactive({
  name: '',
  description: ''
})

// 计算属性
const displayName = computed(() => data.name || '未命名数据')

// 方法
const handleEdit = () => {
  emit('edit', data.id)
}
</script>

<template>
  <div class="data-card">
    <div v-if="showHeader" class="header">
      <h3>{{ displayName }}</h3>
    </div>
    <div class="content">
      <p>{{ data.description }}</p>
    </div>
    <div class="actions">
      <a-button @click="handleEdit" type="primary">编辑</a-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.data-card {
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--color-white);

  .header h3 {
    color: var(--color-high);
    font-size: 16px;
    font-weight: 600;
  }

  .content {
    margin: 12px 0;
    color: var(--color-medium);
  }
}
</style>
```

### TypeScript 类型规范

```typescript
// 接口定义 - 使用 PascalCase
interface DataInfo {
  id: number
  name: string
  description: string
  status: DataStatus
  createTime?: string
}

// 枚举定义
enum DataStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  PROCESSING = 'processing'
}

// 类型别名
type ApiStatus = 'loading' | 'success' | 'error'

// 泛型接口
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// VOC平台特有类型
interface ProjectDetail {
  projectId: number
  projectName: string
  brand: Brand[]
  status: string
}

interface Brand {
  brandCode: string
  brandName: string
  carSeries: CarSeries[]
}
```

### 性能优化规范

```typescript
// 大型对象使用 shallowRef
const chartConfig = shallowRef({
  title: { text: 'Chart' },
  series: [
    /* 大量数据 */
  ]
})

// ECharts 实例标记为非响应式
const chartInstance = ref(null)
onMounted(() => {
  chartInstance.value = markRaw(echarts.init(element))
})

// 路由懒加载
const DataCenter = () => import('@/views/dataCenter/index.vue')

// 异步错误处理 - 使用await-to-js
const fetchDataList = async () => {
  loading.value = true
  const [err, response] = await to(dataApi.getList())
  if (err) {
    console.error('获取数据失败:', err)
    Message.error('获取数据失败，请稍后重试')
    return
  }
  dataList.value = response.data
  loading.value = false
}
```

---

## 目录结构

### 完整项目结构

```
voc_html/
├── .augment/rules/       # Augment AI 规则文档
├── public/               # 静态资源
│   ├── favicon.ico       # 网站图标
│   └── yaxinfavicon.svg  # SVG图标
├── src/                  # 源代码
│   ├── api/             # API 接口封装
│   │   ├── index.ts     # Axios实例配置
│   │   ├── main.ts      # 主要接口(登录、权限等)
│   │   ├── project.ts   # 项目管理接口
│   │   ├── dataCenter.ts # 数据中心接口
│   │   └── tag.ts       # 标签管理接口
│   ├── assets/          # 静态资源
│   │   ├── bg/          # 背景图片
│   │   ├── icon/        # 图标文件
│   │   ├── iconfont/    # 字体图标
│   │   ├── imgs/        # 图片资源
│   │   └── svg/         # SVG图标
│   ├── components/      # 公共组件
│   │   ├── global/      # 全局组件注册
│   │   ├── FCascader/   # 级联选择器
│   │   ├── FSelect/     # 选择器组件
│   │   ├── Header.vue   # 头部组件
│   │   └── Menu.vue     # 菜单组件
│   ├── constant/        # 常量定义
│   │   └── index.ts     # 全局常量(TOKEN_KEY等)
│   ├── directives/      # Vue指令
│   │   ├── auth.ts      # 权限指令
│   │   └── index.ts     # 指令注册
│   ├── hooks/           # 组合式函数
│   │   ├── table.ts     # 表格相关hooks
│   │   ├── useModal.ts  # 弹窗hooks
│   │   ├── useTagVIewData.ts # 标签时间范围处理
│   │   └── useTabPermission.ts # 权限控制hooks
│   ├── layouts/         # 布局组件
│   ├── mock/            # 模拟数据
│   ├── router/          # 路由配置
│   │   └── index.ts     # 路由定义(Hash模式)
│   ├── stores/          # Pinia 状态模块
│   │   ├── index.ts     # Store入口
│   │   └── modules/     # 状态模块
│   │       ├── app.ts   # 应用状态
│   │       └── user.ts  # 用户状态
│   ├── style/           # SCSS 全局样式
│   │   ├── global.scss  # 全局样式
│   │   ├── root.scss    # CSS变量定义
│   │   └── mainTable.scss # 表格样式
│   ├── types/           # TypeScript 类型
│   │   ├── constant.ts  # 常量类型
│   │   ├── project.d.ts # 项目相关类型
│   │   └── dataCenter.types.ts # 数据中心类型
│   ├── utils/           # 工具函数
│   │   ├── index.ts     # 通用工具函数
│   │   ├── permission.ts # 权限相关工具
│   │   └── eventBus.ts  # 事件总线
│   ├── views/           # 页面组件
│   │   ├── dataCenter/  # 数据治理
│   │   │   ├── dataProcessing/ # 规则处理
│   │   │   ├── insDataSource/  # 数据处理
│   │   │   └── dataFounding/   # 数据资产
│   │   ├── tagManagement/ # 标签管理
│   │   │   ├── application/ # 标签应用
│   │   │   ├── index/       # 标签主页
│   │   │   └── library/     # 标签库
│   │   ├── project/       # 项目管理
│   │   │   └── projectManagement/
│   │   ├── settings/      # 系统设置
│   │   │   ├── accountManagement/
│   │   │   ├── role/
│   │   │   └── baseSettings/
│   │   ├── Main.vue       # 主布局组件
│   │   ├── Login.vue      # 登录页面
│   │   └── home/          # 首页
│   ├── App.vue          # 根组件
│   ├── main.ts          # 入口文件
│   └── permission.ts    # 权限控制
├── vite.config.mts      # Vite 配置 (🚫 不要修改 alias)
├── package.json         # 项目依赖和脚本
├── tsconfig.json        # TypeScript 配置
├── components.d.ts      # 自动生成的组件类型
└── README.md            # 项目说明
```

### 组件组织规范

```
# 公共组件 - 使用目录结构
src/components/
├── FCascader/
│   └── index.vue        # 主组件文件
├── FSelect/
│   └── index.vue
├── PermissionConfiguration/
│   ├── index.vue
│   └── MenuGroup.vue    # 子组件
└── global/
    └── index.ts         # 全局组件注册

# 页面组件 - VOC平台特有结构
src/views/
├── dataCenter/          # 数据治理
│   ├── dataProcessing/  # 规则处理
│   ├── insDataSource/   # 数据处理
│   └── dataFounding/    # 数据资产
├── tagManagement/       # 标签管理
│   ├── application/     # 标签应用
│   └── index/
├── project/             # 项目管理
│   └── projectManagement/
└── settings/            # 系统设置
    ├── accountManagement/
    ├── role/
    └── baseSettings/
```

### 文件命名规范

| 文件类型   | 命名规范   | 示例            |
| ---------- | ---------- | --------------- |
| Vue 组件   | PascalCase | `DataTable.vue` |
| TypeScript | camelCase  | `formatDate.ts` |
| 样式文件   | camelCase  | `global.scss`   |
| 目录名称   | camelCase  | `dataCenter/`   |

---

## 样式规范

### SCSS 使用规范

```scss
// 使用项目CSS变量 (定义在 src/style/root.scss)
.data-card {
  padding: 16px;
  background: var(--color-white);
  border: var(--border);
  border-radius: 4px;

  .title {
    color: var(--color-high);
    font-size: 16px;
    font-weight: 600;
  }

  .content {
    color: var(--color-medium);
    margin: 12px 0;
  }

  // 响应式设计
  @media (max-width: 768px) {
    padding: 12px;
  }
}

// Element Plus 组件样式覆盖
:deep(.el-button) {
  border-radius: 4px;
}

:deep(.el-table) {
  .el-table__header-wrapper th {
    background: var(--bgc-def);
  }
}
```

### Element Plus 响应式布局

```vue
<template>
  <!-- 使用 Element Plus 栅格系统 -->
  <el-row :gutter="20">
    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <div class="grid-content">内容</div>
    </el-col>
  </el-row>

  <!-- 表单响应式 -->
  <el-form :model="form" label-position="top">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
```

---

## VOC 平台特有规范

### 数据治理模块规范

```typescript
// 数据处理API调用规范
import { useTable } from '@/hooks/table'
import { findRegulationInfo } from '@/api/dataProcessing'

// 标准表格使用模式
const {
  table,
  handleReset,
  getFirstPageTableData,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange
} = useTable({
  url: '/insights/regulation/findRegulationInfo',
  method: 'POST'
})

// 数据源管理
const { table: dataSourceTable, getFirstPageTableData: getDataSourceList } = useTable({
  url: '/insights/insDataSource/findDataSourceInfo',
  method: 'POST'
})
```

### 标签管理规范

```vue
<script setup lang="ts">
// 标签相关组件引入规范
import { useTagVIewData } from '@/hooks/useTagVIewData'
import { findTagLibCategoryTree } from '@/api/discovery'

// 标签时间范围处理
const { rangeDisabled, rangeSelectedTime } = useTagVIewData()

// 标签分类树查询
const categoryTree = ref<Record<string, any>[]>([])
const tagTypeListChange = async (val: any) => {
  if (val && val?.length) {
    categoryTree.value = await findTagLibCategoryTree(table.filter.tagType).then(res => res.result)
  } else {
    categoryTree.value = []
  }
}
</script>
```

### 项目管理规范

```typescript
// 项目详情类型定义
interface ProjectDetail {
  projectId: number
  projectName: string
  projectDesc: string
  brand: Brand[]
  status: string
  clientId: string
}

// 品牌配置类型
interface Brand {
  brandCode: string
  brandName: string
  carSeries: CarSeries[]
}

// 项目管理表格使用
const {
  table,
  form,
  handleReset,
  getTableData,
  handleSizeChange,
  handleCurrentChange,
  handleAdd,
  handleEdit,
  getFirstPageTableData,
  handleSortChange,
  sortOpts
} = useTable({
  url: '/insights/insProjectInfo/findProjectList',
  method: 'POST',
  notResetKey: ['clientId']
})
```

---

## 常用工具和 Hooks

### 核心 Hooks

```typescript
// useTable - 表格数据管理
import { useTable } from '@/hooks/table'

const {
  table, // 表格状态对象
  form, // 表单状态对象
  rowSelection, // 行选择配置
  handleReset, // 重置筛选
  getFirstPageTableData, // 获取第一页数据
  handleSizeChange, // 页面大小变化
  handleCurrentChange, // 当前页变化
  handleAdd, // 添加操作
  handleEdit, // 编辑操作
  handleSortChange, // 排序变化
  sortOpts // 排序选项
} = useTable({
  url: '/api/endpoint',
  method: 'POST',
  notResetKey: ['clientId'] // 重置时不清空的字段
})

// useTagVIewData - 标签时间范围处理
import { useTagVIewData } from '@/hooks/useTagVIewData'

const {
  rangeSelectedTime, // 选择第一个时间
  rangeDisabled // 时间选择器禁用逻辑
} = useTagVIewData()

// useTabPermission - 权限控制
import { useTabPermission } from '@/hooks/useTabPermission'

const {
  activeKey, // 当前激活的tab
  getHasPermission // 检查是否有权限
} = useTabPermission('processing')

// useModal - 弹窗管理
import { useModal } from '@/hooks/useModal'

const {
  visible, // 弹窗显示状态
  openModal, // 打开弹窗
  closeModal // 关闭弹窗
} = useModal()
```

### 工具函数

```typescript
// 通用工具函数 - src/utils/index.ts
import {
  enCrypt, // AES加密
  generateCurDateRange, // 生成当前日期范围
  resetObjectValues, // 重置对象值
  excludeNodeById, // 根据ID过滤树形结构
  delayer, // 延时器
  computedCardHeight, // 计算表格卡片高度
  listHeight // 计算列表高度
} from '@/utils'

// 权限工具函数 - src/utils/permission.ts
import { hasPermission } from '@/utils/permission'

// 检查按钮权限
const canEdit = hasPermission('project:edit')

// 常量定义 - src/constant/index.ts
import {
  TOKEN_KEY, // Token存储key
  USER_NAME_KEY, // 用户名存储key
  USER_ID_KEY, // 用户ID存储key
  DOWNLOAD_TYPE, // 导出类型
  HANDLE_STATUS, // 处理状态
  riskLevelColorMap // 风险等级颜色映射
} from '@/constant'
```

### API 请求模式

```typescript
// 使用await-to-js处理异步错误
import to from 'await-to-js'
import { Message } from '@arco-design/web-vue'

const fetchData = async () => {
  loading.value = true
  const [err, response] = await to(apiFunction())
  if (err) {
    console.error('请求失败:', err)
    Message.error('请求失败，请稍后重试')
    return
  }
  // 处理成功响应
  dataList.value = response.result
  loading.value = false
}

// 文件导出处理
const handleExport = async () => {
  const [err, response] = await to(exportApi())
  if (err) {
    Message.error('导出失败')
    return
  }
  // 处理文件下载
  const blob = new Blob([response])
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'filename.xlsx'
  a.click()
}
```

---

## 权限系统

### 权限指令使用

```vue
<template>
  <!-- 按钮权限控制 -->
  <el-button v-auth="'project:edit'" type="primary">编辑项目</el-button>
  <el-button v-auth="'project:delete'" type="danger">删除项目</el-button>

  <!-- Tab权限控制 -->
  <el-tab-pane v-if="getHasPermission('general')" name="general" label="标准规则">
    <Standard></Standard>
  </el-tab-pane>
  <el-tab-pane v-if="getHasPermission('custom')" name="custom" label="定制规则">
    <Custom></Custom>
  </el-tab-pane>
</template>

<script setup lang="ts">
import { useTabPermission } from '@/hooks/useTabPermission'

const { getHasPermission } = useTabPermission('processing')
</script>
```

### 权限检查函数

```typescript
// 在组件中检查权限
import { hasPermission } from '@/utils/permission'

const canAddUser = hasPermission('user:add')
const canEditProject = hasPermission('project:edit')

// 在路由守卫中使用
import useUserStore from '@/stores/modules/user'

const userStore = useUserStore()
const hasMenuPermission = userStore.menusMap.has('dataCenter')
```

### 动态路由生成

```typescript
// 用户状态管理中的权限处理
import useUserStore from '@/stores/modules/user'

const userStore = useUserStore()

// 获取用户权限后生成动态路由
await userStore.getUserPermissions()

// 权限数据结构
interface UserPermissions {
  clientId: string
  clientIds: any[]
  defaultClientId: string
  isAdmin: boolean
  menus: any[] // 菜单权限
  buttonPerm: string[] // 按钮权限
}
```

---

## Git 规范

### 提交信息规范

```bash
# 提交格式
<type>(<scope>): <subject>

# Type 类型
feat(数据治理): 添加数据处理规则配置功能
fix(标签管理): 修复标签应用页面筛选问题
docs(文档): 更新 README 文档
style(样式): 调整项目管理页面布局
refactor(重构): 优化API请求逻辑
perf(性能): 优化数据表格渲染性能
test(测试): 添加组件单元测试
chore(工具): 更新依赖包版本

# VOC平台特有提交示例
feat(项目管理): 实现完整的项目配置功能

- 添加品牌配置和车系配置
- 实现数据管理和预警配置
- 添加项目状态管理
- 集成权限控制功能
```

### 分支管理

```bash
# 分支命名规范
feature/data-processing-rules    # 数据处理规则功能
feature/tag-management-app      # 标签管理应用功能
fix/project-form-validation     # 项目表单验证修复
release/v1.2.0                 # 发布分支
hotfix/v1.1.1-urgent-fix       # 热修复分支
```

### 代码质量检查

```bash
# 提交前检查清单
✅ npm run lint        # ESLint 检查通过
✅ npm run type-check  # TypeScript 检查通过
✅ 功能测试正常
✅ 无 console.log 调试代码
✅ 无未使用的导入和变量
✅ Element Plus 组件使用规范
```

---

## 常用命令

```bash
# 开发命令
npm install              # 安装依赖
npm run dev              # 启动开发服务器 (http://localhost:5175)
npm run build            # 构建生产版本
npm run preview          # 预览构建结果

# 不同环境构建
npm run build:dev        # 开发环境构建
npm run build:test       # 测试环境构建
npm run build:rc         # RC环境构建

# 代码质量
npm run lint             # ESLint代码检查
npm run type-check       # TypeScript类型检查

# 环境要求
Node.js: 20.11.0/x64 (Iron)
npm: 9.0.0+
```

## 开发服务器配置

```typescript
// vite.config.mts 关键配置
export default defineConfig({
  base: '/ins/', // 部署基础路径
  server: {
    port: 5175,
    host: '0.0.0.0',
    proxy: {
      '^/api': {
        target: 'http://172.16.80.16:30805/', // 测试环境后端
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
```

## 测试标识规范

```vue
<template>
  <!-- 自动化测试标识格式 -->
  <el-button :data-testid="`login-1004`">登录</el-button>
  <div :data-testid="`processing-detail-3001`">{{ detail?.regulationTypeText }}</div>
  <div :data-testid="`processing-detail-4001-${index}`">动作内容</div>
</template>
```

格式说明：

- `:data-testid="`路由 name-编号`"`
- `:data-testid="`功能模块-详情-编号`"`
- 动态编号使用 `${index}` 格式

---

**总结**: 遵循这些规范确保 VOC 数智平台项目代码质量、开发效率和团队协作的一致性。所有修改都应严格按照这些标准执行。
