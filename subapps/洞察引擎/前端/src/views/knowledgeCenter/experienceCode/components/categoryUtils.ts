import type { ExperienceCategoryItem, ExperienceCodeType, OptionItem } from './types'

/**
 * 基于分类列表建立 id 索引，避免同一轮计算里反复遍历原数组。
 */
const createCategoryMap = (categoryList: ExperienceCategoryItem[]) => {
  return new Map(categoryList.map(item => [item.id, item]))
}

/**
 * 判断分类是否为末级节点，体验代码新增与批量移动都只能挂到末级分类。
 */
export const isLeafCategory = (categoryId: string, categoryList: ExperienceCategoryItem[]) => {
  return !categoryList.some(item => item.tagParentId === categoryId)
}

/**
 * 构造分类完整路径，避免同名末级分类在下拉里无法区分。
 */
export const buildCategoryPathLabel = (
  categoryId: string,
  categoryList: ExperienceCategoryItem[]
) => {
  const categoryMap = createCategoryMap(categoryList)
  const path: string[] = []
  let current = categoryMap.get(categoryId)

  while (current) {
    path.unshift(current.tagName)
    current = current.tagParentId ? categoryMap.get(current.tagParentId) : undefined
  }

  return path.join('#')
}

/**
 * 统一生成某个类型下的末级分类选项，保持各弹框的筛选口径一致。
 */
export const resolveLeafCategoryOptions = (
  categoryList: ExperienceCategoryItem[],
  typeCode: ExperienceCodeType
): OptionItem[] => {
  return categoryList
    .filter(item => item.tagType === typeCode && isLeafCategory(item.id, categoryList))
    .map(item => ({
      label: buildCategoryPathLabel(item.id, categoryList),
      value: item.id
    }))
}
