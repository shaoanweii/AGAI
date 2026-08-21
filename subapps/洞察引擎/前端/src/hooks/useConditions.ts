import request from '../api/index'
import type { Conditions, ConditionsDetailItem } from '@/types'

interface Options {
  url: string
  methods?: string
  params?: Record<string, any>
  headers?: Record<string, any>
  /**
   * 是否在组件挂载前自动请求字典。
   * 默认值保持为 `true`，仅在 store 里手动调用 `getConditions` 时显式关闭。
   */
  immediate?: boolean
}

/**
 * 统一处理条件字典请求与结果映射。
 * @param options 请求配置；默认会在组件挂载前自动拉取一次字典数据
 * @returns 条件字典响应式对象，以及可手动触发的拉取方法
 */
export default function useConditions(options?: Options) {
  const conditions = reactive<Record<string, ConditionsDetailItem[]>>({})
  // 兼容历史页面的自动拉取行为，同时给 store 场景提供关闭入口，避免重复请求。
  const shouldAutoFetch = options?.immediate !== false

  /**
   * 主动拉取字典数据。
   * 这里保留手动触发能力，便于 store / action 等组件外场景按需等待加载完成。
   * @returns 解析后的字典映射，失败时返回空对象
   */
  const getConditions = (): Promise<Record<string, ConditionsDetailItem[]>> => {
    if (!options?.url) {
      return Promise.resolve({})
    }

    return request<Conditions[]>({
      method: options?.methods || 'GET',
      url: options?.url,
      params: options?.params,
      headers: options?.headers
    })
      .then(response => {
        if (response.code === '200') {
          const nextConditions = handleConditions(response.result || [])
          Object.assign(conditions, nextConditions)
          return nextConditions
        }
        return {}
      })
      .catch(err => {
        console.log(err)
        return {}
      })
  }

  /**
   * 将后端数组结构转为页面更易消费的键值映射。
   * @param list 后端返回的条件数组
   * @returns 以条件 key 为索引的详情列表
   */
  const handleConditions = (list: Conditions[]) => {
    const newConditions: Record<string, ConditionsDetailItem[]> = {}
    list?.forEach((item: any) => {
      newConditions[item?.key] = item?.details
    })
    return newConditions
  }

  onBeforeMount(() => {
    // 组件直接使用时保留自动拉取；手动控制的调用方自行触发，避免同一接口打两次。
    if (options?.url && shouldAutoFetch) {
      getConditions()
    }
  })

  return {
    conditions,
    getConditions
  }
}
