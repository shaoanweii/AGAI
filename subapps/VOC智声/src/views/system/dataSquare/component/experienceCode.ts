import type { LabelTag } from '@/api/common/index.d'
import type { DataPlazaReportDefaultCondition } from '@/api/dataPlaza/types'

export type ExperienceCodeTreeNode = LabelTag
export type ExperienceCodeValue = DataPlazaReportDefaultCondition['experienceCode']

/**
 * 创建空的体验代码路径值，避免页面散落手写初始结构。
 * @returns 空的体验代码路径数组
 */
export function createEmptyExperienceCodeValue(): string[][] {
  return []
}

/**
 * 统一清洗路径数组，确保每条路径都按层级顺序、去空值、去重后参与后续计算。
 * @param value 原始路径值
 * @returns 清洗后的路径数组
 */
export function normalizeExperienceCodeValue(value: unknown): ExperienceCodeValue {
  if (!Array.isArray(value) || value.length === 0) {
    return []
  }

  const uniquePathKeySet = new Set<string>()
  const normalizedPaths: string[][] = []

  value.forEach(item => {
    if (!Array.isArray(item)) {
      return
    }

    const normalizedPath = item
      .filter((code): code is string => typeof code === 'string' && !!code)
      .slice(0, 4)

    if (normalizedPath.length === 0) {
      return
    }

    const pathKey = normalizedPath.join('>')
    if (uniquePathKeySet.has(pathKey)) {
      return
    }

    uniquePathKeySet.add(pathKey)
    normalizedPaths.push(normalizedPath)
  })

  return normalizedPaths
}

/**
 * 生成体验代码路径唯一键，用于比较选择变化。
 * @param path 体验代码路径
 * @returns 路径唯一键
 */
function getExperiencePathKey(path: string[]) {
  return JSON.stringify(path)
}

/**
 * 按本次新增路径所在层级过滤体验代码，保证交互新增时只保留同一层级。
 * @param value 当前选择路径
 * @param previousValue 上一次选择路径
 * @returns 同层级体验代码路径
 */
export function normalizeSameLevelExperienceCodeValue(
  value: unknown,
  previousValue?: unknown
): ExperienceCodeValue {
  const nextPaths = normalizeExperienceCodeValue(value)
  if (nextPaths.length <= 1) {
    return nextPaths
  }

  const previousPathKeySet = new Set(
    normalizeExperienceCodeValue(previousValue).map(path => getExperiencePathKey(path))
  )
  const addedPath = nextPaths.find(path => !previousPathKeySet.has(getExperiencePathKey(path)))
  if (!addedPath) {
    return nextPaths
  }

  return nextPaths.filter(path => path.length === addedPath.length)
}

/**
 * 获取当前选择中的末级 code，用于联动标准观点查询。
 * 每条路径只取最后一个节点，再整体去重。
 * @param value 体验代码路径数组
 * @returns 末级 code 数组
 */
export function getExperienceLastLevelCodes(value?: unknown) {
  const paths = normalizeExperienceCodeValue(value)
  if (paths.length === 0) {
    return []
  }

  return Array.from(
    new Set(
      paths
        .map(path => path[path.length - 1] || '')
        .filter((code): code is string => typeof code === 'string' && !!code)
    )
  )
}
