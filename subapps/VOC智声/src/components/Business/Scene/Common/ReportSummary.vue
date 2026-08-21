<script setup lang="ts">
import { computed, ref, nextTick, watch, onBeforeUnmount } from 'vue'
import { debounce, isEmpty } from 'lodash-es'
import { ElMessage } from 'element-plus'
import { globalRequestQueue } from '@/api/reportSummary'

defineOptions({
  name: 'ReportSummary'
})

interface Props {
  // API函数
  apiFunction?: (params: Record<string, any>) => Promise<any>
  // 查询参数
  queryParams?: Record<string, any>
  // 是否自动加载
  autoLoad?: boolean
  // 打字速度（毫秒）
  typeSpeed?: number
  // 静态文本（如果提供则不请求接口）
  staticText?: string
}

const props = withDefaults(defineProps<Props>(), {
  autoLoad: true,
  typeSpeed: 4
})

const displayText = ref('')
const reportStrRef = ref<HTMLElement>()
const loading = ref(false)
const typing = ref(false)
const abortController = ref<AbortController | null>(null)
const isPageExportReady = computed(() => !loading.value && !typing.value)

// 暴露给父组件的方法
defineExpose({
  loadData,
  clearText: () => (displayText.value = '')
})

// 打字机效果
const typeText = async (text: string, controller?: AbortController) => {
  typing.value = true

  try {
    for (const char of text) {
      // 检查是否已取消
      if (controller?.signal.aborted || abortController.value?.signal.aborted) {
        return
      }
      await new Promise(resolve => setTimeout(resolve, props.typeSpeed))
      displayText.value += char
      await nextTick()
      if (reportStrRef.value) {
        reportStrRef.value.scrollTop = reportStrRef.value.scrollHeight
      }
    }
  } finally {
    typing.value = false
  }
}

const handleStreamResponse = async (params: Record<string, any> = {}) => {
  if (!props.apiFunction) {
    ElMessage.error('未提供API函数')
    return
  }

  // 取消之前的请求
  if (abortController.value) {
    abortController.value.abort()
  }

  // 创建新的AbortController
  abortController.value = new AbortController()
  const currentController = abortController.value

  // 立即设置 loading 状态
  loading.value = true
  displayText.value = ''

  // 使用全局队列控制并发
  await globalRequestQueue.add(async () => {
    // 检查是否已被取消
    if (currentController.signal.aborted) {
      return
    }

    // 再次检查 apiFunction 是否存在（TypeScript 类型守卫）
    if (!props.apiFunction) {
      return
    }

    try {
      // 获取流式响应，传入signal
      const response = await props.apiFunction({
        ...params,
        signal: currentController.signal
      })

      // 检查是否是Response对象（流式）
      if (response instanceof Response && response.body) {
        const reader = response.body.getReader()
        const decoder = new TextDecoder()

        try {
          while (true) {
            // 检查是否已取消（使用当前controller）
            if (currentController.signal.aborted) {
              reader.cancel()
              break
            }

            const { done, value } = await reader.read()
            if (done) break

            const chunk = decoder.decode(value, { stream: true })
            const lines = chunk.split('\n')

            for (const line of lines) {
              if (currentController.signal.aborted) {
                return
              }
              if (line.startsWith('data: ')) {
                const data = line.substring(6)
                if (data === '[END]') return
                if (data.trim() && !data.startsWith('<think>')) {
                  await typeText(data, currentController)
                }
              }
            }
          }
        } finally {
          reader.releaseLock()
        }
      } else {
        // 兼容非流式响应
        const responseText = typeof response === 'string' ? response : JSON.stringify(response)
        const lines = responseText.split('\n')

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.substring(6)
            if (data === '[END]') break
            if (data.trim() && !data.startsWith('<think>')) {
              await typeText(data, currentController)
            }
          }
        }
      }
    } catch (error: any) {
      // 如果是取消请求，不显示错误信息
      if (error.name === 'AbortError' || currentController.signal.aborted) {
        // console.log('请求已取消')
        return
      }
      console.error('处理流数据时发生错误:', error)
      ElMessage.error(error.message || '获取数据失败')
    } finally {
      // 只有当前请求才更新loading状态
      if (currentController === abortController.value) {
        loading.value = false
      }
    }
  })
}

// 加载数据方法
async function loadData(customParams?: Record<string, any>) {
  const params = customParams || props.queryParams || {}

  // ✅ 如果 params 是空对象，不调用接口
  if (isEmpty(params) && !props.staticText) {
    console.log('ReportSummary: queryParams 为空对象，跳过接口调用')
    return
  }

  if (props.staticText) {
    displayText.value = ''
    await typeText(props.staticText)
    return
  }

  await handleStreamResponse(params)
}

// 创建防抖版本的loadData，只执行最后一次参数变化
const debouncedLoadData = debounce(loadData, 300)

// 立即清空并加载数据
function clearAndLoad() {
  // 取消之前的请求（包括打字机效果）
  if (abortController.value) {
    abortController.value.abort()
  }
  // 取消之前的防抖调用
  debouncedLoadData.cancel()
  // 立即清空显示内容
  displayText.value = ''
  // 执行防抖加载
  debouncedLoadData()
}

// 组件销毁时取消请求
onBeforeUnmount(() => {
  if (abortController.value) {
    abortController.value.abort()
  }
})

// 自动监听props变化并加载数据
watch(
  () => [props.queryParams, props.staticText, props.apiFunction],
  () => {
    if (props.autoLoad && (props.queryParams || props.staticText || props.apiFunction)) {
      clearAndLoad()
    }
  },
  { deep: true, immediate: true }
)
</script>

<template>
  <div
    class="report-summary"
    data-page-export-report-summary
    :data-page-export-ready="isPageExportReady ? 'true' : 'false'"
  >
    <div class="logo">
      <img src="@/assets/images/ai-head.svg" width="100" height="100" alt="" />
    </div>
    <div class="content" data-page-export-report-summary-content>
      <div class="title-container" data-page-export-report-summary-title>
        <!-- <div class="fh">“</div> -->
        <SvgIcon name="ld" width="28px" height="24px"></SvgIcon>
        <SvgIcon name="bgjd" width="80px" height="28px" class="ml-10"></SvgIcon>
        <div class="subtitle">(内容为模型生成，仅作为参考!)</div>
      </div>

      <div class="report-str-container" data-page-export-report-summary-content>
        <div ref="reportStrRef" class="report-str" data-page-export-report-summary-content>
          {{ displayText }}
        </div>
        <div class="cc-icon">
          <SvgIcon name="rd" width="28px" height="24px"></SvgIcon>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.report-summary {
  width: 100%;
  padding: 16px;
  position: relative;
  border-radius: 8px;
  display: flex;
  // align-items: center;
  background: linear-gradient(180deg, rgba(238, 248, 255, 0.8) 0%, rgba(238, 248, 255, 0) 100%);
  overflow: hidden;
  border: 3px solid transparent;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 2px solid transparent;
    border-image: linear-gradient(180deg, rgba(138, 204, 255, 1), rgba(195, 238, 253, 1)) 2 2;
    border-radius: 12px; /* 稍微大于元素本身的圆角 */
    pointer-events: none; /* 确保点击能穿透伪元素 */
  }
  .logo {
    // width: 100px;
    // height: 100px;
    border-radius: 60px 60px 60px 60px;
    // background: #000;
  }
  .content {
    flex: 1;
    margin-left: 20px;
    .title-container {
      display: flex;
      align-items: center;
      flex-wrap: nowrap;
      .title {
        // font-weight: 500;
        // font-size: 20px;
        // color: #1f2733;
        // line-height: 28px;
      }
      .subtitle {
        font-weight: 500;
        font-size: 14px;
        color: #60b8eb;
        line-height: 28px;
        margin-left: 10px;
        flex-shrink: 0;
        white-space: nowrap;
      }
    }
    .report-str-container {
      display: flex;
    }
    .report-str {
      margin-top: 8px;
      font-weight: 400;
      font-size: 18px;
      color: #1f2733;
      line-height: 32px;
      flex: 1;
      max-height: 64px;
      overflow-y: auto;
    }
    .cc-icon {
      width: 36px;
      height: 29px;
      margin-left: 10px;
      margin-top: auto;
    }
  }
}
</style>
