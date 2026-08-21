<script setup lang="ts">
import ProcessSectionCard from './ProcessSectionCard.vue'
import type { BatchEventCcUserVo } from '@h5/api/batchEvent/types'

defineOptions({
  name: 'BatchCarbonCopySection'
})

interface CarbonCopySectionProps {
  /** 抄送人员列表 */
  persons: BatchEventCcUserVo[]
  /** 是否展示添加入口 */
  showAdd?: boolean
}

const props = withDefaults(defineProps<CarbonCopySectionProps>(), {
  showAdd: false
})
const emit = defineEmits<{
  /** 点击添加抄送人员 */
  (e: 'add'): void
}>()

/**
 * 向父组件通知打开添加抄送人员弹窗。
 */
const handleAdd = () => {
  emit('add')
}

/**
 * 直接按接口字段展示部门层级。
 * @param person 抄送人员接口项
 * @returns 表格部门文案
 */
const getDepartmentText = (person: BatchEventCcUserVo) => {
  return (
    [person.leve2DeptName, person.leve3DeptName].filter(Boolean).join('#') ||
    person.leve2DeptName ||
    person.leve3DeptName ||
    '-'
  )
}

/**
 * 直接按接口字段展示姓名工号。
 * @param person 抄送人员接口项
 * @returns 表格人员文案
 */
const getUserText = (person: BatchEventCcUserVo) => {
  const userName = person.nodeUserName || '-'
  return person.nodeUserEmpNo ? `${userName} ${person.nodeUserEmpNo}` : userName
}
</script>

<template>
  <ProcessSectionCard title="抄送人员" collapsible>
    <template #title-extra>
      <button v-if="props.showAdd" class="copy-add-button" type="button" @click.stop="handleAdd">
        <van-icon name="plus" size="14" color="#1677FF" />
        <span>添加</span>
      </button>
    </template>

    <div class="copy-table">
      <div class="copy-table__header">
        <div class="copy-table__cell">二级部门#三级部门</div>
        <div class="copy-table__cell">姓名工号</div>
      </div>

      <div
        v-for="(person, index) in props.persons"
        :key="`${person.id || person.nodeUserEmpNo || person.nodeUserName || 'cc'}-${index}`"
        class="copy-table__row"
      >
        <div class="copy-table__cell">{{ getDepartmentText(person) }}</div>
        <div class="copy-table__cell">{{ getUserText(person) }}</div>
      </div>

      <div v-if="props.persons.length === 0" class="copy-table__empty">暂无抄送人员</div>
    </div>
  </ProcessSectionCard>
</template>

<style scoped lang="scss">
.copy-add-button {
  height: 24px;
  margin-left: 10px;
  padding: 0 8px;
  border: 1px solid #d6e6ff;
  border-radius: 2px;
  background: #edf5ff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #1677ff;
}

.copy-table {
  overflow: hidden;
  border: 1px solid #ebeef2;
  border-radius: 2px;
}

.copy-table__header,
.copy-table__row {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
}

.copy-table__header {
  background: #eaf3ff;
}

.copy-table__row {
  background: #ffffff;
}

.copy-table__row + .copy-table__row {
  border-top: 1px solid #ebeef2;
}

.copy-table__cell {
  min-width: 0;
  padding: 5px 8px;
  border-right: 1px solid #ebeef2;
  font-weight: 400;
  font-size: 12px;
  line-height: 22px;
  color: #1f2733;
  word-break: break-all;
}

.copy-table__header .copy-table__cell {
  color: #5f6a7a;
}

.copy-table__cell:last-child {
  border-right-width: 0;
}

.copy-table__empty {
  padding: 10px 8px;
  font-weight: 400;
  font-size: 12px;
  line-height: 18px;
  color: #929aa6;
}
</style>
