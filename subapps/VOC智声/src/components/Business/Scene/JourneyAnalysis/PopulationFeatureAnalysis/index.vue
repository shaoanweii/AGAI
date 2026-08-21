<script setup lang="ts">
import Left from './Left.vue'
import Right from './Right.vue'
import Middle from './Middle.vue'
import type {
  AgeDistributionVo,
  GenderDistributionVo,
  RegionDistributionVo,
  UserTypeDistributionVo,
  UserFocusSceneTopVo,
  VoiceUserTopVo
} from '@/api/journeyAnalysis/types'

defineOptions({
  name: 'PopulationFeatureAnalysis'
})

const {
  ageDistributionData,
  regionDistributionData,
  genderData,
  userTypeData,
  userFocusSceneTopData,
  voiceUserTopData
} = defineProps<{
  ageDistributionData: AgeDistributionVo[]
  regionDistributionData: RegionDistributionVo[]
  genderData: GenderDistributionVo[]
  userTypeData: UserTypeDistributionVo[]
  userFocusSceneTopData: UserFocusSceneTopVo[]
  voiceUserTopData: VoiceUserTopVo[]
}>()

// 事件定义
const emit = defineEmits<{
  (e: 'chart-click', data: any): void
  (e: 'gender-click', data: GenderDistributionVo): void
  (e: 'user-type-click', data: UserTypeDistributionVo): void
  (e: 'tag-click', data: UserFocusSceneTopVo): void
  (e: 'user-click', data: VoiceUserTopVo): void
}>()

// 事件转发处理函数
const handleChartClick = (data: any) => {
  emit('chart-click', data)
}

const handleGenderClick = (data: GenderDistributionVo) => {
  emit('gender-click', data)
}

const handleUserTypeClick = (data: UserTypeDistributionVo) => {
  emit('user-type-click', data)
}

const handleTagClick = (data: UserFocusSceneTopVo) => {
  emit('tag-click', data)
}

const handleUserClick = (data: VoiceUserTopVo) => {
  emit('user-click', data)
}
</script>

<template>
  <div class="population-feature-analysis">
    <div class="left">
      <Left
        :ageDistributionData="ageDistributionData"
        :regionDistributionData="regionDistributionData"
        @chart-click="handleChartClick"
      ></Left>
    </div>
    <div class="middle">
      <Middle
        :genderData="genderData"
        :userTypeData="userTypeData"
        @gender-click="handleGenderClick"
        @user-type-click="handleUserTypeClick"
      ></Middle>
    </div>
    <div class="right">
      <Right
        :userFocusSceneTopData="userFocusSceneTopData"
        :voiceUserTopData="voiceUserTopData"
        @tag-click="handleTagClick"
        @user-click="handleUserClick"
      ></Right>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.population-feature-analysis {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-top: 24px;
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>
