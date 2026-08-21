import mitt from 'mitt'
import { onBeforeUnmount } from 'vue'

interface Fn<T = any> {
  (...arg: T[]): T
}

interface Option {
  name: string // 事件名称
  callback: Fn // 回调
}

const emitter = mitt()

export const useEmitt = (option?: Option) => {
  if (option) {
    emitter.on(option.name, option.callback)

    onBeforeUnmount(() => {
      emitter.off(option.name)
    })
  }

  return {
    emitter
  }
}

export const emittName = {
  // 获取渠道
  getChannel: 'GETCHANNEL',
  // 客户切换
  clientChange: 'CLIENTCHANGE'
}
