/**
 * 文件下载工具函数
 */

const isBrowser = typeof window !== 'undefined' && typeof document !== 'undefined'
const DOWNLOAD_URL_REVOKE_DELAY = 1000

/**
 * 从 Blob 数据下载文件
 * @param blob Blob 数据
 * @param filename 文件名
 */
export const downloadFromBlob = (blob: Blob, filename: string): void => {
  if (!isBrowser) {
    return
  }

  // 创建下载链接
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename

  // 触发下载
  document.body.appendChild(link)
  link.click()

  // 延后清理，避免浏览器尚未消费 blob URL 时被提前释放。
  window.setTimeout(() => {
    link.remove()
    window.URL.revokeObjectURL(url)
  }, DOWNLOAD_URL_REVOKE_DELAY)
}

/**
 * 从响应头中提取文件名
 * @param response HTTP 响应对象
 * @returns 文件名
 */
export const getFilenameFromResponse = (response: any): string => {
  const contentDisposition =
    response.headers?.['content-disposition'] || response.headers?.['Content-Disposition']

  if (contentDisposition) {
    // 尝试匹配 filename*=UTF-8''filename 格式
    const utf8Match = contentDisposition.match(/filename\*=UTF-8''(.+)/)
    if (utf8Match) {
      return decodeURIComponent(utf8Match[1])
    }

    // 尝试匹配 filename="filename" 格式
    const filenameMatch = contentDisposition.match(/filename="(.+)"/)
    if (filenameMatch) {
      return filenameMatch[1]
    }

    // 尝试匹配 filename=filename 格式
    const simpleMatch = contentDisposition.match(/filename=([^;]+)/)
    if (simpleMatch) {
      return simpleMatch[1].trim()
    }
  }

  // 默认文件名
  return `download_${Date.now()}`
}

/**
 * 处理文件下载响应
 * @param response BaseResponse<Blob> 响应对象
 * @param defaultFilename 默认文件名
 */
export const handleDownloadResponse = (
  response: BaseResponse<Blob>,
  defaultFilename?: string
): void => {
  if (response.success && response.result instanceof Blob) {
    const filename = defaultFilename || `download_${Date.now()}`
    downloadFromBlob(response.result, filename)
  } else {
    console.error('下载失败:', response.message)
    throw new Error(response.message || '下载失败')
  }
}

/**
 * 根据文件类型获取 MIME 类型
 * @param filename 文件名
 * @returns MIME 类型
 */
export const getMimeType = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase()

  const mimeTypes: Record<string, string> = {
    // 文档类型
    pdf: 'application/pdf',
    doc: 'application/msword',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    xls: 'application/vnd.ms-excel',
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    ppt: 'application/vnd.ms-powerpoint',
    pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',

    // 图片类型
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    png: 'image/png',
    gif: 'image/gif',
    svg: 'image/svg+xml',
    webp: 'image/webp',

    // 文本类型
    txt: 'text/plain',
    csv: 'text/csv',
    json: 'application/json',
    xml: 'application/xml',

    // 压缩文件
    zip: 'application/zip',
    rar: 'application/x-rar-compressed',
    '7z': 'application/x-7z-compressed',

    // 其他
    mp4: 'video/mp4',
    mp3: 'audio/mpeg',
    wav: 'audio/wav'
  }

  return mimeTypes[ext || ''] || 'application/octet-stream'
}

/**
 * 验证文件大小
 * @param blob Blob 对象
 * @param maxSizeMB 最大文件大小（MB）
 * @returns 是否通过验证
 */
export const validateFileSize = (blob: Blob, maxSizeMB: number = 100): boolean => {
  const maxSizeBytes = maxSizeMB * 1024 * 1024
  return blob.size <= maxSizeBytes
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的文件大小字符串
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 创建并下载文本文件
 * @param content 文件内容
 * @param filename 文件名
 * @param mimeType MIME 类型
 */
export const downloadTextFile = (
  content: string,
  filename: string,
  mimeType: string = 'text/plain'
): void => {
  const blob = new Blob([content], { type: mimeType })
  downloadFromBlob(blob, filename)
}

/**
 * 创建并下载 JSON 文件
 * @param data 要下载的数据对象
 * @param filename 文件名
 */
export const downloadJsonFile = (data: any, filename: string): void => {
  const content = JSON.stringify(data, null, 2)
  downloadTextFile(content, filename, 'application/json')
}

/**
 * 创建并下载 CSV 文件
 * @param data 二维数组数据
 * @param filename 文件名
 */
export const downloadCsvFile = (data: string[][], filename: string): void => {
  const csvContent = data
    .map(row => row.map(cell => `"${cell.toString().replace(/"/g, '""')}"`).join(','))
    .join('\n')

  // 添加 BOM 以支持中文
  const bom = '\uFEFF'
  downloadTextFile(bom + csvContent, filename, 'text/csv')
}

/**
 * 获取文件名（确保包含扩展名）
 * @param fileName 文件名（可能不包含扩展名）
 * @param filePath 文件路径（URL 或相对路径）
 * @returns 包含扩展名的完整文件名
 */
export const getFileNameWithExtension = (fileName?: string, filePath?: string): string => {
  let filename = fileName || `download_${Date.now()}`

  // 检查文件名是否已经有扩展名（支持常见的文件扩展名）
  const hasExtension = /\.([a-zA-Z0-9]+)$/.test(filename)

  if (!hasExtension && filePath) {
    // 从 filePath URL 中提取扩展名
    try {
      const url = new URL(filePath)
      // 获取 pathname，并解码 URL 编码
      const pathname = decodeURIComponent(url.pathname)
      // 匹配扩展名（在查询参数之前）
      const extensionMatch = pathname.match(/\.([a-zA-Z0-9]+)(?:\?|$|#)/)
      if (extensionMatch && extensionMatch[1]) {
        filename = `${filename}.${extensionMatch[1]}`
      } else {
        // 如果 pathname 中没有，尝试从原始路径中提取（处理 URL 编码的情况）
        const rawExtensionMatch = url.pathname.match(/\.([a-zA-Z0-9]+)(?:\?|$|#)/)
        if (rawExtensionMatch && rawExtensionMatch[1]) {
          filename = `${filename}.${rawExtensionMatch[1]}`
        } else {
          // 默认使用 xlsx（根据实际业务需求调整）
          filename = `${filename}.xlsx`
        }
      }
    } catch {
      // 如果不是有效的 URL，尝试从路径字符串中直接提取
      // 先尝试解码 URL 编码
      let decodedPath = filePath
      try {
        decodedPath = decodeURIComponent(filePath)
      } catch {
        // 如果解码失败，使用原始路径
        decodedPath = filePath
      }

      const extensionMatch = decodedPath.match(/\.([a-zA-Z0-9]+)(?:\?|$|#)/)
      if (extensionMatch && extensionMatch[1]) {
        filename = `${filename}.${extensionMatch[1]}`
      } else {
        // 默认使用 xlsx
        filename = `${filename}.xlsx`
      }
    }
  }

  return filename
}
