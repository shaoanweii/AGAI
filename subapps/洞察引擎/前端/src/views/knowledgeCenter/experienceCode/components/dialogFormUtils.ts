import { computed, inject } from 'vue'
import type { ExperienceCodeType, ExperienceCodeTypeOption, OptionItem, StatusValue } from './types'
import { experienceCodePageContextKey } from '../context'
import { validateSynonyms, normalizeSynonyms, splitSynonyms } from '../../shared/synonym'
import {
  resolveExperienceCodeEnabledStatus,
  resolveExperienceCodeStatusOptions
} from '../statusOptions'

/**
 * 弹框内的启用状态统一复用页面级字典，缺失时退回本地兜底值。
 */
export const useExperienceCodeStatusField = () => {
  const pageContext = inject(experienceCodePageContextKey, null)
  const statusOptions = computed<OptionItem<StatusValue>[]>(() => {
    return pageContext?.statusOptions.value || resolveExperienceCodeStatusOptions()
  })
  const enabledStatusValue = computed<StatusValue>(() => {
    return resolveExperienceCodeEnabledStatus(statusOptions.value)
  })

  return {
    statusOptions,
    enabledStatusValue
  }
}

/**
 * 类型选项统一来自页面级 conditions.tagLibeType，接口未返回时直接表现为空数组。
 */
export const useExperienceCodeTypeField = () => {
  const pageContext = inject(experienceCodePageContextKey, null)
  const typeOptions = computed<ExperienceCodeTypeOption[]>(() => {
    return pageContext?.typeOptions.value || []
  })
  const firstTypeValue = computed<ExperienceCodeType>(() => {
    return typeOptions.value[0]?.value || ''
  })

  return {
    typeOptions,
    firstTypeValue
  }
}

/**
 * 表单回填和提交前统一做字符串清洗，减少接口脏数据残留空格。
 */
export const normalizeDialogText = (value: unknown) => {
  return String(value ?? '').trim()
}

/**
 * 同义词输入允许中英文逗号混输，但提交和校验统一折算为英文逗号，保证接口拆词口径稳定。
 */
export const normalizeSynonymsField = (value: unknown) => {
  return normalizeSynonyms(value)
}

/**
 * 同义词拆分逻辑统一复用，避免弹框校验和提交流程出现分隔符口径不一致。
 */
export const splitSynonymsField = (value: unknown) => {
  return splitSynonyms(value)
}

/**
 * 状态值只接受 0/1 口径，其余异常值统一回退到启用。
 */
export const normalizeDialogStatus = (value: unknown): StatusValue => {
  return normalizeDialogText(value) === '0' ? '0' : '1'
}

/**
 * 名称类字段的基础校验统一收敛，避免同类弹框维护多套长度与空值规则。
 */
export const createNameValidator = (fieldLabel: string) => {
  return (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    const input = normalizeDialogText(value)
    if (!input) {
      callback(new Error(`${fieldLabel}不能为空`))
      return
    }
    if (input.length > 50) {
      callback(new Error(`${fieldLabel}不能超过50个字符`))
      return
    }
    callback()
  }
}

/**
 * 同义词字段允许中英文逗号混输，但会在校验和提交阶段统一折算成英文逗号。
 */
export const validateSynonymsField = validateSynonyms
