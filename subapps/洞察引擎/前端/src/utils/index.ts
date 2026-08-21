import CryptoJS from 'crypto-js'
import { cloneDeep } from 'lodash-es'
import dayjs from 'dayjs'
import { findChannelById, getChannelPathInfo } from '@/utils/channelUtils'

export const getAssetsFile = (url: string) => {
  return new URL(`../assets/${url}`, import.meta.url).href
}

/**
 * AES 加密
 * @param value
 */
export const enCrypt = (value: string) => {
  const key = 'Futongdongfang!@'
  const tempKey = CryptoJS.enc.Utf8.parse(key)
  const srcs = CryptoJS.enc.Utf8.parse(value)
  const encrypted = CryptoJS.AES.encrypt(srcs, tempKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.toString()
}

export const extractData = (dataList: any, childKey = 'child') => {
  const newData = cloneDeep(dataList)
  return newData.map((item: any) => {
    const currentItem = { ...item }
    currentItem?.[childKey]?.map((el: any) => {
      el[childKey] = null
      return {
        ...el
      }
    })
    return currentItem
  })
}

/**
 * 生成一个结束时间为今天，开始时间为val前的时间段
 * @param val
 * @param timeType day month year
 * @param format YYYY-MM-DD
 */
export const generateCurDateRange = (
  val: number = 1,
  timeType: any = 'month',
  format: string = 'YYYY-MM-DD'
) => {
  return [dayjs().subtract(val, timeType).format(format), dayjs().format(format)]
}

function isObject(value: any) {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}

/**
 * 将对象重置为初始值
 * @param obj
 */
export const resetObjectValues = (obj: { [key: string]: any }) => {
  if (!isObject(obj)) return obj
  Object.keys(obj).forEach(key => {
    const type = typeof obj[key]
    switch (type) {
      case 'object':
        if (Array.isArray(obj[key])) {
          // 如果是数组，则清空数组
          obj[key] = []
        } else if (obj[key] !== null) {
          // 如果是普通对象，则递归调用自身
          resetObjectValues(obj[key])
        }
        break
      case 'string':
        obj[key] = ''
        break
      case 'number':
        obj[key] = 0
        break
      case 'boolean':
        obj[key] = false
        break
      default:
        break
    }
  })
}

/**
 * 根据id过滤树形结构中的自身及子级节点
 * @param tree 树形结构
 * @param targetId 需要过滤的节点
 * @param targetKey 树形结构中与过滤节点的字段
 * @param childKey 子级key
 */
export const excludeNodeById = (
  tree: any,
  targetId: string,
  targetKey = 'id',
  childKey = 'child'
) => {
  const newTree = cloneDeep(tree)
  return newTree.filter((node: any) => {
    if (node[targetKey] !== targetId) {
      if (node[childKey] && node[childKey].length > 0) {
        node[childKey] = excludeNodeById(node[childKey], targetId)
        if (node[childKey].length === 0) {
          delete node[childKey]
        }
      }
      return true
    }
    return false
  })
}

/**
 * 延时器， 手动延时代码执行
 * @param timer
 */
export const delayer = (timer = 0) => {
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(1)
    }, timer)
  })
}

/**
 * 计算tableCardd的高度
 * 减去顶部导航栏高度 60px
 * 减去padding 与间隙高度 72px
 * 减去查询模块的高度
 */
export const computedCardHeight = (queryHeight = 0) => {
  return { height: `calc(100vh - 61px - 72px - ${queryHeight}px)` }
}

/**
 * 计算list高度
 */
export const listHeight = (otherHeight = 0) => {
  const innerHeight = window.innerHeight
  return innerHeight - 61 - 72 - otherHeight
}

// 渠道工具函数
export { findChannelById, getChannelPathInfo }
