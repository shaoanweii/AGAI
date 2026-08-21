<script setup lang="ts">
import { computed } from 'vue'
import type { SingleEventDetailVo } from '@/api/singlePointEvent/types'

// 事件编号
defineOptions({
  name: 'EventNumber'
})

const { eventInfo } = defineProps<{
  eventInfo: SingleEventDetailVo
}>()

// 计算标签，智能过滤空值
const domTagsText = computed(() => {
  if (!eventInfo) return ''

  // 创建标签数组，过滤掉空值
  const tags = [
    eventInfo.domTagFirst,
    eventInfo.domTagSecond,
    eventInfo.domTagThree,
    eventInfo.domTagFour
  ].filter(tag => tag != null && tag !== '')

  // 如果只有一个标签，直接返回
  if (tags.length === 0) return ''
  if (tags.length === 1) return tags[0]

  // 多个标签用#连接
  return tags.join('#')
})
</script>

<template>
  <div class="event-number">
    <el-row :gutter="24">
      <el-col :span="10">
        <div class="en-item">
          <div class="eni-laebl">事件信息</div>
          <div class="eni-value">{{ eventInfo?.eventName }}</div>
        </div>
      </el-col>
      <el-col :span="3">
        <div class="en-item">
          <div class="eni-laebl">主题分类</div>
          <div class="eni-value">{{ eventInfo?.subjectCategoryName }}</div>
        </div>
      </el-col>
      <el-col :span="3">
        <div class="en-item">
          <div class="eni-laebl">主责部门</div>
          <div class="eni-value">{{ eventInfo?.mainRespOrgName }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="en-item">
          <div class="eni-laebl">触发时间</div>
          <div class="eni-value">{{ eventInfo?.warningTime }}</div>
        </div>
      </el-col>
      <el-col :span="2">
        <div class="en-item">
          <div class="eni-laebl">是否需回复</div>
          <div class="eni-value">{{ eventInfo?.isNeedReply ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="2">
        <div class="en-item">
          <div class="eni-laebl">是否需闭环</div>
          <div class="eni-value">{{ eventInfo?.isNeedClosedLoop ?? '-' }}</div>
        </div>
      </el-col>
    </el-row>
    <!-- 第二行 -->
    <el-row :gutter="24" class="mt-16">
      <el-col :span="10">
        <div class="en-item">
          <div class="eni-laebl">声音片段</div>
          <div class="eni-value">{{ eventInfo?.originalTextScene ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="3">
        <div class="en-item">
          <div class="eni-laebl">用户意图</div>
          <div class="eni-value">{{ eventInfo?.intentionType ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="7">
        <div class="en-item">
          <div class="eni-laebl">体验代码</div>
          <div class="eni-value">{{ domTagsText || '-' }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="en-item">
          <div class="eni-laebl">标准观点</div>
          <div class="eni-value">{{ eventInfo?.topic || '-' }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.event-number {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  margin-top: 16px;

  .en-item {
    .eni-laebl {
      font-weight: 400;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.4);
      line-height: 22px;
    }
    .eni-value {
      font-weight: 400;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.9);
      line-height: 22px;
      margin-top: 8px;
    }
  }
}
</style>
