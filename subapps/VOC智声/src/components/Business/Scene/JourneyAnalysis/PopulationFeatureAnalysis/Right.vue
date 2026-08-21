<script setup lang="ts">
import FocusScene from './FocusScene.vue'
import VoiceUserTop from './VoiceUserTop.vue'
import type { UserFocusSceneTopVo, VoiceUserTopVo } from '@/api/journeyAnalysis/types'

defineOptions({
  name: 'Right'
})

const { userFocusSceneTopData, voiceUserTopData } = defineProps<{
  userFocusSceneTopData: UserFocusSceneTopVo[]
  voiceUserTopData: VoiceUserTopVo[]
}>()

// 事件定义
const emit = defineEmits<{
  (e: 'tag-click', data: UserFocusSceneTopVo): void
  (e: 'user-click', data: VoiceUserTopVo): void
}>()

// 处理标签点击事件
const handleTagClick = (data: UserFocusSceneTopVo) => {
  emit('tag-click', data)
}

// 处理用户点击事件
const handleUserClick = (data: VoiceUserTopVo) => {
  emit('user-click', data)
}
</script>

<template>
  <div class="right-view">
    <FCard title="关注场景" titleSize="middle" :height="'357px'" class="f-card-border">
      <FocusScene :data="userFocusSceneTopData" @tag-click="handleTagClick" />
    </FCard>

    <FCard title="发声用户TOP" titleSize="middle" :height="'357px'" class="f-card-border mt-24">
      <VoiceUserTop :data="voiceUserTopData" @user-click="handleUserClick" />
    </FCard>
  </div>
</template>

<style lang="scss" scoped></style>
