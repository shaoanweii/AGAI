<template>
  <el-drawer
    v-model="visible"
    :size="1200"
    :footer="false"
    :data-testid="`warning-drawer`"
    @open="handleOpen"
    @close="handleClose"
  >
    <template #header>
      <div style="display: flex; align-items: center; gap: 12px">
        <h4 style="margin: 0; font-size: 18px; font-weight: 600; color: var(--color-high)">
          纠错详情
        </h4>
        <el-select
          v-model="brand"
          :data-testid="`wf-10001`"
          placeholder="全部"
          style="width: 150px"
          @change="brandChange"
        >
          <el-option
            v-for="(item, index) in brandOptions"
            :key="index"
            :data-testid="`wf-10001-op-${index}`"
            :label="item.brandName"
            :value="item.brandName"
          />
        </el-select>
      </div>
    </template>
    <template #default>
      <div class="body-wrapper">
        <ContentView
          :record="record"
          :brand="brand"
          :channelOptions="channelOptions"
          :carSeriesOptions="carSeriesOptions"
          :tagLibCategoryVosOptions="tagLibCategoryVosOptions"
          :conditions="conditions"
        />
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script lang="ts" setup>
import ContentView from './ContentView.vue'
import to from 'await-to-js'
import useUserStore from '@/stores/modules/user'
import { findProjectInfo, findLabelSearchCriteria, findLabelConditions } from '@/api/project'
import { ElMessage } from 'element-plus'
import type { ConditionsDetailItem, Conditions } from '@/types'

interface Porps {
  record: any
}
const { record } = defineProps<Porps>()
const conditions = ref<Record<string, ConditionsDetailItem[]>>({})
const userStore = useUserStore()

const visible = defineModel({ default: false })

const brandOptions = ref<any[]>([])
const brand = ref('')
const channelOptions = ref<any[]>([])
const carSeriesOptions = ref<any[]>([])
const tagLibCategoryVosOptions = ref<any[]>([])

const brandChange = () => {
  getChannelByBrand()
  getLabelSearchCriteria()
}

// 获取品牌下拉选项
const getBrand = async () => {
  const [err, data] = await to(
    findProjectInfo({
      id: record.id,
      clientId: userStore.clientId
    })
  )
  if (err) return ElMessage.error(err.message)
  if (data) {
    brandOptions.value = data.result.brand

    if (brandOptions.value?.length > 0) {
      brand.value = brandOptions.value[0]?.brandName
      getChannelByBrand()
      getLabelSearchCriteria()
    }
  }
}
// 处理字典数据格式
const handleConditions = (conditions: Conditions[]) => {
  const newConditions: Record<string, ConditionsDetailItem[]> = {}
  conditions?.forEach(el => {
    newConditions[el.key] = el.details
  })
  return newConditions
}
const getConditions = async () => {
  const [err, data] = await to(findLabelConditions({ clientId: userStore.clientId }))
  if (err) return ElMessage.error(err.message)
  if (data) {
    conditions.value = handleConditions(data.result as Conditions[])
  }
}

const getLabelSearchCriteria = async () => {
  const [err, data] = await to(
    findLabelSearchCriteria({
      brand: brand.value,
      clientId: userStore.clientId
    })
  )
  if (err) return ElMessage.error(err.message)
  if (data) {
    const result = data.result as any
    const currentBrand = result?.brandVos?.find((item: any) => item.brandName === brand.value)
    carSeriesOptions.value = currentBrand?.carSeries || []
    tagLibCategoryVosOptions.value = result?.tagLibCategoryVos || []
  }
}
const getChannelByBrand = () => {
  let result = brandOptions.value.find((item: any) => {
    if (item.brandName === brand.value) {
      return item
    }
  })
  channelOptions.value = result?.channelTree
}

const handleOpen = async () => {
  await getBrand()
  await getConditions()
}

const handleCancel = () => {
  handleClose()
}

const handleOk = () => {
  handleClose()
}
// 关闭
const handleClose = () => {
  visible.value = false
}
</script>

<style lang="scss" scoped>
// 优化查看纠错抽屉样式
:deep(.el-drawer__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 0;
}

:deep(.el-drawer__body) {
  padding: 0;
}

.body-wrapper {
  padding: 24px;

  // 优化表单区域样式
  :deep(.el-form) {
    .el-form-item {
      margin-bottom: 20px;

      .el-form-item__label {
        color: var(--color-high);
        font-weight: 500;
      }
    }
  }

  // 优化分隔线样式
  :deep(.el-divider) {
    margin: 24px 0;
    border-color: var(--border-color);
  }

  // 优化表格样式
  :deep(.el-table) {
    .el-table__header-wrapper th {
      background-color: var(--bgc-def);
      color: var(--color-high);
      font-weight: 600;
      border-bottom: 1px solid var(--border-color);
    }

    .el-table__body-wrapper {
      .el-table__row {
        &:hover {
          background-color: var(--el-table-row-hover-bg-color);
        }
      }

      .el-table__cell {
        border-bottom: 1px solid var(--border-color);
      }
    }
  }

  // 优化按钮样式
  :deep(.el-button) {
    &.el-button--primary {
      background-color: var(--color-primary);
      border-color: var(--color-primary);
    }

    &:disabled {
      opacity: 0.6;
    }
  }

  // 优化选择器样式
  :deep(.el-select) {
    .el-select__wrapper {
      border-radius: 4px;
    }
  }

  // 优化日期选择器样式
  :deep(.el-date-editor) {
    .el-input__wrapper {
      border-radius: 4px;
    }
  }

  ::v-deep(.el-tabs) {
    .el-tabs-tab-active {
      background-color: var(--color-primary);

      .el-tabs-tab-title {
        color: #fff;
      }
    }

    .el-tabs-content {
      padding: 0;
    }
  }
}
</style>
