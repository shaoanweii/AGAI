import useQueryStore from '@/store/modules/query'
import { getShortcutDateRange, getTimeDimensionByCode } from '@/utils/date'

export interface CustomerDirectEngageDefaultTime {
  times: [string, string]
  shortcutValue: string
}

export interface CustomerDirectEngageDefaultFilter {
  brandCodes: string[]
  time: CustomerDirectEngageDefaultTime
  hasRoleBrandDefault: boolean
  hasRoleTimeDefault: boolean
}

const BRAND_FILTER_TYPES = ['911', '91']
const TIME_FILTER_TYPE = '93'

/**
 * 判断筛选值是否可用于默认条件解析。
 * @param value 原始筛选值
 * @returns 是否为有效值
 */
const isValidDefaultValue = (value: unknown) => {
  return value !== '' && value !== null && value !== undefined
}

/**
 * 提取高级筛选保存后的值，兼容 value 与 selected 两种字段。
 * @param condition 角色默认筛选项
 * @returns 筛选值
 */
const getConditionValue = (condition: any) => {
  return condition?.value !== undefined ? condition.value : condition?.selected
}

/**
 * 从对象或基础值中提取品牌编码。
 * @param value 品牌值
 * @returns 品牌编码
 */
const resolveBrandValue = (value: any) => {
  const brandValue = value?.key ?? value?.brandCode ?? value?.code ?? value?.value ?? value
  return isValidDefaultValue(brandValue) ? String(brandValue) : ''
}

/**
 * 将角色默认品牌值转换为页面品牌多选需要的编码数组。
 * @param value 角色默认品牌筛选值
 * @param isMultiSelect 当前品牌条件是否为多选配置
 * @returns 品牌编码数组
 */
const normalizeBrandCodes = (value: unknown, isMultiSelect = false): string[] => {
  if (Array.isArray(value)) {
    if (value.every(Array.isArray)) {
      return Array.from(new Set(value.map(item => resolveBrandValue(item[0])).filter(Boolean)))
    }

    const values = value.filter(isValidDefaultValue)
    if (values.length === 0) return []

    if (values.some(item => typeof item === 'object')) {
      return Array.from(new Set(values.map(resolveBrandValue).filter(Boolean)))
    }

    // 角色默认筛选的品牌控件历史上是级联值，[品牌, 车系] 时只取品牌。
    if (!isMultiSelect && values.length === 2) {
      return [String(values[0])]
    }

    return Array.from(new Set(values.map(item => String(item))))
  }

  const brandValue = resolveBrandValue(value)
  return brandValue ? [brandValue] : []
}

/**
 * 判断角色默认品牌条件是否按多选值解析。
 * @param condition 角色默认品牌筛选项
 * @returns 是否为品牌多选条件
 */
const isBrandMultiSelectCondition = (condition: any) => {
  return String(condition?.filterType) === '911' || condition?.multiSelect === true
}

/**
 * 判断是否为 YYYY-MM-DD 日期字符串。
 * @param value 待判断值
 * @returns 是否为日期字符串
 */
const isDateString = (value: unknown) => {
  return typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)
}

/**
 * 将角色默认时间值转换为日期组件需要的区间与快捷文案。
 * @param value 角色默认时间筛选值
 * @returns 默认时间配置
 */
const normalizeDefaultTime = (value: unknown): CustomerDirectEngageDefaultTime | undefined => {
  if (Array.isArray(value)) {
    const values = value.filter(isValidDefaultValue)
    if (values.length === 2 && values.every(isDateString)) {
      return {
        times: [String(values[0]), String(values[1])],
        shortcutValue: '自定义'
      }
    }

    value = values[0]
  }

  if (!isValidDefaultValue(value)) return undefined

  const timeValue =
    typeof value === 'object'
      ? (value as any).code ?? (value as any).key ?? (value as any).value
      : value

  if (!isValidDefaultValue(timeValue)) return undefined

  const dimensionItem = getTimeDimensionByCode(timeValue as string | number)
  if (!dimensionItem) return undefined

  const [startDate, endDate] = getShortcutDateRange(timeValue as string | number)

  return {
    times: [startDate, endDate],
    shortcutValue: dimensionItem.name
  }
}

/**
 * 根据角色配置生成客情直驱页面默认筛选状态。
 * @param routeName 当前路由 name / permissionKey
 * @param fallbackShortcut 无角色时间默认值时使用的页面默认快捷项
 * @returns 品牌与时间默认状态
 */
export const getCustomerDirectEngageDefaultFilter = (
  routeName: string,
  fallbackShortcut: string
): CustomerDirectEngageDefaultFilter => {
  const queryStore = useQueryStore()
  const defaultList = queryStore.getCurDefByRouterName(routeName)
  const fallbackTime = normalizeDefaultTime(fallbackShortcut) as CustomerDirectEngageDefaultTime

  if (!Array.isArray(defaultList) || defaultList.length === 0) {
    return {
      brandCodes: [],
      time: fallbackTime,
      hasRoleBrandDefault: false,
      hasRoleTimeDefault: false
    }
  }

  const brandCondition = BRAND_FILTER_TYPES.map(filterType =>
    defaultList.find((item: any) => String(item?.filterType) === filterType)
  ).find(Boolean)
  const timeCondition = defaultList.find((item: any) => String(item?.filterType) === TIME_FILTER_TYPE)
  const brandCodes = normalizeBrandCodes(
    getConditionValue(brandCondition),
    isBrandMultiSelectCondition(brandCondition)
  )
  const roleDefaultTime = normalizeDefaultTime(getConditionValue(timeCondition))

  return {
    brandCodes,
    time: roleDefaultTime || fallbackTime,
    hasRoleBrandDefault: brandCodes.length > 0,
    hasRoleTimeDefault: Boolean(roleDefaultTime)
  }
}
