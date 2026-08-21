import dayjs from 'dayjs'

/**
 * @description: 数据处理-本地上传-查看数据、系统集成-查看数据。项目管理-查看数据。相同逻辑
 * @return {*}
 */
export function useTagVIewData() {
  const selectedTime = ref()

  /**
   * @description: 选择的第一个时间
   * @param {any} val
   * @return {*}
   */
  const rangeSelectedTime = (val: any) => {
    selectedTime.value = val?.[0]
  }

  /**
   * @description: 时间选择器禁用逻辑
   * @param {any} current
   * @return {*}
   */
  const rangeDisabled = (current: any) => {
    if (selectedTime.value) {
      const startDate = dayjs(selectedTime.value).subtract(1, 'year')
      const endDate = dayjs(selectedTime.value).add(1, 'year')
      const currentDate = dayjs(current)
      if (currentDate.isBefore(startDate) || currentDate.isAfter(endDate)) {
        return true
      }
    }
    return false
  }
  return {
    rangeSelectedTime,
    rangeDisabled
  }
}
