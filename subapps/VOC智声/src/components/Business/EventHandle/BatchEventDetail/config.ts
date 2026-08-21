import { taskStatusMap } from '@/components/Business/EventHandle/ehConstants'
import { BatchEventTaskProgressValue } from './beConstants'
import type {
  BatchEventProcessingConfig,
  BatchEventProcessingFooterAction,
  BatchEventProcessingStageKey,
  BatchEventProcessingStepDefinition,
  BatchEventProcessingStepItem,
  BatchEventProcessingStepStatus
} from './types'

/**
 * 批量事件处理阶段文案映射。
 */
export const batchEventProcessingStageLabelMap: Record<BatchEventProcessingStageKey, string> = {
  approve: '预警审核',
  confirm: '业务响应',
  handle: '闭环处理',
  close: '事件关闭'
}

/**
 * 处理进度步骤定义，状态值沿用单事件任务状态分组。
 */
export const batchEventProcessingStepDefinitions: BatchEventProcessingStepDefinition[] = [
  { label: '声音洞察', statusValues: [] },
  { label: '事件预警', statusValues: [] },
  { label: '预警审核', stage: 'approve', statusValues: taskStatusMap.approve },
  { label: '业务响应', stage: 'confirm', statusValues: taskStatusMap.confirm },
  { label: '闭环处理', stage: 'handle', statusValues: taskStatusMap.handle },
  { label: '事件关闭', stage: 'close', statusValues: taskStatusMap.close }
]

/**
 * 处理进度底部按钮配置。
 */
export const batchEventProcessingFooterActions: Record<
  BatchEventProcessingStageKey,
  BatchEventProcessingFooterAction[]
> = {
  approve: [
    { type: 'close', label: '关闭事件', variant: 'default' },
    { type: 'approve', label: '通过审核', variant: 'primary' }
  ],
  confirm: [
    { type: 'reject', label: '驳回事件', variant: 'default' },
    { type: 'confirm', label: '确认处理', variant: 'primary' }
  ],
  handle: [
    { type: 'handleClose', label: '关闭事件', variant: 'default' },
    { type: 'createTask', label: '新建任务', variant: 'primary' },
    { type: 'transferHandler', label: '转派处理人', variant: 'primary' }
  ],
  close: []
}

/**
 * 批量事件处理进度页静态配置。
 * 仅保留字段标签、占位符、弹窗文案和按钮定义，业务数据全部来自接口或页面上下文。
 */
export const batchEventProcessingProgressConfig: BatchEventProcessingConfig = {
  stepDefinitions: batchEventProcessingStepDefinitions,
  footerActions: batchEventProcessingFooterActions,
  stageModules: {
    approve: {
      stage: 'approve',
      mainOwner: {
        label: '业务责任人',
        required: true,
        placeholder: '请选择业务责任人',
        value: '',
        options: []
      },
      description: {
        label: '添加说明',
        value: '',
        placeholder: '请输入事件说明',
        maxlength: 200
      }
    },
    confirm: {
      stage: 'confirm',
      mainDepartment: {
        label: '主责部门',
        required: true,
        placeholder: '请选择主责部门',
        value: '',
        options: []
      },
      cooperationDepartment: {
        label: '协同部门',
        placeholder: '请选择协同部门',
        value: [],
        options: [],
        collapseTagCount: 1
      },
      handleMode: {
        label: '处理方式',
        required: true,
        value: 'voc-loop',
        options: []
      },
      userType: {
        label: '用户类型',
        placeholder: '请选择用户类型',
        value: [],
        options: [],
        collapseTagCount: 1
      },
      vehicleScene: {
        label: '用车场景',
        placeholder: '请选择用车场景',
        value: [],
        options: [],
        collapseTagCount: 1
      },
      pointIssue: {
        label: '观点问题',
        placeholder: '请选择观点问题',
        value: [],
        options: [],
        collapseTagCount: 1
      },
      description: {
        label: '添加说明',
        value: '',
        placeholder: '请输入事件说明',
        maxlength: 200
      }
    },
    handle: {
      stage: 'handle',
      vocLoop: {
        taskName: {
          label: '任务名称',
          value: '',
          placeholder: '请输入任务名称',
          maxlength: 20
        },
        description: {
          label: '任务说明',
          value: '',
          placeholder: '请输入任务说明',
          maxlength: 200
        },
        departmentRole: {
          label: '责任部门',
          required: true,
          value: '',
          options: []
        },
        departmentOwner: {
          label: '处理人员',
          required: true,
          placeholder: '请选择处理人员',
          value: '',
          options: []
        },
        progress: {
          label: '完成进度',
          required: true,
          placeholder: '请选择完成进度',
          value: BatchEventTaskProgressValue.NotStarted,
          options: []
        },
        progressDescription: {
          label: '进度说明',
          value: '',
          placeholder: '请输入进度说明',
          maxlength: 200
        },
        handler: {
          label: '处理人员',
          required: true,
          placeholder: '请选择处理人员',
          value: '',
          options: []
        },
        tasks: []
      },
      swordLoop: {
        tasks: []
      }
    },
    close: {
      stage: 'close',
      handleMode: {
        label: '处理方式',
        required: true,
        value: 'voc-loop',
        options: []
      },
      handleReason: {
        label: '处理结果',
        required: true,
        placeholder: '请选择处理结果',
        value: '',
        options: []
      },
      handler: {
        label: '处理人员',
        required: true,
        placeholder: '请选择处理人员',
        value: '',
        options: []
      },
      description: {
        label: '处理说明',
        value: '',
        placeholder: '请输入处理说明',
        maxlength: 200
      },
      taskTable: []
    }
  },
  confirmDialogModules: {
    approve: {
      title: '通过审核',
      content: '确认通过当前批量事件审核？',
      cancelText: '取消',
      confirmText: '确定',
      successMessage: '已通过审核'
    },
    confirm: {
      title: '确认处理',
      content: '确认提交当前批量事件处理信息？',
      cancelText: '取消',
      confirmText: '确定',
      successMessage: '已确认处理'
    },
    updateProgress: {
      title: '更新进度',
      content: '确认更新当前批量事件处理进度？',
      cancelText: '取消',
      confirmText: '确定',
      successMessage: '进度已更新'
    }
  },
  handleDialogModules: {
    vocLoop: {
      createTask: {
        title: '新建任务',
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '任务已新建'
      },
      editTask: {
        title: '编辑任务',
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '任务已更新'
      },
      transferTask: {
        title: '转派任务',
        handler: {
          label: '处理人员',
          required: true,
          placeholder: '请选择处理人员',
          value: '',
          options: []
        },
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '任务已转派'
      },
      transferHandler: {
        title: '转派处理人',
        handler: {
          label: '处理人员',
          required: true,
          placeholder: '请选择处理人员',
          value: '',
          options: []
        },
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '处理人已转派'
      },
      updateProgress: {
        title: '更新进度',
        progress: {
          label: '完成进度',
          required: true,
          placeholder: '请选择完成进度',
          value: '',
          options: []
        },
        description: {
          label: '处理说明',
          value: '',
          placeholder: '请输入处理说明',
          maxlength: 200
        },
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '任务进度已更新'
      },
      deleteTask: {
        title: '删除任务',
        content: '确认删除当前任务？',
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '任务已删除'
      },
      closeEvent: {
        title: '关闭事件',
        content: '确认关闭当前批量事件？',
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '事件已关闭'
      }
    },
    swordLoop: {
      closeEvent: {
        title: '关闭事件',
        content: '确认关闭当前批量事件？',
        cancelText: '取消',
        confirmText: '确定',
        successMessage: '事件已关闭'
      }
    }
  },
  closeDialogModule: {
    title: '关闭事件',
    closeReason: {
      label: '关闭原因',
      required: true,
      placeholder: '请选择关闭原因',
      value: '',
      options: []
    },
    description: {
      label: '关闭说明',
      value: '',
      placeholder: '请输入关闭说明',
      maxlength: 200
    },
    cancelText: '取消',
    confirmText: '确定',
    successMessage: '事件已关闭'
  },
  rejectDialogModule: {
    title: '驳回事件',
    rejectReason: {
      label: '驳回原因',
      required: true,
      placeholder: '请选择驳回原因',
      value: '',
      options: []
    },
    description: {
      label: '驳回说明',
      value: '',
      placeholder: '请输入驳回说明',
      maxlength: 200
    },
    cancelText: '取消',
    confirmText: '确定',
    successMessage: '事件已驳回'
  }
}

/**
 * 根据任务状态计算当前所处的处理阶段。
 * 未识别状态默认回落到“业务响应”，避免详情弹窗出现空白阶段。
 * @param taskStatus 任务状态码
 * @returns 当前处理阶段标识
 */
export const getBatchEventProcessingStageByTaskStatus = (
  taskStatus?: string
): BatchEventProcessingStageKey => {
  const normalizedTaskStatus = String(taskStatus || '')

  if (taskStatusMap.approve.includes(normalizedTaskStatus)) {
    return 'approve'
  }

  if (taskStatusMap.confirm.includes(normalizedTaskStatus)) {
    return 'confirm'
  }

  if (taskStatusMap.handle.includes(normalizedTaskStatus)) {
    return 'handle'
  }

  if (taskStatusMap.close.includes(normalizedTaskStatus)) {
    return 'close'
  }

  return 'confirm'
}

/**
 * 根据任务状态生成步骤条状态。
 * 90 状态沿用单事件详情口径，直接展示为全部完成。
 * @param taskStatus 任务状态码
 * @returns 步骤条展示数据
 */
export const buildBatchEventProcessingSteps = (
  taskStatus?: string
): BatchEventProcessingStepItem[] => {
  const normalizedTaskStatus = String(taskStatus || '')

  if (taskStatusMap.close.includes(normalizedTaskStatus)) {
    return batchEventProcessingStepDefinitions.map(step => ({
      label: step.label,
      stage: step.stage,
      status: 'completed'
    }))
  }

  const currentStage = getBatchEventProcessingStageByTaskStatus(normalizedTaskStatus)
  const currentStepIndex = batchEventProcessingStepDefinitions.findIndex(
    step => step.stage === currentStage
  )

  return batchEventProcessingStepDefinitions.map((step, index) => {
    let status: BatchEventProcessingStepStatus

    if (index < currentStepIndex) {
      status = 'completed'
    } else if (index === currentStepIndex) {
      status = 'current'
    } else {
      status = 'pending'
    }

    return {
      label: step.label,
      stage: step.stage,
      status
    }
  })
}

/**
 * 根据任务状态获取当前业务阶段文案。
 * @param taskStatus 任务状态码
 * @returns 当前阶段文案
 */
export const getBatchEventProcessingBusinessTag = (taskStatus?: string) => {
  return batchEventProcessingStageLabelMap[getBatchEventProcessingStageByTaskStatus(taskStatus)]
}
