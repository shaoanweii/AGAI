import { ref, computed, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { getEmployeeList } from '@/api/rules'
import { debounce } from 'lodash-es'

// 中文注释：将后端返回的账号对象统一成前端用的标准结构
function normalizeUser(u: any) {
  // 统一人员结构：id/employeeId/name
  // - id     ← userid | userId | id | accountId
  // - name   ← accountName | name | userName | nickName
  // - 工号   ← employeeId | empNo
  return {
    id: u?.userId ?? '',
    name: u?.userName ?? u?.accountName ?? '',
    employeeId: u?.employeeId ?? ''
  }
}

interface Options {
  ttl?: number // 缓存有效期，毫秒
  minLength?: number // 触发远程搜索的最小关键字长度
  debounceMs?: number // 防抖毫秒
}

export function useEmployeesByDept(opts: Options = {}) {
  const ttl = opts.ttl ?? 60_000
  const minLength = opts.minLength ?? 1
  const debounceMs = opts.debounceMs ?? 300

  const deptId = ref<string>('')
  const loading = ref(false)
  const error = ref<Error | null>(null)
  const list = ref<any[]>([])
  const keyword = ref('')

  // 缓存：按部门维度缓存完整列表，避免频繁请求
  const cache = new Map<string, { ts: number; data: any[] }>()
  let lastKeyword = ''

  const setDeptId = (id?: string) => {
    deptId.value = id || ''
    // 切换部门时清空当前展示与关键字，但保留缓存
    list.value = []
    keyword.value = ''
    error.value = null
  }

  const filterLocal = (source: any[], kw: string) => {
    const q = (kw || '').trim().toLowerCase()
    if (!q) return source
    return source.filter(u => {
      const name = String(u.name || '').toLowerCase()
      const emp = String(u.employeeId || '').toLowerCase()
      return name.includes(q) || emp.includes(q)
    })
  }

  const fetchBaseList = async (id: string) => {
    // 命中有效缓存
    const hit = cache.get(id)
    const now = Date.now()
    if (hit && now - hit.ts < ttl) return hit.data

    const resp: any = await getEmployeeList(id)
    const arr = resp?.result || []
    const norm = arr.map(normalizeUser)
    cache.set(id, { ts: now, data: norm })
    return norm
  }

  const doSearch = async (kw: string) => {
    const normalizedKeyword = String(kw || '').trim()
    keyword.value = normalizedKeyword
    lastKeyword = normalizedKeyword
    error.value = null
    list.value = []

    const id = deptId.value
    if (!id) {
      // 未选部门时提示，并不发请求
      ElMessage.warning('请先选择部门')
      return
    }

    // 仅对用户主动输入的关键字生效，空关键字仍允许查询全量部门人员，便于初始化回显。
    if (normalizedKeyword && normalizedKeyword.length < minLength) {
      return
    }

    try {
      loading.value = true
      const base = await fetchBaseList(id)
      list.value = filterLocal(base, normalizedKeyword)
    } catch (e: any) {
      error.value = e
      list.value = []
    } finally {
      loading.value = false
    }
  }

  const debouncedSearch = debounce((q: string) => {
    // 保留 this 上下文无关，直接调用即可
    // 由于 el-select remote-method 直接传 q，这里只包装防抖
    void doSearch(q)
  }, debounceMs)

  // 组件卸载时清掉待执行的搜索，避免异步结果回写已销毁的页面状态。
  onBeforeUnmount(() => {
    debouncedSearch.cancel()
  })

  const remoteSearch = (q: string) => {
    const normalizedKeyword = String(q || '').trim()
    if (!normalizedKeyword) {
      return doSearch('')
    }

    debouncedSearch(normalizedKeyword)
  }

  const retry = () => {
    return doSearch(lastKeyword)
  }

  const emptyText = computed(() => {
    if (error.value) return '加载失败，点击重试'
    if (!keyword.value || list.value.length > 0) return ''
    return '无匹配人员'
  })

  return {
    // state
    deptId,
    list,
    loading,
    error,
    keyword,
    emptyText,
    // actions
    setDeptId,
    remoteSearch,
    searchNow: doSearch,
    retry
  }
}
