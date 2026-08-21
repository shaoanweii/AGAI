/**
 * 运行环境类型
 */
export enum EnvironmentType {
  /** 企业微信环境 */
  WEWORK = 'wework',
  /** PC浏览器环境 */
  PC = 'pc',
  /** 移动端浏览器环境 */
  MOBILE = 'mobile',
  /** 未知环境 */
  UNKNOWN = 'unknown'
}

/**
 * 获取当前代码运行环境
 * @returns {EnvironmentType} 环境类型
 */
export function getCurrentEnvironment(): EnvironmentType {
  const userAgent = navigator.userAgent.toLowerCase()
  
  // 判断是否为企业微信环境
  if (userAgent.includes('wxwork')) {
    return EnvironmentType.WEWORK
  }
  
  // 判断是否为移动端
  const isMobile = /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(userAgent)
  
  if (isMobile) {
    return EnvironmentType.MOBILE
  }
  
  // 判断是否为PC环境
  if (userAgent.includes('windows') || userAgent.includes('macintosh') || userAgent.includes('linux')) {
    return EnvironmentType.PC
  }
  
  return EnvironmentType.UNKNOWN
}

/**
 * 判断是否为企业微信环境
 * @returns {boolean}
 */
export function isWeWorkEnvironment(): boolean {
  return getCurrentEnvironment() === EnvironmentType.WEWORK
}

/**
 * 判断是否为PC环境
 * @returns {boolean}
 */
export function isPCEnvironment(): boolean {
  return getCurrentEnvironment() === EnvironmentType.PC
}

/**
 * 判断是否为移动端环境
 * @returns {boolean}
 */
export function isMobileEnvironment(): boolean {
  return getCurrentEnvironment() === EnvironmentType.MOBILE
}