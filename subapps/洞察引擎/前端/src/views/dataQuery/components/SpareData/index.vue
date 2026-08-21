<script setup lang="ts">
import dayjs from 'dayjs'
import { inject, h } from 'vue'
import { useTable } from '@/hooks/table'
import type { ConditionsDetailItem } from '@/types'
import { computedCardHeight, listHeight, getChannelPathInfo } from '@/utils'
import { ElMessage, ElButton, ElCheckbox, TableV2FixedDir, ElTooltip } from 'element-plus'
import type { Column } from 'element-plus'
import FDatePicker from '@/components/FDatePicker/index.vue'
import { showOverflowTooltipConfig } from '@/constant/index'
import FtCard from '@/components/FtCard.vue'

defineOptions({
  name: 'SpareData'
})

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const channelOptions = inject('channelOptions') as Ref<any[]>

// 统一处理conditions为空时的默认值
const getConditionOptions = (key: string) => {
  return conditions?.[key] || []
}

const {
  table,
  form,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getTableData,
  getFirstPageTableData,
  handleEdit
} = useTable(
  {
    method: 'POST',
    url: '/insights/insCqCaDataSource/getSentimentResultData'
  },
  res => {
    return res.result
  }
)

const times = ref<any[]>([])
const defaultShortcutValue = '近7天'
const shortcutValue = ref(defaultShortcutValue)
// 业务要求：备用数据筛选时间同样禁止选择未来日期，保证三个子页行为一致。
const maxSelectableDate = dayjs().format('YYYY-MM-DD')
const multipleSelection = ref<any[]>([])
const isExpanded = ref(false)

const secondChannelCodeList = ref<string[]>([])
const secondChannelOptions = ref<any[]>([])
const thirdChannelOptions = ref<any[]>()

// 情感程度选项
const sentimentLevelOptions = [
  { label: '高', value: '高' },
  { label: '中', value: '中' },
  { label: '一般', value: '一般' }
]

// 一级渠道分类变化事件处理
const handleFirstChannelChange = () => {
  // 清空二级和三级的值
  secondChannelCodeList.value = []
  table.filter.threeChannelCodeList = []

  if (!table.filter.firstChannelCodeList?.length || !channelOptions.value?.length) {
    secondChannelOptions.value = []
    thirdChannelOptions.value = []
    return
  }

  const allSecondChannels: any[] = []
  table.filter.firstChannelCodeList.forEach((firstCode: string) => {
    const firstChannel = channelOptions.value.find((item: any) => item.code === firstCode)
    if (firstChannel?.child?.length) {
      allSecondChannels.push(...firstChannel.child)
    }
  })

  secondChannelOptions.value = allSecondChannels
  thirdChannelOptions.value = []
}

// 二级渠道分类变化事件处理
const handleSecondChannelChange = () => {
  // 清空三级的值
  table.filter.threeChannelCodeList = []

  if (!secondChannelCodeList.value?.length || !secondChannelOptions.value?.length) {
    thirdChannelOptions.value = []
    return
  }

  const allThirdChannels: any[] = []
  secondChannelCodeList.value.forEach((secondCode: string) => {
    const secondChannel = secondChannelOptions.value.find((item: any) => item.code === secondCode)
    if (secondChannel?.child?.length) {
      allThirdChannels.push(...secondChannel.child)
    }
  })

  thirdChannelOptions.value = allThirdChannels
}

const init = () => {
  form.data = []
}
init()

onMounted(() => {
  query()
})

// 查询前验证渠道选择
const validateChannels = () => {
  const hasFirstChannel = table.filter.firstChannelCodeList?.length > 0
  const hasSecondChannel = secondChannelCodeList.value?.length > 0
  const hasThirdChannel = table.filter.threeChannelCodeList?.length > 0

  // 如果选择了一级或二级渠道，则必须同时选择所有三个渠道
  if (hasFirstChannel || hasSecondChannel) {
    if (!hasFirstChannel || !hasSecondChannel || !hasThirdChannel) {
      ElMessage.warning(
        '选择了一二级渠道中的一个，就必须将一二级渠道和渠道名称全部选择才能进行查询操作'
      )
      return false
    }
  }

  return true
}

const query = (resetPage = true) => {
  // 验证渠道选择
  if (!validateChannels()) {
    return
  }

  const [startTime, endTime] = times.value

  // 处理二级渠道分类数据：获取所有二级下的三级code
  const allThirdCodes: string[] = []
  if (secondChannelCodeList.value?.length && secondChannelOptions.value?.length) {
    secondChannelCodeList.value.forEach((secondCode: string) => {
      const secondChannel = secondChannelOptions.value.find((item: any) => item.code === secondCode)
      if (secondChannel?.child?.length) {
        secondChannel.child.forEach((thirdItem: any) => {
          allThirdCodes.push(thirdItem.code)
        })
      }
    })
  }

  table.filter.startTime = startTime
  table.filter.endTime = endTime
  table.filter.secondChannelCodeList = allThirdCodes

  if (resetPage) {
    getFirstPageTableData()
  } else {
    getTableData()
  }
}
const reset = () => {
  handleReset(() => {
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime
    shortcutValue.value = defaultShortcutValue

    secondChannelCodeList.value = []
    secondChannelOptions.value = []
    thirdChannelOptions.value = []
  })
}

const tableFcardHeight = computed(() => {
  return computedCardHeight(isExpanded.value ? 275 : 155)
})

// 表格列配置
const columns: Column[] = [
  {
    key: 'dataId',
    title: '原始数据ID',
    dataKey: 'dataId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'title',
    title: '标题',
    dataKey: 'title',
    width: 180,
    cellRenderer: ({ cellData }) =>
      h(ElTooltip, { content: cellData, placement: 'top', ...showOverflowTooltipConfig }, () =>
        h('div', { class: 'text-ellipsis' }, cellData)
      )
  },
  {
    key: 'originalTextScene',
    title: '原始声音',
    dataKey: 'originalTextScene',
    width: 180,
    cellRenderer: ({ cellData }) =>
      h(ElTooltip, { content: cellData, placement: 'top', ...showOverflowTooltipConfig }, () =>
        h('div', { class: 'text-ellipsis' }, cellData)
      )
  },
  {
    key: 'sentiment',
    title: '情感',
    dataKey: 'sentiment',
    width: 80,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'sentimentScore',
    title: '情感程度',
    dataKey: 'sentimentScore',
    width: 80,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'contentType',
    title: '内容类型',
    dataKey: 'contentType',
    width: 120,
    cellRenderer: ({ cellData }) => {
      // 使用过滤菜单中的内容类型映射表
      const contentTypeOptions = getConditionOptions('contentType')
      let displayText = cellData || '-'

      if (contentTypeOptions?.length && cellData) {
        const matchedOption = contentTypeOptions.find((option: any) => option.key === cellData)
        if (matchedOption) {
          displayText = matchedOption.value
        }
      }

      return h('div', { class: 'cell-wrap-text' }, displayText)
    }
  },
  {
    key: 'isOuter',
    title: '一级渠道分类',
    dataKey: 'isOuter',
    width: 120,
    cellRenderer: ({ cellData, rowData }) => {
      // 使用getChannelPathInfo工具函数获取渠道路径信息
      let displayName = cellData
      if (channelOptions.value?.length && rowData.channelId) {
        const channelInfo = getChannelPathInfo(channelOptions.value, rowData.channelId)
        if (channelInfo.firstChannelName) {
          displayName = channelInfo.firstChannelName
        }
      }
      return h('div', { class: 'cell-wrap-text' }, displayName || '-')
    }
  },
  {
    key: 'secondChannelName',
    title: '二级渠道分类',
    dataKey: 'secondChannelName',
    width: 180,
    cellRenderer: ({ cellData, rowData }) => {
      // 使用getChannelPathInfo工具函数获取渠道路径信息
      let displayName = cellData
      if (channelOptions.value?.length && rowData.channelId) {
        const channelInfo = getChannelPathInfo(channelOptions.value, rowData.channelId)
        if (channelInfo.secondChannelName) {
          displayName = channelInfo.secondChannelName
        }
      }
      return h('div', { class: 'cell-wrap-text' }, displayName || '-')
    }
  },
  {
    key: 'channelName',
    title: '渠道名称',
    dataKey: 'channelName',
    width: 120,
    cellRenderer: ({ cellData, rowData }) => {
      // 使用getChannelPathInfo工具函数获取渠道路径信息
      let displayName = cellData
      if (channelOptions.value?.length && rowData.channelId) {
        const channelInfo = getChannelPathInfo(channelOptions.value, rowData.channelId)
        if (channelInfo.thirdChannelName) {
          displayName = channelInfo.thirdChannelName
        }
      }
      return h('div', { class: 'cell-wrap-text' }, displayName || '-')
    }
  },
  {
    key: 'id',
    title: '声音ID',
    dataKey: 'id',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'publishTime',
    title: '发布时间',
    dataKey: 'publishTime',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  }
]
</script>

<template>
  <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
    <FFilterLayout v-model="isExpanded" @query="query" @reset="reset">
      <el-form
        layout="inline"
        :model="table.filter"
        label-width="150px"
        label-position="right"
        class="custom-form"
      >
        <el-row class="w-full" :gutter="0">
          <el-col :span="8">
            <el-form-item label="时间范围">
              <FDatePicker
                v-model="times"
                v-model:shortcutValue="shortcutValue"
                type="daterange"
                :clearable="false"
                :max-selectable-date="maxSelectableDate"
              ></FDatePicker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原始数据ID">
              <el-input
                v-model.trim="table.filter.originalId"
                placeholder="请输入"
                :data-testid="`dataSource-result-10009`"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="情感">
              <el-select
                v-model="table.filter.sentiment"
                multiple
                :max-collapse-tags="1"
                collapse-tags
                placeholder="情感"
                clearable
              >
                <el-option
                  v-for="(item, index) in getConditionOptions('vocSentiment')"
                  :key="index"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="内容类型">
              <el-select v-model="table.filter.contentType" placeholder="不限" clearable>
                <el-option
                  v-for="item in getConditionOptions('contentType')"
                  :key="item.key"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="一级渠道分类">
              <el-select-v2
                v-model="table.filter.firstChannelCodeList"
                placeholder="不限"
                clearable
                filterable
                multiple
                collapse-tags
                :options="channelOptions || []"
                :props="{ label: 'name', value: 'code' }"
                @change="handleFirstChannelChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="二级渠道分类">
              <el-select-v2
                v-model="secondChannelCodeList"
                placeholder="不限"
                clearable
                filterable
                :options="secondChannelOptions || []"
                multiple
                collapse-tags
                :props="{ label: 'name', value: 'code' }"
                @change="handleSecondChannelChange"
                :popper-class="'selectV2PopClass'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="渠道名称">
              <el-select-v2
                v-model="table.filter.threeChannelCodeList"
                placeholder="不限"
                clearable
                filterable
                :options="thirdChannelOptions || []"
                multiple
                collapse-tags
                :props="{ label: 'name', value: 'code' }"
                :popper-class="'selectV2PopClass'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="情感程度">
              <el-select
                v-model="table.filter.sentimentScore"
                placeholder="请选择情感程度"
                clearable
              >
                <el-option
                  v-for="item in sentimentLevelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </FFilterLayout>
  </FtCard>

  <FtCard
    :style="tableFcardHeight"
    title="数据列表"
    model="titleOperation"
    clear-content-top-padding
    class="mt-24"
  >
    <div class="table-container">
      <el-auto-resizer>
        <template #default="slotProps">
          <el-table-v2
            :data-testid="`dataSource-result-table`"
            v-loading="table.loading"
            :columns="columns"
            :data="table.list || []"
            :width="slotProps.width"
            :height="slotProps.height"
            :row-key="'dataId'"
            fixed
          />
        </template>
      </el-auto-resizer>
    </div>
    <el-pagination
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 20, 50, 100, 200, 500, 1000]"
      :total="table.total"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </FtCard>
</template>

<style scoped lang="scss">
.table-container {
  height: calc(100% - 48px);
}

:deep(.text-ellipsis) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.cell-wrap-text) {
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  overflow-wrap: break-word;
  hyphens: auto;
}

/* 统一由单元格承载 hover 背景，避免 TableV2 在固定列场景下出现行加深/重影 */
:deep(.el-table-v2__row) {
  background-color: transparent;
}

:deep(.el-table-v2__row-cell) {
  background-color: #fff;
}

:deep(.el-table-v2__row:hover .el-table-v2__row-cell) {
  background-color: var(--el-fill-color-light);
}
</style>
