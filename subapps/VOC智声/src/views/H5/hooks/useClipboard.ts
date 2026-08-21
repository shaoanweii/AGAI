import { onBeforeUnmount, ref } from 'vue'
import { showToast } from 'vant'

/**
 * H5 端复制 Hook。
 * - 优先使用 Clipboard API
 * - 失败时降级到 execCommand 复制
 * - 统一维护复制状态与移动端提示
 */
export function useClipboard() {
  const copied = ref(false)
  const text = ref('')
  const isSupported = ref(false)
  let copiedTimer: number | null = null

  /**
   * 根据当前运行环境同步复制能力，兼容部分企微内嵌浏览器。
   */
  const syncSupportedState = () => {
    if (typeof navigator === 'undefined' || typeof document === 'undefined') {
      isSupported.value = false
      return
    }

    isSupported.value =
      !!navigator.clipboard?.writeText || typeof document.execCommand === 'function'
  }

  /**
   * 通过临时输入框降级复制文本，兼容不支持 Clipboard API 的环境。
   * @param value 需要写入剪贴板的文本
   * @returns 是否复制成功
   */
  const copyByExecCommand = (value: string) => {
    if (typeof document === 'undefined' || typeof document.execCommand !== 'function') {
      return false
    }

    const input = document.createElement('input')
    input.setAttribute('readonly', 'readonly')
    input.value = value
    input.style.position = 'fixed'
    input.style.left = '-9999px'
    input.style.top = '-9999px'
    document.body.appendChild(input)
    input.select()
    input.setSelectionRange(0, value.length)

    try {
      return document.execCommand('copy')
    } finally {
      document.body.removeChild(input)
    }
  }

  /**
   * 设置复制成功后的状态，并在短时间后自动恢复 copied 标记。
   * @param value 本次复制成功的文本
   */
  const markCopied = (value: string) => {
    text.value = value
    copied.value = true

    if (copiedTimer !== null) {
      window.clearTimeout(copiedTimer)
    }

    copiedTimer = window.setTimeout(() => {
      copied.value = false
      copiedTimer = null
    }, 1500)
  }

  /**
   * 复制文本到剪贴板，并在移动端统一给出提示反馈。
   * @param value 需要复制的文本
   * @returns 是否复制成功
   */
  const copy = async (value: string) => {
    if (!value) {
      showToast('复制链接为空')
      return false
    }

    syncSupportedState()
    if (!isSupported.value) {
      showToast('复制失败，请长按链接复制')
      return false
    }

    if (navigator.clipboard?.writeText) {
      try {
        await navigator.clipboard.writeText(value)
        markCopied(value)
        showToast('复制成功')
        return true
      } catch (error) {
        console.warn('Clipboard API 复制失败，尝试降级复制:', error)
      }
    }

    const success = copyByExecCommand(value)
    if (success) {
      markCopied(value)
      showToast('复制成功')
      return true
    }

    showToast('复制失败，请长按链接复制')
    return false
  }

  syncSupportedState()

  onBeforeUnmount(() => {
    if (copiedTimer !== null) {
      window.clearTimeout(copiedTimer)
      copiedTimer = null
    }
  })

  return {
    copy,
    text,
    copied,
    isSupported
  }
}
