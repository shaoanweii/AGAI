/**
 * 控制弹窗，抽屉的显示隐藏
 */
export function useModal() {
  const visible = ref(false)
  const showVisible = (cb?: () => void) => {
    visible.value = true
    cb && cb()
  }

  const hideVisbble = (cb?: () => void) => {
    visible.value = false
    cb && cb()
  }
  return {
    visible,
    showVisible,
    hideVisbble
  }
}
