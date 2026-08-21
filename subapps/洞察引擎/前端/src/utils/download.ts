/**
 * 文件下载相关工具函数
 */

/**
 * 基于 Blob 触发浏览器下载
 * @param blob 文件二进制数据
 * @param filename 下载文件名
 */
export const downloadFromBlob = (blob: Blob, filename: string): void => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 获取文件扩展名（不包含 "."）
 * @param pathOrUrl 文件路径或 URL
 */
const getExtension = (pathOrUrl: string): string => {
  const extensionRegex = /\.([a-zA-Z0-9]+)(?:\?|#|$)/

  try {
    const url = new URL(pathOrUrl)
    const decodedPath = decodeURIComponent(url.pathname)
    const match = decodedPath.match(extensionRegex)
    return match?.[1] || ''
  } catch {
    let decodedPath = pathOrUrl
    try {
      decodedPath = decodeURIComponent(pathOrUrl)
    } catch {
      decodedPath = pathOrUrl
    }
    const match = decodedPath.match(extensionRegex)
    return match?.[1] || ''
  }
}

/**
 * 获取文件名（若缺失扩展名则根据路径补齐）
 * @param fileName 原始文件名
 * @param filePath 文件路径或 URL
 */
export const getFileNameWithExtension = (fileName?: string, filePath?: string): string => {
  let filename = fileName?.trim() || `download_${Date.now()}`
  const hasExtension = /\.([a-zA-Z0-9]+)$/.test(filename)

  if (hasExtension) return filename

  const extension = filePath ? getExtension(filePath) : ''
  filename = extension ? `${filename}.${extension}` : `${filename}.xlsx`

  return filename
}
