export default function useComputedCascaderWidth() {
  const refDom = ref()
  const baseWidth = ref()
  const getWidht = () => {
    baseWidth.value = refDom.value?.$el?.parentElement?.offsetWidth - 52 - 36
  }
  const handleChange = () => {
    getWidht()
  }
  onMounted(() => {
    nextTick(() => {
      getWidht()
    })
    window.addEventListener('resize', handleChange)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleChange)
  })

  const formatLabelHandle = (ids: string[], options: any, key: string, separator = '#') => {
    const str = options.map((option: any) => option[key])?.join(separator)
    const fontSize = 12

    let newWidth = baseWidth.value
    if (ids?.length === 1) {
      newWidth = baseWidth.value
    } else if (ids?.length < 10) {
      newWidth = baseWidth.value - 45
    } else if (ids?.length < 100) {
      newWidth = baseWidth.value - 55
    } else if (ids?.length < 1000) {
      newWidth = baseWidth.value - 61
    }
    let len = Math.floor(newWidth / fontSize) - 1
    len = len < 0 ? 0 : len
    return str?.length >= len ? `${str.substring(0, len)}...` : str
  }

  return {
    refDom,
    formatLabelHandle
  }
}
