<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { ArrowUp, ArrowDown, Delete } from '@element-plus/icons-vue'
import useSingleEventStore from '@/store/modules/singleEvent'
import type { SingleEventDetailBaseVo } from '@/api/singlePointEvent/types'
import { debounce } from 'lodash-es'
import { openWindow } from '@/utils'
import { EventType } from '../../ehConstants'
import { ElMessage } from 'element-plus'
import { getTopicsByTagId, findAllUpTagLibHierarchicalByTagId } from '@/api/singlePointEvent'
import CorrectionApplyDialog from './CorrectionApplyDialog.vue'

defineOptions({
  name: 'OriginalSoundDetails'
})

const { row, eventType, currentEvent, startTime, endTime } = defineProps<{
  row: any
  eventType: EventType
  currentEvent?: any
  startTime?: string
  endTime?: string
}>()
const emits = defineEmits(['getOriginalSoundDetails', 'refreshDetailEvents'])
const singleEventStore = useSingleEventStore()

type IntentionSnapshot = {
  intentionType: string
  domTagFirstCode: string
  domTagSecondCode: string
  domTagThreeCode: string
  domTagFourCode: string
  topic: string
}

const formData = ref<SingleEventDetailBaseVo>({
  dataId: undefined,
  commentUserName: undefined,
  commentUserId: undefined,
  commentTime: undefined,
  commentDetails: undefined,
  isMainPost: undefined,
  postUserId: undefined,
  postUserName: undefined,
  postTime: undefined,
  mainPostUrl: undefined,
  mainPostTitle: undefined,
  mainPostDetails: undefined,
  brandCode: undefined,
  brandName: undefined,
  carSeriesCode: undefined,
  editPermission: false
})

const originalIntentionSnapshots = ref<Record<string, IntentionSnapshot>>({})
const originalIntentionSnapshotsByIndex = ref<IntentionSnapshot[]>([])

const normalizeSnapshotValue = (value?: string) => value ?? ''

const buildIntentionSnapshot = (item: any): IntentionSnapshot => {
  return {
    intentionType: normalizeSnapshotValue(item.intentionType),
    domTagFirstCode: normalizeSnapshotValue(item.domTagFirstCode),
    domTagSecondCode: normalizeSnapshotValue(item.domTagSecondCode),
    domTagThreeCode: normalizeSnapshotValue(item.domTagThreeCode),
    domTagFourCode: normalizeSnapshotValue(item.domTagFourCode),
    topic: normalizeSnapshotValue(item.topic)
  }
}

const cacheOriginalIntentions = (intentions: any[] = []) => {
  const map: Record<string, IntentionSnapshot> = {}
  const list: IntentionSnapshot[] = []
  intentions.forEach(item => {
    const snapshot = buildIntentionSnapshot(item)
    list.push(snapshot)
    if (item?.id) {
      map[item.id] = snapshot
    }
  })
  originalIntentionSnapshots.value = map
  originalIntentionSnapshotsByIndex.value = list
}

// 是否为评论, 0是评论, 非评论的 其他值都是帖子。 1 | null
const isPost = computed(() => {
  return formData.value.isMainPost === '0' || formData.value.isMainPost === 'N'
})

const formWrapperRef = ref<HTMLElement | null>(null)
const firstIntentionItemRef = ref<HTMLElement | null>(null)
const collapsedContentHeight = ref(0)
const fullContentHeight = ref(0)
let formContentResizeObserver: ResizeObserver | null = null

// 控制详情展开/收起状态，弹窗首次打开默认收起
const isExpanded = ref(false)

/**
 * @description 记录首条用户意图节点，用于计算默认收起高度
 * @param {Element | ComponentPublicInstance | null} element 首条用户意图节点
 * @return {void}
 */
const setFirstIntentionItemRef = (element: Element | ComponentPublicInstance | null) => {
  const targetElement =
    element instanceof Element
      ? element
      : ((element?.$el as Element | undefined) ?? null)

  firstIntentionItemRef.value = targetElement as HTMLElement | null
}

/**
 * @description 计算收起态与展开态的最大高度
 * 1. 始终记录完整内容高度，保证整块展开时显示完整内容
 * 2. 收起态优先锚定到首条用户意图底部，满足“默认展示到第一条用户意图”的需求
 * 3. 当不存在用户意图时，回退到完整高度，避免出现异常截断
 * @return {void}
 */
const updateContentHeights = () => {
  const wrapperElement = formWrapperRef.value
  if (!wrapperElement) return

  fullContentHeight.value = wrapperElement.scrollHeight

  const firstIntentionElement = firstIntentionItemRef.value
  if (!firstIntentionElement || !wrapperElement.contains(firstIntentionElement)) {
    collapsedContentHeight.value = fullContentHeight.value
    return
  }

  const wrapperRect = wrapperElement.getBoundingClientRect()
  const firstIntentionRect = firstIntentionElement.getBoundingClientRect()
  const paddingBottom = Number.parseFloat(window.getComputedStyle(wrapperElement).paddingBottom || '0')
  const nextCollapsedHeight = Math.ceil(firstIntentionRect.bottom - wrapperRect.top + paddingBottom)

  collapsedContentHeight.value = Math.min(
    fullContentHeight.value,
    Math.max(nextCollapsedHeight, 0)
  )
}

/**
 * @description 等待 DOM 更新后同步折叠高度，确保异步详情数据渲染完成后再测量
 * @return {Promise<void>}
 */
const syncContentHeights = async () => {
  await nextTick()
  updateContentHeights()
}

// 切换展开/收起状态
const toggleExpand = async () => {
  if (isExpanded.value) {
    await syncContentHeights()
  }
  isExpanded.value = !isExpanded.value
}

const formWrapperStyle = computed(() => {
  const targetHeight = isExpanded.value ? fullContentHeight.value : collapsedContentHeight.value
  if (!targetHeight) return undefined
  return {
    maxHeight: `${targetHeight}px`
  }
})

const showToggleButton = computed(() => {
  return fullContentHeight.value > collapsedContentHeight.value
})

// 根据品牌获取车系数据
const carSeriesOptions = computed(() => {
  if (!formData.value.brandCode) return []
  return singleEventStore.getCarSeriesOptionsByBrand(formData.value.brandCode!)
})

// 获取原声详情
const getDetail = async () => {
  formData.value = await singleEventStore.fetchGetDetailBase({
    dataId: row.dataId,
    id: row.id
  })
  firstIntentionItemRef.value = null

  formData.value.carSeriesCode = formData.value.carSeriesCode ?? formData.value.carSeriesName

  // 为每个意图项加载标准观点选项
  if (formData.value.intentions && formData.value.intentions.length > 0) {
    await Promise.all(
      formData.value.intentions.map(async item => {
        if (item.domTagFourCode) {
          // 有四级标签时，根据四级标签获取标准观点
          await fetchTopicsByTagId(item.domTagFourCode, item)
        } else {
          // 没有四级标签时，检查是否体验代码全部为空，如果为空则获取所有标准观点
          await checkAndFetchAllTopics(item)
        }
      })
    )
  }

  cacheOriginalIntentions(formData.value.intentions || [])
  emits('getOriginalSoundDetails', formData.value)
  await syncContentHeights()
}

const handleCorrectionSuccess = async () => {
  await getDetail()
  emits('refreshDetailEvents')
}

// 获取主题分类各级选项
const getTagOptions = (level: number, item: any) => {
  if (level === 1) return singleEventStore.tagTreeList
  if (level === 2) return singleEventStore.getTagChildren(item.domTagFirstCode)
  if (level === 3) return singleEventStore.getTagChildren(item.domTagSecondCode)
  if (level === 4) return singleEventStore.getTagChildren(item.domTagThreeCode)
  return []
}

// 主题分类change事件
const handleTagChange = async (level: number, item: any) => {
  if (level === 1) {
    item.domTagSecondCode = undefined
    item.domTagThreeCode = undefined
    item.domTagFourCode = undefined
    item.topic = undefined
    item.topicOptions = []
  } else if (level === 2) {
    item.domTagThreeCode = undefined
    item.domTagFourCode = undefined
    item.topic = undefined
    item.topicOptions = []
  } else if (level === 3) {
    item.domTagFourCode = undefined
    item.topic = undefined
    item.topicOptions = []
  } else if (level === 4) {
    // 四级标签变化时，根据四级标签ID获取标准观点列表
    item.topic = undefined
    if (item.domTagFourCode) {
      await fetchTopicsByTagId(item.domTagFourCode, item)
    } else {
      item.topicOptions = []
    }
  }

  // 当体验代码全部为空时，获取所有标准观点
  await checkAndFetchAllTopics(item)
}

// 检查体验代码是否全部为空，如果为空则获取所有标准观点
const checkAndFetchAllTopics = async (item: any) => {
  const isAllEmpty =
    !item.domTagFirstCode && !item.domTagSecondCode && !item.domTagThreeCode && !item.domTagFourCode

  if (isAllEmpty) {
    // 体验代码全部为空时，获取所有标准观点
    try {
      const res = await getTopicsByTagId([])
      if (res.success && res.result) {
        item.topicOptions = res.result
      } else {
        item.topicOptions = []
      }
    } catch (error) {
      console.error('获取所有标准观点失败:', error)
      item.topicOptions = []
    }
  }
}

// 根据四级标签ID获取标准观点列表
const fetchTopicsByTagId = async (tagCode: string, item: any) => {
  try {
    const res = await getTopicsByTagId([tagCode])
    if (res.success && res.result) {
      // 将标准观点列表存储在当前行的item中
      item.topicOptions = res.result
    }
  } catch (error) {
    console.error('获取标准观点失败:', error)
    item.topicOptions = []
  }
}

// 标准观点选择后，调用接口回显标签
const handleTopicChange = async (item: any) => {
  if (!item.topic) {
    // 如果清空了标准观点，检查是否需要获取所有观点
    await checkAndFetchAllTopics(item)
    return
  }

  // 如果体验代码已经有值，则无需调用接口回显标签
  const hasExperienceCode =
    item.domTagFirstCode || item.domTagSecondCode || item.domTagThreeCode || item.domTagFourCode

  if (hasExperienceCode) {
    return
  }

  try {
    // 调用接口获取标签层级信息
    const res = await findAllUpTagLibHierarchicalByTagId(item.topic)
    if (res.success && res.result && res.result.length > 0) {
      const categoryInfo = res.result[0] // 取第一个结果

      // 回显标签
      if (categoryInfo.firstCode) {
        item.domTagFirstCode = categoryInfo.firstCode
      }
      if (categoryInfo.secondCode) {
        item.domTagSecondCode = categoryInfo.secondCode
      }
      if (categoryInfo.thirdCode) {
        item.domTagThreeCode = categoryInfo.thirdCode
      }
      if (categoryInfo.fourthCode) {
        item.domTagFourCode = categoryInfo.fourthCode
        // 回显四级标签后，根据四级标签重新获取对应的标准观点列表
        await fetchTopicsByTagId(categoryInfo.fourthCode, item)
      }
    }
  } catch (error) {
    console.error('获取标签层级信息失败:', error)
  }
}

// 用户意图change事件
const handleIntentionChange = async (item: any) => {
  item.domTagFirstCode = undefined
  item.domTagSecondCode = undefined
  item.domTagThreeCode = undefined
  item.domTagFourCode = undefined
  item.topic = undefined
  item.topicOptions = []

  // 当体验代码全部为空时，获取所有标准观点
  await checkAndFetchAllTopics(item)
}

const getOriginalSnapshot = (item: any, index: number) => {
  if (item?.id && originalIntentionSnapshots.value[item.id]) {
    return originalIntentionSnapshots.value[item.id]
  }
  return originalIntentionSnapshotsByIndex.value[index]
}

const isIntentionUpdated = (item: any, index: number) => {
  const originalSnapshot = getOriginalSnapshot(item, index)
  if (!originalSnapshot) return false
  const currentSnapshot = buildIntentionSnapshot(item)
  return (
    currentSnapshot.intentionType !== originalSnapshot.intentionType ||
    currentSnapshot.domTagFirstCode !== originalSnapshot.domTagFirstCode ||
    currentSnapshot.domTagSecondCode !== originalSnapshot.domTagSecondCode ||
    currentSnapshot.domTagThreeCode !== originalSnapshot.domTagThreeCode ||
    currentSnapshot.domTagFourCode !== originalSnapshot.domTagFourCode ||
    currentSnapshot.topic !== originalSnapshot.topic
  )
}

const correctionDialogVisible = ref(false)
const correctionRowIndex = ref<number | null>(null)

const validateCorrectionItem = (item: any, index: number) => {
  if (!item.intentionType) {
    ElMessage.warning(`第${index + 1}行用户意图不能为空`)
    return false
  }
  if (!item.domTagFirstCode) {
    ElMessage.warning(`第${index + 1}行体验代码一级不能为空`)
    return false
  }
  if (!item.domTagSecondCode) {
    ElMessage.warning(`第${index + 1}行体验代码二级不能为空`)
    return false
  }
  if (!item.domTagThreeCode) {
    ElMessage.warning(`第${index + 1}行体验代码三级不能为空`)
    return false
  }
  if (!item.domTagFourCode) {
    ElMessage.warning(`第${index + 1}行体验代码四级不能为空`)
    return false
  }
  if (!item.topic) {
    ElMessage.warning(`第${index + 1}行标准观点不能为空`)
    return false
  }
  return true
}

const openCorrectionDialog = (index: number) => {
  const item = formData.value.intentions?.[index]
  if (!item) return
  if (!validateCorrectionItem(item, index)) return
  correctionRowIndex.value = index
  correctionDialogVisible.value = true
}

const correctionDialogItem = computed(() => {
  if (correctionRowIndex.value === null) return null
  return formData.value.intentions?.[correctionRowIndex.value] || null
})

const correctionDialogOriginalSnapshot = computed(() => {
  if (correctionRowIndex.value === null) return null
  const item = formData.value.intentions?.[correctionRowIndex.value]
  if (!item) return null
  return getOriginalSnapshot(item, correctionRowIndex.value) || null
})

const clearIntentionItem = async (item: any) => {
  item.intentionType = undefined
  item.domTagFirstCode = undefined
  item.domTagSecondCode = undefined
  item.domTagThreeCode = undefined
  item.domTagFourCode = undefined
  item.topic = undefined
  item.topicOptions = []

  await checkAndFetchAllTopics(item)
}

// 根据品牌code 获取车系名称
const carSeriesName = (code: string) => {
  if (!code) return code
  return carSeriesOptions.value.find((item: any) => item.key === code)?.value
}

// 更新原声信息
const updateOriginalSound = debounce(async () => {
  // 校验用户意图数据
  // if (formData.value.intentions && formData.value.intentions.length > 0) {
  //   for (let i = 0; i < formData.value.intentions.length; i++) {
  //     const item = formData.value.intentions[i]
  //     if (!item.intentionType) {
  //       ElMessage.warning(`第${i + 1}行用户意图不能为空`)
  //       return
  //     }
  //     if (!item.domTagFirstCode) {
  //       ElMessage.warning(`第${i + 1}行体验代码一级不能为空`)
  //       return
  //     }
  //     if (!item.domTagSecondCode) {
  //       ElMessage.warning(`第${i + 1}行体验代码二级不能为空`)
  //       return
  //     }
  //     if (!item.domTagThreeCode) {
  //       ElMessage.warning(`第${i + 1}行体验代码三级不能为空`)
  //       return
  //     }
  //     if (!item.domTagFourCode) {
  //       ElMessage.warning(`第${i + 1}行体验代码四级不能为空`)
  //       return
  //     }
  //     if (!item.topic) {
  //       ElMessage.warning(`第${i + 1}行标准观点不能为空`)
  //       return
  //     }
  //   }
  // }

  const params = {
    id: row.id,
    dataId: formData.value.dataId,
    carSeriesCode: formData.value.carSeriesCode,
    carSeriesName: carSeriesName(formData.value.carSeriesCode!),
    carModel: formData.value.carModel,
    engineNo: formData.value.engineNo,
    licensePlateNo: formData.value.licensePlateNo,
    vinNo: formData.value.vinNo,
    carPurchaseTime: formData.value.carPurchaseTime,
    dealerName: formData.value.dealerName
    // intentions: formData.value.intentions?.map(item => {
    //   const { topicOptions, ...rest } = item
    //   return {
    //     ...rest,
    //     domTagFirst: getTagOptions(1, item).find((opt: any) => opt.tagCode === item.domTagFirstCode)
    //       ?.tagName,
    //     domTagSecond: getTagOptions(2, item).find(
    //       (opt: any) => opt.tagCode === item.domTagSecondCode
    //     )?.tagName,
    //     domTagThree: getTagOptions(3, item).find((opt: any) => opt.tagCode === item.domTagThreeCode)
    //       ?.tagName,
    //     domTagFour: getTagOptions(4, item).find((opt: any) => opt.tagCode === item.domTagFourCode)
    //       ?.tagName
    //   }
    // })
  }
  const res = await singleEventStore.fetchUpdateOriginalSoundDetail(params)
  if (res.success) {
    ElMessage.success('更新成功')
    getDetail()
  }
}, 300)

const init = async () => {
  // console.log('row', row)
  getDetail()
}

onMounted(async () => {
  await syncContentHeights()

  if (typeof ResizeObserver !== 'undefined' && formWrapperRef.value) {
    formContentResizeObserver = new ResizeObserver(() => {
      updateContentHeights()
    })
    formContentResizeObserver.observe(formWrapperRef.value)
  }

  window.addEventListener('resize', updateContentHeights)
})

onBeforeUnmount(() => {
  formContentResizeObserver?.disconnect()
  formContentResizeObserver = null
  window.removeEventListener('resize', updateContentHeights)
})

// 禁用
const isDisabled = computed(() => {
  return eventType === EventType.VIEW || !formData.value.editPermission
})

// 高亮匹配文本
const highlightText = (text: string | undefined, searchText: string | undefined) => {
  if (!text || !searchText) return text || ''
  try {
    const escapedSearch = searchText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(escapedSearch, 'gi')
    return text.replace(regex, match => `<span style='color: #1677ff;'>${match}</span>`)
  } catch (error) {
    return text
  }
}

// 处理后的评论详情
const highlightedCommentDetails = computed(() => {
  if (isPost.value && currentEvent?.originalTextScene) {
    return highlightText(formData.value.commentDetails, currentEvent.originalTextScene)
  }
  return formData.value.commentDetails
})

// 处理后的主贴详情
const highlightedMainPostDetails = computed(() => {
  if (currentEvent?.originalTextScene) {
    return highlightText(formData.value.mainPostDetails, currentEvent.originalTextScene)
  }
  return formData.value.mainPostDetails
})

init()
</script>

<template>
  <div class="original-sound-details">
    <div class="osd-head">
      <div class="osdh-title">原声详情</div>
      <!-- v-if="eventType !== EventType.VIEW" -->
      <el-button
        v-if="eventType !== EventType.VIEW && formData.editPermission"
        type="primary"
        size="small"
        @click="updateOriginalSound"
        >更新消息</el-button
      >
    </div>
    <div class="osd-content">
      <div ref="formWrapperRef" class="osd-form-wrapper" :style="formWrapperStyle">
        <el-form :model="formData" ref="formDataRef" @submit.prevent>
          <el-row :gutter="8">
            <!-- v-if="isPost" -->
            <!-- 评论类型 -->
            <template v-if="isPost">
              <!-- <template v-if="true"> -->
              <el-col :span="4">
                <el-form-item label="评论用户:" prop="">
                  {{ formData.commentUserName }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="评论用户ID:" prop="">
                  <span class="text-break lh-20"> {{ formData.commentUserId }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="评论时间:" prop="">
                  {{ formData.commentTime }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="数据来源:" prop="">
                  {{ formData.channelName }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="原声类型:" prop="">
                  {{ formData.contentTypeName }}
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="评论详情:" prop="">
                  <div v-html="highlightedCommentDetails"></div>
                </el-form-item>
              </el-col>

              <el-col :span="4">
                <el-form-item label="发帖用户:" prop="">
                  {{ formData.postUserName }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="发帖用户ID:" prop="">
                  <span class="text-break lh-20"> {{ formData.postUserId }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="发帖时间:" prop="">
                  {{ formData.postTime }}
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="主贴链接:" prop="">
                  <!-- <div class="text-link">{{ formData.mainPostUrl }}</div> -->
                  <div
                    class="text-link single-line-ellipsis cursor-point"
                    @click="openWindow(formData.mainPostUrl!)"
                  >
                    {{ formData.mainPostUrl }}
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="主贴标题:" prop="">
                  {{ formData.mainPostTitle }}
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="主贴详情:" prop="">
                  <div v-html="highlightedMainPostDetails"></div>
                </el-form-item>
              </el-col>
            </template>

            <!-- 主贴类型 -->
            <template v-if="!isPost">
              <!-- <template v-if="false"> -->
              <el-col :span="4">
                <el-form-item label="发帖用户:" prop="">
                  {{ formData.postUserName }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="发帖用户ID:" prop="">
                  <span class="text-break lh-20"> {{ formData.postUserId }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="发帖时间:" prop="">
                  {{ formData.postTime }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="数据来源:" prop="">
                  {{ formData.channelName }}
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="原声类型:" prop="">
                  {{ formData.contentTypeName }}
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="主贴标题:" prop="">
                  {{ formData.mainPostTitle }}
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="主贴链接:" prop="">
                  <!-- <div class="text-link">{{ formData.mainPostUrl }}</div> -->
                  <div
                    class="text-link single-line-ellipsis cursor-point"
                    @click="openWindow(formData.mainPostUrl!)"
                  >
                    {{ formData.mainPostUrl }}
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="主贴详情:" prop="">
                  <div v-html="highlightedMainPostDetails"></div>
                </el-form-item>
              </el-col>
            </template>

            <el-col :span="6">
              <el-form-item label="品牌" prop="">
                <el-select
                  v-model="formData.brandCode"
                  placeholder=""
                  clearable
                  :options="singleEventStore.brandOptions"
                  :props="{ label: 'value', value: 'key' }"
                  disabled
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="车系" prop="">
                <el-select
                  v-model="formData.carSeriesCode"
                  placeholder=""
                  clearable
                  filterable
                  :disabled="isDisabled"
                  :options="carSeriesOptions"
                  :props="{ label: 'value', value: 'key' }"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="车型" prop="">
                <el-input
                  v-model.trim="formData.carModel"
                  :disabled="isDisabled"
                  clearable
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="发动机号" prop="">
                <el-input
                  v-model.trim="formData.engineNo"
                  :disabled="isDisabled"
                  clearable
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="车牌号" prop="">
                <el-input
                  v-model.trim="formData.licensePlateNo"
                  clearable
                  :maxlength="50"
                  :disabled="isDisabled"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="车架号" prop="">
                <el-input
                  v-model.trim="formData.vinNo"
                  :disabled="isDisabled"
                  clearable
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="购车时间" prop="">
                <el-date-picker
                  v-model="formData.carPurchaseTime"
                  value-format="YYYY-MM-DD"
                  type="date"
                  placeholder=""
                  :disabled="isDisabled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="经销商" prop="">
                <el-input
                  v-model.trim="formData.dealerName"
                  :disabled="isDisabled"
                  clearable
                  :maxlength="50"
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="用户意图" prop="">
                <div
                  class="flex"
                  v-for="(item, index) in formData.intentions"
                  :key="index"
                  :ref="index === 0 ? setFirstIntentionItemRef : undefined"
                  :class="{ 'mt-16': index > 0 }"
                >
                  <el-select
                    v-model="item.intentionType"
                    placeholder=""
                    clearable
                    :options="singleEventStore.voc_intention"
                    :props="{ label: 'text', value: 'value' }"
                    class="w-90"
                    :disabled="isDisabled || !item.correctButton"
                    @change="handleIntentionChange(item)"
                  />
                  <el-form-item label="体验代码" prop="" class="ml-16">
                    <div class="flex gap-10">
                      <el-select
                        v-model="item.domTagFirstCode"
                        placeholder=""
                        clearable
                        filterable
                        :options="getTagOptions(1, item)"
                        :props="{ label: 'tagName', value: 'tagCode' }"
                        class="w-132"
                        :disabled="isDisabled || !item.correctButton"
                        @change="handleTagChange(1, item)"
                      />
                      <el-select
                        v-model="item.domTagSecondCode"
                        placeholder=""
                        clearable
                        filterable
                        :options="getTagOptions(2, item)"
                        :props="{ label: 'tagName', value: 'tagCode' }"
                        class="w-132"
                        :disabled="isDisabled || !item.correctButton"
                        @change="handleTagChange(2, item)"
                      />
                      <el-select
                        v-model="item.domTagThreeCode"
                        placeholder=""
                        clearable
                        filterable
                        :options="getTagOptions(3, item)"
                        :props="{ label: 'tagName', value: 'tagCode' }"
                        class="w-132"
                        :disabled="isDisabled || !item.correctButton"
                        @change="handleTagChange(3, item)"
                      />
                      <el-select
                        v-model="item.domTagFourCode"
                        placeholder=""
                        clearable
                        filterable
                        :disabled="isDisabled || !item.correctButton"
                        :options="getTagOptions(4, item)"
                        :props="{ label: 'tagName', value: 'tagCode' }"
                        class="w-132"
                        @change="handleTagChange(4, item)"
                      />
                    </div>
                  </el-form-item>
                  <el-form-item label="标准观点" prop="" class="ml-24">
                    <el-select-v2
                      v-model="item.topic"
                      placeholder=""
                      clearable
                      filterable
                      :disabled="isDisabled || !item.correctButton"
                      :options="item.topicOptions || []"
                      :props="{ label: 'tagName', value: 'tagName' }"
                      class="w-212"
                      :popper-class="'selectV2PopClass'"
                      @change="handleTopicChange(item)"
                    />
                  </el-form-item>
                  <el-button
                    v-if="item.correctButton"
                    type="primary"
                    plain
                    class="el-butto__btn-keep-border ml-8"
                    :disabled="isDisabled || !isIntentionUpdated(item, index)"
                    @click="openCorrectionDialog(index)"
                    >数据纠错</el-button
                  >
                  <el-button
                    v-if="item.correctButton"
                    type="danger"
                    plain
                    :disabled="isDisabled"
                    :icon="Delete"
                    class="ml-8"
                    @click="clearIntentionItem(item)"
                  />
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 展开/收起按钮 -->
      <div v-if="showToggleButton" class="toggle-button-wrapper">
        <div class="toggle-button" @click="toggleExpand">
          <span class="toggle-text">{{ isExpanded ? '收起详情' : '展开详情' }}</span>
          <el-icon v-if="isExpanded">
            <ArrowUp />
          </el-icon>
          <el-icon v-else>
            <ArrowDown />
          </el-icon>
        </div>
      </div>
    </div>

    <CorrectionApplyDialog
      v-model:visible="correctionDialogVisible"
      :current-item="correctionDialogItem"
      :original-snapshot="correctionDialogOriginalSnapshot"
      :intention-options="singleEventStore.voc_intention || []"
      :get-tag-options="getTagOptions"
      :start-time="startTime"
      :end-time="endTime"
      :row="row"
      @success="handleCorrectionSuccess"
    />
  </div>
</template>

<style lang="scss" scoped>
.original-sound-details {
  border-radius: 8px;
  border: 1px solid #dde3ee;
  position: relative;

  .osd-head {
    background: #ffffff;
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    border-radius: 8px 8px 0 0;
    .osdh-title {
      font-weight: 600;
      font-size: 16px;
      color: #1f2733;
      line-height: 24px;
    }
  }

  .osd-content {
    background: #eaf3ff;
    position: relative;

    .osd-form-wrapper {
      padding: 16px 24px;
      position: relative;
      overflow: hidden;
      transition: max-height 0.3s ease;

      .text-link {
        color: #1677ff;
      }
    }

    // 展开/收起按钮容器
    .toggle-button-wrapper {
      display: flex;
      justify-content: center;
      // padding: 8px 0 16px;
      background: #eaf3ff;
    }

    // 梯形按钮
    .toggle-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      padding: 2px 14px;
      background: #ffffff;
      // border: 1px solid #dde3ee;
      border: 1px solid #ebedf0;
      // border-bottom: none;
      cursor: pointer;
      user-select: none;
      transition: all 0.3s ease;
      margin-bottom: -1px;

      // 梯形效果
      clip-path: polygon(10% 0%, 90% 0%, 100% 100%, 0% 100%);

      .toggle-text {
        font-weight: 400;
        font-size: 14px;
        color: #1f2733;
        line-height: 20px;
      }

      .el-icon {
        font-size: 14px;
        color: #1f2733;
      }

      // &:hover {
      //   background: #f5f7fa;

      //   .toggle-text,
      //   .el-icon {
      //     color: #409eff;
      //   }
      // }

      // &:active {
      //   background: #ecf5ff;
      // }
    }
  }
}
</style>
