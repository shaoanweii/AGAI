import { sentimentColors } from '@/constants'

export interface VoiceTopicHighlightItem {
  topic?: string
  sentiment?: string
  originalTexTScene?: string | null
  originalTextScene?: string | null
}

interface TextRange {
  start: number
  end: number
}

interface RenderState {
  cursor: number
  color: string
  backgroundColor: string
  ranges: TextRange[]
}

const TEXT_NODE = 3
const ELEMENT_NODE = 1
const DEFAULT_TOPIC_HIGHLIGHT_COLOR = '#1677FF'
// 根据观点,原文高亮背景色MAP
const TOPIC_HIGHLIGHT_BACKGROUND_MAP: Record<string, string> = {
  '#1677FF': '#E2F3FE',
  '#FF4B4C': '#FEE9E5',
  '#14CA64': '#E5FEEB'
}
const VOID_TAGS = new Set([
  'area',
  'base',
  'br',
  'col',
  'embed',
  'hr',
  'img',
  'input',
  'link',
  'meta',
  'param',
  'source',
  'track',
  'wbr'
])

const escapeHtml = (text: string) =>
  text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const normalizeLineBreak = (text: string) => text.replace(/\r\n?/g, '\n').replace(/\u00a0/g, ' ')

const renderPlainText = (text: string) => escapeHtml(text).replace(/\n/g, '<br>')

const resolveHighlightBackground = (color: string) => {
  const normalized = color.trim()
  if (/^#([\da-f]{3}|[\da-f]{6})$/i.test(normalized)) {
    const hex = normalized.slice(1)
    const fullHex =
      hex.length === 3
        ? hex
            .split('')
            .map(char => `${char}${char}`)
            .join('')
        : hex
    const normalizedHex = `#${fullHex.toUpperCase()}`
    const mappedColor = TOPIC_HIGHLIGHT_BACKGROUND_MAP[normalizedHex]
    if (mappedColor) return mappedColor
    const r = parseInt(fullHex.slice(0, 2), 16)
    const g = parseInt(fullHex.slice(2, 4), 16)
    const b = parseInt(fullHex.slice(4, 6), 16)
    return `rgba(${r}, ${g}, ${b}, 0.12)`
  }
  return '#E2F3FE'
}

const collectNodeText = (node: Node): string => {
  if (node.nodeType === TEXT_NODE) {
    return normalizeLineBreak(node.textContent || '')
  }

  if (node.nodeType !== ELEMENT_NODE) return ''

  const element = node as HTMLElement
  if (element.tagName.toLowerCase() === 'br') return '\n'

  return Array.from(element.childNodes)
    .map(child => collectNodeText(child))
    .join('')
}

const extractPlainText = (html: string) => {
  if (typeof document === 'undefined') {
    return normalizeLineBreak(html.replace(/<br\s*\/?>/gi, '\n').replace(/<[^>]+>/g, ''))
  }

  const container = document.createElement('div')
  container.innerHTML = normalizeLineBreak(html)
  return Array.from(container.childNodes)
    .map(child => collectNodeText(child))
    .join('')
}

/**
 * 将观点片段统一转换为可匹配正文的纯文本。
 * 片段字段可能夹杂 HTML 标签，需先剥离后再参与匹配，才能和正文侧口径保持一致。
 */
const normalizeHighlightSceneText = (text: string) => {
  return extractPlainText(text).trim()
}

const findAllRanges = (source: string, keyword: string) => {
  if (!source || !keyword) return [] as TextRange[]

  const ranges: TextRange[] = []
  let cursor = 0
  while (cursor < source.length) {
    const matchIndex = source.indexOf(keyword, cursor)
    if (matchIndex < 0) break
    ranges.push({
      start: matchIndex,
      end: matchIndex + keyword.length
    })
    cursor = matchIndex + keyword.length
  }
  return ranges
}

const serializeAttributes = (element: HTMLElement) =>
  Array.from(element.attributes)
    .map(attr => ` ${attr.name}="${escapeHtml(attr.value)}"`)
    .join('')

const renderTextNode = (text: string, state: RenderState) => {
  if (!text) return ''

  const nodeStart = state.cursor
  const nodeEnd = nodeStart + text.length
  let html = ''
  let localCursor = 0

  for (const range of state.ranges) {
    if (range.end <= nodeStart) continue
    if (range.start >= nodeEnd) break

    const highlightStart = Math.max(range.start - nodeStart, localCursor)
    const highlightEnd = Math.min(range.end - nodeStart, text.length)

    if (highlightEnd <= highlightStart) continue

    html += renderPlainText(text.slice(localCursor, highlightStart))
    html += `<span class="voice-topic-highlight" style="color: ${state.color}; background-color: ${state.backgroundColor}; border-radius: 2px; padding: 0 2px;">${renderPlainText(text.slice(highlightStart, highlightEnd))}</span>`
    localCursor = highlightEnd
  }

  html += renderPlainText(text.slice(localCursor))
  state.cursor = nodeEnd
  return html
}

const serializeNode = (node: Node, state: RenderState): string => {
  if (node.nodeType === TEXT_NODE) {
    return renderTextNode(normalizeLineBreak(node.textContent || ''), state)
  }

  if (node.nodeType !== ELEMENT_NODE) return ''

  const element = node as HTMLElement
  const tagName = element.tagName.toLowerCase()

  if (tagName === 'br') {
    state.cursor += 1
    return '<br>'
  }

  const attrs = serializeAttributes(element)
  const childrenHtml = Array.from(element.childNodes)
    .map(child => serializeNode(child, state))
    .join('')

  if (VOID_TAGS.has(tagName)) {
    return `<${tagName}${attrs}>`
  }

  return `<${tagName}${attrs}>${childrenHtml}</${tagName}>`
}

const renderFragment = (html: string, ranges: TextRange[], color: string) => {
  if (typeof document === 'undefined') {
    return renderPlainText(normalizeLineBreak(html))
  }

  const container = document.createElement('div')
  container.innerHTML = normalizeLineBreak(html)
  const state: RenderState = {
    cursor: 0,
    color,
    backgroundColor: resolveHighlightBackground(color),
    ranges
  }

  return Array.from(container.childNodes)
    .map(child => serializeNode(child, state))
    .join('')
}

const normalizeVoiceTopicList = <T extends VoiceTopicHighlightItem>(source?: T[] | null) => {
  if (!Array.isArray(source) || !source.length) return [] as T[]
  return source.filter(item => {
    const hasTopic = !!String(item?.topic || '').trim()
    const hasSceneText = !!String(item?.originalTexTScene || item?.originalTextScene || '').trim()
    return hasTopic || hasSceneText
  })
}

/**
 * 统一获取详情中可用的观点列表。
 * 优先使用 soundslist；未返回时再回退到其他兼容字段。
 */
export const resolveVoiceTopicList = <T extends VoiceTopicHighlightItem>(
  ...sources: Array<T[] | null | undefined>
) => {
  for (const source of sources) {
    const normalizedList = normalizeVoiceTopicList(source)
    if (normalizedList.length) return normalizedList
  }
  return [] as T[]
}

/**
 * 只有一个观点时默认高亮该观点对应的片段，其余情况保持未选中。
 */
export const getDefaultVoiceTopicIndex = (topics?: VoiceTopicHighlightItem[] | null) => {
  return Array.isArray(topics) && topics.length === 1 ? 0 : -1
}

/**
 * 根据观点情感取正文高亮色，未识别到情感时回退到统一主题色。
 */
export const getVoiceTopicColor = (
  topic?: VoiceTopicHighlightItem | null,
  fallbackColor: string = DEFAULT_TOPIC_HIGHLIGHT_COLOR
) => {
  return sentimentColors?.[topic?.sentiment as keyof typeof sentimentColors] || fallbackColor
}

/**
 * 从 soundslist 项中提取用于匹配详情正文的原声片段，兼容不同字段命名。
 * 片段内容先转纯文本，再与正文纯文本做高亮匹配。
 */
export const getVoiceTopicSceneText = (topic?: VoiceTopicHighlightItem | null) => {
  const rawSceneText = String(topic?.originalTexTScene || topic?.originalTextScene || '')
  return normalizeHighlightSceneText(rawSceneText)
}

/**
 * 生成详情原声 HTML。
 * 会保留原始 HTML 结构，并把当前观点命中的全部片段染成观点对应的文字颜色。
 */
export const buildVoiceTopicHighlightHtml = ({
  originalText,
  activeTopic
}: {
  originalText?: string | null
  activeTopic?: VoiceTopicHighlightItem | null
}) => {
  const sourceHtml = String(originalText || '')
  if (!sourceHtml) return ''

  const topicSceneText = getVoiceTopicSceneText(activeTopic)
  const color = getVoiceTopicColor(activeTopic)

  if (!topicSceneText) {
    return renderFragment(sourceHtml, [], color)
  }

  const plainText = extractPlainText(sourceHtml)
  const ranges = findAllRanges(plainText, topicSceneText)

  return renderFragment(sourceHtml, ranges, color)
}
