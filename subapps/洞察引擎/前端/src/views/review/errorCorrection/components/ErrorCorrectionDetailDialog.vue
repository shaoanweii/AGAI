<template>
  <el-dialog
    v-model="visible"
    :title="isReadOnly ? '纠错详情（查看）' : '纠错详情（审核）'"
    width="95%"
    style="padding: 0; border-radius: 8px"
    header-class="user-dialog-form-header-class"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="review-setps">
        <div class="rs-item">
          <div class="rs-icon">
            <img src="@/assets/imgs/success.png" alt="" />
          </div>
          <div class="rs-info ml-8">
            <div>
              <span>{{ detailObj.submitUserName }}</span> <span class="ml-4 mr-4">|</span>
              <span>{{ detailObj.submitUserEmployeeID }}</span>
              <span class="ml-8">提交纠错申请</span>
            </div>
            <div class="rsi-time mt-4">{{ detailObj.submitTime }}</div>
          </div>
        </div>

        <template v-if="detailObj.auditUserEmployeeID">
          <div class="rs-dividing-line"></div>

          <div class="rs-item">
            <div class="rs-icon">
              <img v-if="rowData.auditStatusCode === '1'" src="@/assets/imgs/success.png" alt="" />
              <img
                v-if="['2', '3'].includes(rowData.auditStatusCode)"
                src="@/assets/imgs/audiStatus-2.png"
                alt=""
              />
            </div>
            <div class="rs-info ml-8">
              <div>
                <span>{{ detailObj.auditUserName }}</span> <span class="ml-4 mr-4">|</span>
                <span>{{ detailObj.auditUserEmployeeID }}</span>
                <span class="ml-8">
                  <span v-if="rowData.auditStatusCode === '1'">审核通过</span>
                  <span v-if="rowData.auditStatusCode === '2'">审核失败</span>
                  <span v-if="rowData.auditStatusCode === '3'">审核撤销</span>
                </span>
              </div>
              <div class="rsi-time mt-4">{{ detailObj.auditTime }}</div>
            </div>
          </div>
        </template>
      </div>
      <div class="subtitle mt-24">数据信息</div>
      <div
        class="table-container mt-16"
        :style="{
          maxHeight: '300px',
          height: (table.list?.length ? Math.min(300, table.list.length * 50 + 50) : 300) + 'px'
        }"
      >
        <el-auto-resizer>
          <template #default="slotProps">
            <el-table-v2
              v-loading="table.loading"
              :columns="dataColumns"
              :data="table.list || []"
              :width="slotProps.width"
              :height="slotProps.height"
              :row-key="'id'"
              fixed
            />
          </template>
        </el-auto-resizer>
      </div>
      <el-pagination
        v-if="table.total > 0"
        v-model:current-page="table.pageNum"
        v-model:page-size="table.pageSize"
        :page-sizes="[10, 20, 50, 100, 200, 500]"
        :total="table.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 16px; justify-content: flex-end"
      />

      <div class="flex-between mt-24 items-center">
        <div class="subtitle">修正字段值</div>

        <div class="flex items-center">
          <div class="subtitle">数据有效性</div>
          <div class="custome-switch-btn-wrap ml-10">
            <div
              class="csb-item"
              :class="{
                'csb-tap': errorType === '2',
                'csb-disabled': true
              }"
            >
              有效数据
            </div>
            <div
              class="csb-item"
              :class="{
                'csb-tap': errorType === '1',
                'csb-disabled': true
              }"
            >
              无效数据
            </div>
          </div>
        </div>
      </div>

      <el-table :data="tableData" size="large" style="width: 100%" class="mt-16">
        <el-table-column prop="field" label="字段" />
        <el-table-column prop="dataValueBefore" label="编辑前数据值" />
        <el-table-column prop="dataValueAfter" label="修改为数据值">
          <template #default="{ row }">
            {{ formatCorrectionDisplayValue(row.field, row.dataValueAfter) }}
          </template>
          <!-- <template #default="{ row }">
            <template v-if="row.field === '品牌'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="品牌"
                clearable
                filterable
                :options="brandOptions"
                @change="handleBrandChange"
              />
            </template>
            <template v-if="row.field === '车系'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="车系"
                clearable
                filterable
                :options="carSeriesOptions"
              />
            </template>
            <template v-if="row.field === '情感'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="情感"
                clearable
                :options="sentimentOptions"
              />
            </template>
            <template v-if="row.field === '意图'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="意图"
                clearable
                :options="intentionOptions"
              />
            </template>
            <template v-if="row.field === '观点'">
              <el-select-v2
                v-model="row.dataValueBefore"
                placeholder="观点"
                clearable
                filterable
                :options="topicOptions"
                :props="{ label: 'tagName', value: 'tagCode' }"
              />
            </template>
          </template> -->
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <div class="dialog-footer flex-y-center">
        <div
          v-if="!isReadOnly && rowData.auditStatusCode === '0'"
          class="footer-btn-layout flex-y-center"
        >
          <!-- 1通过， 2拒绝， 3撤销 -->
          <el-button
            class="flex-1"
            :loading="submitLoading && activeAuditAction === 3"
            :disabled="submitLoading"
            @click="handleSubmit(3)"
          >
            撤销
          </el-button>
          <el-button
            class="flex-1"
            :loading="submitLoading && activeAuditAction === 2"
            :disabled="submitLoading"
            @click="handleSubmit(2)"
          >
            拒绝
          </el-button>
          <el-button
            class="flex-1"
            type="primary"
            :loading="submitLoading && activeAuditAction === 1"
            :disabled="submitLoading"
            @click="handleSubmit(1)"
          >
            通过
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { auditLabelCorrection, queryCorrectionInfo } from '@/api/review'
import { useTable } from '@/hooks/table'
import type { ConditionsDetailItem } from '@/types'
import { ref, h } from 'vue'
import { ElTooltip, TableV2FixedDir } from 'element-plus'
import type { Column } from 'element-plus'
import { showOverflowTooltipConfig } from '@/constant/index'
// import { queryRoleALlList, updateAccountInfo } from '@api/user'

defineOptions({
  name: 'ErrorCorrectionDetailDialog'
})

interface Props {
  // visible: boolean
  // topicOptions: any[]
  rowData: any
  isReadOnly?: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

const visible = defineModel('visible', {
  default: false
})

const { rowData, isReadOnly = false } = defineProps<Props>()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const tableData = ref([
  {
    field: '品牌',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '车系',
    afterValue: '',
    beforeValue: ''
  },
  {
    field: '观点',
    afterValue: '',
    beforeValue: ''
  }
  // 当前详情弹窗暂不展示情感、意图修正项，先注释保留初始化结构，便于后续按需恢复。
  // {
  //   field: '情感',
  //   afterValue: '',
  //   beforeValue: ''
  // },
  // {
  //   field: '意图',
  //   afterValue: '',
  //   beforeValue: ''
  // }
  // {
  //   field: '场景',
  //   afterValue: '',
  //   beforeValue: ''
  // }
])

// 响应式数据
const submitLoading = ref(false)
// 记录当前触发的审核动作，确保仅对应按钮展示 loading
const activeAuditAction = ref<number | null>(null)

// 品牌选项
const brandOptions = computed(
  () => conditions.brandCar?.map(item => ({ label: item.value, value: item.key })) || []
)

// 车系选项 - 根据选中的品牌动态更新
const carSeriesOptions = ref<any[]>([])

// 情感选项
const sentimentOptions = computed(
  () => conditions.vocSentiment?.map(item => ({ label: item.value, value: item.key })) || []
)

// 意图选项
const intentionOptions = computed(
  () => conditions.vocIntention?.map(item => ({ label: item.value, value: item.key })) || []
)

// 处理品牌变化
const handleBrandChange = (brandValue: string) => {
  // 清空车系选择
  const carSeriesRow = tableData.value.find(row => row.field === '车系') as any
  if (carSeriesRow) {
    // carSeriesRow.beforeValue = ''
    carSeriesRow.dataValueBefore = ''
  }

  // 更新车系选项
  if (brandValue) {
    const selectedBrand = conditions.brandCar?.find(item => item.key === brandValue)
    carSeriesOptions.value =
      selectedBrand?.children?.map(child => ({
        label: child.value,
        value: child.key
      })) || []
  } else {
    carSeriesOptions.value = []
  }
}

type ErrorType = '1' | '2'

const errorType = ref<ErrorType>('2')

const switchErrorType = (type: ErrorType) => {
  errorType.value = type
}

// 数据信息表格列配置
const dataColumns: Column[] = [
  {
    key: 'dataId',
    title: '原始数据ID',
    dataKey: 'dataId',
    width: 280,
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
    key: 'originalText',
    title: '原始声音',
    dataKey: 'originalText',
    width: 240,
    cellRenderer: ({ cellData }) =>
      h(ElTooltip, { content: cellData, placement: 'top', ...showOverflowTooltipConfig }, () =>
        h('div', { class: 'text-ellipsis' }, cellData)
      )
  },
  {
    key: 'originalTextScene',
    title: '声音片段',
    dataKey: 'originalTextScene',
    width: 240,
    cellRenderer: ({ cellData }) =>
      h(ElTooltip, { content: cellData, placement: 'top', ...showOverflowTooltipConfig }, () =>
        h('div', { class: 'text-ellipsis' }, cellData)
      )
  },
  {
    key: 'brandName',
    title: '品牌',
    dataKey: 'brandName',
    width: 80,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'carSeriesName',
    title: '车系',
    dataKey: 'carSeriesName',
    width: 80,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'topicText',
    title: '标准观点',
    dataKey: 'topicText',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'opinion',
    title: '原始观点',
    dataKey: 'opinion',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'sentiment',
    title: '情感',
    dataKey: 'sentiment',
    width: 80,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'intention',
    title: '意图',
    dataKey: 'intention',
    width: 80,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  // 当前详情页只展示审核必需字段，“内容类型”先直接注释隐藏。
  // {
  //   key: 'contentType',
  //   title: '内容类型',
  //   dataKey: 'contentType',
  //   width: 120,
  //   cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  // },
  {
    key: 'usageScenarioFirst',
    title: '用车场景一级',
    dataKey: 'usageScenarioFirst',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'usageScenarioSecond',
    title: '用车场景二级',
    dataKey: 'usageScenarioSecond',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  /*
   * 当前详情页只展示审核必需字段，以下数据信息字段先直接注释隐藏：
   * 声音ID、渠道相关、车企车型、发布时间、热词关键词、旅程标签、
   * 各类标签字段、作者与互动字段、问卷字段、客户字段、车辆字段、组织字段等。
  {
    key: 'id',
    title: '声音ID',
    dataKey: 'id',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isOuter',
    title: '一级渠道分类',
    dataKey: 'isOuter',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'secondChannelName',
    title: '二级渠道分类',
    dataKey: 'secondChannelName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'channelName',
    title: '渠道名称',
    dataKey: 'channelName',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'seriesFactory',
    title: '车企名称',
    dataKey: 'seriesFactory',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'modelName',
    title: '车型名称',
    dataKey: 'modelName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'publishTime',
    title: '发布时间',
    dataKey: 'publishTime',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'hotWord',
    title: '热词',
    dataKey: 'hotWord',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'keywords',
    title: '关键词',
    dataKey: 'keywords',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'userJourney1',
    title: '用户旅程一级',
    dataKey: 'userJourney1',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'userJourney2',
    title: '用户旅程二级',
    dataKey: 'userJourney2',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'userJourney3',
    title: '用户旅程三级',
    dataKey: 'userJourney3',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'topic',
    title: '标准观点编码',
    dataKey: 'topic',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cptTagFirst',
    title: 'CPT标签1级',
    dataKey: 'cptTagFirst',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cptTagSecond',
    title: 'CPT标签2级',
    dataKey: 'cptTagSecond',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cptTagThree',
    title: 'CPT标签3级',
    dataKey: 'cptTagThree',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cptTagFour',
    title: 'CPT标签4级',
    dataKey: 'cptTagFour',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'ujyTagFirst',
    title: '全旅程客户标签1级',
    dataKey: 'ujyTagFirst',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'ujyTagSecond',
    title: '全旅程客户标签2级',
    dataKey: 'ujyTagSecond',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'ujyTagThree',
    title: '全旅程客户标签3级',
    dataKey: 'ujyTagThree',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'ujyTagFour',
    title: '全旅程客户标签4级',
    dataKey: 'ujyTagFour',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cmaTagFirst',
    title: 'CMA标签1级',
    dataKey: 'cmaTagFirst',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cmaTagSecond',
    title: 'CMA标签2级',
    dataKey: 'cmaTagSecond',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cmaTagThree',
    title: 'CMA标签3级',
    dataKey: 'cmaTagThree',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'cmaTagFour',
    title: 'CMA标签4级',
    dataKey: 'cmaTagFour',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'domTagFirst',
    title: '全领域业务标签1级',
    dataKey: 'domTagFirst',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'domTagSecond',
    title: '全领域业务标签2级',
    dataKey: 'domTagSecond',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'domTagThree',
    title: '全领域业务标签3级',
    dataKey: 'domTagThree',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'domTagFour',
    title: '全领域业务标签4级',
    dataKey: 'domTagFour',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'npsTagFirst',
    title: 'NPS标签1级',
    dataKey: 'npsTagFirst',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'npsTagSecond',
    title: 'NPS标签2级',
    dataKey: 'npsTagSecond',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'npsTagThree',
    title: 'NPS标签3级',
    dataKey: 'npsTagThree',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'npsTagFour',
    title: 'NPS标签4级',
    dataKey: 'npsTagFour',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vtrTagFirst',
    title: 'VRT标签1级',
    dataKey: 'vtrTagFirst',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vtrTagSecond',
    title: 'VRT标签2级',
    dataKey: 'vtrTagSecond',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vtrTagThree',
    title: 'VRT标签3级',
    dataKey: 'vtrTagThree',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vtrTagFour',
    title: 'VRT标签4级',
    dataKey: 'vtrTagFour',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagAccuracy',
    title: '标签-准确性',
    dataKey: 'tagAccuracy',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagCustomerIssueClassification',
    title: '标签-客户问题分类',
    dataKey: 'tagCustomerIssueClassification',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagIssueSeverity',
    title: '标签-问题严重程度',
    dataKey: 'tagIssueSeverity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagCodeStatus',
    title: '标签-编码状态',
    dataKey: 'tagCodeStatus',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagBusinessDomain',
    title: '标签-业务域',
    dataKey: 'tagBusinessDomain',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagEventClarity',
    title: '标签-事件清晰度',
    dataKey: 'tagEventClarity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagHighValueFlag',
    title: '标签-高价值标识',
    dataKey: 'tagHighValueFlag',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagComplaintFlagNeedingReply',
    title: '标签-需回复投诉标识',
    dataKey: 'tagComplaintFlagNeedingReply',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagHighQualityVocFlag',
    title: '标签-高质量VOC标识',
    dataKey: 'tagHighQualityVocFlag',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagNewEnergyOrFuel',
    title: '标签-新能源/燃油',
    dataKey: 'tagNewEnergyOrFuel',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'tagNeedForvclosedLoop',
    title: '标签-需要闭环',
    dataKey: 'tagNeedForvclosedLoop',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isWsaterArmy',
    title: '是否水军',
    dataKey: 'isWsaterArmy',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isManagerFocused',
    title: '是否管理层关注',
    dataKey: 'isManagerFocused',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isBigV',
    title: '是否大V',
    dataKey: 'isBigV',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'authorId',
    title: '作者ID',
    dataKey: 'authorId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'authorNick',
    title: '用户昵称',
    dataKey: 'authorNick',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isMainPost',
    title: '是否主贴',
    dataKey: 'isMainPost',
    width: 100,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'originalLink',
    title: '原文链接',
    dataKey: 'originalLink',
    width: 180,
    cellRenderer: ({ cellData }) =>
      h(ElTooltip, { content: cellData, placement: 'top', ...showOverflowTooltipConfig }, () =>
        h('div', { class: 'text-ellipsis' }, cellData)
      )
  },
  {
    key: 'viewCount',
    title: '浏览数',
    dataKey: 'viewCount',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'commentCount',
    title: '评论数',
    dataKey: 'commentCount',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'likeCount',
    title: '点赞数',
    dataKey: 'likeCount',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'shareCount',
    title: '分享数',
    dataKey: 'shareCount',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'favoriteCount',
    title: '收藏数',
    dataKey: 'favoriteCount',
    width: 120,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'workOrderId',
    title: '工单ID',
    dataKey: 'workOrderId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'questId',
    title: '问卷ID',
    dataKey: 'questId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'questType',
    title: '问卷类型',
    dataKey: 'questType',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'questAnswerScore',
    title: '问卷答案分数',
    dataKey: 'questAnswerScore',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'questBusinessType',
    title: '问卷业务类型',
    dataKey: 'questBusinessType',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'questBusinessScenario',
    title: '问卷业务场景',
    dataKey: 'questBusinessScenario',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'd2cResponsibleDept',
    title: 'D2C负责部门',
    dataKey: 'd2cResponsibleDept',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'd2cAccountableDept',
    title: 'D2C牵头部门',
    dataKey: 'd2cAccountableDept',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'd2cCcDept',
    title: 'D2C抄送部门',
    dataKey: 'd2cCcDept',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'oneId',
    title: 'ONE_ID',
    dataKey: 'oneId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custGlobalId',
    title: '客户全局ID',
    dataKey: 'custGlobalId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custName',
    title: '用户名',
    dataKey: 'custName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custMainPhone',
    title: '客户手机号',
    dataKey: 'custMainPhone',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isCarOwner',
    title: '是否车主',
    dataKey: 'isCarOwner',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custAge',
    title: '客户年龄',
    dataKey: 'custAge',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custAgeGroup',
    title: '客户年龄段',
    dataKey: 'custAgeGroup',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custGender',
    title: '性别',
    dataKey: 'custGender',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custHighEducaion',
    title: '教育程度',
    dataKey: 'custHighEducaion',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'marrigeStatue',
    title: '婚姻状况',
    dataKey: 'marrigeStatue',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'familyIncome',
    title: '家庭收入',
    dataKey: 'familyIncome',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isExchangeFlg',
    title: '是否换购',
    dataKey: 'isExchangeFlg',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'purchaseCarTimes',
    title: '购车次数',
    dataKey: 'purchaseCarTimes',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'isMemberFlg',
    title: '是否会员',
    dataKey: 'isMemberFlg',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custProvince',
    title: '客户省份',
    dataKey: 'custProvince',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custCity',
    title: '客户城市',
    dataKey: 'custCity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custType',
    title: '客户分类',
    dataKey: 'custType',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custLivedProv',
    title: '客户居住省份',
    dataKey: 'custLivedProv',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custLivedCity',
    title: '客户居住城市',
    dataKey: 'custLivedCity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'custProfession',
    title: '客户职业',
    dataKey: 'custProfession',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlVin',
    title: 'VIN',
    dataKey: 'vhlVin',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlColorName',
    title: '车型颜色名称',
    dataKey: 'vhlColorName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlProductDate',
    title: '车辆生产日期',
    dataKey: 'vhlProductDate',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlOfflineDate',
    title: '车辆下线日期',
    dataKey: 'vhlOfflineDate',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlIsAbroad',
    title: '是否海外',
    dataKey: 'vhlIsAbroad',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlDisCh',
    title: '驱动形式-变速箱',
    dataKey: 'vhlDisCh',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlDisMt',
    title: '驱动形式-手动/自动',
    dataKey: 'vhlDisMt',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlEngClsf',
    title: '发动机分类',
    dataKey: 'vhlEngClsf',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlEngSeris',
    title: '发动机系列',
    dataKey: 'vhlEngSeris',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlVehType',
    title: '车型类型',
    dataKey: 'vhlVehType',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlCountry',
    title: '国家',
    dataKey: 'vhlCountry',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlBdClsf',
    title: '车身分类',
    dataKey: 'vhlBdClsf',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlSegMt',
    title: '细分市场',
    dataKey: 'vhlSegMt',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlPowClsf',
    title: '动力分类',
    dataKey: 'vhlPowClsf',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlFuClsf',
    title: '燃料分类',
    dataKey: 'vhlFuClsf',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlModlSt',
    title: '车型状态',
    dataKey: 'vhlModlSt',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'vhlStdPlntCode',
    title: '标准工厂代码',
    dataKey: 'vhlStdPlntCode',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrOcId',
    title: '销售组织ID',
    dataKey: 'dlrOcId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrOcName',
    title: '销售组织名称',
    dataKey: 'dlrOcName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrOcProvince',
    title: '销售组织省份',
    dataKey: 'dlrOcProvince',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrOcCity',
    title: '销售组织城市',
    dataKey: 'dlrOcCity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrDcId',
    title: '服务组织ID',
    dataKey: 'dlrDcId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrDcName',
    title: '服务组织名称',
    dataKey: 'dlrDcName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrDcProvince',
    title: '服务组织省份',
    dataKey: 'dlrDcProvince',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrDcCity',
    title: '服务组织城市',
    dataKey: 'dlrDcCity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrMcId',
    title: '经销商管理公司ID',
    dataKey: 'dlrMcId',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrMcName',
    title: '经销商管理公司名称',
    dataKey: 'dlrMcName',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrMcProvince',
    title: '经销商管理公司省份',
    dataKey: 'dlrMcProvince',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  {
    key: 'dlrMcCity',
    title: '经销商管理公司城市',
    dataKey: 'dlrMcCity',
    width: 180,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  },
  */
  {
    key: 'dataStatus',
    title: '数据状态',
    dataKey: 'dataStatus',
    width: 120,
    fixed: TableV2FixedDir.RIGHT,
    cellRenderer: ({ cellData }) => h('div', { class: 'cell-wrap-text' }, cellData)
  }
]

const { table, form, handleSizeChange, handleCurrentChange, getFirstPageTableData } = useTable(
  {
    method: 'POST',
    url: '/insights/addLabel/queryDataInfo'
  },
  res => {
    return res.result
  }
)

const detailObj = ref<any>({})
/**
 * 过滤当前弹窗暂不展示的修正字段，避免接口继续返回时仍渲染到页面上。
 * @param correctionInfoList 修正字段列表
 * @returns 过滤后的修正字段列表
 */
const filterCorrectionInfoList = (correctionInfoList: any[] = []) => {
  return correctionInfoList.filter(item => !['情感', '意图'].includes(item?.field || ''))
}

/**
 * 格式化纠错详情中的字段展示值，避免后端约定值直接暴露给用户。
 * @param field 当前修正字段
 * @param value 当前字段展示值
 * @returns 适合页面直接展示的文案
 */
const formatCorrectionDisplayValue = (field: string, value: string) => {
  // 车系传回字符串 'null' 时，前端统一展示为 '-'，其余字段和值保持原样。
  if (field === '车系' && value === 'null') return '-'
  return value
}
// 获取详情信息
const getDetail = async () => {
  try {
    const res = (await queryCorrectionInfo({
      id: rowData.id
    })) as any
    detailObj.value = res.result || {}
    errorType.value = res.result.errorType
    // 当前“修正字段值”表格不再展示情感、意图，因此在前端统一过滤对应行。
    tableData.value = filterCorrectionInfoList(res.result.correctionInfoList || [])

    // if (_correctionInfoList?.length) {
    //   // 品牌
    //   tableData.value[0].afterValue = _correctionInfoList[0].brandName
    //   // 车系
    //   tableData.value[1].afterValue = _correctionInfoList[0].carSeriesName
    //   // 情感
    //   tableData.value[2].afterValue = _correctionInfoList[0].sentiment
    //   // 意图
    //   tableData.value[3].afterValue = _correctionInfoList[0].intention
    //   // 观点
    //   tableData.value[4].afterValue = _correctionInfoList[0].opinion
    //   // 场景
    //   // tableData.value[5].afterValue = rowData[0].scenario
    // }
    //  else if (rowData?.length > 1) {
    //   // 品牌
    //   tableData.value[0].afterValue = '*'
    //   // 车系
    //   tableData.value[1].afterValue = '*'
    //   // 情感
    //   tableData.value[2].afterValue = '*'
    //   // 意图
    //   tableData.value[3].afterValue = '*'
    //   // 观点
    //   tableData.value[4].afterValue = '*'
    //   // 场景
    //   tableData.value[5].afterValue = '*'
    // }
  } catch {
    detailObj.value = {}
  }
}

const handleOpen = () => {
  console.log('rowData', rowData)
  table.filter.id = rowData.id
  getFirstPageTableData()
  getDetail()
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async (type: number) => {
  // 1通过， 2拒绝， 3撤销
  if (submitLoading.value) return

  submitLoading.value = true
  activeAuditAction.value = type

  try {
    const _params = {
      // id: rowData.id,
      idList: rowData.id ? [rowData.id] : [],
      auditStatus: type
    }
    // 审核
    const res = (await auditLabelCorrection(_params)) as any
    if (res.success) {
      emit('success')
      handleClose()
    } else {
      ElMessage.warning(res.message)
    }
  } catch (error: any) {
    ElMessage.warning(error.message)
  } finally {
    submitLoading.value = false
    activeAuditAction.value = null
  }
}
</script>

<style lang="scss">
.user-dialog-form-header-class {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #ebf4fd 0%, #ffffff 100%);
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
}
</style>
<style lang="scss" scoped>
.dialog-content {
  padding: 0 24px;
  max-height: 65vh;
  overflow-y: auto;

  .custome-switch-btn-wrap {
    display: flex;
    align-items: center;
    gap: 8px;
    .csb-item {
      padding: 6px 14px;
      font-weight: 500;
      font-size: 14px;
      color: #535862;
      line-height: 20px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #dfe2e8;
      cursor: pointer;
      &.csb-tap {
        border: 1px solid #1677ff;
        color: #1677ff;
      }
      &.csb-disabled {
        cursor: not-allowed;
        opacity: 0.5;
      }
    }
  }

  .review-setps {
    display: flex;
    align-items: center;

    .rs-dividing-line {
      flex: 1;
      height: 1px;
      background-color: #dfe2e8;
      margin: 0 24px;
    }
    .rs-item {
      display: flex;
      .rs-icon {
        width: 24px;
        height: 24px;
        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }
      .rs-info {
        font-weight: 400;
        font-size: 14px;
        color: #1f2733;
        line-height: 22px;
        .rsi-time {
          font-weight: 400;
          font-size: 12px;
          color: #5f6a7a;
          line-height: 20px;
        }
      }
    }
  }

  :deep(.el-table thead th.el-table__cell) {
    font-weight: 600;
    font-size: 14px;
    color: #1d2129;
    line-height: 22px;
  }
}
.subtitle {
  font-weight: 600;
  font-size: 16px;
  color: #1d2129;
  line-height: 24px;
}
.info-text {
  font-weight: 400;
  font-size: 14px;
  color: #1d2129;
  line-height: 22px;
}
.dialog-footer {
  height: 80px;
  border-top: 1px solid #ebedf0;
}

.footer-btn-layout {
  gap: 8px;
  width: 100%;
  padding: 0 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  &:focus-within {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }
}

:deep(.el-textarea__inner) {
  &:focus {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }
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

.table-container {
  height: 400px;
}
</style>
