<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCustomerTeasing } from '@/api/overview/index'
import type { CustomerTeasingVo } from '@/api/overview/type'
import { useQueryListener } from '@/hooks/useQueryListener'
import { Thousandth } from '@/utils'
import VoiceDetailsDialog from '@components/Business/VoiceDetailsDialog'

defineOptions({
  name: 'CustomerRant'
})

const list = ref<CustomerTeasingVo[]>([])
const loading = ref(false)
const voiceDialogVisible = ref(false)
const voiceDialogVoice = ref<{ id: string | number; originalId?: string | number } | null>(null)

/**
 * 获取用户观点列表。
 * - 直接使用总览页当前筛选条件请求接口
 * - 列表展示直接绑定接口原字段，不做二次映射
 */
const fetchCustomerTeasing = async () => {
  try {
    loading.value = true
    const response = await getCustomerTeasing(queryStore.currentQueryParams as VocQueryParams)

    if (response.success) {
      list.value = response.result || []
      return
    }

    ElMessage.error(response.message || '获取用户观点数据失败')
  } catch (error) {
    console.error('获取用户观点数据失败:', error)
    ElMessage.error('获取用户观点数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const { queryStore } = useQueryListener(fetchCustomerTeasing)

/**
 * 判断当前吐槽卡片是否可打开原声详情。
 * 仅当接口返回 dataId 时才允许点击查看原声。
 */
const isCardClickable = (item: CustomerTeasingVo) => {
  return !!item.dataId
}

/**
 * 打开原声详情弹窗。
 * 参考旅程分析 original 点击逻辑，将接口 dataId 传入 originalId。
 */
const handleCardClick = (item: CustomerTeasingVo) => {
  if (!item.dataId) return

  voiceDialogVoice.value = {
    id: '',
    originalId: item.dataId
  }
  voiceDialogVisible.value = true
}
</script>

<template>
  <div v-loading="loading" class="customer-rant">
    <div v-if="!loading && !list.length" class="customer-rant__empty">
      <el-empty description="暂无数据" />
    </div>

    <div v-else class="customer-rant__list">
      <template v-for="(item, index) of list" :key="item.dataId || item.title || index">
        <div
          class="cr-item"
          :class="{ 'cr-item--clickable': isCardClickable(item) }"
          @click="handleCardClick(item)"
        >
          <div class="cr-header">
            <div class="title">
              <div class="icon">
                <img src="@/assets/images/bq1.png" class="w-full h-full" alt="" />
              </div>
              <div class="info">{{ item.title }}</div>
            </div>
            <div class="num flex-y-center">
              <div class="icon_com">
                <img src="@/assets/images/hot-s.png" class="w-full h-full" alt="" />
              </div>
              <div>{{ Thousandth(item.mentionCount || 0) }} 条</div>
            </div>
          </div>

          <div class="cr-body">
            <SvgIcon class="left-icon" name="left_d" width="27px" height="23px"></SvgIcon>
            {{ item.mentionContent }}
          </div>

          <div class="cr-footer mt-14">
            <div class="flex-x-between-x-center">
              <div class="flex-y-center">
                <div class="icon_com">
                  <img src="@/assets/images/time-fill.png" class="w-full h-full" alt="" />
                </div>
                <span class="text-body text-secondary">{{ item.mentionTime }}</span>
              </div>
              <div class="text-body text-secondary">{{ item.customerName }}</div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <VoiceDetailsDialog v-model:visible="voiceDialogVisible" :voice="voiceDialogVoice" />
  </div>
</template>

<style lang="scss" scoped>
.customer-rant {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.customer-rant__list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  width: 100%;
  height: 100%;
  overflow: auto;
}

.customer-rant__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.cr-item {
  height: 264px;
  overflow: hidden;
  padding: 18px 16px;
  background: linear-gradient(180deg, #cde9fe 0%, #e5f7fd 100%);
  box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #d5d7da;
  transition: box-shadow 0.2s ease;

  &.cr-item--clickable {
    cursor: pointer;

    &:hover {
      box-shadow: 0px 6px 16px 0px rgba(10, 13, 18, 0.12);
    }
  }

  .cr-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .title {
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 0;

      .icon {
        width: 24px;
        height: 24px;
        // background: #0b457f;
        margin-right: 8px;
      }
      .info {
        font-weight: 500;
        font-size: 15px;
        color: #1f2733;
        line-height: 21px;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }
    }
    .num {
      font-weight: 500;
      font-size: 13px;
      color: #1f2733;
      line-height: 20px;
      flex-shrink: 0;
      margin-left: 8px;
    }
  }

  .icon_com {
    width: 20px;
    height: 20px;
    // background: #0b457f;
    margin-right: 8px;
  }

  .cr-body {
    height: 132px;
    overflow: hidden;
    background: #ffffff;
    border-radius: 8px 8px 8px 8px;
    opacity: 0.8;
    padding: 16px 16px 16px 38px;
    font-size: 14px;
    color: #1f2733;
    line-height: 24px;
    position: relative;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;

    .left-icon {
      position: absolute;
      left: 10px;
      top: 10px;
      z-index: 5;
    }
  }
}
</style>
