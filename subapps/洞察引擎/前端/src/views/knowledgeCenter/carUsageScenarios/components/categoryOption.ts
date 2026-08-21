import { carUsageScenarioEnableStatus, findCarSceneCategoryList } from '@/api/carUsageScenarios'
import type { CarUsageScenarioCategoryOption } from './types'

/**
 * 场景选择器只允许挂载到二级分类，因此这里仅提取 level=2 的节点并保留基础展示字段。
 */
const mapCarUsageScenarioCategoryOptions = (
  nodes: Api.CarUsageScenarios.CategoryNode[] = [],
  currentLevel = 1
): CarUsageScenarioCategoryOption[] => {
  return nodes.flatMap(node => {
    const categoryId = String(node.id || '')
    const categoryName = String(node.categoryName || '')
    const nodeLevel = Number(node.level || currentLevel)
    if (!categoryId || !categoryName) {
      return []
    }

    // 后端 level 从 1 开始计数，当前需求只消费二级分类，避免把更深层级误放入下拉选项。
    if (nodeLevel === 2) {
      const currentOption: CarUsageScenarioCategoryOption = {
        id: categoryId,
        label: categoryName,
        status:
          String(node.status || '') === carUsageScenarioEnableStatus.DISABLED
            ? carUsageScenarioEnableStatus.DISABLED
            : carUsageScenarioEnableStatus.ENABLED
      }

      return [currentOption]
    }

    if (nodeLevel > 2) {
      return []
    }

    return mapCarUsageScenarioCategoryOptions(node.children || [], nodeLevel + 1)
  })
}

/**
 * 场景分类接口返回树结构，这里统一在工具层完成请求与转换，弹框可直接使用结果。
 */
export const getCarUsageScenarioCategoryOptions = async (
  query: Api.CarUsageScenarios.CategoryListQuery = {}
) => {
  const response = await findCarSceneCategoryList(query)
  return mapCarUsageScenarioCategoryOptions(response.result || [])
}
