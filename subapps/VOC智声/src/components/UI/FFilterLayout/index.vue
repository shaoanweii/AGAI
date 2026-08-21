<template>
  <div class="f-filter-layout">
    <div class="flex w-full">
      <div class="flex-1" :class="{ 'is-collapsed': !isExpanded }">
        <slot></slot>
      </div>
      <div class="ml-16 mr-16">
        <div class="cursor-point lh-32" @click="toggleExpand">
          <span>{{ isExpanded ? '收起' : '展开' }}</span>
          <el-icon class="ml-8">
            <component :is="isExpanded ? ArrowUp : ArrowDown" />
          </el-icon>
        </div>
      </div>
      <div class="w-200 border-left-e5e6eb">
        <div class="w-full h-full">
          <el-button type="primary" class="ml-16" @click="handleQuery">
            <el-icon style="vertical-align: middle" class="mr-10">
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button color="#F2F3F5" @click="handleReset">
            <el-icon style="vertical-align: middle" class="mr-10">
              <RefreshRight />
            </el-icon>
            重置
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ArrowDown, ArrowUp, Search, RefreshRight } from '@element-plus/icons-vue'

interface Emits {
  (e: 'query'): void
  (e: 'reset'): void
}

const emit = defineEmits<Emits>()

// 展开/收起状态
const isExpanded = defineModel<boolean>({ default: false })

// 切换展开/收起
function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

// 查询事件
function handleQuery() {
  emit('query')
}

// 重置事件
function handleReset() {
  emit('reset')
}
</script>

<style scoped lang="scss">
.f-filter-layout {
  .cursor-point {
    cursor: pointer;
    display: flex;
    align-items: center;
    // color: #1677ff;
    font-size: 14px;
    user-select: none;

    &:hover {
      opacity: 0.8;
    }
  }

  .border-left-e5e6eb {
    border-left: 1px solid #e5e6eb;
    padding-left: 0;
  }

  // 收起状态：只显示一行
  .is-collapsed {
    max-height: 32px;
    overflow: hidden;
  }
}
</style>
