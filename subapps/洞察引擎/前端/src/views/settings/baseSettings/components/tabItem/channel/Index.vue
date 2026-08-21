<script setup lang="ts">
import ChannelCategorize from '@/views/settings/baseSettings/components/tabItem/channel/ChannelCategorize.vue'
import ChannelList from '@/views/settings/baseSettings/components/tabItem/channel/ChannelList.vue'
import type { CategorizeItem } from '@/types/baseSeting.types'
import useConditions from '@/hooks/useConditions'

const { conditions } = useConditions({ url: '/insights/channel/conditions' })
provide('conditions', conditions)

const curCategorize = ref<CategorizeItem>()
const channelListRef = ref()
// 更新选中的渠道分类
const tapCategorizeChange = (item: CategorizeItem) => {
  curCategorize.value = item
  if (item && Object.keys(item).length) {
    channelListRef.value?.getChannelList(item)
  } else {
    channelListRef.value?.resetChannel()
  }
}

const categorizeTree = ref([])
const getCategorize = (list: any) => {
  categorizeTree.value = list
}
</script>

<template>
  <div class="jg" style="width: 100%; height: 24px"></div>
  <div class="flex main-table">
    <div class="categorize ft-card">
      <ChannelCategorize
        @tapCategorizeChange="tapCategorizeChange"
        @getCategorize="getCategorize"
      ></ChannelCategorize>
    </div>
    <div class="jg"></div>
    <div class="channel-list">
      <ChannelList
        ref="channelListRef"
        :curCategorize="curCategorize"
        :categorizeTree="categorizeTree"
      ></ChannelList>
    </div>
  </div>
</template>

<style scoped lang="scss">
.bgc {
  background-color: #f0f3fa;
}

.jg {
  width: 24px;
  background-color: #f0f3fa;
}

.categorize {
  width: 380px;
  //margin-right: 24px;
  min-height: 100px;
}

.channel-list {
  flex: 1 1 0;
  min-height: 100px;
}

.ft-card {
  background: #fff;
  border-radius: 4px;
  padding: 24px;
}
</style>
