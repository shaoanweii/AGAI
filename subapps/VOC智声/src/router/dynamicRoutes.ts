const Layout = () => import('@/layout/index.vue')
/**
 * 动态路由
 * 后端配置菜单时，permissionKey 需与 dynamicRoutes[] 中的name 保持一致
 */
export const dynamicRoutes = [
  {
    path: '/',
    name: 'vocView',
    component: Layout,
    redirect: '/overview',
    meta: {
      title: 'VOC总览',
      icon: 'menu-home-smile-fill',
      alwaysShow: true
    },
    children: [
      {
        path: '/overview',
        name: 'overview',
        component: () => import('@/views/overview/index.vue'),
        meta: { title: 'VOC总览', icon: 'menu-home-smile-fill' }
      }
    ]
  },
  {
    path: '/leaderOverview',
    name: 'leaderOverview',
    component: Layout,
    redirect: '/leaderOverview',
    meta: {
      title: '领导总览',
      icon: 'menu-home-smile-fill',
      alwaysShow: true
    },
    children: [
      {
        path: '/leaderOverview',
        name: 'leaderOverviewPage',
        component: () => import('@/views/leaderOverview/index.vue'),
        meta: { title: '领导总览', icon: 'menu-home-smile-fill' }
      }
    ]
  },
  {
    path: '/scene',
    name: 'sceneAnalysis',
    component: Layout,
    redirect: '/scene/analysis',
    meta: { title: '场景分析', icon: 'menu-function-add-fill' },
    children: [
      {
        path: '/scene/analysis',
        name: 'sceneAnalysisMain',
        component: () => import('@/views/sceneAnalysis/index.vue'),
        meta: { title: '场景分析', icon: 'House' }
      },
      {
        path: '/scene/groupAnalysis',
        name: 'groupAnalysis',
        component: () => import('@/views/sceneAnalysis/GroupAnalysis.vue'),
        meta: { title: '集团分析', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/thisProductAnalysis',
        name: 'thisProductAnalysis',
        component: () => import('@/views/sceneAnalysis/ThisProductAnalysis.vue'),
        meta: { title: '本品分析', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/competitorAnalysis',
        name: 'competitorAnalysis',
        component: () => import('@/views/sceneAnalysis/CompetitorAnalysis.vue'),
        meta: { title: '竞品对比', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/journeyAnalysis',
        name: 'journeyAnalysis',
        component: () => import('@/views/sceneAnalysis/JourneyAnalysis.vue'),
        meta: { title: '旅程分析', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/productAnalysis',
        name: 'productAnalysis',
        component: () => import('@/views/sceneAnalysis/ProductAnalysis.vue'),
        meta: { title: '产品分析', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/serviceAnalysis',
        name: 'serviceAnalysis',
        component: () => import('@/views/sceneAnalysis/ServiceAnalysis.vue'),
        meta: { title: '服务分析', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/newCarLaunch',
        name: 'newCarLaunch',
        component: () => import('@/views/sceneAnalysis/NewCarLaunch.vue'),
        meta: { title: '新车上市', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/mainAccount',
        name: 'mainAccount',
        component: () => import('@/views/sceneAnalysis/MainAccount.vue'),
        meta: { title: '重点账号', icon: 'House', sceneReportDetail: true }
      },
      {
        path: '/scene/hotEvents',
        name: 'hotEvents',
        component: () => import('@/views/sceneAnalysis/HotEvents.vue'),
        meta: { title: '热点事件', icon: 'House' }
      }
    ]
  },
  {
    path: '/selfService',
    name: 'selfServiceAnalysis',
    component: Layout,
    redirect: '/rootCause',
    meta: { title: '自助分析', icon: 'menu-robot-3-fill' },
    children: [
      {
        path: '/rootCause',
        name: 'rootCause',
        component: () => import('@/views/rootCause/index.vue'),
        meta: { title: '根因分析', icon: 'search-eye-line', sceneReportDetail: true }
      },
      /**
       * 原声查询菜单sql(将声音标记菜单更新为原声查询菜单)
       * UPDATE voc_ms_td.sta_sys_menu_permission
SET parent_id='c0c767c11416a2e6d5a0f7d2ff9bec00', name='原声查询', html_uri='/selfService/originalSoundQuery', api_url='/highQuality/*', sort_no=12, icon='House', last_level=0, app_id='report', create_time='2024-09-18 10:43:18', permission_key='selfServiceOriginalSoundQuery', del_flag='0', filter_status=0, big_image=NULL, description=NULL, small_image=NULL, require_filter_types=NULL
WHERE id='c0c767c11416a2e6d5a0f7d2ff9bec59';
       */
      {
        path: '/selfService/originalSoundQuery',
        name: 'selfServiceOriginalSoundQuery',
        component: () => import('@/views/selfService/originalSoundQuery/index.vue'),
        meta: { title: '原声查询', icon: 'search-eye-line' }
      },
      {
        path: '/selfService/closedLoopEvaluation',
        name: 'selfServiceClosedLoopEvaluation',
        component: () => import('@/views/selfService/closedLoopEvaluation/index.vue'),
        meta: { title: '闭环评价', icon: 'search-eye-line' }
      },
      {
        path: '/selfService/localDataAnalysis',
        name: 'selfServiceLocalDataAnalysis',
        component: () => import('@/views/selfService/localDataAnalysis/index.vue'),
        meta: { title: '导入分析', icon: 'search-eye-line' }
      }
    ]
  },
  {
    path: '/system',
    name: 'system',
    component: Layout,
    meta: { title: '系统管理', icon: 'menu-settings-fill' },
    children: [
      {
        path: '/system/user',
        name: 'UserManagement',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '账号管理', icon: 'User' }
      },
      {
        path: '/system/role',
        name: 'RoleManagement',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: '/system/downloadManagement',
        name: 'sysDownloadManagement',
        component: () => import('@/views/system/downloadManagement/index.vue'),
        meta: { title: '下载管理', icon: 'UserFilled' }
      },
      {
        path: '/system/scene',
        name: 'sceneManagement',
        component: () => import('@/views/system/scene/index.vue'),
        meta: { title: '场景管理', icon: 'UserFilled' }
      },
      {
        path: '/system/report',
        name: 'srReportManagement',
        component: () => import('@/views/system/report/index.vue'),
        meta: { title: '报告管理', icon: 'UserFilled' }
      },
      {
        path: '/system/dataSquare',
        name: 'systemDataSquare',
        component: () => import('@/views/system/dataSquare/index.vue'),
        meta: { title: '看数广场', icon: 'UserFilled' }
      },
      {
        path: '/system/pushManagement',
        name: 'sysPushManagement',
        component: () => import('@/views/system/pushManagement/index.vue'),
        meta: { title: '推送管理', icon: 'UserFilled' }
      },
      {
        path: '/system/logQuery',
        name: 'sysLogQuery',
        component: () => import('@/views/system/logQuery/index.vue'),
        meta: { title: '日志查询', icon: 'UserFilled' }
      },
      {
        path: '/system/configuration',
        name: 'configurationManagement',
        component: () => import('@/views/system/configuration/index.vue'),
        meta: { title: '系统配置', icon: 'UserFilled' }
      },
      {
        path: '/system/subscribe',
        name: 'subscribeManagement',
        component: () => import('@/views/system/subscribe/index.vue'),
        meta: { title: '订阅管理', icon: 'UserFilled' }
      },
      /**
       * 声音标记菜单sql
       * UPDATE voc_ms_td.sta_sys_menu_permission
SET parent_id='22a85c723de691f442bb8464d3691098', name='声音标记', html_uri='/system/voice', api_url='/highQuality/*', sort_no=10, icon='House', last_level=0, app_id='report', create_time='2024-09-18 10:43:18', permission_key='voiceManagement', del_flag='0', filter_status=0, big_image=NULL, description=NULL, small_image=NULL, require_filter_types=NULL
WHERE id='c0c767c11416a2e6d5a0f7d2ff9bec59';
       */
      // {
      //   path: '/system/voice',
      //   name: 'voiceManagement',
      //   component: () => import('@/views/system/voice/index.vue'),
      //   meta: { title: '声音标记', icon: 'UserFilled' }
      // },
    ]
  },
  {
    path: '/customerDirectEngage',
    name: 'CustomerDirectEngage',
    component: Layout,
    meta: { title: '客情直驱', icon: 'menu-team-fill' },
    children: [
      {
        path: '/customerDirectEngage/singlePointEvent',
        name: 'CDESinglePointEvent',
        component: () => import('@/views/customerDirectEngage/singlePointEvent/index.vue'),
        meta: { title: '单点事件', icon: 'menu-team-fill' }
      },
      {
        path: '/customerDirectEngage/batchEvent',
        name: 'CDEBatchEvent',
        component: () => import('@/views/customerDirectEngage/batchEvent/index.vue'),
        meta: { title: '批量事件', icon: 'menu-team-fill' }
      }
    ]
  }
]
