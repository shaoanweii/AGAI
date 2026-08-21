<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getRealAttr } from '@/views/leaderOverview/leader/common/fn.ts'
import { useQueryStore } from '@/store/modules/query'
import { getUserIntentionOpinionTop } from '@/api/overview/leader'
import TopTable from './TopTable.vue'
import top_gd1 from '@/assets/images/top-gd1.png'
import top_gd2 from '@/assets/images/top-gd2.png'
import top_gd3 from '@/assets/images/top-gd3.png'
import top_gd4 from '@/assets/images/top-gd4.png'
import { debounce } from 'lodash-es'
import ViewMore from '@/components/Business/DrillDownDialog/components/ViewMore.vue'
import useGeneralDrillDownStore from '@/store/modules/generalDrillDown'
import { DrillTabKey } from '@/components/Business/DrillDownDialog/constants'

defineOptions({
  name: 'TopBank' //原FocusOnSceneDistribution
})

// 基础设置
const queryStore = useQueryStore()
const ddStore = useGeneralDrillDownStore()
const storePms = queryStore.currentQueryParams
const leaderGroupCode = 'groupCode'
const leaderGroupName = '智行汽车集团'

/**
 * 统一品牌洞察模块的品牌筛选参数。
 * 前端状态里仍使用 groupCode 占位，真正请求时再转换为 automark。
 *
 * @returns 当前观点 TOP 请求需要补充的品牌筛选参数
 */
const getInsightBrandQueryParams = (tempCode?: string): Record<string, string | undefined> => {
  if (tempCode === leaderGroupCode) {
    return {
      automark: leaderGroupName,
      brandCode: undefined,
      tempCode: undefined
    }
  }

  return {
    brandCode: tempCode,
    automark: undefined,
    tempCode: undefined
  }
}

const loading = ref(false)
const topData = ref<any>({
  complaintOpinions: [],
  consultOpinions: [],
  suggestionOpinions: [],
  praiseOpinions: []
})

// 表格组件引用
const complaintTableRef = ref<InstanceType<typeof TopTable>>()
const consultTableRef = ref<InstanceType<typeof TopTable>>()
const suggestionTableRef = ref<InstanceType<typeof TopTable>>()
const praiseTableRef = ref<InstanceType<typeof TopTable>>()

// 清空所有表格的排序状态
const clearAllSort = () => {
  complaintTableRef.value?.clearSort()
  consultTableRef.value?.clearSort()
  suggestionTableRef.value?.clearSort()
  praiseTableRef.value?.clearSort()
}

// 暴露方法供父组件调用
defineExpose({
  clearAllSort
})

const fetchData = async () => {
  try {
    loading.value = true

    const intentions = ['抱怨', '咨询', '建议', '表扬']
    const keys = ['complaintOpinions', 'consultOpinions', 'suggestionOpinions', 'praiseOpinions']
    const promises = intentions.map(intention =>
      getUserIntentionOpinionTop(
        getRealAttr({
          ...storePms,
          ...getInsightBrandQueryParams(storePms.tempCode),
          topic: '',
          intention
        })
      )
    )

    const responses = await Promise.all(promises)

    const result: any = {}
    responses.forEach((response, index) => {
      result[keys[index]] = response.success ? response.result : []
    })

    topData.value = result
  } catch (error) {
    console.error('获取用户意图观点TOP数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const fetchDataDelay = debounce(fetchData, 300)

const handleSort = async (intention: string, prop: string, order: string | null) => {
  try {
    const intentionMap: Record<string, string> = {
      抱怨: 'complaintOpinions',
      咨询: 'consultOpinions',
      建议: 'suggestionOpinions',
      表扬: 'praiseOpinions'
    }

    const response = await getUserIntentionOpinionTop(
      getRealAttr({
        ...storePms,
        ...getInsightBrandQueryParams(storePms.tempCode),
        topic: '',
        intention,
        sortField: prop,
        sortOrder: order
      })
    )

    if (response.success) {
      const key = intentionMap[intention]
      topData.value[key] = response.result || []
    }
  } catch (error) {
    console.error('排序失败:', error)
  }
}

/**
 * 点击观点 TOP 行后同步更新客户原声筛选。
 * 兼容历史整行对象与当前观点名称字符串两种入参。
 *
 * @param row 当前点击的观点名称或观点对象
 * @param intention 当前意图
 */
const handleClick = (row: string | Record<string, any>, intention: string) => {
  const nextTopic =
    typeof row === 'string' ? row.trim() : String(row?.opinionName || row?.topic || '').trim()
  queryStore.updateQueryParams({
    topic: nextTopic || undefined,
    intention
  })
}

/**
 * 查看更多
 */
const handleViewMore = (intention: string) => {
  ddStore.openDD(
    {
      ...storePms,
      ...getInsightBrandQueryParams(storePms.tempCode),
      topic: '',
      intention
    },
    {
      subTitle: `${intention || ''}观点TOP`,
      activeTab: DrillTabKey.VIEWPOINT
    },
    [],
    {
      // 领导页下钻不继承场景页的公共筛选，避免跨页面参数串值
      mergeCommonQueryParams: false
    }
  )
}

onMounted(() => {
  fetchDataDelay()
})

watch(
  () => ({
    startDate: storePms.startDate,
    endDate: storePms.endDate
  }),
  () => {
    fetchDataDelay()
  }
)

watch(
  () => [storePms.tempCode, storePms.channelCatagory, storePms.tag2Code],
  () => {
    fetchDataDelay()
  }
)
</script>

<template>
  <div class="focus-on-scene-distribution">
    <div class="mt-24 top-container" v-loading="loading">
      <FCard
        :title="'抱怨观点TOP'"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleViewMore('抱怨')"
      >
        <template #leftExtra>
          <img :src="top_gd1" alt="" class="topImg" />
        </template>
        <template #more>
          <ViewMore />
        </template>

        <TopTable
          ref="complaintTableRef"
          :data="topData.complaintOpinions"
          intention="抱怨"
          @sort-change="handleSort"
          @row-click="handleClick"
        ></TopTable>
      </FCard>
      <FCard
        :title="'咨询观点TOP'"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleViewMore('咨询')"
      >
        <template #leftExtra>
          <img :src="top_gd2" alt="" class="topImg" />
        </template>
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="consultTableRef"
          :data="topData.consultOpinions"
          intention="咨询"
          @sort-change="handleSort"
          @row-click="handleClick"
        ></TopTable>
      </FCard>
      <FCard
        :title="'建议观点TOP'"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleViewMore('建议')"
      >
        <template #leftExtra>
          <img :src="top_gd3" alt="" class="topImg" />
        </template>
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="suggestionTableRef"
          :data="topData.suggestionOpinions"
          intention="建议"
          @sort-change="handleSort"
          @row-click="handleClick"
        ></TopTable>
      </FCard>
      <FCard
        :title="'表扬观点TOP'"
        :height="'340px'"
        :isShowMore="true"
        class="f-card-border"
        @handleMore="() => handleViewMore('表扬')"
      >
        <template #leftExtra>
          <img :src="top_gd4" alt="" class="topImg" />
        </template>
        <template #more>
          <ViewMore />
        </template>
        <TopTable
          ref="praiseTableRef"
          :data="topData.praiseOpinions"
          intention="表扬"
          @sort-change="handleSort"
          @row-click="handleClick"
        ></TopTable>
      </FCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@media screen and (max-width: 1600px) {
  :deep(.f-card) {
    .fc-header {
      padding-left: 12px !important;
      padding-right: 12px !important;
    }
    .fc-body {
      padding-left: 10px !important;
      padding-right: 10px !important;
    }
  }
}

.focus-on-scene-distribution {
  .top-container {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    :deep(.f-card) {
      height: 335px !important;
      background: red;
      box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05) !important;
      border-radius: 12px 12px 12px 12px !important;
      border: 1px solid #ebedf0;

      .fc-header {
        padding: 16px 16px 0;
      }
      .fc-body {
        padding: 16px;
      }

      .fch-left {
        position: relative;
        .text-h2 {
          margin-left: 28px;
          font-size: 16px !important;
        }
        .topImg {
          position: absolute;
          left: 0;
          top: 6px;
          width: 20px;
        }
      }

      .text-h3 {
        line-height: 32px;
        font-size: 16px !important;
      }
    }
  }
}
</style>
