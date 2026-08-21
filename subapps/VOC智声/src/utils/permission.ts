import useUserStore from '@/store/modules/user'
import { isLocalDemo } from '@/utils/env'

interface MenuPermissionItem {
  name?: string
  permissionKey?: string
  path?: string
  children?: MenuPermissionItem[]
}

/**
 * 递归判断菜单树中是否存在满足条件的菜单项。
 * @param menus 菜单树数据
 * @param matcher 菜单匹配函数
 * @returns 是否命中目标菜单
 */
const hasMatchedMenuPermission = (
  menus: MenuPermissionItem[] | null | undefined,
  matcher: (menu: MenuPermissionItem) => boolean
): boolean => {
  if (!menus?.length) return false

  return menus.some(menu => {
    if (matcher(menu)) {
      return true
    }

    if (menu?.children?.length) {
      return hasMatchedMenuPermission(menu.children, matcher)
    }

    return false
  })
}

/**
 * 根据key判断是否有权限
 * @param btnPermissions
 */
export const hasPermission = (btnPermissions: string) => {
  if (isLocalDemo()) return true
  const userStore = useUserStore()
  const userPermission = userStore.buttonPerm as string[]
  if (btnPermissions) {
    return userPermission.includes(btnPermissions)
  }
  return false
}

/**
 * 递归判断菜单树中是否包含智能问数入口。
 * @param menus 菜单树数据
 * @returns 是否命中 canswer 菜单
 */
export const hasCanswerMenuPermission = (menus?: MenuPermissionItem[] | null): boolean => {
  return hasMatchedMenuPermission(menus, menu => {
    return menu?.permissionKey === 'linkUrl' && menu?.path === 'canswer'
  })
}

/**
 * 递归判断菜单树中是否包含原声查询入口。
 * @param menus 菜单树数据
 * @returns 是否命中原声查询菜单
 */
export const hasOriginalSoundQueryMenuPermission = (
  menus?: MenuPermissionItem[] | null
): boolean => {
  return hasMatchedMenuPermission(menus, menu => {
    return (
      menu?.permissionKey === 'selfServiceOriginalSoundQuery' ||
      menu?.path === '/selfService/originalSoundQuery'
    )
  })
}

/**
 * 递归判断菜单树中是否包含客情直驱明细页入口。
 * @param menus 菜单树数据
 * @param permissionKey 目标明细页路由 name / 菜单 permissionKey
 * @param path 目标明细页路径
 * @returns 是否命中目标明细页菜单
 */
export const hasCustomerDrivenDirectlyDetailMenuPermission = (
  menus: MenuPermissionItem[] | null | undefined,
  permissionKey: string,
  path: string
): boolean => {
  return hasMatchedMenuPermission(menus, menu => {
    return menu?.permissionKey === permissionKey || menu?.path === path
  })
}
