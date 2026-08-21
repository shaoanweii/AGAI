import request from '../api/index'
import type { Options, Table, Form, Selection } from './table.d'
import { ElMessage } from 'element-plus'
// Element Plus 表格类型定义
interface TableRowSelection {
  type?: string
  selectedRowKeys?: string[]
  showCheckedAll?: boolean
  onlyCurrent?: boolean
  onChange?: (selectedRowKeys: string[], selectedRows: any[]) => void
}

interface TableSortable {
  sorter?: boolean
  sortDirections?: string[]
}

// 通用表格数据类型
interface TableData {
  [key: string]: any
}

/**
 *
 * @param option
 * @param dataCallBackByQueryApi 在外部处理查询数据接口返回的数据
 */
export function useTable(option?: Options, dataCallBackByQueryApi?: (params?: any) => any) {
  const defaultPageSize = option?.pageSize ?? 20

  const table = <Table>reactive({
    filter: {},
    selection: [],
    loading: false,
    list: [],
    total: 1,
    pageNum: 1,
    pageSize: defaultPageSize
  })

  const rowSelection = reactive<TableRowSelection>({
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false
  })

  const form = <Form>reactive({
    visible: false,
    operation: 'add', // edit view
    data: {}
  })

  const getAllSelection = () => {
    request<Selection[]>({
      url: option?.selectionUrl
    }).then(res => {
      table.selection = res.result
      if (getSlection('province')?.length && !window.localStorage.getItem('provinceOptions')) {
        const provinceOptions = getSlection('province')
        window.localStorage.setItem('provinceOptions', JSON.stringify(provinceOptions))
      }
      if (getSlection('status')?.length && !window.localStorage.getItem('statusOptions')) {
        const statusOptions = getSlection('status')
        window.localStorage.setItem('statusOptions', JSON.stringify(statusOptions))
      }
      if (getSlection('ruleType')?.length && !window.localStorage.getItem('ruleTypeOptions')) {
        const ruleTypeOptions = getSlection('ruleType')
        window.localStorage.setItem('ruleTypeOptions', JSON.stringify(ruleTypeOptions))
      }
      if (getSlection('client')?.length && !window.localStorage.getItem('clientOptions')) {
        const clientOptions = getSlection('client')
        window.localStorage.setItem('clientOptions', JSON.stringify(clientOptions))
      }
      if (getSlection('customer')?.length && !window.localStorage.getItem('customerOptions')) {
        const customerOptions = getSlection('customer')
        window.localStorage.setItem('customerOptions', JSON.stringify(customerOptions))
      }
      if (
        getSlection('accountType')?.length &&
        !window.localStorage.getItem('accountTypeOptions')
      ) {
        const accountTypeOptions = getSlection('accountType')
        window.localStorage.setItem('accountTypeOptions', JSON.stringify(accountTypeOptions))
      }
      if (getSlection('clientCode')?.length && !window.localStorage.getItem('clientCodeOptions')) {
        const clientCodeOptions = getSlection('clientCode')
        window.localStorage.setItem('clientCodeOptions', JSON.stringify(clientCodeOptions))
      }
      if (getSlection('tag_app')?.length && !window.localStorage.getItem('tagAppOptions')) {
        const tagAppOptions = getSlection('tag_app')
        window.localStorage.setItem('tagAppOptions', JSON.stringify(tagAppOptions))
      }
      if (getSlection('channel')?.length && !window.localStorage.getItem('channelOptions')) {
        const channelOptions = getSlection('channel')
        window.localStorage.setItem('channelOptions', JSON.stringify(channelOptions))
      }
      if (
        getSlection('databaseType')?.length &&
        !window.localStorage.getItem('databaseTypeOptions')
      ) {
        const databaseTypeOptions = getSlection('databaseType')
        window.localStorage.setItem('databaseTypeOptions', JSON.stringify(databaseTypeOptions))
      }
    })
  }

  const getSlection = (key: string, list?: any[]) => {
    const arry = list || table.selection || []
    const selectionItem = arry?.find(item => item.key == key)
    return selectionItem?.details
  }

  //请求接口前是否清空table.list
  const getTableData = <Item>(isListClear: boolean = true) => {
    table.loading = true
    isListClear && (table.list = [])
    const pageKey = option?.pageKey || 'pageNum'
    const pageSizeKey = option?.pageSizeKey || 'pageSize'
    const params = {
      ...table.filter,
      [pageKey]: table.pageNum,
      [pageSizeKey]: table.pageSize
    }
    const method = option?.method || 'GET'
    return request<Table<Item>>({
      url: option?.url,
      method,
      timeout: option?.timeout,
      headers: option?.headers,
      params: method == 'GET' ? params : undefined,
      data: method == 'POST' ? params : undefined
    })
      .then(res => {
        let data = res.result
        dataCallBackByQueryApi && (data = dataCallBackByQueryApi(res))
        table.loading = false
        // 接口异常或 Demo 适配器返回不同分页结构时，仍保持表格和分页器的强类型输入。
        const normalizedList = data?.list ?? data?.records
        table.list = Array.isArray(normalizedList) ? normalizedList : []
        const normalizedTotal = Number(data?.total)
        table.total = Number.isFinite(normalizedTotal) ? normalizedTotal : table.list.length
        return data
      })
      .catch(err => {
        table.list = []
        table.total = 0
        table.loading = false
        // ElMessage.error(err.message)
        return {}
      })
  }

  const handleSortChange = (index: string, direction: string) => {
    if (direction) {
      const sortStr = direction === 'ascend' ? 'asc' : 'desc'
      table.filter['order'] = `${index} ${sortStr}`
    } else {
      delete table.filter['order']
    }
    getTableData()
  }
  //重置筛选项
  const handleReset = (setDefault?: any) => {
    if (option?.notResetKey && Array.isArray(option.notResetKey)) {
      Object.keys(table.filter).forEach(key => {
        if (!option.notResetKey?.includes(key)) {
          delete table.filter[key]
        }
      })
    } else {
      table.filter = {}
    }

    table.pageNum = 1
    typeof setDefault === 'function' && setDefault()
    getTableData()
  }
  // 当前页码改变
  const handleCurrentChange = (page: number) => {
    table.loading = true
    table.pageNum = page
    getTableData()
  }
  // 每页数量改变
  const handleSizeChange = (size: number) => {
    table.loading = true
    table.pageSize = size
    table.pageNum = 1
    getTableData()
  }

  const handleAdd = (defaultFormData: Record<string, any> = {}) => {
    const normalizedFormData = defaultFormData instanceof Event ? {} : { ...defaultFormData }
    form.operation = 'add'
    form.data = normalizedFormData
    form.visible = true
  }
  const handleEdit = (item: object) => {
    form.data = JSON.parse(JSON.stringify(item))
    form.visible = true
    form.operation = 'edit'
  }
  const handleView = (item: object) => {
    form.data = JSON.parse(JSON.stringify(item))
    form.visible = true
    form.operation = 'view'
  }

  const handleDelete = (params: object) => {
    table.loading = true
    // let params = {id: key}
    const key = Object.keys(params)
    const value = Object.values(params)
    const method = option?.method || 'GET'
    request<Table>({
      url: option?.deleteUrl,
      method,
      params: method == 'GET' ? params : undefined,
      data: method == 'POST' ? params : undefined
    })
      .then(res => {
        table.loading = false
        ElMessage.success(res.message)
        if (res.code === '500' && res.message === '当前规则为启用状态，无法删除') {
          return
        } else {
          // table.list = table.list.filter(item => item[key[0]] != value[0])
          // table.total = table.total - 1
          getFirstPageTableData()
        }
      })
      .catch((err: any) => {
        ElMessage.error(err.message)
      })
      .finally(() => {
        table.loading = false
      })
  }

  const getFirstPageTableData = async () => {
    table.pageNum = 1
    await getTableData()
  }

  const refreshTableData = async (isListClear: boolean = true) => {
    const data: any = await getTableData(isListClear)
    const total = Number(data?.total ?? table.total ?? 0)
    if (!Number.isFinite(total) || total <= 0) return

    const lastPage = Math.max(1, Math.ceil(total / table.pageSize))
    if (table.pageNum <= lastPage) return

    table.pageNum = lastPage
    await getTableData()
  }

  const sortOpts: TableSortable = {
    sortDirections: ['ascend', 'descend'],
    sorter: true
  }

  return {
    table,
    form,
    sortOpts,
    rowSelection,
    getAllSelection,
    getSlection,
    getTableData,
    handleReset,
    handleSizeChange,
    handleCurrentChange,
    handleSortChange,
    handleAdd,
    handleEdit,
    handleView,
    handleDelete,
    getFirstPageTableData,
    refreshTableData
  }
}
