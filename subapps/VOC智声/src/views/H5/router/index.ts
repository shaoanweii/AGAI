/**
 * H5端专用路由配置
 * 基于多端分离架构，实现H5端独立路由管理
 */
export const h5Routes = [
  {
    path: '/h5',
    name: 'H5Layout',
    component: () => import('@h5/layout/H5Layout.vue'),
    redirect: '/h5/home',
    children: [
      {
        path: 'home',
        name: 'H5Home',
        component: () => import('@h5/views/home-v1/index.vue'),
        meta: {
          title: 'VOC移动端-首页',
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: 'task',
        name: 'H5TaskEvent',
        component: () => import('@h5/views/taskEvent/index.vue'),
        meta: {
          title: 'VOC移动端-事件任务',
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: 'dataSquare',
        name: 'H5DataSquare',
        component: () => import('@h5/views/dataSquare/index.vue'),
        meta: {
          title: '看数广场',
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: 'dataSquare/search',
        name: 'H5DataSquareSearch',
        component: () => import('@h5/views/dataSquare/search/index.vue'),
        meta: {
          title: '看数广场-搜索',
          keepAlive: true,
          showTabBar: true
        }
      },
      {
        path: 'dataSquare/category',
        name: 'H5DataSquareCategory',
        component: () => import('@h5/views/dataSquare/category/index.vue'),
        meta: {
          title: '看数广场-分类详情',
          keepAlive: true,
          showTabBar: false
        }
      },
      {
        path: 'dataSquare/reportDetail',
        name: 'H5DataSquareReportDetail',
        component: () => import('@h5/views/dataSquare/reportDetail/index.vue'),
        meta: {
          title: '数据报告详情',
          keepAlive: true,
          showTabBar: false
        }
      },
      {
        path: 'taskEventDetail',
        name: 'H5TaskEventDetail',
        component: () => import('@h5/views/taskEventDetail/index.vue'),
        meta: {
          title: 'VOC移动端-事件详情',
          keepAlive: true,
          showTabBar: false
        }
      },
      {
        path: 'taskBatchEventDetail',
        name: 'H5TaskBatchEventDetail',
        component: () => import('@h5/views/taskBatchEventDetail/index.vue'),
        meta: {
          title: 'VOC移动端-批量事件详情',
          keepAlive: true,
          showTabBar: false
        }
      },
      {
        path: 'analysisAndVoice',
        name: 'H5AnalysisAndVoice',
        component: () => import('@h5/views/analysisAndVoice/index.vue'),
        meta: {
          title: 'H5AnalysisAndVoice',
          keepAlive: false
        }
      },
      {
        path: 'taskDetail',
        name: 'H5TaskDetail',
        component: () => import('@h5/views/taskDetail/index.vue'),
        meta: {
          title: 'VOC移动端-任务完成率',
          keepAlive: false
        }
      },
      //声音列表
      {
        path: 'soundList/:tag?',
        name: 'H5AllVoiceList',
        component: () => import('@h5/views/allVoiceList/index.vue'),
        meta: {
          title: 'H5AllVoiceList',
          keepAlive: false
        }
      },
      //声音详情
      {
        path: 'voiceDetail',
        name: 'H5VoiceDetail',
        component: () => import('@h5/views/voiceDetail/index.vue'),
        meta: {
          title: 'VOC移动端-声音详情',
          keepAlive: true
        }
      },
      //行业品牌排行
      {
        path: 'brandRanking',
        name: 'H5BrandRanking',
        component: () => import('@h5/views/brandRanking/index.vue'),
        meta: {
          title: 'H5IndustryRanking',
          keepAlive: false
        }
      },
      // 报表页面
      {
        path: 'report',
        name: 'H5Report',
        component: () => import('@h5/views/report/index.vue'),
        meta: {
          title: 'H5Report',
          keepAlive: false
        }
      },
      {
        path: '/h5404',
        name: 'H5404',
        component: () => import('@h5/views/error/404.vue'),
        meta: { title: '页面不存在' }
      },
      {
        path: 'originalView',
        name: 'H5OriginalView',
        component: () => import('@h5/views/originalView/index.vue'),
        meta: {
          title: 'H5OriginalView'
        }
      }
    ]
  },
  {
    path: '/h5Rct',
    name: 'H5Rct',
    component: () => import('@h5/views/redirect/index.vue'),
    meta: {
      title: 'h5Rct'
    }
  },
  {
    path: '/h5LinkCanswer',
    name: 'H5LinkCanswer',
    component: () => import('@h5/views/linkCanswer/index.vue'),
    meta: {
      title: 'VOC移动端-智能问数'
    }
  },
  {
    path: '/h5PdfView',
    name: 'H5PdfView',
    component: () => import('@h5/views/pdfView/index.vue'),
    meta: {
      title: 'H5PdfView'
    }
  },
  {
    path: '/h5NotAuth',
    name: 'H5NotAuthPage',
    component: () => import('@h5/views/notAuth/index.vue'),
    meta: { title: '暂无权限' }
  }
]

/**
 * H5路由导航守卫配置
 */
export const h5RouteGuards = {
  /**
   * H5路由前置守卫
   */
  beforeEnter: (to: any, from: any, next: any) => {
    next()
  }
}

export default h5Routes
