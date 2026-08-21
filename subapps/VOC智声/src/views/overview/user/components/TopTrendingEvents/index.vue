<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({
  name: 'TopTrendingEvents'
})

// 定义数据项类型
interface TTEItemInfo {
  text: string
  index: number
  num: number
  rate: number
}

const selectedIndex = ref(0)
const router = useRouter()

const data: TTEItemInfo[] = [
  {
    index: 0,
    text: '智能座舱升级稳定性',
    num: 3200,
    rate: 13.22
  },
  { index: 1, text: '售后响应时效', num: 2800, rate: 18.33 },
  { index: 2, text: '语音交互连续性', num: 2200, rate: 12.89 },
  { index: 3, text: '新车交付服务体验', num: 1700, rate: 8.55 },
  { index: 4, text: '远程升级说明清晰度', num: 1200, rate: 6.76 }
]

const handleItemClick = (index: number) => {
  selectedIndex.value = selectedIndex.value === index ? -1 : index
  router.push('/scene/hotEvents')
}
</script>

<template>
  <div class="top-trending-events">
    <div
      v-for="(item, index) of data"
      :key="index"
      class="tte-item"
      :class="{ active: index === selectedIndex }"
      @click="handleItemClick(index)"
    >
      <div class="logo ps-relative flex-center" :class="{ hot: item.index < 3 }">
        <!-- <div class="position-center">{{ item.index + 1 }}</div> -->
        <SortNum :rank="item.index + 1"></SortNum>
        <!-- <SvgIcon v-if="item.index < 3" name="hot_rank" width="20px" height="20px"></SvgIcon>
        <SvgIcon v-else name="o_rank" width="20px" height="20px"></SvgIcon> -->
      </div>
      <div class="info">
        <div class="text-content">
          <span>{{ item.text }}</span>
          <el-tag v-if="item.index === 0" type="danger" class="ml-8">飙升</el-tag>
          <el-tag v-if="item.index === 2" type="danger" class="ml-8">
            <div class="flex-x-center">
              <SvgIcon name="meteor-fill" width="12px" height="12px" />
              <span class="ml-3">S级</span>
            </div>
          </el-tag>
        </div>
        <div class="text-body mr-16">{{ item.num }}</div>
        <div class="rate-column text-tertiary text-body">+{{ item.rate }}%</div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.top-trending-events {
  width: 100%;
  max-height: 100%;
  overflow: auto;
}

.tte-item {
  width: 100%;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 0 16px;
  min-height: 40px;
  display: flex;
  align-items: center;
  cursor: pointer;

  &.active {
    background: rgba(102, 130, 164, 0.05);

    .info {
      font-weight: 500;
    }
  }

  & + .tte-item {
    margin-top: 16px;
  }

  .logo {
    width: 20px;
    height: 20px;
    font-weight: bold;
    font-size: 14px;
    color: #5c7092;
    &.hot {
      color: #e5484d;
    }
  }

  .info {
    margin-left: 10px;
    font-weight: 400;
    font-size: 16px;
    color: #333333;
    line-height: 20px;
    flex: 1;
    width: 100%;
    display: flex;
    align-items: center;
  }

  .text-content {
    flex: 1;
    display: flex;
    align-items: center;
    min-width: 0; // 允许文字截断
  }

  .rate-column {
    min-width: 72px;
    text-align: right;
    flex-shrink: 0;
    text-align: center;
  }

  .btn {
    font-weight: 500;
    font-size: 14px;
    color: #1677ff;
    line-height: 18px;
  }
}
</style>
