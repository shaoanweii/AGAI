<script setup lang="ts">
import { debounce } from 'lodash-es'
import { computed, inject, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { deleteTagLibClient } from '@/api/tag'
import { appDialogConfirm } from '@/components/appDialog'
import useUserStore from '@/stores/modules/user'
import CategoryFormDialog from './CategoryFormDialog.vue'
import { experienceCodePageContextKey, type ExperienceCodePageCategoryData } from '../context'
import {
  type CategoryStatusValue,
  type ExperienceCategoryItem,
  type ExperienceCategorySubmitResult,
  type ExperienceCategoryTypeSummary,
  type ExperienceCodeType,
  type ExperienceFilterTarget
} from './types'
import { fetchExperienceCategoryData, fetchExperienceCategoryListData } from '../service'

defineOptions({
  name: 'ExperienceCategoryList'
})

const emit = defineEmits<{
  (e: 'selection-change', target: ExperienceFilterTarget | null): void
  (e: 'refresh-code-list'): void
}>()

const userStore = useUserStore()
const pageContext = inject(experienceCodePageContextKey, null)
const loading = ref(false)
const leftKeyword = ref('')
const expandedKeys = ref<string[]>([])
const categoryDialogVisible = ref(false)
const editingCategory = ref<ExperienceCategoryItem | null>(null)
const defaultTypeCode = ref<ExperienceCodeType>('')
const defaultParentId = ref('')
const pendingCategorySelection = ref<ExperienceCategorySubmitResult | null>(null)

interface ExperienceCategoryListNode {
  nodeKey: string
  id: string
  tagName: string
  tagType: ExperienceCodeType
  tagCode?: string
  leafCount: number
  level: number
  tagStatus?: CategoryStatusValue
  tagParentId?: string
  children: ExperienceCategoryListNode[]
}

type ExperienceCategoryListData = ExperienceCodePageCategoryData

const state = reactive({
  categories: [] as ExperienceCategoryItem[],
  typeSummaries: [] as ExperienceCategoryTypeSummary[],
  displayCategories: [] as ExperienceCategoryItem[],
  displayTypeSummaries: [] as ExperienceCategoryTypeSummary[],
  activeTarget: null as ExperienceFilterTarget | null
})

let requestSerial = 0
const SEARCH_DEBOUNCE_DELAY = 300

/**
 * 将树节点统一转换为筛选目标，避免默认选中和手动点击分别维护两套映射逻辑。
 */
const createTargetFromNode = (node: ExperienceCategoryListNode): ExperienceFilterTarget => {
  return node.level === 0
    ? {
        queryId: node.id,
        typeCode: node.tagType
      }
    : {
        queryId: node.id,
        typeCode: node.tagType,
        categoryId: node.id
      }
}

/**
 * 过滤目标只要携带分类 id，就视为分类节点；这样可以统一类型节点和分类节点的判定口径。
 */
const isCategoryTarget = (
  target: ExperienceFilterTarget | null
): target is ExperienceFilterTarget & { categoryId: string } => {
  return Boolean(target?.categoryId)
}

/**
 * 建立完整分类索引，便于弹框回填、删除定位和当前选中态校验复用。
 */
const categoryMap = computed(() => {
  const map = new Map<string, ExperienceCategoryItem>()
  state.categories.forEach(item => map.set(item.id, item))
  return map
})

/**
 * 搜索结果单独分组，左侧树渲染只消费当前可见数据。
 */
const displayCategoryChildrenMap = computed(() => {
  const map = new Map<string, ExperienceCategoryItem[]>()
  state.displayCategories.forEach(item => {
    const key = item.tagParentId || 'ROOT'
    const list = map.get(key) || []
    list.push(item)
    map.set(key, list)
  })
  return map
})

/**
 * 左侧树直接复用接口返回顺序，避免前端再次介入排序。
 */
const buildCategoryChildren = (
  typeCode: ExperienceCategoryTypeSummary['typeCode'],
  parentId = ''
) => {
  const key = parentId || 'ROOT'
  const children = (displayCategoryChildrenMap.value.get(key) || []).filter(
    item => item.tagType === typeCode
  )

  return children.map<ExperienceCategoryListNode>(item => ({
    ...item,
    // 展开态使用页面内唯一 nodeKey，避免接口 id 或 tagCode 重复时多个节点被一起折叠。
    nodeKey: `CATEGORY::${item.id}::${typeCode}`,
    children: buildCategoryChildren(typeCode, item.id)
  }))
}

/**
 * 左侧顶层直接展示当前可见的类型节点，顺序以接口返回为准。
 */
const fullTree = computed<ExperienceCategoryListNode[]>(() => {
  return state.displayTypeSummaries.map(item => ({
    // 顶层类型节点按 typeCode 独立维护展开态，避免“全旅程业务”等节点互相串联。
    nodeKey: `TYPE::${item.nodeId}-${item.typeCode}`,
    id: item.nodeId,
    tagName: item.label,
    tagType: item.typeCode,
    tagCode: item.tagCode,
    leafCount: item.count || 0,
    level: 0,
    children: buildCategoryChildren(item.typeCode)
  }))
})

/**
 * 默认优先命中首个可见分类节点，只有分类完全为空时才回落到类型节点，贴合目录首屏高亮预期。
 */
const findDefaultNode = (
  nodes: ExperienceCategoryListNode[]
): ExperienceCategoryListNode | null => {
  // 用户旅程以阶段作为顶级分类，首屏直接选中“使用”等阶段，不能再回落到“用户全旅程”层。
  if (nodes[0]?.tagType.startsWith('USER_JOURNEY::')) {
    return nodes[0]
  }

  for (const node of nodes) {
    if (node.level > 0) return node
    const matchedChild = findDefaultNode(node.children)
    if (matchedChild) return matchedChild
  }

  return nodes[0] || null
}

/**
 * 自动切换默认选中项时，需要把目标节点的父级链路展开，避免高亮节点落在折叠区域内。
 */
const findAncestorKeys = (
  target: Pick<ExperienceFilterTarget, 'typeCode' | 'categoryId'>,
  nodes: ExperienceCategoryListNode[],
  ancestorKeys: string[] = []
): string[] | null => {
  for (const node of nodes) {
    if (node.level > 0 && node.id === target.categoryId && node.tagType === target.typeCode) {
      return ancestorKeys
    }

    const matchedKeys = findAncestorKeys(target, node.children, [...ancestorKeys, node.nodeKey])
    if (matchedKeys) {
      return matchedKeys
    }
  }

  return null
}

/**
 * 移除“全部体验代码”后，默认回落到首个可见分类；若当前类型下暂无分类，则兜底选中首个类型节点。
 */
const resolveDefaultTarget = (): ExperienceFilterTarget | null => {
  const defaultNode = findDefaultNode(fullTree.value)
  return defaultNode ? createTargetFromNode(defaultNode) : null
}

/**
 * 将当前展开树拍平成可见列表，样式和交互对齐用车场景目录效果。
 */
const flattenVisibleTree = (nodes: ExperienceCategoryListNode[]) => {
  return nodes.flatMap(node => {
    if (!node.children.length || !expandedKeys.value.includes(node.nodeKey)) {
      return [node]
    }
    return [node, ...flattenVisibleTree(node.children)]
  })
}

/**
 * 收集当前树上所有可展开节点，搜索时一次性展开避免命中项被折叠。
 */
const collectExpandableIds = (nodes: ExperienceCategoryListNode[]) => {
  return nodes.flatMap(node => {
    if (!node.children.length) return []
    return [node.nodeKey, ...collectExpandableIds(node.children)]
  })
}

const displayList = computed(() => flattenVisibleTree(fullTree.value))

/**
 * 当前筛选目标必须是左侧可见节点，搜索或删除导致节点不可见时需要重新回落默认选中项。
 */
const isTargetVisible = (target: ExperienceFilterTarget | null) => {
  if (!target) return false

  if (!isCategoryTarget(target)) {
    return state.displayTypeSummaries.some(item => item.typeCode === target.typeCode)
  }

  return state.displayCategories.some(item => item.id === target.categoryId)
}

/**
 * 左侧高亮键统一采用 id + typeCode 组合，和树节点自身的唯一标识口径保持一致。
 */
const resolveNodeActiveKey = (node: ExperienceCategoryListNode) => {
  return `${node.id}-${node.tagType}`
}

/**
 * 当前选中态只服务左侧样式计算；根节点与分类节点都复用 queryId + typeCode，避免回退时串到其他根节点。
 */
const activeRowKey = computed(() => {
  const currentTarget = state.activeTarget
  if (!currentTarget) return ''

  return `${currentTarget.queryId}-${currentTarget.typeCode}`
})

/**
 * 统一通过 id + typeCode 组合键判断选中态，和目录节点渲染时的主键口径保持一致。
 */
const isNodeActive = (node: ExperienceCategoryListNode) => {
  return resolveNodeActiveKey(node) === activeRowKey.value
}

/**
 * 搜索无结果时提供明确空态文案，避免误判为接口异常。
 */
const emptyDescription = computed(() => {
  return leftKeyword.value.trim() ? '未找到匹配的分类' : '暂无数据'
})

/**
 * 数据刷新后同步展开态：首次进入默认仅展开首个类型，普通场景保留用户手动操作，搜索场景强制展开命中路径。
 */
const syncExpandedKeys = (forceExpandAll = false) => {
  const expandableIds = collectExpandableIds(fullTree.value)
  const expandableIdSet = new Set<string>(expandableIds)
  const firstTopLevelId = fullTree.value[0]?.nodeKey

  if (forceExpandAll) {
    expandedKeys.value = Array.from(new Set<string>(expandableIds))
    return
  }

  if (!expandedKeys.value.length) {
    expandedKeys.value = firstTopLevelId ? [firstTopLevelId] : []
    return
  }

  expandedKeys.value = expandedKeys.value.filter(item => expandableIdSet.has(item))
}

/**
 * 切换左侧节点时同步对外抛出筛选条件。
 */
const handleSelect = (node: ExperienceCategoryListNode) => {
  state.activeTarget = createTargetFromNode(node)
  emit('selection-change', state.activeTarget)
}

/**
 * 目录节点可展开时仅切换折叠态，不改变当前业务选中项。
 */
const toggleExpand = (node: ExperienceCategoryListNode) => {
  if (!node.children.length) return
  expandedKeys.value = expandedKeys.value.includes(node.nodeKey)
    ? expandedKeys.value.filter(item => item !== node.nodeKey)
    : [...expandedKeys.value, node.nodeKey]
}

/**
 * 根据当前选中节点推导新建分类默认值时，仅继承代码类型，不默认带出上级分类，避免误挂到当前节点下。
 */
const openCreateDialog = () => {
  editingCategory.value = null
  defaultParentId.value = ''
  defaultTypeCode.value =
    state.activeTarget?.typeCode || state.displayTypeSummaries[0]?.typeCode || ''

  categoryDialogVisible.value = true
}

/**
 * 分类节点才允许编辑，回填时直接取完整分类实体。
 */
const handleEdit = (node: ExperienceCategoryListNode) => {
  if (node.level === 0) return
  editingCategory.value = categoryMap.value.get(node.id) || null
  categoryDialogVisible.value = true
}

/**
 * 删除仅允许无下挂体验代码且无子分类的节点，避免误删仍被引用的数据。
 */
const canDeleteCategory = (node: ExperienceCategoryListNode) => {
  return false
  // return node.level > 0 && !node.leafCount && !node.children.length
}

/**
 * 读取完整分类源数据；当前页面内可复用已拿到的分类结果，
 * 但跨页面重新进入时会由 service 层重新发起接口请求。
 */
const loadFullCategoryData = async (refreshFull: boolean): Promise<ExperienceCategoryListData> => {
  if (pageContext) {
    if (!refreshFull && pageContext.categoryData.value.categories.length) {
      return pageContext.categoryData.value
    }

    return pageContext.refreshCategoryData({ force: refreshFull })
  }

  if (refreshFull || !state.categories.length) {
    return fetchExperienceCategoryData({ force: refreshFull })
  }

  return {
    categories: state.categories,
    typeSummaries: state.typeSummaries
  }
}

/**
 * 根据当前关键字获取左侧实际展示数据；搜索场景改为直接采用接口返回结果。
 */
const loadDisplayCategoryData = async (
  keyword: string,
  fullData: ExperienceCategoryListData
): Promise<ExperienceCategoryListData> => {
  if (!keyword) {
    return fullData
  }

  return fetchExperienceCategoryListData(keyword)
}

/**
 * 批量写入完整数据与展示数据，保证弹框数据源和左侧树视图始终同步。
 */
const applyCategoryData = (
  fullData: ExperienceCategoryListData,
  displayData: ExperienceCategoryListData
) => {
  state.categories = fullData.categories
  state.typeSummaries = fullData.typeSummaries
  state.displayCategories = displayData.categories
  state.displayTypeSummaries = displayData.typeSummaries
}

/**
 * 新建成功后优先按接口返回 id 回选；若接口未回传主键，则退化为按类型+名称兜底命中新建分类。
 */
const resolvePendingCategorySelection = () => {
  const pendingSelection = pendingCategorySelection.value
  if (!pendingSelection) {
    return null
  }

  const matchedCategory =
    state.categories.find(item => item.id === pendingSelection.categoryId) ||
    state.categories.find(
      item =>
        item.tagType === pendingSelection.typeCode &&
        item.tagName.trim() === pendingSelection.categoryName.trim()
    )

  pendingCategorySelection.value = null

  if (!matchedCategory) {
    return null
  }

  state.activeTarget = {
    queryId: matchedCategory.id,
    typeCode: matchedCategory.tagType,
    categoryId: matchedCategory.id
  }

  return state.activeTarget
}

/**
 * 数据刷新失败时统一回退默认状态，避免左树和右侧列表保留旧的无效选中项。
 */
const resetCategoryState = () => {
  state.categories = []
  state.displayCategories = []
  // 接口失败时直接清空左侧树，页面才能正确落到“暂无数据”空态。
  state.typeSummaries = []
  state.displayTypeSummaries = []
  state.activeTarget = null
}

/**
 * 刷新后校正展开态与当前选中项，确保搜索、删除和默认高亮始终落在可见节点上。
 */
const reconcileActiveTargetAfterRefresh = (
  keyword: string,
  revealTarget: ExperienceFilterTarget | null = null
) => {
  syncExpandedKeys(Boolean(keyword))

  if (isCategoryTarget(revealTarget)) {
    const ancestorKeys = findAncestorKeys(revealTarget, fullTree.value) || []
    expandedKeys.value = Array.from(new Set([...expandedKeys.value, ...ancestorKeys]))
  }

  if (isTargetVisible(state.activeTarget)) {
    return
  }

  state.activeTarget = resolveDefaultTarget()

  if (isCategoryTarget(state.activeTarget)) {
    const ancestorKeys = findAncestorKeys(state.activeTarget, fullTree.value) || []
    expandedKeys.value = Array.from(new Set([...expandedKeys.value, ...ancestorKeys]))
  }
}

/**
 * 刷新左侧分类树：完整数据服务弹框与联动，搜索结果直接以接口返回为准。
 */
const refreshList = async (options: { refreshFull?: boolean } = {}) => {
  const { refreshFull = false } = options
  const keyword = leftKeyword.value.trim()
  const currentRequest = ++requestSerial
  loading.value = true

  try {
    const fullData = await loadFullCategoryData(refreshFull)

    if (currentRequest !== requestSerial) return

    const displayData = await loadDisplayCategoryData(keyword, fullData)

    if (currentRequest !== requestSerial) return

    applyCategoryData(fullData, displayData)
    const pendingTarget = resolvePendingCategorySelection()
    reconcileActiveTargetAfterRefresh(keyword, pendingTarget)

    emit('selection-change', state.activeTarget)
  } catch (error: any) {
    resetCategoryState()
    emit('selection-change', state.activeTarget)
    ElMessage.error(error?.message || '获取分类列表失败，请稍后重试')
  } finally {
    if (currentRequest === requestSerial) {
      loading.value = false
    }
  }
}

/**
 * 输入搜索词时做轻量防抖，避免连续输入导致左侧分类接口被高频触发。
 */
const triggerKeywordSearch = debounce(() => {
  void refreshList()
}, SEARCH_DEBOUNCE_DELAY)

watch(leftKeyword, (keyword, previousKeyword) => {
  if (keyword === previousKeyword) return
  triggerKeywordSearch()
})

/**
 * 用户主动确认搜索时立即执行，并取消尚未触发的防抖任务，保证回车和点击反馈更直接。
 */
const handleSearch = async () => {
  triggerKeywordSearch.cancel()
  await refreshList()
}

/**
 * 删除成功后同步刷新左侧树和右侧列表，避免目录与表格状态不一致。
 */
const handleDelete = async (node: ExperienceCategoryListNode) => {
  if (!canDeleteCategory(node)) return
  const category = categoryMap.value.get(node.id)
  if (!category) return

  try {
    triggerKeywordSearch.cancel()
    await appDialogConfirm(`确认要删除分类「${category.tagName}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px'
      }
    })

    await deleteTagLibClient({
      id: category.id,
      appClient: userStore.clientId || undefined
    })

    ElMessage.success('删除成功')

    if (state.activeTarget?.categoryId === category.id) {
      state.activeTarget = null
    }

    await refreshList({ refreshFull: true })
    emit('refresh-code-list')
  } catch (error: any) {
    if (error === 'cancel' || error === 'close' || error?.message === 'cancel') {
      return
    }
    ElMessage.error(error?.message || '删除分类失败，请稍后重试')
  }
}

/**
 * 分类保存成功后需要刷新完整分类数据，并通知右侧列表同步元数据。
 */
const handleDialogSuccess = async (payload: ExperienceCategorySubmitResult) => {
  triggerKeywordSearch.cancel()

  // 分类保存后若仍保留旧搜索词，目标分类可能因为不在筛选结果中而无法被高亮，因此这里回到完整列表再做回选。
  if (leftKeyword.value.trim()) {
    leftKeyword.value = ''
    triggerKeywordSearch.cancel()
  }

  pendingCategorySelection.value = payload
  await refreshList({ refreshFull: true })
  emit('refresh-code-list')
}

onMounted(async () => {
  await refreshList()
})

onBeforeUnmount(() => {
  triggerKeywordSearch.cancel()
})

defineExpose({
  refreshList
})
</script>

<template>
  <section class="experience-category card-class">
    <div class="panel-head">
      <div class="panel-title">分类列表</div>
      <el-button type="primary" @click="openCreateDialog">
        <template #icon>
          <Plus />
        </template>
        新建分类
      </el-button>
    </div>

    <el-input
      v-model.trim="leftKeyword"
      clearable
      placeholder="请输入分类名称"
      class="category-search"
      @clear="handleSearch"
      @keyup.enter="handleSearch"
    >
      <template #suffix>
        <el-icon class="category-search__icon" @click="handleSearch">
          <Search />
        </el-icon>
      </template>
    </el-input>

    <div v-loading="loading" class="category-scroll">
      <template v-if="displayList.length">
        <div
          v-for="item in displayList"
          :key="item.nodeKey"
          class="category-row"
          :class="{
            'category-row--disabled': item.level > 0 && item.tagStatus === '0',
            'category-row--active': isNodeActive(item)
          }"
        >
          <div
            class="category-row__content"
            :style="{ marginLeft: `${item.level * 24}px` }"
            @click="handleSelect(item)"
          >
            <div class="category-row__left">
              <div v-if="item.children.length" class="category-row__arrow">
                <el-icon class="category-row__arrow-icon" @click.stop="toggleExpand(item)">
                  <CaretBottom v-if="expandedKeys.includes(item.nodeKey)" />
                  <CaretRight v-else />
                </el-icon>
              </div>

              <div class="category-row__name">
                <span class="single-line-ellipsis">{{ item.tagName || '-' }}</span>
                <span class="category-row__count">({{ item.leafCount || 0 }})</span>
              </div>
            </div>

            <div v-if="item.level > 0 && isNodeActive(item)" class="category-row__action">
              <SvgIcon
                name="rules-edit"
                class="category-row__action-icon"
                color="#4E5969"
                @click.stop="handleEdit(item)"
              />
              <SvgIcon
                v-if="canDeleteCategory(item)"
                name="rules-delete"
                class="category-row__action-icon"
                color="#4E5969"
                @click.stop="handleDelete(item)"
              />
            </div>
          </div>
        </div>
      </template>

      <div v-else-if="!loading" class="category-empty">
        <el-empty :description="emptyDescription" :image-size="110" />
      </div>
    </div>

    <CategoryFormDialog
      v-model:visible="categoryDialogVisible"
      :category-data="editingCategory"
      :category-list="state.categories"
      :type-summaries="state.typeSummaries"
      :default-type-code="defaultTypeCode"
      :default-parent-id="defaultParentId"
      @success="handleDialogSuccess"
    />
  </section>
</template>

<style scoped lang="scss">
.card-class {
  display: flex;
  flex-direction: column;
  padding: 16px 24px;
  background: #ffffff;
  box-shadow: 0 1px 2px 0 rgba(10, 13, 18, 0.05);
  border-radius: 8px;
  box-sizing: border-box;
}

.experience-category {
  width: 320px;
  min-width: 320px;
  height: 100%;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.panel-title {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  line-height: 32px;
}

.category-search {
  margin-bottom: 16px;
}

.category-search__icon {
  cursor: pointer;
  color: #667085;
}

.category-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
  position: relative;
  padding-top: 2px;
}

.category-row {
  margin-bottom: 6px;
}

.category-row__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 36px;
  padding: 0 8px 0 14px;
  border-radius: 4px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.category-row__content:hover {
  background: #f7f9fc;
}

.category-row--active .category-row__content {
  background: #eaf3ff;
}

.category-row__left {
  display: flex;
  align-items: center;
  min-width: 0;
  flex: 1;
}

.category-row__arrow {
  width: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.category-row__arrow-icon {
  font-size: 12px;
  color: #667085;
}

.category-row__name {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 7px;
  min-width: 0;
  font-weight: 400;
  font-size: 14px;
  color: #1d2129;
  line-height: 22px;
}

.category-row__count {
  flex-shrink: 0;
  color: #1d2129;
}

.category-row--disabled .category-row__name,
.category-row--disabled .category-row__count {
  color: #929aa6 !important;
}

.category-row--active .category-row__name,
.category-row--active .category-row__count {
  color: #1677ff;
}

.category-row__action {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.category-row__action-icon {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.category-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  padding: 12px 0 24px;
}
</style>
