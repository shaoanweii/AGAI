<template>
  <el-drawer
    :size="1200"
    v-model="form.visible"
    @close="handleClose"
    @open="handleOpen"
    :data-testid="`processing-3011`"
    destroy-on-close
  >
    <template #header>
      <div>
        {{ titleStr }}
      </div>
    </template>
    <el-form :model="formData" :rules="rules" auto-label-width ref="formRef">
      <!--<el-form-item prop="clientId" label="应用客户" :style="{ width: '884px' }">-->
      <!--  <el-select v-model="formData.clientId" :disabled="form.operation == 'edit'" placeholder="全部" clearable :data-testid="`processing-3001`"-->
      <!--            style="width: 100%" @change="handleClientChange">-->
      <!--    <el-option v-for="item in filterClient" :label="item.value" :value="item.key"/>-->
      <!--  </el-select>-->
      <!--</el-form-item>-->
      <el-form-item prop="name" label="规则名称" :style="{ width: '884px' }">
        <el-input
          v-model.trim="formData.name"
          placeholder="请输入"
          :maxlength="50"
          clearable
          :data-testid="`processing-3002`"
        />
      </el-form-item>
      <el-form-item label="规则描述" :style="{ width: '884px' }">
        <el-input
          type="textarea"
          v-model.trim="formData.description"
          :data-testid="`processing-3003`"
          placeholder="请输入"
          :maxlength="200"
          :auto-size="{
            minRows: 4,
            maxRows: 5
          }"
          show-word-limit
          clearable
        />
      </el-form-item>
      <!-- <el-form-item prop="processPhase" label="处理阶段" :style="{ width: '884px' }">
        <el-radio-group
          v-model="formData.processPhase"
          default-value="and"
          :data-testid="`processing-3004`"
          @change="() => handleProcessPhaseChange()"
        >
          <el-radio
            v-for="item in conditions.regulationStage"
            :disabled="item.key === 'pre'"
            :value="item.key"
            :key="item.key"
            >{{ item.value }}
          </el-radio>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item prop="regulationType" label="规则类型" :style="{ width: '884px' }">
        <el-radio-group
          v-model="formData.regulationType"
          default-value="and"
          :data-testid="`processing-3005`"
          @change="(val) => regulationTypeChange(val as string)"
        >
          <el-radio v-for="item in ruleType" :value="item.key" :key="item.key"
            >{{ item.value }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <!-- <el-form-item prop="contentType" label="内容格式" :style="{ width: '884px' }">
        <el-radio-group
          v-model="formData.contentType"
          default-value="and"
          :data-testid="`processing-3006`"
        >
          <el-radio
            v-for="item in conditions.regulationContentType"
            :disabled="item.key === 'order'"
            :value="item.key"
            :key="item.key"
            >{{ item.value }}
          </el-radio>
          <el-radio disabled>对话</el-radio>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item prop="channel" label="数据渠道" :style="{ width: '884px' }">
        <ChannelCascader
          v-model="formData.channel"
          controller="regulation"
          :testid="`processing-3007`"
          width="100%"
          multiple
        ></ChannelCascader>
        <!--<el-cascader-->
        <!--    v-model="formData.channel"-->
        <!--    :data-testid="`processing-3007`"-->
        <!--    :options="channelList"-->
        <!--    :fieldNames="{value: 'key', label: 'value'}"-->
        <!--    clearable-->
        <!--    :max-collapse-tags="1"-->
        <!--    style="width:100%"-->
        <!--    placeholder="全部"-->
        <!--    multiple-->
        <!--/>-->
      </el-form-item>
      <el-form-item label="条件限制" :content-flex="false" required :style="{ width: '924px' }">
        <div style="line-height: 32px">
          <el-radio-group
            v-model="formData.matchingRule"
            default-value="and"
            :data-testid="`processing-3008`"
          >
            <el-radio
              v-for="item in conditions.regulationRelations"
              :value="item.key"
              :key="item.key"
              >{{ item.value }}
            </el-radio>
          </el-radio-group>
        </div>
        <sqlCondition
          ref="regulationConditionsRef"
          v-model="formData.regulationConditions"
          :type="1"
          :contentType="formData.contentType"
          :processPhase="formData.processPhase"
          :regulationType="formData.regulationType"
          :clientId="formData.clientId"
        ></sqlCondition>
      </el-form-item>
      <el-form-item :content-flex="false" label="执行动作" required :style="{ width: '924px' }">
        <div style="line-height: 32px">
          <el-radio-group
            v-model="formData.regulationType"
            default-value="and"
            :data-testid="`processing-3009`"
          >
            <el-radio v-for="item in actionType" :value="item.key" :key="item.key">
              {{ item.value }} {{ regulationTypeStrMapping[(formData as any).regulationType] }}
            </el-radio>
          </el-radio-group>
          <sqlCondition
            v-if="formData.regulationType != NOT_ACTION"
            ref="regulationPerformActionRef"
            v-model="formData.regulationPerformAction"
            :type="2"
            :contentType="formData.contentType"
            :processPhase="formData.processPhase"
            :regulationType="formData.regulationType"
            :clientId="formData.clientId"
          ></sqlCondition>
        </div>
      </el-form-item>
      <el-form-item label="权重" required :style="{ width: '884px' }">
        <el-select
          v-model="formData.regulationWeight"
          placeholder="全部"
          :data-testid="`processing-3010`"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="(item, index) in conditions.ruleWeight"
            :key="index"
            :data-testid="`processing-3010-op-${index}`"
            :label="item.key"
            :value="item.key"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script setup lang="ts">
import { inject } from 'vue'
import type { Form } from '@/hooks/table.d'
import { ElMessage } from 'element-plus'
import type { ConditionsDetailItem } from '@/types'
import sqlCondition from '@/views/dataCenter/dataProcessing/components/sqlCondition.vue'
import { findRegulationInfo, saveRegulationInfo, updateRegulationInfo } from '@/api/dataProcessing'
import { regulationTypeStrMapping } from '@/views/dataCenter/dataProcessing/dataMapping'
import ChannelCascader from '@/components/ChannelCascader.vue'
import to from 'await-to-js'

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshList'])
const props = withDefaults(
  defineProps<{
    // channelList: ConditionsDetailItem[]
    filterClient: ConditionsDetailItem[]
    clientId: string
  }>(),
  {
    // channelList: () => [],
    filterClient: () => []
  }
)

// 规则类型为过滤(R05)的时候 不需要执行动作
const NOT_ACTION = 'R05'
const rules = reactive({
  clientId: [
    {
      required: true,
      message: '应用客户必填'
    }
  ],
  name: [
    {
      required: true,
      message: '规则名称必填'
    }
  ],
  // processPhase: [
  //   {
  //     required: true,
  //     message: '处理阶段必填'
  //   }
  // ],
  regulationType: [
    {
      required: true,
      message: '规则类型必填'
    }
  ],
  // contentType: [
  //   {
  //     required: true,
  //     message: '内容格式必填'
  //   }
  // ],
  channel: [
    {
      required: true,
      message: '数据渠道必填'
    }
  ],
  matchingRule: [
    {
      required: true,
      message: '条件限制必填'
    }
  ]
})
const form = inject('form') as Form
const formRef = ref()
let formData = reactive<Record<string, any>>({
  // regulationConditions: [] // 条件限制
  // regulationPerformAction: [] // 执行动作
})
let titleStr = ref('')

/**
 * 规则类型下拉数据
 */
const ruleType = computed(() => {
  if (formData.processPhase === 'pre') {
    return conditions.regulationPreType
  } else if (formData.processPhase === 'post') {
    return conditions.regulationPostType
  } else {
    return []
  }
})

const actionType = computed(() => {
  return ruleType.value?.filter((el: ConditionsDetailItem) => el.key === formData.regulationType)
})

// const {getChannelList} = useConditions();
// 渠道数据
// const channelOptions = ref<ConditionsDetailItem[] | undefined>([])

/**
 * 处理阶段CHANGE
 */
const handleProcessPhaseChange = () => {
  // 处理阶段切换以后 规则类型如果没有对应的类型 重置为默认类型
  const result = ruleType.value?.find(el => el.key === formData.regulationType)
  if (!result) {
    formData.regulationType = 'R05'
  }
}

/**
 * 应用客户CHANGE
 */
const handleClientChange = async () => {
  // channelOptions.value = await getChannelList(formData.clientId)
}

const regulationConditionsRef = ref()
const regulationPerformActionRef = ref()

const initializeFormData = async () => {
  console.log('form.data', form.data)
  if (form.operation == 'add') {
    Object.assign(formData, form.data)
    titleStr.value = '新建规则'
    formData.clientId = props.clientId
    formData.processPhase = 'post'
    formData.contentType = 'text'
    formData.regulationPerformAction = null
    formData.regulationConditions = regulationConditionsRef.value?.setDefaultValue()
    formData.matchingRule = 'and'
    formData.regulationWeight = 'E'
    // channelOptions.value = props.channelList
    formData.regulationType = 'R05'
  } else if (form.operation == 'edit') {
    titleStr.value = '编辑规则'
    formData.clientId = form.data.clientId
    const [err, response] = await to(
      findRegulationInfo({ id: form.data.id, clientId: form.data.clientId })
    )
    if (err) {
      console.error('获取规则信息失败:', err)
      ElMessage.error(err.message || '获取规则信息失败')
      return
    }
    if (response && response.code === '200') {
      Object.assign(formData, response.result)
    }
    handleClientChange()
  }
}

const regulationTypeChange = (value: string) => {
  if (value != NOT_ACTION) {
    nextTick(() => {
      formData.regulationPerformAction = regulationPerformActionRef.value?.setDefaultValue()
    })
  } else {
    formData.regulationPerformAction = null
  }
}

const handleCancel = () => {
  form.visible = false
}

const handleClose = () => {
  Object.keys(formData).forEach(key => {
    formData[key] = null
  })
}

const handleOpen = () => {
  console.log('弹窗打开，form对象:', form)
  console.log('conditions对象:', conditions)
  initializeFormData()
}

const handleOk = () => {
  console.log('确定按钮被点击')
  console.log('formRef.value:', formRef.value)
  console.log('formData:', formData)

  // 表单基础验证
  formRef.value?.validate(async (errs: any) => {
    if (!errs) {
      console.log('表单验证失败:', errs)
      return
    }
    console.log('表单验证通过')

    // 条件限制验证
    if (regulationConditionsRef.value) {
      const [conditionsErr, isConditionsValid] = await to(
        regulationConditionsRef.value?.checkData()
      )
      if (conditionsErr || !isConditionsValid) {
        console.log('条件限制验证失败')
        ElMessage.error('请检查条件限制配置')
        return
      }
    }

    // 执行动作验证（仅当规则类型不是过滤时）
    if (formData.regulationType !== NOT_ACTION && regulationPerformActionRef.value) {
      const [actionErr, isActionValid] = await to(regulationPerformActionRef.value?.checkData())
      if (actionErr || !isActionValid) {
        console.log('执行动作验证失败')
        ElMessage.error('请检查执行动作配置')
        return
      }
    }

    // 准备提交数据
    const params = {
      ...formData
    }
    console.log('提交参数:', params)

    // 调用API
    const api = form.operation === 'add' ? saveRegulationInfo : updateRegulationInfo
    const [err, res] = await to(api(params))

    if (err) {
      console.error('提交失败:', err)
      ElMessage.error(err.message || '操作失败，请稍后重试')
      return
    }

    if (res.code === '200') {
      ElMessage.success(form.operation === 'add' ? '新建成功' : '更新成功')
      form.visible = false
      emit('refreshList')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  })
}
</script>
