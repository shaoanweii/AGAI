<script setup lang="ts">
import type { HomeMenuVo } from '@/api/overview/type'

defineOptions({
  name: 'GeneralScenario'
})

// 接收从父组件传递的数据和加载状态
interface Props {
  data?: HomeMenuVo[] | null
  loading?: boolean
}

const { data, loading = false } = defineProps<Props>()

// 定义 emit 事件
const emit = defineEmits<{
  itemClick: [item: HomeMenuVo]
}>()

const scenarioImageMap: Record<string, string> = {
  '/scene/groupAnalysis': '/demo-assets/scenes/scene-group.png',
  '/scene/thisProductAnalysis': '/demo-assets/scenes/scene-own-product.png',
  '/scene/competitorAnalysis': '/demo-assets/scenes/scene-competitor.png',
  '/scene/journeyAnalysis': '/demo-assets/scenes/scene-journey.png',
  '/scene/productAnalysis': '/demo-assets/scenes/scene-product.png',
  '/scene/serviceAnalysis': '/demo-assets/scenes/scene-service.png',
  '/scene/newCarLaunch': '/demo-assets/scenes/scene-new-car.png',
  '/scene/mainAccount': '/demo-assets/scenes/scene-key-account.png',
  '/scene/hotEvents': '/demo-assets/scenes/scene-hot-event.png'
}

/** 根据接口图片与路由映射返回场景视觉素材。 */
const resolveScenarioImage = (item: HomeMenuVo) =>
  item.smallImage || scenarioImageMap[item.htmlUri || ''] || scenarioImageMap['/scene/groupAnalysis']

// 处理子项点击事件
const handleItemClick = (item: HomeMenuVo) => {
  emit('itemClick', item)
}
</script>

<template>
  <div class="general-scenario">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 数据展示 -->
    <template v-else-if="data && data.length > 0">
      <div
        v-for="item in data"
        :key="item.name || item.htmlUri"
        class="gs-item"
        @click="handleItemClick(item)"
      >
        <div class="gs-icon">
          <img :src="resolveScenarioImage(item)" :alt="item.name" />
        </div>
        <div class="gs-info">{{ item.name || '通用场景' }}</div>
      </div>
    </template>

    <!-- 无数据状态 -->
    <div v-else class="no-data">
      <el-empty description="暂无数据" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.general-scenario {
  width: 100%;
  max-height: 100%;
  overflow: auto;
  display: grid;
  // grid-template-columns: repeat(3, minmax(174px, 1fr));
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;

  .loading-container {
    grid-column: 1 / -1;
    width: 100%;
    padding: 20px;
  }

  .gs-item {
    min-width: 0;
    height: 82px;
    background: linear-gradient(145deg, #f8fbff 0%, #e8f3ff 100%);
    border: 1px solid #d9e9ff;
    border-radius: 10px;
    padding: 9px 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(22, 119, 255, 0.12);
    }

    .gs-icon {
      width: 42px;
      height: 42px;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    .gs-info {
      font-weight: 500;
      font-size: 13px;
      color: #333333;
      line-height: 18px;
      margin-top: 3px;
      white-space: nowrap;
    }
  }

  .no-data {
    grid-column: 1 / -1;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100px;
  }
}
</style>
