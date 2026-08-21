import type { InsReportAccountInfoVo, InsReportSysDepartVo } from '@/api/common/index.d'
import type { ProcessingProgressCcSelectionItem } from './types'

export type ProcessingProgressPersonnelTreeNodeType = 'dept' | 'user'

export interface ProcessingProgressPersonnelTreeNode {
  key: string
  label: string
  type: ProcessingProgressPersonnelTreeNodeType
  disabled?: boolean
  children?: ProcessingProgressPersonnelTreeNode[]
  userId?: string
  userName?: string
  employeeId?: string
  deptId?: string
  deptName?: string
  fullLabel?: string
}

export interface ProcessingProgressPersonnelCascaderOption extends Record<string, unknown> {
  value: string
  label: string
  type: ProcessingProgressPersonnelTreeNodeType
  children?: ProcessingProgressPersonnelCascaderOption[]
  userId?: string
  userName?: string
  employeeId?: string
  deptId?: string
  deptName?: string
  fullLabel?: string
}

export interface ProcessingProgressCcCascaderOption extends Record<string, unknown> {
  value: string
  label: string
  type: ProcessingProgressPersonnelTreeNodeType
  children?: ProcessingProgressCcCascaderOption[]
  disabled?: boolean
  orgId: string
  orgNo: string
  orgName: string
  allFlag: boolean
  userId?: string
  userName?: string
  userEmpNo?: string
  fullLabel?: string
  filterText?: string
}

/**
 * 从部门路径中提取二级、三级部门名，不足时按当前可用层级兜底。
 * @param path 当前账号所在部门路径
 * @param account 原始账号节点
 * @returns 二级和三级部门名
 */
const getDisplayDepartmentNames = (
  path: InsReportSysDepartVo[] = [],
  account?: InsReportAccountInfoVo
) => {
  const pathNames = path.map(item => item.name || '').filter(Boolean)
  const secondaryDepartment =
    account?.secondDeptName || pathNames[pathNames.length - 2] || pathNames[0] || ''
  const tertiaryDepartment =
    account?.thirdDeptName || account?.deptName || pathNames[pathNames.length - 1] || ''

  return {
    secondaryDepartment,
    tertiaryDepartment
  }
}

/**
 * 按 userId 解析接口需要的二级、三级部门名称。
 * @param tree 原始部门人员树
 * @param userId 用户 id
 * @returns 业务责任人二级、三级部门名称
 */
export const getAccountDepartmentNamesByUserId = (
  tree: InsReportSysDepartVo[] = [],
  userId?: string
) => {
  const matched = findAccountPathByUserId(tree, userId)
  if (!matched) {
    return {
      secondDeptName: '',
      thirdDeptName: ''
    }
  }

  const path = matched.account.deptId
    ? findDepartmentPathInDepartAccountTree(tree, matched.account.deptId)
    : matched.path
  const departmentNames = getDisplayDepartmentNames(
    path.length ? path : matched.path,
    matched.account
  )

  return {
    secondDeptName: departmentNames.secondaryDepartment,
    thirdDeptName: departmentNames.tertiaryDepartment
  }
}

/**
 * 格式化处理进度中人员选择和展示使用的人员文案。
 * @param account 原始部门人员树中的账号节点
 * @returns 人员展示文案
 */
export const formatProcessingProgressAccountLabel = (account: InsReportAccountInfoVo) => {
  return `${account.userName || ''}${account.employeeId ? `-${account.employeeId}` : ''}`
}

/**
 * 格式化处理进度人员选择控件中的完整展示文案。
 * @param account 原始部门人员树中的账号节点
 * @param path 当前账号所在部门路径
 * @returns 二级部门#三级部门#员工-工号
 */
export const formatProcessingProgressAccountFullLabel = (
  account: InsReportAccountInfoVo,
  path: InsReportSysDepartVo[] = []
) => {
  const departmentNames = getDisplayDepartmentNames(path, account)
  const personLabel = formatProcessingProgressAccountLabel(account)

  return [departmentNames.secondaryDepartment, departmentNames.tertiaryDepartment, personLabel]
    .filter(Boolean)
    .join('#')
}

/**
 * 在原始部门人员树中查找账号节点，不预先拍平全量树。
 * @param tree 原始部门人员树
 * @param predicate 匹配函数
 * @returns 命中的账号节点
 */
export const findAccountInDepartAccountTree = (
  tree: InsReportSysDepartVo[] = [],
  predicate: (account: InsReportAccountInfoVo) => boolean
): InsReportAccountInfoVo | undefined => {
  for (const dept of tree) {
    const matchedAccount = dept.account?.find(predicate)
    if (matchedAccount) return matchedAccount

    if (dept.child?.length) {
      const matchedChildAccount = findAccountInDepartAccountTree(dept.child, predicate)
      if (matchedChildAccount) return matchedChildAccount
    }
  }

  return undefined
}

/**
 * 按 userId 在原始部门人员树中查找账号节点。
 * @param tree 原始部门人员树
 * @param userId 用户 id
 * @returns 命中的账号节点
 */
export const findAccountByUserId = (tree: InsReportSysDepartVo[] = [], userId?: string) => {
  if (!userId) return undefined
  return findAccountInDepartAccountTree(tree, account => account.userId === userId)
}

/**
 * 按 userId 查找账号节点及其所在部门路径，兼容账号缺少 deptId 的场景。
 * @param departments 原始部门人员树
 * @param userId 用户 id
 * @param path 当前递归路径
 * @returns 命中的账号和部门路径
 */
export const findAccountPathByUserId = (
  departments: InsReportSysDepartVo[] = [],
  userId?: string,
  path: InsReportSysDepartVo[] = []
): { account: InsReportAccountInfoVo; path: InsReportSysDepartVo[] } | null => {
  if (!userId) return null

  for (const department of departments) {
    const currentPath = [...path, department]
    const matchedAccount = department.account?.find(account => account.userId === userId)

    if (matchedAccount) {
      return {
        account: matchedAccount,
        path: currentPath
      }
    }

    if (department.child?.length) {
      const found = findAccountPathByUserId(department.child, userId, currentPath)
      if (found) return found
    }
  }

  return null
}

/**
 * 获取 userId 对应的人员展示文案。
 * @param tree 原始部门人员树
 * @param userId 用户 id
 * @returns 人员展示文案
 */
export const getAccountLabelByUserId = (tree: InsReportSysDepartVo[] = [], userId?: string) => {
  const account = findAccountByUserId(tree, userId)
  return account ? formatProcessingProgressAccountLabel(account) : ''
}

/**
 * 获取 userId 对应的完整人员展示文案。
 * @param tree 原始部门人员树
 * @param userId 用户 id
 * @returns 二级部门#三级部门#员工-工号
 */
export const getAccountFullLabelByUserId = (tree: InsReportSysDepartVo[] = [], userId?: string) => {
  const matched = findAccountPathByUserId(tree, userId)
  if (!matched) return ''

  const path = matched.account.deptId
    ? findDepartmentPathInDepartAccountTree(tree, matched.account.deptId)
    : matched.path
  return formatProcessingProgressAccountFullLabel(
    matched.account,
    path.length ? path : matched.path
  )
}

/**
 * 在原始部门人员树中递归查找部门路径。
 * @param departments 原始部门人员树
 * @param deptId 目标部门 id
 * @param path 当前递归路径
 * @returns 命中的部门路径
 */
export const findDepartmentPathInDepartAccountTree = (
  departments: InsReportSysDepartVo[] = [],
  deptId: string,
  path: InsReportSysDepartVo[] = []
): InsReportSysDepartVo[] => {
  for (const department of departments) {
    const currentPath = [...path, department]

    if (department.id === deptId) {
      return currentPath
    }

    if (department.child?.length) {
      const foundPath = findDepartmentPathInDepartAccountTree(department.child, deptId, currentPath)
      if (foundPath.length > 0) {
        return foundPath
      }
    }
  }

  return []
}

/**
 * 将原始部门人员树转换为 TreeV2 可消费的本地节点。
 * 该转换只在选择器展开时执行，不进入页面级缓存或 store。
 * @param tree 原始部门人员树
 * @returns TreeV2 节点和默认展开 keys
 */
export const buildProcessingProgressPersonnelTree = (tree: InsReportSysDepartVo[] = []) => {
  const walk = (
    dept: InsReportSysDepartVo,
    path: InsReportSysDepartVo[] = []
  ): ProcessingProgressPersonnelTreeNode => {
    const deptKey = `dept:${dept.id || ''}`
    const currentPath = [...path, dept]
    const childDeptNodes = (dept.child || [])
      .filter(item => item.id)
      .map(item => walk(item, currentPath))
    const accountNodes = (dept.account || [])
      .filter(account => account.userId)
      .map(account => {
        const userKey = `user:${account.userId}`
        const userNode: ProcessingProgressPersonnelTreeNode = {
          key: userKey,
          type: 'user',
          label: formatProcessingProgressAccountLabel(account),
          userId: account.userId,
          userName: account.userName,
          employeeId: account.employeeId,
          deptId: account.deptId || dept.id,
          deptName: account.deptName || dept.name,
          fullLabel: formatProcessingProgressAccountFullLabel(account, currentPath)
        }
        return userNode
      })

    const deptNode: ProcessingProgressPersonnelTreeNode = {
      key: deptKey,
      type: 'dept',
      label: dept.name || '',
      disabled: true,
      children: [...childDeptNodes, ...accountNodes]
    }
    return deptNode
  }

  const treeData = tree.filter(item => item.id).map(item => walk(item))

  return {
    treeData,
    defaultExpandedKeys: treeData.map(item => item.key)
  }
}

/**
 * 生成抄送级联值，部门代表整部门抄送，人员代表指定人员抄送。
 * @param item 抄送选择项
 * @returns Element Plus Cascader 使用的节点值
 */
export const getProcessingProgressCcCascaderValue = (item: ProcessingProgressCcSelectionItem) => {
  if (item.allFlag) {
    return item.orgId ? `dept:${item.orgId}` : ''
  }

  return item.userId ? `user:${item.userId}` : ''
}

/**
 * 将原始部门人员树递归转换为抄送人员级联选项。
 * 部门节点可被独立勾选，表示抄送该部门全部人员；人员节点表示抄送指定人员。
 * @param tree 原始部门人员树
 * @returns 抄送人员级联选项
 */
export const buildProcessingProgressCcCascaderOptions = (
  tree: InsReportSysDepartVo[] = []
): ProcessingProgressCcCascaderOption[] => {
  const walk = (
    dept: InsReportSysDepartVo,
    path: InsReportSysDepartVo[] = []
  ): ProcessingProgressCcCascaderOption | null => {
    if (!dept.id) return null

    const currentPath = [...path, dept]
    const pathLabel = currentPath
      .map(item => item.name || '')
      .filter(Boolean)
      .join('#')
    const accountOptions = (dept.account || [])
      .filter(account => account.userId)
      .map((account): ProcessingProgressCcCascaderOption => {
        const userId = account.userId || ''
        const userLabel = formatProcessingProgressAccountLabel(account)
        const fullLabel = formatProcessingProgressAccountFullLabel(account, currentPath)

        return {
          value: `user:${userId}`,
          type: 'user',
          label: userLabel,
          orgId: account.deptId || dept.id || '',
          orgNo: dept.code || account.thirdDeptCode || account.secondDeptCode || '',
          orgName: account.deptName || dept.name || '',
          allFlag: false,
          userId,
          userName: account.userName || '',
          userEmpNo: account.employeeId || '',
          fullLabel,
          filterText: [pathLabel, fullLabel, userLabel, account.employeeId || '']
            .filter(Boolean)
            .join('#')
        }
      })
    const childOptions = (dept.child || [])
      .map(child => walk(child, currentPath))
      .filter((item): item is ProcessingProgressCcCascaderOption => !!item)
    const children = [...accountOptions, ...childOptions]

    return {
      value: `dept:${dept.id}`,
      type: 'dept',
      label: dept.name || '',
      disabled: true,
      orgId: dept.id || '',
      orgNo: dept.code || '',
      orgName: dept.name || '',
      allFlag: true,
      filterText: pathLabel,
      ...(children.length ? { children } : {})
    }
  }

  return tree
    .map(item => walk(item))
    .filter((item): item is ProcessingProgressCcCascaderOption => !!item)
}

/**
 * 拍平抄送级联选项，便于根据 Cascader 选中值回查业务元数据。
 * @param options 抄送人员级联选项
 * @returns value 到选项节点的映射
 */
export const buildProcessingProgressCcCascaderOptionMap = (
  options: ProcessingProgressCcCascaderOption[] = []
) => {
  const optionMap = new Map<string, ProcessingProgressCcCascaderOption>()

  const walk = (items: ProcessingProgressCcCascaderOption[]) => {
    items.forEach(item => {
      optionMap.set(item.value, item)
      if (item.children?.length) {
        walk(item.children)
      }
    })
  }

  walk(options)
  return optionMap
}

/**
 * 将级联节点转换为抄送选择项。
 * @param option 抄送级联节点
 * @returns 标准化抄送选择项
 */
const mapCcOptionToSelection = (
  option: ProcessingProgressCcCascaderOption
): ProcessingProgressCcSelectionItem => {
  return {
    orgId: option.orgId || '',
    orgNo: option.orgNo || '',
    orgName: option.orgName || '',
    allFlag: option.allFlag,
    userId: option.allFlag ? '' : option.userId || '',
    userEmpNo: option.allFlag ? '' : option.userEmpNo || '',
    userName: option.allFlag ? '' : option.userName || ''
  }
}

/**
 * 将已确认抄送项转换为 Cascader 可回显的值数组。
 * @param selections 当前已确认抄送项
 * @param optionMap 当前级联选项映射
 * @returns Cascader v-model 值数组
 */
export const mapProcessingProgressCcSelectionsToCascaderValues = (
  selections: ProcessingProgressCcSelectionItem[] = [],
  optionMap: Map<string, ProcessingProgressCcCascaderOption>
) => {
  return selections.flatMap(item => {
    const value = getProcessingProgressCcCascaderValue(item)
    const option = value ? optionMap.get(value) : undefined
    if (!option || option.type !== 'user') return []

    return [value]
  })
}

/**
 * 将 Cascader 选中值还原为接口提交所需的抄送选择模型。
 * 仅人员节点允许提交，部门节点只作为展开层级使用。
 * @param values Cascader 选中值
 * @param optionMap 当前级联选项映射
 * @returns 标准化抄送选择项
 */
export const mapProcessingProgressCcValuesToSelections = (
  values: string[] = [],
  optionMap: Map<string, ProcessingProgressCcCascaderOption>
): ProcessingProgressCcSelectionItem[] => {
  return values
    .map(value => optionMap.get(value))
    .filter((option): option is ProcessingProgressCcCascaderOption => option?.type === 'user')
    .map(option => mapCcOptionToSelection(option))
}

/**
 * 将原始部门人员树裁剪为“二级部门 / 三级部门 / 人员”的处理进度级联选项。
 * 仅保留交互所需的三层结构，避免递归转换四级及更深层级造成无效渲染。
 * @param tree 原始部门人员树
 * @returns 处理进度人员级联选项
 */
export const buildProcessingProgressHandlerCascaderOptions = (
  tree: InsReportSysDepartVo[] = []
): ProcessingProgressPersonnelCascaderOption[] => {
  const buildAccountOptions = (
    accounts: InsReportAccountInfoVo[] = [],
    path: InsReportSysDepartVo[] = []
  ) => {
    return accounts
      .filter(account => account.userId)
      .map(account => {
        const userId = account.userId || ''
        const fullLabel = formatProcessingProgressAccountFullLabel(account, path)
        const option: ProcessingProgressPersonnelCascaderOption = {
          value: userId,
          type: 'user',
          label: formatProcessingProgressAccountLabel(account),
          userId,
          userName: account.userName,
          employeeId: account.employeeId,
          deptId: account.deptId || path[path.length - 1]?.id,
          deptName: account.deptName || path[path.length - 1]?.name,
          fullLabel
        }
        return option
      })
  }

  return tree
    .flatMap(root => root.child || [])
    .filter(secondDept => secondDept.id)
    .map((secondDept): ProcessingProgressPersonnelCascaderOption | null => {
      const thirdDeptOptions = (secondDept.child || [])
        .filter(thirdDept => thirdDept.id)
        .map((thirdDept): ProcessingProgressPersonnelCascaderOption | null => {
          const accountOptions = buildAccountOptions(thirdDept.account || [], [
            secondDept,
            thirdDept
          ])

          if (accountOptions.length === 0) {
            return null
          }

          return {
            value: `dept:${thirdDept.id || ''}`,
            type: 'dept',
            label: thirdDept.name || '',
            children: accountOptions
          } satisfies ProcessingProgressPersonnelCascaderOption
        })
        .filter((item): item is ProcessingProgressPersonnelCascaderOption => !!item)

      if (thirdDeptOptions.length === 0) {
        return null
      }

      return {
        value: `dept:${secondDept.id || ''}`,
        type: 'dept',
        label: secondDept.name || '',
        children: thirdDeptOptions
      } satisfies ProcessingProgressPersonnelCascaderOption
    })
    .filter((item): item is ProcessingProgressPersonnelCascaderOption => !!item)
}
