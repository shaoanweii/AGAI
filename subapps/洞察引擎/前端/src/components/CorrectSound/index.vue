<template>
  <el-drawer class="soundDetail" :size="950" v-model="visible" @open="handleOpen" destroy-on-close>
    <template #header>
      <span class="fd-title">数据纠错</span>
    </template>
    <div>
      <div class="mt-24 font-16 lh-24 color-4b5468 bgc-def textContent">
        {{ dataInfo.originalText }}
        <div class="font-14 flex mt-15">
          <div class="py-5 px-10 bg-c8e2fa color-0082d6 radius-4 mr-10">
            {{ dataInfo.tagText }}
          </div>
          <div class="py-5 px-10 bg-c8e2fa color-0082d6 radius-4 mr-10">
            {{ dataInfo.sentiment }}
          </div>
          <div class="py-5 px-10 bg-c8e2fa color-0082d6 radius-4 mr-10">
            {{ dataInfo.intention }}
          </div>
          <div class="py-5 px-10 bg-c8e2fa color-0082d6 radius-4 mr-10">
            {{ dataInfo.topic }}
          </div>
        </div>
      </div>
    </div>
    <div class="mt-24">
      <el-form
        v-if="table.list.length === 0 || showCorrect"
        ref="formRef"
        :model="roleModel"
        :rules="formRules"
        auto-label-width
      >
        <!-- 错误类型 1无效数据 2有效数据 -->
        <el-form-item label="错误类型" prop="errorType" class="align-baseline">
          <el-radio-group v-model="roleModel.errorType" direction="vertical">
            <el-radio :value="1">无效声音</el-radio>
            <div class="arco-form-item-extra">声音不该标记任何标签分类</div>
            <el-radio :value="2">有效声音</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="roleModel.errorType === 2" style="margin-left: 100px">
          <el-row>
            <el-col :span="8">
              <el-form-item label="观点是否正确" prop="topicSelect" class="align-baseline">
                <el-radio-group v-model="roleModel.topicSelect">
                  <el-radio :value="1">正确</el-radio>
                  <el-radio :value="2">错误</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col v-if="roleModel.topicSelect === 2" :span="8">
              <el-form-item prop="topic" class="align-baseline">
                <el-input v-model="roleModel.topic" placeholder="请输入正确观点" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <!-- 选中状态 1正确 2错误 -->
              <el-form-item label="分类是否正确" prop="tagSelect" class="align-baseline">
                <el-radio-group v-model="roleModel.tagSelect">
                  <el-radio :value="1">正确</el-radio>
                  <el-radio :value="2">错误</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col v-if="roleModel.tagSelect === 2" :span="8">
              <el-form-item prop="tag" class="align-baseline">
                <el-cascader
                  v-model="roleModel.tag"
                  path-mode
                  :options="tagListOptions"
                  :style="{ width: '320px' }"
                  :props="{ label: 'tagName', value: 'id', children: 'child' }"
                  placeholder="请选择正确分类"
                  @change="handleTagChange"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <!-- <el-form-item prop="tag" class="align-baseline">
            <el-cascader
              v-model="roleModel.tag"
              path-mode
              :options="tagListOptions"
              :style="{ width: '320px' }"
              :props="{ label: 'tagName', value: 'id', children: 'child' }"
              placeholder="请选择正确分类"
              @change="handleTagChange"
            />
          </el-form-item> -->
          <el-row>
            <el-col :span="8">
              <el-form-item label="情感是否正确" prop="sentimentSelect" class="align-baseline">
                <!-- 选中状态 1正确 2错误 -->
                <el-radio-group v-model="roleModel.sentimentSelect">
                  <el-radio :value="1">正确</el-radio>
                  <el-radio :value="2">错误</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col v-if="roleModel.sentimentSelect === 2" :span="8">
              <el-form-item prop="sentiment" class="align-baseline">
                <el-select
                  v-model="roleModel.sentiment"
                  :options="conditionData.conditions.emotion"
                  placeholder="请选择正确情感"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item label="意图是否正确" prop="intentionSelect" class="align-baseline">
                <el-radio-group v-model="roleModel.intentionSelect">
                  <el-radio :value="1">正确</el-radio>
                  <el-radio :value="2">错误</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col v-if="roleModel.intentionSelect === 2" :span="8">
              <el-form-item prop="intention" class="align-baseline">
                <el-select
                  v-model="roleModel.intention"
                  :options="conditionData.conditions.intention"
                  placeholder="请选择正确意图"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <!-- <el-form-item label="意图是否正确" prop="intentionSelect" class="align-baseline">
            <el-radio-group v-model="roleModel.intentionSelect">
              <el-radio :value="1">正确</el-radio>
              <el-radio :value="2">错误</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="intention" class="align-baseline">
            <el-select v-model="roleModel.intention" :options="conditionData.conditions.intention" />
          </el-form-item>
        </div> -->
      </el-form>
      <div v-else class="correct-table-wrapper">
        <div class="flex-justify-end mb-10">
          <el-button type="primary" class="acro-btn-def" @click="toCorrect">我要纠错</el-button>
        </div>
        <el-table
          :data="table.list"
          :loading="table.loading"
          :pagination="{
            current: table.pageNum,
            pageSize: table.pageSize,
            total: table.total,
            showTotal: true,
            showPageSize: true,
            pageSizeOptions: [10, 20, 50, 100]
          }"
          @page-change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        >
          <template #columns>
            <el-table-column title="纠错时间" data-index="view" :size="190">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.operateTime }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="纠错人" data-index="view" :size="100">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.operateUser }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="观点" data-index="view" :size="160">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.topic }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="分类" data-index="view" :size="180">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.textLabel }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="情感" data-index="view" :size="100">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.sentiment }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="意图" data-index="view" :size="100">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.intentionType }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="错误类型" data-index="view" :size="100">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.errorTypeText }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="纠错明细" data-index="view" :size="200">
              <template #cell="{ record }">
                <div class="">
                  <pre>{{ record.correctionInfo }}</pre>
                </div>
              </template>
            </el-table-column>
            <el-table-column title="状态" data-index="view" :size="100">
              <template #cell="{ record }">
                <div class="">
                  <span>{{ record.auditStatusText }}</span>
                </div>
              </template>
            </el-table-column>
          </template>
        </el-table>
      </div>
    </div>
    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">提交纠错</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import to from 'await-to-js'
import {
  insertLabelCorrection,
  queryDataInfo,
  findTagList,
  getLabelHistoryRecordList
} from '@/api/correct'
import { ElMessage } from 'element-plus'
import useUserStore from '@/stores/modules/user'
import useConditions from '@/hooks/useConditions'

interface Props {
  record: any
}

const props = defineProps<Props>()

const visible = defineModel({ default: false })

const dataInfo = ref<Record<any, any>>({})

const userStore = useUserStore()

const conditionData = useConditions({
  url: '/insights/addLabel/conditions',
  params: { clientId: userStore.clientId }
})

const tagListOptions = ref<Record<any, any>[]>([])
const tagSelectArray = ref()

const formRef = ref()

const roleModel = ref<Record<any, any>>({
  errorType: undefined,
  topicSelect: undefined,
  tagSelect: undefined,
  sentimentSelect: undefined,
  intentionSelect: undefined,
  topic: '',
  tag: [],
  sentiment: '',
  intention: ''
})

// 表单校验规则
const formRules = {
  errorType: [
    {
      validator: (value: any, callback: any) => {
        if (!value) {
          callback('请选择错误类型')
        } else {
          callback()
        }
      },
      trigger: ['change'],
      required: true
    }
  ],
  topicSelect: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && (value === undefined || value === null)) {
          callback('请选择观点是否正确')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ],
  topic: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && roleModel.value.topicSelect === 2 && !value) {
          callback('请输入正确观点')
        } else {
          callback()
        }
      },
      trigger: ['change', 'blur']
    }
  ],
  tagSelect: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && (value === undefined || value === null)) {
          callback('请选择分类是否正确')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ],
  tag: [
    {
      validator: (value: any, callback: any) => {
        if (
          roleModel.value.errorType === 2 &&
          roleModel.value.tagSelect === 2 &&
          (!value || value.length === 0)
        ) {
          callback('请选择正确分类')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ],
  sentimentSelect: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && (value === undefined || value === null)) {
          callback('请选择情感是否正确')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ],
  sentiment: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && roleModel.value.sentimentSelect === 2 && !value) {
          callback('请选择正确情感')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ],
  intentionSelect: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && (value === undefined || value === null)) {
          callback('请选择意图是否正确')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ],
  intention: [
    {
      validator: (value: any, callback: any) => {
        if (roleModel.value.errorType === 2 && roleModel.value.intentionSelect === 2 && !value) {
          callback('请选择正确意图')
        } else {
          callback()
        }
      },
      trigger: ['change']
    }
  ]
}

const table = ref<any>({
  list: [],
  loading: false,
  total: 0,
  pageNum: 1,
  pageSize: 10
})

const showCorrect = ref(true)

const handleOpen = async () => {
  // 重置表单数据
  roleModel.value = {
    errorType: undefined,
    topicSelect: undefined,
    tagSelect: undefined,
    sentimentSelect: undefined,
    intentionSelect: undefined,
    topic: '',
    tag: [],
    sentiment: '',
    intention: ''
  }
  showCorrect.value = true
  getDataInfo()
  getTagList()
  getLabelHistoryRecordListData()
}

const getDataInfo = async () => {
  table.value.loading = true
  const [errs, res] = await to(
    queryDataInfo({ clientId: userStore.clientId, newId: props.record.newId })
  )
  if (errs) {
    ElMessage.error(errs.message)
  }

  // 修复：res 可能为 undefined，需做判断
  if (res && res.result) {
    dataInfo.value = res.result
  } else {
    table.value.list = []
  }
  table.value.loading = false
}

const getLabelHistoryRecordListData = async () => {
  table.value.loading = true
  const [errs, res] = await to(
    getLabelHistoryRecordList({
      clientId: userStore.clientId,
      newId: props.record.newId,
      pageNum: table.value.pageNum,
      pageSize: table.value.pageSize
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
  }
  if (res && res.result) {
    table.value.list = res.result.list
    table.value.total = res.result.total
    table.value.pageNum = res.result.pageNum
    table.value.pageSize = res.result.pageSize
    if (res.result.list.length > 0) {
      showCorrect.value = false
    }
  } else {
    table.value.list = []
    table.value.total = 0
    table.value.pageNum = 1
    table.value.pageSize = 10
  }
  table.value.loading = false
}
const getTagList = async () => {
  const [errs, res] = await to(findTagList({ clientId: userStore.clientId }))
  if (errs) {
    ElMessage.error(errs.message)
  }

  // 修复：res 可能为 undefined，需做判断
  if (res && res.result) {
    tagListOptions.value = res.result
  } else {
    tagListOptions.value = []
  }
}
/**
 * @description: 获取用户轨迹
 * @return {*}
 */

const handleOk = async () => {
  // 使用表单校验
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      return
    }
  } catch (error) {
    console.log('校验错误：', error)
    ElMessage.error('请完善必填信息')
    return
  }

  // 检查当 errorType === 2 且所有radio都选择1时的情况
  if (
    roleModel.value.errorType === 2 &&
    roleModel.value.topicSelect === 1 &&
    roleModel.value.tagSelect === 1 &&
    roleModel.value.sentimentSelect === 1 &&
    roleModel.value.intentionSelect === 1
  ) {
    ElMessage.error('您填写的信息与原内容完全一致，无需纠错。请检查并修改后再提交。')
    return
  }

  let tagParams = {}

  // 只有当tagSelect为2且tag有值时才构建tagParams
  if (
    roleModel.value.errorType === 2 &&
    roleModel.value.tagSelect === 2 &&
    tagSelectArray.value &&
    tagSelectArray.value.length > 0
  ) {
    tagParams = {
      LabelType: tagSelectArray.value[tagSelectArray.value.length - 1].tagType,
      labelTypeLevelFirst: tagSelectArray.value[1]?.tagName || '',
      labelTypeLevelSecond: tagSelectArray.value[2]?.tagName || '',
      labelTypeLevelThree: tagSelectArray.value[3]?.tagName || '',
      labelTypeLevelFour: tagSelectArray.value[4]?.tagName || '',
      labelTypeLevelFive: tagSelectArray.value[5]?.tagName || '',
      labelTypeLevelFirstCode: tagSelectArray.value[1]?.tagCode || '',
      labelTypeLevelSecondCode: tagSelectArray.value[2]?.tagCode || '',
      labelTypeLevelThreeCode: tagSelectArray.value[3]?.tagCode || '',
      labelTypeLevelFourCode: tagSelectArray.value[4]?.tagCode || '',
      labelTypeLevelFiveCode: tagSelectArray.value[5]?.tagCode || ''
    }
  }

  let params = {
    newId: props.record.newId,
    clientId: userStore.clientId,
    ...roleModel.value,
    ...tagParams
  }

  // 删除tag属性，因为我们使用的是拆解后的标签参数
  if ('tag' in params) {
    delete params.tag
  }

  const [errs, res] = await to(insertLabelCorrection(params))
  if (errs) {
    ElMessage.error(errs.message)
    return
  }
  if (res) {
    ElMessage.success('纠错成功')
    visible.value = false
    // 重新获取历史记录
    getLabelHistoryRecordListData()
  }
}

const handleCancel = () => {
  visible.value = false
}

const handleTagChange = (value: any) => {
  // 根据选中的值，从 tagListOptions 中找到对应的完整数据
  const getSelectedOptionsData = (options: any[], values: any[], level = 0): any[] => {
    if (!values || !values[level]) return []

    const currentValue = values[level]
    const currentOption = options.find(option => option.id === currentValue)

    if (!currentOption) return []

    const result = [currentOption]

    // 如果还有下一级，递归查找
    if (values[level + 1] && currentOption.child) {
      result.push(...getSelectedOptionsData(currentOption.child, values, level + 1))
    }

    return result
  }

  if (value && value.length > 0) {
    const selectedOptionsData = getSelectedOptionsData(tagListOptions.value, value)
    tagSelectArray.value = selectedOptionsData
  }
}

const toCorrect = () => {
  showCorrect.value = true
}

const handlePageChange = (page: number) => {
  table.value.pageNum = page
  getLabelHistoryRecordListData()
}

const handlePageSizeChange = (pageSize: number) => {
  table.value.pageSize = pageSize
  table.value.pageNum = 1 // 重置到第一页
  getLabelHistoryRecordListData()
}

// 监听errorType变化，重新校验相关字段
watch(
  () => roleModel.value.errorType,
  () => {
    nextTick(() => {
      if (formRef.value) {
        // 清除之前的校验错误
        formRef.value.clearValidate()
        // 重新校验所有字段
        if (roleModel.value.errorType === 2) {
          formRef.value.validateField([
            'topicSelect',
            'tagSelect',
            'sentimentSelect',
            'intentionSelect'
          ])
        }
      }
    })
  }
)

// 监听各个Select字段变化，重新校验对应的输入字段
watch(
  () => roleModel.value.topicSelect,
  () => {
    nextTick(() => {
      if (formRef.value && roleModel.value.topicSelect === 2) {
        formRef.value.validateField('topic')
      }
    })
  }
)

watch(
  () => roleModel.value.tagSelect,
  () => {
    nextTick(() => {
      if (formRef.value && roleModel.value.tagSelect === 2) {
        formRef.value.validateField('tag')
      }
    })
  }
)

watch(
  () => roleModel.value.sentimentSelect,
  () => {
    nextTick(() => {
      if (formRef.value && roleModel.value.sentimentSelect === 2) {
        formRef.value.validateField('sentiment')
      }
    })
  }
)

watch(
  () => roleModel.value.intentionSelect,
  () => {
    nextTick(() => {
      if (formRef.value && roleModel.value.intentionSelect === 2) {
        formRef.value.validateField('intention')
      }
    })
  }
)
</script>

<style lang="scss">
.soundDetail {
  .el-drawer-body {
    padding: 14px 24px 24px;
  }

  .textContent {
    min-height: 200px;
    max-height: 300px;
    overflow-y: auto;
    padding: 20px;
  }
  .bgc-def {
    background-color: #f0f3fa;
  }
  .color-4b5468 {
    color: #4b5468;
  }
  .font-16 {
    font-size: 16px;
  }
  .lh-24 {
    line-height: 24px;
  }
  .mt-15 {
    margin-top: 15px;
  }
  .font-14 {
    font-size: 14px;
  }
  .flex {
    display: flex;
  }
  .py-5 {
    padding-top: 5px;
    padding-bottom: 5px;
  }
  .px-10 {
    padding-left: 10px;
    padding-right: 10px;
  }
  .radius-4 {
    border-radius: 4px;
  }
  .mr-10 {
    margin-right: 10px;
  }
  .bg-c8e2fa {
    background-color: #c8e2fa;
  }
  .color-0082d6 {
    color: #0082d6;
  }
  .flex-justify-end {
    display: flex;
    justify-content: flex-end;
  }
  // 保持表单样式
  .el-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .align-baseline {
      align-items: baseline;
      .el-form-item-label-col {
        width: 0px !important;
      }
    }
  }
}
</style>
