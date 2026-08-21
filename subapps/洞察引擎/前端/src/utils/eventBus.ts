import mitt from 'mitt'

/**
 * 触发事件 eventBus.emit(eventName)
 * 监听事件 eventBus.on(eventName, () => void)
 */
const eventBus = mitt()
// 用于监听、触发事件
export default eventBus
