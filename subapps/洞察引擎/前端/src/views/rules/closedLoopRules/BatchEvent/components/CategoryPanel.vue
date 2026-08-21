<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { appDialogConfirm } from '@/components/appDialog'
import {
  deleteBatchRuleCategory,
  findBatchRuleCategoryList,
  type BatchRuleCategoryNode as ApiBatchRuleCategoryNode
} from '@/api/batchEventRules'
import CategoryFormDialog from './CategoryFormDialog.vue'
import { useBatchEventContext } from '../useBatchEventContext'
import type {
  BatchCategorySubmitResult,
  BatchCategoryTreeNode,
  BatchCategoryViewItem
} from '../types'

defineOptions({
  name: 'BatchEventCategoryPanel'
})
const { categoryRefreshToken, setCurrentCategory } = useBatchEventContext()

const loading = ref(false)
const categoryRoots = ref<BatchCategoryTreeNode[]>([])
const activeId = ref('')
const expandedKeys = ref<string[]>([])
const categoryKeyword = ref('')
const dialogVisible = ref(false)
const editingCategory = ref<BatchCategoryTreeNode | null>(null)
const pendingCategorySelection = ref<{ id: string; name: string } | null>(null)
const fetchCategoryRequestId = ref(0)

/**
 * 分类树在页面层需要稳定的字符串 ID、数量字段和 children 数组，
 * 这里统一规整接口返回，避免模板和后续树遍历逻辑反复做空值兼容。
 * @param nodes 接口返回的原始分类树
 * @returns BatchCategoryTreeNode[]
 */
const normalizeCategoryTree = (nodes: ApiBatchRuleCategoryNode[] = []): BatchCategoryTreeNode[] => {
  return (nodes || []).map(item => ({
    id: String(item.id || ''),
    name: String(item.name || ''),
    parentId: (() => {
      const normalizedParentId = String(item.parentId ?? '').trim()
      return !normalizedParentId || ['0', 'null', 'undefined'].includes(normalizedParentId)
        ? null
        : normalizedParentId
    })(),
    sort: Number(item.sort ?? item.sortOrder ?? 0),
    sortOrder: Number(item.sortOrder ?? item.sort ?? 0),
    ruleCount: Number(item.ruleCount ?? item.count ?? 0),
    children: normalizeCategoryTree(item.children || [])
  }))
}

/**
 * 分类列表页只关心 searchKey 的查询协议与规整后的树结构，
 * 直接在组件内收口，删除中间文件后依然保留明确的数据边界。
 * @param keyword 分类搜索关键字
 * @returns Promise<{ success?: boolean; message: string; result: BatchCategoryTreeNode[] }>
 */
const fetchCategoryTree = async (keyword = '') => {
  const normalizedKeyword = keyword.trim()
  const response = await findBatchRuleCategoryList({
    searchKey: normalizedKeyword || undefined
  })

  if (!response.success) {
    return {
      ...response,
      result: [] as BatchCategoryTreeNode[]
    }
  }

  return {
    ...response,
    result: normalizeCategoryTree(response.result || [])
  }
}

/**
 * 分类数量展示统一使用当前协议的 ruleCount 字段，避免页面继续耦合历史字段。
 * @param category 分类节点
 * @returns number
 */
const getCategoryRuleCount = (category: BatchCategoryTreeNode | BatchCategoryViewItem) => {
  return Number(category.ruleCount || 0)
}

/**
 * 扁平化整棵树，便于后续做选中定位、父级追溯和删除校验。
 * @param nodes 分类树节点
 * @returns BatchCategoryTreeNode[]
 */
const flattenCategoryTree = (nodes: BatchCategoryTreeNode[]): BatchCategoryTreeNode[] => {
  return nodes.flatMap(item => [item, ...flattenCategoryTree(item.children)])
}

/**
 * 根据展开态生成左侧实际可见的目录列表，同时保留层级信息用于缩进渲染。
 * @param nodes 分类树节点
 * @param depth 当前层级
 * @returns BatchCategoryViewItem[]
 */
const flattenVisibleTree = (nodes: BatchCategoryTreeNode[], depth = 0): BatchCategoryViewItem[] => {
  return nodes.flatMap(item => {
    const currentItem: BatchCategoryViewItem = {
      ...item,
      depth
    }

    if (!item.children.length || !expandedKeys.value.includes(item.id)) {
      return [currentItem]
    }

    return [currentItem, ...flattenVisibleTree(item.children, depth + 1)]
  })
}

/**
 * 默认优先选中首个二级分类，贴近规则页效果图的初始展示。
 * @param nodes 分类树节点
 * @returns BatchCategoryTreeNode | null
 */
const findDefaultActiveCategory = (
  nodes: BatchCategoryTreeNode[]
): BatchCategoryTreeNode | null => {
  for (const item of nodes) {
    if (item.children.length) {
      return item.children[0]
    }
  }

  return nodes[0] || null
}

/**
 * 刷新后如果需要自动定位到某个分类，需要先补齐它的父级展开链路。
 * @param categoryId 分类 ID
 * @param categoryLookup 分类映射
 * @returns string[]
 */
const collectAncestorKeys = (
  categoryId: string,
  categoryLookup: Map<string, BatchCategoryTreeNode>
) => {
  const ancestorKeys: string[] = []
  let currentCategory = categoryLookup.get(categoryId) || null

  while (currentCategory) {
    const parentId = currentCategory.parentId ? String(currentCategory.parentId) : null

    if (!parentId) {
      break
    }

    ancestorKeys.unshift(parentId)
    currentCategory = categoryLookup.get(parentId) || null
  }

  return ancestorKeys
}

/**
 * 仅保留仍然有效的展开项，同时补上目标节点的父级展开态。
 * @param nodes 当前分类树
 * @param targetCategoryId 目标分类 ID
 * @returns string[]
 */
const resolveExpandedKeys = (nodes: BatchCategoryTreeNode[], targetCategoryId = '') => {
  const categoryLookup = new Map(flattenCategoryTree(nodes).map(item => [item.id, item]))
  const rootKeys = nodes.filter(item => item.children.length).map(item => item.id)
  const validExpandedKeys = expandedKeys.value.filter(
    item => categoryLookup.get(item)?.children.length
  )
  const targetAncestorKeys = targetCategoryId
    ? collectAncestorKeys(targetCategoryId, categoryLookup)
    : []

  return Array.from(new Set([...rootKeys, ...validExpandedKeys, ...targetAncestorKeys]))
}

const displayList = computed(() => flattenVisibleTree(categoryRoots.value))
const emptyDescription = computed(() => {
  return categoryKeyword.value.trim() ? '未找到匹配的分类' : '暂无数据'
})

/**
 * 尽量保留当前高亮项；如果已不存在，则自动回落到默认分类。
 */
const syncActiveCategory = () => {
  const categories = displayList.value

  if (!categories.length) {
    activeId.value = ''
    setCurrentCategory(null)
    return
  }

  const matchedCategory = categories.find(item => item.id === activeId.value)
  const nextCategory = matchedCategory || findDefaultActiveCategory(categoryRoots.value)

  if (!nextCategory) {
    activeId.value = ''
    setCurrentCategory(null)
    return
  }

  activeId.value = nextCategory.id
  setCurrentCategory(nextCategory)
}

/**
 * 新建接口如果返回了新 ID，刷新后优先尝试回显该节点，避免仍停留在旧节点。
 */
const resolvePendingCategorySelection = (categories: BatchCategoryTreeNode[]) => {
  const pendingSelection = pendingCategorySelection.value

  if (!pendingSelection) {
    return
  }

  const matchedCategory =
    categories.find(item => item.id === pendingSelection.id) ||
    categories.find(item => item.name.trim() === pendingSelection.name.trim())

  if (matchedCategory) {
    activeId.value = matchedCategory.id
  }

  pendingCategorySelection.value = null
}

/**
 * 左侧分类统一整树刷新，保证统计数量、层级结构和高亮状态保持一致。
 */
const fetchCategoryList = async () => {
  const currentRequestId = ++fetchCategoryRequestId.value
  loading.value = true

  try {
    const response = await fetchCategoryTree(categoryKeyword.value.trim())

    if (currentRequestId !== fetchCategoryRequestId.value) {
      return
    }

    if (!response.success) {
      ElMessage.error(response.message || '获取分类列表失败，请稍后重试')
      categoryRoots.value = []
      expandedKeys.value = []
      syncActiveCategory()
      return
    }

    const flattenedList = flattenCategoryTree(response.result)

    categoryRoots.value = response.result
    resolvePendingCategorySelection(flattenedList)
    expandedKeys.value = resolveExpandedKeys(response.result, activeId.value)

    await nextTick()
    syncActiveCategory()
  } catch (error: any) {
    if (currentRequestId !== fetchCategoryRequestId.value) {
      return
    }

    categoryRoots.value = []
    expandedKeys.value = []
    syncActiveCategory()
    ElMessage.error(error?.message || '获取分类列表失败，请稍后重试')
  } finally {
    if (currentRequestId === fetchCategoryRequestId.value) {
      loading.value = false
    }
  }
}

/**
 * 选中分类后同步驱动右侧规则列表刷新。
 * @param category 当前分类
 */
const handleSelect = (category: BatchCategoryTreeNode) => {
  activeId.value = category.id
  setCurrentCategory(category)
}

/**
 * 可展开目录点击箭头只切换展开状态，不干扰当前业务选中项。
 * @param category 当前分类
 */
const toggleExpand = (category: BatchCategoryTreeNode) => {
  if (!category.children.length) return

  expandedKeys.value = expandedKeys.value.includes(category.id)
    ? expandedKeys.value.filter(item => item !== category.id)
    : [...expandedKeys.value, category.id]
}

/**
 * 分类搜索回车或点击搜索后整树重查，并重置当前高亮项。
 */
const handleSearch = async () => {
  activeId.value = ''
  await refreshList()
}

/**
 * 分类面板的刷新入口统一收口在这里，便于首屏加载、搜索和规则变更复用同一逻辑。
 */
const refreshList = async () => {
  await fetchCategoryList()
}

const handleCreate = () => {
  editingCategory.value = null
  dialogVisible.value = true
}

const handleEdit = (category: BatchCategoryTreeNode) => {
  editingCategory.value = category
  dialogVisible.value = true
}

/**
 * 当前页面仅按“无规则 + 无子节点”展示删除入口。
 * 这里仅修正文档说明，保持现有删除逻辑不变，避免后续维护时误判为还有限制层级。
 * @param category 当前分类
 * @returns boolean
 */
const canDeleteCategory = (category: BatchCategoryViewItem) => {
  return !getCategoryRuleCount(category) && !category.children.length
}

const handleDelete = async (category: BatchCategoryViewItem) => {
  if (!category.id) return

  try {
    await appDialogConfirm(`确认要删除分类「${category.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px'
      }
    })

    const response = await deleteBatchRuleCategory(category.id)

    if (response.success) {
      ElMessage.success('删除成功')

      if (activeId.value === category.id) {
        activeId.value = ''
      }

      await refreshList()
    } else {
      ElMessage.error(response.message || '删除分类失败，请稍后重试')
    }
  } catch (error: any) {
    if (error === 'cancel' || error === 'close' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error?.message || '删除分类失败，请稍后重试')
  }
}

/**
 * 新建或编辑成功后统一刷新左侧树，并尽量保持当前定位稳定。
 * @param payload 弹窗返回的分类结果
 */
const handleDialogSuccess = async (payload: BatchCategorySubmitResult) => {
  pendingCategorySelection.value = {
    id: payload.categoryId,
    name: payload.categoryName
  }

  if (payload.categoryId) {
    activeId.value = payload.categoryId
  }

  await refreshList()
}

onMounted(async () => {
  await refreshList()
})

watch(categoryRefreshToken, async (currentValue, previousValue) => {
  /**
   * 仅在规则侧触发刷新令牌变化时重查，避免面板初始化阶段额外多发一次请求。
   */
  if (currentValue === previousValue) {
    return
  }

  await refreshList()
})
</script>

<template>
  <section class="batch-category card-class">
    <div class="panel-head">
      <div class="panel-title">主题分类</div>
      <el-button type="primary" @click="handleCreate">
        <template #icon>
          <Plus />
        </template>
        新建分类
      </el-button>
    </div>

    <el-input
      v-model.trim="categoryKeyword"
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
          :key="item.id"
          class="category-row"
          :class="{ 'category-row--active': item.id === activeId }"
        >
          <div
            class="category-row__content"
            :style="{ marginLeft: `${item.depth * 24}px` }"
            @click="handleSelect(item)"
          >
            <div class="category-row__left">
              <div v-if="item.children.length" class="category-row__arrow">
                <el-icon class="category-row__arrow-icon" @click.stop="toggleExpand(item)">
                  <CaretBottom v-if="expandedKeys.includes(item.id)" />
                  <CaretRight v-else />
                </el-icon>
              </div>

              <div class="category-row__name">
                <span class="single-line-ellipsis">{{ item.name || '-' }}</span>
                <span class="category-row__count">({{ getCategoryRuleCount(item) }})</span>
              </div>
            </div>

            <div v-if="item.id === activeId" class="category-row__action">
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
      v-model:visible="dialogVisible"
      :category-data="editingCategory"
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
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 8px 8px 8px 8px;
  box-sizing: border-box;
}

.batch-category {
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
  border-radius: 4px 4px 4px 4px;
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
