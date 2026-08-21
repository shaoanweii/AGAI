<template>
  <div class="button-group">
    <button
      v-for="item in options"
      :key="item.value"
      :class="['button', { selected: modelValue === item.value }]"
      @click="onSelect(item.value)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Option {
  label: string
  value: string
}

const props = defineProps<{
  options: Option[]
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
}>()

const lastClickedValue = ref<string>('')

function onSelect(val: string) {
  if (val !== props.modelValue && val !== lastClickedValue.value) {
    lastClickedValue.value = val
    emit('update:modelValue', val)
    emit('change', val)
  }
}
</script>

<style scoped>
.button-group {
  display: flex;
  align-items: center;
}
.button {
  background: #f9f9f9;
  border: 1.05px solid #dadada;
  border-radius: 4.2px 4.2px 0px 0px;
  color: rgba(0, 0, 0, 0.6);
  font-weight: 400;
  text-align: center;
  display: inline-block;
  min-width: 100px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  position: relative;
  padding: 8px 0;
  margin-right: 10px;
}
.button:last-child {
  margin-right: 0;
}
.button.selected {
  background: #fff;
  color: #0077ff;
}
.button.selected::after {
  content: '';
  display: block;
  position: absolute;
  width: 100%;
  height: 2px;
  background: #0077ff;
  left: 0;
  bottom: 0;
  border-radius: 4px;
}
</style>
