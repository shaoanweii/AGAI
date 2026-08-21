<template>
  <el-drawer v-model="visible" :size="1200" @open="handleOpen" @close="handleClose">
    <template #header>
      <h4 class="fw-600">数据详情</h4>
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
        <DetailOriginal
          v-if="type === 1"
          :curDataSource="curDataSource"
          :curDataSourceDetail="curDataSourceDetail"
          :otherConditions="otherConditions"
        ></DetailOriginal>
        <!--结果数据-->
        <DetailResult
          v-if="type === 2"
          :curDataSource="curDataSource"
          :curDataSourceDetail="curDataSourceDetail"
          :otherConditions="otherConditions"
        ></DetailResult>
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <!-- <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template> -->
  </el-drawer>
</template>
<script lang="ts" setup>
import DetailOriginal from './DetailOriginal.vue'
import DetailResult from './DetailResult.vue'
import { getDataSourceSearchCriteria } from '@/api/dataCenter'
import type { DataSourceDetail } from '@/types/dataCenter.types'
import FtButtonGroup from '@/components/FtButtonGroup.vue'
import useUserStore from '@/stores/modules/user'

const props = withDefaults(
  defineProps<{
    curDataSource: any
    curDataSourceDetail: DataSourceDetail | undefined
  }>(),
  {}
)
const { curDataSource, curDataSourceDetail } = toRefs(props)

const userStore = useUserStore()

const visible = defineModel({ default: false })
// const table = ref({
//   filter: {}
// })

const type = ref()

const handleTypeChange = (curType: number) => {
  type.value = curType
}
const otherConditions = ref<any>({})
const handleOpen = async () => {
  try {
    otherConditions.value = await getDataSourceSearchCriteria({
      clientId: userStore.clientId!,
      dataSourceId: curDataSource.value.id,
      batchId: curDataSourceDetail.value?.batchId
    }).then(res => res.result)
  } catch (e) {}
  handleTypeChange(1)
}

// const handleCancel = () => {
//   handleClose()
// }
// 关闭
const handleClose = () => {
  visible.value = false
  handleTypeChange(-1)
}
// const handleOk = () => {
//   handleClose()
// }
</script>

<style lang="scss" scoped>
.body-wrapper {
  padding: 12px 8px;

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
