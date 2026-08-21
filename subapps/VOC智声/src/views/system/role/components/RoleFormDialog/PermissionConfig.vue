<template>
  <el-form
    ref="formRef"
    :model="localFormData"
    :rules="formRules"
    style="height: calc(96vh - 230px)"
    class="pt-24 pl-16 pr-16 flex-auto overflow-auto"
  >
    <template v-if="basicInfoData?.roleType === '1'">
      <!-- 数据范围 -->
      <div>
        <el-form-item label="数据来源" prop="channelIds">
          <el-cascader
            v-model="localFormData.channelIds"
            :options="channelOptions"
            clearable
            collapse-tags
            :max-collapse-tags="1"
            :show-all-levels="false"
            filterable
            :props="{
              value: 'code',
              label: 'name',
              children: 'child',
              multiple: true,
              emitPath: false,
              checkStrictly: false
            }"
            placeholder="请选择数据来源"
            class="w-full"
          />
        </el-form-item>
      </div>

      <!-- 功能模块-PC -->
      <div class="flex-y-center mb-16">
        <span class="fs-14 sub-title lh-32">PC端</span>
      </div>

      <!-- 将 checkbox 和 AdvancedFilter 分离,避免 Element Plus bug -->
      <template v-for="route in newAppKanBan.report" :key="route.id">
        <div class="router-wrapper">
          <div class="one-level-wrapper">
            <!-- 有二级菜单 -->
            <template v-if="route.children">
              <el-checkbox
                :indeterminate="getParentCheckboxState(route).indeterminate"
                :model-value="getParentCheckboxState(route).checked"
                @change="(val: CheckboxValueType) => handleParentChange(route, val)"
              >
                {{ route.name }}
              </el-checkbox>
            </template>
            <!-- 只有一级菜单 -->
            <template v-else>
              <div class="w-full flex-between items-center pr-8">
                <el-checkbox-group v-model="localFormData.reportPermissions">
                  <el-checkbox
                    :value="route.id"
                    :disabled="route.id === OVERVIEWID"
                    @change="(val: CheckboxValueType) => handleChildChange(route, route.id, val)"
                  >
                    {{ route.name }}
                  </el-checkbox>
                </el-checkbox-group>

                <AdvancedFilter
                  :childKey="route.id"
                  v-if="route.requireFilterTypes"
                  :key="route.id"
                  label="默认筛选范围"
                  title="默认筛选范围"
                  width="350px"
                  size="small"
                  :defaultValue="conditionsMap[route.id]"
                  :requireFilterTypes="route.requireFilterTypes || []"
                  :page-name="route.permissionKey"
                  :dataSourceOptions="dataSourceOptions"
                  fixedFieldModel
                  @confirm="(conditions: any) => filterConfirm(conditions, route.id)"
                ></AdvancedFilter>
              </div>
            </template>
          </div>
          <div class="two-level-wrapper" v-if="route.children">
            <template v-for="subRoute of route.children" :key="subRoute.id">
              <div class="two-level-item">
                <el-checkbox-group v-model="localFormData.reportPermissions">
                  <el-checkbox
                    :value="subRoute.id"
                    @change="(val: CheckboxValueType) => handleChildChange(route, subRoute.id, val)"
                  >
                    {{ subRoute.name }}
                  </el-checkbox>
                </el-checkbox-group>
                <template v-if="subRoute.buttonChildren">
                  <div v-if="subRoute.buttonChildren?.[0]?.icon === 'checkbox'">
                    <el-checkbox-group v-model="localFormData.bottonIdList">
                      <el-checkbox
                        v-for="btnItem of subRoute.buttonChildren"
                        :key="btnItem.id"
                        :value="btnItem.permissionKey"
                      >
                        {{ btnItem.name }}
                      </el-checkbox>
                    </el-checkbox-group>
                  </div>
                  <div v-else-if="subRoute.buttonChildren?.[0]?.icon === 'radio'">
                    <el-radio-group v-model="radioButtonValues[subRoute.id]">
                      <el-radio
                        v-for="btnItem of subRoute.buttonChildren"
                        :key="btnItem.id"
                        :value="btnItem.permissionKey"
                      >
                        {{ btnItem.name }}
                      </el-radio>
                    </el-radio-group>
                  </div>
                </template>

                <!-- AdvancedFilter 在 checkbox-group 外面,避免事件冲突 -->
                <AdvancedFilter
                  :childKey="subRoute.id"
                  v-if="subRoute.requireFilterTypes"
                  :key="subRoute.id"
                  label="默认筛选范围"
                  title="默认筛选范围"
                  width="350px"
                  size="small"
                  :defaultValue="conditionsMap[subRoute.id]"
                  :requireFilterTypes="subRoute.requireFilterTypes || []"
                  :page-name="subRoute.permissionKey"
                  :dataSourceOptions="dataSourceOptions"
                  fixedFieldModel
                  @confirm="(conditions: any) => filterConfirm(conditions, subRoute.id)"
                ></AdvancedFilter>
              </div>
              <!-- 单点事件,单独处理的 事件范围, 事件操作 -->
              <template v-if="subRoute.id == SINGLE_EVENT_ID">
                <div class="two-level-item single-event">
                  <div class="h-32 w-90 lh-32" style="font-size: 14px">事件范围</div>
                  <div>
                    <el-checkbox-group v-model="localFormData.singleEventScope">
                      <el-checkbox
                        v-for="item in menuPermissionList.singleEventScope"
                        :key="item.code"
                        :label="item.name"
                        :value="item.code"
                      />
                    </el-checkbox-group>
                  </div>
                </div>

                <div class="two-level-item single-event">
                  <div class="h-32 w-90 lh-32" style="font-size: 14px">事件操作</div>
                  <div>
                    <el-checkbox-group v-model="localFormData.singleEventOperation">
                      <el-checkbox
                        v-for="item in menuPermissionList.singleEventOperation"
                        :key="item.itemValue"
                        :label="item.itemText"
                        :value="item.itemValue"
                      />
                    </el-checkbox-group>
                  </div>
                </div>
              </template>
              <!-- 批量事件,复用单点事件的 事件范围, 事件操作 展示方式 -->
              <template v-if="subRoute.permissionKey === BATCH_EVENT_PERMISSION_KEY">
                <div class="two-level-item single-event">
                  <div class="h-32 w-90 lh-32" style="font-size: 14px">事件范围</div>
                  <div>
                    <el-checkbox-group v-model="localFormData.batchEventScope">
                      <el-checkbox
                        v-for="item in menuPermissionList.batchEventScope"
                        :key="item.code"
                        :label="item.name"
                        :value="item.code"
                      />
                    </el-checkbox-group>
                  </div>
                </div>

                <div class="two-level-item single-event">
                  <div class="h-32 w-90 lh-32" style="font-size: 14px">事件操作</div>
                  <div>
                    <el-checkbox-group v-model="localFormData.batchEventOperation">
                      <el-checkbox
                        v-for="item in menuPermissionList.batchEventOperation"
                        :key="item.itemValue"
                        :label="item.itemText"
                        :value="item.itemValue"
                      />
                    </el-checkbox-group>
                  </div>
                </div>
              </template>
            </template>
          </div>
        </div>
      </template>

      <!-- 功能模块-移动端 -->
      <div class="flex-y-center mb-16 mt-16">
        <span class="fs-14 sub-title lh-32">移动端</span>
      </div>
      <div class="router-wrapper">
        <div class="two-level-wrapper w-full">
          <template v-for="route in newAppKanBan.app" :key="route.id">
            <div class="two-level-item">
              <template v-if="route.children?.length">
                <el-checkbox
                  :indeterminate="getMobileMenuParentCheckboxState(route).indeterminate"
                  :model-value="getMobileMenuParentCheckboxState(route).checked"
                  :disabled="route.id === MHOMEID"
                  @change="(val: CheckboxValueType) => handleMobileMenuParentChange(route, val)"
                >
                  {{ route.name }}
                </el-checkbox>
              </template>
              <template v-else>
                <!-- checkbox-group 只包裹 checkbox,不包裹 AdvancedFilter -->
                <el-checkbox-group v-model="localFormData.mobilePermissions">
                  <el-checkbox
                    :value="route.id"
                    :disabled="route.id === MHOMEID"
                    @change="
                      (val: CheckboxValueType) => handleMobileMenuChildChange(route, route.id, val)
                    "
                  >
                    {{ route.name }}
                  </el-checkbox>
                </el-checkbox-group>
              </template>
              <!-- AdvancedFilter 在 checkbox-group 外面,避免事件冲突 -->
              <AdvancedFilter
                v-if="route.requireFilterTypes"
                :key="route.id"
                :defaultValue="conditionsMap[route.id]"
                label="默认筛选范围"
                title="默认筛选范围"
                width="350px"
                size="small"
                :requireFilterTypes="route.requireFilterTypes || []"
                :page-name="route.permissionKey"
                fixedFieldModel
                @confirm="(conditions: any) => filterConfirm(conditions, route.id)"
              ></AdvancedFilter>
            </div>
            <template v-if="route.children?.length">
              <div
                v-for="subRoute in route.children"
                :key="subRoute.id"
                class="two-level-item mobile-sub-item"
              >
                <el-checkbox-group v-model="localFormData.mobilePermissions">
                  <el-checkbox
                    :value="subRoute.id"
                    :disabled="subRoute.id === MHOMEID"
                    @change="
                      (val: CheckboxValueType) =>
                        handleMobileMenuChildChange(route, subRoute.id, val)
                    "
                  >
                    {{ subRoute.name }}
                  </el-checkbox>
                </el-checkbox-group>
                <AdvancedFilter
                  v-if="subRoute.requireFilterTypes"
                  :key="subRoute.id"
                  :defaultValue="conditionsMap[subRoute.id]"
                  label="默认筛选范围"
                  title="默认筛选范围"
                  width="350px"
                  size="small"
                  :requireFilterTypes="subRoute.requireFilterTypes || []"
                  :page-name="subRoute.permissionKey"
                  fixedFieldModel
                  @confirm="(conditions: any) => filterConfirm(conditions, subRoute.id)"
                ></AdvancedFilter>
              </div>
            </template>
          </template>
        </div>
      </div>
    </template>
    <template v-else-if="basicInfoData?.roleType === '2'">
      <el-row>
        <el-col :span="24">
          <div class="flex-y-center mb-16">
            <span class="fs-14 sub-title lh-32">操作权限</span>
          </div>
        </el-col>
        <div class="router-wrapper">
          <div class="one-level-wrapper">
            <el-checkbox
              :indeterminate="getPCParentCheckboxState().indeterminate"
              :model-value="getPCParentCheckboxState().checked"
              @change="handlePCParentChange"
            >
              PC端
            </el-checkbox>
          </div>
          <div class="two-level-wrapper">
            <template v-for="item in groupedFunctionPermissions.pc" :key="item.permissionKey">
              <div v-if="item.children?.length" class="two-level-item justify-start">
                <el-checkbox
                  :model-value="
                    getPermissionCheckboxState(item, localFormData.pcFunctionPermissions).checked
                  "
                  :indeterminate="
                    getPermissionCheckboxState(item, localFormData.pcFunctionPermissions)
                      .indeterminate
                  "
                  @change="val => handlePCChildChange(item, val)"
                >
                  {{ item.name }}
                </el-checkbox>
                <template v-if="item.description">
                  <SvgIcon name="info-circle" class="ml-8" color="#C9CED6" />
                  <div class="info-text ml-6">{{ item.description }}</div>
                </template>
              </div>
              <el-checkbox-group
                v-model="localFormData.pcFunctionPermissions"
                @change="handlePCGroupChange"
              >
                <template v-if="item.children?.length">
                  <div
                    v-for="child in item.children"
                    :key="child.permissionKey"
                    class="two-level-item sub-item justify-start"
                  >
                    <el-checkbox
                      :value="child.permissionKey"
                      @change="val => handlePCChildChange(child, val)"
                    >
                      {{ child.name }}
                    </el-checkbox>
                    <template v-if="child.description">
                      <SvgIcon name="info-circle" class="ml-8" color="#C9CED6" />
                      <div class="info-text ml-6">{{ child.description }}</div>
                    </template>
                  </div>
                </template>
                <template v-else>
                  <!-- sub-item -->
                  <div class="two-level-item bbm justify-start">
                    <el-checkbox
                      :value="item.permissionKey"
                      @change="val => handlePCChildChange(item, val)"
                    >
                      {{ item.name }}
                    </el-checkbox>
                    <template v-if="item.description">
                      <SvgIcon name="info-circle" class="ml-8" color="#C9CED6" />
                      <div class="info-text ml-6">{{ item.description }}</div>
                    </template>
                  </div>
                </template>
              </el-checkbox-group>
            </template>
          </div>
        </div>
        <div class="router-wrapper">
          <div class="one-level-wrapper">
            <el-checkbox
              :indeterminate="getMobileParentCheckboxState().indeterminate"
              :model-value="getMobileParentCheckboxState().checked"
              @change="handleMobileParentChange"
            >
              移动端
            </el-checkbox>
          </div>
          <div class="two-level-wrapper">
            <template v-for="item in groupedFunctionPermissions.mob" :key="item.permissionKey">
              <div v-if="item.children?.length" class="two-level-item justify-start">
                <el-checkbox
                  :model-value="
                    getPermissionCheckboxState(item, localFormData.mobileFunctionPermissions)
                      .checked
                  "
                  :indeterminate="
                    getPermissionCheckboxState(item, localFormData.mobileFunctionPermissions)
                      .indeterminate
                  "
                  @change="val => handleMobileChildChange(item, val)"
                >
                  {{ item.name }}
                </el-checkbox>
                <template v-if="item.description">
                  <SvgIcon name="info-circle" class="ml-8" color="#C9CED6" />
                  <div class="info-text ml-6">{{ item.description }}</div>
                </template>
              </div>
              <el-checkbox-group
                v-model="localFormData.mobileFunctionPermissions"
                @change="handleMobileGroupChange"
              >
                <template v-if="item.children?.length">
                  <div
                    v-for="child in item.children"
                    :key="child.permissionKey"
                    class="two-level-item sub-item justify-start"
                  >
                    <el-checkbox
                      :value="child.permissionKey"
                      @change="val => handleMobileChildChange(child, val)"
                    >
                      {{ child.name }}
                    </el-checkbox>
                    <template v-if="child.description">
                      <SvgIcon name="info-circle" class="ml-8" color="#C9CED6" />
                      <div class="info-text ml-6">{{ child.description }}</div>
                    </template>
                  </div>
                </template>
                <template v-else>
                  <div class="two-level-item bbm justify-start">
                    <el-checkbox
                      :value="item.permissionKey"
                      @change="val => handleMobileChildChange(item, val)"
                    >
                      {{ item.name }}
                    </el-checkbox>
                    <template v-if="item.description">
                      <SvgIcon name="info-circle" class="ml-8" color="#C9CED6" />
                      <div class="info-text ml-6">{{ item.description }}</div>
                    </template>
                  </div>
                </template>
              </el-checkbox-group>
            </template>
          </div>
        </div>
      </el-row>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { type FormInstance, type FormRules, type CheckboxValueType, ElMessage } from 'element-plus'
import { cloneDeep } from 'lodash-es'
import AdvancedFilter from '@/components/Business/AdvancedFilter/index.vue'
import useQueryStore from '@/store/modules/query'

const {
  menuPermissionList = {},
  isEdit = false,
  basicInfoRef
} = defineProps<{
  menuPermissionList: any
  isEdit?: boolean
  basicInfoRef?: any
}>()

const formRef = ref<FormInstance>()
const queryStore = useQueryStore()

// 总览页ID
const OVERVIEWID = '14f1e75e4cdb0e62d90b5e7222607b2e'
// 领导版总览
const LEADER_OVERVIEWID = '14f1e75e4cdb0e62d90b5e7222607800'
// 系统管理ID
const SYSTEMMANAGEID = '22a85c723de691f442bb8464d3691098'
// 移动端首页ID
const MHOMEID = 'c0c767c11416a2e6d5a0f7d2ff9bec58'
// 单点事件id
const SINGLE_EVENT_ID = '22a85c723de691f442bb8464d3691890'
// 批量事件路由权限标识
const BATCH_EVENT_PERMISSION_KEY = 'CDEBatchEvent'

// 内部表单数据
const localFormData = reactive<Record<any, any>>({
  channelIds: [],
  brandCode: [],
  reportPermissions: [] as string[],
  mobilePermissions: [] as string[],
  // PC端操作权限
  pcFunctionPermissions: [] as string[],
  // 移动端操作权限
  mobileFunctionPermissions: [] as string[],
  // 单点事件范围
  singleEventScope: [] as string[],
  // 单点事件操作
  singleEventOperation: [] as string[],
  // 批量事件范围
  batchEventScope: [] as string[],
  // 批量事件操作
  batchEventOperation: [] as string[],
  bottonIdList: [] as string[]
})

// 需注意, radio的默认值为buttonChildren的第一个选项的permissionKey
// radio 类型按钮的值存储对象，key 为 subRoute.id，value 为选中的 permissionKey
const radioButtonValues = reactive<Record<string, string>>({})

// 计算属性：获取 BasicInfo 表单数据（响应式）
const basicInfoData = computed(() => {
  return basicInfoRef?.getFormData() || {}
})

const isRoleType1 = computed(() => basicInfoData.value?.roleType === '1')

watch(
  () => isEdit,
  (val: boolean) => {
    if (!val) {
      localFormData.reportPermissions = [OVERVIEWID]
      localFormData.mobilePermissions = [MHOMEID]
    }
  },
  {
    immediate: true
  }
)

// 获取已选择的品牌
const getSelectedBrand = computed(() => {
  const result = brandOptions.value.filter((el: any) => localFormData.brandCode?.includes(el.code))

  return result?.map((el: any) => {
    return {
      ...el,
      value: el.name,
      key: el.code
    }
  })
})

// 获取已选择的数据源（保持树形结构）
const dataSourceOptions = computed(() => {
  return channelOptions.value
  // const selectedIds = localFormData.channelIds || []
  // if (!selectedIds.length) return []

  // const filterTree = (options: any[]): any[] => {
  //   return options.reduce((acc: any[], option: any) => {
  //     if (selectedIds.includes(option.code)) {
  //       acc.push(cloneDeep(option))
  //     } else if (option.child?.length) {
  //       const filteredChildren = filterTree(option.child)
  //       if (filteredChildren.length) {
  //         acc.push({ ...cloneDeep(option), child: filteredChildren })
  //       }
  //     }
  //     return acc
  //   }, [])
  // }

  // return filterTree(channelOptions.value)
})

// 渠道下拉选项
const channelOptions = computed(() => {
  return menuPermissionList?.dataChannel || []
})

// 品牌下拉选项
const brandOptions = computed(() => {
  return menuPermissionList?.brandList || []
})

const handleAppKanBan = (appKanban: any) => {
  // 过滤掉总览路由， 用户和领导版默认就设置了总览，所以这里无需处理
  if (!appKanban) {
    return { report: [], app: [] }
  }
  //  ?.filter((el: any) => el.id !== overviewID)
  return appKanban?.reduce(
    (pre: any, cur: any) => {
      if (cur.pageType === 'report') {
        pre.report.push(cloneDeep(cur))
      } else if (cur.pageType === 'app') {
        pre.app.push(cloneDeep(cur))
      }
      return pre
    },
    { report: [], app: [] }
  )
}

// 获取报表路由 区分appKanBan中的路由是移动还是PC， report 是报表  app是移动
const newAppKanBan = computed(() => {
  return handleAppKanBan(menuPermissionList?.appKanban)
})

// 按 pageType 分组 functionPermission
const groupedFunctionPermissions = computed(() => {
  const functionPermissions = menuPermissionList?.functionPermission || []
  const result = {
    pc: [] as any[],
    mob: [] as any[]
  }

  functionPermissions.forEach((item: any) => {
    const pageType = String(item.pageType || '').toLowerCase()
    if (pageType === 'pc') {
      result.pc.push(item)
    } else if (pageType === 'mob') {
      result.mob.push(item)
    }
  })

  return result
})

const formRules = computed<FormRules>(() => {
  if (!isRoleType1.value) return {}
  return {
    channelIds: [{ required: true, message: '请选择数据源', trigger: 'change' }]
  }
})

const conditionsMap = ref<any>({})

/**
 * @description: 默认筛选确定事件
 * @param {*} conditions
 * @param {*} id
 * @return {*}
 */
const filterConfirm = (conditions: any, id: string) => {
  conditionsMap.value[id] = filterValidConditions(conditions)
}

// 过滤条件数据，移除无效的条件项
const filterValidConditions = (conditions: any) => {
  if (!conditions || !Array.isArray(conditions)) return conditions
  return conditions.filter((condition: any) => condition.field && condition.value)
}

/**
 * 递归收集权限项及其所有子项的 permissionKey
 * @param item 权限项
 * @returns permissionKey 数组
 */
const collectAllPermissionKeys = (item: any): string[] => {
  const keys: string[] = []
  if (item.permissionKey) {
    keys.push(item.permissionKey)
  }
  if (item.children?.length) {
    item.children.forEach((child: any) => {
      keys.push(...collectAllPermissionKeys(child))
    })
  }
  return keys
}

const collectChildPermissionKeys = (item: any): string[] => {
  if (!item.children?.length) return []
  return item.children.flatMap((child: any) => collectAllPermissionKeys(child))
}

const getPermissionCheckboxState = (item: any, checkedKeys: string[]) => {
  const childKeys = collectChildPermissionKeys(item)
  if (!childKeys.length) {
    return { checked: checkedKeys.includes(item.permissionKey), indeterminate: false }
  }
  const checkedCount = childKeys.filter((key: string) => checkedKeys.includes(key)).length
  if (checkedCount === 0) {
    return { checked: false, indeterminate: false }
  }
  if (checkedCount === childKeys.length) {
    return { checked: true, indeterminate: false }
  }
  return { checked: false, indeterminate: true }
}

const findPermissionPathByKey = (items: any[], permissionKey: string): any[] => {
  for (const item of items) {
    if (item.permissionKey === permissionKey) {
      return [item]
    }
    if (item.children?.length) {
      const path = findPermissionPathByKey(item.children, permissionKey)
      if (path.length) {
        return [item, ...path]
      }
    }
  }
  return []
}

const syncParentSelection = (parent: any, selectedKeys: string[]) => {
  const childKeys = collectChildPermissionKeys(parent)
  if (!childKeys.length) return

  const checkedCount = childKeys.filter((key: string) => selectedKeys.includes(key)).length
  if (checkedCount === childKeys.length) {
    if (!selectedKeys.includes(parent.permissionKey)) {
      selectedKeys.push(parent.permissionKey)
    }
  } else {
    const index = selectedKeys.indexOf(parent.permissionKey)
    if (index > -1) {
      selectedKeys.splice(index, 1)
    }
  }
}

const syncAncestorSelections = (items: any[], permissionKey: string, selectedKeys: string[]) => {
  const path = findPermissionPathByKey(items, permissionKey)
  if (path.length <= 1) return

  for (let i = path.length - 2; i >= 0; i -= 1) {
    syncParentSelection(path[i], selectedKeys)
  }
}

const syncTreeSelections = (items: any[], selectedKeys: string[]) => {
  items.forEach((item: any) => {
    if (item.children?.length) {
      syncTreeSelections(item.children, selectedKeys)
      syncParentSelection(item, selectedKeys)
    }
  })
}

// 递归查找菜单详情
const findMenuById = (menus: any[], id: string): any => {
  for (const menu of menus) {
    if (menu.id === id) {
      return menu
    }
    if (menu.children?.length) {
      const found = findMenuById(menu.children, id)
      if (found) return found
    }
  }
  return null
}

// 计算父级checkbox的状态
const getParentCheckboxState = (route: any) => {
  if (!route.children?.length) return { checked: false, indeterminate: false }

  // 确保 reportPermissions 是数组
  const permissions = localFormData.reportPermissions || []

  const childIds = route.children.map((child: any) => child.id)
  const checkedChildren = childIds.filter((id: string) => permissions.includes(id))

  if (checkedChildren.length === 0) {
    return { checked: false, indeterminate: false }
  } else if (checkedChildren.length === childIds.length) {
    return { checked: true, indeterminate: false }
  } else {
    return { checked: false, indeterminate: true }
  }
}

// 计算移动端菜单父级 checkbox 的状态
const getMobileMenuParentCheckboxState = (route: any) => {
  if (!route.children?.length) {
    return {
      checked: localFormData.mobilePermissions?.includes(route.id),
      indeterminate: false
    }
  }

  const permissions = localFormData.mobilePermissions || []
  const childIds = route.children.map((child: any) => child.id)
  const checkedChildren = childIds.filter((id: string) => permissions.includes(id))

  if (checkedChildren.length === 0) {
    return {
      checked: permissions.includes(route.id),
      indeterminate: false
    }
  }

  if (checkedChildren.length === childIds.length) {
    return { checked: true, indeterminate: false }
  }

  return { checked: false, indeterminate: true }
}

/**
 * 清空指定子菜单的按钮权限
 * @param subRoute 子菜单对象
 */
const clearButtonPermissions = (subRoute: any) => {
  if (!subRoute.buttonChildren?.length) return

  // 处理 checkbox 类型的按钮权限
  if (subRoute.buttonChildren[0]?.icon === 'checkbox') {
    const buttonPermissionKeys = subRoute.buttonChildren.map((btn: any) => btn.permissionKey)
    localFormData.bottonIdList = localFormData.bottonIdList.filter(
      (id: string) => !buttonPermissionKeys.includes(id)
    )
  }

  // 处理 radio 类型的按钮权限
  if (subRoute.buttonChildren[0]?.icon === 'radio') {
    delete radioButtonValues[subRoute.id]
  }
}

// 处理移动端菜单父级 checkbox 变化
const handleMobileMenuParentChange = (route: any, val: CheckboxValueType) => {
  const checked = Boolean(val)
  if (!route.children?.length || route.id === MHOMEID) return

  if (!Array.isArray(localFormData.mobilePermissions)) {
    localFormData.mobilePermissions = []
  }

  const routeIds = [route.id, ...route.children.map((child: any) => child.id)]

  if (checked) {
    routeIds.forEach((id: string) => {
      if (!localFormData.mobilePermissions.includes(id)) {
        localFormData.mobilePermissions.push(id)
      }
    })
    return
  }

  localFormData.mobilePermissions = localFormData.mobilePermissions.filter(
    (id: string) => !routeIds.includes(id) || id === MHOMEID
  )
}

// 处理移动端菜单子级 checkbox 变化
const handleMobileMenuChildChange = (route: any, childId: string, val: CheckboxValueType) => {
  const checked = Boolean(val)

  if (!Array.isArray(localFormData.mobilePermissions)) {
    localFormData.mobilePermissions = []
  }

  if (childId === MHOMEID && !checked) {
    if (!localFormData.mobilePermissions.includes(MHOMEID)) {
      localFormData.mobilePermissions.push(MHOMEID)
    }
    return
  }

  if (!route.children?.length) return

  const childIds = route.children.map((child: any) => child.id)
  const checkedChildren = childIds.filter((id: string) =>
    localFormData.mobilePermissions.includes(id)
  )

  if (checkedChildren.length === childIds.length) {
    if (!localFormData.mobilePermissions.includes(route.id)) {
      localFormData.mobilePermissions.push(route.id)
    }
  } else {
    const parentIndex = localFormData.mobilePermissions.indexOf(route.id)
    if (parentIndex > -1 && route.id !== MHOMEID) {
      localFormData.mobilePermissions.splice(parentIndex, 1)
    }
  }
}

// 处理父级checkbox变化
const handleParentChange = (route: any, val: CheckboxValueType) => {
  const checked = Boolean(val)
  if (!route.children?.length) return

  // 确保 reportPermissions 是数组
  if (!Array.isArray(localFormData.reportPermissions)) {
    localFormData.reportPermissions = []
  }

  const childIds = route.children.map((child: any) => child.id)

  if (checked) {
    // 选中父级时，选中所有子级
    childIds.forEach((id: string) => {
      if (!localFormData.reportPermissions.includes(id)) {
        localFormData.reportPermissions.push(id)
      }
    })
    // 确保父级也被选中
    if (!localFormData.reportPermissions.includes(route.id)) {
      localFormData.reportPermissions.push(route.id)
    }
  } else {
    // 取消父级时，先清空所有子菜单的按钮权限
    route.children.forEach((subRoute: any) => {
      clearButtonPermissions(subRoute)
    })
    // 取消父级时，取消所有子级
    childIds.forEach((id: string) => {
      const index = localFormData.reportPermissions.indexOf(id)
      if (index > -1) {
        localFormData.reportPermissions.splice(index, 1)
      }
    })
    // 取消父级
    const parentIndex = localFormData.reportPermissions.indexOf(route.id)
    if (parentIndex > -1) {
      localFormData.reportPermissions.splice(parentIndex, 1)
    }
  }
}

// 计算PC端父级checkbox的状态
const getPCParentCheckboxState = () => {
  const allKeys = groupedFunctionPermissions.value.pc.flatMap((item: any) =>
    collectAllPermissionKeys(item)
  )
  const checkedKeys = localFormData.pcFunctionPermissions || []
  if (!allKeys.length) {
    return { checked: false, indeterminate: false }
  }
  const checkedCount = allKeys.filter((key: string) => checkedKeys.includes(key)).length

  if (checkedCount === 0) {
    return { checked: false, indeterminate: false }
  } else if (checkedCount === allKeys.length) {
    return { checked: true, indeterminate: false }
  } else {
    return { checked: false, indeterminate: true }
  }
}

// 计算移动端父级checkbox的状态
const getMobileParentCheckboxState = () => {
  const allKeys = groupedFunctionPermissions.value.mob.flatMap((item: any) =>
    collectAllPermissionKeys(item)
  )
  const checkedKeys = localFormData.mobileFunctionPermissions || []
  if (!allKeys.length) {
    return { checked: false, indeterminate: false }
  }
  const checkedCount = allKeys.filter((key: string) => checkedKeys.includes(key)).length

  if (checkedCount === 0) {
    return { checked: false, indeterminate: false }
  } else if (checkedCount === allKeys.length) {
    return { checked: true, indeterminate: false }
  } else {
    return { checked: false, indeterminate: true }
  }
}

// 处理PC端父级checkbox变化
const handlePCParentChange = (val: CheckboxValueType) => {
  const checked = Boolean(val)
  const allKeys = groupedFunctionPermissions.value.pc.flatMap((item: any) =>
    collectAllPermissionKeys(item)
  )
  if (!allKeys.length) return

  if (checked) {
    // 选中所有PC端权限
    allKeys.forEach((key: string) => {
      if (!localFormData.pcFunctionPermissions.includes(key)) {
        localFormData.pcFunctionPermissions.push(key)
      }
    })
  } else {
    // 取消所有PC端权限
    localFormData.pcFunctionPermissions = localFormData.pcFunctionPermissions.filter(
      (key: string) => !allKeys.includes(key)
    )
  }
}

// 处理移动端父级checkbox变化
const handleMobileParentChange = (val: CheckboxValueType) => {
  const checked = Boolean(val)
  const allKeys = groupedFunctionPermissions.value.mob.flatMap((item: any) =>
    collectAllPermissionKeys(item)
  )
  if (!allKeys.length) return

  if (checked) {
    // 选中所有移动端权限
    allKeys.forEach((key: string) => {
      if (!localFormData.mobileFunctionPermissions.includes(key)) {
        localFormData.mobileFunctionPermissions.push(key)
      }
    })
  } else {
    // 取消所有移动端权限
    localFormData.mobileFunctionPermissions = localFormData.mobileFunctionPermissions.filter(
      (key: string) => !allKeys.includes(key)
    )
  }
}

// 处理PC端子项变化
const handlePCChildChange = (item: any, val: CheckboxValueType) => {
  const checked = Boolean(val)

  if (item.children?.length) {
    const allKeys = collectAllPermissionKeys(item)
    if (checked) {
      // 选中该项及其所有子项
      allKeys.forEach((key: string) => {
        if (!localFormData.pcFunctionPermissions.includes(key)) {
          localFormData.pcFunctionPermissions.push(key)
        }
      })
    } else {
      // 取消该项及其所有子项
      localFormData.pcFunctionPermissions = localFormData.pcFunctionPermissions.filter(
        (key: string) => !allKeys.includes(key)
      )
    }
  }

  nextTick(() => {
    syncAncestorSelections(
      groupedFunctionPermissions.value.pc,
      item.permissionKey,
      localFormData.pcFunctionPermissions
    )
    syncTreeSelections(groupedFunctionPermissions.value.pc, localFormData.pcFunctionPermissions)
  })
}

// 处理移动端子项变化
const handleMobileChildChange = (item: any, val: CheckboxValueType) => {
  const checked = Boolean(val)

  if (item.children?.length) {
    const allKeys = collectAllPermissionKeys(item)
    if (checked) {
      // 选中该项及其所有子项
      allKeys.forEach((key: string) => {
        if (!localFormData.mobileFunctionPermissions.includes(key)) {
          localFormData.mobileFunctionPermissions.push(key)
        }
      })
    } else {
      // 取消该项及其所有子项
      localFormData.mobileFunctionPermissions = localFormData.mobileFunctionPermissions.filter(
        (key: string) => !allKeys.includes(key)
      )
    }
  }

  nextTick(() => {
    syncAncestorSelections(
      groupedFunctionPermissions.value.mob,
      item.permissionKey,
      localFormData.mobileFunctionPermissions
    )
    syncTreeSelections(
      groupedFunctionPermissions.value.mob,
      localFormData.mobileFunctionPermissions
    )
  })
}

const isSyncingPcGroup = ref(false)
const isSyncingMobileGroup = ref(false)

const handlePCGroupChange = () => {
  if (isSyncingPcGroup.value) return
  isSyncingPcGroup.value = true
  syncTreeSelections(groupedFunctionPermissions.value.pc, localFormData.pcFunctionPermissions)
  isSyncingPcGroup.value = false
}

const handleMobileGroupChange = () => {
  if (isSyncingMobileGroup.value) return
  isSyncingMobileGroup.value = true
  syncTreeSelections(groupedFunctionPermissions.value.mob, localFormData.mobileFunctionPermissions)
  isSyncingMobileGroup.value = false
}

// 处理子级checkbox变化
const handleChildChange = (route: any, childId: string, val: CheckboxValueType) => {
  const checked = Boolean(val)

  // 如果取消勾选，清空该子菜单的按钮权限
  if (!checked) {
    // 查找对应的子菜单
    const subRoute = route.children?.find((child: any) => child.id === childId)
    // 如果没有children，说明是一级菜单，直接使用route
    const targetRoute = subRoute || (route.id === childId ? route : null)
    if (targetRoute) {
      clearButtonPermissions(targetRoute)
    }
  }

  // 处理没有children的情况（一级菜单）
  if (!route.children?.length) {
    // 确保 reportPermissions 是数组
    if (!Array.isArray(localFormData.reportPermissions)) {
      localFormData.reportPermissions = []
    }
    return
  }

  // 确保 reportPermissions 是数组
  if (!Array.isArray(localFormData.reportPermissions)) {
    localFormData.reportPermissions = []
  }

  const childIds = route.children.map((child: any) => child.id)
  const checkedChildren = childIds.filter((id: string) =>
    localFormData.reportPermissions.includes(id)
  )

  // 如果所有子级都被选中，自动选中父级
  if (checkedChildren.length === childIds.length) {
    if (!localFormData.reportPermissions.includes(route.id)) {
      localFormData.reportPermissions.push(route.id)
    }
  } else {
    // 如果不是所有子级都被选中，取消父级选中状态
    const parentIndex = localFormData.reportPermissions.indexOf(route.id)
    if (parentIndex > -1) {
      localFormData.reportPermissions.splice(parentIndex, 1)
    }
  }
}

const getCheckedMenu = (array: any): any[] => {
  const result: any[] = []

  const traverse = (items: any[]) => {
    items.forEach((item: any) => {
      if (item.checked !== false) {
        result.push(item)
      }
      if (item.children?.length) {
        traverse(item.children)
      }
    })
  }

  traverse(array)
  return result
}

/**
 * 初始化 radio 组的默认值
 * @param subRoute 子路由对象
 */
const initRadioDefaultValue = (subRoute: any) => {
  if (
    subRoute.buttonChildren?.[0]?.icon === 'radio' &&
    subRoute.buttonChildren?.length > 0 &&
    !radioButtonValues[subRoute.id]
  ) {
    // 使用第一个选项的 permissionKey 作为默认值
    radioButtonValues[subRoute.id] = subRoute.buttonChildren[0].permissionKey
  }
}

/**
 * 遍历所有路由，初始化 radio 组的默认值
 */
const initAllRadioDefaults = () => {
  const result = newAppKanBan.value
  // 遍历 PC 端路由
  if (result.report) {
    result.report.forEach((route: any) => {
      if (route.children?.length) {
        route.children.forEach((subRoute: any) => {
          initRadioDefaultValue(subRoute)
        })
      }
    })
  }
}

// 初始化数据
const initData = (data: any) => {
  const overview = data?.appKanban?.find((el: any) => el.id === OVERVIEWID)

  // 先重置所有数组为空数组,避免 resetFields 导致的 null 值
  localFormData.reportPermissions = []
  localFormData.mobilePermissions = []
  localFormData.pcFunctionPermissions = []
  localFormData.mobileFunctionPermissions = []
  localFormData.bottonIdList = data.buttonList || []

  // 清空 radio 按钮值
  Object.keys(radioButtonValues).forEach(key => {
    delete radioButtonValues[key]
  })

  Object.assign(localFormData, {
    channelIds: data?.channelCodes || [],
    brandCode: data?.brandCodeList || []
  })

  localFormData.singleEventScope = data?.singleEventScope || []
  localFormData.singleEventOperation = data?.singleEventOperation || []
  localFormData.batchEventScope = data?.batchEventScope || []
  localFormData.batchEventOperation = data?.batchEventOperation || []

  const result = handleAppKanBan(data?.appKanban)
  if (result.app) {
    getCheckedMenu(result.app)?.forEach((el: any) => {
      localFormData.mobilePermissions.push(el.id)

      // 检查jsonObject中是否存在filterType为'94'的数据，如果存在则转换value为number[]
      if (el.jsonObject && Array.isArray(el.jsonObject)) {
        el.jsonObject.forEach((item: any) => {
          if (item.filterType === '94' && item.value) {
            item.value = Array.isArray(item.value)
              ? item.value.map((v: any) => Number(v))
              : [Number(item.value)]
          }
        })
      }

      conditionsMap.value[el.id] = el.jsonObject
    })
  }
  if (result.report) {
    getCheckedMenu(result.report)?.forEach((el: any) => {
      localFormData.reportPermissions.push(el.id)
      conditionsMap.value[el.id] = el.jsonObject
    })
  }

  if (!localFormData.reportPermissions.includes(OVERVIEWID)) {
    localFormData.reportPermissions.push(OVERVIEWID)
  }

  // 处理 radio 类型按钮的回显
  if (result.report) {
    result.report.forEach((route: any) => {
      if (route.children?.length) {
        route.children.forEach((subRoute: any) => {
          if (
            subRoute.buttonChildren?.[0]?.icon === 'radio' &&
            subRoute.buttonChildren?.length > 0
          ) {
            // 从 bottonIdList 中找到属于该 subRoute.buttonChildren 的值
            const buttonPermissionKeys = subRoute.buttonChildren.map(
              (btn: any) => btn.permissionKey
            )
            const foundValue = localFormData.bottonIdList.find((id: string) =>
              buttonPermissionKeys.includes(id)
            )

            if (foundValue) {
              // 如果找到了，设置到 radioButtonValues
              radioButtonValues[subRoute.id] = foundValue
            } else {
              // 如果找不到，使用默认值（第一个选项）
              radioButtonValues[subRoute.id] = subRoute.buttonChildren[0].permissionKey
            }
          }
        })
      }
    })
  }

  // 回显操作权限（functionPermissionList）
  if (data?.functionPermissionList && Array.isArray(data.functionPermissionList)) {
    // 将 functionPermissionList 中的 permissionKey 分配到对应的数组中
    const allPcKeys = groupedFunctionPermissions.value.pc.flatMap((item: any) =>
      collectAllPermissionKeys(item)
    )
    const allMobKeys = groupedFunctionPermissions.value.mob.flatMap((item: any) =>
      collectAllPermissionKeys(item)
    )

    data.functionPermissionList.forEach((permissionKey: string) => {
      if (allPcKeys.includes(permissionKey)) {
        if (!localFormData.pcFunctionPermissions.includes(permissionKey)) {
          localFormData.pcFunctionPermissions.push(permissionKey)
        }
      } else if (allMobKeys.includes(permissionKey)) {
        if (!localFormData.mobileFunctionPermissions.includes(permissionKey)) {
          localFormData.mobileFunctionPermissions.push(permissionKey)
        }
      }
    })
  }

  syncTreeSelections(groupedFunctionPermissions.value.pc, localFormData.pcFunctionPermissions)
  syncTreeSelections(groupedFunctionPermissions.value.mob, localFormData.mobileFunctionPermissions)

  // 如果是新建模式，初始化所有 radio 的默认值
  if (!isEdit) {
    initAllRadioDefaults()
  }
  // if (overview) {
  //   conditionsMap.value[overviewID] = overview.jsonObject
  // }
}

// 验证表单并返回数据
const validateAndGetData = async () => {
  if (!formRef.value) return null
  try {
    const fidleList: any[] = (await queryStore.fetchAdvancedFilterTypeList()) as any

    await formRef.value.validate()
    console.log('localFormData', localFormData)

    const permissionIdList: any = []

    if (isRoleType1.value) {
      // 校验用户版和领导版的主控台至少要选一个
      if (
        !localFormData.reportPermissions.includes(OVERVIEWID) &&
        !localFormData.reportPermissions.includes(LEADER_OVERVIEWID)
      ) {
        ElMessage.warning('请至少选择一个总览页')
        return
      }

      if (
        Array.isArray(localFormData.reportPermissions) &&
        localFormData.reportPermissions.length
      ) {
        localFormData.reportPermissions.forEach((el: string) => {
          permissionIdList.push({
            // roleType: el === OVERVIEWID ? localFormData.roleType : undefined,
            permissionId: el,
            jsonObject: filterValidConditions(conditionsMap.value[el])
          })
        })
      }
    }

    // if (localFormData.systemPermissions.length) {
    //   localFormData.systemPermissions.forEach((el: string) => {
    //     permissionIdList.push({
    //       permissionId: el,
    //       jsonObject: conditionsMap.value[el]
    //     })
    //   })
    // }

    if (isRoleType1.value) {
      if (
        Array.isArray(localFormData.mobilePermissions) &&
        localFormData.mobilePermissions.length
      ) {
        localFormData.mobilePermissions.forEach((el: string) => {
          permissionIdList.push({
            permissionId: el,
            jsonObject: filterValidConditions(conditionsMap.value[el])
          })
        })
      }
    }

    // console.log('permissionIdList', permissionIdList)

    if (isRoleType1.value) {
      // 校验默认筛选项范围逻辑
      for (const permission of permissionIdList) {
        // 递归查找菜单详情（支持二级菜单）
        const menuDetail = findMenuById(
          menuPermissionList?.appKanban || [],
          permission.permissionId
        )

        if (menuDetail?.requireFilterTypes?.length) {
          // 遍历requireFilterTypes，检查jsonObject中是否存在对应的filterType
          for (const requiredFilterType of menuDetail.requireFilterTypes) {
            const hasMatchingFilter = permission.jsonObject?.some(
              (filter: any) => filter.filterType === requiredFilterType
            )

            if (!hasMatchingFilter) {
              // 查找filterType对应的名称
              const filterTypeInfo = fidleList.find(
                (field: any) => field.filterType === requiredFilterType
              )
              const filterTypeName = filterTypeInfo?.name || requiredFilterType

              ElMessage.error(`${menuDetail.name} ${filterTypeName} 不允许为空`)
              return null
            }
          }
        }
      }
    }

    // 收集 radio 类型按钮的值到 bottonIdList
    const result = newAppKanBan.value
    // 收集所有 radio 组的 permissionKey，用于过滤
    const allRadioPermissionKeys = new Set<string>()

    if (result.report) {
      result.report.forEach((route: any) => {
        if (route.children?.length) {
          route.children.forEach((subRoute: any) => {
            if (subRoute.buttonChildren?.[0]?.icon === 'radio') {
              subRoute.buttonChildren.forEach((btn: any) => {
                allRadioPermissionKeys.add(btn.permissionKey)
              })
            }
          })
        }
      })
    }

    // 从 bottonIdList 中过滤掉所有 radio 组的 permissionKey（保留 checkbox 的值）
    const finalButtonIdList = localFormData.bottonIdList.filter(
      (id: string) => !allRadioPermissionKeys.has(id)
    )

    // 添加所有选中的 radio 值
    if (result.report) {
      result.report.forEach((route: any) => {
        if (route.children?.length) {
          route.children.forEach((subRoute: any) => {
            if (subRoute.buttonChildren?.[0]?.icon === 'radio' && radioButtonValues[subRoute.id]) {
              finalButtonIdList.push(radioButtonValues[subRoute.id])
            }
          })
        }
      })
    }

    // 收集 functionPermissionList：合并PC端和移动端选中的 permissionKey
    const functionPermissionList = Array.from(
      new Set([...localFormData.pcFunctionPermissions, ...localFormData.mobileFunctionPermissions])
    )

    return {
      channelIds: localFormData.channelIds,
      brandCode: localFormData.brandCode,
      permissionIdList,
      singleEventScope: localFormData.singleEventScope,
      singleEventOperation: localFormData.singleEventOperation,
      batchEventScope: localFormData.batchEventScope,
      batchEventOperation: localFormData.batchEventOperation,
      bottonIdList: finalButtonIdList,
      functionPermissionList
    }
  } catch {
    return null
  }
}

// 重置表单
const resetFields = () => {
  formRef.value?.resetFields()
  // 清空 radio 按钮值
  Object.keys(radioButtonValues).forEach(key => {
    delete radioButtonValues[key]
  })
}

defineExpose({
  initData,
  validateAndGetData,
  resetFields
})
</script>

<style lang="scss" scoped>
.sub-title {
  color: #333;
  font-weight: 600;
}

.card-layout {
  border-radius: 4px;
  border: 1px solid #ebedf0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

.router-wrapper {
  width: 100%;
  border-radius: 4px 4px 4px 4px;
  border: 1px solid #e5e6eb;

  font-weight: 600;
  font-size: 14px;
  color: #1d2129;
  line-height: 22px;

  & + .router-wrapper {
    margin-top: 16px;
  }

  .one-level-wrapper {
    padding: 9px 16px;
    background: #f7f8fa;
    display: flex;
    align-items: center;
    justify-content: space-between;
    &:deep(.el-checkbox__label) {
      font-weight: 600;
      font-size: 14px;
      color: #1d2129;
      line-height: 22px;
    }
  }

  .two-level-wrapper {
    background-color: #fff;

    .two-level-item {
      padding: 9px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid #ebedf0;
      &:deep(.el-checkbox__label) {
        font-weight: 600;
        font-size: 14px;
        color: #1d2129;
        line-height: 22px;
      }

      .info-text {
        font-weight: 400;
        font-size: 14px;
        color: #5f6a7a;
        line-height: 22px;
      }
    }
    > .two-level-item:last-child,
    > .el-checkbox-group:last-child > .two-level-item:last-child {
      border-bottom: none;
    }
    .justify-start {
      justify-content: flex-start;
    }
    .single-event {
      padding-left: 39px;
    }
    .mobile-sub-item {
      padding-left: 44px;
    }
    .sub-item {
      padding-left: 44px;
    }
  }
}
</style>
