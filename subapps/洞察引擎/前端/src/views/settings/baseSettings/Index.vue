<template>
  <div class="baseSettings">
    <el-tabs v-model="activeKey">
      <template v-for="item of tabs" :key="item.key">
        <template v-if="item.key === CHANNEL_CONFIGURATION_KEY">
          <el-tab-pane :label="item.tabTitle" :name="item.key" v-if="getHasPermission(item.key)">
            <Channel />
          </el-tab-pane>
        </template>
        <template v-else-if="item.key === REGION_CONFIGURATION_KEY">
          <el-tab-pane :label="item.tabTitle" :name="item.key" v-if="getHasPermission(item.key)">
            <Config />
          </el-tab-pane>
        </template>
        <template v-else-if="item.key === RISK_KEY_WRODS_KEY">
          <el-tab-pane :label="item.tabTitle" :name="item.key" v-if="getHasPermission(item.key)">
            <RiskKeywords />
          </el-tab-pane>
        </template>
        <template v-else>
          <el-tab-pane :label="item.tabTitle" :name="item.key" v-if="getHasPermission(item.key)">
            <Common :tab-item="item" />
          </el-tab-pane>
        </template>
      </template>
    </el-tabs>
  </div>
</template>
<script setup lang="ts">
import { useSetTabDataId } from '@/hooks/useSetTabDataId'
import type { TabsItem } from '@/types/baseSeting.types'
import Channel from '@/views/settings/baseSettings/components/tabItem/channel/Index.vue'
import Config from '@/views/settings/baseSettings/components/tabItem/config/Index.vue'
import Common from './components/tabItem/common/index.vue'
import { useTabPermission } from '@/hooks/useTabPermission'
import RiskKeywords from './components/tabItem/riskKeywords/index.vue'

// 渠道配置key
const CHANNEL_CONFIGURATION_KEY = 'channelConfiguration'
const REGION_CONFIGURATION_KEY = 'regionConfiguration'
const RISK_KEY_WRODS_KEY = 'riskKeywords'

const tabs: TabsItem[] = [
  {
    tabTitle: '能源类型',
    key: 'findEnergyInfo',
    src: '/insights/basicInfo/findEnergyInfo',
    columns: [
      {
        title: '类型',
        dataIndex: 'typeName',
        slotName: 'typeName',
        width: 190
      },
      {
        title: '分类',
        dataIndex: 'classifyName',
        slotName: 'classifyName',
        width: 190
      },
      {
        title: '关联',
        dataIndex: 'correlation',
        slotName: 'correlation',
        width: 200
      },
      {
        title: '描述',
        dataIndex: 'description',
        slotName: 'description'
      }
    ]
  },
  // {
  //   tabTitle: '区域省市',
  //   key: 'findProvinceAreaInfo',
  //   src: '/insights/basicInfo/findProvinceAreaInfo',
  //   columns: [
  //     {
  //       title: '省份',
  //       dataIndex: 'provinceName',
  //       slotName: 'provinceName',
  //       width: 100
  //     },
  //     {
  //       title: '城市',
  //       dataIndex: 'areaName',
  //       slotName: 'areaName'
  //     }
  //   ]
  // },
  {
    tabTitle: '车辆类型',
    key: 'findVehicleInfo',
    src: '/insights/basicInfo/findVehicleInfo',
    columns: [
      {
        title: '类型',
        dataIndex: 'carType',
        slotName: 'carType',
        width: 120
      },
      {
        title: '级别',
        dataIndex: 'carLevel',
        slotName: 'carLevel',
        ellipsis: true,
        tooltip: true,
        width: 670
      },
      {
        title: '描述',
        dataIndex: 'description',
        slotName: 'description'
      }
    ]
  },
  {
    tabTitle: '标签类型',
    key: 'findLabelTypeInfo',
    src: '/insights/basicInfo/findLabelTypeInfo',
    columns: [
      {
        title: '类型',
        dataIndex: 'typeName',
        width: 120,
        slotName: 'typeName'
      },
      {
        title: '关联配置',
        dataIndex: 'classifyName',
        ellipsis: true,
        tooltip: true,
        width: 270,
        slotName: 'classifyName'
      },
      {
        title: '关联模型',
        dataIndex: 'processingModel',
        slotName: 'processingModel',
        ellipsis: true,
        tooltip: true,
        width: 270
      },
      {
        title: '描述',
        dataIndex: 'description',
        slotName: 'description'
      }
    ]
  },
  {
    tabTitle: '严重性等级',
    key: 'findSeriousnessInfo',
    src: '/insights/basicInfo/findSeriousnessInfo',
    columns: [
      {
        title: '类型',
        dataIndex: 'typeName',
        width: 120,
        slotName: 'typeName'
      },
      {
        title: '描述',
        dataIndex: 'description',
        slotName: 'description'
      }
    ]
  },
  {
    tabTitle: '用户旅程',
    key: 'findUserJourneyInfo',
    src: '/insights/basicInfo/findUserJourneyInfo',
    columns: [
      {
        title: '类型',
        dataIndex: 'typeName',
        slotName: 'typeName',
        width: 120
      },
      {
        title: '描述',
        dataIndex: 'description',
        slotName: 'description'
      }
    ]
  },
  {
    // tabTitle: '区域配置',
    tabTitle: '区域省市',
    key: REGION_CONFIGURATION_KEY,
    src: '',
    columns: []
  },
  {
    tabTitle: '渠道配置',
    key: CHANNEL_CONFIGURATION_KEY,
    src: '',
    columns: []
  },
  {
    tabTitle: '风险关键词',
    key: RISK_KEY_WRODS_KEY,
    src: '',
    columns: []
  }
]

const { activeKey, getHasPermission } = useTabPermission('baseSettings')

useSetTabDataId('baseSettings-100')
</script>

<style lang="scss">
.baseSettings {
  background-color: #fff;
  border-radius: 4px;

  .common-tabs-content {
    padding: 24px;
    background-color: #fff;
  }

  .el-tabs-nav {
    padding: 0 8px 0;
    background-color: #fff;
    border-radius: 4px 4px 0 0;
  }

  .el-tabs-nav-type-line .el-tabs-tab {
    padding: 16px 0;
  }

  .el-tabs-tab-title {
    font-size: 16px;
    line-height: 24px;
    box-sizing: border-box;
  }

  .el-tabs-content {
    //padding: 24px;
    padding: 0;
  }

  .el-table-th-title {
    font-weight: 600;
  }

  .el-tabs-tab-active,
  .el-tabs-tab-active:hover {
    font-weight: 600;
  }
}
</style>
