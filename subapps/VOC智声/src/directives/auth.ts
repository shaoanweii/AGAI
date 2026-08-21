import { hasPermission } from '@/utils/permission'

export const authDirective = {
  mounted(el: HTMLElement, binding: any) {
    const isAuth = hasPermission(binding.value)
    // 无权限
    if (!isAuth) {
      el.parentNode?.removeChild(el)
    }
  }
}
