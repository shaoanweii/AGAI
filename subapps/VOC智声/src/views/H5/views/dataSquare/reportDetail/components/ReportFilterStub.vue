<script setup lang="ts">
defineOptions({
  name: 'ReportFilterStub'
})

defineProps<{
  loading?: boolean
}>()
</script>

<template>
  <div class="report-filter-stub" :class="{ 'is-loading': loading }"></div>
</template>

<style scoped lang="scss">
.report-filter-stub {
  height: 0;
  overflow: hidden;
}
</style>
