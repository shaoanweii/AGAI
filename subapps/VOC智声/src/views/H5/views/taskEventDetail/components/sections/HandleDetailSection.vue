<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type {
  MobileSingleEventDetailVo,
  SingleEventPrivateMsgModel,
  SingleEventUserModel,
  SingleEventWorkOrderModel
} from '@h5/api/taskEvent'
import { HandleModeEnum } from '@/views/H5/constants/index'

defineOptions({
  name: 'H5HandleDetailSection'
})

interface HandleDetailSectionProps {
  data: MobileSingleEventDetailVo | null
  /** 父组件折叠卡展开状态（v-model:expanded） */
  expanded?: boolean
}

const props = defineProps<HandleDetailSectionProps>()
const emit = defineEmits<{
  /** 同步父组件折叠卡展开状态 */
  (e: 'update:expanded', value: boolean): void
}>()

const hasText = (value: unknown): boolean => {
  if (value === undefined || value === null) return false
  return String(value).trim().length > 0
}

const formatText = (value: unknown, fallback = '-'): string => {
  return hasText(value) ? String(value).trim() : fallback
}

const formatUserText = (user?: SingleEventUserModel): string => {
  if (!user) return '-'
  const name = formatText(user.userName, '')
  const empNo = formatText(user.userEmpNo, '')
  const text = [name, empNo ? `(${empNo})` : ''].filter(Boolean).join(' ')
  return formatText(text)
}

const processModeText = computed(() => {
  const d = props.data || {}
  // 后端字段命名为 unprocessedReason，但在“是否处理=是”的场景下也可能承载“处理方式/处理原因”的展示文本
  return formatText(d.unprocessedReasonName)
})

const showPrivateMsgCard = computed(() => {
  const d = props.data || {}
  const reason = String(d.unprocessedReason || '') as HandleModeEnum
  return !!reason && [HandleModeEnum.OnlyPrivateMsg, HandleModeEnum.ReplyAndPrivateMsg].includes(reason)
})

const showReviewCard = computed(() => {
  const d = props.data || {}
  const reason = String(d.unprocessedReason || '') as HandleModeEnum
  return !!reason && [HandleModeEnum.OnlyReply, HandleModeEnum.ReplyAndPrivateMsg].includes(reason)
})

const showWorkOrderCard = computed(() => {
  const d = props.data || {}
  const reason = String(d.unprocessedReason || '') as HandleModeEnum
  return !!reason && [HandleModeEnum.OnlyPrivateMsg, HandleModeEnum.ReplyAndPrivateMsg].includes(reason)
})

const hasHandleUser = computed(() => {
  const user = props.data?.handleUser
  if (!user) return false
  return hasText(user.userName) || hasText(user.userEmpNo)
})

const hasHandleBaseInfo = computed(() => {
  const d = props.data
  if (!d) return false

  return (
    hasText(d.isProcessedName) ||
    hasText(processModeText.value) ||
    hasHandleUser.value ||
    hasText(d.processDescription)
  )
})

// 是否存在“处理信息”：用于控制父级「处理详情」卡片默认展开/收起
const hasHandleInfo = computed(() => {
  return (
    showReviewCard.value ||
    showWorkOrderCard.value ||
    showPrivateMsgCard.value ||
    hasHandleBaseInfo.value
  )
})

// 仅在“数据变更后的首次判断”同步默认展开态，避免用户手动展开/收起后被逻辑覆盖
const allowAutoSyncExpanded = ref(true)

watch(
  () => props.data,
  () => {
    allowAutoSyncExpanded.value = true
  },
  { flush: 'sync' }
)

watch(
  hasHandleInfo,
  value => {
    if (!props.data) return
    if (!allowAutoSyncExpanded.value) return
    emit('update:expanded', value)
    allowAutoSyncExpanded.value = false
  },
  { immediate: true, flush: 'sync' }
)

const workOrders = computed<SingleEventWorkOrderModel[]>(() => {
  const list = props.data?.relatedWorkOrderNos || []
  return Array.isArray(list) ? list.filter(Boolean) : []
})

const privateMsgDetails = computed<SingleEventPrivateMsgModel[]>(() => {
  const list = props.data?.privateMsgDetails || []
  return Array.isArray(list) ? list.filter(Boolean) : []
})

const isSuccessProgress = (code: any): boolean => {
  // 设计稿为绿色完成态
  return code === 'finished'
}

const workOrderStatusClass = (statusText: string): string => {
  if (!hasText(statusText)) return 'status--unknown'
  const s = String(statusText)
  if (s.includes('完成') || s.includes('已完成') || s.includes('关闭')) return 'status--done'
  if (s.includes('进行') || s.includes('处理中') || s.includes('处理中')) return 'status--doing'
  return 'status--unknown'
}
</script>

<template>
  <div class="handle-detail">
    <div v-if="!props.data" class="empty-text">
      暂无处理详情
    </div>

    <template v-else>
      <div class="info-title">处理方式</div>
      <div class="info-list content-layout">
        <div class="info-row">
          <div class="label">是否处理</div>
          <div class="value">{{ formatText(props.data.isProcessedName) }}</div>
        </div>
        <div class="info-row">
          <div class="label">处理方式</div>
          <div class="value">{{ processModeText }}</div>
        </div>
        <div class="info-row">
          <div class="label">处理人员</div>
          <div class="value">{{ formatUserText(props.data.handleUser) }}</div>
        </div>
        <div class="info-row">
          <div class="label">添加说明</div>
          <div class="value value-multiline">{{ formatText(props.data.processDescription) }}</div>
        </div>
      </div>

      <div v-if="showReviewCard" class="mt-12">
        <div class="info-title">回评内容</div>
        <div class="info-list content-layout">
          <div class="info-row">
            <div class="label">回评进度</div>
            <div class="value">
              <span class="progress-text"
                    :class="{ 'is-success': isSuccessProgress(props.data.reviewProgressCode) }">
                <van-icon
                  v-if="isSuccessProgress(props.data.reviewProgressCode)"
                  name="checked"
                  size="14"
                  color="#00B578"
                />
                <span class="progress-gap">{{ formatText(props.data.reviewProgressName) }}</span>
              </span>
            </div>
          </div>
          <div class="info-row">
            <div class="label">回评人员</div>
            <div class="value">{{ formatUserText(props.data.reviewHandler) }}</div>
          </div>
          <div class="info-row">
            <div class="label">回评时间</div>
            <div class="value">{{ formatText(props.data.reviewDate) }}</div>
          </div>
          <div class="info-row">
            <div class="label">模型推荐话术</div>
            <div class="value value-multiline">{{ formatText(props.data.reviewModelContent) }}</div>
          </div>
          <div class="info-row">
            <div class="label">实际回评话术</div>
            <div class="value value-multiline">{{ formatText(props.data.reviewContent) }}</div>
          </div>
        </div>
      </div>

      <div v-if="showPrivateMsgCard" class="mt-12">
        <div class="info-title">私信用户</div>
        <div class="info-list content-layout">
          <div class="info-row">
            <div class="label">私信进度</div>
            <div class="value">
              <span class="progress-text"
                    :class="{ 'is-success': isSuccessProgress(props.data.privateMsgProgressCode) }">
                <van-icon
                  v-if="isSuccessProgress(props.data.privateMsgProgressCode)"
                  name="checked"
                  size="14"
                  color="#00B578"
                />
                <span class="progress-gap">{{ formatText(props.data.privateMsgProgressName) }}</span>
              </span>
            </div>
          </div>
          <div class="info-row">
            <div class="label">私信次数</div>
            <div class="value">{{ formatText(props.data.privateMsgCount) }}</div>
          </div>
          <div class="info-row">
            <div class="label">私信渠道</div>
            <div class="value">{{ formatText(props.data.privateMsgChannelName || props.data.privateMsgChannel) }}</div>
          </div>
          <div class="info-row">
            <div class="label">客户姓名</div>
            <div class="value">{{ formatText(props.data.custName) }}</div>
          </div>
          <div class="info-row">
            <div class="label">手机号</div>
            <div class="value">{{ formatText(props.data.custMobile) }}</div>
          </div>

          <div v-if="privateMsgDetails.length" class="detail-grid mt-12">
            <div
              v-for="(item, index) in privateMsgDetails"
              :key="`${item.privateMsgTime || ''}-${index}`"
              class="detail-row"
            >
              <div class="detail-cell">
                <div class="cell-label">{{ `第${index + 1}次私信时间` }}</div>
                <div class="cell-value">{{ formatText(item.privateMsgTime) }}</div>
              </div>
              <div class="divider"></div>
              <div class="detail-cell">
                <div class="cell-label">{{ `第${index + 1}次私信人员` }}</div>
                <div class="cell-value">{{ formatText(item.userName) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="showWorkOrderCard" class="mt-12">
        <div class="info-title">关联工单</div>
        <div class="workorder-table pt-12 content-layout">
          <div class="table-head">
            <div class="th">工单编号</div>
            <div class="th">工单类型</div>
            <div class="th">负责人</div>
            <div class="th" style="text-align: center;">工单状态</div>
          </div>

          <div
            v-for="(row, index) in workOrders"
            :key="`${row.workOrderNo || ''}-${index}`"
            class="table-row"
          >
            <div class="td">{{ formatText(row.workOrderNo) }}</div>
            <div class="td">{{ formatText(row.type) }}</div>
            <div class="td">{{ formatText(row.respUserName) }}</div>
            <div class="td" style="justify-content: center;">
              <span class="status-dot" :class="workOrderStatusClass(formatText(row.status))"></span>
              <span class="status-text">{{ formatText(row.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.handle-detail {
  font-size: 14px;
  color: #1f2733;
}

.empty-text {
  padding: 8px 0;
  font-size: 12px;
  color: #929aa6;
}

.info-title {
  font-weight: 500;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.9);
  line-height: 22px;
  padding: 4px 10px;
  background: #F5F7FA;
  border: 1px solid #EBEDF0;
  border-radius: 2px 2px 0 0;
}

.info-list {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
  background: #FFFFFF;
  border-radius: 0px 0px 0px 0px;
  padding: 8px 10px;
}

.content-layout {
  border: 1px solid #EBEDF0;
  border-top-width: 0;
  border-radius: 0 0 2px 2px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  column-gap: 8px;

  .label {
    width: 50px;
    text-align: right;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.4);
    line-height: 22px;
  }

  .value {
    flex: 1;
    font-weight: 400;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.9);
    line-height: 22px;
    word-break: break-all;
  }
}

.value-multiline {
  white-space: pre-wrap;
}

.progress-text {
  display: inline-flex;
  align-items: center;
}

.progress-gap {
  margin-left: 4px;
}

.detail-grid {

}

.detail-row {
  display: grid;
  grid-template-columns: 1fr 2px 1fr;
  row-gap: 4px;
  padding: 8px 0;
  background: #F5F7FA;
  border-radius: 8px;

  & + & {
    margin-top: 10px;
  }
}

.detail-cell {
  min-width: 0;
  text-align: center;
  position: relative;
}

.divider {
  display: inline-block;
  height: 100%;
  width: 1px;
  border-right: 1px solid #EBEDF0;
  padding: 13px 0;
}

.cell-label {
  font-weight: 400;
  font-size: 12px;
  color: #929AA6;
  line-height: 18px;
}

.cell-value {
  margin-top: 4px;
  font-weight: 500;
  font-size: 12px;
  color: #1F2733;
  line-height: 22px;
  word-break: break-all;
}

.workorder-table {
  width: 100%;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.3fr 1fr 0.9fr 1fr;
  align-items: center;
}

.table-head {
  background: #EAF3FF;
}

.th {
  padding: 5px 8px;
  font-weight: 400;
  font-size: 12px;
  color: #5F6A7A;
  line-height: 22px;
  border-right: 1px solid #EBEDF0;

  &:last-child {
    border-right-width: 0;
  }
}

.table-row {
  border-bottom: 1px solid #EBEDF0;

  &:last-child {
    border-bottom-width: 0;
  }
}

.td {
  height: 100%;
  display: inline-flex;
  align-items: center;
  padding: 5px 8px;
  font-weight: 400;
  font-size: 12px;
  color: #1D2129;
  line-height: 22px;
  min-width: 0;
  word-break: break-all;
  border-right: 1px solid #EBEDF0;

  &:last-child {
    border-right-width: 0;
  }
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;

  &.status--done {
    background: #00b578;
  }

  &.status--doing {
    background: #1677ff;
  }

  &.status--unknown {
    background: #c0c4cc;
  }
}

.status-text {
  vertical-align: middle;
}
</style>
