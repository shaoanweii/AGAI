<template>
  <el-dialog
    v-model="visible"
    :width="362"
    :show-close="false"
    class="modal-content"
    @open="open"
    @close="close"
  >
    <div class="title">
      <img
        v-if="modelType === 2"
        src="../../../../../assets/svg/exclamation-circle-fill-1.svg"
        :style="{ width: '20px' }"
        alt=""
      />
      <img
        v-if="[3, 4].includes(modelType!)"
        src="../../../../../assets/svg/exclamation-circle-fill.svg"
        :style="{ width: '20px' }"
        alt=""
      />
      <span class="ml-8">{{ titleStr }}</span>
    </div>
    <div class="tipStr">{{ tipStr }}</div>

    <template #footer>
      <div>
        <el-button style="width: 88px" @click="handleCancel">取消</el-button>
        <el-button style="min-width: 88px" class="ml-16" type="primary" @click="handleOk">{{
          okText
        }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
const visible = defineModel({ default: false })

interface Props {
  /**
   * 1 编辑
   * 2 删除
   * 3 移动
   * 4 同步
   */
  modelType: number | undefined
}
const { modelType } = defineProps<Props>()

const emits = defineEmits(['ok', 'cancel'])

const knowledgeBaseId = ref()

const handleOk = () => {
  emits('ok', modelType, knowledgeBaseId.value)
  visible.value = false
}
const handleCancel = () => {
  emits('cancel', modelType)
  visible.value = false
}

const okText = computed(() => {
  return '确定'
})

const titleStr = computed(() => {
  if (modelType === 3) {
    return '操作提示'
  }
  if (modelType === 4) {
    return '操作提示'
  }
  if (modelType === 5) {
    return '操作提示'
  }
  return '操作提示'
})
const tipStr = computed(() => {
  if (modelType === 3) {
    return '请确定是否将已选中标签批量启用'
  }
  if (modelType === 4) {
    return '请确定是否将已选中标签批量禁用'
  }
  if (modelType === 5) {
    return '是否将已选中标签批量删除'
  }
})

const open = async () => {}

const close = () => {
  knowledgeBaseId.value = ''
}
</script>

<style lang="scss">
.modal-content {
  .el-dialog__header {
    display: none;
    margin: 0;
    padding: 0;
  }
  .title {
    margin: 16px 0 24px;
    font-weight: 600;
    font-size: 16px;
    color: #1d2129;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .el-dialog-footer {
    text-align: center;
  }
  .tipStr {
    box-sizing: border-box;
    padding: 0 21px;
    font-weight: 400;
    font-size: 14px;
    color: #1d2129;
    line-height: 22px;
    text-align: center;
  }
}
</style>
