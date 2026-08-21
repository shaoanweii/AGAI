<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { HomeReportTopVo } from '@/api/overview/type'
import { saveReportViewLog } from '@/api/overview/index'
import { useRouter } from 'vue-router'
import useSceneAnalysisStore from '@/store/modules/sceneAnalysis'

defineOptions({
  name: 'SpecialAnalysis'
})

interface Props {
  data?: HomeReportTopVo[]
  loading?: boolean
}

const { data = [], loading = false } = defineProps<Props>()

// 定义 emits
const emit = defineEmits<{
  refresh: []
}>()

const selectedIndex = ref(0)
const router = useRouter()
const sceneAnalysisStore = useSceneAnalysisStore()

/**
 * 处理行点击事件
 */
const handleItemClick = async (index: number, item: HomeReportTopVo | any) => {
  selectedIndex.value = selectedIndex.value === index ? -1 : index
  const reportName = typeof item?.reportName === 'string' ? item.reportName.trim() : undefined

  // 总是设置场景源数据（从 defaultCondition 中解析）
  await sceneAnalysisStore.setSceneOriginData({
    ...item,
    isDetail: true
  })

  if (item.reportUrl) {
    router.push({
      path: item.reportUrl,
      query: {
        reportJudgeId: item.id || undefined,
        isBack: '1',
        reportName
      }
    })
  } else {
    ElMessage.warning('未找到对应页面')
  }
  // 如果是待查看状态，调用接口
  if (item.id) {
    try {
      const response = await saveReportViewLog(item.id)
      if (response.success) {
        // ElMessage.success('查看记录已保存')
        // 通知父组件刷新数据
        if (item.status === 0) {
          emit('refresh')
        }
      } else {
        // ElMessage.error(response.message || '保存查看记录失败')
      }
    } catch (error) {
      console.error('保存查看记录失败:', error)
    }
  }
}
</script>

<template>
  <div class="special-analysis">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="data.length === 0" class="empty-container">
      <el-empty description="暂无专项分析数据" />
    </div>
    <div v-else class="special-analysis__list">
      <div
        v-for="(item, index) of data"
        :key="item.id || index"
        class="sa-item"
        :class="{ active: item.status === 0 }"
        @click="handleItemClick(index, item)"
      >
        <div class="sa-logo flex-center">
          <SvgIcon name="document" width="24px" height="24px"></SvgIcon>
        </div>
        <div class="sa-info">{{ item.reportName || '未知报告' }}</div>
        <div class="sa-btn mr-10" v-if="item.status === 0">待查看</div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.special-analysis {
  width: 100%;
  height: 100%;
  max-height: 100%;
  overflow: hidden;

  .special-analysis__list {
    width: 100%;
    height: 100%;
    overflow: auto;
  }

  .loading-container,
  .empty-container {
    padding: 16px;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
    overflow: hidden;
  }

  .sa-item {
    background: #f5f7fa;
    border-radius: 4px;
    padding: 4px 8px;
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #1f2733;

    &.active {
      background: #f2f4f7;

      .sa-info {
        font-weight: 500;
      }
    }

    & + .sa-item {
      margin-top: 16px;
    }

    .sa-logo {
      width: 32px;
      height: 32px;
      margin-right: 8px;
    }

    .sa-info {
      font-weight: 400;
      font-size: 16px;
      color: #333333;
      line-height: 20px;
      flex: 1;
    }

    .sa-btn {
      font-weight: 500;
      font-size: 14px;
      color: #1677ff;
      line-height: 18px;
    }
  }
}
</style>
