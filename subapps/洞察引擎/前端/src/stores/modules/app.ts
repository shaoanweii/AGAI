import { defineStore } from 'pinia'

const useAppStore = defineStore('app', {
  state: (): Record<any, any> => ({
    // 分页数据大于等于10 展示分页器
    showPaginationMinLength: 10,
    fileData: null
  }),
  getters: {
    isDownloadFlag(): any {
      // 0 失败 1 成功 其余处理中
      return (
        !['0', '1'].includes(this.fileData.status) ||
        (!this.fileData.status && !this.fileData.taskId)
      )
    }
  },
  actions: {
    setFileData(file: any) {
      this.fileData = file
    }
  }
})

export default useAppStore
