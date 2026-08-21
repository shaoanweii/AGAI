<script setup lang="ts">
import ToolPop from './ToolPop.vue'
import { fmtPer, fmtNum } from '@/utils'

interface Props {
  // toolName?: string
  tableData: any[]
}

withDefaults(defineProps<Props>(), {
  // toolName: '',
  tableData: () => []
})

const emit = defineEmits<{
  'row-click': any
}>()

/**
 * 点击观点行时向上层抛出观点名称，供客户原声联动使用。
 *
 * @param opinionName 当前点击的观点名称
 */
const handleClick = (opinionName: string, item: any) => {
  emit('row-click', opinionName, item)
}
</script>

<template>
  <div class="table-body">
    <ToolPop
      v-for="(item, index) in tableData"
      :key="item.id"
      tool-name="opinion"
      :row-data="item"
      @reference-click="handleClick(item.opinion, item)"
    >
      <template #popBtn>
        <div class="table-row">
          <!-- 第一列-->
          <div class="cell flex-1">
            <SortNum :rank="index + 1" />
            <div class="name fs-14 ml-5">{{ item.opinion }}</div>
          </div>

          <!-- 第二列：提及量 -->
          <div class="cell w-100">
            <span>{{ fmtNum(item.mentions) }}</span>
          </div>

          <!-- 第三列：负面率 -->
          <div class="cell w-100">
            <span>{{ fmtPer(item.mentionsMoM) }}</span>
          </div>
        </div>
      </template>
    </ToolPop>
  </div>
</template>

<style lang="scss" scoped>
@media screen and (max-width: 1600px) {
  :deep(.cell) {
    padding: 0 5px !important;
  }
}

// 表格主体
.table-body {
  .table-row {
    display: grid;
    // grid-template-columns: 2fr 1fr 1fr;
    // grid-template-columns: repeat(3, 1fr);
    grid-template-columns: 1fr 100px 100px;

    &:hover {
      font-weight: bold;
      cursor: pointer;
    }

    // &:not(:last-child) {
    //   border-bottom: 1px solid #ebedf0;
    // }
  }
}

.cell {
  display: flex;
  justify-content: center;
  align-items: center;

  height: 42px;
  line-height: 42px;

  padding: 0 8px;
  font-weight: 500;
  font-size: 14px;
  color: #333333;
  // 避免 每等分伸缩
  overflow: hidden;

  &:first-child {
    // display: block;
    color: #333;
    font-size: 16px;

    .name {
      width: calc(100% - 20px);
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
  }

  &:nth-child(2) {
    color: #1f2733;
  }

  &:nth-child(3) {
    color: #666;
  }
}
</style>
