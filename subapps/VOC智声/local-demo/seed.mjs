const DAY = 24 * 60 * 60 * 1000

const isoDate = offset => new Date(Date.now() - offset * DAY).toISOString().slice(0, 10)

export const createSeedData = () => {
  const brands = [
    { id: 'voc-brand-zhixing', name: '智行', series: ['智行 S7', '智行 X5'], score: 82.6, imageUrl: '/demo-assets/brands/zhixing.png' },
    { id: 'voc-brand-yuantu', name: '远途', series: ['远途 M8', '远途 T6'], score: 79.4, imageUrl: '/demo-assets/brands/yuantu.png' },
    { id: 'voc-brand-lingfeng', name: '凌峰', series: ['凌峰 C6', '凌峰 V9'], score: 77.8, imageUrl: '/demo-assets/brands/lingfeng.png' },
    { id: 'voc-brand-xingmai', name: '星迈', series: ['星迈 E5'], score: 75.9, imageUrl: '/demo-assets/brands/xingmai.png' },
    { id: 'voc-brand-yunchi', name: '云驰', series: ['云驰 R7'], score: 74.3, imageUrl: '/demo-assets/brands/yunchi.png' }
  ]

  const voiceStories = [
    {
      sentiment: '负面',
      intention: '抱怨',
      opinionName: '系统升级后的稳定性影响日常用车体验',
      title: 'OTA升级后导航与音乐切换偶发卡顿，希望尽快完成稳定性优化',
      content: '上周完成车机系统升级后，导航、音乐和倒车影像之间快速切换时会偶发卡顿，严重时需要等待十几秒才能恢复。车辆基础驾驶不受影响，但早晚高峰使用导航时很影响体验。售后已经主动联系并安排远程诊断，希望后续版本能优先解决稳定性问题，并明确推送时间。',
      topic: '智能座舱稳定性',
      mentionCount: 3280
    },
    {
      sentiment: '负面',
      intention: '抱怨',
      opinionName: '售后高峰期排队时间过长且进度反馈不及时',
      title: '周末到店保养等待时间超过预期，维修进度缺少主动提醒',
      content: '提前一天预约了周末保养，到店后仍等待了近一个小时才开始接车。服务顾问态度很好，但施工进度需要多次询问才能确认，休息区也看不到实时状态。如果能够在手机端同步排队、开工、完工和预计交车时间，整体体验会更安心。',
      topic: '售后响应时效',
      mentionCount: 2860
    },
    {
      sentiment: '负面',
      intention: '建议',
      opinionName: '低温环境下续航预估偏差需要更透明的说明',
      title: '冬季续航预估变化较大，希望增加能耗原因说明和路线建议',
      content: '最近气温下降后，表显续航和实际可行驶里程的差距明显增大，开启空调后剩余里程下降也比较快。希望系统结合温度、空调和道路拥堵情况解释续航变化，并在长途出行前给出更可靠的充电站规划。',
      topic: '能耗续航',
      mentionCount: 2410
    },
    {
      sentiment: '负面',
      intention: '抱怨',
      opinionName: '高速风噪在特定速度区间内较为明显',
      title: '高速巡航时A柱附近风噪偏大，影响车内通话清晰度',
      content: '城市道路行驶时车内静谧性不错，但速度超过一百公里后，驾驶位A柱附近的风噪会明显增加，蓝牙通话时对方偶尔听不清。已经到店检查过密封条，希望厂家能进一步确认是否存在批次差异并给出处理方案。',
      topic: '驾乘静谧性',
      mentionCount: 1980
    },
    {
      sentiment: '正面',
      intention: '表扬',
      opinionName: '智能语音在连续指令场景中的识别准确率获得认可',
      title: '连续语音控制空调、车窗和导航时响应自然，日常使用很方便',
      content: '日常通勤最常用的是语音控制，连续说出打开空调、调低温度、导航回家等指令都能准确执行，中途不用重复唤醒。家人坐在后排也能分区控制车窗和音乐，整体体验比预期更成熟。',
      topic: '智能语音交互',
      mentionCount: 4560
    },
    {
      sentiment: '正面',
      intention: '表扬',
      opinionName: '二排空间与座椅舒适性满足家庭长途出行需求',
      title: '满载出行时二排空间宽裕，长途乘坐和储物表现令人满意',
      content: '一家五口周末长途出行，二排腿部空间和座椅支撑都比较舒服，后备厢还能放下婴儿车和多个行李箱。老人上下车也方便，连续三小时乘坐没有明显疲劳感，家庭使用场景考虑得很周到。',
      topic: '驾乘空间',
      mentionCount: 4210
    },
    {
      sentiment: '中性',
      intention: '咨询',
      opinionName: '用户关注下一版本OTA升级范围与推送节奏',
      title: '希望了解下一次OTA是否包含座舱快捷入口和能耗优化',
      content: '目前系统整体使用正常，看到社区里有人提到下一版会调整快捷入口并优化能耗统计，想确认具体覆盖哪些车型、是否分批推送，以及升级前是否需要保持车辆充电或预留固定时间。',
      topic: 'OTA升级计划',
      mentionCount: 3650
    },
    {
      sentiment: '中性',
      intention: '建议',
      opinionName: '维修保养过程需要提供更清晰的费用与工项说明',
      title: '建议在服务页面提前展示保养项目、预计费用和可选工项',
      content: '预约保养时只能看到基础套餐，其他检查项目和可能产生的费用要到店后才知道。建议在预约页面根据里程生成建议工项，标明必选、可选和预计价格，确认后再施工，能减少沟通成本。',
      topic: '费用透明',
      mentionCount: 3120
    },
    {
      sentiment: '正面',
      intention: '表扬',
      opinionName: '服务人员主动回访与问题闭环提升客户信任感',
      title: '异响检查后持续回访并同步处理进度，问题闭环体验很好',
      content: '车辆出现轻微异响后，服务专员当天安排检查，并在零件到货、维修完成和取车后分别主动告知进度。虽然问题不大，但整个处理过程透明，取车后还进行了两次回访，能感受到服务团队确实在负责到底。',
      topic: '服务闭环',
      mentionCount: 3870
    },
    {
      sentiment: '负面',
      intention: '建议',
      opinionName: '手机应用与车机状态同步存在延迟',
      title: '远程空调和车辆位置偶尔刷新不及时，希望优化状态同步',
      content: '大多数时候远程控制都能正常使用，但地下车库或网络切换后，手机应用里的车辆位置和剩余电量偶尔会停留在旧状态，远程空调也需要重复点击。希望增加明确的刷新状态和失败原因提示。',
      topic: '车联服务稳定性',
      mentionCount: 2240
    },
    {
      sentiment: '中性',
      intention: '咨询',
      opinionName: '用户希望明确质保范围与易损件判定标准',
      title: '咨询整车质保、三电质保与常用易损件的具体范围',
      content: '购车资料里写了整车和三电系统的质保年限，但对雨刮、刹车片、轮胎和内饰件的判定标准不够清楚。希望在应用里按车辆信息展示适用条款，并提供可直接咨询的入口。',
      topic: '质保政策',
      mentionCount: 2760
    },
    {
      sentiment: '正面',
      intention: '表扬',
      opinionName: '辅助驾驶提示清晰且接管逻辑容易理解',
      title: '高速辅助驾驶的车道保持和接管提示清楚，长途更轻松',
      content: '高速长途使用辅助驾驶时，跟车和车道保持都比较平稳，遇到施工路段会提前提醒接管，仪表和声音提示也容易理解。系统不会让人过度依赖，合理使用时确实减轻了长途驾驶负担。',
      topic: '辅助驾驶体验',
      mentionCount: 4390
    }
  ]

  const voices = Array.from({ length: 48 }, (_, index) => {
    const brand = brands[index % brands.length]
    const story = voiceStories[index % voiceStories.length]
    return {
      id: `voice-${index + 1}`,
      dataId: `voice-${index + 1}`,
      originalId: `voice-${index + 1}`,
      newId: `voice-${index + 1}`,
      brandName: brand.name,
      brandCode: brand.id,
      carSeriesName: brand.series[index % brand.series.length],
      channel: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'][index % 5],
      channelName: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'][index % 5],
      sentiment: story.sentiment,
      intention: story.intention,
      opinionName: story.opinionName,
      title: story.title,
      content: story.content,
      originalTextScene: story.content,
      originalTexTScene: story.content,
      mentionContent: story.content,
      mentionCount: story.mentionCount + Math.floor(index / voiceStories.length) * 37,
      customerName: `体验用户${String(index + 1).padStart(2, '0')}`,
      custName: `体验用户${String(index + 1).padStart(2, '0')}`,
      username: `体验用户${String(index + 1).padStart(2, '0')}`,
      mentionTime: `${isoDate(index % 28)} 10:${String(index % 60).padStart(2, '0')}:00`,
      dataCreateTime: `${isoDate(index % 28)} 10:${String(index % 60).padStart(2, '0')}:00`,
      evaluateTime: `${isoDate(index % 28)} 10:${String(index % 60).padStart(2, '0')}:00`,
      topic: story.topic,
      topics: [{ topic: story.topic, sentiment: story.sentiment, intention: story.intention }],
      highQuality: index % 4 !== 3,
      quality: index % 4 !== 3 ? '高质量原声' : '普通原声',
      riskLevel: story.sentiment === '负面' && index % 2 === 0 ? 'P1' : 'P3'
    }
  })

  const events = Array.from({ length: 18 }, (_, index) => ({
    id: `event-${index + 1}`,
    eventId: `EV${String(index + 1).padStart(4, '0')}`,
    name: index % 2 === 0 ? '智能座舱卡顿专项跟进' : '售后响应体验提升',
    eventName: index % 2 === 0 ? '智能座舱卡顿专项跟进' : '售后响应体验提升',
    title: index % 2 === 0 ? '智能座舱卡顿专项跟进' : '售后响应体验提升',
    type: index % 3 === 0 ? '批量事件' : '单点事件',
    status: ['待处理', '处理中', '待确认', '已完成'][index % 4],
    taskStatus: ['待处理', '处理中', '待确认', '已完成'][index % 4],
    taskStatusName: ['待处理', '处理中', '待确认', '已完成'][index % 4],
    priority: ['P0', 'P1', 'P2', 'P3'][index % 4],
    eventPriority: ['P0', 'P1', 'P2', 'P3'][index % 4],
    eventPriorityName: ['P0', 'P1', 'P2', 'P3'][index % 4],
    owner: ['事件负责人', '服务专员', '体验分析师'][index % 3],
    department: ['客户体验中心', '产品质量中心', '售后服务中心'][index % 3],
    primaryDepName: ['客户体验中心', '产品质量中心', '售后服务中心'][index % 3],
    subjectCategoryName: index % 3 === 0 ? '质量体验' : '服务体验',
    createTime: `${isoDate(index % 20)} 09:30:00`,
    progress: [15, 45, 75, 100][index % 4],
    voiceIds: voices.slice(index, index + 3).map(item => item.id),
    logs: [
      { id: `log-${index}-1`, action: '创建事件', operator: '演示管理员', time: `${isoDate(index % 20)} 09:30:00` },
      { id: `log-${index}-2`, action: '分派责任部门', operator: '演示管理员', time: `${isoDate(index % 20)} 10:00:00` }
    ]
  }))

  const reports = Array.from({ length: 12 }, (_, index) => ({
    id: `report-${index + 1}`,
    reportName: ['月度客户体验洞察', '智能座舱专题分析', '售后服务体验报告'][index % 3],
    name: ['月度客户体验洞察', '智能座舱专题分析', '售后服务体验报告'][index % 3],
    categoryName: ['综合洞察', '产品体验', '服务体验'][index % 3],
    status: index % 4 === 0 ? 0 : 1,
    publishStatus: index % 4 === 0 ? '草稿' : '已发布',
    viewCount: 320 + index * 37,
    collectionCount: 18 + index * 3,
    createTime: `${isoDate(index * 2)} 14:00:00`,
    summary: '围绕客户关注场景、负面体验和改进闭环形成的离线演示报告。'
  }))

  return {
    version: 1,
    resetAt: new Date().toISOString(),
    brands,
    voices,
    events,
    reports,
    users: ['演示管理员', '体验分析师', '事件负责人', '服务专员'].map((userName, index) => ({
      id: ['demo-admin', 'demo-analyst', 'demo-owner', 'demo-service'][index],
      username: userName,
      userName,
      name: userName,
      accountName: ['voc.admin', 'voc.analyst', 'voc.owner', 'voc.service'][index],
      employeeId: `VOC${String(index + 1).padStart(4, '0')}`,
      department: ['客户体验中心', '客户体验中心', '产品质量中心', '售后服务中心'][index],
      secondDeptName: '智行汽车集团',
      thirdDeptName: ['客户体验中心', '客户体验中心', '产品质量中心', '售后服务中心'][index],
      roleName: index === 0 ? '超级管理员' : '演示业务角色',
      operationRoleName: index === 0 ? '全部权限' : '业务操作权限',
      lastLoginTime: `${isoDate(index)} 09:${String(12 + index * 7).padStart(2, '0')}:00`,
      loginCounts: 86 - index * 11,
      originalListenCount: 1260 - index * 145,
      complainOriginalListenCount: 386 - index * 42,
      visitDuration: `${36 - index * 4}小时${18 + index * 5}分钟`,
      listenTaskCompleteRate: `${96 - index * 3}%`,
      status: '1'
    })),
    roles: [{ id: 'demo-super-admin', name: '超级管理员', userCount: 1, status: 1 }],
    categories: [
      { id: 'category-1', name: '综合洞察', sortNo: 1 },
      { id: 'category-2', name: '产品体验', sortNo: 2 },
      { id: 'category-3', name: '服务体验', sortNo: 3 }
    ]
  }
}
