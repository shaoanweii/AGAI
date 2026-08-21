import { nextTick, type Ref } from 'vue'

interface UseAutoFillListHeightOptions {
  // 判断是否还可以继续加载更多（通常由 canLoadMore 或 hasMore 计算属性提供）
  canLoadMore: () => boolean
  // 实际的加载更多函数（内部会按页追加数据）
  loadMore: () => Promise<void> | void
  // 当 scrollHeight 与 clientHeight 的差值在 threshold 以内时视为“已撑满”，默认 10px
  threshold?: number
  // 最多自动触发加载的次数，防止极端场景循环过多，默认 5 次
  maxAutoLoadTimes?: number
}

/**
 * 首屏自动补齐列表高度：当列表内容过少未撑满容器时，自动连续触发 loadMore，直到撑满或没有更多数据
 */
export function useAutoFillListHeight(
  scrollRef: Ref<HTMLElement | null>,
  options: UseAutoFillListHeightOptions
) {
  const { canLoadMore, loadMore, threshold = 10, maxAutoLoadTimes = 5 } = options

  const autoFillListHeight = async () => {
    // 先等待当前这一页渲染完成，再去读取容器高度
    await nextTick()

    for (let i = 0; i < maxAutoLoadTimes; i++) {
      const el = scrollRef.value
      if (!el) break

      // 已经出现滚动条/列表基本撑满，无需再自动加载
      if (el.scrollHeight > el.clientHeight + threshold) {
        break
      }

      // 没有更多可加载的数据则不再继续
      if (!canLoadMore()) {
        break
      }

      await loadMore()
      // 等待追加数据渲染完成，再进入下一轮判断
      await nextTick()
    }
  }

  return {
    autoFillListHeight
  }
}
