<script setup lang="ts">
import { computed } from 'vue'
import type { UserFocusSceneTopVo } from '@/api/journeyAnalysis/types'
import { fmtNum, fmtFix } from '@/utils'
import HoverPopover from '@/components/Business/Scene/Common/HoverPopover.vue'

defineOptions({
  name: 'FocusScene'
})

// 场景标签数据接口
interface SceneTag {
  /** 标签文本 */
  text: string
  /** 左边距 */
  left: string
  /** 上边距 */
  top: string
}

const { data } = defineProps<{
  data: UserFocusSceneTopVo[]
}>()

// 固定的标签位置数组
const tagPositions = [
  { left: '25%', top: '15%' }, // 左上
  { left: '50%', top: '8%' }, // 上方
  { left: '75%', top: '20%' }, // 右上
  { left: '85%', top: '40%' }, // 右方
  { left: '80%', top: '65%' }, // 右下
  { left: '75%', top: '85%' }, // 右下方
  { left: '50%', top: '92%' }, // 下方
  { left: '25%', top: '85%' }, // 左下
  { left: '15%', top: '70%' }, // 左方
  { left: '20%', top: '45%' } // 左方
]

// 事件定义
const emit = defineEmits<{
  (e: 'tag-click', data: UserFocusSceneTopVo): void
}>()

// 处理标签点击事件
const handleTagClick = (item: any) => {
  emit('tag-click', item)
}

// 合并文本和位置数据
const sceneTags = computed<any[]>(() => {
  return data.map((item, index) => ({
    ...item,
    text: item.sceneName || '',
    left: tagPositions[index]?.left || '50%',
    top: tagPositions[index]?.top || '50%'
  }))
})
</script>

<template>
  <div class="focus-scene">
    <!-- 中心汽车图标 -->
    <div class="center-icon">
      <div class="icon-bg">
        <SvgIcon name="car_group" width="212px" height="211px" />
      </div>
    </div>

    <!-- 周围的场景标签 -->
    <HoverPopover
      v-for="(tag, index) in sceneTags"
      :key="index"
      placement="top"
      :show-after="200"
      :width="360"
      trigger="hover"
      :table-config="{
        title: tag.sceneName,
        data: [
          {
            ...tag,
            name: '用户数',
            value: fmtNum(tag.value),
            valueMoM: fmtFix(tag.valueMoM),
            valueYoY: fmtFix(tag.valueYoY)
          }
        ],
        columns: [
          { title: '名称', dataIndex: 'name', width: 70 },
          { title: '数值', dataIndex: 'value', width: 90 },
          { title: '环比', dataIndex: 'valueMoM', width: 90, className: 'c666' },
          { title: '同比', dataIndex: 'valueYoY', width: 90, className: 'c666' }
        ]
      }"
    >
      <template #reference>
        <div
          class="scene-tag"
          :style="{ left: tag.left, top: tag.top }"
          @click="handleTagClick(tag)"
        >
          {{ tag.text }}
        </div>
      </template>
    </HoverPopover>
  </div>
</template>

<style lang="scss" scoped>
.focus-scene {
  position: relative;
  width: 100%;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;

  .center-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10;

    .icon-bg {
      // width: 240px;
      // height: 240px;
      // background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      // border-radius: 50%;
      // display: flex;
      // align-items: center;
      // justify-content: center;
      // box-shadow: 0 4px 12px rgba(33, 150, 243, 0.2);
      // border: 3px solid #ffffff;
    }
  }

  .scene-tag {
    position: absolute;
    padding: 4px 16px;
    background: #f2f4f7;
    border-radius: 16px;
    font-weight: 600;
    font-size: 16px;
    color: #5f6a7a;
    line-height: 24px;
    white-space: nowrap;
    cursor: pointer;
    transition: all 0.3s ease;
    z-index: 5;
    transform: translate(-50%, -50%);

    // &:hover {
    //   background: #f8f9fa;
    //   border-color: #2196f3;
    //   color: #2196f3;
    //   transform: translate(-50%, -50%) scale(1.05);
    //   box-shadow: 0 4px 12px rgba(33, 150, 243, 0.15);
    // }
  }
}
</style>
