<script setup lang="ts">
import Categorize from '@/views/settings/baseSettings/components/tabItem/config/Categorize.vue'
import RegionList from '@/views/settings/baseSettings/components/tabItem/config/RegionList.vue'
import type { CategorizeItem } from '@/types/baseSeting.types'
import useConditions from '@/hooks/useConditions'

const { conditions } = useConditions({ url: '/insights/region/conditions' })
provide('conditions', conditions)

const curCategorize = ref<CategorizeItem>()
const channelListRef = ref()
// 更新选中的渠道分类
const tapCategorizeChange = (item: CategorizeItem) => {
  curCategorize.value = item
  if (item && Object.keys(item).length) {
    channelListRef.value?.getList(item)
  } else {
    channelListRef.value?.resetList()
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
      <Categorize
        @tapCategorizeChange="tapCategorizeChange"
        @getCategorize="getCategorize"
      ></Categorize>
    </div>
    <div class="jg"></div>
    <div class="channel-list">
      <RegionList
        ref="channelListRef"
        :curCategorize="curCategorize"
        :categorizeTree="categorizeTree"
      ></RegionList>
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
