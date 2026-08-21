/**
 * 全局组件类型声明
 * 为全局注册的组件提供 TypeScript 类型支持
 */

declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    // UI 基础组件
    SortNum: (typeof import('@/components/UI/SortNum/index.vue'))['default']
    FCard: (typeof import('@/components/UI/FCard/index.vue'))['default']
    FTable: (typeof import('@/components/UI/FTable/index.vue'))['default']
    FSelect: (typeof import('@/components/UI/FSelect/index.vue'))['default']
    FAnalyseWrap: (typeof import('@/components/UI/FAnalyseWrap/index.vue'))['default']
    ButtonGroup: (typeof import('@/components/UI/ButtonGroup/index.vue'))['default']
    SvgIcon: (typeof import('@/components/UI/SvgIcon/index.vue'))['default']

    // 图表组件
    FEcharts: (typeof import('@/components/Charts/FEcharts/index.vue'))['default']
  }
}

export {}
