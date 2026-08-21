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
          预警详情
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
        <FtButtonGroup
          v-model="tabType"
          class="mb-24"
          :group="btnGroup"
          :testid="`wf-`"
        ></FtButtonGroup>
        <!-- v-if="[1, 2, 3].includes(tabType)" -->
        <ContentView
          v-if="tabType"
          :record="record"
          :riskType="tabType"
          :brand="brand"
          :brandOptions="brandOptions"
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
import FtButtonGroup from '@/components/FtButtonGroup.vue'
import to from 'await-to-js'
import useUserStore from '@/stores/modules/user'
import { findProjectInfo } from '@/api/project'
import { ElMessage } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'

interface Porps {
  record: any
}
const { record } = defineProps<Porps>()
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const userStore = useUserStore()

const visible = defineModel({ default: false })

// const btnGroup = [
//   { label: '业务问题', value: 1 },
//   { label: '质量故障', value: 2 },
//   { label: '投诉用户', value: 3 }
// ]
const tabType = ref()
const brandOptions = ref<any[]>([])
const brand = ref()

const btnGroup = computed(() => {
  return (
    conditions.earlyWarningType
      ?.map((item: any) => {
        return {
          label: item.value,
          value: item.key
        }
      })
      ?.filter((el: any) => riskEarlyWarning.value?.includes(el.value)) || []
  )
})

const riskEarlyWarning = computed(() => {
  const curBrand = brandOptions.value?.find(el => el.brandName === brand.value)
  console.log('curBrand', curBrand)
  if (curBrand) {
    return curBrand.riskEarlyWarning?.map((el: any) => el.warningType)
  } else {
    return []
  }
})

const brandChange = () => {
  tabType.value = btnGroup.value[0]?.value
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
    }
  }
}

const handleOpen = async () => {
  console.log('record', record)
  await getBrand()
  tabType.value = btnGroup.value[0]?.value
}

const handleCancel = () => {
  handleClose()
}

const handleOk = () => {
  handleClose()
}
// 关闭
const handleClose = () => {
  tabType.value = -1
  visible.value = false
}
</script>

<style lang="scss" scoped>
// 优化查看预警抽屉样式
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

  // 优化按钮组样式
  :deep(.ft-button-group) {
    margin-bottom: 24px;
  }

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
