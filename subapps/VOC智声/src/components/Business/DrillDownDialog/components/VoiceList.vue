<script setup lang="ts">
import { ref } from 'vue'
import VoiceListPanel from '@components/Business/VoiceListPanel/index.vue'

defineOptions({ name: 'DrillDownVoiceList' })

const props = defineProps<{ queryParams?: VocQueryParams }>()
const voiceListPanelRef = ref<InstanceType<typeof VoiceListPanel> | null>(null)

/**
 * 转发浏览记录上报，兼容原下钻客户原声组件对外暴露的能力。
 */
const submitBrowseRecord = async () => {
  if (voiceListPanelRef.value) {
    await voiceListPanelRef.value.submitBrowseRecord()
  }
}

/**
 * 转发刷新能力，兼容下钻容器后续主动重查。
 */
const refresh = async () => {
  if (voiceListPanelRef.value) {
    await voiceListPanelRef.value.refresh()
  }
}

defineExpose({
  submitBrowseRecord,
  refresh
})
</script>

<template>
  <VoiceListPanel
    ref="voiceListPanelRef"
    title="客户原声"
    :query-params="props.queryParams"
    list-api-url="/report/voc-sounds/getVocListSounds"
    detail-api-url="/report/voc-sounds/getSoundsDetails"
    :default-page-size="10"
    :watch-store-query="false"
    container-mode="embedded"
    :show-sort-select="true"
    :show-high-quality-filter="true"
    :enable-high-quality-actions="true"
    :enable-high-quality-info="true"
    :enable-voice-management-params="true"
    :enable-error-correction="true"
    :show-batch-action="true"
    :enable-event-issue-action="true"
    user-detail-mode="back"
  />
</template>
