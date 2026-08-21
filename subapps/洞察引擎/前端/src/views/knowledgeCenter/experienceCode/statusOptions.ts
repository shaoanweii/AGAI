import type { ConditionsDetailItem } from '@/types'
import type { OptionItem, StatusValue } from './components/types'

const DEFAULT_STATUS_OPTIONS: OptionItem<StatusValue>[] = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

/**
 * 体验代码页统一从 stopOrEnable 字典解析启用状态选项，缺失时再退回页面兜底值。
 */
export const resolveExperienceCodeStatusOptions = (
  source?: Record<string, ConditionsDetailItem[]>
) => {
  const options = Array.isArray(source?.stopOrEnable) ? source.stopOrEnable : []

  const normalizedOptions = options
    .map(item => {
      const value = String(item?.key ?? '').trim()
      const label = String(item?.value ?? '').trim()
      if ((value !== '1' && value !== '0') || !label) {
        return null
      }
      return {
        label,
        value: value as StatusValue
      }
    })
    .filter((item): item is OptionItem<StatusValue> => Boolean(item))

  return normalizedOptions.length ? normalizedOptions : DEFAULT_STATUS_OPTIONS
}

/**
 * 状态文案统一优先取接口字典，确保列表展示和表单选项口径一致。
 */
export const resolveExperienceCodeStatusLabel = (
  status: string | null | undefined,
  options: OptionItem<StatusValue>[]
) => {
  return (
    options.find(item => item.value === status)?.label || (String(status) === '0' ? '禁用' : '启用')
  )
}

/**
 * 新建表单默认优先落到“启用”对应的字典值，避免后续接口字典顺序调整影响默认态。
 */
export const resolveExperienceCodeEnabledStatus = (options: OptionItem<StatusValue>[]) => {
  return options.find(item => item.value === '1')?.value || '1'
}
