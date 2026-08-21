import { defineStore } from 'pinia'
import { ref, computed, watchEffect } from 'vue'
import { conditions, getDetailBase, updateOriginalSoundDetail } from '@/api/singlePointEvent'
import {
  findDepartTree,
  findDepartAccountTree,
  findAccountByDeptId,
  findDepartAccountTreeByDeptId
} from '@/api/common'
import useUserStore from '@/store/modules/user'
import type { SingleEventDetailBaseVo } from '@/api/singlePointEvent/types'
import type { InsReportSysDepartVo, InsReportAccountInfoVo } from '@/api/common/index.d'
import { debounce } from 'lodash-es'

interface DeptAccountCascaderOption {
  /** 节点值：部门节点使用部门id，人员节点使用 userId */
  value: string
  /** 节点显示名称 */
  label: string
  /** 子节点 */
  children?: DeptAccountCascaderOption[]
  /** 当前节点是否禁用 */
  disabled?: boolean
}

/**
 * 单点事件中的通用逻辑
 */
export const useSingleEventStore = defineStore(
  'singleEvent',
  () => {
    const userStore = useUserStore()
    const _conditionsMap = ref<any>({})
    const carSeriesOptions = ref<any[]>([])
    const departTree = ref<InsReportSysDepartVo[]>([])
    const departAccountTree = ref<InsReportSysDepartVo[]>([])

    // 手动恢复缓存数据
    const cachedData = sessionStorage.getItem('singleEventStore')
    if (cachedData) {
      try {
        const parsed = JSON.parse(cachedData)
        if (parsed.departAccountTree && Array.isArray(parsed.departAccountTree)) {
          departAccountTree.value = parsed.departAccountTree
        }
        if (parsed.departTree && Array.isArray(parsed.departTree)) {
          departTree.value = parsed.departTree
        }
        if (parsed._conditionsMap && typeof parsed._conditionsMap === 'object') {
          _conditionsMap.value = parsed._conditionsMap
        }
      } catch (e) {
        console.error('Failed to restore cache:', e)
      }
    }

    const conditionsLoadCount = ref(0)
    const isConditionsLoading = ref(false)

    // 获取查询条件下拉选项
    const getConditions = async () => {
      if (
        Object.keys(_conditionsMap.value).length > 0 ||
        conditionsLoadCount.value >= 3 ||
        isConditionsLoading.value
      ) {
        return _conditionsMap.value
      }
      isConditionsLoading.value = true
      conditionsLoadCount.value++
      try {
        const res = await conditions()
        if (res.result && Object.keys(res.result).length > 0) {
          Object.assign(_conditionsMap.value, res.result)
        }
        return _conditionsMap.value
      } catch (error) {
        console.error('getConditions error:', error)
        return {}
      } finally {
        isConditionsLoading.value = false
      }
    }

    // 获取部门树
    const getDepartTree = async () => {
      if (departTree.value.length > 0) return departTree.value
      try {
        const res = await findDepartTree()
        if (res.result && res.result.length > 0) {
          departTree.value = res.result
        }
        return departTree.value
      } catch (error) {
        console.error('getDepartTree error:', error)
        return []
      }
    }

    // 获取部门用户树
    const getDepartAccountTree = async () => {
      if (departAccountTree.value.length > 0) return departAccountTree.value
      try {
        const res = await findDepartAccountTree()
        if (res.result && res.result.length > 0) {
          departAccountTree.value = res.result
        }
        return departAccountTree.value
      } catch (error) {
        console.error('getDepartAccountTree error:', error)
        return []
      }
    }

    // 自动加载数据
    watchEffect(() => {
      if (
        Object.keys(_conditionsMap.value).length === 0 &&
        conditionsLoadCount.value < 3 &&
        !isConditionsLoading.value
      ) {
        getConditions()
      }
      // departTree 和 departAccountTree 都在主页面预加载，不在 watchEffect 中自动加载
    })

    // 代理conditionsMap
    const conditionsMap = computed(() => {
      return _conditionsMap.value
    })

    // 事件状态
    const task_event_staus = computed(() => {
      return userStore.getDictItems('task_event_staus')
    })

    // 事件优先级
    const closed_rule_priority = computed(() => {
      return userStore.getDictItems('closed_rule_priority')
    })

    // 是否处理
    const task_event_is_handled = computed(() => {
      return userStore.getDictItems('task_event_is_handled')
    })
    // 处理原因--是
    const task_event_approve_process_mode = computed(() => {
      return userStore.getDictItems('task_event_approve_process_mode')
    })

    // 私信次数
    const task_event_private_mst_count = computed(() => {
      return userStore.getDictItems('task_event_private_mst_count')
    })

    // 私信进度
    const task_event_private_mst_staus = computed(() => {
      return userStore.getDictItems('task_event_private_mst_staus')
    })

    // 回评进度
    const task_event_review_staus = computed(() => {
      return userStore.getDictItems('task_event_review_staus')
    })

    // 事件等级
    const closed_rule_level = computed(() => {
      return userStore.getDictItems('closed_rule_level')
    })

    // 预警事件审核-关闭原因
    const task_event_approve_close_reason = computed(() => {
      return userStore.getDictItems('task_event_approve_close_reason')
      // return _conditionsMap.value.event_attribute || []
    })
    // 预警事件确认-驳回原因
    const task_event_reject_reason = computed(() => {
      return userStore.getDictItems('task_event_reject_reason')
    })
    // 预警事件关闭原因  /  处理原因--否
    const task_event_close_reason = computed(() => {
      // return userStore.getDictItems('task_event_close_reason')
      return _conditionsMap.value.event_attribute || []
    })

    // 意图
    const voc_intention = computed(() => {
      return userStore.getDictItems('voc_intention')
    })

    // 主题分类
    const closedLoopCategory = computed(() => {
      return _conditionsMap.value.closedLoopCategory || []
    })
    //  数据来源（渠道）
    const dataChannel = computed(() => {
      return _conditionsMap.value.dataChannel || []
    })

    // 主责部门
    const mainRespOrgs = computed(() => {
      return _conditionsMap.value.mainRespOrgs || []
    })

    // 敏感类型
    const sensitiveTypes = computed(() => {
      return _conditionsMap.value.sensitiveTypes || []
    })

    // 事件清晰度
    const eventClears = computed(() => {
      return _conditionsMap.value.eventClears || []
    })
    // 事件有效性
    const task_event_validity = computed(() => {
      return userStore.getDictItems('task_event_validity')
    })

    // 事件属性
    const event_attribute = computed(() => {
      return _conditionsMap.value.event_attribute || []
    })

    // 标准观点
    const topicList = computed(() => {
      return _conditionsMap.value.topicList || []
    })

    // 主题分类
    const tagTreeList = computed(() => {
      return _conditionsMap.value.tagTreeList || []
    })

    // 根据tagCode获取子级数据
    const getTagChildren = (tagCode: string | undefined) => {
      if (!tagCode) return []
      const findChildren = (list: any[]): any[] => {
        for (const item of list) {
          if (item.tagCode === tagCode) {
            return item.child || []
          }
          if (item.child && item.child.length > 0) {
            const result = findChildren(item.child)
            if (result.length > 0) return result
          }
        }
        return []
      }
      return findChildren(tagTreeList.value)
    }

    /**
     * @description: 根据channelId从渠道数据中递归查找对应项
     * @param {string} channelId 渠道ID
     * @param {any[]} dataSource 数据源，默认使用dataChannel
     * @return {any | null} 找到的渠道数据项或null
     */
    const getChannelById = (channelId: string, dataSource?: any[]): any | null => {
      if (!channelId) return null
      const findInTree = (list: any[]): any | null => {
        for (const item of list) {
          if (item.code === channelId) return item
          if (item.child?.length) {
            const found = findInTree(item.child)
            if (found) return found
          }
        }
        return null
      }
      return findInTree(dataSource || dataChannel.value)
    }

    // 品牌
    const brandOptions = computed(() => {
      return userStore.getBrandService
    })

    // 根据品牌获取车系选项
    const getCarSeriesOptionsByBrand = (brand: string) => {
      return brandOptions.value?.find(item => item.key === brand)?.children || []
    }

    // 品牌change
    const brandChange = (val: string) => {
      carSeriesOptions.value = getCarSeriesOptionsByBrand(val)
    }

    // 初始化时调用getConditions
    getConditions()

    /**
     * @description: 查询单点事件原声信息
     * @param {any} data
     * @return {*}
     */
    const fetchGetDetailBase = async (data: any): Promise<SingleEventDetailBaseVo> => {
      try {
        const res = await getDetailBase(data)
        return res.result
      } catch (error) {
        console.error('getDetailBase error:', error)
        return {}
      }
    }

    /**
     * @description: 更新单点事件
     * @param {any} data
     * @return {*}
     */
    const fetchUpdateOriginalSoundDetail = async (data: any): Promise<any> => {
      try {
        const res = await updateOriginalSoundDetail(data)
        return res
      } catch (error) {
        console.error('getDetailBase error:', error)
      }
    }

    /**
     * @description: 根据部门id列表获取用户列表
     * @param {string[]} deptIds 部门id数组
     * @return {Promise<InsReportAccountInfoVo[]>}
     */
    const fetchAccountByDeptIds = async (deptIds: string[]): Promise<InsReportAccountInfoVo[]> => {
      try {
        const res = await findAccountByDeptId({ deptId: deptIds })
        return res.result || []
      } catch (error) {
        console.error('fetchAccountByDeptIds error:', error)
        return []
      }
    }

    /**
     * @description: 根据部门id列表获取部门账号树
     * @param {string[]} deptIds 部门id数组
     * @return {Promise<InsReportSysDepartVo[]>}
     */
    const fetchDepartAccountTreeByDeptIds = async (
      deptIds: string[]
    ): Promise<InsReportSysDepartVo[]> => {
      if (!deptIds?.length) return []
      try {
        const res = await findDepartAccountTreeByDeptId({ deptId: deptIds })
        return res.result || []
      } catch (error) {
        console.error('fetchDepartAccountTreeByDeptIds error:', error)
        return []
      }
    }

    /**
     * @description: 格式化人员级联节点文案
     * @param {InsReportAccountInfoVo} account 人员信息
     * @return {string}
     */
    const formatAccountCascaderLabel = (account: InsReportAccountInfoVo) => {
      return `${account.userName || ''}${account.employeeId ? `-${account.employeeId}` : ''}`
    }

    /**
     * @description: 将部门账号树拍平成用户列表，便于按 userId 回查人员模型
     * @param {InsReportSysDepartVo[]} tree 部门账号树
     * @return {InsReportAccountInfoVo[]}
     */
    const flattenDepartAccountUsers = (tree: InsReportSysDepartVo[]): InsReportAccountInfoVo[] => {
      const userMap = new Map<string, InsReportAccountInfoVo>()

      /**
       * 递归遍历部门树，收集所有账号节点。
       * 1. 当前部门账号先入表，保证直接部门账号可被快速命中。
       * 2. 继续向下遍历子部门，兼容多级组织下的处理人员选择。
       */
      const walk = (list: InsReportSysDepartVo[] = []) => {
        list.forEach(item => {
          item.account?.forEach(account => {
            if (account.userId) {
              userMap.set(account.userId, account)
            }
          })

          if (item.child?.length) {
            walk(item.child)
          }
        })
      }

      walk(tree || [])
      return Array.from(userMap.values())
    }

    /**
     * @description: 将部门账号树转换为处理人员 el-cascader 可用的选项结构
     * @param {InsReportSysDepartVo[]} tree 部门账号树
     * @return {DeptAccountCascaderOption[]}
     */
    const buildDeptAccountOptionTree = (
      tree: InsReportSysDepartVo[],
      options: { disableDept: boolean }
    ): DeptAccountCascaderOption[] => {
      const walk = (list: InsReportSysDepartVo[] = []): DeptAccountCascaderOption[] => {
        return list
          .filter(item => !!item.id)
          .map(item => {
            const childDeptOptions = walk(item.child || [])
            const accountOptions = (item.account || [])
              .filter(account => !!account.userId)
              .map(account => ({
                value: account.userId!,
                label: formatAccountCascaderLabel(account)
              }))

            const children = [...childDeptOptions, ...accountOptions]

            return {
              value: item.id!,
              label: item.name || '',
              disabled: options.disableDept || children.length === 0,
              ...(children.length ? { children } : {})
            }
          })
      }

      return walk(tree || [])
    }

    /**
     * @description: 构建单选处理人员级联树
     * @param {InsReportSysDepartVo[]} tree 部门账号树
     * @return {DeptAccountCascaderOption[]}
     */
    const buildDepartAccountCascaderOptions = (
      tree: InsReportSysDepartVo[]
    ): DeptAccountCascaderOption[] => {
      /**
       * 单选场景保持现有行为：部门节点只负责展开，叶子人员节点直接输出 userId。
       */
      return buildDeptAccountOptionTree(tree, {
        disableDept: true
      })
    }

    /**
     * @description: 构建多选处理人员级联树
     * @param {InsReportSysDepartVo[]} tree 部门账号树
     * @return {DeptAccountCascaderOption[]}
     */
    const buildDepartAccountMultiCascaderOptions = (
      tree: InsReportSysDepartVo[]
    ): DeptAccountCascaderOption[] => {
      /**
       * 多选场景允许勾选部门节点，交由级联组件联动选中子级。
       */
      return buildDeptAccountOptionTree(tree, {
        disableDept: false
      })
    }

    /**
     * @description: 根据id从部门树中查找部门信息
     * @param {string} id 部门id
     * @return {{ code: string; name: string; id: string } | null}
     */
    const getDepartInfoById = (id: string): InsReportSysDepartVo | null => {
      const findInTree = (list: InsReportSysDepartVo[]): InsReportSysDepartVo | null => {
        for (const item of list) {
          if (item.id === id) return item
          if (item.child?.length) {
            const found = findInTree(item.child)
            if (found) return found
          }
        }
        return null
      }
      const dept = findInTree(departTree.value)
      return dept
    }

    /**
     * @description: 根据id从部门用户树中查找部门或用户信息
     * @param {string} id 部门或用户id
     * @return {{ code: string; name: string; id: string } | null}
     */
    const getDepartAccountInfoById = (id: string): InsReportSysDepartVo | null => {
      const findInTree = (list: InsReportSysDepartVo[]): any => {
        for (const item of list) {
          if (item.id === id) return item
          if (item.child?.length) {
            const found = findInTree(item.child)
            if (found) return found
          }
        }
        return null
      }
      const node = findInTree(departAccountTree.value)
      return node
    }

    // 根据部门获取到的人员列表
    const departUserOptions = ref<InsReportAccountInfoVo[]>([])

    /**
     * @description: 根据id从用户列表中查找用户信息
     * @param {string} id 用户id
     * @param {InsReportAccountInfoVo[]} userList 用户列表
     * @return {{ code: string; name: string; id: string } | null}
     */
    const getUserInfoById = (
      id: string,
      userOptions?: InsReportAccountInfoVo[]
    ): InsReportAccountInfoVo | undefined => {
      if (userOptions) {
        return userOptions.find(item => item.userId === id)
      }
      const user = departUserOptions.value.find(item => item.userId === id)
      return user
    }

    /**
     * @description: 部门级联Change事件， 根据部门去获取人员
     * @param {*} debounce
     * @return {*}
     */
    const departChange = debounce(async (val: any) => {
      if (!val) {
        departUserOptions.value = []
        return
      }
      departUserOptions.value = await fetchAccountByDeptIds(val ? [val] : [])
    }, 300)

    /**
     * @description: 根据用户id，获取用户的部门信息，名称，id等信息
     * @param {string} userId
     * @return {*}
     */
    const getUserModelByUserId = (userId: string, options: any[]) => {
      if (!userId) {
        return {
          orgId: undefined,
          orgNo: undefined,
          orgName: undefined,
          allFlag: false,
          userId: undefined,
          userEmpNo: undefined,
          userName: undefined
        }
      }
      const _userInfo = getUserInfoById(userId, options)

      return {
        orgId: _userInfo?.deptId,
        orgNo: _userInfo?.deptId,
        orgName: _userInfo?.deptName,
        allFlag: false,
        userId: userId,
        userEmpNo: _userInfo?.employeeId,
        userName: _userInfo?.userName
      }
    }

    /**
     * @description: 根据部门id 获取部门信息
     * @param {any} mainRespOrgId
     * @return {*}
     */
    const deptInfoByOrgId = (mainRespOrgId: any) => {
      if (!mainRespOrgId) {
        return {
          mainRespOrgId: undefined,
          mainRespOrgNo: undefined,
          mainRespOrgName: undefined
        }
      }
      const _deptInfo = getDepartInfoById(mainRespOrgId!)
      return {
        mainRespOrgId: mainRespOrgId,
        mainRespOrgNo: _deptInfo?.code,
        mainRespOrgName: _deptInfo?.name
      }
    }

    /**
     * @description: 根据mainRespUserId 获取用户信息
     * @param {any} mainRespUserId
     * @return {*}
     */
    const userInfoByUserId = (mainRespUserId: any) => {
      if (!mainRespUserId) {
        return {
          mainRespUserId: undefined,
          mainRespUserEmpNo: undefined,
          mainRespUserName: undefined
        }
      }
      const _userInfo = getUserInfoById(mainRespUserId)
      return {
        mainRespUserId: mainRespUserId,
        mainRespUserEmpNo: _userInfo?.employeeId,
        mainRespUserName: _userInfo?.userName
      }
    }

    return {
      conditionsMap,
      carSeriesOptions,
      getConditions,
      task_event_staus,
      closed_rule_priority,
      task_event_is_handled,
      task_event_private_mst_count,
      task_event_private_mst_staus,
      task_event_review_staus,
      closed_rule_level,
      closedLoopCategory,
      mainRespOrgs,
      sensitiveTypes,
      eventClears,
      brandOptions,
      getCarSeriesOptionsByBrand,
      brandChange,
      task_event_approve_close_reason,
      task_event_reject_reason,
      task_event_close_reason,
      fetchGetDetailBase,
      fetchUpdateOriginalSoundDetail,
      dataChannel,
      voc_intention,
      topicList,
      tagTreeList,
      getTagChildren,
      getChannelById,
      departTree,
      departAccountTree,
      getDepartTree,
      getDepartAccountTree,
      fetchAccountByDeptIds,
      fetchDepartAccountTreeByDeptIds,
      flattenDepartAccountUsers,
      buildDepartAccountCascaderOptions,
      buildDepartAccountMultiCascaderOptions,
      getDepartInfoById,
      getDepartAccountInfoById,
      getUserInfoById,
      departChange,
      departUserOptions,
      task_event_approve_process_mode,
      getUserModelByUserId,
      deptInfoByOrgId,
      userInfoByUserId,
      task_event_validity,
      event_attribute
    }
  },
  {
    persist: {
      key: 'singleEventStore',
      storage: sessionStorage,
      pick: ['_conditionsMap', 'departTree', 'departAccountTree']
    }
  }
)

export default useSingleEventStore
