<script setup lang="ts">
import { nextTick, ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import ThisCompetitorBrandServiceSwitch from './ThisCompetitorBrandServiceSwitch.vue'
import type { brandCarSeriesItem, HighestBrandCarVo } from '@/api/competitorAnalysis/types'

defineOptions({
  name: 'CompareHead'
})
enum SwitchType {
  FIRST = 'firstTitle',
  SECOND = 'secondTitle'
}

const route = useRoute()

const { defaultHighestBrandCarData, allBrandOrCarSeriesOptions } = defineProps<{
  defaultHighestBrandCarData: HighestBrandCarVo
  allBrandOrCarSeriesOptions: brandCarSeriesItem[]
}>()

// 判断是否是详情页（通过 isBack=1 判断）
const isDetailPage = computed(() => route.query.isBack === '1')

const emit = defineEmits<{
  (
    e: 'update:selectedCodes',
    firstCode: string | undefined,
    secondCode: string | undefined,
    firstName?: string,
    secondName?: string
  ): void
}>()

const firstTitle = ref('智行')
const secondTitle = ref('远途')
const firstSelectedCode = ref<string>()
const secondSelectedCode = ref<string>()
const isRestoring = ref(false) // 标记是否正在恢复数据
const isCategorySwitching = ref(false) // 标记是否正在切换分类
const hasUserSelected = ref(false) // 标记用户是否手动选择过本品和竞品

const switchChange = (val: any, type: SwitchType) => {
  // console.log(val, type)
  if (type === SwitchType.FIRST) {
    firstTitle.value = val.value
    firstSelectedCode.value = val.key
  } else if (type === SwitchType.SECOND) {
    secondTitle.value = val.value
    secondSelectedCode.value = val.key
  }
  // 标记用户已手动选择
  hasUserSelected.value = true
  // 用户手动切换
  emit(
    'update:selectedCodes',
    firstSelectedCode.value,
    secondSelectedCode.value,
    firstTitle.value,
    secondTitle.value
  )
}

watch(
  () => defaultHighestBrandCarData,
  val => {
    // 如果正在恢复数据，跳过 watch
    if (isRestoring.value) {
      return
    }
    // 如果正在切换分类，跳过 watch（由父组件主动调用更新）
    if (isCategorySwitching.value) {
      return
    }
    // 如果用户已经手动选择过，跳过自动设置默认值（保持用户的选择）
    if (hasUserSelected.value) {
      return
    }
    // 如果是详情页，跳过初始化逻辑，直接使用恢复的数据
    // 但是分类切换时，即使是在详情页，也应该更新（由父组件主动调用）
    if (isDetailPage.value) {
      return
    }
    // console.log('val', val)
    setDefaultBrancCarSeries(val)
  },
  { deep: true }
)

const firstRef = ref()
const secondRef = ref()
// 设置默认品牌车系
const setDefaultBrancCarSeries = async (
  defaultHighestBrandCarData: HighestBrandCarVo,
  forceRestore = false,
  isCategorySwitch = false
) => {
  // 如果没有数据，不执行
  if (!defaultHighestBrandCarData.self || !defaultHighestBrandCarData.competitor) {
    return
  }

  const newFirstCode = defaultHighestBrandCarData.self?.code
  const newSecondCode = defaultHighestBrandCarData.competitor?.code

  // 如果是分类切换，即使code相同也要更新（因为分类变了）
  // 如果code没有变化，不执行后续操作，防止循环（除非是强制恢复或分类切换）
  if (
    !forceRestore &&
    !isCategorySwitch &&
    firstSelectedCode.value === newFirstCode &&
    secondSelectedCode.value === newSecondCode
  ) {
    return
  }

  // 如果是强制恢复，先设置标记，防止 watch 干扰
  // 强制恢复时设置用户选择标记为 true，因为这是用户之前的选择
  if (forceRestore) {
    isRestoring.value = true
    hasUserSelected.value = true
  }

  // 如果是分类切换，设置标记，防止 watch 干扰
  // 分类切换时重置用户选择标记，允许使用新的默认值
  if (isCategorySwitch) {
    isCategorySwitching.value = true
    hasUserSelected.value = false
  }

  firstTitle.value = defaultHighestBrandCarData.self?.name as string
  secondTitle.value = defaultHighestBrandCarData.competitor?.name as string
  firstSelectedCode.value = newFirstCode
  secondSelectedCode.value = newSecondCode

  await nextTick()
  if (firstRef.value) {
    firstRef.value.setCascaderValue(newFirstCode)
  }

  if (secondRef.value) {
    secondRef.value.setCascaderValue(newSecondCode)
  }

  // 只有当两个code都有值时才emit
  if (firstSelectedCode.value && secondSelectedCode.value) {
    // 通知父组件更新 code
    emit(
      'update:selectedCodes',
      firstSelectedCode.value,
      secondSelectedCode.value,
      firstTitle.value,
      secondTitle.value
    )
  }

  // 恢复完成后，延迟重置标记，确保后续的 watch 可以正常工作
  if (forceRestore) {
    // 使用 setTimeout 延迟重置，确保所有异步操作完成
    setTimeout(() => {
      isRestoring.value = false
    }, 100)
  }

  // 分类切换完成后，延迟重置标记
  if (isCategorySwitch) {
    setTimeout(() => {
      isCategorySwitching.value = false
    }, 100)
  }
}

defineExpose({
  setDefaultBrancCarSeries
})
</script>

<template>
  <div class="compare-head">
    <div class="ch-item">市场均值</div>
    <div class="ch-item">
      <span>{{ firstTitle }}</span>
      <ThisCompetitorBrandServiceSwitch
        ref="firstRef"
        key="this"
        :all-brand-or-car-series-options="allBrandOrCarSeriesOptions"
        :disabled-codes="secondSelectedCode ? [secondSelectedCode] : []"
        @change="(val: any) => switchChange(val, SwitchType.FIRST)"
      ></ThisCompetitorBrandServiceSwitch>
    </div>
    <div class="ch-item">
      <span>{{ secondTitle }}</span>
      <ThisCompetitorBrandServiceSwitch
        ref="secondRef"
        key="competitor"
        :all-brand-or-car-series-options="allBrandOrCarSeriesOptions"
        :disabled-codes="firstSelectedCode ? [firstSelectedCode] : []"
        @change="(val: any) => switchChange(val, SwitchType.SECOND)"
      ></ThisCompetitorBrandServiceSwitch>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.compare-head {
  width: calc(100% + 56px);
  margin: 0 -28px;
  height: 48px;
  background: #ffffff;
  border-radius: 0px 0px 0px 0px;
  border-top: 1px solid #dfe2e8;
  border-bottom: 1px solid #dfe2e8;
  display: flex;
  position: relative;
  z-index: 100;

  .ch-item {
    flex: 1;
    height: 100%;
    min-width: 0;
    font-weight: 500;
    font-size: 20px;
    color: #333333;
    line-height: 20px;

    display: flex;
    align-items: center;
    justify-content: center;
    &:not(:last-child) {
      border-right: 1px solid #dfe2e8;
    }

    .ch-switch {
      font-weight: 400;
      font-size: 16px;
      color: #1677ff;
      line-height: 20px;
      margin-left: 16px;
      cursor: pointer;
    }
  }
}
</style>
