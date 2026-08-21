/**
 * 处理进度页签中的抄送人员标准化选择模型。
 * 与 `CcPersonnelSelect` 对外暴露的数据结构保持一致，便于弹窗回显与表格转换复用同一份状态。
 */
export interface ProcessingProgressCcSelectionItem {
  orgId?: string
  orgNo?: string
  orgName?: string
  allFlag: boolean
  userId?: string
  userEmpNo?: string
  userName?: string
}

/**
 * 处理进度组件对父层暴露的操作方法。
 */
export interface ProcessingProgressExpose {
  openApproveDialog: () => Promise<void>
  openCloseDialog: () => void
  openRejectDialog: () => void
  openConfirmDialog: () => Promise<void>
  openUpdateProgressDialog: () => void
  openHandleCloseDialog: () => void
  openCreateTaskDialog: () => Promise<void>
  openTransferHandlerDialog: () => Promise<void>
}
