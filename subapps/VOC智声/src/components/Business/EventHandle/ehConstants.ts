/**
 * 二次确认弹窗枚举类型
 * Pass 审核
 * Handle 确认
 * Close 关闭
 * Confirm 确认
 */
export enum DoubleConfirmatioTypeEnum {
  Pass = 'pass',
  Handle = 'handle',
  Close = 'close',
  Confirm = 'confirm'
}
// 二次确认弹窗标题
export const DoubleConfirmatioTitleByType: Record<DoubleConfirmatioTypeEnum, string> = {
  pass: '通过审核',
  handle: '确认处理',
  confirm: '确认处理',
  close: '关闭事件'
}

// 二次确认弹窗内容
export const DoubleConfirmatioContentByType: Record<DoubleConfirmatioTypeEnum, string> = {
  pass: '是否确认通过事件？',
  handle: '是否确认处理事件？',
  confirm: '是否确认处理事件？',
  close: '是否确认关闭事件？'
}

/**
 * @description: 关闭、驳回事件枚举类型
 * @return {*} close 关闭， reject 驳回
 */
export enum CloseRejectEventEnum {
  Close = 'close',
  Reject = 'reject'
}

/**
 * 关闭、驳回事件标题
 */
export const CloseRejectEventTitleByType: Record<CloseRejectEventEnum, string> = {
  close: '关闭事件',
  reject: '驳回事件'
}

/**
 * @description:  批量处理弹窗枚举类型
 * 审核(pass) 确认(handle) 分派(dispatch) 关闭(close) 单条分派(singleDispatch)
 */
export enum BatchProcessingTypeEnum {
  Pass = 'pass',
  Handle = 'handle',
  Close = 'close',
  Dispatch = 'dispatch',
  SingleDispatch = 'singleDispatch'
}

// 批量处理弹窗标题
export const BatchProcessTitleMapByType: Record<BatchProcessingTypeEnum, string> = {
  pass: '批量审核',
  handle: '批量确认',
  close: '批量关闭',
  dispatch: '批量分派',
  singleDispatch: '分派'
}

/**
 * 操作类型枚举。审核通过(passResolve)、关闭事件(closeReject)、确认处理(handleResolve)、驳回事件(rejectEvent)
 */
export enum OperationTypeEnum {
  PassResolve = 'passResolve',
  CloseReject = 'closeReject',
  HandleResolve = 'handleResolve',
  RejectEvent = 'rejectEvent'
}

/**
 * @description: 审核操作类型
 * @return {*}
 */
export const PassOperationTypeOptions = [
  {
    label: '审核通过',
    value: OperationTypeEnum.PassResolve
  },
  {
    label: '关闭事件',
    value: OperationTypeEnum.CloseReject
  }
]

/**
 * @description: 确认操作类型
 * @return {*}
 */
export const HandleOperationTypeOptions = [
  {
    label: '确认处理',
    value: OperationTypeEnum.HandleResolve
  },
  {
    label: '驳回事件',
    value: OperationTypeEnum.RejectEvent
  }
]

/**
 * @description: 事件类型
 * @return {*}
 * VIEW("查看", "view"),APPROVE("审核", "approve"),CONFIRM("确认", "confirm"),REJECT("驳回", "reject"),ASSIGN("分派","assign"),HANDLE("处理", "handle"),CLOSE("关闭", "close"), inProc 进行中
 */
export enum EventType {
  VIEW = 'view',
  APPROVE = 'approve',
  CONFIRM = 'confirm',
  REJECT = 'reject',
  ASSIGN = 'assign',
  HANDLE = 'handle',
  CLOSE = 'close',
  IN_PROC = 'inProc'
}

/**
 * 处理方式枚举
 * 仅回评-> only reply
 * 仅私信-> only private msg
 * 回评和私信-> reply and private msg
 */
export enum HandleModeEnum {
  // 仅回评
  OnlyReply = 'only reply',
  // 仅私信
  OnlyPrivateMsg = 'only private msg',
  // 回评和私信
  ReplyAndPrivateMsg = 'reply and private msg'
}

// 无效事件
export const INVALID_EVENT = 'invalid'
// 有效事件
export const VALID_EVENT = 'valid'

// 预警审核 -> 10 11 -> 审核、关闭
// 处理确认 -> 20 -> 确认、驳回
// 事件处理 -> 30 40 ->分派、处理
// 事件关闭 -> 90
// 所有节点都有查看

//  事件状态与节点的映射
export const taskStatusMap = {
  approve: ['10', '11'],
  confirm: ['20'],
  handle: ['30', '40'],
  close: ['90']
}

// 事件优先级与处理建议的映射
export const eventPriorityTipMap = {
  p0: '高优先级处理',
  p1: '中高优先级处理',
  p2: '中优先级处理',
  p3: '中低优先级处理',
  p4: '低优先级处理'
}

// 事件等级与处理建议的映射
export const eventLevelTipMap = {
  S: '特别重大事件',
  A: '重大事件',
  B: '较大事件',
  C: '一般事件'
}
