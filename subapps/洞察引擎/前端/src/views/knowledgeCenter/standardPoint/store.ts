import { reactive } from 'vue'
import { findTagTree } from '@/api/tag'

export type TagCategoryNode = {
  id: string | number
  tagName: string
  tagCode?: string
  parentId?: string | number
  child?: TagCategoryNode[]
  [key: string]: any
}

export const standardPointStore = reactive<{
  tagCategoryTreeMap: Record<string, TagCategoryNode[]>
  loadedTagTypes: Record<string, boolean>
  loadingTagTypes: Record<string, boolean>
}>({
  tagCategoryTreeMap: {},
  loadedTagTypes: {},
  loadingTagTypes: {}
})

const normalizeTagType = (tagType?: string | null) => String(tagType ?? '').trim()

export const standardPointActions = {
  async ensureTagLibCategoryTree(tagType?: string | null) {
    const key = normalizeTagType(tagType)
    if (!key) return []

    if (standardPointStore.loadedTagTypes[key]) {
      return standardPointStore.tagCategoryTreeMap[key] || []
    }
    if (standardPointStore.loadingTagTypes[key]) {
      return standardPointStore.tagCategoryTreeMap[key] || []
    }

    standardPointStore.loadingTagTypes[key] = true
    try {
      const res: any = await findTagTree({
        tagType: key,
        tagAttribute: 'Category',
        level: 4,
        tagStatusList: ['1', '0']
      })
      const list = Array.isArray(res?.result) ? (res.result as TagCategoryNode[]) : []
      standardPointStore.tagCategoryTreeMap[key] = list
      standardPointStore.loadedTagTypes[key] = true
      return list
    } catch (e) {
      console.error('获取标签分类树失败', e)
      standardPointStore.tagCategoryTreeMap[key] = []
      standardPointStore.loadedTagTypes[key] = true
      return []
    } finally {
      standardPointStore.loadingTagTypes[key] = false
    }
  },

  async initTagLibCategoryTrees(tagTypes: Array<string | null | undefined>) {
    const keys = Array.from(new Set((tagTypes || []).map(t => normalizeTagType(t)).filter(Boolean)))
    if (!keys.length) return

    await Promise.allSettled(keys.map(k => this.ensureTagLibCategoryTree(k)))
  }
}
