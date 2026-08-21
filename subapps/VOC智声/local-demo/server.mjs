import http from 'node:http'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createSeedData } from './seed.mjs'

const moduleDir = path.dirname(fileURLToPath(import.meta.url))

const ok = result => ({
  success: true,
  message: '请求成功',
  code: '200',
  result,
  tid: 'local-demo'
})

const createDemoPdfDataUrl = () => {
  const content = [
    'BT',
    '/F1 20 Tf',
    '72 760 Td',
    '(VOC Voice Demo Report) Tj',
    '0 -34 Td',
    '/F1 12 Tf',
    '(Offline customer experience insight summary) Tj',
    '0 -24 Td',
    '(Focus: cockpit stability, service response, and action closure.) Tj',
    'ET'
  ].join('\n')
  const objects = [
    '<< /Type /Catalog /Pages 2 0 R >>',
    '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
    '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>',
    `<< /Length ${Buffer.byteLength(content)} >>\nstream\n${content}\nendstream`,
    '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>'
  ]
  let pdf = '%PDF-1.4\n'
  const offsets = [0]
  objects.forEach((object, index) => {
    offsets.push(Buffer.byteLength(pdf))
    pdf += `${index + 1} 0 obj\n${object}\nendobj\n`
  })
  const xrefOffset = Buffer.byteLength(pdf)
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`
  pdf += offsets
    .slice(1)
    .map(offset => `${String(offset).padStart(10, '0')} 00000 n \n`)
    .join('')
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF`
  return `data:application/pdf;base64,${Buffer.from(pdf).toString('base64')}`
}

const readBody = request =>
  new Promise(resolve => {
    const chunks = []
    request.on('data', chunk => chunks.push(chunk))
    request.on('end', () => {
      const raw = Buffer.concat(chunks).toString('utf8')
      if (!raw) return resolve({})
      try {
        resolve(JSON.parse(raw))
      } catch {
        resolve({ raw })
      }
    })
  })

const getLanAddress = () => {
  for (const values of Object.values(os.networkInterfaces())) {
    const match = values?.find(item => item.family === 'IPv4' && !item.internal)
    if (match) return match.address
  }
  return '127.0.0.1'
}

const sendJson = (response, payload, status = 200) => {
  response.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    'Access-Control-Allow-Origin': '*'
  })
  response.end(JSON.stringify(payload))
}

const paginate = (items, body) => {
  const pageNum = Number(body.pageNum || body.pageNo || body.current || 1)
  const pageSize = Number(body.pageSize || body.size || 20)
  const start = (pageNum - 1) * pageSize
  const records = items.slice(start, start + pageSize)
  return {
    records,
    list: records,
    rows: records,
    items: records,
    dataList: records,
    total: items.length,
    pageNum,
    pageNo: pageNum,
    pageSize,
    size: pageSize,
    pages: Math.ceil(items.length / pageSize)
  }
}

const trend = () =>
  Array.from({ length: 14 }, (_, index) => ({
    date: new Date(Date.now() - (13 - index) * 86400000).toISOString().slice(0, 10),
    value: 68 + ((index * 7) % 19),
    mentionCount: 860 + index * 73,
    negativeRate: Number((18 - index * 0.35).toFixed(2)),
    positiveMentions: 520 + index * 29,
    neutralMentions: 230 + index * 17,
    negativeMentions: 110 + index * 9
  }))

const analyticsResult = database => {
  const brandRanking = database.brands.map((brand, index) => ({
    ...brand,
    brandName: brand.name,
    brandCode: brand.id,
    mentionCount: 12800 - index * 1350,
    negativeRate: Number((12.6 + index * 1.8).toFixed(1)),
    growth: Number((4.8 - index * 1.2).toFixed(1)),
    score: brand.score,
    value: brand.score
  }))
  return {
    title: 'VOC智声客户体验洞察',
    brandName: '智行汽车集团',
    brandCode: 'voc-group-zhixing',
    score: 81.6,
    experienceValue: 81.6,
    mentionCount: 48620,
    totalMentions: 48620,
    negativeRate: 14.8,
    positiveRate: 61.2,
    growth: 3.6,
    trend: trend(),
    trendList: trend(),
    dataList: brandRanking,
    list: brandRanking,
    records: brandRanking,
    topDataList: brandRanking.slice(0, 3),
    brandRanking,
    channelList: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'].map(
      (name, index) => ({
        name,
        value: 32 - index * 4,
        mentionCount: 12500 - index * 1700
      })
    ),
    opinionList: [
      { name: '空间表现', value: 92, sentiment: '正面' },
      { name: '智能座舱', value: 86, sentiment: '正面' },
      { name: '系统稳定性', value: 71, sentiment: '负面' },
      { name: '售后响应', value: 78, sentiment: '中性' }
    ]
  }
}

const briefResult = () => ({
  negativeRate: 14.8,
  negativeRateMoM: -2.3,
  negativeRateYoY: -4.1,
  positiveRate: 61.2,
  positiveRateMoM: 3.6,
  positiveRateYoY: 5.8,
  mentions: 48620,
  mentionsMoM: 6.4,
  mentionsYoY: 11.2,
  mentionCount: 48620,
  users: 18640,
  usersMoM: 4.7,
  usersYoY: 8.9,
  achieveRate: 92,
  achieveRateTalk: '体验改善目标达成情况良好'
})

const insightRows = database =>
  database.brands.map((brand, index) => ({
    id: brand.id,
    name: brand.name,
    brandName: brand.name,
    brandCode: brand.id,
    brandImageUrl: brand.imageUrl,
    imgUrl: brand.imageUrl,
    carSeriesName: brand.series[0],
    carSeriesCode: `${brand.id}-series`,
    tagName: ['智能座舱', '售后服务', '空间体验', '产品质量', '品牌口碑'][index],
    tagCode: `voc-tag-${index + 1}`,
    tag1Name: index % 2 === 0 ? '产品体验' : '服务体验',
    tag1Code: index % 2 === 0 ? 'voc-product' : 'voc-service',
    tag2Name: ['智能交互', '服务效率', '驾乘空间', '可靠性', '品牌认知'][index],
    tag2Code: `voc-tag2-${index + 1}`,
    tag4Code: `voc-scene-${index + 1}`,
    scene: ['系统稳定性', '服务响应', '空间舒适性', '质量可靠性', '品牌沟通'][index],
    channelName: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'][index],
    channel: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'][index],
    date: trend()[index + 8]?.date,
    negativeRate: Number((12.8 + index * 1.9).toFixed(1)),
    negativeRateMoM: Number((-2.4 + index * 0.7).toFixed(1)),
    negativeRateYoY: Number((-4.2 + index * 0.8).toFixed(1)),
    positiveRate: Number((64.2 - index * 2.1).toFixed(1)),
    mentions: 12800 - index * 1350,
    mentionCount: 12800 - index * 1350,
    mentionsMoM: Number((8.2 - index * 0.9).toFixed(1)),
    mentionsYoY: Number((12.4 - index).toFixed(1)),
    value: 12800 - index * 1350,
    value1: Number((12.8 + index * 1.9).toFixed(1)),
    value1MoM: Number((-2.4 + index * 0.7).toFixed(1)),
    value1YoY: Number((-4.2 + index * 0.8).toFixed(1)),
    value2: 12800 - index * 1350,
    value2MoM: Number((8.2 - index * 0.9).toFixed(1)),
    value2YoY: Number((12.4 - index).toFixed(1)),
    growth: Number((4.8 - index * 0.7).toFixed(1)),
    trend: trend()
      .slice(-7)
      .map(item => item.mentionCount - index * 70)
  }))

const tagMatrixRows = database => {
  const dimensions = [
    { metric: '产品体验', name: '智能交互' },
    { metric: '产品体验', name: '驾乘空间' },
    { metric: '产品体验', name: '品牌认知' },
    { metric: '服务体验', name: '服务效率' },
    { metric: '服务体验', name: '可靠性' }
  ]
  const rateStyles = [
    { rateColor: '#0f8f5b', rateBackgroundColor: '#e8f8f1' },
    { rateColor: '#1677ff', rateBackgroundColor: '#eaf3ff' },
    { rateColor: '#d46b08', rateBackgroundColor: '#fff7e6' },
    { rateColor: '#cf3f4f', rateBackgroundColor: '#fff1f0' },
    { rateColor: '#7a52c7', rateBackgroundColor: '#f4efff' }
  ]
  return database.brands.flatMap((brand, brandIndex) =>
    dimensions.map((dimension, tagIndex) => ({
      ...insightRows(database)[brandIndex],
      tag1Name: dimension.metric,
      tag1Code: dimension.metric === '产品体验' ? 'voc-product' : 'voc-service',
      tag2Name: dimension.name,
      tag2Code: `voc-tag2-${tagIndex + 1}`,
      mentions: 12800 - brandIndex * 1150 - tagIndex * 620,
      mentionCount: 12800 - brandIndex * 1150 - tagIndex * 620,
      negativeRate: Number((11.6 + brandIndex * 1.4 + tagIndex * 0.8).toFixed(1)),
      negativeRateMoM: Number((-2.8 + brandIndex * 0.4 + tagIndex * 0.3).toFixed(1)),
      mentionsMoM: Number((8.6 - brandIndex * 0.6 - tagIndex * 0.4).toFixed(1)),
      value1: Number((11.6 + brandIndex * 1.4 + tagIndex * 0.8).toFixed(1)),
      value1MoM: Number((-2.8 + brandIndex * 0.4 + tagIndex * 0.3).toFixed(1)),
      value1YoY: Number((-4.1 + brandIndex * 0.5 + tagIndex * 0.25).toFixed(1)),
      value2: 12800 - brandIndex * 1150 - tagIndex * 620,
      value2MoM: Number((8.6 - brandIndex * 0.6 - tagIndex * 0.4).toFixed(1)),
      value2YoY: Number((12.8 - brandIndex * 0.7 - tagIndex * 0.5).toFixed(1)),
      ...rateStyles[(brandIndex + tagIndex) % rateStyles.length]
    }))
  )
}

/** 构造品牌趋势时序，保持日期、品牌和双指标在同一数据点。 */
const brandTrendRows = database =>
  trend()
    .slice(-7)
    .map((point, pointIndex) => ({
      date: point.date,
      brandSeries: [
        {
          date: point.date,
          brandName: '集团均值',
          brandCode: 'voc-group-average',
          value1: 10860 + pointIndex * 310,
          value2: Number((16.9 - pointIndex * 0.22).toFixed(1))
        },
        ...database.brands.map((brand, brandIndex) => ({
          date: point.date,
          brandName: brand.name,
          brandCode: brand.id,
          value1: 12800 - brandIndex * 1250 + pointIndex * (235 - brandIndex * 12),
          value2: Number((12.8 + brandIndex * 1.9 - pointIndex * 0.16).toFixed(1))
        }))
      ]
    }))

const channelRows = () =>
  ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'].map((channelName, index) => ({
    channelName,
    channelCode: `voc-channel-${index + 1}`,
    mentions: 12600 - index * 1750,
    share: Number((31.5 - index * 4.6).toFixed(1)),
    negativeRate: Number((12.4 + index * 1.8).toFixed(1)),
    negativeRateMoM: Number((-2.1 + index * 0.6).toFixed(1)),
    negativeRateYoY: Number((-3.7 + index * 0.8).toFixed(1)),
    mentionsMoM: Number((7.8 - index * 0.9).toFixed(1)),
    mentionsYoY: Number((11.6 - index * 1.1).toFixed(1))
  }))

const opinionRows = database =>
  insightRows(database).map((item, index) => ({
    opinion: index % 2 === 0 ? `${item.tagName}体验获得认可` : `${item.scene}仍需持续改善`,
    sentiment: index % 2 === 0 ? '正面' : '负面',
    mentions: item.mentions,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY,
    negativeRate: `${item.negativeRate}%`,
    channelName: item.channelName,
    remark: [item.scene],
    sound: {
      id: database.voices[index]?.id,
      soundContent: database.voices[index]?.content,
      tags: [item.tagName, item.scene],
      userName: database.voices[index]?.customerName
    }
  }))

const detailedOpinionTexts = {
  positive: [
    '智能座舱语音交互响应迅速，连续指令识别准确且操作路径清晰',
    '售后服务接待专业，维修进度与预计交付时间反馈及时透明',
    '二排乘坐空间宽敞，长途出行的支撑性与舒适度表现突出'
  ],
  negative: [
    '系统升级后导航与音乐切换偶发卡顿，影响高频通勤场景体验',
    '售后高峰期排队时间较长，维修进度通知与主动解释仍不及时',
    '低温环境下续航预估偏差明显，长途出行时容易产生里程焦虑'
  ]
}

const drillDownIndicators = () =>
  [
    '智能座舱稳定性',
    '语音交互准确率',
    '驾乘空间舒适度',
    '低温续航可信度',
    '售后响应及时性',
    '维修质量可靠性',
    '交付流程透明度',
    '辅助驾驶易用性'
  ].map((tagName, index) => {
    const mentions = 12800 - index * 970
    const negativeRateValue = Number((11.8 + index * 1.1).toFixed(1))
    return {
      tagName,
      tagCode: `voc-experience-${index + 1}`,
      tagLevel: 2,
      value: mentions,
      valueMom: `${(7.6 - index * 0.6).toFixed(1)}%`,
      valueYoy: `${(12.4 - index * 0.8).toFixed(1)}%`,
      mentions,
      mentionsShare: Number((26.3 - index * 2.2).toFixed(1)),
      mentionTrend: trend()
        .slice(-7)
        .map((_, pointIndex) => String(mentions - 360 + pointIndex * (62 - index * 3))),
      mentionsMoM: Number((7.6 - index * 0.6).toFixed(1)),
      negativeRateValue,
      negativeTrend: trend()
        .slice(-7)
        .map((_, pointIndex) => String((negativeRateValue + 1.2 - pointIndex * 0.2).toFixed(1))),
      negativeRateMoM: Number((-2.6 + index * 0.25).toFixed(1)),
      positiveRateValue: Number((64.2 - index * 1.5).toFixed(1)),
      positiveTrend: trend()
        .slice(-7)
        .map((_, pointIndex) => String((61.8 - index + pointIndex * 0.4).toFixed(1))),
      positiveRateMoM: Number((3.8 - index * 0.2).toFixed(1)),
      neutralRateValue: Number((24 - index * 0.35).toFixed(1)),
      neutralTrend: trend()
        .slice(-7)
        .map((_, pointIndex) => String((23.1 - index * 0.2 + pointIndex * 0.15).toFixed(1))),
      neutralRateMoM: Number((1.2 + index * 0.1).toFixed(1)),
      rateColor: '#cf3f4f',
      rateBackgroundColor: '#fff1f0'
    }
  })

const drillDownTableRows = () =>
  drillDownIndicators().map(item => ({
    ...item,
    mentionsShare: item.mentionsShare,
    negativeRate: item.negativeRateValue
  }))

const sceneRows = database =>
  insightRows(database).map((item, index) => ({
    scenario: item.scene,
    sceneName: item.scene,
    sceneCode: item.tagCode,
    tag4Code: item.tagCode,
    mentions: item.mentions,
    mentionsMoM: item.mentionsMoM,
    mentionsYoY: item.mentionsYoY,
    negativeRate: item.negativeRate,
    negativeRateMoM: item.negativeRateMoM,
    negativeRateYoY: item.negativeRateYoY,
    mentionsMoMGroup: trend()
      .slice(-7)
      .map((_, pointIndex) => String(2 + index + pointIndex)),
    negativeRateMoMGroup: trend()
      .slice(-7)
      .map((_, pointIndex) => String(-3 + index + pointIndex * 0.4)),
    rateColor: '#f53f3f',
    rateBackgroundColor: '#fff1f0'
  }))

const leadershipScenarioRows = (database, type) => {
  const names =
    type === 'product'
      ? [
          '智能座舱',
          '驾乘空间',
          '动力性能',
          '质量可靠性',
          '能耗续航',
          '外观设计',
          '内饰做工',
          '辅助驾驶'
        ]
      : [
          '服务响应',
          '维修质量',
          '交付体验',
          '费用透明',
          '客户关怀',
          '网点环境',
          '专业能力',
          '配件供应'
        ]

  return names.map((tagName, index) => ({
    id: `voc-leader-${type}-${index + 1}`,
    tagName,
    tagCode: `voc-${type}-scene-${index + 1}`,
    mentionCount: 12680 - index * 1340 + (type === 'service' ? 460 : 0),
    positiveRate: Number((61.8 - index * 1.7 + (type === 'service' ? 2.1 : 0)).toFixed(1)),
    neutralRate: Number((24.6 + index * 0.8).toFixed(1)),
    negativeRate: Number((13.6 + index * 0.9 - (type === 'service' ? 0.4 : 0)).toFixed(1)),
    ringRatio: Number((7.8 - index * 0.7).toFixed(1)),
    yearOnYearRatio: Number((12.4 - index * 0.9).toFixed(1)),
    positiveRingRatio: Number((3.8 - index * 0.3).toFixed(1)),
    positiveYearOnYearRatio: Number((6.9 - index * 0.4).toFixed(1)),
    neutralRingRatio: Number((1.4 + index * 0.2).toFixed(1)),
    neutralYearOnYearRatio: Number((2.8 + index * 0.3).toFixed(1)),
    negativeRingRatio: Number((-2.9 + index * 0.3).toFixed(1)),
    negativeYearOnYearRatio: Number((-4.6 + index * 0.4).toFixed(1))
  }))
}

const leadershipOpinionRows = (database, intention = '抱怨') => {
  const opinionMap = {
    抱怨: [
      '系统升级后的稳定性影响日常用车体验',
      '售后高峰期排队时间过长且进度反馈不及时',
      '低温环境下续航预估偏差影响出行判断',
      '高速风噪在特定速度区间内较为明显',
      '手机应用与车机状态同步存在延迟'
    ],
    咨询: [
      '下一版本OTA升级范围与推送节奏',
      '整车与三电系统质保范围',
      '家用充电桩安装条件与申请流程',
      '保养套餐包含项目与适用周期',
      '长途出行充电路线规划方式'
    ],
    建议: [
      '增加常用功能自定义快捷入口',
      '透明展示维修保养的实时进度',
      '根据温度与路况解释续航变化',
      '优化手机应用的车辆状态刷新提示',
      '提供按车辆匹配的质保条款说明'
    ],
    表扬: [
      '智能语音连续指令识别准确自然',
      '二排空间与座椅舒适性适合家庭出行',
      '服务人员主动回访并持续跟进问题',
      '辅助驾驶提示清晰且接管逻辑易理解',
      '交付讲解细致且功能上手成本低'
    ]
  }
  const rows = opinionMap[intention] || opinionMap.抱怨

  return rows.map((opinionName, index) => ({
    id: `voc-opinion-${intention}-${index + 1}`,
    opinionName,
    mentionCount: 5280 - index * 610 + (intention === '表扬' ? 780 : 0),
    mentionRingRatio: Number(((intention === '抱怨' ? -2.6 : 3.8) + index * 0.7).toFixed(1)),
    mentionYearOnYearRatio: Number((8.6 - index * 0.8).toFixed(1)),
    intention,
    soundId: database.voices[index]?.id
  }))
}

const voiceListRows = (database, body = {}) => {
  const keyword = String(body.searchKeywords || body.keyword || '')
    .trim()
    .toLowerCase()
  const topic = String(body.topic || '').trim()
  const intention = String(body.intention || '').trim()
  const requestedSentiments = Array.isArray(body.sentimentList)
    ? body.sentimentList
    : Array.isArray(body.sentiment)
      ? body.sentiment
      : body.sentiment
        ? [body.sentiment]
        : []

  return database.voices
    .filter(
      item =>
        !keyword ||
        `${item.title}${item.content}${item.opinionName}`.toLowerCase().includes(keyword)
    )
    .filter(item => !topic || item.opinionName === topic || item.topic === topic)
    .filter(item => !intention || item.intention === intention)
    .filter(item => !requestedSentiments.length || requestedSentiments.includes(item.sentiment))
    .map((item, index) => ({
      ...item,
      id: item.newId || item.id,
      newId: item.newId || item.id,
      originalId: item.originalId || item.dataId || item.id,
      custName: item.custName || item.customerName,
      brand: item.brandName,
      carSeries: item.carSeriesName,
      channelName: item.channelName || item.channel,
      originalTexTScene: item.originalTexTScene || item.content,
      originalTextScene: item.originalTextScene || item.content,
      topics: item.topics || [
        { topic: item.topic, sentiment: item.sentiment, intention: item.intention }
      ],
      role: { name: item.customerName, code: `voc-customer-${index + 1}` }
    }))
}

const voiceDetailResult = (database, body = {}) => {
  const voiceId = String(body.newId || body.originalId || body.id || '')
  const item =
    database.voices.find(voice =>
      [voice.id, voice.dataId, voice.originalId, voice.newId].includes(voiceId)
    ) || database.voices[0]
  const relatedEvent = database.events.find(event => event.voiceIds?.includes(item.id))
  const topics = item.topics || [
    { topic: item.topic, sentiment: item.sentiment, intention: item.intention }
  ]

  return {
    ...item,
    id: item.newId || item.id,
    newId: item.newId || item.id,
    originalId: item.originalId || item.dataId || item.id,
    username: item.username || item.customerName,
    custName: item.custName || item.customerName,
    channelName: item.channelName || item.channel,
    originalTextScene: item.originalTextScene || item.content,
    evaluateTime: item.evaluateTime || item.mentionTime,
    soundslist: topics.map(topic => ({
      ...topic,
      brand: item.brandName,
      carSeries: item.carSeriesName
    })),
    topics,
    ext: [
      { name: '数据渠道', value: item.channelName || item.channel },
      { name: '情感倾向', value: item.sentiment },
      { name: '用户意图', value: item.intention },
      { name: '体验场景', value: item.topic }
    ],
    relationEvents: relatedEvent
      ? [
          {
            id: relatedEvent.id,
            dataId: item.dataId || item.id,
            warningEventNo: relatedEvent.eventId,
            eventName: relatedEvent.name,
            mainRespOrgName: relatedEvent.department,
            taskStatus: relatedEvent.status,
            taskStatusName: relatedEvent.status,
            eventPriority: relatedEvent.priority
          }
        ]
      : []
  }
}

const trendResult = () => ({
  negativeRateAvg: 14.8,
  trend: trend().map(item => ({
    ...item,
    totalMentions: item.mentionCount,
    totalMentionsMoM: 6.4,
    totalMentionsYoY: 11.2,
    positiveMentionsMoM: 5.8,
    positiveMentionsYoY: 9.6,
    neutralMentionsMoM: 2.4,
    neutralMentionsYoY: 4.8,
    negativeMentionsMoM: -2.3,
    negativeMentionsYoY: -4.1,
    negativeRateMoM: -2.3,
    negativeRateYoY: -4.1,
    negativeAyg: 14.8,
    emotionType: '3'
  }))
})

const channelTrendRows = () =>
  trend().map(item => ({
    date: item.date,
    chDatas: channelRows().map((channel, index) => ({
      channelName: channel.channelName,
      channelCode: channel.channelCode,
      date: item.date,
      value: Number((item.negativeRate + index * 1.2).toFixed(1)),
      valueMoM: channel.negativeRateMoM,
      valueYoY: channel.negativeRateYoY
    }))
  }))

const sceneAnalysisResult = database => ({
  name: '智行 S7',
  avgName: '集团均值',
  tagData: insightRows(database).map(item => ({
    tagName: item.tagName,
    tagCode: item.tagCode,
    value: item.mentions,
    valueAvg: Math.round(item.mentions * 0.82),
    valueMoM: item.mentionsMoM,
    valueAvgMoM: Number((item.mentionsMoM - 1.2).toFixed(1)),
    valueYoY: item.mentionsYoY,
    valueAvgYoY: Number((item.mentionsYoY - 1.8).toFixed(1))
  }))
})

const sceneAnalysisResultForPath = (pathname, database, body = {}) => {
  const value = pathname.toLowerCase()
  if (
    !/(competitor-compare|journey-analysis|product-analysis|service-analysis|new-car-launch|hot-event|keyaccount)/.test(
      value
    )
  )
    return null
  if (value.includes('/keyaccount/')) {
    if (value.endsWith('/getkeyaccountbrief')) {
      return {
        ...briefResult(),
        comments: 32680,
        commentsMoM: 5.6,
        commentsYoY: 9.4
      }
    }
    if (value.endsWith('/getdatatrendchange')) return trendResult()
    if (value.endsWith('/getfocusscenetop')) return sceneRows(database)
    if (value.endsWith('/getuseopinionwordcloud')) {
      return opinionRows(database).map(item => ({
        ...item,
        name: item.opinion,
        value: item.mentions
      }))
    }
    if (value.endsWith('/getuserintentionopiniontop')) return opinionRows(database)
    if (value.endsWith('/getfocussceneanalysis')) {
      return sceneRows(database).map((item, index) => ({
        ...item,
        tag2Name: item.sceneName,
        tag2Code: item.sceneCode,
        positiveMentions: 6200 - index * 430,
        neutralMentions: 2100 - index * 120,
        negativeMentions: 1350 + index * 110
      }))
    }
    if (value.endsWith('/getfocussceneanalysisresult')) return insightRows(database)
  }
  if (value.includes('/hot-event/') && value.endsWith('/getfocussceneanalysis')) {
    return sceneRows(database).map((item, index) => ({
      ...item,
      tag2Name: item.sceneName,
      tag2Code: item.sceneCode,
      positiveMentions: 6200 - index * 430,
      neutralMentions: 2100 - index * 120,
      negativeMentions: 1350 + index * 110
    }))
  }
  if (value.includes('/journey-analysis/') && value.endsWith('/getuserintentionopiniontop')) {
    return {
      originalSound: {
        id: database.voices[0].id,
        content: database.voices[0].content,
        topics: [database.voices[0].topic],
        userName: database.voices[0].customerName
      },
      opinionTops: opinionRows(database)
    }
  }
  if (value.includes('/new-car-launch/') && value.endsWith('/getproductbrief')) {
    return ['预热期', '上市期', '稳定期'].map((phase, index) => ({
      brandName: database.brands[index].name,
      seriesName: database.brands[index].series[0],
      logo: '',
      phase,
      positiveRate: Number((66 - index * 3.4).toFixed(1)),
      negativeRate: Number((12.8 + index * 1.9).toFixed(1)),
      mentionCount: String(12800 - index * 1850)
    }))
  }
  if (
    value.endsWith('/getproductbrief') ||
    value.endsWith('/gethoteventresultbrief') ||
    value.endsWith('/gethoteventoriginbrief')
  )
    return briefResult()
  if (value.endsWith('/getuserintentionopiniontop') || value.endsWith('/getuseopinionwordcloud'))
    return opinionRows(database)
  if (
    value.endsWith('/getfocusscenetop') ||
    value.endsWith('/getuserfocusscenetop') ||
    value.endsWith('/gethighfreqscenetop') ||
    value.endsWith('/getsurgingscenetop')
  )
    return sceneRows(database)
  if (value.endsWith('/getfocussceneanalysis')) return sceneAnalysisResult(database)
  if (
    value.endsWith('/getdatatrendchange') ||
    value.endsWith('/getresultdatatrendchange') ||
    value.endsWith('/getorigindatatrendchange')
  )
    return trendResult()
  if (value.endsWith('/getdatasourceanalysis') || value.endsWith('/getorigindatasourceanalysis'))
    return channelRows()
  if (value.endsWith('/getchannelnegativetrend')) return channelTrendRows()
  if (value.endsWith('/getchannelmentionshare')) return channelRows()
  if (value.endsWith('/getprovincerank') || value.endsWith('/getprovincemap'))
    return ['广东省', '江苏省', '浙江省', '四川省', '湖北省'].map((provinceName, index) => ({
      provinceName,
      provinceCode: `voc-province-${index + 1}`,
      negativeRate: Number((12.8 + index * 1.6).toFixed(1)),
      negativeRateMoM: Number((-2.4 + index * 0.5).toFixed(1)),
      negativeRateYoY: Number((-4.1 + index * 0.7).toFixed(1)),
      mentions: 8600 - index * 980
    }))
  if (value.endsWith('/getdealerranktop'))
    return ['智行体验中心一店', '远途服务中心', '凌峰用户中心', '星迈交付中心', '云驰服务站'].map(
      (dealerName, index) => ({
        dealerName,
        dealerCode: `voc-dealer-${index + 1}`,
        provinceName: ['上海市', '杭州市', '成都市', '武汉市', '南京市'][index],
        negativeRate: Number((11.6 + index * 1.5).toFixed(1)),
        mentions: 3200 - index * 380,
        score: 94 - index * 3
      })
    )
  if (value.endsWith('/getvoiceusertop'))
    return database.users.map((user, index) => ({
      userName: user.name,
      userId: user.id,
      value: 1680 - index * 210,
      negativeRate: 12.4 + index * 1.7,
      valueMoM: 6.2 - index,
      valueYoY: 10.8 - index
    }))
  if (value.endsWith('/getusertypedistribution'))
    return ['新用户', '活跃用户', '忠诚用户', '流失预警用户'].map((userType, index) => ({
      userType,
      value: 6200 - index * 780,
      percent: 36 - index * 7,
      valueMoM: 4.8 - index,
      valueYoY: 8.6 - index
    }))
  if (value.endsWith('/getregiondistribution'))
    return ['华东', '华南', '西南', '华中', '华北'].map((provinceName, index) => ({
      provinceName,
      provinceCode: `voc-region-${index + 1}`,
      value: 7200 - index * 860,
      percent: 31 - index * 4.5,
      valueMoM: 5.6 - index * 0.7,
      valueYoY: 9.8 - index
    }))
  if (value.endsWith('/getgenderdistribution'))
    return [
      { gender: '男性', value: 10820, percent: 58, valueMoM: 2.4, valueYoY: 4.6 },
      { gender: '女性', value: 7820, percent: 42, valueMoM: 4.1, valueYoY: 7.2 }
    ]
  if (value.endsWith('/getagedistribution'))
    return ['18-25岁', '26-35岁', '36-45岁', '46岁以上'].map((title, index) => ({
      title,
      value: 5600 - index * 620,
      percent: 34 - index * 6,
      valueMoM: 5.2 - index,
      valueYoY: 9.1 - index
    }))
  if (value.endsWith('/getjourneydetailanalysis'))
    return insightRows(database).map((item, index) => ({
      tagName: item.tagName,
      tagCode: item.tagCode,
      tagLevel: index < 2 ? 2 : 3,
      value: item.mentions,
      valueAvg: Math.round(item.mentions * 0.82),
      valueMoM: item.mentionsMoM,
      valueYoY: item.mentionsYoY
    }))
  if (value.endsWith('/defaultHighestBrandCar'.toLowerCase()))
    return (() => {
      const { selected } = comparisonContext(database, body)
      return { self: selected[0], competitor: selected[1] }
    })()
  if (value.endsWith('/getallbrandorcarseriesdata')) return comparisonOptions(database, body)
  if (value.endsWith('/getcomparativebrief')) return comparisonBriefRows(database, body)
  if (value.endsWith('/gettrendchangecompare')) return comparisonTrendRows(database, body)
  if (value.endsWith('/get-service-tag-analysis') || value.endsWith('/get-product-tag-analysis'))
    return comparisonTagRows(database, body)
  if (value.endsWith('/getcomparisondatasources')) return comparisonSourceRows(database, body)
  if (value.endsWith('/getscenecomparisontop') || value.endsWith('/getuseopinioncomparisontop')) {
    const entity = comparisonEntityFromBody(database, body)
    const metric = comparisonMetric(entity, 0)
    return {
      code: metric.code,
      name: metric.name,
      imgUrl: metric.imgUrl,
      sceneTopVos: sceneRows(database),
      opinionTopVos: opinionRows(database)
    }
  }
  if (value.endsWith('/seriescondition'))
    return {
      newCarSeries: optionRows(database).slice(0, 2),
      compareCarSeries: optionRows(database).slice(2)
    }
  if (value.endsWith('/get-opinion-evaluation') || value.endsWith('/getopinionevaluation'))
    return {
      goodOpinions: opinionRows(database).filter(item => item.sentiment === '正面'),
      badOpinions: opinionRows(database).filter(item => item.sentiment === '负面')
    }
  if (value.endsWith('/data-source-analysis')) return channelRows()
  return null
}

const optionRows = database =>
  database.brands.map((brand, index) => ({
    id: brand.id,
    key: brand.id,
    code: brand.id,
    value: brand.name,
    label: brand.name,
    name: brand.name,
    sort: index + 1,
    children: brand.series.map((series, seriesIndex) => ({
      id: `${brand.id}-${seriesIndex + 1}`,
      key: `${brand.id}-${seriesIndex + 1}`,
      code: `${brand.id}-${seriesIndex + 1}`,
      value: series,
      label: series,
      name: series,
      children: []
    }))
  }))

const normalizeCodeList = value => {
  if (Array.isArray(value)) return value.filter(Boolean)
  if (!value) return []
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) return parsed.filter(Boolean)
    } catch {}
    return value.split(',').map(item => item.trim()).filter(Boolean)
  }
  return []
}

const comparisonOptions = (database, body = {}) => {
  const isSeries = body.queryType === 'series'
  if (!isSeries) {
    return database.brands.map((brand, index) => ({
      code: brand.id,
      name: brand.name,
      imgUrl: brand.imageUrl,
      brandIndex: index,
      seriesIndex: 0
    }))
  }

  return database.brands.flatMap((brand, brandIndex) =>
    brand.series.map((name, seriesIndex) => ({
      code: `${brand.id}-${seriesIndex + 1}`,
      name,
      imgUrl: brand.imageUrl,
      brandIndex,
      seriesIndex
    }))
  )
}

const comparisonContext = (database, body = {}) => {
  const options = comparisonOptions(database, body)
  const requestedCodes = normalizeCodeList(
    body.queryType === 'series' ? body.carSeriesList : body.brandCodeList
  )
  const selected = requestedCodes
    .map(code => options.find(option => option.code === code))
    .filter(Boolean)

  const defaults =
    body.queryType === 'series'
      ? options.filter(option => option.seriesIndex === 0).slice(0, 2)
      : options.slice(0, 2)

  for (const option of defaults) {
    if (selected.length >= 2) break
    if (!selected.some(item => item.code === option.code)) selected.push(option)
  }

  for (const option of options) {
    if (selected.length >= 2) break
    if (!selected.some(item => item.code === option.code)) selected.push(option)
  }

  return { options, selected: selected.slice(0, 2) }
}

const comparisonMetric = (entity, position) => {
  if (!entity) {
    return {
      code: 'market-average',
      name: '市场均值',
      imgUrl: '',
      negativeRate: 16.4,
      negativeRateMoM: -0.8,
      negativeRateYoY: -1.6,
      mentions: 32600,
      mentionsMoM: 5.2,
      mentionsYoY: 8.6
    }
  }

  const offset = entity.brandIndex * 1.7 + entity.seriesIndex * 0.6
  return {
    code: entity.code,
    name: entity.name,
    imgUrl: entity.imgUrl,
    negativeRate: Number((12.8 + offset).toFixed(1)),
    negativeRateMoM: Number((-2.4 + offset * 0.25).toFixed(1)),
    negativeRateYoY: Number((-4.2 + offset * 0.4).toFixed(1)),
    mentions: Math.max(6200, 12800 - entity.brandIndex * 1350 - entity.seriesIndex * 720),
    mentionsMoM: Number((8.2 - position * 0.9).toFixed(1)),
    mentionsYoY: Number((12.4 - position * 1.1).toFixed(1))
  }
}

const comparisonBriefRows = (database, body = {}) => {
  const { selected } = comparisonContext(database, body)
  return [comparisonMetric(undefined, 0), ...selected.map(comparisonMetric)]
}

const comparisonTrendRows = (database, body = {}) => {
  const entities = comparisonBriefRows(database, body)
  return trend().map((point, pointIndex) => ({
    date: point.date,
    items: entities.map((entity, entityIndex) => ({
      ...entity,
      date: point.date,
      negativeRate: Number((entity.negativeRate + (6 - pointIndex) * 0.12).toFixed(1)),
      mentions: Math.round(entity.mentions * (0.82 + pointIndex * 0.014)),
      mentionsMoM: Number((entity.mentionsMoM + pointIndex * 0.08).toFixed(1))
    }))
  }))
}

const comparisonEntityFromBody = (database, body = {}) => {
  const { options } = comparisonContext(database, body)
  const code = body.queryType === 'series' ? body.carSeriesCode : body.brandCode
  return code ? options.find(option => option.code === code) : undefined
}

const comparisonTagRows = (database, body = {}) => {
  const entities = comparisonBriefRows(database, body)
  const dimensions = [
    ['产品体验', '智能交互'],
    ['产品体验', '驾乘空间'],
    ['产品体验', '品牌认知'],
    ['服务体验', '服务效率'],
    ['服务体验', '可靠性']
  ]

  return entities.flatMap((entity, entityIndex) =>
    dimensions.map(([tag1Name, tag2Name], tagIndex) => ({
      name: entity.name,
      code: entity.code,
      imageUrl: entity.imgUrl,
      imgUrl: entity.imgUrl,
      tag1Name,
      tag1Code: tag1Name === '产品体验' ? 'voc-product' : 'voc-service',
      tag2Name,
      tag2Code: `voc-tag2-${tagIndex + 1}`,
      mentions: Math.max(3200, entity.mentions - tagIndex * 620),
      mentionCount: Math.max(3200, entity.mentions - tagIndex * 620),
      negativeRate: Number((entity.negativeRate + tagIndex * 0.8).toFixed(1)),
      negativeRateMoM: Number((entity.negativeRateMoM + tagIndex * 0.3).toFixed(1)),
      mentionsMoM: Number((entity.mentionsMoM - tagIndex * 0.4).toFixed(1)),
      value1: Number((entity.negativeRate + tagIndex * 0.8).toFixed(1)),
      value1MoM: Number((entity.negativeRateMoM + tagIndex * 0.3).toFixed(1)),
      value1YoY: Number((entity.negativeRateYoY + tagIndex * 0.25).toFixed(1)),
      value2: Math.max(3200, entity.mentions - tagIndex * 620),
      value2MoM: Number((entity.mentionsMoM - tagIndex * 0.4).toFixed(1)),
      value2YoY: Number((entity.mentionsYoY - tagIndex * 0.5).toFixed(1)),
      rateColor: ['#1677ff', '#0f8f5b', '#d46b08'][entityIndex],
      rateBackgroundColor: ['#eaf3ff', '#e8f8f1', '#fff7e6'][entityIndex]
    }))
  )
}

const comparisonSourceRows = (database, body = {}) =>
  comparisonBriefRows(database, body).flatMap(entity =>
    channelRows().map((channel, channelIndex) => ({
      name: entity.name,
      code: entity.code,
      imgUrl: entity.imgUrl,
      channelName: channel.channelName,
      channelCode: channel.channelCode,
      mentions: Math.max(1200, entity.mentions - channelIndex * 620),
      mentionsMoM: Number((entity.mentionsMoM - channelIndex * 0.5).toFixed(1)),
      mentionsYoY: Number((entity.mentionsYoY - channelIndex * 0.7).toFixed(1)),
      negativeRate: Number((entity.negativeRate + channelIndex * 0.9).toFixed(1)),
      negativeRateMoM: Number((entity.negativeRateMoM + channelIndex * 0.3).toFixed(1)),
      negativeRateYoY: Number((entity.negativeRateYoY + channelIndex * 0.4).toFixed(1))
    }))
  )

const conditionResult = database => ({
  dataChannel: optionRows(database),
  channelList: optionRows(database),
  topicList: insightRows(database).map(item => ({
    label: item.tagName,
    value: item.tagCode,
    key: item.tagCode
  })),
  taskStatusList: ['待处理', '处理中', '待确认', '已完成'].map((label, index) => ({
    label,
    value: String(index + 1),
    key: String(index + 1)
  })),
  eventPriorityList: ['P0', 'P1', 'P2', 'P3'].map(value => ({ label: value, value, key: value })),
  eventLevelList: ['高', '中', '低'].map(value => ({ label: value, value, key: value })),
  closeReasonList: ['问题已解决', '转专项处理'].map(value => ({ label: value, value, key: value })),
  rejectReasonList: ['信息不完整', '责任部门不匹配'].map(value => ({
    label: value,
    value,
    key: value
  }))
})

const arrayResult = (pathname, database, body = {}) => {
  const value = pathname.toLowerCase()
  if (value.includes('findallfinaltaglibclientvolist')) {
    return insightRows(database).map(item => ({
      tagName: item.tagName,
      tagCode: item.tagCode,
      label: item.tagName,
      value: item.tagCode,
      key: item.tagCode
    }))
  }
  if (value.endsWith('/vocleadership/getgroupbrief')) {
    const rows = insightRows(database)
    return rows.map((brand, index) => ({
      ...brand,
      name: index === 0 ? '智行汽车集团' : brand.brandName,
      brandImage: brand.imgUrl,
      topDataList: [
        {
          id: `${brand.id}-average`,
          name: '市场均值',
          mentionCount: 10320 - index * 820,
          ringRatio: 3.2,
          yearOnYearRatio: 7.8,
          negativeRate: 16.8,
          negativeRingRatio: -1.4,
          negativeYearOnYearRatio: -2.6
        },
        ...rows.slice(0, 3).map((item, itemIndex) => ({
          ...item,
          id: `${brand.id}-${item.id}`,
          ranking: itemIndex + 1,
          rankingFlag: itemIndex % 2 === 0 ? 'rising' : 'falling',
          lastRanking: itemIndex + 1,
          ringRatio: item.mentionsMoM,
          yearOnYearRatio: item.mentionsYoY,
          negativeRingRatio: item.negativeRateMoM,
          negativeYearOnYearRatio: item.negativeRateYoY
        }))
      ]
    }))
  }
  if (value.endsWith('/vocleadership/getbrandranking')) {
    return insightRows(database).map((item, index) => ({
      ...item,
      ranking: index + 1,
      rankingFlag: index % 2 === 0 ? 'rising' : 'falling',
      lastRanking: index % 3 === 0 ? 2 : 1,
      image: item.imgUrl,
      type: index === 0 ? '1' : '2',
      mentionRingRatio: item.mentionsMoM,
      mentionYearOnYearRatio: item.mentionsYoY,
      mentionRatio: Number((26.4 - index * 3.8).toFixed(1)),
      negativeRingRatio: item.negativeRateMoM,
      negativeYearOnYearRatio: item.negativeRateYoY,
      focusSceneTop3: sceneRows(database)
        .slice(0, 3)
        .map(scene => ({
          ...scene,
          value: body.dataType === 'mention' ? scene.mentions : scene.negativeRate,
          ringRatio:
            body.dataType === 'mention' ? scene.mentionsMoM : scene.negativeRateMoM,
          yearOnYearRatio:
            body.dataType === 'mention' ? scene.mentionsYoY : scene.negativeRateYoY
        }))
    }))
  }
  if (value.endsWith('/vocleadership/getbrandinsight')) {
    return insightRows(database).map(item => ({
      ...item,
      name: item.brandName,
      imgUrl: item.imgUrl,
      growthTrend: trend()
        .slice(-7)
        .map(point => point.mentionCount)
    }))
  }
  if (value.endsWith('/vocleadership/getproductscenarioanalysis')) {
    return leadershipScenarioRows(database, 'product')
  }
  if (value.endsWith('/vocleadership/getservicescenarioanalysis')) {
    return leadershipScenarioRows(database, 'service')
  }
  if (value.endsWith('/vocleadership/getuserintentionopiniontop')) {
    return leadershipOpinionRows(database, body.intention)
  }
  if (value.includes('/group-analysis/')) {
    if (value.endsWith('/getproductbrief')) return null
    if (value.endsWith('/get-brand-trend-change')) return brandTrendRows(database)
    if (
      value.endsWith('/get-product-tag-analysis') ||
      value.endsWith('/get-service-reputation-analysis')
    ) {
      return tagMatrixRows(database)
    }
    if (value.endsWith('/get-brand-series-rank')) {
      return insightRows(database).map((item, index) => ({
        ...item,
        code: item.brandCode,
        name: item.brandName,
        imageUrl: item.brandImageUrl,
        mentionsTrend: trend()
          .slice(-7)
          .map(point => String(point.mentionCount - index * 75)),
        negativeMentionsTrend: trend()
          .slice(-7)
          .map(point => String(Math.round(point.negativeMentions + index * 8)))
      }))
    }
    if (value.endsWith('/get-opinion-evaluation')) {
      return insightRows(database).map(item => ({
        ...item,
        goodOpinions: insightRows(database)
          .slice(0, 3)
          .map((row, index) => ({
            opinion: detailedOpinionTexts.positive[index],
            mentions: row.mentions,
            mentionsMoM: row.mentionsMoM,
            mentionsYoY: row.mentionsYoY,
            sentiment: '正面'
          })),
        badOpinions: insightRows(database)
          .slice(2, 5)
          .map((row, index) => ({
            opinion: detailedOpinionTexts.negative[index],
            mentions: row.mentions,
            mentionsMoM: row.mentionsMoM,
            mentionsYoY: row.mentionsYoY,
            sentiment: '负面'
          }))
      }))
    }
    return insightRows(database)
  }
  if (value.includes('/product-self-analysis/')) {
    if (value.endsWith('/getproductbrief')) return null
    if (value.endsWith('/getfocusscenetop')) return sceneRows(database)
    if (value.endsWith('/getdatatrendchange')) return trendResult()
    if (value.endsWith('/getdatasourceanalysis')) return channelRows()
    if (value.endsWith('/getchannelnegativetrend')) return channelTrendRows()
    if (value.endsWith('/getchannelmentionshare')) return channelRows()
    if (value.endsWith('/user-journey-analysis')) {
      return ['认知', '选择', '购买', '使用', '维保', '再购'].map((journeyName, index) => ({
        journeyName,
        journeyCode: `voc-journey-${index + 1}`,
        mentions: 9800 - index * 870,
        mentionsMoM: Number((7.6 - index * 0.8).toFixed(1)),
        mentionsYoY: Number((12.2 - index).toFixed(1)),
        negativeRate: Number((11.8 + index * 1.4).toFixed(1)),
        negativeRateMoM: Number((-2.6 + index * 0.5).toFixed(1)),
        emotionType: index < 2 ? 4 : index < 4 ? 3 : 2,
        rateBackgroundColor: '#fff1f0',
        rateColor: '#f53f3f',
        satisfiedTop5: insightRows(database)
          .slice(0, 5)
          .map(item => ({
            opinionName: item.tagName,
            mentions: item.mentions,
            mom: item.mentionsMoM,
            yoy: item.mentionsYoY,
            sentiment: '正面'
          })),
        dissatisfiedTop5: insightRows(database)
          .slice(0, 5)
          .map(item => ({
            opinionName: item.scene,
            mentions: item.mentions,
            mom: item.mentionsMoM,
            yoy: item.mentionsYoY,
            sentiment: '负面'
          }))
      }))
    }
    return insightRows(database)
  }
  if (value.includes('/mobileterminal/rootcause-analysis/')) {
    if (value.endsWith('/getuserintentionopiniontop')) {
      return insightRows(database).map(item => ({
        opinion: item.tagName,
        sentiment: '负面',
        mentions: item.mentions,
        mentionsMoM: item.mentionsMoM,
        mentionsYoY: item.mentionsYoY,
        remark: [item.scene]
      }))
    }
    return insightRows(database)
  }
  if (value.endsWith('/user-browse-record/browse-trend')) {
    return trend()
      .slice(-7)
      .map(item => ({
        dateTime: item.date,
        totalCount: item.mentionCount,
        complainCount: 32,
        consultCount: 24,
        suggestionCount: 18,
        praiseCount: 42
      }))
  }
  if (
    /(getuserchanneltree|finddeparttree|finddepartaccounttree|finddepartaccounttreebydeptid|findaccountbydeptid|queryrolealllist|getrolefiltertypelist|findvisibleuserlist|accountlist|getuserlist|creator|findkeyaccountlist|findallfinaltaglibclientvolist|findallattributelabellist|gettaglibclienttree|display-rule\/list|dict-items-by-dict)/i.test(
      pathname
    ) && !value.includes('/drill-down/')
  ) {
    if (value.includes('role'))
      return database.roles.map(role => ({
        ...role,
        roleName: role.name,
        roleId: role.id,
        label: role.name,
        value: role.id
      }))
    if (value.includes('display-rule'))
      return insightRows(database)
        .slice(0, 3)
        .map((item, index) => ({
          id: `rule-${index + 1}`,
          ruleName: `${item.tagName}展示规则`,
          name: `${item.tagName}展示规则`,
          sortNo: index + 1,
          status: 1
        }))
    if (value.includes('account') || value.includes('user') || value.includes('creator'))
      return database.users.map(user => ({
        ...user,
        label: user.name,
        value: user.id,
        userId: user.id,
        employeeNo: user.id
      }))
    if (value.includes('findkeyaccount')) return optionRows(database)
    return optionRows(database)
  }
  if (value.endsWith('/data-plaza/category/tree')) {
    return database.categories.map(category => ({
      ...category,
      categoryName: category.name,
      parentId: '0',
      categoryLevel: 1,
      reportCount: 4,
      children: []
    }))
  }
  if (value.endsWith('/data-plaza/conditions') || value.endsWith('/accountinfo/conditions')) {
    return Object.entries(conditionResult(database)).map(([key, details]) => ({ key, details }))
  }
  if (value.endsWith('/drill-down/conditions')) {
    return [
      { key: 'competitiveTree', details: optionRows(database) },
      { key: 'newCarTree', details: optionRows(database) },
      {
        key: 'tagType',
        details: insightRows(database).map(item => ({ key: item.tagCode, value: item.tagName }))
      }
    ]
  }
  if (value.includes('/drill-down/')) {
    const tableRows = drillDownTableRows()
    if (value.endsWith('/getdrilldownbrief')) {
      return {
        ...briefResult(),
        negativeMentions: 7196,
        negativeMentionsMoM: -2.3,
        positiveMentions: 29756,
        positiveMentionsMoM: 5.8
      }
    }
    if (value.endsWith('/data-trend-change')) return trendResult()
    if (value.endsWith('/indicator-rank')) return drillDownIndicators().slice(0, 8)
    if (value.endsWith('/indicator-list')) return { list: tableRows, total: tableRows.length }
    if (value.endsWith('/scene-list')) {
      const rows = [
        ['智能出行', '车机交互'],
        ['智能出行', '导航与娱乐'],
        ['日常用车', '城市通勤'],
        ['日常用车', '长途出行'],
        ['服务体验', '进店维修'],
        ['服务体验', '交付与回访'],
        ['能源补给', '低温充电'],
        ['家庭出行', '多人乘坐']
      ].map(([scenarioFirst, scenario], index) => ({
        scenarioFirst,
        scenario,
        mentions: 11200 - index * 880,
        mentionsShare: Number((24.6 - index * 2.1).toFixed(1)),
        mentionTrend: trend()
          .slice(-7)
          .map((_, pointIndex) => String(8200 - index * 410 + pointIndex * 120)),
        mentionsMoM: Number((7.2 - index * 0.55).toFixed(1))
      }))
      return { list: rows, total: rows.length }
    }
    if (value.endsWith('/opinion-list')) {
      const rows = [...detailedOpinionTexts.positive, ...detailedOpinionTexts.negative].map(
        (opinion, index) => ({
          opinion,
          intention: index < 3 ? '表扬' : '抱怨',
          mentions: 5280 - index * 430,
          mentionsShare: Number((18.6 - index * 1.5).toFixed(1)),
          mentionTrend: trend()
            .slice(-7)
            .map((_, pointIndex) => String(3800 - index * 210 + pointIndex * 90)),
          mentionsMoM: Number((5.4 - index * 0.8).toFixed(1)),
          tid: `voc-opinion-${index + 1}`
        })
      )
      return { list: rows, total: rows.length }
    }
    if (
      value.endsWith('/opinion-evaluate-top') ||
      value.endsWith('/province-opinion-evaluate-top')
    ) {
      return detailedOpinionTexts.negative.map((opinion, index) => ({
        opinion,
        positiveMentions: 860 - index * 80,
        negativeMentions: 2680 - index * 240,
        neutralMentions: 1120 - index * 90,
        totalMentions: 4660 - index * 410
      }))
    }
    if (value.endsWith('/channel-top')) {
      return channelRows().map(item => ({
        channelName: item.channelName,
        channelCode: item.channelCode,
        value: item.mentions
      }))
    }
    if (value.endsWith('/data-source-list')) {
      return channelRows().map((item, index) => ({
        ...tableRows[index],
        ...item,
        mentionsShare: Number((31.5 - index * 4.6).toFixed(1)),
        mentionTrend: tableRows[index].mentionTrend,
        negativeRateValue: item.negativeRate,
        negativeTrend: tableRows[index].negativeTrend,
        positiveRateValue: Number((63.2 - index * 1.4).toFixed(1)),
        positiveTrend: tableRows[index].positiveTrend,
        neutralRateValue: Number((24.4 - index * 0.3).toFixed(1)),
        neutralTrend: tableRows[index].neutralTrend
      }))
    }
    if (value.endsWith('/brand-brief')) return briefResult()
    if (value.endsWith('/car-series-rank')) {
      return database.brands.flatMap(brand =>
        brand.series.map((carSeriesName, index) => ({
          carSeriesName,
          carSeriesCode: `${brand.id}-series-${index + 1}`,
          negativeRate: Number((12.6 + index * 1.7).toFixed(1)),
          mentions: 12600 - index * 920,
          mom: Number((6.8 - index * 0.7).toFixed(1)),
          yoy: Number((11.4 - index).toFixed(1))
        }))
      )
    }
    if (value.endsWith('/car-series-list')) {
      const rows = database.brands.flatMap((brand, brandIndex) =>
        brand.series.map((carSeriesName, seriesIndex) => ({
          ...tableRows[(brandIndex + seriesIndex) % tableRows.length],
          brandName: brand.name,
          brandCode: brand.id,
          carSeriesName,
          carSeriesCode: `${brand.id}-series-${seriesIndex + 1}`
        }))
      )
      return { list: rows, total: rows.length }
    }
    if (value.endsWith('/getprovincemap')) {
      return ['广东省', '江苏省', '浙江省', '四川省', '湖北省', '山东省'].map(
        (provinceName, index) => ({
          provinceName,
          provinceCode: `voc-province-${index + 1}`,
          negativeRate: Number((12.4 + index * 0.8).toFixed(1)),
          negativeRateMoM: Number((-2.2 + index * 0.25).toFixed(1)),
          negativeRateYoY: Number((-4.1 + index * 0.4).toFixed(1)),
          mentions: 8200 - index * 760
        })
      )
    }
    if (value.endsWith('/getdealerranktop')) {
      return [
        '智行体验中心一店',
        '远途用户中心',
        '凌峰交付中心',
        '星迈服务中心',
        '云驰体验空间'
      ].map((dealerName, index) => ({
        dealerName,
        dealerCode: `voc-dealer-${index + 1}`,
        provinceName: ['广东省', '江苏省', '浙江省', '四川省', '湖北省'][index],
        provinceCode: `voc-province-${index + 1}`,
        negativeRate: Number((11.8 + index * 1.2).toFixed(1)),
        negativeRateMoM: Number((-2.4 + index * 0.3).toFixed(1)),
        negativeRateYoY: Number((-4.2 + index * 0.5).toFixed(1)),
        mentions: 5680 - index * 520,
        mentionsMoM: Number((7.8 - index * 0.7).toFixed(1)),
        mentionsYoY: Number((12.6 - index * 0.9).toFixed(1))
      }))
    }
    if (value.endsWith('/data-province-list')) {
      const rows = ['广东省', '江苏省', '浙江省', '四川省', '湖北省', '山东省'].map(
        (name, index) => ({
          ...tableRows[index],
          name,
          code: `voc-province-${index + 1}`
        })
      )
      return { list: rows, total: rows.length }
    }
    if (value.endsWith('/getgenderdistribution'))
      return [
        { gender: '男性', value: 10624, percent: 57, valueMoM: 2.6, valueYoY: 5.1 },
        { gender: '女性', value: 8016, percent: 43, valueMoM: 3.2, valueYoY: 6.4 }
      ]
    if (value.endsWith('/getagedistribution'))
      return ['18-25岁', '26-35岁', '36-45岁', '46岁以上'].map((title, index) => ({
        title,
        value: 5200 - index * 680,
        percent: 31 - index * 5,
        valueMoM: 3.8 - index * 0.5,
        valueYoY: 6.2 - index * 0.7
      }))
    if (value.endsWith('/getusertypedistribution'))
      return ['首购用户', '增换购用户', '潜在用户'].map((userType, index) => ({
        userType,
        value: 8200 - index * 1600,
        percent: 44 - index * 9,
        valueMoM: 4.1 - index * 0.8,
        valueYoY: 7.4 - index
      }))
    if (value.endsWith('/getregiondistribution'))
      return ['广东省', '江苏省', '浙江省', '四川省', '湖北省'].map((provinceName, index) => ({
        provinceName,
        provinceCode: `voc-province-${index + 1}`,
        value: 4680 - index * 510,
        percent: 25 - index * 3.2,
        valueMoM: 4.6 - index * 0.5,
        valueYoY: 8.1 - index * 0.8
      }))
    if (value.endsWith('/getuserfocusscenetop'))
      return sceneRows(database).map((item, index) => ({
        sceneName: item.sceneName,
        value: item.mentions,
        ringRatio: item.mentionsMoM,
        yearOnYearRatio: item.mentionsYoY,
        ranking: index + 1
      }))
    if (value.endsWith('/getuserlist')) {
      const rows = database.users.map((user, index) => ({
        userName: user.name,
        userId: user.id,
        value: 860 - index * 90,
        negativeRate: 12.8 + index * 1.4,
        valueMoM: 4.6 - index * 0.6,
        valueYoY: 8.4 - index,
        dataSource: 3 + index,
        postCount: 62 - index * 4,
        complainCount: 18 + index,
        consultCount: 14 + index,
        suggestCount: 10 + index,
        praiseCount: 20 - index
      }))
      return { list: rows, total: rows.length }
    }
  }
  if (/\/(?:mobileterminal\/)?single-event\/get-detail-events?$/.test(value)) {
    const event =
      database.events.find(item => item.id === String(body.id || '')) || database.events[1]
    const voice =
      database.voices.find(item => item.id === String(body.dataId || '')) || database.voices[0]
    const detailResult = {
      ...event,
      dataId: body.dataId || event.voiceIds?.[0],
      warningEventNo: event.eventId,
      eventName: event.name,
      eventPriority: event.priority.toLowerCase(),
      eventPriorityName: event.priority,
      eventLevel: 'high',
      eventLevelName: '高',
      sensitiveType: '体验风险',
      eventClarity: '信息完整',
      mainRespOrgId: 'voc-dept-customer-experience',
      mainRespOrgName: event.department,
      mainRespUserId: 'demo-owner',
      mainRespUserName: event.owner,
      warningTime: event.createTime,
      eventHandler: { userId: 'demo-owner', userName: event.owner },
      handleUser: { userId: 'demo-owner', userName: event.owner },
      handleUsers: [{ userId: 'demo-owner', userName: event.owner }],
      ccUsers: database.users.slice(0, 2).map((user, index) => ({
        userId: user.id,
        userName: user.name,
        userEmpNo: `VOC${String(index + 1).padStart(4, '0')}`,
        orgId: `voc-dept-${index + 1}`,
        orgName: user.department
      })),
      eventValidity: 'valid',
      eventValidityName: '有效事件',
      eventProcessStartTime: event.createTime,
      eventProcessEndTime: `${event.createTime.slice(0, 10)} 18:00:00`,
      isProcessed: event.status === '已完成' ? '1' : '0',
      isProcessedName: event.status === '已完成' ? '已处理' : '处理中',
      processDescription: '已完成问题复现、责任分派与客户回访安排。',
      reviewProgressCode: 'IN_PROGRESS',
      reviewProgressName: '回评中',
      reviewDate: `${event.createTime.slice(0, 10)} 16:30:00`,
      reviewHandler: { userId: 'demo-analyst', userName: '体验分析师' },
      reviewContent: '客户已确认收到处理进展，等待版本验证完成后进行最终回访。',
      privateMsgProgressCode: 'REPLIED',
      privateMsgProgressName: '已回复',
      privateMsgCount: '2',
      privateMsgChannel: 'voc-channel-owner-community',
      privateMsgChannelName: voice.channel,
      custName: voice.customerName,
      custMobile: '138****6608',
      description: voice.content,
      mainPostDetails: voice.content,
      brandCode: database.brands[0].id,
      brandName: database.brands[0].name,
      showType: 2,
      originalTextScene: voice.content,
      topic: voice.topic,
      intentionType: voice.intention,
      domTagFirst: '产品体验',
      domTagSecond: '智能座舱',
      domTagThree: '系统稳定性',
      domTagFour: voice.topic,
      operateLogs: event.logs.map(log => ({
        id: log.id,
        eventId: event.id,
        dataId: body.dataId || event.voiceIds?.[0],
        operateTime: log.time,
        operateOrgName: event.department,
        operateUserId: 'demo-admin',
        operateUserName: log.operator,
        operateType: log.action,
        content: [{ contentType: 'text', content: `${log.action}：${event.name}` }]
      })),
      permissions: ['view', 'handle', 'close']
    }
    return value.endsWith('/get-detail-events') ? [detailResult] : detailResult
  }
  if (/\/(?:mobileterminal\/)?single-event\/get-detail-base$/.test(value)) {
    const voice =
      database.voices.find(item => item.id === String(body.dataId || '')) || database.voices[1]
    const brand = database.brands.find(item => item.id === voice.brandCode) || database.brands[0]
    return {
      dataId: voice.dataId,
      commentUserName: voice.customerName,
      commentUserId: `voc-user-${voice.id}`,
      commentTime: voice.mentionTime,
      commentDetails: voice.content,
      isMainPost: '1',
      channelName: voice.channel,
      channelCode: `voc-channel-${(database.voices.indexOf(voice) % 5) + 1}`,
      contentType: 'comment',
      contentTypeName: '评论',
      postUserId: `voc-user-${voice.id}`,
      postUserName: voice.customerName,
      postTime: voice.mentionTime,
      mainPostUrl: 'http://127.0.0.1:5173/#/h5/voiceDetail',
      mainPostTitle: voice.title,
      mainPostDetails: voice.content,
      brandCode: voice.brandCode,
      brandName: voice.brandName,
      carSeriesCode: `${voice.brandCode}-series`,
      carSeriesName: voice.carSeriesName,
      carModel: brand.series[0],
      engineNo: 'VOC-ENGINE-DEMO',
      licensePlateNo: '演示车辆',
      vinNo: 'VOCDEMO00000000001',
      carPurchaseTime: '2026-03-18',
      dealerName: '智行体验中心一店',
      originalTextScene: voice.content,
      intentions: [
        {
          id: `intention-${voice.id}`,
          intentionType: voice.sentiment === '负面' ? '抱怨' : '表扬',
          domTagFirst: '产品体验',
          domTagSecond: '智能座舱',
          domTagThree: '系统稳定性',
          domTagFour: voice.topic,
          topic: voice.topic,
          originalTextScene: voice.content
        }
      ],
      eventIds: database.events.slice(0, 3).map(item => item.id),
      editPermission: true
    }
  }
  if (/\/(?:mobileterminal\/)?single-event\/get-relation-events$/.test(value)) {
    return database.events.slice(0, 3).map(event => ({
      id: event.id,
      dataId: event.voiceIds?.[0],
      warningEventNo: event.eventId,
      taskStatus: event.taskStatus,
      taskStatusName: event.taskStatusName,
      mainRespOrgName: event.department
    }))
  }
  if (/\/(?:mobileterminal\/)?batch-event\/brief$/.test(value)) {
    const event =
      database.events.find(item => item.id === String(body.eventId || body.id || '')) ||
      database.events[0]
    return {
      ...event,
      warningEventNo: event.eventId,
      warningPeriod: '每日监测',
      warningTime: event.createTime,
      eventPriorityName: event.priority,
      brandCode: database.brands[0].id,
      brandName: database.brands[0].name,
      createUserName: '演示管理员',
      reviewUserName: '体验分析师',
      mainRespUserEmpNo: 'VOC0003',
      mainRespUserName: event.owner,
      primaryDepName: event.department,
      secondDeptName: '智行汽车集团',
      thirdDeptName: event.department,
      handleMode: 'VOC闭环',
      focusTopics: ['系统稳定性', '远程升级体验', '问题闭环时效']
    }
  }
  if (/\/(?:mobileterminal\/)?batch-event\/data-stat$/.test(value))
    return { negativeRatio: 0.148, positiveRatio: 0.612, mentionCount: 48600, userCount: 18600 }
  if (/\/(?:mobileterminal\/)?batch-event\/trend-stat$/.test(value)) {
    const points = trend().slice(-7)
    return {
      positive: points.map(item => ({ date: item.date, count: item.positiveMentions })),
      neutral: points.map(item => ({ date: item.date, count: item.neutralMentions })),
      negative: points.map(item => ({ date: item.date, count: item.negativeMentions }))
    }
  }
  if (/\/(?:mobileterminal\/)?batch-event\/car-series-stat$/.test(value))
    return database.brands.slice(0, 5).map((brand, index) => ({
      carSeriesName: brand.series[0],
      carSeriesCode: `${brand.id}-series`,
      num: 9800 - index * 1350
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/scene-stat$/.test(value))
    return sceneRows(database).map(scene => ({
      sceneName: scene.sceneName,
      sceneCode: scene.sceneCode,
      positiveCount: Math.round(scene.mentions * 0.62),
      neutralCount: Math.round(scene.mentions * 0.23),
      negativeCount: Math.round(scene.mentions * 0.15)
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/opinion-stat$/.test(value))
    return opinionRows(database).map(item => ({
      opinion: item.opinion,
      totalMentions: item.mentions,
      sentiment: item.sentiment
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/province-stat$/.test(value))
    return ['广东省', '江苏省', '浙江省', '四川省', '湖北省'].map((provinceName, index) => ({
      provinceCode: `voc-province-${index + 1}`,
      provinceName,
      num: 8600 - index * 980,
      percentage: Number((28.6 - index * 3.7).toFixed(1))
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/channel-stat$/.test(value))
    return channelRows().map(item => ({
      channelName: item.channelName,
      num: item.mentions,
      percentage: item.share
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/report-summary$/.test(value))
    return {
      summary:
        '智能座舱稳定性是本次批量事件的首要改善主题，远程升级后的卡顿反馈集中在高频通勤场景。建议由产品质量中心牵头完成版本验证，售后服务中心同步客户回访与进度告知。'
    }
  if (/\/(?:mobileterminal\/)?batch-event\/task-list$/.test(value))
    return database.events.slice(0, 4).map((event, index) => ({
      taskId: `voc-task-${index + 1}`,
      eventId: body.eventId || event.id,
      taskName: ['版本稳定性复测', '高频客户回访', '经销商处理培训', '专项结果复盘'][index],
      assigneeId: database.users[index % database.users.length].id,
      assigneeName: database.users[index % database.users.length].name,
      handleDeptId: `voc-dept-${index + 1}`,
      handleDeptName: database.users[index % database.users.length].department,
      progressStatus: String(index + 1),
      progressStatusName: ['待处理', '处理中', '待确认', '已完成'][index],
      deadline: event.createTime,
      taskDesc: '按演示闭环流程完成任务并同步处理结论。',
      progressRemark: '当前进度与计划一致。',
      editable: true,
      deletable: index === 0,
      reassignable: true,
      progressEditable: true
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/user-voice-list$/.test(value))
    return paginate(
      database.voices.map(voice => ({
        ...voice,
        newId: voice.id,
        originalId: voice.dataId,
        originalTextScene: voice.content,
        channelName: voice.channel,
        evaluateTime: voice.mentionTime,
        username: voice.customerName,
        topics: [voice.topic]
      })),
      body
    )
  if (/\/(?:mobileterminal\/)?batch-event\/ope-log-list$/.test(value))
    return database.events[0].logs.map(log => ({
      id: log.id,
      operateTime: log.time,
      operateUserName: log.operator,
      operateOrgName: '客户体验中心',
      operateTypeName: log.action,
      content: log.action
    }))
  if (/\/batch-event\/cc-user-list$/.test(value))
    return database.users.slice(0, 3).map((user, index) => ({
      id: `voc-cc-${index + 1}`,
      nodeUserId: user.id,
      nodeUserEmpNo: `VOC${String(index + 1).padStart(4, '0')}`,
      nodeUserName: user.name,
      nodeOrgId: `voc-dept-${index + 1}`,
      nodeOrgNo: `VOC-DEPT-${index + 1}`,
      nodeOrgName: user.department,
      nodeStatus: 'ACTIVE',
      leve2DeptId: 'voc-group-customer-experience',
      leve2DeptName: '客户体验中心',
      leve3DeptId: `voc-dept-${index + 1}`,
      leve3DeptName: user.department,
      allFlag: 0,
      createTime: database.events[0].createTime
    }))
  if (/\/(?:mobileterminal\/)?batch-event\/permission$/.test(value))
    return {
      editable: true,
      approvable: true,
      closable: true,
      reassignable: true,
      taskCreatable: true,
      viewEvent: true,
      approve: true,
      approveClose: true,
      closeEvent: true,
      confirm: true,
      createTask: true,
      editTask: true,
      deleteTask: true,
      updateTaskProgress: true,
      createEvent: true,
      exportEvent: true,
      addCcUser: true,
      rejectEvent: true,
      reassignHandler: true,
      roleType: 'SUPER_ADMIN',
      roleTypeName: '演示管理员'
    }
  if (/\/(?:mobileterminal\/)?batch-event\/init$/.test(value)) return 'READY'
  return null
}

const menuTree = () => [
  {
    id: 'menu-overview',
    name: 'VOC总览',
    permissionKey: 'vocView',
    path: '/',
    icon: 'menu-home-smile-fill',
    sort: 1,
    children: [
      {
        id: 'menu-overview-page',
        name: 'VOC总览',
        permissionKey: 'overview',
        path: '/overview',
        icon: 'menu-home-smile-fill',
        sort: 1
      }
    ]
  },
  {
    id: 'menu-leader-overview',
    name: '领导总览',
    permissionKey: 'leaderOverview',
    path: '/leaderOverview',
    icon: 'menu-home-smile-fill',
    sort: 2,
    children: [
      {
        id: 'menu-leader-overview-page',
        name: '领导总览',
        permissionKey: 'leaderOverviewPage',
        path: '/leaderOverview',
        icon: 'menu-home-smile-fill',
        sort: 1
      }
    ]
  },
  {
    id: 'menu-scene',
    name: '场景分析',
    permissionKey: 'sceneAnalysis',
    path: '/scene',
    icon: 'menu-function-add-fill',
    sort: 3,
    children: [
      {
        id: 'menu-scene-main',
        name: '场景分析',
        permissionKey: 'sceneAnalysisMain',
        path: '/scene/analysis',
        icon: 'menu-function-add-fill',
        sort: 1
      },
      {
        id: 'menu-scene-group',
        name: '集团分析',
        permissionKey: 'groupAnalysis',
        path: '/scene/groupAnalysis',
        icon: 'menu-function-add-fill',
        sort: 2
      },
      {
        id: 'menu-scene-product-own',
        name: '本品分析',
        permissionKey: 'thisProductAnalysis',
        path: '/scene/thisProductAnalysis',
        icon: 'menu-function-add-fill',
        sort: 3
      },
      {
        id: 'menu-scene-competitor',
        name: '竞品对比',
        permissionKey: 'competitorAnalysis',
        path: '/scene/competitorAnalysis',
        icon: 'menu-function-add-fill',
        sort: 4
      },
      {
        id: 'menu-scene-journey',
        name: '旅程分析',
        permissionKey: 'journeyAnalysis',
        path: '/scene/journeyAnalysis',
        icon: 'menu-function-add-fill',
        sort: 5
      },
      {
        id: 'menu-scene-product',
        name: '产品分析',
        permissionKey: 'productAnalysis',
        path: '/scene/productAnalysis',
        icon: 'menu-function-add-fill',
        sort: 6
      },
      {
        id: 'menu-scene-service',
        name: '服务分析',
        permissionKey: 'serviceAnalysis',
        path: '/scene/serviceAnalysis',
        icon: 'menu-function-add-fill',
        sort: 7
      },
      {
        id: 'menu-scene-new-car',
        name: '新车上市',
        permissionKey: 'newCarLaunch',
        path: '/scene/newCarLaunch',
        icon: 'menu-function-add-fill',
        sort: 8
      },
      {
        id: 'menu-scene-account',
        name: '重点账号',
        permissionKey: 'mainAccount',
        path: '/scene/mainAccount',
        icon: 'menu-function-add-fill',
        sort: 9
      },
      {
        id: 'menu-scene-hot',
        name: '热点事件',
        permissionKey: 'hotEvents',
        path: '/scene/hotEvents',
        icon: 'menu-function-add-fill',
        sort: 10
      }
    ]
  },
  {
    id: 'menu-self',
    name: '自助分析',
    permissionKey: 'selfServiceAnalysis',
    path: '/selfService',
    icon: 'menu-robot-3-fill',
    sort: 4,
    children: [
      {
        id: 'menu-self-root-cause',
        name: '根因分析',
        permissionKey: 'rootCause',
        path: '/rootCause',
        icon: 'search-eye-line',
        sort: 1
      },
      {
        id: 'menu-self-original-sound',
        name: '原声查询',
        permissionKey: 'selfServiceOriginalSoundQuery',
        path: '/selfService/originalSoundQuery',
        icon: 'search-eye-line',
        sort: 2
      },
      {
        id: 'menu-self-closed-loop',
        name: '闭环评价',
        permissionKey: 'selfServiceClosedLoopEvaluation',
        path: '/selfService/closedLoopEvaluation',
        icon: 'search-eye-line',
        sort: 3
      },
      {
        id: 'menu-self-local-data',
        name: '导入分析',
        permissionKey: 'selfServiceLocalDataAnalysis',
        path: '/selfService/localDataAnalysis',
        icon: 'search-eye-line',
        sort: 4
      }
    ]
  },
  {
    id: 'menu-system',
    name: '系统管理',
    permissionKey: 'system',
    path: '/system',
    icon: 'menu-settings-fill',
    sort: 6,
    children: [
      {
        id: 'menu-system-user',
        name: '账号管理',
        permissionKey: 'UserManagement',
        path: '/system/user',
        icon: 'menu-settings-fill',
        sort: 1
      },
      {
        id: 'menu-system-role',
        name: '角色管理',
        permissionKey: 'RoleManagement',
        path: '/system/role',
        icon: 'menu-settings-fill',
        sort: 2
      },
      {
        id: 'menu-system-download',
        name: '下载管理',
        permissionKey: 'sysDownloadManagement',
        path: '/system/downloadManagement',
        icon: 'menu-settings-fill',
        sort: 3
      },
      {
        id: 'menu-system-scene',
        name: '场景管理',
        permissionKey: 'sceneManagement',
        path: '/system/scene',
        icon: 'menu-settings-fill',
        sort: 4
      },
      {
        id: 'menu-system-report',
        name: '报告管理',
        permissionKey: 'srReportManagement',
        path: '/system/report',
        icon: 'menu-settings-fill',
        sort: 5
      },
      {
        id: 'menu-system-square',
        name: '看数广场',
        permissionKey: 'systemDataSquare',
        path: '/system/dataSquare',
        icon: 'menu-settings-fill',
        sort: 6
      },
      {
        id: 'menu-system-push',
        name: '推送管理',
        permissionKey: 'sysPushManagement',
        path: '/system/pushManagement',
        icon: 'menu-settings-fill',
        sort: 7
      },
      {
        id: 'menu-system-log',
        name: '日志查询',
        permissionKey: 'sysLogQuery',
        path: '/system/logQuery',
        icon: 'menu-settings-fill',
        sort: 8
      },
      {
        id: 'menu-system-config',
        name: '系统配置',
        permissionKey: 'configurationManagement',
        path: '/system/configuration',
        icon: 'menu-settings-fill',
        sort: 9
      },
      {
        id: 'menu-system-subscribe',
        name: '订阅管理',
        permissionKey: 'subscribeManagement',
        path: '/system/subscribe',
        icon: 'menu-settings-fill',
        sort: 10
      },
    ]
  },
  {
    id: 'menu-event',
    name: '客情直驱',
    permissionKey: 'CustomerDirectEngage',
    path: '/customerDirectEngage',
    icon: 'menu-team-fill',
    sort: 5,
    children: [
      {
        id: 'menu-event-single',
        name: '单点事件',
        permissionKey: 'CDESinglePointEvent',
        path: '/customerDirectEngage/singlePointEvent',
        icon: 'menu-team-fill',
        sort: 1
      },
      {
        id: 'menu-event-batch',
        name: '批量事件',
        permissionKey: 'CDEBatchEvent',
        path: '/customerDirectEngage/batchEvent',
        icon: 'menu-team-fill',
        sort: 2
      }
    ]
  }
]

const permissionResult = database => ({
  advanced: [],
  clientIds: { details: ['voc-voice'] },
  defaultClientId: 'voc-voice',
  isAdmin: true,
  button: ['*'],
  functionPermission: ['*'],
  username: '演示管理员',
  userId: 'demo-admin',
  name: '演示管理员',
  roleId: 'demo-super-admin',
  brands: {
    details: database.brands.map(brand => ({
      ...brand,
      key: brand.id,
      value: brand.name,
      label: brand.name,
      code: brand.id,
      img: ''
    }))
  },
  appTagsMobile: [
    { key: 'voc-product', value: '产品体验', code: 'voc-product', img: '', sort: 1 },
    { key: 'voc-service', value: '服务体验', code: 'voc-service', img: '', sort: 2 }
  ],
  timeDimension: [
    {
      code: 0,
      name: '近7天',
      startTime: new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10),
      endTime: new Date().toISOString().slice(0, 10)
    },
    {
      code: 1,
      name: '近30天',
      startTime: new Date(Date.now() - 29 * 86400000).toISOString().slice(0, 10),
      endTime: new Date().toISOString().slice(0, 10)
    }
  ],
  menus: [
    ...menuTree().sort((first, second) => first.sort - second.sort),
    {
      id: 'menu-h5-home',
      name: 'H5首页',
      permissionKey: 'H5Home',
      path: '/h5/home',
      jsonObject: [
        { filterType: '94', value: ['0'] },
        { filterType: '91', selected: ['voc-brand-zhixing'] }
      ]
    },
    {
      id: 'menu-h5-task',
      name: 'H5任务',
      permissionKey: 'H5TaskEvent',
      path: '/h5/task',
      jsonObject: [
        { filterType: '94', value: ['0'] },
        { filterType: '91', selected: ['voc-brand-zhixing'] }
      ]
    },
    {
      id: 'menu-h5-data',
      name: '看数广场',
      permissionKey: 'H5DataPlaza',
      path: '/h5/dataSquare',
      jsonObject: [{ filterType: '91', selected: ['voc-brand-zhixing'] }]
    },
    {
      id: 'menu-h5-canswer',
      name: '智能问数',
      permissionKey: 'H5Canswer',
      path: '/h5/analysisAndVoice',
      jsonObject: [{ filterType: '91', selected: ['voc-brand-zhixing'] }]
    }
  ],
  openTaskPermission: true,
  hasDataSquarePermission: true
})

const h5HomeResult = (pathname, database, body = {}) => {
  const value = pathname.toLowerCase()
  if (value.endsWith('/mobileterminal/report/getreport')) {
    const buildTopDetail = (type, offset) =>
      opinionRows(database)
        .slice(offset, offset + 3)
        .map((item, index) => ({
          opinion: item.opinion,
          mention: item.mentions,
          mentionMom: item.mentionsMoM,
          seriesRanks: database.brands.slice(0, 4).map((brand, brandIndex) => ({
            name: brand.series[0],
            mentions: item.mentions - brandIndex * 760 - index * 180
          })),
          userVoices: database.voices.slice(index, index + 3).map(voice => ({
            id: voice.id,
            title: `${type}体验原声`,
            content: voice.content,
            topics: [{ topic: voice.topic, sentiment: voice.sentiment }],
            custName: voice.customerName,
            channel: voice.channel,
            dataCreateTime: voice.mentionTime
          }))
        }))
    return {
      title: 'VOC智声客户体验周报',
      description: '聚焦客户反馈趋势、核心抱怨与体验改善机会，形成可追踪的本地演示洞察。',
      dataSource: '车主社区、社交媒体、电商评价、服务工单、调研问卷',
      dataCycle: `${trend()[0].date} 至 ${trend().at(-1).date}`,
      brandName: database.brands[0].name,
      briefReport: {
        negativeRate: 14.8,
        negativeRateMom: -2.3,
        mentionCount: 48620,
        mentionCountMom: 6.4
      },
      brandTrendComparison: trend().map(item => ({
        date: item.date,
        startTime: `${item.date} 00:00:00`,
        endTime: `${item.date} 23:59:59`,
        negativeRateName: '负面率',
        negativeRate: item.negativeRate,
        negativeRateMom: -2.3,
        negativeRateYoy: -4.1,
        mentionName: '提及量',
        mention: item.mentionCount,
        mentionMom: 6.4,
        mentionYoy: 11.2,
        remark: ['智能座舱', '售后响应']
      })),
      productTopDetail: buildTopDetail('产品', 0),
      serviceTopDetail: buildTopDetail('服务', 2)
    }
  }
  if (value.endsWith('/newly-event-statistics')) {
    return { currentCounts: 18, lastCounts: 15, closeRate: 72.2, ringRate: 20 }
  }
  if (value.endsWith('/event-status-distribution')) {
    return [
      { taskStatus: '1', taskStatusName: '待处理', currentCounts: 5, percent: 27.8 },
      { taskStatus: '2', taskStatusName: '处理中', currentCounts: 7, percent: 38.9 },
      { taskStatus: '3', taskStatusName: '待确认', currentCounts: 3, percent: 16.7 },
      { taskStatus: '4', taskStatusName: '已完成', currentCounts: 3, percent: 16.6 }
    ]
  }
  if (value.endsWith('/event-trend')) {
    return trend()
      .slice(-7)
      .map((item, index) => ({
        dateStr: item.date,
        counts: 8 + index * 2 + (index % 2 ? 3 : 0)
      }))
  }
  if (value.endsWith('/mobileterminal/homepage/getdatabrief')) {
    return {
      name: '近7天',
      negativeRate: 14.8,
      negativeRateMom: -2.3,
      mentionCount: 48620,
      mentionCountMom: 6.4,
      achieveRate: 92,
      achieveRateTalk: '体验改善目标达成情况良好'
    }
  }
  if (value.endsWith('/mobileterminal/homepage/getbrandtrendcomparison')) {
    return trend().map(item => ({
      date: item.date,
      negativeRate: item.negativeRate,
      mentionCount: item.mentionCount,
      mentions: item.mentionCount
    }))
  }
  if (value.endsWith('/mobileterminal/homepage/getfocussceneanalysistop')) {
    return {
      list: [
        {
          opinion: '系统升级后偶发卡顿',
          sentiment: '负面',
          mentions: 3280,
          mentionsMoM: 12.6,
          mentionsYoY: 8.4,
          remark: ['稳定性']
        },
        {
          opinion: '售后响应等待时间偏长',
          sentiment: '负面',
          mentions: 2460,
          mentionsMoM: 7.1,
          mentionsYoY: 4.2,
          remark: ['服务效率']
        },
        {
          opinion: '语音交互识别不稳定',
          sentiment: '负面',
          mentions: 1980,
          mentionsMoM: -3.4,
          mentionsYoY: 2.6,
          remark: ['智能座舱']
        }
      ]
    }
  }
  if (value.endsWith('/mobileterminal/rootcause-analysis/get-series-rank')) {
    return database.brands.slice(0, 4).map((brand, index) => ({
      name: brand.series[0],
      code: `${brand.id}-series`,
      negativeRate: Number((12.8 + index * 2.1).toFixed(1)),
      negativeRateMoM: Number((-2.4 + index).toFixed(1)),
      mentions: 3600 - index * 520,
      mentionsMoM: 8.2 - index
    }))
  }
  if (value.endsWith('/mobileterminal/homepage/getuserdynamicevaluation')) {
    const list = database.voices.slice(0, 10).map(item => ({
      ...item,
      newId: item.id,
      originalId: item.dataId,
      originalTextScene: item.content,
      channelName: item.channel,
      evaluateTime: item.mentionTime,
      username: item.customerName,
      brandName: item.brandName,
      topics: [item.topic],
      intent: item.sentiment === '负面' ? '抱怨' : '表扬'
    }))
    return { list, total: database.voices.length }
  }
  if (value.endsWith('/mobileterminal/homepage/getindustrybrandcomparison')) {
    return paginate(
      insightRows(database).map((item, index) => ({
        code: item.brandCode,
        brandCode: item.brandCode,
        name: item.brandName,
        brandName: item.brandName,
        imgUrl: item.imgUrl,
        brandImg: item.imgUrl,
        negativeRate: item.negativeRate,
        negativeRateMoM: item.negativeRateMoM,
        negativeRateMom: item.negativeRateMoM,
        mentions: item.mentions,
        mentionsMoM: item.mentionsMoM,
        rankChange: index % 3 === 0 ? 1 : index % 3 === 1 ? -1 : 0,
        isSelf: index === 0,
        rateColor: item.negativeRate <= 15 ? '#00a870' : '#f53f3f',
        changeRate: trend()
          .slice(-7)
          .map(point => Number((point.negativeRate + index * 0.6).toFixed(1)))
      })),
      {}
    )
  }
  if (value.endsWith('/mobileterminal/homepage/getuserdynamicevaluationinfo')) {
    const voice =
      database.voices.find(item =>
        [item.id, item.newId, item.originalId, item.dataId].includes(
          String(body.newId || body.originalId || body.id || '')
        )
      ) ||
      database.voices[0]
    return {
      ...voice,
      newId: voice.id,
      originalId: voice.dataId,
      originalTextScene: voice.content,
      browsingDuration: '2分36秒',
      opinion: [voice.topic, voice.sentiment === '负面' ? '稳定性改善' : '体验认可'],
      topics: [voice.topic],
      soundslist: database.voices.slice(0, 3).map(item => ({ id: item.id, newId: item.id })),
      relationEvents: database.events.slice(0, 2).map(event => ({
        id: event.id,
        dataId: event.voiceIds?.[0],
        warningEventNo: event.eventId,
        taskStatus: event.taskStatus,
        taskStatusName: event.taskStatusName,
        mainRespOrgName: event.department
      })),
      channelName: voice.channel,
      channelCode: `voc-channel-${(database.voices.indexOf(voice) % 5) + 1}`,
      username: voice.customerName,
      evaluateTime: voice.mentionTime,
      intent: voice.sentiment === '负面' ? '抱怨' : '表扬',
      quality: '高质量原声',
      ext: [
        { name: '客户类型', value: '活跃车主' },
        { name: '用车场景', value: '日常通勤' },
        { name: '处理状态', value: '已进入体验改善闭环' }
      ]
    }
  }
  return null
}

const reportRows = database =>
  database.reports
    .filter(report => report.reportName || report.name)
    .map((report, index) => ({
      ...report,
      reportName: report.reportName || report.name,
      name: report.name || report.reportName,
      reportId: report.id,
      reportUrl: ['/scene/groupAnalysis', '/scene/productAnalysis', '/scene/serviceAnalysis'][
        index % 3
      ],
      categoryId: database.categories[index % database.categories.length].id,
      categoryName: database.categories[index % database.categories.length].name,
      brandCode: database.brands[index % database.brands.length].id,
      brandName: database.brands[index % database.brands.length].name,
      defaultCondition: {
        formData: {
          dateRange: 'custom',
          brandList: [database.brands[index % database.brands.length].id],
          carSeriesList: [database.brands[index % database.brands.length].series[0]],
          channelIds: [`voc-channel-${(index % 5) + 1}`],
          sentimentList: ['正面', '中性', '负面'],
          intentionList: ['表扬', '咨询', '建议', '抱怨']
        },
        customTimes: [trend()[0].date, trend().at(-1).date]
      },
      isPinned: index < 3 ? 1 : 0,
      pinnedTime: report.createTime,
      publishTime: report.createTime,
      updateTime: report.createTime
    }))

const h5DataSquareResult = (pathname, body, database) => {
  const value = pathname.toLowerCase()
  const reports = reportRows(database)
  if (value.endsWith('/mobile-data-plaza/brand/list')) {
    return database.brands.map((brand, index) => ({
      categoryId: 'category-1',
      categoryName: '综合洞察',
      brandCode: brand.id,
      brandName: brand.name,
      sortNo: index + 1
    }))
  }
  if (value.endsWith('/mobile-data-plaza/home')) {
    const brand = database.brands.find(item => item.id === body.brandCode) || database.brands[0]
    return database.categories.map((category, index) => ({
      categoryId: category.id,
      categoryName: category.name,
      brandCode: brand.id,
      brandName: brand.name,
      sortNo: index + 1,
      reportCount: reports.filter(report => report.categoryId === category.id).length,
      hasMore: true,
      reports: reports
        .filter(report => report.categoryId === category.id)
        .slice(0, Number(body.reportLimit || 3))
    }))
  }
  if (value.endsWith('/mobile-data-plaza/report/search'))
    return paginate(
      reports.filter(report => !body.keyword || report.reportName.includes(body.keyword)),
      body
    )
  if (value.endsWith('/mobile-data-plaza/category/report/list')) {
    const category =
      database.categories.find(item => item.id === body.categoryId) || database.categories[0]
    const categoryReports = reports.filter(report => report.categoryId === category.id)
    return {
      category: {
        ...category,
        categoryId: category.id,
        categoryName: category.name,
        brandCode: database.brands[0].id,
        brandName: database.brands[0].name,
        reportCount: categoryReports.length,
        sortNo: category.sortNo
      },
      reports: paginate(categoryReports, body)
    }
  }
  if (value.endsWith('/data-plaza/report/detail')) {
    const report =
      reports.find(item => item.reportId === String(body.id || body.reportId)) || reports[0]
    return {
      ...report,
      dateCondition: {
        selectedShortcut: '近30天',
        startDate: trend()[0].date,
        endDate: trend().at(-1).date
      },
      defaultCondition: {
        dateRange: '近30天',
        brandList: [report.brandCode],
        carSeriesList: [],
        channelIds: [],
        sentimentList: [],
        intentionList: [],
        tagType: 'voc-product',
        experienceCode: [],
        topicCodes: [],
        usageScenarioCodes: [],
        scenarioAttr: [],
        contentTypes: [],
        advertisementType: [],
        accountTypes: []
      }
    }
  }
  if (value.endsWith('/mobile-data-plaza/report/getdrilldownbrief'))
    return {
      ...briefResult(),
      negativeMentions: 7196,
      negativeMentionsMoM: -2.3,
      positiveMentions: 29756,
      positiveMentionsMoM: 5.8
    }
  if (value.endsWith('/mobile-data-plaza/report/get-series-rank'))
    return insightRows(database).map(item => ({
      name: item.carSeriesName,
      code: item.carSeriesCode,
      brandName: item.brandName,
      mentions: item.mentions,
      mentionsMoM: item.mentionsMoM,
      negativeRate: item.negativeRate,
      negativeRateMoM: item.negativeRateMoM
    }))
  if (value.endsWith('/mobile-data-plaza/report/get-tag-analysis'))
    return insightRows(database).map(item => ({
      tagName: item.tagName,
      tagCode: item.tagCode,
      mentions: item.mentions,
      mentionsMoM: item.mentionsMoM,
      negativeRate: item.negativeRate,
      negativeRateMoM: item.negativeRateMoM,
      positiveRate: item.positiveRate
    }))
  return null
}

const overviewResult = (pathname, database) => {
  const value = pathname.toLowerCase()
  if (value.endsWith('/homepage/getbrandbriefreport')) {
    return database.brands.map((brand, index) => ({
      name: index === 0 ? '智行汽车集团' : brand.name,
      brandCode: brand.id,
      imgUrl: brand.imageUrl,
      negativeRate: Number((12.4 + index * 1.7).toFixed(1)),
      growth: Number((4.8 - index * 0.7).toFixed(1)),
      mentionCount: 12800 - index * 1350,
      growthTrend: trend()
        .slice(-7)
        .map(item => item.mentionCount - index * 80)
    }))
  }
  if (value.endsWith('/homepage/getspecializedanalysis')) {
    const reportRoutes = [
      '/scene/groupAnalysis',
      '/scene/thisProductAnalysis',
      '/scene/serviceAnalysis'
    ]
    return database.reports
      .filter(report => report.reportName || report.name)
      .slice(0, 6)
      .map((report, index) => ({
        ...report,
        reportName: report.reportName || report.name,
        reportUrl: reportRoutes[index % reportRoutes.length]
      }))
  }
  if (value.endsWith('/homepage/getgeneralscenario')) {
    return [
      {
        name: '集团分析',
        htmlUri: '/scene/groupAnalysis',
        smallImage: '/demo-assets/scenes/scene-group-v2.png',
        bigImage: '/demo-assets/scenes/scene-group-v2.png',
        description: '集团整体体验趋势与品牌表现'
      },
      {
        name: '本品分析',
        htmlUri: '/scene/thisProductAnalysis',
        smallImage: '/demo-assets/scenes/scene-own-product.png',
        bigImage: '/demo-assets/scenes/scene-own-product.png',
        description: '本品车系体验与用户观点洞察'
      },
      {
        name: '竞品对比',
        htmlUri: '/scene/competitorAnalysis',
        smallImage: '/demo-assets/scenes/scene-competitor.png',
        bigImage: '/demo-assets/scenes/scene-competitor.png',
        description: '本品与竞品的体验差异对比'
      },
      {
        name: '旅程分析',
        htmlUri: '/scene/journeyAnalysis',
        smallImage: '/demo-assets/scenes/scene-journey.png',
        bigImage: '/demo-assets/scenes/scene-journey.png',
        description: '客户全旅程触点与关键问题分析'
      },
      {
        name: '产品分析',
        htmlUri: '/scene/productAnalysis',
        smallImage: '/demo-assets/scenes/scene-product.png',
        bigImage: '/demo-assets/scenes/scene-product.png',
        description: '产品功能、质量与体验结构分析'
      },
      {
        name: '服务分析',
        htmlUri: '/scene/serviceAnalysis',
        smallImage: '/demo-assets/scenes/scene-service.png',
        bigImage: '/demo-assets/scenes/scene-service.png',
        description: '售后服务流程与客户感知分析'
      },
      {
        name: '新车上市',
        htmlUri: '/scene/newCarLaunch',
        smallImage: '/demo-assets/scenes/scene-new-car.png',
        bigImage: '/demo-assets/scenes/scene-new-car.png',
        description: '新车上市声量、口碑与风险跟踪'
      },
      {
        name: '重点账号',
        htmlUri: '/scene/mainAccount',
        smallImage: '/demo-assets/scenes/scene-key-account.png',
        bigImage: '/demo-assets/scenes/scene-key-account.png',
        description: '重点客户账号与传播影响分析'
      },
      {
        name: '热点事件',
        htmlUri: '/scene/hotEvents',
        smallImage: '/demo-assets/scenes/scene-hot-event.png',
        bigImage: '/demo-assets/scenes/scene-hot-event.png',
        description: '高热风险事件与舆情演化追踪'
      }
    ]
  }
  if (value.endsWith('/homepage/gethoteventstop')) {
    return [
      { name: '智能座舱升级稳定性', mentionCount: 3200, growth: 13.2, label: '飙升' },
      { name: '售后响应时效', mentionCount: 2680, growth: 8.4, label: '关注' },
      { name: '新车空间体验', mentionCount: 2210, growth: 6.7, label: '正向' }
    ]
  }
  if (value.endsWith('/homepage/getcustomerteasing')) {
    return database.voices
      .filter(item => item.sentiment === '负面')
      .slice(0, 6)
      .map(item => ({
        ...item,
        title: item.opinionName,
        mentionContent: item.content,
        mentionCount: item.mentionCount,
        mentionTime: item.mentionTime,
        customerName: item.customerName
      }))
  }
  if (value.endsWith('/homepage/getcustomeremotion')) return database.events.slice(0, 6)
  return null
}

const dashboardResult = (pathname, database, body = {}) => {
  const value = pathname.toLowerCase()
  const eventType = String(body.eventType || '').toUpperCase()
  const scopedEvents = database.events.filter(event => {
    if (eventType === 'BATCH') return event.type === '批量事件'
    if (eventType === 'SINGLE') return event.type === '单点事件'
    return true
  })
  if (value.endsWith('/dashboard/stat-cards')) {
    return ['待处理', '处理中', '待确认', '已完成'].map((statusName, index) => ({
      status: String(index + 1),
      statusName,
      count: scopedEvents.filter(item => item.status === statusName).length,
      changeRate: `${index % 2 === 0 ? '+' : '-'}${3 + index * 2}%`
    }))
  }
  if (value.endsWith('/dashboard/event-list')) {
    return scopedEvents.slice(0, 6).map(event => ({
      id: event.id,
      dataId: event.voiceIds?.[0],
      eventName: event.name,
      subjectCategoryName: event.type,
      eventPriorityName: event.priority,
      primaryDepName: event.department,
      taskStatus: event.status,
      taskStatusName: event.status,
      warningTime: event.createTime,
      brandName: '智行',
      carSeriesName: '智行 S7'
    }))
  }
  return null
}

const selectCollection = (pathname, database) => {
  const value = pathname.toLowerCase()
  if (value.includes('sound') || value.includes('voice') || value.includes('highquality'))
    return database.voices
  if (value.includes('event') || value.includes('task')) return database.events
  if (value.includes('report') || value.includes('subscribe') || value.includes('push'))
    return database.reports
  if (value.includes('role')) return database.roles
  if (value.includes('user') || value.includes('account') || value.includes('depart'))
    return database.users
  if (value.includes('category') || value.includes('scene') || value.includes('dict'))
    return database.categories
  return database.voices
}

const mutateCollection = (pathname, body, database) => {
  const collection = selectCollection(pathname, database)
  const value = pathname.toLowerCase()
  const id = String(body.id || body.eventId || body.reportId || '')
  if (value.includes('delete') || value.includes('/del')) {
    const index = collection.findIndex(item => String(item.id) === id)
    if (index >= 0) collection.splice(index, 1)
    return { id, deleted: true }
  }
  const target = collection.find(item => String(item.id) === id)
  if (target) {
    Object.assign(target, body, { updateTime: new Date().toISOString() })
    return target
  }
  const created = {
    ...body,
    id: id || `local-${Date.now()}`,
    createTime: new Date().toISOString(),
    status: body.status || '处理中'
  }
  collection.unshift(created)
  return created
}

const localDataSourceRows = database =>
  reportRows(database).slice(0, 8).map((report, index) => ({
    id: `voc-data-source-${index + 1}`,
    dataName: `${report.reportName}-${String(index + 1).padStart(2, '0')}`,
    taskInfo: `已导入 ${1280 + index * 240} 条客户声音，覆盖${['智能座舱', '售后服务', '驾乘空间'][index % 3]}场景`,
    batchId: `voc-batch-${index + 1}`,
    statusCode: String(index % 4),
    status: ['未处理', '处理中', '处理完成', '处理失败'][index % 4],
    createUser: database.users[index % database.users.length].name,
    create_userId: database.users[index % database.users.length].id,
    createTime: report.createTime
  }))

const singleEventRows = database =>
  database.events
    .filter(event => event.type === '单点事件')
    .map((event, index) => {
      const voice = database.voices[index % database.voices.length]
      return {
        ...event,
        warningEventNo: event.eventId,
        subjectCategoryName: event.subjectCategoryName,
        channelName: voice.channel,
        eventPriority: event.priority.toLowerCase(),
        eventPriorityName: event.priority,
        eventName: event.name,
        title: voice.title,
        content: voice.content,
        originalTextScene: voice.content,
        topicText: voice.opinionName,
        brandName: voice.brandName,
        carSeriesName: voice.carSeriesName,
        authorName: voice.customerName,
        mainRespOrgName: event.department,
        isProcessed: event.status === '已完成' ? '1' : '0',
        isProcessedName: event.status === '已完成' ? '已处理' : '处理中',
        handler: { userId: 'demo-owner', userName: event.owner },
        reviewProgressName: ['待回评', '回评中', '已完成'][index % 3],
        reviewHandlerName: database.users[(index + 1) % database.users.length].name,
        privateMsgProgressName: ['未联系', '沟通中', '已回复'][index % 3],
        privateMsgHandlerName: database.users[(index + 2) % database.users.length].name,
        privateMsgCount: index + 1,
        taskStatus: event.status,
        taskStatusName: event.status,
        updateTime: `${event.createTime.slice(0, 10)} 16:30:00`,
        warningTime: event.createTime,
        eventAttribute: index % 2 === 0 ? '客户体验风险' : '服务改进机会',
        eventValidity: 'valid',
        eventValidityName: '有效事件',
        dataId: voice.id
      }
    })

const batchEventRows = database =>
  database.events
    .filter(event => event.type === '批量事件')
    .map((event, index) => {
      const voice = database.voices[index % database.voices.length]
      return {
        ...event,
        warningEventNo: event.eventId,
        warningEventName: event.name,
        subjectCategoryName: event.subjectCategoryName,
        eventPriority: event.priority.toLowerCase(),
        channelNames: ['车主社区', '服务工单'].join('、'),
        brandName: voice.brandName,
        carSeriesName: voice.carSeriesName,
        topicText: voice.opinionName,
        intention: voice.intention,
        mentionCount: voice.mentionCount,
        mentionCountRate: Number((8.2 - index * 0.6).toFixed(1)),
        createUserName: '演示管理员',
        mainRespUserName: event.owner,
        mainRespUserEmpNo: 'VOC0003',
        primaryDepName: event.department,
        coordinateSecondDeptName:
          index % 2 === 0 ? '售后服务中心' : '品牌运营中心',
        coordinateThirdDeptName:
          index % 2 === 0 ? '客户体验中心' : '产品质量中心',
        processedUserName: database.users[(index + 1) % database.users.length].name,
        updateTime: `${event.createTime.slice(0, 10)} 16:30:00`,
        warningPeriod: ['实时预警', '每日汇总', '每周复盘'][index % 3],
        eventAttributes: index % 2 === 0 ? '客户体验风险' : '服务改进机会',
        eventValidity: '有效事件',
        isRejected: '否',
        rejectReason: '无',
        taskStatus: event.status,
        taskStatusName: event.status,
        warningTime: event.createTime
      }
    })

const hotEventRows = database =>
  database.events.slice(0, 10).map((event, index) => {
    const brand = database.brands[index % database.brands.length]
    const voice = database.voices[index % database.voices.length]
    return {
      ...event,
      eventName: index % 2 === 0 ? '智能座舱升级稳定性热点追踪' : '售后服务响应时效热点追踪',
      keywords: JSON.stringify([voice.topic, voice.intention, brand.name]),
      brandList: JSON.stringify([brand.name]),
      seriesList: JSON.stringify([brand.series[index % brand.series.length]]),
      status: (index % 3) + 1,
      createBy: database.users[index % database.users.length].name,
      createTime: event.createTime
    }
  })

const specialAnalysisTypeRows = (database, body) => {
  const keyword = String(body.name || body.keyword || '').trim()
  if (Number(body.type) === 2) {
    const parentIndex = Math.max(
      database.categories.findIndex(category => category.id === body.pid),
      0
    )
    const zoneNames = [
      ['月度客户体验专区', '集团经营洞察专区', '重点问题复盘专区'],
      ['智能座舱体验专区', '驾乘空间体验专区', '新能源续航专区'],
      ['售后服务体验专区', '交付流程体验专区', '客户关怀专区']
    ][parentIndex]
    return zoneNames
      .map((name, index) => ({
        id: `${database.categories[parentIndex].id}-zone-${index + 1}`,
        pid: database.categories[parentIndex].id,
        type: 2,
        name,
        reportCnt: 4 + index * 2,
        roleCnt: 3 + index,
        enabled: index === 2 ? 0 : 1,
        sortNo: index + 1
      }))
      .filter(item => !keyword || item.name.includes(keyword))
  }

  return database.categories
    .map((category, index) => ({
      ...category,
      type: 1,
      enabled: 1,
      reportCnt: 8 + index * 3,
      roleCnt: 4 + index
    }))
    .filter(item => !keyword || item.name.includes(keyword))
}

const reportManagementRows = database =>
  reportRows(database).map((report, index) => {
    const category = database.categories[index % database.categories.length]
    return {
      ...report,
      class1Name: category.name,
      class2Name: `${category.name}专区`,
      createBy: database.users[index % database.users.length].name,
      auditBy: database.users[(index + 1) % database.users.length].name,
      auditTime: report.createTime,
      status: index % 4,
      pinToTop: index < 2 ? 1 : 0
    }
  })

const dataPlazaReportRows = database =>
  reportRows(database).map((report, index) => ({
    ...report,
    categoryId: database.categories[index % database.categories.length].id,
    categoryName: database.categories[index % database.categories.length].name,
    dateCondition: {
      selectedShortcut: ['近7天', '近30天', '本季度'][index % 3]
    },
    defaultCondition: {
      formData: {
        dateRange: 'custom',
        brandList: [database.brands[index % database.brands.length].id],
        carSeriesList: [database.brands[index % database.brands.length].series[0]],
        channelIds: [`voc-channel-${(index % 5) + 1}`],
        sentimentList: ['正面', '中性', '负面'],
        intentionList: ['表扬', '咨询', '建议', '抱怨'],
        tagType: 'experience',
        experienceCode: [[`voc-experience-${(index % 8) + 1}`]],
        topicCodes: [`voc-topic-${(index % 6) + 1}`]
      },
      customTimes: [trend()[0].date, trend().at(-1).date]
    },
    publishStatus: index % 3 === 0 ? 0 : 1,
    createBy: database.users[index % database.users.length].name,
    updateBy: database.users[(index + 1) % database.users.length].name,
    updateTime: report.createTime,
    createTime: report.createTime,
    pinnedTime: index < 3 ? report.createTime : '',
    isPinned: index < 3 ? 1 : 0
  }))

const systemPageResult = (pathname, body, database) => {
  const value = pathname.toLowerCase()
  if (value.endsWith('/role/list')) {
    const roles = [
      { id: 'demo-super-admin', name: '超级管理员', userCount: 1, status: 1 },
      { id: 'demo-insight-analyst', name: '体验分析师', userCount: 3, status: 1 },
      { id: 'demo-event-owner', name: '事件负责人', userCount: 4, status: 1 },
      { id: 'demo-service-specialist', name: '服务专员', userCount: 6, status: 1 }
    ]
    return paginate(
      roles.map((role, index) => ({
        ...role,
        roleId: role.id,
        roleName: role.name,
        roleType: index === 0 ? '1' : '2',
        roleTypeName: index === 0 ? '系统角色' : '业务角色',
        remark: index === 0 ? '拥有全部菜单与业务操作权限' : '用于客户体验业务协同',
        userCount: role.userCount || index + 1,
        roleStatusName: role.status === 0 ? '已停用' : '启用中'
      })),
      body
    )
  }
  if (value.endsWith('/reportdownload/findreportdownloadfilelist')) {
    const rows = reportRows(database).slice(0, 8).map((report, index) => ({
      id: `voc-download-${index + 1}`,
      fileName: `${report.reportName}-${report.createTime.slice(0, 10)}.pdf`,
      filePath: `/api/local/files/voc-download-${index + 1}`,
      downloadTime: report.createTime,
      operator: database.users[index % database.users.length].name,
      status: index % 4 === 0 ? '' : index % 4 === 1 ? '0' : '1'
    }))
    return paginate(rows, body)
  }
  if (value.endsWith('/pushmessage/pagemessagedata')) {
    const rows = reportRows(database).slice(0, 8).map((report, index) => {
      const pushTotal = 120 + index * 35
      const successTotal = pushTotal - 2 - index
      return {
        id: `voc-push-${index + 1}`,
        batchId: `voc-push-batch-${index + 1}`,
        pushTotal,
        successTotal,
        successRate: Number(((successTotal / pushTotal) * 100).toFixed(1)),
        pushType: '报告推送',
        pushTime: report.createTime,
        status: ['未开始', '处理中', '已完成'][index % 3],
        statusCode: String(index % 3),
        createUser: database.users[index % database.users.length].name,
        createUserId: database.users[index % database.users.length].id,
        createTime: report.createTime
      }
    })
    return paginate(rows, body)
  }
  if (value.endsWith('/operationlog/findvocloglist')) {
    const menus = ['VOC总览', '领导总览', '集团分析', '根因分析', '单点事件', '报告管理']
    const rows = Array.from({ length: 18 }, (_, index) => {
      const user = database.users[index % database.users.length]
      return {
        id: `voc-log-${index + 1}`,
        employeeName: user.name,
        employeeNo: user.employeeId,
        secondLevelDept: '智行汽车集团',
        thirdLevelDept: user.department,
        accessApp: index % 3 === 0 ? 'H5' : 'PC',
        accessMenu: menus[index % menus.length],
        startTime: `${new Date(Date.now() - index * 3600000).toISOString().slice(0, 10)} ${String(9 + (index % 8)).padStart(2, '0')}:20:00`,
        ipAddress: `192.168.10.${20 + index}`
      }
    })
    return paginate(rows, body)
  }
  if (value.endsWith('/display-rule/list')) {
    return [
      ['1', 0, 10, '#f53f3f', '#fff1f0'],
      ['2', 10, 20, '#ff7d00', '#fff7e8'],
      ['3', 20, 40, '#86909c', '#f2f3f5'],
      ['4', 40, 70, '#00b42a', '#e8ffea'],
      ['5', 70, 100, '#1677ff', '#e8f3ff']
    ].map(([emojiKey, rangeMin, rangeMax, colorHex, backgroundColorHex], index) => ({
      id: `voc-display-rule-${index + 1}`,
      metricCode: 'negativeRate',
      metricName: '负面率',
      rangeMin,
      rangeMax,
      colorHex,
      backgroundColorHex,
      emojiKey,
      sortNo: index + 1,
      status: 1
    }))
  }
  if (value.endsWith('/subscribe-task/list') || value.endsWith('/subscribe-task/push-record-list')) {
    const isRecord = value.endsWith('/push-record-list')
    const rows = reportRows(database).slice(0, 8).map((report, index) => ({
      id: `voc-subscribe-${index + 1}`,
      reportName: report.reportName,
      sourceModule: report.categoryName,
      subscriptionPeriod: ['每周', '每月', '每季度'][index % 3],
      sendRuleDesc: ['周一 09:00', '每月1日 10:00', '季度首日 14:00'][index % 3],
      receiverNameOnly: database.users[(index + 1) % database.users.length].name,
      receiverDesc: `${database.users[(index + 1) % database.users.length].department} · ${database.users[(index + 1) % database.users.length].name}`,
      receiverName: database.users[(index + 1) % database.users.length].name,
      receiverId: database.users[(index + 1) % database.users.length].employeeId,
      deptLevel2: '智行汽车集团',
      deptLevel3: database.users[(index + 1) % database.users.length].department,
      receiveChannelDesc: isRecord ? JSON.stringify(['1', '2']) : '站内通知、邮件',
      pushCount: 4 + index,
      creatorName: database.users[index % database.users.length].name,
      creatorWithDept: `${database.users[index % database.users.length].department} · ${database.users[index % database.users.length].name}`,
      createTime: report.createTime,
      pushTime: isRecord ? report.createTime : '',
      pushResult: isRecord ? (index % 4 === 0 ? 0 : 1) : undefined,
      status: index % 3
    }))
    return paginate(rows, body)
  }
  if (value.includes('/subscribe-task/get/')) {
    const index = Math.max(
      Number.parseInt(value.split('/').at(-1)?.replace(/\D/g, '') || '1', 10) - 1,
      0
    )
    const report = reportRows(database)[index % database.reports.length]
    const receiver = database.users[(index + 1) % database.users.length]
    return {
      id: `voc-subscribe-${index + 1}`,
      taskName: report.reportName,
      reportName: report.reportName,
      sendReportId: report.id,
      startDate: trend()[0].date,
      endDate: trend().at(-1).date,
      sendRule: (index % 3) + 1,
      sendDay: String((index % 5) + 1),
      sendDatetime: ['09:00', '10:00', '14:00'][index % 3],
      receiverIds: JSON.stringify([receiver.employeeId]),
      receiverNames: JSON.stringify([receiver.name]),
      receiveChannel: JSON.stringify(['1', '2']),
      receiveChannelDesc: '站内通知、邮件',
      status: index % 3,
      creatorId: database.users[index % database.users.length].id,
      creatorName: database.users[index % database.users.length].name,
      createTime: report.createTime
    }
  }
  return null
}

const buildResult = (pathname, method, body, database) => {
  const value = pathname.toLowerCase()
  if (value.endsWith('/vocleadership/getvoclistsounds')) {
    return paginate(voiceListRows(database, body), body)
  }
  if (value.endsWith('/vocleadership/getsoundsdetails')) {
    return voiceDetailResult(database, body)
  }
  const overview = overviewResult(pathname, database)
  if (overview) return overview
  const dashboard = dashboardResult(pathname, database, body)
  if (dashboard) return dashboard
  const h5Home = h5HomeResult(pathname, database, body)
  if (h5Home) return h5Home
  const h5DataSquare = h5DataSquareResult(pathname, body, database)
  if (h5DataSquare) return h5DataSquare
  const sceneAnalysis = sceneAnalysisResultForPath(pathname, database, body)
  if (sceneAnalysis) return sceneAnalysis
  const systemPage = systemPageResult(pathname, body, database)
  if (systemPage) return systemPage
  if (value.endsWith('/datasource/finddatasource')) {
    return paginate(localDataSourceRows(database), body)
  }
  if (value.endsWith('/single-event/list')) {
    return paginate(singleEventRows(database), body)
  }
  if (value.endsWith('/batch-event/batcheventlist')) {
    return paginate(batchEventRows(database), body)
  }
  if (value.endsWith('/hot-event/list')) {
    return paginate(hotEventRows(database), body)
  }
  if (value.endsWith('/special-analysis-type/list')) {
    return paginate(specialAnalysisTypeRows(database, body), body)
  }
  if (value.endsWith('/custom-report/reportlist')) {
    return paginate(reportManagementRows(database), body)
  }
  if (value.endsWith('/data-plaza/report/list')) {
    return paginate(dataPlazaReportRows(database), body)
  }
  const array = arrayResult(pathname, database, body)
  if (array) return array
  if (/\/(group-analysis|product-self-analysis)\/getproductbrief$/i.test(pathname)) {
    return briefResult()
  }
  if (/\/(single-event|batch-event)\/conditions$/i.test(pathname)) {
    return conditionResult(database)
  }
  if (value.endsWith('/accountinfo/findaccountinfolist')) return paginate(database.users, body)
  if (value.endsWith('/insdict/dict-list')) {
    const dictionaries = [
      {
        id: 'voc-dict-channel',
        dictName: '数据渠道',
        dictCode: 'voc_channel',
        type: 0,
        description: '客户声音来源渠道',
        operator: '演示管理员',
        itemCount: 5
      },
      {
        id: 'voc-dict-sentiment',
        dictName: '情感倾向',
        dictCode: 'voc_sentiment',
        type: 0,
        description: '正面、中性与负面情感分类',
        operator: '体验分析师',
        itemCount: 3
      },
      {
        id: 'voc-dict-event-status',
        dictName: '事件状态',
        dictCode: 'voc_event_status',
        type: 0,
        description: '客情事件处理进度',
        operator: '事件负责人',
        itemCount: 4
      },
      {
        id: 'voc-dict-priority',
        dictName: '事件优先级',
        dictCode: 'voc_event_priority',
        type: 0,
        description: 'P0 至 P3 事件优先级',
        operator: '演示管理员',
        itemCount: 4
      },
      {
        id: 'voc-dict-user-type',
        dictName: '客户类型',
        dictCode: 'voc_user_type',
        type: 0,
        description: '客户活跃与忠诚度分类',
        operator: '服务专员',
        itemCount: 4
      }
    ].map((item, index) => ({
      ...item,
      createTime: `${new Date().toISOString().slice(0, 10)} ${String(9 + index).padStart(2, '0')}:00:00`
    }))
    return paginate(dictionaries, body)
  }
  if (value.includes('/insdict/dict-detail/')) {
    return {
      id: pathname.split('/').pop(),
      dictName: '数据渠道',
      dictCode: 'voc_channel',
      type: 0,
      description: '客户声音来源渠道',
      operator: '演示管理员',
      itemCount: 5,
      createTime: `${new Date().toISOString().slice(0, 10)} 09:00:00`
    }
  }
  if (
    value.endsWith('/insdictitem/dict-item-list') ||
    value.includes('/insdictitem/dict-items-by-dict/')
  ) {
    const items = ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'].map(
      (itemText, index) => ({
        id: `voc-dict-item-${index + 1}`,
        dictId: body.dictId || pathname.split('/').pop(),
        itemText,
        itemTextEn: `VOC Channel ${index + 1}`,
        itemKey: `voc-channel-${index + 1}`,
        itemValue: itemText,
        description: `${itemText}演示数据`,
        sortOrder: index + 1,
        status: 1,
        operator: '演示管理员',
        createTime: `${new Date().toISOString().slice(0, 10)} 09:${String(index * 5).padStart(2, '0')}:00`
      })
    )
    return value.endsWith('/insdictitem/dict-item-list') ? paginate(items, body) : items
  }
  if (value.endsWith('/custom-report/special-type-list')) {
    return database.categories.map((category, index) => ({
      ...category,
      icon: 'document',
      reportCnt: database.reports.filter(
        (_, reportIndex) => reportIndex % database.categories.length === index
      ).length
    }))
  }
  if (value.endsWith('/custom-report/list')) {
    return paginate(reportRows(database), body)
  }
  if (value.endsWith('/custom-report/special-zone-options')) {
    return database.categories.map((category, index) => ({
      ...category,
      name: category.name,
      pid: '0',
      type: 1,
      icon: 'document',
      children: [
        {
          id: `${category.id}-detail`,
          name: `${category.name}专区`,
          pid: category.id,
          type: 2,
          icon: 'document',
          sortNo: index + 1,
          children: []
        }
      ]
    }))
  }
  if (value.endsWith('/submit-attach/files')) {
    return {
      currentPeriod: `当前周期为 ${new Date().getFullYear()} 年第 ${Math.ceil((Date.now() - new Date(new Date().getFullYear(), 0, 1).getTime()) / 604800000)} 周呈报件附件`,
      fileList: reportRows(database)
        .slice(0, 3)
        .map(report => ({
          fileName: `${report.reportName}.pdf`,
          uploadTime: report.createTime,
          size: 102400
        }))
    }
  }
  if (value.includes('/subscribe-task/getpdffileurl/')) return createDemoPdfDataUrl()
  if (value.endsWith('/single-event/topics')) {
    return insightRows(database).map(item => ({
      tagName: item.tagName,
      tagCode: item.tagCode,
      label: item.tagName,
      value: item.tagCode
    }))
  }
  if (
    value.endsWith('/datasource/finddatasource') ||
    value.endsWith('/localdataanalysis/findtasklist')
  ) {
    return paginate(database.reports, body)
  }
  if (value.endsWith('/user-browse-record/browse-records')) {
    return paginate(
      database.voices.map((voice, index) => ({
        id: `browse-${index + 1}`,
        dateTime: voice.mentionTime,
        browseDuration: `${38 + index * 7}秒`,
        soundId: voice.id,
        originalId: voice.dataId,
        originalTexTScene: voice.content,
        topics: [voice.topic]
      })),
      body
    )
  }
  if (value.endsWith('/user-browse-record/task-completion')) {
    return {
      completionRate: 86,
      totalBrowsingDuration: '3小时26分钟',
      remainingCount: 12,
      remainingTime: '2天',
      browseData: {
        totalCount: 186,
        complainCount: 48,
        consultCount: 42,
        suggestionCount: 36,
        praiseCount: 60
      }
    }
  }
  if (value.includes('listenbroadcast')) {
    return '近7天客户体验指数提升3.6%，智能座舱与售后响应成为本期重点关注场景。'
  }
  if (value.includes('/auth/randomimage/')) {
    const svg =
      '<svg xmlns="http://www.w3.org/2000/svg" width="144" height="40"><rect width="144" height="40" fill="#eef3ff"/><text x="28" y="28" font-size="24" fill="#165dff">8888</text></svg>'
    return `data:image/svg+xml;base64,${Buffer.from(svg).toString('base64')}`
  }
  if (value.endsWith('/report/base/login')) {
    return {
      access_token: 'voc-voice-local-demo-token',
      username: '演示管理员',
      userid: 'demo-admin'
    }
  }
  if (value.endsWith('/canswer/getauthdataurl')) {
    return 'http://127.0.0.1:5173/#/h5/analysisAndVoice?brandCode=voc-brand-zhixing'
  }
  if (value.endsWith('/report/userpermissions')) return permissionResult(database)
  if (value.endsWith('/report/userinfo')) return database.users[0]
  if (value.includes('sysalldictitems')) {
    return {
      sysAllDictItems: { channel: ['车主社区', '社交媒体', '电商评价', '服务工单', '调研问卷'] }
    }
  }
  if (value.includes('detail') || value.includes('brief')) {
    const collection = selectCollection(pathname, database)
    return { ...analyticsResult(database), ...(collection[0] || {}), detail: collection[0] || {} }
  }
  if (
    method !== 'GET' &&
    /(insert|create|save|update|edit|approve|confirm|close|reject|assign|reassign|publish|top|copy|delete|complete)/i.test(
      pathname
    )
  ) {
    return mutateCollection(pathname, body, database)
  }
  if (/(list|page|query|find|top|rank|records|conditions)/i.test(pathname)) {
    const collection = selectCollection(pathname, database)
    return { ...analyticsResult(database), ...paginate(collection, body) }
  }
  return analyticsResult(database)
}

const isStreamRequest = (pathname, request) => {
  const accept = String(request.headers.accept || '')
  if (accept.includes('text/event-stream')) return true
  if (/\/review\/qa\/ask$/i.test(pathname)) return true
  return /\/report\/(group-analysis|product-self-analysis|journey-analysis|product-analysis|service-analysis|competitor-compare|vocleadership|keyaccount|hot-event|new-car-launch)\/(?:.*result|.*out|data-source-report)$/i.test(
    pathname
  )
}

const streamResponse = (response, body, pathname) => {
  response.writeHead(200, {
    'Content-Type': 'text/event-stream; charset=utf-8',
    'Cache-Control': 'no-cache',
    Connection: 'keep-alive',
    'Access-Control-Allow-Origin': '*'
  })
  const isCompetitorComparison = pathname.includes('/competitor-compare/')
  const comparedNames = [body.firstSelectedName, body.secondSelectedName].filter(Boolean)
  const subject = body.brandName || body.carSeriesName || '智行汽车集团'
  const chunks =
    isCompetitorComparison && comparedNames.length === 2
      ? [
          `当前筛选条件下，${comparedNames[0]}与${comparedNames[1]}的客户体验对比已完成。`,
          `${comparedNames[0]}在智能交互和驾乘空间方面提及优势更明显，${comparedNames[1]}在服务响应与交付体验方面表现更均衡。`,
          `建议围绕两者差距最大的系统稳定性、服务闭环和续航解释场景建立专项改善清单。`
        ]
      : [
          `基于当前筛选条件，${subject}客户体验指数保持稳定。`,
          '正向反馈集中在空间、智能交互和服务响应，主要风险来自系统稳定性与问题闭环时效。',
          '建议优先推进高频负面场景专项治理，并将处理结果同步至事件任务和月度报告。'
        ]
  let index = 0
  const usesOpenAiShape = pathname === '/api/review/qa/ask'
  const timer = setInterval(() => {
    if (index >= chunks.length) {
      clearInterval(timer)
      response.write(`data: ${usesOpenAiShape ? '[DONE]' : '[END]'}\n\n`)
      response.end()
      return
    }
    const payload = usesOpenAiShape
      ? JSON.stringify({ choices: [{ delta: { content: chunks[index] } }] })
      : chunks[index]
    response.write(`data: ${payload}\n\n`)
    index += 1
  }, 120)
}

const isDownloadRequest = pathname =>
  /(download|export)/i.test(pathname) && !/list|find/i.test(pathname)

const sendDownload = (response, database) => {
  const header = '编号,主题,状态,负责人,创建时间\n'
  const rows = database.events
    .map(item => [item.eventId, item.name, item.status, item.owner, item.createTime].join(','))
    .join('\n')
  const content = Buffer.from(`\ufeff${header}${rows}`, 'utf8')
  response.writeHead(200, {
    'Content-Type': 'text/csv; charset=utf-8',
    'Content-Disposition': 'attachment; filename="VOCVoice-Demo.csv"',
    'Content-Length': content.length
  })
  response.end(content)
}

const serveStatic = (response, pathname, staticDir) => {
  const requested = pathname === '/' ? 'index.html' : pathname.replace(/^\//, '')
  const normalized = path.normalize(requested).replace(/^\.\.(\/|\\|$)/, '')
  let filePath = path.join(staticDir, normalized)
  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory())
    filePath = path.join(staticDir, 'index.html')
  if (!fs.existsSync(filePath)) {
    response.writeHead(404)
    response.end('VOC智声前端尚未构建')
    return
  }
  const extension = path.extname(filePath)
  const contentTypes = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.svg': 'image/svg+xml',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.woff2': 'font/woff2'
  }
  response.writeHead(200, { 'Content-Type': contentTypes[extension] || 'application/octet-stream' })
  fs.createReadStream(filePath).pipe(response)
}

export const startLocalDemoServer = async ({
  port = 4174,
  publicPort,
  host = '0.0.0.0',
  dataDir,
  staticDir
} = {}) => {
  const resolvedDataDir = dataDir || path.join(moduleDir, '.data')
  const resolvedStaticDir = staticDir || path.resolve(moduleDir, '../dist')
  const databasePath = path.join(resolvedDataDir, 'voc-voice-demo.json')
  fs.mkdirSync(resolvedDataDir, { recursive: true })
  let database = fs.existsSync(databasePath)
    ? JSON.parse(fs.readFileSync(databasePath, 'utf8'))
    : createSeedData()
  const persist = () => fs.writeFileSync(databasePath, JSON.stringify(database, null, 2))
  if (!fs.existsSync(databasePath)) persist()

  const server = http.createServer(async (request, response) => {
    if (request.method === 'OPTIONS') {
      response.writeHead(204, {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': '*',
        'Access-Control-Allow-Methods': 'GET,POST,PUT,PATCH,DELETE,OPTIONS'
      })
      response.end()
      return
    }

    const url = new URL(request.url || '/', `http://${request.headers.host || '127.0.0.1'}`)
    const pathname = url.pathname
    if (!pathname.startsWith('/api')) {
      serveStatic(response, pathname, resolvedStaticDir)
      return
    }

    const body = await readBody(request)
    if (pathname === '/api/local/health') {
      sendJson(response, ok({ status: 'ok', product: 'VOC智声', dataVersion: database.version }))
      return
    }
    if (pathname === '/api/local/runtime') {
      const address = server.address()
      const runtimePort =
        publicPort || (typeof address === 'object' && address ? address.port : port)
      sendJson(
        response,
        ok({
          product: 'VOC智声',
          pcUrl: `http://127.0.0.1:${runtimePort}/#/login`,
          h5Url: `http://${getLanAddress()}:${runtimePort}/#/h5/home?token=voc-voice-local-demo-token`,
          resetAt: database.resetAt
        })
      )
      return
    }
    if (pathname === '/api/local/session/enter') {
      sendJson(response, ok({ token: 'voc-voice-local-demo-token', user: database.users[0] }))
      return
    }
    if (pathname === '/api/local/admin/reset' && request.method === 'POST') {
      database = createSeedData()
      persist()
      sendJson(response, ok({ resetAt: database.resetAt }))
      return
    }
    if (isStreamRequest(pathname, request)) {
      streamResponse(response, body, pathname)
      return
    }
    if (isDownloadRequest(pathname)) {
      sendDownload(response, database)
      return
    }
    const result = buildResult(
      pathname,
      request.method || 'GET',
      { ...Object.fromEntries(url.searchParams), ...body },
      database
    )
    if (request.method !== 'GET') persist()
    sendJson(response, ok(result))
  })

  await new Promise((resolve, reject) => {
    server.once('error', reject)
    server.listen(port, host, resolve)
  })

  const address = server.address()
  const runtimePort = publicPort || (typeof address === 'object' && address ? address.port : port)
  return {
    server,
    port: runtimePort,
    pcUrl: `http://127.0.0.1:${runtimePort}/#/login`,
    h5Url: `http://${getLanAddress()}:${runtimePort}/#/h5/home?token=voc-voice-local-demo-token`,
    reset: () => {
      database = createSeedData()
      persist()
      return database.resetAt
    }
  }
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const requestedPort = Number(process.env.VOC_DEMO_PORT || process.argv[2] || 4174)
  const publicPort = Number(process.env.VOC_DEMO_PUBLIC_PORT || 0) || undefined
  const runtime = await startLocalDemoServer({ port: requestedPort, publicPort })
  console.log(`VOC智声本地服务: ${runtime.pcUrl}`)
  console.log(`VOC智声H5地址: ${runtime.h5Url}`)
}
