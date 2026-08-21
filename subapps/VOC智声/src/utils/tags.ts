import type { LabelTag } from '@/api/common/index.d'
import { TagType } from '@/constants'

/**
 * @description: 给标签包装一层父级
 * @param {*} type
 * @param {*} child
 * @return {*}
 */
export const getAllByType = (type: TagType, routerName: string, child: LabelTag[]) => {
  // const tagNameMap: any = {
  //   [TagType.UserJourney]: '全旅程',
  //   [TagType.Domain]: '全服务',
  //   [TagType.CPT]: '全产品'
  // }
  const tagNameMap: any = {
    journeyAnalysis: '全旅程',
    serviceAnalysis: '全服务',
    productAnalysis: '全产品'
  }

  return [
    {
      tagName: tagNameMap[routerName],
      tagCode: 'all',
      child: child
    }
  ]
}
