<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { usePermissionsStore } from '@h5/store'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { isIndataUrl, setPendingIndataReturnFlag } from '@h5/utils/indataReturn'

defineOptions({
  name: 'CanswerLogin'
})

const userPermStore = usePermissionsStore()

interface IconPosition {
  x: number
  y: number
}

interface ViewportSize {
  width: number
  height: number
}

type DragInputEvent = PointerEvent | MouseEvent | TouchEvent
type DragInputType = 'pointer' | 'mouse' | 'touch'

const iconRef = ref<HTMLElement>()
const position = ref<IconPosition>({ x: 0, y: 0 })
const isDragging = ref(false)
const startPos = ref<IconPosition>({ x: 0, y: 0 })
const hasMoved = ref(false)
const hasUserDragged = ref(false)
const initialDragPoint = ref<IconPosition>({ x: 0, y: 0 })
const viewportSize = ref<ViewportSize>({ width: 0, height: 0 })

let viewportSyncTimer: number | null = null
let activePointerId: number | null = null
let activeDragInput: DragInputType | null = null

const supportsPointerEvents = typeof window !== 'undefined' && 'PointerEvent' in window

// 移动阈值，超过这个距离才认为是拖拽
const MOVE_THRESHOLD = 5
const ICON_FALLBACK_SIZE = 60
const DEFAULT_RIGHT_GAP = 12
const DEFAULT_BOTTOM_GAP = 240
const H5_LAYOUT_RECALIBRATION_DELAY = 30
const VIEWPORT_STABILIZE_DELAY = H5_LAYOUT_RECALIBRATION_DELAY + 50

/**
 * 从 Pointer/Mouse/Touch 事件中提取统一坐标，便于复用同一套拖拽逻辑。
 * @param e 当前输入事件
 * @returns 标准化后的客户端坐标；当事件中无可用触点时返回 null
 */
const getClientPoint = (e: DragInputEvent): IconPosition | null => {
  if ('touches' in e) {
    const touch = e.touches[0] ?? e.changedTouches[0]

    if (!touch) return null

    return {
      x: touch.clientX,
      y: touch.clientY
    }
  }

  return {
    x: e.clientX,
    y: e.clientY
  }
}

/**
 * 读取当前窗口的实时视口尺寸。
 */
const readWindowViewportSize = (): ViewportSize => {
  if (typeof window === 'undefined') {
    return { width: 0, height: 0 }
  }

  return {
    width: window.innerWidth,
    height: window.innerHeight
  }
}

/**
 * 获取当前可用于布局计算的视口尺寸。
 * - 优先使用同步维护的 viewport 状态，保证同一轮渲染内取值稳定
 * - 首次渲染或极端时序下回退到 window 实时值
 */
const getViewportSize = (): ViewportSize => {
  if (viewportSize.value.width > 0 && viewportSize.value.height > 0) {
    return viewportSize.value
  }

  return readWindowViewportSize()
}

/**
 * 同步记录最新视口尺寸，为默认右吸附模式和拖拽纠偏提供依据。
 */
const updateViewportSize = () => {
  viewportSize.value = readWindowViewportSize()
}

/**
 * 获取悬浮图标当前尺寸。
 * - 优先读取真实 DOM 尺寸，保证不同缩放下边界计算准确
 * - DOM 尚未就绪时回退到设计尺寸，避免首次计算异常
 */
const getIconSize = () => {
  return {
    width: iconRef.value?.offsetWidth || ICON_FALLBACK_SIZE,
    height: iconRef.value?.offsetHeight || ICON_FALLBACK_SIZE
  }
}

/**
 * 将坐标限制在当前可视区域内，避免窗口缩放后图标被挤出屏幕。
 * @param nextPosition 即将写入的图标坐标
 * @returns 经过边界收敛后的合法坐标
 */
const clampPosition = (nextPosition: IconPosition): IconPosition => {
  const { width: viewportWidth, height: viewportHeight } = getViewportSize()
  const { width, height } = getIconSize()
  const maxX = Math.max(0, viewportWidth - width)
  const maxY = Math.max(0, viewportHeight - height)

  return {
    x: Math.min(Math.max(nextPosition.x, 0), maxX),
    y: Math.min(Math.max(nextPosition.y, 0), maxY)
  }
}

/**
 * 计算默认右吸附模式下的纵向位置。
 * - 横向使用固定 right 吸附，不再维护默认态的 left 坐标
 * - 纵向沿用当前视觉层级，并在极小高度下自动收敛到可视区
 */
const getDefaultTop = () => {
  const { height } = getIconSize()
  const { height: viewportHeight } = getViewportSize()
  const desiredTop = viewportHeight - height - DEFAULT_BOTTOM_GAP
  const maxTop = Math.max(0, viewportHeight - height)

  return Math.min(Math.max(desiredTop, 0), maxTop)
}

/**
 * 根据当前视口同步悬浮图标状态。
 * - 默认右吸附模式只需刷新视口尺寸，横向天然贴右
 * - 用户拖拽后仅做边界纠偏，不重置回默认位置
 */
const syncFloatingIconState = () => {
  updateViewportSize()

  if (!hasUserDragged.value) return

  position.value = clampPosition(position.value)
}

/**
 * 视口变化后重新校准图标位置。
 * - 默认右吸附模式无需重算横向位置，只需更新视口尺寸
 * - 拖拽模式下再执行一次边界纠偏，避免恢复后落在屏幕外
 */
const queueViewportSync = () => {
  nextTick(() => {
    syncFloatingIconState()
  })

  if (viewportSyncTimer !== null) {
    window.clearTimeout(viewportSyncTimer)
  }

  viewportSyncTimer = window.setTimeout(() => {
    syncFloatingIconState()
    viewportSyncTimer = null
  }, VIEWPORT_STABILIZE_DELAY)
}

/**
 * 处理页面恢复后的图标位置同步。
 * - 统一复用 pageshow / visibilitychange(visible) 的恢复逻辑
 * - 保证悬浮图标跟随布局层的 viewport/rem 重校准重新收敛位置
 */
const handlePageRestore = () => {
  queueViewportSync()
}

/**
 * 处理视口尺寸变化后的图标位置同步。
 */
const handleViewportChange = () => {
  queueViewportSync()
}

/**
 * 页面重新可见时兜底同步图标位置。
 */
const handleVisibilityChange = () => {
  if (document.visibilityState !== 'visible') return

  handlePageRestore()
}

/**
 * 开始一次新的拖拽流程，并记录命中图标时的偏移量。
 * @param point 当前输入事件对应的客户端坐标
 */
const startDragging = (point: IconPosition) => {
  isDragging.value = true
  hasMoved.value = false
  updateViewportSize()
  const currentRect = iconRef.value?.getBoundingClientRect()

  initialDragPoint.value = point

  startPos.value = {
    x: point.x - (currentRect?.left ?? position.value.x),
    y: point.y - (currentRect?.top ?? position.value.y)
  }
}

/**
 * 根据最新指针位置刷新图标坐标，并沿用现有阈值区分点击与拖拽。
 * @param point 当前输入事件对应的客户端坐标
 * @param event 原始输入事件，仅在确认拖拽后用于阻止默认手势/选区行为
 */
const updateDraggingPosition = (point: IconPosition, event?: DragInputEvent) => {
  if (!isDragging.value || !iconRef.value) return

  const moveX = Math.abs(point.x - initialDragPoint.value.x)
  const moveY = Math.abs(point.y - initialDragPoint.value.y)

  if (moveX > MOVE_THRESHOLD || moveY > MOVE_THRESHOLD) {
    hasMoved.value = true
    hasUserDragged.value = true

    if (event?.cancelable) {
      event.preventDefault()
    }

    position.value = clampPosition({
      x: point.x - startPos.value.x,
      y: point.y - startPos.value.y
    })
  }
}

/**
 * 释放当前拖拽过程中可能注册的浏览器能力，避免残留监听或指针占用。
 */
const cleanupDraggingSideEffects = () => {
  if (activePointerId !== null && iconRef.value?.hasPointerCapture?.(activePointerId)) {
    iconRef.value.releasePointerCapture(activePointerId)
  }

  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseup', handleMouseUp)
}

/**
 * 结束拖拽状态，保留当前位置，并重置输入源上下文。
 */
const stopDragging = () => {
  cleanupDraggingSideEffects()
  isDragging.value = false
  activePointerId = null
  activeDragInput = null
}

/**
 * Pointer 事件优先覆盖企微 PC 端与现代移动端，统一处理鼠标/触屏拖拽。
 * @param e PointerDown 事件对象
 */
const handlePointerDown = (e: PointerEvent) => {
  if (!supportsPointerEvents) return
  if (e.pointerType === 'mouse' && e.button !== 0) return

  activeDragInput = 'pointer'
  activePointerId = e.pointerId
  iconRef.value?.setPointerCapture?.(e.pointerId)
  startDragging({
    x: e.clientX,
    y: e.clientY
  })
}

/**
 * 在 Pointer 拖拽过程中实时同步坐标。
 * @param e PointerMove 事件对象
 */
const handlePointerMove = (e: PointerEvent) => {
  if (!supportsPointerEvents || activeDragInput !== 'pointer' || activePointerId !== e.pointerId) return

  updateDraggingPosition(
    {
      x: e.clientX,
      y: e.clientY
    },
    e
  )
}

/**
 * Pointer 流程结束或被浏览器中断时统一收尾。
 * @param e Pointer 结束类事件对象
 */
const handlePointerEnd = (e: PointerEvent) => {
  if (!supportsPointerEvents || activeDragInput !== 'pointer' || activePointerId !== e.pointerId) return

  stopDragging()
}

/**
 * 兼容不支持 PointerEvent 的旧触屏环境。
 * @param e 触摸开始事件对象
 */
const handleTouchStart = (e: TouchEvent) => {
  if (supportsPointerEvents) return

  const point = getClientPoint(e)

  if (!point) return

  activeDragInput = 'touch'
  startDragging(point)
}

/**
 * 兼容旧触屏环境下的拖拽移动。
 * @param e 触摸移动事件对象
 */
const handleTouchMove = (e: TouchEvent) => {
  if (supportsPointerEvents || activeDragInput !== 'touch') return

  const point = getClientPoint(e)

  if (!point) return

  updateDraggingPosition(point, e)
}

/**
 * 兼容旧触屏环境下的拖拽结束。
 */
const handleTouchEnd = () => {
  if (supportsPointerEvents || activeDragInput !== 'touch') return

  stopDragging()
}

/**
 * 兼容不支持 PointerEvent 的桌面环境，使用全局鼠标监听保持拖拽连续性。
 * @param e 鼠标按下事件对象
 */
const handleMouseDown = (e: MouseEvent) => {
  if (supportsPointerEvents || e.button !== 0) return

  activeDragInput = 'mouse'
  startDragging({
    x: e.clientX,
    y: e.clientY
  })
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseup', handleMouseUp)
}

/**
 * 旧桌面环境下根据鼠标位置同步悬浮图标坐标。
 * @param e 鼠标移动事件对象
 */
const handleMouseMove = (e: MouseEvent) => {
  if (supportsPointerEvents || activeDragInput !== 'mouse') return

  updateDraggingPosition(
    {
      x: e.clientX,
      y: e.clientY
    },
    e
  )
}

/**
 * 旧桌面环境下结束鼠标拖拽。
 */
const handleMouseUp = () => {
  if (supportsPointerEvents || activeDragInput !== 'mouse') return

  stopDragging()
}

/**
 * 点击悬浮图标后执行 canswer 鉴权与跳转。
 */
const handleCanswer = async () => {
  if (!hasMoved.value) {
    const res = (await userPermStore.handleCanswerAuth()) as any
    console.log('handleCanswer->getAuthDataUrl--->res', res)
    console.log('handleCanswer->res.result--url', res?.result)
    if (res?.success) {
      if (res?.result) {
        const link = res.result
        // 仅在跳往 /indata 系统页面前写入一次性返回标记。
        // 后续页面从外部系统返回并触发 pageshow/visibilitychange 时，
        // H5 布局层将据此决定是否需要补做登录态探测。
        if (isIndataUrl(link)) {
          setPendingIndataReturnFlag(link)
        }
        // router.push({
        //   path: '/h5/originalView',
        //   query: {
        //     link: encodeURIComponent(link)
        //   }
        // })
        // window.location.href = encodeURIComponent(link)
        window.location.href = link
      } else {
        showToast('抱歉，您暂无此菜单访问权限，请联系系统管理员配置权限，感谢配合。')
      }
    } else {
      console.log('handleCanswer-->error--->', res)
    }
  }
}

/**
 * 悬浮图标样式。
 * - 默认状态固定吸附在右侧，窗口缩放时无需重算横向坐标
 * - 拖拽后切换为 left/top 绝对坐标模式，仅在越界时纠偏
 */
const iconStyle = computed(() => {
  if (hasUserDragged.value) {
    return {
      left: `${position.value.x}px`,
      top: `${position.value.y}px`,
      right: 'auto',
      bottom: 'auto'
    }
  }

  return {
    left: 'auto',
    top: `${getDefaultTop()}px`,
    right: `${DEFAULT_RIGHT_GAP}px`,
    bottom: 'auto'
  }
})

onMounted(() => {
  queueViewportSync()

  window.addEventListener('resize', handleViewportChange)
  window.addEventListener('pageshow', handlePageRestore)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleViewportChange)
  window.removeEventListener('pageshow', handlePageRestore)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  cleanupDraggingSideEffects()

  if (viewportSyncTimer !== null) {
    window.clearTimeout(viewportSyncTimer)
    viewportSyncTimer = null
  }
})

const route = useRoute()
const hideIcon = computed(() => {
  if (route.name === 'H5OriginalView') {
    return false
  } else if (route.name === 'H5DataSquareReportDetail' && route.query.preview === '1') {
    return false
  }
  return true
})

watch(hideIcon, visible => {
  if (!visible) return

  queueViewportSync()
})
</script>

<template>
  <div
    v-if="hideIcon"
    ref="iconRef"
    class="canswer-login"
    :style="iconStyle"
    @pointerdown="handlePointerDown"
    @pointermove="handlePointerMove"
    @pointerup="handlePointerEnd"
    @pointercancel="handlePointerEnd"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
    @touchcancel="handleTouchEnd"
    @mousedown="handleMouseDown"
    @click="handleCanswer"
  >
    <img src="@/assets/h5/AI2.png" class="ai2Img" alt="" />
  </div>
</template>

<style lang="scss" scoped>
.canswer-login {
  position: fixed;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  z-index: 999;
  opacity: 1;
  user-select: none;
  cursor: move;
  touch-action: none;

  .ai2Img {
    width: 60px;
    height: 60px;
    object-fit: contain;
    pointer-events: none;
  }
}
</style>
