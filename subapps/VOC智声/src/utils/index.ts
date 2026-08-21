/**
 * 工具函数统一导出
 * 只保留项目实际需要的工具
 */

import { TOKEN_KEY } from '@/constants'

// 导出基础环境工具
export { isDev, isProd, isTest } from './env'

// 导出运行环境检测工具
export {
  getCurrentEnvironment,
  EnvironmentType,
  isWeWorkEnvironment,
  isPCEnvironment,
  isMobileEnvironment
} from './environment'

/**
 * 清洗浅层查询参数对象中的空值。
 * - 用于接口查询前裁剪无效条件，避免把空数组、空字符串、null、undefined 传给后端。
 * - 保留 0、false 等有业务含义的值。
 * @param params 原始查询参数对象
 * @returns 清洗后的新对象
 */
export const cleanEmptyParams = <T extends Record<string, any>>(params?: T): T | undefined => {
  if (!params) {
    return params
  }

  const cleanedParams: Record<string, any> = {}

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }

    if (Array.isArray(value) && value.length === 0) {
      return
    }

    cleanedParams[key] = value
  })

  return cleanedParams as T
}

// 数据千分位显示
export const Thousandth = (num: number | string): string => {
  if (num != undefined) {
    // const reg = /\d{1,3}(?=(\d{3})+$)/g
    // return (num + '').replace(reg, '$&,')

    // 转为数字
    const value = typeof num === 'string' ? parseFloat(num) : num
    // 整数
    if (Number.isInteger(value)) {
      return value.toLocaleString()
    }
    // 四舍五入+2位
    else {
      return parseFloat(value.toFixed(2)).toLocaleString()
    }
  }
  return '-'
}

/**
 * 格式化数值显示
 * 小于十万：显示千分位分隔符，如 "1,290"
 * 大于等于十万：显示万位科学计数，如 "23.25w"
 * @param num 原始数值
 * @returns 格式化后的数值字符串
 */
export const formatNumber = (num: number | string | undefined): string => {
  if (num === undefined || num === null) return '-'

  const numValue = Number(num)
  if (isNaN(numValue)) return '-'

  if (numValue >= 10000) {
    const wanValue = numValue / 10000
    return wanValue % 1 === 0 ? `${wanValue}w` : `${wanValue.toFixed(2)}w`
  }

  return Thousandth(numValue)
}

/**
 * 格式化坐标轴标签
 * 超过1000使用k单位，超过10000使用w单位
 * @param value 原始数值
 * @returns 格式化后的坐标轴标签字符串
 */
export const formatAxisLabel = (value: number | string): string => {
  if (value === undefined || value === null) return '-'

  const numValue = Number(value)
  if (isNaN(numValue)) return '-'

  if (numValue >= 10000) {
    const wanValue = numValue / 10000
    return wanValue % 1 === 0 ? `${wanValue}w` : `${Number(wanValue.toFixed(2))}w`
  }

  if (numValue >= 1000) {
    const kValue = numValue / 1000
    return kValue % 1 === 0 ? `${kValue}k` : `${Number(kValue.toFixed(2))}k`
  }

  return String(numValue)
}

// 保留一位小数
export const toFixTwo = (data: number): string => {
  const result =
    parseFloat(data.toString()).toString() == 'NaN' ? '-' : parseFloat(data.toString()).toFixed(1)
  return result
}

/**
 * 保留一位小数（四舍五入）
 * - 入参无效返回 '-'
 */
export const toFixOne = (data: number | string): string => {
  const n = Number(data)
  if (!Number.isFinite(n)) return '-'
  return n.toFixed(1)
}

/**
 * 超过/等于 1000 使用 k 单位显示
 * - `fractionDigits` 控制小数位数，默认 0。
 * - 负数保留符号。
 * - 入参无效返回 '-'
 */
export const formatK = (value: number | string, fractionDigits: number = 0): string => {
  const n = Number(value)
  if (!Number.isFinite(n)) return '-'
  const digits = Math.max(0, Math.floor(fractionDigits))
  const abs = Math.abs(n)
  if (abs >= 1000) {
    const k = abs / 1000
    const body = k.toFixed(digits)
    return `${n < 0 ? '-' : ''}${body}k`
  }
  // 小于 1000 原样返回；若需定制小数位，可在调用处处理
  return n % 1 === 0 ? String(n) : n.toFixed(digits)
}

/**
 * 格式化百分比数值
 * @param data 原始数值
 * @returns 格式化后的百分比字符串
 */
export const formatPercent = (data: number | string): string => {
  const numValue = parseFloat(data as string)
  if (isNaN(numValue)) {
    return '-'
  }
  return toFixOne(numValue)
}

/**
 * 格式化显示带符号
 */
export const formatRatePrefix = (value: number | string): string => {
  const prefix = Number(value) > 0 ? '+' : ''
  return `${prefix}${formatPercent(value)}`
}

// 格式化-公共函数
export const fmtCom = (num: any, dataType?: string, hasHot?: boolean, hasFix?: boolean) => {
  // 有定制传参用 数值 (兼容旧版)
  if (dataType === 'mention' || dataType === 'count') {
    return formatNumber(num)
  }
  // 百分比
  else {
    // 空值直接返回
    const numValue = parseFloat(num as string)
    if (isNaN(numValue)) {
      return '-'
    }

    let res
    // 带前缀
    if (hasFix) {
      res = formatRatePrefix(num) + '%'
    } else {
      res = formatPercent(num) + '%'
    }

    // 带高亮
    if (hasHot) {
      const cls = num > 80 ? 'hot' : ''
      res = `<span class="${cls}">${res}</span>`
    }
    return res
  }
}

// 格式化数值
export const fmtNum = (num: any) => {
  return formatNumber(num)
}

// 格式化百分比
export const fmtPer = (num: any) => {
  return fmtCom(num)
}

// 格式化+前缀(环比、同比)
export const fmtFix = (num: any, dataType?: string) => {
  return fmtCom(num, dataType, false, true)
}

// 格式化+高亮（负面率）
export const fmtHot = (num: any, dataType?: string) => {
  return fmtCom(num, dataType, true)
}

// 格式化普通dom 的  悬浮数据
export const fmtHoverData = (item: any, dataType?: string): any[] => {
  if (!item) return []

  const o_negativeRate = {
    label: '负面率',
    value: fmtPer(item.negativeRate),
    rateMoM: fmtFix(item.negativeRateMoM),
    rateYoY: fmtFix(item.negativeRateYoY)
  }
  const o_mentions = {
    label: '提及量',
    value: fmtNum(item.mentions),
    rateMoM: fmtFix(item.mentionsMoM),
    rateYoY: fmtFix(item.mentionsYoY)
  }

  // 默认返回2项
  if (!dataType) return [o_negativeRate, o_mentions]
  // 仅提及量
  if (dataType === 'mentions') return [o_mentions]
  // 仅负面率
  if (dataType === 'negativeRate') return [o_negativeRate]

  return []
}

// 格式化 数据分析表格的悬浮数据
export const fmtHoverData2 = (item: any, dataType: string, valueType: string): any[] => {
  if (!item) return []

  switch (dataType) {
    // ['负面率', '环比'];
    case 'negativeRateMoM':
      return [
        {
          label: '负面率',
          value: fmtPer(item.value1),
          rateMoM: fmtFix(item.value1MoM),
          rateYoY: fmtFix(item.value1YoY)
        }
      ]
    // ['提及量', '环比'];
    case 'mentionMoM':
      return [
        {
          label: '提及量',
          value: fmtNum(item.value1),
          rateMoM: fmtFix(item.value1MoM),
          rateYoY: fmtFix(item.value1YoY)
        }
      ]
    // ['负面率', '提及量']
    case 'negativeRateMention':
    default:
      if (valueType === 'value1')
        return [
          {
            label: '负面率',
            value: fmtPer(item.value1),
            rateMoM: fmtFix(item.value1MoM),
            rateYoY: fmtFix(item.value1YoY)
          }
        ]
      else
        return [
          {
            label: '提及量',
            value: fmtNum(item.value2),
            rateMoM: fmtFix(item.value2MoM),
            rateYoY: fmtFix(item.value2YoY)
          }
        ]
  }
}

/**
 * 格式化日期
 * @param date 日期对象或日期字符串
 * @param format 格式化模板，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export const formatDate = (date: Date | string, format: string = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!date) return '-'

  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year.toString())
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 将颜色转换为 rgba 格式
 * @param color 颜色字符串
 * @param alpha 透明度，默认为 1
 * @returns rgba 格式的颜色字符串
 * */
export const toRgba = (color: string | undefined, alpha: number = 1): string => {
  // 支持 #RGB/#RRGGBB 或 rgb/rgba 字符串，其他直接返回原色
  try {
    if (!color) return ''
    const c = color.trim()
    if (c.startsWith('#')) {
      let r = 0,
        g = 0,
        b = 0
      if (c.length === 4) {
        r = parseInt(c[1] + c[1], 16)
        g = parseInt(c[2] + c[2], 16)
        b = parseInt(c[3] + c[3], 16)
      } else if (c.length === 7) {
        r = parseInt(c.slice(1, 3), 16)
        g = parseInt(c.slice(3, 5), 16)
        b = parseInt(c.slice(5, 7), 16)
      } else {
        return c
      }
      return `rgba(${r}, ${g}, ${b}, ${alpha})`
    }
    if (c.startsWith('rgb')) {
      // 提取数字
      const nums = c
        .replace(/rgba?\(/, '')
        .replace(/\)/, '')
        .split(',')
        .map(n => Number(n.trim()))
      const [r, g, b] = nums
      if ([r, g, b].some(v => Number.isNaN(v))) return c
      return `rgba(${r}, ${g}, ${b}, ${alpha})`
    }
    return c
  } catch (_) {
    return color || ''
  }
}

/**
 * 判断 token 是否为可发送到后端的登录凭证。
 * 仅排除空值和已知的对象占位字符串，兼容后端生成的 opaque token。
 */
export const isValidToken = (token: unknown): token is string => {
  if (typeof token !== 'string') return false

  const normalizedToken = token.trim()
  return !['', '{}', 'null', 'undefined', '[object Object]'].includes(normalizedToken)
}

/**
 * @description: 设置 token
 * @param _token 待保存的登录凭证
 * @returns 是否成功保存；非法占位值会被清理
 */
export const setToken = (_token: string): boolean => {
  if (!isValidToken(_token)) {
    localStorage.removeItem(TOKEN_KEY)
    return false
  }

  localStorage.setItem(TOKEN_KEY, _token)
  return true
}

//清理token
export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getJsonObjectMap = () => {}

// 去除无值的key
export const getRealAttr = (obj: any) => {
  const result = {} as any
  for (const key in obj) {
    if (obj[key] !== '' && obj[key] !== null && obj[key] !== undefined) {
      result[key] = obj[key]
    }
  }
  return result
}

// 判断字符串是否为链接
export const isLink = (val: unknown): boolean => {
  if (typeof val !== 'string') return false
  return /^https?:\/\/.+/i.test(val)
}

/**
 * 统一计算水印平铺单元尺寸。
 * - 保留调用侧传入的疏密控制能力
 * - 同时兜住过小密度，避免字号放大后文字贴边导致裁切
 */
export const getWatermarkTileSize = (density: any) => {
  return Math.max(Number(density) || 0, 240)
}

/**
 * 生成水印图片 dataURL。
 * @param text1 第一行文本
 * @param text2 第二行文本
 * @param color 文本颜色
 * @param opacity 透明度
 * @param fontSize 字号
 * @param angle 旋转角度
 * @param density 水印密度，最终会转换为平铺单元尺寸
 * @returns 可直接用于 background-image 的 dataURL
 */
export const getWatermarkDataUrl = (
  text1: any,
  text2: any,
  color: any,
  opacity: any,
  fontSize: any,
  angle: any,
  density: any
) => {
  const primaryText = String(text1 ?? '--')
  const secondaryText = String(text2 ?? '--')
  const canvasSize = getWatermarkTileSize(density)
  const canvas = document.createElement('canvas')
  canvas.width = canvasSize
  canvas.height = canvasSize

  const ctx = canvas.getContext('2d')

  if (!ctx) {
    console.error('Canvas context could not be retrieved')
    return ''
  }

  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.font = `${fontSize}px Arial`
  ctx.fillStyle = color
  ctx.globalAlpha = opacity
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.translate(canvas.width / 2, canvas.height / 2)
  ctx.rotate((-angle * Math.PI) / 180)

  const lineHeight = fontSize * 1.5
  // 水印只绘制一组居中文字，避免在单个平铺单元四角补字后，
  // 页面边缘出现被截断的半个字符或半截姓名。
  ctx.fillText(primaryText, 0, -lineHeight / 2)
  ctx.fillText(secondaryText, 0, lineHeight / 2)

  return canvas.toDataURL()
}

// 添加水印
export const createWatermark = (
  text1: any,
  text2: any,
  color: any,
  opacity: any,
  fontSize: any,
  angle: any,
  density: any
) => {
  const dataUrl = getWatermarkDataUrl(text1, text2, color, opacity, fontSize, angle, density)
  const tileSize = getWatermarkTileSize(density)

  if (!dataUrl) {
    return
  }

  const watermark = document.createElement('div')
  watermark.id = 'page-watermark'
  watermark.style.position = 'fixed'
  watermark.style.top = '0'
  watermark.style.left = '0'
  watermark.style.width = '100%'
  watermark.style.height = '100%'
  watermark.style.pointerEvents = 'none'
  watermark.style.zIndex = '9999'
  watermark.style.backgroundImage = `url(${dataUrl})`
  watermark.style.backgroundRepeat = 'repeat'
  watermark.style.backgroundSize = `${tileSize}px ${tileSize}px`
  watermark.style.backgroundPosition = '0 0'

  document.body.appendChild(watermark)
}

export const removeWatermark = () => {
  // 查找水印元素
  const watermark = document.getElementById('page-watermark')
  if (watermark) {
    document.body.removeChild(watermark)
  }
}

// 组装品牌车系
/**
 * 将集合中每个对象的品牌与车系组装为 "A-B"，对结果去重后使用、拼接返回。
 * - 仅当同时存在有效 `brand` 与 `carSeries` 时才参与组装；两端空白会被清理。
 * - 去重依据为完整字符串 "brand-carSeries" 的字面值。
 * - 入参为空或无有效项时，返回空字符串。
 * @param list 含有 `brand`、`carSeries` 字段的对象集合
 */
export const assembleBrandCarSeries = (
  list: Array<{ brand?: string | null; carSeries?: string | null } | null | undefined>
): string => {
  if (!Array.isArray(list) || list.length === 0) return ''
  const uniq = new Set<string>()
  for (const item of list) {
    if (!item) continue
    const brand = (item.brand ?? '').toString().trim()
    const series = (item.carSeries ?? '').toString().trim()
    let str = ''
    if (brand && series) {
      str = `${brand}-${series}`
    } else if (brand) {
      str = brand
    } else if (series) {
      str = series
    }
    if (!str) continue
    uniq.add(str)
  }
  return Array.from(uniq).join('、')
}

/**
 * 在树形结构中根据指定字段查找节点
 * @param nodes 树形节点数组
 * @param key 要查找的值
 * @param field 要匹配的字段名，默认为 'key'
 * @param childrenField 要匹配的子级字段名，默认为 'children'
 * @returns 找到的节点对象，未找到返回 null
 */
export const findNodeByField = (
  nodes: any[],
  key: string,
  field: string = 'key',
  childrenField: string = 'children'
): any => {
  if (!nodes || !Array.isArray(nodes)) return null
  for (const node of nodes) {
    if (node[field] === key) return node
    if (node[childrenField] && Array.isArray(node[childrenField])) {
      const found = findNodeByField(node[childrenField], key, field, childrenField)
      if (found) return found
    }
  }
  return null
}

/**
 * 将数字转换为中文小写数字（支持 0~9999）
 * @param {number} num - 要转换的正整数（超出范围会返回原数字字符串）
 * @returns {string} 中文小写数字
 */
export function numberToChinese(num: number) {
  // 1. 基础校验：必须是整数，且在支持范围内
  if (!Number.isInteger(num) || num < 0 || num > 9999) {
    return String(num) // 超出范围返回原数字字符串
  }

  // 2. 中文数字映射（0-9）
  const digitMap = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九']
  // 3. 位数单位映射（十、百、千）
  const unitMap = ['', '十', '百', '千']

  // 特殊处理：0 直接返回
  if (num === 0) return '零'

  // 将数字转为字符串，方便按位处理（如 123 → "123"）
  const numStr = String(num)
  const length = numStr.length // 数字位数（1~4位）
  let result = '' // 最终结果
  let hasZero = false // 标记是否需要加“零”（避免连续零或末尾零）

  // 4. 按位数从高位到低位遍历（千 → 百 → 十 → 个）
  for (let i = 0; i < length; i++) {
    const digit = parseInt(numStr[i]) // 当前位数字（0-9）
    const position = length - 1 - i // 当前位的位数（千=3，百=2，十=1，个=0）

    if (digit === 0) {
      // 情况1：当前位是0 → 标记需要加零（后续非零位时补零）
      hasZero = true
    } else {
      // 情况2：当前位非0
      // 若之前有零，先补“零”（如 103 → 一百零三）
      if (hasZero) {
        result += '零'
        hasZero = false // 补零后重置标记
      }
      // 拼接当前位的中文数字 + 位数单位（个位无单位）
      result += digitMap[digit] + unitMap[position]
    }
  }

  // 5. 特殊优化：处理“一十”→“十”（如 10 → 十，11 → 十一，而非“一十一”）
  if (length === 2 && numStr[0] === '1') {
    result = result.slice(1) // 去掉开头的“一”
  }

  return result
}

/**
 * @description: 跳转新的tab
 * @param {string} url
 * @return {*}
 */
export const openWindow = (url: string) => {
  window.open(url, '_blank')
}

// 获取指定URL的参数
export const getQueryParams = (targetUrl: any): Record<string, string> => {
  if (!targetUrl) {
    return {}
  }

  const extractQueryFromHash = (hash: string): string => {
    const index = hash.indexOf('?')
    return index >= 0 ? hash.slice(index + 1) : ''
  }

  const normalizeQuery = (query: string): string => query.replace(/^[?#]/, '').replace(/&+/g, '&')

  let queryString = ''

  try {
    const url = new URL(targetUrl)
    if (url.hash) {
      queryString = extractQueryFromHash(url.hash)
    }
    if (!queryString && url.search) {
      queryString = url.search.slice(1)
    }
  } catch {
    const hashIndex = targetUrl.indexOf('#')
    if (hashIndex >= 0) {
      queryString = extractQueryFromHash(targetUrl.slice(hashIndex))
    } else {
      const queryIndex = targetUrl.indexOf('?')
      queryString = queryIndex >= 0 ? targetUrl.slice(queryIndex + 1) : ''
    }
  }

  queryString = normalizeQuery(queryString)

  if (!queryString) {
    return {}
  }

  const params = new URLSearchParams(queryString)
  const result: Record<string, string> = {}
  params.forEach((value, key) => {
    result[key] = value
  })
  return result
}
