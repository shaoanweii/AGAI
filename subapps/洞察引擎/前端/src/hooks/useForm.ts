// Element Plus 表单规则类型定义
interface FieldRule {
  required?: boolean
  message?: string
  validator?: (rule: any, value: any, callback: (error?: string | Error) => void) => void
  [key: string]: any
}

/**
 * 校验规则
 */
export function useFormRules() {
  /**
   * 生成默认必传rule
   * @param message
   * @param fieldRule
   */
  function createRequiredRule(message: string, fieldRule?: FieldRule) {
    return {
      required: true,
      message,
      ...fieldRule
    }
  }

  /**
   * 生成字符长度校验
   * @param field
   * @param minLen
   * @param maxLen
   * @param fieldRule
   */
  function createStrLengthRule(
    field: string,
    minLen: number,
    maxLen: number,
    fieldRule?: FieldRule
  ) {
    return {
      required: true,
      validator: (rule: any, value: any, callback: (error?: string | Error) => void) => {
        if (!value) {
          callback(new Error(`${field}必填`))
          return
        }
        const strValue = value?.toString()
        if (strValue?.length < minLen || strValue?.length > maxLen) {
          if (minLen === 0) {
            callback(new Error(`${field}字符限制${maxLen}以内`))
          } else {
            callback(new Error(`${field}字符限制${minLen}-${maxLen}字符`))
          }
          return
        }
        // 验证通过时调用 callback()
        callback()
      },
      ...fieldRule
    }
  }

  return {
    createRequiredRule,
    createStrLengthRule
  }
}
