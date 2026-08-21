<script setup lang="ts">
import { reactive, ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import ResultList, { type ResultRow } from './ResultList.vue'
import RuleDetail from './RuleDetail.vue'
import RuleForm from '@/views/rules/closedLoopRules/SingleEvent/RuleForm.vue'

const props = defineProps<{
  testId?: string
}>()

const visible = defineModel<boolean>('visible', { default: false })

// 视图状态与选中行
const isDetail = ref(false)
const selectedRow = ref<ResultRow | null>(null)

// 监听弹框显示，重置状态和滚动位置
watch(visible, newVal => {
  if (newVal) {
    isDetail.value = false
    selectedRow.value = null
  }
})

// 查看规则 -> 进入详情
const handleView = (row: ResultRow) => {
  selectedRow.value = row
  isDetail.value = true
}

// 返回列表
const handleBack = () => {
  isDetail.value = false
}

// 确定
const onConfirm = ({ close }: { close: () => void }) => {
  close()
}
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    :title="isDetail ? '规则详情' : '查看结果'"
    width="800px"
    body-class="rule-view-dialog-body"
    style="display: flex; flex-direction: column; height: 880px"
    :confirm="onConfirm"
  >
    <!-- 自定义头部：详情态显示返回箭头 -->
    <template #header>
      <div class="dialog-header">
        <span v-if="isDetail" class="back" @click="handleBack">
          <el-icon class="mr8"><ArrowLeft /></el-icon>
        </span>
        <span>{{ isDetail ? '规则详情' : '查看结果' }}</span>
      </div>
    </template>

    <!-- 主体：列表/详情切换 -->
    <div class="h-full overflow-auto" v-if="visible">
      <ResultList v-show="!isDetail" :test-id="props.testId" @view="handleView" />
      <!-- <RuleDetail v-show="isDetail" :row="selectedRow" /> -->
      <!-- 将所有入参透传给 RuleForm，并传入当前可见状态 -->
      <RuleForm
        v-show="isDetail"
        :visible="isDetail"
        :disabled="isDetail"
        :ruleData="selectedRow"
      />
    </div>
  </AppDialog>
</template>
<style lang="scss">
.rule-view-dialog-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
<style scoped lang="scss">
.dialog-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
}
.back {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  margin-right: 8px;
}
.mr8 {
  margin-right: 8px;
}
</style>
