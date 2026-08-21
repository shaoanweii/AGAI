<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { inject } from 'vue'
// import ChannelCascader from '@/components/ChannelCascader.vue'
import { exportProjectRawDataResult } from '@/api/project'
import type { CommonObj, ConditionsDetailItem } from '@/types'
import type { ProjectDetail } from '@/types/project.d.ts'
import { computedCardHeight } from '@/utils'
import useUserStore from '@/stores/modules/user'
import { useModal } from '@/hooks/useModal'
import { ElMessage } from 'element-plus'
import to from 'await-to-js'
import eventBus from '@/utils/eventBus'
import { useTagVIewData } from '@/hooks/useTagVIewData'

const props = withDefaults(
  defineProps<{
    otherConditions: CommonObj
    curDataSourceDetail: ProjectDetail
    projectInfoDetail: any
    brand: string
    defaultBrand: string
    channelOptions: Record<any, any>[] | undefined
    carSeriesOptions: Record<any, any>[] | undefined
    competitiveCarSeriesOptions: Record<any, any>[] | undefined
    mentionCarSeriesOptions: Record<any, any>[] | undefined
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
    url: '/insights/insProjectInfo/findRawData'
  },
  res => {
    // 排除已知字段， 动态展示未知字段
    const whiteList = [
      'channelName',
      'ownCarSeriesList',
      'competitorsCarSeriesList',
      'mentionCarSeries',
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
const times = ref<any[]>([])

onMounted(() => {
  if (otherConditions.value?.defaultStartTime && otherConditions.value?.defaultEndTime) {
    times.value = [otherConditions.value?.defaultStartTime, otherConditions.value?.defaultEndTime]
  }
  query()
})

const query = () => {
  // table.filter.batchId = curDataSourceDetail.value?.batchId;
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
    // table.filter.batchId = curDataSourceDetail.value?.batchId;
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
const handleExport = () => {
  showVisible()
}
// const handleExport = debounce(() => {
//   exportFile(exportProjectRawDataResult, table.filter)
// }, 300)
const handleExportModalOk = async (params: any) => {
  const [errs, data] = await to(
    exportProjectRawDataResult({
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

defineExpose({ query })
</script>

<template>
  <div class="flex">
    <el-form layout="inline" :model="table.filter">
      <el-row class="w-full" :gutter="24">
        <el-col :span="16">
          <el-form-item label="品牌车系" class="w-full">
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
              :data-testid="`detail-original-10001`"
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
              :data-testid="`detail-original-channel-20001`"
            ></FCascader>
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="关键词搜索">
            <el-input
              v-model.trim="table.filter.keywords"
              :data-testid="`detail-original-10003`"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <div class="w-full flex justify-end">
            <el-button
              color="#F2F3F5"
              :data-testid="`detail-original-10004`"
              class="mr-8"
              @click="reset"
              >重置
            </el-button>
            <el-button type="primary" :data-testid="`detail-original-10005`" @click="query"
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
      :data-testid="`detail-original-10006`"
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
  <div class="table" :style="computedCardHeight(380)">
    <el-table
      :data-testid="`detail-original-table`"
      v-loading="table.loading"
      :data="table.list"
      style="width: 100%; height: 100%"
      :height="'100%'"
      :max-height="'100%'"
      @sort-change="handleSortChange"
    >
      <!-- <el-table-column data-index="id" title="原文ID" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t1-${rowIndex}`">{{ record.id }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="channelName" title="渠道" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t21-${rowIndex}`">{{ record.channelName }}</span>
          </template>
        </el-table-column> -->
      <el-table-column prop="ownCarSeriesList" label="本品车系" min-width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-original-t22-${$index}`">{{ row.ownCarSeriesList }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="competitorsCarSeriesList" label="竞品车系" min-width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-original-t23-${$index}`">{{
            row.competitorsCarSeriesList
          }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="mentionCarSeries" label="同时提及车系" min-width="180">
        <template #default="{ row, $index }">
          <span :data-testid="`detail-original-t24-${$index}`">{{ row.mentionCarSeries }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column
          data-index="title"
          title="标题"
          :size="240"
          :ellipsis="true"
          :tooltip="true"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t3-${rowIndex}`">{{ record.title }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column
          data-index="content"
          title="内容"
          :size="300"
          :ellipsis="true"
          :tooltip="true"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t4-${rowIndex}`">{{ record.content }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="publishTime" title="发布时间" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t5-${rowIndex}`">{{ record.publishTime }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="userName" title="昵称" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t6-${rowIndex}`">{{ record.userName }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="readingCount" title="阅读数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t7-${rowIndex}`">{{ record.readingCount }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="focusCount" title="关注数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t8-${rowIndex}`">{{ record.focusCount }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="commentsCount" title="评论数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t9-${rowIndex}`">{{ record.commentsCount }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="favorCount" title="点赞数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t10-${rowIndex}`">{{ record.favorCount }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="collectionsCount" title="收藏数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t11-${rowIndex}`">{{
              record.collectionsCount
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="redirectionCount" title="转发数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t12-${rowIndex}`">{{
              record.redirectionCount
            }}</span>
          </template>
        </el-table-column> -->

      <!-- 客户宽表字段 -->
      <template v-if="dynamicColumn.length > 0">
        <template v-for="(key, index) in dynamicColumn" :key="index">
          <el-table-column :prop="`${key}`" :label="`${key}`" show-overflow-tooltip width="180">
            <template #default="{ row, $index }">
              <span :data-testid="`dataSource-origin-dynamicColumn-t100${index}-${$index}`">{{
                row[key]
              }}</span>
            </template>
          </el-table-column>
        </template>
      </template>

      <!-- <el-table-column data-index="dataStatus" title="数据状态" :size="120">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`detail-original-t13-${rowIndex}`">{{ record.dataStatus }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column
          data-index="url"
          title="原文链接"
          :ellipsis="true"
          :tooltip="true"
          :size="280"
        >
          <template #cell="{ record, rowIndex }">
            <el-link
              style="display: inline"
              :data-testid="`detail-original-t14-${rowIndex}`"
              :href="record.url"
              target="_blank"
              >{{ record.url }}</el-link
            >
          </template>
        </el-table-column> -->
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
</template>

<style scoped lang="scss"></style>
