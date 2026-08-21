<template>
  <el-drawer
    class="result-table"
    :size="1200"
    v-model="visible"
    @open="handleOpen"
    :footer="false"
    destroy-on-close
  >
    <template #header>
      <span class="ml-8">{{ titleStr }}</span>
    </template>
    <div class="detail">
      <div class="title">基本信息</div>
      <div class="wrapper mt-24">
        <el-row class="grid-demo">
          <el-col :span="8">
            <div class="item">
              <div class="item-label">规则名称:</div>
              <div :data-testid="`processing-detail-1001`" class="item-content">
                {{ detail?.name }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">应用客户:</div>
              <div :data-testid="`processing-detail-1002`" class="item-content">
                {{ detail?.clientIdText }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">规则描述:</div>
              <div :data-testid="`processing-detail-1003`" class="item-content">
                {{ detail?.description }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">处理阶段:</div>
              <div :data-testid="`processing-detail-1004`" class="item-content">
                {{ detail?.processPhaseText }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">规则类型:</div>
              <div :data-testid="`processing-detail-1005`" class="item-content">
                {{ detail?.regulationTypeText }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">内容格式:</div>
              <div :data-testid="`processing-detail-1006`" class="item-content">
                {{ detail?.contentTypeText }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">数据渠道:</div>
              <div :data-testid="`processing-detail-1007`" class="item-content">
                {{ detail.channelText?.join('、') }}
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="title mt-24">
        <span>条件限制</span>
        <span :data-testid="`processing-detail-1008`" class="subtitle ml-8">{{
          detail?.matchingRuleText
        }}</span>
      </div>
      <div class="wrapper border mt-16 clear-top-padding">
        <el-row class="grid-demo">
          <el-col
            :span="24"
            v-for="(item, index) of (detail?.regulationConditions as any[])"
            :key="index"
          >
            <div class="item border-line">
              <div class="item-label">条件{{ index + 1 }}:</div>
              <div class="item-content" :data-testid="`processing-detail-2001-${index}`">
                {{
                  `${item?.fieldNameText} ${item?.variableValueText} ${
                    item?.logicalOperatorText || ''
                  } ${item?.conditionTypeText || ''} ${
                    item?.conditionDetailText || item?.conditionDetail || ''
                  }`
                }}
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="title mt-24">
        <span>执行动作</span>
      </div>
      <div class="wrapper border mt-16 clear-top-padding">
        <el-row class="grid-demo">
          <el-col :span="24">
            <div class="item border-line">
              <!--（即对满足条件的整条数据进行删除操作）-->
              <div class="item-content" :data-testid="`processing-detail-3001`">
                {{ detail?.regulationTypeText }}
                {{ regulationTypeStrMapping[detail?.regulationType] }}
              </div>
            </div>
          </el-col>
          <el-col
            :span="24"
            v-for="(item, index) of (detail?.regulationPerformAction as any[])"
            :key="index"
          >
            <div class="item border-line">
              <div class="item-label">动作{{ index + 1 }}:</div>
              <!--item?.logicalOperatorText-->
              <div class="item-content" :data-testid="`processing-detail-4001-${index}`">
                {{
                  `${item?.fieldNameText} ${item?.variableValueText} ${
                    detail?.regulationTypeText || ''
                  } ${item?.conditionTypeText || ''} ${
                    item?.conditionDetailText || item?.conditionDetail || ''
                  }`
                }}
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="title mt-24">
        <span>规则权重</span>
      </div>
      <div class="wrapper border mt-16 clear-top-padding">
        <el-row class="grid-demo">
          <el-col :span="24">
            <div class="item border-line">
              <div class="item-content" :data-testid="`processing-detail-5001`">
                {{ detail?.regulationWeight }}
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { regulationTypeStrMapping } from '../dataMapping'
import { findRegulationInfo } from '@/api/dataProcessing'

const props = withDefaults(
  defineProps<{
    id: string
    clientId: string
  }>(),
  {}
)
const { id, clientId } = toRefs(props)

const visible = defineModel({ required: true, default: false })

const detail: Record<string, any> = ref({})
const titleStr = ref('规则详情')

const handleCancel = () => {
  visible.value = false
}

const getDetailById = async (id: string) => {
  const response = await findRegulationInfo({ id, clientId: clientId.value })
  if (response.code === '200') {
    return response.result
  } else {
    return {}
  }
}

const handleOpen = async () => {
  if (id.value) {
    detail.value = await getDetailById(id.value)
  }
}
</script>

<style scoped lang="scss">
.detail {
  margin-top: 12px;
  padding: 0 24px;

  .title {
    font-weight: 600;
    font-size: 16px;
    color: #1d2129;
    line-height: 24px;
  }

  .subtitle {
    font-weight: 400;
    font-size: 14px;
    color: #1d2129;
    line-height: 22px;
  }

  .wrapper {
    background: #f7f8fa;
    padding: 16px 16px 0;

    & + .border {
      border: 1px solid #e5e6eb;
    }

    &.clear-top-padding {
      padding: 0 16px !important;
    }

    &::v-deep(.el-row .el-col) {
      &:last-child {
        .item {
          border-bottom: none;
        }
      }
    }

    .item {
      display: flex;
      margin-bottom: 28px;
      box-sizing: border-box;

      &.border-line {
        margin-bottom: 0;
        line-height: 50px;
        border-bottom: 1px solid #e5e6eb;
        box-sizing: border-box;

        .item-label,
        .item-content {
          line-height: 50px;
        }
      }

      .item-label {
        font-weight: 400;
        font-size: 14px;
        color: #86909c;
        line-height: 22px;
        flex: none;

        & + .item-content {
          margin-left: 16px;
        }
      }

      .item-content {
        font-weight: 400;
        font-size: 14px;
        color: #1d2129;
        line-height: 22px;
      }
    }
  }
}
</style>
