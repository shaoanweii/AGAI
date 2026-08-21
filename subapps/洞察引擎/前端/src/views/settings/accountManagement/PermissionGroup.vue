<template>
  <el-checkbox v-model="totalChecked" :indeterminate="indeterminate" @change="changeAllChecked"
    >全选</el-checkbox
  >
  <check-group
    :mockData="dashBoardData"
    @updateTotalChecked="getDashBoardData"
    ref="dashboard"
  ></check-group>
  <check-group
    :mockData="dataProcessData"
    @updateTotalChecked="getDataProcessData"
    ref="DataProcess"
  ></check-group>
  <check-group
    :mockData="tagManagementData"
    @updateTotalChecked="getTagManagementData"
    ref="TagManagement"
  ></check-group>
  <check-group :mockData="modelData" @updateTotalChecked="getModelData" ref="Model"></check-group>
  <check-group
    :mockData="projectData"
    @updateTotalChecked="getProjectData"
    ref="Project"
  ></check-group>
  <check-group
    v-if="propsData.accountType === 'system'"
    :mockData="systemSettingData"
    @updateTotalChecked="getSystemSettingData"
    ref="SystemSetting"
  ></check-group>
</template>
<script setup lang="ts">
import CheckGroup from './CheckGroup.vue'

const totalChecked = ref(true)
const indeterminate = ref(false)
const dashBoardDataChecked = ref('all')
const dataProcessDataChecked = ref('all')
const tagManagementDataChecked = ref('all')
const modelDataChecked = ref('all')
const projectDataChecked = ref('all')
const systemSettingDataChecked = ref('all')

const dashboard = ref<InstanceType<typeof CheckGroup> | null>(null)
const DataProcess = ref<InstanceType<typeof CheckGroup> | null>(null)
const TagManagement = ref<InstanceType<typeof CheckGroup> | null>(null)
const Model = ref<InstanceType<typeof CheckGroup> | null>(null)
const Project = ref<InstanceType<typeof CheckGroup> | null>(null)
const SystemSetting = ref<InstanceType<typeof CheckGroup> | null>(null)

// const dashboard = ref(null)
// const DataProcess = ref(null)
// const TagManagement = ref(null)
// const Model = ref(null)
// const Project = ref(null)
// const SystemSetting = ref(null)

const changeAllChecked = () => {
  console.log(111, totalChecked.value)
  if (
    !totalChecked.value &&
    dashboard.value &&
    DataProcess.value &&
    TagManagement.value &&
    Model.value &&
    Project.value &&
    SystemSetting.value
  ) {
    indeterminate.value = false
    dashboard.value.handleChangeAll(false)
    DataProcess.value.handleChangeAll(false)
    TagManagement.value.handleChangeAll(false)
    Model.value.handleChangeAll(false)
    Project.value.handleChangeAll(false)
    SystemSetting.value.handleChangeAll(false)
  } else if (
    totalChecked.value &&
    dashboard.value &&
    DataProcess.value &&
    TagManagement.value &&
    Model.value &&
    Project.value &&
    SystemSetting.value
  ) {
    dashboard.value.handleChangeAll(true)
    DataProcess.value.handleChangeAll(true)
    TagManagement.value.handleChangeAll(true)
    Model.value.handleChangeAll(true)
    Project.value.handleChangeAll(true)
    SystemSetting.value.handleChangeAll(true)
  }
}
watchEffect(() => {
  totalChecked.value =
    dashBoardDataChecked.value === 'all' &&
    dataProcessDataChecked.value === 'all' &&
    tagManagementDataChecked.value === 'all' &&
    modelDataChecked.value === 'all' &&
    projectDataChecked.value === 'all' &&
    systemSettingDataChecked.value === 'all'
})
watchEffect(() => {
  indeterminate.value =
    (dashBoardDataChecked.value !== 'empty' ||
      dataProcessDataChecked.value !== 'empty' ||
      tagManagementDataChecked.value !== 'empty' ||
      modelDataChecked.value !== 'empty' ||
      projectDataChecked.value !== 'empty' ||
      systemSettingDataChecked.value !== 'empty') &&
    !totalChecked.value
  console.log('indeterminate', indeterminate.value)
})

const propsData = defineProps({
  accountType: {
    type: String,
    default: 'system'
  }
})
const dashBoardData = reactive({
  title: '主控台',
  checked: true,
  indeterminate: false,
  checkedList: [],
  children: [
    {
      title: '主控台',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    }
  ]
})
const dataProcessData = reactive({
  title: '数据治理',
  checked: true,
  indeterminate: false,
  checkedList: [],
  children: [
    {
      title: '数据监控',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    },
    {
      title: '数据处理',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    },
    {
      title: '数据资产',
      checked: true,
      indeterminate: false,
      checkedList: [],
      children: [
        {
          title: '数据源',
          checked: true,
          indeterminate: false,
          checkedList: ['查看', '编辑'],
          qxChildren: [
            {
              title: '查看',
              checked: true
            },
            {
              title: '编辑',
              checked: true
            }
          ]
        },
        {
          title: '语料库',
          checked: true,
          indeterminate: false,
          checkedList: ['查看', '编辑'],
          qxChildren: [
            {
              title: '查看',
              checked: true
            },
            {
              title: '编辑',
              checked: true
            }
          ]
        },
        {
          title: '车系库',
          checked: true,
          indeterminate: false,
          checkedList: ['查看', '编辑'],
          qxChildren: [
            {
              title: '查看',
              checked: true
            },
            {
              title: '编辑',
              checked: true
            }
          ]
        }
      ]
    }
  ]
})
const tagManagementData = reactive({
  title: '标签管理',
  checked: true,
  indeterminate: false,
  checkedList: [],
  children: [
    {
      title: '标签库',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    },
    {
      title: '标签应用',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    }
  ]
})
const modelData = reactive({
  title: '模型调优',
  checked: true,
  indeterminate: false,
  checkedList: [],
  children: [
    {
      title: '模型管理',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    }
  ]
})
const projectData = reactive({
  title: '项目应用',
  checked: true,
  indeterminate: false,
  checkedList: [],
  children: [
    {
      title: '项目应用',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    }
  ]
})
const systemSettingData = reactive({
  title: '系统设置',
  checked: true,
  indeterminate: false,
  checkedList: [],
  children: [
    {
      title: '账号管理',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    },
    {
      title: '客户管理',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    },
    {
      title: '基础配置',
      checked: true,
      indeterminate: false,
      checkedList: ['查看', '编辑'],
      qxChildren: [
        {
          title: '查看',
          checked: true
        },
        {
          title: '编辑',
          checked: true
        }
      ]
    }
  ]
})

const getDashBoardData = (val: string) => {
  dashBoardDataChecked.value = val
  console.log(dashBoardDataChecked.value)
}
const getDataProcessData = (val: string) => {
  dataProcessDataChecked.value = val
  console.log(dataProcessDataChecked.value)
}
const getTagManagementData = (val: string) => {
  tagManagementDataChecked.value = val
  console.log(tagManagementDataChecked.value)
}
const getModelData = (val: string) => {
  modelDataChecked.value = val
  console.log(modelDataChecked.value)
}
const getProjectData = (val: string) => {
  projectDataChecked.value = val
  console.log(projectDataChecked.value)
}
const getSystemSettingData = (val: string) => {
  systemSettingDataChecked.value = val
  console.log(systemSettingDataChecked.value)
}
</script>
<style lang="scss">
.check-box-group {
  margin-top: 24px;
  &:nth-child(2) {
    margin-top: 20px;
  }
}
</style>
