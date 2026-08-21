<template>
  <div v-if="imgUrl" style="border: 1px solid #eee; width: 80px; height: 80px">
    <img :src="imgUrl" width="100%" height="100%" style="object-fit: contain" />
  </div>
  <div class="ml-8">
    <div>
      <el-upload
        :action="uploadAction"
        :headers="{ Authorization: token }"
        :before-upload="onBeforeUpload"
        :show-file-list="false"
        accept=".png"
        @error="handleError"
        @success="handleSuccess"
      >
        <el-button type="primary" v-if="!imgUrl">
          <template #icon>
            <Plus />
          </template>
          点击上传
        </el-button>
        <el-button type="primary" v-else>
          <template #icon>
            <Plus />
          </template>
          重新上传
        </el-button>
      </el-upload>
    </div>
    <div class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

interface Props {
  action: string
  token: string
  imgUrl?: string
  tip?: string
  maxSizeKB?: number
  width?: number
  height?: number
  validateDimension?: boolean
}

interface Emits {
  (e: 'success', res: any): void
}

const emit = defineEmits<Emits>()

const props = withDefaults(defineProps<Props>(), {
  imgUrl: '',
  tip: '仅支持上传png格式图片（推荐尺寸80*60，大小不超过50KB）',
  maxSizeKB: 50,
  width: 80,
  height: 60,
  validateDimension: false
})

/**
 * 根据环境变量补齐上传接口前缀，确保生产环境里的 /api 路径能映射到真实网关前缀。
 */
const uploadAction = computed(() => {
  const action = props.action?.trim() || ''
  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').trim()

  if (!action) return ''

  // 已经是完整地址时直接透传，避免重复拼接域名或网关前缀。
  if (/^(https?:)?\/\//i.test(action)) {
    return action
  }

  const normalizedApiBaseUrl = apiBaseUrl.replace(/\/+$/, '')
  if (!normalizedApiBaseUrl) {
    return action
  }

  // 仅替换约定的 /api 前缀，避免误伤其它相对路径或已完成映射的地址。
  if (/^\/api(?:\/|$)/.test(action)) {
    return action.replace(/^\/api(?=\/|$)/, normalizedApiBaseUrl)
  }

  return action
})

/**
 * 上传组件不会经过项目 axios 封装，因此需要在组件内自行兜底解析 HTTP 500/网络异常提示。
 */
const parseUploadErrorMessage = (error: unknown) => {
  const fallbackMessage = '图片上传失败，请稍后重试'

  const tryParseMessage = (value: unknown) => {
    if (typeof value !== 'string') return ''
    const trimmedValue = value.trim()
    if (!trimmedValue) return ''
    try {
      const parsedValue = JSON.parse(trimmedValue)
      return parsedValue?.message || parsedValue?.error || ''
    } catch {
      return trimmedValue
    }
  }

  if (typeof error === 'string') {
    return tryParseMessage(error) || fallbackMessage
  }

  if (!error || typeof error !== 'object') {
    return fallbackMessage
  }

  const uploadError = error as Record<string, any>
  const message = tryParseMessage(uploadError.message)

  if (message) {
    return message
  }

  if (uploadError.status) {
    return `图片上传失败（HTTP ${uploadError.status}）`
  }

  return fallbackMessage
}

const getImageSize = (file: File) => {
  return new Promise<{ width: number; height: number }>((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => {
      const img = new Image()
      img.src = reader.result as string
      img.onload = () => {
        resolve({ width: img.width, height: img.height })
      }
      img.onerror = () => reject(new Error('图片解析失败'))
    }
    reader.onerror = () => reject(new Error('图片读取失败'))
  })
}

const onBeforeUpload = async (file: File) => {
  const maxSize = props.maxSizeKB * 1024

  if (file.type !== 'image/png') {
    ElMessage.error('仅支持上传png格式图片')
    return false
  }

  if (file.size > maxSize) {
    ElMessage.error(`图片大小不能超过${props.maxSizeKB}KB`)
    return false
  }

  // 默认只做格式与大小校验；仅在显式开启时才执行严格尺寸校验。
  if (props.validateDimension) {
    try {
      const size = await getImageSize(file)
      if (size.width !== props.width || size.height !== props.height) {
        ElMessage.error(`图片尺寸必须为${props.width}*${props.height}`)
        return false
      }
    } catch (err: any) {
      ElMessage.error(err?.message || '图片校验失败')
      return false
    }
  }

  return true
}

const handleError = (error: unknown) => {
  ElMessage.error(parseUploadErrorMessage(error))
}

const handleSuccess = (res: any) => {
  // 上传接口存在“HTTP 200 但业务失败”的情况，这里统一补提示，避免页面静默无反馈。
  if (res?.code && res.code !== '200') {
    ElMessage.error(res.message || '图片上传失败，请稍后重试')
    return
  }
  emit('success', res)
}
</script>

<style scoped lang="scss">
.upload-tip {
  font-size: 12px;
  color: var(--color-medium);
  // margin-left: 8px;
}
</style>
