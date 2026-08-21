<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  carUsageScenarioEnableStatus,
  deleteCarSceneCategory,
  findCarSceneCategoryList
} from '@/api/carUsageScenarios'
import { appDialogConfirm } from '@/components/appDialog'
import CategoryFormDialog from './CategoryFormDialog.vue'
import type { CarUsageScenarioCategoryItem, CarUsageScenarioCategorySubmitResult } from './types'

defineOptions({
  name: 'CarUsageScenarioCategoryList'
})

const emit = defineEmits<{
  (e: 'selection-change', category: CarUsageScenarioCategoryItem | null): void
  (e: 'refresh-scene-list'): void
}>()

const loading = ref(false)
const categoryRoots = ref<CarUsageScenarioCategoryItem[]>([])
const activeId = ref('')
const expandedKeys = ref<string[]>([])
const categoryKeyword = ref('')
const dialogVisible = ref(false)
const editingCategory = ref<CarUsageScenarioCategoryItem | null>(null)
const createParentCategory = ref<CarUsageScenarioCategoryItem | null>(null)
const pendingCategorySelection = ref<{ id: string; name: string } | null>(null)

/**
 * 将接口分类树映射成页面可直接渲染的视图模型，并额外记录父级关系，便于后续做新增和定位。
 */
const mapCategoryTreeForView = (
  nodes: Api.CarUsageScenarios.CategoryNode[] = [],
  depth = 0,
  parentId = ''
): CarUsageScenarioCategoryItem[] => {
  return nodes.map(node => {
    const categoryId = String(node.id || '')
    const children = mapCategoryTreeForView(node.children || [], depth + 1, categoryId)

    return {
      id: categoryId,
      patentId: String(node.patentId || parentId || ''),
      parentId,
      categoryName: String(node.categoryName || ''),
      categoryDescription: String(node.categoryDescription || ''),
      synonyms: String(node.synonyms || ''),
      typeName: String(node.typeName || ''),
      level: Number(node.level || depth + 1),
      depth,
      leafCount: Number(node.leafCount || 0),
      status: node.status || carUsageScenarioEnableStatus.ENABLED,
      disabled: !!(node.status !== carUsageScenarioEnableStatus.ENABLED),
      children
    }
  })
}

/**
 * 页面渲染是树形结构，但校验、父级查找和编辑定位都需要扁平数据，这里统一收敛一份。
 */
const flattenCategoryTree = (
  nodes: CarUsageScenarioCategoryItem[]
): CarUsageScenarioCategoryItem[] => {
  return nodes.flatMap(item => [item, ...flattenCategoryTree(item.children)])
}

/**
 * 默认优先选中首个子级分类，贴近效果图的初始高亮表现。
 */
const findDefaultActiveCategory = (
  nodes: CarUsageScenarioCategoryItem[]
): CarUsageScenarioCategoryItem | null => {
  for (const item of nodes) {
    if (item.depth > 0) return item
    const matchedChild = findDefaultActiveCategory(item.children)
    if (matchedChild) return matchedChild
  }
  return nodes[0] || null
}

/**
 * 仅展开当前已打开的节点，生成左侧树形目录的可见列表。
 */
const flattenVisibleTree = (nodes: CarUsageScenarioCategoryItem[]) => {
  return nodes.flatMap(item => {
    if (!item.children.length || !expandedKeys.value.includes(item.id)) {
      return [item]
    }
    return [item, ...flattenVisibleTree(item.children)]
  })
}

/**
 * 需要自动定位某个节点时，先把它的整条父级链路展开，避免刷新后节点高亮了但仍然不可见。
 */
const collectAncestorKeys = (
  categoryId: string,
  categoryLookup: Map<string, CarUsageScenarioCategoryItem>
) => {
  const ancestorKeys: string[] = []
  let currentCategory = categoryLookup.get(categoryId) || null

  while (currentCategory?.parentId) {
    ancestorKeys.unshift(currentCategory.parentId)
    currentCategory = categoryLookup.get(currentCategory.parentId) || null
  }

  return ancestorKeys
}

/**
 * 刷新后保留已展开节点，并在新建/编辑完成时补齐目标节点的父级展开态。
 */
const resolveExpandedKeys = (nodes: CarUsageScenarioCategoryItem[], targetCategoryId = '') => {
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

const allCategories = computed(() => flattenCategoryTree(categoryRoots.value))
const categoryMap = computed(() => {
  return new Map(allCategories.value.map(item => [item.id, item]))
})
const displayList = computed(() => flattenVisibleTree(categoryRoots.value))
const selectedCategory = computed(() => categoryMap.value.get(activeId.value) || null)
/**
 * 空状态需要区分“尚未创建分类”和“搜索无结果”，避免用户误判为接口异常。
 */
const emptyDescription = computed(() => {
  return categoryKeyword.value.trim() ? '未找到匹配的分类' : '暂无分类数据'
})

/**
 * 刷新后尽量保留选中项；不存在时回落到默认节点。
 */
const syncActiveCategory = () => {
  const categories = displayList.value
  if (!categories.length) {
    activeId.value = ''
    emit('selection-change', null)
    return
  }

  const matchedCategory = categories.find(item => item.id === activeId.value)
  const nextCategory = matchedCategory || findDefaultActiveCategory(categoryRoots.value)
  if (!nextCategory) {
    activeId.value = ''
    emit('selection-change', null)
    return
  }

  activeId.value = nextCategory.id
  emit('selection-change', nextCategory)
}

/**
 * 新建接口若未返回主键，则退化为按唯一分类名称回填选中项，避免刷新后仍停留在旧节点。
 */
const resolvePendingCategorySelection = (categories: CarUsageScenarioCategoryItem[]) => {
  const pendingSelection = pendingCategorySelection.value
  if (!pendingSelection) {
    return
  }

  const matchedCategory =
    categories.find(item => item.id === pendingSelection.id) ||
    categories.find(item => item.categoryName.trim() === pendingSelection.name.trim())

  if (matchedCategory) {
    activeId.value = matchedCategory.id
  }

  pendingCategorySelection.value = null
}

/**
 * 左侧分类树改为一次性拉取，避免滚动触底后再追加导致的选中态和展开态跳变。
 */
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const response = await findCarSceneCategoryList({
      categoryName: categoryKeyword.value.trim() || undefined
    })
    const result = Array.isArray(response.result) ? response.result : []
    const categoryViewList = mapCategoryTreeForView(result)
    const flattenedCategoryList = flattenCategoryTree(categoryViewList)

    categoryRoots.value = categoryViewList
    resolvePendingCategorySelection(flattenedCategoryList)

    expandedKeys.value = resolveExpandedKeys(categoryViewList, activeId.value)

    await nextTick()
    syncActiveCategory()
  } catch (error: any) {
    categoryRoots.value = []
    expandedKeys.value = []
    syncActiveCategory()
    ElMessage.error(error?.message || '获取用车场景分类失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 选中目录节点后，右侧仅按当前节点 ID 查询。
 */
const handleSelect = (category: CarUsageScenarioCategoryItem) => {
  activeId.value = category.id
  emit('selection-change', category)
}

/**
 * 目录节点可展开时仅切换折叠态，不改变当前业务选中项。
 */
const toggleExpand = (category: CarUsageScenarioCategoryItem) => {
  if (!category.children.length) return
  expandedKeys.value = expandedKeys.value.includes(category.id)
    ? expandedKeys.value.filter(item => item !== category.id)
    : [...expandedKeys.value, category.id]
}

/**
 * 左侧搜索按分类名称重新查询，并重置高亮状态。
 */
const handleSearch = async () => {
  activeId.value = ''
  await refreshList()
}

/**
 * 对外暴露刷新能力，列表变更后统一重新拉取整棵树，避免局部刷新造成节点状态不一致。
 */
const refreshList = async () => {
  await fetchCategoryList()
}

/**
 * 新建分类仍沿用当前选中分类作为上下文，具体落在哪一层由弹框提交时统一换算。
 */
const resolveCreateParentCategory = () => {
  return selectedCategory.value || categoryRoots.value[0] || null
}

const handleCreate = () => {
  editingCategory.value = null
  createParentCategory.value = resolveCreateParentCategory()
  dialogVisible.value = true
}

const handleEdit = (category: CarUsageScenarioCategoryItem) => {
  editingCategory.value = category
  createParentCategory.value = categoryMap.value.get(category.parentId) || null
  dialogVisible.value = true
}

/**
 * 删除按钮沿用参考页逻辑：存在下挂场景或子节点时不展示，避免误删后端仍被引用的分类。
 */
const canDeleteCategory = (category: CarUsageScenarioCategoryItem) => {
  return false //不需要删除
  // return category.depth != 0 && !category.leafCount && !category.children.length
}

const handleDelete = async (category: CarUsageScenarioCategoryItem) => {
  if (!category.id) return

  try {
    // 删除是高风险操作，统一走 AppDialog 程序化确认，保持全站弹框样式一致。
    await appDialogConfirm(`确认要删除分类「${category.categoryName}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px'
      }
    })

    await deleteCarSceneCategory({ id: category.id })
    ElMessage.success('删除成功')
    if (activeId.value === category.id) {
      activeId.value = ''
    }
    await refreshList()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close' || error?.message === 'cancel') {
      return
    }
    ElMessage.error(error?.message || '删除分类失败，请稍后重试')
  }
}

/**
 * 新建分类时保持当前右侧结果集稳定；仅编辑当前选中分类后，才主动驱动右侧列表重查。
 */
const handleDialogSuccess = async (payload: CarUsageScenarioCategorySubmitResult) => {
  pendingCategorySelection.value = {
    id: payload.categoryId,
    name: payload.categoryName
  }
  if (payload.categoryId) {
    activeId.value = payload.categoryId
  }
  await refreshList()
  if (payload.mode === 'edit') {
    emit('refresh-scene-list')
  }
}

onMounted(async () => {
  await refreshList()
})

defineExpose({
  refreshList
})
</script>

<template>
  <section class="car-usage-category card-class">
    <div class="panel-head">
      <div class="panel-title">分类列表</div>
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
          :class="{
            'category-row--disabled': item.disabled,
            'category-row--active': item.id === activeId
          }"
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
              <!-- <div v-else class="category-row__arrow category-row__arrow--empty" /> -->

              <div class="category-row__name">
                <span class="single-line-ellipsis">{{ item.categoryName || '-' }}</span>
                <span class="category-row__count">({{ item.leafCount || 0 }})</span>
              </div>
            </div>

            <div v-if="item.depth > 0 && item.id === activeId" class="category-row__action">
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
      :category-list="allCategories"
      :parent-category="createParentCategory"
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

.car-usage-category {
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

.category-row__arrow--empty {
  width: 18px;
}

.category-row__arrow-icon {
  font-size: 12px;
  color: #667085;
}

.category-row__icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
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
