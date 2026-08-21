<template>
  <div class="f-filter-layout">
    <div class="f-filter-layout__row flex w-full">
      <div
        ref="contentRef"
        class="f-filter-layout__content flex-1"
        :class="{ 'is-collapsed': !isExpanded }"
      >
        <slot></slot>
      </div>
      <div
        class="f-filter-layout__toggle ml-16 mr-16"
        :class="{ 'expand-placeholder': !shouldShowExpand }"
      >
        <div class="cursor-point lh-32" @click="toggleExpand">
          <span>{{ isExpanded ? expandedText : collapsedText }}</span>
          <el-icon class="ml-8">
            <component :is="isExpanded ? ArrowUp : ArrowDown" />
          </el-icon>
        </div>
      </div>
      <div class="f-filter-layout__actions w-200 border-left-e5e6eb">
        <div class="w-full h-full flex items-center justify-center">
          <el-button type="primary" class="ml-16" @click="handleQuery">
            <el-icon style="vertical-align: middle" class="mr-10">
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button color="#F2F3F5" @click="handleReset">
            <el-icon style="vertical-align: middle" class="mr-10">
              <RefreshRight />
            </el-icon>
            重置
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ArrowDown, ArrowUp, Search, RefreshRight } from '@element-plus/icons-vue'

withDefaults(
  defineProps<{
    collapsedText?: string
    expandedText?: string
  }>(),
  {
    collapsedText: '展开',
    expandedText: '收起'
  }
)

interface Emits {
  (e: 'query'): void
  (e: 'reset'): void
}

const emit = defineEmits<Emits>()

// 展开/收起状态
const isExpanded = defineModel<boolean>({ default: false })
const contentRef = ref<HTMLElement>()
const shouldShowExpand = ref(false)
let resizeObserver: ResizeObserver | undefined

function refreshExpandable() {
  const content = contentRef.value
  if (!content) return

  const previousMaxHeight = content.style.maxHeight
  content.style.maxHeight = 'none'
  const formItems = Array.from(content.querySelectorAll<HTMLElement>('.el-form-item')).filter(
    item => item.offsetParent !== null
  )
  const firstItemTop = formItems[0]?.getBoundingClientRect().top
  const hasMultipleRows =
    firstItemTop !== undefined &&
    formItems.some(item => item.getBoundingClientRect().top > firstItemTop + 8)
  // 部分内联表单在初次测量时不会返回稳定的 item 坐标，补充用完整内容高度判断。
  const fallbackHasMultipleRows = content.scrollHeight > 36
  content.style.maxHeight = previousMaxHeight
  shouldShowExpand.value = hasMultipleRows || fallbackHasMultipleRows

  if (!shouldShowExpand.value) {
    isExpanded.value = false
  }
}

// 切换展开/收起
function toggleExpand() {
  if (!shouldShowExpand.value) return
  isExpanded.value = !isExpanded.value
}

// 查询事件
function handleQuery() {
  emit('query')
}

// 重置事件
function handleReset() {
  emit('reset')
}

onMounted(() => {
  nextTick(refreshExpandable)
  if (contentRef.value) {
    resizeObserver = new ResizeObserver(() => {
      nextTick(refreshExpandable)
    })
    resizeObserver.observe(contentRef.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})
</script>

<style scoped lang="scss">
.f-filter-layout {
  min-width: 0;

  &__row,
  &__content {
    min-width: 0;
  }

  &__content {
    overflow: hidden;

    :deep(.el-form--inline) {
      width: 100%;
    }
  }

  &__toggle,
  &__actions {
    flex-shrink: 0;
  }

  &__actions {
    flex-basis: 200px;
  }

  .cursor-point {
    cursor: pointer;
    display: flex;
    align-items: center;
    // color: #1677ff;
    font-size: 14px;
    user-select: none;

    &:hover {
      opacity: 0.8;
    }
  }

  .border-left-e5e6eb {
    border-left: 1px solid #e5e6eb;
    padding-left: 0;
  }

  .expand-placeholder {
    visibility: hidden;
    pointer-events: none;
  }

  // 收起状态：只显示一行
  .is-collapsed {
    max-height: 32px;
    overflow: hidden;
  }
}
</style>
