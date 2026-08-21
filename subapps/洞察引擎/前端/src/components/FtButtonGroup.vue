<script setup lang="ts">
interface Group {
  label: string
  value: string | number
}

const emit = defineEmits(['change'])
const props = withDefaults(
  defineProps<{
    group: Group[]
    disabled?: boolean
    testid?: string
  }>(),
  {}
)
const { group, disabled, testid } = toRefs(props)
const cur = defineModel()

const change = (item: Group) => {
  if (disabled.value) return
  cur.value = item.value
  emit('change', item)
}
</script>

<template>
  <div class="flex flex-wrap">
    <div
      v-for="(item, index) of group"
      :key="index"
      :class="['button-group', cur === item.value ? 'primary' : '', disabled ? 'disabled' : '']"
      :data-testid="`${testid}-btn-group${index}`"
      @click="change(item)"
    >
      {{ item.label }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.button-group {
  background: var(--bgc-def);
  border-radius: 2px 2px 0px 0px;
  border: 1px solid var(--border-color);
  font-size: 14px;
  color: var(--color-medium);
  line-height: 22px;
  text-align: center;
  padding: 8px 16px;
  box-sizing: border-box;
  cursor: pointer;
  transition: all 0.2s ease;

  & + .button-group {
    margin-left: 8px;
  }

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &.primary {
    background: var(--color-primary);
    border: 1px solid var(--color-primary);
    font-weight: 600;
    color: var(--color-white);

    &.disabled {
      color: var(--color-white);
      background-color: var(--color-primary-light);
    }
  }

  &.disabled {
    cursor: not-allowed;
    color: var(--color-low);
    background-color: var(--bgc-def);
    border-radius: inherit;

    &:hover {
      color: var(--color-low);
      border-color: var(--border-color);
    }
  }
}
</style>
