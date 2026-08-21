import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { constantRoutes } from './constantRoutes'
import { dynamicRoutes } from './dynamicRoutes'
import { isLocalDemo } from '@/utils/env'

// 基础路由
const routes: RouteRecordRaw[] = isLocalDemo()
  ? [...constantRoutes.filter(route => route.name !== 'Root'), ...dynamicRoutes]
  : [...constantRoutes]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
