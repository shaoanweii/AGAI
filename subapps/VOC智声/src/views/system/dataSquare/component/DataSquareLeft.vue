<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import brandA01 from '@/assets/images/system/dataSquare/A01.png'
import brandA02 from '@/assets/images/system/dataSquare/A02.png'
import brandA03 from '@/assets/images/system/dataSquare/A03.png'
import brandA04 from '@/assets/images/system/dataSquare/A04.png'
import brandA05 from '@/assets/images/system/dataSquare/A05.png'
import defaultIcon from '@/assets/images/system/dataSquare/default.png'
import { deleteDataPlazaCategory } from '@/api/dataPlaza'
import type { DataPlazaCategoryItem } from '@/api/dataPlaza/types'
import CategoryDialog from './CategoryDialog.vue'
import SortDialog from './SortDialog.vue'
import { dataSquareActions, dataSquareStore } from '../store'
import { openDataSquareActionConfirm } from '../hooks/useReportActions'

defineOptions({
  name: 'DataSquareLeft'
})

export interface CategoryFilterPayload {
  categoryId: string
}

export interface CategoryRestorePayload {
  selectedParentId: string
  selectedCategoryId: string
}

interface Props {
  restoreSelection?: CategoryRestorePayload | null
  restoreSelectionVersion?: number
  silentRefreshVersion?: number
}

interface Emits {
  (e: 'change', payload: CategoryFilterPayload): void
}

const props = withDefaults(defineProps<Props>(), {
  restoreSelection: null,
  restoreSelectionVersion: 0,
  silentRefreshVersion: 0
})

const emit = defineEmits<Emits>()

const brandIconMap: Record<string, string> = {
  A01: brandA01,
  A02: brandA02,
  A03: brandA03,
  A04: brandA04,
  A05: brandA05
}

const categoryDialogVisible = ref(false)
const categoryDialogMode = ref<'create' | 'edit'>('create')
const categoryDialogEditData = ref<DataPlazaCategoryItem | null>(null)
const sortDialogVisible = ref(false)
const activeParentId = ref('')
const activeCategoryId = ref('')
const expandedParentIds = ref<string[]>([])
const currentDialogParentId = ref('')
const DEFAULT_BRAND_CODE = 'A01'

const categoryList = computed(() => dataSquareStore.categoryTree || [])

interface ApplySelectionOptions {
  emitChange?: boolean
  keepParentSelection?: boolean
}

/**
 * 判断一级分类是否处于展开状态。
 * @param id 一级分类ID
 * @returns 是否展开
 */
const isExpanded = (id: string) => {
  return expandedParentIds.value.includes(id)
}

/**
 * 根据品牌编码获取一级分类图标。
 * @param brandCode 品牌编码
 * @returns 匹配到的本地图标，未匹配时回退默认图
 */
const getParentIcon = (brandCode: string) => {
  return brandIconMap[brandCode] || defaultIcon
}

/**
 * 根据目标分类恢复选中态。
 * @param parentId 目标一级分类ID
 * @param categoryId 目标二级分类ID
 */
const applySelection = (
  parentId?: string,
  categoryId?: string,
  options: ApplySelectionOptions = {}
) => {
  const { emitChange = true, keepParentSelection = false } = options
  const firstParent =
    categoryList.value.find(item => item.brandCode === DEFAULT_BRAND_CODE) || categoryList.value[0]
  if (!firstParent) {
    activeParentId.value = ''
    activeCategoryId.value = ''
    expandedParentIds.value = []
    if (emitChange) {
      emit('change', { categoryId: '' })
    }
    return
  }

  const targetParent =
    categoryList.value.find(item => item.id === parentId) ||
    categoryList.value.find(item => item.children?.some(child => child.id === categoryId)) ||
    firstParent

  expandedParentIds.value = [targetParent.id]
  activeParentId.value = targetParent.id

  if (categoryId) {
    const targetChild = targetParent.children?.find(child => child.id === categoryId)
    if (targetChild) {
      activeCategoryId.value = targetChild.id
      if (emitChange) {
        emit('change', { categoryId: targetChild.id })
      }
      return
    }
  }

  if (keepParentSelection) {
    activeCategoryId.value = ''
    if (emitChange) {
      emit('change', { categoryId: targetParent.id })
    }
    return
  }

  const firstChild = targetParent.children?.[0]
  activeCategoryId.value = firstChild?.id || ''
  if (emitChange) {
    emit('change', { categoryId: firstChild?.id || targetParent.id })
  }
}

/**
 * 获取分类树并按目标分类恢复选中态。
 * @param selectedParentId 目标一级分类ID
 * @param selectedCategoryId 目标二级分类ID
 * @param emitChange 是否通知右侧刷新
 */
const refreshCategoryTree = async (
  selectedParentId?: string,
  selectedCategoryId?: string,
  emitChange = true
) => {
  await dataSquareActions.updateCategoryTree()
  applySelection(selectedParentId, selectedCategoryId, { emitChange })
}

/**
 * 仅刷新左树并保留当前选中态，不通知右侧刷新。
 * 新建分类后只需要让左侧展示最新数据，避免打断右侧当前列表上下文。
 */
const refreshCategoryTreeSilently = async (fallbackParentId?: string) => {
  const selectedParentId = activeParentId.value || fallbackParentId
  const selectedCategoryId = activeCategoryId.value
  const keepParentSelection = Boolean(selectedParentId) && !selectedCategoryId

  await dataSquareActions.updateCategoryTree()
  applySelection(selectedParentId, selectedCategoryId, {
    emitChange: false,
    keepParentSelection
  })
}

/**
 * 点击一级分类时只筛选右侧列表，一级不提供编辑删除入口。
 */
const handlePrimaryClick = (item: DataPlazaCategoryItem) => {
  if (isExpanded(item.id)) {
    expandedParentIds.value = expandedParentIds.value.filter(id => id !== item.id)
  } else {
    expandedParentIds.value.push(item.id)
  }

  activeParentId.value = item.id
  activeCategoryId.value = ''
  emit('change', { categoryId: item.id })
}

/**
 * 点击二级分类后通知右侧刷新报告列表。
 */
const handleChildClick = (parent: DataPlazaCategoryItem, child: DataPlazaCategoryItem) => {
  if (!isExpanded(parent.id)) {
    expandedParentIds.value.push(parent.id)
  }

  activeParentId.value = parent.id
  activeCategoryId.value = child.id
  emit('change', { categoryId: child.id })
}

/**
 * 打开分类排序弹框。
 */
const handleOpenSortDialog = () => {
  if (!categoryList.value.length) {
    ElMessage.warning('暂无可排序分类')
    return
  }
  sortDialogVisible.value = true
}

/**
 * 打开新建分类弹框。
 */
const handleCreateCategory = () => {
  categoryDialogMode.value = 'create'
  categoryDialogEditData.value = null
  currentDialogParentId.value =
    activeParentId.value ||
    categoryList.value.find(item => item.brandCode === DEFAULT_BRAND_CODE)?.id ||
    categoryList.value[0]?.id ||
    ''
  categoryDialogVisible.value = true
}

/**
 * 打开编辑分类弹框，仅支持二级分类。
 * @param child 当前二级分类
 */
const handleEditCategory = (child: DataPlazaCategoryItem) => {
  categoryDialogMode.value = 'edit'
  categoryDialogEditData.value = child
  currentDialogParentId.value = child.parentId
  categoryDialogVisible.value = true
}

/**
 * 分类保存成功后刷新左树并恢复选中态。
 * @param payload 选中回填信息
 */
const handleCategoryDialogSuccess = async (payload: {
  selectedParentId: string
  selectedCategoryId: string
}) => {
  if (categoryDialogMode.value === 'create') {
    await refreshCategoryTreeSilently(payload.selectedParentId)
    return
  }

  await refreshCategoryTree(payload.selectedParentId, payload.selectedCategoryId)
}

/**
 * 排序成功后刷新左树，并定位到当前排序的一级分类。
 * @param payload 排序完成后的回填信息
 */
const handleSortSuccess = async (payload: { selectedParentId: string }) => {
  const wasParentSelected =
    activeParentId.value === payload.selectedParentId && !activeCategoryId.value
  const selectedCategoryId =
    activeParentId.value === payload.selectedParentId ? activeCategoryId.value : undefined
  await dataSquareActions.updateCategoryTree()

  if (wasParentSelected) {
    const targetParent =
      categoryList.value.find(item => item.id === payload.selectedParentId) ||
      categoryList.value.find(item => item.brandCode === DEFAULT_BRAND_CODE) ||
      categoryList.value[0]

    if (!targetParent) {
      applySelection()
      return
    }

    expandedParentIds.value = [targetParent.id]
    activeParentId.value = targetParent.id
    activeCategoryId.value = ''
    emit('change', { categoryId: targetParent.id })
    return
  }

  applySelection(payload.selectedParentId, selectedCategoryId)
}

/**
 * 删除二级分类，删除成功后刷新左树并回退到当前一级分类下首个可用项。
 * @param parent 当前一级分类
 * @param child 当前二级分类
 */
const handleDeleteCategory = async (
  parent: DataPlazaCategoryItem,
  child: DataPlazaCategoryItem
) => {
  try {
    await openDataSquareActionConfirm({
      title: '删除分类',
      actionText: '删除',
      targetText: '当前分类'
    })
  } catch {
    return
  }

  try {
    const response = await deleteDataPlazaCategory({
      id: child.id
    })

    if (!response.success) {
      ElMessage.error(response.message || '操作失败')
      return
    }

    ElMessage.success(response.message || '操作成功')
    await refreshCategoryTree(parent.id)
  } catch (error) {
    console.error('删除数据广场分类失败:', error)
    // ElMessage.error(error instanceof Error ? error.message : '操作失败')
  }
}

onMounted(() => {
  void refreshCategoryTree()
})

watch(
  () => props.restoreSelectionVersion,
  value => {
    if (!value || !props.restoreSelection) {
      return
    }
    void refreshCategoryTree(
      props.restoreSelection.selectedParentId,
      props.restoreSelection.selectedCategoryId
    )
  }
)

watch(
  () => props.silentRefreshVersion,
  value => {
    if (!value) {
      return
    }
    void refreshCategoryTreeSilently()
  }
)
</script>

<template>
  <aside class="category-panel">
    <div class="panel-toolbar">
      <div class="text-h3 panel-title">主题分类</div>
      <div class="category-actions">
        <el-button class="sort-btn" @click="handleOpenSortDialog">
          <SvgIcon
            name="direction_swap"
            width="16px"
            height="16px"
            color="currentColor"
            class="mr-6"
          />
          分类排序
        </el-button>
        <el-button type="primary" @click="handleCreateCategory">
          <el-icon class="mr-6"><Plus /></el-icon>
          新建分类
        </el-button>
      </div>
    </div>

    <div class="category-header">分类名称</div>
    <el-scrollbar class="category-scrollbar">
      <div v-for="parent in categoryList" :key="parent.id" class="category-group">
        <div
          class="category-node category-node-primary"
          :class="{ active: activeParentId === parent.id && !activeCategoryId }"
          @click="handlePrimaryClick(parent)"
        >
          <div class="node-main">
            <el-icon class="expand-icon" color="#667085">
              <CaretBottom v-if="isExpanded(parent.id)" />
              <CaretRight v-else />
            </el-icon>
            <img
              class="node-icon brand-icon"
              :src="getParentIcon(parent.brandCode)"
              :alt="parent.categoryName"
            />
            <span class="node-text">
              <span class="node-name">{{ parent.categoryName }}</span>
              <span class="node-count">({{ parent.reportCount }})</span>
            </span>
          </div>
        </div>

        <div
          v-show="isExpanded(parent.id) && parent.children?.length"
          class="category-node-child-layout"
        >
          <div
            v-for="child in parent.children"
            :key="child.id"
            class="category-node category-node-child"
            :class="{ active: activeCategoryId === child.id }"
            @click="handleChildClick(parent, child)"
          >
            <div class="node-main">
              <img
                class="node-icon"
                :src="child.listIconURL || defaultIcon"
                :alt="child.categoryName"
              />
              <span class="node-text">
                <span class="node-name">{{ child.categoryName }}</span>
                <span class="node-count">({{ child.reportCount }})</span>
              </span>
            </div>
            <div class="node-actions">
              <el-icon @click.stop="handleEditCategory(child)"><Edit /></el-icon>
              <el-icon v-if="!child.reportCount" @click.stop="handleDeleteCategory(parent, child)"
                ><Delete
              /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <CategoryDialog
      v-model:visible="categoryDialogVisible"
      :mode="categoryDialogMode"
      :current-parent-id="currentDialogParentId"
      :edit-data="categoryDialogEditData"
      @success="handleCategoryDialogSuccess"
    />
    <SortDialog v-model:visible="sortDialogVisible" @success="handleSortSuccess" />
  </aside>
</template>

<style lang="scss" scoped>
.category-panel {
  width: 376px;
  min-width: 376px;
  padding-right: 24px;
  border-right: 1px solid $border-dark;
  display: flex;
  flex-direction: column;
}

.panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.panel-title {
  font-weight: 600;
  color: #1d2129;
  white-space: nowrap;
}

.category-actions {
  display: flex;
  align-items: center;
  // gap: 12px;
  flex-wrap: wrap;
}

.sort-btn {
  --el-button-text-color: #1d2129;
}

.category-header {
  padding: 8px 16px;
  background: #f2f4f7;
  border-radius: 0px 0px 0px 0px;
  border-bottom: 1px solid #e5e6eb;
  font-family:
    PingFang SC,
    PingFang SC;
  font-weight: 500;
  font-size: 14px;
  color: #1d2129;
  line-height: 22px;
}

.category-scrollbar {
  flex: 1;
  min-height: 0;
}

.category-group {
}

.category-node {
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 400;
  font-size: 14px;
  color: #1d2129;

  &:hover {
    background: #f7f8fa;

    .node-actions {
      display: flex;
    }
  }

  &.active {
    background: #eaf3ff;
    color: #1677ff;

    .node-actions {
      display: flex;
    }

    .node-count {
      color: #1677ff;
    }
  }
}

.category-node-primary {
  height: 45px;
  border-bottom: 1px solid #e5e6eb;
}

.category-node-child {
  height: 40px;
  margin-left: 36px;
}

.node-main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
}

.category-node-child-layout {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
}

.expand-icon,
.child-placeholder {
  width: 16px;
  height: 16px;
  margin-right: 8px;
  flex-shrink: 0;
}

.node-icon {
  width: 28px;
  height: 28px;
  margin-right: 10px;
  object-fit: contain;
  flex-shrink: 0;
}

.brand-icon {
  width: 28px;
  height: 28px;
}

.node-text {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
}

.node-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 1;
  min-width: 0;
}

.node-count {
  margin-left: 6px;
  color: #4e5969;
  flex-shrink: 0;
  white-space: nowrap;
}

.node-actions {
  display: none;
  align-items: center;
  gap: 16px;
  margin-left: 12px;
  color: #929aa6;
  flex-shrink: 0;
}
</style>
