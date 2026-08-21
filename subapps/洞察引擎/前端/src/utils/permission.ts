import useUserStore from '@/stores/modules/user'

/**
 * 根据code判断是否有权限
 * @param btnPermissions
 */
export const hasPermission = (btnPermissions: string) => {
  const userStore = useUserStore()
  const userPermission = userStore.buttonPerm as string[]
  if (btnPermissions) {
    return userPermission.includes(btnPermissions)
  }
  return false
}
