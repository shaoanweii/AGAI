export interface DataSourceDetail {
  // 数据名称
  dataName: string
  // 导入结果
  importResult: string
  // 导入时间
  createTime: string
  // 状态
  status: string
  // 批次号
  batchId: string
  // 是否可处理  false 就置灰，true就可点击
  processible: boolean
}
