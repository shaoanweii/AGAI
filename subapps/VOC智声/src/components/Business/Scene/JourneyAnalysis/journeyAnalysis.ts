/**
 * 旅程分析工具函数
 * 用于处理标签路径的层级映射、标题计算和下钻逻辑
 */

/**
 * 标签路径项
 */
export interface TagPathItem {
  code: string
  name: string
  level?: number
}

/**
 * 查询参数（用于API调用）
 * 注意：tag1Code, tag2Code, tag3Code, tag4Code 的类型都是 string，不是 string[]
 */
export interface JourneyQueryParams {
  tag1Code?: string
  tag2Code?: string
  tag3Code?: string
  tag4Code?: string
}

/**
 * 检查第一级是否是虚拟的"全旅程"（all）
 */
export function isFirstLevelAll(tagPath: TagPathItem[]): boolean {
  if (!tagPath || tagPath.length === 0) {
    return false
  }
  const level1Tags = tagPath.filter(tag => (tag.level || 1) === 1)
  return level1Tags.length > 0 && level1Tags.every(tag => tag.code === 'all')
}

/**
 * 获取有效的标签路径（过滤掉 'all'）
 */
export function getValidTagPath(tagPath: TagPathItem[]): TagPathItem[] {
  if (!tagPath || tagPath.length === 0) {
    return []
  }
  return tagPath.filter(tag => tag.code !== 'all')
}

/**
 * 获取当前最大有效层级
 */
export function getMaxValidLevel(tagPath: TagPathItem[]): number {
  const validTags = getValidTagPath(tagPath)
  if (validTags.length === 0) return 0
  return Math.max(...validTags.map(tag => tag.level || 0))
}

/**
 * 将标签路径映射为查询参数
 * @param tagPath 完整的标签路径（包含 'all'）
 * @param isFirstLevelAll 第一级是否是 'all'
 * @param clickedTagCode 用户点击的标签 code（用于多选时确定查询哪个标签的下级）
 * @param queryTagPath 查询条件中的原始标签路径（用于多选时查找点击的标签）
 */
export function mapTagPathToQueryParams(
  tagPath: TagPathItem[],
  isFirstLevelAll: boolean,
  clickedTagCode?: string,
  queryTagPath?: TagPathItem[]
): JourneyQueryParams {
  const params: JourneyQueryParams = {}
  const validTags = getValidTagPath(tagPath)
  
  // 获取查询条件中的标签（用于多选场景）
  const queryValidTags = queryTagPath ? getValidTagPath(queryTagPath) : []

  if (isFirstLevelAll) {
    // 第一级是 'all'，后面的级别向前提一级
    // 二级 -> tag1Code，三级 -> tag2Code
    const level2Tags = validTags.filter(tag => tag.level === 2).map(tag => tag.code)
    const level3Tags = validTags.filter(tag => tag.level === 3).map(tag => tag.code)
    
    // 获取查询条件中的所有二级标签（用于多选场景）
    // 关键：直接从 queryTagPath 中获取所有 level 2 的标签 code，确保包含所有选中的标签
    const allQueryLevel2Codes: string[] = []
    if (queryTagPath && Array.isArray(queryTagPath)) {
      queryTagPath.forEach(tag => {
        if (tag.level === 2 && tag.code && tag.code !== 'all' && !allQueryLevel2Codes.includes(tag.code)) {
          allQueryLevel2Codes.push(tag.code)
        }
      })
    }
    // 也从 queryValidTags 中获取（双重保险）
    const queryLevel2Tags = queryValidTags.filter(tag => tag.level === 2).map(tag => tag.code)
    // 合并，确保包含所有查询条件中的二级标签
    const finalQueryLevel2Tags = [...new Set([...allQueryLevel2Codes, ...queryLevel2Tags])]
    
    // 调试日志
    console.log('mapTagPathToQueryParams - isFirstLevelAll:', isFirstLevelAll)
    console.log('mapTagPathToQueryParams - clickedTagCode:', clickedTagCode)
    console.log('mapTagPathToQueryParams - queryTagPath:', queryTagPath)
    console.log('mapTagPathToQueryParams - allQueryLevel2Codes:', allQueryLevel2Codes)
    console.log('mapTagPathToQueryParams - queryLevel2Tags:', queryLevel2Tags)
    console.log('mapTagPathToQueryParams - finalQueryLevel2Tags:', finalQueryLevel2Tags)
    console.log('mapTagPathToQueryParams - level2Tags:', level2Tags)

    // 如果指定了点击的标签 code，优先使用点击的标签
    if (clickedTagCode) {
      // 关键逻辑：当第一级是 'all' 时，如果点击的标签在查询条件中（多选场景），
      // 直接使用点击的标签 code 作为 tag1Code
      // 这样点击"认知"就用"认知"的 code，点击"选择"就用"选择"的 code
      
      // 检查点击的标签是否在查询条件的二级标签中
      const isClickedTagInQuery = finalQueryLevel2Tags.includes(clickedTagCode)
      console.log('mapTagPathToQueryParams - isClickedTagInQuery:', isClickedTagInQuery)
      
      // 如果点击的标签在查询条件中，直接使用它
      if (isClickedTagInQuery) {
        params.tag1Code = clickedTagCode
        console.log('mapTagPathToQueryParams - 使用点击的标签作为 tag1Code:', clickedTagCode)
        // 如果有对应的三级标签，使用它作为 tag2Code
        if (level3Tags.length > 0) {
          const matchedTag = findParentTagCode(clickedTagCode, level3Tags)
          if (matchedTag) {
            params.tag2Code = matchedTag
          }
        }
      } else if (level2Tags.includes(clickedTagCode)) {
        // 点击的标签在当前路径的二级标签中（已经下钻过的情况）
        params.tag1Code = clickedTagCode
        if (level3Tags.length > 0) {
          const matchedTag = findParentTagCode(clickedTagCode, level3Tags)
          if (matchedTag) {
            params.tag2Code = matchedTag
          }
        }
      } else {
        // 点击的标签不在查询条件的二级标签中，检查是否是三级标签
        const clickedTag = validTags.find(tag => tag.code === clickedTagCode)
        if (clickedTag) {
          const clickedLevel = clickedTag.level || 1
          if (clickedLevel === 3) {
            // 点击的是三级标签（新添加的下级标签），需要找到它的二级父级标签
            const allLevel2Tags = [...new Set([...finalQueryLevel2Tags, ...level2Tags])]
            const parentLevel2Tag = findParentTagCode(clickedTagCode, allLevel2Tags)
            if (parentLevel2Tag) {
              params.tag1Code = parentLevel2Tag
              params.tag2Code = clickedTagCode
            } else if (level2Tags.length > 0) {
              params.tag1Code = level2Tags[0]
              params.tag2Code = clickedTagCode
            }
          }
        }
      }
    }

    // 如果没有指定点击的标签，或者点击的标签不在当前路径中，使用默认逻辑
    if (!params.tag1Code && level2Tags.length > 0) {
      params.tag1Code = level2Tags[0]
    }
    if (!params.tag2Code && level3Tags.length > 0) {
      if (params.tag1Code) {
        // 根据 tag1Code 找到对应的 level3Tag（通过前缀匹配）
        const matchedTag = findParentTagCode(params.tag1Code, level3Tags)
        params.tag2Code = matchedTag || level3Tags[0]
      } else {
        params.tag2Code = level3Tags[0]
      }
    }
  } else {
    // 正常情况，直接映射
    const level1Tags = validTags.filter(tag => tag.level === 1).map(tag => tag.code)
    const level2Tags = validTags.filter(tag => tag.level === 2).map(tag => tag.code)
    const level3Tags = validTags.filter(tag => tag.level === 3).map(tag => tag.code)
    const level4Tags = validTags.filter(tag => tag.level === 4).map(tag => tag.code)

    // 如果指定了点击的标签 code，使用点击的标签；否则使用第一个
    if (level1Tags.length > 0) {
      if (clickedTagCode && level1Tags.includes(clickedTagCode)) {
        params.tag1Code = clickedTagCode
      } else {
        params.tag1Code = level1Tags[0]
      }
    }
    if (level2Tags.length > 0) {
      if (clickedTagCode && level2Tags.includes(clickedTagCode)) {
        params.tag2Code = clickedTagCode
      } else if (params.tag1Code) {
        // 根据 tag1Code 找到对应的 level2Tag（通过前缀匹配）
        const matchedTag = findParentTagCode(params.tag1Code, level2Tags) || level2Tags[0]
        params.tag2Code = matchedTag
      } else {
        params.tag2Code = level2Tags[0]
      }
    }
    if (level3Tags.length > 0) {
      if (clickedTagCode && level3Tags.includes(clickedTagCode)) {
        params.tag3Code = clickedTagCode
      } else if (params.tag2Code) {
        // 根据 tag2Code 找到对应的 level3Tag（通过前缀匹配）
        const matchedTag = findParentTagCode(params.tag2Code, level3Tags) || level3Tags[0]
        params.tag3Code = matchedTag
      } else {
        params.tag3Code = level3Tags[0]
      }
    }
    if (level4Tags.length > 0) {
      if (clickedTagCode && level4Tags.includes(clickedTagCode)) {
        params.tag4Code = clickedTagCode
      } else {
        params.tag4Code = level4Tags[0]
      }
    }
  }

  return params
}

/**
 * 计算标题列表
 * @param tagPath 完整的标签路径（包含下钻添加的标签）
 * @param queryTagPath 查询条件中的标签路径（用于确定起始层级）
 */
export function computeTitleList(
  tagPath: TagPathItem[],
  queryTagPath?: TagPathItem[]
): TagPathItem[] {
  if (!tagPath || tagPath.length === 0) {
    return []
  }

  // 确定起始层级：如果查询条件存在，使用查询条件的最大层级；否则从最小层级开始
  let startLevel = 1
  if (queryTagPath && queryTagPath.length > 0) {
    const queryMaxLevel = Math.max(...queryTagPath.map(tag => tag.level || 1))
    startLevel = queryMaxLevel
  } else {
    const minLevel = Math.min(...tagPath.map(tag => tag.level || 1))
    startLevel = minLevel
  }

  // 显示从起始层级开始的所有标签
  return tagPath.filter(tag => (tag.level || 1) >= startLevel)
}

/**
 * 检查是否可以继续下钻
 * @param tagPath 当前的标签路径
 * @param isFirstLevelAll 第一级是否是 'all'
 */
export function canDrillDown(tagPath: TagPathItem[], isFirstLevelAll: boolean): boolean {
  const maxLevel = getMaxValidLevel(tagPath)
  // 如果第一级是 'all'，全旅程只有3级，所以末级是3
  // 否则，末级是4
  const maxAllowedLevel = isFirstLevelAll ? 3 : 4
  return maxLevel < maxAllowedLevel
}

/**
 * 计算下钻后的下一个绝对层级
 * @param tagPath 当前的标签路径
 * @param isFirstLevelAll 第一级是否是 'all'
 */
export function getNextDrillDownLevel(
  tagPath: TagPathItem[],
  isFirstLevelAll: boolean
): number {
  const maxLevel = getMaxValidLevel(tagPath)

  if (isFirstLevelAll) {
    // 第一级是 'all'，全旅程只有3级
    // 检查查询条件中是否已经包含二级标签
    const hasLevel2 = tagPath.some(tag => tag.level === 2 && tag.code !== 'all')
    const hasLevel3 = tagPath.some(tag => tag.level === 3 && tag.code !== 'all')

    if (hasLevel2 && hasLevel3) {
      // 如果已经包含二级和三级，不能再下钻（已到末级）
      return 3 // 虽然返回3，但会在 canDrillDown 中被阻止
    } else if (hasLevel2) {
      // 如果只包含二级，下一个是三级（末级）
      return 3
    } else {
      // 如果都不包含，下一个是二级
      return 2
    }
  } else {
    // 正常情况，下一个层级是最大层级+1
    return maxLevel + 1
  }
}

/**
 * 通过前缀匹配找到父级标签
 * @param childCode 子标签的 code
 * @param parentCodes 父级标签的 code 数组
 */
export function findParentTagCode(
  childCode: string,
  parentCodes: string[]
): string | undefined {
  if (parentCodes.length === 0) {
    return undefined
  }

  let parentTagCode: string | undefined = undefined
  // 找到最长的匹配前缀
  for (const parentCode of parentCodes) {
    if (childCode.startsWith(parentCode)) {
      if (!parentTagCode || parentCode.length > parentTagCode.length) {
        parentTagCode = parentCode
      }
    }
  }
  return parentTagCode
}
