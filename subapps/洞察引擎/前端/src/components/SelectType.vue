<template>
  <div class="select-main" :data-testid="`${testid}select-type-10001`">
    <div
      v-for="(item, index) of data"
      :key="item.key"
      :class="{ 'select-item': true, tap: active === item.key, disabled: disabled }"
      :data-testid="`${testid}select-type-10001-${index}`"
      @click="change(item)"
    >
      {{ item.value }}
    </div>
  </div>
</template>

<script lang="ts" setup>
const emits = defineEmits(['change'])
const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  defaultActice: {
    type: String,
    default: ''
  },
  testid: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const data = computed(() => {
  return props.data
})
// 示例数据结构
// const data = <any>ref([
//   {
//     key: '末级标签',
//     value: 1,
//   },
//   {
//     key: '分类',
//     value: 2,
//   },
// ])
const active = defineModel()
onMounted(() => {
  active.value = props.defaultActice || (data.value && data.value[0]?.key)
})
const change = (item: any) => {
  if (props.disabled) return
  active.value = item.key
  emits('change', item)
}
</script>

<style lang="scss" scoped>
.select-main {
  display: flex;
  flex-wrap: wrap;
  .select-item {
    padding: 5px 16px;
    box-sizing: border-box;
    font-size: 14px;
    color: #4e5969;
    line-height: 22px;
    text-align: center;
    cursor: pointer;
    & + .select-item {
      margin-left: 12px;
    }
    &.tap {
      background: #f2f3f5;
      border-radius: 100px 100px 100px 100px;
      font-weight: 500;
      color: #165dff;
    }

    &.disabled {
      color: #86909c;
      cursor: not-allowed;
    }
  }
}
</style>
