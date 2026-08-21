<template>
  <AppDialog
    class="error-correction-dialog"
    v-model:visible="visible"
    width="95%"
    style="padding: 0; border-radius: 8px; height: 96%"
    destroy-on-close
    :confirm="handleConfirm"
    @close="handleDialogClose"
  >
    <template #header>
      <div class="error-correction-dialog__header">
        <div class="error-correction-dialog__header-title">数据纠错</div>
        <div class="error-correction-dialog__header-filters">
          <el-select-v2
            v-model="filterParams.brandCode"
            placeholder="品牌"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            clearable
            filterable
            :options="filterBrandOptions"
            style="width: 140px"
            @change="handleFilterBrandChange"
          />
          <el-select-v2
            v-model="filterParams.carSeries"
            placeholder="车系"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            clearable
            filterable
            :options="filterCarSeriesOptions"
            :popper-class="'error-correction-dialog__car-series-popper'"
            style="width: 140px"
            @change="handleFilterChange"
          />
          <el-select-v2
            v-model="filterParams.sentiment"
            placeholder="情感"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            clearable
            :options="sentimentOptions"
            style="width: 140px"
            @change="handleFilterChange"
          />
          <el-select-v2
            v-model="filterParams.intention"
            placeholder="意图"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            clearable
            :options="intentionOptions"
            style="width: 140px"
            @change="handleFilterChange"
          />
          <el-select-v2
            v-model="filterParams.topicList"
            ref="topicFilterSelectRef"
            placeholder="标准观点"
            multiple
            collapse-tags
            :max-collapse-tags="1"
            clearable
            filterable
            :fit-input-width="520"
            placement="bottom"
            :options="filterTopicOptions"
            :props="{ label: 'name', value: 'code' }"
            style="width: 140px"
            @change="handleFilterChange"
          >
            <template #header>
              <el-checkbox v-model="checkAll" :indeterminate="indeterminate"> 全选 </el-checkbox>
            </template>
          </el-select-v2>
        </div>
      </div>
    </template>

    <div class="error-correction-dialog__content">
      <div class="error-correction-dialog__subtitle">
        数据列表 <span class="subtitle-desc">共{{ pagination.total }}条数据</span>
      </div>

      <div class="data-list-table-container mt-16" v-loading="querying">
        <el-auto-resizer>
          <template #default="slotProps">
            <el-table-v2
              :columns="dataListV2Columns"
              :data="listData"
              :width="slotProps.width"
              :height="slotProps.height"
              :row-key="'id'"
              fixed
            />
          </template>
        </el-auto-resizer>
      </div>

      <div class="pagination-container mt-16">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.limit"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100, 200, 500]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <div class="divider mt-24"></div>

      <div class="error-correction-dialog__correction-header mt-24">
        <div class="error-correction-dialog__subtitle">修正字段值</div>
        <div class="error-correction-dialog__validity-header">
          <div class="error-correction-dialog__subtitle">数据有效性</div>
          <div class="error-correction-dialog__validity-switch">
            <div
              class="error-correction-dialog__validity-item"
              :class="{ 'error-correction-dialog__validity-item--active': errorType === '2' }"
              @click="switchErrorType('2')"
            >
              有效数据
            </div>
            <div
              class="error-correction-dialog__validity-item"
              :class="{ 'error-correction-dialog__validity-item--active': errorType === '1' }"
              @click="switchErrorType('1')"
            >
              无效数据
            </div>
          </div>
        </div>
      </div>
      <el-table :data="tableData" size="large" style="width: 100%" class="mt-16">
        <el-table-column prop="field" label="字段" />
        <el-table-column prop="afterValue" label="编辑前数据值" />
        <el-table-column prop="beforeValue" label="修改为数据值">
          <template #default="{ row }">
            <el-select-v2
              v-if="row.field === '品牌'"
              v-model="row.beforeValue"
              placeholder="品牌"
              clearable
              filterable
              :options="correctionBrandOptions"
              @change="handleCorrectionBrandChange"
            />
            <el-select-v2
              v-else-if="row.field === '车系'"
              v-model="row.beforeValue"
              placeholder="车系"
              clearable
              filterable
              :options="correctionCarSeriesOptions"
            />
            <!--
              当前“修正字段值”表格按需求先隐藏“情感”编辑行，相关代码注释保留，便于后续快速恢复。
            <el-select-v2
              v-else-if="row.field === '情感'"
              v-model="row.beforeValue"
              placeholder="情感"
              clearable
              :options="sentimentOptions"
            />
            -->
            <!--
              当前“修正字段值”表格按需求先隐藏“意图”编辑行，相关代码注释保留，便于后续快速恢复。
            <el-select-v2
              v-else-if="row.field === '意图'"
              v-model="row.beforeValue"
              placeholder="意图"
              clearable
              :options="intentionOptions"
            />
            -->
            <el-select-v2
              v-else-if="row.field === '观点'"
              v-model="row.beforeValue"
              placeholder="观点"
              clearable
              filterable
              :fit-input-width="520"
              placement="bottom"
              :options="correctionTopicOptions"
              :props="{ label: 'name', value: 'code' }"
              :popper-class="'selectV2PopClass'"
            />
            <el-cascader
              v-else-if="row.field === '用车场景'"
              v-model="row.beforeValue"
              placeholder="用车场景"
              clearable
              filterable
              :options="correctionUsageScenarioOptions"
              :props="correctionUsageScenarioCascaderProps"
              :show-all-levels="false"
              class="w-full"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </AppDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch, reactive, h } from 'vue'
import { ElLoading, ElMessage, ElCheckbox, ElTooltip, TableV2FixedDir } from 'element-plus'
import type { CascaderOption, CascaderProps, Column, Placement } from 'element-plus'
import AppDialog from '@/components/AppDialog.vue'
import useUserStore from '@/store/modules/user'
import useQueryStore from '@/store/modules/query'
import { getDataPlazaConditions } from '@/api/dataPlaza'
import type { DataPlazaConditionOption } from '@/api/dataPlaza/types'
import type { InsertLabelCorrectionParams } from '@/api/labelCorrection'
import {
  findAllFinalTagLibClientVoList,
  findTopicList,
  insertLabelCorrection,
  queryAllBrandList,
  queryAllCarSeriesList,
  queryBrandList,
  queryCarSeriesList,
  querySoundsInfo
} from '@/api/labelCorrection'
import { getRealAttr } from '@/views/leaderOverview/leader/common/fn.ts'

type ErrorType = '1' | '2'

type SelectOptionValue = string
type SelectOption = { label: string; value: SelectOptionValue }

type CorrectionRowInput = {
  id?: string | number
  brandName?: string
  carSeriesName?: string
  originalTextScene?: string
  tagBusinessDomain?: string
  domTagFour?: string
  sentiment?: string
  intention?: string
  topic?: string
  topicText?: string
  usageScenarioFirst?: string
  usageScenarioSecond?: string
}

type CorrectionQueryTimeRange = Pick<InsertLabelCorrectionParams, 'startTime' | 'endTime'>

const visible = defineModel<boolean>('visible', { default: false })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const props = withDefaults(
  defineProps<{
    dataIdList: string[]
    filter?: Record<string, any>
  }>(),
  { dataIdList: () => [] }
)

const userStore = useUserStore()
const queryStore = useQueryStore()

const normalizeDictOptions = (raw: unknown): SelectOption[] => {
  if (!Array.isArray(raw)) return []
  return raw
    .map((item: any) => ({
      label: String(item?.text ?? item?.label ?? ''),
      value: String(item?.value ?? item?.key ?? '')
    }))
    .filter(item => item.label && item.value)
}

const sentimentOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('voc_sentiment'))
)
const intentionOptions = computed(() =>
  normalizeDictOptions(userStore.getDictItems('voc_intention'))
)

type DataListColumn = {
  prop: string
  label: string
  width?: number
  fixed?: 'left' | 'right'
  showOverflowTooltip?: boolean
}

type DataRowKey = string | number

const tableTooltipFallbackPlacements: Placement[] = ['top', 'bottom', 'right', 'left']

/**
 * 统一约束表格 tooltip 的避让策略，减少弹窗边缘和长文本场景下的越界问题。
 */
const tableTooltipPopperOptions = {
  modifiers: [
    {
      name: 'offset',
      options: {
        offset: [0, 12]
      }
    },
    {
      name: 'preventOverflow',
      options: {
        padding: 12,
        altAxis: true,
        tether: true
      }
    }
  ]
}

/**
 * 统一渲染表格单元格 tooltip，保证长文本可换行、可滚动，并尽量留在视口内。
 * @param cellData 当前单元格原始值
 * @returns TableV2 单元格渲染节点
 */
const renderOverflowTooltipCell = (cellData: unknown) => {
  const content = cellData == null ? '' : String(cellData)

  return h(
    ElTooltip,
    {
      placement: 'top',
      fallbackPlacements: tableTooltipFallbackPlacements,
      popperClass: 'error-correction-dialog__table-tooltip text-tooltip-light',
      popperOptions: tableTooltipPopperOptions
    },
    {
      content: () => h('div', { class: 'error-correction-dialog__table-tooltip-content' }, content),
      default: () => h('div', { class: 'text-ellipsis' }, content)
    }
  )
}

/**
 * 数据纠错弹窗中的数据列表仅展示当前纠错所需的核心字段。
 * 其余字段先注释保留，后续如需恢复展示可直接从下方注释块中启用。
 */
const dataListColumns: DataListColumn[] = [
  { prop: 'dataId', label: '原始数据ID', width: 120, showOverflowTooltip: true },
  { prop: 'title', label: '标题', width: 170, showOverflowTooltip: true },
  { prop: 'originalText', label: '原始声音', width: 150, showOverflowTooltip: true },
  { prop: 'originalTextScene', label: '声音片段', width: 150, showOverflowTooltip: true },
  { prop: 'brandName', label: '品牌', width: 100, showOverflowTooltip: true },
  { prop: 'carSeriesName', label: '车系', width: 100, showOverflowTooltip: true },
  { prop: 'opinion', label: '原始观点', width: 150, showOverflowTooltip: true },
  { prop: 'topicText', label: '标准观点', width: 150, showOverflowTooltip: true },
  { prop: 'sentiment', label: '情感', width: 80, showOverflowTooltip: true },
  { prop: 'intention', label: '意图', width: 80, showOverflowTooltip: true },
  { prop: 'usageScenarioFirst', label: '用车场景一级', width: 120, showOverflowTooltip: true },
  { prop: 'usageScenarioSecond', label: '用车场景二级', width: 120, showOverflowTooltip: true },
  { prop: 'dataStatus', label: '数据状态', width: 120, showOverflowTooltip: true }
  /*
    { prop: 'contentType', label: '内容类型', width: 120, showOverflowTooltip: true },
    { prop: 'id', label: '声音ID', width: 180, showOverflowTooltip: true },
    { prop: 'isOuter', label: '一级渠道分类', width: 120, showOverflowTooltip: true },
    { prop: 'secondChannelName', label: '二级渠道分类', width: 180, showOverflowTooltip: true },
    { prop: 'channelName', label: '渠道名称', width: 120, showOverflowTooltip: true },
    { prop: 'seriesFactory', label: '车企名称', width: 120, showOverflowTooltip: true },
    { prop: 'modelName', label: '车型名称', width: 180, showOverflowTooltip: true },
    { prop: 'publishTime', label: '发布时间', width: 180, showOverflowTooltip: true },
    { prop: 'hotWord', label: '热词', width: 180, showOverflowTooltip: true },
    { prop: 'keywords', label: '关键词', width: 180, showOverflowTooltip: true },
    { prop: 'userJourney1', label: '用户旅程一级', width: 120, showOverflowTooltip: true },
    { prop: 'userJourney2', label: '用户旅程二级', width: 120, showOverflowTooltip: true },
    { prop: 'userJourney3', label: '用户旅程三级', width: 120, showOverflowTooltip: true },
    { prop: 'topic', label: '标准观点编码', width: 180, showOverflowTooltip: true },
    { prop: 'cptTagFirst', label: 'CPT标签1级', width: 180, showOverflowTooltip: true },
    { prop: 'cptTagSecond', label: 'CPT标签2级', width: 180, showOverflowTooltip: true },
    { prop: 'cptTagThree', label: 'CPT标签3级', width: 180, showOverflowTooltip: true },
    { prop: 'cptTagFour', label: 'CPT标签4级', width: 180, showOverflowTooltip: true },
    { prop: 'ujyTagFirst', label: '全旅程客户标签1级', width: 180, showOverflowTooltip: true },
    { prop: 'ujyTagSecond', label: '全旅程客户标签2级', width: 180, showOverflowTooltip: true },
    { prop: 'ujyTagThree', label: '全旅程客户标签3级', width: 180, showOverflowTooltip: true },
    { prop: 'ujyTagFour', label: '全旅程客户标签4级', width: 180, showOverflowTooltip: true },
    { prop: 'cmaTagFirst', label: 'CMA标签1级', width: 180, showOverflowTooltip: true },
    { prop: 'cmaTagSecond', label: 'CMA标签2级', width: 180, showOverflowTooltip: true },
    { prop: 'cmaTagThree', label: 'CMA标签3级', width: 180, showOverflowTooltip: true },
    { prop: 'cmaTagFour', label: 'CMA标签4级', width: 180, showOverflowTooltip: true },
    { prop: 'domTagFirst', label: '全领域业务标签1级', width: 180, showOverflowTooltip: true },
    { prop: 'domTagSecond', label: '全领域业务标签2级', width: 180, showOverflowTooltip: true },
    { prop: 'domTagThree', label: '全领域业务标签3级', width: 180, showOverflowTooltip: true },
    { prop: 'domTagFour', label: '全领域业务标签4级', width: 180, showOverflowTooltip: true },
    { prop: 'npsTagFirst', label: 'NPS标签1级', width: 180, showOverflowTooltip: true },
    { prop: 'npsTagSecond', label: 'NPS标签2级', width: 180, showOverflowTooltip: true },
    { prop: 'npsTagThree', label: 'NPS标签3级', width: 180, showOverflowTooltip: true },
    { prop: 'npsTagFour', label: 'NPS标签4级', width: 180, showOverflowTooltip: true },
    { prop: 'vtrTagFirst', label: 'VRT标签1级', width: 180, showOverflowTooltip: true },
    { prop: 'vtrTagSecond', label: 'VRT标签2级', width: 180, showOverflowTooltip: true },
    { prop: 'vtrTagThree', label: 'VRT标签3级', width: 180, showOverflowTooltip: true },
    { prop: 'vtrTagFour', label: 'VRT标签4级', width: 180, showOverflowTooltip: true },
    { prop: 'tagAccuracy', label: '标签-准确性', width: 180, showOverflowTooltip: true },
    {
      prop: 'tagCustomerIssueClassification',
      label: '标签-客户问题分类',
      width: 180,
      showOverflowTooltip: true
    },
    { prop: 'tagIssueSeverity', label: '标签-问题严重程度', width: 180, showOverflowTooltip: true },
    { prop: 'tagCodeStatus', label: '标签-编码状态', width: 180, showOverflowTooltip: true },
    { prop: 'tagBusinessDomain', label: '标签-业务域', width: 180, showOverflowTooltip: true },
    { prop: 'tagEventClarity', label: '标签-事件清晰度', width: 180, showOverflowTooltip: true },
    { prop: 'tagHighValueFlag', label: '标签-高价值标识', width: 180, showOverflowTooltip: true },
    {
      prop: 'tagComplaintFlagNeedingReply',
      label: '标签-需回复投诉标识',
      width: 180,
      showOverflowTooltip: true
    },
    {
      prop: 'tagHighQualityVocFlag',
      label: '标签-高质量VOC标识',
      width: 180,
      showOverflowTooltip: true
    },
    { prop: 'tagNewEnergyOrFuel', label: '标签-新能源/燃油', width: 180, showOverflowTooltip: true },
    { prop: 'tagNeedForvclosedLoop', label: '标签-需要闭环', width: 180, showOverflowTooltip: true },
    { prop: 'isWsaterArmy', label: '是否水军', width: 180, showOverflowTooltip: true },
    { prop: 'isManagerFocused', label: '是否管理层关注', width: 180, showOverflowTooltip: true },
    { prop: 'isBigV', label: '是否大V', width: 180, showOverflowTooltip: true },
    { prop: 'authorId', label: '作者ID', width: 180, showOverflowTooltip: true },
    { prop: 'authorNick', label: '用户昵称', width: 180, showOverflowTooltip: true },
    { prop: 'isMainPost', label: '是否主贴', width: 100, showOverflowTooltip: true },
    { prop: 'originalLink', label: '原文链接', width: 180, showOverflowTooltip: true },
    { prop: 'viewCount', label: '浏览数', width: 120, showOverflowTooltip: true },
    { prop: 'commentCount', label: '评论数', width: 120, showOverflowTooltip: true },
    { prop: 'likeCount', label: '点赞数', width: 120, showOverflowTooltip: true },
    { prop: 'shareCount', label: '分享数', width: 120, showOverflowTooltip: true },
    { prop: 'favoriteCount', label: '收藏数', width: 120, showOverflowTooltip: true },
    { prop: 'workOrderId', label: '工单ID', width: 180, showOverflowTooltip: true },
    { prop: 'questId', label: '问卷ID', width: 180, showOverflowTooltip: true },
    { prop: 'questType', label: '问卷类型', width: 180, showOverflowTooltip: true },
    { prop: 'questAnswerScore', label: '问卷答案分数', width: 180, showOverflowTooltip: true },
    { prop: 'questBusinessType', label: '问卷业务类型', width: 180, showOverflowTooltip: true },
    { prop: 'questBusinessScenario', label: '问卷业务场景', width: 180, showOverflowTooltip: true },
    { prop: 'd2cResponsibleDept', label: 'D2C负责部门', width: 180, showOverflowTooltip: true },
    { prop: 'd2cAccountableDept', label: 'D2C牵头部门', width: 180, showOverflowTooltip: true },
    { prop: 'd2cCcDept', label: 'D2C抄送部门', width: 180, showOverflowTooltip: true },
    { prop: 'oneId', label: 'ONE_ID', width: 180, showOverflowTooltip: true },
    { prop: 'custGlobalId', label: '客户全局ID', width: 180, showOverflowTooltip: true },
    { prop: 'custName', label: '用户名', width: 180, showOverflowTooltip: true },
    { prop: 'custMainPhone', label: '客户手机号', width: 180, showOverflowTooltip: true },
    { prop: 'isCarOwner', label: '是否车主', width: 180, showOverflowTooltip: true },
    { prop: 'custAge', label: '客户年龄', width: 180, showOverflowTooltip: true },
    { prop: 'custAgeGroup', label: '客户年龄段', width: 180, showOverflowTooltip: true },
    { prop: 'custGender', label: '性别', width: 180, showOverflowTooltip: true },
    { prop: 'custHighEducaion', label: '教育程度', width: 180, showOverflowTooltip: true },
    { prop: 'marrigeStatue', label: '婚姻状况', width: 180, showOverflowTooltip: true },
    { prop: 'familyIncome', label: '家庭收入', width: 180, showOverflowTooltip: true },
    { prop: 'isExchangeFlg', label: '是否换购', width: 180, showOverflowTooltip: true },
    { prop: 'purchaseCarTimes', label: '购车次数', width: 180, showOverflowTooltip: true },
    { prop: 'isMemberFlg', label: '是否会员', width: 180, showOverflowTooltip: true },
    { prop: 'custProvince', label: '客户省份', width: 180, showOverflowTooltip: true },
    { prop: 'custCity', label: '客户城市', width: 180, showOverflowTooltip: true },
    { prop: 'custType', label: '客户分类', width: 180, showOverflowTooltip: true },
    { prop: 'custLivedProv', label: '客户居住省份', width: 180, showOverflowTooltip: true },
    { prop: 'custLivedCity', label: '客户居住城市', width: 180, showOverflowTooltip: true },
    { prop: 'custProfession', label: '客户职业', width: 180, showOverflowTooltip: true },
    { prop: 'vhlVin', label: 'VIN', width: 180, showOverflowTooltip: true },
    { prop: 'vhlColorName', label: '车型颜色名称', width: 180, showOverflowTooltip: true },
    { prop: 'vhlProductDate', label: '车辆生产日期', width: 180, showOverflowTooltip: true },
    { prop: 'vhlOfflineDate', label: '车辆下线日期', width: 180, showOverflowTooltip: true },
    { prop: 'vhlIsAbroad', label: '是否海外', width: 180, showOverflowTooltip: true },
    { prop: 'vhlDisCh', label: '驱动形式-变速箱', width: 180, showOverflowTooltip: true },
    { prop: 'vhlDisMt', label: '驱动形式-手动/自动', width: 180, showOverflowTooltip: true },
    { prop: 'vhlEngClsf', label: '发动机分类', width: 180, showOverflowTooltip: true },
    { prop: 'vhlEngSeris', label: '发动机系列', width: 180, showOverflowTooltip: true },
    { prop: 'vhlVehType', label: '车型类型', width: 180, showOverflowTooltip: true },
    { prop: 'vhlCountry', label: '国家', width: 180, showOverflowTooltip: true },
    { prop: 'vhlBdClsf', label: '车身分类', width: 180, showOverflowTooltip: true },
    { prop: 'vhlSegMt', label: '细分市场', width: 180, showOverflowTooltip: true },
    { prop: 'vhlPowClsf', label: '动力分类', width: 180, showOverflowTooltip: true },
    { prop: 'vhlFuClsf', label: '燃料分类', width: 180, showOverflowTooltip: true },
    { prop: 'vhlModlSt', label: '车型状态', width: 180, showOverflowTooltip: true },
    { prop: 'vhlStdPlntCode', label: '标准工厂代码', width: 180, showOverflowTooltip: true },
    { prop: 'dlrOcId', label: '销售组织ID', width: 180, showOverflowTooltip: true },
    { prop: 'dlrOcName', label: '销售组织名称', width: 180, showOverflowTooltip: true },
    { prop: 'dlrOcProvince', label: '销售组织省份', width: 180, showOverflowTooltip: true },
    { prop: 'dlrOcCity', label: '销售组织城市', width: 180, showOverflowTooltip: true },
    { prop: 'dlrDcId', label: '服务组织ID', width: 180, showOverflowTooltip: true },
    { prop: 'dlrDcName', label: '服务组织名称', width: 180, showOverflowTooltip: true },
    { prop: 'dlrDcProvince', label: '服务组织省份', width: 180, showOverflowTooltip: true },
    { prop: 'dlrDcCity', label: '服务组织城市', width: 180, showOverflowTooltip: true },
    { prop: 'dlrMcId', label: '经销商管理公司ID', width: 180, showOverflowTooltip: true },
    { prop: 'dlrMcName', label: '经销商管理公司名称', width: 180, showOverflowTooltip: true },
    { prop: 'dlrMcProvince', label: '经销商管理公司省份', width: 180, showOverflowTooltip: true },
    { prop: 'dlrMcCity', label: '经销商管理公司城市', width: 180, showOverflowTooltip: true }
  */
]

const getDataRowKey = (row: CorrectionRowInput, rowIndex: number): DataRowKey => {
  const id = row.id
  if (id !== '' && id !== null && id !== undefined) return id
  const dataId = (row as Record<string, unknown>).dataId
  if (dataId !== '' && dataId !== null && dataId !== undefined) return String(dataId)
  return `row-${rowIndex}`
}

const selectedRowKeySet = computed(() => {
  return new Set<DataRowKey>(multipleSelection.value.map((row, index) => getDataRowKey(row, index)))
})

const isDataListRowSelected = (row: CorrectionRowInput, rowIndex: number): boolean => {
  return selectedRowKeySet.value.has(getDataRowKey(row, rowIndex))
}

const setMultipleSelectionByRowKeys = (rowKeys: DataRowKey[]) => {
  const rowKeySet = new Set(rowKeys)
  multipleSelection.value = listData.value.filter((row, index) => {
    return rowKeySet.has(getDataRowKey(row, index))
  })
  updateCorrectionTableAfterValues()
}

const handleRowSelectionChange = (
  checked: boolean,
  rowData: CorrectionRowInput,
  rowIndex: number
) => {
  const rowKey = getDataRowKey(rowData, rowIndex)
  const rowKeys = new Set(selectedRowKeySet.value)
  if (checked) {
    rowKeys.add(rowKey)
  } else {
    rowKeys.delete(rowKey)
  }
  setMultipleSelectionByRowKeys(Array.from(rowKeys))
}

const handleAllSelectionChange = (checked: boolean) => {
  if (checked) {
    setMultipleSelectionByRowKeys(listData.value.map((row, index) => getDataRowKey(row, index)))
    return
  }
  setMultipleSelectionByRowKeys([])
}

const dataListSelectionStatus = computed(() => {
  const listKeys = listData.value.map((row, index) => getDataRowKey(row, index))
  if (!listKeys.length) {
    return {
      allSelected: false,
      indeterminate: false
    }
  }

  const selectedCount = listKeys.filter(rowKey => selectedRowKeySet.value.has(rowKey)).length
  return {
    allSelected: selectedCount === listKeys.length,
    indeterminate: selectedCount > 0 && selectedCount < listKeys.length
  }
})

const dataListV2Columns = computed<Column[]>(() => {
  return [
    {
      key: 'selection',
      width: 55,
      fixed: true,
      cellRenderer: ({ rowData, rowIndex }) => {
        const currentRow = rowData as CorrectionRowInput
        return h(ElCheckbox, {
          modelValue: isDataListRowSelected(currentRow, rowIndex),
          'onUpdate:modelValue': (val: unknown) => {
            handleRowSelectionChange(Boolean(val), currentRow, rowIndex)
          }
        })
      },
      headerCellRenderer: () => {
        return h(ElCheckbox, {
          modelValue: dataListSelectionStatus.value.allSelected,
          indeterminate: dataListSelectionStatus.value.indeterminate,
          'onUpdate:modelValue': (val: unknown) => {
            handleAllSelectionChange(Boolean(val))
          }
        })
      }
    },
    ...dataListColumns.map(column => {
      const fixed =
        column.fixed === 'right'
          ? TableV2FixedDir.RIGHT
          : column.fixed === 'left'
            ? TableV2FixedDir.LEFT
            : undefined
      const currentColumn: Column = {
        key: column.prop,
        title: column.label,
        dataKey: column.prop,
        width: column.width ?? 120,
        fixed
      }

      if (column.showOverflowTooltip) {
        currentColumn.cellRenderer = ({ cellData }) => {
          return renderOverflowTooltipCell(cellData)
        }
      }

      return currentColumn
    })
  ]
})

const filterBrandOptions = ref<SelectOption[]>([])
const filterBrandOptionsLoadedKey = ref('')
const filterBrandOptionsLoaded = ref(false)
const filterBrandOptionsRequestId = ref(0)

const correctionBrandOptions = ref<SelectOption[]>([])
const correctionBrandOptionsLoaded = ref(false)
const correctionBrandOptionsRequestId = ref(0)

const filterCarSeriesOptions = ref<SelectOption[]>([])
const correctionCarSeriesOptions = ref<SelectOption[]>([])
const correctionCarSeriesRequestId = ref(0)

const errorType = ref<ErrorType>('2')
const switchErrorType = (type: ErrorType) => {
  errorType.value = type
}

type CorrectionTableField = '品牌' | '车系' | '观点' | '用车场景'
type CorrectionTextField = Exclude<CorrectionTableField, '用车场景'>
type CorrectionTableFieldValue = string | string[]
type CorrectionTableRow = {
  field: CorrectionTableField
  afterValue: string
  beforeValue: CorrectionTableFieldValue
}

// Correction Table
const tableData = ref<CorrectionTableRow[]>([
  { field: '品牌', afterValue: '', beforeValue: '' },
  { field: '车系', afterValue: '', beforeValue: '' },
  // { field: '情感', afterValue: '', beforeValue: '' },
  // { field: '意图', afterValue: '', beforeValue: '' },
  { field: '观点', afterValue: '', beforeValue: '' },
  { field: '用车场景', afterValue: '', beforeValue: [] }
])

/**
 * 按字段名读取纠错表格行，避免依赖固定索引导致后续增删行时错位。
 * @param field 需要读取的纠错字段
 * @returns 对应表格行，未命中时返回 undefined
 */
const getCorrectionTableRow = (field: CorrectionTableField) => {
  return tableData.value.find(row => row.field === field)
}

/**
 * 统一设置“编辑前数据值”，未命中的字段会被安全忽略。
 * @param field 需要回填的纠错字段
 * @param value 回填到表格中的展示值
 */
const setCorrectionAfterValue = (field: CorrectionTableField, value: string) => {
  const targetRow = getCorrectionTableRow(field)
  if (!targetRow) return
  targetRow.afterValue = value
}

/**
 * 统一读取“修改为数据值”原始值。
 * @param field 需要读取的纠错字段
 * @returns 当前字段的原始值
 */
const getCorrectionBeforeValue = (field: CorrectionTableField) => {
  return getCorrectionTableRow(field)?.beforeValue ?? ''
}

/**
 * 统一读取字符串型纠错值，并做文本归一化处理。
 * 品牌、观点等字段仍沿用字符串语义，避免提交时混入空格或非预期空值。
 * @param field 需要读取的纠错字段
 * @returns 归一化后的字符串值
 */
const getNormalizedCorrectionBeforeValue = (field: CorrectionTextField) => {
  return normalizeFilterText(getCorrectionBeforeValue(field))
}

/**
 * 清空纠错编辑值，用车场景使用级联路径，其余字段使用字符串。
 */
const resetCorrectionBeforeValues = () => {
  tableData.value.forEach(row => {
    row.beforeValue = row.field === '用车场景' ? [] : ''
  })
}

// Filter Params
const filterParams = reactive({
  brandCode: [] as string[],
  sentiment: [] as string[],
  intention: [] as string[],
  carSeries: [] as string[],
  topicList: [] as string[]
})

// List Data
const listData = ref<CorrectionRowInput[]>([])
const multipleSelection = ref<CorrectionRowInput[]>([])
const pagination = reactive({
  page: 1,
  limit: 500,
  total: 0
})

const normalizeDataIdList = (raw: unknown): string[] => {
  if (!Array.isArray(raw)) return []
  return raw
    .map(item => (item == null ? '' : String(item)))
    .map(item => item.trim())
    .filter(Boolean)
}

type BrandNode = {
  brandCode?: string
  brandName?: string
  code?: string
  name?: string
  key?: string
  value?: string
}

const normalizeBrandList = (raw: unknown): BrandNode[] => {
  if (!Array.isArray(raw)) return []
  return raw as BrandNode[]
}

const toBrandOptions = (rawList: BrandNode[]): SelectOption[] => {
  const seen = new Set<string>()
  const options: SelectOption[] = []

  for (const item of rawList) {
    const value = String(item.brandCode ?? item.code ?? item.key ?? '').trim()
    const label = String(item.brandName ?? item.name ?? item.value ?? '').trim()
    if (!value || !label) continue
    if (seen.has(value)) continue
    seen.add(value)
    options.push({ label, value })
  }

  return options
}

const ensureFilterBrandOptionsLoaded = async () => {
  const dataIdList = normalizeDataIdList(props.dataIdList)
  const loadedKey = buildCorrectionQueryCacheKey(dataIdList)
  if (filterBrandOptionsLoaded.value && filterBrandOptionsLoadedKey.value === loadedKey) return

  filterBrandOptionsLoadedKey.value = loadedKey
  filterBrandOptions.value = []
  filterBrandOptionsLoaded.value = false

  if (!dataIdList.length) {
    filterBrandOptionsRequestId.value += 1
    filterBrandOptionsLoaded.value = true
    return
  }

  const requestId = ++filterBrandOptionsRequestId.value
  try {
    const res = await queryBrandList({
      dataIdList,
      ...buildCorrectionQueryTimeRange()
    })
    if (requestId !== filterBrandOptionsRequestId.value) return
    filterBrandOptions.value = toBrandOptions(normalizeBrandList(res.result))
  } catch (error: any) {
    if (requestId !== filterBrandOptionsRequestId.value) return
    console.error('queryBrandList failed:', error)
    filterBrandOptions.value = []
    ElMessage.error(error?.message || '获取品牌失败')
  } finally {
    if (requestId === filterBrandOptionsRequestId.value) {
      filterBrandOptionsLoaded.value = true
    }
  }
}

const ensureCorrectionBrandOptionsLoaded = async () => {
  if (correctionBrandOptionsLoaded.value) return

  correctionBrandOptions.value = []
  correctionBrandOptionsLoaded.value = false

  const requestId = ++correctionBrandOptionsRequestId.value
  try {
    const res = await queryAllBrandList()
    if (requestId !== correctionBrandOptionsRequestId.value) return
    correctionBrandOptions.value = toBrandOptions(normalizeBrandList(res.result))
  } catch (error: any) {
    if (requestId !== correctionBrandOptionsRequestId.value) return
    console.error('queryAllBrandList failed:', error)
    correctionBrandOptions.value = []
    ElMessage.error(error?.message || '获取品牌失败')
  } finally {
    if (requestId === correctionBrandOptionsRequestId.value) {
      correctionBrandOptionsLoaded.value = true
    }
  }
}

type CarSeriesNode = {
  carSeriesCode?: string
  carSeriesName?: string
  code?: string
  name?: string
  key?: string
  value?: string
}

const normalizeCarSeriesList = (raw: unknown): CarSeriesNode[] => {
  if (!Array.isArray(raw)) return []
  return raw as CarSeriesNode[]
}

const toCarSeriesOptions = (rawList: CarSeriesNode[]): SelectOption[] => {
  const seen = new Set<string>()
  const options: SelectOption[] = []

  for (const item of rawList) {
    const value = String(item.carSeriesCode ?? item.code ?? item.key ?? '').trim()
    const label = String(item.carSeriesName ?? item.name ?? item.value ?? '').trim()
    if (!value || !label) continue
    if (seen.has(value)) continue
    seen.add(value)
    options.push({ label, value })
  }

  return options
}

/**
 * 在品牌联动车系列表顶部补一个“空车系”选项。
 * 这里直接使用字符串 `'null'`，避免 `el-select-v2` 将真实 `null` 识别为未选中态。
 * @param options 品牌对应的车系候选项
 * @returns 顶部带 `-` 选项的新列表
 */
const prependEmptyCarSeriesOption = (options: SelectOption[]): SelectOption[] => {
  return [{ label: '-', value: 'null' }, ...options]
}

const normalizeCorrectionRowInput = (
  raw: unknown
): Required<CorrectionRowInput> & Record<string, any> => {
  const record =
    raw && typeof raw === 'object' && raw.constructor === Object ? (raw as Record<string, any>) : {}

  return {
    ...record,
    id: (record.id as any) ?? '',
    brandName: (record.brandName as any) ?? '',
    carSeriesName: (record.carSeriesName as any) ?? '',
    originalTextScene: (record.originalTextScene as any) ?? '',
    tagBusinessDomain: (record.tagBusinessDomain as any) ?? '',
    domTagFour: (record.domTagFour as any) ?? '',
    sentiment: (record.sentiment as any) ?? '',
    intention: (record.intention as any) ?? '',
    topic: (record.topic as any) ?? '',
    topicText: (record.topicText as any) ?? '',
    usageScenarioFirst: (record.usageScenarioFirst as any) ?? '',
    usageScenarioSecond: (record.usageScenarioSecond as any) ?? ''
  }
}

/**
 * 判断多条选中数据在指定字段上是否保持一致。
 * @param rows 已标准化的勾选数据列表
 * @param field 需要校验的一致性字段
 * @param fallbackField 主字段缺失时用于兜底比较的字段
 * @returns 一致时返回用于展示的值，不一致时返回 `*`
 */
const getConsistentFieldDisplayValue = (
  rows: Array<Required<CorrectionRowInput> & Record<string, any>>,
  field: keyof CorrectionRowInput,
  fallbackField?: keyof CorrectionRowInput
) => {
  if (!rows.length) return ''

  const values = rows.map(row => normalizeFilterText(row[field]))
  const firstValue = values[0]
  const isConsistent = values.every(value => value === firstValue)
  if (isConsistent) {
    if (field === 'topic') {
      return normalizeFilterText(rows[0].topicText) || firstValue
    }
    return firstValue
  }

  if (!fallbackField) return '*'

  const fallbackValues = rows.map(row => normalizeFilterText(row[fallbackField]))
  const firstFallbackValue = fallbackValues[0]
  return fallbackValues.every(value => value === firstFallbackValue) ? firstFallbackValue : '*'
}

const isSubmitId = (id: unknown): id is string | number =>
  id !== '' && id !== null && id !== undefined

type SoundsInfoQueryResult = { list: CorrectionRowInput[]; total: number }
const normalizeSoundsInfoQueryResult = (raw: unknown): SoundsInfoQueryResult => {
  const record = raw && typeof raw === 'object' ? (raw as Record<string, any>) : {}
  const listRaw = Array.isArray(record.list) ? record.list : []
  const list = listRaw.map(normalizeCorrectionRowInput)

  const totalRaw = record.total
  const totalNumber = typeof totalRaw === 'number' ? totalRaw : Number(totalRaw)
  const total = Number.isFinite(totalNumber) ? totalNumber : list.length
  return { list, total }
}

const querying = ref(false)

const fetchSoundsInfo = async () => {
  const dataIdList = normalizeDataIdList(props.dataIdList)
  if (!dataIdList.length) {
    listData.value = []
    multipleSelection.value = []
    pagination.total = 0
    updateCorrectionTableAfterValues()
    return
  }

  querying.value = true
  try {
    const filters = buildSoundsInfoFilters()
    const params = getRealAttr({
      dataIdList,
      pageNum: pagination.page,
      pageSize: pagination.limit,
      ...filters,
      ...buildCorrectionQueryTimeRange()
    })

    const res = await querySoundsInfo(params)
    const normalized = normalizeSoundsInfoQueryResult(res.result)
    listData.value = normalized.list
    pagination.total = normalized.total

    multipleSelection.value = []
    updateCorrectionTableAfterValues()
  } catch (error: any) {
    console.error('querySoundsInfo failed:', error)
    listData.value = []
    multipleSelection.value = []
    pagination.total = 0
    updateCorrectionTableAfterValues()
    ElMessage.error(error?.message || '查询纠错数据失败')
  } finally {
    querying.value = false
  }
}

// 根据勾选的数据回填“编辑前数据值”（未勾选则清空）
const updateCorrectionTableAfterValues = () => {
  const selection = multipleSelection.value
  if (selection.length === 0) {
    tableData.value.forEach(r => (r.afterValue = ''))
    return
  }

  const normalized = selection.map(normalizeCorrectionRowInput)

  if (normalized.length === 1) {
    setCorrectionAfterValue('品牌', normalized[0].brandName)
    setCorrectionAfterValue('车系', normalized[0].carSeriesName)
    // 当前“修正字段值”表格已隐藏情感、意图行，相关回填逻辑先注释保留。
    // setCorrectionAfterValue('情感', normalized[0].sentiment)
    // setCorrectionAfterValue('意图', normalized[0].intention)
    setCorrectionAfterValue('观点', normalized[0].topicText)
    setCorrectionAfterValue('用车场景', normalized[0].usageScenarioSecond)
  } else {
    // 多选场景下，品牌、车系、观点、用车场景都遵循一致性展示规则：
    // 字段值一致时回显真实值，不一致时继续展示 `*`。
    setCorrectionAfterValue('品牌', getConsistentFieldDisplayValue(normalized, 'brandName'))
    setCorrectionAfterValue('车系', getConsistentFieldDisplayValue(normalized, 'carSeriesName'))
    // setCorrectionAfterValue('情感', getConsistentFieldDisplayValue(normalized, 'sentiment'))
    // setCorrectionAfterValue('意图', getConsistentFieldDisplayValue(normalized, 'intention'))
    setCorrectionAfterValue(
      '观点',
      getConsistentFieldDisplayValue(normalized, 'topic', 'topicText')
    )
    setCorrectionAfterValue(
      '用车场景',
      getConsistentFieldDisplayValue(normalized, 'usageScenarioSecond')
    )
  }
}

/**
 * 提交成功后仅清空本次编辑态，保留当前筛选、列表与分页上下文。
 * 这样用户无需重新打开弹窗或重新筛选，就可以继续处理下一批数据。
 */
const clearSubmittedCorrectionState = () => {
  resetCorrectionBeforeValues()
  multipleSelection.value = []

  // “车系”候选项依赖“品牌”选择，提交后同步清空，避免复用上一次编辑上下文。
  correctionCarSeriesRequestId.value += 1
  correctionCarSeriesOptions.value = []

  updateCorrectionTableAfterValues()
}

const resetBeforeValues = () => {
  resetCorrectionBeforeValues()
  errorType.value = '2'
  correctionCarSeriesRequestId.value += 1
  correctionCarSeriesOptions.value = []

  // Reset filters
  filterParams.brandCode = []
  filterParams.sentiment = []
  filterParams.intention = []
  filterParams.carSeries = []
  filterParams.topicList = []

  filterCarSeriesRequestId.value += 1
  filterCarSeriesOptions.value = []
  listData.value = []
  multipleSelection.value = []
  pagination.page = 1
  pagination.limit = 500
  pagination.total = 0
  updateCorrectionTableAfterValues()
}

const handleDialogClose = () => {
  resetBeforeValues()
}

watch(
  () => visible.value,
  async val => {
    if (!val) return

    pagination.page = 1
    await Promise.all([
      fetchSoundsInfo(),
      ensureFilterBrandOptionsLoaded(),
      ensureCorrectionBrandOptionsLoaded(),
      ensureFilterTopicOptionsLoaded(),
      ensureCorrectionTopicOptionsLoaded(),
      ensureCorrectionUsageScenarioOptionsLoaded()
    ])
  }
)

// Filter Brand Change
const normalizeFilterText = (raw: unknown): string => {
  if (raw == null) return ''
  return String(raw).trim()
}

const normalizeFilterTextList = (raw: unknown): string[] => {
  if (Array.isArray(raw)) {
    return raw.map(item => normalizeFilterText(item)).filter(Boolean)
  }
  const text = normalizeFilterText(raw)
  return text ? [text] : []
}

const filterCarSeriesRequestId = ref(0)
const fetchFilterCarSeriesOptions = async (brandCodes: string[]) => {
  const dataIdList = normalizeDataIdList(props.dataIdList)
  if (!dataIdList.length || !brandCodes.length) {
    filterCarSeriesRequestId.value += 1
    filterCarSeriesOptions.value = []
    return
  }

  const requestId = ++filterCarSeriesRequestId.value
  filterCarSeriesOptions.value = []
  try {
    const res = await queryCarSeriesList({
      dataIdList,
      brandCode: brandCodes,
      ...buildCorrectionQueryTimeRange()
    })
    if (requestId !== filterCarSeriesRequestId.value) return
    filterCarSeriesOptions.value = toCarSeriesOptions(normalizeCarSeriesList(res.result))
  } catch (error: any) {
    if (requestId !== filterCarSeriesRequestId.value) return
    console.error('queryCarSeriesList failed:', error)
    filterCarSeriesOptions.value = []
    ElMessage.error(error?.message || '获取车系失败')
  }
}

const fetchCorrectionCarSeriesOptions = async (brandCodeInput: unknown) => {
  const brandCode = normalizeFilterText(brandCodeInput)
  if (!brandCode) {
    correctionCarSeriesRequestId.value += 1
    correctionCarSeriesOptions.value = []
    return
  }

  const requestId = ++correctionCarSeriesRequestId.value
  correctionCarSeriesOptions.value = []
  try {
    const res = await queryAllCarSeriesList({ brandCode, status: '1' })
    if (requestId !== correctionCarSeriesRequestId.value) return
    correctionCarSeriesOptions.value = prependEmptyCarSeriesOption(
      toCarSeriesOptions(normalizeCarSeriesList(res.result))
    )
  } catch (error: any) {
    if (requestId !== correctionCarSeriesRequestId.value) return
    console.error('queryAllCarSeriesList failed:', error)
    correctionCarSeriesOptions.value = []
    ElMessage.error(error?.message || '获取车系失败')
  }
}

type SoundsInfoFilters = {
  sentiment?: string[]
  intention?: string[]
  brandCode?: string[]
  carSeries?: string[]
  topicList?: string[]
}

const buildSoundsInfoFilters = (): SoundsInfoFilters => {
  const brandCode = normalizeFilterTextList(filterParams.brandCode)
  const carSeries = normalizeFilterTextList(filterParams.carSeries)
  const sentiment = normalizeFilterTextList(filterParams.sentiment)
  const intention = normalizeFilterTextList(filterParams.intention)
  const topicList = normalizeFilterTextList(filterParams.topicList)

  return {
    brandCode: brandCode.length ? brandCode : undefined,
    carSeries: carSeries.length ? carSeries : undefined,
    sentiment: sentiment.length ? sentiment : undefined,
    intention: intention.length ? intention : undefined,
    topicList: topicList.length ? topicList : undefined
  }
}

const handleFilterChange = () => {
  if (!visible.value) return
  pagination.page = 1
  fetchSoundsInfo()
}

const handleFilterBrandChange = (brandValue: unknown) => {
  const brandCodes = normalizeFilterTextList(brandValue)
  filterParams.brandCode = brandCodes
  filterParams.carSeries = []
  void fetchFilterCarSeriesOptions(brandCodes)
  handleFilterChange()
}

// Correction Brand Change
const handleCorrectionBrandChange = (brandValue: unknown) => {
  const carSeriesRow = tableData.value.find(row => row.field === '车系')
  if (carSeriesRow) carSeriesRow.beforeValue = ''
  void fetchCorrectionCarSeriesOptions(brandValue)
}

type TopicOption = {
  code: string
  name: string
  emotion?: string
  intention?: string
}
const filterTopicOptions = ref<TopicOption[]>([])
const filterTopicOptionsLoaded = ref(false)
const filterTopicOptionsLoadedKey = ref('')
const filterTopicOptionsRequestId = ref(0)

const correctionTopicOptions = ref<TopicOption[]>([])
const correctionTopicOptionsLoaded = ref(false)
const correctionTopicOptionsRequestId = ref(0)

const topicFilterSelectRef = ref<any>(null)

const normalizeTopicOptions = (raw: unknown): TopicOption[] => {
  if (!Array.isArray(raw)) return []
  const optionMap = new Map<string, TopicOption>()
  for (const item of raw) {
    const record = item && typeof item === 'object' ? (item as Record<string, any>) : {}
    const code = String(record.code ?? record.tagCode ?? '').trim()
    const name = String(record.name ?? record.tagName ?? '').trim()
    if (!code || !name) continue

    const emotion = normalizeFilterText(record.emotion ?? record.sentiment)
    const intention = normalizeFilterText(record.intention ?? record.intentionType)
    const existing = optionMap.get(code)

    if (existing) {
      if (!existing.emotion && emotion) existing.emotion = emotion
      if (!existing.intention && intention) existing.intention = intention
      continue
    }

    optionMap.set(code, {
      code,
      name,
      emotion: emotion || undefined,
      intention: intention || undefined
    })
  }
  return Array.from(optionMap.values())
}

const normalizeTopicCodesFromSelectV2 = (raw: unknown): string[] | null => {
  if (!Array.isArray(raw)) return null
  const seen = new Set<string>()
  const codes: string[] = []
  for (const item of raw) {
    const record = item && typeof item === 'object' ? (item as Record<string, any>) : {}
    if (record.type === 'Group') continue
    const code = String(record.code ?? '').trim()
    if (!code) continue
    if (seen.has(code)) continue
    seen.add(code)
    codes.push(code)
  }
  return codes
}

const topicDisplayedCodes = computed(() => {
  const raw = topicFilterSelectRef.value?.filteredOptions
  const codes = normalizeTopicCodesFromSelectV2(raw)
  if (codes !== null) return codes
  return filterTopicOptions.value.map(item => item.code)
})

const selectedTopicDisplayedCount = computed(() => {
  const displayedSet = new Set(topicDisplayedCodes.value)
  return filterParams.topicList.filter(code => displayedSet.has(code)).length
})

const checkAll = computed({
  get: () => {
    const displayedCount = topicDisplayedCodes.value.length
    if (!displayedCount) return false
    return selectedTopicDisplayedCount.value === displayedCount
  },
  set: val => {
    const checked = Boolean(val)
    const displayedCodes = topicDisplayedCodes.value
    if (!displayedCodes.length) return

    const displayedSet = new Set(displayedCodes)
    if (checked) {
      const merged = [...filterParams.topicList, ...displayedCodes]
      filterParams.topicList = Array.from(new Set(merged))
    } else {
      filterParams.topicList = filterParams.topicList.filter(code => !displayedSet.has(code))
    }
    handleFilterChange()
  }
})

const indeterminate = computed(() => {
  const displayedCount = topicDisplayedCodes.value.length
  if (!displayedCount) return false
  const selectedCount = selectedTopicDisplayedCount.value
  return selectedCount > 0 && selectedCount < displayedCount
})

const ensureFilterTopicOptionsLoaded = async () => {
  const dataIdList = normalizeDataIdList(props.dataIdList)
  const loadedKey = buildCorrectionQueryCacheKey(dataIdList)
  if (filterTopicOptionsLoaded.value && filterTopicOptionsLoadedKey.value === loadedKey) return

  filterTopicOptionsLoadedKey.value = loadedKey
  filterTopicOptions.value = []
  filterTopicOptionsLoaded.value = false

  if (!dataIdList.length) {
    filterTopicOptionsRequestId.value += 1
    filterTopicOptionsLoaded.value = true
    return
  }

  const requestId = ++filterTopicOptionsRequestId.value
  try {
    const res = await findAllFinalTagLibClientVoList({
      dataIdList,
      ...buildCorrectionQueryTimeRange()
    })
    if (requestId !== filterTopicOptionsRequestId.value) return
    filterTopicOptions.value = normalizeTopicOptions(res.result)
  } catch {
    if (requestId !== filterTopicOptionsRequestId.value) return
    filterTopicOptions.value = []
  } finally {
    if (requestId === filterTopicOptionsRequestId.value) {
      filterTopicOptionsLoaded.value = true
    }
  }
}

const ensureCorrectionTopicOptionsLoaded = async () => {
  if (correctionTopicOptionsLoaded.value) return

  correctionTopicOptions.value = []
  correctionTopicOptionsLoaded.value = false

  const requestId = ++correctionTopicOptionsRequestId.value
  try {
    const res = await findTopicList()
    if (requestId !== correctionTopicOptionsRequestId.value) return
    correctionTopicOptions.value = normalizeTopicOptions(res.result)
  } catch {
    if (requestId !== correctionTopicOptionsRequestId.value) return
    correctionTopicOptions.value = []
  } finally {
    if (requestId === correctionTopicOptionsRequestId.value) {
      correctionTopicOptionsLoaded.value = true
    }
  }
}

type UsageScenarioOption = CascaderOption & {
  label: string
  value: string
  children?: UsageScenarioOption[]
}

const correctionUsageScenarioOptions = ref<UsageScenarioOption[]>([])
const correctionUsageScenarioOptionsLoaded = ref(false)
const correctionUsageScenarioOptionsRequestId = ref(0)
const correctionUsageScenarioCascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: true,
  checkStrictly: false
} satisfies CascaderProps

/**
 * 将用车场景接口树转换为 Cascader 选项，只保留具备二级节点的新场景数据。
 * @param tree 用车场景分类树
 * @returns Cascader 可用的二级末级单选树
 */
const normalizeUsageScenarioOptions = (tree: DataPlazaConditionOption[]): UsageScenarioOption[] => {
  return tree.flatMap(item => {
    if (!item.children?.length) return []

    return [
      {
        label: item.value,
        value: item.value,
        children: item.children.map(child => ({
          label: child.value,
          value: child.value
        }))
      }
    ]
  })
}

/**
 * 加载用车场景纠错候选项。
 * 数据源来自 /report/data-plaza/conditions 的 carScene 分组，弹窗生命周期内只加载一次。
 */
const ensureCorrectionUsageScenarioOptionsLoaded = async () => {
  if (correctionUsageScenarioOptionsLoaded.value) return

  correctionUsageScenarioOptions.value = []
  correctionUsageScenarioOptionsLoaded.value = false

  const requestId = ++correctionUsageScenarioOptionsRequestId.value
  try {
    const res = await getDataPlazaConditions()
    if (requestId !== correctionUsageScenarioOptionsRequestId.value) return
    const carSceneGroup = (res.result || []).find(item => item.key === 'carScene')
    correctionUsageScenarioOptions.value = normalizeUsageScenarioOptions(
      carSceneGroup?.details || []
    )
  } catch (error) {
    if (requestId !== correctionUsageScenarioOptionsRequestId.value) return
    console.error('获取用车场景失败', error)
    correctionUsageScenarioOptions.value = []
    ElMessage.error('获取用车场景失败')
  } finally {
    if (requestId === correctionUsageScenarioOptionsRequestId.value) {
      correctionUsageScenarioOptionsLoaded.value = true
    }
  }
}

/**
 * 统一提取纠错弹窗需要透传的时间范围。
 * 优先使用父层显式传入的筛选条件，兜底沿用当前 queryStore 中的时间字段。
 */
const buildCorrectionQueryTimeRange = (): CorrectionQueryTimeRange => {
  const f = props.filter ?? (queryStore.currentQueryParams as any)
  const startTime = normalizeFilterText(f?.startTime ?? f?.startDate)
  const endTime = normalizeFilterText(f?.endTime ?? f?.endDate)

  return {
    startTime: startTime || undefined,
    endTime: endTime || undefined
  }
}

/**
 * 基于数据 ID 与时间范围生成缓存键，避免不同时间条件下复用旧的下拉选项。
 */
const buildCorrectionQueryCacheKey = (dataIdList: string[]) => {
  const { startTime, endTime } = buildCorrectionQueryTimeRange()
  return `${dataIdList.join(',')}|${startTime || ''}|${endTime || ''}`
}

/**
 * 根据当前“观点”选中值回查完整标准观点对象。
 * 纠错接口仍使用隐藏字段提交情感、意图，因此这里需要保留并读取候选项上的扩展信息。
 */
const getSelectedCorrectionTopicOption = () => {
  const topicCode = getNormalizedCorrectionBeforeValue('观点')
  if (!topicCode) return undefined
  return correctionTopicOptions.value.find(item => item.code === topicCode)
}

/**
 * 将用车场景级联路径拆分为接口字段，只接受一级、二级完整路径。
 * @returns 纠错接口需要的用车场景一、二级字段
 */
const buildUsageScenarioSubmitParams = () => {
  const selectedPath = getCorrectionBeforeValue('用车场景')
  if (!Array.isArray(selectedPath) || selectedPath.length !== 2) {
    return { usageScenarioFirst: '', usageScenarioSecond: '' }
  }

  return {
    usageScenarioFirst: normalizeFilterText(selectedPath[0]),
    usageScenarioSecond: normalizeFilterText(selectedPath[1])
  }
}

const buildSubmitParams = (): InsertLabelCorrectionParams => {
  // 仅提交当前勾选的记录
  const targetItems = multipleSelection.value

  const ids = targetItems
    .map(normalizeCorrectionRowInput)
    .map(item => item.id)
    .filter(isSubmitId)
  const brandCode = getNormalizedCorrectionBeforeValue('品牌')
  const carSeriesCode = normalizeFilterText(getCorrectionBeforeValue('车系'))
  const selectedTopicOption = getSelectedCorrectionTopicOption()
  // 当前接口字段名仍为 sentiment，这里按需求写入观点对象上的 emotion。
  const sentiment = normalizeFilterText(selectedTopicOption?.emotion)
  const intention = normalizeFilterText(selectedTopicOption?.intention)
  // const sentiment = getCorrectionBeforeValue('情感')
  // const intention = getCorrectionBeforeValue('意图')
  const topicCode = getNormalizedCorrectionBeforeValue('观点')
  const usageScenario = buildUsageScenarioSubmitParams()

  const brandName = correctionBrandOptions.value.find(item => item.value === brandCode)?.label || ''
  const carSeriesName =
    carSeriesCode === 'null'
      ? 'null'
      : correctionCarSeriesOptions.value.find(item => item.value === carSeriesCode)?.label || ''
  const topicName = selectedTopicOption?.name || ''

  const range = buildCorrectionQueryTimeRange()
  return {
    newId: ids,
    errorType: errorType.value,
    brandCode,
    carSeriesCode,
    sentiment,
    intention,
    topicCode,
    brandName,
    carSeriesName,
    topicName,
    ...usageScenario,
    ...range
  }
}

const handleConfirm = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择需要纠错的数据')
    return
  }
  const params = buildSubmitParams()
  if (!params.newId.length) {
    ElMessage.warning('所选数据缺少可提交的ID')
    return
  }

  const loading = ElLoading.service({
    lock: true,
    text: '数据提交中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    const res = (await insertLabelCorrection(params)) as any
    if (res.success) {
      ElMessage.success('纠错申请已提交')
      clearSubmittedCorrectionState()
      emit('success')
      // 暂时保留弹窗，纠错成功后不再主动关闭。
      // close()
      return
    }
    ElMessage.warning(res.message || '操作失败')
  } catch (error: any) {
    ElMessage.warning(error?.message || '操作失败')
  } finally {
    loading.close()
  }
}

// Pagination Handlers (Mock)
const handleSizeChange = (val: number) => {
  pagination.limit = val
  pagination.page = 1
  fetchSoundsInfo()
}
const handleCurrentChange = (val: number) => {
  pagination.page = val
  fetchSoundsInfo()
}
</script>

<style lang="scss" scoped>
.error-correction-dialog__header {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 12px 16px;
  flex-wrap: wrap;
}

.error-correction-dialog__header-title {
  flex: 0 0 auto;
  line-height: 32px;
  white-space: nowrap;
}

.error-correction-dialog__header-filters {
  flex: 1 1 760px;
  min-width: 0;
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}

.error-correction-dialog__content {
  .error-correction-dialog__correction-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .error-correction-dialog__validity-header {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .subtitle-desc {
    font-weight: 400;
    font-size: 14px;
    color: #86909c;
    margin-left: 8px;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
  }

  .data-list-table-container {
    height: 300px;

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
  }

  .divider {
    height: 1px;
    background-color: #e5e6eb;
    margin-top: 24px;
  }

  .error-correction-dialog__validity-switch {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .error-correction-dialog__validity-item {
    padding: 6px 14px;
    font-weight: 500;
    font-size: 14px;
    color: #535862;
    line-height: 20px;
    border-radius: 4px;
    border: 1px solid #dfe2e8;
    cursor: pointer;
  }

  .error-correction-dialog__validity-item.error-correction-dialog__validity-item--active {
    border: 1px solid #1677ff;
    color: #1677ff;
  }

  :deep(.el-table thead th.el-table__cell) {
    font-weight: 600;
    font-size: 14px;
    color: #1d2129;
    line-height: 22px;
    background-color: #f7f8fa;
  }

  :deep(.el-table-v2__header-cell) {
    font-weight: 600;
    font-size: 14px;
    color: #1d2129;
    line-height: 22px;
    background-color: #f7f8fa;
  }

  :deep(.text-ellipsis) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.error-correction-dialog__subtitle {
  font-weight: 600;
  font-size: 16px;
  color: #1d2129;
  line-height: 24px;
  display: flex;
  align-items: center;
}

:deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.selectV2PopClass) {
  /* custom style if needed */
}
</style>

<style lang="scss">
.error-correction-dialog.app-dialog .app-dialog__header {
  height: auto;
  min-height: 64px;
  padding: 16px 56px 16px 24px;
  align-items: flex-start;
  box-sizing: border-box;
}

.error-correction-dialog__car-series-popper {
  width: 16em !important;

  .el-select-dropdown {
    width: 100% !important;

    .el-vl__wrapper {
      width: 100% !important;
    }

    .el-select-dropdown__list {
      width: 100% !important;
    }
  }
}

.error-correction-dialog__table-tooltip {
  max-width: min(520px, calc(100vw - 32px));
}

.error-correction-dialog__table-tooltip-content {
  max-height: min(320px, calc(100vh - 48px));
  overflow: auto;
  box-sizing: border-box;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  line-height: 20px;
}
</style>
