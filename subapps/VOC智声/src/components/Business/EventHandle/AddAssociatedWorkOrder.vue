<script setup lang="ts">
import { ref } from 'vue'

// 添加关联工单
defineOptions({
  name: 'AddAssociatedWorkOrder'
})

const visible = defineModel({ default: false })
// const {} = defineProps<{}>()

const formData = ref({
  remark: '',
  close: '1',
  reject: '1'
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const tableData = ref([])

const handleConfirm = () => {
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}
</script>

<template>
  <FDialog v-model:visible="visible" width="1000px" @confirm="handleConfirm" @cancel="handleCancel">
    <template #header>
      <span>添加关联工单</span>
    </template>
    <div>
      <el-form :model="formData" inline ref="formDataRef" @submit.prevent>
        <el-form-item label="工单号" prop="">
          <el-input v-model.trim="formData.remark" clearable placeholder=""></el-input>
        </el-form-item>
        <el-form-item label="客户号码" prop="">
          <el-input v-model.trim="formData.remark" clearable placeholder=""></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">
            <el-icon style="vertical-align: middle" class="mr-10">
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button color="#F2F3F5">
            <el-icon style="vertical-align: middle" class="mr-10">
              <RefreshRight />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" :border="false" size="large" style="width: 100%" height="40vh">
        <el-table-column type="selection" width="55" />
        <el-table-column label="工单号" prop="order" />
        <el-table-column label="客户" prop="order" />
        <el-table-column label="客户号码" prop="order" />
        <el-table-column label="处理优先级" prop="order" />
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="->, total, sizes, prev, pager, next, jumper"
        :total="total"
        class="mt-16"
      />
    </div>
  </FDialog>
</template>

<style lang="scss" scoped></style>
