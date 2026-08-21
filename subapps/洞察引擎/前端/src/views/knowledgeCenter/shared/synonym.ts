const CHINESE_COMMA_REGEXP = /，/g
const DEFAULT_SYNONYM_MAX_LENGTH = 10000
const DEFAULT_SYNONYM_MAX_MESSAGE = '同义词不能超过10000个字符'
const DEFAULT_SYNONYM_EMPTY_ITEM_MESSAGE = '同义词中不能包含空项，请检查逗号位置'

type SynonymValidateCallback = (error?: Error) => void

type SynonymValidatorOptions = {
  max?: number
  maxMessage?: string
  emptyItemMessage?: string
}

/**
 * 知识中心同义词统一兼容中英文逗号输入，提交前收敛为英文逗号，保证后端拆词口径一致。
 */
export const normalizeSynonyms = (value: unknown) => {
  return String(value ?? '')
    .trim()
    .replace(CHINESE_COMMA_REGEXP, ',')
}

/**
 * 同义词拆分统一基于归一化结果执行，避免不同模块对空格和分隔符的处理出现偏差。
 */
export const splitSynonyms = (value: unknown) => {
  const normalizedValue = normalizeSynonyms(value)
  if (!normalizedValue) {
    return []
  }
  return normalizedValue.split(',').map(item => item.trim())
}

/**
 * 知识中心同义词校验统一收口到共享层，避免多个弹框各自维护长度和空项判断导致提示口径漂移。
 */
export const createSynonymValidator = (options: SynonymValidatorOptions = {}) => {
  const max = options.max ?? DEFAULT_SYNONYM_MAX_LENGTH
  const maxMessage = options.maxMessage ?? DEFAULT_SYNONYM_MAX_MESSAGE
  const emptyItemMessage = options.emptyItemMessage ?? DEFAULT_SYNONYM_EMPTY_ITEM_MESSAGE

  /**
   * 这里保留 Element Plus 表单校验签名，方便页面直接复用而不需要额外封装适配层。
   */
  return (_rule: unknown, value: string, callback: SynonymValidateCallback) => {
    const normalizedValue = normalizeSynonyms(value)
    if (!normalizedValue) {
      callback()
      return
    }
    if (normalizedValue.length > max) {
      callback(new Error(maxMessage))
      return
    }

    const synonymList = splitSynonyms(value)
    if (synonymList.some(item => !item)) {
      callback(new Error(emptyItemMessage))
      return
    }

    callback()
  }
}

/**
 * 默认校验器覆盖知识中心大部分表单场景，只有提示文案差异时再按需传入配置。
 */
export const validateSynonyms = createSynonymValidator()
