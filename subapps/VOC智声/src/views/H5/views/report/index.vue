<script setup lang="ts">
import {reactive, ref, computed, onMounted} from 'vue'
import type {EChartsOption} from 'echarts'
import HPage from '@h5/components/UI/HPage/index.vue'
import HCard from '@h5/components/UI/HCard/index.vue'
import BrandTrendChange from '@h5/views/report/BrandTrendChange/index.vue'
import type {TrendItem} from '@h5/views/report/BrandTrendChange/types.d.ts'
import ReportBlock from '@h5/views/report/components/ReportBlock.vue'
import ReportTitleContainer from "@h5/views/report/components/TitleContainer.vue";
import HEcharts from "@h5/components/UI/HEcharts/index.vue";
import {fmtNum, formatAxisLabel, fmtPer, fmtFix, Thousandth} from '@/utils'
import HVoiceListReport from '@h5/views/report/components/HVoiceList/index.vue'
import {getH5Report} from '@h5/api/report'
import {useRoute} from 'vue-router'

// 页面占位查询参数（后续可接真实入参）
const query = reactive({})

// 品牌趋势变化数据（默认空，由接口回填）
const brandTrendItems = ref<TrendItem[]>([])

// 顶部简报 briefReport 与品牌名
const briefReport = ref<any>({})
const brandName = ref<string>('')
// 顶部信息（标题、来源、周期、说明）
const heroTop = ref<{ title?: string; description?: string; dataSource?: string; dataCycle?: string }>({})
// 产品/服务 顶部卡片数据（数组）
const productTopList = ref<any[]>([])
const serviceTopList = ref<any[]>([])

// 车系分布图表：根据某一项的 seriesRanks 直接生成配置
const buildBarOptionsFromSeriesRanks = (seriesRanks: any[] = []): EChartsOption => {
  const names = seriesRanks.map((s: any) => s?.name || '')
  const values = seriesRanks.map((s: any) => Number(s?.mentions ?? s?.mention ?? 0))
  const rotate = names.length >= 5 ? 60 : 0
  return {
    color: ['#60B8EB'],
    grid: {top: 30, left: 15, right: 10, bottom: 0, containLabel: true},
    tooltip: {
      show: false,
      trigger: 'axis',
      axisPointer: {type: 'shadow'},
      formatter: (params: any[]) => {
        const p = params[0]
        return `${p.axisValue}<br/>提及量：${fmtNum(p.value)}`
      }
    },
    xAxis: {
      type: 'category',
      data: names,
      axisTick: {show: false},
      axisLine: {lineStyle: {color: '#F1F1F5'}},
      axisLabel: {
        show: true,
        interval: 0,
        width: 100,
        overflow: 'break',
        color: '#5F6A7A',
        fontSize: 10,
        rotate,
        formatter: function (value: string) {
          return value
        }
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {show: true, lineStyle: {color: '#F1F1F5'}},
      max: 'dataMax',
      splitNumber: 4,
      minInterval:2,
      min: 0,
      axisLabel: {
        formatter: function (value: number) {
          return formatAxisLabel(value)
        },
        color: '#92929D'
      }
    },
    series: [
      {
        name: '提及量',
        type: 'bar',
        data: values,
        barMaxWidth: 16,
        itemStyle: {color: '#60B8EB'},
        label: {
          show: true,
          position: 'top',
          color: '#666',
          fontSize: 10,
          formatter: (p: any) => fmtNum(values[p.dataIndex] || 0)
        }
      }
    ]
  } as any
}

// 产品/服务：采用就地生成图表配置与原声列表，无需额外状态数组

// HVoiceList：直接透传接口的 userVoices，由子组件做显示字段收敛

const onVoiceItemClick = (item: any) => {
  // 这里可对接原声详情弹层
  console.log('voice item click:', item)
}
const onVoiceLoadMore = () => {
  console.log('voice load more')
}

// 进入页面时加载：报告接口
const route = useRoute()
const loadReport = async () => {
  try {
    const res = await getH5Report({
      // 路由参数中的品牌编码
      brandCode: route.query.brandCode as string
    })
    if (res && (res as any).success) {
      const data: any = (res as any).result || {}
      // 顶部简报
      briefReport.value = data.briefReport || {}
      // 品牌名
      brandName.value = data.brandName || ''
      // 顶部信息
      heroTop.value = {
        title: data.title,
        description: data.description,
        dataSource: data.dataSource,
        dataCycle: data.dataCycle
      }
      // 趋势卡片
      brandTrendItems.value = Array.isArray(data.brandTrendComparison) ? (data.brandTrendComparison as any[]) : []
      // 产品/服务 TOP 列表
      productTopList.value = Array.isArray(data.productTopDetail) ? data.productTopDetail : []
      serviceTopList.value = Array.isArray(data.serviceTopDetail) ? data.serviceTopDetail : []
    } else {
      briefReport.value = {}
      brandTrendItems.value = []
      heroTop.value = {}
      productTopList.value = []
      serviceTopList.value = []
    }
  } catch (e) {
    console.error('getH5Report error:', e)
    briefReport.value = {}
    brandTrendItems.value = []
    heroTop.value = {}
    productTopList.value = []
    serviceTopList.value = []
  }
}

onMounted(() => {
  loadReport()
})
</script>
<template>
  <HPage background-color="rgba(230, 236, 245, 1)" class="h5-report">
    <!-- 内容区域 -->
    <div class="h5-report__content">
      <!-- 第一块：头部信息 + 蓝色指标区（所有数字为占位符） -->
      <section class="h5-report__hero">
        <div class="h5-report__hero-top">
          <div class="h5-report__hero-title">{{ heroTop.title || '—' }}</div>
          <div class="h5-report__hero-rows">
            <div class="h5-report__row">
              <div class="h5-report__row-key">报告说明：</div>
              <div class="h5-report__row-val">{{ heroTop.description || '—' }}</div>
            </div>
            <div class="h5-report__row">
              <div class="h5-report__row-key">数据来源：</div>
              <div class="h5-report__row-val">{{ heroTop.dataSource || '—' }}</div>
            </div>
            <div class="h5-report__row">
              <div class="h5-report__row-key">数据周期：</div>
              <div class="h5-report__row-val">{{ heroTop.dataCycle || '—' }}</div>
            </div>
          </div>
        </div>
        <div class="h5-report__hero-bottom">
          <div class="h5-report__hero-left">
            <div class="h5-report__hero-topline">
              <div class="h5-report__hero-number">{{ briefReport?.negativeRate != null ? fmtPer(briefReport.negativeRate) : '—%' }}</div>
              <div class="h5-report__hero-pill">环比 {{ briefReport?.negativeRateMom != null ? fmtFix(briefReport.negativeRateMom) : '—%' }}</div>
            </div>
            <div class="h5-report__hero-sub">本周负面率</div>
          </div>
          <div class="h5-report__hero-right">
            <div class="h5-report__hero-right-row">
              <div class="h5-report__hero-right-key">提及量</div>
              <div class="h5-report__hero-right-val">{{ briefReport?.mentionCount != null ? Thousandth(briefReport.mentionCount) : '—' }}</div>
            </div>
            <div class="h5-report__hero-right-row">
              <div class="h5-report__hero-right-key">环比</div>
              <div class="h5-report__hero-right-val">{{ briefReport?.mentionCountMom != null ? fmtFix(briefReport.mentionCountMom) : '—%' }}</div>
            </div>
          </div>
        </div>
      </section>
      <!-- 品牌声量趋势 -->
      <section class="h5-report__section">
        <BrandTrendChange :items="brandTrendItems" :show-tooltip="true" :brand-name="brandName" />
      </section>
      <!-- 客户抱怨TOP 容器（封装组件） -->
      <ReportBlock title="客户抱怨TOP【产品类】" title-bg="report-bg">
        <report-title-container
          v-for="(item, idx) in productTopList"
          :key="`prod-${idx}`"
          :title="item?.opinion || '—'"
          :rank="idx + 1"
          :percent="item?.mentionMom"
          :number="item?.mention"
        >
          <HCard title="车系分布" class="mt-12" height="auto">
            <HEcharts :options="buildBarOptionsFromSeriesRanks(item?.seriesRanks || [])" width="100%" height="270px" />
          </HCard>
          <HCard title="客户原声" class="mt-12" v-if="item.userVoices.length > 0">
            <HVoiceListReport
              :voice-list="item?.userVoices || []"
              :is-load-more="false"
              :loading="false"
              @item-click="onVoiceItemClick"
              @load-more="onVoiceLoadMore"
            />
          </HCard>
        </report-title-container>
      </ReportBlock>
      <!-- 客户抱怨TOP 容器（封装组件） -->
      <ReportBlock title="客户抱怨TOP【服务类】" title-bg="report-bg2">
        <report-title-container
          v-for="(item, idx) in serviceTopList"
          :key="`serv-${idx}`"
          :title="item?.opinion || '—'"
          :rank="idx + 1"
          :percent="item?.mentionMom"
          :number="item?.mention"
          :bg="'green'"
        >
          <HCard title="车系分布" class="mt-12" height="auto">
            <HEcharts :options="buildBarOptionsFromSeriesRanks(item?.seriesRanks || [])" width="100%" height="270px" />
          </HCard>
          <HCard title="客户原声" class="mt-12" v-if="item.userVoices.length > 0">
            <HVoiceListReport
              :voice-list="item?.userVoices || []"
              :is-load-more="false"
              :loading="false"
              @item-click="onVoiceItemClick"
              @load-more="onVoiceLoadMore"
            />
          </HCard>
        </report-title-container>
      </ReportBlock>
    </div>
  </HPage>
</template>
<style scoped lang="scss">
.h5-report {
  .h5-report__content {
    background-color: rgba(230, 236, 245, 1);
    background-image: url('@/assets/h5/report/report-bg.png');
    background-repeat: no-repeat;
    background-position: top center;
    background-size: 100% 943px; // 沾满宽度

  }

  .h5-report__content {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-top: 354px; // 内容区距页面顶部 354px 开始
  }

  .h5-report__section {
    display: block;
  }

  // Hero
  .h5-report__hero {
    border-radius: 8px;
    overflow: hidden;
  }

  .h5-report__hero-top {
    background: #F4FBFF;
    border-radius: 8px 8px 0 0;
    padding: 12px 0 18px 0;
  }

  .h5-report__hero-title {
    font-weight: 500;
    font-size: 14px;
    color: #000;
    line-height: 20px;
    text-align: left;
    font-style: normal;
    text-transform: none;
    position: relative;
    display: flex;
    align-items: center;
    min-height: 28px; // 配合装饰条高度
    margin: 0 0 12px 0;
    padding-left: 8px; // 贴近左侧，避免与3px装饰条重叠
    z-index: 1; // 确保文字在装饰条之上
  }

  // 背景条装饰（不影响标题文字不透明度）
  .h5-report__hero-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 100%;
    height: 28px;
    background: linear-gradient(90deg, #2F93FD 0%, rgba(47, 147, 253, 0.1) 100%);
    opacity: 0.1;
    border-radius: 0px;
    pointer-events: none;
    z-index: 0;
  }

  // 左侧竖条装饰
  .h5-report__hero-title::after {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 28px;
    background: #2F93FD;
    border-radius: 0px;
    pointer-events: none;
    z-index: 0;
  }

  .h5-report__hero-rows {
    margin-top: 10px;
    display: grid;
    gap: 12px; // 每行间距 12px
    margin-left: 15px;
  }

  .h5-report__row {
    display: grid;
    grid-template-columns: 70px 1fr;
    align-items: center;
  }

  .h5-report__row-key {
    font-weight: 400;
    font-size: 12px;
    color: #1F2733;
    line-height: 16px;
    text-align: left;
    font-style: normal;
    text-transform: none;
  }

  .h5-report__row-val {
    font-weight: 400;
    font-size: 12px;
    color: #1D2129;
    line-height: 20px;
    text-align: left;
    font-style: normal;
    text-transform: none;
  }

  .h5-report__hero-bottom {
    height: 73px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: linear-gradient(180deg, #0DAEFF 0%, #0DAEFF 100%); // 纯色渐变
    border-radius: 8px 8px 8px 8px; // 四角 8px
    color: #FFF;
  }

  .h5-report__hero-left {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .h5-report__hero-topline {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .h5-report__hero-number {
    font-size: 20px;
    font-weight: 500;
  }

  .h5-report__hero-pill {
    min-width: 72px;
    padding: 0 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: #FAFAFA;
    border-radius: 16px;
    border: 1px solid #E9EAEB;
    font-weight: 500;
    font-size: 10px;
    color: #5F6A7A;
    width: fit-content;
    height: 18px;
    line-height: 18px;
    text-align: center;
    font-style: normal;
    text-transform: none;
  }

  .h5-report__hero-sub {
    font-size: 12px;
    color: #FFF;
  }

  .h5-report__hero-right {
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 140px;

    font-weight: 500;
    font-size: 12px;
    color: #FFF;
    text-align: right;
    font-style: normal;
    text-transform: none;
  }

  .h5-report__hero-right-row {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
  }

  .h5-report__hero-right-key {
    width: 40px;
    text-align: right;
    font-size: 12px;
    color: #FFF;
  }

  .h5-report__hero-right-val {
    min-width: 64px;
    text-align: right;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.9);
  }

  // ================== 报表模块：背景标题条 + 内容卡片 ==================
  .report-block {
    position: relative;
  }

  .report-block__title {
    position: relative;
    width: 351px;
    height: 56px;
    background-image: url('@/assets/h5/report/title-bg.png');
    background-repeat: no-repeat;
    background-size: cover;
    background-position: left top;
  }

  .report-block__title-text {
    position: absolute;
    top: 9px; // 距离上边 9px
    left: 16px; // 距离左边 16px
    font-size: 16px;
    color: #FFF;
    text-align: center;
    font-style: normal;
    text-transform: none;
    font-weight: 500;
  }

  .report-block__body {
    margin-top: -16px; // 下方模块向上覆盖标题 16px
    position: relative;
    z-index: 10;
  }

  .report-block__card {
  }

  .report-block__placeholder {
    padding: 12px;
  }

  .report-block__sub-title {
    font-weight: 500;
    font-size: 14px;
    color: #1F2733;
    margin-bottom: 8px;
  }

  .report-block__placeholder-box {
    height: 160px;
    border-radius: 8px;
    background: #F5F7FA;
    border: 1px dashed #E5E7EB;
    color: #6B7280;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  // 渐变容器（通用）
  .h5-report__gradient-container {
    background: linear-gradient(201deg, #C5E8FA 0%, #FFF 100%);
    border-radius: 8px;
    padding: 18px 12px;
  }
}
</style>
