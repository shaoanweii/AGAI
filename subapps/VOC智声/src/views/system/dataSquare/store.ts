import { reactive } from 'vue'
import { getDataPlazaCategoryTree, getDataPlazaConditions } from '@/api/dataPlaza'
import { findAllAttributeLabelList, getTagLibClientTree, getUserChannelTree } from '@/api/common'
import type { LabelTag } from '@/api/common/index.d'
import type {
  DataPlazaCategoryItem,
  DataPlazaConditionGroup,
  DataPlazaConditionOption
} from '@/api/dataPlaza/types'

export const dataSquareStore = reactive<{
  categoryTree: DataPlazaCategoryItem[]
  channelTree: any[]
  conditionGroups: DataPlazaConditionGroup[]
  attributeTagOptions: any[]
  tagTreeMap: Record<string, LabelTag[]>
  tagTreeLoading: boolean
}>({
  categoryTree: [],
  channelTree: [],
  conditionGroups: [],
  attributeTagOptions: [],
  tagTreeMap: {},
  tagTreeLoading: false
})

/**
 * 从筛选条件中提取可用的体验代码类型。
 * @param conditionGroups 筛选条件分组
 * @returns 体验代码类型 key 列表
 */
function getTagTypeKeys(conditionGroups: DataPlazaConditionGroup[]) {
  const tagTypeGroup = (conditionGroups || []).find(item => item.key === 'tagType')
  const tagTypeDetails = (tagTypeGroup?.details || []) as DataPlazaConditionOption[]
  return tagTypeDetails.map(item => item.key).filter((key): key is string => !!key)
}

export const dataSquareActions = {
  /**
   * 获取并缓存数据广场分类树。
   * @returns 最新分类树
   */
  async updateCategoryTree() {
    try {
      const response = await getDataPlazaCategoryTree()
      dataSquareStore.categoryTree = response.result || []
      return dataSquareStore.categoryTree
    } catch (error) {
      console.error('获取数据广场分类树失败', error)
      dataSquareStore.categoryTree = []
      return []
    }
  },

  /**
   * 获取并缓存当前用户可用的数据源树。
   * @returns 最新数据源树
   */
  async updateChannelTree() {
    try {
      const response = await getUserChannelTree()
      dataSquareStore.channelTree = response.result || []
      return dataSquareStore.channelTree
    } catch (error) {
      console.error('获取数据广场数据源树失败', error)
      dataSquareStore.channelTree = []
      return []
    }
  },

  /**
   * 获取并缓存数据广场筛选条件。
   * @returns 最新筛选条件列表
   */
  async updateConditionGroups() {
    try {
      const response = await getDataPlazaConditions()
      dataSquareStore.conditionGroups = response.result || []
      return dataSquareStore.conditionGroups
    } catch (error) {
      console.error('获取数据广场筛选条件失败', error)
      dataSquareStore.conditionGroups = []
      return []
    }
  },

  /**
   * 获取并缓存属性标签选项。
   * @returns 最新属性标签列表
   */
  async updateAttributeTagOptions() {
    try {
      const response = await findAllAttributeLabelList({})
      dataSquareStore.attributeTagOptions = Array.isArray(response.result) ? response.result : []
      return dataSquareStore.attributeTagOptions
    } catch (error) {
      console.error('获取数据广场属性标签失败', error)
      dataSquareStore.attributeTagOptions = []
      return []
    }
  },

  /**
   * 按体验代码类型获取并缓存标签树。
   * @param tagType 体验代码类型
   * @returns 当前类型的标签树
   */
  async updateTagTreeByType(tagType: string) {
    if (!tagType) {
      return []
    }

    try {
      const response = await getTagLibClientTree({ tagLibType: tagType })
      const tagTree = Array.isArray(response.result) ? response.result : []
      dataSquareStore.tagTreeMap = {
        ...dataSquareStore.tagTreeMap,
        [tagType]: tagTree
      }
      return tagTree
    } catch (error) {
      console.error('获取数据广场体验代码树失败', error)
      dataSquareStore.tagTreeMap = {
        ...dataSquareStore.tagTreeMap,
        [tagType]: []
      }
      return []
    }
  },

  /**
   * 批量刷新体验代码标签树缓存。
   * @param tagTypes 指定体验代码类型；未传时从筛选条件中读取
   * @returns 最新标签树缓存
   */
  async updateTagTreeMap(tagTypes?: string[]) {
    const targetTagTypes = Array.from(
      new Set(
        (tagTypes?.length ? tagTypes : getTagTypeKeys(dataSquareStore.conditionGroups)).filter(
          Boolean
        )
      )
    )

    if (targetTagTypes.length === 0) {
      dataSquareStore.tagTreeMap = {}
      return dataSquareStore.tagTreeMap
    }

    dataSquareStore.tagTreeLoading = true
    try {
      await Promise.all(targetTagTypes.map(tagType => this.updateTagTreeByType(tagType)))
      return dataSquareStore.tagTreeMap
    } finally {
      dataSquareStore.tagTreeLoading = false
    }
  },

  /**
   * 预加载报告弹窗的静态依赖，避免每次打开弹窗重复请求。
   */
  async preloadReportDialogOptions() {
    const [, conditionGroups] = await Promise.all([
      this.updateChannelTree(),
      this.updateConditionGroups(),
      this.updateAttributeTagOptions()
    ])
    await this.updateTagTreeMap(getTagTypeKeys(conditionGroups))
  }
}
