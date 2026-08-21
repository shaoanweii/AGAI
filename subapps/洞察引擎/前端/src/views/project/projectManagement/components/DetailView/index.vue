<template>
  <el-drawer v-model="visible" :size="1200" :footer="false" @open="handleOpen" @close="handleClose">
    <template #header>
      <div style="display: flex; align-items: center; gap: 12px">
        <h4 style="margin: 0; font-size: 18px; font-weight: 600; color: var(--color-high)">
          数据详情
        </h4>
        <el-select v-model="brand" style="width: 150px" @change="brandChange">
          <el-option
            v-for="(item, index) in projectInfoDetail.brand"
            :key="index"
            :data-testid="`wf-10001-op-${index}`"
            :label="item.brandName"
            :value="item.brandCode"
          />
        </el-select>
      </div>
    </template>
    <template #default>
      <div class="body-wrapper">
        <div class="mb-24">
          <!--<el-button :type="type === 1 ? 'primary' : undefined" @click="handleTypeChange(1)">原始数据</el-button>-->
          <!--<el-button :type="type === 2 ? 'primary' : undefined" class="ml-8" @click="handleTypeChange(2)">结果数据-->
          <!--</el-button>-->
          <FtButtonGroup
            v-model="type"
            :group="[
              { label: '原始数据', value: 1 },
              { label: '结果数据', value: 2 }
            ]"
            :testid="`dataSource-detailView-`"
          ></FtButtonGroup>
        </div>
        <!--原始数据-->
        <!-- :curDataSource="curDataSource" -->
        <DetailOriginal
          v-if="type === 1"
          ref="originRef"
          :projectInfoDetail="projectInfoDetail"
          :curDataSourceDetail="curDataSourceDetail"
          :otherConditions="otherConditions"
          :brand="brand"
          :defaultBrand="defaultBrand"
          :channelOptions="channelOptions"
          :carSeriesOptions="carSeriesOptions"
          :competitiveCarSeriesOptions="competitiveCarSeriesOptions"
          :mentionCarSeriesOptions="mentionCarSeriesOptions"
        ></DetailOriginal>
        <!--结果数据-->
        <DetailResult
          v-if="type === 2"
          ref="resultRef"
          :projectInfoDetail="projectInfoDetail"
          :curDataSourceDetail="curDataSourceDetail"
          :otherConditions="otherConditions"
          :brand="brand"
          :channelOptions="channelOptions"
          :carSeriesOptions="carSeriesOptions"
          :competitiveCarSeriesOptions="competitiveCarSeriesOptions"
          :mentionCarSeriesOptions="mentionCarSeriesOptions"
          :integrationListOptions="integrationListOptions"
        ></DetailResult>
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
import DetailOriginal from './DetailOriginal.vue'
import DetailResult from './DetailResult.vue'
import { findSearchCriteria, findProjectInfo } from '@/api/project'
import type { ProjectDetail } from '@/types/project.d.ts'
import FtButtonGroup from '@/components/FtButtonGroup.vue'
import useUserStore from '@/stores/modules/user'
import { ElMessage } from 'element-plus'
import to from 'await-to-js'

const props = withDefaults(
  defineProps<{
    curDataSourceDetail: ProjectDetail
  }>(),
  {}
)
const { curDataSourceDetail } = toRefs(props)

const userStore = useUserStore()

const visible = defineModel({ default: false })
const brand = ref()
const defaultBrand = ref()
const originRef = ref()
const resultRef = ref()
const type = ref()

const otherConditions = ref<any>({})

const channelOptions = ref<Record<any, any>[]>()

let projectInfoDetail = ref<any>({})
/**
 * @description: 获取项目详情
 * @return {*}
 */
const getProjectDetail = async () => {
  const [err, data] = await to(
    findProjectInfo({
      id: curDataSourceDetail.value.id,
      clientId: userStore.clientId
    })
  )
  if (err) return ElMessage.error(err.message)
  if (data) {
    projectInfoDetail.value = data.result

    defaultBrand.value = projectInfoDetail.value?.brand?.[0]?.brandCode
    brand.value = defaultBrand.value
    getChannelByBrand()
  }
}

/**
 * @description: 根据品牌获取渠道
 * @return {*}
 */
const getChannelByBrand = () => {
  let result = projectInfoDetail.value.brand.find((item: any) => {
    if (item.brandCode === brand.value) {
      return item
    }
  })
  channelOptions.value = result?.channelTree
}

const carSeriesOptions = ref<Record<any, any>[]>()
const competitiveCarSeriesOptions = ref<Record<any, any>[]>()
const mentionCarSeriesOptions = ref<Record<any, any>[]>()
const integrationListOptions = ref<Record<any, any>[]>()
/**
 * @description: 根据品牌查询车系、竞品车系
 * @return {*}
 */
const getCarSeriesAndCompetitiveCarSeries = () => {
  const result = otherConditions.value?.brandVos?.find((item: any) => {
    if (item.brandCode === brand.value) {
      return item
    }
  })
  carSeriesOptions.value = result?.carSeries
  competitiveCarSeriesOptions.value = result?.competitiveCarSeries
  mentionCarSeriesOptions.value = result?.mentionCarSeriesList || []
  integrationListOptions.value = result?.integrationList || []
}

/**
 * @description: 品牌change
 * @param {*} val
 * @return {*}
 */
const brandChange = async (val: any) => {
  brand.value = val
  await getSearchCriteria()
  nextTick(() => {
    getChannelByBrand()
    getCarSeriesAndCompetitiveCarSeries()
    if (type.value === 1) {
      originRef.value?.query()
    } else if (type.value === 2) {
      resultRef.value?.query()
    }
  })
}

const handleTypeChange = (curType: number) => {
  type.value = curType
}

const getBrandName = computed(() => {
  return projectInfoDetail.value?.brand.find((el: any) => el.brandCode === brand.value)?.brandName
})

/**
 * @description: 获取查询相关选项及限制条件
 * @return {*}
 */
const getSearchCriteria = async () => {
  const [errs, data] = await to(
    findSearchCriteria({
      brand: getBrandName.value,
      clientId: userStore.clientId!,
      projectId: curDataSourceDetail.value.id
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
  }
  if (data) {
    otherConditions.value = data.result

    getCarSeriesAndCompetitiveCarSeries()
  }
}

const handleOpen = async () => {
  await getProjectDetail()
  await getSearchCriteria()
  // try {
  //   otherConditions.value = await findSearchCriteria({
  //     clientId: userStore.clientId!,
  //     projectId: curDataSourceDetail.value.id
  //   }).then(res => res.result)
  //   getCarSeriesAndCompetitiveCarSeries()
  // } catch (e: any) {
  //   ElMessage.error(e.message)
  // }
  handleTypeChange(1)
}

const handleCancel = () => {
  handleClose()
}
// 关闭
const handleClose = () => {
  visible.value = false
  handleTypeChange(-1)
}
const handleOk = () => {
  handleClose()
}
</script>

<style lang="scss" scoped>
// 优化查看数据弹窗样式
:deep(.el-drawer__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 0;

  .el-drawer__title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-high);
  }
}

:deep(.el-drawer__body) {
  padding: 0;
}

.body-wrapper {
  padding: 24px;

  .upload-area {
    background-color: rgb(229, 241, 255);
    text-align: center;
    padding: 40px 60px;

    i {
      font-size: 24px;
      color: var(--color-primary);
    }

    p {
      color: var(--color-primary);
      margin-bottom: 8px;
    }

    span {
      font-size: 12px;
      color: var(--color-low);
    }
  }

  .sub-form {
    padding: 24px;
  }

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
