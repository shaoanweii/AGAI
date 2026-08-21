<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import Draggable from 'vuedraggable'
import AppDialog from '@/components/AppDialog.vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { updateDataPlazaCategorySort } from '@/api/dataPlaza'
import type { DataPlazaCategorySortItem } from '@/api/dataPlaza/types'
import { dataSquareStore } from '../store'

defineOptions({
  name: 'DataSquareSortDialog'
})

interface SortDialogItem {
  id: string
  categoryName: string
  sortNo: number
}

const DEFAULT_BRAND_CODE = 'A01'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', payload: { selectedParentId: string }): void
}>()

const innerVisible = computed({
  get: () => props.visible,
  set: value => emit('update:visible', value)
})

const activeParentId = ref('')
const list = ref<SortDialogItem[]>([])
const submitting = ref(false)
const sortDraftMap = ref<Record<string, SortDialogItem[]>>({})

const parentOptions = computed(() => dataSquareStore.categoryTree || [])

/**
 * 根据一级分类 ID 获取排序列表。
 * @param parentId 一级分类 ID
 * @returns 可拖拽的二级分类列表
 */
const buildChildList = (parentId: string) => {
  const currentParent = parentOptions.value.find(item => item.id === parentId)
  return (currentParent?.children || []).map(child => ({
    id: child.id,
    categoryName: child.categoryName,
    sortNo: child.sortNo
  }))
}

/**
 * 复制排序列表，避免缓存草稿时与当前拖拽列表共用同一引用。
 * @param items 当前排序列表
 * @returns 独立的排序列表副本
 */
const cloneSortList = (items: SortDialogItem[]) => {
  return items.map(item => ({
    ...item
  }))
}

/**
 * 获取一级分类当前有效的排序列表。
 * 优先使用弹窗会话内草稿，未命中时回退接口树中的原始顺序。
 * @param parentId 一级分类 ID
 * @returns 当前有效排序列表
 */
const getEffectiveSortList = (parentId: string) => {
  return cloneSortList(sortDraftMap.value[parentId] || buildChildList(parentId))
}

/**
 * 计算弹窗打开时默认选中的一级分类。
 * 优先使用 A01，对应品牌不存在时回退第一个一级分类。
 * @returns 一级分类 ID
 */
const getDefaultParentId = () => {
  return (
    parentOptions.value.find(item => item.brandCode === DEFAULT_BRAND_CODE)?.id ||
    parentOptions.value[0]?.id ||
    ''
  )
}

/**
 * 保存当前一级分类的排序草稿，确保切换 tab 后仍可恢复未提交顺序。
 * @param parentId 一级分类 ID
 */
const persistSortDraft = (parentId: string) => {
  if (!parentId) return
  sortDraftMap.value[parentId] = cloneSortList(list.value)
}

/**
 * 同步当前一级分类下的二级分类排序列表。
 * 优先使用当前弹窗会话内的排序草稿，未命中时再回退接口树顺序。
 * @param parentId 一级分类 ID
 */
const syncListByParentId = (parentId: string) => {
  activeParentId.value = parentId
  list.value = getEffectiveSortList(parentId)
}

/**
 * 切换一级分类时刷新拖拽列表。
 * @param parentId 一级分类 ID
 */
const handleParentChange = (parentId: string) => {
  if (!parentId || parentId === activeParentId.value) return
  persistSortDraft(activeParentId.value)
  syncListByParentId(parentId)
}

/**
 * 弹窗打开时初始化默认选中品牌及其二级分类。
 */
const initDialog = () => {
  sortDraftMap.value = {}
  const defaultParentId = getDefaultParentId()
  syncListByParentId(defaultParentId)
}

/**
 * 根据一级分类构造扁平化排序项。
 * 每个二级分类输出一条独立记录，匹配后端最新批量更新协议。
 * @param parentId 一级分类 ID
 * @param brandCode 品牌编码
 * @param items 二级分类列表
 * @returns 排序项数组
 */
const buildSortItemsByList = (
  parentId: string,
  brandCode: string,
  items: SortDialogItem[]
): DataPlazaCategorySortItem[] => {
  return items.map((item, index) => ({
    id: item.id,
    parentId,
    brandCode,
    sortNo: index + 1
  }))
}

/**
 * 判断一级分类下的二级分类排序是否发生变化。
 * 仅比较当前顺序与接口树原始顺序，顺序一致则视为未改动。
 * @param parentId 一级分类 ID
 * @param currentSortList 当前有效排序列表
 * @returns 是否发生变化
 */
const hasSortChanged = (parentId: string, currentSortList: SortDialogItem[]) => {
  const originalSortList = buildChildList(parentId)

  if (originalSortList.length !== currentSortList.length) {
    return true
  }

  return currentSortList.some((item, index) => item.id !== originalSortList[index]?.id)
}

/**
 * 组装批量排序提交参数。
 * 仅提交二级排序实际发生变化的一级分类，并转换为扁平数组结构。
 * @returns 批量排序参数
 */
const buildBatchSortParams = (): DataPlazaCategorySortItem[] => {
  return parentOptions.value
    .flatMap(parent => {
      const currentSortList =
        parent.id === activeParentId.value ? cloneSortList(list.value) : getEffectiveSortList(parent.id)

      if (!currentSortList.length || !hasSortChanged(parent.id, currentSortList)) {
        return []
      }

      return buildSortItemsByList(parent.id, parent.brandCode, currentSortList)
    })
}

/**
 * 提交所有一级分类下的二级分类排序结果。
 * @param close 关闭弹框方法
 */
const handleConfirm = async ({ close }: { close: () => void }) => {
  if (submitting.value) return

  if (!activeParentId.value) {
    ElMessage.warning('暂无可排序分类')
    return
  }

  persistSortDraft(activeParentId.value)

  const batchSortParams = buildBatchSortParams()

  if (!batchSortParams.length) {
    ElMessage.warning('请排序')
    return
  }

  submitting.value = true
  try {
    const response = await updateDataPlazaCategorySort(batchSortParams)

    if (!response.success) {
      ElMessage.error(response.message || '操作失败')
      return
    }

    ElMessage.success(response.message || '操作成功')
    emit('success', { selectedParentId: activeParentId.value })
    close()
  } catch (error) {
    console.error('更新数据广场分类排序失败:', error)
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.visible,
  value => {
    if (value) {
      initDialog()
    }
  }
)
</script>

<template>
  <AppDialog v-model:visible="innerVisible" width="720px" destroy-on-close :confirm="handleConfirm">
    <template #header>分类排序</template>

    <div class="sort-dialog">
      <div class="sort-dialog__tabs">
        <div
          v-for="parent in parentOptions"
          :key="parent.id"
          class="sort-dialog__tab"
          :class="{ active: activeParentId === parent.id }"
          @click="handleParentChange(parent.id)"
        >
          {{ parent.categoryName }}
        </div>
      </div>

      <div class="sort-dialog__list">
        <Draggable
          v-model="list"
          item-key="id"
          tag="div"
          class="sort-dialog__draggable"
          :animation="200"
          ghost-class="sort-item--ghost"
          chosen-class="sort-item--chosen"
          drag-class="sort-item--drag"
          :delay="300"
          :delay-on-touch-only="true"
          :touch-start-threshold="8"
        >
          <template #item="{ element }">
            <div class="sort-item">
              <div class="sort-item__label">
                {{ element.categoryName || '-' }}
              </div>
              <div class="sort-item__handle" title="长按拖动排序">
                <SvgIcon name="direction_drag" width="20px" height="20px" color="#86909c" />
              </div>
            </div>
          </template>
        </Draggable>

        <el-empty
          v-if="!list.length"
          description="暂无数据"
          :image-size="80"
          class="sort-dialog__empty"
        />
      </div>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.sort-dialog {
  display: flex;
  flex-direction: column;
}

.sort-dialog__tabs {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.sort-dialog__tab {
  min-width: 94px;
  height: 34px;
  padding: 0 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 14px;
  color: #4e5969;
  line-height: 22px;
  cursor: pointer;
}

.sort-dialog__tab.active {
  border-color: #165dff;
  background: #165dff;
  color: #fff;
}

.sort-dialog__list {
  min-height: 320px;
}

.sort-dialog__draggable {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sort-item {
  height: 40px;
  padding: 8px 12px;
  border-radius: 4px;
  background: #f2f4f7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  user-select: none;
  cursor: grab;
}

.sort-item:active {
  cursor: grabbing;
}

.sort-item--ghost {
  opacity: 0.6;
}

.sort-item--chosen {
  background: #eaf3ff;
}

.sort-item__label {
  flex: 1 1 auto;
  min-width: 0;
  font-weight: 500;
  font-size: 14px;
  color: #1d2129;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sort-item__handle {
  flex: 0 0 auto;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  pointer-events: none;
}

.sort-dialog__empty {
  min-height: 240px;
}
</style>
