<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FtCard from '@/components/FtCard.vue'
import FFilterLayout from '@/components/UI/FFilterLayout/index.vue'
import { computedCardHeight } from '@/utils'

defineOptions({
  name: 'rules-cleaningRules'
})

type RuleStatus = '1' | '0'

interface CleaningRule {
  id: string
  ruleName: string
  ruleType: string
  channels: string[]
  weight: string
  createTime: string
  operator: string
  status: RuleStatus
  conditionMode: 'AND' | 'OR'
  field: string
  valueType: string
  conditionValue: string
  action: string
}

const ruleTypeOptions = ['清洗', '过滤', '补充', '修改']
const weightOptions = ['A', 'B', 'C']
const operatorOptions = ['*雯婷-6320824', '王集福-6322321', '6322321']
const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' }
]

const publicChannels = [
  '抖音',
  '快手',
  '微博',
  '微信公众号',
  '微信视频号',
  '小红书',
  '一点资讯',
  'B站',
  '今日头条',
  '百度知道',
  '百家号',
  '百度贴吧',
  '百度有驾',
  '腾讯微视',
  '腾讯视频',
  '腾讯新闻',
  '网易号',
  '网易新闻',
  '网易汽车',
  '网易视频',
  '新浪看点',
  '新浪新闻',
  '新浪汽车',
  '搜狐视频',
  '搜狐新闻APP',
  '搜狐文章',
  '汽车之家-文章',
  '汽车之家-车家号',
  '汽车之家-论坛',
  '汽车之家-口碑评分',
  '汽车之家-视频',
  '汽车之家-口碑描述',
  '懂车帝-车友圈',
  '懂车帝-口碑评分',
  '懂车帝-视频',
  '懂车帝-口碑描述',
  '懂车帝-文章',
  '易车-社区',
  '易车-视频',
  '易车-口碑评分',
  '易车-文章',
  '易车号',
  '易车-口碑描述',
  '太平洋汽车-论坛',
  '太平洋号',
  '太平洋汽车-文章',
  '太平洋汽车-视频',
  '新出行-社区',
  '新出行-视频',
  '新出行-文章',
  '爱咖号',
  '爱卡汽车-社区',
  '爱卡汽车-文章',
  '爱卡汽车-视频',
  '车主之家',
  '车质网-新闻',
  '车质网-投诉',
  '车质网-论坛',
  '车质网-答疑',
  '车质网-口碑描述',
  '汽车投诉网-投诉',
  '汽车投诉网-资讯',
  '汽车召回网-投诉',
  '汽车召回网-汽车资讯',
  '汽车门网-新闻',
  '汽车门网-投诉',
  '中国汽车质量网-投诉',
  '中国汽车质量网-新闻',
  '黑猫投诉-投诉',
  '知乎',
  '中国网汽车-投诉',
  '中国新闻网',
  '21财经',
  '东方财富网-股吧',
  '雪球',
  '北青网',
  '人民网',
  '央视频',
  '学习强国',
  '其他',
  '爱奇艺'
]

const privateChannels = [
  '领航汽车-热线服务',
  '领航品牌-热线服务',
  '启程品牌-热线服务',
  '商用品牌-热线服务',
  '星海汽车-热线服务',
  '高端品牌-热线服务',
  '启程品牌-留言板',
  '高端品牌-留言板',
  '星海汽车-留言板',
  '领航品牌-留言板',
  '商用品牌-留言板',
  '高端品牌-在线对话',
  '启程品牌-在线对话',
  '商用品牌-在线对话',
  '领航品牌-在线对话',
  '星海汽车-在线对话',
  '领航品牌直评',
  '客服公司直评',
  '高端品牌-直评（体验随手评）',
  '星海汽车直评',
  '车和美直评',
  '启程品牌直评',
  '集团智慧营销直评',
  '商用汽车直评',
  '领航品牌-三包工单',
  '商用品牌-三包工单',
  '启程品牌-三包工单',
  '星海汽车-三包工单',
  '高端品牌-DMS工单',
  '车机端-意见反馈（梧桐）',
  '车机端-意见反馈（SDA）',
  '车机端-意见反馈（华为）',
  '星海汽车APP/小程序-社区',
  '星海汽车APP/小程序-意见反馈',
  '高端品牌APP/小程序-社区',
  '高端品牌APP/小程序-意见反馈',
  '高端品牌APP/小程序-资讯（评论）',
  '启程品牌APP/小程序-意见反馈',
  '启程品牌APP/小程序-社区',
  '启程品牌APP/小程序-资讯（评论）',
  '品牌社区-帖子',
  '品牌社区-资讯（评论）',
  '品牌社区-意见反馈',
  '商用品牌APP/小程序-意见反馈',
  '商用品牌APP/小程序-资讯（评论）',
  '商用品牌APP/小程序-圈子',
  'TopspaceAPP/小程序-意见反馈',
  'TopspaceAPP/小程序-新鲜事',
  '高端品牌门店之声',
  '高端品牌塔迷圈',
  '线下重点渠道'
]
const channelOptions = [...publicChannels, ...privateChannels]

const defaultRules: CleaningRule[] = [
  {
    id: '1',
    ruleName: '提车广告',
    ruleType: '过滤',
    channels: publicChannels,
    weight: 'A',
    createTime: '2026-06-04 10:13:35',
    operator: '*雯婷-6320824',
    status: '1',
    conditionMode: 'AND',
    field: '内容',
    valueType: '通配符',
    conditionValue: '*提车*广告*',
    action: '过滤命中内容'
  },
  {
    id: '2',
    ruleName: '过滤2',
    ruleType: '过滤',
    channels: publicChannels,
    weight: 'A',
    createTime: '2026-06-01 10:18:48',
    operator: '王集福-6322321',
    status: '0',
    conditionMode: 'AND',
    field: '标题',
    valueType: '通配符',
    conditionValue: '*无效*',
    action: '过滤'
  },
  {
    id: '3',
    ruleName: '过滤1',
    ruleType: '过滤',
    channels: privateChannels,
    weight: 'A',
    createTime: '2026-06-01 10:18:10',
    operator: '王集福-6322321',
    status: '0',
    conditionMode: 'AND',
    field: '内容',
    valueType: '通配符',
    conditionValue: '*测试*',
    action: '过滤'
  },
  {
    id: '4',
    ruleName: '过滤公域数据',
    ruleType: '清洗',
    channels: publicChannels,
    weight: 'A',
    createTime: '2026-06-01 10:03:00',
    operator: '王集福-6322321',
    status: '1',
    conditionMode: 'AND',
    field: '数据渠道',
    valueType: '包含',
    conditionValue: '公域',
    action: '清洗公域数据'
  },
  {
    id: '5',
    ruleName: '清洗私域内容带html标签',
    ruleType: '清洗',
    channels: privateChannels,
    weight: 'A',
    createTime: '2026-05-26 10:23:46',
    operator: '王集福-6322321',
    status: '1',
    conditionMode: 'OR',
    field: '原文',
    valueType: '正则',
    conditionValue: '<[^>]+>',
    action: '清洗HTML标签内容'
  },
  {
    id: '6',
    ruleName: '匹配内容有领导人数据补充为非水军',
    ruleType: '补充',
    channels: privateChannels,
    weight: 'A',
    createTime: '2026-05-26 10:05:26',
    operator: '王集福-6322321',
    status: '1',
    conditionMode: 'AND',
    field: '内容',
    valueType: '包含',
    conditionValue: '领导人',
    action: '补充非水军标识'
  },
  {
    id: '7',
    ruleName: '匹配领导人账号数据补充为非水军',
    ruleType: '补充',
    channels: privateChannels,
    weight: 'A',
    createTime: '2026-05-26 09:57:30',
    operator: '王集福-6322321',
    status: '1',
    conditionMode: 'AND',
    field: '账号标签',
    valueType: '包含',
    conditionValue: '领导人',
    action: '补充非水军标识'
  },
  {
    id: '8',
    ruleName: '过滤私域内容带html标签',
    ruleType: '过滤',
    channels: privateChannels,
    weight: 'A',
    createTime: '2026-05-26 09:57:26',
    operator: '王集福-6322321',
    status: '1',
    conditionMode: 'OR',
    field: '原文',
    valueType: '正则',
    conditionValue: '<[^>]+>',
    action: '过滤HTML标签内容'
  },
  {
    id: '9',
    ruleName: '车主之家渠道过滤',
    ruleType: '过滤',
    channels: ['车主之家'],
    weight: 'A',
    createTime: '2026-05-25 13:14:22',
    operator: '6322321',
    status: '1',
    conditionMode: 'AND',
    field: '数据渠道',
    valueType: '等于',
    conditionValue: '车主之家',
    action: '过滤'
  },
  {
    id: '10',
    ruleName: '高端品牌APP/小程序-社区渠道逻辑删除状态过滤',
    ruleType: '过滤',
    channels: ['高端品牌APP/小程序-社区'],
    weight: 'B',
    createTime: '2026-01-28 15:32:03',
    operator: '6322321',
    status: '1',
    conditionMode: 'AND',
    field: '逻辑删除状态',
    valueType: '通配符',
    conditionValue: '已删除',
    action: '过滤'
  }
]

const expandedDefaultRules = Array.from({ length: 100 }, (_, index) => {
  const source = defaultRules[index % defaultRules.length]
  return {
    ...source,
    id: String(index + 1),
    ruleName: source.ruleName,
    createTime: `2026-05-${String(1 + (index % 28)).padStart(2, '0')} ${String(
      9 + (index % 9)
    ).padStart(2, '0')}:${String(index % 60).padStart(2, '0')}:00`
  }
})

const rules = ref<CleaningRule[]>(expandedDefaultRules)
const selectedRules = ref<CleaningRule[]>([])
// 筛选条件默认折叠为一行，展开状态同时用于给列表卡片预留正确空间。
const isFilterExpanded = ref(false)

const filter = reactive({
  ruleName: '',
  ruleType: '',
  channels: [] as string[],
  weight: '',
  operator: '',
  status: ''
})

const page = reactive({
  pageNum: 1,
  pageSize: 10
})

const formVisible = ref(false)
const formMode = ref<'add' | 'edit' | 'view'>('add')
const form = reactive<CleaningRule>({
  id: '',
  ruleName: '',
  ruleType: '清洗',
  channels: [],
  weight: 'A',
  createTime: '',
  operator: '本地管理员',
  status: '1',
  conditionMode: 'AND',
  field: '',
  valueType: '通配符',
  conditionValue: '',
  action: ''
})

const filteredRules = computed(() => {
  return rules.value.filter(rule => {
    const matchName = !filter.ruleName || rule.ruleName.includes(filter.ruleName)
    const matchType = !filter.ruleType || rule.ruleType === filter.ruleType
    const matchChannel =
      !filter.channels.length || filter.channels.some(channel => rule.channels.includes(channel))
    const matchWeight = !filter.weight || rule.weight === filter.weight
    const matchOperator = !filter.operator || rule.operator === filter.operator
    const matchStatus = !filter.status || rule.status === filter.status
    return matchName && matchType && matchChannel && matchWeight && matchOperator && matchStatus
  })
})

const pagedRules = computed(() => {
  const start = (page.pageNum - 1) * page.pageSize
  return filteredRules.value.slice(start, start + page.pageSize)
})

const dialogTitle = computed(() => {
  if (formMode.value === 'view') return '规则详情'
  if (formMode.value === 'edit') return '编辑规则'
  return '新建规则'
})

const isReadonly = computed(() => formMode.value === 'view')

// 筛选区展开后增加列表顶部预留，避免分页器被固定高度的表格挤出卡片。
const tableCardStyle = computed(() => computedCardHeight(isFilterExpanded.value ? 205 : 141))

const formatChannels = (channels: string[]) => channels.join(',')

const resetForm = (rule?: Partial<CleaningRule>) => {
  Object.assign(form, {
    id: '',
    ruleName: '',
    ruleType: '清洗',
    channels: [],
    weight: 'A',
    createTime: '',
    operator: '本地管理员',
    status: '1',
    conditionMode: 'AND',
    field: '',
    valueType: '通配符',
    conditionValue: '',
    action: '',
    ...rule
  })
}

const query = () => {
  page.pageNum = 1
}

const reset = () => {
  filter.ruleName = ''
  filter.ruleType = ''
  filter.channels = []
  filter.weight = ''
  filter.operator = ''
  filter.status = ''
  query()
}

const openAdd = () => {
  formMode.value = 'add'
  resetForm()
  formVisible.value = true
}

const openView = (row: CleaningRule) => {
  formMode.value = 'view'
  resetForm({ ...row, channels: [...row.channels] })
  formVisible.value = true
}

const openEdit = (row: CleaningRule) => {
  formMode.value = 'edit'
  resetForm({ ...row, channels: [...row.channels] })
  formVisible.value = true
}

const handleCopy = (row: CleaningRule) => {
  const copied = {
    ...row,
    id: String(Date.now()),
    ruleName: `${row.ruleName}-复制`,
    createTime: new Date().toLocaleString('zh-CN', { hour12: false })
  }
  rules.value = [copied, ...rules.value]
  ElMessage.success('复制成功')
}

const handleDelete = (row: CleaningRule) => {
  ElMessageBox.confirm('确定要删除该规则吗？删除后无法恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    rules.value = rules.value.filter(item => item.id !== row.id)
    selectedRules.value = selectedRules.value.filter(item => item.id !== row.id)
    ElMessage.success('删除成功')
  })
}

const handleBatchStatus = (status: RuleStatus) => {
  if (!selectedRules.value.length) {
    ElMessage.warning('请先选择规则')
    return
  }
  const selectedIds = new Set(selectedRules.value.map(item => item.id))
  rules.value = rules.value.map(item => (selectedIds.has(item.id) ? { ...item, status } : item))
  selectedRules.value = []
  ElMessage.success(status === '1' ? '批量启用成功' : '批量禁用成功')
}

const handleBatchDelete = () => {
  if (!selectedRules.value.length) {
    ElMessage.warning('请先选择规则')
    return
  }
  ElMessageBox.confirm('确定要删除选中的规则吗？删除后无法恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const selectedIds = new Set(selectedRules.value.map(item => item.id))
    rules.value = rules.value.filter(item => !selectedIds.has(item.id))
    selectedRules.value = []
    ElMessage.success('批量删除成功')
  })
}

const submitForm = () => {
  if (!form.ruleName.trim()) {
    ElMessage.warning('请输入规则名称')
    return
  }
  if (!form.channels.length) {
    ElMessage.warning('请选择数据来源')
    return
  }
  if (!form.field || !form.conditionValue || !form.action) {
    ElMessage.warning('请完善条件配置和执行操作')
    return
  }

  if (formMode.value === 'add') {
    rules.value = [
      {
        ...form,
        id: String(Date.now()),
        createTime: new Date().toLocaleString('zh-CN', { hour12: false })
      },
      ...rules.value
    ]
    ElMessage.success('新建成功')
  } else {
    rules.value = rules.value.map(item => (item.id === form.id ? { ...form } : item))
    ElMessage.success('编辑成功')
  }
  formVisible.value = false
}

const handleCurrentChange = (current: number) => {
  page.pageNum = current
}

const handleSizeChange = (size: number) => {
  page.pageSize = size
  page.pageNum = 1
}
</script>

<template>
  <div>
    <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
      <FFilterLayout v-model="isFilterExpanded" @query="query" @reset="reset">
        <el-form :inline="true" :model="filter">
          <el-form-item label="规则名称" style="width: 260px">
            <el-input
              v-model.trim="filter.ruleName"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
          <el-form-item label="规则类型" style="width: 190px">
            <el-select v-model="filter.ruleType" placeholder="不限" clearable>
              <el-option v-for="item in ruleTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据渠道" style="width: 260px">
            <el-select
              v-model="filter.channels"
              placeholder="不限"
              multiple
              collapse-tags
              :max-collapse-tags="1"
              clearable
            >
              <el-option v-for="item in channelOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="规则权重" style="width: 170px">
            <el-select v-model="filter.weight" placeholder="不限" clearable>
              <el-option v-for="item in weightOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作人" style="width: 200px">
            <el-select v-model="filter.operator" placeholder="不限" clearable>
              <el-option v-for="item in operatorOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用状态" style="width: 170px">
            <el-select v-model="filter.status" placeholder="不限" clearable>
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </FFilterLayout>
    </FtCard>

    <FtCard
      :style="tableCardStyle"
      title="规则列表"
      model="titleOperation"
      clear-content-top-padding
      class="mt-24 cleaning-rules-table-card"
    >
      <template #extra>
        <el-dropdown trigger="click" class="mr-16">
          <el-button>批量操作</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleBatchStatus('1')">启用</el-dropdown-item>
              <el-dropdown-item @click="handleBatchStatus('0')">禁用</el-dropdown-item>
              <el-dropdown-item divided @click="handleBatchDelete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="openAdd">新建规则</el-button>
      </template>

      <div class="cleaning-rules-table-content">
        <el-table
          v-loading="false"
          :data="pagedRules"
          height="100%"
          row-key="id"
          @selection-change="selectedRules = $event"
        >
          <el-table-column type="selection" width="52" />
          <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="ruleType" label="规则类型" width="110" />
          <el-table-column label="数据渠道" min-width="260">
            <template #default="{ row }">
              <el-tooltip
                effect="light"
                placement="top-start"
                popper-class="channel-tooltip"
                :show-after="200"
              >
                <template #content>
                  <div class="channel-tooltip__content">{{ formatChannels(row.channels) }}</div>
                </template>
                <span class="channel-cell">{{ formatChannels(row.channels) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="weight" label="规则权重" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column prop="operator" label="操作人" width="140" show-overflow-tooltip />
          <el-table-column label="启用状态" width="110">
            <template #default="{ row }">
              <span class="status-dot" :class="{ disabled: row.status === '0' }"></span>
              {{ row.status === '1' ? '启用' : '禁用' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openView(row)">查看</el-button>
              <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              <el-button type="primary" link @click="handleCopy(row)">复制</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="flex justify-end mt-16">
          <el-pagination
            v-model:current-page="page.pageNum"
            v-model:page-size="page.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredRules.length"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </FtCard>

    <el-dialog v-model="formVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <el-form :model="form" label-width="112px" :disabled="isReadonly">
        <el-form-item label="规则名称" required>
          <el-input v-model.trim="form.ruleName" placeholder="请输入" :maxlength="50" />
        </el-form-item>
        <el-form-item label="规则类型" required>
          <el-radio-group v-model="form.ruleType">
            <el-radio-button v-for="item in ruleTypeOptions" :key="item" :label="item" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据来源" required>
          <el-select
            v-model="form.channels"
            placeholder="请选择"
            multiple
            collapse-tags
            :max-collapse-tags="2"
            clearable
          >
            <el-option v-for="item in channelOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="权重" required>
          <el-select v-model="form.weight" placeholder="请选择">
            <el-option v-for="item in weightOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件配置" required>
          <div class="condition-box">
            <el-radio-group v-model="form.conditionMode" class="mb-12">
              <el-radio label="AND">满足全部条件(AND)</el-radio>
              <el-radio label="OR">满足任意条件(OR)</el-radio>
            </el-radio-group>
            <div class="condition-row">
              <el-input v-model.trim="form.field" placeholder="字段" />
              <el-select v-model="form.valueType" placeholder="值类型">
                <el-option label="通配符" value="通配符" />
                <el-option label="正则" value="正则" />
                <el-option label="包含" value="包含" />
                <el-option label="等于" value="等于" />
              </el-select>
              <el-input v-model.trim="form.conditionValue" placeholder="请输入" />
            </div>
            <el-button class="mt-12" :disabled="isReadonly">添加条件配置</el-button>
          </div>
        </el-form-item>
        <el-form-item label="执行操作" required>
          <el-input v-model.trim="form.action" type="textarea" placeholder="请输入" :rows="3" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-radio-group v-model="form.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ isReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isReadonly" type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 6px;
  vertical-align: middle;
  background: #00b42a;
  border-radius: 50%;

  &.disabled {
    background: #c9cdd4;
  }
}

.condition-box {
  width: 100%;
  padding: 12px;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  border-radius: 4px;
}

.condition-row {
  display: grid;
  grid-template-columns: 1fr 140px 1.5fr;
  gap: 12px;
}

.cleaning-rules-table-card {
  :deep(.content) {
    min-height: 0;
  }
}

.cleaning-rules-table-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;

  .el-table {
    flex: 1;
    min-height: 0;
  }
}

.channel-cell {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.channel-tooltip) {
  max-width: min(480px, calc(100vw - 48px));
}

:global(.channel-tooltip .channel-tooltip__content) {
  max-height: 240px;
  overflow: auto;
  line-height: 22px;
  overflow-wrap: anywhere;
}
</style>
