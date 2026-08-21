<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import HVoiceList from '@h5/components/HVoiceList/index.vue'
import { getVocListSounds } from '@h5/api/home'
import { showToast } from 'vant'
import type { H5VocBaseRequest, VoiceListItem } from '@h5/api/home/types'

/**
 * 声音列表组件
 */
defineOptions({
  name: 'VoiceListTab'
})

const route = useRoute()

const adFilter = ref<any>()

const hub = reactive({
  voiceList: [] as VoiceListItem[],
  loading: false,
  refreshing: false,
  finished: false,
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 判断声音列表是否为空
const isVoiceListEmpty = computed(
  () => !hub.loading && !hub.refreshing && hub.voiceList.length === 0
)

// 重置并刷新数据
const resetAndRefresh = (filters?: any) => {
  hub.pageNum = 1
  hub.voiceList = []
  hub.finished = false

  adFilter.value = filters
  getVoiceList()
}

// 下拉刷新
const onRefresh = () => {
  hub.refreshing = true
  hub.pageNum = 1
  hub.voiceList = []
  hub.finished = false
  getVoiceList().finally(() => {
    hub.refreshing = false
  })
}

// 上拉加载
const onLoad = () => {
  if (hub.finished) {
    hub.loading = false
    return
  }
  getVoiceList()
}

// 获取声音列表数据
const getVoiceList = (): Promise<void> => {
  hub.loading = true
  const requestParams: H5VocBaseRequest = {
    ...route.query,
    provinceCodeSet: JSON.parse((route.query.provinceCodeSet as string) || '[]'),
    pageSize: hub.pageSize,
    pageNum: hub.pageNum,
    filterItems: adFilter.value ? adFilter.value : undefined
  }

  return getVocListSounds(requestParams)
    .then((res: BaseResponse) => {
      if (res.success && res.result) {
        const transformedList = res.result.list || []

        if (hub.pageNum === 1) {
          hub.voiceList = transformedList
        } else {
          hub.voiceList.push(...transformedList)
        }

        hub.total = res.result.total || 0

        // 判断是否已加载完所有数据
        if (hub.voiceList.length >= hub.total || transformedList.length < hub.pageSize) {
          hub.finished = true
        } else {
          hub.pageNum++
        }
      } else {
        showToast(res.message || '获取数据失败')
        hub.finished = true
      }
    })
    .catch((err: any) => {
      console.error('获取声音列表失败:', err)
      showToast('网络错误，请重试')
      hub.finished = true
    })
    .finally(() => {
      hub.loading = false
    })
}

// 初始化数据加载
const initData = () => {
  if (hub.voiceList.length === 0) {
    resetAndRefresh()
  }
}

// 暴露方法给父组件
defineExpose({
  initData,
  resetAndRefresh
})

onMounted(() => {
  initData()
})
</script>

<template>
  <div class="voice-list-tab">
    <van-pull-refresh v-model="hub.refreshing" @refresh="onRefresh" :disabled="hub.loading">
      <van-list
        v-model:loading="hub.loading"
        v-model:finished="hub.finished"
        :finished-text="isVoiceListEmpty ? '' : '没有更多了'"
        @load="onLoad"
        :immediate-check="false"
      >
        <div class="p-12">
          <HVoiceList :voice-list="hub.voiceList" />
        </div>

        <!-- 无数据视图 -->
        <van-empty v-if="isVoiceListEmpty" description="暂无声音数据" class="empty-container" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<style lang="scss" scoped>
.voice-list-tab {
  height: 100%;
}

.empty-container {
  padding: 60px 20px;
}
</style>
