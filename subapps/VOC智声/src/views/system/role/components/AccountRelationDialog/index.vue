<template>
  <el-dialog
    v-model="dialogVisible"
    title="关联账号"
    width="800px"
    style="padding: 0; height: 70vh; display: flex; flex-direction: column; border-radius: 12px"
    align-center
    header-class="account-relation-dialog-header-class"
    body-class="account-relation-dialog-body-class"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="dialog-content-wrapper">
      <!-- Tabs -->
      <div class="tabs-wrapper flex-y-center">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ 'tab-active': tab.key === currentTab }"
          @click="handleTabClick(tab)"
        >
          {{ tab.label }}
        </div>
      </div>
      <div v-if="dialogVisible" class="content-layout">
        <!-- 已关联 -->
        <AccountListTab
          v-show="currentTab === TabKey.LINKED"
          ref="linkedTabRef"
          tab-type="linked"
          :role-data="roleData"
          :departs="departs"
          @changed="handleChanged"
        />

        <!-- 未关联 -->
        <AccountListTab
          v-show="currentTab === TabKey.UNLINKED"
          ref="unlinkedTabRef"
          tab-type="unlinked"
          :role-data="roleData"
          :departs="departs"
          @changed="handleChanged"
        />
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Role } from '@/types/system'
import { findDepartTree } from '@/api/common'
import AccountListTab from './AccountListTab.vue'

defineOptions({
  name: 'AccountRelationDialog'
})

interface Props {
  roleData?: Role | null
}

const props = withDefaults(defineProps<Props>(), {
  roleData: null
})

const emit = defineEmits<{
  (e: 'success'): void
}>()

// Tab Key 枚举
enum TabKey {
  LINKED = 'linked',
  UNLINKED = 'unlinked'
}

const tabs = [
  {
    label: '已关联',
    key: TabKey.LINKED
  },
  {
    label: '未关联',
    key: TabKey.UNLINKED
  }
]

const currentTab = ref<TabKey>(TabKey.LINKED)
const linkedTabRef = ref<InstanceType<typeof AccountListTab> | null>(null)
const unlinkedTabRef = ref<InstanceType<typeof AccountListTab> | null>(null)
const departs = ref<any[]>([])
const hasRelationChange = ref(false)

// 使用 defineModel 实现 visible 的双向绑定
const dialogVisible = defineModel<boolean>('visible', { default: false })

// 弹窗打开事件
const handleOpen = async () => {
  if (departs.value.length === 0) {
    const response = await findDepartTree()
    if (response.success) {
      departs.value = response.result || []
    }
  }
  currentTab.value = TabKey.LINKED
  linkedTabRef.value?.handleQuery()
}

// 关闭弹窗事件
const handleClose = () => {
  dialogVisible.value = false
  if (hasRelationChange.value) {
    emit('success')
  }
  hasRelationChange.value = false
}

// 切换tab
const handleTabClick = (tab: { key: TabKey }) => {
  currentTab.value = tab.key
  if (tab.key === TabKey.LINKED) {
    linkedTabRef.value?.handleQuery()
  } else {
    unlinkedTabRef.value?.handleQuery()
  }
}

// 刷新事件
const handleChanged = () => {
  hasRelationChange.value = true
}
</script>

<style lang="scss">
.account-relation-dialog-header-class {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 24px;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #ebf4fd 0%, #ffffff 100%);
  font-weight: 600;
  font-size: 20px;
  color: #1f2733;
  padding-bottom: 0;
}

.account-relation-dialog-body-class {
  height: 100%;
  padding: 0 !important;
  overflow: hidden;
}
</style>

<style lang="scss" scoped>
.dialog-content-wrapper {
  padding: 24px 40px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tabs-wrapper {
  flex-shrink: 0;
  margin-bottom: 0;
}

.content-layout {
  border: 1px solid #ebedf0;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tab-item {
  cursor: pointer;
  width: 88px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f4f7;
  border-radius: 0;
  border-left: 1px solid;
  border-top: 1px solid;
  border-color: #ebedf0;
  font-weight: 600;
  font-size: 14px;
  color: #414651;
}

.tab-item:hover {
  background: #f2f4f7;
}

.tab-item:first-child {
  border-left: 1px solid #ebedf0;
}

.tab-item:last-child {
  border-right: 1px solid #ebedf0;
}

.tab-active {
  background: #1677ff !important;
  color: #ffffff !important;
}
</style>
