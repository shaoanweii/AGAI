<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import ComScene from './components/ComScene/index.vue'
import SpecialAnalysis from './components/SpecialAnalysis/index.vue'
import { getGeneralScenario } from '@/api/overview/index'
import type { HomeMenuVo } from '@/api/overview/type'
import { getSpecialTypeList, getCustomReportList } from '@/api/sceneAnalysis/index'
import type {
  SpecialAnalysisTypeListVo,
  CustomReportListVo,
  CustomReportQueryParams
} from '@/api/sceneAnalysis/types.d'
import { insertReportViewLog } from '@/api/reportViewLog'
import { useQueryStore } from '@/store/modules/query'
import { useAppStore, useUserStore } from '@/store'
import { useRouter } from 'vue-router'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'

defineOptions({
  name: 'SceneAnalysis'
})

const MY_PUBLISH_CATEGORY_ID = 'my_publish'
const ALL_CATEGORY_ID = ''

// 状态管理
const queryStore = useQueryStore()

// 通用场景：展开/收起（默认展开）
const isComSceneExpanded = ref<boolean>(true)
const toggleComScene = () => {
  isComSceneExpanded.value = !isComSceneExpanded.value
}

// 通用场景数据
const generalScenarioData = ref<HomeMenuVo[] | null>(null)
const generalScenarioLoading = ref<boolean>(false)

// 专项分析类型数据
const specialTypeData = ref<SpecialAnalysisTypeListVo[]>([])
const specialTypeLoading = ref<boolean>(false)
const secondTypeOptios = ref<SpecialAnalysisTypeListVo[]>([])
const secondTypeLoading = ref<boolean>(false)
let secondTypeRequestSeq = 0

// 自定义报告数据
const customReportData = ref<CustomReportListVo[]>([])
const customReportLoading = ref<boolean>(false)
const customReportTotal = ref<number>(0)
const reportPageNum = ref<number>(1)
const reportPageSize = ref<number>(10)

const getDefaultSaQueryForm = () => ({
  reportName: undefined,
  sortField: '',
  sortOrder: 'desc'
})

// 专项分析查询条件（用于翻页时保持筛选条件不丢失）
const saQueryForm = ref(getDefaultSaQueryForm())

const sceneAnalysisStore = useSceneAnalysisStore()
const userStore = useUserStore()
const appStore = useAppStore()
const router = useRouter()

// 一级分类id
const firstLevelZoneId = ref<string>()
const specialTypeId = ref<string>()

// “我发布的”统计（已发布且审核通过上架）
const myPublishTotal = ref<number>(0)
const isMyPublishMode = ref<boolean>(false)
const defaultCategoryId = ref<string>('')

const handleOpenDD = async (item: any) => {
  const reportName = typeof item?.reportName === 'string' ? item.reportName.trim() : undefined

  await sceneAnalysisStore.setSceneOriginData({
    ...item,
    isDetail: true
  })
  if (item.reportUrl) {
    const reportId = item?.id || ''
    // 记录报告查看行为：不阻塞跳转，避免影响用户打开速度
    if (reportId) {
      void insertReportViewLog({ reportId }).catch(error => {
        console.error('新增报告查看记录失败:', error)
      })
    }
    router.push({
      path: item.reportUrl,
      query: {
        reportJudgeId: reportId,
        isBack: '1',
        reportName
      }
    })
  } else {
    ElMessage.warning('未找到对应页面')
  }
}

/**
 * 获取通用场景数据
 */
const fetchGeneralScenario = async () => {
  try {
    generalScenarioLoading.value = true
    // 构建查询参数
    const queryParams: VocQueryParams = {
      startDate: queryStore.currentQueryParams.startDate,
      endDate: queryStore.currentQueryParams.endDate
    }

    const response = await getGeneralScenario(queryParams)

    if (response.success && response.result) {
      generalScenarioData.value = response.result || null
      console.log('通用场景数据:', generalScenarioData.value)
    } else {
      ElMessage.error(response.message || '获取通用场景数据失败')
    }
  } catch (error) {
    console.error('获取通用场景数据失败:', error)
    ElMessage.error('获取通用场景数据失败，请稍后重试')
  } finally {
    generalScenarioLoading.value = false
  }
}

/**
 * 清空二级分类，并让未完成的旧请求结果失效
 */
const clearSecondTypeOptions = () => {
  secondTypeRequestSeq += 1
  secondTypeOptios.value = []
  secondTypeLoading.value = false
}

/**
 * 获取指定一级分类下的二级分类
 * @param pid 一级分类 id
 */
const secondTypeList = async (pid: string) => {
  const requestSeq = ++secondTypeRequestSeq

  try {
    secondTypeLoading.value = true
    secondTypeOptios.value = []

    const response = await getSpecialTypeList({
      type: 2,
      pid,
      roleIds: userStore.roleId ? [userStore.roleId] : undefined
    })

    if (requestSeq !== secondTypeRequestSeq) return

    if (response.success && response.result) {
      secondTypeOptios.value = response.result || []
    } else {
      ElMessage.error(response.message)
    }
  } catch (error: any) {
    if (requestSeq !== secondTypeRequestSeq) return
    ElMessage.error(error.message)
  } finally {
    if (requestSeq === secondTypeRequestSeq) {
      secondTypeLoading.value = false
    }
  }
}

/**
 * 获取专项分析类型数据 以及分类
 */
const fetchSpecialTypeList = async () => {
  try {
    specialTypeLoading.value = true
    // 构建查询参数
    const queryParams: any = {
      type: 1,
      roleIds: userStore.roleId ? [userStore.roleId] : undefined
    }

    const response = await getSpecialTypeList(queryParams)

    if (response.success && response.result) {
      specialTypeData.value = response.result || []
      console.log('专项分析类型数据:', specialTypeData.value)
    } else {
      ElMessage.error(response.message || '获取专项分析类型数据失败')
    }
  } catch (error) {
    console.error('获取专项分析类型数据失败:', error)
    ElMessage.error('获取专项分析类型数据失败，请稍后重试')
  } finally {
    specialTypeLoading.value = false
  }
}

/**
 * 获取自定义报告列表数据
 */
const fetchCustomReportList = async (queryForm?: any) => {
  try {
    customReportLoading.value = true
    // 构建查询参数
    const queryParams: CustomReportQueryParams = {
      pageSize: reportPageSize.value,
      pageNum: reportPageNum.value,
      firstLevelZoneId: isMyPublishMode.value ? undefined : firstLevelZoneId.value,
      specialTypeId:
        isMyPublishMode.value || specialTypeId.value === 'all' ? undefined : specialTypeId.value,
      ...saQueryForm.value,
      ...queryForm,
      roleIds: userStore.roleId ? [userStore.roleId] : undefined,
      // “我发布的”只看当前登录人已发布且上架的报告
      // createBy 以员工工号为准（与后端“发布人”字段保持一致）
      createBy: isMyPublishMode.value ? appStore.userInfo.userName : undefined
      // status: 1 // 只获取已发布的报告
    }

    const response = await getCustomReportList(queryParams)

    if (response.success && response.result) {
      customReportData.value = response.result.list || []
      customReportTotal.value = response.result.total || 0
      // 用 list 接口的 total 作为“我发布的”条数来源
      if (isMyPublishMode.value) {
        myPublishTotal.value = response.result.total || 0
      }

      console.log('自定义报告数据:', customReportData.value)
    } else {
      ElMessage.error(response.message || '获取自定义报告数据失败')
    }
  } catch (error) {
    console.error('获取自定义报告数据失败:', error)
    ElMessage.error('获取自定义报告数据失败，请稍后重试')
  } finally {
    customReportLoading.value = false
  }
}

/**
 * 获取“我发布的”报告条数（已发布且审核通过上架）
 */
const fetchMyPublishTotal = async () => {
  const employeeNo = appStore.userInfo.userName
  if (!employeeNo) {
    myPublishTotal.value = 0
    return
  }

  try {
    const response = await getCustomReportList({
      pageSize: 1,
      pageNum: 1,
      createBy: employeeNo,
      roleIds: userStore.roleId ? [userStore.roleId] : undefined
    })

    if (response.success && response.result) {
      myPublishTotal.value = response.result.total || 0
    } else {
      myPublishTotal.value = 0
    }
  } catch {
    myPublishTotal.value = 0
  }
}

/**
 * 处理分类变化
 */
const handleCategoryChange = (categoryId: string) => {
  console.log('一级分类变化:---》', categoryId)
  reportPageNum.value = 1
  // 切换一级分类时，查询条件回到默认，避免残留条件导致“看起来没数据”
  saQueryForm.value = getDefaultSaQueryForm()

  // “我发布的”是特殊场景：不走一级/二级分类筛选
  if (categoryId === MY_PUBLISH_CATEGORY_ID) {
    isMyPublishMode.value = true
    firstLevelZoneId.value = undefined
    specialTypeId.value = undefined
    clearSecondTypeOptions()
    fetchCustomReportList()
    return
  }

  isMyPublishMode.value = false
  firstLevelZoneId.value = categoryId || undefined
  specialTypeId.value = undefined
  if (categoryId === ALL_CATEGORY_ID) {
    clearSecondTypeOptions()
  } else {
    secondTypeList(categoryId)
  }
  // 根据选中的分类ID重新获取报告数据
  fetchCustomReportList()
}

const secondChange = (item: any) => {
  console.log('二级分类----》item', item)
  if (isMyPublishMode.value) return
  reportPageNum.value = 1
  specialTypeId.value = item.id
  fetchCustomReportList()
}

/**
 * @description: 触发查询
 * @param {*} queryForm
 * @return {*}
 */
const SAQueryChange = (queryForm: any) => {
  console.log('queryForm', queryForm)
  if (isMyPublishMode.value) return
  reportPageNum.value = 1
  saQueryForm.value = {
    ...getDefaultSaQueryForm(),
    ...queryForm
  }
  fetchCustomReportList()
}

const handleReportPageChange = (pageNum: number) => {
  reportPageNum.value = pageNum
  fetchCustomReportList()
}

const handleReportPageSizeChange = (pageSize: number) => {
  reportPageSize.value = pageSize
  reportPageNum.value = 1
  fetchCustomReportList()
}

// 组件挂载时获取数据
onMounted(async () => {
  fetchGeneralScenario()
  fetchSpecialTypeList()

  // 先拿“我发布的”数量，再决定默认展示哪个分类，避免先请求一遍列表再切换导致闪动
  await fetchMyPublishTotal()

  if (myPublishTotal.value > 0) {
    reportPageNum.value = 1
    saQueryForm.value = getDefaultSaQueryForm()
    defaultCategoryId.value = MY_PUBLISH_CATEGORY_ID
    isMyPublishMode.value = true
    firstLevelZoneId.value = undefined
    specialTypeId.value = undefined
    clearSecondTypeOptions()
    fetchCustomReportList()
    return
  }

  // 无“我发布的”数据时，默认展示“全部”；该场景不展示二级分类，避免保留无效标签数据
  reportPageNum.value = 1
  saQueryForm.value = getDefaultSaQueryForm()
  defaultCategoryId.value = ''
  isMyPublishMode.value = false
  firstLevelZoneId.value = undefined
  specialTypeId.value = undefined
  clearSecondTypeOptions()
  fetchCustomReportList()
})
</script>

<template>
  <div>
    <FCard
      :title="'通用场景'"
      :is-show-more="false"
      :height="isComSceneExpanded ? '386px' : 'auto'"
      class="general-scene-card"
      :class="{ 'is-collapsed': !isComSceneExpanded }"
      @handleMore="toggleComScene"
    >
      <!-- 右上角：展开/收起（默认展开） -->
      <template #more>
        <span class="text-body text-secondary mr-8">
          {{ isComSceneExpanded ? '收起' : '展开' }}
        </span>
        <el-icon :size="16" color="#929AA6">
          <component :is="isComSceneExpanded ? ArrowUp : ArrowDown" />
        </el-icon>
      </template>
      <el-collapse-transition>
        <div v-show="isComSceneExpanded" class="com-scene-wrapper">
          <ComScene :data="generalScenarioData" :loading="generalScenarioLoading" />
        </div>
      </el-collapse-transition>
    </FCard>
    <FCard :title="'专项分析'" :is-show-more="false" class="mt-24">
      <SpecialAnalysis
        :category-data="specialTypeData"
        :report-data="customReportData"
        :total="customReportTotal"
        :page-num="reportPageNum"
        :page-size="reportPageSize"
        :secondTypeOptios="secondTypeOptios"
        :second-type-loading="secondTypeLoading"
        :my-publish-total="myPublishTotal"
        :default-category-id="defaultCategoryId"
        :loading="specialTypeLoading"
        :report-loading="customReportLoading"
        @open-d-d="handleOpenDD"
        @category-change="handleCategoryChange"
        @secondChange="secondChange"
        @queryChange="SAQueryChange"
        @pageChange="handleReportPageChange"
        @pageSizeChange="handleReportPageSizeChange"
      />
    </FCard>
  </div>
</template>

<style lang="scss" scoped>
.general-scene-card {
  :deep(.fc-body) {
    transition: padding 0.2s ease;
  }
}

.general-scene-card.is-collapsed {
  :deep(.fc-body) {
    padding: 0 24px 24px;
  }
}

.test-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.test-info {
  p {
    margin: 4px 0;
    font-size: 14px;
    color: #666;
  }
}
</style>
