/**
 * @description: 将筛选项里的value值映射到指定的字段上
 * 高级筛选的filterType: 1选择框，2输入框，91 品牌单选，911 品牌多选，92 标签，93时间，94移动端时间，95 数据源
 * @param {any} conditions
 * @return {*}
 */
export const filterValueMapping = (conditions: any) => {
  return conditions?.map((el: any) => {
    return {
      ...el,
      selected: ['1', '91', '911', '92', '93', '95'].includes(el.filterType)
        ? el.value
        : undefined,
      inputSelected: el.filterType === '2' ? el.value : undefined
    }
  })
}
