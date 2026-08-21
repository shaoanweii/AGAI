import useConditions from '@/hooks/useConditions'

export const rulesTestStore = reactive<any>({
  // 字典数据
  conditions: {}
})

export const rulesTestActions = {
  async updateDicts() {
    try {
      // 该 store 需要主动等待字典完成加载，因此关闭自动拉取避免重复请求。
      const { conditions, getConditions } = useConditions({
        url: '/insights/ruleTest/conditions',
        immediate: false
      })
      // 显式等待字典加载完成，避免调用方误以为 store 已经准备就绪。
      await getConditions()
      rulesTestStore.conditions = conditions || {}
    } catch (e) {
      console.error('获取是否启用的字典失败', e)
    }
  }
}
