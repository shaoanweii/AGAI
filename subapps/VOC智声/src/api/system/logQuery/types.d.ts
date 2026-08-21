/**
 * 日志查询模块类型定义
 */

/**
 * 日志查询参数
 *
 * 说明：
 * - 接口：/report/operationLog/findVocLogList（POST）
 * - startTime/endTime：接口声明为 string(date)，页面侧按 YYYY-MM-DD 传参
 * - deptId：二/三级部门ID，多选
 * - searchKey：员工姓名或工号的模糊搜索关键字
 */
export interface LogQueryParams {
  /** 页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
  /** 开始时间（YYYY-MM-DD） */
  startTime?: string
  /** 结束时间（YYYY-MM-DD） */
  endTime?: string
  /**
   * 访问应用
   * - 常见：PC端/移动端（具体值以服务端为准）
   */
  accessApp?: string
  /** 访问菜单 */
  accessMenu?: string
  /** 部门ID列表（二/三级部门），多选 */
  deptId?: string[]
  /** 搜索关键字（员工姓名/工号，模糊查询） */
  searchKey?: string
}

/**
 * 操作/访问日志列表项
 */
export interface LogQueryItem {
  /** 记录ID */
  id?: string
  /** 员工姓名 */
  employeeName?: string
  /** 员工工号 */
  employeeNo?: string
  /** 二级部门 */
  secondLevelDept?: string
  /** 三级部门 */
  thirdLevelDept?: string
  /** 访问应用 */
  accessApp?: string
  /** 访问菜单 */
  accessMenu?: string
  /** 开始访问时间 */
  startTime?: string
  /** 结束访问时间 */
  endTime?: string
  /** IP地址 */
  ipAddress?: string
}

/**
 * 后端常见分页结构：records + total
 */
export interface IPageLogQueryItem {
  /** 每页条数 */
  size: number
  /** 当前页码 */
  current: number
  /** 列表数据 */
  records: LogQueryItem[]
  /** 总条数 */
  total: number
  /** 总页数 */
  pages: number
}
