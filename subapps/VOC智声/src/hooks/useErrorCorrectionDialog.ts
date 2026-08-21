import { ref } from 'vue'
import { ElMessage } from 'element-plus'

type Options = {
  emptyMessage?: string
}

const normalizeDataIdList = (raw: unknown): string[] => {
  if (!Array.isArray(raw)) return []
  return raw
    .map(item => (item == null ? '' : String(item)))
    .map(item => item.trim())
    .filter(Boolean)
}

export function useErrorCorrectionDialog(options: Options = {}) {
  const visible = ref(false)
  const dataIdList = ref<string[]>([])

  const open = (rawDataIdList: unknown) => {
    const normalized = normalizeDataIdList(rawDataIdList)
    if (!normalized.length) {
      ElMessage.warning(options.emptyMessage || '未获取到可纠错的数据ID')
      return false
    }
    dataIdList.value = normalized
    visible.value = true
    return true
  }

  return {
    visible,
    dataIdList,
    open,
    normalizeDataIdList
  }
}

