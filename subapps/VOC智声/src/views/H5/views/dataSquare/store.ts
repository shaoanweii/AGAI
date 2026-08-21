import { reactive } from 'vue'
import {
  findH5AllAttributeLabelList,
  getH5DataSquareConditions,
  getH5UserChannelTree
} from '@h5/api/dataSquare'
import type {
  H5DataSquareAttributeLabelItem,
  H5DataSquareChannelNode,
  H5DataSquareConditionGroup
} from '@h5/api/dataSquare'

export const h5DataSquareStore = reactive<{
  channelTree: H5DataSquareChannelNode[]
  conditionGroups: H5DataSquareConditionGroup[]
  attributeTagOptions: H5DataSquareAttributeLabelItem[]
}>({
  channelTree: [],
  conditionGroups: [],
  attributeTagOptions: []
})

export const h5DataSquareActions = {
  /**
   * 加载 H5 筛选弹层的数据源树。
   * @returns 数据源树
   */
  async updateChannelTree() {
    try {
      const response = await getH5UserChannelTree()
      h5DataSquareStore.channelTree = response.result || []
      return h5DataSquareStore.channelTree
    } catch (error) {
      console.error('获取H5看数广场数据源树失败', error)
      h5DataSquareStore.channelTree = []
      return []
    }
  },

  /**
   * 加载 H5 报告筛选条件配置。
   * @returns 筛选条件配置
   */
  async updateConditionGroups() {
    try {
      const response = await getH5DataSquareConditions()
      h5DataSquareStore.conditionGroups = response.result || []
      return h5DataSquareStore.conditionGroups
    } catch (error) {
      console.error('获取H5看数广场筛选条件失败', error)
      h5DataSquareStore.conditionGroups = []
      return []
    }
  },

  /**
   * 加载 H5 报告筛选的属性标签选项。
   * @returns 属性标签选项
   */
  async updateAttributeTagOptions() {
    try {
      const response = await findH5AllAttributeLabelList({})
      h5DataSquareStore.attributeTagOptions = Array.isArray(response.result) ? response.result : []
      return h5DataSquareStore.attributeTagOptions
    } catch (error) {
      console.error('获取H5看数广场属性标签失败', error)
      h5DataSquareStore.attributeTagOptions = []
      return []
    }
  }
}
