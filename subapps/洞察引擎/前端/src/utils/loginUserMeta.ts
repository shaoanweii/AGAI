import { USER_ID_KEY, USER_NAME_KEY } from '@/constant'

export interface LoginUserMeta {
  operatorName: string
  employeeId: string
}

export const getLoginUserMetaOrNull = (): LoginUserMeta | null => {
  const name = (localStorage.getItem(USER_NAME_KEY) || '').trim()
  const employeeId = (localStorage.getItem(USER_ID_KEY) || '').trim()
  if (!name || !employeeId) return null
  return { operatorName: `${name} ${employeeId}`, employeeId }
}
