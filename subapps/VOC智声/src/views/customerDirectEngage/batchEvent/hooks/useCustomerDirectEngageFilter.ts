import { computed, ref, watch, type Ref } from 'vue'
import dayjs from 'dayjs'
import { getTopicsByTagId } from '@/api/singlePointEvent'
import { getShortcutDateRange } from '@/utils/date'
import type { CustomerDirectEngageFilterFormData } from '../types'
import { useBatchEventOptions } from './useBatchEventOptions'
import type { CustomerDirectEngageDefaultTime } from '../../shared/defaultFilter'

const DEFAULT_WARNING_TIME_SHORTCUT = '近30天'

interface CustomerDirectEngageFilterOptions {
  defaultTime?: CustomerDirectEngageDefaultTime
  getDefaultTime?: () => CustomerDirectEngageDefaultTime
}

/**
 * 客户直连事件公共筛选逻辑。
 * - 统一维护默认时间、品牌与车系联动、体验代码联动和标准观点加载。
 * - 页面只负责把 query/reset 与列表请求串起来，避免在两个页面复制复杂筛选脚本。
 */
export const useCustomerDirectEngageFilter = (
  formData: Ref<CustomerDirectEngageFilterFormData>,
  options: CustomerDirectEngageFilterOptions = {}
) => {
  const batchEventOptions = useBatchEventOptions()
  const resolveDefaultTime = () => options.getDefaultTime?.() || options.defaultTime
  const defaultTime = resolveDefaultTime()

  const times = ref<string[]>(defaultTime ? [...defaultTime.times] : [])
  const shortcutValue = ref(defaultTime?.shortcutValue || DEFAULT_WARNING_TIME_SHORTCUT)
  const isExpanded = ref(false)
  const topicOptions = ref<any[]>([])

  const queryTimeRange = computed(() => {
    return {
      startTime: times.value[0] ? `${times.value[0]} 00:00:00` : '',
      endTime: times.value[1] ? `${times.value[1]} 23:59:59` : ''
    }
  })

  const brandOptions = computed(() => {
    return batchEventOptions.brandOptions.value || []
  })

  const brandOptionProps = computed(() => batchEventOptions.brandOptionProps.value)

  const carSeriesOptionProps = computed(() => batchEventOptions.carSeriesOptionProps.value)

  /**
   * 将任意筛选值转换为字符串数组，兼容历史缓存与数字值。
   * @param values 原始筛选值
   * @returns 过滤空值后的字符串数组
   */
  const normalizeSelectedValues = (values: unknown): string[] => {
    if (Array.isArray(values)) {
      return values
        .filter(value => value !== '' && value !== null && value !== undefined)
        .map(value => String(value))
    }

    if (values === '' || values === null || values === undefined) {
      return []
    }

    return [String(values)]
  }

  /**
   * 提取车系节点的稳定 value。
   * @param series 车系节点
   * @returns 车系筛选值字符串
   */
  const getSeriesOptionValue = (series: any) => {
    const value = series?.[carSeriesOptionProps.value.value]
    return value === '' || value === null || value === undefined ? '' : String(value)
  }

  /**
   * 根据选中的品牌聚合车系列表，并对重复筛选值做去重。
   * @param brandCodes 已选品牌编码
   * @returns 当前品牌集合下可选的车系列表
   */
  const buildCarSeriesOptionsByBrandCodes = (brandCodes: string[] = []) => {
    const selectedBrandValues = normalizeSelectedValues(brandCodes)
    if (!selectedBrandValues.length) return []

    const seriesMap = new Map<string, any>()

    brandOptions.value.forEach(brand => {
      const brandValue = brand?.[brandOptionProps.value.value]
      const isSelectedBrand = selectedBrandValues.includes(String(brandValue || ''))

      if (!isSelectedBrand) {
        return
      }

      brand?.children?.forEach((series: any) => {
        const seriesValue = getSeriesOptionValue(series)
        if (seriesValue && !seriesMap.has(seriesValue)) {
          seriesMap.set(seriesValue, series)
        }
      })
    })

    return Array.from(seriesMap.values())
  }

  /**
   * 获取当前可用车系列表。
   * 批量事件后端若返回独立车系列表，则作为真实数据源兜底；否则沿用品牌树联动。
   * @param brandCodes 已选品牌编码
   * @returns 车系列表
   */
  const getAvailableCarSeriesOptions = (brandCodes: string[] = []) => {
    const linkedOptions = buildCarSeriesOptionsByBrandCodes(brandCodes)
    if (linkedOptions.length > 0) {
      return linkedOptions
    }

    return batchEventOptions.batchCarSeriesOptions.value
  }

  const carSeriesOptions = computed(() => {
    return getAvailableCarSeriesOptions(formData.value.brandCodes || [])
  })

  /**
   * 品牌切换后回收已失效的车系筛选值，避免表单保留无效选项。
   * @param brandCodes 当前选中的品牌编码
   */
  const syncCarSeriesCodesByBrandCodes = (brandCodes: string[] = []) => {
    const validCarSeriesCodeSet = new Set(
      getAvailableCarSeriesOptions(brandCodes)
        .map((item: any) => getSeriesOptionValue(item))
        .filter(Boolean)
    )

    const selectedCarSeriesValues = normalizeSelectedValues(formData.value.carSeriesCodes || [])

    formData.value.carSeriesCodes = selectedCarSeriesValues.filter(value =>
      validCarSeriesCodeSet.has(value)
    )
  }

  watch(
    () => formData.value.brandCodes,
    brandCodes => {
      syncCarSeriesCodesByBrandCodes(brandCodes || [])
    },
    { deep: true }
  )

  watch(
    carSeriesOptions,
    () => {
      syncCarSeriesCodesByBrandCodes(formData.value.brandCodes || [])
    },
    { deep: true }
  )

  /**
   * 基于快捷选项重置日期范围。
   * 这里直接复用公共日期工具，避免对子组件 ref 做隐式依赖。
   * @param shortcut 快捷选项名称
   */
  const applyShortcutDateRange = (shortcut: string = shortcutValue.value) => {
    const [startTime, endTime] = getShortcutDateRange(shortcut)
    times.value = [dayjs(startTime).format('YYYY-MM-DD'), dayjs(endTime).format('YYYY-MM-DD')]
  }

  /**
   * 获取当前已选择的体验代码末级。
   * 查询标准观点时仅使用末级代码，未选时回退为全部观点。
   * @returns 当前末级体验代码数组
   */
  const getLastLevelCodes = (): string[] => {
    if (formData.value.fourCodeTag.length > 0) {
      return formData.value.fourCodeTag
    }
    if (formData.value.threeCodeTag.length > 0) {
      return formData.value.threeCodeTag
    }
    if (formData.value.secondCodeTag.length > 0) {
      return formData.value.secondCodeTag
    }
    if (formData.value.firstCodeTag.length > 0) {
      return formData.value.firstCodeTag
    }

    return []
  }

  /**
   * 按已选末级体验代码重新加载标准观点。
   * 未选择体验代码时查询全量观点，保持与原单点事件页一致。
   */
  const fetchTopicsByLastLevel = async () => {
    const lastLevelCodes = getLastLevelCodes()
    const codesToFetch = lastLevelCodes.length === 0 ? [] : lastLevelCodes

    try {
      const res = await getTopicsByTagId(codesToFetch)
      if (res.success && Array.isArray(res.result)) {
        topicOptions.value = res.result
        return
      }

      topicOptions.value = []
    } catch (error) {
      console.error('获取标准观点失败:', error)
      topicOptions.value = []
    }
  }

  const experienceCodeOptions = computed(() => {
    return batchEventOptions.tagTreeList.value || []
  })

  watch(
    [
      () => formData.value.fourCodeTag,
      () => formData.value.threeCodeTag,
      () => formData.value.secondCodeTag,
      () => formData.value.firstCodeTag
    ],
    () => {
      formData.value.topicList = []
      topicOptions.value = []
      void fetchTopicsByLastLevel()
    },
    { deep: true, immediate: true }
  )

  /**
   * 重置公共筛选状态。
   * - 日期回到近 30 天
   * - 标准观点选项恢复为全量结果
   */
  const resetFilterState = () => {
    const nextDefaultTime = resolveDefaultTime()
    shortcutValue.value = nextDefaultTime?.shortcutValue || DEFAULT_WARNING_TIME_SHORTCUT
    if (nextDefaultTime) {
      times.value = [...nextDefaultTime.times]
    } else {
      applyShortcutDateRange(shortcutValue.value)
    }
    topicOptions.value = []
    void fetchTopicsByLastLevel()
  }

  if (!defaultTime) {
    applyShortcutDateRange(shortcutValue.value)
  }

  return {
    times,
    shortcutValue,
    isExpanded,
    queryTimeRange,
    brandOptions,
    brandOptionProps,
    carSeriesOptions,
    carSeriesOptionProps,
    experienceCodeOptions,
    topicOptions,
    resetFilterState
  }
}
