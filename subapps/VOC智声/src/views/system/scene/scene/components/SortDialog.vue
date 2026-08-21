<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import Draggable from 'vuedraggable'
import { getSpecialTypeList, updateSpecialTypeSort } from '@/api/system/scene'

type AnyItem = Record<string, any>

interface Props {
  visible: boolean
  title: string
  /**
   * type：1 一级（分类），2 二级（专区）
   */
  type: number
  /**
   * 列表总数（用于按 total 拉全量数据，避免弹窗内额外查询 total）
   */
  total: number
  /**
   * 二级（专区）排序时必传：当前选中的一级分类 id
   */
  pid?: string
  labelKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  labelKey: 'name',
  total: 0
})

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'success'): void
}>()

const innerVisible = computed({
  get: () => props.visible,
  set: (v: boolean) => emit('update:visible', v)
})

const loading = ref(false)
const list = ref<AnyItem[]>([])

const load = async () => {
  loading.value = true
  try {
    // 二级排序必须绑定到当前一级分类
    if (props.type === 2 && !props.pid) {
      list.value = []
      return
    }

    const total = props.total || 0
    if (total <= 0) {
      list.value = []
      return
    }

    const listRes = await getSpecialTypeList({
      type: props.type,
      pid: props.type === 2 ? props.pid : undefined,
      name: '',
      pageNum: 1,
      pageSize: total
    })

    if (listRes?.success && listRes?.result) {
      list.value = listRes.result.list || []
    } else {
      list.value = []
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => props.visible,
  (v, oldV) => {
    // 只在“从关闭 -> 打开”时加载，避免重复触发请求
    if (v && !oldV) load()
  }
)

const onConfirm = async ({ close }: { close: () => void }) => {
  const ids = list.value.map(it => String(it?.id)).filter(Boolean)
  if (ids.length === 0) {
    ElMessage.warning('暂无可排序数据')
    return
  }

  const idAndSortNo = ids.map((id, index) => `${id},${index + 1}`)
  const res = await updateSpecialTypeSort({
    type: props.type,
    idAndSortNo
  })

  if (!res?.success) {
    ElMessage.error(res?.message || '排序失败')
    return
  }

  ElMessage.success('排序成功')
  close()
  emit('success')
}
</script>

<template>
  <AppDialog v-model:visible="innerVisible" :title="props.title" width="720px" style="max-height: 90%" :confirm="onConfirm">
    <div class="sort-dialog">
      <div v-loading="loading" class="sort-dialog__list">
        <Draggable
          v-model="list"
          item-key="id"
          tag="div"
          class="sort-draggable"
          :disabled="loading"
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
                {{ element?.[props.labelKey] ?? '-' }}
              </div>
              <div class="sort-item__handle" title="长按拖动排序">
                <SvgIcon name="direction_drag" width="20px" height="20px" color="#86909c" />
              </div>
            </div>
          </template>
        </Draggable>
      </div>
    </div>
  </AppDialog>
</template>

<style scoped lang="scss">
.sort-dialog {
  display: flex;
  flex-direction: column;
}

.sort-dialog__list {
  // 保留容器，方便后续扩展（如：空状态/提示文案等）
}

.sort-draggable {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sort-item {
  height: 40px;
  padding: 8px 12px;
  border-radius: 4px;
  background: #F2F4F7;
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
  color: #1D2129;
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

</style>
