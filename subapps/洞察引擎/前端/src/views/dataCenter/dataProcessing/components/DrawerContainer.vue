<template>
  <el-drawer
    class="result-table"
    :size="1200"
    v-model="visible"
    :show-close="true"
    @close="handleCancel"
    destroy-on-close
  >
    <template #header>
      <span class="ml-8">{{ titleStr }}</span>
    </template>
    <slot></slot>
  </el-drawer>
</template>
<script setup lang="ts">
defineProps({
  titleStr: {
    type: String,
    required: true,
    default: ''
  },
  footer: {
    type: Boolean,
    required: false,
    default: false
  }
})
const visible = defineModel({ required: true, default: false })
const emits = defineEmits(['cancel'])
const handleCancel = () => {
  visible.value = false
  emits('cancel')
}
</script>

<style lang="scss"></style>
