<script setup lang="ts">
import dayjs from 'dayjs'
import { inject } from 'vue'
import { useTable } from '@/hooks/table'
// import ChannelCascader from '@/components/ChannelCascader.vue'
import { exportProjectResultData } from '@/api/project'
import type { CommonObj, ConditionsDetailItem } from '@/types'
import type { ProjectDetail } from '@/types/project.d.ts'
import { computedCardHeight } from '@/utils'
import useUserStore from '@/stores/modules/user'
import { useModal } from '@/hooks/useModal'
import { ElMessage } from 'element-plus'
import to from 'await-to-js'
import eventBus from '@/utils/eventBus'
import { useTagVIewData } from '@/hooks/useTagVIewData'
// import CorrectSound from '@/components/CorrectSound/index.vue'

const props = withDefaults(
  defineProps<{
    otherConditions: CommonObj
    curDataSourceDetail: ProjectDetail
    projectInfoDetail: any
    brand: string
    channelOptions: Record<any, any>[] | undefined
    carSeriesOptions: Record<any, any>[] | undefined
    competitiveCarSeriesOptions: Record<any, any>[] | undefined
    mentionCarSeriesOptions: Record<any, any>[] | undefined
    integrationListOptions: Record<any, any>[] | undefined
  }>(),
  {}
)
const {
  curDataSourceDetail,
  otherConditions,
  brand,
  channelOptions,
  carSeriesOptions,
  competitiveCarSeriesOptions,
  mentionCarSeriesOptions
} = toRefs(props)

const userStore = useUserStore()
const { rangeDisabled, rangeSelectedTime } = useTagVIewData()

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const dynamicColumn = ref<any[]>([])

const carSeries = ref<any[]>([])

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
    url: '/insights/insProjectInfo/findResultData'
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
      'dataStatus',
      'ownCarSeriesList',
      'competitorsCarSeriesList',
      'mentionCarSeries'
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

const times = ref<any[]>([])

onMounted(() => {
  if (otherConditions.value?.defaultStartTime && otherConditions.value?.defaultEndTime) {
    times.value = [otherConditions.value?.defaultStartTime, otherConditions.value?.defaultEndTime]
  }
  query()
})

const query = () => {
  // table.filter.brand = brandOptions[0]?.brandCode
  table.filter.brand = brand.value
  table.filter.projectId = curDataSourceDetail.value.id
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
    // table.filter.brand = brandOptions[0]?.brandCode
    table.filter.projectId = curDataSourceDetail.value.id
    table.filter.clientId = userStore.clientId
    // times.value = []
    // table.filter.startTime = undefined
    // table.filter.endTime = undefined
    const [startTime, endTime] = times.value
    table.filter.startTime = startTime
    table.filter.endTime = endTime
  })
}

const { visible, showVisible, hideVisbble } = useModal()
// const { exportFile } = useExport()
// const handleExport = debounce(() => {
//   exportFile(exportProjectResultData, table.filter)
// }, 300)
const handleExport = () => {
  showVisible()
  // exportFile(exportProjectResultData, table.filter)
}

const handleExportModalOk = async (params: any) => {
  const [errs, data] = await to(
    exportProjectResultData({
      ...params,
      clientId: userStore.clientId,
      projectId: curDataSourceDetail.value.id,
      brand: brand.value
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
defineExpose({ query })
</script>

<template>
  <div class="flex">
    <el-form layout="inline" :model="table.filter">
      <el-row class="w-full" :gutter="24">
        <el-col :span="16">
          <el-form-item label="品牌车系" class="w-full">
            <!-- <FCascader
              v-model="carSeries"
              :options="integrationListOptions"
              multiple
              clearable
              pathMode
              :subLength="7"
              :props="{
                value: 'value',
                label: 'name',
                children: 'child'
              }"
              placeholder="全部"
            ></FCascader> -->
            <div class="w-full flex">
              <FSelect
                v-model="table.filter.ownCarSeries"
                multiple
                clearable
                :subLength="7"
                :options="carSeriesOptions"
                :fieldNames="{ value: 'value', label: 'name' }"
                placeholder="全部车系"
              ></FSelect>

              <FCascader
                v-model="table.filter.competitorsCarSeries"
                :options="competitiveCarSeriesOptions"
                multiple
                clearable
                :subLength="7"
                :fieldNames="{
                  value: 'value',
                  label: 'name',
                  children: 'child'
                }"
                placeholder="全部竞品"
              ></FCascader>
              <FCascader
                v-model="table.filter.mentionCarSeriesList"
                :options="mentionCarSeriesOptions"
                multiple
                clearable
                :subLength="7"
                :fieldNames="{ value: 'value', label: 'name', children: 'child' }"
                placeholder="全部同时"
              ></FCascader>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="times"
              type="daterange"
              :data-testid="`detail-result-10001`"
              :clearable="false"
              :disabled-date="rangeDisabled"
              @change="rangeSelectedTime"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="数据渠道">
            <FCascader
              v-model="table.filter.channelIdList"
              :options="channelOptions"
              width="100%"
              multiple
              clearable
              :subLength="10"
              :placeholder="'全部'"
              :fieldNames="{ value: 'code', label: 'name', children: 'child' }"
              :data-testid="`detail-result-channel-20001`"
            ></FCascader>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="标签分类">
            <FCascader
              v-model="table.filter.businessEndTag"
              :options="otherConditions?.tagLibCategoryVos"
              :fieldNames="{ value: 'id', label: 'tagName', children: 'child' }"
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
          <el-form-item label="情感意图">
            <div class="w-full flex">
              <!--情感-->
              <el-select
                v-model="table.filter.sentiment"
                :data-testid="`detail-result-10007`"
                placeholder="情感"
                multiple
                :max-collapse-tags="1"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.emotion"
                  :key="index"
                  :data-testid="`detail-result-10007-op-${index}`"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
              <!--意图-->
              <el-select
                v-model="table.filter.intention"
                placeholder="意图"
                :data-testid="`detail-result-10008`"
                multiple
                :max-collapse-tags="1"
                clearable
              >
                <el-option
                  v-for="(item, index) in conditions.intention"
                  :key="index"
                  :data-testid="`detail-result-10008-op-${index}`"
                  :label="item.value"
                  :value="item.value"
                />
              </el-select>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="关键词搜索">
            <el-input
              v-model.trim="table.filter.keywords"
              placeholder="请输入"
              :data-testid="`detail-result-10009`"
              :maxlength="50"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="8" :offset="8">
          <div class="w-full flex justify-end">
            <el-button
              color="#F2F3F5"
              class="mr-8"
              :data-testid="`detail-result-10010`"
              @click="reset"
            >
              重置
            </el-button>
            <el-button type="primary" :data-testid="`detail-result-10011`" @click="query"
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
      :data-testid="`detail-result-10012`"
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
  <div class="table" :style="computedCardHeight(420)">
    <el-table
      :data-testid="`detail-result-table`"
      v-loading="table.loading"
      :data="table.list"
      style="width: 100%; height: 100%"
      :height="'100%'"
      :max-height="'100%'"
      @sort-change="handleSortChange"
    >
      <!--<el-table-column data-index="content" title="声音ID"/>-->
      <!-- <el-table-column data-index="channelName" title="渠道" :size="180">
        <template #cell="{ record, rowIndex }">
          <span :data-testid="`dataSource-result-t0-${rowIndex}`">{{ record.channelName }}</span>
        </template>
      </el-table-column> -->
      <el-table-column prop="brandName" label="品牌" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-result-t1-${$index}`">{{ row.brandName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="carSeriesName" label="车系" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-result-t2-${$index}`">{{ row.carSeriesName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ownCarSeriesList" label="本品车系" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-original-t22-${$index}`">{{ row.ownCarSeriesList }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitorsCarSeriesList" label="竞品车系" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-original-t23-${$index}`">{{
            row.competitorsCarSeriesList
          }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="mentionCarSeries" label="同时提及车系" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-original-t24-${$index}`">{{ row.mentionCarSeries }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalTextScene" label="声音片段" width="300" show-overflow-tooltip>
        <template #default="{ row, $index }">
          <span :data-testid="`detail-result-t3-${$index}`">{{ row.originalTextScene }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="labelTypeName" label="标签类型" width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-result-t4-${$index}`">{{ row.labelTypeName }}</span>
        </template>
      </el-table-column>

      <!-- <el-table-column
          v-if="curBrandTags.includes('BIZ')"
          data-index="businessEndLevelLabel"
          title="业务标签"
          :size="180"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t4-${rowIndex}`">{{
              record.businessEndLevelLabel
            }}</span>
          </template>
        </el-table-column> -->
      <!-- v-if="curDataSourceDetail.tags.includes('BIZ')" -->
      <!-- <el-table-column
          v-if="curBrandTags.includes('BIZ')"
          data-index="businessCategory"
          title="业务分类"
          :size="180"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t5-${rowIndex}`">{{ record.businessCategory }}</span>
          </template>
        </el-table-column> -->
      <!-- v-if="curDataSourceDetail.tags.includes('QY')" -->
      <!-- <el-table-column
          v-if="curBrandTags.includes('QY')"
          data-index="qualityEndLevelLabel"
          title="质量标签"
          :size="180"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t6-${rowIndex}`">{{
              record.qualityEndLevelLabel
            }}</span>
          </template>
        </el-table-column> -->
      <!-- v-if="curDataSourceDetail.tags.includes('QY')" -->
      <!-- <el-table-column
          v-if="curBrandTags.includes('QY')"
          data-index="qualityCategory"
          title="质量分类"
          :size="180"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t7-${rowIndex}`">{{ record.qualityCategory }}</span>
          </template>
        </el-table-column> -->

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

      <!-- <el-table-column data-index="sentiment" title="观点热词" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t8-${rowIndex}`">{{ record.opinionKeywords }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="sentiment" title="情感" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t9-${rowIndex}`">{{ record.sentiment }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="intention" title="意图" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t10-${rowIndex}`">{{ record.intention }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="channelName" title="数据渠道" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t11-${rowIndex}`">{{ record.channelName }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="originalId" title="原文ID" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t12-${rowIndex}`">{{ record.originalId }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="publishTime" title="发布时间" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-result-t13-${rowIndex}`">{{ record.publishTime }}</span>
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
          <span :data-testid="`detail-result-t14-${$index}`">{{ row.dataStatus }}</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="{ row, $index }">
          <el-link
            :data-testid="`detail-result-t15-${$index}`"
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
    v-model="visible"
    :otherConditions="otherConditions"
    :channelOptions="channelOptions"
  />

  <CorrectSound v-model="correctSoundVisible" :record="curRecord" />
</template>

<style scoped lang="scss"></style>
