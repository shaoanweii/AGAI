import { dynamicRoutes } from '@/router/dynamicRoutes'
import type { RouteRecordRaw } from 'vue-router'

export const LOCAL_DEMO_TOKEN = 'voc-voice-local-demo-token'

const defaultFilters = [
  { filterType: '93', value: ['2'], name: '时间' },
  { filterType: '91', value: ['voc-brand-zhixing'], name: '品牌' },
  { filterType: '92', value: ['voc-product-001'], name: '标签' }
]

const localDemoMenuOrder = [
  'vocView',
  'leaderOverview',
  'sceneAnalysis',
  'selfServiceAnalysis',
  'CustomerDirectEngage',
  'system'
]

const toDictOptions = (values: string[]) =>
  values.map((text, index) => ({
    key: String(index),
    value: String(index),
    label: text,
    text,
    name: text,
    code: String(index)
  }))

const toDemoMenu = (route: RouteRecordRaw, index: number): Record<string, any> => ({
  id: `demo-menu-${String(route.name || index)}`,
  name: String(route.meta?.title || route.name || '菜单'),
  permissionKey: String(route.name || ''),
  path: route.path,
  htmlUri: route.path,
  icon: String(route.meta?.icon || ''),
  sort: index + 1,
  jsonObject: defaultFilters,
  children: route.children?.map((child, childIndex) => toDemoMenu(child, childIndex)) || []
})

/**
 * 创建本地演示账号的完整菜单和基础业务上下文。
 */
export const createLocalDemoSession = () => ({
  accessToken: LOCAL_DEMO_TOKEN,
  userId: 'demo-admin',
  username: '演示管理员',
  name: '演示管理员',
  roleId: 'demo-super-admin',
  clientIds: { details: ['voc-voice'] },
  defaultClientId: 'voc-voice',
  isAdmin: true,
  button: ['*'],
  functionPermission: ['*'],
  brands: {
    details: [
      {
        label: '智行',
        key: 'voc-brand-zhixing',
        value: 'voc-brand-zhixing',
        code: 'voc-brand-zhixing',
        children: [
          { label: '智行 S7', key: 'voc-series-s7', value: 'voc-series-s7', code: 'voc-series-s7' },
          { label: '智行 X5', key: 'voc-series-x5', value: 'voc-series-x5', code: 'voc-series-x5' }
        ]
      },
      {
        label: '远途',
        key: 'voc-brand-yuantu',
        value: 'voc-brand-yuantu',
        code: 'voc-brand-yuantu',
        children: [{ label: '远途 M8', key: 'voc-series-m8', value: 'voc-series-m8', code: 'voc-series-m8' }]
      },
      {
        label: '凌峰',
        key: 'voc-brand-lingfeng',
        value: 'voc-brand-lingfeng',
        code: 'voc-brand-lingfeng',
        children: [{ label: '凌峰 C6', key: 'voc-series-c6', value: 'voc-series-c6', code: 'voc-series-c6' }]
      }
    ]
  },
  timeDimension: [
    { code: '2', name: '近7天' },
    { code: '3', name: '近30天' },
    { code: '12', name: '本月' }
  ],
  menus: [...dynamicRoutes]
    .sort(
      (first, second) =>
        localDemoMenuOrder.indexOf(String(first.name)) -
        localDemoMenuOrder.indexOf(String(second.name))
    )
    .map((route, index) => toDemoMenu(route, index)),
  allDictItems: {
    sentiment: [
      { value: 'positive', label: '正面' },
      { value: 'neutral', label: '中性' },
      { value: 'negative', label: '负面' }
    ],
    channel: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'].map(value => ({
      value,
      label: value
    })),
    report_release_staus: toDictOptions(['待审核', '已发布', '已下架', '未通过']),
    task_event_staus: toDictOptions(['待处理', '处理中', '待确认', '已完成']),
    closed_rule_priority: ['P0', 'P1', 'P2', 'P3'].map(value => ({ key: value, value, label: value, text: value, name: value, code: value })),
    closed_rule_level: toDictOptions(['高', '中', '低']),
    task_event_validity: toDictOptions(['有效', '无效']),
    task_event_is_handled: toDictOptions(['是', '否']),
    task_event_approve_process_mode: toDictOptions(['直接处理', '转交处理']),
    task_event_private_mst_count: toDictOptions(['0次', '1次', '2次及以上']),
    task_event_private_mst_staus: toDictOptions(['未联系', '已联系', '已回复']),
    task_event_review_staus: toDictOptions(['未回评', '已回评']),
    task_event_approve_close_reason: toDictOptions(['问题已解决', '转专项处理']),
    task_event_reject_reason: toDictOptions(['信息不完整', '责任部门不匹配']),
    task_event_close_reason: toDictOptions(['问题已解决', '已形成专项改善']),
    batch_event_status: toDictOptions(['待审批', '待分派', '处理中', '待确认', '已关闭']),
    batchEvent_event_validity: toDictOptions(['有效', '无效']),
    batchEvent_reject_reason_type: toDictOptions(['数据依据不足', '责任范围不符']),
    batchEvent_close_reason_type: toDictOptions(['目标达成', '专项结项']),
    batchEvent_warning_rate: toDictOptions(['正常', '临期', '超期']),
    batchEvent_is_rejected: toDictOptions(['否', '是']),
    event_attribute: toDictOptions(['产品问题', '服务问题', '舆情问题']),
    voc_sentiment: ['正面', '中性', '负面'].map(value => ({ key: value, value, label: value, text: value, name: value, code: value })),
    voc_intention: ['抱怨', '咨询', '建议', '表扬'].map(value => ({ key: value, value, label: value, text: value, name: value, code: value }))
  }
})
