import { h5Routes } from '@/views/H5/router/index'
import { isDev } from '@/utils/env'
const Layout = () => import('@/layout/index.vue')

/**
 * 静态路由,公共路由
 */

// 404路由
export const notFoundRoute = {
  path: '/:pathMatch(.*)*',
  name: 'notFound',
  redirect: '/404'
}

// 开发环境展示的路由
const devRputer = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/data',
    name: 'Data',
    meta: { title: '数据管理', icon: 'DataAnalysis' },
    children: [
      {
        path: '/data/ui-showcase',
        name: 'UIShowcase',
        component: () => import('@/views/data/UIShowcase/index.vue'),
        meta: { title: 'UI组件展示', icon: 'Grid' }
      },
      {
        path: '/data/dictionary',
        name: 'DictionaryManagement',
        component: () => import('@/views/data/dictionary/index.vue'),
        meta: { title: '数据字典', icon: 'CollectionTag' }
      }
    ]
  }
]

// 默认存在的路由
export const constantRoutes = [
  {
    path: '/',
    name: 'Root',
    redirect: '/overview' // 默认重定向，会被动态路由覆盖
  },
  // {
  //   path: '/login',
  //   name: 'Login',
  //   component: () => import('@/views/login/index.vue'),
  //   meta: { title: '登录' }
  // },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  },
  {
    path: '/redirect',
    name: 'redirect',
    meta: {
      hidden: true
    },
    component: () => import('@/views/redirect/index.vue')
  },
  // 过渡跳转页面 用于其他直接访问这个系统注入筛选项
  {
    path: '/transitionView',
    name: 'transitionView',
    meta: { title: '过渡' },
    component: () => import('@/views/transitionView/index.vue')
  },
  {
    path: '/hotView',
    name: 'hotView',
    component: Layout,
    redirect: '/hotView/hotDetailEvents',
    meta: { title: '查看', icon: 'House', hidden: true },
    children: [
      {
        path: '/hotView/hotDetailEvents',
        name: 'hotDetailEvents',
        meta: { title: '热点事件查看', icon: 'House', hidden: true },
        component: () => import('@/views/sceneAnalysis/HotDetailEvents.vue')
      }
    ]
  },
  // 开发环境展示的路由
  ...(isDev() ? devRputer : []),
  // H5路由配置 - 从独立文件导入
  ...h5Routes
]
