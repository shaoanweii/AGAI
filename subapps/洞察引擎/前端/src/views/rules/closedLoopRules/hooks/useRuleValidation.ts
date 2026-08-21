import { reactive } from 'vue'
import type { FormRules } from 'element-plus'

// 校验逻辑集中在此，组件通过引入 rules 即可
// 说明：保持与现有交互一致，尽量不改变行为
export function useRuleValidation(form: any, isSingleRule: { value: boolean }) {
  // ===== 自定义校验函数（结合显示条件） =====
  const validateConditions = (_rule: any, _value: any, callback: any) => {
    // 至少一条条件
    if (!Array.isArray(form.conditions) || form.conditions.length === 0) {
      return callback(new Error('请至少添加一条条件'))
    }
    // 条件类型不可重复
    const types = form.conditions.map((r: any) => r?.conditionType).filter(Boolean)
    const unique = new Set(types)
    if (types.length !== unique.size) {
      return callback(new Error('条件类型不可重复'))
    }
    // 数据必须填写完成
    const isComplete = form.conditions.every((r: any) => {
      // 检查基础字段是否存在
      if (!r.conditionType || !r.operator || !r.option) return false
      // 对于 value 字段的检查：
      // - 如果是数组，需要长度大于0
      // - 如果不是数组，只需要存在即可（非空字符串、数字等）
      if (Array.isArray(r.value)) {
        return r.value.length > 0
      } else {
        return r.value !== undefined && r.value !== null && r.value !== ''
      }
    })
    if (!isComplete) {
      return callback(new Error('请填写完整条件'))
    }
    return callback()
  }
  const validateAuditDepartment = (_rule: any, _value: any, callback: any) => {
    if (form?.auditDepartment?.id && form?.auditor?.id) return callback()
    callback(new Error('请选择审核人员'))
  }
  const validateMainDepartment = (_rule: any, _value: any, callback: any) => {
    if (form?.mainDepartment?.id && form?.mainResponder?.id) return callback()
    callback(new Error('请选择责任部门'))
  }
  const validateCcPersonnel = (_rule: any, _value: any, callback: any) => {
    const v: any = (form as any).ccPersonnel
    if (Array.isArray(v)) return v.length > 0 ? callback() : callback(new Error('请选择抄送人员'))
    callback(new Error('请选择抄送人员'))
  }
  const validateConfirmMethod = (_rule: any, _value: any, callback: any) => {
    if (!isSingleRule.value) return callback()
    if (form.confirmMethod) return callback()
    callback(new Error('请选择确认方式'))
  }
  const validateHandleType = (_rule: any, _value: any, callback: any) => {
    if (isSingleRule.value) return callback()
    if ((form as any).handleType) return callback()
    callback(new Error('请选择处理方式'))
  }
  const validateRuleAlert = (_rule: any, _value: any, callback: any) => {
    const a = form.ruleAlert as any
    if (!a) return callback()
    const ok = !!a.alertType && !!a.alertFrequency && !!a.alertTime
    return ok ? callback() : callback(new Error('请完整填写预警设置'))
  }

  // 表单规则
  const rules = reactive<FormRules>({
    // 规则名称
    ruleName: [
      { required: true, message: '请输入规则名称', trigger: 'blur' },
      { max: 30, message: '规则名称不能超过30个字', trigger: 'blur' },
      {
        pattern: /^[\u4e00-\u9fa5a-zA-Z0-9]+$/,
        message: '规则名称仅支持中英文和数字',
        trigger: 'blur'
      }
    ],
    // 数据来源（多选）
    dataSource: [{ type: 'array', required: true, message: '请选择数据来源', trigger: 'change' }],
    // 品牌
    brandCode: [{ required: true, message: '请选择品牌', trigger: 'change' }],
    // 条件配置（至少一条）
    conditions: [
      { required: true, message: '请添加条件', trigger: 'change' },
      { validator: validateConditions, trigger: 'change' }
    ],
    // 事件等级 / 处理优先级 / 审核方式
    eventLevel: [{ required: true, message: '请选择事件等级', trigger: 'change' }],
    processPriority: [{ required: true, message: '请选择处理优先级', trigger: 'change' }],
    auditMethod: [{ required: true, message: '请选择审核方式', trigger: 'change' }],
    auditDepartment: [
      { required: true, message: '请选择', trigger: 'change' },
      { validator: validateAuditDepartment, trigger: 'change' }
    ],
    // 责任部门 / 抄送人员
    mainDepartment: [
      { required: true, message: '请选择', trigger: 'change' },
      { validator: validateMainDepartment, trigger: 'change' }
    ],
    // ccPersonnel: [
    //   { required: true, message: '请选择', trigger: 'change' },
    //   { validator: validateCcPersonnel, trigger: 'change' }
    // ],
    // 确认方式（仅单条规则时必填）
    confirmMethod: [
      { required: true, message: '请选择', trigger: 'change' },
      { validator: validateConfirmMethod, trigger: 'change' }
    ],
    // 预警设置（存在时要求完整）
    ruleAlert: [],
    // 处理方式（仅批量规则时必填）
    handleType: [{ validator: validateHandleType, trigger: 'change' }],
    // 是否启用
    isEnabled: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
  })

  return { rules }
}
