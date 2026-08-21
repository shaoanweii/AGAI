<script setup lang="ts">
import type { CategorizeItem } from '@/types/baseSeting.types'

const emit = defineEmits(['handleCategorizeChange', 'handleEdit', 'handleDelete'])

const props = withDefaults(
  defineProps<{
    treeData: CategorizeItem[]
    curCategorize: CategorizeItem | undefined
  }>(),
  {}
)
const { treeData, curCategorize } = toRefs(props)

const handleCategorizeChange = (item: any, index: number) => {
  emit('handleCategorizeChange', item, index)
}
const handleEdit = (item: any) => {
  emit('handleEdit', item)
}
const handleDelete = (item: any) => {
  emit('handleDelete', item)
}
const showChild = (item: CategorizeItem, index: number) => {
  treeData.value[index].showChild = !treeData.value[index].showChild
}
</script>

<template>
  <template v-for="(item, index) of treeData" :key="index">
    <div
      class="listItem list-item-wrapper"
      :class="{ active: curCategorize?.id == item?.id, tap: curCategorize?.id == item?.id }"
      @click.stop="handleCategorizeChange(item, index)"
    >
      <div class="list-item flex item-center">
        <div :style="{ width: '16px', marginLeft: (item?.level || 0) * 16 + 'px' }">
          <template v-if="item.child">
            <i
              v-if="!item.showChild"
              :data-testid="`baseSetting-channel-left-icon1-${index}`"
              class="iconfont icon-you lh-1"
              @click.stop="showChild(item, index)"
            ></i>
            <i
              v-else
              class="iconfont icon-xia lh-1"
              :data-testid="`baseSetting-channel-left-icon2-${index}`"
              @click.stop="showChild(item, index)"
            ></i>
          </template>
        </div>
        <span :data-testid="`baseSetting-channel-left-10002-${index}`" style="margin-left: 8px">{{
          item?.name
        }}</span>
      </div>
      <div class="list-item-actions">
        <icon-edit
          v-auth="`settings-channelConfig-edit`"
          v-if="item.id !== '-1'"
          :data-testid="`baseSetting-channel-left-10003-${index}`"
          :class="{ 'ft-disabled': item?.name === '未确认渠道' }"
          @click.stop="handleEdit(item)"
        />
        <icon-delete
          v-auth="`settings-channelConfig-delete`"
          :data-testid="`baseSetting-channel-left-10004-${index}`"
          :class="{ 'ft-disabled': item?.name === '未确认渠道' }"
          v-if="item.id !== '-1'"
          @click.stop="handleDelete(item)"
        />
      </div>
    </div>
    <CategorizeTree
      v-if="item.child && item.showChild"
      :tree-data="item.child"
      :cur-categorize="curCategorize"
      @handle-categorize-change="handleCategorizeChange"
      @handle-edit="handleEdit"
      @handle-delete="handleDelete"
    ></CategorizeTree>
  </template>
</template>

<style scoped lang="scss">
.listItem {
  //padding: 13px 16px !important;
  padding: 10px 16px 9px !important;
  box-sizing: border-box !important;
}
</style>
