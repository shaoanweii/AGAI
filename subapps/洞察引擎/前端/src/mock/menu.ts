export const mockMenu = [
  {
    icon: 'icon-zhukongtai',
    name: '主控台',
    path: '/home'
  },
  {
    icon: 'menus-database-2-fill',
    name: '数据中心',
    path: '/dataCenter',
    children: [
      {
        icon: '',
        name: '数据查询',
        path: '/dataCenter/dataQuery'
      },
      {
        icon: '',
        name: '数据处理',
        path: '/dataCenter/insDataSource'
      }
    ]
  },
  {
    icon: 'menus-operation-management-fill',
    name: '运营管理',
    path: '/operationManagement',
    children: [
      {
        icon: '',
        name: '新词发现',
        path: '/operationManagement/discovery'
      }
    ]
  },
  {
    icon: 'menus-knowledge-center-fill',
    name: '知识中心',
    path: '/knowledgeCenter',
    children: [
      {
        icon: '',
        name: '标签体系',
        path: '/knowledgeCenter/experienceCode'
      },
      {
        icon: '',
        name: '标准观点',
        path: '/knowledgeCenter/standardPoint'
      },
      {
        icon: '',
        name: '语料映射',
        path: '/knowledgeCenter/corpusMapping'
      },
      {
        icon: '',
        name: '用户旅程',
        path: '/knowledgeCenter/userJourney'
      },
      {
        icon: '',
        name: '用车场景',
        path: '/knowledgeCenter/carUsageScenarios'
      },
      {
        icon: '',
        name: '品牌车系',
        path: '/knowledgeCenter/brandSeries'
      },
      {
        icon: '',
        name: '关键词库',
        path: '/knowledgeCenter/keywordLibrary'
      }
    ]
  },
  {
    icon: 'menus-ruler-fill',
    name: '规则引擎',
    path: '/rules',
    children: [
      {
        icon: '',
        name: '规则测试',
        path: '/rules/rulesTest'
      },
      {
        icon: '',
        name: '清洗规则',
        path: '/rules/cleaningRules'
      }
    ]
  },
  {
    icon: 'menus-settings-6-fill',
    name: '系统设置',
    path: '/settings',
    children: [
      {
        icon: '',
        name: '账号管理',
        path: '/settings/accountManagement'
      },
      {
        icon: '',
        name: '角色管理',
        path: '/settings/role'
      },
      {
        icon: '',
        name: '下载管理',
        path: '/settings/download'
      }
    ]
  }
]

export const userPermissionsRes = {
  userId: null,
  username: null,
  roleId: null,
  roleName: null,
  email: null,
  phone: null,
  systemIcon: null,
  headPortrait: null,
  systemName: null,
  clientIds: {
    key: 'clientId',
    details: [
      {
        key: '0',
        value: '标准',
        code: 'system'
      },
      {
        key: '764547797eb2e192763f5334028d49c9',
        value: '东风日产',
        code: 'dndc'
      }
    ]
  },
  isAdmin: false,
  defaultClientId: '764547797eb2e192763f5334028d49c9',
  defaultClientIdText: '东风日产',
  menus: [
    {
      id: 'd507ba01d63ad1637b08f17e5cd58d60',
      pid: '0',
      icon: 'menus-database-2-fill',
      name: '数据中心',
      path: '/dataCenter',
      apiPath: null,
      permissionKey: 'dataCenter',
      sort: 2,
      children: [
        {
          id: 'm_dc_dq',
          pid: 'd507ba01d63ad1637b08f17e5cd58d60',
          icon: null,
          name: '数据查询',
          path: '/dataCenter/dataQuery',
          apiPath: null,
          permissionKey: 'dataCenter-dataQuery',
          sort: 1,
          children: null
        },
        {
          id: 'm_dc_data_source',
          pid: 'd507ba01d63ad1637b08f17e5cd58d60',
          icon: null,
          name: '数据处理',
          path: '/dataCenter/insDataSource',
          apiPath: null,
          permissionKey: 'dataCenter-insDataSource',
          sort: 2,
          children: null
        }
      ]
    },
    {
      id: 'm_operation',
      pid: '0',
      icon: 'menus-operation-management-fill',
      name: '运营管理',
      path: '/operationManagement',
      apiPath: null,
      permissionKey: 'operationManagement',
      sort: 3,
      children: [
        {
          id: 'm_operation_discovery',
          pid: 'm_operation',
          icon: null,
          name: '新词发现',
          path: '/operationManagement/discovery',
          apiPath: null,
          permissionKey: 'operationManagement-discovery',
          sort: 1,
          children: null
        }
      ]
    },
    {
      id: 'm_knowledge',
      pid: '0',
      icon: 'menus-knowledge-center-fill',
      name: '知识中心',
      path: '/knowledgeCenter',
      apiPath: null,
      permissionKey: 'knowledgeCenter',
      sort: 4,
      children: [
        {
          id: 'm_kc_tag',
          pid: 'm_knowledge',
          icon: null,
          name: '标签体系',
          path: '/knowledgeCenter/experienceCode',
          apiPath: null,
          permissionKey: 'knowledgeCenter-experienceCode',
          sort: 1,
          children: null
        },
        {
          id: 'm_kc_sp',
          pid: 'm_knowledge',
          icon: null,
          name: '标准观点',
          path: '/knowledgeCenter/standardPoint',
          apiPath: null,
          permissionKey: 'knowledgeCenter-standardPoint',
          sort: 2,
          children: null
        },
        {
          id: 'm_kc_corpus',
          pid: 'm_knowledge',
          icon: null,
          name: '语料映射',
          path: '/knowledgeCenter/corpusMapping',
          apiPath: null,
          permissionKey: 'knowledgeCenter-corpusMapping',
          sort: 3,
          children: null
        },
        {
          id: 'm_kc_journey',
          pid: 'm_knowledge',
          icon: null,
          name: '用户旅程',
          path: '/knowledgeCenter/userJourney',
          apiPath: null,
          permissionKey: 'knowledgeCenter-userJourney',
          sort: 4,
          children: null
        },
        {
          id: 'm_kc_scene',
          pid: 'm_knowledge',
          icon: null,
          name: '用车场景',
          path: '/knowledgeCenter/carUsageScenarios',
          apiPath: null,
          permissionKey: 'knowledgeCenter-carUsageScenarios',
          sort: 5,
          children: null
        },
        {
          id: 'm_kc_bs',
          pid: 'm_knowledge',
          icon: null,
          name: '品牌车系',
          path: '/knowledgeCenter/brandSeries',
          apiPath: null,
          permissionKey: 'knowledgeCenter-brandSeries',
          sort: 6,
          children: null
        },
        {
          id: 'm_kc_kw',
          pid: 'm_knowledge',
          icon: null,
          name: '关键词库',
          path: '/knowledgeCenter/keywordLibrary',
          apiPath: null,
          permissionKey: 'knowledgeCenter-keywordLibrary',
          sort: 7,
          children: null
        }
      ]
    },
    {
      id: 'm_rules',
      pid: '0',
      icon: 'menus-ruler-fill',
      name: '规则引擎',
      path: '/rules',
      apiPath: null,
      permissionKey: 'rules',
      sort: 5,
      children: [
        {
          id: 'm_rules_rt',
          pid: 'm_rules',
          icon: null,
          name: '规则测试',
          path: '/rules/rulesTest',
          apiPath: null,
          permissionKey: 'rules-rulesTest',
          sort: 1,
          children: null
        },
        {
          id: 'm_rules_clean',
          pid: 'm_rules',
          icon: null,
          name: '清洗规则',
          path: '/rules/cleaningRules',
          apiPath: null,
          permissionKey: 'rules-cleaningRules',
          sort: 2,
          children: null
        }
      ]
    },
    {
      id: '72a8d2c30d63cb282a40d13bff96e8e0',
      pid: '0',
      icon: 'menus-settings-6-fill',
      name: '系统设置',
      path: '/settings',
      apiPath: null,
      permissionKey: 'settings',
      sort: 6,
      children: [
        {
          id: 'a2f83815acfe0f62d8f4d494b283a2f7',
          pid: '72a8d2c30d63cb282a40d13bff96e8e0',
          icon: null,
          name: '账号管理',
          path: '/settings/accountManagement',
          apiPath: '/accountInfo/**',
          permissionKey: 'settings-accountManagement',
          sort: 1,
          children: null
        },
        {
          id: 'bd84a06639d88854618de46090a1170c',
          pid: '72a8d2c30d63cb282a40d13bff96e8e0',
          icon: null,
          name: '角色管理',
          path: '/settings/role',
          apiPath: '/role/**',
          permissionKey: 'settings-role',
          sort: 2,
          children: null
        },
        {
          id: 'm_set_dl',
          pid: '72a8d2c30d63cb282a40d13bff96e8e0',
          icon: null,
          name: '下载管理',
          path: '/settings/download',
          apiPath: null,
          permissionKey: 'settings-download',
          sort: 3,
          children: null
        }
      ]
    }
  ],
  button: [
    'settings-area-select',
    'dataCenter-resources-add',
    'dataCenter-resources-delete',
    'settings-channelConfig-add',
    'tagManagement-application-add',
    'tagManagement-keywords-select',
    'settings-channelConfig-delete',
    'settings-baseSettings-channelConfig',
    'dataCenter-carSeries-delete',
    'dataCenter-standard-disable',
    'dataCenter-customized-delete',
    'dataCenter-founding-resources',
    'dataCenter-customized-edit',
    'settings-baseSettings-area',
    'dataCenter-carSeries-edit',
    'settings-baseSettings-labelType',
    'settings-labelType-select',
    'settings-accountManagement-edit',
    'dataCenter-carSeries-add',
    'project-projectList-select',
    'dataCenter-customized-test',
    'settings-baseSettings-severityLevel',
    'tagManagement-application-select',
    'settings-severityLevel-select',
    'tagManagement-discovery-keywords',
    'system_integration-all',
    'system_integration',
    'dataCenter-customized-add',
    'local_upload',
    'dataCenter-customized-copy',
    'dataCenter-resources-edit',
    'settings-role-select',
    'rules-cleaningRules-select',
    'dataCenter-customized-select',
    'settings-baseSettings-userJourney',
    'tagManagement-keywords-add',
    'dataCenter-carSeries-select',
    'dataCenter-standard-regulation',
    'settings-channelConfig-edit',
    'dataCenter-resources-select',
    'project-projectList-edit',
    'settings-baseSettings-energyType',
    'dataCenter-customized-regulation',
    'dataCenter-customized-enable',
    'settings-carType-select',
    'settings-accountManagement-select',
    'settings-role-add',
    'settings-userJourney-select',
    'dataCenter-dataSource-delete',
    'settings-channelConfig-select',
    'tagManagement-keywords-edit',
    'dataCenter-dataSource-add',
    'settings-baseSettings-carType',
    'tagManagement-application-batch',
    'settings-role-edit',
    'dataCenter-standard-select',
    'tagManagement-application-edit',
    'dataCenter-dataSource-import',
    'dataCenter-dataSource-edit',
    'settings-energyType-select',
    'dataCenter-dataSource-select',
    'tagManagement-application-del',
    'settings-accountManagement-add',
    'system_integration-select',
    'dataCenter-customized-check',
    'dataCenter-resources-enable',
    'dataCenter-founding-carSeries'
  ]
}
