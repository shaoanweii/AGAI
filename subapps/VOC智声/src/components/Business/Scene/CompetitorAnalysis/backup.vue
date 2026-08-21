<script setup lang="ts">
import { Search, Close } from '@element-plus/icons-vue'
import { useGeneralScenarioStore } from '@/store'

defineOptions({
  name: 'CompetitorAnalysis'
})

const generalScenarioStore = useGeneralScenarioStore()

const handleClosed = () => {
  generalScenarioStore.handleClose()
}
</script>

<template>
  <div v-if="generalScenarioStore.visible" class="competitor-analysis">
    <div class="ca-header">
      <!-- 标题 -->
      <div class="ca-title">竞品分析报告</div>
      <!-- 操作按钮 -->
      <div class="ca-action">
        <el-date-picker type="daterange" placeholder="" />
        <el-button :icon="Search" class="ml-12">高级筛选</el-button>
        <el-button :icon="Search">导出</el-button>
        <el-button type="primary" :icon="Search">订阅报告</el-button>
        <el-button type="primary" :icon="Search">收藏 128</el-button>

        <div class="ca-close" @click="handleClosed">
          <el-icon :size="24" color="#999999"><Close /></el-icon>
        </div>
      </div>
    </div>
    <!-- 筛选条件 -->
    <div class="ca-search">
      <div class="cas-label">筛选条件:</div>
      <div class="cas-item">
        <span>竞品：比亚迪</span>
        <el-icon :size="18" color="#999999" class="ml-8"><Close /></el-icon>
      </div>

      <div class="cas-item">
        <span>时间：2024年</span>
        <el-icon :size="18" color="#999999" class="ml-8"><Close /></el-icon>
      </div>
    </div>
    <!-- 内容卡片 -->
    <div class="ca-container">
      <FCard :title="'竞品分析'" :is-show-more="false" :height="'600px'">
        <template #title>
          <div class="cacf-title-container">
            <div class="title">竞品分析</div>
            <div class="line"></div>
            <div class="subtitle">市场对比</div>
          </div>
        </template>
        <div class="ca-content">
          <h3>竞品分析内容</h3>
          <p>这是竞品分析组件的示例内容，用于测试动态组件渲染功能。</p>
          <ul>
            <li>市场份额对比</li>
            <li>产品功能对比</li>
            <li>价格策略分析</li>
            <li>用户口碑对比</li>
          </ul>
        </div>
      </FCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.competitor-analysis {
  background-color: blue;
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 64px;
  z-index: 10;

  .ca-header {
    width: 100%;
    height: 72px;
    background: #ffffff;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    border-radius: 0px 0px 0px 0px;
    border: 1px solid #dfe2e8;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 40px;

    .ca-title {
      font-weight: 600;
      font-size: 24px;
      color: #1f2733;
      line-height: 28px;
    }
    .ca-action {
      display: flex;
      .ca-close {
        height: 32px;
        border-left: 1px solid #ebedf0;
        padding-left: 25px;
        display: flex;
        align-items: center;
      }
    }
  }
  .ca-search {
    width: 100%;
    height: 72px;
    background: #ffffff;
    box-shadow: 0px 1px 2px 0px rgba(10, 13, 18, 0.05);
    border-radius: 0px 0px 0px 0px;
    border: 1px solid #dfe2e8;
    display: flex;
    align-items: center;
    padding: 0 40px;

    .cas-label {
      font-weight: 500;
      font-size: 14px;
      color: #5f6a7a;
      margin-right: 16px;
    }

    .cas-item {
      padding: 10px 16px;
      height: 40px;
      background: #f2f4f7;
      border-radius: 8px 8px 8px 8px;
      font-weight: 500;
      font-size: 14px;
      color: #333333;
      line-height: 20px;
      display: flex;
      align-items: center;

      & + .cas-item {
        margin-left: 16px;
      }
    }
  }

  .ca-container {
    background-color: #e9eef8;
    width: 100%;
    height: calc(100% - 144px);
    overflow: auto;
    padding: 24px;

    .cacf-title-container {
      display: flex;
      align-items: center;
      font-weight: 600;
      font-size: 24px;
      line-height: 32px;
      .title {
        color: #1f2733;
      }
      .line {
        width: 2px;
        height: 20px;
        background-color: #929aa6;
        margin: 0 8px;
      }
      .subtitle {
        color: #1677ff;
      }
    }

    .ca-content {
      padding: 20px;
      background: white;
      border-radius: 8px;
      margin-top: 20px;

      h3 {
        color: #1f2733;
        margin-bottom: 16px;
      }

      p {
        color: #5f6a7a;
        margin-bottom: 16px;
        line-height: 1.6;
      }

      ul {
        color: #5f6a7a;
        padding-left: 20px;

        li {
          margin-bottom: 8px;
          line-height: 1.5;
        }
      }
    }
  }
}
</style>
