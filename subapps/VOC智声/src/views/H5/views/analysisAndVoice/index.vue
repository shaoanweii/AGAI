<script setup lang="ts">
import RootCauseAnalysis from '@h5/components/RootCauseAnalysis/index.vue'
import VoiceListTab from './VoiceListTab.vue'
import HPage from '@h5/components/UI/HPage'
import HNavBar from '@h5/components/UI/HNavBar'
import AdvancedFilter from '@h5/components/AdvancedFilter/index.vue'
import { useRoute, useRouter } from 'vue-router'
import { AnalysisAndVoiceEnum } from '@h5/constants'
import { ref } from 'vue'
import type { VoiceListTabExpose } from './types'

defineOptions({
  name: 'analysisAndVoice'
})

const router = useRouter()
const route = useRoute()

// 当前选中的tab
const currentTab = ref((route.query.tab as string) || AnalysisAndVoiceEnum.rootCauseAnalysis)

// 声音列表组件引用
const voiceListTabRef = ref<VoiceListTabExpose>()
const rootCauseAnalysisRef = ref<any>()

const tabs = [
  {
    label: '根因分析',
    value: AnalysisAndVoiceEnum.rootCauseAnalysis
  },
  {
    label: '声音列表',
    value: AnalysisAndVoiceEnum.voiceList
  }
]

const handleBack = () => {
  router.back()
}

// 切换tab时的处理
const handleTabChange = (tabValue: string) => {
  currentTab.value = tabValue

  // 如果切换到声音列表，初始化数据
  if (tabValue === AnalysisAndVoiceEnum.voiceList && voiceListTabRef.value) {
    voiceListTabRef.value.initData()
  }
}

// 高级筛选显示控制
const filterVisible = ref<boolean>(false)

const visibleChange = (value: boolean = true) => {
  filterVisible.value = value
}

const handleAFConfirm = (filter: any) => {
  console.log('filter', filter)
  // 筛选确认后，如果当前是声音列表tab，刷新数据
  if (currentTab.value === AnalysisAndVoiceEnum.voiceList && voiceListTabRef.value) {
    voiceListTabRef.value.resetAndRefresh(filter)
  }

  if (currentTab.value === AnalysisAndVoiceEnum.rootCauseAnalysis && rootCauseAnalysisRef.value) {
    rootCauseAnalysisRef.value.resetAndRefresh(filter)
  }
}
</script>

<template>
  <HPage>
    <!-- 导航栏插槽 -->
    <template #nav-bar>
      <HNavBar left-text="返回" @click-left="handleBack">
        <template #right>
          <van-icon name="filter-o" size="24" @click="() => visibleChange()" />
        </template>
      </HNavBar>
      <!--      tabs-->
      <div class="flex-y-center mt-12 ml-12 mr-12 tabs-layout">
        <div
          v-for="(tab, index) in tabs"
          :key="index"
          class="flex-1 flex-center tab-item"
          :class="{ 'tab-active': tab.value === currentTab }"
          @click="handleTabChange(tab.value)"
        >
          <span>{{ tab.label }}</span>
        </div>
      </div>
    </template>
    <template #default>
      <div class="p-12" v-show="currentTab === AnalysisAndVoiceEnum.rootCauseAnalysis">
        <RootCauseAnalysis ref="rootCauseAnalysisRef"></RootCauseAnalysis>
      </div>

      <!-- 声音列表内容 -->
      <div v-show="currentTab === AnalysisAndVoiceEnum.voiceList">
        <VoiceListTab ref="voiceListTabRef" />
      </div>
    </template>
  </HPage>
  <!-- 高级筛选 -->
  <AdvancedFilter v-model="filterVisible" @confirm="handleAFConfirm"></AdvancedFilter>
</template>

<style lang="scss" scoped>
.tabs-layout {
  border: 1px solid #1677ff;
  border-radius: 8px;
  margin-bottom: 1px;
}
.tab-item {
  height: 40px;
  background: #eaf3ff;
  border-radius: 0;
  font-weight: 500;
  color: #1f2733;
  &:first-child {
    border-radius: 6px 0 0 6px;
  }
  &:last-child {
    border-radius: 0 6px 6px 0;
  }
}
.tab-active {
  background: linear-gradient(204deg, #42a8fe 0%, #1677ff 100%) !important;
  font-weight: 600 !important;
  color: #ffffff !important;
}
</style>
