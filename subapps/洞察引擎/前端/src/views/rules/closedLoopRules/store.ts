import { reactive } from 'vue'
import { findAllResourceTree } from '@/api/rules'
import useUserStore from '@/stores/modules/user'

/**
 * 闭环规则共享组件的状态管理
 * 仅包含共享组件 CcPersonnelSelect 和 DataResourceCascader 所需的数据
 * 单点事件和批量事件模块各自维护独立的 store
 */

export const rulesStore = reactive<any>({
  // 词库资源信息（供 DataResourceCascader 使用）
  dataResource: {
    loading: false,
    allList: [], // 原始的数据
    accountList: [], // 账号数据
    ruleList: [] // 规则数据
  },
  // 部门员工树（供 CcPersonnelSelect 使用）
  deptEmployeeTree: []
})

export const rulesActions = {
  /**
   * 获取资源树（账号、规则词库）
   */
  async updateAllResourceTree() {
    try {
      rulesStore.dataResource.loading = true
      const response = await findAllResourceTree({ typeList: ['account', 'rule'] })
      if (response.success) {
        rulesStore.dataResource.allList = response.result || []
      }
      rulesStore.dataResource.accountList = (response.result || []).filter(
        (item: any) => item.type === 'account'
      )
      rulesStore.dataResource.ruleList = (response.result || []).filter(
        (item: any) => item.type === 'rule'
      )
    } catch (error) {
      console.error('获取资源树失败', error)
    } finally {
      rulesStore.dataResource.loading = false
    }
  },

  /**
   * 获取部门员工树（有缓存则直接返回，无缓存则请求接口）
   * 供 CcPersonnelSelect 组件使用
   */
  async updateDeptEmployeeTree() {
    if (!rulesStore.deptEmployeeTree.length) {
      const userStore = useUserStore()
      const list = await userStore.getDepartAccountTree({ silent: true })
      rulesStore.deptEmployeeTree = Array.isArray(list) ? list : []
    }
    return rulesStore.deptEmployeeTree
  }
}
