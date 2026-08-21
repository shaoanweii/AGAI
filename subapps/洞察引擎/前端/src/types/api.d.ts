declare namespace Api {
  namespace Common {
    /**
     * enable status
     *
     * - "1": enabled
     * - "0": disabled
     */
    type EnableStatus = '0' | '1'

    type CommonRecord<T = any> = {
      id?: string
      clientId: string
    } & T
    interface Params {
      id?: string
      clientId?: string
      checkAdmin?: boolean
    }
  }

  namespace User {
    interface LoginReq {
      username: string
      password: string
      checkKey: string
      captcha: string
    }
  }

  namespace Role {
    type RoleParams = Common.CommonRecord<{
      enabled: string
      roleName: string
      permissionIdList: string[] | undefined
    }>
    type QueryInfoById = Pick<RoleParams, 'id' | 'clientId'>
    type QueryInfoByIdRecord = RoleParams & {
      roleAuthTreeList: PermissionTree[]
    }
    type ClientId = Pick<RoleParams, 'clientId'>
    type QueryMenu = Partial<ClientId & { selectAll?: boolean }>

    interface PermissionTree {
      code: string | null
      name: string
      // 选中状态
      checked: boolean
      // 当前节点是否是按钮   true 按钮
      checkButton: boolean
      // 半选状态
      indeterminate?: boolean
      // 展开状态
      expand?: boolean
      icon?: string
      path?: string
      pid: string
      id: string
      children?: PermissionTree[] | null
    }
  }

  namespace InsDataSource {
    interface params {
      clientId: string
      dataSourceId: string
      batchId?: string
      dataName?: string
    }

    interface ExportRawParams extends params {
      //  0 无效数据
      dataValidity?: string
      // -1 失败数据
      status?: string
    }
  }

  namespace BrandSeries {
    /**
     * AGAI 智擎通用响应结构
     */
    type ApiResponse<T> = {
      success: boolean
      message: string
      code: string
      result: T
      tid?: string
    }

    /**
     * 通用分页请求参数
     */
    interface PageQuery {
      pageNum?: number
      pageSize?: number
      order?: string
    }

    /**
     * 下拉选项节点（支持树形 children）
     */
    interface OptionItem {
      key: string
      value: string
      code?: string
      children?: OptionItem[]
      [props: string]: any
    }

    /**
     * 过滤条件分组（兼容 useConditions）
     */
    interface ConditionGroup {
      key: string
      details: OptionItem[]
    }

    /**
     * 批量启用/禁用载荷
     */
    interface BatchStatusPayload {
      ids: string[]
      status: '0' | '1'
    }

    /**
     * 标签树/渠道树查询参数
     */
    interface TreeQuery {
      clientId?: string
      level?: number
      tagLibType?: string
    }

    /**
     * 三个模块共用的基础实体字段
     */
    interface BaseEntity {
      id?: string
      name?: string
      nameEn?: string
      img?: string
      isCore?: number
      isCoreName?: string
      competitiveType?: number
      competitiveTypeName?: string
      status?: string
      statusName?: string
      operator?: string
      createTime?: string
      updateTime?: string
    }

    /**
     * 轻量关联对象（本竞品关系等）
     */
    interface SimpleRef {
      id?: string
      name?: string
      code?: string
    }

    /**
     * 统一分页结果，便于表格复用
     */
    interface PageResult<T> {
      list: T[]
      total: number
    }

    /**
     * 车企（automark）请求/返回模型
     */
    interface Automaker extends BaseEntity, PageQuery {
      ids?: string[]
      competitiveProduct?: SimpleRef[]
    }

    /**
     * 品牌（brandInfo）请求/返回模型
     */
    interface Brand extends BaseEntity, PageQuery {
      code?: string
      automark?: string
      automarkId?: string
      alias?: string
      exclusionWords?: string
      automarkList?: string[]
      brandFilters?: string[]
      notIdFilter?: string
      nameFilter?: string
      ids?: string[]
      orderBy?: number
      competitiveProduct?: SimpleRef[]
    }

    /**
     * 车系（carSeriesInfo）请求/返回模型
     */
    interface Series extends BaseEntity, PageQuery {
      code?: string
      codes?: string[]
      brandId?: string
      brandCode?: string
      brandCodes?: string[]
      alias?: string
      exclusionWords?: string
      isNewCar?: string | number
      preheatStartTime?: string
      preheatEndTime?: string
      launchStartTime?: string
      launchEndTime?: string
      stableStartTime?: string
      stableEndTime?: string
      startTime?: string
      endTime?: string
      ids?: string[]
      orderBy?: number
      competitiveProduct?: SimpleRef[]
    }

    /**
     * 品牌-车系联动查询返回项
     */
    interface BrandAndSeries {
      brandId?: string
      brandCode?: string
      brandName?: string
      brandAlias?: string
      brandExclusionWords?: string
      carSeriesCode?: string
      carSeriesName?: string
      carSeriesAlias?: string
      carSeriesExclusionWords?: string
      carLevel1?: string
      carLevel2?: string
      energyType1?: string
      energyType2?: string
    }
  }

  namespace CarUsageScenarios {
    /**
     * 用车场景分类树查询参数
     */
    interface CategoryListQuery {
      categoryName?: string
      pageNum?: number
      pageSize?: number
    }

    /**
     * 用车场景分类树节点
     */
    interface CategoryNode {
      id?: string
      patentId?: string
      children?: CategoryNode[]
      nodeType?: string
      categoryName?: string
      categoryDescription?: string
      synonyms?: string
      sceneName?: string
      sceneDescription?: string
      categoryId?: string
      typeName?: string
      level?: number
      leafCount?: number
      status?: Api.Common.EnableStatus
      creator?: string
      createTime?: string
      updateTime?: string
    }

    /**
     * 新增或编辑分类载荷
     */
    interface SaveCategoryPayload {
      id?: string
      patentId?: string
      categoryName?: string
      categoryDescription?: string
      level?: number
      synonyms?: string
      status?: Api.Common.EnableStatus
    }

    /**
     * 删除分类载荷
     */
    interface DeleteCategoryPayload {
      id?: string
      patentId?: string
    }

    /**
     * 用车场景分页查询参数
     */
    interface SceneListQuery {
      pageNum?: number
      pageSize?: number
      order?: string
      id?: string
      sceneName?: string
      sceneDescription?: string
      categoryId?: string
      categoryIds?: string[]
      synonyms?: string
      operator?: string
      status?: Api.Common.EnableStatus
    }

    /**
     * 用车场景操作人下拉项
     */
    interface SceneOperatorOption {
      id?: string
      userName?: string
    }

    /**
     * 用车场景实体
     */
    interface SceneRecord {
      id?: string
      sceneName?: string
      sceneDescription?: string
      categoryId?: string
      categoryName?: string
      synonyms?: string
      status?: Api.Common.EnableStatus
      statusName?: string
      operator?: string
      createTime?: string
      updateTime?: string
    }

    /**
     * 用车场景分页结果
     */
    interface ScenePageResult {
      size?: number
      current?: number
      records?: SceneRecord[]
      pages?: number
      total?: number
    }

    /**
     * 新增或编辑用车场景载荷
     */
    interface SaveScenePayload {
      id?: string
      sceneName?: string
      sceneDescription?: string
      categoryId?: string
      categoryIds?: string[]
      synonyms?: string
      status?: Api.Common.EnableStatus
    }

    /**
     * 批量移动用车场景载荷
     */
    interface BatchMoveScenePayload {
      categoryId?: string
      ids?: string[]
    }

    /**
     * 批量修改用车场景状态载荷
     */
    interface BatchChangeSceneStatusPayload {
      ids?: string[]
      status?: Api.Common.EnableStatus
    }
  }

  namespace AttributeLabel {
    /**
     * 属性标签分页查询参数
     */
    interface QueryParams {
      pageSize?: number
      pageNum?: number
      order?: string
      id?: string
      name?: string
      status?: string
      createUser?: string
      updateUser?: string
      createTime?: string
      updateTime?: string
      ids?: string[]
    }

    /**
     * 属性标签列表记录
     */
    interface RecordItem {
      id?: string
      name?: string
      status?: string
      statusName?: string
      createUser?: string
      updateUser?: string
      createTime?: string
      updateTime?: string
    }

    /**
     * 属性标签保存参数
     */
    interface SavePayload {
      id?: string
      name?: string
      status?: Api.Common.EnableStatus
    }

    /**
     * 属性标签全量列表结果
     */
    type AllListResult = RecordItem[]

    /**
     * 属性标签批量修改状态参数
     */
    interface BatchChangeStatusPayload {
      status?: Api.Common.EnableStatus
      ids?: string[]
    }

    /**
     * 属性标签分页结果
     */
    interface PageResult {
      size?: number
      current?: number
      records?: RecordItem[]
      total?: number
      pages?: number
    }
  }
}
