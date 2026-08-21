import { ref, type Ref } from 'vue'

/**
 * @description: 状态切换
 * @param {boolean} defaultValue
 * @return {*}
 */
export function useToggle(defaultValue: boolean = false) {
  const state: Ref<boolean> = ref(defaultValue)
  const toggle = (value: boolean = !state.value, cb?: () => void) => {
    state.value = value
    // eslint-disable-next-line @typescript-eslint/no-unused-expressions
    cb && cb()
  }

  return [state, toggle] as const
}
