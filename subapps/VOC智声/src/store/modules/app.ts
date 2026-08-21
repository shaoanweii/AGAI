import { getAuthDataUrl } from '@/api/common'
import { openWindow } from '@/utils'
import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'

interface UserInfo {
  id?: string
  userName?: string
  userAccount?: string
  email?: string
  avatar?: string
  name?: string
}

export interface AppState {
  isCollapse: boolean
  user: UserInfo
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    isCollapse: true,
    user: {
      id: undefined,
      userName: undefined,
      userAccount: undefined
    }
  }),

  getters: {
    userInfo: state => state.user
  },

  actions: {
    setIsCollapse(isCollapse: boolean) {
      this.isCollapse = isCollapse
    },

    toggleCollapse() {
      this.isCollapse = !this.isCollapse
    },

    setUser(user: Partial<UserInfo>) {
      this.user = { ...this.user, ...user }
    },

    logout() {},

    /**
     * 打开本地智能问答入口，保留既有菜单调用契约。
     */
    async handleCanswerAuth() {
      try {
        const response = await getAuthDataUrl({ userCode: this.userInfo.userName })
        if (response.success && response.result) {
          openWindow(response.result)
          return
        }
        ElMessage.warning('智能问答暂不可用')
      } catch (error) {
        console.warn('打开智能问答失败:', error)
      }
    }
  }
})
