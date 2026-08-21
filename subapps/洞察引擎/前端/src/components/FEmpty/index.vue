<template>
  <div class="no-data">
    <img src="@/assets/imgs/nodata.png" alt="" />
    <slot>
      <span>{{ tipStr }}</span>
    </slot>
  </div>
</template>
<script lang="ts" setup>
const { tipStr = '暂无数据' } = defineProps<{
  tipStr?: string
}>()
</script>
<style lang="scss" scoped>
.no-data {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  /* min-height: 250px;
  max-height: 500px; */
  img {
    width: 60%;
    max-width: 300px;
  }
  span {
    color: #98a2b2;
    font-size: 16px;
    margin-top: 16px;
  }
}
</style>
