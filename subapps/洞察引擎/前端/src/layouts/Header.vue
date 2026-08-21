<template>
  <header class="flex justify-between item-center">
    <div class="left flex item-center">
      <div class="h-title" v-if="['home'].includes(route.name as string)">
        <div class="home-title-row">
          <span>{{ route.meta.title }}</span>
          <el-radio-group v-model="homeRange" class="home-range-tabs">
            <el-radio-button value="today">今日</el-radio-button>
            <el-radio-button value="7">近 7 天</el-radio-button>
            <el-radio-button value="30">近 30 天</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <div
        class="h-title"
        v-else-if="['dataCenter-insDataSource', 'settings-accountManagement', 'settings-role', 'settings-download', 'review-errorCorrection', 'rules-rulesTest', 'rules-cleaningRules', 'knowledgeCenter-standardPoint', 'knowledgeCenter-carUsageScenarios', 'knowledgeCenter-experienceCode', 'knowledgeCenter-userJourney', 'knowledgeCenter-attributeLabel', 'knowledgeCenter-corpusMapping'].includes(route.name as string)"
      >
        {{ route.meta.title }}
      </div>
      <div class="h-title" v-else-if="['rules-closedLoopRules'].includes(route.name as string)">
        <div class="flex items-center">
          <div
            class="point"
            :class="{ untap: middlewareStore.closedLoopRulesType !== ClosedLoopRulesTab.SINGLE }"
            @click="middlewareStore.setClosedLoopRulesType(ClosedLoopRulesTab.SINGLE)"
          >
            单点事件
          </div>
          <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.closedLoopRulesType !== ClosedLoopRulesTab.BATCH }"
            @click="middlewareStore.setClosedLoopRulesType(ClosedLoopRulesTab.BATCH)"
          >
            批量事件
          </div>
        </div>
      </div>
      <div class="h-title" v-else-if="['dataCenter-dataQuery'].includes(route.name as string)">
        <div class="flex items-center">
          <div
            class="point"
            :class="{ untap: middlewareStore.dataType !== DataType.RESULT }"
            @click="middlewareStore.setDataType(DataType.RESULT)"
          >
            结果数据
          </div>
          <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.dataType !== DataType.CLEAN }"
            @click="middlewareStore.setDataType(DataType.CLEAN)"
          >
            清洗数据
          </div>
          <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.dataType !== DataType.RAW }"
            @click="middlewareStore.setDataType(DataType.RAW)"
          >
            原始数据
          </div>
          <!-- <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.dataType !== 'spare' }"
            @click="middlewareStore.setDataType('spare')"
          >
            备用数据
          </div> -->
        </div>
      </div>
      <div
        class="h-title"
        v-else-if="
          ['dataCenter-discovery', 'operationManagement-discovery'].includes(route.name as string)
        "
      >
        <div class="flex items-center">
          <div class="point">新词发现</div>
        </div>
      </div>
      <div
        class="h-title"
        v-else-if="['knowledgeCenter-keywordLibrary'].includes(route.name as string)"
      >
        <div class="flex items-center">
          <div
            class="point"
            :class="{ untap: middlewareStore.keywordLibraryType !== KeywordLibraryTab.RULE }"
            @click="middlewareStore.setKeywordLibraryType(KeywordLibraryTab.RULE)"
          >
            规则词库
          </div>
          <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.keywordLibraryType !== KeywordLibraryTab.ACCOUNT }"
            @click="middlewareStore.setKeywordLibraryType(KeywordLibraryTab.ACCOUNT)"
          >
            账号词库
          </div>
        </div>
      </div>
      <div
        class="h-title"
        v-else-if="['knowledgeCenter-brandSeries'].includes(route.name as string)"
      >
        <div class="flex items-center">
          <div
            class="point"
            :class="{ untap: middlewareStore.brandSeriesTab !== BrandSeriesTab.SERIES }"
            @click="middlewareStore.setBransSeriewType(BrandSeriesTab.SERIES)"
          >
            车系管理
          </div>
          <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.brandSeriesTab !== BrandSeriesTab.BRAND }"
            @click="middlewareStore.setBransSeriewType(BrandSeriesTab.BRAND)"
          >
            品牌管理
          </div>
          <div
            class="ml-24 point"
            :class="{ untap: middlewareStore.brandSeriesTab !== BrandSeriesTab.AUTOMAKER }"
            @click="middlewareStore.setBransSeriewType(BrandSeriesTab.AUTOMAKER)"
          >
            车企管理
          </div>
        </div>
      </div>
    </div>
    <div class="right flex item-center">
      <el-dropdown trigger="click" @command="handleUserCommand">
        <div class="user-info text-h4 font-500 ml-21">
          <div class="avatar font-600 flex-center ml-4">U</div>
          <span class="user-name">{{ userStore.username }}</span>
          <el-icon class="user-arrow mr-12">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <!-- <el-dropdown-item command="linkIns">
              <div class="flex item-center lh-36 custom-item">
                <div class="font-600 ml-10">前往声音洞察引擎</div>
              </div>
            </el-dropdown-item> -->
            <!-- divided -->
            <el-dropdown-item :command="isAgaiEntry ? 'workbench' : 'logout'">
              <div class="flex item-center lh-36 custom-item">
                <div class="font-600 ml-10">{{ isAgaiEntry ? '返回工作台' : '退出登录' }}</div>
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script lang="ts" setup>
import useUserStore from '@/stores/modules/user'
import useMiddlewareStore from '@/stores/modules/middleware'
import { DataType, KeywordLibraryTab, BrandSeriesTab, ClosedLoopRulesTab } from '@/constant'
import { useRoute } from 'vue-router'

const route = useRoute()
console.log('route', route)

const middlewareStore = useMiddlewareStore()
const userStore = useUserStore()
const homeRange = ref('today')
const isAgaiEntry = new URLSearchParams(window.location.search).get('agai') === '1'

const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'linkIns':
      break
    case 'workbench':
      // 门户内嵌模式不退出子系统，由顶层 AGAI 路由返回工作台。
      window.parent.location.href = '/workbench'
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()

        // if (isDev()) {
        //   userStore.logout()
        // } else {
        //   userStore.logout()
        // }
        // useUserStore().logout()

        ElMessage.success('已退出登录')
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style lang="scss" scoped>
header {
  height: 100%;
}

.left {
  .h-title {
    font-weight: 600;
    font-size: 24px;
    color: #1f2733;
    margin-left: 32px;

    .untap {
      font-weight: 500;
      font-size: 20px;
      color: #333333;
    }

    .home-title-row {
      display: flex;
      align-items: center;
      gap: 24px;
    }

    .home-range-tabs {
      :deep(.el-radio-button__inner) {
        height: 32px;
        min-width: 76px;
        padding: 0 16px;
        line-height: 30px;
        color: #526070;
        font-weight: 500;
      }

      :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
        background: #1677ff;
        border-color: #1677ff;
        box-shadow: -1px 0 0 0 #1677ff;
        color: #fff;
      }
    }
  }
  img {
    width: 150px;
    height: 40px;
  }

  h1 {
    color: var(--color-high);
    font-size: 20px;
    font-weight: bold;
    margin-left: 12px;
  }
}

.right {
  .user-info {
    background: #f8f8f8;
    border-radius: 24px 24px 24px 24px;
    border: 1px solid #d5d7da;
    line-height: 40px;
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .avatar {
      width: 32px;
      height: 32px;
      background: #4a9eff;
      border-radius: 50%;
      color: #fff;
    }

    .user-name {
      color: #1b212d;
    }

    .user-arrow {
      color: #999999;
      font-size: 20px;
    }
  }

  :deep(.el-dropdown-menu) {
    .el-dropdown-menu__item {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        margin-right: 0;
      }
    }
  }
  .custom-item {
    margin: -5px -16px;
    padding: 5px 16px;
  }
}
</style>
