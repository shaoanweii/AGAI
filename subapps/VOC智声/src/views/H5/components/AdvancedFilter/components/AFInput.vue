<script setup lang="ts">
defineOptions({
  name: 'AFInput'
})

const value = defineModel<any>()
</script>

<template>
  <input type="text" v-model="value" class="af-input" />
</template>

<style lang="scss" scoped>
.af-input {
  height: 32px;
  width: 100%;
  padding: 0 4px;
  border: 1px solid #999;
  border-radius: 8px;
}
</style>
