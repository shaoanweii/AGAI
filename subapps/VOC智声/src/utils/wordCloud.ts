import { WORD_CLOUD_RANDOM_COLOR_PALETTE } from '@/constants'

interface WordCloudDatum {
  name?: string
  value?: number | string | null
}

interface WordCloudTextColorParams {
  dataIndex?: number
  data?: {
    name?: string
  }
}

interface WordCloudTextColorResolverOptions {
  palette?: readonly string[]
  highlightTopCount?: number
  dimOpacity?: number
}

const DEFAULT_HIGHLIGHT_TOP_COUNT = 10
const DEFAULT_DIM_OPACITY = 0.8
const FALLBACK_COLOR = '#1978C8'
const PRIMARY_HIGHLIGHT_COLOR_INDEX = 0

/**
 * 将十六进制颜色转换为 rgba 字符串，用于按词条排名控制透明度。
 * @param hex 十六进制颜色值
 * @param opacity 透明度
 * @returns rgba 颜色字符串
 */
const toRgba = (hex: string, opacity: number) => {
  const normalized = hex.trim().replace('#', '')
  const fullHex =
    normalized.length === 3
      ? normalized
          .split('')
          .map(char => `${char}${char}`)
          .join('')
      : normalized

  if (!/^[\da-fA-F]{6}$/.test(fullHex)) {
    return `rgba(25, 120, 200, ${opacity})`
  }

  const r = parseInt(fullHex.slice(0, 2), 16)
  const g = parseInt(fullHex.slice(2, 4), 16)
  const b = parseInt(fullHex.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${opacity})`
}

/**
 * 通过词条名称生成稳定哈希，让同一词条在多次渲染时保持同色。
 * @param text 词条名称
 * @param paletteLength 色板长度
 * @returns 对应色板索引
 */
const getStablePaletteIndex = (text: string, paletteLength: number) => {
  if (paletteLength <= 1) return 0

  let hash = 0
  for (let index = 0; index < text.length; index += 1) {
    hash = (hash * 31 + text.charCodeAt(index)) >>> 0
  }

  return hash % paletteLength
}

/**
 * 根据提及量取 TopN 词条索引；数值相同则保持原始顺序，避免渲染抖动。
 * @param data 词云数据
 * @param topCount 高亮数量
 * @returns TopN 词条索引集合
 */
export const createWordCloudHighlightIndexSet = (
  data: WordCloudDatum[],
  topCount: number = DEFAULT_HIGHLIGHT_TOP_COUNT
) => {
  const normalizedTopCount = Math.max(0, Math.floor(topCount))
  if (!data.length || normalizedTopCount === 0) return new Set<number>()

  const sortedIndexes = data
    .map((item, index) => ({
      index,
      value: Number(item?.value) || 0
    }))
    .sort((prev, next) => {
      if (next.value !== prev.value) return next.value - prev.value
      return prev.index - next.index
    })
    .slice(0, normalizedTopCount)
    .map(item => item.index)

  return new Set(sortedIndexes)
}

/**
 * 创建词云文字颜色解析器。
 * TopN 词条固定使用首个色值，其余词条在剩余色板中稳定随机，并降低透明度。
 * @param data 词云数据
 * @param options 色板和透明度配置
 * @returns ECharts 可直接使用的颜色解析函数
 */
export const createWordCloudTextColorResolver = (
  data: WordCloudDatum[],
  options: WordCloudTextColorResolverOptions = {}
) => {
  const palette =
    options.palette && options.palette.length > 0
      ? options.palette
      : WORD_CLOUD_RANDOM_COLOR_PALETTE
  const highlightColor = palette[PRIMARY_HIGHLIGHT_COLOR_INDEX] || FALLBACK_COLOR
  const secondaryPalette = palette.slice(PRIMARY_HIGHLIGHT_COLOR_INDEX + 1)
  const dimOpacity =
    typeof options.dimOpacity === 'number' && options.dimOpacity >= 0 && options.dimOpacity <= 1
      ? options.dimOpacity
      : DEFAULT_DIM_OPACITY
  const highlightIndexSet = createWordCloudHighlightIndexSet(data, options.highlightTopCount)

  return (params: WordCloudTextColorParams) => {
    const isHighlighted = highlightIndexSet.has(params?.dataIndex ?? -1)
    if (isHighlighted) return toRgba(highlightColor, 1)

    const paletteKey = params?.data?.name || String(params?.dataIndex ?? 0)
    const paletteIndex = getStablePaletteIndex(
      paletteKey,
      secondaryPalette.length || palette.length
    )
    const baseColor =
      secondaryPalette[paletteIndex] || highlightColor || palette[PRIMARY_HIGHLIGHT_COLOR_INDEX]
    return toRgba(baseColor, dimOpacity)
  }
}
