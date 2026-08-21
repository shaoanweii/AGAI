<script setup lang="ts">
import { usePermissionsStore } from '@/views/H5/store/permissions'
import { computed, ref } from 'vue'
import AFInput from './components/AFInput.vue'
import AFSelect from './components/AFSelect.vue'
import { FILTER_TYPE } from '@/constants'

defineOptions({
  name: 'AdvancedFilter'
})

const visible = defineModel<boolean>()
const emits = defineEmits(['confirm', 'cancel'])

const userPermStore = usePermissionsStore()

const getAdvanced = computed<any>(() => {
  return (
    userPermStore.getAdvanced
      .filter((el: any) => !FILTER_TYPE.includes(el.filterType))
      .filter((el: any) => el?.pageDisplayType.includes('H5Home')) || []
  )
})

const handleOpen = () => {
  console.log('userPermStore', userPermStore.getAdvanced)
  console.log('getAdvanced', getAdvanced.value)
  if (!curLabel.value) {
    labelChange(getAdvanced.value[0])
  }
}

const jsonMap = ref<any>({})

const curLabel = ref<any>()
const labelChange = (label: string) => {
  curLabel.value = label
}

// 取消
const handleCancel = () => {
  visible.value = false
  emits('cancel')
}

// 将高级筛选中的数据, 处理成
const handleJsonMap = (jsonMap: any) => {
  const result: any = []
  Object.entries(jsonMap).forEach(([key, value]) => {
    const fieldConfig = getAdvanced.value.find((el: any) => key === el.field)
    result.push({
      ...fieldConfig,
      selected: fieldConfig.filterType === '1' ? value : undefined,
      inputSelected: fieldConfig.filterType === '2' ? value : undefined
    })
  })

  return result
}

// 确定
const handleConfirm = () => {
  emits('confirm', handleJsonMap(jsonMap.value))
  visible.value = false
}
</script>

<template>
  <van-popup v-model:show="visible" position="top" :style="{ height: '50%' }" @open="handleOpen">
    <div class="popup-wrap">
      <div class="pw-content">
        <!-- 左侧label -->
        <div class="pwc-label">
          <template v-for="item of getAdvanced" :key="item.field">
            <div
              class="pwcl-item"
              :class="{ active: curLabel?.field === item.field }"
              @click="labelChange(item)"
            >
              <div
                class="dot"
                :class="{
                  'dot-active': Array.isArray(jsonMap[item?.field])
                    ? jsonMap[item?.field]?.length
                    : jsonMap[item?.field]
                }"
              ></div>
              <div>{{ item.name }}</div>
            </div>
          </template>
        </div>
        <!-- 选项 -->
        <div class="pwc-value">
          <div class="title">{{ curLabel?.name }}</div>
          <div class="pwcv-content">
            <template v-if="curLabel">
              <AFInput
                v-if="curLabel.filterType === '2'"
                v-model="jsonMap[curLabel.field]"
              ></AFInput>
              <AFSelect
                v-if="curLabel.filterType === '1'"
                v-model="jsonMap[curLabel.field]"
                :options="curLabel.enumValue"
                :fields="{ label: 'value', value: 'key' }"
                :multiSelect="curLabel.multiSelect"
              ></AFSelect>
            </template>
          </div>
        </div>
      </div>
      <!-- 底部操作按钮 -->
      <div class="pw-footer">
        <div class="pwf-cancel" @click="handleCancel">取消</div>
        <div class="pwf-confirm" @click="handleConfirm">确定</div>
      </div>
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
.popup-wrap {
  width: 100%;
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  .pw-content {
    flex: 1;
    min-width: 0;
    min-height: 0;
    display: flex;

    .pwc-label {
      width: 100px;
      height: 100%;
      overflow: auto;
      background: #f2f4f7;

      .pwcl-item {
        padding: 8px 10px;
        min-height: 40px;
        background: #f2f4f7;
        font-weight: 500;
        font-size: 14px;
        color: #333333;
        line-height: 24px;
        display: flex;
        // justify-content: center;
        align-items: center;
        &.active {
          color: #1677ff;
          background: #ffffff;
        }

        .dot {
          width: 4px;
          height: 4px;
          background: transparent;
          border-radius: 50%;
          margin-right: 8px;
          flex: none;

          &.dot-active {
            background: #1677ff;
          }
        }
      }
    }

    .pwc-value {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      .title {
        font-weight: 500;
        font-size: 12px;
        color: #929aa6;
        line-height: 24px;
        flex: none;
        padding: 0 12px;
        margin-bottom: 12px;
      }
      .pwcv-content {
        flex: 1;
        min-width: 0;
        overflow-y: auto;
        padding: 0 12px;
      }
    }
  }

  .pw-footer {
    height: 56px;
    background: #ffffff;
    padding: 12px;
    display: flex;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    .pwf-cancel {
      flex: 1;
      height: 32px;
      background: #f2f3f5;
      border-radius: 2px 2px 2px 2px;
      color: #4e5969;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .pwf-confirm {
      flex: 1;
      height: 32px;
      background: #165dff;
      border-radius: 2px 2px 2px 2px;
      color: #ffffff;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
}
</style>
