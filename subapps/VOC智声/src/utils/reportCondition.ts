import { getShortcutDateRange, getTimeDimensionByCode } from '@/utils/date'
import type { FilterFieldConfig } from '@/components/Business/UniversaFilter/types'

/**
 * 将 formData 和 customTimes 转换为 filterItems 格式
 * 用于查看报告时设置查询条件
 */
export function convertFormDataToFilterItems(
  formData: Record<string, any>,
  customTimes: string[],
  config: FilterFieldConfig[],
  standardViewpointOptions: any[] = []
): any[] {
  const items: any[] = []

  config.forEach((field, index) => {
    // 跳过占位符和车系（车系通过品牌级联处理）
    if (field.type === 'placeholder' || field.type === 'series') {
      return
    }

    // 处理日期范围
    if (field.type === 'daterange') {
      const dateRangeValue = formData[field.prop]
      if (dateRangeValue) {
        let startDate = ''
        let endDate = ''
        let selectedShortcut = ''

        // 如果 dateRange 是 'custom'，直接使用 customTimes
        if (dateRangeValue === 'custom') {
          if (customTimes && Array.isArray(customTimes) && customTimes.length === 2) {
            startDate = customTimes[0]
            endDate = customTimes[1]
            selectedShortcut = '自定义'
          }
        } else if (dateRangeValue) {
          // 如果是快捷选项的 code，直接使用 getShortcutDateRange 计算
          const times = getShortcutDateRange(dateRangeValue)
          startDate = times[0]
          endDate = times[1]
          const dimensionItem = getTimeDimensionByCode(dateRangeValue)
          if (dimensionItem && dimensionItem.name) {
            selectedShortcut = dimensionItem.name
          }
        }

        // 将日期范围作为 filterItem 添加到数组中
        if (startDate && endDate) {
          const dateFilterItem: any = {
            id: `filter_${field.prop}_${index}`,
            name: field.label,
            filterType: '93', // 日期范围的 filterType 是 93
            value: dateRangeValue === 'custom' ? [startDate, endDate] : dateRangeValue,
            ext: {
              selectedShortcut,
              startDate,
              endDate
            }
          }
          items.push(dateFilterItem)
        }
      }
      return
    }

    const value = formData[field.prop]
    // 如果值为空，跳过
    if (
      value === null ||
      value === undefined ||
      value === '' ||
      (Array.isArray(value) && value.length === 0)
    ) {
      return
    }

    // 根据字段类型构建 filterItem
    let filterItem: any = {
      id: `filter_${field.prop}_${index}`,
      name: field.label,
      filterType: field.filterType || '',
      value: value,
      ext: {}
    }

    // 根据不同的字段类型设置 filterType
    if (field.type === 'brand') {
      filterItem.filterType = '91'
      filterItem.value = Array.isArray(value) ? value : [value]
    } else if (field.type === 'experienceCode') {
      filterItem.filterType = '92'
      filterItem.value = Array.isArray(value) ? value : [value]
    } else if (field.type === 'dataSource') {
      filterItem.filterType = '95'
      filterItem.value = Array.isArray(value) ? value : [value]
      filterItem.multiSelect = field.multiple ?? true
    } else if (field.type === 'select' || field.type === 'selectv2') {
      filterItem.filterType = '1'
      filterItem.value = Array.isArray(value) ? value : [value]
      filterItem.multiSelect = field.multiple ?? true
      // 如果是标准观点，需要设置选项
      if (field.prop === 'topicCodes' && standardViewpointOptions.length > 0) {
        filterItem.enumValue = standardViewpointOptions.map((item: any) => ({
          key: item.code,
          value: item.name
        }))
      } else if (field.options && field.options.length > 0) {
        filterItem.enumValue = field.options.map((item: any) => {
          const labelKey = field.props?.label || 'label'
          const valueKey = field.props?.value || 'value'
          return {
            key: item[valueKey],
            value: item[labelKey]
          }
        })
      }
    } else if (field.type === 'input') {
      filterItem.filterType = '2'
      filterItem.value = value
    }

    items.push(filterItem)
  })

  return items
}

