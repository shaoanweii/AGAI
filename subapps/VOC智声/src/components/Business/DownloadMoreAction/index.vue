<script setup lang="ts">
import { computed } from 'vue'
import { FunctionPermission } from '@/constants/btnPermMap'
import useUserStore from '@/store/modules/user'

defineOptions({ name: 'DownloadMoreAction' })

const props = withDefaults(
  defineProps<{
    showStat?: boolean
    showDetail?: boolean
    loading?: boolean
    disabled?: boolean
  }>(),
  {
    showStat: true,
    showDetail: true,
    loading: false,
    disabled: false
  }
)

const emit = defineEmits<{
  (e: 'download-stat'): void
  (e: 'download-detail'): void
}>()

const userStore = useUserStore()

/**
 * 结合父组件开关与功能权限，统一计算实际可展示的下载动作。
 */
const canShowStatDownload = computed(
  () => props.showStat && userStore.checkfunctionPermission(FunctionPermission.STATISTICAL_DOWNLOAD)
)
const canShowDetailDownload = computed(
  () =>
    props.showDetail && userStore.checkfunctionPermission(FunctionPermission.DETAILED_DATA_DOWNLOAD)
)
const hasVisibleDownloadAction = computed(
  () => canShowStatDownload.value || canShowDetailDownload.value
)

/**
 * 根据菜单命令分发下载动作。
 * 菜单组件只负责交互，不直接处理下载接口。
 */
const handleCommand = (command: string) => {
  if (command === 'stat') {
    emit('download-stat')
    return
  }

  if (command === 'detail') {
    emit('download-detail')
  }
}
</script>

<template>
  <el-dropdown
    v-if="hasVisibleDownloadAction"
    trigger="click"
    placement="bottom-end"
    :disabled="props.disabled || props.loading"
    @command="handleCommand"
  >
    <span class="download-more-action" @click.stop>
      <SvgIcon name="more" width="20px" height="20px" color="#8A94A6" />
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item v-if="canShowStatDownload" command="stat">下载统计数据</el-dropdown-item>
        <el-dropdown-item v-if="canShowDetailDownload" command="detail"
          >下载明细数据</el-dropdown-item
        >
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
.download-more-action {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;

  &:hover {
    background: #f2f4f7;
  }
}
</style>
