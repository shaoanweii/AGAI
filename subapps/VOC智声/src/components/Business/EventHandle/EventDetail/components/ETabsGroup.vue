<script setup lang="ts">
defineOptions({
  name: 'ETabsGroup'
})

interface option {
  label: string
  value: string
}

const active = defineModel<string>({ default: '' })

const { options, isShowBottomBorder = false } = defineProps<{
  options: option[]
  isShowBottomBorder?: boolean
}>()

const tabsChange = (item: option) => {
  active.value = item.value
}
</script>

<template>
  <div class="tab-wrapper">
    <div
      v-for="(item, index) in options"
      :key="index"
      :class="[
        'tab-item',
        active === item.value ? 'tap' : '',
        isShowBottomBorder ? 'show-bottom-border' : ''
      ]"
      @click="tabsChange(item)"
    >
      {{ item.label }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
.tab-wrapper {
  display: flex;
  background: #ffffff;

  .tab-item {
    padding: 10px 16px;
    font-weight: 600;
    font-size: 16px;
    color: #414651;
    line-height: 20px;
    border: 1px solid #ebedf0;
    border-bottom: none;
    overflow: hidden;
    cursor: pointer;
    &.show-bottom-border {
      border-bottom: 1px solid #ebedf0;
    }
    &:first-child {
      border-radius: 4px 0px 0px 0px;
    }
    &:last-child {
      border-radius: 0px 4px 0px 0px;
    }
    &:not(:first-child) {
      border-left: none;
    }
    &.tap {
      background: #1677ff;
      border: 1px solid #1677ff;
      color: #ffffff;
    }
  }
}
</style>
