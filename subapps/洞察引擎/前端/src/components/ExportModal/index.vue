<template>
  <!--
    -->
  <el-dialog v-model="visible" @close="handleClose">
    <template #header> 选择导出范围 </template>
    <el-form ref="formRef" :model="filter" :rules="rules">
      <el-form-item prop="times" label="时间范围">
        <el-date-picker
          v-model="filter.times"
          type="month"
          format="YYYY年MM月"
          :disabled-date="disabledDate"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item prop="channelIdList" label="数据渠道">
        <!-- <el-cascader
          v-model="filter.channelIdList"
          :options="transitionChannelOptions"
          placeholder="请选择"
          :props="{ value: 'code', label: 'name', children: 'child' }"
        /> -->
        <el-select v-model="filter.channelIdList" placeholder="请选择">
          <el-option
            v-for="(item, index) of transitionChannelOptions"
            :key="index"
            :value="item.code"
          >
            {{ item.name }}
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleOk">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { useAppStore } from '@/stores'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { debounce } from 'lodash-es'

const { channelOptions } = defineProps<{
  channelOptions: any[]
  otherConditions: any
}>()
const visible = defineModel<boolean>('visible', { default: false })
const emits = defineEmits(['ok'])
const appStore = useAppStore()

const formRef = ref<any>(null)

const rules = {
  times: [{ required: true, message: '请选择时间范围', trigger: 'change' }],
  channelIdList: [{ required: true, message: '请选择数据渠道', trigger: 'change' }]
}

const filter = ref({
  times: undefined,
  channelIdList: undefined
})

const transitionChannelOptions = computed(() => {
  return channelOptions?.filter(el => el.code !== 'all')
})

/**
 * 禁用当前月份之后的所有月份
 * @param current
 */
const disabledDate = (current: any) => {
  const now = dayjs().format('YYYY-MM')
  const currentDate = dayjs(current).format('YYYY-MM')
  if (dayjs(now).isBefore(currentDate)) {
    return true
  }
  return false
  // if (otherConditions?.startTime && otherConditions?.endTime) {
  //   if (otherConditions?.startTime === otherConditions?.endTime) {
  //     return (
  //       dayjs(otherConditions?.startTime).format('YYYY-MM') !== dayjs(current).format('YYYY-MM')
  //     )
  //   } else {
  //     return (
  //       dayjs(current).isBefore(dayjs(otherConditions?.startTime).format('YYYY-MM')) ||
  //       dayjs(current).isAfter(dayjs(otherConditions?.endTime).format('YYYY-MM'))
  //     )
  //   }
  // } else {
  //   return false
  // }
}

const handleOk = debounce(() => {
  if (appStore.isDownloadFlag) {
    ElMessage.error('系统尚未完成上一次导出的文件，请稍后再试')
    return
  }

  formRef.value?.validate(async (errs: any) => {
    if (!errs) {
      emits('ok', {
        startTime: dayjs(filter.value.times).startOf('month').format('YYYY-MM-DD'),
        endTime: dayjs(filter.value.times).endOf('month').format('YYYY-MM-DD'),
        channelIdList: filter.value.channelIdList ? [filter.value.channelIdList] : ''
      })
    }
  })
}, 300)

const handleCancel = () => {
  visible.value = false
}

const handleClose = () => {
  filter.value.times = undefined
  filter.value.channelIdList = undefined
}
</script>

<style lang="scss" scoped></style>
