const mobileContainer = {
  init: async (_options?: unknown) => ({ success: true }),
  login: async () => ({ success: true }),
  getUserInfo: async () => ({
    userAccount: 'demo-admin',
    userName: '演示管理员',
    loginID: 'demo-admin'
  }),
  open: async ({ url, uri }: { url?: string; uri?: string } = {}) => {
    const target = url || uri
    if (target) window.location.href = target
  },
  openSystemBrowser: async (url: string) => {
    window.location.href = url
  }
}

export const HOST = 'local-demo'
export default mobileContainer
