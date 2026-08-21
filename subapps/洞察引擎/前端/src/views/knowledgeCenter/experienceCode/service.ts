import { findTopicOperatorListClient } from '@/api/tag'
import {
  type CategoryStatusValue,
  type ExperienceCategoryItem,
  type ExperienceCategoryTypeSummary,
  type ExperienceCodeItem,
  type ExperienceCodeOperatorApiItem,
  type ExperienceCodeOperatorOption,
  type ExperienceCodeType
} from './components/types'

interface ExperienceCodeOperatorApiResponse {
  result?: ExperienceCodeOperatorApiItem[]
}

interface ExperienceCategoryDataResult {
  categories: ExperienceCategoryItem[]
  typeSummaries: ExperienceCategoryTypeSummary[]
}

type LocalExperienceRootName = '产品' | '服务' | '用户全旅程'
type LocalExperienceTuple = [string, string, string, string, string, string?]

export type LocalExperienceDataScope = 'tagSystem' | 'userJourney'

interface LocalExperienceRecord {
  id: string
  first: LocalExperienceRootName
  second: string
  third: string
  tagName: string
  keyword: string
  createTime: string
}

const ROOT_TYPE_MAP: Record<LocalExperienceRootName, ExperienceCodeType> = {
  产品: 'PRODUCT',
  服务: 'SERVICE',
  用户全旅程: 'USER_JOURNEY'
}

const ROOT_NAME_MAP = Object.entries(ROOT_TYPE_MAP).reduce<Record<string, LocalExperienceRootName>>(
  (map, [name, code]) => {
    map[code] = name as LocalExperienceRootName
    return map
  },
  {}
)

const ROOT_ORDER: LocalExperienceRootName[] = ['产品', '服务', '用户全旅程']
const DEFAULT_OPERATOR = 'admin'
const DEFAULT_STATUS: CategoryStatusValue = '1'
const LOCAL_DATA_URL_MAP: Record<LocalExperienceDataScope, string> = {
  tagSystem: `${import.meta.env.BASE_URL}experience-code-flat-data.json`,
  userJourney: `${import.meta.env.BASE_URL}user-journey-flat-data.json`
}

const localRecordsCache = new Map<LocalExperienceDataScope, Promise<LocalExperienceRecord[]>>()
const categoryDataCache = new Map<LocalExperienceDataScope, ExperienceCategoryDataResult>()

const normalizeText = (value?: string | number | boolean | null) => String(value ?? '').trim()

/**
 * 用户旅程与标签体系复用展示组件，但必须读取各自独立的快照，避免两套分类相互串数据。
 */
const resolveDataScope = (): LocalExperienceDataScope =>
  window.location.hash.includes('/knowledgeCenter/userJourney') ? 'userJourney' : 'tagSystem'

const loadLocalExperienceRecords = (scope: LocalExperienceDataScope = resolveDataScope()) => {
  const cachedRecords = localRecordsCache.get(scope)
  if (cachedRecords) return cachedRecords

  const recordsPromise = fetch(LOCAL_DATA_URL_MAP[scope])
    .then(response => {
      if (!response.ok) {
        throw new Error(
          scope === 'userJourney' ? '用户旅程本地数据加载失败' : '标签体系本地数据加载失败'
        )
      }
      return response.json() as Promise<LocalExperienceTuple[]>
    })
    .then(rows =>
      rows.map(([first, second, third, tagName, keyword, createTime], index) => ({
        id: `${scope === 'userJourney' ? 'JOURNEY' : 'LOCAL-TAG'}-${String(index + 1).padStart(
          5,
          '0'
        )}`,
        first: first as LocalExperienceRootName,
        second: normalizeText(second),
        third: normalizeText(third),
        tagName: normalizeText(tagName),
        keyword: normalizeText(keyword),
        createTime: normalizeText(createTime) || '2026-03-11 00:00:00'
      }))
    )

  localRecordsCache.set(scope, recordsPromise)
  return recordsPromise
}

export interface AssociationTreeNode {
  id: string
  tagName: string
  child?: AssociationTreeNode[]
}

/**
 * 标准观点关联下拉复用标签体系、用户旅程页面的同一份快照数据。
 * 标签体系排除旧的“用户全旅程”根，旅程数据则直接以旅程阶段作为顶级节点。
 */
export const fetchAssociationTreeData = async (
  scope: LocalExperienceDataScope
): Promise<AssociationTreeNode[]> => {
  const allRecords = await loadLocalExperienceRecords(scope)
  const records =
    scope === 'tagSystem' ? allRecords.filter(record => record.first !== '用户全旅程') : allRecords
  const roots = new Map<string, AssociationTreeNode>()

  records.forEach(record => {
    const rootName = scope === 'userJourney' ? record.second : record.first
    const secondName = scope === 'userJourney' ? record.third : record.second
    const rootId = `${scope}::${rootName}`
    const secondId = `${rootId}::${secondName}`

    let root = roots.get(rootId)
    if (!root) {
      root = { id: rootId, tagName: rootName, child: [] }
      roots.set(rootId, root)
    }

    let second = root.child?.find(item => item.id === secondId)
    if (!second) {
      second = { id: secondId, tagName: secondName, child: [] }
      root.child?.push(second)
    }

    if (scope === 'tagSystem') {
      const thirdId = `${secondId}::${record.third}`
      let third = second.child?.find(item => item.id === thirdId)
      if (!third) {
        third = { id: thirdId, tagName: record.third, child: [] }
        second.child?.push(third)
      }
      third.child?.push({ id: record.id, tagName: record.tagName })
      return
    }

    second.child?.push({ id: record.id, tagName: record.tagName })
  })

  return Array.from(roots.values())
}

const buildCategoryId = (first: LocalExperienceRootName, second?: string, third?: string) => {
  return [ROOT_TYPE_MAP[first], second, third].filter(Boolean).join('::')
}

const buildJourneyStageType = (stage: string) => `USER_JOURNEY::${stage}`

const buildJourneyCategoryId = (stage: string, subStage: string) =>
  [buildJourneyStageType(stage), subStage].filter(Boolean).join('::')

const buildPathLabel = (...parts: string[]) => parts.filter(Boolean).join('#')

const createCategory = ({
  id,
  parentId,
  first,
  name,
  level,
  order,
  leafCount,
  pathNames
}: {
  id: string
  parentId: string
  first: LocalExperienceRootName
  name: string
  level: 1 | 2 | 3
  order: number
  leafCount: number
  pathNames: string[]
}): ExperienceCategoryItem => ({
  id,
  tagParentId: parentId,
  tagName: name,
  tagDescription: '',
  synonyms: '',
  tagStatus: DEFAULT_STATUS,
  tagType: ROOT_TYPE_MAP[first],
  tagCode: id,
  leafCount,
  hasFinalCategory: true,
  hasFinalTopic: false,
  level,
  order,
  pathNames,
  pathLabel: buildPathLabel(...pathNames)
})

const buildLocalCategoryData = (records: LocalExperienceRecord[]): ExperienceCategoryDataResult => {
  const categories: ExperienceCategoryItem[] = []
  const typeSummaries: ExperienceCategoryTypeSummary[] = []
  let order = 0

  ROOT_ORDER.forEach(first => {
    const rootRecords = records.filter(item => item.first === first)
    if (!rootRecords.length) return
    const typeCode = ROOT_TYPE_MAP[first]

    typeSummaries.push({
      nodeId: typeCode,
      typeCode,
      label: first,
      tagCode: typeCode,
      count: rootRecords.length,
      hasFinalCategory: true
    })

    const secondNames = Array.from(new Set(rootRecords.map(item => item.second)))
    secondNames.forEach(second => {
      const secondRecords = rootRecords.filter(item => item.second === second)
      const secondId = buildCategoryId(first, second)
      order += 1
      categories.push(
        createCategory({
          id: secondId,
          parentId: '',
          first,
          name: second,
          level: 1,
          order,
          leafCount: secondRecords.length,
          pathNames: [second]
        })
      )

      const thirdNames = Array.from(new Set(secondRecords.map(item => item.third)))
      thirdNames.forEach(third => {
        const thirdRecords = secondRecords.filter(item => item.third === third)
        order += 1
        categories.push(
          createCategory({
            id: buildCategoryId(first, second, third),
            parentId: secondId,
            first,
            name: third,
            level: 2,
            order,
            leafCount: thirdRecords.length,
            pathNames: [second, third]
          })
        )
      })
    })
  })

  return {
    categories,
    typeSummaries
  }
}

/**
 * 用户旅程不再展示“用户全旅程”这一中间层，五个阶段直接作为左侧树的顶级分类。
 */
const buildUserJourneyCategoryData = (
  records: LocalExperienceRecord[]
): ExperienceCategoryDataResult => {
  const categories: ExperienceCategoryItem[] = []
  const typeSummaries: ExperienceCategoryTypeSummary[] = []
  let order = 0

  Array.from(new Set(records.map(item => item.second))).forEach(stage => {
    const stageRecords = records.filter(item => item.second === stage)
    const typeCode = buildJourneyStageType(stage)

    typeSummaries.push({
      nodeId: typeCode,
      typeCode,
      label: stage,
      tagCode: typeCode,
      count: stageRecords.length,
      hasFinalCategory: true
    })

    Array.from(new Set(stageRecords.map(item => item.third))).forEach(subStage => {
      const subStageRecords = stageRecords.filter(item => item.third === subStage)
      order += 1
      categories.push({
        id: buildJourneyCategoryId(stage, subStage),
        tagParentId: '',
        tagName: subStage,
        tagDescription: '',
        synonyms: '',
        tagStatus: DEFAULT_STATUS,
        tagType: typeCode,
        tagCode: buildJourneyCategoryId(stage, subStage),
        leafCount: subStageRecords.length,
        hasFinalCategory: true,
        hasFinalTopic: false,
        level: 1,
        order,
        pathNames: [stage, subStage],
        pathLabel: buildPathLabel(stage, subStage)
      })
    })
  })

  return { categories, typeSummaries }
}

const loadFullCategoryData = async () => {
  const scope = resolveDataScope()
  const cachedData = categoryDataCache.get(scope)
  if (cachedData) return cachedData
  const records = await loadLocalExperienceRecords()
  const categoryData =
    scope === 'userJourney'
      ? buildUserJourneyCategoryData(records)
      : buildLocalCategoryData(records)
  categoryDataCache.set(scope, categoryData)
  return categoryData
}

const filterCategoryData = (
  fullData: ExperienceCategoryDataResult,
  keyword: string
): ExperienceCategoryDataResult => {
  const normalizedKeyword = keyword.trim()
  if (!normalizedKeyword) return fullData

  const matchedIds = new Set<string>()
  fullData.categories.forEach(item => {
    const matched =
      item.tagName.includes(normalizedKeyword) || item.pathLabel.includes(normalizedKeyword)
    if (!matched) return

    matchedIds.add(item.id)
    if (item.tagParentId) {
      matchedIds.add(item.tagParentId)
    }
  })

  const categories = fullData.categories.filter(item => matchedIds.has(item.id))
  const visibleTypeCodes = new Set(categories.map(item => item.tagType))
  const typeSummaries = fullData.typeSummaries.filter(item => visibleTypeCodes.has(item.typeCode))

  return {
    categories,
    typeSummaries
  }
}

const resolveRecordByTarget = (record: LocalExperienceRecord, query: ExperienceCodeListQuery) => {
  if (resolveDataScope() === 'userJourney') {
    const stageType = buildJourneyStageType(record.second)
    if (query.tagType && query.tagType !== stageType) return false

    const tagParentId = normalizeText(query.tagParentId)
    return (
      !tagParentId ||
      tagParentId === stageType ||
      tagParentId === buildJourneyCategoryId(record.second, record.third)
    )
  }

  const rootName = query.tagType ? ROOT_NAME_MAP[query.tagType] : undefined
  if (rootName && record.first !== rootName) return false

  const tagParentId = normalizeText(query.tagParentId)
  if (!tagParentId) return true

  return (
    tagParentId === ROOT_TYPE_MAP[record.first] ||
    tagParentId === buildCategoryId(record.first, record.second) ||
    tagParentId === buildCategoryId(record.first, record.second, record.third)
  )
}

const compareText = (left: string, right: string, direction: 'asc' | 'desc') => {
  const result = left.localeCompare(right, 'zh-Hans-CN')
  return direction === 'asc' ? result : -result
}

const applyLocalSort = (list: ExperienceCodeItem[], order?: string) => {
  const [field, directionValue] = normalizeText(order).split(/\s+/)
  const direction = directionValue === 'desc' ? 'desc' : 'asc'

  if (!field) return list

  return [...list].sort((a, b) =>
    compareText(
      normalizeText(a[field as keyof ExperienceCodeItem]),
      normalizeText(b[field as keyof ExperienceCodeItem]),
      direction
    )
  )
}

/**
 * 获取完整分类数据。当前标签体系页按 Excel 快照展示产品/服务分类树。
 */
export const fetchExperienceCategoryData = async (options: { force?: boolean } = {}) => {
  if (options.force) {
    categoryDataCache.delete(resolveDataScope())
  }
  return loadFullCategoryData()
}

/**
 * 获取左侧分类列表数据。
 */
export const fetchExperienceCategoryListData = async (
  keyword = '',
  options: { force?: boolean } = {}
) => {
  const fullData = await fetchExperienceCategoryData(options)
  return filterCategoryData(fullData, keyword)
}

/**
 * 获取体验代码操作人筛选项，查询时透传 id，展示层使用 userName。
 */
export const fetchExperienceCodeOperatorOptions = async (): Promise<
  ExperienceCodeOperatorOption[]
> => {
  const response = (await findTopicOperatorListClient({})) as ExperienceCodeOperatorApiResponse
  const operatorList = Array.isArray(response.result) ? response.result : []

  const remoteOptions = operatorList
    .map(item => ({
      id: normalizeText(item.id),
      userName: normalizeText(item.userName)
    }))
    .filter(item => item.id && item.userName)

  return remoteOptions.length
    ? remoteOptions
    : [
        {
          id: DEFAULT_OPERATOR,
          userName: DEFAULT_OPERATOR
        }
      ]
}

/**
 * 体验代码列表查询参数。
 */
export interface ExperienceCodeListQuery {
  tagParentId?: string
  pageNum?: number
  pageSize?: number
  tagStatus?: string
  operateUser?: string
  tagName?: string
  tagType?: ExperienceCodeType
  order?: string
}

/**
 * 体验代码分页结果。
 */
export interface ExperienceCodePageResult {
  list: ExperienceCodeItem[]
  total: number
}

/**
 * 末级标签列表直接来自 Excel 快照，分页、筛选和排序在本页完成。
 */
export const fetchExperienceCodeListData = async (
  query: ExperienceCodeListQuery = {}
): Promise<ExperienceCodePageResult> => {
  const scope = resolveDataScope()
  const records = await loadLocalExperienceRecords()
  const keyword = normalizeText(query.tagName)
  const status = normalizeText(query.tagStatus)
  const operator = normalizeText(query.operateUser)
  const pageNum = Math.max(Number(query.pageNum || 1), 1)
  const pageSize = Math.max(Number(query.pageSize || 10), 1)

  const filteredRecords = records.filter(record => {
    if (!resolveRecordByTarget(record, query)) return false
    if (status && status !== DEFAULT_STATUS) return false
    if (operator && operator !== DEFAULT_OPERATOR) return false
    if (keyword && !record.tagName.includes(keyword) && !record.keyword.includes(keyword)) {
      return false
    }
    return true
  })

  const mappedList = filteredRecords.map<ExperienceCodeItem>(record => ({
    id: record.id,
    tagParentId:
      scope === 'userJourney'
        ? buildJourneyCategoryId(record.second, record.third)
        : buildCategoryId(record.first, record.second, record.third),
    tagName: record.tagName,
    tagCode: record.id,
    tagType:
      scope === 'userJourney' ? buildJourneyStageType(record.second) : ROOT_TYPE_MAP[record.first],
    tagTypeName: scope === 'userJourney' ? record.second : record.first,
    tagStatus: DEFAULT_STATUS,
    tagStatusText: '启用',
    tagDescription: record.keyword,
    synonyms: record.keyword,
    tagLibNameHierarchical:
      scope === 'userJourney'
        ? buildPathLabel(record.second, record.third)
        : buildPathLabel(record.first, record.second, record.third),
    createTime: record.createTime,
    updateTime: record.createTime,
    operateUser: DEFAULT_OPERATOR,
    hasFinalTopic: false
  }))

  const sortedList = applyLocalSort(mappedList, query.order)
  const start = (pageNum - 1) * pageSize

  return {
    list: sortedList.slice(start, start + pageSize),
    total: sortedList.length
  }
}
