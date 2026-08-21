<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, toRaw, watch } from 'vue'
import filterLinesPng from '@/assets/h5/filter-lines.png'
import { usePermissionsStore, useTaskEventStore } from '@h5/store'
import HSelectTabs from '@h5/components/UI/HSelectTabs'
import HFilterMultiSelect from '../HFilterMultiSelect.vue'
import HFilterPersonnelSelect from '../HFilterPersonnelSelect.vue'
import HFilterMainRespOrgCascade from '../HFilterMainRespOrgCascade.vue'
import HFilterCodeExperienceCascade from '../HFilterCodeExperienceCascade.vue'
import HFilterTreeSelect from '../HFilterTreeSelect.vue'
import cloneDeep from 'lodash-es/cloneDeep'

defineOptions({
  name: 'TaskEventFilter'
})

type TaskEventType = 'batch' | 'single'

interface TaskEventFilterConfirmPayload {
  eventType: TaskEventType
  filters: Record<string, any>
}

const props = withDefaults(
  defineProps<{
    /** 当前事件类型：批量事件展示批量条件，单点事件展示原有条件 */
    eventType?: TaskEventType
  }>(),
  {
    eventType: 'batch'
  }
)

const taskEventStore = useTaskEventStore()
const permStore = usePermissionsStore()

// 弹框展示状态
const visible = ref(false)
// 弹层内容就绪（用于把重渲染延后到弹层可见之后，避免点击后“卡一下才弹出”）
const panelReady = ref(false)
let openSeq = 0

const emit = defineEmits<{
  /** 点击查询时返回当前筛选值 */
  confirm: [payload: TaskEventFilterConfirmPayload]
  /** 点击重置时触发 */
  reset: [eventType: TaskEventType]
}>()

const batchFilterForm = reactive({
  /** 事件有效性列表：H5 交互保持单选，提交时按后端数组字段传递 */
  eventValidityList: [] as string[],
  /** 主题分类：多选 value 数组，空数组代表不限 */
  subjectCategoryIds: [] as string[],
  /** 事件优先级：多选 code，空数组代表全部 */
  eventPriorities: [] as string[],
  /** 主责部门ID：级联选择的部门标识 */
  mainRespOrgId: [] as string[],
  /** 处理人员：多选 userId 数组，空数组代表不限 */
  handlerUserIds: [] as string[],
  /** 标准观点：多选 value 数组，空数组代表不限 */
  topicList: [] as string[]
})

const initialBatchForm = cloneDeep(toRaw(batchFilterForm))

// 单点事件筛选表单：保留原有筛选条件与字段结构
const filterForm = reactive({
  /** 事件有效性：单选 code，空字符串代表全部 */
  eventValidity: '' as string,
  /** 主题分类：多选 value 数组，空数组代表不限 */
  subjectCategoryIds: [] as string[],
  /** 事件等级：多选 code，空字符串代表全部 */
  eventLevel: [] as string[],
  /** 事件优先级：多选 code，空字符串代表全部 */
  eventPriorities: [] as string[],
  /** 主责部门ID：级联选择的部门标识 */
  mainRespOrgId: [] as string[],
  /** 处理人员：多选 userId 数组，空数组代表不限 */
  handlerIds: [] as string[],
  /** 事件清晰度：单选 code，空字符串代表全部 */
  eventClarity: '' as string,
  /** 体验代码：一级标签 */
  domTagFirstCode: '' as string,
  /** 体验代码：二级标签 */
  domTagSecondCode: '' as string,
  /** 体验代码：三级标签 */
  domTagThreeCode: '' as string,
  /** 体验代码：四级标签（多选） */
  domTagFourCodes: [] as string[],
  /** 标准观点：多选 value 数组，空数组代表不限 */
  topics: [] as string[],
  /** 事件意图：多选 code，空字符串代表全部 */
  intentions: [] as string[]
})

const initialForm = cloneDeep(toRaw(filterForm))

// 条件数据与加载状态（由 Store 统一管理缓存与并发去重）
const loading = computed(() => taskEventStore.conditionsLoading)
const conditions = computed(() => taskEventStore.conditions)
const allDictItems = computed(() => taskEventStore.allDictItems)
const departAccountTree = computed(() => taskEventStore.departAccountTree)
const departAccountTreeLoading = computed(() => taskEventStore.departAccountTreeLoading)
const batchRuleCategoryTree = computed(() => taskEventStore.batchRuleCategoryTree)
const batchRuleCategoryTreeLoading = computed(() => taskEventStore.batchRuleCategoryTreeLoading)
const isBatchEventType = computed(() => props.eventType === 'batch')

const batchEventValidity = computed<string>({
  get: () => batchFilterForm.eventValidityList[0] || '',
  set: value => {
    batchFilterForm.eventValidityList = value ? [String(value)] : []
  }
})

// 当前账号部门：用于“当前部门分支优先展示”（仅用于排序与聚焦，不会默认选中）
const currentDeptId = computed(() => String(permStore.finalDeptId || ''))
const currentDeptCode = computed(() => String(permStore.finalDeptCode || ''))

// 全领域业务标签树（代码体验数据源）
const codeExperienceTagTree = computed(() => {
  const list = (conditions.value as any)?.tagTreeList
  return Array.isArray(list) ? list : []
})

// 事件有效性选项
const eventValidityOptions = computed(() => {
  return (
    (allDictItems.value?.task_event_validity || []).filter(
      (item: any) => item.value != 'long-standing'
    ) || []
  )
})

// 事件清晰度选项
const eventClarityOptions = computed(() => {
  return (conditions.value?.eventClears || []).map((item: any) => ({
    ...item,
    name: `${item.name || ''}类`
  }))
})

// 标准观点（topics）数据源：随“代码体验”联动（体验代码有值时通过接口按最末级 code 拉取）
// 体验代码最末级代码集合：四级 > 三级 > 二级 > 一级
const experienceLastLevelCodes = computed<string[]>(() => {
  const fourCodes = Array.isArray(filterForm.domTagFourCodes)
    ? filterForm.domTagFourCodes.filter(Boolean).map(String)
    : []
  if (fourCodes.length > 0) return fourCodes
  if (filterForm.domTagThreeCode) return [String(filterForm.domTagThreeCode)]
  if (filterForm.domTagSecondCode) return [String(filterForm.domTagSecondCode)]
  if (filterForm.domTagFirstCode) return [String(filterForm.domTagFirstCode)]
  return []
})

const topicsBatchKey = computed(() =>
  taskEventStore.getTopicsBatchKey(experienceLastLevelCodes.value)
)

const topicsLoading = computed(() => {
  if (experienceLastLevelCodes.value.length === 0) return false
  return taskEventStore.isTopicsBatchLoadingByKey(topicsBatchKey.value)
})

// 标准观点选项：根据是否选择体验代码决定数据源
const topicsOptions = computed<any[]>(() => {
  // 未选择体验代码：沿用 conditions 下发的全量观点
  if (experienceLastLevelCodes.value.length === 0) {
    const list = (conditions.value as any)?.topicList
    return Array.isArray(list) ? list : []
  }
  return taskEventStore.getTopicsBatchOptionsByKey(topicsBatchKey.value)
})

// 批量事件标准观点不做体验代码联动，直接使用条件接口下发的全量观点
const batchTopicsOptions = computed<any[]>(() => {
  const list = (conditions.value as any)?.topicList
  return Array.isArray(list) ? list : []
})

// 标准观点占位符文本
const topicsPlaceholder = computed(() => {
  if (experienceLastLevelCodes.value.length > 0 && topicsLoading.value) return '加载中...'
  if (experienceLastLevelCodes.value.length > 0 && topicsOptions.value.length === 0)
    return '暂无可选项'
  return '请选择'
})

// 标准观点是否禁用：加载中时禁用
const topicsDisabled = computed(
  () => experienceLastLevelCodes.value.length > 0 && topicsLoading.value
)

// 监听体验代码变化，自动获取对应的标准观点
watch(
  () => experienceLastLevelCodes.value,
  (codes, oldCodes) => {
    // flush=post：等待 CodeExperience 级联内部同步完成，减少中间态重复请求
    if (Array.isArray(oldCodes)) {
      const oldKey = taskEventStore.getTopicsBatchKey(oldCodes)
      const newKey = taskEventStore.getTopicsBatchKey(codes)
      if (oldKey !== newKey) {
        // 体验代码变化：直接清空“标准观点”的已选
        filterForm.topics = []
      }
    }

    if (Array.isArray(codes) && codes.length > 0) {
      taskEventStore.fetchTopicsBatchByCodes(codes)
    }
  },
  { deep: true, flush: 'post', immediate: true }
)

onMounted(() => {
  taskEventStore.fetchTaskEventConditions()
  taskEventStore.fetchBatchRuleCategoryTree()
  taskEventStore.fetchDepartAccountTree()
  taskEventStore.fetchSysAllDictItems()
})

const waitNextFrame = () =>
  new Promise<void>(resolve => {
    const raf =
      window.requestAnimationFrame ||
      ((cb: FrameRequestCallback) => window.setTimeout(() => cb(Date.now() as any), 16))
    raf(() => resolve())
  })

const schedulePanelReady = async () => {
  const seq = ++openSeq
  panelReady.value = false
  await nextTick()
  // 让弹层与遮罩先完成一次绘制，再挂载重组件
  await waitNextFrame()
  await waitNextFrame()
  if (seq !== openSeq) return
  panelReady.value = true
}

/**
 * 打开筛选弹层
 */
const handleOpen = () => {
  visible.value = true
  schedulePanelReady()
}

/**
 * 关闭筛选弹层
 */
const handleClose = () => {
  visible.value = false
  openSeq++
  panelReady.value = false
}

/**
 * 重置表单到初始状态
 * 重置后实现“清空筛选”效果
 */
const handleReset = () => {
  if (isBatchEventType.value) {
    Object.assign(batchFilterForm, cloneDeep(initialBatchForm))
  } else {
    Object.assign(filterForm, cloneDeep(initialForm))
  }
  emit('reset', props.eventType)
}

/**
 * 提交表单筛选条件
 * 将当前筛选值传递给父组件处理
 */
const handleSubmit = () => {
  // 这里必须做一次深拷贝，避免把 reactive 内部的数组/对象引用透传给父组件：
  // - 否则父组件 requestParams 与弹层内表单会共享引用
  // - 用户在未点击“查询”前修改筛选项时，可能触发列表 watch 深度监听导致意外刷新
  const currentForm = isBatchEventType.value ? batchFilterForm : filterForm
  emit('confirm', {
    eventType: props.eventType,
    filters: cloneDeep(toRaw(currentForm))
  })
  handleClose()
}
</script>

<template>
  <div class="task-event-filter">
    <!-- 筛选入口按钮：使用产品提供的 filter-lines 图标 -->
    <div class="filter-entry" @click="handleOpen">
      <van-image
        class="filter-entry__icon"
        :src="filterLinesPng"
        width="24"
        height="24"
        fit="contain"
      />
    </div>

    <!-- 筛选弹层：底部弹出，包含所有筛选条件 -->
    <van-popup
      v-model:show="visible"
      position="bottom"
      round
      :safe-area-inset-bottom="true"
      :style="{
        minHeight: '40%',
        maxHeight: '95%',
        overflow: 'hidden',
        display: 'flex',
        flex: '1',
        width: '100%'
      }"
      teleport="body"
    >
      <div class="task-event-filter__panel flex-1 overflow-hidden">
        <div class="panel-body h-full">
          <!-- 加载状态 -->
          <div v-if="loading || !panelReady" class="panel-loading flex-center">
            <van-loading size="24px" color="#1677FF">加载筛选条件...</van-loading>
          </div>
          <!-- 筛选表单 -->
          <div v-else class="panel-form h-full overflow-hidden">
            <van-form @submit="handleSubmit" class="h-full flex-col">
              <div class="filter-form flex-1 overflow-y-auto">
                <template v-if="isBatchEventType">
                  <!-- 批量事件筛选：本阶段只保留产品截图中的核心查询条件 -->
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">事件有效性</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="batchEventValidity"
                        :options="eventValidityOptions"
                        :fields="{
                          name: 'text',
                          code: 'value'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">主题分类</div>
                    <div class="filter-form-item__control">
                      <HFilterTreeSelect
                        v-model="batchFilterForm.subjectCategoryIds"
                        title="主题分类"
                        placeholder="请选择"
                        :options="batchRuleCategoryTree"
                        :loading="batchRuleCategoryTreeLoading"
                        leaf-only
                        :fields="{
                          label: 'name',
                          value: 'id',
                          children: 'children'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">处理优先级</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="batchFilterForm.eventPriorities"
                        :options="allDictItems?.closed_rule_priority || []"
                        multiSelect
                        :fields="{
                          name: 'text',
                          code: 'value'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">主责部门</div>
                    <div class="filter-form-item__control">
                      <HFilterMainRespOrgCascade
                        v-model="batchFilterForm.mainRespOrgId"
                        :options="departAccountTree"
                        :loading="departAccountTreeLoading"
                        :currentDeptId="currentDeptId"
                        :currentDeptCode="currentDeptCode"
                        :fields="{
                          label: 'name',
                          value: 'id',
                          children: 'child'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">处理人员</div>
                    <div class="filter-form-item__control">
                      <HFilterPersonnelSelect
                        v-model="batchFilterForm.handlerUserIds"
                        title="处理人员"
                        placeholder="请选择"
                        :options="departAccountTree"
                        :loading="departAccountTreeLoading"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">标准观点</div>
                    <div class="filter-form-item__control">
                      <HFilterMultiSelect
                        v-model="batchFilterForm.topicList"
                        title="标准观点"
                        placeholder="请选择"
                        :options="batchTopicsOptions"
                        pinMode="selected"
                        :fields="{
                          label: 'tagName',
                          value: 'tagName'
                        }"
                      />
                    </div>
                  </div>
                </template>
                <template v-else>
                  <!-- 事件有效性筛选 -->
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">事件有效性</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="filterForm.eventValidity"
                        :options="eventValidityOptions"
                        :fields="{
                          name: 'text',
                          code: 'value'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">主题分类</div>
                    <div class="filter-form-item__control">
                      <HFilterMultiSelect
                        v-model="filterForm.subjectCategoryIds"
                        title="主题分类"
                        placeholder="请选择"
                        :options="conditions?.closedLoopCategory || []"
                        :fields="{
                          label: 'name',
                          value: 'id'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">事件等级</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="filterForm.eventLevel"
                        :options="allDictItems?.closed_rule_level || []"
                        multiSelect
                        :fields="{
                          name: 'text',
                          code: 'value'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">处理优先级</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="filterForm.eventPriorities"
                        :options="allDictItems?.closed_rule_priority || []"
                        multiSelect
                        :fields="{
                          name: 'text',
                          code: 'value'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">主责部门</div>
                    <div class="filter-form-item__control">
                      <HFilterMainRespOrgCascade
                        v-model="filterForm.mainRespOrgId"
                        :options="departAccountTree"
                        :loading="departAccountTreeLoading"
                        :currentDeptId="currentDeptId"
                        :currentDeptCode="currentDeptCode"
                        :fields="{
                          label: 'name',
                          value: 'id',
                          children: 'child'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">处理人员</div>
                    <div class="filter-form-item__control">
                      <HFilterPersonnelSelect
                        v-model="filterForm.handlerIds"
                        title="处理人员"
                        placeholder="请选择"
                        :options="departAccountTree"
                        :loading="departAccountTreeLoading"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">事件清晰度</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="filterForm.eventClarity"
                        :options="eventClarityOptions"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">代码体验</div>
                    <div class="filter-form-item__control">
                      <HFilterCodeExperienceCascade
                        v-model:firstCode="filterForm.domTagFirstCode"
                        v-model:secondCode="filterForm.domTagSecondCode"
                        v-model:thirdCode="filterForm.domTagThreeCode"
                        v-model:fourCodes="filterForm.domTagFourCodes"
                        :options="codeExperienceTagTree"
                        :fields="{
                          label: 'tagName',
                          value: 'tagCode',
                          children: 'child'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">标准观点</div>
                    <div class="filter-form-item__control">
                      <HFilterMultiSelect
                        v-model="filterForm.topics"
                        title="标准观点"
                        :placeholder="topicsPlaceholder"
                        :options="topicsOptions"
                        :disabled="topicsDisabled"
                        pinMode="selected"
                        :fields="{
                          label: 'tagName',
                          value: 'tagName'
                        }"
                      />
                    </div>
                  </div>
                  <div class="filter-form-item">
                    <div class="filter-form-item__label">意图</div>
                    <div class="filter-form-item__control">
                      <HSelectTabs
                        v-model="filterForm.intentions"
                        :options="allDictItems?.voc_intention || []"
                        multiSelect
                        :fields="{
                          name: 'text',
                          code: 'value'
                        }"
                      />
                    </div>
                  </div>
                </template>
                <!-- 其他筛选项后续在此补充 -->
              </div>
              <div class="filter-actions">
                <van-button
                  class="filter-actions__btn"
                  block
                  type="default"
                  @click.prevent="handleReset"
                  style="background: #f2f3f5; border-color: #f2f3f5"
                >
                  重置
                </van-button>
                <van-button class="filter-actions__btn" block type="primary" native-type="submit">
                  查询
                </van-button>
              </div>
            </van-form>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
.task-event-filter {
  display: inline-flex;
  align-items: center;
}

.filter-entry {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.filter-entry__icon {
  display: block;
}

.task-event-filter__panel {
  padding: 12px 16px 24px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
}

.panel-title {
  font-weight: 500;
  font-size: 16px;
  color: #1f2733;
}

.panel-close {
  font-size: 14px;
  color: #1677ff;
}

.panel-body {
  padding-top: 8px;
}

.panel-loading {
  min-height: 120px;
}

.panel-form {
  min-height: 120px;
  overflow: auto;
}

.filter-form {
  padding: 8px 0 12px;
}

.filter-form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  gap: 16px;
}

.filter-form-item__label {
  flex: none;
  min-width: 65px;
  font-weight: 400;
  text-align: right;
  font-size: 12px;
  color: #1d2129;
  line-height: 22px;
}

.filter-form-item__control {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: flex-end;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.filter-actions__btn {
  flex: 1;
  height: 36px;
  border-radius: 2px;
}
</style>
