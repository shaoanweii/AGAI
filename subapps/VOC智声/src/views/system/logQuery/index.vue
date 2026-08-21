<script setup lang="ts">
import { onMounted, ref, computed, shallowRef, h } from 'vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import { useTable } from '@/hooks/useTable'
import { findDepartTree } from '@/api/common'
import { getLogQueryPage } from '@/api/system/logQuery'
import { useUserStore } from '@/store'

defineOptions({
  name: 'LogQuery'
})

/**
 * 时间范围
 * - 默认：本月1号 -> 今天
 * - 可选范围：最近一年 ~ 未来90天（含首尾）
 * - 最大可选区间：90天（含首尾）
 */
const timeRange = ref<[string, string]>([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

// 选中第一个日期后，用于动态限制第二个日期的可选范围（<= 90天，支持前后选择）
const selectingStartDate = ref<string | null>(null)

const disabledDate = (date: Date) => {
  const cur = dayjs(date)
  const today = dayjs()
  const minDate = today.subtract(1, 'year')
  // 以“今天”为基准，最多允许选择到未来第 90 天（含首尾 => +89）
  const maxDate = today.add(89, 'day')

  // 禁止选择一年以前的日期；禁止选择未来90天之后的日期
  if (cur.isBefore(minDate, 'day')) return true
  if (cur.isAfter(maxDate, 'day')) return true

  // 选择第一个日期后，限制第二个日期范围：前后 90 天内（含首尾）
  if (!selectingStartDate.value) return false
  const start = dayjs(selectingStartDate.value)
  const diffDays = Math.abs(start.diff(cur, 'day'))
  return diffDays > 89
}

const handleCalendarChange = (val: Array<Date | null>) => {
  const [start, end] = val || []
  if (start && !end) {
    selectingStartDate.value = dayjs(start).format('YYYY-MM-DD')
    return
  }
  // 结束选择 or 清空时复位
  selectingStartDate.value = null
}

const normalizeTimeRange = (val: [string, string]) => {
  const today = dayjs()
  const minDate = today.subtract(1, 'year')
  // 以“今天”为基准，最多允许选择到未来第 90 天（含首尾 => +89）
  const maxDate = today.add(89, 'day')

  let [start, end] = val
  let startDay = dayjs(start)
  let endDay = dayjs(end)

  // 兜底：手动输入/异常值时，统一裁剪到 [minDate, maxDate]
  if (startDay.isBefore(minDate, 'day')) startDay = minDate
  if (startDay.isAfter(maxDate, 'day')) startDay = maxDate
  if (endDay.isAfter(maxDate, 'day')) endDay = maxDate
  if (endDay.isBefore(minDate, 'day')) endDay = minDate

  // 若存在反向输入（start > end），统一交换，确保 start <= end
  if (startDay.isAfter(endDay, 'day')) {
    const tmp = startDay
    startDay = endDay
    endDay = tmp
  }

  const diffDays = endDay.diff(startDay, 'day') + 1
  if (diffDays <= 90) {
    return [startDay.format('YYYY-MM-DD'), endDay.format('YYYY-MM-DD')] as [string, string]
  }

  // 超出 90 天时：默认以开始日期为基准，自动收敛到 90 天内
  let fixedEnd = startDay.add(89, 'day')
  if (fixedEnd.isAfter(maxDate, 'day')) fixedEnd = maxDate
  ElMessage.warning('时间范围最大支持选择90天，已自动调整时间范围')
  return [startDay.format('YYYY-MM-DD'), fixedEnd.format('YYYY-MM-DD')] as [string, string]
}

/**
 * 部门下拉（二/三级部门树）
 */
const departs = ref<any[]>([])
const getDepartList = async () => {
  try {
    const response = await findDepartTree()
    if (response.success) {
      departs.value = response.result || []
    } else {
      departs.value = []
    }
  } catch (e) {
    departs.value = []
    console.error('获取部门树失败:', e)
  }
}

const userStore = useUserStore()
const visitAppOptions = computed(() => {
  return userStore.getDictItems('voc_report_log_access_app')
})

// 访问菜单（单选）
const visitMenuOptions = computed(() => {
  return userStore.getDictItems('voc_report_log_menu')
})

const customPrefix = shallowRef({
  render() {
    return h('div', { style: { display: 'flex', alignItems: 'center', gap: '4px' } }, [
        h(SvgIcon, { name: 'calendar', width: '20px', height: '20px', color: '#999999' }),
        h('span', { style: { color: '#333', fontWeight: 400 } }, '自定义:')
      ])
  }
})

/**
 * 列表查询
 */
const {
  tableState: { loading, dataList, currentPage, pageSize, total },
  tableMethods,
  formData
} = useTable({
  immediate: false,
  initialFormData: {
    accessApp: '',
    // 访问菜单改为单选（提交时直接传字符串）
    accessMenu: '' as string,
    deptId: [] as string[],
    searchKey: ''
  },
  fetchDataApi: async () => {
    const [startDate, endDate] = timeRange.value
    const deptId = ((formData.value as any).deptId || []) as Array<string | number>
    const accessMenu = (formData.value as any).accessMenu as string | number | undefined | null
    const params = {
      ...formData.value,
      startTime: startDate,
      endTime: endDate,
      // 接口声明 deptId 为 string 数组，这里统一转成字符串
      deptId: deptId.map(v => String(v)),
      // accessMenu 单选：直接传值（空值时传空字符串）
      accessMenu: accessMenu || '',
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    const response = await getLogQueryPage(params as any)
    if (!response.success) {
      ElMessage.error(response.message || '获取日志列表失败')
      return { list: [], total: 0 }
    }
    const result: any = response.result
    return { list: result?.list || [], total: Number(result?.total || 0) }
  }
})

const handleSearch = async () => {
  currentPage.value = 1
  await tableMethods.handleQuery()
}

const handleTimeRangeChange = async (val: [string, string]) => {
  if (!val || val.length !== 2) return
  timeRange.value = normalizeTimeRange(val)
  await handleSearch()
}

const calcIndex = (index: number) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

onMounted(async () => {
  await getDepartList()
  // 首次进入：按默认时间范围加载
  await tableMethods.getList()
})
</script>

<template>
  <div class="log-query">
    <el-card class="table-card" shadow="never">
      <div class="flex-between items-center mb-24">
        <div class="text-h3" style="font-weight: 600">操作日志</div>
        <div class="flex gap-16">
          <div class="log-custom-date-picker">
          <el-date-picker
            v-model="timeRange"
            type="daterange"
            :prefix-icon="customPrefix"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY.MM.DD"
            value-format="YYYY-MM-DD"
            :clearable="false"
            :disabled-date="disabledDate"
            @calendar-change="handleCalendarChange"
            @change="handleTimeRangeChange"
            style="width: 300px"
          />
          </div>


          <el-select
            v-model="formData.accessApp"
            placeholder="应用"
            clearable
            style="width: 100px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in visitAppOptions"
              :key="item.value"
              :label="`${item.text}端`"
              :value="item.value"
            />
          </el-select>

          <el-cascader
            v-model="formData.deptId"
            :options="departs"
            filterable
            placeholder="部门"
            clearable
            :props="{
              value: 'code',
              label: 'name',
              children: 'child',
              multiple: true,
              emitPath: false,
              checkStrictly: true
            }"
            :max-collapse-tags="1"
            collapse-tags
            collapse-tags-tooltip
            style="width: 450px"
            @blur="handleSearch"
          />

          <el-select
            v-model="formData.accessMenu"
            placeholder="菜单"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in visitMenuOptions"
              :key="item.value"
              :label="item.text"
              :value="item.value"
            />
          </el-select>

          <el-input
            v-model="formData.searchKey"
            placeholder="请输入员工姓名或工号搜索"
            clearable
            maxlength="20"
            style="width: 228px"
            :suffix-icon="Search"
            @change="handleSearch"
          />
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="dataList as any"
        max-height="calc(100vh - 84px - 48px - 106px - 32px - 10px)"
        class="flex-auto overflow-auto"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="employeeName" label="员工姓名" min-width="180" />
        <el-table-column prop="employeeNo" label="员工工号" min-width="180" />
        <el-table-column prop="secondLevelDept" label="二级部门" min-width="180">
          <template #default="{ row }">
            {{ row.secondLevelDept || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="thirdLevelDept" label="三级部门" min-width="180">
          <template #default="{ row }">
            {{ row.thirdLevelDept || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="accessApp" label="访问应用" min-width="100">
          <template #default="{ row }">
            {{ row.accessApp ? `${row.accessApp}端` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="accessMenu" label="访问菜单" min-width="160" />
        <el-table-column prop="startTime" label="访问时间" width="180" />
        <!-- <el-table-column prop="endTime" label="结束访问时间" width="180" /> -->
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="->,total, prev, pager, next, sizes"
        />
      </div>
    </el-card>
  </div>
</template>
<style lang="scss">
.log-custom-date-picker {
  width: 300px;
  .el-range__icon {
    width: 86px !important;
    font-style: normal !important;
  }
}
</style>

<style lang="scss" scoped>
.log-query {
  height: 100%;
  display: flex;
  flex-direction: column;

  .table-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-table .el-table__cell) {
      height: 55px;
      padding: 0 !important;
    }

    :deep(.el-table--fit .el-table__inner-wrapper:before) {
      width: 0 !important;
    }

    :deep(.el-table__header) {
      .el-table__cell {
        color: #1d2129;
        font-weight: 600;
      }
    }

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    :deep(.el-table__body-wrapper) {
      .el-table__cell {
        color: #1d2129;
        font-weight: 400;
      }
    }

    .pagination-wrapper {
      margin-top: 16px;
    }
  }
}

@media (max-width: 768px) {
  .log-query {
    .table-card {
      .pagination-wrapper {
        text-align: center;
      }
    }
  }
}
</style>
