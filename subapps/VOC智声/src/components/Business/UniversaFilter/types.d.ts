// 字段类型枚举
export type FieldType =
  | 'daterange' // 日期范围
  | 'brand' // 品牌
  | 'series' // 车系
  | 'cascader' // 级联选择
  | 'select' // 下拉选择
  | 'selectv2' // 下拉选择 v2（用于大量数据）
  | 'input' // 输入框
  | 'experienceCode' // 体验代码（旧版：1-4 级多下拉）
  | 'experienceCodeLinkage' // 体验代码（新版：类型 + 级联联动）
  | 'placeholder' // 占位符
  | 'dataSource' // 数据源
  | 'btnSwitch' // 按钮开关组

// 字段配置接口
export interface FilterFieldConfig {
  type: FieldType // 字段类型
  prop: string // 字段属性名
  label?: string // 字段标签
  span?: number // el-col 的 span 值，默认 6
  placeholder?: string // 占位提示
  options?: Array<{ label?: string; value?: any; [key: string]: any }> // 下拉选项
  props?: Record<string, any> // el-select / el-select-v2 的 props
  cascaderProps?: Record<string, any> // el-cascader 的 props
  defaultValue?: any // 默认值
  getDefaultValue?: (
    field: FilterFieldConfig,
    otherProps?: { allConfig: FilterFieldConfig[]; route: any } // 其他的所有配置项
  ) => any // 动态默认值
  clearable?: boolean // 是否可清空
  multiple?: boolean // 是否多选（select/selectv2）
  showSplitLine?: boolean // 是否在该字段后显示分割线
  maxLength?: number // input 允许输入的最大长度
  disabled?: boolean // 是否禁用
  filterType?: string // 过滤类型默认值（用于角色默认值回填）
  showSelectAll?: boolean // 是否显示全选按钮（仅适用于 selectv2 多选）

  // experienceCodeLinkage 专用：体验代码类型字段名（会写入 formData/params），例如 tagType
  tagTypeProp?: string
  // experienceCodeLinkage 专用：体验代码类型默认值，例如 CA
  tagTypeDefaultValue?: string
  // experienceCodeLinkage 专用：固定标签体系，固定后不展示标签体系下拉，也不参与查询传参
  fixedTagType?: string
  // experienceCodeLinkage 专用：是否隐藏标签体系下拉
  hideTagType?: boolean
  // experienceCodeLinkage 专用：按根节点名称过滤体验代码树，不配置则不过滤
  rootTagName?: string
  // experienceCodeLinkage 专用：体验代码树请求层级
  requestLevel?: number
  // experienceCodeLinkage 专用：隐藏固定根节点，仅展示其子树；提交时自动补齐根节点路径
  hideRootInCascader?: boolean
  tyPageType?: string // 专用：页面类型（用于区分不同页面的配置） 如果同一个类型的配置 但是不同页面显示的东西不一样 就可以使用这个配置
}

// 组件 Props 接口
export interface UniversaFilterProps {
  routeName: string // 路由名称
}

// 组件 Emits 接口
export interface UniversaFilterEmits {
  (e: 'search', value: Record<string, any>): void
  (e: 'reset'): void
}
