<script setup lang="ts">
import dayjs from 'dayjs'
import { inject } from 'vue'
import { useTable } from '@/hooks/table'
import ChannelCascader from '@/components/ChannelCascader.vue'
import { exportSIRawDataResult } from '@/api/dataCenter'
import type { CommonObj, ConditionsDetailItem } from '@/types'
import type { DataSourceDetail } from '@/types/dataCenter.types'
import { computedCardHeight } from '@/utils'
import useUserStore from '@/stores/modules/user'
import useComputedCascaderWidth from '@/hooks/useComputedCascaderWidth'
import { useModal } from '@/hooks/useModal'
import to from 'await-to-js'
import { ElMessage } from 'element-plus'
import eventBus from '@/utils/eventBus'
import { useTagVIewData } from '@/hooks/useTagVIewData'
import CorrectSound from '@/components/CorrectSound/index.vue'

const props = withDefaults(
  defineProps<{
    curDataSource: any
    otherConditions: CommonObj
    curDataSourceDetail: DataSourceDetail | undefined
  }>(),
  {}
)
const { curDataSource, curDataSourceDetail, otherConditions } = toRefs(props)

const userStore = useUserStore()
const { rangeDisabled, rangeSelectedTime } = useTagVIewData()
const { refDom: channelRef, formatLabelHandle } = useComputedCascaderWidth()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const dynamicColumn = ref<any[]>([])
const brandCarSeries = ref<any[]>([])
const brandCarSeriesTreeOptions = ref<Record<any, any>[]>()

const {
  table,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleSortChange,
  getFirstPageTableData
} = useTable(
  {
    method: 'POST',
    url: '/insights/insDataSource/getSIRawDataResult'
  },
  res => {
    // 排除已知字段， 动态展示未知字段
    const whiteList = [
      'channelName',
      'brandName',
      'carSeriesName',
      'originalTextScene',
      'labelTypeName',
      'labelTypeLevelFirst',
      'labelTypeLevelSecond',
      'labelTypeLevelThree',
      'labelTypeLevelFour',
      'opinion',
      'sentiment',
      'intention',
      'dataStatus'
    ]
    if (res.result.list?.length > 0) {
      dynamicColumn.value = Object.keys(res.result.list[0]).filter(key => {
        if (!whiteList.includes(key)) {
          return key
        }
      })
    }
    return res.result
  }
)

/**
 * @description: 品牌车系组成一棵树
 * @param {*} computed
 * @return {*}
 */
const handleBrandCarSeriesTreeOptions = () => {
  const tree: any = []
  otherConditions.value?.brandCarModelList?.forEach((item: any) => {
    tree.push({
      value: item.brandName,
      label: item.brandName,
      children: item.carName.map((car: any) => ({
        value: car,
        label: car
      }))
    })
  })
  return tree
}

const times = ref<any[]>([])

const init = () => {
  brandCarSeriesTreeOptions.value = handleBrandCarSeriesTreeOptions()

  if (otherConditions.value?.defaultStartTime && otherConditions.value?.defaultEndTime) {
    times.value = [otherConditions.value?.defaultStartTime, otherConditions.value?.defaultEndTime]
  }
}
init()

onMounted(() => {
  query()
})

const getBrandStrArr = computed(() => {
  if (!brandCarSeries.value?.length) return undefined
  return [...new Set(brandCarSeries.value?.map(el => el?.[0]))]
})
const getCarSeriesArr = computed(() => {
  if (!brandCarSeries.value?.length) return undefined
  return brandCarSeries.value?.map(el => el?.[1])
})

const query = () => {
  table.filter.batchId = curDataSourceDetail.value?.batchId
  table.filter.dataName = curDataSourceDetail.value?.dataName
  table.filter.dataSourceId = curDataSource.value.id

  table.filter.brandCode = getBrandStrArr.value
  table.filter.carSeries = getCarSeriesArr.value

  table.filter.clientId = userStore.clientId
  const [startTime, endTime] = times.value
  table.filter.startTime = startTime
  table.filter.endTime = endTime
  getFirstPageTableData()
}
const reset = () => {
  if (otherConditions.value?.defaultStartTime && otherConditions.value?.defaultEndTime) {
    times.value = [otherConditions.value?.defaultStartTime, otherConditions.value?.defaultEndTime]
  }
  handleReset(() => {
    table.filter.batchId = curDataSourceDetail.value?.batchId
    table.filter.dataName = curDataSourceDetail.value?.dataName
    table.filter.dataSourceId = curDataSource.value.id
    table.filter.clientId = userStore.clientId
    // times.value = []
    // table.filter.startTime = undefined
    // table.filter.endTime = undefined
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime

    brandCarSeries.value = []
    table.filter.brandCode = undefined
    table.filter.carSeries = undefined
  })
}

const { visible, showVisible, hideVisbble } = useModal()
const channelOptions = ref<any[]>([])
const getChannelOptions = (options: any) => {
  channelOptions.value = options
}

// const { exportFile } = useExport()
// const handleExport = debounce(() => {
//   exportFile(exportSIRawDataResult, table.filter)
// }, 300)

const handleExport = () => {
  showVisible()
}

const handleExportModalOk = async (params: any) => {
  const [errs, data] = await to(
    exportSIRawDataResult({
      ...params,
      clientId: userStore.clientId,
      batchId: curDataSourceDetail.value?.batchId
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
    return
  }
  if (data) {
    hideVisbble()
    eventBus.emit('updateFile')
  }
}

const correctSoundVisible = ref(false)
const curRecord = ref<any>(null)
const handleCorrectSound = (record: any) => {
  curRecord.value = record
  correctSoundVisible.value = true
}
</script>

<template>
  <div class="flex">
    <el-form layout="inline" :model="table.filter">
      <el-row class="w-full" :gutter="24">
        <el-col :span="8">
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="times"
              type="daterange"
              :data-testid="`dataSource-result-10001`"
              :clearable="false"
              :disabled-date="rangeDisabled"
              @change="rangeSelectedTime"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="品牌车系">
            <FCascader
              key="brandCarSeries"
              v-model="brandCarSeries"
              :options="brandCarSeriesTreeOptions"
              :props="{ value: 'value', label: 'label', children: 'children' }"
              clearable
              :pathMode="true"
              :subLength="4"
              :max-collapse-tags="1"
              placeholder="全部"
              multiple
              :data-testid="`dataSource-result-10004`"
            ></FCascader>
            <!--品牌-->
            <!-- <el-select
              v-model="table.filter.brandCode"
              :data-testid="`dataSource-result-10002`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in otherConditions?.brandCarModelList"
                :key="index"
                :data-testid="`dataSource-result-10002-op-${index}`"
                :label="item.brandName"
                :value="item.brandName"
              />
            </el-select> -->
            <!--车系-->
            <!-- <el-select
              v-model="table.filter.carSeries"
              :data-testid="`dataSource-result-10003`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in carSeriesOptions"
                :key="index"
                :data-testid="`dataSource-result-10003-op-${index}`"
                :label="item"
                :value="item"
              />
            </el-select> -->
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="数据渠道">
            <ChannelCascader
              ref="channelRef"
              v-model="table.filter.channelIdList"
              controller="insDataSource"
              multiple
              :fieldNames="{ value: 'code', label: 'name', children: 'child' }"
              :data-testid="`dataSource-result-channel-20001`"
              :format-label="(options: any) => {
                  return formatLabelHandle(table.filter.channelIdList, options, 'name', '/')
              }"
              @getChannelOptions="getChannelOptions"
            ></ChannelCascader>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="标签分类">
            <FCascader
              v-model="table.filter.businessEndTag"
              :options="otherConditions?.tagLibCategoryVos"
              :props="{ value: 'tagName', label: 'tagName', children: 'child' }"
              clearable
              :subLength="4"
              :max-collapse-tags="1"
              placeholder="全部"
              multiple
              :data-testid="`dataSource-result-10004`"
            ></FCascader>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="数据状态">
            <el-select
              v-model="table.filter.dataStatus"
              :data-testid="`dataSource-result-10006`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.resultDataStatus"
                :key="index"
                :data-testid="`dataSource-result-10006-op-${index}`"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="关键词搜索">
            <el-input
              v-model.trim="table.filter.keywords"
              placeholder="请输入"
              :data-testid="`dataSource-result-10009`"
              :maxlength="50"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="情感意图">
            <div class="flex w-full">
              <!--情感-->
              <el-select
                v-model="table.filter.sentiment"
                :data-testid="`dataSource-result-10007`"
                placeholder="情感"
                multiple
                :max-collapse-tags="1"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.emotion"
                  :key="index"
                  :data-testid="`dataSource-result-10007-op-${index}`"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
              <!--意图-->
              <el-select
                v-model="table.filter.intention"
                placeholder="意图"
                :data-testid="`dataSource-result-10008`"
                multiple
                :max-collapse-tags="1"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.intention"
                  :key="index"
                  :data-testid="`dataSource-result-10008-op-${index}`"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <div class="w-full flex justify-end">
            <el-button
              color="#F2F3F5"
              class="mr-8"
              :data-testid="`dataSource-result-10010`"
              @click="reset"
            >
              重置
            </el-button>
            <el-button type="primary" :data-testid="`dataSource-result-10011`" @click="query"
              >查询
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-form>
  </div>

  <el-divider />

  <div class="flex justify-between mb-12">
    <h3 class="table-left-title flex item-center">数据列表</h3>
    <el-button
      :disabled="!table.list?.length"
      :data-testid="`dataSource-result-10012`"
      type="primary"
      class="ml-8"
      @click="handleExport"
    >
      <template #icon>
        <i class="iconfont icon-Export"></i>
      </template>
      导出数据
    </el-button>
  </div>
  <div class="table" :style="computedCardHeight(330)">
    <el-table
      :data-testid="`dataSource-result-table`"
      v-loading="table.loading"
      :data="table.list"
      style="width: 100%; height: 100%"
      :height="computedCardHeight(330)"
      @sort-change="handleSortChange"
    >
      <!-- <el-table-column data-index="channelName" title="渠道" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-result-t0-${rowIndex}`">{{ record.channelName }}</span>
          </template>
        </el-table-column> -->
      <!--<el-table-column data-index="content" title="声音ID"/>-->
      <el-table-column prop="brandName" label="品牌" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t1-${$index}`">{{ row.brandName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="carSeriesName" label="车系" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t2-${$index}`">{{ row.carSeriesName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalTextScene" label="声音片段" show-overflow-tooltip width="300">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t3-${$index}`">{{ row.originalTextScene }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="labelTypeName" label="标签类型" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t41-${$index}`">{{ row.labelTypeName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="labelTypeLevelFirst" label="一级标签" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t51-${$index}`">{{
            row.labelTypeLevelFirst
          }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="labelTypeLevelSecond" label="二级标签" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t61-${$index}`">{{
            row.labelTypeLevelSecond
          }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="labelTypeLevelThree" label="三级标签" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t71-${$index}`">{{
            row.labelTypeLevelThree
          }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="labelTypeLevelFour" label="四级标签" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t72-${$index}`">{{ row.labelTypeLevelFour }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column data-index="labelTypeLevelFive" title=" 五级标签" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-result-t73-${rowIndex}`">{{
              record.labelTypeLevelFive
            }}</span>
          </template>
        </el-table-column> -->
      <el-table-column prop="opinion" label="观点标签" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t74-${$index}`">{{ row.opinion }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sentiment" label="情感" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t8-${$index}`">{{ row.sentiment }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="intention" label="意图" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t9-${$index}`">{{ row.intention }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column data-index="channelName" title="渠道" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-result-t10-${rowIndex}`">{{ record.channelName }}</span>
          </template>
        </el-table-column>
        <el-table-column data-index="originalId" title="原文ID" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-result-t11-${rowIndex}`">{{ record.originalId }}</span>
          </template>
        </el-table-column>
        <el-table-column data-index="publishTime" title="发布时间" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-result-t12-${rowIndex}`">{{ record.publishTime }}</span>
          </template>
        </el-table-column> -->

      <template v-if="dynamicColumn.length > 0">
        <template v-for="(key, index) in dynamicColumn" :key="index">
          <el-table-column :prop="`${key}`" :label="`${key}`" show-overflow-tooltip width="180">
            <template #default="{ row, $index }">
              <span :data-testid="`dataSource-dynamicColumn-t100${index}-${$index}`">{{
                row[key]
              }}</span>
            </template>
          </el-table-column>
        </template>
      </template>

      <el-table-column prop="dataStatus" label="数据状态" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t13-${$index}`">{{ row.dataStatus }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row, $index }">
          <el-link
            :data-testid="`dataSource-result-t15-${$index}`"
            :underline="false"
            type="primary"
            @click="handleCorrectSound(row)"
            >我要纠错
          </el-link>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-if="table.total > 0"
      v-model:current-page="table.pageNum"
      v-model:page-size="table.pageSize"
      :page-sizes="[10, 15, 20, 25]"
      :total="table.total"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </div>

  <ExportModal
    v-modelv-model="visible"
    :otherConditions="otherConditions"
    :channelOptions="channelOptions"
  />

  <CorrectSound v-model="correctSoundVisible" :record="curRecord" />
</template>

<style scoped lang="scss"></style>
