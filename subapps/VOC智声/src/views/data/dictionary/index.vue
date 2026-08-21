<script setup lang="ts">
/**
 * 数据字典维护页面
 * 主体：字典类型管理
 * 抽屉：字典项详情面板
 */

defineOptions({
  name: 'DictionaryManagement'
})

import { ref, onMounted } from 'vue'
import DictTypeTable from './components/DictTypeTable.vue'
import DictItemPanel from './components/DictItemPanel.vue'
import type { DictListVo } from '@/api/dictionary/index.d'

// 页面状态
const pageLoading = ref(false)

// 选中的字典类型
const selectedDict = ref<DictListVo | null>(null)

// 抽屉显示状态
const drawerVisible = ref(false)

// 处理字典类型选择
const handleDictSelect = (dict: DictListVo) => {
  selectedDict.value = dict
  drawerVisible.value = true
}

// 处理字典类型数据更新（新增、编辑、删除后刷新）
const handleDictUpdated = () => {
  // 如果当前选中的字典被删除，关闭抽屉
  if (selectedDict.value) {
    drawerVisible.value = false
    selectedDict.value = null
  }
}

// 关闭抽屉
const handleCloseDrawer = () => {
  drawerVisible.value = false
  selectedDict.value = null
}

onMounted(() => {
  // 页面初始化
})
</script>

<template>
  <div class="dictionary-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">数据字典维护</h2>
      <p class="page-description">管理系统中的数据字典类型和字典项明细</p>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content" v-loading="pageLoading">
      <!-- 字典类型管理 -->
      <div class="content-panel">
        <div class="panel-header">
          <h3 class="panel-title">数据字典</h3>
        </div>
        <div class="panel-content">
          <DictTypeTable @dict-select="handleDictSelect" @dict-updated="handleDictUpdated" />
        </div>
      </div>
    </div>

    <!-- 字典项管理抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="`字典项列表 - ${selectedDict?.dictName || ''}`"
      size="900px"
      direction="rtl"
      :before-close="handleCloseDrawer"
      destroy-on-close
    >
      <template #header="{ titleId, titleClass }">
        <div class="drawer-header">
          <h3 :id="titleId" :class="titleClass" class="drawer-title">字典项列表</h3>
          <div v-if="selectedDict" class="dict-info">
            <el-tag type="primary" size="small">{{ selectedDict.dictType }}</el-tag>
            <span class="dict-name">{{ selectedDict.dictName }}</span>
          </div>
        </div>
      </template>

      <div class="drawer-content">
        <DictItemPanel v-if="selectedDict" :dict-info="selectedDict" />
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.dictionary-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;

  .page-header {
    padding: 24px;
    border-bottom: 1px solid #dcdfe6;
    background: #fff;

    .page-title {
      margin: 0 0 4px 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .page-description {
      margin: 0;
      font-size: 13px;
      color: #909399;
    }
  }

  .main-content {
    flex: 1;
    padding: 24px;

    .content-panel {
      height: 100%;
      display: flex;
      flex-direction: column;
      background: #fff;
      border-radius: 6px;
      border: 1px solid #dcdfe6;

      .panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 24px;
        border-bottom: 1px solid #dcdfe6;

        .panel-title {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
      }

      .panel-content {
        flex: 1;
        padding: 24px;
        min-height: 0;
      }
    }
  }
}

// 抽屉样式
:deep(.el-drawer) {
  .el-drawer__header {
    padding: 24px;
    margin-bottom: 0;
    border-bottom: 1px solid #dcdfe6;
    background: #f9f9f9;
  }

  .el-drawer__body {
    padding: 0;
  }
}

.drawer-header {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .drawer-title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .dict-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .dict-name {
      font-size: 13px;
      color: #909399;
    }
  }
}

.drawer-content {
  padding: 24px;
  height: 100%;
}
</style>
