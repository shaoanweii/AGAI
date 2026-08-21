<script setup lang="ts">
import dayjs from 'dayjs'
import { useTable } from '@/hooks/table'
import { inject, h } from 'vue'
import type { ConditionsDetailItem } from '@/types'
import { computedCardHeight } from '@/utils'
import { ElCheckbox, ElMessage, ElTooltip, TableV2FixedDir } from 'element-plus'
import type { Column } from 'element-plus'
import FDatePicker from '@/components/FDatePicker/index.vue'
import { showOverflowTooltipConfig } from '@/constant/index'
import { exportRawData } from '@/api/downloadManagement'
import to from 'await-to-js'
import DownloadDialog from './DownloadDialog.vue'
import { hasPermission } from '@/utils/permission'
import { DATAQUERY_DOWNLOAD_MAP } from '@/constant'

defineOptions({
  name: 'RawData'
})

type RawDataMode = 'raw' | 'clean'

const props = withDefaults(
  defineProps<{
    mode?: RawDataMode
  }>(),
  {
    mode: 'raw'
  }
)

const isCleanMode = computed(() => props.mode === 'clean')

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const channelOptions = inject('channelOptions') as Ref<any[]>

// 统一处理conditions为空时的默认值
const getConditionOptions = (key: string) => {
  return conditions?.[key] || []
}

const isDownload = computed(() => {
  return hasPermission(DATAQUERY_DOWNLOAD_MAP.DOWNLOAD)
})

const { table, handleReset, handleSizeChange, handleCurrentChange, getFirstPageTableData } =
  useTable(
    {
      method: 'POST',
      url: isCleanMode.value
        ? '/insights/insCqCaDataSource/getCleanData'
        : '/insights/insCqCaDataSource/getRawData'
    },
    res => {
      return res.result
    }
  )

const times = ref<any[]>([])
const defaultShortcutValue = '近7天'
const shortcutValue = ref(defaultShortcutValue)
// 业务要求：数据查询页的筛选时间不允许超过今天，避免选择未来日期导致无效查询。
const maxSelectableDate = dayjs().format('YYYY-MM-DD')
const isExpanded = ref(false)

// 当前页/当前筛选下的选择态（用于“导出选中”）
const multipleSelection = ref<any[]>([])
const downloadDialogVisible = ref(false)
const exporting = ref(false)

const channelSelection = ref<string[][]>([])
const channelCascaderProps = {
  label: 'name',
  value: 'code',
  children: 'child',
  multiple: true,
  emitPath: true,
  checkStrictly: true
}

const syncChannelFilter = () => {
  const firstCodes = new Set<string>()
  const secondCodes = new Set<string>()
  const thirdCodes = new Set<string>()

  ;(channelSelection.value || []).forEach(path => {
    if (!Array.isArray(path) || path.length === 0) return
    if (path[0]) firstCodes.add(path[0])
    if (path[1]) secondCodes.add(path[1])
    if (path[2]) thirdCodes.add(path[2])
  })

  table.filter.firstChannelCodeList = Array.from(firstCodes)
  table.filter.secondChannelCodeList = Array.from(secondCodes)
  table.filter.threeChannelCodeList = Array.from(thirdCodes)
}

const syncDerivedFilter = () => {
  const [startTime, endTime] = times.value

  table.filter.startTime = startTime
  table.filter.endTime = endTime
  syncChannelFilter()
}

onMounted(() => {
  query()
})

const query = () => {
  multipleSelection.value = []
  syncDerivedFilter()
  getFirstPageTableData()
}

const reset = () => {
  handleReset(() => {
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime
    shortcutValue.value = defaultShortcutValue

    multipleSelection.value = []
    channelSelection.value = []
  })
}

const getSelectedDataIds = () => {
  return (multipleSelection.value || [])
    .map(item => item?.dataId)
    .filter((id: any) => id !== undefined && id !== null && String(id).trim() !== '')
}

const handleExport = async () => {
  if (exporting.value) return

  const selectedIds = getSelectedDataIds()

  // 选中优先：有选中就仅导出选中；未选中则导出当前筛选下前10万条
  if (!selectedIds.length && Number(table.total || 0) <= 0) {
    ElMessage.warning('暂无可导出的数据')
    return
  }

  if (selectedIds.length > 100000 || (!selectedIds.length && Number(table.total || 0) > 100000)) {
    ElMessage.warning('当前系统仅支持导出数据上限为10万条，请合理筛选数据范围后重试。')
    return
  }

  // 与列表查询保持一致，确保导出参数取到派生字段（时间、渠道等）
  syncDerivedFilter()

  const params: Record<string, any> = {
    ...table.filter
    // // 按时间倒序导出 + 限制前10万条
    // order: 'dataCreateTime desc',
    // pageNum: 1,
    // pageSize: 100000
  }

  if (selectedIds.length) {
    params.idList = selectedIds
  }

  exporting.value = true
  const [errs] = await to(exportRawData(params))
  exporting.value = false

  if (errs) {
    ElMessage.error(errs?.message || '导出失败，请稍后重试')
    return
  }
  downloadDialogVisible.value = true
}

const tableFcardHeight = computed(() => {
  return computedCardHeight(isExpanded.value ? 275 : 155)
})

// TableV2 采用固定行高；标题/内容列改为两行省略后，需要同步抬高表头和行高，避免文本被裁切。
const RAW_DATA_HEADER_HEIGHT = 56
const RAW_DATA_ROW_HEIGHT = 64

/**
 * 统一格式化表格文本，避免空值在单元格与 tooltip 中展示为 undefined/null。
 * @param value 表格原始值
 * @returns 可直接展示的文本
 */
const formatTableText = (value: unknown) => {
  return value === undefined || value === null ? '' : String(value)
}

/**
 * 为标题/内容列表头渲染两行省略文本，避免列名在窄列宽下被单行截断。
 * @param title 表头文案
 * @returns TableV2 表头节点
 */
const renderTwoLineHeader = (title: string) => {
  return h('div', { class: 'two-line-header-ellipsis', title }, title)
}

// 表格列配置
const columns: Column[] = [
  {
    key: 'selection',
    width: 55,
    fixed: TableV2FixedDir.LEFT,
    cellRenderer: ({ rowData }) => {
      const rowId = rowData?.dataId
      const isChecked = multipleSelection.value.some(item => item?.dataId === rowId)
      return h(ElCheckbox, {
        modelValue: isChecked,
        'onUpdate:modelValue': (val: any) => {
          const checked = Boolean(val)
          if (checked) {
            if (!multipleSelection.value.some(item => item?.dataId === rowId)) {
              multipleSelection.value = [...multipleSelection.value, rowData]
            }
          } else {
            multipleSelection.value = multipleSelection.value.filter(item => item?.dataId !== rowId)
          }
        }
      })
    },
    headerCellRenderer: () => {
      const tableList = (table.list || []) as any[]
      const isRowSelected = (row: any) =>
        multipleSelection.value.some(item => item?.dataId === row?.dataId)

      const allSelected = tableList.length > 0 && tableList.every(isRowSelected)
      const indeterminate = tableList.some(isRowSelected) && !allSelected

      return h(ElCheckbox, {
        modelValue: allSelected,
        indeterminate,
        'onUpdate:modelValue': (val: any) => {
          const checked = Boolean(val)
          if (checked) {
            multipleSelection.value = [...tableList]
          } else {
            // 只清空当前页已选，避免误伤跨页已选
            const currentIds = new Set(tableList.map(row => row?.dataId))
            multipleSelection.value = multipleSelection.value.filter(
              item => !currentIds.has(item?.dataId)
            )
          }
        }
      })
    }
  }
]
type RawDataColumnDef = {
  key: string
  title: string
  dataKeys: string[]
  width: number
  twoLine?: boolean
}

const resolveRawDataValue = (rowData: any, dataKeys: string[]) => {
  for (const key of dataKeys) {
    const value = rowData?.[key]
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      return value
    }
  }
  return ''
}

const renderRawDataTextCell = (text: unknown, twoLine = false) => {
  const value = formatTableText(text)
  return h(ElTooltip, { content: value, placement: 'top', ...showOverflowTooltipConfig }, () =>
    h('div', { class: twoLine ? 'two-line-ellipsis' : 'text-ellipsis' }, value)
  )
}

const createRawDataColumn = (field: RawDataColumnDef): Column => ({
  key: field.key,
  title: field.title,
  dataKey: field.key,
  width: field.width,
  headerCellRenderer: field.twoLine ? () => renderTwoLineHeader(field.title) : undefined,
  cellRenderer: ({ rowData }) =>
    renderRawDataTextCell(resolveRawDataValue(rowData, field.dataKeys), field.twoLine)
})

const rawDataColumnFields: RawDataColumnDef[] = [
  {
    key: 'dataId',
    title: '原声ID',
    dataKeys: ['dataId', 'rawDataId', 'originalId', 'soundsId'],
    width: 180
  },
  {
    key: 'firstContentType',
    title: '一级内容类型',
    dataKeys: ['firstContentType', 'firstContentTypeName', 'contentTypeFirst', 'contentType'],
    width: 140
  },
  {
    key: 'secondContentType',
    title: '二级内容类型',
    dataKeys: ['secondContentType', 'secondContentTypeName', 'contentTypeSecond'],
    width: 140
  },
  { key: 'title', title: '标题', dataKeys: ['title'], width: 260, twoLine: true },
  { key: 'content', title: '内容', dataKeys: ['content'], width: 420, twoLine: true },
  {
    key: 'firstChannelName',
    title: '一级渠道分类',
    dataKeys: ['firstChannelName', 'isOuter'],
    width: 140
  },
  { key: 'secondChannelName', title: '二级渠道分类', dataKeys: ['secondChannelName'], width: 140 },
  { key: 'channelName', title: '渠道名称', dataKeys: ['channelName'], width: 140 },
  {
    key: 'dataCreateTime',
    title: '发布时间',
    dataKeys: ['dataCreateTime', 'publishTime'],
    width: 180
  },
  { key: 'viewCount', title: '浏览数', dataKeys: ['viewCount'], width: 100 },
  { key: 'commentCount', title: '评论数', dataKeys: ['commentCount'], width: 100 },
  { key: 'likeCount', title: '点赞数', dataKeys: ['likeCount'], width: 100 },
  { key: 'shareCount', title: '分享数', dataKeys: ['shareCount'], width: 100 },
  { key: 'favoriteCount', title: '收藏数', dataKeys: ['favoriteCount'], width: 100 },
  { key: 'originalLink', title: '原文链接', dataKeys: ['originalLink'], width: 220 },
  { key: 'authorNick', title: '发声用户昵称', dataKeys: ['authorNick'], width: 150 },
  { key: 'authorId', title: '发声用户ID', dataKeys: ['authorId'], width: 150 },
  { key: 'oneId', title: 'ONE_ID', dataKeys: ['oneId'], width: 180 },
  { key: 'idCarNo', title: '证件号', dataKeys: ['idCarNo', 'certificateNo'], width: 180 },
  { key: 'mobile', title: '手机号', dataKeys: ['mobile', 'phone'], width: 150 },
  { key: 'email', title: '邮箱', dataKeys: ['email'], width: 180 },
  { key: 'authorType', title: '发声用户类型', dataKeys: ['authorType', 'userType'], width: 140 },
  { key: 'isWsaterArmy', title: '是否水军', dataKeys: ['isWsaterArmy', 'isWaterArmy'], width: 100 },
  {
    key: 'mainPostAuthorNick',
    title: '主帖用户昵称',
    dataKeys: ['mainPostAuthorNick'],
    width: 150
  },
  { key: 'mainPostAuthorId', title: '主帖用户ID', dataKeys: ['mainPostAuthorId'], width: 150 },
  { key: 'mainPostId', title: '主帖ID', dataKeys: ['mainPostId'], width: 160 },
  {
    key: 'mainPostContent',
    title: '主帖内容',
    dataKeys: ['mainPostContent'],
    width: 360,
    twoLine: true
  },
  { key: 'brand', title: '品牌', dataKeys: ['brand'], width: 120 },
  { key: 'series', title: '车系', dataKeys: ['series'], width: 120 },
  { key: 'model', title: '车型', dataKeys: ['model'], width: 120 },
  { key: 'vhlId', title: '车辆ID', dataKeys: ['vhlId'], width: 160 },
  { key: 'vhlVin', title: '车辆车架号', dataKeys: ['vhlVin'], width: 180 },
  { key: 'weight', title: '内容权重值', dataKeys: ['weight', 'contentWeight'], width: 120 }
]

const cleanDataColumnFields: RawDataColumnDef[] = [
  ...rawDataColumnFields,
  { key: 'cleanDataId', title: '清洗后ID', dataKeys: ['cleanDataId'], width: 180 },
  { key: 'cleanTime', title: '清洗时间', dataKeys: ['cleanTime'], width: 180 },
  { key: 'hitRule', title: '命中规则', dataKeys: ['hitRule'], width: 180 },
  { key: 'dataStatus', title: '数据状态', dataKeys: ['dataStatus'], width: 120 }
]

columns.splice(
  1,
  columns.length - 1,
  ...(isCleanMode.value ? cleanDataColumnFields : rawDataColumnFields).map(createRawDataColumn)
)
</script>

<template>
  <FtCard title="筛选条件" model="titleOperation" clear-content-top-padding>
    <FFilterLayout v-model="isExpanded" @query="query" @reset="reset">
      <el-form layout="inline" :model="table.filter" label-width="150px" label-position="right">
        <el-row class="w-full" :gutter="0">
          <el-col :span="8" :style="{ order: 1 }">
            <el-form-item label="发布时间">
              <FDatePicker
                v-model="times"
                v-model:shortcutValue="shortcutValue"
                type="daterange"
                :clearable="false"
                :max-selectable-date="maxSelectableDate"
              ></FDatePicker>
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 6 : 2 }">
            <el-form-item label="渠道">
              <el-cascader
                v-model="channelSelection"
                placeholder="请选择"
                clearable
                filterable
                collapse-tags
                collapse-tags-tooltip
                :options="channelOptions || []"
                :props="channelCascaderProps"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 4 : 5 }">
            <el-form-item label="标题">
              <el-input
                v-model.trim="table.filter.title"
                placeholder="请输入，支持多关键词逗号分隔"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 5 : 6 }">
            <el-form-item label="内容">
              <el-input
                v-model.trim="table.filter.content"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 2 : 7 }">
            <el-form-item label="原声ID">
              <el-input
                v-model.trim="table.filter.id"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 3 : 8 }">
            <el-form-item label="内容类型">
              <el-select v-model="table.filter.contentType" placeholder="不限" clearable>
                <el-option
                  v-for="(item, index) in getConditionOptions('contentType')"
                  :key="index"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 9 : 9 }">
            <el-form-item label="发声用户昵称">
              <el-input
                v-model.trim="table.filter.authorNick"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 10 : 10 }">
            <el-form-item label="发声用户ID">
              <el-input
                v-model.trim="table.filter.authorId"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 12 : 11 }">
            <el-form-item label="发声用户类型">
              <el-input
                v-model.trim="table.filter.authorType"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 13 : 12 }">
            <el-form-item label="是否水军">
              <el-select-v2
                v-model="table.filter.isWsaterArmy"
                placeholder="不限"
                clearable
                filterable
                :options="getConditionOptions('dropdownFilter')"
                :props="{ label: 'value', value: 'key' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 14 : 13 }">
            <el-form-item label="主帖用户昵称">
              <el-input
                v-model.trim="table.filter.mainPostAuthorNick"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 15 : 14 }">
            <el-form-item label="主帖用户ID">
              <el-input
                v-model.trim="table.filter.mainPostAuthorId"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 16 : 15 }">
            <el-form-item label="主帖ID">
              <el-input
                v-model.trim="table.filter.mainPostId"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8" :style="{ order: isCleanMode ? 17 : 16 }">
            <el-form-item label="主帖内容">
              <el-input
                v-model.trim="table.filter.mainPostContent"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col v-if="isCleanMode" :span="8" :style="{ order: 18 }">
            <el-form-item label="清洗后ID">
              <el-input
                v-model.trim="table.filter.cleanDataId"
                placeholder="请输入"
                :maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col v-if="isCleanMode" :span="8" :style="{ order: 20 }">
            <el-form-item label="数据状态">
              <el-select-v2
                v-model="table.filter.dataStatus"
                placeholder="不限"
                clearable
                filterable
                :options="getConditionOptions('dataStatus')"
                :props="{ label: 'value', value: 'key' }"
              />
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
    <template #extra>
      <!-- 卡片标题右侧操作区 -->
      <div class="raw-data-actions">
        <el-button
          v-if="isDownload"
          :disabled="(table.list?.length || 0) === 0 && !multipleSelection.length"
          :loading="exporting"
          type="primary"
          @click="handleExport"
        >
          <template #icon>
            <!-- <i class="iconfont icon-Export"></i> -->
            <SvgIcon name="download" style="width: 20px; height: 20px" />
          </template>
          导出数据
        </el-button>
      </div>
    </template>

    <div class="table-container">
      <el-auto-resizer>
        <template #default="slotProps">
          <el-table-v2
            v-loading="table.loading"
            :columns="columns"
            :data="table.list || []"
            :width="slotProps.width"
            :height="slotProps.height"
            :header-height="RAW_DATA_HEADER_HEIGHT"
            :row-height="RAW_DATA_ROW_HEIGHT"
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

  <DownloadDialog v-model="downloadDialogVisible" />
</template>

<style scoped lang="scss">
.raw-data-actions {
  display: flex;
  gap: 12px;
}

.table-container {
  height: calc(100% - 48px);
}

:deep(.text-ellipsis) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.two-line-ellipsis) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  width: 100%;
  line-height: 20px;
  max-height: 40px;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: break-word;
}

:deep(.two-line-header-ellipsis) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  width: 100%;
  line-height: 18px;
  max-height: 36px;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: break-word;
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
