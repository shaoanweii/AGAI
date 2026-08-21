<script setup lang="ts">
import { auditLabelCorrection } from '@/api/review'
import { reactive, watch } from 'vue'

defineOptions({
  name: 'BatchReview'
})

const visible = defineModel<boolean>('visible', {
  default: false
})

interface Props {
  selectedData: any[]
}

const props = withDefaults(defineProps<Props>(), {
  selectedData: () => []
})

type ErrorType = '1' | '2'

const switchErrorType = (type: ErrorType) => {
  form.auditStatus = type
}

interface Emits {
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

const form = reactive({
  auditStatus: '1'
})

const resetForm = () => {
  form.auditStatus = '1'
}

watch(
  () => visible.value,
  val => {
    if (!val) return
    resetForm()
  }
)

const handleConfirm = async ({ close }: { close: () => void }) => {
  const params = {
    auditStatus: form.auditStatus,
    idList: props.selectedData?.map((item: any) => item.id)
  }
  const res = (await auditLabelCorrection(params)) as any
  if (res.success) {
    emit('success')
    close()
  } else {
    ElMessage.warning(res.message)
  }
}

const handleCancel = () => {
  resetForm()
}
</script>

<template>
  <AppDialog
    v-model:visible="visible"
    :title="'批量审核'"
    width="400px"
    @cancel="handleCancel"
    :confirm="handleConfirm"
  >
    <div class="corpus-create-dialog">
      <el-form ref="formRef" :model="form" label-width="80px" class="corpus-create-dialog__form">
        <el-form-item :label="'审核条数'" prop="subject">
          <div>{{ selectedData?.length }}条</div>
        </el-form-item>
        <el-form-item label="审核方式" prop="description">
          <div class="custome-switch-btn-wrap">
            <!--  // 1通过， 2拒绝， 3撤销 -->
            <div
              class="csb-item"
              :class="{ 'csb-tap': form.auditStatus === '1' }"
              @click="switchErrorType('1')"
            >
              通过
            </div>
            <div
              class="csb-item"
              :class="{ 'csb-tap': form.auditStatus === '2' }"
              @click="switchErrorType('2')"
            >
              拒绝
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </AppDialog>
</template>

<style lang="scss" scoped>
.corpus-create-dialog {
  padding-top: 8px;

  .custome-switch-btn-wrap {
    display: flex;
    align-items: center;
    gap: 8px;
    .csb-item {
      padding: 6px 14px;
      font-weight: 500;
      font-size: 14px;
      color: #535862;
      line-height: 20px;
      border-radius: 4px 4px 4px 4px;
      border: 1px solid #dfe2e8;
      cursor: pointer;
      &.csb-tap {
        border: 1px solid #1677ff;
        color: #1677ff;
      }
    }
  }
}

.corpus-create-dialog__form {
  width: 560px;
  margin: 0 auto;
}
</style>
