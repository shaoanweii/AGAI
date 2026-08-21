import { computed, ref, shallowRef } from 'vue'
import { debounce } from 'lodash-es'
import { getBatchEventConditions, getBatchEventRuleCategoryTree } from '@/api/batchEvent'
import { conditions as getSingleEventReferenceConditions } from '@/api/singlePointEvent'
import {
  findAccountByDeptId,
  findDepartAccountTree,
  findDepartAccountTreeByDeptId,
  findDepartTree
} from '@/api/common'
import useUserStore from '@/store/modules/user'
import type {
  BatchEventConditionsVo,
  BatchEventDataSourceType,
  BatchEventOptionVo,
  BatchEventRuleCategoryVo
} from '@/api/batchEvent/types'
import type { InsReportAccountInfoVo, InsReportSysDepartVo } from '@/api/common/index.d'

interface DeptAccountCascaderOption extends Record<string, unknown> {
  /** 节点值：部门节点使用部门 id，人员节点使用 userId */
  value: string
  /** 节点显示名称 */
  label: string
  /** 子节点 */
  children?: DeptAccountCascaderOption[]
  /** 当前节点是否禁用 */
  disabled?: boolean
}

interface FilterDeptCascaderOption extends Record<string, unknown> {
  /** 部门 id */
  id: string
  /** 部门名称 */
  name: string
  /** 下级部门 */
  child?: FilterDeptCascaderOption[]
}

interface FilterHandlerCascaderOption extends Record<string, unknown> {
  /** 节点 id：部门节点使用部门 id，人员节点使用 userId */
  id: string
  /** 节点显示名称 */
  name: string
  /** 子节点 */
  child?: FilterHandlerCascaderOption[]
  /** 当前节点是否禁用 */
  disabled?: boolean
}

interface PersonSelectOption {
  label: string
  value: string
}

const batchConditionsMap = shallowRef<BatchEventConditionsVo>({})
const referenceConditionsMap = shallowRef<Record<string, any>>({})
const departTree = shallowRef<InsReportSysDepartVo[]>([])
const departAccountTree = shallowRef<InsReportSysDepartVo[]>([])
const departUserOptions = ref<InsReportAccountInfoVo[]>([])
const personOptions = shallowRef<PersonSelectOption[]>([])
const personUserMap = shallowRef<Map<string, InsReportAccountInfoVo>>(new Map())
const departAccountCascaderOptions = shallowRef<DeptAccountCascaderOption[]>([])
const departAccountMultiCascaderOptions = shallowRef<DeptAccountCascaderOption[]>([])
const filterDepartmentCascaderOptions = shallowRef<FilterDeptCascaderOption[]>([])
const filterHandlerCascaderOptions = shallowRef<FilterHandlerCascaderOption[]>([])
const batchRuleCategoryTree = shallowRef<BatchEventRuleCategoryVo[]>([])

let conditionsPromise: Promise<BatchEventConditionsVo> | null = null
let departTreePromise: Promise<InsReportSysDepartVo[]> | null = null
let departAccountTreePromise: Promise<InsReportSysDepartVo[]> | null = null
let batchRuleCategoryTreePromise: Promise<BatchEventRuleCategoryVo[]> | null = null
let personOptionsPromise: Promise<PersonSelectOption[]> | null = null
let cascaderOptionsPromise: Promise<DeptAccountCascaderOption[]> | null = null
let multiCascaderOptionsPromise: Promise<DeptAccountCascaderOption[]> | null = null
let filterDepartmentOptionsPromise: Promise<FilterDeptCascaderOption[]> | null = null
let filterHandlerOptionsPromise: Promise<FilterHandlerCascaderOption[]> | null = null
let conditionsLoaded = false
const batchConditionsPromiseMap = new Map<string, Promise<BatchEventConditionsVo>>()

/**
 * 格式化人员级联节点文案。
 * @param account 人员信息
 * @returns 人员展示文案
 */
const formatAccountLabel = (account: InsReportAccountInfoVo) => {
  return `${account.userName || ''}${account.employeeId ? `-${account.employeeId}` : ''}`
}

/**
 * 查找部门人员树中的用户，不提前拍平整棵树。
 * @param tree 部门人员树
 * @param predicate 匹配函数
 * @returns 命中的人员
 */
const findAccountInTree = (
  tree: InsReportSysDepartVo[] = [],
  predicate: (account: InsReportAccountInfoVo) => boolean
): InsReportAccountInfoVo | undefined => {
  for (const dept of tree) {
    const matchedAccount = dept.account?.find(predicate)
    if (matchedAccount) return matchedAccount

    if (dept.child?.length) {
      const matchedChildAccount = findAccountInTree(dept.child, predicate)
      if (matchedChildAccount) return matchedChildAccount
    }
  }

  return undefined
}

/**
 * 按 userId 查找人员及其直属上级部门路径。
 * @param tree 部门人员树
 * @param userId 用户 id
 * @param path 当前递归部门路径
 * @returns 命中的人员和部门路径
 */
const findAccountPathByUserId = (
  tree: InsReportSysDepartVo[] = [],
  userId?: string,
  path: InsReportSysDepartVo[] = []
): { account: InsReportAccountInfoVo; path: InsReportSysDepartVo[] } | null => {
  if (!userId) return null

  for (const dept of tree) {
    const currentPath = [...path, dept]
    const matchedAccount = dept.account?.find(account => account.userId === userId)

    if (matchedAccount) {
      return {
        account: matchedAccount,
        path: currentPath
      }
    }

    if (dept.child?.length) {
      const matchedChildAccount = findAccountPathByUserId(dept.child, userId, currentPath)
      if (matchedChildAccount) return matchedChildAccount
    }
  }

  return null
}

/**
 * 按需构建人员下拉选项和 userId 索引。
 * @param tree 部门人员树
 * @returns 人员下拉选项
 */
const buildPersonOptions = (tree: InsReportSysDepartVo[] = []) => {
  const optionList: PersonSelectOption[] = []
  const userMap = new Map<string, InsReportAccountInfoVo>()

  const walk = (list: InsReportSysDepartVo[] = []) => {
    list.forEach(dept => {
      dept.account?.forEach(account => {
        if (!account.userId || userMap.has(account.userId)) return

        userMap.set(account.userId, account)
        optionList.push({
          label: `${account.deptName ? `${account.deptName}/` : ''}${formatAccountLabel(account)}`,
          value: account.userId
        })
      })

      if (dept.child?.length) {
        walk(dept.child)
      }
    })
  }

  walk(tree)
  personUserMap.value = userMap
  return optionList.filter(item => item.label && item.value)
}

/**
 * 将部门账号树转换成级联组件选项，仅在相关弹窗打开时执行。
 * @param tree 部门账号树
 * @param options 转换选项
 * @returns 级联选项
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
            label: formatAccountLabel(account)
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
 * 将部门树裁剪为筛选可用的二三级部门。
 * @param tree 原始部门树
 * @returns 二三级部门级联选项
 */
const buildFilterDepartmentOptionTree = (tree: InsReportSysDepartVo[]) => {
  const buildDepartment = (
    list: InsReportSysDepartVo[] = [],
    depth = 2
  ): FilterDeptCascaderOption[] => {
    return list
      .filter(item => !!item.id)
      .map(item => ({
        id: item.id!,
        name: item.name || '',
        ...(depth < 3 && item.child?.length
          ? { child: buildDepartment(item.child, depth + 1) }
          : {})
      }))
  }

  return (tree || []).flatMap(item => buildDepartment(item.child || [], 2))
}

/**
 * 将部门人员树裁剪为“二三级部门 + 员工”级联筛选选项。
 * @param tree 原始部门人员树
 * @returns 处理人员筛选级联选项
 */
const buildFilterHandlerOptionTree = (tree: InsReportSysDepartVo[]) => {
  const userMap = new Map<string, InsReportAccountInfoVo>()

  const buildDepartment = (
    list: InsReportSysDepartVo[] = [],
    depth = 2
  ): FilterHandlerCascaderOption[] => {
    return list
      .filter(item => !!item.id)
      .map(item => {
        const childDeptOptions = depth < 3 ? buildDepartment(item.child || [], depth + 1) : []
        const accountOptions = (item.account || [])
          .filter(account => !!account.userId)
          .map(account => {
            userMap.set(account.userId!, account)
            return {
              id: account.userId!,
              name: formatAccountLabel(account)
            }
          })

        const children = [...childDeptOptions, ...accountOptions]

        return {
          id: item.id!,
          name: item.name || '',
          disabled: true,
          ...(children.length ? { child: children } : {})
        }
      })
      .filter(item => item.child?.length)
  }

  const options = (tree || []).flatMap(item => buildDepartment(item.child || [], 2))
  personUserMap.value = new Map([...personUserMap.value, ...userMap])
  return options
}

/**
 * 查询批量事件页面筛选依赖的单点参考条件。
 * 批量 conditions 接口已要求事件 ID，不再在页面初始化时无参调用。
 * @returns 批量事件页面级条件缓存
 */
const loadConditions = async () => {
  if (conditionsLoaded) {
    return batchConditionsMap.value
  }

  if (conditionsPromise) {
    return conditionsPromise
  }

  conditionsPromise = getSingleEventReferenceConditions()
    .then(res => {
      if (res.result && Object.keys(res.result).length > 0) {
        referenceConditionsMap.value = { ...res.result }
      }
      conditionsLoaded = true
      return batchConditionsMap.value
    })
    .catch(error => {
      console.error('loadBatchEventReferenceConditions error:', error)
      return batchConditionsMap.value
    })
    .finally(() => {
      conditionsPromise = null
    })

  return conditionsPromise
}

/**
 * 将单个、逗号分隔字符串或数组 ID 规范化为接口数组字段。
 * @param ids 事件 ID 或原声 ID
 * @returns 去重后的 ID 数组
 */
const normalizeConditionIds = (ids: string | string[]) => {
  const rawIds = Array.isArray(ids) ? ids : String(ids || '').split(',')

  return [...new Set(rawIds.map(id => String(id || '').trim()).filter(Boolean))]
}

/**
 * 查询批量事件条件并按来源类型隔离缓存。
 * @param ids 事件 ID 或原声 ID
 * @param source 条件来源；batch-event 使用 ids，sound 使用 soundIds
 * @returns 当前范围可用的批量事件条件
 */
const loadBatchEventConditions = async (
  ids: string | string[],
  source: 'batch-event' | 'sound' = 'batch-event',
  dataSourceType?: BatchEventDataSourceType
) => {
  const normalizedIds = normalizeConditionIds(ids)
  if (normalizedIds.length === 0) {
    return {}
  }

  const cacheKey = `${source}:${dataSourceType || 'DEFAULT'}:${normalizedIds.join(',')}`
  const cachedPromise = batchConditionsPromiseMap.get(cacheKey)
  if (cachedPromise) {
    return cachedPromise
  }

  const requestPromise = getBatchEventConditions(
    source === 'sound'
      ? { soundIds: normalizedIds, dataSourceType }
      : { ids: normalizedIds, dataSourceType }
  )
    .then(res => res.result || {})
    .catch(error => {
      console.error('loadBatchEventConditions error:', error)
      return {}
    })
    .finally(() => {
      batchConditionsPromiseMap.delete(cacheKey)
    })

  batchConditionsPromiseMap.set(cacheKey, requestPromise)
  return requestPromise
}

/**
 * 按事件 ID 查询批量事件条件。
 * @param id 单个事件 ID，事件 ID 数组，或英文逗号拼接的多个 ID
 * @returns 当前事件范围可用的批量事件条件
 */
const loadBatchEventConditionsById = async (id: string | string[]) => {
  return loadBatchEventConditions(id, 'batch-event')
}

/**
 * 按原声 ID 查询批量事件下发条件。
 * @param soundIds 单个原声 ID，原声 ID 数组，或英文逗号拼接的多个 ID
 * @param dataSourceType 数据源类型，原始数据用于驱动后端返回全量聚焦观点
 * @returns 当前原声范围可用的批量事件条件
 */
const loadBatchEventConditionsBySoundId = async (
  soundIds: string | string[],
  dataSourceType?: BatchEventDataSourceType
) => {
  return loadBatchEventConditions(soundIds, 'sound', dataSourceType)
}

/**
 * 查询批量事件规则主题分类树。
 * @returns 主题分类树
 */
const loadBatchRuleCategoryTree = async () => {
  if (batchRuleCategoryTree.value.length > 0) return batchRuleCategoryTree.value
  if (batchRuleCategoryTreePromise) return batchRuleCategoryTreePromise

  batchRuleCategoryTreePromise = getBatchEventRuleCategoryTree()
    .then(res => {
      batchRuleCategoryTree.value = Array.isArray(res.result) ? res.result : []
      return batchRuleCategoryTree.value
    })
    .catch(error => {
      console.error('loadBatchRuleCategoryTree error:', error)
      return []
    })
    .finally(() => {
      batchRuleCategoryTreePromise = null
    })

  return batchRuleCategoryTreePromise
}

/**
 * 获取部门树。
 * @returns 部门树
 */
const loadDepartTree = async () => {
  if (departTree.value.length > 0) return departTree.value
  if (departTreePromise) return departTreePromise

  departTreePromise = findDepartTree()
    .then(res => {
      departTree.value = res.result || []
      return departTree.value
    })
    .catch(error => {
      console.error('loadBatchEventDepartTree error:', error)
      return []
    })
    .finally(() => {
      departTreePromise = null
    })

  return departTreePromise
}

/**
 * 获取部门人员树，只保留后端原始结构，不在加载阶段派生下拉数据。
 * @returns 部门人员树
 */
const loadDepartAccountTree = async () => {
  if (departAccountTree.value.length > 0) return departAccountTree.value
  if (departAccountTreePromise) return departAccountTreePromise

  departAccountTreePromise = findDepartAccountTree()
    .then(res => {
      departAccountTree.value = res.result || []
      return departAccountTree.value
    })
    .catch(error => {
      console.error('loadBatchEventDepartAccountTree error:', error)
      return []
    })
    .finally(() => {
      departAccountTreePromise = null
    })

  return departAccountTreePromise
}

/**
 * 人员下拉需要扁平数组，按用户打开下拉时才构建一次。
 * @returns 人员下拉选项
 */
const ensurePersonOptions = async () => {
  if (personOptions.value.length > 0) return personOptions.value
  if (personOptionsPromise) return personOptionsPromise

  personOptionsPromise = loadDepartAccountTree()
    .then(tree => {
      personOptions.value = buildPersonOptions(tree)
      return personOptions.value
    })
    .finally(() => {
      personOptionsPromise = null
    })

  return personOptionsPromise
}

/**
 * 业务责任人级联树只在弹窗打开时构建。
 * @returns 只允许选择人员的级联树
 */
const ensureDepartAccountCascaderOptions = async () => {
  if (departAccountCascaderOptions.value.length > 0) return departAccountCascaderOptions.value
  if (cascaderOptionsPromise) return cascaderOptionsPromise

  cascaderOptionsPromise = loadDepartAccountTree()
    .then(tree => {
      departAccountCascaderOptions.value = buildDeptAccountOptionTree(tree, { disableDept: true })
      return departAccountCascaderOptions.value
    })
    .finally(() => {
      cascaderOptionsPromise = null
    })

  return cascaderOptionsPromise
}

/**
 * 多选抄送级联树按需构建，部门节点可选。
 * @returns 可选择部门和人员的级联树
 */
const ensureDepartAccountMultiCascaderOptions = async () => {
  if (departAccountMultiCascaderOptions.value.length > 0) {
    return departAccountMultiCascaderOptions.value
  }
  if (multiCascaderOptionsPromise) return multiCascaderOptionsPromise

  multiCascaderOptionsPromise = loadDepartAccountTree()
    .then(tree => {
      departAccountMultiCascaderOptions.value = buildDeptAccountOptionTree(tree, {
        disableDept: false
      })
      return departAccountMultiCascaderOptions.value
    })
    .finally(() => {
      multiCascaderOptionsPromise = null
    })

  return multiCascaderOptionsPromise
}

/**
 * 构建批量筛选的二三级部门级联选项。
 * @returns 部门级联选项
 */
const ensureFilterDepartmentCascaderOptions = async () => {
  if (filterDepartmentCascaderOptions.value.length > 0) {
    return filterDepartmentCascaderOptions.value
  }
  if (filterDepartmentOptionsPromise) return filterDepartmentOptionsPromise

  filterDepartmentOptionsPromise = loadDepartTree()
    .then(tree => {
      filterDepartmentCascaderOptions.value = buildFilterDepartmentOptionTree(tree)
      return filterDepartmentCascaderOptions.value
    })
    .finally(() => {
      filterDepartmentOptionsPromise = null
    })

  return filterDepartmentOptionsPromise
}

/**
 * 构建批量筛选的二三级部门 + 员工级联选项。
 * @returns 处理人员级联选项
 */
const ensureFilterHandlerCascaderOptions = async () => {
  if (filterHandlerCascaderOptions.value.length > 0) {
    return filterHandlerCascaderOptions.value
  }
  if (filterHandlerOptionsPromise) return filterHandlerOptionsPromise

  filterHandlerOptionsPromise = loadDepartAccountTree()
    .then(tree => {
      filterHandlerCascaderOptions.value = buildFilterHandlerOptionTree(tree)
      return filterHandlerCascaderOptions.value
    })
    .finally(() => {
      filterHandlerOptionsPromise = null
    })

  return filterHandlerOptionsPromise
}

/**
 * 页面初始化时预加载筛选部门和处理人员数据源。
 * @returns 部门与处理人员筛选选项加载结果
 */
const preloadDepartmentAndHandlerOptions = async () => {
  return Promise.all([
    ensureFilterDepartmentCascaderOptions(),
    ensureFilterHandlerCascaderOptions()
  ])
}

/**
 * 根据部门 id 列表获取用户列表。
 * @param deptIds 部门 id 数组
 * @returns 用户列表
 */
const fetchAccountByDeptIds = async (deptIds: string[]): Promise<InsReportAccountInfoVo[]> => {
  try {
    const res = await findAccountByDeptId({ deptId: deptIds })
    return res.result || []
  } catch (error) {
    console.error('fetchBatchEventAccountByDeptIds error:', error)
    return []
  }
}

/**
 * 根据部门 id 列表获取部门账号树。
 * @param deptIds 部门 id 数组
 * @returns 部门账号树
 */
const fetchDepartAccountTreeByDeptIds = async (
  deptIds: string[]
): Promise<InsReportSysDepartVo[]> => {
  if (!deptIds?.length) return []
  try {
    const res = await findDepartAccountTreeByDeptId({ deptId: deptIds })
    return res.result || []
  } catch (error) {
    console.error('fetchBatchEventDepartAccountTreeByDeptIds error:', error)
    return []
  }
}

/**
 * 根据部门 id 查找部门信息。
 * @param id 部门 id
 * @returns 部门信息
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
  return findInTree(departTree.value)
}

/**
 * 根据用户 id 查找用户信息。
 * @param id 用户 id
 * @param userOptions 可选用户列表
 * @returns 用户信息
 */
const getUserInfoById = (
  id: string,
  userOptions?: InsReportAccountInfoVo[]
): InsReportAccountInfoVo | undefined => {
  if (userOptions) {
    return userOptions.find(item => item.userId === id)
  }

  return (
    departUserOptions.value.find(item => item.userId === id) ||
    personUserMap.value.get(id) ||
    findAccountInTree(departAccountTree.value, account => account.userId === id)
  )
}

/**
 * 过滤处理人员筛选值，仅保留真实人员 userId。
 * @param ids 级联组件当前值
 * @returns 可提交给批量事件列表接口的人员 ID
 */
const filterValidHandlerUserIds = (ids: string[] = []) => {
  return ids.filter(id => !!getUserInfoById(id))
}

/**
 * 根据员工编号或人员姓名/部门从原始部门人员树中查找用户。
 * @param item 后端抄送人展示项
 * @returns 命中的用户
 */
const findUserByCcDisplay = (item: {
  employeeId?: string
  userName?: string
  tertiaryDepartment?: string
  secondaryDepartment?: string
}) => {
  return findAccountInTree(departAccountTree.value, user => {
    return (
      user.employeeId === item.employeeId ||
      (user.userName === item.userName &&
        (user.deptName === item.tertiaryDepartment ||
          user.secondDeptName === item.secondaryDepartment ||
          user.thirdDeptName === item.secondaryDepartment))
    )
  })
}

const departChange = debounce(async (val: any) => {
  if (!val) {
    departUserOptions.value = []
    return
  }
  departUserOptions.value = await fetchAccountByDeptIds(val ? [val] : [])
}, 300)

/**
 * 根据用户 id 获取接口需要的人员模型。
 * @param userId 用户 id
 * @param options 用户备选列表
 * @returns 人员模型
 */
const getUserModelByUserId = (userId: string, options: InsReportAccountInfoVo[] = []) => {
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
  const userInfo = getUserInfoById(userId, options)

  return {
    orgId: userInfo?.deptId,
    orgNo: userInfo?.deptId,
    orgName: userInfo?.deptName,
    deptName: userInfo?.deptName,
    allFlag: false,
    userId,
    userEmpNo: userInfo?.employeeId,
    userName: userInfo?.userName
  }
}

/**
 * 根据用户 id 获取主责人接口字段。
 * @param mainRespUserId 用户 id
 * @param options 用户备选列表
 * @returns 主责人字段
 */
const userInfoByUserId = (mainRespUserId: string, options: InsReportAccountInfoVo[] = []) => {
  if (!mainRespUserId) {
    return {
      mainRespUserId: undefined,
      mainRespUserEmpNo: undefined,
      mainRespUserName: undefined,
      mainRespOrgName: undefined
    }
  }

  const matched = findAccountPathByUserId(departAccountTree.value, mainRespUserId)
  const userInfo = matched?.account || getUserInfoById(mainRespUserId, options)
  const parentDepartment = matched?.path[matched.path.length - 1]

  return {
    mainRespUserId,
    mainRespUserEmpNo: userInfo?.employeeId,
    mainRespUserName: userInfo?.userName,
    mainRespOrgId: parentDepartment?.id,
    mainRespOrgName: parentDepartment?.name
  }
}

/**
 * 根据部门 id 获取主责部门接口字段。
 * @param mainRespOrgId 部门 id
 * @returns 主责部门字段
 */
const deptInfoByOrgId = (mainRespOrgId: string) => {
  if (!mainRespOrgId) {
    return {
      mainRespOrgId: undefined,
      mainRespOrgNo: undefined,
      mainRespOrgName: undefined
    }
  }

  const deptInfo = getDepartInfoById(mainRespOrgId)
  return {
    mainRespOrgId,
    mainRespOrgNo: deptInfo?.code,
    mainRespOrgName: deptInfo?.name
  }
}

/**
 * 批量事件页面级下拉数据源。
 * 普通下拉直接返回后端原始结构，人员树仅在真实交互时派生组件所需结构。
 * @returns 下拉数据源、原始树和按需加载方法
 */
export const useBatchEventOptions = () => {
  const userStore = useUserStore()

  const batch_event_status = computed(() => userStore.getDictItems('batch_event_status'))

  const closed_rule_priority = computed(() => userStore.getDictItems('closed_rule_priority'))

  const task_event_validity = computed(() => userStore.getDictItems('task_event_validity'))

  const batchEvent_event_validity = computed(() => {
    return userStore.getDictItems('batchEvent_event_validity')
  })

  const task_event_approve_close_reason = computed(() => {
    return userStore.getDictItems('task_event_approve_close_reason')
  })

  const task_event_reject_reason = computed(() => {
    return userStore.getDictItems('task_event_reject_reason')
  })

  const task_event_close_reason = computed(() => {
    return userStore.getDictItems('task_event_close_reason')
  })

  const batchEvent_reject_reason_type = computed(() => {
    return userStore.getDictItems('batchEvent_reject_reason_type')
  })

  const batchEvent_close_reason_type = computed(() => {
    return userStore.getDictItems('batchEvent_close_reason_type')
  })

  const batchEvent_warning_rate = computed(() => {
    return userStore.getDictItems('batchEvent_warning_rate')
  })

  const batchEvent_is_rejected = computed(() => {
    return userStore.getDictItems('batchEvent_is_rejected')
  })

  const task_event_approve_process_mode = computed(() => {
    return userStore.getDictItems('task_event_approve_process_mode')
  })

  const voc_intention = computed(() => {
    return userStore.getDictItems('voc_intention')
  })

  const closedLoopCategory = computed(() => {
    return referenceConditionsMap.value.closedLoopCategory || []
  })

  const mainRespOrgs = computed(() => {
    return referenceConditionsMap.value.mainRespOrgs || []
  })

  const event_attribute = computed(() => {
    return referenceConditionsMap.value.event_attribute || []
  })

  const topicList = computed(() => {
    return referenceConditionsMap.value.topicList || []
  })

  const tagTreeList = computed(() => {
    return referenceConditionsMap.value.tagTreeList || []
  })

  const batchBrandOptions = computed<BatchEventOptionVo[]>(() => {
    return batchConditionsMap.value.brandList || []
  })

  const batchCarSeriesOptions = computed<BatchEventOptionVo[]>(() => {
    return batchConditionsMap.value.carSeriesList || []
  })

  const brandOptions = computed(() => {
    return batchBrandOptions.value.length > 0 ? batchBrandOptions.value : userStore.getBrandService
  })

  const brandOptionProps = computed(() => {
    return batchBrandOptions.value.length > 0
      ? { label: 'name', value: 'code' }
      : { label: 'value', value: 'key' }
  })

  const carSeriesOptionProps = computed(() => {
    return batchCarSeriesOptions.value.length > 0
      ? { label: 'name', value: 'name' }
      : { label: 'value', value: 'value' }
  })

  /**
   * 根据体验代码查找子级标签。
   * @param tagCode 标签编码
   * @returns 子级标签列表
   */
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

  return {
    conditionsMap: batchConditionsMap,
    referenceConditionsMap,
    loadConditions,
    loadBatchEventConditions,
    loadBatchEventConditionsById,
    loadBatchEventConditionsBySoundId,
    batchRuleCategoryTree,
    loadBatchRuleCategoryTree,
    departTree,
    departAccountTree,
    departAccountCascaderOptions,
    departAccountMultiCascaderOptions,
    filterDepartmentCascaderOptions,
    filterHandlerCascaderOptions,
    loadDepartTree,
    loadDepartAccountTree,
    ensurePersonOptions,
    ensureDepartAccountCascaderOptions,
    ensureDepartAccountMultiCascaderOptions,
    ensureFilterDepartmentCascaderOptions,
    ensureFilterHandlerCascaderOptions,
    preloadDepartmentAndHandlerOptions,
    fetchAccountByDeptIds,
    fetchDepartAccountTreeByDeptIds,
    getDepartInfoById,
    getUserInfoById,
    filterValidHandlerUserIds,
    findUserByCcDisplay,
    getUserModelByUserId,
    userInfoByUserId,
    deptInfoByOrgId,
    departChange,
    departUserOptions,
    batchBrandOptions,
    batchCarSeriesOptions,
    batch_event_status,
    closed_rule_priority,
    task_event_validity,
    batchEvent_event_validity,
    task_event_approve_close_reason,
    task_event_reject_reason,
    task_event_close_reason,
    batchEvent_reject_reason_type,
    batchEvent_close_reason_type,
    batchEvent_warning_rate,
    batchEvent_is_rejected,
    task_event_approve_process_mode,
    voc_intention,
    closedLoopCategory,
    mainRespOrgs,
    event_attribute,
    topicList,
    tagTreeList,
    brandOptions,
    brandOptionProps,
    carSeriesOptionProps,
    personOptions,
    getTagChildren
  }
}
