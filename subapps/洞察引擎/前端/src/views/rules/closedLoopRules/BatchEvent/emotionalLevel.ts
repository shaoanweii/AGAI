import type { BatchCascaderOption } from './types'

const EMOTIONAL_LEVEL_VALUE_SEPARATOR = '::'

/**
 * 情感程度二级节点的 value 不能直接复用“高/低/一般”，
 * 否则不同父级下会出现重复值，导致 parent_value 回算命中错误分组。
 * @param value 当前节点值
 * @param parentValue 父级值
 * @returns string
 */
export const buildEmotionalLevelNodeValue = (value: unknown, parentValue = '') => {
  const normalizedValue = String(value || '').trim()
  const normalizedParentValue = String(parentValue || '').trim()

  if (!normalizedParentValue) {
    return normalizedValue
  }

  return `${normalizedParentValue}${EMOTIONAL_LEVEL_VALUE_SEPARATOR}${normalizedValue}`
}

/**
 * 将页面内部的唯一节点值还原为“父级 + 当前值”，供提交和回显复用。
 * 顶级节点不带父级时直接返回空父级。
 * @param nodeValue 页面内保存的节点值
 * @returns {{ parentValue: string; value: string }}
 */
export const parseEmotionalLevelNodeValue = (nodeValue: unknown) => {
  const normalizedValue = String(nodeValue || '').trim()
  const separatorIndex = normalizedValue.indexOf(EMOTIONAL_LEVEL_VALUE_SEPARATOR)

  if (separatorIndex === -1) {
    return {
      parentValue: '',
      value: normalizedValue
    }
  }

  return {
    parentValue: normalizedValue.slice(0, separatorIndex),
    value: normalizedValue.slice(separatorIndex + EMOTIONAL_LEVEL_VALUE_SEPARATOR.length)
  }
}

/**
 * 情感程度树在前端统一映射为“展示文案不变、节点值唯一”的级联结构。
 * 这样既不影响用户选择体验，又能保证保存时准确区分“正面-高”和“负面-高”。
 * @param nodes 后端返回的树形字典
 * @param parentValue 当前父级值
 * @returns BatchCascaderOption[]
 */
export const mapEmotionalLevelTreeOptions = (
  nodes: any[] = [],
  parentValue = ''
): BatchCascaderOption[] => {
  return (nodes || []).map(item => {
    const currentValue = String(item?.key || item?.value || '').trim()

    return {
      label: String(item?.value || ''),
      value: buildEmotionalLevelNodeValue(currentValue, parentValue),
      children: Array.isArray(item?.children)
        ? mapEmotionalLevelTreeOptions(item.children, currentValue)
        : undefined
    }
  })
}
