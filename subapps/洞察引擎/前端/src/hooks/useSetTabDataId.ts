import { onMounted, nextTick } from 'vue'
export function useSetTabDataId(prefix?: string) {
  onMounted(() => {
    nextTick(() => {
      setTabDataId(prefix)
    })
  })

  const setTabDataId = (prefix?: string) => {
    if (prefix) {
      const doms = document.querySelectorAll('.arco-tabs-tab-title')
      doms.forEach((el, index) => {
        el.setAttribute('data-testid', `${prefix}${index + 1}`)
      })
    }
  }
  return {
    setTabDataId
  }
}
