<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getTagLibStandardView } from '@/api/rules'
import { updateNewWord } from '@/api/newWords'
import { createOpinionSynonym } from '@/api/opinionSynonyms'
import request from '@/api/index'
import { DATA_KNOWLEDGE_API_KEY_HEADER } from '@/api/constants'
import { getLoginUserMetaOrNull } from '@/utils/loginUserMeta'
import { NEW_WORD_DETAILS_DIALOG_TAB, type NewWordDetailsDialogTab } from './constants'
import {
  findAllUpTagLibHierarchicalByTagId,
  getTagLibClientTree,
  insAllDictItems
} from '@/api/main'

defineOptions({
  name: 'NewWordDetailsDialog'
})

const props = defineProps<{
  visible: boolean
  row: any
  timeRange?: string[] // 可选：如果需要，从父组件传递时间范围
  opinionType?: 0 | 1 // 可选：由父组件明确指定语料类型（0 问卷 / 1 文本）
}>()

const emit = defineEmits(['update:visible', 'refresh'])

const dialogVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val)
})

const activeTab = ref<NewWordDetailsDialogTab>(NEW_WORD_DETAILS_DIALOG_TAB.OPERATION)
const defaultActiveTab = ref<NewWordDetailsDialogTab>(NEW_WORD_DETAILS_DIALOG_TAB.OPERATION)

const setDefaultActiveTab = (tab: NewWordDetailsDialogTab) => {
  defaultActiveTab.value = tab
}

const resolveOpinionType = (raw: any): 0 | 1 => {
  if (raw === 0 || raw === 1) return raw
  const rowType = props.row?.opinion_type
  return Number(rowType) === 0 ? 0 : 1
}

const resolvedOpinionType = computed<0 | 1>(() => resolveOpinionType(props.opinionType))

const isSurveyMode = computed(() => resolvedOpinionType.value === 0)

const statusCode = computed(() => {
  const rawStatus =
    props.row?.status ??
    props.row?.enable_status ??
    props.row?.auditStatusCode ??
    props.row?.auditStatus
  const numeric = Number(rawStatus)
  if (Number.isNaN(numeric)) return null
  return numeric
})

const isReadOnly = computed(() => statusCode.value === 1 || statusCode.value === 0)

const sentimentOptions = ref<{ label: string; value: string }[]>([])

// --- 标签页 1: 操作 ---
const operationForm = reactive({
  action: 'add' as 'add' | 'discard',
  mappingOpinion: '',
  experienceCodePath: [] as string[],
  sentiment: ''
})

const isAddAction = computed(() => operationForm.action === 'add')
const skipNextMappingOpinionWatch = ref(false)

interface MappingOpinionOption {
  tagName: string
  tagCode: string
  [key: string]: any
}

const originalMappingOpinionOptions = ref<MappingOpinionOption[]>([])
const mappingOpinionOptions = ref<MappingOpinionOption[]>([])
const mappingOpinionLoading = ref(false)
const lastMappingOpinionParentId = ref<string>('')
const mappingOpinionFetchSeq = ref(0)
const mappingOpinionOptionsCache = new Map<string, MappingOpinionOption[]>()

interface ExperienceCodeNode {
  tagName: string
  tagCode: string
  child: ExperienceCodeNode[]
  [key: string]: any
}

interface ExperienceCodeDisplayNode extends ExperienceCodeNode {
  rootTagCode: string
}

interface UpHierarchicalItem {
  firstCode?: string
  secondCode?: string
  thirdCode?: string
  fourthCode?: string
  fifthCode?: string
  sixthCode?: string
  fifthEmotion?: string
  [key: string]: any
}

function normalizeExperienceCodeTree(raw: any): ExperienceCodeNode[] {
  if (!Array.isArray(raw)) return []
  return raw
    .map(normalizeExperienceCodeNode)
    .filter((node): node is ExperienceCodeNode => !!node && !!node.tagCode)
}

function normalizeExperienceCodeNode(raw: any): ExperienceCodeNode | null {
  if (!raw) return null
  const tagName = raw.tagName ?? raw.label ?? raw.name ?? ''
  const tagCode = raw.tagCode ?? raw.value ?? raw.code ?? raw.id ?? ''
  const childRaw = raw.child ?? raw.children ?? []
  const child = normalizeExperienceCodeTree(childRaw)
  return {
    ...raw,
    tagName: String(tagName),
    tagCode: String(tagCode),
    child
  }
}

const normalizeExperienceCodeNodeId = (raw: any): string => {
  if (!raw || typeof raw !== 'object') return ''
  const id = raw.id ?? raw.tagId ?? raw.tag_id ?? raw.valueId ?? ''
  return hasValidExperienceCodeValue(id) ? String(id) : ''
}

const findExperienceCodeNodeByCode = (
  nodes: ExperienceCodeNode[],
  code: string
): ExperienceCodeNode | null => {
  if (!code || !nodes.length) return null
  for (const node of nodes) {
    if (String(node.tagCode) === code) return node
    if (node.child.length) {
      const found = findExperienceCodeNodeByCode(node.child, code)
      if (found) return found
    }
  }
  return null
}

const resolveExperienceCodeParentId = (path: string[]): string => {
  if (!Array.isArray(path) || path.length === 0) return ''
  const selectedCode = String(path[path.length - 1] || '')
  if (!selectedCode) return ''
  const matched = findExperienceCodeNodeByCode(experienceCodeOptions.value, selectedCode)
  return normalizeExperienceCodeNodeId(matched)
}

const experienceCodeOptions = ref<ExperienceCodeNode[]>([])
const experienceCodeLoading = ref(false)
const experienceCodeEchoLoading = ref(false)
const experienceCodeEchoSeq = ref(0)
const inFlightExperienceCodeEchoTagId = ref<string | null>(null)
const lastExperienceCodeEchoTagId = ref<string | null>(null)
const lastExperienceCodeEchoEmotion = ref<string | null>(null)
const pendingSentimentEcho = ref<string | null>(null)

const buildPriorityOptions = (
  source: MappingOpinionOption[],
  recommendList: any[]
): MappingOpinionOption[] => {
  if (!Array.isArray(source) || !source.length) return []
  if (!Array.isArray(recommendList) || !recommendList.length) return source
  const matchedKeys = new Set<string>()
  const prioritized: MappingOpinionOption[] = []

  const getKey = (option?: MappingOpinionOption) => {
    if (!option) return ''
    return String(option.tagCode ?? option.tagName ?? '')
  }

  const pushIfNew = (option?: MappingOpinionOption) => {
    if (!option) return
    const key = getKey(option)
    if (!key || matchedKeys.has(key)) return
    matchedKeys.add(key)
    prioritized.push(option)
  }

  recommendList.forEach(topic => {
    if (!topic) return
    const topicId = topic.topic_id
    const topicText = topic.topic_text
    if (topicId) {
      const matchedById = source.find(item => String(item.tagCode) === String(topicId))
      if (matchedById) {
        pushIfNew(matchedById)
        return
      }
    }
    if (topicText) {
      const matchedByText = source.find(item => item.tagName === topicText)
      pushIfNew(matchedByText)
    }
  })

  const rest = source.filter(item => !matchedKeys.has(getKey(item)))
  return [...prioritized, ...rest]
}

const updateMappingOpinionOptions = () => {
  const recommendList = Array.isArray(props.row?.recommend_topic_list)
    ? props.row?.recommend_topic_list
    : []
  mappingOpinionOptions.value = buildPriorityOptions(
    originalMappingOpinionOptions.value,
    recommendList
  )
  ensureMappingOpinionValid()
}

const ensureMappingOpinionValid = () => {
  if (!operationForm.mappingOpinion) return
  if (!mappingOpinionOptions.value.length) return
  const exists = mappingOpinionOptions.value.some(
    item => item.tagName === operationForm.mappingOpinion
  )
  if (!exists) {
    operationForm.mappingOpinion = ''
  }
}

const fetchMappingOpinions = async (tagParentId = '') => {
  if (
    tagParentId === lastMappingOpinionParentId.value &&
    originalMappingOpinionOptions.value.length &&
    !mappingOpinionLoading.value
  ) {
    updateMappingOpinionOptions()
    return
  }

  if (mappingOpinionOptionsCache.has(tagParentId)) {
    // 视图已切换到其它 parentId 时，直接用缓存恢复并使之前的请求失效，避免返回后覆盖当前视图。
    mappingOpinionFetchSeq.value += 1
    mappingOpinionLoading.value = false
    originalMappingOpinionOptions.value = mappingOpinionOptionsCache.get(tagParentId) || []
    lastMappingOpinionParentId.value = tagParentId
    updateMappingOpinionOptions()
    return
  }

  const seq = (mappingOpinionFetchSeq.value += 1)
  mappingOpinionLoading.value = true
  try {
    const resp = await getTagLibStandardView({
      tagType: 'CA',
      tagParentId
    })
    if (seq !== mappingOpinionFetchSeq.value) return
    const list = Array.isArray(resp?.result) ? resp.result : []
    const normalized = list.filter((item: any) => item && (item.tagName || item.tagCode))
    originalMappingOpinionOptions.value = normalized
    mappingOpinionOptionsCache.set(tagParentId, normalized)
    lastMappingOpinionParentId.value = tagParentId
    updateMappingOpinionOptions()
  } catch (error) {
    if (seq !== mappingOpinionFetchSeq.value) return
    originalMappingOpinionOptions.value = []
    mappingOpinionOptions.value = []
    lastMappingOpinionParentId.value = tagParentId
    mappingOpinionOptionsCache.set(tagParentId, [])
  } finally {
    if (seq === mappingOpinionFetchSeq.value) {
      mappingOpinionLoading.value = false
    }
  }
}

const fetchExperienceCodes = async () => {
  experienceCodeLoading.value = true
  try {
    const res = await getTagLibClientTree({ level: '4', tagAttribute: 'Category', tagType: 'CA' })
    const list = normalizeExperienceCodeTree(res?.result)
    experienceCodeOptions.value = list
  } catch (error) {
    experienceCodeOptions.value = []
  } finally {
    experienceCodeLoading.value = false
  }
}

const experienceCodeLevelLabels = ['一级', '二级', '三级', '四级', '五级', '六级']

const getExperienceCodeLevelLabel = (levelIndex: number) =>
  experienceCodeLevelLabels[levelIndex] || `第${levelIndex + 1}级`

const experienceCodeDisplayLevelOffset = 1

const calcExperienceCodeMaxDepth = (nodes: ExperienceCodeNode[]): number => {
  if (!nodes.length) return 0
  let maxDepth = 1
  nodes.forEach(node => {
    if (node.child.length) {
      const depth = 1 + calcExperienceCodeMaxDepth(node.child)
      if (depth > maxDepth) {
        maxDepth = depth
      }
    }
  })
  return maxDepth
}

const experienceCodeLevelCount = computed(() => {
  const depth = calcExperienceCodeMaxDepth(experienceCodeOptions.value)
  const displayDepth = depth - experienceCodeDisplayLevelOffset
  return displayDepth > 0 ? displayDepth : 1
})

const experienceCodeDisplayRoots = computed<ExperienceCodeDisplayNode[]>(() => {
  const roots = experienceCodeOptions.value
  if (!roots.length) return []
  const flattened: ExperienceCodeDisplayNode[] = []
  roots.forEach(root => {
    if (!root.child.length) return
    root.child.forEach(child => {
      flattened.push({ ...child, rootTagCode: root.tagCode })
    })
  })
  return flattened
})

const experienceCodeDisplayPath = computed(() =>
  operationForm.experienceCodePath.slice(experienceCodeDisplayLevelOffset)
)

const experienceCodeLevelOptions = computed<ExperienceCodeNode[][]>(() => {
  const levels: ExperienceCodeNode[][] = []
  let currentOptions: ExperienceCodeNode[] = experienceCodeDisplayRoots.value
  const selectedPath = experienceCodeDisplayPath.value
  const levelCount = experienceCodeLevelCount.value

  for (let i = 0; i < levelCount; i += 1) {
    levels.push(currentOptions)
    const selectedCode = selectedPath[i]
    const selectedNode = currentOptions.find(option => option.tagCode === selectedCode)
    currentOptions = selectedNode ? selectedNode.child : []
  }

  return levels
})

const hasValidExperienceCodeValue = (value: any): value is string =>
  value !== undefined && value !== null && value !== ''

const handleExperienceCodeUserChange = (nextPath: string[]) => {
  operationForm.mappingOpinion = ''
  operationForm.sentiment = ''
  // 用户手动调整体验代码后，旧的回显请求结果已经失效，需要直接作废。
  experienceCodeEchoSeq.value += 1
  inFlightExperienceCodeEchoTagId.value = null
  experienceCodeEchoLoading.value = false
  pendingSentimentEcho.value = null
  lastExperienceCodeEchoTagId.value = null
  lastExperienceCodeEchoEmotion.value = null

  const tagParentId = resolveExperienceCodeParentId(nextPath)
  void fetchMappingOpinions(tagParentId)
}

const upHierarchicalCodeKeys = [
  'firstCode',
  'secondCode',
  'thirdCode',
  'fourthCode',
  'fifthCode',
  'sixthCode'
] as const

const normalizeUpHierarchicalCodes = (raw: any): string[] => {
  if (!raw || typeof raw !== 'object') return []
  return upHierarchicalCodeKeys
    .map(key => (raw as UpHierarchicalItem)[key])
    .filter(hasValidExperienceCodeValue)
    .map(code => String(code))
}

const normalizeUpHierarchicalEmotion = (raw: any): string | null => {
  if (!raw || typeof raw !== 'object') return null
  const emotion =
    (raw as UpHierarchicalItem).fifthEmotion ?? (raw as any).fifth_emotion ?? (raw as any).emotion
  if (!hasValidExperienceCodeValue(emotion)) return null
  return String(emotion)
}

const applySentimentEchoIfReady = () => {
  const emotionText = pendingSentimentEcho.value
  if (!emotionText) return
  const options = sentimentOptions.value
  if (!options.length) return
  const matchedByValue = options.find(option => option.value === emotionText)
  if (matchedByValue) {
    operationForm.sentiment = matchedByValue.value
    pendingSentimentEcho.value = null
    return
  }
  const matchedByLabel = options.find(option => option.label === emotionText)
  if (matchedByLabel) {
    operationForm.sentiment = matchedByLabel.value
    pendingSentimentEcho.value = null
    return
  }
  operationForm.sentiment = emotionText
  pendingSentimentEcho.value = null
}

const reconcileExperienceCodePathWithTree = (codes: string[]) => {
  if (!codes.length) return []
  const roots = experienceCodeOptions.value
  if (!roots.length) return codes
  const rootCodes = roots.map(item => String(item.tagCode))
  if (rootCodes.includes(codes[0])) return codes

  const matchedRoot = roots.find(root => {
    if (!Array.isArray(root.child) || root.child.length === 0) return false
    return root.child.some(child => String(child.tagCode) === codes[0])
  })
  if (!matchedRoot) return codes
  return [String(matchedRoot.tagCode), ...codes]
}

const reconcileExperienceCodePathIfNeeded = () => {
  const currentPath = operationForm.experienceCodePath
  if (!currentPath.length) return
  const nextPath = reconcileExperienceCodePathWithTree(currentPath)
  if (nextPath.join('|') !== currentPath.join('|')) {
    operationForm.experienceCodePath = nextPath
  }
}

const normalizeMappingOpinionTagId = (row: any): string | null => {
  const rawCandidate =
    row?.selected_topic_id ??
    row?.selectedTopicId ??
    row?.standard_opinion_id ??
    row?.standardOpinionId ??
    row?.topic_id ??
    row?.topicId

  const candidate = hasValidExperienceCodeValue(rawCandidate) ? String(rawCandidate) : ''

  const getOptionId = (option?: MappingOpinionOption) => {
    if (!option) return ''
    return String((option as any).id ?? (option as any).tagId ?? (option as any).tag_id ?? '')
  }

  if (candidate) {
    const matchedById = mappingOpinionOptions.value.find(item => getOptionId(item) === candidate)
    if (matchedById) {
      const optionId = getOptionId(matchedById)
      if (optionId) return optionId
    }

    const matchedByCode = mappingOpinionOptions.value.find(
      item => String(item.tagCode) === candidate
    )
    if (matchedByCode) {
      const optionId = getOptionId(matchedByCode)
      if (optionId) return optionId
    }

    // 如果候选值不像体验代码那样有明确层级约定，则保持原值（用于兼容后端直接给ID）
    return candidate
  }

  const text = operationForm.mappingOpinion
  if (!text) return null
  const matched = mappingOpinionOptions.value.find(item => item.tagName === text)
  const optionId = getOptionId(matched)
  if (!optionId) return null
  return optionId
}

const echoExperienceCodeByMappingOpinion = async () => {
  if (!props.visible) return
  if (isReadOnly.value || !isAddAction.value) return
  if (!operationForm.mappingOpinion) return

  const tagId = normalizeMappingOpinionTagId(props.row)
  if (!tagId) return

  if (lastExperienceCodeEchoTagId.value === tagId) {
    reconcileExperienceCodePathIfNeeded()
    if (lastExperienceCodeEchoEmotion.value) {
      pendingSentimentEcho.value = lastExperienceCodeEchoEmotion.value
      applySentimentEchoIfReady()
    } else {
      operationForm.sentiment = ''
    }
    return
  }

  // 同一映射观点的体验代码回显请求如果仍在进行中，则直接复用当前过程，避免重复请求后端。
  if (experienceCodeEchoLoading.value && inFlightExperienceCodeEchoTagId.value === tagId) return

  const seq = (experienceCodeEchoSeq.value += 1)
  inFlightExperienceCodeEchoTagId.value = tagId
  experienceCodeEchoLoading.value = true
  try {
    const res = await findAllUpTagLibHierarchicalByTagId({ ids: [tagId] })
    if (seq !== experienceCodeEchoSeq.value) return
    const list = Array.isArray(res?.result) ? res.result : []
    const firstItem = list[0]
    const rawCodes = normalizeUpHierarchicalCodes(firstItem)
    const emotionText = normalizeUpHierarchicalEmotion(firstItem)
    const codes = reconcileExperienceCodePathWithTree(rawCodes)
    operationForm.experienceCodePath = codes
    pendingSentimentEcho.value = emotionText
    applySentimentEchoIfReady()
    lastExperienceCodeEchoEmotion.value = emotionText
    lastExperienceCodeEchoTagId.value = tagId
  } catch (error) {
    // 回显失败时不打断用户操作
  } finally {
    if (seq === experienceCodeEchoSeq.value) {
      experienceCodeEchoLoading.value = false
      inFlightExperienceCodeEchoTagId.value = null
    }
  }
}

const handleExperienceCodeLevelChange = (levelIndex: number, value: any) => {
  if (levelIndex === 0) {
    if (!hasValidExperienceCodeValue(value)) {
      operationForm.experienceCodePath = []
      handleExperienceCodeUserChange([])
      return
    }
    const selected = experienceCodeDisplayRoots.value.find(
      option => option.tagCode === String(value)
    )
    if (!selected) {
      operationForm.experienceCodePath = []
      handleExperienceCodeUserChange([])
      return
    }
    operationForm.experienceCodePath = [selected.rootTagCode, selected.tagCode]
    handleExperienceCodeUserChange(operationForm.experienceCodePath)
    return
  }

  const originalLevelIndex = levelIndex + experienceCodeDisplayLevelOffset
  const currentPath = operationForm.experienceCodePath
  const nextPath = currentPath.slice(0, originalLevelIndex)
  if (hasValidExperienceCodeValue(value)) {
    nextPath[originalLevelIndex] = String(value)
  }
  operationForm.experienceCodePath = nextPath
  handleExperienceCodeUserChange(nextPath)
}

const isExperienceCodeLevelDisabled = (levelIndex: number) => {
  if (experienceCodeLoading.value) return true
  if (levelIndex === 0) {
    return experienceCodeDisplayRoots.value.length === 0
  }
  const prevSelected = experienceCodeDisplayPath.value[levelIndex - 1]
  if (!prevSelected) return true
  return experienceCodeLevelOptions.value[levelIndex].length === 0
}

const fetchSentimentDict = async () => {
  try {
    const res = await insAllDictItems()
    const dict = (res && res.result && (res.result as any).insAllDictItems) || {}
    const list = dict.voc_sentiment || dict.vocSentiment || []
    if (Array.isArray(list)) {
      const items = list.map((item: any) => ({
        label: item.text ?? item.value,
        value: item.value
      }))
      sentimentOptions.value = [{ label: '全部', value: '' }, ...items]
    } else {
      sentimentOptions.value = []
    }
  } catch (error) {
    sentimentOptions.value = []
  }
}

onMounted(() => {
  fetchChannelOptions()
  fetchMappingOpinions()
  fetchExperienceCodes()
  fetchSentimentDict()
})

const actionStatusMap: Record<'add' | 'discard', 1 | 0> = {
  add: 1,
  discard: 0
}

const statusLabelMap: Record<string, string> = {
  '-1': '未修改',
  '0': '弃用',
  '1': '添加'
}

const operationLabelMap: Record<string, string> = {
  UPDATE_MAPPING: '关联观点',
  CREATE_MAPPING: '关联观点',
  DELETE_MAPPING: '取消关联',
  ADD: '添加',
  DISCARD: '弃用'
}

const formatStatusLabel = (value: any) => {
  if (value === undefined || value === null || value === '') return '-'
  const key = String(value)
  return statusLabelMap[key] || key
}

const formatOperationLabel = (value: any) => {
  if (!value) return '操作'
  const upper = String(value).toUpperCase()
  return operationLabelMap[upper] || upper
}

const historyTimeline = computed(() => {
  const source = Array.isArray(props.row?.modification_history)
    ? props.row?.modification_history
    : []
  return source.map((item: any, index: number) => {
    const timestamp = item.modified_time || '-'
    const user = item.username || '系统操作'
    const opRaw = String(item.operation).toUpperCase()
    const operationLabel = formatOperationLabel(opRaw)

    let content = operationLabel
    // 处理映射操作
    if (opRaw.includes('MAPPING')) {
      const newTopic = item.new_topic_text || item.new_topic_id || ''
      if (newTopic) {
        content = `${operationLabel} : ${newTopic}`
      }
    } else {
      // 处理状态/其他操作
      // 如果状态改变，显示新状态
      if (item.new_status !== undefined && item.new_status !== null) {
        const statusText = formatStatusLabel(item.new_status)
        content = `${operationLabel} : ${statusText}`
      }
    }

    // 格式化: "用户 | 时间"
    const metaInfo = `${user} | ${timestamp}`

    return {
      key: `${timestamp}-${index}`,
      timestamp, // 保留用于排序（如果需要），但界面显示 metaInfo
      content,
      metaInfo
    }
  })
})

const buildRecommendOpinionText = (row: any) => {
  const list = Array.isArray(row?.recommend_topic_list) ? row.recommend_topic_list : []
  if (list.length) {
    const texts = list.map((item: any) => (item && item.topic_text) || '').filter(text => !!text)
    if (texts.length) {
      return texts.join(' | ')
    }
  }
  const legacyText =
    row?.standardOpinion ||
    row?.standard_opinion ||
    row?.recommended_topic ||
    row?.standardOpinionText ||
    ''
  return legacyText || '-'
}

const normalizeLooseText = (raw: unknown) => {
  if (raw == null) return ''
  if (typeof raw === 'string') return raw.trim()
  if (typeof raw === 'number') return String(raw)
  return ''
}

const resolveDefaultMappingOpinion = (row: any) => {
  const recommendList = Array.isArray(row?.recommend_topic_list) ? row.recommend_topic_list : []

  if (recommendList.length > 1) return ''

  if (recommendList.length === 1) {
    const first = recommendList[0] || {}
    const text = normalizeLooseText(first.topic_text ?? first.topicText)
    return text
  }

  return (
    row?.selected_topic_text ||
    row?.standardOpinion ||
    row?.standard_opinion ||
    row?.recommended_topic ||
    ''
  )
}

/**
 * 重置体验代码与情感的回显缓存，避免复用弹窗时沿用上次处理结果。
 */
const resetOperationEchoState = () => {
  // 重新打开弹窗或切换数据时，作废旧请求，避免旧响应覆盖新的默认态。
  experienceCodeEchoSeq.value += 1
  inFlightExperienceCodeEchoTagId.value = null
  experienceCodeEchoLoading.value = false
  operationForm.experienceCodePath = []
  operationForm.sentiment = ''
  lastExperienceCodeEchoTagId.value = null
  lastExperienceCodeEchoEmotion.value = null
  pendingSentimentEcho.value = null
}

/**
 * 统一设置映射观点的程序性默认值，并跳过对应的 watch 副作用，防止初始化阶段重复请求。
 */
const setOperationMappingOpinion = (value: string) => {
  const nextValue = value || ''
  if (operationForm.mappingOpinion !== nextValue) {
    skipNextMappingOpinionWatch.value = true
  }
  operationForm.mappingOpinion = nextValue
}

/**
 * 每次打开弹窗或切换数据时，都按当前行重新初始化操作表单，确保“新词操作”恢复默认值。
 */
const initializeOperationForm = (row: any) => {
  operationForm.action = 'add'
  resetOperationEchoState()
  setOperationMappingOpinion(row ? resolveDefaultMappingOpinion(row) : '')
}

const info = computed(() => {
  const row = props.row || {}
  return {
    subject: row.subject || row.entity || '-',
    description: row.description || '-',
    // 使用传递的时间范围或模拟/行数据
    timeRange:
      props.timeRange && props.timeRange.length === 2
        ? `${props.timeRange[0]} - ${props.timeRange[1]}`
        : row.timeRange || '-',
    frequency: row.frequency || row.count || 0,
    recommendOpinion: buildRecommendOpinionText(row)
  }
})

// --- 标签页 2: 数据明细 ---
const assertRowId = (raw: any): number | string => {
  if (!raw || raw.id === undefined || raw.id === null) {
    throw new Error('缺少数据明细所需的新词ID')
  }
  if (String(raw.id).trim() === '') {
    throw new Error('新词ID为空，无法查询数据明细')
  }
  return raw.id
}

const detailsQuery = reactive({
  channelCodeList: [] as string[],
  keyword: ''
})

const detailsBatchId = ref<string>('')
const detailsRowId = ref<string>('')

const detailsTableData = ref<any[]>([])
const detailsLoading = ref(false)
const channelOptions = ref<any[]>([])

const detailsOverflowTooltipOptions = { popperClass: 'new-word-details-tooltip' } as const

const detailsFetchSeq = ref(0)
const detailsOpenSeq = ref(0)
const detailsTabEnterSeq = ref(0)
const lastAutoDetailsFetchKey = ref('')
const isAutoDetailsRefreshScheduled = ref(false)

const detailsPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const resolveBatchIdFromDetailsRecords = (records: any[]): string => {
  if (!Array.isArray(records) || records.length === 0) return ''
  const first = records[0]
  if (!first || typeof first !== 'object') return ''
  const batchId = (first as any).batchId
  if (batchId === undefined || batchId === null) return ''
  return String(batchId).trim()
}

const buildDetailsRequestPayload = () => {
  const currentRow = props.row
  const rowId = assertRowId(currentRow)
  const payload: Record<string, any> = {
    id: rowId,
    pageNum: detailsPagination.page,
    pageSize: detailsPagination.size
  }

  if (detailsBatchId.value) {
    payload.batchId = detailsBatchId.value
  }
  if (Array.isArray(detailsQuery.channelCodeList) && detailsQuery.channelCodeList.length) {
    payload.channelCodeList = detailsQuery.channelCodeList
  }
  if (detailsQuery.keyword) {
    payload.keywords = detailsQuery.keyword
  }

  return payload
}

const fetchChannelOptions = async () => {
  try {
    const res = await request<any>({
      url: '/insights/insCqCaDataSource/getChannelTree',
      method: 'GET',
      headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
    })
    const result = res && res.result ? res.result : []
    channelOptions.value = Array.isArray(result) ? result : []
  } catch (error) {
    channelOptions.value = []
  }
}

const fetchDetailsTableData = async () => {
  const seq = (detailsFetchSeq.value += 1)
  try {
    detailsLoading.value = true
    const payload = buildDetailsRequestPayload()
    const res = await request<any>({
      url: '/insights/insCqCaDataSource/getRawDataDetail',
      method: 'POST',
      data: payload,
      headers: { ...DATA_KNOWLEDGE_API_KEY_HEADER }
    })
    if (seq !== detailsFetchSeq.value) return
    const result = res && res.result ? res.result : {}
    const records = Array.isArray((result as any).records) ? (result as any).records : []
    const nextBatchId = resolveBatchIdFromDetailsRecords(records)
    if (nextBatchId && nextBatchId !== detailsBatchId.value) {
      detailsBatchId.value = nextBatchId
    }
    detailsTableData.value = records.map(item => {
      const record = item || {}
      return {
        ...record,
        channelL1: record.channelLevelOneName ?? record.isOuter,
        channelL2: record.channelLevelTwoName,
        channelName: record.channelLevelThreeName
      }
    })
    const totalFromResult = (result as any).total
    if (typeof totalFromResult === 'number') {
      detailsPagination.total = totalFromResult
    } else {
      detailsPagination.total = detailsTableData.value.length
    }
  } catch (error: any) {
    if (seq !== detailsFetchSeq.value) return
    detailsTableData.value = []
    detailsPagination.total = 0
    const messageText = error && error.message ? error.message : '获取数据明细失败'
    ElMessage.error(messageText)
  } finally {
    if (seq === detailsFetchSeq.value) {
      detailsLoading.value = false
    }
  }
}

const handleDetailsSearch = () => {
  detailsPagination.page = 1
  fetchDetailsTableData()
}

const handlePageChange = (page: number) => {
  detailsPagination.page = page
  fetchDetailsTableData()
}

const resetDetailsTableState = () => {
  detailsTableData.value = []
  detailsPagination.total = 0
}

const resolveDetailsRowId = (raw: any): string => {
  if (!raw) return ''
  const rawId = (raw as any).id
  if (rawId === undefined || rawId === null) return ''
  return String(rawId).trim()
}

const buildAutoDetailsFetchKey = () => {
  const row = props.row as any
  const rowId = row?.id ?? ''
  const channels = Array.isArray(detailsQuery.channelCodeList)
    ? detailsQuery.channelCodeList.join(',')
    : ''
  const keyword = detailsQuery.keyword || ''
  return `${detailsOpenSeq.value}|${detailsTabEnterSeq.value}|${rowId}|${detailsPagination.page}|${detailsPagination.size}|${channels}|${keyword}`
}

const refreshDetailsTableDataIfNeeded = () => {
  if (!props.visible) return
  if (activeTab.value !== NEW_WORD_DETAILS_DIALOG_TAB.DETAILS) return
  if (!props.row) return

  const nextKey = buildAutoDetailsFetchKey()
  if (nextKey === lastAutoDetailsFetchKey.value) return
  lastAutoDetailsFetchKey.value = nextKey
  fetchDetailsTableData()
}

const handlePageSizeChange = (size: number) => {
  detailsPagination.size = size
  detailsPagination.page = 1
  fetchDetailsTableData()
}

const scheduleDetailsRefreshIfNeeded = () => {
  if (isAutoDetailsRefreshScheduled.value) return
  isAutoDetailsRefreshScheduled.value = true
  queueMicrotask(() => {
    isAutoDetailsRefreshScheduled.value = false
    refreshDetailsTableDataIfNeeded()
  })
}

const handleClose = () => {
  dialogVisible.value = false
}

const handleConfirm = async () => {
  if (isReadOnly.value) {
    handleClose()
    return
  }
  const currentRow = props.row
  if (!currentRow || currentRow.id === undefined || currentRow.id === null) {
    ElMessage.error('缺少新词ID，无法更新')
    return
  }
  const loginMeta = getLoginUserMetaOrNull()
  if (!loginMeta) {
    ElMessage.error('缺少登录用户信息，无法更新')
    return
  }
  const statusValue = actionStatusMap[operationForm.action] ?? 1

  try {
    if (isSurveyMode.value) {
      const payload: any = {
        id: currentRow.id,
        status: statusValue,
        username: loginMeta.operatorName,
        user_id: loginMeta.employeeId
      }

      if (isAddAction.value) {
        const matched = mappingOpinionOptions.value.find(
          item => item.tagName === operationForm.mappingOpinion
        )
        if (!matched) {
          ElMessage.error('请选择有效的映射观点')
          return
        }
        payload.selected_topic_id = matched.tagCode
        payload.selected_topic_text = matched.tagName
      }

      await updateNewWord(payload)

      if (isAddAction.value) {
        const synonymPayload: any = {
          standard_opinion: payload.selected_topic_text,
          standard_opinion_id: String(payload.selected_topic_id),
          status_name: 'enabled',
          opinion_type: 0,
          opinion:
            currentRow.opinion ||
            currentRow.subject ||
            currentRow.description ||
            currentRow.full_opinion ||
            '',
          operator: loginMeta.operatorName
        }
        await createOpinionSynonym(synonymPayload)
      }
    } else {
      let selectedTopicId: string | undefined
      let selectedTopicText: string | undefined

      if (isAddAction.value) {
        const matched = mappingOpinionOptions.value.find(
          item => item.tagName === operationForm.mappingOpinion
        )
        if (!matched) {
          ElMessage.error('请选择有效的映射观点')
          return
        }
        selectedTopicId = matched.tagCode
        selectedTopicText = matched.tagName
      }

      // 映射观点缺失时，尝试使用当前行的推荐/标准观点回填
      if (!selectedTopicText || !selectedTopicId) {
        const fallbackText =
          currentRow.standardOpinion ||
          currentRow.standard_opinion ||
          currentRow.recommended_topic ||
          ''
        const fallbackId =
          currentRow.standard_opinion_id || currentRow.standardOpinionId || fallbackText

        if (!fallbackText || !fallbackId) {
          ElMessage.error('缺少标准观点信息，无法创建映射')
          return
        }
        selectedTopicText = fallbackText
        selectedTopicId = fallbackId
      }

      await updateNewWord({
        id: currentRow.id,
        status: statusValue,
        selected_topic_id: selectedTopicId,
        selected_topic_text: selectedTopicText,
        username: loginMeta.operatorName,
        user_id: loginMeta.employeeId
      })

      if (isAddAction.value) {
        const opinionType = resolvedOpinionType.value
        const synonymPayload: any = {
          standard_opinion: selectedTopicText,
          standard_opinion_id: String(selectedTopicId),
          status_name: 'enabled',
          opinion_type: opinionType,
          operator: loginMeta.operatorName
        }

        if (opinionType === 0) {
          synonymPayload.opinion =
            currentRow.opinion ||
            currentRow.subject ||
            currentRow.description ||
            currentRow.full_opinion ||
            ''
        } else {
          synonymPayload.entity =
            currentRow.subject || currentRow.entity || currentRow.full_opinion || ''
          synonymPayload.description =
            currentRow.description || currentRow.subject_desc || currentRow.full_opinion || ''
        }

        await createOpinionSynonym(synonymPayload)
      }
    }

    ElMessage.success('更新成功')
    emit('refresh')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error?.message || '更新失败')
  }
}

watch(
  () => props.row,
  val => {
    const nextRowId = resolveDetailsRowId(val)
    if (nextRowId !== detailsRowId.value) {
      detailsBatchId.value = ''
      detailsPagination.page = 1
      resetDetailsTableState()
    }
    detailsRowId.value = nextRowId
    if (val) {
      initializeOperationForm(val)
      void fetchMappingOpinions('')
      if (props.visible) {
        void echoExperienceCodeByMappingOpinion()
      }
    } else {
      initializeOperationForm(null)
    }
    scheduleDetailsRefreshIfNeeded()
  },
  { immediate: true }
)

watch(
  () => props.visible,
  visible => {
    if (visible) {
      detailsBatchId.value = ''
      detailsPagination.page = 1
      resetDetailsTableState()
      detailsOpenSeq.value += 1
      activeTab.value = defaultActiveTab.value || NEW_WORD_DETAILS_DIALOG_TAB.OPERATION
      initializeOperationForm(props.row)
      void fetchMappingOpinions('')
      void echoExperienceCodeByMappingOpinion()
      scheduleDetailsRefreshIfNeeded()
    }
  }
)

watch(
  () => operationForm.mappingOpinion,
  (val, prev) => {
    if (val === prev) return
    if (skipNextMappingOpinionWatch.value) {
      skipNextMappingOpinionWatch.value = false
      return
    }
    // 映射观点切换后，旧的体验代码回显结果不再可信，先作废再按新值触发回显。
    experienceCodeEchoSeq.value += 1
    inFlightExperienceCodeEchoTagId.value = null
    experienceCodeEchoLoading.value = false
    operationForm.sentiment = ''
    lastExperienceCodeEchoTagId.value = null
    lastExperienceCodeEchoEmotion.value = null
    pendingSentimentEcho.value = null
    if (!hasValidExperienceCodeValue(val)) return
    operationForm.experienceCodePath = []
    void echoExperienceCodeByMappingOpinion()
  }
)

watch(
  () => mappingOpinionOptions.value.length,
  () => {
    void echoExperienceCodeByMappingOpinion()
  }
)

watch(
  () => experienceCodeOptions.value,
  () => {
    reconcileExperienceCodePathIfNeeded()
  }
)

watch(
  () => sentimentOptions.value.length,
  () => {
    applySentimentEchoIfReady()
  }
)

watch(
  () => activeTab.value,
  val => {
    if (val === NEW_WORD_DETAILS_DIALOG_TAB.DETAILS) {
      detailsTabEnterSeq.value += 1
      scheduleDetailsRefreshIfNeeded()
    }
  }
)

defineExpose({
  setDefaultActiveTab
})
</script>

<template>
  <AppDialog
    v-model:visible="dialogVisible"
    title="新词详情"
    width="900px"
    style="max-height: 80vh"
    :confirm="handleConfirm"
    class="new-word-dialog"
  >
    <div class="new-word-dialog__body">
      <el-tabs v-model="activeTab" type="card" class="custom-tabs">
        <el-tab-pane label="新词操作" name="operation">
          <div class="operation-pane">
            <!-- Info Card -->
            <div class="info-card">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">主体：</span>
                    <span class="value">{{ info.subject }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">时间范围：</span>
                    <span class="value">{{ info.timeRange }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">描述：</span>
                    <span class="value">{{ info.description }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">提及频次：</span>
                    <span class="value">{{ info.frequency }}</span>
                  </div>
                </el-col>
                <el-col :span="24">
                  <div class="info-item">
                    <span class="label">推荐观点：</span>
                    <span class="value">{{ info.recommendOpinion }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- Form -->
            <div v-if="!isReadOnly" class="form-section">
              <el-form :model="operationForm" label-width="80px" label-position="left">
                <el-form-item label="新词操作" required>
                  <el-radio-group v-model="operationForm.action">
                    <el-radio label="add">添加</el-radio>
                    <el-radio label="discard">弃用</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="isAddAction" label="体验代码">
                  <div class="experience-code-selects">
                    <el-select-v2
                      v-for="(levelOptions, levelIndex) in experienceCodeLevelOptions"
                      :key="levelIndex"
                      :model-value="experienceCodeDisplayPath[levelIndex]"
                      :options="levelOptions"
                      :props="{ label: 'tagName', value: 'tagCode' }"
                      :loading="experienceCodeLoading"
                      filterable
                      clearable
                      :disabled="isExperienceCodeLevelDisabled(levelIndex)"
                      class="experience-code-select"
                      :placeholder="getExperienceCodeLevelLabel(levelIndex)"
                      @update:model-value="val => handleExperienceCodeLevelChange(levelIndex, val)"
                    />
                  </div>
                </el-form-item>
                <el-form-item v-if="isAddAction" label="情感">
                  <el-radio-group v-model="operationForm.sentiment">
                    <el-radio
                      v-for="item in sentimentOptions"
                      :key="item.value"
                      :label="item.value"
                    >
                      {{ item.label }}
                    </el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="isAddAction" label="映射观点" required>
                  <el-select-v2
                    v-model="operationForm.mappingOpinion"
                    placeholder="请选择"
                    class="w-full"
                    :options="mappingOpinionOptions"
                    :loading="mappingOpinionLoading"
                    :props="{ label: 'tagName', value: 'tagName' }"
                    filterable
                    clearable
                  />
                </el-form-item>
              </el-form>
            </div>

            <!-- History -->
            <div class="history-section">
              <div class="section-title">操作记录</div>
              <div v-if="historyTimeline.length" class="mt-16" style="max-width: 600px">
                <el-timeline>
                  <el-timeline-item
                    v-for="activity in historyTimeline"
                    :key="activity.key"
                    hide-timestamp
                  >
                    <div class="history-content">
                      <div class="history-text">{{ activity.content }}</div>
                      <div class="history-meta">{{ activity.metaInfo }}</div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
              <el-empty v-else description="暂无操作记录" :image-size="120" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="数据明细" name="details">
          <div class="details-pane">
            <!-- Filters -->
            <div class="filters-bar flex items-center justify-between mb-16">
              <div class="filters-title">数据明细列表</div>
              <div class="flex gap-12">
                <el-cascader
                  v-model="detailsQuery.channelCodeList"
                  :options="channelOptions"
                  :props="{
                    label: 'name',
                    value: 'code',
                    children: 'child',
                    multiple: true,
                    emitPath: false
                  }"
                  :show-all-levels="false"
                  placeholder="全部渠道"
                  collapse-tags
                  style="width: 220px"
                  class="w-full"
                  clearable
                  filterable
                  @change="handleDetailsSearch"
                />
                <el-input
                  v-model="detailsQuery.keyword"
                  placeholder="请输入关键词搜索"
                  style="width: 200px"
                  :suffix-icon="Search"
                  @change="handleDetailsSearch"
                  @keyup.enter="handleDetailsSearch"
                />
              </div>
            </div>

            <!-- Table -->
            <div style="flex: 1; overflow: hidden; min-height: 0">
              <el-table
                :data="detailsTableData"
                style="width: 100%"
                height="100%"
                v-loading="detailsLoading"
              >
                <el-table-column type="index" label="#" width="50" />
                <el-table-column prop="id" label="ID" width="100" />
                <el-table-column
                  prop="content"
                  label="原声"
                  min-width="260"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                />
                <el-table-column
                  label="主体"
                  min-width="150"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                >
                  <template #default>
                    {{ info.subject }}
                  </template>
                </el-table-column>
                <el-table-column
                  label="描述"
                  min-width="180"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                >
                  <template #default>
                    {{ info.description }}
                  </template>
                </el-table-column>
                <el-table-column
                  label="推荐观点"
                  min-width="180"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                >
                  <template #default>
                    {{ info.recommendOpinion }}
                  </template>
                </el-table-column>
                <el-table-column prop="channelL1" label="渠道一级分类" width="120" />
                <el-table-column prop="channelL2" label="渠道二级分类" width="120" />
                <el-table-column
                  prop="channelName"
                  label="渠道名称"
                  min-width="150"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                />
                <el-table-column prop="contentType" label="内容类型" width="120" />
                <el-table-column prop="isMainPost" label="是否主贴" width="100" />
                <el-table-column
                  prop="title"
                  label="标题"
                  min-width="200"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                />
                <el-table-column prop="userId" label="发布用户ID" width="150" />
                <el-table-column
                  prop="userName"
                  label="发布用户昵称"
                  min-width="140"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                />
                <el-table-column prop="authorId" label="主贴用户ID" width="150" />
                <el-table-column
                  prop="authorNick"
                  label="主贴用户名称"
                  min-width="140"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                />
                <el-table-column
                  prop="dataCreateTime"
                  label="发布时间"
                  width="180"
                  :show-overflow-tooltip="detailsOverflowTooltipOptions"
                />
              </el-table>
            </div>

            <!-- Pagination -->
            <div class="flex justify-end mt-16">
              <el-pagination
                v-model:current-page="detailsPagination.page"
                v-model:page-size="detailsPagination.size"
                :total="detailsPagination.total"
                layout="total, prev, pager, next, sizes"
                :page-sizes="[10, 20, 50]"
                @current-change="handlePageChange"
                @size-change="handlePageSizeChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </AppDialog>
</template>

<style lang="scss" scoped>
.new-word-dialog__body {
  padding: 0 4px;

  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
}

.operation-pane {
  border: 1px solid #ebedf0;
  padding: 20px;
  border-top: none;
  height: calc(80vh - 280px);
  overflow-y: auto;
}

.details-pane {
  border: 1px solid #ebedf0;
  padding: 20px;
  border-top: none;
  height: calc(80vh - 280px);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.el-table__row) {
    height: 52px;
  }
}

.filters-title {
  font-weight: 600;
  color: #1d2129;
}

.info-card {
  background: #eaf3ff;
  border-radius: 8px;
  border: 1px solid #dde3ee;
  padding: 16px 24px;
  margin-bottom: 24px;

  .info-item {
    margin-bottom: 12px;
    display: flex;

    .label {
      color: #86909c;
      width: 80px;
      flex-shrink: 0;
    }
    .value {
      color: #1d2129;
      font-weight: 500;
    }
  }
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #e5e6eb;
}

.experience-code-selects {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.experience-code-select {
  flex: 1;
  min-width: 140px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
  margin-bottom: 16px;
  border-left: 3px solid #165dff; // 蓝色强调色
  padding-left: 8px;
  line-height: 1;
}

.history-section {
  .history-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    .history-text {
      color: #1d2129;
      font-size: 14px;
      margin-bottom: 4px;
    }
    .history-meta {
      color: #86909c;
      font-size: 12px;
    }
    .history-remark {
      color: #86909c;
      font-size: 12px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

<style lang="scss">
.new-word-details-tooltip {
  max-width: 520px;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}
</style>
