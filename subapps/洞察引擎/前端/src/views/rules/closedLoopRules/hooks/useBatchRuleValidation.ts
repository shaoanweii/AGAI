import { reactive } from 'vue'
import type { FormRules } from 'element-plus'
import type { BatchRuleRecord } from '@/views/rules/closedLoopRules/BatchEvent/types'

/**
 * 批量规则表单校验集中在此，便于和单点规则保持一致的维护方式。
 * @param form 批量规则表单响应式对象
 * @returns { rules: FormRules<BatchRuleRecord> }
 */
export function useBatchRuleValidation(
  form: BatchRuleRecord
): { rules: FormRules<BatchRuleRecord> } {
  /**
   * 规则名称统一限制长度和字符集，避免不同规则表单出现口径漂移。
   * @param _rule 当前校验规则
   * @param value 当前字段值
   * @param callback Element Plus 校验回调
   */
  const validateRuleName = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (!value) {
      callback()
      return
    }

    if (value.length > 30) {
      callback(new Error('规则名称最多输入30个字符'))
      return
    }

    if (!/^[A-Za-z0-9\u4e00-\u9fa5]+$/.test(value)) {
      callback(new Error('规则名称仅支持中英文和数字'))
      return
    }

    callback()
  }

  /**
   * 审核人员在批量规则中调整为始终必填，保持和单点规则一致的口径。
   * @param _rule 当前校验规则
   * @param _value 当前字段值
   * @param callback Element Plus 校验回调
   */
  const validateAuditDepartment = (
    _rule: unknown,
    _value: unknown,
    callback: (error?: Error) => void
  ) => {
    if (form.auditDepartment?.id && form.auditor?.id) {
      callback()
      return
    }

    callback(new Error('请选择审核人员'))
  }

  /**
   * 业务责任人要求部门和人员同时完整，避免只保存半套责任信息。
   * @param _rule 当前校验规则
   * @param _value 当前字段值
   * @param callback Element Plus 校验回调
   */
  const validateMainDepartment = (
    _rule: unknown,
    _value: unknown,
    callback: (error?: Error) => void
  ) => {
    if (form.mainDepartment?.id && form.mainResponder?.id) {
      callback()
      return
    }

    callback(new Error('请选择业务责任人'))
  }

  const rules = reactive<FormRules<BatchRuleRecord>>({
    ruleName: [
      { required: true, message: '请输入规则名称', trigger: 'blur' },
      { validator: validateRuleName, trigger: 'blur' }
    ],
    brand: [{ required: true, message: '请选择品牌', trigger: 'change' }],
    processPriority: [{ required: true, message: '请选择处理优先级', trigger: 'change' }],
    auditMethod: [{ required: true, message: '请选择审核方式', trigger: 'change' }],
    auditDepartment: [
      { required: true, message: '请选择审核人员', trigger: 'change' },
      { validator: validateAuditDepartment, trigger: 'change' }
    ],
    mainDepartment: [
      { required: true, message: '请选择业务责任人', trigger: 'change' },
      { validator: validateMainDepartment, trigger: 'change' }
    ],
    isEnabled: [{ required: true, message: '请选择是否启用', trigger: 'change' }]
  })

  return { rules }
}
