import { createRouter, createWebHashHistory } from 'vue-router'
import Main from '@/layouts/Main.vue'
import Home from '../views/home/Home.vue'
import { isDev } from '@/utils/env'

/**
 * meta字段说明
 * title 菜单名称
 * icon
 * alwaysShow 适用于children只有一个的时候， 不用展示子级
 */

export const notFoundRoute = {
  path: '/:pathMatch(.*)*',
  name: 'notFound',
  redirect: '/404'
}

/**
 * 公共路由
 */
const constantRoutes = [
  {
    path: '/',
    // name: 'main',
    component: Main,
    redirect: '/home',
    meta: {
      title: '主控台',
      icon: 'menus-instance-fill',
      alwaysShow: true
    },
    children: [
      {
        path: '/home',
        name: 'home',
        component: Home,
        meta: {
          title: '主控台',
          icon: 'menus-instance-fill'
        }
      }
    ]
  },
  // {
  //   path: '/exception',
  //   name: 'exception',
  //   component: Main,
  //   redirect: '/exception/404',
  //   children: [
  //
  //   ]
  // },
  {
    path: '/403',
    name: '403',
    meta: {
      hidden: true
    },
    component: () => import('@/views/exception/403.vue')
  },
  {
    path: '/404',
    name: '404',
    meta: {
      hidden: true
    },
    component: () => import('@/views/exception/404.vue')
  },
  {
    path: '/500',
    name: '500',
    meta: {
      hidden: true
    },
    component: () => import('@/views/exception/500.vue')
  },
  {
    path: '/redirect',
    name: 'redirect',
    meta: {
      hidden: true
    },
    component: () => import('@/views/redirect/index.vue')
  },
  ...(isDev()
    ? [
        {
          path: '/login',
          name: 'login',
          meta: {
            hidden: true
          },
          component: () => import('../views/Login.vue')
        }
      ]
    : [])
  // notFoundRoute
]

/**
 * 动态路由
 * 后端配置菜单时，permissionKey 需与 dynamicRoutes[] 中的name 保持一致
 */
export const dynamicRoutes = [
  {
    path: '/dataCenter',
    name: 'dataCenter',
    component: Main,
    redirect: '/dataCenter/dataQuery',
    meta: {
      title: '数据中心',
      icon: 'menus-database-2-fill'
    },
    children: [
      {
        path: '/dataCenter/dataQuery',
        name: 'dataCenter-dataQuery',
        component: () => import('@/views/dataQuery/index.vue'),
        meta: {
          title: '数据查询',
          icon: 'menus-database-2-fill'
        }
      },
      {
        path: '/dataCenter/insDataSource',
        name: 'dataCenter-insDataSource',
        component: () => import('@/views/dataCenter/dataProcessing/index.vue'),
        meta: {
          title: '数据处理',
          icon: 'menus-database-2-fill'
        }
      }
      // {
      //   path: '/dataCenter/processing', //规则处理
      //   name: 'dataCenter-processing',
      //   meta: {
      //     title: '规则处理',
      //     icon: ''
      //   },
      //   component: () => import('../views/dataCenter/dataProcessing/index.vue')
      // },
      // {
      //   path: '/dataCenter/founding', //数据资产
      //   name: 'dataCenter-founding',
      //   meta: {
      //     title: '数据资产',
      //     icon: ''
      //   },
      //   component: () => import('../views/dataCenter/dataFounding/index.vue')
      // }
    ]
  },
  {
    path: '/operationManagement',
    name: 'operationManagement',
    component: Main,
    redirect: '/operationManagement/discovery',
    meta: {
      title: '运营管理',
      icon: 'menus-operation-management-fill'
    },
    children: [
      {
        path: '/operationManagement/discovery',
        alias: '/dataCenter/discovery',
        name: 'operationManagement-discovery',
        component: () => import('@/views/dataCenter/discovery/index.vue'),
        meta: {
          title: '新词发现',
          icon: 'menus-operation-management-fill'
        }
      }
    ]
  },
  {
    path: '/knowledgeCenter',
    name: 'knowledgeCenter',
    component: Main,
    redirect: '/knowledgeCenter/standardPoint',
    meta: {
      title: '知识中心',
      icon: 'menus-knowledge-center-fill'
    },
    children: [
      {
        path: '/knowledgeCenter/experienceCode',
        name: 'knowledgeCenter-experienceCode',
        component: () => import('@/views/knowledgeCenter/experienceCode/index.vue'),
        meta: {
          title: '标签体系',
          icon: 'menus-knowledge-center-fill'
        }
      },
      {
        path: '/knowledgeCenter/standardPoint',
        name: 'knowledgeCenter-standardPoint',
        component: () => import('@/views/knowledgeCenter/standardPoint/index.vue'),
        meta: {
          title: '标准观点',
          icon: 'menus-knowledge-center-fill'
        }
      },
      {
        path: '/knowledgeCenter/corpusMapping',
        name: 'knowledgeCenter-corpusMapping',
        component: () => import('@/views/knowledgeCenter/corpusMapping/index.vue'),
        meta: {
          title: '语料映射',
          icon: 'menus-knowledge-center-fill'
        }
      },
      {
        path: '/knowledgeCenter/userJourney',
        name: 'knowledgeCenter-userJourney',
        component: () => import('@/views/knowledgeCenter/userJourney/index.vue'),
        meta: {
          title: '用户旅程',
          icon: 'menus-knowledge-center-fill'
        }
      },
      {
        path: '/knowledgeCenter/carUsageScenarios',
        name: 'knowledgeCenter-carUsageScenarios',
        component: () => import('@/views/knowledgeCenter/carUsageScenarios/index.vue'),
        meta: {
          title: '用车场景',
          icon: 'menus-knowledge-center-fill'
        }
      },
      {
        path: '/knowledgeCenter/brandSeries',
        name: 'knowledgeCenter-brandSeries',
        component: () => import('@/views/knowledgeCenter/brandSeries/index.vue'),
        meta: {
          title: '品牌车系',
          icon: 'menus-knowledge-center-fill'
        }
      },
      {
        path: '/knowledgeCenter/keywordLibrary',
        name: 'knowledgeCenter-keywordLibrary',
        component: () => import('@/views/knowledgeCenter/keywordLibrary/index.vue'),
        meta: {
          title: '关键词库',
          icon: 'menus-knowledge-center-fill'
        }
      }
    ]
  },
  // {
  //   path: '/tagManagement',
  //   name: 'tagManagement',
  //   component: Main,
  //   // redirect: '/tagManagement/library',
  //   redirect: '/tagManagement/application',
  //   meta: {
  //     title: '标签管理',
  //     icon: 'icon-biaoqianguanli',
  //     alwaysShow: true
  //   },
  //   children: [
  //     // {
  //     //   path: '/tagManagement/library', //标签库
  //     //   name: 'tagManagement-library',
  //     //   meta: {
  //     //     title: '标签库',
  //     //     icon: ''
  //     //   },
  //     //   component: () => import('../views/tagManagement/library/Index.vue')
  //     // },
  //     {
  //       path: '/tagManagement/application', // 标签应用
  //       name: 'tagManagement-application',
  //       meta: {
  //         title: '标签应用',
  //         icon: ''
  //       },
  //       // component: () => import('../views/tagManagement/application/index.vue')
  //       component: () => import('../views/tagManagement/index/index.vue')
  //     }
  //   ]
  // },
  {
    path: '/rules',
    name: 'rules',
    component: Main,
    redirect: '/rules/rulesTest',
    meta: {
      title: '规则引擎',
      icon: 'menus-ruler-fill'
    },
    children: [
      {
        path: '/rules/rulesTest', //规则测试
        name: 'rules-rulesTest',
        meta: {
          title: '规则测试',
          icon: 'menus-ruler-fill'
        },
        component: () => import('@/views/rules/rulesTest/index.vue')
      },
      {
        path: '/rules/cleaningRules', //清洗规则
        name: 'rules-cleaningRules',
        meta: {
          title: '清洗规则',
          icon: 'menus-ruler-fill'
        },
        component: () => import('@/views/rules/cleaningRules/index.vue')
      }
    ]
  },
  // {
  //   path: '/project',
  //   name: 'project',
  //   component: Main,
  //   // redirect: '/project/projectList',
  //   redirect: '/project/projectList',
  //   meta: {
  //     title: '项目应用',
  //     icon: 'icon-xiangmuyingyong'
  //   },
  //   children: [
  //     // {
  //     //   path: "/project/projectList", //项目列表
  //     //   name: "project-projectList",
  //     //   meta: {
  //     //     title: "项目列表",
  //     //     icon: "icon-xiangmuyingyong",
  //     //   },
  //     //   component: () => import("../views/project/projectList/Index.vue"),
  //     // },
  //     {
  //       path: '/project/projectList', //项目列表
  //       name: 'project-projectList',
  //       meta: {
  //         title: '项目管理',
  //         icon: 'icon-xiangmuyingyong'
  //       },
  //       component: () => import('../views/project/projectManagement/Index.vue')
  //     }
  //   ]
  // },
  {
    path: '/settings',
    name: 'settings',
    component: Main,
    redirect: '/settings/accountManagement',
    meta: {
      title: '系统设置',
      icon: 'menus-settings-6-fill'
    },
    children: [
      {
        path: '/settings/accountManagement', //账号管理
        name: 'settings-accountManagement',
        meta: {
          title: '账号管理',
          icon: 'menus-settings-6-fill'
        },
        component: () => import('../views/settings/accountManagement/Index.vue')
      },
      {
        path: '/settings/role', //角色管理
        name: 'settings-role',
        meta: {
          title: '角色管理',
          icon: 'menus-settings-6-fill'
        },
        component: () => import('../views/settings/role/Index.vue')
      },
      {
        path: '/settings/download',
        name: 'settings-download',
        meta: {
          title: '下载管理',
          icon: 'menus-settings-6-fill'
        },
        component: () => import('../views/settings/download/index.vue')
      }
      // {
      //   path: '/settings/projectList', //项目列表
      //   name: 'project-projectList',
      //   meta: {
      //     title: '项目管理',
      //     icon: 'icon-xiangmuyingyong'
      //   },
      //   component: () => import('../views/project/projectManagement/Index.vue')
      // },
      // {
      //   path: '/settings/baseSettings', //基础设置
      //   name: 'settings-baseSettings',
      //   meta: {
      //     title: '基础设置',
      //     icon: ''
      //   },
      //   component: () => import('../views/settings/baseSettings/Index.vue')
      // }
    ]
  }

  // {
  //   path: '/review',
  //   name: 'review',
  //   // component: Main,
  //   redirect: '/review/errorCorrection',
  //   meta: {
  //     title: '审核管理',
  //     icon: 'icon-shujuzhongxin'
  //   },
  //   children: [
  //     {
  //       path: '/review/errorCorrection', //规则处理
  //       name: 'review-errorCorrection',
  //       meta: {
  //         title: '纠错审核',
  //         icon: ''
  //       },
  //       component: () => import('@/views/review/errorCorrection/index.vue')
  //     }
  //   ]
  // }
]

const routes = [...constantRoutes, ...dynamicRoutes]
const router = createRouter({
  // history: createWebHistory(import.meta.env.BASE_URL),
  history: createWebHashHistory(),
  routes
})

export default router
