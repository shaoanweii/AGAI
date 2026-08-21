import { ElMessage } from 'element-plus'
import { h, onMounted, ref, unref, watch } from 'vue'
import type { Ref } from 'vue'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'

export type DlViewType = 'ADD' | 'EDIT' | 'DETAIL'
export interface ViewEntity {
  type: DlViewType
  visible?: boolean
  record?: any
  [key: string]: any
}

interface UseTableConfig<T> {
  /**
   * 是否初始化的时候请求一次
   */
  immediate?: boolean
  initialFormData?: T
  fetchDataApi: () => Promise<{
    list: any[]
    total?: number
  }>
  fetchDelApi?: (record?: any) => Promise<boolean>
  // 重置之前处理数据
  resetBefore?: () => void
}

interface UseTableMethods<T> {
  /** 获取列表数据并同步分页状态 */
  getList: () => Promise<void>
  /** 重新请求当前查询条件下的列表 */
  refresh: () => void
  /** 打开新增视图 */
  handleAdd: (cb?: () => void) => void
  /** 打开编辑视图并写入当前行 */
  handleEdit: (row: any, cb?: (record: any) => void) => void
  /** 打开详情视图并写入当前行 */
  handleDetail: (row: any, cb?: (record: any) => void) => void
  /** 删除当前记录后按分页边界刷新列表 */
  hadnleDel: (record: any, idsLength?: number) => Promise<void>
  /** 按当前条件查询列表 */
  handleQuery: () => void
  /** 重置查询条件后重新查询列表 */
  handleReset: () => void
  /** 同步表格选择项 */
  handleSelectionChange: (selected: T[]) => void
}

interface UseTableReturn<T> {
  tableMethods: UseTableMethods<T>
  tableState: {
    currentPage: Ref<number>
    pageSize: Ref<number>
    total: Ref<number>
    dataList: Ref<any[]>
    loading: Ref<boolean>
    viewVisible: Ref<boolean>
    viewType: Ref<DlViewType | undefined>
    record: Ref<any>
  }
  viewEntity: ViewEntity
  formData: Ref<T>
  selection: Ref<T[]>
}

export const useTable = <T = any>(config: UseTableConfig<T>): UseTableReturn<T> => {
  const { immediate = true, initialFormData } = config

  const loading = ref(false)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const dataList = ref<any[]>([])
  const record = ref<any>()
  const viewType = ref<DlViewType>()
  const viewVisible = ref(false)
  const selection = ref<T[]>([]) as Ref<T[]>
  const formData = ref<T>({ ...initialFormData } as T) as Ref<T>

  const viewEntity = ref<ViewEntity>({
    type: 'ADD',
    visible: false,
    record: undefined
  })
  let isPageSizeChange = false

  watch(
    () => currentPage.value,
    () => {
      if (!isPageSizeChange) methods.getList()
      isPageSizeChange = false
    }
  )

  watch(
    () => pageSize.value,
    () => {
      if (unref(currentPage) === 1) {
        methods.getList()
      } else {
        currentPage.value = 1
        isPageSizeChange = true
        methods.getList()
      }
    }
  )

  onMounted(() => {
    if (immediate) {
      methods.getList()
    }
  })

  const methods: UseTableMethods<T> = {
    /**
     * 获取表单数据
     */
    getList: async () => {
      loading.value = true
      try {
        const res = await config?.fetchDataApi()
        if (res) {
          dataList.value = res.list
          total.value = res.total || 0
        }
      } catch (errs) {
        dataList.value = []
        console.log('fetchDataApi error', errs)
      } finally {
        loading.value = false
      }
    },

    refresh: () => {
      methods.getList()
    },

    handleAdd: (cb?: () => void) => {
      viewEntity.value.visible = true
      viewEntity.value.type = 'ADD'
      viewEntity.value.record = undefined // 清除之前的记录数据
      cb?.()
    },

    handleEdit: (row: any, cb?: (record: any) => void) => {
      viewEntity.value.visible = true
      viewEntity.value.type = 'EDIT'
      viewEntity.value.record = row
      cb?.(row)
    },

    handleDetail: (row: any, cb?: (record: any) => void) => {
      viewEntity.value.visible = true
      viewEntity.value.type = 'DETAIL'
      viewEntity.value.record = row
      cb?.(row)
    },

    hadnleDel: async (record: any, idsLength?: number) => {
      const { fetchDelApi } = config
      if (!fetchDelApi) {
        console.warn('fetchDelApi is undefined')
        return
      }
      try {
        await appDialogConfirm(
          () =>
            h('div', { class: 'flex items-center' }, [
              h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
              h('span', { class: 'ml-8' }, '是否删除所选中数据？')
            ]),
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消'
          }
        )
      } catch {
        return
      }

      const res = await fetchDelApi(record)
      if (res) {
        ElMessage.success('成功')

        // 计算出临界点
        const current =
          unref(total) % unref(pageSize) === idsLength || unref(pageSize) === 1
            ? unref(currentPage) > 1
              ? unref(currentPage) - 1
              : unref(currentPage)
            : unref(currentPage)

        currentPage.value = current
        methods.getList()
      }
    },

    handleQuery: () => {
      methods.getList()
    },

    handleReset: () => {
      config.resetBefore?.()
      formData.value = Object.assign(formData.value as object, initialFormData ?? {}) as T

      methods.handleQuery()
    },
    handleSelectionChange: (selected: any[]) => {
      selection.value = selected
    }
  }

  return {
    tableMethods: methods,
    tableState: {
      currentPage,
      pageSize,
      total,
      dataList,
      loading,
      viewVisible,
      viewType,
      record
    },
    viewEntity: viewEntity.value,
    formData,
    selection
  }
}
