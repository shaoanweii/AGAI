<script setup lang="ts">
import HPage from '@h5/components/UI/HPage/index'
import HNavBar from '@h5/components/UI/HNavBar/index'
import HVoiceList from '@h5/components/HVoiceList/index'
import { useRoute, useRouter } from 'vue-router'
import { computed, onMounted, reactive, ref } from 'vue'
import { getVocListSounds, getDictItemsByDict, getBrowseListSounds } from '@h5/api/home'
import { showToast } from 'vant'
import type {
  H5VocBaseRequest,
  VoiceListItem,
  DictItemVo
} from '@h5/api/home/types'
import AdvancedFilter from '@h5/components/AdvancedFilter/index.vue'

const router = useRouter()
const route = useRoute()

const hub = reactive({
  loading: false,
  refreshing: false,
  finished: false,
  voiceTypeTabs: [
    {
      text: '抱怨',
      value: '抱怨',
      icon: 'h5-thumbs-down'
    },
    {
      text: '咨询',
      value: '咨询',
      icon: 'h5-notification-message'
    },
    {
      text: '建议',
      value: '建议',
      icon: 'h5-notification-text'
    },
    {
      text: '表扬',
      value: '表扬',
      icon: 'h5-thumbs-up'
    }
  ],
  currentVoiceType: '', //当前选中的标签
  sceneTypeTabs: [] as any[],
  currentScene: '',
  list: [] as VoiceListItem[],
  pageNum: 1,
  pageSize: 20,
  total: 0,
  filterVisible: false //筛选弹窗
})

// 从路由参数初始化声音类型：优先 route.params.intention，其次 voiceTypeTabs[0]
const initVoiceTypeFromRoute = () => {
  const tabParam = (route.query?.intention as string) || ''
  const allowed = hub.voiceTypeTabs.map((i: { value: string }) => i.value)
  if (tabParam && allowed.includes(tabParam)) {
    hub.currentVoiceType = tabParam
  } else {
    hub.currentVoiceType = hub.voiceTypeTabs[0]?.value || ''
  }
}

// 判断是否为空
const isEmpty = computed(() => !hub.loading && !hub.refreshing && hub.list.length === 0)

onMounted(() => {
  // 初始化默认声音类型
  initVoiceTypeFromRoute()
  // 获取字典数据
  fetchSceneTypeTabs()
})

const handleBack = () => {
  router.back()
}

// 切换标签
const voiceTypeChange = (type: string) => {
  hub.currentVoiceType = type
  resetAndRefresh()
}

// 筛选场景
const sceneTypeChange = (type: string) => {
  hub.currentScene = type
  resetAndRefresh()
}

const adFilter = ref<any>()

// 高级筛选
const handleAFConfirm = (filter: any) => {
  console.log('filter', filter)
  adFilter.value = filter
  resetAndRefresh()
}

//筛选弹框
const visibleChange = (value: boolean = true) => {
  hub.filterVisible = value
}

// 重置并刷新数据
const resetAndRefresh = () => {
  hub.pageNum = 1
  hub.list = []
  hub.finished = false
  getList()
}

// 下拉刷新
const onRefresh = () => {
  hub.refreshing = true
  hub.pageNum = 1
  hub.list = []
  hub.finished = false
  getList().finally(() => {
    hub.refreshing = false
  })
}

// 上拉加载
const onLoad = () => {
  if (hub.finished) {
    hub.loading = false
    return
  }
  getList()
}

// 获取场景类型字典数据
const fetchSceneTypeTabs = async () => {
  try {
    const response = await getDictItemsByDict('content_type')
    if (response.success && response.result && response.result.length > 0) {
      // 将字典数据转换为组件需要的格式
      hub.sceneTypeTabs = response.result
        .filter((item: DictItemVo) => item.status === 1) // 只显示启用的字典项
        .sort((a: DictItemVo, b: DictItemVo) => a.sortOrder - b.sortOrder) // 按排序字段排序
        .map((item: DictItemVo) => ({
          text: item.itemText,
          value: item.itemValue
        }))

      // 设置默认选中第一个
      if (hub.sceneTypeTabs.length > 0) {
        hub.currentScene = hub.sceneTypeTabs[0].value
      }
      // 获取列表数据
      getList()
    } else {
      console.warn('未获取到场景类型字典数据')
    }
  } catch (error) {
    console.error('获取场景类型字典数据失败:', error)
    showToast('获取场景类型失败')
  }
}

const getList = (): Promise<void> => {
  hub.loading = true
  const requestParams: H5VocBaseRequest = {
    ...route.query,
    provinceCodeSet: JSON.parse((route.query.provinceCodeSet as string) || '[]'),
    intention: hub.currentVoiceType,
    contentType: hub.currentScene,
    pageSize: hub.pageSize,
    pageNum: hub.pageNum,
    filterItems: adFilter.value ? adFilter.value : undefined
  }
  let func: any = getVocListSounds
  //任务详情跳转过来查询以往记录的 数据
  if(route.params.tag === 'history') {
    func = getBrowseListSounds
  }
  return func(requestParams)
    .then((res: BaseResponse) => {
      if (res.success && res.result) {
        const { list, total } = res.result

        if (hub.pageNum === 1) {
          hub.list = list || []
        } else {
          hub.list.push(...(list || []))
        }

        hub.total = total || 0

        // 判断是否已加载完所有数据
        if (hub.list.length >= hub.total || (list && list.length < hub.pageSize)) {
          hub.finished = true
        } else {
          hub.pageNum++
        }
      } else {
        showToast(res.message || '获取数据失败')
        hub.finished = true // 请求失败时也应该停止加载
      }
    })
    .catch((err: any) => {
      console.error('获取声音列表失败:', err)
      showToast('网络错误，请重试')
      hub.finished = true // 网络错误时停止加载
    })
    .finally(() => {
      hub.loading = false
    })
}
</script>
<template>
  <HPage backgroundColor="#fff">
    <!-- 导航栏插槽 -->
    <template #nav-bar>
      <HNavBar left-text="返回" @click-left="handleBack">
        <template #right>
          <van-icon name="filter-o" size="24" @click="() => visibleChange()" />
        </template>
      </HNavBar>
      <!--      声音类型   咨询、抱怨-->
      <div class="flex-y-center pt-12 pl-16 pr-16 overflow-auto" style="gap: 8px">
        <div
          v-for="item in hub.voiceTypeTabs"
          :key="item.value"
          :class="{ 'voice-type-active': item.value === hub.currentVoiceType }"
          class="voice-type-item"
          @click="voiceTypeChange(item.value)"
        >
          <SvgIcon
            :name="item.icon"
            width="20px"
            height="20px"
            :color="item.value === hub.currentVoiceType ? '#fff' : '#5F6A7A'"
          ></SvgIcon>
          <div class="pt-4">{{ item.text }}</div>
        </div>
      </div>
      <div class="flex-y-center pt-12 pl-16 pr-16 scene-type-layout">
        <!--      场景类型   工单、咨询、主贴-->
        <div
          v-for="item in hub.sceneTypeTabs"
          :key="item.value"
          :class="{ 'scene-type-active': item.value === hub.currentScene }"
          class="scene-type-item"
          @click="sceneTypeChange(item.value)"
        >
          <div>{{ item.text }}</div>
        </div>
      </div>
    </template>
    <template #default>
      <van-pull-refresh v-model="hub.refreshing" @refresh="onRefresh" :disabled="hub.loading">
        <van-list
          v-model:loading="hub.loading"
          v-model:finished="hub.finished"
          :finished-text="isEmpty ? '' : '没有更多了'"
          @load="onLoad"
          :immediate-check="false"
        >
          <HVoiceList :voice-list="hub.list" />
          <!-- 无数据视图 -->
          <van-empty v-if="isEmpty" description="暂无声音数据" class="empty-container" />
        </van-list>
      </van-pull-refresh>
    </template>
  </HPage>
  <!-- 高级筛选 -->
  <AdvancedFilter v-model="hub.filterVisible" @confirm="handleAFConfirm"></AdvancedFilter>
</template>
<style lang="scss" scoped>
.voice-type-item {
  flex: 0 0 auto;
  min-width: 79px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  font-weight: 500;
  font-size: 14px;
  color: #5f6a7a;
  background: #eaf3ff;
  border-radius: 8px;
  border: 1px solid #eaf3ff;
}
.voice-type-active {
  background: #1677ff !important;
  color: #ffffff !important;
  font-weight: 600 !important;
  border: 1px solid #1677ff !important;
}
.scene-type-layout {
  overflow-x: auto;
  border-bottom: 1px solid #dfe2e8;
  gap: 15px;
}
.scene-type-item {
  flex: 0 0 auto;
  height: 36px;
  min-width: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;
}
.scene-type-active {
  font-weight: 500;
  font-size: 14px;
  color: #1677ff;
  border-bottom: 2px solid #1677ff;
}
.empty-container {
  padding: 60px 20px;
}
</style>
