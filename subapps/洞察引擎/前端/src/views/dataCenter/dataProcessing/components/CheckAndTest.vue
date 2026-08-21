<template>
  <el-drawer
    :size="1200"
    v-model="visible"
    :data-testid="`processing-4006`"
    @open="handleOpen"
    @close="handleClose"
    destroy-on-close
    :show-close="true"
  >
    <template #header>
      <span class="ml-8">{{ titleStrMapping[type] }}</span>
    </template>
    <div class="detail">
      <div class="title">规则信息</div>
      <div class="wrapper mt-24">
        <el-row class="grid-demo">
          <el-col :span="8">
            <div class="item">
              <div class="item-label">规则名称:</div>
              <div :data-testid="`processing-checkandtest-1001`" class="item-content">
                {{ detail?.name }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">应用客户:</div>
              <div :data-testid="`processing-checkandtest-1002`" class="item-content">
                {{ detail?.clientIdText }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">规则类型:</div>
              <div :data-testid="`processing-checkandtest-1003`" class="item-content">
                {{ detail?.regulationTypeText }}
              </div>
            </div>
          </el-col>

          <el-col :span="8">
            <div class="item">
              <div class="item-label">数据渠道:</div>
              <div :data-testid="`processing-checkandtest-1004`" class="item-content">
                {{ detail.channelText?.join('、') }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">处理阶段:</div>
              <div :data-testid="`processing-checkandtest-1005`" class="item-content">
                {{ detail?.processPhaseText }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="item">
              <div class="item-label">内容格式:</div>
              <div :data-testid="`processing-checkandtest-1006`" class="item-content">
                {{ detail?.contentTypeText }}
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="title mt-24">
        <span>条件限制</span>
        <span :data-testid="`processing-checkandtest-1007`" class="subtitle ml-8">{{
          detail?.matchingRuleText
        }}</span>
      </div>
      <div v-if="hisRegulationConditions?.length" class="wrapper border mt-16 clear-top-padding">
        <el-row class="grid-demo">
          <el-col
            :span="24"
            v-for="(item, index) of (hisRegulationConditions as any[])"
            :key="index"
          >
            <div class="item border-line">
              <div class="item-label">条件{{ index + 1 }}:</div>
              <div class="item-content" :data-testid="`processing-checkandtest-2001-${index}`">
                {{
                  `${item?.fieldNameText} ${item?.variableValueText || ''} ${
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
              <div class="item-content" :data-testid="`processing-checkandtest-4001`">
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
              <div class="item-content" :data-testid="`processing-checkandtest-3001-${index}`">
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
        <span>选择{{ typeStrMapping[type] }}数据</span>
        <!--<span class="subtitle ml-8">满足全部条件(AND)</span>-->
      </div>
      <div class="wrapper border mt-16">
        <el-row class="grid-demo">
          <el-col :span="24">
            <el-form :model="formData" :rules="rules" auto-label-width ref="formRef">
              <el-form-item prop="times" label="数据范围" required :style="{ width: '884px' }">
                <el-date-picker
                  v-model="formData.times"
                  type="daterange"
                  :data-testid="`processing-4001`"
                />
              </el-form-item>
              <el-form-item prop="contentType" label="内容格式" required>
                <el-radio-group
                  v-model="formData.contentType"
                  default-value="and"
                  :data-testid="`processing-4002`"
                >
                  <el-radio
                    v-for="item in conditions.regulationContentType"
                    :disabled="item.key === 'order'"
                    :value="item.key"
                    :key="item.key"
                  >
                    {{ item.value }}
                  </el-radio>
                  <!--<el-radio disabled>对话（禁选）</el-radio>-->
                </el-radio-group>
              </el-form-item>
              <el-form-item prop="channel" label="数据渠道" required :style="{ width: '884px' }">
                <ChannelCascader
                  v-model="formData.channel"
                  controller="regulation"
                  :testid="`processing-4003`"
                  width="100%"
                  multiple
                ></ChannelCascader>
                <!--<el-cascader-->
                <!--    v-model="formData.channel"-->
                <!--    :data-testid="`processing-4003`"-->
                <!--    :options="channelList"-->
                <!--    :fieldNames="{value: 'key', label: 'value'}"-->
                <!--    clearable-->
                <!--    :max-collapse-tags="1"-->
                <!--    style="width:100%"-->
                <!--    placeholder="全部"-->
                <!--    multiple-->
                <!--/>-->
              </el-form-item>
              <el-form-item
                label="条件限制"
                :content-flex="false"
                required
                :style="{ width: '924px' }"
              >
                <div style="line-height: 32px">
                  <el-radio-group
                    v-model="formData.matchingRule"
                    default-value="and"
                    :data-testid="`processing-4004`"
                  >
                    <el-radio
                      v-for="item in conditions.regulationRelations"
                      :value="item.key"
                      :key="item.key"
                    >
                      {{ item.value }}
                    </el-radio>
                  </el-radio-group>
                </div>
                <sqlCondition
                  ref="regulationConditionsRef"
                  v-model="formData.attrs"
                  :type="1"
                  :contentType="formData.contentType"
                  :processPhase="detail.processPhase"
                  :regulationType="detail.regulationType"
                  :client-id="clientId"
                ></sqlCondition>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </div>

      <div class="title mt-24">
        <span>数据量</span>
      </div>
      <div class="wrapper bg-white border mt-16 clear-top-padding">
        <el-row class="grid-demo">
          <!--未检索-->
          <el-col v-if="retrievalStatus === -1" :span="24">
            <div class="item lh-60">
              <div class="item-content flex item-center">
                <span :data-testid="`processing-6001`">-</span>
                <el-button
                  type="primary"
                  class="ml-16"
                  :data-testid="`processing-4005`"
                  @click="handleFindValidateRegulationCondition"
                  >检索
                </el-button>
              </div>
            </div>
          </el-col>
          <!--检索中-->
          <el-col v-if="retrievalStatus === 1" :span="24">
            <div class="item lh-60">
              <div class="item-content flex item-center">
                <span :data-testid="`processing-6001`">检索中...</span>
              </div>
            </div>
          </el-col>
          <el-col v-if="retrievalStatus === -2" :span="24">
            <div class="item lh-60">
              <div class="item-content flex item-center">
                <span>3299条</span>
                <el-button type="secondary" class="ml-16">检索</el-button>
              </div>
            </div>
          </el-col>
          <!--检索成功-->
          <el-col v-if="retrievalStatus === 2" :span="24">
            <div class="item lh-60">
              <div class="item-content flex item-center">
                <span :data-testid="`processing-6001`">{{ retrievalResult?.dataCount }}条</span>
                <el-button
                  type="primary"
                  class="ml-16"
                  :data-testid="`processing-4005`"
                  @click="handleFindValidateRegulationCondition"
                  >检索
                </el-button>
                <p v-if="retrievalResult?.dataCount > 1000" style="color: #f53f3f" class="ml-16">
                  校验数据量不可超过1000条，系统默认选取前1000条数据进行校验
                </p>
                <p v-if="retrievalResult?.dataCount === 0" style="color: #f53f3f" class="ml-16">
                  无满足条件的数据，请调整数据范围后重新检索
                </p>
              </div>
            </div>
          </el-col>

          <el-col v-if="retrievalStatus === 3" :span="24">
            <div class="item lh-60">
              <div class="item-content flex item-center">
                <p :data-testid="`processing-6002`" style="color: #f53f3f">检索失败</p>
                <el-button
                  type="primary"
                  class="ml-16"
                  :data-testid="`processing-4005`"
                  @click="handleFindValidateRegulationCondition"
                  >重新检索
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="drawer-footer">
        <el-button :data-testid="`processing-checkandtest-cancel`" @click="handleCancel">
          取消
        </el-button>
        <el-button
          type="primary"
          :disabled="okBtnDisabledStatus"
          :data-testid="`processing-checkandtest-confirm`"
          @click="handleOk"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script setup lang="ts">
import { inject } from 'vue'
import {
  findRegulationInfo,
  findValidateRegulationCondition,
  startTestRegulationInfo,
  startValidateRegulationInfo
} from '@/api/dataProcessing'
import { ElMessage } from 'element-plus'
import { regulationTypeStrMapping } from '@/views/dataCenter/dataProcessing/dataMapping'
import type { ConditionsDetailItem } from '@/types'
import sqlCondition from '@/views/dataCenter/dataProcessing/components/sqlCondition.vue'
import { debounce } from 'lodash-es'
import type { ValidationRequest } from '@/types/rule.types'
import { cloneDeep } from 'lodash-es'
import ChannelCascader from '@/components/ChannelCascader.vue'

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const props = withDefaults(
  defineProps<{
    // 1 校验、 2 测试
    type: 1 | 2
    id: string
    clientId: string
    // channelList: ConditionsDetailItem[]
  }>(),
  {
    type: 1,
    id: '',
    clientId: ''
  }
)

const { type, id, clientId } = toRefs(props)

const formRef = ref()
// 确定按钮 disabled状态
const okBtnDisabledStatus = ref(true)
const visible = defineModel({ required: true, default: false })
const emit = defineEmits(['refreshList'])

// const {getChannelList} = useConditions();

const regulationConditionsRef = ref()
let formData = reactive<Record<string, any>>({})
// const channelList = ref<ConditionsDetailItem[]>([])
let titleStrMapping = {
  1: '规则校验',
  2: '流程测试'
}

let typeStrMapping = {
  1: '校验',
  2: '测试'
}

const rules = reactive({
  times: [
    {
      required: true,
      message: '数据范围必填'
    }
  ],
  contentType: [
    {
      required: true,
      message: '内容格式必填'
    }
  ],
  channel: [
    {
      required: true,
      message: '数据渠道必填'
    }
  ]
})

const handleCancel = () => {
  visible.value = false
}

const handleClose = () => {
  setRetrievalStatus(-1)
  okBtnDisabledStatus.value = true
  formData.times = []
  console.log('close', formData)
}

const detail: Record<string, any> = ref({})

const getDetailById = async (id: string) => {
  const response = await findRegulationInfo({ id, clientId: clientId.value })
  if (response.code === '200') {
    return response.result
  } else {
    return {}
  }
}

// 条件限制
const regulationConditions = ref([])
const hisRegulationConditions = ref([])

const handleOpen = async () => {
  if (id.value) {
    detail.value = await getDetailById(id.value)
    hisRegulationConditions.value = cloneDeep(detail.value?.regulationConditions)
    regulationConditions.value = Object.assign([], detail.value?.regulationConditions)
    formData.attrs =
      detail.value.regulationConditions || regulationConditionsRef.value?.setDefaultValue()
    formData.contentType = detail.value.contentType
    formData.matchingRule = detail.value.matchingRule
    formData.channel = detail.value.channel

    // channelList.value = await getChannelList(detail.value.clientId) as ConditionsDetailItem[]
    // formData.contentType = detail.value.contentType
  }
}

/**
 * 检索状态
 * -1 未检索
 * 1 检索中
 * 2 检索成功
 * 3 检索失败
 */
const retrievalStatus = ref(-1)
const setRetrievalStatus = (val: number) => {
  retrievalStatus.value = val
}
/**
 * 校验页面数据 并处理请求参数
 */
const validateData = () => {
  return new Promise(resolve => {
    formRef.value?.validate(async (errs: any) => {
      if (regulationConditionsRef.value && !(await regulationConditionsRef.value?.checkData())) {
        resolve(false)
      }
      if (errs) {
        formData.startTime = formData.times[0]
        formData.endTime = formData.times[1]
        const params = {
          clientId: detail.value.clientId as string,
          startTime: formData.times[0],
          endTime: formData.times[1],
          matchingRule: formData.matchingRule,
          channel: formData.channel,
          attrs: formData.attrs,
          contentType: formData.contentType,
          validRuleIds: (detail.value.id && [detail.value.id]) || []
        }
        resolve(params)
      }
      resolve(false)
    })
  })
}

const retrievalResult = ref<any>({})
/**
 * 检索
 */
const handleFindValidateRegulationCondition = debounce(async () => {
  const params = await validateData()
  if (params) {
    setRetrievalStatus(1)
    findValidateRegulationCondition(params as ValidationRequest)
      .then(res => {
        if (res.code === '200') {
          retrievalResult.value = res.result
          setRetrievalStatus(2)
          if (res.result.dataCount > 0) {
            okBtnDisabledStatus.value = false
          } else {
            okBtnDisabledStatus.value = true
          }
        } else {
          setRetrievalStatus(3)
        }
      })
      .catch(() => {
        setRetrievalStatus(3)
      })
  }
}, 300)

// 当校验条件（时间、格式、渠道、条件）发生变化时，检索状态需重置
watch(
  [
    () => formData.matchingRule,
    () => formData.attrs,
    () => formData.channel,
    () => formData.contentType,
    () => formData.times
  ],
  () => {
    setRetrievalStatus(-1)
  },
  {
    deep: true
  }
)

const handleOk = debounce(async () => {
  const params = await validateData()
  if (params) {
    const api = type.value === 1 ? startValidateRegulationInfo : startTestRegulationInfo
    api(params as ValidationRequest)
      .then(res => {
        if (res.code === '200') {
          visible.value = false
          emit('refreshList')
        } else {
          ElMessage.error(res.message)
        }
      })
      .catch((err: any) => {
        ElMessage.error(err.message)
      })
  }
}, 300)

// const refreshList = () => {
//   emit('refreshList')
// }
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

    &.clear-padding {
      padding: 0;
    }

    &.border {
      border: 1px solid #e5e6eb;
    }

    &.bg-white {
      background-color: #fff;
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

      &.lh-60 {
        margin-bottom: 0;
        box-sizing: border-box;

        .item-label,
        .item-content {
          line-height: 60px;
        }
      }

      .item-label {
        font-weight: 400;
        font-size: 14px;
        color: #86909c;
        line-height: 22px;
        flex: none;
        display: flex;
        align-items: center;

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

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  background: #fff;
}
</style>
