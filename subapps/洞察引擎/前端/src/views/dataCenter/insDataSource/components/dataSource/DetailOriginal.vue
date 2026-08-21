<script setup lang="ts">
import { useTable } from '@/hooks/table'
import { inject } from 'vue'
import ChannelCascader from '@/components/ChannelCascader.vue'
import { exportRawData } from '@/api/dataCenter'
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
    url: '/insights/insDataSource/getRawData'
  },
  res => {
    // 排除已知字段， 动态展示未知字段
    const whiteList = [
      'channelName',
      'dataStatus'
      // 'originalId',
      // 'originalTextScene',
      // 'businessEndLevelLabel',
      // 'businessCategory',
      // 'sentiment',
      // 'intention',
      // 'channelName',
      // 'publishTime',
      // 'dataStatus',
      // 'metaDataType',
      // 'brandName',
      // 'carSeriesName',
      // 'city',
      // 'opinionKeywords'
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
  table.filter.batchId = curDataSourceDetail.value?.batchId
  table.filter.dataSourceId = curDataSource.value.id
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
    table.filter.dataSourceId = curDataSource.value.id
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
const channelOptions = ref<any[]>([])
const getChannelOptions = (options: any) => {
  channelOptions.value = options
}

// const { exportFile, exporting } = useExport()
// const handleExport = debounce(() => {
//   exportFile(exportRawData, table.filter)
// }, 300)

const handleExport = () => {
  showVisible()
}
const handleExportModalOk = async (params: any) => {
  const [errs, data] = await to(
    exportRawData({
      ...params,
      clientId: userStore.clientId,
      dataSourceId: curDataSource.value.id,
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
              :data-testid="`dataSource-original-10001`"
              :clearable="false"
              :disabled-date="rangeDisabled"
              @change="rangeSelectedTime"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="数据渠道">
            <!--<el-select v-model="table.filter.formatFilters" placeholder="全部" clearable style="width: 120px">-->
            <!--  <el-option v-for="item in []" :label="item.value" :value="item.key"/>-->
            <!--</el-select>-->
            <ChannelCascader
              ref="channelRef"
              v-model="table.filter.channelIdList"
              controller="insDataSource"
              width="100%"
              multiple
              :fieldNames="{ value: 'code', label: 'name', children: 'child' }"
              :data-testid="`dataSource-original-channel-20001`"
              :format-label="(options: any) => {
                  return formatLabelHandle(table.filter.channelIdList, options, 'name', '/')
              }"
              @getChannelOptions="getChannelOptions"
            ></ChannelCascader>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="数据状态">
            <el-select
              v-model="table.filter.dataStatus"
              :data-testid="`dataSource-original-10002`"
              placeholder="全部"
              multiple
              :max-collapse-tags="1"
              clearable
            >
              <el-option
                v-for="(item, index) in conditions.originalDataStatus"
                :key="index"
                :data-testid="`dataSource-original-10002-op-${index}`"
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
              :data-testid="`dataSource-original-10003`"
              placeholder="请输入"
              :maxlength="50"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="8" :offset="8">
          <div class="w-full flex justify-end">
            <el-button
              color="#F2F3F5"
              :data-testid="`dataSource-original-10004`"
              class="mr-8"
              @click="reset"
              >重置
            </el-button>
            <el-button type="primary" :data-testid="`dataSource-original-10005`" @click="query"
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
      :data-testid="`dataSource-original-10006`"
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
  <div class="table" :style="computedCardHeight(280)">
    <el-table
      :data-testid="`dataSource-original-table`"
      v-loading="table.loading"
      :data="table.list"
      style="width: 100%; height: 100%"
      :height="computedCardHeight(280)"
      @sort-change="handleSortChange"
    >
      <!-- <el-table-column data-index="id" title="原文ID" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t1-${rowIndex}`">{{ record.id }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="channelName" title="渠道" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t2-${rowIndex}`">{{
              record.channelName
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="title" title="标题" :size="240">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t3-${rowIndex}`">{{ record.title }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column
          data-index="content"
          title="内容"
          ellipsis
          :tooltip="{
            contentClass: 'tooltipClass'
          }"
          :size="300"
        >
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t4-${rowIndex}`">{{ record.content }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="publishTime" title="发布时间" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t5-${rowIndex}`">{{
              record.publishTime
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="userName" title="昵称" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t6-${rowIndex}`">{{ record.userName }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="readingCount" title="阅读数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t7-${rowIndex}`">{{
              record.readingCount
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="focusCount" title="关注数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t8-${rowIndex}`">{{ record.focusCount }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="commentsCount" title="评论数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t9-${rowIndex}`">{{
              record.commentsCount
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="favorCount" title="点赞数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t10-${rowIndex}`">{{
              record.favorCount
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="collectionsCount" title="收藏数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t11-${rowIndex}`">{{
              record.collectionsCount
            }}</span>
          </template>
        </el-table-column> -->
      <!-- <el-table-column data-index="redirectionCount" title="转发数" :size="180">
          <template #cell="{ record, rowIndex }">
            <span :data-testid="`dataSource-original-t12-${rowIndex}`">{{
              record.redirectionCount
            }}</span>
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
              :data-testid="`dataSource-original-t14-${rowIndex}`"
              :href="record.url"
              target="_blank"
              >{{ record.url }}</el-link
            >
          </template>
        </el-table-column> -->
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
      <el-table-column prop="dataStatus" label="数据状态" width="120">
        <template #default="{ row, $index }">
          <span :data-testid="`dataSource-original-t13-${$index}`">{{ row.dataStatus }}</span>
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
</template>

<style scoped lang="scss"></style>
