<script setup lang="ts">
defineOptions({
  name: 'EventPriorityTag'
})
const { tagName, type, tooltip } = defineProps<{
  tagName: string
  type: string
  tooltip?: string
}>()
</script>

<template>
  <div class="ep-tag-wrapper">
    <el-tooltip
      v-if="tooltip"
      :content="tooltip"
      placement="top"
      :show-after="300"
      popper-class="text-tooltip-light"
    >
      <div class="ep-tag" :class="type">
        <span>{{ tagName }}</span>
      </div>
    </el-tooltip>
    <div v-else class="ep-tag" :class="type">
      <span>{{ tagName }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.ep-tag-wrapper {
  display: inline-block;
}

.ep-tag {
  padding: 1px 8px;
  font-weight: 500;
  font-size: 14px;
  color: #ff7d00;
  line-height: 22px;
  background: #fff7e8;
  border-radius: 4px;
  display: inline-block;
  min-width: 34px;
  text-align: center;

  &.p0,
  &.S {
    color: #f53f3f;
    background: #ffece8;
  }
  &.p1,
  &.A {
    color: #ff7d00;
    background: #fff7e8;
  }
  &.p2,
  &.B {
    color: #fab007;
    background: #fefae0;
  }
  &.p3,
  &.C {
    color: #165dff;
    background: #e8f3ff;
  }
  &.p4 {
    color: #0fc6c2;
    background: #e8fffb;
  }
}
</style>
