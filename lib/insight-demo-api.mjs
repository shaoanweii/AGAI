const DAY = '2026-08-21'

const pageNumber = value => Math.max(Number(value || 1) || 1, 1)
const pageSize = value => Math.max(Number(value || 20) || 20, 1)

/**
 * 统一生成洞察引擎分页结构，同时兼容历史页面读取 records 或 list 的两种方式。
 */
const pageResult = (rows, body = {}) => {
  const current = pageNumber(body.pageNum || body.current || body.page)
  const size = pageSize(body.pageSize || body.size || body.page_size)
  const start = (current - 1) * size
  const records = rows.slice(start, start + size)
  return { records, list: records, total: rows.length, current, size, pageNum: current, pageSize: size }
}

const repeatRows = (rows, total, prefix) =>
  Array.from({ length: total }, (_, index) => ({
    ...rows[index % rows.length],
    id: `${prefix}_${index + 1}`
  }))

const statusConditions = [
  {
    key: 'ruleStatus',
    details: [
      { key: 'Enabled', value: '启用' },
      { key: 'Disabled', value: '禁用' }
    ]
  },
  {
    key: 'stopOrEnable',
    details: [
      { key: '1', value: '启用' },
      { key: '0', value: '禁用' }
    ]
  },
  {
    key: 'enableType',
    details: [
      { key: '1', value: '启用' },
      { key: '0', value: '禁用' }
    ]
  }
]

const commonConditions = [
  ...statusConditions,
  {
    key: 'ruleType',
    details: [
      { key: '1', value: '清洗规则' },
      { key: '2', value: '闭环规则' }
    ]
  },
  {
    key: 'ruleTest',
    details: [
      { key: '0', value: '未开始' },
      { key: '1', value: '已完成' }
    ]
  },
  {
    key: 'isCore',
    details: [
      { key: '1', value: '是' },
      { key: '0', value: '否' }
    ]
  },
  {
    key: 'isNewCar',
    details: [
      { key: '1', value: '是' },
      { key: '0', value: '否' }
    ]
  },
  {
    key: 'competitiveType',
    details: [
      { key: '1', value: '本品' },
      { key: '2', value: '竞品' }
    ]
  },
  {
    key: 'brand',
    details: [
      { key: 'BRAND_LH', value: '领航品牌' },
      { key: 'BRAND_QC', value: '启程品牌' },
      { key: 'BRAND_XH', value: '星海汽车' }
    ]
  },
  {
    key: 'vocSentiment',
    details: [
      { key: '正向', value: '正向' },
      { key: '中性', value: '中性' },
      { key: '负向', value: '负向' }
    ]
  },
  {
    key: 'vocIntention',
    details: [
      { key: '抱怨', value: '抱怨' },
      { key: '建议', value: '建议' },
      { key: '咨询', value: '咨询' },
      { key: '表扬', value: '表扬' }
    ]
  }
]

const accountConditions = [
  {
    key: 'ruleStatus',
    details: [
      { key: '1', value: '启用' },
      { key: '0', value: '禁用' }
    ]
  },
  ...commonConditions.filter(item => item.key !== 'ruleStatus')
]

const channelTree = [
  {
    code: 'public',
    name: '公开渠道',
    child: [
      {
        code: 'social',
        name: '社交媒体',
        child: [
          { code: 'douyin', name: '抖音' },
          { code: 'xiaohongshu', name: '小红书' },
          { code: 'weibo', name: '微博' }
        ]
      },
      {
        code: 'auto_media',
        name: '汽车媒体',
        child: [
          { code: 'autohome', name: '汽车之家' },
          { code: 'dongchedi', name: '懂车帝' },
          { code: 'yiche', name: '易车' }
        ]
      }
    ]
  },
  {
    code: 'private',
    name: '私域渠道',
    child: [
      {
        code: 'service',
        name: '服务渠道',
        child: [
          { code: 'hotline', name: '客服热线' },
          { code: 'app_feedback', name: 'APP反馈' }
        ]
      }
    ]
  }
]

const experienceTree = [
  {
    id: 'exp_root_ca',
    tagName: '全领域业务',
    tagCode: 'CA',
    level: 0,
    tagStatus: '1',
    child: [
      {
        id: 'exp_product',
        tagName: '产品',
        tagCode: 'CA_PRODUCT',
        level: 1,
        tagStatus: '1',
        child: [
          {
            id: 'exp_intelligent',
            tagName: '智能化',
            tagCode: 'CA_INTELLIGENT',
            level: 2,
            tagStatus: '1',
            child: [
              {
                id: 'exp_cockpit',
                tagName: '智能座舱',
                tagCode: 'CA_COCKPIT',
                level: 3,
                tagStatus: '1',
                child: []
              }
            ]
          }
        ]
      }
    ]
  }
]

const standardPoints = [
  ['TOPIC_001', '语音助手识别准确', '正向', '表扬', '杨浪'],
  ['TOPIC_002', '车机系统运行流畅', '正向', '表扬', '赵玉兰'],
  ['TOPIC_003', '智驾接管频率偏高', '负向', '抱怨', '王集福'],
  ['TOPIC_004', '高速续航表现稳定', '正向', '表扬', '冉江雪'],
  ['TOPIC_005', '售后响应速度较慢', '负向', '建议', '*雯婷'],
  ['TOPIC_006', '座椅乘坐舒适', '正向', '表扬', '*建秋']
].map(([topicCode, topicName, emotion, intention, operateUser], index) => ({
  id: `topic_${index + 1}`,
  topicCode,
  topicName,
  topicDesc: `${topicName}相关客户声音的标准化观点`,
  synonyms: index % 2 ? ['体验稳定', '表现可靠'] : ['识别精准', '交互自然'],
  ca: {
    firstName: '产品',
    secondName: index % 2 ? '智能化' : '用户体验',
    thirdName: index % 2 ? '智能座舱' : '综合体验',
    fourthName: topicName
  },
  jour: {
    firstName: '使用',
    secondName: index % 2 ? '日常用车' : '服务体验',
    thirdName: topicName
  },
  emotion,
  intention,
  tagCustomerIssueClassification: index % 2 ? 'C' : 'B',
  tagBusinessDomain: index % 2 ? '产品体验' : '产品质量',
  operateUser,
  updateTime: `2026-08-${String(15 + index).padStart(2, '0')} 10:20:00`,
  createUser: operateUser,
  createTime: `2026-07-${String(10 + index).padStart(2, '0')} 09:30:00`,
  tagStatus: '1'
}))

const resultDataRows = [
  ['VOICE_0001', '语音助手识别准确，连续指令执行很顺畅', '领航汽车', '领航品牌', 'CS75 PLUS', '智能座舱', '正向', '表扬', '抖音'],
  ['VOICE_0002', '高速NOA偶尔需要人工接管', '领航汽车', '领航品牌', 'UNI-V', '智驾接管', '负向', '抱怨', '汽车之家'],
  ['VOICE_0003', '高速续航扎实，冬季电耗可以接受', '星海汽车', '星海汽车', '星海S09', '续航表现', '正向', '表扬', '懂车帝'],
  ['VOICE_0004', '售后预约等待时间有点长', '启程汽车', '启程品牌', '启程Q05', '服务响应', '负向', '建议', '客服热线'],
  ['VOICE_0005', '座椅支撑性好，长途乘坐不累', '领航汽车', '领航品牌', 'CS55 PLUS', '座椅舒适', '正向', '表扬', '小红书'],
  ['VOICE_0006', '车机升级后导航启动速度明显提升', '星海汽车', '星海汽车', '星海SL03', '车机流畅', '正向', '表扬', '微博']
].map(([id, originalText, seriesFactory, brandName, carSeriesName, opinion, sentiment, intention, channelName], index) => ({
  id,
  soundsId: id,
  originalTextScene: originalText,
  seriesFactory,
  brandName,
  carSeriesName,
  modelName: `${carSeriesName} 2026款`,
  opinion,
  topicText: standardPoints[index % standardPoints.length].topicName,
  topic: standardPoints[index % standardPoints.length].topicCode,
  hotWord: opinion,
  sentiment,
  intention,
  usageScenarioFirst: index % 2 ? '行驶' : '使用',
  usageScenarioSecond: index % 2 ? '高速行驶' : '日常用车',
  domTagFirst: '产品',
  domTagSecond: '智能化',
  domTagThree: '智能座舱',
  domTagFour: opinion,
  userJourney1: '使用',
  userJourney2: '日常用车',
  dataId: `RAW_${String(index + 1).padStart(4, '0')}`,
  title: `${carSeriesName}${opinion}用户反馈`,
  originalText,
  content: originalText,
  publishTime: `2026-08-${String(20 - index).padStart(2, '0')} 1${index}:20:00`,
  firstContentType: '评论',
  secondContentType: '用户评价',
  firstChannelName: channelName === '客服热线' ? '私域' : '公域',
  secondChannelName: channelName,
  channelName,
  viewCount: 1200 + index * 137,
  commentCount: 42 + index * 5,
  likeCount: 180 + index * 23,
  shareCount: 16 + index,
  favoriteCount: 28 + index * 2,
  originalLink: `https://demo.local/voice/${index + 1}`,
  authorNick: `体验用户${index + 1}`,
  authorId: `USER_${1000 + index}`,
  oneId: `ONE_${2000 + index}`,
  authorType: index % 2 ? '车主' : '潜客',
  isWsaterArmy: '否',
  mainPostAuthorNick: `车友${index + 1}`,
  mainPostAuthorId: `POST_USER_${index + 1}`,
  mainPostId: `POST_${index + 1}`,
  mainPostContent: originalText,
  vhlId: `VEHICLE_${index + 1}`,
  vhlVin: `LS5A3${String(index + 1).padStart(12, '0')}`,
  weight: Number((0.92 - index * 0.03).toFixed(2)),
  dataStatus: '已完成'
}))

const dataProcessingTasks = [
  ['TASK_001', '本地舆情原声导入', 'local', '本地上传', 1280, 1280, '已完成'],
  ['TASK_002', '客服工单系统同步', 'system', '客服工单', 856, 1200, '处理中'],
  ['TASK_003', '新车上市专项分析', 'local', '汽车之家', 2048, 2048, '已完成'],
  ['TASK_004', '经销商回访数据同步', 'system', '经销商回访', 960, 960, '已完成'],
  ['TASK_005', '售后热线投诉导入', 'local', '客服热线', 86, 100, '部分失败'],
  ['TASK_006', '抖音评论同步', 'system', '抖音', 720, 900, '处理中']
].map(([batchId, taskName, taskType, dataSourceName, completedCount, totalCount, statusName], index) => ({
  batchId,
  id: batchId,
  taskName,
  taskType,
  taskTypeName: taskType === 'local' ? '本地导入' : '系统同步',
  dataSourceName,
  completedCount,
  totalCount,
  createUser: ['*建秋', '*旭东', '*雯婷'][index % 3],
  createTime: `2026-08-${String(21 - index).padStart(2, '0')} 09:30:00`,
  status: statusName === '已完成' ? '2' : statusName === '处理中' ? '1' : '-1',
  statusName
}))

const newWordRows = resultDataRows.map((row, index) => {
  const operator = index % 3 === 0 ? '*建秋' : '系统'
  return {
    id: `new_word_${index + 1}`,
    dataId: row.dataId,
    title: row.title,
    content: row.originalText,
    originalTextScene: row.originalTextScene,
    entity: row.opinion,
    opinion: row.opinion,
    originalOpinion: row.opinion,
    subject: row.opinion,
    description: row.originalText,
    full_opinion: row.originalText,
    standard_opinion: row.topicText,
    recommended_topic: row.topicText,
    standard_opinion_id: row.topic,
    frequency: 18 + index * 4,
    status: index % 3 === 0 ? 1 : -1,
    process_status: index % 3 === 0 ? 1 : -1,
    publishTime: row.publishTime,
    processed_time: index % 3 === 0 ? `${DAY} 10:00:00` : '-',
    created_time: row.publishTime,
    update_time: row.publishTime,
    operator,
    username: operator,
    last_operator: operator
  }
})

const corpusRows = [
  ['语音操控', '特别顺手', '语音助手识别准确', '表扬'],
  ['高速续航', '表现扎实', '高速续航表现稳定', '表扬'],
  ['智驾接管', '频率偏高', '智驾接管频率偏高', '抱怨'],
  ['售后预约', '等待较久', '售后响应速度较慢', '建议'],
  ['座椅支撑', '长途舒适', '座椅乘坐舒适', '表扬']
].map(([entity, description, standardOpinion, intention], index) => ({
  id: `corpus_${index + 1}`,
  entity,
  description,
  standard_opinion: standardOpinion,
  intention,
  intent: intention,
  created_time: `2026-07-${String(18 + index).padStart(2, '0')} 10:20:00`,
  updated_time: `2026-08-${String(12 + index).padStart(2, '0')} 15:30:00`,
  updatedTime: `2026-08-${String(12 + index).padStart(2, '0')} 15:30:00`,
  operator: ['冉江雪', '赵玉兰', '杨浪'][index % 3],
  enable_status: '1',
  status: '1'
}))

const sceneRows = [
  ['使用导航', '*雯婷'],
  ['开启空调', '*雯婷'],
  ['播放音乐', '王集福'],
  ['高速巡航', '*建秋'],
  ['城市拥堵', '*旭东'],
  ['快充补能', '赵玉兰'],
  ['售后维保', '杨浪'],
  ['雨天驾驶', '冉江雪']
].map(([sceneName, operator], index) => ({
  id: `scene_${index + 1}`,
  sceneName,
  operator,
  updateTime: `2026-08-${String(20 - index).padStart(2, '0')} 14:20:00`,
  createTime: `2026-07-${String(10 + index).padStart(2, '0')} 09:10:00`,
  status: index === 6 ? '0' : '1',
  statusName: index === 6 ? '已禁用' : '已启用'
}))

const sceneCategories = [
  ['操作情景', 23],
  ['路况', 14],
  ['天气', 6],
  ['行车速度', 5],
  ['使用场景', 20]
].map(([categoryName, leafCount], index) => ({
  id: `scene_category_${index + 1}`,
  patentId: '0',
  categoryName,
  level: 1,
  leafCount,
  status: '1',
  children: []
}))

const brands = [
  ['BRAND_LH', '领航品牌'],
  ['BRAND_QC', '启程品牌'],
  ['BRAND_XH', '星海汽车']
].map(([code, name], index) => ({
  id: `brand_${index + 1}`,
  code,
  brandCode: code,
  brandName: name,
  name,
  operator: ['王集福', '*雯婷', '*建秋'][index],
  updateTime: `2026-08-${18 + index} 11:00:00`,
  createTime: `2026-06-${10 + index} 09:00:00`,
  status: '1',
  statusName: '已启用'
}))

const seriesRows = [
  ['CS75 PLUS', '领航品牌', 'BRAND_LH', '是', '是', '本品'],
  ['UNI-V', '领航品牌', 'BRAND_LH', '是', '否', '本品'],
  ['启程Q05', '启程品牌', 'BRAND_QC', '是', '是', '本品'],
  ['星海S09', '星海汽车', 'BRAND_XH', '是', '是', '本品'],
  ['星海SL03', '星海汽车', 'BRAND_XH', '否', '否', '本品'],
  ['竞驰M8', '星海汽车', 'BRAND_XH', '否', '否', '竞品']
].map(([name, brandName, brandCode, isCoreName, isNewCarName, competitiveTypeName], index) => ({
  id: `series_${index + 1}`,
  code: `SERIES_${String(index + 1).padStart(3, '0')}`,
  name,
  brandName,
  brandCode,
  isCore: isCoreName === '是' ? '1' : '0',
  isCoreName,
  isNewCar: isNewCarName === '是' ? '1' : '0',
  isNewCarName,
  competitiveType: competitiveTypeName === '本品' ? 1 : 2,
  competitiveTypeName,
  operator: ['王集福', '*雯婷', '*建秋'][index % 3],
  updateTime: `2026-08-${String(20 - index).padStart(2, '0')} 16:20:00`,
  createTime: `2026-06-${String(8 + index).padStart(2, '0')} 09:30:00`,
  status: '1',
  statusName: '已启用'
}))

const ruleGroups = [
  ['rule_quality', '质量问题关键词', 18],
  ['rule_cockpit', '智能座舱关键词', 16],
  ['rule_driving', '智能驾驶关键词', 14],
  ['rule_energy', '续航与补能关键词', 12],
  ['rule_service', '售后服务关键词', 11],
  ['rule_delivery', '交付体验关键词', 9]
].map(([id, name, cnt]) => ({ id, name, type: 'rule', cnt, count: cnt, resourceCount: cnt, allowDeletion: true }))

const accountGroups = [
  ['account_official', '车企官方账号', 12],
  ['account_media', '汽车媒体账号', 10],
  ['account_kol', '行业KOL账号', 9],
  ['account_service', '服务渠道账号', 8],
  ['account_competitor', '竞品观察账号', 7]
].map(([id, name, cnt]) => ({ id, name, type: 'account', ruleType: 'single', cnt, count: cnt, resourceCount: cnt, allowDeletion: true }))

const keywordSets = [
  ['异响', '漏水', '抖动', '断电', '故障灯', '制动异常'],
  ['车机卡顿', '语音误识别', '导航漂移', '蓝牙断连', '屏幕黑屏', '系统重启'],
  ['智驾接管', '车道偏离', '跟车过近', '泊车失败', '避障迟缓', '误刹车'],
  ['续航缩水', '充电慢', '充电中断', '能耗偏高', '低温衰减', '补能不便'],
  ['服务等待', '预约困难', '配件缺货', '维修反复', '回复迟缓', '服务热情'],
  ['交付延期', '验车问题', '手续缓慢', '车辆划痕', '赠品缺失', '交车仪式']
]
const keywordRows = ruleGroups.flatMap((group, groupIndex) =>
  keywordSets[groupIndex].map((name, itemIndex) => ({
    id: `keyword_${groupIndex + 1}_${itemIndex + 1}`,
    resourceId: group.id,
    name,
    createTime: `2026-08-${String(20 - itemIndex).padStart(2, '0')} 10:${String(groupIndex * 7 + itemIndex * 3).padStart(2, '0')}:00`,
    status: itemIndex === 5 ? 'Disabled' : 'Enabled'
  }))
)

const accountSets = [
  [
    ['领航汽车官方', 'lh_auto_official', 'douyin'],
    ['星海汽车官方', 'xinghai_auto', 'xiaohongshu'],
    ['启程品牌官方', 'qicheng_auto', 'weibo'],
    ['高端品牌官方', 'premium_auto', 'douyin']
  ],
  [
    ['汽车之家编辑部', 'autohome_editor', 'autohome'],
    ['懂车帝新能源', 'dcd_ev', 'dongchedi'],
    ['易车观察', 'yiche_insight', 'yiche'],
    ['太平洋汽车', 'pcauto_editor', 'autohome']
  ],
  [
    ['智驾观察社', 'adas_insight', 'weibo'],
    ['新能源体验官', 'ev_reviewer', 'xiaohongshu'],
    ['座舱研究员', 'cockpit_lab', 'douyin'],
    ['汽车产品经理', 'auto_pm', 'weibo']
  ],
  [
    ['领航客户服务', 'lh_service', 'weibo'],
    ['星海售后服务', 'xinghai_service', 'douyin'],
    ['启程用户关怀', 'qicheng_care', 'xiaohongshu'],
    ['400客服中心', 'hotline_center', 'weibo']
  ],
  [
    ['竞驰汽车官方', 'jingchi_auto', 'douyin'],
    ['远途汽车官方', 'yuantu_auto', 'weibo'],
    ['凌峰汽车官方', 'lingfeng_auto', 'xiaohongshu'],
    ['云驰汽车官方', 'yunchi_auto', 'dongchedi']
  ]
]
const accountRows = accountGroups.flatMap((group, groupIndex) =>
  accountSets[groupIndex].map(([accountName, accountId, channel], itemIndex) => ({
    id: `account_lexicon_${groupIndex + 1}_${itemIndex + 1}`,
    resourceId: group.id,
    accountName,
    accountId,
    channel,
    createTime: `2026-08-${String(18 - itemIndex).padStart(2, '0')} 11:${String(groupIndex * 8 + itemIndex * 4).padStart(2, '0')}:00`,
    status: itemIndex === 3 ? '0' : '1'
  }))
)

const ruleTestRows = [
  ['智能座舱负面识别测试', '闭环规则', 6, 120, '已完成'],
  ['智驾接管场景测试', '闭环规则', 4, 80, '已完成'],
  ['续航关键词清洗测试', '清洗规则', 8, 160, '运行中'],
  ['服务投诉识别测试', '闭环规则', 5, 100, '未开始'],
  ['新车上市舆情测试', '清洗规则', 7, 140, '已完成']
].map(([ruleTestInfo, ruleTypeText, ruleCount, sampleCount, testStatusStr], index) => ({
  id: `rule_test_${index + 1}`,
  ruleTestInfo,
  ruleType: ruleTypeText === '闭环规则' ? '2' : '1',
  ruleTypeText,
  ruleCount,
  sampleCount,
  createTime: `2026-08-${String(20 - index).padStart(2, '0')} 09:30:00`,
  createUser: ['王集福', '*雯婷', '*建秋'][index % 3],
  finishTime: testStatusStr === '已完成' ? `2026-08-${String(20 - index).padStart(2, '0')} 10:10:00` : '-',
  testStatus: testStatusStr === '已完成' ? '1' : '0',
  testStatusStr
}))

const systemAccounts = [
  ['*瑞鸿', '6319203', '客户创新及数据处', '超级管理员', 21, 100, '已启用'],
  ['*建秋', '6356329', 'NPS运营管理室', '运营管理员', 36, 100, '已启用'],
  ['*雯婷', '6320824', '数字化推进室', '业务分析师', 27, 92, '已启用'],
  ['王集福', '6322321', '客户创新及数据处', '规则管理员', 18, 85, '已启用'],
  ['*旭东', '6315436', 'NPS运营管理室', '数据专员', 14, 78, '已启用'],
  ['赵玉兰', 'Y00522', '数字化推进室', '业务分析师', 9, 70, '已禁用']
].map(([userName, employeeId, deptName, roleName, loginCounts, completeRate, statusText], index) => ({
  id: `system_account_${index + 1}`,
  userName,
  employeeId,
  deptName,
  roleName,
  loginCounts,
  lastLoginTime: `2026-08-${String(21 - index).padStart(2, '0')} 09:${String(10 + index * 5).padStart(2, '0')}:00`,
  completeRate,
  status: statusText === '已启用' ? '1' : '0',
  statusText
}))

const roles = [
  ['role_admin', '超级管理员', '系统全部权限', 2, '已启用'],
  ['role_operation', '运营管理员', '运营管理与知识中心权限', 6, '已启用'],
  ['role_analyst', '业务分析师', '查询、纠错和分析权限', 12, '已启用'],
  ['role_rule', '规则管理员', '规则配置与测试权限', 4, '已启用'],
  ['role_viewer', '只读访客', '仅查看权限', 8, '已禁用']
].map(([roleId, roleName, remark, userCount, roleStatusName]) => ({ id: roleId, roleId, roleName, remark, userCount, roleStatusName }))

const downloads = [
  ['原始数据-202608210930', '*建秋', '已完成'],
  ['结果数据-202608201615', '*雯婷', '已完成'],
  ['标准观点-202608191020', '王集福', '已完成'],
  ['用车场景-202608181430', '赵玉兰', '已完成'],
  ['规则测试报告-202608171100', '*旭东', '生成中']
].map(([fileName, operator, status], index) => ({
  id: `download_${index + 1}`,
  fileName,
  downloadContent: fileName,
  downloadTime: `2026-08-${String(21 - index).padStart(2, '0')} ${String(9 + index).padStart(2, '0')}:30:00`,
  operator,
  status: status === '已完成' ? '1' : '0',
  statusName: status,
  filePath: `/mock-download/${fileName}.xlsx`
}))

const departmentRows = [
  { code: 'dept_1', id: 'dept_1', name: '客户创新及数据处', options: [] },
  { code: 'dept_2', id: 'dept_2', name: 'NPS运营管理室', options: [] },
  { code: 'dept_3', id: 'dept_3', name: '数字化推进室', options: [] }
]

const insightNamespaces = ['/api/insights/', '/api/new-words/', '/api/ai/opinion-synonyms']

const isPath = (pathname, ...suffixes) => suffixes.some(suffix => pathname.endsWith(suffix))

/**
 * 为 Sites 中的声音洞察引擎返回独立 Mock 数据，严禁回落到 VOC 数据生成器。
 */
export function buildInsightResult(pathname, method = 'GET', body = {}) {
  if (!insightNamespaces.some(prefix => pathname.startsWith(prefix))) {
    return { handled: false, result: undefined }
  }

  if (isPath(pathname, '/insCqCaDataSource/getChannelTree', '/accountLexicon/getChannelTree', '/insClosedRule/getChannelTree')) {
    return { handled: true, result: channelTree }
  }
  if (isPath(pathname, '/insCqCaDataSource/getResultData', '/insCqCaDataSource/getRawData', '/insCqCaDataSource/getCleanData')) {
    return { handled: true, result: pageResult(repeatRows(resultDataRows, 36, 'voice'), body) }
  }
  if (isPath(pathname, '/insDataSource/findDataProcessingTasks')) {
    return { handled: true, result: pageResult(dataProcessingTasks, body) }
  }
  if (isPath(pathname, '/new-words/search')) {
    const page = pageResult(repeatRows(newWordRows, 24, 'new_word'), body)
    return { handled: true, result: { items: page.records, total: page.total, page: page.current, page_size: page.size } }
  }
  if (isPath(pathname, '/ai/opinion-synonyms/search')) {
    return { handled: true, result: { items: repeatRows(corpusRows, 20, 'corpus'), total: 20 } }
  }
  if (isPath(pathname, '/insTagLibClient/findAllTopicList')) {
    return { handled: true, result: pageResult(repeatRows(standardPoints, 30, 'topic'), body) }
  }
  if (isPath(pathname, '/insTagLibClient/findAllFinalTagLib', '/insTagLibClient/findAllFinalTagLibClientVoList')) {
    return {
      handled: true,
      result: standardPoints.map(item => ({
        ...item,
        tagName: item.topicName,
        tagCode: item.topicCode
      }))
    }
  }
  if (isPath(pathname, '/insTagLibClient/getTagLibClientTree', '/insTagLibClient/findTagTree')) {
    return { handled: true, result: experienceTree }
  }
  if (isPath(pathname, '/carSceneCategory/findCarSceneCategoryTree', '/carSceneCategory/findCarSceneCategoryList')) {
    return { handled: true, result: sceneCategories }
  }
  if (isPath(pathname, '/carScene/findCarSceneList')) {
    return { handled: true, result: pageResult(repeatRows(sceneRows, 24, 'scene'), body) }
  }
  if (isPath(pathname, '/carScene/findCarSceneOperatorList', '/insTagLibClient/findTopicOperatorList')) {
    return { handled: true, result: [{ id: '1', userName: 'admin' }, { id: '2', userName: '*建秋' }, { id: '3', userName: '*雯婷' }] }
  }
  if (isPath(pathname, '/carSeriesInfo/queryBySelect')) {
    return { handled: true, result: pageResult(repeatRows(seriesRows, 30, 'series'), body) }
  }
  if (isPath(pathname, '/brandInfo/queryBySelect')) {
    return { handled: true, result: pageResult(repeatRows(brands, 18, 'brand'), body) }
  }
  if (isPath(pathname, '/automark/findAutomarkList')) {
    const automakers = brands.map((brand, index) => ({ ...brand, id: `automark_${index + 1}`, name: brand.brandName, statusName: '已启用' }))
    return { handled: true, result: pageResult(automakers, body) }
  }
  if (isPath(pathname, '/brandInfo/findAll', '/brandInfo/findByParam')) {
    return { handled: true, result: brands }
  }
  if (isPath(pathname, '/brandInfo/findAllBrandAndCarSeries')) {
    return { handled: true, result: brands.map(brand => ({ ...brand, carSeriesList: seriesRows.filter(series => series.brandCode === brand.code) })) }
  }
  if (isPath(pathname, '/insDataResource/findDataResourceList')) {
    const groups = body.type === 'account' ? accountGroups : ruleGroups
    return { handled: true, result: pageResult(groups, body) }
  }
  if (isPath(pathname, '/insDataResourceDesc/list')) {
    const rows = body.resourceId ? keywordRows.filter(item => item.resourceId === String(body.resourceId)) : keywordRows
    return { handled: true, result: pageResult(rows, body) }
  }
  if (isPath(pathname, '/accountLexicon/findAccountLexiconList')) {
    const rows = accountRows.filter(item => {
      if (body.resourceId && item.resourceId !== String(body.resourceId)) return false
      if (body.channel && item.channel !== String(body.channel)) return false
      if (body.keyword && !`${item.accountName}${item.accountId}`.includes(String(body.keyword))) return false
      return true
    })
    return { handled: true, result: pageResult(rows, body) }
  }
  if (isPath(pathname, '/accountLexicon/findAccountLexiconInfo')) {
    return { handled: true, result: accountRows.find(item => item.id === body.id) || accountRows[0] }
  }
  if (isPath(pathname, '/ruleTest/ruleTestList')) {
    return { handled: true, result: pageResult(repeatRows(ruleTestRows, 25, 'rule_test'), body) }
  }
  if (isPath(pathname, '/insDataResource/findAllResourceTree')) {
    return { handled: true, result: [...ruleGroups, ...accountGroups] }
  }
  if (isPath(pathname, '/ruleTest/queryCreateUserList', '/addLabel/queryCreateUserList')) {
    return { handled: true, result: ['王集福', '*雯婷', '*建秋', '*旭东'] }
  }
  if (isPath(pathname, '/accountInfo/findAccountInfoList')) {
    return { handled: true, result: pageResult(repeatRows(systemAccounts, 24, 'system_account'), body) }
  }
  if (isPath(pathname, '/accountInfo/queryRoleALlList', '/role/getUserRoleList')) {
    return { handled: true, result: roles }
  }
  if (isPath(pathname, '/accountInfo/findDepartList', '/accountInfo/findDepartAccountTree', '/accountInfo/findDepartTree')) {
    return { handled: true, result: departmentRows }
  }
  if (isPath(pathname, '/role/queryRoleList')) {
    return { handled: true, result: pageResult(roles, body) }
  }
  if (isPath(pathname, '/role/queryMenuPermissionList')) {
    return { handled: true, result: [] }
  }
  if (isPath(pathname, '/downLoad/findDownLoadFileList')) {
    return { handled: true, result: pageResult(repeatRows(downloads, 18, 'download'), body) }
  }
  if (isPath(pathname, '/insDictItem/insAllDictItems')) {
    return {
      handled: true,
      result: {
        insAllDictItems: {
          voc_sentiment: [
            { text: '正向', value: '正向' },
            { text: '中性', value: '中性' },
            { text: '负向', value: '负向' }
          ]
        }
      }
    }
  }
  if (isPath(pathname, '/accountLexicon/conditions')) {
    return { handled: true, result: accountConditions }
  }
  if (pathname.endsWith('/conditions')) {
    return { handled: true, result: commonConditions }
  }

  const isMutation = method !== 'GET' && /(insert|save|update|delete|change|batch|copy|start|audit)/i.test(pathname)
  if (isMutation) {
    return { handled: true, result: { id: `insight_demo_${Date.now()}` } }
  }
  if (/tree/i.test(pathname)) {
    return { handled: true, result: [] }
  }
  if (/(list|page|query|find)/i.test(pathname)) {
    return { handled: true, result: pageResult([], body) }
  }
  return { handled: true, result: {} }
}

export const insightMockContracts = {
  pageResult,
  commonConditions,
  channelTree,
  standardPoints,
  resultDataRows,
  dataProcessingTasks,
  newWordRows,
  corpusRows,
  sceneRows,
  brands,
  seriesRows,
  ruleGroups,
  accountGroups,
  keywordRows,
  accountRows,
  ruleTestRows,
  systemAccounts,
  roles,
  downloads
}
