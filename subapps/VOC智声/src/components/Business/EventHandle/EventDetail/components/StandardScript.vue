<script setup lang="ts">
// 关闭、驳回事件
defineOptions({
  name: 'StandardScript'
})

const visible = defineModel({ default: false })
const { tableData } = defineProps<{
  tableData?: any[]
}>()
const emits = defineEmits(['CopyAndApply'])

const handleCopyAndApply = (row: any) => {
  emits('CopyAndApply', row)
  visible.value = false
}
</script>

<template>
  <FDialog v-model:visible="visible" width="1000px" :showFooter="false" destoryOnClose>
    <template #header>
      <span>查看标准话术</span>
    </template>
    <div>
      <el-table
        :data="tableData"
        :border="false"
        size="large"
        :style="{ width: '100%' }"
        :maxHeight="400"
      >
        <el-table-column type="index" label="#" width="50" />
        <el-table-column
          prop="answer"
          label="标准话术"
          :show-overflow-tooltip="{ popperClass: 'text-tooltip-light' }"
        />
        <el-table-column label="操作" align="center" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleCopyAndApply(row)">复制并应用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </FDialog>
</template>

<style lang="scss" scoped></style>
