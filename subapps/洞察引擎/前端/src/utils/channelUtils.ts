/**
 * 在渠道树中查找指定的渠道节点
 * @param channelTree 渠道树形结构数据
 * @param channelId 要查找的渠道ID
 * @returns 找到的渠道节点，如果未找到则返回null
 */
export const findChannelById = (channelTree: any[], channelId: string): any | null => {
  if (!channelTree || !channelId) {
    return null
  }

  // 递归查找函数
  const findRecursive = (nodes: any[]): any | null => {
    for (const node of nodes) {
      // 如果当前节点匹配，直接返回
      if (node.code === channelId || node.id === channelId) {
        return node
      }

      // 如果有子节点，递归查找子节点
      if (node.child && node.child.length > 0) {
        const found = findRecursive(node.child)
        if (found) {
          return found
        }
      }
    }

    return null
  }

  return findRecursive(channelTree)
}

/**
 * 获取渠道的完整路径信息（一级、二级、三级渠道名称）
 * @param channelTree 渠道树形结构数据
 * @param channelId 要查找的渠道ID
 * @returns 包含一级、二级、三级渠道名称的对象
 */
export const getChannelPathInfo = (
  channelTree: any[],
  channelId: string
): {
  firstChannelName: string | null
  secondChannelName: string | null
  thirdChannelName: string | null
} => {
  if (!channelTree || !channelId) {
    return {
      firstChannelName: null,
      secondChannelName: null,
      thirdChannelName: null
    }
  }

  // 用于存储路径节点的数组
  let pathNodes: any[] = []

  // 递归查找并构建路径
  const findPathRecursive = (nodes: any[], path: any[] = []): boolean => {
    for (const node of nodes) {
      const currentPath = [...path, node]

      // 如果当前节点匹配，保存路径
      if (node.code === channelId || node.id === channelId) {
        pathNodes = currentPath
        return true
      }

      // 如果有子节点，递归查找子节点
      if (node.child && node.child.length > 0) {
        if (findPathRecursive(node.child, currentPath)) {
          return true
        }
      }
    }

    return false
  }

  // 执行查找
  findPathRecursive(channelTree)

  // 根据路径长度提取各级渠道名称
  let firstChannelName = null
  let secondChannelName = null
  let thirdChannelName = null

  if (pathNodes.length > 0) {
    firstChannelName = pathNodes[0].name
  }

  if (pathNodes.length > 1) {
    secondChannelName = pathNodes[1].name
  }

  if (pathNodes.length > 2) {
    thirdChannelName = pathNodes[2].name
  }

  return {
    firstChannelName,
    secondChannelName,
    thirdChannelName
  }
}
