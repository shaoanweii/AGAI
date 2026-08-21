<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Plus, Close, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useQueryStore } from '@/store/modules/query'
import { useRoute } from 'vue-router'
import FilterValueInput from '../FilterValueInput/index.vue'
import { filterValueMapping } from './helper'
import { FILTER_TYPE } from '@/constants'
import { cloneDeep } from 'lodash-es'

defineOptions({
  name: 'AdvancedFilter'
})

// 筛选条件接口定义
interface FilterCondition {
  id: string
  field: string
  value: string | string[] | any[] // 支持字符串、字符串数组或任意数组
  ext: any
  [key: string]: any // 允许存储字段的所有配置项
}

const {
  width = '310px',
  label = '高级筛选',
  title = '高级筛选',
  size = 'default',
  defaultValue,
  pageName = '',
  brandOptions,
  dataSourceOptions,
  requireFilterTypes = [],
  childKey,
  fixedFieldModel = false
} = defineProps<{
  width?: string
  label?: string
  title?: string
  size?: string
  // 子组件的key, 用于区分多个子组件
  childKey?: string
  // 默认值
  defaultValue?: FilterCondition[]
  // 页面名称, 用于过滤字段选项
  pageName?: string
  // 品牌选项
  brandOptions?: any[]
  // 数据源选项
  dataSourceOptions?: any[]
  // 标识条件必填, 用于角色管理
  requireFilterTypes?: any[]
  // 固定字段模式, 用于角色管理
  fixedFieldModel?: boolean
}>()

// Emits 定义
const emit = defineEmits<{
  confirm: [filters: FilterCondition[]]
  cancel: []
}>()

const route = useRoute()

// 创建默认条件的工厂函数,确保每个实例都有独立的对象
const createDefaultCondition = (): FilterCondition[] => [
  {
    id: Date.now().toString(),
    field: '',
    value: '',
    ext: {}
  }
]

// 筛选条件列表 - 使用工厂函数创建独立的初始值
const filterConditions = ref<FilterCondition[]>(!fixedFieldModel ? createDefaultCondition() : [])

// 设置默认选中的值， 用于回显
watch(
  () => defaultValue,
  newVal => {
    if (Array.isArray(newVal)) {
      if (newVal.length === 0) {
        if (!fixedFieldModel) {
          filterConditions.value = createDefaultCondition()
        }
      } else {
        // 角色管理页面使用固定字段模式回显
        if (fixedFieldModel && route.name === 'RoleManagement') {
          // 将 defaultValue 的值填充到固定字段中
          filterConditions.value.forEach(condition => {
            const defaultItem = newVal.find((item: any) => item.field === condition.field)
            if (defaultItem) {
              condition.value = defaultItem.value
              condition.ext = defaultItem.ext || {}
            }
          })
        } else {
          // 其他页面使用原有回显逻辑
          filterConditions.value = cloneDeep(newVal)?.map((el: any, index: number) => {
            return {
              ...el,
              id: el.id || `${Date.now()}_${index}`,
              ext: el.ext || {}
            }
          })
        }
        confirmedConditions.value = cloneDeep(filterConditions.value)
      }
    }
  },
  { immediate: true, deep: true }
)

const queryStore = useQueryStore()

// 响应式数据
const visible = ref(false)
const popoverRef = ref()

// 字段选项
const fieldOptions = ref<any>([])

// 已确认的条件（用于显示在触发器上）
const confirmedConditions = ref<FilterCondition[]>([])

// 监听面板打开，恢复已确认的条件
watch(visible, newVisible => {
  if (newVisible && confirmedConditions.value.length > 0) {
    filterConditions.value = cloneDeep(confirmedConditions.value)
  }
})

// 获取字段配置
const getFieldConfig = (fieldValue: string) => {
  return fieldOptions.value.find((item: any) => item.field === fieldValue)
}

/**
 * 将筛选类型统一转换为字符串，避免后端返回数字和字符串时匹配失败。
 * @param filterType 后端筛选类型编码
 * @returns 字符串筛选类型
 */
const normalizeFilterType = (filterType: unknown) => {
  return String(filterType ?? '')
}

/**
 * 将页面展示范围配置统一拆成字符串数组。
 * @param pageDisplayType 后端 pageDisplayType 配置
 * @returns 页面标识数组
 */
const normalizePageDisplayTypes = (pageDisplayType: unknown): string[] => {
  const values = Array.isArray(pageDisplayType) ? pageDisplayType : [pageDisplayType]

  return values
    .flatMap(item =>
      String(item ?? '')
        .split(/[、,，;；\s]+/)
        .map(value => value.trim())
    )
    .filter(Boolean)
}

/**
 * 判断字段配置是否允许在当前页面展示。
 * @param fieldConfig 高级筛选字段配置
 * @returns 是否命中当前页面
 */
const isPageDisplayMatched = (fieldConfig: any) => {
  const currentPageName = String(pageName || route.name || '')
  if (!currentPageName) return false

  return normalizePageDisplayTypes(fieldConfig?.pageDisplayType).includes(currentPageName)
}

/**
 * 判断是否为内置固定筛选类型，普通高级筛选中需要过滤。
 * @param filterType 后端筛选类型编码
 * @returns 是否为内置固定筛选类型
 */
const isBuiltInFilterType = (filterType: unknown) => {
  return FILTER_TYPE.includes(normalizeFilterType(filterType))
}

/**
 * 判断字段配置是否为当前菜单要求填写的默认筛选类型。
 * @param filterType 后端筛选类型编码
 * @returns 是否命中菜单 requireFilterTypes
 */
const isRequiredFilterType = (filterType: unknown) => {
  const requiredTypes = requireFilterTypes.map(normalizeFilterType)
  return requiredTypes.includes(normalizeFilterType(filterType))
}

// 处理字段选择变化
const handleFieldChange = (condition: FilterCondition) => {
  // 获取字段配置
  const fieldConfig = getFieldConfig(condition.field)

  // 根据字段类型初始化值
  if (fieldConfig) {
    // 如果是多选类型，初始化为空数组
    if (fieldConfig.multiSelect) {
      condition.value = []
    } else {
      // 否则初始化为空字符串
      condition.value = ''
    }

    // 存储字段配置到条件中
    Object.assign(condition, fieldConfig)
  } else {
    // 没有配置时，默认为空字符串
    condition.value = ''
  }
}

// 处理角色管理页面的默认筛选项
const handleRoleManagementfixedField = (fidleList: any) => {
  fieldOptions.value = fidleList?.filter(isPageDisplayMatched)
  // console.log('fieldOptions.value--->1', fieldOptions.value)
  const _fixedField = fieldOptions.value.filter((el: any) => isRequiredFilterType(el.filterType))
  // console.log('_fixedField--->2', _fixedField)

  if (_fixedField) {
    for (const element of _fixedField) {
      const newId = `${Date.now()}_${Math.random().toString(36).substring(2, 11)}`
      filterConditions.value.push({
        id: newId,
        field: element.field || '',
        value: element.multiSelect ? [] : '',
        ext: {},
        ...element
      })
    }
  }
}

const init = async () => {
  // 获取字段选项
  let fidleList: any[] = (await queryStore.fetchAdvancedFilterTypeList()) as any
  fidleList = fidleList.map(el => ({ ...el, ext: el.ext || {} }))
  // 根据路由name, 或者菜单中的permissionKey,过滤出本页面的高级筛选的条件
  if (route.name === 'RoleManagement') {
    // 处理角色管理页面的默认筛选项
    handleRoleManagementfixedField(fidleList)
    // fieldOptions.value = fidleList
  } else if (
    route.name === 'voiceManagement' ||
    pageName === 'voiceManagement' ||
    route.name === 'selfServiceOriginalSoundQuery' ||
    pageName === 'selfServiceOriginalSoundQuery'
  ) {
    // 处理声音标记页面的高级筛选项
    // FILTER_TYPE_TIME
    fieldOptions.value = fidleList?.filter((el: any) => !isBuiltInFilterType(el.filterType))
  } else {
    fieldOptions.value = fidleList
      ?.filter((el: any) => !isBuiltInFilterType(el.filterType))
      ?.filter(isPageDisplayMatched)
  }
}

init()

// 方法
const addCondition = (_field?: string) => {
  // 使用时间戳 + 随机数确保 ID 唯一性
  const newId = `${Date.now()}_${Math.random().toString(36).substring(2, 11)}`
  filterConditions.value.push({
    id: newId,
    field: _field || '',
    value: '',
    ext: {}
  })
}

const removeCondition = (id: string) => {
  if (filterConditions.value.length > 1) {
    filterConditions.value = filterConditions.value.filter(item => item.id !== id)
  } else {
    // 如果是最后一个条件，清空数据而不删除行
    const condition = filterConditions.value.find(item => item.id === id)
    if (condition) {
      const isMultiSelect = condition.multiSelect
      condition.field = ''
      // 根据是否多选初始化值
      condition.value = isMultiSelect ? [] : ''
      condition.ext = {}
      // 清除其他字段配置
      Object.keys(condition).forEach(key => {
        if (key !== 'id' && key !== 'field' && key !== 'value' && key !== 'ext') {
          delete condition[key]
        }
      })
    }
  }
}

const handleConfirm = () => {
  // 更新已确认的条件
  if (fixedFieldModel) {
    // 校验所有条件的字段和值都不能为空
    const emptyCondition = filterConditions.value.find((condition: any) => {
      if (!condition.field) return true
      if (Array.isArray(condition.value)) {
        return condition.value.length === 0
      }
      return !condition.value
    })

    if (emptyCondition) {
      const fieldName = emptyCondition.field ? getFieldDisplayName(emptyCondition.field) : '字段'
      ElMessage.error(`${fieldName}不能为空`)
      return
    }

    confirmedConditions.value = cloneDeep(filterConditions.value)
    emit('confirm', filterValueMapping(filterConditions.value))
  } else {
    confirmedConditions.value = cloneDeep(validConditions.value)
    emit('confirm', filterValueMapping(validConditions.value))
  }

  visible.value = false
}

const handleCancel = () => {
  emit('cancel')
  visible.value = false
}

// 阻止面板内部点击事件冒泡
const handlePanelClick = (event: Event) => {
  event.stopPropagation()
}

// 获取有效的筛选条件
const validConditions = computed(() => {
  return filterConditions.value.filter(condition => condition.field && condition.value)
})

// 获取字段显示名称
const getFieldDisplayName = (fieldValue: string) => {
  const config = getFieldConfig(fieldValue)
  return config?.name || fieldValue
}

// 获取已选择的字段
const getSelectedFields = (currentConditionId: string) => {
  return filterConditions.value
    .filter(condition => condition.id !== currentConditionId && condition.field)
    .map(condition => condition.field)
}

// 获取可用的字段选项
const getAvailableFieldOptions = (currentConditionId: string) => {
  const selectedFields = getSelectedFields(currentConditionId)
  return fieldOptions.value.map((option: any) => ({
    ...option,
    disabled: selectedFields.includes(option.field)
  }))
}
</script>

<template>
  <div class="advanced-filter">
    <!--
       :hide-after="0"
      :popper-style="{
        padding: 0,
        borderRadius: '12px',
        border: '1px solid #DFE2E8'
      }"
    -->
    <el-popover
      :key="childKey"
      ref="popoverRef"
      v-model:visible="visible"
      placement="bottom-end"
      :width="720"
      trigger="click"
      :teleported="true"
    >
      <template #reference>
        <div
          class="filter-trigger"
          :class="{ default: size === 'default', small: size === 'small' }"
          :style="{ width }"
        >
          <SvgIcon name="filter-lines" width="20px" height="20px" color="#999999"></SvgIcon>
          <div class="label">{{ label }}</div>
          <div class="line"></div>
          <div class="content">
            <!-- 显示第一个已确认条件 -->
            <div v-if="confirmedConditions.length > 0" class="c-tag flex-y-center cursor-point">
              <span class="single-line-ellipsis">{{
                getFieldDisplayName(confirmedConditions[0].field)
              }}</span>
              <SvgIcon
                name="close"
                width="14px"
                height="14px"
                color="#5F6A7A"
                class="ml-4 flex-none"
              ></SvgIcon>
            </div>

            <!-- 显示剩余已确认条件数量 -->
            <div v-if="confirmedConditions.length > 1" class="c-tag flex-y-center cursor-point">
              <span>+{{ confirmedConditions.length - 1 }}</span>
            </div>
          </div>

          <SvgIcon
            name="chevron-down"
            width="20px"
            height="20px"
            color="#929AA6"
            class="ml-4"
          ></SvgIcon>
        </div>
      </template>

      <template #default>
        <div class="filter-panel" @click="handlePanelClick">
          <!-- 面板标题 -->
          <div class="panel-header">
            <span class="panel-title">{{ title }}</span>
            <el-button type="text" @click="visible = false" class="close-btn">
              收起
              <el-icon class="ml-4" :size="20">
                <ArrowUp />
              </el-icon>
            </el-button>
          </div>

          <!-- 筛选条件列表 -->
          <div class="filter-conditions">
            <div v-for="condition in filterConditions" :key="condition.id" class="condition-row">
              <!-- 字段选择 -->
              <div class="condition-item" @click.stop>
                <el-select
                  v-model="condition.field"
                  placeholder="请选择"
                  class="field-select"
                  clearable
                  filterable
                  :teleported="false"
                  :disabled="fixedFieldModel"
                  @change="handleFieldChange(condition)"
                >
                  <el-option
                    v-for="option in getAvailableFieldOptions(condition.id)"
                    :key="option.field"
                    :value="option.field || ''"
                    :label="option.name"
                    :disabled="option.disabled"
                  >
                    <span v-if="requireFilterTypes?.includes(option.filterType)" class="required"
                      >*</span
                    >
                    <span>{{ option.name }}</span>
                  </el-option>
                </el-select>
              </div>

              <!-- 值输入 -->
              <div class="condition-item" @click.stop>
                <FilterValueInput
                  v-model="condition.value"
                  v-model:shortcut-value="condition.ext.selectedShortcut"
                  :condition="condition"
                  :page-name="pageName"
                  :brand-options="brandOptions"
                  :dataSourceOptions="dataSourceOptions"
                  :teleported="false"
                  :key="`${childKey}_${condition.id}`"
                  :childKey="`${childKey}_${condition.id}`"
                />
              </div>

              <!-- 删除按钮 -->
              <div class="condition-actions" v-if="!fixedFieldModel">
                <div @click="removeCondition(condition.id)" class="remove-btn">
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 添加条件按钮 -->
          <div class="add-condition" v-if="!fixedFieldModel">
            <el-button type="text" @click="() => addCondition()" class="add-btn">
              <el-icon><Plus /></el-icon>
              <span class="ml-8">添加筛选条件</span>
            </el-button>
          </div>

          <!-- 操作按钮 -->
          <div class="panel-actions">
            <div class="cancel-btn flex-center cursor-point" @click="handleCancel">取消</div>
            <div class="confirm-btn flex-center cursor-point" @click="handleConfirm">确定</div>
          </div>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<style lang="scss" scoped>
.advanced-filter {
  display: inline-block;

  .filter-trigger {
    width: 310px;

    background: #ffffff;
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #d5d7da;

    display: flex;
    align-items: center;
    &.default {
      height: 40px;
      padding: 8px 12px;
    }
    &.small {
      height: 32px;
      padding: 5px 12px;
    }
    .label {
      font-weight: 500;
      font-size: 14px;
      color: #333333;
      line-height: 20px;
      margin-left: 8px;
    }

    .line {
      width: 1px;
      height: 100%;
      background-color: #ebedf0;
      margin: 0 8px;
    }

    .content {
      flex: 1;
      min-width: 0;
      display: flex;
      .c-tag {
        background: #f2f4f7;
        border-radius: 4px;
        padding: 2px 8px 2px 12px;

        font-weight: 500;
        font-size: 14px;
        color: #1f2733;
        line-height: 20px;
        flex-shrink: 0;
        max-width: 110px;

        & + .c-tag {
          margin-left: 8px;
        }
      }
    }

    // &:hover {
    //   border-color: $brand-primary;
    //   color: $brand-primary;
    // }
  }
}

.filter-panel {
  padding: 0;
  min-width: 720px;

  .panel-header {
    height: 48px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px 8px 24px;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);

    .panel-title {
      font-size: 16px;
      font-weight: 500;
      color: $text-primary;
    }

    .close-btn {
      color: $text-secondary;
      padding: 4px 8px;

      &:hover {
        color: $brand-primary;
      }
    }
  }

  .filter-conditions {
    padding: 20px 40px;
    // max-height: 300px;
    // overflow: auto;

    .condition-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .condition-item {
        flex: 1;
        min-width: 0;

        .required {
          // color: #ff5959;
          color: #f53f3f;
          margin-right: 5px;
          line-height: 24px;
        }

        &:first-child {
          flex: 0 0 140px;
        }

        &:nth-child(2) {
          flex: 1;
        }
      }

      .condition-actions {
        flex: 0 0 32px;
        display: flex;
        justify-content: center;

        .remove-btn {
          width: 24px;
          height: 24px;
          background: #f2f4f7;
          border-radius: 20px 20px 20px 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;

          &:hover {
            color: $color-error;
          }
        }
      }
    }
  }

  .add-condition {
    padding: 0 40px 20px;

    .add-btn {
      color: $brand-primary;
      padding: 8px 12px;
      border: 1px dashed $brand-primary;
      border-radius: 4px;
      width: 100%;
      justify-content: center;
    }
  }

  .panel-actions {
    padding: 16px 40px;
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .cancel-btn {
      flex: 1;
      height: 40px;
      background: #f5f6f7;
      // border: 1px solid #d5d7da;
      color: #4e5969;
      font-size: 14px;
      border-radius: 2px;
    }

    .confirm-btn {
      flex: 1;
      height: 40px;
      background: #1677ff;
      border: 1px solid #1677ff;
      color: #ffffff;
      font-size: 14px;
      border-radius: 2px;
    }
  }
}
</style>
