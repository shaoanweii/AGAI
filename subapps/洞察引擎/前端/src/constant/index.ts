// 存储token的key
export const TOKEN_KEY = 'ins_token'
export const USER_NAME_KEY = 'ins_user_name'
export const USER_ID_KEY = 'ins_user_id'
export const VERSION_KEY = 'ins_version'
/**
 * 导出类型
 */
export const DOWNLOAD_TYPE: Record<DownLoadType, DownLoadType> = {
  all: 'all',
  invalid: 'invalid',
  fail: 'fail'
}

type HandleStatusKey = 'untreated' | 'process' | 'done'
/**
 * 处理状态
 */
export const HANDLE_STATUS: Record<HandleStatusKey, HandleStatus> = {
  untreated: '0',
  process: '1',
  done: '2'
}

/**
 * @description: 风险预警颜色
 * @return {*}
 */
export const riskLevelColorMap: Record<string, string> = {
  S: '#F53F3F',
  A: '#F77234',
  B: '#F7BA1E',
  C: '#007AFF',
  D: '#23BCF8'
}

/**
 * 数据类型枚举
 */
export enum DataType {
  RESULT = 'result',
  CLEAN = 'clean',
  RAW = 'raw'
}

export enum KeywordLibraryTab {
  RULE = 'rule',
  ACCOUNT = 'account'
}

export enum CorpusMappingTab {
  TEXT = 'text',
  SURVEY = 'survey'
}

export enum DiscoveryTab {
  TEXT = 'text',
  SURVEY = 'survey'
}

export enum ClosedLoopRulesTab {
  SINGLE = 'single',
  BATCH = 'batch'
}

export enum BrandSeriesTab {
  BRAND = 'brand',
  SERIES = 'series',
  AUTOMAKER = 'Automaker'
}

// EAC退出调整
const EacSSOMap = {
  development: 'https://example.com/logout',
  build_dev: 'https://example.com/logout',
  build_test: 'https://example.com/logout',
  build_rc: 'https://example.com/logout',
  production: 'https://example.com/logout'
}

export const eacSso =
  EacSSOMap[import.meta.env.MODE as keyof typeof EacSSOMap] || EacSSOMap.development

// tooltip 配置 - 直接在 show-overflow-tooltip 上传递对象
export const showOverflowTooltipConfig = {
  popperClass: 'raw-data-tooltip common-tooltip',
  effect: 'dark'
}

//数据查询下载
export const DATAQUERY_DOWNLOAD_MAP = {
  // 数据查询-下载按钮
  DOWNLOAD: 'dataCenter-dataQuery-down'
} as const
