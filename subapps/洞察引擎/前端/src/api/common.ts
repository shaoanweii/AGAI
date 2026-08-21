import request from './index'

/**
 *  下载文件
 * @param data
 * @returns
 */
export const downloadFile = (data: { id: string }) => {
  return request<any>({
    url: `/insights/downLoad/downloadFile`,
    method: 'post',
    data,
    responseType: 'blob'
  })
}
