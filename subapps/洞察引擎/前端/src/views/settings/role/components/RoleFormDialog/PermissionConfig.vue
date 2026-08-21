<template>
  <el-form
    ref="formRef"
    :model="localFormData"
    :rules="formRules"
    style="height: calc(96vh - 230px)"
    class="pt-24 pl-16 pr-16 flex-auto overflow-auto"
  >
    <el-row>
      <el-col :span="24">
        <div class="flex-y-center">
          <span class="mr-5" style="color: #f53f3f">*</span>
          <span class="fs-14 sub-title">数据范围</span>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="15" class="pl-12">
      <el-col :span="12" class="pt-16">
        <el-form-item label="数据源" prop="channelIds">
          <el-cascader
            v-model="localFormData.channelIds"
            :options="channelOptions"
            clearable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="1"
            filterable
            :props="{
              value: 'code',
              label: 'name',
              children: 'child',
              multiple: true,
              emitPath: false,
              checkStrictly: false
            }"
            placeholder="请选择数据源"
            class="w-full"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12" class="pt-16">
        <el-form-item label="品牌" prop="brandCode">
          <el-cascader
            v-model="localFormData.brandCode"
            :options="brandOptions"
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            :max-collapse-tags="1"
            :props="{
              value: 'code',
              label: 'name',
              children: 'child',
              multiple: true,
              emitPath: false,
              checkStrictly: true
            }"
            placeholder="请选择品牌"
            class="w-full"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-divider style="margin: 0 0 16px 0" />
    <el-row>
      <el-col :span="24">
        <div class="flex-y-center">
          <span class="mr-5" style="color: #f53f3f">*</span>
          <span class="fs-14 sub-title">功能模块-PC</span>
        </div>
      </el-col>
    </el-row>

    <el-checkbox-group v-model="localFormData.reportPermissions">
      <template v-for="route in newAppKanBan.report" :key="route.id">
        <template v-if="route.id === OVERVIEWID">
          <el-row :gutter="15" class="pl-12">
            <el-col :span="24" class="pl-16 pt-16">
              <el-checkbox :value="route.id" disabled>{{ route.name }}</el-checkbox>
            </el-col>
          </el-row>
          <el-row class="card-layout pl-16 ml-16 mt-16">
            <el-col :span="24">
              <el-form-item prop="roleType" class="mt-16">
                <div class="w-full flex-between items-center pr-8">
                  <el-radio-group v-model="localFormData.roleType">
                    <el-radio label="1">用户版</el-radio>
                    <el-radio label="2">领导版</el-radio>
                  </el-radio-group>

                  <!-- <AdvancedFilter
                    :key="route.id"
                    :defaultValue="conditionsMap[route.id]"
                    label="默认筛选范围"
                    width="350px"
                    size="small"
                    :requireFilterTypes="route.requireFilterTypes || []"
                    :page-name="route.permissionKey"
                    :brandOptions="getSelectedBrand"
                    @confirm="(conditions: any) => filterConfirm(conditions, route.id)"
                  ></AdvancedFilter> -->
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        <template v-else-if="route.id === SYSTEMMANAGEID">
          <el-row :gutter="15" class="pl-12">
            <el-col :span="24" class="pl-16 pt-16">
              <el-checkbox
                :value="route.id"
                :indeterminate="getParentCheckboxState(route).indeterminate"
                :model-value="getParentCheckboxState(route).checked"
                @change="(val: CheckboxValueType) => handleParentChange(route, val)"
              >
                {{ route.name }}
              </el-checkbox>
            </el-col>
          </el-row>
          <el-row class="card-layout pl-16 ml-16 mt-16">
            <el-col :span="24">
              <el-checkbox
                v-for="subRoute of route.children"
                :key="subRoute.id"
                :value="subRoute.id"
                @change="(val: CheckboxValueType) => handleChildChange(route, subRoute.id, val)"
                >{{ subRoute.name }}</el-checkbox
              >
            </el-col>
          </el-row>
        </template>
        <template v-else>
          <el-row :gutter="15" class="pl-12">
            <el-col :span="24" class="pl-16 pt-16">
              <el-checkbox
                :value="route.id"
                :indeterminate="getParentCheckboxState(route).indeterminate"
                :model-value="getParentCheckboxState(route).checked"
                @change="(val: CheckboxValueType) => handleParentChange(route, val)"
              >
                {{ route.name }}
              </el-checkbox>
            </el-col>
          </el-row>
          <el-row class="card-layout pl-16 ml-16 mt-16 pb-10">
            <el-col v-for="subRoute of route.children" :key="subRoute.id" :span="24" class="mt-11">
              <div class="w-full flex-between items-center pr-8">
                <el-checkbox
                  :value="subRoute.id"
                  @change="(val: CheckboxValueType) => handleChildChange(route, subRoute.id, val)"
                  >{{ subRoute.name }}</el-checkbox
                >
                <!-- <AdvancedFilter
                  :key="subRoute.id"
                  :defaultValue="conditionsMap[subRoute.id]"
                  label="默认筛选范围"
                  width="350px"
                  size="small"
                  :requireFilterTypes="subRoute.requireFilterTypes || []"
                  :page-name="subRoute.permissionKey"
                  :brandOptions="getSelectedBrand"
                  @confirm="(conditions: any) => filterConfirm(conditions, subRoute.id)"
                ></AdvancedFilter> -->
              </div>
            </el-col>
          </el-row>
        </template>
      </template>
    </el-checkbox-group>

    <el-divider style="margin: 16px 0" />
    <el-row>
      <el-col :span="24">
        <div class="flex-y-center">
          <span class="mr-5" style="color: #f53f3f">*</span>
          <span class="fs-14 sub-title">功能模块-移动端</span>
        </div>
      </el-col>
      <el-col :span="24">
        <el-checkbox-group v-model="localFormData.mobilePermissions">
          <template v-for="route in newAppKanBan.app" :key="route.id">
            <!-- <el-form-item :label="route.name" class="ml-24 mt-16">

          </el-form-item> -->
            <!-- <el-checkbox-group v-model="localFormData.mobilePermissions">

            </el-checkbox-group> -->
            <div class="flex-y-center ml-12 mt-16 mb-16 flex-between">
              <el-checkbox :value="route.id" :disabled="route.id === MHOMEID">
                {{ route.name }}
              </el-checkbox>
              <!-- <AdvancedFilter
                :key="route.id"
                :defaultValue="conditionsMap[route.id]"
                label="默认筛选范围"
                width="350px"
                size="small"
                :requireFilterTypes="route.requireFilterTypes || []"
                :page-name="route.permissionKey"
                :brandOptions="getSelectedBrand"
                @confirm="(conditions: any) => filterConfirm(conditions, route.id)"
              ></AdvancedFilter> -->
            </div>
          </template>
        </el-checkbox-group>
      </el-col>
    </el-row>
    <el-divider style="margin: 0 0 16px 0" />
    <el-row>
      <el-col :span="24">
        <div class="flex-y-center">
          <span class="mr-5" style="color: #f53f3f">*</span>
          <span class="fs-14 sub-title">操作权限</span>
        </div>
      </el-col>
      <el-col :span="24" class="pt-16">
        <el-form-item label="PC端" class="ml-24" label-width="60px" label-position="left">
          <el-checkbox-group v-model="localFormData.operationPermissions">
            <el-checkbox value="publish">场景报告发布</el-checkbox>
          </el-checkbox-group>
          <el-checkbox-group v-model="localFormData.highQualityPermission" class="ml-16">
            <el-checkbox value="highQP">标记高质量声音</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="移动端" class="ml-24" label-width="60px" label-position="left">
          <el-checkbox-group v-model="localFormData.mobileOperationPermissions">
            <el-checkbox value="executiveTasks">高管任务</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { type FormInstance, type FormRules, type CheckboxValueType, ElMessage } from 'element-plus'
import { cloneDeep } from 'lodash-es'
// import AdvancedFilter from '@/components/Business/AdvancedFilter/index.vue'
// import useQueryStore from '@/store/modules/query'

const { menuPermissionList = {}, isEdit = false } = defineProps<{
  menuPermissionList: any
  isEdit?: boolean
}>()

const formRef = ref<FormInstance>()
// const queryStore = useQueryStore()

// 总览页ID
const OVERVIEWID = '14f1e75e4cdb0e62d90b5e7222607b2e'
// 系统管理ID
const SYSTEMMANAGEID = '22a85c723de691f442bb8464d3691098'
// 移动端首页ID
const MHOMEID = 'c0c767c11416a2e6d5a0f7d2ff9bec58'

// 内部表单数据
const localFormData = reactive<Record<any, any>>({
  channelIds: [],
  brandCode: [],
  roleType: '1',
  reportPermissions: [] as string[],
  // systemPermissions: [] as string[],
  mobilePermissions: [] as string[],
  operationPermissions: [] as string[],
  highQualityPermission: [] as string[],
  mobileOperationPermissions: [] as string[]
})

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

const formRules = computed<FormRules>(() => ({
  channelIds: [{ required: true, message: '请选择数据源', trigger: 'change' }],
  brandCode: [{ required: true, message: '请选择品牌', trigger: 'change' }]
}))

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

  const childIds = route.children.map((child: any) => child.id)
  const checkedChildren = childIds.filter((id: string) =>
    localFormData.reportPermissions.includes(id)
  )

  if (checkedChildren.length === 0) {
    return { checked: false, indeterminate: false }
  } else if (checkedChildren.length === childIds.length) {
    return { checked: true, indeterminate: false }
  } else {
    return { checked: false, indeterminate: true }
  }
}

// 处理父级checkbox变化
const handleParentChange = (route: any, val: CheckboxValueType) => {
  const checked = Boolean(val)
  if (!route.children?.length) return

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

// 处理子级checkbox变化
const handleChildChange = (route: any, childId: string, val: CheckboxValueType) => {
  if (!route.children?.length) return

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
  if (!Array.isArray(array)) return result

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

// 初始化数据
const initData = (data: any) => {
  const overview = data?.appKanban?.find((el: any) => el.id === OVERVIEWID)

  Object.assign(localFormData, {
    channelIds: data?.channelCodes || [],
    brandCode: data?.brandCodeList || [],
    roleType: overview?.roleType || '1'
  })

  if (data?.permissionScenario) {
    localFormData.operationPermissions.push('publish')
  }

  if (data?.highQualityPermission) {
    localFormData.highQualityPermission.push('highQP')
  }

  if (data?.executivePermission) {
    localFormData.mobileOperationPermissions.push('executiveTasks')
  }

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
  // if (overview) {
  //   conditionsMap.value[overviewID] = overview.jsonObject
  // }
}

// 验证表单并返回数据
const validateAndGetData = async () => {
  if (!formRef.value) return null
  try {
    // const fidleList: any[] = (await queryStore.fetchAdvancedFilterTypeList()) as any
    const fidleList: any[] = []

    // console.log('newAppKanBan', menuPermissionList?.appKanban)
    // console.log('conditionsMap.value', conditionsMap.value)
    // console.log('fidleList', fidleList)

    await formRef.value.validate()
    // console.log('localFormData', localFormData)

    const permissionIdList: any = []

    // permissionIdList.push({
    //   roleType: localFormData.roleType,
    //   permissionId: overviewID,
    //   jsonObject: conditionsMap.value[overviewID]
    // })

    if (localFormData.reportPermissions.length) {
      localFormData.reportPermissions.forEach((el: string) => {
        permissionIdList.push({
          roleType: el === OVERVIEWID ? localFormData.roleType : undefined,
          permissionId: el,
          jsonObject: filterValidConditions(conditionsMap.value[el])
        })
      })
    }

    // if (localFormData.systemPermissions.length) {
    //   localFormData.systemPermissions.forEach((el: string) => {
    //     permissionIdList.push({
    //       permissionId: el,
    //       jsonObject: conditionsMap.value[el]
    //     })
    //   })
    // }

    if (localFormData.mobilePermissions.length) {
      localFormData.mobilePermissions.forEach((el: string) => {
        permissionIdList.push({
          permissionId: el,
          jsonObject: filterValidConditions(conditionsMap.value[el])
        })
      })
    }

    const permissionScenario = ref(false)

    if (localFormData.operationPermissions.includes('publish')) {
      permissionScenario.value = true
    }
    const highQualityPermission = ref(false)
    if (localFormData.highQualityPermission.includes('highQP')) {
      highQualityPermission.value = true
    }

    const executivePermission = ref(false)
    if (localFormData.mobileOperationPermissions.includes('executiveTasks')) {
      executivePermission.value = true
    }

    // console.log('permissionIdList', permissionIdList)

    // 校验默认筛选项范围逻辑
    for (const permission of permissionIdList) {
      // 递归查找菜单详情（支持二级菜单）
      const menuDetail = findMenuById(menuPermissionList?.appKanban || [], permission.permissionId)

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

    return {
      channelIds: localFormData.channelIds,
      brandCode: localFormData.brandCode,
      permissionScenario: permissionScenario.value,
      executivePermission: executivePermission.value,
      permissionIdList,
      highQualityPermission: highQualityPermission.value
    }
  } catch {
    return null
  }
}

// 重置表单
const resetFields = () => {
  formRef.value?.resetFields()
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
</style>
