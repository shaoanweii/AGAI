<script setup lang="ts">
import type { SingleEventDetailVo } from '@/api/singlePointEvent/types'

defineOptions({
  name: 'OperationLog'
})

const { eventInfo } = defineProps<{
  eventInfo: SingleEventDetailVo
}>()
</script>

<template>
  <div class="operation-log">
    <div v-for="(log, index) in eventInfo.operateLogs" :key="log.id || index" class="log-item">
      <!-- 时间轴圆点 -->
      <div class="timeline-dot"></div>

      <!-- 日志内容 -->
      <div class="log-content">
        <!-- 标题 -->
        <div class="log-title">{{ log.operateType }}</div>

        <!-- 详情信息 -->
        <div v-if="log.content?.length" class="log-details">
          <div v-for="(detail, idx) in log.content" :key="idx" class="detail-item">
            <span v-if="detail.contentType" class="detail-label">{{ detail.contentType }}</span>
            <span class="detail-value">{{ detail.content }}</span>
          </div>
        </div>

        <!-- 操作人和时间 -->
        <div class="log-footer">
          <span v-if="log.operateUserName" class="operator">
            {{ log.operateOrgName ? `${log.operateOrgName}-` : log.operateOrgName
            }}{{ log.operateUserName
            }}{{ log.operateUserEmpNo ? `(${log.operateUserEmpNo})` : log.operateUserEmpNo }}
          </span>
          <span v-if="log.operateUserName && log.operateTime" class="divider">|</span>
          <span class="time">{{ log.operateTime }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.operation-log {
  // padding: 20px;

  .log-item {
    position: relative;
    padding-left: 24px;
    padding-bottom: 20px;

    &:not(:last-child) {
      &::before {
        content: '';
        position: absolute;
        left: 3px;
        top: 22px;
        bottom: 0;
        width: 2px;
        background: #dcdcdc;
      }
    }

    .timeline-dot {
      position: absolute;
      left: 0;
      top: 8px;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #929aa6;
    }

    .log-content {
      .log-title {
        font-weight: 400;
        font-size: 16px;
        color: rgba(0, 0, 0, 0.9);
        line-height: 24px;
        margin-bottom: 8px;
      }

      .log-details {
        margin-bottom: 12px;
        border-radius: 4px;
        padding: 16px;
        background-color: #f5f7fa;

        .detail-item {
          font-weight: 400;
          font-size: 14px;
          color: rgba(0, 0, 0, 0.6);
          line-height: 22px;
          .detail-label {
            display: inline-block;
            // min-width: 70px;
            color: #909399;
            margin-right: 16px;
            font-weight: 400;
            font-size: 14px;
            color: #86909c;
            line-height: 22px;
          }

          .detail-value {
            font-weight: 400;
            font-size: 14px;
            color: #1d2129;
            line-height: 22px;
          }
        }
      }

      .log-footer {
        font-weight: 400;
        font-size: 14px;
        color: #929aa6;
        line-height: 22px;

        .operator {
          margin-right: 8px;
        }

        .divider {
          margin: 0 8px;
        }

        .time {
          margin-left: 8px;
        }
      }
    }
  }
}
</style>
